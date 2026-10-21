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
 * </pre>
 * Menu: Y ta Chansey muc "Poké Ball &amp; Hộp PC" (mua bong theo goi 5, mo rong Hop PC bang xu), NPC Pokedex muc
 * "Hộp PC" (xem, cho di theo, luyen / tien hoa bang Keo loai, tha ve).
 */
public final class BatPokemonGame {

    public static final int LENH = -121;
    public static final byte C_TRANG_THAI = 0, C_NEM = 1, C_POKEDEX = 2;
    public static final byte S_TRANG_THAI = 0, S_KET_QUA = 1, S_POKEDEX = 2, S_DONG_HANH = 3;

    public static final String K_BAT = "bat.bat";
    public static final String K_NGUONG_HP = "bat.nguong_hp_phan_tram";
    public static final String K_TRAN_NGAY = "bat.tran_ngay";
    public static final String K_TRAN_NEM_NGAY = "bat.tran_nem_ngay";
    public static final String K_CACH_MS = "bat.cach_ms";
    public static final String K_SO_LAN_BO_CHAY = "bat.so_lan_bo_chay";
    public static final String K_SU_KIEN = "bat.su_kien";
    public static final String K_DONG_HANH = "bat.dong_hanh";

    /** Npc ao (xem BoNgocGame.NPC_ID): menu bong, Hop PC, mot con, xac nhan tha. */
    public static final int NPC_BONG = -581, NPC_HOP = -582, NPC_CON = -583, NPC_THA = -584;
    public static final String TEN_MUC_CHANSEY = "Poké Ball & Hộp PC";
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
            LOAI = m;
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
            h.ban = false;
            if (!DatabaseBoNgoc.luuHoSo(DatabaseBoNgoc.BANG_HOP_PC, p.charDBID, h.maHoa())) {
                h.ban = true;
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
                guiTrangThai(p);
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
        if (h.day()) {
            tuChoi(p, idQuai, thuTu, "Hộp PC đã đầy (" + h.sucChua() + " ô). Thả bớt hoặc mở rộng ở Chansey.");
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
        final long[] g;
        synchronized (GIAO_CHIEN) {
            g = GIAO_CHIEN.get(m);
        }
        if (g == null || g[0] != p.charDBID || bay - g[1] > GIAO_CHIEN_MS) {
            tuChoi(p, idQuai, thuTu, "Hãy tự giao chiến với Pokémon này trước khi ném bóng.");
            return;
        }
        final MonsterTemplate t = Map.monsterTemplates.get(Integer.valueOf(m.idTemplate));
        final int dex = t != null ? t.dex : 0;
        final boolean laBoss = m.isBoss || m.isBoss() || m.isMaterialMons() || m.isMonsterVantieu() || m.isTruMoba()
                || (t != null && t.maxhp >= 10_000_000);
        final BatPokemon.Loai l = loai(dex);
        final int capNv = p.lvDetail != null ? p.lvDetail.lv : 1;
        final int ma = BatPokemon.kiem(l, laBoss, m.hp, m.maxhp, m.level, capNv, nguongHp(), LauDai.so(K_SU_KIEN, 0) != 0);
        if (ma != BatPokemon.OK) {
            tuChoi(p, idQuai, thuTu, BatPokemon.chuKiem(ma, nguongHp()));
            return;
        }
        final boolean alpha = NoiDungLauDai.laAlpha(m);
        final boolean shiny = t != null && t.shiny != 0;
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
            synchronized (h) {
                h.batNgay++;
                h.tongBat++;
                h.them(dex, Math.min(m.level, BatPokemon.capToiDa(capNv)), shiny, homNay(), b.ma);
                h.themKeo(goc(dex), BatPokemon.keoKhiBat(shiny));
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
                            + " · Hộp PC " + h.soCon() + "/" + h.sucChua(),
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
                        "Tỉ lệ " + tiLe / 10 + "% · lần sau +" + BatPokemon.THUONG_TRUOT / 10 + "% · còn "
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
            m.add(ten(c.dex) + (c.shiny ? " (sắc màu)" : "") + " Lv." + c.cap + (h.dangTheo == c.id ? " (đi theo)" : "")
                    + " · Kẹo " + h.keo(goc(c.dex)));
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
        m.add(c.cap >= capTd ? "Luyện cấp (đã tối đa Lv." + capTd + ")"
                : "Luyện cấp → Lv." + (c.cap + 1) + " (" + BatPokemon.keoLuyen(c.cap) + " kẹo, có " + keo + ")");
        final BatPokemon.Loai l = LOAI.get(Integer.valueOf(c.dex));
        if (l != null && l.tienHoaThanh > 0) {
            m.add("Tiến hoá → " + ten(l.tienHoaThanh) + " (Lv." + l.capTienHoa + ", " + l.keoTienHoa + " kẹo)");
        } else {
            m.add("Không tiến hoá thêm");
        }
        m.add("Thả về tự nhiên (+" + BatPokemon.KEO_KHI_THA + " kẹo)");
        p.sendMessage(MessageCreator.createMsgMenu(m.toArray(new String[0]), NPC_CON, 0));
    }

    /** MapDoMenu: idNpc la mot trong NPC_BONG / NPC_HOP / NPC_CON / NPC_THA. true = da xu ly. */
    public static boolean doMenu(final Char p, final int idNpc, final int idMenu, final int idOption) {
        if (idNpc != NPC_BONG && idNpc != NPC_HOP && idNpc != NPC_CON && idNpc != NPC_THA) {
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
            } else {
                final HopPc.Con c = h.lay(chon(p)[1]);
                if (c != null && idOption == 0) {
                    h.tha(c.id);
                    h.themKeo(goc(c.dex), BatPokemon.KEO_KHI_THA);
                    com.htbb.thongbao.ThongBao.gui(p, com.htbb.thongbao.ThongBao.KET_QUA, "Đã thả " + ten(c.dex) + " về tự nhiên.",
                            "+" + BatPokemon.KEO_KHI_THA + " Kẹo " + ten(goc(c.dex)), com.htbb.thongbao.ThongBao.HINH_POKEMON, c.dex);
                    guiDongHanh(p);
                    guiTrangThai(p);
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
        final int capNv = p.lvDetail != null ? p.lvDetail.lv : 1;
        switch (idOption) {
            case 0:
                h.datTheo(h.dangTheo == c.id ? 0 : c.id);
                com.htbb.thongbao.ThongBao.ketQua(p, h.dangTheo == c.id ? ten(c.dex) + " đi theo bạn." : "Không còn Pokémon đi theo.", "");
                guiDongHanh(p);
                return;
            case 1: {
                if (c.cap >= BatPokemon.capToiDa(capNv)) {
                    com.htbb.thongbao.ThongBao.loi(p, "Cấp tối đa bằng cấp của bạn (Lv." + BatPokemon.capToiDa(capNv) + ").", "");
                    return;
                }
                final int can = BatPokemon.keoLuyen(c.cap);
                if (!h.truKeo(goc(c.dex), can)) {
                    com.htbb.thongbao.ThongBao.loi(p, "Cần " + can + " Kẹo " + ten(goc(c.dex)) + ".", "");
                    return;
                }
                c.cap++;
                h.danhDau();
                com.htbb.thongbao.ThongBao.ketQua(p, ten(c.dex) + " lên Lv." + c.cap + ".", "Còn " + h.keo(goc(c.dex)) + " kẹo");
                moCon(p, h, c);
                return;
            }
            case 2: {
                final BatPokemon.Loai l = LOAI.get(Integer.valueOf(c.dex));
                if (l == null || l.tienHoaThanh <= 0) {
                    com.htbb.thongbao.ThongBao.loi(p, ten(c.dex) + " không tiến hoá thêm.", "");
                    return;
                }
                if (c.cap < l.capTienHoa) {
                    com.htbb.thongbao.ThongBao.loi(p, "Cần Lv." + l.capTienHoa + " để tiến hoá.", "");
                    return;
                }
                if (!h.truKeo(goc(c.dex), l.keoTienHoa)) {
                    com.htbb.thongbao.ThongBao.loi(p, "Cần " + l.keoTienHoa + " Kẹo " + ten(goc(c.dex)) + ".", "");
                    return;
                }
                final int cu = c.dex;
                c.dex = l.tienHoaThanh;
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
                moCon(p, h, c);
                return;
            }
            case 3:
                p.sendMessage(MessageCreator.createMsgMenu(new String[]{"Thả " + ten(c.dex) + " Lv." + c.cap, "Thôi"}, NPC_THA, 0));
                return;
            default:
                return;
        }
    }

    /** Thoat game: bo cache nhip. */
    public static void khiThoat(final Char p) {
        if (p != null && p.charDBID > 0) {
            NEM_CUOI.remove(Integer.valueOf(p.charDBID));
            CHON.remove(Integer.valueOf(p.charDBID));
        }
    }
}
