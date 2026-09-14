import java.io.*;
import java.nio.file.*;
import java.util.*;

/**
 * 批量替换Entity引用工具
 * 将 com.wwl.entity.Xxx 替换为 com.wwl.model.entity.Xxx
 * 将 com.wwl.entity.XxxVO 替换为 com.wwl.model.vo.XxxVO
 */
public class BatchReplaceImports {
    
    private static final String SRC_DIR = "D:\\graduation_project\\src\\main\\java\\com\\wwl";
    
    public static void main(String[] args) throws Exception {
        System.out.println("开始批量替换Entity引用...\n");
        
        List<String> directories = Arrays.asList(
            SRC_DIR + "\\controller",
            SRC_DIR + "\\mapper",
            SRC_DIR + "\\service",
            SRC_DIR + "\\service\\impl",
            SRC_DIR + "\\util"
        );
        
        int fileCount = 0;
        int replaceCount = 0;
        
        for (String dirPath : directories) {
            File dir = new File(dirPath);
            if (!dir.exists()) continue;
            
            File[] files = dir.listFiles((d, name) -> name.endsWith(".java"));
            if (files == null) continue;
            
            for (File file : files) {
                String content = new String(Files.readAllBytes(file.toPath()), "UTF-8");
                String originalContent = content;
                
                // 替换Entity引用（排除OrderInfoVO等已经在vo包的）
                content = content.replaceAll(
                    "import com\\.wwl\\.entity\\.(?!OrderInfoVO)([A-Z][a-zA-Z]*);",
                    "import com.wwl.model.entity.$1;"
                );
                
                // 替换OrderInfoVO引用
                content = content.replaceAll(
                    "import com\\.wwl\\.entity\\.OrderInfoVO;",
                    "import com.wwl.model.vo.OrderInfoVO;"
                );
                
                if (!content.equals(originalContent)) {
                    Files.write(file.toPath(), content.getBytes("UTF-8"));
                    System.out.println("✓ 已修改: " + file.getName());
                    fileCount++;
                    
                    // 统计替换次数
                    replaceCount += countReplacements(originalContent, content);
                }
            }
        }
        
        System.out.println("\n========================================");
        System.out.println("批量替换完成！");
        System.out.println("修改文件数: " + fileCount);
        System.out.println("替换引用数: " + replaceCount);
        System.out.println("========================================");
    }
    
    private static int countReplacements(String original, String modified) {
        int count = 0;
        String[] origLines = original.split("\n");
        String[] modLines = modified.split("\n");
        
        for (int i = 0; i < Math.min(origLines.length, modLines.length); i++) {
            if (!origLines[i].equals(modLines[i]) && modLines[i].contains("import com.wwl.model")) {
                count++;
            }
        }
        return count;
    }
}
