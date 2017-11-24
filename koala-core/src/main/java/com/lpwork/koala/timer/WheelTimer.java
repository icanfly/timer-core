package com.lpwork.koala.timer;

/**
 * 时间轮接口
 * @author luopeng
 * created at 2017/11/17
 */
public interface WheelTimer {

    void init();

    void start();

    void shutdown();

    void newTimeout(Timer timer);

    boolean isRunning(String timerId);

    int length();

    void cancel(String timerId);

    interface Callback {
        void handle(Timer timer);
    }
}
