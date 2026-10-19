import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Su kien World Cup (real/SuKienWorldCup) - cac rang buoc de vo im lang.
 *
 * BON CAI BAY, deu la ly do phai co test nay:
 *
 * 1. THUOC 108. Su kien them mot vat pham moi vao hanh trang. Ban goc dung id 162,
 *    ban nay KHONG duoc: data_potion co dung 162 dong (0..161) va client cap phat
 *    mang listPotion theo con so server gui luc dang nhap. Gui id vuot tran thi
 *    setInventory() nem IndexOutOfRange va NGUOI CHOI THAY CA HANH TRANG RONG
 *    (chu thich day du o Char.getAllPotion). Nen phai dung lai mot dong TRONG
 *    san co - 108 - va id do bat buoc < Char.SO_O_THUOC.
 *
 * 2. POTION_SHOP. Id 108 phai co trong LoginHandler.POTION_SHOP, khong thi nguoi
 *    choi khong mo duoc chi tiet hop qua (client doc Res.potionTemplates[108-19],
 *    mang do chi co 25 o). Day dung lai co che da lam hong sach ky nang pet
 *    ngay 30/08/2026 - xem KiemChiTietThuoc.
 *
 * 3. VI TRI TRONG MENU NPC. Muc "Su kien World Cup" duoc NOI VAO CUOI menu NPC
 *    Tong Quan chu khong chen vao giua: chen giua lam moi muc phia sau tut mot bac
 *    va client gui ve vi tri NGUOI CHOI NHIN THAY, nen bam "Nhan qua top" se chay
 *    nham chuc nang khac. Cung ly do voi VI_TRI_MENU_DAU_TRUONG trong MapDoMenu.
 *
 * 4. idMenu KHONG PHAI idOptionMenu. Client tra lai DUNG idMenu no nhan duoc
 *    (menu goc luon la 0) va gui dong duoc chon trong idOptionMenu - xem
 *    GameService.doMenuOption ben client_pc_ios. Bat nham truong la muc menu chet im.
 */
public final class KiemSuKienWorldCup {

    /** Cong thuc client dung de tra cuu mo ta thuoc. */
    private static final int LECH_SHOP = 19;

    public static void chay() {
        Kiem.nhom("Su kien World Cup");

        // ================= 1. VAT PHAM =================
        final int hopMayMan = real.SuKienWorldCup.ITEM_HOP_MAY_MAN;
        final int bongVang = real.SuKienWorldCup.ITEM_BONG_VANG;
        final int hopBiMat = real.SuKienWorldCup.ITEM_HOP_BI_MAT;

        Kiem.bang("Hop qua may man dung dong thuoc trong 108",
                Integer.valueOf(108), Integer.valueOf(hopMayMan));
        Kiem.bang("Qua bong vang la Potion.TYPE_QUA_BONG_VANG",
                Integer.valueOf(real.Potion.TYPE_QUA_BONG_VANG), Integer.valueOf(bongVang));
        Kiem.bang("Hop qua bi mat la Potion.TYPE_HOP_QUA_BI_MAT",
                Integer.valueOf(real.Potion.TYPE_HOP_QUA_BI_MAT), Integer.valueOf(hopBiMat));

        // Vuot tran nay la ca hanh trang cua nguoi choi bien mat, khong chi rieng hop qua.
        Kiem.dung("moi id thuoc cua su kien < Char.SO_O_THUOC (" + real.Char.SO_O_THUOC + ")",
                hopMayMan < real.Char.SO_O_THUOC
                        && bongVang < real.Char.SO_O_THUOC
                        && hopBiMat < real.Char.SO_O_THUOC);

        // ================= 2. POTION_SHOP =================
        final short[] ds = real.cmd.LoginHandler.POTION_SHOP;
        Kiem.dung("hop qua may man (" + hopMayMan + ") co trong POTION_SHOP", co(ds, hopMayMan));
        Kiem.dung("qua bong vang (" + bongVang + ") co trong POTION_SHOP", co(ds, bongVang));
        Kiem.dung("hop qua bi mat (" + hopBiMat + ") co trong POTION_SHOP", co(ds, hopBiMat));

        // Client noi them mo ta cua data_shop[id-19] vao chi tiet. Dong 89 hien co cot
        // decript rong - neu sau nay ai dien vao do thi hop qua se hien nham mo ta.
        Kiem.bang("hop qua may man anh xa sang data_shop 89",
                Integer.valueOf(89), Integer.valueOf(hopMayMan - LECH_SHOP));

        // ================= 3. THOI GIAN =================
        final String bd = real.SuKienWorldCup.batDau;
        final String kt = real.SuKienWorldCup.ketThuc;
        final String han = real.SuKienWorldCup.hanNhanQua;
        try {
            // De trong = tat han.
            real.SuKienWorldCup.datThoiGian("", "", "");
            Kiem.sai("de trong moc thoi gian -> su kien tat", real.SuKienWorldCup.hienTrongMenu());

            // Thieu mot khoa cung phai tat, khong duoc chay nua voi.
            real.SuKienWorldCup.datThoiGian("2026-01-01 00:00:00", "2026-12-31 23:59:59", "");
            Kiem.sai("thieu han nhan qua -> su kien tat", real.SuKienWorldCup.hienTrongMenu());

            // Moc lon xon (bat dau sau ket thuc) cung phai tat.
            real.SuKienWorldCup.datThoiGian("2026-12-31 00:00:00", "2026-01-01 00:00:00",
                    "2027-01-01 00:00:00");
            Kiem.sai("bat dau sau ket thuc -> su kien tat", real.SuKienWorldCup.hienTrongMenu());

            // Cua so hop le bao trum hien tai -> dang binh chon.
            real.SuKienWorldCup.datThoiGian("2000-01-01 00:00:00", "2099-12-31 23:59:59",
                    "2099-12-31 23:59:59");
            // 19/10/2026: World Cup (binh chon Messi/Ronaldo) da bo khoi danh muc su kien - cua so sv.worldCup*
            // khong con bat su kien nua (SuKien.macDinh luon tat).
            Kiem.sai("da bo: cua so bao trum hien tai cung KHONG binh chon",
                    real.SuKienWorldCup.dangDienRa());
            Kiem.sai("dang binh chon thi chua toi han nhan qua",
                    real.SuKienWorldCup.dangHanNhanQua());

            // Da het binh chon nhung con han nhan qua.
            real.SuKienWorldCup.datThoiGian("2000-01-01 00:00:00", "2000-01-02 00:00:00",
                    "2099-12-31 23:59:59");
            Kiem.sai("het binh chon thi quai khong roi bong vang nua",
                    real.SuKienWorldCup.dangDienRa());
            Kiem.sai("da bo: con han nhan qua cung KHONG phat qua",
                    real.SuKienWorldCup.dangHanNhanQua());
            Kiem.sai("da bo: menu khong hien",
                    real.SuKienWorldCup.hienTrongMenu());

            // Qua han het.
            real.SuKienWorldCup.datThoiGian("2000-01-01 00:00:00", "2000-01-02 00:00:00",
                    "2000-01-03 00:00:00");
            Kiem.sai("qua han nhan qua -> menu khong hien",
                    real.SuKienWorldCup.hienTrongMenu());
        } finally {
            real.SuKienWorldCup.datThoiGian(bd, kt, han);
        }

        // ================= 4. MA NGUON: MENU =================
        final String npc = doc("src/real/cmd/RequestNPCInfoHandler.java");
        final int viTriMocNap = npc.indexOf("p.idxMocNapMenu = menu.length;");
        final int viTriSuKien = npc.indexOf("p.idxSuKienWorldCup = menu.length;");
        Kiem.dung("RequestNPCInfoHandler ghi lai vi tri muc su kien", viTriSuKien > 0);
        Kiem.dung("muc su kien NOI VAO CUOI, sau ca muc Nhan moc nap",
                viTriMocNap > 0 && viTriSuKien > viTriMocNap);
        Kiem.dung("khong co su kien thi dat lai vi tri ve -1",
                npc.contains("p.idxSuKienWorldCup = -1;"));

        final String doMenu = doc("src/real/MapDoMenu.java");
        // Bat idOptionMenu chu khong phai idMenu - xem cai bay 4 o dau tep.
        Kiem.dung("MapDoMenu so vi tri muc su kien voi idOptionMenu",
                doMenu.contains("idOptionMenu == player.idxSuKienWorldCup"));
        Kiem.dung("MapDoMenu chi mo menu su kien khi idMenu == 0",
                doMenu.contains("idMenu == 0 && player.idxSuKienWorldCup >= 0"));

        // ================= 5. MA NGUON: DUNG VAT PHAM =================
        final String dung = doc("src/real/doUsePotion.java");
        // 108 truoc day nam trong nhom "khong lam gi" -> vat pham bi tru mat luon.
        Kiem.sai("108 khong con trong nhom case rong cua doUsePotion",
                coCaseRong(dung, 108));
        Kiem.dung("doUsePotion co nhanh mo hop qua may man",
                dung.contains("case SuKienWorldCup.ITEM_HOP_MAY_MAN:"));
        Kiem.dung("qua bong bac (160) duoc tra lai chu khong bi nuot",
                dung.contains("case TYPE_QUA_BONG_BAC:"));

        // ================= 6. QUAI ROI BONG VANG =================
        final String quai = doc("src/real/Monster.java");
        Kiem.dung("Monster goi quaiRoiBongVang",
                quai.contains("SuKienWorldCup.quaiRoiBongVang(m, p, this, droplist)"));
        Kiem.dung("chi goi khi su kien dang dien ra",
                quai.contains("if (SuKienWorldCup.dangDienRa())"));

        // ================= 7. CAU HINH =================
        final String tmpl = doc("../vps/server.ini.tmpl");
        Kiem.dung("server.ini.tmpl co sv.worldCupBatDau", tmpl.contains("sv.worldCupBatDau="));
        Kiem.dung("server.ini.tmpl co sv.worldCupKetThuc", tmpl.contains("sv.worldCupKetThuc="));
        Kiem.dung("server.ini.tmpl co sv.worldCupHanNhanQua",
                tmpl.contains("sv.worldCupHanNhanQua="));
        // Mac dinh phai la TAT: deploy khong duoc tu nhien mo su kien.
        Kiem.dung("mac dinh trong tmpl la TAT (de trong)",
                khoaDeTrong(tmpl, "sv.worldCupBatDau"));

        final String cauHinh = doc("src/server/CauHinh.java");
        Kiem.dung("napLai() co ap dung lai moc thoi gian su kien",
                cauHinh.contains("apDungSuKienWorldCup(p);"));

        // ================= 8. SQL =================
        Kiem.dung("thuoc 108 co ten (data_potion)", !String.valueOf(DayDu.giaTri("data_potion", "id", "108", "name")).trim().isEmpty());
        Kiem.dung("htbb_day_du.sql co bang diem tob_top_event_world_cup", DayDu.coBang("tob_top_event_world_cup"));
        Kiem.dung("htbb_day_du.sql co bang trang thai tob_event_world_cup_state", DayDu.coBang("tob_event_world_cup_state"));
        // Them dong moi vao data_potion la doi so o thuoc cua client -> cam.
        Kiem.bang("data_potion dung 162 dong (client cap phat theo so nay)", 162, DayDu.soDong("data_potion"));
    }

    /** Tim "case <n>:" nam trong nhom roi vao "default: break;" (khong co than xu ly). */
    private static boolean coCaseRong(final String nguon, final int id) {
        final int viTri = nguon.indexOf("case " + id + ":");
        if (viTri < 0) {
            return false;
        }
        final int viTriDefault = nguon.indexOf("default:", viTri);
        if (viTriDefault < 0) {
            return false;
        }
        // Giua "case N:" va "default:" chi toan nhan case khac thi N la case rong.
        final String giua = nguon.substring(viTri + ("case " + id + ":").length(), viTriDefault);
        return giua.replaceAll("case\\s+\\d+:", "").trim().isEmpty();
    }

    private static boolean khoaDeTrong(final String noiDung, final String khoa) {
        for (final String d : noiDung.split("\r\n|\n|\r")) {
            final String t = d.trim();
            if (t.startsWith(khoa + "=")) {
                return t.substring(khoa.length() + 1).trim().isEmpty();
            }
        }
        return false;
    }

    private static boolean co(final short[] ds, final int id) {
        if (ds == null) {
            return false;
        }
        for (final short v : ds) {
            if (v == id) {
                return true;
            }
        }
        return false;
    }

    private static String doc(final String duong) {
        try {
            return new String(Files.readAllBytes(Paths.get(duong)), StandardCharsets.UTF_8);
        } catch (final IOException e) {
            return "";
        }
    }

    private KiemSuKienWorldCup() {
    }
}
