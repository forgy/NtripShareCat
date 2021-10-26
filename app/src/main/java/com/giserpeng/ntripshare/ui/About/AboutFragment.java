package com.giserpeng.ntripshare.ui.About;

import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.giserpeng.ntripshare.MainActivity;
import com.giserpeng.ntripshare.R;
import com.giserpeng.ntripshare.ShareApplication;
import com.giserpeng.ntripshare.re.ReModel;
import com.giserpeng.ntripshare.update.UpdateManager;
import com.giserpeng.ntripshare.util.DateUtils;
import com.giserpeng.ntripshare.util.EncryptUtil;
import com.ut.device.UTDevice;

public class AboutFragment extends Fragment {
    Button buttonRe;
    TextView textViewRe;
    TextView textViewVersion;
    Button buttonPrivate;
    Button buttonCopy;
    int versionCode;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_about, container, false);
        buttonRe = root.findViewById(R.id.buttonRegist);
        textViewRe = root.findViewById(R.id.text_registinfo);
        versionCode = UpdateManager.getVersionCode(getActivity());
        buttonRe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final EditText editText = new EditText(getActivity());
                AlertDialog.Builder inputDialog =
                        new AlertDialog.Builder(getActivity());
                inputDialog.setTitle(R.string.register).setView(editText);
                inputDialog.setNegativeButton(R.string.trya, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ((MainActivity)getActivity()).trial();
                        updateReCode();
                    }
                });
                inputDialog.setPositiveButton(R.string.ok,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Toast.makeText(getActivity(),
                                        editText.getText().toString(),
                                        Toast.LENGTH_SHORT).show();

                                String code =  editText.getText().toString();
                                ((MainActivity)getActivity()).re(code);
                                updateReCode();
                            }
                        }).show();
            }
        });

        buttonCopy = root.findViewById(R.id.buttonCopy);
        buttonCopy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ClipboardManager cm = (ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData mClipData = ClipData.newPlainText("Label", UTDevice.getUtdid(getActivity()));
                cm.setPrimaryClip(mClipData);
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getActivity(), "复制共享信息成功", Toast.LENGTH_LONG).show();
                    }
                });
            }
        });
        buttonPrivate = root.findViewById(R.id.buttonPrivat);
        buttonPrivate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder inputDialog =
                        new AlertDialog.Builder(getActivity());
                inputDialog.setIcon(R.drawable.ic_info);
                inputDialog.setTitle(R.string.private_policy);
                inputDialog.setMessage(R.string.msg_private);
                inputDialog.setNegativeButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
                inputDialog.show();
            }
        });
        textViewVersion = root.findViewById(R.id.text_version);
        if (ShareApplication.USER_TYPE != 1) {
            textViewVersion.setText("Version:V"+ UpdateManager.getVersionName(getActivity()));
        }else{
            textViewVersion.setText("Version:V"+ UpdateManager.getVersionName(getActivity())+ " Pro");
        }

        updateReCode();
        textViewRe.setText(UTDevice.getUtdid(getActivity()));
        return root;
    }

    private void updateReCode(){
        SharedPreferences defaultSharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        String re =  defaultSharedPreferences.getString(getString(R.string.ResistCode),"");
        String ss = EncryptUtil.decrypt(re);
        if(re.equalsIgnoreCase("") ){
            textViewRe.setText("未注册");
        }
        else if(!ReModel.CheckCode(ss)){
            textViewRe.setText("注册码无效");
        }else{
            ReModel reModel = new ReModel(ss);
            textViewRe.setText("有效期至" + DateUtils.dateToString(reModel.getEndTime(),"yyyy年MM月dd日"));
        }
    }





}
