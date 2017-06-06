package com.jeff.guard.taskmanager.engine;

import android.annotation.TargetApi;
import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Debug;
import android.util.Log;
//import com.jaredrummler.android.processes.ProcessManager;
//import com.jaredrummler.android.processes.models.AndroidAppProcess;

import com.jaredrummler.android.processes.ProcessManager;
import com.jaredrummler.android.processes.models.AndroidAppProcess;
import com.jeff.guard.taskmanager.R;
import com.jeff.guard.taskmanager.domain.TaskInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * 提供手机的进程信息
 * Created by 小太阳jeff on 2017/5/7.
 */

public class TaskInfoProvider {
    private static final String TAG = "TaskInfoProvider";

    public static List<TaskInfo> getRunningTaskInfos(Context context){
        int sysVersion = (Build.VERSION.SDK_INT);
        if (sysVersion>=21){
            return getAllTaskInfos(context);
        }else {
            return getTaskInfos(context);
        }
    }

    /**
     * 获取所有的进程信息
     * 支持Android5.0以下
     * @param context 上下文
     * @return
     */
    private static List<TaskInfo> getTaskInfos(Context context) {

        PackageManager packageManager = context.getPackageManager();

        List<TaskInfo> TaskInfos = new ArrayList<>();

        // 获取到进程管理器
        ActivityManager activityManager = (ActivityManager) context
                .getSystemService(context.ACTIVITY_SERVICE);
        // 获取到手机上面所有运行的进程
        List<ActivityManager.RunningAppProcessInfo> appProcesses = activityManager
                .getRunningAppProcesses();

        for (ActivityManager.RunningAppProcessInfo runningAppProcessInfo : appProcesses) {

            TaskInfo taskInfo = new TaskInfo();
            // 获取到进程的包名
            String packageName = runningAppProcessInfo.processName;
            taskInfo.setPackageName(packageName);

            try {
                // 获取到内存基本信息
                /**
                 * 这个里面一共只有一个数据
                 */
                Debug.MemoryInfo[] memoryInfo = activityManager
                        .getProcessMemoryInfo(new int[]{runningAppProcessInfo.pid});

                // (当前应用程序占用多少内存)
                long totalPrivateDirty = memoryInfo[0].getTotalPrivateDirty() * 1024;

                taskInfo.setMemorySize(totalPrivateDirty);

                PackageInfo packageInfo = packageManager.getPackageInfo(
                        packageName, 0);

                // 获取到图片
                Drawable icon = packageInfo.applicationInfo
                        .loadIcon(packageManager);
                taskInfo.setIcon(icon);

                // 获取到应用的名字
                String appName = packageInfo.applicationInfo.loadLabel(
                        packageManager).toString();
                taskInfo.setAppName(appName);

                Log.d(TAG, "-------------------");
                Log.d(TAG, "processName=" + packageName);
                Log.d(TAG, "appName=" + appName);
                //获取到当前应用程序的标记
                //packageInfo.applicationInfo.flags 我们写的答案
                //ApplicationInfo.FLAG_SYSTEM表示老师的该卷器
                int flags = packageInfo.applicationInfo.flags;
                //ApplicationInfo.FLAG_SYSTEM 表示系统应用程序
                if ((flags & ApplicationInfo.FLAG_SYSTEM) != 0) {
                    //系统应用
                    taskInfo.setuserTask(false);
                } else {
                    //用户应用
                    taskInfo.setuserTask(true);
                }

            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                // 系统核心库里面有些系统没有图标。必须给一个默认的图标
                taskInfo.setAppName(packageName);
                taskInfo.setIcon(context.getResources().getDrawable(
                        R.mipmap.ic_launcher));
            }

            TaskInfos.add(taskInfo);
        }

        return TaskInfos;
    }

    @TargetApi(21)
    private static List<TaskInfo> getAllTaskInfos(Context context) {
        // 应用程序管理器
        ActivityManager am = (ActivityManager) context
                .getSystemService(context.ACTIVITY_SERVICE);

        // 应用程序包管理器
        PackageManager pm = context.getPackageManager();

        // 获取正在运行的程序信息, 就是以下粗体的这句代码,获取系统运行的进程     要使用这个方法，需要加载
//         import com.jaredrummler.android.processes.ProcessManager;
//         import com.jaredrummler.android.processes.models.AndroidAppProcess;  这两个包, 这两个包附件可以下载

        List<AndroidAppProcess> processInfos = ProcessManager.getRunningAppProcesses();

        List<TaskInfo> taskinfos = new ArrayList<>();
        // 遍历运行的程序,并且获取其中的信息
        for (AndroidAppProcess processInfo : processInfos) {
            TaskInfo taskinfo = new TaskInfo();
            // 应用程序的包名
            String packname = processInfo.name;
            taskinfo.setPackageName(packname);
            // 湖区应用程序的内存信息
            android.os.Debug.MemoryInfo[] memoryInfos = am
                    .getProcessMemoryInfo(new int[] { processInfo.pid });
            long memsize = memoryInfos[0].getTotalPrivateDirty() * 1024L;
            taskinfo.setMemorySize(memsize);
            try {
                // 获取应用程序信息
                ApplicationInfo applicationInfo = pm.getApplicationInfo(
                        packname, 0);
                Drawable icon = applicationInfo.loadIcon(pm);
                taskinfo.setIcon(icon);
                String name = applicationInfo.loadLabel(pm).toString();
                taskinfo.setAppName(name);
                if ((applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 0) {
                    // 用户进程
                    taskinfo.setuserTask(true);
                } else {
                    // 系统进程
                    taskinfo.setuserTask(false);
                }
            } catch (PackageManager.NameNotFoundException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                // 系统内核进程 没有名称
                taskinfo.setAppName(packname);
                Drawable icon = context.getResources().getDrawable(
                        R.mipmap.ic_launcher);
                taskinfo.setIcon(icon);
            }
            if (taskinfo != null) {
                taskinfos.add(taskinfo);
            }
        }
        return taskinfos;
    }

}
