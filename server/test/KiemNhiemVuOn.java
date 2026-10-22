import real.Char;
import real.NewCharQuest;

/**
 * 0.9.7.1: nhan / tra nhiem vu on dinh (bao cao "nhan nhiem vu khong nhan, tra khong on"). Ham thuan kiem truc tiep,
 * phan con lai quet ma nguon server + client (cung nguon client_jar/src cho PC / APK / J2ME). Kiem end-to-end that:
 * tools/pokemon/kiem_tai/KichBanNhiemVu.java (bot) va client_pc/test/KiemNhiemVu.java (client that, headless).
 */
public final class KiemNhiemVuOn {

    private KiemNhiemVuOn() {
    }

    static final String CHAR = "src/real/Char.java";
    static final String ENGINE = "src/real/NhiemVuEngine.java";
    static final String LAU_DAI = "src/real/NhiemVuLauDaiGame.java";
    static final String CLIENT = "../client_jar/src/game/";

    public static void chay() {
        Kiem.nhom("0.9.7.1 nhiem vu on dinh - nhan / tra / huy / dong bo danh sach");
        hamThuan();
        server();
        client();
    }

    static void hamThuan() {
        final int chua = NewCharQuest.UN_RECEIVE;
        final int dang = NewCharQuest.WORKING;
        final int xong = NewCharQuest.FINISH;
        final int da = NewCharQuest.DONE;
        Kiem.dung("nhiem vu noi chuyen CHUA NHAN + goi tra (client nghe het loi) = nhan roi tra luon", Char.nhanVaTraNoiChuyen(chua, 3));
        Kiem.sai("danh quai chua nhan + goi tra: khong tu nhan", Char.nhanVaTraNoiChuyen(chua, 0));
        Kiem.sai("giao do chua nhan + goi tra: khong tu nhan", Char.nhanVaTraNoiChuyen(chua, 1));
        Kiem.sai("noi chuyen da tra (DONE) + goi tra lap: khong nhan lai", Char.nhanVaTraNoiChuyen(da, 3));
        Kiem.sai("noi chuyen dang lam: di duong tra thuong", Char.nhanVaTraNoiChuyen(dang, 3));
        Kiem.bang("tra duoc: khong co ly do", null, Char.lyDoKhongTra(xong, 0, "A"));
        Kiem.dung("tra som (dang lam, danh quai): ly do 'Chưa xong'", String.valueOf(Char.lyDoKhongTra(dang, 0, "A")).startsWith("Chưa xong nhiệm vụ A"));
        Kiem.dung("tra khi chua nhan: ly do 'chưa nhận'", String.valueOf(Char.lyDoKhongTra(chua, 2, "A")).contains("chưa nhận"));
        Kiem.dung("tra lai nhiem vu da tra: ly do 'đã trả'", String.valueOf(Char.lyDoKhongTra(da, 3, "A")).contains("đã trả"));
        Kiem.dung("thuong co trang bi: can o trong", Char.canOTrongDeTra(new short[]{27, 27, 27, 27, 27, 0}));
        Kiem.sai("thuong khong trang bi (32000): khong can o", Char.canOTrongDeTra(new short[]{32000}));
        Kiem.sai("thuong null: khong can o", Char.canOTrongDeTra(null));
    }

    static void server() {
        final String c = KiemSuaLogic096.doc(CHAR);
        final String on = KiemSuaLogic096.doan(c, "private void xuLyNhiemVu(", 40000);
        Kiem.dung("OnQuest khoa theo nhan vat (goi -64 luong phien vs tien do luong ban do)",
                c.contains("synchronized (this.khoaNhiemVu)") && c.contains("this.xuLyNhiemVu(type, questID, main_sub)"));
        Kiem.dung("chinh: nhan chi khi dung id + UN_RECEIVE (bam dup / ds cu khong dat lai tien do, khong tru diem 2 lan)",
                on.contains("questID != this.mainQuest.id_quest || this.mainQuest.state_quest != NewCharQuest.UN_RECEIVE"));
        Kiem.dung("chinh: tra nhiem vu noi chuyen chua nhan -> nhan (kiem cap / ve) roi tra",
                on.contains("nhanVaTraNoiChuyen(this.mainQuest.state_quest, info.type) && !this.nhanNoiChuyenChinh(info)"));
        Kiem.dung("chinh: tra id cu (bam dup sau khi sang buoc moi) chi dong bo", on.contains("if (questID != this.mainQuest.id_quest) {"));
        Kiem.dung("phu: tra noi chuyen chua nhan (id -1) khong con NPE", on.contains("Map.allSubQuest.get(questID)"));
        Kiem.dung("phu: tra xong ghi tien do engine (khong lam lai o So tay / NPC)", on.contains("NhiemVuEngine.ghiXongPhuCu(this, info.ma)"));
        Kiem.dung("tui day: khong tra nhiem vu co thuong trang bi", on.contains("TUI_DAY_NHIEM_VU"));
        Kiem.dung("moi lan tu choi: ly do + gui lai 3 danh sach", c.contains("void tuChoiNhiemVu(final String lyDo)")
                && KiemSuaLogic096.doan(c, "void tuChoiNhiemVu(final String lyDo)", 300).contains("this.sendInfoQuest()"));
        Kiem.sai("phu: khong con return im lang khi dang lam nhiem vu phu khac",
                on.contains("if (this.subQuest.id_quest > -1 && this.subQuest.id_quest != questID) {\n                    return;"));
        final String r = KiemSuaLogic096.doan(c, "public void receiveQuest(final Message m) {", 600);
        Kiem.dung("goi 52 type 3 (nut Huy cua client 0.9.7) huy that nhiem vu chinh", r.contains("this.OnQuest((byte) 2, this.mainQuest.id_quest, (byte) 0)"));
        final String e = KiemSuaLogic096.doc(ENGINE);
        Kiem.dung("So tay: tra thuong co vat pham khi tui day -> tu choi (truoc bo vat pham im lang)",
                KiemSuaLogic096.doan(e, "static String traThuong(", 2000).contains("coThuongVatPham(m) && p.isFullInventory()"));
        Kiem.dung("So tay: khong nhan nhiem vu phu dang lam qua NPC", e.contains("Bạn đang làm nhiệm vụ này (nhận ở NPC)."));
        Kiem.dung("ds phu NPC bo nhiem vu da nhan / xong o So tay", KiemSuaLogic096.doc(LAU_DAI).contains("NhiemVuEngine.phuCuDaDung(p, t.ma)"));
    }

    static void client() {
        final String q = KiemSuaLogic096.doc(CLIENT + "model/Quest.java");
        Kiem.dung("client: nhiem vu noi chuyen gui THANG tra (-64 type 1) - server phai nhan + tra",
                q.contains("if (this.type == 3) {") && q.contains("GameService.gI().onSendInfoQuest(1, this.idQuest, this.main_sub);"));
        final String g = KiemSuaLogic096.doc(CLIENT + "render/screen/GameScr.java");
        final String oq = KiemSuaLogic096.doan(g, "public void onQuest(Message m) {", 9000);
        Kiem.dung("client: ds xong / dang lam tron ven - bo o cu (tra / huy / vao lai)", oq.contains("mainQuest = boOCu(mainQuest, type_Group_Quest);"));
        Kiem.dung("client: nhiem vu lai trong Co the nhan -> bo o dang lam", oq.contains("subQuest = boNeuCoTheNhan(subQuest);"));
        final String s = KiemSuaLogic096.doc(CLIENT + "networklogic/GameService.java");
        Kiem.dung("client: chan goi -64 trung (cham dup)", KiemSuaLogic096.doan(s, "public void onSendInfoQuest(", 500).contains("trungQuest(khoa, bay)"));
        final String m = KiemSuaLogic096.doc(CLIENT + "pokemon/ManNhiemVu.java");
        Kiem.sai("client: Huy nhiem vu khong con gui goi 52 cu / xoa thuoc cuc bo", m.contains("receiveQuest(0, 0, 3)") || m.contains("dellPotionQuest"));
        Kiem.dung("client: Huy nhiem vu gui -64 type 2", m.contains("onSendInfoQuest(2, q.idQuest, q.main_sub)"));
    }
}
