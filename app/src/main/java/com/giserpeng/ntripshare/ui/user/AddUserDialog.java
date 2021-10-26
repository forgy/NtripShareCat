package com.giserpeng.ntripshare.ui.user;

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
import com.giserpeng.ntripshare.util.DateUtils;
import com.google.gson.reflect.TypeToken;

import java.util.Date;
import java.util.List;

public class AddUserDialog extends Dialog {

    private EditText editTextUsername;
    private TextView editTextTitleAdduser;
    private EditText editTextPassword;
    private EditText editTextEndtime;

    public UserModel getUserModel() {
        UserModel userModel = new UserModel();
        userModel.setUserName(editTextUsername.getText().toString());
        userModel.setPassword(editTextPassword.getText().toString());
        userModel.setEndTime(DateUtils.StringToDate(editTextEndtime.getText().toString(),"yyyy-MM-dd HH:mm:ss"));
        return userModel;
    }

    public void setUserModel(UserModel userModel) {
        editTextUsername.setText(userModel.getUserName());
        editTextUsername.setEnabled(false);
        editTextTitleAdduser.setText(R.string.edit_user);
        editTextPassword.setText(userModel.getPassword());
        editTextEndtime.setText(DateUtils.dateToString(userModel.getEndTime(),"yyyy-MM-dd HH:mm:ss"));
    }

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

    /**
     * 确认和取消按钮
     */
    private Button negtiveBn, positiveBn;

    public AddUserDialog(@NonNull Context context) {
        super(context);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_adduser);
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
        negtiveBn = (Button) findViewById(R.id.addusernegtive);
        positiveBn = (Button) findViewById(R.id.adduserpositive);
        editTextUsername =(EditText)findViewById(R.id.textViewUsername);
        editTextPassword =(EditText)findViewById(R.id.textViewPassword);
        editTextEndtime =(EditText)findViewById(R.id.textViewEndtime);
        editTextTitleAdduser = (TextView) findViewById(R.id.titleAdduser);
        editTextEndtime.setText(DateUtils.dateToString(new Date(),"yyyy-MM-dd HH:mm:ss"));
    }

    private boolean checkValue(){
        if(editTextUsername.getText()== null || "".equalsIgnoreCase(editTextUsername.getText().toString())){
            Toast.makeText(getContext(),R.string.enter_username,Toast.LENGTH_LONG).show();
            return false;
        }
        if(editTextPassword.getText()== null || "".equalsIgnoreCase(editTextPassword.getText().toString())){
            Toast.makeText(getContext(),R.string.enter_password,Toast.LENGTH_LONG).show();
            return false;
        }
        if(editTextEndtime.getText()== null || "".equalsIgnoreCase(editTextEndtime.getText().toString())
                || DateUtils.StringToDate(editTextEndtime.getText().toString(),"yyyy-MM-dd HH:mm:ss") ==null){
            Toast.makeText(getContext(),R.string.enter_time,Toast.LENGTH_LONG).show();
            return false;
        }
        if(editTextUsername.isEnabled()){
            SharedPreferences defaultSharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
            String string = defaultSharedPreferences.getString("userlist", "");
            if(!"".equalsIgnoreCase(string)){
                List<UserModel> userModels = JsonUtil.json2object(string, new TypeToken<List<UserModel>>() {
                });
                if(userModels != null){
                    for(UserModel userModel:userModels){
                        if(userModel.getUserName().equalsIgnoreCase(editTextUsername.getText().toString())){
                            Toast.makeText(getContext(),R.string.user_exist,Toast.LENGTH_LONG).show();
                            return false;
                        }
                    }
                }
            }
        }
        return true;
    }


    /**
     * 设置确定取消按钮的回调
     */
    public OnClickBottomListener onClickBottomListener;

    public AddUserDialog setOnClickBottomListener(OnClickBottomListener onClickBottomListener) {
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