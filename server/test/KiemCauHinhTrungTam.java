import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.htbb.cauhinh.CauHinhTrungTam;
import com.htbb.cauhinh.DinhNghiaKhoa;
import com.htbb.cauhinh.KiemGiaTri;
import com.htbb.cauhinh.NgheThayDoi;
import com.htbb.cauhinh.TinhCauHinh;

/**
 * Cau hinh trung tam (com.htbb.cauhinh): thu tu uu tien CSDL > ini > mac dinh, kiem gia tri, tu choi gia tri
 * sai (giu gia tri tot gan nhat), khoa thu nghiem bi khoa o may that, khoa khoi dong lai dong bang + cho,
 * nap lai nong bao dung nguoi nghe, danh muc trong CSDL day du (cau_hinh_danh_muc) khop danh muc dang dung.
 *
 * Ham chi-cho-test cua CauHinhTrungTam: datChoTest / napLaiChoTest / xoaNgheChoTest (khong cham CSDL).
 */
public final class KiemCauHinhTrungTam {

    private KiemCauHinhTrungTam() {
    }

    static Map<String, String> m(final String... kv) {
        final Map<String, String> r = new HashMap<String, String>();
        for (int i = 0; i + 1 < kv.length; i += 2) {
            r.put(kv[i], kv[i + 1]);
        }
        return r;
    }

    public static void chay() {
        Kiem.nhom("Cau hinh trung tam - uu tien, kiem, nap lai nong, khoi dong lai");
        final DinhNghiaKhoa[] dm = CauHinhTrungTam.danhMuc();
        Kiem.dung("danh muc co tren 100 khoa", dm.length > 100);

        // ---- 1. Thu tu uu tien ----
        TinhCauHinh.KetQua k = TinhCauHinh.tinh(dm, m("sv.tileXu", "40", "sv.tileLuong", "15"), m("sv.tileXu", "60"), null, false);
        Kiem.bang("CSDL > ini", "60", k.hieuLuc.get("sv.tileXu"));
        Kiem.bang("nguon CSDL", "db", k.nguon.get("sv.tileXu"));
        Kiem.bang("ini khi CSDL khong co", "15", k.hieuLuc.get("sv.tileLuong"));
        Kiem.bang("nguon ini", "ini", k.nguon.get("sv.tileLuong"));
        Kiem.bang("mac dinh khi khong co o dau", "14", k.hieuLuc.get("sv.tilePotion"));
        Kiem.bang("nguon mac dinh", "mac_dinh", k.nguon.get("sv.tilePotion"));
        Kiem.sai("khoa mac dinh KHONG dua vao Properties cu (ma cu tu dung mac dinh rieng)", k.tatCa.containsKey("sv.tilePotion"));
        Kiem.bang("Properties cu thay gia tri CSDL", "60", k.tatCa.get("sv.tileXu"));

        // ---- 2. Khoa ngoai danh muc di thang ----
        k = TinhCauHinh.tinh(dm, m("db.host", "x:1"), m("boss.the_gioi.mau", "41", "lau_dai.thang_exp", "moi"), null, false);
        Kiem.bang("khoa de so lieu ngoai danh muc di thang", "41", k.tatCa.get("boss.the_gioi.mau"));
        Kiem.bang("khoa ini khac giu nguyen", "x:1", k.tatCa.get("db.host"));

        // ---- 3. Gia tri CSDL sai bi tu choi ----
        k = TinhCauHinh.tinh(dm, m("sv.tileXu", "40"), m("sv.tileXu", "150"), null, false);
        Kiem.bang("CSDL sai luc khoi dong -> ini", "40", k.hieuLuc.get("sv.tileXu"));
        Kiem.dung("ghi ly do tu choi", k.loi.containsKey("sv.tileXu"));
        final TinhCauHinh.KetQua tot = TinhCauHinh.tinh(dm, m("sv.tileXu", "40"), m("sv.tileXu", "70"), null, false);
        k = TinhCauHinh.tinh(dm, m("sv.tileXu", "40"), m("sv.tileXu", "abc"), tot, false);
        Kiem.bang("CSDL sai luc dang chay -> giu gia tri tot gan nhat", "70", k.hieuLuc.get("sv.tileXu"));
        Kiem.bang("nguon giu_cu", "giu_cu", k.nguon.get("sv.tileXu"));
        k = TinhCauHinh.tinh(dm, m("sv.maxConnPerIp", "abc"), null, null, false);
        Kiem.bang("server.ini sai luat van giu (hanh vi cu)", "abc", k.hieuLuc.get("sv.maxConnPerIp"));
        Kiem.dung("... nhung co canh bao", k.loi.containsKey("sv.maxConnPerIp"));

        // ---- 4. Khoa thu nghiem ----
        k = TinhCauHinh.tinh(dm, null, m("sv.testMode", "1", "sv.tua_nhanh_ngay", "30"), null, false);
        Kiem.bang("may that: testMode tu CSDL bi khoa", "0", k.hieuLuc.get("sv.testMode"));
        Kiem.bang("may that: tua nhanh bi khoa", "0", k.hieuLuc.get("sv.tua_nhanh_ngay"));
        Kiem.dung("ghi ly do khoa thu nghiem", k.loi.containsKey("sv.testMode"));
        k = TinhCauHinh.tinh(dm, null, m("sv.testMode", "1", "sv.tua_nhanh_ngay", "30"), null, true);
        Kiem.bang("may thu (sv.cho_phep_thu_nghiem=1): duoc bat", "1", k.hieuLuc.get("sv.testMode"));
        Kiem.bang("may thu: tua nhanh 30", "30", k.hieuLuc.get("sv.tua_nhanh_ngay"));
        k = TinhCauHinh.tinh(dm, null, m("sv.testMode", "0"), null, false);
        Kiem.sai("gia tri mac dinh cua khoa thu nghiem van nhan", k.loi.containsKey("sv.testMode"));

        // ---- 5. Khoa khoi dong lai ----
        final TinhCauHinh.KetQua luc0 = TinhCauHinh.tinh(dm, m("sv.nap_tien", "0"), m("sv.nap_tien", "1"), null, false);
        Kiem.bang("khoi dong: sv.nap_tien lay CSDL", "1", luc0.hieuLuc.get("sv.nap_tien"));
        k = TinhCauHinh.tinh(dm, m("sv.nap_tien", "0"), m("sv.nap_tien", "0", "sv.tileXu", "9"), luc0, false);
        Kiem.bang("dang chay: sv.nap_tien DONG BANG", "1", k.hieuLuc.get("sv.nap_tien"));
        Kiem.bang("... gia tri moi cho khoi dong lai", "0", k.choKhoiDong.get("sv.nap_tien"));
        Kiem.bang("... khoa ap dung ngay van doi", "9", k.hieuLuc.get("sv.tileXu"));
        k = TinhCauHinh.tinh(dm, m("sv.nap_tien", "0"), m("sv.nap_tien", "1"), luc0, false);
        Kiem.sai("doi lai nhu cu -> het cho", k.choKhoiDong.containsKey("sv.nap_tien"));

        // ---- 6. Khoa ngoai bang cau_hinh khong do may chu doc ----
        k = TinhCauHinh.tinh(dm, null, m("tran_tuan_vnd", "9"), null, false);
        Kiem.sai("khoa nap_cau_hinh khong nam trong hieu luc may chu", k.hieuLuc.containsKey("tran_tuan_vnd"));
        Kiem.sai("khoa env khong nam trong hieu luc may chu", k.hieuLuc.containsKey("HTBB_NAP_BAT"));

        // ---- 7. Nap lai nong + nguoi nghe ----
        CauHinhTrungTam.xoaNgheChoTest();
        final List<String> nghe = new ArrayList<String>();
        CauHinhTrungTam.dangKyNghe(new String[]{"sv.spam."}, new NgheThayDoi() {
            public void khiDoi(final Set<String> khoa, final CauHinhTrungTam.AnhChup moi) {
                nghe.add(String.valueOf(khoa) + "=" + moi.chuoi("sv.spam.chat.gan"));
            }
        }, "test");
        final List<String> ngheTat = new ArrayList<String>();
        CauHinhTrungTam.dangKyNghe(null, new NgheThayDoi() {
            public void khiDoi(final Set<String> khoa, final CauHinhTrungTam.AnhChup moi) {
                ngheTat.add(String.valueOf(khoa));
            }
        }, "test-tat");
        // sv.spam.* la khoa CO DINH: may that bo qua CSDL -> nap lai nong thu tren may thu (true).
        CauHinhTrungTam.datChoTest(m("sv.spam.chat.gan", "5/10"), m("sv.tileXu", "20"), true);
        Kiem.bang("doc ngay sau khi nap", "20", CauHinhTrungTam.chuoi("sv.tileXu"));
        Set<String> doi = CauHinhTrungTam.napLaiChoTest(m("sv.tileXu", "25"), 2L);
        Kiem.bang("nap lai: chi sv.tileXu doi", "[sv.tileXu]", String.valueOf(doi));
        Kiem.bang("nguoi nghe sv.spam. khong bi goi", 0, Integer.valueOf(nghe.size()));
        Kiem.bang("nguoi nghe moi khoa duoc goi", "[[sv.tileXu]]", String.valueOf(ngheTat));
        Kiem.bang("gia tri moi ap dung", "25", CauHinhTrungTam.chuoi("sv.tileXu"));
        doi = CauHinhTrungTam.napLaiChoTest(m("sv.tileXu", "25", "sv.spam.chat.gan", "3/10"), 3L);
        Kiem.bang("nguoi nghe sv.spam. nhan dung khoa + gia tri moi", "[[sv.spam.chat.gan]=3/10]", String.valueOf(nghe));
        CauHinhTrungTam.napLaiChoTest(m("sv.tileXu", "25", "sv.spam.chat.gan", "loi"), 4L);
        Kiem.bang("gia tri sai -> khong goi nguoi nghe, giu 3/10", 1, Integer.valueOf(nghe.size()));
        Kiem.bang("... van doc 3/10", "3/10", CauHinhTrungTam.chuoi("sv.spam.chat.gan"));
        Kiem.dung("co / so doc kieu", CauHinhTrungTam.so("sv.tileXu", -1) == 25L && !CauHinhTrungTam.co("sv.testMode"));
        CauHinhTrungTam.xoaNgheChoTest();

        // ---- 7b. Phan loai: khoa an (co dinh / luon bat) ----
        KiemPhanLoaiCauHinh.chay(dm);
        CauHinhTrungTam.datChoTest(null, null, false);

        // ---- 8. Ghi: kiem truoc, tat ca hoac khong ----
        final CauHinhTrungTam.KetQuaGhi g = CauHinhTrungTam.ghi(m("sv.tileXu", "500", "sv.tileLuong", "5"), "test", "test", "");
        Kiem.sai("ghi co gia tri sai -> tu choi ca lo (khong cham CSDL)", g.ok);
        Kiem.dung("... bao dung khoa sai", g.loi.containsKey("sv.tileXu") && !g.loi.containsKey("sv.tileLuong"));
        final CauHinhTrungTam.KetQuaGhi g2 = CauHinhTrungTam.ghi(m("HTBB_NAP_BAT", "1"), "test", "test", "");
        Kiem.sai("khoa env khong ghi duoc", g2.ok);

        // ---- 9. Bo vi du dung chung voi web ----
        int soViDu = 0;
        for (final String dong : docDong("../tools/cauhinh/vi_du_kiem.tsv")) {
            if (dong.startsWith("#") || dong.trim().isEmpty()) {
                continue;
            }
            final String[] c = dong.split("\t", -1);
            if (c.length < 3) {
                continue;
            }
            final DinhNghiaKhoa d = CauHinhTrungTam.dinhNghia(c[0]);
            Kiem.dung("vi du: co khoa " + c[0], d != null);
            if (d == null) {
                continue;
            }
            final KiemGiaTri.KetQua r = KiemGiaTri.kiem(d, c[1]);
            final String thucTe = r.dung() ? "ok:" + r.giaTri : "loi";
            Kiem.bang("vi du " + c[0] + " = \"" + c[1] + "\"", c[2], thucTe);
            soViDu++;
        }
        Kiem.dung("doc duoc bo vi du (tren 30 dong)", soViDu > 30);

        // ---- 10. Moi macDinh trong danh muc phai hop le theo chinh luat cua no ----
        for (final DinhNghiaKhoa d : dm) {
            if (d.trongCauHinh()) {
                Kiem.dung("mac dinh hop le: " + d.khoa, KiemGiaTri.kiem(d, d.macDinh).dung());
            }
        }

        // ---- 11. Danh muc trong CSDL day du = danh muc dang dung ----
        final List<String> khoaSql = new ArrayList<String>();
        for (final java.util.Map<String, String> r : DayDu.dong("cau_hinh_danh_muc")) {
            khoaSql.add(r.get("khoa"));
        }
        final List<String> khoaJava = new ArrayList<String>();
        for (final DinhNghiaKhoa d : dm) {
            khoaJava.add(d.khoa);
        }
        java.util.Collections.sort(khoaSql);
        final List<String> khoaJavaSap = new ArrayList<String>(khoaJava);
        java.util.Collections.sort(khoaJavaSap);
        Kiem.bang("danh muc nap tu SQL = dong cau_hinh_danh_muc cua sql/htbb_day_du.sql", khoaSql, khoaJavaSap);
        for (final String b : new String[]{"cau_hinh_phien_ban", "cau_hinh_lich_su", "cau_hinh_ap_dung", "cau_hinh_hieu_luc",
            "cau_hinh_khoi_dong_lai", "cau_hinh_danh_muc", "cau_hinh_nhom"}) {
            Kiem.dung("htbb_day_du.sql co bang " + b, DayDu.coBang(b));
            Kiem.dung("sql/nang_cap.sql tao bang " + b, DayDu.nangCap().contains("CREATE TABLE IF NOT EXISTS `" + b + "`"));
        }

        // ---- 12. Noi day ----
        final String ts = doc("src/server/TeamServer.java");
        final int iLink = ts.indexOf("Database.setLink1(");
        final int iKhoi = ts.indexOf("CauHinhTrungTam.khoiTao(p)");
        final int iCo = ts.indexOf("LauDai.dat(");
        Kiem.dung("TeamServer: khoiTao sau setLink1, truoc khi doc co che do", iLink > 0 && iKhoi > iLink && iCo > iKhoi);
        final String ch = doc("src/server/CauHinh.java");
        Kiem.dung("CauHinh.napLai uy quyen cho trung tam", ch.contains("CauHinhTrungTam.napLaiNgay("));
        Kiem.dung("CauHinh.luuNhieu tang phien ban", ch.contains("CauHinhTrungTam.tangPhienBan("));
    }

    static String doc(final String f) {
        try {
            return new String(Files.readAllBytes(Paths.get(f)), StandardCharsets.UTF_8);
        } catch (final IOException e) {
            return "";
        }
    }

    static List<String> docDong(final String f) {
        try {
            return Files.readAllLines(Paths.get(f), StandardCharsets.UTF_8);
        } catch (final IOException e) {
            return new ArrayList<String>();
        }
    }
}
