package com.jeff.appmanager;

import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class SecurityActivity extends AppCompatActivity {

    private EditText et_password;
    private Button btn_ok;
    private TextView tv_title;
    private String packname_pro;
    private ImageView iv_icon;
    private ApplicationInfo applicationInfo;
    private PackageManager packageManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_security);
        Intent intent=getIntent();
        packname_pro = intent.getStringExtra("packname");
        packageManager = getPackageManager();
        try {
            applicationInfo = packageManager.getApplicationInfo(packname_pro,0);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        initView();
    }

    private void initView() {
        tv_title = (TextView) findViewById(R.id.tv_title);
        tv_title.setText(applicationInfo.loadLabel(packageManager)+"已被密码保护\n请输入密码");
        iv_icon = (ImageView) findViewById(R.id.iv_icon_pro);
        iv_icon.setImageDrawable(applicationInfo.loadIcon(packageManager));
        et_password = (EditText) findViewById(R.id.et_password);
        btn_ok = (Button) findViewById(R.id.btn_ensure);
        btn_ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String password = String.valueOf(et_password.getText());
                if (TextUtils.isEmpty(password)) {
                    Toast.makeText(SecurityActivity.this, "输入不能为空", Toast.LENGTH_SHORT).show();
                    return;
                }
                //输入非空
                if (password.equals("123456")) {
                    //密码正确，告诉看门狗服务临时不再拦截
                    //自定义广播临时不再拦截该程序
                    Intent intent=new Intent();
                    intent.setAction("com.jeff.appmanager.tempstop");
                    intent.putExtra("packname",packname_pro);
                    sendBroadcast(intent);
                    finish();
                } else {
                    //密码错误
                    Toast.makeText(SecurityActivity.this, "密码错误", Toast.LENGTH_SHORT).show();
                }

            }
        });
    }

    @Override
    protected void onStop() {
        super.onStop();
        finish();
    }

    @Override
    public void onBackPressed() {
        //回到桌面
        Intent intent =new Intent();
        intent.setAction("android.intent.action.MAIN");
        intent.addCategory("android.intent.category.HOME");
        intent.addCategory("android.intent.category.DEFAULT");
        intent.addCategory("android.intent.category.MONKEY");
        startActivity(intent);
    }
}
