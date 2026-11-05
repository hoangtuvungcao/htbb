import java.io.IOException;
import java.util.*;

/**
 * Kich ban "chuyen_luyen" (bao loi rongngoo 05/11/2026): nguoi choi THUONG (khong GM, goi tin nhu client that) o nha
 * Machamp Pewter (103):
 *   1. Che tao Vong co kim cuong cap 54 (-120 kieu 1) - mon moi phai co trong hanh trang NGAY (goi 16/1 do may chu tu gui,
 *      KHONG goi 88 lam moi): bao loi "che tao xong phai doi mot luc lau moi co trong hanh trang".
 *   2. Nang Vong co bach kim cap 44 len +7, mac vao roi thao ra (nhu nguoi choi).
 *   3. Machamp (goi 23 loai 2) > "Chuyển luyện" > nguon +7 > nhan cap 54 > "Chuyển (mất 1 cấp)": phai co thong bao thanh
 *      cong, tru xu, va hanh trang (khong lam moi) thay nhan +6 / nguon +0 ngay; lam moi (88) van dung (may chu da luu).
 *   4. Bam xac nhan lan nua sau khi da xong (menu cu): phai co thong bao, khong im lang.
 * GM (kt0xx) chi dung de cap xu / da / nguyen lieu / cap cho nguoi choi thuong.
 */
class KichBanChuyenLuyen {

    static final int NPC_CL = -535;
    static final int VONG_44 = 134, VONG_54 = 136, MANH_SAO = 1035;

    static void ket(String t, Boolean ok, String bang) {
        BotChucNang.ket(t, ok, bang);
    }

    static void dangKy() {
        BotChucNang.dk("chuyen_luyen", true, KichBanChuyenLuyen::chay);
    }

    /** GM ait mon {@code tpl} cho nguoi choi {@code t}; tra ve id moi (hoac id chong vua tang) / KHONG. */
    static int choMon(BotChucNang.Bot gm, BotChucNang.Bot t, int tpl, int lop) throws IOException {
        t.lamMoi();
        Map<Integer, Integer> cu = new HashMap<>();
        for (int[] d : t.doDung) cu.put(d[0], BotChucNang.Bot.soO(d));
        gm.gm("ait " + t.ten + " " + tpl + " " + lop + " 0 0", 900);
        t.lamMoi();
        int[] moi = t.timDo(d -> d[1] == tpl && !cu.containsKey(d[0]));
        if (moi == null) moi = t.timDo(d -> d[1] == tpl && BotChucNang.Bot.soO(d) > cu.getOrDefault(d[0], Integer.MAX_VALUE));
        return moi == null ? KichBanDS3.KHONG : moi[0];
    }

    static void chay() throws Exception {
        try (BotChucNang.Bot gm = KichBanDS3.botLang(60); BotChucNang.Bot t = BotChucNang.botThuong()) {
            KichBanDS.veLang(t);
            gm.gm("slv " + t.ten + " 56 0", 900);
            gm.gm("smn " + t.ten + " 50000000 0 20000", 800); // luong khoa: bua bao ho +7
            gm.gm("gem " + t.ten + " 8 400 0", 800);
            gm.gm("gem " + t.ten + " 0 40 0", 800);
            for (int i = 0; i < 2; i++) choMon(gm, t, MANH_SAO, 5);
            int a = choMon(gm, t, VONG_44, 0); // lop cua nhan vat (0): lop 5 = "danh cho he khac"
            if (a == KichBanDS3.KHONG) { ket("chuyen_luyen", false, "khong cho duoc mon " + VONG_44 + " " + t.chuTu(0)); return; }
            if (!KichBanDS2.vaoNha(t, 103)) { ket("chuyen_luyen", false, "khong vao duoc nha Machamp tu map " + t.mapId); return; }
            if (BotChucNang.Bot.CHUAN_CLIENT && KichBanHuongDan.oNpc(t.mapLoad, 2) != null) KichBanHuongDan.toiNpc(t, 2);
            t.lamMoi();

            // ---- 1. che tao: mon moi vao hanh trang ngay (khong lam moi)
            Set<Integer> truoc = new HashSet<>();
            for (int[] d : t.doDung) truoc.add(d[0]);
            int m = t.moc();
            t.gui(-120, (byte) 1, (short) VONG_54, (byte) -1, (byte) 0);
            boolean coNgay = t.cho(x -> x.timDo(d -> d[1] == VONG_54 && !truoc.contains(d[0])) != null, 2500);
            String kqCt = t.doiChu(m, 500, "Chế tạo", "Đã vào", "thiếu", "Cần", "Không");
            ket("chuyen_luyen.che_tao_vao_tui_ngay", coNgay, "mon " + VONG_54 + " trong tui (chua lam moi)=" + coNgay + " | " + kqCt + " | " + t.chuTu(m));
            t.lamMoi();
            int[] dn = t.timDo(d -> d[1] == VONG_54 && !truoc.contains(d[0]));
            if (dn == null) { ket("chuyen_luyen", false, "che tao khong ra mon " + VONG_54 + ": " + t.chuTu(m)); return; }
            final int bId = dn[0];

            // ---- 2. nang nguon len +7 (bua tu +6: khong rot), mac vao roi thao ra
            for (int lan = 0; lan < 60; lan++) {
                final int af = a;
                int[] d = t.timDo(x -> x[0] == af);
                if (d == null || d[2] >= 7) break;
                String kq = KichBanDS3.moNangCap(t, a, d[2] >= 6);
                if (!kq.contains("Thành công") && !kq.contains("Thất bại")) { ket("chuyen_luyen", false, "nang cap: " + kq); return; }
            }
            final int a0 = a;
            int[] da = t.timDo(x -> x[0] == a0);
            if (da == null || da[2] != 7) { ket("chuyen_luyen", false, "khong nang duoc nguon len +7: " + Arrays.toString(da)); return; }
            int mMac = t.moc();
            t.gui(29, (short) a);
            t.bom(1000);
            t.lamMoi();
            if (t.dangMac.isEmpty() || !t.dangMacTpl.contains(VONG_44)) { ket("chuyen_luyen", false, "khong mac duoc vong co +7: " + t.dangMacTpl + " " + t.chuTu(mMac)); return; }
            Set<Integer> truocThao = new HashSet<>();
            for (int[] d : t.doDung) truocThao.add(d[0]);
            int mThao = t.moc();
            final int idMac = t.dangMac.get(t.dangMacTpl.indexOf(VONG_44));
            t.gui(29, (short) idMac); // "Tháo ra" = lenh 29 voi id mon dang mac (DungVatPham.thaoTrangBi)
            t.bom(1000);
            t.lamMoi();
            int[] thao = t.timDo(x -> x[1] == VONG_44 && x[2] == 7 && !truocThao.contains(x[0]));
            if (thao == null) thao = t.timDo(x -> x[1] == VONG_44 && x[2] == 7);
            if (thao == null) { ket("chuyen_luyen", false, "thao vong co +7 (id mac " + idMac + ") ra khong thay trong tui: mac " + t.dangMac + " " + t.dangMacTpl + " " + t.chuTu(mThao)); return; }
            a = thao[0];

            // ---- 3. Machamp > Chuyen luyen > nguon > nhan > xac nhan
            m = t.moc();
            t.gui(23, (byte) 2);
            int n = t.doiMenu(m, 2, 0, 3000);
            int vt = t.timMuc("Chuyển luyện");
            ket("chuyen_luyen.muc_machamp", n > 0 && vt >= 0, "menu Machamp: " + t.chuTu(m));
            if (vt < 0) return;
            m = t.moc();
            t.menu(2, 0, vt);
            n = t.doiMenu(m, NPC_CL, 0, 3000);
            int vtNguon = t.timMuc("bạch kim");
            ket("chuyen_luyen.menu_nguon", n > 0 && vtNguon >= 0, "menu nguon: " + t.chuTu(m) + " " + t.menuChu);
            if (vtNguon < 0) return;
            m = t.moc();
            t.menu(NPC_CL, 0, vtNguon);
            n = t.doiMenu(m, NPC_CL, 1, 3000);
            int vtNhan = t.timMuc("kim cương");
            ket("chuyen_luyen.menu_nhan", n > 0 && vtNhan >= 0, "menu nhan: " + t.chuTu(m) + " " + t.menuChu);
            if (vtNhan < 0) return;
            m = t.moc();
            t.menu(NPC_CL, 1, vtNhan);
            n = t.doiMenu(m, NPC_CL, 2, 3000);
            int vtXn = t.timMuc("mất 1 cấp");
            ket("chuyen_luyen.menu_xac_nhan", n > 0 && vtXn >= 0, "menu xac nhan: " + t.chuTu(m) + " " + t.menuChu);
            if (vtXn < 0) return;
            long xu0 = t.xu;
            m = t.moc();
            t.menu(NPC_CL, 2, vtXn);
            String tb = t.doiChu(m, 4000, "Chuyển luyện thành công", "Cần", "Không", "Hãy", "Giá", "Món", "Đang", "tạm");
            ket("chuyen_luyen.thong_bao", tb != null && tb.contains("thành công"), tb + " | " + t.chuTu(m));
            final int aF = a;
            boolean tuiNgay = t.cho(x -> {
                int[] xa = x.timDo(d -> d[0] == aF), xb = x.timDo(d -> d[0] == bId);
                return xa != null && xb != null && xa[2] == 0 && xb[2] == 6;
            }, 2500);
            int[] na = t.timDo(d -> d[0] == aF), nb = t.timDo(d -> d[0] == bId);
            ket("chuyen_luyen.tui_cap_nhat_ngay", tuiNgay, "khong lam moi: nguon " + Arrays.toString(na) + " nhan " + Arrays.toString(nb));
            ket("chuyen_luyen.tru_xu", t.xu < xu0, "xu " + xu0 + " -> " + t.xu);
            t.lamMoi();
            na = t.timDo(d -> d[0] == aF);
            nb = t.timDo(d -> d[0] == bId);
            ket("chuyen_luyen.sau_lam_moi", na != null && nb != null && na[2] == 0 && nb[2] == 6,
                    "nguon " + Arrays.toString(na) + " nhan " + Arrays.toString(nb));

            // ---- 4. bam lai xac nhan cu: phai co thong bao (khong im lang)
            m = t.moc();
            t.menu(NPC_CL, 2, vtXn);
            String lai = t.doiChu(m, 3000, "");
            ket("chuyen_luyen.bam_lai_co_thong_bao", lai != null, "bam lai: " + t.chuTu(m));
        }
    }
}
