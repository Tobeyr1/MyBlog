package com.example.frametest.UserMode;

import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.ContentUris;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.example.frametest.R;
import com.example.frametest.tools.DBOpenHelper;
import com.example.frametest.tools.MyApplication;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class User_DataActivity extends AppCompatActivity {
    private ImageView imageView_user;
    public static final int CHOOSE_USER_TOUX =11;
    public static final int USER_SETTINGS_NAME =12;
    public static final int USER_UPDATE_NAME =13;
    private TextView tv_user_photo,tv_nc,tv_nc_fb;
    String user_setting_phone;
    String input_userName;
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
            }
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user__data);
        user_setting_phone = MyApplication.getMoublefhoneUser();
        Toolbar uToolbar = (Toolbar) findViewById(R.id.userData_toolbar);
        tv_user_photo = (TextView)findViewById(R.id.tv_user_photo);
        imageView_user = (ImageView)findViewById(R.id.imageView_user);
        tv_nc = (TextView)findViewById(R.id.tv_nc);
        tv_nc_fb = (TextView) findViewById(R.id.tv_nc_fb);
        uToolbar.setTitle("个人信息");
        setSupportActionBar(uToolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null){
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeAsUpIndicator(R.drawable.ic_chevron_left);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        tv_user_photo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ContextCompat.checkSelfPermission(User_DataActivity.this,Manifest.permission.WRITE_EXTERNAL_STORAGE) !=PackageManager.PERMISSION_GRANTED){
                    ActivityCompat.requestPermissions(User_DataActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},1);
                }else {
                    openAlbum();
                }
            }
        });
        tv_nc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final EditText editText =new EditText(User_DataActivity.this);
                AlertDialog.Builder alog = new AlertDialog.Builder(User_DataActivity.this);
                alog.setTitle("输入用户名");
                alog.setView(editText);
                alog.setCancelable(false);
                alog.setPositiveButton("提交", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                input_userName = editText.getText().toString();
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
                });
                alog.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });
                alog.show();


            }
        });
    }
    private void openAlbum() {
        Intent mIntent = new Intent("android.intent.action.GET_CONTENT");
        mIntent.setType("image/*");
        startActivityForResult(mIntent,CHOOSE_USER_TOUX);
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case 1:
                if (grantResults.length>0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    openAlbum();
                }else {
                    Toast.makeText(this,"you denied the permission",Toast.LENGTH_SHORT).show();
                }
                break;
            default:
        }
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        switch (requestCode){
            case CHOOSE_USER_TOUX:
                if (resultCode == RESULT_OK) {
                    if (Build.VERSION.SDK_INT >= 19){
                        handleImageOnKiKat(data);
                    }else {
                        handleImageBeforeKiKat(data);
                    }
                }
                break;
        }
    }
    @TargetApi(Build.VERSION_CODES.KITKAT)
    private void handleImageOnKiKat(Intent data) {
        String imagePath = null;
        Uri uri = data.getData();
        if (DocumentsContract.isDocumentUri(this,uri)){
            String docId = DocumentsContract.getDocumentId(uri);
            if ("com.android.providers.media.documents".equals(uri.getAuthority())){
                String id = docId.split(":")[1];
                String selection = MediaStore.Images.Media._ID + "=" + id;
                imagePath = getImagePath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,selection);
            }else if ("com.android.providers.downloads.documents".equals(uri.getAuthority())){
                Uri contentUri = ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"),Long.valueOf(docId));
                imagePath = getImagePath(contentUri,null);
            }
        } else if ("content".equalsIgnoreCase(uri.getScheme())){
            //如或是content类型的URI就使用普通方法处理
            imagePath = getImagePath(uri,null);

        } else if ("file".equalsIgnoreCase(uri.getScheme())){
            //如果是file类型的直接获取图片路径就行
            imagePath = uri.getPath();
        }
        diplayImage(imagePath); //根据路径显示图片
    }
    private void handleImageBeforeKiKat(Intent data) {
        Uri uri = data.getData();
        String imagePath = getImagePath(uri,null);
        diplayImage(imagePath);
    }

    private String getImagePath(Uri uri, String selection) {
        String path = null;
        //通过Uri和selection来获取真实的图片路径
        Cursor cursor = getContentResolver().query(uri,null,selection,null,null);
        if (cursor != null){
            if (cursor.moveToFirst()){
                path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
            }
            cursor.close();
        }
        return path;
    }

    private void diplayImage(String imagePath) {
        if (imagePath != null){
            Bitmap bitmap = BitmapFactory.decodeFile(imagePath);
            imageView_user.setImageBitmap(bitmap);
            int a =10;
        } else {
            Toast.makeText(this,"failed to get image",Toast.LENGTH_SHORT).show();
        }
    }

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
        finish();
    }
}
