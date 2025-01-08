package org.kaguya;

import java.util.ArrayList;
import java.util.List;

public class SimilarityResult {
    public static class FileWithSimilarity {
        private final String fileName;
        private final double similarity;
        
        public FileWithSimilarity(String fileName, double similarity) {
            this.fileName = fileName;
            this.similarity = similarity;
        }
        
        public String getFileName() {
            return fileName;
        }
        
        public double getSimilarity() {
            return similarity;
        }
    }

    public static class SimilarityGroup {
        private final List<FileWithSimilarity> files;
        
        public SimilarityGroup() {
            this.files = new ArrayList<>();
        }
        
        public void addFile(String fileName, double similarity) {
            files.add(new FileWithSimilarity(fileName, similarity));
        }
        
        public List<FileWithSimilarity> getFiles() {
            return files;
        }
    }
} 