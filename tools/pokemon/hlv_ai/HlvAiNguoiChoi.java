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
    /** Kiem khai thac: >= 0 = dung yen o ban do nay (van tra loi giao dich / thach dau / trao doi / vao doi); khan = deo khan PK. */
    volatile int dungYen = -1;
    volatile boolean dungYenKhan;
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
        try { hopPc(); } catch (IOException ignored) { }
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

    /** Vao lai giua buoc (BotXuyenSuot.kiem / qua cong): dem + ghi ly do nhu vong phien. */
    @Override
    void vaoLai() throws Exception {
        DichVuHlvAi.dem("ket_noi_lai", 1);
        DichVuHlvAi.dem("ket_noi_lai:" + viec, 1);
        String ly = b == null ? null : b.lyDoDut;
        DichVuHlvAi.loi("vao lai (" + viec + "): " + (ly == null ? "khong ro" : ly));
        b.close();
        Thread.sleep(3000);
        vao();
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
                    String ly = b == null ? null : b.lyDoDut;
                    DichVuHlvAi.dem("ket_noi_lai:" + viec, 1);
                    ghi("mat ket noi (" + e + "; " + (ly == null ? "gui that bai" : ly) + "; viec " + viec + ") -> vao lai | "
                            + (b == null ? "" : b.chuTu(Math.max(0, b.su.size() - 6))));
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
        if (b.lv >= 25 && System.currentTimeMillis() - lanHocChieu > 30 * 60000L && DichVuHlvAi.bat("nang_cap")) {
            lanHocChieu = System.currentTimeMillis();
            try { hocChieu(); } catch (SocketException | EOFException e) { throw e; } catch (Exception e) { DichVuHlvAi.loi("hoc chieu: " + e); }
        }
        if (dungYen >= 0) { // kiem khai thac: dung yen cho nguoi that
            viec = "dung_yen";
            if (b.hp <= 0) hoiSinh();
            if (b.mapId != dungYen && b.mapId != 205) toi(dungYen);
            if (dungYenKhan && khanMinh < 0 && !KichBanDS.laLang(b.mapId)) {
                int k = -1;
                for (int t = 14; t <= 18; t++) if (b.soThuoc(t) > 0) k = t;
                if (k < 0) k = muaKhan();
                else { b.gui(22, (byte) k); b.bom(800); khanMinh = k; }
            } else if (!dungYenKhan && khanMinh >= 14) {
                b.gui(22, (byte) khanMinh);
                khanMinh = -1;
            }
            if (b.mapId == 205) sanDau(-1);
            xuLyXaHoi();
            int gq = yeuCauGopQuy;
            if (gq > 0) { yeuCauGopQuy = 0; gopQuy(gq); }
            b.bom(3000);
            b.lamMoi();
            return;
        }
        if (b.mapId == 205) { viec = "thach_dau"; sanDau(-1); return; } // bi keo vao san thach dau (dong y loi moi)
        if (b.mapId == KichBanPhoBanCup.MAP_PB) { viec = "pho_ban"; trongPhoBan(System.currentTimeMillis() + 8 * 60000L); return; } // vao cung nhom
        String v = chonViec();
        viec = v;
        long bd = System.currentTimeMillis();
        long het = bd + (3 + rd.nextInt(10)) * 60000L;
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
        if (ok == 0) {
            // nguoi choi gap viec khong lam duoc (chua du doi Dau truong, chua co khan PK...) thi thoi viec do mot luc
            nghiViec.put(v, System.currentTimeMillis() + (20 + rd.nextInt(21)) * 60000L);
            if (System.currentTimeMillis() - bd < 60000L && !dungLai && System.currentTimeMillis() < henRa) {
                viec = DichVuHlvAi.bat("cay") ? "cay" : "treo"; // lam viec quen thuoc cho het luot thay vi doi viec lien tuc
                lam(viec, Math.min(het, System.currentTimeMillis() + 3 * 60000L));
            }
        }
        xuLyXaHoi();
        if (System.currentTimeMillis() - lanChat > 4 * 60000L && rd.nextInt(3) == 0 && DichVuHlvAi.bat("chat")) {
            noi(HlvAiChat.theoViec(v), rd.nextInt(6) == 0);
        }
        ho.cap = b.lv;
    }

    /** Viec vua hong -> luc duoc thu lai. */
    final Map<String, Long> nghiViec = new java.util.HashMap<>();

    String chonViec() {
        int[] w = TRONG_SO[ho.tinhCach].clone();
        long bay = System.currentTimeMillis();
        boolean duCap = b.lv >= ho.capMucTieu;
        int tong = 0;
        for (int i = 0; i < VIEC.length; i++) {
            if (!DichVuHlvAi.bat(VIEC[i]) || !moKhoa(VIEC[i]) || nghiViec.getOrDefault(VIEC[i], 0L) > bay) w[i] = 0;
            else if (duCap && VIEC_EXP.contains(VIEC[i])) w[i] = Math.max(w[i] / 10, w[i] > 0 ? 1 : 0);
            tong += w[i];
        }
        // muc tieu nhu nguoi choi: chua du 3 loai Pokemon -> hay di bat (mo Dau truong); cap 50 chua co doi -> lap doi; cap 45+ -> pho ban
        int loai = (int) hopCache.stream().mapToInt(c -> c[1]).distinct().count();
        for (int i = 0; i < VIEC.length; i++) {
            if (w[i] == 0) continue;
            if (VIEC[i].equals("bat") && loai < 3 && b.lv >= 10) { w[i] += 12; tong += 12; }
            if (VIEC[i].equals("doi") && b.lv >= 50 && !coDoi) { w[i] += 8; tong += 8; }
            if (VIEC[i].equals("pho_ban") && b.lv >= 45) { w[i] += 6; tong += 6; }
            if (VIEC[i].equals("dau_truong") && loai >= 3) { w[i] += 6; tong += 6; }
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
            case "pho_ban": return lv >= 45; // pho ban 5 tang danh theo nhom: bot le cap 35 guc o tang 1 (kiem ngam 31/10)
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
            case "giao_dich": return rd.nextBoolean() ? traoDoiPokemon() || giaoDichChuDong() : giaoDichChuDong();
            case "cho": return cho();
            case "sinh_thai": return sinhThai();
            case "vuon": return vuon();
            case "nha_tre": return nhaTre();
            case "bat": return bat(het);
            case "nang_cap": muaThuoc(); macDoTot(); nangVuKhi(); if (b.lv >= 25) hocChieu(); return true;
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
     * Giao dich (goi 66) - dieu khien NGAY trong luong doc goi (nhu nguoi bam nut, khong doi het viec dang lam):
     * <pre>
     *  may chu -> client: 0 loi moi (id), 1 da mo, 2 mot ben dat (2, loai, id nguoi dat, thuoc, so), 5 ca hai da khoa,
     *  4 xong, -1 huy. client -> may chu: 1 dong y, 2 dat, 3 huy, 4 khoa, 5 xac nhan.
     *  Luat cong bang: chi khoa khi doi phuong DA dat gi; dat lai DUNG loai + so doi phuong dat (toi da 3, minh phai co);
     *  nguoi moi (bot) dat truoc 1 binh; doi phuong khong dat trong 25 giay -> huy. Khong bao gio cho khong.
     * </pre>
     */
    volatile int gdTrangThai, gdDoi;
    volatile boolean gdChuDong, gdMinhDat, gdDoiDat, gdKhoa;
    volatile long gdLuc, lanMoiGd;
    volatile int gdXong;

    void gdDatLai() {
        gdTrangThai = 0;
        gdDoi = 0;
        gdChuDong = gdMinhDat = gdDoiDat = gdKhoa = false;
    }

    /** Goi 66 den (luong doc goi cua chinh bot). */
    void gdGoi(byte[] d) throws IOException {
        int loai = d[0];
        if (loai == 0 && d.length >= 3) {
            int id = (short) (((d[1] & 255) << 8) | (d[2] & 255));
            boolean nhan = gdTrangThai == 0 && DichVuHlvAi.bat("giao_dich") && b.lv >= 20 && !dungLai && rd.nextInt(4) > 0;
            if (!nhan) return;
            gdDatLai();
            gdTrangThai = 1;
            gdDoi = id;
            gdLuc = System.currentTimeMillis();
            b.gui(66, (byte) 1, (short) id);
            DichVuHlvAi.dem("nhan_giao_dich", 1);
        } else if (loai == 1) {
            gdTrangThai = Math.max(gdTrangThai, 1);
            gdLuc = System.currentTimeMillis();
            if (gdChuDong && !gdMinhDat) {
                int t = b.soThuoc(1) > 0 ? 1 : b.soThuoc(2) > 0 ? 2 : -1;
                if (t < 0) { b.gui(66, (byte) 3); gdDatLai(); return; }
                b.gui(66, (byte) 2, (byte) 1, (byte) t, (short) 1);
                gdMinhDat = true;
            }
        } else if (loai == 2 && d.length >= 7 && gdTrangThai >= 1) {
            int nguoiDat = (short) (((d[2] & 255) << 8) | (d[3] & 255));
            if (nguoiDat == b.actor) return;
            int thuoc = d[4] & 255, so = ((d[5] & 255) << 8) | (d[6] & 255);
            gdDoiDat = true;
            if (!gdMinhDat) {
                int sl = Math.min(3, Math.min(so, b.soThuoc(thuoc)));
                if (sl <= 0) { b.gui(66, (byte) 3); gdDatLai(); return; }
                b.gui(66, (byte) 2, (byte) 1, (byte) thuoc, (short) sl);
                gdMinhDat = true;
            }
            if (!gdKhoa) { b.gui(66, (byte) 4); gdKhoa = true; }
        } else if (loai == 5 && gdTrangThai >= 1) {
            if (gdDoiDat && gdMinhDat) b.gui(66, (byte) 5);
            else { b.gui(66, (byte) 3); gdDatLai(); }
        } else if (loai == 4) {
            if (gdTrangThai >= 1) { DichVuHlvAi.dem("giao_dich_xong", 1); gdXong++; }
            gdDatLai();
        } else if (loai == -1) {
            gdDatLai();
        }
    }

    /** Moi mot nguoi dang dung gan (bot hoac nguoi that) - toi da 1 loi moi / 10 phut; phien do gdGoi lo. */
    boolean giaoDichChuDong() throws Exception {
        if (System.currentTimeMillis() - lanMoiGd < 10 * 60000L || b.nguoi.isEmpty() || gdTrangThai != 0) return false;
        lanMoiGd = System.currentTimeMillis();
        List<Integer> ds = new ArrayList<>(b.nguoi.keySet());
        int id = ds.get(rd.nextInt(ds.size()));
        int x0 = gdXong;
        gdDatLai();
        gdChuDong = true;
        gdDoi = id;
        gdLuc = System.currentTimeMillis();
        b.gui(66, (byte) 0, (short) id);
        long het = System.currentTimeMillis() + 40000;
        while (System.currentTimeMillis() < het && !b.dut && (gdChuDong || gdTrangThai != 0)) {
            b.bom(500);
            if (gdTrangThai == 0 && System.currentTimeMillis() - gdLuc > 15000) break; // khong ai nhan loi moi
        }
        boolean xong = gdXong > x0;
        if (gdTrangThai != 0) { b.gui(66, (byte) 3); b.bom(300); }
        gdDatLai();
        return xong;
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

    // ================================================================== Hop PC (goi -121 kieu 3 -> 4)
    /** Hop PC cua minh: {id, dex, cap, co} (co: bit1 khoa trao doi, bit4 dang theo). Cache cho luong doc goi (trao doi). */
    volatile List<int[]> hopCache = new ArrayList<>();

    List<int[]> hopPc() throws IOException {
        List<int[]> ds = docHop(b);
        if (ds != null) hopCache = ds;
        return hopCache;
    }

    /** Doc Hop PC qua goi -121 kieu 3 -> 4 (dung chung kich ban ngam); null = khong tra loi. */
    static List<int[]> docHop(BotChucNang.Bot b) throws IOException {
        int m = b.moc();
        b.gui(-121, (byte) 3);
        b.cho(x -> layGoi(x, m, -121, 4) != null, 3000);
        byte[] d = layGoi(b, m, -121, 4);
        List<int[]> ds = new ArrayList<>();
        if (d == null) return null;
        try {
            DataInputStream r = new DataInputStream(new ByteArrayInputStream(d));
            r.readByte(); r.readShort(); r.readShort(); r.readInt(); r.readShort();
            int n = r.readShort();
            for (int i = 0; i < n; i++) {
                int id = r.readInt(), dex = r.readShort(), cap = r.readByte();
                r.skipBytes(1 + 1 + 1 + 2 + 1 + 1 + 2 + 2 + 2 + 1 + 2);
                int co = r.readByte();
                r.skipBytes(6);
                r.readUTF(); r.readUTF(); r.readUTF(); r.readUTF();
                ds.add(new int[]{id, dex, cap, co});
            }
        } catch (IOException e) {
            // goi ngan hon (ban may chu khac): giu phan da doc
        }
        return ds;
    }

    /** Goi trao doi Pokemon (-121 kieu 6) trang thai {@code tt} sau moc. */
    static byte[] layGoi2(BotChucNang.Bot x, int tu, int tt) {
        byte[] r = null;
        for (int i = tu; i < x.su.size(); i++) {
            BotChucNang.SuKien e = x.su.get(i);
            if (e.cmd == -121 && e.d != null && e.d.length > 1 && e.d[0] == 6 && e.d[1] == tt) r = e.d;
        }
        return r;
    }

    static byte[] layGoi(BotChucNang.Bot x, int tu, int cmd, int kieu) {
        byte[] r = null;
        for (int i = tu; i < x.su.size(); i++) {
            BotChucNang.SuKien e = x.su.get(i);
            if (e.cmd == cmd && e.d != null && e.d.length > 0 && e.d[0] == kieu) r = e.d;
        }
        return r;
    }

    // ================================================================== bat Pokemon
    /**
     * Bat Pokemon: mua Poke Ball o Chansey (xu), ra bai luyen hop cap va cay; Pokemon hoang da bat duoc hien (-125, dex > 0
     * - quai hoi sinh co ~5% thanh hoang da o ban do ngoai troi) thi danh yeu (<= 35% HP) roi nem (-121 kieu 1).
     */
    boolean bat(long het) throws Exception {
        if (b.xu > 2500) coBong = KichBanCap140TinhNang.muaBong(this) || coBong; // moi lan di bat mua them mot goi 5 bong
        het = Math.max(het, System.currentTimeMillis() + 8 * 60000L); // kien nhan nhu nguoi choi san Pokemon
        if (het > henRa) het = henRa;
        int ml = mapLuyen();
        if (ml >= 0 && ml != b.mapId) toi(ml);
        int soBat0 = soBat;
        while (System.currentTimeMillis() < het && !dungLai && !b.dut) {
            if (!b.hoangDa.isEmpty()) {
                DichVuHlvAi.dem("gap_hoang_da", 1);
                if (KichBanCap140TinhNang.batPokemon(this)) { DichVuHlvAi.dem("bat_duoc", 1); hopPc(); return true; }
                b.hoangDa.clear();
                if (b.xu > 2500) KichBanCap140TinhNang.muaBong(this); // het bong / thoat: mua them roi san tiep
            }
            danh(-1, 4, 60000L);
            kiem();
        }
        return soBat > soBat0;
    }

    // ================================================================== PvP
    /** Khan PK cua nguoi xung quanh (goi 65: short actor, byte deo, byte loai): actor -> loai khan (bo khi thao). */
    final Map<Integer, Integer> khanNguoi = new java.util.concurrent.ConcurrentHashMap<>();
    int khanMinh = -1;

    /** Mua 1 khan PK (thuoc 14..18, 500 xu) o nha cua hang lang - nhu nguoi choi. */
    int muaKhan() throws Exception {
        for (int t = 14; t <= 18; t++) if (b.soThuoc(t) > 0) return t;
        if (b.xu < 3000) return -1;
        int lang = b.mapId;
        if (!KichBanDS.laLang(lang) && !toi(0)) return -1;
        if (!KichBanDS2.vaoNha(b, 103)) return -1;
        int t = 14 + rd.nextInt(5);
        b.gui(24, (byte) 1, (byte) 4, (short) t, (short) 1);
        b.bom(1200);
        b.lamMoi();
        KichBanDS2.raNgoai(b);
        DichVuHlvAi.dem("mua_khan", b.soThuoc(t) > 0 ? 1 : 0);
        return b.soThuoc(t) > 0 ? t : -1;
    }

    /**
     * PK cong bang: deo khan PK (ai deo khan = chap nhan PK), ra bai ngoai lang, chi danh nguoi DANG DEO khan khac mau (bot
     * hay nguoi that) - may chu ap luat PK (lech cap, nghiep, ban do cam). Cap 40+ trong lang: thach dau co cuoc nho voi bot
     * khac. Khong co ai thi Dau truong.
     */
    boolean pk(long het) throws Exception {
        if (b.lv >= 40 && rd.nextBoolean() && thachDau()) return true;
        int khan = muaKhan();
        if (khan < 0) return dauTruong();
        HlvAiNguoiChoi doi = DichVuHlvAi.doiThuPk(this);
        int dich = doi != null ? doi.b.mapId : mapLuyen();
        if (dich >= 0 && dich != b.mapId && !toi(dich)) return false;
        if (KichBanDS.laLang(b.mapId)) return false;
        b.gui(22, (byte) khan); // deo
        b.bom(800);
        khanMinh = khan;
        BotChucNang.BanDo bd = BotChucNang.BanDo.nap(b.mapLoad);
        int don = 0;
        long hetPk = Math.min(het, System.currentTimeMillis() + 3 * 60000L);
        while (System.currentTimeMillis() < hetPk && !b.dut && b.hp > 0) {
            Integer muc = null;
            for (Map.Entry<Integer, Integer> e : khanNguoi.entrySet()) {
                if (e.getValue() != null && e.getValue() >= 14 && e.getValue() != khan && b.nguoi.containsKey(e.getKey())) { muc = e.getKey(); break; }
            }
            if (muc == null) { danh(-1, 2, 30000L); continue; }
            int[] vt = b.nguoi.get(muc);
            if (vt != null && bd != null) { int[] o = bd.oGan(vt[0] / 16, vt[1] / 16, 2); if (o != null) b.diDuong(bd, o[0], o[1]); }
            for (int k = 0; k < 8 && !b.dut && b.hp > 0; k++) { b.gui(6, (short) (int) muc, (byte) 0); don++; b.bom(500); uongThuoc(); }
        }
        if (b.hp <= 0) hoiSinh();
        b.bom(10500); // khong thao khan trong 10 giay sau khi danh (luat CoPk)
        b.gui(22, (byte) khan); // thao
        b.bom(500);
        khanMinh = -1;
        DichVuHlvAi.dem("pk_don", don);
        return don > 0;
    }

    /**
     * Thach dau 1-1 co cuoc nho (1.000-5.000 xu) voi bot khac cap 40+ cung lang (-31 o nhap 5 = so xu, 6 = ten). May chu ky
     * quy hai ben, han muc bot -> nguoi that ap qua ChongClone.chanCaHai (thach_dau). Tran o san 205.
     */
    boolean thachDau() throws Exception {
        if (!java.util.Arrays.asList(0, 70, 301, 1701, 201).contains(b.mapId)) {
            if (!toi(0)) return false;
        }
        HlvAiNguoiChoi doi = null;
        for (DichVuHlvAi.Ho h : DichVuHlvAi.HO) {
            HlvAiNguoiChoi q = h.dang;
            if (q != null && q != this && q.b != null && !q.b.dut && q.b.mapId == b.mapId && q.b.lv >= 40) doi = q;
        }
        if (doi == null) return false;
        long cuoc = 1000 + rd.nextInt(5) * 1000L;
        if (b.xu < cuoc * 2) return false;
        int m = b.moc();
        b.gui(-31, (short) b.actor, (byte) 5, String.valueOf(cuoc));
        b.bom(800);
        b.gui(-31, (short) b.actor, (byte) 6, doi.b.ten);
        boolean vao = b.cho(x -> x.mapId == 205 || x.dut, 15000);
        DichVuHlvAi.dem("thach_dau", vao ? 1 : 0);
        if (!vao) { ghi("thach dau " + doi.b.ten + ": " + b.chuTu(m)); return false; }
        sanDau(doi.b.actor);
        return true;
    }

    /** Dang o san thach dau (205): danh doi thu toi khi ve (may chu dua ve 201 sau tran). */
    void sanDau(int doiActor) throws Exception {
        long het = System.currentTimeMillis() + 6 * 60000L;
        while (b.mapId == 205 && !b.dut && System.currentTimeMillis() < het) {
            int muc = doiActor;
            if (!b.nguoi.containsKey(muc) && !b.nguoi.isEmpty()) muc = b.nguoi.keySet().iterator().next();
            int[] vt = b.nguoi.get(muc);
            if (vt != null && (Math.abs(vt[0] - b.px) > 48 || Math.abs(vt[1] - b.py) > 48)) b.diToi(vt[0], vt[1]);
            if (b.hp > 0) b.gui(6, (short) muc, (byte) (rd.nextInt(Math.max(1, Math.min(4, b.chieu.length)))));
            b.bom(600);
            uongThuoc();
        }
        if (b.hp <= 0) hoiSinh();
    }

    /**
     * Dau truong 3v3 (-109): can 3 Pokemon khac loai trong Hop PC (bat duoc) - luu doi tan cong / phong thu, tim doi thu,
     * thach dau, moi luot chon chieu. Chua du 3 con thi di bat.
     */
    boolean dauTruong() throws Exception {
        List<int[]> hop = hopPc();
        List<Integer> chon = new ArrayList<>();
        java.util.Set<Integer> loai = new java.util.HashSet<>();
        for (int[] c : hop) if (loai.add(c[1]) && chon.size() < 3) chon.add(c[0]);
        if (chon.size() < 3) { DichVuHlvAi.dem("dau_truong_thieu_con", 1); return bat(System.currentTimeMillis() + 5 * 60000L); }
        if (!KichBanDS.laLang(b.mapId) && !toi(0)) return false;
        BotChucNang.BanDo bdl = BotChucNang.BanDo.nap(b.mapLoad);
        int[] oNpc = bdl == null ? null : bdl.oGan(22, 33, 3);
        if (oNpc != null) b.diDuong(bdl, oNpc[0], oNpc[1]);
        if (KichBanDauTruong.chonDauTruong(b, "Đấu trường Bảo Bối") < 0) return false;
        b.bom(800);
        for (int kieu = 0; kieu < 2; kieu++) {
            b.gui(-109, (byte) 1, (byte) kieu, (byte) 3, chon.get(0), chon.get(1), chon.get(2));
            b.bom(700);
        }
        int m = b.moc();
        b.gui(-109, (byte) 2, (byte) 0);
        byte[] dt = KichBanDauTruong.cho(b, m, 1, 5000);
        List<Integer> ds = dt == null ? new ArrayList<>() : KichBanDauTruong.doiThu(dt);
        if (ds.isEmpty()) return false;
        int doi = ds.get(rd.nextInt(ds.size()));
        m = b.moc();
        b.gui(-109, (byte) 3, doi, (byte) 0);
        byte[] bd = KichBanDauTruong.cho(b, m, 2, 5000);
        if (bd == null) return false;
        int tranId;
        try { DataInputStream r = new DataInputStream(new ByteArrayInputStream(bd)); r.readByte(); tranId = r.readInt(); } catch (IOException e) { return false; }
        for (int luot = 0; luot < 30 && !b.dut; luot++) {
            b.bom(1500 + rd.nextInt(1500));
            int mm = b.moc();
            b.gui(-109, (byte) 4, tranId, (byte) 0, (byte) rd.nextInt(2));
            KichBanDauTruong.cho(b, mm, 3, 6000);
            if (KichBanDauTruong.goi(b, m, 4) != null) break;
        }
        DichVuHlvAi.dem("dau_truong_tran", 1);
        return true;
    }

    /**
     * Pho ban 5 tang theo nhom (cap 45+): moi bot cung ban do, cap lech <= 10 vao nhom (48 / 49 - ho dong y ngay trong
     * luong doc goi), roi vao pho ban ca nhom (So tay -> Ham nguc -> Pho ban). Trong phong: danh toi het gio roi roi.
     */
    boolean phoBan(long het) throws Exception {
        if (b.hp < b.maxhp * 9 / 10) hoiMau("truoc pho ban");
        int moi = 0;
        for (DichVuHlvAi.Ho h : DichVuHlvAi.HO) {
            HlvAiNguoiChoi q = h.dang;
            if (moi >= 3 || q == null || q == this || q.b == null || q.b.dut || q.b.mapId != b.mapId || Math.abs(q.b.lv - b.lv) > 10) continue;
            if (moi == 0) { b.gui(48, new byte[0]); b.bom(500); }
            b.gui(49, (byte) 0, (short) q.b.actor);
            b.bom(800);
            moi++;
        }
        if (moi > 0) b.bom(2000);
        KichBanPhoBanCup.moVao(b);
        if (b.mapId != KichBanPhoBanCup.MAP_PB) return false;
        DichVuHlvAi.dem("pho_ban_nhom", moi);
        return trongPhoBan(het);
    }

    boolean trongPhoBan(long het) throws Exception {
        int ha0 = soHaQuai;
        long h = Math.min(het, System.currentTimeMillis() + 8 * 60000L);
        // phong pho ban: quai theo tang (khong theo cap / o sinh cua ban do luyen) -> danh moi quai nhin thay, het quai thi di
        // quanh phong tim (danh(-1) cua ban do luyen loc quai theo cap +-, phong khong co o sinh -> truoc day dung im = 0 quai)
        BotChucNang.BanDo bdPb = BotChucNang.BanDo.nap(b.mapLoad);
        while (System.currentTimeMillis() < h && !b.dut && b.mapId == KichBanPhoBanCup.MAP_PB && b.hp > 0) {
            Integer chon = null;
            double tot = 1e18;
            for (Map.Entry<Integer, int[]> e : b.quai.entrySet()) {
                int[] q = e.getValue();
                if (q == null || q[2] <= 0) continue;
                double kc = Math.hypot(q[0] - b.px, q[1] - b.py);
                if (kc < tot) { tot = kc; chon = e.getKey(); }
            }
            if (chon == null) {
                b.lamMoi();
                if (bdPb != null) {
                    int[] o = bdPb.oGan(Math.max(1, b.px / 16 + rd.nextInt(17) - 8), Math.max(1, b.py / 16 + rd.nextInt(17) - 8), 3);
                    if (o != null) b.diDuong(bdPb, o[0], o[1]);
                }
                b.bom(1500);
                continue;
            }
            int[] q = b.quai.get(chon);
            if (bdPb != null && q != null) { int[] o = bdPb.oGan(q[0] / 16, q[1] / 16, 2); if (o != null) b.diDuong(bdPb, o[0], o[1]); }
            for (int don = 0; don < 120 && !b.dut && b.hp > 0; don++) {
                int[] qt = b.quai.get(chon);
                b.gui(9, (short) (int) chon, (byte) BotXuyenSuot.chieuTheoHe(b, lop, qt == null ? -1 : qt[5], don));
                b.bom(260);
                if (don % 3 == 2) { b.gui(7, (short) (int) chon); b.bom(150); }
                int[] q2 = b.quai.get(chon);
                if (q2 == null || q2[2] == 0) break;
                if (bdPb != null && don % 6 == 5 && Math.hypot(q2[0] - b.px, q2[1] - b.py) > 60) {
                    int[] o = bdPb.oGan(q2[0] / 16, q2[1] / 16, 2);
                    if (o != null) b.diDuong(bdPb, o[0], o[1]);
                }
                if (b.hp > 0 && b.hp < b.maxhp / 2) uongThuoc();
            }
            int[] q3 = b.quai.get(chon);
            if (q3 != null && q3[2] == 0) { soHaQuai++; DichVuHlvAi.dem("pho_ban_ha_quai", 1); }
            else if (q3 == null) b.quai.remove(chon);
        }
        if (b.mapId == KichBanPhoBanCup.MAP_PB) {
            b.menu(KichBanPhoBanCup.SO_TAY, KichBanPhoBanCup.MENU_PB_TRONG, 0);
            b.cho(x -> x.mapId != KichBanPhoBanCup.MAP_PB, 4000);
        }
        if (b.hp <= 0) hoiSinh();
        return soHaQuai > ha0;
    }

    // ================================================================== doi (bang)
    long lanGopQuy, lanMoiDoi, lanLapDoi, lanHocChieu;

    /**
     * Doi: chua co doi + cap 50 -> lap doi (may chu mien phi cho bot khi so doi bot < hlv_ai.doi_bot_toi_da); chu doi moi
     * nguoi cung lang (cap 50, 1 loi moi / 10 phut); thanh vien chat doi, gop quy 1 lan / ngay (han muc bot -> nguoi).
     */
    boolean doi() throws Exception {
        if (!coDoi && b.lv >= 50 && System.currentTimeMillis() - lanLapDoi > 20 * 60000L) {
            lanLapDoi = System.currentTimeMillis();
            int m = b.moc();
            b.gui(-9, new byte[0]);
            b.cho(x -> x.coLenh(m, -9) || x.doiChu(m, 0, "") != null, 4000);
            BotChucNang.SuKien e = b.layLenh(m, -9);
            if (e != null && e.d.length >= 4) {
                int icon = ((e.d[2] & 255) << 8) | (e.d[3] & 255);
                String ten = ("Doi" + HlvAiTen.moi(rd)).replaceAll("[^0-9A-Za-z]", "");
                if (ten.length() > 15) ten = ten.substring(0, 15);
                int m2 = b.moc();
                b.gui(-10, (short) icon, ten);
                b.cho(x -> x.coLenh(m2, -10) || x.doiChu(m2, 0, "") != null, 5000);
                if (b.coLenh(m2, -10)) { coDoi = true; chuDoi = true; DichVuHlvAi.dem("lap_doi", 1); ghi("lap doi " + ten); }
            } else if (b.doiChu(m, 0, "đang ở trong đội") != null) {
                coDoi = true;
            }
        }
        if (coDoi && chuDoi && System.currentTimeMillis() - lanMoiDoi > 10 * 60000L && !b.nguoi.isEmpty()) {
            lanMoiDoi = System.currentTimeMillis();
            List<Integer> ds = new ArrayList<>(b.nguoi.keySet());
            b.gui(-11, (byte) 0, (short) (int) ds.get(rd.nextInt(ds.size())));
            b.bom(800);
            DichVuHlvAi.dem("moi_doi", 1);
        }
        if (coDoi && b.lv >= 50 && System.currentTimeMillis() - lanGopQuy > 20 * 3600000L / Math.max(1, (long) DichVuHlvAi.TANG_TOC) && b.xu > 20000) {
            lanGopQuy = System.currentTimeMillis();
            gopQuy(1000 + rd.nextInt(4) * 1000);
        }
        if (coDoi) {
            String c = HlvAiChat.chon(HlvAiChat.XA_HOI, rd, b.lv, b.tenMap, 0);
            if (c != null) { b.gui(-18, c); b.bom(500); lanChat = System.currentTimeMillis(); }
            return true;
        }
        return noi(HlvAiChat.XA_HOI, false);
    }

    boolean chuDoi;
    /** Kiem khai thac: bot dung yen gop quy doi (doi cua nguoi that vua moi) so xu nay; 0 = khong. */
    volatile int yeuCauGopQuy;

    /** Gop quy doi dang o (goi -20 + xac nhan popup -32), nhu nguoi choi. */
    boolean gopQuy(int xu) throws IOException {
        int m = b.moc();
        b.gui(-20, xu);
        b.cho(x -> x.layLenh(m, -32) != null, 3000);
        BotChucNang.SuKien pp = b.layLenh(m, -32);
        if (pp == null) return false;
        b.gui(-32, (short) pp.so[0], (byte) pp.so[1], "", (byte) 0);
        b.bom(1000);
        DichVuHlvAi.dem("gop_quy", 1);
        return true;
    }

    // ================================================================== hoc chieu
    static final int[][] ID_SKILL = {{6, 7, 8}, {6, 7, 8}, {8, 9, 10}, {6, 7, 8}, {6, 7, 8}};
    final java.util.Set<Integer> daHoc = new java.util.HashSet<>();

    /** Hoc chieu mua o NPC (-122 kieu 0 xem danh sach, goi 109 hoc) khi du cap va du xu (giu lai 20.000 xu). */
    boolean hocChieu() throws Exception {
        int m = b.moc();
        b.gui(-122, (byte) 0);
        b.cho(x -> layGoi(x, m, -122, 0) != null, 3000);
        byte[] d = layGoi(b, m, -122, 0);
        DichVuHlvAi.dem("hoc_chieu_xem", d == null ? 0 : 1);
        if (d == null) {
            // client ban -1 (J2ME cu) khong nhan goi learnset (HocChieuGame.coGoiMoi): nhu client cu - bam hoc tung chieu NPC
            // (goi 109 lop, idx), may chu tu kiem learnset / cap / nguon NPC va tra thong bao
            if (lop < 0 || lop >= ID_SKILL.length) return false;
            for (int idx = 0; idx < ID_SKILL[lop].length; idx++) {
                int o = ID_SKILL[lop][idx];
                if (daHoc.contains(o)) continue;
                int mh = b.moc();
                b.gui(109, (byte) lop, (byte) idx);
                String tb = b.doiChu(mh, 2500, "đã học", "chưa học được", "cần cấp", "tự học", "Không đủ xu");
                if (tb == null) continue;
                if (tb.contains("chưa học được") || tb.contains(": đã học")) daHoc.add(o); // loai khong hoc / da co: khong thu lai
                boolean ok = tb.contains("đã học") && !tb.contains(": đã học");
                DichVuHlvAi.dem("hoc_chieu", ok ? 1 : 0);
                if (ok) { daHoc.add(o); b.lamMoi(); ghi("hoc chieu o " + o + ": " + tb); return true; }
            }
            return false;
        }
        try {
            DataInputStream r = new DataInputStream(new ByteArrayInputStream(d));
            r.readByte(); r.readShort();
            int lop = r.readByte();
            r.readByte();
            int n = r.readByte();
            for (int i = 0; i < n; i++) {
                int o = r.readByte();
                r.readShort(); r.readUTF();
                String ten = r.readUTF();
                r.readByte();
                int nguon = r.readByte(), capHoc = r.readByte(), gia = r.readInt();
                if (nguon != 1 || capHoc > b.lv || daHoc.contains(o) || lop < 0 || lop >= ID_SKILL.length) continue; // bot hoc mien phi
                int idx = -1;
                for (int k = 0; k < ID_SKILL[lop].length; k++) if (ID_SKILL[lop][k] == o) idx = k;
                if (idx < 0) continue;
                daHoc.add(o);
                int mh = b.moc();
                b.gui(109, (byte) lop, (byte) idx);
                boolean ok = b.doiChu(mh, 2500, "đã học") != null;
                b.lamMoi();
                DichVuHlvAi.dem("hoc_chieu", ok ? 1 : 0);
                ghi("hoc chieu " + ten + " (" + gia + " xu): " + ok);
                return ok;
            }
        } catch (IOException ignored) {
        }
        return false;
    }

    // ================================================================== trao doi Pokemon
    /**
     * Trao doi Pokemon (-121 kieu 6) - dieu khien trong luong doc goi: nhan loi moi, dat MOT con (khong dang theo, khong khoa
     * trao doi) khi doi phuong dat, khoa khi hai ben deu da dat, xac nhan khi doi phuong da khoa. 1 doi 1, phi do may chu thu;
     * han muc gia tri bot -> nguoi qua ChongClone (trao_doi_pokemon).
     */
    volatile int tdTrangThai;
    volatile long tdLuc, lanMoiTd;
    volatile int tdXong;

    void tdGoi(byte[] d) throws IOException {
        if (d.length < 2) return;
        int tt = d[1];
        DataInputStream r = new DataInputStream(new ByteArrayInputStream(d));
        r.readByte(); r.readByte();
        if (tt == 1) {
            int id = r.readShort();
            if (tdTrangThai == 0 && DichVuHlvAi.bat("giao_dich") && b.lv >= 20 && rd.nextInt(4) > 0 && conDeTrao() != null) {
                b.gui(-121, (byte) 6, (byte) 1, (byte) 1);
                tdTrangThai = 1;
                tdLuc = System.currentTimeMillis();
            } else {
                b.gui(-121, (byte) 6, (byte) 1, (byte) 0);
            }
        } else if (tt == 2) {
            tdTrangThai = Math.max(1, tdTrangThai);
            tdLuc = System.currentTimeMillis();
            r.readUTF();
            int buocMinh = r.readByte(), buocDoi = r.readByte();
            int nMinh = r.readByte();
            for (int i = 0; i < nMinh; i++) boQuaCon(r);
            int nDoi = r.readByte();
            if (nMinh == 0 && (nDoi > 0 || tdChuDong)) {
                int[] c = conDeTrao();
                if (c == null) { b.gui(-121, (byte) 6, (byte) 5); tdTrangThai = 0; return; }
                b.gui(-121, (byte) 6, (byte) 2, (byte) 1, c[0]);
            } else if (nMinh > 0 && nDoi > 0 && buocMinh == 0) {
                b.gui(-121, (byte) 6, (byte) 3);
            } else if (nMinh > 0 && nDoi > 0 && buocMinh == 1 && buocDoi >= 1) {
                b.gui(-121, (byte) 6, (byte) 4);
            }
        } else if (tt == 3) {
            tdXong++;
            DichVuHlvAi.dem("trao_doi_xong", 1);
            tdTrangThai = 0;
            tdChuDong = false;
        } else if (tt == 0) {
            tdTrangThai = 0;
            tdChuDong = false;
        }
    }

    volatile boolean tdChuDong;

    static void boQuaCon(DataInputStream r) throws IOException {
        r.readInt(); r.readShort(); r.skipBytes(1 + 1 + 1 + 1 + 2 + 1 + 1); r.readUTF();
    }

    int[] conDeTrao() {
        for (int[] c : hopCache) if ((c[3] & (2 | 16)) == 0) return c;
        return null;
    }

    /** Moi mot nguoi gan do trao doi Pokemon (toi da 1 lan / 15 phut); phien do tdGoi lo. */
    boolean traoDoiPokemon() throws Exception {
        if (System.currentTimeMillis() - lanMoiTd < 15 * 60000L || b.nguoi.isEmpty() || tdTrangThai != 0) return false;
        hopPc();
        if (conDeTrao() == null) return false;
        lanMoiTd = System.currentTimeMillis();
        List<Integer> ds = new ArrayList<>(b.nguoi.keySet());
        int x0 = tdXong;
        tdChuDong = true;
        b.gui(-121, (byte) 6, (byte) 0, (short) (int) ds.get(rd.nextInt(ds.size())));
        long het = System.currentTimeMillis() + 60000;
        tdLuc = System.currentTimeMillis();
        while (System.currentTimeMillis() < het && !b.dut && tdChuDong) {
            b.bom(500);
            if (tdTrangThai == 0 && System.currentTimeMillis() - tdLuc > 35000) break;
        }
        if (tdChuDong && tdTrangThai != 0) b.gui(-121, (byte) 6, (byte) 5);
        tdChuDong = false;
        tdTrangThai = 0;
        if (tdXong > x0) hopPc();
        return tdXong > x0;
    }

    // ================================================================== xa hoi
    /** Nghe goi den: loi moi to doi / giao dich / vao doi / ket ban, chat, boss xuat hien -> hop (xu ly ngoai luong doc). */
    void ngheXaHoi() {
        final BotChucNang.Bot bb = b;
        final java.util.function.Consumer<BotChucNang.SuKien> cu = bb.ngheNhan;
        bb.ngheNhan = e -> {
            if (cu != null) cu.accept(e);
            try {
                byte[] d = e.d;
                if (e.cmd == 66 && d != null && d.length >= 1) { gdGoi(d); return; }
                if (e.cmd == -121 && d != null && d.length >= 2 && d[0] == 6) { tdGoi(d); return; }
                if (e.cmd == 65 && d != null && d.length >= 4) { // khan PK cua nguoi xung quanh
                    int ac = (short) (((d[0] & 255) << 8) | (d[1] & 255));
                    if (d[2] == 1) khanNguoi.put(ac, (int) d[3]); else khanNguoi.remove(ac);
                    return;
                }
                if (e.cmd == 49 && d != null && d.length >= 5 && d[0] == 0) { // loi moi to doi: dong y ngay (pho ban nhom)
                    if (rd.nextInt(ho.tinhCach == 3 ? 1 : 2) == 0) { b.gui(49, (byte) 1, (short) (((d[3] & 255) << 8) | (d[4] & 255))); DichVuHlvAi.dem("to_doi", 1); }
                    return;
                }
                if (e.cmd == -32 && e.so != null && e.so[1] == 10) { // loi thach dau 1-1 co cuoc: dong y khi du cap
                    boolean nhan = b.lv >= 40 && DichVuHlvAi.bat("pk") && rd.nextInt(3) > 0 && !dungLai;
                    b.gui(-32, (short) e.so[0], (byte) 10, "", (byte) (nhan ? 1 : 0));
                    DichVuHlvAi.dem("nhan_thach_dau", nhan ? 1 : 0);
                    return;
                }
                if (hop.size() > 30) return;
                if (e.cmd == -11 && d != null && d.length >= 3 && d[0] == 0) hop.add(new Object[]{"vao_doi", (int) (short) (((d[1] & 255) << 8) | (d[2] & 255)), ""});
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
        if (gdTrangThai != 0 && !gdChuDong && System.currentTimeMillis() - gdLuc > 25000) { // doi phuong khong dat gi
            b.gui(66, (byte) 3);
            gdDatLai();
        }
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
                        chuDoi = false;
                        DichVuHlvAi.dem("vao_doi", 1);
                    }
                    break;
                case "ket_ban":
                    if (DichVuHlvAi.bat("ban_be") && rd.nextInt(3) > 0) { b.gui(101, (short) id, (byte) 1); b.bom(400); DichVuHlvAi.dem("nhan_ket_ban", 1); }
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
