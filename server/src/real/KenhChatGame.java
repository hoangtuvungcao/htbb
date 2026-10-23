package real;

import com.htbb.pokemon.KenhChatLogic;
import com.htbb.pokemon.KenhChatLogic.Loi;
import com.htbb.pokemon.KenhChatLogic.Tin;
import io.Message;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.WeakHashMap;

/**
 * KENH CHAT (0.9.8) - phan noi vao game cua com.htbb.pokemon.KenhChatLogic: dinh tuyen tin theo kenh (ban do, the
 * gioi, bang hoi, doi, rieng), kiem thanh vien / cam chat / cap / xu / gian cach, chong spam (ChongSpam) + tu xau
 * (BadWord), lich su trong bo nho (khong gui lai khi vao game - chat chi trong phien), danh sach chan
 * (tob_chat_chan), trang thai online cua ban be.
 *
 * <p>Client &gt;= 908 nhan goi -124; client cu nhan goi cu (the gioi -60, bang -18, rieng -5, chat doi -5 tu the
 * "Nhóm"). Cac duong cu (loa -31, -18, -5) di qua cung ham phat o day nen nguoi dung client moi van thay tin cua
 * nguoi dung client cu va nguoc lai.
 *
 * <p>Diem noi: MapXuLyLenh (lenh -124, vao ban do), MapXaHoi (chat ban do, tin rieng -5), MapDoi (chat bang -18),
 * MapNhapText (loa), CharManager.kickPlayer (ban offline), TeamServer (tao bang, cau hinh).
 */
public final class KenhChatGame {

    private KenhChatGame() {
    }

    // ================================================================== diem cam (kiem thu thay duoc)

    /** CSDL: danh sach chan (tin chat KHONG luu - kieu Lang La: chi trong phien). */
    public interface Kho {
        void taoBang();

        List<String> docChan(int idChar);

        void ghiChan(int idChar, String ten, boolean chan);
    }

    public interface TimNguoi {
        Char theoTen(String ten);

        List<Char> tatCa();

        /** Thanh vien bang dang online (ke ca chinh minh). */
        List<Char> bang(Char p);
    }

    public static volatile Kho kho = new KhoCsdl();
    public static volatile TimNguoi tim = new TimThat();

    // ================================================================== trang thai moi phien

    static final class TT {
        boolean daGuiDau;
        boolean napChan;
        final Set<String> chan = new LinkedHashSet<String>();
        long lucTheGioi;
    }

    private static final java.util.Map<Char, TT> TRANG_THAI = Collections.synchronizedMap(new WeakHashMap<Char, TT>());

    static TT tt(final Char p) {
        synchronized (TRANG_THAI) {
            TT t = TRANG_THAI.get(p);
            if (t == null) {
                t = new TT();
                TRANG_THAI.put(p, t);
            }
            return t;
        }
    }

    /** Danh sach chan cua p (khoa ten thuong), nap tu CSDL lan dau. */
    static Set<String> chan(final Char p) {
        final TT t = tt(p);
        synchronized (t) {
            if (!t.napChan) {
                t.napChan = true;
                if (p.charDBID > 0) {
                    try {
                        for (final String s : kho.docChan(p.charDBID)) {
                            t.chan.add(KenhChatLogic.khoaTen(s));
                        }
                    } catch (final Exception e) {
                        // CSDL loi: danh sach rong
                    }
                }
            }
            return t.chan;
        }
    }

    /** nguoi nhan da chan ten gui? */
    public static boolean daChan(final Char nguoiNhan, final String tenGui) {
        if (nguoiNhan == null) {
            return false;
        }
        final Set<String> c = chan(nguoiNhan);
        synchronized (tt(nguoiNhan)) {
            return c.contains(KenhChatLogic.khoaTen(tenGui));
        }
    }

    public static void taoBang() {
        try {
            kho.taoBang();
        } catch (final Exception e) {
            System.out.println("[KENH CHAT] loi tao bang: " + e);
        }
    }

    // ================================================================== tien ich

    public static boolean coGoi(final Char p) {
        return p != null && p.isBot == -1 && p.getSession() != null && p.getSession().banClient >= KenhChatLogic.BAN_CLIENT;
    }

    interface Viet {
        void viet(java.io.DataOutputStream o) throws IOException;
    }

    static Message goi(final Viet v) {
        final Message m = new Message(KenhChatLogic.LENH);
        try {
            v.viet(m.dos);
        } catch (final IOException e) {
            // ByteArrayOutputStream khong nem
        }
        return m;
    }

    static Message goiTin(final Tin t, final int coThem) {
        return goi(o -> KenhChatLogic.viet(o, t, coThem));
    }

    static void ketQua(final Char p, final byte kenh, final byte ma, final int giay, final String lyDo) {
        if (coGoi(p)) {
            p.sendMessage(goi(o -> KenhChatLogic.vietKetQua(o, kenh, ma, giay, lyDo)));
        } else if (lyDo != null && ma != KenhChatLogic.KQ_DA_GUI) {
            p.sendMessage(MessageCreator.createMsgChat(p.id, lyDo));
        }
    }

    static String ten(final Char p) {
        return p.charname == null ? "" : p.charname;
    }

    /** Con bi cam chat (GM camchat / the cam chat / cam tu dong cu): ly do, null = khong. */
    static String biCam(final Char p) {
        if (p.isAdmin) {
            return null;
        }
        if (p.forbitChat > 0) {
            return "Bạn đang bị cấm chat, còn " + p.forbitChat + " phút.";
        }
        try {
            if (p.dayCamChat != null && !p.dayCamChat.isEmpty() && Char.getDayTime(0L).compareTo(p.dayCamChat) < 0) {
                return "Bạn đang bị cấm chat tới " + p.dayCamChat + ".";
            }
        } catch (final Exception e) {
            // chuoi ngay hong: coi nhu khong cam
        }
        if (System.currentTimeMillis() - p.timecamchat < 0L) {
            return "Bạn đang bị cấm chat.";
        }
        return null;
    }

    static KenhChatLogic.NguoiGui nguoiGui(final Char p) {
        final KenhChatLogic.NguoiGui n = new KenhChatLogic.NguoiGui();
        n.cap = p.lvDetail == null ? 0 : p.lvDetail.lv;
        n.xu = p.getxu();
        n.admin = p.isAdmin;
        n.biCam = biCam(p);
        n.coBang = p.idClan != -1;
        n.coDoi = p.party != null && p.party.userParty != null && p.party.userParty.size() > 1;
        n.lucTheGioi = tt(p).lucTheGioi;
        return n;
    }

    /** Tu xau + quang cao (sau ChongSpam): null = tin hong (da bao nguoi gui). */
    static String locChu(final Char p, final byte kenh, final String goc) {
        if (BadWord.checkBadWord(goc)) {
            ketQua(p, kenh, KenhChatLogic.KQ_LOI, 0, "Nội dung không hợp lệ. Người chơi khác sẽ không thấy câu này.");
            return null;
        }
        String s = Map.checkBadWord(goc);
        s = Map.replaceStringWithSpace(s);
        s = Map.replaceExtendDomain(s);
        s = BadWord.checkBadWord2(s);
        s = KenhChatLogic.chuan(s);
        return s;
    }

    static com.htbb.chongspam.ChongSpam.Kenh kenhSpam(final byte kenh) {
        switch (kenh) {
            case KenhChatLogic.THE_GIOI:
                return com.htbb.chongspam.ChongSpam.Kenh.THE_GIOI;
            case KenhChatLogic.BANG:
                return com.htbb.chongspam.ChongSpam.Kenh.BANG;
            case KenhChatLogic.DOI:
                return com.htbb.chongspam.ChongSpam.Kenh.DOI;
            case KenhChatLogic.RIENG:
                return com.htbb.chongspam.ChongSpam.Kenh.RIENG;
            default:
                return com.htbb.chongspam.ChongSpam.Kenh.GAN;
        }
    }

    /** Chong spam (khong bao chung: bao bang goi ket qua cua kenh). true = duoc. */
    static boolean quaSpam(final Char p, final byte kenh, final String goc) {
        if (com.htbb.chongspam.ChongSpam.rong(goc)) {
            return false;
        }
        if (p.isAdmin) {
            return true;
        }
        final long bayGio = System.currentTimeMillis();
        final com.htbb.chongspam.ChongSpam.TrangThai t = com.htbb.chongspam.ChongSpam.cua(Integer.valueOf(p.charDBID));
        final long camTruoc = t.conCam(bayGio);
        final com.htbb.chongspam.ChongSpam.KetQua kq = com.htbb.chongspam.ChongSpam.kiemChat(t, kenhSpam(kenh), goc, bayGio);
        if (kq == com.htbb.chongspam.ChongSpam.KetQua.CHO_PHEP) {
            return true;
        }
        server.ChiSoMayChu.CHAT_CHAN.incrementAndGet();
        if (camTruoc == 0L && t.conCam(bayGio) > 0L) {
            com.htbb.log.Nhat.kiemToan("CHONG_SPAM", "tu dong cam chat " + com.htbb.log.Nhat.sach(ten(p)) + " "
                    + (t.conCam(bayGio) / 1000L) + "s (kenh " + kenhSpam(kenh) + ", ly do " + kq + "): " + com.htbb.log.Nhat.sach(goc));
        }
        final String lyDo = kq == com.htbb.chongspam.ChongSpam.KetQua.DANG_CAM ? com.htbb.chongspam.ChongSpam.lyDo(kq, t, bayGio)
                : KenhChatLogic.lyDoSpam(kq);
        ketQua(p, kenh, KenhChatLogic.KQ_LOI, 0, lyDo);
        return false;
    }

    static void ghiNhatKy(final byte kenh, final Char p, final String dich, final String s) {
        try {
            com.htbb.log.Nhat.thongTin("CHAT", "[" + KenhChatLogic.TEN_KENH[kenh] + "] " + com.htbb.log.Nhat.sach(ten(p))
                    + (dich == null || dich.isEmpty() ? "" : " -> " + com.htbb.log.Nhat.sach(dich)) + ": " + com.htbb.log.Nhat.sach(s));
        } catch (final Throwable e) {
            // nhat ky khong duoc lam hong chat
        }
    }

    // ================================================================== lenh -124 tu client

    public static void xuLy(final Char p, final Message m) {
        try {
            final int op = m.dis.readByte();
            switch (op) {
                case KenhChatLogic.K_XIN:
                    guiDau(p, true);
                    break;
                case KenhChatLogic.K_GUI: {
                    final byte kenh = m.dis.readByte();
                    final String dich = m.dis.readUTF();
                    final String noiDung = m.dis.readUTF();
                    gui(p, kenh, dich, noiDung);
                    break;
                }
                case KenhChatLogic.K_CHAN: {
                    final boolean bat = m.dis.readByte() != 0;
                    datChan(p, m.dis.readUTF(), bat);
                    break;
                }
                case KenhChatLogic.K_XIN_BAN:
                    guiBan(p);
                    break;
                case KenhChatLogic.K_TRA_ID: {
                    final byte viec = m.dis.readByte();
                    traId(p, viec, m.dis.readUTF());
                    break;
                }
                default:
                    break;
            }
        } catch (final Exception e) {
            // goi hong
        }
    }

    /**
     * Gui mot tin theo kenh (lenh -124). @return null = da gui; khac = ly do khong gui (da bao nguoi gui).
     */
    public static String gui(final Char p, final byte kenh, final String dich, final String noiDungGoc) {
        if (p == null || noiDungGoc == null) {
            return "rong";
        }
        if (kenh == KenhChatLogic.BAN_DO) {
            // chat ban do: di dung duong lenh 27 cu (lenh admin, dap an nhiem vu, he sinh thai...)
            try {
                final java.io.ByteArrayOutputStream b = new java.io.ByteArrayOutputStream();
                new java.io.DataOutputStream(b).writeUTF(noiDungGoc);
                if (p.map != null) {
                    p.map.doChat(p, new Message(27, b.toByteArray()));
                }
            } catch (final Exception e) {
                return "loi";
            }
            return null;
        }
        final long bayGio = System.currentTimeMillis();
        final Loi loi = KenhChatLogic.kiem(kenh, nguoiGui(p), bayGio);
        if (loi != null) {
            ketQua(p, kenh, loi.ma, loi.giayCho, loi.lyDo);
            return loi.lyDo;
        }
        if (kenh == KenhChatLogic.THE_GIOI && com.htbb.pokemon.ThanhVien.chanVaBao(p, "chat_the_gioi")) {
            ketQua(p, kenh, KenhChatLogic.KQ_LOI, 0, null);
            return "thanh vien";
        }
        if (kenh == KenhChatLogic.RIENG && com.htbb.pokemon.ThanhVien.chanVaBao(p, "gui_thu")) {
            ketQua(p, kenh, KenhChatLogic.KQ_LOI, 0, null);
            return "thanh vien";
        }
        if (kenh == KenhChatLogic.THE_GIOI) {
            tt(p).lucTheGioi = bayGio; // lan thu toi chong spam tinh vao gian cach (bao "cho N giay", khong vi pham)
        }
        if (!quaSpam(p, kenh, noiDungGoc)) {
            return "spam";
        }
        final String s = locChu(p, kenh, noiDungGoc);
        if (s == null) {
            return "tu xau";
        }
        switch (kenh) {
            case KenhChatLogic.THE_GIOI:
                return theGioi(p, s);
            case KenhChatLogic.BANG:
                phatBang(p, s);
                return null;
            case KenhChatLogic.DOI:
                phatDoi(p, s);
                return null;
            case KenhChatLogic.RIENG:
                return rieng(p, dich, s);
            default:
                return "kenh";
        }
    }

    // ------------------------------------------------------------------ the gioi

    static String theGioi(final Char p, final String s) {
        final int gia = p.isAdmin ? 0 : KenhChatLogic.xuTheGioi;
        if (gia > 0) {
            if (!p.truXuNeuDu(gia, false, "chat the gioi")) {
                tt(p).lucTheGioi = 0L;
                ketQua(p, KenhChatLogic.THE_GIOI, KenhChatLogic.KQ_LOI, 0, "Cần " + gia + " xu để chat thế giới.");
                return "xu";
            }
            p.sendMessage(MessageCreator.createCharInventoryMessage(p, 0));
        }
        phatTheGioi(p, s, false);
        ketQua(p, KenhChatLogic.THE_GIOI, KenhChatLogic.KQ_DA_GUI, KenhChatLogic.giayTheGioi,
                gia > 0 ? "Đã gửi (-" + gia + " xu)." : "Đã gửi.");
        return null;
    }

    /**
     * Phat len kenh the gioi: client moi nhan -124 (the Thế giới), client cu -60 (chu chay). loa = duong loa cu
     * (MapNhapText) da tu gui -60 cho moi nguoi - o day chi them lich su + -124.
     */
    public static void phatTheGioi(final Char p, final String s, final boolean loa) {
        final Tin t = new Tin(KenhChatLogic.THE_GIOI, System.currentTimeMillis(), p.id, ten(p), "", s, "",
                (loa ? KenhChatLogic.CO_LOA : 0) | (p.isAdmin ? KenhChatLogic.CO_GM : 0));
        KenhChatLogic.ghiTheGioi(t);
        ghiNhatKy(KenhChatLogic.THE_GIOI, p, null, s);
        final Message moi = goiTin(t, 0);
        final Message cu = loa ? null : MessageCreator.createMsgSpeaker(t.dongCu());
        for (final Char q : tim.tatCa()) {
            if (q == null || q.isBot != -1) {
                continue;
            }
            if (coGoi(q)) {
                if (q == p) {
                    q.sendMessage(goiTin(t, KenhChatLogic.CO_CUA_MINH));
                } else if (!daChan(q, t.tenGui)) {
                    q.sendMessage(moi);
                }
            } else if (cu != null) {
                q.sendMessage(cu);
            }
        }
    }

    // ------------------------------------------------------------------ bang hoi

    static String nhanBang(final Char p) {
        if (p.rankClan < 3 && p.rankClan > -1) {
            return new String[]{"(BC)", "(PB)", "(TL)"}[p.rankClan];
        }
        return "";
    }

    /** Chat bang (lenh -18 cu va -124): client moi -124, client cu -18 "nhan ten: noi dung". */
    public static void phatBang(final Char p, final String s) {
        final Tin t = new Tin(KenhChatLogic.BANG, System.currentTimeMillis(), p.id, ten(p), "", s, nhanBang(p),
                p.isAdmin ? KenhChatLogic.CO_GM : 0);
        KenhChatLogic.ghiBang(p.idClan, t);
        ghiNhatKy(KenhChatLogic.BANG, p, "bang " + p.idClan, s);
        final Message moi = goiTin(t, 0);
        Message cu = null;
        for (final Char q : tim.bang(p)) {
            if (q == null) {
                continue;
            }
            if (coGoi(q)) {
                if (q == p) {
                    q.sendMessage(goiTin(t, KenhChatLogic.CO_CUA_MINH));
                } else if (!daChan(q, t.tenGui)) {
                    q.sendMessage(moi);
                }
            } else {
                if (cu == null) {
                    cu = new Message(-18);
                    try {
                        cu.dos.writeUTF(t.dongCu());
                    } catch (final IOException e) {
                        // khong nem
                    }
                }
                q.sendMessage(cu);
            }
        }
    }

    // ------------------------------------------------------------------ doi (nhom)

    static List<Char> doi(final Char p) {
        final List<Char> r = new ArrayList<Char>();
        if (p.party == null || p.party.userParty == null) {
            return r;
        }
        synchronized (p.party.userParty) {
            for (final Object o : p.party.userParty) {
                if (o instanceof Char) {
                    r.add((Char) o);
                }
            }
        }
        return r;
    }

    /** Chat doi: chi thanh vien doi; client cu nhan -5 tu the "Nhóm". */
    public static void phatDoi(final Char p, final String s) {
        final List<Char> ds = doi(p);
        if (!ds.contains(p)) {
            ketQua(p, KenhChatLogic.DOI, KenhChatLogic.KQ_LOI, 0, "Bạn chưa vào nhóm.");
            return;
        }
        final Tin t = new Tin(KenhChatLogic.DOI, System.currentTimeMillis(), p.id, ten(p), "", s, "", p.isAdmin ? KenhChatLogic.CO_GM : 0);
        ghiNhatKy(KenhChatLogic.DOI, p, "doi " + p.partyID, s);
        final Message moi = goiTin(t, 0);
        final Message cu = goiRiengCu(KenhChatLogic.TEN_NHOM_CU, t.tenGui + ": " + s);
        for (final Char q : ds) {
            if (coGoi(q)) {
                if (q == p) {
                    q.sendMessage(goiTin(t, KenhChatLogic.CO_CUA_MINH));
                } else if (!daChan(q, t.tenGui)) {
                    q.sendMessage(moi);
                }
            } else if (q != p) {
                q.sendMessage(cu);
            }
        }
    }

    // ------------------------------------------------------------------ rieng

    /** Goi -5 cho client cu: mot tin (ten, noi dung) - client mo the tin rieng mang ten do. */
    static Message goiRiengCu(final String ten, final String noiDung) {
        final Message m = new Message(-5);
        try {
            m.dos.writeByte(1);
            m.dos.writeUTF(ten);
            m.dos.writeUTF(noiDung);
        } catch (final IOException e) {
            // khong nem
        }
        return m;
    }

    static String rieng(final Char p, final String dich, final String s) {
        String ten = dich == null ? "" : dich.trim();
        if (ten.indexOf('-') > -1) {
            ten = ten.substring(0, ten.indexOf('-')).trim();
        }
        if (ten.isEmpty()) {
            ketQua(p, KenhChatLogic.RIENG, KenhChatLogic.KQ_LOI, 0, "Chọn người nhận trước.");
            return "dich";
        }
        if (KenhChatLogic.khoaTen(ten).equals(KenhChatLogic.khoaTen(ten(p)))) {
            ketQua(p, KenhChatLogic.RIENG, KenhChatLogic.KQ_LOI, 0, "Không nhắn cho chính mình được.");
            return "minh";
        }
        return giaoRieng(p, ten, s);
    }

    /**
     * Giao mot tin rieng (sau moi kiem): online -&gt; -124 / -5 ngay; offline -&gt; bao nguoi gui "khong truc tuyen, tin
     * khong duoc gui" (khong luu - chat chi trong phien). Nguoi nhan da chan nguoi gui: khong giao (nguoi gui van thay tin cua minh). Dung chung cho lenh -5 cu.
     * @return null = da gui.
     */
    public static String giaoRieng(final Char p, final String tenNhan, final String s) {
        final Char q = tim.theoTen(KenhChatLogic.khoaTen(tenNhan));
        final long bayGio = System.currentTimeMillis();
        if (q == null || q.isBot != -1) {
            // 0.9.8 (chu game): chat chi trong phien nhu Lang La - khong co hop thu offline
            ketQua(p, KenhChatLogic.RIENG, KenhChatLogic.KQ_LOI, 0, tenNhan + " không trực tuyến, tin không được gửi.");
            return "offline";
        }
        final String tenThat = ten(q);
        ghiNhatKy(KenhChatLogic.RIENG, p, tenThat, s);
        final Tin den = new Tin(KenhChatLogic.RIENG, bayGio, p.id, ten(p), ten(p), s, "", p.isAdmin ? KenhChatLogic.CO_GM : 0);
        if (!daChan(q, ten(p))) {
            if (coGoi(q)) {
                q.sendMessage(goiTin(den, 0));
            } else {
                q.sendMessage(goiRiengCu(ten(p), s));
            }
        }
        if (coGoi(p)) {
            p.sendMessage(goiTin(new Tin(KenhChatLogic.RIENG, bayGio, p.id, ten(p), tenThat, s, "", KenhChatLogic.CO_CUA_MINH), 0));
        }
        return null;
    }

    // ------------------------------------------------------------------ chan

    public static void datChan(final Char p, final String tenGoc, final boolean bat) {
        final String k = KenhChatLogic.khoaTen(tenGoc);
        if (k.isEmpty() || k.equals(KenhChatLogic.khoaTen(ten(p)))) {
            return;
        }
        final Set<String> c = chan(p);
        synchronized (tt(p)) {
            if (bat) {
                if (c.contains(k)) {
                    return;
                }
                if (c.size() >= KenhChatLogic.chanToiDa) {
                    ketQua(p, KenhChatLogic.RIENG, KenhChatLogic.KQ_LOI, 0, "Danh sách chặn đã đầy (" + KenhChatLogic.chanToiDa + ").");
                    return;
                }
                c.add(k);
            } else if (!c.remove(k)) {
                return;
            }
        }
        if (p.charDBID > 0) {
            try {
                kho.ghiChan(p.charDBID, k, bat);
            } catch (final Exception e) {
                // CSDL loi: van chan trong phien
            }
        }
        guiChan(p);
        ketQua(p, KenhChatLogic.RIENG, KenhChatLogic.KQ_DA_GUI, 0, bat ? "Đã chặn " + tenGoc.trim() + "." : "Đã bỏ chặn " + tenGoc.trim() + ".");
    }

    static void guiChan(final Char p) {
        if (!coGoi(p)) {
            return;
        }
        final List<String> ds;
        synchronized (tt(p)) {
            ds = new ArrayList<String>(chan(p));
        }
        p.sendMessage(goi(o -> KenhChatLogic.vietTen(o, KenhChatLogic.G_CHAN, ds)));
    }

    // ------------------------------------------------------------------ ban be, tra id

    static List<String> tenBan(final Char p) {
        final List<String> r = new ArrayList<String>();
        if (p.friendListName == null) {
            return r;
        }
        synchronized (p.friendListName) {
            for (final Object o : p.friendListName) {
                if (o != null) {
                    r.add(o.toString());
                }
            }
        }
        return r;
    }

    public static void guiBan(final Char p) {
        if (!coGoi(p)) {
            return;
        }
        final List<String> ds = tenBan(p);
        p.sendMessage(goi(o -> {
            o.writeByte(KenhChatLogic.G_BAN);
            final int n = Math.min(ds.size(), 500);
            o.writeShort(n);
            for (int i = 0; i < n; i++) {
                final Char q = tim.theoTen(KenhChatLogic.khoaTen(ds.get(i)));
                final boolean on = q != null && q.isBot == -1;
                o.writeUTF(ds.get(i));
                o.writeByte(on ? 1 : 0);
                o.writeShort(on && q.lvDetail != null ? q.lvDetail.lv : 0);
            }
        }));
    }

    static void traId(final Char p, final byte viec, final String tenGoc) {
        final Char q = tim.theoTen(KenhChatLogic.khoaTen(tenGoc));
        final boolean on = q != null && q.isBot == -1;
        final short id = on ? q.id : (short) -1;
        final boolean cung = on && q.map != null && q.map == p.map;
        if (!coGoi(p)) {
            return;
        }
        p.sendMessage(goi(o -> {
            o.writeByte(KenhChatLogic.G_ID);
            o.writeByte(viec);
            o.writeUTF(tenGoc == null ? "" : tenGoc);
            o.writeShort(id);
            o.writeByte(cung ? 1 : 0);
        }));
    }

    // ------------------------------------------------------------------ vao game / thoat

    /** Vao ban do (MapXuLyLenh): lan dau trong phien gui cau hinh, chan, ban be, lich su, hop thu; bao ban be online. */
    public static void khiVaoBanDo(final Char p) {
        try {
            if (p == null || p.isBot != -1) {
                return;
            }
            final TT t = tt(p);
            synchronized (t) {
                if (t.daGuiDau) {
                    return;
                }
                t.daGuiDau = true;
            }
            guiDau(p, false);
            baoBan(p, true);
        } catch (final Exception e) {
            System.out.println("KenhChatGame.khiVaoBanDo: " + e);
        }
    }

    /** Cau hinh + chan + ban be + lich su (the gioi, bang) + hop thu offline. */
    static void guiDau(final Char p, final boolean xinLai) {
        if (coGoi(p)) {
            final boolean[] duoc = new boolean[KenhChatLogic.SO_KENH];
            final KenhChatLogic.NguoiGui n = nguoiGui(p);
            for (byte k = 0; k < KenhChatLogic.SO_KENH; k++) {
                final Loi l = KenhChatLogic.kiem(k, n, Long.MAX_VALUE / 2);
                duoc[k] = l == null || l.ma == KenhChatLogic.KQ_CHO || (k == KenhChatLogic.THE_GIOI && l.lyDo.contains("xu"));
            }
            p.sendMessage(goi(o -> KenhChatLogic.vietCauHinh(o, duoc)));
            guiChan(p);
            guiBan(p);
            // 0.9.8 (chu game): chat chi trong phien (kieu Lang La) - vao game KHONG gui lai lich su the gioi / doi, khong hop thu
            guiDoi(p);
        }
    }

    /** Goi -124 loai 7: ten doi (bang hoi) cua p + so thanh vien dang online / tong ("" = chua vao doi). */
    public static void guiDoi(final Char p) {
        if (p == null || !coGoi(p)) {
            return;
        }
        String tenDoi = "";
        int tong = 0;
        int on = 0;
        try {
            final data.NewClan c = p.idClan == -1 ? null : data.NewClan.getClan(p.idClan);
            if (c != null) {
                tenDoi = c.name == null ? "" : c.name;
                tong = c.member == null ? 0 : c.member.size();
                on = tim.bang(p).size();
            }
        } catch (final Exception e) {
            // giu rong
        }
        final String t = tenDoi;
        final int a = on, b = tong;
        p.sendMessage(goi(o -> KenhChatLogic.vietDoi(o, t, a, b)));
    }

    /** Bao ban be (client moi, dang online) rang p vua online / offline. */
    public static void baoBan(final Char p, final boolean online) {
        try {
            final String ten = ten(p);
            for (final String b : tenBan(p)) {
                final Char q = tim.theoTen(KenhChatLogic.khoaTen(b));
                if (q == null || q == p || !coGoi(q)) {
                    continue;
                }
                q.sendMessage(goi(o -> {
                    o.writeByte(KenhChatLogic.G_TRANG_THAI);
                    o.writeUTF(ten);
                    o.writeByte(online ? 1 : 0);
                }));
            }
        } catch (final Exception e) {
            // bo
        }
    }

    /** CharManager.kickPlayer. */
    public static void khiThoat(final Char p) {
        if (p == null || p.isBot != -1) {
            return;
        }
        final TT t;
        synchronized (TRANG_THAI) {
            t = TRANG_THAI.remove(p);
        }
        if (t != null && t.daGuiDau) {
            baoBan(p, false);
        }
    }

    // ================================================================== mac dinh that

    static final class TimThat implements TimNguoi {
        public Char theoTen(final String ten) {
            return ten == null || ten.isEmpty() ? null : CharManager.instance.getCharByCharName(ten);
        }

        public List<Char> tatCa() {
            synchronized (CharManager.instance.vChars) {
                return new ArrayList<Char>(CharManager.instance.vChars);
            }
        }

        public List<Char> bang(final Char p) {
            final List<Char> r = new ArrayList<Char>();
            final data.NewClan c = data.NewClan.getClan(p.idClan);
            if (c == null || c.member == null) {
                return r;
            }
            for (final data.CharClan m : new ArrayList<data.CharClan>(c.member.values())) {
                String ten = m.charname == null ? "" : m.charname;
                if (ten.indexOf('-') > -1) {
                    ten = ten.substring(0, ten.indexOf('-'));
                }
                final Char q = CharManager.instance.getCharByCharName(ten);
                if (q != null && q.isBot == -1) {
                    r.add(q);
                }
            }
            return r;
        }
    }

    /** tob_chat_chan (danh sach chan). Bang tu tao (sql/htbb.sql phan 11 cung vay). tob_chat_cho (hop thu offline) da bo. */
    static final class KhoCsdl implements Kho {
        public void taoBang() {
            data.DatabaseLauDai.ghiCong("CREATE TABLE IF NOT EXISTS tob_chat_chan (id_char INT NOT NULL, ten_chan VARCHAR(45) NOT NULL,"
                    + " luc DATETIME NOT NULL, PRIMARY KEY (id_char, ten_chan)) DEFAULT CHARSET=utf8");
        }

        public List<String> docChan(final int idChar) {
            final List<String> r = new ArrayList<String>();
            Connection conn = null;
            PreparedStatement pre = null;
            ResultSet rs = null;
            try {
                conn = data.Database.instance.getConnection();
                pre = conn.prepareStatement("SELECT ten_chan FROM tob_chat_chan WHERE id_char = ? ORDER BY luc LIMIT 1000");
                pre.setInt(1, idChar);
                rs = pre.executeQuery();
                while (rs.next()) {
                    r.add(rs.getString(1));
                }
            } catch (final Exception e) {
                // rong
            } finally {
                XepHangPvpGame.dong(rs, pre, conn);
            }
            return r;
        }

        public void ghiChan(final int idChar, final String ten, final boolean chan) {
            // xoa roi them (khong dung INSERT IGNORE: NovaDB may dev)
            data.DatabaseLauDai.ghiCong("DELETE FROM tob_chat_chan WHERE id_char = ? AND ten_chan = ?", Integer.valueOf(idChar), ten);
            if (chan) {
                data.DatabaseLauDai.ghiCong("INSERT INTO tob_chat_chan (id_char, ten_chan, luc) VALUES (?, ?, NOW())",
                        Integer.valueOf(idChar), ten);
            }
        }
    }
}
