package org.scau.mimi.activity;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Point;
import android.graphics.Rect;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;


import com.hitomi.tilibrary.transfer.Transferee;
import com.nineoldandroids.view.ViewHelper;

import org.jetbrains.annotations.NotNull;
import org.litepal.crud.DataSupport;
import org.scau.mimi.R;

import org.scau.mimi.database.User;
import org.scau.mimi.fragment.ChatFragment;
import org.scau.mimi.fragment.MomentFragment;
import org.scau.mimi.gson.MessagesInfo;
import org.scau.mimi.SingleInstance.Client;
import org.scau.mimi.receiver.NetworkChangeReceiver;
import org.scau.mimi.util.HttpUtil;
import org.scau.mimi.util.LogUtil;
import org.scau.mimi.util.ResponseUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import client.yalantis.com.foldingtabbar.FoldingTabBar;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;
import rx.functions.Action1;
import ua.naiksoftware.stomp.LifecycleEvent;
import ua.naiksoftware.stomp.client.StompClient;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    //Views;
    private ActionBar actionBar;
    private DrawerLayout drawerLayout;
    private FoldingTabBar ftbMenu;
    private ImageView ivAddtionButtonLeft;
    private TextView tvLogOut;
    private RelativeLayout rlHomeLayout;

    //Variables
    private int mOriginFtbMenuTop;
    private List<MessagesInfo.Content.Message.Location> mLocations;
    private Client mClient;
    private NetworkChangeReceiver mReceiver;
    //Test
    private Transferee mTransferee;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);




//        UltimateBar ultimateBar = new UltimateBar(this);
//        ultimateBar.setTransparentBar(Color.parseColor("#000000"), 127);
//        ultimateBar.setHintBar();下拉通知才显示，平时隐藏状态栏
//        ultimateBar.setColorBarForDrawer(Color.parseColor("#4a4a52"), 50);

//        ultimateBar.setHintBar();

        loadData();
        initVariables();
        initViews();


    }

    @Override
    protected void onResume() {
        super.onResume();
        mTransferee = Transferee.getDefault(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mTransferee.destroy();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mReceiver);
    }

    private void initViews() {
        actionBar = getSupportActionBar();
        drawerLayout = (DrawerLayout) findViewById(R.id.dl_main);
        ftbMenu = (FoldingTabBar) findViewById(R.id.ftb_main_menu);
        ivAddtionButtonLeft = (ImageView) findViewById(R.id.fab_addtion_button_left);
        tvLogOut = (TextView) findViewById(R.id.tv_main_menu_log_out);

        //设置抽屉菜单
        tvLogOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                HttpUtil.logOut(new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {

                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        String data = ResponseUtil.getString(response);
                        LogUtil.d(TAG, "log out: " + data);
                        LoginActivity.actionStart(MainActivity.this);
                        finish();
                    }
                });
            }
        });

        rlHomeLayout = (RelativeLayout) findViewById(R.id.rl_main_home_layout);

        if (actionBar != null) {
            actionBar.hide();
        }

        drawerLayout.setScrimColor(0x00);
        drawerLayout.setDrawerListener(new DrawerLayout.DrawerListener() {
            @Override
            public void onDrawerStateChanged(int newState) {
            }

            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {
                View mContent = drawerLayout.getChildAt(0);
                View mMenu = drawerView;
                float scale = 1 - slideOffset;
                float rightScale = 0.8f + scale * 0.2f;

                if (drawerView.getTag().equals("LEFT")) {

                    float leftScale = 1 - 0.3f * scale;

                    ViewHelper.setScaleX(mMenu, leftScale);
                    ViewHelper.setScaleY(mMenu, leftScale);
                    ViewHelper.setAlpha(mMenu, 0.6f + 0.4f * (1 - scale));
//                    ViewHelper.setAlpha(mMenu,1f);

                    ViewHelper.setTranslationX(mContent, mMenu.getMeasuredWidth() * (1 - scale));
                    ViewHelper.setPivotX(mContent, 0);
                    ViewHelper.setPivotY(mContent, mContent.getMeasuredHeight() / 2);
                    mContent.invalidate();
                    ViewHelper.setScaleX(mContent, rightScale);
                    ViewHelper.setScaleY(mContent, rightScale);
                } else {
                    ViewHelper.setTranslationX(mContent, -mMenu.getMeasuredWidth() * slideOffset);
                    ViewHelper.setPivotX(mContent, mContent.getMeasuredWidth());
                    ViewHelper.setPivotY(mContent, mContent.getMeasuredHeight() / 2);
                    mContent.invalidate();
                    ViewHelper.setScaleX(mContent, rightScale);
                    ViewHelper.setScaleY(mContent, rightScale);
                }

            }

            @Override
            public void onDrawerOpened(View drawerView) {
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED, Gravity.RIGHT);
            }
        });

        ftbMenu.post(new Runnable() {
            @Override
            public void run() {
                mOriginFtbMenuTop = ftbMenu.getTop();
            }
        });
        ftbMenu.setOnFoldingItemClickListener(new FoldingTabBar.OnFoldingItemSelectedListener() {
            @Override
            public boolean onFoldingItemSelected(@NotNull MenuItem menuItem) {
                Fragment fragment = getSupportFragmentManager()
                        .findFragmentById(R.id.fl_fragment_container);
                switch (menuItem.getItemId()) {
                    case R.id.activity_main_menu_chat:
                        replaceFragment(new ChatFragment());
                        break;
                    case R.id.activity_main_menu_moment:
                        if (fragment != null && fragment instanceof MomentFragment) {
                            //back to top
                            ((MomentFragment) fragment).backToTop();
                        } else {
                            replaceFragment(MomentFragment.newInstance());
                        }
                        break;
                    case R.id.activity_main_menu_settings:
                        drawerLayout.openDrawer(Gravity.LEFT);
                        break;
                    case R.id.activity_main_menu_personal:
                        replaceFragment(new MomentFragment());
                        break;
                }
                return true;
            }
        });

        ftbMenu.setOnMainButtonClickListener(new FoldingTabBar.OnMainButtonClickedListener() {
            @Override
            public void onMainButtonClicked() {
                
            }
        });



        ivAddtionButtonLeft.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SendMomentActivity.actionStart(MainActivity.this);
            }
        });

        addDefaultFragment();

    }


    private void initVariables() {

        mReceiver = new NetworkChangeReceiver();
        registerReceiver(mReceiver,
                new IntentFilter("android.net.conn.CONNECTIVITY_CHANGE"));

        mLocations = new ArrayList<>();


    }

    private void loadData() {
        User user = DataSupport.findFirst(User.class);
        if (user == null) {
            LoginActivity.actionStart(this);
            finish();
        }


//        mClient = Client.getInstance(user.getSecret());
//        mClient.conncectServer();


    }

    public Transferee getTransferee() {
        return mTransferee;
    }

    private void addDefaultFragment() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction()
                .add(R.id.fl_fragment_container, MomentFragment.newInstance())
                .commit();
    }

    private void replaceFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.fl_fragment_container, fragment)
                .commit();
    }

    public void setFtbMenuTopAndBottom(int newTop, int newBottom) {
        ftbMenu.setTop(newTop);
        ftbMenu.setBottom(newBottom);
    }

    public int getFtbMenuTop() {
        return ftbMenu.getTop();
    }

    public int getFtbMenuBottom() {
        return ftbMenu.getBottom();
    }

    public int getOriginFtbMenuTop() {
        return mOriginFtbMenuTop;
    }

    public boolean isFtbMenuInScreen() {
        int width;
        int height;
        Point point = new Point();
        getWindowManager().getDefaultDisplay().getSize(point);
        width = point.x;
        height = point.y;

        Rect rect = new Rect(0, 0, width, height);

        if (!ftbMenu.getLocalVisibleRect(rect))
            return false;

        return true;
    }

    public void ftbMenuScrollTo(int y) {

    }

    public static void actionStart(Context context) {
        Intent intent = new Intent(context, MainActivity.class);
        context.startActivity(intent);
    }


}
