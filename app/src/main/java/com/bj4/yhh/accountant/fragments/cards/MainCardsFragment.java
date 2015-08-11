package com.bj4.yhh.accountant.fragments.cards;

import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;

import com.bj4.yhh.accountant.R;

/**
 * Created by yenhsunhuang on 15/7/12.
 */
public class MainCardsFragment extends Fragment {
    private static final boolean DEBUG = true;
    private static final String TAG = "MainCardsFragment";
    private CardListView mCardListView;
    private CardListAdapter mCardListAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.main_card_fragment, null);
        mCardListView = (CardListView) rootView.findViewById(R.id.card_list_container);
        mCardListAdapter = new CardListAdapter(getActivity());
        mCardListView.setAdapter(mCardListAdapter);
        mCardListView.setCallback(new CardListView.Callback() {
            @Override
            public void onFling(View onFlingView) {
                Card card = (Card) onFlingView.getTag();
                if (DEBUG) {
                    Log.v(TAG, "card: " + card.getTitle());
                }
            }

            @Override
            public void onFinishAllAnimator() {
                final int index = mCardListView.getFirstVisiblePosition();
                View v = mCardListView.getChildAt(0);
                final int top = (v == null) ? 0 : (v.getTop() - mCardListView.getPaddingTop());
                mCardListView.setAdapter(mCardListAdapter);
                mCardListView.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
                    @Override
                    public boolean onPreDraw() {
                        final ViewTreeObserver observer = mCardListView.getViewTreeObserver();
                        if (observer.isAlive()) {
                            observer.removeOnPreDrawListener(this);
                        }
                        try {
                            mCardListView.setSelectionFromTop(index, top);
                        } catch (Exception e) {
                            mCardListView.setSelection(index);
                        }
                        return false;
                    }
                });
            }
        });
        return rootView;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

}
