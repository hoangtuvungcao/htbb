/**
 * Kich ban SUA TUYEN (nhanh sua-tuyen, sql/nang_cap.sql khoi sua_tuyen_nhiem_vu.sql) - goi tin that nhu client 0.9.7:
 *  st_lang_vung    tai khoan moi Kanto -> roi san 105 ra Pewter (0), Johto -> Blackthorn (70); dong tob_lang_khoi_dau.
 *  st_lang_cu      nhan vat "cu" (xoa dong tob_lang_khoi_dau, nhu nhan vat tao truoc ban sua) chon Kanto -> van ra
 *                  Blackthorn nhu truoc (khong ai bi doi cho).
 *  st_dia_diem     (GM thu, cap 20) buoc dia diem C01.5 (Chansey: "ra dung cho ngoi sao roi", Pallet o 53,19): nghe NPC -> dang lam, KHONG
 *                  sang buoc; sang Pallet nhung chua toi -> van dang lam (co chi duong); di bo toi o 53,19 -> xong, sang
 *                  buoc sau, co thuong, luu CSDL.
 * Du 10 buoc huong dan theo vung: KichBanHuongDan hd_toan_bo (Kanto) / hd_toan_bo_johto. Can -Ddb, -Ddbport, -Dnoi.
 */
class KichBanSuaTuyen {

    static void ket(String t, Boolean ok, String bang) {
        BotChucNang.ket(t, ok, bang);
    }

    static void dangKy() {
        BotChucNang.dk("st_lang_vung", false, KichBanSuaTuyen::langVung);
        BotChucNang.dk("st_lang_cu", false, KichBanSuaTuyen::langCu);
        BotChucNang.dk("st_dia_diem", false, KichBanSuaTuyen::diaDiem);
    }

    /** Co chu chua {@code s} trong cac goi tu moc {@code m}. */
    static boolean coChu(BotChucNang.Bot b, int m, String s) {
        for (int i = m; i < b.su.size(); i++) {
            String c = b.su.get(i).chu;
            if (c != null && c.contains(s)) return true;
        }
        return false;
    }

    static void langVung() throws Exception {
        for (int vung : new int[]{0, 1}) {
            String ten = vung == 0 ? "kanto" : "johto";
            try (BotChucNang.Bot b = KichBanHuongDan.botMoiTinh(vung == 0 ? "ka" : "jo", vung)) {
                KichBanHuongDan.roiSan(b);
                int mong = KichBanHuongDan.langCuaVung(vung);
                ket("st.lang." + ten, b.mapLoad == mong, "vung " + vung + " -> map " + b.mapId + " nap " + b.mapLoad + " (mong " + mong + ")");
                String r = BotChucNang.sql1("SELECT CONCAT(vung, '/', lang) FROM tob_lang_khoi_dau WHERE id_char=" + b.charDb);
                ket("st.lang." + ten + ".csdl", (vung + "/" + mong).equals(r), String.valueOf(r));
                String thieu = "";
                for (int loai : new int[]{5, 21, 0, 1, 2, 22, 7, 8}) if (KichBanHuongDan.oNpc(b.mapLoad, loai) == null) thieu += loai + " ";
                ket("st.lang." + ten + ".npc_huong_dan", thieu.isEmpty(), "lang " + b.mapLoad + " thieu " + thieu);
                KichBanHuongDan.Nv q = KichBanHuongDan.chinh(b, 0, 0);
                ket("st.lang." + ten + ".buoc1_xatu", q != null && q.id == 0 && q.npc == 5, String.valueOf(q));
            }
        }
    }

    static void langCu() throws Exception {
        String u;
        int id;
        try (BotChucNang.Bot b = KichBanHuongDan.botMoiTinh("sc", 0)) {
            u = b.user;
            id = b.charDb;
        }
        Thread.sleep(2000);
        BotChucNang.sql("DELETE FROM tob_lang_khoi_dau WHERE id_char=" + id); // nhu nhan vat tao truoc ban sua
        BotChucNang.Bot b = new BotChucNang.Bot(u);
        try {
            String l = b.dangNhap("123456");
            if (l == null) l = b.vaoGame(0, 0);
            if (l != null) throw new IllegalStateException(l);
            b.bom(1000);
            KichBanHuongDan.roiSan(b);
            ket("st.lang_cu.giu_lang_cu", b.mapLoad == 70, "nhan vat cu Kanto -> nap " + b.mapLoad + " (giu nhu truoc: 70)");
        } finally {
            b.close();
        }
    }

    /**
     * Di bo (BFS 4 huong nhu BotChucNang.diDuong) toi (dx, dy); quai dong co danh nhan vat cap 1 -> moi 4 buoc mau duoi
     * 70% thi uong thuoc (goi 22), nhu nguoi choi that (guc thi server bo qua goi di chuyen).
     */
    static boolean diCoHoiMau(BotChucNang.Bot b, BotChucNang.BanDo bd, int dx, int dy) throws Exception {
        int sx = b.px / 16, sy = b.py / 16;
        int[] truoc = new int[bd.w * bd.h];
        java.util.Arrays.fill(truoc, -1);
        java.util.ArrayDeque<Integer> q = new java.util.ArrayDeque<>();
        int bdau = sy * bd.w + sx, dich = dy * bd.w + dx;
        truoc[bdau] = bdau;
        q.add(bdau);
        while (!q.isEmpty() && truoc[dich] < 0) {
            int c = q.poll(), cx = c % bd.w, cy = c / bd.w;
            for (int[] h : new int[][]{{1, 0}, {-1, 0}, {0, 1}, {0, -1}}) {
                int nx = cx + h[0], ny = cy + h[1];
                if (nx < 0 || ny < 0 || nx >= bd.w || ny >= bd.h || !bd.diDuoc(nx, ny) || bd.laCong(nx, ny)) continue;
                int n = ny * bd.w + nx;
                if (truoc[n] >= 0) continue;
                truoc[n] = c;
                q.add(n);
            }
        }
        if (truoc[dich] < 0) return false;
        java.util.List<Integer> duong = new java.util.ArrayList<>();
        for (int c = dich; c != bdau; c = truoc[c]) duong.add(c);
        java.util.Collections.reverse(duong);
        int k = 0;
        for (int c : duong) {
            b.diToi((c % bd.w) * 16 + 8, (c / bd.w) * 16 + 8);
            b.bom(210);
            if (++k % 4 == 0 && b.maxhp > 0 && b.hp * 10 < b.maxhp * 7) {
                b.gui(22, (byte) 1);
                b.bom(200);
            }
            if (b.dut) return false;
        }
        return true;
    }

    static final int C01_5 = 14; // chi so chuoi chinh cua data_quest_moi 1005 (901..910, 1001..1005)

    static void diaDiem() throws Exception {
        String u;
        int id;
        // tai khoan GM thu (kt0xx, sv.adminAccounts): len cap 20 nhu nguoi choi da qua huong dan - cap 1 bi quai dong co
        // ha giua duong (guc thi server bo qua goi di chuyen)
        try (BotChucNang.Bot b = BotChucNang.botMoi(0)) {
            KichBanHuongDan.roiSan(b);
            b.gm("slv " + b.ten + " 20 0", 1200);
            u = b.user;
            id = b.charDb;
        }
        // doi server luu xong luc thoat (luu sau ghi de) roi moi dat tien do
        String isf0 = null;
        Thread.sleep(6000);
        for (int lan = 0; lan < 6; lan++) {
            BotChucNang.sql("UPDATE tob_char_quest SET isfinish='" + C01_5 + ",0,0', itemget='0', monskilled='0' WHERE id_char=" + id);
            Thread.sleep(4000); // con giu 4 giay = khong bi lan luu luc thoat ghi de
            isf0 = BotChucNang.sql1("SELECT isfinish FROM tob_char_quest WHERE id_char=" + id);
            if (isf0 != null && isf0.startsWith(C01_5 + ",0")) break;
        }
        ket("st.dd.dat_tien_do", isf0 != null && isf0.startsWith(C01_5 + ",0"), isf0);
        BotChucNang.Bot b = new BotChucNang.Bot(u);
        try {
            String l = b.dangNhap("123456");
            if (l == null) l = b.vaoGame(0, 0);
            if (l != null) throw new IllegalStateException(l);
            b.bom(1500);
            KichBanHuongDan.Nv q = KichBanHuongDan.chinh(b, 0, 0);
            ket("st.dd.co_the_nhan", q != null && q.id == C01_5 && q.loai == 3, String.valueOf(q));
            if (q == null) return;
            if (!KichBanHuongDan.laLang(b.mapLoad)) KichBanHuongDan.quaCong(b, 0);
            String toi = KichBanHuongDan.toiNpc(b, q.npc);
            ket("st.dd.toi_npc_giao", toi == null, toi == null ? "NPC " + q.npc : toi);
            long xu0 = b.xu;
            int m = b.moc();
            KichBanHuongDan.gui64(b, 1, q.id); // client 0.9.7: nghe het loi thoai -> gui thang "tra"
            b.bom(1500);
            KichBanHuongDan.Nv sau = KichBanHuongDan.chinh(b, m, 0);
            KichBanHuongDan.Nv dang = KichBanHuongDan.chinh(b, m, 2);
            ket("st.dd.nghe_npc_chua_xong", (sau == null || sau.id == C01_5) && dang != null && dang.id == C01_5,
                    "co the nhan " + sau + ", dang lam " + dang + " | " + KichBanHuongDan.chuSau(b, m));
            ket("st.dd.chi_duong", dang != null && dang.chu.contains("Pallet") && coChu(b, m, "Tới nơi là hoàn thành"),
                    dang == null ? KichBanHuongDan.chuSau(b, m) : dang.chu);
            String isf = BotChucNang.sql1("SELECT isfinish FROM tob_char_quest WHERE id_char=" + id);
            ket("st.dd.luu_dang_lam", isf != null && isf.startsWith(C01_5 + ",2"), isf);
            // noi chuyen lan nua truoc khi toi: van chan
            int m2 = b.moc();
            KichBanHuongDan.gui64(b, 1, q.id);
            b.bom(1200);
            KichBanHuongDan.Nv sau2 = KichBanHuongDan.chinh(b, m2, 0);
            ket("st.dd.noi_lai_chua_toi_van_chan", sau2 == null || sau2.id == C01_5, String.valueOf(sau2));
            // sang Pallet (cong bac lang), dung o cho den - chua toi diem
            int m3 = b.moc();
            boolean qua = KichBanHuongDan.quaCong(b, 0);
            ket("st.dd.ra_pallet", qua && b.mapLoad == 1, "map " + b.mapId + " nap " + b.mapLoad);
            b.diToi(b.px, b.py); // mot goi di chuyen tai cho (o cong)
            b.bom(1200);
            isf = BotChucNang.sql1("SELECT isfinish FROM tob_char_quest WHERE id_char=" + id);
            ket("st.dd.o_cong_chua_xong", isf != null && isf.startsWith(C01_5 + ","), isf + " o " + b.px / 16 + "," + b.py / 16);
            ket("st.dd.bao_khi_vao_ban_do", coChu(b, m3, "Điểm nhiệm vụ"), KichBanHuongDan.chuSau(b, m3));
            // di bo toi o 53,19
            BotChucNang.BanDo bd = BotChucNang.BanDo.nap(b.mapLoad);
            KichBanHuongDan.dongBoViTri(b);
            int m4 = b.moc();
            boolean di = bd != null && diCoHoiMau(b, bd, 53, 19);
            b.bom(1500);
            // quai tren dong co danh choang / dong bang -> server bo qua vai goi di chuyen (vi tri server tut lai sau bot):
            // nhu client that sau khi het choang, gui lai vi tri dang dung vai lan
            for (int lan = 0; lan < 8; lan++) {
                String f = BotChucNang.sql1("SELECT isfinish FROM tob_char_quest WHERE id_char=" + id);
                if (f != null && !f.startsWith(C01_5 + ",")) break;
                b.diToi(53 * 16 + 8, 19 * 16 + 8);
                b.bom(1200);
            }
            ket("st.dd.di_bo_toi_diem", di, "o " + b.px / 16 + "," + b.py / 16);
            KichBanHuongDan.Nv tiep = KichBanHuongDan.chinh(b, m4, 0);
            isf = BotChucNang.sql1("SELECT isfinish FROM tob_char_quest WHERE id_char=" + id);
            ket("st.dd.toi_noi_xong", tiep != null && tiep.id == C01_5 + 1 && isf != null && isf.startsWith((C01_5 + 1) + ","),
                    "tiep " + tiep + " isfinish " + isf + " | " + KichBanHuongDan.chuSau(b, m4));
            ket("st.dd.loi_ket", coChu(b, m4, "vòng cỏ cháy xém"), KichBanHuongDan.chuSau(b, m4));
            b.lamMoi();
            ket("st.dd.co_thuong", b.xu > xu0, "xu " + xu0 + " -> " + b.xu);
        } finally {
            b.close();
        }
        KichBanHuongDan.logSach();
    }
}
