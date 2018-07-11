package com.example.yuanshuai.wrj.adapter;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;


import com.example.yuanshuai.wrj.R;

import java.util.ArrayList;
import java.util.List;

public class SettinglistAdapter extends RecyclerView.Adapter<SettinglistAdapter.ViewHolder> {
    private Context context;
    private OnItemClickListener onItemClickListener=null;
    private int defItem=-1;
    int[] list_select=new int[]{R.mipmap.take,R.mipmap.option,R.mipmap.wifi_n,R.mipmap.batery_n,R.mipmap.setting};
    int[] list_notselect=new int[]{R.mipmap.take_select,R.mipmap.option_select,R.mipmap.wifi_select,R.mipmap.batery_select,R.mipmap.setting};
    public SettinglistAdapter(Context context) {
        super();
        this.context=context;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(context).inflate(R.layout.settingitem,parent,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        holder.imageView.setImageResource(list_notselect[position]);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(onItemClickListener!=null){
                    onItemClickListener.onItemClick(v,position);
                }
            }
        });
        if(defItem!=-1){
            if(defItem==position){
                holder.imageView.setImageResource(list_notselect[position]);
            }
            else{
                holder.imageView.setImageResource(list_select[position]);
            }
        }
    }

    @Override
    public int getItemCount() {
        return list_select.length;
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        ImageView imageView;
        public ViewHolder(View itemView) {
            super(itemView);
            imageView=(ImageView)itemView.findViewById(R.id.image1);
        }
    }


    public interface OnItemClickListener {
        public void onItemClick(View view, int postion);
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public void setDefItem(int defItem) {
        this.defItem = defItem;
        notifyDataSetChanged();
    }
}
