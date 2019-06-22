package com.example.frametest;

import android.content.DialogInterface;
import android.content.Intent;
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
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity {
    private android.support.v7.widget.Toolbar toolbar;
    private DrawerLayout mDrawerLayout;
    private NavigationView navigationView;
    private TabLayout tabLayout;
    private ViewPager viewPager;
    private List<String> list;
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
    }

    @Override
    protected void onStart() {
        super.onStart();
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
                        //每个菜单项的点击事件，通过Intent实现点击item简单实现活动页面的跳转。
                        /*Intent intent = new Intent(MainActivity.this, Main2Activity.class);
                        //第二个Main2Activity.class需要你自己new一个 Activity来做出其他功能页面
                        startActivity(intent);*/
                        break;
                    case R.id.nav_friends:
                        Toast.makeText(MainActivity.this, "你点击了好友", Toast.LENGTH_SHORT).show();
                        break;
                    case R.id.nav_location:
                        Toast.makeText(MainActivity.this, "你点击了发布新闻，下步实现", Toast.LENGTH_SHORT).show();
                        break;
                    case R.id.nav_favorite:
                        Toast.makeText(MainActivity.this, "你点击了个人收藏，下步实现", Toast.LENGTH_SHORT).show();
                        break;
                    case R.id.nav_settings:
                        Toast.makeText(MainActivity.this,"需要做出登出功能，可扩展夜间模式，离线模式等,检查更新",Toast.LENGTH_LONG).show();
                        break;
                    case R.id.nav_exit:

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
                mDrawerLayout.openDrawer(GravityCompat.START);  //打开侧滑栏
                break;
            case R.id.userFeedback:
                final EditText ed =new EditText(MainActivity.this);
                AlertDialog.Builder dialog = new AlertDialog.Builder(MainActivity.this);
                dialog.setTitle("用户反馈");
                dialog.setView(ed);
                dialog.setCancelable(false);
                dialog.setPositiveButton("确认", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                       //添加点击事件
                    }
                });
                dialog.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });
                dialog.show();
                break;
            case R.id.userExit:
                Toast.makeText(this,"ni click 退出",Toast.LENGTH_SHORT).show();
                break;
            default:

        }
        return true;
    }
}