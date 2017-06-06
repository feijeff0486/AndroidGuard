package com.jeff.guard.taskmanager.services;

import android.app.ActivityManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import com.jaredrummler.android.processes.ProcessManager;
import com.jaredrummler.android.processes.models.AndroidAppProcess;
import com.jeff.guard.taskmanager.utils.SystemInfoUtil;

import java.util.List;

/**
 * Created by 小太阳jeff on 2017/5/7.
 */

public class AutoCleanService extends Service {

    private ScreenOffReceiver receiver;
    private static final String TAG = "AutoCleanService";
    private ActivityManager am;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        am = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        receiver = new ScreenOffReceiver();
        registerReceiver(receiver, new IntentFilter(Intent.ACTION_SCREEN_OFF));
        super.onCreate();
    }

    @Override
    public void onDestroy() {
        unregisterReceiver(receiver);
        receiver = null;
        super.onDestroy();
    }

    private class ScreenOffReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "onReceive: ScreenOff");
            SystemInfoUtil.killBackProcess(am);

        }
    }

//    private void killBackProcess() {
//        int sysVersion = (Build.VERSION.SDK_INT);
//        if (sysVersion >= 21) {
//            List<AndroidAppProcess> processInfos = ProcessManager.getRunningAppProcesses();
//            for (AndroidAppProcess processInfo : processInfos) {
//                am.killBackgroundProcesses(processInfo.getPackageName());
//                Log.d(TAG, "onReceive: processInfo.getPackageName= " + processInfo.getPackageName());
//            }
//
//        } else {
//            List<ActivityManager.RunningAppProcessInfo> infos = am.getRunningAppProcesses();
//            for (ActivityManager.RunningAppProcessInfo info : infos) {
//                am.killBackgroundProcesses(info.processName);
//            }
//        }
//    }
}
