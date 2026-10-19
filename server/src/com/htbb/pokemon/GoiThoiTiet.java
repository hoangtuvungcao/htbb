package com.htbb.pokemon;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * Goi -118 THOI_TIET (S-&gt;C, CHI client ban &gt;= {@link #BAN_CLIENT}): trang thai thoi tiet / phu mau mua / ngay cong
 * dong cua ban do nguoi choi dang dung. Lop THUAN (test/KiemThoiTiet ghi roi doc lai). Client: game.pokemon.ThoiTiet.
 *
 * <pre>
 *   u8  phienBan = 1
 *   i8  thoiTiet        (-1 = trong nha / hang: khong thoi tiet; 0 quang .. 6 gio manh)
 *   UTF ten             ("Mưa")
 *   UTF goiY            ("Mưa: chiêu hệ Nước +10%, chiêu hệ Lửa −10%. Pokémon hệ Nước, Điện xuất hiện nhiều.")
 *   i32 giayConLai      (toi khi doi khung)
 *   i8  tiepTheo        (thoi tiet khung sau, -1 = khong)
 *   i8  mauMua          (ThoiTietLich.MAU_*; 0 = khong phu)
 *   UTF tenMauMua
 *   u8  congDong        (0 khong; 1 hom nay la ngay cong dong, ngoai gio vang; 2 dang gio vang)
 *   UTF tenDong         ("Torchic")
 *   i32 giayCongDong    (toi luc gio vang mo / dong; 0)
 *   u8  coTrenMap       (1 = ban do nay co loai noi bat)
 *   UTF goiYCongDong
 * </pre>
 * Client cu khong biet -118 (khong gui cho ho; ho van nhan -39 nhu cu). Them truong moi: noi DUOI, tang phienBan.
 */
public final class GoiThoiTiet {

    public static final int LENH = -118;
    /** Ban client dau tien biet -118 (0.9.8; 0.9.7 = 907 da phat hanh khong co -118). Da grep server + client moi nhanh (19/10/2026): -118 trong. */
    public static final int BAN_CLIENT = 908;
    public static final int PHIEN_BAN = 1;

    private GoiThoiTiet() {
    }

    /** Noi dung mot goi. */
    public static final class TrangThai {
        public int thoiTiet = -1;
        public String ten = "";
        public String goiY = "";
        public int giayConLai;
        public int tiepTheo = -1;
        public int mauMua;
        public String tenMauMua = "";
        public int congDong;
        public String tenDong = "";
        public int giayCongDong;
        public boolean coTrenMap;
        public String goiYCongDong = "";

        /** Khoa so sanh: chi gui lai khi doi (giay con lai khong tinh). */
        public String khoa() {
            return thoiTiet + "|" + tiepTheo + "|" + mauMua + "|" + congDong + "|" + tenDong + "|" + coTrenMap + "|" + goiY;
        }
    }

    public static void ghi(final DataOutputStream o, final TrangThai t) throws IOException {
        o.writeByte(PHIEN_BAN);
        o.writeByte(t.thoiTiet);
        o.writeUTF(t.ten == null ? "" : t.ten);
        o.writeUTF(t.goiY == null ? "" : t.goiY);
        o.writeInt(Math.max(0, t.giayConLai));
        o.writeByte(t.tiepTheo);
        o.writeByte(t.mauMua);
        o.writeUTF(t.tenMauMua == null ? "" : t.tenMauMua);
        o.writeByte(t.congDong);
        o.writeUTF(t.tenDong == null ? "" : t.tenDong);
        o.writeInt(Math.max(0, t.giayCongDong));
        o.writeByte(t.coTrenMap ? 1 : 0);
        o.writeUTF(t.goiYCongDong == null ? "" : t.goiYCongDong);
    }

    /** Doc (test; client co ban rieng J2ME). */
    public static TrangThai doc(final DataInputStream i) throws IOException {
        final TrangThai t = new TrangThai();
        if (i.readUnsignedByte() < 1) {
            return t;
        }
        t.thoiTiet = i.readByte();
        t.ten = i.readUTF();
        t.goiY = i.readUTF();
        t.giayConLai = i.readInt();
        t.tiepTheo = i.readByte();
        t.mauMua = i.readByte();
        t.tenMauMua = i.readUTF();
        t.congDong = i.readUnsignedByte();
        t.tenDong = i.readUTF();
        t.giayCongDong = i.readInt();
        t.coTrenMap = i.readUnsignedByte() == 1;
        t.goiYCongDong = i.readUTF();
        return t;
    }
}
