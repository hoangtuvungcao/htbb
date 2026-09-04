import real.GiaoDichThuoc;

/**
 * Kiem thu luat hoan tat GIAO DICH THUOC (real.GiaoDichThuoc).
 *
 * Bo test nay ghi lai dung cac tinh huong da bi loi dung tren server that:
 *   - trang thai "doi phuong bo cho minh" (rPotions) con sot tu lan giao dich truoc da huy,
 *   - ben chuyen da tieu bot thuoc sau khi bo vao,
 *   - thuoc khong duoc phep giao dich (nhan sam, the van tieu... isTrade = 0) van lot vao.
 * Moi truong hop phai bi tu choi TRUOC khi cong thuoc cho ai.
 */
public final class KiemGiaoDichThuoc {

    private static final int N = 162;

    /** Luat gia lap: chi o 1..6 (HP/MP) va 109 (bo hoa vang) giao dich duoc; 82 khong gioi han. */
    private static final GiaoDichThuoc.LuatThuoc LUAT = new GiaoDichThuoc.LuatThuoc() {
        @Override
        public boolean giaoDichDuoc(final int id) {
            return (id >= 1 && id <= 6) || id == 109 || id == 82;
        }

        @Override
        public boolean khongGioiHan(final int id) {
            return id == 82;
        }
    };

    public static void chay() {
        Kiem.nhom("real.GiaoDichThuoc - luat hoan tat giao dich thuoc");

        giaoDichBinhThuongHopLe();
        khongAiBoGiCungHopLe();
        trangThaiCuConSotBiTuChoi();
        benChuyenKhongConDuBiTuChoi();
        thuocKhongGiaoDichDuocBiTuChoi();
        xuKhongGiaoDichDuoc();
        tranOBiTuChoi();
        thuocKhongGioiHanKhongTinhTran();
        soAmBiTuChoi();
        mangLechChieuDaiBiTuChoi();
        moTaVaTomTat();
    }

    private static int[] mang() {
        return new int[N];
    }

    private static void giaoDichBinhThuongHopLe() {
        final int[] potA = mang(), tA = mang(), rA = mang(), potB = mang(), tB = mang(), rB = mang();
        potA[2] = 10; tA[2] = 5; rB[2] = 5;      // A dua 5 HP vua
        potB[5] = 3;  tB[5] = 3; rA[5] = 3;      // B dua 3 MP vua
        Kiem.bang("hai ben dua thuoc hop le", Integer.valueOf(GiaoDichThuoc.HOP_LE),
                Integer.valueOf(GiaoDichThuoc.kiemTra(potA, tA, rA, potB, tB, rB, LUAT)));
    }

    private static void khongAiBoGiCungHopLe() {
        Kiem.bang("khong ai bo gi -> hop le", Integer.valueOf(GiaoDichThuoc.HOP_LE),
                Integer.valueOf(GiaoDichThuoc.kiemTra(mang(), mang(), mang(), mang(), mang(), mang(), LUAT)));
    }

    /**
     * Tinh huong that: A tung giao dich voi C, C bo 999 thuoc cho A roi giao dich bi huy ngang
     * (C dung binh / doi map). rPotions cua A khong duoc xoa. A mo giao dich moi voi B, khong ai
     * bo gi, hoan tat -> ban cu cong cho A 999 thuoc tu khong khi.
     */
    private static void trangThaiCuConSotBiTuChoi() {
        final int[] potA = mang(), tA = mang(), rA = mang(), potB = mang(), tB = mang(), rB = mang();
        rA[2] = 999; // "B da bo cho A 999" nhung tB[2] = 0
        Kiem.bang("rPotions con sot tu lan truoc -> lech hai ben", Integer.valueOf(GiaoDichThuoc.LOI_LECH_HAI_BEN),
                Integer.valueOf(GiaoDichThuoc.kiemTra(potA, tA, rA, potB, tB, rB, LUAT)));

        final int[] tA2 = mang(), rB2 = mang();
        tA2[2] = 5; rB2[2] = 4; // A ghi 5, B ghi nhan 4
        potA[2] = 10;
        Kiem.bang("tA khac rB -> lech hai ben", Integer.valueOf(GiaoDichThuoc.LOI_LECH_HAI_BEN),
                Integer.valueOf(GiaoDichThuoc.kiemTra(potA, tA2, mang(), potB, mang(), rB2, LUAT)));
    }

    private static void benChuyenKhongConDuBiTuChoi() {
        final int[] potA = mang(), tA = mang(), rA = mang(), potB = mang(), tB = mang(), rB = mang();
        potA[2] = 3; tA[2] = 10; rB[2] = 10; // bo vao 10 luc con 10, gio chi con 3
        Kiem.bang("ben chuyen da tieu bot thuoc -> khong du", Integer.valueOf(GiaoDichThuoc.LOI_KHONG_DU),
                Integer.valueOf(GiaoDichThuoc.kiemTra(potA, tA, rA, potB, tB, rB, LUAT)));
    }

    private static void thuocKhongGiaoDichDuocBiTuChoi() {
        final int[] potA = mang(), tA = mang(), rA = mang(), potB = mang(), tB = mang(), rB = mang();
        potA[9] = 50; tA[9] = 1; rB[9] = 1; // nhan sam: isTrade = 0
        Kiem.bang("nhan sam (isTrade=0) -> khong giao dich duoc", Integer.valueOf(GiaoDichThuoc.LOI_KHONG_GIAO_DICH_DUOC),
                Integer.valueOf(GiaoDichThuoc.kiemTra(potA, tA, rA, potB, tB, rB, LUAT)));
    }

    private static void xuKhongGiaoDichDuoc() {
        final int[] potA = mang(), tA = mang(), rA = mang(), potB = mang(), tB = mang(), rB = mang();
        potA[0] = 1000; tA[0] = 100; rB[0] = 100;
        Kiem.bang("o 0 (xu) -> khong giao dich duoc", Integer.valueOf(GiaoDichThuoc.LOI_KHONG_GIAO_DICH_DUOC),
                Integer.valueOf(GiaoDichThuoc.kiemTra(potA, tA, rA, potB, tB, rB, LUAT)));
    }

    private static void tranOBiTuChoi() {
        final int[] potA = mang(), tA = mang(), rA = mang(), potB = mang(), tB = mang(), rB = mang();
        potA[2] = 10; tA[2] = 10; rB[2] = 10;
        potB[2] = 995; // 995 + 10 = 1005 > 999
        Kiem.bang("ben nhan vuot 999 -> tran o", Integer.valueOf(GiaoDichThuoc.LOI_TRAN_O),
                Integer.valueOf(GiaoDichThuoc.kiemTra(potA, tA, rA, potB, tB, rB, LUAT)));
        potB[2] = 989; // 989 + 10 = 999: vua du
        Kiem.bang("ben nhan dung 999 -> hop le", Integer.valueOf(GiaoDichThuoc.HOP_LE),
                Integer.valueOf(GiaoDichThuoc.kiemTra(potA, tA, rA, potB, tB, rB, LUAT)));
    }

    private static void thuocKhongGioiHanKhongTinhTran() {
        final int[] potA = mang(), tA = mang(), rA = mang(), potB = mang(), tB = mang(), rB = mang();
        potA[82] = 5000; tA[82] = 5000; rB[82] = 5000;
        potB[82] = 5000;
        Kiem.bang("thuoc khong gioi han khong bi chan 999", Integer.valueOf(GiaoDichThuoc.HOP_LE),
                Integer.valueOf(GiaoDichThuoc.kiemTra(potA, tA, rA, potB, tB, rB, LUAT)));
    }

    private static void soAmBiTuChoi() {
        final int[] potA = mang(), tA = mang(), rA = mang(), potB = mang(), tB = mang(), rB = mang();
        tA[2] = -5; rB[2] = -5;
        Kiem.bang("so luong am -> tu choi", Integer.valueOf(GiaoDichThuoc.LOI_SO_AM),
                Integer.valueOf(GiaoDichThuoc.kiemTra(potA, tA, rA, potB, tB, rB, LUAT)));
    }

    /** Ban cu tao lai mang bang hai con so khac nhau (160 va 162); mang lech thi khong duoc tin. */
    private static void mangLechChieuDaiBiTuChoi() {
        Kiem.bang("mang 160 so voi 162 -> mang hong", Integer.valueOf(GiaoDichThuoc.LOI_MANG),
                Integer.valueOf(GiaoDichThuoc.kiemTra(mang(), new int[160], mang(), mang(), mang(), mang(), LUAT)));
        Kiem.bang("mang null -> mang hong", Integer.valueOf(GiaoDichThuoc.LOI_MANG),
                Integer.valueOf(GiaoDichThuoc.kiemTra(mang(), null, mang(), mang(), mang(), mang(), LUAT)));
    }

    private static void moTaVaTomTat() {
        Kiem.bang("mo ta loi lech", "trang thai hai ben khong khop", GiaoDichThuoc.moTaLoi(GiaoDichThuoc.LOI_LECH_HAI_BEN));
        final int[] tA = mang(), rA = mang(), tB = mang(), rB = mang();
        tA[9] = 999; rB[9] = 999;
        final String tt = GiaoDichThuoc.tomTat(tA, rA, tB, rB);
        Kiem.bang("tom tat chi liet ke o khac 0", "tA[9]=999 rB[9]=999", tt);
        Kiem.sai("tom tat khong xuong dong", tt.indexOf('\n') >= 0);
        Kiem.bang("tom tat mang rong", "(rong)", GiaoDichThuoc.tomTat(mang(), mang(), mang(), mang()));
    }

    private KiemGiaoDichThuoc() {
    }
}
