package com.giserpeng.ntripshare;

import android.app.Activity;
import android.app.Application;
import android.app.Notification;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import com.baidu.mapapi.CoordType;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.model.LatLng;
import com.giserpeng.ntripshare.common.JsonUtil;
import com.giserpeng.ntripshare.ntrip.Servers.NtripCaster;
import com.giserpeng.ntripshare.ntrip.Spatial.Point;
import com.giserpeng.ntripshare.re.ReModel;
import com.giserpeng.ntripshare.ui.net.NetPointModel;
import com.giserpeng.ntripshare.ui.user.UserModel;
import com.google.gson.reflect.TypeToken;
import com.umeng.commonsdk.UMConfigure;
import com.umeng.message.IUmengRegisterCallback;
import com.umeng.message.PushAgent;
import com.umeng.message.UmengMessageHandler;
import com.umeng.message.UmengNotificationClickHandler;
import com.umeng.message.entity.UMessage;
import com.ut.device.UTDevice;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import cn.wch.ch34xuartdriver.CH34xUARTDriver;

import static android.content.ContentValues.TAG;

public class ShareApplication extends Application {

    private static final String ACTION_USB_PERMISSION = "com.giserpeng.ntripshare.USB_PERMISSION";
    public static CH34xUARTDriver driver;// 需要将CH34x的驱动类写在APP类下面，使得帮助类的生命周期与整个应用程序的生命周期是相同的
    public static int USER_TYPE = 0;
    public static int IS_PRO = 0;
    public static NtripCaster NTRIP_CASTER;
    public static ShareApplication INSTANCE;

    private Context mContext;

    public enum SOURCE_TYP {
        NTRIPV1,
        TCPIP,
        BLUETOOTH,
        OTG,
        NTRRIP_NET,
        NONE
    }

    @Override
    public void onCreate() {
        super.onCreate();
        INSTANCE = this;
        // 在使用 SDK 各组间之前初始化 context 信息，传入 ApplicationContext
        // 默认本地个性化地图初始化方法
        SDKInitializer.initialize(this);

        //自4.3.0起，百度地图SDK所有接口均支持百度坐标和国测局坐标，用此方法设置您使用的坐标类型.
        //包括BD09LL和GCJ02两种坐标，默认是BD09LL坐标。
        SDKInitializer.setCoordType(CoordType.BD09LL);
        initUmengSDK();

        ShareApplication.driver = new CH34xUARTDriver(
                (UsbManager) getSystemService(Context.USB_SERVICE), this,
                ACTION_USB_PERMISSION);
        NTRIP_CASTER = new NtripCaster();
        initActivityLifecycleCallbacks();
    }


    public void saveUser( List<UserModel> clients) {
        SharedPreferences defaultSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = defaultSharedPreferences.edit();
        editor.putString("userlist", JsonUtil.object2json(clients));
        editor.commit();
        boolean mutiUser = Boolean.valueOf(defaultSharedPreferences.getBoolean("multiuser", false));
        if (mutiUser) {
            NtripCaster.getInstance().setUserModel(clients);
        }
    }

    /**
     * 在application里监听所以activity生命周期的回调
     */
    private void initActivityLifecycleCallbacks() {
        registerActivityLifecycleCallbacks(new ActivityLifecycleCallbacks() { //添加监听
            @Override
            public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
                //activity创建生命周期
                if (activity instanceof MainActivity) { //判断创建的activity对应对象
                    mContext = activity;
                }

            }

            @Override
            public void onActivityStarted(Activity activity) {
                //activity启动生命周期

            }

            @Override
            public void onActivityResumed(Activity activity) {
                //activity恢复生命周期

            }

            @Override
            public void onActivityPaused(Activity activity) {
                //activity暂停生命周期

            }

            @Override
            public void onActivityStopped(Activity activity) {
                //activity停止生命周期

            }

            @Override
            public void onActivitySaveInstanceState(Activity activity, Bundle outState) {
                //保存activity实例状态

            }

            @Override
            public void onActivityDestroyed(Activity activity) {
                //activity销毁生命周期

            }
        });
    }

    private void initUmengSDK() {
        UMConfigure.setLogEnabled(true);
        UMConfigure.init(this, "5f9649c71c520d307397e9f2", "umeng", UMConfigure.DEVICE_TYPE_PHONE,
                "41b5221ec74fcf93a280b7cd980a5bd2");

        PushAgent mPushAgent = PushAgent.getInstance(this);
        //注册推送服务，每次调用register方法都会回调该接口
        mPushAgent.register(new IUmengRegisterCallback() {
            @Override
            public void onSuccess(String deviceToken) {
                //注册成功会返回deviceToken deviceToken是推送消息的唯一标志
                Log.i(TAG, "注册成功：deviceToken：-------->  " + deviceToken);
            }

            @Override
            public void onFailure(String s, String s1) {
                Log.e(TAG, "注册失败：-------->  " + "s:" + s + ",s1:" + s1);
            }
        });

        UmengNotificationClickHandler notificationClickHandler = new UmengNotificationClickHandler() {
            @Override
            public void launchApp(Context context, UMessage msg) {
                super.launchApp(context, msg);
                Log.i(TAG, "dealWithCustomAction：-------->  " + "s:" + msg.title + ",s1:" + msg.text);
                final AlertDialog.Builder normalDialog =
                        new AlertDialog.Builder(mContext);
                normalDialog.setIcon(R.drawable.ic_info);
                normalDialog.setTitle(msg.title);
                normalDialog.setMessage(msg.text);
                normalDialog.setPositiveButton(R.string.ok,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        });
                normalDialog.show();
            }

            @Override
            public void openUrl(Context context, UMessage msg) {
                super.openUrl(context, msg);
                Log.i(TAG, "dealWithCustomAction：-------->  " + "s:" + msg.title + ",s1:" + msg.text);
            }

            @Override
            public void openActivity(Context context, UMessage msg) {
                super.openActivity(context, msg);
                Log.i(TAG, "dealWithCustomAction：-------->  " + "s:" + msg.title + ",s1:" + msg.text);
            }

            @Override
            public void dealWithCustomAction(Context context, UMessage msg) {
//                Toast.makeText(context, msg.custom, Toast.LENGTH_LONG).show();
                Log.i(TAG, "dealWithCustomAction：-------->  " + "s:" + msg.title + ",s1:" + msg.text);
            }
        };
        mPushAgent.setNotificationClickHandler(notificationClickHandler);
    }

    public static SOURCE_TYP getSourceType(Context context) {
        SharedPreferences defaultSharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        String source = defaultSharedPreferences.getString("networkprotocol", "none");
        if (source.equalsIgnoreCase("ntripv1")) {
            return SOURCE_TYP.NTRIPV1;
        }
        if (source.equalsIgnoreCase("rawtcpip")) {
            return SOURCE_TYP.TCPIP;
        }
        if (source.equalsIgnoreCase("bluetooth")) {
            return SOURCE_TYP.BLUETOOTH;
        }
        if (source.equalsIgnoreCase("otg")) {
            return SOURCE_TYP.OTG;
        }
        if (source.equalsIgnoreCase("ntripnet")) {
            return SOURCE_TYP.NTRRIP_NET;
        }
        return SOURCE_TYP.NONE;
    }

    /*
    获取网节点
     */
    public static List<NetPointModel> getNetPointList(Context context) {
        try {
            SharedPreferences defaultSharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
            String string = defaultSharedPreferences.getString("nodelist", "");
            List<NetPointModel> userModels = JsonUtil.json2object(string, new TypeToken<List<NetPointModel>>() {
            });
            if (userModels != null) {
                return userModels;
            }
        } catch (Exception e) {

        }
        return new ArrayList<>();
    }

    /*
获取网节点
 */
    public static List<UserModel> getUserList(Context context) {
        try {
            SharedPreferences defaultSharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
            String string = defaultSharedPreferences.getString("userlist", "");
            List<UserModel> userModels = JsonUtil.json2object(string, new TypeToken<List<UserModel>>() {
            });
            if (userModels != null) {
                return userModels;
            }
        } catch (Exception e) {

        }
        return new ArrayList<>();
    }

    public static int getMaxDis(Context context) {
        try {
            SharedPreferences defaultSharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
            int num = Integer.parseInt(defaultSharedPreferences.getString("maxDis", "0")) * 1000;
            return num;
        } catch (Exception e) {

        }
        return 0;
    }

    public static int getBaudRate(Context context) {
        try {
            SharedPreferences defaultSharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
            int num = Integer.parseInt(defaultSharedPreferences.getString("pBaudRate", "0"));
            return num;
        } catch (Exception e) {

        }
        return 0;
    }

    public static int getSelServer(Context context) {
        try {
            SharedPreferences defaultSharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
            int num = Integer.parseInt(defaultSharedPreferences.getString("sel_server", "0"));
            return num;
        } catch (Exception e) {

        }
        return 0;
    }

    public static boolean isShowInfo(Context context) {
        SharedPreferences defaultSharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        boolean showInfo = Boolean.valueOf(defaultSharedPreferences.getBoolean("showinfo", true));
        return showInfo;
    }

    public static boolean isRequireGGA(Context context) {
        SharedPreferences defaultSharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        boolean showInfo = Boolean.valueOf(defaultSharedPreferences.getBoolean("requireGGA", true));
        return showInfo;
    }

    public static int getGGATime(Context context) {
        try {
            SharedPreferences defaultSharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
            int num = Integer.parseInt(defaultSharedPreferences.getString("ggaTime", "0")) * 1000;
            return num ;
        } catch (Exception e) {

        }
        return 60 * 1000;
    }

    public static int updateLastLocation(Context context, double longitude, double latitude) {
        try {
            SharedPreferences defaultSharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
            SharedPreferences.Editor editor = defaultSharedPreferences.edit();
            editor.putString("lastLongitude", String.valueOf(longitude));
            editor.putString("lastLatitude", String.valueOf(latitude));
            editor.commit();
        } catch (Exception e) {

        }
        return 60 * 1000;
    }

    public static void updateBluetoothMac(Context context, String mac) {
        try {
            SharedPreferences defaultSharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
            SharedPreferences.Editor editor = defaultSharedPreferences.edit();
            editor.putString("bluetooth_mac", mac);
            editor.commit();
        } catch (Exception e) {

        }
    }


    public static void updateUserNum(Context context, int num) {
        try {
            SharedPreferences defaultSharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
            SharedPreferences.Editor editor = defaultSharedPreferences.edit();
            editor.putString("usernum",String.valueOf(num) );
            editor.commit();
        } catch (Exception e) {

        }
    }

    public static String getBluetoothMac(Context context) {
        SharedPreferences defaultSharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        String ResistCode = defaultSharedPreferences.getString("bluetooth_mac", "");
        return ResistCode;
    }

    public static String getShareMountpoint(Context context) {
        SharedPreferences defaultSharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        String mountPoint = defaultSharedPreferences.getString("sharemountpoint", "");
        return mountPoint;
    }


    public static String getYuMing(Context context) {
        SharedPreferences defaultSharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        String YuMing = defaultSharedPreferences.getString("YuMing", "");
        return YuMing;
    }

    public static void updateYuMing(Context context, String YuMing) {
        try {
            SharedPreferences defaultSharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
            SharedPreferences.Editor editor = defaultSharedPreferences.edit();
            editor.putString("YuMing",String.valueOf(YuMing) );
            editor.commit();
        } catch (Exception e) {

        }
    }


    public static List<Double> getLastPosition(Context context) {
        SharedPreferences defaultSharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        double longitude = Double.parseDouble(defaultSharedPreferences.getString("lastLongitude", "0"));
        double latitude = Double.parseDouble(defaultSharedPreferences.getString("lastLatitude", "0"));
        List<Double> coord = new ArrayList<>();
        coord.add(longitude);
        coord.add(latitude);
        return coord;
    }

    public static String getServerIP(Context context) {
        SharedPreferences defaultSharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        String ResistCode = defaultSharedPreferences.getString("ResistCode", "");
        ReModel reModel = new ReModel(ResistCode);
        return reModel.getServerIp();
    }

    public static boolean isOtgOutPut(Context context) {
        SharedPreferences defaultSharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        boolean otgOut = Boolean.valueOf(defaultSharedPreferences.getBoolean("otgOut", true));
        return otgOut;
    }

    public static boolean isBluetoothOutPut(Context context) {
        SharedPreferences defaultSharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        boolean otgOut = Boolean.valueOf(defaultSharedPreferences.getBoolean("bluetoothOut", true));
        return otgOut;
    }

    public static boolean isMultiUser(Context context) {
        SharedPreferences defaultSharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        boolean otgOut = Boolean.valueOf(defaultSharedPreferences.getBoolean("multiuser", true));
        return otgOut;
    }


}