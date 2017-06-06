package com.jeff.guard.taskmanager.activity;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.text.format.Formatter;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.jeff.guard.taskmanager.R;
import com.jeff.guard.taskmanager.domain.TaskInfo;
import com.jeff.guard.taskmanager.engine.TaskInfoProvider;
import com.jeff.guard.taskmanager.services.AutoCleanService;
import com.jeff.guard.taskmanager.services.UpdateWidgetService;
import com.jeff.guard.taskmanager.utils.SharedPreferencesUtils;
import com.jeff.guard.taskmanager.utils.SystemInfoUtil;

import java.util.ArrayList;
import java.util.List;

public class TaskManagerActivity extends Activity implements View.OnClickListener {

    //    @ViewInject(R.id.lv_tasks)//进程列表
    private ListView mTasksListView;
    //    @ViewInject(R.id.tv_process_count)//显示运行中的进程
    private TextView tvProcessCount;
    //    @ViewInject(R.id.tv_memory_info)//显示当前的内存信息
    private TextView tvMemoryCount;

    //    @ViewInject(R.id.btn_select_all)//全选
    private Button btSelectAll;
    //    @ViewInject(R.id.btn_select_oppsite)//反选
    private Button btSelectOpposite;
    //    @ViewInject(R.id.btn_kill_process)//清理进程
    private Button btKillProcess;
    //    @ViewInject(R.id.btn_open_setting)//打开设置
    private Button btOpenSetting;

    private LinearLayout llLoading;
    private TextView tvCountStatus;//程序种类显示

    private List<TaskInfo> allTaskInfos;//所有进程
    private List<TaskInfo> userTaskInfos;//用户进程
    private List<TaskInfo> systemTaskInfos;//系统进程

    private TaskManagerAdapter adapter;
    private static final String TAG = "TaskManagerActivity";
    private int currentProcessCount;
    private long availMem;
    private long totalMem;
    private int sysVersion;
    private SharedPreferences sp;
    private final int TYPE_SELECT_ALL=100;
    private final int TYPE_SELECT_OPPSITE=101;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_task_manager);
        sysVersion = (Build.VERSION.SDK_INT);
        Log.d(TAG, "onCreate: sysVersion= "+sysVersion);
//        ViewUtils.inject(this);//注入view和事件
        initView();

        boolean autoClean=SharedPreferencesUtils.getBoolean(this,"killOnTime",false);
        Intent intent = new Intent(this, AutoCleanService.class);
        if (autoClean) {
            startService(intent);
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        initData();//初始化数据
    }

    private void initView() {
        mTasksListView = (ListView) findViewById(R.id.lv_tasks);
        tvProcessCount = (TextView) findViewById(R.id.tv_task_process_count);
        tvMemoryCount = (TextView) findViewById(R.id.tv_memory_info);
        btSelectAll = (Button) findViewById(R.id.btn_select_all);
        btSelectOpposite = (Button) findViewById(R.id.btn_select_oppsite);
        btKillProcess = (Button) findViewById(R.id.btn_kill_process);
        btOpenSetting = (Button) findViewById(R.id.btn_open_setting);
        llLoading = (LinearLayout) findViewById(R.id.ll_loading);
        tvCountStatus = (TextView) findViewById(R.id.tv_status);
        btSelectAll.setOnClickListener(this);
        btSelectOpposite.setOnClickListener(this);
        btKillProcess.setOnClickListener(this);
        btOpenSetting.setOnClickListener(this);

        mTasksListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                TaskInfo taskinfo;
                if (position == 0) {//用户进程的标签
                    return;
                } else if (position == (userTaskInfos.size() + 1)) {
                    return;
                } else if (position <= userTaskInfos.size()) {
                    taskinfo = userTaskInfos.get(position - 1);
                } else {
                    taskinfo = systemTaskInfos.get(position - userTaskInfos.size() - 2);
                }
                //过滤掉自身，不会kill自己
                if (taskinfo.getPackageName().equals(getPackageName())) {
                    return;
                }

                ViewHolder holder = (ViewHolder) view.getTag();
                if (taskinfo.isChecked()) {
                    taskinfo.setChecked(false);
                    holder.cb_status.setChecked(false);
                } else {
                    taskinfo.setChecked(true);
                    holder.cb_status.setChecked(true);
                }
            }
        });
    }

    private void initData() {
//        currentProcessCount=sysVersion>21?SystemInfoUtil.getRunningProcess():SystemInfoUtil.getRunningProcessCount(this);

        availMem = SystemInfoUtil.getAvailMem(this) / 1024 / 1024;
        totalMem = SystemInfoUtil.getTotalMem(this) / 1024 / 1024;
        sp = getSharedPreferences("config", MODE_PRIVATE);


        llLoading.setVisibility(View.VISIBLE);

        new Thread() {
            @Override
            public void run() {
//                currentProcessCount = SystemInfoUtil.getRunningAppCount(getApplicationContext());
                allTaskInfos=TaskInfoProvider.getRunningTaskInfos(getApplicationContext());
//                allTaskInfos = sysVersion>21?TaskInfoProvider.getAllTaskInfos(getApplicationContext()):TaskInfoProvider.getTaskInfos(getApplicationContext());
                userTaskInfos = new ArrayList<>();
                systemTaskInfos = new ArrayList<>();
                for (TaskInfo info : allTaskInfos) {
                    if (info.isuserTask()) {
                        userTaskInfos.add(info);
                    } else {
                        systemTaskInfos.add(info);
                    }
                }

                //更新设置界面
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (adapter == null) {
                            adapter = new TaskManagerAdapter();
                            mTasksListView.setAdapter(adapter);
                        } else {
                            adapter.notifyDataSetChanged();
                        }

                        llLoading.setVisibility(View.INVISIBLE);
                        if (sp.getBoolean("showSystem", true)) {
                            currentProcessCount=allTaskInfos.size();
                        } else {
                            currentProcessCount=userTaskInfos.size();
                        }

                        tvProcessCount.setText("运行中的进程：" + currentProcessCount + "个");
                        tvMemoryCount.setText(availMem + "M可用/" + totalMem + "M");

                    }
                });
            }
        }.start();

        mTasksListView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {

            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                if (userTaskInfos != null && systemTaskInfos != null) {
                    if (firstVisibleItem > userTaskInfos.size()) {
                        tvCountStatus.setText("系统程序：" + systemTaskInfos.size() + "个");
                    } else {
                        tvCountStatus.setText("用户程序：" + userTaskInfos.size() + "个");
                    }
                }
            }
        });

    }

    private class TaskManagerAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            if (sp.getBoolean("showSystem", true)) {
                return userTaskInfos.size() + 1 + systemTaskInfos.size() + 1;
            } else {
                return userTaskInfos.size() + 1;
            }

        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            TaskInfo taskinfo;
            if (position == 0) {//用户进程的标签
                TextView tv = new TextView(getApplicationContext());
                tv.setBackgroundColor(Color.GRAY);
                tv.setTextColor(Color.WHITE);
                tv.setTextSize(18);
                tv.setText("用户进程：" + userTaskInfos.size() + "个");
                return tv;
            } else if (position == (userTaskInfos.size() + 1)) {
                TextView tv = new TextView(getApplicationContext());
                tv.setBackgroundColor(Color.GRAY);
                tv.setTextColor(Color.WHITE);
                tv.setTextSize(18);
                tv.setText("系统进程：" + systemTaskInfos.size() + "个");
                return tv;
            } else if (position <= userTaskInfos.size()) {
                taskinfo = userTaskInfos.get(position - 1);
            } else {
                taskinfo = systemTaskInfos.get(position - userTaskInfos.size() - 2);
            }

            View view;
            ViewHolder holder;
            if (convertView != null && convertView instanceof LinearLayout) {
                view = convertView;
                holder = (ViewHolder) view.getTag();
                Log.d(TAG, "getView: 复用缓存 " + position);
            } else {
                view = View.inflate(getApplicationContext(), R.layout.item_task_manager, null);
                holder = new ViewHolder();
                holder.iv_icon = (ImageView) view.findViewById(R.id.iv_task_icon);
                holder.tv_name = (TextView) view.findViewById(R.id.tv_task_name);
                holder.tv_memSize = (TextView) view.findViewById(R.id.tv_task_memory_size);
                holder.cb_status = (CheckBox) view.findViewById(R.id.cb_task_status);
                view.setTag(holder);
                Log.d(TAG, "getView: 创建新的View对象 " + position);
            }

            holder.iv_icon.setImageDrawable(taskinfo.getIcon());
            holder.tv_name.setText(taskinfo.getAppName());
            holder.tv_memSize.setText("内存占用：" + Formatter.formatFileSize(getApplicationContext(), taskinfo.getMemorySize()));
            holder.cb_status.setChecked(taskinfo.isChecked());
            //过滤掉自身，不会kill自己
            if (taskinfo.getPackageName().equals(getPackageName())) {
                holder.cb_status.setVisibility(View.INVISIBLE);
            } else {
                holder.cb_status.setVisibility(View.VISIBLE);
            }
            return view;
        }
    }

    static class ViewHolder {
        ImageView iv_icon;
        TextView tv_name;
        TextView tv_memSize;
        CheckBox cb_status;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_select_all:
                selectAll();
                break;
            case R.id.btn_select_oppsite:
                selectOppsite();
                break;
            case R.id.btn_kill_process:
                killProcess();
                break;
            case R.id.btn_open_setting:
                startActivityForResult(new Intent(TaskManagerActivity.this, TaskSettingActivity.class), 0);
                break;
            default:
                break;
        }
    }

    /**
     * 杀死进程
     * 实际上只是清除allTaskInfos中的信息
     */
    private void killProcess() {
        ActivityManager am = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        int count = 0;
        long savedMem = 0;

        List<TaskInfo> killedTaskInfos = new ArrayList<>();
        for (TaskInfo info : allTaskInfos) {
            if (info.isChecked()) {//杀死勾选的进程
                am.killBackgroundProcesses(info.getPackageName());
                if (info.isuserTask()) {
                    userTaskInfos.remove(info);
                } else {
                    systemTaskInfos.remove(info);
                }
                killedTaskInfos.add(info);
                savedMem += info.getMemorySize();
                count++;
            }
        }
        allTaskInfos.removeAll(killedTaskInfos);
        if (count == 0) {
            Toast.makeText(this, "勾选后才可清理", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "杀死了" + count + "个进程，释放" + Formatter.formatFileSize(this, savedMem) + "内存", Toast.LENGTH_SHORT).show();
        }

        currentProcessCount -= count;
        //虚假的清除内存
//        availMem+=(savedMem/1024/1024);
//        Log.d(TAG, "killProcess: availMem= "+availMem);
        availMem = SystemInfoUtil.getAvailMem(this) / 1024 / 1024;
        tvProcessCount.setText("运行中的进程：" + currentProcessCount + "个");
        tvMemoryCount.setText(availMem + "M可用/" + totalMem + "M");
        adapter.notifyDataSetChanged();
    }

    /**
     * 反选
     */
    private void selectOppsite() {
        if (sp.getBoolean("showSystem", true)){
            for (TaskInfo info : allTaskInfos) {
                setKillSelected(info,TYPE_SELECT_OPPSITE);
//                if (info.getPackageName().equals(getPackageName())) {
//                    continue;
//                }
//                info.setChecked(!info.isChecked());
//                adapter.notifyDataSetChanged();
            }
        }else{
            for (TaskInfo info : userTaskInfos) {
                setKillSelected(info,TYPE_SELECT_OPPSITE);
            }
        }
    }

    /**
     * 全选
     */
    private void selectAll() {
        if (sp.getBoolean("showSystem", true)){
            for (TaskInfo info : allTaskInfos) {
                setKillSelected(info,TYPE_SELECT_ALL);
            }
        }else{
            for (TaskInfo info : userTaskInfos) {
                setKillSelected(info,TYPE_SELECT_ALL);
            }
        }
    }

    /**
     * 设置选择
     * @param info 选择的进程的信息
     * @param type 选择类型（全选/反选）
     */
    private void setKillSelected(TaskInfo info,int type) {
        //过滤掉自身，不会kill自己
        if (info.getPackageName().equals(getPackageName())) {
            return;
        }
        if (type==TYPE_SELECT_ALL){
            info.setChecked(true);
        }else if (type==TYPE_SELECT_OPPSITE){
            info.setChecked(!info.isChecked());
        }
        adapter.notifyDataSetChanged();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        //用于在设置界面选择是否显示系统进程后刷新界面
        adapter.notifyDataSetChanged();
        super.onActivityResult(requestCode, resultCode, data);
    }
}
