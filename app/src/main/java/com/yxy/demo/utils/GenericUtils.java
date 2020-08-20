package com.yxy.demo.utils;

import android.app.ActivityManager;
import android.content.Context;
import android.util.Log;
import android.view.Gravity;
import android.widget.Toast;
import androidx.annotation.NonNull;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Created by Nuclear on 2020/8/17
 */
@SuppressWarnings("FieldCanBeLocal")
public class GenericUtils {
    private static final String TAG = "###GenericUtils";

    private static Toast toast;
    private static Toast gravityToast;

    public static void show(Context context, String text) {
        show(context, text, Toast.LENGTH_SHORT);
    }

    public static void show(Context context, String text, int duration) {
        if (toast != null) {
            toast.cancel();
        }
        toast = Toast.makeText(context, text, duration);
        toast.show();
    }

    public static void showGravity(Context context, String text) {
        showGravity(context, text, Gravity.CENTER);
    }

    public static void showGravity(Context context, String text, int gravity) {
        if (gravityToast != null) {
            gravityToast.cancel();
        }
        gravityToast = Toast.makeText(context, text, Toast.LENGTH_SHORT);
        gravityToast.setGravity(gravity, 0, 0);
        gravityToast.show();
    }

    //判断服务是否在运行
    public static boolean isServiceRunning(@NonNull Context context, @NonNull Class<?> clazz) {
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningServiceInfo> infos = manager.getRunningServices(30);
        for (ActivityManager.RunningServiceInfo info : infos) {
            if (clazz.getName().equals(info.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    //Date转成String的工具
    public static String D2S(Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA);
        return sdf.format(date);
    }

    //数字转进度
    public static String N2P(long sum, long total) {
        if (sum < 0 || total <= 0) {
            return "ERROR";
        } else if (sum == total) {
            return "100%";
        } else {
            DecimalFormat df = new DecimalFormat("0.00");
            return df.format((sum / (double) total) * 100) + "%";
        }
    }

    //从URL中获取文件名
    public static String getFileNameFromURL(String url) {
        if (url == null) {
            return "";
        } else {
            //反编码Url
            String decodeUrl = null;
            try {
                decodeUrl = URLDecoder.decode(url, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                Log.e(TAG, "UnsupportedEncodingException: 不支持的编码格式", e);
            }
            if (decodeUrl == null) return "";
            int endIndex = decodeUrl.indexOf("?");
            int beginIndex = decodeUrl.lastIndexOf("/", endIndex) + 1;
            return decodeUrl.substring(beginIndex, endIndex);
        }
    }

    //获取文件类型
    public static String obtainFileType(String fileName) {
        int index = fileName.lastIndexOf(".");
        if (index >= 0 && index < fileName.length()) {
            return fileName.substring(index + 1);
        } else {
            return "";
        }
    }
}
