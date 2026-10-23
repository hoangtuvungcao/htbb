package game.pokemon;

import game.model.AnimalInfo;
import game.model.Char;
import game.model.IAction;
import game.networklogic.GameService;
import network.Message;
import java.io.DataInputStream;
import java.util.Vector;

/**
 * QUẢN LÝ THÚ (thú cưỡi + Pokémon đi theo, chung một danh sách): ô thú có giới hạn, thả về tự nhiên lấy ít xu, khoá
 * con yêu thích, thả bản trùng, Vé mở ô thú. Máy chủ quyết định mọi thứ (real.QuanLyThuGame); client chỉ hiện và gửi.
 *
 * Gói 16 loại 2 (danh sách thú) có PHẦN NỐI THÊM sau "int luongKhoa" (máy chủ chỉ gửi cho client BAN >= 912):
 * byte phiên, short số thú, short sức chứa, short tối đa, short số vé, short id vé, int giá vé (lượng, -1 = không bán),
 * int xu thả còn nhận hôm nay,
 * byte n + n dòng (cùng thứ tự danh sách): byte cờ (1 khoá, 2 không thả được), int loài, short cấp, int xu, UTF lý do.
 * Máy chủ cũ không gửi phần này -> {@link #co} = false: danh sách như cũ, không nút mới.
 *
 * Lệnh -119 (C->S): byte kiểu 0 trạng thái / 1 thả (byte n, n x (byte loại 0 cưỡi / 1 đi theo, short id)) / 2 khoá
 * (byte loại, short id, byte khoá) / 3 dùng vé / 4 mua vé (Cửa hàng lượng).
 */
public final class QuanLyThu {
    public static final int LENH = -119;
    public static final int CO_KHOA = 1, CO_KHONG_THA = 2;

    /** máy chủ có gửi phần nối thêm */
    public static boolean co;
    public static int soThu, sucChua, toiDa, soVe, idVe = -1, giaVe = -1;
    /** xu thả còn nhận được hôm nay (trần mỗi ngày của máy chủ; 0 = đã đạt trần: thả vẫn được, 0 xu) */
    public static int xuConNgay = Integer.MAX_VALUE;
    static Vector ds = new Vector();
    static int[] coDong = new int[0], loai = new int[0], cap = new int[0], xu = new int[0];
    static String[] lyDo = new String[0];
    /** chống bấm hai lần: lệnh thả cuối, chờ máy chủ trả lời */
    static long luThaCuoi;

    private QuanLyThu() {
    }

    /** MessageHandler gói 16 loại 2: đọc phần nối thêm (nếu có) cho {@code animal} vừa đọc. */
    public static void doc(DataInputStream r, Vector animal) {
        try {
            if (r.available() <= 0) {
                co = false;
                return;
            }
            r.readByte();
            int st = r.readShort(), sc = r.readShort(), td = r.readShort(), sv = r.readShort(), iv = r.readShort();
            int gv = r.readInt();
            int xn = r.readInt();
            int n = r.readUnsignedByte();
            int[] c = new int[n], l = new int[n], cp = new int[n], x = new int[n];
            String[] ly = new String[n];
            for (int i = 0; i < n; i++) {
                c[i] = r.readByte();
                l[i] = r.readInt();
                cp[i] = r.readShort();
                x[i] = r.readInt();
                ly[i] = r.readUTF();
            }
            Vector v = new Vector();
            for (int i = 0; i < animal.size() && i < n; i++) {
                v.addElement(animal.elementAt(i));
            }
            soThu = st;
            sucChua = sc;
            toiDa = td;
            soVe = sv;
            idVe = iv;
            giaVe = gv;
            xuConNgay = Math.max(0, xn);
            coDong = c;
            loai = l;
            cap = cp;
            xu = x;
            lyDo = ly;
            ds = v;
            co = true;
            luThaCuoi = 0;
        } catch (Throwable e) {
            co = false;
        }
    }

    static int viTri(AnimalInfo a) {
        for (int i = 0; i < ds.size(); i++) {
            if (ds.elementAt(i) == a) {
                return i;
            }
        }
        return -1;
    }

    public static boolean coThongTin(AnimalInfo a) {
        return co && viTri(a) >= 0;
    }

    public static boolean daKhoa(AnimalInfo a) {
        int i = viTri(a);
        return i >= 0 && (coDong[i] & CO_KHOA) != 0;
    }

    public static boolean thaDuoc(AnimalInfo a) {
        int i = viTri(a);
        return i >= 0 && (coDong[i] & CO_KHONG_THA) == 0;
    }

    /** Xu thả con này nhận thật hôm nay (đã tính trần mỗi ngày). */
    public static int xuTha(AnimalInfo a) {
        return Math.min(xuGoc(a), xuConNgay);
    }

    /** Xu thả theo cấp / bậc hiếm (chưa tính trần mỗi ngày). */
    public static int xuGoc(AnimalInfo a) {
        int i = viTri(a);
        return i >= 0 ? xu[i] : 0;
    }

    /** Con này lẽ ra có xu nhưng hôm nay đã đạt trần. */
    public static boolean hetTranNgay(AnimalInfo a) {
        return xuGoc(a) > 0 && xuConNgay <= 0;
    }

    public static final String HET_TRAN = "Đã đạt trần xu thả hôm nay";

    public static String lyDo(AnimalInfo a) {
        int i = viTri(a);
        return i >= 0 ? lyDo[i] : "";
    }

    public static boolean day() {
        return co && soThu >= sucChua;
    }

    /** "Ô thú 7/20" */
    public static String nhanO() {
        return "Ô thú " + soThu + "/" + sucChua;
    }

    /**
     * Bản trùng: mỗi loài giữ MỘT con cấp cao nhất (bằng cấp: giữ con không thả được / đứng trước), còn lại thả được
     * là ứng viên. Trả Vector AnimalInfo (thứ tự danh sách).
     */
    public static Vector banTrung() {
        Vector ra = new Vector();
        if (!co) {
            return ra;
        }
        int n = ds.size();
        boolean[] xet = new boolean[n];
        for (int i = 0; i < n; i++) {
            if (xet[i]) {
                continue;
            }
            int giu = i;
            for (int j = i; j < n; j++) {
                if (loai[j] != loai[i]) {
                    continue;
                }
                xet[j] = true;
                boolean thaJ = (coDong[j] & CO_KHONG_THA) == 0, thaGiu = (coDong[giu] & CO_KHONG_THA) == 0;
                if (cap[j] > cap[giu] || (cap[j] == cap[giu] && !thaJ && thaGiu)) {
                    giu = j;
                }
            }
            for (int j = i; j < n; j++) {
                if (loai[j] == loai[i] && j != giu && (coDong[j] & CO_KHONG_THA) == 0) {
                    ra.addElement(ds.elementAt(j));
                }
            }
        }
        // giữ thứ tự danh sách
        Vector sap = new Vector();
        for (int i = 0; i < n; i++) {
            if (ra.contains(ds.elementAt(i))) {
                sap.addElement(ds.elementAt(i));
            }
        }
        return sap;
    }

    /** Tổng xu nhận thật khi thả cả nhóm (không quá phần còn lại của trần hôm nay). */
    public static long tongXu(Vector v) {
        return Math.min(tongXuGoc(v), (long) xuConNgay);
    }

    static long tongXuGoc(Vector v) {
        long s = 0;
        for (int i = 0; i < v.size(); i++) {
            s += xuGoc((AnimalInfo) v.elementAt(i));
        }
        return s;
    }

    // ------------------------------------------------------------------ gửi máy chủ

    static Message moi(int kieu) {
        Message m = new Message(LENH);
        try {
            m.writer().writeByte(kieu);
        } catch (Throwable e) {
            // bỏ qua
        }
        return m;
    }

    static void gui(Message m) {
        try {
            GameService.gI().session.sendMessage(m);
            m.cleanup();
        } catch (Throwable e) {
            // mất kết nối
        }
    }

    public static void xinTrangThai() {
        gui(moi(0));
    }

    /** Thả các con trong {@code v} (AnimalInfo). Bấm hai lần trong lúc chờ máy chủ: bỏ qua. */
    public static boolean tha(Vector v) {
        long t = System.currentTimeMillis();
        if (v.size() == 0 || t - luThaCuoi < 3000) {
            return false;
        }
        luThaCuoi = t;
        Message m = moi(1);
        try {
            int n = Math.min(120, v.size());
            m.writer().writeByte(n);
            for (int i = 0; i < n; i++) {
                AnimalInfo a = (AnimalInfo) v.elementAt(i);
                m.writer().writeByte(a.typeAnimal == 0 ? 0 : 1);
                m.writer().writeShort(a.id);
            }
        } catch (Throwable e) {
            return false;
        }
        gui(m);
        return true;
    }

    public static void doiKhoa(AnimalInfo a, boolean khoa) {
        Message m = moi(2);
        try {
            m.writer().writeByte(a.typeAnimal == 0 ? 0 : 1);
            m.writer().writeShort(a.id);
            m.writer().writeByte(khoa ? 1 : 0);
        } catch (Throwable e) {
            return;
        }
        gui(m);
    }

    public static void dungVe() {
        gui(moi(3));
    }

    public static void muaVe() {
        gui(moi(4));
    }

    // ------------------------------------------------------------------ hộp hỏi

    /** Nút "Thêm ô": dùng vé nếu có, không thì hỏi mua ở Cửa hàng lượng. */
    public static void hoiThemO() {
        if (!co) {
            return;
        }
        if (sucChua >= toiDa) {
            DungDo.loi("Ô thú đã mở tối đa (" + toiDa + " ô).");
            return;
        }
        if (soVe > 0) {
            HopXacNhan.hoi("Mở thêm ô thú", "Dùng 1 Vé mở ô thú (còn " + soVe + " vé): ô thú " + sucChua + " -> " + (sucChua + 1)
                    + " (tối đa " + toiDa + ")?", "Dùng vé", new IAction() {
                public void perform() {
                    dungVe();
                }
            }, null);
            return;
        }
        if (giaVe < 0) {
            DungDo.bao("Vé mở ô thú có ở Cửa hàng lượng (mục Tiện ích). Mỗi vé thêm 1 ô, tối đa " + toiDa + " ô.");
            return;
        }
        HopXacNhan.hoiGia("Mở thêm ô thú", "Mua 1 Vé mở ô thú ở Cửa hàng lượng. Vé vào hành trang (tab nguyên liệu), bấm Dùng hoặc "
                + "nút Thêm ô để mở " + sucChua + " -> " + (sucChua + 1) + " ô.", "Mua vé", TienTe.LUONG, giaVe, new IAction() {
            public void perform() {
                muaVe();
            }
        });
    }

    /** Thả một con: hộp xác nhận ghi rõ tên + xu nhận. */
    public static void hoiTha(final AnimalInfo a, String ten) {
        if (!thaDuoc(a)) {
            DungDo.loi(ten + ": " + (lyDo(a).length() > 0 ? lyDo(a) : "không thả được") + ".");
            return;
        }
        int x = xuTha(a);
        String xuCau;
        if (hetTranNgay(a)) {
            xuCau = " " + HET_TRAN + ": thả không nhận xu.";
        } else if (x > 0) {
            xuCau = " Nhận " + TienTe.so(x) + " xu" + (x < xuGoc(a) ? " (chạm trần xu thả hôm nay)." : ".");
        } else {
            xuCau = " Thú có hạn: không nhận xu.";
        }
        HopXacNhan.hoiNguyHiem("Thả về tự nhiên", "Thả " + ten + " về tự nhiên? Thú sẽ mất hẳn, không lấy lại được." + xuCau, "Thả", new IAction() {
            public void perform() {
                Vector v = new Vector();
                v.addElement(a);
                tha(v);
            }
        });
    }

    /** Thả bản trùng: giữ con cấp cao nhất mỗi loài, hỏi kèm tổng xu. */
    public static void hoiThaTrung() {
        final Vector v = banTrung();
        if (v.size() == 0) {
            DungDo.bao("Không có bản trùng nào để thả (mỗi loài đã giữ con cấp cao nhất; con đang dùng / đã khoá được giữ).");
            return;
        }
        long x = tongXu(v), goc = tongXuGoc(v);
        String xuCau = x > 0 ? " Nhận tổng " + TienTe.so(x) + " xu" + (x < goc ? " (chạm trần xu thả hôm nay)." : ".")
                : (goc > 0 ? " " + HET_TRAN + ": thả không nhận xu." : "");
        HopXacNhan.hoiNguyHiem("Thả bản trùng", "Thả " + v.size() + " thú trùng loài (mỗi loài giữ lại con cấp cao nhất; con đang dùng "
                + "và đã khoá không bị thả)." + xuCau, "Thả " + v.size() + " con", new IAction() {
            public void perform() {
                tha(v);
            }
        });
    }

    /** Thú trong danh sách hiện (Char.animal) còn không. */
    public static boolean conTrongDs(AnimalInfo a) {
        return a != null && Char.animal.contains(a);
    }
}
