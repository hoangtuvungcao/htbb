package real;

import com.htbb.pokemon.BatPokemon;
import com.htbb.pokemon.HopPc;
import com.htbb.pokemon.LauDai;
import com.htbb.pokemon.Pokedex;
import data.DatabaseBoNgoc;
import io.Message;
import java.io.DataOutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentHashMap;

/**
 * THU PHUC POKEMON - phan GAME (docs/thiet-ke-game.md 7.7; logic thuan com.htbb.pokemon.BatPokemon / HopPc).
 *
 * <h3>Luong</h3>
 * Nguoi choi danh mot Pokemon hoang da xuong &lt;= 25% HP -&gt; client hien nut "Ném bóng" (chi khi co bong) -&gt; goi -121
 * kieu 1 -&gt; {@link #nem}: may chu kiem LAI moi dieu kien (loai bat duoc, khong boss / Gym / huyen thoai, HP, lech cap,
 * khoang cach, chinh nguoi nay vua danh con do trong 30 giay, gioi han ngay, nhip nem, Hop PC con cho), tru 1 bong, quay
 * ti le (BatPokemon.tiLe). Thanh cong: quai bien mat nhu bi ha (khong roi do / EXP them), Pokedex "Da bat", +1 con vao
 * Hop PC, + Keo loai. Truot: lan sau +5%; truot lan thu 3 quai bo chay. Tu dong danh KHONG bao gio nem bong (TuDong
 * khong co hanh dong nay) - chong bot: nhip &gt;= {@code bat.cach_ms}, tran nem / ngay, tran bat / ngay.
 *
 * <h3>Goi -121 THU_PHUC (client &gt;= BoNgocGame.BAN_CLIENT)</h3>
 * <pre>
 * C-&gt;S byte kieu: 0 xin trang thai; 1 nem (short idQuai, byte thuTuBong); 2 xin Pokedex
 * S-&gt;C byte kieu:
 *   0 TRANG_THAI byte nguongHp%, byte nBong, n x (byte thuTu, UTF ten, short so, short tranPhanNghin), short batNgay,
 *                short tranNgay, short soCon, short sucChua
 *   1 KET_QUA    short charId, short idQuai, byte thuTuBong, byte kq (0 truot / 1 bat duoc / 2 bo chay), byte soLanLac,
 *                short dex, byte shiny, short tiLePhanNghin, UTF chu   (gui ca nguoi gan: ho chi xem hieu ung)
 *   2 POKEDEX    short n, n x (short dex, byte co: bit0 da ha, bit1 da bat, bit2 bat shiny), short soDaBat
 *   3 DONG_HANH  short charId, short dex (0 = khong), byte shiny  (ban dong hanh di theo, chi de nhin)
 *   4 HOP_PC     short soCon, short sucChua, int dangTheo, short giuMoiLoai, short n, n x (int id, short dex, byte cap,
 *                byte shiny, byte sao, byte ivPhanTram, short sucManh, byte tinhCach, byte doHiem, short keo, short keoLuyen,
 *                short dexSau, byte capTienHoa, short keoTienHoa, byte co, 6 x byte iv, UTF ten, UTF tinhCach, UTF tenKeo,
 *                UTF thuongChuyenDoi)   co: bit0 trung loai, bit1 khoa trao doi, bit2 nhan qua trao doi, bit3 tien hoa khi
 *                trao doi, bit4 dang theo
 *   5 LOAI       short dex, UTF ten, byte coDong, byte doHiem, UTF tenDoHiem, short tiLeGoc, short dexTruoc, short dexSau,
 *                byte capTienHoa, byte tienHoaTraoDoi, UTF tenTruoc, UTF tenSau, short soBat, short soHa, byte n, n x UTF
 *                noiSong, byte 6|0, 6 x byte chiSoGoc
 *   6 TRAO_DOI   (TraoDoiPokemonGame)
 *   7 CHO        (ChoPokemonGame)
 * C-&gt;S 0.9.8: 0 + byte 1 = client co man Hop PC; 3 xin Hop PC; 4 byte hanhDong (0 theo, 1 luyen, 2 tien hoa, 3 tha,
 *   4 chuyen doi), int id; 5 short dex; 6 trao doi (TraoDoiPokemonGame)
 * </pre>
 * Menu: Y ta Chansey muc "Poké Ball &amp; Hộp PC" (mua bong theo goi 5, mo rong Hop PC bang xu), NPC Pokedex muc
 * "Hộp PC" (xem, cho di theo, luyen / tien hoa bang Keo loai, tha ve).
 */
public final class BatPokemonGame {

    public static final int LENH = -121;
    public static final byte C_TRANG_THAI = 0, C_NEM = 1, C_POKEDEX = 2, C_XIN_HOP = 3, C_HANH_DONG = 4, C_XIN_LOAI = 5,
            C_TRAO_DOI = 6, C_CHO = 7;
    public static final byte S_TRANG_THAI = 0, S_KET_QUA = 1, S_POKEDEX = 2, S_DONG_HANH = 3, S_HOP_PC = 4, S_LOAI = 5,
            S_TRAO_DOI = 6, S_CHO = 7;

    public static final String K_BAT = "bat.bat";
    public static final String K_NGUONG_HP = "bat.nguong_hp_phan_tram";
    public static final String K_TRAN_NGAY = "bat.tran_ngay";
    public static final String K_TRAN_NEM_NGAY = "bat.tran_nem_ngay";
    public static final String K_CACH_MS = "bat.cach_ms";
    public static final String K_SO_LAN_BO_CHAY = "bat.so_lan_bo_chay";
    public static final String K_SU_KIEN = "bat.su_kien";
    public static final String K_DONG_HANH = "bat.dong_hanh";
    /** Giu toi da bao nhieu con THUONG moi loai khi bat; con thua tu chuyen doi (0 = tat). */
    public static final String K_GIU_MOI_LOAI = "bat.giu_moi_loai";
    /** Tran xu / ngay tu chuyen doi (phan nghin gio thu nhap) va tran luong khoa / ngay. */
    public static final String K_CHUYEN_TRAN_XU = "bat.chuyen_doi.tran_xu_phan_nghin_gio";
    public static final String K_CHUYEN_TRAN_LUONG = "bat.chuyen_doi.tran_luong_ngay";

    /** Npc ao (xem BoNgocGame.NPC_ID): menu bong, Hop PC, mot con, xac nhan tha. */
    public static final int NPC_BONG = -581, NPC_HOP = -582, NPC_CON = -583, NPC_THA = -584, NPC_TIEN_HOA = -585;
    public static final String TEN_MUC_CHANSEY = "Poké Ball & Hộp PC";
    /** Nem vao quai thuong (khong phai Pokemon hoang da). */
    public static final String KHONG_HOANG_DA = "Pokémon này đã thuần hoá, không thể bắt - hãy tìm Pokémon hoang dã (có dấu Poké Ball cạnh tên).";
    public static final String TEN_MUC_POKEDEX = "Hộp PC";
    /** So bong moi lan mua. */
    public static final int GOI_BONG = 5;
    /** Khoang cach nem toi da (px) va thoi gian "vua giao chien" (ms). */
    public static final int TAM_NEM = 160;
    public static final long GIAO_CHIEN_MS = 30_000L;

    private BatPokemonGame() {
    }

    static boolean bat() {
        return LauDai.bat() && LauDai.so(K_BAT, 1) != 0;
    }

    static int nguongHp() {
        return Math.max(1, Math.min(100, LauDai.so(K_NGUONG_HP, BatPokemon.NGUONG_HP_PHAN_TRAM)));
    }

    static int tranNgay() {
        return Math.max(0, LauDai.so(K_TRAN_NGAY, 30));
    }

    static int tranNem() {
        return Math.max(0, LauDai.so(K_TRAN_NEM_NGAY, 150));
    }

    // ================================================================== du lieu

    static volatile HashMap<Integer, BatPokemon.Loai> LOAI = new HashMap<Integer, BatPokemon.Loai>();
    static volatile List<BatPokemon.Bong> BONG = BatPokemon.bongMacDinh();
    /** Thuong chuyen doi theo bac hiem 0..3 (data_bat_chuyen_doi). */
    static volatile com.htbb.pokemon.TraoDoiPokemon.ThuongChuyen[] CHUYEN = com.htbb.pokemon.TraoDoiPokemon.chuyenMacDinh();

    /** BoNgocGame.nap goi (cung lan nap, cung luong theo doi chu ky). */
    static String nap() {
        final List<Object[]> b = DatabaseBoNgoc.docBang("SELECT ma, ten, thu_tu, he_so_phan_tram, tran_phan_nghin, gia_phan_nghin_gio,"
                + " cap_mo FROM data_bat_bong WHERE bat = 1 ORDER BY thu_tu", 7);
        if (b != null && !b.isEmpty()) {
            final List<BatPokemon.Bong> v = new ArrayList<BatPokemon.Bong>();
            for (final Object[] r : b) {
                v.add(new BatPokemon.Bong(DatabaseBoNgoc.chu(r[0]), DatabaseBoNgoc.chu(r[1]), DatabaseBoNgoc.so(r[2]),
                        DatabaseBoNgoc.so(r[3]), DatabaseBoNgoc.so(r[4]), DatabaseBoNgoc.so(r[5]), DatabaseBoNgoc.so(r[6])));
            }
            BONG = Collections.unmodifiableList(v);
        }
        final List<Object[]> l = DatabaseBoNgoc.docBang("SELECT dex, ten, do_hiem, ti_le_phan_nghin, tien_hoa_thanh, cap_tien_hoa,"
                + " keo_tien_hoa, bat_duoc, chi_su_kien FROM data_bat_loai", 9);
        if (l != null && !l.isEmpty()) {
            final HashMap<Integer, BatPokemon.Loai> m = new HashMap<Integer, BatPokemon.Loai>();
            for (final Object[] r : l) {
                final BatPokemon.Loai x = new BatPokemon.Loai(DatabaseBoNgoc.so(r[0]), DatabaseBoNgoc.chu(r[1]), DatabaseBoNgoc.so(r[2]),
                        DatabaseBoNgoc.so(r[3]), DatabaseBoNgoc.so(r[4]), DatabaseBoNgoc.so(r[5]), DatabaseBoNgoc.so(r[6]),
                        DatabaseBoNgoc.so(r[7]) != 0, DatabaseBoNgoc.so(r[8]) != 0);
                if (x.dex > 0) {
                    m.put(Integer.valueOf(x.dex), x);
                }
            }
            // 0.9.8 trao doi (sql/htbb.sql phan 11): tien hoa khi trao doi + chi so goc loai. Bang / cot chua co: bo qua.
            final java.util.Set<Integer> traoDoi = new java.util.HashSet<Integer>();
            final List<Object[]> td = DatabaseBoNgoc.docBang("SELECT dex, tien_hoa_trao_doi FROM data_bat_loai WHERE tien_hoa_trao_doi = 1", 2);
            if (td != null) {
                for (final Object[] r : td) {
                    traoDoi.add(Integer.valueOf(DatabaseBoNgoc.so(r[0])));
                }
            }
            final HashMap<Integer, int[]> goc = new HashMap<Integer, int[]>();
            final List<Object[]> cs = DatabaseBoNgoc.docBang("SELECT dex, hp, tan_cong, phong_thu, tan_cong_db, phong_thu_db, toc_do"
                    + " FROM data_bat_chi_so_loai", 7);
            if (cs != null) {
                for (final Object[] r : cs) {
                    goc.put(Integer.valueOf(DatabaseBoNgoc.so(r[0])), new int[]{DatabaseBoNgoc.so(r[1]), DatabaseBoNgoc.so(r[2]),
                        DatabaseBoNgoc.so(r[3]), DatabaseBoNgoc.so(r[4]), DatabaseBoNgoc.so(r[5]), DatabaseBoNgoc.so(r[6])});
                }
            }
            if (!traoDoi.isEmpty() || !goc.isEmpty()) {
                for (final java.util.Map.Entry<Integer, BatPokemon.Loai> e : m.entrySet()) {
                    final BatPokemon.Loai x = e.getValue();
                    e.setValue(new BatPokemon.Loai(x.dex, x.ten, x.doHiem, x.tiLePhanNghin == BatPokemon.GOC_THEO_DO_HIEM[x.doHiem] ? 0
                            : x.tiLePhanNghin, x.tienHoaThanh, x.capTienHoa, x.keoTienHoa, x.batDuoc, x.chiSuKien,
                            traoDoi.contains(e.getKey()), goc.get(e.getKey())));
                }
            }
            LOAI = m;
        }
        final List<Object[]> cd = DatabaseBoNgoc.docBang("SELECT do_hiem, keo, xu_phan_nghin_gio, luong_khoa FROM data_bat_chuyen_doi"
                + " ORDER BY do_hiem", 4);
        if (cd != null && !cd.isEmpty()) {
            final com.htbb.pokemon.TraoDoiPokemon.ThuongChuyen[] t = com.htbb.pokemon.TraoDoiPokemon.chuyenMacDinh();
            for (final Object[] r : cd) {
                final int h = DatabaseBoNgoc.so(r[0]);
                if (h >= 0 && h < t.length) {
                    t[h] = new com.htbb.pokemon.TraoDoiPokemon.ThuongChuyen(DatabaseBoNgoc.so(r[1]), DatabaseBoNgoc.so(r[2]),
                            DatabaseBoNgoc.so(r[3]));
                }
            }
            CHUYEN = t;
        }
        return BONG.size() + " loại bóng, " + LOAI.size() + " loài thu phục được";
    }

    /** Loai cua dex; bang trong (CSDL cu) thi moi quai thuong deu la loai thuong khong tien hoa. */
    static BatPokemon.Loai loai(final int dex) {
        final HashMap<Integer, BatPokemon.Loai> m = LOAI;
        if (m.isEmpty()) {
            return dex > 0 ? new BatPokemon.Loai(dex, "", 0, 0, 0, 0, 0, true, false) : null;
        }
        return m.get(Integer.valueOf(dex));
    }

    static BatPokemon.Bong bong(final int thuTu) {
        for (final BatPokemon.Bong b : BONG) {
            if (b.thuTu == thuTu) {
                return b;
            }
        }
        return null;
    }

    static int goc(final int dex) {
        return BatPokemon.goc(dex, LOAI);
    }

    static String ten(final int dex) {
        final BatPokemon.Loai l = LOAI.get(Integer.valueOf(dex));
        if (l != null && !l.ten.isEmpty()) {
            return l.ten;
        }
        return PokedexPokemon.tenLoai(dex);
    }

    // ================================================================== Hop PC (ho so)

    private static final ConcurrentHashMap<Integer, HopPc> HOP = new ConcurrentHashMap<Integer, HopPc>();

    static HopPc hop(final Char p) {
        if (p == null || p.charDBID <= 0) {
            return null;
        }
        final Integer k = Integer.valueOf(p.charDBID);
        HopPc h = HOP.get(k);
        if (h != null) {
            return h;
        }
        final String s = DatabaseBoNgoc.napHoSo(DatabaseBoNgoc.BANG_HOP_PC, p.charDBID);
        if (s == null) {
            return null;
        }
        h = HopPc.giaiMa(s);
        final HopPc cu = HOP.putIfAbsent(k, h);
        return cu != null ? cu : h;
    }

    /** Database.saveChar / saveCharAuto. */
    public static void luu(final Char p) {
        if (p == null || p.charDBID <= 0) {
            return;
        }
        final HopPc h = HOP.get(Integer.valueOf(p.charDBID));
        if (h != null && h.ban) {
            // GIU KHOA hop qua ca ma hoa + ghi: trao doi (TraoDoiPokemon.thucHien) ghi CSDL trong cung khoa, nen mot lan luu
            // nhan vat khong bao gio ghi de ban CU len ban da trao doi (chong nhan ban Pokemon).
            synchronized (h) {
                if (h.ban) {
                    h.ban = false;
                    if (!DatabaseBoNgoc.luuHoSo(DatabaseBoNgoc.BANG_HOP_PC, p.charDBID, h.maHoa())) {
                        h.ban = true;
                    }
                }
            }
        }
    }

    static int homNay() {
        final Calendar c = com.htbb.pokemon.DongHo.lich();
        return c.get(Calendar.YEAR) * 10000 + (c.get(Calendar.MONTH) + 1) * 100 + c.get(Calendar.DAY_OF_MONTH);
    }

    // ================================================================== giao chien (ai vua danh con nao)

    /** quai -&gt; {charDBID, ms lan danh cuoi, so lan truot bong}. */
    private static final WeakHashMap<Monster, long[]> GIAO_CHIEN = new WeakHashMap<Monster, long[]>();
    /** charDBID -&gt; ms lan nem cuoi (nhip nem). */
    private static final ConcurrentHashMap<Integer, Long> NEM_CUOI = new ConcurrentHashMap<Integer, Long>();

    /** PokedexPokemon.khiDanh (moi don danh trung quai). */
    public static void khiDanh(final Char p, final LiveActor a) {
        if (!(a instanceof Monster) || p == null || p.charDBID <= 0) {
            return;
        }
        synchronized (GIAO_CHIEN) {
            final Monster m = (Monster) a;
            final long[] g = GIAO_CHIEN.get(m);
            if (g == null || g[0] != p.charDBID) {
                GIAO_CHIEN.put(m, new long[]{p.charDBID, System.currentTimeMillis(), 0});
            } else {
                g[1] = System.currentTimeMillis();
            }
        }
    }

    // ================================================================== goi tin

    public static void xuLy(final Char p, final Message msg) {
        try {
            final byte kieu = msg.dis.readByte();
            if (kieu == C_TRANG_THAI) {
                if (msg.dis.available() > 0 && msg.dis.readByte() >= 1 && p != null) {
                    CO_UI.add(Integer.valueOf(p.charDBID)); // client 0.9.8 co man Hop PC / trao doi
                }
                guiTrangThai(p);
            } else if (kieu == C_XIN_HOP) {
                CO_UI.add(Integer.valueOf(p.charDBID));
                guiHop(p);
            } else if (kieu == C_HANH_DONG) {
                final int hd = msg.dis.readByte();
                final int id = msg.dis.readInt();
                hanhDong(p, hd, id);
            } else if (kieu == C_XIN_LOAI) {
                guiLoai(p, msg.dis.readShort());
            } else if (kieu == C_TRAO_DOI) {
                TraoDoiPokemonGame.xuLy(p, msg);
            } else if (kieu == C_CHO) {
                ChoPokemonGame.xuLy(p, msg);
            } else if (kieu == C_NEM) {
                final short id = msg.dis.readShort();
                final int thuTu = msg.dis.readByte();
                nem(p, id, thuTu);
            } else if (kieu == C_POKEDEX) {
                guiPokedex(p);
            }
        } catch (final Exception e) {
            // goi hong: bo qua
        }
    }

    static Message moi(final byte kieu) throws java.io.IOException {
        final Message m = new Message(LENH);
        m.dos.writeByte(kieu);
        return m;
    }

    static void guiTrangThai(final Char p) {
        if (!BoNgocGame.coClient(p) || !bat()) {
            return;
        }
        final HopPc h = hop(p);
        if (h == null) {
            return;
        }
        h.doiNgay(homNay());
        ChoPokemonGame.khiVao(p, false); // [cho-pokemon] tra con het han + nhan tien ban (nhip 60 giay)
        try {
            final Message m = moi(S_TRANG_THAI);
            final DataOutputStream o = m.dos;
            o.writeByte(nguongHp());
            final List<BatPokemon.Bong> ds = BONG;
            o.writeByte(ds.size());
            for (final BatPokemon.Bong b : ds) {
                o.writeByte(b.thuTu);
                o.writeUTF(b.ten);
                o.writeShort(Math.min(Short.MAX_VALUE, h.bong(b.ma)));
                o.writeShort(b.tranPhanNghin);
            }
            o.writeShort(h.batNgay);
            o.writeShort(tranNgay());
            o.writeShort(h.soCon());
            o.writeShort(h.sucChua());
            p.sendMessage(m);
        } catch (final Exception ignored) {
        }
    }

    static void guiPokedex(final Char p) {
        if (!BoNgocGame.coClient(p)) {
            return;
        }
        final Pokedex d = PokedexPokemon.lay(p);
        if (d == null) {
            return;
        }
        try {
            final List<int[]> ds = d.trangThai();
            final Message m = moi(S_POKEDEX);
            final int n = Math.min(2000, ds.size());
            m.dos.writeShort(n);
            for (int i = 0; i < n; i++) {
                m.dos.writeShort(ds.get(i)[0]);
                m.dos.writeByte(ds.get(i)[1]);
            }
            m.dos.writeShort(d.soLoaiDaBat());
            p.sendMessage(m);
        } catch (final Exception ignored) {
        }
    }

    static void ketQuaNem(final Char p, final int idQuai, final int thuTu, final int kq, final int lac, final int dex,
            final boolean shiny, final int tiLe, final String chu, final boolean choNguoiGan) {
        try {
            final Message m = moi(S_KET_QUA);
            m.dos.writeShort(p.id);
            m.dos.writeShort(idQuai);
            m.dos.writeByte(thuTu);
            m.dos.writeByte(kq);
            m.dos.writeByte(lac);
            m.dos.writeShort(dex);
            m.dos.writeByte(shiny ? 1 : 0);
            m.dos.writeShort(tiLe);
            m.dos.writeUTF(chu == null ? "" : chu);
            if (BoNgocGame.coClient(p)) {
                p.sendMessage(m);
            }
            if (choNguoiGan) {
                p.sendToNearPlayer(m);
            }
        } catch (final Exception ignored) {
        }
    }

    // ================================================================== nem bong

    /** Ly do tu choi (khong tru bong) - tra chu, gui thong bao loi. */
    static void tuChoi(final Char p, final int idQuai, final int thuTu, final String chu) {
        com.htbb.thongbao.ThongBao.loi(p, chu, "");
        ketQuaNem(p, idQuai, thuTu, 3, 0, 0, false, 0, chu, false);
    }

    static void nem(final Char p, final short idQuai, final int thuTu) {
        if (p == null || p.map == null) {
            return;
        }
        if (!bat()) {
            tuChoi(p, idQuai, thuTu, "Thu phục Pokémon chưa mở.");
            return;
        }
        if (!BoNgocGame.coClient(p)) {
            return;
        }
        final long bay = System.currentTimeMillis();
        final Integer k = Integer.valueOf(p.charDBID);
        final Long cuoi = NEM_CUOI.get(k);
        final long cach = Math.max(300L, LauDai.so(K_CACH_MS, 1500));
        if (cuoi != null && bay - cuoi.longValue() < cach) {
            return; // nhip nem: bo qua im lang (chong bot spam goi)
        }
        NEM_CUOI.put(k, Long.valueOf(bay));
        final HopPc h = hop(p);
        if (h == null) {
            tuChoi(p, idQuai, thuTu, "Hộp PC tạm thời không đọc được, thử lại sau.");
            return;
        }
        h.doiNgay(homNay());
        if (h.nemNgay >= tranNem()) {
            tuChoi(p, idQuai, thuTu, "Hôm nay bạn đã ném đủ " + tranNem() + " quả bóng.");
            return;
        }
        if (h.batNgay >= tranNgay()) {
            tuChoi(p, idQuai, thuTu, "Hôm nay bạn đã thu phục đủ " + tranNgay() + " Pokémon.");
            return;
        }
        final BatPokemon.Bong b = bong(thuTu);
        if (b == null || h.bong(b.ma) <= 0) {
            tuChoi(p, idQuai, thuTu, "Bạn không còn " + (b != null ? b.ten : "bóng") + ". Mua ở Y tá Chansey.");
            return;
        }
        final Monster m = p.map.getMonster(idQuai, p.inCountry, p.region);
        if (m == null || m.isDead || m.hp <= 0) {
            tuChoi(p, idQuai, thuTu, "Không thấy Pokémon đó.");
            return;
        }
        if (Math.abs(m.x - p.x) > TAM_NEM || Math.abs(m.y - p.y) > TAM_NEM) {
            tuChoi(p, idQuai, thuTu, "Pokémon ở quá xa, hãy lại gần hơn.");
            return;
        }
        if (!LoaiQuaiGame.laHoangDa(m)) {
            // 0.9.8 (chu game): quai thuong (mau ban do, muc tieu nhiem vu, quai cay) KHONG bat duoc - chi Pokemon hoang da
            tuChoi(p, idQuai, thuTu, KHONG_HOANG_DA);
            return;
        }
        final long[] g;
        synchronized (GIAO_CHIEN) {
            g = GIAO_CHIEN.get(m);
        }
        if (g == null || g[0] != p.charDBID || bay - g[1] > GIAO_CHIEN_MS) {
            tuChoi(p, idQuai, thuTu, "Hãy tự giao chiến với Pokémon này trước khi ném bóng.");
            return;
        }
        final MonsterTemplate t = Map.monsterTemplates.get(Integer.valueOf(m.idTemplate));
        // loai RIENG cua con quai (loai-day-du, client >= loai.ban_client thay loai nay); client cu: loai cua mau
        final int dex = PokedexPokemon.dexQuai(p, m);
        final boolean laBoss = m.isBoss || m.isBoss() || m.isMaterialMons() || m.isMonsterVantieu() || m.isTruMoba()
                || (t != null && t.maxhp >= 10_000_000);
        final BatPokemon.Loai l = loai(dex);
        final int capNv = p.lvDetail != null ? p.lvDetail.lv : 1;
        // Hop day: van bat duoc neu loai nay da du so con giu (con moi / con kem nhat se tu chuyen doi thanh Keo)
        // shiny: mau shiny (template) HOAC con quai mang loai rieng sac mau ma nguoi nay thay (client moi, LoaiQuaiGame)
        final boolean shiny = t != null && t.shiny != 0 || LoaiQuaiGame.shinyChoNguoi(p, m) && dex == m.dexRieng;
        if (h.day() && (shiny || giuMoiLoai() <= 0 || h.soCungLoai(dex, true) < giuMoiLoai())) {
            tuChoi(p, idQuai, thuTu, "Hộp PC đã đầy (" + h.sucChua() + " ô). Chuyển đổi, thả bớt hoặc mở rộng ở Chansey.");
            return;
        }
        final int ma = BatPokemon.kiem(l, laBoss, m.hp, m.maxhp, m.level, capNv, nguongHp(), LauDai.so(K_SU_KIEN, 0) != 0);
        if (ma != BatPokemon.OK) {
            tuChoi(p, idQuai, thuTu, BatPokemon.chuKiem(ma, nguongHp()));
            return;
        }
        final boolean alpha = NoiDungLauDai.laAlpha(m);
        final int lanTruot = (int) g[2];
        final int tiLe = BatPokemon.tiLe(l, m.hp, m.maxhp, m.level, capNv, alpha, b, lanTruot);
        synchronized (h) {
            if (!h.truBong(b.ma)) {
                tuChoi(p, idQuai, thuTu, "Bạn không còn " + b.ten + ".");
                return;
            }
            h.nemNgay++;
        }
        final int roll = Map.r.nextInt(1000);
        final boolean duoc = BatPokemon.thanhCong(roll, tiLe);
        final int lac = BatPokemon.soLanLac(roll, tiLe, duoc);
        final String tenLoai = ten(dex) + (shiny ? " sắc màu" : "");
        if (duoc) {
            final int iv = com.htbb.pokemon.CaThePokemon.ivNgauNhien(Map.r);
            final int tc = com.htbb.pokemon.CaThePokemon.tinhCachNgauNhien(Map.r);
            String chuyen = "";
            synchronized (h) {
                h.batNgay++;
                h.tongBat++;
                h.themKeo(goc(dex), BatPokemon.keoKhiBat(shiny));
                // con trung loai qua so giu: chuyen doi con kem nhat (con moi hoac con cu IV thap hon), giu con tot
                final int idChuyen = com.htbb.pokemon.TraoDoiPokemon.chonChuyenKhiBat(h, dex, shiny, iv, giuMoiLoai(), p.charDBID);
                final int cap = Math.min(m.level, BatPokemon.capToiDa(capNv));
                if (idChuyen == 0) {
                    final HopPc.Con tam = new HopPc.Con(0, dex, cap, false, homNay(), b.ma);
                    tam.iv = iv;
                    tam.tinhCach = tc;
                    tam.chuGoc = p.charDBID;
                    chuyen = chuyenDoi(p, h, tam, true);
                } else {
                    if (idChuyen > 0) {
                        final HopPc.Con cu = h.lay(idChuyen);
                        if (cu != null) {
                            h.tha(idChuyen);
                            chuyen = chuyenDoi(p, h, cu, true);
                        }
                    }
                    h.them(dex, cap, shiny, homNay(), b.ma, iv, tc, p.charDBID);
                }
            }
            m.actorDie(); // bien mat nhu bi ha, KHONG roi do / xu / EXP them (EXP da nhan theo sat thuong)
            synchronized (GIAO_CHIEN) {
                GIAO_CHIEN.remove(m);
            }
            final Pokedex d = PokedexPokemon.lay(p);
            final boolean dau = d != null && d.ghiBat(dex, shiny, bay / 1000L);
            NhiemVuEngine.suKien(p, "bat_pokemon", String.valueOf(dex), 1);
            final String chu = "Đã thu phục " + tenLoai + "!";
            com.htbb.thongbao.ThongBao.gui(p, com.htbb.thongbao.ThongBao.HIEM, chu,
                    (dau ? "Pokédex: loài mới đã bắt · " : "") + "+" + BatPokemon.keoKhiBat(shiny) + " Kẹo " + ten(goc(dex))
                            + " · " + com.htbb.pokemon.CaThePokemon.chuDanhGia(iv) + " " + com.htbb.pokemon.CaThePokemon.phanTram(iv)
                            + "% · " + com.htbb.pokemon.CaThePokemon.tenTinhCach(tc)
                            + (chuyen.isEmpty() ? "" : " · " + chuyen) + " · Hộp PC " + h.soCon() + "/" + h.sucChua(),
                    com.htbb.thongbao.ThongBao.HINH_POKEMON, dex);
            ketQuaNem(p, idQuai, thuTu, 1, lac, dex, shiny, tiLe, chu, true);
            if (shiny) {
                com.htbb.chongspam.ThongBaoGop.dua("nhan_thuong", p.charname + " vừa thu phục " + tenLoai + "!");
            }
        } else {
            final int truotSau = lanTruot + 1;
            final boolean chay = BatPokemon.boChay(truotSau, Math.max(1, LauDai.so(K_SO_LAN_BO_CHAY, BatPokemon.SO_LAN_BO_CHAY)));
            synchronized (GIAO_CHIEN) {
                g[2] = truotSau;
                if (chay) {
                    GIAO_CHIEN.remove(m);
                }
            }
            final String chu;
            if (chay) {
                m.actorDie(); // bo chay: bien mat, hoi sinh nhu thuong, khong thuong
                chu = tenLoai + " đã bỏ chạy!";
                com.htbb.thongbao.ThongBao.gui(p, com.htbb.thongbao.ThongBao.TIN, chu, "Tỉ lệ lần vừa rồi " + tiLe / 10 + "%",
                        com.htbb.thongbao.ThongBao.HINH_POKEMON, dex);
            } else {
                chu = tenLoai + " thoát khỏi bóng!";
                com.htbb.thongbao.ThongBao.gui(p, com.htbb.thongbao.ThongBao.TIN, chu,
                        "Tỉ lệ " + tiLe / 10 + "% · lần sau cao hơn · còn "
                                + (Math.max(1, LauDai.so(K_SO_LAN_BO_CHAY, BatPokemon.SO_LAN_BO_CHAY)) - truotSau) + " lần trước khi nó bỏ chạy",
                        com.htbb.thongbao.ThongBao.HINH_POKEMON, dex);
            }
            ketQuaNem(p, idQuai, thuTu, chay ? 2 : 0, lac, dex, shiny, tiLe, chu, true);
        }
        guiTrangThai(p);
        try {
            data.Database.instance.saveOrtherLog("", p.charname, "nem " + b.ma + " quai " + m.idTemplate + " dex " + dex + " cap " + m.level
                    + " hp " + m.hp + "/" + m.maxhp + " tl " + tiLe + " roll " + roll + (duoc ? " BAT" : " truot"), "batpokemon");
        } catch (final Exception ignored) {
        }
    }

    static int giuMoiLoai() {
        return Math.max(0, LauDai.so(K_GIU_MOI_LOAI, 2));
    }

    static com.htbb.pokemon.TraoDoiPokemon.ThuongChuyen thuongChuyen(final int dex) {
        final BatPokemon.Loai l = LOAI.get(Integer.valueOf(dex));
        final com.htbb.pokemon.TraoDoiPokemon.ThuongChuyen[] t = CHUYEN;
        return t[Math.max(0, Math.min(t.length - 1, l != null ? l.doHiem : 0))];
    }

    /**
     * CHUYEN DOI mot con (da roi khoi hop hoac con tam vua bat): + Keo loai, + xu / luong khoa nho theo bac hiem (tran
     * ngay; con nhan qua trao doi chi ra Keo). Goi trong khoa h. Tra chu tom tat.
     */
    static String chuyenDoi(final Char p, final HopPc h, final HopPc.Con c, final boolean tuDong) {
        final com.htbb.pokemon.TraoDoiPokemon.DaChuyen d = com.htbb.pokemon.TraoDoiPokemon.tinhChuyen(h, c, p.charDBID,
                thuongChuyen(c.dex), BoNgocGame.xuMoiGio(p), LauDai.so(K_CHUYEN_TRAN_XU, 300), LauDai.so(K_CHUYEN_TRAN_LUONG, 1), homNay());
        h.themKeo(goc(c.dex), d.keo);
        if (d.xu > 0) {
            p.addXu(d.xu, "chuyen doi pokemon " + c.dex); // addXu tu dem xu_vao
        }
        if (d.luongKhoa > 0) {
            p.addLuongLock(d.luongKhoa);
        }
        try {
            data.Database.instance.saveOrtherLog("", p.charname, (tuDong ? "tu " : "") + "chuyen doi dex " + c.dex + " cap " + c.cap
                    + " iv " + com.htbb.pokemon.CaThePokemon.phanTram(c.iv) + " keo " + d.keo + " xu " + d.xu + " luong " + d.luongKhoa,
                    "batpokemon");
        } catch (final Exception ignored) {
        }
        return (tuDong ? "Tự chuyển đổi " : "Chuyển đổi ") + ten(c.dex) + " (" + com.htbb.pokemon.CaThePokemon.phanTram(c.iv) + "%): +"
                + d.keo + " Kẹo" + (d.xu > 0 ? ", +" + xu(d.xu) : "") + (d.luongKhoa > 0 ? ", +" + d.luongKhoa + " lượng khoá" : "");
    }

    // ================================================================== ban dong hanh (chi de nhin)

    /** -121 kieu 3 cho nhan vat {@code ch} (null neu tat / khong co). Noi vao chuoi -90 cua TienHoaPokemon. */
    public static Message taoDongHanh(final Char ch) {
        if (ch == null || !bat() || LauDai.so(K_DONG_HANH, 1) == 0) {
            return null;
        }
        final HopPc h = ch.charDBID > 0 ? HOP.get(Integer.valueOf(ch.charDBID)) : null;
        final HopPc.Con c = h != null ? h.conDangTheo() : null;
        try {
            final Message m = moi(S_DONG_HANH);
            m.dos.writeShort(ch.id);
            m.dos.writeShort(c != null ? c.dex : 0);
            m.dos.writeByte(c != null && c.shiny ? 1 : 0);
            return m;
        } catch (final Exception e) {
            return null;
        }
    }

    static void guiDongHanh(final Char p) {
        final Message m = taoDongHanh(p);
        if (m != null) {
            p.sendMessage(m);
            final Message m2 = taoDongHanh(p);
            if (m2 != null) {
                p.sendToNearPlayer(m2);
            }
        }
    }

    // ================================================================== menu (Chansey / Pokedex)

    static String xu(final long x) {
        return com.htbb.pokemon.TienTeChu.so(x) + " xu";
    }

    /**
     * 0.9.8: TANG bong (nhiem vu "bong:&lt;ma&gt;xN", su kien, ngay cong dong, ruong pho ban, cup tuan) - vao Hop PC (khoa,
     * khong giao dich / khong ban), tran chung {@link BatPokemon#BONG_TANG_NGAY_TOI_DA} Poke Ball quy doi / ngay.
     * @return so bong da tang (0 = het tran / chua mo).
     */
    public static int thuongBong(final Char p, final String ma, final int so, final String lyDo) {
        if (p == null || so <= 0 || !bat()) {
            return 0;
        }
        BatPokemon.Bong b = null, poke = null;
        for (final BatPokemon.Bong x : BONG) {
            if (x.ma.equals(ma)) {
                b = x;
            }
            if (x.thuTu == 1) {
                poke = x;
            }
        }
        final HopPc h = hop(p);
        if (b == null || h == null) {
            return 0;
        }
        final int qd = BatPokemon.quyDoiPhanMuoi(b, poke);
        final int n;
        synchronized (h) {
            h.doiNgay(homNay());
            n = BatPokemon.duocTang(so, qd, h.bongTangNgay);
            if (n > 0) {
                h.bongTangNgay += n * qd;
                h.themBong(b.ma, n);
            }
        }
        if (n > 0) {
            com.htbb.thongbao.ThongBao.ketQua(p, "+" + n + " " + b.ten + " (" + lyDo + ")", "Vào Hộp PC: đánh Pokémon hoang dã (dấu Poké Ball cạnh tên)"
                    + " còn ≤ " + nguongHp() + "% HP rồi bấm nút Poké Ball.");
            guiTrangThai(p);
            try {
                data.Database.instance.saveOrtherLog("", p.charname, "tang bong " + b.ma + " x" + n + " (" + lyDo + ")", "laudai");
            } catch (final Exception ignored) {
            }
        }
        return n;
    }

    /** Y ta Chansey muc "Poké Ball & Hộp PC". */
    public static void moMenuBong(final Char p) {
        if (!bat()) {
            com.htbb.thongbao.ThongBao.loi(p, "Thu phục Pokémon chưa mở.", "");
            return;
        }
        final HopPc h = hop(p);
        if (h == null) {
            com.htbb.thongbao.ThongBao.loi(p, "Hộp PC tạm thời không đọc được, thử lại sau.", "");
            return;
        }
        final long xmg = BoNgocGame.xuMoiGio(p);
        final List<String> m = new ArrayList<String>();
        for (final BatPokemon.Bong b : BONG) {
            m.add("Mua " + GOI_BONG + " " + b.ten + " (" + xu(BatPokemon.giaBong(b, xmg) * GOI_BONG) + ", có " + h.bong(b.ma) + ")");
        }
        m.add("Hộp PC (" + h.soCon() + "/" + h.sucChua() + ")");
        if (h.moRongDuoc()) {
            m.add("Mở rộng Hộp PC +" + HopPc.O_MOI_LAN + " ô (" + xu(h.giaMoRong(xmg)) + ")");
        }
        p.sendMessage(MessageCreator.createMsgMenu(m.toArray(new String[0]), NPC_BONG, 0));
    }

    /** Trang Hop PC dang xem (charDBID -&gt; trang) va con dang chon. */
    private static final ConcurrentHashMap<Integer, int[]> CHON = new ConcurrentHashMap<Integer, int[]>();
    static final int MOI_TRANG = 8;

    static int[] chon(final Char p) {
        return CHON.computeIfAbsent(Integer.valueOf(p.charDBID), z -> new int[]{0, 0});
    }

    /** NPC Pokedex muc "Hộp PC" / Chansey: danh sach con, trang {@code trang}. */
    public static void moHop(final Char p, final int trang) {
        final HopPc h = hop(p);
        if (h == null || !bat()) {
            com.htbb.thongbao.ThongBao.loi(p, "Hộp PC tạm thời không mở được.", "");
            return;
        }
        if (coUi(p)) {
            guiHop(p); // client co man Hop PC (hinh Pokemon, sao danh gia, nut): mo man thay menu chu
            return;
        }
        final List<HopPc.Con> ds = h.danhSach();
        if (ds.isEmpty()) {
            com.htbb.thongbao.ThongBao.gui(p, com.htbb.thongbao.ThongBao.TIN, "Hộp PC đang trống.",
                    "Làm Pokémon hoang dã yếu đi rồi ném bóng để thu phục.", com.htbb.thongbao.ThongBao.HINH_KHONG, 0);
            return;
        }
        final int soTrang = (ds.size() + MOI_TRANG - 1) / MOI_TRANG;
        final int tr = Math.max(0, Math.min(soTrang - 1, trang));
        chon(p)[0] = tr;
        final List<String> m = new ArrayList<String>();
        for (int i = tr * MOI_TRANG; i < Math.min(ds.size(), (tr + 1) * MOI_TRANG); i++) {
            final HopPc.Con c = ds.get(i);
            m.add(ten(c.dex) + (c.shiny ? " (sắc màu)" : "") + " cấp " + c.cap + " " + com.htbb.pokemon.CaThePokemon.sao(c.iv) + " sao"
                    + (h.dangTheo == c.id ? " (đi theo)" : "") + ", kẹo " + h.keo(goc(c.dex)));
        }
        if (tr + 1 < soTrang) {
            m.add("Trang sau (" + (tr + 2) + "/" + soTrang + ")");
        }
        if (tr > 0) {
            m.add("Trang trước");
        }
        p.sendMessage(MessageCreator.createMsgMenu(m.toArray(new String[0]), NPC_HOP, 0));
    }

    static void moCon(final Char p, final HopPc h, final HopPc.Con c) {
        chon(p)[1] = c.id;
        final List<String> m = new ArrayList<String>();
        m.add(h.dangTheo == c.id ? "Thôi đi theo" : "Cho đi theo (chỉ để ngắm)");
        final int keo = h.keo(goc(c.dex));
        final int capTd = BatPokemon.capToiDa(p.lvDetail != null ? p.lvDetail.lv : 1);
        m.add(c.cap >= capTd ? "Luyện cấp (đã tối đa cấp " + capTd + ")"
                : "Luyện lên cấp " + (c.cap + 1) + " (" + BatPokemon.keoLuyen(c.cap) + " kẹo, có " + keo + ")");
        final BatPokemon.Loai l = LOAI.get(Integer.valueOf(c.dex));
        final List<Integer> dich = dichTienHoa(c.dex);
        if (l != null && dich.size() == 1) {
            m.add("Tiến hoá thành " + ten(dich.get(0)) + " (cấp " + l.capTienHoa + ", " + l.keoTienHoa + " kẹo)");
        } else if (l != null && dich.size() > 1) {
            m.add("Tiến hoá (" + dich.size() + " nhánh, cấp " + l.capTienHoa + ", " + l.keoTienHoa + " kẹo)");
        } else {
            m.add("Không tiến hoá thêm");
        }
        m.add("Thả về tự nhiên (+" + BatPokemon.KEO_KHI_THA + " kẹo)");
        if (trungLoai(h, c)) {
            m.add("Chuyển đổi (trùng loài, " + thuongChuyen(c.dex).keo * (c.shiny ? 2 : 1) + " kẹo)");
        }
        p.sendMessage(MessageCreator.createMsgMenu(m.toArray(new String[0]), NPC_CON, 0));
    }

    /** MapDoMenu: idNpc la mot trong NPC_BONG / NPC_HOP / NPC_CON / NPC_THA. true = da xu ly. */
    public static boolean doMenu(final Char p, final int idNpc, final int idMenu, final int idOption) {
        if (idNpc != NPC_BONG && idNpc != NPC_HOP && idNpc != NPC_CON && idNpc != NPC_THA && idNpc != NPC_TIEN_HOA) {
            return false;
        }
        if (p == null || !bat()) {
            return true;
        }
        final HopPc h = hop(p);
        if (h == null) {
            return true;
        }
        synchronized (h) {
            if (idNpc == NPC_BONG) {
                menuBong(p, h, idOption);
            } else if (idNpc == NPC_HOP) {
                final List<HopPc.Con> ds = h.danhSach();
                final int tr = chon(p)[0];
                final int dau = tr * MOI_TRANG;
                final int soTrenTrang = Math.max(0, Math.min(MOI_TRANG, ds.size() - dau));
                if (idOption >= 0 && idOption < soTrenTrang) {
                    moCon(p, h, ds.get(dau + idOption));
                } else {
                    final boolean coSau = dau + MOI_TRANG < ds.size();
                    if (coSau && idOption == soTrenTrang) {
                        moHop(p, tr + 1);
                    } else {
                        moHop(p, tr - 1);
                    }
                }
            } else if (idNpc == NPC_CON) {
                final HopPc.Con c = h.lay(chon(p)[1]);
                if (c != null) {
                    menuCon(p, h, c, idOption);
                }
            } else if (idNpc == NPC_TIEN_HOA) {
                final HopPc.Con c = h.lay(chon(p)[1]);
                final List<Integer> dich = c != null ? dichTienHoa(c.dex) : null;
                if (c != null && idOption >= 0 && idOption < dich.size() && !TraoDoiPokemonGame.dangDua(p, c.id)
                        && tienHoa(p, h, c, dich.get(idOption))) {
                    guiHop(p);
                    moCon(p, h, c);
                }
            } else {
                final HopPc.Con c = h.lay(chon(p)[1]);
                if (c != null && idOption == 0) {
                    lam(p, h, c, H_THA);
                }
            }
        }
        return true;
    }

    static void menuBong(final Char p, final HopPc h, final int idOption) {
        if (!NpcTinhBanDo.oNoiDichVu(p, NpcTinhBanDo.NPC_Y_TA)) {
            com.htbb.thongbao.ThongBao.loi(p, "Hãy đến gần Y tá Chansey.", "");
            return;
        }
        final List<BatPokemon.Bong> ds = BONG;
        final long xmg = BoNgocGame.xuMoiGio(p);
        final int capNv = p.lvDetail != null ? p.lvDetail.lv : 1;
        if (idOption >= 0 && idOption < ds.size()) {
            final BatPokemon.Bong b = ds.get(idOption);
            if (capNv < b.capMo) {
                com.htbb.thongbao.ThongBao.loi(p, b.ten + " mở từ cấp " + b.capMo + ".", "");
                return;
            }
            final long gia = BatPokemon.giaBong(b, xmg) * GOI_BONG;
            if (!p.truNeuDu(() -> p.xu >= gia, () -> p.subXu(gia, true, "mua bong " + b.ma))) {
                com.htbb.thongbao.ThongBao.loi(p, "Cần " + xu(gia) + ".", "");
                return;
            }
            h.themBong(b.ma, GOI_BONG);
            com.htbb.thongbao.ThongBao.ketQua(p, "Mua " + GOI_BONG + " " + b.ten + ".", "Đang có " + h.bong(b.ma));
            guiTrangThai(p);
            moMenuBong(p);
        } else if (idOption == ds.size()) {
            moHop(p, 0);
        } else if (idOption == ds.size() + 1 && h.moRongDuoc()) {
            final long gia = h.giaMoRong(xmg);
            if (!p.truNeuDu(() -> p.xu >= gia, () -> p.subXu(gia, true, "mo rong hop pc"))) {
                com.htbb.thongbao.ThongBao.loi(p, "Mở rộng cần " + xu(gia) + ".", "");
                return;
            }
            h.moRong();
            com.htbb.thongbao.ThongBao.ketQua(p, "Hộp PC: " + h.sucChua() + " ô.", "");
            guiTrangThai(p);
        }
    }

    static void menuCon(final Char p, final HopPc h, final HopPc.Con c, final int idOption) {
        switch (idOption) {
            case 0:
                lam(p, h, c, H_THEO);
                return;
            case 1:
                if (lam(p, h, c, H_LUYEN)) {
                    moCon(p, h, c);
                }
                return;
            case 2:
                if (lam(p, h, c, H_TIEN_HOA)) {
                    moCon(p, h, c);
                }
                return;
            case 3:
                p.sendMessage(MessageCreator.createMsgMenu(new String[]{"Thả " + ten(c.dex) + " cấp " + c.cap, "Thôi"}, NPC_THA, 0));
                return;
            case 4:
                lam(p, h, c, H_CHUYEN);
                return;
            default:
                return;
        }
    }

    /** Hanh dong tren mot con (goi -121 kieu 4 va menu NPC). */
    public static final int H_THEO = 0, H_LUYEN = 1, H_TIEN_HOA = 2, H_THA = 3, H_CHUYEN = 4;

    /** Con trung loai (chuyen doi duoc): hop con it nhat mot con khac cung loai. */
    static boolean trungLoai(final HopPc h, final HopPc.Con c) {
        return h.soCungLoai(c.dex, false) >= 2;
    }

    /** Lam mot hanh dong (trong khoa h). true = da doi. */
    static boolean lam(final Char p, final HopPc h, final HopPc.Con c, final int hd) {
        final int capNv = p.lvDetail != null ? p.lvDetail.lv : 1;
        if (TraoDoiPokemonGame.dangDua(p, c.id)) {
            com.htbb.thongbao.ThongBao.loi(p, ten(c.dex) + " đang nằm trong một cuộc trao đổi.", "Huỷ trao đổi trước.");
            return false;
        }
        switch (hd) {
            case H_THEO:
                h.datTheo(h.dangTheo == c.id ? 0 : c.id);
                com.htbb.thongbao.ThongBao.ketQua(p, h.dangTheo == c.id ? ten(c.dex) + " đi theo bạn." : "Không còn Pokémon đi theo.", "");
                guiDongHanh(p);
                return true;
            case H_LUYEN: {
                if (c.cap >= BatPokemon.capToiDa(capNv)) {
                    com.htbb.thongbao.ThongBao.loi(p, "Cấp tối đa bằng cấp của bạn (cấp " + BatPokemon.capToiDa(capNv) + ").", "");
                    return false;
                }
                final int can = BatPokemon.keoLuyen(c.cap);
                if (!h.truKeo(goc(c.dex), can)) {
                    com.htbb.thongbao.ThongBao.loi(p, "Cần " + can + " Kẹo " + ten(goc(c.dex)) + ".", "");
                    return false;
                }
                c.cap++;
                h.danhDau();
                com.htbb.thongbao.ThongBao.ketQua(p, ten(c.dex) + " lên cấp " + c.cap + ".", "Còn " + h.keo(goc(c.dex)) + " kẹo");
                return true;
            }
            case H_TIEN_HOA: {
                final List<Integer> dich = dichTienHoa(c.dex);
                if (dich.size() > 1) {
                    // nhieu nhanh (Eevee, Gloom, Poliwhirl...): chon loai dich
                    chon(p)[1] = c.id;
                    final BatPokemon.Loai l = LOAI.get(Integer.valueOf(c.dex));
                    final String[] m = new String[dich.size()];
                    for (int i = 0; i < m.length; i++) {
                        m[i] = "Tiến hoá thành " + ten(dich.get(i)) + (l != null ? " (cấp " + l.capTienHoa + ", " + l.keoTienHoa + " kẹo)" : "");
                    }
                    p.sendMessage(MessageCreator.createMsgMenu(m, NPC_TIEN_HOA, 0));
                    return false;
                }
                return tienHoa(p, h, c, dich.isEmpty() ? 0 : dich.get(0));
            }
            case H_THA:
                h.tha(c.id);
                h.themKeo(goc(c.dex), BatPokemon.KEO_KHI_THA);
                com.htbb.thongbao.ThongBao.gui(p, com.htbb.thongbao.ThongBao.KET_QUA, "Đã thả " + ten(c.dex) + " về tự nhiên.",
                        "+" + BatPokemon.KEO_KHI_THA + " Kẹo " + ten(goc(c.dex)), com.htbb.thongbao.ThongBao.HINH_POKEMON, c.dex);
                guiDongHanh(p);
                guiTrangThai(p);
                return true;
            case H_CHUYEN: {
                if (!trungLoai(h, c)) {
                    com.htbb.thongbao.ThongBao.loi(p, "Chỉ chuyển đổi được Pokémon trùng loài.", "Con duy nhất của loài: dùng Thả về tự nhiên.");
                    return false;
                }
                if (h.dangTheo == c.id) {
                    com.htbb.thongbao.ThongBao.loi(p, ten(c.dex) + " đang đi theo bạn.", "Chọn Thôi đi theo trước.");
                    return false;
                }
                h.tha(c.id);
                final String chu = chuyenDoi(p, h, c, false);
                com.htbb.thongbao.ThongBao.gui(p, com.htbb.thongbao.ThongBao.KET_QUA, chu, "Kẹo dùng để luyện cấp và tiến hoá cả họ "
                        + ten(goc(c.dex)), com.htbb.thongbao.ThongBao.HINH_POKEMON, c.dex);
                guiTrangThai(p);
                return true;
            }
            default:
                return false;
        }
    }

    // ================================================================== Hop PC / Pokedex chi tiet (goi -121 kieu 3..5)

    /** Client da co man Hop PC moi (gui kem byte trong goi trang thai) - charDBID. */
    static final java.util.Set<Integer> CO_UI = java.util.Collections.newSetFromMap(new ConcurrentHashMap<Integer, Boolean>());

    /** Client co man Hop PC / trao doi / cho: moi client &gt;= 908 (ban 908 phat hanh kem cac man nay). */
    static boolean coUi(final Char p) {
        return BoNgocGame.coClient(p);
    }

    /** S-&gt;C kieu 4: toan bo Hop PC (client ManHopPc). */
    static void guiHop(final Char p) {
        final HopPc h = hop(p);
        if (h == null || !BoNgocGame.coClient(p)) {
            return;
        }
        final int capNv = p.lvDetail != null ? p.lvDetail.lv : 1;
        final long xmg = BoNgocGame.xuMoiGio(p);
        final long bayGiay = System.currentTimeMillis() / 1000L;
        try {
            final Message m = moi(S_HOP_PC);
            final DataOutputStream o = m.dos;
            synchronized (h) {
                h.doiNgay(homNay());
                final List<HopPc.Con> ds = h.danhSach();
                o.writeShort(h.soCon());
                o.writeShort(h.sucChua());
                o.writeInt(h.dangTheo);
                o.writeShort(giuMoiLoai());
                o.writeShort(ds.size());
                for (final HopPc.Con c : ds) {
                    final BatPokemon.Loai l = LOAI.get(Integer.valueOf(c.dex));
                    o.writeInt(c.id);
                    o.writeShort(c.dex);
                    o.writeByte(c.cap);
                    o.writeByte(c.shiny ? 1 : 0);
                    o.writeByte(com.htbb.pokemon.CaThePokemon.sao(c.iv));
                    o.writeByte(com.htbb.pokemon.CaThePokemon.phanTram(c.iv));
                    o.writeShort(Math.min(Short.MAX_VALUE, com.htbb.pokemon.CaThePokemon.sucManh(l != null ? l.chiSoGoc : null, c.iv, c.tinhCach, c.cap)));
                    o.writeByte(c.tinhCach);
                    o.writeByte(l != null ? l.doHiem : 0);
                    o.writeShort(Math.min(Short.MAX_VALUE, h.keo(goc(c.dex))));
                    o.writeShort(c.cap >= BatPokemon.capToiDa(capNv) ? 0 : BatPokemon.keoLuyen(c.cap));
                    final List<Integer> dich = dichTienHoa(c.dex); // nhanh mo dau tien (nhieu nhanh: may chu hoi chon)
                    o.writeShort(l != null && !dich.isEmpty() ? dich.get(0) : 0);
                    o.writeByte(l != null ? l.capTienHoa : 0);
                    o.writeShort(l != null ? l.keoTienHoa : 0);
                    int co = 0;
                    if (trungLoai(h, c)) {
                        co |= 1;
                    }
                    if (c.doiLuc > 0 && bayGiay - c.doiLuc < TraoDoiPokemonGame.luat().khoaGiay) {
                        co |= 2;
                    }
                    if (c.chuGoc != p.charDBID) {
                        co |= 4;
                    }
                    if (l != null && l.tienHoaTraoDoi) {
                        co |= 8;
                    }
                    if (h.dangTheo == c.id) {
                        co |= 16;
                    }
                    o.writeByte(co);
                    for (int i = 0; i < 6; i++) {
                        o.writeByte(com.htbb.pokemon.CaThePokemon.iv(c.iv, i));
                    }
                    o.writeUTF(ten(c.dex));
                    o.writeUTF(com.htbb.pokemon.CaThePokemon.chuTinhCach(c.tinhCach));
                    o.writeUTF(ten(goc(c.dex)));
                    final com.htbb.pokemon.TraoDoiPokemon.ThuongChuyen t = thuongChuyen(c.dex);
                    final boolean caXu = c.chuGoc == p.charDBID && c.doiLuc == 0;
                    final long xuC = caXu ? xmg * t.xuPhanNghinGio / 1000L : 0L;
                    o.writeUTF("+" + t.keo * (c.shiny ? 2 : 1) + " Kẹo" + (xuC > 0 ? ", +" + xu(xuC) : "")
                            + (caXu && t.luongKhoa > 0 ? ", +" + t.luongKhoa + " lượng khoá" : ""));
                }
            }
            p.sendMessage(m);
        } catch (final Exception ignored) {
        }
    }

    /** C-&gt;S kieu 4: hanh dong tren con id. */
    /**
     * Loai dich tien hoa duoc cua {@code dex} trong Hop PC: cac nhanh THAT cua chuoi co loai dich da mo
     * (LoaiDuLieu.tienHoaMo, loai-day-du; nhanh chinh data_bat_loai.tien_hoa_thanh dung dau). Bang loai chua nap / CSDL
     * cu: chi nhanh chinh data_bat_loai.
     */
    static List<Integer> dichTienHoa(final int dex) {
        final List<Integer> kq = new ArrayList<Integer>();
        final BatPokemon.Loai l = LOAI.get(Integer.valueOf(dex));
        com.htbb.pokemon.LoaiDuLieu bang = null;
        try {
            bang = LoaiQuaiGame.bang();
        } catch (final Throwable e) {
            bang = null;
        }
        if (bang == null || bang.rong()) {
            if (l != null && l.tienHoaThanh > 0) {
                kq.add(Integer.valueOf(l.tienHoaThanh));
            }
            return kq;
        }
        return dichTienHoa(l != null ? l.tienHoaThanh : 0, bang.tienHoaMo(dex));
    }

    /** Thuan (test): nhanh chinh {@code chinh} (neu mo) dung dau, roi cac nhanh mo khac, khong trung. */
    public static List<Integer> dichTienHoa(final int chinh, final List<com.htbb.pokemon.LoaiDuLieu.TienHoa> mo) {
        final List<Integer> kq = new ArrayList<Integer>();
        for (final com.htbb.pokemon.LoaiDuLieu.TienHoa t : mo) {
            if (t.sang > 0 && !kq.contains(Integer.valueOf(t.sang))) {
                kq.add(Integer.valueOf(t.sang));
            }
        }
        if (chinh > 0 && kq.remove(Integer.valueOf(chinh))) {
            kq.add(0, Integer.valueOf(chinh));
        }
        return kq;
    }

    /** Tien hoa con {@code c} thanh {@code sang} (mot nhanh cua dichTienHoa), tru keo theo data_bat_loai cua loai goc. */
    static boolean tienHoa(final Char p, final HopPc h, final HopPc.Con c, final int sang) {
        final BatPokemon.Loai l = LOAI.get(Integer.valueOf(c.dex));
        if (l == null || sang <= 0 || !dichTienHoa(c.dex).contains(Integer.valueOf(sang))) {
            com.htbb.thongbao.ThongBao.loi(p, ten(c.dex) + " không tiến hoá thêm.", "");
            return false;
        }
        if (c.cap < l.capTienHoa) {
            com.htbb.thongbao.ThongBao.loi(p, "Cần cấp " + l.capTienHoa + " để tiến hoá"
                    + (l.tienHoaTraoDoi && sang == l.tienHoaThanh ? " (hoặc trao đổi cho người chơi khác)." : "."), "");
            return false;
        }
        if (!h.truKeo(goc(c.dex), l.keoTienHoa)) {
            com.htbb.thongbao.ThongBao.loi(p, "Cần " + l.keoTienHoa + " Kẹo " + ten(goc(c.dex)) + ".", "");
            return false;
        }
        final int cu = c.dex;
        c.dex = sang; // ca the (IV, tinh cach) giu nguyen nhu game chinh ban
        h.danhDau();
        final Pokedex d = PokedexPokemon.lay(p);
        if (d != null) {
            d.ghiBat(c.dex, c.shiny, System.currentTimeMillis() / 1000L);
        }
        com.htbb.thongbao.ThongBao.gui(p, com.htbb.thongbao.ThongBao.HIEM, ten(cu) + " tiến hoá thành " + ten(c.dex) + "!",
                "Pokédex đã ghi " + ten(c.dex), com.htbb.thongbao.ThongBao.HINH_POKEMON, c.dex);
        if (h.dangTheo == c.id) {
            guiDongHanh(p);
        }
        return true;
    }

    static void hanhDong(final Char p, final int hd, final int id) {
        if (!bat()) {
            return;
        }
        final HopPc h = hop(p);
        if (h == null) {
            return;
        }
        synchronized (h) {
            final HopPc.Con c = h.lay(id);
            if (c == null) {
                com.htbb.thongbao.ThongBao.loi(p, "Pokémon đó không còn trong Hộp PC.", "");
            } else {
                lam(p, h, c, hd);
            }
        }
        guiHop(p);
    }

    /** Ban do co Pokemon hoang da {@code dex} (Pokedex: noi song). Nho 10 phut. */
    private static volatile java.util.Map<Integer, List<String>> NOI_SONG = new HashMap<Integer, List<String>>();
    private static volatile long noiSongLuc;

    static List<String> noiSong(final int dex) {
        final long bay = System.currentTimeMillis();
        if (bay - noiSongLuc > 600_000L) {
            final HashMap<Integer, java.util.LinkedHashSet<String>> m = new HashMap<Integer, java.util.LinkedHashSet<String>>();
            try {
                for (final Map mp : RealController.mapList.values()) {
                    final String tenMap = TenBanDoMayChu.tenDayDu(mp.mapId);
                    if (tenMap.isEmpty() || mp.mapIDLoadMap != mp.mapId) {
                        continue;
                    }
                    for (final int idT : dsMauQuai(mp)) {
                        final MonsterTemplate t = Map.monsterTemplates.get(Integer.valueOf(idT));
                        if (t != null && t.dex > 0) {
                            m.computeIfAbsent(Integer.valueOf(t.dex), z -> new java.util.LinkedHashSet<String>()).add(tenMap);
                        }
                    }
                }
            } catch (final Throwable ignored) {
            }
            final HashMap<Integer, List<String>> v = new HashMap<Integer, List<String>>();
            for (final java.util.Map.Entry<Integer, java.util.LinkedHashSet<String>> e : m.entrySet()) {
                v.put(e.getKey(), new ArrayList<String>(e.getValue()));
            }
            NOI_SONG = v;
            noiSongLuc = bay;
        }
        final List<String> d = NOI_SONG.get(Integer.valueOf(dex));
        return d != null ? d : new ArrayList<String>();
    }

    /** Mau quai hoang da (khong boss) dang co tren ban do (khu 0 / vung 0). */
    static java.util.Set<Integer> dsMauQuai(final Map mp) {
        final java.util.Set<Integer> v = new java.util.HashSet<Integer>();
        try {
            final java.util.Vector<java.util.Hashtable<Short, Monster>> ds = mp.nRegion > 0 && !mp.ALL_REGION.isEmpty()
                    ? mp.ALL_REGION.get(0).monsters : mp.monsters;
            if (ds != null && !ds.isEmpty()) {
                for (final Monster m : ds.get(0).values()) {
                    if (m != null && !m.isBoss && !m.isBoss()) {
                        v.add(Integer.valueOf(m.idTemplate));
                    }
                }
            }
        } catch (final Throwable ignored) {
        }
        return v;
    }

    /** S-&gt;C kieu 5: thong tin loai cho the Pokedex. */
    static void guiLoai(final Char p, final int dex) {
        if (!BoNgocGame.coClient(p) || dex <= 0) {
            return;
        }
        final BatPokemon.Loai l = LOAI.get(Integer.valueOf(dex));
        int truoc = 0;
        for (final BatPokemon.Loai x : LOAI.values()) {
            if (x.tienHoaThanh == dex) {
                truoc = x.dex;
            }
        }
        final Pokedex d = PokedexPokemon.lay(p);
        try {
            final Message m = moi(S_LOAI);
            final DataOutputStream o = m.dos;
            o.writeShort(dex);
            o.writeUTF(ten(dex));
            o.writeByte(l != null ? 1 : 0);
            o.writeByte(l != null ? l.doHiem : 0);
            o.writeUTF(l == null ? "Không thu phục được (boss / huyền thoại / chỉ có ở sự kiện)"
                    : !l.batDuoc ? "Chỉ có được bằng tiến hoá" : l.chiSuKien ? "Chỉ thu phục trong sự kiện" : BatPokemon.tenDoHiem(l.doHiem));
            o.writeShort(l != null && l.batDuoc ? l.tiLePhanNghin : 0);
            o.writeShort(truoc);
            o.writeShort(l != null ? l.tienHoaThanh : 0);
            o.writeByte(l != null ? l.capTienHoa : 0);
            o.writeByte(l != null && l.tienHoaTraoDoi ? 1 : 0);
            o.writeUTF(truoc > 0 ? ten(truoc) : "");
            o.writeUTF(l != null && l.tienHoaThanh > 0 ? ten(l.tienHoaThanh) : "");
            o.writeShort(d != null ? Math.min(Short.MAX_VALUE, d.soBat(dex)) : 0);
            o.writeShort(d != null ? Math.min(Short.MAX_VALUE, d.soHa(dex)) : 0);
            final List<String> ns = noiSong(dex);
            final int n = Math.min(4, ns.size());
            o.writeByte(n);
            for (int i = 0; i < n; i++) {
                o.writeUTF(ns.get(i));
            }
            final int[] g = l != null && l.chiSoGoc != null ? l.chiSoGoc : null;
            o.writeByte(g != null ? 6 : 0);
            for (int i = 0; g != null && i < 6; i++) {
                o.writeByte(Math.min(255, g[i]));
            }
            // DUOI (loai-bo-sung): UTF mo ta Pokedex (data_loai.mo_ta, tu viet), u8 co (bit0 loai co ban sac mau,
            // bit1 da bat shiny). Client doc toi chi so goc roi dung duoc (doc duoi khi con byte) - client cu bo qua.
            final com.htbb.pokemon.LoaiDuLieu ld = LoaiQuaiGame.bang();
            o.writeUTF(catUtf(ld.moTa(dex), 600));
            o.writeByte((ld.coShiny(dex) ? 1 : 0) | (d != null && d.batShiny(dex) ? 2 : 0));
            p.sendMessage(m);
        } catch (final Exception ignored) {
        }
    }

    /** Cat chuoi cho writeUTF (toi da {@code toiDa} ky tu, khong vuot 65535 byte). */
    static String catUtf(final String s, final int toiDa) {
        if (s == null) {
            return "";
        }
        return s.length() <= toiDa ? s : s.substring(0, toiDa);
    }

    /** Thoat game: bo cache nhip. */
    public static void khiThoat(final Char p) {
        try {
            khiThoat0(p);
        } catch (final Throwable ignored) {
            // khong bao gio lam hong luong thoat game
        }
    }

    static void khiThoat0(final Char p) {
        if (p != null && p.charDBID > 0) {
            NEM_CUOI.remove(Integer.valueOf(p.charDBID));
            CHON.remove(Integer.valueOf(p.charDBID));
            CO_UI.remove(Integer.valueOf(p.charDBID));
            TraoDoiPokemonGame.khiThoat(p);
            // 0.9.8: ghi Hop PC roi bo khoi bo nho (truoc giu mai toi khi khoi dong lai: ton RAM theo moi nguoi tung vao,
            // sua CSDL tay khong co tac dung). Con thay doi chua ghi duoc (CSDL loi) thi giu lai cho lan luu sau.
            luu(p);
            final HopPc h = HOP.get(Integer.valueOf(p.charDBID));
            if (h != null) {
                synchronized (h) {
                    if (!h.ban) {
                        HOP.remove(Integer.valueOf(p.charDBID), h);
                    }
                }
            }
        }
    }
}
