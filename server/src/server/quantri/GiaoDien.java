package server.quantri;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.awt.RenderingHints;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JViewport;
import javax.swing.UIManager;
import javax.swing.text.JTextComponent;

/**
 * GIAO DIEN bang dieu khien may chu (AdminPanel) - bang mau Pokemon, phong chu, ti le HiDPI,
 * che do sang / toi. Khong thu vien ngoai, Java 8.
 *
 * Moi thanh phan tu ve (ThanhPhan, BieuDo) doc mau o day LUC VE, nen doi sang/toi chi can goi
 * {@link #datToi(boolean)} roi repaint. Thanh phan Swing chuan (bang, o nhap) duoc to lai qua
 * {@link #toCay(Component)}.
 *
 * Lop THUAN ve mat logic: khong goi Toolkit/man hinh truc tiep tru trong {@link #khoiTao()}
 * (bao try) nen dung duoc ca khi headless (kiem thu).
 */
public final class GiaoDien {

    // ------------------------------------------------------------ bang mau thuong hieu
    public static final Color DO = new Color(0xE3350D);
    public static final Color DO_DAM = new Color(0xB3270A);
    public static final Color VANG = new Color(0xFFCB05);
    public static final Color VANG_DAM = new Color(0xC79A00);
    public static final Color XANH_DUONG = new Color(0x3B4CCA);
    public static final Color XANH_DUONG_DAM = new Color(0x2A3591);
    public static final Color XANH_LA = new Color(0x4CAF50);
    public static final Color XANH_LA_DAM = new Color(0x2E7D32);
    public static final Color XANH_LA_NHAT = new Color(0x8BC34A);
    public static final Color TIM = new Color(0x8E5CC4);
    public static final Color CAM = new Color(0xF2832B);
    public static final Color XANH_NUOC = new Color(0x2CA6C9);

    /** Mau trang thai: xanh = tot, vang = can xem, do = nguy. */
    public static final int TOT = 0, CHU_Y = 1, NGUY = 2, TRUNG_TINH = 3;

    // ------------------------------------------------------------ mau theo che do (doi khi sang/toi)
    public static volatile boolean toi = false;
    public static Color nen, nenThe, nenThe2, nenO, vien, vienDam, chu, chuPhu, chuMo, bong, nenHover;
    public static Color benTren, benDuoi, chuBen, chuBenMo, chonBen;
    public static Color dongLe, chonDong;

    /** Ti le giao dien (1 = 96 dpi). Java 9+ tu nhan doi diem anh khi he dieu hanh bat HiDPI -> giu 1. */
    public static float tiLe = 1f;

    private static Font goc = new Font(Font.SANS_SERIF, Font.PLAIN, 13);
    private static Font gocDam = null;
    private static Font gocMono = new Font(Font.MONOSPACED, Font.PLAIN, 12);
    private static String tenFont = Font.SANS_SERIF;
    private static boolean daKhoiTao;

    private static final List<Runnable> NGHE = new CopyOnWriteArrayList<Runnable>();

    /** Phong chu uu tien (giong Nunito: tron, de doc, du dau tieng Viet). */
    static final String[] UU_TIEN = {
        "Nunito", "Nunito Sans", "Quicksand", "Varela Round", "M PLUS Rounded 1c", "Segoe UI", "Be Vietnam Pro",
        "Fira Sans", "Ubuntu", "Noto Sans", "Open Sans", "Roboto", "DejaVu Sans", "Arial", "Tahoma"
    };

    static final String MAU_TIENG_VIET = "Quản Lý Máy Chủ \u1EA0\u1EA2\u1EA4\u1EA6\u1EA8\u1EAA\u1EAC\u1EAE\u1EB0 ạảấầẩẫậđĐ";

    static {
        datToi(false);
    }

    private GiaoDien() {
    }

    // ================================================================== khoi tao

    /** Goi mot lan truoc khi dung giao dien (an toan khi headless: chi co phan tinh ti le bi bo qua). */
    public static synchronized void khoiTao() {
        if (daKhoiTao) {
            return;
        }
        daKhoiTao = true;
        tiLe = tinhTiLe();
        tenFont = chonFont();
        goc = new Font(tenFont, Font.PLAIN, 13);
        gocDam = null;
        try {
            for (final String ten : new String[]{"JetBrains Mono", "Cascadia Mono", "Consolas", "DejaVu Sans Mono", "Noto Sans Mono",
                "Liberation Mono", "Ubuntu Mono", "Fira Mono"}) {
                if (Arrays_contains(dsFont(), ten) && new Font(ten, Font.PLAIN, 12).canDisplayUpTo(MAU_TIENG_VIET) == -1) {
                    gocMono = new Font(ten, Font.PLAIN, 12);
                    break;
                }
            }
        } catch (final Throwable ignored) {
        }
        caiUIManager();
    }

    private static boolean Arrays_contains(final Set<String> s, final String ten) {
        return s.contains(ten.toLowerCase(Locale.ROOT));
    }

    private static Set<String> dsFontCache;

    static synchronized Set<String> dsFont() {
        if (dsFontCache == null) {
            final Set<String> s = new HashSet<String>();
            try {
                for (final String f : GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames()) {
                    s.add(f.toLowerCase(Locale.ROOT));
                }
            } catch (final Throwable ignored) {
            }
            dsFontCache = s;
        }
        return dsFontCache;
    }

    /** Ten phong chu dau tien trong UU_TIEN co tren may VA ve duoc tieng Viet. */
    static String chonFont() {
        final Set<String> co = dsFont();
        for (final String ten : UU_TIEN) {
            if (co.contains(ten.toLowerCase(Locale.ROOT))) {
                try {
                    final Font f = new Font(ten, Font.PLAIN, 13);
                    if (f.canDisplayUpTo(MAU_TIENG_VIET) == -1) {
                        return ten;
                    }
                } catch (final Throwable ignored) {
                }
            }
        }
        return Font.SANS_SERIF;
    }

    public static String tenFont() {
        return tenFont;
    }

    static float tinhTiLe() {
        try {
            final String ep = System.getProperty("htbb.panel.tiLe", System.getenv("HTBB_PANEL_TILE"));
            if (ep != null && !ep.trim().isEmpty()) {
                return kep(Float.parseFloat(ep.trim()), 0.75f, 3f);
            }
        } catch (final Throwable ignored) {
        }
        if (GraphicsEnvironment.isHeadless()) {
            return 1f;
        }
        try {
            final double hs = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice()
                    .getDefaultConfiguration().getDefaultTransform().getScaleX();
            if (hs > 1.01) {
                return 1f; // JVM (9+) da tu phong to theo he dieu hanh
            }
            final int dpi = java.awt.Toolkit.getDefaultToolkit().getScreenResolution();
            return kep(Math.round(dpi / 96f * 4f) / 4f, 1f, 3f);
        } catch (final Throwable t) {
            return 1f;
        }
    }

    static float kep(final float v, final float a, final float b) {
        return Math.max(a, Math.min(b, v));
    }

    // ================================================================== mau

    /** Doi che do sang / toi va bao cho moi nguoi nghe (repaint, to lai cay). */
    public static void datToi(final boolean coToi) {
        toi = coToi;
        if (!coToi) {
            nen = new Color(0xF3F5FA);
            nenThe = Color.WHITE;
            nenThe2 = new Color(0xF7F8FC);
            nenO = Color.WHITE;
            vien = new Color(0xE2E6EF);
            vienDam = new Color(0xC7CEDC);
            chu = new Color(0x1D2333);
            chuPhu = new Color(0x5A6377);
            chuMo = new Color(0x8D95A8);
            bong = new Color(20, 30, 70, 22);
            nenHover = new Color(0xEEF1FA);
            benTren = new Color(0x3B4CCA);
            benDuoi = new Color(0x26318A);
            chuBen = Color.WHITE;
            chuBenMo = new Color(0xC9D0F5);
            chonBen = new Color(255, 255, 255, 38);
            dongLe = new Color(0xF8F9FD);
            chonDong = new Color(0xFFF3C4);
        } else {
            nen = new Color(0x10141F);
            nenThe = new Color(0x1A2030);
            nenThe2 = new Color(0x202739);
            nenO = new Color(0x141927);
            vien = new Color(0x2B3349);
            vienDam = new Color(0x3C4664);
            chu = new Color(0xE8ECF5);
            chuPhu = new Color(0xA2ABC2);
            chuMo = new Color(0x6E7894);
            bong = new Color(0, 0, 0, 70);
            nenHover = new Color(0x252D42);
            benTren = new Color(0x1B2246);
            benDuoi = new Color(0x111630);
            chuBen = new Color(0xEEF1FB);
            chuBenMo = new Color(0x8E97BD);
            chonBen = new Color(255, 255, 255, 24);
            dongLe = new Color(0x1D2335);
            chonDong = new Color(0x4A4220);
        }
        if (daKhoiTao) {
            caiUIManager();
        }
        for (final Runnable r : NGHE) {
            try {
                r.run();
            } catch (final Throwable ignored) {
            }
        }
    }

    public static void ngheDoiMau(final Runnable r) {
        NGHE.add(r);
    }

    public static Color mauTrangThai(final int tt) {
        switch (tt) {
            case TOT:
                return XANH_LA;
            case CHU_Y:
                return toi ? VANG : new Color(0xE0A800);
            case NGUY:
                return DO;
            default:
                return XANH_DUONG;
        }
    }

    public static Color trong(final Color c, final int alpha) {
        return new Color(c.getRed(), c.getGreen(), c.getBlue(), Math.max(0, Math.min(255, alpha)));
    }

    /** Pha mau: t = 0 -> a, t = 1 -> b. */
    public static Color pha(final Color a, final Color b, final float t) {
        final float u = Math.max(0f, Math.min(1f, t));
        return new Color(
                Math.round(a.getRed() + (b.getRed() - a.getRed()) * u),
                Math.round(a.getGreen() + (b.getGreen() - a.getGreen()) * u),
                Math.round(a.getBlue() + (b.getBlue() - a.getBlue()) * u),
                Math.round(a.getAlpha() + (b.getAlpha() - a.getAlpha()) * u));
    }

    /** Chu tren nen mau nay nen trang hay toi (do sang cam nhan). */
    public static Color chuTren(final Color nenMau) {
        final double y = 0.299 * nenMau.getRed() + 0.587 * nenMau.getGreen() + 0.114 * nenMau.getBlue();
        return y > 170 ? new Color(0x1D2333) : Color.WHITE;
    }

    // ================================================================== kich thuoc / phong chu

    public static int s(final int px) {
        return Math.round(px * tiLe);
    }

    public static float sf(final float px) {
        return px * tiLe;
    }

    public static Font font(final float co) {
        return goc.deriveFont(Font.PLAIN, co * tiLe);
    }

    public static Font dam(final float co) {
        return goc.deriveFont(Font.BOLD, co * tiLe);
    }

    public static Font mono(final float co) {
        return gocMono.deriveFont(Font.PLAIN, co * tiLe);
    }

    public static void khuRang(final Graphics2D g) {
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);
        g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
        g.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
    }

    // ================================================================== UIManager + to lai cay

    static void caiUIManager() {
        try {
            UIManager.put("Panel.background", nen);
            UIManager.put("OptionPane.background", nenThe);
            UIManager.put("OptionPane.messageForeground", chu);
            UIManager.put("OptionPane.messageFont", font(13));
            UIManager.put("OptionPane.buttonFont", dam(12.5f));
            UIManager.put("Label.font", font(13));
            UIManager.put("Label.foreground", chu);
            UIManager.put("Button.font", dam(12.5f));
            UIManager.put("TextField.font", font(13));
            UIManager.put("TextArea.font", font(13));
            UIManager.put("ComboBox.font", font(13));
            UIManager.put("CheckBox.font", font(13));
            UIManager.put("RadioButton.font", font(13));
            UIManager.put("Table.font", font(12.5f));
            UIManager.put("TableHeader.font", dam(12));
            UIManager.put("ToolTip.font", font(12));
            UIManager.put("ToolTip.background", toi ? new Color(0x2A3248) : new Color(0x1D2333));
            UIManager.put("ToolTip.foreground", Color.WHITE);
            UIManager.put("ToolTip.border", javax.swing.BorderFactory.createEmptyBorder(s(5), s(8), s(5), s(8)));
            UIManager.put("Menu.font", font(13));
            UIManager.put("MenuItem.font", font(13));
            UIManager.put("PopupMenu.background", nenThe);
            UIManager.put("MenuItem.background", nenThe);
            UIManager.put("MenuItem.foreground", chu);
            UIManager.put("List.font", font(13));
            UIManager.put("Spinner.font", font(13));
            UIManager.put("TabbedPane.font", dam(12.5f));
            UIManager.put("RadioButton.icon", new IconChon(true));
            UIManager.put("CheckBox.icon", new IconChon(false));
        } catch (final Throwable ignored) {
        }
    }

    /** To lai mau nen/chu cho thanh phan Swing chuan trong cay (sau khi doi sang/toi). */
    public static void toCay(final Component c) {
        if (c == null) {
            return;
        }
        if (c instanceof JComponent && Boolean.TRUE.equals(((JComponent) c).getClientProperty("giuMau"))) {
            return;
        }
        if (c instanceof JTable) {
            final JTable t = (JTable) c;
            t.setBackground(nenThe);
            t.setForeground(chu);
            t.setSelectionBackground(chonDong);
            t.setSelectionForeground(chu);
            if (t.getTableHeader() != null) {
                t.getTableHeader().setBackground(nenThe2);
                t.getTableHeader().setForeground(chuPhu);
            }
        } else if (c instanceof JTextComponent && !(c instanceof JEditorPane && !((JEditorPane) c).isEditable())) {
            c.setBackground(c instanceof JTextArea && !((JTextArea) c).isEditable() ? nenThe2 : nenO);
            c.setForeground(chu);
            ((JTextComponent) c).setCaretColor(chu);
            ((JTextComponent) c).setSelectionColor(trong(XANH_DUONG, 90));
            ((JTextComponent) c).setSelectedTextColor(chu);
        } else if (c instanceof JList) {
            c.setBackground(nenO);
            c.setForeground(chu);
        } else if (c instanceof JViewport) {
            final Component v = ((JViewport) c).getView();
            if (v instanceof Trang.PanelCuon) {
                ((JViewport) c).setOpaque(false); // nen trang trong suot: thay mau nen cua cua so
            } else {
                c.setBackground(v instanceof JTable || v instanceof JList ? nenThe : nenThe2);
            }
        } else if (c instanceof JScrollPane) {
            if (!(((JScrollPane) c).getViewport().getView() instanceof Trang.PanelCuon)) {
                c.setBackground(nenThe);
            }
        } else if (c instanceof JSpinner) {
            final JSpinner sp = (JSpinner) c;
            sp.setBorder(new ThanhPhan.VienTron(s(8), false));
            sp.setFont(font(13));
            if (sp.getEditor() instanceof JSpinner.DefaultEditor) {
                final JTextField tf = ((JSpinner.DefaultEditor) sp.getEditor()).getTextField();
                tf.setBorder(javax.swing.BorderFactory.createEmptyBorder(s(4), s(8), s(4), s(4)));
                tf.setBackground(nenO);
                tf.setForeground(chu);
                tf.setFont(font(13));
            }
            if (sp.getPreferredSize().height < s(32)) {
                sp.setPreferredSize(new java.awt.Dimension(Math.max(sp.getPreferredSize().width, s(80)), s(32)));
            }
        } else if (c instanceof javax.swing.JComboBox) {
            ThanhPhan.kieuHopChon((javax.swing.JComboBox<?>) c);
            c.setBackground(nenO);
            c.setForeground(chu);
        } else if (c instanceof javax.swing.JLabel || c instanceof javax.swing.AbstractButton) {
            if (c instanceof JComponent && ((JComponent) c).getClientProperty("mauChu") != null) {
                // nhan co mau rieng (vd chu phu): doc lai theo ten
                final Object k = ((JComponent) c).getClientProperty("mauChu");
                c.setForeground("phu".equals(k) ? chuPhu : "mo".equals(k) ? chuMo : chu);
            } else if (!(c instanceof ThanhPhan.Nut)) {
                c.setForeground(chu);
            }
        }
        if (c instanceof Container) {
            for (final Component con : ((Container) c).getComponents()) {
                toCay(con);
            }
        }
        if (c instanceof JComponent) {
            final javax.swing.JPopupMenu pm = ((JComponent) c).getComponentPopupMenu();
            if (pm != null) {
                toCay(pm);
            }
        }
    }

    /** Icon o chon / nut radio tu ve theo ti le (Metal mac dinh 13px co dinh, qua nho khi HiDPI). */
    static final class IconChon implements javax.swing.Icon {
        private final boolean tron;

        IconChon(final boolean tron) {
            this.tron = tron;
        }

        @Override
        public void paintIcon(final Component c, final java.awt.Graphics g0, final int x, final int y) {
            final Graphics2D g = (Graphics2D) g0.create();
            khuRang(g);
            final javax.swing.AbstractButton b = (javax.swing.AbstractButton) c;
            final boolean chon = b.isSelected();
            final int d = s(15);
            final java.awt.Shape khung = tron ? new java.awt.geom.Ellipse2D.Float(x + 0.5f, y + 0.5f, d - 1, d - 1)
                    : new java.awt.geom.RoundRectangle2D.Float(x + 0.5f, y + 0.5f, d - 1, d - 1, s(5), s(5));
            g.setColor(chon ? XANH_DUONG : nenO);
            g.fill(khung);
            g.setColor(chon ? XANH_DUONG_DAM : vienDam);
            g.draw(khung);
            if (chon) {
                g.setColor(Color.WHITE);
                if (tron) {
                    g.fill(new java.awt.geom.Ellipse2D.Float(x + d * 0.32f, y + d * 0.32f, d * 0.36f, d * 0.36f));
                } else {
                    g.setStroke(new java.awt.BasicStroke(sf(2f), java.awt.BasicStroke.CAP_ROUND, java.awt.BasicStroke.JOIN_ROUND));
                    final java.awt.geom.Path2D.Float p = new java.awt.geom.Path2D.Float();
                    p.moveTo(x + d * 0.25f, y + d * 0.52f);
                    p.lineTo(x + d * 0.43f, y + d * 0.70f);
                    p.lineTo(x + d * 0.76f, y + d * 0.32f);
                    g.draw(p);
                }
            }
            g.dispose();
        }

        @Override
        public int getIconWidth() {
            return s(15);
        }

        @Override
        public int getIconHeight() {
            return s(15);
        }
    }

    /** Danh sach phong chu da thu (cho bao cao / kiem thu). */
    public static List<String> phongChuUuTien() {
        final List<String> l = new ArrayList<String>();
        for (final String s : UU_TIEN) {
            l.add(s);
        }
        return l;
    }

    /** JTextField nho gon dung chung. */
    public static JTextField oNhap(final int cot) {
        final JTextField f = new ThanhPhan.ONhap("", cot);
        return f;
    }
}
