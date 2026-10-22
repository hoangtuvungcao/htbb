package real;

import com.htbb.pokemon.BatPokemon;
import com.htbb.pokemon.CaThePokemon;
import com.htbb.pokemon.ChongClone;
import com.htbb.pokemon.HopPc;
import com.htbb.pokemon.LauDai;
import com.htbb.pokemon.Pokedex;
import com.htbb.pokemon.TraoDoiPokemon;
import data.DatabaseBoNgoc;
import io.Message;
import java.io.DataOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * TRAO DOI POKEMON giua hai nguoi choi (docs/thiet-ke-game.md 7.8; logic thuan com.htbb.pokemon.TraoDoiPokemon).
 *
 * <h3>Luong</h3>
 * Menu nguoi choi "Trao đổi Pokémon" -&gt; loi moi (30 giay) -&gt; nguoi kia dong y -&gt; cua so trao doi hai ben: moi ben
 * chon 1..3 con trong Hop PC -&gt; "Khoá" (xac nhan 1: khong doi duoc nua; doi lai thi ca hai mo khoa) -&gt; ca hai da khoa
 * -&gt; "Trao đổi" (xac nhan 2). Ca hai xac nhan: may chu kiem LAI moi cong (online, cung ban do, gan nhau, chong clone
 * hai chieu, cap, tran ngay, nhip, du xu phi, con nguyen ven nhu luc khoa), tru phi, roi
 * {@link TraoDoiPokemon#thucHien} doi nguyen tu (mot giao dich CSDL ghi hai Hop PC + nhat ky
 * tob_log_trao_doi_pokemon). Ghi hong: tra lai phi, khong ai mat gi. Roi game / doi y bat ky luc nao truoc khi chot:
 * huy, khong doi gi.
 *
 * <h3>Goi -121 kieu 6 (client &gt;= 908 co man trao doi)</h3>
 * <pre>
 * C-&gt;S byte sub: 0 MOI short idTrenBanDo; 1 TRA_LOI byte dongY; 2 DAT byte n, n x int idCon; 3 KHOA; 4 XAC_NHAN;
 *              5 HUY; 6 XIN (gui lai trang thai)
 * S-&gt;C byte trangThai:
 *   0 DONG    UTF lyDo
 *   1 LOI_MOI short idTrenBanDo, UTF ten, byte giayConLai
 *   2 PHIEN   UTF tenDoi, byte buocToi, byte buocDoi (0 dang chon / 1 da khoa / 2 da xac nhan), byte nToi, con...,
 *             byte nDoi, con..., int phiToi, UTF ghiChu
 *   3 XONG    UTF chu, byte n, con... (con vua nhan)
 *   con = int id, short dex, byte cap, byte shiny, byte sao, byte ivPhanTram, short sucManh, byte doHiem,
 *         byte co (bit0 se tien hoa khi trao doi), UTF ten
 * </pre>
 */
public final class TraoDoiPokemonGame {

    public static final byte C_MOI = 0, C_TRA_LOI = 1, C_DAT = 2, C_KHOA = 3, C_XAC_NHAN = 4, C_HUY = 5, C_XIN = 6;
    public static final byte S_DONG = 0, S_LOI_MOI = 1, S_PHIEN = 2, S_XONG = 3;

    public static final String K_BAT = "trao_doi.bat";
    public static final String K_CAP = "trao_doi.cap_toi_thieu";
    public static final String K_TRAN_NGAY = "trao_doi.tran_ngay";
    public static final String K_KHOA_GIO = "trao_doi.khoa_gio";
    public static final String K_CACH_GIAY = "trao_doi.cach_giay";
    public static final String K_PHI = "trao_doi.phi_phan_nghin_gio";
    public static final String K_PHI_TRAN = "trao_doi.phi_tran_phan_nghin_gio";
    public static final String KIEU_CHONG_CLONE = "trao_doi_pokemon";

    /** Khoang cach toi da (px), thoi han loi moi / phien (ms). */
    public static final int TAM = 200;
    public static final long MOI_MS = 30_000L, PHIEN_MS = 300_000L;

    private TraoDoiPokemonGame() {
    }

    static boolean bat() {
        return BatPokemonGame.bat() && LauDai.so(K_BAT, 1) != 0;
    }

    static TraoDoiPokemon.Luat luat() {
        return new TraoDoiPokemon.Luat(LauDai.so(K_TRAN_NGAY, 10), Math.max(0, LauDai.so(K_KHOA_GIO, 24)) * 3600L,
                Math.max(0, LauDai.so(K_CACH_GIAY, 60)));
    }

    static int capToiThieu() {
        return Math.max(1, LauDai.so(K_CAP, 10));
    }

    // ================================================================== phien

    static final class Phien {
        final Char[] ben = new Char[2];
        @SuppressWarnings("unchecked")
        final List<Integer>[] dua = new List[]{new ArrayList<Integer>(), new ArrayList<Integer>()};
        final int[] buoc = new int[2];
        /** Ban sao con luc khoa (kiem "con da doi" luc chot). */
        @SuppressWarnings("unchecked")
        final List<HopPc.Con>[] mau = new List[]{new ArrayList<HopPc.Con>(), new ArrayList<HopPc.Con>()};
        long luc = System.currentTimeMillis();
        boolean xong;

        int ben(final Char p) {
            return this.ben[0] == p ? 0 : this.ben[1] == p ? 1 : -1;
        }
    }

    /** charDBID -&gt; phien dang mo. */
    static final ConcurrentHashMap<Integer, Phien> PHIEN = new ConcurrentHashMap<Integer, Phien>();
    /** charDBID nguoi duoc moi -&gt; {charDBID nguoi moi, ms}. */
    static final ConcurrentHashMap<Integer, long[]> LOI_MOI = new ConcurrentHashMap<Integer, long[]>();

    /** Con {@code id} cua p dang nam trong phien trao doi (khong cho tha / tien hoa / chuyen doi / cho theo). */
    static boolean dangDua(final Char p, final int id) {
        final Phien f = p != null ? PHIEN.get(Integer.valueOf(p.charDBID)) : null;
        if (f == null) {
            return false;
        }
        synchronized (f) {
            final int b = f.ben(p);
            return b >= 0 && !f.xong && f.dua[b].contains(Integer.valueOf(id));
        }
    }

    // ================================================================== goi tin

    static void xuLy(final Char p, final Message msg) {
        try {
            final byte sub = msg.dis.readByte();
            if (!bat()) {
                dong(p, "Trao đổi Pokémon chưa mở.");
                return;
            }
            switch (sub) {
                case C_MOI:
                    moi(p, msg.dis.readShort());
                    break;
                case C_TRA_LOI:
                    traLoi(p, msg.dis.readByte() != 0);
                    break;
                case C_DAT: {
                    final int n = msg.dis.readByte();
                    final List<Integer> ds = new ArrayList<Integer>();
                    for (int i = 0; i < n && i < 10; i++) {
                        ds.add(Integer.valueOf(msg.dis.readInt()));
                    }
                    dat(p, ds);
                    break;
                }
                case C_KHOA:
                    khoa(p);
                    break;
                case C_XAC_NHAN:
                    xacNhan(p);
                    break;
                case C_HUY:
                    huy(p, p.charname + " đã huỷ trao đổi.");
                    break;
                case C_XIN:
                    final Phien f = PHIEN.get(Integer.valueOf(p.charDBID));
                    if (f != null) {
                        guiPhien(f, "");
                    }
                    break;
                default:
                    break;
            }
        } catch (final Exception e) {
            // goi hong: bo qua
        }
    }

    static Message tao(final byte trangThai) throws java.io.IOException {
        final Message m = BatPokemonGame.moi(BatPokemonGame.S_TRAO_DOI);
        m.dos.writeByte(trangThai);
        return m;
    }

    static void dong(final Char p, final String lyDo) {
        if (p == null || !BatPokemonGame.coUi(p)) {
            if (p != null && lyDo != null && !lyDo.isEmpty()) {
                com.htbb.thongbao.ThongBao.loi(p, lyDo, "");
            }
            return;
        }
        try {
            final Message m = tao(S_DONG);
            m.dos.writeUTF(lyDo == null ? "" : lyDo);
            p.sendMessage(m);
        } catch (final Exception ignored) {
        }
    }

    // ================================================================== cong (kiem truoc moi buoc va luc chot)

    static boolean online(final Char p) {
        return p != null && !p.exit && p.getSession() != null && CharManager.instance.getCharByCharDbID(p.charDBID) == p;
    }

    /** Ly do hai nguoi KHONG trao doi duoc luc nay (null = duoc). Khong kiem chong clone (lam rieng, co bao). */
    static String lyDoCoBan(final Char a, final Char b) {
        if (b == null || !online(b)) {
            return "Người chơi kia không còn trực tuyến.";
        }
        if (a == b || a.charDBID == b.charDBID) {
            return "Không thể tự trao đổi với chính mình.";
        }
        if (!BatPokemonGame.coUi(b)) {
            return b.charname + " cần cập nhật bản 0.9.8 để trao đổi Pokémon.";
        }
        if (a.map == null || a.map != b.map || a.inCountry != b.inCountry || a.region != b.region) {
            return "Hai bên phải ở cùng một bản đồ.";
        }
        if (Math.abs(a.x - b.x) > TAM || Math.abs(a.y - b.y) > TAM) {
            return "Hãy đứng gần " + b.charname + " hơn.";
        }
        final int cap = capToiThieu();
        if ((a.lvDetail != null ? a.lvDetail.lv : 1) < cap) {
            return "Bạn cần đạt cấp " + cap + " mới trao đổi Pokémon được.";
        }
        if ((b.lvDetail != null ? b.lvDetail.lv : 1) < cap) {
            return b.charname + " chưa đạt cấp " + cap + ".";
        }
        if (dangGiaoDichDo(a) || dangGiaoDichDo(b)) {
            return "Đang trao đổi vật phẩm, hãy xong việc đó trước.";
        }
        return null;
    }

    static boolean dangGiaoDichDo(final Char p) {
        return p.userTrade != null && !p.userTrade.isEmpty();
    }

    // ================================================================== moi / tra loi

    static void moi(final Char p, final short idTrenBanDo) {
        final Char q = CharManager.instance.getByCharID(idTrenBanDo);
        if (PHIEN.containsKey(Integer.valueOf(p.charDBID))) {
            dong(p, "Bạn đang trong một cuộc trao đổi khác.");
            return;
        }
        String m = lyDoCoBan(p, q);
        if (m == null && PHIEN.containsKey(Integer.valueOf(q.charDBID))) {
            m = q.charname + " đang trao đổi với người khác.";
        }
        if (m != null) {
            dong(p, m);
            return;
        }
        if (ChongClone.chanCaHai(p, q, p, KIEU_CHONG_CLONE)) {
            return; // da bao (cap / tuoi tai khoan / gio choi)
        }
        LOI_MOI.put(Integer.valueOf(q.charDBID), new long[]{p.charDBID, System.currentTimeMillis()});
        try {
            final Message mm = tao(S_LOI_MOI);
            mm.dos.writeShort(p.id);
            mm.dos.writeUTF(p.charname);
            mm.dos.writeByte((int) (MOI_MS / 1000L));
            q.sendMessage(mm);
        } catch (final Exception ignored) {
        }
        com.htbb.thongbao.ThongBao.ketQua(p, "Đã mời " + q.charname + " trao đổi Pokémon.", "Chờ người kia đồng ý (30 giây).");
    }

    static void traLoi(final Char q, final boolean dongY) {
        final long[] lm = LOI_MOI.remove(Integer.valueOf(q.charDBID));
        if (lm == null || System.currentTimeMillis() - lm[1] > MOI_MS) {
            dong(q, "Lời mời trao đổi đã hết hạn.");
            return;
        }
        final Char p = CharManager.instance.getCharByCharDbID((int) lm[0]);
        if (!dongY) {
            if (p != null) {
                dong(p, q.charname + " từ chối trao đổi Pokémon.");
            }
            return;
        }
        String m = lyDoCoBan(q, p);
        if (m == null && (PHIEN.containsKey(Integer.valueOf(p.charDBID)) || PHIEN.containsKey(Integer.valueOf(q.charDBID)))) {
            m = "Một bên đang trong cuộc trao đổi khác.";
        }
        if (m != null) {
            dong(q, m);
            return;
        }
        final Phien f = new Phien();
        f.ben[0] = p;
        f.ben[1] = q;
        if (PHIEN.putIfAbsent(Integer.valueOf(p.charDBID), f) != null) {
            dong(q, "Người chơi kia đang trong cuộc trao đổi khác.");
            return;
        }
        if (PHIEN.putIfAbsent(Integer.valueOf(q.charDBID), f) != null) {
            PHIEN.remove(Integer.valueOf(p.charDBID), f);
            dong(q, "Bạn đang trong cuộc trao đổi khác.");
            return;
        }
        guiPhien(f, "Chọn tối đa " + TraoDoiPokemon.TOI_DA_MOI_BEN + " Pokémon trong Hộp PC rồi bấm Khoá.");
    }

    // ================================================================== trong phien

    static Phien phien(final Char p) {
        final Phien f = PHIEN.get(Integer.valueOf(p.charDBID));
        if (f == null) {
            dong(p, "Không còn cuộc trao đổi nào.");
            return null;
        }
        final Char doi = f.ben[f.ben(p) == 0 ? 1 : 0];
        if (!online(doi)) {
            huy(p, "Người chơi kia đã rời game, trao đổi bị huỷ.");
            return null;
        }
        if (System.currentTimeMillis() - f.luc > PHIEN_MS) {
            huy(p, "Trao đổi quá 5 phút không xong, đã huỷ.");
            return null;
        }
        return f;
    }

    static void dat(final Char p, final List<Integer> ds) {
        final Phien f = phien(p);
        if (f == null) {
            return;
        }
        final HopPc h = BatPokemonGame.hop(p);
        if (h == null) {
            return;
        }
        String loi = null;
        synchronized (f) {
            final int b = f.ben(p);
            if (f.xong || b < 0) {
                return;
            }
            if (ds.size() > TraoDoiPokemon.TOI_DA_MOI_BEN) {
                loi = "Mỗi lần tối đa " + TraoDoiPokemon.TOI_DA_MOI_BEN + " Pokémon.";
            } else {
                final long bay = System.currentTimeMillis() / 1000L;
                for (final Integer id : ds) {
                    final String m = TraoDoiPokemon.lyDoKhongDua(h, h.lay(id.intValue()), bay, luat());
                    if (m != null) {
                        loi = m;
                        break;
                    }
                }
            }
            if (loi == null) {
                f.dua[b].clear();
                for (final Integer id : ds) {
                    if (!f.dua[b].contains(id)) {
                        f.dua[b].add(id);
                    }
                }
                // doi de nghi: CA HAI ben mo khoa (chong "doi con luc nguoi kia vua bam")
                f.buoc[0] = 0;
                f.buoc[1] = 0;
                f.luc = System.currentTimeMillis();
            }
        }
        guiPhien(f, loi != null ? loi : "");
    }

    static void khoa(final Char p) {
        final Phien f = phien(p);
        if (f == null) {
            return;
        }
        final HopPc h = BatPokemonGame.hop(p);
        String loi = null;
        synchronized (f) {
            final int b = f.ben(p);
            if (f.xong || b < 0 || h == null) {
                return;
            }
            if (f.dua[b].isEmpty()) {
                loi = "Chọn ít nhất 1 Pokémon trước khi khoá.";
            } else {
                final List<HopPc.Con> mau = new ArrayList<HopPc.Con>();
                synchronized (h) {
                    for (final Integer id : f.dua[b]) {
                        final HopPc.Con c = h.lay(id.intValue());
                        if (c == null) {
                            loi = "Pokémon đó không còn trong Hộp PC.";
                            break;
                        }
                        mau.add(c.saoVoiId(c.id));
                    }
                }
                if (loi == null) {
                    f.mau[b] = mau;
                    f.buoc[b] = Math.max(1, f.buoc[b]);
                }
            }
        }
        guiPhien(f, loi != null ? loi : "");
    }

    static void xacNhan(final Char p) {
        final Phien f = phien(p);
        if (f == null) {
            return;
        }
        boolean chot = false;
        synchronized (f) {
            final int b = f.ben(p);
            if (f.xong || b < 0) {
                return;
            }
            if (f.buoc[0] < 1 || f.buoc[1] < 1) {
                guiPhienKhoa(f, "Cả hai bên cần Khoá trước khi trao đổi.");
                return;
            }
            f.buoc[b] = 2;
            if (f.buoc[0] == 2 && f.buoc[1] == 2) {
                f.xong = true; // chi MOT luong vao chot
                chot = true;
            }
        }
        if (chot) {
            chot(f);
        } else {
            guiPhien(f, "");
        }
    }

    static void guiPhienKhoa(final Phien f, final String chu) {
        guiPhien(f, chu);
    }

    // ================================================================== chot (doi nguyen tu)

    static void chot(final Phien f) {
        final Char a = f.ben[0];
        final Char b = f.ben[1];
        String loi = lyDoCoBan(a, b);
        final HopPc hA = BatPokemonGame.hop(a);
        final HopPc hB = BatPokemonGame.hop(b);
        if (loi == null && (hA == null || hB == null)) {
            loi = "Hộp PC tạm thời không đọc được, thử lại sau.";
        }
        // chong clone HAI chieu (moi ben vua gui vua nhan gia tri): cap, tuoi tai khoan, gio choi, cung may / IP
        if (loi == null && (ChongClone.chanChuyen(a, b, a, KIEU_CHONG_CLONE, 0L) || ChongClone.chanChuyen(b, a, b, KIEU_CHONG_CLONE, 0L))) {
            loi = "Trao đổi bị chặn bởi luật chống clone.";
        }
        final long phiA = loi == null ? phiNhan(a, f.mau[1]) : 0L;
        final long phiB = loi == null ? phiNhan(b, f.mau[0]) : 0L;
        if (loi == null && a.xu < phiA) {
            loi = a.charname + " cần " + BatPokemonGame.xu(phiA) + " phí trao đổi.";
        }
        if (loi == null && b.xu < phiB) {
            loi = b.charname + " cần " + BatPokemonGame.xu(phiB) + " phí trao đổi.";
        }
        if (loi != null) {
            moLai(f, loi);
            return;
        }
        // tru phi truoc (hoan neu ghi hong) - sap may truoc khi luu nhan vat: xu va Hop PC cung chua ghi, khong lech
        if (!a.truNeuDu(() -> a.xu >= phiA, () -> a.subXu(phiA, true, "phi trao doi pokemon"))) {
            moLai(f, a.charname + " không đủ xu phí.");
            return;
        }
        if (!b.truNeuDu(() -> b.xu >= phiB, () -> b.subXu(phiB, true, "phi trao doi pokemon"))) {
            a.addXu(phiA, "hoan phi trao doi pokemon");
            moLai(f, b.charname + " không đủ xu phí.");
            return;
        }
        final HashMap<Integer, Integer> thd = new HashMap<Integer, Integer>();
        for (final BatPokemon.Loai l : BatPokemonGame.LOAI.values()) {
            if (l.tienHoaTraoDoi && l.tienHoaThanh > 0) {
                thd.put(Integer.valueOf(l.dex), Integer.valueOf(l.tienHoaThanh));
            }
        }
        final long bayGiay = System.currentTimeMillis() / 1000L;
        final TraoDoiPokemon.KetQua kq = TraoDoiPokemon.thucHien(a.charDBID, hA, f.mau[0], b.charDBID, hB, f.mau[1], bayGiay,
                BatPokemonGame.homNay(), luat(), thd, (idA, sA, idB, sB, aDua, bDua) -> DatabaseBoNgoc.luuTraoDoi(idA, a.charname, sA,
                        idB, b.charname, sB, moTa(aDua), moTa(bDua), phiA, phiB));
        if (!kq.ok) {
            a.addXu(phiA, "hoan phi trao doi pokemon");
            b.addXu(phiB, "hoan phi trao doi pokemon");
            moLai(f, kq.loi);
            return;
        }
        LauDai.ghiXuRa(phiA + phiB);
        PHIEN.remove(Integer.valueOf(a.charDBID), f);
        PHIEN.remove(Integer.valueOf(b.charDBID), f);
        xong(a, kq.aNhan, kq.tienHoa);
        xong(b, kq.bNhan, kq.tienHoa);
        final String ct = "A dua " + moTa(f.mau[0]) + " | B dua " + moTa(f.mau[1]) + " | phi " + phiA + "/" + phiB;
        ChongClone.ghi(a, b, KIEU_CHONG_CLONE, phiA, ct);
        ChongClone.ghi(b, a, KIEU_CHONG_CLONE, phiB, ct);
        try {
            data.Database.instance.saveOrtherLog("", a.charname, "trao doi voi " + b.charname + ": " + ct, "traodoipokemon");
            data.Database.instance.saveOrtherLog("", b.charname, "trao doi voi " + a.charname + ": " + ct, "traodoipokemon");
            // 0.9.8 dong bo: diem moc nhiem vu / danh hieu chung (kieu trao_doi_pokemon) cho ca hai ben
            NhiemVuEngine.suKien(a, "trao_doi_pokemon", "", 1);
            NhiemVuEngine.suKien(b, "trao_doi_pokemon", "", 1);
        } catch (final Exception ignored) {
        }
    }

    /** Huy buoc xac nhan (giu de nghi), bao ly do cho ca hai. */
    static void moLai(final Phien f, final String loi) {
        synchronized (f) {
            f.xong = false;
            f.buoc[0] = 0;
            f.buoc[1] = 0;
        }
        guiPhien(f, loi);
    }

    static void xong(final Char p, final List<HopPc.Con> nhan, final List<int[]> tienHoa) {
        final Pokedex d = PokedexPokemon.lay(p);
        final StringBuilder sb = new StringBuilder();
        for (final HopPc.Con c : nhan) {
            if (d != null) {
                d.ghiBat(c.dex, c.shiny, System.currentTimeMillis() / 1000L); // nhan qua trao doi cung ghi "Đã bắt"
            }
            sb.append(sb.length() > 0 ? ", " : "").append(BatPokemonGame.ten(c.dex));
        }
        String chu = "Trao đổi xong! Nhận " + sb;
        for (final int[] t : tienHoa) {
            for (final HopPc.Con c : nhan) {
                if (c.dex == t[1]) {
                    chu += ". " + BatPokemonGame.ten(t[0]) + " đã tiến hoá thành " + BatPokemonGame.ten(t[1]) + "!";
                    break;
                }
            }
        }
        try {
            final Message m = tao(S_XONG);
            m.dos.writeUTF(chu);
            ghiDs(m.dos, nhan, p);
            p.sendMessage(m);
        } catch (final Exception ignored) {
        }
        com.htbb.thongbao.ThongBao.gui(p, com.htbb.thongbao.ThongBao.HIEM, "Trao đổi Pokémon thành công", chu,
                com.htbb.thongbao.ThongBao.HINH_POKEMON, nhan.isEmpty() ? 0 : nhan.get(0).dex);
        BatPokemonGame.guiTrangThai(p);
        BatPokemonGame.guiHop(p);
    }

    static String moTa(final List<HopPc.Con> ds) {
        final StringBuilder sb = new StringBuilder();
        for (final HopPc.Con c : ds) {
            sb.append(sb.length() > 0 ? "," : "").append(c.dex).append(" c").append(c.cap).append(c.shiny ? " s" : "")
                    .append(" iv").append(CaThePokemon.phanTram(c.iv)).append(" #").append(c.id);
        }
        return sb.toString();
    }

    /** Phi nguoi {@code p} tra cho cac con se NHAN. */
    static long phiNhan(final Char p, final List<HopPc.Con> nhan) {
        final long xmg = BoNgocGame.xuMoiGio(p);
        long t = 0L;
        for (final HopPc.Con c : nhan) {
            final BatPokemon.Loai l = BatPokemonGame.LOAI.get(Integer.valueOf(c.dex));
            t += TraoDoiPokemon.phi(xmg, LauDai.so(K_PHI, 50), LauDai.so(K_PHI_TRAN, 2000), l != null ? l.doHiem : 0, c.shiny,
                    CaThePokemon.sao(c.iv));
        }
        return t;
    }

    // ================================================================== huy / thoat

    static void huy(final Char p, final String lyDo) {
        final Phien f = PHIEN.get(Integer.valueOf(p.charDBID));
        LOI_MOI.remove(Integer.valueOf(p.charDBID));
        if (f == null) {
            return;
        }
        synchronized (f) {
            if (f.xong) {
                return; // dang chot: de luong chot xong (hoac mo lai)
            }
            f.xong = true;
        }
        for (final Char c : f.ben) {
            PHIEN.remove(Integer.valueOf(c.charDBID), f);
            dong(c, lyDo);
        }
    }

    /** CharManager.remove (qua BatPokemonGame.khiThoat): roi game = huy, khong doi gi. */
    static void khiThoat(final Char p) {
        if (p != null) {
            huy(p, p.charname + " đã rời game, trao đổi bị huỷ.");
        }
    }

    // ================================================================== gui trang thai phien

    static void guiPhien(final Phien f, final String chu) {
        for (int b = 0; b < 2; b++) {
            final Char p = f.ben[b];
            final Char q = f.ben[1 - b];
            final HopPc hp = BatPokemonGame.hop(p);
            final HopPc hq = BatPokemonGame.hop(q);
            if (hp == null || hq == null) {
                continue;
            }
            try {
                final Message m = tao(S_PHIEN);
                final DataOutputStream o = m.dos;
                final List<HopPc.Con> toi = new ArrayList<HopPc.Con>();
                final List<HopPc.Con> doi = new ArrayList<HopPc.Con>();
                synchronized (f) {
                    for (final Integer id : f.dua[b]) {
                        final HopPc.Con c = hp.lay(id.intValue());
                        if (c != null) {
                            toi.add(c);
                        }
                    }
                    for (final Integer id : f.dua[1 - b]) {
                        final HopPc.Con c = hq.lay(id.intValue());
                        if (c != null) {
                            doi.add(c);
                        }
                    }
                    o.writeUTF(q.charname);
                    o.writeByte(f.buoc[b]);
                    o.writeByte(f.buoc[1 - b]);
                }
                ghiDs(o, toi, p);
                ghiDs(o, doi, p);
                o.writeInt((int) Math.min(Integer.MAX_VALUE, phiNhan(p, doi)));
                o.writeUTF(chu == null ? "" : chu);
                p.sendMessage(m);
            } catch (final Exception ignored) {
            }
        }
    }

    static void ghiDs(final DataOutputStream o, final List<HopPc.Con> ds, final Char p) throws java.io.IOException {
        o.writeByte(ds.size());
        for (final HopPc.Con c : ds) {
            final BatPokemon.Loai l = BatPokemonGame.LOAI.get(Integer.valueOf(c.dex));
            o.writeInt(c.id);
            o.writeShort(c.dex);
            o.writeByte(c.cap);
            o.writeByte(c.shiny ? 1 : 0);
            o.writeByte(CaThePokemon.sao(c.iv));
            o.writeByte(CaThePokemon.phanTram(c.iv));
            o.writeShort(Math.min(Short.MAX_VALUE, CaThePokemon.sucManh(l != null ? l.chiSoGoc : null, c.iv, c.tinhCach, c.cap)));
            o.writeByte(l != null ? l.doHiem : 0);
            o.writeByte(l != null && l.tienHoaTraoDoi ? 1 : 0);
            o.writeUTF(BatPokemonGame.ten(c.dex));
        }
    }
}
