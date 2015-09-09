package com.bj4.yhh.lawhelper.fragments.entry;

import android.graphics.drawable.Drawable;

/**
 * Created by yenhsunhuang on 15/8/16.
 */
public class GridMenuItem {
    private String mMenuTitle;
    private Drawable mMenuIcon;

    public GridMenuItem(String menuTitle) {
        this(menuTitle, null);
    }


    public GridMenuItem(String menuTitle, Drawable menuIcon) {
        setMenuTitle(menuTitle);
        setMenuIcon(menuIcon);
    }

    public String getMenuTitle() {
        return mMenuTitle;
    }

    public void setMenuTitle(String mMenuTitle) {
        this.mMenuTitle = mMenuTitle;
    }

    public Drawable getMenuIcon() {
        return mMenuIcon;
    }

    public void setMenuIcon(Drawable mMenuIcon) {
        this.mMenuIcon = mMenuIcon;
    }
}
