package game.pokemon;

import game.model.GemTemp;
import game.model.GemTemplate;
import game.model.IAction;
import game.model.ItemInInventory;
import game.model.ItemTemplate;
import game.model.MainChar;
import game.render.Canvas;
import game.render.Res;
import java.util.Vector;
import javax.microedition.lcdui.Graphics;

/**
 * HÀNG PHẦN THƯỞNG kiểu NRO (0.9.7): máy chủ gửi phần thưởng dạng CHỮ ("500 xu, 120 điểm kinh nghiệm, 2 Thuốc hồi HP")
 * ở nhiệm vụ, quà / thư, giftcode, lên cấp, tiến hoá, rương boss... Lớp này tách chữ thành các MỤC và vẽ mỗi mục là một
 * Ô hộp gỗ (icon thật + số lượng): vật phẩm nhận ra được (bình thuốc theo tên, nguyên liệu theo tên, trang bị theo tên)
 * chạm = THẺ VẬT PHẨM chỉ xem (TheVatPham.moXem: icon, tên màu độ hiếm, chỉ số, mô tả, X); xu / lượng / EXP = ô biểu
 * tượng tiền (chạm = chú thích TienTe). Mục không nhận ra: không vẽ ô (chữ gốc vẫn hiện ở trên).
 */
public final class PhanThuong {
    private PhanThuong() {
    }

    /** Một mục: o = Integer bình / GemTemplate / ItemInInventory / Integer âm (-1 - loại tiền: XU, LUONG..., -100 EXP). */
    static final class Muc {
        Object o;
        long so;
        String ten;
    }

    private static String khoa;
    private static Vector dem = new Vector();

    /** Tách chữ phần thưởng thành các mục nhận ra được (nhớ lần gần nhất). */
    public static Vector tach(String s) {
        if (s == null) {
            return new Vector();
        }
        if (s.equals(khoa)) {
            return dem;
        }
        Vector v = new Vector();
        String t = s;
        String th = ChuPokemon.thuong(t);
        int pt = cuoi(th, "thưởng");
        if (pt < 0) {
            pt = cuoi(th, "nhận được");
            if (pt >= 0) {
                pt += 3;
            }
        }
        if (pt < 0 && th.indexOf(':') >= 0) {
            // "Hoàn thành nhiệm vụ X: +500 xu, ..." / "Đổi giftcode thành công: ..."
            t = t.substring(th.indexOf(':') + 1);
        } else if (pt >= 0) {
            int hai = th.indexOf(':', pt);
            t = hai >= 0 && hai <= pt + 14 ? t.substring(hai + 1) : t.substring(pt + 6);
        }
        String[] phan = tachNhieu(t);
        for (int i = 0; i < phan.length && v.size() < 8; i++) {
            Muc m = nhan(phan[i].trim());
            if (m != null) {
                v.addElement(m);
            }
        }
        khoa = s;
        dem = v;
        return v;
    }

    static String[] tachNhieu(String s) {
        Vector v = new Vector();
        StringBuffer b = new StringBuffer();
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            boolean ngat = c == ',' || c == ';' || c == '|' || c == '\n' || c == '.' && (i + 1 >= s.length() || s.charAt(i + 1) == ' ')
                    || c == '+' && b.toString().trim().length() > 0;
            if (!ngat && c == ' ' && s.startsWith(" và ", i)) {
                ngat = true;
                i += 3;
            }
            if (ngat) {
                v.addElement(b.toString());
                b.setLength(0);
            } else {
                b.append(c);
            }
        }
        v.addElement(b.toString());
        String[] r = new String[v.size()];
        v.copyInto(r);
        return r;
    }

    /** Một mục chữ ("+2 Thuốc hồi HP", "500 xu", "Khăn quàng x1") -> Muc hoặc null. */
    static Muc nhan(String p) {
        if (p.length() == 0) {
            return null;
        }
        if (p.charAt(0) == '+') {
            p = p.substring(1).trim();
        }
        long so = 1;
        String ten = p;
        // số đầu: "2 Thuốc", "5.000 xu"
        int i = 0;
        while (i < p.length() && (Character.isDigit(p.charAt(i)) || p.charAt(i) == '.')) {
            i++;
        }
        if (i == 0) {
            // "Bạn được 500 xu" -> từ số đầu tiên đứng sau dấu cách
            for (int j = 1; j < p.length() - 1; j++) {
                if ((p.charAt(j - 1) == ' ' || p.charAt(j - 1) == '+') && Character.isDigit(p.charAt(j))) {
                    p = p.substring(j);
                    break;
                }
            }
            while (i < p.length() && (Character.isDigit(p.charAt(i)) || p.charAt(i) == '.')) {
                i++;
            }
        }
        if (i > 0 && i < p.length() && (p.charAt(i) == ' ' || Character.isUpperCase(p.charAt(i)))) {
            so = soCua(p.substring(0, i));
            ten = p.substring(i).trim();
        } else {
            // số cuối: "Khăn quàng x2", "Ether ×3"
            int x = Math.max(cuoi(p, " x"), cuoi(p, " ×"));
            if (x > 0) {
                long n = soCua(p.substring(x + 2).trim());
                if (n > 0) {
                    so = n;
                    ten = p.substring(0, x).trim();
                }
            }
        }
        String th = ChuPokemon.thuong(ten);
        Muc m = new Muc();
        m.so = so;
        m.ten = ten;
        if (th.equals("xu")) {
            m.o = new Integer(-1 - TienTe.XU);
            return m;
        }
        if (th.startsWith("lượng khoá") || th.startsWith("lượng khóa")) {
            m.o = new Integer(-1 - TienTe.LUONG_KHOA);
            return m;
        }
        if (th.equals("lượng")) {
            m.o = new Integer(-1 - TienTe.LUONG);
            return m;
        }
        if (th.indexOf("kinh nghiệm") >= 0 || th.equals("exp")) {
            m.o = new Integer(-100);
            return m;
        }
        // bình thuốc theo tên
        try {
            for (int k = 0; MainChar.listPotion != null && k < MainChar.listPotion.length; k++) {
                if (MainChar.listPotion[k] != null && MainChar.listPotion[k].name != null
                        && ChuPokemon.thuong(MainChar.listPotion[k].name).equals(th)) {
                    m.o = new Integer(k);
                    return m;
                }
            }
        } catch (Throwable e) {
            // bảng bình chưa nạp
        }
        // nguyên liệu theo tên
        for (int k = 0; k < Res.gemTemplate.size(); k++) {
            GemTemp g = (GemTemp) Res.gemTemplate.elementAt(k);
            if (g.name != null && ChuPokemon.thuong(g.name).equals(th)) {
                GemTemplate gt = new GemTemplate();
                gt.id = g.id;
                gt.number = (int) Math.min(Integer.MAX_VALUE, so);
                m.o = gt;
                return m;
            }
        }
        // trang bị theo tên mẫu
        try {
            ItemTemplate[] ds = (ItemTemplate[]) Res.itemTemplates.elementAt(0);
            for (int k = 0; k < ds.length; k++) {
                if (ds[k] != null && ds[k].name != null && ChuPokemon.thuong(ds[k].name).equals(th)) {
                    ItemInInventory it = new ItemInInventory();
                    it.idTemplate = ds[k].id;
                    it.charClazz = Canvas.gameScr != null && Canvas.gameScr.mainChar != null ? Canvas.gameScr.mainChar.clazz : 0;
                    it.isEnoughData = true;
                    m.o = it;
                    return m;
                }
            }
        } catch (Throwable e) {
            // bảng vật phẩm chưa nạp
        }
        return null;
    }

    static long soCua(String s) {
        long n = 0;
        boolean co = false;
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c >= '0' && c <= '9') {
                n = n * 10 + (c - '0');
                co = true;
            } else if (c != '.') {
                return co ? n : -1;
            }
        }
        return co ? n : -1;
    }

    /** Cạnh ô phần thưởng (>= cỡ chạm). */
    public static int coO() {
        return Math.max(Bang.co(28), ChuDe.dt() ? 34 : 30);
    }

    /** Cao hàng (0 = không có mục). */
    public static int cao(String s, int w) {
        Vector v = tach(s);
        if (v.size() == 0) {
            return 0;
        }
        int o = coO(), k = Bang.cach();
        int cot = Math.max(1, (w + k) / (o + k));
        int hang = (v.size() + cot - 1) / cot;
        return hang * (o + k) - k;
    }

    /**
     * Vẽ hàng ô phần thưởng trong (x, y, w), canh giữa. Chạm ô vật phẩm = thẻ chỉ xem; khiDong (có thể null) chạy khi
     * thẻ đóng (hộp gọi mở lại chính nó). Trả cao đã dùng.
     */
    public static int ve(Graphics g, int x, int y, int w, String s, final IAction khiDong) {
        Vector v = tach(s);
        if (v.size() == 0) {
            return 0;
        }
        int o = coO(), k = Bang.cach();
        int cot = Math.max(1, Math.min(v.size(), (w + k) / (o + k)));
        int hang = (v.size() + cot - 1) / cot;
        for (int i = 0; i < v.size(); i++) {
            final Muc m = (Muc) v.elementAt(i);
            int c = i % cot, r = i / cot;
            int soCot = r == hang - 1 ? v.size() - r * cot : cot;
            int x0 = x + (w - (soCot * (o + k) - k)) / 2 + c * (o + k);
            int y0 = y + r * (o + k);
            if (m.o instanceof Integer && ((Integer) m.o).intValue() < 0) {
                final int loai = -1 - ((Integer) m.o).intValue();
                Bang.oDo(g, x0, y0, o, true, false, null, null);
                // 0.9.7: biểu tượng TRÊN, số DƯỚI (không chữ đè biểu tượng) - ô >= 30 px
                int hs = ChuDe.cao(ChuDe.NHAN_NHO);
                int co = Math.max(8, Math.min(16, o - hs - 7));
                if (loai >= 0 && loai < TienTe.SO_LOAI) {
                    TienTe.ve(g, loai, x0 + (o - co) / 2, y0 + 3, co);
                    TienTe.dangKyGoiY(g, loai, m.so, x0, y0, o, o);
                } else {
                    BieuTuong.ve(g, BieuTuong.SU_KIEN, x0 + (o - co) / 2, y0 + 3, co, ChuDe.VANG, 0);
                }
                ChuDe.chuVienGiua(g, KieuO.soGon((int) Math.min(Integer.MAX_VALUE, m.so)), x0 + o / 2, y0 + o - 3 - hs, hs, ChuDe.NHAN_NHO,
                        0xFFFFFF, ChuDe.VIEN, 2);
                continue;
            }
            LuoiTui.boSo = true;
            LuoiTui.veO(g, m.o, x0, y0, o, false, new IAction() {
                public void perform() {
                    TheVatPham.moXem(m.o, "Phần thưởng: " + m.ten + (m.so > 1 ? " x" + m.so : ""), khiDong);
                }
            }, "phan-thuong-" + i);
            LuoiTui.boSo = false;
            if (m.so > 1 && !(m.o instanceof GemTemplate)) {
                LuoiTui.soLuong(g, x0, y0, o, (int) Math.min(Integer.MAX_VALUE, m.so));
            }
        }
        return hang * (o + k) - k;
    }

    /** String.lastIndexOf(String) - CLDC 1.1 chi co lastIndexOf(char) (Down45 kiem API). */
    static int cuoi(String s, String t) {
        int kq = -1;
        int i = s.indexOf(t);
        while (i >= 0) {
            kq = i;
            i = s.indexOf(t, i + 1);
        }
        return kq;
    }
}
