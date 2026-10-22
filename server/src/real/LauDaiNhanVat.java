package real;

import com.htbb.pokemon.CapDoLauDai;
import com.htbb.pokemon.CuonHut;
import com.htbb.pokemon.HoSoLauDai;
import com.htbb.pokemon.LauDai;
import com.htbb.pokemon.NangLuong;
import com.htbb.pokemon.NhiemVuLauDai;
import com.htbb.pokemon.ThongSoLauDai;
import data.Database;
import data.DatabaseLauDai;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Noi trang thai dai han cua nhan vat (com.htbb.pokemon.HoSoLauDai) vao game - giong
 * PokedexPokemon: logic thuan o com.htbb.pokemon, CSDL o data.DatabaseLauDai, lop nay noi.
 *
 * Diem goi (moi diem deu kiem LauDai.bat() truoc):
 *   - CharNapDuLieu (nap nhan vat)       -> {@link #khiNap}
 *   - CharCapNhat (khoi doi ngay)        -> {@link #khiDoiNgay}
 *   - Database.saveChar / saveCharAuto   -> {@link #luu}
 *   - ExpLauDai / Monster.onDropItem     -> {@link #nangLuong}
 *
 * Tat co: moi ham khong lam gi.
 */
public final class LauDaiNhanVat {

    private static final ConcurrentHashMap<Integer, HoSoLauDai> CACHE = new ConcurrentHashMap<Integer, HoSoLauDai>();

    /** Cap trung vi may chu (nguoi hoat dong 7 ngay), tinh lai moi gio (BaoTriDaily). */
    static volatile int capTrungVi = 0;

    private LauDaiNhanVat() {
    }

    /** Ho so cua nhan vat (nap tu CSDL lan dau), null neu tat co / bot / loi CSDL. */
    public static HoSoLauDai lay(final Char p) {
        if (!LauDai.bat() || p == null || p.charDBID <= 0) {
            return null;
        }
        final Integer k = Integer.valueOf(p.charDBID);
        HoSoLauDai h = CACHE.get(k);
        if (h != null) {
            return h;
        }
        final String s = DatabaseLauDai.napHoSo(p.charDBID);
        if (s == null) {
            return null; // loi CSDL: khong tao ho so trong, tranh ghi de ban that
        }
        h = HoSoLauDai.giaiMa(s);
        final HoSoLauDai cu = CACHE.putIfAbsent(k, h);
        return cu != null ? cu : h;
    }

    /** Goi tu Database.saveChar/saveCharAuto: chi ghi khi co doi. */
    public static void luu(final Char p) {
        if (!LauDai.bat() || p == null || p.charDBID <= 0) {
            return;
        }
        final HoSoLauDai h = CACHE.get(Integer.valueOf(p.charDBID));
        if (h == null) {
            return;
        }
        TangTocGame.ghiMocLuu(h, com.htbb.pokemon.DongHo.bay()); // moc "thoat" cho uy thac luyen tap
        if (!h.ban) {
            return;
        }
        final String s;
        synchronized (h) {
            s = h.maHoa();
            h.ban = false;
        }
        if (!DatabaseLauDai.luuHoSo(p.charDBID, s)) {
            h.ban = true;
        }
    }

    // ------------------------------------------------------------------ nap / doi ngay

    /** Nhan vat vua nap tu CSDL. */
    public static void khiNap(final Char p) {
        if (!LauDai.bat()) {
            return;
        }
        try {
            final HoSoLauDai h = lay(p);
            if (h == null) {
                return;
            }
            final int hom = LauDai.ngayHienTai();
            synchronized (h) {
                if (h.ngayDauTien == 0) {
                    h.ngayDauTien = Math.max(1, hom + 1); // +1: ngay 0 van khac "chua dat"
                }
                if (h.ngayGanNhat > 0 && hom + 1 - h.ngayGanNhat >= 14) {
                    h.quayLaiDenNgay = hom + 1 + 14; // nguoi quay lai: 14 ngay uu dai
                }
                h.ngayGanNhat = hom + 1;
                h.ban = true;
            }
            NhiemVuLauDaiGame.khiNap(p, h);
            HuongDanTanThuGame.khiNap(p); // buoc huong dan luu theo du lieu buoc cu -> nhan lai buoc do
            TangTocGame.khiNap(p, h);
        } catch (final Exception e) {
            System.out.println("LauDaiNhanVat.khiNap: " + e);
        }
    }

    /**
     * Khoi doi ngay cua CharCapNhat. Lich dang nhap 7 ngay + lich thang tinh theo TAI KHOAN
     * (QuaHoatDong - chan acc phu), khong con theo nhan vat (yeu cau 17/10/2026).
     */
    public static void khiDoiNgay(final Char p) {
        if (!LauDai.bat()) {
            return;
        }
        try {
            QuaHoatDong.khiDoiNgay(p);
        } catch (final Exception e) {
            System.out.println("LauDaiNhanVat.khiDoiNgay: " + e);
        }
    }

    // ------------------------------------------------------------------ he so

    /** Ghi nhan mot lan ha quai va tra ve he so nang luong (%). Loi / tat co = 100. */
    public static int nangLuong(final Char p) {
        final HoSoLauDai h = lay(p);
        if (h == null) {
            return 100;
        }
        return NangLuong.ghiNhan(h, com.htbb.pokemon.DongHo.bay(),
                LauDai.so(LauDai.K_NANG_LUONG_PHUT, ThongSoLauDai.NANG_LUONG_PHUT_NGAY),
                LauDai.so(LauDai.K_NANG_LUONG_TICH, ThongSoLauDai.NANG_LUONG_PHUT_TICH_TOI_DA));
    }

    /**
     * He so nang luong hien tai cho XU / da / do roi, khong ghi nhan (de roi xu cung lan ha da ghi o EXP). Khong
     * tinh Bua the luc (vat pham tang toc chi cho EXP).
     */
    public static int nangLuongHienTai(final Char p) {
        final HoSoLauDai h = lay(p);
        return h == null ? 100 : NangLuong.heSoXuHienTai(h, com.htbb.pokemon.DongHo.bay());
    }

    /** Bat kip (%): vao sau ngay 30 hoac dang trong 14 ngay quay lai, va cap < trung vi - 5. */
    public static int batKip(final Char p) {
        final HoSoLauDai h = lay(p);
        if (h == null) {
            return 100;
        }
        final int hom = LauDai.ngayHienTai() + 1;
        final boolean du = h.ngayDauTien > 31 || h.quayLaiDenNgay >= hom;
        return CapDoLauDai.heSoBatKip(p.lvDetail.lv, capTrungVi, du);
    }

    /** Phan nghin tan cong (loai 0) / HP (loai 1) tu tinh thong. */
    public static int tinhThongPhanNghin(final Char p, final int loai) {
        if (!LauDai.bat()) {
            return 0;
        }
        final HoSoLauDai h = lay(p);
        if (h == null || h.ttLoai != loai) {
            return 0;
        }
        return CapDoLauDai.tinhThongPhanNghin(h.ttDiem);
    }

    /** Cong exp thua (vuot tran) vao tinh thong; bao khi len diem. */
    static void congTinhThong(final Char p, final long thua, final int tran) {
        final HoSoLauDai h = lay(p);
        if (h == null || thua <= 0L) {
            return;
        }
        final int diemCu;
        final long[] kq;
        synchronized (h) {
            diemCu = h.ttDiem;
            kq = CapDoLauDai.congTinhThong(h.ttDiem, h.ttQuy, thua, tran, LauDai.mua(LauDai.ngayHienTai()));
            h.ttDiem = (int) kq[0];
            h.ttQuy = kq[1];
            h.ban = true;
        }
        if (kq[0] > diemCu) {
            try {
                p.sendMessage(MessageCreator.createServerAlertAutoOffMessage(
                        "Tinh thông +" + (kq[0] - diemCu) + " (hiện " + kq[0] + " điểm)."));
                p.calculateAttrib();
            } catch (final Exception ignored) {
            }
        }
    }

    /** Job moi gio (BaoTriDaily): tinh lai cap trung vi. */
    public static void capNhatTrungVi() {
        if (!LauDai.bat()) {
            return;
        }
        final int v = DatabaseLauDai.capTrungVi();
        if (v > 0) {
            capTrungVi = v;
        }
    }

    /** Thong bao tu tat (nuot IOException). */
    static void bao(final Char p, final String s) {
        try {
            p.sendMessage(MessageCreator.createServerAlertAutoOffMessage(s));
        } catch (final Exception ignored) {
        }
    }

    /** Tuan hien tai cho ve chuong / nhiem vu tuan. */
    static int tuanNay() {
        return NhiemVuLauDai.tuan(com.htbb.pokemon.DongHo.bay());
    }
}
