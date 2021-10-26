package com.giserpeng.ntripshare.ui.user;

import android.app.Dialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
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

import com.giserpeng.ntripshare.MainActivity;
import com.giserpeng.ntripshare.ShareApplication;
import com.giserpeng.ntripshare.re.ReModel;
import com.giserpeng.ntripshare.ui.net.NetPointModel;
import com.giserpeng.ntripshare.view.swipemenulistview.BaseSwipListAdapter;
import com.giserpeng.ntripshare.view.swipemenulistview.SwipeMenuListView;
import com.giserpeng.ntripshare.R;
import com.giserpeng.ntripshare.common.JsonUtil;
import com.giserpeng.ntripshare.ntrip.Servers.NtripCaster;
import com.giserpeng.ntripshare.util.DateUtils;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class UserFragment extends Fragment {

    private List<ApplicationInfo> mAppList;
    private List<UserModel> clients = new ArrayList<>();
    private AppAdapter mAdapter;
    private SwipeMenuListView mListView;
    Timer timer;
    TextView tv_nouser;
    Button addUser;
    Button exportUser;
    Button importUser;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_user, container, false);
        tv_nouser = root.findViewById(R.id.tv_nousername);
        addUser = root.findViewById(R.id.addUser);
        addUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final AddUserDialog dialog = new AddUserDialog(getActivity());
                dialog.setOnClickBottomListener(new AddUserDialog.OnClickBottomListener() {
                    @Override
                    public void onPositiveClick() {
                        UserModel userModel = dialog.getUserModel();
                        if (userModel != null) {
                            addUser(userModel);
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
        exportUser = root.findViewById(R.id.exportUser);
        exportUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences defaultSharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
                String string = defaultSharedPreferences.getString("userlist", "");
                ClipboardManager cm = (ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData mClipData = ClipData.newPlainText("Label", string);
                cm.setPrimaryClip(mClipData);
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getActivity(), "已导出用户信息至剪贴板", Toast.LENGTH_LONG).show();
                    }
                });
            }
        });

        importUser = root.findViewById(R.id.importUser);
        importUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final EditText et = new EditText(getActivity());
                //获取ip而已，不用在乎
                new AlertDialog.Builder(getActivity()).setTitle("请输入").setView(et)
                        .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface arg0, int arg1) {
                                try {
                                    List<UserModel> userModels = JsonUtil.json2object(et.getText().toString(), new TypeToken<List<UserModel>>() {
                                    });
                                    if(userModels == null || userModels.size() == 0){
                                        Toast.makeText(getActivity(), "导入用户信息出错！", Toast.LENGTH_LONG).show();
                                        return;
                                    }
                                    clients.clear();
                                    clients.addAll(userModels);
                                    ShareApplication.INSTANCE.saveUser(clients);
                                    Toast.makeText(getActivity(), "导入用户信息成功！", Toast.LENGTH_LONG).show();
                                }catch (Exception e){
                                    Toast.makeText(getActivity(), "导入用户信息出错！", Toast.LENGTH_LONG).show();
                                }

                            }
                        }).setNegativeButton("取消", null).show();
            }
        });
        mAppList = getActivity().getPackageManager().getInstalledApplications(0);
        mListView = (SwipeMenuListView) root.findViewById(R.id.listViewUser);
        mAdapter = new AppAdapter();
        mListView.setAdapter(mAdapter);
        mListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                Dialog dialog = new AlertDialog.Builder(getActivity())
                        .setTitle(R.string.node_remove)
                        .setIcon(R.drawable.ic_info)
                        .setMessage(R.string.confirm_remove)
                        .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                clients.remove(position);
                                ShareApplication.INSTANCE.saveUser(clients);
                                mAdapter.notifyDataSetChanged();
                                Toast.makeText(getActivity(), R.string.node_remove_ok, Toast.LENGTH_LONG).show();
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
                UserModel userModel = clients.get(position);
                final AddUserDialog dialog = new AddUserDialog(getActivity());
                dialog.setOnClickBottomListener(new AddUserDialog.OnClickBottomListener() {
                    @Override
                    public void onPositiveClick() {
                        UserModel userModel = dialog.getUserModel();
                        if (userModel != null) {
                            updateUser(userModel);
                        }
                        dialog.dismiss();
                    }

                    @Override
                    public void onNegtiveClick() {
                        SharedPreferences defaultSharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
                        String ResistCode = defaultSharedPreferences.getString("ResistCode", "");
                        ReModel reModel = new ReModel(ResistCode);
                        String mes = getString(R.string.server) + "：" + reModel.getServerIp() + "\n" +
                                getString(R.string.port) + "：" + reModel.getCasterPort() + "\n" +
                                getString(R.string.mountpoint) + "：" + NtripCaster.getInstance().getMountPointName() + "\n";

                        mes += getString(R.string.username) + "：" + userModel.getUserName() + "\n" +
                                getString(R.string.password) + "：" + userModel.getPassword() + "\n";

                        final String mg = mes;
                        ClipboardManager cm = (ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
                        ClipData mClipData = ClipData.newPlainText("Label", mg);
                        cm.setPrimaryClip(mClipData);
                        Toast.makeText(getActivity(), R.string.copy_success, Toast.LENGTH_LONG).show();
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

    private void addUser(UserModel userModel) {
        clients.add(userModel);
        ShareApplication.INSTANCE.saveUser(clients);
        mAdapter.notifyDataSetChanged();
    }

    private void updateUser(UserModel userModel) {
        for (UserModel userModel1 : clients) {
            if (userModel1.getUserName().equalsIgnoreCase(userModel.getUserName())) {
                userModel1.setPassword(userModel.getPassword());
                userModel1.setEndTime(userModel.getEndTime());
            }
        }
        ShareApplication.INSTANCE.saveUser(clients);
        mAdapter.notifyDataSetChanged();
    }

    private void updateList() {
        Log.i("updateList", "clients" + clients.size());
        SharedPreferences defaultSharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String string = defaultSharedPreferences.getString("userlist", "");
        List<UserModel> userModels = JsonUtil.json2object(string, new TypeToken<List<UserModel>>() {
        });
        clients.clear();
        if (userModels != null) {
            for (UserModel userModel : userModels) {
                userModel.setOnLine(NtripCaster.getInstance().getReferenceStation().checkLogin(userModel.getUserName()));
            }
            clients.addAll(userModels);
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
        public UserModel getItem(int position) {
            return clients.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            try {
                if (convertView == null) {
                    convertView = View.inflate(getContext(),
                            R.layout.item_list_user, null);
                    new ViewHolder(convertView);
                }
                ViewHolder holder = (ViewHolder) convertView.getTag();
                UserModel item = getItem(position);
                holder.tv_username.setText(getString(R.string.user_username) + item.getUserName());
                holder.tv_password.setText(getString(R.string.password) + "：" + item.getPassword());
                holder.tv_endtime.setText(getString(R.string.user_endtime) + DateUtils.dateToString(item.getEndTime(), "yyyy-MM-dd HH:mm:SS"));
                Date date = NtripCaster.getInstance().getLoginTime(item.getUserName());
                if(date != null){
                    holder.tv_logintime.setText(getString(R.string.user_logintime) + DateUtils.dateToString(date, "yyyy-MM-dd HH:mm:SS"));
                }else{
                    holder.tv_logintime.setText(getString(R.string.user_logintime) );
                }
                if (item.isOnLine()) {
                    holder.tc_status.setText(R.string.online);
                } else {
                    holder.tc_status.setText(R.string.offline);
                }
            }catch (Exception e){

            }

            return convertView;
        }

        class ViewHolder {
            ImageView iv_usericon;
            TextView tv_username;
            TextView tv_password;
            TextView tv_endtime;
            TextView tc_status;
            TextView tv_logintime;

            public ViewHolder(View view) {
                iv_usericon = (ImageView) view.findViewById(R.id.iv_usericon);
                tv_username = (TextView) view.findViewById(R.id.tv_username);
                tv_password = (TextView) view.findViewById(R.id.tv_password);
                tv_endtime = (TextView) view.findViewById(R.id.tv_endtime);
                tv_logintime = (TextView) view.findViewById(R.id.tv_logintime);
                tc_status = (TextView) view.findViewById(R.id.tv_userstatus);
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
