package com.jeff.applock;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

public class AppLockActivity extends AppCompatActivity {
    private static final String TAG = "AppLockActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_lock);
    }

}
