package com.example.oppoapp;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Point;
import android.os.Bundle;
import android.view.Display;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;

public class CreateRequestDialog extends Dialog implements View.OnClickListener{

    private EditText mip, mport;
    private String sip,sport, sconfirm;
    private Button bconfirm;
    private View.OnClickListener confirmListener;
    //控制点击dialog外部是否dismiss
    private boolean isCancelable = true;

    //返回键是否dismiss
    private boolean isCanceledOnTouchOutside = true;

    //Dialog View
    private View view;
        public CreateRequestDialog(@NonNull Context context) {
        super(context);

    }
    public CreateRequestDialog(@NonNull Context context, View view) {
        super(context, R.style.InfoEditDialog);
        this.view = view;
    }
    /**
     * 设置是否可以点击 Dialog View 外部关闭 Dialog
     *
     * @param isCancelable true可关闭，false不可关闭
     */
    public void setCancelable(boolean isCancelable) {
        this.isCancelable = isCancelable;
    }
    /**
     * 设置是否可以按返回键关闭 Dialog
     *
     * @param isCanceledOnTouchOutside true可关闭，false不可关闭
     */
    public void setCanceledOnTouchOutside(boolean isCanceledOnTouchOutside) {
        this.isCanceledOnTouchOutside = isCanceledOnTouchOutside;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(view);
        setCancelable(isCancelable);//点击外部是否可以关闭Dialog
        setCanceledOnTouchOutside(isCanceledOnTouchOutside);//返回键是否可以关闭Dialog

        // 自定义dialog 宽度
        WindowManager m = getWindow().getWindowManager();
        Display d = m.getDefaultDisplay();
        WindowManager.LayoutParams p = getWindow().getAttributes();
        Point size = new Point();
        d.getSize(size);
        p.width = (int) ((size.x)*0.9);        //设置为屏幕的0.7倍宽度
        getWindow().setAttributes(p);
        System.out.println("自定义大小结束");
        mip = (EditText) findViewById(R.id.ip);
        mport = (EditText) findViewById(R.id.port);
        bconfirm = findViewById(R.id.btn_rq);
        bconfirm.setOnClickListener(this);
        System.out.println("绑定监听事件");
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_rq:
                //在这里处理点击弹窗连接按钮之后的操作
                //mip.getText()是IP的值
                Toast.makeText(getContext(), "IP："+mip.getText()+"PORT:"+mport.getText(),Toast.LENGTH_SHORT).show();
                //关闭弹窗
                dismiss();
                break;
            default:
                break;
        }

    }
}