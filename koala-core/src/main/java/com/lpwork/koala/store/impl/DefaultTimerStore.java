package com.lpwork.koala.store.impl;

import com.lpwork.koala.store.KVStore;
import com.lpwork.koala.store.TimerStore;
import com.lpwork.koala.timer.Timer;
import com.lpwork.koala.util.JSONUtils;

/**
 * 默认timer存储器
 *
 * @author luopeng
 * created at 2017/11/17
 */
public class DefaultTimerStore implements TimerStore {

    private KVStore kvStore;

    public DefaultTimerStore(KVStore kvStore) {
        this.kvStore = kvStore;
    }

    public void init() throws Exception {
        kvStore.init();
    }

    public void open() throws Exception {
        kvStore.open();
    }

    public void close() throws Exception {
        kvStore.close();
    }

    public void save(Timer timer) {
        byte[] key = JSONUtils.toBytes(timer.getTimerId());
        byte[] value = JSONUtils.toBytes(timer);
        kvStore.put(key, value);
    }

    public void saveSafety(Timer timer) {
        byte[] key = JSONUtils.toBytes(timer.getTimerId());
        byte[] value = JSONUtils.toBytes(timer);
        kvStore.putSafety(key, value);
    }

    public Timer query(String timerId) {
        byte[] value = kvStore.get(JSONUtils.toBytes(timerId));
        if (value == null) {
            return null;
        }
        return JSONUtils.toTimer(value);
    }

    public void delete(String timerId) {
        kvStore.delete(JSONUtils.toBytes(timerId));
    }

    public void iteratorOps(final Callback callback) {
        kvStore.iteratorOps(new KVStore.Callback() {
            public boolean handle(byte[] key, byte[] value) {
                if(value == null || value.length == 0){
                    return true;//这个时候就要返回true，返回false会再次插入一个空值，这里就是一个坑，一定要注意
                }
                return callback.handle(JSONUtils.toTimer(value));
            }
        });
    }
}
