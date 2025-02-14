package com.whosmyqueen.oim.service;

import static androidx.core.app.NotificationCompat.PRIORITY_MIN;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Color;
import android.inputmethodservice.InputMethodService;
import android.os.Build;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import com.whosmyqueen.oim.R;
import com.whosmyqueen.oim.keyboard.InputModeSwitcher;
import com.whosmyqueen.oim.keyboard.SkbContainer;
import com.whosmyqueen.oim.utils.MeasureHelper;
import com.whosmyqueen.oim.utils.OPENLOG;

/**
 * 输入法服务.
 *
 * @author hailong.qiu 356752238@qq.com
 */
public class IMEService extends InputMethodService {

    private static final String TAG = "IMEService";

    InputModeSwitcher mInputModeSwitcher;
    SkbContainer mSkbContainer;
    EditorInfo mSaveEditorInfo;

    @SuppressLint("ForegroundServiceType")
    @Override
    public void onCreate() {
        super.onCreate();
        startForeground();
        OPENLOG.D(TAG, "onCreate");
        // 读取屏幕的宽高.
        MeasureHelper measureHelper = MeasureHelper.getInstance();
        measureHelper.onConfigurationChanged(getResources().getConfiguration(), this);
        //
        mInputModeSwitcher = new InputModeSwitcher();
    }

    @Override
    public View onCreateInputView() {
        OPENLOG.D(TAG, "onCreateInputView");
        LayoutInflater inflater = getLayoutInflater();
        mSkbContainer = (SkbContainer) inflater.inflate(R.layout.skb_container, null);
        mSkbContainer.setInputModeSwitcher(mInputModeSwitcher);
        return mSkbContainer;
    }

    @Override
    public void onStartInput(EditorInfo editorInfo, boolean restarting) {
        OPENLOG.D(TAG, "onStartInput");
        mSaveEditorInfo = editorInfo;
    }

    @Override
    public void onStartInputView(EditorInfo editorInfo, boolean restarting) {
        OPENLOG.D(TAG, "onStartInputView");
        // 根据inputType设置软键盘样式.
        mInputModeSwitcher.setInputMode(editorInfo);
        mSkbContainer.updateInputMode(null);
    }

    @Override
    public void onDestroy() {
        OPENLOG.D(TAG, "onDestroy");
        super.onDestroy();
    }

    /**
     * 1. onConfigurationChanged事件并不是只有屏幕方向改变才可以触发，<br>
     * 其他的一些系统设置改变也可以触发，比如打开或者隐藏键盘。<br>
     * 2. 屏幕方向发生改变时 <br>
     * TV版本，暂时不处理关于屏幕旋转的傻B问题.
     */
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        OPENLOG.D(TAG, "onConfigurationChanged newConfig:" + newConfig);
        MeasureHelper measureHelper = MeasureHelper.getInstance();
        measureHelper.onConfigurationChanged(newConfig, this);
        super.onConfigurationChanged(newConfig);
    }

    /**
     * 发送字符到编辑框(EditText)
     */
    public void commitResultText(String resultText) {
        OPENLOG.D(TAG, "commitResultText resultText:" + resultText);
        InputConnection ic = getCurrentInputConnection();
        if (null != ic && !TextUtils.isEmpty(resultText)) {
            ic.commitText(resultText, 1);
        }
    }

    public static final int MAX_INT = Integer.MAX_VALUE / 2 - 1;

    public void setCursorRightMove() {
        int cursorPos = getSelectionStart();
        cursorPos++;
        getCurrentInputConnection().setSelection(cursorPos, cursorPos);
    }

    public void setCursorLeftMove() {
        int cursorPos = getSelectionStart();
        cursorPos -= 1;
        if (cursorPos < 0) {
            cursorPos = 0;
        }
        getCurrentInputConnection().setSelection(cursorPos, cursorPos);
    }

    private int getSelectionStart() {
        return getCurrentInputConnection().getTextBeforeCursor(MAX_INT, 0).length();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // 防止输入法退出还在监听事件.
        if (isImeServiceStop()) {
            OPENLOG.D(TAG, "onKeyDown isImeServiceStop keyCode:" + keyCode);
            return super.onKeyDown(keyCode, event);
        }
        OPENLOG.D(TAG, "onKeyDown keyCode:" + keyCode);
        if (mSkbContainer.onSoftKeyDown(keyCode, event)) {
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        // 防止输入法退出还在监听事件.
        if (isImeServiceStop()) {
            OPENLOG.D(TAG, "onKeyUp isImeServiceStop keyCode:" + keyCode);
            return super.onKeyDown(keyCode, event);
        }
        OPENLOG.D(TAG, "onKeyUp keyCode:" + keyCode);
        if (mSkbContainer != null && mSkbContainer.onSoftKeyUp(keyCode, event)) {
            return true;
        }
        return super.onKeyUp(keyCode, event);
    }

    /**
     * 防止输入法退出还在监听事件.
     */
    public boolean isImeServiceStop() {
        return ((mSkbContainer == null) || !isInputViewShown());
    }

    /**
     * 防止全屏.
     */
    @Override
    public boolean onEvaluateFullscreenMode() {
        return false;
    }

    /**
     * 启动前台服务
     */
    private void startForeground() {
        String channelId = null;
        // 8.0 以上需要特殊处理
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            channelId = createNotificationChannel("kim.hsl", "ForegroundService");
        } else {
            channelId = "";
        }
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, channelId);
        Notification notification = builder.setOngoing(true)
                .setPriority(PRIORITY_MIN)
                .setCategory(Notification.CATEGORY_SERVICE)
                .build();
        startForeground(1, notification);
    }


    /**
     * 创建通知通道
     * @param channelId
     * @param channelName
     * @return
     */
    @RequiresApi(Build.VERSION_CODES.O)
    private String createNotificationChannel(String channelId, String channelName){
        NotificationChannel chan = new NotificationChannel(channelId,
                channelName, NotificationManager.IMPORTANCE_NONE);
        chan.setLightColor(Color.BLUE);
        chan.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
        NotificationManager service = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        service.createNotificationChannel(chan);
        return channelId;
    }
}
