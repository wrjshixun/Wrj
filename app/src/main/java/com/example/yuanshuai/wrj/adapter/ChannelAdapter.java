package com.example.yuanshuai.wrj.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.yuanshuai.wrj.R;

import java.net.PasswordAuthentication;

public class ChannelAdapter extends RecyclerView.Adapter<ChannelAdapter.ViewHolder>{
    private Context context;
    private OnItemClickListener onItemClickListener;
    private  int defItem=-1;
    private int[] list=new int[]{149,151,153,155,157159,161};
    public ChannelAdapter(Context context) {
        super();
        this.context=context;
    }

    @Override
    public int getItemCount() {
        return list.length;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        holder.textView.setText(""+list[position]);
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
                holder.textView.setTextColor(context.getResources().getColor(R.color.blue));
            }
            else{
                holder.textView.setTextColor(context.getResources().getColor(R.color.white));
            }
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(context).inflate(R.layout.channelitem, parent,false);
        return new ViewHolder(view);
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        private TextView textView;

        public ViewHolder(View itemView) {
            super(itemView);
            textView=(TextView)itemView.findViewById(R.id.channeltext);
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
