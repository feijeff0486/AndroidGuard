package com.jeff.appmanager.utils;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by 小太阳jeff on 2017/6/2.
 */

public class AppLockDBOpenHelper extends SQLiteOpenHelper {
    /**
     * 数据库创建构造方法，数据库名AppLock.db
     * @param context
     */
    public AppLockDBOpenHelper(Context context) {
        super(context, "AppLock.db", null, 1);
    }

    //初始化表结构
    @Override
    public void onCreate(SQLiteDatabase db) {
db.execSQL("create table applock (_id integer primary key autoincrement,packname varchar(20))");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
