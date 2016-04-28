package com.project.ultimatenote.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.SpeechRecognizer;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.speech.VoiceRecognitionService;
import com.dxjia.library.BaiduVoiceHelper;
import com.project.ultimatenote.Constant;
import com.project.ultimatenote.R;
import com.project.ultimatenote.entity.Note;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;


public class EditActivity extends AppCompatActivity implements RecognitionListener{
    static final int GET_IMAGE_FROM_OTHER = 11;  //图片信息
    static final int GET_OCR_RESULT = 12;        //语音信息
    private long cTime;                          //创建时间
    private long rTime;                          //更改时间
    private ArrayList<String> tagsArray;         //标签信息
    private String ocrResult;                    //语音结果
    private Uri resultUri;                       //结果的URI
    private boolean cloudNote;                   //是否同步到云端
    private Context context;
    private SpeechRecognizer speechRecognizer;
    @Bind(R.id.toolbar)
    Toolbar toolbar;
    @Bind(R.id.id_note_scrollView)
    ScrollView scrollView;
    @Bind(R.id.noteText)
    EditText noteText;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);
        ButterKnife.bind(this);
        context = this;
        //初始化toolbar
        setSupportActionBar(toolbar);

        scrollView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                noteText.requestFocus();
            }
        });

        //初始化ActionBar
        final ActionBar ab = getSupportActionBar();
        ab.setHomeAsUpIndicator(R.drawable.ic_check_white);
        ab.setDisplayHomeAsUpEnabled(true);

        //get intent to decide whether editing note or creating note
        Intent intent = getIntent();
        cTime = intent.getLongExtra(MainActivity.C_TIME, 0);
        rTime = intent.getLongExtra(MainActivity.TIME, 0);
        if(rTime != 0){
            initNote(rTime);
        }
    }

    @Override
    public void onReadyForSpeech(Bundle params) {

    }

    @Override
    public void onBeginningOfSpeech() {

    }

    @Override
    public void onRmsChanged(float rmsdB) {

    }

    @Override
    public void onBufferReceived(byte[] buffer) {

    }

    @Override
    public void onEndOfSpeech() {

    }

    @Override
    public void onError(int error) {

    }

    @Override
    public void onResults(Bundle results) {
        ArrayList<String> nbest = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
        String json_res = results.getString("origin_result");
        //get selected text
        ViewGroup group = ((ViewGroup) findViewById(R.id.noteGroup));
        EditText selectedText = (EditText) group.findFocus();
        if (selectedText == null) {
            selectedText = (EditText) group.getChildAt(0);
            selectedText.setSelection(0);
        }
        int i = 0;
        for (; i < group.getChildCount(); ++i) {
            if (selectedText == group.getChildAt(i))
                break;
        }
        EditText editText = (EditText) group.getChildAt(i);
        JSONObject jsonObject = null;
        try{
            jsonObject = new JSONObject(json_res);
            JSONArray items = jsonObject.getJSONObject("content").getJSONArray("item");
            String itemStr = (String)items.get(0);
            Toast.makeText(EditActivity.this, itemStr, Toast.LENGTH_SHORT).show();
            editText.append(itemStr);
        }catch (JSONException e){
            e.printStackTrace();
        }


    }

    @Override
    public void onPartialResults(Bundle partialResults) {

    }

    @Override
    public void onEvent(int eventType, Bundle params) {

    }
    @Override
    protected void onStart() {
        super.onStart();
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this, new ComponentName(this, VoiceRecognitionService.class));
        speechRecognizer.setRecognitionListener(this);
    }

    public void bindParams(Intent intent) {

    }

    private void startRecongnize() {
        Intent intent = new Intent();
        bindParams(intent);
        Log.i("audio recongnize", "start listening");
        BaiduVoiceHelper.startBaiduVoiceDialogForResult(EditActivity.this,
                Constant.BAI_DU_DEMO_APIKEY, Constant.BAI_DU_DEMO_SECRET, Constant.REQUEST_UI);
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_edit, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }

        switch (item.getItemId()) {
            case R.id.menu_audio:
                startRecongnize();
                break;
            case android.R.id.home:
                //save button
                Long newTime = saveNote();
                Intent intent = getIntent();
                intent.putExtra(MainActivity.TIME, newTime);
                intent.putExtra(MainActivity.C_TIME, cTime);
                setResult(ShowActivity.EDIT_NOTE, intent);
                this.finish();
                break;
            case R.id.menu_photo_gallery:
                //photo from gallery
                Intent pickImageIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(pickImageIntent,GET_IMAGE_FROM_OTHER);
                break;
            //todo take photo
           // case R.id.menu_photo_camera:
             //   break;
            case R.id.menu_tag:
                AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
                final EditText tagsText = new EditText(this);
                tagsText.setSingleLine(true);
                String oldTag = "";
                if(tagsArray != null){
                    for(int i= 0; i< tagsArray.size(); ++i){
                        oldTag += tagsArray.get(i);
                        oldTag += " ";
                    }
                }
                tagsText.setText(oldTag);
                alertDialog.setTitle("标签设置")
                .setView(tagsText).setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String[] tags = tagsText.getText().toString().split(" ");
                        if(tagsArray == null)
                            tagsArray = new ArrayList<>();
                        else
                            tagsArray.clear();
                        for (int i = 0; i < tags.length; ++i) {
                            if(!tags[i].equals(""))
                                tagsArray.add(tags[i]);
                        }
                        createTag(tagsArray);
                    }
                })
                .setNegativeButton("取消", null).show();
                break;
            case R.id.menu_return:
                myFinishActivity();
                break;
            default:
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode!=GET_IMAGE_FROM_OTHER && requestCode !=GET_OCR_RESULT) {
            onResults(data.getExtras());
        }else
        if(requestCode==GET_IMAGE_FROM_OTHER) {
            if(data!=null) {
                Uri imgData = data.getData();
                Intent intent = new Intent(this, EditPhotoActivity.class);
                intent.setData(imgData);
                startActivityForResult(intent, GET_OCR_RESULT);
            }
        }
        else if(requestCode==GET_OCR_RESULT){
            if(resultCode==EditPhotoActivity.OCR_SUCCESS){
                ocrResult=data.getStringExtra(EditPhotoActivity.OCR_RESULT);
                resultUri=data.getData();

                //insert picture
                if (resultUri != null) {
                    Uri pic = data.getData();
                    InputStream is = null;
                    try {
                        is = getContentResolver().openInputStream(pic);
                        BitmapFactory.Options opt = new BitmapFactory.Options();
                        opt.inPurgeable = true;
                        opt.inInputShareable = true;
                        BitmapFactory.decodeStream(is, null, opt);
                        opt.inSampleSize = calculateInSampleSize(opt, 300, 300);
                        is.close();
                        is = getContentResolver().openInputStream(pic);
                        Bitmap bitmap = BitmapFactory.decodeStream(is, null, opt);
                        is.close();
                        final ViewGroup group = (ViewGroup) findViewById(R.id.noteGroup);
                        //remove origin hint
                        ((EditText) group.getChildAt(0)).setHint("");
                        //image view
                        ImageView imageView = new ImageView(this);
                        imageView.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
                        imageView.setImageBitmap(bitmap);
                        imageView.setAdjustViewBounds(true);
                        imageView.setOnLongClickListener(new View.OnLongClickListener() {
                            @Override
                            public boolean onLongClick(final View v) {
                                new AlertDialog.Builder(group.getContext())
                                        .setTitle("操作")
                                        .setPositiveButton("删除", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                int i = 0;
                                                for (i = 0; i < group.getChildCount(); ++i) {
                                                    if (group.getChildAt(i) == v)
                                                        break;
                                                }
                                                //merge text
                                                String str2 = ((EditText) group.getChildAt(i + 1)).getText().toString();
                                                ((EditText) group.getChildAt(i - 1)).append(str2);
                                                group.removeViewAt(i + 1);
                                                group.removeView(v);
                                                ((EditText)group.getChildAt(i-1)).requestFocus();
                                                if (group.getChildCount() == 1) {
                                                    ((EditText) group.getChildAt(i - 1)).setHint("请输入笔记内容");
                                                }
                                            }
                                        })
                                        .setNegativeButton("取消", null).show();
                                return false;
                            }
                        });
                        //get selected text
                        EditText selectedText = (EditText) group.findFocus();
                        if(selectedText == null){
                            selectedText = (EditText)group.getChildAt(0);
                            selectedText.setSelection(0);
                        }
                        int i = 0;
                        for (; i < group.getChildCount(); ++i) {
                            if (selectedText == group.getChildAt(i))
                                break;
                        }
                        group.addView(imageView, i + 1);
                        //text view
                        EditText editText = new EditText(this);
                        editText.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
                        editText.setTextSize(20);
                        editText.setBackgroundDrawable(null);
                        editText.setMinEms(2);
                        editText.requestFocus();
                        //add split text
                        int position = selectedText.getSelectionStart();
                        String str = selectedText.getText().toString();
                        selectedText.setText(str.substring(0, position));
                        editText.setText(str.substring(position));
                        group.addView(editText, i + 2);
                    } catch (Exception e) {
                        return;
                    }
                }

                //insert text
                if(ocrResult != null){
                    //get selected text
                    ViewGroup group = ((ViewGroup)findViewById(R.id.noteGroup));
                    EditText selectedText = (EditText) group.findFocus();
                    if(selectedText == null){
                        selectedText = (EditText)group.getChildAt(0);
                        selectedText.setSelection(0);
                    }
                    int i = 0;
                    for (; i < group.getChildCount(); ++i) {
                        if (selectedText == group.getChildAt(i))
                            break;
                    }
                    EditText editText = (EditText)group.getChildAt(i);
                    if(resultUri != null){
                        //picture inserted
                        editText.setText(ocrResult + editText.getText().toString());
                    }else{
                        int position = selectedText.getSelectionStart();
                        String originStr = editText.getText().toString();
                        editText.setText(originStr.substring(0, position) + ocrResult + originStr.substring(position));
                    }
                }
            }
        }
    }

    public void onActivitySaveNote(View view){
        saveNote();
        this.finish();
    }

    public void onActivityGiveUpShow(View view){
        finish();
    }

    public static int dip2px(Context context, float dpValue){
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int)(dpValue*scale + 0.5f);
    }

    public static int px2dip(Context context, float pxValue){
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int)(pxValue/scale + 0.5f);
    }

    public void addPhoto(View view){
        Intent pickImageIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(pickImageIntent,GET_IMAGE_FROM_OTHER);
    }

    protected void setTime(String time) {
        TextView editTime = (TextView)findViewById(R.id.text_time);
        editTime.setText(time);
    }

    protected void setTitle(String title) {
        EditText editTitle = (EditText)findViewById(R.id.text_title);
        editTitle.setText(title);
    }

    protected void createTag(ArrayList<String> tag) {
        TableLayout tableLayout = (TableLayout)findViewById(R.id.tableLayout);
        int cnt = tableLayout.getChildCount();
        if(cnt != 0)
            tableLayout.removeViewAt(cnt-1);

        if(tag.size() == 0)
            return;

        LinearLayout tagRow=new LinearLayout(this);
        tableLayout.addView(tagRow);
        int number = tag.size();
        int now = 0;
        int row = 0;
        int line = 0;
        while(now<number)
        {
            TextView textView = new TextView(this);

            textView.setText(tag.get(now));
            textView.setTextColor(Color.parseColor("#616161"));
            textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16);
            Drawable drawable = this.getResources().getDrawable(R.drawable.ic_tag_small_amber);
            drawable.setBounds(0, 0, dip2px(this, 16), dip2px(this, 16));

            textView.setCompoundDrawables(drawable, null, null, null);
            textView.setCompoundDrawablePadding(dip2px(this, 8));

            textView.measure(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            tagRow.measure(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            int tag_width;
            if(line!=0) tag_width = textView.getMeasuredWidth()+dip2px(this,16);
            else tag_width = textView.getMeasuredWidth();
            int table_width = dip2px(this,px2dip(this,this.getResources().getDisplayMetrics().widthPixels)-32);
            int now_width = tagRow.getMeasuredWidth();

            if(now_width+tag_width>table_width) {
                tagRow = new LinearLayout(this);
                tableLayout.addView(tagRow);
                tagRow.setPadding(0, dip2px(this, 8),0,0);
                line=0;
                row++;
            }

            if(line!=0)
                textView.setPadding(dip2px(this,16),0,0,0);

            tagRow.addView(textView);
            line++;
            now++;
        }
    }


    private void initNote(long time){
        try {
            Context ctx = this;
            String filePath = ctx.getFilesDir().getAbsolutePath();

            FileInputStream fis = new FileInputStream(filePath+"/describe/"+time);
            byte[] b2 = new byte[fis.available()];
            fis.read(b2);
            fis.close();
            String describe = new String(b2, "utf-8");

            //draw the note
            //clean
            final ViewGroup group = (ViewGroup) findViewById(R.id.noteGroup);
            while(group.getChildCount() > 1){
                group.removeViewAt(1);
            }

            //draw
            Bitmap bitmap = null;
            String[] counts = describe.split("\n");
            if(counts[0].equals("N")){
                cloudNote = false;
            }else{
                cloudNote = true;
            }
            String document = null;


            //title
            ((EditText)findViewById(R.id.text_title)).setText(counts[1]);

            //tag
            String[] tags = counts[2].split(" ");
            if(tagsArray == null)
                tagsArray = new ArrayList<>();
            else
                tagsArray.clear();
            for (int i = 0; i < tags.length; ++i) {
                if(!tags[i].equals(""))
                    tagsArray.add(tags[i]);
            }
            createTag(tagsArray);

            //time
            Date date = new Date(rTime);
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

            if(cloudNote)
                setTime(sdf.format(date) + "  @cloud");
            else
                setTime(sdf.format(date));

            //words
            fis = new FileInputStream(filePath+"/document/"+time);
            byte[] b = new byte[Integer.parseInt(counts[3])];
            fis.read(b, 0, b.length);
            document = new String(b, "utf-8");
            ((EditText)group.getChildAt(0)).setText(document);


            //pic and word
            for(int i= 4; i< counts.length; ++i){
                BitmapFactory.Options opt = new BitmapFactory.Options();
                opt.inPurgeable = true;
                opt.inInputShareable = true;
                bitmap = BitmapFactory.decodeFile(filePath + "/image/" + time + "_" + String.valueOf(i - 4), opt);
                ImageView imageView = new ImageView(this);
                imageView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                imageView.setImageBitmap(bitmap);
                imageView.setAdjustViewBounds(true);
                imageView.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(final View v) {
                        new AlertDialog.Builder(group.getContext())
                                .setTitle("操作")
                                .setPositiveButton("删除", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        int i = 0;
                                        for (i = 0; i < group.getChildCount(); ++i) {
                                            if (group.getChildAt(i) == v)
                                                break;
                                        }
                                        //merge text
                                        String str2 = ((EditText) group.getChildAt(i + 1)).getText().toString();
                                        ((EditText) group.getChildAt(i - 1)).append(str2);
                                        group.removeViewAt(i + 1);
                                        group.removeView(v);
                                        ((EditText) group.getChildAt(i - 1)).requestFocus();
                                        if (group.getChildCount() == 1) {
                                            ((EditText) group.getChildAt(i - 1)).setHint("请输入笔记内容");
                                        }
                                    }
                                })
                                .setNegativeButton("取消", null).show();
                        return false;
                    }
                });
                group.addView(imageView, 2 * i - 7);


                //textView
                b = new byte[Integer.parseInt(counts[i])];
                fis.read(b, 0, b.length);
                document = new String(b, "utf-8");

                EditText editText = new EditText(this);
                editText.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                editText.setTextSize(20);
                editText.setBackgroundDrawable(null);
                editText.setMinEms(2);
                editText.setText(document);

                group.addView(editText, 2 * i - 6);
            }

            fis.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Long saveNote(){
        ViewGroup group = (ViewGroup)findViewById(R.id.noteGroup);
        Context ctx = this;
        String filePath = ctx.getFilesDir().getAbsolutePath();
        String title = ((EditText)findViewById(R.id.text_title)).getText().toString();
        if(title.equals("")){
            title = "未命名";
        }
        String tags = "";
        if(tagsArray != null){
            for(int i= 0; i< tagsArray.size(); ++i){
                tags += tagsArray.get(i);
                tags += " ";
            }
        }

        String document = "";
        String describe;
        if(cloudNote)
            describe = "Y\n"+title+"\n"+tags+"\n";
        else
            describe = "N\n"+title+"\n"+tags+"\n";
        Long newTime = System.currentTimeMillis();
        if(cTime == 0) {
            //new note
            cTime = newTime;

            //check whether cTime has been used
            try {
                //get note cTime list
                FileInputStream fis = new FileInputStream(filePath + "/noteList");
                List<Long> cTimes = new ArrayList<>();
                if (fis.available() != 0) {
                    byte[] b = new byte[fis.available()];
                    fis.read(b);
                    fis.close();
                    String[] notesStr = new String(b, "utf-8").split("\n");
                    for (int i = 0; i < notesStr.length; i += 2) {
                        String crTime = notesStr[i];
                        int pos = crTime.indexOf("_");
                        String notesCTime = crTime.substring(0, pos);
                        cTimes.add(Long.parseLong(notesCTime));
                    }
                }

                //check cTime
                boolean hasSame = true;
                while (hasSame) {
                    hasSame = false;
                    for (int i = 0; i < cTimes.size(); ++i) {
                        if (cTimes.get(i) == cTime) {
                            cTime++;
                            hasSame = true;
                            break;
                        }
                    }
                }
                newTime = cTime;
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        //save note
        for(int i= 0; i< group.getChildCount(); i++){
            if(i%2 == 0){
                document += ((EditText)group.getChildAt(i)).getText().toString();
                describe += String.valueOf(((EditText)group.getChildAt(i)).getText().toString().getBytes().length);
                describe += "\n";
            }else{
                try {
                    FileOutputStream fos = new FileOutputStream(filePath+"/image/"+String.valueOf(newTime)+"_"+String.valueOf(i/2));
                    ((ImageView)group.getChildAt(i)).setDrawingCacheEnabled(true);
                    ((ImageView) group.getChildAt(i)).getDrawingCache().compress(Bitmap.CompressFormat.JPEG, 90, fos);
                    fos.flush();
                    fos.close();
                    ((ImageView)group.getChildAt(i)).setDrawingCacheEnabled(false);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        try {
            FileOutputStream fos = new FileOutputStream(filePath+"/document/"+String.valueOf(newTime));
            if(document.equals(""))
                fos.write(" ".getBytes("utf-8"));
            else
                fos.write(document.getBytes("utf-8"));
            fos.flush();
            fos.close();
            fos = new FileOutputStream(filePath+"/describe/"+String.valueOf(newTime));
            fos.write(describe.getBytes("utf-8"));
            fos.flush();
            fos.close();

            //note list file
            fos = new FileOutputStream(filePath+"/noteList", true);
            fos.write((String.valueOf(cTime) + "_" + String.valueOf(newTime) + "\n" + title + "\n").getBytes("utf-8"));
            fos.flush();
            fos.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        //delete old note
        if(rTime != 0) {
            try {
                Note.deleteNote(rTime, cTime,  filePath);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return newTime;
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

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            myFinishActivity();
        }
        return false;
    }

    private void myFinishActivity(){
        new AlertDialog.Builder(this)
                .setMessage("放弃此次编辑吗?")
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent rIntent = getIntent();
                        rIntent.putExtra(MainActivity.TIME, rTime);
                        rIntent.putExtra(MainActivity.C_TIME, cTime);
                        setResult(ShowActivity.EDIT_NOTE, rIntent);
                        ((Activity)context).finish();
                    }
                })
                .setNegativeButton("取消", null).show();
    }
}
