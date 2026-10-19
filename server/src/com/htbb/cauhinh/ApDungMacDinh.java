package com.htbb.cauhinh;

import java.util.Properties;
import java.util.Set;

/**
 * Nguoi nghe co san: noi khoa cau hinh voi bien tinh cua cac tinh nang da co (ma cu doc server.ini MOT lan
 * luc khoi dong). Moi ham chi chay khi khoa cua no doi (lan nap dau sau khoi dong: chay het mot lan).
 *
 * Khoa doc ngay moi lan dung (CongTac, ThanhVien, LauDai.so...) khong can o day.
 */
final class ApDungMacDinh {

    private static boolean daDangKy = false;

    private ApDungMacDinh() {
    }

    static synchronized void dangKy() {
        if (daDangKy) {
            return;
        }
        daDangKy = true;

        // 1. Toan bo khoa cua server.CauHinh cu (ti le roi, EXP, van tieu, chiem thanh, su kien, GM, thanh vien,
        //    de so lieu dai han...) - ham cu nhan Properties day du, re, chay moi khi co gi doi.
        CauHinhTrungTam.dangKyNghe(null, new NgheThayDoi() {
            public void khiDoi(final Set<String> k, final CauHinhTrungTam.AnhChup m) {
                server.CauHinh.apDung(m.thanhProperties());
            }
        }, "server.CauHinh.apDung");

        CauHinhTrungTam.dangKyNghe(new String[]{"sv.dangKy*"}, new NgheThayDoi() {
            public void khiDoi(final Set<String> k, final CauHinhTrungTam.AnhChup m) {
                real.cmd.DangKyTrongGame.docCauHinh(m.chuoi("sv.dangKyTrongGame"), m.chuoi("sv.dangKyTuKichHoat"),
                        m.chuoi("sv.dangKyCsdlTaiKhoan"), m.chuoi("sv.dangKyTreMs"));
            }
        }, "DangKyTrongGame");

        CauHinhTrungTam.dangKyNghe(new String[]{"sv.spam."}, new NgheThayDoi() {
            public void khiDoi(final Set<String> k, final CauHinhTrungTam.AnhChup m) {
                com.htbb.chongspam.ChongSpam.docCauHinh(m.thanhProperties());
                System.out.println("[CAU HINH] " + com.htbb.chongspam.ChongSpam.moTa());
            }
        }, "ChongSpam");

        CauHinhTrungTam.dangKyNghe(new String[]{"sv.thongBao."}, new NgheThayDoi() {
            public void khiDoi(final Set<String> k, final CauHinhTrungTam.AnhChup m) {
                com.htbb.chongspam.ThongBaoGop.docCauHinh(m.thanhProperties());
            }
        }, "ThongBaoGop");

        CauHinhTrungTam.dangKyNghe(new String[]{"sv.log.muc", "sv.log.mucTep", "sv.log.tat", "sv.log.kiemToan"}, new NgheThayDoi() {
            public void khiDoi(final Set<String> k, final CauHinhTrungTam.AnhChup m) {
                final Properties p = m.thanhProperties();
                com.htbb.log.Nhat.docCauHinh(p, p.getProperty("sv.pathLog"));
            }
        }, "Nhat");

        CauHinhTrungTam.dangKyNghe(new String[]{"sv.socketTimeout", "sv.maxConnPerIp", "sv.connRateMax", "sv.connRateWindow",
            "sv.dosBanSec", "sv.reloginWaitSec", "sv.tranGoiThuong", "sv.tranGoiTaiNguyen", "sv.tranGoiTraCuu",
            "sv.gmCanCoPInfo", "sv.banToiThieu", "sv.banMoiNhat", "sv.nhacCapNhat", "sv.giaXuBoHoaDoVuLan"}, new NgheThayDoi() {
            public void khiDoi(final Set<String> k, final CauHinhTrungTam.AnhChup m) {
                server.TeamServer.SOCKET_TIMEOUT_MS = (int) so(m, "sv.socketTimeout", 180) * 1000;
                server.TeamServer.MAX_CONN_PER_IP = (int) so(m, "sv.maxConnPerIp", 50);
                server.TeamServer.CONN_RATE_MAX = (int) so(m, "sv.connRateMax", 60);
                server.TeamServer.CONN_RATE_WINDOW_MS = (int) so(m, "sv.connRateWindow", 30) * 1000;
                server.TeamServer.DOS_BAN_MS = so(m, "sv.dosBanSec", 300) * 1000L;
                server.TeamServer.RELOGIN_WAIT_MS = so(m, "sv.reloginWaitSec", 0) * 1000L;
                io.BoDemGoiTin.GOI_THUONG_TOI_DA = (int) so(m, "sv.tranGoiThuong", io.BoDemGoiTin.GOI_THUONG_MAC_DINH);
                io.BoDemGoiTin.GOI_TAI_NGUYEN_TOI_DA = (int) so(m, "sv.tranGoiTaiNguyen", io.BoDemGoiTin.GOI_TAI_NGUYEN_MAC_DINH);
                io.BoDemGoiTin.GOI_TRA_CUU_TOI_DA = (int) so(m, "sv.tranGoiTraCuu", io.BoDemGoiTin.GOI_TRA_CUU_MAC_DINH);
                server.TeamServer.gmCanCoPInfo = so(m, "sv.gmCanCoPInfo", 0) == 1;
                server.TeamServer.banToiThieu = (int) so(m, "sv.banToiThieu", 0);
                server.TeamServer.banMoiNhat = (int) so(m, "sv.banMoiNhat", 0);
                server.TeamServer.nhacCapNhat = so(m, "sv.nhacCapNhat", 1) != 0;
                server.TeamServer.giaXuBoHoaDoVuLan = (int) so(m, "sv.giaXuBoHoaDoVuLan", 150000);
            }
        }, "TeamServer (DoS / goi tin / GM)");

        CauHinhTrungTam.dangKyNghe(new String[]{"sv.proxyTinCay", "sv.dangNhapIpNguong", "sv.dangNhapIpChoMs"}, new NgheThayDoi() {
            public void khiDoi(final Set<String> k, final CauHinhTrungTam.AnhChup m) {
                final com.htbb.mang.ProxyTinCay p = com.htbb.mang.ProxyTinCay.dat(m.chuoi("sv.proxyTinCay"));
                com.htbb.mang.GioiHanIp.datDangNhap(m.chuoi("sv.dangNhapIpNguong"), m.chuoi("sv.dangNhapIpChoMs"));
                System.out.println("[CAU HINH] proxy tin cay = " + p + ", dang nhap/IP nguong="
                        + com.htbb.mang.GioiHanIp.dangNhapNguong + " cho=" + com.htbb.mang.GioiHanIp.dangNhapChoMs + "ms");
            }
        }, "ProxyTinCay / GioiHanIp");

        CauHinhTrungTam.dangKyNghe(new String[]{"sv.gioBaoTri"}, new NgheThayDoi() {
            public void khiDoi(final Set<String> k, final CauHinhTrungTam.AnhChup m) {
                real.BaoTriDaily.datGio(m.chuoi("sv.gioBaoTri"));
            }
        }, "BaoTriDaily");

        CauHinhTrungTam.dangKyNghe(new String[]{"bao_tri."}, new NgheThayDoi() {
            public void khiDoi(final Set<String> k, final CauHinhTrungTam.AnhChup m) {
                server.ChiSoMayChu.datBaoTriCauHinh("1".equals(m.chuoi("bao_tri.bat")), m.chuoi("bao_tri.cau"),
                        java.util.Arrays.asList(m.chuoi("bao_tri.cho").split(",")));
            }
        }, "ChiSoMayChu (bao tri)");
    }

    static long so(final CauHinhTrungTam.AnhChup m, final String k, final long macDinh) {
        try {
            return Long.parseLong(m.chuoi(k).trim());
        } catch (final NumberFormatException e) {
            return macDinh;
        }
    }
}
