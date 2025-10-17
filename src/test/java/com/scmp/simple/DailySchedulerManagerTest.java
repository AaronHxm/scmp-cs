package com.scmp.simple;

 import com.scmp.simple.manager.DailySchedulerManager;
 import javafx.application.Platform;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
 import java.util.concurrent.atomic.AtomicInteger;

 import static org.junit.jupiter.api.Assertions.*;

public class DailySchedulerManagerTest {

    private static DailySchedulerManager manager;

    @BeforeAll
    static void init() {
        manager = new DailySchedulerManager();
    }

    @AfterAll
    static void cleanup() {
        manager.shutdown();
    }

    @Test
    void testDailyTaskImmediateExecution() throws InterruptedException {
        AtomicInteger count = new AtomicInteger(0);
        manager.startDailyTask("test1", count::incrementAndGet, 23, 59, true);

        Thread.sleep(1000); // 等待立即执行
        assertEquals(1, count.get(), "任务应立即执行一次");
    }

    @Test
    void testPauseTask() {
        Runnable dummy = () -> {};
        manager.startDailyTask("pauseTask", dummy, 10, 0);
        assertTrue(manager.isTaskRunning("pauseTask"));
        assertTrue(manager.pauseTask("pauseTask"));
        assertFalse(manager.isTaskRunning("pauseTask"));
    }

    @Test
    void testDuplicateStart() {
        Runnable dummy = () -> {};
        manager.startDailyTask("dup", dummy, 10, 0);
        assertFalse(manager.startDailyTask("dup", dummy, 10, 0));
    }
}
