package com.example.oppoapp;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.scrat.app.selectorlibrary.ImageSelector;

import org.json.JSONException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    private GlobalApp globalApp;
    private NetUtils netUtils;
    private String network_name = "MobileNetV2";
    private String network_file_name = network_name+".tflite";
    private RelativeLayout camera_ll;
    private String DEVICE_NUMBER;
    private Utils utils = new Utils();


    private TextView tv_trans;
    private TextView tv_fed;
    private TextView tv_epoch;
    private TextView tv_loss;
    private TextView tv_acc;


    private Button bn_train;
    private Button bn_test;
    private Button add_data;
    private Button bn_tran;
    private Button bn_con;
    private Button down_mod;
    private Button up_mod;
    private Spinner class_sel_spinner;

    private ArrayAdapter<String> adapter;
    private List<String> dataList = new ArrayList<>();



    private String className;

    private static final int REQUEST_CODE_SELECT_IMG = 1;
    private static final int MAX_SELECT_COUNT = 30;

    private static final int NUM_THREADS =
            Math.max(1, Runtime.getRuntime().availableProcessors() - 1);

    private final ExecutorService executor = Executors.newFixedThreadPool(NUM_THREADS);


    private final Lock trainingInferenceLock = new ReentrantLock();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //获取相册
        setContentView(R.layout.activity_main);
        if (!hasPermission()) {
            requestPermission();
        }

        setContentView(R.layout.activity_main);
        globalApp = ((GlobalApp) getApplicationContext());
        if ( DEVICE_NUMBER == null ) {
            DEVICE_NUMBER = utils.randomString(10);
            globalApp.setDeviceNumber(DEVICE_NUMBER);
        }
        System.out.println("main:"+DEVICE_NUMBER);
        netUtils = new NetUtils(DEVICE_NUMBER);


//        String modelFilePath = getCacheDir().getAbsolutePath() + "/model/download/" + network_name+"/"+network_file_name;
//        System.out.println(modelFilePath);
//        loadModel(modelFilePath);


        ActivityManager am = (ActivityManager) MainActivity.this.getSystemService(Context.ACTIVITY_SERVICE);
        int heapGrowthLimit = am.getMemoryClass();

        System.out.println("内存："+heapGrowthLimit);

        camera_ll = findViewById(R.id.camera_ll);
        camera_ll.setVisibility(View.INVISIBLE);


        tv_trans = (TextView) findViewById(R.id.tv_trans);
        tv_fed = (TextView) findViewById(R.id.tv_fed);
        tv_epoch = findViewById(R.id.epoch);
        tv_loss = findViewById(R.id.loss);
        tv_acc = findViewById(R.id.Acc);



        bn_train = findViewById(R.id.bn_train);
        bn_test = findViewById(R.id.bn_test);
        bn_con = findViewById(R.id.bn_con);
        bn_tran = findViewById(R.id.bn_trans);
        add_data = findViewById(R.id.add_data);
        down_mod = findViewById(R.id.model_down);
        up_mod = findViewById(R.id.model_up);
        tv_trans.setSelected(true);
        tv_fed.setSelected(false);
        class_sel_spinner = (Spinner) findViewById(R.id.select_class);

        dataList.add("A");
        dataList.add("B");
        dataList.add("C");
        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, dataList);
        class_sel_spinner.setAdapter(adapter);
        class_sel_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener(){
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                className = (String) class_sel_spinner.getSelectedItem();

            }
            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });
        tv_fed.setOnClickListener(this);
        bn_train.setOnClickListener(this);
        bn_test.setOnClickListener(this);
        bn_con.setOnClickListener(this);
        bn_train.setOnClickListener(this);
        add_data.setOnClickListener(this);
        down_mod.setOnClickListener(this);
        up_mod.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.tv_fed:
                //点击联邦学习，跳转到另一个activity
                Intent intent = new Intent();
                intent.setClass(MainActivity.this, fed_activity.class);
                startActivity(intent);
                break;
            case R.id.bn_train:
                //点击训练按钮,在这添加后续操作
                train();
                break;
            case R.id.bn_test:
                //点击推理按钮
                String mes = "main";
                Intent inferIntent = new Intent(MainActivity.this, CameraActivity.class);
                inferIntent.putExtra("from", mes);
                startActivity(inferIntent);
                break;
            case R.id.add_data:
                selectImg(view);
                break;
            case R.id.bn_trans:
                //迁移学习
                break;
            case R.id.bn_con:
                //持续学习
                dataList.add("D类(10)");
                adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, dataList);
                break;
            case R.id.model_down:
                //模型下载
                Toast.makeText(this, "开始下载模型", Toast.LENGTH_SHORT).show();
                doRegister();
                doDownload();
                break;
            case R.id.model_up:
                //模型上传
                break;
            default:
                break;

        }
    }


    // check had permission
    private boolean hasPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED &&
                    checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED &&
                    checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
        } else {
            return true;
        }
    }

    // request permission
    private void requestPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(new String[]{Manifest.permission.CAMERA,
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        }
    }



    public void doRegister() {
        AtomicBoolean flag = new AtomicBoolean(false);
        Thread thread = new Thread(() -> {
            try {
                flag.set(netUtils.doRegister());
            } catch (JSONException e) {
                e.printStackTrace();
            }
        });
        thread.start();
        try {
            thread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        if (flag.get()) {
            Toast.makeText(this, "设备连接成功", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "设备连接失败", Toast.LENGTH_SHORT).show();
        }
    }

    public void doDownload() {
        String cacheDir = getCacheDir().getAbsolutePath();
        AtomicBoolean flag = new AtomicBoolean(false);
        Thread thread = new Thread(() -> {
            flag.set(netUtils.download(cacheDir));
        });
        thread.start();
        try {
            thread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        if (flag.get()) {
            Toast.makeText(this, "模型下载完成", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "模型下载失败", Toast.LENGTH_SHORT).show();
        }

        String modelFilePath = getCacheDir().getAbsolutePath() + "/model/download/" + network_name+"/"+network_file_name;
        loadModel(modelFilePath);
        Toast.makeText(this, "模型加载完成", Toast.LENGTH_SHORT).show();
    }

    private void loadModel(String modelFilePath) {
        if (!globalApp.isNull()){
            Toast.makeText(this, "模型加载完成", Toast.LENGTH_SHORT).show();
            return;
        }
        if (new File(modelFilePath).exists()) {
            String parentDir = getCacheDir().getAbsolutePath();
            String directoryName = "model/download/" + network_name;
            List<String> list = Arrays.asList("A","B","C");
            try {
                globalApp.setTlModel(new TransferLearningModelWrapper(parentDir,directoryName, list));
                Toast.makeText(this, "模型加载完成", Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                throw new RuntimeException("加载模型报错！", e);
            }
        } else {
            Toast.makeText(this, "模型文件不存在，请点击模型下载按钮", Toast.LENGTH_SHORT).show();
        }
    }

    private void train(){
        DecimalFormat b = new DecimalFormat("0.00");
        globalApp.getTlModel().setEpochs(20);
        globalApp.getTlModel().enableTraining((epoch, loss) -> {
                    System.out.println("epoch: "+epoch + " ----- loss:" + loss);

                    tv_epoch.setText(b.format(epoch));
                    tv_loss.setText(b.format(loss));
                    }, (acc) -> {
            System.out.println( "test------" + acc);
            tv_acc.setText(b.format(acc));
        });
    }

    private Future<Void> loadImg(Intent data) throws IOException, InterruptedException {

        return executor.submit(
                () -> {
                    Message msg = new Message();
                    if (Thread.interrupted()) {
                        msg.what = 2;
                        msg.obj = "加载图片出错。";
                        handler.sendMessage(msg);
                        return null;
                    }
                    String label = className;
                    trainingInferenceLock.lockInterruptibly();
                    try {
                        List<String> paths = ImageSelector.getImagePaths(data);
                        Bitmap bitmap = null;
                        for (String path : paths) {
                            Uri uri = Uri.parse(path);
                            String img_path = getPathFromURI(MainActivity.this, uri);
                            FileInputStream fis = new FileInputStream(img_path);
                            bitmap = BitmapFactory.decodeStream(fis);
                            float[][][] rgbImage = utils.prepareCameraImage(bitmap, 0);
                            System.out.println(rgbImage.length);
                            System.out.println(rgbImage[0].length);
                            System.out.println(rgbImage[0][0].length);
                            globalApp.getTlModel().addBatchSample(label, rgbImage);
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {
                        trainingInferenceLock.unlock();
                    }
                    msg.what = 2;
                    msg.obj = "加载此批 "+label+" 数据完成！";
                    handler.sendMessage(msg);
                    return null;
                });

    }

    public void selectImg(View v) {
        ImageSelector.show(this, REQUEST_CODE_SELECT_IMG, MAX_SELECT_COUNT);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE_SELECT_IMG) {
            Thread thread = new Thread(() -> {
                try {
                    loadImg(data);
                } catch (IOException | InterruptedException e) {
                    e.printStackTrace();
                }
            });
            thread.start();
            try {
                thread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    // 根据相册的Uri获取图片的路径
    public static String getPathFromURI(Context context, Uri uri) {
        String result;
        Cursor cursor = context.getContentResolver().query(uri, null, null, null, null);
        if (cursor == null) {
            result = uri.getPath();
        } else {
            cursor.moveToFirst();
            int idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
            result = cursor.getString(idx);
            cursor.close();
        }
        return result;
    }

    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == 1) {
                String res = (String) msg.obj;
                Toast.makeText(getApplicationContext(), res, Toast.LENGTH_SHORT).show();
            } else if (msg.what == 2) {
                String res = (String) msg.obj;
                Toast.makeText(getApplicationContext(), res, Toast.LENGTH_SHORT).show();
            }
        }
    };

}