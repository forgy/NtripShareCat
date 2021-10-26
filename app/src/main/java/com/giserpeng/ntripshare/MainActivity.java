package com.giserpeng.ntripshare;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.navigation.NavController;
import androidx.navigation.NavDestination;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.baidu.location.BDAbstractLocationListener;
import com.baidu.location.BDLocation;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.model.LatLng;
import com.giserpeng.ntripshare.ntrip.Clients.Client;
import com.giserpeng.ntripshare.ntrip.Models.MountPointModel;
import com.giserpeng.ntripshare.ntrip.NTRIPService;
import com.giserpeng.ntripshare.ntrip.Servers.ICasterEvent;
import com.giserpeng.ntripshare.ntrip.Servers.NtripCaster;
import com.giserpeng.ntripshare.ntrip.Servers.ReferenceStation;
import com.giserpeng.ntripshare.otg.UsbService;
import com.giserpeng.ntripshare.re.ReModel;
import com.giserpeng.ntripshare.ui.About.AboutFragment;
import com.giserpeng.ntripshare.ui.device.DeviceFragment;
import com.giserpeng.ntripshare.ui.home.AddFeedBackDialog;
import com.giserpeng.ntripshare.ui.home.HomeFragment;
import com.giserpeng.ntripshare.ui.log.LogFragment;
import com.giserpeng.ntripshare.ui.pro.ProDialog;
import com.giserpeng.ntripshare.ui.setting.GetBluetoothDialog;
import com.giserpeng.ntripshare.ui.setting.SettingFragment;
import com.giserpeng.ntripshare.update.UpdateManager;
import com.giserpeng.ntripshare.util.DateUtils;
import com.giserpeng.ntripshare.util.EncryptUtil;
import com.giserpeng.ntripshare.util.NetUtils;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.inuker.bluetooth.library.search.SearchResult;
import com.umeng.analytics.MobclickAgent;
import com.umeng.message.PushAgent;
import com.ut.device.UTDevice;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import okhttp3.Response;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, BottomNavigationView.OnNavigationItemSelectedListener {
    public static String LOG = "";
    public static String CONNECT_LOG = "";
    private AppBarConfiguration mAppBarConfiguration;
    private Messenger outMessenger = null;
    final Messenger inMessenger = new Messenger(new IncomingHandler(this));
    boolean mIsBound;
    FloatingActionButton btnService;
    LogFragment logFragment;
    double CurrentLatitude = 0.0d;
    double CurrentLongitude = 0.0d;

    TextView connectionNumTextView;
    ProgressBar progressBar;
    TextView tcConnectSource;
    TextView tvConnectServer;
    boolean isUsbConnect = false;
    boolean isUsbStart = false;
    boolean isSetCoord = false;

    public double getCurrentLatitude() {
        return CurrentLatitude;
    }

    public double getCurrentLongitude() {
        return CurrentLongitude;
    }

    private Timer updateTimer;

    private ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            MainActivity.this.outMessenger = new Messenger(iBinder);
            try {
                Message obtain = Message.obtain(null, 1);
                obtain.replyTo = MainActivity.this.inMessenger;
                MainActivity.this.outMessenger.send(obtain);
                MainActivity.this.outMessenger.send(Message.obtain(null, 3, 0, 0));
                MainActivity.this.outMessenger.send(Message.obtain(null, 7, 0, 0));
            } catch (RemoteException e) {
            }
        }

        public void onServiceDisconnected(ComponentName componentName) {
            MainActivity.this.outMessenger = null;
        }
    };

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        item.setChecked(true);
        switch (item.getItemId()) {
            case R.id.navigation_home:
                if (navController.getCurrentDestination().getId() != R.id.nav_home) {
                    navController.navigate(R.id.nav_home);
                }
                btnService.setVisibility(View.VISIBLE);
                break;
            case R.id.navigation_device:
                if (navController.getCurrentDestination().getId() != R.id.nav_dash) {
                    navController.navigate(R.id.nav_dash);
                }
                btnService.setVisibility(View.VISIBLE);
                break;
            case R.id.navigation_log:
                if (navController.getCurrentDestination().getId() != R.id.nav_log) {
                    navController.navigate(R.id.nav_log);
                }
                btnService.setVisibility(View.VISIBLE);
                break;
        }
        return false;
    }


    private boolean checkSettings() {
        SharedPreferences defaultSharedPreferences = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        if (ShareApplication.getSourceType(this) == ShareApplication.SOURCE_TYP.BLUETOOTH) {
            if (defaultSharedPreferences.getString("bluetooth_mac", "00.00.00.00:00:00").equalsIgnoreCase("00.00.00.00:00:00")) {
                return false;
            }
        } else if (ShareApplication.getSourceType(this) == ShareApplication.SOURCE_TYP.TCPIP) {
            if (defaultSharedPreferences.getString("ntripcasterip", "").equalsIgnoreCase("")
                    || Integer.parseInt(defaultSharedPreferences.getString("ntripcasterport", "0")) == 0) {
                return false;
            }
        } else if (ShareApplication.getSourceType(this) == ShareApplication.SOURCE_TYP.NTRIPV1) {
            if (defaultSharedPreferences.getString("ntripcasterip", "").equalsIgnoreCase("")    ||
                    defaultSharedPreferences.getString("ntripcasterip", "").contains("000")    ||
            defaultSharedPreferences.getString("ntripcasterip", "").equalsIgnoreCase("000.000.00.000")
                    || Integer.parseInt(defaultSharedPreferences.getString("ntripcasterport", "0")) == 0
                    || defaultSharedPreferences.getString("ntripsourcetable", "").equalsIgnoreCase("")
                    || defaultSharedPreferences.getString("ntrippassword", "").equalsIgnoreCase("")
                    || defaultSharedPreferences.getString("networkprotocol", "").equalsIgnoreCase("")) {
                return false;
            }
        }
        return true;
    }

    private HashMap<String, MountPointModel> createMountPointModel(String name) {
        MountPointModel mountPointModel = new MountPointModel();
        mountPointModel.setMountpoint(name);
        mountPointModel.setIdentifier(name);
        mountPointModel.setFormat("RTCM 3.2");
        mountPointModel.setFormatDetails("1074(1),1084(1),1124(1),1005(5),1007(5),1033(5)");
        mountPointModel.setCarrier(2);
        mountPointModel.setNavSystem("GNSS");
        mountPointModel.setNetwork("EagleGnss");
        mountPointModel.setCountry("CHN");
        mountPointModel.setLatitude(0.0);
        mountPointModel.setLongitude(0.0);
        mountPointModel.setNmea(true);
        mountPointModel.setSolution(true);
        mountPointModel.setGenerator("NRS1.180703");
        mountPointModel.setCompression("none");
        mountPointModel.setAuthenticator("Basic");
        mountPointModel.setFee(false);
        mountPointModel.setBitrate(19200);
        mountPointModel.setMisc("");
        mountPointModel.initStationPool();
        HashMap<String, MountPointModel> mountPointModelHashMap = new HashMap<>();
        mountPointModelHashMap.put(name, mountPointModel);
        return mountPointModelHashMap;
    }

    private static class IncomingHandler extends Handler {
        private final WeakReference<MainActivity> mTarget;

        IncomingHandler(MainActivity mainActivity) {
            this.mTarget = new WeakReference(mainActivity);
        }

        public void handleMessage(Message message) {
            super.handleMessage(message);
            MainActivity mainActivity = (MainActivity) this.mTarget.get();
            if (mainActivity != null) {
                int i = message.what;
                if (i == NTRIPService.MSG_THREAD_SUICIDE) {
                    mainActivity.doUnbindService();
                } else if (i == NTRIPService.MSG_CONNECT_SOURCE_OK) {
                    if (mainActivity.mIsBound) {
                        mainActivity.tcConnectSource.setText(mainActivity.getString(R.string.data_source_connected));
                    }
                    SharedPreferences defaultSharedPreferences = PreferenceManager.getDefaultSharedPreferences(mainActivity);
                    boolean showInfo = Boolean.valueOf(defaultSharedPreferences.getBoolean("showinfo", true));
                    if (showInfo) {
                        mainActivity.showShareInfo();
                    }

                } else if (i == NTRIPService.MSG_CONNECT_NODE_SOURCE_OK) {
                    if (mainActivity.mIsBound) {
                        mainActivity.tcConnectSource.setText(R.string.data_source_connected);
                    }
//                    SharedPreferences defaultSharedPreferences = PreferenceManager.getDefaultSharedPreferences(mainActivity);
//                    boolean showInfo = Boolean.valueOf(defaultSharedPreferences.getBoolean("showinfo", true));
//                    if (showInfo) {
//                        mainActivity.showShareInfo();
//                    }
                } else if (i == NTRIPService.MSG_CONNECT_BLUETOOTH_OK) {
                    Toast.makeText(mainActivity, mainActivity.getString(R.string.bluetooth_connected), Toast.LENGTH_LONG).show();
                } else if (i == NTRIPService.MSG_CONNECT_SOURCE_FAIL) {
                    Bundle data = message.getData();
                    Toast.makeText(mainActivity, mainActivity.getString(R.string.data_source_reconnect), Toast.LENGTH_LONG).show();
//                    mainActivity.doUnbindService();
                } else if (i == NTRIPService.MSG_CONNECT_PROXY_OK) {
                    if (mainActivity.mIsBound) {
                        mainActivity.tvConnectServer.setText(mainActivity.getString(R.string.server_connected));
                    }
                } else if (i == NTRIPService.MSG_CONNECT_PROXY_FAIL) {
                    Bundle data = message.getData();
                    if (mainActivity.mIsBound) {
                        mainActivity.tvConnectServer.setText(mainActivity.getString(R.string.server_disconnect));
                    }
                    Toast.makeText(mainActivity, mainActivity.getString(R.string.server_reconnect), Toast.LENGTH_LONG).show();
//                    mainActivity.doUnbindService();
                } else if (i == NTRIPService.MSG_UPDATE_BYTES_IN) {
                    i = message.arg1;
                    if (i > 0) {
                        if (mainActivity.mIsBound) {
                            mainActivity.progressBar.setVisibility(View.VISIBLE);
                            mainActivity.progressBar.setProgress(i % 4096);
                        }
                        if (mainActivity.mIsBound) {
                            mainActivity.tcConnectSource.setText(mainActivity.getString(R.string.data_source_connected));
                        }
                    }
                } else if (i == NTRIPService.MSG_UPDATE_LOG_APPEND) {
                    mainActivity.LogConnectMessage(message.getData().getString("logappend"));
                } else if (i == 7) {
//                        mainActivity.textLog_setText(message.getData().getString("logfull"));
                } else if (i == NTRIPService.MSG_REQUEST_LOCATION) {
                    mainActivity.RequestPermissionLocation();
                } else if (i == NTRIPService.MSG_UPDATE_LOCATION) {
//                    Bundle data = message.getData();
//                    mainActivity.CurrentLatitude = data.getDouble("Lat");
//                    mainActivity.CurrentLongitude = data.getDouble("Lon");
//                    MyLocationData locData = new MyLocationData.Builder()
//                            .accuracy(0).latitude(mainActivity.CurrentLatitude)
//                            .longitude(mainActivity.CurrentLongitude).build();
//                    if (HomeFragment.INSTANCE != null &&
//                            ShareApplication.getSourceType(mainActivity) != ShareApplication.SOURCE_TYP.BLUETOOTH &&
//                            ShareApplication.getSourceType(mainActivity) != ShareApplication.SOURCE_TYP.OTG &&
//                            ShareApplication.getSourceType(mainActivity) != ShareApplication.SOURCE_TYP.NTRRIP_NET
//                    ) {
//                        HomeFragment.INSTANCE.setLocation(locData);
//                    }

//                    Log.i("CurrentLatitude", mainActivity.CurrentLatitude + "-" + mainActivity.CurrentLongitude);
                }
            }
        }
    }

    private void RequestPermissionLocation() {
        ActivityCompat.requestPermissions(this, new String[]{"android.permission.ACCESS_FINE_LOCATION"}, NTRIPService.MSG_REQUEST_LOCATION);
    }

    NavController navController;
    private HomeFragment homeFragment;
    private DeviceFragment deviceFragment;
    private SettingFragment settingFragment;
    private AboutFragment aboutFragment;
    private BottomNavigationView bottomNavigationView;
    NavigationView navigationView;
    private String updatePath = "";

    @SuppressLint("InvalidWakeLockTag")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        requestPermission();
        homeFragment = new HomeFragment();
        logFragment = new LogFragment();
        deviceFragment = new DeviceFragment();
        aboutFragment = new AboutFragment();
        settingFragment = new SettingFragment();
        bottomNavigationView = findViewById(R.id.nav_view_bottom);
        bottomNavigationView.setOnNavigationItemSelectedListener(this);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        this.btnService = findViewById(R.id.fab);
        this.progressBar = findViewById(R.id.progressBar);
        this.progressBar.setMax(4096);
        this.tcConnectSource = findViewById(R.id.tv_source);
        this.tvConnectServer = findViewById(R.id.tv_server);
        this.connectionNumTextView = findViewById(R.id.tcConnectNum);
        this.btnService.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MainActivity.this.ListenerBtnService();
            }
        });
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home, R.id.nav_dash, R.id.nav_log, R.id.nav_user, R.id.nav_net, R.id.nav_setting, R.id.nav_about)
                .setDrawerLayout(drawer)
                .build();
        navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);
        Menu menu = navigationView.getMenu();
        menu.findItem(R.id.nav_update).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                UpdateManager manager = new UpdateManager(MainActivity.this);
                manager.setUpdateHandler(new UpdateManager.UpdateHandler() {
                    @Override
                    public void onfindUpdate(String re) {
                        try {
                            JSONObject jsonObject = new JSONObject(re);
                            if (null != jsonObject) {
                                double serviceCode = jsonObject.getInt("version");
                                updatePath = jsonObject.getString("url");
                                // 版本判断
                                if (serviceCode > UpdateManager.getVersionCode(MainActivity.this)) {
                                    UpdateManager.showNoticeDialog(MainActivity.this, updatePath);
                                    return;
                                }
                            }
                        } catch (JSONException e) {
                            Toast.makeText(MainActivity.this, MainActivity.this.getString(R.string.connect_server_fail), Toast.LENGTH_LONG).show();
                            return;
                        }
                        Toast.makeText(MainActivity.this, R.string.soft_update_no, Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void onfindUpdateFail(String msg) {
                        Toast.makeText(MainActivity.this, MainActivity.this.getString(R.string.connect_server_fail), Toast.LENGTH_LONG).show();
                    }
                });
                // 检查软件更新
                manager.checkUpdate();
                return false;
            }
        });

        menu.findItem(R.id.nav_pro).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                if (ShareApplication.USER_TYPE == 1) {
                    Toast.makeText(MainActivity.this, "当前已经是高级版本！", Toast.LENGTH_LONG).show();
                } else {
                    showProDialog();
                }
                return false;
            }
        });
        menu.findItem(R.id.nav_doc).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                Thread thread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        Uri uri = Uri.parse(getString(R.string.URL_DOC));
                        try {
                            Response response = NetUtils.get(getApplication().getString(R.string.TRIAL_URL_DOC));
                            JSONObject jsonObject1 = new JSONObject(response.body().string());
                            if (jsonObject1.getString("data") != null && !jsonObject1.getString("data").equalsIgnoreCase("")) {
                                uri = Uri.parse(jsonObject1.getString("data"));
                                Log.i("nav_doc", jsonObject1.getString("data"));
                            }
                        } catch (Exception e) {
                            Log.i("nav_doc", e.getMessage());
                        }
                        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                startActivity(intent);
                            }
                        });
                    }
                });
                thread.start();
                return false;
            }
        });

        menu.findItem(R.id.nav_feedback).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                showFeedbackDialog();
                return false;
            }
        });



        navController.addOnDestinationChangedListener(new NavController.OnDestinationChangedListener() {
            @Override
            public void onDestinationChanged(@NonNull NavController controller, @NonNull NavDestination destination, @Nullable Bundle arguments) {
                updateNavi();
                if (destination.getId() == R.id.nav_about || destination.getId() == R.id.nav_setting || destination.getId() == R.id.nav_user || destination.getId() == R.id.nav_net) {
                    btnService.setVisibility(View.GONE);
                } else {
                    if (destination.getId() == R.id.nav_log) {
                        bottomNavigationView.setSelectedItemId(R.id.navigation_log);
                    }
                    if (destination.getId() == R.id.nav_dash) {
                        bottomNavigationView.setSelectedItemId(R.id.navigation_device);
                    }
                    if (destination.getId() == R.id.nav_home) {
                        bottomNavigationView.setSelectedItemId(R.id.navigation_home);
                    }
                    btnService.setVisibility(View.VISIBLE);
                }
            }
        });

        CheckIfServiceIsRunning();
        updateTimer = new Timer();
        startLocation();

        mHandler = new UsbHandler(this);
        startService(UsbService.class, usbConnection, null); // Start UsbService(if it was not started before) and Bind it
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);// 保持常亮的屏幕的状态
        startCasterServer();

        PushAgent.getInstance(this).onAppStart();
        updateNavi();
    }

    public  void updateNavi(){
        if (ShareApplication.USER_TYPE == 1) {
            if (ShareApplication.isMultiUser(this)) {
                navigationView.getMenu().findItem(R.id.nav_user).setVisible(true);
            } else {
                navigationView.getMenu().findItem(R.id.nav_user).setVisible(false);
            }
            if (ShareApplication.getSourceType(this) == ShareApplication.SOURCE_TYP.NTRRIP_NET) {
                navigationView.getMenu().findItem(R.id.nav_net).setVisible(true);
            } else {
                navigationView.getMenu().findItem(R.id.nav_net).setVisible(false);
            }
        } else {
            navigationView.getMenu().findItem(R.id.nav_net).setVisible(false);
            navigationView.getMenu().findItem(R.id.nav_user).setVisible(false);
        }
    }

    public  void updateNavi(boolean showUser,boolean showNet){
        if (ShareApplication.USER_TYPE == 1) {
                navigationView.getMenu().findItem(R.id.nav_user).setVisible(showUser);
                navigationView.getMenu().findItem(R.id.nav_net).setVisible(showNet);
        } else {
            navigationView.getMenu().findItem(R.id.nav_net).setVisible(false);
            navigationView.getMenu().findItem(R.id.nav_user).setVisible(false);
        }
    }

    private void startCasterServer() {
        if (NtripCaster.getInstance() != null) {
            NtripCaster.getInstance().setMountPoint(ShareApplication.getShareMountpoint(this));
            NtripCaster.getInstance().setCasterEvent(new ICasterEvent() {
                @Override
                public void onMessage(String msg) {
                    LogConnectMessage(msg);
                }

                @Override
                public void onUserMessage(String msg) {
                    LogMessage(msg);
                }

                @Override
                public void onClientAuthenticationFail(Client client) {
                    MainActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            MainActivity.this.tvConnectServer.setText(MainActivity.this.getString(R.string.server_connected));
                            Toast.makeText(MainActivity.this, MainActivity.this.getString(R.string.user_login_fail), Toast.LENGTH_LONG).show();
                        }
                    });
                }

                @Override
                public void onClientAuthenticationSuccess(Client client) {
                    MainActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            MainActivity.this.tvConnectServer.setText(MainActivity.this.getString(R.string.server_connected));
                            Toast.makeText(MainActivity.this, getApplicationContext().getText(R.string.user_login), Toast.LENGTH_LONG).show();
                        }
                    });
                }
            });
        }
    }

    TimerTask task = new TimerTask() {
        @Override
        public void run() {
            updateMap();
        }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        MenuItem menuItem = menu.findItem(R.id.action_ble_output);
        if (ShareApplication.USER_TYPE != 1) {
            if (menuItem != null) {
                menuItem.setVisible(false);
            }
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_register:
                regist();
                break;
            case R.id.action_share:
                showShareInfo();
                break;
            case R.id.action_ble_output:
                bluetoothConnection();
                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
    }

    private void bluetoothConnection() {
        if (!this.mIsBound) {
            Toast.makeText(this, R.string.start_share_service, Toast.LENGTH_LONG).show();
            return;
        }
        if (ShareApplication.getSourceType(this) == ShareApplication.SOURCE_TYP.BLUETOOTH) {
            Toast.makeText(this, R.string.switch_source, Toast.LENGTH_LONG).show();
            return;
        }
        if (!ShareApplication.isBluetoothOutPut(this)) {
            Toast.makeText(this, R.string.turnon_bluetooth_output, Toast.LENGTH_LONG).show();
            return;
        }
        final GetBluetoothDialog dialog = new GetBluetoothDialog(this);
        dialog.setOnClickBottomListener(new GetBluetoothDialog.OnClickBottomListener() {
            @Override
            public void onSelectedClick(SearchResult bluetoothDevice) {
                ShareApplication.updateBluetoothMac(MainActivity.this, bluetoothDevice.getAddress());
                try {
                    MainActivity.this.outMessenger.send(Message.obtain(null, NTRIPService.MSG_CONNECT_BLUETOOTH_OUTPUT, 0, 0));
                } catch (Exception e) {

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

    private void regist() {
        final EditText editText = new EditText(this);
        AlertDialog.Builder inputDialog =
                new AlertDialog.Builder(this);
        inputDialog.setTitle(getString(R.string.register)).setView(editText);
        inputDialog.setNegativeButton(getString(R.string.trya), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                trial();
            }
        });
        inputDialog.setPositiveButton(getString(R.string.ok),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText(MainActivity.this,
                                editText.getText().toString(),
                                Toast.LENGTH_SHORT).show();
                        String code = editText.getText().toString();
                        re(code);
                    }
                }).show();
    }

    private void showProDialog() {
        ProDialog dialog = new ProDialog(this);
        dialog.setOnClickBottomListener(new ProDialog.OnClickBottomListener() {
            @Override
            public void onPositiveClick() {
                if (ShareApplication.USER_TYPE == 1) {
                    Toast.makeText(getApplicationContext(), R.string.is_pro, Toast.LENGTH_LONG).show();
                } else {
                    trialPro();
                }
            }

            @Override
            public void onNegtiveClick() {
                Toast.makeText(MainActivity.this,
                        R.string.contact_me,
                        Toast.LENGTH_SHORT).show();
            }
        });
        dialog.show();
//
//        AlertDialog.Builder inputDialog =
//                new AlertDialog.Builder(this);
//        inputDialog.setTitle(getString(R.string.pro_name)).setMessage(R.string.pro_des);
//        inputDialog.setNegativeButton(R.string.trya, new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialog, int which) {
//                if (ShareApplication.USER_TYPE == 1) {
//                    Toast.makeText(getApplicationContext(), R.string.is_pro, Toast.LENGTH_LONG).show();
//                } else {
//                    trialPro();
//                }
//            }
//        });
//        inputDialog.setPositiveButton(R.string.buy,
//                new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialog, int which) {
//                        Toast.makeText(MainActivity.this,
//                                R.string.contact_me,
//                                Toast.LENGTH_SHORT).show();
//                    }
//                }).show();
    }

    private void showFeedbackDialog() {
        AddFeedBackDialog dialog = new AddFeedBackDialog(this);
        dialog.setOnClickBottomListener(new AddFeedBackDialog.OnClickBottomListener() {
            @Override
            public void onPositiveClick() {
                Thread thread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Response response = NetUtils.get(getApplication().getString(R.string.URL_FEEDBACK) + "?des=" + dialog.getDes() + "&weixin=" + dialog.getWexin());
                            JSONObject jsonObject1 = new JSONObject(response.body().string());
                            Log.i("checkUpdate", jsonObject1.toString());
                            if (jsonObject1.getBoolean("success")) {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(getApplicationContext(), R.string.feedback_success, Toast.LENGTH_LONG).show();
                                        dialog.dismiss();
                                    }
                                });
                            } else {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(getApplicationContext(), R.string.connect_server_fail, Toast.LENGTH_LONG).show();
                                        dialog.dismiss();
                                    }
                                });
                            }
                        } catch (Exception e) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(getApplicationContext(), R.string.connect_server_fail, Toast.LENGTH_LONG).show();
                                    dialog.dismiss();
                                }
                            });
                        }
                    }
                });
                thread.start();
            }

            @Override
            public void onNegtiveClick() {
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    public void trial() {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    String data = getString(R.string.platform_name) + "-" + DateUtils.dateToString(new Date(), "yyyyMMddHHmmss") + "-" + UTDevice.getUtdid(MainActivity.this);
                    Response response = NetUtils.get(getString(R.string.TRIAL_SERVER_URL) + "?deviceCode=" + EncryptUtil.encrypt(data));
                    JSONObject jsonObject1 = new JSONObject(response.body().string());
                    if (jsonObject1.getBoolean("success")) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    re(jsonObject1.getString("data"));
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        });
                    } else {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    Toast.makeText(MainActivity.this, getString(R.string.try_fail) + jsonObject1.getString("message"), Toast.LENGTH_LONG).show();
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        });
                    }
                } catch (IOException | JSONException e) {
                    e.printStackTrace();
                }
            }
        });
        thread.start();
    }

    public void trialPro() {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    String data = "V" + UpdateManager.getVersionName(MainActivity.this) + "-" + DateUtils.dateToString(new Date(), "yyyyMMddHHmmss") + "-" + UTDevice.getUtdid(MainActivity.this);
                    Log.i("trialPro", EncryptUtil.encrypt(data));
                    Response response = NetUtils.get(getString(R.string.TRIAL_PRO_SERVER_URL) + "?deviceCode=" + EncryptUtil.encrypt(data));
                    JSONObject jsonObject1 = new JSONObject(response.body().string());
                    if (jsonObject1.getBoolean("success")) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(MainActivity.this, R.string.try_ok, Toast.LENGTH_LONG).show();
                            }
                        });
                    } else {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    Toast.makeText(MainActivity.this, getString(R.string.try_fail) + jsonObject1.getString("message"), Toast.LENGTH_LONG).show();
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        });
                    }
                } catch (IOException | JSONException e) {
                    e.printStackTrace();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(MainActivity.this, R.string.connect_server_fail, Toast.LENGTH_LONG).show();
                        }
                    });
                }
            }
        });
        thread.start();
    }

    public void re(String key) {
        try {
            if (ReModel.CheckCode(key)) {
                SharedPreferences defaultSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
                SharedPreferences.Editor editor = defaultSharedPreferences.edit();
                editor.putString(getString(R.string.ResistCode), key);
                editor.commit();
                updateServiceSetting();

            } else {
                Toast.makeText(this, R.string.connect_server_fail, Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            Toast.makeText(this, R.string.connect_server_fail, Toast.LENGTH_LONG).show();
        }
    }

    private void showShareInfo() {
        if (ShareApplication.getSourceType(this) == ShareApplication.SOURCE_TYP.OTG) {
            if (!isUsbConnect) {
                new AlertDialog.Builder(this)
                        .setTitle(R.string.tips)
                        .setIcon(R.drawable.ic_info)
                        .setMessage(R.string.start_share_service)
                        .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        }).create().show();
                return;
            }
        } else {
            if (!mIsBound) {
                new AlertDialog.Builder(this)
                        .setTitle(R.string.tips)
                        .setIcon(R.drawable.ic_info)
                        .setMessage(R.string.start_share_service)
                        .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        }).create().show();
                return;
            }
        }

        SharedPreferences defaultSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        String ResistCode = defaultSharedPreferences.getString("ResistCode", "");
        ReModel reModel = new ReModel(ResistCode);
        String yuMing = ShareApplication.getYuMing(this);
        String userName = defaultSharedPreferences.getString("shareusername", "");
        String pass = defaultSharedPreferences.getString("sharepassword", "");

        String mes = getString(R.string.server) + "：" + reModel.getServerIp() + "\n" +
                getString(R.string.port) + "：" + reModel.getCasterPort() + "\n" +
                getString(R.string.mountpoint) + "：" + NtripCaster.getInstance().getMountPointName() + "\n";
        boolean mutiUser = Boolean.valueOf(defaultSharedPreferences.getBoolean("multiuser", false));
        if (!mutiUser) {
            mes += getString(R.string.username) + "：" + userName + "\n" +
                    getString(R.string.password) + "：" + pass + "\n";
        }
        mes += getString(R.string.fail_info);
        mes = getString(R.string.YuMing) + "：" + yuMing+ "\n" +mes;
        final String mg = mes;
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setIcon(R.drawable.ic_info);
        builder.setTitle(R.string.share_info);
        builder.setMessage(mes);
        //设置确定按钮
        builder.setNegativeButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        //设置取消按钮
        builder.setPositiveButton(R.string.copy, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData mClipData = ClipData.newPlainText("Label", mg);
                cm.setPrimaryClip(mClipData);
                MainActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(MainActivity.this, R.string.copy_success, Toast.LENGTH_LONG).show();
                    }
                });
            }
        });
        //显示提示框
        builder.show();
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }


    private void CheckIfServiceIsRunning() {
        if (NTRIPService.isRunning()) {
            doBindService();
            return;
        }
        this.btnService.setTag(getString(R.string.connect));
        this.btnService.setImageDrawable(getDrawable(android.R.drawable.ic_media_play));

        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        if (notificationManager != null) {
            notificationManager.cancelAll();
        }
    }

    void ListenerBtnService() {
        if (this.btnService.getTag().toString().equals(getString(R.string.connect))) {
            SharedPreferences defaultSharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            String recode = defaultSharedPreferences.getString(getString(R.string.ResistCode), "");
            if (!ReModel.CheckCode(recode)) {
                Toast.makeText(this, R.string.start_fail, Toast.LENGTH_LONG).show();
                return;
            }
            if (ShareApplication.getSourceType(this) == ShareApplication.SOURCE_TYP.OTG) {
                if (!isUsbConnect) {
                    Toast.makeText(this, R.string.usb_disconnect, Toast.LENGTH_LONG).show();
                    return;
                } else {
                    usbService.changeBaudRate(ShareApplication.getBaudRate(this));
                    this.btnService.setTag(getString(R.string.connecting));
                    this.btnService.setImageDrawable(getDrawable(android.R.drawable.ic_media_pause));
                    isUsbStart = true;
                    tcConnectSource.setText(R.string.data_source_connected);
                    if (Build.VERSION.SDK_INT >= 26) {
                        startForegroundService(new Intent(this, NTRIPService.class));
                    } else {
                        startService(new Intent(this, NTRIPService.class));
                    }
                    doBindService();
                }
            } else if (ShareApplication.getSourceType(this) == ShareApplication.SOURCE_TYP.NTRRIP_NET) {
                LogConnectMessage(getString(R.string.starting_service));
                if (Build.VERSION.SDK_INT >= 26) {
                    startForegroundService(new Intent(this, NTRIPService.class));
                } else {
                    startService(new Intent(this, NTRIPService.class));
                }
                doBindService();
                return;
            } else {
                if (!checkSettings()) {
                    new AlertDialog.Builder(this)
                            .setTitle(R.string.tips)
                            .setIcon(R.drawable.ic_setting2)
                            .setMessage(R.string.setting_first)
                            .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    getSupportFragmentManager().beginTransaction().replace(R.id.nav_host_fragment, settingFragment).commitNow();
                                }
                            }).create().show();
                    return;
                }

                LogConnectMessage(getString(R.string.starting_service));
                if (Build.VERSION.SDK_INT >= 26) {
                    startForegroundService(new Intent(this, NTRIPService.class));
                } else {
                    startService(new Intent(this, NTRIPService.class));
                }
                doBindService();
                return;
            }
        } else {
            if (ShareApplication.getSourceType(this) == ShareApplication.SOURCE_TYP.OTG) {
                this.btnService.setTag(getString(R.string.disconnect));
                this.btnService.setImageDrawable(getDrawable(android.R.drawable.ic_media_play));
                isUsbStart = false;
                doUnbindService();
                stopService(new Intent(this, NTRIPService.class));
                LogConnectMessage(getString(R.string.service_stopped));
            } else {
                doUnbindService();
                stopService(new Intent(this, NTRIPService.class));
                LogConnectMessage(getString(R.string.service_stopped));
            }
        }

    }

    private void LogMessage(String str) {
        if (MainActivity.LOG.length() > 4000) {
            String charSequence = MainActivity.LOG;
            MainActivity.LOG = (charSequence.substring(charSequence.indexOf("\n", charSequence.length() + NotificationManagerCompat.IMPORTANCE_UNSPECIFIED) + 1));
        }
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(MainActivity.LOG);
        stringBuilder.append("\n");
        stringBuilder.append(NTRIPService.TheTimeIs() + str);
        MainActivity.LOG = stringBuilder.toString();
    }

    private void LogConnectMessage(String str) {
        if (MainActivity.CONNECT_LOG.length() > 4000) {
            String charSequence = MainActivity.LOG;
            MainActivity.CONNECT_LOG = (charSequence.substring(charSequence.indexOf("\n", charSequence.length() + NotificationManagerCompat.IMPORTANCE_UNSPECIFIED) + 1));
        }
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(MainActivity.CONNECT_LOG);
        stringBuilder.append("\n");
        stringBuilder.append(NTRIPService.TheTimeIs() + str);
        MainActivity.CONNECT_LOG = stringBuilder.toString();
    }

    void doBindService() {
        bindService(new Intent(this, NTRIPService.class), this.mConnection, BIND_AUTO_CREATE);
        this.btnService.setTag(getString(R.string.connecting));
        this.btnService.setImageDrawable(getDrawable(android.R.drawable.ic_media_pause));
        this.mIsBound = true;
        if (this.outMessenger != null) {
            try {
                Message obtain = Message.obtain(null, 3, 0, 0);
                obtain.replyTo = this.inMessenger;
                this.outMessenger.send(obtain);
                this.outMessenger.send(Message.obtain(null, 7, 0, 0));
            } catch (RemoteException e) {
            }
        }
        startTimer();

    }

    public void updateServiceSetting() {
        if (this.outMessenger != null) {
            try {
                Message obtain = Message.obtain(null, NTRIPService.MSG_RELOAD_PREFERENCES, 0, 0);
                obtain.replyTo = this.inMessenger;
                this.outMessenger.send(obtain);
            } catch (RemoteException e) {
            }
        }
    }

    void doUnbindService() {
        if (this.mIsBound) {
            if (this.outMessenger != null) {
                try {
                    Message obtain = Message.obtain(null, 2);
                    obtain.replyTo = this.inMessenger;
                    this.outMessenger.send(obtain);
                } catch (RemoteException e) {
                }
            }
            try {
                unbindService(this.mConnection);
            } catch (Exception e) {

            }
            this.mIsBound = false;
        }
        this.btnService.setTag(getString(R.string.connect));
        this.btnService.setImageDrawable(getDrawable(android.R.drawable.ic_media_play));
        this.progressBar.setVisibility(View.GONE);
        this.tcConnectSource.setText(R.string.source_disconnect);
        this.tvConnectServer.setText(R.string.server_disconnect);
        NtripCaster.getInstance().getReferenceStation().clearClient();
        this.connectionNumTextView.setText(getString(R.string.User) + "：0");
        stopTimer();
    }

    private void startTimer() {
        if (updateTimer == null) {
            updateTimer = new Timer();
        }

        if (task == null) {
            task = new TimerTask() {
                @Override
                public void run() {
                    updateMap();
                }
            };
        }

        if (updateTimer != null && task != null) {
            updateTimer.schedule(task, 1000, 1000);
        }
    }

    private void stopTimer() {
        if (updateTimer != null) {
            updateTimer.cancel();
            updateTimer = null;
        }
        if (task != null) {
            task.cancel();
            task = null;
        }
    }

    private void updateMap() {
        ReferenceStation referenceStation = NtripCaster.getInstance().getReferenceStation();
        if (referenceStation != null) {
            if (ShareApplication.isRequireGGA(this)) {
                referenceStation.clearDeadClient(ShareApplication.getGGATime(this));
            }
            int num = referenceStation.getClientNum() < 0 ? 0 : referenceStation.getClientNum();
            Log.i("getClientNum", "getClientNum" + num);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    connectionNumTextView.setText(getString(R.string.User) + "：" + num);
                }
            });

            return;
        }
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                connectionNumTextView.setText(getString(R.string.User) + "：0");
            }
        });
    }

    @Override
    protected void onResume() {

        super.onResume();
        MobclickAgent.onResume(this);
        UTDevice.getUtdid(this);
        setFilters();  // Start listening notifications from UsbService

    }

    @Override
    protected void onPause() {
        super.onPause();
        MobclickAgent.onPause(this);

    }

    @Override
    protected void onDestroy() {
        try {
            doUnbindService();
            mLocationClient.stop();
//        NtripCaster.getInstance().close();
//        if (mNtripCasterThread != null) {
//            mNtripCasterThread.interrupt();
//        }
            unregisterReceiver(mUsbReceiver);
            unbindService(usbConnection);
        } catch (Exception e) {

        }
        super.onDestroy();
    }

    LocationClient mLocationClient;

    private void startLocation() {
        mLocationClient = new LocationClient(this);
        mLocationClient.registerLocationListener(new MyLocationListener());
        LocationClientOption locationClientOption = new LocationClientOption();
        // 可选，设置定位模式，默认高精度 LocationMode.Hight_Accuracy：高精度；
        locationClientOption.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);
        // 可选，设置返回经纬度坐标类型，默认GCJ02
        locationClientOption.setCoorType("bd09ll");
        // 如果设置为0，则代表单次定位，即仅定位一次，默认为0
        // 如果设置非0，需设置1000ms以上才有效
        locationClientOption.setScanSpan(1000);
        //可选，设置是否使用gps，默认false
        locationClientOption.setOpenGps(true);
        // 可选，是否需要地址信息，默认为不需要，即参数为false
        // 如果开发者需要获得当前点的地址信息，此处必须为tru
        locationClientOption.setIsNeedAddress(true);
        // 可选，默认false，设置是否需要POI结果，可以在BDLocation
        locationClientOption.setIsNeedLocationPoiList(true);
        // 设置定位参数
        mLocationClient.setLocOption(locationClientOption);
        // 开启定位
        mLocationClient.start();
    }

    public class MyLocationListener extends BDAbstractLocationListener {
        @Override
        public void onReceiveLocation(BDLocation location) {
            //mapView 销毁后不在处理新接收的位置
            if (location == null || HomeFragment.INSTANCE == null) {
                return;
            }

            if (!isSetCoord) {
                Log.i("ShareApplicationlocation", location.getLongitude() + "-" + location.getLatitude());
                ShareApplication.updateLastLocation(MainActivity.this, location.getLongitude(), location.getLatitude());
                isSetCoord = true;
            }
//            boolean first = (CurrentLongitude == 0 && CurrentLatitude == 0);
//            CurrentLatitude = location.getLatitude();
//            CurrentLongitude = location.getLongitude();
//            SharedPreferences defaultSharedPreferences = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
//            String ntriplocation = defaultSharedPreferences.getString("ntriplocation", "internalfine");
//            if (mIsBound) {
//                if (!ntriplocation.equals("internalfine") && ShareApplication.getSourceType(getApplicationContext()) == ShareApplication.SOURCE_TYP.NTRIPV1) {
//                    try {
//                        String ntriplatitude = defaultSharedPreferences.getString("ntriplatitude", "");
//                        String ntriplongitude = defaultSharedPreferences.getString("ntriplongitude", "");
                        CurrentLatitude = Double.valueOf(location.getLatitude());
                        CurrentLongitude = Double.valueOf(location.getLongitude());
//                    } catch (Exception e) {
//                    }
//                }
//                if (ShareApplication.getSourceType(getApplicationContext()) == ShareApplication.SOURCE_TYP.NTRRIP_NET) {
//                    return;
//                }
//            }

            MyLocationData locData = new MyLocationData.Builder()
                    .accuracy(location.getRadius())
                    // 此处设置开发者获取到的方向信息，顺时针0-360
                    .direction(location.getDirection()).latitude(location.getLatitude())
                    .longitude(location.getLongitude()).build();
//            if (btnService.getTag().toString().equals(getString(R.string.connect))) {
//                if (first) {
//                    LatLng GEO_BEIJING = new LatLng(location.getLatitude(), location.getLongitude());
//                    MapStatusUpdate status1 = MapStatusUpdateFactory.newLatLng(GEO_BEIJING);
//                    HomeFragment.INSTANCE.setMapStatus(status1);
//                }

                HomeFragment.INSTANCE.setLocation(locData);
//            }

            NTRIPService.InternalGPSLat = location.getLatitude();
            NTRIPService.InternalGPSLon = location.getLongitude();
        }
    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (navController.getCurrentDestination().getId() == R.id.nav_home) {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setIcon(R.drawable.ic_info);
                builder.setTitle(R.string.tips);
                builder.setMessage(R.string.confirm_exit);
                //设置确定按钮
                builder.setNegativeButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (mIsBound) {
                            ListenerBtnService();
                        }
                        finish();
                    }
                });
                //设置取消按钮
                builder.setPositiveButton(R.string.later, null);
                //显示提示框
                builder.show();
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    /**
     * Android6.0之后需要动态申请权限
     */
    private void requestPermission() {
        if (Build.VERSION.SDK_INT >= 23) {
            ArrayList<String> permissionsList = new ArrayList<>();
            String[] permissions = {
                    Manifest.permission.ACCESS_NETWORK_STATE,
                    Manifest.permission.INTERNET,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_WIFI_STATE,
            };
            for (String perm : permissions) {
                if (PackageManager.PERMISSION_GRANTED != checkSelfPermission(perm)) {
                    permissionsList.add(perm);
                    // 进入到这里代表没有权限.
                }
            }
            if (!permissionsList.isEmpty()) {
                String[] strings = new String[permissionsList.size()];
                requestPermissions(permissionsList.toArray(strings), 0);
            }
        }
    }

    /*
     * Notifications from UsbService will be received here.
     */
    private final BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {
                case UsbService.ACTION_USB_PERMISSION_GRANTED: // USB PERMISSION GRANTED
                    Toast.makeText(context, R.string.usb_connected, Toast.LENGTH_SHORT).show();
                    isUsbConnect = true;
                    break;
                case UsbService.ACTION_USB_PERMISSION_NOT_GRANTED: // USB PERMISSION NOT GRANTED
                    Toast.makeText(context, R.string.usb_no_permission, Toast.LENGTH_SHORT).show();
                    isUsbConnect = false;
                    break;
                case UsbService.ACTION_NO_USB: // NO USB CONNECTED
//                    Toast.makeText(context, "USB未连接", Toast.LENGTH_SHORT).show();
                    isUsbConnect = false;
                    break;
                case UsbService.ACTION_USB_DISCONNECTED: // USB DISCONNECTED
                    Toast.makeText(context, R.string.usb_disconnected, Toast.LENGTH_SHORT).show();
                    isUsbConnect = false;
                    break;
                case UsbService.ACTION_USB_NOT_SUPPORTED: // USB NOT SUPPORTED
                    Toast.makeText(context, R.string.usb_not_support, Toast.LENGTH_SHORT).show();
                    isUsbConnect = false;
                    break;
            }
        }
    };
    public static UsbService usbService;

    private void startService(Class<?> service, ServiceConnection serviceConnection, Bundle extras) {
        if (!UsbService.SERVICE_CONNECTED) {
            Intent startService = new Intent(this, service);
            if (extras != null && !extras.isEmpty()) {
                Set<String> keys = extras.keySet();
                for (String key : keys) {
                    String extra = extras.getString(key);
                    startService.putExtra(key, extra);
                }
            }
            startService(startService);
        }
        Intent bindingIntent = new Intent(this, service);
        bindService(bindingIntent, serviceConnection, Context.BIND_AUTO_CREATE);
    }

    private UsbHandler mHandler;
    private final ServiceConnection usbConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName arg0, IBinder arg1) {
            usbService = ((UsbService.UsbBinder) arg1).getService();
            usbService.setHandler(mHandler);
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            usbService = null;
        }
    };

    private void setFilters() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(UsbService.ACTION_USB_PERMISSION_GRANTED);
        filter.addAction(UsbService.ACTION_NO_USB);
        filter.addAction(UsbService.ACTION_USB_DISCONNECTED);
        filter.addAction(UsbService.ACTION_USB_NOT_SUPPORTED);
        filter.addAction(UsbService.ACTION_USB_PERMISSION_NOT_GRANTED);
        registerReceiver(mUsbReceiver, filter);
    }

    /*
     * This handler will be passed to UsbService. Data received from serial port is displayed through this handler
     */
    private static class UsbHandler extends Handler {
        private final WeakReference<MainActivity> mActivity;

        public UsbHandler(MainActivity activity) {
            mActivity = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            try {
                switch (msg.what) {
                    case UsbService.MESSAGE_FROM_SERIAL_PORT:
                        String data = (String) msg.obj;
                        mActivity.get().LogConnectMessage(data);
                        break;
                    case UsbService.CTS_CHANGE:
                        Toast.makeText(mActivity.get(), "CTS_CHANGE", Toast.LENGTH_LONG).show();
                        break;
                    case UsbService.DSR_CHANGE:
                        Toast.makeText(mActivity.get(), "DSR_CHANGE", Toast.LENGTH_LONG).show();
                        break;
                    case UsbService.SYNC_READ:
                        if (ShareApplication.getSourceType(mActivity.get()) == ShareApplication.SOURCE_TYP.OTG) {
                            byte[] buffer = (byte[]) msg.obj;
                            mActivity.get().SendDataToCasterReceiver(buffer);
//                        mActivity.get().LogConnectMessage(buffer.length + "");
                        }
                        break;
                }
            } catch (Exception e) {
//                Toast.makeText(mActivity.get(), e.getMessage(), Toast.LENGTH_LONG).show();
            }
        }
    }

    public void SendDataToCasterReceiver(final byte[] bArr) {
        if (mIsBound) {
            MainActivity.this.progressBar.setVisibility(View.VISIBLE);
            MainActivity.this.progressBar.setProgress(MainActivity.this.progressBar.getProgress() + bArr.length % 4096);
            if (MainActivity.this.progressBar.getProgress() >= 4096) {
                MainActivity.this.progressBar.setProgress(0);
            }
            tcConnectSource.setText(getString(R.string.data_source_connected));
            new Thread(new Runnable() {
                public void run() {
                    try {
                        ReferenceStation referenceStation = NtripCaster.getInstance().getReferenceStation();
                        if (referenceStation != null) {
                            NtripCaster.getInstance().getReferenceStation().pushData(bArr);
                            NtripCaster.getInstance().getReferenceStation().run();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        }
    }
}
