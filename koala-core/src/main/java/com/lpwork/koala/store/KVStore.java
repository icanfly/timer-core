package com.lpwork.koala.store;

/**
 * @author luopeng
 * created at 2017/11/17
 */
public interface KVStore {

    void init() throws Exception;

    void open() throws Exception;

    void close() throws Exception;

    void put(byte[] key, byte[] value);

    void putSafety(byte[] key,byte[] value);

    byte[] get(byte[] key);

    void delete(byte[] key);

    void iteratorOps(Callback callback);

    interface Callback {
        /**
         *
         * @param key
         * @param value
         * @return 是否继续
         */
        boolean handle(byte[] key, byte[] value);
    }
}
