package real;

import com.htbb.pokemon.CheTaoLogic;
import com.htbb.pokemon.LauDai;
import com.htbb.pokemon.NgocHe;
import io.Message;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

/**
 * CHE TAO NGUYEN TU (bao loi Zalo 03/11/2026 "Chế tạo trang bị xong vào kho không thấy mà xu cũng trừ rồi"):
 * CheTaoGame.cheTao chi tru xu / da / nguyen lieu khi MON DA TAO DUOC va con cho trong hanh trang. Ban cu tru truoc
 * roi moi taoMon -> taoMon tra null (mau id 2 / mau loi / ngoai le) = mat xu, khong co mon. Goi tu Kiem.java qua
 * {@link #BAO}.
 */
public final class KiemCheTaoNguyenTu {

    private KiemCheTaoNguyenTu() {
    }

    public interface Bao {
        void bang(String moTa, Object mongDoi, Object thucTe);
    }

    public static Bao BAO;

    static void bang(final String m, final Object a, final Object b) {
        BAO.bang(m, a, b);
    }

    static final int ID_HONG = 2; // Map.dropItemByIdTemplate tra null cho mau id 2 -> taoMon null
    static final int ID_TOT = 9_901;
    static final long GIA = 16_170L;
    static int idKe = 880_100;

    static final class NV extends Char {
        final List<Message> goi = new ArrayList<Message>();

        NV(final long xu) {
            super(null);
            charDBID = idKe;
            id = (short) (idKe % 30000);
            idKe++;
            charname = "kiemchetao" + charDBID;
            lvDetail = new LevelDetail();
            lvDetail.lv = 50;
            charClass = 0;
            MAX_INVENTORY = 42;
            maxBag = 1;
            listGemitem = new int[400];
            listGemitemLock = new int[400];
            listGemitemSell = new int[400];
            this.xu = xu;
        }

        @Override
        public void sendMessage(final Message m) {
            goi.add(m);
        }
    }

    static ItemTemplates mau(final int id, final String ten) {
        final ItemTemplates t = new ItemTemplates();
        t.id = id;
        t.type = 9;
        t.name = ten;
        t.level = 44;
        t.clazz = -1;
        t.atb = new short[12];
        t.atb[0] = 100;
        for (int i = 0; i < 6; i++) {
            Map.itemTemplates.get(i).put(Integer.valueOf(id), t);
        }
        return t;
    }

    static CheTaoLogic.CongThuc ct(final int id, final String ten) {
        return new CheTaoLogic.CongThuc(id, ten, 1, 1, 0, "", 0, 0, GIA, 0, false, true);
    }

    static int dem(final Char p, final int tpl) {
        int n = 0;
        for (final Item it : p.iItems) {
            if (it.tempateID == tpl) {
                n++;
            }
        }
        return n;
    }

    public static void chay() {
        final boolean lauDaiCu = LauDai.bat();
        final BoNgocGame.DuLieu dlCu = BoNgocGame.DL;
        final ItemTemplates[] cu = new ItemTemplates[12];
        final GemTemplate[] gemCu = Map.gemTemplate;
        try {
            LauDai.dat(true);
            if (Map.gemTemplate == null) {
                Map.gemTemplate = new GemTemplate[0];
            }
            while (Map.itemTemplates.size() < 6) {
                Map.itemTemplates.add(new Hashtable<Integer, ItemTemplates>());
            }
            for (int i = 0; i < 6; i++) {
                cu[i] = Map.itemTemplates.get(i).get(Integer.valueOf(ID_HONG));
                cu[6 + i] = Map.itemTemplates.get(i).get(Integer.valueOf(ID_TOT));
            }
            mau(ID_HONG, "Mẫu hỏng");
            mau(ID_TOT, "Vòng cổ kiểm thử");
            final BoNgocGame.DuLieu d = new BoNgocGame.DuLieu();
            final List<CheTaoLogic.CongThuc> ds = new ArrayList<CheTaoLogic.CongThuc>();
            ds.add(ct(ID_HONG, "Mẫu hỏng"));
            ds.add(ct(ID_TOT, "Vòng cổ kiểm thử"));
            d.congThuc = ds;
            BoNgocGame.datDuLieu(d);

            // 1) mon tao that bai -> KHONG mat xu, khong them mon, pity giu nguyen
            final NV a = new NV(48_318L);
            final NgocHe.Tui ta = new NgocHe.Tui();
            ta.cheTaoPity = 5;
            BoNgocGame.datTuiKiem(a.charDBID, ta);
            final int truocA = a.iItems.size();
            CheTaoGame.cheTao(a, ID_HONG, -1, 0);
            bang("che tao: tao mon loi -> khong tru xu", Long.valueOf(48_318L), Long.valueOf(a.xu));
            bang("che tao: tao mon loi -> hanh trang khong doi", Integer.valueOf(truocA), Integer.valueOf(a.iItems.size()));
            bang("che tao: tao mon loi -> pity khong doi", Integer.valueOf(5), Integer.valueOf(ta.cheTaoPity));

            // 2) thanh cong -> tru dung gia, mon vao hanh trang (iItems), co id + chu
            final NV b = new NV(48_318L);
            BoNgocGame.datTuiKiem(b.charDBID, new NgocHe.Tui());
            CheTaoGame.cheTao(b, ID_TOT, -1, 0);
            bang("che tao: thanh cong -> tru dung " + GIA + " xu", Long.valueOf(48_318L - GIA), Long.valueOf(b.xu));
            bang("che tao: thanh cong -> mon nam trong hanh trang", Integer.valueOf(1), Integer.valueOf(dem(b, ID_TOT)));
            Item mon = null;
            for (final Item it : b.iItems) {
                if (it.tempateID == ID_TOT) {
                    mon = it;
                }
            }
            bang("che tao: mon co chu = nhan vat", Integer.valueOf(b.charDBID), Integer.valueOf(mon == null ? -1 : mon.owner));
            // bao loi rongngoo 05/11/2026 "che tao xong phai doi lau moi co trong hanh trang": ban cu chi gui 16 kieu 0 (xu +
            // thuoc), danh sach mon (kieu 1) co mon moi phai gui NGAY, truoc goi ket qua -120/3
            int i16 = -1;
            int iKq = -1;
            for (int i = 0; i < b.goi.size(); i++) {
                final byte[] g = b.goi.get(i).toByteArray();
                if (b.goi.get(i).command == 16 && g.length > 3 && g[3] == 1 && i16 < 0) {
                    i16 = i;
                }
                if (b.goi.get(i).command == BoNgocGame.LENH && g.length > 3 && g[3] == BoNgocGame.S_CHE_TAO_KQ && iKq < 0) {
                    iKq = i;
                }
            }
            bang("che tao: gui hanh trang 16 kieu 1 (danh sach mon) ngay", Boolean.TRUE, Boolean.valueOf(i16 >= 0));
            bang("che tao: hanh trang truoc goi ket qua", Boolean.TRUE, Boolean.valueOf(i16 >= 0 && (iKq < 0 || i16 < iKq)));

            // 3) thieu xu -> khong gi doi
            final NV c = new NV(GIA - 1);
            BoNgocGame.datTuiKiem(c.charDBID, new NgocHe.Tui());
            CheTaoGame.cheTao(c, ID_TOT, -1, 0);
            bang("che tao: thieu xu -> xu giu nguyen", Long.valueOf(GIA - 1), Long.valueOf(c.xu));
            bang("che tao: thieu xu -> khong co mon", Integer.valueOf(0), Integer.valueOf(dem(c, ID_TOT)));

            // 4) hanh trang day -> khong tru xu
            final NV e = new NV(48_318L);
            BoNgocGame.datTuiKiem(e.charDBID, new NgocHe.Tui());
            while (!e.isFullInventory()) {
                final Item it = new Item();
                it.tempateID = ID_HONG;
                it.id = e.getIDItem();
                e.iItems.add(it);
            }
            CheTaoGame.cheTao(e, ID_TOT, -1, 0);
            bang("che tao: tui day -> khong tru xu", Long.valueOf(48_318L), Long.valueOf(e.xu));
            bang("che tao: tui day -> khong co mon", Integer.valueOf(0), Integer.valueOf(dem(e, ID_TOT)));

            // 5) ma nguon: taoMon goi TRUOC truNeuDu (khong quay lai thu tu cu)
            final String src = KiemCheTaoNguyenTuDoc.doc("src/real/CheTaoGame.java");
            final int iTao = src.indexOf("it = taoMon(p, t, bac, he, kieuDa)");
            final int iTru = src.indexOf("p.truNeuDu(");
            bang("che tao: taoMon truoc truNeuDu trong ma nguon", Boolean.TRUE, Boolean.valueOf(iTao > 0 && iTru > iTao));
            bang("che tao: thong bao noi ro 'Đã vào hành trang'", Boolean.TRUE, Boolean.valueOf(src.contains("\"Đã vào hành trang\"")));
        } catch (final Exception ex) {
            bang("KiemCheTaoNguyenTu khong nem loi", "", String.valueOf(ex));
            ex.printStackTrace();
        } finally {
            Map.gemTemplate = gemCu;
            LauDai.dat(lauDaiCu);
            BoNgocGame.datDuLieu(dlCu);
            for (int i = 0; i < 6; i++) {
                if (cu[i] == null) {
                    Map.itemTemplates.get(i).remove(Integer.valueOf(ID_HONG));
                } else {
                    Map.itemTemplates.get(i).put(Integer.valueOf(ID_HONG), cu[i]);
                }
                if (cu[6 + i] == null) {
                    Map.itemTemplates.get(i).remove(Integer.valueOf(ID_TOT));
                } else {
                    Map.itemTemplates.get(i).put(Integer.valueOf(ID_TOT), cu[6 + i]);
                }
            }
        }
    }
}

final class KiemCheTaoNguyenTuDoc {
    private KiemCheTaoNguyenTuDoc() {
    }

    static String doc(final String f) {
        try {
            return new String(java.nio.file.Files.readAllBytes(java.nio.file.Paths.get(f)), java.nio.charset.StandardCharsets.UTF_8);
        } catch (final Exception e) {
            return "";
        }
    }
}
