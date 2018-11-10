package com.android.renly.plusclub.Fragment;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.android.renly.plusclub.Activity.EditAcitivity;
import com.android.renly.plusclub.Activity.HomeActivity;
import com.android.renly.plusclub.Activity.LoginActivity;
import com.android.renly.plusclub.Activity.PostsActivity;
import com.android.renly.plusclub.Activity.UserDetailActivity;
import com.android.renly.plusclub.Adapter.ForumAdapter;
import com.android.renly.plusclub.Api.RetrofitService;
import com.android.renly.plusclub.App;
import com.android.renly.plusclub.Bean.Forum;
import com.android.renly.plusclub.Bean.User;
import com.android.renly.plusclub.Common.BaseFragment;
import com.android.renly.plusclub.Common.NetConfig;
import com.android.renly.plusclub.DataBase.MyDB;
import com.android.renly.plusclub.R;
import com.android.renly.plusclub.UI.CircleImageView;
import com.android.renly.plusclub.Utils.DateUtils;
import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.squareup.picasso.Picasso;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import okhttp3.Call;

public class HomeFragment extends BaseFragment {
    @BindView(R.id.ci_home_img)
    CircleImageView ciHomeImg;
    @BindView(R.id.tv_home_title)
    TextView tvHomeTitle;
    @BindView(R.id.iv_home_search)
    ImageView ivHomeSearch;
    @BindView(R.id.list)
    RecyclerView list;
    @BindView(R.id.tv_weather)
    TextView tvWeather;
    @BindView(R.id.iv_weather)
    ImageView ivWeather;
    @BindView(R.id.scrollView)
    ScrollView scrollView;
    @BindView(R.id.ll_logintip)
    RelativeLayout llLogintip;
    @BindView(R.id.refresh_layout)
    SmartRefreshLayout refreshLayout;
    @BindView(R.id.ll_edittip)
    RelativeLayout llEdittip;
    private Unbinder unbinder;


    private List<Forum> forumList;
    private String[] headers;

    @Override
    public int getLayoutid() {
        return R.layout.fragment_home;
    }

    @Override
    protected void initData(Context content) {
        if (App.ISLOGIN(mActivity)) {
            MyDB db = new MyDB(mActivity);
            if (db.isUserExist(App.getUid(mActivity)))
                Picasso.get()
                        .load(db.getUserAvatarPath(App.getUid(mActivity)))
                        .placeholder(R.drawable.image_placeholder)
                        .into(ciHomeImg);
            else
                getUserAvator();
            llLogintip.setVisibility(View.GONE);
            llEdittip.setVisibility(View.VISIBLE);
        } else {
            ciHomeImg.setImageDrawable(getResources().getDrawable(R.drawable.image_placeholder));
            llLogintip.setVisibility(View.VISIBLE);
            llEdittip.setVisibility(View.GONE);
        }
        getWeatherData();
        initForumListData();
        initView();
    }

    private void initView() {
        initForumList();
        initRefreshLayout();
    }

    private void initRefreshLayout() {
        refreshLayout.setOnRefreshListener(refreshLayout -> {
            doRefresh();
            refreshLayout.finishRefresh(2000);
        });
        refreshLayout.setOnLoadMoreListener(refreshLayout -> refreshLayout.finishLoadMore(2000));

    }

    public void doRefresh() {
        if (App.ISLOGIN(mActivity)) {
            getUserAvator();
            llLogintip.setVisibility(View.GONE);
            llEdittip.setVisibility(View.VISIBLE);
        } else {
            ciHomeImg.setImageDrawable(getResources().getDrawable(R.drawable.image_placeholder));
            llLogintip.setVisibility(View.VISIBLE);
            llEdittip.setVisibility(View.GONE);
        }
        getWeatherData();
    }

    private void initForumList() {
        ForumAdapter adapter = new ForumAdapter(mActivity, forumList);
        adapter.setOnItemClickListener(pos -> {
            Intent intent = new Intent(mActivity, PostsActivity.class);
            intent.putExtra("Title", forumList.get(pos).getTitle());
            intent.putExtra("category", forumList.get(pos).getCategory());
            startActivity(intent);
        });
        GridLayoutManager layoutManager = new GridLayoutManager(mActivity, 4);
        list.setClipToPadding(false);
        list.setLayoutManager(layoutManager);
        list.setAdapter(adapter);
    }

    private void initWeatherView(boolean isSuccess, String weather) {
        ivWeather.setImageResource(R.drawable.ic_sun);
        int currentHour = DateUtils.getHourTimeOfDay();
        if (currentHour <= 5 || currentHour >= 19) {
            tvWeather.setText("晚上好！");
            ivWeather.setImageResource(R.drawable.ic_moon);
        } else if (currentHour <= 10) {
            tvWeather.setText("早上好！");
        } else if (currentHour <= 13) {
            tvWeather.setText("中午好！");
        } else {
            tvWeather.setText("下午好！");
        }

        if (isSuccess) {
//            JSONObject jsonObject = JSON.parseObject(weatherObj);
//            String weather = jsonObject.getString("weather");
            if (weather.contains("晴"))
                return;
            else if (weather.contains("雨"))
                ivWeather.setImageResource(R.drawable.ic_rain);
            else if (weather.contains("风"))
                ivWeather.setImageResource(R.drawable.ic_cloudy);
            else if (weather.contains("雪"))
                ivWeather.setImageResource(R.drawable.ic_snow);
        }
    }

    private void getWeatherData() {
        RetrofitService.getWeather("101210801")
                .subscribe(weather -> initWeatherView(true, weather.getWeatherinfo().getWeather()),
                        throwable -> initWeatherView(false, null));
    }

    private void initForumListData() {
        forumList = new ArrayList<>();
        forumList.add(new Forum("灌水专区", R.drawable.ic_01, 0, "daily"));
        forumList.add(new Forum("技术交流", R.drawable.ic_02, 0, "code"));
        forumList.add(new Forum("问答专区", R.drawable.ic_03, 1, "qa"));
        forumList.add(new Forum("发展建议", R.drawable.ic_08, 1, "suggests"));
        forumList.add(new Forum("论坛反馈", R.drawable.ic_05, 2, "feedback"));
        forumList.add(new Forum("校园交易", R.drawable.ic_06, 2, "transaction"));
        forumList.add(new Forum("公告活动", R.drawable.ic_07, 2, "activity"));
    }

    @Override
    public void ScrollToTop() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = super.onCreateView(inflater, container, savedInstanceState);
        unbinder = ButterKnife.bind(this, rootView);
        return rootView;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    @OnClick({R.id.ci_home_img, R.id.iv_home_search, R.id.tip_login, R.id.tip_edit})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.ci_home_img:
                if (App.ISLOGIN(mActivity)) {
                    Intent intent = new Intent(mActivity, UserDetailActivity.class);
                    intent.putExtra("userid", App.getUid(mActivity));
                    mActivity.startActivity(intent);
                } else {
                    Intent intent = new Intent(mActivity, LoginActivity.class);
                    mActivity.startActivityForResult(intent, LoginActivity.requestCode);
                }
                mActivity.overridePendingTransition(R.anim.translate_in, R.anim.translate_out);
                break;
            case R.id.iv_home_search:
                ToastProgramError();
                testRxjava();
                break;
            case R.id.tip_login:
                Intent intent = new Intent(mActivity, LoginActivity.class);
                mActivity.startActivityForResult(intent, LoginActivity.requestCode);
                break;
            case R.id.tip_edit:
                Intent in = new Intent(mActivity, EditAcitivity.class);
                in.putExtra("category", "E-M-P-T-Y");
                mActivity.startActivity(in);
        }
    }

    private void testRxjava() {
        Observable.create((ObservableOnSubscribe<String>) emitter -> {
                    printLog("rxjava start");
                    emitter.onError(new Throwable("rxjava onError"));
                    printLog("rxjava after onError");
                    emitter.onNext("rxjava onNext");
                    printLog("rxjava after onNext");
                    emitter.onComplete();
                    printLog("rxjava after onComplete");
                }

        )
//                .retryWhen(new Function<Observable<Throwable>, ObservableSource<?>>() {
//                    @Override
//                    public ObservableSource<String> apply(Observable<Throwable> throwableObservable) throws Exception {
//                        return throwableObservable.flatMap(new Function<Throwable, ObservableSource<?>>() {
//                            @Override
//                            public ObservableSource<String> apply(Throwable throwable) throws Exception {
//                                return Observable.create(emitter -> {
//                                    printLog("doIn new Observable");
//                                }).flatMap(new Function<List<String>, ObservableSource<String>>() {
//                                    @Override
//                                    public ObservableSource<String> apply(List<String> lists) {
//                                        return null;
//                                    }
//                                });
//                            }
//                        });
//                    }
//                })
                .subscribe(new Observer<String>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        printLog("onSubscribe()");
                    }

                    @Override
                    public void onNext(String s) {
                        printLog("onNext() " + s);
                    }

                    @Override
                    public void onError(Throwable e) {
                        printLog("onError() " + e.getMessage());
                    }

                    @Override
                    public void onComplete() {
                        printLog("onComplete()");
                    }
                });
    }

    public void getUserAvator() {
        Observable.create((ObservableOnSubscribe<String>) emitter ->
                OkHttpUtils.get()
                        .url(NetConfig.BASE_USERDETAIL_PLUS)
                        .addHeader("Authorization", "Bearer " + App.getToken(mActivity))
                        .build()
                        .execute(new StringCallback() {
                            @Override
                            public void onError(Call call, Exception e, int id) {
                                printLog("getUserAvator onError" + e.getMessage());
                                ToastNetWorkError();
                            }

                            @Override
                            public void onResponse(String response, int id) {
                                if (!response.contains("code")) {
                                    ToastNetWorkError();
                                    return;
                                }
                                JSONObject jsonObject = JSON.parseObject(response);
                                if (jsonObject.getInteger("code") == 50011) {
                                    emitter.onError(new Throwable("getNewToken"));
                                } else if (jsonObject.getInteger("code") != 20000) {
                                    ToastShort("服务器出状况惹，再试试( • ̀ω•́ )✧");
                                    printLog("getInfoError" + response);
                                } else {
                                    JSONObject obj = JSON.parseObject(jsonObject.getString("result"));
                                    String avatarSrc = obj.getString("avatar");
                                    emitter.onNext(avatarSrc);
                                }

                            }
                        }))
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<String>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(String path) {
                        printLog("onNext()");
                        Picasso.get()
                                .load(path)
                                .placeholder(R.drawable.image_placeholder)
                                .into(ciHomeImg);
                    }

                    @Override
                    public void onError(Throwable e) {
                        printLog("onError()" + e.getMessage());
                        if (e.getMessage().equals("getNewToken"))
                            getNewToken();
                    }

                    @Override
                    public void onComplete() {

                    }
                });

    }

    /**
     * 获取新的Token
     */
    private void getNewToken() {

        OkHttpUtils.post()
                .url(NetConfig.BASE_GETNEWTOKEN_PLUS)
                .addHeader("Authorization", "Bearer " + App.getToken(mActivity))
                .build()
                .execute(new StringCallback() {
                    @Override
                    public void onError(Call call, Exception e, int id) {
                        printLog("HomeFragment getNewToken onError");
                    }

                    @Override
                    public void onResponse(String response, int id) {
                        JSONObject obj = JSON.parseObject(response);
                        if (obj.getInteger("code") != 20000) {
                            printLog("HomeFragment getNewToken() onResponse获取Token失败,重新登陆");
                            doRefresh();
                        } else {
                            printLog("获取Token成功");
                            App.setToken(getContext(), obj.getString("result"));
                            getUserAvator();
                        }
                    }
                });
    }

    private HomeActivity mActivity;
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mActivity = (HomeActivity)context;
    }
}
