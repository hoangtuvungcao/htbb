package com.htbb.dangnhap;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

/**
 * Cau hinh login server - doc tu CUNG tep server.ini ban cu dung (thu muc lam viec: server/loginServer/server.ini).
 * Bon khoa cu bat buoc: port, db.url, db.user, db.password. Moi khoa login.* deu tuy chon (thieu -> mac dinh,
 * so ngoai khoang -> kep). Bang day du: docs/dang-nhap.md muc 5.
 */
public final class CauHinh {

    public int cong;
    public String diaChi = "";
    public String dbUrl;
    public String dbUser;
    /** KHONG bao gio in ra log. */
    public String dbMatKhau;

    public int csdlToiDa = 8;
    public int csdlChoMuonMs = 3000;
    public int csdlTruyVanGiay = 5;
    public int csdlKetNoiMs = 3000;
    public int csdlSocketMs = 6000;
    public int csdlKiemSauRanhGiay = 30;
    public int csdlTuoiToiDaPhut = 30;

    public int luong = 8;
    public int hangDoiToiDa = 2000;
    /**
     * Han xu ly MOT yeu cau tinh tu luc doc xong khung (cho hang + muon ket noi + truy van). Phai nho hon han cho cua
     * game (io.LogInController.CHO_LOGIN_MS = 15 s) de moi tra loi ve kip: qua han -> -3 ngay, khong de game bao
     * "Máy chủ đăng nhập chưa phản hồi" roi bo tra loi muon.
     */
    public int hanXuLyMs = 13000;

    public int ketNoiToiDa = 64;
    public int ketNoiMoiIp = 16;
    public List<String> ipChoPhep = Collections.emptyList();
    public int yeuCauMoiGiayMoiIp = 0;

    public int khungToiDa = 4096;
    public int nhipGiay = 60;
    public boolean logMoiLan = true;
    public int dungChoMs = 10000;

    /** Loi cau hinh bat buoc (thieu khoa) - rong neu du. */
    public final List<String> loi = new ArrayList<>();

    public static CauHinh doc(final Properties p) {
        final CauHinh c = new CauHinh();
        final String port = trim(p.getProperty("port"));
        if (port == null) {
            c.loi.add("thieu khoa 'port'");
        } else {
            try {
                c.cong = Integer.parseInt(port);
                if (c.cong < 0 || c.cong > 65535) {
                    c.loi.add("port ngoai khoang 0..65535: " + port);
                }
            } catch (final NumberFormatException e) {
                c.loi.add("port khong phai so: " + port);
            }
        }
        c.dbUrl = trim(p.getProperty("db.url"));
        c.dbUser = trim(p.getProperty("db.user"));
        c.dbMatKhau = p.getProperty("db.password"); // giu nguyen (mat khau co the co dau cach)
        if (c.dbUrl == null) {
            c.loi.add("thieu khoa 'db.url'");
        }
        if (c.dbUser == null) {
            c.loi.add("thieu khoa 'db.user'");
        }
        if (c.dbMatKhau == null) {
            c.loi.add("thieu khoa 'db.password'");
        }
        final String dc = trim(p.getProperty("login.diaChi"));
        c.diaChi = dc == null ? "" : dc;

        c.csdlToiDa = so(p, "login.csdl.toiDa", c.csdlToiDa, 1, 64);
        c.csdlChoMuonMs = so(p, "login.csdl.choMuonMs", c.csdlChoMuonMs, 100, 60000);
        c.csdlTruyVanGiay = so(p, "login.csdl.truyVanGiay", c.csdlTruyVanGiay, 1, 60);
        c.csdlKetNoiMs = so(p, "login.csdl.ketNoiMs", c.csdlKetNoiMs, 500, 60000);
        c.csdlSocketMs = so(p, "login.csdl.socketMs", c.csdlSocketMs, 1000, 120000);
        c.csdlKiemSauRanhGiay = so(p, "login.csdl.kiemSauRanhGiay", c.csdlKiemSauRanhGiay, 0, 3600);
        c.csdlTuoiToiDaPhut = so(p, "login.csdl.tuoiToiDaPhut", c.csdlTuoiToiDaPhut, 1, 1440);

        c.luong = so(p, "login.luong", c.csdlToiDa, 1, 64);
        c.hangDoiToiDa = so(p, "login.hangDoi.toiDa", c.hangDoiToiDa, 1, 100000);
        c.hanXuLyMs = so(p, "login.hanXuLyMs", c.hanXuLyMs, 500, 600000);

        c.ketNoiToiDa = so(p, "login.ketNoi.toiDa", c.ketNoiToiDa, 1, 10000);
        c.ketNoiMoiIp = so(p, "login.ketNoi.moiIp", c.ketNoiMoiIp, 1, 10000);
        final String ds = trim(p.getProperty("login.ipChoPhep"));
        if (ds != null) {
            final List<String> l = new ArrayList<>();
            for (final String s : ds.split("[,\\s]+")) {
                if (!s.isEmpty()) {
                    l.add(s);
                }
            }
            c.ipChoPhep = Collections.unmodifiableList(l);
        }
        c.yeuCauMoiGiayMoiIp = so(p, "login.yeuCauMoiGiayMoiIp", c.yeuCauMoiGiayMoiIp, 0, 1000000);

        c.khungToiDa = so(p, "login.khungToiDa", c.khungToiDa, 64, 65535);
        c.nhipGiay = so(p, "login.nhipGiay", c.nhipGiay, 0, 3600);
        final String moi = trim(p.getProperty("login.logMoiLan"));
        c.logMoiLan = moi == null || !("0".equals(moi) || "false".equalsIgnoreCase(moi) || "tat".equalsIgnoreCase(moi));
        c.dungChoMs = so(p, "login.dungChoMs", c.dungChoMs, 0, 120000);
        return c;
    }

    static String trim(final String s) {
        if (s == null) {
            return null;
        }
        final String t = s.trim();
        return t.isEmpty() ? null : t;
    }

    static int so(final Properties p, final String khoa, final int macDinh, final int min, final int max) {
        final String s = trim(p.getProperty(khoa));
        if (s == null) {
            return macDinh;
        }
        try {
            final long v = Long.parseLong(s);
            return (int) Math.max(min, Math.min(max, v));
        } catch (final NumberFormatException e) {
            return macDinh;
        }
    }

    /** Tom tat cho dong log khoi dong - KHONG co mat khau. */
    public String tomTat() {
        return "cong=" + this.cong + (this.diaChi.isEmpty() ? "" : " diaChi=" + this.diaChi)
                + " csdl=" + anMatKhauUrl(this.dbUrl) + " user=" + this.dbUser
                + " csdl.toiDa=" + this.csdlToiDa + " luong=" + this.luong + " hangDoi=" + this.hangDoiToiDa
                + " hanXuLy=" + this.hanXuLyMs + "ms ketNoi=" + this.ketNoiToiDa + "/ip " + this.ketNoiMoiIp
                + (this.ipChoPhep.isEmpty() ? "" : " ipChoPhep=" + this.ipChoPhep)
                + (this.yeuCauMoiGiayMoiIp > 0 ? " yeuCau/s/ip=" + this.yeuCauMoiGiayMoiIp : "");
    }

    /** Bo password=... neu ai do nhet mat khau vao db.url. */
    static String anMatKhauUrl(final String url) {
        if (url == null) {
            return "?";
        }
        return url.replaceAll("(?i)(password=)[^&]*", "$1***");
    }
}
