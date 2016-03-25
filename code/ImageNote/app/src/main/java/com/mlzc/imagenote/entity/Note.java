package com.mlzc.imagenote.entity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Environment;
import android.support.v7.internal.widget.FitWindowsFrameLayout;
import android.util.Log;

import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVFile;
import com.avos.avoscloud.AVObject;
import com.avos.avoscloud.AVQuery;
import com.avos.avoscloud.AVUser;
import com.avos.avoscloud.FindCallback;
import com.avos.avoscloud.GetDataCallback;
import com.avos.avoscloud.SaveCallback;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;


public class Note {
    private String title;
    private String tag;
    private String username;
    private String describeFilePath;
    private String documentPath;
    private ArrayList<String> picturePaths;
    private  int pictureCnt;
    private long createTime;
    private long reviseTime;
    private boolean cloudNote;
    private boolean isPublic;

    //for note list
    public String getUserName() {
        return username;
    }

    public void  setUserName(String s) { username = s;  }

    public String getTitle() {
        return title;
    }

    public void setTitle(String t){
        title = t;
    }

    public long getCreateTime(){
        return createTime;
    }

    public void setCreateTime(long t){
        createTime = t;
    }

    public long getReviseTime(){
        return reviseTime;
    }


    public void setReviseTime(long t){
        reviseTime = t;
    }

    public void setPictureCnt(int cnt){
        pictureCnt = cnt;
    }

    public boolean getIsPublic(){ return isPublic; }

    public void setIsPublic(boolean b){ isPublic = b; }

    //for database
    public String getDescribeFilePath() {
        return describeFilePath;
    }

    public String getDocumentPath() {
        return documentPath;
    }

    public ArrayList<String> getPicturePaths() {
       return picturePaths;
    }

    public void setDescribeFilePath(String path) {
        describeFilePath = path;
    }

    public void setDocumentPath(String path) {
        documentPath = path;
    }

    public void setPicturePaths(ArrayList<String> paths) {
        picturePaths = new ArrayList<String>(paths);
    }

    public boolean isCloudNote(){
        return cloudNote;
    }

    public void setCloudNote(boolean c){
        cloudNote = c;
    }

    public static void deleteNote(long time, long cTime, String filePath) throws IOException {
        File file = new File(filePath+"/document/"+String.valueOf(time));
        if(file.exists())
            file.delete();
        file = new File(filePath+"/describe/"+String.valueOf(time));
        if(file.exists())
            file.delete();
        int i= 0;
        while(true){
            file = new File(filePath+"/image/"+String.valueOf(time)+"_"+String.valueOf(i));
            if(file.exists()) {
                file.delete();
                ++i;
            }else{
                break;
            }
        }

        //delete from note list
        FileInputStream fis = new FileInputStream(filePath+"/noteList");
        byte[] b = new byte[fis.available()];
        fis.read(b);
        String[] notes = (new String(b, "utf-8")).split("\n");
        fis.close();

        String crTime = String.valueOf(cTime) + "_" + String.valueOf(time);

        FileOutputStream fos = new FileOutputStream(filePath+"/noteList");
        for(i= 0; i< notes.length; i+=2){
            if(notes[i].equals(crTime))
                continue;
            fos.write((notes[i]+"\n"+notes[i+1]+"\n").getBytes());
        }
        fos.close();
    }

    public long saveInCloud(String openLevel){
        //first check whether there is same cTime in cloud
        AVQuery<AVObject> query = new AVQuery<>("Notes");
        query.whereEqualTo("username", username);
        List<AVObject> avObjects = null;
        try {
            avObjects = query.find();
            List<Long> cTimes = new ArrayList<>();
            for(int i= 0; i< avObjects.size(); ++i){
                cTimes.add(Long.parseLong((String) avObjects.get(i).get("createTime")));
            }
            if(!cloudNote) {
                //check cTime
                boolean hasSame = true;
                while (hasSame) {
                    hasSame = false;
                    for (int i = 0; i < cTimes.size(); ++i) {
                        if (cTimes.get(i) == createTime) {
                            createTime++;
                            hasSame = true;
                            break;
                        }
                    }
                }
            }else{
                //overwrite note in cloud
                deleteInCloud();
            }
        } catch (AVException e) {
            return 0;
        }

        AVFile file1 = null, file2 = null;
        List<AVFile> fileList = new LinkedList<AVFile>();
        try {
            file1 = AVFile.withAbsoluteLocalPath(String.valueOf(createTime)+"_describe", describeFilePath);
            file2 = AVFile.withAbsoluteLocalPath(String.valueOf(createTime)+"_document", documentPath);
            file1.save();
            file2.save();
            for(int i = 0; i < pictureCnt; ++i){
                AVFile file = null;
                try {
                    file = AVFile.withAbsoluteLocalPath(String.valueOf(createTime)+"_"+String.valueOf(i), picturePaths.get(i));
                } catch (IOException e) {
                    e.printStackTrace();
                }
                file.save();
                fileList.add(file);
            }
        } catch (Exception e) {
            return 0;
        }

        //get title and tag
        try {
            FileInputStream fis = new FileInputStream(describeFilePath);
            if(fis != null && fis.available() != 0) {
                byte[] b = new byte[fis.available()];
                fis.read(b);
                String[] tmpDescribes = new String(b, "utf-8").split("\n");
                title = tmpDescribes[1];
                tag = tmpDescribes[2];
            }

        } catch (Exception e) {
            return 0;
        }

        AVObject avObject = new AVObject("Notes");
        avObject.put("username", username);
        avObject.put("createTime", String.valueOf(createTime));
        avObject.put("pictureCnt", pictureCnt);
        if(openLevel.equals("public"))
            avObject.put("isPublic", true);
        else
            avObject.put("isPublic", false);
        avObject.put("title", title);
        avObject.put("tag", tag);
        avObject.put("describe", file1);
        avObject.put("document", file2);
        for(int i = 0;i < pictureCnt; i++ ){
            avObject.put("picture"+String.valueOf(i),fileList.get(i));
        }

        try {
            avObject.save();
        } catch (AVException e) {
            e.printStackTrace();
            return 0;
        }
        FileInputStream fis = null;
        FileOutputStream fos = null;
        try {
            fis = new FileInputStream(describeFilePath);
            byte[] b = new byte[fis.available()];
            fis.read(b);
            fis.close();
            String describe = new String(b, "utf-8");
            fos = new FileOutputStream(describeFilePath);
            if(openLevel.equals("public"))
                fos.write("P".getBytes("utf-8"));
            else
                fos.write("Y".getBytes("utf-8"));
            fos.write(describe.substring(1).getBytes("utf-8"));
            fos.flush();
            fos.close();

            //test
            fis = new FileInputStream(describeFilePath);
            b = new byte[fis.available()];
            fis.read(b);
            fis.close();
            describe = new String(b, "utf-8");

        } catch (Exception e) {
            return 0;
        }
        return (long) 1;
    }


    public long deleteInCloud(){
        AVQuery<AVObject> query = new AVQuery<AVObject>("Notes");
        query.whereEqualTo("username", username);
        query.whereEqualTo("createTime", String.valueOf(createTime));
        List<AVObject> avObjects = null;
        try {
            avObjects = query.find();
            if (avObjects.size() != 0) {
                AVObject avObject = avObjects.get(0);
                AVFile avFile = avObject.getAVFile("document");
                avFile.delete();
                avFile = avObject.getAVFile("describe");
                avFile.delete();
                int cnt = avObject.getInt("pictureCnt");
                for (int i = 0; i < cnt; ++i) {
                    avFile = avObject.getAVFile("picture" + String.valueOf(i));
                    avFile.delete();
                }
                avObject.delete();
            }else {
                return 0;
            }
        }catch (AVException e){
            return 0;
        }
        return 1;
    }

    public static void downloadFromCloud(String filePath, String username) {
        //get note list
        List<Note> notes = new ArrayList<>();
        try {
            FileInputStream fis = new FileInputStream(filePath+"/noteList");
            if(fis.available() != 0){
                byte[] b = new byte[fis.available()];
                fis.read(b);
                fis.close();
                String[] notesStr = new String(b, "utf-8").split("\n");
                for(int i= 0; i< notesStr.length; i+=2){
                    Note note = new Note();
                    String crTime = notesStr[i];
                    int pos = crTime.indexOf("_");
                    String cTime = crTime.substring(0, pos);
                    String rTime = crTime.substring(pos+1);
                    note.setCreateTime(Long.parseLong(cTime));
                    note.setReviseTime(Long.parseLong(rTime));
                    note.setDescribeFilePath(filePath + "/describe/" + rTime);
                    note.setDocumentPath(filePath + "/document/" + rTime);
                    ArrayList<String> picturePaths = new ArrayList<>();
                    picturePaths.add(filePath + "/image/" + rTime + "_0");
                    note.setPicturePaths(picturePaths);
                    note.setTitle(notesStr[i + 1]);

                    fis = new FileInputStream(note.getDescribeFilePath());
                    byte[] b2 = new byte[1];
                    fis.read(b2, 0, 1);
                    fis.close();
                    String cloudMask = new String(b2, "utf-8");
                    if(cloudMask.equals("P")) {
                        note.cloudNote = true;
                        note.isPublic = true;
                    } else if(cloudMask.equals("Y")) {
                        note.cloudNote = true;
                        note.isPublic = false;
                    }else{
                        note.cloudNote = false;
                        note.isPublic = false;
                    }
                    notes.add(note);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        //download
        AVQuery<AVObject> query = new AVQuery<>("Notes");
        query.whereEqualTo("username", username);
        List<AVObject> avObjects = null;
        try {
            avObjects = query.find();
            for(int i= 0; i< avObjects.size(); ++i){
                AVObject avObject = avObjects.get(i);
                long cTime = Long.parseLong(avObject.getString("createTime"));

                //check cTime
                int j;
                for (j = 0; j < notes.size(); ++j) {
                    if(notes.get(j).getCreateTime() == cTime){
                        break;
                    }
                }
                if(j != notes.size()){
                    //has same in local and cloud
                    //todo now just delete old note
                    if(!notes.get(j).cloudNote){
                        //not cloud note
                        deleteNote(notes.get(j).getReviseTime(), notes.get(j).getCreateTime(), filePath);
                        notes.remove(j);
                    }else{
                        //is cloud
                        //for changing local-cloud note to local which is missing in cloud (delete somewhere else)
                        notes.get(j).cloudNote = false;
                    }
                }else {
                    //save in local
                    int cnt = avObject.getInt("pictureCnt");
                    boolean publicMask = avObject.getBoolean("isPublic");
                    String title = null;

                    //describe
                    AVFile avFile = avObject.getAVFile("describe");
                    byte[] b = avFile.getData();
                    String describe = new String(b, "utf-8");
                    String[] splitDescribe = describe.split("\n");
                    title = splitDescribe[1];
                    if(publicMask)
                        describe = "P" + describe.substring(1);
                    else
                        describe = "Y" + describe.substring(1);

                    FileOutputStream fos = new FileOutputStream(filePath + "/describe/" + String.valueOf(cTime));
                    fos.write(describe.getBytes("utf-8"));
                    fos.flush();
                    fos.close();

                    //document
                    avFile = avObject.getAVFile("document");
                    b = avFile.getData();
                    fos = new FileOutputStream(filePath + "/document/" + String.valueOf(cTime));
                    fos.write(b);
                    fos.flush();
                    fos.close();

                    //pictures
                    for (j = 0; j < cnt; ++j) {
                        avFile = avObject.getAVFile("picture" + String.valueOf(j));
                        b = avFile.getData();
                        fos = new FileOutputStream(filePath + "/image/" + String.valueOf(cTime) + "_" + String.valueOf(j));
                        fos.write(b);
                        fos.flush();
                        fos.close();
                    }

                    //note list
                    fos = new FileOutputStream(filePath + "/noteList", true);
                    fos.write((String.valueOf(cTime) + "_" + String.valueOf(cTime) + "\n" + title + "\n").getBytes("utf-8"));
                    fos.flush();
                    fos.close();


                    FileInputStream fis = new FileInputStream(filePath + "/noteList");
                    byte[] b2 = new byte[fis.available()];
                    fis.read(b2);
                    fis.close();
                }
            }
        }catch (Exception e) {
            e.printStackTrace();
        }

        //chang local-cloud note to local which is missing in cloud (delete somewhere else)
        for(int i= 0; i< notes.size(); ++i){
            if(notes.get(i).isCloudNote()){
                //missing in cloud (local-cloud note should be marked non-cloud in former code
                try {
                    FileInputStream fis = new FileInputStream(notes.get(i).getDescribeFilePath());
                    if(fis != null && fis.available() != 0) {
                        byte[] b = new byte[fis.available()];
                        fis.read(b);
                        fis.close();
                        String tmpDescribe = new String(b, "utf-8");
                        tmpDescribe = "N" + tmpDescribe.substring(1);
                        FileOutputStream fos = new FileOutputStream(notes.get(i).getDescribeFilePath());
                        fos.write(tmpDescribe.getBytes("utf-8"));
                        fos.flush();
                        fos.close();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static int getOtherNote(long cTime, String username, String cachePath){
        FileInputStream fis = null;
        int cnt = 0;
        try {
            fis = new FileInputStream(cachePath + "/pictureCnt");
            if(fis != null && fis.available() != 0) {
                byte[] b = new byte[fis.available()];
                fis.read(b);
                fis.close();
                cnt = Integer.parseInt(new String(b, "utf-8"));
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        File file = new File(cachePath + "/describe");
        if(file.exists())
            file.delete();
        file = new File(cachePath + "/document");
        if(file.exists())
            file.delete();
        file = new File(cachePath + "/pictureCnt");
        if(file.exists())
            file.delete();
        for(int i= 0; i< cnt; ++i){
            file = new File(cachePath + "/image_" + String.valueOf(i));
            if(file.exists())
                file.delete();
        }

        //download note from cloud
        AVQuery<AVObject> query = new AVQuery<>("Notes");
        query.whereEqualTo("username", username);
        query.whereEqualTo("createTime", String.valueOf(cTime));
        query.whereEqualTo("isPublic", true);

        try {
            List<AVObject> avObjects =  query.find();
            if(avObjects == null || avObjects.size() == 0)
                return 0;
            AVObject avObject = avObjects.get(0);
            cnt = 0;
            cnt = avObject.getInt("pictureCnt");

            //describe file
            AVFile avFile = avObject.getAVFile("describe");
            byte[] b = avFile.getData();
            FileOutputStream fos = new FileOutputStream(cachePath + "/describe");
            fos.write(b);
            fos.flush();
            fos.close();

            //document file
            avFile = avObject.getAVFile("document");
            b = avFile.getData();
            fos = new FileOutputStream(cachePath + "/document");
            fos.write(b);
            fos.flush();
            fos.close();

            //picture count
            fos = new FileOutputStream(cachePath + "/pictureCnt");
            fos.write(String.valueOf(cnt).getBytes("utf-8"));
            fos.flush();
            fos.close();

            //pictures
            for (int i= 0; i < cnt; ++i) {
                avFile = avObject.getAVFile("picture" + String.valueOf(i));
                b = avFile.getData();
                fos = new FileOutputStream(cachePath + "/image_" + String.valueOf(i));
                fos.write(b);
                fos.flush();
                fos.close();
            }
        } catch (Exception e) {
            return 0;
        }
        return 1;
    }

    public static int saveOtherToLocal(String filePath, String cachePath, String title){
        long cTime = System.currentTimeMillis();
        int cnt = 0;
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
        }catch (Exception e) {
            return 0;
        }

        //save note

        //get picture count and revise 'Y' in describe to 'N'
        try {
            FileInputStream fis = new FileInputStream(cachePath + "/pictureCnt");
            if(fis.available() != 0){
                byte[] b = new byte[fis.available()];
                fis.read(b);
                fis.close();
                cnt = Integer.parseInt(new String(b, "utf-8"));
            }
            fis = new FileInputStream(cachePath + "/describe");
            if(fis.available() != 0){
                byte[] b = new byte[fis.available()];
                fis.read(b);
                fis.close();
                String describeStr = new String(b, "utf-8");
                describeStr = "N" + describeStr.substring(1);
                FileOutputStream fos = new FileOutputStream(cachePath + "/describe");
                fos.write(describeStr.getBytes());
                fos.flush();
                fos.close();
            }
        }catch (Exception e) {
            return 0;
        }

        //copy file
        if(copyFile(cachePath + "/describe", filePath+"/describe/"+cTime) == 0)
            return 0;
        if(copyFile(cachePath + "/document", filePath+"/document/"+cTime) == 0)
            return 0;
        for(int i= 0; i< cnt; ++i){
            if(copyFile(cachePath + "/image_" + String.valueOf(i), filePath + "/image/" + cTime + "_" + String.valueOf(i)) == 0)
                return 0;
        }

        //revise note list
        try {
            FileOutputStream fos = new FileOutputStream(filePath+"/noteList", true);
            fos.write((String.valueOf(cTime) + "_" + String.valueOf(cTime) + "\n" + title + "\n").getBytes("utf-8"));
            fos.flush();
            fos.close();
        }catch (Exception e) {
            return 0;
        }
        return 1;
    }

    private static int copyFile(String srcFilePath, String destFilePath){
        try {
            FileInputStream fis = new FileInputStream(srcFilePath);
            if(fis != null && fis.available() != 0){
                FileOutputStream fos = new FileOutputStream(destFilePath);
                byte[] buffer = new byte[1024];
                int count = 0;
                while ((count = fis.read(buffer)) > 0) {
                    fos.write(buffer, 0, count);
                }
                fos.close();
                fis.close();
            }
        } catch (Exception e) {
            return 0;
        }
        return 1;
    }

    public static void removeLocalCloudNotes(String filePath) {
        String[] notes;
        try {
            FileInputStream fis = new FileInputStream(filePath + "/noteList");
            if(fis.available() != 0){
                //get note list to array notes
                byte[] b = new byte[fis.available()];
                fis.read(b);
                fis.close();
                notes = (new String(b, "utf-8")).split("\n");

                //remove cloud notes
                for(int i= 0; i< notes.length; i+=2){
                    //check cloud
                    String crTime = notes[i];
                    int pos = crTime.indexOf("_");
                    String cTime = crTime.substring(0, pos);
                    String rTime = crTime.substring(pos+1);
                    fis = new FileInputStream(filePath + "/describe/" + rTime);
                    b = new byte[1];
                    fis.read(b, 0, 1);
                    fis.close();
                    if((new String(b, "utf-8")).equals("N"))
                        continue;
                    else
                        deleteNote(Long.parseLong(rTime), Long.parseLong(cTime), filePath);
                }
            }
        }catch(Exception e){
            return;
        }
    }
}
