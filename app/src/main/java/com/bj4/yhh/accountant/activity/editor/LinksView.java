package com.bj4.yhh.accountant.activity.editor;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Rect;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bj4.yhh.accountant.R;
import com.bj4.yhh.accountant.act.Act;
import com.bj4.yhh.accountant.act.ActContent;
import com.bj4.yhh.accountant.act.Article;
import com.bj4.yhh.accountant.act.Chapter;
import com.bj4.yhh.accountant.utils.dialogs.ConfirmDialogFragment;

import java.util.ArrayList;
import java.util.Collections;

/**
 * Created by Yen-Hsun_Huang on 2015/9/3.
 */
public class LinksView extends LinearLayout {
    private final ArrayList<Long> mLinks = new ArrayList<Long>();
    private final Context mContext;
    private FrameLayout mMainContainer;
    private final int mLinkReadingWindowMargin;
    private ActContent mActContent;

    public static boolean sIsShowing = false;

    private Activity mActivity;

    private Long mPendingTag = -1l;

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
        mLinkReadingWindowMargin = context.getResources().getDimensionPixelSize(R.dimen.link_reading_window_margin);
        sIsShowing = false;
    }

    public void setActivity(Activity activity) {
        mActivity = activity;
    }

    public void setMainContainer(FrameLayout main) {
        mMainContainer = main;
    }

    public void setActContent(ActContent actContent) {
        mActContent = actContent;
        if (actContent == null || actContent.mLinks == null) return;
        mLinks.clear();
        mLinks.addAll(actContent.mLinks);
        removeAllViews();
        inflateAll();
    }

    public boolean deleteItem() {
        if (mPendingTag == -1) return false;
        if (mActContent == null) return false;
        if (!mActContent.mLinks.contains(mPendingTag)) return false;
        mActContent.mLinks.remove(mPendingTag);
        mActContent.updateLinks(mContext);
        setActContent(mActContent);
        mPendingTag = -1l;
        return true;
    }

    private void inflateAll() {
        if (mLinks.isEmpty()) return;
        final LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        Collections.sort(mLinks);
        for (Long link : mLinks) {
            final TextView text = (TextView) inflater.inflate(R.layout.links_view_item, null);
            text.setTag(link);
            text.setText(R.string.links_view_loading_text);
            new DisplayTextTask(mContext, link, text, mMainContainer, mLinkReadingWindowMargin).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            addView(text);
            text.setOnLongClickListener(new OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    mPendingTag = -1l;
                    Long link = (Long) view.getTag();
                    if (link == null) return false;
                    mPendingTag = link;
                    ConfirmDialogFragment dialog = new ConfirmDialogFragment();
                    Bundle args = new Bundle();
                    args.putString(ConfirmDialogFragment.ARGUS_TITLE, mContext.getResources().getString(R.string.links_view_delete_item_confirm_title));
                    args.putString(ConfirmDialogFragment.ARGUS_MESSAGE, mContext.getResources().getString(R.string.links_view_delete_item_confirm_content, text.getText().toString()));
                    dialog.setArguments(args);
                    dialog.show(mActivity.getFragmentManager(), dialog.getClass().getName());
                    return true;
                }
            });
        }
    }

    private static class DisplayTextTask extends AsyncTask<Void, Void, String> {
        private final Context mContext;
        private final Long mArticleId;
        private final TextView mTextView;
        private final FrameLayout mMainContainer;
        private final int mLinkReadingWindowMargin;
        private Act mAct;
        private Chapter mChapter;
        private Article mArticle;

        DisplayTextTask(Context context, long id, TextView txt, FrameLayout main, int windowMargin) {
            mContext = context;
            mArticleId = id;
            mTextView = txt;
            mMainContainer = main;
            mLinkReadingWindowMargin = windowMargin;
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
                    sIsShowing = true;
                    LinkReadingWindow window = new LinkReadingWindow(mContext);
                    window.setData(mAct, mChapter, mArticle);
                    window.setCallback(new LinkReadingWindow.Callback() {
                        @Override
                        public void onHide(View self) {
                            if (mMainContainer != null) {
                                mMainContainer.removeView(self);
                            }
                            sIsShowing = false;
                        }
                    });
                    Rect viewRect = new Rect();
                    view.getGlobalVisibleRect(viewRect);
                    FrameLayout.LayoutParams fl = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT);
                    window.setPadding(mLinkReadingWindowMargin, mLinkReadingWindowMargin, mLinkReadingWindowMargin, mLinkReadingWindowMargin);
                    fl.gravity = Gravity.CENTER;
                    mMainContainer.addView(window, fl);
                    window.setPivotX(viewRect.centerX());
                    window.setPivotY(viewRect.centerY());
                    window.setScaleX(0);
                    window.setScaleY(0);
                    window.animate().scaleX(1).scaleY(1).setDuration(300).start();
                }
            });
        }
    }
}
