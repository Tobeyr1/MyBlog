package com.example.frametest.UserMode;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.ActionBar;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import com.example.frametest.R;
import com.example.frametest.WebActivity;
import com.example.frametest.json.NewsBean;
import com.example.frametest.tools.BasicActivity;
import com.example.frametest.tools.DBOpenHelper;
import com.example.frametest.tools.DialogUtil;
import com.example.frametest.tools.MyApplication;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class UserFavoriteActivity extends BasicActivity {
    private ListView listView;
    private List<NewsBean.ResultBean.DataBean> newList = new ArrayList<>();
    NewsBean.ResultBean.DataBean dataBean;
    String phone_userfavorite;
    private static final int NewsFav_List = 6;
    private TextView textView;
    Dialog mDialog;
    @SuppressLint("HandlerLeak")
    private Handler newsFavHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case NewsFav_List:
                    NewsInfoAdapter adapter = new NewsInfoAdapter(UserFavoriteActivity.this,R.layout.item_layout_news,newList);
                    Log.d("传入数据后", String.valueOf(newList.size()));
                    listView.setAdapter(adapter);
                    adapter.notifyDataSetChanged();
                    DialogUtil.closeDialog(mDialog);
                    break;
            }
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_favorite);
        initNews();
        listView = (ListView) findViewById(R.id.list_news);
        phone_userfavorite = MyApplication.getMoublefhoneUser();
        System.out.println("收藏页面是否传值"+phone_userfavorite);
        Toolbar toolbar = (Toolbar) findViewById(R.id.userFavorite_toolbar);
        toolbar.setTitle("我的收藏");
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null){
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeAsUpIndicator(R.drawable.ic_chevron_left);
        }
    }
    private void initNews() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                Connection conn = null;
                conn = (Connection) DBOpenHelper.getConn();
                //这里优化子查询
                String sql = "select title,url from news_info LEFT JOIN user_collect on news_info.uniquekey=user_collect.news_id where user_collect.user_phone= ?";
                PreparedStatement pstmt;
                try {
                    String  num = phone_userfavorite;
                    pstmt = (PreparedStatement) conn.prepareStatement(sql);
                    pstmt.setString(1,num);
                    ResultSet rs = pstmt.executeQuery();
                    while (rs.next()){
                        rs.getString(1);
                        NewsBean.ResultBean.DataBean dataBean = new NewsBean.ResultBean.DataBean();
                        dataBean.setTitle(rs.getString(1));
                        dataBean.setUrl(rs.getString(2));
                        newList.add(dataBean);
                    }
                    pstmt.close();
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
                //此处优化
                //
                Message msg = newsFavHandler.obtainMessage();
                msg.what=NewsFav_List;
                newsFavHandler.sendMessage(msg);
            }
        }).start();
        mDialog = DialogUtil.createLoadingDialog(UserFavoriteActivity.this,"加载中...");
    }

    @SuppressLint("MissingSuperCall")
    @Override
    protected void onStart() {
        super.onStart();
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                NewsBean.ResultBean.DataBean dataBean = newList.get(position);
                String url = dataBean.getUrl();
                Intent intent = new Intent(UserFavoriteActivity.this,WebActivity.class);
                intent.putExtra("url",url);
                startActivity(intent);
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                UserFavoriteActivity.this.finish();
                break;
        }
        return true;
    }
}