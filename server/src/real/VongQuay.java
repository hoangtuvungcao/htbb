package real;

import com.htbb.cauhinh.CauHinhTrungTam;
import com.htbb.pokemon.ChongClone;
import com.htbb.pokemon.ChongCloneLogic;
import com.htbb.pokemon.VongQuayLogic;
import data.Database;
import data.DatabaseVongQuay;
import java.security.SecureRandom;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * VONG QUAY GOP HU tai NPC Ruong (-15, muc "Vong quay"): VIP + thuong (xu), luong. Luat thuan:
 * {@link VongQuayLogic}; cau hinh: nhom {@code vong_quay.*} cua cau hinh trung tam (tools/cauhinh/danh_muc.json);
 * CSDL: {@link DatabaseVongQuay} + sql/sua_vong_quay.sql.
 *
 * Thay toan bo he cu (Char.doXoso / doXosoLow / doXosoLuong, CharVongLap.startThreadXoso, VongQuayLuong) - sua
 * 19/10/2026 theo bao loi cua chu game:
 * <ol>
 *   <li>Nut "Dong y" cua hop thong tin (popup 100) chi gui lai DUNG hop do (Map.doPopup -> infoXoso), va khi chua du
 *       2 nguoi infoXoso con dat lai dong ho ve 02:00 -> bam bao nhieu cung "khong co gi xay ra", cuoc treo mai.
 *       Nay: moi loai mot popup rieng (97/98/99); Dong y = mo bang chon muc dat (chua dat) / lam moi (da dat).</li>
 *   <li>Muc cu 1.000.000 - 1.000.000.000 xu / luot (VIP), can giu 5.000.000 xu: hang tram gio thu nhap cua nguoi
 *       choi moi -> nay theo dai cap (thiet_ke_can_bang.md 1.6), co tran ngay, phi hu la ho hut.</li>
 *   <li>Chi 1 nguoi: hoan du khi het gio (khong "thang 100%" cua chinh minh, khong treo cuoc).</li>
 *   <li>Chuyen tien giua nguoi choi -> qua cong ChongClone (cap, tuoi tai khoan, gio choi) + toi da N Pokemon
 *       cung thiet bi / IP moi ngay; moi lan thua -> thang ghi vao tob_log_chuyen_gia_tri.</li>
 * </ol>
 */
public final class VongQuay {

    public static final int NPC = -15;
    /** Menu chon loai {"Vong quay VIP","Vong quay thuong","Vong quay luong"} (idMenu cu cua NPC -15). */
    public static final int MENU_CHON_LOAI = 4;

    public static final VongQuay VIP = new VongQuay(1, "vip", "Vòng quay VIP", false, 5, 104, 97, -32040);
    public static final VongQuay THUONG = new VongQuay(0, "thuong", "Vòng quay thường", false, 6, 105, 98, -32041);
    public static final VongQuay LUONG = new VongQuay(2, "luong", "Vòng quay lượng", true, 103, 106, 99, -32056);
    /** Thu tu trong menu chon loai. */
    static final VongQuay[] THEO_MENU = {VIP, THUONG, LUONG};

    static final VongQuayLogic.SoNgay SO_NGAY = new VongQuayLogic.SoNgay();
    private static final Random NGAU_NHIEN = new SecureRandom();
    /** Moi ghi CSDL cua vong quay di qua MOT luong, dung thu tu (dat -> xoa luot -> tra). */
    private static final ExecutorService GHI = new ThreadPoolExecutor(1, 1, 60L, TimeUnit.SECONDS,
            new LinkedBlockingQueue<Runnable>(), r -> {
                final Thread t = new Thread(r, "vong-quay-ghi");
                t.setDaemon(true);
                return t;
            });
    private static volatile boolean daChay = false;

    /** 0 = thuong, 1 = VIP, 2 = luong (khoa CSDL tob_vong_quay_*.loai va macDinh cua VongQuayLogic). */
    public final int loai;
    public final String ma;
    public final String ten;
    public final boolean laLuong;
    public final int menuThongTin;
    public final int menuDat;
    public final int popup;
    public final int oNhap;

    final VongQuayLogic.Vong vong = new VongQuayLogic.Vong();
    String thangTruoc = "";
    long traTruoc;
    long cuocTruoc;

    private VongQuay(final int loai, final String ma, final String ten, final boolean laLuong, final int menuThongTin,
            final int menuDat, final int popup, final int oNhap) {
        this.loai = loai;
        this.ma = ma;
        this.ten = ten;
        this.laLuong = laLuong;
        this.menuThongTin = menuThongTin;
        this.menuDat = menuDat;
        this.popup = popup;
        this.oNhap = oNhap;
    }

    String donVi() {
        return this.laLuong ? "lượng" : "xu";
    }

    // ------------------------------------------------------------------ cau hinh

    public VongQuayLogic.CauHinh cauHinh() {
        final VongQuayLogic.CauHinh d = VongQuayLogic.macDinh(this.loai);
        try {
            final String k = "vong_quay." + this.ma + ".";
            return new VongQuayLogic.CauHinh(
                    CauHinhTrungTam.so(k + "bat", d.bat ? 1 : 0) == 1,
                    (int) CauHinhTrungTam.so(k + "cap_toi_thieu", d.capToiThieu),
                    CauHinhTrungTam.so(k + "cuoc_toi_thieu", d.cuocToiThieu),
                    CauHinhTrungTam.so(k + "cuoc_toi_da", d.cuocToiDa),
                    CauHinhTrungTam.so(k + "cuoc_ngay_toi_da", d.cuocNgayToiDa),
                    (int) CauHinhTrungTam.so(k + "luot_ngay_toi_da", d.luotNgayToiDa),
                    (int) CauHinhTrungTam.so(k + "so_nguoi_toi_da", d.soNguoiToiDa),
                    (int) CauHinhTrungTam.so(k + "phi_phan_tram", d.phiPhanTram),
                    (int) CauHinhTrungTam.so("vong_quay.giay_moi_vong", d.giayMoiVong),
                    (int) CauHinhTrungTam.so("vong_quay.giay_khoa", d.giayKhoa));
        } catch (final Throwable t) {
            return d;
        }
    }

    /** Cap hieu luc = max(cap cua vong quay, cap cua cong chong clone). */
    static int capHieuLuc(final VongQuayLogic.CauHinh c) {
        final ChongCloneLogic.CauHinh cc = ChongClone.cauHinh();
        return cc.bat ? Math.max(c.capToiThieu, cc.capToiThieu) : c.capToiThieu;
    }

    // ------------------------------------------------------------------ dinh tuyen (MapDoMenu / Map / MapNhapText)

    public static VongQuay theoPopup(final int idPopup) {
        for (final VongQuay v : THEO_MENU) {
            if (v.popup == idPopup) {
                return v;
            }
        }
        return null;
    }

    public static VongQuay theoO(final int idActor) {
        for (final VongQuay v : THEO_MENU) {
            if (v.oNhap == idActor) {
                return v;
            }
        }
        return null;
    }

    /** Mo menu chon loai (muc "Vong quay" cua NPC -15). */
    public static void moChonLoai(final Char p, final int idNpc) {
        p.sendMessage(MessageCreator.createMsgMenu(new String[]{"Vòng quay VIP", "Vòng quay thường", "Vòng quay lượng"},
                idNpc, MENU_CHON_LOAI));
    }

    /** Xu ly menu cua NPC -15 thuoc vong quay. true = da xu ly. */
    public static boolean doMenu(final Char p, final int idNpc, final int idMenu, final int idOption) {
        if (idMenu == MENU_CHON_LOAI) {
            if (idOption >= 0 && idOption < THEO_MENU.length) {
                THEO_MENU[idOption].moMenu(p, idNpc);
            }
            return true;
        }
        for (final VongQuay v : THEO_MENU) {
            if (idMenu == v.menuThongTin) {
                if (idOption == 0) {
                    v.guiThongTin(p);
                } else if (idOption == 1) {
                    v.moChonMuc(p, idNpc);
                } else if (idOption == 2) {
                    v.guiHuongDan(p);
                }
                return true;
            }
            if (idMenu == v.menuDat) {
                v.chonMuc(p, idNpc, idOption);
                return true;
            }
        }
        return false;
    }

    void moMenu(final Char p, final int idNpc) {
        p.sendMessage(MessageCreator.createMsgMenu(new String[]{"Thông tin", "Tham gia", "Hướng dẫn"}, idNpc, this.menuThongTin));
    }

    /** Nut "Dong y" cua hop thong tin: chua dat -> bang chon muc; da dat / khong dat duoc -> lam moi. */
    public void dongY(final Char p) {
        if (daDat(p) || lyDoKhongDatDuoc(p) != null) {
            guiThongTin(p);
        } else {
            moChonMuc(p, NPC);
        }
    }

    synchronized boolean daDat(final Char p) {
        return this.vong.cua(p.charname) != null;
    }

    void moChonMuc(final Char p, final int idNpc) {
        if (daDat(p)) {
            guiThongTin(p);
            return;
        }
        final String chan = lyDoKhongDatDuoc(p);
        if (chan != null) {
            bao(p, chan);
            return;
        }
        final long[] muc = VongQuayLogic.mucGoiY(cauHinh());
        final String[] menu = new String[muc.length + 2];
        for (int i = 0; i < muc.length; i++) {
            menu[i] = "Đặt " + VongQuayLogic.so(muc[i]) + " " + donVi();
        }
        menu[muc.length] = "Nhập số khác";
        menu[muc.length + 1] = "Xem hũ";
        p.sendMessage(MessageCreator.createMsgMenu(menu, idNpc, this.menuDat));
    }

    void chonMuc(final Char p, final int idNpc, final int idOption) {
        final VongQuayLogic.CauHinh c = cauHinh();
        final long[] muc = VongQuayLogic.mucGoiY(c);
        if (idOption >= 0 && idOption < muc.length) {
            datCuoc(p, muc[idOption]);
        } else if (idOption == muc.length) {
            p.sendMessage(MessageCreator.createMsgInputText(this.oNhap, 0, "Số " + donVi() + " đặt ("
                    + VongQuayLogic.so(c.cuocToiThieu) + " – " + VongQuayLogic.so(c.cuocToiDa) + ")", 1));
        } else {
            guiThongTin(p);
        }
    }

    /** O nhap so (MapNhapText). */
    public void nhapSo(final Char p, final String chu) {
        final String s = chu == null ? "" : chu.replace(".", "").replace(",", "").replace(" ", "").trim();
        final long so;
        try {
            so = Long.parseLong(s);
        } catch (final Exception e) {
            bao(p, "Số " + donVi() + " không hợp lệ.");
            return;
        }
        datCuoc(p, so);
    }

    // ------------------------------------------------------------------ dat cuoc

    /** Ly do nguoi choi khong dat duoc (chua xet so tien), null neu duoc. Goi NGOAI khoa {@code this} (doc CSDL). */
    String lyDoKhongDatDuoc(final Char p) {
        if (p == null || p.isBot != -1) {
            return "Không thể tham gia.";
        }
        if (Char.isBaotriVxmm) {
            return "Vòng quay đang bảo trì.";
        }
        final VongQuayLogic.CauHinh c = cauHinh();
        if (!c.bat) {
            return this.ten + " đang đóng.";
        }
        final int cap = capHieuLuc(c);
        if (p.lvDetail == null || p.lvDetail.lv < cap) {
            return "Cần đạt cấp " + cap + " mới tham gia " + this.ten + ".";
        }
        final String cc = ChongClone.lyDo(p, true);
        if (cc != null) {
            return cc.replace("giao dịch xu", "tham gia vòng quay").replace("giao dịch / mua bán xu với người chơi khác", "tham gia vòng quay");
        }
        final ChongCloneLogic.Ben b = ChongClone.ben(p, false);
        final String may = SO_NGAY.kiemMay(ngay(), p.charname, b.may, b.ip, ChongClone.cauHinh().nvCungMayToiDa);
        if (may != null) {
            return may;
        }
        final long bay = System.currentTimeMillis();
        synchronized (this) {
            if (VongQuayLogic.dangKhoa(c, this.vong, bay) || VongQuayLogic.denGio(c, this.vong, bay)) {
                return "Lượt này đã khoá đặt (" + c.giayKhoa + " giây cuối). Đợi lượt sau nhé.";
            }
            if (this.vong.soNguoi() >= c.soNguoiToiDa) {
                return "Lượt này đã đủ " + c.soNguoiToiDa + " người. Đợi lượt sau nhé.";
            }
        }
        if (SO_NGAY.luot(ngay(), this.loai, p.charname) >= c.luotNgayToiDa) {
            return "Hôm nay bạn đã quay đủ " + c.luotNgayToiDa + " lượt " + this.ten + ". Hẹn ngày mai.";
        }
        return null;
    }

    public void datCuoc(final Char p, final long so) {
        final VongQuayLogic.CauHinh c = cauHinh();
        final ChongCloneLogic.Ben b = ChongClone.ben(p, false);
        final VongQuayLogic.NguoiCuoc n;
        final String chan = daDat(p) ? null : lyDoKhongDatDuoc(p); // cong chong clone doc CSDL: ngoai khoa
        if (chan != null) {
            bao(p, chan);
            return;
        }
        synchronized (this) {
            final String n0 = ngay();
            final VongQuayLogic.NguoiDat nd = new VongQuayLogic.NguoiDat(p.charname, p.lvDetail == null ? 0 : p.lvDetail.lv,
                    this.laLuong ? (long) p.getLuong() : p.getxu(), SO_NGAY.luot(n0, this.loai, p.charname),
                    SO_NGAY.tong(n0, this.loai, p.charname));
            String loi = VongQuayLogic.kiemCuoc(c, this.vong, System.currentTimeMillis(), nd, so, donVi());
            if (loi == null) { // kiem lai trong khoa: hai nick cung may bam cung luc
                loi = SO_NGAY.kiemMay(n0, p.charname, b.may, b.ip, ChongClone.cauHinh().nvCungMayToiDa);
            }
            if (loi != null) {
                bao(p, loi);
                return;
            }
            final boolean daTru = this.laLuong ? p.truLuongMoChuyen(so)
                    : p.truXuNeuDu(so, false, "vong quay " + this.ma + " dat cuoc");
            if (!daTru) {
                bao(p, "Không đủ " + VongQuayLogic.so(so) + " " + donVi() + ".");
                return;
            }
            n = new VongQuayLogic.NguoiCuoc(p.charname, p.charDBID, so, b.may, b.ip);
            this.vong.them(n, System.currentTimeMillis());
            SO_NGAY.ghi(ngay(), this.loai, p.charname, b.may, b.ip, so);
        }
        try {
            p.sendMessage(MessageCreator.createCharInventoryMessage(p, 0));
        } catch (final Exception ignored) {
        }
        final Char pp = p;
        GHI.execute(() -> {
            luuNhanVat(pp); // luu so du DA TRU truoc, roi moi ghi cuoc: sap may giua chung = mat cuoc, khong nhan doi
            DatabaseVongQuay.ghiCuoc(this.loai, n);
            DatabaseVongQuay.ghiNgay(this.loai, n.ten, n.may, n.ip, 1, n.so);
        });
        try {
            Database.instance.saveOrtherLog("", p.charname, "dat cuoc " + this.ma + " " + so + " " + donVi(), "bidxoso");
        } catch (final Throwable ignored) {
        }
        guiThongTin(p);
    }

    // ------------------------------------------------------------------ thong tin

    public void guiThongTin(final Char p) {
        p.sendMessage(MessageCreator.createMsgPopUp(p.id, this.popup, thongTin(p)));
    }

    String thongTin(final Char p) {
        final VongQuayLogic.CauHinh c = cauHinh();
        final long bay = System.currentTimeMillis();
        final String dv = donVi();
        final StringBuilder s = new StringBuilder();
        final boolean datDuoc = !daDat(p) && lyDoKhongDatDuoc(p) == null;
        synchronized (this) {
            final VongQuayLogic.NguoiCuoc minh = this.vong.cua(p.charname);
            final long hu = this.vong.hu();
            s.append(this.ten).append(": mỗi người góp ").append(dv).append(", hết giờ quay ra một người nhận cả hũ.\n");
            s.append("Hũ: ").append(VongQuayLogic.so(hu)).append(' ').append(dv).append(" (")
                    .append(this.vong.soNguoi()).append('/').append(c.soNguoiToiDa).append(" người)\n");
            if (this.vong.batDau() <= 0L) {
                s.append("Thời gian: chưa bắt đầu (").append(VongQuayLogic.phutGiay(c.giayMoiVong))
                        .append(" tính từ người đặt đầu tiên)\n");
            } else {
                s.append("Còn lại: ").append(VongQuayLogic.phutGiay(VongQuayLogic.conLaiGiay(c, this.vong, bay)))
                        .append(VongQuayLogic.dangKhoa(c, this.vong, bay) ? " (đã khoá đặt)" : "").append('\n');
            }
            if (minh != null) {
                s.append("Bạn đã đặt: ").append(VongQuayLogic.so(minh.so)).append(' ').append(dv)
                        .append(", cơ hội thắng ").append(VongQuayLogic.tiLe(minh.so, hu)).append("%\n");
                if (this.vong.soNguoi() < 2) {
                    s.append("Cần ít nhất 2 người. Hết giờ mà chỉ có bạn: hoàn lại đủ, không mất phí.\n");
                }
            } else {
                s.append("Mức đặt: ").append(VongQuayLogic.so(c.cuocToiThieu)).append(" – ")
                        .append(VongQuayLogic.so(c.cuocToiDa)).append(' ').append(dv).append(" (cấp ")
                        .append(capHieuLuc(c)).append("+)\n");
            }
            final String n0 = ngay();
            s.append("Hôm nay: ").append(SO_NGAY.luot(n0, this.loai, p.charname)).append('/').append(c.luotNgayToiDa)
                    .append(" lượt, đã đặt ").append(VongQuayLogic.so(SO_NGAY.tong(n0, this.loai, p.charname))).append('/')
                    .append(VongQuayLogic.so(c.cuocNgayToiDa)).append(' ').append(dv).append('\n');
            s.append("Phí hũ ").append(c.phiPhanTram).append("% (bị huỷ, chống lạm phát)\n");
            s.append("Lượt trước: ").append(this.thangTruoc.isEmpty() ? "chưa có"
                    : this.thangTruoc + " thắng " + VongQuayLogic.so(this.traTruoc) + " " + dv + " (đặt "
                    + VongQuayLogic.so(this.cuocTruoc) + ")").append('\n');
            s.append(minh == null && datDuoc ? "Bấm Đồng ý để chọn mức đặt." : "Bấm Đồng ý để làm mới.");
        }
        return s.toString();
    }

    void guiHuongDan(final Char p) {
        final VongQuayLogic.CauHinh c = cauHinh();
        final ChongCloneLogic.CauHinh cc = ChongClone.cauHinh();
        final String dv = donVi();
        final StringBuilder s = new StringBuilder();
        s.append(this.ten).append(": mỗi người góp ").append(dv).append(" vào hũ, hết giờ quay ngẫu nhiên một người thắng.\n");
        s.append("- Đặt 1 lần mỗi lượt, từ ").append(VongQuayLogic.so(c.cuocToiThieu)).append(" đến ")
                .append(VongQuayLogic.so(c.cuocToiDa)).append(' ').append(dv).append(".\n");
        s.append("- Cơ hội thắng = số bạn đặt / tổng hũ.\n");
        s.append("- Mỗi lượt ").append(VongQuayLogic.phutGiay(c.giayMoiVong)).append(", tính từ người đặt đầu tiên; ")
                .append(c.giayKhoa).append(" giây cuối khoá đặt.\n");
        s.append("- Người thắng nhận cả hũ trừ phí ").append(c.phiPhanTram).append("% (phí bị huỷ để chống lạm phát).\n");
        s.append("- Hết giờ mà chỉ có 1 người: hoàn lại đủ, không mất phí.\n");
        s.append("- Mỗi ngày tối đa ").append(c.luotNgayToiDa).append(" lượt, tổng ").append(VongQuayLogic.so(c.cuocNgayToiDa))
                .append(' ').append(dv).append(".\n");
        s.append("- Cần cấp ").append(capHieuLuc(c));
        if (cc.bat) {
            s.append(", tài khoản tạo đủ ").append(cc.tuoiTaiKhoanGio).append(" giờ, chơi đủ ").append(cc.gioChoiToiThieu)
                    .append(" giờ; mỗi thiết bị / mạng tối đa ").append(cc.nvCungMayToiDa).append(" Pokémon mỗi ngày");
        }
        s.append(".\n- Đang thoát game lúc có kết quả: tiền được cộng khi bạn vào lại.");
        bao(p, s.toString());
    }

    // ------------------------------------------------------------------ ket thuc luot

    /** Mot nhip cua luong nen. */
    void nhip(final long bay) {
        final VongQuayLogic.KetQua kq;
        synchronized (this) {
            kq = VongQuayLogic.ketThucNeuDenGio(cauHinh(), this.vong, bay, NGAU_NHIEN);
            if (kq != null && !kq.hoan) {
                this.thangTruoc = kq.nguoiThang.ten;
                this.traTruoc = kq.tra;
                this.cuocTruoc = kq.nguoiThang.so;
            }
        }
        if (kq != null) {
            xuLyKetQua(kq);
        }
    }

    void xuLyKetQua(final VongQuayLogic.KetQua kq) {
        GHI.execute(() -> DatabaseVongQuay.xoaCuoc(this.loai));
        final String dv = donVi();
        if (kq.hoan) {
            for (final VongQuayLogic.NguoiCuoc n : kq.nguoi) {
                tra(n, n.so, "hoan " + this.ma + " (chi 1 nguoi)");
                SO_NGAY.hoan(ngay(), this.loai, n.ten, n.so);
                GHI.execute(() -> DatabaseVongQuay.ghiNgay(this.loai, n.ten, "", "", -1, -n.so));
                final Char c = online(n);
                if (c != null) {
                    bao(c, this.ten + ": hết giờ mà chỉ có bạn tham gia, đã hoàn lại " + VongQuayLogic.so(n.so) + " " + dv
                            + ". Lượt này không tính vào giới hạn ngày.");
                }
            }
        } else {
            final VongQuayLogic.NguoiCuoc w = kq.nguoiThang;
            tra(w, kq.tra, "thang " + this.ma + " hu " + kq.hu + " phi " + kq.phi);
            for (final VongQuayLogic.NguoiCuoc n : kq.nguoi) {
                final Char c = online(n);
                if (n != w && c != null) {
                    // thua -> thang: mot lan chuyen gia tri (nhat ky + danh dau nghi clone).
                    try {
                        ChongClone.ghi(c, w.charId, w.ten, ChongClone.KIEU_VONG_QUAY, n.so, this.ma + " hu " + kq.hu);
                    } catch (final Throwable ignored) {
                    }
                }
                if (c != null) {
                    bao(c, n == w
                            ? "Chúc mừng! Bạn thắng " + this.ten + ": nhận " + VongQuayLogic.so(kq.tra) + " " + dv + " (hũ "
                                    + VongQuayLogic.so(kq.hu) + ", phí " + VongQuayLogic.so(kq.phi) + ")."
                            : this.ten + ": " + w.ten + " thắng hũ " + VongQuayLogic.so(kq.hu) + " " + dv
                                    + ". Bạn đặt " + VongQuayLogic.so(n.so) + " " + dv + ", chúc may mắn lượt sau.");
                }
            }
            try {
                com.htbb.chongspam.ThongBaoGop.dua("nhan_thuong", "Chúc mừng " + w.ten + " thắng " + VongQuayLogic.so(kq.tra)
                        + " " + dv + " ở " + this.ten + ".");
            } catch (final Throwable ignored) {
            }
        }
        GHI.execute(() -> DatabaseVongQuay.ghiLichSu(this.loai, kq));
        try {
            Database.instance.saveOrtherLog("", kq.hoan ? "" : kq.nguoiThang.ten, "vong quay " + this.ma + (kq.hoan ? " hoan" : " thang")
                    + " hu " + kq.hu + " phi " + kq.phi + " tra " + kq.tongTra() + " nguoi " + kq.nguoi.size(), "xoso");
        } catch (final Throwable ignored) {
        }
    }

    /** Tra {@code so} cho {@code n}: online -> cong ngay + luu; offline -> tob_vong_quay_cho_nhan. */
    void tra(final VongQuayLogic.NguoiCuoc n, final long so, final String lyDo) {
        if (so <= 0L) {
            return;
        }
        final Char c = online(n);
        if (c != null) {
            if (this.laLuong) {
                c.addLuong((int) Math.min(Integer.MAX_VALUE, so));
            } else {
                c.addXu(so, "vong quay " + lyDo);
            }
            try {
                c.sendMessage(MessageCreator.createCharInventoryMessage(c, 0));
            } catch (final Exception ignored) {
            }
            GHI.execute(() -> luuNhanVat(c));
        } else {
            GHI.execute(() -> DatabaseVongQuay.congChoNhan(n.ten, this.laLuong ? 0L : so, this.laLuong ? so : 0L, lyDo));
        }
    }

    static Char online(final VongQuayLogic.NguoiCuoc n) {
        try {
            final Char c = CharManager.instance.getCharByCharName(n.ten);
            if (c != null && (n.charId <= 0 || c.charDBID == n.charId)) {
                return c;
            }
        } catch (final Throwable ignored) {
        }
        return null;
    }

    // ------------------------------------------------------------------ khoi dong / dang nhap

    /** Nap cuoc dang cho + so ngay tu CSDL, hoan cuoc cua he cu (goi luc khoi dong, truoc {@link #khoiDong}). */
    public static void napLucKhoiDong() {
        try {
            DatabaseVongQuay.taoBang();
            DatabaseVongQuay.chuyenCuocCu();
            final long bay = System.currentTimeMillis();
            for (final VongQuay v : THEO_MENU) {
                synchronized (v) {
                    for (final VongQuayLogic.NguoiCuoc n : DatabaseVongQuay.napCuoc(v.loai)) {
                        v.vong.them(n, bay);
                    }
                }
            }
            DatabaseVongQuay.napNgay(SO_NGAY, ngay());
        } catch (final Throwable t) {
            System.out.println("[VONG QUAY] napLucKhoiDong: " + t);
        }
    }

    /** Luong nen ket thuc luot (moi 500 ms). */
    public static synchronized void khoiDong() {
        if (daChay) {
            return;
        }
        daChay = true;
        final Thread t = new Thread(() -> {
            while (!AdminHandler.isStopServer) {
                final long bay = System.currentTimeMillis();
                for (final VongQuay v : THEO_MENU) {
                    try {
                        v.nhip(bay);
                    } catch (final Throwable e) {
                        e.printStackTrace();
                    }
                }
                try {
                    Thread.sleep(500L);
                } catch (final InterruptedException e) {
                    return;
                }
            }
        }, "vong-quay");
        t.setDaemon(true);
        t.start();
    }

    /** Nhan vat vua nap tu CSDL (CharNapDuLieu): cong tien thang / hoan luc offline. */
    public static void khiDangNhap(final Char p) {
        if (p == null || p.isBot != -1 || p.charname == null) {
            return;
        }
        try {
            final long[] r = DatabaseVongQuay.nhanChoNhan(p.charname);
            if (r == null) {
                return;
            }
            if (r[0] > 0L) {
                p.addXu(r[0], "vong quay cho nhan");
            }
            if (r[1] > 0L) {
                p.addLuong((int) Math.min(Integer.MAX_VALUE, r[1]));
            }
            Database.instance.saveOrtherLog("", p.charname, "vong quay cho nhan xu " + r[0] + " luong " + r[1], "xoso");
            GHI.execute(() -> luuNhanVat(p));
        } catch (final Throwable t) {
            System.out.println("[VONG QUAY] khiDangNhap: " + t);
        }
    }

    // ------------------------------------------------------------------ tien ich

    static void luuNhanVat(final Char p) {
        try {
            Database.instance.saveCharAuto(p);
        } catch (final Throwable ignored) {
        }
    }

    static String ngay() {
        return new SimpleDateFormat("yyyyMMdd").format(new Date());
    }

    static void bao(final Char p, final String m) {
        try {
            p.sendMessage(MessageCreator.createServerAlertMessage(m, ""));
        } catch (final Throwable ignored) {
        }
    }

    /** Chi test: trang thai luot hien tai. */
    public synchronized List<VongQuayLogic.NguoiCuoc> danhSachThu() {
        return this.vong.danhSach();
    }
}
