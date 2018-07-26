package com.example.yuanshuai.wrj.activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.SurfaceTexture;
import android.graphics.Typeface;
import android.media.Image;
import android.media.MediaCodec;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.os.PersistableBundle;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.Size;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;


import com.amap.api.maps.AMap;
import com.amap.api.maps.CameraUpdate;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.MapView;
import com.amap.api.maps.UiSettings;
import com.amap.api.maps.model.BitmapDescriptor;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.CameraPosition;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.maps.model.PolylineOptions;
import com.example.yuanshuai.wrj.Classifier.Classifier;
import com.example.yuanshuai.wrj.Classifier.TensorFlowObjectDetectionAPIModel;
import com.example.yuanshuai.wrj.R;
import com.example.yuanshuai.wrj.Views.OverlayView;
import com.example.yuanshuai.wrj.adapter.ChannelAdapter;
import com.example.yuanshuai.wrj.adapter.SettinglistAdapter;
import com.example.yuanshuai.wrj.application.MyFPVApplication;
import com.example.yuanshuai.wrj.env.BorderedText;
import com.example.yuanshuai.wrj.env.ImageUtils;
import com.example.yuanshuai.wrj.env.Logger;
import com.example.yuanshuai.wrj.model.UserInfoOutput;
import com.example.yuanshuai.wrj.net.Net;
import com.example.yuanshuai.wrj.tracking.MultiBoxTracker;

import org.bouncycastle.crypto.engines.ISAACEngine;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

import dji.common.airlink.SignalQualityCallback;
import dji.common.battery.BatteryState;
import dji.common.camera.SystemState;
import dji.common.error.DJIError;
import dji.common.error.DJISDKError;
import dji.common.flightcontroller.FlightControllerState;
import dji.common.product.Model;
import dji.common.useraccount.UserAccountState;
import dji.common.util.CommonCallbacks;
import dji.log.DJILog;
import dji.sdk.base.BaseProduct;
import dji.sdk.camera.Camera;
import dji.sdk.camera.MediaFile;
import dji.sdk.camera.VideoFeeder;
import dji.sdk.codec.DJICodecManager;
import dji.sdk.flightcontroller.FlightController;
import dji.sdk.products.Aircraft;
import dji.sdk.sdkmanager.DJISDKManager;
import dji.sdk.useraccount.UserAccountManager;
import dji.ux.widget.BatteryWidget;
import dji.ux.widget.ReturnHomeWidget;
import dji.ux.widget.TakeOffWidget;
import dji.ux.widget.WiFiSignalWidget;
import dji.ux.widget.dashboard.DashboardWidget;

public class Main extends AppCompatActivity implements TextureView.SurfaceTextureListener {

    //
    // Used to load the 'native-lib' library on application startup.
    static {
        System.loadLibrary("native-lib");
    }
    /*------------------------------物体识别相关--------------------------------------------------------*/
    private static final Logger LOGGER = new Logger();
    //private static final String TAG = Main.class.getName();
    //protected VideoFeeder.VideoDataCallback mReceivedVideoDataCallBack = null;
    //protected DJICodecManager mCodecManager = null;
//    protected TextureView mVideoSurface = null;  //更名为video
//    private Handler handler; //有一个不需要忘记哪个了
    private Handler handlers;
    private HandlerThread handlerThread;
    private int[] rgbBytes = null;
    private byte[][] yuvBytes = new byte[3][];
    private byte[] luminanceCopy;
    private int yRowStride;
    protected int previewWidth = 0;
    protected int previewHeight = 0;
    private Runnable imageConverter;
    private static final float TEXT_SIZE_DIP = 10;
    private static final int TF_OD_API_INPUT_SIZE = 300;
    private static final String TF_OD_API_MODEL_FILE =
            "file:///android_asset/ssd_mobilenet_v1_android_export.pb";
    private static final String TF_OD_API_LABELS_FILE = "file:///android_asset/coco_labels_list.txt";
    private enum DetectorMode {
        TF_OD_API, MULTIBOX, YOLO;
    }
    private static final DetectorMode MODE = DetectorMode.TF_OD_API;
    private static final boolean MAINTAIN_ASPECT = MODE == DetectorMode.YOLO;
    private Classifier detector ;
    private BorderedText borderedText;//字体组件
    private MultiBoxTracker tracker;//识别的物体框
    private Integer sensorOrientation = 90;  //手机可以获得，但是无人机发现90正好适配，可能存在隐患，保留，不清楚参数的详细背后逻辑
    private long lastProcessingTimeMs;
    private Bitmap rgbFrameBitmap = null;
    private Bitmap croppedBitmap = null;
    private Bitmap cropCopyBitmap = null;
    private static final float MINIMUM_CONFIDENCE_TF_OD_API = 0.6f;
    private Matrix frameToCropTransform;
    private Matrix cropToFrameTransform;
    private long timestamp = 0;
    private static final boolean SAVE_PREVIEW_BITMAP = true;
    OverlayView trackingOverlay;
    private final int NUMBER_OF_FRAME = 2; //1s几帧
    private final long  TIME_INTEVAL = 1000/NUMBER_OF_FRAME;
    private boolean flags = true;
    boolean flag = true;
    public boolean isDetection = false;
    public boolean isTrackVisible = true;
    /*------------------------------大疆无人机开发相关--------------------------------------------------------*/
    private String[] namelist = new String[]{"拍照设置", "飞行参数设置", "WIFI设置", "电池设置", "待定"};

    private AtomicBoolean isRegistrationInProgress = new AtomicBoolean(false);
    private static final String[] REQUIRED_PERMISSION_LIST = new String[]{
            Manifest.permission.VIBRATE,
            Manifest.permission.INTERNET,
            Manifest.permission.ACCESS_WIFI_STATE,
            Manifest.permission.WAKE_LOCK,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_NETWORK_STATE,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.CHANGE_WIFI_STATE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.BLUETOOTH,
            Manifest.permission.BLUETOOTH_ADMIN,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.READ_PHONE_STATE,
    };
    private List<String> missingPermission = new ArrayList<>();
    private static final int REQUEST_PERMISSION_CODE = 12345;


    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private RelativeLayout cover;
    //    地图侧滑容器
    private LinearLayout mapcontain;
    //    大疆侧滑容器
    private LinearLayout djcontain;
    //    设置列表
    private RecyclerView settinglist;

    //    信道列表
    private RecyclerView channelist;
    //    设置页面容器
    private LinearLayout container;
    private SettinglistAdapter settinglistAdapter;
    private ChannelAdapter channelAdapter;
    private TextView listname;

    //    侧滑页的五个子页面
    private View view1;
    private View view2;
    private View view3;
    private View view4;
    private View view5;
    private View[] views = null;
    //    主页侧滑开关
    private ImageView setting;
    private boolean b = true;
//    b为true的时候，小地图，开启监听事件，b为false的时候，大地图，禁用监听事件

    //    地图
    private MapView map;
    private AMap aMap;
    private UiSettings uiSettings;
    private RelativeLayout mapcontainer;
    private View mview;
    private RelativeLayout maptools;
    private LinearLayout offline;
    private LatLng latLng = new LatLng(36.66694, 117.14017);                           //无人机当前位置
    private List latlngs = new ArrayList<LatLng>();
    private float height = 0;

    //    无人机
    private RelativeLayout dcontainer;
    private TextureView video;
    private View vview;
    private int times=0;
    private FlightController flightController = null;
    private Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            Toast.makeText(Main.this, "" + handler, Toast.LENGTH_SHORT).show();
            if (msg.what == 0) {
                Toast.makeText(Main.this, "" + latLng.toString(), Toast.LENGTH_SHORT).show();
                latlngs.add(latLng);
                CameraUpdate cameraUpdate = CameraUpdateFactory.newCameraPosition(new CameraPosition(latLng, 19, 20, 0));
                aMap.moveCamera(cameraUpdate);
                aMap.setMapTextZIndex(2);
                aMap.addPolyline((new PolylineOptions())
                        //手动数据测试
                        //.add(new LatLng(26.57, 106.71),new LatLng(26.14,105.55),new LatLng(26.58, 104.82), new LatLng(30.67, 104.06))
                        //集合数据
                        .addAll(latlngs)
                        //线的宽度
                        .width(10).setDottedLine(true).geodesic(true)
                        //颜色
                        .color(Color.argb(255, 255, 20, 147)));
            }
            return false;
        }
    });
    private Thread t;


    //    主页控件
    private ImageView wifistate;
    private ImageView baterystate;
    private TextView batery;
    private TextView distance;
    private TextView recordTime;
    private TextView h;
    private TextView d;
    private TextView vs;
    private TextView hs;
    private ImageView pic;
    private ImageView user;
    private ImageView locate;
    private ImageView layer;
    private ImageView detect;
    private ImageView v;                                                  //录像按钮
    private ImageView take;
    private TakeOffWidget up;
    private ReturnHomeWidget down;

    //  侧滑控件
    private ImageView standard;
    private ImageView satelite;
    private ImageView night;
    private static final String TAG = "dj";
    protected VideoFeeder.VideoDataCallback mReceivedVideoDataCallBack = null;

    // Codec for video live view
    protected DJICodecManager mCodecManager = null;

    Thread thread;
    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            while(flag) {
                Bitmap bitmap = video.getBitmap();
                if(bitmap != null)
                    doDetection(bitmap);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.main);

        initView(savedInstanceState);

        //识别线程初始化

    }


    //    绑定界面
    private void initView(Bundle savedInstanceState) {
        cover = new RelativeLayout(this);
        setting = (ImageView) findViewById(R.id.setting);
        drawerLayout = (DrawerLayout) findViewById(R.id.drawerlayoout);
        navigationView = (NavigationView) findViewById(R.id.navigation_view);
        mapcontain = (LinearLayout) findViewById(R.id.mapr);
        djcontain = (LinearLayout) findViewById(R.id.djr);
        mapcontainer = (RelativeLayout) findViewById(R.id.map_contain);
        dcontainer = (RelativeLayout) findViewById(R.id.vedio_contain);
        settinglist = (RecyclerView) findViewById(R.id.settinglist);
        maptools = (RelativeLayout) findViewById(R.id.maptools);
        offline = (LinearLayout) findViewById(R.id.offline);
        layer = (ImageView) findViewById(R.id.layer);
        locate = (ImageView) findViewById(R.id.locate);
        channelAdapter = new ChannelAdapter(this);
        listname = (TextView) findViewById(R.id.settingname);
        container = (LinearLayout) findViewById(R.id.container);
        wifistate = (ImageView) findViewById(R.id.wifistate);
        baterystate = (ImageView) findViewById(R.id.baterystate);
        batery = (TextView) findViewById(R.id.batery);
        distance = (TextView) findViewById(R.id.distance);
        recordTime = (TextView) findViewById(R.id.recordtime);
        h = (TextView) findViewById(R.id.h);
        d = (TextView) findViewById(R.id.d);
        vs = (TextView) findViewById(R.id.vs);
        hs = (TextView) findViewById(R.id.hs);
        pic = (ImageView) findViewById(R.id.pic);
        user = (ImageView) findViewById(R.id.user);
        up = (TakeOffWidget) findViewById(R.id.up);
        down = (ReturnHomeWidget) findViewById(R.id.down);
        v = (ImageView) findViewById(R.id.video);
        take = (ImageView) findViewById(R.id.take);
        detect = (ImageView) findViewById(R.id.detect);
        standard = (ImageView) findViewById(R.id.standard);
        satelite = (ImageView) findViewById(R.id.satellite);
        night = (ImageView) findViewById(R.id.night);
        init();

        settinglistAdapter = new SettinglistAdapter(this);
        settinglist.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        channelist.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));

        detect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(isDetection) {
                    if(thread != null && thread.isAlive()){
                        flag = false;
                        flags = true;
                        isDetection = false;

                        trackingOverlay.setVisibility(View.INVISIBLE);
                        isTrackVisible = false;
                    }
                }else {
                    flag = true;
                    flags = false;
                    thread = new Thread(runnable);
                    thread.start();
                    isDetection = true;
                    if(!isTrackVisible){
                        trackingOverlay.setVisibility(View.VISIBLE);
                        isTrackVisible = true;
                    }
                }
            }
        });

        settinglistAdapter.setOnItemClickListener(new SettinglistAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int postion) {
                settinglistAdapter.setDefItem(postion);
                listname.setText(namelist[postion]);
                container.removeAllViews();
                container.addView(views[postion], new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            }
        });
        settinglistAdapter.setDefItem(0);
        channelAdapter.setOnItemClickListener(new ChannelAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int postion) {
                channelAdapter.setDefItem(postion);
            }
        });
        channelist.setAdapter(channelAdapter);
        channelAdapter.setDefItem(0);
        container.addView(views[0], new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        settinglist.setAdapter(settinglistAdapter);
        navigationView.setLayoutParams(new DrawerLayout.LayoutParams(1500, DrawerLayout.LayoutParams.MATCH_PARENT, Gravity.RIGHT));
        mview = LayoutInflater.from(this).inflate(R.layout.mapview, null);
        map = (MapView) mview.findViewById(R.id.map);
//        initMap
        map.onCreate(savedInstanceState);
        if (aMap == null) {
            aMap = map.getMap();
            uiSettings = aMap.getUiSettings();
        }
        uiSettings.setZoomControlsEnabled(false);
        uiSettings.setCompassEnabled(true);
        locate();
        mapcontainer.addView(mview, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        mapcontainer.addView(cover, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        vview = LayoutInflater.from(this).inflate(R.layout.vidview, null);
        maptools.setVisibility(View.GONE);
        video = (TextureView) vview.findViewById(R.id.video);
        video.setSurfaceTextureListener(this);
        dcontainer.addView(video, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        cover.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (b) {
                    if(thread != null && thread.isAlive()){
                        flag = false;
                        flags = true;
                        isDetection = false;

                        trackingOverlay.setVisibility(View.INVISIBLE);
                        isTrackVisible = false;
                    }

                    mapcontainer.removeAllViews();
                    dcontainer.removeAllViews();
                    dcontainer.addView(mview, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

                    mapcontainer.addView(video);
                    mapcontainer.addView(cover);
                    maptools.setVisibility(View.VISIBLE);

                    b = false;
                } else {
                    mapcontainer.removeAllViews();
                    dcontainer.removeAllViews();
                    dcontainer.addView(video, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

                    mapcontainer.addView(map);
                    mapcontainer.addView(cover);
                    maptools.setVisibility(View.GONE);
                    b = true;
                }
            }
        });
        setting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mapcontain.setVisibility(View.GONE);
                djcontain.setVisibility(View.VISIBLE);
                navigationView.setLayoutParams(new DrawerLayout.LayoutParams(1500, DrawerLayout.LayoutParams.MATCH_PARENT, Gravity.RIGHT));
                drawerLayout.openDrawer(Gravity.RIGHT);
            }
        });
        layer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mapcontain.setVisibility(View.VISIBLE);
                djcontain.setVisibility(View.GONE);
                navigationView.setLayoutParams(new DrawerLayout.LayoutParams(1500, DrawerLayout.LayoutParams.MATCH_PARENT, Gravity.RIGHT));
                drawerLayout.openDrawer(Gravity.RIGHT);
            }
        });
        locate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                locate();
            }
        });
        pic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Main.this, PicManager.class);
                startActivity(intent);
            }
        });
        user.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                if (Net.getNet().getUserInfoOutput() == null) {
                    intent.setClass(Main.this, Login.class);
                } else {
                    intent.setClass(Main.this, Infomation.class);
                }
                startActivity(intent);
            }
        });
        offline.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Main.this,
                        com.amap.api.maps.offlinemap.OfflineMapActivity.class));
            }
        });
        standard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                aMap.setMapType(AMap.MAP_TYPE_NORMAL);
            }
        });
        satelite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                aMap.setMapType(AMap.MAP_TYPE_SATELLITE);
            }
        });
        night.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                aMap.setMapType(AMap.MAP_TYPE_NIGHT);
            }
        });


//        飞机的初始化
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        initCamera();

//        起飞
        up.onTakeOffEnable(true);
        down.onReturnHomeEnable(true);

    }


    //    初始化侧滑页的五个子页面
    public void init() {
        view1 = LayoutInflater.from(this).inflate(R.layout.takeoption, null);
        view2 = LayoutInflater.from(this).inflate(R.layout.flyoption, null);
        view3 = LayoutInflater.from(this).inflate(R.layout.wifioption, null);
        view4 = LayoutInflater.from(this).inflate(R.layout.bateryoption, null);
        view5 = LayoutInflater.from(this).inflate(R.layout.bateryoption, null);
        views = new View[]{view1, view2, view3, view4, view5};
        channelist = (RecyclerView) view3.findViewById(R.id.channelist);
    }

    @Override
    protected void onPause() {
        handlerThread.quitSafely();
        try {
            handlerThread.join();
            handlerThread = null;
            handlers = null;
        } catch (final InterruptedException e) {
            LOGGER.e(e, "Exception!");
        }

        map.onPause();
        uninitPreviewer();
        super.onPause();

    }

    @Override
    protected void onResume() {
        super.onResume();

        handlerThread = new HandlerThread("inference");
        handlerThread.start();
        handlers = new Handler(handlerThread.getLooper());

        map.onResume();
        initPreviewer();
        Log.e("main","onrume");
        onProductChange();
    }

    @Override
    protected void onDestroy() {
        map.onDestroy();
        uninitPreviewer();
        super.onDestroy();
    }

    @Override
    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
        super.onSaveInstanceState(outState, outPersistentState);
        map.onSaveInstanceState(outState);
    }

    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */
    public native String stringFromJNI();

    //    关闭侧滑
    public void closeDrawer(View v) {
        drawerLayout.closeDrawer((Gravity.RIGHT));
    }

    private void initPreviewer() {

        BaseProduct product = MyFPVApplication.getProductInstance();

        if (product == null || !product.isConnected()) {
            showToast(getString(R.string.disconnected));
        } else {
            if (null != video) {
                video.setSurfaceTextureListener(this);
            }
            if (!product.getModel().equals(Model.UNKNOWN_AIRCRAFT)) {
                VideoFeeder.getInstance().getPrimaryVideoFeed().setCallback(mReceivedVideoDataCallBack);
            }
            Aircraft aircraft=(Aircraft)product;
            if(aircraft!=null){
                aircraft.getAirLink().setUplinkSignalQualityCallback(new SignalQualityCallback() {
                    @Override
                    public void onUpdate(int i) {
                        update(i);
                    }
                });
                aircraft.getBattery().setStateCallback(new BatteryState.Callback() {
                    @Override
                    public void onUpdate(BatteryState batteryState) {
                        int i=batteryState.getCurrent()*100/batteryState.getFullChargeCapacity();
                        updateBattery(i);
                    }
                });
                aircraft.getFlightController().setStateCallback(new FlightControllerState.Callback() {
                    @Override
                    public void onUpdate(@NonNull FlightControllerState flightControllerState) {
                        times++;
                        if(times==20){
                            times=0;
                            if(flightControllerState!=null){
                                if(flightControllerState.isFlying()&&flightControllerState.getAircraftLocation()!=null){
                                    latLng=new LatLng(flightControllerState.getAircraftLocation().getLatitude(),flightControllerState.getAircraftLocation().getLongitude());
                                    aMap.addPolyline((new PolylineOptions())
                                            .add(latLng)
                                            //线的宽度
                                            .width(10).setDottedLine(true).geodesic(true)
                                            //颜色
                                            .color(Color.argb(255, 255, 20, 147)));
                                    locate();
                                    h.setText("H:"+flightControllerState.getAircraftLocation().getAltitude()+"M");
                                    if(height<flightControllerState.getAircraftLocation().getAltitude()){
                                        height=flightControllerState.getAircraftLocation().getAltitude();
                                    }
                                    hs.setText("H.S:"+Math.sqrt(Math.pow(flightControllerState.getVelocityX(),2)+Math.pow(flightControllerState.getVelocityY(),2)));
                                    vs.setText("V.S:"+flightControllerState.getVelocityZ());

                                }
                                else{

                                }



                            }
                        }
                    }
                });
            }


        }

    }

    private void uninitPreviewer() {
        Camera camera = MyFPVApplication.getCameraInstance();
        if (camera != null) {
            // Reset the callback
            VideoFeeder.getInstance().getPrimaryVideoFeed().setCallback(null);
        }
        BaseProduct product=MyFPVApplication.getProductInstance();
        Aircraft aircraft=(Aircraft)product;
        if(aircraft!=null){
            aircraft.getAirLink().setUplinkSignalQualityCallback(null);
            aircraft.getBattery().setStateCallback(null);
            aircraft.getFlightController().setStateCallback(null);
        }

    }

    public void showToast(final String msg) {
        runOnUiThread(new Runnable() {
            public void run() {
                Toast.makeText(Main.this, msg, Toast.LENGTH_SHORT).show();
            }
        });
    }

    protected void onProductChange() {
        initPreviewer();
        loginAccount();
    }

    private void loginAccount() {

        UserAccountManager.getInstance().logIntoDJIUserAccount(this,
                new CommonCallbacks.CompletionCallbackWith<UserAccountState>() {
                    @Override
                    public void onSuccess(final UserAccountState userAccountState) {
                        Log.e(TAG, "Login Success");
                    }

                    @Override
                    public void onFailure(DJIError error) {
                        showToast("Login Error:"
                                + error.getDescription());
                    }
                });
    }

    private void initCamera() {
        mReceivedVideoDataCallBack = new VideoFeeder.VideoDataCallback() {
            boolean flags = true;
            @Override
            public void onReceive(byte[] videoBuffer, int size) {
                if (mCodecManager != null) {
                    mCodecManager.sendDataToDecoder(videoBuffer, size);
                }


            }
        };

        Camera camera = MyFPVApplication.getCameraInstance();
        if (camera != null) {
            camera.setMediaFileCallback(new MediaFile.Callback() {
                @Override
                public void onNewFile(@NonNull MediaFile mediaFile) {
//                    截图或者录像
//                    @NonNull final File destDir	File instance of location to save the files, which can not be null.
//                    @Nullable String fileNameWithoutExtension	The fileName to store in mobile devices, without the file extension.<br> If it is null, the file name in the camera's SDCard will be used.
//                    @NonNull final DownloadListener<String> callback	Completion callback that receives the execution result.
                    if(mediaFile.getMediaType()== MediaFile.MediaType.JPEG||mediaFile.getMediaType()== MediaFile.MediaType.RAW_DNG){
//                        mediaFile.fetchFileData();
                    }
                }
            });

            camera.setSystemStateCallback(new SystemState.Callback() {
                @Override
                public void onUpdate(SystemState cameraSystemState) {
                    if (null != cameraSystemState) {

                        final int Time = cameraSystemState.getCurrentVideoRecordingTimeInSeconds();
                        int minutes = (Time % 3600) / 60;
                        int seconds = Time % 60;

                        final String timeString = String.format("%02d:%02d", minutes, seconds);
                        final boolean isVideoRecording = cameraSystemState.isRecording();
                        Main.this.runOnUiThread(new Runnable() {

                            @Override
                            public void run() {

                                recordTime.setText(timeString);

                                /*
                                 * Update recordingTime TextView visibility and mRecordBtn's check state
                                 */
                                if (isVideoRecording) {
                                    recordTime.setVisibility(View.VISIBLE);
                                } else {
                                    recordTime.setVisibility(View.INVISIBLE);
                                }
                            }
                        });


                    }
                }
            });

        }

        final Aircraft aircraft=(Aircraft) MyFPVApplication.getProductInstance();
        if(aircraft!=null){
            aircraft.getAirLink().setUplinkSignalQualityCallback(new SignalQualityCallback() {
                @Override
                public void onUpdate(int i) {
                    update(i);
                }
            });
            aircraft.getBattery().setStateCallback(new BatteryState.Callback() {
                @Override
                public void onUpdate(BatteryState batteryState) {
                    int i=batteryState.getCurrent()*100/batteryState.getFullChargeCapacity();
                    updateBattery(i);
                }
            });
            aircraft.getFlightController().setStateCallback(new FlightControllerState.Callback() {
                @Override
                public void onUpdate(@NonNull FlightControllerState flightControllerState) {
                    times++;
                    if(times==20){
                        times=0;
                        if(flightControllerState!=null){
                            if(flightControllerState.isFlying()&&flightControllerState.getAircraftLocation()!=null){
                                latLng=new LatLng(flightControllerState.getAircraftLocation().getLatitude(),flightControllerState.getAircraftLocation().getLongitude());
                                aMap.addPolyline((new PolylineOptions())
                                        .add(latLng)
                                        //线的宽度
                                        .width(10).setDottedLine(true).geodesic(true)
                                        //颜色
                                        .color(Color.argb(255, 255, 20, 147)));
                                locate();
                                h.setText("H:"+flightControllerState.getAircraftLocation().getAltitude()+"M");
                                if(height<flightControllerState.getAircraftLocation().getAltitude()){
                                    height=flightControllerState.getAircraftLocation().getAltitude();
                                }
                                hs.setText("H.S:"+Math.sqrt(Math.pow(flightControllerState.getVelocityX(),2)+Math.pow(flightControllerState.getVelocityY(),2)));
                                vs.setText("V.S:"+flightControllerState.getVelocityZ());

                            }
                            else{

                            }



                        }
                    }
                }
            });
        }


    }


    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {

        Log.e(TAG, "onSurfaceTextureAvailable");
        if (mCodecManager == null) {
            mCodecManager = new DJICodecManager(this, surface, width, height);
            mCodecManager.setYuvDataCallback(new DJICodecManager.YuvDataCallback() {
                @Override
                public void onYuvDataReceived(ByteBuffer byteBuffer, int i, int i1, int i2) {
                    //                    ByteBuffer yuvFrame	YUV data buffer in the codec.
//                    int dataSize	size of the YUV data
//                    int width	width of the video
//                    int height	height of the video
                }
            });
        }
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
        Log.e(TAG, "onSurfaceTextureSizeChanged");
    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        Log.e(TAG,"onSurfaceTextureDestroyed");
        if (mCodecManager != null) {
            mCodecManager.cleanSurface();
            mCodecManager = null;
        }

        return false;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {
    }


    //    回到中心位置
    private void locate() {
        //参数依次是：视角调整区域的中心点坐标、希望调整到的缩放级别(3-19)、俯仰角0°~45°（垂直与地图时为0）、偏航角 0~360° (正北方为0)
        aMap.clear();
        aMap.addMarker(new MarkerOptions().position(latLng).title("无人机").snippet("DefaultMarker").icon(BitmapDescriptorFactory.fromBitmap(BitmapFactory.decodeResource(getResources(), R.mipmap.locate2))));
        CameraUpdate cameraUpdate = CameraUpdateFactory.newCameraPosition(new CameraPosition(latLng, 19, 20, 0));
        aMap.moveCamera(cameraUpdate);
    }



    private void checkAndRequestPermissions() {
        // Check for permissions
        for (String eachPermission : REQUIRED_PERMISSION_LIST) {
            if (ContextCompat.checkSelfPermission(this, eachPermission) != PackageManager.PERMISSION_GRANTED) {
                missingPermission.add(eachPermission);
            }
        }
        // Request for missing permissions
        if (!missingPermission.isEmpty() && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            ActivityCompat.requestPermissions(this,
                    missingPermission.toArray(new String[missingPermission.size()]),
                    REQUEST_PERMISSION_CODE);
        }

    }


    /**
     * Result of runtime permission request
     */
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        // Check for granted permission and remove from missing list
        if (requestCode == REQUEST_PERMISSION_CODE) {
            for (int i = grantResults.length - 1; i >= 0; i--) {
                if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                    missingPermission.remove(permissions[i]);
                }
            }
        }
        // If there is enough permission, we will start the registration
        if (missingPermission.isEmpty()) {
            startSDKRegistration();
        } else {
            showToast("Missing permissions!!!");
        }
    }

    private void startSDKRegistration() {
        if (isRegistrationInProgress.compareAndSet(false, true)) {
            AsyncTask.execute(new Runnable() {
                @Override
                public void run() {
                    showToast("registering, pls wait...");
                    DJISDKManager.getInstance().registerApp(getApplicationContext(), new DJISDKManager.SDKManagerCallback() {
                        @Override
                        public void onRegister(DJIError djiError) {
                            if (djiError == DJISDKError.REGISTRATION_SUCCESS) {
                                DJILog.e("App registration", DJISDKError.REGISTRATION_SUCCESS.getDescription());
                                DJISDKManager.getInstance().startConnectionToProduct();
                                showToast("Register Success");
                            } else {
                                showToast("Register sdk fails, check network is available");
                            }
                            Log.v(TAG, djiError.getDescription());
                        }

                        @Override
                        public void onProductChange(BaseProduct oldProduct, BaseProduct newProduct) {
                            Log.d(TAG, String.format("onProductChanged oldProduct:%s, newProduct:%s", oldProduct, newProduct));
                        }
                    });
                }
            });
        }

    }
    private void update(int i){
        if(i<25){
            wifistate.setImageResource(R.mipmap.wifi0);
        }
        else if(i<50){
            wifistate.setImageResource(R.mipmap.wifi1);

        }
        else if(i<75){
            wifistate.setImageResource(R.mipmap.wifi2);
        }
        else if(i<100){
            wifistate.setImageResource(R.mipmap.wifi3);
        }
    }
    private void updateBattery(int i){
        if(i<20){
            baterystate.setImageResource(R.mipmap.battery1);
        }
        else if(i<30){
            baterystate.setImageResource(R.mipmap.battery2);
        }
        else if(i<50){
            baterystate.setImageResource(R.mipmap.battery3);
        }
        else if(i<70){
            baterystate.setImageResource(R.mipmap.battery4);
        }
        else if(i<90){
            baterystate.setImageResource(R.mipmap.battery5);
        }
        else if(i<100){
            baterystate.setImageResource(R.mipmap.battery6);
        }
    }

    private void doDetection(Bitmap bitmap){
        try {
            // Initialize the storage bitmaps once when the resolution is known.
            if (rgbBytes == null) {
                //预览尺寸
                previewHeight = bitmap.getHeight();
                previewWidth = bitmap.getWidth();
                rgbBytes = new int[previewHeight * previewWidth];
                onPreviewSizeChosen(new Size(previewWidth, previewHeight), 90);
            }
        } catch (final Exception e) {
            LOGGER.e(e, "Exception!");
            return;
        }

        final byte[] bytes  = getNV21(previewWidth,previewHeight,bitmap);
        yuvBytes[0] = bytes;
        yRowStride = previewWidth;

        imageConverter =
                new Runnable() {
                    @Override
                    public void run() {
                        ImageUtils.convertYUV420SPToARGB8888(bytes, previewWidth, previewHeight, rgbBytes);//rgbBytes = new int[previewWidth * previewHeight];已经被实例化
                    }
                };

        processImage(bitmap);

        try {
            Thread.sleep(TIME_INTEVAL);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void onPreviewSizeChosen(final Size size, final int rotation) throws IOException {
        final float textSizePx =
                TypedValue.applyDimension(
                        TypedValue.COMPLEX_UNIT_DIP, TEXT_SIZE_DIP, getResources().getDisplayMetrics());

        borderedText = new BorderedText(textSizePx);//字体控件,参数是px值
        borderedText.setTypeface(Typeface.MONOSPACE);//设置字体

        tracker = new MultiBoxTracker(this);//方框

        int cropSize = TF_OD_API_INPUT_SIZE;//OD的输入尺寸大小，300*300

        if (MODE == DetectorMode.TF_OD_API){
            //此处创建了分类器，创建了分类模型
            detector = TensorFlowObjectDetectionAPIModel.create(
                    getAssets(), TF_OD_API_MODEL_FILE, TF_OD_API_LABELS_FILE, TF_OD_API_INPUT_SIZE);
            cropSize = TF_OD_API_INPUT_SIZE;
        }

        previewWidth = size.getWidth(); //相机预览图的宽、高
        previewHeight = size.getHeight();

        sensorOrientation = rotation - getScreenOrientation();
        LOGGER.i("Camera orientation relative to screen canvas: %d", sensorOrientation);

        LOGGER.i("Initializing at size %dx%d", previewWidth, previewHeight);
        rgbFrameBitmap = Bitmap.createBitmap(previewWidth, previewHeight, Bitmap.Config.ARGB_8888);
        croppedBitmap = Bitmap.createBitmap(cropSize, cropSize, Bitmap.Config.ARGB_8888);

        frameToCropTransform =
                ImageUtils.getTransformationMatrix(
                        previewWidth, previewHeight,
                        cropSize, cropSize,
                        sensorOrientation, MAINTAIN_ASPECT);

        cropToFrameTransform = new Matrix();
        frameToCropTransform.invert(cropToFrameTransform);

        trackingOverlay = (OverlayView) findViewById(R.id.tracking_overlay);
        trackingOverlay.addCallback(
                new OverlayView.DrawCallback() {
                    @Override
                    public void drawCallback(final Canvas canvas) {
                        tracker.draw(canvas);
                    }
                });

    }

    protected int getScreenOrientation() {
        switch (getWindowManager().getDefaultDisplay().getRotation()) {
            case Surface.ROTATION_270:
                return 270;
            case Surface.ROTATION_180:
                return 180;
            case Surface.ROTATION_90:
                return 90;
            default:
                return 0;
        }
    }

    byte [] getNV21(int inputWidth, int inputHeight, Bitmap scaled) {
        int [] argb = new int[inputWidth * inputHeight];
        scaled.getPixels(argb, 0, inputWidth, 0, 0, inputWidth, inputHeight);
        byte [] yuv = new byte[inputWidth*inputHeight*3/2];
        encodeYUV420SP(yuv, argb, inputWidth, inputHeight);
        //scaled.recycle();

        return yuv;
    }

    void encodeYUV420SP(byte[] yuv420sp, int[] argb, int width, int height) {

        final int frameSize = width * height;
        int yIndex = 0;
        int uvIndex = frameSize;
        int a, R, G, B, Y, U, V;
        int index = 0;

        for (int j = 0; j < height; j++) {
            for (int i = 0; i < width; i++) {
                a = (argb[index] & 0xff000000) >> 24; // a is not used obviously
                R = (argb[index] & 0xff0000) >> 16;
                G = (argb[index] & 0xff00) >> 8;
                B = (argb[index] & 0xff) >> 0;
                // well known RGB to YUV algorithm

                Y = ( (  66 * R + 129 * G +  25 * B + 128) >> 8) +  16;
                U = ( ( -38 * R -  74 * G + 112 * B + 128) >> 8) + 128;
                V = ( ( 112 * R -  94 * G -  18 * B + 128) >> 8) + 128;

                // NV21 has a plane of Y and interleaved planes of VU each sampled by a factor of 2
                //    meaning for every 4 Y pixels there are 1 V and 1 U.  Note the sampling is every other
                //    pixel AND every other scanline.
                yuv420sp[yIndex++] = (byte) ((Y < 0) ? 0 : ((Y > 255) ? 255 : Y));

                if (j % 2 == 0 && index % 2 == 0) {
                    yuv420sp[uvIndex++] = (byte)((V<0) ? 0 : ((V > 255) ? 255 : V));
                    yuv420sp[uvIndex++] = (byte)((U<0) ? 0 : ((U > 255) ? 255 : U));
                }

                index ++;

            }

        }

    }

    protected void processImage(Bitmap bitmap){
        ++timestamp;
        final long currTimestamp = timestamp;
        byte[] originalLuminance = getLuminance();//亮度
        Log.e("OnFrameInfo",""+previewHeight+" "+previewWidth+" "+getLuminanceStride()+
                " "+sensorOrientation+" "+originalLuminance+" "+timestamp);

        tracker.onFrame(
                previewWidth,
                previewHeight,
                getLuminanceStride(),
                sensorOrientation,//传感器方向
                originalLuminance,
                timestamp);

        trackingOverlay.postInvalidate();

        //下面这一句问题很大
        imageConverter.run();
        rgbFrameBitmap = bitmap;
        // rgbFrameBitmap.setPixels(getRgbBytes(), 0, previewWidth, 0, 0, previewWidth, previewHeight);

        //imageView.setImageBitmap(rgbFrameBitmap);

        //ImageUtils.saveBitmap(rgbFrameBitmap,"rgbfile.png");
        if (luminanceCopy == null) {
            luminanceCopy = new byte[originalLuminance.length];
        }

        System.arraycopy(originalLuminance, 0, luminanceCopy, 0, originalLuminance.length);
//
        final Canvas canvas = new Canvas(croppedBitmap);
        canvas.drawBitmap(rgbFrameBitmap, frameToCropTransform, null);
        // For examining the actual TF input.
        if (SAVE_PREVIEW_BITMAP) {
            ImageUtils.saveBitmap(croppedBitmap);//保存图片，保存路径root = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "tensorflow";
        }
        //图像保存正确
//
        runInBackground(new Runnable() {
            @Override
            public void run() {
                LOGGER.i("Running detection on image " + currTimestamp);
                final long startTime = SystemClock.uptimeMillis();
                final List<Classifier.Recognition> results = detector.recognizeImage(croppedBitmap);
                lastProcessingTimeMs = SystemClock.uptimeMillis() - startTime;

                cropCopyBitmap = Bitmap.createBitmap(croppedBitmap);
                final Canvas canvas = new Canvas(cropCopyBitmap);
                final Paint paint = new Paint();
                paint.setColor(Color.RED);
                paint.setStyle(Paint.Style.STROKE);
                paint.setStrokeWidth(2.0f);

                float minimumConfidence = MINIMUM_CONFIDENCE_TF_OD_API;

                final List<Classifier.Recognition> mappedRecognitions =
                        new LinkedList<Classifier.Recognition>();

                for (final Classifier.Recognition result : results) {
                    final RectF location = result.getLocation();
                    if (location != null && result.getConfidence() >= minimumConfidence) {
                        canvas.drawRect(location, paint);

                        cropToFrameTransform.mapRect(location);//将location矩形进行了变换，将裁剪的图片转换成了原来的正常帧
                        result.setLocation(location);//存储正常帧的位置信息
                        mappedRecognitions.add(result);//匹配的矩形列表存储此结果
                    }
                }

                tracker.trackResults(mappedRecognitions, luminanceCopy, currTimestamp);
                trackingOverlay.postInvalidate();

                //requestRender();
            }
        });
    }

    //亮度 跨度
    protected int getLuminanceStride() {
        return yRowStride;
    }

    //亮度
    protected byte[] getLuminance() {
        return yuvBytes[0];
    }

    protected synchronized void runInBackground(final Runnable r) {
        if (handlers != null) {
            handlers.post(r);
        }
    }

    public void requestRender() {
        final OverlayView overlay = (OverlayView) findViewById(R.id.debug_overlay);
        if (overlay != null) {
            overlay.postInvalidate();
        }
    }
}


