package com.bj4.yhh.accountant.fragments.display.actcontent;

import android.content.Context;
import android.os.AsyncTask;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.BackgroundColorSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ViewSwitcher;

import com.bj4.yhh.accountant.R;
import com.bj4.yhh.accountant.act.Act;
import com.bj4.yhh.accountant.act.ActContent;
import com.bj4.yhh.accountant.act.Article;
import com.bj4.yhh.accountant.act.Chapter;
import com.bj4.yhh.accountant.fragments.plan.Plan;
import com.bj4.yhh.accountant.fragments.test.TestItem;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;

/**
 * Created by yenhsunhuang on 15/7/18.
 */
public class ActContentAdapter extends BaseAdapter {
    public interface Callback {
        void onQueryDone();
    }

    public static final int SORT_BY_WRONG_TIME = 0;
    public static final int SORT_BY_ARTICLE = 1;

    private static final boolean DEBUG = false;
    private static final String TAG = "ActContentAdapter";
    private final Act mAct;
    private final Context mContext;
    private final LayoutInflater mInflater;
    private final int mDisplayType;

    private int mSortingType = SORT_BY_ARTICLE;

    private String mQueryString = "";

    private boolean mQueryHighLight = false;
    private boolean mQueryImageNote = false;
    private boolean mQueryTextNote = false;

    private Callback mCallback;


    private int mLikeBackgroundColor;

    private final ArrayList<ActContent> mData = new ArrayList<ActContent>();
    private final ArrayList<ActContent> mQueryData = new ArrayList<ActContent>();

    public ActContentAdapter(Context context, Act act) {
        this(context, act, DisplayActContentFragment.ARGUS_DISPLAY_TYPE_NONE);
    }

    public ActContentAdapter(Context context, Act act, int displayType) {
        mAct = act;
        mContext = context;
        mDisplayType = displayType;
        mInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mLikeBackgroundColor = context.getResources().getColor(R.color.main_title_color);
        initData();
    }

    public void setSortingType(int sortingType) {
        if (DEBUG) Log.d(TAG, "sortingType: " + sortingType);
        mSortingType = sortingType;
        queryAsync();
    }

    public void queryHighLight(boolean query) {
        mQueryHighLight = query;
        queryAsync();
    }

    public void queryImageNote(boolean query) {
        mQueryImageNote = query;
        queryAsync();
    }

    public void queryTextNote(boolean query) {
        mQueryTextNote = query;
        queryAsync();
    }

    public boolean isQueryHighLight() {
        return mQueryHighLight;
    }

    public boolean isQueryImageNote() {
        return mQueryImageNote;
    }

    public boolean isQueryTextNote() {
        return mQueryTextNote;
    }

    public void queryByLike(String like) {
        if (DEBUG) Log.d(TAG, "queryByLike, like: " + like);
        mQueryString = like;
        queryAsync();
        notifyDataSetChanged();
    }

    public boolean isQueryString() {
        return !"".equals(mQueryString) && !TextUtils.isEmpty(mQueryString);
    }

    public boolean isQuery() {
        return isQueryHighLight() || isQueryImageNote() || isQueryTextNote() || isQueryString();
    }

    public void resetQueryStatus() {
        mQueryHighLight = mQueryImageNote = mQueryTextNote = false;
        mQueryString = "";
        queryAsync();
        notifyDataSetChanged();
    }

    public void updateContent() {
        initData();
    }

    private static class AsyncQuery extends AsyncTask<Void, Void, Void> {
        final ArrayList<ActContent> tempQueryData = new ArrayList<ActContent>();
        final ArrayList<ActContent> queryData;
        final String queryString;
        final boolean queryHighLight, queryImageNote, queryTextNote;
        final int likeBackgroundColor;
        final ActContentAdapter actContentAdapter;
        final int sortingType;
        final Context context;
        final Act act;

        AsyncQuery(ActContentAdapter actContentAdapter, ArrayList<ActContent> queryData, ArrayList<ActContent> allData,
                   String queryString, boolean queryHighLight, boolean queryImageNote, boolean queryTextNote,
                   int likeBackgroundColor, int sortingType, Context context, Act act) {
            this.queryString = queryString;
            this.queryHighLight = queryHighLight;
            this.queryImageNote = queryImageNote;
            this.queryTextNote = queryTextNote;
            this.likeBackgroundColor = likeBackgroundColor;
            this.queryData = queryData;
            this.actContentAdapter = actContentAdapter;
            this.sortingType = sortingType;
            this.context = context;
            this.act = act;
            tempQueryData.addAll(allData);
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            queryData.clear();
            queryData.addAll(tempQueryData);
            actContentAdapter.notifyDataSetChanged();
            if (actContentAdapter.mCallback != null) {
                actContentAdapter.mCallback.onQueryDone();
            }
        }

        @Override
        protected Void doInBackground(Void... params) {
            for (ActContent content : tempQueryData) {
                content.resetDisplayContent();
            }
            // check query string
            if (!"".equals(queryString) && !TextUtils.isEmpty(queryString)) {
                Iterator<ActContent> actContentIter = tempQueryData.iterator();
                while (actContentIter.hasNext()) {
                    final ActContent content = actContentIter.next();
                    SpannableString spannableString = content.mSpannableContent;
                    boolean isLike = false;
                    int indexOfString = -1;
                    do {
                        indexOfString = content.mContent.indexOf(queryString, indexOfString + 1);
                        if (indexOfString >= 0) {
                            spannableString.setSpan(new BackgroundColorSpan(likeBackgroundColor), indexOfString, indexOfString + queryString.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                            isLike = true;
                        }
                    } while (indexOfString >= 0);
                    if (!isLike) {
                        actContentIter.remove();
                    }
                }
            }

            // check query high light
            if (queryHighLight) {
                Iterator<ActContent> actContentIter = tempQueryData.iterator();
                while (actContentIter.hasNext()) {
                    final ActContent content = actContentIter.next();
                    if (!content.mHasHighLight) {
                        actContentIter.remove();
                    }
                }
            }
            if (queryImageNote) {
                Iterator<ActContent> actContentIter = tempQueryData.iterator();
                while (actContentIter.hasNext()) {
                    final ActContent content = actContentIter.next();
                    if (!content.mHasImageNote) {
                        actContentIter.remove();
                    }
                }
            }
            if (queryTextNote) {
                Iterator<ActContent> actContentIter = tempQueryData.iterator();
                while (actContentIter.hasNext()) {
                    final ActContent content = actContentIter.next();
                    if (!content.mHasTextNote) {
                        actContentIter.remove();
                    }
                }
            }

            if (sortingType == SORT_BY_WRONG_TIME) {
                Plan plan = Plan.queryByActId(context, act.getId());
                if (plan != null) {
                    ArrayList<TestItem> testItems = TestItem.queryTestItem(context, null, TestItem.PLAN_ID + "=" + plan.mId, null, TestItem.FAILED_TIME);
                    if (testItems != null && !testItems.isEmpty()) {
                        Iterator<ActContent> actContentIter = tempQueryData.iterator();
                        while (actContentIter.hasNext()) {
                            final ActContent content = actContentIter.next();
                            for (TestItem item : testItems) {
                                if (content instanceof Chapter) {
                                    if (item.mChapterId == content.mId) {
                                        content.mFailedTime = item.mFailedTime;
                                    }
                                } else if (content instanceof Article) {
                                    if (item.mArticleId == content.mId) {
                                        content.mFailedTime = item.mFailedTime;
                                        Log.d(TAG, "failedTime: " + content.mFailedTime);
                                    }
                                }
                            }
                        }
                    }
                }
                Collections.sort(tempQueryData, new Comparator<ActContent>() {
                    @Override
                    public int compare(ActContent lhs, ActContent rhs) {
                        if (lhs.mFailedTime < rhs.mFailedTime) return 1;
                        else if (lhs.mFailedTime > rhs.mFailedTime) return -1;
                        else return 0;
                    }
                });
            }


            return null;
        }
    }

    private void queryAsync() {
        new AsyncQuery(this, mQueryData, mData, mQueryString, mQueryHighLight, mQueryImageNote,
                mQueryTextNote, mLikeBackgroundColor, mSortingType, mContext, mAct).executeOnExecutor(AsyncTask.SERIAL_EXECUTOR);
    }

    public void setCallback(Callback cb) {
        mCallback = cb;
    }

    private static class AsyncInitData extends AsyncTask<Void, Void, Void> {
        final Act act;
        final Context context;
        final ArrayList<ActContent> data;
        final ActContentAdapter adapter;
        final ArrayList<ActContent> tempData = new ArrayList<ActContent>();
        final int displayType;

        public AsyncInitData(Act act, Context context, ArrayList<ActContent> data, ActContentAdapter adapter, int displayType) {
            this.act = act;
            this.context = context;
            this.data = data;
            this.adapter = adapter;
            this.displayType = displayType;
        }

        @Override
        protected Void doInBackground(Void... params) {
            Act.queryAllActContent(context, act);
            for (Chapter chapter : act.getChapters()) {
                if (chapter.isEmptyChapter()) {
                    // ignore empty chapter
                } else {
                    if (displayType == DisplayActContentFragment.ARGUS_DISPLAY_TYPE_REVIEW_MODE) {
                    } else {
                        tempData.add(chapter);
                    }
                }
                tempData.addAll(chapter.getArticles());
            }
            for (ActContent content : tempData) {
                content.updateNoteStatus(context);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            data.clear();
            data.addAll(tempData);
            adapter.queryAsync();
        }
    }

    private void initData() {
        new AsyncInitData(mAct, mContext, mData, this, mDisplayType).executeOnExecutor(AsyncTask.SERIAL_EXECUTOR);
    }

    @Override
    public int getCount() {
        return mQueryData.size();
    }

    @Override
    public ActContent getItem(int position) {
        return mQueryData.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.display_act_content_fragment_adapter, null);
            holder = new ViewHolder();
            holder.mChapterContent = (TextView) convertView.findViewById(R.id.txt_chapter_content);

            holder.mArticleContent = (TextView) convertView.findViewById(R.id.txt_article_content);
            holder.mArticleNumber = (TextView) convertView.findViewById(R.id.txt_article_number);
            holder.mContentSwitcher = (ViewSwitcher) convertView.findViewById(R.id.act_content_switcher);
            holder.mChapterImportant = (ImageView) convertView.findViewById(R.id.chapter_is_important);
            holder.mChapterHasTextNote = (ImageView) convertView.findViewById(R.id.chapter_has_text_note);
            holder.mChapterHasImageNote = (ImageView) convertView.findViewById(R.id.chapter_has_image_note);
            holder.mArticleImportant = (ImageView) convertView.findViewById(R.id.article_is_important);
            holder.mArticleHasTextNote = (ImageView) convertView.findViewById(R.id.article_has_text_note);
            holder.mArticleHasImageNote = (ImageView) convertView.findViewById(R.id.article_has_image_note);

            holder.mArticleWrongTime = (TextView) convertView.findViewById(R.id.txt_article_wrong_time);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        final ActContent item = getItem(position);
        if (DEBUG) Log.v(TAG, item.toString());
        if (item instanceof Chapter) {
            holder.mContentSwitcher.setDisplayedChild(0);

            holder.mChapterContent.setText(item.mNumber + " " + item.mSpannableContent);
            holder.mArticleContent.setText("");
            holder.mArticleNumber.setText("");

            holder.mChapterImportant.setVisibility(item.mHasHighLight ? View.VISIBLE : View.GONE);
            holder.mChapterHasTextNote.setVisibility(item.mHasTextNote ? View.VISIBLE : View.GONE);
            holder.mChapterHasImageNote.setVisibility(item.mHasImageNote ? View.VISIBLE : View.GONE);

            holder.mArticleContent.setVisibility(View.GONE);
            holder.mArticleNumber.setVisibility(View.GONE);
            holder.mChapterContent.setVisibility(View.VISIBLE);

            holder.mArticleWrongTime.setVisibility(View.GONE);
        } else {
            holder.mContentSwitcher.setDisplayedChild(1);

            holder.mChapterContent.setText("");
            holder.mArticleContent.setText(item.mSpannableContent);
            holder.mArticleNumber.setText(item.mNumber);

            holder.mArticleImportant.setVisibility(item.mHasHighLight ? View.VISIBLE : View.GONE);
            holder.mArticleHasTextNote.setVisibility(item.mHasTextNote ? View.VISIBLE : View.GONE);
            holder.mArticleHasImageNote.setVisibility(item.mHasImageNote ? View.VISIBLE : View.GONE);

            holder.mArticleContent.setVisibility(View.VISIBLE);
            holder.mArticleNumber.setVisibility(View.VISIBLE);
            holder.mChapterContent.setVisibility(View.GONE);

            if (item.mFailedTime <= 0) {
                holder.mArticleWrongTime.setVisibility(View.GONE);
                holder.mArticleWrongTime.setText(null);
            } else {
                holder.mArticleWrongTime.setVisibility(View.VISIBLE);
                holder.mArticleWrongTime.setText(mContext.getResources().getString(R.string.act_content_adapter_wrong_time, String.valueOf(item.mFailedTime)));
            }
        }
        return convertView;
    }

    private static class ViewHolder {
        TextView mChapterContent;
        TextView mArticleContent, mArticleNumber;
        ViewSwitcher mContentSwitcher;
        ImageView mChapterImportant, mChapterHasTextNote, mChapterHasImageNote;
        ImageView mArticleImportant, mArticleHasTextNote, mArticleHasImageNote;
        TextView mArticleWrongTime;
    }

    public int getChapterItemIndex(long chapterId) {
        for (ActContent content : mQueryData) {
            if (content instanceof Chapter) {
                if (((Chapter) content).mId == chapterId) {
                    return mQueryData.indexOf(content);
                }
            }
        }
        return -1;
    }
}
