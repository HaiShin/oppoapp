package com.example.oppoapp;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {
    private TextView tv_trans;
    private TextView tv_fed;
    private Spinner class_sel_spinner;
    private String data[];
    private ArrayAdapter<String> adapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tv_trans = (TextView) findViewById(R.id.tv_trans);
        tv_fed = (TextView) findViewById(R.id.tv_fed);
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
    }
}