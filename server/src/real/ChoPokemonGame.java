package real;

import com.htbb.pokemon.BatPokemon;
import com.htbb.pokemon.CaThePokemon;
import com.htbb.pokemon.ChoPokemon;
import com.htbb.pokemon.ChongClone;
import com.htbb.pokemon.HopPc;
import com.htbb.pokemon.LauDai;
import com.htbb.pokemon.Pokedex;
import data.DatabaseChoPokemon;
import io.Message;
import java.io.DataOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * CHO POKEMON - phan GAME (docs/thiet-ke-game.md 7.9; logic thuan com.htbb.pokemon.ChoPokemon, CSDL
 * data.DatabaseChoPokemon). Client 0.9.8 (game.pokemon.ManChoPokemon) tab "Pokémon" (dang ban) / "Của tôi" + dang ban tu
 * Hop PC voi khung gia may chu tinh.
 *
 * <h3>Goi -121 kieu 7 (client &gt;= 908)</h3>
 * <pre>
 * C-&gt;S byte sub: 0 XIN byte tab (0 cho, 1 cua toi); 1 KHUNG int idCon; 2 DANG int idCon, int gia; 3 MUA long idTin;
 *              4 HUY long idTin
 * S-&gt;C byte sub:
 *   0 DANH_SACH byte tab, short n, n x (long id, short dex, byte cap, byte shiny, byte sao, byte ivPhanTram, short sucManh,
 *               byte doHiem, int gia, UTF tenBan, short phutConLai, byte cuaToi, UTF ten), byte soDang, byte toiDaDang,
 *               byte thue%, byte muaHomNay, byte muaToiDa
 *   1 KHUNG     int idCon, UTF ten, short dex, byte cap, byte shiny, byte sao, int san, int tran, byte phiDang%, byte thue%,
 *               UTF lyDo ("" = dang duoc)
 *   2 KET_QUA   byte ok, UTF chu
 * </pre>
 */
public final class ChoPokemonGame {

    public static final byte C_XIN = 0, C_KHUNG = 1, C_DANG = 2, C_MUA = 3, C_HUY = 4;
    public static final byte S_DANH_SACH = 0, S_KHUNG = 1, S_KET_QUA = 2;
    public static final String KIEU_CHONG_CLONE = "cho_pokemon";

    public static final String K_BAT = "cho_pokemon.bat";
    public static final String K_THUE = "cho_pokemon.thue_phan_tram";
    public static final String K_PHI_DANG = "cho_pokemon.phi_dang_phan_tram";
    public static final String K_CAP = "cho_pokemon.cap_toi_thieu";
    public static final String K_TOI_DA_DANG = "cho_pokemon.toi_da_dang";
    public static final String K_MUA_NGAY = "cho_pokemon.mua_ngay";
    public static final String K_KHOA_GIO = "cho_pokemon.khoa_gio";
    public static final String K_HAN_GIO = "cho_pokemon.han_gio";
    public static final String K_SAC_MAU = "cho_pokemon.he_so_sac_mau";
    public static final String K_3_SAO = "cho_pokemon.he_so_3_sao";

    private ChoPokemonGame() {
    }

    static boolean bat() {
        return BatPokemonGame.bat() && LauDai.so(K_BAT, 1) != 0;
    }

    static volatile List<long[]> KHUNG = new ArrayList<long[]>();
    static volatile long khungLuc;

    static ChoPokemon.Luat luat() {
        final ChoPokemon.Luat l = ChoPokemon.Luat.macDinh();
        // thue cho Pokemon luon >= thue cho ky gui
        l.thuePhanTram = Math.max(LauDai.so(LauDai.K_THUE_CHO, 6), LauDai.so(K_THUE, 8));
        l.phiDangPhanTram = Math.max(0, LauDai.so(K_PHI_DANG, 1));
        l.capToiThieu = Math.max(1, LauDai.so(K_CAP, 20));
        l.toiDaDang = Math.max(0, LauDai.so(K_TOI_DA_DANG, 3));
        l.muaNgay = Math.max(0, LauDai.so(K_MUA_NGAY, 5));
        l.khoaGiay = Math.max(0, LauDai.so(K_KHOA_GIO, 72)) * 3600L;
        l.hanGiay = Math.max(1, LauDai.so(K_HAN_GIO, 48)) * 3600L;
        l.heSoSacMau = Math.max(1, LauDai.so(K_SAC_MAU, 3));
        l.heSo3Sao = Math.max(1, LauDai.so(K_3_SAO, 2));
        final long bay = System.currentTimeMillis();
        if (bay - khungLuc > 60_000L) {
            khungLuc = bay;
            final List<long[]> k = DatabaseChoPokemon.docKhung();
            if (k != null) {
                KHUNG = k;
            }
        }
        for (final long[] r : KHUNG) {
            final int h = (int) r[0];
            if (h >= 0 && h < l.san.length && r[1] > 0 && r[2] >= r[1]) {
                l.san[h] = r[1];
                l.tran[h] = r[2];
            }
        }
        return l;
    }

    static long xuMoiGioCap(final int cap) {
        return LauDai.xuMoiGio(cap);
    }

    static int cap(final Char p) {
        return p.lvDetail != null ? p.lvDetail.lv : 1;
    }

    static ChoPokemon.Ben ben(final Char p) {
        final com.htbb.pokemon.ChongCloneLogic.Ben b = ChongClone.ben(p, false);
        return new ChoPokemon.Ben(p.charDBID, p.charname, p.userID, b.may, b.ip, cap(p));
    }

    static long[] khungCon(final ChoPokemon.Luat l, final HopPc.Con c) {
        final BatPokemon.Loai loai = BatPokemonGame.LOAI.get(Integer.valueOf(c.dex));
        return ChoPokemon.khung(l, loai != null ? loai.doHiem : 0, c.shiny, CaThePokemon.sao(c.iv), xuMoiGioCap(c.cap));
    }

    // ================================================================== goi tin

    /** Nhip thao tac (ms) moi nhan vat - chong spam goi. */
    private static final ConcurrentHashMap<Integer, Long> NHIP = new ConcurrentHashMap<Integer, Long>();

    static void xuLy(final Char p, final Message msg) {
        try {
            final byte sub = msg.dis.readByte();
            if (!bat() || !BoNgocGame.coClient(p)) {
                ketQua(p, false, "Chợ Pokémon chưa mở.");
                return;
            }
            final long bay = System.currentTimeMillis();
            if (sub != C_XIN && sub != C_KHUNG) {
                final Long t = NHIP.get(Integer.valueOf(p.charDBID));
                if (t != null && bay - t.longValue() < 800L) {
                    return;
                }
                NHIP.put(Integer.valueOf(p.charDBID), Long.valueOf(bay));
            }
            switch (sub) {
                case C_XIN:
                    khiVao(p, true);
                    guiDanhSach(p, msg.dis.readByte());
                    break;
                case C_KHUNG:
                    guiKhung(p, msg.dis.readInt());
                    break;
                case C_DANG: {
                    final int id = msg.dis.readInt();
                    dang(p, id, msg.dis.readInt());
                    break;
                }
                case C_MUA:
                    mua(p, msg.dis.readLong());
                    break;
                case C_HUY:
                    huy(p, msg.dis.readLong());
                    break;
                default:
                    break;
            }
        } catch (final Exception e) {
            // goi hong
        }
    }

    static Message tao(final byte sub) throws java.io.IOException {
        final Message m = BatPokemonGame.moi(BatPokemonGame.S_CHO);
        m.dos.writeByte(sub);
        return m;
    }

    static void ketQua(final Char p, final boolean ok, final String chu) {
        try {
            final Message m = tao(S_KET_QUA);
            m.dos.writeByte(ok ? 1 : 0);
            m.dos.writeUTF(chu == null ? "" : chu);
            p.sendMessage(m);
        } catch (final Exception ignored) {
        }
        if (ok) {
            com.htbb.thongbao.ThongBao.ketQua(p, chu, "");
        } else {
            com.htbb.thongbao.ThongBao.loi(p, chu, "");
        }
    }

    // ================================================================== danh sach / khung

    static volatile List<ChoPokemon.Tin> DS = new ArrayList<ChoPokemon.Tin>();
    static volatile long dsLuc;

    static List<ChoPokemon.Tin> dangBan() {
        final long bay = System.currentTimeMillis();
        if (bay - dsLuc > 3000L) {
            final List<ChoPokemon.Tin> v = DatabaseChoPokemon.tim("trang_thai = 0 AND het_han > ? ORDER BY dang_luc DESC LIMIT 60",
                    Long.valueOf(bay / 1000L));
            if (v != null) {
                DS = v;
            }
            dsLuc = bay;
        }
        return DS;
    }

    static List<ChoPokemon.Tin> cuaToi(final Char p) {
        final List<ChoPokemon.Tin> v = DatabaseChoPokemon.tim("id_ban = ? AND trang_thai = 0 ORDER BY dang_luc DESC", Integer.valueOf(p.charDBID));
        return v != null ? v : new ArrayList<ChoPokemon.Tin>();
    }

    static void guiDanhSach(final Char p, final int tab) {
        final ChoPokemon.Luat l = luat();
        final List<ChoPokemon.Tin> mine = cuaToi(p);
        final List<ChoPokemon.Tin> ds = tab == 1 ? mine : dangBan();
        final HopPc h = BatPokemonGame.hop(p);
        final long bay = System.currentTimeMillis() / 1000L;
        try {
            final Message m = tao(S_DANH_SACH);
            final DataOutputStream o = m.dos;
            o.writeByte(tab);
            final int n = Math.min(60, ds.size());
            o.writeShort(n);
            for (int i = 0; i < n; i++) {
                final ChoPokemon.Tin t = ds.get(i);
                final HopPc.Con c = t.con;
                final BatPokemon.Loai loai = BatPokemonGame.LOAI.get(Integer.valueOf(c.dex));
                o.writeLong(t.id);
                o.writeShort(c.dex);
                o.writeByte(c.cap);
                o.writeByte(c.shiny ? 1 : 0);
                o.writeByte(CaThePokemon.sao(c.iv));
                o.writeByte(CaThePokemon.phanTram(c.iv));
                o.writeShort(Math.min(Short.MAX_VALUE, CaThePokemon.sucManh(loai != null ? loai.chiSoGoc : null, c.iv, c.tinhCach, c.cap)));
                o.writeByte(t.doHiem);
                o.writeInt((int) Math.min(Integer.MAX_VALUE, t.gia));
                o.writeUTF(t.tenBan);
                o.writeShort((int) Math.max(0L, Math.min(Short.MAX_VALUE, (t.hetHan - bay) / 60L)));
                o.writeByte(t.idBan == p.charDBID ? 1 : 0);
                o.writeUTF(BatPokemonGame.ten(c.dex));
            }
            o.writeByte(mine.size());
            o.writeByte(l.toiDaDang);
            o.writeByte(l.thuePhanTram);
            o.writeByte(h != null && h.ngay == BatPokemonGame.homNay() ? Math.min(127, h.muaNgay) : 0);
            o.writeByte(l.muaNgay);
            p.sendMessage(m);
        } catch (final Exception ignored) {
        }
    }

    static void guiKhung(final Char p, final int idCon) {
        final HopPc h = BatPokemonGame.hop(p);
        if (h == null) {
            return;
        }
        final ChoPokemon.Luat l = luat();
        final HopPc.Con c = h.lay(idCon);
        if (c == null) {
            ketQua(p, false, "Pokémon đó không còn trong Hộp PC.");
            return;
        }
        final long[] k = khungCon(l, c);
        final BatPokemon.Loai loai = BatPokemonGame.LOAI.get(Integer.valueOf(c.dex));
        String ly = ChoPokemon.lyDoDang(l, h, c, loai, cap(p), cuaToi(p).size(), k[0], k, System.currentTimeMillis() / 1000L);
        if (ly == null && TraoDoiPokemonGame.dangDua(p, c.id)) {
            ly = "Pokémon này đang nằm trong một cuộc trao đổi.";
        }
        try {
            final Message m = tao(S_KHUNG);
            final DataOutputStream o = m.dos;
            o.writeInt(c.id);
            o.writeUTF(BatPokemonGame.ten(c.dex));
            o.writeShort(c.dex);
            o.writeByte(c.cap);
            o.writeByte(c.shiny ? 1 : 0);
            o.writeByte(CaThePokemon.sao(c.iv));
            o.writeInt((int) Math.min(Integer.MAX_VALUE, k[0]));
            o.writeInt((int) Math.min(Integer.MAX_VALUE, k[1]));
            o.writeByte(l.phiDangPhanTram);
            o.writeByte(l.thuePhanTram);
            o.writeUTF(ly == null ? "" : ly);
            p.sendMessage(m);
        } catch (final Exception ignored) {
        }
    }

    // ================================================================== dang ban

    static void dang(final Char p, final int idCon, final long gia) {
        final HopPc h = BatPokemonGame.hop(p);
        if (h == null) {
            ketQua(p, false, "Hộp PC tạm thời không đọc được.");
            return;
        }
        if (TraoDoiPokemonGame.dangDua(p, idCon)) {
            ketQua(p, false, "Pokémon này đang nằm trong một cuộc trao đổi.");
            return;
        }
        if (ChongClone.chanMotBen(p, KIEU_CHONG_CLONE)) {
            return; // da bao (cap / tuoi tai khoan / gio choi)
        }
        final ChoPokemon.Luat l = luat();
        final HopPc.Con c = h.lay(idCon);
        if (c == null) {
            ketQua(p, false, "Pokémon đó không còn trong Hộp PC.");
            return;
        }
        final BatPokemon.Loai loai = BatPokemonGame.LOAI.get(Integer.valueOf(c.dex));
        final long[] k = khungCon(l, c);
        final int soDang = cuaToi(p).size();
        final String ly = ChoPokemon.lyDoDang(l, h, c, loai, cap(p), soDang, gia, k, System.currentTimeMillis() / 1000L);
        if (ly != null) {
            ketQua(p, false, ly);
            return;
        }
        final long phi = ChoPokemon.phiDang(l, gia);
        if (!p.truNeuDu(() -> p.xu >= phi, () -> p.subXu(phi, true, "phi dang cho pokemon"))) {
            ketQua(p, false, "Cần " + BatPokemonGame.xu(phi) + " phí đăng bán.");
            return;
        }
        final ChoPokemon.KetQua kq = ChoPokemon.dang(l, ben(p), h, idCon, gia, loai, xuMoiGioCap(c.cap), soDang,
                System.currentTimeMillis() / 1000L, DatabaseChoPokemon.KHO);
        if (!kq.ok) {
            p.addXu(phi, "hoan phi dang cho pokemon");
            ketQua(p, false, kq.loi);
            return;
        }
        LauDai.ghiXuRa(phi);
        dsLuc = 0L;
        if (h.dangTheo == 0) {
            BatPokemonGame.guiDongHanh(p);
        }
        log(p.charname, "dang tin " + kq.id + " " + HopPc.maHoaCon(kq.con) + " gia " + gia + " phi " + phi + " khung " + k[0] + "-" + k[1]);
        ketQua(p, true, "Đã đăng bán " + BatPokemonGame.ten(c.dex) + " giá " + BatPokemonGame.xu(gia) + " (phí " + BatPokemonGame.xu(phi)
                + ", hết hạn sau " + l.hanGiay / 3600L + " giờ).");
        BatPokemonGame.guiHop(p);
        guiDanhSach(p, 1);
    }

    // ================================================================== mua

    static ChoPokemon.Tin tin(final long id) {
        final List<ChoPokemon.Tin> v = DatabaseChoPokemon.tim("id = ?", Long.valueOf(id));
        return v != null && !v.isEmpty() ? v.get(0) : null;
    }

    static void mua(final Char p, final long idTin) {
        final HopPc h = BatPokemonGame.hop(p);
        final ChoPokemon.Tin t = tin(idTin);
        if (h == null || t == null) {
            ketQua(p, false, "Tin bán này không còn.");
            return;
        }
        final ChoPokemon.Luat l = luat();
        final long bayGiay = System.currentTimeMillis() / 1000L;
        final ChoPokemon.Ben mua = ben(p);
        final String ly = ChoPokemon.lyDoMua(l, t, mua, h, bayGiay, BatPokemonGame.homNay());
        if (ly != null) {
            ketQua(p, false, ly);
            return;
        }
        // chong clone CA HAI ben: nguoi mua (cap / tuoi / gio choi) + nguoi ban (neu online) + gioi han cung may / IP / ngay
        if (ChongClone.chanMotBen(p, KIEU_CHONG_CLONE) || ChongClone.chanChuyen(p, t.idBan, t.tenBan, KIEU_CHONG_CLONE, t.gia)) {
            return;
        }
        final long gia = t.gia;
        if (!p.truNeuDu(() -> p.xu >= gia, () -> p.subXu(gia, true, "mua cho pokemon " + idTin))) {
            ketQua(p, false, "Cần " + BatPokemonGame.xu(gia) + ".");
            return;
        }
        final long[] k = khungCon(l, t.con);
        final int[] dem = DatabaseChoPokemon.demNghi(t.idBan, p.charDBID);
        final String nghi = ChoPokemon.nghiVan(dem[0] + 1, dem[1], gia, k[1]);
        DatabaseChoPokemon.NGHI.set(new String[]{ChoPokemon.satTran(gia, k[1]) ? "1" : "0", nghi});
        final ChoPokemon.KetQua kq;
        try {
            kq = ChoPokemon.mua(l, t, mua, h, bayGiay, BatPokemonGame.homNay(), DatabaseChoPokemon.KHO);
        } finally {
            DatabaseChoPokemon.NGHI.remove();
        }
        if (!kq.ok) {
            p.addXu(gia, "hoan mua cho pokemon " + idTin);
            ketQua(p, false, kq.loi);
            dsLuc = 0L;
            return;
        }
        final long thue = ChoPokemon.thue(l, gia);
        LauDai.ghiXuRa(thue); // thue dot: nguoi mua tra gia = nguoi ban nhan + thue
        dsLuc = 0L;
        final Pokedex d = PokedexPokemon.lay(p);
        if (d != null) {
            d.ghiBat(kq.con.dex, kq.con.shiny, bayGiay);
        }
        ChongClone.ghi(p, t.idBan, t.tenBan, KIEU_CHONG_CLONE, gia, "cho pokemon tin " + idTin + " " + HopPc.maHoaCon(t.con));
        log(p.charname, "mua tin " + idTin + " cua " + t.tenBan + " " + HopPc.maHoaCon(t.con) + " gia " + gia + " thue " + thue);
        if (!nghi.isEmpty()) {
            log(p.charname, "NGHI VAN cho pokemon: " + t.tenBan + " -> " + p.charname + " tin " + idTin + " gia " + gia + " tran " + k[1] + ": " + nghi);
        }
        ketQua(p, true, "Đã mua " + BatPokemonGame.ten(kq.con.dex) + " giá " + BatPokemonGame.xu(gia) + ". Pokémon đã vào Hộp PC.");
        NhiemVuEngine.suKien(p, "cho_pokemon_mua", String.valueOf(kq.con.dex), 1); // 0.9.8 dong bo: diem moc nhiem vu / danh hieu
        final Char ban = CharManager.instance.getCharByCharDbID(t.idBan);
        if (ban != null) {
            nhanTien(ban); // nguoi ban online: nhan tien ngay; offline: nhan khi vao game
        }
        BatPokemonGame.guiTrangThai(p);
        guiDanhSach(p, 0);
    }

    // ================================================================== huy / het han / nhan tien

    static void huy(final Char p, final long idTin) {
        final HopPc h = BatPokemonGame.hop(p);
        final ChoPokemon.Tin t = tin(idTin);
        if (h == null || t == null || t.idBan != p.charDBID) {
            ketQua(p, false, "Tin bán này không còn.");
            return;
        }
        final ChoPokemon.KetQua kq = ChoPokemon.thuHoi(t, p.charDBID, h, DatabaseChoPokemon.KHO);
        if (!kq.ok) {
            ketQua(p, false, kq.loi);
            return;
        }
        dsLuc = 0L;
        log(p.charname, "huy tin " + idTin + " (phi dang khong hoan)");
        ketQua(p, true, "Đã huỷ tin, " + BatPokemonGame.ten(kq.con.dex) + " về Hộp PC (phí đăng không hoàn).");
        BatPokemonGame.guiHop(p);
        guiDanhSach(p, 1);
    }

    /** Nhip "vao game / mo cho": tra con het han + nhan tien ban. */
    private static final ConcurrentHashMap<Integer, Long> VAO = new ConcurrentHashMap<Integer, Long>();

    /** BatPokemonGame.guiTrangThai (vao ban do) va mo cho. */
    static void khiVao(final Char p, final boolean ngay) {
        if (p == null || p.charDBID <= 0 || !bat()) {
            return;
        }
        final long bay = System.currentTimeMillis();
        final Long t = VAO.get(Integer.valueOf(p.charDBID));
        if (!ngay && t != null && bay - t.longValue() < 60_000L) {
            return;
        }
        VAO.put(Integer.valueOf(p.charDBID), Long.valueOf(bay));
        thuHoiHetHan(p);
        nhanTien(p);
    }

    static void thuHoiHetHan(final Char p) {
        final HopPc h = BatPokemonGame.hop(p);
        if (h == null) {
            return;
        }
        final List<ChoPokemon.Tin> v = DatabaseChoPokemon.tim("id_ban = ? AND trang_thai = 0 AND het_han <= ?", Integer.valueOf(p.charDBID),
                Long.valueOf(System.currentTimeMillis() / 1000L));
        if (v == null) {
            return;
        }
        for (final ChoPokemon.Tin t : v) {
            final ChoPokemon.KetQua kq = ChoPokemon.thuHoi(t, p.charDBID, h, DatabaseChoPokemon.KHO);
            if (kq.ok) {
                log(p.charname, "het han tin " + t.id + ", tra " + HopPc.maHoaCon(t.con));
                com.htbb.thongbao.ThongBao.ketQua(p, "Tin bán " + BatPokemonGame.ten(t.con.dex) + " đã hết hạn.", "Pokémon đã về Hộp PC.");
            }
        }
    }

    static void nhanTien(final Char p) {
        final List<ChoPokemon.Tin> v = DatabaseChoPokemon.tim("id_ban = ? AND trang_thai = 1", Integer.valueOf(p.charDBID));
        if (v == null) {
            return;
        }
        final ChoPokemon.Luat l = luat();
        for (final ChoPokemon.Tin t : v) {
            if (DatabaseChoPokemon.nhanTien(t.id, p.charDBID)) {
                final long nhan = Math.max(0L, t.gia - t.thue);
                p.addXu(nhan, "ban cho pokemon tin " + t.id);
                log(p.charname, "nhan tien tin " + t.id + " gia " + t.gia + " thue " + t.thue + " nhan " + nhan);
                com.htbb.thongbao.ThongBao.ketQua(p, "Chợ Pokémon: " + t.tenMua + " đã mua " + BatPokemonGame.ten(t.con.dex) + ".",
                        "+" + BatPokemonGame.xu(nhan) + " (đã trừ thuế " + l.thuePhanTram + "%)");
            }
        }
    }

    static void log(final String ten, final String chu) {
        try {
            data.Database.instance.saveOrtherLog("", ten, chu, "chopokemon");
        } catch (final Exception ignored) {
        }
    }
}
