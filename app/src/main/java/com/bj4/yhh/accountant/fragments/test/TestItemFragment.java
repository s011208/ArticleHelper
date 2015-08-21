package com.bj4.yhh.accountant.fragments.test;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.bj4.yhh.accountant.R;
import com.bj4.yhh.accountant.act.Article;
import com.bj4.yhh.accountant.act.Chapter;
import com.bj4.yhh.accountant.fragments.plan.Plan;
import com.bj4.yhh.accountant.utils.BaseFragment;

import java.util.ArrayList;

/**
 * Created by yenhsunhuang on 15/8/20.
 */
public class TestItemFragment extends BaseFragment {
    public interface Callback {
        Plan getPlan();

        ArrayList<TestItem> getTestItems();
    }

    private static final String TAG = "TestItemFragment";
    private static final boolean DEBUG = true;

    private static final int TEST_BY_NUMBER = 0;
    private static final int TEST_BY_CONTENT = 1;

    private Callback mCallback;
    private Plan mPlan;
    private ArrayList<TestItem> mAllTestItems;
    private ArrayList<TestItem> mTestScopeItems = new ArrayList<TestItem>();
    private int mCurrentDay;
    private boolean mIsInReadingMode = true;
    private TestItem mCurrentTestItem;

    private TextView mActTitle, mActInfo;
    private ImageView mOutlineBtn;

    private Button mYes, mNo;

    private TextView mRemainTestItems;

    private TextView mQuestions;
    private ListView mAnswers;

    private static int getTestBy() {
        return (int) ((Math.random() * 10000) % 2);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (activity instanceof Callback) {
            mCallback = (Callback) activity;
            mPlan = mCallback.getPlan();
            mAllTestItems = mCallback.getTestItems();
            mCurrentDay = mPlan.mCurrentPlanProgress;
            loadTestScope();
            if (DEBUG) {
                Log.d(TAG, "plan: " + mPlan);
            }
        }
    }

    private void loadTestScope() {
        mTestScopeItems.clear();
        for (TestItem item : mAllTestItems) {
            if (item.mDisplayDay == mCurrentDay) {
                mTestScopeItems.add(item);
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.test_item_fragment, null);
        initComponents(root);
        return root;
    }

    private boolean moveToNextItem() {
        mCurrentTestItem = null;
        if (mIsInReadingMode) {
            for (TestItem testItem : mTestScopeItems) {
                if (!testItem.mIsRead) {
                    mIsInReadingMode = true;
                    mCurrentTestItem = testItem;
                    return true;
                }

            }
        }
        mIsInReadingMode = false;
        for (TestItem testItem : mTestScopeItems) {
            if (!testItem.mIsAnswer) {
                mCurrentTestItem = testItem;
                return true;

            }
        }
        return false;
    }

    private void updateActInfo() {
        if (mCurrentTestItem != null) {
            Log.i(TAG, "test item: " + mCurrentTestItem);
            ArrayList<Chapter> chapters = Chapter.queryChapterByChapterId(getActivity(), mCurrentTestItem.mChapterId);
            if (chapters.isEmpty()) return;
            ArrayList<Article> articles = Article.quertArticleByArticleId(getActivity(), mCurrentTestItem.mArticleId);
            if (articles.isEmpty()) return;
            Chapter chapter = chapters.get(0);
            Article article = articles.get(0);
            mActInfo.setText(chapter.mNumber + ", " + article.mNumber);
            if (DEBUG) {
                Log.d(TAG, "chapter: " + chapter);
                Log.d(TAG, "article: " + article);
            }
        }
    }

    private void initComponents(View root) {
        moveToNextItem();
        mActTitle = (TextView) root.findViewById(R.id.act_title);
        mActInfo = (TextView) root.findViewById(R.id.act_detail_info);
        mOutlineBtn = (ImageView) root.findViewById(R.id.outline_btn);

        mActTitle.setText(mPlan.getActTitle());
        updateActInfo();

        mYes = (Button) root.findViewById(R.id.yes);
        mYes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mCurrentTestItem == null)
                    return;
                mCurrentTestItem.mIsRead = true;
                moveToNextItem();
                updateActInfo();
            }
        });
        mNo = (Button) root.findViewById(R.id.no);

        mRemainTestItems = (TextView) root.findViewById(R.id.remain_test_items);

        mQuestions = (TextView) root.findViewById(R.id.question_text);
        mAnswers = (ListView) root.findViewById(R.id.answer_llist);
    }

    @Override
    public boolean onBackPress() {
        return false;
    }
}
