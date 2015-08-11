package com.bj4.yhh.accountant.fragments.display.actcontent;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bj4.yhh.accountant.R;
import com.bj4.yhh.accountant.act.ActContent;
import com.bj4.yhh.accountant.activity.ActEditorActivity;
import com.bj4.yhh.accountant.activity.BaseActivity;
import com.bj4.yhh.accountant.activity.MainActivity;
import com.bj4.yhh.accountant.fragments.display.ActFragment;
import com.bj4.yhh.accountant.utils.FloatingActionButton;
import com.bj4.yhh.accountant.utils.TranslationHeaderLayout;

/**
 * Created by yenhsunhuang on 15/7/18.
 */
public class DisplayActContentFragment extends ActFragment implements TranslationHeaderLayout.Callback, ActContentAdapter.Callback {
    private static final boolean DEBUG = false;
    private static final String TAG = "DisplayActContent";
    private static final int QUERY_INTERVAL = 300;
    private String mQueryString = "";
    private int mTouchedX;

    private TextView mActTitle, mActAmendDate, mActCategory;
    private LinearLayout mActArea;
    private TranslationHeaderLayout mTranslationHeader;
    private RelativeLayout mRoot;
    private ListView mActContent;
    private ActContentAdapter mActContentAdapter;

    private FloatingActionButton mFloatingActionButton;

    private boolean mPaused = false;

    private float mCalculatedDeltaY;

    private Handler mHandler = new Handler();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (DEBUG) {
            Log.v(TAG, "onResume");
        }
        if (mPaused) {
            // refresh when resume
            if (mActContentAdapter != null) {
                mActContentAdapter.updateContent();
                mActContentAdapter.notifyDataSetChanged();
            }
        }
        mPaused = false;
    }

    @Override
    public void onPause() {
        super.onPause();
        mPaused = true;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mRoot = (RelativeLayout) inflater.inflate(R.layout.display_act_content_fragment, null);
        mTranslationHeader = (TranslationHeaderLayout) mRoot.findViewById(R.id.translation_header_layout);
        mTranslationHeader.setCallback(this);
        mActArea = (LinearLayout) inflater.inflate(R.layout.display_act_content_fragment_header, null);
        mActTitle = (TextView) mActArea.findViewById(R.id.act_title);
        mActAmendDate = (TextView) mActArea.findViewById(R.id.act_amenddate);
        mActCategory = (TextView) mActArea.findViewById(R.id.act_category);
        mActTitle.setText(mAct.getTitle());
        mActAmendDate.setText(mAct.getAmendedDate());
        mActCategory.setText(mAct.getCategory());
        mActContent = (ListView) inflater.inflate(R.layout.display_act_content_fragment_footer, null);
        mActContentAdapter = new ActContentAdapter(getActivity(), mAct);
        mActContentAdapter.setCallback(this);
        mActContent.setAdapter(mActContentAdapter);
        mTranslationHeader.setHeader(mActArea);
        mTranslationHeader.setFooter(mActContent);
        mActContent.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                mTouchedX = (int) event.getRawX();
                return false;
            }
        });
        mActContent.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ActContent act = (ActContent) mActContent.getAdapter().getItem(position);
                if (DEBUG) {
                    Log.d(TAG, "position: " + position + ", act: " + act.toString());
                }
                Intent startIntent = new Intent(getActivity(), ActEditorActivity.class);
                startIntent.putExtra(BaseActivity.EXTRA_ACT_CONTENT, act.toString());
                startIntent.putExtra(BaseActivity.EXTRA_ACT_CONTENT_TYPE, act.getClass().getName());
                startIntent.putExtra(BaseActivity.EXTRA_TOUCH_X, mTouchedX);
                Rect viewRect = new Rect();
                view.getGlobalVisibleRect(viewRect);
                startIntent.setSourceBounds(viewRect);
                getActivity().startActivityForResult(startIntent, MainActivity.REQUEST_EDIT_ACT_CONTENT);
            }
        });

        // floating button
        Resources res = getActivity().getResources();
        mFloatingActionButton = (FloatingActionButton) mRoot.findViewById(R.id.floating_action_button);
        mFloatingActionButton.setIconDrawable(res.getDrawable(R.drawable.white_magnify))
                .setTinitColor(res.getColor(R.color.main_title_color))
                .setPressTintColor(res.getColor(R.color.main_title_color_dark)).build();
        mFloatingActionButton.bringToFront();
        mFloatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showMenuPanel();
            }
        });
        initMenuPanel();
        return mRoot;
    }


    private RelativeLayout mMenuPanel;
    private View mMenuBackground;
    private ImageView mSelectImportant;
    private ImageView mSelectNote;
    private ImageView mSelectImage;
    private EditText mSearchView;
    private boolean mIsMenuShowing = false;

    private void showMenuPanel() {
        mIsMenuShowing = true;
        final int centerOfX = getActivity().getResources().getDisplayMetrics().widthPixels / 2;
        final int gridSize = getActivity().getResources().getDisplayMetrics().widthPixels / 3;
        final int translationY = mSelectImportant.getHeight();
        final int menuItemSize = translationY;
        final int animationDuration = 300;
        ValueAnimator animator = ValueAnimator.ofFloat(1, 0);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                final float value = (Float) animation.getAnimatedValue();
                mMenuBackground.setAlpha(1 - value);
            }
        });
        animator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                hideFloatingActionButton();
                mMenuBackground.setAlpha(0);
                mMenuPanel.setVisibility(View.VISIBLE);

                mSelectImportant.setTranslationX(centerOfX - menuItemSize / 2);
                mSelectNote.setTranslationX(centerOfX - menuItemSize / 2);
                mSelectImage.setTranslationX(centerOfX - menuItemSize / 2);

                mSelectImportant.setTranslationY(translationY);
                mSelectNote.setTranslationY(translationY);
                mSelectImage.setTranslationY(translationY);

                mSelectImportant.animate().translationX(centerOfX - menuItemSize / 2 - gridSize).translationY(0).setStartDelay(0).setDuration(animationDuration).start();
                mSelectNote.animate().translationX(centerOfX - menuItemSize / 2).translationY(0).setStartDelay(100).setDuration(animationDuration).start();
                mSelectImage.animate().translationX(centerOfX - menuItemSize / 2 + gridSize).translationY(0).setStartDelay(50).setDuration(animationDuration).start();

                mSelectImportant.setImageResource(mActContentAdapter.isQueryHighLight() ? R.drawable.holo_light_thumb_up : R.drawable.white_thumb_up);
                mSelectNote.setImageResource(mActContentAdapter.isQueryTextNote() ? R.drawable.holo_light_pen : R.drawable.white_border_color);
                mSelectImage.setImageResource(mActContentAdapter.isQueryImageNote() ? R.drawable.holo_light_image_filter : R.drawable.white_image_filter);

            }

            @Override
            public void onAnimationEnd(Animator animation) {

            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        animator.setDuration(animationDuration);
        animator.start();
    }

    private void hideMenuPanel() {
        final int centerOfX = getActivity().getResources().getDisplayMetrics().widthPixels / 2;
        final int translationY = mSelectImportant.getHeight();
        final int menuItemSize = translationY;
        final int animationDuration = 300;
        ValueAnimator animator = ValueAnimator.ofFloat(1, 0);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                final float value = (Float) animation.getAnimatedValue();
                mMenuBackground.setAlpha(value);
            }
        });
        animator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                mSelectImportant.animate().translationX(centerOfX - menuItemSize / 2).translationY(translationY).setDuration(animationDuration).start();
                mSelectNote.animate().translationX(centerOfX - menuItemSize / 2).translationY(translationY).setDuration(animationDuration).start();
                mSelectImage.animate().translationX(centerOfX - menuItemSize / 2).translationY(translationY).setDuration(animationDuration).start();
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                showFloatingActionButton();
                mMenuPanel.setVisibility(View.INVISIBLE);
                mIsMenuShowing = false;
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        animator.setDuration(animationDuration);
        animator.start();
    }

    private void initMenuPanel() {
        mMenuPanel = (RelativeLayout) mRoot.findViewById(R.id.menu_panel);
        mMenuPanel.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    hideMenuPanel();
                }
                return true;
            }
        });
        mMenuBackground = mRoot.findViewById(R.id.menu_bg);
        mSelectImportant = (ImageView) mRoot.findViewById(R.id.select_important);
        mSelectImportant.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mActContentAdapter.queryHighLight(!mActContentAdapter.isQueryHighLight());
                hideMenuPanel();
            }
        });
        mSelectNote = (ImageView) mRoot.findViewById(R.id.select_note);
        mSelectNote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mActContentAdapter.queryTextNote(!mActContentAdapter.isQueryTextNote());
                hideMenuPanel();
            }
        });
        mSelectImage = (ImageView) mRoot.findViewById(R.id.select_image);
        mSelectImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mActContentAdapter.queryImageNote(!mActContentAdapter.isQueryImageNote());
                hideMenuPanel();
            }
        });
        mSearchView = (EditText) mRoot.findViewById(R.id.search_view);
        mSearchView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                mQueryString = s.toString();
                mHandler.removeCallbacks(mQueryTask);
                mHandler.postDelayed(mQueryTask, QUERY_INTERVAL);
            }
        });
    }

    private final Runnable mQueryTask = new Runnable() {
        @Override
        public void run() {
            if (mActContentAdapter != null) {
                mActContentAdapter.queryByLike(mQueryString);
            }
        }
    };

    private void hideFloatingActionButton() {
        mFloatingActionButton.animate().alpha(0).setDuration(150).setListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                mFloatingActionButton.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        }).start();
    }

    private void showFloatingActionButton() {
        mFloatingActionButton.animate().alpha(1).setDuration(150).setListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                mFloatingActionButton.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animator animation) {

            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        }).start();
    }

    @Override
    public void onScroll(int deltaY) {
        mCalculatedDeltaY -= deltaY;
    }

    @Override
    public void onScrollDone() {
        if (DEBUG)
            Log.d(TAG, "onScrollDone, mCalculatedDeltaY: " + mCalculatedDeltaY);
        if (mCalculatedDeltaY > 0) {
            showFloatingActionButton();
            mCalculatedDeltaY = 1;
        } else {
            hideFloatingActionButton();
            mCalculatedDeltaY = 0;
        }
    }

    @Override
    public boolean onBackPress() {
        if (mIsMenuShowing) {
            hideMenuPanel();
            return true;
        }
        if (mActContentAdapter.isQuery()) {
            mActContentAdapter.resetQueryStatus();
            return true;
        }
        return false;
    }

    @Override
    public void onQueryDone() {

    }
}
