package com.bj4.yhh.accountant.activity.editor;

import android.content.Context;
import android.graphics.Color;
import android.os.AsyncTask;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bj4.yhh.accountant.R;
import com.bj4.yhh.accountant.act.Act;
import com.bj4.yhh.accountant.act.Article;
import com.bj4.yhh.accountant.act.Chapter;

import java.util.ArrayList;
import java.util.Collections;

/**
 * Created by Yen-Hsun_Huang on 2015/9/3.
 */
public class LinksView extends LinearLayout {
    private final ArrayList<Long> mLinks = new ArrayList<Long>();
    private final Context mContext;

    public LinksView(Context context) {
        this(context, null);
    }

    public LinksView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public LinksView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
        setOrientation(LinearLayout.VERTICAL);
    }

    public void setLinks(ArrayList<Long> links) {
        if (links == null) return;
        mLinks.clear();
        mLinks.addAll(links);
        removeAllViews();
        inflateAll();
    }

    private void inflateAll() {
        if (mLinks.isEmpty()) return;
        final LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        Collections.sort(mLinks);
        for (Long link : mLinks) {
            TextView text = (TextView) inflater.inflate(R.layout.links_view_item, null);
            text.setText(R.string.links_view_loading_text);
            new DisplayTextTask(mContext, link, text).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            addView(text);
        }
    }

    private static class DisplayTextTask extends AsyncTask<Void, Void, String> {
        private final Context mContext;
        private final Long mArticleId;
        private final TextView mTextView;
        private Act mAct;
        private Chapter mChapter;
        private Article mArticle;

        DisplayTextTask(Context context, long id, TextView txt) {
            mContext = context;
            mArticleId = id;
            mTextView = txt;
        }

        @Override
        protected String doInBackground(Void... voids) {
            ArrayList<Article> articles = Article.quertArticleByArticleId(mContext, mArticleId);
            if (articles.isEmpty()) return null;
            mArticle = articles.get(0);
            ArrayList<Chapter> chapters = Chapter.queryChapterByChapterId(mContext, mArticle.mChapterId);
            if (chapters.isEmpty()) return null;
            mChapter = chapters.get(0);
            mAct = Act.queryActById(mContext, mChapter.mActId);
            if (mAct == null) return null;
            String displayString = "[ " + mAct.getTitle() + " ]";
            if (!mChapter.isEmptyChapter()) {
                displayString += "[ " + mChapter.mNumber + " ]";
            }
            displayString += " " + mArticle.mNumber;
            return displayString;
        }

        @Override
        protected void onPostExecute(String displayString) {
            if (displayString == null) {
                mTextView.setText(R.string.links_view_lost_data_text);
                return;
            }
            mTextView.setText(displayString);
            mTextView.setTextColor(Color.rgb(0x33, 0x33, 0xff));
            mTextView.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {

                }
            });
        }
    }
}
