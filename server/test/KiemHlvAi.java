import com.htbb.cauhinh.CauHinhTrungTam;
import com.htbb.cauhinh.DinhNghiaKhoa;
import com.htbb.pokemon.HlvAi;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Huan luyen vien AI (bot lap may chu, chu game 31/10/2026): luat thuan com.htbb.pokemon.HlvAi (tien to ten cau hinh duoc, he so thu
 * nhap, tran xu / tui, gia san cho, han muc bot -> nguoi that), danh muc nhom hlv_ai (14 khoa chinh, mac dinh hop le,
 * chong lam phat: he so < 100), noi game (moi duong cong xu / chuyen gia tri / rao ban / tien that deu qua real.HlvAiGame),
 * khoi SQL them_hlv_ai.sql, dich vu bot + unit systemd (khong tu bat).
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
        HlvAi.datLaTenBot(t -> t.equalsIgnoreCase("minhanh"));
        HlvAi.datTienTo("ai ");
        Kiem.bang("tien to mac dinh 'ai '", "ai minhanh", HlvAi.hienThi("minhanh", true));
        Kiem.bang("ten nguoi giu nguyen", "minhanh", HlvAi.hienThi("minhanh", false));
        Kiem.bang("khong them tien to hai lan", "ai minhanh", HlvAi.hienThi("ai minhanh", true));
        Kiem.bang("bo tien to (tra loi tin rieng)", "minhanh", HlvAi.boTienTo("ai minhanh"));
        Kiem.bang("bo tien to khong phan biet hoa thuong", "minhanh", HlvAi.boTienTo("AI minhanh"));
        Kiem.bang("ten thuong khong doi khi bo tien to", "minhanh", HlvAi.boTienTo(" minhanh "));
        HlvAi.datTienTo("AI");
        Kiem.bang("doi tien to ngay, dinh lien ten", "AIminhanh", HlvAi.hienThi("minhanh", true));
        Kiem.bang("tien to moi bo duoc (phan con lai la bot)", "minhanh", HlvAi.boTienTo("AIminhanh"));
        Kiem.bang("tien to CU van bo duoc (ten client con giu)", "minhanh", HlvAi.boTienTo("ai minhanh"));
        // tien to dinh lien: nguoi choi that "aikhanh" (khong phai bot) KHONG bi cat thanh "khanh"
        Kiem.bang("ten nguoi bat dau bang 'ai' khong bi cat", "aikhanh", HlvAi.boTienTo("aikhanh"));
        for (final String dung : new String[]{"ai ", "AI", "[Bot] ", "BOT-", "bạn ai ", "robot_"}) {
            Kiem.dung("tien to hop le: '" + dung + "'", HlvAi.tienToHopLe(dung));
        }
        for (final String sai : new String[]{"", "   ", "abc ", "ai:", "qua dai hon 12 ai", "\t", "pk "}) {
            Kiem.sai("tien to bi tu choi (phai co AI / BOT, khong ':'): '" + sai + "'", HlvAi.tienToHopLe(sai));
        }
        HlvAi.datTienTo("xyz");
        Kiem.bang("gia tri sai luu trong CSDL -> mac dinh 'ai '", "ai minhanh", HlvAi.hienThi("minhanh", true));
        Kiem.dung("ten bot dai nhat (tien to 12 + ten 10) <= 22 ky tu cho J2ME", HlvAi.TIEN_TO_TOI_DA + 10 <= 22);
        HlvAi.datTienTo("ai ");
        HlvAi.datLaTenBot(null);
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
        capTheoNguoi();
    }

    /** hlv_ai.cap_theo_nguoi: khoang cap muc tieu bot theo nguoi choi that (HlvAi.khoangCap / mucTieuMoi / nangDan). */
    static void capTheoNguoi() {
        Kiem.dung("chua co nguoi that -> null (dung hlv_ai.cap_muc_tieu)", HlvAi.khoangCap(new int[0], 60) == null && HlvAi.khoangCap(null, 60) == null);
        // so lieu production 31/10/2026: 156 nguoi that 7 ngay, trung binh cap 10, cao nhat 54
        final int[] nguoi = new int[156];
        for (int i = 0; i < nguoi.length; i++) {
            nguoi[i] = i < 100 ? 2 + i % 9 : (i < 146 ? 8 + (i - 100) / 3 : 40 + (i - 146) * 14 / 9);
        }
        nguoi[155] = 54;
        final int[] sx = nguoi.clone();
        java.util.Arrays.sort(sx);
        int tTop = 0;
        for (int i = sx.length - 10; i < sx.length; i++) {
            tTop += sx[i];
        }
        final int[] k = HlvAi.khoangCap(nguoi, 60);
        Kiem.bang("thap = phan vi 25% (hang ceil(n/4))", sx[(int) Math.ceil(156 * 0.25) - 1], k[0]);
        Kiem.bang("cao = trung binh top 10 - 2", tTop / 10 - 2, k[1]);
        Kiem.dung("cao < cap nguoi that cao nhat (top nguoi that van dung dau): " + k[1], k[1] < 54);
        Kiem.dung("khoang hop le: thap + 5 <= cao", k[0] + HlvAi.KHOANG_TOI_THIEU <= k[1]);
        Kiem.bang("cao khong qua tran cap_muc_tieu", 30, HlvAi.khoangCap(nguoi, 30)[1]);
        final int[] it = HlvAi.khoangCap(new int[]{5, 5, 6}, 60);
        Kiem.bang("it nguoi, top thap: cao keo len thap + 5", it[0] + 5, it[1]);
        Kiem.bang("tran thap hon thap + 5: van giu thap + 5", 15, HlvAi.khoangCap(new int[]{10, 10, 10, 10}, 12)[1]);
        Kiem.bang("mot nguoi that cap 1: thap toi thieu 1", 1, HlvAi.khoangCap(new int[]{1}, 60)[0]);
        final java.util.Random rd = new java.util.Random(7);
        boolean trong = true;
        for (int i = 0; i < 2000; i++) {
            final int m = HlvAi.mucTieuMoi(k[0], k[1], rd);
            trong &= m >= k[0] && m <= k[1];
        }
        Kiem.dung("bot moi: muc tieu trong [thap, cao]", trong);
        Kiem.bang("bot cu duoi thap: +3 / ngay", 13, HlvAi.nangDan(10, 20, 40));
        Kiem.bang("bot cu sat thap: khong vuot thap", 20, HlvAi.nangDan(19, 20, 40));
        Kiem.bang("bot cu trong khoang: giu nguyen", 30, HlvAi.nangDan(30, 20, 40));
        Kiem.bang("bot cu tren cao: chan ve cao", 40, HlvAi.nangDan(55, 20, 40));
        int m = 1;
        int ngay = 0;
        while (m < 20 && ngay < 100) {
            m = HlvAi.nangDan(m, 20, 40);
            ngay++;
        }
        Kiem.bang("tu muc tieu 1 len 20: 7 ngay (+3 / ngay)", 7, ngay);
    }

    static void danhMuc() throws Exception {
        final String[] khoa = {HlvAi.K_HE_SO, HlvAi.K_TRAN_XU, HlvAi.K_TRAN_VAT_PHAM, HlvAi.K_GIA_BAN, HlvAi.K_CHUYEN_NGUOI,
            HlvAi.K_CHUYEN_TONG, "hlv_ai.bat", "hlv_ai.so_luong", "hlv_ai.lich_gio", "hlv_ai.ty_le_lop", "hlv_ai.ty_le_tinh_cach",
            "hlv_ai.cap_muc_tieu", "hlv_ai.ban_do", "hlv_ai.hoat_dong", HlvAi.K_DOI_BOT, HlvAi.K_TIEN_TO, HlvAi.K_CAP_THEO_NGUOI};
        int chinh = 0;
        for (final DinhNghiaKhoa d : CauHinhTrungTam.danhMuc()) {
            if ("hlv_ai".equals(d.nhom)) {
                chinh += "chinh".equals(d.lop()) ? 1 : 0;
            }
        }
        Kiem.bang("danh muc: nhom hlv_ai co 17 khoa chinh", 17, chinh);
        Kiem.bang("cap theo nguoi: mac dinh bat (chu game 31/10/2026)", "1", CauHinhTrungTam.dinhNghia(HlvAi.K_CAP_THEO_NGUOI).macDinh);
        Kiem.bang("cap theo nguoi: kieu co", "co", CauHinhTrungTam.dinhNghia(HlvAi.K_CAP_THEO_NGUOI).kieu);
        for (final String k : khoa) {
            Kiem.dung("danh muc co khoa " + k, CauHinhTrungTam.dinhNghia(k) != null);
        }
        Kiem.bang("mac dinh he so thu nhap (khop HlvAi.HE_SO)", String.valueOf(HlvAi.HE_SO), CauHinhTrungTam.dinhNghia(HlvAi.K_HE_SO).macDinh);
        Kiem.dung("chong lam phat: he so mac dinh < 100", HlvAi.HE_SO < 100);
        Kiem.bang("mac dinh tran xu", String.valueOf(HlvAi.TRAN_XU), CauHinhTrungTam.dinhNghia(HlvAi.K_TRAN_XU).macDinh);
        Kiem.bang("tran xu mac dinh 300.000 (chu game duyet)", 300000L, HlvAi.TRAN_XU);
        Kiem.bang("mac dinh so doi bot", String.valueOf(HlvAi.DOI_BOT), CauHinhTrungTam.dinhNghia(HlvAi.K_DOI_BOT).macDinh);
        Kiem.bang("mac dinh tien to", HlvAi.TIEN_TO_MAC_DINH, CauHinhTrungTam.dinhNghia(HlvAi.K_TIEN_TO).macDinh);
        Kiem.dung("mau tien to danh muc == luat may chu", CauHinhTrungTam.dinhNghia(HlvAi.K_TIEN_TO).mauChu().equals(HlvAi.MAU_TIEN_TO));
        Kiem.dung("mac dinh tien to hop le", HlvAi.tienToHopLe(CauHinhTrungTam.dinhNghia(HlvAi.K_TIEN_TO).macDinh));
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
        Kiem.dung("hoc chieu NPC: bot mien xu, nguoi choi tra du", doc("src/real/HocChieuGame.java").contains("final long gia = HlvAiGame.laBot(p) ? 0L : r.giaXu;")
                && doc("src/real/HocChieuGame.java").contains("p.subXu(gia, true, \"map 80\")"));
        Kiem.dung("co bot doc luc chon nhan vat", doc("src/real/cmd/SelectCharHandler.java").contains("real.HlvAiGame.vaoGame(p)"));
        Kiem.dung("ten goi 5 (vao tam nhin) co tien to", doc("src/real/MessageCreator.java").contains("HlvAi.hienThi(ch.getName(), ch.laHlvAi)"));
        Kiem.dung("tra cuu ten bo tien to (CharManager + kenh chat)", doc("src/real/CharManager.java").contains("HlvAi.boTienTo(name)")
                && doc("src/com/htbb/pokemon/KenhChatLogic.java").contains("HlvAi.boTienTo(ten)"));
        Kiem.dung("tien to doi tren web ap dung ngay (nhip 15 giay)", doc("src/real/HlvAiGame.java").contains("HlvAi.datTienTo(CauHinhTrungTam.chuoi(HlvAi.K_TIEN_TO))"));
        Kiem.dung("doi bot: mien 100 trieu + huy hieu chi qua HlvAiGame.mienPhiLapDoi", doc("src/real/MapDoi.java").split("HlvAiGame.mienPhiLapDoi\\(", -1).length - 1 == 2
                && doc("src/real/MapDoi.java").contains("if (!mienPhi && !player.truXuNeuDu((long) price, true, \"map 79\"))"));
        Kiem.dung("luong doi cua bot cho nguoi that qua han muc", doc("src/real/MapNhapText.java").split("HlvAiGame.chanChuyen\\(player, p, player, \"luong_doi\"", -1).length - 1 == 2);
        Kiem.dung("tien to khong ':' -> chat kieu cu tach ten o ': ' khong can xu ly rieng", !HlvAi.tienToHopLe("ai:"));
        Kiem.dung("web cung mau + cau bao loi tien to", doc("../web/shared/utils/hlvAi.ts").contains(HlvAi.LOI_TIEN_TO));
        final String sql = doc("../sql/htbb.sql");
        final String khoi = sql.substring(sql.indexOf("-- >>> them_hlv_ai.sql"));
        Kiem.dung("sql: cot la_bot (them neu chua co)", khoi.contains("ALTER TABLE `account`.`team_user` ADD COLUMN IF NOT EXISTS `la_bot`"));
        for (final String b : new String[]{"tob_hlv_ai_kinh_te_ngay", "tob_hlv_ai_moc", "tob_hlv_ai_chuyen", "tob_hlv_ai_gia", "tob_hlv_ai_trang_thai", "tob_hlv_ai_cap_nguoi"}) {
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
        // bao loi production 31/10/2026: bot dung o 53,19 trong LANG cho buoc dong co Pallet (#14 / #19) vi dich vu khong
        // nap bang ten ban do -> id (chi kiem soak nap) -> moi buoc "dung o (x,y) tren <ban do>" lam o ban do dang dung
        final String nc = doc("../tools/pokemon/hlv_ai/HlvAiNguoiChoi.java");
        final int vao = nc.indexOf("void vao() throws Exception");
        final int napTg = nc.indexOf("HoiQuy.napTheGioi(b);", vao);
        Kiem.dung("bot nap ten ban do (LOAD_THEO_TEN) moi lan vao game, khong chi trong kiem soak",
                vao > 0 && napTg > vao && nc.indexOf("KichBanCap140.napTen();", napTg) > napTg
                        && nc.indexOf("KichBanCap140.napTen();", napTg) < nc.indexOf("ngheXaHoi();", napTg));
        final String dv = doc("../tools/pokemon/hlv_ai/DichVuHlvAi.java");
        Kiem.dung("cap theo nguoi: dich vu tinh luc khoi dong + moi nhip (mot lan / ngay), nguoi that = la_bot 0 hoat dong 7 ngay",
                dv.split("capTheoNguoi\\(\\);", -1).length - 1 == 2 && dv.contains("u.la_bot = 0") && dv.contains("INTERVAL 7 DAY")
                        && dv.contains("HlvAi.khoangCap(") && dv.contains("HlvAi.nangDan(") && dv.contains("HlvAi.mucTieuMoi("));
        final String nc2 = doc("../tools/pokemon/hlv_ai/HlvAiNguoiChoi.java");
        Kiem.dung("cap theo nguoi: bot thoi viec ra EXP khi toi muc cao", nc2.contains("DichVuHlvAi.CAP_CAO > 0 && b.lv >= DichVuHlvAi.CAP_CAO")
                && nc2.contains("else if (tran && VIEC_EXP.contains(VIEC[i])) w[i] = 0;"));
        Kiem.dung("cap theo nguoi: trang /admin/hlv-ai hien khoang + 'theo người chơi thật'",
                doc("../web/app/pages/admin/hlv-ai.vue").contains("theo người chơi thật") && doc("../web/server/api/admin/hlv-ai/index.get.ts").contains("tob_hlv_ai_cap_nguoi"));
    }

    public static void main(final String[] args) {
        try {
            com.htbb.dulieu.DuLieuSql.napTuDumpDayDu();
        } catch (final Throwable ignored) {
        }
        chay();
        final int code = Kiem.tongKet();
        System.exit(code);
    }
}
