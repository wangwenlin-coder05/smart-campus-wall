import java.io.*;
import java.nio.file.*;

/**
 * 批量修改Entity包名工具
 */
public class UpdatePackage {
    public static void main(String[] args) throws Exception {
        String dir = "D:\\graduation_project\\src\\main\\java\\com\\wwl\\model\\entity";
        File folder = new File(dir);
        File[] files = folder.listFiles((d, name) -> name.endsWith(".java"));
        
        int count = 0;
        for (File file : files) {
            String content = new String(Files.readAllBytes(file.toPath()), "UTF-8");
            String newContent = content.replaceAll("package\\s+com\\.wwl\\.entity;", "package com.wwl.model.entity;");
            
            if (!content.equals(newContent)) {
                Files.write(file.toPath(), newContent.getBytes("UTF-8"));
                System.out.println("✓ 已修改: " + file.getName());
                count++;
            }
        }
        
        System.out.println("\n共修改 " + count + " 个文件");
    }
}
