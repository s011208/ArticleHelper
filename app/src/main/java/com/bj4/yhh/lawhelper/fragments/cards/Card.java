package com.bj4.yhh.lawhelper.fragments.cards;

/**
 * Created by yenhsunhuang on 15/7/12.
 */
public class Card {
    private String mTitle;
    private String mContent;

    public Card(String title, String content) {
        mTitle = title;
        mContent = content;
    }

    public String getTitle() {
        return mTitle;
    }

    public String getContent(){
        return mContent;
    }
}
