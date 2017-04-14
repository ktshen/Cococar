package com.tom.cococar;

import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLEncoder;

/**
 * Created by Katherine on 2017/4/7.
 */

public class DeleteTask extends AsyncTask<String, Void, Void>
{
    protected Void doInBackground(String... params) //背景中做的事
    {
        Log.d("janice", "in back");
        String reg_url = "http://140.115.158.81/cococar/marker";
        String liverand_get = params[0];
        String fixrand_get=params[1];
        try {
            Log.d("janice", "in back 2");
            URL url = new URL(reg_url);
            HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
            //　先取得HttpURLConnection urlConn = new URL("http://www.google.com").openConnection();
            httpURLConnection.setRequestMethod("POST");
            httpURLConnection.setDoOutput(true);//post的情況下需要設置DoOutput為true
            OutputStream os = httpURLConnection.getOutputStream();//java.io.OutputStream是以byte為單位的輸出串流（stream）類別，用來處理出的資料通道
            BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
            Log.d("timmy", "in back 3");

            String data = "{\"marker_id\":\"" + fixrand_get +
                    "\", \"message\": \"" + "delete" +
                    "\"}";

//            String data =URLEncoder.encode("message", "UTF-8") + "=" + URLEncoder.encode("delete", "UTF-8") + "&" + URLEncoder.encode("marker_id", "UTF-8") + "=" + URLEncoder.encode(fixrand_get, "UTF-8");
            Log.d("timmy", "in back 4");
            //&在php中表示下一個表單欄位的開始
            bufferedWriter.write(data);// //使用缓冲区中的方法将数据写入到缓冲区中。
            bufferedWriter.flush();//flush();將緩衝數據寫到文件去
            bufferedWriter.close();
            os.close();
            InputStream IS = httpURLConnection.getInputStream();
            IS.close();

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (ProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return  null;
    }

    @Override
    protected void  onPreExecute() //AsyncTask 執行時會 第一個被呼叫的
    {
        super.onPreExecute();
    }

    @Override
    protected void onProgressUpdate(Void... values)//會以 Void 的型態回報進度
    {
        super.onProgressUpdate(values);
    }
}
