package org.kaguya;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.BitSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SimilarityChecker {
    private static final int HASH_BITS = 64; // 使用64位哈希
    private double similarityThreshold = 0.8; // 默认阈值改为成员变量
    private boolean processNonText = false;    // 是否处理非文本文件

    // 用于存储文件的SimHash值
    private static class SimHashValue {
        private final BitSet bits;
        private final String fileName;

        public SimHashValue(BitSet bits, String fileName) {
            this.bits = bits;
            this.fileName = fileName;
        }
    }

    // 计算文件的相似度并返回相似文件组
    public Map<String, SimilarityResult.SimilarityGroup> findSimilarFiles(List<File> files) {
        List<SimHashValue> simHashValues = new ArrayList<>();
        Map<String, SimilarityResult.SimilarityGroup> similarGroups = new HashMap<>();

        // 计算所有文件的SimHash值
        for (File file : files) {
            try {
                if (isTextFile(file) || processNonText) {
                    BitSet simHash = calculateSimHash(file);
                    simHashValues.add(new SimHashValue(simHash, file.getName()));
                }
            } catch (IOException e) {
                System.err.println("处理文件时出错: " + file.getName());
                e.printStackTrace();
            }
        }

        // 比较所有文件对
        for (int i = 0; i < simHashValues.size(); i++) {
            for (int j = i + 1; j < simHashValues.size(); j++) {
                SimHashValue file1 = simHashValues.get(i);
                SimHashValue file2 = simHashValues.get(j);
                
                double similarity = calculateSimilarity(file1.bits, file2.bits);
                if (similarity >= similarityThreshold) {  // 使用设定的阈值
                    String key = file1.bits.toString();
                    addToSimilarityGroup(similarGroups, key, file1.fileName, file2.fileName, similarity);
                }
            }
        }

        // 移除只有一个文件的组
        similarGroups.entrySet().removeIf(entry -> entry.getValue().getFiles().size() <= 1);
        
        return similarGroups;
    }

    private BitSet calculateSimHash(File file) throws IOException {
        if (isTextFile(file)) {
            // 文本文件处理逻辑保持不变
            String content = Files.readString(file.toPath(), StandardCharsets.UTF_8);
            return calculateTextSimHash(content);
        } else {
            // 二进制文件处理逻辑
            return calculateBinarySimHash(file);
        }
    }

    private BitSet calculateTextSimHash(String content) {
        // 原来的文本SimHash计算逻辑
        List<String> features = extractFeatures(content);
        return calculateSimHashFromFeatures(features);
    }

    private BitSet calculateBinarySimHash(File file) throws IOException {
        // 二进制文件的SimHash计算
        byte[] content = Files.readAllBytes(file.toPath());
        List<String> features = new ArrayList<>();
        
        // 每8字节作为一个特征
        for (int i = 0; i < content.length - 7; i += 8) {
            byte[] chunk = Arrays.copyOfRange(content, i, i + 8);
            features.add(Base64.getEncoder().encodeToString(chunk));
        }
        
        return calculateSimHashFromFeatures(features);
    }

    private BitSet calculateSimHashFromFeatures(List<String> features) {
        int[] weights = new int[HASH_BITS];
        
        for (String feature : features) {
            BitSet hash = hashFeature(feature);
            for (int i = 0; i < HASH_BITS; i++) {
                if (hash.get(i)) {
                    weights[i]++;
                } else {
                    weights[i]--;
                }
            }
        }
        
        BitSet simHash = new BitSet(HASH_BITS);
        for (int i = 0; i < HASH_BITS; i++) {
            if (weights[i] > 0) {
                simHash.set(i);
            }
        }
        
        return simHash;
    }

    private List<String> extractFeatures(String content) {
        List<String> features = new ArrayList<>();
        // 使用3-gram分词
        int gramSize = 3;
        for (int i = 0; i < content.length() - gramSize + 1; i++) {
            features.add(content.substring(i, i + gramSize));
        }
        return features;
    }

    private BitSet hashFeature(String feature) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] hash = md.digest(feature.getBytes(StandardCharsets.UTF_8));
            BitSet bits = new BitSet(HASH_BITS);
            
            // 使用前8个字节（64位）
            for (int i = 0; i < 8; i++) {
                for (int j = 0; j < 8; j++) {
                    if ((hash[i] & (1 << j)) != 0) {
                        bits.set(i * 8 + j);
                    }
                }
            }
            return bits;
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("MD5算法不可用", e);
        }
    }

    private double calculateSimilarity(BitSet bits1, BitSet bits2) {
        BitSet xor = (BitSet) bits1.clone();
        xor.xor(bits2);
        int hammingDistance = xor.cardinality();
        return 1.0 - (double) hammingDistance / HASH_BITS;
    }

    private boolean isTextFile(File file) {
        // 简单的文本文件检测
        String name = file.getName().toLowerCase();
        String[] textExtensions = {
            ".txt", ".java", ".py", ".cpp", ".c", ".h", ".hpp", ".cs", ".js", 
            ".html", ".css", ".xml", ".json", ".md", ".log", ".ini", ".conf",
            ".properties", ".sql", ".sh", ".bat", ".cmd"
        };
        
        for (String ext : textExtensions) {
            if (name.endsWith(ext)) {
                return true;
            }
        }
        
        return false;
    }

    // 设置相似度阈值
    public void setSimilarityThreshold(double threshold) {
        if (threshold <= 0 || threshold > 1) {
            throw new IllegalArgumentException("相似度阈值必须在0到1之间");
        }
        this.similarityThreshold = threshold;
    }

    // 设置是否处理非文本文件
    public void setProcessNonText(boolean process) {
        this.processNonText = process;
    }

    private void addToSimilarityGroup(Map<String, SimilarityResult.SimilarityGroup> groups, 
                                    String key, String fileName1, String fileName2, double similarity) {
        SimilarityResult.SimilarityGroup group = groups.computeIfAbsent(key, 
            k -> new SimilarityResult.SimilarityGroup());
        
        // 检查文件是否已经在组中
        boolean hasFile1 = group.getFiles().stream()
            .anyMatch(f -> f.getFileName().equals(fileName1));
        boolean hasFile2 = group.getFiles().stream()
            .anyMatch(f -> f.getFileName().equals(fileName2));
            
        // 添加文件1（如果不存在）
        if (!hasFile1) {
            group.addFile(fileName1, 1.0); // 第一个文件作为基准文件
        }
        
        // 添加文件2（如果不存在）
        if (!hasFile2) {
            group.addFile(fileName2, similarity);
        }
    }
} 
