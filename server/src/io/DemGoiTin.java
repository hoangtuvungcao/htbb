package io;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 * Bo dem goi tin theo tung nhan vat - de CHAN DOAN tren server dang chay, khong can debugger.
 *
 * VI SAO CO LOP NAY (05/09/2026). Nguoi choi bao "hinh nhan vat nhay 1-2 lan/giay, o moi
 * ban do, ke ca nhan vat cua chinh minh", bat dau ngay sau commit 2480c47 (68 tep, ~3000
 * dong). Nam ban va lien tiep tren duong tam nhin (nearChars, sendToNearPlayer,
 * writeActorPos...) deu khong doi duoc gi - bytecode trong jar da xac nhan ca nam deu co
 * mat. Tuc la moi gia thuyet deu sai, va can DU LIEU THAT thay cho gia thuyet thu sau:
 * server dang gui gi, bao nhieu lan moi giay, cho dung nguoi dang thay nhay.
 *
 * CACH DUNG (khong can build lai, khong can restart):
 *   1. Trong thu muc lam viec cua server (noi co server.ini), tao tep debug_goi_tin.txt,
 *      moi dong mot ten nhan vat can theo doi (khong phan biet hoa/thuong; dong bat dau
 *      bang # la chu thich).
 *   2. Toi da 5 giay sau, log server bat dau in moi giay mot dong cho moi nhan vat do:
 *        [GOI] tennhanvat 1002ms | RA(14): 4x3 60x2 -71x9 | VAO(2): 3x2
 *      RA = server gui xuong, VAO = client gui len. "4x3" = lenh 4 gui 3 lan trong cua so.
 *      Tong trong ngoac. Sap theo so lan giam dan nen thu gi ban nhieu nhat dung dau.
 *   3. Xoa tep (hoac xoa ten) la tat - cung toi da 5 giay.
 *
 * CHI PHI KHI TAT: moi goi tin them mot phep doc bien volatile va mot phep so thoi gian
 * (chi doc lai tep 5 giay mot lan). Khong khoa, khong cap phat. Khi khong theo doi ai thi
 * khong co gi khac ngoai do.
 *
 * Bang tra lenh thuong gap (tu MessageCreator): 3 thong tin nhan vat chinh, 4 vi tri
 * actor, 5 ngoai hinh nhan vat, 8 xoa actor, 12 NAP LAI BAN DO, 15 trang bi, 16 hanh
 * trang, 27 chat, 30 EXP, 60 HP/MP, -44 thu cuoi, -51 anh, -70/-71 hieu ung.
 */
public final class DemGoiTin {

    /** Ten tep co bat/tat, dat o thu muc lam viec cua server (cung cho voi server.ini). */
    public static final String TEP_CO = "debug_goi_tin.txt";
    static final long CHU_KY_DOC_TEP_MS = 5000L;
    static final long CHU_KY_IN_MS = 1000L;

    private static final Object KHOA = new Object();
    private static volatile Set<String> theoDoi = Collections.<String>emptySet();
    private static volatile long lanDocTep = 0L;
    /** Test dat danh sach bang tay thi KHONG doc tep nua, tranh tep that tren may dev xen vao. */
    private static volatile boolean khoaDanhSach = false;

    // Ten nhan vat (chu thuong) -> (lenh -> so lan). Chi cham vao trong KHOA.
    private static final Map<String, Map<Integer, Integer>> demRa = new HashMap<String, Map<Integer, Integer>>();
    private static final Map<String, Map<Integer, Integer>> demVao = new HashMap<String, Map<Integer, Integer>>();
    private static final Map<String, Long> mocCuaSo = new HashMap<String, Long>();

    /** Server GUI mot goi cho nhan vat nay. Goi tu Session.sendMessage. */
    public static void ghiRa(final String charname, final int cmd) {
        ghi(charname, cmd, true);
    }

    /** Client cua nhan vat nay GUI LEN mot goi. Goi tu Session ngay truoc processMessage. */
    public static void ghiVao(final String charname, final int cmd) {
        ghi(charname, cmd, false);
    }

    private static void ghi(final String charname, final int cmd, final boolean ra) {
        if (charname == null || charname.isEmpty()) {
            return;
        }
        capNhatDanhSachTuTep();
        final Set<String> ds = theoDoi;
        if (ds.isEmpty()) {
            return;
        }
        final String k = charname.toLowerCase();
        if (!ds.contains(k)) {
            return;
        }
        final String dong;
        synchronized (KHOA) {
            final Map<String, Map<Integer, Integer>> bang = ra ? demRa : demVao;
            Map<Integer, Integer> m = bang.get(k);
            if (m == null) {
                m = new TreeMap<Integer, Integer>();
                bang.put(k, m);
            }
            final Integer cu = m.get(Integer.valueOf(cmd));
            m.put(Integer.valueOf(cmd), Integer.valueOf(cu == null ? 1 : cu.intValue() + 1));

            final long now = System.currentTimeMillis();
            final Long moc = mocCuaSo.get(k);
            if (moc == null) {
                mocCuaSo.put(k, Long.valueOf(now));
                return;
            }
            final long troi = now - moc.longValue();
            if (troi < CHU_KY_IN_MS) {
                return;
            }
            dong = tomTat(k, troi);
            demRa.remove(k);
            demVao.remove(k);
            mocCuaSo.put(k, Long.valueOf(now));
        }
        // In ngoai khoa - System.out co the cham (ghi ra tep log).
        System.out.println(dong);
    }

    /** Mot dong tom tat cua so hien tai cua nhan vat k. Goi trong KHOA (hoac tu test don luong). */
    static String tomTat(final String k, final long ms) {
        final StringBuilder sb = new StringBuilder(160);
        sb.append("[GOI] ").append(k).append(' ').append(ms).append("ms");
        sb.append(" | RA").append(lietKe(demRa.get(k)));
        sb.append(" | VAO").append(lietKe(demVao.get(k)));
        return sb.toString();
    }

    /** "(tong): lenhxN lenhxN ..." sap theo N giam dan, cung N thi theo ma lenh tang. */
    private static String lietKe(final Map<Integer, Integer> m) {
        if (m == null || m.isEmpty()) {
            return "(0):";
        }
        int tong = 0;
        final List<Map.Entry<Integer, Integer>> ds = new ArrayList<Map.Entry<Integer, Integer>>(m.entrySet());
        for (final Map.Entry<Integer, Integer> e : ds) {
            tong += e.getValue().intValue();
        }
        Collections.sort(ds, new Comparator<Map.Entry<Integer, Integer>>() {
            public int compare(final Map.Entry<Integer, Integer> a, final Map.Entry<Integer, Integer> b) {
                final int d = b.getValue().intValue() - a.getValue().intValue();
                return d != 0 ? d : a.getKey().intValue() - b.getKey().intValue();
            }
        });
        final StringBuilder sb = new StringBuilder(64);
        sb.append('(').append(tong).append("):");
        for (final Map.Entry<Integer, Integer> e : ds) {
            sb.append(' ').append(e.getKey()).append('x').append(e.getValue());
        }
        return sb.toString();
    }

    private static void capNhatDanhSachTuTep() {
        if (khoaDanhSach) {
            return;
        }
        final long now = System.currentTimeMillis();
        if (now - lanDocTep < CHU_KY_DOC_TEP_MS) {
            return;
        }
        synchronized (KHOA) {
            if (now - lanDocTep < CHU_KY_DOC_TEP_MS) {
                return;
            }
            lanDocTep = now;
            final Set<String> moi = docTep(Paths.get(TEP_CO));
            if (!moi.equals(theoDoi)) {
                System.out.println("[GOI] danh sach theo doi doi thanh: " + (moi.isEmpty() ? "(tat)" : moi.toString()));
                theoDoi = moi;
            }
        }
    }

    /** Doc tep co: moi dong mot ten, bo dong trong va dong "#", ve chu thuong. Khong co tep -> rong. */
    public static Set<String> docTep(final Path tep) {
        final Set<String> ra = new HashSet<String>();
        if (tep == null || !Files.isRegularFile(tep)) {
            return ra;
        }
        try {
            for (final String d : Files.readAllLines(tep, StandardCharsets.UTF_8)) {
                final String t = d.trim();
                if (t.isEmpty() || t.startsWith("#")) {
                    continue;
                }
                ra.add(t.toLowerCase());
            }
        } catch (final IOException e) {
            // tep dang duoc ghi giua chung - lan doc sau (5 giay) se lay lai
        }
        return ra;
    }

    /** Cho test: dat thang danh sach theo doi, khoa khong doc tep, xoa sach bo dem. */
    public static void datDanhSachTheoDoi(final Set<String> ds) {
        synchronized (KHOA) {
            khoaDanhSach = true;
            final Set<String> t = new HashSet<String>();
            for (final String s : ds) {
                t.add(s.toLowerCase());
            }
            theoDoi = t;
            demRa.clear();
            demVao.clear();
            mocCuaSo.clear();
        }
    }

    /** Cho test: dong tom tat cua bo dem hien tai (khong in, khong xoa). */
    public static String tomTatHienTai(final String charname) {
        synchronized (KHOA) {
            return tomTat(charname.toLowerCase(), 0L);
        }
    }

    private DemGoiTin() {
    }
}
