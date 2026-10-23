package game.render.screen;

import game.model.FilePack;
import game.pokemon.ChuPokemon;
import game.render.Res;
import java.util.Vector;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;

public class Font {
    public static final int LEFT = 0;
    public static final int RIGHT = 1;
    public static final int CENTER = 2;
    private Image imgFont;
    private String charList;
    public byte[] charWidth;
    private int charHeight;
    private int charSpace;
    public static Font normalFontColor;
    public static Font borderFont;
    public static Font bigFont;
    public static Font blackFont;
    public static Font arialFont;
    public static Font smallFontYellow;
    public static Font smallFont;
    public static Font arialFontW;
    public static Font[] smallFontColor = new Font[5];
    public static Font[] normalFont = new Font[6];

    /**
     * 0.9.7.1 (BoNho.giai - thiếu bộ nhớ): bỏ các ảnh phụ tạo thêm của mọi font (bản tô đậm anhToi, chữ hoa có dấu
     * ghép boGhep). Tạo lại khi cần; ảnh font gốc giữ nguyên.
     */
    public static void giaiPhong() {
        Font[] ds = {normalFontColor, borderFont, bigFont, blackFont, arialFont, smallFontYellow, smallFont, arialFontW};
        for (int i = 0; i < ds.length; i++) {
            giaiPhong(ds[i]);
        }
        for (int i = 0; i < smallFontColor.length; i++) {
            giaiPhong(smallFontColor[i]);
        }
        for (int i = 0; i < normalFont.length; i++) {
            giaiPhong(normalFont[i]);
        }
    }

    private static void giaiPhong(Font f) {
        if (f != null) {
            f.anhToi = null;
            f.boGhep = null;
            f.boGhepToi = null;
        }
    }

    public static void init() {
        FilePack.init("/font.sh");
        for (int i = 0; i < 5; i++) {
            normalFont[i] = new Font(" 0123456789.,:!?()+*$#/-%abcdefghijklmnopqrstuvwxyzáàảãạăắằẳẵặâấầẩẫậéèẻẽẹêếềểễệíìỉĩịóòỏõọôốồổỗộơớờởỡợúùủũụưứừửữựýỳỷỹỵđABCDEFGHIJKLMNOPQRSTUVWXYZĐ~ĂÁ", new byte[]{4, 6, 5, 6, 6, 7, 6, 6, 6, 6, 6, 3, 3, 3, 4, 5, 4, 4, 6, 5, 8, 8, 6, 6, 10, 6, 7, 5, 7, 6, 4, 7, 7, 3, 4, 6, 3, 9, 7, 7, 7, 7, 5, 5, 4, 7, 6, 9, 6, 7, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 7, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 7, 6, 6, 3, 3, 3, 5, 3, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 8, 8, 8, 8, 8, 7, 7, 7, 7, 8, 7, 7, 7, 7, 7, 6, 6, 7, 7, 3, 5, 7, 6, 10, 8, 7, 7, 7, 6, 7, 7, 7, 7, 9, 7, 7, 8, 8, 6, 8, 7}, 13, "fb", i, 0);
        }
        normalFontColor = new Font(" 0123456789.,:!?()+*~#/-%abcdefghijklmnopqrstuvwxyzáàảãạăắằẳẵặâấầẩẫậéèẻẽẹêếềểễệíìỉĩịóòỏõọôốồổỗộơớờởỡợúùủũụưứừửữựýỳỷỹỵđABCDEFGHIJKLMNOPQRSTUVWXYZĐ$", new byte[]{3, 7, 6, 7, 7, 8, 7, 7, 7, 7, 7, 4, 4, 4, 4, 6, 4, 4, 7, 6, 9, 9, 6, 7, 10, 7, 7, 6, 7, 7, 5, 7, 7, 4, 5, 7, 4, 10, 7, 7, 7, 7, 6, 6, 5, 7, 7, 9, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 8, 7, 7, 8, 7, 8, 7, 7, 7, 7, 7, 7, 7, 7, 8, 7, 8, 7, 7, 4, 4, 4, 6, 4, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 8, 8, 8, 8, 8, 8, 7, 7, 7, 7, 8, 8, 7, 7, 7, 7, 6, 6, 7, 7, 4, 5, 8, 6, 9, 8, 7, 7, 7, 7, 7, 8, 7, 7, 9, 8, 8, 8, 8, 7}, 14, "fb11", 0, -1);
        borderFont = new Font(" 0123456789.,:!?()+*$#/-%abcdefghijklmnopqrstuvwxyzáàảãạăắằẳẵặâấầẩẫậéèẻẽẹêếềểễệíìỉĩịóòỏõọôốồổỗộơớờởỡợúùủũụưứừửữựýỳỷỹỵđABCDEFGHIJKLMNOPQRSTUVWXYZĐ~Á", new byte[]{4, 6, 5, 6, 6, 7, 6, 6, 6, 6, 6, 3, 3, 3, 4, 5, 4, 4, 6, 5, 8, 8, 6, 6, 10, 6, 7, 5, 7, 6, 4, 7, 7, 3, 4, 6, 3, 9, 7, 7, 7, 7, 5, 5, 4, 7, 6, 9, 6, 7, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 7, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 7, 6, 6, 3, 3, 3, 5, 3, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 8, 8, 8, 8, 8, 7, 7, 7, 7, 8, 7, 7, 7, 7, 7, 6, 6, 7, 7, 3, 5, 7, 6, 10, 8, 7, 7, 7, 6, 7, 7, 7, 7, 9, 7, 7, 8, 8, 6, 7}, 15, "fb1", 0, 0);
        bigFont = new Font(" 0123456789.,:!?()-'/ABCDEFGHIJKLMNOPQRSTUVWXYZÁÀẢÃẠĂẮẰẲẴẶÂẤẦẨẪẬÉÈẺẼẸÊẾỀỂỄỆÍÌỈĨỊÓÒỎÕỌÔỐỒỔỖỘƠỚỜỞỠỢÚÙỦŨỤƯỨỪỬỮỰÝỲỶỸỴĐ", new byte[]{4, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 4, 4, 4, 4, 8, 6, 6, 6, 3, 7, 10, 10, 10, 10, 8, 8, 10, 10, 5, 8, 9, 8, 13, 11, 10, 10, 10, 10, 10, 9, 10, 10, 13, 11, 11, 9, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 5, 5, 5, 5, 5, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10}, 20, "fcg14", 0, 0);
        blackFont = new Font(" 0123456789.,:!?()+*$#/-%abcdefghijklmnopqrstuvwxyzáàảãạăắằẳẵặâấầẩẫậéèẻẽẹêếềểễệíìỉĩịóòỏõọôốồổỗộơớờởỡợúùủũụưứừửữựýỳỷỹỵđABCDEFGHIJKLMNOPQRSTUVWXYZĐ~", new byte[]{4, 6, 4, 6, 6, 6, 6, 6, 6, 6, 6, 2, 2, 2, 2, 6, 4, 3, 6, 5, 6, 7, 3, 3, 10, 6, 6, 5, 6, 6, 4, 6, 6, 2, 2, 6, 2, 10, 6, 6, 6, 6, 4, 6, 3, 6, 5, 9, 5, 5, 5, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 3, 2, 3, 4, 2, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 7, 7, 7, 7, 7, 7, 6, 6, 6, 6, 6, 8, 8, 8, 8, 8, 8, 5, 5, 5, 5, 5, 7, 7, 7, 8, 8, 7, 6, 8, 8, 2, 5, 8, 7, 8, 8, 8, 7, 8, 8, 7, 7, 8, 7, 9, 7, 7, 7, 8, 5}, 14, "f", 0, 0);
        arialFont = new Font(" 0123456789.,:!?()+*$#/-%abcdefghijklmnopqrstuvwxyzáàảãạăắằẳẵặâấầẩẫậéèẻẽẹêếềểễệíìỉĩịóòỏõọôốồổỗộơớờởỡợúùủũụưứừửữựýỳỷỹỵđABCDEFGHIJKLMNOPQRSTUVWXYZĐ~@", new byte[]{4, 6, 4, 6, 6, 6, 6, 6, 6, 6, 6, 2, 2, 2, 2, 6, 4, 4, 6, 5, 6, 7, 4, 4, 10, 6, 6, 6, 6, 6, 4, 6, 6, 2, 2, 5, 2, 8, 6, 6, 6, 6, 4, 6, 3, 6, 6, 10, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 3, 2, 3, 4, 2, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 7, 7, 7, 7, 7, 7, 6, 6, 6, 6, 6, 8, 8, 8, 8, 8, 8, 6, 6, 6, 6, 6, 7, 8, 7, 7, 7, 6, 6, 8, 7, 2, 5, 7, 6, 8, 7, 8, 6, 8, 7, 7, 6, 7, 8, 11, 7, 8, 7, 7, 6, 9}, 13, "/arial11.png", 0);
        smallFontYellow = new Font("0123456789-+abcdefghijklmnopqrstuvwxyz ", new byte[]{10, 6, 10, 10, 10, 10, 11, 10, 10, 9, 8, 8, 11, 10, 10, 10, 9, 9, 10, 10, 6, 8, 11, 9, 11, 11, 11, 10, 11, 10, 10, 11, 11, 11, 11, 11, 11, 10, 5}, 11, "small_f", 0, 0);
        for (int i2 = 0; i2 < 5; i2++) {
            if (i2 != 4) {
                smallFontColor[i2] = new Font("0123456789+-%$:abcdefghijklmnopqrstuvwxyz", new byte[]{5, 3, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 7, 5, 3, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 7, 6, 5, 5, 5, 5, 5, 5, 5, 5, 7, 5, 5, 5}, 8, "fs", i2, -1);
            } else {
                smallFontColor[i2] = new Font("0123456789+-%$:abcdefghijklmnopqrstuvwxyz@", new byte[]{5, 3, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 7, 5, 3, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 7, 6, 5, 5, 5, 5, 5, 5, 5, 5, 7, 5, 5, 5, 7}, 8, "/font/fs4_red.png", -1);
            }
        }
        normalFont[5] = new Font(" 0123456789.,:!?()+*$#/-%abcdefghijklmnopqrstuvwxyzáàảãạăắằẳẵặâấầẩẫậéèẻẽẹêếềểễệíìỉĩịóòỏõọôốồổỗộơớờởỡợúùủũụưứừửữựýỳỷỹỵđABCDEFGHIJKLMNOPQRSTUVWXYZĐ~ĂÁ", new byte[]{4, 6, 5, 6, 6, 7, 6, 6, 6, 6, 6, 3, 3, 3, 4, 5, 4, 4, 6, 5, 8, 8, 6, 6, 10, 6, 7, 5, 7, 6, 4, 7, 7, 3, 4, 6, 3, 9, 7, 7, 7, 7, 5, 5, 4, 7, 6, 9, 6, 7, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 7, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 7, 6, 6, 3, 3, 3, 5, 3, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 8, 8, 8, 8, 8, 7, 7, 7, 7, 8, 7, 7, 7, 7, 7, 6, 6, 7, 7, 3, 5, 7, 6, 10, 8, 7, 7, 7, 6, 7, 7, 7, 7, 9, 7, 7, 8, 8, 6, 8, 7}, 13, "/font/fbv.png", 0);
        smallFont = new Font("0123456789+-./abcdefghijklmnopqrstuvwxyz:@ ", new byte[]{4, 3, 4, 4, 4, 4, 4, 4, 4, 4, 3, 3, 1, 3, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 1}, 5, "/font/fss_yellow.png", 1);
        arialFontW = new Font(" 0123456789.,:!?()+*$#/-%abcdefghijklmnopqrstuvwxyzáàảãạăắằẳẵặâấầẩẫậéèẻẽẹêếềểễệíìỉĩịóòỏõọôốồổỗộơớờởỡợúùủũụưứừửữựýỳỷỹỵđABCDEFGHIJKLMNOPQRSTUVWXYZĐ~", new byte[]{4, 6, 4, 6, 6, 6, 6, 6, 6, 6, 6, 2, 2, 2, 2, 6, 4, 4, 6, 5, 6, 7, 4, 4, 10, 6, 6, 6, 6, 6, 4, 6, 6, 2, 2, 5, 2, 8, 6, 6, 6, 6, 4, 6, 3, 6, 6, 10, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 3, 2, 3, 4, 2, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 7, 7, 7, 7, 7, 7, 6, 6, 6, 6, 6, 8, 8, 8, 8, 8, 8, 6, 6, 6, 6, 6, 7, 8, 7, 7, 7, 6, 6, 8, 7, 2, 5, 7, 6, 8, 7, 8, 6, 8, 7, 7, 6, 7, 8, 11, 7, 8, 7, 7, 6}, 13, "arialWhite", 0, 0);
        FilePack.reset();
    }

    public Font(String charList, byte[] charWidth, int charHeight, String fontFile, int type, int charSpace) {
        this.charSpace = charSpace;
        this.charList = charList;
        this.charWidth = charWidth;
        this.charHeight = charHeight;
        try {
            byte[] header = FilePack.hienTai().loadFile(new StringBuffer(String.valueOf(fontFile)).append(type).append("_h").toString());
            byte[] data = FilePack.hienTai().loadFile(new StringBuffer(String.valueOf(fontFile)).append("_data").toString());
            this.imgFont = Res.createImgByHeader(header, data);
        } catch (Exception e) {
            e.printStackTrace();
        }
        kiemBang();
    }

    /**
     * Danh sách chữ dài hơn bảng bề rộng hoặc số hàng của ảnh font (vd "~" cuối blackFont) làm
     * drawRegion ném lỗi và hỏng cả khung hình: cắt danh sách cho khớp, chữ thừa đi đường thay chữ.
     */
    private void kiemBang() {
        int n = this.charList.length();
        if (this.charWidth != null && this.charWidth.length < n) {
            n = this.charWidth.length;
        }
        if (this.imgFont != null && this.charHeight > 0 && this.imgFont.getHeight() / this.charHeight < n) {
            n = this.imgFont.getHeight() / this.charHeight;
        }
        if (n < this.charList.length()) {
            this.charList = this.charList.substring(0, n < 1 ? 1 : n);
        }
    }

    public Font(String charList, byte[] charWidth, int charHeight, String fontFile, int charSpace) {
        this.charSpace = charSpace;
        this.charList = charList;
        this.charWidth = charWidth;
        this.charHeight = charHeight;
        try {
            this.imgFont = Image.createImage(fontFile);
        } catch (Exception e) {
            e.printStackTrace();
        }
        kiemBang();
    }

    /**
     * pokemon (giao diện sáng kiểu GBA/DS): đang vẽ trên bảng kem/trắng (GiaoDien.bang) thì font có màu
     * chính sáng (trắng, xám nhạt, vàng) vẽ bằng bản tô navy của chính nó (anhToi, tạo khi cần) để đọc rõ.
     * GiaoDien.bang bật; đầu mỗi khung hình, GameScr.paint, thanh phím mềm, bảng tối / mờ tắt.
     */
    public static boolean nenSang;

    public void drawString(Graphics g, String st, int x, int y, int align) {
        // pokemon (G7.C3): moi chu ve ra man hinh doi sang the gioi Pokemon o day (co bo nho dem)
        if (st == null || g == null) {
            return;
        }
        if (st.indexOf('\u2212') >= 0) {
            st = st.replace('\u2212', '-');   // 0.9.8: dấu trừ U+2212 (chữ máy chủ) không có trong font
        }
        if (game.pokemon.GhiChu.bat && game.pokemon.GhiChu.trongChuDe == 0) {
            game.pokemon.GhiChu.chu(g, st, x, y, getWidth(st), getHeight(), align == CENTER ? 2 : align == RIGHT ? 1 : 0);
        }
        Image anh = this.imgFont;
        // Kiem lag 10/2026: bo ghep phai co TRUOC khi giu lai - truoc day boGhep con null thi ghep() trong veChuoi
        // tao bang moi roi khoi finally dat lai null: MOI lan ve chuoi co chu hoa co dau (Â, Ấ, Ữ...) lai ghep anh
        // tu dau (doc pixel + createRGBImage) - do duoc ~6 KB cap phat / khung o man Hanh trang (duong J2ME / APK).
        if (this.boGhep == null) {
            this.boGhep = new java.util.Hashtable();
        }
        java.util.Hashtable boCu = this.boGhep;
        if (nenSang && laSang()) {
            // chữ sáng trên bảng sáng: dùng bản tô đậm của chính font (cùng nét, cùng bề rộng)
            Image t = anhToi();
            if (t != null) {
                anh = t;
                if (this.boGhepToi == null) {
                    this.boGhepToi = new java.util.Hashtable();
                }
                this.boGhep = this.boGhepToi;
            }
        }
        Image anhCu = this.imgFont;
        this.imgFont = anh;
        try {
            veChuoi(g, st, x, y, align);
        } finally {
            this.imgFont = anhCu;
            this.boGhep = boCu;
        }
    }

    private java.util.Hashtable boGhepToi;
    private int sang = -1;      // 1 = màu chữ chính sáng (trắng / xám nhạt / vàng)
    private Image anhToi;

    /** Màu chủ đạo của ảnh font là màu sáng? (đếm điểm theo độ sáng, một lần) */
    private boolean laSang() {
        if (this.sang < 0) {
            this.sang = 0;
            try {
                int w = this.imgFont.getWidth();
                int h = this.imgFont.getHeight();
                int[] p = new int[w * h];
                this.imgFont.getRGB(p, 0, w, 0, 0, w, h);
                int nSang = 0, nToi = 0;
                for (int i = 0; i < p.length; i++) {
                    int c = p[i];
                    if ((c >>> 24) < 128) {
                        continue;
                    }
                    int l = ((c >> 16 & 255) * 3 + (c >> 8 & 255) * 6 + (c & 255)) / 10;
                    if (l > 170) {
                        nSang++;
                    } else {
                        nToi++;
                    }
                }
                this.sang = nSang > nToi ? 1 : 0;
            } catch (Throwable e) {
                this.sang = 0;
            }
        }
        return this.sang == 1;
    }

    /** Bản tô đậm: điểm sáng -> xanh navy thương hiệu (giữ viền tối sẵn có); tạo một lần. */
    private Image anhToi() {
        if (this.anhToi == null && this.imgFont != null) {
            try {
                int w = this.imgFont.getWidth();
                int h = this.imgFont.getHeight();
                int[] p = new int[w * h];
                this.imgFont.getRGB(p, 0, w, 0, 0, w, h);
                for (int i = 0; i < p.length; i++) {
                    int c = p[i];
                    if ((c >>> 24) < 128) {
                        p[i] = 0;
                        continue;
                    }
                    int l = ((c >> 16 & 255) * 3 + (c >> 8 & 255) * 6 + (c & 255)) / 10;
                    p[i] = l > 150 ? 0xFF141A3C : 0xFFB8C4E0;   // viền tối cũ -> viền nhạt cho chữ đậm
                }
                this.anhToi = Image.createRGBImage(p, w, h, true);
            } catch (Throwable e) {
                this.anhToi = null;
            }
        }
        return this.anhToi;
    }

    private void veChuoi(Graphics g, String st, int x, int y, int align) {
        st = ChuPokemon.D(st);
        int x1;
        int len = st.length();
        if (align == 0) {
            x1 = x;
        } else if (align == 1) {
            x1 = x - getWidthNguyenVan(st);
        } else {
            x1 = x - (getWidthNguyenVan(st) >> 1);
        }
        for (int i = 0; i < len; i++) {
            char c = st.charAt(i);
            int pos = this.charList.indexOf(c);
            if (pos < 0) {
                // chữ thiếu trong ảnh font: chữ hoa có dấu ghép tại chỗ, còn lại thay chữ gần nhất
                Image gh = ghep(c);
                if (gh != null) {
                    g.drawImage(gh, x1, y, 20);
                    x1 += gh.getWidth() + this.charSpace;
                    continue;
                }
                pos = thay(c);
            }
            try {
                g.drawRegion(this.imgFont, 0, pos * this.charHeight, this.imgFont.getWidth(), this.charHeight, 0, x1, y, 20);
            } catch (Throwable e) {
                // chữ hỏng không được làm hỏng cả khung hình
            }
            x1 += this.charWidth[pos] + this.charSpace;
        }
    }

    /** Do rong theo chu SAU khi doi (khop chu drawString ve ra). */
    public int getWidth(String st) {
        return getWidthNguyenVan(ChuPokemon.D(st));
    }

    /** Do rong DUNG chu dua vao, khong qua ChuPokemon (TField, ngat dong da doi san). */
    public int getWidthNguyenVan(String st) {
        if (st == null) {
            return 0;
        }
        int len = 0;
        for (int i = 0; i < st.length(); i++) {
            char c = st.charAt(i);
            int pos = this.charList.indexOf(c);
            if (pos < 0) {
                Image gh = ghep(c);
                if (gh != null) {
                    len += gh.getWidth() + this.charSpace;
                    continue;
                }
                pos = thay(c);
            }
            len += this.charWidth[pos] + this.charSpace;
        }
        return len;
    }

    // ------------------------------------------------------------------ chữ tiếng Việt thiếu trong font
    /** Chữ thường tiếng Việt có dấu và chữ gốc không dấu tương ứng (cùng thứ tự). */
    private static final String VIET = "àáảãạăằắẳẵặâầấẩẫậèéẻẽẹêềếểễệìíỉĩịòóỏõọôồốổỗộơờớởỡợùúủũụưừứửữựỳýỷỹỵđ";
    private static final String GOC = "aaaaaaaaaaaaaaaaaeeeeeeeeeeeiiiiiooooooooooooooooouuuuuuuuuuuyyyyyd";
    private static final Object KHONG_GHEP = new Object();
    /** chữ -&gt; Image ghép (hoặc KHONG_GHEP); tạo khi dùng lần đầu, mỗi chữ ~11x13 px. */
    private java.util.Hashtable boGhep;

    private static char boDau(char c) {
        char t = ChuPokemon.thuong(c);
        int i = VIET.indexOf(t);
        if (i < 0) {
            return c;
        }
        char b = GOC.charAt(i);
        return t == c ? b : ChuPokemon.hoa(b);
    }

    /**
     * Chữ thay khi font không có (không bao giờ bỏ chữ im lặng): đúng chữ khác hoa/thường (giữ dấu)
     * -&gt; bỏ dấu -&gt; bỏ dấu khác hoa/thường -&gt; '?' (ký tự ASCII điều khiển/thiếu: khoảng trắng như cũ).
     */
    /** Ký tự ASCII hay thiếu trong font ảnh -> ký tự gần giống (cặp liền nhau). */
    private static final String THAY_ASCII = "{(" + "})" + "[(" + "])" + "|/" + "`'" + "^-" + "~-" + "\\/" + "_-" + ";:" + "\"'" + "<(" + ">)" + "&+" + "=-" + "@a";

    /** Dấu câu Unicode font ảnh không có -> ký tự ASCII gần giống (0.9.7: không bao giờ ra '?'). */
    private static final String THAY_DAU = "\u00b7-\u203a>\u2039<\u2026.\u00d7x\u2013-\u2014-\u2022-\u201c\"\u201d\"\u2018'\u2019'\u2192>\u2190<";

    private int thay(char c) {
        if (c <= ' ') {
            return 0;                    // khoảng trắng / ký tự điều khiển
        }
        for (int k = 0; k + 1 < THAY_DAU.length(); k += 2) {
            if (THAY_DAU.charAt(k) == c) {
                char t = THAY_DAU.charAt(k + 1);
                int p = this.charList.indexOf(t);
                return p >= 0 ? p : thay(t);
            }
        }
        if (c < 0x80 && !(c >= 'a' && c <= 'z' || c >= 'A' && c <= 'Z' || c >= '0' && c <= '9')) {
            for (int k = 0; k + 1 < THAY_ASCII.length(); k += 2) {
                if (THAY_ASCII.charAt(k) == c) {
                    int p = this.charList.indexOf(THAY_ASCII.charAt(k + 1));
                    if (p >= 0) {
                        return p;
                    }
                    break;
                }
            }
            int q = this.charList.indexOf('?');
            if (c != '?') {
                thieuNet++;
                thieuCuoi = c;
            }
            return q >= 0 ? q : 0;
        }
        char t = ChuPokemon.thuong(c);
        char h = ChuPokemon.hoa(c);
        int pos = this.charList.indexOf(t != c ? t : h);
        if (pos >= 0) {
            return pos;
        }
        char b = boDau(c);
        pos = this.charList.indexOf(b);
        if (pos >= 0) {
            return pos;
        }
        pos = this.charList.indexOf(b == ChuPokemon.thuong(b) ? ChuPokemon.hoa(b) : ChuPokemon.thuong(b));
        if (pos >= 0) {
            return pos;
        }
        pos = this.charList.indexOf('?');
        thieuNet++;   // 0.9.7: ký tự font ảnh không có (vẽ thành '?') - kiểm thử đếm
        thieuCuoi = c;
        return pos >= 0 ? pos : 0;
    }

    /** số lần vẽ ký tự font ảnh không có (kiểm thử chữ) + ký tự gần nhất */
    public static int thieuNet;
    public static char thieuCuoi;

    private int[] doc(int pos) {
        int w = this.imgFont.getWidth();
        int[] p = new int[w * this.charHeight];
        this.imgFont.getRGB(p, 0, w, 0, pos * this.charHeight, w, this.charHeight);
        return p;
    }

    /** Khung mực [trái, trên, phải, dưới] của ô chữ (null nếu trống). */
    private int[] khung(int[] p, int w) {
        int x0 = w, y0 = this.charHeight, x1 = -1, y1 = -1;
        for (int y = 0; y < this.charHeight; y++) {
            for (int x = 0; x < w; x++) {
                if ((p[y * w + x] >>> 24) != 0) {
                    if (x < x0) {
                        x0 = x;
                    }
                    if (x > x1) {
                        x1 = x;
                    }
                    if (y < y0) {
                        y0 = y;
                    }
                    if (y > y1) {
                        y1 = y;
                    }
                }
            }
        }
        return x1 < 0 ? null : new int[]{x0, y0, x1, y1};
    }

    /**
     * Chữ HOA có dấu font thiếu (Â, Í, Ấ, Ữ...): ghép chữ hoa gốc của chính font này + dấu lấy từ chữ
     * thường có dấu (nét khác giữa "ấ" và "a"): dấu trên dời lên sát đỉnh chữ hoa (thiếu chỗ thì hạ chữ
     * hoa 1 px), dấu dưới giữ, râu ơ/ư dời theo mép phải - đúng nét pixel của font.
     */
    private Image ghep(char c) {
        if (c < 0xC0 && KY_TU.indexOf(c) < 0 || this.imgFont == null) {
            return null;
        }
        if (this.boGhep == null) {
            this.boGhep = new java.util.Hashtable();
        }
        Character k = new Character(c);
        Object o = this.boGhep.get(k);
        if (o != null) {
            return o == KHONG_GHEP ? null : (Image) o;
        }
        Image kq = null;
        try {
            kq = KY_TU.indexOf(c) >= 0 ? taoKyTu(c) : taoGhep(c);
        } catch (Throwable e) {
            kq = null;
        }
        this.boGhep.put(k, kq == null ? KHONG_GHEP : (Object) kq);
        return kq;
    }

    private Image taoGhep(char c) {
        char thuong = ChuPokemon.thuong(c);
        if (thuong == c) {
            return null;                      // chỉ ghép chữ hoa
        }
        char gocThuong = boDau(thuong);
        char gocHoa = ChuPokemon.hoa(gocThuong);
        int pThuong = this.charList.indexOf(thuong);
        int pGoc = this.charList.indexOf(gocThuong);
        int pHoa = this.charList.indexOf(gocHoa);
        if (gocThuong == thuong || pThuong < 0 || pGoc < 0 || pHoa < 0) {
            return null;
        }
        int w = this.imgFont.getWidth();
        int H = this.charHeight;
        int[] dau = doc(pThuong);
        int[] goc = doc(pGoc);
        int[] hoa = doc(pHoa);
        int[] kGoc = khung(goc, w);
        int[] kHoa = khung(hoa, w);
        if (kGoc == null || kHoa == null) {
            return null;
        }
        // chiều cao chữ thường (x-height) theo chữ "o": chữ "i" có chấm nên đỉnh mực của nó cao hơn
        int pO = this.charList.indexOf('o');
        int[] kO = pO >= 0 ? khung(doc(pO), w) : null;
        if (kO != null) {
            kGoc = new int[]{kGoc[0], kO[1], kGoc[2], kO[3]};
        }
        // nét dấu = điểm của chữ thường có dấu khác chữ thường gốc (với "đ": cả nét gạch)
        int tren0 = H, tren1 = -1;
        boolean coDuoi = false;
        // có dấu trên thật (khác chữ gốc ở vùng trên x-height)? có thì lấy MỌI điểm vùng đó (dấu sắc của
        // "í" trùng chỗ chấm "i"); không thì vùng trên giữ như chữ gốc (chấm "ị" không thành dấu)
        boolean dauTren = false;
        for (int y = 0; y < kGoc[1] && !dauTren; y++) {
            for (int x = 0; x < w; x++) {
                int a = dau[y * w + x];
                if ((a >>> 24) != 0 && a != goc[y * w + x]) {
                    dauTren = true;
                    break;
                }
            }
        }
        for (int y = 0; y < H; y++) {
            for (int x = 0; x < w; x++) {
                int a = dau[y * w + x];
                if ((a >>> 24) != 0 && (y < kGoc[1] ? dauTren : a != goc[y * w + x])) {
                    if (y < kGoc[1]) {
                        if (y < tren0) {
                            tren0 = y;
                        }
                        if (y > tren1) {
                            tren1 = y;
                        }
                    } else if (y > kGoc[3]) {
                        coDuoi = true;
                    }
                }
            }
        }
        int dyTren = tren1 >= 0 ? (kHoa[1] - 1) - tren1 : 0;
        int haHoa = 0;
        if (tren1 >= 0 && tren0 + dyTren < 0) {
            int thieu = -(tren0 + dyTren);
            int choDuoi = H - 1 - kHoa[3] - (coDuoi ? 2 : 0);
            haHoa = thieu < 1 ? thieu : 1;
            if (haHoa > choDuoi) {
                haHoa = choDuoi < 0 ? 0 : choDuoi;
            }
            dyTren = -tren0;                  // phần còn thiếu: dấu chạm đỉnh chữ hoa
        }
        // dấu nặng (chấm dưới): lấy chấm của "ạ" (khác "a" dưới chân chữ) đặt GIỮA dưới chữ hoa - chữ "ỵ"
        // có đuôi y nên chấm của chính nó lệch / lẫn vào đuôi
        int[] chamDuoi = null;
        int xCham = 0;
        if (NANG.indexOf(thuong) >= 0) {
            int pA1 = this.charList.indexOf('ạ'), pA0 = this.charList.indexOf('a');
            if (pA1 >= 0 && pA0 >= 0) {
                int[] a1 = doc(pA1), a0 = doc(pA0);
                int[] kA = khung(a0, w);
                chamDuoi = new int[w * H];
                int x0c = w, x1c = -1;
                for (int y = kGoc[3] + 1; y < H; y++) {
                    for (int x = 0; x < w; x++) {
                        int a = a1[y * w + x];
                        if ((a >>> 24) != 0 && a != a0[y * w + x]) {
                            chamDuoi[y * w + x] = a;
                            x0c = x < x0c ? x : x0c;
                            x1c = x > x1c ? x : x1c;
                        }
                    }
                }
                if (x1c < 0 || kA == null) {
                    chamDuoi = null;
                } else {
                    xCham = (kHoa[0] + kHoa[2]) / 2 - (x0c + x1c) / 2;
                }
            }
        }
        int dxTren = ((kHoa[0] + kHoa[2]) - (kGoc[0] + kGoc[2])) / 2;
        int dxRau = kHoa[2] - kGoc[2];
        int dyRau = kHoa[1] - kGoc[1];
        int rong = w;
        int[] p = new int[rong * H];
        for (int y = 0; y < H; y++) {
            int yy = y - haHoa;
            if (yy < 0) {
                continue;
            }
            for (int x = 0; x < w; x++) {
                p[y * rong + x] = hoa[yy * w + x];
            }
        }
        int phai = kHoa[2];
        for (int y = 0; y < H; y++) {
            for (int x = 0; x < w; x++) {
                int a = dau[y * w + x];
                if ((a >>> 24) == 0 || (y < kGoc[1] ? !dauTren : a == goc[y * w + x])) {
                    continue;
                }
                int tx, ty;
                if (y < kGoc[1]) {
                    tx = x + dxTren;
                    ty = y + dyTren;
                } else if (y > kGoc[3]) {
                    if (chamDuoi != null) {
                        continue;              // chấm dưới vẽ riêng (giữa chữ hoa)
                    }
                    tx = x + dxTren;
                    ty = y + haHoa;
                } else {
                    tx = x + dxRau;            // râu ơ / ư (hoặc gạch đ) bám mép phải chữ hoa
                    ty = y + dyRau + haHoa;
                }
                if (tx >= 0 && tx < rong && ty >= 0 && ty < H) {
                    p[ty * rong + tx] = a;
                    if (tx > phai) {
                        phai = tx;
                    }
                }
            }
        }
        if (chamDuoi != null) {
            for (int y = 0; y < H; y++) {
                for (int x = 0; x < w; x++) {
                    int a = chamDuoi[y * w + x];
                    int tx = x + xCham, ty = y + haHoa;
                    if (a != 0 && tx >= 0 && tx < rong && ty >= 0 && ty < H) {
                        p[ty * rong + tx] = a;
                    }
                }
            }
        }
        int wHoa = this.charWidth[pHoa];
        int wMoi = phai + 2 > wHoa ? phai + 2 : wHoa;
        if (wMoi > rong) {
            wMoi = rong;
        }
        int[] q = new int[wMoi * H];
        for (int y = 0; y < H; y++) {
            for (int x = 0; x < wMoi; x++) {
                q[y * wMoi + x] = p[y * rong + x];
            }
        }
        return Image.createRGBImage(q, wMoi, H, true);
    }

    /** Chữ có dấu nặng (chấm dưới). */
    private static final String NANG = "ạặậẹệịọộợụựỵ";

    /** Ký tự font ảnh thiếu, vẽ theo mẫu (taoKyTu): ' " ; < > = @ [ ] _ & … – — → × ≥ ≤ ± “ ”. */
    private static final String KY_TU = "'\";<>=@[]_&\u2026\u2013\u2014\u2192\u00d7\u2265\u2264\u00b1\u201c\u201d";
    /**
     * Mẫu từng ký tự (cùng thứ tự KY_TU): 8 hàng cách nhau "/" - hàng 0 = đỉnh chữ hoa, hàng 6 = chân chữ,
     * hàng 7 = dưới chân (gạch dưới, đuôi chấm phẩy). '#' = nét. Dựng theo nét của chính font: độ dày = độ
     * cao nét "-", màu nét / màu viền lấy từ "-".
     */
    private static final String[] MAU_KY_TU = {
        "#/#//////",
        "#.#/#.#//////",
        "//#///#/#/#",
        "/...#/..#./.#../..#./...#//",
        "/#.../.#../..#./.#../#...//",
        "//####//####///",
        ".###./#...#/#.###/#.#.#/#.###/#..../.####/",
        "##/#./#./#./#./#./##/",
        "##/.#/.#/.#/.#/.#/##/",
        "///////####",
        ".##../#..#./#.#../.#.../#.#.#/#..#./.##.#/",
        "//////#.#.#/",
        "///####////",
        "///#######////",
        "/...#./....#./######/....#./...#..//",
        "/#...#/.#.#./..#../.#.#./#...#//",
        "#.../.#../..#./.#../#...//####/",
        "...#/..#./.#../..#./...#//####/",
        "/..#../#####/..#..//#####//",
        "#.#/#.#//////",
        "#.#/#.#//////",
    };

    private Image taoKyTu(char c) {
        int iMau = KY_TU.indexOf(c);
        int pGach = this.charList.indexOf('-');
        if (iMau < 0 || pGach < 0) {
            return null;
        }
        int w = this.imgFont.getWidth();
        int H = this.charHeight;
        int[] gach = doc(pGach);
        int[] kG = khung(gach, w);
        if (kG == null) {
            return null;
        }
        // màu nét = điểm giữa nét "-"; màu viền = màu khác trong "-" (font chữ viền), nếu có
        int giua = gach[((kG[1] + kG[3]) / 2) * w + (kG[0] + kG[2]) / 2];
        int vien = 0;
        int day = 0;
        for (int y = kG[1]; y <= kG[3]; y++) {
            int a = gach[y * w + (kG[0] + kG[2]) / 2];
            if (a == giua) {
                day++;
            } else if ((a >>> 24) != 0 && vien == 0) {
                vien = a;
            }
        }
        if (day < 1) {
            day = 1;
        }
        int k = day > 2 ? 2 : day;    // độ dày nét: 1 hoặc 2 px
        // đỉnh chữ hoa + chân chữ: theo "A" (không có thì "0") và "o" (không có thì "0")
        int pTren = this.charList.indexOf('A');
        if (pTren < 0) {
            pTren = this.charList.indexOf('0');
        }
        int pChan = this.charList.indexOf('o');
        if (pChan < 0) {
            pChan = pTren;
        }
        int[] kT = pTren >= 0 ? khung(doc(pTren), w) : null;
        int[] kC = pChan >= 0 ? khung(doc(pChan), w) : null;
        int dinh = kT != null ? kT[1] : 1;
        int chan = kC != null ? kC[3] : H - 3;
        if (chan - dinh < 4) {
            dinh = chan - 6 < 0 ? 0 : chan - 6;
        }
        String[] hang = new String[8];
        String m = MAU_KY_TU[iMau];
        int r = 0, tu = 0;
        for (int i = 0; i <= m.length() && r < 8; i++) {
            if (i == m.length() || m.charAt(i) == '/') {
                hang[r++] = m.substring(tu, i);
                tu = i + 1;
            }
        }
        for (; r < 8; r++) {
            hang[r] = "";
        }
        int cot = 0;
        for (int i = 0; i < 8; i++) {
            cot = hang[i].length() > cot ? hang[i].length() : cot;
        }
        int le = vien != 0 ? 1 : 0;
        int rong = 1 + le + cot * k + le + 1;
        int[] p = new int[rong * H];
        for (int i = 0; i < 8; i++) {
            int y0, y1;
            if (i < 7) {
                y0 = dinh + i * (chan - dinh) / 6;
                y1 = i < 6 ? dinh + (i + 1) * (chan - dinh) / 6 : chan + 1;
                if (i == 6) {
                    y0 = chan - k + 1;
                }
                if (y1 - y0 > k && i != 6) {
                    y1 = y0 + k;                // nét không dày hơn "-" khi hàng giãn
                }
            } else {
                y0 = chan + 1 + le;
                y1 = y0 + k;
            }
            for (int x = 0; x < hang[i].length(); x++) {
                if (hang[i].charAt(x) != '#') {
                    continue;
                }
                for (int yy = y0; yy < y1 && yy < H; yy++) {
                    for (int xx = 0; xx < k; xx++) {
                        int px = 1 + le + x * k + xx;
                        if (yy >= 0 && px < rong) {
                            p[yy * rong + px] = giua;
                        }
                    }
                }
            }
        }
        if (vien != 0) {
            int[] q = new int[p.length];
            for (int y = 0; y < H; y++) {
                for (int x = 0; x < rong; x++) {
                    int i = y * rong + x;
                    if (p[i] != 0) {
                        q[i] = p[i];
                        continue;
                    }
                    boolean ke = false;
                    for (int dy = -1; dy <= 1 && !ke; dy++) {
                        for (int dx = -1; dx <= 1; dx++) {
                            int xx = x + dx, yy = y + dy;
                            if (xx >= 0 && yy >= 0 && xx < rong && yy < H && p[yy * rong + xx] != 0) {
                                ke = true;
                                break;
                            }
                        }
                    }
                    q[i] = ke ? vien : 0;
                }
            }
            p = q;
        }
        return Image.createRGBImage(p, rong, H, true);
    }

    public String[] splitFontBStrInLine(String src, int lineWidth) {
        // pokemon: ngat dong theo chu SAU khi doi (chu doi co the dai/ngan hon)
        src = ChuPokemon.D(src);
        Vector list = new Vector();
        int srclen = src.length();
        if (srclen <= 1) {
            return new String[]{src};
        }
        String tem = "";
        int start = 0;
        int end = 0;
        src.replace('\t', ' ');
        while (true) {
            if (getWidthNguyenVan(tem) > lineWidth) {
                if (end > 0) {
                    end--;
                }
            } else {
                tem = new StringBuffer(String.valueOf(tem)).append(src.charAt(end)).toString();
                end++;
                if (src.charAt(end) != '\n') {
                    if (end >= srclen - 1) {
                        end = srclen - 1;
                    } else {
                        continue;
                    }
                }
            }
            if (end != srclen - 1 && src.charAt(end + 1) != ' ') {
                int endAnyway = end;
                while (src.charAt(end + 1) != '\n' && ((src.charAt(end + 1) != ' ' || src.charAt(end) == ' ') && end != start)) {
                    end--;
                }
                if (end == start) {
                    end = endAnyway;
                }
            }
            list.addElement(src.substring(start, end + 1));
            if (end == srclen - 1) {
                break;
            }
            start = end + 1;
            while (start != srclen - 1 && src.charAt(start) == ' ') {
                start++;
            }
            if (start == srclen - 1) {
                break;
            }
            end = start;
            tem = "";
        }
        String[] strs = new String[list.size()];
        for (int i = 0; i < list.size(); i++) {
            strs[i] = (String) list.elementAt(i);
            while (strs[i].length() > 0 && (strs[i].charAt(0) == '\n' || strs[i].charAt(0) == '\r')) {
                strs[i] = strs[i].substring(1);
            }
            while (strs[i].length() > 0 && (strs[i].charAt(strs[i].length() - 1) == '\n' || strs[i].charAt(strs[i].length() - 1) == '\r')) {
                strs[i] = strs[i].substring(0, strs[i].length() - 1);
            }
        }
        return strs;
    }

    public static String[] splitString(String _text, String _searchStr) {
        int count = 0;
        int searchStringLength = _searchStr.length();
        int aa = _text.indexOf(_searchStr, 0);
        while (aa != -1) {
            int pos = aa + searchStringLength;
            aa = _text.indexOf(_searchStr, pos);
            count++;
        }
        String[] sb = new String[count + 1];
        int searchStringPos = _text.indexOf(_searchStr);
        int startPos = 0;
        int index = 0;
        while (searchStringPos != -1) {
            sb[index] = _text.substring(startPos, searchStringPos);
            startPos = searchStringPos + searchStringLength;
            searchStringPos = _text.indexOf(_searchStr, startPos);
            index++;
        }
        sb[index] = _text.substring(startPos, _text.length());
        return sb;
    }

    public static String replace(String _text, String _searchStr, String _replacementStr) {
        StringBuffer sb = new StringBuffer();
        int searchStringPos = _text.indexOf(_searchStr);
        int startPos = 0;
        int searchStringLength = _searchStr.length();
        while (searchStringPos != -1) {
            sb.append(_text.substring(startPos, searchStringPos)).append(_replacementStr);
            startPos = searchStringPos + searchStringLength;
            searchStringPos = _text.indexOf(_searchStr, startPos);
        }
        sb.append(_text.substring(startPos, _text.length()));
        return sb.toString();
    }

    public int getHeight() {
        return this.charHeight;
    }
}
