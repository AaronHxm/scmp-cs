package com.scmp.simple.manager;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.*;

public class SchedulerManager {
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(4);
    private final Map<String, ScheduledFuture<?>> tasks = new ConcurrentHashMap<>();

    // ========== 每日任务 ==========
    public void startDailyTask(String name, Runnable task, int hour, int minute) {
        startDailyTask(name, task, hour, minute, false);
    }

    public void startDailyTask(String name, Runnable task, int hour, int minute, boolean runImmediatelyIfFuture) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime nextRun = now.withHour(hour).withMinute(minute).withSecond(0).withNano(0);
        if (!now.isBefore(nextRun)) nextRun = nextRun.plusDays(1);

        long initialDelay = Duration.between(now, nextRun).toMillis();
        long period = Duration.ofDays(1).toMillis();

        schedule(name, task, initialDelay, period, runImmediatelyIfFuture);
    }

    // ========== 每小时任务 ==========
    public void startHourlyTask(String name, Runnable task, long hours) {
        startHourlyTask(name, task, hours, false);
    }

    public void startHourlyTask(String name, Runnable task, long hours, boolean runImmediately) {
        schedule(name, task, 0, TimeUnit.HOURS.toMillis(hours), runImmediately);
    }

    // ========== 每分钟任务 ==========
    public void startMinuteTask(String name, Runnable task, long minutes) {
        startMinuteTask(name, task, minutes, false);
    }

    public void startMinuteTask(String name, Runnable task, long minutes, boolean runImmediately) {
        schedule(name, task, 0, TimeUnit.MINUTES.toMillis(minutes), runImmediately);
    }

    // ========== 每秒任务 ==========
    public void startSecondTask(String name, Runnable task, long seconds) {
        startSecondTask(name, task, seconds, false);
    }

    public void startSecondTask(String name, Runnable task, long seconds, boolean runImmediately) {
        schedule(name, task, 0, TimeUnit.SECONDS.toMillis(seconds), runImmediately);
    }

    // ========== 公共方法 ==========
    private void schedule(String name, Runnable task, long initialDelay, long period, boolean runImmediately) {
        cancelTask(name);
        if (runImmediately) task.run();
        ScheduledFuture<?> future = scheduler.scheduleAtFixedRate(task, initialDelay, period, TimeUnit.MILLISECONDS);
        tasks.put(name, future);
    }

    // ========== 取消任务 ==========
    public void cancelTask(String name) {
        Optional.ofNullable(tasks.remove(name)).ifPresent(f -> f.cancel(false));
    }

    // ========== 查询任务状态 ==========
    public boolean isTaskRunning(String name) {
        ScheduledFuture<?> future = tasks.get(name);
        return future != null && !future.isCancelled() && !future.isDone();
    }

    // ========== 关闭调度器 ==========
    public void shutdown() {
        tasks.forEach((name, future) -> future.cancel(false));
        tasks.clear();
        scheduler.shutdownNow();
    }
}
