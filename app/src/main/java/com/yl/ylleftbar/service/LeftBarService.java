package com.yl.ylleftbar.service;

import android.app.Instrumentation;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.IBinder;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.yl.ylleftbar.R;
import com.yl.ylleftbar.service.adapter.AppRecyAdapter;
import com.yl.ylleftbar.service.model.AppInfoModel;
import com.yl.ylleftbar.service.utils.AnimationUtil;
import com.yl.ylleftbar.service.utils.CommonUtil;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class LeftBarService extends Service implements View.OnClickListener {

    private WindowManager mWindowManager;
    private WindowManager.LayoutParams wmParams;
    private View mLeftBarView;
    private RecyclerView mainLeftRecy;
    private AppRecyAdapter mainLeftRecyAdapter;
    private List<PackageInfo> packageInfos;
    private List<AppInfoModel> appInfoModels = new ArrayList<>();
    private Handler mHandler;
    private ImageView wifiImg, signalImg, allApp, mainWifi;
    private TextView mainTime;
    private int[] wifiStateImgs = new int[]{R.drawable.selector_wifi_1,
            R.drawable.selector_wifi_2, R.drawable.selector_wifi_3,
            R.drawable.selector_wifi_4};
    private MyReceiver mMyReceiver;
    private final String NETWORK_STATE_CHANGE = "android.net.conn.CONNECTIVITY_CHANGE";
    private boolean isAllApp = false;


    @Override
    public void onCreate() {
        super.onCreate();
        mHandler = new Handler();
        initWindow();
        initView();
        registReceiver();
    }

    public void registReceiver() {
        if (mMyReceiver != null)
            return;
        mMyReceiver = new MyReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(NETWORK_STATE_CHANGE);
        filter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
        filter.addAction(WifiManager.RSSI_CHANGED_ACTION);
        registerReceiver(mMyReceiver, filter);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void initWindow() {
        mWindowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        wmParams = new WindowManager.LayoutParams();
        wmParams.type =
                WindowManager.LayoutParams.TYPE_SYSTEM_ERROR;

        wmParams.flags = WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;

        wmParams.format = PixelFormat.TRANSLUCENT;
        wmParams.gravity = Gravity.START | Gravity.TOP;
        wmParams.x = 0;
        wmParams.y = 0;
        wmParams.width = 100;
        wmParams.height = WindowManager.LayoutParams.MATCH_PARENT;
    }

    private void initView() {
        LayoutInflater inflater = LayoutInflater.from(getApplicationContext());
        mLeftBarView = inflater.inflate(R.layout.left_bar_view, null);
        mLeftBarView.setBackgroundColor(Color.parseColor("#1a2933"));
        allApp = mLeftBarView.findViewById(R.id.main_all_app);
        mainWifi = mLeftBarView.findViewById(R.id.main_wifi);
        mainLeftRecy = mLeftBarView.findViewById(R.id.main_left_recy);
        mainTime = mLeftBarView.findViewById(R.id.main_time);
        wifiImg = mLeftBarView.findViewById(R.id.main_wifi);
        signalImg = mLeftBarView.findViewById(R.id.main_signal);
        updateCurrentTime();
        initData();
        mainLeftRecyAdapter = new AppRecyAdapter(this, appInfoModels);
        //禁止滑动  布局管理器
        LinearLayoutManager linearLayoutManager=new LinearLayoutManager(this){
            //禁止竖向滑动 RecyclerView 为垂直状态（VERTICAL）
            @Override
            public boolean canScrollVertically() {
                return false;
            }
            //禁止横向滑动 RecyclerView 为水平状态（HORIZONTAL）
            /*@Override
            public boolean canScrollHorizontally() {
                return false;
            }*/
        };
        mainLeftRecy.setLayoutManager(linearLayoutManager);
        mainLeftRecy.setAdapter(mainLeftRecyAdapter);
        allApp.setOnClickListener(this);
        allApp.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                triggerRecentApps();
                return true;
            }
        });
        wifiImg.setOnClickListener(this);
        signalImg.setOnClickListener(this);
        allApp.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        AnimationUtil.startScaleUpAnimation(allApp);
                        break;
                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:
                        AnimationUtil.startScaleDownAnimation(allApp);
                        break;
                }
                return false;
            }
        });
        mWindowManager.addView(mLeftBarView, wmParams);
    }

    public void updateCurrentTime() {
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
        String currentTime = simpleDateFormat.format(calendar.getTime());
        updateTime(currentTime);
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                updateCurrentTime();
            }
        });
    }

    public void updateTime(String time) {
        if (mainTime.isAttachedToWindow() && !TextUtils.isEmpty(time)) {
            mainTime.setText(time);
        }
    }

    public void triggerRecentApps() {
        try {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    // 通过 Instrumentation 发送按键事件
                    Instrumentation instrumentation = new Instrumentation();
                    instrumentation.sendKeyDownUpSync(KeyEvent.KEYCODE_APP_SWITCH);
                }
            }).start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void initData() {
        appInfoModels.add(new AppInfoModel(getResources().getDrawable(R.drawable.voice_assiant), "voice_assiant", "voice_assiant", 0));
        appInfoModels.add(new AppInfoModel(getResources().getDrawable(R.drawable.mute), "mute", "mute", 0));
        appInfoModels.add(new AppInfoModel(getResources().getDrawable(R.drawable.back), "back", "back", 0));
    }

    public void bindRecy() {
        mainLeftRecyAdapter.setDataList(appInfoModels);
    }


    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.main_all_app) {
            if (!"com.yl.yldesktop".equals(CommonUtil.getForegroundActivity(this))) {
                allApp.setImageResource(R.drawable.all_app_unselected);
                Intent home = new Intent(Intent.ACTION_MAIN);
                home.setComponent(new ComponentName("com.yl.yldesktop","com.yl.yldesktop.activity.MainActivity"));
                home.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(home);
                isAllApp = false;
            } else {
                if (isAllApp) {
                    allApp.setImageResource(R.drawable.all_app_unselected);
                    Intent home = new Intent(Intent.ACTION_MAIN);
                    home.setComponent(new ComponentName("com.yl.yldesktop","com.yl.yldesktop.activity.MainActivity"));
                    home.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(home);
                } else {
                    allApp.setImageResource(R.drawable.all_app_selected);
                    Intent home = new Intent(Intent.ACTION_MAIN);
                    home.setComponent(new ComponentName("com.yl.yldesktop","com.yl.yldesktop.activity.AllAppActivity"));
                    home.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(home);
                }
                isAllApp = !isAllApp;
            }
        } else if (v.getId() == R.id.main_wifi) {
            Intent intent = new Intent(Settings.ACTION_WIFI_SETTINGS);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }
    }

    private class MyReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.d("TAG", "action: " + action);
            if (action.equals(NETWORK_STATE_CHANGE)) {
                ConnectivityManager connectivityManager =
                        (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
                boolean isNetOK = false;
                try {
                    isNetOK = networkInfo.isConnected();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                Log.d("TAG", "网络状态发生变化,是否可用：" + isNetOK);
                if (isNetOK) {
                    initWifiState();
                } else {
                    updateNoWifi();
                }

            } else if (action.equals(WifiManager.WIFI_STATE_CHANGED_ACTION)) {
                int wifistate = intent.getIntExtra(
                        WifiManager.EXTRA_WIFI_STATE,
                        WifiManager.WIFI_STATE_DISABLED);

                if (wifistate == WifiManager.WIFI_STATE_DISABLED) {
                    updateNoWifi();
                } else if (wifistate == WifiManager.WIFI_STATE_ENABLED) {
                    try {
                        updateWifiStrength();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            } else if (action.equals(WifiManager.RSSI_CHANGED_ACTION)) {
                initWifiState();
            }
        }
    }


    public void initWifiState() {

        ConnectivityManager manager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = manager
                .getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        Log.d("TAG", "info.isConnected(): " + info.isConnected());
        if (info.isConnected()) {
            updateWifiStrength();
        } else {
            updateNoWifi();
        }
    }

    public void updateWifiStrength() {
        int strength = getStrength(this);
        if (strength >= 0 && strength <= 3)
            mainWifi.setImageResource(wifiStateImgs[strength]);
        Log.d("TAG", "wifi strength: " + strength);
    }

    public void updateNoWifi() {
        if (mainWifi != null) {
            mainWifi.setImageResource(R.drawable.selector_wifi_0);
        }
    }


    public int getStrength(Context context) {
        WifiManager wifiManager = (WifiManager) context
                .getSystemService(Context.WIFI_SERVICE);
        WifiInfo info = wifiManager.getConnectionInfo();
        if (info.getBSSID() != null) {
            int strength = WifiManager.calculateSignalLevel(info.getRssi(), 4);
            // 链接速度
            // int speed = info.getLinkSpeed();
            // // 链接速度单位
            // String units = WifiInfo.LINK_SPEED_UNITS;
            // // Wifi源名称
            // String ssid = info.getSSID();
            return strength;

        }
        return 0;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mMyReceiver != null) {
            unregisterReceiver(mMyReceiver);
        }
    }

    public void sendBroadCast(String action) {
        Intent broadcastIntent = new Intent(action);
        sendBroadcast(broadcastIntent);
    }

}
