package com.abucraft.imagecropstretch;

import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.PointF;

/**
 * Created by c on 2015/8/4.
 */
public class StretchBitmap {
    public static boolean isConvexQuadrangle(Point ltPoint,Point lbPoint,Point rbPoint,Point rtPoint){
        /*if(ltPoint.equals(lbPoint)||ltPoint.equals(rbPoint)||ltPoint.equals(rtPoint)||
                lbPoint.equals(rbPoint)||lbPoint.equals(rtPoint)||rbPoint.equals(rtPoint)) {
            return false;
        }*/

        if(intersect(ltPoint,rbPoint,lbPoint,rtPoint)){
            return true;
        }
        return false;
    }

    //判断两个线段是否相交，如果某个点在线段上，返回false
    protected static boolean intersect(Point a,Point b,Point c,Point d){
        if((crossProduct(a,c,d)*crossProduct(b,c,d))>=0){
            return false;
        }
        if((crossProduct(c,a,b)*crossProduct(d,a,b))>=0){
            return false;
        }
        return true;
    }
    protected static float crossProduct(Point a,Point b,Point c){
        return ((float)b.x-a.x)*((float)c.y-a.y)-((float)b.y-a.y)*((float)c.x-a.x);
    }

    public static Bitmap stretchCrop(Bitmap src, float[] points){
        Point lt = new Point();
        Point lb = new Point();
        Point rb = new Point();
        Point rt = new Point();
        lt.set((int)points[0],(int)points[1]);
        lb.set((int)points[2],(int)points[3]);
        rb.set((int)points[4],(int)points[5]);
        rt.set((int)points[6],(int)points[7]);
        return  stretchCrop(src, lt, lb, rb, rt);
    }

    public static Bitmap stretchCrop(Bitmap src, Point lt, Point lb, Point rb, Point rt){
        int resWidth = (int)(Math.sqrt(((double) rt.x - lt.x) * ((double) rt.x - lt.x)) + Math.sqrt(((double)rt.y - lt.y) * ((double)rt.y - lt.y))+
                Math.sqrt(((double)rb.x - lb.x) * ((double)rb.x - lb.x)) + Math.sqrt(((double)rb.y - lb.y) * ((double)rb.y - lb.y)))/2;
        int resHeight = (int)(Math.sqrt(((double) lt.x - lb.x) * ((double) lt.x - lb.x)) + Math.sqrt(((double)lt.y - lb.y) * ((double)lt.y - lb.y))+
                Math.sqrt(((double)rt.x - rb.x) * ((double)rt.x - rb.x)) + Math.sqrt(((double)rt.y - rb.y) * ((double)rt.y - rb.y)))/2;
        int[] colors = new int[resWidth*resHeight];
        for(int j = 0; j < resHeight; j++){
            for(int i = 0; i < resWidth; i++){
                PointF a = new PointF();
                a.set(((float)i/resWidth)*((float)rt.x - lt.x),((float)i/resWidth)*((float)rt.y - lt.y));
                PointF b = new PointF();
                b.set(((float)i/resWidth)*((float)rb.x - lb.x),((float)i/resWidth)*((float)rb.y - lb.y));
                PointF c = new PointF();
                c.set((float)lb.x + b.x - ((float)lt.x + a.x), (float)lb.y + b.y - ((float)lt.y + a.y));
                PointF d = new PointF();
                d.set(((float)j/resHeight)*c.x, ((float)j/resHeight)*c.y);
                Point dest = new Point();
                dest.set((int)(lt.x + a.x + d.x), (int)(lt.y + a.y + d.y));
                colors[j*resWidth + i] = src.getPixel(dest.x, dest.y);
            }
        }
        Bitmap res = Bitmap.createBitmap(colors, resWidth, resHeight, Bitmap.Config.ARGB_8888);
        return res;
    }
}
