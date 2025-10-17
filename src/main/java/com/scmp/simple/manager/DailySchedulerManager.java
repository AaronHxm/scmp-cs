package com.scmp.simple.manager;

import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.*;

/**
 * 通用每日调度管理器
 * 不依赖 JavaFX，可用于任意 Java 应用（含 Spring Boot）
 */
@Slf4j
public class DailySchedulerManager {

    private final ScheduledExecutorService scheduler;
    private final Map<String, ScheduledFuture<?>> taskFutures = new ConcurrentHashMap<>();

    public DailySchedulerManager() {
        this.scheduler = Executors.newScheduledThreadPool(5);
        log.info("DailySchedulerManager 初始化完成");
    }

    /**
     * 启动每日任务
     * @param taskId 任务ID
     * @param task 执行逻辑
     * @param hour 执行小时 (0-23)
     * @param minute 执行分钟 (0-59)
     * @param runImmediatelyIfFuture 如果目标时间在未来，是否立即执行一次
     */
    public synchronized boolean startDailyTask(String taskId, Runnable task, int hour, int minute, boolean runImmediatelyIfFuture) {
        if (taskFutures.containsKey(taskId)) {
            log.warn("任务 {} 已存在", taskId);
            return false;
        }

        long initialDelay = calculateInitialDelay(hour, minute);
        log.debug("任务 [{}] 首次延迟: {} ms", taskId, initialDelay);

        // 如果未来时间并要求立即执行一次
        if (runImmediatelyIfFuture && initialDelay > 0) {
            log.info("任务 [{}] 在未来时间执行，立即运行一次", taskId);
            scheduler.submit(task);
        }

        ScheduledFuture<?> future = scheduler.scheduleAtFixedRate(() -> {
            try {
                log.info("开始执行每日任务 [{}]，时间：{}", taskId, LocalDateTime.now());
                task.run();
            } catch (Exception e) {
                log.error("每日任务执行失败 [{}]", taskId, e);
            }
        }, initialDelay, TimeUnit.DAYS.toMillis(1), TimeUnit.MILLISECONDS);

        taskFutures.put(taskId, future);
        log.info("任务 [{}] 启动成功，计划时间：{}:{:02d}", taskId, hour, minute);
        return true;
    }

    /** 默认不立即执行版本 */
    public boolean startDailyTask(String taskId, Runnable task, int hour, int minute) {
        return startDailyTask(taskId, task, hour, minute, false);
    }

    /**
     * 暂停任务
     */
    public synchronized boolean pauseTask(String taskId) {
        ScheduledFuture<?> future = taskFutures.get(taskId);
        if (future != null) {
            boolean cancelled = future.cancel(false);
            if (cancelled) {
                taskFutures.remove(taskId);
                log.info("任务 [{}] 已暂停", taskId);
                return true;
            }
        }
        log.warn("任务 [{}] 不存在或无法暂停", taskId);
        return false;
    }

    /**
     * 停止所有任务
     */
    public synchronized void stopAll() {
        log.info("正在停止所有任务...");
        for (Map.Entry<String, ScheduledFuture<?>> entry : taskFutures.entrySet()) {
            entry.getValue().cancel(false);
            log.info("任务 [{}] 已停止", entry.getKey());
        }
        taskFutures.clear();
    }

    /**
     * 关闭管理器
     */
    public void shutdown() {
        stopAll();
        scheduler.shutdownNow();
        log.info("DailySchedulerManager 已关闭");
    }

    /**
     * 检查任务是否在运行
     */
    public boolean isTaskRunning(String taskId) {
        ScheduledFuture<?> f = taskFutures.get(taskId);
        return f != null && !f.isCancelled() && !f.isDone();
    }

    /**
     * 计算首次延迟
     */
    private long calculateInitialDelay(int hour, int minute) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime target = now.withHour(hour).withMinute(minute).withSecond(0).withNano(0);
        if (now.isAfter(target)) {
            target = target.plusDays(1);
        }
        return java.time.Duration.between(now, target).toMillis();
    }
}