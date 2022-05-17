package com.example.oppoapp;

import android.os.Message;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.FileNameMap;
import java.net.URLConnection;
import java.util.Enumeration;
import java.util.concurrent.TimeUnit;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class NetUtils {
    private String network_name = "MobileNetV2";
    private String network_file_name = network_name+".tflite";
    private String network_version = "v1.0";
    private final static String URL = "http://106.15.39.182:8080";
    private final static String REGISTER_URL = URL + "/device/register";
    private static final String CONNECT_URL = URL + "/device/connect";
    private static final String DOWNLOAD_URL = URL + "/network/download";
    private static final String UPLOAD_URL = URL + "/network/upload";
    private static final String DEVICE_NUMBER = "1233211234567";
    private static final String DEVICE_NAME = "giao";
    private String token;

    private static int BUFFER = 1024;

    public void doRegister() throws JSONException {
        OkHttpClient okHttpClient = new OkHttpClient();
        // 先封装一个 JSON 对象
        JSONObject param = new JSONObject();
        param.put("device_number", DEVICE_NUMBER);
        param.put("device_name", DEVICE_NAME);
        MediaType JSON = MediaType.parse("application/json;charset=utf-8");
        String params =  param.toString();

        RequestBody requestBody = RequestBody.create(JSON, params);
        //创建一个请求对象
        Request request = new Request.Builder()
                .url(REGISTER_URL)
                .post(requestBody)
                .build();
        //发送请求获取响应
        try {
            Response response=okHttpClient.newCall(request).execute();
            //判断请求是否成功
            if(response.isSuccessful()){
                //打印服务端返回结果
                String result = response.body().string();
                JSONObject jsonObject = new JSONObject(result);
                int code = jsonObject.getInt("code");
                if (code == 1) {
                    JSONObject data = jsonObject.getJSONObject("data");
                    token = data.getString("token");
                    System.out.println("服务器连接成功"); //消息发送的内容如：  Object String 类 int
                } else if (code == 201) { //已注册，则通过连接去返回token
                    doConnect();
                } else {
                    System.out.println("设备注册失败，请检查网络设置。"); //消息发送的内容如：  Object String 类 int
                }
            } else {
                System.out.println( "设备注册失败，请检查网络设置。"); //消息发送的内容如：  Object String 类 int
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void doConnect() throws IOException, JSONException {
        OkHttpClient okHttpClient = new OkHttpClient();
        // 先封装一个 JSON 对象
        JSONObject param = new JSONObject();
        param.put("device_number", DEVICE_NUMBER);
        MediaType JSON = MediaType.parse("application/json;charset=utf-8");
        String params =  param.toString();
        RequestBody requestBody = RequestBody.create(JSON, params);
        //创建一个请求对象
        Request request = new Request.Builder()
                .url(CONNECT_URL)
                .post(requestBody)
                .build();
        //发送请求获取响应
        try {
            Response response=okHttpClient.newCall(request).execute();
            //判断请求是否成功
            if(response.isSuccessful()){
                //打印服务端返回结果
                String result = response.body().string();
                System.out.println(result);
                JSONObject jsonObject = new JSONObject(result);
                int code = jsonObject.getInt("code");
                if (code == 1) {
                    JSONObject data = jsonObject.getJSONObject("data");
                    token = data.getString("token");
                    System.out.println("服务器连接成功"); //消息发送的内容如：  Object String 类 int
                } else {
                    System.out.println("设备连接失败，请检查网络设置。"); //消息发送的内容如：  Object String 类 int
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public ResponseBody getResponeBody(String url) {
        if (token == null) {
            System.out.println("token为空，需要先连接服务器。"); //消息发送的内容如：  Object String 类 int
            return null;
        }
        url = url + "?" +
                "network_name=" + network_name +
                "&network_version=" + network_version +
                "&token=" + token;
        System.out.println(url);
        ResponseBody result =null;
        OkHttpClient okHttpClient = new OkHttpClient()
                .newBuilder()
                .connectTimeout(60000, TimeUnit.MILLISECONDS)
                .readTimeout(60000, TimeUnit.MILLISECONDS)
                .build();

        Request request = new Request.Builder()
                .url(url)
                .build();
        Call call = okHttpClient.newCall(request);
        try {
            Response response = call.execute();
            result = response.body();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    //将InputStream写入到文件，成功返回true 失败返回false
    public boolean WriteFile4InputStream(String FilePath, InputStream inputStream)
    {
        //默认为flase 即失败
        boolean result = false;
        try {
            OutputStream os = new FileOutputStream(FilePath);
            byte[] arr = new byte[1024];
            int len = 0;
            while ( ( len=inputStream.read(arr) ) != -1 ){
                os.write(arr, 0, len);
            }
            os.close();
            inputStream.close();
            result = true;
        }catch (IOException e)
        {
            e.printStackTrace();
            result = false;
        }
        return result;
    }

    public void download(String cachePath) throws IOException {
        ResponseBody response = getResponeBody(DOWNLOAD_URL);
        if (response == null ) {
            return;
        }
        InputStream inputStream = response.byteStream();
        String dirPath = cachePath + "/model/";

        if (!new File(dirPath).exists()) {
            new File(dirPath).mkdir();
        }
        String filePath = dirPath + network_file_name;

        boolean result = WriteFile4InputStream(filePath, inputStream);

        if (result) {
            System.out.println("模型下载成功");
        }else {
            System.out.println("模型下载失败");
        }
    }

    private void upload(String path) {
//        String path  = getCacheDir().getAbsolutePath() + File.separator + network_file_name;
        String url = UPLOAD_URL + "?network_name=" + network_name;
        System.out.println(url);
        upLoadingFile(path,url);
    }

    /**
     * 上传文件(支持单个, 多个文件上传)
     */
    public void upLoadingFile(String filePath,String url) {

        Message msg = new Message();
        msg.what = 1;
        // 1.RequestBody
        //创建MultipartBody.Builder，用于添加请求的数据
        MultipartBody.Builder multipartBodyBuilder = new MultipartBody.Builder()
                .setType(MultipartBody.FORM);
        File file = new File(filePath); //生成文件
        if (!file.exists()){
            System.out.println("网络文件不存在");
            return;
        }

        // 根据文件的后缀名，获得文件类型
        String fileType = getMimeType(file.getName());
        //给Builder添加上传的文件
        multipartBodyBuilder.addFormDataPart(
                "network_file",  //请求的名字
                file.getName(), //文件的文字，服务器端用来解析的
                RequestBody.create(MediaType.parse(fileType), file) //创建RequestBody，把上传的文件放入
        );

        // 添加其他参数信息, 如果只是单纯的上传文件, 下面的添加其他参数的方法不用调用
        RequestBody requestBody = multipartBodyBuilder.build();//根据Builder创建请求

        // 2.requestBuilder
        Request requestBuilderRequest = new Request.Builder()
                .url(url)
                .header("Authorization", token)
                .post(requestBody)
                .build();

        // 3.OkHttpClient
        OkHttpClient mOkHttpClient = new OkHttpClient.Builder()
                .connectTimeout(60000, TimeUnit.SECONDS)
                .readTimeout(60000, TimeUnit.SECONDS)
                .writeTimeout(60000, TimeUnit.SECONDS)
                .build();

        Call call = mOkHttpClient.newCall(requestBuilderRequest);


        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                //请求失败监听: 异步请求(非主线程)
                System.out.println("文件上传出错,请重试");
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String res = response.body().string();
                System.out.println(res);
                try {
                    JSONObject resJson = new JSONObject(res);
                    int code = resJson.getInt("code");
                    if (code == 1) {
                        System.out.println("文件上传成功");
                    } else {
                        System.out.println("文件上传失败");
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private static String getMimeType(String fileName) {
        FileNameMap filenameMap = URLConnection.getFileNameMap();
        String contentType = filenameMap.getContentTypeFor(fileName);
        if (contentType == null) {
            contentType = "application/octet-stream"; //* exe,所有的可执行程序
        }
        return contentType;
    }

    public boolean getData(String filePath,String dataUrl) {

        ResponseBody response =null;
        OkHttpClient okHttpClient = new OkHttpClient()
                .newBuilder()
                .connectTimeout(60000, TimeUnit.MILLISECONDS)
                .readTimeout(60000, TimeUnit.MILLISECONDS)
                .build();

        Request request = new Request.Builder()
                .url(dataUrl)
                .build();
        Call call = okHttpClient.newCall(request);
        try {
            response = call.execute().body();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (response == null ) {
            return false;
        }
        InputStream inputStream = response.byteStream();
        boolean result = WriteFile4InputStream(filePath + "/rps.zip", inputStream);
        if (result) {
            System.out.println("数据下载成功");
            unzip(filePath+"/rps.zip",filePath + "/");
            System.out.println("解压数据完成");
        }else {
            System.out.println("数据下载失败");
        }
        return true;
    }

    public static String unzip(String filePath,String zipDir) {
        String name = "";
        try {
            BufferedOutputStream dest = null;
            BufferedInputStream is = null;
            ZipEntry entry;
            ZipFile zipfile = new ZipFile(filePath);

            Enumeration dir = zipfile.entries();
            while (dir.hasMoreElements()){
                entry = (ZipEntry) dir.nextElement();

                if( entry.isDirectory()){
                    name = entry.getName();
                    name = name.substring(0, name.length() - 1);
                    File fileObject = new File(zipDir + name);
                    fileObject.mkdir();
                }
            }

            Enumeration e = zipfile.entries();
            while (e.hasMoreElements()) {
                entry = (ZipEntry) e.nextElement();
                if( entry.isDirectory()){
                    continue;
                }else{
                    is = new BufferedInputStream(zipfile.getInputStream(entry));
                    int count;
                    byte[] dataByte = new byte[BUFFER];
                    FileOutputStream fos = new FileOutputStream(zipDir+entry.getName());
                    dest = new BufferedOutputStream(fos, BUFFER);
                    while ((count = is.read(dataByte, 0, BUFFER)) != -1) {
                        dest.write(dataByte, 0, count);
                    }
                    dest.flush();
                    dest.close();
                    is.close();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return  name;
    }

}
