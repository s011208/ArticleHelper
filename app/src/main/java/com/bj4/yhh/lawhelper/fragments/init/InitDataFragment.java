package com.bj4.yhh.lawhelper.fragments.init;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bj4.yhh.lawhelper.AccountDataHelper;
import com.bj4.yhh.lawhelper.R;
import com.bj4.yhh.lawhelper.activity.MainActivity;
import com.bj4.yhh.lawhelper.fragments.entry.MainEntryFragment;
import com.bj4.yhh.lawhelper.utils.BaseFragment;
import com.bj4.yhh.lawhelper.utils.BaseToast;
import com.bj4.yhh.lawhelper.utils.Utils;

/**
 * Created by Yen-Hsun_Huang on 2015/8/20.
 */
public class InitDataFragment extends BaseFragment implements AccountDataHelper.Callback {
    private static final String TAG = "InitDataFragment";
    private static final boolean DEBUG = true;
    private AccountDataHelper mAccountDataHelper;

    private TextView mProgressText;

    private int mLocalActListCount = 0;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAccountDataHelper = AccountDataHelper.getInstance(getActivity());
        mAccountDataHelper.addCallback(this);
        mLocalActListCount = mAccountDataHelper.getAllActListCount();
        if (DEBUG) {
            Log.d(TAG, "mAccountDataHelper.isRetrieveDataSuccess(): " + mAccountDataHelper.isRetrieveDataSuccess());
            Log.d(TAG, "mLocalActListCount: " + mLocalActListCount);
        }
        if (Utils.haveInternet(getActivity())) {
            if (DEBUG) Log.d(TAG, "have internet");
            if (mAccountDataHelper.isRetrieveDataSuccess()) {
                gotoMainEntryFragment(false);
            } else {
                mAccountDataHelper.parseAllActListFromParse();
            }
        } else {
            if (DEBUG) Log.w(TAG, "Don't have internet");
            if (mLocalActListCount <= 0) {
                BaseToast.showToast(getActivity(), R.string.init_data_fragment_please_connect_to_internet_at_first_time);
            } else {
                BaseToast.showToast(getActivity(), R.string.init_data_fragment_please_connect_to_internet);
                gotoMainEntryFragment(false);
            }
        }
    }

    @Override
    public void onDestroy() {
        mAccountDataHelper.removeCallback(this);
        super.onDestroy();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.init_data_fragment, null);
        mProgressText = (TextView) root.findViewById(R.id.progress_text);
        mProgressText.setText(getActivity().getString(R.string.init_data_fragment_percentage, "0"));
        TextView skip = (TextView) root.findViewById(R.id.skip_loading);
        skip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                gotoMainEntryFragment(true);
            }
        });
        if (mLocalActListCount <= 0) {
            skip.setVisibility(View.INVISIBLE);
        }
        return root;
    }

    private void gotoMainEntryFragment(final boolean withAnimation) {
        Runnable task = new Runnable() {
            @Override
            public void run() {
                if (withAnimation) {
                    getFragmentManager().beginTransaction().setCustomAnimations(R.anim.fragment_alpha_in, R.anim.fragment_alpha_out).replace(MainActivity.getMainFragmentContainerId(), new MainEntryFragment()).commitAllowingStateLoss();
                } else {
                    getFragmentManager().beginTransaction().replace(MainActivity.getMainFragmentContainerId(), new MainEntryFragment()).commitAllowingStateLoss();
                }
            }
        };
        if (android.os.Process.myPid() == android.os.Process.myTid()) {
            task.run();
        } else {
            getActivity().runOnUiThread(task);
        }
    }

    @Override
    public boolean onBackPress() {
        return false;
    }

    @Override
    public void onStartRetrieveAllActDataFromParse() {
        if (DEBUG) Log.d(TAG, "onStartRetrieveAllActDataFromParse");
    }

    @Override
    public void onFinishRetrieveAllActDataFromParse() {
        if (DEBUG) Log.d(TAG, "onFinishRetrieveAllActDataFromParse");
        gotoMainEntryFragment(true);
    }

    @Override
    public void onProgressUpdate(int progress, String extraMessage) {
        mProgressText.setText(getActivity().getString(R.string.init_data_fragment_percentage, String.valueOf(progress)));
    }
}
