package com.project.ocr;

import android.graphics.Bitmap;


public class PreProcess {
    private static int[] pix;
    private static int width;
    private static int height;
    private static final int req_width = 720;
    private static final int req_height = 720;
    public static void doProcess(Bitmap bitmap, Bitmap outBitmap){
        width = bitmap.getWidth();
        height = bitmap.getHeight();

        //get pixels
        pix = new int[width * height];
        bitmap.getPixels(pix, 0, width, 0, 0, width, height);

        //process
        binary();
        outBitmap.setPixels(pix, 0, width, 0, 0, width, height);

        deNoise();
        outBitmap.setPixels(pix, 0, width, 0, 0, width, height);

        System.out.println("");
    }

    private static void compress(){
        //get ratio and origin width and height
        int ratio = 1;
        int origin_width = width;
        int origin_height = height;
        while(width > req_width * ratio || height > req_height * ratio){
            ratio++;
        }
        width = origin_width / ratio;
        height = origin_height / ratio;

        //compress
        for(int i= 0; i< height; ++i){
            for(int j= 0; j< width; ++j){
                pix[i*width+j] = pix[ratio * (i*origin_width+j)];
            }
        }
    }

    private static void binary(){
        int rawColor = 0;
        int grey = 0;
        long total_grey = 0;

        //replace color with grey and get max grey
        for(int i= 0; i< height; ++i){
            for(int j= 0; j< width; ++j){
                rawColor = pix[i*width+j] & 0x00ffffff;
                //grey = (77*pix.R + 150*pix.G + 29*pix.B + 128) >> 8;
                grey = ((rawColor & 0xff)*29 + ((rawColor >> 8) & 0xff)*150 + ((rawColor >> 16) & 0xff)*77 + 128) >> 8;
                pix[i*width+j] = grey;
            }
        }


        //testing grey picture
        int[] testingPix = new int[width * height];
        for(int i= 0; i< height; ++i){
            for(int j= 0; j< width; ++j){
                grey = pix[i*width+j];
                testingPix[i*width+j] = grey | (grey << 8) | (grey << 16) | 0xff000000;
            }
        }
        Bitmap outBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        outBitmap.setPixels(testingPix, 0, width, 0, 0, width, height);
        //end test
/*
        //enhance the picture
        //直方图均衡化法
        final int MAX_GREY = 256;
        double[] greyRatio = new double[MAX_GREY];
        for(int i= 0; i< MAX_GREY; ++i){
            greyRatio[i] = 0;
        }

        //get grey ratio
        for(int i= 0;i< height; ++i){
            for(int j= 0; j< width; ++j){
                greyRatio[pix[i*width+j]]++;
            }
        }
        for(int i= 0; i< MAX_GREY; ++i){
            greyRatio[i] = greyRatio[i]/(width*height);
        }

        //change f to F (start with 0% and end with 100%)
        for(int i= 1; i< MAX_GREY; ++i){
            greyRatio[i] += greyRatio[i-1];
        }

        //get result
        for(int i= 0; i< height; ++i) {
            for (int j = 0; j < width; ++j) {
                pix[i*width+j] = (int)(greyRatio[pix[i*width+j]]*(MAX_GREY-1));
            }
        }

        //testing grey picture
        for(int i= 0; i< height; ++i){
            for(int j= 0; j< width; ++j){
                grey = pix[i*width+j];
                testingPix[i*width+j] = grey | (grey << 8) | (grey << 16) | 0xff000000;
            }
        }
        outBitmap.setPixels(testingPix, 0, width, 0, 0, width, height);
        //end test
*/


        int max = 0;
        int min = 255;
        for(int i= 0; i< height; ++i){
            for(int j= 0; j< width; ++j){
                if(max < pix[i*width+j])
                    max = pix[i*width+j];
                if(min > pix[i*width+j])
                    min = pix[i*width+j];
            }
        }

        //replace grey with 1 and 0
        // total grey
        total_grey = 0;
        for(int i= 0; i< height; ++i){
            for(int j= 0; j< width; ++j){
                total_grey += pix[i*width+j];
            }
        }
        long avg_grey = total_grey / (height * width);
        avg_grey = (avg_grey + (max + min)/2)/2;
        for(int i= 0; i< height; ++i) {
            for (int j = 0; j < width; ++j) {
                if(pix[i*width+j] < avg_grey)
                    //black
                    pix[i*width+j] = 0xff000000;
                else
                    //white
                    pix[i*width+j] = 0xffffffff;
            }
        }
    }

    private static void deNoise(){
        int[] oldPix = new int[width * height];
        for(int i= height*width-1; i>= 0; --i){
            oldPix[i] = pix[i];
        }
        int whiteSum;
        for(int i= 1; i< height-1; ++i){
            for(int j= 1; j< width-1; ++j){
                whiteSum = 0;
                whiteSum += oldPix[i*width+j] & 0x1;
                whiteSum += oldPix[(i-1)*width+j] & 0x1;
                whiteSum += oldPix[(i+1)*width+j] & 0x1;
                whiteSum += oldPix[i*width+j+1] & 0x1;
                whiteSum += oldPix[i * width + j - 1] & 0x1;
                if(whiteSum < 3)
                    //black
                    pix[i*width+j] = 0xff000000;
                else
                    //white
                    pix[i*width+j] = 0xffffffff;

            }
        }
    }
}
