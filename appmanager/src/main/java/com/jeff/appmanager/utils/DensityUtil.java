package com.jeff.appmanager.utils;

import android.content.Context;

/**
 * 实现像素px和dp之间转换的工具类
 * Created by 小太阳jeff on 2017/5/9.
 */

public class DensityUtil {
    /**
     * 根据手机分辨率从dip的单位转换成px
     * @param context 上下文
     * @param dpValue dp
     * @return px
     */
    public static int dip2px(Context context,float dpValue){
        //获取屏幕的像素密度
        final float scale= context.getResources().getDisplayMetrics().density;
        return (int) (dpValue*scale+0.5f);
    }

    /**
     * 根据手机分辨率从px的单位转换成dp
     * @param context 上下文
     * @param pxValue px
     * @return dp
     */
    public static int px2dip(Context context,float pxValue){
        //获取屏幕的像素密度
        final float scale=context.getResources().getDisplayMetrics().density;
        return (int) (pxValue/scale+0.5f);
    }
}
