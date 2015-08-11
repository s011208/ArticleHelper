package com.bj4.yhh.accountant.activity.image;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.bj4.yhh.accountant.R;
import com.bj4.yhh.accountant.act.ActContent;
import com.bj4.yhh.accountant.act.Article;
import com.bj4.yhh.accountant.act.Chapter;
import com.bj4.yhh.accountant.act.Note;
import com.bj4.yhh.accountant.activity.BaseActivity;
import com.bj4.yhh.accountant.utils.ImageNoteHelper;

/**
 * Created by yenhsunhuang on 15/7/25.
 */
public class ImageWallpaperActivity extends BaseActivity implements ImagePreviewPagerAdapter.Callback {
    private static final boolean DEBUG = true;
    private static final String TAG = "ImageWallpaperActivity";

    public static final String EXTRA_DISPLAY_NOTE_URI = "display_note_uri";

    private static final int ANIMATIONS_DURATION = 200;

    private ActContent mActContent;
    private Rect mViewRect;
    private int mTouchedX;

    private ImageView mImageView;
    private ViewPager mPreviewPager;
    private ImagePreviewPagerAdapter mImagePreviewPagerAdapter;
    private RelativeLayout mActionBar;
    private ImageView mBack, mShare, mDelete;

    private ImageNoteHelper mLargeImageNoteHelper, mPreviewImageNoteHelper;

    private ValueAnimator mPreviewVisibilityAnimation, mActionBarVisibilityAnimation;
    private boolean mIsShowPreviewItems = true;

    private Note mCurrentNote;
    private String mDefaultDisplayNoteUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initActContent();
        setContentView(R.layout.activity_image_wallpaper);
        initComponents();
    }

    private void startPreviewPagerAnimation() {
        if (mPreviewPager == null) {
            return;
        }
        if (mPreviewVisibilityAnimation != null && mPreviewVisibilityAnimation.isRunning()) {
            mPreviewVisibilityAnimation.cancel();
        }
        final float startY = mPreviewPager.getTranslationY();
        final float endY = mIsShowPreviewItems ? 0 : mPreviewPager.getHeight();
        mPreviewVisibilityAnimation = ValueAnimator.ofFloat(startY, endY);
        mPreviewVisibilityAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                final float value = (float) animation.getAnimatedValue();
                mPreviewPager.setTranslationY(value);
            }
        });
        mPreviewVisibilityAnimation.setDuration(ANIMATIONS_DURATION);
        mPreviewVisibilityAnimation.start();
    }

    private void startActionBarAnimation() {
        if (mActionBar == null) {
            return;
        }
        if (mActionBarVisibilityAnimation != null && mActionBarVisibilityAnimation.isRunning()) {
            mActionBarVisibilityAnimation.cancel();
        }
        final float startY = mActionBar.getTranslationY();
        final float endY = mIsShowPreviewItems ? 0 : -mActionBar.getHeight();
        mActionBarVisibilityAnimation = ValueAnimator.ofFloat(startY, endY);
        mActionBarVisibilityAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                final float value = (float) animation.getAnimatedValue();
                mActionBar.setTranslationY(value);
            }
        });
        mActionBarVisibilityAnimation.setDuration(ANIMATIONS_DURATION);
        mActionBarVisibilityAnimation.setStartDelay(100);
        mActionBarVisibilityAnimation.start();
    }

    private void initComponents() {
        // image preview
        mImageView = (ImageView) findViewById(R.id.image_view);
        mImageView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                ViewTreeObserver vto = mImageView.getViewTreeObserver();
                if (vto.isAlive()) {
                    vto.removeOnGlobalLayoutListener(this);
                }
                mLargeImageNoteHelper.setMaximumImageSize(mImageView.getWidth(), mImageView.getHeight());
            }
        });
        mImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mIsShowPreviewItems = !mIsShowPreviewItems;
                startPreviewPagerAnimation();
                startActionBarAnimation();
            }
        });
        mPreviewPager = (ViewPager) findViewById(R.id.preview_pager);
        if (isNavBarTintEnabled()) {
            mPreviewPager.setBackgroundColor(0x20ff9900);
        }
        mPreviewPager.setOffscreenPageLimit(Integer.MAX_VALUE);
        mImagePreviewPagerAdapter = new ImagePreviewPagerAdapter(mActContent, this, this);
        mPreviewPager.setAdapter(mImagePreviewPagerAdapter);
        mPreviewImageNoteHelper = mImagePreviewPagerAdapter.getImageNoteHelper();
        mPreviewPager.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                ViewTreeObserver vto = mPreviewPager.getViewTreeObserver();
                if (vto.isAlive()) {
                    vto.removeOnGlobalLayoutListener(this);
                }
                if (mPreviewPager.getChildCount() <= 0)
                    return;
                final int childHeight = mPreviewPager.getChildAt(0).getHeight();
                if (DEBUG) {
                    Log.d(TAG, "child size: " + childHeight + ", pager size: " + mPreviewPager.getHeight());
                }
                ViewGroup.LayoutParams vl = mPreviewPager.getLayoutParams();
                vl.height = childHeight;
                mPreviewPager.setLayoutParams(vl);
                if (mDefaultDisplayNoteUri != null) {
                    showImageView(mDefaultDisplayNoteUri);
                } else {
                    if (!mPreviewImageNoteHelper.getAllImageNotes().isEmpty()) {
                        final Note note = mPreviewImageNoteHelper.getAllImageNotes().get(0);
                        showImageView(note);
                        mCurrentNote = note;
                    }
                }
            }
        });
        mLargeImageNoteHelper = new ImageNoteHelper(mActContent, this);
        mLargeImageNoteHelper.init();

        //action bar
        mActionBar = (RelativeLayout) findViewById(R.id.custom_action_bar);
        if (isStatusBarTintEnabled()) {
            mActionBar.setBackgroundColor(0x20ff9900);
        }
        mBack = (ImageView) findViewById(R.id.action_bar_back);
        mBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        mShare = (ImageView) findViewById(R.id.action_bar_share);
        mShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendShareIntent(ImageWallpaperActivity.this, mCurrentNote);
            }
        });
        mDelete = (ImageView) findViewById(R.id.action_bar_delete);
        mDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteNote();
            }
        });
    }

    private void deleteNote() {
        mImagePreviewPagerAdapter.clearAllFocusDrawable();
        mImagePreviewPagerAdapter.deletePage(mCurrentNote);
        if (!mPreviewImageNoteHelper.getAllImageNotes().isEmpty()) {
            final Note note = mPreviewImageNoteHelper.getAllImageNotes().get(0);
            onItemClick(note);
        } else {
            mImageView.setImageDrawable(null);
            mCurrentNote = null;
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    private static void sendShareIntent(Context context, Note note) {
        Intent intent = new Intent(Intent.ACTION_SEND);
        Uri uri = Uri.parse(note.mNoteContent);
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_STREAM, String.valueOf(uri));
        context.startActivity(intent);
    }

    private void initActContent() {
        String extraActContent = getIntent().getStringExtra(BaseActivity.EXTRA_ACT_CONTENT);
        String extraActContentType = getIntent().getStringExtra(BaseActivity.EXTRA_ACT_CONTENT_TYPE);
        if (Chapter.class.getName().equals(extraActContentType)) {
            mActContent = new Chapter(extraActContent);
        } else {
            mActContent = new Article(extraActContent);
        }
        mViewRect = getIntent().getSourceBounds();
        if (mViewRect == null) {
            mViewRect = new Rect();
        }
        if (DEBUG) {
            Log.d(TAG, "view rect: " + mViewRect);
        }
        mTouchedX = getIntent().getIntExtra(BaseActivity.EXTRA_TOUCH_X, -1);
        mDefaultDisplayNoteUri = getIntent().getStringExtra(EXTRA_DISPLAY_NOTE_URI);
    }

    private void showImageView(String noteUri) {
        mCurrentNote = mImagePreviewPagerAdapter.setFocusDrawable(noteUri);
        mLargeImageNoteHelper.getImageLoader().displayImage(noteUri, mImageView, mLargeImageNoteHelper.getDisplayImageOptions());

    }

    private void showImageView(Note note) {
        mImagePreviewPagerAdapter.setFocusDrawable(note);
        mLargeImageNoteHelper.getImageLoader().displayImage(note.mNoteContent, mImageView, mLargeImageNoteHelper.getDisplayImageOptions());
    }

    @Override
    public void onItemClick(Note note) {
        showImageView(note);
        mCurrentNote = note;
    }

    @Override
    public void onItemRemoved() {
        final int currentPagerItem = mPreviewPager.getCurrentItem();
        if (currentPagerItem >= mImagePreviewPagerAdapter.getCount()) {
            mPreviewPager.setCurrentItem(mImagePreviewPagerAdapter.getCount() - 1, true);
        }
        if (DEBUG)
            Log.d(TAG, "currentPagerItem: " + currentPagerItem + ",mImagePreviewPagerAdapter.getCount(): " + mImagePreviewPagerAdapter.getCount());
    }
}
