package com.example.frametest.UserMode;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.frametest.R;
import com.example.frametest.tools.DBOpenHelper;
import com.mob.MobSDK;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import cn.smssdk.EventHandler;
import cn.smssdk.SMSSDK;


public class LoginActivity extends AppCompatActivity implements View.OnClickListener {
    String APPKEY = "2b85a17d997b2";
    String APPSECRET = "bc386e05d6171e915f1195858a9d017b";
    String phoneNums;
    // 手机号输入框
    private EditText inputPhoneEt;

    private static final int UPDATE_INSERT = 0;

    // 验证码输入框
    private EditText inputCodeEt;

    // 获取验证码按钮
    private Button requestCodeBtn;

    // 注册按钮
    private Button commitBtn;
    private Button shareBtn;

    //倒计时显示   可以手动更改。
    int i = 50;
    String inputText;
    @SuppressLint("HandlerLeak")
    private Handler userHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            String user_phone,user_name,user_pasw;
            switch (msg.what){
                case UPDATE_INSERT:
                    User user = (User) msg.obj;
                    user_phone = user.getUser_phone();
                    break;
            }
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        inputPhoneEt = (EditText) findViewById(R.id.login_input_phone_et);
        inputCodeEt = (EditText) findViewById(R.id.login_input_code_et);
        requestCodeBtn = (Button) findViewById(R.id.login_request_code_btn);
        commitBtn = (Button) findViewById(R.id.login_commit_btn);
        // 初始化sdk
        MobSDK.init(this,APPKEY,APPSECRET);
    }

    @Override
    protected void onStart() {
        super.onStart();
        requestCodeBtn.setOnClickListener(this);
        commitBtn.setOnClickListener(this);
        //启动短信验证SDK
        EventHandler eventHandler = new EventHandler(){
            @Override
            public void afterEvent(int event, int result, Object data) {
                Message msg = new Message();
                msg.arg1 = event;
                msg.arg2 = result;
                msg.obj = data;
                handler.sendMessage(msg);
            }
        };
        //注册回调监听接口
        SMSSDK.registerEventHandler(eventHandler);
    }

    @Override
    public void onClick(View v) {
        //取出输入的手机号
        phoneNums = inputPhoneEt.getText().toString();
        switch (v.getId()) {
            case R.id.login_request_code_btn:
                // 1. 判断手机号是不是11位并且看格式是否合理
                if (!judgePhoneNums(phoneNums)) {
                    return;
                } // 2. 通过sdk发送短信验证
                SMSSDK.getVerificationCode("86", phoneNums);

                // 3. 把按钮变成不可点击，并且显示倒计时（正在获取）
                requestCodeBtn.setClickable(false);
                requestCodeBtn.setText("重新发送(" + i + ")");
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        for (; i > 0; i--) {
                            handler.sendEmptyMessage(-9);
                            if (i <= 0) {
                                break;
                            }
                            try {
                                Thread.sleep(1000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                        handler.sendEmptyMessage(-8);
                    }
                }).start();
                break;

            case R.id.login_commit_btn:
                //将收到的验证码和手机号提交再次核对
                SMSSDK.submitVerificationCode("86", phoneNums, inputCodeEt
                        .getText().toString());
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        Connection conn = null;
                        conn = (Connection) DBOpenHelper.getConn();
                        String sql = "insert into user_info(user_phone) values(?)";
                        int i = 0;
                        String id=null;
                        String user_name=null;
                        String user_phone=null;
                        String user_pasw=null;
                        PreparedStatement pstmt;

                        try {
                            User user = new User();
                            pstmt = (PreparedStatement) conn.prepareStatement(sql);
                            pstmt.setString(1,phoneNums);
                            i = pstmt.executeUpdate();
                            user.setUser_phone(user_phone);
                            Message message = new Message();
                            message.what = UPDATE_INSERT;
                            message.obj = user;
                            userHandler.sendMessage(message);
                            pstmt.close();
                            conn.close();
                        } catch (SQLException e) {
                            e.printStackTrace();
                        }
                    }
                }).start();
                break;
        }
    }
    @SuppressLint("HandlerLeak")
    Handler handler = new Handler(){
        public void handleMessage(Message msg) {
            if (msg.what == -9) {
                requestCodeBtn.setText("重新发送(" + i + ")");
            } else if (msg.what == -8) {
                requestCodeBtn.setText("获取验证码");
                requestCodeBtn.setClickable(true);
                i = 30;
            } else {
                int event = msg.arg1;
                int result = msg.arg2;
                Object data = msg.obj;
                Log.e("event", "event=" + event);
                if (result == SMSSDK.RESULT_COMPLETE) {
                    // 短信注册成功后，返回MainActivity,然后提示
                    if (event == SMSSDK.EVENT_SUBMIT_VERIFICATION_CODE) {// 提交验证码成功
                        Toast.makeText(getApplicationContext(), "提交验证码成功",
                                Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent();
                        intent.putExtra("data_return",inputPhoneEt.getText().toString());
                        setResult(RESULT_OK,intent);
                        Bundle bundle = new Bundle();
                        bundle.putString("userName",phoneNums);
                        intent.putExtras(bundle);
                        finish();
                    } else if (event == SMSSDK.EVENT_GET_VERIFICATION_CODE) {
                        Toast.makeText(getApplicationContext(), "正在获取验证码",
                                Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(LoginActivity.this,"验证码不正确",Toast.LENGTH_SHORT).show();
                        ((Throwable) data).printStackTrace();
                    }
                }
            }
        }
    };
    /**
     * 判断手机号码是否合理
     *
     * @param phoneNums
     */
    private boolean judgePhoneNums(String phoneNums) {
        if (isMatchLength(phoneNums, 11)
                && isMobileNO(phoneNums)) {
            return true;
        }
        Toast.makeText(this, "手机号码输入有误！",Toast.LENGTH_SHORT).show();
        return false;

    }
    /**
     * 验证手机格式
     */
    private boolean isMobileNO(String mobileNums) {
        /*
         * 移动：134、135、136、137、138、139、150、151、157(TD)、158、159、187、188
         * 联通：130、131、132、152、155、156、185、186 电信：133、153、180、189、（1349卫通）
         * 总结起来就是第一位必定为1，第二位必定为3或5或8，其他位置的可以为0-9
         */
        String telRegex = "[1][358]\\d{9}";// "[1]"代表第1位为数字1，"[358]"代表第二位可以为3、5、8中的一个，"\\d{9}"代表后面是可以是0～9的数字，有9位。
        if (TextUtils.isEmpty(mobileNums))
            return false;
        else
            return mobileNums.matches(telRegex);

    }
    /**
     * 判断一个字符串的位数
     * @param str
     * @param length
     * @return
     */
    private boolean isMatchLength(String str, int length) {
        if (str.isEmpty()) {
            return false;
        } else {
            return str.length() == length ? true : false;
        }

    }
    @Override
    protected void onDestroy() {
        //反注册回调监听接口
        SMSSDK.unregisterAllEventHandler();
        //存储手机号
        inputText = inputPhoneEt.getText().toString();
        save(inputText);
        System.out.println("活动毁灭之前是否传值"+inputText);
        super.onDestroy();
    }

    public void save(String inputText) {
        FileOutputStream out = null;
        BufferedWriter writer = null;
        try {
            out = openFileOutput("data",Context.MODE_PRIVATE);
            writer = new BufferedWriter(new OutputStreamWriter(out));
            writer.write(inputText);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (writer != null){
                try {
                    writer.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

    }
}
