package com.giserpeng.ntripshare.ui.pro;

import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.giserpeng.ntripshare.R;
import com.giserpeng.ntripshare.common.JsonUtil;
import com.giserpeng.ntripshare.ui.user.UserModel;
import com.giserpeng.ntripshare.util.DateUtils;
import com.google.gson.reflect.TypeToken;

import java.util.Date;
import java.util.List;

public class ProDialog extends Dialog {

    public OnClickBottomListener getOnClickBottomListener() {
        return onClickBottomListener;
    }

    /**
     * 确认和取消按钮
     */
    private TextView negtiveBn, positiveBn;

    public ProDialog(@NonNull Context context) {
        super(context);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_pro);
        initView();
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
                if (onClickBottomListener != null) {
                    onClickBottomListener.onPositiveClick();
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
        negtiveBn = (TextView) findViewById(R.id.buyPro);
        positiveBn = (TextView) findViewById(R.id.trialPro);
    }


    /**
     * 设置确定取消按钮的回调
     */
    public OnClickBottomListener onClickBottomListener;

    public ProDialog setOnClickBottomListener(OnClickBottomListener onClickBottomListener) {
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