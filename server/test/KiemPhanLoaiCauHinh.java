import com.htbb.cauhinh.CauHinhTrungTam;
import com.htbb.cauhinh.DinhNghiaKhoa;
import com.htbb.cauhinh.TinhCauHinh;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

/**
 * PHAN LOAI CAU HINH (docs/cau-hinh.md, cot "lop" cua bang cau_hinh_danh_muc):
 *   chinh    - hien tren /admin/cau-hinh-he-thong, CSDL > server.ini > mac dinh
 *   co_dinh  - an; may that BO QUA dong CSDL: server.ini (khan cap) > gia tri co dinh
 *   luon_bat - an; nhu co_dinh, gia tri luon "1"
 * Goi tu KiemCauHinhTrungTam (cung nhom).
 */
public final class KiemPhanLoaiCauHinh {

    private KiemPhanLoaiCauHinh() {
    }

    static void chay(final DinhNghiaKhoa[] dm) {
        final Map<String, DinhNghiaKhoa> theo = new HashMap<String, DinhNghiaKhoa>();
        int chinh = 0;
        int coDinh = 0;
        int luonBat = 0;
        boolean luonBatDung = true;
        for (final DinhNghiaKhoa d : dm) {
            theo.put(d.khoa, d);
            if (DinhNghiaKhoa.LOP_CO_DINH.equals(d.lop())) {
                coDinh++;
            } else if (DinhNghiaKhoa.LOP_LUON_BAT.equals(d.lop())) {
                luonBat++;
                luonBatDung &= "co".equals(d.kieu) && "1".equals(d.macDinh);
            } else {
                chinh++;
            }
        }
        // <= 72: 0.9.6 them 3 khoa chinh cua the dat ten Pokemon (gia + muc + trong so muc - chu game: gia / ti le chinh tren web)
        // <= 77: quan ly thu them 6 khoa chinh (thu.o_co_ban / o_toi_da / tha_xu_* gom tran / ngay - chu game chinh tren web)
        // <= 78: them 2 khoa han muc dang ky (dang_ky.toi_da_gio / toi_da_ngay - open test: mang di dong chung IP);
        //        cung tran voi web/tests/cau-hinh-phan-loai.test.ts
        // <= 81: them 3 khoa Ve lang mien phi (ve_lang.moi_ngay / thang_gym / toi_da - chu game chinh luot tren web)
        // <= 95: them nhom hlv_ai 14 khoa (huan luyen vien AI: so luong / lich / ti le / cap / ban do / hoat dong + 6 luat
        //        chong lam phat - chu game 31/10/2026: chinh tren trang /admin/hlv-ai)
        // <= 97: hlv_ai them doi_bot_toi_da + tien_to_ten (chu game 31/10/2026: doi tien to ten bot tren trang /admin/hlv-ai)
        // <= 102: doi luong (chu game 31/10/2026): doi_luong.ti_le, doi_luong_xu.*, san_luong.* (5 khoa kinh_te)
        // <= 105: +3 khoa exp.nang_luong_{thanh_vien,the_thang,ve_mua}_phut (chu game 31/10/2026: thuong nang luong theo goi)
        // <= 106: +tang_toc.bua_ngay_toi_da (Bua the luc khong gioi han, admin dat lai tran)
        // <= 108: +tang_toc.bua_exp_phan_tram / bua_xu_phan_tram (bac bua 50% / 50%, chinh tren web)
        // <= 109: hlv_ai.cap_theo_nguoi (cap muc tieu bot theo nguoi choi that, 31/10/2026)
        // <= 114: PK 3 vs 3 buoi toi (chu game 05/11/2026): pk3v3.bat / gio_mo / gio_dong / tran_ngay / luong_thang (su_kien)
        Kiem.dung("phan loai: chi con it khoa chinh tren web (<= 114), con lai an", chinh <= 114 && chinh >= 40);
        Kiem.dung("phan loai: co khoa co dinh + luon bat", coDinh > 100 && luonBat >= 15);
        Kiem.dung("luon_bat: moi khoa la co, gia tri 1", luonBatDung);

        // Tinh nang cot loi luon bat, gia tri san xuat (truoc day den tu server.ini cua VPS)
        for (final String k : new String[]{"sv.lau_dai", "sv.he_sinh_thai", "sv.spam.bat", "chi_so.bat", "gym.bat",
            "ham_nguc.bat", "pvp.tinh_diem.bat", "tu_dong.bat", "hoi_sinh.bat", "sv.chiSoMayChu"}) {
            final DinhNghiaKhoa d = theo.get(k);
            Kiem.dung("luon bat: " + k, d != null && DinhNghiaKhoa.LOP_LUON_BAT.equals(d.lop()) && "1".equals(d.macDinh));
        }
        // Khoa ky thuat co dinh
        for (final String k : new String[]{"sv.maxConnPerIp", "sv.connRateMax", "sv.dosBanSec", "sv.tranGoiThuong", "sv.log.muc",
            "sv.spam.chat.gan", "chi_so.tran_chi_mang", "tu_dong.nhip_danh", "thue.cho", "sv.tieuNguongMau", "sv.testMode",
            "sv.isSuKienTet2017", "hoi_sinh.he_so_lap"}) {
            final DinhNghiaKhoa d = theo.get(k);
            Kiem.dung("co dinh (an): " + k, d != null && DinhNghiaKhoa.LOP_CO_DINH.equals(d.lop()) && d.laAn());
        }
        // Khoa nguoi van hanh chinh: van hien
        for (final String k : new String[]{"sv.tileXu", "sv.tileExpLv50", "bao_tri.bat", "sv.gioBaoTri", "sv.nap_tien",
            "nap.luong.bat", "thanh_vien.gia", "kich_hoat.chan_dang_nhap", "tu_dong.gio_moi_ngay", "hoi_sinh.xu_phan_tram_gio",
            "dang_ky.web.bat", "sv.dangKyTrongGame", "sv.website", "sv.linkTaiGame", "giftcode.ngan_sach_thang", "sv.gioChiemThanh"}) {
            final DinhNghiaKhoa d = theo.get(k);
            Kiem.dung("chinh tren web: " + k, d != null && !d.laAn());
        }
        // Gia tri san xuat (cau_hinh_hieu_luc tren VPS 19/10/2026) giu nguyen sau khi khoa
        Kiem.bang("co dinh sv.chiaXuRoi = gia tri dang chay", "30", theo.get("sv.chiaXuRoi").macDinh);
        Kiem.bang("co dinh sv.ExpQua = gia tri dang chay", "1", theo.get("sv.ExpQua").macDinh);
        Kiem.bang("mac dinh sv.tileXu = gia tri dang chay", "4", theo.get("sv.tileXu").macDinh);

        // ---- May that: bo qua CSDL cua khoa an ----
        final Map<String, String> db = new HashMap<String, String>();
        db.put("sv.lau_dai", "0");
        db.put("sv.spam.chat.gan", "1/1");
        db.put("sv.chiaXuRoi", "30");
        db.put("sv.tileXu", "7");
        TinhCauHinh.KetQua k = TinhCauHinh.tinh(dm, null, db, null, false);
        Kiem.bang("may that: sv.lau_dai CSDL=0 bi bo qua -> 1", "1", k.hieuLuc.get("sv.lau_dai"));
        Kiem.bang("... nguon co_dinh", TinhCauHinh.NGUON_CO_DINH, k.nguon.get("sv.lau_dai"));
        Kiem.bang("... gia tri co dinh vao Properties cu (TeamServer doc sv.lau_dai)", "1", k.tatCa.get("sv.lau_dai"));
        Kiem.dung("... ghi canh bao de trang thay", k.loi.containsKey("sv.lau_dai"));
        Kiem.bang("may that: nhip chat CSDL bi bo qua", theo.get("sv.spam.chat.gan").macDinh, k.hieuLuc.get("sv.spam.chat.gan"));
        Kiem.sai("dong CSDL bang dung gia tri co dinh -> khong canh bao", k.loi.containsKey("sv.chiaXuRoi"));
        Kiem.bang("khoa chinh van doc CSDL", "7", k.hieuLuc.get("sv.tileXu"));

        // ---- server.ini van doi duoc khi khan cap ----
        final Map<String, String> ini = new HashMap<String, String>();
        ini.put("sv.he_sinh_thai", "0");
        k = TinhCauHinh.tinh(dm, ini, db, null, false);
        Kiem.bang("khan cap: server.ini tat sv.he_sinh_thai", "0", k.hieuLuc.get("sv.he_sinh_thai"));
        Kiem.bang("... nguon ini", TinhCauHinh.NGUON_INI, k.nguon.get("sv.he_sinh_thai"));

        // ---- May thu (sv.cho_phep_thu_nghiem=1): doc CSDL nhu cu ----
        k = TinhCauHinh.tinh(dm, null, db, null, true);
        Kiem.bang("may thu: khoa an doc CSDL", "1/1", k.hieuLuc.get("sv.spam.chat.gan"));

        // ---- Ghi (Swing / lenh trong game): tu choi khoa an tren may that ----
        CauHinhTrungTam.datChoTest(null, null, false);
        final Map<String, String> g = new HashMap<String, String>();
        g.put("sv.spam.chat.gan", "3/10");
        final CauHinhTrungTam.KetQuaGhi kq = CauHinhTrungTam.ghi(g, "test", "test", "");
        Kiem.sai("ghi khoa co dinh tren may that bi tu choi", kq.ok);
        Kiem.dung("... bao dung khoa", kq.loi.containsKey("sv.spam.chat.gan"));
        Kiem.bang("doc khoa luon bat khi khong co CSDL / ini", "1", CauHinhTrungTam.chuoi("sv.lau_dai"));

        // ---- CSDL day du + nang cap mang phan loai ----
        Kiem.dung("htbb.sql: cau_hinh_danh_muc co cot lop + an", DayDu.cot("cau_hinh_danh_muc").contains("lop")
                && DayDu.cot("cau_hinh_danh_muc").contains("an"));
        Kiem.dung("htbb.sql: co bang cau_hinh_nhom (cot an)", DayDu.cot("cau_hinh_nhom").contains("an"));
        Kiem.bang("htbb.sql: sv.lau_dai lop luon_bat, an = 1", "luon_bat|1",
                DayDu.giaTri("cau_hinh_danh_muc", "khoa", "sv.lau_dai", "lop") + "|" + DayDu.giaTri("cau_hinh_danh_muc", "khoa", "sv.lau_dai", "an"));
        final String nc = DayDu.nangCap();
        Kiem.dung("sql/htbb.sql tao lai cau_hinh_danh_muc co lop + an", nc.contains("`lop` VARCHAR(10)") && nc.contains("`an` TINYINT"));
        final String ts = doc("../web/shared/utils/cauHinhTrungTam.gen.ts");
        Kiem.dung("web gen.ts mang lop", ts.contains("lop: \"luon_bat\"") && ts.contains("lop: \"co_dinh\""));
        final String md = doc("../docs/cau-hinh.md");
        Kiem.dung("docs/cau-hinh.md co bang phan loai", md.contains("**Cố định** (`co_dinh`)") && md.contains("Khoá ẩn: cố định & luôn bật"));
    }

    private static String doc(final String f) {
        try {
            return new String(Files.readAllBytes(Paths.get(f)), StandardCharsets.UTF_8);
        } catch (final IOException e) {
            return "";
        }
    }
}
