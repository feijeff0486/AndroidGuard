package com.jeff.cacheclear.virusclear;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

/**
 * 病毒数据库查询业务类
 * Created by 小太阳jeff on 2017/6/4.
 */

public class AntivirusDao {


    public static boolean isVirus(String md5) {
        String path = "/data/data/com.jeff.androidguard.virusclear/files/antivirus.db";
        boolean result = false;
        //打开病毒数据库
        SQLiteDatabase db = SQLiteDatabase.openDatabase(path, null, SQLiteDatabase.OPEN_READONLY);
        Cursor cursor = db.rawQuery("select * from datable where md5=?", new String[]{md5});
        if (cursor.moveToNext()) {
            result = true;
        }
        cursor.close();
        db.close();
        return result;
    }
}
