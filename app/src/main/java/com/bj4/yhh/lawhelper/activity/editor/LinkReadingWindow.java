package com.bj4.yhh.lawhelper.activity.editor;

import android.animation.Animator;
import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bj4.yhh.lawhelper.R;
import com.bj4.yhh.lawhelper.act.Act;
import com.bj4.yhh.lawhelper.act.Article;
import com.bj4.yhh.lawhelper.act.Chapter;

/**
 * Created by Yen-Hsun_Huang on 2015/9/9.
 */
public class LinkReadingWindow extends RelativeLayout {
    private final Context mContext;

    private Act mAct;
    private Chapter mChapter;
    private Article mArticle;

    private Callback mCallback;

    public LinkReadingWindow(Context context) {
        this(context, null);
    }

    public LinkReadingWindow(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public LinkReadingWindow(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // hide when touch space area
        hide();
        return true;
    }

    private void initContent() {
        removeAllViews();
        View content = ((LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.link_reading_window_content, null);
        RelativeLayout.LayoutParams rl = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        rl.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);
        addView(content, rl);

        TextView actArea = (TextView) content.findViewById(R.id.act_area);
        TextView chapterArea = (TextView) content.findViewById(R.id.chapter_area);
        TextView articleArea = (TextView) content.findViewById(R.id.article_area);

        actArea.setText(mAct.getTitle());
        if (!mChapter.isEmptyChapter()) {
            chapterArea.setText(mChapter.mNumber);
        }
        articleArea.setText(mArticle.mNumber + "\n" + mArticle.mContent);

        ImageView closeWindowButton = (ImageView) content.findViewById(R.id.close_window_btn);
        closeWindowButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                hide();
            }
        });
    }

    public void setData(Act act, Chapter chapter, Article article) {
        mAct = act;
        mChapter = chapter;
        mArticle = article;
        initContent();
    }

    public void show(Rect startRect) {
        setPivotX(startRect.centerX());
        setPivotY(startRect.centerY());
        setScaleX(0);
        setScaleY(0);
        setAlpha(0);
        animate().scaleX(1).scaleY(1).alpha(1).setDuration(300).start();
    }

    public void setCallback(Callback cb) {
        mCallback = cb;
    }

    public void hide() {
        animate().scaleX(0).scaleY(0).alpha(0).setDuration(300).setListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {

            }

            @Override
            public void onAnimationEnd(Animator animator) {
                if (mCallback != null) {
                    mCallback.onHide(LinkReadingWindow.this);
                }
            }

            @Override
            public void onAnimationCancel(Animator animator) {

            }

            @Override
            public void onAnimationRepeat(Animator animator) {

            }
        }).start();
    }

    public interface Callback {
        void onHide(View self);
    }
}
