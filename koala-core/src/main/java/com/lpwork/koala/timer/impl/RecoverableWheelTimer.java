package com.lpwork.koala.timer.impl;

import com.lpwork.koala.store.TimerStore;
import com.lpwork.koala.store.impl.DefaultKVStore;
import com.lpwork.koala.store.impl.DefaultTimerStore;
import com.lpwork.koala.store.impl.LevelDBOp;
import com.lpwork.koala.timer.Timer;
import com.lpwork.koala.timer.WheelTimer;
import io.netty.util.concurrent.DefaultThreadFactory;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 可恢复的时间轮实现
 *
 * @author luopeng
 * created at 2017/11/17
 */
public class RecoverableWheelTimer implements WheelTimer {

    private Logger logger = LoggerFactory.getLogger(RecoverableWheelTimer.class);

    public static final String RUNNING_DB_DIR = "/running";
    public static final String FINISH_DB_DIR = "/finished";

    private WheelTimer wheelTimer;

    private String dbPath;

    private TimerStore storeForRunning;

    private TimerStore storeForFinished;

    private volatile State state = State.NONE;

    private TimeoutCallback timeoutCallback;

    private Callback finishedCallback;

    private ScheduledExecutorService masterWorker = Executors.newSingleThreadScheduledExecutor(new DefaultThreadFactory("masterWorker"));

    private enum State {
        NONE,
        INIT,
        RUNNING,
        CLOSED
    }

    public RecoverableWheelTimer(String dbPath, long tickDuration, TimeUnit unit, Callback callback) {
        if (StringUtils.isBlank(dbPath)) {
            throw new IllegalArgumentException("dbPath should not be blank.");
        }
        this.dbPath = dbPath;
        this.timeoutCallback = new TimeoutCallback();
        this.finishedCallback = callback;
        this.wheelTimer = new DefaultWheelTimer(tickDuration, unit, this.timeoutCallback);
    }

    public void init() {
        this.storeForRunning = new DefaultTimerStore(new DefaultKVStore(new LevelDBOp(toPath(this.dbPath, RUNNING_DB_DIR))));
        this.storeForFinished = new DefaultTimerStore(new DefaultKVStore(new LevelDBOp(toPath(this.dbPath, FINISH_DB_DIR))));
        this.state = State.INIT;
    }

    private String toPath(String parent, String child) {
        if (!StringUtils.endsWith(parent, "/") && !StringUtils.endsWith(parent, "\\")) {
            parent += "/";
        }
        return parent + child;
    }

    public void start() {
        if (this.state != State.INIT) {
            throw new IllegalStateException("Not in INIT state. please init first.");
        }
        try {
            this.storeForRunning.open();
            this.storeForFinished.open();
        } catch (Exception e) {
            throw new RuntimeException("open failed.", e);
        }

        this.wheelTimer.start();
        //恢复timer
        recover();

        //开始后台finish相关的线程
        startWorkerBackground();

        //添加关闭钩子
        addShutdownHook();

        this.state = State.RUNNING;
    }

    /**
     * 关闭钩子
     */
    private void addShutdownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                shutdown();
            }
        });
    }

    /**
     * 开启后台工作线程
     */
    private void startWorkerBackground() {
        masterWorker.scheduleWithFixedDelay(new Runnable() {
            public void run() {
                RecoverableWheelTimer.this.storeForFinished.iteratorOps(new TimerStore.Callback() {
                    public boolean handle(Timer timer) {
                        //回调通知类服务
                        RecoverableWheelTimer.this.finishedCallback.handle(timer);
                        //移除通知回调完成的timer
                        RecoverableWheelTimer.this.storeForFinished.delete(timer.getTimerId());
                        return true;
                    }
                });
            }
        }, 0, 10, TimeUnit.MILLISECONDS);
    }

    private void recover() {

        logger.info("start recovering from local database files...");
        //恢复步骤
        //1. 从storeForFinish中迭代查询，并将查询到的timer从storeForRunning中删除
        storeForFinished.iteratorOps(new TimerStore.Callback() {
            public boolean handle(Timer timer) {
                storeForRunning.delete(timer.getTimerId());
                return true;
            }
        });

        //2. 遍历storeForRunning，将其中符合要求的timer重新加入时间轮，不符合要求的根据情况改变状态并投入storeForFinish中
        storeForRunning.iteratorOps(new TimerStore.Callback() {
            public boolean handle(Timer timer) {
                if (timer.getTimerState() == Timer.STATE_CANCEL) {
                    transferToFinish(timer);
                    logger.info("timer canceled:" + timer);
                }

                if (timer.getTimerState() == Timer.STATE_RUNNING) {
                    wheelTimer.newTimeout(timer);
                    logger.info("timer recovered:" + timer);
                }

                if (timer.getTimerState() == Timer.STATE_FINISH) {
                    RecoverableWheelTimer.this.timeoutCallback.handle(timer);
                    logger.info("timer finished:" + timer);
                }

                return true;
            }
        });
    }

    private void transferToFinish(Timer timer) {
        logger.debug("timer finished:{}", timer);
        storeForFinished.save(timer);
        storeForRunning.delete(timer.getTimerId());
    }

    public void shutdown() {
        logger.info("begin to shutdown...");

        masterWorker.shutdown();

        wheelTimer.shutdown();

        try {
            this.storeForRunning.close();
            this.storeForRunning.close();
        } catch (Exception e) {
            throw new RuntimeException("close failed.", e);
        }

        this.state = State.CLOSED;
        logger.info("shutdown finished.");
    }

    public void newTimeout(Timer timer) {
        if (this.state != State.RUNNING) {
            throw new IllegalStateException("Not in RUNNING state.");
        }

        //存储timer
        timer.setTimerState(Timer.STATE_RUNNING);

        if (Timer.LEVEL_IMPORTANT == timer.getLevel()) {
            storeForRunning.saveSafety(timer);
        } else {
            storeForRunning.save(timer);
        }

        wheelTimer.newTimeout(timer);
    }

    public boolean isRunning(String timerId) {
        if (this.state != State.RUNNING) {
            throw new IllegalStateException("Not in RUNNING state.");
        }
        return storeForRunning.query(timerId) != null;
    }

    public int length() {
        return wheelTimer.length();
    }

    public void cancel(String timerId) {
        if (this.state != State.RUNNING) {
            return;
        }
        wheelTimer.cancel(timerId);
        Timer timer = storeForRunning.query(timerId);
        timer.setTimerState(Timer.STATE_CANCEL);
        transferToFinish(timer);
        logger.info("timer canceled:{}", timer);
    }

    private class TimeoutCallback implements Callback {

        public void handle(Timer timer) {
            timer.setTimerState(Timer.STATE_FINISH);
            //容错处理
            //先改变RUNNING存储中timer的状态，再插入FINISH存储timer，最后删除RUNNING存储中的timer
            RecoverableWheelTimer.this.storeForRunning.save(timer);
            transferToFinish(timer);
        }
    }
}
