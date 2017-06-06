package com.jeff.appmanager.engine;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.net.TrafficStats;
import android.util.Log;

import com.jeff.appmanager.domain.AppInfo;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * engine，数据引擎
 * 业务方法，提供手机里面安装的所有应用程序信息
 * Created by 小太阳jeff on 2017/5/9.
 */

public class AppInfoProvider {
    private static final String TAG = "AppInfoProvider";
    /**
     * 获取所有安装的应用的程序信息
     * system/app 系统应用所在目录
     * data/app 用户安装的应用所在目录
     *
     * @return
     */
    public static List<AppInfo> getAppInfos(Context context) {
        //获取包管理器
        PackageManager packageManager = context.getPackageManager();
        //所有安装在系统上的应用程序信息
        List<PackageInfo> packageInfos = packageManager.getInstalledPackages(0);
        List<AppInfo> appInfos = new ArrayList<>();
        for (PackageInfo packageInfo : packageInfos) {
            AppInfo appInfo = new AppInfo();
            //packageInfo 相当于一个应用程序的清单文件
            String packageName = packageInfo.packageName;
            Drawable icon = packageInfo.applicationInfo.loadIcon(packageManager);
            String name = packageInfo.applicationInfo.loadLabel(packageManager).toString();
            //操作系统分配给应用在系统中的一个固定编号，安装后编号不变，卸载后释放该ID
            int uid = packageInfo.applicationInfo.uid;
//            File revfile=new File("/proc/uid_stat/"+uid+"/tcp_rcv");
//            File sndfile=new File("/proc/uid_stat/"+uid+"/tcp_rnd");
            long tx = TrafficStats.getUidTxBytes(uid);//发送的流量
            long rx = TrafficStats.getUidRxBytes(uid);//接收的流量
            Log.d(TAG, "getAppInfos: uid="+uid+" tx="+tx);

//            TrafficStats.getMobileTxBytes();//获取手机3g/2g网络上传的总流量
//            TrafficStats.getMobileRxBytes();//手机2g/3g下载的总流量
//
//            TrafficStats.getTotalTxBytes();//手机全部网络接口 包括wifi，3g、2g上传的总流量
//            TrafficStats.getTotalRxBytes();//手机全部网络接口 包括wifi，3g、2g下载的总流量</applicationinfo>
            int flags = packageInfo.applicationInfo.flags;//应用程序的标记
            if ((flags & ApplicationInfo.FLAG_SYSTEM) == 0) {
                //用户程序
                appInfo.setUserApp(true);
            } else {
                //系统程序
                appInfo.setUserApp(false);
            }

            if ((flags & ApplicationInfo.FLAG_EXTERNAL_STORAGE) == 0) {
                //手机内存
                appInfo.setInRom(true);
            } else {
                //手机外存储设备
                appInfo.setInRom(false);
            }
            appInfo.setIcon(icon);
            appInfo.setName(name);
            appInfo.setPackageName(packageName);
            appInfo.setUid(uid);
            appInfo.setTx(tx);
            appInfo.setRx(rx);
            appInfos.add(appInfo);

        }
        return appInfos;
    }

}
