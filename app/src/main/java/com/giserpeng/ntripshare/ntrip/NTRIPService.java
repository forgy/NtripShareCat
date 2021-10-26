package com.giserpeng.ntripshare.ntrip;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.location.LocationListener;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.PowerManager;
import android.os.RemoteException;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.util.Base64;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import com.giserpeng.ntripshare.BuildConfig;
import com.giserpeng.ntripshare.MainActivity;
import com.giserpeng.ntripshare.R;
import com.giserpeng.ntripshare.ShareApplication;
import com.giserpeng.ntripshare.client.ProxyClientContainer;
import com.giserpeng.ntripshare.common.JsonUtil;
import com.giserpeng.ntripshare.common.container.Container;
import com.giserpeng.ntripshare.common.container.ContainerHelper;
import com.giserpeng.ntripshare.gnss.GnssService;
import com.giserpeng.ntripshare.gnss.bean.GnssData;
import com.giserpeng.ntripshare.gnss.listener.RTCMParserListener;
import com.giserpeng.ntripshare.gnss.utils.BitUtils;
import com.giserpeng.ntripshare.ntrip.Servers.NtripCaster;
import com.giserpeng.ntripshare.ntrip.Servers.ReferenceStation;
import com.giserpeng.ntripshare.ntrip.Tools.GPSPosition;
import com.giserpeng.ntripshare.ntrip.Tools.NMEA;
import com.giserpeng.ntripshare.ntrip.source.StreamSource;
import com.giserpeng.ntripshare.otg.UsbService;
import com.giserpeng.ntripshare.re.ReModel;
import com.giserpeng.ntripshare.ui.net.NetPointModel;
import com.giserpeng.ntripshare.ui.user.UserModel;
import com.giserpeng.ntripshare.util.DateUtils;
import com.giserpeng.ntripshare.util.ToolUtil;
import com.google.gson.reflect.TypeToken;
import com.ut.device.UTDevice;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ref.WeakReference;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

import static androidx.core.app.NotificationCompat.PRIORITY_MAX;

public class NTRIPService<TryToStartInForeground> extends Service {
    static final int MSG_ADD_NOTE_TO_NMEA = 9;
    static final int MSG_BT_FINISHED = 299;
    static final int MSG_BT_GOT_DATA = 201;
    static final int MSG_LOG_MESSAGE = 200;
    static final int MSG_NODE_NETWORK_FINISHED = 1999;
    static final int MSG_NETWORK_FINISHED = 199;
    static final int MSG_NETWORK_GOT_DATA = 101;
    static final int MSG_NODE_NETWORK_GOT_DATA = 1018;
    static final int MSG_NETWORK_TIMEOUT = 198;
    static final int MSG_NODE_NETWORK_TIMEOUT = 1988;
    static final int MSG_PROMPT_FOR_MOUNTPOINT = 11;
    public static final int MSG_RELOAD_PREFERENCES = 10;
    static final int MSG_REQUEST_FILESAVE = 902;
    public static final int MSG_REQUEST_LOCATION = 901;
    static final int MSG_STARTUP = 99;
    public static final int MSG_THREAD_SUICIDE = 0;
    static final int MSG_TIMER_TICK = 100;
    static final int MSG_UNREGISTER_CLIENT = 2;
    public static final int MSG_UPDATE_BYTES_IN = 4;
    public static final int MSG_UPDATE_LOG_APPEND = 6;
    public static final int MSG_UPDATE_LOCATION = 20;
    public static final int MSG_CONNECT_SOURCE_FAIL = 77;
    public static final int MSG_CONNECT_SOURCE_OK = 8;
    public static final int MSG_CONNECT_BLUETOOTH_OK = 8032;
    public static final int MSG_CONNECT_NODE_SOURCE_FAIL = 777;
    public static final int MSG_CONNECT_NODE_SOURCE_OK = 888;
    public static final int MSG_CONNECT_PROXY_FAIL = 79;
    public static final int MSG_CONNECT_PROXY_OK = 78;
    public static final int MSG_CONNECT_BLUETOOTH_OUTPUT = 788;
    static final int MSG_UPDATE_STATUS = 3;
    public static final int STATE_CONNECTED = 3;
    public static final int STATE_CONNECTING = 2;
    public static final int STATE_LISTEN = 1;
    public static final int STATE_NONE = 0;
    /* access modifiers changed from: private */
    public static boolean isRunning;
    private float AntennaHeight = 0.0f;
    private float BaselineLengthMeters;
    private String CorrectionAge;
    private String CorrectionStationID;
    private byte[] DataDGPSToSave = new byte[4096];
    private int DataDGPSToSaveIndex = 0;
    private byte[] DataNMEAToSave = new byte[4096];
    private int DataNMEAToSaveIndex = 0;
    /* access modifiers changed from: private */
    public int DisplayMsgType = 0;
    private float Elevation;
    private float FixAccuracy;
    private Boolean FixChangeAudioAlert = false;
    private String FixChangeRingtone = "";
    private int FixType;
    private String GPSTime;
    private String HDOP;
    private float Heading;
    private Boolean HideSatCountAbove6 = false;
    private Boolean mutiUser = false;
    /* access modifiers changed from: private */
    public static Double InternalGPSLat;
    /* access modifiers changed from: private */
    public static Double InternalGPSLon;
    //    public static double Latitude;
    public int LocalhostUDPPort = 20000;
    //    public static double Longitude;
    public static Double ManualLat;
    public static Double ManualLon;
    private String MostRecentGGA;
    private String NMEA = "";
    InputStream NTRIPInputStream = null;
    OutputStream NTRIPOutputStream = null;
    private String NTRIPResponse = "";
    public int NTRIPSendGGAEveryXSeconds = 10;
    Socket NTRIPSocket;
    StreamSource NTRIPStreamSource;
    Thread NTRIPThread;
    Thread ProxyThread;
//    Thread OTGhread;

    private int NTRIPTicksSinceGGASent = 0;
    private String NTRIP_Mountpoint = "";
    private String NTRIP_Password = "";
    private String NTRIP_Server_IP = "";
    private int maxUserNum = 5;
    private int NTRIP_Server_Port = 10000;
    private String NTRIP_Username = "";
    //    private String Caster_Username = "";
//    private String Caster_Password = "";
    private List<UserModel> userModelList = new ArrayList<>();
    private String ProxyServerIP = "106.13.183.59";
    private int ProxyServerPort = 4900;
    private String ProxyKey = "JFVJFJFF555251N2N81N48J2613F46CD536861726550726F";
    private boolean SslEnabel = false;
    private int NetworkConnectionAttempts = 0;
    private int NetworkDataMode = 0;
    /* access modifiers changed from: private */
    public Boolean NetworkIsConnected = false;
    //    public Boolean ProxyIsConnected = false;
    /* access modifiers changed from: private */
    public String NetworkProtocol = "none";
    /* access modifiers changed from: private */
    public int NetworkReConnectInTicks = 2;
    /* access modifiers changed from: private */
    public int NetworkReceivedByteCount = 0;
    private long testNet;
    private String PDOP;
    private String RMS2D;
    private String RMS3D;
    public int ReportLocationFrom = 0;
    private int SatsTracked;
    private float Speed;
    private int TicksSinceLastStatusSent;
    private float Undulation;
    private String VDOP;
    private double VehicleHeading;
    /* access modifiers changed from: private */
    public Handler dataHandler;
    private HandlerThread dataHandlerThread;
    final Messenger inMessenger;
    private boolean isCompleteLine;
    LocationListener locationlistener;
    private String logMsgs = "";
    private String logNMEA = "";
    /* access modifiers changed from: private */
    private BTConnectThread mBTConnectThread;
    private BTConnectedThread mBTConnectedThread;
    private int mBTState;
    private BluetoothAdapter mBluetoothAdapter;

    ArrayList<Messenger> mClients = new ArrayList<>();
    private Notification mNotification;
    private NotificationManager mNotificationManager;
    WifiManager.WifiLock mWifiLock;
    PowerManager powerManager;
    private Timer timer = new Timer();
    PowerManager.WakeLock wakeLock;

    private int BTConnectionMethod;
    private Boolean BTShouldAutoSwitch;
    private Boolean BTwasDisabled;
    private String MACAddress;
    private NtripClient ntripClient;
    private Boolean KeepScreenOn = false;
    private Boolean mBluetoothOutput = false;

    public static NTRIPService INSTANCE;

    private void setBTState(int i) {
        synchronized (this) {
            this.mBTState = i;
        }
    }

    public void BTconnect(BluetoothDevice bluetoothDevice) {
        synchronized (this) {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append(getString(R.string.connect_blutooth));
            stringBuilder.append(bluetoothDevice.getName() + "...");
            LogMessage(stringBuilder.toString());
            if (this.mBTState == STATE_CONNECTING && this.mBTConnectThread != null) {
                this.mBTConnectThread.cancel();
                this.mBTConnectThread = null;
            }
            if (this.mBTConnectedThread != null) {
                this.mBTConnectedThread.cancel();
                this.mBTConnectedThread = null;
            }
            BTConnectThread bTConnectThread = new BTConnectThread(bluetoothDevice, this.BTConnectionMethod);
            this.mBTConnectThread = bTConnectThread;
            bTConnectThread.start();
            setBTState(STATE_CONNECTING);
        }
    }

    public void BTconnected(BluetoothSocket bluetoothSocket) {
        synchronized (this) {
            if (this.mBTConnectThread != null) {
                this.mBTConnectThread.cancel();
                this.mBTConnectThread = null;
            }
            if (this.mBTConnectedThread != null) {
                this.mBTConnectedThread.cancel();
                this.mBTConnectedThread = null;
            }
            BTConnectedThread bTConnectedThread = new BTConnectedThread(this, bluetoothSocket);
            this.mBTConnectedThread = bTConnectedThread;
            bTConnectedThread.start();
            setBTState(STATE_CONNECTED);
        }
    }

    public void BTstart() {
        Log.i("BTstart", "BTstart");
        synchronized (this) {
            SetDisplayMsgType(STATE_NONE);
            if (this.mBTConnectThread != null) {
                this.mBTConnectThread.cancel();
                this.mBTConnectThread = null;
            }
            if (this.mBTConnectedThread != null) {
                this.mBTConnectedThread.cancel();
                this.mBTConnectedThread = null;
            }
            if (!BluetoothAdapter.checkBluetoothAddress(this.MACAddress)) {
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append(getString(R.string.ble_mac_error));
                stringBuilder.append(this.MACAddress);
                stringBuilder.append("\"");
                LogMessage(stringBuilder.toString());
                InformActivityOfThreadSuicide();
            } else if (this.MACAddress.equals("00:00:00:00:00:00")) {
                LogMessage(getString(R.string.ble_set));
                isRunning = false;
                InformActivityOfThreadSuicide();
                if (this.timer != null) {
                    this.timer.cancel();
                }
                StopThisService();
            } else {
                setBTState(STATE_LISTEN);
                BTconnect(this.mBluetoothAdapter.getRemoteDevice(this.MACAddress));
            }
        }
    }

    public void BTstop() {
        synchronized (this) {
            if (this.mBTConnectThread != null) {
                this.mBTConnectThread.cancel();
                this.mBTConnectThread = null;
            }
            if (this.mBTConnectedThread != null) {
                this.mBTConnectedThread.cancel();
                this.mBTConnectedThread = null;
            }
            setBTState(STATE_NONE);
        }
    }

    public void SendDataToBluetooth(byte[] bArr) {
        synchronized (this) {
            if (this.mBTState != STATE_CONNECTED && !ShareApplication.isBluetoothOutPut(this) && this.mBTConnectedThread != null) {
                return;
            }
            try {
                BTConnectedThread bTConnectedThread = this.mBTConnectedThread;
                bTConnectedThread.write(bArr);
            } catch (Exception e) {

            }

        }
    }

    private class BTConnectThread extends Thread {
        private final BluetoothSocket mmSocket;

        private BTConnectThread(BluetoothDevice bluetoothDevice, int i) {
            BluetoothSocket createInsecureRfcommSocketToServiceRecord;
            try {
                UUID fromString = UUID.fromString("07011101-0701-1701-8701-00805F9B34FB");
                createInsecureRfcommSocketToServiceRecord = i != 0 ? i != 1 ? i != 2 ?
                        bluetoothDevice.createInsecureRfcommSocketToServiceRecord(fromString) :
                        InsecureBluetooth.createRfcommSocketToServiceRecord(bluetoothDevice, fromString, true) :
                        bluetoothDevice.createRfcommSocketToServiceRecord(fromString) :
                        (BluetoothSocket) bluetoothDevice.getClass().getMethod("createRfcommSocket", new Class[]{Integer.TYPE}).invoke(bluetoothDevice, new Object[]{Integer.valueOf(1)});
            } catch (Exception e) {
                e.printStackTrace();
                Handler access$2300 = NTRIPService.this.dataHandler;
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append(getString(R.string.ble_error));
                stringBuilder.append(e);
                access$2300.sendMessage(Message.obtain(null, NTRIPService.MSG_LOG_MESSAGE, stringBuilder.toString()));
                createInsecureRfcommSocketToServiceRecord = null;
            }
            this.mmSocket = createInsecureRfcommSocketToServiceRecord;
        }

        private void cancel() {
            try {
                if (this.mmSocket != null) {
                    this.mmSocket.close();
                }
            } catch (IOException e) {
            }
        }

        public void run() {
            NTRIPService.this.dataHandler.sendMessage(Message.obtain(null, NTRIPService.MSG_LOG_MESSAGE, getString(R.string.connecting_ble)));
            try {
                if (NTRIPService.this.mBluetoothAdapter != null) {
                    NTRIPService.this.mBluetoothAdapter.cancelDiscovery();
                }
                this.mmSocket.connect();
                synchronized (NTRIPService.this) {
                    NTRIPService.this.mBTConnectThread = null;
                }
                NTRIPService.this.BTconnected(this.mmSocket);
            } catch (Exception e) {
                try {
                    if (this.mmSocket != null) {
                        this.mmSocket.close();
                    }
                } catch (IOException e2) {
                }
                Handler access$2300 = NTRIPService.this.dataHandler;
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append(getString(R.string.connecting_ble_fail));
                stringBuilder.append(e.getMessage());
                access$2300.sendMessage(Message.obtain(null, NTRIPService.MSG_LOG_MESSAGE, stringBuilder.toString()));
                NTRIPService.this.dataHandler.sendMessage(Message.obtain(null, NTRIPService.MSG_BT_FINISHED));
            }
        }
    }

    private BlueWaitingTimer timerBTConnectedThread = new BlueWaitingTimer(System.currentTimeMillis(), 100);

    private class BlueWaitingTimer extends CountDownTimer {
        private BTConnectedThread thread;

        public void setThread(BTConnectedThread thread) {
            this.thread = thread;
        }

        public BlueWaitingTimer(long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);
        }

        @Override
        public void onTick(long millisUntilFinished) {
            if (this.thread != null && (System.currentTimeMillis() - this.thread.mile > 100) && this.thread.currentLen > 0) {
                this.thread.sendData();
            }
        }

        @Override
        public void onFinish() {

        }
    }


    private class BTConnectedThread extends Thread {
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;
        private final BluetoothSocket mmSocket;
        private final NTRIPService ntripService;
        byte[] obj = new byte[10240];
        long mile = System.currentTimeMillis();
        int currentLen = 0;
        private static final int MSG_PRE = (byte) 0XD3;

        private BTConnectedThread(NTRIPService nTRIPService, BluetoothSocket bluetoothSocket) {
            timerBTConnectedThread.setThread(this);
            InputStream inputStream = null;
            OutputStream outputStream = null;
            this.ntripService = nTRIPService;
            this.mmSocket = bluetoothSocket;
            try {
                inputStream = bluetoothSocket.getInputStream();
                try {
                    outputStream = bluetoothSocket.getOutputStream();
                } catch (IOException e) {
                    Log.i("MSG_BT_GOT_DATA", e.getMessage());
                    ntripService.dataHandler.sendMessage(Message.obtain(null, NTRIPService.MSG_LOG_MESSAGE, "Could not create Streams"));
                    ntripService.dataHandler.sendMessage(Message.obtain(null, NTRIPService.MSG_BT_FINISHED));
//                    this.mmInStream = inputStream;
//                    this.mmOutStream = outputStream;
                }
            } catch (IOException e2) {
                Log.i("MSG_BT_GOT_DATA", e2.getMessage());
//                inputStream = outputStream;
                ntripService.dataHandler.sendMessage(Message.obtain(null, NTRIPService.MSG_LOG_MESSAGE, "Could not create Streams"));
                ntripService.dataHandler.sendMessage(Message.obtain(null, NTRIPService.MSG_BT_FINISHED));
//                this.mmInStream = inputStream;
//                this.mmOutStream = outputStream;
            }
            this.mmInStream = inputStream;
            this.mmOutStream = outputStream;
        }

        private void cancel() {
            try {
                if (this.mmSocket != null) {
                    this.mmSocket.close();
                }
            } catch (IOException e) {
            }
        }

        private void write(byte[] bArr) {
            try {
                this.mmOutStream.write(bArr);
            } catch (IOException e) {
            }
        }

        public void sendData() {
            Log.i("MSG_BT_GOT_DATA", "sendData" + currentLen);
            Object obj2 = new byte[currentLen];
            System.arraycopy(obj, 0, obj2, 0, currentLen);
            ntripService.dataHandler.sendMessage(Message.obtain(null, NTRIPService.MSG_BT_GOT_DATA, obj2));
            currentLen = 0;
        }

        public void run() {
            ntripService.dataHandler.sendMessage(Message.obtain(null, NTRIPService.MSG_LOG_MESSAGE, getString(R.string.connecting_ble_success)));
            if (ShareApplication.getSourceType(ntripService) == ShareApplication.SOURCE_TYP.BLUETOOTH) {
                SendConnectStatusToActivity(MSG_CONNECT_SOURCE_OK, getString(R.string.connect_source_server_log));
            }
            SendConnectStatusToActivity(MSG_CONNECT_BLUETOOTH_OK, getString(R.string.connect_source_server_log));
            mile = System.currentTimeMillis();
            timerBTConnectedThread.start();
            while (true) {
                try {
                    if (System.currentTimeMillis() - mile >= 200 && currentLen > 0) {
                        sendData();
                    }

                    Log.i("MSG_BT_GOT_DATA", mile + "");
                    byte[] data = new byte[4096];
                    int read = this.mmInStream.read(data, 0, 4096);
                    if (read <= 0) {
                        continue;
                    }
                    mile = System.currentTimeMillis();
                    Log.i("MSG_BT_GOT_DATA", (System.currentTimeMillis() - mile) + "*" + currentLen);
                    if (data[0] == MSG_PRE) {
                        sendData();
                        currentLen = 0;
                    }
                    System.arraycopy(data, 0, obj, currentLen, read);

//                    timerBTConnectedThread.cancel();
                    currentLen += read;
//                    timerBTConnectedThread.start();
                } catch (IOException e2) {
                    Log.i("MSG_BT_GOT_DATA", e2.getMessage());
                    ntripService.dataHandler.sendMessage(Message.obtain(null, NTRIPService.MSG_BT_FINISHED));
                    return;
                }
            }
        }
    }

    private static class IncomingHandler extends Handler {
        private final WeakReference<NTRIPService> mTarget;

        IncomingHandler(NTRIPService nTRIPService) {
            this.mTarget = new WeakReference<>(nTRIPService);
        }

        public void handleMessage(Message message) {
            super.handleMessage(message);
            NTRIPService nTRIPService = (NTRIPService) this.mTarget.get();
            if (nTRIPService != null) {
                int i = message.what;
                if (i == 1) {
                    nTRIPService.mClients.add(message.replyTo);
                } else if (i == 2) {
                    nTRIPService.mClients.remove(message.replyTo);
                } else if (i == 3) {
//                    nTRIPService.sendStatusMessageToUI();
                } else if (i == 7) {
                    nTRIPService.sendAllLogMessagesToUI();
                    if (!NTRIPService.isRunning) {
                        nTRIPService.InformActivityOfThreadSuicide();
                    }
                    if (nTRIPService.NetworkReceivedByteCount > 0) {
                        nTRIPService.SendByteCountProgressBarVisibility(1);
                        nTRIPService.SendByteCountToActivity();
                    }
                } else if (i == MSG_CONNECT_BLUETOOTH_OUTPUT) {
                    nTRIPService.MACAddress = ShareApplication.getBluetoothMac(nTRIPService);
                    BluetoothAdapter bluetoothAdapter = nTRIPService.mBluetoothAdapter;
                    if (bluetoothAdapter == null) {
                        Toast.makeText(nTRIPService, R.string.no_blutooth_support, Toast.LENGTH_SHORT).show();
                        nTRIPService.LogMessage(nTRIPService.getString(R.string.no_blutooth_support));
                    } else if (bluetoothAdapter.isEnabled()) {
                        nTRIPService.mBluetoothOutput = true;
                        nTRIPService.BTstart();
                    } else {
                        Toast.makeText(nTRIPService, R.string.confirm_blutooth_open, Toast.LENGTH_SHORT).show();
                        nTRIPService.LogMessage(nTRIPService.getString(R.string.confirm_blutooth_open));
                    }
                } else if (i == MSG_RELOAD_PREFERENCES) {
                    nTRIPService.LoadPreferences();
                } else if (nTRIPService.DisplayMsgType == 0) {
                    nTRIPService.SetDisplayMsgType(1);
                } else {
                    nTRIPService.SetDisplayMsgType(0);
                }
            }
        }
    }

    private class NetworkClient implements Runnable {
        String nMountpoint;
        String nPassword;
        int nPort;
        String nProtocol;
        String nServer;
        String nUsername;

        private NetworkClient(String Protocol, String Server, int Port, String Mountpoint, String Username, String Password) {
            this.nProtocol = Protocol;
            this.nServer = Server;
            this.nPort = Port;
            this.nMountpoint = Mountpoint;
            this.nUsername = Username;
            this.nPassword = Password;
            if (Mountpoint.contains(" ")) {
                NTRIPService.this.dataHandler.sendMessage(Message.obtain((Handler) null, NTRIPService.MSG_LOG_MESSAGE, "Error: Stream name contains a space."));
                Mountpoint = "";
            }
            if (Mountpoint.endsWith("_GGA1HZ")) {
                int unused = NTRIPService.this.NTRIPSendGGAEveryXSeconds = 1;
            } else {
                int unused2 = NTRIPService.this.NTRIPSendGGAEveryXSeconds = 1;
            }
        }

        private String ToBase64(String str) {
            return Base64.encodeToString(str.getBytes(), 4);
        }

        @Override
        public void run() {
            String str;
            try {
                InetSocketAddress inetSocketAddress = new InetSocketAddress(this.nServer, this.nPort);
                NTRIPService.this.NTRIPSocket = new Socket();
                NTRIPService.this.NTRIPSocket.connect(inetSocketAddress, 10000);
                if (NTRIPService.this.NTRIPSocket.isConnected()) {
                    NTRIPService.this.NTRIPSocket.setSoTimeout(20000);
                    NTRIPService.this.NTRIPInputStream = NTRIPService.this.NTRIPSocket.getInputStream();
                    NTRIPService.this.NTRIPOutputStream = NTRIPService.this.NTRIPSocket.getOutputStream();
                    if (this.nProtocol.equals("ntripv1")) {
                        String str2 = ("GET /" + this.nMountpoint + " HTTP/1.0\r\n") + "User-Agent: NTRIP Android";
                        if (NTRIPService.this.ReportLocationFrom == 0) {
                            str = str2 + "Ext";
                        } else if (NTRIPService.this.ReportLocationFrom == 1) {
                            str = str2 + "Man";
                        } else {
                            str = str2 + "Int";
                        }
                        String str3 = ((str + UTDevice.getUtdid(NTRIPService.this) + ToolUtil.getRandomString() + "\r\n") + "Accept: */*\r\n") + "Connection: close\r\n";
                        if (this.nUsername.length() > 0) {
                            StringBuilder sb = new StringBuilder();
                            sb.append(str3);
                            sb.append("Authorization: Basic ");
                            sb.append(ToBase64(this.nUsername + ":" + this.nPassword));
                            str3 = sb.toString();
                        }
                        NTRIPService.this.NTRIPOutputStream.write((str3 + "\r\n").getBytes());
                    }
                    byte[] bArr = new byte[4096];
                    int read = NTRIPService.this.NTRIPInputStream.read(bArr, 0, 4096);
                    while (read != -1) {
                        byte[] bArr2 = new byte[read];
                        System.arraycopy(bArr, 0, bArr2, 0, read);
                        NTRIPService.this.dataHandler.sendMessage(Message.obtain((Handler) null, NTRIPService.MSG_NETWORK_GOT_DATA, bArr2));
                        read = NTRIPService.this.NTRIPInputStream.read(bArr, 0, 4096);
                    }
                }
                try {
                    if (NTRIPService.this.NTRIPInputStream != null) {
                        NTRIPService.this.NTRIPInputStream.close();
                    }
                    if (NTRIPService.this.NTRIPOutputStream != null) {
                        NTRIPService.this.NTRIPOutputStream.close();
                    }
                    if (NTRIPService.this.NTRIPSocket != null) {
                        NTRIPService.this.NTRIPSocket.close();
                    }
                } catch (Exception e) {
                    e = e;
                    e.printStackTrace();
                    NTRIPService.this.dataHandler.sendMessage(Message.obtain((Handler) null, NTRIPService.MSG_NETWORK_FINISHED));
                }
            } catch (SocketTimeoutException e2) {
                NTRIPService.this.dataHandler.sendMessage(Message.obtain((Handler) null, NTRIPService.MSG_NETWORK_TIMEOUT));
                try {
                    if (NTRIPService.this.NTRIPInputStream != null) {
                        NTRIPService.this.NTRIPInputStream.close();
                    }
                    if (NTRIPService.this.NTRIPOutputStream != null) {
                        NTRIPService.this.NTRIPOutputStream.close();
                    }
                    if (NTRIPService.this.NTRIPSocket != null) {
                        NTRIPService.this.NTRIPSocket.close();
                    }
                } catch (Exception e3) {
                    e3.printStackTrace();
                    NTRIPService.this.dataHandler.sendMessage(Message.obtain((Handler) null, NTRIPService.MSG_NETWORK_FINISHED));
                }
            } catch (Exception e4) {
                e4.printStackTrace();
                try {
                    if (NTRIPService.this.NTRIPInputStream != null) {
                        NTRIPService.this.NTRIPInputStream.close();
                    }
                    if (NTRIPService.this.NTRIPOutputStream != null) {
                        NTRIPService.this.NTRIPOutputStream.close();
                    }
                    if (NTRIPService.this.NTRIPSocket != null) {
                        NTRIPService.this.NTRIPSocket.close();
                    }
                } catch (Exception e5) {
                    e5.printStackTrace();
                    NTRIPService.this.dataHandler.sendMessage(Message.obtain((Handler) null, NTRIPService.MSG_NETWORK_FINISHED));
                }
            } catch (Throwable th) {
                try {
                    if (NTRIPService.this.NTRIPInputStream != null) {
                        NTRIPService.this.NTRIPInputStream.close();
                    }
                    if (NTRIPService.this.NTRIPOutputStream != null) {
                        NTRIPService.this.NTRIPOutputStream.close();
                    }
                    if (NTRIPService.this.NTRIPSocket != null) {
                        NTRIPService.this.NTRIPSocket.close();
                    }
                } catch (Exception e6) {
                    e6.printStackTrace();
                }
                NTRIPService.this.dataHandler.sendMessage(Message.obtain((Handler) null, NTRIPService.MSG_NETWORK_FINISHED));
                throw th;
            }
            NTRIPService.this.dataHandler.sendMessage(Message.obtain((Handler) null, NTRIPService.MSG_NETWORK_FINISHED));
        }
    }

    private class NetworkNodeClient implements Runnable {
        String nMountpoint;
        String nPassword;
        int nPort;
        String nServer;
        String nUsername;
        NetPointModel mNetPointModel;
        Socket mNTRIPSocket;
        InputStream mNTRIPInputStream;
        OutputStream mNTRIPOutputStream;
        long dataTime;
        private int mNetworkDataMode = 0;
        private String NTRIPResponse = "";

        private NetworkNodeClient(NetPointModel netPointModel) {
            this.mNetPointModel = netPointModel;
            this.nServer = netPointModel.getIp();
            this.nPort = netPointModel.getPort();
            this.nMountpoint = netPointModel.getMountPoint();
            this.nUsername = netPointModel.getUserName();
            this.nPassword = netPointModel.getPassword();
        }

        private String ToBase64(String str) {
            return Base64.encodeToString(str.getBytes(), 4);
        }

        @Override
        public void run() {
            String str;
            try {
                InetSocketAddress inetSocketAddress = new InetSocketAddress(this.nServer, this.nPort);
                mNTRIPSocket = new Socket();
                mNTRIPSocket.connect(inetSocketAddress, 10000);
                if (mNTRIPSocket.isConnected()) {
                    mNTRIPSocket.setSoTimeout(20000);
                    mNTRIPInputStream = mNTRIPSocket.getInputStream();
                    mNTRIPOutputStream = mNTRIPSocket.getOutputStream();
                    String str2 = ("GET /" + this.nMountpoint + " HTTP/1.0\r\n") + "User-Agent: NTRIP Android";
                    if (NTRIPService.this.ReportLocationFrom == 0) {
                        str = str2 + "Ext";
                    } else if (NTRIPService.this.ReportLocationFrom == 1) {
                        str = str2 + "Man";
                    } else {
                        str = str2 + "Int";
                    }
                    String str3 = ((str + UTDevice.getUtdid(NTRIPService.this) + ToolUtil.getRandomString() + "\r\n") + "Accept: */*\r\n") + "Connection: close\r\n";
                    if (this.nUsername.length() > 0) {
                        StringBuilder sb = new StringBuilder();
                        sb.append(str3);
                        sb.append("Authorization: Basic ");
                        sb.append(ToBase64(this.nUsername + ":" + this.nPassword));
                        str3 = sb.toString();
                    }
                    mNTRIPOutputStream.write((str3 + "\r\n").getBytes());

                    byte[] bArr = new byte[4096];
                    int read = mNTRIPInputStream.read(bArr, 0, 4096);
                    while (read != -1) {
                        byte[] bArr2 = new byte[read];
                        System.arraycopy(bArr, 0, bArr2, 0, read);
                        ParseNTRIPDataStream(bArr2);
                        read = mNTRIPInputStream.read(bArr, 0, 4096);
                    }
                }
                try {
                    if (mNTRIPInputStream != null) {
                        mNTRIPInputStream.close();
                    }
                    if (mNTRIPOutputStream != null) {
                        mNTRIPOutputStream.close();
                    }
                    if (mNTRIPSocket != null) {
                        mNTRIPSocket.close();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    NTRIPService.this.dataHandler.sendMessage(Message.obtain((Handler) null, NTRIPService.MSG_NODE_NETWORK_FINISHED, mNetPointModel));
                }
            } catch (SocketTimeoutException e2) {
                NTRIPService.this.dataHandler.sendMessage(Message.obtain((Handler) null, NTRIPService.MSG_NODE_NETWORK_TIMEOUT, mNetPointModel));
                try {
                    if (mNTRIPInputStream != null) {
                        mNTRIPInputStream.close();
                    }
                    if (mNTRIPOutputStream != null) {
                        mNTRIPOutputStream.close();
                    }
                    if (mNTRIPSocket != null) {
                        mNTRIPSocket.close();
                    }
                } catch (Exception e3) {
                    e3.printStackTrace();
                    NTRIPService.this.dataHandler.sendMessage(Message.obtain((Handler) null, NTRIPService.MSG_NODE_NETWORK_FINISHED, mNetPointModel));
                }
            } catch (Exception e4) {
                e4.printStackTrace();
                try {
                    if (mNTRIPInputStream != null) {
                        mNTRIPInputStream.close();
                    }
                    if (mNTRIPOutputStream != null) {
                        mNTRIPOutputStream.close();
                    }
                    if (mNTRIPSocket != null) {
                        mNTRIPSocket.close();
                    }
                } catch (Exception e5) {
                    e5.printStackTrace();
                    NTRIPService.this.dataHandler.sendMessage(Message.obtain((Handler) null, NTRIPService.MSG_NODE_NETWORK_FINISHED, mNetPointModel));
                }
            } catch (Throwable th) {
                try {
                    if (mNTRIPInputStream != null) {
                        mNTRIPInputStream.close();
                    }
                    if (mNTRIPOutputStream != null) {
                        mNTRIPOutputStream.close();
                    }
                    if (mNTRIPSocket != null) {
                        mNTRIPSocket.close();
                    }
                } catch (Exception e6) {
                    e6.printStackTrace();
                }
                NTRIPService.this.dataHandler.sendMessage(Message.obtain((Handler) null, NTRIPService.MSG_NODE_NETWORK_FINISHED, mNetPointModel));
                throw th;
            }
            NTRIPService.this.dataHandler.sendMessage(Message.obtain((Handler) null, NTRIPService.MSG_NODE_NETWORK_FINISHED, mNetPointModel));
        }


        /*
向服务器发送GGA信息
 */
        public void SendNodeGGAToCaster() {
            String gga = GenerateGGAFromLatLon(this.mNetPointModel.getLat() + (Math.random() / 10000), this.mNetPointModel.getLon() + (Math.random() / 10000)) + "\r\n";
            Socket socket = this.mNTRIPSocket;
            if (socket != null && socket.isConnected()) {
                new Thread(new Runnable() {
                    public void run() {
                        try {
                            NetworkNodeClient.this.mNTRIPOutputStream.write(gga.getBytes());
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }).start();
            }
        }

        public void SendNodeDataToCasterReceiver(byte[] data) {

            new Thread(new Runnable() {
                public void run() {
                    try {
                        ReferenceStation referenceStation = NtripCaster.getInstance().getReferenceStation();
                        if (referenceStation != null) {
                            NtripCaster.getInstance().getReferenceStation().pushData(mNetPointModel, data);
                            Log.i("SendNodeDataToCasterReceiver", mNetPointModel.getUuid() + "-" + data.length);
                            NtripCaster.getInstance().getReferenceStation().run();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        }

        public void ParseNTRIPDataStream(byte[] bArr) {
            try {
                String ConnectionStr = "";
                if (mNetPointModel != null) {
                    ConnectionStr = mNetPointModel.getConnectionStr();
                }
                if (mNetworkDataMode == 0) {
                    String str = this.NTRIPResponse + new String(bArr);
                    this.NTRIPResponse = str;
                    if (str.contains("ICY 200 OK")) {
                        SendNodeGGAToCaster();
                        dataTime = System.currentTimeMillis();
                        mNetworkDataMode = MSG_STARTUP;
                        SendConnectStatusToActivity(MSG_CONNECT_NODE_SOURCE_OK, ConnectionStr + getString(R.string.connect_source_server_log));
                        LogMessage(ConnectionStr + getString(R.string.connect_source_server_log));
                    } else if (this.NTRIPResponse.contains("401 Unauthorized")) {
                        NTRIPService.this.dataHandler.sendMessage(Message.obtain((Handler) null, NTRIPService.MSG_LOG_MESSAGE,
                                ConnectionStr + getString(R.string.connect_source_server_error_log)));
                        SendConnectStatusToActivity(MSG_CONNECT_NODE_SOURCE_FAIL, ConnectionStr + getString(R.string.connect_source_server_error_log));
                        if (NTRIP_Username.length() == 0) {
                            NTRIPService.this.dataHandler.sendMessage(Message.obtain((Handler) null, NTRIPService.MSG_LOG_MESSAGE,
                                    ConnectionStr + getString(R.string.connect_source_server_error_log)));
                        } else if (NTRIP_Username.startsWith(" ")) {
                            NTRIPService.this.dataHandler.sendMessage(Message.obtain((Handler) null, NTRIPService.MSG_LOG_MESSAGE,
                                    ConnectionStr + getString(R.string.connect_source_server_error_log)));
                        } else if (NTRIP_Username.endsWith(" ")) {
                            NTRIPService.this.dataHandler.sendMessage(Message.obtain((Handler) null, NTRIPService.MSG_LOG_MESSAGE,
                                    ConnectionStr + getString(R.string.connect_source_server_error_log)));
                        } else if (NTRIP_Password.contains(" ")) {
                            NTRIPService.this.dataHandler.sendMessage(Message.obtain((Handler) null, NTRIPService.MSG_LOG_MESSAGE,
                                    ConnectionStr + getString(R.string.connect_source_server_error_log)));
                        }
                        NTRIPService.this.dataHandler.sendMessage(Message.obtain((Handler) null, NTRIPService.MSG_NODE_NETWORK_FINISHED, mNetPointModel));
                    } else if (this.NTRIPResponse.length() > 1024) {
                        SendConnectStatusToActivity(MSG_CONNECT_NODE_SOURCE_FAIL, ConnectionStr + getString(R.string.unknow_response));
                        NTRIPService.this.dataHandler.sendMessage(Message.obtain((Handler) null, NTRIPService.MSG_LOG_MESSAGE,
                                ConnectionStr + getString(R.string.unknow_response)));
                        NTRIPService.this.dataHandler.sendMessage(Message.obtain((Handler) null, NTRIPService.MSG_LOG_MESSAGE,
                                this.NTRIPResponse));
                        NTRIPService.this.dataHandler.sendMessage(Message.obtain((Handler) null, NTRIPService.MSG_LOG_MESSAGE,
                                ConnectionStr + getString(R.string.unknow_response)));
                        NTRIPService.this.dataHandler.sendMessage(Message.obtain((Handler) null, NTRIPService.MSG_NODE_NETWORK_FINISHED, mNetPointModel));
                    }
                } else if (mNetworkDataMode == MSG_STARTUP) {
                    dataTime = System.currentTimeMillis();
                    NTRIPService.this.NetworkReceivedByteCount += bArr.length;
                    SendByteCountToActivity();
                    SendNodeDataToCasterReceiver(bArr);
                }
            } catch (Exception e) {

            }
        }
    }

    private class ProxyThread implements Runnable {
        private String ProxyServerIP = "106.13.183.59";
        private int ProxyServerPort = 4900;
        private String ProxyKey = "JFVJFJFF555251N2N81N48J2613F46CD536861726550726F";
        private boolean SslEnabel = false;
        private ProxyClientContainer proxyClientContainer;

        private ProxyThread(String ProxyServerIP, int ProxyServerPort, String ProxyKey, boolean SslEnabel) {
            this.ProxyServerIP = ProxyServerIP;
            this.ProxyServerPort = ProxyServerPort;
            this.ProxyKey = ProxyKey;
            this.SslEnabel = SslEnabel;
            this.proxyClientContainer = new ProxyClientContainer(this.ProxyServerIP, this.ProxyKey, this.ProxyServerPort, UTDevice.getUtdid(NTRIPService.this));
            this.proxyClientContainer.setOnProxyEvent(new ProxyClientContainer.OnProxyEvent() {
                @Override
                public void onConnected() {
                    try {
                        NtripCaster.getInstance().getReferenceStation().clearClient();
                    } catch (Exception e) {

                    }
                    SendConnectStatusToActivity(MSG_CONNECT_PROXY_OK, getString(R.string.connecting_server_success));
                    LogMessage(getString(R.string.connecting_server_success));
                }

                @Override
                public void onDisConnected() {
                    SendConnectStatusToActivity(MSG_CONNECT_PROXY_FAIL, getString(R.string.connecting_server_fail));
                }

                @Override
                public void onReConnect() {
//                    SendConnectStatusToActivity(MSG_CONNECT_PROXY_FAIL, getString(R.string.connecting_server_fail));
                    LogMessage(getString(R.string.connecting_server));
                }
            });
        }

        public void run() {
            try {
                LogMessage(getString(R.string.connecting_server));
                ContainerHelper.start(Arrays.asList(new Container[]{proxyClientContainer}));
            } catch (Exception e) {
                SendConnectStatusToActivity(MSG_CONNECT_PROXY_FAIL, getString(R.string.connecting_server_fail));
                LogMessage(getString(R.string.connecting_server_fail));
            }
        }
    }

    public NTRIPService() {
        this.ManualLat = 0.0;
        this.ManualLon = 0.0;
        this.InternalGPSLat = 0.0;
        this.InternalGPSLon = 0.0;
        this.MostRecentGGA = "";
        this.GPSTime = "";
        this.FixType = 10;
        this.FixAccuracy = 100.0f;
        this.SatsTracked = 0;
        this.HDOP = "";
        this.PDOP = "";
        this.VDOP = "";
        this.RMS2D = "";
        this.RMS3D = "";
        this.MACAddress = "00:00:00:00:00:00";
        this.CorrectionAge = "?";
        this.CorrectionStationID = "?";
        this.BaselineLengthMeters = 0.0f;
        this.Elevation = 0.0f;
        this.Undulation = 0.0f;
        this.Speed = 0.0f;
        this.Heading = 0.0f;
        this.TicksSinceLastStatusSent = 0;
        this.inMessenger = new Messenger(new IncomingHandler(this));
        this.dataHandlerThread = new HandlerThread("Data Handler Thread");
    }

    private String CalculateChecksum(String str) {
        int i = 0;
        char c = 0;
        while (true) {
            int i2 = i;
            if (i2 >= str.length()) {
                break;
            }
            c ^= str.charAt(i2);
            i = i2 + 1;
        }
        String upperCase = Integer.toHexString(c).toUpperCase(Locale.ENGLISH);
        if (upperCase.length() >= 2) {
            return upperCase;
        }
        return "0" + upperCase;
    }

    private void CheckIfDownloadedSourceTableIsComplete() {
        if (this.NTRIPResponse.indexOf("\r\nENDSOURCETABLE") > 0) {
            LogMessage("NTRIP: Downloaded stream list");
            LogMessage("Please select a stream");
            LogMessage("NTRIP: Disabled");
            SharedPreferences.Editor edit = PreferenceManager.getDefaultSharedPreferences(getBaseContext()).edit();
            edit.putString("ntripsourcetable", this.NTRIPResponse);
            edit.apply();
            for (int size = this.mClients.size() - 1; size >= 0; size--) {
                try {
                    this.mClients.get(size).send(Message.obtain((Handler) null, 11, 0, 0));
                } catch (RemoteException e) {
                    this.mClients.remove(size);
                }
            }
        }
    }

    private String GenerateGGAFromLatLon(double d, double d2) {
        String str;
        String str2;
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HHmmss", Locale.US);
        simpleDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        String format = simpleDateFormat.format(new Date());
        if (format.equals("000000")) {
            format = "000001";
        }
        String str3 = "GPGGA," + format + ",";
        double abs = Math.abs(d);
        double d3 = abs % 1.0d;
        int i = (int) (abs - d3);
        double d4 = 60.0d * d3;
        double d5 = d4 % 1.0d;
        int i2 = (int) (d5 * 10000.0d);
        int i3 = (i * MSG_TIMER_TICK) + ((int) (d4 - d5));
        if (i3 < 1000) {
            str3 = str3 + "0";
            if (i3 < MSG_TIMER_TICK) {
                str3 = str3 + "0";
            }
        }
        String str4 = str3 + i3 + ".";
        if (i2 < 1000) {
            str4 = str4 + "0";
            if (i2 < MSG_TIMER_TICK) {
                str4 = str4 + "0";
                if (i2 < 10) {
                    str4 = str4 + "0";
                }
            }
        }
        String str5 = str4 + i2;
        if (d > 0.0d) {
            str = str5 + ",N,";
        } else {
            str = str5 + ",S,";
        }
        double abs2 = Math.abs(d2);
        double d6 = abs2 % 1.0d;
        int i4 = (int) (abs2 - d6);
        double d7 = 60.0d * d6;
        double d8 = d7 % 1.0d;
        int i5 = (int) (d8 * 10000.0d);
        int i6 = (i4 * MSG_TIMER_TICK) + ((int) (d7 - d8));
        if (i6 < 10000) {
            str = str + "0";
            if (i6 < 1000) {
                str = str + "0";
                if (i6 < MSG_TIMER_TICK) {
                    str = str + "0";
                }
            }
        }
        String str6 = str + i6 + ".";
        if (i5 < 1000) {
            str6 = str6 + "0";
            if (i5 < MSG_TIMER_TICK) {
                str6 = str6 + "0";
                if (i5 < 10) {
                    str6 = str6 + "0";
                }
            }
        }
        String str7 = str6 + i5;
        if (d2 > 0.0d) {
            str2 = str7 + ",E,";
        } else {
            str2 = str7 + ",W,";
        }
        String str8 = str2 + "4,8,1,0,M,-32,M,3,0";
        return "$" + str8 + "*" + CalculateChecksum(str8);
    }

    /* access modifiers changed from: private */
    public void InformActivityOfThreadSuicide() {
        for (int size = this.mClients.size() - 1; size >= 0; size--) {
            try {
                this.mClients.get(size).send(Message.obtain((Handler) null, MSG_THREAD_SUICIDE, 0, 0));
            } catch (RemoteException e) {
                this.mClients.remove(size);
            }
        }
    }

    private void LoadPreferences() {
        SharedPreferences defaultSharedPreferences = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        this.NetworkProtocol = defaultSharedPreferences.getString("networkprotocol", "none");
        this.NetworkReConnectInTicks = 2;

        this.KeepScreenOn = Boolean.valueOf(defaultSharedPreferences.getBoolean("keepscreenon", false));
        String ntriplocation = defaultSharedPreferences.getString("ntriplocation", "internalfine");
        if (ntriplocation.equals("internalfine")) {
            this.ReportLocationFrom = 1;
        } else if (ntriplocation.equals("auto")) {
            this.ReportLocationFrom = 2;
        } else {
            this.ReportLocationFrom = 0;
            try {
                String ntriplatitude = defaultSharedPreferences.getString("ntriplatitude", "");
                String ntriplongitude = defaultSharedPreferences.getString("ntriplongitude", "");
                this.ManualLat = Double.valueOf(Double.parseDouble(ntriplatitude));
                this.ManualLon = Double.valueOf(Double.parseDouble(ntriplongitude));
            } catch (Exception e) {
            }
        }
        this.maxUserNum = Integer.parseInt(defaultSharedPreferences.getString("usernum", "5"));
        this.NTRIP_Server_IP = defaultSharedPreferences.getString("ntripcasterip", "");
        this.NTRIP_Server_Port = Integer.parseInt(defaultSharedPreferences.getString("ntripcasterport", "0"));
        try {
            String data = defaultSharedPreferences.getString("ntripsourcetable", "");
            Log.i("ntripsourcetable", data);
            NTRIPStreamSource = new StreamSource(data);
            if (NTRIPStreamSource != null) {
                this.NTRIP_Mountpoint = NTRIPStreamSource.getMountPoint();
            }
        } catch (Exception e) {
            this.NTRIPStreamSource = new StreamSource("STR;RTCM32;RTCM32;RTCM 3.2;1074(1),1084(1),1124(1),1005(5),1007(5),1033(5);2;GNSS;EagleGnss;CHN;0.00;0.00;1;1;NtripShare2020;none;B;N;19200;");
            this.NTRIP_Mountpoint = NTRIPStreamSource.getMountPoint();
        }

        this.NTRIP_Username = defaultSharedPreferences.getString("ntripusername", "");
        this.NTRIP_Password = defaultSharedPreferences.getString("ntrippassword", "");
        this.NetworkProtocol = defaultSharedPreferences.getString("networkprotocol", "ntripv1");
        if (!this.NetworkProtocol.equalsIgnoreCase("bluetooth")) {
            BTstop();
        }
        String recode = defaultSharedPreferences.getString(getString(R.string.ResistCode), "");
        ReModel reModel = new ReModel(recode);
        this.ProxyServerIP = reModel.getServerIp();
        this.ProxyServerPort = reModel.getServerPort();
        this.ProxyKey = reModel.getKey();
        this.SslEnabel = false;
        this.mutiUser = Boolean.valueOf(defaultSharedPreferences.getBoolean("multiuser", false));
        if (mutiUser) {
            String userlist = defaultSharedPreferences.getString("userlist", "");
            List<UserModel> userModels = JsonUtil.json2object(userlist, new TypeToken<List<UserModel>>() {
            });
            userModelList.clear();
            if (userModels != null) {
                userModelList.addAll(userModels);
            }
        } else {
            String Caster_Username = defaultSharedPreferences.getString("shareusername", "");
            String Caster_Password = defaultSharedPreferences.getString("sharepassword", "");
            UserModel userModel = new UserModel();
            userModel.setUserName(Caster_Username);
            userModel.setPassword(Caster_Password);
            userModel.setEndTime(DateUtils.StringToDate("2080-12-12 00:00:00", "yyyy-MM-dd HH:mm:ss"));
            userModelList.clear();
            userModelList.add(userModel);
        }

        String string2 = defaultSharedPreferences.getString("bluetooth_mac", "00:00:00:00:00:00");
        if (!string2.equals(this.MACAddress)) {
            if (!this.MACAddress.equals("00:00:00:00:00:00")) {
                LogMessage("BT: Target Device Changed. You will need to Disconnet/Reconnect.");
            }
            this.MACAddress = string2;
            BTstop();
        }
        updateCaster();
    }

    /* access modifiers changed from: private */
    public void LogMessage(String str) {
        Log.i("LogMessage", str + this.DisplayMsgType);
        if (this.logMsgs.length() > 1000) {
            this.logMsgs = this.logMsgs.substring(this.logMsgs.indexOf("\n", this.logMsgs.length() - 500) + 1);
        }
        this.logMsgs += "\n" + str;
        if (this.DisplayMsgType == 0) {
            Bundle bundle = new Bundle();
            bundle.putString("logappend", str);
            for (int size = this.mClients.size() + -1; size >= 0; size--) {
                try {
                    Message obtain = Message.obtain((Handler) null, MSG_UPDATE_LOG_APPEND);
                    obtain.setData(bundle);
                    this.mClients.get(size).send(obtain);
                } catch (RemoteException e) {
                    this.mClients.remove(size);
                }
            }
        }
    }

    private void updateCaster() {
        try {
            if (!this.NetworkProtocol.equals("rawtcpip")) {
                NtripCaster.getInstance().updateMountPointName(ShareApplication.getShareMountpoint(this));
            }
            NtripCaster.getInstance().setUserModel(userModelList);
            NtripCaster.getInstance().setMultiUser(this.mutiUser);
            if (ShareApplication.USER_TYPE == 0) {
                this.maxUserNum = 2;
            }
            NtripCaster.getInstance().setMaxUserNum(this.maxUserNum);
//            NtripCaster.getInstance().setDeiviceID(UTDevice.getUtdid(this));
        } catch (Exception e) {

        }
    }

    private static String Make2Digits(int i) {
        if (i >= 10) {
            return Integer.toString(i);
        }
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("0");
        stringBuilder.append(i);
        return stringBuilder.toString();
    }

    /* access modifiers changed from: private */
    public void ParseNTRIPDataStream(byte[] bArr) {
        int i = this.NetworkDataMode;
        if (i == 0) {
            String str = this.NTRIPResponse + new String(bArr);
            this.NTRIPResponse = str;
            if (str.contains("ICY 200 OK")) {
                if (this.NTRIPStreamSource.getNmea().getNmeaId() == 1) {
                    SendGGAToCaster();
                    updateLocation();
                }
                this.NetworkDataMode = MSG_STARTUP;
                SendConnectStatusToActivity(MSG_CONNECT_SOURCE_OK, getString(R.string.connect_source_server_log));
                LogMessage(getString(R.string.connect_source_server_log));
            } else if (this.NTRIPResponse.contains("401 Unauthorized")) {
                LogMessage(getString(R.string.connect_source_server_error_log));
                SendConnectStatusToActivity(MSG_CONNECT_SOURCE_FAIL, getString(R.string.connect_source_server_error_log));
//                if (this.NTRIP_Username.length() == 0) {
//                    LogMessage("连接数据源服务器，注意用户名不能为空.");
//                } else if (this.NTRIP_Username.startsWith(" ")) {
//                    LogMessage("连接数据源服务器，注意用户名包含空格.");
//                } else if (this.NTRIP_Username.endsWith(" ")) {
//                    LogMessage("连接数据源服务器，注意用户名包含空格.");
//                } else if (this.NTRIP_Password.contains(" ")) {
//                    LogMessage("连接数据源服务器，注意密码包含空格.");
//                }
                TerminateNTRIPThread(false);
                TerminateProxyThread(false);
//                TerminateOTGThread(false);
            } else if (this.NTRIPResponse.contains("SOURCETABLE 200 OK")) {
                LogMessage("NTRIP: Downloading stream list");
                this.NetworkProtocol = "none";
                this.NetworkDataMode = 1;
                this.NTRIPResponse = this.NTRIPResponse.substring(20);
                CheckIfDownloadedSourceTableIsComplete();
            } else if (this.NTRIPResponse.length() > 1024) {
                SendConnectStatusToActivity(MSG_CONNECT_SOURCE_FAIL, getString(R.string.unknow_response));
                LogMessage(getString(R.string.unknow_response));
                LogMessage(this.NTRIPResponse);
                LogMessage(getString(R.string.unknow_response));
                TerminateNTRIPThread(true);
                TerminateProxyThread(true);
//                TerminateOTGThread(false);
            }
        } else if (i == 1) {
            this.NTRIPResponse += new String(bArr);
            CheckIfDownloadedSourceTableIsComplete();
        } else {
            if (this.NetworkReceivedByteCount == 0) {
                if (this.NetworkProtocol.equals("rawtcpip")) {
                    LogMessage(getString(R.string.connect_tcp_server));
                    SendConnectStatusToActivity(MSG_CONNECT_SOURCE_OK, "");
                }
            }
            this.NetworkReceivedByteCount += bArr.length;
            SendByteCountToActivity();
            SendDataToCasterReceiver(bArr);
            SendDataToOTGReceiver(bArr);
            SendDataToBluetooth(bArr);
        }
    }

    /* access modifiers changed from: private */
    public void ParseReceiverDataStream(byte[] bArr) {
        try {
            Log.i("GnssService", "ParseReceiverDataStream");
//            GnssService.getInstance().parseRTCM(bArr);
        } catch (Exception e) {
            Log.i("GnssService", "Exception");
        }
    }

    private void SaveDataDGPSChunk() {
        if (this.DataDGPSToSaveIndex > 0) {
            if (Build.VERSION.SDK_INT >= 23 && ContextCompat.checkSelfPermission(this, "android.permission.WRITE_EXTERNAL_STORAGE") != 0) {
                LogMessage("Permission not yet granted for WRITE_EXTERNAL_STORAGE");
                for (int size = this.mClients.size() - 1; size >= 0; size--) {
                    try {
                        this.mClients.get(size).send(Message.obtain((Handler) null, MSG_REQUEST_FILESAVE, 0, 0));
                    } catch (RemoteException e) {
                        this.mClients.remove(size);
                    }
                }
            }
            try {
                if ("mounted".equals(Environment.getExternalStorageState())) {
                    File externalStorageDirectory = Environment.getExternalStorageDirectory();
                    File file = new File(externalStorageDirectory.getAbsolutePath() + "/NTRIP/");
                    if (!file.exists() && !file.mkdirs()) {
                        LogMessage("Failed to create directory /NTRIP/");
                    }
                    Calendar instance = Calendar.getInstance();
                    int i = instance.get(Calendar.YEAR);
                    int i2 = instance.get(Calendar.MONTH);
                    int i3 = instance.get(Calendar.DAY_OF_MONTH);
                    FileOutputStream fileOutputStream = new FileOutputStream(new File(file, "NTRIP-" + i + "-" + Make2Digits(i2 + 1) + "-" + Make2Digits(i3) + ".txt"), true);
                    fileOutputStream.write(this.DataDGPSToSave, 0, this.DataDGPSToSaveIndex);
                    fileOutputStream.flush();
                    fileOutputStream.close();
                }
            } catch (Exception e2) {
            }
            this.DataDGPSToSaveIndex = 0;
        }
    }

    private void SaveDataNMEAChunk() {
        if (this.DataNMEAToSaveIndex > 0) {
            if (Build.VERSION.SDK_INT >= 23 && ContextCompat.checkSelfPermission(this, "android.permission.WRITE_EXTERNAL_STORAGE") != 0) {
                LogMessage("Permission not yet granted for WRITE_EXTERNAL_STORAGE");
                for (int size = this.mClients.size() - 1; size >= 0; size--) {
                    try {
                        this.mClients.get(size).send(Message.obtain((Handler) null, MSG_REQUEST_FILESAVE, 0, 0));
                    } catch (RemoteException e) {
                        this.mClients.remove(size);
                    }
                }
            }
            try {
                if ("mounted".equals(Environment.getExternalStorageState())) {
                    File externalStorageDirectory = Environment.getExternalStorageDirectory();
                    File file = new File(externalStorageDirectory.getAbsolutePath() + "/NTRIP/");
                    if (!file.exists() && !file.mkdirs()) {
                        LogMessage("Failed to create directory /NTRIP/");
                    }
                    Calendar instance = Calendar.getInstance();
                    int i = instance.get(Calendar.YEAR);
                    int i2 = instance.get(Calendar.MONTH);
                    int i3 = instance.get(Calendar.DAY_OF_MONTH);
                    FileOutputStream fileOutputStream = new FileOutputStream(new File(file, "GPS-" + i + "-" + Make2Digits(i2 + 1) + "-" + Make2Digits(i3) + ".txt"), true);
                    fileOutputStream.write(this.DataNMEAToSave, 0, this.DataNMEAToSaveIndex);
                    fileOutputStream.flush();
                    fileOutputStream.close();
                }
            } catch (Exception e2) {
            }
            this.DataNMEAToSaveIndex = 0;
        }
    }


    /* access modifiers changed from: private */
    public void SendByteCountProgressBarVisibility(int i) {
        for (int size = this.mClients.size() - 1; size >= 0; size--) {
            try {
                this.mClients.get(size).send(Message.obtain((Handler) null, 5, i, 0));
            } catch (RemoteException e) {
                this.mClients.remove(size);
            }
        }
    }

    /* access modifiers changed from: private */
    public void SendByteCountToActivity() {
        for (int size = this.mClients.size() - 1; size >= 0; size--) {
            try {
                this.mClients.get(size).send(Message.obtain((Handler) null, MSG_UPDATE_BYTES_IN, this.NetworkReceivedByteCount, 0));
            } catch (RemoteException e) {
                this.mClients.remove(size);
            }
        }
    }

    /* access modifiers changed from: private */
    public void SendConnectStatusToActivity(int type, String msg) {
        for (int size = this.mClients.size() - 1; size >= 0; size--) {
            try {
                this.mClients.get(size).send(Message.obtain((Handler) null, type, msg));
            } catch (RemoteException e) {
                this.mClients.remove(size);
            }
        }
    }


    /*
    向服务器发送GGA信息
     */
    private void SendGGAToCaster() {
        int i = this.ReportLocationFrom;
        if (i == 0) {
            String gga = GenerateGGAFromLatLon(this.ManualLat.doubleValue() + (Math.random() / 10000), this.ManualLon.doubleValue() + (Math.random() / 10000)) + "\r\n";
            SendDataToNTRIP(gga);
            Log.i("SendGGAToCaste0r", gga);
        } else if (i == 1) {
            String gga = GenerateGGAFromLatLon(this.InternalGPSLat.doubleValue() + (Math.random() / 10000), this.InternalGPSLon.doubleValue() + (Math.random() / 10000)) + "\r\n";
            Log.i("SendGGAToCaste1r", gga);
            SendDataToNTRIP(gga);
        } else if (i == 2) {
            GPSPosition position = NtripCaster.getInstance().getReferenceStation().getCurrentPosition();
            String gga = GenerateGGAFromLatLon(this.InternalGPSLat.doubleValue() + (Math.random() / 10000), this.InternalGPSLon.doubleValue() + (Math.random() / 10000)) + "\r\n";
            if (position != null) {
                gga = GenerateGGAFromLatLon(position.lat + (Math.random() / 10000), position.lon + (Math.random() / 10000)) + "\r\n";
            }
            Log.i("SendAutoGGAToCaste1r", gga);
            SendDataToNTRIP(gga);
        } else {
            SendDataToNTRIP(this.MostRecentGGA + "\r\n");
        }
        this.NTRIPTicksSinceGGASent = 0;
    }

    /*
    更新位置信息
     */
    private void updateLocation() {
        int i = this.ReportLocationFrom;
        Bundle bundle = new Bundle();

        if (i == 0) {
            bundle.putDouble("Lat", this.ManualLat.doubleValue());
            bundle.putDouble("Lon", this.ManualLon.doubleValue());
        } else if (i >= 1) {
            bundle.putDouble("Lat", this.InternalGPSLat.doubleValue());
            bundle.putDouble("Lon", this.InternalGPSLon.doubleValue());
        }
        for (int size = this.mClients.size() + -1; size >= 0; size--) {
            try {
                Message obtain = Message.obtain((Handler) null, MSG_UPDATE_LOCATION);
                obtain.setData(bundle);
                this.mClients.get(size).send(obtain);
            } catch (RemoteException e) {
                this.mClients.remove(size);
            }
        }
    }

    /* access modifiers changed from: private */
    public void SetDisplayMsgType(int i) {
        if (this.logNMEA.length() == 0 && i == 1) {
            i = 0;
        }
        if (this.DisplayMsgType != i) {
            this.DisplayMsgType = i;
            sendAllLogMessagesToUI();
        }
    }

    private void StartDataHandlerThread() {
        this.dataHandlerThread.start();
        this.dataHandler = new Handler(this.dataHandlerThread.getLooper()) {
            public void handleMessage(Message message) {
                int i = message.what;
                switch (i) {
                    case NTRIPService.MSG_STARTUP /*99*/:
                        NTRIPService.this.LoadPreferences();
                        NTRIPService.this.mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
                        return;
                    case NTRIPService.MSG_TIMER_TICK /*100*/:
                        NTRIPService.this.onTimerTick();
                        return;
                    case NTRIPService.MSG_NETWORK_GOT_DATA /*101*/:
                        NTRIPService.this.ParseNTRIPDataStream((byte[]) message.obj);
                        return;
                    case NTRIPService.MSG_NETWORK_TIMEOUT /*198*/:
                        int unused2 = NTRIPService.this.NetworkReceivedByteCount = 0;
                        NTRIPService.this.SendByteCountToActivity();
                        if (!NTRIPService.this.NetworkProtocol.equals("none")) {
                            NTRIPService.this.LogMessage(getString(R.string.connect_source_server_fail));
                            return;
                        }
                        return;
                    case NTRIPService.MSG_NETWORK_FINISHED /*199*/:
                        Boolean unused3 = NTRIPService.this.NetworkIsConnected = false;
                        int unused4 = NTRIPService.this.NetworkReConnectInTicks = 2;
                        int unused5 = NTRIPService.this.NetworkReceivedByteCount = 0;
                        NTRIPService.this.SendByteCountToActivity();
                        NTRIPService.this.LogMessage(getString(R.string.connect_source_disconnect));
                        SendConnectStatusToActivity(MSG_CONNECT_SOURCE_FAIL, getString(R.string.connect_source_disconnect));
                        return;
                    case NTRIPService.MSG_NODE_NETWORK_FINISHED /*199*/:
                        NetPointModel netPointModel = (NetPointModel) message.obj;
                        NtripCaster.getInstance().getReferenceStation().removeNetPoint(netPointModel.getUuid());
                        NTRIPService.this.LogMessage(netPointModel.getConnectionStr() + getString(R.string.connect_source_disconnect));
                        TerminateNetNodeThread(netPointModel.getUuid());
//                        SendConnectStatusToActivity(MSG_CONNECT_NODE_SOURCE_FAIL, getString(R.string.connect_source_disconnect));
//                        NTRIPService.this.LogMessage(netPointModel.getConnectionStr()+ getString(R.string.connect_source_disconnect));
                        return;
                    case NTRIPService.MSG_LOG_MESSAGE /*200*/:
                        NTRIPService.this.LogMessage((String) message.obj);
                        return;
                    case NTRIPService.MSG_BT_GOT_DATA /*201*/:
                        if (ShareApplication.getSourceType(NTRIPService.this) == ShareApplication.SOURCE_TYP.BLUETOOTH) {
                            NTRIPService.this.NetworkReceivedByteCount += ((byte[]) message.obj).length;
                            NTRIPService.this.SendByteCountToActivity();
                            SendDataToCasterReceiver((byte[]) message.obj);
                            SendDataToOTGReceiver((byte[]) message.obj);
                        }
                        break;
                    case NTRIPService.MSG_BT_FINISHED /*201*/:
                        NTRIPService.this.BTstop();
                        break;
                    default:
                        return;
                }
//                NTRIPService.this.ParseReceiverDataStream((byte[]) message.obj);
                return;
            }
        };
    }

    private void TerminateNetThread(boolean z) {
        netStart = false;
        try {
            List<NetPointModel> list = ShareApplication.getNetPointList(this);
            for (NetPointModel netPointModel : list) {
                TerminateNetNodeThread(netPointModel.getUuid());
            }
        } catch (Exception e) {

        }

    }

    private void TerminateNetNodeThread(String uuid) {
        Log.i("TerminateNetNodeThread ", uuid);
        try {
            if (netNodeClientMap.containsKey(uuid)) {
                if (netNodeClientMap.get(uuid).mNTRIPInputStream != null) {
                    netNodeClientMap.get(uuid).mNTRIPInputStream.close();
                }
                if (netNodeClientMap.get(uuid).mNTRIPOutputStream != null) {
                    netNodeClientMap.get(uuid).mNTRIPOutputStream.close();
                }
                if (netNodeClientMap.get(uuid).mNTRIPSocket != null) {
                    netNodeClientMap.get(uuid).mNTRIPSocket.close();
                }
                netNodeClientMap.remove(uuid);
            }
            if (netNodeTheadMap.containsKey(uuid)) {
                if (netNodeTheadMap.get(uuid) != null) {
                    netNodeTheadMap.get(uuid).interrupt();
                }
                netNodeTheadMap.remove(uuid);
            }
        } catch (Exception e) {
            Log.i("TerminateNetNodeThread ", e.getMessage());
        }

    }

    private void TerminateNTRIPThread(boolean z) {
        if (this.NTRIPThread != null) {
            try {
                if (this.NTRIPInputStream != null) {
                    this.NTRIPInputStream.close();
                }
                if (this.NTRIPOutputStream != null) {
                    this.NTRIPOutputStream.close();
                }
                if (this.NTRIPSocket != null) {
                    this.NTRIPSocket.close();
                }
            } catch (Exception e) {
            }
            Thread thread = this.NTRIPThread;
            this.NTRIPThread = null;
            thread.interrupt();
        }
        this.NetworkReceivedByteCount = 0;
        SendByteCountProgressBarVisibility(0);
        SendByteCountToActivity();
        if (z) {
            this.NetworkReConnectInTicks = 2;
        } else {
//            this.NetworkProtocol = "none";
        }
    }

    private void TerminateProxyThread(boolean z) {
        if (this.ProxyThread != null) {
            try {
                ContainerHelper.stop();
            } catch (Exception e) {
            }
            Thread thread = this.ProxyThread;
            this.ProxyThread = null;
            thread.interrupt();
        }
    }

    public static String TheTimeIs() {
        Calendar instance = Calendar.getInstance();
        int i = instance.get(Calendar.HOUR_OF_DAY);
        int i2 = instance.get(Calendar.MINUTE);
        int i3 = instance.get(Calendar.SECOND);
        return Make2Digits(i) + ":" + Make2Digits(i2) + ":" + Make2Digits(i3) + " ";
    }

    public static boolean isRunning() {
        return isRunning;
    }

    public int getBTState() {
        int i;
        synchronized (this) {
            i = this.mBTState;
        }
        return i;
    }

    private boolean netStart = false;
    Map<String, Thread> netNodeTheadMap = new HashMap<>();
    Map<String, NetworkNodeClient> netNodeClientMap = new HashMap<>();
    private int netNum = 0;

    public List<NetPointModel> getOnLineNetNodes() {
        List<NetPointModel> re = new ArrayList<>();
        Iterator<Map.Entry<String, NetworkNodeClient>> entries = netNodeClientMap.entrySet().iterator();
        while (entries.hasNext()) {
            Map.Entry<String, NetworkNodeClient> entry = entries.next();
            NetworkNodeClient value = entry.getValue();
            if (value.mNetworkDataMode == MSG_STARTUP) {
                re.add(value.mNetPointModel);
            }
        }
        return re;
    }

    /* access modifiers changed from: private */
    public void onTimerTick() {
        int i2 = this.TicksSinceLastStatusSent + 1;
        this.TicksSinceLastStatusSent = i2;
        this.mNotification.iconLevel = 10;
        this.mNotificationManager.notify(123, this.mNotification);
        if (ShareApplication.getSourceType(this) == ShareApplication.SOURCE_TYP.NTRRIP_NET) {
            netNum++;
            if (!netStart) {
                NtripCaster.getInstance().getReferenceStation().setNetMode(true);
                List<NetPointModel> list = ShareApplication.getNetPointList(this);
                for (NetPointModel netPointModel : list) {
                    NetworkNodeClient networkNodeClient = new NetworkNodeClient(netPointModel);
                    Thread thread2 = new Thread(networkNodeClient);
                    netNodeTheadMap.put(netPointModel.getUuid(), thread2);
                    netNodeClientMap.put(netPointModel.getUuid(), networkNodeClient);
                    thread2.start();
                }
                if (this.ProxyThread == null) {
                    Thread thread3 = new Thread(new ProxyThread(this.ProxyServerIP, this.ProxyServerPort, this.ProxyKey, this.SslEnabel));
                    this.ProxyThread = thread3;
                    thread3.start();
                }
                this.NetworkIsConnected = true;
                netStart = true;
            } else {
                try {
                    Iterator<Map.Entry<String, NetworkNodeClient>> entries = netNodeClientMap.entrySet().iterator();
                    while (entries.hasNext()) {
                        Map.Entry<String, NetworkNodeClient> entry = entries.next();
                        NetworkNodeClient value = entry.getValue();
                        if (value.mNetworkDataMode == MSG_STARTUP) {
                            if (value.dataTime + 5000 > System.currentTimeMillis()) {
                                value.SendNodeGGAToCaster();
                            } else {
                                NetPointModel netPointModel = value.mNetPointModel;
                                TerminateNetNodeThread(netPointModel.getUuid());
                            }
                        }
                    }
                    if (netNum % 5 == 0) {
                        List<NetPointModel> list = ShareApplication.getNetPointList(this);
                        for (NetPointModel netPointModel : list) {
                            if (!netNodeTheadMap.containsKey(netPointModel.getUuid())) {
                                NetworkNodeClient networkNodeClient = new NetworkNodeClient(netPointModel);
                                Thread thread2 = new Thread(networkNodeClient);
                                netNodeTheadMap.put(netPointModel.getUuid(), thread2);
                                netNodeClientMap.put(netPointModel.getUuid(), networkNodeClient);
                                thread2.start();
                            }
                        }
                    }
                } catch (Exception e) {

                }
            }
        } else if (ShareApplication.getSourceType(this) == ShareApplication.SOURCE_TYP.OTG) {
            NtripCaster.getInstance().getReferenceStation().setNetMode(false);
            if (!this.NetworkIsConnected) {
                int i8 = this.NetworkReConnectInTicks - 1;
                this.NetworkReConnectInTicks = i8;
                if (i8 == 0) {
                    int i9 = this.NetworkConnectionAttempts + 1;
                    this.NetworkConnectionAttempts = i9;

                    if (this.ProxyThread == null) {
                        Thread thread3 = new Thread(new ProxyThread(this.ProxyServerIP, this.ProxyServerPort, this.ProxyKey, this.SslEnabel));
                        this.ProxyThread = thread3;
                        thread3.start();
                    }
                    this.NetworkIsConnected = true;
                }
            }
        } else if (ShareApplication.getSourceType(this) == ShareApplication.SOURCE_TYP.BLUETOOTH) {
            NtripCaster.getInstance().getReferenceStation().setNetMode(false);
            if (getBTState() == 0) {
                BluetoothAdapter bluetoothAdapter = this.mBluetoothAdapter;
                Timer timer;
                if (bluetoothAdapter == null) {
                    Toast.makeText(this, R.string.no_blutooth_support, Toast.LENGTH_SHORT).show();
                    LogMessage(getString(R.string.no_blutooth_support));
                    isRunning = false;
                    InformActivityOfThreadSuicide();
                    timer = this.timer;
                    if (timer != null) {
                        timer.cancel();
                    }
                    StopThisService();
                } else if (bluetoothAdapter.isEnabled()) {
                    BTstart();
                    if (this.ProxyThread == null) {
                        Thread thread3 = new Thread(new ProxyThread(this.ProxyServerIP, this.ProxyServerPort, this.ProxyKey, this.SslEnabel));
                        this.ProxyThread = thread3;
                        thread3.start();
                    }
                    this.NetworkIsConnected = true;
                } else {
                    Toast.makeText(this, R.string.confirm_blutooth_open, Toast.LENGTH_SHORT).show();
                    LogMessage(getString(R.string.confirm_blutooth_open));
                    isRunning = false;
                    InformActivityOfThreadSuicide();
                    timer = this.timer;
                    if (timer != null) {
                        timer.cancel();
                    }
                    StopThisService();
                }
            }
        } else if (!this.NetworkProtocol.equals("none") && !this.NetworkIsConnected && (this.NetworkReConnectInTicks > 0)) {
//            NtripCaster.getInstance().getReferenceStation().setNetMode(false);
            int i8 = this.NetworkReConnectInTicks - 1;
            this.NetworkReConnectInTicks = i8;
            if (i8 == 0) {
                int i9 = this.NetworkConnectionAttempts + 1;
                this.NetworkConnectionAttempts = i9;
                if (i9 == 1) {
                    LogMessage(getString(R.string.connecting_source));
                } else {
                    LogMessage(getString(R.string.connecting_source) + " Attempt " + this.NetworkConnectionAttempts);
                }
                this.NTRIPResponse = "";
                this.NetworkReceivedByteCount = 0;
                this.NetworkDataMode = 0;
                if (this.NetworkProtocol.equals("rawtcpip")) {
                    this.NetworkDataMode = MSG_STARTUP;
                }
                Thread thread2 = new Thread(new NetworkClient(this.NetworkProtocol, this.NTRIP_Server_IP, this.NTRIP_Server_Port,
                        this.NTRIP_Mountpoint, this.NTRIP_Username, this.NTRIP_Password));
                this.NTRIPThread = thread2;
                thread2.start();

                if (this.ProxyThread == null) {
                    Thread thread3 = new Thread(new ProxyThread(this.ProxyServerIP, this.ProxyServerPort, this.ProxyKey, this.SslEnabel));
                    this.ProxyThread = thread3;
                    thread3.start();
                }

                this.NetworkIsConnected = true;
            }
        }
        Log.i("BTstart", mBluetoothOutput + " " + getBTState() + " " + (System.currentTimeMillis() / 1000) / 10);
        if (mBluetoothOutput && getBTState() == STATE_NONE && (System.currentTimeMillis() / 1000) % 10 == 0) {
            if (mBluetoothAdapter == null) {
                LogMessage(getString(R.string.no_blutooth_support));
            } else if (mBluetoothAdapter.isEnabled()) {
                BTstart();
            } else {
                LogMessage(getString(R.string.confirm_blutooth_open));
            }
        }
        if (this.NetworkIsConnected.booleanValue() && this.NTRIPStreamSource.getNmea().getNmeaId() == 1) {
            int i10 = this.NTRIPTicksSinceGGASent + 1;
            this.NTRIPTicksSinceGGASent = i10;
            if (i10 >= this.NTRIPSendGGAEveryXSeconds) {
                SendGGAToCaster();
                updateLocation();
            }
        }
    }

    /* access modifiers changed from: private */
    public void onTimerTick_TimerThread() {
        this.dataHandler.sendMessage(Message.obtain((Handler) null, MSG_TIMER_TICK));
    }

    /* access modifiers changed from: private */
    public void sendAllLogMessagesToUI() {
        Bundle bundle = new Bundle();
        if (this.DisplayMsgType == 1) {
            bundle.putString("logfull", this.logNMEA);
        } else {
            bundle.putString("logfull", this.logMsgs);
        }
        for (int size = this.mClients.size() - 1; size >= 0; size--) {
            try {
                Message obtain = Message.obtain((Handler) null, 7);
                obtain.setData(bundle);
                this.mClients.get(size).send(obtain);
            } catch (RemoteException e) {
                this.mClients.remove(size);
            }
        }
    }


    public void SendDataToNTRIP(final String str) {
        Socket socket = this.NTRIPSocket;
        if (socket != null && socket.isConnected()) {
            new Thread(new Runnable() {
                public void run() {
                    try {
                        NTRIPService.this.NTRIPOutputStream.write(str.getBytes());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        }
    }

    public void SendDataToCasterReceiver(final byte[] bArr) {
        try {
            NtripCaster ntripCaster = NtripCaster.getInstance();
            if (ntripCaster != null) {
                ReferenceStation referenceStation = NtripCaster.getInstance().getReferenceStation();
                if (referenceStation != null) {
                    NtripCaster.getInstance().getReferenceStation().pushData(bArr);
                    NtripCaster.getInstance().getReferenceStation().run();
                }

                ParseReceiverDataStream(bArr);
            } else {
                int mm = 1;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void SendDataToOTGReceiver(final byte[] bArr) {
        try {
            if (ShareApplication.isOtgOutPut(this)) {
                UsbService usbService = MainActivity.usbService;
                if (usbService != null) {
                    usbService.write(bArr);
                } else {
                    int mm = 1;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void StopThisService() {
        if (Build.VERSION.SDK_INT >= 26) {
            stopForeground(true);
        } else {
            stopSelf();
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        INSTANCE = this;
        return this.inMessenger.getBinder();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        isRunning = true;
        if (Build.VERSION.SDK_INT >= 26) {
            NotificationChannel notificationChannel = new NotificationChannel(BuildConfig.APPLICATION_ID, "NTRIP Service", NotificationManager.IMPORTANCE_NONE);
            notificationChannel.setLockscreenVisibility(1);
            NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            this.mNotificationManager = notificationManager;
            notificationManager.createNotificationChannel(notificationChannel);
            Notification build = new NotificationCompat.Builder(this, BuildConfig.APPLICATION_ID).setOngoing(true).
                    setSmallIcon(R.drawable.ic_launcher).setContentTitle(getText(R.string.app_name)).setContentText(getText(R.string.app_name) + getString(R.string.is_running))
                    .setAutoCancel(false).setPriority(PRIORITY_MAX).setCategory(NotificationCompat.CATEGORY_SERVICE).setContentIntent(PendingIntent.getActivity(this,
                            0, new Intent(this, MainActivity.class), PendingIntent.FLAG_ONE_SHOT)).build();
            this.mNotification = build;
            startForeground(123, build);
        } else {
            NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "0");
            builder.setSmallIcon(R.drawable.ic_launcher);
            builder.setContentTitle(getText(R.string.app_name));
            builder.setContentText(getText(R.string.app_name) + getString(R.string.is_running));
            builder.setAutoCancel(false);
            builder.setContentIntent(PendingIntent.getActivity(this, 0, new Intent(this, MainActivity.class), 0));
            builder.setOngoing(true);
            this.mNotification = builder.build();
            NotificationManager notificationManager2 = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            this.mNotificationManager = notificationManager2;
            if (notificationManager2 != null) {
                try {
                    notificationManager2.cancelAll();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                this.mNotificationManager.notify(123, this.mNotification);
            }
        }
        StartDataHandlerThread();
        this.dataHandler.sendMessage(Message.obtain((Handler) null, MSG_STARTUP));
        this.timer.scheduleAtFixedRate(new TimerTask() {
            public void run() {
                NTRIPService.this.onTimerTick_TimerThread();
            }
        }, 0, 1000);
        PowerManager powerManager2 = (PowerManager) getSystemService(POWER_SERVICE);
        this.powerManager = powerManager2;
        if (powerManager2 != null) {
            this.wakeLock = powerManager2.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "NTRIP::MyWakelockTag");
        }
        this.wakeLock.acquire(43200000);
        WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
        if (this.mWifiLock == null && wifiManager != null) {
            this.mWifiLock = wifiManager.createWifiLock(PowerManager.PARTIAL_WAKE_LOCK, "tag");
        }
        WifiManager.WifiLock wifiLock = this.mWifiLock;
        if (wifiLock != null) {
            wifiLock.setReferenceCounted(false);
            if (!this.mWifiLock.isHeld()) {
                this.mWifiLock.acquire();
            }
        }

        IntentFilter intentfilter = new IntentFilter();
        intentfilter.addAction(Intent.ACTION_SCREEN_ON);
        intentfilter.addAction(Intent.ACTION_SCREEN_OFF);
        intentfilter.addAction(Intent.ACTION_USER_PRESENT);
        mreceiver = new Mreceiver();
        registerReceiver(mreceiver, intentfilter);

        IntentFilter intentFile = new IntentFilter();
        intentFile.addAction("repeating");
        locationReceiver = new LocationReceiver();
        registerReceiver(locationReceiver, intentFile);
        Intent intent = new Intent();
        intent.setAction("repeating");
        pi = PendingIntent.getBroadcast(this, 0, intent, 0);
        am = (AlarmManager) getSystemService(ALARM_SERVICE);


        GnssService.getInstance().setRTCMParserListener(new RTCMParserListener() {

            @Override
            public void onSARP(com.giserpeng.ntripshare.gnss.bean.ReferenceStation station) {
                Log.i("GnssService", station.getLon() + "-" + station.getLat());
            }

            @Override
            public void onGNSS(GnssData data) {
                Log.i("GnssService", "onGNSS");
                // updateSatellite(data);
            }
        });
    }

    private AlarmManager am;
    private PendingIntent pi;
    private Mreceiver mreceiver;
    private LocationReceiver locationReceiver;

    private void wake() {
        wakeLock.acquire();
        am.cancel(pi);
        am.set(AlarmManager.RTC_WAKEUP, SystemClock.elapsedRealtime() + 10000, pi);
    }

    class LocationReceiver extends BroadcastReceiver {
        @SuppressLint("NewApi")
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.v("onReceive", "open");
            reLosition();
        }

        public void reLosition() {
            am.cancel(pi);
            am.set(AlarmManager.RTC_WAKEUP, SystemClock.elapsedRealtime() + 10000, pi);
            if (NTRIPService.this.KeepScreenOn) {
                PowerManager powerManager = (PowerManager) getApplicationContext().getSystemService(Context.POWER_SERVICE);
                @SuppressLint("InvalidWakeLockTag")
                PowerManager.WakeLock wl = powerManager.newWakeLock(PowerManager.ACQUIRE_CAUSES_WAKEUP | PowerManager.SCREEN_DIM_WAKE_LOCK, "bright");
                wl.acquire();
                //点亮屏幕
                wl.release();
            }
        }
    }

    public class Mreceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (Intent.ACTION_SCREEN_ON.equals(action)) {
                Log.v("sunlei", "开屏");
            }//锁屏
            else if (intent.ACTION_SCREEN_OFF.equals(action)) {
                Log.v("sunlei", "锁屏");
//如果锁屏关闭当前常规定位方法，调用alarm,每2秒发动一次单次定位
                if (android.os.Build.VERSION.SDK_INT >= 24) {
                    wake();
                }
            }//解锁
            else if (intent.ACTION_USER_PRESENT.equals(action)) {
                Log.v("sunlei", "解锁");
            }
        }

    }

    @Override
    public void onDestroy() {
        SaveDataNMEAChunk();
        SaveDataDGPSChunk();
        Timer timer2 = this.timer;
        if (timer2 != null) {
            timer2.cancel();
        }
        TerminateNTRIPThread(false);
        TerminateProxyThread(false);
        TerminateNetThread(false);
        this.dataHandlerThread.interrupt();

        NotificationManager notificationManager = this.mNotificationManager;
        if (notificationManager != null) {
            try {
                notificationManager.cancelAll();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        try {
            this.wakeLock.release();
            WifiManager.WifiLock wifiLock = this.mWifiLock;
            if (wifiLock != null && wifiLock.isHeld()) {
                this.mWifiLock.release();
            }
            stopForeground(true);
        } catch (Exception e) {

        }

        isRunning = false;
        unregisterReceiver(mreceiver);
        unregisterReceiver(locationReceiver);
        BTstop();
        super.onDestroy();
    }

    @Override
    public int onStartCommand(Intent intent, int i, int i2) {
        return Service.START_STICKY;
    }
}
