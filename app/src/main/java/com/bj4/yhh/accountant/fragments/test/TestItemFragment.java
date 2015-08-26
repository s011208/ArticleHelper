package com.bj4.yhh.accountant.fragments.test;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.ViewSwitcher;

import com.bj4.yhh.accountant.R;
import com.bj4.yhh.accountant.act.Act;
import com.bj4.yhh.accountant.act.Article;
import com.bj4.yhh.accountant.act.Chapter;
import com.bj4.yhh.accountant.dialogs.OutlineDialogFragment;
import com.bj4.yhh.accountant.fragments.plan.Plan;
import com.bj4.yhh.accountant.fragments.testmode.TestModeItemFragment;
import com.bj4.yhh.accountant.utils.BaseFragment;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

/**
 * Created by yenhsunhuang on 15/8/20.
 */
public class TestItemFragment extends BaseFragment {
    public interface Callback {
        Plan getPlan();

        ArrayList<TestItem> getTestItems();

        int getTestType();
    }

    private static final String TAG = "TestItemFragment";
    private static final boolean DEBUG = true;

    static final int TEST_BY_NUMBER = 0;
    static final int TEST_BY_CONTENT = 1;
    private int mTestBy = TEST_BY_NUMBER;

    private Callback mCallback;
    private Plan mPlan;
    private ArrayList<TestItem> mAllTestItems;
    private ArrayList<TestItem> mTestScopeItems = new ArrayList<TestItem>();
    private int mCurrentDay;
    private boolean mIsInReadingMode = true;
    private TestItem mCurrentTestItem;
    private Article mCurrentArticle;
    private Chapter mCurrentChapter;

    private TextView mActTitle, mActInfo;
    private ImageView mOutlineBtn;

    private ViewSwitcher mButtonArea;
    private Button mYes, mNo, mNext;
    private boolean mIsWrongAnswer = false;

    private TextView mRemainTestItems;

    private TextView mQuestions;
    private ListView mAnswers;
    private TestAnswerAdapter mTestAnswerAdapter;
    private boolean mAnswerClickable = true;
    private int mMaximumQuestionDisplayLineWhenReading, mMaximumQuestionDisplayLineWhenTesting;

    private boolean mTestMode = false;

    private static int getTestBy() {
        return (int) ((Math.random() * 10000) % 2);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mMaximumQuestionDisplayLineWhenReading = getActivity().getResources().getInteger(R.integer.test_item_fragment_maximum_question_line_when_reading);
        mMaximumQuestionDisplayLineWhenTesting = getActivity().getResources().getInteger(R.integer.test_item_fragment_maximum_question_line_when_testing);
        if (DEBUG) {
            Log.d(TAG, "max read lines: " + mMaximumQuestionDisplayLineWhenReading
                    + ", max test lines: " + mMaximumQuestionDisplayLineWhenTesting);
        }
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
        final int type = mCallback == null ? TestModeItemFragment.TEST_TYPE_BY_DEFAULT : mCallback.getTestType();
        switch (type) {
            case TestModeItemFragment.TEST_TYPE_BY_PLAN:
                mTestMode = true;
                for (TestItem item : mAllTestItems) {
                    if (item.mDisplayDay <= mCurrentDay) {
                        mTestScopeItems.add(item);
                    }
                }
                break;
            case TestModeItemFragment.TEST_TYPE_BY_ALL:
                mTestMode = true;
                mTestScopeItems.addAll(mAllTestItems);
                break;
            case TestModeItemFragment.TEST_TYPE_BY_ALL_RANDOMLY:
                mTestMode = true;
                mTestScopeItems.addAll(mAllTestItems);
                Collections.shuffle(mTestScopeItems, new Random(System.nanoTime()));
                break;
            default:
                mTestMode = false;
                for (TestItem item : mAllTestItems) {
                    if (item.mDisplayDay == mCurrentDay) {
                        mTestScopeItems.add(item);
                    }
                }
        }
        if (mTestMode) {
            for (TestItem item : mAllTestItems) {
                item.mIsRead = true;
            }
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.test_item_fragment, null);
        initComponents(root);
        return root;
    }

    private void moveToNextItem() {
        mIsWrongAnswer = false;
        final boolean moveToNext = nextItem();
        if (!moveToNext) return;
        updateActInfo();
        updateRemainItemCount();
        updateQuestionText();
        updateAnswerList();
        updateButtonsVisibility();
    }

    private void setCurrentData(TestItem testItem) {
        mCurrentTestItem = testItem;
        mCurrentChapter = getTestItemChapter(getActivity(), testItem);
        mCurrentArticle = getTestItemArticle(getActivity(), testItem);
        if (DEBUG) {
            Log.v(TAG, "mCurrentTestItem is null: " + (mCurrentTestItem == null));
            Log.v(TAG, "mCurrentChapter is null: " + (mCurrentChapter == null));
            Log.v(TAG, "mCurrentArticle is null: " + (mCurrentArticle == null));
        }
    }

    private TestItem getCurrentTestItem() {
        return mCurrentTestItem;
    }

    public static Chapter getTestItemChapter(Context context, TestItem testItem) {
        Chapter rtn = null;
        ArrayList<Chapter> chapters = Chapter.queryChapterByChapterId(context, testItem.mChapterId);
        if (!chapters.isEmpty()) {
            rtn = chapters.get(0);
        }
        return rtn;
    }

    public static Article getTestItemArticle(Context context, TestItem testItem) {
        Article rtn = null;
        ArrayList<Article> articles = Article.quertArticleByArticleId(context, testItem.mArticleId);
        if (!articles.isEmpty()) {
            rtn = articles.get(0);
        }
        return rtn;
    }

    private void resetCurrentData() {
        mCurrentTestItem = null;
        mCurrentArticle = null;
        mCurrentChapter = null;
    }

    private void updateButtonsVisibility() {
        if (mIsInReadingMode) {
            mButtonArea.setDisplayedChild(0);
            mButtonArea.setVisibility(View.VISIBLE);
        } else {
            mButtonArea.setDisplayedChild(1);
            mButtonArea.setVisibility(mIsWrongAnswer ? View.VISIBLE : View.INVISIBLE);
        }
    }

    private boolean nextItem() {
        boolean rtn = false;
        resetCurrentData();
        if (mIsInReadingMode) {
            for (TestItem testItem : mTestScopeItems) {
                if (!testItem.mIsRead) {
                    mIsInReadingMode = true;
                    setCurrentData(testItem);
                    mTestScopeItems.remove(testItem);
                    mTestScopeItems.add(testItem);
                    rtn = true;
                    break;
                }

            }
            if (!rtn) {
                mIsInReadingMode = false;
                loadTestScope();
            }
        }
        if (!mIsInReadingMode) {
            for (TestItem testItem : mTestScopeItems) {
                if (!testItem.mIsAnswer) {
                    setCurrentData(testItem);
                    mTestScopeItems.remove(testItem);
                    mTestScopeItems.add(testItem);
                    rtn = true;
                    break;

                }
            }
        }
        if (getCurrentTestItem() == null) {
            rtn = false;
            updatePlanAndExit();
        }
        return rtn;
    }

    private void updatePlanAndExit() {
        ++mPlan.mCurrentPlanProgress;
        Plan.insertOrUpdate(getActivity(), mPlan);
        getActivity().onBackPressed();
    }

    private void updateActInfo() {
        if (mCurrentTestItem != null) {
            Log.i(TAG, "test item: " + mCurrentTestItem);

            if (mCurrentChapter.isEmptyChapter()) {
                mActInfo.setText(null);
            } else {
                mActInfo.setText(mCurrentChapter.mNumber);
            }
            if (DEBUG) {
                Log.d(TAG, "chapter: " + mCurrentChapter);
            }
        } else {
            mActInfo.setText(null);
        }
        if (DEBUG) Log.d(TAG, "updateActInfo, mIsInReadingMode: " + mIsInReadingMode
                + ", mActInfo text: " + mActInfo.getText());
    }

    private void updateRemainItemCount() {
        int count = mTestScopeItems.size();
        if (mIsInReadingMode) {
            for (TestItem item : mTestScopeItems) {
                if (item.mIsRead) --count;
            }
        } else {
            for (TestItem item : mTestScopeItems) {
                if (item.mIsAnswer) --count;
            }
        }
        mRemainTestItems.setText(getActivity().getResources().getString(R.string.test_item_fragment_remain_item_text, String.valueOf(count)));
    }

    private void updateQuestionText() {
        if (mCurrentArticle != null) {
            mTestBy = getTestBy();
            switch (mTestBy) {
                case TEST_BY_CONTENT:
                    mQuestions.setText(mCurrentArticle.mContent);
                    break;
                case TEST_BY_NUMBER:
                    mQuestions.setText(mCurrentArticle.mNumber);
                    break;
            }
        } else {
            mQuestions.setText(null);
        }
        if (mIsInReadingMode) {
            mQuestions.setMaxLines(mMaximumQuestionDisplayLineWhenReading);
        } else {
            mQuestions.setMaxLines(mMaximumQuestionDisplayLineWhenTesting);
        }
        mQuestions.scrollTo(mQuestions.getScrollX(), 0);

        if (DEBUG) Log.v(TAG, "mCurrentArticle: " + mCurrentArticle);
    }

    private void updateAnswerList() {
        if (mIsInReadingMode) {
            mAnswers.setVisibility(View.INVISIBLE);
        } else {
            mAnswers.setVisibility(View.VISIBLE);
            mTestAnswerAdapter.updateListByCurrentData(mTestBy, mCurrentTestItem);
            mAnswers.setSelection(0);
        }
    }

    private void initComponents(View root) {
        mActTitle = (TextView) root.findViewById(R.id.act_title);
        mActInfo = (TextView) root.findViewById(R.id.act_detail_info);
        mOutlineBtn = (ImageView) root.findViewById(R.id.outline_btn);
        mOutlineBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Act act = mPlan.getAct();
                OutlineDialogFragment.showDialog(act, getFragmentManager());
            }
        });

        mActTitle.setText(mPlan.getActTitle());

        mButtonArea = (ViewSwitcher) root.findViewById(R.id.button_area);
        mYes = (Button) root.findViewById(R.id.yes);
        mYes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCurrentTestItem.mIsRead = true;
                TestItem.update(getActivity(), mCurrentTestItem);
                moveToNextItem();
            }
        });
        mNo = (Button) root.findViewById(R.id.no);
        mNo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                moveToNextItem();
            }
        });
        mNext = (Button) root.findViewById(R.id.next);
        mNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TestItem.update(getActivity(), mCurrentTestItem);
                moveToNextItem();
                mAnswerClickable = true;
            }
        });

        mRemainTestItems = (TextView) root.findViewById(R.id.remain_test_items);

        mQuestions = (TextView) root.findViewById(R.id.question_text);
        mQuestions.setMovementMethod(new ScrollingMovementMethod());
        mAnswers = (ListView) root.findViewById(R.id.answer_list);
        mAnswers.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (!mAnswerClickable) return;
                final boolean isCorrect = mTestAnswerAdapter.checkIsCorrect(position);
                if (isCorrect) {
                    mCurrentTestItem.mIsAnswer = true;
                    if (!mTestMode) {
                        TestItem.update(getActivity(), mCurrentTestItem);
                        ++mPlan.mFinishedItem;
                        Plan.insertOrUpdate(getActivity(), mPlan);
                    }
                    moveToNextItem();
                } else {
                    mIsWrongAnswer = true;
                    mCurrentTestItem.mFailedTime++;
                    TestItem.update(getActivity(), mCurrentTestItem);
                    updateButtonsVisibility();
                    mAnswerClickable = false;
                }
            }
        });
        mTestAnswerAdapter = new TestAnswerAdapter(getActivity(), mAllTestItems, mTestScopeItems);
        mAnswers.setAdapter(mTestAnswerAdapter);

        moveToNextItem();
    }

    @Override
    public boolean onBackPress() {
        return false;
    }
}
