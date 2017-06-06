package com.jeff.appmanager.utils;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * 程序锁Dao
 * Created by 小太阳jeff on 2017/6/2.
 */

public class AppLockDao {
    private AppLockDBOpenHelper helper;
    private static final String TAG = "AppLockDao";

    public AppLockDao(Context context) {
        this.helper = new AppLockDBOpenHelper(context);
    }

    /**
     * 添加一个要锁定的程序包名
     *
     * @param packname
     */
    public void add(String packname) {
        SQLiteDatabase db = helper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("packname", packname);
        db.insert("applock", null, values);
        db.close();
    }

    /**
     * 将锁定的程序包名从表中取消
     *
     * @param packname
     */
    public void delete(String packname) {
        SQLiteDatabase db = helper.getWritableDatabase();
        db.delete("applock", "packname=?", new String[]{packname});
        db.close();
    }

    /**
     * 查询一条程序锁记录是否存在
     *
     * @param packname
     * @return
     */
    public boolean find(String packname) {
        boolean result = false;
        SQLiteDatabase db = helper.getReadableDatabase();
        Cursor cursor = db.query("applock", null, "packname=?", new String[]{packname}, null, null, null);
        if (cursor.moveToNext()) {
            result = true;
        }
        cursor.close();
        db.close();
        return result;
    }

    /**
     * 查询全部包名
     *
     * @return
     */
    public List<String> findAll() {
        List<String> protectPacknames=new ArrayList<>();
        SQLiteDatabase db = helper.getReadableDatabase();
        Cursor cursor = db.query("applock", new String[]{"packname"}, null, null, null, null, null);
        while (cursor.moveToNext()) {
            protectPacknames.add(cursor.getString(0));
        }
        cursor.close();
        db.close();
        Log.d(TAG, "findAll: protectPacknames= "+protectPacknames);
        return protectPacknames;
    }
}
