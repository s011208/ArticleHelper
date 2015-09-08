package com.bj4.yhh.accountant.activity.editor;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bj4.yhh.accountant.R;

import java.util.ArrayList;

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
        mLinks.add(123l);
        mLinks.add(456l);
        removeAllViews();
        inflateAll();
    }

    private void inflateAll() {
        if (mLinks.isEmpty()) return;
        final LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        for (Long link : mLinks) {
            TextView text = (TextView) inflater.inflate(R.layout.links_view_item, null);
            text.setText(link.toString());
            text.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {

                }
            });
            addView(text);
        }
    }
}
