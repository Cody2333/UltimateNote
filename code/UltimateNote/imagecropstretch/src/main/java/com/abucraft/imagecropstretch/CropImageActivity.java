package com.abucraft.imagecropstretch;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.ref.WeakReference;


public class CropImageActivity extends Activity {

    //Scheme ,用来判断Uri的文件类型
    private static final String SCHEME_FILE = "file";
    private static final String SCHEME_CONTENT = "content";

    //请求类型
    public static final int REQ_CROP_BASE=6709;
    public static final String REQUEST="request";

    //返回结果的字段
    public static final String ERROR="error";
    public static final String EXCEPTION="exception";

    //resultCode，用来表示裁剪是否成功
    public static final int RESULT_OK=100;
    public static final int RESULT_ERROR=200;

    //bitmap大小限制
    public static final int MAX_BITMAP=2000000;

    //错误类型
    //无错误
    public static final int ERROR_NULL=1;

    //没有源图片
    public static final int ERROR_NOSOURCE=2;

    //没有设置用来保存结果图片的Uri
    public static final int ERROR_NODEST=3;

    //执行时遇到的错误，主要是用户将裁剪框移动到了图片之外
    public static final int ERROR_EXCUTE=4;

    //文件读写错误
    public static final int ERROR_IO=5;

    //用户返回
    public static final int ERROR_USERCLOSE=6;
    CropImageView myView;

    //imageView的状态(裁剪，移动)
    protected int state=CropImageView.FIRST_MOVE;

    //进程对话框，提示
    ProgressDialog dialog;
    ImageButton cropBtn;
    ImageButton stretchBtn;
    Uri srcUri;
    Uri desUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crop_image);
        //获取源图片
        int resCode=setupFromIntent();
        if(setupFromIntent()!=ERROR_NULL){
            finishActivity(RESULT_ERROR,resCode,null,null);
            return;
        }
        myView= (CropImageView) findViewById(R.id.view);
        cropBtn=(ImageButton)findViewById(R.id.main_crop_btn);
        stretchBtn=(ImageButton)findViewById(R.id.main_stretch_btn);
        BitmapWorkerTask bmTask=new BitmapWorkerTask(myView);
        bmTask.execute(getImagePathFromUri(srcUri));
    }

    protected int setupFromIntent(){
        Intent intent=getIntent();
        Bundle extras=intent.getExtras();

        srcUri=intent.getData();
        if(srcUri==null){
            return ERROR_NOSOURCE;
        }
        if(extras!=null){
            desUri = extras.getParcelable(MediaStore.EXTRA_OUTPUT);

            if(desUri==null){
                return ERROR_NODEST;
            }
        }

        return ERROR_NULL;
    }


    //结束Activity并设置相应返回值
    protected void finishActivity(int resCode,int errorCode,Throwable exception,Uri resultUri){
        Intent rIntent=new Intent();
        rIntent.putExtra(ERROR,errorCode);
        rIntent.putExtra(EXCEPTION,exception);
        rIntent.putExtra(MediaStore.EXTRA_OUTPUT,resultUri);
        setResult(resCode, rIntent);
        //关闭进程对话框
        if(dialog!=null){
            dialog.dismiss();
        }
        finish();
        return;
    }


    //异步加载图片
    class BitmapWorkerTask extends AsyncTask<String, Void, Bitmap> {
        private final WeakReference<ImageView> imageViewReference;
        private Integer pathName = null;

        public BitmapWorkerTask(ImageView imageView) {
            // Use a WeakReference to ensure the ImageView can be garbage collected
            imageViewReference = new WeakReference<ImageView>(imageView);
        }

        // Decode image in background.
        @Override
        protected Bitmap doInBackground(String... params) {
            String path=params[0];
            return decodeSampledBitmapFromFile(path, MAX_BITMAP);
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


    //从Uri获取图片路径
    public String getImagePathFromUri(Uri uri){
        if(uri.getScheme().equals(SCHEME_FILE)){
            return uri.getPath();
        }
        if (uri.getScheme().equals(SCHEME_CONTENT)) {
            String[] proj = {MediaStore.Images.Media.DATA};
            Cursor cursor = managedQuery(uri, proj, null, null, null);
            int columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            String bitmapPath = cursor.getString(columnIndex);
            return bitmapPath;
        }
        return null;
    }


    //计算图片缩放程度
    public static int calculateInSampleSize(
            BitmapFactory.Options options, int maxArea) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;
        if (height*width>maxArea) {

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((height*width/(inSampleSize*inSampleSize))>maxArea) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }

    public static Bitmap decodeSampledBitmapFromFile(String path,int maxArea) {

        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, options);
        options.inPurgeable = true;
        options.inInputShareable = true;
        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, maxArea);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFile(path, options);
    }


    //异步裁剪图片，并保存结果
    class BitmapCropTask extends AsyncTask<Integer,Void,Void>{
        @Override
        protected Void doInBackground(Integer... integers) {
            Bitmap resultBitmap=myView.getResultBitmap();
            if(resultBitmap==null){
                finishActivity(RESULT_ERROR,ERROR_EXCUTE,null,null);
                return null;
            }
            if(desUri!=null){
                OutputStream outStream=null;
                try{
                    outStream = getContentResolver().openOutputStream(desUri);
                    if (outStream != null) {
                        resultBitmap.compress(Bitmap.CompressFormat.JPEG, 100, outStream);
                    }
                }catch (IOException e){
                    //io错误
                    finishActivity(RESULT_ERROR,ERROR_IO,e,null);
                    return null;
                }finally {
                    if(outStream!=null){
                        try {
                            outStream.close();
                        }catch (Throwable t){
                            //什么都不干
                        }
                    }
                }
            }
            finishActivity(RESULT_OK,ERROR_NULL,null,desUri);
            return null;
        }
    }


    //改变按钮状态
    public void changeFocus(){
        Resources resources=getResources();
        switch (state){
            case CropImageView.FIRST_CROP:
                cropBtn.setBackgroundColor(resources.getColor(R.color.btnFocused));
                stretchBtn.setBackgroundResource(R.drawable.crop_selectable_background);
                break;
            case CropImageView.FIRST_MOVE:
                cropBtn.setBackgroundResource(R.drawable.crop_selectable_background);
                stretchBtn.setBackgroundResource(R.drawable.crop_selectable_background);
                break;
            case CropImageView.FIRST_STRETCH:
                cropBtn.setBackgroundResource(R.drawable.crop_selectable_background);
                stretchBtn.setBackgroundColor(resources.getColor(R.color.btnFocused));
                break;
        }
        return;
    }


    //旋转按钮触发事件
    public void onRotateClick(View view){
        myView.rotateBitmap();
        return;
    }

    //裁剪按钮触发事件
    public void onCropClick(View view) {
        //如果imageView的模式不是裁剪模式，就将其设置为裁剪模式
        if(state!=CropImageView.FIRST_CROP){
            state=CropImageView.FIRST_CROP;
            myView.setModeCrop();
            changeFocus();
        }
        //如果是裁剪模式，则设置为移动模式
        else {
            state=CropImageView.FIRST_MOVE;
            myView.setModeDrag();
            changeFocus();
        }
        return;
    }

    public void onStretchClick(View view){
        if(state!=CropImageView.FIRST_STRETCH){
            state=CropImageView.FIRST_STRETCH;
            myView.setModeStretch();
            changeFocus();
        }
        else {
            state=CropImageView.FIRST_MOVE;
            myView.setModeDrag();
            changeFocus();
        }
        return;
    }
    //按下左上角确认按钮，则裁剪图片并返回
    public void onCheckClick(View view){
        //显示对话框提醒用户等待
        dialog = ProgressDialog.show(this, getString(R.string.crop_dialog_title), getString(R.string.crop_dialog_content), true, false);
        BitmapCropTask bitmapCropTask=new BitmapCropTask();
        bitmapCropTask.execute(0);
    }

    //右上角返回按钮返回
    public void onReturnClick(View view){
        finishActivity(RESULT_ERROR,ERROR_USERCLOSE,null,null);
    }
}
