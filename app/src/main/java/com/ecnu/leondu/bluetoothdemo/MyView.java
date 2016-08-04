package com.ecnu.leondu.bluetoothdemo;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by LeonDu on 7/5/16.
 */
public class MyView extends View {

    private MyThread myThread;
    private RectF rectF = new RectF(100, 600, 400, 900);
    private float sweepAngle = 0;

    int alphaValue = 0;

    private float xScale = 0.1f;
    private float yScale = 0.1f;

    // 代码用构造函数
    public MyView(Context context) {
        super(context);
    }

    // 布局用构造函数
    public MyView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    // 加入绘制元素

    @Override
    protected void onDraw(Canvas canvas) {
        // super.onDraw什么都没有做,可以删掉
        super.onDraw(canvas);

//        // 画笔元素
//        Paint paint = new Paint();
//        paint.setColor(0xffff0000);// 参数代表a,r,g,b,表示透明度和rgb组合
////        paint.setStyle(Paint.Style.STROKE); // 设置为空心
//        // 抗锯齿
//        paint.setAntiAlias(true);
//        // 参数是像素
//        paint.setTextSize(120);
////         绘制元素通过Canvas来绘制
//        canvas.drawText("参数",300,300,paint);
        // 绘制直线
//        canvas.drawLine(50,120,290,120,paint);
        // 绘制矩形
//        canvas.drawRect(50,120,290,240,paint);
        // 创建一个矩形，绘制矩形
//        RectF rect = new RectF(50,120,290,360);
//        canvas.drawRect(rect,paint);
        // 绘制圆角矩形
//        canvas.drawRoundRect(rect,30,30,paint);
        // 绘制圆形(圆心x,圆心y,半径长度,paint)
//        canvas.draw(120,120,100,paint);
        // 只能绘制bitmap么，尝试绘制矢量图

//        canvas.drawArc(rectF, 0, sweepAngle, true, paint);


        // 获取资源文件的引用res
        Resources res = getResources();
        // 获取图形资源文件
        Bitmap bmp = BitmapFactory.decodeResource(res, R.mipmap.animation_great);
        // 设置canvas画布背景为白色
//        canvas.drawColor(Color.BLACK);
//         在画布上绘制缩放之前的位图，以做对比
//        屏幕上的位置坐标是0,0
        canvas.drawBitmap(bmp, 0, 0, null);
//         定义矩阵对象
        Matrix matrix = new Matrix();
        // 缩放原图
        matrix.postScale(1.8f, 1.8f);
        //bmp.getWidth(), bmp.getHeight()分别表示缩放后的位图宽高
        Bitmap dstbmp = Bitmap.createBitmap(bmp, 0, 0, bmp.getWidth(), bmp.getHeight(),
                matrix, true);
        // 在画布上绘制旋转后的位图
        //放在坐标为60,460的位置

        Paint bitmapPaint = new Paint();
        bitmapPaint.setAlpha(alphaValue);
        canvas.drawBitmap(dstbmp, 330, 720, bitmapPaint);
//        canvas.drawBitmap(bmp, 330, 640, bitmapPaint);


    }

    public void start() {

        if (myThread == null || !myThread.isAlive()) {
            myThread = new MyThread();
            myThread.start();
        }

    }

    class MyThread extends Thread {
        @Override
        public void run() {
            // 刷新draw函数

            alphaValue = 0;

            while (true) {

                alphaValue = alphaChangeing(alphaValue);

                if (alphaValue <0)
                {
                    alphaValue = 0;
                    break;
                }
                postInvalidate();



                try {
                    sleep(30);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                postInvalidate();
            }

        }
    }

    //TODO 透明度变化曲线(考虑 sin 函数和 cos 函数结合)
    private int alphaChangeing(int alphaValue) {

        // zengjia
        if (alphaValue % 2 ==0)
        {
            if (alphaValue < 150) {
                alphaValue += 20;
            } else if (alphaValue < 230)
            {
                alphaValue += 6;
            }else if(alphaValue < 250)
            {
                alphaValue +=4;
            }else
            {
                alphaValue +=2;
            }
        }else
        {
            if (alphaValue > 240) {
                alphaValue -= 8;
            } else if (alphaValue > 210)
            {
                alphaValue -= 16;
            }else {
                alphaValue -= 50;
            }
        }

        if(alphaValue > 255)
            alphaValue = 255;

        return alphaValue;
    }
}
