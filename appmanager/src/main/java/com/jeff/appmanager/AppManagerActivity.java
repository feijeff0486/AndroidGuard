package com.jeff.appmanager;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.StatFs;
import android.text.format.Formatter;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.ScaleAnimation;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.jeff.appmanager.domain.AppInfo;
import com.jeff.appmanager.engine.AppInfoProvider;
import com.jeff.appmanager.utils.AppLockDao;
import com.jeff.appmanager.utils.DensityUtil;
import com.jeff.appmanager.watchdog.WatchDogService;

import java.util.ArrayList;
import java.util.List;

public class AppManagerActivity extends Activity implements View.OnClickListener {

    private static final String TAG = "AppManagerActivity";

    private TextView tvAvaliMem;//内存可用
    private TextView tvAvaliSD;//SD可用
    private ListView lvAppManager;
    private LinearLayout llLoading;
    private TextView tvAppCount;
    private TextView tvCountStatus;//程序种类显示

    private List<AppInfo> allAppInfos;//所有应用程序
    private List<AppInfo> userAppInfos;//用户应用程序
    private List<AppInfo> systemAppInfos;//系统应用程序
    private AppManagerAdapter adapter;


    private PopupWindow popupWindow;//弹出悬浮窗体
    private LinearLayout ll_uninstall;
    private LinearLayout ll_start;
    private LinearLayout ll_share;

    //被点击的条目
    private AppInfo appInfo;
    private final int REFRESH_DATA = 103;

    private AppLockDao appLockDao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_manager);
        appLockDao = new AppLockDao(this);
        startService(new Intent(this, WatchDogService.class));
        initView();

        installShortCut();

    }

    /**
     * 创建应用图标
     */
    private void installShortCut() {
        //发送广播的意图
        Intent intent = new Intent();
        intent.setAction("com.android.launcher.action.INSTALL_SHORTCUT");
        intent.putExtra(Intent.EXTRA_SHORTCUT_NAME, "应用管理");
        intent.putExtra(Intent.EXTRA_SHORTCUT_ICON,
                BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher_round));
        //桌面点击图标意图
        Intent shortCutIntent = new Intent();
        shortCutIntent.setAction("android.intent.action.MAIN");
        shortCutIntent.addCategory("android.intent.category.LAUNCHER");
        shortCutIntent.setClassName(getPackageName(), "com.jeff.appmanager.AppManagerActivity");
        intent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, shortCutIntent);
//通过循环一次添加多个快捷方式，熊猫烧香
//        for (int i = 0; i < 5; i++) {
        sendBroadcast(intent);
//        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        initData();
    }

    private void initView() {
        tvAvaliMem = (TextView) findViewById(R.id.tv_avail_rom);
        tvAvaliSD = (TextView) findViewById(R.id.tv_avail_sd);
        lvAppManager = (ListView) findViewById(R.id.lv_appmanger);
        llLoading = (LinearLayout) findViewById(R.id.ll_loading);
        tvAppCount = (TextView) findViewById(R.id.tv_app_count);
        tvCountStatus = (TextView) findViewById(R.id.tv_status);
    }

    private void initData() {
        long sdSize = getAvailSpace(Environment.getExternalStorageDirectory().getAbsolutePath());
        long romSize = getAvailSpace(Environment.getDataDirectory().getAbsolutePath());
        tvAvaliSD.setText("SD卡可用空间：" + Formatter.formatFileSize(this, sdSize));
        tvAvaliMem.setText("内存可用空间：" + Formatter.formatFileSize(this, romSize));

        fillData();

        lvAppManager.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {

            }

            /**
             * 滚动时调用
             * @param view
             * @param firstVisibleItem 第一个条目
             * @param visibleItemCount
             * @param totalItemCount
             */
            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                dismissPopupWindow();
                if (userAppInfos != null && systemAppInfos != null) {
                    if (firstVisibleItem > userAppInfos.size()) {
                        tvCountStatus.setText("系统程序：" + systemAppInfos.size() + "个");
                    } else {
                        tvCountStatus.setText("用户程序：" + userAppInfos.size() + "个");
                    }
                }
            }
        });

        lvAppManager.setOnItemClickListener(new AdapterView.OnItemClickListener() {


            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                if (position == 0) {
                    return;
                } else if (position == (userAppInfos.size() + 1)) {
                    return;
                } else if (position <= userAppInfos.size()) {//用户程序
                    appInfo = userAppInfos.get(position - 1);
                } else {
                    appInfo = systemAppInfos.get(position - userAppInfos.size() - 2);
                }
                Log.d(TAG, "onItemClick: appPackageName= " + appInfo.getPackageName());

                dismissPopupWindow();
                View contentView = View.inflate(getApplicationContext(), R.layout.popup_app_item, null);

//                TextView contentView=new TextView(getApplicationContext());
//                contentView.setText(appInfo.getPackageName());
//                contentView.setTextColor(Color.BLACK);

                //获取popupWindow的控件
                ll_uninstall = (LinearLayout) contentView.findViewById(R.id.ll_uninstall);
                ll_start = (LinearLayout) contentView.findViewById(R.id.ll_start);
                ll_share = (LinearLayout) contentView.findViewById(R.id.ll_share);
                ll_uninstall.setOnClickListener(AppManagerActivity.this);
                ll_start.setOnClickListener(AppManagerActivity.this);
                ll_share.setOnClickListener(AppManagerActivity.this);

                popupWindow = new PopupWindow(contentView, -2, -2);
                //动画效果的播放要求窗体必须有背景颜色
                //透明也是颜色
                popupWindow.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                int[] location = new int[2];
                view.getLocationInWindow(location);
                int dip = 50;
                int px = DensityUtil.dip2px(getApplicationContext(), dip);
                Log.d(TAG, "onItemClick: px= " + px);
                //在代码里设置的宽高值单位为像素，会出现屏幕不适配的问题因转化为dp
                popupWindow.showAtLocation(parent, Gravity.LEFT | Gravity.TOP, px, location[1]);

                //设置动画
                ScaleAnimation sa = new ScaleAnimation(0.3f, 1.0f, 0.3f, 1.0f, Animation.RELATIVE_TO_SELF,
                        0, Animation.RELATIVE_TO_SELF, 0.5f);
                sa.setDuration(300);
                AlphaAnimation aa = new AlphaAnimation(0.5f, 1.0f);
                aa.setDuration(300);
                AnimationSet set = new AnimationSet(false);
                set.addAnimation(sa);
                set.addAnimation(aa);
                contentView.startAnimation(set);

            }
        });

        //程序锁长点击事件
        lvAppManager.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0) {
                    return true;
                } else if (position == (userAppInfos.size() + 1)) {
                    return true;
                } else if (position <= userAppInfos.size()) {//用户程序
                    appInfo = userAppInfos.get(position - 1);
                } else {//系统程序
                    appInfo = systemAppInfos.get(position - userAppInfos.size() - 2);
                }
                //判断条目是否在数据库中
                ViewHolder holder= (ViewHolder) view.getTag();
                if (appLockDao.find(appInfo.getPackageName())) {
                    //被锁定的程序解锁，更新界面图标
                    appLockDao.delete(appInfo.getPackageName());
                    Toast.makeText(AppManagerActivity.this, appInfo.getName()+"已解除锁定", Toast.LENGTH_SHORT).show();
                    holder.iv_status.setImageResource(R.mipmap.security_lock_open);
                } else {
                    //锁定程序，更新界面图标
                    appLockDao.add(appInfo.getPackageName());
                    Toast.makeText(AppManagerActivity.this, appInfo.getName()+"已锁定", Toast.LENGTH_SHORT).show();
                    holder.iv_status.setImageResource(R.mipmap.security_lock);
                }
                //自定义广播刷新内存中加锁的列表
                Intent intent=new Intent();
                intent.setAction("com.jeff.appmanager.notifpackages");
                sendBroadcast(intent);
                return true;
            }
        });
    }

    private void fillData() {
        llLoading.setVisibility(View.VISIBLE);
        new Thread() {
            @Override
            public void run() {
                allAppInfos = AppInfoProvider.getAppInfos(AppManagerActivity.this);
                userAppInfos = new ArrayList<>();
                systemAppInfos = new ArrayList<>();
                for (AppInfo info : allAppInfos) {
                    if (info.isUserApp()) {
                        userAppInfos.add(info);
                    } else {
                        systemAppInfos.add(info);
                    }
                }

                //加载listView数据适配器
                runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        if (adapter == null) {
                            adapter = new AppManagerAdapter();
                            lvAppManager.setAdapter(adapter);
                        } else {
                            adapter.notifyDataSetChanged();
                        }
                        llLoading.setVisibility(View.INVISIBLE);
                        tvAppCount.setText("应用程序：" + allAppInfos.size() + "个");
                    }
                });
            }
        }.start();
    }

    /**
     * 关闭弹出的悬浮窗体
     */
    private void dismissPopupWindow() {
        if (popupWindow != null && popupWindow.isShowing()) {
            popupWindow.dismiss();
            popupWindow = null;
        }
    }

    /**
     * popupWindow对应的点击事件
     *
     * @param v
     */
    @Override
    public void onClick(View v) {
        dismissPopupWindow();
        switch (v.getId()) {
            case R.id.ll_uninstall:
                if (appInfo.isUserApp()) {
                    unInstallApp();
                } else {
                    Toast.makeText(this, "卸载系统应用，需要获取Root权限", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.ll_start:
                startApp();
                break;
            case R.id.ll_share:
//                appInfo.getPackageName();
                shareApp();
                break;
            default:
                break;
        }
    }

    /**
     * 分享应用
     */
    private void shareApp() {
        Intent intent = new Intent();
        intent.setAction("android.intent.action.SEND");
        intent.addCategory(Intent.CATEGORY_DEFAULT);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TEXT, "应用推荐：" + appInfo.getName());
        startActivity(intent);
    }

    /**
     * 卸载应用
     */
    private void unInstallApp() {
        Intent intent = new Intent();
        intent.setAction("android.intent.action.VIEW");
        intent.setAction("android.intent.action.DELETE");
        intent.addCategory("android.intent.category.DEFAULT");
        intent.setData(Uri.parse("package:" + appInfo.getPackageName()));
        startActivityForResult(intent, REFRESH_DATA);
    }

    /**
     * 启动应用
     */
    private void startApp() {
        //查询应用的入口Activity配置信息
        PackageManager pm = getPackageManager();
//        Intent intent=new Intent();
//        intent.setAction("android.intent.action.MAIN");
//        intent.addCategory("android.intent.category.LAUNCHER");
//        //查询手机上所有具有启动能力的activity
//        List<ResolveInfo> infos=pm.queryIntentActivities(intent,PackageManager.GET_INTENT_FILTERS);

        Intent intent = pm.getLaunchIntentForPackage(appInfo.getPackageName());
        if (intent != null) {
            startActivity(intent);
        } else {
            Toast.makeText(this, "对不起，不能启动该应用", Toast.LENGTH_SHORT).show();
        }
    }

    private class AppManagerAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return allAppInfos.size() + 2;
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
            AppInfo appInfo;
            if (position == 0) {//用户进程的标签
                TextView tv = new TextView(getApplicationContext());
                tv.setBackgroundColor(Color.GRAY);
                tv.setTextColor(Color.WHITE);
                tv.setTextSize(18);
                tv.setText("用户程序：" + userAppInfos.size() + "个");
                return tv;
            } else if (position == (userAppInfos.size() + 1)) {
                TextView tv = new TextView(getApplicationContext());
                tv.setBackgroundColor(Color.GRAY);
                tv.setTextColor(Color.WHITE);
                tv.setTextSize(18);
                tv.setText("系统程序：" + systemAppInfos.size() + "个");
                return tv;
            } else if (position <= userAppInfos.size()) {
                appInfo = userAppInfos.get(position - 1);
            } else {
                appInfo = systemAppInfos.get(position - userAppInfos.size() - 2);
            }

            View view;
            ViewHolder holder;
            if (convertView != null && convertView instanceof LinearLayout) {
                view = convertView;
                holder = (ViewHolder) view.getTag();
                Log.d(TAG, "getView: 复用缓存 " + position);
            } else {
                view = View.inflate(getApplicationContext(), R.layout.item_app_manager, null);
                holder = new ViewHolder();
                holder.iv_icon = (ImageView) view.findViewById(R.id.iv_app_icon);
                holder.iv_status = (ImageView) view.findViewById(R.id.iv_status);
                holder.tv_name = (TextView) view.findViewById(R.id.tv_app_name);
                holder.tv_position = (TextView) view.findViewById(R.id.tv_app_position);
                view.setTag(holder);
                Log.d(TAG, "getView: 创建新的View对象 " + position);
            }

            holder.iv_icon.setImageDrawable(appInfo.getIcon());
            holder.tv_name.setText(appInfo.getName());
            if (appInfo.isInRom()) {
                holder.tv_position.setText("手机内存  uid="+appInfo.getUid()+
                        " 接收的流量："+appInfo.getRx()/1024/1024+"MB 发送的流量："+appInfo.getTx()/1024/1024+"MB");
            } else {
                holder.tv_position.setText("手机外部存储  uid="+appInfo.getUid()+
                        " 接收的流量："+appInfo.getRx()/1024/1024+"MB 发送的流量："+appInfo.getTx()/1024/1024+"MB");
            }
            if (appLockDao.find(appInfo.getPackageName())) {
                holder.iv_status.setImageResource(R.mipmap.security_lock);
            } else {
                holder.iv_status.setImageResource(R.mipmap.security_lock_open);
            }
//            holder.iv_status.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//
//                }
//            });

            return view;
        }
    }

    static class ViewHolder {
        ImageView iv_icon;
        ImageView iv_status;
        TextView tv_name;
        TextView tv_position;
    }

    /**
     * 获取某个目录的可用空间
     *
     * @param path 路径
     * @return 可用空间
     */
    private long getAvailSpace(String path) {
        StatFs statf = new StatFs(path);
        statf.getBlockCount();//获取分区个数
        long size = statf.getBlockSize();//获取分区大小
        long count = statf.getAvailableBlocks();//获取可用的区块个数
        return size * count;
    }

    @Override
    protected void onDestroy() {
        dismissPopupWindow();//避免窗体泄露
        super.onDestroy();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        if (requestCode == REFRESH_DATA) {
        fillData();
//        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}
