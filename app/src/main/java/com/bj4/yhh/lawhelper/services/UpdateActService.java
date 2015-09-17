package com.bj4.yhh.lawhelper.services;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import com.bj4.yhh.lawhelper.act.Act;

/**
 * Created by yenhsunhuang on 15/9/9.
 */
public class UpdateActService extends IntentService {
    private static final boolean DEBUG = true;
    private static final String TAG = "UpdateActService";

    public static final String ACTION_UPDATE_ACT = "action_update_act";
    public static final String EXTRA_ACT = "extra_act";

    public UpdateActService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        final String action = intent.getAction();
        if (ACTION_UPDATE_ACT.equals(action)) {
            String jsonAct = intent.getStringExtra(EXTRA_ACT);
            if (jsonAct != null) {
                Act updateAct = new Act(jsonAct);
                updateAct(updateAct);
            }
        }
    }

    private void updateAct(Act act) {
        if (act == null) return;
        if (DEBUG) Log.d(TAG, "updateAct act: " + act);
    }
}
