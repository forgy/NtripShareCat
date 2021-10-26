package com.giserpeng.ntripshare.update;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.giserpeng.ntripshare.R;
import com.giserpeng.ntripshare.ShareApplication;
import com.giserpeng.ntripshare.util.DateUtils;
import com.giserpeng.ntripshare.util.EncryptUtil;
import com.giserpeng.ntripshare.util.NetUtils;
import com.ut.device.UTDevice;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;
import java.util.List;

import okhttp3.Response;

/**
 * @author coolszy
 * @date 2012-4-26
 * @blog http://blog.92coding.com
 */

public class UpdateManager {
    /* 下载中 */
    private static final int DOWNLOAD = 1;
    /* 下载结束 */
    private static final int DOWNLOAD_FINISH = 2;
    /* 下载保存路径 */
    private String mSavePath;
    /* 记录进度条数量 */
    private int progress;
    /* 是否取消更新 */
    private boolean cancelUpdate = false;
    private String updatePath = "";
    private Activity mContext;
    /* 更新进度条 */
    private ProgressBar mProgress;
    private Dialog mDownloadDialog;
    String apkName = "";
    private UpdateHandler updateHandler;

    public void setUpdateHandler(UpdateHandler updateHandler) {
        this.updateHandler = updateHandler;
    }

    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                // 正在下载
                case DOWNLOAD:
                    // 设置进度条位置
                    mProgress.setProgress(progress);
                    break;
                case DOWNLOAD_FINISH:
                    // 安装文件
                    installApk();
                    break;
                default:
                    break;
            }
        }

        ;
    };

    public UpdateManager(Activity context) {
        this.mContext = context;
    }

    /**
     * 检测软件更新
     */
    public void checkUpdate() {
        final int versionCode = UpdateManager.this.getVersionCode(mContext);
        final List<Double> coord = ShareApplication.getLastPosition(mContext);
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    String uuid = UTDevice.getUtdid(mContext);
                    BigInteger bigInteger =  new BigInteger(uuid.getBytes("UTF-8"));
                    int num = bigInteger.intValue()%5;
                    num = 0;
                    int sel = ShareApplication.getSelServer(mContext);
                    if(sel>0 && sel <=5 ){
                        num = sel-1;
                    }

                    String data = mContext.getString(R.string.platform_name)+"/"+ versionCode + "-" + DateUtils.dateToString(new Date(), "yyyyMMddHHmmss") + "-" + UTDevice.getUtdid(mContext);
                    String url = mContext.getString(R.string.URL_VERSION_PRO).replace("pro0","pro"+ num)
                            + "?deviceCode=" + EncryptUtil.encrypt(data) + "&lon1=" + coord.get(0).toString() + "&lat=" + coord.get(1).toString();
                    if(sel == 6){
                        url = mContext.getString(R.string.URL_VERSION) + "?deviceCode=" + EncryptUtil.encrypt(data) + "&lon1=" + coord.get(0).toString() + "&lat=" + coord.get(1).toString();
                    }
                    Log.i("checkUpdate", url);
                    Response response = NetUtils.get(url);
                    JSONObject jsonObject1 = new JSONObject(response.body().string());
                    Log.i("checkUpdate", jsonObject1.toString());
                    if (jsonObject1.getBoolean("success")) {
                        ShareApplication.updateYuMing(mContext,NetUtils.getYuMing(url));
                        ShareApplication.IS_PRO = 1;
                        mContext.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Log.i("checkUpdate", "updateHandler");
                                if (updateHandler != null) {
                                    try {
                                        updateHandler.onfindUpdate(jsonObject1.getJSONObject("data").toString());
                                    } catch (JSONException e) {
                                        Log.i("checkUpdate", e.getMessage());
                                        e.printStackTrace();
                                    }
                                }
                            }
                        });
                    } else {
                        ShareApplication.IS_PRO = 0;
                        data = mContext.getString(R.string.platform_name)+"/"+ versionCode + "-" + DateUtils.dateToString(new Date(), "yyyyMMddHHmmss") + "-" + UTDevice.getUtdid(mContext);
                        url = mContext.getString(R.string.URL_VERSION) + "?deviceCode=" + EncryptUtil.encrypt(data) + "&lon1=" + coord.get(0).toString() + "&lat=" + coord.get(1).toString();
                        Log.i("checkUpdate", url);
                        response = NetUtils.get(url);
                        JSONObject jsonObject2 = new JSONObject(response.body().string());
                        Log.i("checkUpdate", jsonObject2.toString());
                        if (jsonObject2.getBoolean("success")) {
                            ShareApplication.updateYuMing(mContext,NetUtils.getYuMing(url));
                            mContext.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Log.i("checkUpdate", "updateHandler");
                                    if (updateHandler != null) {
                                        try {
                                            updateHandler.onfindUpdate(jsonObject2.getJSONObject("data").toString());
                                        } catch (JSONException e) {
                                            Log.i("checkUpdate", e.getMessage());
                                            e.printStackTrace();
                                        }
                                    }
                                }
                            });
                        } else {
                            mContext.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(mContext, R.string.connect_server_fail, Toast.LENGTH_LONG).show();
                                    if (updateHandler != null) {
                                        updateHandler.onfindUpdateFail(mContext.getString(R.string.connect_server_fail));
                                    }
                                }
                            });

                        }
                    }

                } catch (Exception e) {
                    mContext.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(mContext, R.string.connect_server_fail, Toast.LENGTH_LONG).show();
                            if (updateHandler != null) {
                                updateHandler.onfindUpdateFail(mContext.getString(R.string.connect_server_fail));
                            }
                        }
                    });
                }
            }
        });
        thread.start();
    }


    /**
     * 获取软件版本号
     *
     * @param context
     * @return
     */
    public static int getVersionCode(Context context) {
        int versionCode = 0;
        try {
            // 获取软件版本号，对应AndroidManifest.xml下android:versionCode
            versionCode = context.getPackageManager().getPackageInfo("com.giserpeng.ntripshare", 0).versionCode;
        } catch (NameNotFoundException e) {
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
    public static String getVersionName(Context context) {
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
     * 显示软件更新对话框
     */
    public static void showNoticeDialog(Context context, String updatePath) {
        // 构造对话框
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(R.string.soft_update_title);
        builder.setMessage(R.string.soft_update_info);
        builder.setIcon(R.drawable.ic_info);
        // 更新
        builder.setPositiveButton(R.string.soft_update_updatebtn, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Uri uri = Uri.parse(updatePath);
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                context.startActivity(intent);
                dialog.dismiss();
                // 显示下载对话框
            }
        });
        //稍后更新
        builder.setNegativeButton(R.string.soft_update_later, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                dialog.dismiss();
            }
        });

        Dialog noticeDialog = builder.create();
        noticeDialog.setCancelable(false);
        noticeDialog.show();
    }

    /**
     * 显示软件更新对话框
     */
    private void showNoticeDialog() {
        // 构造对话框
        Builder builder = new Builder(mContext);
        builder.setTitle(R.string.soft_update_title);
        builder.setMessage(R.string.soft_update_info);
        builder.setIcon(R.drawable.ic_info);
        // 更新
        builder.setPositiveButton(R.string.soft_update_updatebtn, new OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
//                showDownloadDialog();
                Uri uri = Uri.parse(updatePath);
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                mContext.startActivity(intent);
                dialog.dismiss();
                // 显示下载对话框
            }
        });
        //稍后更新
        builder.setNegativeButton(R.string.soft_update_later, new OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                dialog.dismiss();
            }
        });

        Dialog noticeDialog = builder.create();
        noticeDialog.setCancelable(false);
        noticeDialog.show();
    }

    /**
     * 显示软件下载对话框
     */
    private void showDownloadDialog() {
        // 构造软件下载对话框
        Builder builder = new Builder(mContext);
        builder.setTitle(R.string.soft_updating);
        builder.setIcon(R.drawable.ic_info);
        // 给下载对话框增加进度条
        final LayoutInflater inflater = LayoutInflater.from(mContext);
        View v = inflater.inflate(R.layout.softupdate_progress, null);
        mProgress = (ProgressBar) v.findViewById(R.id.update_progress);
        builder.setView(v);

        mDownloadDialog = builder.create();
        mDownloadDialog.setCancelable(false);
        mDownloadDialog.show();
        // 现在文件
        downloadApk();
    }

    /**
     * 下载apk文件
     */
    private void downloadApk() {
        // 启动新线程下载软件
        new downloadApkThread().start();
    }

    /**
     * 下载文件线程
     *
     * @author coolszy
     * @date 2012-4-26
     * @blog http://blog.92coding.com
     */
    private class downloadApkThread extends Thread {
        @Override
        public void run() {
            try {
                // 判断SD卡是否存在，并且是否具有读写权限
                if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                    // 获得存储卡的路径
                    String sdpath = Environment.getExternalStorageDirectory() + "/";
                    mSavePath = sdpath + "download";
                    URL url = new URL(updatePath);
                    // 创建连接
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.connect();
                    // 获取文件大小
                    int length = conn.getContentLength();
                    // 创建输入流
                    InputStream is = conn.getInputStream();

                    File file = new File(mSavePath);
                    // 判断文件目录是否存在
                    if (!file.exists()) {
                        file.mkdir();
                    }
                    apkName = "NtripShare" + DateUtils.dateToString(new Date(), "yyyyMMddHHmmss");
                    File apkFile = new File(mSavePath, apkName);
                    FileOutputStream fos = new FileOutputStream(apkFile);
                    int count = 0;
                    // 缓存
                    byte buf[] = new byte[1024];
                    // 写入到文件中
                    do {
                        int numread = is.read(buf);
                        count += numread;
                        // 计算进度条位置
                        progress = (int) (((float) count / length) * 100);
                        // 更新进度
                        mHandler.sendEmptyMessage(DOWNLOAD);
                        if (numread <= 0) {
                            // 下载完成
                            mHandler.sendEmptyMessage(DOWNLOAD_FINISH);
                            break;
                        }
                        // 写入文件
                        fos.write(buf, 0, numread);
                    } while (!cancelUpdate);// 点击取消就停止下载.
                    fos.close();
                    is.close();
                }
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            // 取消下载对话框显示
            mDownloadDialog.dismiss();
        }
    }


    /**
     * 安装APK文件
     */
    private void installApk() {
        File apkfile = new File(mSavePath, apkName);
        if (!apkfile.exists()) {
            return;
        }
        // 通过Intent安装APK文件
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        i.setDataAndType(Uri.parse("file://" + apkfile.toString()), "application/vnd.android.package-archive");
        mContext.startActivity(i);
    }

    public interface UpdateHandler {
        public void onfindUpdate(String re);

        public void onfindUpdateFail(String msg);
    }
}
