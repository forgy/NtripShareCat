package com.giserpeng.ntripshare.ui.setting;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;

import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapPoi;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.UiSettings;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.utils.CoordinateConverter;
import com.giserpeng.ntripshare.R;

public class GetMapCoordDialog extends Dialog {

    private MapView mapView ;
    private  LatLng latLng;
    private BitmapDescriptor mBitmapRed = BitmapDescriptorFactory.fromResource(R.drawable.marker);
    /**
     * 确认和取消按钮
     */
    private Button negtiveBn ,positiveBn;
//    public GetMapCoordDialog(@NonNull Context context) {
//        super(context);
//    }

    public  LatLng getLatLng(){
        return  latLng;
    }

    public GetMapCoordDialog(@NonNull Context context, LatLng latLng) {
        super(context);
        this.latLng = latLng;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_get_center);
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
                if ( onClickBottomListener!= null) {
                    onClickBottomListener.onPositiveClick();
                }
            }
        });
        //设置取消按钮被点击后，向外界提供监听
        negtiveBn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if ( onClickBottomListener!= null) {
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
        negtiveBn = (Button) findViewById(R.id.negtive);
        positiveBn = (Button) findViewById(R.id.positive);
        mapView = (MapView) findViewById(R.id.mapview);
        // 隐藏指南针zhi
        UiSettings mUiSettings = mapView.getMap().getUiSettings();
        mUiSettings.setCompassEnabled(true);
        mUiSettings.setRotateGesturesEnabled(false);
        // 删除百度地dao图logo
        mapView.removeViewAt(1);
        mapView.removeViewAt(2);
        mapView.getMap().setOnMapDoubleClickListener(new BaiduMap.OnMapDoubleClickListener() {
            @Override
            public void onMapDoubleClick(LatLng latLng) {
                if ( onClickBottomListener!= null) {
                    GetMapCoordDialog.this.latLng = latLng;
                    onClickBottomListener.onPositiveClick();
                }
            }
        });
        mapView.getMap().setOnMapClickListener(new BaiduMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                GetMapCoordDialog.this.latLng = latLng;
                updateLocation();
            }

            @Override
            public void onMapPoiClick(MapPoi mapPoi) {
            }
        });
        updateLocation();
    }

    private void updateLocation(){
        mapView.getMap().clear();
//        CoordinateConverter converter = new CoordinateConverter()
//                .from(CoordinateConverter.CoordType.GPS)
//                .coord(latLng);
        //desLatLng 转换后的坐标
//        LatLng desLatLng = converter.convert();
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(latLng);
        markerOptions.icon(mBitmapRed);
        mapView.getMap().addOverlay(markerOptions);
        mapView.getMap().setMapStatus(MapStatusUpdateFactory.newLatLng(latLng));
    }

    /**
     * 设置确定取消按钮的回调
     */
    public OnClickBottomListener onClickBottomListener;
    public GetMapCoordDialog setOnClickBottomListener(OnClickBottomListener onClickBottomListener) {
        this.onClickBottomListener = onClickBottomListener;
        return this;
    }

    public interface OnClickBottomListener{
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