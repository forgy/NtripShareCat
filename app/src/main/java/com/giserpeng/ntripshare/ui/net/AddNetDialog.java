package com.giserpeng.ntripshare.ui.net;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.baidu.mapapi.model.LatLng;
import com.giserpeng.ntripshare.R;
import com.giserpeng.ntripshare.ntrip.CasterClient;
import com.giserpeng.ntripshare.ntrip.source.NtripSource;
import com.giserpeng.ntripshare.ntrip.source.ServerInfo;
import com.giserpeng.ntripshare.ntrip.source.StreamSource;
import com.giserpeng.ntripshare.ui.setting.GetMapCoordDialog;
import com.giserpeng.ntripshare.util.ToolUtil;
import com.wrbug.editspinner.EditSpinner;
import com.wrbug.editspinner.SimpleAdapter;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class AddNetDialog extends Dialog {

    private EditText editTextUsername;
    private TextView editTextTitleAddNode;
    private EditText editTextPassword;
    EditText textViewIp;
    EditText textViewPort;
    EditSpinner spinnerMountPoint;
    EditText textViewLon;
    Button selMap;
    Button updateMountPoint;
    EditText textViewLat;
    String uuid;
    List<String> points = new ArrayList<>();

    public NetPointModel getUserModel() {
        NetPointModel userModel = new NetPointModel();
        userModel.setUserName(editTextUsername.getText().toString());
        userModel.setPassword(editTextPassword.getText().toString());
        userModel.setPort(Integer.valueOf(textViewPort.getText().toString()));
        userModel.setLon(Double.parseDouble(textViewLon.getText().toString()));
        userModel.setLat(Double.parseDouble(textViewLat.getText().toString()));
//        userModel.setMountPoint(spinnerMountPoint.getSelectedItem().toString());
        userModel.setMountPoint(spinnerMountPoint.getText());
        userModel.setUuid(uuid);
        userModel.setIp(textViewIp.getText().toString());
        return userModel;
    }

    public void setUserModel(NetPointModel userModel) {
        try{

        updateMountPoint(userModel.getIp(), userModel.getPort(), userModel.getUserName(), userModel.getPassword());
        uuid = userModel.getUuid();
        editTextUsername.setText(userModel.getUserName());
        editTextTitleAddNode.setText(R.string.edit_node);
        editTextPassword.setText(userModel.getPassword());
        textViewIp.setText(userModel.getIp());
        textViewPort.setText(String.valueOf(userModel.getPort()));

        textViewLon.setText(String.valueOf(userModel.getLon()));
        textViewLat.setText(String.valueOf(userModel.getLat()));
//        for (int i= 0;i < points.size();i++){
//            String key = points.get(i);
//            if(key.equalsIgnoreCase(userModel.getMountPoint())){
//                spinnerMountPoint.setSelection(i);
                spinnerMountPoint.setText(userModel.getMountPoint());
//            }
//        }

        }catch (Exception e){
            Log.e("setUserModel",e.getMessage());
        }
    }


    /*
   更新接入点
    */
    public void updateMountPoint(String ip, int port, String userName, String password) {
        new Thread(new Runnable() {
            public void run() {
                try {
                    ServerInfo serverInfo = new ServerInfo(ip, port, userName, password);
                    CasterClient casterClient = new CasterClient(serverInfo);
                    List<NtripSource> tables = casterClient.getSourceTable();
                    Log.i("updateMountPoint", "==" + tables.size());
                    Message msg = new Message();
                    msg.what = 100;  //消息发送的标志
                    msg.obj = tables; //消息发送的内容如：  Object String 类 int
                    handler.sendMessage(msg);

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == 100) {
                List<NtripSource> tables = (List) msg.obj;
//                List<String> names = new ArrayList<>();
//                List lines = new ArrayList<>();
                points.clear();
                for (NtripSource ntripSource : tables) {
                    if (ntripSource instanceof StreamSource) {
                        points.add(((StreamSource) ntripSource).getMountPoint());
                    }
                }

                SimpleAdapter adapter = new SimpleAdapter(getContext(),  points);
                spinnerMountPoint.setAdapter(adapter);
            }
        }
    };


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

    public AddNetDialog(@NonNull Context context) {
        super(context);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_addnode);
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
                if (checkValue()) {
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

        selMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LatLng latLng = new LatLng(40,117);
                if(textViewLon.getText().toString().length() != 0 && textViewLat.getText().toString().length() != 0){
                    latLng = new LatLng(Double.parseDouble(textViewLat.getText().toString()),
                            Double.parseDouble(textViewLon.getText().toString()));
                }
                final GetMapCoordDialog dialog = new GetMapCoordDialog(getContext(), latLng);
                dialog.setOnClickBottomListener(new GetMapCoordDialog.OnClickBottomListener() {
                    @Override
                    public void onPositiveClick() {
                        LatLng latLng = dialog.getLatLng();
                        if (latLng != null) {
                            textViewLon.setText(String.valueOf(latLng.longitude));
                            textViewLat.setText(String.valueOf(latLng.latitude));
                        }
                        dialog.dismiss();
                    }
                    @Override
                    public void onNegtiveClick() {
                        dialog.dismiss();
                    }
                });
                dialog.show();
            }
        });

        updateMountPoint.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try{
                    updateMountPoint(textViewIp.getText().toString(),Integer.valueOf(textViewPort.getText().toString()),editTextUsername.getText().toString(),editTextPassword.getText().toString());
                }catch (Exception e){
                    Toast.makeText(getContext(),R.string.get_mountpoint_error,Toast.LENGTH_LONG).show();
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
        editTextUsername = (EditText) findViewById(R.id.textViewNodeUsername);
        editTextPassword = (EditText) findViewById(R.id.textViewNodePassword);
        editTextTitleAddNode = (TextView) findViewById(R.id.titleAddNode) ;
        textViewIp = (EditText) findViewById(R.id.textViewIp);
        textViewPort = (EditText) findViewById(R.id.textViewPort);
        textViewLon = (EditText) findViewById(R.id.textViewLon);
        textViewLat = (EditText) findViewById(R.id.textViewLat);
        spinnerMountPoint = (EditSpinner) findViewById(R.id.spinnerMountPoint);
        selMap = (Button) findViewById(R.id.selMap);
        updateMountPoint = (Button) findViewById(R.id.updateMountpoint);
        uuid = UUID.randomUUID().toString();
    }

    private boolean checkValue() {
        if (editTextUsername.getText() == null || "".equalsIgnoreCase(editTextUsername.getText().toString())) {
            Toast.makeText(getContext(), R.string.enter_username, Toast.LENGTH_LONG).show();
            return false;
        }
        if (editTextPassword.getText() == null || "".equalsIgnoreCase(editTextPassword.getText().toString())) {
            Toast.makeText(getContext(), R.string.enter_password, Toast.LENGTH_LONG).show();
            return false;
        }
        if (textViewIp.getText() == null || "".equalsIgnoreCase(textViewIp.getText().toString())|| !ToolUtil.isIPAddress(textViewIp.getText().toString())) {
            Toast.makeText(getContext(), R.string.enter_ip, Toast.LENGTH_LONG).show();
            return false;
        }
        if (textViewPort.getText() == null || "".equalsIgnoreCase(textViewPort.getText().toString())) {
            Toast.makeText(getContext(), R.string.enter_port, Toast.LENGTH_LONG).show();
            return false;
        }
        if (textViewLon.getText() == null || "".equalsIgnoreCase(textViewLon.getText().toString())) {
            Toast.makeText(getContext(), R.string.enter_lon, Toast.LENGTH_LONG).show();
            return false;
        }
        if (textViewLat.getText() == null || "".equalsIgnoreCase(textViewLat.getText().toString())) {
            Toast.makeText(getContext(), R.string.enter_lat, Toast.LENGTH_LONG).show();
            return false;
        }
//        if (spinnerMountPoint.getSelectedItem() == null || "".equalsIgnoreCase(spinnerMountPoint.getSelectedItem().toString())) {
        if (spinnerMountPoint.getText() == null || "".equalsIgnoreCase(spinnerMountPoint.getText())) {
            Toast.makeText(getContext(), R.string.enter_mountpoint, Toast.LENGTH_LONG).show();
            return false;
        }
        return true;
    }


    /**
     * 设置确定取消按钮的回调
     */
    public OnClickBottomListener onClickBottomListener;

    public AddNetDialog setOnClickBottomListener(OnClickBottomListener onClickBottomListener) {
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