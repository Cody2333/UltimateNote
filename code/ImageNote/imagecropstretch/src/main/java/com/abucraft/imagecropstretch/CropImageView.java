package com.abucraft.imagecropstretch;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Region;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

/**
 * Created by c on 2015/7/17.
 */
public class CropImageView extends ImageView{
    //第一级别的状态（移动、裁剪、拉伸裁剪）
    public static final int FIRST_MOVE=1;
    public static final int FIRST_CROP=2;
    public static final int FIRST_STRETCH=3;

    //移动状态下第二级别的状态（移动、缩放）
    public static final int MODE_NULL=0;
    public static final int MODE_DRAG=1;
    public static final int MODE_ZOOM=2;

    public static final int CROPDISABLE=0;
    public static final int CROPABLE=1;
    //最大缩放比例
    public static final float MAX_SCALE=10;

    //双击缩放比例
    public static final float DOUBLE_CLICK_SCALE=2;

    //裁剪状态下，裁剪框的移动状态（左上角、右上角、左下、右下、整体移动）
    public static final int EDGE_LT = 1;
    public static final int EDGE_RT = 2;
    public static final int EDGE_LB = 3;
    public static final int EDGE_RB = 4;
    public static final int EDGE_MOVE_IN = 5;
    public static final int EDGE_MOVE_OUT = 6;
    public static final int EDGE_NONE = 7;



    //画裁剪框的类
    protected FloatDrawable mFloatDrawable;
    protected StretchCropDrawable mStretchDrawable;

    //裁剪框矩形
    protected Rect mFloatRect=new Rect();

    //裁剪框移动变量
    protected int currentEdge=EDGE_NONE;
    protected Point mEdgePoint=new Point();

    public int cropAbility=CROPABLE;
    //源图片
    Bitmap srcBitmap;

    //第一级状态变量
    int firstLevelMode=FIRST_MOVE;

    //第二级状态变量
    int secondLevelMode=MODE_NULL;
    PointF center=new PointF();

    //基本矩阵，即图片放置于view视图中央的矩阵
    Matrix baseMatrix=new Matrix();

    //移动矩阵
    Matrix changeMatrix=new Matrix();

    Point ltPoint=new Point();
    Point lbPoint=new Point();
    Point rbPoint=new Point();
    Point rtPoint=new Point();

    //触摸点监听器
    MyTouchListener myTouchListener;

    //手势监听器
    GestureDetector mGestureDetector;

    //位图宽高
    int bitmapWidth;
    int bitmapHeight;

    //旋转角度
    int rotateDegree=360;

    private void initial(){
        //初始化监听器
        myTouchListener=new MyTouchListener();
        setOnTouchListener(myTouchListener);
        mGestureDetector=new GestureDetector(getContext(),new GestureListener());
        //设置第一级别状态为移动
        firstLevelMode=FIRST_MOVE;
        center.set((float) getWidth() / 2, (float) getHeight() / 2);
        //设置imageview的图片显示类型为矩阵
        setScaleType(ScaleType.MATRIX);
        mFloatDrawable=new FloatDrawable(getContext());
        mStretchDrawable=new StretchCropDrawable(getContext());
    }

    public CropImageView(Context context) {
        super(context);
        initial();
    }

    public CropImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initial();
    }

    public CropImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initial();
    }

    //设置模式为裁剪，并刷新画布
    public boolean setModeCrop(){
        firstLevelMode=FIRST_CROP;
        secondLevelMode=MODE_NULL;
        invalidate();
        return true;
    }

    //设置模式为移动，并刷新画布
    public boolean setModeDrag(){
        firstLevelMode=FIRST_MOVE;
        secondLevelMode=MODE_NULL;
        invalidate();
        return true;
    }

    public boolean setModeStretch(){
        firstLevelMode=FIRST_STRETCH;
        secondLevelMode=EDGE_NONE;
        invalidate();
        return true;
    }
    //裁剪图片
    public Bitmap getResultBitmap(){
        switch (firstLevelMode){
            //如果模式为移动，则返回旋转后的源图片
            case FIRST_MOVE:
                return getRotatedBitmap();
            //模式为裁剪，返回裁剪后的图片
            case FIRST_CROP:
                return getCroppedBitmap();
            case FIRST_STRETCH:
                if(cropAbility == CROPABLE){
                    return getStretchCropBitmap();
                }
            default:
                return null;
        }
    }

    //旋转图片并刷新画面
    public void rotateBitmap(){
        baseMatrix.postRotate(-90,(float)getWidth()/2,(float)getHeight()/2);
        setImageMatrix(baseMatrix);
        rotateDegree-=90;
        if(rotateDegree==0){
            rotateDegree=360;
        }
        invalidate();
    }

    //旋转图片
    private Bitmap getRotatedBitmap(){

        Bitmap resultBmp=null;
        try {
            Matrix outputMatrix=new Matrix();
            outputMatrix.postRotate((float)rotateDegree);
            resultBmp=Bitmap.createBitmap(srcBitmap, 0, 0, bitmapWidth, bitmapHeight,outputMatrix,true);
            return resultBmp;
        }catch (IllegalArgumentException e){
            return null;
        }
    }

    //裁剪图片
    private Bitmap getCroppedBitmap(){
        RectF bitmapRect=new RectF();
        Matrix destMatrix=getImageMatrix();
        //获取图片显示矩阵的逆矩阵
        Matrix inverse=new Matrix();
        if(destMatrix.invert(inverse)) {
            //将逆矩阵应用到裁剪框就能获得其相对于源图片的位置和大小
            RectF transformed=new RectF();
            transformed.set(mFloatRect);
            inverse.mapRect(transformed);
            //设置旋转矩阵
            Matrix outputMatrix=new Matrix();
            outputMatrix.postRotate((float)rotateDegree);
            //设置裁剪左边界，如果裁剪框左边界在源图片左边界以左就设置为源图片的左边界，其他的值都以此类推
            int left=transformed.left<0?0:(int)transformed.left;
            int top=transformed.top<0?0:(int)transformed.top;
            int right=(int)transformed.right>bitmapWidth?bitmapWidth:(int)transformed.right;
            int bottom=(int)transformed.bottom>bitmapHeight?bitmapHeight:(int)transformed.bottom;

            try {
                //裁剪图片并应用旋转矩阵
                Bitmap resBitmap = Bitmap.createBitmap(srcBitmap, left, top, right - left, bottom - top,outputMatrix,true);
                return resBitmap;
            }catch (IllegalArgumentException e){
                return null;
            }
        }
        return null;
    }

    private Bitmap getStretchCropBitmap(){
        Matrix destMatrix=getImageMatrix();
        //获取图片显示矩阵的逆矩阵
        Matrix inverse=new Matrix();
        if(destMatrix.invert(inverse)) {
            float[] points = {ltPoint.x, ltPoint.y, lbPoint.x, lbPoint.y, rbPoint.x, rbPoint.y, rtPoint.x, rtPoint.y};
            inverse.mapPoints(points);
            return StretchBitmap.stretchCrop(srcBitmap, points);
        }
        return null;
    }

    private Boolean checkCropAbility(){
        if(StretchBitmap.isConvexQuadrangle(ltPoint, lbPoint, rbPoint, rtPoint)){
            cropAbility=CROPABLE;
            Matrix destMatrix=getImageMatrix();
            //获取图片显示矩阵的逆矩阵
            Matrix inverse=new Matrix();
            if(destMatrix.invert(inverse)) {
                float [] points = {ltPoint.x,ltPoint.y,lbPoint.x,lbPoint.y,rbPoint.x,rbPoint.y,rtPoint.x,rtPoint.y};
                inverse.mapPoints(points);
                for(int i = 0 ; i<points.length ; i++ ){
                    //y坐标
                    if(i%2 == 1){
                        if(points[i] < 0 || points[i] > bitmapHeight){
                            cropAbility=CROPDISABLE;
                            return false;
                        }
                    }else {
                        if(points[i] < 0 || points[i] > bitmapWidth){
                            cropAbility = CROPDISABLE;
                            return false;
                        }
                    }
                }
            }
            return true;
        }
        else {
            cropAbility=CROPDISABLE;
            return false;
        }
    }



    @Override
    public void setImageBitmap(Bitmap bm) {
        super.setImageBitmap(bm);
        baseMatrix.set(getImageMatrix());
        bitmapHeight=bm.getHeight();
        bitmapWidth=bm.getWidth();
        float scaleX=(float)getWidth()/bitmapWidth;
        float scaleY=(float)getHeight()/bitmapHeight;
        float scale=1.0f;
        srcBitmap=bm;
        //将图片进行适当缩小并居中
        if(scaleX<1||scaleY<1){
            if(scaleX<scaleY){
                scale=scaleX;
                baseMatrix.postScale(scaleX,scaleX);
            }else {
                scale=scaleY;
                baseMatrix.postScale(scaleY,scaleY);
            }
        }
        float dx=((float)getWidth()-bitmapWidth*scale)/2;
        float dy=((float)getHeight()-bitmapHeight*scale)/2;
        baseMatrix.postTranslate(dx,dy);
        setImageMatrix(baseMatrix);
        //根据图片大小设置裁剪框
        configueFloatDrawable((int)(bitmapWidth*scale),(int)(bitmapHeight*scale));
        center.set((float)getWidth()/2,(float)getHeight()/2);
        configueStretchDrawable((int)(bitmapWidth*scale),(int)(bitmapHeight*scale));
        return;
    }

    public void configueStretchDrawable(int bitmapWidth,int bitmapHeight){
        int floatWidth=getWidth()/2>bitmapWidth?bitmapWidth:getWidth()/2;
        int floatHeight=getHeight()/2>bitmapHeight?bitmapHeight:getHeight()/2;
        mStretchDrawable.setBounds(0,0,getWidth(),getHeight());
        mStretchDrawable.setPoints(getWidth() / 2 - floatWidth / 2, getHeight() / 2 - floatHeight / 2, getWidth() / 2 + floatWidth / 2, getHeight() / 2 + floatHeight / 2);
        mStretchDrawable.mapPoints(ltPoint, lbPoint, rbPoint, rtPoint);
        return;
    }

    public void configueFloatDrawable(int bitmapWidth,int bitmapHeight){
        int floatWidth=getWidth()/2>bitmapWidth?bitmapWidth:getWidth()/2;
        int floatHeight=getHeight()/2>bitmapHeight?bitmapHeight:getHeight()/2;
        mFloatRect.set(getWidth() / 2 - floatWidth / 2, getHeight() / 2 - floatHeight / 2, getWidth() / 2 + floatWidth / 2, getHeight() / 2 + floatHeight / 2);
        mFloatDrawable.setBounds(mFloatRect);
        return;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if(firstLevelMode==FIRST_CROP) {
            //在裁剪框之外画上阴影
            canvas.clipRect(mFloatRect, Region.Op.DIFFERENCE);
            canvas.drawColor(Color.parseColor("#a0000000"));
            canvas.restore();
            mFloatDrawable.draw(canvas);
        }
        if(firstLevelMode==FIRST_STRETCH){
            Path path=new Path();
            path.moveTo(ltPoint.x,ltPoint.y);
            path.lineTo(rtPoint.x, rtPoint.y);
            path.lineTo(rbPoint.x, rbPoint.y);
            path.lineTo(lbPoint.x, lbPoint.y);
            path.lineTo(ltPoint.x, ltPoint.y);

            canvas.clipPath(path, Region.Op.DIFFERENCE);
            canvas.drawColor(Color.parseColor("#a0000000"));

            canvas.restore();
            if(cropAbility==CROPABLE) {
                mStretchDrawable.draw(canvas);
            }
            if(cropAbility==CROPDISABLE){
                mStretchDrawable.drawRed(canvas);
            }
        }
    }

    //根据触摸点判断碰到哪个角
    public int getTouchEdge(int eventX, int eventY) {
        if (mFloatDrawable.getBounds().left <= eventX
                && eventX < (mFloatDrawable.getBounds().left + mFloatDrawable
                .getBorderWidth())
                && mFloatDrawable.getBounds().top <= eventY
                && eventY < (mFloatDrawable.getBounds().top + mFloatDrawable
                .getBorderHeight())) {
            return EDGE_LT;
        } else if ((mFloatDrawable.getBounds().right - mFloatDrawable
                .getBorderWidth()) <= eventX
                && eventX < mFloatDrawable.getBounds().right
                && mFloatDrawable.getBounds().top <= eventY
                && eventY < (mFloatDrawable.getBounds().top + mFloatDrawable
                .getBorderHeight())) {
            return EDGE_RT;
        } else if (mFloatDrawable.getBounds().left <= eventX
                && eventX < (mFloatDrawable.getBounds().left + mFloatDrawable
                .getBorderWidth())
                && (mFloatDrawable.getBounds().bottom - mFloatDrawable
                .getBorderHeight()) <= eventY
                && eventY < mFloatDrawable.getBounds().bottom) {
            return EDGE_LB;
        } else if ((mFloatDrawable.getBounds().right - mFloatDrawable
                .getBorderWidth()) <= eventX
                && eventX < mFloatDrawable.getBounds().right
                && (mFloatDrawable.getBounds().bottom - mFloatDrawable
                .getBorderHeight()) <= eventY
                && eventY < mFloatDrawable.getBounds().bottom) {
            return EDGE_RB;
        } else if (mFloatDrawable.getBounds().contains(eventX, eventY)) {
            return EDGE_MOVE_IN;
        }
        return EDGE_MOVE_OUT;
    }

    protected void changeFloatRect(MotionEvent event){
        if(firstLevelMode!=FIRST_CROP){
            return;
        }
        int dx=(int)(event.getX()-mEdgePoint.x);
        int dy=(int)(event.getY()-mEdgePoint.y);
        mEdgePoint.set((int)event.getX(),(int)event.getY());
        switch (currentEdge){
            case EDGE_LT:
                //裁剪框左边界不能移动到右边界右侧，上边界不能移动到下边界之下，并禁止移出屏幕
                if(mFloatRect.left+dx<mFloatRect.right&&mFloatRect.top+dy<mFloatRect.bottom
                        &&mFloatRect.left+dx>0&&mFloatRect.top+dy>0) {
                    mFloatRect.set(mFloatRect.left + dx, mFloatRect.top + dy, mFloatRect.right, mFloatRect.bottom);
                }
                break;
            case EDGE_LB:
                if(mFloatRect.left+dx<mFloatRect.right&&mFloatRect.top<mFloatRect.bottom+dy
                        &&mFloatRect.left+dx>0&&mFloatRect.bottom+dy<getHeight()) {
                    mFloatRect.set(mFloatRect.left + dx, mFloatRect.top, mFloatRect.right, mFloatRect.bottom + dy);
                }
                break;
            case EDGE_RT:
                if(mFloatRect.left<mFloatRect.right+dx&&mFloatRect.top+dy<mFloatRect.bottom
                        &&mFloatRect.right+dx<getWidth()&&mFloatRect.top+dy>0) {
                    mFloatRect.set(mFloatRect.left, mFloatRect.top + dy, mFloatRect.right + dx, mFloatRect.bottom);
                }
                break;
            case EDGE_RB:
                if(mFloatRect.left<mFloatRect.right+dx&&mFloatRect.top<mFloatRect.bottom+dy
                        &&mFloatRect.right+dx<getWidth()&&mFloatRect.bottom+dy<getHeight()) {
                    mFloatRect.set(mFloatRect.left, mFloatRect.top, mFloatRect.right + dx, mFloatRect.bottom + dy);
                }
                break;
            case EDGE_MOVE_IN:
                if(mFloatRect.left+dx>0&&mFloatRect.right+dx<getWidth()&&mFloatRect.top+dy>0&&mFloatRect.bottom+dy<getHeight()){
                    mFloatRect.offset(dx,dy);
                }
                break;
            default:
                break;
        }
        mFloatDrawable.setBounds(mFloatRect);
        invalidate();
        return;
    }

    //判断触碰到拉伸框的哪个角
    protected int getTouchStretchPoint(MotionEvent event) {
        if (distanceOfPoints(ltPoint.x, ltPoint.y, event.getX(), event.getY()) < mStretchDrawable.dpTopx(getContext(), (int)(mStretchDrawable.RADIUS*1.5))) {
            return EDGE_LT;
        }
        if (distanceOfPoints(lbPoint.x, lbPoint.y, event.getX(), event.getY()) < mStretchDrawable.dpTopx(getContext(), (int)(mStretchDrawable.RADIUS*1.5))) {
            return EDGE_LB;
        }
        if (distanceOfPoints(rtPoint.x, rtPoint.y, event.getX(), event.getY()) < mStretchDrawable.dpTopx(getContext(), (int)(mStretchDrawable.RADIUS*1.5))){
            return EDGE_RT;
        }
        if(distanceOfPoints(rbPoint.x,rbPoint.y,event.getX(),event.getY())<mStretchDrawable.dpTopx(getContext(),(int)(mStretchDrawable.RADIUS*1.5))){
            return EDGE_RB  ;
        }
        return EDGE_NONE;
    }

    protected void moveStretchEdge(MotionEvent event){
        if(firstLevelMode==FIRST_STRETCH){
            int dx=(int)(event.getX()-mEdgePoint.x);
            int dy=(int)(event.getY()-mEdgePoint.y);
            mEdgePoint.set((int) event.getX(), (int) event.getY());
            switch (currentEdge){
                case EDGE_LT:
                    ltPoint.set(ltPoint.x+dx,ltPoint.y+dy);
                    break;
                case EDGE_LB:
                    lbPoint.set(lbPoint.x + dx, lbPoint.y + dy);
                    break;
                case EDGE_RT:
                    rtPoint.set(rtPoint.x+dx,rtPoint.y+dy);
                    break;
                case EDGE_RB:
                    rbPoint.set(rbPoint.x+dx,rbPoint.y+dy);
                    break;
                default:
                    break;
            }
            mStretchDrawable.setLtPoint(ltPoint.x, ltPoint.y);
            mStretchDrawable.setLbPoint(lbPoint.x, lbPoint.y);
            mStretchDrawable.setRtPoint(rtPoint.x, rtPoint.y);
            mStretchDrawable.setRbPoint(rbPoint.x, rbPoint.y);
            checkCropAbility();
            invalidate();
        }
    }

    protected float distanceOfPoints(float x1,float y1,float x2,float y2){
        return (float)Math.sqrt((x1-x2)*(x1-x2)+(y1-y2)*(y1-y2));
    }
    class MyTouchListener implements OnTouchListener{
        float startDistance;
        private PointF startPoint=new PointF();
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            switch (motionEvent.getActionMasked()){
                case MotionEvent.ACTION_DOWN:
                    //如果第一级状态为移动，则在第一个触摸点时将第二级状态设置为移动，并设置初始点
                    if(firstLevelMode==FIRST_MOVE){
                        secondLevelMode=MODE_DRAG;
                        startPoint.set(motionEvent.getX(),motionEvent.getY());
                        changeMatrix.set(getImageMatrix());
                    }
                    if(firstLevelMode==FIRST_CROP){
                        currentEdge=getTouchEdge((int)motionEvent.getX(),(int)motionEvent.getY());
                        //如果落点在框外，则将第二级状态设置为移动
                        if(currentEdge==EDGE_MOVE_OUT){
                            secondLevelMode=MODE_DRAG;
                            startPoint.set(motionEvent.getX(),motionEvent.getY());
                            changeMatrix.set(getImageMatrix());
                        }
                        //否则获取初始落点
                        else {
                            secondLevelMode=MODE_NULL;
                            mEdgePoint.set((int)motionEvent.getX(),(int)motionEvent.getY());
                        }
                    }
                    if(firstLevelMode==FIRST_STRETCH){
                        currentEdge=getTouchStretchPoint(motionEvent);
                        if(currentEdge==EDGE_NONE){
                            secondLevelMode=MODE_DRAG;
                            startPoint.set(motionEvent.getX(),motionEvent.getY());
                            changeMatrix.set(getImageMatrix());
                        }
                        else {
                            secondLevelMode=MODE_NULL;
                            mEdgePoint.set((int)motionEvent.getX(),(int)motionEvent.getY());
                        }
                    }
                    break;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    secondLevelMode=MODE_NULL;
                    currentEdge=EDGE_NONE;
                    break;
                case MotionEvent.ACTION_MOVE:
                    if(secondLevelMode!=MODE_NULL){
                        if(secondLevelMode==MODE_DRAG){
                            setDragMatrix(motionEvent);
                        }
                        if(secondLevelMode==MODE_ZOOM){
                            setZoomMatrix(motionEvent);
                        }
                    }
                    if(firstLevelMode==FIRST_CROP){
                        changeFloatRect(motionEvent);
                    }
                    if(firstLevelMode==FIRST_STRETCH){
                        moveStretchEdge(motionEvent);
                    }
                    break;
                case MotionEvent.ACTION_POINTER_DOWN:
                    if(firstLevelMode==FIRST_MOVE){
                        secondLevelMode=MODE_ZOOM;
                        startDistance=getDistance(motionEvent);
                        startPoint.set((motionEvent.getX(0)+motionEvent.getX(1))/2,(motionEvent.getY(0)+motionEvent.getY(1))/2);
                    }

                    //在裁剪模式下，多点触控也会被识别为对原图的缩放而不是裁剪框的缩放
                    if(firstLevelMode==FIRST_CROP){
                        if(secondLevelMode==MODE_DRAG){
                            secondLevelMode=MODE_ZOOM;
                            startDistance=getDistance(motionEvent);
                            startPoint.set((motionEvent.getX(0)+motionEvent.getX(1))/2,(motionEvent.getY(0)+motionEvent.getY(1))/2);
                        }
                        if(secondLevelMode==MODE_NULL){
                            //取消原先的裁剪框移动状态
                            currentEdge=EDGE_MOVE_OUT;
                            secondLevelMode=MODE_ZOOM;
                            startDistance=getDistance(motionEvent);
                            startPoint.set((motionEvent.getX(0)+motionEvent.getX(1))/2,(motionEvent.getY(0)+motionEvent.getY(1))/2);
                        }
                    }
                    if(firstLevelMode == FIRST_STRETCH){
                        if(secondLevelMode == MODE_DRAG){
                            secondLevelMode = MODE_ZOOM;
                            startDistance = getDistance(motionEvent);
                            startPoint.set((motionEvent.getX(0)+motionEvent.getX(1))/2,(motionEvent.getY(0)+motionEvent.getY(1))/2);
                        }
                        if(secondLevelMode == MODE_NULL){
                            currentEdge = EDGE_NONE;
                            secondLevelMode = MODE_ZOOM;
                            startDistance = getDistance(motionEvent);
                            startPoint.set((motionEvent.getX(0)+motionEvent.getX(1))/2,(motionEvent.getY(0)+motionEvent.getY(1))/2);
                        }
                    }
                    break;
                default:
                    break;
            }
            return mGestureDetector.onTouchEvent(motionEvent);
            //return true;
        }
        public float getDistance(MotionEvent event){
            float dx = event.getX(1) - event.getX(0);
            float dy = event.getY(1) - event.getY(0);
            return (float)Math.sqrt(dx*dx+dy*dy);
        }

        public void setDragMatrix(MotionEvent event){
            if(secondLevelMode!=MODE_DRAG){
                return;
            }
            float dx=event.getX()-startPoint.x;
            float dy=event.getY()-startPoint.y;
            float newDistance=(float)Math.sqrt(dx*dx+dy*dy);
            //防止和双击事件起冲突
            if (Math.abs(newDistance - startDistance) < 5.0f) {
                return;
            }
            startPoint.set(event.getX(), event.getY());
            RectF imageRect=new RectF();
            imageRect.set(0,0,(float)bitmapWidth,(float)bitmapHeight);
            changeMatrix.set(getImageMatrix());
            changeMatrix.mapRect(imageRect);
            //图片不能移动到视图之外
            if(imageRect.right+dx<0.0f||imageRect.left+dx>(float)getWidth() ||imageRect.bottom+dy<0||imageRect.top+dy>(float)getHeight()){
                return;
            }else {
                changeMatrix.postTranslate(dx, dy);
                setImageMatrix(changeMatrix);
                return;
            }
        }

        public void setZoomMatrix(MotionEvent event) {
            if (secondLevelMode == MODE_ZOOM) {
                if (event.getPointerCount() < 2) {
                    secondLevelMode = MODE_NULL;
                    return;
                }
                float newDistance = getDistance(event);
                float scale = newDistance / startDistance;
                startDistance = newDistance;
                PointF eCenter = new PointF();
                eCenter.set((event.getX(0) + event.getX(1)) / 2, (event.getY(0) + event.getY(1)) / 2);
                RectF imageRect=new RectF();
                imageRect.set(0,0,(float)bitmapWidth,(float)bitmapHeight);
                changeMatrix.set(getImageMatrix());
                changeMatrix.postScale(scale, scale, eCenter.x, eCenter.y);
                changeMatrix.mapRect(imageRect);
                //检查最大缩放级别
                if (imageRect.width()/bitmapWidth > (1.0 / MAX_SCALE) && imageRect.width()/bitmapWidth < MAX_SCALE) {
                    setImageMatrix(changeMatrix);
                }
            }
        }

    }


    class GestureListener extends GestureDetector.SimpleOnGestureListener{

        @Override
        public boolean onDown(MotionEvent e) {
            //捕获Down事件
            return true;
        }
        @Override
        public boolean onDoubleTap(MotionEvent e) {
            //触发双击事件
            if(firstLevelMode==FIRST_MOVE) {
                float[] value = new float[9];
                float[] baseValue = new float[9];
                baseMatrix.getValues(baseValue);
                changeMatrix.set(getImageMatrix());
                changeMatrix.getValues(value);
                //检查是否已经经过缩放
                if (Math.abs(value[Matrix.MSCALE_Y] - baseValue[Matrix.MSCALE_Y]) < 0.01 && value[Matrix.MTRANS_X] == baseValue[Matrix.MTRANS_X] && value[Matrix.MTRANS_Y] == baseValue[Matrix.MTRANS_Y]) {
                    float dx = center.x - e.getX();
                    float dy = center.y - e.getY();
                    value[Matrix.MTRANS_X] = dx;
                    value[Matrix.MTRANS_Y] = dy;
                    changeMatrix.postScale(DOUBLE_CLICK_SCALE, DOUBLE_CLICK_SCALE, center.x, center.y);
                    changeMatrix.postTranslate(dx * DOUBLE_CLICK_SCALE, dy * DOUBLE_CLICK_SCALE);
                    setImageMatrix(changeMatrix);
                } else {
                    setImageMatrix(baseMatrix);
                }
            }

            return true;
        }
        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            // TODO Auto-generated method stub
            return super.onSingleTapUp(e);
        }

        @Override
        public void onLongPress(MotionEvent e) {
            // TODO Auto-generated method stub
            super.onLongPress(e);
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2,
                                float distanceX, float distanceY) {
            return super.onScroll(e1, e2, distanceX, distanceY);
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
                               float velocityY) {
            // TODO Auto-generated method stub

            return super.onFling(e1, e2, velocityX, velocityY);
        }

        @Override
        public void onShowPress(MotionEvent e) {
            // TODO Auto-generated method stub
            super.onShowPress(e);
        }





        @Override
        public boolean onDoubleTapEvent(MotionEvent e) {
            // TODO Auto-generated method stub
            return super.onDoubleTapEvent(e);
        }

        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            // TODO Auto-generated method stub
            return super.onSingleTapConfirmed(e);
        }
    }

}
