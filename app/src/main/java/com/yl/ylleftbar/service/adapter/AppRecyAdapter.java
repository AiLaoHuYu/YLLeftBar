package com.yl.ylleftbar.service.adapter;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;

import android.app.Instrumentation;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.yl.ylleftbar.R;
import com.yl.ylleftbar.service.model.AppInfoModel;
import com.yl.ylleftbar.service.utils.AnimationUtil;
import com.yl.ylleftbar.service.utils.CommonUtil;
import com.yl.ylleftbar.service.utils.ScreenUtil;


import java.util.List;

public class AppRecyAdapter extends BaseRecyclerViewAdapter<RecyclerView.ViewHolder, AppInfoModel> {

    private boolean isMute = false;

    public AppRecyAdapter(Context mContext, List<AppInfoModel> dataList) {
        super(mContext, dataList);
    }

    @Override
    protected RecyclerView.ViewHolder baseCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.main_left_recy_item, parent, false);
        return new MainLeftViewHolder(view);
    }

    @Override
    protected int baseGetItemViewType(int position) {
        return dataList.get(position).getViewHolderType();
    }

    @Override
    protected void baseItemClick(View v, int position) {
        try {
            if (dataList.get(position).getLabelName().equals("back")) {
                if (!"com.yl.yldesktop".equals(CommonUtil.getForegroundActivity(mContext))) {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            Instrumentation inst = new Instrumentation();
                            inst.sendKeyDownUpSync(KeyEvent.KEYCODE_BACK);
                        }
                    }).start();
                } else {
                    Toast.makeText(mContext, "当前已经在桌面啦！", Toast.LENGTH_SHORT).show();
                }
            } else if (dataList.get(position).getLabelName().equals("mute")) {
                if (isMute) {
                    silentSwitchOff();
                } else {
                    silentSwitchOn();
                }
                isMute = !isMute;
            } else if (dataList.get(position).getLabelName().equals("voice_assiant")) {
                mContext.sendBroadcast(new Intent("com.yl.start.voice"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void silentSwitchOn() {
        AudioManager audioManager = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
        if (audioManager != null) {
            // 静音所有音频流
            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, 0, 0);
            audioManager.setStreamVolume(AudioManager.STREAM_RING, 0, 0);
            audioManager.setStreamVolume(AudioManager.STREAM_NOTIFICATION, 0, 0);
            audioManager.setStreamVolume(AudioManager.STREAM_SYSTEM, 0, 0);
            Toast.makeText(mContext, "已静音", Toast.LENGTH_SHORT).show();
        }
    }

    private void silentSwitchOff() {
        AudioManager audioManager = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
        if (audioManager != null) {
            // 恢复默认音量（50%）
            int maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, maxVolume / 2, 0);
            audioManager.setStreamVolume(AudioManager.STREAM_RING, maxVolume / 2, 0);
            audioManager.setStreamVolume(AudioManager.STREAM_NOTIFICATION, maxVolume / 2, 0);
            audioManager.setStreamVolume(AudioManager.STREAM_SYSTEM, maxVolume / 2, 0);
            Toast.makeText(mContext, "已解除静音", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected boolean baseOnTouch(RecyclerView.ViewHolder holder, MotionEvent event, int position) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                AnimationUtil.startScaleUpAnimation(((MainLeftViewHolder) holder).imageView);
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                AnimationUtil.startScaleDownAnimation(((MainLeftViewHolder) holder).imageView);
                break;
        }
        return false;
    }

    @Override
    protected void bindView(RecyclerView.ViewHolder holder, int position) {
        RequestOptions options = new RequestOptions().transform(new RoundedCorners(ScreenUtil.dp2px(mContext, 10)));
        Glide.with(mContext).load(dataList.get(position).getDrawable()).apply(options).into(((MainLeftViewHolder) holder).imageView);
    }

    class MainLeftViewHolder extends RecyclerView.ViewHolder {

        private ImageView imageView;

        public MainLeftViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.main_left_item_img);
        }
    }


}
