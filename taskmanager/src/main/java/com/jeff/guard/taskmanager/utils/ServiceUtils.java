package com.jeff.guard.taskmanager.utils;

import android.app.ActivityManager;
import android.content.Context;
import android.util.Log;

import java.util.List;

/**
 * Created by 小太阳jeff on 2017/5/7.
 */

public class ServiceUtils {
    private static final String TAG = "ServiceUtils";

    public static boolean isServiceRunning(Context context, String packageName) {
        boolean isrunning= false;
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningServiceInfo> infos = am.getRunningServices(50);
        for (ActivityManager.RunningServiceInfo info : infos) {
            String name = info.process;
            Log.d(TAG, "isServiceRunning: name= "+name);
            if (name.equals(packageName)){
                isrunning=true;
            }
        }
        return isrunning;
    }
}
