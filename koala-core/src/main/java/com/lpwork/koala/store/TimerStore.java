package com.lpwork.koala.store;

import com.lpwork.koala.timer.Timer;

/**
 * 定时器存储
 *
 * @author luopeng
 * created at 2017/11/17
 */
public interface TimerStore {

    void init() throws Exception;

    void open() throws Exception;

    void close() throws Exception;

    void save(Timer timer);

    void saveSafety(Timer timer);

    Timer query(String timerId);

    void delete(String timerId);

    void iteratorOps(Callback callback);

    interface Callback {
        /**
         *
         * @param timer
         * @return 是否继续
         */
        boolean handle(Timer timer);
    }

}
