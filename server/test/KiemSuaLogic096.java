import com.htbb.pokemon.NhiemVuMau;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import real.Char;
import real.Map;
import real.NewCharQuest;

/**
 * 0.9.6: quet loi logic phia server (ban ngoc NPC, ky gui, EXP to doi / ha quai, nhiem vu, nhom). Moi muc la mot
 * loi that da sua; ham thuan kiem truc tiep, phan con lai quet ma nguon (khong can DB / mang).
 */
public final class KiemSuaLogic096 {

    private KiemSuaLogic096() {
    }

    static String doc(final String duong) {
        try {
            return new String(Files.readAllBytes(Paths.get(duong)), StandardCharsets.UTF_8);
        } catch (final Exception e) {
            return "";
        }
    }

    /** Doan than ham bat dau tu {@code dau} (toi da {@code dai} ky tu). */
    static String doan(final String nguon, final String dau, final int dai) {
        final int i = nguon.indexOf(dau);
        return i < 0 ? "" : nguon.substring(i, Math.min(nguon.length(), i + dai));
    }

    public static void chay() {
        Kiem.nhom("0.9.6 sua logic server - ban ngoc NPC, ky gui, EXP to doi / ha quai, nhiem vu, nhom");
        banNgoc();
        kyGui();
        expDoi();
        expHaQuai();
        nhiemVuCu();
        nhiemVuEngine();
        nhom();
    }

    // ------------------------------------------------------------------ 1. ban ngoc cho NPC (lenh 78)

    static void banNgoc() {
        Kiem.bang("gia ban ngoc: 1 trieu vien gia 20000 -> 4 ty xu (truoc: tran int ra am)", 4000000000L,
                Map.giaBanNgocNpc(20000, 1000000));
        Kiem.bang("gia ban ngoc: (gia/5)*n nhu cu khi khong tran", 400L, Map.giaBanNgocNpc(1000, 2));
        Kiem.bang("gia ban ngoc: n <= 0 -> 0", 0L, Map.giaBanNgocNpc(1000, -5));
        final String off = doc("src/real/OfflineMap.java");
        final String banOff = doan(off, "public void doSellGemItem(", 2500);
        Kiem.dung("OfflineMap ban ngoc: loai khac 0/1 bi bo (truoc: kiem KHOA, tru THUONG -> xu khong mat ngoc)",
                banOff.contains("typeMaterial != 0 && typeMaterial != 1"));
        Kiem.dung("OfflineMap ban ngoc: ngoc khoa chan theo ChongClone.banNpcDoKhoa (truoc: chi Map co)",
                banOff.contains("typeMaterial == 1 && !com.htbb.pokemon.ChongClone.banNpcDoKhoa()"));
        Kiem.dung("OfflineMap ban ngoc: gia tinh bang long", banOff.contains("Map.giaBanNgocNpc("));
        final String banMap = doan(doc("src/real/Map.java"), "public void doSellGemItem(", 2500);
        Kiem.dung("Map ban ngoc: loai khac 0/1 bi bo", banMap.contains("typeMaterial != 0 && typeMaterial != 1"));
        Kiem.dung("Map ban ngoc: gia tinh bang long", banMap.contains("giaBanNgocNpc("));
    }

    // ------------------------------------------------------------------ 2. gian hang ky gui

    static void kyGui() {
        final String off = doc("src/real/OfflineMap.java");
        Kiem.sai("mua ngoc o gian hang: khong ep GemSellEntry sang GemItem (ClassCastException sau khi chuyen xu)",
                off.contains("((GemItem)item).getTemplate().name) + \" đã được bán.\""));
        final int khoa = off.indexOf("if (((Item)item).lock == 1) {\n                     player.sendMessage(MessageCreator.createServerAlertMessage(\"Không thể bán vật phẩm đã khóa\"");
        final int canSell = off.indexOf("if (!item.canSellTown() && (");
        Kiem.dung("rao ban: do khoa (lock 1) bi chan TRUOC nhanh canSellTown (thoi trang co han / mat na khoa)",
                khoa > 0 && canSell > khoa);
        final String mua = doan(off, "public synchronized void buyItemOfUser(", 6000);
        Kiem.dung("xac nhan mua: gia / so luong hien tai phai trung luc hoi",
                mua.contains("giaHienTai != player.sellerInfo.priceSell"));
    }

    // ------------------------------------------------------------------ 3. EXP to doi (tat sv.lau_dai)

    static void expDoi() {
        Kiem.bang("chia EXP doi: 100 trieu x80% / 5 nguoi = 16 trieu (truoc: tran int -> am -> ca doi 0 EXP)",
                16000000, Map.chiaExpDoi(100000000, 80, 5, 1));
        Kiem.bang("chia EXP doi: phan nguoi danh 20% x he so 2", 40000000, Map.chiaExpDoi(100000000, 20, 1, 2));
        Kiem.bang("chia EXP doi: gia tri nho giong cong thuc int cu", 1000 * 80 / (100 * 3), Map.chiaExpDoi(1000, 80, 3, 1));
        Kiem.bang("chia EXP doi: (newxp*20/100)*heSo giong thu tu cu", 99 * 20 / 100 * 3, Map.chiaExpDoi(99, 20, 1, 3));
        Kiem.bang("chia EXP doi: kep Integer.MAX_VALUE", Integer.MAX_VALUE, Map.chiaExpDoi(Integer.MAX_VALUE, 20, 1, 50));
        Kiem.bang("chia EXP doi: chia 0 nguoi khong nem loi", 800, Map.chiaExpDoi(1000, 80, 0, 1));
        boolean conCu = false;
        for (final String f : new String[]{"MapDanhNhau", "MapTown", "MapClan", "MapLienDau", "Region", "RegionVantienTran",
            "CharCopy", "CharTrangBi"}) {
            final String s = doc("src/real/" + f + ".java");
            conCu |= s.contains("newxp * 80 /") || s.contains("newxp * 20 / 100");
        }
        Kiem.sai("khong con phep chia EXP doi o int (newxp * 80 / ... , newxp * 20 / 100)", conCu);
    }

    // ------------------------------------------------------------------ 4. EXP ha quai: lech cap chia 2 lan

    static void expHaQuai() {
        for (final String f : new String[]{"MapDanhNhau", "MapTown"}) {
            final String s = doc("src/real/" + f + ".java");
            Kiem.sai(f + ": nhanh ha quai khong con {.., dxpx} (dxpx/dxpx = 1 EXP khi lech >= 12 cap)",
                    s.contains("10, dxpx}") || s.contains("3, dxpx}"));
            Kiem.dung(f + ": nhanh ha quai chia qua ExpLauDai.chiaCu (bat co: traoHaQuai tu ap lech cap)",
                    s.contains("dxpx /= ExpLauDai.chiaCu(downPercentx[ax]);"));
        }
    }

    // ------------------------------------------------------------------ 5. nhiem vu cu (OnQuest, goi -64)

    static void nhiemVuCu() {
        final int chua = NewCharQuest.UN_RECEIVE;
        final int dang = NewCharQuest.WORKING;
        final int xong = NewCharQuest.FINISH;
        final int da = NewCharQuest.DONE;
        Kiem.sai("tra nhiem vu noi chuyen (type 3) CHUA NHAN: khong duoc (truoc: duoc -> bo qua cap / ve chuong)",
                Char.duocTraNhiemVu(chua, 3));
        Kiem.sai("tra nhiem vu giao do (type 1) chua nhan: khong duoc", Char.duocTraNhiemVu(chua, 1));
        Kiem.dung("tra nhiem vu noi chuyen dang lam: duoc", Char.duocTraNhiemVu(dang, 3));
        Kiem.dung("tra nhiem vu giao do da nhan (FINISH): duoc", Char.duocTraNhiemVu(xong, 1));
        Kiem.sai("tra nhiem vu danh quai (type 2) dang lam chua du: khong duoc", Char.duocTraNhiemVu(dang, 2));
        Kiem.dung("tra nhiem vu danh quai da du (FINISH): duoc", Char.duocTraNhiemVu(xong, 2));
        Kiem.sai("tra lai nhiem vu da tra (DONE): khong duoc", Char.duocTraNhiemVu(da, 3));

        final List<NewCharQuest> ds = new ArrayList<NewCharQuest>();
        ds.add(q((short) 2005, (byte) 1));
        ds.add(q((short) 7, (byte) 0));
        Kiem.dung("nhan phu: id co trong danh sach moi", Char.coTrongDanhSachNhan(ds, (short) 2005, (byte) 1));
        Kiem.sai("nhan phu: id khong co trong danh sach (cap cao / khong ton tai)", Char.coTrongDanhSachNhan(ds, (short) 2999, (byte) 1));
        Kiem.sai("nhan phu: id cua nhiem vu CHINH khong tinh la phu", Char.coTrongDanhSachNhan(ds, (short) 7, (byte) 1));
        Kiem.sai("nhan phu: danh sach null", Char.coTrongDanhSachNhan(null, (short) 1, (byte) 1));
    }

    /** NewCharQuest(short, byte) doc template (can DB) - dung ban rong roi gan truong. */
    static NewCharQuest q(final short id, final byte mainSub) {
        final NewCharQuest x = new NewCharQuest(false);
        x.id_quest = id;
        x.main_sub = mainSub;
        return x;
    }

    // ------------------------------------------------------------------ 6. engine nhiem vu (So tay)

    static NhiemVuMau mau(final String ma, final String kieu) {
        final NhiemVuMau m = new NhiemVuMau();
        m.ma = ma;
        m.kieu = kieu;
        return m;
    }

    static void nhiemVuEngine() {
        final NhiemVuMau ngay = mau("N-1.03", "danh_quai");
        Kiem.bang("nhan nv ngay lan dau: duoc", null, NhiemVuMau.chanNhanLai(ngay, null, 20261019));
        Kiem.dung("nhan lai nv ngay CUNG ngay (da tra thuong): chan (truoc: dat ve 0 -> lanh lai vo han)",
                NhiemVuMau.chanNhanLai(ngay, new long[]{5, 2, 20261019, 0}, 20261019) != null);
        Kiem.bang("nhan nv ngay sang ngay moi: duoc", null, NhiemVuMau.chanNhanLai(ngay, new long[]{5, 2, 20261018, 0}, 20261019));
        final NhiemVuMau tuan = mau("T-2", "danh_quai");
        Kiem.dung("nhan lai nv tuan cung tuan: chan", NhiemVuMau.chanNhanLai(tuan, new long[]{1, 2, 42, 0}, 42) != null);
        final NhiemVuMau phu = mau("P-9", "noi_chuyen");
        Kiem.dung("nhan lai nv phu da xong (khong lap lai): chan", NhiemVuMau.chanNhanLai(phu, new long[]{1, 2, 0, 0}, 0) != null);
        final NhiemVuMau tt = mau("TT-1", "thanh_tuu_cap");
        Kiem.dung("thanh tuu khong 'nhan' qua menu nhan (truoc: dat lai -> tra thuong lan nua)",
                NhiemVuMau.chanNhanLai(tt, null, 0) != null);
        Kiem.dung("NhiemVuEngine.nhan goi chanNhanLai trong khoa",
                doan(doc("src/real/NhiemVuEngine.java"), "static String nhan(", 1500).contains("NhiemVuMau.chanNhanLai(m, t.get(ma), kyCua(m))"));
    }

    // ------------------------------------------------------------------ 7. nhom (lenh 48 - 50)

    static void nhom() {
        final long t = 1000000L;
        Kiem.dung("vao nhom: co loi moi con han, nhom ton tai, chua du", Char.duocVaoNhom(12, t, 12, t + 1000L, 300, 2));
        Kiem.sai("vao nhom: KHONG co loi moi (truoc: goi 49/1 id bat ky la vao)", Char.duocVaoNhom(-1, 0L, 12, t, 300, 2));
        Kiem.sai("vao nhom: loi moi cua nguoi khac", Char.duocVaoNhom(13, t, 12, t + 1000L, 300, 2));
        Kiem.sai("vao nhom: loi moi het han", Char.duocVaoNhom(12, t, 12, t + Char.HAN_LOI_MOI_NHOM_MS + 1L, 300, 2));
        Kiem.sai("vao nhom: nhom da du 5 (truoc: kiem nham nhom rong cua nguoi vao)", Char.duocVaoNhom(12, t, 12, t + 1000L, 300, 5));
        Kiem.sai("vao nhom: nguoi moi chua co nhom (partyID -1)", Char.duocVaoNhom(12, t, 12, t + 1000L, -1, 0));
        final String c = doc("src/real/Char.java");
        Kiem.dung("giai tan nhom rong tra id ve kho (truoc: 48 + 50 lap lai can 32000 id)",
                doan(c, "public void deleteParty()", 2500).contains("putID(this.partyID, 5, \"xoa party rong\")"));
        Kiem.dung("roi nhom: nhom rong / truong nhom roi -> giai tan",
                doan(c, "public void outParty()", 800).contains("this.id == this.party.idMaster)) {\n                this.deleteParty();"));
        Kiem.dung("thoat game voi nhom rong: tra id", doan(c, "public void userExitGame()", 2000).contains("this.deleteParty(); // 0.9.6: nhom rong"));
    }
}
