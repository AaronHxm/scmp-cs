package com.scmp.simple.utils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDate;
import java.time.Month;
import java.time.format.DateTimeFormatter;
import java.util.Base64;

public class PasswordUtils {

    public static boolean validatePassword(String inputPassword) {
        try {
            LocalDate now = LocalDate.now();
            int day = now.getDayOfMonth();
            Month month = now.getMonth();

            // 构造原始密码格式：<月份英文缩写> + LAB2025 + yyyyMMdd + <月份英文缩写>
            String monthAbbr = month.name().substring(0, 3); // 前三个字母
            String dateStr = now.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
            String rawPassword = monthAbbr + "LAB2025" + dateStr + monthAbbr;

            // 使用工具类加密原始密码
            String encryptedPassword = PasswordUtils.generateEncryptedPassword(now, rawPassword);

            LogUtils.info("密码验证", "当前日期: " + now + ", 算法: " + (day % 3) + ", 原始密码: " + rawPassword + ", 加密后: " + encryptedPassword);

            return encryptedPassword.equals(inputPassword);

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static String generateEncryptedPassword(LocalDate date, String rawPassword) {
        try {
            int day = date.getDayOfMonth();
            
            switch (day % 3) {
                case 0:
                    return Base64.getEncoder().encodeToString(rawPassword.getBytes(StandardCharsets.UTF_8));
                case 1:
                    return md5(rawPassword);
                case 2:
                    return sha256(rawPassword);
                default:
                    return "";
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }
    
    private static String md5(String input) throws Exception {
        MessageDigest md5 = MessageDigest.getInstance("MD5");
        byte[] hash = md5.digest(input.getBytes(StandardCharsets.UTF_8));
        StringBuilder sb = new StringBuilder();
        for (byte b : hash) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
    
    private static String sha256(String input) throws Exception {
        MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
        byte[] hash = sha256.digest(input.getBytes(StandardCharsets.UTF_8));
        StringBuilder sb = new StringBuilder();
        for (byte b : hash) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}