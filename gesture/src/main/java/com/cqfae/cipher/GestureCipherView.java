package com.cqfae.cipher;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Vibrator;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import com.cqfae.cipher.gesture.R;


/**
 * @Description: 手势密码控件
 * @Author: huashigen370
 * @CreateDate: 2018/8/17 上午10:26
 * @UpdateUser: 更新者
 * @UpdateDate: 2018/8/17 上午10:26
 * @UpdateRemark: 更新说明
 * @Version: 1.0
 */
public class GestureCipherView extends View {
    private static final String TAG = "GestureCipherView";
    //保存9个圆点的坐标等信息
    private Point[] mPoints = new Point[9];
    //保存上一个被选中的圆点的位置
    private int mLastPointI = -1;
    //保存当前手指位置的x坐标
    private float mCurPositionX = 0.0f;
    //保存当前手指位置的Y坐标
    private float mCurPositionY = 0.0f;
    //画布
    private Paint mPaint;

    /**
     * 属性值
     **/
    //最少连接点
    private int minTracePoint;
    //默认最少连接点
    private static final int DEFAULT_MIN_TRACE_POINT = 4;
    //圆点未被选中时的颜色
    private int mUnSelectColor;
    //圆点被选中时的颜色
    private int mSelectColor;
    //轨迹错误显示的颜色
    private int mErrorColor;
    //绘制选中时的颜色
    private int mDrawSelectColor;
    //绘制未选中时的颜色
    private int mDrawUnSelectColor;
    //是否显示错误轨迹
    private boolean mShowErrorTrace;
    //内部圆半径
    private float mInnerOvalRadius;
    //外部圆半径
    private float mOutOvalRadius;
    //默认内部圆半径
    private static final float DEFAULT_INNER_OVAL_RADIUS = 10f;//dp
    //默认外部圆半径
    private static final float DEFAULT_OUT_OVAL_RADIUS = 30f;//dp
    private float mDefaultPadding = 30f;//dp
    //线条的尺寸
    private float mLineWidth;
    private static final float DEFAULT_LINE_WIDTH = 4f;//dp
    //圆线条的尺寸
    private float mCircleLineWidth;
    private static final float DEFAULT_CIRCLE_LINE_WIDTH = 2f;//dp
    //默认颜色
    private static final int DEFAULT_COLOR = Color.parseColor("#68b968");
    //默认错误颜色
    private static final int DEFAULT_ERROR_COLOR = Color.parseColor("#d90015");
    //连接到点是否震动
    private boolean mVibrate;
    //控制震动
    private Vibrator mVibrator;
    //记录轨迹字符串
    private StringBuilder traceCode;
    //记录手指是否离开屏幕
    private boolean actionUp = false;

    private TraceListener traceListener;

    public GestureCipherView(Context context) {
        this(context, null);
    }

    public GestureCipherView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public GestureCipherView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    public TraceListener getTraceListener() {
        return traceListener;
    }

    public void setTraceListener(TraceListener traceListener) {
        this.traceListener = traceListener;
    }

    /**
     * 初始化属性
     */
    private void init(Context context, AttributeSet attrs) {
        //读取布局文件中设置的属性
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.gesture);
        if (typedArray != null) {
            mVibrate = typedArray.getBoolean(R.styleable.gesture_vibrate, false);
            minTracePoint = typedArray.getInteger(R.styleable.gesture_minTracePoint, DEFAULT_MIN_TRACE_POINT);
            mUnSelectColor = typedArray.getColor(R.styleable.gesture_unSelectColor, DEFAULT_COLOR);
            mSelectColor = typedArray.getColor(R.styleable.gesture_selectColor, DEFAULT_COLOR);
            mErrorColor = typedArray.getColor(R.styleable.gesture_errorColor, DEFAULT_ERROR_COLOR);
            mInnerOvalRadius = typedArray.getDimension(R.styleable.gesture_innerRadius, getResources().getDisplayMetrics().density * DEFAULT_INNER_OVAL_RADIUS);
            mOutOvalRadius = typedArray.getDimension(R.styleable.gesture_outRadius, getResources().getDisplayMetrics().density * DEFAULT_OUT_OVAL_RADIUS);
            mLineWidth = typedArray.getDimension(R.styleable.gesture_lineWidth, getResources().getDisplayMetrics().density * DEFAULT_LINE_WIDTH);
            mCircleLineWidth = typedArray.getDimension(R.styleable.gesture_circleLineWidth, getResources().getDisplayMetrics().density * DEFAULT_CIRCLE_LINE_WIDTH);
            mShowErrorTrace = typedArray.getBoolean(R.styleable.gesture_showErrorTrace, true);
        }
        typedArray.recycle();

        mDefaultPadding = getResources().getDisplayMetrics().density * mDefaultPadding;

        mDrawSelectColor = mSelectColor;
        mDrawUnSelectColor = mUnSelectColor;
        //初始化画布
        mPaint = new Paint();
        mPaint.setColor(mDrawUnSelectColor);
        mPaint.setFlags(Paint.ANTI_ALIAS_FLAG);
        mPaint.setStrokeWidth(mLineWidth);

        //初始化9个点
        for (int i = 0; i < 9; i++) {
            Point point = new Point();
            point.setLastPostIndex(-1);
            mPoints[i] = point;
        }
        traceCode = new StringBuilder();
        //初始化震动API
        if (mVibrate) {
            mVibrator = (Vibrator) context.getSystemService(context.VIBRATOR_SERVICE);
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        //wrap_content时的处理
        int finalSize;
        int widthMeasureMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightMeasureMode = MeasureSpec.getMode(heightMeasureSpec);
        if (widthMeasureMode == MeasureSpec.AT_MOST || heightMeasureMode == MeasureSpec.AT_MOST) {
            int widthMost = MeasureSpec.getSize(widthMeasureSpec);
            int heightMost = MeasureSpec.getSize(heightMeasureSpec);
            finalSize = Math.min(widthMost, heightMost);
            //自定义的一个较为合适的尺寸
            int properSize = (int) ((mOutOvalRadius + mCircleLineWidth) * 2 * 3 + getPaddingLeft() + getPaddingRight() + mDefaultPadding * 2);
            finalSize = Math.min(properSize, finalSize);
            int newWidthMeasureSpec, newHeightMeasureSpec;
            if (widthMeasureMode == MeasureSpec.AT_MOST) {
                newWidthMeasureSpec = MeasureSpec.makeMeasureSpec(finalSize, widthMeasureMode);
            } else {
                newWidthMeasureSpec = widthMeasureSpec;
            }
            if (heightMeasureMode == MeasureSpec.AT_MOST) {
                newHeightMeasureSpec = MeasureSpec.makeMeasureSpec(finalSize, heightMeasureMode);
            } else {
                newHeightMeasureSpec = heightMeasureSpec;
            }
            super.onMeasure(newWidthMeasureSpec, newHeightMeasureSpec);
        } else {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        }
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        int width = getWidth() - getPaddingLeft() - getPaddingRight();
        int height = getHeight() - getPaddingBottom() - getPaddingTop();

        int size = Math.min(width, height);
        //整个控件居中的margin
        float marginGlobal = 0;
        if (size < width) {
            marginGlobal = (width - size) / 2;
        }
        //圆圈之间的margin
        float margin = (size - (mOutOvalRadius + mCircleLineWidth) * 2 * 3) / 2;
        //初始化9个圆圈的位置
        int paddingLeft = getPaddingLeft();
        int paddingTop = getPaddingTop();
        float singleCircleSize = (mOutOvalRadius + mCircleLineWidth) * 2;
        for (int i = 0; i < 9; i++) {
            Point point = mPoints[i];
            point.setX(marginGlobal + paddingLeft + margin * (i % 3) + singleCircleSize / 2 + (i % 3) * singleCircleSize);
            point.setY(paddingTop + margin * (i / 3) + singleCircleSize / 2 + (i / 3) * singleCircleSize);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        for (int i = 0; i < 9; i++) {
            Point point = mPoints[i];
            //1、已连接的点之间要画线
            if (point.getLastPostIndex() != -1) {
                //有上一个连接点,两点之间画一条线
                Point startPoint = mPoints[point.getLastPostIndex()];
                Point endPoint = point;
                float startX = startPoint.getX();
                float startY = startPoint.getY();
                float endX = endPoint.getX();
                float endY = endPoint.getY();
                mPaint.setStrokeWidth(mLineWidth);
                mPaint.setColor(mDrawSelectColor);
                canvas.drawLine(startX, startY, endX, endY, mPaint);
            }
            //2、画9个圆圈-需要根据是否被选中区分
            //1)选中的画法
            if (point.isSelected()) {
                RectF rectFInner = new RectF(point.getX() - mInnerOvalRadius, point.getY() - mInnerOvalRadius, point.getX() + mInnerOvalRadius, point.getY() + mInnerOvalRadius);
                //画内心
                mPaint.setStyle(Paint.Style.FILL);
                mPaint.setColor(mDrawSelectColor);
                mPaint.setStrokeWidth(mCircleLineWidth);
                canvas.drawOval(rectFInner, mPaint);
                //画外圈
                mPaint.setStyle(Paint.Style.STROKE);
                canvas.drawCircle(point.getX(), point.getY(), mOutOvalRadius, mPaint);
            } else {
                //2)未选中的画法
                mPaint.setStyle(Paint.Style.STROKE);
                mPaint.setStrokeWidth(mCircleLineWidth);
                mPaint.setColor(mDrawUnSelectColor);
                canvas.drawCircle(point.getX(), point.getY(), mOutOvalRadius, mPaint);
            }
        }
        //手指离开屏幕后,不再画动态的线
        if (actionUp) {
            return;
        }
        //3、即将连接的点跟随手指画线
        //起始点-最后一个被选中的点
        float startX;
        float startY;
        if (mLastPointI != -1) {
            startX = mPoints[mLastPointI].getX();
            startY = mPoints[mLastPointI].getY();
            //目标点手指停留的点
            float endX = mCurPositionX;
            float endY = mCurPositionY;
            mPaint.setStrokeWidth(mLineWidth);
            mPaint.setColor(mUnSelectColor);
            canvas.drawLine(startX, startY, endX, endY, mPaint);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        boolean bool = false;
        int action = event.getAction();
        float x = event.getX();
        float y = event.getY();
        Log.d(TAG, "onTouchEvent: x:" + x);
        Log.d(TAG, "onTouchEvent: y:" + y);
        mCurPositionX = x;
        mCurPositionY = y;
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                if (!isEnabled()) {
                    return false;
                }
                actionUp = false;
                bool = handleTouchEvent(x, y);
                invalidate();
                break;
            case MotionEvent.ACTION_MOVE:
                bool = handleTouchEvent(x, y);
                invalidate();
                break;
            case MotionEvent.ACTION_UP:
                actionUp = true;
                traceFinish();
                break;
        }
        return bool;
    }

    /**
     * 滑动轨迹完成
     */
    private void traceFinish() {
        if (traceListener != null) {
            String traceCodeStr = traceCode.toString();
            if (TextUtils.isEmpty(traceCodeStr) || traceCodeStr.length() < minTracePoint) {
                traceListener.onTraceUnFinish();
            } else {
                traceListener.onTraceFinished(traceCodeStr);
            }
        }
        if (mShowErrorTrace) {
            showErrorTraces();
        } else {
            clearTraces();
        }
    }

    /**
     * 处理手指事件
     *
     * @param x
     * @param y
     * @return
     */
    private boolean handleTouchEvent(float x, float y) {
        int curPointI = getPoint(x, y);
        if (curPointI != -1) {
            Point curPoint = mPoints[curPointI];
            if (curPointI != mLastPointI && !curPoint.isSelected()) {
                curPoint.setLastPostIndex(mLastPointI);
                mLastPointI = curPointI;
                curPoint.setSelected(true);
                //调用震动
                if (mVibrator != null) {
                    mVibrator.vibrate(50);
                }
                traceCode.append(curPointI + "");
            }
            return true;
        } else {
            return false;
        }
    }

    //计算落点
    private int getPoint(float locX, float locY) {
        //首先判断触点有没有落在大范围的九宫格内
        Point pointFirst = mPoints[0];
        float left = pointFirst.getX() - mOutOvalRadius;
        float top = pointFirst.getY() - mOutOvalRadius;
        if (locX < left || locY < top) {
            return -1;
        }
        Point pointLast = mPoints[8];
        float right = pointLast.getX() + mOutOvalRadius;
        float bottom = pointLast.getY() + mOutOvalRadius;
        if (locX > right || locY > bottom) {
            return -1;
        }
        //触点在九宫格内,找出在具体哪个小格内
        int targetPoint = -1;
        for (int i = 0; i < 9; i++) {
            float l = mPoints[i].getX() - mOutOvalRadius;
            float t = mPoints[i].getY() - mOutOvalRadius;
            float r = mPoints[i].getX() + mOutOvalRadius;
            float b = mPoints[i].getY() + mOutOvalRadius;
            if (locX > l && locX < r && locY > t && locY < b) {
                targetPoint = i;
                break;
            }
        }
        return targetPoint;
    }

    /**
     * 重置每个点的状态
     */
    private void resetPointStatus() {
        for (int i = 0; i < 9; i++) {
            mPoints[i].setSelected(false);
            mPoints[i].setLastPostIndex(-1);
        }
    }

    /**
     * 清除轨迹
     */
    public void clearTraces() {
        resetPointStatus();
        mLastPointI = -1;
        mCurPositionX = 0.0f;
        mCurPositionY = 0.0f;
        traceCode = new StringBuilder();
        invalidate();
    }

    /**
     * 手势密码验证错误显示错误轨迹1秒后消失
     */
    public void showErrorTraces() {
        mDrawSelectColor = mErrorColor;
        invalidate();
        setEnabled(false);
        //恢复绘制颜色
        postDelayed(new Runnable() {
            @Override
            public void run() {
                mDrawSelectColor = mSelectColor;
                mDrawUnSelectColor = mUnSelectColor;
                clearTraces();
                invalidate();
                setEnabled(true);
            }
        }, 1000);
    }
}
