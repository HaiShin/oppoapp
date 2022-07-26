package com.example.oppoapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicBoolean;

public class fed_activity extends AppCompatActivity implements View.OnClickListener {
    private TextView tv_trans2;
    private TextView tv_fed2;
    private TextView tv_epoch;
    private TextView tv_loss;
    private EditText mip;
    private EditText mport;
    private Button request;
    private Button bn_train2;
    private Button bn_test2;
    private Button add_data2;
    private Button bn_tran2;
    private Button bn_con2;
    private Button down_mod2;
    private Button up_mod2;
    private Spinner class_sel_spinner2;
    private RelativeLayout camera_ll;

    private NetUtils netUtils;
    private Utils utils = new Utils();
    private String network_name = "MobileNetV2";
    private String network_file_name = network_name + ".tflite";
    private GlobalApp globalApp;
    private String DEVICE_NUMBER;
    private List<String> dataList = new ArrayList<>();
    private ArrayAdapter<String> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fed);
        globalApp = ((GlobalApp) getApplicationContext());

        if (DEVICE_NUMBER == null) {
            DEVICE_NUMBER = globalApp.getDeviceNumber();
        }
        netUtils = new NetUtils(DEVICE_NUMBER);

        String modelFilePath = getCacheDir().getAbsolutePath() + "/model/download/" + network_file_name;
        System.out.println(modelFilePath);
        loadModel(modelFilePath);

        tv_trans2 = (TextView) findViewById(R.id.tv_trans2);
        tv_fed2 = (TextView) findViewById(R.id.tv_fed2);


        tv_epoch = findViewById(R.id.epoch_2);
        tv_loss = findViewById(R.id.loss_2);
        tv_trans2.setSelected(false);
        tv_fed2.setSelected(true);
        tv_trans2.setOnClickListener(this);
        request = findViewById(R.id.request);
        mip = findViewById(R.id.ip);
        mport = findViewById(R.id.port);
        bn_train2 = findViewById(R.id.bn_train_2);
        bn_test2 = findViewById(R.id.bn_test_2);
        add_data2 = findViewById(R.id.add_data_2);
        down_mod2 = findViewById(R.id.model_down_2);
        up_mod2 = findViewById(R.id.model_up_2);

        camera_ll = findViewById(R.id.camera_ll_2);
        camera_ll.setVisibility(View.INVISIBLE);

        class_sel_spinner2 = findViewById(R.id.select_class_2);
        dataList.add("A类(10)");
        dataList.add("B类(10)");
        dataList.add("C类(10)");
        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, dataList);
        class_sel_spinner2.setAdapter(adapter);
        class_sel_spinner2.setSelection(0, true);
        class_sel_spinner2.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener(){
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                String content = adapterView.getItemAtPosition(i).toString();
                switch (adapterView.getId()){
                    case R.id.select_class_2:
                        Toast.makeText(fed_activity.this,"您选择了"+content,Toast.LENGTH_SHORT).show();
                        break;
                    default:
                        break;
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });

        request.setOnClickListener(this);
        bn_train2.setOnClickListener(this);
        bn_test2.setOnClickListener(this);
        // bn_con2.setOnClickListener(this);
        // bn_train2.setOnClickListener(this);
        add_data2.setOnClickListener(this);
        down_mod2.setOnClickListener(this);
        up_mod2.setOnClickListener(this);

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
                CreateRequestDialog rdialog = new CreateRequestDialog(fed_activity.this,"连接");
                rdialog.setOnDialogClickListener(new CreateRequestDialog.OnDialogClickListener() {
                    @Override
                    public void onSureCLickListener(EditText mip,EditText mport) {

                        System.out.println("mip:"+mip.getText());
                        System.out.println("mport:"+mport.getText());
                    }

                });
                rdialog.show();
                break;
            case R.id.bn_train_2:
                //点击训练按钮,在这添加后续操作
                fedTrain();
                break;
            case R.id.bn_test_2:
                //点击推理按钮
                String mes = "fed";
                Intent inferIntent = new Intent(fed_activity.this, CameraActivity.class);
                inferIntent.putExtra("from", mes);
                startActivity(inferIntent);
                break;
            case R.id.add_data_2:
                //添加数据
                try {
                    Toast.makeText(this, "开始加载数据", Toast.LENGTH_SHORT).show();
                    getData();
                } catch (FileNotFoundException | InterruptedException e) {
                    e.printStackTrace();
                }
                break;
//            case R.id.bn_trans_2:
//                //迁移学习
//                break;
//            case R.id.bn_con_2:
//                //持续学习
//                dataList.add("D类(10)");
//                adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, dataList);
//                break;
            case R.id.model_down_2:
                //模型下载
                Toast.makeText(this, "开始下载模型", Toast.LENGTH_SHORT).show();
                doRegisterAndDownload();
                break;
            case R.id.model_up_2:
                //模型上传
                break;
            default:
                break;
        }
    }

    public void doRegisterAndDownload() {
        String cacheDir = getCacheDir().getAbsolutePath();
        AtomicBoolean flag = new AtomicBoolean(false);
        Thread thread = new Thread(() -> {
            try {
                flag.set(netUtils.doRegisterAndDownload(cacheDir));
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
            Toast.makeText(this, "模型下载完成", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "模型下载失败", Toast.LENGTH_SHORT).show();
        }

        String modelFilePath = getCacheDir().getAbsolutePath() + "/model/download/" + network_file_name;
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
            List<String> list = Arrays.asList("paper", "rock", "scissors");
            try {
                globalApp.setTlModel(new TransferLearningModelWrapper(parentDir, list));
                Toast.makeText(this, "模型加载完成", Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                throw new RuntimeException("加载模型报错！", e);
            }
        } else {
            Toast.makeText(this, "模型文件不存在，请点击模型下载按钮", Toast.LENGTH_SHORT).show();
        }
    }

    private void getData() throws FileNotFoundException, InterruptedException {
        String dataPath = getCacheDir().getAbsolutePath() + "/rps";
        if (!new File(dataPath).exists()) {
            new File(dataPath).mkdirs();
        }

        String downDataUrl = "http://112.124.109.236/rps_64.zip";
        Thread thread = new Thread(() -> {
            if (!new File(dataPath + "/rps_64").exists()) {
                netUtils.getData(dataPath, downDataUrl);
            }
            try {
                globalApp.getTlModel().addBatchSample(dataPath + "/rps_64");
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        });
        thread.start();
        thread.join();
        Toast.makeText(this, "数据加载完成", Toast.LENGTH_SHORT).show();
    }

    //联邦学习推理
    public void fedTrain() {
        String ckptDirPath = getCacheDir() + "/ckpt";
        File file = new File(ckptDirPath);
        if (!file.exists()) {
            file.mkdir();
        }
        new Thread(() -> {
            netUtils.doConnect();

            for (int i = 0; i < 50; i++) {
                CountDownLatch countDownLatch = new CountDownLatch(1);
                System.out.println(i);
                String ckptFilePath = ckptDirPath + "/checkpoint_" + i + ".ckpt";

                int finalI = i;
                globalApp.getTlModel().fedTraining((epoch, loss) -> {
                    System.out.println("epoch: " + finalI + " ----- loss:" + loss);

                    tv_epoch.setText(finalI + "");
                    tv_loss.setText(loss + "");
                }, (acc) -> {
                    System.out.println("acc------" + acc);
//            tv_acc.setText(acc + "");
                });
                // 保存参数
                globalApp.getTlModel().saveModel(ckptFilePath);
//                Toast.makeText(this, "第"+i+"epoch参数文件正在上传中......", Toast.LENGTH_SHORT).show();
                // 子线程上传和下载参数文件
                netUtils.doUpAndDownLoadParam(ckptFilePath, countDownLatch);
                try {
                    countDownLatch.await();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                if (!new File(ckptFilePath).exists()) {
//                    Toast.makeText(this, "ckpt文件不存在", Toast.LENGTH_SHORT).show();
                    System.out.println("ckpt文件不存在");
                    return;
                }
                globalApp.getTlModel().restoreModel(ckptFilePath);
//                Toast.makeText(this, "第"+i+"epoch参数文件更新中", Toast.LENGTH_SHORT).show();

            }
        }).start();

    }
}