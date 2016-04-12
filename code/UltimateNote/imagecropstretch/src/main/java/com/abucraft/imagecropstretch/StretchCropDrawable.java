package com.abucraft.imagecropstretch;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;

/**
 * Created by c on 2015/8/2.
 */
public class StretchCropDrawable extends Drawable {
    Point ltPoint=new Point();
    Point lbPoint=new Point();
    Point rtPoint=new Point();
    Point rbPoint=new Point();
    Paint mPaint=new Paint();
    public final static int RADIUS=12;
    Context mContext;

    public StretchCropDrawable(Context context) {
        mContext=context;
        mPaint.setColor(Color.WHITE);
        mPaint.setStrokeWidth(1f);
        mPaint.setAntiAlias(true);
        mPaint.setStyle(Paint.Style.STROKE);

    }

    @Override
    public void setBounds(int left, int top, int right, int bottom) {
        super.setBounds(left, top, right, bottom);
    }

    @Override
    public void setBounds(Rect bounds) {
        super.setBounds(bounds);
    }

    public void setPoints(int left, int top, int right, int bottom){
        ltPoint.set(left, top);
        lbPoint.set(left, bottom);
        rtPoint.set(right, top);
        rbPoint.set(right, bottom);
    }
    public void mapPoints(Point nltPoint,Point nlbPoint,Point nrbPoint,Point nrtPoint){
        nltPoint.set(ltPoint.x,ltPoint.y);
        nlbPoint.set(lbPoint.x,lbPoint.y);
        nrbPoint.set(rbPoint.x,rbPoint.y);
        nrtPoint.set(rtPoint.x,rtPoint.y);
    }

    public Point getLtPoint() {
        return ltPoint;
    }

    public Point getLbPoint() {
        return lbPoint;
    }

    public Point getRtPoint() {
        return rtPoint;
    }

    public Point getRbPoint() {
        return rbPoint;
    }

    public void setLtPoint(int x,int y){
        this.ltPoint.set(x,y);
    }

    public void setLbPoint(int x,int y){
        this.lbPoint.set(x,y);
    }

    public void setRtPoint(int x,int y){
        this.rtPoint.set(x,y);
    }

    public void setRbPoint(int x,int y){
        this.rbPoint.set(x,y);
    }
    @Override
    public void setColorFilter(ColorFilter cf) {

    }

    @Override
    public int getOpacity() {
        return 0;
    }

    @Override
    public void setAlpha(int alpha) {

    }

    @Override
    public void draw(Canvas canvas) {
        mPaint.setStrokeWidth(4f);
        canvas.drawCircle(ltPoint.x,ltPoint.y,dpTopx(mContext,RADIUS),mPaint);
        canvas.drawCircle(lbPoint.x,lbPoint.y,dpTopx(mContext,RADIUS),mPaint);
        canvas.drawCircle(rtPoint.x, rtPoint.y, dpTopx(mContext, RADIUS), mPaint);
        canvas.drawCircle(rbPoint.x, rbPoint.y, dpTopx(mContext, RADIUS), mPaint);
        mPaint.setStrokeWidth(1f);
        canvas.drawLine(ltPoint.x,ltPoint.y,rtPoint.x,rtPoint.y,mPaint);
        canvas.drawLine(rtPoint.x,rtPoint.y,rbPoint.x,rbPoint.y,mPaint);
        canvas.drawLine(rbPoint.x,rbPoint.y,lbPoint.x,lbPoint.y,mPaint);
        canvas.drawLine(lbPoint.x,lbPoint.y,ltPoint.x,ltPoint.y,mPaint);
    }

    public void drawRed(Canvas canvas){
        Paint redPaint=new Paint();
        redPaint.setColor(Color.RED);
        redPaint.setStrokeWidth(4f);
        redPaint.setAntiAlias(true);
        redPaint.setStyle(Paint.Style.STROKE);
        canvas.drawCircle(ltPoint.x, ltPoint.y, dpTopx(mContext, RADIUS), redPaint);
        canvas.drawCircle(lbPoint.x,lbPoint.y,dpTopx(mContext,RADIUS),redPaint);
        canvas.drawCircle(rtPoint.x, rtPoint.y, dpTopx(mContext, RADIUS), redPaint);
        canvas.drawCircle(rbPoint.x, rbPoint.y, dpTopx(mContext, RADIUS), redPaint);
        redPaint.setStrokeWidth(1f);
        canvas.drawLine(ltPoint.x,ltPoint.y,rtPoint.x,rtPoint.y,redPaint);
        canvas.drawLine(rtPoint.x,rtPoint.y,rbPoint.x,rbPoint.y,redPaint);
        canvas.drawLine(rbPoint.x,rbPoint.y,lbPoint.x,lbPoint.y,redPaint);
        canvas.drawLine(lbPoint.x,lbPoint.y,ltPoint.x,ltPoint.y,redPaint);
    }
    public int dpTopx(Context context,int dp){
        final float scale=context.getResources().getDisplayMetrics().density;
        return (int)(dp*scale);
    }
}
