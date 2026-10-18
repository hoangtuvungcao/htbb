package game.render.screen;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.text.InputFilter;
import android.text.method.PasswordTransformationMethod;
import android.view.KeyEvent;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.htbb.client.GameMidlet;
import game.render.GCanvas;

public class myEditText extends EditText {
    public static int height = 35;
    public static boolean isVisible = true;
    public static int width = 100;
    public static int x;
    public static int y;
    public int ID;
    public myEditText instance;
    public RelativeLayout.LayoutParams myParames;
    String strchange;

    public myEditText(Context context) {
        super(context);
        this.strchange = "";
        this.instance = this;
        setFocusable(true);
        setFocusableInTouchMode(true);
        init();
    }

    public myEditText(Context context, int i, int i2) {
        super(context);
        this.strchange = "";
        this.instance = this;
        setFocusable(true);
        setFocusableInTouchMode(true);
        init(i, i2);
    }

    public void init() {
        init(width, height);
    }

    public void action_Next_Done(TextView textView, int i, KeyEvent keyEvent) {
        if (i == 6 || i == 5) {
            hideSoftKeyboard();
            for (int i2 = 0; i2 < GameMidlet.allField.size(); i2++) {
                if (GameMidlet.allField.elementAt(i2).ID == this.ID) {
                    GameMidlet.allField.elementAt(i2).setText(getString());
                    GameMidlet.allField.elementAt(i2).isFocus = false;
                    ((GameMidlet) GCanvas.gCanvas).update(2, this);
                }
            }
            setVisible(false);
            if (GCanvas.gameScr.chatMode) {
                GCanvas.gameScr.chatFast();
            }
        }
    }

    public void init(int i, int i2) {
        if (!GameMidlet.isChaCha) {
            setImeOptions(268435456);
            setOnEditorActionListener(new TextView.OnEditorActionListener() {
                @Override
                public boolean onEditorAction(TextView textView, int i3, KeyEvent keyEvent) {
                    myEditText.this.action_Next_Done(textView, i3, keyEvent);
                    return true;
                }
            });
        }
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(i, i2);
        this.myParames = layoutParams;
        layoutParams.leftMargin = 0;
        this.myParames.topMargin = 0;
        setLayoutParams(this.myParames);
        setWidth(i);
        setHeight(i2);
        setBackgroundColor(0);
        setTextColor(-16777216);
        setSingleLine();
        if (GCanvas.h < 400) {
            setTextSize(15.0f);
        } else {
            setTextSize(12.0f);
        }
        setTextSize(20.0f);
    }

    @Override
    public void onDraw(Canvas canvas) {
        if (this.strchange.compareTo(getString()) != 0) {
            this.strchange = getString();
            for (int i = 0; i < GameMidlet.allField.size(); i++) {
                if (GameMidlet.allField.elementAt(i).ID == this.ID) {
                    GameMidlet.allField.elementAt(i).setText(getString());
                }
            }
        }
    }

    public String getString() {
        return getText().toString();
    }

    public void setText(String str) {
        setText(str, TextView.BufferType.EDITABLE);
        this.strchange = str;
    }

    public void clear() {
        setText("", TextView.BufferType.EDITABLE);
    }

    public void setMaxTextInput(int i) {
        setFilters(new InputFilter[]{new InputFilter.LengthFilter(i)});
    }

    public void setWidthHeight(int i, int i2) {
        this.myParames.width = i;
        this.myParames.height = i2;
        setWidth(i);
        setHeight(i2);
    }

    public void changePosition(int i, int i2) {
        int visibility = getVisibility();
        setVisibility(4);
        x += i;
        y += i2;
        this.myParames.leftMargin += i;
        this.myParames.topMargin += i2;
        setLayoutParams(this.myParames);
        setVisibility(visibility);
    }

    public void setPosition(int i, int i2) {
        x = i;
        y = i2;
        this.myParames.leftMargin = i;
        this.myParames.topMargin = y;
        setLayoutParams(this.myParames);
    }

    public void setPositionLogin() {
        this.myParames.topMargin = ServerListScr.cmy;
        setLayoutParams(this.myParames);
    }

    public void setmyInputType(int i) {
        if (i == 0) {
            setInputType(1);
            return;
        }
        if (i == 1) {
            setInputType(2);
            return;
        }
        if (i == 2) {
            setInputType(1);
            setTransformationMethod(PasswordTransformationMethod.getInstance());
        } else {
            if (i != 3) {
                return;
            }
            setInputType(1);
        }
    }

    public void setVisible(boolean z) {
        isVisible = z;
        if (z) {
            setVisibility(0);
        } else {
            setVisibility(4);
        }
    }

    @Override
    public boolean onKeyDown(int i, KeyEvent keyEvent) {
        if (i == 66) {
            hideSoftKeyboard();
            int size = GameMidlet.allField.size();
            for (int i2 = 0; i2 < size; i2++) {
                if (GameMidlet.allField.elementAt(i2).ID == this.ID) {
                    GameMidlet.allField.elementAt(i2).setText(getString());
                    ((GameMidlet) GCanvas.gCanvas).update(2, this);
                }
            }
            if (GCanvas.gameScr.chatMode) {
                GCanvas.gameScr.chatFast();
            }
            setVisible(false);
        }
        if (i == 4) {
            hideSoftKeyboard();
            int size2 = GameMidlet.allField.size();
            for (int i3 = 0; i3 < size2; i3++) {
                if (GameMidlet.allField.elementAt(i3).ID == this.ID) {
                    GameMidlet.allField.elementAt(i3).setText(getString());
                    ((GameMidlet) GCanvas.gCanvas).update(2, this);
                }
            }
            if (GCanvas.gameScr.chatMode) {
                GCanvas.gameScr.chatFast();
            }
            setVisible(false);
        }
        return super.onKeyDown(i, keyEvent);
    }

    @Override
    public void onFocusChanged(boolean z, int i, Rect rect) {
        if (!z) {
            hideSoftKeyboard();
            int size = GameMidlet.allField.size();
            for (int i2 = 0; i2 < size; i2++) {
                TField tFieldElementAt = GameMidlet.allField.elementAt(i2);
                if (tFieldElementAt.ID == this.ID) {
                    tFieldElementAt.setText(getString());
                    ((GameMidlet) GCanvas.gCanvas).update(2, this);
                }
            }
            setVisible(false);
        } else {
            showSoftKeyboard();
        }
        super.onFocusChanged(z, i, rect);
    }

    public void hideSoftKeyboard() {
        ((InputMethodManager) GCanvas.Context.getSystemService("input_method")).hideSoftInputFromWindow(getWindowToken(), 0);
    }

    public void showSoftKeyboard() {
        ((InputMethodManager) GCanvas.Context.getSystemService("input_method")).showSoftInput(this, 0);
    }

    public void OkiTest() {
        hideSoftKeyboard();
        for (int i = 0; i < GameMidlet.allField.size(); i++) {
            if (GameMidlet.allField.elementAt(i).ID == this.ID) {
                GameMidlet.allField.elementAt(i).setText(getString());
            }
        }
        isVisible = false;
    }
}
