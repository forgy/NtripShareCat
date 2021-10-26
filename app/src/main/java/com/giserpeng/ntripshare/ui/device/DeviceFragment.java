package com.giserpeng.ntripshare.ui.device;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.map.PolylineOptions;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.utils.DistanceUtil;
import com.giserpeng.ntripshare.ShareApplication;
import com.giserpeng.ntripshare.ntrip.Tools.GPSPosition;
import com.giserpeng.ntripshare.ntrip.Tools.NMEA;
import com.giserpeng.ntripshare.ui.net.NetPointModel;
import com.giserpeng.ntripshare.view.swipemenulistview.BaseSwipListAdapter;
import com.giserpeng.ntripshare.view.swipemenulistview.SwipeMenuListView;
import com.giserpeng.ntripshare.MainActivity;
import com.giserpeng.ntripshare.R;
import com.giserpeng.ntripshare.ntrip.Clients.Client;
import com.giserpeng.ntripshare.ntrip.NTRIPService;
import com.giserpeng.ntripshare.ntrip.Servers.NtripCaster;
import com.giserpeng.ntripshare.ntrip.Servers.ReferenceStation;
import com.giserpeng.ntripshare.util.DateUtils;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

public class DeviceFragment extends Fragment {

    private List<ApplicationInfo> mAppList;
    private List<Client> clients = new ArrayList<>();
    private AppAdapter mAdapter;
    private SwipeMenuListView mListView;
    Timer timer;
    TextView tv_nouser;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_gallery, container, false);
        tv_nouser = root.findViewById(R.id.tv_nouser);
        mAppList = getActivity().getPackageManager().getInstalledApplications(0);
        mListView = (SwipeMenuListView) root.findViewById(R.id.listView);
        mAdapter = new AppAdapter();
        mListView.setAdapter(mAdapter);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if(ShareApplication.getSourceType(getActivity()) == ShareApplication.SOURCE_TYP.NTRRIP_NET){
                    return;
                }
                AlertDialog.Builder inputDialog =
                        new AlertDialog.Builder(getActivity());
                inputDialog.setIcon(R.drawable.ic_setting2);
                inputDialog.setTitle(R.string.action_settings);
                inputDialog.setMessage(R.string.confirm_set_basecoord);
                inputDialog.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
                inputDialog.setPositiveButton(R.string.ok,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                try {
                                    SharedPreferences defaultSharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
                                    String string = defaultSharedPreferences.getString("ntriplocation", "auto");
                                    if ("manual".equalsIgnoreCase(string)) {
                                        Client client = clients.get(position);
                                        if (client != null) {
                                            SharedPreferences.Editor editor = defaultSharedPreferences.edit();
                                            editor.putString("ntriplatitude", String.valueOf(client.getPosition().lat));
                                            editor.putString("ntriplongitude", String.valueOf(client.getPosition().lon));
                                            editor.commit();

                                            NTRIPService.ManualLat = Double.valueOf(client.getPosition().lat);
                                            NTRIPService.ManualLon = Double.valueOf(client.getPosition().lon);
                                        }
                                    } else  if ("auto".equalsIgnoreCase(string)) {
                                        Toast.makeText(getActivity(), R.string.info_manual, Toast.LENGTH_LONG).show();
                                    }else {
                                        Toast.makeText(getActivity(), R.string.info_manual, Toast.LENGTH_LONG).show();
                                    }
                                }catch (Exception e){
                                }
                            }
                        }).show();
            }
        });
        mListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                Dialog dialog=new androidx.appcompat.app.AlertDialog.Builder(getActivity())
                        .setTitle(R.string.client_remove)
                        .setIcon(R.drawable.ic_info)
                        .setMessage(R.string.confirm_client_remove)
                        .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                Client client = clients.get(position);
                                ReferenceStation referenceStation = NtripCaster.getInstance().getReferenceStation();
                                if (referenceStation != null) {
                                    referenceStation.removeClient(client);
                                }
                                clients.remove(position);
                                mAdapter.notifyDataSetChanged();
                                Toast.makeText(getActivity(),R.string.client_remove_ok,Toast.LENGTH_LONG).show();
                            }
                        })
                        .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        })
                        .create();
                dialog.show();
                return true;
            }
        });
        startTimer();
        updateList();
        return root;
    }

    TimerTask task = new TimerTask() {
        @Override
        public void run() {
            updateList();
        }
    };

    private void updateList() {
        Log.i("updateList", "clients" + clients.size());
        ReferenceStation referenceStation = NtripCaster.getInstance().getReferenceStation();
        if (referenceStation != null) {
            List<Client> list = referenceStation.getClient();
            clients.clear();
            clients.addAll(list);
        }

        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mAdapter.notifyDataSetChanged();
                if (clients.size() > 0) {
                    tv_nouser.setVisibility(View.GONE);
                } else {
                    tv_nouser.setVisibility(View.VISIBLE);
                }
                Log.i("updateList", "clients" + clients.size());
            }
        });
    }

    private void startTimer() {
        Log.i("GalleryFragment", "startTimer()");
        if (timer == null) {
            timer = new Timer();
        }
        if (task == null) {
            task = new TimerTask() {
                @Override
                public void run() {
                    updateList();
                }
            };
        }
        if (timer != null && task != null) {
            timer.schedule(task, 1000, 1000);
        }
    }

    private void stopTimer() {
        Log.i("GalleryFragment", "stopTimer()");
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

    class AppAdapter extends BaseSwipListAdapter {

        @Override
        public int getCount() {
            return clients.size();
        }

        @Override
        public Client getItem(int position) {
            return clients.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = View.inflate(getContext(),
                        R.layout.item_list_app, null);
                new ViewHolder(convertView);
            }
            ViewHolder holder = (ViewHolder) convertView.getTag();
            Client item = getItem(position);
            String agentStr = item.getHttpHeader("User-Agent");

            Log.i("User-Agent", agentStr);
            holder.tc_time.setText(getString(R.string.connect_time) + "：" + DateUtils.dateToString(item.getConnectionTime(), "yyyy-MM-dd HH:mm:SS"));
            if (agentStr != null) {
                DecimalFormat df = new DecimalFormat(".000000");
                holder.tv_logusername.setText(getString(R.string.username) + "：" + item.getClientUserName());

                String[] agent = agentStr.split(" ");
                String name = agent[agent.length - 1].split("/")[0];
                holder.tv_name.setText(getString(R.string.client) +"："+ name);
                Log.i("User-Agent", name);
                if (item.getPosition() != null) {
                    try{
                        Locale locale = getResources().getConfiguration().getLocales().get(0);
                        String language = locale.getLanguage();
                        if (!language.contains("zh")){
                            holder.tc_status.setText(String.valueOf(item.getPosition().getStatus(false)));
                        }else{
                            holder.tc_status.setText(String.valueOf(item.getPosition().getStatus(true)));
                        }
                    }catch (Exception e){

                    }

                    String coord = getString(R.string.lon) + "：" + df.format(item.getPosition().lon) + " " + getString(R.string.lat) + ":" + df.format(item.getPosition().lat);
                    holder.tc_coord.setText(coord);
                    LatLng latLng = new LatLng(item.getPosition().lat, item.getPosition().lon);
                    LatLng center = new LatLng(((MainActivity) getActivity()).getCurrentLatitude(), ((MainActivity) getActivity()).getCurrentLongitude());
                    if (ShareApplication.getSourceType(getActivity()) == ShareApplication.SOURCE_TYP.NTRIPV1) {
                        SharedPreferences defaultSharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
                        String string = defaultSharedPreferences.getString("ntriplocation", "auto");
                        if ("manual".equalsIgnoreCase(string)) {
                            Client client = clients.get(position);
                            if (client != null) {
                                center = new LatLng(NTRIPService.ManualLat, NTRIPService.ManualLon);
                            }
                        } else if ("auto".equalsIgnoreCase(string)) {
                            GPSPosition gpsPosition = NtripCaster.getInstance().getReferenceStation().getCurrentPosition();
                            if(gpsPosition != null){
                                center = new LatLng(gpsPosition.lat, gpsPosition.lon);
                            }
                        }
                    }
                    if (ShareApplication.getSourceType(getActivity()) == ShareApplication.SOURCE_TYP.NTRRIP_NET) {
                        ReferenceStation referenceStation = NtripCaster.getInstance().getReferenceStation();
                        if(referenceStation != null){
                            Client client = clients.get(position);
                            NetPointModel netPointModel = referenceStation.getNearestNode(client);
                            if(netPointModel != null){
                                center = new LatLng(netPointModel.getLat(), netPointModel.getLon());
                            }
                        }
                    }
                    double dis = DistanceUtil.getDistance(latLng, center);
                    holder.tc_dis.setText(String.valueOf((int) dis) + getString(R.string.meter));
                }
            }
            holder.iv_icon.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                }
            });
            holder.tv_name.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                }
            });
            return convertView;
        }

        class ViewHolder {
            ImageView iv_icon;
            TextView tv_name;
            TextView tc_coord;
            TextView tc_time;
            TextView tc_status;
            TextView tc_dis;
            TextView tv_logusername;

            public ViewHolder(View view) {
                iv_icon = (ImageView) view.findViewById(R.id.iv_icon);
                tv_name = (TextView) view.findViewById(R.id.tv_name);
                tc_coord = (TextView) view.findViewById(R.id.tv_coord);
                tc_time = (TextView) view.findViewById(R.id.tv_time);
                tc_status = (TextView) view.findViewById(R.id.tv_status);
                tc_dis = (TextView) view.findViewById(R.id.tv_dis);
                tv_logusername = (TextView) view.findViewById(R.id.tv_logusername);
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
