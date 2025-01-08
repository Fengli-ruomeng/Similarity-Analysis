package org.kaguya;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class FileUtil {
    private final ZipUtil zipUtil = new ZipUtil();
    private final Set<File> tempDirs = new HashSet<>();
    private final List<File> extractedFiles = new ArrayList<>();

    public List<File> readFilesFromFolder(String folderPath, String[] extensions, boolean isInverted, boolean processZip) {
        List<File> fileList = new ArrayList<>();
        File folder = new File(folderPath);
        
        if (!folder.exists() || !folder.isDirectory()) {
            return fileList;
        }
        
        extractedFiles.clear();
        
        traverseFolder(folder, fileList, extensions, isInverted, processZip);
        
        if (processZip) {
            fileList.addAll(extractedFiles);
        }
        
        return fileList;
    }
    
    private void traverseFolder(File folder, List<File> fileList, String[] extensions, boolean isInverted, boolean processZip) {
        File[] files = folder.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isFile()) {
                    if (processZip && isZipFile(file)) {
                        try {
                            File tempDir = zipUtil.unzipToTemp(file);
                            tempDirs.add(tempDir);
                            List<File> zipFiles = new ArrayList<>();
                            traverseFolder(tempDir, zipFiles, extensions, isInverted, false);
                            extractedFiles.addAll(zipFiles);
                        } catch (IOException e) {
                            System.err.println("无法处理压缩文件: " + file.getName());
                            e.printStackTrace();
                        }
                    }
                    
                    if (!processZip || !isZipFile(file)) {
                        boolean matches = hasMatchingExtension(file, extensions);
                        if (extensions.length == 0 || (matches != isInverted)) {
                            fileList.add(file);
                        }
                    }
                } else if (file.isDirectory()) {
                    traverseFolder(file, fileList, extensions, isInverted, processZip);
                }
            }
        }
    }
    
    private boolean isZipFile(File file) {
        return file.getName().toLowerCase().endsWith(".zip");
    }
    
    private void cleanupTempDirs() {
        for (File tempDir : tempDirs) {
            deleteDirectory(tempDir);
        }
        tempDirs.clear();
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
    
    private boolean hasMatchingExtension(File file, String[] extensions) {
        String fileName = file.getName().toLowerCase();
        for (String extension : extensions) {
            extension = extension.trim().toLowerCase();
            if (extension.isEmpty()) continue;
            if (!extension.startsWith(".")) {
                extension = "." + extension;
            }
            if (fileName.endsWith(extension)) {
                return true;
            }
        }
        return false;
    }

    //输入一个List，然后根据这个List，通过MD5检查文件，将md5设为key，value设置为一个List，这个List里有所有都是这个md5的文件名。
    public Map<String,List<String>> checkFolder(List<File> files) {
        Hash hash = new Hash();
        ArrayList<String> list = new ArrayList<>();
        Map<String,Integer> count = new HashMap<>();
        Map<String,List<String>> name = new HashMap<>();
        Map<String,List<String>> finallycount = new HashMap<>();
        try {
            for (File file : files) {
                try {
                    if (file.exists() && file.isFile()) {  // 添加文件存在检查
                        String fileHash = hash.calculateMD5(file);
                        list.add(fileHash);
                    }
                } catch (IOException e) {
                    System.err.println("无法处理文件: " + file.getName());
                    e.printStackTrace();
                }
            }
            
            for (int x = 0; x < list.size(); x++) {
                if (!count.containsKey(list.get(x))){
                    count.put(list.get(x),1);
                    name.put(list.get(x), new ArrayList<>());
                    name.get(list.get(x)).add(files.get(x).getName());
                } else {
                    count.put(list.get(x), count.get(list.get(x)) + 1);
                    name.get(list.get(x)).add(files.get(x).getName());
                }
            }
            
            for (Map.Entry<String, Integer> maps : count.entrySet()) {
                if (maps.getValue() > 1){
                    finallycount.put(maps.getKey(),name.get(maps.getKey()));
                }
            }
            return finallycount;
        } catch (Exception error) {
            error.printStackTrace();
            throw new RuntimeException("检查文件夹时发生错误", error);
        }
    }
}

