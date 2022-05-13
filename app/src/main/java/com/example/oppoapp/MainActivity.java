package com.example.oppoapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.PrimitiveIterator;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{
    private TextView tv_trans;
    private TextView tv_fed;
    private Button bn_train;
    private Button bn_test;
    private Button add_data;
    private Button bn_tran;
    private Button bn_con;
    private Button down_mod;
    private Button up_mod;
    private Spinner class_sel_spinner;
    private String data[];
    private ArrayAdapter<String> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tv_trans = (TextView) findViewById(R.id.tv_trans);
        tv_fed = (TextView) findViewById(R.id.tv_fed);
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
        data = getResources().getStringArray(R.array.classname);
        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, data);
        class_sel_spinner.setAdapter(adapter);
        class_sel_spinner.setSelection(0, true);
        class_sel_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener(){
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                String content = adapterView.getItemAtPosition(i).toString();
                switch (adapterView.getId()){
                    case R.id.select_class:
                        Toast.makeText(MainActivity.this,"您选择了"+content,Toast.LENGTH_SHORT).show();
                        break;
                    default:
                        break;
                }
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
                intent.setClass(this, fed_activity.class);
                startActivity(intent);
                break;
            case R.id.bn_train:
                //点击训练按钮,在这添加后续操作
                Toast.makeText(this,"点击了训练按钮",Toast.LENGTH_SHORT).show();
                break;
            case R.id.bn_test:
                //点击推理按钮
                Toast.makeText(this,"点击了推理按钮",Toast.LENGTH_SHORT).show();
                break;
            case R.id.add_data:
                //添加数据
                break;
            case R.id.bn_trans:
                //迁移学习
                break;
            case R.id.bn_con:
                //持续学习
                break;
            case R.id.model_down:
                //模型下载
                break;
            case R.id.model_up:
                //模型上传
                break;
            default:
                break;

        }
    }
}