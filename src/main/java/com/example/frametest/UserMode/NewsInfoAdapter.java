package com.example.frametest.UserMode;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.example.frametest.R;
import com.example.frametest.json.NewsBean;

import java.util.List;

public class NewsInfoAdapter extends ArrayAdapter<NewsBean.ResultBean.DataBean> {
    private int resourceId;
    public NewsInfoAdapter(Context context, int textViewResourceId, List<NewsBean.ResultBean.DataBean> objects){
        super(context, textViewResourceId, objects);
        resourceId = textViewResourceId;
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        NewsBean.ResultBean.DataBean dataBean = getItem(position);
        View view =null;
        if (convertView == null){
            view = LayoutInflater.from(getContext()).inflate(resourceId, parent,false);
        } else {
            view = convertView;
        }
        TextView newsName = (TextView) view.findViewById(R.id.title_news);
        newsName.setText(dataBean.getTitle());
        return view;
    }
    public class ViewHoder{
        private TextView newsName;
    }
}
