package com.bj4.yhh.accountant.fragments.test;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.bj4.yhh.accountant.R;
import com.bj4.yhh.accountant.act.Article;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

/**
 * Created by yenhsunhuang on 15/8/22.
 */
public class TestAnswerAdapter extends BaseAdapter {
    private static final boolean DEBUG = true;
    private static final String TAG = "TestAnswerAdapter";
    private static final int MAXIMUM_ANSWER_ITEMS = 5;

    private final ArrayList<TestItem> mAllTestItems;
    private final ArrayList<TestItem> mTestScopeItems;
    private final Context mContext;
    private final LayoutInflater mInflater;
    private final ArrayList<String> mAnswers = new ArrayList<String>();

    public TestAnswerAdapter(Context context, ArrayList<TestItem> allTestItems, ArrayList<TestItem> testScopeItems) {
        mContext = context;
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mAllTestItems = allTestItems;
        mTestScopeItems = testScopeItems;
    }

    public void updateListByCurrentData(int testBy, TestItem currentTestItem) {
        mAnswers.clear();
        ArrayList<TestItem> tempTestScope = new ArrayList<TestItem>(mTestScopeItems);
        if (tempTestScope.size() < MAXIMUM_ANSWER_ITEMS) {
            tempTestScope.clear();
            tempTestScope.addAll(mAllTestItems);
        }
        tempTestScope.remove(currentTestItem);
        Collections.shuffle(tempTestScope, new Random(System.nanoTime()));
        for (int i = 0; i < tempTestScope.size() && i < MAXIMUM_ANSWER_ITEMS; i++) {
            final TestItem item = tempTestScope.get(i);
            final Article itemArticle = TestItemFragment.getTestItemArticle(mContext, item);
            if (testBy == TestItemFragment.TEST_BY_CONTENT) {
                mAnswers.add(itemArticle.mNumber);
            } else if (testBy == TestItemFragment.TEST_BY_NUMBER) {
                mAnswers.add(itemArticle.mContent);
            }
        }
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return mAnswers.size();
    }

    @Override
    public String getItem(int position) {
        return mAnswers.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.test_item_fragment_answer_list_item, null);
            holder = new ViewHolder();
            holder.mAnswer = (TextView) convertView.findViewById(R.id.answer);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        holder.mAnswer.setText(getItem(position));
        return convertView;
    }

    private static class ViewHolder {
        TextView mAnswer;
    }
}
