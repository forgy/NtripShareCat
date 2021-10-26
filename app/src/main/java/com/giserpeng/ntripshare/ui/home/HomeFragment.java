package com.giserpeng.ntripshare.ui.home;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.CircleOptions;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.map.PolylineOptions;
import com.baidu.mapapi.map.Stroke;
import com.baidu.mapapi.map.TextOptions;
import com.baidu.mapapi.map.UiSettings;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.utils.CoordinateConverter;
import com.giserpeng.ntripshare.MainActivity;
import com.giserpeng.ntripshare.R;
import com.giserpeng.ntripshare.ShareApplication;
import com.giserpeng.ntripshare.ntrip.Clients.Client;
import com.giserpeng.ntripshare.ntrip.NTRIPService;
import com.giserpeng.ntripshare.ntrip.Servers.NtripCaster;
import com.giserpeng.ntripshare.ntrip.Servers.ReferenceStation;
import com.giserpeng.ntripshare.ntrip.Tools.GPSPosition;
import com.giserpeng.ntripshare.ntrip.Tools.NMEA;
import com.giserpeng.ntripshare.ui.net.NetPointModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;


public class HomeFragment extends Fragment {
    public static HomeFragment INSTANCE;
    MapView mapView;
    Timer timer;

    private MapView getMapView() {
        return mapView;
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        INSTANCE = this;
        Log.i("SettingFragment", "onCreatePreferences");
        View root = inflater.inflate(R.layout.fragment_home, container, false);
        mapView = (MapView) root.findViewById(R.id.mapview);
        Button button = root.findViewById(R.id.buttonLocation);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                center();
            }
        });
        hidemapControl();
        BaiduMap mBaiduMap = mapView.getMap();
        SharedPreferences defaultSharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        String maptype = defaultSharedPreferences.getString("maptype", "street");
        if (maptype.equalsIgnoreCase("street")) {
            mBaiduMap.setMapType(BaiduMap.MAP_TYPE_NORMAL);
        } else {
            mBaiduMap.setMapType(BaiduMap.MAP_TYPE_SATELLITE);
        }

        mBaiduMap.setMyLocationEnabled(true);
        startTimer();
        updateMap();
        center();
        return root;
    }

    public void setLocation(MyLocationData locData) {
        getMapView().getMap().setMyLocationData(locData);
    }

//    public void setMapStatus(MapStatusUpdate status1) {
//        getMapView().getMap().setMapStatus(status1);
//    }

    private void startTimer() {
        Log.i("HomeFragment", "startTimer()");
        if (timer == null) {
            timer = new Timer();
        }
        if (task == null) {
            task = new TimerTask() {
                @Override
                public void run() {
                    updateMap();
                }
            };
        }

        if (timer != null && task != null) {
            timer.schedule(task, 1000, 5000);
        }
    }

    private void stopTimer() {
        Log.i("HomeFragment", "stopTimer()");
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
        if (task != null) {
            task.cancel();
            task = null;
        }
    }

    public void onDestroyView() {
        stopTimer();
        super.onDestroyView();
    }

    private void center() {

        if (mapView.getMap().getLocationData() != null) {
            LatLng GEO_BEIJING = new LatLng(mapView.getMap().getLocationData().latitude, mapView.getMap().getLocationData().longitude);
            MapStatusUpdate status1 = MapStatusUpdateFactory.newLatLng(GEO_BEIJING);
            mapView.getMap().setMapStatus(status1);

        } else {
            MainActivity activity = (MainActivity) getActivity();
            if (activity.getCurrentLongitude() != 0 && activity.getCurrentLatitude() != 0) {
                LatLng GEO_BEIJING = new LatLng(activity.getCurrentLatitude(), activity.getCurrentLongitude());
                MapStatusUpdate status1 = MapStatusUpdateFactory.newLatLng(GEO_BEIJING);
                mapView.getMap().setMapStatus(status1);

                MyLocationData locData = new MyLocationData.Builder()
                        .accuracy(10)
                        // 此处设置开发者获取到的方向信息，顺时针0-360
                        .direction(0).latitude(activity.getCurrentLatitude())
                        .longitude(activity.getCurrentLongitude()).build();
                setLocation(locData);
            }
        }

        updateMap();
    }

    TimerTask task = new TimerTask() {
        @Override
        public void run() {
            updateMap();
        }
    };
    private BitmapDescriptor mBitmapRed = BitmapDescriptorFactory.fromResource(R.drawable.marker);
    private BitmapDescriptor mBitmapblue = BitmapDescriptorFactory.fromResource(R.drawable.marker_blue);
    private BitmapDescriptor mBitmapGreen = BitmapDescriptorFactory.fromResource(R.drawable.marker_green);

    private void updateMap() {
        Log.i("HomeFragment", "updateMap()");
        try {
            //清除地图上的所有覆盖物
            getMapView().getMap().clear();
            ReferenceStation referenceStation = NtripCaster.getInstance().getReferenceStation();
            LatLng GEO_BEIJING = null;
            if (ShareApplication.getSourceType(getActivity()) == ShareApplication.SOURCE_TYP.NTRRIP_NET) {
                if (NTRIPService.INSTANCE == null) {
                    return;
                }
                int num = ShareApplication.getMaxDis(getActivity());
                List<NetPointModel> netPointModels = NTRIPService.INSTANCE.getOnLineNetNodes();

                List<MarkerOptions> markerOptionsArrayList = new ArrayList<>();
                for (NetPointModel netPointModel : netPointModels) {
                    MarkerOptions markerOptions = new MarkerOptions();
                    markerOptions.position(new LatLng(netPointModel.getLat(), netPointModel.getLon()));
                    markerOptions.icon(mBitmapblue);
                    markerOptionsArrayList.add(markerOptions);

                    OverlayOptions mTextOptions = new TextOptions()
                            .text(netPointModel.getUserName()) //文字内容
                            .bgColor(0xFFFFFFFF) //背景色
                            .fontSize(20) //字号
                            .fontColor(Color.BLUE) //文字颜色
                            .position(new LatLng(netPointModel.getLat(), netPointModel.getLon()));
                    //在地图上显示文字覆盖物
                    getMapView().getMap().addOverlay(mTextOptions);

                    CircleOptions mCircleOptions = new CircleOptions().center(new LatLng(netPointModel.getLat(), netPointModel.getLon()))
                            .radius(num)
                            .fillColor(Color.TRANSPARENT) //填充颜色
                            .dottedStroke(true)
                            .stroke(new Stroke(2, 0xAA0000FF)); //边框宽和边框颜色
                    //在地图上显示圆
                    getMapView().getMap().addOverlay(mCircleOptions);
                }

                for (MarkerOptions markerOptions : markerOptionsArrayList) {
                    getMapView().getMap().addOverlay(markerOptions);
                }
                if (netPointModels.size() > 0) {
                    LatLng center = getNetCenter(netPointModels);
                    MyLocationData locData = new MyLocationData.Builder()
                            .accuracy(0).latitude(center.latitude)
                            .longitude(center.longitude).build();
                    setLocation(locData);
                }

            }
            else if(ShareApplication.getSourceType(getActivity()) == ShareApplication.SOURCE_TYP.NTRIPV1) {
                SharedPreferences defaultSharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
                String ntriplocation = defaultSharedPreferences.getString("ntriplocation", "internalfine");
                if (ntriplocation.equals("auto")) {
                   if(referenceStation != null){
                      GPSPosition position = referenceStation.getCurrentPosition() ;
                      if(position != null){
                          GEO_BEIJING =  new LatLng(position.lat, position.lon);
                      }
                   }
                }  if ("manual".equalsIgnoreCase(ntriplocation)) {
                     GEO_BEIJING = new LatLng(NTRIPService.ManualLat, NTRIPService.ManualLon);
                }
            }
            if(GEO_BEIJING != null){
                int num = ShareApplication.getMaxDis(getActivity());
                CircleOptions mCircleOptions = new CircleOptions().center(GEO_BEIJING)
                        .radius(num)
                        .fillColor(Color.TRANSPARENT) //填充颜色
                        .dottedStroke(true)
                        .stroke(new Stroke(2, 0xAA0000FF)); //边框宽和边框颜色
                //在地图上显示圆
                getMapView().getMap().addOverlay(mCircleOptions);
                MarkerOptions markerOptions = new MarkerOptions();
                markerOptions.position(GEO_BEIJING);
                markerOptions.icon(mBitmapblue);
                getMapView().getMap().addOverlay(markerOptions);
            }

            if (referenceStation != null) {
                List<Client> list = referenceStation.getClient();
                Log.i("HomeFragment", "updateMap()" + list.size());
                for (Client client : list) {
                    if (client != null && client.getPosition() != null) {
                        CoordinateConverter converter = new CoordinateConverter()
                                .from(CoordinateConverter.CoordType.GPS)
                                .coord(new LatLng(client.getPosition().lat, client.getPosition().lon));
                        //desLatLng 转换后的坐标
                        LatLng desLatLng = converter.convert();
                        Log.i("HomeFragment", desLatLng.longitude + "updateMap()" + desLatLng.latitude);
                        MarkerOptions markerOptions = new MarkerOptions();
                        markerOptions.position(desLatLng);
                        markerOptions.icon(mBitmapRed);
                        if(client.getPosition() != null && client.getPosition().quality == 4){
                            markerOptions.icon(mBitmapGreen);
                        }
                        //构建TextOptions对象
                        OverlayOptions mTextOptions = new TextOptions()
                                .text(client.getClientUserName()) //文字内容
                                .bgColor(0xFFFFFFFF) //背景色
                                .fontSize(20) //字号
                                .fontColor(Color.RED) //文字颜色
                                .position(desLatLng);
                        //在地图上显示文字覆盖物
                        getMapView().getMap().addOverlay(mTextOptions);
                        getMapView().getMap().addOverlay(markerOptions);
                        if (ShareApplication.getSourceType(getActivity()) == ShareApplication.SOURCE_TYP.NTRRIP_NET) {
                            NetPointModel netPointModel = referenceStation.getNearestNode(client);
                            if (netPointModel != null) {
                                List<LatLng> latLngs = new ArrayList<LatLng>();
                                LatLng net = new LatLng(netPointModel.getLat(), netPointModel.getLon());
                                latLngs.add(net);
                                latLngs.add(desLatLng);
                                OverlayOptions mOverlayOptions = new PolylineOptions()
                                        .width(2)
                                        .color(Color.RED)
                                        .dottedLine(true)
                                        .points(latLngs);
                                getMapView().getMap().addOverlay(mOverlayOptions);
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
        }
    }

    private void hidemapControl() {
        // 隐藏指南针zhi
        UiSettings mUiSettings = mapView.getMap().getUiSettings();
        mUiSettings.setCompassEnabled(true);
        mUiSettings.setRotateGesturesEnabled(false);
        // 删除百度地dao图logo
        mapView.removeViewAt(1);
        mapView.removeViewAt(2);
    }

    private LatLng getNetCenter(List<NetPointModel> list) {
        if (list.size() > 0) {
            double lat = 0, lon = 0;
            for (NetPointModel netPointModel : list) {
                lat += netPointModel.getLat();
                lon += netPointModel.getLon();
            }
            lat /= list.size();
            lon /= list.size();
            return new LatLng(lat, lon);
        }
        return null;
    }
}
