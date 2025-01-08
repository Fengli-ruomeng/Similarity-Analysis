package org.kaguya;

import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipFile;
import java.io.*;
import java.nio.file.*;

public class ZipUtil {
    private static final String TEMP_DIR = "temp_unzip_";

    public File unzipToTemp(File zipFile) throws IOException {
        // 创建临时目录
        File tempDir = Files.createTempDirectory(TEMP_DIR).toFile();
        tempDir.deleteOnExit();

        try (ZipFile zip = new ZipFile(zipFile, "GBK")) {  // 使用GBK编码打开zip文件
            // 遍历压缩包中的文件
            zip.getEntries().asIterator().forEachRemaining(entry -> {
                try {
                    extractEntry(zip, entry, tempDir);
                } catch (IOException e) {
                    System.err.println("无法解压文件: " + entry.getName());
                    e.printStackTrace();
                }
            });
        } catch (IOException e) {
            deleteDirectory(tempDir);
            throw e;
        }

        return tempDir;
    }

    private void extractEntry(ZipFile zip, ZipArchiveEntry entry, File destDir) throws IOException {
        String sanitizedName = sanitizeFileName(entry.getName());
        File destFile = newFile(destDir, sanitizedName);

        if (entry.isDirectory()) {
            if (!destFile.mkdirs() && !destFile.isDirectory()) {
                throw new IOException("无法创建目录: " + destFile);
            }
            return;
        }

        // 确保父目录存在
        File parent = destFile.getParentFile();
        if (!parent.exists() && !parent.mkdirs()) {
            throw new IOException("无法创建目录: " + parent);
        }

        // 解压文件
        try (InputStream is = zip.getInputStream(entry);
             BufferedInputStream bis = new BufferedInputStream(is);
             FileOutputStream fos = new FileOutputStream(destFile);
             BufferedOutputStream bos = new BufferedOutputStream(fos)) {

            byte[] buffer = new byte[8192];
            int count;
            while ((count = bis.read(buffer)) != -1) {
                bos.write(buffer, 0, count);
            }
        }
    }

    private String sanitizeFileName(String fileName) {
        // 替换不合法的文件名字符
        return fileName.replaceAll("[\\\\/:*?\"<>|]", "_");
    }

    private File newFile(File destinationDir, String entryName) throws IOException {
        File destFile = new File(destinationDir, entryName);

        String destDirPath = destinationDir.getCanonicalPath();
        String destFilePath = destFile.getCanonicalPath();

        if (!destFilePath.startsWith(destDirPath + File.separator)) {
            throw new IOException("Entry is outside of target dir: " + entryName);
        }

        return destFile;
    }

    private void deleteDirectory(File directory) {
        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    deleteDirectory(file);
                } else {
                    file.delete();
                }
            }
        }
        directory.delete();
    }
} 