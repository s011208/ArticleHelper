package com.bj4.yhh.accountant.activity;

import android.animation.ValueAnimator;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.TextView;

import com.bj4.yhh.accountant.R;
import com.bj4.yhh.accountant.act.ActContent;
import com.bj4.yhh.accountant.act.Article;
import com.bj4.yhh.accountant.act.Chapter;
import com.bj4.yhh.accountant.act.Note;
import com.bj4.yhh.accountant.activity.editor.LinksView;
import com.bj4.yhh.accountant.activity.image.ImageWallpaperActivity;
import com.bj4.yhh.accountant.database.ActDatabase;
import com.bj4.yhh.accountant.fragments.display.actcontent.editor.EditorImageNoteGridAdapter;
import com.bj4.yhh.accountant.fragments.display.actcontent.editor.EditorNoteDialogFragment;
import com.bj4.yhh.accountant.fragments.display.actcontent.editor.ImageResourceChooser;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by yenhsunhuang on 15/7/23.
 */
public class ActEditorActivity extends BaseActivity implements EditorNoteDialogFragment.Callback, ImageResourceChooser.Callback {
    public static final boolean DEBUG = true;
    public static final String TAG = "ActEditorActivity";


    public static final String IMAGE_FILE_PATH = Environment.getExternalStoragePublicDirectory(
            Environment.DIRECTORY_PICTURES) + File.separator + "Account" + File.separator;

    private static final int CREATE_ANIMATION_DURATION = 350;
    private static final float CREATE_ANIMATION_START_VALUE = 0.2f;

    private static final int REQUEST_IMAGE_CAPTURE = 1001;
    private static final int REQUEST_IMAGE_GALLERY = 1002;
    private Uri mCurrentPhotoPath;
    private Note mPendingNote;
    // for edit area text note
    private Note mTextNote;

    private ActContent mActContent;
    private Rect mViewRect;
    private int mTouchedX;

    private FrameLayout mMainContainer;
    private CheckBox mHighLightCheckBox;
    private TextView mContentTextView;
    private TextView mTextNoteView;
    private TextView mEditNoteButton;
    private GridView mImageNoteArea;
    private EditorImageNoteGridAdapter mEditorImageNoteGridAdapter;

    private LinksView mLinksView;

    private boolean mHasAnyContentChanged = false;
    private boolean mPaused = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initActContent();
        initTextNoteData();
        setContentView(R.layout.activity_act_editor);
        initComponents();
//        startOnCreateAnimation();
    }

    private void initTextNoteData() {
        Cursor data = Note.getNoteData(this, ActDatabase.NOTE_TYPE_TEXT, (mActContent instanceof Chapter) ? ActDatabase.NOTE_PARENT_TYPE_CHAPTER : ActDatabase.NOTE_PARENT_TYPE_ARTICLE, mActContent.mId);
        if (data != null) {
            try {
                final int idIndex = data.getColumnIndex(ActDatabase.ID);
                final int noteParentTypeIndex = data.getColumnIndex(ActDatabase.NOTE_PARENT_TYPE);
                final int noteParentTypeIdIndex = data.getColumnIndex(ActDatabase.NOTE_PARENT_ID);
                final int noteTypeIndex = data.getColumnIndex(ActDatabase.NOTE_TYPE);
                final int noteContentIndex = data.getColumnIndex(ActDatabase.NOTE_CONTENT);
                while (data.moveToNext()) {
                    mTextNote = new Note(data.getLong(idIndex), data.getInt(noteParentTypeIndex), data.getLong(noteParentTypeIdIndex), data.getInt(noteTypeIndex), data.getString(noteContentIndex));
                }
            } finally {
                data.close();
            }
        }
    }

    /**
     * not smooth enough in activity
     */
    private void startOnCreateAnimation() {
        mMainContainer.setPivotY(mViewRect.centerY());
        final int centerX = mTouchedX == -1 ? mViewRect.centerX() : mTouchedX;
        mMainContainer.setPivotX(centerX);
        mMainContainer.setScaleY(CREATE_ANIMATION_START_VALUE);
        mMainContainer.setScaleX(CREATE_ANIMATION_START_VALUE);
        ValueAnimator va = ValueAnimator.ofFloat(CREATE_ANIMATION_START_VALUE, 1);
        va.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                final float value = (Float) animation.getAnimatedValue();
                mMainContainer.setScaleY(value);
                mMainContainer.setScaleX(value);
            }
        });
        va.setInterpolator(new DecelerateInterpolator());
        va.setDuration(CREATE_ANIMATION_DURATION);
        va.start();
    }

    @Override
    public void onBackPressed() {
        if (mHasAnyContentChanged) {
            setResult(RESULT_OK);
            finish();
            return;
        }
        super.onBackPressed();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mPaused) {
            if (mEditorImageNoteGridAdapter != null) {
                mEditorImageNoteGridAdapter.updateContent();
                mEditorImageNoteGridAdapter.notifyDataSetChanged();
            }
        }
        mPaused = false;
    }

    @Override
    protected void onPause() {
        super.onPause();
        mPaused = true;
    }

    private void initComponents() {
        // main
        mMainContainer = (FrameLayout) findViewById(R.id.main_container);

        // checkbox
        mHighLightCheckBox = (CheckBox) findViewById(R.id.highlight_checkbox);
        mHighLightCheckBox.setChecked(mActContent.mHasHighLight);
        mHighLightCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                boolean result = mActContent.updateHighLight(ActEditorActivity.this, isChecked);
                mHasAnyContentChanged = true;
                if (DEBUG) Log.d(TAG, "result: " + result);
            }
        });

        // content textview
        mContentTextView = (TextView) findViewById(R.id.edit_content_text);
        mContentTextView.setText(mActContent.mContent);

        // text note area
        mEditNoteButton = (TextView) findViewById(R.id.edit_button);
        mEditNoteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditorNoteDialogFragment dialog = new EditorNoteDialogFragment();
                Bundle extras = new Bundle();
                extras.putString("txt", mTextNote == null ? "" : mTextNote.mNoteContent);
                dialog.setArguments(extras);
                dialog.show(getFragmentManager(), EditorNoteDialogFragment.TAG);
            }
        });
        mTextNoteView = (TextView) findViewById(R.id.text_note_view);
        if (mTextNote == null) {
            mTextNoteView.setText(R.string.activity_act_editor_note_default_text);
        } else {
            mTextNoteView.setText(mTextNote.mNoteContent);
        }

        // img note area
        mImageNoteArea = (GridView) findViewById(R.id.image_note_area);
        mEditorImageNoteGridAdapter = new EditorImageNoteGridAdapter(mActContent, this);
        mImageNoteArea.setAdapter(mEditorImageNoteGridAdapter);
        mImageNoteArea.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (DEBUG)
                    Log.d(TAG, "position: " + position);
                Note note = (Note) parent.getAdapter().getItem(position);
                if (note.isMore()) {
                    Intent startIntent = new Intent(ActEditorActivity.this, ImageWallpaperActivity.class);
                    startIntent.putExtra(BaseActivity.EXTRA_ACT_CONTENT, mActContent.toString());
                    startIntent.putExtra(BaseActivity.EXTRA_ACT_CONTENT_TYPE, mActContent.getClass().getName());
                    startIntent.putExtra(BaseActivity.EXTRA_TOUCH_X, mTouchedX);
                    Rect viewRect = new Rect();
                    view.getGlobalVisibleRect(viewRect);
                    startIntent.setSourceBounds(viewRect);
                    startActivity(startIntent);
                } else if (note.isEmptyContent()) {
                    mPendingNote = note;
                    ImageResourceChooser chooser = new ImageResourceChooser();
                    chooser.show(getFragmentManager(), ImageResourceChooser.class.getName());
                } else {
                    Intent startIntent = new Intent(ActEditorActivity.this, ImageWallpaperActivity.class);
                    startIntent.putExtra(BaseActivity.EXTRA_ACT_CONTENT, mActContent.toString());
                    startIntent.putExtra(BaseActivity.EXTRA_ACT_CONTENT_TYPE, mActContent.getClass().getName());
                    startIntent.putExtra(BaseActivity.EXTRA_TOUCH_X, mTouchedX);
                    startIntent.putExtra(ImageWallpaperActivity.EXTRA_DISPLAY_NOTE_URI, note.mNoteContent);
                    Rect viewRect = new Rect();
                    view.getGlobalVisibleRect(viewRect);
                    startIntent.setSourceBounds(viewRect);
                    startActivity(startIntent);
                }
            }
        });

        mLinksView = (LinksView) findViewById(R.id.links_view_area);
        mLinksView.setLinks(mActContent.mLinks);
    }

    private void dispatchQueryGalleryImageIntent() {
        Intent i = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(i, REQUEST_IMAGE_GALLERY);
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            File photoFile;
            try {
                photoFile = createImageFile();
            } catch (IOException e) {
                // TODO failed to create image file
                Log.w(TAG, "Failed to create file", e);
                return;
            }
            if (photoFile != null) {
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT,
                        mCurrentPhotoPath);
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            }
        }
    }

    private void createDirectoryIfNeed() {
        File directory = new File(IMAGE_FILE_PATH);
        if (directory.exists() == false) {
            directory.mkdir();
        }
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + ".png";
        if (DEBUG) Log.v(TAG, "file path: " + IMAGE_FILE_PATH + imageFileName);
        createDirectoryIfNeed();
        File image = new File(IMAGE_FILE_PATH + imageFileName);
        image.createNewFile();

        mCurrentPhotoPath = Uri.fromFile(image);
        return image;
    }

    private void scanAddedFile() {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        mediaScanIntent.setData(mCurrentPhotoPath);
        sendBroadcast(mediaScanIntent);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (DEBUG)
            Log.d(TAG, "onActivityResult requestCode: " + requestCode + ", resultCode: " + resultCode);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            scanAddedFile();
            mHasAnyContentChanged = true;
            mPendingNote.setImageContent(mCurrentPhotoPath.toString());
            boolean updateSuccess = Note.insertOrUpdate(this, mPendingNote);
            if (updateSuccess && mEditorImageNoteGridAdapter != null) {
                mEditorImageNoteGridAdapter.updateContent();
                mEditorImageNoteGridAdapter.notifyDataSetChanged();
            }
            if (DEBUG) {
                Log.d(TAG, "REQUEST_IMAGE_CAPTURE mCurrentPhotoPath: " + mCurrentPhotoPath.toString() + ", updateSuccess: " + updateSuccess);
            }
        } else if (requestCode == REQUEST_IMAGE_GALLERY && resultCode == RESULT_OK) {
            mCurrentPhotoPath = data.getData();
            mHasAnyContentChanged = true;
            mPendingNote.setImageContent(mCurrentPhotoPath.toString());
            boolean updateSuccess = Note.insertOrUpdate(this, mPendingNote);
            if (updateSuccess && mEditorImageNoteGridAdapter != null) {
                mEditorImageNoteGridAdapter.updateContent();
                mEditorImageNoteGridAdapter.notifyDataSetChanged();
            }
            if (DEBUG) {
                Log.d(TAG, "REQUEST_IMAGE_GALLERY mCurrentPhotoPath: " + mCurrentPhotoPath.toString() + ", updateSuccess: " + updateSuccess);
            }
        }
        mCurrentPhotoPath = null;
        mPendingNote = null;
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
    }

    @Override
    public void onPositiveClick(String newNote) {
        mTextNoteView.setText(newNote);
        if (mTextNote == null) {
            mTextNote = new Note((mActContent instanceof Chapter) ? ActDatabase.NOTE_PARENT_TYPE_CHAPTER : ActDatabase.NOTE_PARENT_TYPE_ARTICLE, mActContent.mId, ActDatabase.NOTE_TYPE_TEXT, newNote);
        }
        boolean updateSuccess = Note.insertOrUpdate(this, mTextNote);
        if (DEBUG) Log.v(TAG, "newNote: " + newNote + ", updateSuccess: " + updateSuccess);
        mHasAnyContentChanged = true;
    }

    @Override
    public void onSelectCamera() {
        dispatchTakePictureIntent();
    }

    @Override
    public void onSelectGallery() {
        dispatchQueryGalleryImageIntent();
    }
}
