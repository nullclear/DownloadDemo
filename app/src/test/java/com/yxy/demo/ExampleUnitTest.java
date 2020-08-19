package com.yxy.demo;

import com.yxy.demo.utils.GenericUtils;
import org.junit.Test;

import java.util.Date;

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
}