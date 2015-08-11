package com.bj4.yhh.accountant.fragments.searchActs;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Filter;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * Created by yenhsunhuang on 15/7/29.
 */
public class ActAutoCompleteAdapter extends ArrayAdapter<String> {
    private static final boolean DEBUG = false;
    private static final String TAG = "ActAutoCompleteAdapter";
    private final ArrayList<String> mData = new ArrayList<String>();

    public ActAutoCompleteAdapter(Context context, int resource, List<String> objects) {
        super(context, resource, objects);
        mData.addAll(objects);
    }

    @Override
    public Filter getFilter() {
        return mContainsFilter;
    }

    private final Filter mContainsFilter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            if (DEBUG) Log.d(TAG, "constraint: " + constraint);
            FilterResults results = new FilterResults();
            if (constraint == null || TextUtils.isEmpty(constraint)) {
                return results;
            }
            HashMap<Integer, ArrayList<String>> fuzzyMap = new HashMap<Integer, ArrayList<String>>();
            HashMap<Integer, ArrayList<String>> indexOfMap = new HashMap<Integer, ArrayList<String>>();
            final String strConstraint = constraint.toString();
            for (String text : mData) {
                if (text == null) {
                    continue;
                }
                text = text.trim();
                final int indexOfText = text.toString().indexOf(strConstraint);
                if (indexOfText >= 0) {
                    ArrayList<String> indexOfList = indexOfMap.get(indexOfText);
                    if (indexOfList == null) {
                        indexOfList = new ArrayList<String>();
                        indexOfMap.put(indexOfText, indexOfList);
                    }
                    indexOfList.add(text);
                } else {
                    final int fuzzy = StringUtils.getFuzzyDistance((CharSequence) text, constraint, Locale.TAIWAN);
                    if (fuzzy <= 2) {
                        continue;
                    }
                    ArrayList<String> fuzzyList = fuzzyMap.get(fuzzy);
                    if (fuzzyList == null) {
                        fuzzyList = new ArrayList<String>();
                        fuzzyMap.put(fuzzy, fuzzyList);
                    }
                    fuzzyList.add(text);
                }
            }
            ArrayList<String> data = new ArrayList<String>();

            SortedSet<Integer> indexOfKeys = new TreeSet<Integer>(indexOfMap.keySet());
            for (Integer indexOfKey : indexOfKeys) {
                ArrayList<String> value = indexOfMap.get(indexOfKey);
                data.addAll(value);
            }

            SortedSet<Integer> fuzzyKeys = new TreeSet<Integer>(fuzzyMap.keySet());
            for (Integer fuzzyKey : fuzzyKeys) {
                ArrayList<String> value = fuzzyMap.get(fuzzyKey);
                data.addAll(value);
            }

            if (DEBUG) {
                Log.v(TAG, "indexOfKeys size: " + indexOfKeys.size() + ", fuzzyKeys size: " + fuzzyKeys.size());
                Log.d(TAG, "result size: " + data.size());
            }

            results.values = data;
            results.count = data.size();
            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            clear();
            if (results.values != null) {
                for (String result : (ArrayList<String>) results.values) {
                    add(result);
                }
                notifyDataSetChanged();
            }
        }
    };
}
