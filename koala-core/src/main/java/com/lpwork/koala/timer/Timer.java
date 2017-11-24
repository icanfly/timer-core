package com.lpwork.koala.timer;

import com.google.common.collect.Maps;

import java.util.Map;

/**
 * @author luopeng
 * created at 2017/11/17
 */
public class Timer {

    public static final int STATE_INIT = 0;
    public static final int STATE_RUNNING = 1;
    public static final int STATE_FINISH = 2;
    public static final int STATE_CANCEL = 3;

    public static final int LEVEL_NORMAL = 0;
    public static final int LEVEL_IMPORTANT = 1;

    /**
     * timer Id
     */
    private String timerId;

    /**
     * 应用名
     */
    private String app;

    /**
     * 超时时间，绝对时间值，单位毫秒，如在2017-11-17 18:00:00超时
     */
    private long timeout;

    /**
     * 重要等级
     */
    private int level = LEVEL_NORMAL;

    /**
     * timer类型
     */
    private int timerType;

    /**
     * timer状态
     */
    private int timerState;

    /**
     * 任务元数据
     */
    private Map<String, String> taskMeta = Maps.newHashMap();

    public boolean isExpired() {
        return System.currentTimeMillis() > timeout;
    }

    public String getTimerId() {
        return timerId;
    }

    public void setTimerId(String timerId) {
        this.timerId = timerId;
    }

    public String getApp() {
        return app;
    }

    public void setApp(String app) {
        this.app = app;
    }

    public long getTimeout() {
        return timeout;
    }

    public void setTimeout(long timeout) {
        this.timeout = timeout;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public int getTimerType() {
        return timerType;
    }

    public void setTimerType(int timerType) {
        this.timerType = timerType;
    }

    public int getTimerState() {
        return timerState;
    }

    public void setTimerState(int timerState) {
        this.timerState = timerState;
    }

    public Map<String, String> getTaskMeta() {
        return taskMeta;
    }

    public void setTaskMeta(Map<String, String> taskMeta) {
        this.taskMeta = taskMeta;
    }

    public void addTaskMeta(String key, String value) {
        this.taskMeta.put(key, value);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Timer{");
        sb.append("timerId='").append(timerId).append('\'');
        sb.append(", app='").append(app).append('\'');
        sb.append(", handle=").append(timeout);
        sb.append(", level=").append(level);
        sb.append(", timerType=").append(timerType);
        sb.append(", timerState=").append(timerState);
        sb.append(", taskMeta=").append(taskMeta);
        sb.append('}');
        return sb.toString();
    }
}
