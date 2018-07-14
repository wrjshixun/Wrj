package com.example.yuanshuai.wrj.application;

import android.app.Application;
import android.content.Context;

import com.secneo.sdk.Helper;

public class MApplication extends Application {

    private MyFPVApplication myFPVApplication;
    @Override
    protected void attachBaseContext(Context paramContext) {
        super.attachBaseContext(paramContext);
        Helper.install(MApplication.this);
        if ( myFPVApplication== null) {
            myFPVApplication = new MyFPVApplication();
            myFPVApplication.setContext(this);
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        myFPVApplication.onCreate();
    }

}