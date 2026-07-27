package real;

import data.CharClan;
import data.Database;
import data.NewClan;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Vector;

/**
 * Thuong cho bang hoi chiem duoc thanh: moi thanh vien dang online nhan
 * 10.000 luong + 50.000.000 xu. Cua so nhan thuong keo dai 1 gio ke tu luc
 * chiem thanh thanh cong, nen thanh vien vao game muon trong 1 gio do van
 * duoc nhan (moi nhan vat chi nhan 1 lan).
 *
 * Du lieu luu trong bo nho: restart server trong 1 gio do se mat cua so nhan.
 */
public class ThuongChiemThanh {

    public static final int LUONG_THUONG = 10000;
    public static final long XU_THUONG = 50000000L;
    public static final long THOI_GIAN_NHAN = 3600000L; // 1 gio

    private static final short[] idClanThang = {-1, -1, -1};
    private static final long[] timeHetHan = {0L, 0L, 0L};
    private static final List<HashSet<Integer>> daNhan = new ArrayList<HashSet<Integer>>();
    // Danh sach thanh vien (chu thuong) tai dung thoi diem chiem thanh, de nguoi
    // moi vao bang sau khi chiem thanh khong an duoc thuong.
    private static final List<HashSet<String>> thanhVienLucChiem = new ArrayList<HashSet<String>>();

    static {
        for (int i = 0; i < 3; i++) {
            daNhan.add(new HashSet<Integer>());
            thanhVienLucChiem.add(new HashSet<String>());
        }
    }

    /**
     * Goi ngay khi mot bang chiem thanh thanh cong. Tra thuong lien cho thanh
     * vien dang online va mo cua so 1 gio cho nhung nguoi vao sau.
     */
    public static synchronized void batDau(int country, int idClan, String tenBang) {
        if (country < 0 || country > 2 || idClan < 0) {
            return;
        }
        idClanThang[country] = (short) idClan;
        timeHetHan[country] = System.currentTimeMillis() + THOI_GIAN_NHAN;
        daNhan.get(country).clear();
        HashSet<String> snapshot = thanhVienLucChiem.get(country);
        snapshot.clear();
        try {
            NewClan clan = NewClan.getClan((short) idClan);
            if (clan != null) {
                Vector<CharClan> mems = clan.getAllMember();
                for (int i = 0; i < mems.size(); i++) {
                    CharClan cc = mems.get(i);
                    if (cc != null && cc.charname != null) {
                        snapshot.add(cc.charname.toLowerCase());
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("THUONG CHIEM THANH: bang " + tenBang + " (id " + idClan + ") lanh tho " + country
                + ", " + snapshot.size() + " thanh vien, nhan thuong den " + Char.getDayTime(THOI_GIAN_NHAN));
        try {
            Database.instance.saveLogClan(tenBang, "thuongct",
                    "mo thuong chiem thanh 1h: " + LUONG_THUONG + " luong + " + XU_THUONG + " xu " + Char.getDayTime(0L));
        } catch (Exception e) {
            e.printStackTrace();
        }
        int soNguoi = 0;
        try {
            Vector<Char> all = CharManager.instance.vChars;
            for (int i = 0; i < all.size(); i++) {
                Char p;
                try {
                    p = all.elementAt(i);
                } catch (Exception e) {
                    continue;
                }
                if (traThuong(p, country)) {
                    soNguoi++;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("THUONG CHIEM THANH: da tra cho " + soNguoi + " thanh vien dang online");
    }

    /**
     * Goi khi nhan vat vao game. Neu bang cua nhan vat vua chiem thanh va con
     * trong cua so 1 gio thi tra thuong.
     */
    public static synchronized void kiemTraKhiVaoGame(Char p) {
        if (p == null || p.idClan == -1) {
            return;
        }
        for (int country = 0; country < 3; country++) {
            if (traThuong(p, country)) {
                return;
            }
        }
    }

    private static boolean traThuong(Char p, int country) {
        try {
            if (p == null || p.isBot != -1 || p.idClan == -1) {
                return false;
            }
            if (idClanThang[country] == -1 || p.idClan != idClanThang[country]) {
                return false;
            }
            if (System.currentTimeMillis() > timeHetHan[country]) {
                return false;
            }
            HashSet<String> snapshot = thanhVienLucChiem.get(country);
            if (!snapshot.isEmpty() && p.charname != null && !snapshot.contains(p.charname.toLowerCase())) {
                return false; // vao bang sau khi chiem thanh -> khong duoc thuong
            }
            if (!daNhan.get(country).add(p.charDBID)) {
                return false; // nhan vat nay da nhan
            }
            p.addLuong(LUONG_THUONG);
            p.addXu(XU_THUONG, "thuong chiem thanh");
            p.sendMessage(MessageCreator.createServerAlertMessage(
                    "Chúc mừng! Bang hội của bạn đã chiếm được thành. Bạn nhận được " + LUONG_THUONG
                            + " lượng và " + (XU_THUONG / 1000000L) + " triệu xu.", ""));
            p.sendMessage(MessageCreator.createCharInventoryMessage(p, 0));
            Database.instance.saveOrtherLog("", p.charname,
                    "nhan thuong chiem thanh " + LUONG_THUONG + " luong + " + XU_THUONG + " xu (clan "
                            + p.idClan + ", lanh tho " + country + ")",
                    "thuongchiemthanh");
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
