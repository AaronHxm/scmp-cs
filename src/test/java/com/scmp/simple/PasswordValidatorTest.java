package com.scmp.simple;

import org.junit.jupiter.api.Test;
import java.time.LocalDate;
import java.time.Month;
import static org.junit.jupiter.api.Assertions.*;

public class PasswordValidatorTest {

    @Test
    public void testValidatePassword() {
        // 测试不同月份的密码生成逻辑
        testForMonth(Month.JANUARY, "JanLAB2025Jan");
        testForMonth(Month.FEBRUARY, "FebLAB2025Feb");
        testForMonth(Month.MARCH, "MarLAB2025Mar");
        testForMonth(Month.APRIL, "AprLAB2025Apr");
        testForMonth(Month.MAY, "MayLAB2025May");
        testForMonth(Month.JUNE, "JunLAB2025Jun");
        testForMonth(Month.JULY, "JulLAB2025Jul");
        testForMonth(Month.AUGUST, "AugLAB2025Aug");
        testForMonth(Month.SEPTEMBER, "SepLAB2025Sep");
        testForMonth(Month.OCTOBER, "OctLAB2025Oct");
        testForMonth(Month.NOVEMBER, "NovLAB2025Nov");
        testForMonth(Month.DECEMBER, "DecLAB2025Dec");
    }

    private void testForMonth(Month month, String expectedPassword) {
        // 模拟当前日期为当月15日
        LocalDate testDate = LocalDate.of(2025, month, 15);
        
        // 测试Base64加密方式 (day % 3 == 0)
        // 使用反射测试私有方法
        assertTrue(invokeValidatePassword(expectedPassword, testDate.withDayOfMonth(15)));
        
        // 测试MD5加密方式 (day % 3 == 1)
        assertTrue(invokeValidatePassword(expectedPassword, testDate.withDayOfMonth(16)));
        
        // 测试SHA256加密方式 (day % 3 == 2)
        assertTrue(invokeValidatePassword(expectedPassword, testDate.withDayOfMonth(17)));
        
        // 测试错误密码
        assertFalse(invokeValidatePassword("WrongPassword", testDate));
        
    private boolean invokeValidatePassword(String password, LocalDate date) {
        try {
            OrderGrabberApp app = new OrderGrabberApp();
            Method method = OrderGrabberApp.class.getDeclaredMethod("validatePassword", String.class);
            method.setAccessible(true);
            
            // 临时修改系统日期
            System.setProperty("current.date", date.toString());
            boolean result = (boolean) method.invoke(app, password);
            System.clearProperty("current.date");
            return result;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    }
}