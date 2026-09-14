#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
三层架构实体类批量生成脚本
自动读取com.wwl.entity下的Entity类，生成对应的DTO和VO类
"""

import os
import re
from pathlib import Path

# 项目根目录
PROJECT_ROOT = r"D:\graduation_project"
ENTITY_DIR = os.path.join(PROJECT_ROOT, "src", "main", "java", "com", "wwl", "entity")
MODEL_ENTITY_DIR = os.path.join(PROJECT_ROOT, "src", "main", "java", "com", "wwl", "model", "entity")
MODEL_DTO_DIR = os.path.join(PROJECT_ROOT, "src", "main", "java", "com", "wwl", "model", "dto")
MODEL_VO_DIR = os.path.join(PROJECT_ROOT, "src", "main", "java", "com", "wwl", "model", "vo")

# 需要排除的字段（页面展示字段，不应出现在Entity中）
EXCLUDE_FIELDS_FOR_ENTITY = [
    'orderImageList', 'mediaUrlList', 'hotComments',
    'startAddressText', 'endAddressText', 'nickname', 'avatar',
    'replyNickname', 'categoryName', 'subCategoryName',
    'userUid'
]

# 查询专用字段（只出现在DTO中）
DTO_EXTRA_FIELDS = """
    // ==================== 查询专用字段 ====================

    /** 查询场景标识 */
    private Integer sceneType;

    /** 分页页码 */
    private Integer pageNum;

    /** 每页条数 */
    private Integer pageSize;
"""

# VO额外字段模板（联表查询字段）
VO_EXTRA_FIELDS_TEMPLATE = """
    /** 取件完整地址文本（联表查询） */
    private String startAddressText;

    /** 送达完整地址文本（联表查询） */
    private String endAddressText;
"""

def read_entity_file(filepath):
    """读取Entity文件内容"""
    with open(filepath, 'r', encoding='utf-8') as f:
        return f.read()

def extract_class_info(content):
    """提取类名、包名、import、字段等信息"""
    # 提取包名
    package_match = re.search(r'package\s+([\w.]+);', content)
    package_name = package_match.group(1) if package_match else "com.wwl.entity"
    
    # 提取类名
    class_match = re.search(r'public\s+class\s+(\w+)', content)
    class_name = class_match.group(1) if class_match else "Unknown"
    
    # 提取import语句
    imports = re.findall(r'import\s+([^;]+);', content)
    
    # 提取类注释
    class_comment_match = re.search(r'/\*\*[\s\S]*?\*/\s*@Data', content)
    class_comment = class_comment_match.group(0) if class_comment_match else ""
    
    # 提取字段
    fields = []
    field_pattern = r'(/\*\*[\s\S]*?\*/\s*)?(@\w+[^\n]*\s+)*private\s+([\w<>[\],\s]+)\s+(\w+);'
    for match in re.finditer(field_pattern, content):
        comment = match.group(1) or ""
        annotations = match.group(2) or ""
        field_type = match.group(3).strip()
        field_name = match.group(4)
        fields.append({
            'comment': comment,
            'annotations': annotations,
            'type': field_type,
            'name': field_name
        })
    
    return {
        'package': package_name,
        'class_name': class_name,
        'imports': imports,
        'class_comment': class_comment,
        'fields': fields
    }

def generate_dto(class_info):
    """生成DTO类"""
    class_name = class_info['class_name']
    dto_name = class_name + "DTO"
    
    # 构建import
    imports = set()
    imports.add("import com.fasterxml.jackson.annotation.JsonFormat;")
    imports.add("import lombok.Data;")
    
    for field in class_info['fields']:
        if 'LocalDateTime' in field['type'] or 'LocalDate' in field['type']:
            imports.add("import java.time.LocalDateTime;")
            imports.add("import java.time.LocalDate;")
        if 'BigDecimal' in field['type']:
            imports.add("import java.math.BigDecimal;")
        if 'List' in field['type']:
            imports.add("import java.util.List;")
    
    # 构建字段
    fields_str = ""
    for field in class_info['fields']:
        fields_str += f"    {field['comment']}"
        if '@JsonIgnore' not in field['annotations']:
            fields_str += field['annotations'].replace('@JsonIgnore', '').replace('@NotBlank(message = "用户标识不能为空")', '').replace('@NotNull(message = "关联帖子ID不能为空")', '').replace('@NotBlank(message = "评论内容不能为空")', '')
        fields_str += f"    private {field['type']} {field['name']};\n\n"
    
    # 添加DTO专属字段
    fields_str += DTO_EXTRA_FIELDS
    
    dto_content = f"""package com.wwl.model.dto;

{''.join(sorted(imports))}

/**
 * {class_name}请求参数DTO类
 * 用于接收前端查询、提交、修改的入参
 * 包含查询条件、分页参数、筛选字段等
 */
@Data
public class {dto_name} {{
{fields_str}}}
"""
    return dto_content

def generate_vo(class_info):
    """生成VO类"""
    class_name = class_info['class_name']
    vo_name = class_name + "VO"
    
    # 构建import
    imports = set()
    imports.add("import com.fasterxml.jackson.annotation.JsonFormat;")
    imports.add("import com.fasterxml.jackson.annotation.JsonIgnore;")
    imports.add("import lombok.Data;")
    
    for field in class_info['fields']:
        if 'LocalDateTime' in field['type'] or 'LocalDate' in field['type']:
            imports.add("import java.time.LocalDateTime;")
            imports.add("import java.time.LocalDate;")
        if 'BigDecimal' in field['type']:
            imports.add("import java.math.BigDecimal;")
        if 'List' in field['type']:
            imports.add("import java.util.List;")
    
    # 构建字段（过滤掉@JsonIgnore字段，但保留id并添加@JsonIgnore）
    fields_str = ""
    for field in class_info['fields']:
        # 跳过isDeleted、updateTime等内部字段
        if field['name'] in ['isDeleted', 'updatedAt', 'updateTime']:
            continue
        
        comment = field['comment']
        annotations = field['annotations']
        
        # id字段添加@JsonIgnore
        if field['name'] == 'id':
            if '@JsonIgnore' not in annotations:
                comment += "\n    @JsonIgnore"
        
        # 移除验证注解
        annotations = annotations.replace('@NotBlank(message = "用户标识不能为空")', '')
        annotations = annotations.replace('@NotNull(message = "关联帖子ID不能为空")', '')
        annotations = annotations.replace('@NotBlank(message = "评论内容不能为空")', '')
        
        fields_str += f"    {comment}"
        fields_str += annotations
        fields_str += f"    private {field['type']} {field['name']};\n\n"
    
    vo_content = f"""package com.wwl.model.vo;

{''.join(sorted(imports))}

/**
 * {class_name}视图返回VO类
 * 用于后端返回前端页面展示
 * 可额外增加联表查询字段，用于页面直接展示
 */
@Data
public class {vo_name} {{
{fields_str}}}
"""
    return vo_content

def main():
    """主函数"""
    print("=" * 60)
    print("开始批量生成三层架构实体类...")
    print("=" * 60)
    
    # 确保目标目录存在
    os.makedirs(MODEL_ENTITY_DIR, exist_ok=True)
    os.makedirs(MODEL_DTO_DIR, exist_ok=True)
    os.makedirs(MODEL_VO_DIR, exist_ok=True)
    
    # 遍历所有Entity文件
    entity_files = [f for f in os.listdir(ENTITY_DIR) if f.endswith('.java')]
    
    generated_count = 0
    for filename in entity_files:
        filepath = os.path.join(ENTITY_DIR, filename)
        print(f"\n处理: {filename}")
        
        try:
            content = read_entity_file(filepath)
            class_info = extract_class_info(content)
            
            # 生成DTO
            dto_content = generate_dto(class_info)
            dto_path = os.path.join(MODEL_DTO_DIR, f"{class_info['class_name']}DTO.java")
            with open(dto_path, 'w', encoding='utf-8') as f:
                f.write(dto_content)
            print(f"  ✓ 生成DTO: {class_info['class_name']}DTO.java")
            
            # 生成VO
            vo_content = generate_vo(class_info)
            vo_path = os.path.join(MODEL_VO_DIR, f"{class_info['class_name']}VO.java")
            with open(vo_path, 'w', encoding='utf-8') as f:
                f.write(vo_content)
            print(f"  ✓ 生成VO: {class_info['class_name']}VO.java")
            
            generated_count += 1
            
        except Exception as e:
            print(f"  ✗ 处理失败: {str(e)}")
    
    print("\n" + "=" * 60)
    print(f"批量生成完成！共处理 {generated_count} 个实体类")
    print(f"  - Entity目录: {MODEL_ENTITY_DIR}")
    print(f"  - DTO目录: {MODEL_DTO_DIR}")
    print(f"  - VO目录: {MODEL_VO_DIR}")
    print("=" * 60)

if __name__ == "__main__":
    main()
