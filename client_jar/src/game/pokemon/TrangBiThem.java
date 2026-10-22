package game.pokemon;

import game.networklogic.GameService;
import java.io.DataInputStream;
import java.util.Hashtable;
import network.Message;

/**
 * 0.9.8: BỘ TRANG BỊ, NGỌC HỆ, CHẾ TẠO, MỐC NEO NÂNG CẤP - gói -120 của máy chủ HTBB (real.BoNgocGame / real.CheTaoGame).
 *
 * <ul>
 * <li>kiểu 0: dòng thêm cho thẻ chi tiết vật phẩm (bộ cùng bậc đang mặc x/11, lỗ ngọc, lợi / hại của ngọc) - máy chủ gửi
 *     TRƯỚC gói 21; ItemInInventory.getDescription nối {@link #moTa}.</li>
 * <li>kiểu 1: tỉ lệ thật + pity + mốc neo của lần nâng kế tiếp - ScreenDapDo đổi chữ nút "Đập" ({@link #chuNut}).</li>
 * <li>kiểu 2 / 3: danh sách công thức / kết quả chế tạo -&gt; {@link ManCheTao}.</li>
 * <li>kiểu 4: màn khảm ngọc (món có lỗ, túi ngọc, bảng giá) -&gt; {@link ManKhamNgoc}; kiểu 5: kết quả (máy chủ đã gửi
 *     thông báo -104, ở đây chỉ bỏ trạng thái chờ).</li>
 * </ul>
 * Mọi điều kiện / giá kiểm ở máy chủ; client chỉ hiện và gửi yêu cầu.
 */
public final class TrangBiThem {
    public static final int LENH = -120;
    public static final int S_DONG_THEM = 0, S_NANG_CAP = 1, S_CHE_TAO = 2, S_CHE_TAO_KQ = 3, S_KHAM = 4, S_KET_QUA = 5;
    public static final int C_XIN_CHE_TAO = 0, C_CHE_TAO = 1, C_XIN_KHAM = 2, C_DAT_NGOC = 3, C_NANG_NGOC = 4,
            C_THAO_NGOC = 5, C_MAI_NGOC = 6, C_PHAN_RA = 7;

    private TrangBiThem() {
    }

    // ---------------------------------------------------------------- dòng thêm của thẻ chi tiết

    /** itemID -&gt; chuỗi "\n&lt;màu&gt;dòng..." (định dạng ItemInInventory.setTemp). */
    static final Hashtable MO_TA = new Hashtable();
    /** itemID -&gt; {int[] màu, String[] dòng} (bảng chi tiết kiểu GO - ManNhanVat). */
    static final Hashtable MO_TA_THO = new Hashtable();

    /** Dòng thêm cho món {@code itemID} ("" nếu không có) - đã ở định dạng setTemp. */
    public static String moTa(int itemID) {
        Object o = MO_TA.get(new Integer(itemID));
        return o == null ? "" : (String) o;
    }

    /** Đặt dòng thêm (kiểm thử / gói 0). */
    public static void datMoTa(int itemID, int[] mau, String[] dong) {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < dong.length; i++) {
            sb.append(game.model.ItemInInventory.setTemp(dong[i], String.valueOf(mau[i] >= 0 && mau[i] <= 5 ? mau[i] : 0)));
        }
        if (MO_TA.size() > 300) {
            MO_TA.clear();
            MO_TA_THO.clear();
        }
        MO_TA.put(new Integer(itemID), sb.toString());
        MO_TA_THO.put(new Integer(itemID), new Object[]{mau, dong});
    }

    /** Màu chữ của mã màu dòng (0 thường, 1 đang kích, 2 ngọc, 4 tiêu đề bộ, 5 phần hại). */
    static int mauChu(int m) {
        switch (m) {
            case 1:
                return ChuDe.LA_TOI;
            case 2:
                return ChuDe.XANH_TOI;
            case 4:
                return ChuDe.VANG_TOI;
            case 5:
                return ChuDe.DO_TOI;
            default:
                return ChuDe.CHU_PHU;
        }
    }

    /**
     * Thẻ chi tiết kiểu GO (ManNhanVat): vẽ dòng bộ / lỗ ngọc của món {@code itemID} từ y, tự xuống dòng trong bề ngang
     * w, không quá {@code day}. Trả y sau cùng.
     */
    /** Chiều cao (px) các dòng bộ / lỗ ngọc của món itemID ở bề ngang w (0 nếu không có) - thẻ vật phẩm NRO (TheVatPham). */
    public static int caoDong(int itemID, int w) {
        Object o = MO_TA_THO.get(new Integer(itemID));
        if (o == null) {
            return 0;
        }
        String[] dong = (String[]) ((Object[]) o)[1];
        int n = 0;
        for (int i = 0; i < dong.length; i++) {
            n += ChuDe.ngat(dong[i], ChuDe.PHU, w, 3).length;
        }
        return 2 + n * ChuDe.cao(ChuDe.PHU) + 4;
    }

    public static int veDong(javax.microedition.lcdui.Graphics g, int itemID, int x, int y, int w, int day) {
        Object o = MO_TA_THO.get(new Integer(itemID));
        if (o == null) {
            return y;
        }
        int[] mau = (int[]) ((Object[]) o)[0];
        String[] dong = (String[]) ((Object[]) o)[1];
        int lh = ChuDe.cao(ChuDe.PHU);
        y += 2;
        for (int i = 0; i < dong.length; i++) {
            String[] d = ChuDe.ngat(dong[i], ChuDe.PHU, w, 3);
            for (int j = 0; j < d.length; j++) {
                if (y + lh > day) {
                    return y;
                }
                ChuDe.chu(g, d[j], x + (j > 0 ? 8 : 0), y, ChuDe.PHU, mauChu(mau[i]), 0);
                y += lh;
            }
        }
        return y;
    }

    // ---------------------------------------------------------------- nâng cấp: tỉ lệ + pity + mốc neo

    public static int ncItem = -1, ncK, ncTiLe, ncPity, ncMoc, ncXu, ncDa;

    /** Chữ nút "Đập" cho món đang đặt ({@code itemID}); null = giữ chữ cũ (máy chủ cũ / chưa có số). */
    public static String chuNut(int itemID) {
        if (itemID != ncItem || ncK <= 0) {
            return null;
        }
        return "Đập " + ncTiLe + "%";
    }

    /** Dòng phụ dưới ô nâng cấp: mốc neo + chi phí. */
    public static String chuPhu(int itemID) {
        if (itemID != ncItem) {
            return null;
        }
        if (ncK <= 0) {
            return "Đã đạt +15.";
        }
        String s = "Lên +" + ncK + ": tỉ lệ " + ncTiLe + "%" + (ncPity > 0 ? " (gồm +" + ncPity + "% pity)" : "") + " · "
                + TienTe.so(ncXu) + " xu · " + ncDa + " đá";
        if (ncMoc > 0) {
            s = s + " · giữ mốc +" + ncMoc;
        } else if (ncK >= 7) {
            s = s + " · trượt −1 cấp (mốc neo +7 / +10 / +13)";
        }
        return s;
    }

    // ---------------------------------------------------------------- mạng

    public static boolean nhan(Message msg) {
        if (msg.command != LENH) {
            return false;
        }
        try {
            DataInputStream r = msg.reader();
            int kieu = r.readByte();
            switch (kieu) {
                case S_DONG_THEM: {
                    int id = r.readShort();
                    int n = r.readUnsignedByte();
                    int[] mau = new int[n];
                    String[] dong = new String[n];
                    for (int i = 0; i < n; i++) {
                        mau[i] = r.readByte();
                        dong[i] = r.readUTF();
                    }
                    datMoTa(id, mau, dong);
                    break;
                }
                case S_NANG_CAP:
                    ncItem = r.readShort();
                    ncK = r.readUnsignedByte();
                    ncTiLe = r.readUnsignedByte();
                    ncPity = r.readUnsignedByte();
                    ncMoc = r.readUnsignedByte();
                    ncXu = r.readInt();
                    ncDa = r.readUnsignedByte();
                    break;
                case S_CHE_TAO:
                    ManCheTao.doc(r);
                    break;
                case S_CHE_TAO_KQ:
                    ManCheTao.ketQua(r);
                    break;
                case S_KHAM:
                    ManKhamNgoc.doc(r);
                    break;
                case S_KET_QUA:
                    r.readByte();
                    r.readUTF();
                    ManCheTao.cho = false;
                    ManKhamNgoc.cho = false;
                    break;
                default:
                    break;
            }
        } catch (Throwable e) {
            // gói hỏng: bỏ qua
        }
        return true;
    }

    static void gui(int kieu, int[] so, int[] co) {
        try {
            Message m = new Message(LENH);
            m.writer().writeByte(kieu);
            for (int i = 0; so != null && i < so.length; i++) {
                if (co[i] == 2) {
                    m.writer().writeShort(so[i]);
                } else {
                    m.writer().writeByte(so[i]);
                }
            }
            GameService.gI().session.sendMessage(m);
        } catch (Throwable e) {
            // mất kết nối: bỏ qua
        }
    }

    /** "Ngọc Lửa 3" */
    public static String tenNgoc(int he, int cap) {
        return "Ngọc " + KieuO.tenHe(he) + " " + cap;
    }
}
