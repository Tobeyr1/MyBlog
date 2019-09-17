package com.example.frametest;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.bumptech.glide.Glide;
import com.cazaea.sweetalert.SweetAlertDialog;
import com.example.frametest.UserMode.LoginActivity;
import com.example.frametest.UserMode.User;
import com.example.frametest.UserMode.UserFavoriteActivity;
import com.example.frametest.UserMode.User_DataActivity;
import com.example.frametest.UserMode.User_LogoutActivity;
import com.example.frametest.tools.ActivityCollector;
import com.example.frametest.tools.BasicActivity;
import com.example.frametest.tools.DBOpenHelper;
import com.example.frametest.tools.MyApplication;
import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import de.hdodenhof.circleimageview.CircleImageView;
import interfaces.heweather.com.interfacesmodule.bean.Lang;
import interfaces.heweather.com.interfacesmodule.bean.Unit;
import interfaces.heweather.com.interfacesmodule.bean.air.now.AirNow;
import interfaces.heweather.com.interfacesmodule.bean.weather.now.Now;
import interfaces.heweather.com.interfacesmodule.view.HeWeather;

public class MainActivity extends BasicActivity {
    private android.support.v7.widget.Toolbar toolbar;
    private DrawerLayout mDrawerLayout;
    private NavigationView navigationView;
    private TabLayout tabLayout;
    private ViewPager viewPager;
    private List<String> list;
    private TextView tvhuoqu,tvName;
    String phonenumber,userName;
    private static final int USER_LOOK_NAME = 0;
    private static final int USER_FEEDBACK = 1;
    private static final int USER_ISNULL = 2;
    private static boolean mBackKeyPressed = false;//记录是否有首次按键
    private TextView tv_tianqi,tv_kongqi,tv_airqlty;
    ImageView image_weather,image_exit;
    @SuppressLint("HandlerLeak")
    private Handler userFeedHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            String admin_title,admin_url,user_name;
            switch (msg.what){
                case USER_LOOK_NAME:
                    User user = (User) msg.obj;
                    user_name =user.getUser_name();
                    tvName = (TextView) findViewById(R.id.text_username);
                    tvName.setText(user_name);
                    break;
                case USER_FEEDBACK:
                    Toast.makeText(MainActivity.this,"反馈成功",Toast.LENGTH_SHORT).show();
                    break;
                case USER_ISNULL:
                    Toast.makeText(MainActivity.this,"用户未登录！",Toast.LENGTH_SHORT).show();
                    break;
                    default:
                        break;
            }
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        toolbar =  findViewById(R.id.toolbar);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout); //获取抽屉布局
        navigationView = (NavigationView) findViewById(R.id.nav_design);//获取菜单控件实例
        View v = navigationView.getHeaderView(0);
        CircleImageView circleImageView =(CircleImageView) v.findViewById(R.id.icon_image);
        tabLayout = (TabLayout) findViewById(R.id.tabLayout);
        viewPager = (ViewPager) findViewById(R.id.viewPager);
        list = new ArrayList<>();
        tvhuoqu = (TextView) findViewById(R.id.text_huoqu);
        tv_tianqi =(TextView) findViewById(R.id.tv_tianqi);
          tv_kongqi =(TextView) findViewById(R.id.tv_kongqi);
         image_weather =(ImageView) findViewById(R.id.img_weather);
         tv_airqlty =(TextView) findViewById(R.id.tv_airqlty);
    }

    @Override
    protected void onStart() {
        super.onStart();
        getWeather();
       /* toolbar.setLogo(R.drawable.icon);//设置图片logo,你可以添加自己的图片*/
        toolbar.setTitle("简易新闻");
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar !=null){
            //通过HomeAsUp来让导航按钮显示出来
            actionBar.setDisplayHomeAsUpEnabled(true);
            //设置Indicator来添加一个点击图标
            actionBar.setHomeAsUpIndicator(R.drawable.ic_menu_24dp);
        }
        navigationView.setCheckedItem(R.id.nav_call);//设置第一个默认选中
        navigationView.setNavigationItemSelectedListener(new  NavigationView.OnNavigationItemSelectedListener() {
            //设置菜单项的监听事件
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                mDrawerLayout.closeDrawers();
                switch (menuItem.getItemId()) {
                    case R.id.nav_call:
                        phonenumber = MyApplication.getMoublefhoneUser();
                        //通过判断手机号是否存在，来决定是进入编辑资料页面还是进入登陆页面
                        if (phonenumber != null){
                            Intent unIntent = new Intent(MainActivity.this,User_DataActivity.class);
                            startActivity(unIntent);
                        } else {
                            Intent exitIntent = new Intent(MainActivity.this,LoginActivity.class);
                            startActivity(exitIntent);
                        }
                        break;
                    case R.id.nav_friends:
                        //
                        break;
                    case R.id.nav_location:
                        Toast.makeText(MainActivity.this, "你点击了发布新闻，下步实现", Toast.LENGTH_SHORT).show();
                        break;
                    case R.id.nav_favorite:
                        phonenumber = MyApplication.getMoublefhoneUser();
                        if (phonenumber != null){
                            Intent userFavIntent = new Intent(MainActivity.this,UserFavoriteActivity.class);
                            startActivity(userFavIntent);
                        } else {
                            Intent exitIntent = new Intent(MainActivity.this,LoginActivity.class);
                            startActivity(exitIntent);
                        }
                        break;
                    case R.id.nav_settings:
                        Intent logoutIntent = new Intent(MainActivity.this,User_LogoutActivity.class);
                        startActivity(logoutIntent);
                        Toast.makeText(MainActivity.this,"需要做出注销功能，可扩展夜间模式，离线模式等,检查更新",Toast.LENGTH_LONG).show();
                        break;
                    case R.id.nav_exit:
                        Intent intent = new Intent(MainActivity.this,LoginActivity.class);
                        startActivity(intent);
                        break;
                    default:
                }
                return true;
            }
        });
        list.add("头条");
        list.add("社会");
        list.add("国内");
        list.add("国际");
        list.add("娱乐");
        list.add("体育");
        list.add("军事");
        list.add("科技");
        list.add("财经");
       /* viewPager.setOffscreenPageLimit(1);*/
        viewPager.setAdapter(new FragmentStatePagerAdapter(getSupportFragmentManager()) {
            //得到当前页的标题，也就是设置当前页面显示的标题是tabLayout对应标题

            @Nullable
            @Override
            public CharSequence getPageTitle(int position) {
                return list.get(position);
            }
            @Override
            public Fragment getItem(int position) {
                NewsFragment newsFragment = new NewsFragment();
                //判断所选的标题，进行传值显示
                Bundle bundle = new Bundle();
                if (list.get(position).equals("头条")){
                    bundle.putString("name","top");
                }else if (list.get(position).equals("社会")){
                    bundle.putString("name","shehui");
                }else if (list.get(position).equals("国内")){
                    bundle.putString("name","guonei");
                }else if (list.get(position).equals("国际")){
                    bundle.putString("name","guoji");
                }else if (list.get(position).equals("娱乐")){
                    bundle.putString("name","yule");
                }else if (list.get(position).equals("体育")){
                    bundle.putString("name","tiyu");
                }else if (list.get(position).equals("军事")){
                    bundle.putString("name","junshi");
                }else if (list.get(position).equals("科技")){
                    bundle.putString("name","keji");
                }else if (list.get(position).equals("财经")){
                    bundle.putString("name","caijing");
                }else if (list.get(position).equals("时尚")){
                    bundle.putString("name","shishang");
                }
                newsFragment.setArguments(bundle);
                return newsFragment;
            }

            @NonNull
            @Override
            public Object instantiateItem(@NonNull ViewGroup container, int position) {
                NewsFragment newsFragment = (NewsFragment)  super.instantiateItem(container, position);

                return newsFragment;
            }

            @Override
            public int getItemPosition(@NonNull Object object) {
                return FragmentStatePagerAdapter.POSITION_NONE;
            }

            @Override
            public int getCount() {
                return list.size();
            }
        });
        //TabLayout要与ViewPAger关联显示
        tabLayout.setupWithViewPager(viewPager);
        String inputText = load();
        if (!TextUtils.isEmpty(inputText)){
            System.out.println("________)))))))");
            System.out.println("________)))))))");
            System.out.println(phonenumber);
            phonenumber =inputText;
            MyApplication.setMoublefhoneUser(phonenumber);
        }



    }

    private void getWeather() {
        /**
         * 实况天气
         * 实况天气即为当前时间点的天气状况以及温湿风压等气象指数，具体包含的数据：体感温度、
         * 实测温度、天气状况、风力、风速、风向、相对湿度、大气压强、降水量、能见度等。
         *
         * @param context  上下文
         * @param location 地址详解
         * @param lang       多语言，默认为简体中文
         * @param unit        单位选择，公制（m）或英制（i），默认为公制单位
         * @param listener  网络访问回调接口
         */
        HeWeather.getWeatherNow(MainActivity.this, "CN101190101",  Lang.CHINESE_SIMPLIFIED , Unit.METRIC , new HeWeather.OnResultWeatherNowBeanListener() {
            public static final String TAG="he_feng_now";
            @Override
            public void onError(Throwable e) {
                Log.i(TAG, "onError: ", e);
                System.out.println("Weather Now Error:"+new Gson());
            }

            @Override
            public void onSuccess(Now dataObject) {
                Log.i(TAG, " Weather Now onSuccess: " + new Gson().toJson(dataObject));
                String jsonData = new Gson().toJson(dataObject);
                System.out.println("返回的数据内容："+dataObject.getStatus());
                String tianqi = null,wendu = null, tianqicode = null;
                if (dataObject.getStatus().equals("ok")){
                    String JsonNow = new Gson().toJson(dataObject.getNow());
                    JSONObject jsonObject = null;
                    try {
                        jsonObject = new JSONObject(JsonNow);
                        tianqi = jsonObject.getString("cond_txt");
                        wendu = jsonObject.getString("tmp");
                        tianqicode = jsonObject.getString("cond_code");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }else {
                    Toast.makeText(MainActivity.this,"有错误",Toast.LENGTH_SHORT).show();
                    return;
                }
                String wendu2 = wendu +"℃";
                tv_tianqi.setText(tianqi);
                tv_kongqi.setText(wendu2);
                String tagurl = "https://cdn.heweather.com/cond_icon/" +tianqicode+".png";
                Glide.with(MainActivity.this).load(tagurl).into(image_weather);
            }
        });
        HeWeather.getAirNow(MainActivity.this, "CN101190101", Lang.CHINESE_SIMPLIFIED, Unit.METRIC, new HeWeather.OnResultAirNowBeansListener() {
            public static final String TAG2="he_feng_air";
            @Override
            public void onError(Throwable throwable) {
                Log.i(TAG2,"ERROR IS:",throwable);
            }

            @Override
            public void onSuccess(AirNow airNow) {
                Log.i(TAG2,"Air Now onSuccess:"+new Gson().toJson(airNow));
                String airStatus = airNow.getStatus();
                if (airStatus.equals("ok")){
                    String jsonData = new Gson().toJson(airNow.getAir_now_city());
                    String aqi = null,qlty = null;
                    JSONObject objectAir = null;
                    try {
                        objectAir = new JSONObject(jsonData);
                        aqi = objectAir.getString("aqi");
                        qlty = objectAir.getString("qlty");
                        tv_airqlty.setText(qlty+"("+aqi+")");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }else {
                    Toast.makeText(MainActivity.this,"有错误",Toast.LENGTH_SHORT).show();
                    return;
                }
            }

        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //获取toolbar菜单项
        getMenuInflater().inflate(R.menu.toolbar,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            //R.id.home修改导航按钮的点击事件为打开侧滑栏
            case android.R.id.home:
                if (MyApplication.getInstance().getMoublefhoneUser() != null){
                    phonenumber = MyApplication.getInstance().getMoublefhoneUser();
                }
                mDrawerLayout.openDrawer(GravityCompat.START);  //打开侧滑栏
                tvhuoqu = (TextView) findViewById(R.id.text_huoqu);
                tvhuoqu.setText(phonenumber);
                //用户开启侧滑栏时，查询数据库对应手机号的用户名，并显示在侧滑栏头部
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        Connection conn = null;
                        conn = (Connection) DBOpenHelper.getConn();
                        String sql = "select user_name from user_info where  user_phone ='"+phonenumber+"'";
                        Statement pstmt;
                        try {
                            pstmt = (Statement) conn.createStatement();
                            ResultSet rs = pstmt.executeQuery(sql);
                            while (rs.next()){
                                User user = new User();
                                user.setUser_name(rs.getString(1));
                                //此处优化方法，去掉以前的new Message()这样会不断地新增一个Handle增加内存空间响应时间
                                //
                                Message msg = userFeedHandler.obtainMessage();
                                msg.what=USER_LOOK_NAME;
                                msg.obj = user;
                                userFeedHandler.sendMessage(msg);
                            }
                            pstmt.close();
                            conn.close();
                        } catch (SQLException e) {
                            e.printStackTrace();
                        }
                    }
                }).start();
                break;
            case R.id.userFeedback:
                new MaterialDialog.Builder(MainActivity.this)
                        .title("意见反馈")
                        .inputRangeRes(2,20,R.color.yellow)
                        .inputType(InputType.TYPE_CLASS_TEXT)
                        .input("请输入反馈信息", null, new MaterialDialog.InputCallback() {
                            @Override
                            public void onInput(@NonNull MaterialDialog dialog, CharSequence input) {
                                new Thread(new Runnable() {
                                    @Override
                                    public void run() {
                                        String input_text = input.toString();
                                        if ("".equals(MyApplication.getMoublefhoneUser()) || MyApplication.getMoublefhoneUser() == null) {
                                            Message msg = Message.obtain();
                                            msg.what =USER_ISNULL;
                                            userFeedHandler.sendMessage(msg);
                                        } else if ("".equals(input_text) || input_text == null) {
                                            Message msg = Message.obtain();
                                            msg.what =USER_FEEDBACK;
                                            userFeedHandler.sendMessage(msg);
                                        }else {
                                            Connection conn = null;
                                            conn = (Connection) DBOpenHelper.getConn();
                                            String sql = "insert into user_feedback(user_feed,user_phone) values(?,?)";
                                            int i = 0;
                                            PreparedStatement pstmt;
                                            try {
                                                pstmt = (PreparedStatement) conn.prepareStatement(sql);
                                                pstmt.setString(1, input_text);
                                                pstmt.setString(2,MyApplication.getMoublefhoneUser());
                                                i = pstmt.executeUpdate();
                                                pstmt.close();
                                                conn.close();
                                            } catch (SQLException e) {
                                                e.printStackTrace();
                                            }
                                            Message msg = Message.obtain();
                                            msg.what =USER_FEEDBACK;
                                            userFeedHandler.sendMessage(msg);
                                        }
                                    }

                                }).start();
                            }
                        }).positiveText("确定").negativeText("取消").show();
               /* final EditText ed =new EditText(MainActivity.this);
                AlertDialog.Builder dialog = new AlertDialog.Builder(MainActivity.this);
                dialog.setTitle("用户反馈");
                dialog.setView(ed);
                dialog.setCancelable(false);
                dialog.setPositiveButton("确认", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                String input_text = ed.getText().toString();
                                if ("".equals(MyApplication.getMoublefhoneUser()) || MyApplication.getMoublefhoneUser() == null) {
                                    Message msg = Message.obtain();
                                    msg.what =USER_ISNULL;
                                    userFeedHandler.sendMessage(msg);
                                } else if ("".equals(input_text) || input_text == null) {
                                    Message msg = Message.obtain();
                                    msg.what =USER_FEEDBACK;
                                    userFeedHandler.sendMessage(msg);
                                }else {
                                    Connection conn = null;
                                    conn = (Connection) DBOpenHelper.getConn();
                                    String sql = "insert into user_feedback(user_feed,user_phone) values(?,?)";
                                    int i = 0;
                                    PreparedStatement pstmt;
                                    try {
                                        pstmt = (PreparedStatement) conn.prepareStatement(sql);
                                        pstmt.setString(1, input_text);
                                        pstmt.setString(2,MyApplication.getMoublefhoneUser());
                                        i = pstmt.executeUpdate();
                                        pstmt.close();
                                        conn.close();
                                    } catch (SQLException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }
                        }).start();
                        Toast.makeText(MainActivity.this,"反馈成功",Toast.LENGTH_SHORT).show();
                    }
                });
                dialog.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });
                dialog.show();*/
                break;
            case R.id.userExit:
                final SweetAlertDialog mDialog = new SweetAlertDialog(MainActivity.this,SweetAlertDialog.NORMAL_TYPE)
                        .setTitleText("提示")
                        .setContentText("您是否要退出？")
                        .setCustomImage(null)
                        .setCancelText("取消")
                        .setConfirmText("确定")
                        .setCancelClickListener(new SweetAlertDialog.OnSweetClickListener() {
                            @Override
                            public void onClick( SweetAlertDialog sweetAlertDialog) {
                                sweetAlertDialog.dismiss();
                            }
                        }).setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                            @Override
                            public void onClick(SweetAlertDialog sweetAlertDialog) {
                                sweetAlertDialog.dismiss();
                                ActivityCollector.finishAll();
                            }
                        });
                mDialog.show();
                break;
            default:
                break;

        }
        return true;
    }
    public String load() {
        FileInputStream in = null;
        BufferedReader reader = null;
        StringBuilder content = new StringBuilder();
        try {
            in = openFileInput("data");
            System.out.println("是否读到文件内容"+in);
            reader = new BufferedReader(new InputStreamReader(in));
            String line = "";
            while ((line = reader.readLine()) != null){
                content.append(line);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            if (reader != null){
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return content.toString();
    }

    @Override
    public void onBackPressed() {
        if(!mBackKeyPressed){
            Toast.makeText(this, "再按一次退出程序", Toast.LENGTH_SHORT).show();
            mBackKeyPressed = true;
            new Timer().schedule(new TimerTask() {//延时两秒，如果超出则擦错第一次按键记录
                @Override
                public void run() {
                    mBackKeyPressed = false;
                }
            }, 2000);
        }
        else{//退出程序
            this.finish();
            System.exit(0);
        }
    }
}