package com.bj4.yhh.lawhelper.dialogs;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.FragmentManager;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;

import com.bj4.yhh.lawhelper.R;
import com.bj4.yhh.lawhelper.act.Act;
import com.bj4.yhh.lawhelper.act.Article;
import com.bj4.yhh.lawhelper.act.Chapter;
import com.bj4.yhh.lawhelper.utils.dialogs.BaseDialogFragment;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by yenhsunhuang on 15/8/22.
 */
public class OutlineDialogFragment extends BaseDialogFragment {
    public static final String OUTLINE_ACT = "outline_act";
    private Act mAct;

    private final ArrayList<String> mIndentList = new ArrayList<String>();

    private final ArrayList<Chapter> mChapters = new ArrayList<Chapter>();

    public interface Callback {
        void onItemClick(long chapterId);
    }

    private Callback mCallback;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (activity instanceof Callback) {
            mCallback = (Callback) activity;
        }
    }

    public void setCallback(Callback cb) {
        mCallback = cb;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String strAct = getArguments().getString(OUTLINE_ACT);
        try {
            mAct = new Act(new JSONObject(strAct));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public View getCustomMessage() {
        return null;
    }

    @Override
    public int getTitleTextResources() {
        return 0;
    }

    @Override
    public String getTitleText() {
        return getActivity().getResources().getString(R.string.outline_dialog_fragment_title, mAct.getTitle());
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = getDialogBuilder();
        final CharSequence[] items = generateItemList();
        if (items == null || items.length == 0) {
            builder.setMessage(R.string.outline_dialog_fragment_no_outline);
        } else {
            builder.setItems(items, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if (mCallback != null) {
                        mCallback.onItemClick(mChapters.get(which).mId);
                    }
                }
            });
        }
        return builder.create();
    }

    private CharSequence[] generateItemList() {
        CharSequence[] rtn = null;
        mChapters.clear();
        Act.queryAllActContent(getActivity(), mAct);
        final ArrayList<CharSequence> items = new ArrayList<CharSequence>();
        int startIndexOfArticle = 1;
        for (Chapter chapter : mAct.getChapters()) {
            if (chapter.isEmptyChapter()) continue;
            String lastWord = chapter.mNumber.substring(chapter.mNumber.length() - 1);
            int indent = mIndentList.indexOf(lastWord);
            if (indent == -1) {
                mIndentList.add(lastWord);
                indent = mIndentList.indexOf(lastWord);
            }
            String prefix = "";
            for (int i = 0; i < indent; i++) {
                prefix += "    ";
            }
            items.add(prefix + chapter.mNumber + "  " + chapter.mContent + " ＃" + startIndexOfArticle);
            mChapters.add(chapter);
            int size = 0;
            for (Article article : chapter.getArticles()) {
                /**
                 * ignore sub items
                 */
                if (article.mNumber.contains("-")) continue;
                ++size;
            }
            startIndexOfArticle += size;
        }
        rtn = new CharSequence[items.size()];
        for (int i = 0; i < items.size(); i++) {
            rtn[i] = items.get(i);
        }
        return rtn;
    }

    public static void showDialog(Act act, FragmentManager fm) {
        showDialog(act, fm, null);
    }

    public static void showDialog(Act act, FragmentManager fm, Callback cb) {
        if (act == null) return;
        OutlineDialogFragment dialog = new OutlineDialogFragment();
        Bundle args = new Bundle();
        args.putString(OutlineDialogFragment.OUTLINE_ACT, act.toString());
        dialog.setArguments(args);
        dialog.setCallback(cb);
        dialog.show(fm, OutlineDialogFragment.class.getName());
    }
}
