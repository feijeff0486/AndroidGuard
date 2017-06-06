package com.jeff.cacheclear.cacheclear;

import android.content.Context;
import android.text.format.Formatter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

/**
 * Created by 小太阳jeff on 2017/6/5.
 */

public class AppCacheInfoAdapter extends BaseAdapter {
    private Context mContext;
    private List<AppCacheInfo> mInfos;
    private LayoutInflater mInflater;
    private static final String TAG = "AppCacheInfoAdapter";
    public AppCacheInfoAdapter(Context context,List<AppCacheInfo> infos) {
        this.mContext=context;
        this.mInflater=LayoutInflater.from(context);
        this.mInfos=infos;
    }

    @Override
    public int getCount() {
        Log.d(TAG, "getCount: mInfos.size()="+mInfos.size());
        return mInfos.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view;
        ViewHolder holder;
        if (convertView != null) {
            view = convertView;
            holder = (ViewHolder) view.getTag();
            Log.d(TAG, "getView: 复用缓存 " + position);
        } else {
            view = View.inflate(mContext, R.layout.item_app_cache_info, null);
            holder = new ViewHolder();
            holder.iv_icon = (ImageView) view.findViewById(R.id.iv_app_icon);
            holder.tv_cache = (TextView) view.findViewById(R.id.tv_app_cache);
            holder.tv_name = (TextView) view.findViewById(R.id.tv_app_name);
            holder.tv_size = (TextView) view.findViewById(R.id.tv_app_size);
            view.setTag(holder);
            Log.d(TAG, "getView: 创建新的View对象 " + position);
        }
        holder.iv_icon.setImageDrawable(mInfos.get(position).getIcon());
        holder.tv_name.setText(mInfos.get(position).getName());
        holder.tv_size.setText("应用大小："+Formatter.formatFileSize(mContext,mInfos.get(position).getCode()));
        holder.tv_cache.setText("缓存大小："+
                Formatter.formatFileSize(mContext,mInfos.get(position).getCache())+
                "  数据大小："+Formatter.formatFileSize(mContext,mInfos.get(position).getData()));

        return view;
    }

    class ViewHolder {
        ImageView iv_icon;
        TextView tv_name;
        TextView tv_cache;
        TextView tv_size;
    }
}


