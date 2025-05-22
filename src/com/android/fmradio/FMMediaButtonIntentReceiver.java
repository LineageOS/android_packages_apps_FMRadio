package com.android.fmradio;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.KeyEvent;

/* loaded from: classes.dex */
public class FMMediaButtonIntentReceiver extends BroadcastReceiver {
    public interface MediaHandleListener {
        public boolean handleIntent(Intent intent);
    }
    private MediaHandleListener listener;
    public void setMediaHandleListener(MediaHandleListener listener) {
        this.listener = listener;
    }
    @Override
    public void onReceive(Context context, Intent intent) {
        if(listener!=null) listener.handleIntent(intent);
    }
}
