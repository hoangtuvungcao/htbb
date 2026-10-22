package game.pokemon;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;
import javax.microedition.rms.RecordStore;
import javax.microedition.rms.RecordStoreFullException;

/**
 * SPRITE TAI THEO NHU CAU (1.0, dot loai Gen 1-2+): loai KHONG co trong jar (res/pk) thi xin may chu goi sprite
 * (-126 kieu 0, GoiSprite), luu RMS co ngan sach byte va xoa theo LRU (KhoSpriteLru).
 *
 * Bo cuc RMS:
 *   "pkdx"        chi muc: ban ghi 1 = KhoSpriteLru.ghi() (khoa, kich, lan dung)
 *   "pk&lt;khoa&gt;"   moi loai mot kho (vd "pk0016"), ban ghi 1 = nguyen goi HTPK. Xoa loai = deleteRecordStore
 *                 (giai phong cho that tren may).
 * Ngan sach mac dinh 1 MB (NGAN_SACH); dong RecordStoreFullException -> bo loai cu nhat roi thu lai (toi da 6
 * lan). May khong co RMS (mo kho loi) -> giu trong RAM toi da RAM_TOI_DA byte trong phien.
 *
 * Luong: xin() / docTep() goi tu luong ve (PokeBody), nhanKhuc() tu luong mang -&gt; moi trang thai dong bo tren KHOA.
 * Moi loai chi xin 1 lan / phien; toi da DANG_XIN_TOI_DA goi dang tai cung luc, con lai xep hang.
 * Dang tai: PokeBody ve bong den ("bong"); xong thi PokeBody.boLoai(dex) de lan ve sau nap hinh that.
 * Ban SAC MAU (shiny, goi "&lt;NNNN&gt;s", kho RMS "pk0016s"): cung so sach LRU / cung ngan sach byte voi ban thuong (mot
 * con shiny tren man = them mot goi trong ngan sach, khong vuot). Dang tai / may chu bao thieu (loai "chua co shiny"):
 * PokeBody ve ban thuong.
 */
public final class KhoSprite {
    public static int NGAN_SACH = KhoSpriteLru.NGAN_SACH_MAC_DINH;
    public static int RAM_TOI_DA = 262144;
    public static final int DANG_XIN_TOI_DA = 2;
    /** Goi qua lon so voi sprite jar (~32 KB) coi la hong, khong cap phat. */
    public static final int GOI_TOI_DA = 400000;
    static final String KHO_CHI_MUC = "pkdx";

    /** false = chi doc kho da co, khong xin may chu (vd man Pokedex luot 1.025 loai). */
    public static boolean choXin = true;

    private static final Object KHOA = new Object();
    private static boolean daKhoiTao;
    private static boolean coRms;
    private static KhoSpriteLru lru;
    private static KhoSpriteLru lruRam;
    /** khoa -> byte[] goi (khi khong co RMS: toan bo; co RMS: vai goi vua dung). */
    private static final Hashtable ram = new Hashtable();
    /** khoa -> Hashtable tep da tach (goi gan nhat, toi da TACH_TOI_DA). */
    private static final Hashtable tach = new Hashtable();
    private static final Vector thuTuTach = new Vector();
    static final int TACH_TOI_DA = 4;

    private static final Hashtable daXin = new Hashtable();
    private static final Hashtable thieu = new Hashtable();
    private static final Vector hang = new Vector();
    /** khoa -> Object[]{byte[] buf, int[]{daNhan}} */
    private static final Hashtable dangTai = new Hashtable();
    private static int soDangXin;
    private static int doiChiMuc;

    private KhoSprite() {
    }

    static String khoa(int dex) {
        return PokeBody.tenKhoa(dex, false);
    }

    static String khoa(int dex, boolean shiny) {
        return PokeBody.tenKhoa(dex, shiny);
    }

    /** Ma hang doi: dex, bit 16 = shiny. */
    static final int CO_SHINY = 0x10000;

    private static void khoiTao() {
        if (daKhoiTao) {
            return;
        }
        daKhoiTao = true;
        lru = new KhoSpriteLru(NGAN_SACH);
        lruRam = new KhoSpriteLru(RAM_TOI_DA);
        try {
            RecordStore r = RecordStore.openRecordStore(KHO_CHI_MUC, true);
            if (r.getNumRecords() > 0) {
                lru.doc(r.getRecord(1));
            }
            r.closeRecordStore();
            coRms = true;
        } catch (Throwable e) {
            coRms = false;
        }
    }

    private static void ghiChiMuc() {
        if (!coRms) {
            return;
        }
        try {
            byte[] b = lru.ghi();
            RecordStore r = RecordStore.openRecordStore(KHO_CHI_MUC, true);
            if (r.getNumRecords() > 0) {
                r.setRecord(1, b, 0, b.length);
            } else {
                r.addRecord(b, 0, b.length);
            }
            r.closeRecordStore();
        } catch (Throwable e) {
            // khong ghi duoc chi muc: lan sau doc lai tu kho that
        }
    }

    private static void xoaKho(String k) {
        try {
            RecordStore.deleteRecordStore("pk" + k);
        } catch (Throwable e) {
            // khong co / dang mo
        }
    }

    /** Goi da luu cua khoa (RAM truoc, roi RMS). null neu chua co. */
    private static byte[] layGoi(String k) {
        byte[] b = (byte[]) ram.get(k);
        if (b != null) {
            if (!coRms) {
                lruRam.dung(k);
            }
            return b;
        }
        if (!coRms || !lru.co(k)) {
            return null;
        }
        try {
            RecordStore r = RecordStore.openRecordStore("pk" + k, false);
            b = r.getRecord(1);
            r.closeRecordStore();
        } catch (Throwable e) {
            b = null;
        }
        if (b == null) {
            lru.xoa(k);
            ghiChiMuc();
            return null;
        }
        lru.dung(k);
        if (++doiChiMuc >= 16) {
            doiChiMuc = 0;
            ghiChiMuc();
        }
        return b;
    }

    /**
     * Tep {@code tenTep} cua loai {@code khoa} trong kho (null neu chua tai). Chi doc, khong xin may chu.
     */
    public static byte[] docTep(String k, String tenTep) {
        synchronized (KHOA) {
            khoiTao();
            Hashtable t = (Hashtable) tach.get(k);
            if (t == null) {
                byte[] b = layGoi(k);
                if (b == null) {
                    return null;
                }
                t = GoiSprite.doc(b, 0);
                if (t == null) {
                    boLoaiHong(k);
                    return null;
                }
                tach.put(k, t);
                thuTuTach.addElement(k);
                while (thuTuTach.size() > TACH_TOI_DA) {
                    tach.remove(thuTuTach.elementAt(0));
                    thuTuTach.removeElementAt(0);
                }
            }
            return (byte[]) t.get(tenTep);
        }
    }

    private static void boLoaiHong(String k) {
        ram.remove(k);
        if (coRms) {
            lru.xoa(k);
            xoaKho(k);
            ghiChiMuc();
        } else {
            lruRam.xoa(k);
        }
    }

    /** Loai da co trong kho (khong doc noi dung). */
    public static boolean co(int dex) {
        return co(dex, false);
    }

    public static boolean co(int dex, boolean shiny) {
        synchronized (KHOA) {
            khoiTao();
            String k = khoa(dex, shiny);
            return ram.containsKey(k) || coRms && lru.co(k);
        }
    }

    /** Loai nay may chu bao khong co sprite (trong phien). */
    public static boolean thieu(int dex) {
        return thieu(dex, false);
    }

    public static boolean thieu(int dex, boolean shiny) {
        return thieu.containsKey(khoa(dex, shiny));
    }

    /**
     * Can sprite loai {@code dex} ma jar khong co: xep hang xin may chu (1 lan / phien). Khong lam gi neu choXin
     * = false, da co trong kho, da xin, hoac may chu da bao thieu.
     */
    public static void xin(int dex) {
        xin(dex, false);
    }

    /** Nhu xin(dex) cho ban thuong hoac ban SAC MAU (goi "&lt;NNNN&gt;s"). */
    public static void xin(int dex, boolean shiny) {
        if (dex <= 0 || dex > 9999) {
            return;
        }
        synchronized (KHOA) {
            if (!choXin) {
                return;
            }
            khoiTao();
            String k = khoa(dex, shiny);
            if (daXin.containsKey(k) || thieu.containsKey(k) || ram.containsKey(k) || coRms && lru.co(k)) {
                return;
            }
            daXin.put(k, Boolean.TRUE);
            hang.addElement(new Integer(dex | (shiny ? CO_SHINY : 0)));
        }
        bom();
    }

    /** Gui yeu cau tiep theo khi con cho (toi da DANG_XIN_TOI_DA dang tai). */
    static void bom() {
        while (true) {
            int dex;
            synchronized (KHOA) {
                if (soDangXin >= DANG_XIN_TOI_DA || hang.isEmpty()) {
                    return;
                }
                dex = ((Integer) hang.elementAt(0)).intValue();
                hang.removeElementAt(0);
                soDangXin++;
            }
            boolean sh = (dex & CO_SHINY) != 0;
            int d = dex & 0xFFFF;
            if (!LoaiMang.guiXinSprite(d, sh)) {
                synchronized (KHOA) {
                    soDangXin--;
                    daXin.remove(khoa(d, sh));   // chua noi may chu: lan ve sau xin lai
                }
                return;
            }
        }
    }

    /**
     * Mot khuc goi sprite (-126 kieu 0): tong = 0 -&gt; may chu khong co. Tra ve dex vua tai xong (&gt; 0) de noi goi
     * lam moi hinh, 0 neu chua xong / loi.
     */
    public static int nhanKhuc(int dex, boolean shiny, int tong, int viTri, byte[] du) {
        String k = khoa(dex, shiny);
        byte[] xong = null;
        boolean het = false;
        synchronized (KHOA) {
            khoiTao();
            if (tong <= 0 || tong > GOI_TOI_DA) {
                thieu.put(k, Boolean.TRUE);
                dangTai.remove(k);
                het = true;
            } else {
                Object[] dt = (Object[]) dangTai.get(k);
                if (dt == null || ((byte[]) dt[0]).length != tong) {
                    dt = new Object[]{new byte[tong], new int[1]};
                    dangTai.put(k, dt);
                }
                byte[] buf = (byte[]) dt[0];
                int[] nhan = (int[]) dt[1];
                if (du != null && viTri >= 0 && viTri + du.length <= tong) {
                    System.arraycopy(du, 0, buf, viTri, du.length);
                    nhan[0] += du.length;
                }
                if (nhan[0] >= tong) {
                    dangTai.remove(k);
                    xong = buf;
                    het = true;
                }
            }
            if (het && soDangXin > 0) {
                soDangXin--;
            }
        }
        int kq = 0;
        if (xong != null) {
            if (luu(dex, shiny, xong)) {
                kq = dex;
            } else {
                thieu.put(k, Boolean.TRUE);
            }
        }
        if (het) {
            bom();
        }
        return kq;
    }

    /** Kiem goi roi luu (RMS co ngan sach, hoac RAM). false = goi hong. */
    public static boolean luu(int dex, byte[] goi) {
        return luu(dex, false, goi);
    }

    /** Luu goi ban thuong / ban sac mau (co shiny trong dau goi phai khop). */
    public static boolean luu(int dex, boolean shiny, byte[] goi) {
        if (GoiSprite.doc(goi, dex) == null || GoiSprite.shiny(goi) != (shiny ? 1 : 0)) {
            return false;
        }
        String k = khoa(dex, shiny);
        synchronized (KHOA) {
            khoiTao();
            tach.remove(k);
            thuTuTach.removeElement(k);
            if (coRms && ghiRms(k, goi)) {
                // giu ban RAM cho lan ve dau tien (ve ngay khong doc lai dia), bo khi kho tach xoay vong
                ram.put(k, goi);
                giuRamGon(k);
                return true;
            }
            ram.put(k, goi);
            lruRam.datNganSach(RAM_TOI_DA);
            Vector bo = lruRam.them(k, goi.length);
            for (int i = 0; i < bo.size(); i++) {
                ram.remove(bo.elementAt(i));
                tach.remove(bo.elementAt(i));
                thuTuTach.removeElement(bo.elementAt(i));
            }
            return true;
        }
    }

    /** Co RMS: RAM chi giu goi vua tai gan nhat (toi da TACH_TOI_DA). */
    private static void giuRamGon(String moi) {
        if (!coRms || ram.size() <= TACH_TOI_DA) {
            return;
        }
        Enumeration e = ram.keys();
        Vector bo = new Vector();
        while (e.hasMoreElements()) {
            Object k = e.nextElement();
            if (!k.equals(moi)) {
                bo.addElement(k);
            }
        }
        for (int i = 0; i < bo.size() && ram.size() > TACH_TOI_DA; i++) {
            ram.remove(bo.elementAt(i));
        }
    }

    private static boolean ghiRms(String k, byte[] goi) {
        lru.datNganSach(NGAN_SACH);   // ngan sach doi duoc luc chay (cai dat / kiem)
        Vector bo = lru.them(k, goi.length);
        for (int i = 0; i < bo.size(); i++) {
            String c = (String) bo.elementAt(i);
            xoaKho(c);
            ram.remove(c);
            tach.remove(c);
            thuTuTach.removeElement(c);
        }
        for (int lan = 0; lan < 6; lan++) {
            try {
                xoaKho(k);
                RecordStore r = RecordStore.openRecordStore("pk" + k, true);
                try {
                    r.addRecord(goi, 0, goi.length);
                } finally {
                    r.closeRecordStore();
                }
                ghiChiMuc();
                return true;
            } catch (RecordStoreFullException e) {
                String c = lru.cuNhat(k);
                if (c == null) {
                    break;
                }
                lru.xoa(c);
                xoaKho(c);
            } catch (Throwable e) {
                break;
            }
        }
        lru.xoa(k);
        xoaKho(k);
        ghiChiMuc();
        return false;
    }

    /** Doi tai khoan / mat ket noi: quen trang thai xin cua phien (kho dia giu nguyen). */
    public static void datLaiPhien() {
        synchronized (KHOA) {
            daXin.clear();
            thieu.clear();
            hang.removeAllElements();
            dangTai.clear();
            soDangXin = 0;
        }
    }

    /** So byte dang dung (RMS hoac RAM) - de hien / kiem. */
    public static int dungLuong() {
        synchronized (KHOA) {
            khoiTao();
            return coRms ? lru.tong() : lruRam.tong();
        }
    }
}
