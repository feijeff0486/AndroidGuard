package com.jeff.guard.taskmanager;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import com.jeff.guard.taskmanager.services.UpdateWidgetService;
import com.jeff.guard.taskmanager.utils.SharedPreferencesUtils;

import static android.content.Context.MODE_PRIVATE;

/**
 * Created by 小太阳jeff on 2017/5/8.
 */
//rl2
    //r1
    //af3
    //a2
public class TaskCleanWidget extends AppWidgetProvider {
    private static final String TAG = "TaskCleanWidget";

    @Override
    public void onReceive(Context context, Intent intent) {
        context.startService(new Intent(context, UpdateWidgetService.class));
        Log.d(TAG, "Receive");
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        Log.d(TAG, "onUpdate: ");
        super.onUpdate(context, appWidgetManager, appWidgetIds);
    }

    @Override
    public void onEnabled(Context context) {
        context.startService(new Intent(context, UpdateWidgetService.class));
        Log.d(TAG, "onEnabled: ");
        super.onEnabled(context);
    }

    @Override
    public void onDisabled(Context context) {
        context.stopService(new Intent(context, UpdateWidgetService.class));
        Log.d(TAG, "onDisabled: ");
        super.onDisabled(context);
    }
}
