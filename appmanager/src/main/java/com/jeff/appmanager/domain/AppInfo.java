package com.jeff.appmanager.domain;

import android.graphics.drawable.Drawable;

/**
 * 应用信息的业务bean
 * Created by 小太阳jeff on 2017/5/9.
 */

public class AppInfo {
    private Drawable icon;//应用图标
    private String name;//应用名
    private String packageName;//包名
    private int uid;//应用分配的Id
    private long tx;//应用发送的流量
    private long rx;//应用接收的流量
    private boolean userApp;//是否是用户应用
    private boolean inRom;//是否安装在手机内部存储

    public long getTx() {
        return tx;
    }

    public void setTx(long tx) {
        this.tx = tx;
    }

    public long getRx() {
        return rx;
    }

    public void setRx(long rx) {
        this.rx = rx;
    }

    @Override
    public String toString() {
        return "AppInfo{" +
                "icon=" + icon +
                ", name='" + name + '\'' +
                ", packageName='" + packageName + '\'' +
                ", uid=" + uid +
                ", tx=" + tx +
                ", rx=" + rx +
                ", userApp=" + userApp +
                ", inRom=" + inRom +
                '}';
    }


    public int getUid() {
        return uid;
    }

    public void setUid(int uid) {
        this.uid = uid;
    }
    public boolean isInRom() {
        return inRom;
    }

    public void setInRom(boolean inRom) {
        this.inRom = inRom;
    }

    public Drawable getIcon() {
        return icon;
    }

    public void setIcon(Drawable icon) {
        this.icon = icon;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public boolean isUserApp() {
        return userApp;
    }

    public void setUserApp(boolean userApp) {
        this.userApp = userApp;
    }

}
