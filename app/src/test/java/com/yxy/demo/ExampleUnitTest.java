package com.yxy.demo;

import com.yxy.demo.utils.GenericUtils;
import org.junit.Test;

import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.Assert.assertEquals;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    @Test
    public void addition_isCorrect() {
        assertEquals(4, 2 + 2);
    }

    @Test
    public void test() {
        String fileName = GenericUtils.getFileNameFromURL("http://www.vaidu.com/123/456.cv?dir=/123");
        System.out.println(fileName);
        String fileType = GenericUtils.obtainFileType(fileName);
        System.out.println(fileType);
        System.out.println(GenericUtils.D2S(new Date()));
    }

    @Test
    public void test2() {
        AtomicLong a = new AtomicLong(0);
        a.getAndIncrement();
        System.out.println(a.get());
    }

    @Test
    public void test3() throws InterruptedException {
        Timer timer = new Timer();
        long millis = System.currentTimeMillis();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (System.currentTimeMillis() > (millis + 200)) {
                    System.out.println("取消");
                    timer.cancel();
                }else {
                    System.out.println("23333");
                }
            }
        }, 1000, 1000);
        Thread.sleep(100000);
    }

    @Test
    public void test_4() {
        try {
            System.out.println(1111);
            try {
                System.out.println(22222);
                int i = 1 / 0;
            }catch (Exception e){
                //return;
            }
            finally {
                System.out.println(333333);
            }
            System.out.println(3.5);
        } finally {
            System.out.println(4444);
        }
        System.out.println(55555);
    }

    @Test
    public void test9(){
        Long a=0L;
        int value = Long.valueOf((a / 1024)).intValue();
        System.out.println(value);
    }
}