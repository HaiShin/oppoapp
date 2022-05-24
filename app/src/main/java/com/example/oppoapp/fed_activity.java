package com.example.oppoapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Point;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class fed_activity extends AppCompatActivity implements View.OnClickListener{
    private TextView tv_trans2;
    private TextView tv_fed2;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fed);
        tv_trans2 = (TextView) findViewById(R.id.tv_trans2);
        tv_fed2 = (TextView) findViewById(R.id.tv_fed2);
        tv_trans2.setSelected(false);
        tv_fed2.setSelected(true);
        tv_trans2.setOnClickListener(this);
        request = findViewById(R.id.request);
        mip = findViewById(R.id.ip);
        mport = findViewById(R.id.port);
        bn_train2 = findViewById(R.id.bn_train_2);
        bn_test2 = findViewById(R.id.bn_test_2);
        bn_con2 = findViewById(R.id.bn_con_2);
        bn_tran2 = findViewById(R.id.bn_trans_2);
        add_data2 = findViewById(R.id.add_data_2);
        down_mod2 = findViewById(R.id.model_down_2);
        up_mod2 = findViewById(R.id.model_up_2);
        request.setOnClickListener(this);
        bn_train2.setOnClickListener(this);
        bn_test2.setOnClickListener(this);
        bn_con2.setOnClickListener(this);
        bn_train2.setOnClickListener(this);
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
//                Intent i1 = new Intent();
//                View inflate = LayoutInflater.from(context).inflate(R.layout.request_dialog,null,false);
//                CreateRequestDialog crdialog = new CreateRequestDialog(fed_activity.this,inflate);
//                crdialog.setCancelable(true);
//                crdialog.setCanceledOnTouchOutside(true);
//                crdialog.show();
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
                Toast.makeText(this,"点击了训练按钮2",Toast.LENGTH_SHORT).show();
                break;
            case R.id.bn_test_2:
                //点击推理按钮
                Toast.makeText(this,"点击了推理按钮2",Toast.LENGTH_SHORT).show();
                break;
            case R.id.add_data_2:
                //添加数据
                break;
            case R.id.bn_trans_2:
                //迁移学习
                break;
            case R.id.bn_con_2:
                //持续学习
                break;
            case R.id.model_down_2:
                //模型下载
                break;
            case R.id.model_up_2:
                //模型上传
                break;
            default:
                break;
        }
    }
}