import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.Predicate;

/**
 * Bo kich ban kiem thu CHUC NANG cho server Huyen Thoai Bao Boi (noi giao thuc that, giong BotTai).
 * Moi kich ban in PASS / FAIL / BO QUA kem bang chung (thong bao server, so du, dong CSDL).
 *
 * Chay (o thu muc HTBB/):
 *   javac -d build/kiem_tai tools/pokemon/kiem_tai/BotChucNang.java
 *   java -cp build/kiem_tai -Ddb=htbb_db_bot -Dgm=kt001 BotChucNang 127.0.0.1 19169 kt [kich_ban ...]
 *     args: host cong tienTo [ten kich ban | all]   (tai khoan <tienTo>NNN / 123456)
 *     -Ddb      CSDL nhan vat de doi chieu (bo trong = khong kiem CSDL)
 *     -Dtu=N    so thu tu tai khoan dau tien (mac dinh 1); moi kich ban lay tai khoan rieng
 *     -Dden=N   so thu tu tai khoan GM cuoi (mac dinh 40, het thi quay vong). Tai khoan GM <tienTo>001..040 phai co
 *               ten trong sv.adminAccounts - o do toi da 255 ky tu (cot cau_hinh.gia_tri, gioi_han doDai 255):
 *               "admin,kt001,...,kt040" = 245 ky tu. Danh sach dai hon truoc day bi CSDL cat im lang (kt042.. mat GM,
 *               pvp / dung_vat_pham / chiem_thanh FAIL); nay server khong chep va bao "[CAUHINH] KHONG chep".
 *               Tai khoan thuong: <tienTo>101..120 (-DtuThuong / -DdenThuong).
 *     -Dgm=0    server khong cho bot quyen GM (vd server that): bo qua kich ban can GM
 *     -Dnoi=... thu muc monorepo htbb (doc server/cMap/ de kiem o di duoc)
 * Ket qua tong: in cuoi + ghi file -Dra=<tep> (mac dinh khong ghi).
 *
 * Xem tools/README.md (muc kiem_tai) va docs/kien-truc.md.
 */
public class BotChucNang {

    // =========================================================== cau hinh chung
    static String HOST;
    static int PORT;
    static String TIEN_TO;
    static final String DB = System.getProperty("db", "");
    static final int BAN = Integer.getInteger("ban", -1);
    static final boolean GM = !"0".equals(System.getProperty("gm", "1"));
    static final String NOI = System.getProperty("noi", System.getProperty("user.home") + "/Downloads/pokemon/htbb");
    /**
     * Goc MA NGUON (ban checkout monorepo dang chay bo bot: server/src, du_lieu, client_pc) - khac {@link #NOI} (thu muc
     * may chu dang chay: server/map, server/cMap, sql/htbb.sql). Staging (/opt/htbb-staging) chi co tai nguyen may chu,
     * khong co server/src (cong kiem 25/10: NoSuchFileException RealController.java). chay_hoi_quy.sh truyen -Dgoc.
     */
    static final String GOC = System.getProperty("goc", NOI);

    /** Tep ma nguon trong {@link #GOC}; thieu -> loi RO RANG (khong phai NoSuchFileException tran). */
    static java.io.File nguon(String duong) throws java.io.FileNotFoundException {
        java.io.File f = new java.io.File(GOC, duong);
        if (!f.isFile()) throw new java.io.FileNotFoundException("thieu ma nguon " + f + " (-Dgoc=<monorepo> - chay_hoi_quy.sh tu dat)");
        return f;
    }
    static int soTk = Integer.getInteger("tu", 1);
    static final List<String> KET_QUA = new ArrayList<>();
    static int nPass, nFail, nBo;

    static final int TK_DAU = Integer.getInteger("tu", 1), TK_CUOI = Integer.getInteger("den", 40);

    /** Tai khoan ke tiep trong [tu, den], quay vong (moi kich ban mot tai khoan, khong dung chung cung luc). */
    static synchronized String tkMoi() {
        if (soTk > TK_CUOI) soTk = TK_DAU;
        return String.format("%s%03d", TIEN_TO, soTk++);
    }

    // =========================================================== mot ket noi bot
    static class Bot implements Closeable {
        final String user;
        Socket s;
        DataInputStream in;
        OutputStream out;
        byte[] key;
        int curR, curW;
        boolean dut;
        final ArrayDeque<Long> guiGan = new ArrayDeque<>();
        // trang thai
        int mapCu = -1;
        int charDb = -1, actor = -1, lv, pt, hp, maxhp, mp, maxmp, nSkill, quocGia = -1, mapId = -1, mapLoad = -1, tx, ty, px, py;
        String ten = "", tenMap = "";
        int[] chieu = new int[0];
        long xu = -1;
        int tanCong, thuVl, thuPt; // goi 3 (do suc: KichBanDoSuc)
        int luong = -1, lk = -1;
        boolean coBanDo;
        final Map<Integer, Integer> thuoc = new TreeMap<>();
        final List<int[]> doDung = new ArrayList<>(); // {id, tpl, plus, level, lock, color}
        final Map<Integer, int[]> ngoc = new TreeMap<>(); // realId -> {tpl, sl, khoa}
        final List<int[]> dsNv = new ArrayList<>(); // {dbid, lv, using}
        final List<String> dsTen = new ArrayList<>();
        final List<int[]> thu = new ArrayList<>(), vatNuoi = new ArrayList<>();
        final Map<Integer, String> tenThu = new HashMap<>();
        int soMac = -1;
        final List<Integer> dangMac = new ArrayList<>(), dangMacTpl = new ArrayList<>(), dangMacPlus = new ArrayList<>();
        final Map<Integer, int[]> quai = new HashMap<>(); // id -> {x,y,hp,lv,maxhp,tpl}
        final Map<Integer, int[]> hoangDa = new java.util.concurrent.ConcurrentHashMap<>(); // id quai -> {dex, bac} (-125)
        final Map<Integer, int[]> nguoi = new HashMap<>(); // actor -> {x,y}
        short[][][] viTriMap; // [nhom][toM][clone*3]
        short[][] maIdNhom;
        short[] idArr;
        final List<SuKien> su = new ArrayList<>();
        final Map<Integer, Integer> demLenh = new TreeMap<>();
        int[] menuCuoi; // {npc, menu}
        List<String> menuChu = new ArrayList<>();
        /** Nghe moi goi den sau khi phan tich (bat bien HoiQuy). */
        java.util.function.Consumer<SuKien> ngheNhan;
        /** Nghe moi goi di (cmd, du lieu) truoc khi gui. */
        java.util.function.BiConsumer<Integer, byte[]> ngheGui;

        Bot(String user) {
            this.user = user;
        }

        // ------------------------------------------------------- ma hoa + khung goi
        byte rk(byte b) {
            if (key == null) return b;
            byte r = (byte) (key[curR] ^ b);
            curR = (curR + 1) % key.length;
            return r;
        }

        byte wk(byte b) {
            if (key == null) return b;
            byte r = (byte) (key[curW] ^ b);
            curW = (curW + 1) % key.length;
            return r;
        }

        void gui(int cmd, byte[] d) throws IOException {
            // tu gioi han 200 goi / 5 giay (server chan 300)
            long bay = System.currentTimeMillis();
            while (!guiGan.isEmpty() && bay - guiGan.peekFirst() > 5000) guiGan.pollFirst();
            if (guiGan.size() >= 200) {
                try { Thread.sleep(5000 - (bay - guiGan.peekFirst()) + 20); } catch (InterruptedException ignored) { }
            }
            guiGan.addLast(System.currentTimeMillis());
            if (ngheGui != null) ngheGui.accept(cmd, d);
            ByteArrayOutputStream bo = new ByteArrayOutputStream();
            bo.write(wk((byte) cmd));
            bo.write(wk((byte) (d.length >> 8)));
            bo.write(wk((byte) d.length));
            for (byte b : d) bo.write(wk(b));
            out.write(bo.toByteArray());
            out.flush();
        }

        void gui(int cmd, Object... f) throws IOException {
            gui(cmd, goi(f));
        }

        Object[] doc() throws IOException {
            int cmd = rk(in.readByte());
            int size;
            if (key != null && cmd == -128) {
                cmd = rk(in.readByte());
                byte[] a = new byte[4];
                in.readFully(a);
                size = ((rk(a[0]) & 255) << 24) | ((rk(a[1]) & 255) << 16) | ((rk(a[2]) & 255) << 8) | (rk(a[3]) & 255);
            } else {
                int a1 = rk(in.readByte()) & 255, a2 = rk(in.readByte()) & 255;
                size = (a1 << 8) | a2;
            }
            byte[] d = new byte[size];
            in.readFully(d);
            for (int i = 0; i < d.length; i++) d[i] = rk(d[i]);
            return new Object[]{cmd, d};
        }

        void ketNoi() throws IOException {
            s = new Socket();
            s.connect(new java.net.InetSocketAddress(HOST, PORT), 10000);
            s.setSoTimeout(0);
            in = new DataInputStream(new BufferedInputStream(s.getInputStream()));
            out = s.getOutputStream();
            gui(-40, new byte[0]);
            Object[] k = doc();
            xuLy((Integer) k[0], (byte[]) k[1]);
        }

        /** Doc het goi den trong ms mili giay. */
        void bom(long ms) {
            long het = System.currentTimeMillis() + ms;
            try {
                while (System.currentTimeMillis() < het) {
                    if (in.available() == 0) {
                        if (dut) return;
                        Thread.sleep(10);
                        // phat hien dut: available khong bao EOF -> kiem bang read voi timeout ngan
                        if (s.isClosed()) { dut = true; return; }
                        continue;
                    }
                    Object[] m = doc();
                    xuLy((Integer) m[0], (byte[]) m[1]);
                }
            } catch (EOFException | java.net.SocketException e) {
                dut = true;
            } catch (Exception e) {
                ghi(0, "loi doc: " + e);
                dut = true;
            }
        }

        /** Kiem ket noi con song: doc thu voi timeout 1 ms. */
        boolean conSong() {
            if (dut) return false;
            try {
                s.setSoTimeout(30);
                in.mark(1);
                int b = -2;
                try {
                    if (in.available() == 0) b = in.read();
                } catch (java.net.SocketTimeoutException t) {
                    b = -2;
                }
                if (b == -1) { dut = true; return false; }
                if (b >= 0) in.reset();
            } catch (IOException e) {
                dut = true;
                return false;
            } finally {
                try { s.setSoTimeout(0); } catch (Exception ignored) { }
            }
            return true;
        }

        /** Doc goi den khi dieu kien dung (tra true) hoac het gio. */
        boolean cho(Predicate<Bot> dk, long ms) {
            long het = System.currentTimeMillis() + ms;
            while (System.currentTimeMillis() < het) {
                if (dk.test(this)) return true;
                bom(50);
                if (dut) return dk.test(this);
            }
            return dk.test(this);
        }

        boolean choLenh(int tu, int cmd, long ms) {
            return cho(b -> b.coLenh(tu, cmd), ms);
        }

        int moc() {
            return su.size();
        }

        /** Thong bao (37/-26/27 cua minh/-32/-31/77/-35/-36) xuat hien sau moc, co chua mot trong cac chuoi. */
        String doiChu(int tu, long ms, String... chua) {
            final String[] kq = {null};
            cho(b -> {
                for (int i = tu; i < su.size(); i++) {
                    SuKien e = su.get(i);
                    if (e.chu == null) continue;
                    String t = khongDau(e.chu);
                    for (String c : chua) {
                        if (c.isEmpty() || t.contains(khongDau(c))) { kq[0] = e.chu; return true; }
                    }
                }
                return false;
            }, ms);
            return kq[0];
        }

        String chuTu(int tu) {
            StringBuilder sb = new StringBuilder();
            for (int i = tu; i < su.size(); i++) if (su.get(i).chu != null) sb.append('[').append(su.get(i).cmd).append("] ").append(su.get(i).chu.replace('\n', ' ')).append(" | ");
            return sb.length() > 600 ? sb.substring(0, 600) + "..." : sb.toString();
        }

        boolean coLenh(int tu, int cmd) {
            for (int i = tu; i < su.size(); i++) if (su.get(i).cmd == cmd) return true;
            return false;
        }

        SuKien layLenh(int tu, int cmd) {
            for (int i = tu; i < su.size(); i++) if (su.get(i).cmd == cmd) return su.get(i);
            return null;
        }

        void ghi(int cmd, String chu) {
            su.add(new SuKien(cmd, chu, null));
        }

        // ------------------------------------------------------- phan tich goi den
        void xuLy(int cmd, byte[] d) {
            demLenh.merge(cmd, 1, Integer::sum);
            DataInputStream r = new DataInputStream(new ByteArrayInputStream(d));
            SuKien e = new SuKien(cmd, null, d);
            try {
                switch (cmd) {
                    case -40: {
                        int n = r.readUnsignedByte();
                        key = new byte[n];
                        r.readFully(key);
                        for (int j = 0; j < n - 1; j++) key[j + 1] ^= key[j];
                        curR = curW = 0;
                        break;
                    }
                    case 2:
                        e.chu = "DANG NHAP LOI: " + r.readUTF();
                        break;
                    case 13: {
                        int n = r.readByte();
                        dsNv.clear();
                        dsTen.clear();
                        for (int i = 0; i < n; i++) {
                            int id = r.readInt();
                            String t = r.readUTF();
                            r.readByte();
                            int ni = r.readByte();
                            for (int j = 0; j < ni; j++) { r.readByte(); r.readByte(); }
                            int l = r.readShort();
                            int dung = r.readByte();
                            r.readByte();
                            r.readShort();
                            int c = r.readByte();
                            if (c != -1) {
                                int a = r.readShort(); r.skipBytes(a);
                                a = r.readShort(); r.skipBytes(a);
                                r.readByte(); r.readByte();
                            }
                            dsNv.add(new int[]{id, l, dung});
                            dsTen.add(t);
                        }
                        e.chu = "DS NHAN VAT " + dsTen;
                        break;
                    }
                    case 37:
                    case -26:
                    case 38:
                    case -60:
                        e.chu = r.readUTF();
                        break;
                    case 77:
                        e.chu = "NANG CAP: " + r.readUTF();
                        break;
                    case -104: { // ket qua dung vat pham (thong bao nhanh, ban >= 902)
                        int loai = r.readByte();
                        e.so = new int[]{loai};
                        e.chu = (loai == 1 ? "DUNG LOI: " : loai == 0 ? "DUNG OK: " : "DUNG TIN: ") + r.readUTF();
                        break;
                    }
                    case -35:
                        e.chu = "KHAM: " + r.readUTF();
                        break;
                    case 27: {
                        int id = r.readShort();
                        if (r.available() > 0) {
                            String t = r.readUTF();
                            e.chu = (id == actor ? "CHAT(minh): " : "CHAT(" + id + "): ") + t;
                        }
                        break;
                    }
                    case -125: { // 0.9.8 loai rieng tung quai: dex > 0 = Pokemon HOANG DA (bat duoc)
                        int n = r.readUnsignedByte();
                        for (int i = 0; i < n; i++) {
                            int id = r.readShort();
                            int dex = r.readShort();
                            r.readUnsignedByte();
                            int bac = r.readUnsignedByte();
                            if (dex > 0) hoangDa.put(id, new int[]{dex, bac});
                            else hoangDa.remove(id);
                        }
                        break;
                    }
                    case -124: { // 0.9.8 kenh chat (client >= 908): tin = byte 1
                        int k = r.readByte();
                        if (k == 1) {
                            int kenh = r.readByte();
                            r.readByte();
                            r.readLong();
                            r.readShort();
                            String gui = r.readUTF();
                            String doi = r.readUTF();
                            String nd = r.readUTF();
                            e.chu = (kenh == 4 ? "TIN RIENG tu " + gui + " (" + doi + "): " : "KENH" + kenh + " " + gui + ": ") + nd;
                        } else if (k == 4) {
                            int kenh = r.readByte();
                            int ma = r.readByte();
                            r.readShort();
                            e.chu = "KENH KET QUA(" + kenh + "/" + ma + "): " + r.readUTF();
                        }
                        break;
                    }
                    case -5: {
                        int a = r.readByte();
                        if (a == 1 && d.length > 3) {
                            e.chu = "TIN RIENG tu " + r.readUTF() + ": " + r.readUTF();
                        } else {
                            StringBuilder sb = new StringBuilder("HOP THU:");
                            for (int i = 0; i < a; i++) sb.append(' ').append(r.readUTF()).append(": ").append(r.readUTF());
                            e.chu = sb.toString();
                        }
                        break;
                    }
                    case -18:
                        e.chu = "CHAT BANG: " + r.readUTF();
                        break;
                    case -30: {
                        int npc = r.readShort();
                        int menu = r.readUnsignedByte();
                        int n = r.readUnsignedByte();
                        List<String> ds = new ArrayList<>();
                        for (int i = 0; i < n; i++) ds.add(r.readUTF());
                        menuCuoi = new int[]{npc, menu};
                        menuChu = ds;
                        e.so = new int[]{npc, menu};
                        e.chu = "MENU(" + npc + "/" + menu + "): " + String.join(" ; ", ds);
                        break;
                    }
                    case -31: {
                        int npc = r.readShort();
                        int menu = r.readByte();
                        e.so = new int[]{npc, menu};
                        e.chu = "NHAP(" + npc + "/" + menu + "): " + r.readUTF();
                        break;
                    }
                    case -32: {
                        int npc = r.readShort();
                        int pop = r.readByte();
                        e.so = new int[]{npc, pop};
                        e.chu = "POPUP(" + npc + "/" + pop + "): " + r.readUTF();
                        break;
                    }
                    case 3: {
                        actor = r.readShort();
                        ten = r.readUTF();
                        hp = r.readInt();
                        maxhp = r.readInt();
                        mp = r.readInt(); maxmp = r.readInt();
                        r.readByte(); r.readByte();
                        tanCong = r.readInt(); thuVl = r.readInt(); thuPt = r.readInt();
                        r.readShort(); r.readShort(); r.readShort();
                        r.readByte();
                        lv = r.readUnsignedByte();
                        pt = r.readShort();
                        for (int i = 0; i < 7; i++) r.readShort();
                        r.readInt(); r.readShort();
                        nSkill = r.readByte();
                        chieu = new int[nSkill];
                        for (int i = 0; i < nSkill; i++) chieu[i] = r.readByte();
                        e.chu = null;
                        break;
                    }
                    case 12: {
                        if (mapId >= 0) mapCu = mapId;
                        mapId = r.readShort();
                        tx = r.readShort();
                        ty = r.readShort();
                        px = tx * 16 + 8;
                        py = ty * 16 + 8;
                        r.readByte();
                        mapLoad = r.readShort();
                        tenMap = r.readUTF();
                        coBanDo = true;
                        quai.clear();
                        nguoi.clear();
                        e.so = new int[]{mapId, tx, ty, mapLoad};
                        e.chu = "VAO MAP " + mapId + " (" + tenMap + ") o " + tx + "," + ty;
                        break;
                    }
                    case 16: {
                        int t = r.readByte();
                        if (t == 0) {
                            xu = r.readLong();
                            int n = r.readUnsignedByte();
                            thuoc.clear();
                            for (int i = 0; i < n; i++) thuoc.put(r.readUnsignedByte(), r.readInt());
                        } else if (t == 1) {
                            int n = r.readShort();
                            doDung.clear();
                            for (int i = 0; i < n; i++) {
                                r.readByte();
                                int id = r.readShort(), tpl = r.readShort(), plus = r.readByte(), lev = r.readByte();
                                r.readShort(); r.readShort(); r.readByte(); r.readByte(); r.readByte();
                                int color = r.readByte();
                                r.readByte(); r.readByte(); r.readByte(); r.readByte();
                                int lock = r.readByte();
                                r.readUTF();
                                r.readShort();
                                int na = r.readByte();
                                for (int k = 0; k < na; k++) { r.readByte(); r.readShort(); }
                                r.readByte();
                                doDung.add(new int[]{id, tpl, plus, lev, lock, color});
                            }
                        }
                        if (t == 2) {
                            luong = r.readInt();
                            thu.clear();
                            vatNuoi.clear();
                            int na = r.readByte();
                            for (int i = 0; i < na; i++) {
                                int id = r.readShort(), img = r.readByte();
                                r.readByte();
                                int lvt = r.readByte();
                                String tn = r.readUTF();
                                r.readUTF();
                                r.readByte();
                                thu.add(new int[]{id, img, lvt});
                                tenThu.put(id, tn);
                            }
                            int np = r.readByte();
                            for (int i = 0; i < np; i++) {
                                int id = r.readShort(), img = r.readByte();
                                r.readByte();
                                String tn = r.readUTF();
                                r.readByte();
                                r.readUTF();
                                r.readInt();
                                r.readByte();
                                vatNuoi.add(new int[]{id, img});
                                tenThu.put(-id - 1, tn);
                            }
                            lk = r.readInt();
                        }
                        if (t == 0 || t == 1) {
                            luong = r.readInt();
                            if (r.available() >= 4) {
                                // type 2/3 khong co o day; lk o cuoi
                                byte[] con = new byte[r.available()];
                                r.readFully(con);
                                if (con.length >= 4) {
                                    int n = con.length;
                                    lk = ((con[n - 4] & 255) << 24) | ((con[n - 3] & 255) << 16) | ((con[n - 2] & 255) << 8) | (con[n - 1] & 255);
                                }
                            }
                        }
                        break;
                    }
                    case 73: {
                        int n = r.readShort();
                        List<int[]> l = new ArrayList<>();
                        for (int i = 0; i < n; i++) l.add(new int[]{r.readShort(), r.readShort(), r.readShort()});
                        int khoa = r.available() > 0 ? r.readByte() : 0;
                        ngoc.values().removeIf(v -> v[2] == khoa);
                        for (int[] g : l) ngoc.put(g[0] * 2 + khoa, new int[]{g[1], g[2], khoa, g[0]});
                        break;
                    }
                    case 4: {
                        while (r.available() >= 8) {
                            int cat = r.readByte();
                            int type = r.readUnsignedByte();
                            int id = r.readShort();
                            int x = r.readShort(), y = r.readShort();
                            if (r.available() > 0) r.readByte();
                            if (cat == 1) {
                                int[] q = quai.get(id);
                                if (q == null) quai.put(id, new int[]{x, y, -1, -1, -1, type});
                                else { q[0] = x; q[1] = y; }
                            } else if (cat == 0) {
                                if (id == actor) { px = x; py = y; e.so = new int[]{x, y}; e.chu = "VI TRI MINH " + x + "," + y; }
                                else nguoi.put(id, new int[]{x, y});
                            }
                            break; // goi thuong chi 1 actor; phan con lai la truong phu
                        }
                        break;
                    }
                    case 7: {
                        int id = r.readShort();
                        int tp = r.readUnsignedByte();
                        int x = r.readShort(), y = r.readShort();
                        int h = r.readInt();
                        int l = r.readByte();
                        r.readByte();
                        int mh = r.readInt();
                        quai.put(id, new int[]{x, y, h, l, mh, tp});
                        break;
                    }
                    case 105: {
                        int nClone = r.readByte();
                        int nMap = r.readByte();
                        viTriMap = new short[nMap][][];
                        for (int i = 0; i < nMap; i++) {
                            int nm = r.readByte();
                            viTriMap[i] = new short[nm][];
                            for (int j = 0; j < nm; j++) {
                                viTriMap[i][j] = new short[nClone * 3];
                                for (int k = 0; k < nClone * 3; k++) {
                                    viTriMap[i][j][k] = r.readShort();
                                    if (k % 3 == 0) r.readUTF();
                                }
                            }
                        }
                        int n2 = r.readByte();
                        maIdNhom = new short[n2][nClone];
                        idArr = new short[n2];
                        for (int i = 0; i < n2; i++) {
                            for (int j = 0; j < nClone; j++) maIdNhom[i][j] = r.readShort();
                            idArr[i] = r.readShort();
                        }
                        break;
                    }
                    case -92:
                        e.chu = "TIEN HOA";
                        break;
                    case 10: { // quai danh: {quai, muc tieu, sat thuong, hp con} - HP minh song khong can hoi lai (lenh 88)
                        r.readShort();
                        int tg = r.readShort();
                        r.readInt();
                        int con = r.readInt();
                        if (tg == actor && actor >= 0) hp = Math.max(0, con);
                        break;
                    }
                    case 15: {
                        int t = r.readByte();
                        int id = r.readShort();
                        if (t == 0 && id == actor) {
                            int n = r.readByte();
                            soMac = n;
                            dangMac.clear();
                            dangMacTpl.clear();
                            dangMacPlus.clear();
                            // MessageCreator.createCharWearingMessage: clazz, id, tpl, plus, level, mDur, dur, mau, pos, he, beKick,
                            // hang, mp, khoa, UTF niem, phut thue, nAtb x (id byte, gia tri short). Doc het (30/10/2026: bot can id +
                            // +N cua vu khi dang cam de nang cap); goi lech phien ban -> giu phan da doc.
                            for (int i = 0; i < n; i++) {
                                r.readByte();
                                int idm = r.readShort();
                                int tpl = r.readShort();
                                int plus = r.readByte();
                                dangMac.add(idm);
                                dangMacTpl.add(tpl);
                                dangMacPlus.add(plus);
                                r.readByte(); r.readShort(); r.readShort();
                                for (int k = 0; k < 7; k++) r.readByte();
                                r.readUTF(); r.readShort();
                                int na = r.readByte();
                                for (int k = 0; k < na; k++) { r.readByte(); r.readShort(); }
                            }
                            e.so = new int[]{n};
                        }
                        break;
                    }
                    case 23:
                        e.chu = "SHOP NPC (" + d.length + " byte)";
                        break;
                    case 66: {
                        e.so = new int[]{r.readByte()};
                        e.chu = "GIAO DICH " + e.so[0];
                        break;
                    }
                    default:
                }
            } catch (IOException ex) {
                // goi ngan hon du doan: giu phan da doc
            }
            su.add(e);
            if (ngheNhan != null) {
                try { ngheNhan.accept(e); } catch (RuntimeException ex) { System.out.println("[NGHE] " + ex); }
            }
        }

        // ------------------------------------------------------- hanh dong co ban
        /** Dang nhap; tra ve null neu ok, chuoi loi neu khong. */
        String dangNhap(String mk) throws IOException {
            ketNoi();
            int m = moc();
            // -Dban=N: gui them so ban client sau ma may nhu client that (AutoGuard) - ban >= 902 nhan thong bao nhanh -104
            if (BAN >= 0) gui(1, user, mk, "2.7.6", "nokia/1/1", "0", "0", "0", (byte) 4, (short) 800, (byte) 0, (byte) 0, "botcn-" + user, BAN);
            else gui(1, user, mk, "2.7.6", "nokia/1/1", "0", "0", "0", (byte) 4, (short) 800, (byte) 0, (byte) 0, "botcn-" + user);
            cho(b -> b.coLenh(m, 13) || b.coLenh(m, 2) || b.dut, 8000);
            if (coLenh(m, 13)) return null;
            String t = chuTu(m);
            return t.isEmpty() ? (dut ? "server dong ket noi" : "khong tra loi") : t;
        }

        /** Tao nhan vat (neu chua co) va vao game. */
        String vaoGame(int lop) throws IOException {
            return vaoGame(lop, 0);
        }

        /** Vao game; chua co nhan vat thi tao voi vung {@code vung} (0 Kanto, 1 Johto - client CreateCharScr "Vùng"). */
        String vaoGame(int lop, int vung) throws IOException {
            if (dsNv.stream().noneMatch(v -> v[2] == 1)) {
                int m = moc();
                gui(14, user, (byte) lop, (byte) 0, (byte) 0, (byte) vung, (byte) 0); // ten, lop, kieu dau, gioi, vung, may chu
                cho(b -> b.coLenh(m, 13) && b.dsNv.stream().anyMatch(v -> v[2] == 1) || b.doiChu(m, 0, "") != null, 6000);
                if (dsNv.stream().noneMatch(v -> v[2] == 1)) return "tao nhan vat that bai: " + chuTu(m) + " lenh=" + demLenh + " ds=" + dsTen + " " + (dsNv.isEmpty() ? "" : Arrays.toString(dsNv.get(0)));
            }
            int[] nv = dsNv.stream().filter(v -> v[2] == 1).findFirst().get();
            charDb = nv[0];
            int m = moc();
            gui(13, (byte) 0, charDb);
            cho(b -> b.coLenh(m, 12) && b.actor >= 0, 10000);
            if (!coLenh(m, 12)) return "chon nhan vat: khong nhan map. " + chuTu(m);
            gui(12, (short) -500, (short) 0, (short) 0); // client bao vao map xong
            bom(1500);
            return null;
        }

        void chat(String t) throws IOException {
            gui(27, t);
        }

        /** Lenh GM qua chat, doi phan hoi. */
        void gm(String t, long ms) throws IOException {
            chat(t);
            bom(ms);
        }

        /** Den map/o bang GM mto, tra ve true neu da vao dung map. */
        boolean den(int map, int x, int y) throws IOException {
            int m = moc();
            gui(27, "mto " + map + " " + x + " " + y);
            boolean ok = cho(b -> {
                SuKien k = b.layLenh(m, 12);
                return k != null;
            }, 6000);
            if (ok) {
                gui(12, (short) -500, (short) 0, (short) 0);
                bom(800);
            }
            return ok && mapId == map;
        }

        /** Di bo theo duong ngan nhat (BFS 8 huong tren o di duoc) toi o (tx,ty); tra ve true neu toi noi. */
        boolean diDuong(BanDo bd, int dx, int dy) throws IOException {
            return diDuong(bd, dx, dy, 3);
        }

        /**
         * Nhu tren; {@code lanThu} = so lan tinh lai duong khi may chu tra vi tri cua minh lech (buoc bi tu choi / bi day):
         * di tiep tu vi tri THAT thay vi tu vi tri bot tuong (lech vi tri -> xin qua cong o sai cho -> may chu kick
         * "doi map khong dung o", hoi quy 25/10).
         */
        boolean diDuong(BanDo bd, int dx, int dy, int lanThu) throws IOException {
            int sx = px / 16, sy = py / 16;
            if (sx == dx && sy == dy) return true;
            int[] truoc = new int[bd.w * bd.h];
            Arrays.fill(truoc, -1);
            ArrayDeque<Integer> q = new ArrayDeque<>();
            int bdau = sy * bd.w + sx, dich = dy * bd.w + dx;
            if (bdau < 0 || bdau >= truoc.length) return false;
            truoc[bdau] = bdau;
            q.add(bdau);
            int[][] hg = {{1, 0}, {-1, 0}, {0, 1}, {0, -1}};
            while (!q.isEmpty() && truoc[dich] < 0) {
                int c = q.poll(), cx = c % bd.w, cy = c / bd.w;
                for (int[] h : hg) {
                    int nx = cx + h[0], ny = cy + h[1];
                    if (nx < 0 || ny < 0 || nx >= bd.w || ny >= bd.h) continue;
                    int n = ny * bd.w + nx;
                    if (truoc[n] >= 0 || !bd.diDuoc(nx, ny) || (n != dich && bd.laCong(nx, ny))) continue;
                    truoc[n] = c;
                    q.add(n);
                }
            }
            if (truoc[dich] < 0) return false;
            List<Integer> duong = new ArrayList<>();
            for (int c = dich; c != bdau; c = truoc[c]) duong.add(c);
            Collections.reverse(duong);
            int m = moc();
            for (int c : duong) {
                int mb = moc();
                int gx = (c % bd.w) * 16 + 8, gy = (c / bd.w) * 16 + 8;
                diToi(gx, gy);
                bom(210);
                if (dut) return false;
                for (int i = mb; i < su.size(); i++) {
                    SuKien k = su.get(i);
                    if (k.cmd == 4 && k.chu != null && k.so != null && (Math.abs(k.so[0] - gx) > 16 || Math.abs(k.so[1] - gy) > 16)) {
                        px = k.so[0];
                        py = k.so[1];
                        return lanThu > 0 && diDuong(bd, dx, dy, lanThu - 1);
                    }
                }
            }
            bom(300);
            for (int i = m; i < su.size(); i++) if (su.get(i).cmd == 4 && su.get(i).chu != null) {
                SuKien k = su.get(i);
                if (k.so[0] / 16 != dx || k.so[1] / 16 != dy) { px = k.so[0]; py = k.so[1]; }
            }
            return px / 16 == dx && py / 16 == dy;
        }

        void diToi(int x, int y) throws IOException {
            gui(4, (short) x, (short) y);
            px = x;
            py = y;
        }

        /** Chon menu: npc, menu, lua chon. */
        void menu(int npc, int idMenu, int chon) throws IOException {
            gui(-30, (short) npc, (byte) idMenu, (byte) chon);
        }

        /** Tim lua chon co chua chuoi trong menu cuoi, -1 neu khong co. */
        int timMuc(String chua) {
            for (int i = 0; i < menuChu.size(); i++) if (khongDau(menuChu.get(i)).contains(khongDau(chua))) return i;
            return -1;
        }

        /** Mo menu -> doi menu moi (npc,menu) -> tra ve so muc, -1 neu khong co menu. */
        int doiMenu(int tu, int npc, int idMenu, long ms) {
            cho(b -> {
                for (int i = tu; i < b.su.size(); i++) {
                    SuKien e = b.su.get(i);
                    if (e.cmd == -30 && e.so != null && e.so[0] == npc && (idMenu < 0 || e.so[1] == idMenu)) return true;
                }
                return false;
            }, ms);
            for (int i = su.size() - 1; i >= tu; i--) {
                SuKien e = su.get(i);
                if (e.cmd == -30 && e.so != null && e.so[0] == npc && (idMenu < 0 || e.so[1] == idMenu)) return menuChu.size();
            }
            return -1;
        }

        void lamMoi() throws IOException {
            gui(88, new byte[0]);
            bom(700);
        }

        int soThuoc(int id) {
            return thuoc.getOrDefault(id, 0);
        }

        int soNgoc(int tpl) {
            int n = 0;
            for (int[] g : ngoc.values()) if (g[0] == tpl) n += g[1];
            return n;
        }

        int[] timDo(Predicate<int[]> p) {
            for (int[] d : doDung) if (p.test(d)) return d;
            return null;
        }

        @Override
        public void close() {
            try { if (s != null) s.close(); } catch (Exception ignored) { }
        }
    }

    static class SuKien {
        final int cmd;
        String chu;
        int[] so;
        final byte[] d;
        final long luc = System.currentTimeMillis();

        SuKien(int cmd, String chu, byte[] d) {
            this.cmd = cmd;
            this.chu = chu;
            this.d = d;
        }
    }

    // =========================================================== tien ich
    static byte[] goi(Object... fs) throws IOException {
        ByteArrayOutputStream bo = new ByteArrayOutputStream();
        DataOutputStream o = new DataOutputStream(bo);
        for (Object f : fs) {
            if (f instanceof String) o.writeUTF((String) f);
            else if (f instanceof Byte) o.writeByte((Byte) f);
            else if (f instanceof Short) o.writeShort((Short) f);
            else if (f instanceof Integer) o.writeInt((Integer) f);
            else if (f instanceof Long) o.writeLong((Long) f);
            else if (f instanceof Boolean) o.writeBoolean((Boolean) f);
            else if (f instanceof byte[]) o.write((byte[]) f);
        }
        return bo.toByteArray();
    }

    static String khongDau(String s) {
        String n = java.text.Normalizer.normalize(s, java.text.Normalizer.Form.NFD).replaceAll("\\p{M}", "");
        return n.replace('đ', 'd').replace('Đ', 'D').toLowerCase();
    }

    /**
     * CSDL tai khoan (mac dinh "account"). Staging (vps/staging/) dung ten khac: -Ddbtk=htbb_staging_account -> moi
     * "account." trong cau truy van cua kich ban duoc doi sang ten nay (khong bao gio cham CSDL account that).
     */
    static final String DB_TK = System.getProperty("dbtk", "account");
    /** -Ddbcnf=<tep>: mysql --defaults-file (host/port/user/password) thay cho -Ddbport + htbb/htbb cua may thu. */
    static final String DB_CNF = System.getProperty("dbcnf", "");

    /**
     * Chay truy van tren CSDL, tra ve cac dong (cot cach nhau tab, NULL = "NULL", dinh dang nhu {@code mysql -N -B}).
     * Rong neu khong cau hinh -Ddb.
     *
     * <p>Cong kiem 25/10/2026: staging va production dung CHUNG mot MariaDB (max_connections 300). Ban cu mo mot tien
     * trinh mysql CLI (= mot ket noi moi) cho MOI truy van, va khi CSDL het ket noi thi tra dong "ERROR 1040 ... Too many
     * connections" nhu DU LIEU (ket_tau: ArrayIndexOutOfBounds khi tach pInfo; pho_ban_cup: so sanh voi chu loi; cac lenh
     * UPDATE dung canh hong IM LANG). Nay:
     * <ul>
     *   <li>co JDBC (mysql-connector tren classpath - chay_hoi_quy.sh them server/lib): MOT ket noi dung chung cho ca JVM
     *       (dong bo), mo lai khi rot; khong co thi van dung mysql CLI nhu cu (-Ddbjdbc=0 ep dung CLI);</li>
     *   <li>loi TAM THOI (het ket noi 1040 / vuot max_user_connections 1203,1226 / mat ket noi 2002,2003,2006,2013 /
     *       khoa 1205,1213): thu lai toi 8 lan, cho tang dan;</li>
     *   <li>loi con lai / het luot: in "[SQL] LOI ..." va tra MOT dong "LOI SQL ..." (khong con tra chu loi tran nhu du lieu
     *       hop le).</li>
     * </ul>
     */
    static List<String> sql(String q) {
        List<String> kq = new ArrayList<>();
        if (DB.isEmpty()) return kq;
        if (!"account".equals(DB_TK)) q = q.replaceAll("(?<![A-Za-z0-9_`])`?account`?\\.", DB_TK + ".");
        String loi = null;
        for (int lan = 0; lan < 8; lan++) {
            kq.clear();
            loi = coJdbc() ? sqlJdbc(q, kq) : sqlCli(q, kq);
            if (loi == null) return kq;
            if (!loiTamThoi(loi)) break;
            try { Thread.sleep(1000L * (lan + 1)); } catch (InterruptedException e) { Thread.currentThread().interrupt(); break; }
        }
        String ngan = q.length() > 160 ? q.substring(0, 160) + "..." : q;
        System.out.println("[SQL] LOI: " + loi + " <- " + ngan.replace('\n', ' '));
        kq.clear();
        kq.add("LOI SQL " + loi);
        return kq;
    }

    /** Ma loi MariaDB/MySQL tam thoi (het ket noi, mat ket noi, khoa) - dang "ERROR nnnn ...". */
    static boolean loiTamThoi(String loi) {
        java.util.regex.Matcher m = java.util.regex.Pattern.compile("ERROR (\\d+)").matcher(loi);
        if (!m.find()) return loi.contains("Too many connections") || loi.contains("Communications link failure");
        switch (Integer.parseInt(m.group(1))) {
            case 1040: case 1203: case 1226: case 2002: case 2003: case 2006: case 2013: case 1205: case 1213: case 0:
                return true;
            default:
                return false;
        }
    }

    private static final Object KHOA_SQL = new Object();
    private static java.sql.Connection ketNoiSql;
    private static Boolean coJdbc;

    static boolean coJdbc() {
        if (coJdbc == null) {
            boolean co;
            try {
                Class.forName("com.mysql.jdbc.Driver");
                co = !"0".equals(System.getProperty("dbjdbc", "1"));
            } catch (Throwable t) {
                co = false;
            }
            coJdbc = co;
        }
        return coJdbc;
    }

    /** {host, port, user, password} tu -Ddbcnf (muc [client]) hoac -Ddbhost/-Ddbport/-Ddbuser/-Ddbpass. */
    static String[] thongSoCsdl() {
        String[] t = {System.getProperty("dbhost", "127.0.0.1"), System.getProperty("dbport", "3318"),
                System.getProperty("dbuser", "htbb"), System.getProperty("dbpass", "htbb")};
        if (DB_CNF.isEmpty()) return t;
        try {
            String muc = "";
            for (String d : java.nio.file.Files.readAllLines(java.nio.file.Paths.get(DB_CNF), StandardCharsets.UTF_8)) {
                d = d.trim();
                if (d.startsWith("[")) { muc = d; continue; }
                if (!("[client]".equals(muc) || "[mysql]".equals(muc)) || !d.contains("=")) continue;
                String k = d.substring(0, d.indexOf('=')).trim(), v = d.substring(d.indexOf('=') + 1).trim();
                if (v.length() >= 2 && (v.startsWith("\"") && v.endsWith("\"") || v.startsWith("'") && v.endsWith("'"))) v = v.substring(1, v.length() - 1);
                switch (k) {
                    case "host": t[0] = v; break;
                    case "port": t[1] = v; break;
                    case "user": t[2] = v; break;
                    case "password": t[3] = v; break;
                    default: break;
                }
            }
        } catch (IOException e) {
            System.out.println("[SQL] khong doc duoc " + DB_CNF + ": " + e);
        }
        return t;
    }

    /** Ky tu dac biet nhu mysql -B (\\, \n, \t, \0 viet thanh hai ky tu). */
    static String thoatB(String v) {
        if (v.indexOf('\\') < 0 && v.indexOf('\n') < 0 && v.indexOf('\t') < 0 && v.indexOf('\0') < 0) return v;
        StringBuilder b = new StringBuilder(v.length() + 8);
        for (char c : v.toCharArray()) {
            switch (c) {
                case '\\': b.append("\\\\"); break;
                case '\n': b.append("\\n"); break;
                case '\t': b.append("\\t"); break;
                case '\0': b.append("\\0"); break;
                default: b.append(c);
            }
        }
        return b.toString();
    }

    /** Mot ket noi JDBC dung chung; null = xong, khac null = chu loi "ERROR nnnn (state): ...". */
    static String sqlJdbc(String q, List<String> kq) {
        synchronized (KHOA_SQL) {
            try {
                if (ketNoiSql == null || ketNoiSql.isClosed() || !ketNoiSql.isValid(3)) {
                    dongSql();
                    String[] t = thongSoCsdl();
                    // jdbcCompliantTruncation=false: Connector/J 5.1 KHONG tu them STRICT_TRANS_TABLES vao sql_mode (giu y
                    // nhu mysql CLI); allowMultiQueries: "a; b" trong mot lan goi nhu mysql -e
                    String url = "jdbc:mysql://" + t[0] + ":" + t[1] + "/" + DB + "?useUnicode=true&characterEncoding=UTF-8"
                            + "&useSSL=false&allowMultiQueries=true&jdbcCompliantTruncation=false&connectTimeout=10000";
                    ketNoiSql = java.sql.DriverManager.getConnection(url, t[2], t[3]);
                    try (java.sql.Statement st = ketNoiSql.createStatement()) { st.execute("SET NAMES utf8mb4"); }
                }
                try (java.sql.Statement st = ketNoiSql.createStatement()) {
                    boolean coKq = st.execute(q);
                    while (true) {
                        if (coKq) {
                            try (java.sql.ResultSet rs = st.getResultSet()) {
                                int n = rs.getMetaData().getColumnCount();
                                while (rs.next()) {
                                    StringBuilder d = new StringBuilder();
                                    for (int i = 1; i <= n; i++) {
                                        if (i > 1) d.append('\t');
                                        // getBytes = chu tho cua giao thuc van ban (nhu mysql CLI): getString them ".0" vao
                                        // DATETIME va nem loi voi '0000-00-00 00:00:00'
                                        byte[] v = rs.getBytes(i);
                                        d.append(v == null ? "NULL" : thoatB(new String(v, StandardCharsets.UTF_8)));
                                    }
                                    kq.add(d.toString());
                                }
                            }
                        } else if (st.getUpdateCount() == -1) {
                            break;
                        }
                        coKq = st.getMoreResults();
                    }
                }
                return null;
            } catch (java.sql.SQLException e) {
                String st = String.valueOf(e.getSQLState());
                if (st.startsWith("08") || e.getErrorCode() == 0 || e.getErrorCode() >= 2000) dongSql();
                return "ERROR " + e.getErrorCode() + " (" + st + "): " + e.getMessage();
            }
        }
    }

    static void dongSql() {
        synchronized (KHOA_SQL) {
            if (ketNoiSql != null) {
                try { ketNoiSql.close(); } catch (Exception ignored) { }
                ketNoiSql = null;
            }
        }
    }

    /** mysql CLI (khong co JDBC): mot tien trinh / truy van; null = xong, khac null = dong "ERROR ..." cua mysql. */
    static String sqlCli(String q, List<String> kq) {
        try {
            List<String> lenh = new ArrayList<>();
            lenh.add(System.getProperty("mysql", "mysql"));
            if (!DB_CNF.isEmpty()) {
                lenh.add("--defaults-file=" + DB_CNF);
            } else {
                lenh.addAll(Arrays.asList("--no-defaults", "-h" + System.getProperty("dbhost", "127.0.0.1"), "-P" + System.getProperty("dbport", "3318"),
                        "-u" + System.getProperty("dbuser", "htbb"), "-p" + System.getProperty("dbpass", "htbb"), "--skip-ssl"));
            }
            lenh.addAll(Arrays.asList("--default-character-set=utf8mb4", "-N", "-B", DB, "-e", q));
            Process p = new ProcessBuilder(lenh).redirectErrorStream(true).start();
            BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream(), StandardCharsets.UTF_8));
            String l, loi = null;
            while ((l = br.readLine()) != null) {
                if (l.startsWith("mysql: [Warning]")) continue;
                if (loi == null && l.matches("^ERROR \\d+ \\(.*")) loi = l;
                kq.add(l);
            }
            int ma = p.waitFor();
            if (loi == null && ma != 0) loi = "ERROR 0 (mysql thoat " + ma + "): " + String.join(" | ", kq);
            return loi;
        } catch (Exception e) {
            return "ERROR 0 (" + e + ")";
        }
    }

    static String sql1(String q) {
        List<String> l = sql(q);
        return l.isEmpty() ? null : l.get(0);
    }

    static void ket(String ten, Boolean ok, String bang) {
        String nhan = ok == null ? "BO QUA" : ok ? "PASS" : "FAIL";
        if (ok == null) nBo++;
        else if (ok) nPass++;
        else nFail++;
        String dong = String.format("%-6s %-28s %s", nhan, ten, bang == null ? "" : bang.replace('\n', ' '));
        System.out.println(dong);
        System.out.flush();
        synchronized (KET_QUA) {
            KET_QUA.add(dong);
        }
    }

    static int soThuong = Integer.getInteger("tuThuong", 101);
    static final int THUONG_CUOI = Integer.getInteger("denThuong", 120);

    /** Tai khoan KHONG GM ke tiep (mac dinh kt101..kt120; admin bo qua mot so kiem tra). */
    static synchronized String tkThuong() {
        if (soThuong > THUONG_CUOI) soThuong = Integer.getInteger("tuThuong", 101);
        return String.format("%s%03d", TIEN_TO, soThuong++);
    }

    /**
     * -DnvMoi (mac dinh 1 khi co -Ddb): tai khoan bot dung lai (kt001..040 / kt101..199) vao game bang NHAN VAT MOI - xoa
     * nhan vat cu truoc khi dang nhap. Cong kiem chay nhieu lan / ngay: nhan vat cu day tui, het luot tuan, chieu toi da,
     * danh hieu / tien do nhiem vu cu -> FAIL gia (hoi quy 25/10). Kich ban can giu nhan vat thi dang nhap lai bang new Bot(user).
     */
    static final boolean NV_MOI = !"0".equals(System.getProperty("nvMoi", "1"));

    static void xoaNhanVatCu(String user) {
        if (DB.isEmpty() || !NV_MOI) return;
        sql("DELETE c FROM tob_char c JOIN account.team_user u ON u.id = c.userId WHERE u.username = '" + user.replace("'", "") + "'");
    }

    static Bot botThuong() throws Exception {
        Bot b = new Bot(tkThuong());
        xoaNhanVatCu(b.user);
        String l = b.dangNhap("123456");
        if (l != null) throw new IllegalStateException(b.user + " dang nhap: " + l);
        l = b.vaoGame(0);
        if (l != null) throw new IllegalStateException(b.user + " " + l);
        duTuoiChongClone(b);
        return b;
    }

    /**
     * Tai khoan thu vua tao nhan vat: chong clone (com.htbb.pokemon.ChongCloneLogic) chan moi giao dich / ky gui khi
     * tai khoan chua du chong_clone.tuoi_tai_khoan_gio (24 gio) hoac nhan vat chua choi du
     * chong_clone.gio_choi_toi_thieu (10 gio). Day la DUNG thiet ke; bot khong ha cong ma lui ngay dang ky va cong
     * gio choi tren CSDL thu (server cong don totalTimePlay=totalTimePlay+?, doc lai sau toi da 5 phut - lam ngay khi
     * vao game, truoc lan doc dau tien). Khong cau hinh -Ddb (server that) thi khong lam gi.
     */
    static void duTuoiChongClone(Bot b) {
        if (DB.isEmpty() || b.charDb <= 0) return;
        sql("UPDATE tob_char SET totalTimePlay = GREATEST(totalTimePlay, 900) WHERE id = " + b.charDb);
        sql("UPDATE account.team_user SET regdate = LEAST(regdate, DATE_SUB(NOW(), INTERVAL 30 DAY)) WHERE username = '" + b.user.replace("'", "") + "'");
    }

    /** Bot moi da vao game (tai khoan rieng). lop -1 = mac dinh 0. */
    static Bot botMoi(int lop) throws Exception {
        Bot b = new Bot(tkMoi());
        xoaNhanVatCu(b.user);
        String l = b.dangNhap("123456");
        if (l != null && l.contains("khong tra loi")) { // phien cu cua tai khoan dung lai vua bi da: thu lai
            b.close();
            Thread.sleep(2000);
            b = new Bot(b.user);
            l = b.dangNhap("123456");
        }
        if (l != null) throw new IllegalStateException(b.user + " dang nhap: " + l);
        l = b.vaoGame(lop < 0 ? 0 : lop);
        if (l != null) throw new IllegalStateException(b.user + " " + l);
        duTuoiChongClone(b);
        return b;
    }

    // =========================================================== ban do (doc tep tren dia)
    static class BanDo {
        int w, h;
        int[] type;
        boolean offline;
        List<int[]> cong = new ArrayList<>(); // {x,y,toM,xOut,yOut}

        static final Map<Integer, BanDo> CACHE = new HashMap<>();

        static synchronized BanDo nap(int load) {
            if (CACHE.containsKey(load)) return CACHE.get(load);
            BanDo m = null;
            try {
                File f = new File(NOI, "server/cMap/" + load);
                if (!f.isFile()) f = new File(NOI, "server/cMap/" + load);
                String bo = load < 110 ? "t" : (load > 200 ? "t_thanh" : "t_hang");
                File ft = new File(NOI, "server/cMap/" + bo + ".type");
                if (!ft.isFile()) ft = new File(NOI, "server/cMap/" + bo + ".type");
                byte[] tt = java.nio.file.Files.readAllBytes(ft.toPath());
                byte[] b = java.nio.file.Files.readAllBytes(f.toPath());
                m = new BanDo();
                int p = 0;
                m.w = b[p++] & 255;
                m.h = b[p++] & 255;
                m.type = new int[m.w * m.h];
                for (int i = 0; i < m.w * m.h; i++) {
                    int t = b[p++] & 255;
                    if (t != 254 && t != 255 && t < tt.length) m.type[i] = tt[t] & 255;
                }
                while ((b[p] & 255) != 255) { // khoi tileTop
                    int ww = b[p + 2] & 255, hh = b[p + 3] & 255;
                    p += 4 + ww * hh;
                }
                p++;
                while ((b[p] & 255) != 255) p += 3;
                p++;
                while ((b[p] & 255) != 255 && (b[p] & 255) != 254) p += 3;
                p++;
                m.offline = b[p++] == 1;
                int n = b[p++] & 255;
                for (int i = 0; i < n; i++) {
                    int x = b[p] & 255, y = b[p + 1] & 255;
                    int toM = (b[p + 2] & 255) | ((b[p + 3] & 255) << 8);
                    int xo = b[p + 4] & 255, yo = b[p + 5] & 255;
                    p += 6;
                    m.cong.add(new int[]{x, y, (short) toM, xo, yo});
                    m.type[y * m.w + x] = 2000000000 + i;
                }
            } catch (Exception e) {
                m = null;
            }
            CACHE.put(load, m);
            return m;
        }

        boolean diDuoc(int x, int y) {
            if (x < 0 || y < 0 || x >= w || y >= h) return false;
            return (type[y * w + x] & 2) != 2;
        }

        /** So o di duoc lien thong (4 huong) tu (x,y), dung som khi vuot gioiHan. */
        int vungLienThong(int x, int y, int gioiHan) {
            if (!diDuoc(x, y)) return 0;
            boolean[] tham = new boolean[w * h];
            ArrayDeque<int[]> q = new ArrayDeque<>();
            q.add(new int[]{x, y});
            tham[y * w + x] = true;
            int n = 0;
            while (!q.isEmpty() && n < gioiHan) {
                int[] c = q.poll();
                n++;
                int[][] hg = {{1, 0}, {-1, 0}, {0, 1}, {0, -1}};
                for (int[] d : hg) {
                    int nx = c[0] + d[0], ny = c[1] + d[1];
                    if (nx >= 0 && ny >= 0 && nx < w && ny < h && !tham[ny * w + nx] && diDuoc(nx, ny)) {
                        tham[ny * w + nx] = true;
                        q.add(new int[]{nx, ny});
                    }
                }
            }
            return n;
        }

        /** O di duoc gan (x,y) nhat trong ban kinh r. */
        int[] oGan(int x, int y, int r) {
            for (int k = 0; k <= r; k++)
                for (int dy = -k; dy <= k; dy++)
                    for (int dx = -k; dx <= k; dx++)
                        if (Math.max(Math.abs(dx), Math.abs(dy)) == k && diDuoc(x + dx, y + dy)) return new int[]{x + dx, y + dy};
            return null;
        }

        boolean laCong(int x, int y) {
            return x >= 0 && y >= 0 && x < w && y < h && type[y * w + x] >= 2000000000;
        }

        /** O dung truoc cong nhu client (Tilemap.toOtherMapAt o phia truoc): o di duoc, KHONG phai o cong, ke 4 huong. */
        int[] oTruocCong(int x, int y) {
            int[][] hg = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}};
            for (int k = 1; k <= 3; k++)
                for (int[] d : hg) {
                    int nx = x + d[0] * k, ny = y + d[1] * k;
                    if (diDuoc(nx, ny) && !laCong(nx, ny)) return new int[]{nx, ny};
                }
            return null;
        }
    }

    // =========================================================== kich ban
    interface KichBan {
        void chay() throws Exception;
    }

    static final LinkedHashMap<String, KichBan> KB = new LinkedHashMap<>();
    static final Set<String> CAN_GM = new HashSet<>();

    static void dk(String ten, boolean canGm, KichBan k) {
        KB.put(ten, k);
        if (canGm) CAN_GM.add(ten);
    }

    public static void main(String[] a) throws Exception {
        HOST = a[0];
        PORT = Integer.parseInt(a[1]);
        TIEN_TO = a[2];
        KichBanDS.dangKy();
        List<String> chon = new ArrayList<>();
        for (int i = 3; i < a.length; i++) chon.add(a[i]);
        if (chon.isEmpty() || chon.contains("all")) chon = new ArrayList<>(KB.keySet());
        long bd = System.currentTimeMillis();
        for (String t : chon) {
            KichBan k = KB.get(t);
            if (k == null) { ket(t, null, "khong co kich ban nay"); continue; }
            if (!GM && CAN_GM.contains(t)) { ket(t, null, "can quyen GM (-Dgm=0)"); continue; }
            System.out.println("---- " + t);
            try {
                k.chay();
            } catch (Throwable e) {
                ket(t, false, "ngoai le: " + e);
                e.printStackTrace(System.out);
            }
        }
        System.out.printf("=== TONG: PASS %d, FAIL %d, BO QUA %d (%ds) ===%n", nPass, nFail, nBo, (System.currentTimeMillis() - bd) / 1000);
        String ra = System.getProperty("ra");
        if (ra != null) {
            try (PrintWriter w = new PrintWriter(new OutputStreamWriter(new FileOutputStream(ra), StandardCharsets.UTF_8))) {
                for (String d : KET_QUA) w.println(d);
                w.printf("TONG PASS %d FAIL %d BO_QUA %d%n", nPass, nFail, nBo);
            }
        }
        System.exit(nFail == 0 ? 0 : 1);
    }
}
