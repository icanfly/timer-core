package com.lpwork.koala.util;

import com.alibaba.fastjson.JSON;
import com.lpwork.koala.timer.Timer;

/**
 * JSON工具类，处理JSON相关的转换
 *
 * @author luopeng
 * created at 2017/11/17
 */
public class JSONUtils {

    public static byte[] toBytes(Object object) {
        return JSON.toJSONBytes(object);
    }

    public static Timer toTimer(byte[] timerBytes) {
        return JSON.parseObject(timerBytes, Timer.class);
    }

}
