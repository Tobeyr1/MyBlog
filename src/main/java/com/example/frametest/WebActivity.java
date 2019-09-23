package com.example.frametest;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.http.SslError;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.ActionBar;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.SslErrorHandler;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.Toast;
import com.example.frametest.UserMode.LoginActivity;
import com.example.frametest.UserMode.NewsInfoAdapter;
import com.example.frametest.UserMode.UserFavoriteActivity;
import com.example.frametest.json.NewsBean;
import com.example.frametest.tools.BasicActivity;
import com.example.frametest.tools.DBOpenHelper;
import com.example.frametest.tools.DialogUtil;
import com.example.frametest.tools.MyApplication;
import com.example.frametest.tools.ToastUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class WebActivity extends BasicActivity {
    private WebView webView;
    private Toolbar toolbar,ltoolBar;
    private final  static int SEARCH_MOHU =1;
    String url,user_phonenumber;
    private boolean flags=true;
    private Dialog mDialog;
    private List<NewsBean.ResultBean.DataBean> newList = new ArrayList<>();
    private ListView listView;
    @SuppressLint("HandlerLeak")
    private Handler searchHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
           switch (msg.what){
               case SEARCH_MOHU:
                   NewsInfoAdapter adapter = new NewsInfoAdapter(WebActivity.this,R.layout.item_layout_news,newList);
                   listView.setVisibility(View.VISIBLE);
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
        setContentView(R.layout.activity_web);
        //获取传递的路径
        webView = (WebView) findViewById(R.id.webView);
        toolbar = (Toolbar) findViewById(R.id.toolbar_webview);
        ltoolBar = (Toolbar) findViewById(R.id.toolbar_webcomment);
        listView = (ListView) findViewById(R.id.list_view);
        findViewById(R.id.toolbar_webcomment).bringToFront();
    }

    @Override
    protected void onStart() {
        super.onStart();
        url = getIntent().getStringExtra("url");
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                NewsBean.ResultBean.DataBean dataBean = newList.get(position);
                String url = dataBean.getUrl();
                Intent intent = new Intent(getApplicationContext(),WebActivity.class);
                intent.putExtra("url",url);
                startActivity(intent);
            }
        });
        //显示JavaScript页面
        WebSettings settings = webView.getSettings();
        webView.setWebViewClient(new WebViewClient(){
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                mDialog = DialogUtil.createLoadingDialog(WebActivity.this,"加载中...");
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view,url );
                view.loadUrl("javascript:function setTop(){document.querySelector('body > div.top-wrap.gg-item.J-gg-item').style.display=\"none\";}setTop();");
                DialogUtil.closeDialog(mDialog);
            }


            public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error){

//handler.cancel(); 默认的处理方式，WebView变成空白页
                handler.proceed();

//handleMessage(Message msg); 其他处理
            }

        });
        settings.setJavaScriptEnabled(true);
        settings.setSupportZoom(true); //支持缩放，默认为true。是下面那个的前提。
        settings.setBuiltInZoomControls(true); //设置内置的缩放控件。若为false，则该WebView不可缩放
        settings.setJavaScriptCanOpenWindowsAutomatically(true);
        settings.setUseWideViewPort(true);
        settings.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN);
        settings.setLoadWithOverviewMode(true);
        /*settings.setDisplayZoomControls(false);*/
        webView.loadUrl(url);

        setSupportActionBar(ltoolBar);
        toolbar.setTitle("简易新闻");
        setSupportActionBar(toolbar);
        ltoolBar.inflateMenu(R.menu.tool_webbottom);
        ltoolBar.setTitle("感谢观看");
        ltoolBar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                switch (menuItem.getItemId()){
                    case R.id.news_share:
                        Intent intent = new Intent(Intent.ACTION_SEND);
                        intent.putExtra(Intent.EXTRA_SUBJECT,url);
                        intent.setType("text/plain");
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(Intent.createChooser(intent,getTitle()));
                        break;
                    case R.id.news_collect:
                        //下一步实现点击收藏功能，以及用户查看收藏功能
                      user_phonenumber = MyApplication.getMoublefhoneUser();
                        if (user_phonenumber != null){
                            if (flags){
                            flags = !flags;
                            Toast.makeText(WebActivity.this,"收藏成功",Toast.LENGTH_SHORT).show();
                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    Connection conn = null;
                                    conn = (Connection) DBOpenHelper.getConn();
                                    String uniquekey = getIntent().getStringExtra("uniquekey");
                                    String sql = "insert into user_collect(user_phone,news_id) values(?,?)";
                                    int i = 0;
                                    PreparedStatement pstmt;
                                    try {
                                        pstmt = (PreparedStatement) conn.prepareStatement(sql);
                                        pstmt.setString(1,user_phonenumber);
                                        pstmt.setString(2,uniquekey);
                                        i = pstmt.executeUpdate();
                                        pstmt.close();
                                        conn.close();
                                    } catch (SQLException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }).start();
                            }else {
                                ToastUtil.showShortToastCenter(WebActivity.this,"您已经收藏过啦");
                            }
                        } else {
                            Intent exitIntent = new Intent(WebActivity.this,LoginActivity.class);
                            exitIntent.setFlags(Intent.FLAG_ACTIVITY_NO_USER_ACTION);
                            startActivity(exitIntent);
                        }
                        break;
                }
                return true;
            }
        });
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null){
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeAsUpIndicator(R.drawable.ic_chevron_left);
        }
    }

    @SuppressLint("NewApi")
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_webview,menu);
        final SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView) menu.findItem(R.id.news_search).getActionView();
        searchView.setSubmitButtonEnabled(true);
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(final String query) {
                if (!TextUtils.isEmpty(query)){
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            NewsBean newsBean = new NewsBean();
                            Connection conn = null;
                            conn = DBOpenHelper.getConn();
                            String sql ="select title,url from news_info where  match(title,category) AGAINST ('"+query+"' IN BOOLEAN MODE )";
                            PreparedStatement pst;
                            try {
                                pst =(PreparedStatement) conn.prepareStatement(sql);
                                ResultSet rs = pst.executeQuery();
                                while (rs.next()){
                                    NewsBean.ResultBean.DataBean dataBean = new NewsBean.ResultBean.DataBean();
                                    dataBean.setTitle(rs.getString(1));
                                    dataBean.setUrl(rs.getString(2));
                                    newList.add(dataBean);
                                }
                                pst.close();
                                conn.close();
                            } catch (SQLException e) {
                                e.printStackTrace();
                            }
                            Message msg = searchHandler.obtainMessage();
                            msg.what = SEARCH_MOHU;
                            searchHandler.sendMessage(msg);
                        }

                    }).start();
                    mDialog = DialogUtil.createLoadingDialog(WebActivity.this,"加载中...");
                }

                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                if (listView.getVisibility() ==View.VISIBLE){
                    listView.setVisibility(View.INVISIBLE);
                }else {
                    Intent returnIntent = new Intent();
                    WebActivity.this.finish();
                }
                break;
            case R.id.news_setting:
                Toast.makeText(this,"夜间模式",Toast.LENGTH_SHORT).show();
                break;
            case R.id.news_feedback:
                break;
            default:
                break;
        }
        return true;
    }
}
