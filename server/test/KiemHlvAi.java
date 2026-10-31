import com.htbb.cauhinh.CauHinhTrungTam;
import com.htbb.cauhinh.DinhNghiaKhoa;
import com.htbb.pokemon.HlvAi;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Huan luyen vien AI (bot lap may chu, chu game 31/10/2026): luat thuan com.htbb.pokemon.HlvAi (ten "bot_ai: ", he so thu
 * nhap, tran xu / tui, gia san cho, han muc bot -> nguoi that), danh muc nhom hlv_ai (14 khoa chinh, mac dinh hop le,
 * chong lam phat: he so < 100), noi game (moi duong cong xu / chuyen gia tri / rao ban / tien that deu qua real.HlvAiGame),
 * khoi SQL them_hlv_ai.sql, dich vu bot + unit systemd (khong tu bat), client cu tach ten "bot_ai: x: noi dung".
 */
public final class KiemHlvAi {

    private KiemHlvAi() {
    }

    static String doc(final String p) throws Exception {
        return new String(Files.readAllBytes(Paths.get(p)), StandardCharsets.UTF_8);
    }

    public static void chay() {
        Kiem.nhom("Huan luyen vien AI (bot lap may chu)");
        tenHienThi();
        kinhTe();
        try {
            danhMuc();
            noiGame();
        } catch (final Exception e) {
            Kiem.dung("hlv ai: doc tep: " + e, false);
        }
    }

    static void tenHienThi() {
        Kiem.bang("ten bot co tien to", "bot_ai: minhanh", HlvAi.hienThi("minhanh", true));
        Kiem.bang("ten nguoi giu nguyen", "minhanh", HlvAi.hienThi("minhanh", false));
        Kiem.bang("khong them tien to hai lan", "bot_ai: minhanh", HlvAi.hienThi("bot_ai: minhanh", true));
        Kiem.bang("bo tien to (tra loi tin rieng)", "minhanh", HlvAi.boTienTo("bot_ai: minhanh"));
        Kiem.bang("bo tien to khong phan biet hoa thuong / mat dau cach", "minhanh", HlvAi.boTienTo("BOT_AI:minhanh"));
        Kiem.bang("ten thuong khong doi khi bo tien to", "minhanh", HlvAi.boTienTo(" minhanh "));
        // ten that chi [0-9a-zA-Z] (CreateCharHandler.checkInfo) -> tien to khong bao gio trung ten nguoi choi
        Kiem.sai("tien to khong phai ten hop le", HlvAi.TIEN_TO.matches("[0-9a-zA-Z]+"));
        Kiem.dung("ten bot dai nhat (10 ky tu) van gon cho J2ME (<= 18 ky tu)", HlvAi.hienThi("abcdefghij", true).length() <= 18);
    }

    static void kinhTe() {
        Kiem.bang("he so 60%: 1000 -> 600", 600L, HlvAi.thuNhap(1000L, 60L));
        Kiem.bang("he so 60%: so le lam tron xuong", 59L, HlvAi.thuNhap(99L, 60L));
        Kiem.bang("he so kep <= 100 (bot khong hon nguoi)", 1000L, HlvAi.thuNhap(1000L, 250L));
        Kiem.bang("he so 0 = khong thu nhap", 0L, HlvAi.thuNhap(1000L, 0L));
        Kiem.bang("so lon khong tran", 3000000000L / 100L * 60L, HlvAi.thuNhap(3000000000L, 60L));
        Kiem.bang("tran xu: vuot thi dot phan vuot", 500L, HlvAi.phanDot(2000500L, 2000000L));
        Kiem.bang("tran xu: chua vuot khong dot", 0L, HlvAi.phanDot(1999999L, 2000000L));
        Kiem.bang("tran xu am = khong tran", 0L, HlvAi.phanDot(Long.MAX_VALUE, -1L));
        Kiem.bang("tran tui: 65 o / tran 60 -> huy 5", 5, HlvAi.soOHuy(65, 60));
        Kiem.bang("tran tui: chua vuot", 0, HlvAi.soOHuy(60, 60));
        Kiem.bang("trung vi le", 30L, HlvAi.trungVi(new long[]{50, 10, 30}, 3));
        Kiem.bang("trung vi chan", 25L, HlvAi.trungVi(new long[]{10, 20, 30, 40}, 4));
        Kiem.bang("trung vi rong", -1L, HlvAi.trungVi(new long[0], 0));
        Kiem.bang("gia san: du mau = 100% trung vi", 900L, HlvAi.giaSan(900L, 5, 500L, 100L));
        Kiem.bang("gia san: it mau = gia tham chieu", 500L, HlvAi.giaSan(900L, 2, 500L, 100L));
        Kiem.bang("gia san: khong duoi tham chieu (chong pha gia)", 500L, HlvAi.giaSan(300L, 9, 500L, 100L));
        Kiem.bang("gia san: 120% trung vi", 1080L, HlvAi.giaSan(900L, 5, 500L, 120L));
        Kiem.bang("gia tri: lan co xu = so xu", 7000L, HlvAi.giaTri("ky_gui", 7000L));
        Kiem.bang("gia tri: doi thuoc (khong xu) van tinh", HlvAi.GIA_TRI_KHONG_XU, HlvAi.giaTri("giao_dich", 0L));
        Kiem.bang("gia tri: doi Pokemon - tham so la PHI, van tinh gia uoc", HlvAi.GIA_TRI_KHONG_XU, HlvAi.giaTri("trao_doi_pokemon", 300L));
        Kiem.dung("han muc: con duoc", HlvAi.conDuocChuyen(40000L, 100000L, 10000L, 50000L, 500000L));
        Kiem.sai("han muc: vuot moi nguoi", HlvAi.conDuocChuyen(45000L, 100000L, 10000L, 50000L, 500000L));
        Kiem.sai("han muc: vuot ca may chu", HlvAi.conDuocChuyen(0L, 495000L, 10000L, 50000L, 500000L));
        Kiem.sai("han muc 0 = cam", HlvAi.conDuocChuyen(0L, 0L, 1L, 0L, 500000L));
        Kiem.dung("han muc am = khong gioi han", HlvAi.conDuocChuyen(1L << 40, 1L << 40, 1L << 40, -1L, -1L));
        Kiem.dung("han muc: them 0 luon duoc", HlvAi.conDuocChuyen(99999L, 99999L, 0L, 0L, 0L));
        // lap mua-thap-ban-cao / thua cuoc lien tuc: tong gia tri bot -> mot nguoi khong bao gio vuot han muc ngay
        long da = 0L;
        int lan = 0;
        while (lan < 1000 && HlvAi.conDuocChuyen(da, da, 7000L, HlvAi.CHUYEN_NGUOI, HlvAi.CHUYEN_TONG)) {
            da += 7000L;
            lan++;
        }
        Kiem.dung("lap 1000 lan chuyen 7000: dung truoc han muc nguoi (" + da + ")", da <= HlvAi.CHUYEN_NGUOI && lan < 1000);
        Kiem.bang("ngay khoa han muc", 20261031, HlvAi.ngay(new java.util.GregorianCalendar(2026, 9, 31)));
    }

    static void danhMuc() throws Exception {
        final String[] khoa = {HlvAi.K_HE_SO, HlvAi.K_TRAN_XU, HlvAi.K_TRAN_VAT_PHAM, HlvAi.K_GIA_BAN, HlvAi.K_CHUYEN_NGUOI,
            HlvAi.K_CHUYEN_TONG, "hlv_ai.bat", "hlv_ai.so_luong", "hlv_ai.lich_gio", "hlv_ai.ty_le_lop", "hlv_ai.ty_le_tinh_cach",
            "hlv_ai.cap_muc_tieu", "hlv_ai.ban_do", "hlv_ai.hoat_dong"};
        int chinh = 0;
        for (final DinhNghiaKhoa d : CauHinhTrungTam.danhMuc()) {
            if ("hlv_ai".equals(d.nhom)) {
                chinh += "chinh".equals(d.lop()) ? 1 : 0;
            }
        }
        Kiem.bang("danh muc: nhom hlv_ai co 14 khoa chinh", 14, chinh);
        for (final String k : khoa) {
            Kiem.dung("danh muc co khoa " + k, CauHinhTrungTam.dinhNghia(k) != null);
        }
        Kiem.bang("mac dinh he so thu nhap (khop HlvAi.HE_SO)", String.valueOf(HlvAi.HE_SO), CauHinhTrungTam.dinhNghia(HlvAi.K_HE_SO).macDinh);
        Kiem.dung("chong lam phat: he so mac dinh < 100", HlvAi.HE_SO < 100);
        Kiem.bang("mac dinh tran xu", String.valueOf(HlvAi.TRAN_XU), CauHinhTrungTam.dinhNghia(HlvAi.K_TRAN_XU).macDinh);
        Kiem.bang("mac dinh tran tui", String.valueOf(HlvAi.TRAN_VAT_PHAM), CauHinhTrungTam.dinhNghia(HlvAi.K_TRAN_VAT_PHAM).macDinh);
        Kiem.bang("mac dinh gia ban toi thieu", String.valueOf(HlvAi.GIA_BAN), CauHinhTrungTam.dinhNghia(HlvAi.K_GIA_BAN).macDinh);
        Kiem.bang("mac dinh han muc moi nguoi", String.valueOf(HlvAi.CHUYEN_NGUOI), CauHinhTrungTam.dinhNghia(HlvAi.K_CHUYEN_NGUOI).macDinh);
        Kiem.bang("mac dinh han muc tong", String.valueOf(HlvAi.CHUYEN_TONG), CauHinhTrungTam.dinhNghia(HlvAi.K_CHUYEN_TONG).macDinh);
        Kiem.bang("dich vu tat mac dinh (khong tu chay)", "0", CauHinhTrungTam.dinhNghia("hlv_ai.bat").macDinh);
        Kiem.bang("lich gio co 24 so", 24, CauHinhTrungTam.dinhNghia("hlv_ai.lich_gio").macDinh.split(",").length);
    }

    static void noiGame() throws Exception {
        final String ch = doc("src/real/Char.java");
        Kiem.dung("Char.addXu: xu SINH RA cua bot qua he so", ch.contains("kieu == XU_DEM && xu > 0L ? HlvAiGame.thuNhap(this, xu)"));
        Kiem.dung("Char.addXu: tran xu bot (dot phan vuot)", ch.contains("HlvAi.phanDot(this.xu, Math.max(truoc, HlvAiGame.tranXu()))"));
        Kiem.dung("Char.tenHienThi = ten client thay", ch.contains("public String tenHienThi()"));
        final String cc = doc("src/com/htbb/pokemon/ChongClone.java");
        Kiem.dung("ChongClone: moi cong chuyen gia tri qua han muc bot", cc.split("real.HlvAiGame.chanChuyen\\(", -1).length - 1 == 2
                && cc.contains("real.HlvAiGame.chanCaHai("));
        Kiem.dung("ChongClone: moi lan ghi chuyen ghi ca bot <-> nguoi", cc.split("real.HlvAiGame.ghiChuyen\\(", -1).length - 1 == 3);
        final String om = doc("src/real/OfflineMap.java");
        Kiem.bang("ky gui: 3 duong rao ban qua gia san bot", 3, om.split("real.HlvAiGame.giaBanThap\\(", -1).length - 1);
        Kiem.dung("ky gui: ghi gia ban xong (trung vi)", om.contains("real.HlvAiGame.ghiGiaBan(\"ky_gui\""));
        Kiem.dung("cho Pokemon: gia san + ghi gia", doc("src/real/ChoPokemonGame.java").contains("HlvAiGame.giaBanThap(p, \"cho_pokemon\"")
                && doc("src/real/ChoPokemonGame.java").contains("HlvAiGame.ghiGiaBan(\"cho_pokemon\""));
        final String hst = doc("src/com/htbb/pokemon/HeSinhThaiMenu.java");
        Kiem.dung("cho sinh thai: gia san + ghi gia", hst.contains("real.HlvAiGame.giaBanThap(p, \"sinh_thai\"") && hst.contains("real.HlvAiGame.ghiGiaBan(\"sinh_thai\""));
        Kiem.dung("tien that: nap / mua goi chan bot", doc("src/com/htbb/pokemon/NapTienGame.java").split("HlvAiGame.chanTienThat\\(", -1).length - 1 == 2);
        Kiem.dung("tien that: chuyen luong chan bot gui + nhan", doc("src/real/CharChuyenLuong.java").contains("real.HlvAiGame.chanTienThat(player)")
                && doc("src/real/CharChuyenLuong.java").split("Huấn luyện viên AI không nhận lượng", -1).length - 1 == 2);
        Kiem.dung("tien that: giftcode chan bot", doc("src/real/GiftcodeAnToan.java").contains("HlvAiGame.chanTienThat(p)"));
        Kiem.dung("gop quy doi: tinh han muc bot -> nguoi", doc("src/real/Map.java").contains("HlvAiGame.MA_BANG + clandata.id"));
        Kiem.dung("co bot doc luc chon nhan vat", doc("src/real/cmd/SelectCharHandler.java").contains("real.HlvAiGame.vaoGame(p)"));
        Kiem.dung("ten goi 5 (vao tam nhin) co tien to", doc("src/real/MessageCreator.java").contains("HlvAi.hienThi(ch.getName(), ch.laHlvAi)"));
        Kiem.dung("tra cuu ten bo tien to (CharManager + kenh chat)", doc("src/real/CharManager.java").contains("HlvAi.boTienTo(name)")
                && doc("src/com/htbb/pokemon/KenhChatLogic.java").contains("HlvAi.boTienTo(ten)"));
        final String kc = doc("../client_jar/src/game/pokemon/KenhChat.java");
        Kiem.dung("client cu: tach ten 'bot_ai: x: noi dung' dung cho", kc.contains("static int nganTen(String s)") && kc.split("nganTen\\(", -1).length - 1 == 4);
        final String sql = doc("../sql/htbb.sql");
        final String khoi = sql.substring(sql.indexOf("-- >>> them_hlv_ai.sql"));
        Kiem.dung("sql: cot la_bot (them neu chua co)", khoi.contains("ALTER TABLE `account`.`team_user` ADD COLUMN IF NOT EXISTS `la_bot`"));
        for (final String b : new String[]{"tob_hlv_ai_kinh_te_ngay", "tob_hlv_ai_moc", "tob_hlv_ai_chuyen", "tob_hlv_ai_gia", "tob_hlv_ai_trang_thai"}) {
            Kiem.dung("sql: bang " + b, khoi.contains("CREATE TABLE IF NOT EXISTS `" + b + "`"));
        }
        final String unit = doc("../vps/htbb-hlv-ai.service");
        boolean deployCham = false;
        for (final String t : new String[]{"../vps/deploy-auto.sh", "../vps/update.sh", "../vps/len-production.sh", "../vps/capnhat.sh",
            "../vps/staging/cai-dat.sh"}) {
            if (Files.isRegularFile(Paths.get(t)) && doc(t).contains("htbb-hlv-ai")) {
                deployCham = true;
            }
        }
        Kiem.dung("unit systemd dich vu bot: deploy / staging khong tu cai, khong tu bat", !deployCham && unit.contains("ExecStartPre=/bin/sh /opt/htbb/tools/pokemon/hlv_ai/xay.sh"));
        Kiem.dung("dich vu bot khong chua dia chi WAF", !unit.contains("51.79."));
        Kiem.dung("dich vu bot: ma nguon co", Files.isRegularFile(Paths.get("../tools/pokemon/hlv_ai/DichVuHlvAi.java")));
    }
}
