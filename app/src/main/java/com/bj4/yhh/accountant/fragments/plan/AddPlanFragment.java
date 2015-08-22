package com.bj4.yhh.accountant.fragments.plan;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.bj4.yhh.accountant.R;
import com.bj4.yhh.accountant.act.Act;
import com.bj4.yhh.accountant.act.Article;
import com.bj4.yhh.accountant.act.Chapter;
import com.bj4.yhh.accountant.fragments.test.TestItem;
import com.bj4.yhh.accountant.utils.BaseFragment;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.Random;

/**
 * Created by yenhsunhuang on 15/8/18.
 */
public class AddPlanFragment extends BaseFragment implements View.OnClickListener {
    private static final String TAG = "AddPlanFragment";
    private static final boolean DEBUG = false;

    private Act mSelectedAct;
    private int mTotalPlanDay = 7;
    private int mOrderBy = Plan.ORDER_BY_ARTICLE;
    private int mTotalArticleCount;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.add_plan_fragment, null);
        Button btnOk = (Button) root.findViewById(R.id.ok);
        btnOk.setOnClickListener(this);
        Button btnCancel = (Button) root.findViewById(R.id.cancel);
        btnCancel.setOnClickListener(this);

        final TextView articleCount = (TextView) root.findViewById(R.id.total_article_counr);
        Spinner actSpinner = (Spinner) root.findViewById(R.id.act_spinner);
        final ArrayList<Act> acts = Act.query(getActivity(), null, null, null, null);
        final ArrayList<Plan> plans = Plan.query(getActivity());
        for (Plan plan : plans) {
            for (Act act : acts) {
                if (plan.mActId == act.getId()) {
                    acts.remove(act);
                    break;
                }
            }
        }
        final ArrayList<String> actsName = new ArrayList<String>();
        for (Act act : acts) {
            actsName.add(act.getTitle());
        }
        if (!acts.isEmpty())
            mSelectedAct = acts.get(0);
        ArrayAdapter<String> actSpinnerAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_dropdown_item, actsName);
        actSpinner.setAdapter(actSpinnerAdapter);
        actSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                mSelectedAct = acts.get(position);
                if (DEBUG) {
                    Log.d(TAG, "onItemSelected, act: " + mSelectedAct.getTitle());
                }
                mTotalArticleCount = Act.getArticleCount(getActivity(), mSelectedAct);
                articleCount.setText(String.valueOf(mTotalArticleCount));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        EditText totalPlanDays = (EditText) root.findViewById(R.id.total_day);
        totalPlanDays.setText(String.valueOf(mTotalPlanDay));
        totalPlanDays.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                try {
                    mTotalPlanDay = Integer.valueOf(s.toString());
                } catch (Exception e) {
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        Spinner orderSpinner = (Spinner) root.findViewById(R.id.act_order_spinner);
        final String[] orderSpinnerArray = getActivity().getResources().getStringArray(R.array.add_plan_fragment_order);
        ArrayAdapter<String> orderSpinnerAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_dropdown_item, orderSpinnerArray);
        orderSpinner.setAdapter(orderSpinnerAdapter);
        orderSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (DEBUG) {
                    Log.d(TAG, "order by: " + orderSpinnerArray[position]);
                }
                mOrderBy = position;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });


        return root;
    }

    @Override
    public boolean onBackPress() {
        return false;
    }

    @Override
    public void onClick(View v) {
        final int vId = v.getId();
        if (vId == R.id.ok) {
            if (DEBUG) {
                Log.d(TAG, "ok");
            }
            if (mSelectedAct == null) {
                // TODO show add new act toast
                return;
            }
            if (mTotalPlanDay <= 0) {
                mTotalPlanDay = 1;
            }

            Plan plan = new Plan(mSelectedAct.getId(), mOrderBy, mTotalPlanDay, 0, mTotalArticleCount, 0);
            if (DEBUG) Log.v(TAG, "plan: " + plan.toString());
            Plan.insertOrUpdate(getActivity(), plan);
            new InsertTestItemTask(getActivity(), plan, mTotalPlanDay, mOrderBy).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        } else if (vId == R.id.cancel) {
            if (DEBUG) {
                Log.d(TAG, "cancel");
            }
        }
        getActivity().onBackPressed();
    }

    private static class InsertTestItemTask extends AsyncTask<Void, Void, Void> {
        private final Plan mPlan;
        private final int mTotalDay;
        private final int mOrderBy;
        private final Context mContext;

        public InsertTestItemTask(Context context, Plan plan, int totalDay, int orderBy) {
            mContext = context;
            mPlan = plan;
            mTotalDay = totalDay;
            mOrderBy = orderBy;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            mPlan.initAct(mContext);
            final Act act = mPlan.getAct();
            Act.queryAllActContent(mContext, act);
            final int totalArticleCount = Act.getArticleCount(mContext, act);
            final int itemPerDay = totalArticleCount / mTotalDay;
            final long planId = mPlan.mId;
            final long actId = act.getId();
            final ArrayList<TestItem> testItems = new ArrayList<TestItem>();
            for (Chapter chapter : act.getChapters()) {
                final long chapterId = chapter.mId;
                for (Article article : chapter.getArticles()) {
                    final long articleId = article.mId;
                    testItems.add(new TestItem(planId, actId, chapterId, articleId, -1));
                    if (DEBUG) Log.v(TAG, "article: " + article);
                }
                if (DEBUG) {
                    Log.i(TAG, "chapter: " + chapter);
                }
            }
            if (mOrderBy == Plan.ORDER_BY_ARTICLE) {
            } else if (mOrderBy == Plan.ORDER_BY_RANDOM) {
                Collections.shuffle(testItems, new Random(System.nanoTime()));
            }
            int displayDay = 0;
            int itemCounter = 0;
            int remainItemCount = totalArticleCount % mTotalDay; // distribute to every day
            Iterator<TestItem> itemIterator = testItems.iterator();
            while (itemIterator.hasNext()) {
                TestItem item = itemIterator.next();
                item.mDisplayDay = displayDay;
                ++itemCounter;
                if (itemCounter % itemPerDay == 0) {
                    if (remainItemCount != 0) {
                        item = itemIterator.next();
                        item.mDisplayDay = displayDay;
                        --remainItemCount;
                    }
                    ++displayDay;
                }
            }
            int numInserted = TestItem.bulkInsert(mContext, testItems);

            if (DEBUG) Log.d(TAG, "insert: " + numInserted);
            return null;
        }
    }
}
