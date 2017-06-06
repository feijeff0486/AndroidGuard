package com.jeff.guard.taskmanager.activity;

import android.app.Activity;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.widget.CheckBox;
import android.widget.CompoundButton;

import com.jeff.guard.taskmanager.R;
import com.jeff.guard.taskmanager.services.AutoCleanService;
import com.jeff.guard.taskmanager.utils.ServiceUtils;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by 小太阳jeff on 2017/5/7.
 */

public class TaskSettingActivity extends Activity implements CompoundButton.OnCheckedChangeListener {

    private CheckBox cbShowSystem;
    private CheckBox cbClearOnTime;

    private SharedPreferences sp;
    private SharedPreferences.Editor editor;
    private Timer timer;
    private TimerTask timerTask;

    private static final String TAG = "TaskSettingActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_setting);
        initView();
        initData();
    }

    private void initData() {
        //获取SharedPreferences编辑器
        sp = getSharedPreferences("config", MODE_PRIVATE);
        editor = sp.edit();

        if (sp.getBoolean("showSystem", true)) {
            cbShowSystem.setChecked(true);
        } else {
            cbShowSystem.setChecked(false);
        }

        if (sp.getBoolean("killOnTime", false)) {
            cbClearOnTime.setChecked(true);
        } else {
            cbClearOnTime.setChecked(false);
        }

        timer = new Timer();
        timerTask = new TimerTask() {
            @Override
            public void run() {
//                Thread t=new Thread(){
//                    @Override
//                    public void run() {
//                    }
//                };
//                runOnUiThread(t);
                Log.d(TAG, "timer_run: *********");
            }

        };
        timer.schedule(timerTask,0,3000);

//        CountDownTimer countDownTimer = new CountDownTimer(3000, 1000) {
//            @Override
//            public void onTick(long millisUntilFinished) {
//                Log.d(TAG, "onTick: millisUntilFinished= " + millisUntilFinished);
//            }
//
//            @Override
//            public void onFinish() {
//                Log.d(TAG, "onFinish");
//            }
//        };
//        countDownTimer.start();
    }

    private void initView() {
        cbShowSystem = (CheckBox) findViewById(R.id.cb_show_system);
        cbClearOnTime = (CheckBox) findViewById(R.id.cb_kill_process_on_time);
        cbShowSystem.setOnCheckedChangeListener(this);
        cbClearOnTime.setOnCheckedChangeListener(this);
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

        switch (buttonView.getId()) {
            case R.id.cb_show_system:
                editor.putBoolean("showSystem", isChecked);
                break;
            case R.id.cb_kill_process_on_time:
                editor.putBoolean("killOnTime", isChecked);
                //锁屏的广播事件在代码中注册才会生效
                Intent intent = new Intent(this, AutoCleanService.class);
                if (isChecked) {
                    startService(intent);
                } else {
                    stopService(intent);
                }
                break;
            default:
                break;
        }
        editor.commit();
    }

    @Override
    protected void onStart() {
        boolean running = ServiceUtils.isServiceRunning(this, "com.jeff.guard.taskmanager");
//        cbClearOnTime.setChecked(running);
        super.onStart();
    }
}
