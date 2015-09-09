package com.bj4.yhh.lawhelper.fragments.cards;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bj4.yhh.lawhelper.R;

/**
 * Created by yenhsunhuang on 15/7/12.
 */
public class CardView extends LinearLayout {
    private TextView mTitle, mContent;
    private Card mCard;

    public CardView(Context context) {
        this(context, null);
    }

    public CardView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CardView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mTitle = (TextView) findViewById(R.id.title);
        mContent = (TextView) findViewById(R.id.content);
    }

    public void setCard(Card card) {
        if (card == null)
            return;
        mCard = card;
        mTitle.setText(mCard.getTitle());
        mContent.setText(mCard.getContent());
    }
}
