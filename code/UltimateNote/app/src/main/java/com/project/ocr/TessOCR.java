package com.project.ocr;


import android.graphics.Bitmap;
import com.googlecode.tesseract.android.TessBaseAPI;

public class TessOCR {
    public static int CHINESE = 0;
    public static int ENGLISH = 1;
    private TessBaseAPI mTess;
    private String dataPath;

    public TessOCR(String path) {
        dataPath = path;
        mTess = new TessBaseAPI();
    }
    public String getOCRResult(Bitmap bitmap, int language) {
        /*
         * directory should be ensured out
         * sample:
         * dataPath: .../tesseract
         * language package should be in: .../tesseract/tessdata
         * such as: .../tesseract/tessdata/eng.traineddata
         *          .../tesseract/tessdata/chi_sim.traineddata
         */


        if(language == CHINESE) {
            mTess.init(dataPath, "chi_sim");
        }else if(language == ENGLISH){
            mTess.init(dataPath, "eng");
        }else{
            return "";
        }
        Bitmap outBitmap = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        PreProcess.doProcess(bitmap, outBitmap);
        mTess.setImage(outBitmap);
        String result = mTess.getUTF8Text();
        return result;
    }
    public void onDestroy() {
        if (mTess != null)
            mTess.end();
    }
}
