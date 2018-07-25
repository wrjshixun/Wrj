package com.example.yuanshuai.wrj.adapter;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.example.yuanshuai.wrj.R;

import java.util.List;

public class PicfileAdapter extends RecyclerView.Adapter<PicfileAdapter.ViewholderA> {
    private Context mcontext;
    //private List<String> picadresslist;
    //private
    //Bitmap bt = BitmapFactory.decodeFile("/sdcard/myImage/" + "head.jpg");
    //public int[]resIds;
    public PicfileAdapter(Context mcontext) {
        this.mcontext = mcontext;

    }

    @Override
    public ViewholderA onCreateViewHolder(ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(mcontext).inflate(R.layout.picfileitem,null,false);
        PicfileAdapter.ViewholderA viewholderA=new PicfileAdapter.ViewholderA(view);

        //动态加载viewholder的布局文件并返回holder
        return viewholderA;
    }

    @Override
    public void onBindViewHolder(ViewholderA holder, int position) {


        holder.imageView.setImageResource(R.mipmap.main);
    }

    @Override
    public int getItemCount() {
        return 1;
    }

    class ViewholderA extends RecyclerView.ViewHolder{
        ImageView imageView;
        public ViewholderA(View itemView) {
            super(itemView);
            imageView=(ImageView)itemView.findViewById(R.id.picfile);
        }
    }
}
