package com.scmp.simple;

import com.scmp.simple.manager.SchedulerManager;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

public class SchedulerManagerTest {
    private static SchedulerManager manager;

    @BeforeAll
    static void init() {
        manager = new SchedulerManager();
    }

    @AfterAll
    static void cleanup() {
        manager.shutdown();
    }

    @Test
    void testDailyTaskRunImmediately() throws InterruptedException {
        AtomicInteger count = new AtomicInteger();
        // 设置runImmediately = true
        manager.startDailyTask("dailyTask", count::incrementAndGet, 23, 59, true);

        Thread.sleep(500); // 等待立即执行一次
        assertEquals(1, count.get(), "任务应立即执行一次");

        // 暂停任务
//        assertTrue(manager.cancelTask("dailyTask"));
        System.out.println(manager.isTaskRunning("dailyTask"));
    }

    @Test
    void testHourlyTask() throws InterruptedException {
        AtomicInteger count = new AtomicInteger();
        // 每1小时执行一次，runImmediately=false
        manager.startHourlyTask("hourlyTask", count::incrementAndGet, 1);

        Thread.sleep(200); // 短暂等待，不会立即执行
        assertEquals(0, count.get(), "不立即执行");

//        assertTrue(manager.cancelTask("hourlyTask"));
        System.out.println(manager.isTaskRunning("hourlyTask"));
    }

    @Test
    void testMinuteTaskRunImmediately() throws InterruptedException {
        AtomicInteger count = new AtomicInteger();
        manager.startMinuteTask("minuteTask", count::incrementAndGet, 1, true);

        Thread.sleep(500); // 等待立即执行一次
        assertEquals(1, count.get(), "立即执行一次");

//        assertTrue(manager.cancelTask("minuteTask"));
    }

    @Test
    void testSecondTask() throws InterruptedException {
        AtomicInteger count = new AtomicInteger();
        // 每1秒执行一次
        manager.startSecondTask("secondTask", count::incrementAndGet, 1);

        Thread.sleep(2200); // 等待2次触发
        int val = count.get();
        assertTrue(val >= 2, "任务应至少执行2次, 实际执行次数: " + val);

//        assertTrue(manager.cancelTask("secondTask"));
    }

    @Test
    void testDuplicateTask() {
        Runnable dummy = () -> {};
//        assertTrue(manager.startSecondTask("dupTask", dummy, 1));
//        assertFalse(manager.startSecondTask("dupTask", dummy, 1), "重复任务启动应返回false");

        manager.cancelTask("dupTask");
    }
}
