package com.giserpeng.ntripshare.ui.setting;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.preference.CheckBoxPreference;
import androidx.preference.EditTextPreference;
import androidx.preference.ListPreference;
import androidx.preference.MultiSelectListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceGroup;

import com.baidu.mapapi.model.LatLng;
import com.giserpeng.ntripshare.MainActivity;
import com.giserpeng.ntripshare.R;
import com.giserpeng.ntripshare.ShareApplication;
import com.giserpeng.ntripshare.ntrip.CasterClient;
import com.giserpeng.ntripshare.ntrip.source.NtripSource;
import com.giserpeng.ntripshare.ntrip.source.ServerInfo;
import com.giserpeng.ntripshare.ntrip.source.StreamSource;
import com.giserpeng.ntripshare.re.ReModel;
import com.giserpeng.ntripshare.update.UpdateManager;
import com.giserpeng.ntripshare.util.DateUtils;
import com.giserpeng.ntripshare.util.EncryptUtil;
import com.giserpeng.ntripshare.util.NetUtils;
import com.giserpeng.ntripshare.util.ToolUtil;
import com.inuker.bluetooth.library.search.SearchResult;
import com.ut.device.UTDevice;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

import okhttp3.Response;

public class SettingFragment extends PreferenceFragmentCompat {

    ListPreference pBaudRate;
    ListPreference pnetworkprotocol;
    ListPreference maptype;
    Preference pntripcasterip;
    EditTextPreference pntripcasterport;
    EditTextPreference pntriplatitude;
    Preference pntriplocation;
    EditTextPreference pntriplongitude;
    EditTextPreference pbluetooth_mac;
    Preference pntrippassword;
    ListPreference pntripsourcetable;
    ListPreference pserver;
    Preference pntripusername;
    Preference pshareusername;
    Preference psharepassword;
    EditTextPreference pusernum;
    EditTextPreference pmaxDis;
    EditTextPreference ggaTime;
    EditTextPreference psharemountpoint;
    CheckBoxPreference multiuser;
    CheckBoxPreference otgOut;
    CheckBoxPreference bluetoothOut;
    PreferenceGroup sourceGroup;
    PreferenceGroup otgGroup;
    PreferenceGroup bluetoothGroup;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);
        view.setBackground(new ColorDrawable(Color.WHITE));
        return view;
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.preferences, rootKey);
        this.sourceGroup = (PreferenceGroup) findPreference("data_source");
        this.otgGroup = (PreferenceGroup) findPreference("otg_setting");
        this.bluetoothGroup = (PreferenceGroup) findPreference("bluetooth_setting");
        this.pnetworkprotocol = (ListPreference) findPreference("networkprotocol");
        updateSummarys(this.pnetworkprotocol);
        this.pnetworkprotocol.setOnPreferenceChangeListener(sBindPreferenceSummaryToValueListener);
        this.pBaudRate = (ListPreference) findPreference("pBaudRate");
        updateSummarys(this.pBaudRate);
        this.pBaudRate.setOnPreferenceChangeListener(sBindPreferenceSummaryToValueListener);
        this.multiuser = (CheckBoxPreference) findPreference("multiuser");
        updateSummarys(this.multiuser);
        this.multiuser.setOnPreferenceChangeListener(sBindPreferenceSummaryToValueListener);

        this.otgOut = (CheckBoxPreference) findPreference("otgOut");
        updateSummarys(this.otgOut);
        this.otgOut.setOnPreferenceChangeListener(sBindPreferenceSummaryToValueListener);

        this.bluetoothOut = (CheckBoxPreference) findPreference("bluetoothOut");
        updateSummarys(this.bluetoothOut);
        this.bluetoothOut.setOnPreferenceChangeListener(sBindPreferenceSummaryToValueListener);

        this.maptype = (ListPreference) findPreference("maptype");
        updateSummarys(this.maptype);
        this.maptype.setOnPreferenceChangeListener(sBindPreferenceSummaryToValueListener);

        this.pserver = (ListPreference) findPreference("sel_server");
        updateSummarys(this.pserver);
//        this.pserver.setOnPreferenceChangeListener(sBindPreferenceSummaryToValueListener);
        this.pserver.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                ListPreference listPreference = (ListPreference) preference;
                int index = listPreference.findIndexOfValue(newValue.toString());
                preference.setSummary(index >= 0 ? listPreference.getEntries()[index] : null);

                int num1 = Integer.valueOf(newValue.toString()) - 1;
                if (num1 == -1) {
                    String uuid = UTDevice.getUtdid(getActivity());
                    BigInteger bigInteger = null;
                    try {
                        bigInteger = new BigInteger(uuid.getBytes("UTF-8"));
                        num1 = bigInteger.intValue() % 5;
                        num1 =0;
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                        num1 = 0;
                    }
                }
                final int num = num1;
                Thread thread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            final int versionCode = UpdateManager.getVersionCode(getContext());
                            final List<Double> coord = ShareApplication.getLastPosition(getContext());
//                            String uuid = UTDevice.getUtdid(getContext());
//                            BigInteger bigInteger = new BigInteger(uuid.getBytes("UTF-8"));
//                            int num = bigInteger.intValue() % 5;
                            String data = getContext().getString(R.string.platform_name) + "/" + versionCode + "-" + DateUtils.dateToString(new Date(), "yyyyMMddHHmmss") + "-" + UTDevice.getUtdid(getContext());
                         String url = getContext().getString(R.string.URL_VERSION_PRO).replace("pro0", "pro" + num)
                                    + "?deviceCode=" + EncryptUtil.encrypt(data) + "&lon1=" + coord.get(0).toString() + "&lat=" + coord.get(1).toString();
                            if (num == 5) {
                                url = getContext().getString(R.string.URL_VERSION) + "?deviceCode=" + EncryptUtil.encrypt(data) + "&lon1=" + coord.get(0).toString() + "&lat=" + coord.get(1).toString();
                            }
                            Log.i("checkUpdate", url);
                            Response response = NetUtils.get(url);
                            final JSONObject jsonObject1 = new JSONObject(response.body().string());
                            final  String ur = url;
                            Log.i("checkUpdate", jsonObject1.toString());
                            if (jsonObject1.getBoolean("success")) {

                                getActivity().runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        try {
                                            if (null != jsonObject1) {
                                                ShareApplication.updateYuMing(getActivity(),NetUtils.getYuMing(ur));
                                                String key = jsonObject1.getJSONObject("data").getString("key");
                                                SharedPreferences defaultSharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
                                                SharedPreferences.Editor editor = defaultSharedPreferences.edit();
                                                editor.putString(getString(R.string.ResistCode), key);
                                                editor.commit();
                                                Log.i("checkUpdate" + num, "切换服务器成功！");
                                                Toast.makeText(getActivity(), "切换服务器成功！", Toast.LENGTH_LONG).show();
                                            }

                                        } catch (Exception e) {
                                            Toast.makeText(getActivity(), "发生错误，切换服务器失败！", Toast.LENGTH_LONG).show();
                                            Log.i("checkUpdate" + num, e.getMessage());
                                        }
                                    }
                                });

                            }
                        } catch (Exception e) {
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(getContext(), "发生错误，切换服务器失败！", Toast.LENGTH_LONG).show();
                                    Log.i("checkUpdate", e.getMessage());
                                }
                            });
                        }
                    }
                });
                thread.start();
                return true;
            }
        });
        this.pserver.setVisible(false);

        this.pntripcasterip = findPreference("ntripcasterip");
        updateSummarys(this.pntripcasterip);
        this.pntripcasterip.setOnPreferenceChangeListener(sBindPreferenceSummaryToValueListener);

        this.psharemountpoint = findPreference("sharemountpoint");
        updateSummarys(this.psharemountpoint);
        this.psharemountpoint.setOnPreferenceChangeListener(sBindPreferenceSummaryToValueListener);

        this.ggaTime = findPreference("ggaTime");
        this.ggaTime.setOnBindEditTextListener(new EditTextPreference.OnBindEditTextListener() {
            @Override
            public void onBindEditText(@NonNull EditText editText) {
                editText.setInputType(InputType.TYPE_CLASS_NUMBER);
            }
        });
        updateSummarys(this.ggaTime);
        this.ggaTime.setOnPreferenceChangeListener(sBindPreferenceSummaryToValueListener);

        this.pntripcasterport = findPreference("ntripcasterport");
        this.pntripcasterport.setOnBindEditTextListener(new EditTextPreference.OnBindEditTextListener() {
            @Override
            public void onBindEditText(@NonNull EditText editText) {
                editText.setInputType(InputType.TYPE_CLASS_NUMBER);
            }
        });
        updateSummarys(this.pntripcasterport);
        this.pntripcasterport.setOnPreferenceChangeListener(sBindPreferenceSummaryToValueListener);
        this.pntripusername = findPreference("ntripusername");
        updateSummarys(this.pntripusername);
        this.pntripusername.setOnPreferenceChangeListener(sBindPreferenceSummaryToValueListener);
        this.pntrippassword = findPreference("ntrippassword");
        updateSummarys(this.pntrippassword);
        this.pntrippassword.setOnPreferenceChangeListener(sBindPreferenceSummaryToValueListener);
        this.pntripsourcetable = (ListPreference) findPreference("ntripsourcetable");
        updateSummarys(this.pntripsourcetable);
        this.pntripsourcetable.setOnPreferenceChangeListener(sBindPreferenceSummaryToValueListener);

        this.pntriplocation = findPreference("ntriplocation");
        updateSummarys(this.pntriplocation);

        this.pntriplocation.setOnPreferenceChangeListener(sBindPreferenceSummaryToValueListener);
        this.pntriplongitude = findPreference("ntriplongitude");
        this.pntriplongitude.setOnBindEditTextListener(new EditTextPreference.OnBindEditTextListener() {
            @Override
            public void onBindEditText(@NonNull EditText editText) {
                editText.setInputType(InputType.TYPE_CLASS_NUMBER);
            }
        });
        updateSummarys(this.pntriplongitude);
        this.pntriplongitude.setOnPreferenceChangeListener(sBindPreferenceSummaryToValueListener);
        this.pntriplatitude = findPreference("ntriplatitude");
        this.pntriplatitude.setOnBindEditTextListener(new EditTextPreference.OnBindEditTextListener() {
            @Override
            public void onBindEditText(@NonNull EditText editText) {
                editText.setInputType(InputType.TYPE_CLASS_NUMBER);
            }
        });
        updateSummarys(this.pntriplatitude);
        this.pntriplatitude.setOnPreferenceChangeListener(sBindPreferenceSummaryToValueListener);
        this.pshareusername = findPreference("shareusername");
        updateSummarys(this.pshareusername);
        this.pshareusername.setOnPreferenceChangeListener(sBindPreferenceSummaryToValueListener);
        this.psharepassword = findPreference("sharepassword");
        updateSummarys(this.psharepassword);
        this.psharepassword.setOnPreferenceChangeListener(sBindPreferenceSummaryToValueListener);
        this.pusernum = findPreference("usernum");
        this.pusernum.setOnBindEditTextListener(new EditTextPreference.OnBindEditTextListener() {
            @Override
            public void onBindEditText(@NonNull EditText editText) {
                editText.setInputType(InputType.TYPE_CLASS_NUMBER);
            }
        });
        this.pusernum.setEnabled(false);
        updateSummarys(this.pusernum);
        this.pusernum.setOnPreferenceChangeListener(sBindPreferenceSummaryToValueListener);
        this.pmaxDis = findPreference("maxDis");
        this.pmaxDis.setOnBindEditTextListener(new EditTextPreference.OnBindEditTextListener() {
            @Override
            public void onBindEditText(@NonNull EditText editText) {
                editText.setInputType(InputType.TYPE_CLASS_NUMBER);
            }
        });
        updateSummarys(this.pmaxDis);
        this.pmaxDis.setOnPreferenceChangeListener(sBindPreferenceSummaryToValueListener);

        this.pbluetooth_mac = findPreference("bluetooth_mac");
        updateSummarys(this.pbluetooth_mac);
        this.pbluetooth_mac.setOnPreferenceChangeListener(sBindPreferenceSummaryToValueListener);

        String ip = ((EditTextPreference) pntripcasterip).getText();
        int port = 0;
        try {
            port = Integer.parseInt(((EditTextPreference) pntripcasterport).getText());
        } catch (Exception e) {
            Toast.makeText(getContext(), R.string.port_error, Toast.LENGTH_LONG).show();
        }

        String userName = "userName";
        String password = "password";
        if (ip != null && port != 0) {
            Log.i("updateMountPoint", "updateMountPoint");
            updateMountPoint(ip, port, userName, password);
        }

        SharedPreferences defaultSharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        if ("rawtcpip".equalsIgnoreCase(defaultSharedPreferences.getString("networkprotocol", ""))) {
            pntriplatitude.setVisible(false);
            pntriplocation.setVisible(false);
            pntriplongitude.setVisible(false);
            pntripusername.setVisible(false);
            pntrippassword.setVisible(false);
            pntripsourcetable.setVisible(false);
            pntripcasterip.setVisible(true);
            pntripcasterport.setVisible(true);
            this.sourceGroup.setVisible(true);
            this.otgGroup.setVisible(false);
            this.bluetoothGroup.setVisible(false);
        } else if ("ntripv1".equalsIgnoreCase(defaultSharedPreferences.getString("networkprotocol", ""))) {
            pntriplatitude.setVisible(true);
            pntriplocation.setVisible(true);
            pntriplongitude.setVisible(true);
            pntripusername.setVisible(true);
            pntrippassword.setVisible(true);
            pntripsourcetable.setVisible(true);
            pntripcasterip.setVisible(true);
            pntripcasterport.setVisible(true);
            if ("manual".equalsIgnoreCase(defaultSharedPreferences.getString("ntriplocation", ""))) {
                pntriplongitude.setVisible(true);
                pntriplatitude.setVisible(true);
            } else {
                pntriplongitude.setVisible(false);
                pntriplatitude.setVisible(false);
            }
            this.sourceGroup.setVisible(true);
            this.otgGroup.setVisible(false);
            this.bluetoothGroup.setVisible(false);
        } else if ("bluetooth".equalsIgnoreCase(defaultSharedPreferences.getString("networkprotocol", ""))) {
            this.sourceGroup.setVisible(false);
            this.otgGroup.setVisible(false);
            this.bluetoothGroup.setVisible(true);
        } else if ("otg".equalsIgnoreCase(defaultSharedPreferences.getString("networkprotocol", ""))) {
            this.sourceGroup.setVisible(false);
            this.otgGroup.setVisible(true);
            this.bluetoothGroup.setVisible(false);
        } else {
            this.sourceGroup.setVisible(false);
            this.otgGroup.setVisible(false);
            this.bluetoothGroup.setVisible(false);
        }

        if( ShareApplication.IS_PRO == 0){
            pserver.setVisible(false);
        }

        if (ShareApplication.USER_TYPE != 1) {
            pserver.setVisible(false);
            multiuser.setChecked(false);
            multiuser.setVisible(false);
            otgOut.setChecked(false);
            otgOut.setVisible(false);
            bluetoothOut.setChecked(false);
            bluetoothOut.setVisible(false);
            pusernum.setEnabled(false);
            this.otgGroup.setVisible(false);
            this.bluetoothGroup.setVisible(false);
            ShareApplication.updateUserNum(getActivity(), 2);
            updateSummarys(this.pusernum);
            List<String> names = new ArrayList<>();
            names.add("NTRIP v1.0");
            names.add("TCP/IP");

            List<String> lines = new ArrayList<>();
            lines.add("ntripv1");
            lines.add("rawtcpip");

            pnetworkprotocol.setEntries((CharSequence[]) names.toArray(new CharSequence[0]));
            pnetworkprotocol.setEntryValues((CharSequence[]) lines.toArray(new CharSequence[0]));
        } else {
            pusernum.setEnabled(true);
        }

        if (multiuser.isChecked()) {
            pshareusername.setVisible(false);
            psharepassword.setVisible(false);
            pusernum.setVisible(false);
        } else {
            pshareusername.setVisible(true);
            psharepassword.setVisible(true);
            pusernum.setVisible(true);
        }
    }

    /**
     * 更新标识
     * @param preference
     */
    private void updateSummarys(Preference preference) {
        if (preference instanceof ListPreference) {
            ListPreference listPreference = (ListPreference) preference;
            int index = listPreference.findIndexOfValue(listPreference.getValue());
            if (preference.getKey().equalsIgnoreCase("sel_server")) {
                Log.i("updateSummarys", index + "");
            }
            if(index >= 0){
                preference.setSummary(  listPreference.getEntries()[index]);
            }else{
                if(listPreference.getValue() != null && listPreference.getValue().toLowerCase().contains("str;")){
                    String [] data = listPreference.getValue().split(";");
                    if(data.length > 1){
                        preference.setSummary(data[1]);
                    }
                }else{
                    preference.setSummary( listPreference.getValue());
                }

            }
        } else if (preference instanceof EditTextPreference) {
            EditTextPreference editTextPreference = (EditTextPreference) preference;
            preference.setSummary(editTextPreference.getText());
        }
    }

    Preference.OnPreferenceChangeListener sBindPreferenceSummaryToValueListener = new Preference.OnPreferenceChangeListener() {
        public boolean onPreferenceChange(Preference preference, Object value) {
            if (preference.getKey().equalsIgnoreCase("multiuser")) {
                if ((Boolean) value) {
                    pshareusername.setVisible(false);
                    psharepassword.setVisible(false);
                    pusernum.setVisible(false);
                } else {
                    pshareusername.setVisible(true);
                    psharepassword.setVisible(true);
                    pusernum.setVisible(true);
                }

                ((MainActivity)getActivity()).updateNavi((Boolean) value,ShareApplication.getSourceType(getActivity()) == ShareApplication.SOURCE_TYP.NTRRIP_NET);
            }

            if (preference.getKey().equalsIgnoreCase("networkprotocol")) {
                ((MainActivity)getActivity()).updateNavi(ShareApplication.isMultiUser(getActivity()),"ntripnet".equalsIgnoreCase(value.toString()));
                if ("rawtcpip".equalsIgnoreCase(value.toString())) {
                    pntriplatitude.setVisible(false);
                    pntriplocation.setVisible(false);
                    pntriplongitude.setVisible(false);
                    pntripusername.setVisible(false);
                    pntrippassword.setVisible(false);
                    pntripsourcetable.setVisible(false);
                    pntripcasterip.setVisible(true);
                    pntripcasterport.setVisible(true);
                    sourceGroup.setVisible(true);
                    otgGroup.setVisible(false);
                    bluetoothGroup.setVisible(false);
                } else if ("ntripv1".equalsIgnoreCase(value.toString())) {
                    pntriplatitude.setVisible(true);
                    pntriplocation.setVisible(true);
                    pntriplongitude.setVisible(true);
                    pntripusername.setVisible(true);
                    pntrippassword.setVisible(true);
                    pntripsourcetable.setVisible(true);
                    pntripcasterip.setVisible(true);
                    pntripcasterport.setVisible(true);
//                    if ("internalfine".equalsIgnoreCase(value.toString())) {
//                        pntriplongitude.setVisible(false);
//                        pntriplatitude.setVisible(false);
//                    } else {
//                        pntriplongitude.setVisible(true);
//                        pntriplatitude.setVisible(true);
//                    }
                    sourceGroup.setVisible(true);
                    otgGroup.setVisible(false);
                    bluetoothGroup.setVisible(false);
                } else if ("bluetooth".equalsIgnoreCase(value.toString())) {
                    sourceGroup.setVisible(false);
                    otgGroup.setVisible(false);
                    bluetoothGroup.setVisible(true);

                    final GetBluetoothDialog dialog = new GetBluetoothDialog(getActivity());
                    dialog.setOnClickBottomListener(new GetBluetoothDialog.OnClickBottomListener() {
                        @Override
                        public void onSelectedClick(SearchResult bluetoothDevice) {
                            pbluetooth_mac.setText(bluetoothDevice.getAddress());
                            pbluetooth_mac.setSummary(bluetoothDevice.getAddress());
                            dialog.dismiss();
                        }

                        @Override
                        public void onNegtiveClick() {
                            dialog.dismiss();
                        }
                    });
                    dialog.show();
                } else if ("otg".equalsIgnoreCase(value.toString())) {
                    sourceGroup.setVisible(false);
                    bluetoothGroup.setVisible(false);
                    otgGroup.setVisible(true);
                    if (!ShareApplication.driver.UsbFeatureSupported())// 判断系统是否支持USB HOST
                    {
                        Dialog dialog = new AlertDialog.Builder(getActivity())
                                .setTitle(R.string.tips)
                                .setMessage(R.string.usb_not_support)
                                .setPositiveButton(R.string.ok,
                                        new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface arg0,
                                                                int arg1) {
                                            }
                                        }).create();
                        dialog.setCanceledOnTouchOutside(false);
                        dialog.show();
                    }
                } else {
                    sourceGroup.setVisible(false);
                    bluetoothGroup.setVisible(false);
                    otgGroup.setVisible(false);
                }
            }
            if (preference.getKey().equalsIgnoreCase("ntriplocation")) {
                if ("manual".equalsIgnoreCase(value.toString())) {
                    pntriplongitude.setVisible(true);
                    pntriplatitude.setVisible(true);
                    SharedPreferences defaultSharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
                    String ntriplatitude = defaultSharedPreferences.getString("ntriplatitude", "0");
                    String ntriplongitude = defaultSharedPreferences.getString("ntriplongitude", "0");
                    double CurrentLatitude = Double.valueOf(Double.parseDouble(ntriplatitude));
                    double CurrentLongitude = Double.valueOf(Double.parseDouble(ntriplongitude));
                    final GetMapCoordDialog dialog = new GetMapCoordDialog(getActivity(), new LatLng(CurrentLatitude, CurrentLongitude));
                    dialog.setOnClickBottomListener(new GetMapCoordDialog.OnClickBottomListener() {
                        @Override
                        public void onPositiveClick() {
                            LatLng latLng = dialog.getLatLng();
                            if (latLng != null) {
                                pntriplongitude.setText(String.valueOf(latLng.longitude));
                                pntriplongitude.setSummary(pntriplongitude.getText());
                                pntriplatitude.setText(String.valueOf(latLng.latitude));
                                pntriplatitude.setSummary(pntriplatitude.getText());
                            }
                            dialog.dismiss();
                        }

                        @Override
                        public void onNegtiveClick() {
                            dialog.dismiss();
                        }
                    });
                    dialog.show();
                } else {
                    pntriplongitude.setVisible(false);
                    pntriplatitude.setVisible(false);
                }
            }
            if (preference.getKey().equalsIgnoreCase("ntripsourcetable")) {
                if (getString(R.string.enter_mountpoint_name).equalsIgnoreCase(value.toString())) {
                    final EditText editText = new EditText(getActivity());
                    AlertDialog.Builder inputDialog =
                            new AlertDialog.Builder(getActivity());
                    inputDialog.setTitle(R.string.enter_mountpoint).setView(editText);
                    inputDialog.setPositiveButton(R.string.ok,
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    pntripsourcetable.setValue("STR;" + editText.getText().toString() + ";" + editText.getText().toString() + ";RTCM 3.2;1074(1),1084(1),1124(1),1005(5),1007(5),1033(5);2;GNSS;EagleGnss;CHN;0.00;0.00;1;1;NtripShare2020;none;B;N;19200;");
                                    pntripsourcetable.setSummary(editText.getText().toString());
                                }
                            }).show();
                }
            }
            String stringValue = value.toString();
            if (preference instanceof ListPreference) {
                ListPreference listPreference = (ListPreference) preference;
                int index = listPreference.findIndexOfValue(stringValue);
                preference.setSummary(index >= 0 ? listPreference.getEntries()[index] : null);

            } else if (preference instanceof MultiSelectListPreference) {
                StringBuffer summarys = new StringBuffer();
                MultiSelectListPreference multiSelectListPreference = (MultiSelectListPreference) preference;
                Set<String> values = (Set<String>) value;
                for (String v : values) {
                    int i = multiSelectListPreference.findIndexOfValue(v);
                    if (i >= 0) {
                        summarys.append(multiSelectListPreference.getEntries()[i] + ",");
                    }
                }
                preference.setSummary(summarys.toString());
            } else {
                preference.setSummary(stringValue);
                if ("ntripcasterip".equalsIgnoreCase(preference.getKey()) || "ntripcasterport".equalsIgnoreCase(preference.getKey())) {
                    String ip = ((EditTextPreference) pntripcasterip).getText();
                    if ("ntripcasterip".equalsIgnoreCase(preference.getKey())) {
                        ip = (String) value;
                        if (!ToolUtil.isIPAddress((String) value)) {
                            Toast.makeText(getActivity(), R.string.ip_error, Toast.LENGTH_LONG).show();
                        }
                        if (((String) value).equalsIgnoreCase(ShareApplication.getServerIP(getActivity()))) {
                            ((EditTextPreference) pntripcasterip).setText("000.000.00.000");
                            Toast.makeText(getActivity(), R.string.ip_error, Toast.LENGTH_LONG).show();
                        }
                    }
                    int port = 0;
                    try {
                        port = Integer.parseInt(((EditTextPreference) pntripcasterport).getText());
                    } catch (Exception e) {
                    }

                    if ("ntripcasterport".equalsIgnoreCase(preference.getKey())) {
                        try {
                            port = Integer.parseInt((String) value);
                        } catch (Exception e) {
                            Toast.makeText(getActivity(), R.string.port_error, Toast.LENGTH_LONG).show();
                        }
                    }
                    String userName = "userName";
                    String password = "password";
                    if (ip != null && port != 0) {
                        Log.i("updateMountPoint", "updateMountPoint");
                        updateMountPoint(ip, port, userName, password);
                    }
                }
            }
            ((MainActivity) getActivity()).updateServiceSetting();

            return true;
        }
    };

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
                    Log.i("updateMountPoint", "sendMessage");
                } catch (Exception e) {
                    Log.i("updateMountPoint", e.getMessage());
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
                List names = new ArrayList<>();
                List lines = new ArrayList<>();
                for (NtripSource ntripSource : tables) {
                    if (ntripSource instanceof StreamSource) {
                        names.add(((StreamSource) ntripSource).getMountPoint());
                        lines.add(((StreamSource) ntripSource).getRawLine());
                    }
                }
                names.add(getString(R.string.enter_mountpoint_name));
                lines.add(getString(R.string.enter_mountpoint_name));
                pntripsourcetable.setEntries((CharSequence[]) names.toArray(new CharSequence[0]));
                pntripsourcetable.setEntryValues((CharSequence[]) lines.toArray(new CharSequence[0]));
                updateSummarys(pntripsourcetable);
            }
        }
    };


}
