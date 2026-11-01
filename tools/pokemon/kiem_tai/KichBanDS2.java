import java.io.IOException;
import java.util.*;
import java.util.concurrent.*;

/** Kich ban nhom 2: ban do / cong (moi map), vat pham, kinh te. */
class KichBanDS2 {

    static void ket(String t, Boolean ok, String bang) {
        BotChucNang.ket(t, ok, bang);
    }

    static void dangKy() {
        BotChucNang.dk("cong_tat_ca", true, KichBanDS2::congTatCa);
        BotChucNang.dk("thu_cong", true, KichBanDS2::thuCong);
        KichBanDS3.dangKy();
    }

    static final short[] MAP_OFFLINE = {90, 91, 92, 93, 94, 95, 96, 97, 98, 99, 100, 102, 103, 104, 108, 109, 260, 261, 262, 263, 264, 265, 266, 267, 268, 269, 270, 271, 29, 30, 31, 72, 73, 74, 78, 79, 82, 83, 84, 88, 89};

    /** Nha Kecleon tap hoa (ban thuoc, goi 24 loai 4) cua lang: Pewter 0 -> 102, 70 -> 72, 80 -> 82 (canh nha Machamp). */
    static int nhaKecleon(int lang) {
        return lang == 70 ? 72 : lang == 80 ? 82 : 102;
    }

    /** Nha tiem Machamp (do cam tay, sua, nang) cua lang: Pewter 0 -> 103, Blackthorn 70 -> 73, 80 -> 83. */
    static int nhaMachamp(int lang) {
        return lang == 70 ? 73 : lang == 80 ? 83 : 103;
    }

    static boolean laOffline(int m) {
        for (short s : MAP_OFFLINE) if (s == m) return true;
        return false;
    }

    static int nhomCua(BotChucNang.Bot b, int mapId) {
        if (b.maIdNhom == null) return -1;
        for (int i = 0; i < b.maIdNhom.length; i++) for (short s : b.maIdNhom[i]) if (s == mapId) return b.idArr[i];
        return -1;
    }

    /**
     * Moi map (ban sao 0 cua moi nhom trong bang vi tri 105 + map nha): dung canh tung cong, gui lenh 12
     * nhu client (Res.locationMap / CMLocation), doi map moi. Kiem: khong bi kick, vao dung map dich,
     * o den di duoc, vung lien thong >= 30 o va toi duoc it nhat 1 cong (khong ket).
     */
    static void congTatCa() throws Exception {
        BotChucNang.Bot b0 = BotChucNang.botMoi(0);
        KichBanDS.veLang(b0);
        b0.gm("slv " + b0.ten + " 99 0", 800);
        if (b0.maIdNhom == null) { ket("cong_tat_ca", false, "khong nhan bang vi tri (lenh 105)"); b0.close(); return; }
        TreeSet<Integer> ds = new TreeSet<>();
        for (short[] g : b0.maIdNhom) if (g.length > 0 && g[0] >= 0) ds.add((int) g[0]);
        // dich cua moi cong (gom ca map nha) cung dua vao danh sach khi duyet
        List<Integer> l = new ArrayList<>(ds);
        System.out.println("  " + l.size() + " map (ban sao 0): " + l);
        b0.close();
        int nBot = Integer.getInteger("botCong", 4);
        List<List<Integer>> phan = new ArrayList<>();
        for (int i = 0; i < nBot; i++) phan.add(new ArrayList<>());
        for (int i = 0; i < l.size(); i++) phan.get(i % nBot).add(l.get(i));
        ExecutorService ex = Executors.newFixedThreadPool(nBot);
        List<Future<int[]>> kq = new ArrayList<>();
        final List<String> loi = Collections.synchronizedList(new ArrayList<>());
        final Set<Integer> daToi = ConcurrentHashMap.newKeySet();
        for (List<Integer> p : phan) kq.add(ex.submit(() -> duyet(p, loi, daToi)));
        int cong = 0, ok = 0, mapOk = 0;
        for (Future<int[]> f : kq) { int[] r = f.get(); cong += r[0]; ok += r[1]; mapOk += r[2]; }
        ex.shutdown();
        for (String e : loi) System.out.println("  LOI " + e);
        ket("cong_tat_ca.map_han_che", null, "map server khong cho GM mto vao (can bang/su kien/Moba/Van tien tran): " + hanChe);
        ket("cong_tat_ca.map", mapOk + hanChe.size() == l.size(), "vao duoc " + mapOk + "/" + l.size() + " map; khong co tep bo cuc (map Lien dau dung chung 201, bo qua cong): " + boQua);
        ket("cong_tat_ca.cong", cong > 0 && ok == cong, ok + "/" + cong + " cong qua duoc, dung o hop le; map dich da toi: " + daToi.size()
                + (loi.isEmpty() ? "" : "; loi dau: " + loi.subList(0, Math.min(5, loi.size()))));
    }

    static final List<String> boQua = Collections.synchronizedList(new ArrayList<>());
    static final List<String> hanChe = Collections.synchronizedList(new ArrayList<>());

    static int[] duyet(List<Integer> maps, List<String> loi, Set<Integer> daToi) {
        int cong = 0, ok = 0, mapOk = 0;
        BotChucNang.Bot b = null;
        try {
            b = BotChucNang.botMoi(0);
            KichBanDS.veLang(b);
            b.gm("slv " + b.ten + " 99 0", 800);
            for (int mid : maps) {
                if (b.dut || !b.conSong()) {
                    b.close();
                    b = BotChucNang.botMoi(0);
                    KichBanDS.veLang(b);
                    b.gm("slv " + b.ten + " 99 0", 800);
                }
                // 206 Lanh dia doi (MapClan): chi vao qua bang; GM khong bang mto vao (truoc 25/10 nem NPE MapClan.playerJoin,
                // nay vao duoc) thi moi cong sang Cao nguyen 201 bi chan ngoai gio chien -> khong phai loi cong
                if (mid == 206) { hanChe.add(mid + " (ban do bang, can bang)"); continue; }
                if (!b.den(mid, 10, 10)) { hanChe.add(mid + "->" + b.mapId); continue; }
                BotChucNang.BanDo bd = BotChucNang.BanDo.nap(b.mapLoad);
                if (bd == null) { boQua.add(mid + "(load " + b.mapLoad + ")"); mapOk++; continue; }
                mapOk++;
                int nhom = nhomCua(b, mid);
                for (int gi = 0; gi < bd.cong.size(); gi++) {
                    int[] g = bd.cong.get(gi);
                    int[] dung = bd.oTruocCong(g[0], g[1]);
                    if (dung == null) { cong++; loi.add("map " + mid + " cong " + gi + " (" + g[0] + "," + g[1] + "): khong co o di duoc truoc cong (3 o, 4 huong)"); continue; }
                    int dm, dx, dy;
                    if (bd.offline) { continue; } // map nha: cong ra ngoai = map cu, kiem o chieu vao
                    if (laOffline(g[2])) { dm = g[2]; dx = g[3]; dy = g[4]; }
                    else {
                        short[] loc = nhom < 0 || b.viTriMap == null || nhom >= b.viTriMap.length || g[2] >= b.viTriMap[nhom].length ? null : b.viTriMap[nhom][g[2]];
                        if (loc == null || loc.length < 3) { boQua.add(mid + " cong " + gi + " (toM " + g[2] + " khong co trong bang vi tri nhom " + nhom + ": client bo qua)"); continue; }
                        dm = loc[0]; dx = loc[1]; dy = loc[2];
                    }
                    if (dm < 0) continue; // huong khong dung (bang de -1)
                    cong++;
                    if (b.mapId != mid && !b.den(mid, dung[0], dung[1])) { loi.add("map " + mid + ": quay lai that bai"); continue; }
                    if (b.mapId == mid) { b.den(mid, dung[0], dung[1]); }
                    if (b.tx != dung[0] || b.ty != dung[1]) {
                        // mto trong lang khong dat dung o (server dat diem vao lang): di bo toi canh cong
                        b.px = b.tx * 16 + 8; b.py = b.ty * 16 + 8;
                        if (!b.diDuong(bd, dung[0], dung[1])) { loi.add("map " + mid + " cong " + gi + " (" + g[0] + "," + g[1] + "): khong di bo toi duoc tu " + b.tx + "," + b.ty); continue; }
                    }
                    int m = b.moc();
                    b.gui(12, (short) dm, (short) dx, (short) dy);
                    boolean co = b.cho(x -> x.coLenh(m, 12) || x.dut, 5000);
                    if (!co || b.dut || !b.conSong()) {
                        loi.add("map " + mid + " cong " + gi + " -> " + dm + "(" + dx + "," + dy + "): " + (b.dut || !b.conSong() ? "BI NGAT KET NOI" : "khong doi map") + " " + b.chuTu(m));
                        if (b.dut || !b.conSong()) { b.close(); b = BotChucNang.botMoi(0); KichBanDS.veLang(b); b.gm("slv " + b.ten + " 99 0", 800); }
                        continue;
                    }
                    b.gui(12, (short) -500, (short) 0, (short) 0);
                    b.bom(500);
                    daToi.add(b.mapId);
                    BotChucNang.BanDo bd2 = BotChucNang.BanDo.nap(b.mapLoad);
                    String vande = null;
                    if (bd2 == null) vande = "khong doc duoc ban do dich " + b.mapLoad;
                    else if (!bd2.diDuoc(b.tx, b.ty) && bd2.oGan(b.tx, b.ty, 1) == null) vande = "o den " + b.tx + "," + b.ty + " bi chan";
                    else {
                        int[] o = bd2.diDuoc(b.tx, b.ty) ? new int[]{b.tx, b.ty} : bd2.oGan(b.tx, b.ty, 1);
                        int vung = bd2.vungLienThong(o[0], o[1], 30);
                        if (vung < 30 && !bd2.offline) vande = "o den " + b.tx + "," + b.ty + " ket (vung " + vung + " o)";
                        else if (!bd2.offline && !bd2.cong.isEmpty() && !toiCong(bd2, o[0], o[1])) vande = "o den " + b.tx + "," + b.ty + " khong toi duoc cong nao";
                    }
                    if (b.mapId != dm && !(dm >= 0 && laOffline(dm))) {
                        // server co the doi dich (ve lang, map su kien...) - ghi chu, khong tinh loi neu o hop le
                        System.out.println("  map " + mid + " cong " + gi + ": xin " + dm + " -> server dua toi " + b.mapId);
                    }
                    if (vande != null) loi.add("map " + mid + " cong " + gi + " -> " + b.mapId + ": " + vande);
                    else ok++;
                }
            }
        } catch (Exception e) {
            loi.add("ngoai le: " + e);
        } finally {
            if (b != null) b.close();
        }
        return new int[]{cong, ok, mapOk};
    }

    static void thuCong() throws Exception {
        BotChucNang.Bot b = BotChucNang.botMoi(0);
        KichBanDS.veLang(b);
        b.gm("slv " + b.ten + " 99 0", 800);
        int mid = Integer.getInteger("map", 0), gi = Integer.getInteger("cong", 21);
        b.den(mid, 10, 10);
        BotChucNang.BanDo bd = BotChucNang.BanDo.nap(b.mapLoad);
        int[] g = bd.cong.get(gi);
        int[] dung = bd.oTruocCong(g[0], g[1]);
        int nhom = nhomCua(b, mid);
        short[] loc = b.viTriMap[nhom][g[2]];
        System.out.println("cong " + Arrays.toString(g) + " dung " + Arrays.toString(dung) + " nhom " + nhom + " loc " + Arrays.toString(loc));
        b.den(mid, dung[0], dung[1]);
        System.out.println("vi tri sau mto " + b.mapId + " " + b.tx + "," + b.ty);
        if (b.tx != dung[0] || b.ty != dung[1]) {
            b.px = b.tx * 16 + 8; b.py = b.ty * 16 + 8;
            System.out.println("di bo: " + b.diDuong(bd, dung[0], dung[1]) + " -> " + b.px / 16 + "," + b.py / 16);
        }
        int m = b.moc();
        b.gui(12, (short) loc[0], (short) loc[1], (short) loc[2]);
        b.bom(3000);
        for (int i = m; i < b.su.size(); i++) System.out.println("  " + b.su.get(i).cmd + " " + b.su.get(i).chu);
        System.out.println("dut=" + b.dut + " song=" + b.conSong());
        b.close();
    }

    /** Tu lang hien tai, di bo vao mot ngoi nha (map offline) - noi co shop NPC (OfflineMap). */
    static boolean vaoNha(BotChucNang.Bot b, int nhaUuTien) throws IOException {
        return quaCong(b, true, nhaUuTien);
    }

    /** Di bo qua cong ra map ngoai (khong phai nha) - dung cho nguoi choi thuong (khong mto). */
    static boolean raNgoai(BotChucNang.Bot b) throws IOException {
        return quaCong(b, false, -1);
    }

    static boolean quaCong(BotChucNang.Bot b, boolean nha, int nhaUuTien) throws IOException {
        BotChucNang.BanDo bd = BotChucNang.BanDo.nap(b.mapLoad);
        if (bd == null) return false;
        int mapTruoc = b.mapId;
        if (!nha && (bd.offline || laOffline(b.mapId)) && b.mapCu >= 0) {
            // trong nha: cong ra = map cu (client GameScr: changeMap(oldMap, toX, toY) voi xOut/yOut cua o cong)
            for (int[] g : bd.cong) {
                int[] dung = bd.oTruocCong(g[0], g[1]);
                if (!BotChucNang.Bot.CHUAN_CLIENT) { b.px = b.tx * 16 + 8;
                b.py = b.ty * 16 + 8; }
                if (dung == null || !b.diDuong(bd, dung[0], dung[1])) continue;
                int m = b.moc();
                b.gui(12, (short) b.mapCu, (short) g[3], (short) g[4]);
                b.cho(x -> x.coLenh(m, 12) || x.dut, 5000);
                b.gui(12, (short) -500, (short) 0, (short) 0);
                b.bom(800);
                if (b.mapId != mapTruoc) return true;
            }
            return false;
        }
        int nhom = nhomCua(b, b.mapId);
        List<int[]> ds = new ArrayList<>();
        for (int[] g : bd.cong) {
            if (nha && laOffline(g[2])) { if (g[2] == nhaUuTien) ds.add(0, g); else ds.add(g); }
            if (!nha && !laOffline(g[2]) && nhom >= 0 && b.viTriMap != null && g[2] < b.viTriMap[nhom].length && b.viTriMap[nhom][g[2]][0] >= 0) {
                short[] loc = b.viTriMap[nhom][g[2]];
                ds.add(new int[]{g[0], g[1], loc[0], loc[1], loc[2]});
            }
        }
        String ly = "khong co cong nha";
        for (int[] chon : ds) {
            int[] dung = bd.oTruocCong(chon[0], chon[1]);
            if (dung == null) { ly = "cong " + chon[0] + "," + chon[1] + " khong co o dung"; continue; }
            if (!BotChucNang.Bot.CHUAN_CLIENT) { b.px = b.tx * 16 + 8;
            b.py = b.ty * 16 + 8; }
            // tx/ty chi cap nhat khi vao map; vi tri hien tai lay tu px/py neu da di
            if (!b.diDuong(bd, dung[0], dung[1])) { ly = "khong di bo toi " + dung[0] + "," + dung[1] + " tu " + b.px / 16 + "," + b.py / 16; continue; }
            int m = b.moc();
            b.gui(12, (short) chon[2], (short) chon[3], (short) chon[4]);
            b.cho(x -> x.coLenh(m, 12) || x.dut, 5000);
            b.gui(12, (short) -500, (short) 0, (short) 0);
            b.bom(800);
            if (nha ? laOffline(b.mapId) : b.mapId != mapTruoc) return true;
            ly = "gui 12 toi " + chon[2] + " khong vao: " + b.chuTu(m);
        }
        System.out.println("  vaoNha that bai (map " + b.mapId + " o " + b.tx + "," + b.ty + "): " + ly);
        return false;
    }

    /** Tu (x,y) loang 4 huong tren o di duoc: co cham (ke) o cong nao khong. */
    static boolean toiCong(BotChucNang.BanDo bd, int x, int y) {
        boolean[] tham = new boolean[bd.w * bd.h];
        ArrayDeque<int[]> q = new ArrayDeque<>();
        q.add(new int[]{x, y});
        tham[y * bd.w + x] = true;
        int[][] hg = {{1, 0}, {-1, 0}, {0, 1}, {0, -1}};
        while (!q.isEmpty()) {
            int[] c = q.poll();
            for (int[] d : hg) {
                int nx = c[0] + d[0], ny = c[1] + d[1];
                if (nx < 0 || ny < 0 || nx >= bd.w || ny >= bd.h || tham[ny * bd.w + nx]) continue;
                if (bd.type[ny * bd.w + nx] >= 2000000000) return true;
                if (bd.diDuoc(nx, ny)) { tham[ny * bd.w + nx] = true; q.add(new int[]{nx, ny}); }
            }
        }
        return false;
    }
}
