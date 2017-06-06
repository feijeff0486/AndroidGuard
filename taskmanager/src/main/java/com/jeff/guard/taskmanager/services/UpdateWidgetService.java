package com.jeff.guard.taskmanager.services;

import android.app.PendingIntent;
import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.text.format.Formatter;
import android.util.Log;
import android.widget.RemoteViews;

import com.jeff.guard.taskmanager.R;
import com.jeff.guard.taskmanager.TaskCleanWidget;
import com.jeff.guard.taskmanager.utils.SystemInfoUtil;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by 小太阳jeff on 2017/5/8.
 */

public class UpdateWidgetService extends Service {
    private static final String TAG = "UpdateWidgetService";
    private Timer timer;
    private TimerTask timerTask;
    private AppWidgetManager awm;
    private ScreenOffReceiver offReceiver;
    private ScreenOnReceiver onReceiver;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        onReceiver = new ScreenOnReceiver();
        offReceiver = new ScreenOffReceiver();
        registerReceiver(onReceiver, new IntentFilter(Intent.ACTION_SCREEN_ON));
        registerReceiver(offReceiver, new IntentFilter(Intent.ACTION_SCREEN_OFF));
        awm = AppWidgetManager.getInstance(this);
        StartTimer();

        super.onCreate();
    }

    private void StartTimer() {

        if (timer == null && timerTask == null) {
            timer = new Timer();
            timerTask = new TimerTask() {
                @Override
                public void run() {
                    Log.d(TAG, "run: 更新Widget");
                    //设置更新组件
                    ComponentName componentName = new ComponentName(UpdateWidgetService.this, TaskCleanWidget.class);
                    //获取到桌面widget的组件
                    RemoteViews views = new RemoteViews(getPackageName(), R.layout.widget_layout);
                    views.setTextViewText(R.id.tv_process_count, "正在运行的进程：" + SystemInfoUtil.getRunningAppCount(getApplicationContext()) + "个");
                    long size = SystemInfoUtil.getAvailMem(getApplicationContext());
                    views.setTextViewText(R.id.tv_process_memory, "可用内存：" + Formatter.formatFileSize(getApplicationContext(), size));
                    //描述一个动作由另一个应用程序来执行
                    //自定义一个广播事件，杀死后台进度的事件
                    Intent intent = new Intent();
                    intent.setAction("com.jeff.guard.taskmanager.killall");//自定义的指令,在清单文件中应保持一致

                    PendingIntent pendingIntent = PendingIntent.getBroadcast(getApplicationContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);//FLAG_UPDATE_CURRENT表示第二次点击覆盖第一次
                    views.setOnClickPendingIntent(R.id.bt_clean, pendingIntent);
                    awm.updateAppWidget(componentName, views);
                }
            };
            timer.schedule(timerTask, 0, 3000);
        }

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(onReceiver);
        unregisterReceiver(offReceiver);
        offReceiver = null;
        onReceiver = null;
        stopTimer();

    }

    private void stopTimer() {
        if (timer != null && timerTask != null) {
            timer.cancel();
            timerTask.cancel();
            timer = null;
            timerTask = null;
        }
    }

    private class ScreenOffReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "onReceive: ScreenOff");
            stopTimer();
        }
    }

    private class ScreenOnReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "onReceive: ScreenOn");
            StartTimer();
        }
    }
}
