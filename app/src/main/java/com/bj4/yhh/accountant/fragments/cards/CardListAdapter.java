package com.bj4.yhh.accountant.fragments.cards;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.bj4.yhh.accountant.R;

import java.util.ArrayList;

/**
 * Created by yenhsunhuang on 15/7/12.
 */
public class CardListAdapter extends BaseAdapter {
    private final Context mContext;
    private final LayoutInflater mInflater;
    private final ArrayList<Card> mCards = new ArrayList<Card>();

    public CardListAdapter(Context context) {
        mContext = context;
        mInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        initTestData();
    }

    private void initTestData(){
        mCards.add(new Card("1", "txt"));
        mCards.add(new Card("2", "txt"));
        mCards.add(new Card("3", "txt"));
        mCards.add(new Card("4", "txt"));
        mCards.add(new Card("5", "txt"));
        mCards.add(new Card("6", "txt"));
        mCards.add(new Card("7", "txt"));
        mCards.add(new Card("8", "txt"));
        mCards.add(new Card("9", "txt"));
        mCards.add(new Card("10", "txt"));
        mCards.add(new Card("11", "txt"));
        mCards.add(new Card("12", "txt"));
        mCards.add(new Card("13", "txt"));
        mCards.add(new Card("14", "txt"));
        mCards.add(new Card("15", "txt"));
        mCards.add(new Card("16", "txt"));
        mCards.add(new Card("17", "txt"));
        mCards.add(new Card("18", "txt"));
        mCards.add(new Card("19", "txt"));
        mCards.add(new Card("20", "txt"));
    }

    @Override
    public int getCount() {
        return mCards.size();
    }

    @Override
    public Card getItem(int position) {
        return mCards.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        CardView cardView;
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.card_view, null);
        }
        cardView = (CardView) convertView;
        Card card = getItem(position);
        convertView.setTag(card);
        cardView.setCard(card);

        return convertView;
    }
}
