package com.giserpeng.ntripshare.ui.log;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationManagerCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.giserpeng.ntripshare.MainActivity;
import com.giserpeng.ntripshare.R;

import java.util.Timer;
import java.util.TimerTask;

public class LogFragment extends Fragment {

    TextView textLogConnect;
    TextView textLog;
    ScrollView svLog;
    ScrollView svLogConnect;
    Timer timer;
    private static LogFragment Instance;

    public  static LogFragment  getInstance(){
        return Instance;
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        Instance = this;
        View root = inflater.inflate(R.layout.fragment_log, container, false);
        this.textLog = root.findViewById(R.id.textLog);
        this.svLog = root.findViewById(R.id.svLog);
        this.textLogConnect = root.findViewById(R.id.textLogConnect);
        this.svLogConnect = root.findViewById(R.id.svLogConnect);
        startTimer();
        updateLog();
        return root;
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    private void startTimer() {
        Log.i("HomeFragment", "startTimer()");
        if (timer == null) {
            timer = new Timer();
        }
        if(task == null){
            task = new TimerTask() {
                @Override
                public void run() {
                    updateLog();
                }
            };
        }

        if (timer != null && task != null) {
            timer.schedule(task, 1000, 1000);
        }
    }


    TimerTask task = new TimerTask() {
        @Override
        public void run() {
            updateLog();
        }
    };
    private void stopTimer() {
        Log.i("HomeFragment", "stopTimer()");
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
        if (task != null) {
            task.cancel();
            task = null;
        }
    }

    private void  updateLog(){
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                textLogConnect.setText(MainActivity.CONNECT_LOG);
                svLogConnect.fullScroll(130);
                textLog.setText(MainActivity.LOG);
                svLog.fullScroll(130);
            }
        });
    }

    public void onDestroyView() {
        stopTimer();
        super.onDestroyView();
    }
}
