package com.example.yuanshuai.wrj.activity;

import android.content.pm.ActivityInfo;
import android.graphics.SurfaceTexture;
import android.os.Handler;
import android.os.PersistableBundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.amap.api.maps2d.AMap;
import com.amap.api.maps2d.MapView;
import com.example.yuanshuai.wrj.R;
import com.example.yuanshuai.wrj.adapter.ChannelAdapter;
import com.example.yuanshuai.wrj.adapter.SettinglistAdapter;
import com.example.yuanshuai.wrj.application.MyFPVApplication;

import dji.common.camera.SystemState;
import dji.common.error.DJIError;
import dji.common.product.Model;
import dji.common.useraccount.UserAccountState;
import dji.common.util.CommonCallbacks;
import dji.sdk.base.BaseProduct;
import dji.sdk.camera.Camera;
import dji.sdk.camera.VideoFeeder;
import dji.sdk.codec.DJICodecManager;
import dji.sdk.useraccount.UserAccountManager;

public class Main extends AppCompatActivity implements TextureView.SurfaceTextureListener {

//
    // Used to load the 'native-lib' library on application startup.
    static {
        System.loadLibrary("native-lib");
    }



    private String[] namelist=new String[]{"拍照设置","飞行参数设置","WIFI设置","电池设置","待定"};






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
    private View[] views=null;
//    主页侧滑开关
    private ImageView setting;
    private boolean b=true;
//    b为true的时候，小地图，开启监听事件，b为false的时候，大地图，禁用监听事件

//    地图
    private MapView map;
    private AMap aMap;
    private RelativeLayout mapcontainer;
    private View mview;
    private RelativeLayout maptools;
    private ImageView locate;
    private ImageView layer;

//    无人机
    private RelativeLayout dcontainer;
    private TextureView video;
    private View vview;


    private static final String TAG = "dj";
    protected VideoFeeder.VideoDataCallback mReceivedVideoDataCallBack = null;

    // Codec for video live view
    protected DJICodecManager mCodecManager = null;
    private Handler handler;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.main);

        initView(savedInstanceState);



    }






//    绑定界面
    private void initView(Bundle savedInstanceState){
        cover=new RelativeLayout(this);
        setting = (ImageView)findViewById(R.id.setting);
        drawerLayout=(DrawerLayout)findViewById(R.id.drawerlayoout);
        navigationView=(NavigationView)findViewById(R.id.navigation_view);
        mapcontain=(LinearLayout)findViewById(R.id.mapr);
        djcontain=(LinearLayout)findViewById(R.id.djr);
        mapcontainer=(RelativeLayout)findViewById(R.id.map_contain);
        dcontainer=(RelativeLayout)findViewById(R.id.vedio_contain);
        settinglist=(RecyclerView)findViewById(R.id.settinglist);
        maptools=(RelativeLayout)findViewById(R.id.maptools);
        layer=(ImageView)findViewById(R.id.layer);
        locate=(ImageView)findViewById(R.id.locate);
        channelAdapter=new ChannelAdapter(this);
        listname=(TextView)findViewById(R.id.settingname);
        container=(LinearLayout) findViewById(R.id.container);
        init();
        settinglistAdapter=new SettinglistAdapter(this);
        settinglist.setLayoutManager(new LinearLayoutManager(this,LinearLayoutManager.VERTICAL,false));
        channelist.setLayoutManager(new LinearLayoutManager(this,LinearLayoutManager.HORIZONTAL,false));
        settinglistAdapter.setOnItemClickListener(new SettinglistAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int postion) {
                settinglistAdapter.setDefItem(postion);
                listname.setText(namelist[postion]);
                container.removeAllViews();
                container.addView(views[postion],new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
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
        container.addView(views[0],new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        settinglist.setAdapter(settinglistAdapter);
//        渲染mapcontain





//        暂时隐藏mapr
        navigationView.setLayoutParams(new DrawerLayout.LayoutParams(1500, DrawerLayout.LayoutParams.MATCH_PARENT,Gravity.RIGHT));



        setting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mapcontain.setVisibility(View.GONE);
                djcontain.setVisibility(View.VISIBLE);
                navigationView.setLayoutParams(new DrawerLayout.LayoutParams(1500, DrawerLayout.LayoutParams.MATCH_PARENT,Gravity.RIGHT));
                drawerLayout.openDrawer(Gravity.RIGHT);
            }
        });
        layer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mapcontain.setVisibility(View.VISIBLE);
                djcontain.setVisibility(View.GONE);
                navigationView.setLayoutParams(new DrawerLayout.LayoutParams(1500, DrawerLayout.LayoutParams.MATCH_PARENT,Gravity.RIGHT));
                drawerLayout.openDrawer(Gravity.RIGHT);
            }
        });


        mview=LayoutInflater.from(this).inflate(R.layout.mapview,null);
        map=(MapView) mview.findViewById(R.id.map);
        map.onCreate(savedInstanceState);
        if(aMap==null){
            aMap=map.getMap();
        }

        mapcontainer.addView(mview,new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        mapcontainer.addView(cover,new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        vview=LayoutInflater.from(this).inflate(R.layout.vidview,null);
        maptools.setVisibility(View.GONE);
        video=(TextureView)vview.findViewById(R.id.video);
        video.setSurfaceTextureListener(this);
        dcontainer.addView(video,new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        cover.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(b){
                    mapcontainer.removeAllViews();
                    dcontainer.removeAllViews();
                    dcontainer.addView(mview,new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

                    mapcontainer.addView(video);
                    mapcontainer.addView(cover);
                    maptools.setVisibility(View.VISIBLE);

                    b=false;
                }
                else{
                    mapcontainer.removeAllViews();
                    dcontainer.removeAllViews();
                    dcontainer.addView(video,new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

                    mapcontainer.addView(map);
                    mapcontainer.addView(cover);
                    maptools.setVisibility(View.GONE);
                    b=true;
                }
            }
        });
    }



//    初始化侧滑页的五个子页面
    public void init(){
        view1= LayoutInflater.from(this).inflate(R.layout.takeoption,null);
        view2=LayoutInflater.from(this).inflate(R.layout.flyoption,null);
        view3=LayoutInflater.from(this).inflate(R.layout.wifioption,null);
        view4=LayoutInflater.from(this).inflate(R.layout.bateryoption,null);
        view5=LayoutInflater.from(this).inflate(R.layout.bateryoption,null);
        views=new View[]{view1,view2,view3,view4,view5};


        channelist=(RecyclerView)view3.findViewById(R.id.channelist);
    }

    @Override
    protected void onPause() {
        map.onPause();
        uninitPreviewer();
        super.onPause();

    }

    @Override
    protected void onResume() {
        super.onResume();
        map.onResume();
        initPreviewer();
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
    public void closeDrawer(View v){
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
        }
    }

    private void uninitPreviewer() {
        Camera camera = MyFPVApplication.getCameraInstance();
        if (camera != null){
            // Reset the callback
            VideoFeeder.getInstance().getPrimaryVideoFeed().setCallback(null);
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

    private void loginAccount(){

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
    private void initCamera(){
        mReceivedVideoDataCallBack = new VideoFeeder.VideoDataCallback() {

            @Override
            public void onReceive(byte[] videoBuffer, int size) {
                if (mCodecManager != null) {
                    mCodecManager.sendDataToDecoder(videoBuffer, size);
                }
            }
        };

        Camera camera = MyFPVApplication.getCameraInstance();

        if (camera != null) {

            camera.setSystemStateCallback(new SystemState.Callback() {
                @Override
                public void onUpdate(SystemState cameraSystemState) {
                    if (null != cameraSystemState) {

                        int recordTime = cameraSystemState.getCurrentVideoRecordingTimeInSeconds();
                        int minutes = (recordTime % 3600) / 60;
                        int seconds = recordTime % 60;

                        final String timeString = String.format("%02d:%02d", minutes, seconds);
                        final boolean isVideoRecording = cameraSystemState.isRecording();

//                        Main.this.runOnUiThread(new Runnable() {
//
//                            @Override
//                            public void run() {
//
//                                recordingTime.setText(timeString);
//
//                                /*
//                                 * Update recordingTime TextView visibility and mRecordBtn's check state
//                                 */
//                                if (isVideoRecording){
//                                    recordingTime.setVisibility(View.VISIBLE);
//                                }else

//                                    recordingTime.setVisibility(View.INVISIBLE);
//                                }
//                            }
//                        });
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

}


