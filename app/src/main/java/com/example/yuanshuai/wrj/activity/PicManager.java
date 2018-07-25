package com.example.yuanshuai.wrj.activity;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.example.yuanshuai.wrj.R;
import com.example.yuanshuai.wrj.adapter.PiclistAdapter;

import java.util.ArrayList;
import java.util.List;

public class PicManager extends AppCompatActivity {
    private PiclistAdapter piclistAdapter;
    private List<String> picdatalist;
    private RecyclerView picmanagelist;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pic_manager);
        initpicdatalist();
        picmanagelist= (RecyclerView) findViewById(R.id.picmanagelist);
        picmanagelist.setLayoutManager(new GridLayoutManager(this,5));
        piclistAdapter=new PiclistAdapter(getApplicationContext(),picdatalist);
        picmanagelist.setAdapter(piclistAdapter);

    }

    public void initpicdatalist(){
        picdatalist = new ArrayList<>();
        picdatalist.add("描述");
        picdatalist.add("时间");
        picdatalist.add("时长");
        picdatalist.add("图片");
        picdatalist.add("视频");
    }
    public void add(){
        picdatalist.add("TestMyItem");
        int position  = picdatalist.size();
        if (position > 0){
            piclistAdapter.notifyDataSetChanged();
        }

    }
    public void click(View view){
        switch (view.getId()){
            case R.id.back:
                finish();
                break;

        }
    }

}
