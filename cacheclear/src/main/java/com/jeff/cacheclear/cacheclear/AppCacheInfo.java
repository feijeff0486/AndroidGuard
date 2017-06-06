package com.jeff.cacheclear.cacheclear;

import android.graphics.drawable.Drawable;

/**
 * Created by 小太阳jeff on 2017/6/5.
 */

public class AppCacheInfo {
    private Drawable icon;
    private String name;
    private String packname;
    private long cache;
    private long data;
    private long code;

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

    public String getPackname() {
        return packname;
    }

    public void setPackname(String packname) {
        this.packname = packname;
    }

    public long getCache() {
        return cache;
    }

    public void setCache(long cache) {
        this.cache = cache;
    }


    public long getData() {
        return data;
    }

    public void setData(long data) {
        this.data = data;
    }

    public long getCode() {
        return code;
    }

    public void setCode(long code) {
        this.code = code;
    }

    @Override
    public String toString() {
        return "AppCacheInfo{" +
                "icon=" + icon +
                ", name='" + name + '\'' +
                ", packname='" + packname + '\'' +
                ", cache=" + cache +
                ", data=" + data +
                ", code=" + code +
                '}';
    }
}
