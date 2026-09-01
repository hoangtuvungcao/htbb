package real;

import java.util.Random;
import java.util.Vector;

import data.Database;

/**
 * Cai Tao Cai Trang (NPC dich vu chung, npcType -8, muc "Cai Tao Cai Trang").
 *
 * LUAT (yeu cau ngay 02/09/2026). Dem mot bo thoi trang VINH VIEN thuoc mot trong ba nhom
 * duoi day di cai tao. Thanh cong (70%) thi bo do doi hinh thanh Giap Son Tinh Vuong (do
 * nam) hoac Mi Nuong Cong Chua (do nu), GIU NGUYEN chi so va mo ta goc, co them dong
 * "Tinh Hoa: +N% HP" voi N ngau nhien 1-10. That bai thi mat diem. Cai tao lai bao nhieu
 * lan tuy y, ke ca khi da thanh cong, de tim N cao hon - N moi GHI DE N cu, co the thap hon.
 *
 *   nhom 1  Nghe Thuong / Ngoc Nu / Xich Cuoc / Kim Dong / Loi Cong  (797-801)   500 diem XAI
 *   nhom 2  Hac Lang / Than Nu / Bach Mi / Thuy Vu Hong Sam           (723-726)   300 diem XAI
 *   nhom 3  Lac Long Quan / Au Co                                     (756-757)   500 diem NAP
 *
 * Khong ton luong. Diem xai = Char.diemxaiVip (tich tu khi tieu luong, 2 luong = 1 diem),
 * diem nap = Char.diemNapVip. Sau khi tru phai goi Database.addCharXaiNapLuong de ghi CSDL
 * ngay, khong doi luu nhan vat.
 *
 * Duong "tinh hoa bao dam" (doi binh hoa hinh thu cuoi lay chac 10%) da BO ngay 02/09/2026
 * theo yeu cau nguoi dung; may ngau nhien la duong duy nhat.
 *
 * CAC MANH GHEP KHAC cua tinh nang, de tim mot lan ra het:
 *   - Map.showCaiTaoMaterialList  -> danh sach chon (shop type 5), dung locTrangPhuc o day
 *   - Map: nhanh type == 5         -> kiem dieu kien, mo hop thoai xac nhan (idpopup 125)
 *   - Map: nhanh idpopup == 125    -> tru diem, quay, doi CaiTao, ghi log 'caitaocaitrang'
 *   - Item.getName / tenHienThiSauCaiTao -> ten hien thi (giu cac dong mo ta hieu ung goc)
 *   - ModelWearing.isCaiTaoSonTinh / isCaiTaoMiNuong (CaiTao > 0) -> MessageCreator va Char
 *     chon bo hinh 8700+278/279/281/282/297/298, avatar 831/833
 *   - CharChiSo.calculateMaxHPMP   -> cong % HP theo CaiTao, va CHI HP
 *
 * Lop nay gom luat ve MOT cho de buoc chon va buoc xac nhan trong Map dung chung, va de
 * test/KiemCaiTaoCaiTrang.java goi truc tiep (ham thuan, khong dong CSDL).
 */
public final class CaiTaoCaiTrang {

    /** Ti le thanh cong moi lan quay, tinh theo phan tram. */
    public static final int TI_LE_THANH_CONG = 70;

    static final int[] NHOM_1 = {797, 798, 799, 800, 801};
    static final int[] NHOM_2 = {723, 724, 725, 726};
    static final int[] NHOM_3 = {756, 757};

    /** 1, 2 hay 3 - hoac 0 neu id nay khong cai tao duoc. */
    public static int nhom(final int templateID) {
        if (thuoc(NHOM_1, templateID)) {
            return 1;
        }
        if (thuoc(NHOM_2, templateID)) {
            return 2;
        }
        if (thuoc(NHOM_3, templateID)) {
            return 3;
        }
        return 0;
    }

    /** So diem phai tra cho mot lan cai tao, hoac -1 neu id khong cai tao duoc. */
    public static int diemCan(final int templateID) {
        switch (nhom(templateID)) {
            case 1:
                return 500;
            case 2:
                return 300;
            case 3:
                return 500;
            default:
                return -1;
        }
    }

    /** Nhom 3 tra bang diem NAP; hai nhom con lai tra bang diem XAI. */
    public static boolean dungDiemNap(final int templateID) {
        return nhom(templateID) == 3;
    }

    /** Ten loai diem de ghep vao thong bao cho nguoi choi. */
    public static String tenDiem(final int templateID) {
        return dungDiemNap(templateID) ? "điểm nạp" : "điểm xài";
    }

    /** Diem nguoi choi dang co, dung loai diem ma bo do nay doi hoi. */
    static int diemHienCo(final Char p, final int templateID) {
        return dungDiemNap(templateID) ? p.diemNapVip : p.diemxaiVip;
    }

    /** Tru diem dung loai va ghi CSDL ngay (type 0 = luu diem nap, khac 0 = luu diem xai). */
    static void truDiem(final Char p, final int templateID, final int diem) {
        if (dungDiemNap(templateID)) {
            p.diemNapVip -= diem;
            Database.instance.addCharXaiNapLuong(p, 0);
        } else {
            p.diemxaiVip -= diem;
            Database.instance.addCharXaiNapLuong(p, 1);
        }
    }

    /**
     * Phan tram Tinh Hoa nhan duoc khi thanh cong:
     * 40% ra 1-3, 30% ra 4-6, 20% ra 7-8, 10% ra 9-10 (giu nguyen bang ti le cu).
     */
    public static int quayPhanTramTinhHoa(final Random r) {
        final int x = r.nextInt(100);
        if (x < 40) {
            return r.nextInt(3) + 1;
        }
        if (x < 70) {
            return r.nextInt(3) + 4;
        }
        if (x < 90) {
            return r.nextInt(2) + 7;
        }
        return r.nextInt(2) + 9;
    }

    /**
     * Dong dau cua ten template, truoc dau xuong dong - vi cot name trong data_item chua ca
     * mo ta hieu ung ("Ao Xich Cuoc\n- Tang 50% bang ,set ,doc ,lua lan\n...").
     */
    public static String tenNgan(final String tenTemplate) {
        if (tenTemplate == null) {
            return "";
        }
        final int nl = tenTemplate.indexOf('\n');
        return (nl < 0 ? tenTemplate : tenTemplate.substring(0, nl)).trim();
    }

    /** Bo do cai tao duoc khi: thuoc mot nhom VA vinh vien (minuteExist <= 0). */
    public static boolean caiTaoDuoc(final Item item) {
        return item != null && item.minuteExist <= 0 && nhom(item.tempateID) > 0;
    }

    /**
     * Danh sach de nguoi choi chon: bo DANG MAC (neu cai tao duoc) dung dau, roi cac bo trong
     * hanh trang. Buoc chon (type == 5) va buoc xac nhan (idpopup == 125) PHAI dung chung ham
     * nay, vi client gui ve chi so trong danh sach - hai ben loc lech la tru diem sai mon.
     */
    static Vector<Item> locTrangPhuc(final Char player) {
        final Vector<Item> ra = new Vector<Item>();
        final Item dangMac = player.wModel == null ? null : player.wModel.itemModel;
        if (caiTaoDuoc(dangMac)) {
            ra.addElement(dangMac);
        }
        for (int i = 0; i < player.iItems.size(); i++) {
            final Item item = (Item) player.iItems.elementAt(i);
            if (caiTaoDuoc(item)) {
                ra.addElement(item);
            }
        }
        return ra;
    }

    /** Mon vua cai tao co phai bo dang mac khong - neu phai thi chi so va hinh anh doi ngay. */
    static boolean laBoDangMac(final Char player, final Item item) {
        return player.wModel != null && player.wModel.itemModel == item;
    }

    private static boolean thuoc(final int[] ds, final int id) {
        for (final int x : ds) {
            if (x == id) {
                return true;
            }
        }
        return false;
    }

    private CaiTaoCaiTrang() {
    }
}
