package com.lpwork.koala;

import com.lpwork.koala.timer.Timer;
import com.lpwork.koala.timer.WheelTimer;
import com.lpwork.koala.timer.impl.RecoverableWheelTimer;
import com.lpwork.koala.util.IdWorker;

import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @author luopeng
 * created at 2017/11/17
 */
public class MainTest {

    private static ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();

    public static void main(String[] args) {

        final WheelTimer wt = new RecoverableWheelTimer("/Users/luopeng/tmp/leveldb_recoverable", 1L, TimeUnit.SECONDS, new WheelTimer.Callback() {
            public void handle(Timer timer) {
                System.out.println("handle. timerId:" + timer.getTimerId());
            }
        });

        wt.init();
        wt.start();

        scheduledExecutorService.scheduleAtFixedRate(new Runnable() {
            public void run() {
                System.out.println("WheelTimer length: " + wt.length());
            }
        },0,2,TimeUnit.SECONDS);

        IdWorker idWorker = new IdWorker(1);

        Random rand = new Random();

        for(int i=0;i<1000;++i){
            Timer timer = new Timer();
            timer.setTimerId(idWorker.nextIdStr());
            timer.setTimeout(System.currentTimeMillis() + rand.nextInt(1000000));
            wt.newTimeout(timer);
        }

//        Timer timer = new Timer();
//        timer.setTimerId(idWorker.nextIdStr());
//        timer.setTimeout(System.currentTimeMillis() + 10000);
//        wt.newTimeout(timer);
//
//        Timer timer2 = new Timer();
//        timer2.setTimerId(idWorker.nextIdStr());
//        timer2.setTimeout(System.currentTimeMillis() + 100000);
//        wt.newTimeout(timer2);

    }

}
