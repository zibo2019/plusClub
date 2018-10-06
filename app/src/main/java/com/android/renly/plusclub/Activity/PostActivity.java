package com.android.renly.plusclub.Activity;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ScrollView;

import com.android.renly.plusclub.Adapter.PostAdapter;
import com.android.renly.plusclub.Bean.Post;
import com.android.renly.plusclub.Common.BaseActivity;
import com.android.renly.plusclub.Fragment.PostContentFragment;
import com.android.renly.plusclub.R;
import com.android.renly.plusclub.UI.ThemeUtil;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;
import com.sothree.slidinguppanel.SlidingUpPanelLayout.PanelState;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public class PostActivity extends BaseActivity {
    @BindView(R.id.myToolBar)
    FrameLayout myToolBar;
    @BindView(R.id.sliding_layout)
    SlidingUpPanelLayout slidingLayout;
    @BindView(R.id.rv_post)
    RecyclerView rvPost;
    @BindView(R.id.sv_postcontent)
    ScrollView svPostcontent;
    @BindView(R.id.swiperefresh_post)
    SwipeRefreshLayout refreshLayout;

    private Unbinder unbinder;

    /**
     * 帖子区的标题
     */
    private String title;
    /**
     * 帖子列表
     */
    private List<Post> postList;
    /**
     * Panel Fragment管理器
     */
    private FragmentManager fragmentManager;
    /**
     * Panel Fragment池
     */
    private List<PostContentFragment> fragmentPool;
    private Fragment nowFragment = null;

    @Override
    protected int getLayoutID() {
        return R.layout.activity_post;
    }

    @Override
    protected void initData() {
        title = getIntent().getStringExtra("Title");
        fragmentManager = getSupportFragmentManager();
        initPostListData();
    }

    @Override
    protected void initView() {
        initToolBar(true, title);
        addToolbarMenu(R.drawable.ic_create_black_24dp).setOnClickListener(view -> {
            gotoActivity(EditAcitivity.class);
        });
        initSlidr();
        initRefreshLayout();
        initSlidingLayout();
        initpostList();
    }

    /**
     * 下拉刷新样式
     */
    private void initRefreshLayout() {
        refreshLayout.setColorSchemeColors(ThemeUtil.getThemeColor(this,R.attr.colorAccent));
        refreshLayout.setOnRefreshListener(() -> new Thread(){
            @Override
            public void run() {
                try {
                    sleep(3000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                runOnUiThread(() -> {
                    if (refreshLayout.isRefreshing())
                        refreshLayout.setRefreshing(false);
                });
            }
        }.start());
    }

    /**
     * 准备测试的帖子列表数据
     */
    private void initPostListData() {
        postList = new ArrayList<>();
        for (int i = 0; i < 10; i++)
            postList.add(new Post(i, "测试标题" + i, "测试姓名" + i, "2018-1-1 19:20:15", 233, 128));
    }

    private void initpostList() {
        // 初始化fragmentPool池
        fragmentPool = new ArrayList<>();
        PostAdapter adapter = new PostAdapter(this, postList);
        // 设置监听事件
        rvPost.setAdapter(adapter);
        adapter.setOnItemClickListener(pos -> {
            PostContentFragment fragment = new PostContentFragment();
            fragmentPool.add(fragment);
            nowFragment = fragment;
            loadPanel(fragment, fragmentPool.size() == 1 ? null : fragmentPool.get(fragmentPool.size() - 2));
        });
        rvPost.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        rvPost.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        // 调整draw缓存,加速recyclerview加载
        rvPost.setItemViewCacheSize(20);
        rvPost.setDrawingCacheEnabled(true);
        rvPost.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);
    }

    /**
     * 加载panel布局
     */
    private void loadPanel(Fragment targetFragment, Fragment lastFragment) {
        slidingLayout.setPanelState(PanelState.HIDDEN);

        //加载fragment
        FragmentTransaction transaction = fragmentManager.beginTransaction();

        if (lastFragment != null)
            transaction.remove(lastFragment);
        transaction.add(R.id.sv_postcontent, targetFragment);

        transaction.commit();
        //显示
        slidingLayout.setPanelState(PanelState.COLLAPSED);
    }

    /**
     *
     */

    private void initSlidingLayout() {
        slidingLayout.setAnchorPoint(0.7f);
        slidingLayout.setPanelState(PanelState.HIDDEN);
        slidingLayout.setFadeOnClickListener(view -> {
            doDownAnimation();
            slidingLayout.setPanelState(PanelState.COLLAPSED);
        });
        slidingLayout.addPanelSlideListener(new SlidingUpPanelLayout.SimplePanelSlideListener() {
            @Override
            public void onPanelSlide(View panel, float slideOffset) {
                super.onPanelSlide(panel, slideOffset);
            }

            @Override
            public void onPanelStateChanged(View panel, PanelState previousState, PanelState newState) {
                if (newState == PanelState.DRAGGING) {
                    ppState = previousState;
                    return;
                }
                if (newState == PanelState.HIDDEN
                        || newState == PanelState.COLLAPSED
                        || (previousState == PanelState.DRAGGING && newState == PanelState.ANCHORED && ppState == PanelState.EXPANDED))
                    animToolBar(false);
                else
                    animToolBar(true);
            }
        });
    }

    // 前前次的状态
    PanelState ppState = PanelState.COLLAPSED;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        unbinder = ButterKnife.bind(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbinder.unbind();
    }

    @Override
    public void onBackPressed() {
        if (slidingLayout != null &&
                (slidingLayout.getPanelState() == PanelState.EXPANDED || slidingLayout.getPanelState() == PanelState.ANCHORED)) {
            slidingLayout.setPanelState(PanelState.COLLAPSED);
            if (nowFragment != null) {
                svPostcontent.stopNestedScroll();
                svPostcontent.scrollTo(0, 0);
            }
        } else {
            super.onBackPressed();
        }
    }

    Animator upAnimator, downAnimator;
    boolean isShow = true;

    public void animToolBar(boolean isUP) {
        if (upAnimator != null && downAnimator != null &&
                (upAnimator.isRunning() || downAnimator.isRunning())) {
            return;
        }
        if (isUP && isShow) {
            // 向上滑动
            doUpAnimation();
        } else if (!isUP && !isShow) {
            // 向下滑动
            doDownAnimation();
        }
    }

    private void doUpAnimation() {
        isShow = false;
        upAnimator = new ObjectAnimator().ofFloat(myToolBar, "translationY", myToolBar.getTranslationY(), -myToolBar.getHeight());
        upAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {

            }

            @Override
            public void onAnimationEnd(Animator animator) {
                myToolBar.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationCancel(Animator animator) {

            }

            @Override
            public void onAnimationRepeat(Animator animator) {

            }
        });
        upAnimator.start();
        printLog("向上滑动");
    }

    private void doDownAnimation() {
        isShow = true;
        downAnimator = new ObjectAnimator().ofFloat(myToolBar, "translationY", myToolBar.getTranslationY(), 0);
        downAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {
                myToolBar.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animator animator) {
            }

            @Override
            public void onAnimationCancel(Animator animator) {

            }

            @Override
            public void onAnimationRepeat(Animator animator) {

            }
        });
        downAnimator.start();
        printLog("向下滑动");
    }

}