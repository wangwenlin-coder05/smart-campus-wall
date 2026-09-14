import java.io.*;
import java.nio.file.*;
import java.util.regex.*;

/**
 * 三层架构实体类批量生成工具
 * 使用方法：javac ModelClassGenerator.java && java ModelClassGenerator
 */
public class ModelClassGenerator {
    
    private static final String ENTITY_DIR = "D:\\graduation_project\\src\\main\\java\\com\\wwl\\entity";
    private static final String DTO_DIR = "D:\\graduation_project\\src\\main\\java\\com\\wwl\\model\\dto";
    private static final String VO_DIR = "D:\\graduation_project\\src\\main\\java\\com\\wwl\\model\\vo";
    
    public static void main(String[] args) throws Exception {
        System.out.println("开始批量生成DTO和VO类...\n");
        
        File dir = new File(ENTITY_DIR);
        File[] files = dir.listFiles((d, name) -> name.endsWith(".java") && !name.contains("VO"));
        
        int count = 0;
        for (File file : files) {
            String content = new String(Files.readAllBytes(file.toPath()), "UTF-8");
            String className = extractClassName(content);
            
            if (className != null && !className.equals("OrderInfo") && !className.equals("User")) {
                System.out.println("处理: " + className);
                generateDTO(content, className);
                generateVO(content, className);
                count++;
            }
        }
        
        System.out.println("\n完成！共生成 " + count + " 个类的DTO和VO");
    }
    
    private static String extractClassName(String content) {
        Matcher m = Pattern.compile("public\\s+class\\s+(\\w+)").matcher(content);
        return m.find() ? m.group(1) : null;
    }
    
    private static void generateDTO(String content, String className) throws IOException {
        String dtoName = className + "DTO";
        StringBuilder sb = new StringBuilder();
        
        sb.append("package com.wwl.model.dto;\n\n");
        sb.append("import com.fasterxml.jackson.annotation.JsonFormat;\n");
        sb.append("import lombok.Data;\n\n");
        
        if (content.contains("LocalDateTime")) sb.append("import java.time.LocalDateTime;\n");
        if (content.contains("LocalDate")) sb.append("import java.time.LocalDate;\n");
        if (content.contains("BigDecimal")) sb.append("import java.math.BigDecimal;\n");
        if (content.contains("List")) sb.append("import java.util.List;\n");
        
        sb.append("\n/**\n * ").append(className).append("请求参数DTO类\n */\n");
        sb.append("@Data\npublic class ").append(dtoName).append(" {\n");
        
        // 提取字段
        extractFields(content, sb, false);
        
        sb.append("\n    // ==================== 查询专用字段 ====================\n");
        sb.append("    private Integer sceneType;\n");
        sb.append("    private Integer pageNum;\n");
        sb.append("    private Integer pageSize;\n");
        sb.append("}\n");
        
        Files.write(Paths.get(DTO_DIR + "\\" + dtoName + ".java"), sb.toString().getBytes("UTF-8"));
        System.out.println("  ✓ DTO: " + dtoName);
    }
    
    private static void generateVO(String content, String className) throws IOException {
        String voName = className + "VO";
        StringBuilder sb = new StringBuilder();
        
        sb.append("package com.wwl.model.vo;\n\n");
        sb.append("import com.fasterxml.jackson.annotation.JsonFormat;\n");
        sb.append("import com.fasterxml.jackson.annotation.JsonIgnore;\n");
        sb.append("import lombok.Data;\n\n");
        
        if (content.contains("LocalDateTime")) sb.append("import java.time.LocalDateTime;\n");
        if (content.contains("LocalDate")) sb.append("import java.time.LocalDate;\n");
        if (content.contains("BigDecimal")) sb.append("import java.math.BigDecimal;\n");
        if (content.contains("List")) sb.append("import java.util.List;\n");
        
        sb.append("\n/**\n * ").append(className).append("视图返回VO类\n */\n");
        sb.append("@Data\npublic class ").append(voName).append(" {\n");
        sb.append("    @JsonIgnore\n    private Long id;\n\n");
        
        extractFields(content, sb, true);
        sb.append("}\n");
        
        Files.write(Paths.get(VO_DIR + "\\" + voName + ".java"), sb.toString().getBytes("UTF-8"));
        System.out.println("  ✓ VO: " + voName);
    }
    
    private static void extractFields(String content, StringBuilder sb, boolean isVO) {
        Pattern p = Pattern.compile("/\\*\\*([^*]|\\*[^/])*\\*/\\s*private\\s+(\\S+)\\s+(\\w+);");
        Matcher m = p.matcher(content);
        
        while (m.find()) {
            String comment = m.group(0).split("private")[0].trim();
            String type = m.group(2);
            String name = m.group(3);
            
            if (isVO && (name.equals("id") || name.equals("isDeleted"))) continue;
            
            sb.append("    ").append(comment).append("\n");
            sb.append("    private ").append(type).append(" ").append(name).append(";\n\n");
        }
    }
}
