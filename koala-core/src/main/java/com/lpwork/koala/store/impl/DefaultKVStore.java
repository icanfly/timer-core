package com.lpwork.koala.store.impl;

import com.lpwork.koala.store.KVStore;

/**
 * 默认的KVStore实现，底层使用LevelDB存储引擎实现
 *
 * @author luopeng
 * created at 2017/11/17
 */
public class DefaultKVStore implements KVStore {

    private LevelDBOp op;

    public DefaultKVStore(LevelDBOp op) {
        this.op = op;
    }

    public void init() throws Exception {
        //do nothing for current implementation
    }

    public void open() throws Exception {
        this.op.open();
    }

    public void close() throws Exception {
        this.op.close();
    }

    public void put(byte[] key, byte[] value) {
        op.put(key, value);
    }

    public void putSafety(byte[] key, byte[] value) {
        op.putSafety(key, value);
    }

    public byte[] get(byte[] key) {
        return op.get(key);
    }

    public void delete(byte[] key) {
        op.delete(key);
    }

    public void iteratorOps(final Callback callback) {
        op.iteratorOps(new LevelDBOp.Callback() {
            public boolean handle(byte[] key, byte[] value) {
                return callback.handle(key, value);
            }
        });
    }
}
