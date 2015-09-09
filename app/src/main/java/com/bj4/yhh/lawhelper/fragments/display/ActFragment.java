package com.bj4.yhh.lawhelper.fragments.display;

import android.os.Bundle;
import android.util.Log;

import com.bj4.yhh.lawhelper.act.Act;
import com.bj4.yhh.lawhelper.utils.BaseFragment;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by yenhsunhuang on 15/7/18.
 */
public abstract class ActFragment extends BaseFragment {
    public static final boolean DEBUG = true;
    public static final String TAG = "ActFragment";

    public static final String ARGUMENT_JSON_ACT = "argument_json_act";

    public Act mAct;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle argument = getArguments();
        if (argument != null) {
            try {
                mAct = new Act(new JSONObject(argument.getString(ARGUMENT_JSON_ACT)));
            } catch (JSONException e) {
                if (DEBUG) {
                    Log.w(TAG, "failed", e);
                }
            }
        }
    }
}
