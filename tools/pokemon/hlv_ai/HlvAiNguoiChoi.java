import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.net.SocketException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Mot Huan luyen vien AI: choi NHU NGUOI qua giao thuc that (BotChucNang.Bot = client), khong GM, khong lenh dac biet.
 * Ke thua nguoi choi tu nhien KichBanCap140.NguoiChoi (nhiem vu chinh / phu / cua ai, Gym, mua thuoc + vu khi bang xu
 * kiem duoc, nang cap vu khi, mac do, huong dan tan thu...). Moi phien: dang nhap -> luan phien hoat dong theo tinh cach
 * (trong so) va cong tac hlv_ai.hoat_dong -> thoat khi dich vu bao het phien. Moi viec ghi DichVuHlvAi.dem (bang phu
 * tinh nang trong bao cao).
 */
final class HlvAiNguoiChoi extends KichBanCap140.NguoiChoi {

    /** Hoat dong (khop hlv_ai.hoat_dong). */
    static final String[] VIEC = {"nhiem_vu", "cay", "treo", "gym", "boss", "pk", "dau_truong", "pho_ban", "doi", "chat", "ban_be",
        "giao_dich", "cho", "sinh_thai", "vuon", "nha_tre", "bat", "nang_cap", "su_kien"};
    /** Trong so viec theo tinh cach: cay / pvp / buon / xa_hoi / thuong (cung thu tu VIEC). */
    static final int[][] TRONG_SO = {
        {30, 22, 10, 8, 6, 2, 2, 6, 1, 2, 1, 1, 2, 1, 1, 1, 4, 6, 2},
        {14, 14, 4, 8, 6, 22, 15, 5, 2, 3, 1, 1, 1, 1, 0, 0, 2, 6, 2},
        {12, 10, 4, 3, 1, 0, 1, 1, 1, 4, 2, 8, 22, 14, 10, 8, 5, 3, 2},
        {18, 12, 5, 4, 2, 4, 2, 2, 12, 15, 10, 6, 2, 5, 2, 2, 5, 2, 8},
        {20, 10, 20, 3, 1, 1, 1, 2, 2, 8, 2, 1, 3, 10, 8, 3, 8, 2, 10}};
    static final String[] TINH_CACH = {"cay", "pvp", "buon", "xa_hoi", "thuong"};
    /** Viec cho EXP: tu cap muc tieu tro len giam con 10% trong so. */
    static final java.util.Set<String> VIEC_EXP = new java.util.HashSet<>(Arrays.asList("nhiem_vu", "cay", "treo", "pho_ban", "boss"));

    final DichVuHlvAi.Ho ho;
    final Random rd;
    volatile boolean dungLai;
    volatile String viec = "dang_nhap";
    volatile long vaoLuc, henRa;
    volatile boolean daChao;
    int soVaoLaiPhien;
    final ArrayDeque<String> nhatKyGan = new ArrayDeque<>();
    /** Su kien xa hoi den (nghe goi tin): {loai, id/ten, noi dung}. */
    final ConcurrentLinkedDeque<Object[]> hop = new ConcurrentLinkedDeque<>();
    volatile String bossMap = null;
    volatile long bossLuc;
    volatile boolean coDoi;
    long lanChat, lanXaHoi;
    volatile Thread luong;
    int ngayDanh = -1;

    boolean dangChay() {
        return luong != null && luong.isAlive();
    }

    HlvAiNguoiChoi(DichVuHlvAi.Ho ho) {
        super(ho.nhanh());
        this.ho = ho;
        this.user = ho.taiKhoan;
        this.rd = new Random(ho.taiKhoan.hashCode() * 31L + System.nanoTime());
        // vong tinh nang kiem thu (quet moi menu / NPC lien tuc) khong phai cach nguoi choi that choi: tat
        for (int m : new int[]{5, 10, 15, 20, 25, 30, 35, 40}) mocTinhNang.add(m);
        daGiaoDich = true;
    }

    @Override
    void ghi(String s) {
        String d = String.format("[%s %s lv%d map%d %s] %s", ho.ten, new java.text.SimpleDateFormat("HH:mm:ss").format(new java.util.Date()),
                b == null ? 0 : b.lv, b == null ? -1 : b.mapId, viec, s);
        synchronized (nhatKyGan) {
            nhatKyGan.addLast(d);
            while (nhatKyGan.size() > 60) nhatKyGan.removeFirst();
        }
        if (DichVuHlvAi.CHI_TIET) System.out.println(d);
    }

    // ================================================================== dang nhap
    @Override
    void vao() throws Exception {
        Exception cuoi = null;
        for (int lan = 0; lan < 6 && !dungLai; lan++) {
            BotChucNang.Bot x = new BotChucNang.Bot(ho.taiKhoan);
            try {
                String l = x.dangNhap(ho.matKhau);
                if (l == null) l = vaoNhanVat(x);
                if (l == null) { b = x; break; }
                cuoi = new IllegalStateException(l);
            } catch (IOException e) {
                cuoi = e;
            }
            x.close();
            Thread.sleep(5000 + rd.nextInt(5000));
        }
        if (b == null || b.dut) throw cuoi == null ? new IllegalStateException("khong vao duoc " + ho.taiKhoan) : cuoi;
        HoiQuy.napTheGioi(b);
        ngheXaHoi();
        ngheDon();
        KichBanHuongDan.roiSan(b);
        b.lamMoi();
        capTruoc = b.lv;
        ptTruoc = b.pt;
        lanVaoLai = System.currentTimeMillis();
        congDiem();
        napDangMac();
        if (b.hp <= 0 && b.maxhp > 0) hoiSinh();
        ho.charId = b.charDb;
        ho.cap = b.lv;
        ghi("vao game map " + b.mapId + " (" + b.tenMap + ") xu " + b.xu);
    }

    /**
     * Chua co nhan vat: tao bang ten da chon (goi 14 nhu man tao nhan vat - may chu kiem do dai / ky tu / tu cam / trung
     * ten); bi tu choi thi chon ten khac (toi da 12 lan). Roi chon nhan vat (goi 13).
     */
    String vaoNhanVat(BotChucNang.Bot x) throws IOException {
        if (x.dsNv.stream().noneMatch(v -> v[2] == 1)) {
            for (int lan = 0; lan < 12 && x.dsNv.stream().noneMatch(v -> v[2] == 1); lan++) {
                int m = x.moc();
                x.gui(14, ho.ten, (byte) ho.lop, (byte) 0, (byte) rd.nextInt(2), (byte) ho.vung, (byte) 0);
                x.cho(z -> z.coLenh(m, 13) && z.dsNv.stream().anyMatch(v -> v[2] == 1) || z.doiChu(m, 0, "") != null, 6000);
                if (x.dsNv.stream().anyMatch(v -> v[2] == 1)) { DichVuHlvAi.luuTen(ho); break; }
                String loi = x.chuTu(m);
                ghi("ten '" + ho.ten + "' bi tu choi: " + loi);
                ho.ten = HlvAiTen.moi(rd);
                x.bom(1500);
            }
            if (x.dsNv.stream().noneMatch(v -> v[2] == 1)) return "khong tao duoc nhan vat";
            ho.moi = true;
        }
        int[] nv = x.dsNv.stream().filter(v -> v[2] == 1).findFirst().get();
        x.charDb = nv[0];
        if (x.dsTen.size() > 0) {
            String t = x.dsTen.get(x.dsNv.indexOf(nv));
            if (t != null && !t.isEmpty() && !t.equals(ho.ten)) { ho.ten = t; DichVuHlvAi.luuTen(ho); }
        }
        int m = x.moc();
        x.gui(13, (byte) 0, x.charDb);
        x.cho(z -> z.coLenh(m, 12) && z.actor >= 0, 10000);
        if (!x.coLenh(m, 12)) return "chon nhan vat: khong nhan map " + x.chuTu(m);
        x.gui(12, (short) -500, (short) 0, (short) 0);
        x.bom(1500);
        return null;
    }

    // ================================================================== vong phien
    @Override
    public void run() {
        vaoLuc = System.currentTimeMillis();
        try {
            vao();
            if (ho.moi) {
                ho.moi = false;
                viec = "huong_dan";
                try { huongDanTanThu(); } catch (SocketException | EOFException e) { throw e; } catch (Exception e) { ghi("huong dan: " + e); }
            }
            while (!dungLai && System.currentTimeMillis() < henRa) {
                try {
                    if (b.dut || !b.conSong()) throw new SocketException("mat ket noi");
                    buoc();
                } catch (SocketException | EOFException e) {
                    if (dungLai) break; // dich vu dung / het phien: dong ket noi chu dong, khong phai rot mang
                    soVaoLaiPhien++;
                    DichVuHlvAi.dem("ket_noi_lai", 1);
                    ghi("mat ket noi (" + e + ") -> vao lai");
                    if (soVaoLaiPhien > 4 || dungLai) break; // khong lap dang nhap: het phien
                    b.close();
                    Thread.sleep(8000 + rd.nextInt(8000));
                    vao();
                }
            }
            if (b != null && !b.dut) {
                if (rd.nextInt(4) == 0) noi(HlvAiChat.TAM_BIET, false);
                b.bom(1500);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (Throwable e) {
            DichVuHlvAi.loi(ho.taiKhoan + ": " + e);
            ghi("NGOAI LE " + e);
        } finally {
            viec = "nghi";
            if (b != null) b.close();
        }
    }

    /** Huong dan tan thu: tinh cach thuong / xa hoi lam tung buoc, con lai bo qua (nhu nguoi choi bam Bỏ qua). */
    void huongDanTanThu() throws Exception {
        int m = b.moc();
        KichBanHuongDan.roiSan(b);
        b.doiMenu(m, KichBanHuongDan.NPC_SO_TAY, KichBanHuongDan.MENU_MOI, 3000);
        if (ho.tinhCach >= 3 && rd.nextBoolean()) {
            b.menu(KichBanHuongDan.NPC_SO_TAY, KichBanHuongDan.MENU_MOI, 0);
            b.bom(1000);
            KichBanHuongDan.Nv q = KichBanHuongDan.chinh(b, 0, 0);
            for (int k = 0; k < KichBanHuongDan.SO_BUOC && q != null && !dungLai; k++) {
                if (KichBanHuongDan.lamBuoc(b, q, k) != null) break;
                KichBanHuongDan.Nv sau = KichBanHuongDan.chinh(b, 0, 0);
                if (sau == null || sau.id != q.id + 1) break;
                q = sau;
            }
            DichVuHlvAi.dem("huong_dan_lam", 1);
        } else {
            KichBanHoiQuy.boQuaHuongDan(b);
            DichVuHlvAi.dem("huong_dan_bo_qua", 1);
        }
    }

    /** Mot viec (3..15 phut) theo trong so tinh cach, roi xu ly xa hoi. */
    void buoc() throws Exception {
        if (b.su.size() > 4000) b.su.subList(0, b.su.size() - 500).clear(); // gioi han bo nho phien dai
        lanVaoLai = System.currentTimeMillis(); // vong() cua lop cha tu thoat / vao lai moi 30 phut: dich vu tu quan phien
        int nay = java.util.Calendar.getInstance().get(java.util.Calendar.DAY_OF_YEAR);
        if (nay != ngayDanh) { ngayDanh = nay; phutDanhNgay = 0; } // nang luong danh quai: ngay moi (khong tua dong ho)
        if (ket.size() > 50) ket.clear();
        if (loi.size() > 50) loi.clear();
        String v = chonViec();
        viec = v;
        long het = System.currentTimeMillis() + (3 + rd.nextInt(10)) * 60000L;
        het = Math.min(het, henRa);
        int ok;
        try {
            ok = lam(v, het) ? 1 : 0;
        } catch (SocketException | EOFException e) {
            throw e;
        } catch (Exception e) {
            ok = 0;
            DichVuHlvAi.loi(v + ": " + e);
            ghi("loi viec " + v + ": " + e);
        }
        DichVuHlvAi.dem(v, ok);
        xuLyXaHoi();
        if (System.currentTimeMillis() - lanChat > 4 * 60000L && rd.nextInt(3) == 0 && DichVuHlvAi.bat("chat")) {
            noi(HlvAiChat.theoViec(v), rd.nextInt(6) == 0);
        }
        ho.cap = b.lv;
    }

    String chonViec() {
        int[] w = TRONG_SO[ho.tinhCach].clone();
        boolean duCap = b.lv >= ho.capMucTieu;
        int tong = 0;
        for (int i = 0; i < VIEC.length; i++) {
            if (!DichVuHlvAi.bat(VIEC[i]) || !moKhoa(VIEC[i])) w[i] = 0;
            else if (duCap && VIEC_EXP.contains(VIEC[i])) w[i] = Math.max(w[i] / 10, w[i] > 0 ? 1 : 0);
            tong += w[i];
        }
        if (tong <= 0) return DichVuHlvAi.bat("treo") ? "treo" : "chat";
        int r = rd.nextInt(tong);
        for (int i = 0; i < VIEC.length; i++) {
            if (r < w[i]) return VIEC[i];
            r -= w[i];
        }
        return "cay";
    }

    /** Viec mo theo cap (giong tinh nang mo cho nguoi choi). */
    boolean moKhoa(String v) {
        int lv = b.lv;
        switch (v) {
            case "pk": return lv >= 15;
            case "dau_truong": return lv >= 20;
            case "pho_ban": return lv >= 20;
            case "boss": return lv >= 20;
            case "gym": return lv >= 8;
            case "cho": case "giao_dich": return lv >= 20;
            case "vuon": return lv >= 7;
            case "nha_tre": return lv >= 12;
            case "bat": return lv >= 5;
            default: return true;
        }
    }

    boolean lam(String v, long het) throws Exception {
        switch (v) {
            case "nhiem_vu": return nhiemVu(het);
            case "cay": return cay(het);
            case "treo": return treo(het);
            case "gym": gym(); return true;
            case "boss": return boss(het);
            case "pk": return pk(het);
            case "dau_truong": return dauTruong();
            case "pho_ban": return phoBan(het);
            case "doi": return doi();
            case "chat": return noi(HlvAiChat.theoViec(v), rd.nextInt(4) == 0);
            case "ban_be": return banBe();
            case "giao_dich": return giaoDichChuDong();
            case "cho": return cho();
            case "sinh_thai": return sinhThai();
            case "vuon": return vuon();
            case "nha_tre": return nhaTre();
            case "bat": return bat();
            case "nang_cap": muaThuoc(); macDoTot(); nangVuKhi(); return true;
            case "su_kien": return khamPha();
            default: return false;
        }
    }

    // ================================================================== viec choi
    boolean nhiemVu(long het) throws Exception {
        int nv0 = chinhDaXong.size() + phuDaXong.size(), lv0 = b.lv;
        while (System.currentTimeMillis() < het && !dungLai && !b.dut) {
            lanVaoLai = System.currentTimeMillis();
            vong();
            xuLyXaHoi();
        }
        return chinhDaXong.size() + phuDaXong.size() > nv0 || b.lv > lv0;
    }

    /** Cay o bai luyen hop cap; hlv_ai.ban_do co thi uu tien ban do trong danh sach ma minh vao duoc. */
    boolean cay(long het) throws Exception {
        int ha0 = soHaQuai;
        muaThuoc();
        int dich = DichVuHlvAi.banDoUuTien(this);
        if (dich >= 0 && dich != b.mapId) toi(dich);
        while (System.currentTimeMillis() < het && !dungLai && !b.dut) {
            if (dich >= 0 && b.mapId == dich) danh(-1, 6, 2 * 60000L);
            else luyen(2);
            kiem();
        }
        return soHaQuai > ha0;
    }

    /**
     * Treo may: Tu dong danh cua game (-100 loai 1 bat, loai 4 xin ke hoach - may chu chon muc tieu / chieu, client thuc
     * hien), nhu client 0.9.2+. Het gio / may chu dung (het gio treo ngay, tui day...) thi tat (-100 loai 2).
     */
    boolean treo(long het) throws Exception {
        muaThuoc();
        int ml = mapLuyen();
        if (ml >= 0 && ml != b.mapId) toi(ml);
        if (KichBanDS.laLang(b.mapId)) return false;
        int m0 = b.moc();
        b.gui(-100, (byte) 1, (short) 240, (byte) 0, (byte) 2, (byte) 50, (byte) 30, (byte) 1, (byte) 1, (byte) 0);
        b.bom(800);
        int ke = 0, danh = 0;
        int tu = b.moc();
        while (System.currentTimeMillis() < het && !dungLai && !b.dut) {
            b.gui(-100, (byte) 4);
            b.bom(600 + rd.nextInt(200));
            int[] k = null;
            boolean dung = false;
            for (int i = tu; i < b.su.size(); i++) {
                BotChucNang.SuKien e = b.su.get(i);
                if (e.cmd != -100 || e.d == null || e.d.length < 2) continue;
                if (e.d[0] == 0 && e.d[1] == 0) dung = true; // trang thai: da tat (het gio / bi dung)
                if (e.d[0] != 1) continue;
                try {
                    DataInputStream r = new DataInputStream(new ByteArrayInputStream(e.d));
                    r.readByte();
                    k = new int[]{r.readByte(), r.readShort(), r.readShort(), r.readShort(), r.readShort()};
                } catch (IOException ignored) { }
                ke++;
            }
            tu = b.su.size();
            if (b.su.size() > 3000) { b.su.subList(0, 2500).clear(); tu = b.su.size(); }
            if (dung && ke > 3) break;
            if (b.hp <= 0) { hoiSinh(); break; }
            if (k == null) continue;
            if (k[0] == 1) {
                if (Math.abs(b.px - k[3]) > 40 || Math.abs(b.py - k[4]) > 40) {
                    b.diToi(b.px + Integer.signum(k[3] - b.px) * Math.min(40, Math.abs(k[3] - b.px)),
                            b.py + Integer.signum(k[4] - b.py) * Math.min(40, Math.abs(k[4] - b.py)));
                }
                if (k[2] >= 0) { b.gui(9, (short) k[1], (byte) k[2]); danh++; }
            } else if (k[0] == 4 && (Math.abs(b.px - k[3]) > 16 || Math.abs(b.py - k[4]) > 16)) {
                b.diToi(k[3], k[4]);
            }
            if (ke % 50 == 0) xuLyXaHoi();
        }
        b.gui(-100, (byte) 2);
        b.bom(400);
        ghi("treo: " + ke + " ke hoach, " + danh + " don (" + b.chuTu(m0).length() + ")");
        return danh > 0;
    }

    /** Boss lang thang / the gioi: may chu bao "<boss> đã xuất hiện ở <ban do>" -> toi danh trong 3 phut (du cap). */
    boolean boss(long het) throws Exception {
        String bd = bossMap;
        if (bd == null || System.currentTimeMillis() - bossLuc > 20 * 60000L) return cay(het);
        int dich = KichBanCap140.mapTheoTen(b.mapId, bd);
        if (dich < 0 || !toi(dich)) return cay(het);
        long hetBoss = Math.min(het, System.currentTimeMillis() + 3 * 60000L);
        int don = 0;
        while (System.currentTimeMillis() < hetBoss && !b.dut && b.hp > 0) {
            int muc = -1, hpMax = -1;
            for (Map.Entry<Integer, int[]> e : b.quai.entrySet()) if (e.getValue()[4] > hpMax) { hpMax = e.getValue()[4]; muc = e.getKey(); }
            if (muc < 0) { b.bom(1000); continue; }
            int[] q = b.quai.get(muc);
            if (q != null && (Math.abs(q[0] - b.px) > 60 || Math.abs(q[1] - b.py) > 60)) b.diToi(q[0], q[1]);
            b.gui(9, (short) muc, (byte) (don % Math.max(1, Math.min(4, b.chieu.length))));
            don++;
            b.bom(700);
            uongThuoc();
        }
        if (b.hp <= 0) hoiSinh();
        return don > 0;
    }

    /** PvP: deo khan PK (neu co) va danh bot PvP cung bac cap o cung ban do; khong co doi thu thi Dau truong. */
    boolean pk(long het) throws Exception {
        HlvAiNguoiChoi doi = DichVuHlvAi.doiThuPk(this);
        if (doi == null) return dauTruong();
        int khan = -1;
        for (int t = 14; t <= 18; t++) if (b.soThuoc(t) > 0) { khan = t; break; }
        if (khan < 0) return dauTruong();
        if (!toi(doi.b.mapId)) return false;
        b.gui(22, (byte) khan);
        b.bom(800);
        BotChucNang.BanDo bd = BotChucNang.BanDo.nap(b.mapLoad);
        int don = 0;
        for (int lan = 0; lan < 60 && !b.dut && System.currentTimeMillis() < het && doi.b != null && !doi.b.dut && b.hp > 0; lan++) {
            int[] vt = b.nguoi.get(doi.b.actor);
            if (vt != null && bd != null && lan % 8 == 0) { int[] o = bd.oGan(vt[0] / 16, vt[1] / 16, 2); if (o != null) b.diDuong(bd, o[0], o[1]); }
            b.gui(6, (short) doi.b.actor, (byte) 0);
            don++;
            b.bom(500);
            uongThuoc();
        }
        b.gui(22, (byte) khan); // thao khan
        b.bom(500);
        if (b.hp <= 0) hoiSinh();
        return don > 0;
    }

    /**
     * Dau truong Bao Boi 3v3 (goi -109): NPC Dau truong o lang -> luu doi (3 con Hop PC dau) -> tim doi thu -> thach dau ->
     * moi luot chon chieu nhu nguoi bam (may chu gioi han nhip). Can >= 3 Pokemon trong Hop PC (bat duoc).
     */
    boolean dauTruong() throws Exception {
        if (!toi(0)) return false;
        BotChucNang.BanDo bdl = BotChucNang.BanDo.nap(b.mapLoad);
        int[] oNpc = bdl == null ? null : bdl.oGan(22, 33, 3);
        if (oNpc != null) b.diDuong(bdl, oNpc[0], oNpc[1]);
        int i = KichBanDauTruong.chonDauTruong(b, "Đấu trường Bảo Bối");
        if (i < 0) { ghi("dau truong: khong mo duoc NPC"); return false; }
        b.bom(800);
        for (int loai = 0; loai < 2; loai++) {
            b.gui(-109, (byte) 1, (byte) loai, (byte) 3, 1, 2, 3);
            b.bom(700);
        }
        int m = b.moc();
        b.gui(-109, (byte) 2, (byte) 0);
        byte[] dt = KichBanDauTruong.cho(b, m, 1, 5000);
        List<Integer> ds = dt == null ? new ArrayList<>() : KichBanDauTruong.doiThu(dt);
        if (ds.isEmpty()) { ghi("dau truong: khong co doi thu " + b.chuTu(m)); return false; }
        int doi = ds.get(rd.nextInt(ds.size()));
        m = b.moc();
        b.gui(-109, (byte) 3, doi, (byte) 0);
        byte[] bd = KichBanDauTruong.cho(b, m, 2, 5000);
        if (bd == null) { ghi("dau truong: khong vao tran " + b.chuTu(m)); return false; }
        int tranId;
        try { DataInputStream r = new DataInputStream(new ByteArrayInputStream(bd)); r.readByte(); tranId = r.readInt(); } catch (IOException e) { return false; }
        for (int luot = 0; luot < 30 && !b.dut; luot++) {
            b.bom(1500 + rd.nextInt(1500));
            int mm = b.moc();
            b.gui(-109, (byte) 4, tranId, (byte) 0, (byte) rd.nextInt(2));
            KichBanDauTruong.cho(b, mm, 3, 6000);
            if (KichBanDauTruong.goi(b, m, 4) != null) break;
        }
        ghi("dau truong: tran " + tranId + " xong=" + (KichBanDauTruong.goi(b, m, 4) != null));
        return true;
    }

    /** Pho ban Ham nguc (So tay -> Ham nguc -> Pho ban): danh quai trong phong toi het gio roi roi phong. */
    boolean phoBan(long het) throws Exception {
        if (b.hp < b.maxhp / 2) hoiMau("truoc pho ban");
        KichBanPhoBanCup.moVao(b);
        if (b.mapId != KichBanPhoBanCup.MAP_PB) { ghi("pho ban: khong vao duoc " + b.chuTu(Math.max(0, b.moc() - 20))); return false; }
        int ha0 = soHaQuai;
        long h = Math.min(het, System.currentTimeMillis() + 6 * 60000L);
        while (System.currentTimeMillis() < h && !b.dut && b.mapId == KichBanPhoBanCup.MAP_PB && b.hp > 0) {
            danh(-1, 4, 60000L);
        }
        if (b.mapId == KichBanPhoBanCup.MAP_PB) {
            b.menu(KichBanPhoBanCup.SO_TAY, KichBanPhoBanCup.MENU_PB_TRONG, 0);
            b.cho(x -> x.mapId != KichBanPhoBanCup.MAP_PB, 4000);
        }
        if (b.hp <= 0) hoiSinh();
        return soHaQuai > ha0;
    }

    /** Doi (bang): da vao doi thi chat doi; loi moi vao doi xu ly o xuLyXaHoi (dong y). */
    boolean doi() throws Exception {
        if (coDoi) {
            String c = HlvAiChat.chon(HlvAiChat.XA_HOI, rd, b.lv, b.tenMap, 0);
            if (c != null) { b.gui(-18, c); b.bom(500); lanChat = System.currentTimeMillis(); }
            return true;
        }
        return noi(HlvAiChat.XA_HOI, false);
    }

    /** Ket ban voi mot nguoi dang dung gan (goi 101 loi moi). */
    boolean banBe() throws IOException {
        if (b.nguoi.isEmpty()) return false;
        List<Integer> ds = new ArrayList<>(b.nguoi.keySet());
        int id = ds.get(rd.nextInt(ds.size()));
        b.gui(101, (short) id, (byte) 0);
        b.bom(800);
        return true;
    }

    /**
     * Giao dich chu dong (goi 66): moi mot nguoi gan do (bot hoac nguoi that); ho dong y thi dat 1-3 binh thuoc, cho ho dat
     * mon cua ho roi khoa + xac nhan; ho khong dat gi trong 20 giay thi huy (khong cho khong). Toi da 1 loi moi / 10 phut.
     */
    long lanMoiGd;

    boolean giaoDichChuDong() throws Exception {
        if (System.currentTimeMillis() - lanMoiGd < 10 * 60000L || b.nguoi.isEmpty()) return false;
        lanMoiGd = System.currentTimeMillis();
        List<Integer> ds = new ArrayList<>(b.nguoi.keySet());
        int id = ds.get(rd.nextInt(ds.size()));
        int m = b.moc();
        b.gui(66, (byte) 0, (short) id);
        boolean mo = b.cho(x -> KichBanBatThuong.giaoDich(x, m, 1), 15000);
        if (!mo) return false;
        return giaoDichPhien(m);
    }

    /** Phien giao dich da mo (66/1): dat thuoc doi lai thuoc cua doi phuong, cong bang (cung so luong). */
    boolean giaoDichPhien(int m) throws IOException {
        int loai = b.soThuoc(2) > 3 ? 2 : b.soThuoc(1) > 3 ? 1 : -1;
        long het = System.currentTimeMillis() + 20000;
        boolean doiDat = false;
        while (System.currentTimeMillis() < het && !b.dut) {
            b.bom(500);
            for (int i = m; i < b.su.size(); i++) {
                BotChucNang.SuKien e = b.su.get(i);
                if (e.cmd == 66 && e.so != null && (e.so[0] == 2 || e.so[0] == 4)) doiDat = true;
                if (e.cmd == 66 && e.so != null && e.so[0] == 3) return false; // doi phuong huy
            }
            if (doiDat) break;
        }
        if (!doiDat || loai < 0) {
            b.gui(66, (byte) 3);
            b.bom(500);
            return false;
        }
        b.gui(66, (byte) 2, (byte) 1, (byte) loai, (short) (1 + rd.nextInt(3)));
        b.bom(800);
        b.gui(66, (byte) 4);
        b.bom(1500);
        b.gui(66, (byte) 5);
        b.bom(2000);
        b.lamMoi();
        ghi("giao dich xong (thuoc " + loai + ")");
        return true;
    }

    /**
     * Cho ky gui (goi 92 trong nha cua hang): rao mot mon khong khoa, khong dang mac. Gia = gia tham chieu x 1..1,3; may
     * chu tu choi vi duoi gia thi truong ("... (N xu)") thi rao lai dung N. Mua: mon bot khac dang rao (danh sach dich vu)
     * neu gia khong qua 1,3 lan gia NPC.
     */
    boolean cho() throws Exception {
        if (rd.nextBoolean() && DichVuHlvAi.muaChoBot(this)) return true;
        int[] mon = b.timDo(d -> d[4] == 0 && !b.dangMac.contains(d[0]));
        if (mon == null) return sinhThai();
        int lang = b.mapId;
        if (!KichBanDS.laLang(lang) && !toi(0)) return false;
        if (!KichBanDS2.vaoNha(b, 103)) return false;
        long gia = Math.max(100L, (long) (KichBanCap140.NguoiChoi.gia(mon[1]) * (1.0 + rd.nextInt(30) / 100.0)));
        for (int lan = 0; lan < 2; lan++) {
            for (int npc = 11; npc <= 20; npc++) {
                int m = b.moc();
                b.gui(92, true, (byte) npc, (byte) 0, (short) mon[0], (int) Math.min(Integer.MAX_VALUE, gia), (byte) 0);
                b.cho(x -> x.layLenh(m, -31) != null || x.coLenh(m, 92) || x.doiChu(m, 0, "") != null, 3000);
                if (b.layLenh(m, -31) != null) { b.gui(-31, (short) -32047, (byte) 0, ""); b.bom(1000); }
                if (b.coLenh(m, 92)) {
                    DichVuHlvAi.raoBan(this, npc, 0, mon[0], mon[1], gia);
                    ghi("rao ban tpl " + mon[1] + " gia " + gia + " o npc " + npc);
                    KichBanDS2.raNgoai(b);
                    return true;
                }
                String t = b.chuTu(m);
                Matcher mm = Pattern.compile("giá thị trường \\((\\d+) xu\\)").matcher(t);
                if (mm.find()) { gia = Long.parseLong(mm.group(1)); break; }
                if (b.doiChu(m, 0, "chỉ bán") == null) break;
            }
        }
        KichBanDS2.raNgoai(b);
        return false;
    }

    /** He sinh thai (menu "hst"): cau ca / khao co / nau an / xem cho sinh thai. */
    boolean sinhThai() throws Exception {
        int m0 = b.moc();
        b.gui(23, (byte) -90);
        b.bom(800);
        int vt = b.timMuc("sinh thái");
        if (vt < 0) return false;
        int chon = rd.nextInt(4);
        if (chon == 0) {
            int m = b.moc();
            b.chat("cauca");
            String t = b.doiChu(m, 2500, "");
            if (t == null) return false;
            Matcher mm = Pattern.compile("\\((\\d+),(\\d+)\\)").matcher(t);
            if (t.contains("Điểm câu") && mm.find()) {
                BotChucNang.BanDo bd = BotChucNang.BanDo.nap(b.mapLoad);
                int[] o = bd == null ? null : bd.oGan(Integer.parseInt(mm.group(1)), Integer.parseInt(mm.group(2)), 3);
                if (o != null && b.diDuong(bd, o[0], o[1])) { int m2 = b.moc(); b.chat("cauca"); b.doiChu(m2, 2500, ""); }
            }
            return true;
        }
        b.menu(-90, 0, vt);
        b.bom(700);
        int m = b.moc();
        if (chon == 1) b.menu(-528, 0, 1);
        else if (chon == 2) { b.menu(-528, 0, 2); if (b.doiMenu(m, -528, 1, 2000) > 0) { int m2 = b.moc(); b.menu(-528, 1, 0); b.doiChu(m2, 2000, ""); } }
        else b.menu(-528, 0, 5);
        b.bom(1200);
        return b.moc() > m0;
    }

    /** Vuon berry: o trong thi gieo, cay chin thi thu (menu "hst" -> Vuon nha va ban be -> O dat cua toi). */
    boolean vuon() throws Exception {
        List<String> o = KichBanVuon.moODat(b);
        if (o == null) return false;
        for (int i = 0; i < o.size(); i++) {
            String s = o.get(i);
            KichBanVuon.cho(b);
            KichBanVuon.moODat(b);
            int m = b.moc();
            b.menu(KichBanVuon.NPC, KichBanVuon.M_O, i);
            if (s.contains("trống")) {
                if (b.doiMenu(m, KichBanVuon.NPC, KichBanVuon.M_GIEO, 4000) >= 0) {
                    KichBanVuon.cho(b);
                    b.menu(KichBanVuon.NPC, KichBanVuon.M_GIEO, 0);
                    b.bom(1200);
                }
            } else {
                b.bom(1500); // chin thi may chu thu hoach, dang lon thi bao con bao lau
            }
        }
        return true;
    }

    /** Nha tre (menu "hst" -> Nhà trẻ): xem tinh trang, nhan pet / tra phi / nhan trung theo muc may chu dua. */
    boolean nhaTre() throws Exception {
        int n = KichBanTinhNangThuong.moHst(b, "Nhà trẻ", 6);
        if (n <= 0) return false;
        int op = rd.nextInt(Math.min(n, 3));
        KichBanTinhNangThuong.chonHst(b, 6, op, -1, "");
        b.bom(1000);
        return true;
    }

    /** Bat Pokemon hoang da: mua Poke Ball o Chansey (xu) neu chua co, cay gap hoang da (-125) thi ha yeu roi nem. */
    boolean bat() throws Exception {
        if (!coBong && b.xu > 3000) coBong = KichBanCap140TinhNang.muaBong(this);
        return KichBanCap140TinhNang.batPokemon(this);
    }

    /** Kham pha: danh hieu, doi khu, Ve dich chuyen, Pokedex, xem menu Quay doi thuong / So tay. */
    boolean khamPha() throws Exception {
        switch (rd.nextInt(5)) {
            case 4: // xem menu nhu nguoi choi to mo: Quay doi thuong / So tay (xep hang, su kien, Pokedex, cua hang...) - chi muc "xem"
                KichBanCap140TinhNang.quetMenu(this, rd.nextBoolean());
                DichVuHlvAi.dem("xem_menu", 1);
                return true;
            case 0: KichBanCap140TinhNang.danhHieu(this); return true;
            case 1: KichBanCap140TinhNang.doiKhu(this); return true;
            case 2: if (b.xu > 4000) { KichBanCap140TinhNang.veDichChuyen(this); return true; } return false;
            default:
                if (KichBanDS4.chonQuay(b, "Pokédex")) { b.bom(800); b.menu(-526, 0, rd.nextInt(2)); b.bom(800); return true; }
                return false;
        }
    }

    // ================================================================== xa hoi
    /** Nghe goi den: loi moi to doi / giao dich / vao doi / ket ban, chat, boss xuat hien -> hop (xu ly ngoai luong doc). */
    void ngheXaHoi() {
        final BotChucNang.Bot bb = b;
        final java.util.function.Consumer<BotChucNang.SuKien> cu = bb.ngheNhan;
        bb.ngheNhan = e -> {
            if (cu != null) cu.accept(e);
            try {
                if (hop.size() > 30) return;
                byte[] d = e.d;
                if (e.cmd == 49 && d != null && d.length >= 5 && d[0] == 0) hop.add(new Object[]{"to_doi", (int) (short) (((d[3] & 255) << 8) | (d[4] & 255)), ""});
                else if (e.cmd == 66 && d != null && d.length >= 3 && d[0] == 0) hop.add(new Object[]{"giao_dich", (int) (short) (((d[1] & 255) << 8) | (d[2] & 255)), ""});
                else if (e.cmd == -11 && d != null && d.length >= 3 && d[0] == 0) hop.add(new Object[]{"vao_doi", (int) (short) (((d[1] & 255) << 8) | (d[2] & 255)), ""});
                else if (e.cmd == 101 && d != null && d.length >= 4 && d[0] == 0) {
                    DataInputStream r = new DataInputStream(new ByteArrayInputStream(d));
                    r.readByte();
                    String ten = r.readUTF();
                    hop.add(new Object[]{"ket_ban", (int) r.readShort(), ten});
                } else if (e.chu != null) {
                    String c = e.chu;
                    if (c.startsWith("CHAT(") && !c.startsWith("CHAT(minh)")) hop.add(new Object[]{"chat_ban_do", 0, c.substring(c.indexOf("): ") + 3)});
                    else if (c.startsWith("TIN RIENG tu ")) {
                        int a = c.indexOf(" (", 13), k = c.indexOf("): ");
                        String ten = a > 13 ? c.substring(13, a) : c.substring(13, Math.max(13, c.indexOf(':')));
                        if (k > 0 && !ten.equalsIgnoreCase(b.ten)) hop.add(new Object[]{"tin_rieng", 0, ten + "\t" + c.substring(k + 3)});
                    } else if (c.startsWith("KENH1 ")) {
                        int k = c.indexOf(": ", 6);
                        String ten = k > 6 ? c.substring(6, k) : "";
                        if (!ten.endsWith(b.ten)) hop.add(new Object[]{"chat_the_gioi", 0, ten + "\t" + (k > 0 ? c.substring(k + 2) : c)});
                    }
                    Matcher co = Pattern.compile("đã xuất hiện ở ([^!|]+)!").matcher(c);
                    if (co.find()) { bossMap = co.group(1).trim(); bossLuc = System.currentTimeMillis(); }
                }
            } catch (Exception ignored) {
            }
        };
    }

    /** Xu ly hop xa hoi: dong y loi moi theo tinh cach, tra loi chat (tin rieng gan nhu luon tra loi). */
    void xuLyXaHoi() throws IOException {
        Object[] s;
        int n = 0;
        while ((s = hop.poll()) != null && n++ < 8 && b != null && !b.dut) {
            String loai = (String) s[0];
            int id = (Integer) s[1];
            String chu = (String) s[2];
            boolean xaHoi = ho.tinhCach == 3;
            switch (loai) {
                case "to_doi":
                    if (rd.nextInt(xaHoi ? 1 : 2) == 0) { b.gui(49, (byte) 1, (short) id); b.bom(400); DichVuHlvAi.dem("to_doi", 1); }
                    break;
                case "vao_doi":
                    if (DichVuHlvAi.bat("doi") && (xaHoi || rd.nextInt(3) == 0)) {
                        b.gui(-11, (byte) 1, (short) id, true);
                        b.bom(800);
                        coDoi = true;
                        DichVuHlvAi.dem("vao_doi", 1);
                    }
                    break;
                case "ket_ban":
                    if (DichVuHlvAi.bat("ban_be") && rd.nextInt(3) > 0) { b.gui(101, (short) id, (byte) 1); b.bom(400); DichVuHlvAi.dem("nhan_ket_ban", 1); }
                    break;
                case "giao_dich":
                    if (DichVuHlvAi.bat("giao_dich") && b.lv >= 20 && rd.nextInt(3) > 0) {
                        int m = b.moc();
                        b.gui(66, (byte) 1, (short) id);
                        if (b.cho(x -> KichBanBatThuong.giaoDich(x, m, 1), 5000)) DichVuHlvAi.dem("nhan_giao_dich", giaoDichPhien(m) ? 1 : 0);
                    }
                    break;
                case "tin_rieng": {
                    if (!DichVuHlvAi.bat("chat")) break;
                    String[] f = chu.split("\t", 2);
                    String tl = HlvAiChat.traLoi(f.length > 1 ? f[1] : chu, true, rd, b.lv);
                    if (tl != null && f[0].length() > 0) {
                        b.bom(1500 + rd.nextInt(3000)); // go phim
                        b.gui(-124, (byte) 1, (byte) 4, f[0], tl);
                        b.bom(300);
                        DichVuHlvAi.dem("tra_loi_rieng", 1);
                    }
                    break;
                }
                case "chat_ban_do":
                case "chat_the_gioi": {
                    if (!DichVuHlvAi.bat("chat") || System.currentTimeMillis() - lanXaHoi < 90000L) break;
                    String noiDung = chu.contains("\t") ? chu.split("\t", 2)[1] : chu;
                    boolean goiTen = BotChucNang.khongDau(noiDung).toLowerCase().contains(BotChucNang.khongDau(b.ten).toLowerCase());
                    if (!goiTen && rd.nextInt(xaHoi ? 4 : 10) != 0) break;
                    String tl = HlvAiChat.traLoi(noiDung, false, rd, b.lv);
                    if (tl != null) {
                        b.bom(1500 + rd.nextInt(3000));
                        b.chat(tl);
                        lanXaHoi = lanChat = System.currentTimeMillis();
                        DichVuHlvAi.dem("tra_loi_chat", 1);
                    }
                    break;
                }
                default:
                    break;
            }
        }
    }

    /** Noi mot cau (ban do, hoac the gioi - ton xu nhu moi nguoi). */
    boolean noi(String[] bang, boolean theGioi) throws IOException {
        if (b == null || b.dut) return false;
        if (!daChao) {
            daChao = true;
            if (rd.nextInt(3) == 0) bang = HlvAiChat.CHAO;
        }
        String c = HlvAiChat.chon(bang, rd, b.lv, b.tenMap, 1 + b.lv / 3);
        if (c == null) return false;
        if (theGioi && b.xu > 20000) b.gui(-124, (byte) 1, (byte) 1, "", c);
        else b.chat(c);
        b.bom(400);
        lanChat = System.currentTimeMillis();
        DichVuHlvAi.dem(theGioi ? "chat_the_gioi" : "chat_ban_do", 1);
        return true;
    }

    // ================================================================== tien ich
    @Override
    int danh(int t, int n, long ms) throws Exception {
        xuLyXaHoi();
        if (dungLai) return 0;
        return super.danh(t, n, ms);
    }

    @Override
    void luyen(int phut) throws Exception {
        xuLyXaHoi();
        if (dungLai) return;
        super.luyen(phut);
    }

    String nhatKy() {
        synchronized (nhatKyGan) {
            return String.join("\n", nhatKyGan);
        }
    }

    Map<String, Object> trangThai() {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("viec", viec);
        m.put("cap", b == null ? 0 : b.lv);
        m.put("map", b == null ? -1 : b.mapId);
        return m;
    }
}
