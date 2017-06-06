package com.jeff.cacheclear.virusclear;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private ImageView ivScanStat;
    private ImageView ivRotate;
    private TextView tvStatText;
    private ProgressBar pbScan;
    private PackageManager pm;
    private final int SCANING=1;
    private final int DBCOPYED=2;
    private final int SCANFINISHED=3;
    private LinearLayout llScanList;
    private Handler handler=new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case SCANING:
                    ScanInfo scaninfo= (ScanInfo) msg.obj;
                    tvStatText.setText("正在扫描："+scaninfo.name);
                    TextView child=new TextView(getApplicationContext());
                    if (scaninfo.isVirus){
                        child.setTextColor(Color.RED);
                        child.setText("发现病毒："+scaninfo.name);
                        ivScanStat.setImageResource(R.mipmap.main_status_icon_danger);
                    }else {
                        child.setTextColor(Color.BLACK);
                        child.setText("扫描安全："+scaninfo.name);
                    }
                    llScanList.addView(child,0);//加载在最上方
                    break;
                case DBCOPYED:
                    Toast.makeText(MainActivity.this, "数据库拷贝完成", Toast.LENGTH_SHORT).show();
                    break;
                case SCANFINISHED:
                    tvStatText.setText("扫描完毕");
                    ivRotate.clearAnimation();
                    ivRotate.setVisibility(View.INVISIBLE);
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        RotateAnimation scanAnim = new RotateAnimation(0, 360, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        scanAnim.setDuration(1000);
        scanAnim.setRepeatCount(Animation.INFINITE);
        ivRotate.startAnimation(scanAnim);

        new Thread() {
            @Override
            public void run() {
                copyDB("antivirus.db");
                Log.d(TAG, "run: 数据库拷贝完成");
                Message msg=Message.obtain();
                msg.what=DBCOPYED;
                handler.sendMessage(msg);
            }
        }.start();
        scanVirus();
    }

    //扫描病毒
    private void scanVirus() {
        pm = getPackageManager();
        tvStatText.setText("正在初始化杀毒引擎...");
        new Thread() {
            @Override
            public void run() {
                List<PackageInfo> infos = pm.getInstalledPackages(0);
                pbScan.setMax(infos.size());
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                int process=0;
                for (PackageInfo info : infos) {
                    String dataDir = info.applicationInfo.dataDir;
                    String sourceDir = info.applicationInfo.sourceDir;//apk完整路径
                    Log.d(TAG, "scanVirus: dataDir= " + dataDir + "\nsourceDir= " +
                            sourceDir);
                    String md5 = getFileMd5(sourceDir);
                    Log.d(TAG, "scanVirus: md5= " + md5 + "\n-------------------------");

                    //zip
//                    ZipFile zipFile=new ZipFile(sourceDir);
//                    zipFile.getEntry("AndroidManifest.xml");

                    ScanInfo scanInfo=new ScanInfo();
                    scanInfo.name=info.applicationInfo.loadLabel(pm).toString();
                    scanInfo.packname=info.packageName;

                    if (AntivirusDao.isVirus(md5)) {
                        //发现病毒
                        scanInfo.isVirus=true;
                    } else {
                        //扫描安全
                        scanInfo.isVirus=false;
                    }
                    Message msg=Message.obtain();
                    msg.obj=scanInfo;
                    msg.what=SCANING;
                    handler.sendMessage(msg);
                    try {
                        Thread.sleep(300);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    process++;
                    pbScan.setProgress(process);
                }
                Message msg=Message.obtain();
                msg.what=SCANFINISHED;
                handler.sendMessage(msg);
            }
        }.start();

    }

    //扫描信息内部类
    class ScanInfo{
        String packname;
        String name;
        boolean isVirus;
    }

    private void initView() {
        ivScanStat = (ImageView) findViewById(R.id.iv_scan_status);
        ivRotate = (ImageView) findViewById(R.id.iv_scan);
        tvStatText = (TextView) findViewById(R.id.tv_scan_stat);
        pbScan = (ProgressBar) findViewById(R.id.pb_scan);
        llScanList = (LinearLayout) findViewById(R.id.ll_scan_list);
        pbScan.setMax(100);
        new Thread() {
            @Override
            public void run() {
                for (int i = 0; i <= 100; i++) {
                    try {
                        Thread.sleep(100);
                        pbScan.setProgress(i);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }.start();
    }

    /**
     * 获取文件的Md5
     *
     * @param path
     * @return
     */
    private String getFileMd5(String path) {
        File file = new File(path);
        StringBuffer sb = new StringBuffer();
        try {
            MessageDigest digest = MessageDigest.getInstance("md5");//或sha1
            //md5
            FileInputStream fis = new FileInputStream(file);
            byte[] buffer = new byte[1024];
            int len = -1;
            while ((len = fis.read(buffer)) != -1) {
                digest.update(buffer, 0, len);
            }
            byte[] result = digest.digest();
            for (byte b : result) {
                //与运算
                int number = b & 0xff;//加盐
                String str = Integer.toHexString(number);
                if (str.length() == 1) {
                    sb.append("0");
                }
                sb.append(str);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return sb.toString();
    }

    /**
     * 拷贝数据库文件
     * @param daName 数据库db文件
     */
    private void copyDB(String daName) {
        //拿到数据库文件
        File filesDir = getFilesDir();
        Log.d(TAG, "copyDB: 路径:" + filesDir.getAbsolutePath());
        File destFile = new File(getFilesDir(), daName);//获取路径，要拷贝的目标地址

        //数据库已经存在就不再拷贝
        if (destFile.exists()) {
            Log.d(TAG, "copyDB: 数据库:" + daName + "已存在!");
            return;
        }
        FileOutputStream out = null;
        InputStream in = null;
        try {
            in = getAssets().open(daName);
            out = new FileOutputStream(destFile);

            //读数据
            int len = 0;
            byte[] buffer = new byte[1024];
            while ((len = in.read(buffer)) != -1) {
                out.write(buffer, 0, len);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                in.close();
                out.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
