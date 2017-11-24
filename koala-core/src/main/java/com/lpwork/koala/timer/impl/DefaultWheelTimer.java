package com.lpwork.koala.timer.impl;

import com.google.common.collect.Maps;
import com.lpwork.koala.timer.Timer;
import com.lpwork.koala.timer.WheelTimer;
import io.netty.util.HashedWheelTimer;
import io.netty.util.Timeout;
import io.netty.util.concurrent.DefaultThreadFactory;

import java.util.Map;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

/**
 * 默认时间轮
 *
 * @author luopeng
 * created at 2017/11/17
 */
public class DefaultWheelTimer implements WheelTimer {

    private HashedWheelTimer hashedWheelTimer;

    private static final ThreadFactory threadFactory = new DefaultThreadFactory("timeWheel");

    private Map<String, Timeout> timerTaskMap = Maps.newConcurrentMap();

    private Callback callback;

    public DefaultWheelTimer(long tickDuration, TimeUnit unit, Callback callback) {
        this.hashedWheelTimer = new HashedWheelTimer(threadFactory, tickDuration, unit);
        this.callback = callback;
    }

    public void init() {
        //do nothing for current implement
    }

    public void start() {
        hashedWheelTimer.start();
    }

    public void shutdown() {
        hashedWheelTimer.stop();
    }

    public void newTimeout(final Timer timer) {

        long duration = timer.getTimeout() - System.currentTimeMillis();

        Timeout timeout = this.hashedWheelTimer.newTimeout(new io.netty.util.TimerTask() {
            public void run(Timeout timeout) throws Exception {
                timerTaskMap.remove(timer.getTimerId());
                DefaultWheelTimer.this.callback.handle(timer);
            }
        }, duration, TimeUnit.MILLISECONDS);

        timerTaskMap.put(timer.getTimerId(), timeout);

    }

    public boolean isRunning(String timerId) {
        Timeout tt = timerTaskMap.get(timerId);
        if (tt == null) {
            return false;
        }
        return !tt.isCancelled();
    }

    public int length() {
        return timerTaskMap.size();
    }

    public void cancel(String timerId) {
        Timeout tt = timerTaskMap.get(timerId);
        if (tt != null) {
            tt.cancel();
        }
        timerTaskMap.remove(timerId);
    }
}
