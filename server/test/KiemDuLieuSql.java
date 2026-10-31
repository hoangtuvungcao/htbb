import com.htbb.cauhinh.CauHinhTrungTam;
import com.htbb.cauhinh.DinhNghiaKhoa;
import com.htbb.dulieu.BangSql;
import com.htbb.dulieu.DocDumpSql;
import com.htbb.dulieu.DuLieuSql;
import com.htbb.dulieu.JsonNho;
import com.htbb.dulieu.NguonDuLieu;
import com.htbb.pokemon.BanDoSinhThai;
import com.htbb.pokemon.GiftcodeAnToanDuLieu;
import com.htbb.pokemon.SuKienLauDai;
import com.htbb.pokemon.ThongSoLauDai;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * P1: du lieu thiet ke doc tu SQL la NGUON DUY NHAT (com.htbb.dulieu.DuLieuSql, docs/kien-truc.md muc 4.4).
 *
 *   1. Ban nap tu phan A cua sql/htbb.sql (Kiem.main nap truoc moi nhom, giong may chu nap tu CSDL) BANG DUNG ban chep
 *      Java da xoa: van tay SHA-256 duoi day tinh tu ban chep Java NGAY TRUOC khi xoa (19/10/2026), cung cach noi chuoi.
 *      Lech = so lieu game da doi; neu co y doi (sua tren trang quan tri / CSDL roi xuat lai phan A) thi
 *      cap nhat van tay.
 *   2. sql/ chi co MOT tep sql/htbb.sql: phan A (CSDL day du) chi tao bang IF NOT EXISTS va chep du lieu vao CSDL MOI;
 *      phan B (nang cap) chua CUNG dong voi phan A cho cac bang may chu nap luc khoi dong; khong pha bang nguoi choi.
 *   3. Thieu bang / bang rong / sai hinh dang: loi ro rang, bang quan trong -> loi (server dung), KHONG gan nua voi.
 *   4. Ban chep Java da xoa that; TeamServer nap dung cho; bo doc dump + JSON dung.
 */
public final class KiemDuLieuSql {

    /** Van tay ban chep Java (truoc khi xoa). Ban do 40 / 42 khong co diem cau (khong cau, khong hien goi y). */
    static final String VT_CAP_DO = "5172ffb9c4135d6fa35d7a8e9bb3ed43a6206e18381c6499da72e41e83c5aec8";
    static final String VT_NANG_CAP = "791217844fa68ee500a583ae9dab389feb8cfe40641a2fe12593159c9e4ac9a2";
    static final String VT_KINH_TE = "9eefa10bbf60d4a91ba894e652725058c3c5d169dd00b9c1dba3c0a030a75f6e";
    static final String VT_LICH = "78ad902ac9e07511c0e989c00025a6a137a2f7851504a3b462fc5fb6fba921fd";
    static final String VT_TRANG_BI = "18c33f993af029df538f5125e956287de982d6830694fccd85006ead057e3a3e";
    /** DanhMucSinh cua main cfff771b (213 khoa / 15 nhom: them nhom chong_clone; co phan loai lop), tinh cung cach noi
     *  chuoi (KE CA lop) ngay truoc khi bo ban chep Java luc gop. Lich su: P1 185 khoa 382a04b3..., 0.9.2 5934a2e6...,
     *  0.9.4 (203 khoa, co lop) e6505ed5..., 213 khoa 9be03a6a.... khoa-map: +ban_do.khoa.bat (214 khoa); gop main 0cbf73ae: +nhom vong_quay 26 khoa (240 / 16); gop main bda0e901: +nhom cuoc_xu (256 / 17); gop main 1a705be6: 269 khoa main + ban_do.khoa.bat (luon_bat, an) = 270 / 17; ban-098 (0.9.8): +nhom hoc_chieu 5 khoa, +nhom trang_bi 21 khoa,
     *  +8 khoa pho_ban.* / cup.* (nhom pvp) = 305 / 19 (moi khoa moi deu an, 70 khoa web giu nguyen); +chong_clone.ban_npc_khoa_phan_tram = 306; bat-trade +10 (bat.*, trao_doi.*), kenh-chat +10 (sv.chat.*) = 326, pvp-keo +4 (pvp.ngam_tay*) = 330, bat-trade cho +10 (cho_pokemon.*) = 340, ten-pet +10 (dat_ten_pet.*, an) = 350 / 20, loai-day-du +8 (loai.*, nhom loai) = 358 / 21, do-trang-phuc +2 (do_moi.bat, trang_phuc.bat, luon_bat) = 360 / 21; dong bo 0.9.8: bo sv.chat.thuChoToiDa / thuChoNgay (chat chi trong phien) = 358 / 21; loai.doi_thuong_phan_tram 30 -> 5 (chi Pokemon hoang da bat duoc);
     *  quan-ly-thu: +6 khoa thu.* (o thu + xu tha + tran xu tha / ngay, nhom kinh_te, chinh) = 364 / 21;
     *  han-muc-dang-ky: +2 khoa dang_ky.toi_da_gio / toi_da_ngay (nhom bao_tri, chinh, ca_hai) = 366 / 21;
     *  doi-tuan-0500 (0.9.14): mo_ta sv.gioBaoTri "ngau nhien 05:01–05:30" (bao tri sau moc tuan thu Hai 05:00) = 366 / 21;
     *  ve-lang-mien-phi: +3 khoa ve_lang.moi_ngay / thang_gym / toi_da (nhom tu_dong, chinh, thu_tu 369..371) = 369 / 21;
     *  do-ben-guc (31/10/2026, chu game): mac_dinh hoi_sinh.do_ben_mat_phan_tram 5 -> 2 (dang nhap lai khong con hoi day do ben) = 369 / 21;
     *  gioi-han-khac-he-pve (31/10/2026, chu game): +3 khoa chi_so.khac_he_pve_ra_toi_thieu / _ra_toi_da / _vao_toi_da (nhom
     *  chi_so, co_dinh, thu_tu 372..374) = 372 / 21;
     *  hlv-ai (31/10/2026, chu game): +nhom hlv_ai 16 khoa (huan luyen vien AI, chinh, thu_tu 375..390; tien to ten,
     *  doi bot, tran_xu 300000) = 388 / 22; sua mac dinh tien_to_ten = "" = 388 / 22;
     *  nang-luong-thanh-vien (31/10/2026, chu game): +3 khoa exp.nang_luong_{thanh_vien,the_thang,ve_mua}_phut (kinh_te,
     *  chinh, thu_tu 391..393) = 391 / 22.
     *  Lech = danh muc da doi (sua trong CSDL / phan B muc 2 cua sql/htbb.sql, xuat lai), cap nhat. */
    static final String VT_DANH_MUC = "c770f21c39bc0771f465217f213992a2168f929e401caa076339e231008c9558";
    static final String VT_HO_CAU = "2c36bb149458fd83c8e7d4c63b23ff55937be0d8863236c396102a2a088dc5c1";

    /** Bang P1 -> tep sinh goc. */
    static final String[][] BANG = {
        {DuLieuSql.CAP_DO, "pokemon_lau_dai.sql"}, {DuLieuSql.NANG_CAP, "pokemon_lau_dai.sql"},
        {DuLieuSql.KINH_TE, "pokemon_lau_dai.sql"}, {DuLieuSql.SU_KIEN_NAM, "pokemon_lau_dai.sql"},
        {DuLieuSql.HO_CAU, "pokemon_he_sinh_thai.sql"}, {DuLieuSql.TRANG_BI, "pokemon_noi_dung.sql"},
        {"cau_hinh_danh_muc", "pokemon_cau_hinh_trung_tam.sql"}, {"cau_hinh_nhom", "pokemon_cau_hinh_trung_tam.sql"},
    };

    static DuLieuSql.KetQua napKiem;

    public static void chay() {
        Kiem.nhom("Du lieu SQL (P1) - bang la nguon duy nhat, khop ban chep Java cu");
        try {
            vanTay();
            dongBo();
            hong();
            daXoa();
            boDoc();
        } catch (final Exception e) {
            e.printStackTrace(System.out);
            Kiem.dung("KiemDuLieuSql chay het: " + e, false);
        }
    }

    // ------------------------------------------------------------------ 1

    static void vanTay() throws Exception {
        Kiem.dung("Kiem.main nap du lieu SQL tu sql/htbb.sql (phan A) (khong loi, khong canh bao)",
                napKiem != null && napKiem.dat() && napKiem.canhBao.isEmpty());
        Kiem.bang("so bang nap duoc (danh muc + 6 bang so lieu)", Integer.valueOf(7),
                Integer.valueOf(napKiem == null ? 0 : napKiem.ok.size()));
        Kiem.bang("van tay data_cap_do_lau_dai == ThongSoLauDai cu (5 mang)", VT_CAP_DO, sha(chuoiCapDo()));
        Kiem.bang("van tay data_nang_cap_lau_dai == ThongSoLauDai cu (8 bang)", VT_NANG_CAP, sha(chuoiNangCap()));
        Kiem.bang("van tay data_kinh_te_lau_dai == ThongSoLauDai cu (12 so)", VT_KINH_TE, sha(chuoiKinhTe()));
        Kiem.bang("van tay data_su_kien_nam == SuKienLauDai.LICH cu", VT_LICH, sha(Arrays.deepToString(SuKienLauDai.LICH)));
        Kiem.bang("van tay data_noi_dung_trang_bi == GiftcodeAnToanDuLieu.TRANG_BI cu", VT_TRANG_BI,
                sha(Arrays.toString(GiftcodeAnToanDuLieu.TRANG_BI)));
        Kiem.bang("van tay cau_hinh_danh_muc + cau_hinh_nhom == DanhMucSinh cu", VT_DANH_MUC,
                sha(chuoiDanhMuc(CauHinhTrungTam.danhMuc(), CauHinhTrungTam.nhom())));
        Kiem.bang("van tay data_hst_ho_cau == BanDoSinhThai.diemCau cu (moi ban do cMap tru 40/42)", VT_HO_CAU, sha(chuoiHoCau()));
        // Mau tay: vai so de nguoi doc log thay ngay.
        Kiem.bang("EXP_LEN_CAP[1] / [99] / [100]", "215/421000000/0", ThongSoLauDai.EXP_LEN_CAP[1] + "/"
                + ThongSoLauDai.EXP_LEN_CAP[99] + "/" + ThongSoLauDai.EXP_LEN_CAP[100]);
        Kiem.bang("THUE_CHO / MAT_CAP_TU / BAC_MET", "6/7/[[120, 20], [120, 5]]", ThongSoLauDai.THUE_CHO_PHAN_TRAM + "/"
                + ThongSoLauDai.MAT_CAP_TU + "/" + Arrays.deepToString(ThongSoLauDai.BAC_MET));
        Kiem.dung("CHI_PHI_NC du 11 bac x 16", ThongSoLauDai.CHI_PHI_NC.length == 11 && ThongSoLauDai.CHI_PHI_NC[10].length == 16
                && ThongSoLauDai.CHI_PHI_NC[10][15] == 656682L);
        Kiem.bang("khoa dau tien cua danh muc (thu_tu)", "sv.lau_dai", CauHinhTrungTam.danhMuc()[0].khoa);
    }

    // ------------------------------------------------------------------ 2

    static void dongBo() throws Exception {
        final DocDumpSql dump = DayDu.dump();
        final DocDumpSql mig = DocDumpSql.tuChuoi(DayDu.nangCap());
        for (final String[] b : BANG) {
            final String[] cot = dump.cotCua(b[0]);
            Kiem.dung(b[0] + ": phan A (CSDL day du) co bang", cot != null);
            if (cot == null) {
                continue;
            }
            final List<String> dDump = dong(dump.doc(b[0], cot));
            Kiem.dung(b[0] + ": phan A co du lieu (" + dDump.size() + " dong, " + cot.length + " cot)", dDump.size() > 0);
            Kiem.bang(b[0] + ": phan B (nang cap) == phan A (moi cot, moi dong)", dDump, dong(mig.doc(b[0], cot)));
        }
        // MOT tep cho CSDL moi + CSDL dang chay: phan A khong pha gi khi chay tren CSDL co nguoi choi.
        final String a = DocDumpSql.phanDayDu(DayDu.toanVan());
        final String mg = DayDu.nangCap();
        Kiem.dung("sql/htbb.sql co phan A + phan B (dong " + DocDumpSql.MOC_NANG_CAP + ")", !a.isEmpty() && !mg.isEmpty()
                && a.length() + mg.length() == DayDu.toanVan().length());
        final java.util.regex.Matcher m = java.util.regex.Pattern.compile("(?m)^CREATE TABLE IF NOT EXISTS `(\\w+)`").matcher(
                a.substring(a.indexOf("USE `htbb_db`;")));
        int n = 0;
        while (m.find()) {
            n++;
        }
        Kiem.dung("phan A tao (IF NOT EXISTS) > 150 bang htbb_db (" + n + ")", n > 150);
        Kiem.sai("phan A: khong CREATE TABLE thieu IF NOT EXISTS (bang da co giu nguyen)",
                java.util.regex.Pattern.compile("(?m)^CREATE TABLE (?!IF NOT EXISTS )").matcher(a).find());
        Kiem.sai("phan A: khong DROP TABLE / DELETE / UPDATE / TRUNCATE / REPLACE / DROP DATABASE",
                java.util.regex.Pattern.compile("(?mi)^\\s*(DROP\\s+TABLE|DROP\\s+DATABASE|DELETE|UPDATE|TRUNCATE|REPLACE)\\b").matcher(a).find());
        // Du lieu day du: INSERT khong ten CSDL chi vao bang TAM (USE `account`), chep sang htbb_db chi khi CSDL moi.
        final java.util.regex.Matcher tam = java.util.regex.Pattern.compile("(?m)^CREATE TEMPORARY TABLE `(\\w+)`").matcher(a);
        final List<String> bangTam = new ArrayList<String>();
        while (tam.find()) {
            bangTam.add(tam.group(1));
        }
        final List<String> saiChep = new ArrayList<String>();
        for (final String t : bangTam) {
            if (!a.contains("SET @l := IF(@htbb_csdl_moi = 1 AND NOT EXISTS (SELECT 1 FROM `htbb_db`.`" + t + "`), 'INSERT INTO `htbb_db`.`"
                    + t + "` SELECT * FROM `account`.`" + t + "`', 'DO 0');")) {
                saiChep.add(t);
            }
        }
        Kiem.dung("phan A: > 100 bang du lieu thiet ke qua bang tam (" + bangTam.size() + ")", bangTam.size() > 100);
        Kiem.bang("phan A: moi bang tam chep co dieu kien (CSDL moi + bang dich rong)", "[]", saiChep.toString());
        final java.util.regex.Matcher ins = java.util.regex.Pattern.compile("(?m)^INSERT INTO `(\\w+)` VALUES").matcher(a);
        final java.util.Set<String> ngoaiTam = new java.util.TreeSet<String>();
        while (ins.find()) {
            if (!bangTam.contains(ins.group(1)) || ins.start() < a.indexOf("CREATE TEMPORARY TABLE `" + ins.group(1) + "`")) {
                ngoaiTam.add(ins.group(1));
            }
        }
        Kiem.bang("phan A: INSERT du lieu chi vao bang tam", "[]", ngoaiTam.toString());
        final int iMoi = a.indexOf("SET @htbb_csdl_moi := (SELECT COUNT(*) = 0 FROM `htbb_db`.`data_item`);");
        final int iUse = a.indexOf("USE `account`;", Math.max(iMoi, 0));
        Kiem.dung("phan A: co CSDL moi = data_item rong, tinh TRUOC khi nap bang tam (USE `account`)", iMoi > 0 && iUse > iMoi
                && iUse < a.indexOf("CREATE TEMPORARY TABLE"));
        Kiem.dung("phan A: data_item chep CUOI (nap do giua chung -> chay lai van la CSDL moi)",
                a.lastIndexOf("'INSERT INTO `htbb_db`.`") == a.indexOf("'INSERT INTO `htbb_db`.`data_item`"));
        Kiem.dung("phan A: tai khoan admin chi tao o CSDL moi, chua co admin", a.contains("WHERE @htbb_csdl_moi = 1 AND NOT EXISTS (SELECT 1 FROM `account`.`team_user` WHERE `username` = 'admin');"));
        Kiem.sai("phan B: khong con lenh CREATE DATABASE / USE CSDL khac", mg.contains("CREATE DATABASE")
                || mg.replace("USE `htbb_db`;", "").matches("(?s).*\\nUSE .*"));
        final String readme = doc("../sql/README.md");
        Kiem.dung("sql/README.md noi vps/nang_cap_csdl.sh + vps/xuat_csdl_day_du.sh", readme.contains("vps/nang_cap_csdl.sh")
                && readme.contains("vps/xuat_csdl_day_du.sh"));
        Kiem.dung("nang cap khong pha bang nguoi choi (tob_*: chi CREATE IF NOT EXISTS / ADD COLUMN IF NOT EXISTS / noi cot)",
                khongPhaNguoiChoi(mg));
        Kiem.dung("nang cap DROP + CREATE cau_hinh_danh_muc (cot thu_tu, lop)", mg.contains("DROP TABLE IF EXISTS `cau_hinh_danh_muc`")
                && mg.contains("`thu_tu` INT") && mg.contains("`lop` VARCHAR(10)"));
        final String nc = doc("../vps/nang_cap_csdl.sh");
        final String xu = doc("../vps/xuat_csdl_day_du.sh");
        Kiem.dung("vps/nang_cap_csdl.sh nap sql/htbb.sql, dem danh muc trong phan B", nc.contains("SQL=${HTBB_SQL:-$DIR/sql/htbb.sql}")
                && nc.contains("MOC='" + DocDumpSql.MOC_NANG_CAP + "'") && nc.contains("sed -n \"/^$MOC/,\\$p\" \"$SQL\""));
        Kiem.dung("vps/xuat_csdl_day_du.sh ghi sql/htbb.sql, giu phan B", xu.contains("RA=${1:-$DIR/sql/htbb.sql}")
                && xu.contains("MOC='" + DocDumpSql.MOC_NANG_CAP + "'") && xu.contains("CREATE TEMPORARY TABLE"));
        // sql/ = MOT tep CSDL (+ README); khong con tep le / bo sinh trong luong chinh
        final String[] tep = new java.io.File("../sql").list();
        final java.util.Set<String> co = new java.util.TreeSet<String>(tep == null ? java.util.Collections.<String>emptyList()
                : java.util.Arrays.asList(tep));
        Kiem.bang("sql/ chi co htbb.sql + README.md", "[README.md, htbb.sql]", co.toString());
        Kiem.sai("du_lieu/sql_nguon da luu tru (khong con o du_lieu/)", new java.io.File("../du_lieu/sql_nguon").exists());
        for (final String g : new String[]{"../tools/pokemon/xuat_sql_day_du.sh", "../tools/cauhinh/sinh_cau_hinh.py",
            "../tools/pokemon/sinh_nang_cap_csdl.py", "../tools/pokemon/sinh_noi_dung.py", "../tools/pokemon/sinh_sql_lau_dai.py"}) {
            Kiem.sai("bo sinh " + g + " khong con trong luong chinh (da luu tru)", new java.io.File(g).exists());
        }
    }

    /**
     * Moi lenh trong phan B cua sql/htbb.sql nhac toi bang nguoi choi (tob_*, giftcode_log, 5h_*, account) phai la loai KHONG PHA:
     * CREATE TABLE IF NOT EXISTS, ALTER TABLE ... ADD COLUMN IF NOT EXISTS / MODIFY (noi cot), CREATE INDEX IF NOT EXISTS,
     * INSERT IGNORE. Khong DROP / DELETE / UPDATE / TRUNCATE / REPLACE.
     */
    static boolean khongPhaNguoiChoi(final String sql) {
        boolean ok = true;
        for (final String l : sql.split(";\\s*\\n")) {
            final String t = l.replaceAll("(?m)^--.*$", "").trim();
            final int v = t.indexOf(" VALUES");
            final String dau = v < 0 ? t : t.substring(0, v); // chi xet ten bang, khong xet chuoi du lieu
            if (t.isEmpty() || !dau.matches("(?s).*(\\s|`)(tob_|giftcode_log|5h_|team_user|nap_|tv_|web_admin).*")) {
                continue;
            }
            final boolean hopLe = t.startsWith("CREATE TABLE IF NOT EXISTS") || t.startsWith("CREATE INDEX IF NOT EXISTS")
                    || t.startsWith("INSERT IGNORE INTO")
                    || t.startsWith("UPDATE `5h_systems` SET") // bang cau hinh he thong (he so nap), khong phai nguoi choi
                    // chu game 22/10/2026: chat chi trong phien - bo hop thu offline (bang tin cho, khong phai tai san nguoi choi)
                    || t.equals("DROP TABLE IF EXISTS `tob_chat_cho`")
                    // dong bo 0.9.8: doi ten mon cua hang cu CHI khi con ten mac dinh cu (giu ten admin da sua)
                    || t.startsWith("UPDATE `nap_cua_hang` SET `ten` = ") && t.matches("(?s).* WHERE `ma` = '[a-z_]+' AND `ten` = '[^']+'$")
                    // 31/10/2026: mo ta goi nap ghi them +phut nang luong CHI khi con mo ta mac dinh cu (giu ban admin sua)
                    || t.startsWith("UPDATE `nap_goi` SET `mo_ta` = ") && t.matches("(?s).* WHERE `ma` = '[A-Z_0-9]+' AND `mo_ta` = '[^']+'$")
                    // bang CONG TAC su kien (khong phai du lieu nguoi choi): lich su kien nam 0.9.5
                    || t.startsWith("UPDATE tob_su_kien SET mo_luc = NULL") || t.startsWith("DELETE FROM tob_su_kien WHERE ma IN")
                    || (t.startsWith("ALTER TABLE tob_su_kien") && t.contains("ADD COLUMN IF NOT EXISTS"))
                    || (t.startsWith("ALTER TABLE") && !t.matches("(?is).*\\b(DROP|RENAME|CHANGE)\\b.*")
                        && (t.contains("ADD COLUMN IF NOT EXISTS") || t.contains(" MODIFY ")));
            if (!hopLe) {
                System.out.println("   lenh pha / khong chay lai duoc: " + t.substring(0, Math.min(160, t.length())));
                ok = false;
            }
        }
        return ok;
    }

    /** Cot co o ca hai tep (thu tu cua tep goc). */
    static String[] cotChung(final DocDumpSql a, final DocDumpSql b, final String bang) {
        final List<String> ca = Arrays.asList(a.cotCua(bang));
        final List<String> ra = new ArrayList<String>();
        for (final String c : b.cotCua(bang)) {
            if (ca.contains(c)) {
                ra.add(c);
            }
        }
        return ra.toArray(new String[0]);
    }

    /** Moi dong thanh chuoi, sap xep (thu tu chen trong tep sinh != thu tu khoa cua dump). So thuc chuan hoa. */
    static List<String> dong(final BangSql b) {
        final List<String> ra = new ArrayList<String>();
        final String[] cot = b.cot();
        for (int r = 0; r < b.soDong(); r++) {
            final StringBuilder s = new StringBuilder();
            for (final String c : cot) {
                String v = b.chuoi(r, c);
                if (v != null && v.matches("-?\\d+\\.\\d+")) {
                    v = new java.math.BigDecimal(v).stripTrailingZeros().toPlainString();
                }
                s.append(v).append('|');
            }
            ra.add(s.toString());
        }
        Collections.sort(ra);
        return ra;
    }

    // ------------------------------------------------------------------ 3

    /** Nguon gia: lay that tu dump, rieng mot bang thi thieu / rong / thay noi dung. */
    static NguonDuLieu gia(final NguonDuLieu that, final String bang, final String sql) {
        return new NguonDuLieu() {
            @Override
            public BangSql doc(final String b, final String[] cot) throws Exception {
                if (!b.equals(bang)) {
                    return that.doc(b, cot);
                }
                if (sql == null) {
                    throw new java.sql.SQLException("Table 'htbb_db." + b + "' doesn't exist");
                }
                return DocDumpSql.tuChuoi(sql).doc(b, cot);
            }

            @Override
            public String moTa() {
                return "nguon gia (" + bang + ")";
            }
        };
    }

    static void hong() throws Exception {
        final DocDumpSql dump = DayDu.dump();
        final long[] expTruoc = ThongSoLauDai.EXP_LEN_CAP;
        final int thueTruoc = ThongSoLauDai.THUE_CHO_PHAN_TRAM;

        DuLieuSql.KetQua kq = DuLieuSql.napSoLieu(gia(dump, DuLieuSql.CAP_DO, null));
        Kiem.sai("thieu data_cap_do_lau_dai -> khong dat", kq.dat());
        Kiem.dung("loi ghi ro ten bang + ly do CSDL", kq.loi.size() == 1 && kq.loi.get(0).contains("data_cap_do_lau_dai")
                && kq.loi.get(0).contains("doesn't exist"));
        Kiem.dung("bang loi giu nguyen mang dang chay", ThongSoLauDai.EXP_LEN_CAP == expTruoc);

        final Object[][] lichTruoc = SuKienLauDai.LICH;
        kq = DuLieuSql.napSoLieu(gia(dump, DuLieuSql.SU_KIEN_NAM,
                "CREATE TABLE `data_su_kien_nam` (`tuan` int, `so_ngay` int, `ma_su_kien` varchar(9));"));
        Kiem.dung("data_su_kien_nam RONG -> loi 'RONG'", !kq.dat() && kq.loi.get(0).contains("RONG"));
        Kiem.dung("lich dang chay giu nguyen", SuKienLauDai.LICH == lichTruoc);

        kq = DuLieuSql.napSoLieu(gia(dump, DuLieuSql.CAP_DO,
                "CREATE TABLE `data_cap_do_lau_dai` (`cap` int, `exp_len_cap` bigint, `gio_tich_luy_phan_nghin` int, `mob_exp` int,"
                + " `quai_moi_gio` int, `xu_moi_quai` int, `exp_cu` bigint); INSERT INTO `data_cap_do_lau_dai` VALUES (1,215,0,20,359,10,200);"));
        Kiem.dung("data_cap_do_lau_dai thieu cap -> loi so dong", !kq.dat() && kq.loi.get(0).contains("can dung 100 dong"));

        kq = DuLieuSql.napSoLieu(gia(dump, DuLieuSql.KINH_TE, "CREATE TABLE `data_kinh_te_lau_dai` (`khoa` varchar(64), `gia_tri` varchar(64));"
                + " INSERT INTO `data_kinh_te_lau_dai` VALUES ('thue.cho','99');"));
        Kiem.dung("data_kinh_te_lau_dai thieu khoa -> loi, KHONG gan thue.cho=99 nua voi", !kq.dat()
                && kq.loi.get(0).contains("thieu khoa") && ThongSoLauDai.THUE_CHO_PHAN_TRAM == thueTruoc);

        kq = DuLieuSql.napSoLieu(gia(dump, DuLieuSql.KINH_TE, null));
        Kiem.dung("thieu cot (bang cu) -> loi", !kq.dat());

        final int[] tbTruoc = GiftcodeAnToanDuLieu.TRANG_BI;
        kq = DuLieuSql.napSoLieu(gia(dump, DuLieuSql.TRANG_BI, null));
        Kiem.dung("thieu data_noi_dung_trang_bi (bang phu) -> chi canh bao, van dat", kq.dat() && kq.canhBao.size() == 1
                && kq.canhBao.get(0).contains("data_noi_dung_trang_bi"));
        Kiem.dung("trang bi giftcode giu nguyen", GiftcodeAnToanDuLieu.TRANG_BI == tbTruoc);
        kq = DuLieuSql.napSoLieu(gia(dump, DuLieuSql.HO_CAU, null));
        Kiem.dung("thieu data_hst_ho_cau (bang phu) -> chi canh bao", kq.dat() && kq.canhBao.size() == 1);

        final DinhNghiaKhoa[] dmTruoc = CauHinhTrungTam.danhMuc();
        Kiem.sai("danh muc: thieu cau_hinh_nhom -> nap that bai", CauHinhTrungTam.napDanhMuc(gia(dump, "cau_hinh_nhom", null)));
        Kiem.bang("danh muc dang chay giu nguyen", Integer.valueOf(dmTruoc.length), Integer.valueOf(CauHinhTrungTam.danhMuc().length));
        Kiem.dung("danh muc da nap tu truoc -> khong bao loi khoi dong", CauHinhTrungTam.loiDanhMuc() == null);
        Kiem.sai("danh muc: gioi_han sai JSON -> that bai", CauHinhTrungTam.napDanhMuc(gia(dump, "cau_hinh_danh_muc",
                "CREATE TABLE `cau_hinh_danh_muc` (`khoa` varchar(64), `nhom` varchar(20), `ten` varchar(9), `mo_ta` text, `kieu` varchar(9),"
                + " `mac_dinh` varchar(9), `gioi_han` text, `ap_dung` varchar(16), `nhay` int, `khoa_prod` int, `chi_doc` int, `bang` varchar(9),"
                + " `thu_tu` int); INSERT INTO `cau_hinh_danh_muc` VALUES ('sv.x','che_do','X','','so','1','{min:1','ngay',0,0,0,'cau_hinh',1);")));
        Kiem.sai("danh muc: nhom khong ton tai -> that bai", CauHinhTrungTam.napDanhMuc(gia(dump, "cau_hinh_danh_muc",
                "CREATE TABLE `cau_hinh_danh_muc` (`khoa` varchar(64), `nhom` varchar(20), `ten` varchar(9), `mo_ta` text, `kieu` varchar(9),"
                + " `mac_dinh` varchar(9), `gioi_han` text, `ap_dung` varchar(16), `nhay` int, `khoa_prod` int, `chi_doc` int, `bang` varchar(9),"
                + " `thu_tu` int); INSERT INTO `cau_hinh_danh_muc` VALUES ('sv.x','khong_co','X','','so','1','{}','ngay',0,0,0,'cau_hinh',1);")));
        Kiem.sai("danh muc: CSDL cu chua co cot thu_tu -> that bai", CauHinhTrungTam.napDanhMuc(gia(dump, "cau_hinh_danh_muc",
                "CREATE TABLE `cau_hinh_danh_muc` (`khoa` varchar(64), `nhom` varchar(20));")));
        Kiem.bang("sau cac lan hong: danh muc van du", Integer.valueOf(dmTruoc.length), Integer.valueOf(CauHinhTrungTam.danhMuc().length));

        // Nap lai that (nap lai nong cung duong) -> trang thai nhu cu.
        final DuLieuSql.KetQua lai = DuLieuSql.napTatCa(dump);
        Kiem.dung("nap lai tu dump: dat", lai.dat() && lai.canhBao.isEmpty());
        Kiem.bang("nap lai: van tay cap do khong doi", VT_CAP_DO, sha(chuoiCapDo()));

        // Danh muc luc khoi dong: ket noi truc tiep hong vi "Too many connections" (pool vua mo het) -> thu pool;
        // khong toi duoc may CSDL (08S01) -> khong thu pool (cham), kiemTraCSDL dung server.
        Kiem.dung("matKetNoi: 08S01 (tu choi / het gio) = khong toi duoc",
                CauHinhTrungTam.matKetNoi(new java.sql.SQLException("Communications link failure", "08S01")));
        Kiem.dung("matKetNoi: ConnectException boc trong loi khac = khong toi duoc",
                CauHinhTrungTam.matKetNoi(new RuntimeException(new java.net.ConnectException("Connection refused"))));
        Kiem.sai("matKetNoi: 1040 Too many connections = van thu pool",
                CauHinhTrungTam.matKetNoi(new java.sql.SQLException("Too many connections", "08004", 1040)));
        final String cht = doc("src/com/htbb/cauhinh/CauHinhTrungTam.java");
        Kiem.dung("khoiTao: danh muc thu lai qua pool khi ket noi truc tiep hong", cht.contains("if (!khongToiDuoc && (db == null || DM.length == 0))")
                && cht.contains("\"CSDL \" + p.getProperty(\"db.name\") + \" (pool)\""));

        // May chu dung khi hong: DatabaseDuLieuSql in ro + System.exit(1).
        final String db = doc("src/data/DatabaseDuLieuSql.java");
        Kiem.dung("DatabaseDuLieuSql: in 'DUNG SERVER' + System.exit(1) khi loi", db.contains("DUNG SERVER") && db.contains("System.exit(1)"));
        Kiem.dung("DatabaseDuLieuSql: gom loi danh muc cau hinh", db.contains("CauHinhTrungTam.loiDanhMuc()"));
        Kiem.dung("DatabaseDuLieuSql: chi ra lenh nang cap", db.contains("vps/nang_cap_csdl.sh") && db.contains("sql/htbb.sql"));
    }

    // ------------------------------------------------------------------ 4

    static void daXoa() {
        final String ts = doc("src/server/TeamServer.java");
        final int iKiem = ts.indexOf("kiemTraCSDL(p);");
        final int iNap = ts.indexOf("DatabaseDuLieuSql.napLucKhoiDong()");
        Kiem.dung("TeamServer: nap du lieu SQL ngay sau kiemTraCSDL", iKiem > 0 && iNap > iKiem);
        Kiem.dung("... truoc SuKien.nap / ChuyenDoiLauDai / startSaveChar", iNap < ts.indexOf("real.SuKien.nap();")
                && iNap < ts.indexOf("ChuyenDoiLauDai.khiKhoiDong()") && iNap < ts.indexOf("Database.startSaveChar();"));
        Kiem.sai("DanhMucSinh.java da xoa", new File("src/com/htbb/cauhinh/DanhMucSinh.java").exists());
        final String tsl = doc("src/com/htbb/pokemon/ThongSoLauDai.java");
        boolean khongChep = true;
        for (final String t : new String[]{"EXP_LEN_CAP", "MOB_EXP", "QUAI_MOI_GIO", "XU_MOI_QUAI", "GIO_TICH_LUY_PHAN_NGHIN",
            "TI_LE_NC", "DA_NC", "BUA_BAO_HO_LUONG_KHOA", "CHI_SO_NC_PHAN_TRAM", "CHI_PHI_NC", "BAC_TB_TEN", "BAC_TB_CAP", "BAC_MET"}) {
            khongChep &= tsl.contains("public static volatile ") && !tsl.matches("(?s).*\\b" + t + " = \\{.*");
        }
        for (final String t : new String[]{"MAT_CAP_TU", "TI_LE_ROI_XU", "THUE_CHO_PHAN_TRAM", "NANG_LUONG_PHUT_NGAY", "VE_CHUONG_TOI_DA"}) {
            khongChep &= tsl.contains("public static volatile int " + t + ";");
        }
        Kiem.dung("ThongSoLauDai.java khong con chep bang SQL (chi khai bao truong nap)", khongChep);
        Kiem.sai("SuKienLauDai.java khong con chep lich", doc("src/com/htbb/pokemon/SuKienLauDai.java").contains("\"tetduonglich\""));
        Kiem.dung("GiftcodeAnToanDuLieu.java khong con chep trang bi",
                doc("src/com/htbb/pokemon/GiftcodeAnToanDuLieu.java").contains("TRANG_BI = new int[0];"));
        Kiem.sai("BanDoSinhThai khong con thuat toan diem cau chep tu bo sinh",
                doc("src/com/htbb/pokemon/BanDoSinhThai.java").contains("public static List<int[]> diemCau("));
        Kiem.bang("BanDoSinhThai: diem cau da nap", Boolean.TRUE, Boolean.valueOf(BanDoSinhThai.soBanDoCoDiemCau() >= 40));
    }

    // ------------------------------------------------------------------ 5

    static void boDoc() throws Exception {
        final DocDumpSql d = DocDumpSql.tuChuoi("-- chu thich; khong phai lenh\n"
                + "/*!40101 SET x = 1 */;\n"
                + "CREATE TABLE `t` (\n  `a` int(11) NOT NULL,\n  `b` varchar(9) DEFAULT 'x,y',\n  `c` text,\n  PRIMARY KEY (`a`)\n)"
                + " ENGINE=InnoDB COMMENT='(z)';\n"
                + "INSERT INTO `t` VALUES (1,'it\\'s; ok',NULL),(2,'a\\\\b\\nc','');\n"
                + "INSERT IGNORE INTO `t` (`c`, `a`) VALUES ('ch''o', 3);\n"
                + "CREATE TABLE IF NOT EXISTS `t` (`khac` int);\n"
                + "DELETE FROM `t` WHERE a = 1;\n"
                + "DROP TABLE IF EXISTS `u`; CREATE TABLE u (k int); INSERT INTO u VALUES (5); DELETE FROM u; INSERT INTO u VALUES (6);");
        final BangSql t = d.doc("t", new String[]{"a", "b", "c"});
        Kiem.bang("doc dump: 3 dong (DELETE co WHERE khong mo phong, IF NOT EXISTS khong xoa)", Integer.valueOf(3), Integer.valueOf(t.soDong()));
        Kiem.bang("doc dump: chuoi thoat \\' va ';' trong chuoi", "it's; ok", t.chuoi(0, "b"));
        Kiem.bang("doc dump: NULL", null, t.chuoi(0, "c"));
        Kiem.bang("doc dump: \\\\ va \\n", "a\\b\nc", t.chuoi(1, "b"));
        Kiem.bang("doc dump: danh sach cot + '' + cot thieu = NULL", "3|null|ch'o", t.chuoi(2, "a") + "|" + t.chuoi(2, "b") + "|" + t.chuoi(2, "c"));
        Kiem.bang("doc dump: DELETE FROM khong WHERE xoa dong cu", "[6]", Arrays.toString(new String[]{d.doc("u", new String[]{"k"}).chuoi(0, "k")}));
        boolean nem = false;
        try {
            d.doc("khong_co", new String[]{"a"});
        } catch (final IllegalStateException e) {
            nem = e.getMessage().contains("khong_co");
        }
        Kiem.dung("doc dump: bang khong co -> loi co ten bang", nem);
        nem = false;
        try {
            d.doc("t", new String[]{"zz"});
        } catch (final IllegalStateException e) {
            nem = e.getMessage().contains("zz");
        }
        Kiem.dung("doc dump: cot khong co -> loi co ten cot", nem);
        nem = false;
        try {
            t.so(1, "b");
        } catch (final IllegalArgumentException e) {
            nem = e.getMessage().contains("t.b dong 2");
        }
        Kiem.dung("BangSql.so: o khong phai so -> loi ghi bang.cot + dong", nem);
        final Map<String, Object> j = JsonNho.docObject("{\"min\": -5, \"max\": 2000000000000, \"mau\": \"^[a-z\\\\\\\"]+$\", \"luaChon\": [\"a\", \"b\"], \"hoa\": true, \"x\": null, \"f\": 1.5}");
        Kiem.bang("JsonNho: so / chuoi thoat / mang / bool / null / thuc", "-5|2000000000000|^[a-z\\\"]+$|[a, b]|true|null|1.5",
                j.get("min") + "|" + j.get("max") + "|" + j.get("mau") + "|" + j.get("luaChon") + "|" + j.get("hoa") + "|" + j.get("x") + "|" + j.get("f"));
        Kiem.bang("JsonNho: chuoi rong -> object rong", Integer.valueOf(0), Integer.valueOf(JsonNho.docObject("").size()));
        nem = false;
        try {
            JsonNho.docObject("{\"a\": 1,");
        } catch (final IllegalArgumentException e) {
            nem = true;
        }
        Kiem.dung("JsonNho: JSON hong -> loi", nem);
    }

    // ------------------------------------------------------------------ chuoi chuan (giong luc tinh van tay ban Java)

    static String chuoiCapDo() {
        return "EXP_LEN_CAP=" + Arrays.toString(ThongSoLauDai.EXP_LEN_CAP) + "\nGIO_TICH_LUY_PHAN_NGHIN="
                + Arrays.toString(ThongSoLauDai.GIO_TICH_LUY_PHAN_NGHIN) + "\nMOB_EXP=" + Arrays.toString(ThongSoLauDai.MOB_EXP)
                + "\nQUAI_MOI_GIO=" + Arrays.toString(ThongSoLauDai.QUAI_MOI_GIO) + "\nXU_MOI_QUAI=" + Arrays.toString(ThongSoLauDai.XU_MOI_QUAI);
    }

    static String chuoiNangCap() {
        return "TI_LE_NC=" + Arrays.toString(ThongSoLauDai.TI_LE_NC) + "\nDA_NC=" + Arrays.toString(ThongSoLauDai.DA_NC) + "\nBUA="
                + Arrays.toString(ThongSoLauDai.BUA_BAO_HO_LUONG_KHOA) + "\nCHI_SO=" + Arrays.toString(ThongSoLauDai.CHI_SO_NC_PHAN_TRAM)
                + "\nCHI_PHI=" + Arrays.deepToString(ThongSoLauDai.CHI_PHI_NC) + "\nTEN=" + Arrays.toString(ThongSoLauDai.BAC_TB_TEN)
                + "\nCAP=" + Arrays.toString(ThongSoLauDai.BAC_TB_CAP) + "\nMAT_CAP_TU=" + ThongSoLauDai.MAT_CAP_TU;
    }

    static String chuoiKinhTe() {
        return ThongSoLauDai.TI_LE_ROI_XU + "," + ThongSoLauDai.DA_ROI_PHAN_NGHIN + "," + ThongSoLauDai.NANG_LUONG_PHUT_NGAY + ","
                + ThongSoLauDai.NANG_LUONG_PHUT_TICH_TOI_DA + "," + ThongSoLauDai.THUE_CHO_PHAN_TRAM + ","
                + ThongSoLauDai.PHI_DANG_BAN_PHAN_TRAM + "," + ThongSoLauDai.TIEU_HAO_PHAN_TRAM + "," + ThongSoLauDai.SUA_DO_PHAN_TRAM + ","
                + ThongSoLauDai.GIFTCODE_TOI_DA_THANG + "," + ThongSoLauDai.VE_CHUONG_TUAN + "," + ThongSoLauDai.VE_CHUONG_TOI_DA
                + "|" + Arrays.deepToString(ThongSoLauDai.BAC_MET);
    }

    static String chuoiDanhMuc(final DinhNghiaKhoa[] ds, final String[][] nhom) {
        final StringBuilder b = new StringBuilder(Arrays.deepToString(nhom)).append('\n');
        for (final DinhNghiaKhoa d : ds) {
            b.append(d.khoa).append('|').append(d.nhom).append('|').append(d.kieu).append('|').append(d.macDinh).append('|')
                    .append(d.apDungNgay).append('|').append(d.bang).append('|').append(d.ten()).append('|').append(d.moTa())
                    .append('|').append(d.min()).append('|').append(d.max()).append('|').append(d.buoc()).append('|')
                    .append(Arrays.toString(d.luaChon())).append('|').append(d.soPhanTu()).append('|').append(d.mauChu())
                    .append('|').append(d.doDai()).append('|').append(d.chuHoa()).append('|').append(d.laNhay()).append('|')
                    .append(d.laKhoaProd()).append('|').append(d.laChiDoc()).append('|').append(d.lop()).append('\n');
        }
        return b.toString();
    }

    /** id ban do -> "ho:so_o:x:y ..." cho moi tep cMap/<so> co diem cau (giong ban Java luc tinh van tay). */
    static String chuoiHoCau() {
        final TreeMap<Integer, String> m = new TreeMap<Integer, String>();
        final File[] tep = new File("cMap").listFiles();
        if (tep != null) {
            for (final File f : tep) {
                if (!f.getName().matches("\\d+")) {
                    continue;
                }
                final int id = Integer.parseInt(f.getName());
                final StringBuilder s = new StringBuilder();
                for (final int[] p : BanDoSinhThai.diemCauCua(id)) {
                    s.append(p[0]).append(':').append(p[3]).append(':').append(p[1]).append(':').append(p[2]).append(' ');
                }
                if (s.length() > 0) {
                    m.put(id, s.toString());
                }
            }
        }
        final StringBuilder b = new StringBuilder();
        for (final Map.Entry<Integer, String> e : m.entrySet()) {
            b.append(e.getKey()).append('=').append(e.getValue()).append('\n');
        }
        return b.toString();
    }

    static String sha(final String s) throws Exception {
        final byte[] h = MessageDigest.getInstance("SHA-256").digest(s.getBytes(StandardCharsets.UTF_8));
        final StringBuilder b = new StringBuilder();
        for (final byte x : h) {
            b.append(String.format("%02x", x));
        }
        return b.toString();
    }

    static String doc(final String f) {
        try {
            return new String(Files.readAllBytes(Paths.get(f)), StandardCharsets.UTF_8);
        } catch (final Exception e) {
            return "";
        }
    }

    private KiemDuLieuSql() {
    }
}
