import com.htbb.pokemon.KenhChatLogic;
import io.Message;
import io.Session;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import real.Char;
import real.KenhChatGame;

/**
 * KENH CHAT (0.9.8, goi -124, client ban &gt;= 908): luat tung kenh (cap, xu, gian cach the gioi, thanh vien bang /
 * doi, cam chat GM), dinh tuyen (the gioi toi moi nguoi, bang chi thanh vien, doi chi nguoi trong doi, rieng toi dung
 * nguoi), chong spam (trung lap, link), danh sach chan, tin rieng cho nguoi OFFLINE khong gui (chat chi trong phien) (
 * day / qua han), client cu (&lt; 908) nhan goi cu (-60 / -18 / -5), lan dau vao ban do gui cau hinh + lich su mot
 * lan, ban be online / offline, va noi vao ma (lenh -124, -18, -5, loa) + SQL / danh muc cau hinh.
 */
public final class KiemKenhChat {

    private KiemKenhChat() {
    }

    /** goi da gui toi tung nhan vat */
    static final Map<Char, List<Message>> GOI = new HashMap<Char, List<Message>>();
    static int idKe = 81000;

    static Char nv(final String ten, final int ban, final int cap) {
        final Char p = new Char(null);
        final List<Message> ds = new ArrayList<Message>();
        GOI.put(p, ds);
        p.setSession(new Session(null, null, true) {
            @Override
            public void sendMessage(final Message m) {
                ds.add(m);
            }
        });
        p.getSession().banClient = ban;
        p.charname = ten;
        p.charDBID = idKe;
        p.id = (short) (idKe - 80000);
        idKe++;
        p.lvDetail.lv = cap;
        p.isBot = -1;
        p.idClan = -1;
        p.setxu(0);
        return p;
    }

    // ------------------------------------------------------------------ gia lap CSDL / danh ba

    static final class KhoGia implements KenhChatGame.Kho {
        final Map<Integer, List<String>> chan = new HashMap<Integer, List<String>>();
        int ghiChan;

        public void taoBang() {
        }

        public List<String> docChan(final int idChar) {
            final List<String> l = chan.get(Integer.valueOf(idChar));
            return l == null ? new ArrayList<String>() : new ArrayList<String>(l);
        }

        public void ghiChan(final int idChar, final String ten, final boolean bat) {
            ghiChan++;
            List<String> l = chan.get(Integer.valueOf(idChar));
            if (l == null) {
                l = new ArrayList<String>();
                chan.put(Integer.valueOf(idChar), l);
            }
            l.remove(ten);
            if (bat) {
                l.add(ten);
            }
        }
    }

    static final class TimGia implements KenhChatGame.TimNguoi {
        final List<Char> online = new ArrayList<Char>();
        final Map<Integer, List<Char>> bang = new HashMap<Integer, List<Char>>();

        public Char theoTen(final String ten) {
            for (final Char c : online) {
                if (KenhChatLogic.khoaTen(c.charname).equals(KenhChatLogic.khoaTen(ten))) {
                    return c;
                }
            }
            return null;
        }

        public List<Char> tatCa() {
            return new ArrayList<Char>(online);
        }

        public List<Char> bang(final Char p) {
            final List<Char> l = bang.get(Integer.valueOf(p.idClan));
            final List<Char> r = new ArrayList<Char>();
            if (l != null) {
                for (final Char c : l) {
                    if (online.contains(c)) {
                        r.add(c);
                    }
                }
            }
            return r;
        }
    }

    // ------------------------------------------------------------------ doc goi

    static final class Goi {
        int lenh;
        byte[] du;

        DataInputStream doc() {
            return new DataInputStream(new ByteArrayInputStream(du));
        }
    }

    static Goi giai(final Message m) {
        final byte[] b = m.toByteArray();
        final Goi g = new Goi();
        if (b[0] == -128) {
            g.lenh = b[1];
            g.du = java.util.Arrays.copyOfRange(b, 6, b.length);
        } else {
            g.lenh = b[0];
            g.du = java.util.Arrays.copyOfRange(b, 3, b.length);
        }
        return g;
    }

    static List<Goi> goi(final Char p) {
        final List<Goi> r = new ArrayList<Goi>();
        for (final Message m : GOI.get(p)) {
            r.add(giai(m));
        }
        return r;
    }

    static void xoa() {
        for (final List<Message> l : GOI.values()) {
            l.clear();
        }
    }

    /** Mot tin -124 G_TIN da doc. */
    static final class TinDoc {
        int kenh, co;
        long luc;
        short id;
        String ten, doiPhuong, noiDung, nhan;
    }

    static List<TinDoc> tin(final Char p, final int kenh) {
        final List<TinDoc> r = new ArrayList<TinDoc>();
        for (final Goi g : goi(p)) {
            if (g.lenh != KenhChatLogic.LENH || g.du[0] != KenhChatLogic.G_TIN) {
                continue;
            }
            try {
                final DataInputStream d = g.doc();
                d.readByte();
                final TinDoc t = new TinDoc();
                t.kenh = d.readByte();
                t.co = d.readByte();
                t.luc = d.readLong();
                t.id = d.readShort();
                t.ten = d.readUTF();
                t.doiPhuong = d.readUTF();
                t.noiDung = d.readUTF();
                t.nhan = d.readUTF();
                if (kenh < 0 || t.kenh == kenh) {
                    r.add(t);
                }
            } catch (final Exception e) {
                Kiem.dung("doc goi tin -124 khong loi: " + e, false);
            }
        }
        return r;
    }

    /** {ma, giay, ly do} cua goi ket qua cuoi (null = khong co). */
    static Object[] ketQua(final Char p) {
        Object[] r = null;
        for (final Goi g : goi(p)) {
            if (g.lenh == KenhChatLogic.LENH && g.du[0] == KenhChatLogic.G_KET_QUA) {
                try {
                    final DataInputStream d = g.doc();
                    d.readByte();
                    d.readByte();
                    final int ma = d.readByte();
                    final int giay = d.readShort();
                    r = new Object[]{Integer.valueOf(ma), Integer.valueOf(giay), d.readUTF()};
                } catch (final Exception e) {
                    Kiem.dung("doc goi ket qua: " + e, false);
                }
            }
        }
        return r;
    }

    static int dem(final Char p, final int lenh) {
        int n = 0;
        for (final Goi g : goi(p)) {
            n += g.lenh == lenh ? 1 : 0;
        }
        return n;
    }

    static String chuoiCu(final Char p, final int lenh) {
        for (final Goi g : goi(p)) {
            if (g.lenh == lenh) {
                try {
                    final DataInputStream d = g.doc();
                    if (lenh == -5) {
                        d.readByte();
                        return d.readUTF() + "|" + d.readUTF();
                    }
                    return d.readUTF();
                } catch (final Exception e) {
                    return "loi " + e;
                }
            }
        }
        return null;
    }

    // ================================================================== chay

    public static void chay() {
        Kiem.nhom("Kenh chat 0.9.8 - luat kenh, the gioi / bang / doi / rieng, chan, hop thu offline, client cu, noi ma");
        final KenhChatGame.Kho khoCu = KenhChatGame.kho;
        final KenhChatGame.TimNguoi timCu = KenhChatGame.tim;
        final real.GemTemplate[] gemCu = real.Map.gemTemplate;
        try {
            if (real.Map.gemTemplate == null) {
                real.Map.gemTemplate = new real.GemTemplate[0];
            }
            com.htbb.chongspam.ChongSpam.datLaiChoKiemThu();
            KenhChatLogic.datLaiChoKiemThu();
            luat();
            game();
        } catch (final Throwable e) {
            e.printStackTrace();
            Kiem.dung("kenh chat: khong nem loi: " + e, false);
        } finally {
            real.Map.gemTemplate = gemCu;
            KenhChatGame.kho = khoCu;
            KenhChatGame.tim = timCu;
            KenhChatLogic.datLaiChoKiemThu();
            com.htbb.chongspam.ChongSpam.datLaiChoKiemThu();
        }
        noiMa();
    }

    static void luat() {
        final KenhChatLogic.NguoiGui n = new KenhChatLogic.NguoiGui();
        n.cap = 9;
        n.xu = 1000;
        final long t = 1000000L;
        Kiem.dung("cap 9 < 10: khong chat the gioi", KenhChatLogic.kiem(KenhChatLogic.THE_GIOI, n, t) != null);
        n.cap = 10;
        Kiem.dung("cap 10, du xu: chat the gioi duoc", KenhChatLogic.kiem(KenhChatLogic.THE_GIOI, n, t) == null);
        n.lucTheGioi = t - 10000L;
        final KenhChatLogic.Loi cho = KenhChatLogic.kiem(KenhChatLogic.THE_GIOI, n, t);
        Kiem.dung("gui 10 giay truoc: phai cho", cho != null && cho.ma == KenhChatLogic.KQ_CHO);
        Kiem.bang("con 20 giay", Integer.valueOf(20), Integer.valueOf(cho == null ? -1 : cho.giayCho));
        n.lucTheGioi = t - 30000L;
        Kiem.dung("du 30 giay: gui lai duoc", KenhChatLogic.kiem(KenhChatLogic.THE_GIOI, n, t) == null);
        n.xu = 99;
        Kiem.dung("thieu xu (99 < 100): khong gui", KenhChatLogic.kiem(KenhChatLogic.THE_GIOI, n, t) != null);
        n.admin = true;
        Kiem.dung("admin: mien cap / xu / gian cach", KenhChatLogic.kiem(KenhChatLogic.THE_GIOI, n, t) == null);
        n.admin = false;
        n.xu = 1000;
        Kiem.dung("chua vao bang: khong chat bang", KenhChatLogic.kiem(KenhChatLogic.BANG, n, t) != null);
        n.coBang = true;
        Kiem.dung("co bang: chat bang duoc", KenhChatLogic.kiem(KenhChatLogic.BANG, n, t) == null);
        Kiem.dung("chua vao doi: khong chat doi", KenhChatLogic.kiem(KenhChatLogic.DOI, n, t) != null);
        n.coDoi = true;
        Kiem.dung("co doi: chat doi duoc", KenhChatLogic.kiem(KenhChatLogic.DOI, n, t) == null);
        Kiem.dung("kenh He thong: khong gui duoc", KenhChatLogic.kiem(KenhChatLogic.HE_THONG, n, t) != null);
        Kiem.dung("kenh la (9): khong gui duoc", KenhChatLogic.kiem((byte) 9, n, t) != null);
        n.biCam = "Bạn đang bị cấm chat.";
        for (byte k = 0; k < 5; k++) {
            final KenhChatLogic.Loi l = KenhChatLogic.kiem(k, n, t);
            Kiem.dung("GM cam chat: chan kenh " + KenhChatLogic.TEN_KENH[k], l != null && l.lyDo.equals(n.biCam));
        }
        n.biCam = null;
        n.cap = 2;
        Kiem.dung("cap 2 < 3: khong nhan rieng", KenhChatLogic.kiem(KenhChatLogic.RIENG, n, t) != null);
        Kiem.dung("cap 2 < 5: khong chat ban do", KenhChatLogic.kiem(KenhChatLogic.BAN_DO, n, t) != null);
        n.cap = 5;
        Kiem.dung("cap 5: chat ban do duoc (truoc 0.9.8: 20)", KenhChatLogic.kiem(KenhChatLogic.BAN_DO, n, t) == null);

        // chu
        Kiem.bang("chuan: gop khoang trang, bo ky tu vo hinh", "xin chao ban", KenhChatLogic.chuan("  xin ​  chao\t\nban "));
        Kiem.dung("chuan: rong -> null", KenhChatLogic.chuan(" ​  ") == null);
        final StringBuilder dai = new StringBuilder();
        for (int i = 0; i < 300; i++) {
            dai.append('a');
        }
        Kiem.bang("chuan: cat 80 ky tu", Integer.valueOf(80), Integer.valueOf(KenhChatLogic.chuan(dai.toString()).length()));

        // cau hinh
        final Properties p = new Properties();
        p.setProperty("sv.chat.capTheGioi", "15");
        p.setProperty("sv.chat.xuTheGioi", "0");
        p.setProperty("sv.chat.giayTheGioi", "abc");
        p.setProperty("sv.chat.doDai", "5");
        KenhChatLogic.docCauHinh(p);
        Kiem.bang("cau hinh: cap the gioi 15", Integer.valueOf(15), Integer.valueOf(KenhChatLogic.capTheGioi));
        Kiem.bang("cau hinh: xu 0 = mien phi", Integer.valueOf(0), Integer.valueOf(KenhChatLogic.xuTheGioi));
        Kiem.bang("cau hinh hong -> mac dinh 30", Integer.valueOf(30), Integer.valueOf(KenhChatLogic.giayTheGioi));
        Kiem.bang("cau hinh kep min do dai 10", Integer.valueOf(10), Integer.valueOf(KenhChatLogic.doDai));
        KenhChatLogic.datLaiChoKiemThu();
        Kiem.bang("dat lai: cap the gioi 10", Integer.valueOf(10), Integer.valueOf(KenhChatLogic.capTheGioi));

        // lich su gioi han
        for (int i = 0; i < 70; i++) {
            KenhChatLogic.ghiTheGioi(new KenhChatLogic.Tin(KenhChatLogic.THE_GIOI, i, (short) 1, "a", "", "tin " + i, "", 0));
        }
        final List<KenhChatLogic.Tin> ls = KenhChatLogic.lichSuTheGioi();
        Kiem.bang("lich su the gioi giu 50 dong", Integer.valueOf(50), Integer.valueOf(ls.size()));
        Kiem.bang("lich su: dong cu nhat con lai la tin 20", "tin 20", ls.get(0).noiDung);
        KenhChatLogic.datLaiChoKiemThu();
    }

    static void game() throws Exception {
        final KhoGia kho = new KhoGia();
        final TimGia tim = new TimGia();
        KenhChatGame.kho = kho;
        KenhChatGame.tim = tim;
        KenhChatLogic.xuTheGioi = 0; // tru xu ghi log CSDL - phan tru xu kiem rieng ben duoi neu CSDL co

        final Char an = nv("kcan", 908, 30);
        final Char binh = nv("kcbinh", 908, 30);
        final Char cu = nv("kccu", 907, 30);       // client cu
        final Char moi = nv("kcmoi", 908, 1);      // cap 1
        final Char ngoai = nv("kcngoai", 908, 30); // khong bang / doi
        tim.online.add(an);
        tim.online.add(binh);
        tim.online.add(cu);
        tim.online.add(moi);
        tim.online.add(ngoai);

        // ---------------- the gioi
        xoa();
        Kiem.dung("the gioi: an gui duoc", KenhChatGame.gui(an, KenhChatLogic.THE_GIOI, "", "chao   moi nguoi") == null);
        final List<TinDoc> tb = tin(binh, KenhChatLogic.THE_GIOI);
        Kiem.bang("the gioi: binh (908) nhan 1 tin -124", Integer.valueOf(1), Integer.valueOf(tb.size()));
        Kiem.dung("the gioi: noi dung da chuan, ten nguoi gui", tb.size() == 1 && tb.get(0).noiDung.equals("chao moi nguoi")
                && tb.get(0).ten.equals("kcan") && tb.get(0).id == an.id);
        Kiem.dung("the gioi: tin co thoi gian", tb.size() == 1 && Math.abs(tb.get(0).luc - System.currentTimeMillis()) < 60000L);
        final List<TinDoc> ta = tin(an, KenhChatLogic.THE_GIOI);
        Kiem.dung("the gioi: nguoi gui nhan lai tin cua minh (co CUA_MINH)", ta.size() == 1 && (ta.get(0).co & KenhChatLogic.CO_CUA_MINH) != 0);
        Kiem.bang("the gioi: client cu nhan -60 'ten: noi dung'", "kcan: chao moi nguoi", chuoiCu(cu, -60));
        Kiem.bang("the gioi: client cu KHONG nhan -124", Integer.valueOf(0), Integer.valueOf(dem(cu, KenhChatLogic.LENH)));
        Kiem.bang("the gioi: vao lich su", Integer.valueOf(1), Integer.valueOf(KenhChatLogic.lichSuTheGioi().size()));
        // gian cach
        xoa();
        Kiem.dung("the gioi: gui lai ngay bi chan", KenhChatGame.gui(an, KenhChatLogic.THE_GIOI, "", "tin hai") != null);
        final Object[] kq = ketQua(an);
        Kiem.dung("the gioi: bao cho + so giay (29-30)", kq != null && ((Integer) kq[0]).intValue() == KenhChatLogic.KQ_CHO
                && ((Integer) kq[1]).intValue() >= 29 && ((Integer) kq[1]).intValue() <= 30);
        Kiem.bang("the gioi: binh khong nhan tin bi chan", Integer.valueOf(0), Integer.valueOf(tin(binh, KenhChatLogic.THE_GIOI).size()));
        // cap thap
        xoa();
        Kiem.dung("the gioi: cap 1 bi chan", KenhChatGame.gui(moi, KenhChatLogic.THE_GIOI, "", "xin chao") != null);
        Kiem.dung("the gioi: cap 1 duoc bao ly do cap", ketQua(moi) != null && ((String) ketQua(moi)[2]).contains("cấp 10"));
        // link / quang cao
        xoa();
        Kiem.dung("the gioi: link bi chan", KenhChatGame.gui(binh, KenhChatLogic.THE_GIOI, "", "vao web abc.com nhe") != null);
        Kiem.dung("the gioi: bao khong gui link", ketQua(binh) != null && ((String) ketQua(binh)[2]).contains("liên kết"));
        Kiem.bang("the gioi: link khong toi ai", Integer.valueOf(0), Integer.valueOf(tin(an, KenhChatLogic.THE_GIOI).size()));
        // cam chat GM
        xoa();
        ngoai.forbitChat = 5;
        Kiem.dung("GM cam chat: khong gui the gioi", KenhChatGame.gui(ngoai, KenhChatLogic.THE_GIOI, "", "alo") != null);
        Kiem.dung("GM cam chat: bao con phut", ketQua(ngoai) != null && ((String) ketQua(ngoai)[2]).contains("cấm chat"));
        ngoai.forbitChat = 0;
        // phi xu (neu tru duoc khong can CSDL)
        KenhChatLogic.xuTheGioi = 100;
        com.htbb.chongspam.ChongSpam.datLaiChoKiemThu();
        final Char giau = nv("kcgiau", 908, 30);
        tim.online.add(giau);
        giau.setxu(50);
        xoa();
        Kiem.dung("phi xu: 50 < 100 xu bi chan", KenhChatGame.gui(giau, KenhChatLogic.THE_GIOI, "", "tin co phi") != null);
        Kiem.bang("phi xu: khong tru khi bi chan", Long.valueOf(50), Long.valueOf(giau.getxu()));
        if (data.Database.instance != null) {
            giau.setxu(1000);
            KenhChatGame.gui(giau, KenhChatLogic.THE_GIOI, "", "tin co phi");
            Kiem.bang("phi xu: tru 100 xu", Long.valueOf(900), Long.valueOf(giau.getxu()));
        }
        KenhChatLogic.xuTheGioi = 0;

        // ---------------- bang hoi
        xoa();
        com.htbb.chongspam.ChongSpam.datLaiChoKiemThu();
        an.idClan = 7;
        binh.idClan = 7;
        cu.idClan = 7;
        an.rankClan = 0;
        final List<Char> thanhVien = new ArrayList<Char>();
        thanhVien.add(an);
        thanhVien.add(binh);
        thanhVien.add(cu);
        tim.bang.put(Integer.valueOf(7), thanhVien);
        Kiem.dung("bang: an gui duoc", KenhChatGame.gui(an, KenhChatLogic.BANG, "", "hop bang toi nay") == null);
        final List<TinDoc> bb = tin(binh, KenhChatLogic.BANG);
        Kiem.dung("bang: binh nhan, co nhan chuc vu (BC)", bb.size() == 1 && bb.get(0).nhan.equals("(BC)"));
        Kiem.bang("bang: client cu nhan -18 nhu truoc", "(BC)kcan: hop bang toi nay", chuoiCu(cu, -18));
        Kiem.bang("bang: nguoi ngoai bang khong nhan", Integer.valueOf(0), Integer.valueOf(tin(ngoai, -1).size() + dem(ngoai, -18)));
        Kiem.bang("bang: lich su bang 7", Integer.valueOf(1), Integer.valueOf(KenhChatLogic.lichSuBang(7).size()));
        Kiem.bang("bang: lich su bang khac rong", Integer.valueOf(0), Integer.valueOf(KenhChatLogic.lichSuBang(8).size()));
        xoa();
        Kiem.dung("bang: nguoi ngoai gui bi chan", KenhChatGame.gui(ngoai, KenhChatLogic.BANG, "", "cho vao bang") != null);
        Kiem.bang("bang: tin nguoi ngoai khong toi thanh vien", Integer.valueOf(0), Integer.valueOf(tin(an, KenhChatLogic.BANG).size()));

        // ---------------- doi
        xoa();
        final real.Party doi = new real.Party((short) 3);
        doi.addUser(an);
        doi.addUser(binh);
        doi.addUser(cu);
        an.party = doi;
        binh.party = doi;
        cu.party = doi;
        an.partyID = 3;
        Kiem.dung("doi: an gui duoc", KenhChatGame.gui(an, KenhChatLogic.DOI, "", "di danh boss") == null);
        Kiem.bang("doi: binh nhan 1 tin doi", Integer.valueOf(1), Integer.valueOf(tin(binh, KenhChatLogic.DOI).size()));
        Kiem.bang("doi: client cu nhan -5 tu the 'Nhóm'", "Nhóm|kcan: di danh boss", chuoiCu(cu, -5));
        Kiem.bang("doi: nguoi ngoai doi khong nhan", Integer.valueOf(0), Integer.valueOf(tin(ngoai, -1).size() + dem(ngoai, -5)));
        xoa();
        Kiem.dung("doi: nguoi ngoai gui bi chan", KenhChatGame.gui(ngoai, KenhChatLogic.DOI, "", "cho vao doi") != null);
        Kiem.bang("doi: an khong nhan tin nguoi ngoai", Integer.valueOf(0), Integer.valueOf(tin(an, KenhChatLogic.DOI).size()));

        // ---------------- rieng
        xoa();
        Kiem.dung("rieng: an -> binh", KenhChatGame.gui(an, KenhChatLogic.RIENG, "KCBinh", "chao binh") == null);
        final List<TinDoc> rb = tin(binh, KenhChatLogic.RIENG);
        Kiem.dung("rieng: binh nhan, doi phuong = an", rb.size() == 1 && rb.get(0).doiPhuong.equals("kcan") && rb.get(0).noiDung.equals("chao binh"));
        final List<TinDoc> ra = tin(an, KenhChatLogic.RIENG);
        Kiem.dung("rieng: an thay tin minh, doi phuong = binh", ra.size() == 1 && ra.get(0).doiPhuong.equals("kcbinh")
                && (ra.get(0).co & KenhChatLogic.CO_CUA_MINH) != 0);
        Kiem.bang("rieng: nguoi thu ba khong nhan", Integer.valueOf(0), Integer.valueOf(tin(ngoai, -1).size()));
        xoa();
        KenhChatGame.gui(an, KenhChatLogic.RIENG, "kccu", "chao ban cu");
        Kiem.bang("rieng: client cu nhan -5 (ten, noi dung) ngay", "kcan|chao ban cu", chuoiCu(cu, -5));
        xoa();
        Kiem.dung("rieng: khong nhan cho chinh minh", KenhChatGame.gui(an, KenhChatLogic.RIENG, "kcan", "tu nhan") != null);
        Kiem.dung("rieng: nguoi khong ton tai / khong online -> bao khong gui", KenhChatGame.gui(an, KenhChatLogic.RIENG, "khongco", "alo") != null
                && ((String) ketQua(an)[2]).contains("không trực tuyến, tin không được gửi"));
        Kiem.dung("rieng: cap 1 khong nhan duoc", KenhChatGame.gui(moi, KenhChatLogic.RIENG, "kcan", "alo") != null);

        // ---------------- nguoi nhan offline: KHONG gui, KHONG luu (chat chi trong phien - chu game 22/10/2026, kieu Lang La)
        xoa();
        Kiem.dung("offline: an -> kcoff khong gui (bao loi)", KenhChatGame.gui(an, KenhChatLogic.RIENG, "kcoff", "khi nao ve") != null);
        final Object[] kqo = ketQua(an);
        Kiem.dung("offline: bao nguoi gui '<ten> khong truc tuyen, tin khong duoc gui'", kqo != null
                && ((Integer) kqo[0]).intValue() == KenhChatLogic.KQ_LOI && ((String) kqo[2]).contains("không trực tuyến, tin không được gửi"));
        Kiem.bang("offline: nguoi gui khong nhan tin cua minh (khong gui)", Integer.valueOf(0), Integer.valueOf(tin(an, KenhChatLogic.RIENG).size()));
        final Char off = nv("kcoff", 908, 20);
        off.charDBID = 99901;
        tim.online.add(off);
        xoa();
        KenhChatGame.khiVaoBanDo(off);
        Kiem.bang("offline: vao game sau do khong nhan tin rieng nao", Integer.valueOf(0), Integer.valueOf(tin(off, KenhChatLogic.RIENG).size()));
        tim.online.remove(off);

        // ---------------- chan
        xoa();
        com.htbb.chongspam.ChongSpam.datLaiChoKiemThu();
        KenhChatGame.datChan(binh, "KCAN", true);
        Kiem.dung("chan: ghi CSDL", kho.docChan(binh.charDBID).contains("kcan"));
        Kiem.dung("chan: gui danh sach chan cho binh", dem(binh, KenhChatLogic.LENH) >= 1);
        xoa();
        KenhChatGame.gui(an, KenhChatLogic.RIENG, "kcbinh", "binh oi");
        Kiem.bang("chan: tin rieng cua nguoi bi chan khong toi", Integer.valueOf(0), Integer.valueOf(tin(binh, KenhChatLogic.RIENG).size()));
        Kiem.bang("chan: nguoi gui van thay tin minh", Integer.valueOf(1), Integer.valueOf(tin(an, KenhChatLogic.RIENG).size()));
        xoa();
        KenhChatGame.gui(an, KenhChatLogic.BANG, "", "tin bang moi");
        Kiem.bang("chan: tin bang cua nguoi bi chan khong toi", Integer.valueOf(0), Integer.valueOf(tin(binh, KenhChatLogic.BANG).size()));
        Kiem.bang("chan: thanh vien khac (client cu) van nhan", "(BC)kcan: tin bang moi", chuoiCu(cu, -18));
        KenhChatGame.datChan(binh, "kcan", false);
        xoa();
        KenhChatGame.gui(an, KenhChatLogic.RIENG, "kcbinh", "binh oi lan hai");
        Kiem.bang("bo chan: tin toi lai", Integer.valueOf(1), Integer.valueOf(tin(binh, KenhChatLogic.RIENG).size()));
        final int ghiTruoc = kho.ghiChan;
        KenhChatGame.datChan(binh, "kcbinh", true);
        Kiem.bang("chan chinh minh: bo qua", Integer.valueOf(ghiTruoc), Integer.valueOf(kho.ghiChan));
        // danh sach chan nap tu CSDL o phien moi
        kho.ghiChan(77777, "kcan", true);
        final Char moiPhien = nv("kcphien", 908, 30);
        moiPhien.charDBID = 77777;
        Kiem.dung("chan: nap tu CSDL o phien moi", KenhChatGame.daChan(moiPhien, "KCAn"));

        // ---------------- vao game: cau hinh, ban be, lich su
        xoa();
        KenhChatLogic.ghiTheGioi(new KenhChatLogic.Tin(KenhChatLogic.THE_GIOI, System.currentTimeMillis(), (short) 9, "kcxa", "", "tin cu", "", 0));
        final Char vao = nv("kcvao", 908, 20);
        vao.friendListName.add("kcan");
        vao.friendListName.add("kcvang");
        an.friendListName.add("kcvao");
        tim.online.add(vao);
        xoa();
        KenhChatGame.khiVaoBanDo(vao);
        final List<Goi> gv = goi(vao);
        Kiem.dung("vao game: goi dau la cau hinh", !gv.isEmpty() && gv.get(0).lenh == KenhChatLogic.LENH && gv.get(0).du[0] == KenhChatLogic.G_CAU_HINH);
        int soBan = -1;
        int banOnline = -1;
        for (final Goi g : gv) {
            if (g.lenh == KenhChatLogic.LENH && g.du[0] == KenhChatLogic.G_BAN) {
                final DataInputStream d = g.doc();
                d.readByte();
                soBan = d.readShort();
                banOnline = 0;
                for (int i = 0; i < soBan; i++) {
                    d.readUTF();
                    banOnline += d.readByte();
                    d.readShort();
                }
            }
        }
        Kiem.bang("vao game: danh sach 2 ban", Integer.valueOf(2), Integer.valueOf(soBan));
        Kiem.bang("vao game: 1 ban online", Integer.valueOf(1), Integer.valueOf(banOnline));
        final List<TinDoc> ls = tin(vao, KenhChatLogic.THE_GIOI);
        Kiem.bang("vao game: KHONG gui lai lich su the gioi (chat chi trong phien)", Integer.valueOf(0), Integer.valueOf(ls.size()));
        boolean coDoi = false;
        for (final Goi g : gv) {
            coDoi |= g.lenh == KenhChatLogic.LENH && g.du[0] == KenhChatLogic.G_DOI;
        }
        Kiem.dung("vao game: goi doi (loai 7: ten doi + so online)", coDoi);
        boolean baoAn = false;
        for (final Goi g : goi(an)) {
            if (g.lenh == KenhChatLogic.LENH && g.du[0] == KenhChatLogic.G_TRANG_THAI) {
                final DataInputStream d = g.doc();
                d.readByte();
                baoAn = d.readUTF().equals("kcvao") && d.readByte() == 1;
            }
        }
        Kiem.dung("vao game: ban online (an) duoc bao kcvao online", baoAn);
        xoa();
        KenhChatGame.khiThoat(vao);
        boolean baoOff = false;
        for (final Goi g : goi(an)) {
            if (g.lenh == KenhChatLogic.LENH && g.du[0] == KenhChatLogic.G_TRANG_THAI) {
                final DataInputStream d = g.doc();
                d.readByte();
                baoOff = d.readUTF().equals("kcvao") && d.readByte() == 0;
            }
        }
        Kiem.dung("thoat: ban duoc bao offline", baoOff);
        // client cu vao game: khong goi -124
        final Char cu3 = nv("kccu3", 907, 20);
        xoa();
        KenhChatGame.khiVaoBanDo(cu3);
        Kiem.bang("client cu vao game: khong goi -124 nao", Integer.valueOf(0), Integer.valueOf(dem(cu3, KenhChatLogic.LENH)));

        // ---------------- lenh -124 tu client (giai goi)
        xoa();
        com.htbb.chongspam.ChongSpam.datLaiChoKiemThu();
        final java.io.ByteArrayOutputStream b = new java.io.ByteArrayOutputStream();
        final java.io.DataOutputStream o = new java.io.DataOutputStream(b);
        o.writeByte(KenhChatLogic.K_GUI);
        o.writeByte(KenhChatLogic.RIENG);
        o.writeUTF("kcbinh");
        o.writeUTF("qua goi -124");
        KenhChatGame.xuLy(an, new Message(KenhChatLogic.LENH, b.toByteArray()));
        Kiem.bang("lenh -124 gui rieng: binh nhan", Integer.valueOf(1), Integer.valueOf(tin(binh, KenhChatLogic.RIENG).size()));
        final java.io.ByteArrayOutputStream b2 = new java.io.ByteArrayOutputStream();
        final java.io.DataOutputStream o2 = new java.io.DataOutputStream(b2);
        o2.writeByte(KenhChatLogic.K_TRA_ID);
        o2.writeByte(KenhChatLogic.VIEC_MOI_DOI);
        o2.writeUTF("kcbinh");
        xoa();
        KenhChatGame.xuLy(an, new Message(KenhChatLogic.LENH, b2.toByteArray()));
        short idTra = -2;
        for (final Goi g : goi(an)) {
            if (g.lenh == KenhChatLogic.LENH && g.du[0] == KenhChatLogic.G_ID) {
                final DataInputStream d = g.doc();
                d.readByte();
                d.readByte();
                d.readUTF();
                idTra = d.readShort();
            }
        }
        Kiem.bang("lenh -124 tra id theo ten: id cua binh", Short.valueOf(binh.id), Short.valueOf(idTra));
        KenhChatGame.xuLy(an, new Message(KenhChatLogic.LENH, new byte[]{1, 1}));
        Kiem.dung("lenh -124 goi cut: khong nem loi", true);
        // trung lap (chong spam)
        xoa();
        com.htbb.chongspam.ChongSpam.datLaiChoKiemThu();
        KenhChatGame.gui(an, KenhChatLogic.DOI, "", "lap lai");
        Kiem.dung("trung lap: lan hai bi chan", KenhChatGame.gui(an, KenhChatLogic.DOI, "", "lap  lai") != null);
        Kiem.bang("trung lap: binh chi nhan mot", Integer.valueOf(1), Integer.valueOf(tin(binh, KenhChatLogic.DOI).size()));
    }

    static String doc(final String tep) {
        try {
            return new String(Files.readAllBytes(Paths.get(tep)), StandardCharsets.UTF_8);
        } catch (final Exception e) {
            return "";
        }
    }

    static void noiMa() {
        final String xl = doc("src/real/MapXuLyLenh.java");
        Kiem.dung("MapXuLyLenh: case -124 -> KenhChatGame.xuLy", xl.contains("case -124:") && xl.contains("KenhChatGame.xuLy(player, message)"));
        Kiem.dung("MapXuLyLenh: vao ban do -> KenhChatGame.khiVaoBanDo", xl.contains("KenhChatGame.khiVaoBanDo(player)"));
        final String xh = doc("src/real/MapXaHoi.java");
        Kiem.dung("MapXaHoi: tin rieng -5 cu di qua giaoRieng (offline: bao khong gui)", xh.contains("KenhChatGame.giaoRieng(player, receiver, info)"));
        Kiem.dung("MapXaHoi: the 'Nhóm' cua client cu = chat doi", xh.contains("TEN_NHOM_CU.equals(receiver)"));
        Kiem.sai("MapXaHoi: het cap cung 20 bo im lang", xh.contains("player.lvDetail.lv >= 20 && msgChat.length() >= 2"));
        Kiem.dung("MapDoi: chat bang -18 di qua phatBang", doc("src/real/MapDoi.java").contains("KenhChatGame.phatBang(player, ms)"));
        Kiem.dung("MapNhapText: loa vao the The gioi", doc("src/real/MapNhapText.java").contains("KenhChatGame.phatTheGioi(player, text, true)"));
        Kiem.dung("CharManager: thoat -> khiThoat", doc("src/real/CharManager.java").contains("KenhChatGame.khiThoat(p)"));
        Kiem.dung("TeamServer: tao bang + doc cau hinh", doc("src/server/TeamServer.java").contains("KenhChatGame.taoBang()")
                && doc("src/server/TeamServer.java").contains("KenhChatLogic.docCauHinh(p)"));
        final String g = doc("src/real/KenhChatGame.java");
        Kiem.dung("KenhChatGame: cong thanh vien chat the gioi / gui thu", g.contains("chanVaBao(p, \"chat_the_gioi\")") && g.contains("chanVaBao(p, \"gui_thu\")"));
        Kiem.dung("KenhChatGame: tao tob_chat_chan, KHONG con hop thu tob_chat_cho", !g.contains("CREATE TABLE IF NOT EXISTS tob_chat_cho")
                && !g.contains("INSERT INTO tob_chat_cho") && g.contains("CREATE TABLE IF NOT EXISTS tob_chat_chan"));
        final String sql = DayDu.phanNangCap("them_kenh_chat.sql"); // tep cu gop vao sql/nang_cap.sql phan 11
        Kiem.dung("nang_cap.sql them_kenh_chat: tob_chat_chan + bo tob_chat_cho (DROP IF EXISTS, chay lai duoc)",
                sql.contains("DROP TABLE IF EXISTS `tob_chat_cho`;") && !sql.contains("CREATE TABLE IF NOT EXISTS `tob_chat_cho`")
                && sql.contains("CREATE TABLE IF NOT EXISTS `tob_chat_chan`"));
        final String dm = DayDu.danhMucJson(); // danh muc trong CSDL (thay DanhMucSinh.java da xoa)
        final String[] khoa = {"sv.chat.capGan", "sv.chat.capTheGioi", "sv.chat.xuTheGioi", "sv.chat.giayTheGioi", "sv.chat.capRieng",
            "sv.chat.doDai", "sv.chat.lichSu", "sv.chat.chanToiDa"};
        for (final String k : khoa) {
            Kiem.dung("danh muc: " + k + " lop co_dinh (khong tang khoa web)", dm.contains("\"khoa\": \"" + k + "\", \"nhom\": \"")
                    && dm.matches("(?s).*\"khoa\": \"" + k.replace(".", "\\.") + "\", \"nhom\": \"\\w+\", \"lop\": \"co_dinh\".*"));
        }
        Kiem.dung("ApDungMacDinh: nghe sv.chat.", doc("src/com/htbb/cauhinh/ApDungMacDinh.java").contains("\"sv.chat.\""));
    }
}
