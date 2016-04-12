package com.project.ultimatenote.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;

import com.abucraft.imagecropstretch.CropImageActivity;
import com.project.ultimatenote.R;
import com.project.ocr.TessOCR;

import java.io.File;
import java.lang.ref.WeakReference;


public class EditPhotoActivity extends Activity {
    //result code
    public final static int OCR_SUCCESS=1;
    public final static int OCR_FAIL=2;
    //返回ocr结果的栏位
    public final static String OCR_RESULT="OCR_RESULT";
    private static final String SCHEME_FILE = "file";
    private static final String SCHEME_CONTENT = "content";
    ImageView imageView;
    EditText ocrText;
    Uri srcUri;
    String bitmapPath;
    String ocrResult=null;
    String filePath;
    Dialog waitDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_photo);
        filePath = this.getFilesDir().getAbsolutePath();
        imageView=(ImageView)findViewById(R.id.image_view);
        ocrText=(EditText)findViewById(R.id.edit_text);
        ocrText.clearFocus();
        initial();
    }

    private void initial(){
        Intent intent=getIntent();
        if(intent!=null){
            srcUri=intent.getData();
            if(srcUri==null){
                finishActivity(OCR_FAIL,null,null);
            }else {
                loadBitmap(srcUri, imageView);
                return;
            }
        }else {
            finishActivity(OCR_FAIL,null,null);
        }
    }


    private void finishActivity(int resCode,Uri img,String result){
        Intent intent=new Intent();
        intent.setData(img);
        intent.putExtra(OCR_RESULT, result);
        setResult(resCode, intent);
        finish();
        return;
    }


    public void ocrEnglish(){
        waitDialog=ProgressDialog.show(this,getString(R.string.dialog_ocr_wait_title),getString(R.string.dialog_ocr_wait_content),true,false);
        OcrTask ocrTask=new OcrTask(ocrText, TessOCR.ENGLISH);
        ocrTask.execute(bitmapPath);
    }

    public void ocrChinese(){
        waitDialog=ProgressDialog.show(this,getString(R.string.dialog_ocr_wait_title),getString(R.string.dialog_ocr_wait_content),true,false);
        OcrTask ocrTask=new OcrTask(ocrText, TessOCR.CHINESE);
        ocrTask.execute(bitmapPath);
    }

    public void onOcrClick(View view){
        AlertDialog.Builder builder=new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.dialog_select_language));
        final String[] ocrString={getString(R.string.dialog_language_english),getString(R.string.dialog_language_chinese)};
        builder.setItems(ocrString, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                switch (i){
                    case 0:
                        ocrEnglish();
                        break;
                    case 1:
                        ocrChinese();
                        break;
                    default:
                        break;
                }
            }
        });
        builder.show();
        return;
    }

    public void onReturnClick(View view){
        finishActivity(OCR_FAIL, null, null);
        return;
    }

    public void onCheckClick(View view){
        AlertDialog.Builder builder=new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.dialog_select_save_type));
        final String[] saveString={getString(R.string.dialog_save_image),getString(R.string.dialog_save_text),getString(R.string.dialog_save_image_text)};
        builder.setItems(saveString, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                switch (i){
                    case 0:
                        finishActivity(OCR_SUCCESS,srcUri,null);
                        break;
                    case 1:
                        ocrResult=ocrText.getText().toString();
                        finishActivity(OCR_SUCCESS,null,ocrResult);
                        break;
                    case 2:
                        ocrResult=ocrText.getText().toString();
                        finishActivity(OCR_SUCCESS,srcUri,ocrResult);
                        break;
                    default:
                        break;

                }
            }
        });
        builder.show();
        return;
    }

    public void onCropClick(View view){

        Uri dest=Uri.fromFile(new File(getCacheDir(),"cropped"));
        Intent intent=new Intent(this, CropImageActivity.class);
        intent.setData(srcUri);
        intent.putExtra(MediaStore.EXTRA_OUTPUT,dest);
        intent.putExtra(CropImageActivity.REQUEST,CropImageActivity.REQ_CROP_BASE);
        startActivityForResult(intent, CropImageActivity.REQ_CROP_BASE);
        return;
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==CropImageActivity.REQ_CROP_BASE){
            if(resultCode==CropImageActivity.RESULT_OK){
                //获取到裁剪之后的图片Uri
                srcUri=data.getParcelableExtra(MediaStore.EXTRA_OUTPUT);
                loadBitmap(srcUri,imageView);
                return;
            }
        }
        return;
    }

    class OcrTask extends AsyncTask<String, Void, String>{
        private final WeakReference<EditText> textViewReference;
        private int language;
        public OcrTask(EditText editText, int l) {
            this.textViewReference = new WeakReference<EditText>(editText);
            this.language = l;
        }

        @Override
        protected String doInBackground(String... strings) {
            String pathName=strings[0];
            Resources res =getResources();
            int req_size = res.getInteger(R.integer.default_img_size);
            Bitmap bitmap=decodeSampledBitmapFromFile(pathName, req_size, req_size);
            TessOCR tessOCR = new TessOCR(filePath+"/tesseract/");
            return tessOCR.getOCRResult(bitmap, language);
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            if(waitDialog!=null){
                waitDialog.dismiss();
            }
            if(textViewReference!=null && s!=null){
                final EditText textView=textViewReference.get();
                if(textView!=null) {
                    textView.setText(s);
                    ocrResult=s;
                }
            }
        }
    }

    public static int calculateInSampleSize(
            BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;
        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) > reqHeight
                    || (halfWidth / inSampleSize) > reqWidth) {
                //inSampleSize *= 2;
                inSampleSize ++;
            }
        }

        return inSampleSize;
    }

    public static Bitmap decodeSampledBitmapFromFile(String pathName,int reqWidth, int reqHeight) {

        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;

        options.inPurgeable = true;
        options.inInputShareable = true;

        BitmapFactory.decodeFile(pathName, options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;

        return BitmapFactory.decodeFile(pathName, options);
    }


    class BitmapWorkerTask extends AsyncTask<String, Void, Bitmap> {
        private final WeakReference<ImageView> imageViewReference;
        private String pathName = null;

        public BitmapWorkerTask(ImageView imageView) {
            // Use a WeakReference to ensure the ImageView can be garbage collected
            imageViewReference = new WeakReference<ImageView>(imageView);
        }

        // Decode image in background.
        @Override
        protected Bitmap doInBackground(String... params) {
            pathName = params[0];
            Resources res = getResources();
            int req_size = res.getInteger(R.integer.default_img_size);
            return decodeSampledBitmapFromFile(pathName, req_size, req_size);
        }

        // Once complete, see if ImageView is still around and set bitmap.
        @Override
        protected void onPostExecute(Bitmap bitmap) {
            if (imageViewReference != null && bitmap != null) {
                final ImageView imageView = imageViewReference.get();
                if (imageView != null) {
                    imageView.setImageBitmap(bitmap);
                }
            }
        }
    }


    public void loadBitmap(Uri src, ImageView imageView) {
        if(src.getScheme().equals(SCHEME_FILE)) {
            bitmapPath = src.getPath();
            BitmapWorkerTask task = new BitmapWorkerTask(imageView);
            task.execute(bitmapPath);
        }
        if(src.getScheme().equals(SCHEME_CONTENT)) {
            String[] proj = {MediaStore.Images.Media.DATA};
            Cursor cursor = managedQuery(src, proj, null, null, null);
            int columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            bitmapPath = cursor.getString(columnIndex);
            BitmapWorkerTask task = new BitmapWorkerTask(imageView);
            task.execute(bitmapPath);
        }
    }


}
