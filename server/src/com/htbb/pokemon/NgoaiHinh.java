package com.htbb.pokemon;

import java.io.DataOutputStream;
import java.io.IOException;

/**
 * NGOAI HINH TRANG BI: trang bi dang mac -> phu kien VE TREN than Pokemon (mu/no o dinh dau, khan va vong
 * o co, do cam o tay, ruy bang / ao choang sau than, da he bay quanh than, kinh) + vang sang theo bac,
 * hat lap lanh theo cap cuong hoa, vong bo trang bi, trang phuc doi mau. Chi la HINH: khong doi chi so.
 * Thiet ke: docs/thiet-ke-game.md.
 *
 * Lop THUAN (chi tinh + ghi byte), kiem thu o test/KiemNgoaiHinh.java. Doc trang bi tu Char va gui goi
 * nam o real.NgoaiHinhGame.
 *
 * <h3>Goi -102 POKE_NGOAI_HINH (S-&gt;C), di kem chuoi -90 -&gt; -93 -&gt; -94 -&gt; -102</h3>
 * Nho di kem -90 nen moi cho da gui thong tin nhan vat (vao tam nhin, mac/thao do, len/xuong thu, goi 3
 * cua chinh minh) tu mang theo ngoai hinh - khong them cho goi nao. Client cu khong biet lenh -102 thi bo
 * qua (MessageHandler khong co case), nen tuong thich nguoc.
 * <pre>
 *   i16 charId, u8 phienBan = 1
 *   u8 coO           bit o = 1 neu o do co phu kien (o: MU 0, KHAN 1, VONG 2, TAY 3, LUNG 4, DA 5, KINH 6)
 *   lap moi o co bit (tang dan): u8 kieu, u8 (hiem &lt;&lt; 4 | bac), u8 cong
 *   u8 bacTot        bac cao nhat dang mac (0..10), 255 = khong mac gi
 *   u8 congTot       cap cuong hoa cao nhat (0..15)
 *   u8 boCungBac     so mon cung bac nhieu nhat (bo trang bi: 4 / 7 / 11)
 *   u8 trangPhuc     bang mau trang phuc (0 = khong)
 *   u8 an            bit o an + bit 7 = an trang phuc (de bang Cai dat cua chinh minh hien dung)
 * </pre>
 * Phien ban sau chi THEM truong o cuoi; client doc phien ban 1 bo qua phan thua.
 * <pre>
 *   0.9.8 DUOI TRANG TRI (chi goi -102, KHONG co trong khoi goi 13):
 *   u8 'T' (0x54), u8 phienBanDuoi = 1, u8 trangPhuc, u8 haoQuang, u8 skinThu, u8 theDongHanh
 *   (so hieu data_trang_phuc.so trong tung o, 0 = khong; an trang phuc -&gt; ca bon = 0). Client &lt; 908 bo qua.
 * </pre>
 *
 * <h3>Khoi ngoai hinh cuoi goi 13 (danh sach nhan vat, man chon Pokemon)</h3>
 * Sau byte DataGame.version cuoi goi 13 (client cu khong doc toi day nen tuong thich nguoc):
 * u8 'N', u8 phienBan = 1, u8 n, lap n: i32 idNhanVat, i16 dex, u8 shiny, u8 giaiDoan (1..3), roi phan sau
 * charId cua -102 ({@link #ghiNoiDung}). Doc trang bi tu cot equip (place = 1, dung o dang mac).
 *
 * <h3>Goi -103 NGOAI_HINH_CAI_DAT (C-&gt;S)</h3>
 * u8 loai = 0, u8 an (bit o an + bit 7 an trang phuc). Server luu (HoSoLauDai "nh"), gui lai -102.
 */
public final class NgoaiHinh {

    public static final int LENH = -102;
    public static final int LENH_CAI_DAT = -103;
    public static final int PHIEN_BAN = 1;

    public static final int MU = 0;
    public static final int KHAN = 1;
    public static final int VONG = 2;
    public static final int TAY = 3;
    public static final int LUNG = 4;
    public static final int DA = 5;
    public static final int KINH = 6;
    public static final int SO_O = 7;
    /** Bit "an trang phuc" trong mat na an. */
    public static final int AN_TRANG_PHUC = 0x80;

    /** Ten o (cho bang Cai dat / the trang bi). */
    public static final String[] TEN_O = {"Mũ / nơ", "Khăn quàng", "Vòng cổ", "Đồ cầm tay", "Ruy băng / áo choàng",
        "Phiến đá hệ", "Kính"};

    /**
     * Do hiem hien thi (hang bang mau phu kien gui trong -102) = bac chung {@link DoHiemSinh} (MOT nguon
     * tools/cauhinh/do_hiem.json): 0 Thuong, 1 Hiem, 2 Quy, 3 Su thi, 4 Huyen thoai (lap lanh), 5 Trang tri.
     * Client cu (bang 5 hang trong phukien.bin) van ve gan nhu cu; hang 5 ngoai bang -> Thuong.
     */
    public static final String[] TEN_HIEM = DoHiemSinh.TEN;

    /** Ten bac (docs/thiet-ke-game.md 3.1). */
    public static final String[] TEN_BAC = DoHiemSinh.TEN_BAC_TRANG_BI;

    /** So trang phuc (bang mau) client co: id 1..SO_TRANG_PHUC. */
    public static final int SO_TRANG_PHUC = 8;

    private NgoaiHinh() {
    }

    /**
     * colorName cua Item -> do hiem hien thi = bac chung (DoHiemSinh.bacVatPham: Trang 0 -> Thuong, Xanh 3 -> Hiem,
     * Do 2 -> Quy, Hoan my 1 / Tim 4 -> Su thi, 6 -> Huyen thoai, Vang 5 -> Trang tri).
     */
    public static int hangHiem(final int colorName) {
        return DoHiemSinh.bacVatPham(colorName);
    }

    /** Bac theo cap yeu cau cua mon (bac k mo o cap 10k, bac 10 o cap 98). */
    public static int bacTheoCap(final int cap) {
        if (cap >= 98) {
            return 10;
        }
        if (cap <= 0) {
            return 0;
        }
        return Math.min(9, cap / 10);
    }

    /**
     * type cua ItemTemplates -> o phu kien, -1 neu mon do khong ve len than (dai, gang, giay, huy hieu chi
     * tinh vao vang sang / bo trang bi).
     */
    public static int oTheoLoai(final int type) {
        switch (type) {
            case 2:
                return MU;          // bang do (non)
            case 0:
                return KHAN;        // khan quang (ao)
            case 9:
                return VONG;        // vong co (day chuyen)
            case 3:
            case 4:
            case 5:
            case 6:
            case 7:
                return TAY;         // do cam tay theo dong
            case 19:
                return LUNG;        // ruy bang (phi phong)
            case 12:
                return DA;          // phien da he (ngoc boi)
            default:
                return -1;
        }
    }

    /** Mon co tinh vao bac / bo trang bi khong (11 o trang bi nhan vat: type 0..12 va ruy bang 19). */
    public static boolean laTrangBi(final int type) {
        return (type >= 0 && type <= 12) || type == 19;
    }

    /**
     * Kieu phu kien cua o theo bac (bac cang cao kieu cang "xin"). Do cam tay theo dong (type 3..7 -> 1..5).
     * Gia tri phai khop bang hinh trong phukien.bin (tools/pokemon/SinhPhuKien.java).
     */
    public static int kieu(final int o, final int type, final int bac) {
        switch (o) {
            case MU:
                return bac <= 1 ? 1 : bac <= 3 ? 2 : bac <= 5 ? 3 : bac <= 7 ? 4 : 5;
            case KHAN:
                return bac <= 4 ? 1 : 2;
            case VONG:
                return bac <= 4 ? 1 : 2;
            case TAY:
                return type >= 3 && type <= 7 ? type - 2 : 1;
            case LUNG:
                return bac <= 4 ? 1 : 2;
            case DA:
                return 1;
            case KINH:
                return 1;
            default:
                return 0;
        }
    }

    /** Kieu phu kien thoi trang (chi hinh): mu thoi trang, canh, kinh/mat na. */
    public static final int KIEU_MU_THOI_TRANG = 6;
    public static final int KIEU_CANH = 3;

    /** Ket qua tinh ngoai hinh cua mot nhan vat. */
    public static final class Du {
        public final int[] kieu = new int[SO_O];
        public final int[] hiem = new int[SO_O];
        public final int[] bac = new int[SO_O];
        public final int[] cong = new int[SO_O];
        public int bacTot = -1;
        public int congTot;
        public int boCungBac;
        public int trangPhuc;
        public int an;
        /** 0.9.8 tu do trang tri (so hieu trong o, 0 = khong): trang phuc, hao quang, skin thu cuoi, the ban dong hanh. */
        public int tpSo;
        public int hqSo;
        public int skinSo;
        public int theSo;

        public int coO() {
            int m = 0;
            for (int o = 0; o < SO_O; o++) {
                if (kieu[o] > 0) {
                    m |= 1 << o;
                }
            }
            return m;
        }

        /** Khoa so sanh (goi lai -102 chi khi doi). */
        public long khoa() {
            long h = 1469598103934665603L;
            for (int o = 0; o < SO_O; o++) {
                h = (h ^ (kieu[o] | hiem[o] << 8 | bac[o] << 12 | cong[o] << 16)) * 1099511628211L;
            }
            h = (h ^ (bacTot & 0xFF | congTot << 8 | boCungBac << 16 | (long) trangPhuc << 24 | (long) an << 32))
                    * 1099511628211L;
            h = (h ^ (tpSo & 0xFF | (hqSo & 0xFF) << 8 | (skinSo & 0xFF) << 16 | (long) (theSo & 0xFF) << 24)) * 1099511628211L;
            return h;
        }
    }

    /**
     * Tinh ngoai hinh tu trang bi dang mac (mang song song: type, cap mon, colorName, plus) va do thoi trang.
     *
     * @param thoiTrang  id template bo thoi trang (itemModel), 0 = khong
     * @param muThoiTrang id template mu thoi trang (hatModel), 0 = khong
     * @param matNa      id template mat na (kinh), 0 = khong
     * @param canh       id template canh thoi trang, 0 = khong
     * @param an         mat na an cua nguoi choi (bit o + AN_TRANG_PHUC)
     */
    public static Du tinh(final int[] type, final int[] cap, final int[] mau, final int[] plus,
            final int thoiTrang, final int muThoiTrang, final int matNa, final int canh, final int an) {
        final Du d = new Du();
        d.an = an & (0x7F | AN_TRANG_PHUC);
        final int[] demBac = new int[11];
        final int n = type == null ? 0 : type.length;
        for (int i = 0; i < n; i++) {
            if (!laTrangBi(type[i])) {
                continue;
            }
            final int b = bacTheoCap(cap[i]);
            final int c = Math.max(0, Math.min(15, plus[i]));
            final int h = hangHiem(mau[i]);
            demBac[b]++;
            d.bacTot = Math.max(d.bacTot, b);
            d.congTot = Math.max(d.congTot, c);
            final int o = oTheoLoai(type[i]);
            if (o < 0) {
                continue;
            }
            // hai mon cung o (khong xay ra voi trang bi nhan vat, phong khi): giu mon "xin" hon
            if (d.kieu[o] > 0 && d.bac[o] * 16 + d.hiem[o] > b * 16 + h) {
                continue;
            }
            d.kieu[o] = kieu(o, type[i], b);
            d.hiem[o] = h;
            d.bac[o] = b;
            d.cong[o] = c;
        }
        for (int b = 0; b < demBac.length; b++) {
            d.boCungBac = Math.max(d.boCungBac, demBac[b]);
        }
        // Do thoi trang: CHI doi hinh (kieu phu kien / bang mau), giu do hiem + bac cua mon that o do
        if (muThoiTrang > 0) {
            if (d.kieu[MU] == 0) {
                d.hiem[MU] = 1;
            }
            d.kieu[MU] = KIEU_MU_THOI_TRANG;
        }
        if (canh > 0) {
            if (d.kieu[LUNG] == 0) {
                d.hiem[LUNG] = DoHiemSinh.SU_THI; // canh thoi trang khong co mon that: tim nhu ban cu
            }
            d.kieu[LUNG] = KIEU_CANH;
        }
        if (matNa > 0) {
            d.kieu[KINH] = 1 + (matNa & 1);
            d.hiem[KINH] = 0;
        }
        if (thoiTrang > 0) {
            d.trangPhuc = trangPhuc(thoiTrang);
        }
        // An theo y nguoi choi (chi an HINH; vang sang/bo van tinh theo do that)
        for (int o = 0; o < SO_O; o++) {
            if ((an & (1 << o)) != 0) {
                d.kieu[o] = 0;
                d.hiem[o] = d.bac[o] = d.cong[o] = 0;
            }
        }
        if ((an & AN_TRANG_PHUC) != 0) {
            d.trangPhuc = 0;
            if (d.kieu[MU] == KIEU_MU_THOI_TRANG) {
                d.kieu[MU] = 0;
            }
            if (d.kieu[LUNG] == KIEU_CANH) {
                d.kieu[LUNG] = 0;
            }
            d.kieu[KINH] = 0;
        }
        return d;
    }

    /**
     * Template thoi trang -&gt; bang mau trang phuc 1..SO_TRANG_PHUC. Trang phuc theo mua (noi_dung_trang_tri.csv:
     * Pikachu, Togepi, Ho-Oh, Lugia, Latias, Rayquaza) dung bang co ten rieng; con lai chia deu theo id.
     */
    public static int trangPhuc(final int idTemplate) {
        if (idTemplate <= 0) {
            return 0;
        }
        return 1 + idTemplate % SO_TRANG_PHUC;
    }

    /**
     * Trang bi dang mac tu chuoi cot tob_char.equip (mon cach nhau '>', moi mon "clazz,tpl,place,durable,mDurable,
     * plus,level,nhadSock,nSocAdd,magic_physic,minuteExist,colorName,...,slotWearing(17),..." | thuoc tinh).
     * Tra ve {type[], cap[], mau[], plus[]} (type tra qua loaiCua). Loi dong nao bo dong do.
     */
    public static int[][] tuCotEquip(final String equip, final int slot, final java.util.function.IntUnaryOperator loaiCua) {
        final java.util.List<int[]> ds = new java.util.ArrayList<int[]>();
        if (equip != null) {
            for (final String mon : equip.split(">")) {
                try {
                    final int g = mon.indexOf('|');
                    final String[] f = (g >= 0 ? mon.substring(0, g) : mon).split(",");
                    if (f.length < 18 || Integer.parseInt(f[2].trim()) != 1) {
                        continue;
                    }
                    if (Integer.parseInt(f[17].trim()) != slot) {
                        continue;
                    }
                    final int tpl = Integer.parseInt(f[1].trim());
                    ds.add(new int[]{loaiCua.applyAsInt(tpl), Integer.parseInt(f[6].trim()), Integer.parseInt(f[11].trim()),
                        Integer.parseInt(f[5].trim())});
                } catch (final Exception e) {
                    // mon hong: bo
                }
            }
        }
        final int[][] kq = new int[4][ds.size()];
        for (int i = 0; i < ds.size(); i++) {
            for (int j = 0; j < 4; j++) {
                kq[j][i] = ds.get(i)[j];
            }
        }
        return kq;
    }

    public static final int DUOI_TRANG_TRI = 0x54;
    public static final int PHIEN_BAN_DUOI = 1;

    /** Ghi noi dung goi -102 (+ duoi trang tri 0.9.8). */
    public static void ghi(final DataOutputStream dos, final int charId, final Du d) throws IOException {
        dos.writeShort(charId);
        ghiNoiDung(dos, d);
        ghiDuoi(dos, d);
    }

    /** Duoi trang tri cua -102 (an trang phuc -&gt; 0 het). */
    public static void ghiDuoi(final DataOutputStream dos, final Du d) throws IOException {
        final boolean an = (d.an & AN_TRANG_PHUC) != 0;
        dos.writeByte(DUOI_TRANG_TRI);
        dos.writeByte(PHIEN_BAN_DUOI);
        dos.writeByte(an ? 0 : d.tpSo & 0xFF);
        dos.writeByte(an ? 0 : d.hqSo & 0xFF);
        dos.writeByte(an ? 0 : d.skinSo & 0xFF);
        dos.writeByte(an ? 0 : d.theSo & 0xFF);
    }

    /** Phan sau charId cua -102 (dung chung cho khoi ngoai hinh cua goi 13 danh sach nhan vat). */
    public static void ghiNoiDung(final DataOutputStream dos, final Du d) throws IOException {
        dos.writeByte(PHIEN_BAN);
        final int co = d.coO();
        dos.writeByte(co);
        for (int o = 0; o < SO_O; o++) {
            if ((co & (1 << o)) == 0) {
                continue;
            }
            dos.writeByte(d.kieu[o]);
            dos.writeByte((d.hiem[o] & 0x0F) << 4 | (d.bac[o] & 0x0F));
            dos.writeByte(d.cong[o]);
        }
        dos.writeByte(d.bacTot < 0 ? 255 : d.bacTot);
        dos.writeByte(d.congTot);
        dos.writeByte(d.boCungBac);
        dos.writeByte(d.trangPhuc);
        dos.writeByte(d.an);
    }
}
