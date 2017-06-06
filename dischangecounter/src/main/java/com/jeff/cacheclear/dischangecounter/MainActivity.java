package com.jeff.cacheclear.dischangecounter;

import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ListView;
import android.widget.SlidingDrawer;
import android.widget.TextView;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private PackageManager pm;
    private List<AppTrafficInfo> trafficInfos;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //1.获取一个包管理器。
        pm = getPackageManager();
        trafficInfos = AppTrafficInfoProvider.getAppTrafficInfos(pm);

        initView();

}

    private void initView() {
        SlidingDrawer slidDraw = (SlidingDrawer) findViewById(R.id.slid_draw);
        ListView lv_traffic = (ListView) findViewById(R.id.lv_traffic);
        TextView tv_phone_traffic= (TextView) findViewById(R.id.tv_phone_traffic);
        long[]totalTraffic=AppTrafficInfoProvider.getTotalBytes();
        int totalTx= (int) totalTraffic[0]/1024/1024;
        int totalRx= (int) totalTraffic[1]/1024/1024;
        long[]mobileTraffic=AppTrafficInfoProvider.getMobileBytes();
        int mobileTx= (int) mobileTraffic[0]/1024/1024;
        int mobileRx= (int) mobileTraffic[1]/1024/1024;
        tv_phone_traffic.setText("手机发送："+totalTx+"MB 手机接收："+totalRx+"MB\n"+
        "其中2g/3g/4g发送："+mobileTx+"MB 接收："+mobileRx+"MB");

        TrafficInfoAdapter adapter=new TrafficInfoAdapter(this,trafficInfos);
        lv_traffic.setAdapter(adapter);
    }
}
