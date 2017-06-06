package com.jeff.cacheclear.dischangecounter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

/**
 * Created by 小太阳jeff on 2017/6/4.
 */

public class TrafficInfoAdapter extends BaseAdapter{
    private Context mContext;
    private List<AppTrafficInfo> infos;
    private LayoutInflater mInflater;

    public TrafficInfoAdapter(Context context, List<AppTrafficInfo> infos) {
        this.mContext=context;
        this.infos=infos;
        this.mInflater=LayoutInflater.from(context);
    }

    @Override

    public int getCount() {
        return infos.size();
    }

    @Override
    public Object getItem(int position) {
        return position;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;
        if (convertView == null) {
            holder=new ViewHolder();
            convertView = mInflater.inflate(R.layout.item_traffic_info, null);
            holder.icon = (ImageView)convertView.findViewById(R.id.iv_icon);
            holder.name = (TextView)convertView.findViewById(R.id.tv_name);
            holder.trafficinfo = (TextView)convertView.findViewById(R.id.tv_app_traffic);
            convertView.setTag(holder);
        }else {
            holder = (ViewHolder)convertView.getTag();
        }
        holder.icon.setImageDrawable(infos.get(position).getIcon());
        holder.name.setText(infos.get(position).getName()+" uid="+infos.get(position).getUid());
        int tx= (int) (infos.get(position).getTx()/1024/1024);
        int rx= (int) (infos.get(position).getRx()/1024/1024);
        holder.trafficinfo.setText("发送数据："+tx+"MB 接收数据："+rx+"MB");

        return convertView;
    }

    public final class ViewHolder{
        public ImageView icon;
        public TextView name;
        public TextView trafficinfo;
    }

}
