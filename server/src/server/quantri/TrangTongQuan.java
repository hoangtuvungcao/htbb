package server.quantri;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import static server.quantri.GiaoDien.s;

/** Trang 1 - Tong quan: the so lieu song, bieu do 30 phut, co tinh nang, su kien, bao tri. */
public final class TrangTongQuan extends Trang {

    private final DuLieuTrucTiep du;
    private final ThanhPhan.TheChiSo tOnline, tKetNoi, tChay, tHeap, tCpu, tThread, tVao, tRa, tNgap, tLoi, tDb, tSpam;
    private final JPanel oCo = danhSach();
    private final JPanel oSuKien = danhSach();
    private final JPanel oBaoTri = danhSach();
    private long lanDoc;
    private final BieuDo.Duong bdNguoi, bdGoi, bdHeap;
    private String nhanTraiCu = "";

    public TrangTongQuan(final DuLieuTrucTiep du) {
        super("Tổng quan", "Sức khoẻ máy chủ theo thời gian thực - cập nhật mỗi giây, không ảnh hưởng luồng game.");
        this.du = du;
        tOnline = the("Người chơi", GiaoDien.DO, "Heart_Pink").duong(du.online);
        tKetNoi = the("Kết nối mở", GiaoDien.XANH_DUONG, "Ring_Target");
        tChay = the("Thời gian chạy", GiaoDien.XANH_LA_DAM, "Note_Gold");
        tHeap = the("Bộ nhớ heap", GiaoDien.TIM, "Box_Purple").duong(du.heapMb);
        tCpu = the("CPU tiến trình", GiaoDien.CAM, "Flame_Orb").duong(du.cpu);
        tThread = the("Luồng (thread)", GiaoDien.XANH_NUOC, "Silk_Gray").duong(du.thread);
        tVao = the("Gói vào / giây", GiaoDien.XANH_DUONG, "Orb_Blue").duong(du.goiVao);
        tRa = the("Gói ra / giây", GiaoDien.XANH_LA, "Orb_Green").duong(du.goiRa);
        tNgap = the("Chặn ngập / phút", GiaoDien.VANG_DAM, "Iron_Ball");
        tLoi = the("Lỗi / phút", GiaoDien.DO_DAM, "Sticky_Barb").duong(du.loi);
        tDb = the("Kết nối CSDL", GiaoDien.XANH_DUONG_DAM, "Chest_Blue");
        tSpam = the("Chặn spam", GiaoDien.TIM, "Specs_Yellow");

        final JPanel luoiThe = luoi(s(176), tOnline, tKetNoi, tChay, tHeap, tCpu, tThread, tVao, tRa, tNgap, tLoi, tDb, tSpam);

        bdNguoi = new BieuDo.Duong().tran(10).them("Người chơi", du.online, GiaoDien.DO)
                .them("CPU %", du.cpu, GiaoDien.CAM);
        bdGoi = new BieuDo.Duong().tran(10).them("Gói vào/s", du.goiVao, GiaoDien.XANH_DUONG)
                .them("Gói ra/s", du.goiRa, GiaoDien.XANH_LA);
        bdHeap = new BieuDo.Duong().tran(64).them("Heap MB", du.heapMb, GiaoDien.TIM)
                .them("Lỗi/phút", du.loi, GiaoDien.DO_DAM);
        final ThanhPhan.The c1 = new ThanhPhan.The("Người chơi & CPU · 30 phút", icon("Heart_Pink")).nhan(GiaoDien.DO);
        c1.noiDung(cao(bdNguoi, 200));
        final ThanhPhan.The c2 = new ThanhPhan.The("Lưu lượng gói tin · 30 phút", icon("Orb_Blue")).nhan(GiaoDien.XANH_DUONG);
        c2.noiDung(cao(bdGoi, 200));
        final ThanhPhan.The c3 = new ThanhPhan.The("Bộ nhớ & lỗi · 30 phút", icon("Box_Purple")).nhan(GiaoDien.TIM);
        c3.noiDung(cao(bdHeap, 200));

        final ThanhPhan.The cCo = new ThanhPhan.The("Tính năng đang bật", icon("Key_White")).nhan(GiaoDien.VANG);
        cCo.noiDung(oCo);
        final ThanhPhan.The cSk = new ThanhPhan.The("Sự kiện đang chạy", icon("Crown_Yellow")).nhan(GiaoDien.XANH_LA);
        cSk.noiDung(oSuKien);
        final ThanhPhan.The cBt = new ThanhPhan.The("Bảo trì & đăng nhập", icon("Machine_Green")).nhan(GiaoDien.XANH_DUONG);
        cBt.noiDung(oBaoTri);

        thanh.add(cot(luoiThe, luoi(s(360), c1, c2, c3), tiLe(new double[]{1, 1.4, 1.1}, cCo, cSk, cBt)), BorderLayout.NORTH);
    }

    static java.awt.Image anh(final String ten) {
        return AnhPokemon.vatPham(ten);
    }

    static javax.swing.Icon icon(final String ten) {
        return ThanhPhan.iconPixel(AnhPokemon.vatPham(ten), s(20));
    }

    private static ThanhPhan.TheChiSo the(final String t, final Color m, final String icon) {
        return new ThanhPhan.TheChiSo(t, m, anh(icon));
    }

    @Override
    public String ten() {
        return "Tổng quan";
    }

    @Override
    public String icon() {
        return "Orb_Red";
    }

    @Override
    public void khiHien() {
        docCham();
    }

    @Override
    public void nhip(final DuLieuTrucTiep.Mau m) {
        if (m == null) {
            return;
        }
        final int pctHeap = m.heapToiDa > 0 ? (int) (m.heapDung * 100 / m.heapToiDa) : 0;
        tOnline.capNhat(DinhDang.so(m.online), "Đỉnh " + DinhDang.so(m.dinh) + " · giới hạn " + DinhDang.so(gioiHan()),
                m.online >= gioiHan() * 0.9 ? GiaoDien.CHU_Y : GiaoDien.TOT);
        tKetNoi.capNhat(DinhDang.so(m.ketNoi), "kể cả đang đăng nhập", GiaoDien.TRUNG_TINH);
        tChay.capNhat(DinhDang.dongHo(m.chayMs), "từ " + DinhDang.ngayGio(m.luc - m.chayMs), GiaoDien.TOT);
        tHeap.capNhat(DinhDang.so(m.heapDung >> 20) + " MB", pctHeap + "% của " + DinhDang.so(m.heapToiDa >> 20) + " MB",
                pctHeap > 90 ? GiaoDien.NGUY : pctHeap > 75 ? GiaoDien.CHU_Y : GiaoDien.TOT);
        tCpu.capNhat(m.cpu < 0 ? "—" : m.cpu + "%", Runtime.getRuntime().availableProcessors() + " nhân CPU",
                m.cpu > 85 ? GiaoDien.NGUY : m.cpu > 60 ? GiaoDien.CHU_Y : GiaoDien.TOT);
        tThread.capNhat(DinhDang.so(m.thread), "luồng xử lý của máy chủ", m.thread > 6000 ? GiaoDien.CHU_Y : GiaoDien.TOT);
        tVao.capNhat(DinhDang.gon(Math.round(m.goiVaoGiay)), "client → máy chủ", GiaoDien.TRUNG_TINH);
        tRa.capNhat(DinhDang.gon(Math.round(m.goiRaGiay)), "máy chủ → client", GiaoDien.TRUNG_TINH);
        tNgap.capNhat(DinhDang.so(Math.round(m.catNgapPhut)), "tổng bỏ gói " + DinhDang.gon(m.goiBoTong) + " · cắt "
                + DinhDang.so(m.catNgapTong), m.catNgapPhut > 50 ? GiaoDien.NGUY : m.catNgapPhut > 0 ? GiaoDien.CHU_Y : GiaoDien.TOT);
        tLoi.capNhat(DinhDang.so(Math.round(m.loiPhut)), "lỗi trong 60 giây qua",
                m.loiPhut > 30 ? GiaoDien.NGUY : m.loiPhut > 0 ? GiaoDien.CHU_Y : GiaoDien.TOT);
        tDb.capNhat(m.dbRanh < 0 ? "—" : DinhDang.so(m.dbRanh) + " rảnh", m.dbTong < 0 ? "chưa có pool" : "tối đa "
                + (m.dbToiDa > 0 ? DinhDang.so(m.dbToiDa) : "?") + " · mở thêm " + DinhDang.so(m.dbTong),
                m.dbRanh == 0 ? GiaoDien.CHU_Y : GiaoDien.TOT);
        tSpam.capNhat(DinhDang.gon(m.chatChan + m.thaoTacChan), "chat " + DinhDang.gon(m.chatChan) + " · thao tác "
                + DinhDang.gon(m.thaoTacChan), GiaoDien.TRUNG_TINH);
        if (System.currentTimeMillis() - lanDoc > 15000L) {
            docCham();
        }
        // truc thoi gian: chua du 30 phut thi bat dau tu diem dau (khong de trong ben trai roi "vot" o cuoi)
        final int n = du.online.soDiem();
        final String trai = n >= DuLieuTrucTiep.SO_DIEM ? "30 phút trước"
                : "từ " + new java.text.SimpleDateFormat("HH:mm").format(new java.util.Date(m.luc
                        - Math.max(0, n - 1) * (long) DuLieuTrucTiep.GIAY_MOI_DIEM * 1000L));
        if (!trai.equals(nhanTraiCu)) {
            nhanTraiCu = trai;
            for (final BieuDo.Duong d : new BieuDo.Duong[]{bdNguoi, bdGoi, bdHeap}) {
                d.nhanTruc(trai, "bây giờ");
            }
        }
        bdNguoi.repaint();
        bdGoi.repaint();
        bdHeap.repaint();
    }

    static int gioiHan() {
        try {
            return server.TeamServer.LIMIT_CCU > 0 ? server.TeamServer.LIMIT_CCU : 1000;
        } catch (final Throwable t) {
            return 1000;
        }
    }

    /** Phan doc CSDL / tep: ngoai EDT, 15 giay mot lan. */
    void docCham() {
        lanDoc = System.currentTimeMillis();
        HopThoai.doc(() -> {
            final Object[] kq = new Object[4];
            kq[0] = ThaoTacMayChu.coTinhNang();
            final List<String> sk = new ArrayList<String>();
            final List<Object[]> r = ThaoTacMayChu.truyVan("SELECT ten, ma FROM tob_su_kien WHERE dang_bat=1 ORDER BY ten");
            if (ThaoTacMayChu.loiTruyVan(r) == null) {
                for (int i = 1; i < r.size(); i++) {
                    sk.add(String.valueOf(r.get(i)[0] == null ? r.get(i)[1] : r.get(i)[0]));
                }
            }
            kq[1] = sk;
            List<String> st;
            try {
                st = com.htbb.pokemon.QuanTriSinhThai.trangThai();
            } catch (final Throwable t) {
                st = new ArrayList<String>();
            }
            kq[2] = st;
            kq[3] = new Object[]{ThaoTacMayChu.docBaoTri(), ThaoTacMayChu.gioBaoTriHangNgay(), dungDangNhap()};
            return kq;
        }, this::hienCham);
    }

    static boolean dungDangNhap() {
        try {
            return real.cmd.LoginHandler.stopLogin;
        } catch (final Throwable t) {
            return false;
        }
    }

    @SuppressWarnings("unchecked")
    void hienCham(final Object[] kq) {
        oCo.removeAll();
        final Map<String, Boolean> co = (Map<String, Boolean>) kq[0];
        for (final Map.Entry<String, Boolean> e : co.entrySet()) {
            oCo.add(dongCo(ThaoTacMayChu.tenCo(e.getKey()), e.getValue()));
        }
        oSuKien.removeAll();
        final List<String> sk = (List<String>) kq[1];
        if (sk.isEmpty()) {
            oSuKien.add(ThanhPhan.nhanPhu("Không có sự kiện nào đang bật."));
        } else {
            oSuKien.add(ThanhPhan.doanVan(String.join(" · ", sk), false));
        }
        for (final String s0 : (List<String>) kq[2]) {
            oSuKien.add(ThanhPhan.doanVan(s0, true));
        }
        oBaoTri.removeAll();
        final Object[] bt = (Object[]) kq[3];
        final String[] tep = (String[]) bt[0];
        final boolean dangBt = tep != null || server.ChiSoMayChu.dangBaoTri();
        oBaoTri.add(dongNhan("Chế độ bảo trì", dangBt ? "Đang bảo trì" : "Không bảo trì", dangBt ? GiaoDien.DO : GiaoDien.XANH_LA));
        oBaoTri.add(dongNhan("Đăng nhập mới", (Boolean) bt[2] ? "Đang tắt" : "Đang mở", (Boolean) bt[2] ? GiaoDien.DO : GiaoDien.XANH_LA));
        oBaoTri.add(dongNhan("Bảo trì tự động", String.valueOf(bt[1]), GiaoDien.XANH_DUONG));
        oBaoTri.add(dongNhan("Giờ máy chủ", DinhDang.ngayGio(System.currentTimeMillis()), GiaoDien.TIM));
        GiaoDien.toCay(this);
        revalidate();
        repaint();
    }

    static String esc(final String s) {
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }

    static JPanel dongCo(final String ten, final Boolean bat) {
        return dongNhan(ten, bat == null ? "Chưa có" : bat ? "Bật" : "Tắt",
                bat == null ? GiaoDien.chuMo : bat ? GiaoDien.XANH_LA : GiaoDien.DO);
    }

    static JPanel dongNhan(final String ten, final String gt, final Color m) {
        final JPanel p = new JPanel(new BorderLayout(s(8), 0));
        p.setOpaque(false);
        if (ten.startsWith("<html>")) {
            p.add(ThanhPhan.doanVan(ten.replaceAll("<[^>]+>", "").replace("&lt;", "<").replace("&gt;", ">").replace("&amp;", "&"), false),
                    BorderLayout.CENTER);
        } else {
            p.add(ThanhPhan.nhan(ten), BorderLayout.CENTER);
        }
        final JPanel giua = new JPanel(new java.awt.GridBagLayout());
        giua.setOpaque(false);
        giua.add(new ThanhPhan.HuyHieu(gt, m));
        p.add(giua, BorderLayout.EAST);
        p.setBorder(new EmptyBorder(s(2), 0, s(2), 0));
        return p;
    }

    /** Anh dai dien Pokemon tron (dung chung nhieu trang). */
    static BufferedImage avatar(final int dex, final int co) {
        return AnhPokemon.tron(AnhPokemon.chanDung(dex), co, GiaoDien.toi ? new Color(0x2A3248) : new Color(0xEEF1FA));
    }
}
