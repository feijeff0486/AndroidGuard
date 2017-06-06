package com.jeff.cacheclear.cacheclear;

import android.content.pm.ApplicationInfo;
import android.content.pm.IPackageDataObserver;
import android.content.pm.IPackageStatsObserver;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageStats;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.support.v7.app.AppCompatActivity;
import android.text.format.Formatter;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private TextView tvScanStat;
    private ProgressBar pbScanStat;
    private PackageManager pm;
    private static final String TAG = "MainActivity";
    private LinearLayout llScanList;
    private final int SCAN_FINISHED = 0;
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
//                case SCAN_FINISHED:
//                    tvScanStat.setText("扫描完成");
////                    AppCacheInfoAdapter adapter = new AppCacheInfoAdapter(getApplicationContext(), appCacheInfos);
////                    lv_info.setAdapter(adapter);
//                    break;
            }
        }
    };
//    private List<AppCacheInfo> appCacheInfos;
//    private ListView lv_info;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
//        getFilesDir();//data/data/包名/files/
//        getCacheDir();//data/data/包名/cache目录
//        //缓存清理实际上是清理data/data/包名/cache目录下的文件
//
        File file = new File(getCacheDir(), "aaa.txt");
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(file);
            fos.write("随便输入的缓存数据".getBytes());
            fos.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        scanCache();
    }

    /**
     * 扫描手机中所有应用的缓存信息
     */
    private void scanCache() {
        pm = getPackageManager();
//        appCacheInfos = new ArrayList<>();
        new Thread() {
            @Override
            public void run() {
                Method getPackageSizeInfo = null;
                try {
                    getPackageSizeInfo = PackageManager.class.getMethod(
                            "getPackageSizeInfo", String.class,
                            IPackageStatsObserver.class);
                } catch (NoSuchMethodException e) {
                    e.printStackTrace();
                }

                List<PackageInfo> packageInfos = pm.getInstalledPackages(0);
                pbScanStat.setMax(packageInfos.size());
                int progress = 0;
                for (PackageInfo info : packageInfos) {
                    try {
                        getPackageSizeInfo.invoke(pm, info.packageName, new MyDataObserver());
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    } catch (InvocationTargetException e) {
                        e.printStackTrace();
                    }
                    progress++;
                    pbScanStat.setProgress(progress);
                    try {
                        Thread.sleep(50);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        tvScanStat.setText("扫描完成");
                    }
                });
//                Message msg = Message.obtain();
//                msg.what = SCAN_FINISHED;
//                handler.sendMessage(msg);
            }
        }.start();

    }

    private class MyDataObserver extends IPackageStatsObserver.Stub {

        @Override
        public void onGetStatsCompleted(PackageStats pStats, boolean succeeded) throws RemoteException {
            final long cache = pStats.cacheSize;
            final long dataSize = pStats.dataSize;
            final long codeSize = pStats.codeSize;
            final String packName = pStats.packageName;
//            if (cache>0){
                Log.d(TAG, "onGetStatsCompleted: cache= " + cache +
                        " dataSize= " + dataSize + " codeSize= " + codeSize);
                Log.d(TAG, "packName= " + packName + "\n----------------------");
//            }

            final ApplicationInfo appinfo;
            try {
                appinfo = pm.getApplicationInfo(packName, 0);

//                AppCacheInfo cacheInfo = new AppCacheInfo();
//                cacheInfo.setIcon(appinfo.loadIcon(pm));
//                cacheInfo.setName(appinfo.loadLabel(pm).toString());
//                cacheInfo.setPackname(appinfo.packageName);
//                cacheInfo.setCache(cache);
//                cacheInfo.setCode(codeSize);
//                cacheInfo.setData(dataSize);
//                appCacheInfos.add(cacheInfo);

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        tvScanStat.setText("正在扫描：" + appinfo.loadLabel(pm));
//                        if (cache > 0) {
//                            TextView item = new TextView(getApplicationContext());
//                            item.setText(appinfo.loadLabel(pm)+"- 缓存大小："+
//                                    Formatter.formatFileSize(getApplicationContext(),cache));
//                            item.setTextColor(Color.BLACK);
//                            llScanList.addView(item,0);

                            View view = View.inflate(getApplicationContext(), R.layout.item_app_cache_info, null);
                            ImageView iv_icon = (ImageView) view.findViewById(R.id.iv_app_icon);
                            TextView tv_cache = (TextView) view.findViewById(R.id.tv_app_cache);
                            TextView tv_name = (TextView) view.findViewById(R.id.tv_app_name);
                            TextView tv_size = (TextView) view.findViewById(R.id.tv_app_size);
                            iv_icon.setImageDrawable(appinfo.loadIcon(pm));
                            tv_name.setText(appinfo.loadLabel(pm).toString());
                            tv_size.setText("应用大小：" + Formatter.formatFileSize(getApplicationContext(), codeSize));
                            tv_cache.setText("缓存大小：" +
                                    Formatter.formatFileSize(getApplicationContext(), cache) +
                                    "  数据大小：" + Formatter.formatFileSize(getApplicationContext(), dataSize));
                            view.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    try {
                                        Method deleteApplicationCacheFiles = PackageManager.class.getMethod(
                                                "deleteApplicationCacheFiles", String.class,
                                                IPackageDataObserver.class);

                                        deleteApplicationCacheFiles.invoke(pm, packName, new MyPackageDataObserver());

                                    } catch (NoSuchMethodException e) {
                                        e.printStackTrace();
                                    } catch (IllegalAccessException e) {
                                        e.printStackTrace();
                                    } catch (InvocationTargetException e) {
                                        e.printStackTrace();
                                    }
                                    Toast.makeText(MainActivity.this, "清理成功", Toast.LENGTH_SHORT).show();
                                }
                            });
                            llScanList.addView(view, 0);
//                        if (cache==0){
//                            llScanList.removeView(view);
//                        }

//                        }
                    }
                });

            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }

        }
    }


    private class MyPackageDataObserver extends IPackageDataObserver.Stub {

        @Override
        public void onRemoveCompleted(String packageName, boolean succeeded) throws RemoteException {
            Log.e(TAG, "onRemoveCompleted: " + packageName + " " + succeeded);
            System.out.printf("onRemoveCompleted: " + packageName + " " + succeeded);
        }
    }

    private void initView() {
        tvScanStat = (TextView) findViewById(R.id.tv_scan_stat);
        pbScanStat = (ProgressBar) findViewById(R.id.pb_clean_stat);
        llScanList = (LinearLayout) findViewById(R.id.ll_scan_list);
//        lv_info = (ListView) findViewById(R.id.lv_packInfo);
//        lv_info.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//            @Override
//            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                Toast.makeText(MainActivity.this, "长按清除缓存", Toast.LENGTH_SHORT).show();
//            }
//        });
//        lv_info.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
//            @Override
//            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
//                Toast.makeText(MainActivity.this, "清除成功", Toast.LENGTH_SHORT).show();
//                return true;
//            }
//        });
    }

    //清理手机的全部缓存
    public void cleanAll(View v) {
        try {
            Method freeStorageAndNotify = PackageManager.class.getMethod(
                    "freeStorageAndNotify", String.class,
                    IPackageDataObserver.class);

            freeStorageAndNotify.invoke(pm, Integer.MAX_VALUE, new MyPackageDataObserver());

        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }


    }
}
