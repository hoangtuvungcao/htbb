import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;

/**
 * Kich ban HUONG DAN TAN THU (nhanh huong-dan, sql/htbb.sql (khoi them_huong_dan_tan_thu.sql)) - goi tin that nhu client 0.9.7 (-Dban=907):
 *  hd_toan_bo     tai khoan MOI TINH: san 105 -> lang -> menu Lam / Bo qua -> 10 buoc (di bo toi tung NPC theo cMap, nhan /
 *                 tra -64, ra dong Pallet qua cong, danh Caterpie, uong thuoc, cong diem ky nang, mac Ruy bang, tha cau o
 *                 ao lang) -> ban giao chuong 1 (C01.1 o Xatu); kiem thuong (khong luong / luong khoa), log server sach.
 *  hd_bo_qua      tai khoan moi: Bo qua (menu moi) -> hop xac nhan -32 id 96 -> "Khong" khong doi gi, "Dong y" -> chuong 1,
 *                 nhan du thuoc / Ruy bang / xu cua 10 buoc; bo qua lan 2 bi tu choi; So tay khong con muc Bo qua.
 *  hd_so_tay      So tay (-90 muc 2): dang huong dan co muc Bo qua + Huong dan tan thu; danh sach cong dung NPC mo duoc.
 *  hd_tien_do_cu  nhan vat dang lam buoc huong dan luu theo du lieu cu (mang tien do lech mau) -> vao lai la nhan lai buoc.
 * Can -Ddb (tao tai khoan moi, sua tien do) va -Dnoi (doc server/cMap, du_lieu/diem_cau_ca.csv).
 */
class KichBanHuongDan {

    static void ket(String t, Boolean ok, String bang) {
        BotChucNang.ket(t, ok, bang);
    }

    static void dangKy() {
        BotChucNang.dk("hd_toan_bo", false, () -> toanBo(0));
        BotChucNang.dk("hd_toan_bo_johto", false, () -> toanBo(1)); // sua-tuyen: nhan vat Johto -> Blackthorn, du 10 buoc
        BotChucNang.dk("hd_bo_qua", false, KichBanHuongDan::boQua);
        BotChucNang.dk("hd_so_tay", false, KichBanHuongDan::soTay);
        BotChucNang.dk("hd_tien_do_cu", false, KichBanHuongDan::tienDoCu);
    }

    static final int SO_BUOC = 10;
    static final int NPC_SO_TAY = -527, MENU_MOI = 40, MENU_NPC = 42, POPUP_BO_QUA = 96;

    // ------------------------------------------------------------------ goi -64 day du

    /** Mot dong -64: nhom, id, main_sub, ten, npc (nhan / tra), loai, muc {id, co, can}. */
    static final class Nv {
        int nhom, id, ms, npc = -1, loai = -1;
        String ten, chu = "";
        final List<int[]> muc = new ArrayList<>();

        public String toString() {
            return ten + "#" + id + "/" + ms + " npc" + npc + " t" + loai;
        }
    }

    static List<Nv> ds(BotChucNang.Bot b, int tu, int nhom) {
        List<Nv> kq = null;
        for (int i = tu; i < b.su.size(); i++) {
            BotChucNang.SuKien e = b.su.get(i);
            if (e.cmd != -64 || e.d == null || e.d.length < 2 || e.d[0] != nhom) continue;
            try {
                DataInputStream r = new DataInputStream(new ByteArrayInputStream(e.d));
                r.readByte();
                int n = r.readByte();
                List<Nv> l = new ArrayList<>();
                for (int k = 0; k < n; k++) {
                    Nv q = new Nv();
                    q.nhom = nhom;
                    q.id = r.readShort();
                    q.ms = r.readByte();
                    q.ten = r.readUTF();
                    if (nhom == 0) {
                        q.npc = r.readByte();
                        q.chu = r.readUTF();
                        q.loai = r.readByte();
                        r.readByte();
                    } else if (nhom == 1) {
                        q.npc = r.readByte();
                        q.chu = r.readUTF();
                        r.readUTF();
                        r.readUTF();
                    } else {
                        q.loai = r.readByte();
                        q.chu = r.readUTF();
                        q.npc = r.readByte();
                        r.readByte();
                        r.readUTF();
                        if (q.loai == 2 || q.loai == 0) {
                            int m = r.readByte();
                            for (int j = 0; j < m; j++) q.muc.add(new int[]{r.readShort(), r.readShort(), r.readShort()});
                        } else if (q.loai == 4) {
                            r.skipBytes(4);
                        }
                    }
                    l.add(q);
                }
                kq = l;
            } catch (IOException ex) {
                // goi cat ngan
            }
        }
        return kq;
    }

    static Nv chinh(List<Nv> l) {
        if (l != null) for (Nv q : l) if (q.ms == 0) return q;
        return null;
    }

    static Nv chinh(BotChucNang.Bot b, int tu, int nhom) {
        return chinh(ds(b, tu, nhom));
    }

    static void gui64(BotChucNang.Bot b, int type, int id) throws IOException {
        b.gui(-64, (byte) type, (short) id, (byte) 0);
    }

    // ------------------------------------------------------------------ ban do / NPC

    /** NPC tinh {x, y, loai} cuoi tep cMap (cung dinh dang com.htbb.pokemon.DinhDangBanDo). */
    static List<int[]> npcCMap(int load) {
        List<int[]> kq = new ArrayList<>();
        try {
            byte[] b = Files.readAllBytes(new File(BotChucNang.NOI, "server/cMap/" + load).toPath());
            int p = 2 + (b[0] & 255) * (b[1] & 255);
            while ((b[p] & 255) != 255) p += 4 + (b[p + 2] & 255) * (b[p + 3] & 255);
            p++;
            while ((b[p] & 255) != 255) p += 3;
            p++;
            while ((b[p] & 255) != 255 && (b[p] & 255) != 254) p += 3;
            p += 2;
            int n = b[p] & 255;
            p += 1 + n * 6;
            if (p >= b.length) return kq;
            int nn = b[p++] & 255;
            for (int i = 0; i < nn; i++, p += 3) kq.add(new int[]{b[p] & 255, b[p + 1] & 255, b[p + 2] & 255});
        } catch (Exception e) {
            // khong doc duoc
        }
        return kq;
    }

    static int[] oNpc(int load, int loai) {
        for (int[] n : npcCMap(load)) if (n[2] == loai) return n;
        return null;
    }

    static void dongBoViTri(BotChucNang.Bot b) {
        if (b.px <= 0 && b.py <= 0) {
            b.px = b.tx * 16 + 8;
            b.py = b.ty * 16 + 8;
        }
    }

    /** Di bo toi canh NPC loai {@code loai} tren ban do hien tai; null = toi noi, chuoi = ly do. */
    static String toiNpc(BotChucNang.Bot b, int loai) throws IOException {
        int[] n = oNpc(b.mapLoad, loai);
        if (n == null) return "NPC " + loai + " khong co tren ban do " + b.mapId + " (nap " + b.mapLoad + ")";
        BotChucNang.BanDo bd = BotChucNang.BanDo.nap(b.mapLoad);
        if (bd == null) return "khong doc ban do " + b.mapLoad;
        dongBoViTri(b);
        int[][] thu = {{0, 1}, {0, 2}, {1, 1}, {-1, 1}, {1, 0}, {-1, 0}, {0, -1}};
        for (int[] d : thu) {
            int x = n[0] + d[0], y = n[1] + d[1];
            if (!bd.diDuoc(x, y) || bd.laCong(x, y)) continue;
            if (b.diDuong(bd, x, y)) return null;
        }
        int[] g = bd.oGan(n[0], n[1] + 1, 3);
        if (g != null && b.diDuong(bd, g[0], g[1])) return null;
        return "khong di bo toi NPC " + loai + " o " + n[0] + "," + n[1] + " tu " + b.px / 16 + "," + b.py / 16;
    }

    /** Qua cong co chi so dich (Map.locationMap) {@code toM}: tu lang 0 = cong bac ra dong co Pallet; tu Pallet 0 = ve lang. */
    static boolean quaCong(BotChucNang.Bot b, int toM) throws IOException {
        BotChucNang.BanDo bd = BotChucNang.BanDo.nap(b.mapLoad);
        int nhom = KichBanDS2.nhomCua(b, b.mapId);
        if (nhom < 0) nhom = KichBanDS2.nhomCua(b, b.mapLoad); // ban sao lang (301 / 1701 / 1901) dung bang cong cua lang goc
        if (bd == null || nhom < 0 || b.viTriMap == null) return false;
        int truoc = b.mapId;
        for (int[] g : bd.cong) {
            if (g[2] != toM || toM >= b.viTriMap[nhom].length || b.viTriMap[nhom][toM][0] < 0) continue;
            int[] dung = bd.oTruocCong(g[0], g[1]);
            dongBoViTri(b);
            if (dung == null || !b.diDuong(bd, dung[0], dung[1])) continue;
            short[] loc = b.viTriMap[nhom][toM];
            int m = b.moc();
            b.gui(12, (short) loc[0], (short) loc[1], (short) loc[2]);
            b.cho(x -> x.coLenh(m, 12) || x.dut, 6000);
            b.gui(12, (short) -500, (short) 0, (short) 0);
            b.bom(1200);
            b.px = b.tx * 16 + 8;
            b.py = b.ty * 16 + 8;
            if (b.mapId != truoc) return true;
        }
        return false;
    }

    /** Danh mot quai mau {@code tpl} (di bo toi canh); true neu ha duoc. */
    static boolean haMotCon(BotChucNang.Bot b, int tpl) throws IOException {
        List<Integer> song = KichBanDS.timQuai(b, 0, 260);
        BotChucNang.BanDo bd = BotChucNang.BanDo.nap(b.mapLoad);
        dongBoViTri(b);
        Integer chon = null;
        int gan = Integer.MAX_VALUE;
        for (int id : song) {
            int[] q = b.quai.get(id);
            if (q == null || q[5] != tpl || q[2] <= 0) continue;
            int d = Math.abs(q[0] - b.px) + Math.abs(q[1] - b.py);
            if (d < gan) { gan = d; chon = id; }
        }
        if (chon == null || bd == null) return false;
        for (int lan = 0; lan < 60; lan++) {
            int[] q = b.quai.get(chon);
            if (q == null) return false;
            if (Math.abs(q[0] - b.px) > 28 || Math.abs(q[1] - b.py) > 28) {
                int[] o = bd.oGan(q[0] / 16, q[1] / 16, 2);
                if (o != null) b.diDuong(bd, o[0], o[1]);
            }
            b.gui(9, (short) (int) chon, (byte) 0);
            b.bom(350);
            b.gui(7, (short) (int) chon);
            b.bom(200);
            q = b.quai.get(chon);
            if (q != null && q[2] <= 0 && q[2] != -1) return true;
            if (b.dut) return false;
        }
        return false;
    }

    static String chuSau(BotChucNang.Bot b, int m) {
        return b.chuTu(m).replace('\n', ' ');
    }

    // ------------------------------------------------------------------ tai khoan moi

    /** Tai khoan chua co nhan vat (tao moi trong account.team_user khi can - chi may thu, can -Ddb). */
    static BotChucNang.Bot botMoiTinh(String tienTo) throws Exception {
        return botMoiTinh(tienTo, 0);
    }

    /** Tai khoan moi tinh, nhan vat tao o vung {@code vung} (0 Kanto, 1 Johto). */
    static BotChucNang.Bot botMoiTinh(String tienTo, int vung) throws Exception {
        String ten = tienTo + String.valueOf(System.currentTimeMillis() % 100000000L);
        BotChucNang.sql("INSERT INTO account.team_user (username,password,phone,regdate,ban,provider,fromgame) VALUES ('" + ten
                + "',PASSWORD('123456'),'',DATE_SUB(NOW(), INTERVAL 30 DAY),0,0,0)");
        BotChucNang.sql("INSERT INTO 5h_active (userID,username,time_end) SELECT id,username,'1894945812' FROM account.team_user WHERE username='" + ten + "'");
        BotChucNang.Bot b = new BotChucNang.Bot(ten);
        String l = b.dangNhap("123456");
        if (l != null) throw new IllegalStateException(ten + " dang nhap: " + l);
        l = b.vaoGame(0, vung);
        if (l != null) throw new IllegalStateException(ten + " " + l);
        BotChucNang.duTuoiChongClone(b);
        return b;
    }

    /** Roi san 105 nhu client (het loi ong noi -> changeMap(0, x, y)), vao lang quoc gia. */
    static void roiSan(BotChucNang.Bot b) throws IOException {
        if (b.mapId != 105) return;
        int m = b.moc();
        b.gui(12, (short) 0, (short) 14, (short) 12);
        b.cho(x -> x.coLenh(m, 12), 6000);
        b.gui(12, (short) -500, (short) 0, (short) 0);
        b.bom(2500);
        b.px = b.tx * 16 + 8;
        b.py = b.ty * 16 + 8;
    }

    static boolean laLang(int load) {
        return load == 0 || load == 70 || load == 80;
    }

    static int[] diemCau(int load) {
        try {
            for (String l : Files.readAllLines(new File(BotChucNang.GOC, "du_lieu/diem_cau_ca.csv").toPath())) {
                String[] f = l.split(",");
                if (f.length >= 8 && f[0].equals(String.valueOf(load))) return new int[]{Integer.parseInt(f[6]), Integer.parseInt(f[7])};
            }
        } catch (Exception e) {
            // khong co tep
        }
        return null;
    }

    // ------------------------------------------------------------------ hd_toan_bo

    /** Lang khoi dau dung cua vung (sua-tuyen): Kanto -> Pewter 0, Johto -> Blackthorn 70. */
    static int langCuaVung(int vung) {
        return vung == 1 ? 70 : 0;
    }

    static void toanBo(int vung) throws Exception {
        long bd = System.currentTimeMillis();
        try (BotChucNang.Bot b = botMoiTinh(vung == 1 ? "hj" : "hd", vung)) {
            Nv q0 = chinh(b, 0, 0);
            ket("hd.tao_moi.san_105", b.mapId == 105, "map " + b.mapId);
            ket("hd.buoc1.xatu_noi_chuyen", q0 != null && q0.id == 0 && q0.npc == 5 && q0.loai == 3, String.valueOf(q0));
            long thuoc0 = b.soThuoc(1), ether0 = b.soThuoc(4), xu0 = b.xu;
            int m = b.moc();
            roiSan(b);
            ket("hd.vao_lang", laLang(b.mapLoad), "map " + b.mapId + " nap " + b.mapLoad);
            ket("hd.lang_dung_vung." + (vung == 1 ? "johto" : "kanto"), b.mapLoad == langCuaVung(vung),
                    "vung " + vung + " -> lang nap " + b.mapLoad + " (mong " + langCuaVung(vung) + ")");
            boolean moi = b.doiMenu(m, NPC_SO_TAY, MENU_MOI, 3000) == 2;
            ket("hd.moi_lam_bo_qua", moi && b.timMuc("Bỏ qua") == 1, chuSau(b, m));
            int m2 = b.moc();
            b.menu(NPC_SO_TAY, MENU_MOI, 0);
            b.bom(800);
            ket("hd.chon_lam", BotChucNang.khongDau(chuSau(b, m2)).contains("xatu"), chuSau(b, m2));
            // NPC cua 10 buoc deu dung trong lang nay
            String thieu = "";
            for (int loai : new int[]{5, 21, 0, 1, 2, 22, 7}) if (oNpc(b.mapLoad, loai) == null) thieu += loai + " ";
            ket("hd.npc_du_trong_lang", thieu.isEmpty(), "lang " + b.mapLoad + " thieu " + thieu);

            Nv q = chinh(b, 0, 0);
            List<String> buoc = new ArrayList<>();
            boolean dung = true;
            for (int k = 0; k < SO_BUOC && q != null && dung; k++) {
                String ly = lamBuoc(b, q, k);
                buoc.add(q.ten + (ly == null ? "" : " <" + ly + ">"));
                ket("hd.buoc" + (k + 1) + "." + BotChucNang.khongDau(q.ten).replace(' ', '_'), ly == null, ly == null ? q.toString() : ly);
                if (ly != null) dung = false;
                Nv sau = chinh(b, 0, 0);
                if (sau == null || sau.id != q.id + 1) {
                    if (dung) ket("hd.sang_buoc_sau." + (k + 1), false, "sau " + q + " la " + sau);
                    dung = false;
                }
                q = sau;
            }
            ket("hd.ban_giao_chuong_1", q != null && q.id == SO_BUOC && q.npc == 5, "nhiem vu chinh tiep: " + q);
            String isf = BotChucNang.sql1("SELECT isfinish FROM tob_char_quest WHERE id_char=" + b.charDb);
            ket("hd.luu_csdl", isf != null && isf.startsWith(SO_BUOC + ","), isf);
            b.lamMoi();
            String lk = BotChucNang.sql1("SELECT CONCAT(luong,'/',luonglock) FROM tob_char WHERE id=" + b.charDb);
            ket("hd.khong_luong", "0/0".equals(lk) || (b.luong == 0 && b.lk == 0), "luong/luong khoa " + lk + " | bot " + b.luong + "/" + b.lk);
            Thread.sleep(1500);
            String mac = BotChucNang.sql1("SELECT COUNT(*) FROM tob_char WHERE id=" + b.charDb + " AND CONCAT(',', equip, ',') LIKE '%,1001,%'");
            // buoc "Mac do moi" chi tinh khi mac dung Ruy bang 1001 (diem moc nang_cap, muc_tieu_id 1001) -> da qua = da nhan + mac
            boolean coRb = b.timDo(d -> d[1] == 1001) != null || b.dangMacTpl.contains(1001) || "1".equals(mac)
                    || buoc.size() >= 7 && !buoc.get(6).contains("<");
            ket("hd.thuong_vat_pham", coRb && b.xu > xu0, "Ruy bang 1001 (tui / dang mac): " + coRb
                    + " thuoc " + thuoc0 + "->" + b.soThuoc(1) + " ether " + ether0 + "->" + b.soThuoc(4) + " xu " + xu0 + "->" + b.xu);
            long phut = (System.currentTimeMillis() - bd) / 1000;
            ket("hd.thoi_gian_bot", true, phut + " giay; buoc: " + String.join(" | ", buoc));
        }
        logSach();
    }

    /** Lam mot buoc tu luc "co the nhan" toi luc tra xong. null = ok. */
    static String lamBuoc(BotChucNang.Bot b, Nv q, int k) throws Exception {
        if (!laLang(b.mapLoad) && !quaCong(b, 0)) return "khong ve duoc lang tu " + b.mapId;
        String l = toiNpc(b, q.npc);
        if (l != null) return "di toi NPC giao: " + l;
        int m = b.moc();
        if (q.loai == 3) {
            gui64(b, 1, q.id); // client: nghe het loi -> tra thang
            b.bom(1500);
            return chinh(b, m, 0) != null ? null : "noi chuyen khong sang buoc: " + chuSau(b, m);
        }
        gui64(b, 0, q.id);
        b.bom(1200);
        Nv dang = chinh(b, m, q.loai == 1 ? 1 : 2);
        if (dang == null && chinh(b, m, 1) == null) return "nhan khong duoc: " + chuSau(b, m);
        if (q.loai == 0) {
            if (!quaCong(b, 0) || b.mapLoad != 1) return "khong ra duoc dong Pallet (map " + b.mapId + ")";
            for (int lan = 0; lan < 40 && chinh(b, m, 1) == null; lan++) {
                if (!haMotCon(b, 2)) b.bom(1500);
                b.gui(22, (byte) 1); // giu mau (HP day thi server tu choi, khong mat binh)
                b.bom(400);
            }
        } else if (q.loai == 2) {
            int vat = dang != null && !dang.muc.isEmpty() ? dang.muc.get(0)[0] : -1;
            if (vat < 0 && BotChucNang.khongDau(q.ten).contains("cau")) vat = 57; // buoc tha cau: goi -64 khong kem vat dau tap
            String lam = hanhDong(b, vat, m);
            if (lam != null) return lam;
        }
        if (chinh(b, m, 1) == null && q.loai != 1) {
            b.bom(1500);
            if (chinh(b, m, 1) == null) return "chua xong muc tieu: " + chuSau(b, m);
        }
        Nv tra = chinh(b, m, 1);
        if (!laLang(b.mapLoad) && !quaCong(b, 0)) return "khong ve duoc lang";
        l = toiNpc(b, tra != null ? tra.npc : q.npc);
        if (l != null) return "di toi NPC tra: " + l;
        int m2 = b.moc();
        gui64(b, 1, q.id);
        b.bom(1500);
        Nv sau = chinh(b, m2, 0);
        return sau != null && sau.id == q.id + 1 ? null : "tra khong duoc: " + chuSau(b, m2);
    }

    /** Muc tieu hanh dong theo vat pham dau tap cua buoc (data_vat_pham_nhiem_vu 51..58). */
    static String hanhDong(BotChucNang.Bot b, int vat, int m) throws Exception {
        switch (vat) {
            case 53: { // dung thuoc khi mau voi: ra dong danh toi khi mat mau roi uong
                if (!quaCong(b, 0) || b.mapLoad != 1) return "khong ra duoc dong Pallet";
                for (int lan = 0; lan < 30 && chinh(b, m, 1) == null; lan++) {
                    haMotCon(b, 2);
                    b.gui(22, (byte) 1); // HP / PP day thi server tu choi ("HP đã đầy"), khong tinh
                    b.bom(500);
                    if (chinh(b, m, 1) == null) { b.gui(22, (byte) 4); b.bom(500); }
                }
                return chinh(b, m, 1) != null ? null : "uong thuoc khong tinh: hp " + b.hp + "/" + b.maxhp + " " + chuSau(b, m);
            }
            case 52: // cong 1 diem ky nang cho chieu 0 (Menu -> Pokemon -> Ky nang -> Hoc)
                for (int o = 0; o < 4 && chinh(b, m, 1) == null; o++) {
                    b.gui(36, (byte) o);
                    b.bom(800);
                }
                return chinh(b, m, 1) != null ? null : "cong diem khong tinh (lv " + b.lv + "): " + chuSau(b, m);
            case 55: { // mac Ruy bang (template 1001) vua nhan
                b.lamMoi();
                int[] rb = b.timDo(d -> d[1] == 1001);
                if (rb == null) return chinh(b, m, 1) != null ? null : "khong co Ruy bang trong tui";
                b.gui(29, (short) rb[0]);
                b.bom(1200);
                return chinh(b, m, 1) != null ? null : "mac Ruy bang khong tinh: " + chuSau(b, m);
            }
            case 57: { // tha cau o ao lang
                int[] c = diemCau(b.mapLoad);
                BotChucNang.BanDo bd = BotChucNang.BanDo.nap(b.mapLoad);
                if (c == null || bd == null) return "khong co diem cau o lang " + b.mapLoad;
                dongBoViTri(b);
                if (!b.diDuong(bd, c[0], c[1])) return "khong di toi diem cau " + c[0] + "," + c[1];
                for (int lan = 0; lan < 3 && chinh(b, m, 1) == null; lan++) {
                    b.chat("cauca");
                    b.bom(1500);
                }
                return chinh(b, m, 1) != null ? null : "tha cau khong tinh: " + chuSau(b, m);
            }
            default:
                return "buoc hanh dong la (vat " + vat + ")";
        }
    }

    static void logSach() {
        String log = System.getProperty("log");
        if (log == null) return;
        try {
            List<String> l = Files.readAllLines(new File(log).toPath());
            String loi = "";
            for (String s : l) if (s.contains("HuongDanTanThu") || (s.contains("Exception") && s.contains("real."))) loi += s + " / ";
            ket("hd.log_khong_loi", loi.isEmpty(), loi.length() > 400 ? loi.substring(0, 400) : loi);
        } catch (Exception e) {
            ket("hd.log_khong_loi", null, "khong doc log: " + e);
        }
    }

    // ------------------------------------------------------------------ hd_bo_qua

    static void boQua() throws Exception {
        try (BotChucNang.Bot b = botMoiTinh("hq")) {
            roiSan(b);
            b.lamMoi();
            long xu0 = b.xu;
            int hp0 = b.soThuoc(1), et0 = b.soThuoc(4);
            int m = b.moc();
            b.menu(NPC_SO_TAY, MENU_MOI, 1);
            b.cho(x -> x.coLenh(m, -32), 3000);
            BotChucNang.SuKien p = b.layLenh(m, -32);
            ket("hq.hop_xac_nhan", p != null && p.so[1] == POPUP_BO_QUA && p.chu.contains("Ruy băng"), p == null ? chuSau(b, m) : p.chu);
            // "Khong": khong doi gi
            int m1 = b.moc();
            b.gui(-32, (short) (p == null ? b.actor : p.so[0]), (byte) POPUP_BO_QUA, "", (byte) 0);
            b.bom(1200);
            String isf = BotChucNang.sql1("SELECT isfinish FROM tob_char_quest WHERE id_char=" + b.charDb);
            ket("hq.khong_dong_y_giu_nguyen", chinh(b, m1, 0) == null && (isf == null || isf.startsWith("0,")), isf + " | " + chuSau(b, m1));
            int m2 = b.moc();
            b.gui(-32, (short) (p == null ? b.actor : p.so[0]), (byte) POPUP_BO_QUA, "", (byte) 1);
            b.bom(2000);
            Nv q = chinh(b, m2, 0);
            ket("hq.sang_chuong_1", q != null && q.id == SO_BUOC && q.npc == 5, String.valueOf(q) + " | " + chuSau(b, m2));
            b.lamMoi();
            boolean rb = b.timDo(d -> d[1] == 1001) != null;
            ket("hq.du_thuong", rb && b.soThuoc(1) - hp0 == 6 && b.soThuoc(4) - et0 == 4 && b.xu - xu0 == 1450,
                    "Ruy bang " + rb + " thuoc +" + (b.soThuoc(1) - hp0) + " ether +" + (b.soThuoc(4) - et0) + " xu +" + (b.xu - xu0));
            String lk = BotChucNang.sql1("SELECT CONCAT(luong,'/',luonglock) FROM tob_char WHERE id=" + b.charDb);
            ket("hq.khong_luong", "0/0".equals(lk), lk);
            // lan 2: khong bo qua them (khong nhan thuong lan nua)
            int m3 = b.moc();
            b.gui(-32, (short) (p == null ? b.actor : p.so[0]), (byte) POPUP_BO_QUA, "", (byte) 1);
            b.bom(1200);
            b.lamMoi();
            ket("hq.lan_hai_tu_choi", b.xu - xu0 == 1450 && BotChucNang.khongDau(chuSau(b, m3)).contains("da xong huong dan"), chuSau(b, m3));
            int m4 = b.moc();
            KichBanDS4.chonQuay(b, "Sổ tay huấn luyện"); // Quay doi thuong -> So tay huan luyen
            b.doiMenu(m4, NPC_SO_TAY, 0, 3000);
            ket("hq.so_tay_het_muc_bo_qua", b.timMuc("Bỏ qua") < 0 && b.timMuc("Hướng dẫn tân thủ") >= 0, String.join(" ; ", b.menuChu));
        }
    }

    // ------------------------------------------------------------------ hd_so_tay

    static void soTay() throws Exception {
        try (BotChucNang.Bot b = botMoiTinh("hs")) {
            roiSan(b);
            int m = b.moc();
            KichBanDS4.chonQuay(b, "Sổ tay huấn luyện");
            int n = b.doiMenu(m, NPC_SO_TAY, 0, 3000);
            int iBo = b.timMuc("Bỏ qua hướng dẫn"), iHd = b.timMuc("công dụng các NPC");
            ket("hs.muc_so_tay", n >= 12 && iBo >= 10 && iHd >= 10, String.join(" ; ", b.menuChu));
            int m2 = b.moc();
            b.menu(NPC_SO_TAY, 0, iHd);
            int k = b.doiMenu(m2, NPC_SO_TAY, MENU_NPC, 3000);
            ket("hs.danh_sach_npc", k >= 12 && b.timMuc("Chansey") >= 0 && b.timMuc("Meowth") >= 0, String.join(" ; ", b.menuChu));
            int iCh = b.timMuc("Chansey");
            int m3 = b.moc();
            b.menu(NPC_SO_TAY, MENU_NPC, iCh);
            b.bom(800);
            ket("hs.cong_dung_chansey", BotChucNang.khongDau(chuSau(b, m3)).contains("hoi day hp"), chuSau(b, m3));
            int m4 = b.moc();
            b.menu(NPC_SO_TAY, 0, iBo);
            b.cho(x -> x.coLenh(m4, -32), 3000);
            ket("hs.bo_qua_tu_so_tay", b.layLenh(m4, -32) != null, chuSau(b, m4));
        }
    }

    // ------------------------------------------------------------------ hd_tien_do_cu

    static void tienDoCu() throws Exception {
        String u;
        int id;
        try (BotChucNang.Bot b = botMoiTinh("hc")) {
            roiSan(b);
            u = b.user;
            id = b.charDb;
        }
        Thread.sleep(2500);
        // buoc 5 (chi so 4, kieu 2 "cong diem ky nang") DANG LAM nhung luu theo ban cu "ha 20 quai" (mang tien do 2 phan tu)
        BotChucNang.sql("UPDATE tob_char_quest SET isfinish='4,2,0', itemget='0,0', monskilled='5,7' WHERE id_char=" + id);
        BotChucNang.Bot b = new BotChucNang.Bot(u);
        try {
            String l = b.dangNhap("123456");
            if (l == null) l = b.vaoGame(0);
            if (l != null) throw new IllegalStateException(l);
            b.bom(1500);
            Nv q = chinh(b, 0, 0);
            ket("hc.lam_lai_buoc", q != null && q.id == 4 && chinh(b, 0, 2) == null, "co the nhan " + q + ", dang lam " + chinh(b, 0, 2));
            String isf = BotChucNang.sql1("SELECT isfinish FROM tob_char_quest WHERE id_char=" + id);
            ket("hc.luu_lai", isf != null && isf.startsWith("4,0"), isf);
        } finally {
            b.close();
        }
    }
}
