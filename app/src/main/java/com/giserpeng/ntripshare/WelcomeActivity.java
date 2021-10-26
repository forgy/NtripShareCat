package com.giserpeng.ntripshare;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.giserpeng.ntripshare.re.ReModel;
import com.giserpeng.ntripshare.update.UpdateManager;
import com.ut.device.UTDevice;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Locale;


/**
 * ClassName:LoginActivity
 * Function: 登录界面
 * Reason:   TODO ADD REASON
 *
 * @author kzq
 * @version 1.1
 * @since 2015   2015-7-28   下午1:51:01
 */
public class WelcomeActivity extends Activity {

    private TextView textVersion;
    private TextView textViewProgress;
    int versionCode;
    private String updatePath = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_welcome);
        this.initView();
        textVersion.setText("Version:V" + getVersionName(this));

        versionCode = getVersionCode(this);
        UpdateManager manager = new UpdateManager(this);
        manager.setUpdateHandler(new UpdateManager.UpdateHandler() {
            @Override
            public void onfindUpdate(String re) {
                try {
                    JSONObject jsonObject = new JSONObject(re);
                    if (null != jsonObject) {
                        double serviceCode = jsonObject.getInt("version");
                        updatePath = jsonObject.getString("url");
                        re(jsonObject.getString("key"));
                        // 版本判断
                        if (serviceCode > versionCode) {
                            showNoticeDialog();
                            return;
                        }
                    }
                } catch (JSONException e) {
                    Toast.makeText(WelcomeActivity.this, R.string.connect_server_fail, Toast.LENGTH_LONG).show();
                    textViewProgress.setText(getString(R.string.connect_server_fail) + "\n" + UTDevice.getUtdid(WelcomeActivity.this));
                    return;
                }
                textViewProgress.setText(R.string.latest_version);

                SharedPreferences defaultSharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                String shouPrivate = defaultSharedPreferences.getString("shouPrivate", "");
                if ("".equalsIgnoreCase(shouPrivate)) {

                    if (!WelcomeActivity.this.isFinishing())//xActivity即为本界面的Activity
                    {
                        AlertDialog.Builder inputDialog =
                                new AlertDialog.Builder(WelcomeActivity.this);
                        inputDialog.setIcon(R.drawable.ic_info);
                        inputDialog.setTitle(R.string.private_policy);
                        inputDialog.setMessage(R.string.msg_private);
                        inputDialog.setPositiveButton(R.string.accept, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                SharedPreferences.Editor editor = defaultSharedPreferences.edit();
                                editor.putString("shouPrivate", "show");
                                editor.commit();
                                Intent intent = new Intent();
                                intent.setClass(getApplicationContext(), MainActivity.class);
                                startActivity(intent);
                                WelcomeActivity.this.finish();
                            }
                        });
                        inputDialog.setNegativeButton(R.string.reject, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                WelcomeActivity.this.finish();
                            }
                        });
                        inputDialog.show();
                    }
                } else {
                    Intent intent = new Intent();
                    intent.setClass(getApplicationContext(), MainActivity.class);
                    startActivity(intent);
                    WelcomeActivity.this.finish();
                }

            }

            @Override
            public void onfindUpdateFail(String msg) {
                textViewProgress.setText(getString(R.string.connect_server_fail) + "\n" + UTDevice.getUtdid(WelcomeActivity.this));
            }
        });
        // 检查软件更新
        manager.checkUpdate();
    }


    public void re(String key) {
        try {
            Log.i("ReModel", key);
            SharedPreferences defaultSharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            SharedPreferences.Editor editor = defaultSharedPreferences.edit();
            editor.putString(getString(R.string.ResistCode), key);
            editor.commit();
            if (ReModel.CheckCode(key)) {
                ReModel re = new ReModel(key);
                ShareApplication.USER_TYPE = re.getType();

            } else {
                Toast.makeText(this, getString(R.string.connect_server_fail), Toast.LENGTH_LONG).show();
                textViewProgress.setText(getString(R.string.connect_server_fail));
//                Toast.makeText(this, "注册失败，注册码无效！", Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            Toast.makeText(this, getString(R.string.connect_server_fail), Toast.LENGTH_LONG).show();
            textViewProgress.setText(getString(R.string.connect_server_fail));
//            Toast.makeText(this, "注册失败，注册码无效！", Toast.LENGTH_LONG).show();
        }
    }

    /**
     * initView:(初始化界面)
     * <p>
     * ver     date         author
     * ──────────────────────────────────
     * 2015-7-28      Administrator
     */
    protected void initView() {
        textVersion = (TextView) this.findViewById(R.id.textViewVersion);
        textViewProgress = (TextView) this.findViewById(R.id.textViewProgress);

        try {
            Locale locale = getResources().getConfiguration().getLocales().get(0);
            String language = locale.getLanguage();
            if (!language.contains("zh")) {
                RelativeLayout back = findViewById(R.id.back);
                back.setBackground(getDrawable(R.drawable.webclome_bg_en));
            }
        } catch (Exception e) {

        }

    }


    /**
     * 显示软件更新对话框
     */
    private void showNoticeDialog() {
        textViewProgress.setText(R.string.find_new_version);
            // 构造对话框
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(R.string.soft_update_title);
            builder.setMessage(R.string.soft_update_info);
            builder.setIcon(R.drawable.ic_info);
            // 更新
            builder.setPositiveButton(R.string.soft_update_updatebtn, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
//                showDownloadDialog();
                    Uri uri = Uri.parse(updatePath);
                    Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                    startActivity(intent);
                    dialog.dismiss();
                    // 显示下载对话框
                }
            });
            //稍后更新
            builder.setNegativeButton(R.string.soft_update_later, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    Intent intent = new Intent();
                    intent.setClass(getApplicationContext(), MainActivity.class);
                    startActivity(intent);
                    WelcomeActivity.this.finish();
                }
            });
            builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialog) {
                    dialog.dismiss();
//                Intent intent = new Intent();
//                intent.setClass(getApplicationContext(),MainActivity.class);
//                startActivity(intent);
//                WelcomeActivity.this.finish();
                }
            });


        if (!WelcomeActivity.this.isFinishing())//xActivity即为本界面的Activity
        {
            Dialog noticeDialog = builder.create();
            noticeDialog.setCancelable(false);
            noticeDialog.show();
        }
    }

    /**
     * 获取软件版本号
     *
     * @param context
     * @return
     */
    private String getVersionName(Context context) {
        String versionCode = "";
        try {
            // 获取软件版本号，对应AndroidManifest.xml下android:versionCode
            versionCode = context.getPackageManager().getPackageInfo("com.giserpeng.ntripshare", 0).versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return versionCode;
    }

    /**
     * 获取软件版本号
     *
     * @param context
     * @return
     */
    private int getVersionCode(Context context) {
        int versionCode = 0;
        try {
            // 获取软件版本号，对应AndroidManifest.xml下android:versionCode
            versionCode = context.getPackageManager().getPackageInfo("com.giserpeng.ntripshare", 0).versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return versionCode;
    }

}
