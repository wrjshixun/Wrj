package com.example.yuanshuai.wrj.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.yuanshuai.wrj.R;

import java.util.List;

public class PiclistAdapter extends RecyclerView.Adapter<PiclistAdapter.viewholderA> {

    private Context mContext;
    private List<String> mlist;

    public PiclistAdapter(Context mContext,List<String> list) {
        this.mContext = mContext;
        mlist=list;
    }

    @Override
    public viewholderA onCreateViewHolder(ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(mContext).inflate(R.layout.picitem,parent,false);
        viewholderA viewholderA=new viewholderA(view);

        //动态加载viewholder的布局文件并返回holder
        return viewholderA;
    }

    @Override
    public int getItemCount() {
        return mlist.size();
    }

    @Override
    public void onBindViewHolder(viewholderA holder, int position) {

        //设置item中的数据
       holder.mTextView.setText(mlist.get(position));

    }

    class viewholderA extends RecyclerView.ViewHolder{
        TextView mTextView;
        public viewholderA(View itemView) {
            super(itemView);
            mTextView=itemView.findViewById(R.id.picitem1);
        }
    }

}
