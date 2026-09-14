package com.wwl.service.impl;

import com.baidu.aip.ocr.AipOcr;
import com.wwl.model.entity.MovieTicket;
import com.wwl.mapper.TicketMapper;
import com.wwl.service.TicketService;
import com.wwl.common.util.BaiDuOcrUtil;
import com.wwl.common.util.TicketPictureParseUtil;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

/**
 * 电影票OCR识别业务实现类
 * 作者：王文伟
 * 时间：2026/5/9 下午2:47
 * 描述：上传电影票图片 + 百度OCR文字识别 + 信息解析入库
 */
@Slf4j
@Service
public class TicketServiceImpl implements TicketService {

    @Resource
    private BaiDuOcrUtil ocrUtil;

    @Resource
    private TicketMapper ticketMapper;

    /**
     * 上传电影票图片并自动解析信息入库
     * @param file 电影票图片文件
     * @return 解析封装后的电影票实体信息
     * @throws IOException 文件读取异常
     */
    @Override
    public MovieTicket uploadAndParseTicket(MultipartFile file) throws IOException {
        // 1. 获取上传图片字节数组
        byte[] imgBytes = file.getBytes();

        // 2. 获取百度OCR客户端实例，调用通用文字识别接口
        AipOcr client = ocrUtil.getOcrClient();
        HashMap<String, String> option = new HashMap<>();
        JSONObject result = client.basicGeneral(imgBytes, option);
        log.info("百度OCR识别原始结果：{}", result);

        // 3. 遍历拼接所有识别到的文本内容
        StringBuilder allText = new StringBuilder();
        JSONArray wordsArray = result.getJSONArray("words_result");
        for (int i = 0; i < wordsArray.length(); i++) {
            String text = wordsArray.getJSONObject(i).getString("words");
            allText.append(text).append("\n");
        }
        String totalText = allText.toString();
        log.info("图片完整识别文本：{}", totalText);

        // 4. 调用解析工具类提取对应字段信息
        String cinemaName = TicketPictureParseUtil.getCinema(totalText);
        String movieName = TicketPictureParseUtil.getMovieName(totalText);
        String startDate = TicketPictureParseUtil.getStartDate(totalText);
        String startTime = TicketPictureParseUtil.getStartTime(totalText);
        String endTime = TicketPictureParseUtil.getEndTime(totalText);

        // 解析座位信息并拼接为字符串
        List<String> seatList = TicketPictureParseUtil.getSeatList(totalText);
        String seatInfo = String.join(",", seatList);
        // 统计座位数量
        Integer seatCount = seatList.size();

        // 解析单价与订单总价
        String seatPrice = TicketPictureParseUtil.getSeatPrice(totalText);
        String totalPrice = TicketPictureParseUtil.getTotalPrice(totalText);

        // 5. 使用建造者模式封装电影票实体数据
        MovieTicket ticket = MovieTicket.builder()
                .cinemaName(cinemaName)
                .movieName(movieName)
                .startDate(startDate)
                .startTime(startTime)
                .endTime(endTime)
                .seatInfo(seatInfo)
                .seatCount(seatCount)
                .seatPrice(seatPrice)
                .totalPrice(totalPrice)
                .allText(totalText)
                .build();

        // 6. 将解析完成的数据插入数据库
        ticketMapper.insertTicketPicture(ticket);
        return ticket;
    }
}
