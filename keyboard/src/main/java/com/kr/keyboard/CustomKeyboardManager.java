package com.kr.keyboard;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.inputmethodservice.Keyboard;
import android.inputmethodservice.KeyboardView;
import android.text.Editable;
import android.util.Log;
import android.util.SparseIntArray;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Random;

/**
 * 自定义软件盘
 * 1.屏蔽截屏和录制屏幕
 * 2.支持随机数字
 */
public final class CustomKeyboardManager implements KeyboardView.OnKeyboardActionListener {
    private Dialog mPopUpDialog;
    private EditText mEditText;
    private Keyboard mKeyboard;
    private boolean mIsCapital = false;
    private KeyboardView mKeyBoardView;
    private View mViewHide;
    private static final SparseIntArray UN_PREVIEW_LIST = new SparseIntArray(); // codes in this list 将不会被预览提示

    static {
        UN_PREVIEW_LIST.put(Keyboard.KEYCODE_MODE_CHANGE, 1);
        UN_PREVIEW_LIST.put(Keyboard.KEYCODE_DELETE, 1);
        UN_PREVIEW_LIST.put(Keyboard.KEYCODE_SHIFT, 1);
        UN_PREVIEW_LIST.put(32, 1);
        //number 0-9 don't preview
        for (int i = 48; i <= 57; i++) {
            UN_PREVIEW_LIST.put(i, 1);
        }
    }

    /**
     * @param editText 接收软件盘输入内容的 edittext
     */
    public CustomKeyboardManager(EditText editText) {
        if (editText == null) {
            throw new NullPointerException("EditText can not be nulls");
        }
        mEditText = editText;
    }

    /**
     * 初始化自定义软键盘并监听EditText点击事件
     */
    @SuppressLint("ClickableViewAccessibility")
    public void subscribe() {
        final View view = LayoutInflater.from(mEditText.getContext()).inflate(R.layout.keyboard_view, null);

        if (mPopUpDialog == null) {
            mPopUpDialog = new Dialog(mEditText.getContext());
        }

        initWindow();

        mPopUpDialog.setContentView(view);

        mKeyBoardView = view.findViewById(R.id.keyboardview);
        mViewHide = view.findViewById(R.id.v_hide);

        mViewHide.setSelected(false);
        mViewHide.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mViewHide.setSelected(!mViewHide.isSelected());
            }
        });
        mKeyboard = new Keyboard(mEditText.getContext(), R.xml.keyboard_number);
        mKeyBoardView.setKeyboard(mKeyboard);
        mKeyBoardView.setOnKeyboardActionListener(this);


        mEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    if (!mPopUpDialog.isShowing()) {
                        randomNumberKeys();
                        mPopUpDialog.show();
                    }
                } else {
                    if (mPopUpDialog.isShowing()) {
                        mPopUpDialog.dismiss();
                    }
                }
            }
        });
        forbidDefaultSoftKeyboard();
    }

    private void initWindow() {
        Window mWindow = mPopUpDialog.getWindow();
        if (mWindow != null) {
            mWindow.setWindowAnimations(R.style.PopupAnimation);
            mWindow.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
            mWindow.setGravity(Gravity.BOTTOM | Gravity.FILL_HORIZONTAL);
            mWindow.setLayout(ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
            mWindow.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

            mWindow.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                    WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
            mWindow.addFlags(WindowManager.LayoutParams.FLAG_SECURE);
            mWindow.addFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL);
            mWindow.setDimAmount(0);
        }
    }


    /**
     * 随机数字键
     */
    private void randomNumberKeys() {
        //洗牌算法来随机排序所有数字键值
        Random random = new Random();
        int swapIndex;
        Keyboard.Key destKey, srcKey;
        for (int i = 0; i < 10; i++) {
            swapIndex = random.nextInt(10 - i) + i;
            destKey = mKeyboard.getKeys().get(swapIndex);
            srcKey = mKeyboard.getKeys().get(i);
            CharSequence label = destKey.label;
            int code = destKey.codes[0];

            destKey.codes[0] = srcKey.codes[0];
            destKey.label = srcKey.label;

            srcKey.label = label;
            srcKey.codes[0] = code;

        }
        mKeyBoardView.setKeyboard(mKeyboard);
    }


    /**
     * 禁止系统默认的软键盘弹出
     */
    private void forbidDefaultSoftKeyboard() {
        if (mEditText == null) {
            return;
        }
        try {
            Class<EditText> cls = EditText.class;
            Method setShowSoftInputOnFocus;
            setShowSoftInputOnFocus = cls.getMethod("setShowSoftInputOnFocus", boolean.class);
            setShowSoftInputOnFocus.setAccessible(true);
            setShowSoftInputOnFocus.invoke(mEditText, false);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Override
    public void onPress(int primaryCode) {
        setPreview(primaryCode);
    }

    @Override
    public void onRelease(int primaryCode) {

    }

    /**
     * 将大小字母进行互换
     */
    private void shiftEnglish() {
        List<Keyboard.Key> keyList = mKeyboard.getKeys();
        for (Keyboard.Key key : keyList) {
            if (key.label != null) {
                Log.d("key", key.label.toString());
            }

            if (key.label != null && isLetter(key.label.toString())) {
                if (mIsCapital) {
                    key.label = key.label.toString().toLowerCase();
                    key.codes[0] = key.codes[0] + 32;
                } else {
                    key.label = key.label.toString().toUpperCase();
                    key.codes[0] = key.codes[0] - 32;
                }
            }
        }
        mIsCapital = !mIsCapital;
    }


    private boolean isLetter(String key) {
        String lowercase = "abcdefghijklmnopqrstuvwxyz";
        return lowercase.contains(key.toLowerCase());
    }

    @Override
    public void onKey(int primaryCode, int[] keyCodes) {
        Editable editable = mEditText.getText();
        int start = mEditText.getSelectionStart();

        switch (primaryCode) {
            case Keyboard.KEYCODE_MODE_CHANGE:// 英文键盘与数字键盘切换-2
//                shiftKeyboard();
                break;
            case Keyboard.KEYCODE_DELETE:// 回退-5
                if (editable != null && editable.length() > 0 && start > 0) {
                    editable.delete(start - 1, start);
                }
                break;
            case Keyboard.KEYCODE_SHIFT:// 英文大小写切换-1
                shiftEnglish();
                mKeyBoardView.setKeyboard(mKeyboard);
                break;
            case Keyboard.KEYCODE_DONE:// 完成-4
                mPopUpDialog.dismiss();
                break;

            default:
                editable.insert(start, Character.toString((char) primaryCode));
                break;
        }
    }


    /**
     * 判断是否需要预览Key
     *
     * @param primaryCode keyCode
     */
    private void setPreview(int primaryCode) {
        if (mViewHide.isSelected()) {
            if (UN_PREVIEW_LIST.get(primaryCode) > 0) {
                mKeyBoardView.setPreviewEnabled(false);
            } else {
                mKeyBoardView.setPreviewEnabled(true);
            }
        } else {
            mKeyBoardView.setPreviewEnabled(false);
        }
    }

    @Override
    public void onText(CharSequence text) {

    }

    @Override
    public void swipeLeft() {

    }

    @Override
    public void swipeRight() {

    }

    @Override
    public void swipeDown() {

    }

    @Override
    public void swipeUp() {

    }
}
