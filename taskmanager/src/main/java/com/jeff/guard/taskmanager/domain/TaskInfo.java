package com.jeff.guard.taskmanager.domain;

import android.graphics.drawable.Drawable;

/**
 * 进程信息的而业务bean
 * Created by 小太阳jeff on 2017/5/7.
 */

public class TaskInfo {
    private Drawable icon;
    private String packageName;
    private String appName;
    private long memorySize;
    //是否是用户进程
    private boolean userTask;
    //判断当前的item的条目是否被勾选上
    private boolean checked;

    public boolean isChecked() {
        return checked;
    }

    public void setChecked(boolean checked) {
        this.checked = checked;
    }

    public Drawable getIcon() {
        return icon;
    }

    public void setIcon(Drawable icon) {
        this.icon = icon;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public long getMemorySize() {
        return memorySize;
    }

    public void setMemorySize(long memorySize) {
        this.memorySize = memorySize;
    }

    public boolean isuserTask() {
        return userTask;
    }

    public void setuserTask(boolean userTask) {
        this.userTask = userTask;
    }

    @Override
    public String toString() {
        return "TaskInfo [packageName=" + packageName + ", appName=" + appName
                + ", memorySize=" + memorySize + ", userTask=" + userTask + "]";
    }
}
