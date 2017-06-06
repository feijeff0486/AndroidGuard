package com.jeff.guard.desktop;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity {
    private List<String> packNames;
    private GridView gvApp;
    private MyAdapter adapter;
    private PackageManager pm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        gvApp = (GridView) findViewById(R.id.gv_app);
        pm = getPackageManager();
        Intent intent=new Intent();
        intent.setAction("android.intent.action.MAIN");
        intent.addCategory("android.intent.category.LAUNCHER");
        List<ResolveInfo> infos= pm.queryIntentActivities(intent,
                PackageManager.MATCH_ALL);
        packNames=new ArrayList<>();
        for (ResolveInfo info:infos){
            String packName=info.activityInfo.packageName;
            packNames.add(packName);
//            Intent launchINtent=pm.getLaunchIntentForPackage(packName);
        }

        adapter = new MyAdapter();
        gvApp.setAdapter(adapter);
        gvApp.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String packname=packNames.get(position);
                Intent intent=pm.getLaunchIntentForPackage(packname);
                startActivity(intent);
            }
        });
    }

    private class MyAdapter extends BaseAdapter{

        @Override
        public int getCount() {
            return packNames.size();
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view=View.inflate(getApplicationContext(),R.layout.item_gv_app,null);
            TextView tvAppName= (TextView) view.findViewById(R.id.tv_name);
            ImageView ivAppIcon= (ImageView) view.findViewById(R.id.iv_icon);
            String packName=packNames.get(position);
            try {
                tvAppName.setText(pm.getPackageInfo(packName,0).applicationInfo.loadLabel(pm));
                ivAppIcon.setImageDrawable(pm.getPackageInfo(packName,0).applicationInfo.loadIcon(pm));
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
            tvAppName.setTextColor(Color.BLACK);
            return view;
        }
    }

    @Override
    public void onBackPressed() {
    }
}
