package com.kr.banner;

import android.content.Context;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import java.util.ArrayList;
import java.util.List;

/**
 * Banner 广告banner，支持页面动画，循环转动，指示器，自动播放
 */
public final class Banner {

    private static final int DEFAULT_CHANGE_PAGE_TIME = 5 * 1000;

    private IndicatorView mIndicatorView;

    private List<?> mItems;

    private int mItemViewId = -1;
    private Context mContext;
    private ViewPager mViewPager;

    private ViewPager.PageTransformer mPageTransformer;

    private final Handler mHandler = new Handler();

    private final List<View> mCacheViews = new ArrayList<>();

    private BindViewListener mBindViewListener;

    private int mNextPageTime;

    private final Runnable mTask = new Runnable() {
        @Override
        public void run() {
            mViewPager.setCurrentItem(mViewPager.getCurrentItem() + 1, true);
        }
    };

    public interface BindViewListener {
        void onBind(View view, int pos);
    }

    private Banner() {
    }

    private Banner(Builder builder) {
        mItems = builder.mItems;
        mItemViewId = builder.mItemViewId;
        mPageTransformer = builder.mPageTransformer;
        mIndicatorView = builder.mIndicatorView;
        mViewPager = builder.mViewPager;
        mBindViewListener = builder.mBindViewListener;
        mNextPageTime = builder.mNextPageTime;

        if (mNextPageTime == 0) {
            mNextPageTime = DEFAULT_CHANGE_PAGE_TIME;
        }


        mContext = mViewPager.getContext();
        if (mIndicatorView != null) {
            mIndicatorView.setCount(mItems.size());
        }

    }

    private void init() {
        mCacheViews.clear();
        for (int i = 0; i < 6; i++) {
            mCacheViews.add(LayoutInflater.from(mContext).inflate(mItemViewId, null));
        }
        mViewPager.setAdapter(new PagerAdapter() {

            @Override
            public int getCount() {
                return Integer.MAX_VALUE;
            }

            @Override
            public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
                return view == object;
            }

            @NonNull
            @Override
            public Object instantiateItem(@NonNull ViewGroup container, int position) {
                int pos = position % mCacheViews.size();
                Log.d("PagerAdapter", "add pos:" + pos);
                View v = mCacheViews.get(pos);
                container.addView(v);
                if (mBindViewListener != null) {
                    mBindViewListener.onBind(v, position % mItems.size());
                }
                return v;
            }

            @Override
            public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
                int pos = position % mCacheViews.size();
                Log.d("PagerAdapter", "remove pos:" + pos);
                container.removeView((View) object);
            }
        });
        mViewPager.setOffscreenPageLimit(2);
        mViewPager.setCurrentItem(Integer.MAX_VALUE / 2 - Integer.MAX_VALUE / 2 % mCacheViews.size());
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {

                if (mIndicatorView != null) {
                    mIndicatorView.moveTo(position % mItems.size());
                }

            }

            @Override
            public void onPageScrollStateChanged(int state) {

                switch (state) {
                    case ViewPager.SCROLL_STATE_IDLE:
                        mHandler.postDelayed(mTask, mNextPageTime);
                        break;
                    case ViewPager.SCROLL_STATE_DRAGGING:
                        mHandler.removeCallbacks(mTask);
                        break;
                    case ViewPager.SCROLL_STATE_SETTLING:
                        break;
                }

            }
        });

        if (mPageTransformer == null) {
            mViewPager.setPageTransformer(false, new ViewPager.PageTransformer() {
                @Override
                public void transformPage(@NonNull View page, float position) {
                    float SCALE_MAX = 0.8f;
                    float ALPHA_MAX = 0.5f;

                    float scale = (position < 0) ? ((1 - SCALE_MAX) * position + 1) : ((SCALE_MAX - 1) * position + 1);
                    float alpha = (position < 0) ? ((1 - ALPHA_MAX) * position + 1) : ((ALPHA_MAX - 1) * position + 1);
                    //为了滑动过程中，page间距不变，这里做了处理
                    if (position < 0) {
                        page.setPivotX(page.getWidth());
                        page.setPivotY(page.getHeight() / 2f);
                    } else {
                        page.setPivotX(0f);
                        page.setPivotY(page.getHeight() / 2f);
                    }
                    page.setScaleX(scale);
                    page.setScaleY(scale);
                    page.setAlpha(Math.abs(alpha));
                }
            });
        } else {
            mViewPager.setPageTransformer(false, mPageTransformer);
        }


    }

    public void start() {
        init();
        mHandler.postDelayed(mTask, mNextPageTime);
    }


    public static final class Builder {

        IndicatorView mIndicatorView;
        ViewPager mViewPager;

        List<?> mItems;

        int mItemViewId;
        ViewPager.PageTransformer mPageTransformer;
        BindViewListener mBindViewListener;
        int mNextPageTime;


        public Builder setItems(@NonNull List<?> items) {
            mItems = items;
            return this;
        }

        public Builder setPageIndicator(@Nullable IndicatorView indicatorView) {
            mIndicatorView = indicatorView;
            return this;
        }

        public Builder setBindViewListener(@Nullable BindViewListener bindViewListener) {
            mBindViewListener = bindViewListener;
            return this;
        }

        public Builder setImtemViewId(@LayoutRes int itemViewId) {
            mItemViewId = itemViewId;
            return this;
        }

        public Builder setViewPager(@NonNull ViewPager viewPager) {
            mViewPager = viewPager;
            return this;
        }


        public Builder setPageTransformer(@Nullable ViewPager.PageTransformer pageTransformer) {
            mPageTransformer = pageTransformer;
            return this;
        }

        public Builder setRotatePageTime(int time) {
            mNextPageTime = time;
            return this;
        }

        public Banner build() {

            if (mItems == null) {
                throw new NullPointerException("items can't be null");
            }
            if (mItemViewId == -1) {
                throw new NullPointerException("pls set item View");
            }
            if (mViewPager == null) {
                throw new NullPointerException("viewpager can't be null");
            }


            return new Banner(this);
        }


    }

}
