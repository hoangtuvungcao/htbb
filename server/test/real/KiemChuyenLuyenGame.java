package real;

import com.htbb.pokemon.ChuyenLuyen;
import com.htbb.pokemon.HoSoLauDai;
import com.htbb.pokemon.LauDai;
import com.htbb.pokemon.NangCapLauDai;
import com.htbb.pokemon.ThongSoLauDai;
import io.Message;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

/**
 * CHUYEN LUYEN - luong game real.ChuyenLuyenGame.chuyen (chu game 05/11/2026): ket qua cap co / khong bua, tru dung xu /
 * da / bua, chi so tinh lai bang NangCapLauDaiGame.doiChiSo, khoa lan sang mon nhan, pity ve 0, tat ca hoac khong (thieu
 * xu / gia doi / dang giao dich / dang rao ban / khac o: khong doi gi), menu xac nhan. Goi tu Kiem.java qua {@link #BAO}.
 */
public final class KiemChuyenLuyenGame {

    private KiemChuyenLuyenGame() {
    }

    public interface Bao {
        void bang(String moTa, Object mongDoi, Object thucTe);
    }

    public static Bao BAO;

    static void bang(final String m, final Object a, final Object b) {
        BAO.bang(m, a, b);
    }

    static final int ID_VK30 = 9_911;  // vu khi cap 30 (type 3)
    static final int ID_VK40 = 9_912;  // vu khi lop khac cap 40 (type 5)
    static final int ID_KHAN = 9_913;  // khan cap 40 (type 0)
    static final int[] IDS = {ID_VK30, ID_VK40, ID_KHAN, ChuyenLuyen.ID_BUA};
    static int idKe = 881_100;

    static final class NV extends Char {
        final List<Message> goi = new ArrayList<Message>();

        NV(final long xu) {
            super(null);
            charDBID = idKe;
            id = (short) (idKe % 30000);
            idKe++;
            charname = "kiemchuyenluyen" + charDBID;
            lvDetail = new LevelDetail();
            lvDetail.lv = 50;
            MAX_INVENTORY = 42;
            maxBag = 1;
            listGemitem = new int[400];
            listGemitemLock = new int[400];
            listGemitemSell = new int[400];
            if (ALL_ID_USE == null) {
                ALL_ID_USE = new Hashtable<Short, Short>();
            }
            this.xu = xu;
        }

        @Override
        public void sendMessage(final Message m) {
            goi.add(m);
        }
    }

    static void mau(final int id, final int type, final int level) {
        final ItemTemplates t = new ItemTemplates();
        t.id = id;
        t.type = (byte) type;
        t.name = "Mẫu " + id;
        t.level = (byte) level;
        t.clazz = -1;
        t.atb = new short[12];
        for (int i = 0; i < 6; i++) {
            Map.itemTemplates.get(i).put(Integer.valueOf(id), t);
        }
    }

    static short idMon = 100;

    static Item mon(final Char p, final int tpl, final int plus, final int goc) {
        final Item it = new Item();
        it.tempateID = tpl;
        it.classTemplate = -1;
        it.id = idMon++;
        it.atb = new short[12];
        it.atb[0] = (short) goc;
        it.atb[1] = (short) (goc / 2);
        if (plus > 0) {
            NangCapLauDaiGame.doiChiSo(it, 0, plus);
        }
        it.plus = (byte) plus;
        p.iItems.add(it);
        return it;
    }

    static Item bua(final Char p, final int so, final boolean khoa) {
        final Item it = new Item();
        it.tempateID = ChuyenLuyen.ID_BUA;
        it.classTemplate = -1;
        it.id = idMon++;
        it.quanlity = so;
        it.lock = (byte) (khoa ? 1 : 0);
        p.iItems.add(it);
        return it;
    }

    static int demBua(final Char p) {
        final int[] b = ChuyenLuyenGame.demBua(p);
        return b[0] + b[1];
    }

    /** Chuyen voi dung gia hien thi (nhu bam xac nhan ngay sau menu). */
    static String chuyen(final Char p, final Item a, final Item b, final boolean coBua) {
        final int r = ChuyenLuyen.capKetQua(a.plus, coBua);
        return ChuyenLuyenGame.chuyen(p, a.id, b.id, coBua, ChuyenLuyenGame.giaXu(b, r), ChuyenLuyenGame.giaDa(r));
    }

    static long xuMong(final int capDo, final int r) {
        long t = 0L;
        for (int k = 1; k <= r; k++) {
            t += NangCapLauDai.chiPhi(capDo, k, BoNgocGame.heSoXuBacCao());
        }
        return (t * ChuyenLuyen.XU_PHAN_TRAM_MAC_DINH + 99L) / 100L;
    }

    public static void chay() {
        final boolean lauDaiCu = LauDai.bat();
        final ItemTemplates[][] cu = new ItemTemplates[IDS.length][6];
        final long[][] chiPhiCu = ThongSoLauDai.CHI_PHI_NC;
        final int[] daCu = ThongSoLauDai.DA_NC;
        final int[] bacCu = ThongSoLauDai.BAC_TB_CAP;
        final int[] chiSoCu = ThongSoLauDai.CHI_SO_NC_PHAN_TRAM;
        try {
            LauDai.dat(true);
            while (Map.itemTemplates.size() < 6) {
                Map.itemTemplates.add(new Hashtable<Integer, ItemTemplates>());
            }
            for (int j = 0; j < IDS.length; j++) {
                for (int i = 0; i < 6; i++) {
                    cu[j][i] = Map.itemTemplates.get(i).get(Integer.valueOf(IDS[j]));
                }
            }
            if (ThongSoLauDai.CHI_PHI_NC.length == 0 || ThongSoLauDai.DA_NC.length < 15) {
                // chua nap SQL (chay le): bang nho cung hinh dang data_nang_cap_lau_dai
                ThongSoLauDai.BAC_TB_CAP = new int[]{1, 10, 20, 30, 40};
                final long[][] c = new long[5][16];
                for (int b = 0; b < 5; b++) {
                    for (int k = 1; k <= 15; k++) {
                        c[b][k] = 100L * (b + 1) * k;
                    }
                }
                ThongSoLauDai.CHI_PHI_NC = c;
                ThongSoLauDai.DA_NC = new int[]{1, 1, 1, 1, 1, 2, 2, 2, 2, 2, 4, 4, 4, 4, 4};
                ThongSoLauDai.CHI_SO_NC_PHAN_TRAM = new int[]{3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 5, 5, 5, 5, 5};
            }
            mau(ID_VK30, 3, 30);
            mau(ID_VK40, 5, 40);
            mau(ID_KHAN, 0, 40);
            mau(ChuyenLuyen.ID_BUA, 28, 1);

            // 1) khong bua: +10 -> nhan +9, nguon +0, tru dung xu, chi so tinh lai, pity ve 0
            final NV p = new NV(10_000_000L);
            final HoSoLauDai h = HoSoLauDai.giaiMa("");
            LauDaiNhanVat.datKiem(p.charDBID, h);
            final Item a = mon(p, ID_VK30, 10, 100);
            final Item b = mon(p, ID_VK40, 0, 200);
            h.datTruot(a.id, 4);
            h.datTruot(b.id, 2);
            final long xu9 = xuMong(40, 9);
            String kq = chuyen(p, a, b, false);
            bang("khong bua: thanh cong", Boolean.TRUE, Boolean.valueOf(kq.startsWith("Chuyển luyện thành công")));
            bang("khong bua: nhan +9 (mat 1 cap)", Integer.valueOf(9), Integer.valueOf(b.plus));
            bang("khong bua: nguon ve +0, van trong tui", "0/true", a.plus + "/" + p.iItems.contains(a));
            bang("khong bua: tru " + xu9 + " xu (35% tong 1 lan thu +1..+9 cap 40)", Long.valueOf(10_000_000L - xu9), Long.valueOf(p.xu));
            bang("khong bua: chi so nhan = goc x (100 + tong% +9) (doiChiSo)", Integer.valueOf((int) Math.round(200 * (100 + NangCapLauDai.tongChiSoPhanTram(9)) / 100.0)),
                    Integer.valueOf(b.atb[0]));
            bang("khong bua: chi so nguon ve goc", Integer.valueOf(100), Integer.valueOf(a.atb[0]));
            bang("khong bua: dong 2 nguon ve goc", Integer.valueOf(50), Integer.valueOf(a.atb[1]));
            bang("pity hai mon ve 0", "0/0", h.truot(a.id) + "/" + h.truot(b.id));
            bang("mon mo -> nhan van mo", Integer.valueOf(0), Integer.valueOf(b.lock));
            bang("nguon +0 khong chuyen tiep duoc", Boolean.FALSE, Boolean.valueOf(chuyen(p, a, b, false).startsWith("Chuyển luyện thành công")));

            // 2) co bua: +12 giu +12, tru 2 bua + 5 da (50% tong da +10..+12), nhan khong khoa (bua / da mo)
            final NV q = new NV(10_000_000L);
            final Item a2 = mon(q, ID_VK30, 12, 100);
            final Item b2 = mon(q, ID_VK40, 3, 200);
            final Item bu = bua(q, 3, false);
            q.listGemitem[8] = 7;
            final long xu12 = xuMong(40, 12);
            kq = chuyen(q, a2, b2, true);
            bang("co bua: thanh cong", Boolean.TRUE, Boolean.valueOf(kq.startsWith("Chuyển luyện thành công")));
            bang("co bua: nhan +12 (khong mat cap)", Integer.valueOf(12), Integer.valueOf(b2.plus));
            bang("co bua: tru 2 bua (+10..+12)", Integer.valueOf(1), Integer.valueOf(demBua(q)));
            bang("co bua: chong bua con 1", Integer.valueOf(1), Integer.valueOf(bu.quanlity));
            bang("co bua: tru 5 da luyen kim", Integer.valueOf(2), Integer.valueOf(q.listGemitem[8]));
            bang("co bua: tru " + xu12 + " xu", Long.valueOf(10_000_000L - xu12), Long.valueOf(q.xu));
            bang("co bua: chi so nhan tinh lai tu +3 len +12", Integer.valueOf((int) Math.round(200 * (100 + NangCapLauDai.tongChiSoPhanTram(12)) / 100.0)),
                    Integer.valueOf(b2.atb[0]));
            bang("co bua: nhan mo (da / bua mo)", Integer.valueOf(0), Integer.valueOf(b2.lock));

            // 3) thieu xu: khong doi gi
            final NV r = new NV(10L);
            final Item a3 = mon(r, ID_VK30, 8, 100);
            final Item b3 = mon(r, ID_VK40, 0, 200);
            final short goc = b3.atb[0];
            kq = chuyen(r, a3, b3, false);
            bang("thieu xu: bao can", Boolean.TRUE, Boolean.valueOf(kq.startsWith("Cần ")));
            bang("thieu xu: hai mon giu nguyen", "8/0/" + goc, a3.plus + "/" + b3.plus + "/" + b3.atb[0]);
            bang("thieu xu: xu giu nguyen", Long.valueOf(10L), Long.valueOf(r.xu));

            // 4) thieu bua: khong doi gi (ke ca xu, da)
            final NV s = new NV(10_000_000L);
            final Item a4 = mon(s, ID_VK30, 13, 100);
            final Item b4 = mon(s, ID_VK40, 0, 200);
            bua(s, 2, false);
            s.listGemitem[8] = 50;
            kq = chuyen(s, a4, b4, true);
            bang("thieu bua (+13 can 3, co 2): tu choi", Boolean.TRUE, Boolean.valueOf(kq.startsWith("Cần ")));
            bang("thieu bua: khong tru gi", "13/0/2/50/10000000", a4.plus + "/" + b4.plus + "/" + demBua(s) + "/" + s.listGemitem[8] + "/" + s.xu);
            // thieu da
            s.listGemitem[8] = 1;
            bua(s, 1, false);
            kq = chuyen(s, a4, b4, true);
            bang("thieu da (+13 can 7): tu choi, khong tru bua", "13/3", a4.plus + "/" + demBua(s));

            // 5) gia doi so voi luc hoi -> tu choi
            final NV t = new NV(10_000_000L);
            final Item a5 = mon(t, ID_VK30, 6, 100);
            final Item b5 = mon(t, ID_VK40, 0, 200);
            kq = ChuyenLuyenGame.chuyen(t, a5.id, b5.id, false, 1L, 0);
            bang("gia doi: tu choi", Boolean.TRUE, Boolean.valueOf(kq.contains("đã thay đổi")));
            bang("gia doi: khong doi", "6/0/10000000", a5.plus + "/" + b5.plus + "/" + t.xu);

            // 6) khoa: nguon khoa vinh vien -> nhan khoa vinh vien; nguon khoa do mac -> nhan khoa do mac
            final NV u = new NV(10_000_000L);
            final Item a6 = mon(u, ID_VK30, 7, 100);
            a6.khoaVinhVien();
            final Item b6 = mon(u, ID_VK40, 0, 200);
            chuyen(u, a6, b6, false);
            bang("nguon khoa vinh vien -> nhan khoa vinh vien", "6/1/0", b6.plus + "/" + b6.lock + "/" + b6.daMac);
            final Item a7 = mon(u, ID_VK30, 7, 100);
            a7.khoaKhiMac();
            final Item b7 = mon(u, ID_VK40, 0, 200);
            chuyen(u, a7, b7, false);
            bang("nguon khoa do mac -> nhan khoa do mac", "6/1/1", b7.plus + "/" + b7.lock + "/" + b7.daMac);
            // chi co da khoa -> nhan khoa vinh vien
            final Item a8 = mon(u, ID_VK30, 11, 100);
            final Item b8 = mon(u, ID_VK40, 0, 200);
            u.listGemitemLock[9] = 10;
            chuyen(u, a8, b8, false);
            bang("dung da khoa -> nhan khoa vinh vien", "10/1/0/9", b8.plus + "/" + b8.lock + "/" + b8.daMac + "/" + u.listGemitemLock[9]);
            // co ca da mo + khoa, nhan mo: tieu da mo truoc (khong khoa oan)
            final Item a9 = mon(u, ID_VK30, 11, 100);
            final Item b9 = mon(u, ID_VK40, 0, 200);
            u.listGemitem[8] = 5;
            chuyen(u, a9, b9, false);
            bang("da mo truoc: nhan mo, da khoa giu", "10/0/4/9", b9.plus + "/" + b9.lock + "/" + u.listGemitem[8] + "/" + u.listGemitemLock[9]);
            // bua khoa -> nhan khoa
            final Item a10 = mon(u, ID_VK30, 5, 100);
            final Item b10 = mon(u, ID_VK40, 0, 200);
            bua(u, 1, true);
            chuyen(u, a10, b10, true);
            bang("dung bua khoa -> nhan +5 khoa vinh vien, bua het", "5/1/0/0", b10.plus + "/" + b10.lock + "/" + b10.daMac + "/" + demBua(u));
            bang("nguon giu khoa cua no", Integer.valueOf(1), Integer.valueOf(a6.lock));

            // 7) dieu kien phia game: khac o, rao ban, dang giao dich
            final NV v = new NV(10_000_000L);
            final Item av = mon(v, ID_VK30, 8, 100);
            final Item kh = mon(v, ID_KHAN, 0, 200);
            kq = chuyen(v, av, kh, false);
            bang("vu khi -> khan: tu choi", "8/0", av.plus + "/" + kh.plus);
            final Item bv = mon(v, ID_VK40, 0, 200);
            bv.isSelling = true;
            chuyen(v, av, bv, false);
            bang("nhan dang rao ban: tu choi", "8/0", av.plus + "/" + bv.plus);
            bv.isSelling = false;
            v.userTrade = new java.util.Vector<Char>();
            v.userTrade.add(v);
            kq = chuyen(v, av, bv, false);
            bang("dang giao dich: tu choi", "8/0/10000000", av.plus + "/" + bv.plus + "/" + v.xu);
            v.userTrade = null;
            bang("het giao dich: duoc", Integer.valueOf(7), Integer.valueOf(chuyen(v, av, bv, false).startsWith("Chuyển luyện thành công") ? bv.plus : -1));

            // 8) menu xac nhan: 2 lua chon + Huy, gia dung
            final NV w = new NV(10_000_000L);
            final Item aw = mon(w, ID_VK30, 9, 100);
            final Item bw = mon(w, ID_VK40, 2, 200);
            final ChuyenLuyenGame.Chon c = ChuyenLuyenGame.chon(w);
            ChuyenLuyenGame.hoiXacNhan(w, c, aw.id, bw.id);
            bang("menu xac nhan: mat cap / bua / huy", "0,1,2", c.luaChon[0] + "," + c.luaChon[1] + "," + c.luaChon[2]);
            bang("menu xac nhan: gia khong bua = +8", Long.valueOf(xuMong(40, 8)), Long.valueOf(c.xuHoi[0]));
            bang("menu xac nhan: gia co bua = +9", Long.valueOf(xuMong(40, 9)), Long.valueOf(c.xuHoi[1]));
            bang("menu xac nhan: gui menu", Integer.valueOf(1), Integer.valueOf(w.goi.size()));
            final Item bw2 = mon(w, ID_VK40, 8, 200);
            ChuyenLuyenGame.hoiXacNhan(w, c, aw.id, bw2.id);
            bang("nhan +8, nguon +9: chi con lua chon bua + huy", "1,2", c.luaChon[0] + "," + c.luaChon[1]);
        } catch (final Exception ex) {
            bang("KiemChuyenLuyenGame khong nem loi", "", String.valueOf(ex));
            ex.printStackTrace();
        } finally {
            LauDai.dat(lauDaiCu);
            ThongSoLauDai.CHI_PHI_NC = chiPhiCu;
            ThongSoLauDai.DA_NC = daCu;
            ThongSoLauDai.BAC_TB_CAP = bacCu;
            ThongSoLauDai.CHI_SO_NC_PHAN_TRAM = chiSoCu;
            for (int j = 0; j < IDS.length; j++) {
                for (int i = 0; i < 6; i++) {
                    if (cu[j][i] == null) {
                        Map.itemTemplates.get(i).remove(Integer.valueOf(IDS[j]));
                    } else {
                        Map.itemTemplates.get(i).put(Integer.valueOf(IDS[j]), cu[j][i]);
                    }
                }
            }
        }
    }
}
