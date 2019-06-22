package com.example.frametest.TabAdapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;


import com.example.frametest.R;
import com.example.frametest.json.NewsBean;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;

import java.util.List;

public class MyTabAdapter extends BaseAdapter {
    private List<NewsBean.ResultBean.DataBean> list;
    private Context context;
    private int IMAGE_01 =0;
    private int IMAGE_02 = 1;
    private int IMAGE_03 = 2;
    public MyTabAdapter(Context context, List<NewsBean.ResultBean.DataBean> list){
        this.context = context;
        this.list = list;
        //配置lmageloader类
        ImageLoader.getInstance().init(ImageLoaderConfiguration.createDefault(context));

    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int position) {
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getViewTypeCount() {
        return 3;
    }

    @Override
    public int getItemViewType(int position) {
        if (list.get(position).getThumbnail_pic_s() != null &&
                list.get(position).getThumbnail_pic_s02() !=null &&
                list.get(position).getThumbnail_pic_s03() !=null){
            return IMAGE_03;
        }else if (list.get(position).getThumbnail_pic_s() !=null &&
                list.get(position).getThumbnail_pic_s02() !=null){
            return IMAGE_02;
        }
        return IMAGE_01;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (getItemViewType(position) == IMAGE_01){
            Image01_ViewHolder holder;
            if (convertView == null){
                convertView =View.inflate(context, R.layout.item_layout01,null);
                holder =new Image01_ViewHolder();

                //查找控件
                holder.author_name = (TextView) convertView.findViewById(R.id.author_name);
                holder.title = (TextView) convertView.findViewById(R.id.title);
                holder.image = (ImageView) convertView.findViewById(R.id.image);
                convertView.setTag(holder);
            }else {
                holder = (Image01_ViewHolder) convertView.getTag();
            }

            //获取数据重新赋值
            holder.title.setText(list.get(position).getTitle());
            holder.author_name.setText(list.get(position).getAuthor_name());
            ImageLoader.getInstance().displayImage(list.get(position).getThumbnail_pic_s(),holder.image,getOption());
        }else if (getItemViewType(position) == IMAGE_02){
            Image02_ViewHolder holder;
            if (convertView == null){
                convertView =View.inflate(context, R.layout.item_layout02,null);
                holder =new Image02_ViewHolder();

                //查找控件
                holder.image002 = (ImageView) convertView.findViewById(R.id.image002);
                holder.image001 = (ImageView) convertView.findViewById(R.id.image001);
                holder.title = (TextView) convertView.findViewById(R.id.title);
                convertView.setTag(holder);
            }else {
                holder = (Image02_ViewHolder) convertView.getTag();
            }

            //获取数据重新赋值
            holder.title.setText(list.get(position).getTitle());
            ImageLoader.getInstance().displayImage(list.get(position).getThumbnail_pic_s(),holder.image001,getOption());
            ImageLoader.getInstance().displayImage(list.get(position).getThumbnail_pic_s02(),holder.image002,getOption());
        } else {
            Image03_ViewHolder holder;
            if (convertView == null){
                convertView =View.inflate(context, R.layout.item_layout03,null);
                holder =new Image03_ViewHolder();

                //查找控件
                holder.image01 = (ImageView) convertView.findViewById(R.id.image01);
                holder.image02 = (ImageView) convertView.findViewById(R.id.image02);
                holder.image03 = (ImageView) convertView.findViewById(R.id.image03);
                holder.title = (TextView) convertView.findViewById(R.id.title);
                convertView.setTag(holder);
            }else {
                holder = (Image03_ViewHolder) convertView.getTag();
            }

            //获取数据重新赋值
            holder.title.setText(list.get(position).getTitle());
            ImageLoader.getInstance().displayImage(list.get(position).getThumbnail_pic_s(),holder.image01,getOption());
            ImageLoader.getInstance().displayImage(list.get(position).getThumbnail_pic_s02(),holder.image02,getOption());
            ImageLoader.getInstance().displayImage(list.get(position).getThumbnail_pic_s03(),holder.image03,getOption());
        }
        return convertView;
    }
    //配置图片加载失败和加载中显示的Android小机器人logo
    public static DisplayImageOptions getOption() {
        DisplayImageOptions options = new DisplayImageOptions.Builder()
                .showImageOnLoading(R.mipmap.ic_launcher) // 设置图片下载期间显示的图片
                .showImageForEmptyUri(R.mipmap.ic_launcher) // 设置图片Uri为空或是错误的时候显示的图片
                .showImageOnFail(R.mipmap.ic_launcher) // 设置图片加载或解码过程中发生错误显示的图片
                .resetViewBeforeLoading(true)  // default 设置图片在加载前是否重置、复位
                .delayBeforeLoading(1000)  // 下载前的延迟时间
                .cacheInMemory(true) // default  设置下载的图片是否缓存在内存中
                .cacheOnDisk(true) // default  设置下载的图片是否缓存在SD卡中
                .imageScaleType(ImageScaleType.EXACTLY_STRETCHED) // default 设置图片以如何的编码方式显示
                .bitmapConfig(Bitmap.Config.RGB_565) // default 设置图片的解码类型
                .build();

        return options;
    }

    static  class  Image01_ViewHolder{
        TextView title,author_name;
        ImageView image;
    }
    static  class  Image02_ViewHolder{
        TextView title;
        ImageView image001,image002;
    }
    static  class  Image03_ViewHolder{
        TextView title;
        ImageView image01,image02,image03;
    }
}