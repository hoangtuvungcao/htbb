package real;

import com.htbb.cauhinh.CauHinhTrungTam;
import com.htbb.pokemon.KhoaBanDo;
import io.Message;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Noi KHOA BAN DO (com.htbb.pokemon.KhoaBanDo, bang data_ban_do_mo_khoa) vao game: chan moi duong vao ban do
 * (doChangeMap, move2Map, xa phu, Pidgeot / Lapras, bai luyen...), mien tru nhan vat cu luc nap (tob_ban_do_mo), gui
 * danh sach ban do khoa / khong cho tu dong cho client (goi {@link #LENH} -106) va bao "Đã mở bản đồ" khi len cap /
 * xong cua ai. Admin khong bi chan. Tat ca bang cong tac {@link #K_BAT} (danh muc cau hinh, mac dinh 1).
 */
public final class KhoaBanDoGame {

    public static final String K_BAT = "ban_do.khoa.bat";
    public static final int LENH = -106;
    /** Client dau tien doc goi -106 (cac ban 0.9.6 cu bo qua goi la, chi thay thong bao -26). */
    public static final int BAN_CLIENT = 906;

    /** charDBID -> moc mien tru (giu ca khi thoat: vai so nguyen / nhan vat, tranh dua giua phien cu va moi). */
    private static final ConcurrentHashMap<Integer, Integer> MIEN = new ConcurrentHashMap<Integer, Integer>();
    /** charDBID -> ban do goc dang khoa lan gui truoc (bao "Đã mở"). */
    private static final ConcurrentHashMap<Integer, Set<Integer>> KHOA_TRUOC = new ConcurrentHashMap<Integer, Set<Integer>>();
    /** charDBID -> van tay goi -106 lan truoc (khong gui lai neu khong doi). */
    private static final ConcurrentHashMap<Integer, Integer> VAN_TAY = new ConcurrentHashMap<Integer, Integer>();

    private KhoaBanDoGame() {
    }

    /** Khoi dong server (RealController sau TenBanDoMayChu): tao bang mien tru, nap luat, nap lai tu dong. */
    public static void batDau() {
        KhoaBanDo.taoBang();
        KhoaBanDo.nap();
        KhoaBanDo.batNapLaiTuDong(new Runnable() {
            @Override
            public void run() {
                capNhatTatCa();
            }
        });
    }

    public static boolean bat() {
        return !"0".equals(CauHinhTrungTam.chuoi(K_BAT).trim());
    }

    /** Ban do goc (mapIDLoadMap) cua id bat ky (ban sao theo khu theo luat ban goc). */
    static int goc(final int mapId) {
        final Map m = RealController.mapList == null ? null : RealController.mapList.get(Integer.valueOf(mapId));
        return m != null ? m.mapIDLoadMap : mapId;
    }

    public static KhoaBanDo.Luat luatCua(final int mapId) {
        return KhoaBanDo.luat(goc(mapId));
    }

    static int capCua(final Char p) {
        try {
            return p.lvDetail.lv;
        } catch (final Exception e) {
            return 0;
        }
    }

    static int mien(final Char p) {
        final Integer m = MIEN.get(Integer.valueOf(p.charDBID));
        // chua nap (vd test / nhan vat ao): mien tru theo cap hien tai - an toan, khong bao gio khoa nham
        return m != null ? m.intValue() : Math.max(1, capCua(p));
    }

    static boolean daXong(final Char p, final KhoaBanDo.Luat l) {
        return l.ma.isEmpty() || NhiemVuEngine.daXongMa(p, l.ma);
    }

    static String tenMap(final int mapId) {
        try {
            final String t = TenBanDoMayChu.tenDayDu(mapId);
            return t.isEmpty() ? TenBanDoMayChu.ten(goc(mapId)) : t;
        } catch (final Exception e) {
            return "";
        }
    }

    /** null = vao duoc; nguoc lai cau bao. Khong gui gi. */
    public static String lyDoChan(final Char p, final int toMapId) {
        if (p == null || p.isAdmin || !bat() || toMapId == p.mapID) {
            return null;
        }
        final KhoaBanDo.Luat l = luatCua(toMapId);
        if (l == null || !l.coKhoa()) {
            return null;
        }
        return KhoaBanDo.lyDo(l, capCua(p), daXong(p, l), mien(p), tenMap(toMapId));
    }

    /** Chan + bao (thong bao -26). true = bi chan (nguoi goi giu nguoi choi o cho cu, KHONG tru phi). */
    public static boolean chan(final Char p, final int toMapId) {
        final String ly = lyDoChan(p, toMapId);
        if (ly == null) {
            return false;
        }
        bao(p, ly);
        try {
            com.htbb.log.Nhat.thongTin("KHOA_MAP", "chan " + p.charname + " cap " + capCua(p) + " -> map " + toMapId);
        } catch (final Throwable ignored) {
        }
        return true;
    }

    /**
     * Bi chan o duong khong tu nap lai map (xa phu / bay da chuyen client sang man cho): nap lai map hien tai tai cho
     * dung (createMapMessage) de client khong ket man cho 30 giay.
     */
    public static void giuTaiCho(final Char p) {
        try {
            if (p.map != null) {
                p.map.move2Map(p, p.x / 16, p.y / 16, p.mapID, p.inCountry);
            }
        } catch (final Exception e) {
            System.out.println("KhoaBanDoGame.giuTaiCho: " + e);
        }
    }

    static void bao(final Char p, final String s) {
        try {
            p.sendMessage(MessageCreator.createServerAlertAutoOffMessage(s));
        } catch (final Exception ignored) {
        }
    }

    /**
     * Nhan diem den trong menu bay / do (goi -30, chu): khoa -> "Khoá: <nhan>" (client ve hang xam + o khoa theo chu
     * "Khoá"), kem " · AUTO" / " · Không AUTO". Chon diem khoa -> server chan + bao dieu kien (khong tru phi).
     */
    public static String nhanDiem(final Char p, final int mapId, final String nhan) {
        if (!bat()) {
            return nhan;
        }
        final KhoaBanDo.Luat l = luatCua(mapId);
        final boolean khoa = lyDoChan(p, mapId) != null;
        return (khoa ? "Khoá: " : "") + nhan + (l == null || l.tuDong ? " · AUTO" : " · Không AUTO");
    }

    // ------------------------------------------------------------------ tu dong

    /** null = ban do cho tu dong; nguoc lai cau bao. */
    public static String lyDoKhongTuDong(final int mapId) {
        if (!bat()) {
            return null;
        }
        return KhoaBanDo.lyDoTuDong(luatCua(mapId));
    }

    // ------------------------------------------------------------------ nap nhan vat / mien tru

    /**
     * CharNapDuLieu (sau khi chon cho hoi sinh): lan dau -> moc mien = max(cap, cap ban do dang luu); moi lan: ban do
     * dang luu bi khoa -> nang moc len cap ban do do (khong bao gio dich chuyen nguoi choi di).
     */
    public static void khiNap(final Char p) {
        if (p == null || p.charDBID <= 0) {
            return;
        }
        try {
            final KhoaBanDo.Luat luu = luatCua(p.mapID);
            int moc = KhoaBanDo.docMien(p.charDBID);
            if (moc == -2) {
                MIEN.put(Integer.valueOf(p.charDBID), Integer.valueOf(KhoaBanDo.mocMienDau(capCua(p), luu)));
                return; // loi CSDL: mien tru tam, khong ghi
            }
            if (moc < 0) {
                moc = KhoaBanDo.mocMienDau(capCua(p), luu);
                KhoaBanDo.ghiMien(p.charDBID, moc, "dau");
            } else if (luu != null && !p.isAdmin && !KhoaBanDo.mo(luu, capCua(p), daXong(p, luu), moc)) {
                moc = Math.max(moc, luu.cap);
                KhoaBanDo.ghiMien(p.charDBID, moc, "ban_do_luu");
                System.out.println("[KHOA BAN DO] mien tru " + p.charname + " o map " + p.mapID + " -> moc " + moc);
            }
            MIEN.put(Integer.valueOf(p.charDBID), Integer.valueOf(moc));
        } catch (final Exception e) {
            System.out.println("KhoaBanDoGame.khiNap: " + e);
        }
    }

    /** Test: dat moc mien tru. */
    public static void datMien(final int idChar, final int moc) {
        MIEN.put(Integer.valueOf(idChar), Integer.valueOf(moc));
    }

    // ------------------------------------------------------------------ goi -106

    /** Nhom ban do theo (co, yeu cau, ly do tu dong) -> danh sach id (ca ban sao). Ban do mac dinh (mo, cho tu dong) bo. */
    public static LinkedHashMap<String, List<Integer>> nhom(final Char p, final Set<Integer> khoaGoc) {
        final LinkedHashMap<String, List<Integer>> kq = new LinkedHashMap<String, List<Integer>>();
        if (RealController.mapList == null) {
            return kq;
        }
        final List<Integer> ids = new ArrayList<Integer>(RealController.mapList.keySet());
        Collections.sort(ids);
        for (final Integer id : ids) {
            if (id.intValue() < 0) {
                continue;
            }
            final KhoaBanDo.Luat l = luatCua(id.intValue());
            if (l == null) {
                continue;
            }
            final String ly = p.isAdmin ? null : KhoaBanDo.lyDo(l, capCua(p), daXong(p, l), mien(p), tenMap(id.intValue()));
            final int co = (ly != null ? KhoaBanDo.CO_KHOA : 0) | (l.tuDong ? 0 : KhoaBanDo.CO_KHONG_TU_DONG);
            if (co == 0) {
                continue;
            }
            if (ly != null && khoaGoc != null) {
                khoaGoc.add(Integer.valueOf(l.mapId));
            }
            final String k = co + "\u0001" + (ly == null ? "" : ly) + "\u0001" + (l.tuDong ? "" : l.lyDoTuDong);
            List<Integer> ds = kq.get(k);
            if (ds == null) {
                ds = new ArrayList<Integer>();
                kq.put(k, ds);
            }
            ds.add(id);
        }
        return kq;
    }

    /** Goi -106: byte phien ban 1, short n nhom; moi nhom: byte co, UTF yeu cau, UTF ly do tu dong, short k, k x short id. */
    public static Message taoGoi(final LinkedHashMap<String, List<Integer>> nhom) throws java.io.IOException {
        final Message m = new Message(LENH);
        m.dos.writeByte(1);
        m.dos.writeShort(nhom.size());
        for (final java.util.Map.Entry<String, List<Integer>> e : nhom.entrySet()) {
            final String[] a = e.getKey().split("\u0001", -1);
            m.dos.writeByte(Integer.parseInt(a[0]));
            m.dos.writeUTF(a[1]);
            m.dos.writeUTF(a[2]);
            m.dos.writeShort(e.getValue().size());
            for (final Integer id : e.getValue()) {
                m.dos.writeShort(id.intValue());
            }
        }
        return m;
    }

    static boolean clientDoc(final Char p) {
        return p != null && p.session != null && p.session.banClient >= BAN_CLIENT;
    }

    /** Vao game: gui danh sach (khong bao "Đã mở"). */
    public static void guiDanhSach(final Char p) {
        if (p != null) {
            VAN_TAY.remove(Integer.valueOf(p.charDBID)); // phien moi: luon gui
        }
        capNhat(p, false);
    }

    /** Len cap / xong cua ai / admin sua bang: gui lai neu doi, bao ban do vua mo. */
    public static void capNhat(final Char p) {
        capNhat(p, true);
    }

    static void capNhat(final Char p, final boolean baoMo) {
        if (p == null || p.charDBID <= 0) {
            return;
        }
        try {
            final Set<Integer> khoa = new TreeSet<Integer>();
            final LinkedHashMap<String, List<Integer>> n = bat() ? nhom(p, khoa) : new LinkedHashMap<String, List<Integer>>();
            final Integer k = Integer.valueOf(p.charDBID);
            final Set<Integer> truoc = KHOA_TRUOC.put(k, khoa);
            if (baoMo && truoc != null) {
                final List<String> moi = new ArrayList<String>();
                for (final Iterator<Integer> it = truoc.iterator(); it.hasNext(); ) {
                    final Integer g = it.next();
                    if (!khoa.contains(g)) {
                        final String t = tenMap(g.intValue());
                        moi.add(t.isEmpty() ? String.valueOf(g) : t);
                    }
                }
                if (!moi.isEmpty()) {
                    bao(p, "Đã mở bản đồ mới: " + String.join(", ", moi) + ".");
                }
            }
            if (!clientDoc(p)) {
                return;
            }
            final int vt = n.hashCode();
            final Integer cu = VAN_TAY.put(k, Integer.valueOf(vt));
            if (cu != null && cu.intValue() == vt) {
                return;
            }
            p.sendMessage(taoGoi(n));
        } catch (final Exception e) {
            System.out.println("KhoaBanDoGame.capNhat: " + e);
        }
    }

    /** Bang luat vua nap lai (admin sua): cap nhat moi nguoi dang choi. */
    static void capNhatTatCa() {
        try {
            for (int i = 0; i < CharManager.instance.vChars.size(); i++) {
                final Char c = CharManager.instance.vChars.elementAt(i);
                if (c != null) {
                    capNhat(c);
                }
            }
        } catch (final Exception e) {
            System.out.println("KhoaBanDoGame.capNhatTatCa: " + e);
        }
    }
}
