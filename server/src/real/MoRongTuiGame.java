package real;

import com.htbb.pokemon.MoRongHanhTrang;
import data.Database;
import io.Message;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * Mo rong hanh trang theo o: ve "Vé mở rộng hành trang" (+10 o) va mua thang bang xu (gia tang dan). Luat / gia /
 * tran: com.htbb.pokemon.MoRongHanhTrang. Luu: Char.oMoRong -&gt; tob_char.o_mo_rong (Database.luuCotMoRong).
 *
 * <pre>
 * GIAO THUC (19/10/2026, tuong thich nguoc):
 *
 * C-&gt;S  -107 LENH (MapXuLyLenh):   byte kieu
 *          kieu 0 = chi xin trang thai (server tra -105)
 *          kieu 1 = dung 1 ve mo rong (uu tien ve khoa, roi ve thuong)
 *          kieu 2 = mua +10 o bang xu (gia = giaXuBuocKe trong -105)
 *        Tra loi: -104 (byte loai 0 ok / 1 loi / 2 tin, UTF noi dung, byte kieu = HanhDongVatPham.MO_RONG = 10)
 *        + -105 moi (client &gt;= 903). Kieu 0 chi tra -105.
 *
 * C-&gt;S  -67 douseGemItem (cu):     short idGem (= idVe 270), byte khoa -&gt; dung 1 ve (nhu kieu 1, dung loai khoa
 *        da chon). Client cu bam "Dung" tren nguyen lieu la vao duong nay.
 *
 * S-&gt;C  -105 (HanhDongVatPham.taoTrangThai): sau "short soTui, short soMac" NOI THEM (client cu bo qua):
 *          short oDaMo        tong so o da mo = suc chua that (42 x trang + o le)
 *          short oToiDaVe     tran mo bang ve / xu (126) - hoac suc chua that neu da vuot (mua trang luong)
 *          byte  oMoiVe       so o moi ve / moi lan mua xu (10)
 *          int   giaXuBuocKe  gia xu lan mo ke tiep; -1 = da cham tran
 *          int   soVe         so ve dang giu (thuong + khoa)
 *          short idVe         id mau nguyen lieu cua ve (Map.gemTemplate, icon 6500 + idImage); -1 = may chu khong co ve
 *          byte  loaiVe       loai vat pham cua ve: 0 = binh (potion), 1 = nguyen lieu (gem, listGemitem, dung -67),
 *                             2 = vat pham dac biet (specialItem), 3 = vat pham thuong (iItems). Hien tai luon 1.
 *
 * NPC: Kangaskhan thu kho (loai 9 / 30) - menu "Mở kho đồ" / "Mở rộng hành trang (+10 ô: N xu)" -&gt; hop xac nhan.
 * Cua hang luong: mon ve_mo_rong_tui (trao mo_rong_tui) -&gt; 1 ve KHOA vao tui (NapTienGame).
 * </pre>
 */
public final class MoRongTuiGame {

    public static final int LENH = -107;
    public static final byte KIEU_TRANG_THAI = 0, KIEU_DUNG_VE = 1, KIEU_MUA_XU = 2;
    /** Id mau nguyen lieu (Map.gemTemplate) cua Ve mo rong hanh trang. */
    public static final short ID_VE = 270;
    /** Anh tam: giay vang (gem 52). Client moi nen ve icon rieng theo idVe. */
    public static final int ANH_VE = 52;
    public static final byte LOAI_BINH = 0, LOAI_NGUYEN_LIEU = 1, LOAI_DAC_BIET = 2, LOAI_VAT_PHAM = 3;
    public static final String TEN_VE = "Vé mở rộng hành trang";
    /** Ma mon cua hang luong (nap_cua_hang.ma) trao 1 ve vao tui. */
    public static final String MA_CUA_HANG = "ve_mo_rong_tui";

    private MoRongTuiGame() {
    }

    /** Mau nguyen lieu cua ve (MapNapTaiNguyen.loadGemTemplate). */
    public static GemTemplate taoMauVe() {
        return new GemTemplate(ID_VE, ANH_VE, TEN_VE,
                "Dùng: hành trang thêm " + MoRongHanhTrang.O_MOI_VE + " ô (mở bằng vé tối đa "
                        + MoRongHanhTrang.TRAN_VE + " ô).",
                MoRongHanhTrang.O_MOI_VE, -1, 0, GemTemplate.TYPE_TU_BINH, Map.NOT_SELL, 0);
    }

    /** Id ve neu may chu co mau ve (ban Indo khong co), -1 neu khong. */
    public static short idVe() {
        final GemTemplate[] g = Map.gemTemplate;
        return g != null && g.length > ID_VE && g[ID_VE] != null && g[ID_VE].id == ID_VE ? ID_VE : (short) -1;
    }

    public static boolean laVe(final int idGem) {
        return idGem == ID_VE && idVe() == ID_VE;
    }

    static boolean coMang(final int[] a) {
        return a != null && a.length > ID_VE;
    }

    /** So ve thuong co the dung (tru phan dang rao ban). */
    static int soVeThuong(final Char p) {
        if (!coMang(p.listGemitem)) {
            return 0;
        }
        final int ban = coMang(p.listGemitemSell) ? p.listGemitemSell[ID_VE] : 0;
        return Math.max(0, p.listGemitem[ID_VE] - ban);
    }

    static int soVeKhoa(final Char p) {
        return coMang(p.listGemitemLock) ? Math.max(0, p.listGemitemLock[ID_VE]) : 0;
    }

    /** Tong so ve dang giu (thuong + khoa). */
    public static int soVe(final Char p) {
        return idVe() < 0 ? 0 : soVeThuong(p) + soVeKhoa(p);
    }

    /** Ten muc NPC Kangaskhan. */
    static String tenMucNpc(final Char p) {
        final long gia = MoRongHanhTrang.giaXuKeTiep(p.sucChuaTui(), p.oMoRong);
        return gia < 0 ? "Mở rộng hành trang (đã tối đa)"
                : "Mở rộng hành trang (+" + MoRongHanhTrang.soOThem(p.sucChuaTui()) + " ô: " + HanhDongVatPham.xu(gia) + " xu)";
    }

    // ================================================================== trang thai -105

    /** Ban client dau tien doc phan noi them cua -105 / gui -107 (0.9.8; 906 / 907 da phat hanh khong co). */
    public static final int BAN_CLIENT = 908;

    /** Phan noi them cua -105 (HanhDongVatPham.taoTrangThai), chi client &gt;= {@link #BAN_CLIENT}. */
    static void ghiTrangThai(final DataOutputStream dos, final Char p) throws IOException {
        final int sucChua = p.sucChuaTui();
        dos.writeShort(Math.min(Short.MAX_VALUE, sucChua));
        dos.writeShort(Math.min(Short.MAX_VALUE, MoRongHanhTrang.tranHienThi(sucChua)));
        dos.writeByte(MoRongHanhTrang.O_MOI_VE);
        final long gia = MoRongHanhTrang.giaXuKeTiep(sucChua, p.oMoRong);
        dos.writeInt((int) Math.max(-1L, Math.min(Integer.MAX_VALUE, gia)));
        dos.writeInt(soVe(p));
        dos.writeShort(idVe());
        dos.writeByte(LOAI_NGUYEN_LIEU);
    }

    /** Gui -105 cho client &gt;= 903 (dang nhap, sau moi lan doi suc chua). */
    public static void guiTrangThai(final Char p) {
        try {
            if (HanhDongVatPham.coTrangThai(p)) {
                p.sendMessage(HanhDongVatPham.taoTrangThai(p, HanhDongVatPham.MO_RONG));
            }
        } catch (final Exception ignored) {
        }
    }

    // ================================================================== thao tac

    /** Ly do khong mo rong duoc chung cho ve / xu (null = duoc). */
    static String kiemChung(final Char p) {
        if (p.userTrade != null && p.userTrade.size() > 0) {
            return "Không thể mở rộng hành trang khi đang trao đổi.";
        }
        if (MoRongHanhTrang.soOThem(p.sucChuaTui()) <= 0) {
            return "Hành trang đã mở rộng tối đa bằng vé (" + MoRongHanhTrang.TRAN_VE + " ô).";
        }
        return null;
    }

    /**
     * Dung 1 ve. {@code khoa} null = tu chon (ve khoa truoc), true / false = dung dung loai do (goi -67).
     * @return null neu thanh cong, ly do neu tu choi (ve KHONG bi tru).
     */
    public static String dungVe(final Char p, final Boolean khoa) {
        synchronized (p) {
            if (idVe() < 0) {
                return "Máy chủ chưa có vé mở rộng hành trang.";
            }
            final boolean dungKhoa;
            if (khoa == null) {
                dungKhoa = soVeKhoa(p) > 0;
            } else {
                dungKhoa = khoa.booleanValue();
            }
            if ((dungKhoa ? soVeKhoa(p) : soVeThuong(p)) <= 0) {
                return "Bạn không có " + TEN_VE + ".";
            }
            final String loi = kiemChung(p);
            if (loi != null) {
                return loi;
            }
            final int them = MoRongHanhTrang.soOThem(p.sucChuaTui());
            p.delGem(ID_VE, 1, dungKhoa);
            p.oMoRong = (short) (p.oMoRong + them);
            sauKhiMo(p, "ve" + (dungKhoa ? " khoa" : ""), them, 0L);
            return null;
        }
    }

    /** Mua +10 o bang xu. @return null neu thanh cong, ly do neu tu choi (khong tru xu). */
    public static String muaXu(final Char p) {
        synchronized (p) {
            final String loi = kiemChung(p);
            if (loi != null) {
                return loi;
            }
            final long gia = MoRongHanhTrang.giaXuKeTiep(p.sucChuaTui(), p.oMoRong);
            if (gia < 0) {
                return "Hành trang đã mở rộng tối đa.";
            }
            if (!p.truXuNeuDu(gia, true, "mo rong hanh trang")) {
                return "Không đủ xu (cần " + HanhDongVatPham.xu(gia) + " xu).";
            }
            final int them = MoRongHanhTrang.soOThem(p.sucChuaTui());
            p.oMoRong = (short) (p.oMoRong + them);
            sauKhiMo(p, "xu", them, gia);
            return null;
        }
    }

    static void sauKhiMo(final Char p, final String cach, final int them, final long gia) {
        try {
            p.sendMessage(MessageCreator.createCharGemItem(p));
            p.sendMessage(MessageCreator.createMainCharInfoMessage(p));
        } catch (final Exception ignored) {
        }
        if (p.charDBID > 0 && Database.instance != null) {
            try {
                Database.instance.saveCharAuto(p);
                Database.instance.saveOrtherLog("tob_other_log", p.charname,
                        "mo rong hanh trang +" + them + " o bang " + cach + (gia > 0 ? " " + gia + " xu" : "")
                                + " -> " + p.sucChuaTui() + " o", "morong");
                if (gia > 0) {
                    Database.instance.saveLogThongKeChiTieu("mo rong hanh trang", 0, 1, gia, "xu");
                }
            } catch (final Exception e) {
                System.out.println("[MO_RONG] luu loi " + p.charname + ": " + e);
            }
        }
    }

    static String thanhCong(final Char p) {
        return "Hành trang đã mở rộng: " + p.sucChuaTui() + " ô.";
    }

    /** Dung ve qua goi -67 (Map.doTuBinh). */
    static void dungVeTuGoiCu(final Char p, final boolean khoa) throws IOException {
        HanhDongVatPham.boc(p, HanhDongVatPham.MO_RONG, () -> traLoi(p, dungVe(p, Boolean.valueOf(khoa))));
    }

    static void traLoi(final Char p, final String loi) {
        if (loi == null) {
            DungVatPham.bao(p, thanhCong(p));
        } else {
            DungVatPham.loi(p, loi);
        }
    }

    /** Lenh -107 tu client. */
    public static void xuLy(final Char p, final Message m) throws IOException {
        final byte kieu = m.dis.available() > 0 ? m.dis.readByte() : KIEU_TRANG_THAI;
        switch (kieu) {
            case KIEU_DUNG_VE:
                HanhDongVatPham.boc(p, HanhDongVatPham.MO_RONG, () -> traLoi(p, dungVe(p, null)));
                return;
            case KIEU_MUA_XU:
                HanhDongVatPham.boc(p, HanhDongVatPham.MO_RONG, () -> traLoi(p, muaXu(p)));
                return;
            default:
                guiTrangThai(p);
        }
    }

    // ================================================================== cua hang luong

    /** Mua ve o cua hang luong duoc khong (null = duoc). Khong ban ve thua: tinh ca ve dang giu. */
    public static String kiemMuaVe(final Char p) {
        if (idVe() < 0) {
            return "Máy chủ chưa có vé mở rộng hành trang.";
        }
        final int sauKhiDungHet = p.sucChuaTui() + soVe(p) * MoRongHanhTrang.O_MOI_VE;
        if (sauKhiDungHet >= MoRongHanhTrang.TRAN_VE) {
            return soVe(p) > 0 ? "Số vé đang giữ đã đủ mở tới " + MoRongHanhTrang.TRAN_VE + " ô, hãy dùng vé trước."
                    : "Hành trang đã mở rộng tối đa bằng vé (" + MoRongHanhTrang.TRAN_VE + " ô).";
        }
        if (soVe(p) <= 0 && p.isFullInventory()) {
            return "Hành trang cần 1 ô trống.";
        }
        return null;
    }

    /** Trao {@code n} ve KHOA (khong giao dich) vao tui. */
    public static void traoVe(final Char p, final int n) {
        if (idVe() < 0 || n <= 0) {
            throw new IllegalStateException("may chu chua co ve mo rong");
        }
        p.doAddGemItem(ID_VE, n, true);
        try {
            p.sendMessage(MessageCreator.createCharGemItem(p));
        } catch (final Exception ignored) {
        }
    }

    // ================================================================== NPC Kangaskhan (9 / 30)

    /** Menu NPC: 0 = mo kho do, 1 = mo rong hanh trang. */
    static String[] menuKangaskhan(final Char p) {
        return new String[]{"Mở kho đồ", tenMucNpc(p)};
    }

    /** idMenu 0: menu chinh; idMenu 1: xac nhan mua xu. */
    static void chonKangaskhan(final Char p, final int idNpc, final int idMenu, final int chon) throws IOException {
        if (idMenu == 0) {
            if (chon == 0) {
                guiKhoDo(p);
            } else if (chon == 1) {
                final String loi = kiemChung(p);
                if (loi != null) {
                    DungVatPham.loi(p, loi);
                    return;
                }
                final long gia = MoRongHanhTrang.giaXuKeTiep(p.sucChuaTui(), p.oMoRong);
                final int them = MoRongHanhTrang.soOThem(p.sucChuaTui());
                p.sendMessage(MessageCreator.createMsgMenu(new String[]{
                    "Đồng ý: +" + them + " ô, trả " + HanhDongVatPham.xu(gia) + " xu (" + p.sucChuaTui() + " → "
                            + (p.sucChuaTui() + them) + " ô)",
                    "Thôi",
                }, idNpc, 1));
            }
        } else if (idMenu == 1 && chon == 0) {
            HanhDongVatPham.boc(p, HanhDongVatPham.MO_RONG, () -> traLoi(p, muaXu(p)));
        }
    }

    /** Cua so kho do (goi 23 kieu 2) - cung noi dung RequestNPCInfoHandler case 9 / 30. */
    static void guiKhoDo(final Char p) throws IOException {
        final Message m = new Message(23);
        m.dos.writeByte(2);
        p.isPutItem2Bag = true;
        m.dos.writeByte(p.bItem.size());
        for (int k = 0; k < p.bItem.size(); ++k) {
            final Item it = p.bItem.get(k);
            m.dos.writeByte(it.clazz);
            m.dos.writeShort(it.id);
            m.dos.writeShort(it.getTemplate().id);
            m.dos.writeByte(it.plus);
            m.dos.writeByte(it.level);
            m.dos.writeShort(it.mDurable);
            m.dos.writeShort(it.durable);
        }
        p.sendMessage(m);
    }
}
