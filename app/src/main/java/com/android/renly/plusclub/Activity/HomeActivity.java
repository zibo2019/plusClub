package com.android.renly.plusclub.Activity;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.WindowManager;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.android.renly.plusclub.Adapter.MainPageAdapter;
import com.android.renly.plusclub.Common.BaseActivity;
import com.android.renly.plusclub.Common.BaseFragment;
import com.android.renly.plusclub.Common.MyToast;
import com.android.renly.plusclub.Common.NetConfig;
import com.android.renly.plusclub.Fragment.HomeFragment;
import com.android.renly.plusclub.Fragment.HotNewsFragment;
import com.android.renly.plusclub.Fragment.MineFragment;
import com.android.renly.plusclub.Fragment.ScheduleFragment;
import com.android.renly.plusclub.R;
import com.android.renly.plusclub.UI.MyBottomTab;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import okhttp3.Call;

public class HomeActivity extends BaseActivity implements ViewPager.OnPageChangeListener {
    @BindView(R.id.bottom_bar)
    MyBottomTab bottomBar;
    private ViewPager viewPager;
    private List<BaseFragment> fragments = new ArrayList<>();

    private long mExitTime;
    private Unbinder unbinder;
    private String version_name;

    private static final int GET_VERSION = 2333;
    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case GET_VERSION:
                    afterGetVersion(msg.getData().getString("obj"));
                    break;
            }
        }
    };

    @Override
    protected int getLayoutID() {
        return R.layout.activity_home;
    }

    @Override
    protected void initData() {
        initViewpager();
        discoverVersion();
    }

    private void discoverVersion() {
        PackageManager manager = getPackageManager();
        PackageInfo info = null;
        try {
            info = manager.getPackageInfo(getPackageName(), 0);
        } catch (Exception e) {
            e.printStackTrace();
        }
        version_name = "1.0";
        if (info != null) {
            version_name = info.versionName;
        }
        OkHttpUtils.get()
                .url(NetConfig.GITHUB_GET_RELEASE)
                .build()
                .execute(new StringCallback() {
                    @Override
                    public void onError(Call call, Exception e, int id) {

                    }

                    @Override
                    public void onResponse(String response, int id) {
                        if (!response.contains("url"))
                            return;
                        else{
                            Message msg = new Message();
                            msg.what = GET_VERSION;
                            Bundle bundle = new Bundle();
                            // 这里有bug
                            bundle.putString("obj",response);
                            msg.setData(bundle);
                            handler.sendMessage(msg);
                        }
                    }
                });
    }

    @Override
    protected void initView() {
        bottomBar.setOnTabChangeListener((v, position, isChange) -> setSelect(position, isChange));
    }

    private void setSelect(int position, boolean isChange) {
        if (isChange)
            viewPager.setCurrentItem(position, false);
        else
            fragments.get(position).ScrollToTop();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        printLog("HomeActivity onActivityResult");
        if (resultCode == RESULT_OK) {
            printLog("resultCode == RESULT_OK");
            switch (requestCode) {
                case ThemeActivity.requestCode://32
                    recreate();
                    printLog("onActivityResult ThemeActivity");
                    break;
                case LoginActivity.requestCode://64
                    doRefresh();
                    printLog("onActivityResult LoginActivity");
                    break;
                case UserDetailActivity.requestCode://128
                    doRefresh();
                    printLog("onActivityResult UserDetailActivity");
                    break;
                case SettingActivity.requestCode://256
                    doRefresh();
                    printLog("onActivityResult SettingActivity");
                    break;
                case ScheduleActivity.requestCode://512
                    doRefresh();
                    printLog("onActivityResult SettingActivity");
                    break;
            }
        } else
            printLog("resultCode != RESULT_OK");
        hideKeyBoard();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        hideKeyBoard();
        unbinder = ButterKnife.bind(this);
    }

    private void doRefresh() {
        if (homeFragment != null)
            homeFragment.doRefresh();
        if (hotNewsFragment != null)
            hotNewsFragment.doRefresh();
        if (scheduleFragment != null)
            scheduleFragment.doRefresh();
        if (mineFragment != null)
            mineFragment.doRefresh();
    }

    private HomeFragment homeFragment;
    private HotNewsFragment hotNewsFragment;
    private ScheduleFragment scheduleFragment;
    private MineFragment mineFragment;

    private void initViewpager() {
        viewPager = findViewById(R.id.view_pager);
        viewPager.setOffscreenPageLimit(4);
        viewPager.addOnPageChangeListener(this);
        homeFragment = new HomeFragment();
        hotNewsFragment = new HotNewsFragment();
        scheduleFragment = new ScheduleFragment();
        mineFragment = new MineFragment();
        fragments.add(homeFragment);
        fragments.add(hotNewsFragment);
        fragments.add(scheduleFragment);
        fragments.add(mineFragment);
        MainPageAdapter adapter = new MainPageAdapter(getSupportFragmentManager(), fragments);
        viewPager.setAdapter(adapter);
    }

    private void afterGetVersion(String jsonObj) {
        JSONObject jsonObject = JSON.parseObject(jsonObj);
        String tag_name = jsonObject.getString("tag_name"); // eg:1.4.0
        String name = jsonObject.getString("name"); // eg:正式推送版本
        String body = jsonObject.getString("body"); // eg:不要改需求了！
        JSONArray assets = JSONArray.parseArray(jsonObject.getString("assets"));
        JSONObject assetObj = assets.getJSONObject(0);
        String updated_at = assetObj.getString("updated_at"); // eg:2018-10-09T07:06:09Z
        String browser_download_url = assetObj.getString("browser_download_url");
        // eg:https://github.com/WithLei/DistanceMeasure/releases/download/1.4.0/DistanceMeasure.apk

        if (tag_name.equals(version_name)){
//            MyToast.showText(this,"已经是最新版本");
        }else{
            new AlertDialog.Builder(this)
                    .setTitle("检测到新版本")
                    .setMessage("版本名：" + name + "\n" +
                            "版本号：" + tag_name + "\n" +
                            "更新内容：\n" + body + "\n\n" +
                            "更新时间：" + updated_at)
                    .setCancelable(!body.contains("重要更新"))
                    .setPositiveButton("下载", (dialogInterface, i) -> {
                        Intent intent = new Intent();
                        Uri uri = Uri.parse(browser_download_url);
                        intent.setAction(Intent.ACTION_VIEW);
                        intent.setData(uri);
                        startActivity(intent);
                    })
                    .create()
                    .show();
        }
    }

    @Override
    public void onBackPressed() {
        if (getFragmentManager().getBackStackEntryCount() > 0) {
            getFragmentManager().popBackStack();
        } else {
            if ((System.currentTimeMillis() - mExitTime) > 1500) {
                Toast.makeText(this, "再按一次退出Plus客户端(｡･ω･｡)~~", Toast.LENGTH_SHORT).show();
                mExitTime = System.currentTimeMillis();
            } else {
                finish();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbinder.unbind();
    }

    @Override
    protected void onResume() {
        super.onResume();
        hideKeyBoard();
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {
        bottomBar.setSelect(position);
    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }
}
