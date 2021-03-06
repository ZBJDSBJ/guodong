package com.guodong.sun.guodong.fragment;

import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.animation.LinearInterpolator;

import com.guodong.sun.guodong.R;
import com.guodong.sun.guodong.adapter.MeiziAdapter;
import com.guodong.sun.guodong.adapter.QiubaiAdapter;
import com.guodong.sun.guodong.base.AbsBaseFragment;
import com.guodong.sun.guodong.entity.qiubai.QiuShiBaiKe;
import com.guodong.sun.guodong.listener.OnRcvScrollListener;
import com.guodong.sun.guodong.presenter.presenterImpl.QiubaiPersenterImpl;
import com.guodong.sun.guodong.uitls.AppUtil;
import com.guodong.sun.guodong.uitls.SnackbarUtil;
import com.guodong.sun.guodong.view.IQiubaiView;
import com.guodong.sun.guodong.widget.CustomEmptyView;
import com.guodong.sun.guodong.widget.WrapContentLinearLayoutManager;
import com.melnykov.fab.FloatingActionButton;
import com.nineoldandroids.animation.ObjectAnimator;
import com.nineoldandroids.animation.ValueAnimator;

import java.util.ArrayList;

import butterknife.BindView;

/**
 * Created by Administrator on 2016/10/9.
 */

public class QiubaiFragment extends AbsBaseFragment implements IQiubaiView
{
    @BindView(R.id.meizi_swipe_refresh_layout)
    SwipeRefreshLayout mSwipeRefreshLayout;

    @BindView(R.id.meizi_recycler)
    RecyclerView mRecyclerView;

    @BindView(R.id.meizi_fb)
    FloatingActionButton mFButton;

    @BindView(R.id.empty_layout)
    CustomEmptyView mCustomEmptyView;

    //RecycleView是否正在刷新
    private boolean isRefreshing = false;
    private boolean isLoading;
    private int page = 1;

    private QiubaiAdapter mAdapter;
    private QiubaiPersenterImpl mQiubaiPresenter;
    private ObjectAnimator mAnimator;

    public static QiubaiFragment newInstance()
    {
        return new QiubaiFragment();
    }

    @Override
    protected int getLayoutResId()
    {
        return R.layout.fragment_meizi;
    }

    @Override
    protected void lazyLoad()
    {
        if (!isPrepared || !isVisible)
            return;
        showProgressBar();
        initRecyclerView();
        initFButton();
        isPrepared = false;
        mQiubaiPresenter.getQiubaiData(1);
    }

    private void initFButton()
    {
        mFButton.attachToRecyclerView(mRecyclerView);
        mFButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if (isRefreshing || isLoading)
                    return;
                mAnimator = ObjectAnimator.ofFloat(v, "rotation", 0F, 360F);
                mAnimator.setDuration(500);
                mAnimator.setInterpolator(new LinearInterpolator());
                mAnimator.setRepeatCount(ValueAnimator.INFINITE);
                mAnimator.setRepeatMode(ValueAnimator.RESTART);
                mAnimator.start();
                mRecyclerView.scrollToPosition(0);
                isRefreshing = true;
                mQiubaiPresenter.getQiubaiData(1);
            }
        });
    }

    private void initRecyclerView()
    {
        mAdapter = new QiubaiAdapter(getContext(), mRecyclerView);
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setLayoutManager(new WrapContentLinearLayoutManager(getContext()));
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView.addOnScrollListener(new OnRcvScrollListener()
        {
            @Override
            public void onBottom()
            {
                super.onBottom();
                if (!isLoading)
                {
                    isLoading = true;
                    page++;
                    loadMoreDate();
                }
            }
        });
    }

    private void loadMoreDate()
    {
        mAdapter.onLoadStart();
        mQiubaiPresenter.getQiubaiData(page);
    }

    @Override
    public void finishCreateView(Bundle state)
    {
        isPrepared = true;
        mQiubaiPresenter = new QiubaiPersenterImpl(getContext(), this, this.bindToLifecycle());
        lazyLoad();
    }

    @Override
    public void onDetach()
    {
        super.onDetach();
        mQiubaiPresenter.unsubcrible();
    }

    @Override
    public void updateQiubaiData(ArrayList<QiuShiBaiKe.Item> list)
    {
        hideEmptyView();
        mAdapter.addLists(list);
        mAdapter.onLoadFinish();
        isLoading = false;
    }

    @Override
    public void showProgressBar()
    {
        isRefreshing = true;
        mSwipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary);
        mSwipeRefreshLayout.setRefreshing(true);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener()
        {
            @Override
            public void onRefresh()
            {
                isRefreshing = true;
//                mAdapter.clearQiubaiList();
                mQiubaiPresenter.getQiubaiData(1);
            }
        });
    }

    @Override
    public void hideProgressBar()
    {
        if (mAnimator != null)
            mAnimator.cancel();
        if (mSwipeRefreshLayout != null)
            mSwipeRefreshLayout.setRefreshing(false);
        isLoading = false;
        isRefreshing = false;
    }

    @Override
    public void showError(String error)
    {
        initEmptyView();
    }

    public void hideEmptyView()
    {
        mCustomEmptyView.setVisibility(View.GONE);
    }

    public void initEmptyView()
    {
        if (!AppUtil.isNetworkConnected())
        {
            SnackbarUtil.showMessage(mRecyclerView, getString(R.string.noNetwork));
        }
        else
        {
            mSwipeRefreshLayout.setRefreshing(false);
            mCustomEmptyView.setVisibility(View.VISIBLE);
            mCustomEmptyView.setEmptyImage(R.drawable.img_tips_error_load_error);
            mCustomEmptyView.setEmptyText(getString(R.string.loaderror));
            SnackbarUtil.showMessage(mRecyclerView, getString(R.string.noNetwork));
            mCustomEmptyView.reload(new CustomEmptyView.ReloadOnClickListener()
            {
                @Override
                public void reloadClick()
                {
                    mQiubaiPresenter.getQiubaiData(1);
                }
            });
        }
    }
}
