package com.jeff.guard.taskmanager.utils;

import android.app.ActivityManager;
import android.content.Context;
import android.os.Build;
import android.util.Log;

import com.jaredrummler.android.processes.ProcessManager;
import com.jaredrummler.android.processes.models.AndroidAppProcess;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.List;


/**
 * 系统信息工具类
 * Created by 小太阳jeff on 2017/5/7.
 */

public class SystemInfoUtil {
    /**
     * 根据API选择适合的方法
     * @param context
     * @return
     */
    public static int getRunningAppCount(Context context){
        int sysVersion = (Build.VERSION.SDK_INT);
        if (sysVersion>=21){
            return getRunningProcess();
        }else {
            return getRunningProcessCount(context);
        }
    }

    /**
     * 清理进程
     * @param am
     */
    public static void killBackProcess(ActivityManager am) {
        int sysVersion = (Build.VERSION.SDK_INT);
        if (sysVersion >= 21) {
            List<AndroidAppProcess> processInfos = ProcessManager.getRunningAppProcesses();
            for (AndroidAppProcess processInfo : processInfos) {
                am.killBackgroundProcesses(processInfo.getPackageName());
                Log.d("killBackProcess ","");
            }

        } else {
            List<ActivityManager.RunningAppProcessInfo> infos = am.getRunningAppProcesses();
            for (ActivityManager.RunningAppProcessInfo info : infos) {
                am.killBackgroundProcesses(info.processName);
            }
        }
    }

    /**
     * 判断一个服务是否处于运行状态
     *
     * @param context 上下文
     * @return
     */
    public static boolean isServiceRunning(Context context, String className) {
        ActivityManager am = (ActivityManager) context
                .getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningServiceInfo> infos = am.getRunningServices(200);
        for (ActivityManager.RunningServiceInfo info : infos) {
            String serviceClassName = info.service.getClassName();
            if (className.equals(serviceClassName)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 返回进程的总个数
     *
     * @param context
     * @return
     */
    private static int getRunningProcessCount(Context context) {
        // 得到进程管理者
        ActivityManager activityManager = (ActivityManager) context
                .getSystemService(context.ACTIVITY_SERVICE);
        // 获取到当前手机上面所有运行的进程
        List<ActivityManager.RunningAppProcessInfo> runningAppProcesses = activityManager
                .getRunningAppProcesses();

        // 获取手机上面一共有多少个进程
        return runningAppProcesses.size();
    }

    /**
     * 21+
     * @return
     */
    private static int getRunningProcess(){
        List<AndroidAppProcess> processInfos = ProcessManager.getRunningAppProcesses();
        return processInfos.size();
    }

    /**
     * 返回剩余可用内存
     * @param context
     * @return
     */
    public static long getAvailMem(Context context) {
        // 得到进程管理者
        ActivityManager activityManager = (ActivityManager) context
                .getSystemService(context.ACTIVITY_SERVICE);
        ActivityManager.MemoryInfo memoryInfo = new ActivityManager.MemoryInfo();

        // 获取到内存的基本信息
        activityManager.getMemoryInfo(memoryInfo);
        // 获取到剩余内存,同样可以获取总内存memoryInfo.totalMem
        return memoryInfo.availMem;
    }

    /**
     * 获取到总内存
     * @param context
     * @return
     */
    public static long getTotalMem(Context context) {
		/*
		 * 这个地方不能直接跑到低版本的手机上面 MemTotal: 344740 kB "/proc/meminfo"
		 */
        try {
            // /proc/meminfo 配置文件的路径
            FileInputStream fis = new FileInputStream(new File("/proc/meminfo"));

            BufferedReader reader = new BufferedReader(new InputStreamReader(
                    fis));

            String readLine = reader.readLine();

            StringBuffer sb = new StringBuffer();
            //遍历字符串取出表示内存的数字信息
            for (char c : readLine.toCharArray()) {
                if (c >= '0' && c <= '9') {
                    sb.append(c);
                }
            }
            return Long.parseLong(sb.toString()) * 1024;
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return 0;

    }
}
