package com.scmp.simple;


import com.scmp.simple.utils.PasswordUtils;
import org.junit.jupiter.api.Test;


import java.time.LocalDate;
import java.time.Month;
import java.time.format.DateTimeFormatter;


public class PasswordValidatorTest {

    @Test
    public void testValidatePassword() {
        LocalDate now = LocalDate.now();
        int day = now.getDayOfMonth();
        Month month = now.getMonth();

        // 构造原始密码格式：<月份英文缩写> + LAB2025 + yyyyMMdd + <月份英文缩写>
        String monthAbbr = month.name().substring(0, 3); // 前三个字母
        String dateStr = now.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String rawPassword = monthAbbr + "LAB2025" + dateStr + monthAbbr;
        // 测试不同月份的密码生成逻辑
        System.out.println(PasswordUtils.generateEncryptedPassword(LocalDate.now(),rawPassword));
    }

}