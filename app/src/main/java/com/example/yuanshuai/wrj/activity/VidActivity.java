package com.example.yuanshuai.wrj.activity;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.example.yuanshuai.wrj.R;

public class VidActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vid);
    }
    public static void actionStart(Context context){
        Intent intent=new Intent(context,VidActivity.class);
        context.startActivity(intent);

    }
}
