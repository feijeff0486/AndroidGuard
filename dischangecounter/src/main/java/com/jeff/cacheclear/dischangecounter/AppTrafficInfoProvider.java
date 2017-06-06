package com.jeff.cacheclear.dischangecounter;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.net.TrafficStats;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by 小太阳jeff on 2017/6/4.
 */

public class AppTrafficInfoProvider {
    private static final String TAG = "AppTrafficInfoProvider";
    private static AppTrafficInfo info;

    public static List<AppTrafficInfo> getAppTrafficInfos(PackageManager pm) {

        List<AppTrafficInfo> appTrafficInfos = new ArrayList<>();
        //遍历手机操作系统 获取所有的应用程序的uid
        List<ApplicationInfo> appliactaionInfos = pm.getInstalledApplications(0);
        for (ApplicationInfo applicationInfo : appliactaionInfos) {
            info = new AppTrafficInfo();

            String name = (String) applicationInfo.loadLabel(pm);
            Drawable icon = applicationInfo.loadIcon(pm);
            int uid = applicationInfo.uid;    // 获得软件uid
            //proc/uid_stat/10086
            //发送的 上传的流量byte
            long tx = TrafficStats.getUidTxBytes(uid);
            //下载的流量 byte
            //方法返回值 -1 代表的是应用程序没有产生流量 或者操作系统不支持流量统计
            long rx = TrafficStats.getUidRxBytes(uid);

            info.setUid(uid);
            info.setName(name);
            info.setIcon(icon);
            info.setTx(tx);
            info.setRx(rx);
            Log.d(TAG, "uid:" + uid);
            Log.d(TAG, "Label: " + applicationInfo.loadLabel(pm));
            Log.d(TAG, "tx= " + tx + " rx= " + rx);
            Log.d(TAG, "-----------------------------");
            appTrafficInfos.add(info);
        }

        return appTrafficInfos;
    }

    public static long[] getTotalBytes() {
        //手机全部网络接口 包括wifi，3g、2g上传的总流量
        long ttx = TrafficStats.getTotalTxBytes();
        //手机全部网络接口 包括wifi，3g、2g下载的总流量</applicationinfo>
        long trx = TrafficStats.getTotalRxBytes();
        long[] total = {ttx, trx};
        return total;
    }

    public static long[] getMobileBytes() {
        //获取手机3g/2g网络上传的总流量
        long mtx = TrafficStats.getMobileTxBytes();
        //手机2g/3g下载的总流量
        long mrx = TrafficStats.getMobileRxBytes();
        long[] mobile = {mtx, mrx};
        return mobile;
    }
}
