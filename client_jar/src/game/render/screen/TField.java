package game.render.screen;

import game.GameMidlet;
import game.model.Command;
import game.model.IAction;
import javax.microedition.lcdui.Canvas;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;
import game.pokemon.ChuPokemon;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.TextBox;
import javax.microedition.midlet.MIDlet;

public class TField extends ObjGame {
    public boolean isFocus;
    public boolean lockArrow;
    public boolean paintFocus;
    private static final int CARET_WIDTH = 1;
    private static final int CARET_SHOWING_TIME = 5;
    private static final int TEXT_GAP_X = 4;
    private static final int MAX_SHOW_CARET_COUNER = 10;
    public static final int INPUT_TYPE_ANY = 0;
    public static final int INPUT_TYPE_NUMERIC = 1;
    public static final int INPUT_TYPE_PASSWORD = 2;
    public static final int INPUT_ALPHA_NUMBER_ONLY = 3;
    private String text;
    private String passwordText;
    private String paintedText;
    private int caretPos;
    private int counter;
    private int maxTextLenght;
    private int offsetX;
    private int keyInActiveState;
    private int indexOfActiveChar;
    private int showCaretCounter;
    private int inputType;
    public static boolean isQwerty;
    public static int typingModeAreaWidth;
    public static int timeChangeMode;
    public static final int NOKIA = 0;
    public static final int MOTO = 1;
    public static final int ORTHER = 2;
    public static final int BB = 3;
    public static final byte abc = 0;
    public static final byte Abc = 1;
    public static final byte ABC = 2;
    public static final byte number123 = 3;
    public static Canvas c;
    public static MIDlet m;
    public Command cmdClear;
    Screen parentScr;
    int holdCount;
    private boolean openVirtual;
    public static int typeXpeed = 2;
    private static final int[] MAX_TIME_TO_CONFIRM_KEY = {18, 14, 11, 9, 6, 4, 2};
    private static int CARET_HEIGHT = 0;
    private static String[] print = {" 0", ".,@?!_1\"/$-():*+<=>;%&~#%^&*{}[];'/1", "abc2áàảãạâấầẩẫậăắằẳẵặ2", "def3đéèẻẽẹêếềểễệ3", "ghi4íìỉĩị4", "jkl5", "mno6óòỏõọôốồổỗộơớờởỡợ6", "pqrs7", "tuv8úùủũụưứừửữự8", "wxyz9ýỳỷỹỵ9", "*", "#"};
    private static String[] printA = {"0", "1", "abc2", "def3", "ghi4", "jkl5", "mno6", "pqrs7", "tuv8", "wxyz9", "0", "0"};
    private static String[] printBB = {" 0", "er1", "ty2", "ui3", "df4", "gh5", "jk6", "cv7", "bn8", "m9", "0", "0", "qw!", "as?", "zx", "op.", "l,"};
    private static int lastKey = -1984;
    public static int mode = 0;
    public static final String[] modeNotify = {"abc", "Abc", "ABC", "123"};
    public static int changeModeKey = 11;
    public static int[][] BBKEY = {new int[]{32, 48}, new int[]{49, 69}, new int[]{50, 84}, new int[]{51, 85}, new int[]{52, 68}, new int[]{53, 71}, new int[]{54, 74}, new int[]{55, 67}, new int[]{56, 66}, new int[]{57, 77}, new int[]{42, 128}, new int[]{35, 137}, new int[]{33, 113}, new int[]{63, 97}, new int[]{64, 121, 122}, new int[]{46, 111}, new int[]{44, 108}};

    public void doChangeToTextBox() {
        c = game.render.Canvas.instance;
        m = GameMidlet.instance;
        final TextBox tb = new TextBox("", "", 500, 0);
        tb.addCommand(new javax.microedition.lcdui.Command("OK", 4, 0));
        tb.addCommand(new javax.microedition.lcdui.Command("Cancel", 3, 0));
        tb.setCommandListener(new CommandListener() {
            public void commandAction(javax.microedition.lcdui.Command cmd, Displayable arg1) {
                if (cmd.getLabel().equals("OK")) {
                    TField.this.setText(tb.getString());
                }
                Display.getDisplay(TField.m).setCurrent(TField.c);
                TField.c.setFullScreenMode(true);
                TField.c = null;
                TField.m = null;
            }
        });
        try {
            if (this.inputType == 2) {
                tb.setConstraints(65536);
            } else if (this.inputType == 1) {
                tb.setConstraints(2);
            } else {
                tb.setConstraints(0);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        tb.setString(getText());
        tb.setMaxSize(this.maxTextLenght);
        Display.getDisplay(m).setCurrent(tb);
    }

    public static void setVendorTypeMode(int mode2) {
        if (mode2 == 1) {
            print[0] = "0";
            print[10] = " *";
            print[11] = "#";
            changeModeKey = 35;
            return;
        }
        if (mode2 == 0) {
            print[0] = " 0";
            print[10] = "*";
            print[11] = "#";
            changeModeKey = 35;
            return;
        }
        if (mode2 == 2) {
            print[0] = "0";
            print[10] = "*";
            print[11] = " #";
            changeModeKey = 42;
        }
    }

    private void init() {
        CARET_HEIGHT = Font.normalFont[0].getHeight() + 1;
        this.cmdClear = new Command("Xóa", new IAction() {
            @Override
            public void perform() {
                TField.this.clear();
            }
        });
        if (this.parentScr != null) {
            this.parentScr.right = this.cmdClear;
        }
    }

    public TField(Screen parentScr) {
        this.lockArrow = false;
        this.paintFocus = true;
        this.text = "";
        this.passwordText = "";
        this.paintedText = "";
        this.caretPos = 0;
        this.counter = 0;
        this.maxTextLenght = 500;
        this.offsetX = 0;
        this.keyInActiveState = 0;
        this.indexOfActiveChar = 0;
        this.showCaretCounter = 10;
        this.inputType = 0;
        this.text = "";
        this.parentScr = parentScr;
        init();
    }

    public TField() {
        this.lockArrow = false;
        this.paintFocus = true;
        this.text = "";
        this.passwordText = "";
        this.paintedText = "";
        this.caretPos = 0;
        this.counter = 0;
        this.maxTextLenght = 500;
        this.offsetX = 0;
        this.keyInActiveState = 0;
        this.indexOfActiveChar = 0;
        this.showCaretCounter = 10;
        this.inputType = 0;
        this.text = "";
        init();
    }

    public TField(int x, int y, int w, int h) {
        this.lockArrow = false;
        this.paintFocus = true;
        this.text = "";
        this.passwordText = "";
        this.paintedText = "";
        this.caretPos = 0;
        this.counter = 0;
        this.maxTextLenght = 500;
        this.offsetX = 0;
        this.keyInActiveState = 0;
        this.indexOfActiveChar = 0;
        this.showCaretCounter = 10;
        this.inputType = 0;
        this.text = "";
        init();
        this.x = x;
        this.y = y;
        this.width = w;
        this.height = h;
    }

    public TField(String text, int maxLen, int inputType) {
        this.lockArrow = false;
        this.paintFocus = true;
        this.text = "";
        this.passwordText = "";
        this.paintedText = "";
        this.caretPos = 0;
        this.counter = 0;
        this.maxTextLenght = 500;
        this.offsetX = 0;
        this.keyInActiveState = 0;
        this.indexOfActiveChar = 0;
        this.showCaretCounter = 10;
        this.inputType = 0;
        this.text = text;
        this.maxTextLenght = maxLen;
        this.inputType = inputType;
        init();
        this.isTfield = true;
    }

    public void clear() {
        if (this.caretPos > 0 && this.text.length() > 0) {
            this.text = new StringBuffer(String.valueOf(this.text.substring(0, this.caretPos - 1))).append(this.text.substring(this.caretPos, this.text.length())).toString();
            this.caretPos--;
            setOffset();
            setPasswordTest();
        }
    }

    public void setOffset() {
        if (this.inputType == 2) {
            this.paintedText = this.passwordText;
        } else {
            this.paintedText = this.text;
        }
        if (this.offsetX < 0 && Font.arialFont.getWidthNguyenVan(this.paintedText) + this.offsetX < ((this.width - 4) - 13) - typingModeAreaWidth) {
            this.offsetX = ((this.width - 10) - typingModeAreaWidth) - Font.arialFont.getWidthNguyenVan(this.paintedText);
        }
        if (this.offsetX + Font.arialFont.getWidthNguyenVan(this.paintedText.substring(0, this.caretPos)) <= 0) {
            this.offsetX = -Font.arialFont.getWidthNguyenVan(this.paintedText.substring(0, this.caretPos));
            this.offsetX += 40;
        } else if (this.offsetX + Font.arialFont.getWidthNguyenVan(this.paintedText.substring(0, this.caretPos)) >= (this.width - 12) - typingModeAreaWidth) {
            this.offsetX = (((this.width - 10) - typingModeAreaWidth) - Font.arialFont.getWidthNguyenVan(this.paintedText.substring(0, this.caretPos))) - 8;
        }
        if (this.offsetX > 0) {
            this.offsetX = 0;
        }
    }

    private void keyPressedAny(int keyCode) {
        String[] print2;
        char c2;
        char c3;
        if (game.render.Canvas.isBB) {
            print2 = printBB;
        } else if (this.inputType == 2 || this.inputType == 3) {
            print2 = printA;
        } else {
            print2 = print;
        }
        if (game.render.Canvas.isBB) {
            keyCode = getBBKeyCode(keyCode);
            if (keyCode == -1) {
                return;
            }
        }
        if (keyCode == lastKey) {
            this.indexOfActiveChar = (this.indexOfActiveChar + 1) % print2[keyCode - 48].length();
            char c4 = print2[keyCode - 48].charAt(this.indexOfActiveChar);
            if (mode == 0) {
                c3 = Character.toLowerCase(c4);
            } else if (mode == 1 || mode == 2) {
                c3 = Character.toUpperCase(c4);
            } else {
                c3 = print2[keyCode - 48].charAt(print2[keyCode - 48].length() - 1);
            }
            String ttext = new StringBuffer(String.valueOf(this.text.substring(0, this.caretPos - 1))).append(c3).toString();
            if (this.caretPos < this.text.length()) {
                ttext = new StringBuffer(String.valueOf(ttext)).append(this.text.substring(this.caretPos, this.text.length())).toString();
            }
            this.text = ttext;
            this.keyInActiveState = MAX_TIME_TO_CONFIRM_KEY[typeXpeed];
            setPasswordTest();
        } else if (this.text.length() < this.maxTextLenght) {
            if (mode == 1 && lastKey != -1984) {
                mode = 0;
            }
            this.indexOfActiveChar = 0;
            char c5 = print2[keyCode - 48].charAt(this.indexOfActiveChar);
            if (mode == 0) {
                c2 = Character.toLowerCase(c5);
            } else if (mode == 1 || mode == 2) {
                c2 = Character.toUpperCase(c5);
            } else {
                c2 = print2[keyCode - 48].charAt(print2[keyCode - 48].length() - 1);
            }
            String ttext2 = new StringBuffer(String.valueOf(this.text.substring(0, this.caretPos))).append(c2).toString();
            if (this.caretPos < this.text.length()) {
                ttext2 = new StringBuffer(String.valueOf(ttext2)).append(this.text.substring(this.caretPos, this.text.length())).toString();
            }
            this.text = ttext2;
            this.keyInActiveState = MAX_TIME_TO_CONFIRM_KEY[typeXpeed];
            this.caretPos++;
            setPasswordTest();
            setOffset();
        }
        lastKey = keyCode;
    }

    private void keyPressedAscii(int keyCode) {
        if (((this.inputType != 2 && this.inputType != 3) || ((keyCode >= 48 && keyCode <= 57) || ((keyCode >= 65 && keyCode <= 90) || (keyCode >= 97 && keyCode <= 122)))) && this.text.length() < this.maxTextLenght) {
            String ttext = new StringBuffer(String.valueOf(this.text.substring(0, this.caretPos))).append((char) keyCode).toString();
            if (this.caretPos < this.text.length()) {
                ttext = new StringBuffer(String.valueOf(ttext)).append(this.text.substring(this.caretPos, this.text.length())).toString();
            }
            this.text = ttext;
            this.caretPos++;
            setPasswordTest();
            setOffset();
        }
    }

    public void keyHold(int keyCode) {
        this.holdCount++;
        if (this.holdCount > 15 && !isQwerty && keyCode < print.length) {
            clear();
            keyPressedAscii(print[keyCode].charAt(print[keyCode].length() - 1));
            this.keyInActiveState = MAX_TIME_TO_CONFIRM_KEY[typeXpeed];
            this.holdCount = 0;
        }
    }

    public static void setMode() {
        mode++;
        if (mode > 3) {
            mode = 0;
        }
        lastKey = changeModeKey;
        timeChangeMode = (int) (System.currentTimeMillis() / 1000);
    }

    @Override
    public boolean keyPressed(int keyCode) {
        if (!this.isFocus) {
            return true;
        }
        if (game.render.Canvas.isBB) {
            if (keyCode == 8 || keyCode == 127) {
                clear();
            }
        } else if (keyCode == 8 || keyCode == -8 || keyCode == 204) {
            clear();
            return true;
        }
        if (!game.render.Canvas.isBB && keyCode >= 65 && keyCode <= 122) {
            isQwerty = true;
            typingModeAreaWidth = 0;
        }
        this.holdCount = 0;
        if (keyCode >= 65 && keyCode <= 122) {
            isQwerty = true;
            typingModeAreaWidth = 0;
        }
        if (isQwerty && !game.render.Canvas.isBB) {
            if (keyCode == 45) {
                if (keyCode == lastKey && this.keyInActiveState < MAX_TIME_TO_CONFIRM_KEY[typeXpeed]) {
                    this.text = new StringBuffer(String.valueOf(this.text.substring(0, this.caretPos - 1))).append('_').toString();
                    this.paintedText = this.text;
                    setPasswordTest();
                    setOffset();
                    lastKey = -1984;
                    return false;
                }
                lastKey = 45;
            }
            if (keyCode >= 32) {
                keyPressedAscii(keyCode);
                return true;
            }
        }
        if (keyCode == changeModeKey) {
            mode++;
            if (mode > 3) {
                mode = 0;
            }
            this.keyInActiveState = 1;
            lastKey = keyCode;
            return true;
        }
        if (keyCode == 42) {
            keyCode = 58;
        }
        if (keyCode == 35) {
            keyCode = 59;
        }
        if (game.render.Canvas.isBB && keyCode >= 48) {
            if (isQwerty) {
                keyPressedAscii(keyCode);
                this.keyInActiveState = 1;
                return false;
            }
            if (this.inputType == 0 || this.inputType == 2 || this.inputType == 3) {
                keyPressedAny(keyCode);
                return false;
            }
            if (this.inputType == 1) {
                keyPressedAscii(keyCode);
                this.keyInActiveState = 1;
                return false;
            }
            return false;
        }
        if (keyCode >= 48 && keyCode <= 59) {
            if (this.inputType == 0 || this.inputType == 2 || this.inputType == 3) {
                keyPressedAny(keyCode);
                return true;
            }
            if (this.inputType == 1) {
                keyPressedAscii(keyCode);
                this.keyInActiveState = 1;
                return true;
            }
            return true;
        }
        this.indexOfActiveChar = 0;
        lastKey = -1984;
        if (keyCode == -3 && !this.lockArrow) {
            if (this.caretPos > 0) {
                this.caretPos--;
                setOffset();
                this.showCaretCounter = 10;
                return true;
            }
            return false;
        }
        if (keyCode == -4 && !this.lockArrow) {
            if (this.caretPos < this.text.length()) {
                this.caretPos++;
                setOffset();
                this.showCaretCounter = 10;
                return true;
            }
            return false;
        }
        if (keyCode == -8) {
            clear();
            return true;
        }
        lastKey = keyCode;
        return false;
    }

    public void pointerRelease(int px, int py) {
    }

    @Override
    public void paint(Graphics g) {
        boolean isFocus = isFocused();
        if (this.inputType == 2) {
            this.paintedText = this.passwordText;
        } else {
            this.paintedText = this.text;
        }
        if (isFocus) {
            if (this.paintFocus) {
                g.setColor(16767892);
                g.fillRect(this.x + 1, this.y + 1, this.width - 1, this.height - 1);
                g.setColor(7950336);
            }
        } else {
            g.setColor(11768142);
            g.fillRect(this.x + 1, this.y + 1, this.width - 1, this.height - 1);
            g.setColor(5979911);
        }
        g.drawRect(this.x + 1, this.y + 1, this.width - 2, this.height - 2);
        g.setClip(this.x + 3, this.y + 1, (this.width - typingModeAreaWidth) - 8, this.height - 4);
        g.setColor(0);
        // pokemon (G7.C3): chu nguoi choi dang go giu nguyen (khong qua ChuPokemon)
        ChuPokemon.KhongDoi = true;
        try {
            Font.arialFont.drawString(g, this.paintedText, 4 + this.offsetX + this.x, this.y + ((this.height - Font.arialFont.getHeight()) / 2), 0);
        } finally {
            ChuPokemon.KhongDoi = false;
        }
        if (isFocused() && this.keyInActiveState == 0) {
            if (this.showCaretCounter > 0 || (this.counter / 5) % 2 == 0) {
                g.setColor(0);
                g.fillRect((((5 + this.offsetX) + this.x) + Font.arialFont.getWidthNguyenVan(this.paintedText.substring(0, this.caretPos))) - 1, this.y + ((this.height - CARET_HEIGHT) / 2) + 1, 1, CARET_HEIGHT);
            }
        }
    }

    private boolean isFocused() {
        return this.isFocus;
    }

    private void setPasswordTest() {
        if (this.inputType == 2) {
            this.passwordText = "";
            for (int i = 0; i < this.text.length(); i++) {
                this.passwordText = new StringBuffer(String.valueOf(this.passwordText)).append("*").toString();
            }
            if (this.keyInActiveState > 0 && this.caretPos > 0) {
                this.passwordText = new StringBuffer(String.valueOf(this.passwordText.substring(0, this.caretPos - 1))).append(this.text.charAt(this.caretPos - 1)).append(this.passwordText.substring(this.caretPos, this.passwordText.length())).toString();
            }
        }
    }

    public void paintFocus(Graphics g) {
    }

    @Override
    public void update() {
        this.counter++;
        if (this.keyInActiveState > 0) {
            this.keyInActiveState--;
            if (this.keyInActiveState == 0) {
                this.indexOfActiveChar = 0;
                if (mode == 1 && lastKey != changeModeKey) {
                    mode = 0;
                }
                lastKey = -1984;
                setPasswordTest();
            }
        }
        if (this.showCaretCounter > 0) {
            this.showCaretCounter--;
        }
        if (game.render.Canvas.isPointerClick) {
            if ((game.render.Canvas.currentDialog == null || game.render.Canvas.currentDialog == game.render.Canvas.inputDlg) && !game.render.Canvas.menu.showMenu) {
                setTextBox();
            }
        }
    }

    private void setTextBox() {
        if (game.render.Canvas.isPointer(0, 0, game.render.Canvas.w, game.render.Canvas.h)) {
            if (game.render.Canvas.isPointer(this.x, this.y, this.width, this.height)) {
                if (!this.isFocus) {
                    this.isFocus = true;
                    return;
                }
                game.render.Canvas.isPointerClick = false;
                if (!game.render.Canvas.isVirTualKey) {
                    this.openVirtual = true;
                    game.render.Canvas.isVirTualKey = true;
                    game.render.Canvas.instance.sizeChanged(0, 0);
                }
                VirTualKeyB.isABC = true;
                return;
            }
            if (this.openVirtual) {
                game.render.Canvas.isPointerClick = false;
                game.render.Canvas.isVirTualKey = false;
                game.render.Canvas.instance.sizeChanged(0, 0);
                this.openVirtual = false;
            }
            this.isFocus = false;
        }
    }

    public String getText() {
        return this.text;
    }

    public void setText(String text) {
        if (text == null) {
            return;
        }
        lastKey = -1984;
        this.keyInActiveState = 0;
        this.indexOfActiveChar = 0;
        this.text = text;
        this.paintedText = text;
        setPasswordTest();
        this.caretPos = text.length();
        setOffset();
    }

    public void insertText(String text) {
        this.text = new StringBuffer(String.valueOf(this.text.substring(0, this.caretPos))).append(text).append(this.text.substring(this.caretPos)).toString();
        setPasswordTest();
        this.caretPos += text.length();
        setOffset();
    }

    public int getMaxTextLenght() {
        return this.maxTextLenght;
    }

    public void setMaxTextLenght(int maxTextLenght) {
        this.maxTextLenght = maxTextLenght;
    }

    public int getIputType() {
        return this.inputType;
    }

    public void setIputType(int iputType) {
        this.inputType = iputType;
    }

    public int getBBKeyCode(int keyCode) {
        for (int i = 0; i < BBKEY.length; i++) {
            for (int j = 0; j < BBKEY[i].length; j++) {
                if (BBKEY[i][j] == keyCode) {
                    return i + 48;
                }
            }
        }
        return -1;
    }
}
