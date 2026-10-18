package com.htbb.chongspam;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.function.Consumer;

/**
 * GIOI HAN + GOP thong bao TOAN MAY CHU (server -> moi client): "Chuc mung X nhan duoc...",
 * bung phat sinh thai, alpha / boss xuat hien, ket qua su kien...
 *
 * VI SAO. Moi thong bao la MOT goi tin cho MOI nguoi dang online. Dong nguoi thi chinh dong
 * nguoi lai sinh ra nhieu thong bao (nhieu nguoi mo hop / ha quai cung luc): N nguoi x N thong
 * bao = N^2 goi tin, va client hien thong bao theo hang doi (moi tin 15-20 giay) nen tin bi
 * don lai hang phut - nguoi choi chi thay mot dai chu chay khong dut.
 *
 * LUAT:
 *   - Trung noi dung (chuan hoa) trong sv.thongBao.trungLapGiay -> bo.
 *   - Toi da sv.thongBao.toiDa tin / sv.thongBao.cuaSoGiay cho ca may chu (cua so truot);
 *     moi LOAI them tran rieng sv.thongBao.moiLoai (mot su kien on ao khong an het tran chung).
 *   - Vuot tran -> vao hang cho (toi da 50); cu sv.thongBao.gopGiay giay, neu tran con cho,
 *     gui MOT tin gop: "tin 1 | tin 2 (+N tin khac)". Hang cho day thi bo tin cu nhat.
 *   - Tin UU TIEN (bao tri, admin) khong qua day - goi thang Map.sendAllCharServer nhu cu.
 *
 * Logic thuan (thoi gian truyen vao, dau ra la Consumer) de kiem thu.
 */
public final class ThongBaoGop {

    private static final int HANG_CHO_TOI_DA = 50;

    static volatile boolean bat = true;
    static volatile ChongSpam.Nhip tranChung = new ChongSpam.Nhip(6, 60000L);
    static volatile int moiLoai = 3;
    static volatile long trungLapMs = 120000L;
    static volatile long gopMs = 15000L;

    private static final ChongSpam.Vong VONG_CHUNG = new ChongSpam.Vong();
    private static final Map<String, ChongSpam.Vong> VONG_LOAI = new LinkedHashMap<String, ChongSpam.Vong>();
    private static final LinkedHashMap<String, Long> DA_GUI = new LinkedHashMap<String, Long>();
    private static final List<String> CHO = new ArrayList<String>();
    private static long mocGop;
    private static int boTrung;
    private static int boDay;

    /** Noi gui that (mac dinh: moi nguoi online qua real.Map). Kiem thu thay bang Consumer rieng. */
    private static volatile Consumer<String> boGui = null;
    private static volatile Thread luong;

    private ThongBaoGop() {
    }

    /** Dua mot thong bao toan may chu (loai = nhom tin, vd "nhan_thuong", "sinh_thai", "boss"). */
    public static void dua(final String loai, final String tin) {
        final String gui = nhan(loai, tin, System.currentTimeMillis());
        if (gui != null) {
            phat(gui);
        }
        batLuong();
    }

    /**
     * @return chuoi can gui NGAY, hoac null (bo vi trung / da vao hang cho gop).
     */
    static synchronized String nhan(final String loai, final String tin, final long bayGio) {
        if (tin == null || tin.trim().isEmpty()) {
            return null;
        }
        if (!bat) {
            return tin;
        }
        final String chuan = ChongSpam.chuanHoa(tin);
        donDaGui(bayGio);
        final Long cu = DA_GUI.get(chuan);
        if (cu != null && bayGio - cu.longValue() < trungLapMs) {
            boTrung++;
            return null;
        }
        DA_GUI.put(chuan, Long.valueOf(bayGio));
        final String l = loai == null ? "" : loai;
        ChongSpam.Vong v = VONG_LOAI.get(l);
        if (v == null) {
            v = new ChongSpam.Vong();
            VONG_LOAI.put(l, v);
        }
        // Thu tran loai truoc tren mot BAN SAO de khong "tieu" tran loai khi tran chung het.
        if (CHO.isEmpty() && conTran(v, new ChongSpam.Nhip(moiLoai, tranChung.ms), bayGio)
                && VONG_CHUNG.choPhep(tranChung, bayGio)) {
            v.choPhep(new ChongSpam.Nhip(moiLoai, tranChung.ms), bayGio);
            return tin;
        }
        if (CHO.isEmpty() && bayGio - mocGop >= gopMs) {
            mocGop = bayGio; // tin dau tien vao hang cho: doi du gopMs roi moi gop
        }
        CHO.add(tin);
        if (CHO.size() > HANG_CHO_TOI_DA) {
            CHO.remove(0);
            boDay++;
        }
        return null;
    }

    private static boolean conTran(final ChongSpam.Vong v, final ChongSpam.Nhip n, final long bayGio) {
        if (n.so <= 0) {
            return true;
        }
        if (v.moc == null || v.moc.length != n.so) {
            return true;
        }
        final long cu = v.moc[v.vt];
        return cu == 0L || bayGio - cu >= n.ms;
    }

    /** Goi dinh ky: neu den gio gop va tran chung con, tra ve MOT tin gop tu hang cho. */
    static synchronized String gop(final long bayGio) {
        if (CHO.isEmpty() || bayGio - mocGop < gopMs) {
            return null;
        }
        if (!VONG_CHUNG.choPhep(tranChung, bayGio)) {
            return null;
        }
        mocGop = bayGio;
        final String ra;
        if (CHO.size() == 1) {
            ra = CHO.get(0);
        } else {
            final int n = CHO.size();
            ra = CHO.get(n - 2) + " | " + CHO.get(n - 1) + (n > 2 ? " (+" + (n - 2) + " tin khác)" : "");
        }
        CHO.clear();
        return ra;
    }

    private static void donDaGui(final long bayGio) {
        final Iterator<Map.Entry<String, Long>> it = DA_GUI.entrySet().iterator();
        while (it.hasNext()) {
            final Map.Entry<String, Long> e = it.next();
            if (bayGio - e.getValue().longValue() >= trungLapMs || DA_GUI.size() > 500) {
                it.remove();
            } else {
                break; // LinkedHashMap theo thu tu chen: phan con lai moi hon
            }
        }
    }

    /** So tin bo vi trung + vi hang cho day (tu lan goi truoc), roi dat lai - cho chi so / log. */
    public static synchronized int[] layVaDatLaiDem() {
        final int[] r = {boTrung, boDay, CHO.size()};
        boTrung = 0;
        boDay = 0;
        return r;
    }

    private static void phat(final String s) {
        final Consumer<String> g = boGui;
        if (g != null) {
            g.accept(s);
            return;
        }
        try {
            real.Map.sendAllCharServer(-1, real.MessageCreator.createServerAlertAutoOffMessage(s));
        } catch (final Exception e) {
            com.htbb.log.Nhat.canhBao("THONG_BAO", "gui thong bao toan server loi: " + e);
        }
    }

    private static synchronized void batLuong() {
        if (luong != null || boGui != null) {
            return;
        }
        final Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    try {
                        Thread.sleep(1000L);
                        final String s = gop(System.currentTimeMillis());
                        if (s != null) {
                            phat(s);
                        }
                    } catch (final InterruptedException e) {
                        return;
                    } catch (final Throwable t) {
                        com.htbb.log.Nhat.canhBao("THONG_BAO", "luong gop thong bao loi: " + t);
                    }
                }
            }
        }, "THONG BAO GOP");
        t.setDaemon(true);
        luong = t;
        t.start();
    }

    public static synchronized void docCauHinh(final Properties p) {
        bat = !"0".equals(p.getProperty("sv.thongBao.bat", "1").trim());
        final int toiDa = so(p, "sv.thongBao.toiDa", 6);
        final int cuaSo = Math.max(1, so(p, "sv.thongBao.cuaSoGiay", 60));
        tranChung = new ChongSpam.Nhip(toiDa, cuaSo * 1000L);
        moiLoai = so(p, "sv.thongBao.moiLoai", 3);
        trungLapMs = so(p, "sv.thongBao.trungLapGiay", 120) * 1000L;
        gopMs = Math.max(1, so(p, "sv.thongBao.gopGiay", 15)) * 1000L;
    }

    public static String moTa() {
        return "THONG BAO TOAN SERVER -> " + (bat ? "BAT" : "TAT") + ", toi da " + tranChung + " (moi loai " + moiLoai
                + "), trung lap " + (trungLapMs / 1000L) + "s, gop moi " + (gopMs / 1000L) + "s";
    }

    private static int so(final Properties p, final String k, final int macDinh) {
        try {
            final String v = p.getProperty(k);
            return v == null || v.trim().isEmpty() ? macDinh : Integer.parseInt(v.trim());
        } catch (final NumberFormatException e) {
            return macDinh;
        }
    }

    /** Kiem thu: Consumer nhan tin thay vi gui that; null = gui that. Xoa trang thai. */
    public static synchronized void datLaiChoKiemThu(final Consumer<String> g) {
        boGui = g;
        VONG_CHUNG.moc = null;
        VONG_LOAI.clear();
        DA_GUI.clear();
        CHO.clear();
        mocGop = 0L;
        boTrung = 0;
        boDay = 0;
        docCauHinh(new Properties());
    }

    /** Kiem thu: dua voi thoi gian gia. */
    public static String nhanKiemThu(final String loai, final String tin, final long bayGio) {
        return nhan(loai, tin, bayGio);
    }

    public static String gopKiemThu(final long bayGio) {
        return gop(bayGio);
    }
}
