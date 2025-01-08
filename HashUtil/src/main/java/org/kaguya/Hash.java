package org.kaguya;

import java.io.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

public class Hash {

    //使用md5算法，反正MD5够快
    public String calculateMD5(File file) throws IOException {
        try {
            MessageDigest digest = MessageDigest.getInstance("MD5");
            //try-with-resources+8K缓冲区防止读取速度太慢
            try (BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file))) {
                byte[] buffer = new byte[8192];
                int count;
                //总之就是统计文件还有没有读完，一点一点的把文件内容更新到digest里。
                while ((count = bis.read(buffer)) > 0) {
                    digest.update(buffer, 0, count);
                }
            }

            //这个是算完的hash
            byte[] hash = digest.digest();
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                //无符号整数->16进制 (网上好像都这么写的)
                String hex = Integer.toHexString(0xff & b);
                //如果是一位就补个0变十六进制
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();

        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("MD5 算法不可用", e);
        }
    }

    //暂时不用
    public List<String> calculateMD5FromFolders(List<File> filelist){
        if (filelist == null){
            return null;
        }
        ArrayList<String> MD5list = new ArrayList<>();
        for (File file : filelist){
            try {
                MD5list.add(calculateMD5(file));
            } catch (IOException exception) {
                System.out.println("ERROR");
            }
        }
        return MD5list;
    }





}