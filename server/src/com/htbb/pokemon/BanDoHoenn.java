package com.htbb.pokemon;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Vung Hoenn (noi dung nam 2): 29 ban do MOI (cMap/&lt;id&gt;, khong co
 * cMap/&lt;id&gt; goc). DAY LA NGUON DUY NHAT cua bo cuc khung (kich thuoc, cong, NPC client, quai):
 *  - tools/pokemon/VeMapMoi.java dung lop nay de dung "ban goc ao" roi ve nhu moi ban do khac;
 *  - real.VungHoenn (server) dung lop nay de dang ky Map, bang locationMap/nameMap/mapID/mapChange,
 *    danh sach gui client va kiem tra mo cong theo mua;
 *  - test/KiemMapPokemon kiem cong 2 chieu, diem den, quai.
 * Lop thuan du lieu (khong phu thuoc real.*) de tool/test bien dich doc lap.
 *
 * Cap id (bo tile chon theo id nap, DinhDangBanDo.boTile): 43..62 ngoai troi (t), 160..165 hang
 * (t_hang), 280..282 dau truong (t_thanh). Cac id nay khong co trong cMap goc, khong nam trong
 * offMapID cua client, khong roi vao khoang 200..271 (luat thanh/cong thanh) - xem docs/kien-truc.md.
 *
 * Loi vao duy nhat tu the gioi cu: cong bac cua map 9 (Cinnabar, cum cong toM=1 -> Cao nguyen Indigo /
 * Duong Victory) them lua chon thu 3 "Littleroot (Hoenn)". Server chan qua cong khi nhan vat duoi cap
 * {@link #CAP_VAO}, hoac (sv.lau_dai bat) chua xong ket nam 1 {@link #MA_KET_NAM_1} - hoac tran cap mua &lt; 91
 * khi sv.tran_cap_theo_mua=1 (real.VungHoenn.chanVao).
 */
public final class BanDoHoenn {

    /** Map cu co cong dan vao Hoenn (nhom locationMap = idArrMap cua map 9 = 9, muc toM 1, khe ban sao 2). */
    public static final int MAP_CONG_VAO = 9;
    public static final int TOM_CONG_VAO = 1;
    public static final int KHE_CONG_VAO = 2;
    /** Diem ve lai map 9 (giong duong Cao nguyen Indigo -> 9). */
    public static final int[] DIEM_VE_KANTO = {33, 1};
    /** Tran cap mua toi thieu de cong mo (nam 2) - chi khi sv.tran_cap_theo_mua=1. */
    public static final int TRAN_CAP_MO = 91;
    /** Cap nhan vat toi thieu de qua cong. */
    public static final int CAP_VAO = 84;
    /** Do rong cum o cong tren mep ban do. */
    public static final int RONG_CONG = 5;

    /** Mot cum cong tren mep: canh N/S/W/E, o dau tien doc canh, ban do dich. */
    public static final class Cong {
        public final char canh;
        public final int vt, den;

        Cong(final char canh, final int vt, final int den) {
            this.canh = canh;
            this.vt = vt;
            this.den = den;
        }
    }

    public static final class BanDo {
        public final int id, w, h;
        public final String loai, ten, moc;
        public final List<Cong> cong = new ArrayList<Cong>();
        /** {dx, dy, loai NPC} - lech so voi tam ban do. */
        public final List<int[]> npc = new ArrayList<int[]>();
        /** Mau quai (id data_monster 139..174), so con ~ w*h/matDo. */
        public int[] quai = new int[0];
        public int matDo = 75;
        public long hat;

        BanDo(final int id, final String loai, final String ten, final int w, final int h, final String moc) {
            this.id = id;
            this.loai = loai;
            this.ten = ten;
            this.w = w;
            this.h = h;
            this.moc = moc;
            this.hat = 2000 + id;
        }

        BanDo c(final char canh, final int vt, final int den) {
            cong.add(new Cong(canh, vt, den));
            return this;
        }

        BanDo n(final int dx, final int dy, final int loaiNpc) {
            npc.add(new int[]{dx, dy, loaiNpc});
            return this;
        }

        BanDo q(final int matDo, final int... mau) {
            this.matDo = matDo;
            this.quai = mau;
            return this;
        }

        /** Chi so toM cua cong dan toi {@code den} (-1 neu khong co). */
        public int toM(final int den) {
            for (int i = 0; i < cong.size(); i++) {
                if (cong.get(i).den == den) {
                    return i;
                }
            }
            return -1;
        }

        /** Cac o {x, y} cua cum cong thu i. */
        public List<int[]> oCong(final int i) {
            final Cong c = cong.get(i);
            final List<int[]> ra = new ArrayList<int[]>();
            for (int k = 0; k < RONG_CONG; k++) {
                switch (c.canh) {
                    case 'N':
                        ra.add(new int[]{c.vt + k, 0});
                        break;
                    case 'S':
                        ra.add(new int[]{c.vt + k, h - 1});
                        break;
                    case 'W':
                        ra.add(new int[]{0, c.vt + k});
                        break;
                    default:
                        ra.add(new int[]{w - 1, c.vt + k});
                        break;
                }
            }
            return ra;
        }

        /** O dung khi vao ban do nay qua cum cong thu i (3 o vao trong tu giua cum). */
        public int[] diemVao(final int i) {
            final Cong c = cong.get(i);
            final int g = c.vt + RONG_CONG / 2;
            switch (c.canh) {
                case 'N':
                    return new int[]{g, 3};
                case 'S':
                    return new int[]{g, h - 4};
                case 'W':
                    return new int[]{3, g};
                default:
                    return new int[]{w - 4, g};
            }
        }

        /** NPC client {x, y, loai} (toa do tuyet doi). */
        public List<int[]> npcTuyetDoi() {
            final List<int[]> ra = new ArrayList<int[]>();
            for (final int[] n : npc) {
                ra.add(new int[]{w / 2 + n[0], h / 2 + n[1], n[2]});
            }
            return ra;
        }

        public String boTile() {
            return DinhDangBanDo.boTile(id);
        }
    }

    private static final Map<Integer, BanDo> DS = new LinkedHashMap<Integer, BanDo>();

    private static BanDo them(final int id, final String loai, final String ten, final int w, final int h, final String moc) {
        final BanDo b = new BanDo(id, loai, ten, w, h, moc);
        DS.put(id, b);
        return b;
    }

    static {
        // ---- ngoai troi (bo t) ----------------------------------------------------------------
        them(43, "thi_tran", "Littleroot", 56, 46, "[bien] ben tau tu Kanto, cau go, thi tran dau tien")
                .c('S', 25, MAP_CONG_VAO).c('N', 25, 44)
                .n(-5, -1, 10).n(5, -1, 5).n(0, 3, 0);
        them(44, "rung", "Rừng Petalburg", 70, 64, "[ho] rung toi am, loi mon quanh co, ao nho")
                .c('S', 30, 43).c('N', 34, 45)
                .n(2, 0, 21)
                .q(70, 142, 143, 147);
        them(45, "thi_tran", "Rustboro", 60, 50, "[pho] thanh pho da, xuong che tao, loi len Thac Meteor")
                .c('S', 28, 44).c('E', 22, 46).c('N', 30, 161)
                .n(-5, -1, 2).n(5, -1, 5).n(0, 3, 0).n(-2, 3, 1);
        them(46, "bien", "Bờ biển Dewford", 80, 50, "[dam] Route 116 - bai cat, bien phia nam, loi vao Hang Granite")
                .c('W', 20, 45).c('N', 40, 160).c('E', 22, 47)
                .q(70, 144, 145, 147);
        them(47, "thi_tran", "Slateport", 64, 52, "[bien] [pho] cho ben cang + bao tang hai duong")
                .c('W', 22, 46).c('N', 30, 48)
                .n(-6, -1, 22).n(6, -1, 1).n(0, 4, 3).n(0, -4, 0);
        them(48, "duong", "Route 110", 50, 90, "duong ven bien doc bac-nam, co cao hai ben")
                .c('S', 22, 47).c('N', 22, 49)
                .q(70, 152, 145, 148);
        them(49, "thi_tran", "Mauville", 60, 54, "[pho] nga tu Hoenn: tay sa mac, dong rung mua")
                .c('S', 28, 48).c('W', 24, 50).c('E', 24, 53)
                .n(-4, -2, 22).n(4, -2, 0).n(0, 3, 1);
        them(50, "sa_mac", "Sa mạc Route 111", 90, 60, "sa mac gio cat, vach da, diem dao")
                .c('E', 26, 49).c('N', 40, 51)
                .n(-6, 0, 21).n(6, 4, 5)
                .q(75, 148, 154, 156);
        them(51, "thi_tran", "Lavaridge", 56, 46, "[ho] suoi nuoc nong giua thi tran, chan nui lua")
                .c('S', 26, 50).c('N', 26, 52)
                .n(-10, -3, 0).n(10, -3, 8).n(-10, 4, 21).n(10, 4, 23);
        them(52, "nui", "Núi Chimney", 80, 70, "[tro] nui lua: tro bui, vach da, mieng nui")
                .c('S', 36, 51).c('W', 30, 161)
                .n(0, -6, 21)
                .q(80, 154, 156, 159);
        them(53, "rung", "Rừng mưa Route 119", 60, 100, "[ho] rung mua nhiet doi, suoi va ho")
                .c('S', 28, 49).c('N', 28, 54)
                .n(4, 0, 6)
                .q(75, 153, 159, 160);
        them(54, "thi_tran", "Fortree", 60, 50, "[rung] lang tren cay giua rung")
                .c('S', 28, 53).c('E', 24, 55)
                .n(-4, -1, 7).n(4, -1, 1).n(0, 3, 0);
        them(55, "dong_co", "Route 120 - Viện Thời tiết", 90, 60, "[ho] dong co mua, ao, Vien thoi tiet")
                .c('W', 26, 54).c('E', 26, 56).c('S', 44, 57)
                .n(0, -6, 3)
                .q(75, 159, 160, 164);
        them(56, "thi_tran", "Lilycove", 64, 52, "[bien] [pho] thanh pho cang, cho lon")
                .c('W', 24, 55).c('S', 30, 58)
                .n(-5, -1, 1).n(5, -1, 22).n(0, 3, 5).n(0, -4, 0);
        them(57, "nui", "Núi Pyre", 60, 80, "nui mo suong mu, bia mo")
                .c('N', 28, 55)
                .n(0, 4, 8)
                .q(75, 161, 151, 163);
        them(58, "bien", "Biển Route 124", 90, 60, "[khoi] bien khoi, dao cat, loi xuong hang duoi bien")
                .c('N', 30, 56).c('E', 28, 59).c('S', 60, 162)
                .n(-8, 0, 10)
                .q(80, 158, 155, 164);
        them(59, "thi_tran", "Mossdeep", 60, 50, "[bien] dao trung tam vu tru")
                .c('W', 24, 58).c('S', 28, 61)
                .n(-4, -1, 5).n(4, -1, 3).n(0, 3, 0);
        them(60, "thi_tran", "Sootopolis", 64, 56, "[ho] thanh pho trong mieng nui lua co, ho giua")
                .c('N', 30, 162).c('W', 26, 163).c('E', 26, 62)
                .n(-8, -6, 5).n(8, -6, 0).n(-8, 6, 10).n(8, 6, 8);
        them(61, "bien", "Biển Route 131", 90, 56, "[khoi] [dam] bien phia nam, dao Thap Troi")
                .c('N', 30, 59).c('E', 26, 164)
                .q(80, 172, 164, 170);
        them(62, "thi_tran", "Ever Grande", 60, 56, "[pho] thanh pho thac nuoc truoc Lien minh")
                .c('W', 26, 60).c('N', 28, 165)
                .n(-4, -1, 21).n(4, -1, 22).n(0, 3, 0).n(-2, 3, 1);
        // ---- hang (bo t_hang) -----------------------------------------------------------------
        them(160, "hang", "Hang Granite", 60, 50, "hang toi, buc ve co")
                .c('S', 28, 46)
                .n(0, -8, 8)
                .q(70, 146, 151, 148);
        them(161, "hang", "Thác Meteor", 70, 56, "[nuoc] hang thac, ho ngam")
                .c('S', 32, 45).c('E', 26, 52)
                .q(70, 157, 149, 155);
        them(162, "hang", "Hang dưới biển", 64, 56, "[nuoc] hang ngap nuoc duoi day bien")
                .c('N', 30, 58).c('S', 30, 60)
                .n(4, 0, 10)
                .q(70, 158, 165, 167);
        them(163, "hang", "Hang Khởi Nguyên", 60, 60, "[nuoc] Cave of Origin - ho ngam sau")
                .c('E', 28, 60)
                .n(-8, 0, 21).n(-8, 4, 5)
                .q(70, 168, 169, 166);
        them(164, "hang", "Tháp Trời", 50, 70, "Sky Pillar - tang thap do nat")
                .c('S', 22, 61).c('N', 22, 280)
                .n(4, 4, 5)
                .q(65, 171, 173, 168);
        them(165, "hang", "Đường Victory Hoenn", 80, 56, "hang cat dai truoc Lien minh")
                .c('S', 36, 62).c('N', 36, 281)
                .q(65, 169, 171, 173, 174);
        // ---- dau truong (bo t_thanh) ----------------------------------------------------------
        them(280, "dau_truong", "Đỉnh Tháp Trời", 44, 40, "san dinh thap - noi Rayquaza ngu")
                .c('S', 20, 164)
                .n(0, -6, 5)
                .q(90, 173, 174);
        them(281, "thanh", "Liên minh Hoenn", 60, 56, "san Lien minh Pokemon - Tu Thien Vuong")
                .c('S', 28, 165).c('N', 28, 282)
                .n(-4, 0, 0).n(4, 0, 4);
        them(282, "dau_truong", "Sảnh Nhà vô địch", 40, 36, "san nha vo dich Hoenn")
                .c('S', 18, 281)
                .n(0, -4, 21).n(4, -4, 5);
    }

    private BanDoHoenn() {
    }

    /** Moi ban do Hoenn (thu tu thiet ke). */
    public static List<BanDo> ds() {
        return Collections.unmodifiableList(new ArrayList<BanDo>(DS.values()));
    }

    public static BanDo lay(final int id) {
        return DS.get(id);
    }

    public static boolean laHoenn(final int idNap) {
        return DS.containsKey(idNap);
    }

    /** Ban do dau tien khi vao tu Kanto. */
    public static BanDo cuaNgo() {
        return DS.get(43);
    }

    /**
     * Diem den {x, y} khi di tu {@code tu} sang {@code den} (dung truoc cong chieu nguoc cua {@code den}).
     * den = MAP_CONG_VAO -> DIEM_VE_KANTO. null neu khong co cong noi hai ban do.
     */
    public static int[] diemDen(final int tu, final int den) {
        if (den == MAP_CONG_VAO && !DS.containsKey(den)) {
            return DIEM_VE_KANTO.clone();
        }
        final BanDo b = DS.get(den);
        if (b == null) {
            return null;
        }
        final int i = b.toM(tu);
        return i < 0 ? null : b.diemVao(i);
    }

    /** Diem dung o Littleroot khi qua cong tu map 9. */
    public static int[] diemVaoTuKanto() {
        return diemDen(MAP_CONG_VAO, 43);
    }

    /**
     * Ly do khong cho qua cong vao Hoenn, null = duoc qua.
     *
     * @param lauDai  sv.lau_dai dang bat
     * @param tranCap tran cap mua hien tai (CapDoLauDai.tranCap)
     * @param cap     cap nhan vat
     */
    public static String lyDoChan(final boolean lauDai, final int tranCap, final int cap) {
        return lyDoChan(lauDai, tranCap, cap, true);
    }

    /** Ma buoc cuoi cot truyen nam 1 (Xatu: "Vung dat phia nam") - can xong moi len tau di Hoenn (lau dai). */
    public static final String MA_KET_NAM_1 = "C52.7";

    /**
     * Nhu tren + dieu kien COT TRUYEN (18/10/2026, khong tran theo mua: Hoenn mo theo cap + cot truyen thay vi
     * theo ngay): khi lau dai bat phai xong {@link #MA_KET_NAM_1} (ket nam 1; chuoi truoc do gom Nha vo dich C50.7
     * o Cao nguyen Indigo, va chuong 52 can cap 90). Tran cap theo mua tat thi {@code tranCap} = 100 (luon qua).
     *
     * @param xongNam1 nhan vat da xong {@link #MA_KET_NAM_1}
     */
    public static String lyDoChan(final boolean lauDai, final int tranCap, final int cap, final boolean xongNam1) {
        if (lauDai && tranCap < TRAN_CAP_MO) {
            return "Tàu đi Hoenn chưa khởi hành: vùng Hoenn mở khi trần cấp mùa đạt " + TRAN_CAP_MO
                    + " (năm 2). Trần cấp hiện tại: " + tranCap + ".";
        }
        if (cap < CAP_VAO) {
            return "Vùng Hoenn rất nguy hiểm: cần đạt cấp " + CAP_VAO + " trở lên mới được lên tàu.";
        }
        if (lauDai && !xongNam1) {
            return "Tàu đi Hoenn chỉ đón nhà vô địch: hãy hoàn thành cốt truyện năm 1 (chương 52 \"Vùng đất phía nam\""
                    + " ở Núi Silver) rồi quay lại.";
        }
        return null;
    }
}
