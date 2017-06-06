package com.jeff.appmanager.watchdog;

import android.app.ActivityManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.jeff.appmanager.SecurityActivity;
import com.jeff.appmanager.utils.AppLockDao;

import java.util.ArrayList;
import java.util.List;

/**
 * 看门狗服务，监视系统程序的运行状态
 * Created by 小太阳jeff on 2017/6/2.
 */

public class WatchDogService extends Service {
    private ActivityManager am;
    private static final String TAG = "WatchDogService";
    private boolean flag;
    private AppLockDao dao;
    private String tempStopProtectPackname;
    private InnerReceiver receiver;
    private ScreenOffReceiver offReceiver;
    private ScreenOnReceiver onReceiver;
    private NotifyPackagesReceiver notifyReceiver;
    private Intent toSecurityintent;
    private List<String> protectPacknames;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private class InnerReceiver extends BroadcastReceiver{

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "onReceive: 接收到临时停止广播");
            tempStopProtectPackname = intent.getStringExtra("packname");

        }
    }

    private class NotifyPackagesReceiver extends BroadcastReceiver{

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "onReceive: NotifyPackagesReceiver");
            protectPacknames=dao.findAll();
        }
    }

    private class ScreenOffReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "onReceive: ScreenOff");
            //保证再次亮屏需要再次输入密码
            tempStopProtectPackname=null;
        }
    }

    @Override
    public void onCreate() {
        receiver=new InnerReceiver();
        registerReceiver(receiver,new IntentFilter("com.jeff.appmanager.tempstop"));
        offReceiver = new ScreenOffReceiver();
        registerReceiver(offReceiver, new IntentFilter(Intent.ACTION_SCREEN_OFF));
        onReceiver=new ScreenOnReceiver();
        registerReceiver(onReceiver,new IntentFilter(Intent.ACTION_SCREEN_ON));
        notifyReceiver=new NotifyPackagesReceiver();
        registerReceiver(notifyReceiver,new IntentFilter("com.jeff.appmanager.notifpackages"));

        flag = true;
        this.am = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        this.dao = new AppLockDao(this);
        protectPacknames=new ArrayList<>();
        protectPacknames=dao.findAll();

        //当前应用需要保护，弹出输入保护密码的界面
        toSecurityintent = new Intent(getApplicationContext(), SecurityActivity.class);
        //服务没有任务栈信息，在服务中开启activity，需指定这个activity运行的任务栈
        toSecurityintent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        new Thread() {
            @Override
            public void run() {
                while (flag) {//死循环
                    List<ActivityManager.RunningTaskInfo> infos = am.getRunningTasks(1);
                    String packName = infos.get(0).topActivity.getPackageName();
//                    Log.d(TAG, "onCreate: topActivity= " + packName);
//                    if (dao.find(packName)) {
                    //从内存中匹配,查询速度优化
                    if (protectPacknames.contains(packName)) {
                        //判断这个应用程序是否需要临时停止拦截
                        if (packName.equals(tempStopProtectPackname)){

                        }else {
                            //设置要保护程序的包名
                            toSecurityintent.putExtra("packname",packName);
                            startActivity(toSecurityintent);
                        }

                    }
                    try {
                        Thread.sleep(30);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }.start();
        super.onCreate();
    }

    @Override
    public void onDestroy() {
        flag = false;
        unregisterReceiver(receiver);
        unregisterReceiver(offReceiver);
        unregisterReceiver(onReceiver);
        unregisterReceiver(notifyReceiver);
        receiver=null;
        offReceiver=null;
        onReceiver=null;
        notifyReceiver=null;
        super.onDestroy();
    }

    private class ScreenOnReceiver extends BroadcastReceiver{
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "onReceive: 屏幕亮了");
        }
    }
}
