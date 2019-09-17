package com.example.frametest;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import com.example.frametest.json.NewsBean;
import com.example.frametest.TabAdapter.MyTabAdapter;
import com.example.frametest.tools.DBOpenHelper;
import com.example.frametest.tools.DialogUtil;
import com.google.gson.Gson;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class NewsFragment extends Fragment {
    private FloatingActionButton fab;
    private ListView listView;
    private SwipeRefreshLayout swipeRefreshLayout;
    private List<NewsBean.ResultBean.DataBean> list;
    private static final int UPNEWS_INSERT = 0;
    private int page =0,row =10;
    private static final int SELECT_REFLSH = 1;
    String  responseDate;
    Dialog mDialog;
    @SuppressLint("HandlerLeak")
    private Handler newsHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            String uniquekey,title,date, category,author_name,url,thumbnail_pic_s,thumbnail_pic_s02,thumbnail_pic_s03;
            switch (msg.what){
                case UPNEWS_INSERT:
                    list = ((NewsBean) msg.obj).getResult().getData();
                    MyTabAdapter adapter = new MyTabAdapter(getActivity(),list);
                    listView.setAdapter(adapter);
                    adapter.notifyDataSetChanged();

                    break;
                case SELECT_REFLSH:
                    list =((NewsBean) msg.obj).getResult().getData();
                    MyTabAdapter myTabAdapter = new MyTabAdapter(getActivity(),list);
                    listView.setAdapter(myTabAdapter);
                    myTabAdapter.notifyDataSetChanged();
                    if (swipeRefreshLayout.isRefreshing()){
                        swipeRefreshLayout.setRefreshing(false);//设置不刷新
                        DialogUtil.closeDialog(mDialog);
                    }
                    break;
                    default:
            }
        }
    };
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.list_item,container,false);
        listView = (ListView) view.findViewById(R.id.listView);
        fab = (FloatingActionButton) view.findViewById(R.id.fab);
        swipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipe_refresh);
        return view;
    }
    @SuppressLint("HandlerLeak")
    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        onAttach(getActivity());
        //获取传递的值
        Bundle bundle = getArguments();
        final String data = bundle.getString("name","top");
        //置顶功能
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listView.smoothScrollToPosition(0);
            }
        });
        //下拉刷新
        swipeRefreshLayout.setColorSchemeResources(R.color.colorRed);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                page++;

                      // 下一步实现从数据库中读取数据刷新到listview适配器中
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                               NewsBean newsBean = new NewsBean();
                               List<NewsBean.ResultBean.DataBean> dataBeanList = new ArrayList<>();
                                Connection conn = null;
                                conn = (Connection) DBOpenHelper.getConn();
                                int pages = (page -1)*row;
                                String sql = "select *from news_info limit "+pages+","+row;
                                PreparedStatement pst;
                                try {
                                    pst = (PreparedStatement) conn.prepareStatement(sql);
                                    ResultSet rs = pst.executeQuery();
                                    while (rs.next()){
                                        NewsBean.ResultBean.DataBean dataBean = new NewsBean.ResultBean.DataBean();
                                        dataBean.setUniquekey(rs.getString(1));
                                        dataBean.setTitle(rs.getString(2));
                                        dataBean.setDate(rs.getString(3));
                                        dataBean.setCategory(rs.getString(4));
                                        dataBean.setAuthor_name(rs.getString(5));
                                        dataBean.setUrl(rs.getString(6));
                                        dataBean.setThumbnail_pic_s(rs.getString(7));
                                        dataBean.setThumbnail_pic_s02(rs.getString(8));
                                        dataBean.setThumbnail_pic_s03(rs.getString(9));
                                        dataBeanList.add(dataBean);
                                    }
                                    newsBean.setResult(new NewsBean.ResultBean());
                                    newsBean.getResult().setData(dataBeanList);
                                    pst.close();
                                    conn.close();
                                } catch (SQLException e) {
                                    e.printStackTrace();
                                }
                                Message msg = newsHandler.obtainMessage();
                                msg.what = SELECT_REFLSH;
                                msg.obj = newsBean;
                                newsHandler.sendMessage(msg);
                            }
                        }).start();
                        mDialog = DialogUtil.createLoadingDialog(getActivity(),"加载中...");
                    }
        });

        //异步加载数据
        getDataFromNet(data);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //获取点击条目的路径，传值显示webview页面
                String url = list.get(position).getUrl();
                String uniquekey = list.get(position).getUniquekey();
                final NewsBean.ResultBean.DataBean dataBean = (NewsBean.ResultBean.DataBean) list.get(position);
                //这里是在listview子item的点击事件中添加一个插入新闻的具体json数据到news_info表中
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        Connection conn = null;
                        conn = (Connection) DBOpenHelper.getConn();
                        System.out.print(conn);
                        String sql = "insert into news_info(uniquekey,title,date,category,author_name,url,thumbnail_pic_s,thumbnail_pic_s02,thumbnail_pic_s03) values(?,?,?,?,?,?,?,?,?)";
                        int i = 0;
                        PreparedStatement pstmt;
                        try {
                            pstmt = (PreparedStatement) conn.prepareStatement(sql);
                            pstmt.setString(1,dataBean.getUniquekey());
                            pstmt.setString(2,dataBean.getTitle());
                            pstmt.setString(3,dataBean.getDate());
                            pstmt.setString(4,dataBean.getCategory());
                            pstmt.setString(5,dataBean.getAuthor_name());
                            pstmt.setString(6,dataBean.getUrl());
                            pstmt.setString(7,dataBean.getThumbnail_pic_s());
                            pstmt.setString(8,dataBean.getThumbnail_pic_s02());
                            pstmt.setString(9,dataBean.getThumbnail_pic_s03());
                            i = pstmt.executeUpdate();
                            pstmt.close();
                            conn.close();
                        } catch (SQLException e) {
                            e.printStackTrace();
                        }
                    }
                }).start();
                Intent intent = new Intent(getActivity(),WebActivity.class);
                intent.putExtra("url",url);
                intent.putExtra("uniquekey",uniquekey);
                startActivity(intent);

            }
        });
    }
    private void getDataFromNet(final String data){
        final String path = "http://v.juhe.cn/toutiao/index?type="+data+"&key=547ee75ef186fc55a8f015e38dcfdb9a";
        new Thread(new Runnable() {
            @Override
            public void run() {
                OkHttpClient okHttpClient = new OkHttpClient();
                Request request = new Request.Builder()
                        .url(path)
                        .build();
                try {
                    Response response = okHttpClient.newCall(request).execute();
                    responseDate = response.body().string();
                    NewsBean newsBean = new Gson().fromJson(responseDate, NewsBean.class);
                    if ("10012".equals("" + newsBean.getError_code())) {
                        List<NewsBean.ResultBean.DataBean> listDataBean = new ArrayList<>();
                        Connection conn = null;
                        conn = (Connection) DBOpenHelper.getConn();
                        String sql = "select * from news_info ";
                        PreparedStatement pstmt;
                        pstmt = (PreparedStatement) conn.prepareStatement(sql);
                        ResultSet rs = pstmt.executeQuery();
                        while (rs.next()) {
                            NewsBean.ResultBean.DataBean dataBean = new NewsBean.ResultBean.DataBean();
                            dataBean.setUniquekey(rs.getString(1));
                            dataBean.setTitle(rs.getString(2));
                            dataBean.setDate(rs.getString(3));
                            dataBean.setCategory(rs.getString(4));
                            dataBean.setAuthor_name(rs.getString(5));
                            dataBean.setUrl(rs.getString(6));
                            dataBean.setThumbnail_pic_s(rs.getString(7));
                            dataBean.setThumbnail_pic_s02(rs.getString(8));
                            dataBean.setThumbnail_pic_s03(rs.getString(9));
                            listDataBean.add(dataBean);
                        }
                        newsBean.setResult(new NewsBean.ResultBean());
                        newsBean.getResult().setData(listDataBean);
                        pstmt.close();
                        conn.close();
                    }
                    Message msg = newsHandler.obtainMessage();
                    msg.what = UPNEWS_INSERT;
                    msg.obj = newsBean;
                    newsHandler.sendMessage(msg);
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (SQLException e) {
                    e.printStackTrace();
                }

            }

        }).start();
    }

    private String streamToString(InputStream inputStream, String charset){
        try {
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream,charset);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            String s = null;
            StringBuilder builder = new StringBuilder();
            while ((s = bufferedReader.readLine()) != null){
                builder.append(s);
            }
            bufferedReader.close();
            return builder.toString();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;

    }

}
