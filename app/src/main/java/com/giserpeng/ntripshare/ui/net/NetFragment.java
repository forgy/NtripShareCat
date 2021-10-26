package com.giserpeng.ntripshare.ui.net;

import android.app.Dialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.giserpeng.ntripshare.R;
import com.giserpeng.ntripshare.common.JsonUtil;
import com.giserpeng.ntripshare.ntrip.Servers.NtripCaster;
import com.giserpeng.ntripshare.util.DateUtils;
import com.giserpeng.ntripshare.view.swipemenulistview.BaseSwipListAdapter;
import com.giserpeng.ntripshare.view.swipemenulistview.SwipeMenuListView;
import com.google.gson.reflect.TypeToken;
import com.ut.device.UTDevice;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class NetFragment extends Fragment {

    private List<NetPointModel> clients = new ArrayList<>();
    private AppAdapter mAdapter;
    private SwipeMenuListView mListView;
    Timer timer;
    TextView tv_nonode;
    Button addNode;
    Button exportNode;
    Button importNode;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_net, container, false);
        tv_nonode = root.findViewById(R.id.tv_nonode);
        addNode = root.findViewById(R.id.addNode);
        addNode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final AddNetDialog dialog = new AddNetDialog(getActivity());
                dialog.setOnClickBottomListener(new AddNetDialog.OnClickBottomListener() {
                    @Override
                    public void onPositiveClick() {
                        NetPointModel userModel = dialog.getUserModel();
                        if (userModel != null) {
                           addNode(userModel);
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
        exportNode = root.findViewById(R.id.exportNode);
        exportNode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences defaultSharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
                String string = defaultSharedPreferences.getString("nodelist", "");
                ClipboardManager cm = (ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData mClipData = ClipData.newPlainText("Label", string);
                cm.setPrimaryClip(mClipData);
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getActivity(), "已导出节点信息至剪贴板", Toast.LENGTH_LONG).show();
                    }
                });
            }
        });

        importNode = root.findViewById(R.id.importNode);
        importNode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final EditText et = new EditText(getActivity());
                //获取ip而已，不用在乎
                new AlertDialog.Builder(getActivity()).setTitle("请输入").setView(et)
                        .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface arg0, int arg1) {
                                try {
                                    List<NetPointModel> userModels = JsonUtil.json2object(et.getText().toString(), new TypeToken<List<NetPointModel>>() {
                                    });
                                    if(userModels == null || userModels.size() == 0){
                                        Toast.makeText(getActivity(), "导入节点信息出错！", Toast.LENGTH_LONG).show();
                                        return;
                                    }
                                    clients.clear();
                                    clients.addAll(userModels);
                                    saveNode();
                                    Toast.makeText(getActivity(), "导入节点信息成功！", Toast.LENGTH_LONG).show();
                                }catch (Exception e){
                                    Toast.makeText(getActivity(), "导入节点信息出错！", Toast.LENGTH_LONG).show();
                                }

                            }
                        }).setNegativeButton("取消", null).show();
            }
        });
        mListView = (SwipeMenuListView) root.findViewById(R.id.listViewNet);
        mAdapter = new AppAdapter();
        mListView.setAdapter(mAdapter);
        mListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                Dialog dialog=new AlertDialog.Builder(getActivity())
                        .setTitle(R.string.node_remove)
                        .setIcon(R.drawable.ic_info)
                        .setMessage(R.string.confirm_remove)
                        .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                clients.remove(position);
                                saveNode();
                                mAdapter.notifyDataSetChanged();
                                Toast.makeText(getActivity(),R.string.node_remove_ok,Toast.LENGTH_LONG).show();
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
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                NetPointModel userModel = clients.get(position);
                final AddNetDialog dialog = new AddNetDialog(getActivity());
                dialog.setOnClickBottomListener(new AddNetDialog.OnClickBottomListener() {
                    @Override
                    public void onPositiveClick() {
                        NetPointModel userModel = dialog.getUserModel();
                        if (userModel != null) {
                            updateNode(userModel);
                        }
                        dialog.dismiss();
                    }

                    @Override
                    public void onNegtiveClick() {
                        dialog.dismiss();
                    }
                });

                dialog.show();
                dialog.setUserModel(userModel);
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
    private  void addNode(NetPointModel userModel){
        clients.add(userModel);
        saveNode();
        mAdapter.notifyDataSetChanged();
    }

    private  void updateNode(NetPointModel userModel){
        for (NetPointModel userModel1 :clients){
            if(userModel1.getUuid().equalsIgnoreCase(userModel.getUuid()) ){
                userModel1.setPassword(userModel.getPassword());
                userModel1.setIp(userModel.getIp());
                userModel1.setUserName(userModel.getUserName());
                userModel1.setMountPoint(userModel.getMountPoint());
                userModel1.setLat(userModel.getLat());
                userModel1.setLon(userModel.getLon());
                userModel1.setPort(userModel.getPort());
            }
        }
        saveNode();
        mAdapter.notifyDataSetChanged();
    }

    private void saveNode(){
        SharedPreferences defaultSharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        SharedPreferences.Editor editor = defaultSharedPreferences.edit();
        editor.putString("nodelist", JsonUtil.object2json(clients));
        editor.commit();
    }

    private void updateList() {
        Log.i("updateList", "clients" + clients.size());
        SharedPreferences defaultSharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String string = defaultSharedPreferences.getString("nodelist", "");
        List<NetPointModel> userModels = JsonUtil.json2object(string, new TypeToken<List<NetPointModel>>() {
        });
        clients.clear();
        if(userModels!= null){
            clients.addAll(userModels);
        }

        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mAdapter.notifyDataSetChanged();
                if (clients.size() > 0) {
                    tv_nonode.setVisibility(View.GONE);
                } else {
                    tv_nonode.setVisibility(View.VISIBLE);
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
        public NetPointModel getItem(int position) {
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
                        R.layout.item_list_node, null);
                new ViewHolder(convertView);
            }
            ViewHolder holder = (ViewHolder) convertView.getTag();
            NetPointModel item = getItem(position);
            holder.tv_username.setText(item.getUserName() +"："+item.getPassword());
            holder.tv_mountPoint.setText(getString(R.string.mountpoint)+"："+item.getMountPoint());
            holder.tv_server.setText(getString(R.string.server)+"：" + item.getIp() + ":"+ item.getPort());
            holder.tv_cord_node.setText(String.format("%.4f",item.getLon())+"\n"+ String.format("%.4f",item.getLat()));
            return convertView;

        }

        class ViewHolder {
            ImageView iv_usericon;
            TextView tv_username;
            TextView tv_mountPoint;
            TextView tv_server;
            TextView tv_cord_node;


            public ViewHolder(View view) {
                iv_usericon = (ImageView) view.findViewById(R.id.iv_usericon);
                tv_username = (TextView) view.findViewById(R.id.tv_username_pass);
                tv_mountPoint = (TextView) view.findViewById(R.id.tv_mounntpoint);
                tv_server = (TextView) view.findViewById(R.id.tv_server);
                tv_cord_node = (TextView) view.findViewById(R.id.tv_cord_node);
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
