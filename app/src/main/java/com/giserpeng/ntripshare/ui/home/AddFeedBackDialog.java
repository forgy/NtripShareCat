package com.giserpeng.ntripshare.ui.home;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.giserpeng.ntripshare.R;

public class AddFeedBackDialog extends Dialog {

    private EditText editTextdes;
    private TextView editTextWeiXin;


    public Button getNegtiveBn() {
        return negtiveBn;
    }

    public void setNegtiveBn(Button negtiveBn) {
        this.negtiveBn = negtiveBn;
    }

    public Button getPositiveBn() {
        return positiveBn;
    }

    public void setPositiveBn(Button positiveBn) {
        this.positiveBn = positiveBn;
    }

    public OnClickBottomListener getOnClickBottomListener() {
        return onClickBottomListener;
    }


    public String getDes(){
        return  editTextdes.getText().toString();
    }

    public  String getWexin(){
        return  editTextWeiXin.getText().toString();

    }    /**
     * 确认和取消按钮
     */
    private Button negtiveBn, positiveBn;

    public AddFeedBackDialog(@NonNull Context context) {
        super(context);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_feedback);
        //初始化界面控件
        initView();
        //初始化界面控件的事件
        initEvent();
    }

    /**
     * 初始化界面的确定和取消监听器
     */
    private void initEvent() {
        //设置确定按钮被点击后，向外界提供监听
        positiveBn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(checkValue()){
                    if (onClickBottomListener != null) {
                        onClickBottomListener.onPositiveClick();
                    }
                }
            }
        });
        //设置取消按钮被点击后，向外界提供监听
        negtiveBn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onClickBottomListener != null) {
                    onClickBottomListener.onNegtiveClick();
                }
            }
        });
    }

    @Override
    public void show() {
        super.show();
    }

    /**
     * 初始化界面控件
     */
    private void initView() {
        negtiveBn = (Button) findViewById(R.id.addfeedbacknegtive);
        positiveBn = (Button) findViewById(R.id.addfeedbackpositive);
        editTextWeiXin =(EditText)findViewById(R.id.weixin);
        editTextdes =(EditText)findViewById(R.id.des);
    }

    private boolean checkValue(){
        if(editTextdes.getText()== null || "".equalsIgnoreCase(editTextdes.getText().toString())){
            Toast.makeText(getContext(),R.string.enter_des,Toast.LENGTH_LONG).show();
            return false;
        }

        return true;
    }


    /**
     * 设置确定取消按钮的回调
     */
    public OnClickBottomListener onClickBottomListener;

    public AddFeedBackDialog setOnClickBottomListener(OnClickBottomListener onClickBottomListener) {
        this.onClickBottomListener = onClickBottomListener;
        return this;
    }

    public interface OnClickBottomListener {
        /**
         * 点击确定按钮事件
         */
        public void onPositiveClick();

        /**
         * 点击取消按钮事件
         */
        public void onNegtiveClick();
    }

}