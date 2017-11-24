package com.lpwork.koala.store.impl;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.iq80.leveldb.DB;
import org.iq80.leveldb.DBFactory;
import org.iq80.leveldb.DBIterator;
import org.iq80.leveldb.Options;
import org.iq80.leveldb.ReadOptions;
import org.iq80.leveldb.WriteOptions;
import org.iq80.leveldb.impl.Iq80DBFactory;

import java.io.File;
import java.io.IOException;
import java.util.Map;

public class LevelDBOp {

    private DBFactory factory = Iq80DBFactory.factory;

    private String dbPath;

    private boolean deleteIfExist;

    private DB db;

    public LevelDBOp(String dbPath) {
        this(dbPath, false);
    }

    public LevelDBOp(String dbPath, boolean deleteIfExist) {
        if (StringUtils.isBlank(dbPath)) {
            throw new IllegalArgumentException("dbPath should not be blank.");
        }
        this.dbPath = dbPath;
        this.deleteIfExist = deleteIfExist;
    }

    /**
     * @throws IOException
     */
    public void open() throws IOException {
        //init
        File dir = new File(this.dbPath);
        //如果数据不需要reload，则每次重启，尝试清理磁盘中path下的旧数据。
        if (this.deleteIfExist) {
            truncateDb();
        }
        Options options = new Options().createIfMissing(true);
        //重新open新的db
        db = factory.open(dir, options);
    }

    /**
     * 清除数据库
     *
     * @throws IOException
     */
    public void truncateDb() throws IOException {
        File dir = new File(this.dbPath);
        factory.destroy(dir, null);//清除文件夹内的所有文件。
    }

    /**
     * 关闭数据库
     *
     * @throws IOException
     */
    public void close() throws IOException {
        db.close();
    }

    /**
     * 设置数据
     *
     * @param key
     * @param value
     */
    public void put(byte[] key, byte[] value) {
        db.put(key, value);
    }

    /**
     * 设置数据（安全），安全但速度较慢
     *
     * @param key
     * @param value
     */
    public void putSafety(byte[] key, byte[] value) {
        WriteOptions writeOptions = new WriteOptions().sync(true);//线程安全
        db.put(key, value, writeOptions);
    }

    /**
     * 获取数据
     *
     * @param key
     * @return
     */
    public byte[] get(byte[] key) {
        return db.get(key);
    }

    /**
     * 删除key对应的数据
     *
     * @param key
     */
    public void delete(byte[] key) {
        db.delete(key);
    }

    /**
     * 迭代操作
     *
     * @param callback 对每一条数据的回调操作
     */
    public void iteratorOps(Callback callback) {
        ReadOptions readOptions = new ReadOptions();
        readOptions.fillCache(false);//遍历中swap出来的数据，不应该保存在memory table中。
        DBIterator iterator = null;
        try {
            iterator = db.iterator(readOptions);
            while (iterator.hasNext()) {
                Map.Entry<byte[], byte[]> item = iterator.next();
                if (!callback.handle(item.getKey(), item.getValue())) {
                    break;
                }
            }
        } finally {
            IOUtils.closeQuietly(iterator);
        }
    }

    public interface Callback {
        /**
         * @param key
         * @param value
         * @return 是否继续
         */
        boolean handle(byte[] key, byte[] value);
    }
}
