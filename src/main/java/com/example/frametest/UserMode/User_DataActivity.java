package com.example.frametest.UserMode;


import android.Manifest;
import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.view.MenuItem;
import android.view.View;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.example.frametest.R;
import com.example.frametest.tools.BasicActivity;
import com.example.frametest.tools.DBOpenHelper;
import com.example.frametest.tools.DialogUtil;
import com.example.frametest.tools.MyApplication;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Calendar;

public class User_DataActivity extends BasicActivity {
    private ImageView imageView_user;
    public static final int CHOOSE_USER_TOUX =11;
    public static final int USER_SETTINGS_NAME =12;
    public static final int USER_UPDATE_NAME =13;
    public static final int USER_DATE_SELECT =14; //查询用户信息
    public static final int USER_SEX_INSERT =15;
    private TextView tv_nc_fb,text_Age,text_Sex;
    String user_setting_phone;
    String input_userName;
    Calendar calendar;
    String phone_userfavorite;
    String user_SEX;
    Dialog mDialog;
    @SuppressLint("HandlerLeak")
    private Handler userSettingsHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            String admin_title,admin_url;
            switch (msg.what){
                case USER_SETTINGS_NAME:
                    Toast.makeText(User_DataActivity.this,"用户名为空或不存在！",Toast.LENGTH_SHORT).show();
                    break;
                case USER_UPDATE_NAME:
                    tv_nc_fb.setText(input_userName);
                    break;
                //查询用户信息
                case USER_DATE_SELECT:
                    String user_name,user_age,user_sex;
                    User user =(User)msg.obj;
                    user_name =user.getUser_name();
                    user_age=user.getUser_age();
                    user_sex=user.getUser_sex();
                    DialogUtil.closeDialog(mDialog);
                    tv_nc_fb.setText(user_name);
                    text_Age.setText(user_age);
                    text_Sex.setText(user_sex);
                    break;
                case USER_SEX_INSERT:
                    text_Sex.setText(user_SEX);
                    break;
            }
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user__data);
        phone_userfavorite = MyApplication.getMoublefhoneUser();
        //初始化时首先加载用户已有的数据并将其显示出来
       initData();
        user_setting_phone = MyApplication.getMoublefhoneUser();
        Toolbar uToolbar = (Toolbar) findViewById(R.id.userData_toolbar);
        text_Age =(TextView)findViewById(R.id.text_Age);
        text_Sex =(TextView)findViewById(R.id.text_Sex);
        calendar = Calendar.getInstance();
        imageView_user = (ImageView)findViewById(R.id.imageView_user);
        tv_nc_fb = (TextView) findViewById(R.id.tv_nc_fb);
        uToolbar.setTitle("个人信息");
        setSupportActionBar(uToolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null){
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeAsUpIndicator(R.drawable.back);
        }
        InitView();
    }
    //定义布局
    LinearLayout layout_touxiang;
    LinearLayout layout_name;
    LinearLayout layout_age;
    LinearLayout layout_sex;

    private void initData() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                Connection conn = null;
                conn = (Connection) DBOpenHelper.getConn();
                //这里优化子查询
                String sql = "select user_name,user_age,user_sex from user_info where  user_phone ='"+user_setting_phone+"'";
                Statement pstmt;
                try {
                    pstmt = (Statement) conn.createStatement();
                    ResultSet rs = pstmt.executeQuery(sql);
                    while (rs.next()){
                        User user = new User();
                        user.setUser_name(rs.getString(1));
                        user.setUser_age(rs.getString(2));
                        user.setUser_sex(rs.getString(3));
                        Message msg = userSettingsHandler.obtainMessage();
                        msg.what=USER_DATE_SELECT;
                        msg.obj = user;
                        userSettingsHandler.sendMessage(msg);
                    }
                    pstmt.close();
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }).start();
        mDialog = DialogUtil.createLoadingDialog(User_DataActivity.this,"加载中...");
    }


    private void InitView() {
        layout_touxiang =(LinearLayout) findViewById(R.id.lay_touxiang);
        layout_name = (LinearLayout)findViewById(R.id.layout_name);
        layout_age = findViewById(R.id.layout_Age);
        layout_sex =findViewById(R.id.layout_sex);
        layout_touxiang.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               Toast.makeText(getApplicationContext(),"头像将放在主界面实现",Toast.LENGTH_SHORT).show();
            }
        });
        layout_name.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("ResourceAsColor")
            @Override
            public void onClick(View view) {
                new   MaterialDialog.Builder(User_DataActivity.this).title("修改昵称")
                        // danlanse的颜色代码为 #C6E2FF，可以起替换为你喜欢的颜色
                        .inputRangeRes(1,8,R.color.danlanse)
                        .inputType(InputType.TYPE_CLASS_TEXT)
                        .linkColor(R.color.danlanse)
                        .input("请输入", null, new MaterialDialog.InputCallback() {
                            @Override
                            public void onInput(@NonNull MaterialDialog dialog, CharSequence input) {
                                new Thread(new Runnable() {
                                    @Override
                                    public void run() {
                                        input_userName = input.toString();
                                        if ("".equals(input_userName) || input_userName == null) {
                                            //此处优化
                                            //
                                            Message msg = userSettingsHandler.obtainMessage();
                                            msg.what =USER_SETTINGS_NAME;
                                            userSettingsHandler.sendMessage(msg);
                                        } else {
                                            Connection conn = null;
                                            conn = (Connection) DBOpenHelper.getConn();
                                            String sql = "update user_info set user_name='"+input_userName+"' where user_phone='"+user_setting_phone+"'";
                                            int i = 0;
                                            PreparedStatement pstmt;
                                            try {
                                                pstmt = (PreparedStatement) conn.prepareStatement(sql);
                                                i = pstmt.executeUpdate();
                                                pstmt.close();
                                                conn.close();
                                                //此处优化
                                                //
                                                Message msg = userSettingsHandler.obtainMessage();
                                                msg.what = USER_UPDATE_NAME;
                                                userSettingsHandler.sendMessage(msg);
                                            } catch (SQLException e) {
                                                e.printStackTrace();
                                            }
                                        }
                                    }
                                }).start();
                            }
                        }).positiveText("确定").positiveColor(R.color.danlanse).negativeText("取消").negativeColor(R.color.black).show();
            }

        });
        layout_sex.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int [] itemId ={11,12};
                String [] contentArray = {"男","女"};
                new MaterialDialog.Builder(User_DataActivity.this)
                        .title("选择你的性别").items(contentArray).itemsIds(itemId).itemsCallbackSingleChoice(-1, new MaterialDialog.ListCallbackSingleChoice() {
                    @SuppressLint("ResourceType")
                    @Override
                    public boolean onSelection(MaterialDialog dialog, View itemView, int which, CharSequence text) {
                        new Thread(new Runnable() {
                            @Override
                            public void run() {

                                int id =itemView.getId();
                                if (id==11){
                                    user_SEX="男";
                                }else if (id==12){
                                    user_SEX="女";
                                }
                                Connection conn = null;
                                conn = (Connection) DBOpenHelper.getConn();
                                String sql = "update user_info set user_sex='"+user_SEX+"' where user_phone='"+user_setting_phone+"'";
                                int i = 0;
                                PreparedStatement pstmt;
                                try {
                                    pstmt = (PreparedStatement) conn.prepareStatement(sql);
                                    i = pstmt.executeUpdate();
                                    pstmt.close();
                                    conn.close();
                                    //此处优化
                                    //
                                    Message msg = userSettingsHandler.obtainMessage();
                                    msg.what = USER_SEX_INSERT;
                                    userSettingsHandler.sendMessage(msg);
                                } catch (SQLException e) {
                                    e.printStackTrace();
                                }
                            }
                        }).start();
                        return true;
                    };
                }).show();
            }
        });
        layout_age.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DatePickerDialog dialog = new DatePickerDialog(User_DataActivity.this, listener,
                        calendar.get(Calendar.YEAR),
                        calendar.get(Calendar.MONTH),
                        calendar.get(Calendar.DAY_OF_MONTH));
                dialog.show();
            }
        });

    }
    private DatePickerDialog.OnDateSetListener listener = new DatePickerDialog.OnDateSetListener() {

        @Override
        public void onDateSet(DatePicker datePicker, int year, int monthOfYear, int dayOfMonth) {
            String user_AGE=year+"-"+(monthOfYear + 1)+"-"+dayOfMonth;
            System.out.println("用户的出生日期："+user_AGE);
            text_Age.setText(year+"-"+(monthOfYear + 1)+"-"+dayOfMonth);
            new Thread(new Runnable() {
                @Override
                public void run() {
                    if ("".equals(user_AGE) || user_AGE == null) {
                        //此处优化
                        //
                        Message msg = userSettingsHandler.obtainMessage();
                        msg.what =USER_SETTINGS_NAME;
                        userSettingsHandler.sendMessage(msg);
                    } else {
                        Connection conn = null;
                        conn = (Connection) DBOpenHelper.getConn();
                        String sql = "update user_info set user_age='"+user_AGE+"' where user_phone='"+user_setting_phone+"'";
                        int i = 0;
                        PreparedStatement pstmt;
                        try {
                            pstmt = (PreparedStatement) conn.prepareStatement(sql);
                            i = pstmt.executeUpdate();
                            pstmt.close();
                            conn.close();
                        } catch (SQLException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }).start();

        }

    };


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                User_DataActivity.this.finish();
                break;
        }
        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Intent intent = new Intent();
        intent.setFlags(Intent.FLAG_ACTIVITY_NO_USER_ACTION);
        finish();
    }


}
