package game.render.screen;

import android.view.inputmethod.InputMethodManager;
import com.htbb.client.GameMidlet;
import game.core.j2me.Graphics;
import game.model.Command;
import game.model.FrameImage;
import game.model.IAction;
import game.render.GCanvas;

public class TField {
    private static int CARET_HEIGHT = 0;
    private static final int CARET_SHOWING_TIME = 5;
    private static final int CARET_WIDTH = 1;
    public static final int INPUT_ALPHA_NUMBER_ONLY = 3;
    public static final int INPUT_TYPE_ANY = 0;
    public static final int INPUT_TYPE_NUMERIC = 1;
    public static final int INPUT_TYPE_PASSWORD = 2;
    private static final int MAX_SHOW_CARET_COUNER = 10;
    public static final int MOTO = 1;
    public static final int NOKIA = 0;
    public static final int ORTHER = 2;
    private static final int TEXT_GAP_X = 4;
    public static GCanvas c = null;
    public static FrameImage frame = null;
    static InputMethodManager imm = null;
    public static boolean isOpenTextBox = false;
    public static boolean isQwerty = false;
    public static GameMidlet m = null;
    public static int mode = 0;
    public static int timeChangeMode = 0;
    public static int typeXpeed = 2;
    public static int typingModeAreaWidth;
    public int ID;
    private int caretPos;
    public Command cmdClear;
    private int counter;
    myEditText editText;
    public int height;
    public int inputType;
    public boolean isChangeFocus;
    public boolean isFocus;
    boolean isposition;
    private int keyInActiveState;
    private int lastKey;
    public boolean lockArrow;
    private int maxTextLenght;
    private int offsetX;
    public boolean paintFocus;
    private String paintedText;
    Screen parentScr;
    private String passwordText;
    public String sDefaust;
    private int showCaretCounter;
    int tempTime;
    private String text;
    public boolean visible;
    public int width;
    public int x;
    public int y;
    int yt;
    public static final String[] modeNotify = {"abc", "Abc", "ABC", "123"};
    public static int changeModeKey = 11;

    private void keyPressedAny(int i) {
    }

    private void keyPressedAscii(int i) {
    }

    public static boolean setNormal(char c2) {
        if (c2 >= '0' && c2 <= '9') {
            return true;
        }
        if (c2 < 'A' || c2 > 'Z') {
            return c2 >= 'a' && c2 <= 'z';
        }
        return true;
    }

    public boolean keyPressed(int i) {
        return true;
    }

    public void doChangeToTextBox() {
        this.isFocus = true;
        myEditText.isVisible = true;
        myEditText myedittext = new myEditText(GCanvas.Context, this.width * GCanvas.screenlevel, GCanvas.screenlevel * 30);
        this.editText = myedittext;
        myedittext.setPosition(this.x * GCanvas.screenlevel, this.y * GCanvas.screenlevel);
        if (getText() != null) {
            this.editText.setText(getText());
        }
        this.editText.setmyInputType(this.inputType);
        this.editText.setMaxTextInput(this.maxTextLenght);
        this.editText.setVisible(true);
        this.editText.ID = this.ID;
        ((GameMidlet) GCanvas.gCanvas).update(1, this.editText);
    }

    public void positionLogin() {
        this.editText.setPositionLogin();
    }

    private void init() {
        try {
            CARET_HEIGHT = Font.arialFontBlack.getHeight() + 1;
            Command command = new Command("", new IAction() {
                @Override
                public void perform() {
                }
            });
            this.cmdClear = command;
            Screen screen = this.parentScr;
            if (screen != null) {
                screen.right = command;
            }
            typingModeAreaWidth = Font.arialFontBlack.getWidth("ABC") + 5;
        } catch (Exception unused) {
            CARET_HEIGHT = 16;
            typingModeAreaWidth = 20;
        }
        GameMidlet.allField.add(this);
        this.ID = GameMidlet.getID();
    }

    public TField(Screen screen) {
        this.width = 100;
        this.height = 30;
        this.lockArrow = false;
        this.paintFocus = true;
        this.isChangeFocus = true;
        this.passwordText = "";
        this.paintedText = "";
        this.caretPos = 0;
        this.counter = 0;
        this.maxTextLenght = 500;
        this.offsetX = 0;
        this.lastKey = -1984;
        this.keyInActiveState = 0;
        this.showCaretCounter = 10;
        this.inputType = 0;
        this.sDefaust = "";
        this.visible = false;
        this.isposition = false;
        this.yt = 0;
        this.tempTime = -1;
        this.parentScr = screen;
        this.text = "";
        init();
    }

    public TField() {
        this.width = 100;
        this.height = 30;
        this.lockArrow = false;
        this.paintFocus = true;
        this.isChangeFocus = true;
        this.passwordText = "";
        this.paintedText = "";
        this.caretPos = 0;
        this.counter = 0;
        this.maxTextLenght = 500;
        this.offsetX = 0;
        this.lastKey = -1984;
        this.keyInActiveState = 0;
        this.showCaretCounter = 10;
        this.inputType = 0;
        this.sDefaust = "";
        this.visible = false;
        this.isposition = false;
        this.yt = 0;
        this.tempTime = -1;
        this.text = "";
        init();
    }

    public TField(int i, int i2, int i3, int i4) {
        this.width = 100;
        this.height = 30;
        this.lockArrow = false;
        this.paintFocus = true;
        this.isChangeFocus = true;
        this.passwordText = "";
        this.paintedText = "";
        this.caretPos = 0;
        this.counter = 0;
        this.maxTextLenght = 500;
        this.offsetX = 0;
        this.lastKey = -1984;
        this.keyInActiveState = 0;
        this.showCaretCounter = 10;
        this.inputType = 0;
        this.sDefaust = "";
        this.visible = false;
        this.isposition = false;
        this.yt = 0;
        this.tempTime = -1;
        this.text = "";
        init();
        this.x = i;
        this.y = i2;
        this.width = i3;
        this.height = i4;
    }

    public TField(String str, int i, int i2) {
        this.width = 100;
        this.height = 30;
        this.lockArrow = false;
        this.paintFocus = true;
        this.isChangeFocus = true;
        this.passwordText = "";
        this.paintedText = "";
        this.caretPos = 0;
        this.counter = 0;
        this.offsetX = 0;
        this.lastKey = -1984;
        this.keyInActiveState = 0;
        this.showCaretCounter = 10;
        this.sDefaust = "";
        this.visible = false;
        this.isposition = false;
        this.yt = 0;
        this.tempTime = -1;
        this.text = str;
        this.maxTextLenght = i;
        this.inputType = i2;
        init();
    }

    public void clear() {
        if (this.caretPos <= 0 || this.text.length() <= 0) {
            return;
        }
        StringBuilder sbAppend = new StringBuilder().append(this.text.substring(0, this.caretPos - 1));
        String str = this.text;
        this.text = sbAppend.append(str.substring(this.caretPos, str.length())).toString();
        this.caretPos--;
        setOffset();
        setPasswordTest();
    }

    public void setOffset() {
        if (this.inputType == 2) {
            this.paintedText = this.passwordText;
        } else {
            this.paintedText = this.text;
        }
        if (this.offsetX < 0) {
            int width = Font.arialFontBlack.getWidth(this.paintedText) + this.offsetX;
            int i = this.width;
            int i2 = typingModeAreaWidth;
            if (width < (i - 17) - i2) {
                this.offsetX = ((i - 10) - i2) - Font.arialFontBlack.getWidth(this.paintedText);
            }
        }
        if (this.offsetX > 0) {
            this.offsetX = 0;
        }
    }

    public void paint(Graphics graphics) {
        boolean zIsFocused = isFocused();
        if (this.inputType == 2) {
            this.paintedText = this.passwordText;
        } else {
            this.paintedText = this.text;
        }
        if (zIsFocused) {
            if (this.paintFocus) {
                graphics.setColor(-8826880);
                graphics.fillRect(this.x, this.y, this.width + 1, this.height + 1);
                graphics.setColor(-9324);
                graphics.fillRect(this.x + 1, this.y + 1, this.width - 1, this.height - 1);
            }
        } else {
            graphics.setColor(-10797305);
            graphics.fillRect(this.x, this.y, this.width + 1, this.height + 1);
            graphics.setColor(-5009074);
            graphics.fillRect(this.x + 1, this.y + 1, this.width - 1, this.height - 1);
        }
        graphics.setClip(this.x + 3, this.y + 1, this.width - 6, this.height - 4);
        graphics.ClipRec(this.x + 3, this.y + 1, this.width - 6, this.height - 4);
        graphics.setColor(0);
        String str = this.paintedText;
        if (zIsFocused) {
            int i = this.tempTime + 1;
            this.tempTime = i;
            if (i % 20 > 15) {
                str = str + "|";
            }
        } else {
            this.tempTime = -1;
        }
        Font.arialFontBlack.drawString(graphics, str, this.offsetX + 4 + this.x, (this.y + ((this.height - Font.arialFontBlack.getHeight()) / 2)) - (GCanvas.screenlevel == 1 ? 0 : 2), 0);
        graphics.restoreCanvas();
    }

    boolean isFocused() {
        return this.isFocus;
    }

    private void setPasswordTest() {
        if (this.inputType == 2) {
            this.passwordText = "";
            for (int i = 0; i < this.text.length(); i++) {
                this.passwordText += "*";
            }
            if (this.keyInActiveState <= 0 || this.caretPos <= 0) {
                return;
            }
            StringBuilder sbAppend = new StringBuilder().append(this.passwordText.substring(0, this.caretPos - 1)).append(this.text.charAt(this.caretPos - 1));
            String str = this.passwordText;
            this.passwordText = sbAppend.append(str.substring(this.caretPos, str.length())).toString();
        }
    }

    public void update() {
        if (!GCanvas.isPointerClick || GCanvas.menu.showMenu) {
            return;
        }
        setTextBox();
    }

    public void setTextBox() {
        if (GCanvas.isPoint(this.x, this.y, this.width, this.height)) {
            if (GCanvas.currentDialog != null && !this.isFocus) {
                this.isFocus = true;
            }
            doChangeToTextBox();
            return;
        }
        this.isFocus = false;
        myEditText myedittext = this.editText;
        if (myedittext != null) {
            setText(myedittext.getString());
            ((GameMidlet) GCanvas.gCanvas).removeEditText();
            this.editText.OkiTest();
            this.editText = null;
        }
        if (this.isChangeFocus) {
            this.isFocus = false;
        }
    }

    public String getText() {
        return this.text;
    }

    public void setText(String str) {
        myEditText myedittext;
        if (str.trim() == null) {
            return;
        }
        this.text = str;
        this.paintedText = str;
        setPasswordTest();
        this.caretPos = str.length();
        setOffset();
        if (str != "" || (myedittext = this.editText) == null) {
            return;
        }
        myedittext.clear();
    }

    public void insertText(String str) {
        this.text = this.text.substring(0, this.caretPos) + str + this.text.substring(this.caretPos);
        setPasswordTest();
        this.caretPos += str.length();
        setOffset();
    }

    public int getMaxTextLenght() {
        return this.maxTextLenght;
    }

    public void setMaxTextLenght(int i) {
        this.maxTextLenght = i;
    }

    public int getIputType() {
        return this.inputType;
    }

    public void setIputType(int i) {
        this.inputType = i;
    }

    public void setFocus(boolean z) {
        this.isFocus = z;
    }
}
