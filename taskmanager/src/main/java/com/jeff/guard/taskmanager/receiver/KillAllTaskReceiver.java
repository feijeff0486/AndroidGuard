package com.jeff.guard.taskmanager.receiver;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import com.jaredrummler.android.processes.ProcessManager;
import com.jaredrummler.android.processes.models.AndroidAppProcess;
import com.jeff.guard.taskmanager.utils.SystemInfoUtil;

import java.util.List;

/**
 * Created by 小太阳jeff on 2017/5/8.
 */

public class KillAllTaskReceiver extends BroadcastReceiver {
    private static final String TAG = "KillAllTaskReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "onReceive: 自定义广播消息接收到了");
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);

        SystemInfoUtil.killBackProcess(am);
//        int sysVersion = (Build.VERSION.SDK_INT);
//        if (sysVersion>=21){
//            List<AndroidAppProcess> processInfos = ProcessManager.getRunningAppProcesses();
//            for (AndroidAppProcess processInfo : processInfos) {
//                am.killBackgroundProcesses(processInfo.getPackageName());
//                Log.d(TAG, "onReceive: processInfo.getPackageName= "+processInfo.getPackageName());
//            }
//
//        }else {
//            List<ActivityManager.RunningAppProcessInfo> infos = am.getRunningAppProcesses();
//            for (ActivityManager.RunningAppProcessInfo info : infos) {
//                am.killBackgroundProcesses(info.processName);
//            }
//        }
    }
}
