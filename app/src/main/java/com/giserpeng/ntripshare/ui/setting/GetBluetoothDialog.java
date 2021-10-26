package com.giserpeng.ntripshare.ui.setting;

import android.app.Dialog;
import android.bluetooth.BluetoothClass;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.giserpeng.ntripshare.view.swipemenulistview.BaseSwipListAdapter;
import com.giserpeng.ntripshare.R;
import com.inuker.bluetooth.library.BluetoothClient;
import com.inuker.bluetooth.library.connect.listener.BluetoothStateListener;
import com.inuker.bluetooth.library.search.SearchRequest;
import com.inuker.bluetooth.library.search.SearchResult;
import com.inuker.bluetooth.library.search.response.SearchResponse;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GetBluetoothDialog extends Dialog {

    private BluetoothAdapter listAdapter;
    private TextView tvEmpty;
    private List<SearchResult> devList = new ArrayList<>();
    private Map<String, SearchResult> map = new HashMap<>();
    BluetoothClient mClient;
    ProgressBar progressBar;

    /**
     * 确认和取消按钮
     */
    private Button negtiveBn, positiveBn;

    public GetBluetoothDialog(@NonNull Context context) {
        super(context);
        mClient = new BluetoothClient(context);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_get_bluetooth);
        //初始化界面控件
        initView();
        //初始化界面控件的事件
        initEvent();
        if (!mClient.isBluetoothOpened()) {
            Toast.makeText(getContext(), R.string.open_bluetooth, Toast.LENGTH_LONG).show();
            progressBar.setVisibility(View.INVISIBLE);
            mClient.registerBluetoothStateListener(new BluetoothStateListener() {
                @Override
                public void onBluetoothStateChanged(boolean openOrClosed) {
                    searchBluetooth();
                }
            });
            mClient.openBluetooth();
        } else {
            searchBluetooth();
        }
    }

    private void searchBluetooth() {
        SearchRequest request = new SearchRequest.Builder()
                .searchBluetoothLeDevice(3000, 3)   // 先扫BLE设备3次，每次3s
                .searchBluetoothClassicDevice(5000) // 再扫经典蓝牙5s
                .searchBluetoothLeDevice(2000)      // 再扫BLE设备2s
                .build();

        mClient.search(request, new SearchResponse() {
            @Override
            public void onSearchStarted() {
                progressBar.setVisibility(View.VISIBLE);
                map.clear();
                devList.clear();
                tvEmpty.setVisibility(View.VISIBLE);
            }

            @Override
            public void onDeviceFounded(SearchResult device) {
                Log.i("onDeviceFounded","onDeviceFounded");

                BluetoothClass bluetoothClass = device.device.getBluetoothClass();
                final int deviceClass = bluetoothClass.getDeviceClass(); //设备类型（音频、手机、电脑、音箱等等）
                final int majorDeviceClass = bluetoothClass.getMajorDeviceClass();//具体的设备类型（例如音频设备又分为音箱、耳机、麦克风等等）
                if (deviceClass == BluetoothClass.Device.AUDIO_VIDEO_WEARABLE_HEADSET) {
                    //音箱

                } else if (deviceClass == BluetoothClass.Device.AUDIO_VIDEO_MICROPHONE) {
                    //麦克风

                } else if (deviceClass == BluetoothClass.Device.AUDIO_VIDEO_HEADPHONES) {
                    //耳机

                } else if (majorDeviceClass == BluetoothClass.Device.Major.COMPUTER) {
                    //电脑

                } else if (majorDeviceClass == BluetoothClass.Device.Major.PHONE) {
                    //手机

                } else if (majorDeviceClass == BluetoothClass.Device.Major.HEALTH) {
                    //健康类设备

                } else {
                    if(map.containsKey(device.getAddress())){

                        devList.remove(map.get(device.getAddress()));
                    }
                    map.put(device.getAddress(),device);
                    devList.add(device);
                    listAdapter.notifyDataSetChanged();
                    tvEmpty.setVisibility(View.INVISIBLE);
                }
            }

            @Override
            public void onSearchStopped() {
                progressBar.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onSearchCanceled() {

            }
        });
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
                    if (!mClient.isBluetoothOpened()) {
                        Toast.makeText(getContext(), R.string.open_bluetooth, Toast.LENGTH_LONG).show();
                    } else {
                        searchBluetooth();
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
        ListView lv = findViewById(R.id.lv);
        tvEmpty = findViewById(R.id.tvEmpty);
        listAdapter = new BluetoothAdapter();
        lv.setAdapter(listAdapter);
        lv.setOnItemClickListener((parent, view, position, id) -> {
            if (onClickBottomListener != null) {
                onClickBottomListener.onSelectedClick(devList.get(position));
            }
        });
        negtiveBn = (Button) findViewById(R.id.negtive);
        positiveBn = (Button) findViewById(R.id.positive);
        progressBar = (ProgressBar) findViewById(R.id.pro);
    }


    /**
     * 设置确定取消按钮的回调
     */
    public OnClickBottomListener onClickBottomListener;

    public GetBluetoothDialog setOnClickBottomListener(OnClickBottomListener onClickBottomListener) {
        this.onClickBottomListener = onClickBottomListener;
        return this;
    }

    public interface OnClickBottomListener {
        /**
         * 点击确定按钮事件
         */
        public void onSelectedClick(SearchResult bluetoothDevice);

        /**
         * 点击取消按钮事件
         */
        public void onNegtiveClick();
    }

    class BluetoothAdapter extends BaseSwipListAdapter {

        @Override
        public int getCount() {
            return devList.size();
        }

        @Override
        public SearchResult getItem(int position) {
            return devList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = View.inflate(getContext(),
                        R.layout.item_scan, null);
                new GetBluetoothDialog.BluetoothAdapter.ViewHolder(convertView);
            }
            GetBluetoothDialog.BluetoothAdapter.ViewHolder holder = (GetBluetoothDialog.BluetoothAdapter.ViewHolder) convertView.getTag();
            SearchResult item = getItem(position);
            holder.tvName.setText(item.device.getName());
            holder.tvAddr.setText(item.device.getAddress());
            holder.tvRssi.setText(String.valueOf(item.rssi));
            return convertView;
        }

        class ViewHolder {
            TextView tvName;
            TextView tvAddr;
            TextView tvRssi;

            public ViewHolder(View view) {
                tvName = view.findViewById(R.id.tvName);
                tvAddr = view.findViewById(R.id.tvAddr);
                tvRssi = view.findViewById(R.id.tvRssi);
                view.setTag(this);
            }
        }

        @Override
        public boolean getSwipEnableByPosition(int position) {
            if (position % 2 == 0) {
                return false;
            }
            return true;
        }
    }
}