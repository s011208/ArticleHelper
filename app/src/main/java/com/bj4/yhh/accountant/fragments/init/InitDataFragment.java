package com.bj4.yhh.accountant.fragments.init;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bj4.yhh.accountant.AccountDataHelper;
import com.bj4.yhh.accountant.R;
import com.bj4.yhh.accountant.activity.MainActivity;
import com.bj4.yhh.accountant.fragments.entry.MainEntryFragment;
import com.bj4.yhh.accountant.utils.BaseFragment;
import com.bj4.yhh.accountant.utils.BaseToast;
import com.bj4.yhh.accountant.utils.Utils;

/**
 * Created by Yen-Hsun_Huang on 2015/8/20.
 */
public class InitDataFragment extends BaseFragment implements AccountDataHelper.Callback {
    private static final String TAG = "InitDataFragment";
    private static final boolean DEBUG = true;
    private AccountDataHelper mAccountDataHelper;

    private TextView mProgressText;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAccountDataHelper = AccountDataHelper.getInstance(getActivity());
        mAccountDataHelper.addCallback(this);
        if (DEBUG) {
            Log.d(TAG, "mAccountDataHelper.isRetrieveDataSuccess(): " + mAccountDataHelper.isRetrieveDataSuccess());
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
            // TODO toast connect to wifi to update
            gotoMainEntryFragment(false);
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
        TextView mSkip = (TextView) root.findViewById(R.id.skip_loading);
        mSkip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                gotoMainEntryFragment(true);
            }
        });
        return root;
    }

    private void gotoMainEntryFragment(final boolean withAnimation) {
        if (withAnimation) {
            getFragmentManager().beginTransaction().setCustomAnimations(R.anim.fragment_alpha_in, R.anim.fragment_alpha_out).replace(MainActivity.getMainFragmentContainerId(), new MainEntryFragment()).commit();
        } else {
            getFragmentManager().beginTransaction().replace(MainActivity.getMainFragmentContainerId(), new MainEntryFragment()).commit();
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
    public void onProgressUpdate(int progress) {
        mProgressText.setText(getActivity().getString(R.string.init_data_fragment_percentage, String.valueOf(progress)));
    }
}
