package com.example.yuanshuai.wrj.activity;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.example.yuanshuai.wrj.R;
import com.example.yuanshuai.wrj.adapter.PicfileAdapter;
import com.example.yuanshuai.wrj.adapter.PiclistAdapter;

import java.util.ArrayList;
import java.util.List;

public class PicActivity extends AppCompatActivity {
    private PicfileAdapter picfileAdapter;
    //private List<String> picdatalist;
    private RecyclerView picfilelist;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pic);
        picfilelist= (RecyclerView) findViewById(R.id.picfilelist);
        picfilelist.setLayoutManager(new GridLayoutManager(this,5));
        picfileAdapter=new PicfileAdapter(getApplicationContext());
        picfilelist.setAdapter(picfileAdapter);
        ImageView picfileback=(ImageView)findViewById(R.id.picfileback);
        picfileback.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }
    public static void actionStart(Context context){
        Intent intent =new Intent(context,PicActivity.class);
        context.startActivity(intent);
    }
    public void initpicfilelist(){

    }

}
