package com.bj4.yhh.accountant.activity;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.bj4.yhh.accountant.R;
import com.bj4.yhh.accountant.fragments.plan.Plan;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Yen-Hsun_Huang on 2015/8/20.
 */
public class TestActivity extends BaseActivity {
    private static final boolean DEBUG = true;
    private static final String TAG = "TestActivity";

    public static final String EXTRA_PLAN = "extra_plan";

    private Plan mPlan;

    private TextView mActTitle, mActInfo;
    private ImageView mOutlineBtn;

    private Button mYes, mNo;

    private TextView mRemainTestItems;

    private TextView mQuestions;
    private ListView mAnswers;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            mPlan = new Plan(new JSONObject(getIntent().getStringExtra(EXTRA_PLAN)));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        if (DEBUG) {
            Log.d(TAG, "Plan: " + mPlan);
        }
        setContentView(R.layout.activity_test);
        initComponents();
    }

    private void initComponents() {
        mActTitle = (TextView) findViewById(R.id.act_title);
        mActInfo = (TextView) findViewById(R.id.act_detail_info);
        mOutlineBtn = (ImageView) findViewById(R.id.outline_btn);

        mYes = (Button) findViewById(R.id.yes);
        mNo = (Button) findViewById(R.id.no);

        mRemainTestItems = (TextView) findViewById(R.id.remain_test_items);

        mQuestions = (TextView) findViewById(R.id.question_text);
        mAnswers = (ListView) findViewById(R.id.answer_llist);
    }
}
