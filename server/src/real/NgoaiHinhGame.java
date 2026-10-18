package real;

import com.htbb.pokemon.HoSoLauDai;
import com.htbb.pokemon.NgoaiHinh;
import io.Message;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Noi NGOAI HINH TRANG BI (com.htbb.pokemon.NgoaiHinh) vao game. Thiet ke: docs/thiet-ke-game.md.
 *
 *   - {@link #tao}: goi -102 cua mot nhan vat; TienHoaPokemon.taoPokeThan treo no cuoi chuoi -90 -> -93 -> -94,
 *     nen moi lan server gui thong tin nhan vat (vao tam nhin, mac/thao do, goi 3 cua chinh minh) la co.
 *   - {@link #guiNeuDoi}: duong doi do KHONG gui lai thong tin nhan vat (nang cap +N, doi cai dat) - chi gui
 *     -102 khi ngoai hinh that su doi (so khoa), cho chinh minh + nguoi o gan.
 *   - Lenh -103 (client -&gt; server, MapXuLyLenh): u8 loai 0 + u8 an = luu mat na an (HoSoLauDai "nh"; tat
 *     sv.lau_dai thi giu trong bo nho toi khi thoat). Gioi han 1 lan / 400 ms moi nhan vat (chong spam).
 */
public final class NgoaiHinhGame {

    private NgoaiHinhGame() {
    }

    /** Mat na an khi khong co ho so (sv.lau_dai tat): charDBID -&gt; an. */
    private static final ConcurrentHashMap<Integer, Integer> AN_DU_PHONG = new ConcurrentHashMap<Integer, Integer>();
    /** Khoa ngoai hinh da gui gan nhat: char id -&gt; khoa. */
    private static final ConcurrentHashMap<Integer, Long> DA_GUI = new ConcurrentHashMap<Integer, Long>();
    /** Lan doi cai dat gan nhat (ms): charDBID -&gt; ms. */
    private static final ConcurrentHashMap<Integer, Long> LAN_DOI = new ConcurrentHashMap<Integer, Long>();
    static final long KHOANG_DOI_MS = 400L;

    /** Mat na an cua nhan vat. */
    public static int an(final Char p) {
        if (p == null || p.charDBID <= 0) {
            return 0;
        }
        final HoSoLauDai h = LauDaiNhanVat.lay(p);
        if (h != null) {
            return h.ngoaiHinhAn;
        }
        final Integer v = AN_DU_PHONG.get(Integer.valueOf(p.charDBID));
        return v == null ? 0 : v.intValue();
    }

    static void datAn(final Char p, final int an) {
        final HoSoLauDai h = LauDaiNhanVat.lay(p);
        if (h != null) {
            synchronized (h) {
                if (h.ngoaiHinhAn != an) {
                    h.ngoaiHinhAn = an;
                    h.ban = true;
                }
            }
            return;
        }
        AN_DU_PHONG.put(Integer.valueOf(p.charDBID), Integer.valueOf(an));
    }

    static int idTemplate(final Item it) {
        try {
            return it == null || it.getTemplate() == null ? 0 : it.getTemplate().id;
        } catch (final Exception e) {
            return 0;
        }
    }

    /** Ngoai hinh cua {@code ch} (trang bi bo dang mac + thoi trang + mat na an). */
    public static NgoaiHinh.Du tinh(final Char ch) {
        final Vector<Item> ds = ch.wItems == null ? new Vector<Item>() : ch.getCharWearing();
        final int n = ds.size();
        final int[] type = new int[n], cap = new int[n], mau = new int[n], plus = new int[n];
        for (int i = 0; i < n; i++) {
            final Item it = ds.get(i);
            try {
                type[i] = it.getTemplate().type;
                cap[i] = it.getTemplate().level;
            } catch (final Exception e) {
                type[i] = -1;
            }
            mau[i] = it.colorName;
            plus[i] = it.plus;
        }
        int thoiTrang = 0, mu = 0, matNa = 0, canh = 0;
        final data.ModelWearing w = ch.wModel;
        if (w != null) {
            thoiTrang = idTemplate(w.itemModel);
            mu = idTemplate(w.hatModel);
            matNa = idTemplate(w.matNa);
            canh = idTemplate(w.CanhModel);
        }
        return NgoaiHinh.tinh(type, cap, mau, plus, thoiTrang, mu, matNa, canh, an(ch));
    }

    /** -102 cua {@code ch}; null khi loi. */
    public static Message tao(final Char ch) {
        if (ch == null) {
            return null;
        }
        try {
            final NgoaiHinh.Du d = tinh(ch);
            final Message m = new Message(NgoaiHinh.LENH);
            NgoaiHinh.ghi(m.dos, ch.id, d);
            DA_GUI.put(Integer.valueOf(ch.id), Long.valueOf(d.khoa()));
            return m;
        } catch (final Exception e) {
            return null;
        }
    }

    /** Gui -102 cho chinh minh + nguoi o gan neu ngoai hinh doi so voi lan gui truoc (vd vua nang cap +N). */
    public static void guiNeuDoi(final Char p) {
        if (p == null || p.isBot != -1) {
            return;
        }
        try {
            final long k = tinh(p).khoa();
            final Long cu = DA_GUI.get(Integer.valueOf(p.id));
            if (cu != null && cu.longValue() == k) {
                return;
            }
            final Message m = tao(p);
            if (m == null) {
                return;
            }
            p.sendMessage(m);
            p.sendToNearPlayer(m);
        } catch (final Exception e) {
            // khong de loi hinh lam hong duong nang cap
        }
    }

    /** Lenh -103 tu client. */
    public static void xuLy(final Char p, final Message msg) {
        if (p == null || p.charDBID <= 0) {
            return;
        }
        try {
            final int loai = msg.dis.readUnsignedByte();
            if (loai != 0) {
                return;
            }
            final int an = msg.dis.readUnsignedByte() & (0x7F | NgoaiHinh.AN_TRANG_PHUC);
            final Integer k = Integer.valueOf(p.charDBID);
            final long bay = System.currentTimeMillis();
            final Long lan = LAN_DOI.get(k);
            if (lan != null && bay - lan.longValue() < KHOANG_DOI_MS) {
                return;
            }
            LAN_DOI.put(k, Long.valueOf(bay));
            datAn(p, an);
            guiNeuDoi(p);
        } catch (final Exception e) {
            // goi hong: bo qua
        }
    }

    /**
     * Khoi ngoai hinh cuoi goi 13 (man chon Pokemon): dex, shiny, giai doan tien hoa + ngoai hinh tung nhan vat
     * (doc lai tob_char: dex, shiny, class, lastLv, equip, pInfo). Loi: khong ghi gi.
     */
    public static void ghiKhoiDanhSach(final java.io.DataOutputStream dos, final java.util.List<data.CharData> ds) {
        if (ds == null || ds.isEmpty()) {
            return;
        }
        try {
            final java.io.ByteArrayOutputStream bo = new java.io.ByteArrayOutputStream();
            final java.io.DataOutputStream o = new java.io.DataOutputStream(bo);
            int n = 0;
            for (final data.CharData c : ds) {
                final Object[] r = napNhanVat(c.id);
                if (r == null) {
                    continue;
                }
                int dex = ((Integer) r[0]).intValue();
                final int lop = ((Integer) r[2]).intValue();
                final int cap = Math.max(1, ((Integer) r[3]).intValue());
                if (dex <= 0) {
                    dex = com.htbb.pokemon.LoaiPokemon.khoiDau(lop);
                }
                if (dex > 0) {
                    dex = com.htbb.pokemon.LoaiPokemon.dangTheoCap(dex, cap);
                }
                int slot = 0;
                try {
                    slot = Integer.parseInt(((String) r[5]).split(",")[36].trim());
                } catch (final Exception e) {
                    slot = 0;
                }
                final int[][] t = NgoaiHinh.tuCotEquip((String) r[4], slot, tpl -> loaiTemplate(tpl));
                final NgoaiHinh.Du d = NgoaiHinh.tinh(t[0], t[1], t[2], t[3], 0, 0, 0, 0,
                        anTheoId(c.id));
                o.writeInt(c.id);
                o.writeShort(Math.max(0, dex));
                o.writeByte(((Integer) r[1]).intValue() != 0 ? 1 : 0);
                o.writeByte(dex > 0 ? com.htbb.pokemon.LoaiPokemon.giaiDoan(dex) : 1);
                NgoaiHinh.ghiNoiDung(o, d);
                n++;
            }
            if (n == 0 || n > 255) {
                return;
            }
            dos.writeByte('N');
            dos.writeByte(1);
            dos.writeByte(n);
            dos.write(bo.toByteArray());
        } catch (final Exception e) {
            // khong de loi hinh lam hong dang nhap
        }
    }

    /** type cua template (lop 5 = bang chung), -1 neu khong co. */
    static int loaiTemplate(final int tpl) {
        try {
            final Object t = Map.itemTemplates.get(5).get(Integer.valueOf(tpl));
            return t == null ? -1 : ((ItemTemplates) t).type;
        } catch (final Exception e) {
            return -1;
        }
    }

    /** Mat na an theo id nhan vat (ho so dai han neu da nap, khong thi du phong; mac dinh 0). */
    static int anTheoId(final int idChar) {
        final Integer v = AN_DU_PHONG.get(Integer.valueOf(idChar));
        if (v != null) {
            return v.intValue();
        }
        final String s = com.htbb.pokemon.LauDai.bat() ? data.DatabaseLauDai.napHoSo(idChar) : null;
        return s == null || s.isEmpty() ? 0 : HoSoLauDai.giaiMa(s).ngoaiHinhAn;
    }

    /** {dex, shiny, class, lastLv, equip, pInfo} cua nhan vat; null neu loi. */
    static Object[] napNhanVat(final int idChar) {
        java.sql.Connection conn = null;
        java.sql.PreparedStatement pre = null;
        java.sql.ResultSet rs = null;
        try {
            conn = data.Database.instance.getConnection();
            pre = conn.prepareStatement("SELECT dex, shiny, class, lastLv, equip, pInfo FROM tob_char WHERE id = ?");
            pre.setInt(1, idChar);
            rs = pre.executeQuery();
            if (!rs.next()) {
                return null;
            }
            return new Object[]{Integer.valueOf(rs.getInt(1)), Integer.valueOf(rs.getInt(2)), Integer.valueOf(rs.getInt(3)),
                Integer.valueOf(rs.getInt(4)), rs.getString(5), rs.getString(6)};
        } catch (final Exception e) {
            return null;
        } finally {
            try {
                if (rs != null) {
                    rs.close();
                }
            } catch (final Exception ignored) {
            }
            try {
                if (pre != null) {
                    pre.close();
                }
            } catch (final Exception ignored) {
            }
            if (conn != null) {
                data.Database.connPool.free(conn);
            }
        }
    }

    /** Nhan vat thoat game: bo khoa da gui (char id co the duoc dung lai). */
    public static void khiThoat(final Char p) {
        if (p != null) {
            DA_GUI.remove(Integer.valueOf(p.id));
            LAN_DOI.remove(Integer.valueOf(p.charDBID));
        }
    }
}
