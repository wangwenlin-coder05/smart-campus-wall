// utils/upload.js
import OSS from 'ali-oss';
import request from '@/utils/request.js';

// 简易生成 uuid
const uuid = () => {
  return Date.now().toString(36) + Math.random().toString(36).slice(2);
};

// 【企业级上传限制配置】
const UPLOAD_LIMIT = {
  image: { size: 20 * 1024 * 1024, suffix: ['jpg', 'jpeg', 'png', 'gif', 'webp', 'bmp'] },
  video: { size: 100 * 1024 * 1024, duration: 300, suffix: ['mp4', 'mov', 'avi', 'mkv'] },
  audio: { size: 50 * 1024 * 1024, duration: 900, suffix: ['mp3', 'wav', 'aac'] }
};

// 获取STS临时凭证
const getStsToken = async () => {
  const res = await request({ url: '/oss/sts', method: 'GET' });
  console.log('STS 接口返回的原始数据：', res);
  if (res && (res.code === 1 || res.code === 200)) {
    return res.data;
  }
  throw new Error(`获取OSS STS凭证失败，code=${res?.code}`);
};

// 获取文件类型、后缀
const getFileInfo = (filePath) => {
  const rawPath = String(filePath || '');
  // H5 chooseImage 常返回 blob:http://...，它没有真实后缀，直接 split 会把整段地址当后缀。
  const isBlobUrl = rawPath.startsWith('blob:');
  const cleanPath = rawPath.split('?')[0].split('#')[0];
  const matchedExt = cleanPath.match(/\.([a-zA-Z0-9]+)$/);
  const ext = isBlobUrl ? 'jpg' : (matchedExt ? matchedExt[1].toLowerCase() : 'jpg');
  let type = 'other';
  if (UPLOAD_LIMIT.image.suffix.includes(ext)) type = 'image';
  else if (UPLOAD_LIMIT.video.suffix.includes(ext)) type = 'video';
  else if (UPLOAD_LIMIT.audio.suffix.includes(ext)) type = 'audio';
  else type = 'image'; // 兜底：未知格式当作图片处理
  return { ext, type };
};

// 校验文件大小
const checkFileSize = (size, type) => {
  const limit = UPLOAD_LIMIT[type] || UPLOAD_LIMIT.image;
  if (size > limit.size) {
    const mb = (limit.size / 1024 / 1024).toFixed(0);
    throw new Error(`${type === 'image' ? '图片' : type === 'video' ? '视频' : '音频'}最大限制${mb}MB`);
  }
};

// 校验视频/音频时长
const checkMediaDuration = async (filePath) => {
  return new Promise((resolve, reject) => {
    uni.getVideoInfo({
      src: filePath,
      success: (res) => {
        const duration = Math.round(res.duration);
        if (duration > UPLOAD_LIMIT.video.duration) {
          reject(new Error(`视频最长限制${UPLOAD_LIMIT.video.duration}秒`));
        } else {
          resolve(true);
        }
      },
      fail: () => resolve(true)
    });
  });
};

/**
 * OSS直传上传文件
 * @param {String} filePath 本地临时文件路径
 * @param {String} category 分类
 * @param {Function} onProgress 上传进度回调
 * @param {String} customExt 自定义文件后缀（用于H5等无法从路径提取后缀的场景）
 * @returns {String} OSS线上文件URL
 */
export const uploadFile = async (filePath, category, onProgress, customExt) => {
  // 1. 获取文件信息并校验格式（注意解构时重命名 ext 避免重复声明）
  const { ext: fileExt, type } = getFileInfo(filePath);

  // 2. 获取文件大小
  const fileInfo = await new Promise((resolve) => {
    uni.getFileInfo({ filePath, success: resolve });
  });
  checkFileSize(fileInfo.size, type);

  // 3. 视频额外校验时长
  if (type === 'video') {
    await checkMediaDuration(filePath);
  }

  // 4. 获取OSS凭证
  const sts = await getStsToken();
  const client = new OSS({
    endpoint: `https://oss-${sts.region}.aliyuncs.com`,
    bucket: sts.bucket,
    accessKeyId: sts.accessKeyId,
    accessKeySecret: sts.accessKeySecret,
    stsToken: sts.securityToken,
    secure: true,
    refreshSTSToken: async () => {
      const newSts = await getStsToken();
      return {
        accessKeyId: newSts.accessKeyId,
        accessKeySecret: newSts.accessKeySecret,
        stsToken: newSts.securityToken
      };
    }
  });

  // 5. 生成OSS存储路径，优先使用传入的自定义后缀
  const date = new Date();
  const year = date.getFullYear();
  const month = String(date.getMonth() + 1).padStart(2, '0');
  const safeCategory = String(category || 'default')
    .split(/[\\/]+/)
    .map(part => part.replace(/[:*?"<>|]/g, '').trim())
    .filter(Boolean)
    .join('/') || 'default';
  // 只允许简单文件后缀进入 OSS key，避免 blob/http 地址污染文件名。
  const ext = /^[a-zA-Z0-9]+$/.test(customExt || '') ? customExt : (fileExt || 'jpg');
  const key = `${safeCategory}/${year}/${month}/${uuid()}.${ext}`;

  // 6. 处理上传内容：H5 下需要将 blob URL 转为 Blob 对象
  let uploadContent;
  if (typeof window !== 'undefined' && (filePath.startsWith('blob:') || filePath.startsWith('http'))) {
    const response = await fetch(filePath);
    uploadContent = await response.blob();
  } else {
    uploadContent = filePath; // 小程序/App 本地路径
  }

  // 7. 分片上传
  const result = await client.multipartUpload(key, uploadContent, {
    parallel: 3,
    partSize: 1024 * 1024,
    progress: (percent) => {
      onProgress && onProgress(Math.floor(percent * 100));
    }
  });

  // 返回纯净URL
  return result.res.requestUrls[0]?.split('?')[0];
};

/**
 * 批量上传（支持图片/视频混合，最多9个）
 * @param {Array} fileInfos 文件信息数组，每个元素包含 { path, ext }
 * @param {String} category 分类
 * @param {Function} onItemProgress 单个文件进度回调
 * @returns {Array} 上传后的URL数组
 */
export const uploadMultipleFiles = async (fileInfos, category, onItemProgress) => {
  if (fileInfos.length > 9) {
    throw new Error('最多只能上传9个文件');
  }
  const urlList = [];
  for (let i = 0; i < fileInfos.length; i++) {
    const { path, ext } = fileInfos[i];
    const url = await uploadFile(path, category, (progress) => {
      onItemProgress && onItemProgress(i, progress);
    }, ext);
    urlList.push(url);
  }
  return urlList;
};

// 兼容旧页面的 uploadImage 导入
export const uploadImage = uploadFile;
