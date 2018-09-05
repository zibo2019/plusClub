package com.android.renly.plusclub.Common;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AppCompatDelegate;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import com.android.renly.plusclub.Activity.ThemeActivity;
import com.android.renly.plusclub.App;
import com.android.renly.plusclub.R;

import java.io.File;
import java.io.RandomAccessFile;
import java.util.Calendar;

import butterknife.ButterKnife;

public abstract class BaseActivity extends FragmentActivity {
    /***是否显示标题栏*/
    private  boolean isshowtitle = true;
    /***是否显示标题栏*/
    private  boolean isshowstate = false;
    /***封装toast对象**/
    private static Toast toast;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //切换主题
        switchTheme();
        if(!isshowtitle){
            requestWindowFeature(Window.FEATURE_NO_TITLE);
        }

        if(isshowstate){
            getWindow().setFlags(WindowManager.LayoutParams. FLAG_FULLSCREEN ,
                    WindowManager.LayoutParams. FLAG_FULLSCREEN);
        }
        setContentView(getLayoutID());
        ButterKnife.bind(this);
        //设置数据
        initData();
        //初始化控件
        initView();
    }
    protected abstract int getLayoutID();

    protected abstract void initData();

    protected abstract void initView();

    public void ToastLong(String msg){
        MyToast.showText(this,msg,Toast.LENGTH_LONG);
    }

    public void ToastShort(String msg){
        MyToast.showText(this,msg,Toast.LENGTH_SHORT);
    }

    /**
     * 是否设置标题栏
     *
     * @return
     */
    public void setTitle(boolean ishow) {
        isshowtitle=ishow;
    }

    /**
     * 设置是否显示状态栏
     * @param ishow
     */
    public void setState(boolean ishow) {
        isshowstate=ishow;
    }

    /**
     * Log输出
     * error
     * Filter:print
     * @param str
     */
    public void printLog(String str){
        Log.e("print",str);
    }

    /**
     * 打印str到手机src内存中
     * @param src 地址
     * @param str 打印内容
     */
    public void writeData(String src,String str) {
        try {
            File file = new File(src);
            if (!file.exists()) {
                printLog("Create the file:" + src);
                file.getParentFile().mkdirs();
                file.createNewFile();
            }
            RandomAccessFile raf = new RandomAccessFile(file, "rwd");
//            raf.seek(file.length());
            raf.write(str.getBytes());
            raf.close();
        } catch (Exception e) {
            Log.e("TestFile", "Error on write File:" + e);
        }
    }

    /**
     * 打开targetActivity
     * @param targetActivity
     */
    public void gotoActivity(Class targetActivity){
        startActivity(new Intent(this,targetActivity));
    }

    /**
     * 中途 切换主题
     */
    public void switchTheme() {
        //直接夜间 设置退出
        int theme = App.getCustomTheme(this);
        int cur = AppCompatDelegate.getDefaultNightMode();
        int to = cur;
        boolean autoChnage = false;

        //夜间主题
        if (theme == ThemeActivity.THEME_NIGHT) {
            to = AppCompatDelegate.MODE_NIGHT_YES;
        } else {//白天主题
            if (App.isAutoDarkMode(this)) {
                autoChnage = true;
                int[] time = App.getDarkModeTime(this);
                int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
                if ((hour >= time[0] || hour < time[1])) {
                    //自动切换
                    to = AppCompatDelegate.MODE_NIGHT_YES;
                } else {
                    to = AppCompatDelegate.MODE_NIGHT_NO;
                }
            } else {
                to = AppCompatDelegate.MODE_NIGHT_NO;
            }
        }

        if (to == AppCompatDelegate.MODE_NIGHT_YES) {
            //夜间模式主题
            setTheme(R.style.AppTheme);
        } else {
            setTheme(theme);
        }

        //黑白发生了变化
        if (to != cur) {
//            if (autoChnage) {
//                showToast("自动" + (to == AppCompatDelegate.MODE_NIGHT_YES ?
//                        "切换到夜间模式" : "关闭夜间模式"));
//            }
            AppCompatDelegate.setDefaultNightMode(to);
        }
    }
}
