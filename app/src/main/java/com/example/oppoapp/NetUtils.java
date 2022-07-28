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
import java.util.concurrent.CountDownLatch;
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
    private String URL = "http://47.100.0.251:30000";
    private String REGISTER_URL = URL + "/device";
    private String CONNECT_URL = URL + "/connection";
    private String DOWNLOAD_URL = URL + "/network";
    private String UPLOAD_URL = URL + "/network";
    private String DEVICE_NUMBER;
    private String DEVICE_NAME = "giao";
    private String APPLICATION = "xxxx";
    private String AGGREGATE_URL = URL + "/network";
    private String token;
    private String APPLICATION_ID = "65aec123-0752-4a3d-9e99-2877ee9e11bb";
    private String model_id = "f788f533-1c8e-2d2e-8863-49c7e1d74ebf";

    private static int BUFFER = 1024;

    public NetUtils(String DEVICE_NUMBER){
        this.DEVICE_NUMBER = DEVICE_NUMBER;
    }

    public boolean doRegister() throws JSONException {
        if (token != null) {
            return true;
        }
        OkHttpClient okHttpClient = new OkHttpClient()
                .newBuilder()
                .followRedirects(true)
                .connectTimeout(60000, TimeUnit.SECONDS)
                .readTimeout(60000, TimeUnit.SECONDS)
                .build();
        // 先封装一个 JSON 对象
        JSONObject param = new JSONObject();
        param.put("device_identity", DEVICE_NUMBER);
        param.put("device_category", DEVICE_NAME);
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
            Response response= okHttpClient.newCall(request)
                    .execute();
            //判断请求是否成功
            if(response.isSuccessful()){
                //打印服务端返回结果
                String result = response.body().string();
                JSONObject jsonObject = new JSONObject(result);
                int code = jsonObject.getInt("code");
                if (code == 1 || code == -1) {
                    System.out.println("设备注册成功"); //消息发送的内容如：  Object String 类 int
                    boolean flag = doConnect();
                    return flag;
                } else {
                    System.out.println("设备注册失败，请检查服务器设置。");
                    return false;
                }
            } else {
                System.out.println( "设备注册失败，请检查网络设置。");
                return false;
            }
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean doRegisterAndDownload(String cachePath) throws JSONException{

        JSONObject param = new JSONObject();
        param.put("device_number", DEVICE_NUMBER);
        param.put("device_name", DEVICE_NAME);
        param.put("application", APPLICATION);
        MediaType JSON = MediaType.parse("application/json;charset=utf-8");
        String params =  param.toString();

        RequestBody requestBody = RequestBody.create(JSON, params);

        OkHttpClient okHttpClient = new OkHttpClient()
                .newBuilder()
                .connectTimeout(60000, TimeUnit.SECONDS)
                .readTimeout(60000, TimeUnit.SECONDS)
                .build();

        Request request = new Request.Builder()
                .post(requestBody)
                .url(REGISTER_URL)
                .build();
        Call call = okHttpClient.newCall(request);

        try {
            ResponseBody responseBody = call.execute().body();
            //判断请求是否成功
            if(responseBody != null){
                InputStream inputStream = responseBody.byteStream();
                String dirPath = cachePath + "/model/download/";

                if (!new File(dirPath).exists()) {
                    new File(dirPath).mkdirs();
                }
                String filePath = dirPath + network_file_name;
                boolean result = WriteFile4InputStream(filePath, inputStream);
                if (result) {
                    System.out.println("模型下载成功");
                }else {
                    System.out.println("模型下载失败");
                    return false;
                }
            } else {
                System.out.println( "设备注册失败，请检查网络设置。"); //消息发送的内容如：  Object String 类 int
                return false;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return true;
    }

    public void doUpAndDownLoadParam(String filePath, CountDownLatch countDownLatch ) {
        String url = AGGREGATE_URL + "/" + model_id + "/weight";
        upLoadingFile(filePath, url);
        countDownLatch.countDown();
    }

    public boolean doConnect()  {
        OkHttpClient okHttpClient = new OkHttpClient()
                .newBuilder()
                .followRedirects(true)
                .connectTimeout(60000, TimeUnit.SECONDS)
                .readTimeout(60000, TimeUnit.SECONDS)
                .build();
        String connectUrl= CONNECT_URL + "/" + DEVICE_NUMBER;
        //创建一个请求对象
        Request request = new Request.Builder()
                .url(connectUrl)
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
                    return true;
                } else {
                    System.out.println("设备连接失败，请检查网络设置。"); //消息发送的内容如：  Object String 类 int
                    return false;
                }
            } else {
                return false;
            }
        } catch (IOException | JSONException e) {
            e.printStackTrace();
            return false;
        }
    }

    public ResponseBody getResponeBody(String url) {
        if (token == null) {
            System.out.println("token为空，需要先连接服务器。"); //消息发送的内容如：  Object String 类 int
            return null;
        }

        System.out.println(url);
        ResponseBody result =null;
        OkHttpClient okHttpClient = new OkHttpClient()
                .newBuilder()
                .followRedirects(true)
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
    public boolean WriteFile4InputStream(String FilePath, InputStream inputStream) throws IOException {
        System.out.println(FilePath);
        if (!new File(FilePath).exists()) {
            new File(FilePath).createNewFile();
        }
        //默认为flase 即失败
        boolean result = false;
        try {
            OutputStream os = new FileOutputStream(FilePath);
            byte[] arr = new byte[1024];
            int len = 0;
            while ( ( len=inputStream.read(arr) ) != -1 ) {
                os.write(arr, 0, len);
            }
            os.close();
            inputStream.close();
            result = true;
        }catch (IOException e) {
            e.printStackTrace();
            result = false;
        }
        return result;
    }

    public boolean download(String cachePath) {
        if (token == null) {
            System.out.println("token为空，需要先连接服务器。"); //消息发送的内容如：  Object String 类 int
            return false;
        }

        String url = DOWNLOAD_URL + "/" + model_id;

        OkHttpClient okHttpClient = new OkHttpClient()
                .newBuilder()
                .followRedirects(true)
                .connectTimeout(60000, TimeUnit.SECONDS)
                .readTimeout(60000, TimeUnit.SECONDS)
                .build();

        Request request = new Request.Builder()
                .url(url)
                .header("Authorization", token)
                .build();

        Call call = okHttpClient.newCall(request);
        try {
            Response response = call.execute();
            ResponseBody responseBody = response.body();
            if (responseBody == null ) {
                return false;
            }

            InputStream inputStream = responseBody.byteStream();
            String dirPath = cachePath + "/model/download/" + network_name;
            if (!new File(dirPath).exists()) {
                new File(dirPath).mkdirs();
            }


            if (!new File(dirPath).exists()) {
                new File(dirPath).mkdir();
            }
            String filePath = dirPath + "/" +network_file_name;

            boolean result = WriteFile4InputStream(filePath, inputStream);
            return result;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }


    /**
     * 上传ckpt文件
     */
    public void upLoadingFile(String filePath,String url)  {

        // 1.RequestBody
        //创建MultipartBody.Builder，用于添加请求的数据
        MultipartBody.Builder multipartBodyBuilder = new MultipartBody.Builder()
                .setType(MultipartBody.FORM);
        File file = new File(filePath); //ckpt文件
        if (!file.exists()){
            System.out.println("ckpt文件不存在");
            return;
        }

        // 根据文件的后缀名，获得文件类型
        String fileType = getMimeType(file.getName());
        //给Builder添加上传的文件
        multipartBodyBuilder.addFormDataPart(
                "network_weight",  //请求的名字
                file.getName(), //文件的文字，服务器端用来解析的
                RequestBody.create(MediaType.parse(fileType), file) //创建RequestBody，把上传的文件放入
        );


        // 添加其他参数信息, 如果只是单纯的上传文件, 下面的添加其他参数的方法不用调用
        RequestBody requestBody = multipartBodyBuilder.build();//根据Builder创建请求

        // 2.requestBuilder
        Request requestBuilderRequest = new Request.Builder()
                .url(url)
                .header("Authorization", token)
                .put(requestBody)
                .build();

        // 3.OkHttpClient
        OkHttpClient mOkHttpClient = new OkHttpClient.Builder()
                .connectTimeout(60000, TimeUnit.SECONDS)
                .readTimeout(60000, TimeUnit.SECONDS)
                .writeTimeout(60000, TimeUnit.SECONDS)
                .build();

        Call call = mOkHttpClient.newCall(requestBuilderRequest);
        try {
            ResponseBody responseBody = call.execute().body();
            if (responseBody == null ) {
                return;
            }
            InputStream inputStream = responseBody.byteStream();
            boolean result = WriteFile4InputStream(filePath, inputStream);
            if (result) {
                System.out.println("参数文件下载成功");
            }else {
                System.out.println("参数文件下载失败");
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
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
                .followRedirects(true)
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
        boolean result = false;
        try {
            result = WriteFile4InputStream(filePath + "/rps.zip", inputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }
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

