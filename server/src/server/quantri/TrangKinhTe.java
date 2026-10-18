package server.quantri;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

import static server.quantri.GiaoDien.s;

/** Trang 4 - Kinh te: chi so hang ngay (tob_kinh_te_ngay), bieu do, top nguoi giu tien, nguon vao / ho hut, canh bao. */
public final class TrangKinhTe extends Trang {

    private final ThanhPhan.TheChiSo tNguoi, tCap, tGia, tXu, tLuong, tLk, tTop, tMuc;
    private final BieuDo.Duong bdXu = new BieuDo.Duong().nhanTruc("30 ngày trước", "hôm nay");
    private final BieuDo.Duong bdVaoRa = new BieuDo.Duong().nhanTruc("30 ngày trước", "hôm nay");
    private final BieuDo.Cot cot = new BieuDo.Cot();
    private final JPanel canhBao = danhSach();
    private final DefaultTableModel[] mhTop = new DefaultTableModel[3];
    private long lanDoc;

    public TrangKinhTe() {
        super("Kinh tế", "Tiền lưu hành, nguồn vào / hố hút, người giữ nhiều nhất và cảnh báo lạm phát.");
        tNguoi = new ThanhPhan.TheChiSo("Người hoạt động 7 ngày", GiaoDien.XANH_DUONG, AnhPokemon.vatPham("Heart_Pink"));
        tCap = new ThanhPhan.TheChiSo("Cấp trung vị", GiaoDien.TIM, AnhPokemon.vatPham("Stone_Purple"));
        tGia = new ThanhPhan.TheChiSo("Chỉ số giá (xu/giờ)", GiaoDien.CAM, AnhPokemon.vatPham("Note_Gold"));
        tXu = new ThanhPhan.TheChiSo("Tổng xu lưu hành", GiaoDien.VANG_DAM, AnhPokemon.vatPham("Money_Yellow"));
        tLuong = new ThanhPhan.TheChiSo("Tổng lượng", GiaoDien.XANH_DUONG, AnhPokemon.vatPham("Gummi_Blue"));
        tLk = new ThanhPhan.TheChiSo("Tổng lượng khoá", GiaoDien.XANH_NUOC, AnhPokemon.vatPham("Gummi_Gray"));
        tTop = new ThanhPhan.TheChiSo("Top 1% nắm giữ", GiaoDien.DO, AnhPokemon.vatPham("Crown_Yellow"));
        tMuc = new ThanhPhan.TheChiSo("Mức cảnh báo", GiaoDien.XANH_LA, AnhPokemon.vatPham("Orb_Green"));
        final JPanel luoiThe = luoi(s(225), tNguoi, tCap, tGia, tXu, tLuong, tLk, tTop, tMuc);

        final ThanhPhan.The c1 = new ThanhPhan.The("Tổng xu lưu hành · 30 ngày", TrangTongQuan.icon("Money_Yellow")).nhan(GiaoDien.VANG);
        c1.noiDung(cao(bdXu, 210));
        final ThanhPhan.The c2 = new ThanhPhan.The("Xu vào (nguồn) và xu ra (hố hút) · 30 ngày", TrangTongQuan.icon("Orb_Green"))
                .nhan(GiaoDien.XANH_LA);
        c2.noiDung(cao(bdVaoRa, 210));
        final ThanhPhan.The c3 = new ThanhPhan.The("Nguồn vào / hố hút", TrangTongQuan.icon("Sack_Red")).nhan(GiaoDien.DO);
        final JPanel thanC3 = ThanhPhan.trongSuot(new BorderLayout(0, s(6)));
        thanC3.add(cao(cot, 250), BorderLayout.CENTER);
        thanC3.add(ThanhPhan.doanVan("Xu vào / ra: hôm nay (tính đến lúc này). Lượng: từ lúc máy chủ khởi động.", true),
                BorderLayout.SOUTH);
        c3.noiDung(thanC3);
        final ThanhPhan.The c4 = new ThanhPhan.The("Cảnh báo", TrangTongQuan.icon("Sticky_Barb")).nhan(GiaoDien.CAM);
        c4.noiDung(canhBao);

        final PhanDoan tab = new PhanDoan();
        final String[] ten = {"Xu (túi + kho)", "Lượng", "Lượng khoá"};
        for (int i = 0; i < 3; i++) {
            mhTop[i] = new DefaultTableModel(new Object[]{"#", "Pokémon", "Cấp", ten[i], "Lần cuối"}, 0) {
                @Override
                public boolean isCellEditable(final int r, final int c) {
                    return false;
                }

                @Override
                public Class<?> getColumnClass(final int c) {
                    return c == 0 || c == 2 || c == 3 ? Long.class : Object.class;
                }
            };
            final JTable t = new ThanhPhan.Bang(mhTop[i]);
            ThanhPhan.kieuBang(t);
            ((ThanhPhan.Bang) t).rong("Chưa có Pokémon nào.");
            final int[] rong = {40, 150, 56, 130, 140};
            for (int k = 0; k < rong.length; k++) {
                t.getColumnModel().getColumn(k).setPreferredWidth(s(rong[k]));
            }
            t.getColumnModel().getColumn(0).setMaxWidth(s(48));
            t.getColumnModel().getColumn(2).setMaxWidth(s(70));
            final JScrollPane sp = ThanhPhan.cuon(t);
            sp.setPreferredSize(new Dimension(s(400), s(250)));
            tab.them(ten[i], sp);
        }
        final ThanhPhan.The c5 = new ThanhPhan.The("Người giữ nhiều nhất (top 15)", TrangTongQuan.icon("Chest_Gold")).nhan(GiaoDien.VANG_DAM);
        c5.noiDung(tab);

        thanh.add(cot(luoiThe, c4, luoi(s(420), c1, c2), tiLe(new double[]{1, 1.25}, c3, c5)), BorderLayout.NORTH);
    }

    @Override
    public String ten() {
        return "Kinh tế";
    }

    @Override
    public String icon() {
        return "Money_Yellow";
    }

    @Override
    public void khiHien() {
        doc();
    }

    @Override
    public void nhip(final DuLieuTrucTiep.Mau m) {
        if (System.currentTimeMillis() - lanDoc > 60000L) {
            doc();
        }
    }

    void doc() {
        lanDoc = System.currentTimeMillis();
        HopThoai.doc(() -> new Object[]{
            ThaoTacMayChu.truyVan("SELECT ngay, so_nguoi, cap_trung_vi, chi_so_gia, tong_xu, tong_luong, tong_luong_khoa,"
                + " top1_phan_tram_x100, xu_vao, xu_ra, muc FROM tob_kinh_te_ngay ORDER BY ngay DESC LIMIT 30"),
            ThaoTacMayChu.truyVan("SELECT charname, lastLv, gold + kho_xu AS t, lastLog FROM tob_char WHERE del=1 ORDER BY t DESC LIMIT 15"),
            ThaoTacMayChu.truyVan("SELECT charname, lastLv, luong + kho_luong AS t, lastLog FROM tob_char WHERE del=1 ORDER BY t DESC LIMIT 15"),
            ThaoTacMayChu.truyVan("SELECT charname, lastLv, luonglock + kho_luong_khoa AS t, lastLog FROM tob_char WHERE del=1"
                + " ORDER BY t DESC LIMIT 15"),
            luongNap(), KinhTeTrucTiep.ghiNeuCan(), KinhTeTrucTiep.mauGio()}, this::hien);
    }

    static long[] luongNap() {
        try {
            return new long[]{com.htbb.pokemon.NapTien.luongDaVao(), com.htbb.pokemon.NapTien.luongDaTieu()};
        } catch (final Throwable t) {
            return new long[]{0, 0};
        }
    }

    static long so(final Object o) {
        return o instanceof Number ? ((Number) o).longValue() : 0L;
    }

    @SuppressWarnings("unchecked")
    void hien(final Object[] kq) {
        final List<Object[]> kt = (List<Object[]>) kq[0];
        final KinhTeTrucTiep.Anh song = (KinhTeTrucTiep.Anh) kq[5];
        final List<long[]> gio = (List<long[]>) kq[6];
        final List<String[]> cb = new ArrayList<String[]>();
        final boolean coNgay = ThaoTacMayChu.loiTruyVan(kt) == null && kt.size() > 1;
        final Object[] r = coNgay ? kt.get(1) : null;
        final String luc = song == null ? "" : "trực tiếp lúc " + new java.text.SimpleDateFormat("HH:mm").format(new java.util.Date(song.luc));
        // the: tien te + nguoi hoat dong lay so do TRUC TIEP (chot ngay chi co sau 04:00), so sanh voi chot ngay gan nhat
        if (song != null) {
            tNguoi.capNhat(DinhDang.so(song.nguoiHoatDong), luc + " · " + song.online + " đang online", GiaoDien.TRUNG_TINH);
            tXu.capNhat(DinhDang.gon(song.tongXu), r == null ? luc : doi(song.tongXu, so(r[4]), "so với lần chốt gần nhất"), GiaoDien.TRUNG_TINH);
            tLuong.capNhat(DinhDang.gon(song.tongLuong), r == null ? luc : doi(song.tongLuong, so(r[5]), "so với lần chốt gần nhất"),
                    GiaoDien.TRUNG_TINH);
            tLk.capNhat(DinhDang.gon(song.tongLuongKhoa), r == null ? luc : doi(song.tongLuongKhoa, so(r[6]), "so với lần chốt gần nhất"),
                    GiaoDien.TRUNG_TINH);
        } else {
            for (final ThanhPhan.TheChiSo t : new ThanhPhan.TheChiSo[]{tNguoi, tXu, tLuong, tLk}) {
                t.capNhat("—", "chưa đọc được dữ liệu", GiaoDien.TRUNG_TINH);
            }
        }
        if (r != null) {
            final Object[] t = kt.size() > 2 ? kt.get(2) : null;
            tCap.capNhat(DinhDang.so(so(r[2])), t == null ? "chốt ngày " + r[0] : doi(so(r[2]), so(t[2])), GiaoDien.TRUNG_TINH);
            tGia.capNhat(DinhDang.gon(so(r[3])), t == null ? "chốt ngày " + r[0] : doi(so(r[3]), so(t[3])), GiaoDien.TRUNG_TINH);
            final double top = so(r[7]) / 100.0;
            tTop.capNhat(String.format("%.1f%%", top), "tổng xu do top 1% giữ", top > 40 ? GiaoDien.NGUY : top > 25 ? GiaoDien.CHU_Y : GiaoDien.TOT);
            final int muc = (int) so(r[10]);
            tMuc.capNhat(muc == 2 ? "Đỏ" : muc == 1 ? "Vàng" : "Xanh", "đánh giá ngày " + r[0],
                    muc == 2 ? GiaoDien.NGUY : muc == 1 ? GiaoDien.CHU_Y : GiaoDien.TOT);
            if (muc == 2) {
                cb.add(new String[]{"2", "Cảnh báo kinh tế mức đỏ - tiền đang tập trung hoặc tăng quá nhanh."});
            } else if (muc == 1) {
                cb.add(new String[]{"1", "Cảnh báo kinh tế mức vàng - theo dõi nguồn xu."});
            }
            if (t != null && so(t[4]) > 0 && so(r[4]) > so(t[4]) * 12 / 10) {
                cb.add(new String[]{"2", "Tổng xu tăng " + String.format("%.0f%%", (so(r[4]) - so(t[4])) * 100.0 / so(t[4])) + " trong một ngày."});
            }
            if (top > 40) {
                cb.add(new String[]{"2", "Top 1% giữ " + String.format("%.0f%%", top) + " tổng xu - tài sản đang tập trung."});
            } else if (top > 25) {
                cb.add(new String[]{"1", "Top 1% giữ " + String.format("%.0f%%", top) + " tổng xu."});
            }
        } else {
            for (final ThanhPhan.TheChiSo t : new ThanhPhan.TheChiSo[]{tCap, tGia, tTop}) {
                t.capNhat("—", "có sau lần chốt số 04:00 đầu tiên", GiaoDien.TRUNG_TINH);
            }
            tMuc.capNhat("—", "có sau lần chốt số 04:00 đầu tiên", GiaoDien.TRUNG_TINH);
        }
        // bieu do 30 ngay: chot ngay + mau theo gio + so do hien tai (luon co it nhat 1 diem)
        final List<Object[]> dongNgay = new ArrayList<Object[]>();
        if (coNgay) {
            for (int i = 1; i < kt.size(); i++) {
                final Object[] d = kt.get(i);
                dongNgay.add(new Object[]{d[0], d[4], d[8], d[9]});
            }
        }
        final java.util.LinkedHashMap<String, long[]> ngay = KinhTeTrucTiep.chuoiNgay(dongNgay, gio, song);
        final int n = ngay.size();
        final double[] xu = new double[n], vao = new double[n], ra = new double[n];
        int i0 = 0;
        String dau = "";
        for (final java.util.Map.Entry<String, long[]> e : ngay.entrySet()) {
            if (i0 == 0) {
                dau = e.getKey();
            }
            xu[i0] = e.getValue()[0];
            vao[i0] = e.getValue()[1];
            ra[i0] = e.getValue()[2];
            i0++;
        }
        final String nhanDau = n <= 1 ? "hôm nay" : dau.length() >= 10 ? dau.substring(8, 10) + "/" + dau.substring(5, 7) : dau;
        bdXu.xoaChuoi();
        bdXu.nhanTruc(nhanDau, "hôm nay");
        bdXu.them("Tổng xu", BieuDo.tuMang(xu), GiaoDien.VANG_DAM);
        bdVaoRa.xoaChuoi();
        bdVaoRa.nhanTruc(nhanDau, "hôm nay");
        bdVaoRa.them("Xu vào", BieuDo.tuMang(vao), GiaoDien.XANH_LA).them("Xu ra", BieuDo.tuMang(ra), GiaoDien.DO);
        bdXu.repaint();
        bdVaoRa.repaint();
        final long[] luong = (long[]) kq[4];
        final long v = song != null ? song.xuVao : r != null ? so(r[8]) : 0, rr = song != null ? song.xuRa : r != null ? so(r[9]) : 0;
        cot.dat(new String[]{"Xu vào hôm nay", "Xu ra hôm nay", "Lượng vào (nạp)", "Lượng tiêu"},
                new double[]{v, rr, luong[0], luong[1]},
                new Color[]{GiaoDien.XANH_LA, GiaoDien.DO, GiaoDien.XANH_DUONG, GiaoDien.CAM});
        if (rr > 0 && v > rr * 3 / 2) {
            cb.add(new String[]{"1", "Xu vào gấp " + String.format("%.1f", v / (double) rr) + " lần xu ra - có dấu hiệu lạm phát."});
        }
        if (cb.isEmpty()) {
            cb.add(new String[]{"0", "Kinh tế ổn định - không có cảnh báo."});
        }
        canhBao.removeAll();
        for (final String[] c : cb) {
            final int m = Integer.parseInt(c[0]);
            canhBao.add(TrangTongQuan.dongNhan("<html>" + TrangTongQuan.esc(c[1]) + "</html>", m == 2 ? "Đỏ" : m == 1 ? "Vàng" : "Xanh",
                    m == 2 ? GiaoDien.DO : m == 1 ? GiaoDien.VANG_DAM : GiaoDien.XANH_LA));
        }
        for (int i = 0; i < 3; i++) {
            mhTop[i].setRowCount(0);
            final List<Object[]> l = (List<Object[]>) kq[i + 1];
            if (ThaoTacMayChu.loiTruyVan(l) == null) {
                for (int j = 1; j < l.size(); j++) {
                    final Object[] d = l.get(j);
                    mhTop[i].addRow(new Object[]{(long) j, d[0], so(d[1]), so(d[2]), TrangSuKien.gio(d[3])});
                }
            }
        }
        GiaoDien.toCay(this);
        revalidate();
        repaint();
    }

    static String doi(final long nay, final long truoc) {
        return doi(nay, truoc, "so với hôm trước");
    }

    static String doi(final long nay, final long truoc, final String soVoi) {
        if (truoc == 0) {
            return "";
        }
        final double p = (nay - truoc) * 100.0 / truoc;
        return (p >= 0 ? "tăng " : "giảm ") + String.format("%.1f%%", Math.abs(p)) + " " + soVoi;
    }
}
