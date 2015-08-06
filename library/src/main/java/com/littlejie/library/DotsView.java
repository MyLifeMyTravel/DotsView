package com.littlejie.library;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

/**
 * 构造方法->onMesure->onDraw
 * Created by Lion on 2015/8/5 0005.
 */
public class DotsView extends View {

    private static final String TAG = "dots";

    //默认文字未选中颜色
    private static final int DEFAULT_TEXT_UNSELECTED_COLOR = R.color.dots_text_unselect;
    //默认文字选中颜色
    private static final int DEFAULT_TEXT_SELECTED_COLOR = R.color.dots_text_select;
    //默认未选中颜色
    private static final int DEFAULT_UNSELECT_COLOR = R.color.dots_unselect;
    //默认选中颜色
    private static final int DEFAULT_SELECT_COLOR = R.color.dots_select;
    //画笔
    private Paint paint;
    //屏幕宽度、高度
    private int measureWidth, measureHeight;
    //dots width、height
    private int radius;
    //line width、height
    private int lineWidth;
    //dots选中位置
    private int position;
    //显示dots个数,lineNum = dotsNum - 1
    private int dotsNum;
    //边距
    private int padding;
    //文字与Dot间的间距
    private int dotTextMargin;
    //文字大小
    private float textSize;
    //文字高度
    private float textHeight;
    //dots位置数组
    private float[] dotPos;
    //Dots点击监听器
    private OnDotsClickListener onDotsClickListener;
    //数据
    private List<String> content;

    public DotsView(Context context) {
        this(context, null);
    }

    public DotsView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DotsView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        TypedArray typedArray = context.obtainStyledAttributes(attrs,
                R.styleable.DotsView);
        radius = typedArray.getInt(R.styleable.DotsView_ringRadius, 40);
        dotsNum = typedArray.getInt(R.styleable.DotsView_dotsNum, 4);
        padding = typedArray.getInt(R.styleable.DotsView_dotsPadding, 60);
        dotTextMargin = typedArray.getInt(R.styleable.DotsView_dotTextMargin, 150);
        textSize = typedArray.getFloat(R.styleable.DotsView_dotTextSize, 50);
        //dots选中位置，默认为选中最后一个
        position = typedArray.getInt(R.styleable.DotsView_dotSelected, dotsNum);

        paint = new Paint();
        dotPos = new float[dotsNum];
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        setMeasuredDimension(measureWidth(widthMeasureSpec), measureHeight(heightMeasureSpec));
        this.measureWidth = measureWidth(widthMeasureSpec);
        this.measureHeight = measureHeight(heightMeasureSpec);
        //计算直线的长度
        lineWidth = (measureWidth - dotsNum * radius * 2 - padding * 2) / (dotsNum - 1);
    }

    /**
     * Determines the width of this view
     *
     * @param measureSpec A measureSpec packed into an int
     * @return The width of the view, honoring constraints from measureSpec
     */
    private int measureWidth(int measureSpec) {
        int result = 0;
        int specMode = MeasureSpec.getMode(measureSpec);
        int specSize = MeasureSpec.getSize(measureSpec);

        if (specMode == MeasureSpec.EXACTLY) {
            // We were told how big to be
            result = specSize;
        } else {
            // Measure the text
            result = lineWidth * (dotsNum - 1) + radius * 2 * dotsNum + getPaddingLeft()
                    + getPaddingRight();
            if (specMode == MeasureSpec.AT_MOST) {
                // Respect AT_MOST value if that was what is called for by measureSpec
                result = Math.min(result, specSize);
            }
        }

        return result;
    }

    /**
     * Determines the height of this view
     *
     * @param measureSpec A measureSpec packed into an int
     * @return The height of the view, honoring constraints from measureSpec
     */
    private int measureHeight(int measureSpec) {
        int result = 0;
        int specMode = MeasureSpec.getMode(measureSpec);
        int specSize = MeasureSpec.getSize(measureSpec);

        if (specMode == MeasureSpec.EXACTLY) {
            // We were told how big to be
            result = specSize;
        } else {
            // Measure the text (beware: ascent is a negative number)
            result = (int) (radius + dotTextMargin + textHeight + 10) + getPaddingTop()
                    + getPaddingBottom();
            if (specMode == MeasureSpec.AT_MOST) {
                // Respect AT_MOST value if that was what is called for by measureSpec
                result = Math.min(result, specSize);
            }
        }
        return result;
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        //绘制控件背景
        paint.reset();
        paint.setColor(Color.WHITE);
        canvas.drawRect(new Rect(0, 0, measureWidth, measureHeight), paint);
        for (int i = 0; i < dotsNum; i++) {
            drawRing(i, DEFAULT_SELECT_COLOR, canvas);
            if (content != null)
                drawText(i, content.get(i), DEFAULT_TEXT_SELECTED_COLOR, canvas);
            else
                drawText(i, getTestContent().get(i), DEFAULT_TEXT_SELECTED_COLOR, canvas);
        }
        for (int i = 0; i < dotsNum - 1; i++) {
            drawLine(i, DEFAULT_SELECT_COLOR, canvas);
        }

        if (position != dotsNum) {
            for (int i = position + 1; i < dotsNum; i++) {
                drawDots(i, DEFAULT_UNSELECT_COLOR, canvas);
                if (content != null)
                    drawText(i, content.get(i), DEFAULT_TEXT_UNSELECTED_COLOR, canvas);
                else
                    drawText(i, getTestContent().get(i), DEFAULT_TEXT_UNSELECTED_COLOR, canvas);
            }
            for (int i = position; i < dotsNum - 1; i++) {
                drawLine(i, DEFAULT_UNSELECT_COLOR, canvas);
            }
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            //判断是否点击到Dots上
            case MotionEvent.ACTION_DOWN:
                float x = event.getX();
                float y = event.getY();
                for (int i = 0; i < dotPos.length; i++) {
                    if (x < dotPos[i] + radius && x > dotPos[i] - radius && y < 2 * radius + getPaddingTop() && y > getPaddingTop()) {
                        position = i;
                        if (onDotsClickListener != null) {
                            onDotsClickListener.onDotsClickListener(i);
                        }
                    }
                }
                //重绘，调用onDraw()
                invalidate();
                break;
        }
        return super.onTouchEvent(event);
    }

    private void drawText(int pos, String text, int color, Canvas canvas) {
        Log.d(TAG, "DotsView->drawText");
        //重置画笔
        paint.reset();
        paint.setAntiAlias(true);
        //设置画笔颜色
        paint.setColor(getResources().getColor(color));
        //设置文字居中
        paint.setTextAlign(Paint.Align.CENTER);
        //设置字体大小
        paint.setTextSize(textSize);
        Paint.FontMetrics fontMetrics = paint.getFontMetrics();
        textHeight = fontMetrics.bottom - fontMetrics.top;
        float x = padding + radius * (pos + 1) * 2 - radius + pos * lineWidth;
        canvas.drawText(text, x, radius + dotTextMargin + getPaddingTop(), paint);
    }

    private void drawDots(int pos, int color, Canvas canvas) {
        Log.d(TAG, "DotsView->drawDots");
        //重置画笔
        paint.reset();
        paint.setAntiAlias(true);
        paint.setColor(getResources().getColor(color));
        int x = padding + pos * lineWidth + (pos + 1) * 2 * radius - radius;
        canvas.drawCircle(x, radius + getPaddingTop(), this.radius, paint);
    }

    private void drawRing(int pos, int color, Canvas canvas) {
        Log.d(TAG, "DotsView->drawRing");
        //重置画笔
        paint.reset();
        paint.setAntiAlias(true);
        paint.setColor(Color.parseColor("#eef2f4"));
        int x = padding + pos * lineWidth + (pos + 1) * 2 * radius - radius;
        dotPos[pos] = x;
        canvas.drawCircle(x, radius + getPaddingTop(), this.radius, paint);
        paint.setColor(getResources().getColor(color));
        canvas.drawCircle(x, radius + getPaddingTop(), this.radius - 5, paint);
    }

    private void drawLine(int pos, int color, Canvas canvas) {
        Log.d(TAG, "DotsView->drawLine");
        //重置画笔
        paint.reset();
        paint.setAntiAlias(true);
        paint.setColor(getResources().getColor(color));
        float x = padding + (pos + 1) * radius * 2 + pos * lineWidth;
        RectF rectf = new RectF(x, radius - 10 + getPaddingTop(), x + lineWidth, radius + 10 + getPaddingTop());
        canvas.drawRect(rectf, paint);
    }

    public List<String> getContent() {
        return content;
    }

    public void setContent(List<String> content) {
        this.content = content;
        invalidate();
    }

    public int getDotsNum() {
        return dotsNum;
    }

    private List<String> getTestContent() {
        List<String> testContent = new ArrayList<>();
        for (int i = 0; i < dotsNum; i++)
            testContent.add("测试" + i);
        return testContent;
    }

    public OnDotsClickListener getOnDotsClickListener() {
        return onDotsClickListener;
    }

    public void setOnDotsClickListener(OnDotsClickListener onDotsClickListener) {
        this.onDotsClickListener = onDotsClickListener;
    }

    public interface OnDotsClickListener {
        void onDotsClickListener(int position);
    }
}
