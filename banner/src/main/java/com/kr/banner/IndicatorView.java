package com.kr.banner;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;

import androidx.annotation.Nullable;

public final class IndicatorView extends ViewGroup {


    private int mCount = 0;
    private int mCurrentPosition = 0;
    private float mIndicatorWidth;
    private Paint mPaint;
    private ValueAnimator mAnime;
    private float mIndicatorLeft = 0;


    public IndicatorView(Context context) {
        super(context);
    }

    public IndicatorView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public IndicatorView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);

    }

    private void init(Context context, @Nullable AttributeSet attrs) {
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.IndicatorView);
        mCount = ta.getInteger(R.styleable.IndicatorView_count, 0);
        mIndicatorWidth = (int) ta.getDimension(R.styleable.IndicatorView_indicatorWidth, 30);
        mCurrentPosition = ta.getInteger(R.styleable.IndicatorView_defaultPosition, 0);
        mPaint = new Paint();
        mPaint.setColor(ta.getColor(R.styleable.IndicatorView_indicatorColor, getResources().getColor(R.color.white)));
        mPaint.setStyle(Paint.Style.FILL);
        ta.recycle();
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {


        float result = 0;
        int specMode = MeasureSpec.getMode(widthMeasureSpec);
        int specSize = MeasureSpec.getSize(widthMeasureSpec);

        float defaultWidth = mCount * mIndicatorWidth; //默认大小为指示器宽度乘以数量

        switch (specMode) {
            case MeasureSpec.UNSPECIFIED:
                result = defaultWidth;
                break;
            case MeasureSpec.AT_MOST:
            case MeasureSpec.EXACTLY:
                if (defaultWidth > specSize) {
                    result = specSize;
                    mIndicatorWidth = result / mCount;
                }
                result = defaultWidth;
                break;
        }

        setMeasuredDimension((int) result,
                getDefaultSize(getSuggestedMinimumHeight(), heightMeasureSpec));
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        mIndicatorLeft = mCurrentPosition * mIndicatorWidth;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawRect(mIndicatorLeft,0f,mIndicatorLeft + mIndicatorWidth,  getHeight(),mPaint);
    }

    public void setCount(int count) {
        mCount = count;
        requestLayout();
    }



    public void moveTo(int pos) {
        if (pos > mCount || pos < 0) {
            throw new IndexOutOfBoundsException("滑动的范围不能<0或者大于其范围");
        }
        if(mAnime!= null && mAnime.isRunning()){
            mAnime.cancel();
        }
        mAnime= ValueAnimator.ofFloat(mIndicatorLeft, pos*mIndicatorWidth);
        mAnime.setDuration(500);
        mAnime.setInterpolator(new LinearInterpolator());
        mAnime.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mIndicatorLeft = (float) animation.getAnimatedValue();
                invalidate();
            }
        });
        mAnime.start();
        mCurrentPosition = pos;

    }





}
