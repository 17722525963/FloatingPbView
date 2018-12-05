package com.zsrun.floatingpbview.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import com.zsrun.floatingpbview.BitmapUtil;
import com.zsrun.floatingpbview.R;

/**
 * Created by zsrun on 2018/11/19 09:48
 */
public class FloatingPbView extends View {

    private static final String TAG = "FloatingPbView";

    private boolean flag = false;

    private Paint mPaint;//画笔

    private Bitmap mBitmapBeford;
    private Bitmap mBitmapAfter;
    private Bitmap mBitmapBackground;

    //外部阴影色
    private int mOutShadeColor = Color.GRAY;

    //进度条颜色
    private int mLineColor = Color.BLUE;

    //进度条宽度
    private int mStrokeWidth = 8;

    //总进度
    private int mCountProgress = 100;

    //当前进度
    private int mCurrentProgress = 0;

    //设置总时间
    private int mTotalTime = 30000;//30S

    private int mAwardTextSize = 36;//奖励字体大小

    //这里如无（报错），可填入对应替代图片地址
    private int mImageBefore = R.drawable.fb_red_package;
    private int mImageAfter = R.drawable.fb_award;
    private int mBackground = R.drawable.fb_background;

    private int mWidthWithProgressToBackground = 0;//背景外边到进度条外边的宽度

    private String mAwardText = "10";//设置奖励金币

    private OnProgressListener mOnProgressListener;//进度监听

    //View的显示区域
    private final Rect mBounds = new Rect();

    public FloatingPbView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initialize(context, attrs);
    }

    /**
     * 初始化
     */
    private void initialize(Context context, AttributeSet attributeSet) {
        TypedArray typedArray = context.obtainStyledAttributes(attributeSet, R.styleable.FloatingPbView);
        mLineColor = typedArray.getColor(R.styleable.FloatingPbView_pbLineColor, mLineColor);
        mStrokeWidth = typedArray.getInt(R.styleable.FloatingPbView_pbStrokeWidth, mStrokeWidth);
        typedArray.recycle();
    }

    /**
     * 设置奖励金币数
     */
    public void setAwardText(String awardText) {
        this.mAwardText = awardText;
        invalidate();
    }

    /**
     * 设置外圈阴影颜色
     */
    public void setOutShadeColor(int outShadeColor) {
        this.mOutShadeColor = outShadeColor;
        invalidate();//通知onDraw 重绘View
    }

    /**
     * 设置进度条颜色
     */
    public void setLineColor(int lineColor) {
        this.mLineColor = lineColor;
        invalidate();
    }

    /**
     * 设置进度条宽度
     */
    public void setStrokeWidth(int strokeWidth) {
        this.mStrokeWidth = strokeWidth;
        invalidate();
    }

    /**
     * 设置 背景外边框 到进度条外边框之间的距离
     */
    public void setWidthWithProgressToBackground(int widthWithProgressToBackground) {
        this.mWidthWithProgressToBackground = widthWithProgressToBackground;
        invalidate();
    }

    /**
     * 设置总进度 默认100
     */
    public void setCountProgress(int countProgress) {
        this.mCountProgress = countProgress;
        invalidate();
    }

    /**
     * 设置当前进度
     */
    public void setCurrentProgress(int currentProgress) {
        this.mCurrentProgress = currentProgress;
        invalidate();
    }

    /**
     * 设置进度条旋转一圈 总时间
     */
    public void setTotalTime(int totalTime) {
        this.mTotalTime = totalTime;
        invalidate();
    }

    /**
     * 设置进度监听
     */
    public void setOnProgressListener(OnProgressListener onProgressListener) {
        this.mOnProgressListener = onProgressListener;
    }

    /**
     * 设置中间显示图片
     */
    public void setImageBefore(int imageBefore) {
        this.mImageBefore = imageBefore;
        invalidate();
    }

    /**
     * 设置背景图片
     */
    public void setBackground(int background) {
        this.mBackground = background;
        invalidate();
    }

    /**
     * 设置完成一圈后显示图片
     */
    public void setImageAfter(int imageAfter) {
        this.mImageAfter = imageAfter;
        invalidate();
    }

    /**
     * 设置奖励字体大小
     */
    public void setAwardTextSize(int awardTextSize) {
        this.mAwardTextSize = awardTextSize;
        invalidate();
    }

    /**
     * 开始
     */
    public void start() {
        stop();
        post(runnable);
    }

    public void reStart() {
        //重置进度
        mCurrentProgress = 0;
        start();
    }

    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            removeCallbacks(this);//停止线程
            mCurrentProgress += 1; //每次加1
            invalidate();
            postDelayed(runnable, mTotalTime / mCountProgress);
            if (mCurrentProgress >= mCountProgress) {
                mCurrentProgress = 0;
                invalidate();
                postDelayed(runnable, mTotalTime / mCountProgress);
            }

            if (mOnProgressListener != null)
                mOnProgressListener.onProgress(mCurrentProgress);
        }
    };

    /**
     * 停止
     */
    public void stop() {
        removeCallbacks(runnable);
    }


    @SuppressLint("DrawAllocation")
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        //获取 view 的边界
        getDrawingRect(mBounds);

        mPaint = new Paint();
        mPaint.setColor(mLineColor);
        mPaint.setStyle(Paint.Style.STROKE);//设置画笔为空心圆
        mPaint.setStrokeWidth(mStrokeWidth);//设置画笔粗细
        mPaint.setAntiAlias(true);//抗锯齿

        //先画背景图
        mBitmapBackground = BitmapUtil.getBitmapFromResource(getContext(), mBackground);
        RectF rectF = new RectF(mBounds.left + mStrokeWidth, mBounds.top + mStrokeWidth, mBounds.right - mStrokeWidth, mBounds.bottom - mStrokeWidth);//确定外切矩形范围
        canvas.drawBitmap(mBitmapBackground, null, rectF, mPaint);

        //装载progress的矩形区域
        int width = mWidthWithProgressToBackground == 0 ? mBounds.right / 6 : mWidthWithProgressToBackground;
        RectF rectFCircle = new RectF(mBounds.left + mStrokeWidth + width, mBounds.top + mStrokeWidth + width, mBounds.right - mStrokeWidth - width, mBounds.bottom - mStrokeWidth - width);//确定外切矩形范围

        //rectF:矩形区域  startAngle:开始角度，时钟3点钟方向为0度  sweepAngle:扫过角度，以时钟3点钟方向为0度  useCenter:是否包含圆心
        //阴影圆
        mPaint.setColor(mOutShadeColor);
        canvas.drawArc(rectFCircle, 0, 360, false, mPaint);

        mPaint.setColor(mLineColor);
        canvas.drawArc(rectFCircle, 0, 360F / mCountProgress * mCurrentProgress, false, mPaint);//转为Float类型进行计算，否则可能导致被int类型丢失精度

        mBitmapBeford = BitmapUtil.getBitmapFromResource(getContext(), mImageBefore, mBounds.right / 3, mBounds.bottom / 3);
        mBitmapAfter = BitmapUtil.getBitmapFromResource(getContext(), mImageAfter, mBounds.right / 6, mBounds.bottom / 6);

        /**
         * 获取bitmap 的实际宽高  和 外边界的实际宽度   确定bitmap的起始位置  为 外边界宽度 - bitmap 宽度  除以二    高度类似
         * 作用：使图片居中显示
         */

        /**
         * 判断到99 后1-9 展示 奖励页面 (奖励页面需要绘制矩形进行展示，需要对外暴露方法传入奖励数值)
         *
         */
        if (mCurrentProgress == 99) {
            flag = true;
        }
        if (mCurrentProgress == 99 || mCurrentProgress < 10) {
            if (flag) {
                //装载奖励页面
                canvas.drawBitmap(mBitmapAfter, (mBounds.right - mBitmapAfter.getWidth()) / 3, (mBounds.bottom - mBitmapAfter.getHeight()) / 2, mPaint);
                mPaint.setColor(Color.RED);
                mPaint.setTextSize(mAwardTextSize);
                mPaint.setStyle(Paint.Style.FILL);
                mPaint.setStrokeWidth(60);
                mPaint.setAntiAlias(true);
                mPaint.setDither(true);//设置是否使用图像抖动处理。会使绘制的图片等颜色更加的清晰以及饱满。
                canvas.drawText("+", (float) (mBounds.right / 2 - mAwardTextSize / 4), (float) (mBounds.bottom / 2) + (float) (mAwardTextSize / 3), mPaint);
                mPaint.setTextSize(mAwardTextSize);
                canvas.drawText(mAwardText, (mBounds.right / 2 - (float) (mAwardTextSize / 2) + mBounds.right / 8), (float) (mBounds.bottom / 2) + (float) (mAwardTextSize / 3), mPaint);
            } else {
                canvas.drawBitmap(mBitmapBeford, (mBounds.right - mBitmapBeford.getWidth()) / 2, (mBounds.bottom - mBitmapBeford.getHeight()) / 2, mPaint);
            }
        } else {
            canvas.drawBitmap(mBitmapBeford, (mBounds.right - mBitmapBeford.getWidth()) / 2, (mBounds.bottom - mBitmapBeford.getHeight()) / 2, mPaint);
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        /**
         * 获取宽的模式
         * 三种模式：
         * UNSPECIFIED 未指定的   父元素不对子元素施加任何束缚
         * EXACTLY     完全       父元素决定子元素的确切大小，子元素将被限定在给定的边界里而忽略他的本身大小
         * AT_MOST     至多       子元素至多达到指定大小的值
         *
         */
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        /**
         * 获取宽的大小 PX值
         */
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);

        int heightMode = MeasureSpec.getMode(heightMeasureSpec);

        //PX值   DP = PX/(PPI/160)
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);


        //定义需要设置的宽和高
        int width = 10;
        int height = 10;

        //宽度处理
        if (widthMode == MeasureSpec.EXACTLY) {
            //如果是match_parent 或者是 具体的值，直接进行赋值
            //TODO 此处是否需要 进行转换？像素值 和DP值
            width = widthSize;
        } else if (widthMode == MeasureSpec.AT_MOST) {//wrap_content处理
            /**
             * 获取控件的宽度  + 控件的左右padding值    margin  不考虑
             */
            float mViewWidth = mBounds.width();
            width = (int) (mViewWidth + getPaddingLeft() + getPaddingRight());
        }

        //高度处理
        if (heightMode == MeasureSpec.EXACTLY) {
            //TODO 像素值判断转化处理
            height = heightSize;
        } else if (heightMode == MeasureSpec.AT_MOST) {//wrap_content
            /**
             * 获取控件的高度 + 上下 padding
             */
            float textHeight = mBounds.height();
            height = (int) (getPaddingTop() + textHeight + getPaddingBottom());
        }

        //设置宽度、高度
        setMeasuredDimension(width, height);
    }

    /**
     * 进度监听
     */
    public interface OnProgressListener {

        void onProgress(int progress);

    }
}
