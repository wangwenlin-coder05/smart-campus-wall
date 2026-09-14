import java.io.*;
import java.nio.file.*;

/**
 * 批量替换MyBatis XML中的resultType引用
 */
public class BatchReplaceXML {
    
    private static final String XML_DIR = "D:\\graduation_project\\src\\main\\resources\\com\\wwl\\mapper";
    
    public static void main(String[] args) throws Exception {
        System.out.println("开始批量替换XML中的resultType引用...\n");
        
        File dir = new File(XML_DIR);
        File[] files = dir.listFiles((d, name) -> name.endsWith(".xml"));
        
        int fileCount = 0;
        int replaceCount = 0;
        
        for (File file : files) {
            String content = new String(Files.readAllBytes(file.toPath()), "UTF-8");
            String originalContent = content;
            
            // 替换Entity的resultType
            content = content.replaceAll(
                "resultType=\"com\\.wwl\\.entity\\.([A-Z][a-zA-Z]*)\"",
                "resultType=\"com.wwl.model.entity.$1\""
            );
            
            // 替换VO的resultType
            content = content.replaceAll(
                "resultType=\"com\\.wwl\\.entity\\.([A-Z][a-zA-Z]*VO)\"",
                "resultType=\"com.wwl.model.vo.$1\""
            );
            
            if (!content.equals(originalContent)) {
                Files.write(file.toPath(), content.getBytes("UTF-8"));
                System.out.println("✓ 已修改: " + file.getName());
                fileCount++;
                
                // 计算替换次数
                String diff = originalContent.replaceAll("com\\.wwl\\.entity\\.", "");
                replaceCount += (originalContent.length() - diff.length()) / "com.wwl.entity.".length();
            }
        }
        
        System.out.println("\n========================================");
        System.out.println("XML批量替换完成！");
        System.out.println("修改文件数: " + fileCount);
        System.out.println("替换引用数: " + replaceCount);
        System.out.println("========================================");
    }
}
