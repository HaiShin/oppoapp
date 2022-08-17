package com.example.oppoapp;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.RelativeLayout;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class fed_activity extends AppCompatActivity implements View.OnClickListener {
    private TextView tv_trans2;
    private TextView tv_fed2;
    private TextView tv_epoch;
    private TextView tv_loss;
    private TextView tv_acc;
    private Button request;
    private Button bn_train2;
    private Button bn_test2;
    private Button add_data2;
    private Button down_mod2;
    //private Button up_mod2;
    private Spinner class_sel_spinner2;
    private Spinner model_sel_spinner;
    private RelativeLayout camera_ll;
    private Boolean issel;
    private NetUtils netUtils;
    private Utils utils = new Utils();
    private String network_name = "MobileNetV2";
    private String network_file_name = network_name + ".tflite";
    private GlobalApp globalApp;
    private String DEVICE_NUMBER;
    private List<String> dataList = new ArrayList<>();
    private ArrayAdapter<String> adapter;
    private ArrayAdapter<String> modeladapter;
    private List<String> networkList = new ArrayList<>();

    private String className;
    private String modelname;

    private static final int REQUEST_CODE_SELECT_IMG = 1;
    private static final int MAX_SELECT_COUNT = 30;

    private static final int NUM_THREADS =
            Math.max(1, Runtime.getRuntime().availableProcessors() - 1);

    private final ExecutorService executor = Executors.newFixedThreadPool(NUM_THREADS);
    private final Lock trainingInferenceLock = new ReentrantLock();
    private boolean isTrain = true;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fed);
        globalApp = ((GlobalApp) getApplicationContext());

        if (DEVICE_NUMBER == null) {
            DEVICE_NUMBER = globalApp.getDeviceNumber();
        }
        netUtils = new NetUtils(DEVICE_NUMBER);

//        String modelFilePath = getCacheDir().getAbsolutePath() + "/model/download/" + network_file_name;
//        System.out.println(modelFilePath);
//        loadModel(modelFilePath);

        tv_trans2 = (TextView) findViewById(R.id.tv_trans2);
        tv_fed2 = (TextView) findViewById(R.id.tv_fed2);


        tv_epoch = findViewById(R.id.epoch_2);
        tv_loss = findViewById(R.id.loss_2);
        tv_acc = findViewById(R.id.Acc_2);
        tv_trans2.setSelected(false);
        tv_fed2.setSelected(true);
        tv_trans2.setOnClickListener(this);
        request = findViewById(R.id.request);
        bn_train2 = findViewById(R.id.bn_train_2);
        bn_test2 = findViewById(R.id.bn_test_2);
        add_data2 = findViewById(R.id.add_data_2);
        down_mod2 = findViewById(R.id.model_down_2);
        //up_mod2 = findViewById(R.id.model_up_2);

        camera_ll = findViewById(R.id.camera_ll_2);
        camera_ll.setVisibility(View.INVISIBLE);
        model_sel_spinner = findViewById(R.id.model_name_2);
        class_sel_spinner2 = findViewById(R.id.select_class_2);
        dataList.add("A");
        dataList.add("B");
        dataList.add("C");
        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, dataList);
        class_sel_spinner2.setAdapter(adapter);
        class_sel_spinner2.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener(){
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                className = (String) class_sel_spinner2.getSelectedItem();
            }
            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });
        modeladapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, networkList);
        model_sel_spinner.setAdapter(modeladapter);
        model_sel_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                modelname = (String) model_sel_spinner.getSelectedItem();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        request.setOnClickListener(this);
        bn_train2.setOnClickListener(this);
        bn_test2.setOnClickListener(this);
        add_data2.setOnClickListener(this);
        down_mod2.setOnClickListener(this);
        //up_mod2.setOnClickListener(this);

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.tv_trans2:
                Intent intent = new Intent();
                intent.setClass(this, MainActivity.class);
                startActivity(intent);
                break;
            case R.id.request:
                Toast.makeText(this, "正在连接中", Toast.LENGTH_SHORT).show();
                doRegister();
                break;
            case R.id.bn_train_2:
                //点击训练按钮,在这添加后续操作
                String content = bn_train2.getText().toString();
                if (content.equals("训练")) {

                    bn_train2.setText("取消训练");
                    fedTrain();
                } else {
                    isTrain = false;
                    bn_train2.setText("训练");
                }
                break;
            case R.id.bn_test_2:
                //点击推理按钮
                String mes = "fed";
                Intent inferIntent = new Intent(fed_activity.this, CameraActivity.class);
                inferIntent.putExtra("from", mes);
                startActivity(inferIntent);
                break;
            case R.id.add_data_2:
//                添加数据
                selectImg(view);
                break;
            case R.id.model_down_2:
                //模型下载
                Toast.makeText(this, "开始下载模型", Toast.LENGTH_SHORT).show();
                doDownload();
                break;

            default:
                break;
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

    //联邦学习推理
    public void fedTrain() {
        String ckptDirPath = getCacheDir() + "/ckpt";
        File file = new File(ckptDirPath);
        if (!file.exists()) {
            file.mkdir();
        }
        new Thread(() -> {
            DecimalFormat b = new DecimalFormat("0.00");
            for (int i = 0; i < 20; i++) {
                if (isTrain) {
                    CountDownLatch countDownLatch = new CountDownLatch(1);
                    System.out.println(i);
                    String ckptFilePath = ckptDirPath + "/checkpoint_" + i + ".ckpt";

                    int finalI = i;
                    globalApp.getTlModel().fedTraining((epoch, loss) -> {

                        System.out.println("epoch: " + finalI + " ----- loss:" + b.format(loss));

                        tv_epoch.setText(finalI + "");
                        tv_loss.setText(b.format(loss));
                    }, (acc) -> {
                        System.out.println("acc------" + acc);
                        tv_acc.setText(b.format(acc));
                    });
                    // 保存参数
                    globalApp.getTlModel().saveModel(ckptFilePath);
//                // 子线程上传和下载参数文件
                    netUtils.doUpAndDownLoadParam(ckptFilePath, countDownLatch);
                    try {
                        countDownLatch.await();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    if (!new File(ckptFilePath).exists()) {
                        System.out.println("ckpt文件不存在");
                        return;
                    }
                    globalApp.getTlModel().restoreModel(ckptFilePath);
                } else {
                    isTrain = true;
                    break;
                }
            }
            Message msg = new Message();
            msg.what = 2;
            msg.obj = "联邦学习训练完成！";
            handler.sendMessage(msg);

        }).start();

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
                    List<String> paths = new ArrayList<>();
                    try {
                        paths = ImageSelector.getImagePaths(data);
                        Bitmap bitmap = null;
                        for (String path : paths) {
                            Uri uri = Uri.parse(path);
                            String img_path = getPathFromURI(fed_activity.this, uri);
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
                    if (paths.size() != 0) {
                        msg.what = 2;
                        msg.obj = "加载此批 "+label+" 数据完成！";
                        handler.sendMessage(msg);
                    }
                    return null;
                });

    }

    public void selectImg(View v) {
        ImageSelector.show(this, REQUEST_CODE_SELECT_IMG, MAX_SELECT_COUNT);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE_SELECT_IMG) {
            try {
                loadImg(data);
            } catch (IOException e) {
                e.printStackTrace();
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