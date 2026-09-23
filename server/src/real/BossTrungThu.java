package real;

import data.Database;
import java.util.Calendar;
import java.util.Hashtable;
import java.util.Map.Entry;
import java.util.Vector;
import real.cmd.LoginHandler;

/**
 * Trung Thu 2026 - Boss xuat hien tai map Truong Giang (id 9) theo gio 9h/12h/23h moi ngay,
 * gate boi Char.isSuKienTrungThul2016(). Do hoa/chi so goc TAM DUNG mau "Su Tu" (id 48 trong
 * data_monster) theo yeu cau nguoi dung (16/09/2026: "dung tam do hoa 1 quai/boss co san") -
 * chi so goc cua id 48 la QUAI THUONG (maxhp ~193k, qua yeu), nen BOOST lai o constructor
 * thanh chi so tuong duong cac boss khac dang co (BossThanLan/BossLinhXa: ~150-200 trieu HP).
 *
 * KHONG chan danh o MapDanhNhau/Region (qua nhieu diem ap dung sat thuong rai rac trong 2
 * file do, sua rong se rui ro cao cho toan bo combat). Thay vao do: AI CUNG danh duoc binh
 * thuong, nhung CHI nguoi dang deo mat na Ong Dia (ModelWearing.isMatNaOngDia()) LUC HA GUC
 * moi duoc nhan qua rieng cua boss nay (xem onDropItem) - khong deo van tinh la ha guc binh
 * thuong (kich hoat lai bornTime nhu moi boss khac) nhung khong co qua rieng.
 *
 * "Top dame" (them 19/09/2026, yeu cau nguoi dung "lam tiep"): ledger sat thuong RIENG cho
 * boss nay (soDoSatThuong, key = charname) - CHI ghi nhan tai cac diem GOI ghiNhanSatThuong()
 * duoc them THEM VAO (khong sua) o MapDanhNhau.java/Region.java, moi cho deu boc trong
 * "if (mt instanceof BossTrungThu)" nen KHONG anh huong toi bat ky monster/combat nao khac -
 * dung dung tinh than "khong dung vao code combat dung chung" da ghi truoc do. Ledger duoc
 * xoa moi lan boss HOI SINH (checkTimeLife) de moi lan xuat hien la mot "cuoc dua" rieng.
 *
 * "Bat than thu ve lam ho ve" (them 19/09/2026): CHUA co template/sprite rieng cho "than thu
 * ho ve" (can quyet dinh thiet ke rieng), nen tam thoi dung lai co che Lan Su Tu da co san
 * (player.createLan - dung EFFECT/Y NGHIA voi boss goc dang mang dang "Su Tu") lam phan
 * thuong "bat duoc" ty le thap khi ha guc, thay vi bia mot loai thu hoan toan moi khong co
 * hinh anh that.
 */
public class BossTrungThu extends Boss {

    private static final int[] GIO_XUAT_HIEN = {9, 12, 23};

    /**
     * Danh dau "khung gio" (nam + ngay-trong-nam + gio) da tung xuat hien, tranh hoi
     * sinh lap tuc trong CUNG mot khung gio. DA SUA (23/09/2026): checkTimeLife() chay
     * lien tuc ~10ms/lan (TeamServer.java, vong lap chung moi boss) va truoc day chi
     * kiem "dang chet + gio hien tai thuoc {9,12,23}" - khong co gi ngan no hoi sinh
     * NGAY LAP TUC sau khi bi ha guc neu van con trong cung gio do, nen boss bi cay
     * lien tuc thay vi xuat hien 1 lan/khung gio nhu spec dinh. -1 = chua tung xuat
     * hien, khong the trung voi bat ky khung gio that nao.
     */
    private long khungGioDaXuatHien = -1L;

    private final Hashtable<String, Long> soDoSatThuong = new Hashtable<>();

    /** Goi TU BEN NGOAI (MapDanhNhau.java/Region.java) moi lan mot Char gay sat thuong len
     * mot Actor - CHI lam gi do neu actor la BossTrungThu, hoan toan khong anh huong monster
     * khac. Dat o day (khong phai Monster/Boss dung chung) de giu pham vi anh huong hep nhat. */
    public static void ghiNhanSatThuong(final Actor mt, final Char nguoiDanh, final long dam) {
        if (!(mt instanceof BossTrungThu) || nguoiDanh == null || dam <= 0L) {
            return;
        }
        final BossTrungThu boss = (BossTrungThu) mt;
        synchronized (boss.soDoSatThuong) {
            final Long hienTai = boss.soDoSatThuong.get(nguoiDanh.charname);
            boss.soDoSatThuong.put(nguoiDanh.charname, (hienTai == null ? 0L : hienTai) + dam);
        }
    }

    private String layTenTopDame() {
        synchronized (this.soDoSatThuong) {
            String tenTop = null;
            long damTop = -1L;
            for (final Entry<String, Long> e : this.soDoSatThuong.entrySet()) {
                if (e.getValue() > damTop) {
                    damTop = e.getValue();
                    tenTop = e.getKey();
                }
            }
            return tenTop;
        }
    }

    public BossTrungThu(final Map mapLiveIn, final MonsterTemplate template, final int x, final int y, final int country) {
        super(mapLiveIn, template, x, y, country);
        this.isBoss = true;
        // Boost tu chi so "Su Tu" (quai thuong, ~193k hp) len muc boss that su.
        this.maxhp = 180000000;
        this.hp = this.maxhp;
        this.attack = 27000;
        this.defend_physic = 25000;
    }

    @Override
    public void checkTimeLife() {
        if (!this.isOpen || Char.isFinishAllSuKienTrungThul2016()) {
            return;
        }
        final Calendar cl = Calendar.getInstance();
        final int gioHienTai = cl.get(Calendar.HOUR_OF_DAY);
        final boolean dangTrongGioXuatHien = coTrongMang(gioHienTai);
        final long khungGioHienTai = cl.get(Calendar.YEAR) * 10000L + cl.get(Calendar.DAY_OF_YEAR) * 100L + gioHienTai;
        if (this.isDead) {
            this.hp = this.maxhp;
            if (dangTrongGioXuatHien && khungGioHienTai != this.khungGioDaXuatHien) {
                this.khungGioDaXuatHien = khungGioHienTai;
                Map.createLocationGate(0);
                this.isDead = false;
                synchronized (this.soDoSatThuong) {
                    this.soDoSatThuong.clear();
                }
                try {
                    this.map = (Map) RealController.mapList.get(9);
                    this.map.removeMonster(this.id, this.inCountry, this.region);
                    this.map.addMonsterDynamic(this, this.inCountry, this.region);
                } catch (final Exception e) {
                    e.printStackTrace();
                }
                try {
                    Map.sendAllCharServer(-1, MessageCreator.createServerAlertAutoOffMessage(
                            "Boss Trung Thu đã xuất hiện tại Trường Giang! Phải đeo Mặt nạ Ông Địa mới nhận được quà khi hạ gục."));
                } catch (final java.io.IOException ex) {
                }
            }
        } else if (!dangTrongGioXuatHien) {
            this.actorDie();
        }
    }

    private static boolean coTrongMang(final int gio) {
        for (final int g : GIO_XUAT_HIEN) {
            if (g == gio) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void actorDie() {
        super.actorDie();
        try {
            this.map.removeMonster(this.id, this.inCountry, this.region);
        } catch (final Exception ignored) {
        }
    }

    @Override
    public Vector<Actor> onDropItem(final Map m, final Char p) {
        // Lay ten top dame TRUOC KHI actorDie()/super.onDropItem() co the don dep trang
        // thai - layTenTopDame() doc rieng soDoSatThuong, khong bi anh huong, nhung lay
        // som cho chac.
        final String tenTopDame = this.layTenTopDame();
        if (p.wModel == null || !p.wModel.isMatNaOngDia()) {
            p.sendMessage(MessageCreator.createServerAlertMessage(
                    "Bạn cần đeo Mặt nạ Ông Địa để nhận thưởng từ Boss Trung Thu.", ""));
            return super.onDropItem(m, p);
        }
        phatQuaTrungThu(p);
        // "Co hoi san than thu ve lam ho ve" (them 19/09/2026) - ty le thap, CHI cho nguoi
        // ha guc (dang deo mat na, da qua dieu kien o tren). Xem chu thich dau file ve ly
        // do dung Lan Su Tu thay vi mot loai thu hoan toan moi.
        if (Map.r.nextInt(100) < 8) {
            p.createLan(10080L, false, 95);
            p.sendMessage(MessageCreator.createServerAlertMessage(
                    "Chúc mừng! Bạn đã bắt được Boss Trung Thu về làm Lân Sư Tử hộ vệ (7 ngày).", ""));
            Database.instance.saveOrtherLog("", p.charname, "bat boss trung thu ve lam ho ve", "bosstrungthu");
        }
        // "Top dame" (them 19/09/2026) - phan thuong RIENG, ngau nhien nhu nguoi ha guc,
        // cho nguoi gay nhieu sat thuong nhat NEU dang online VA cung dang deo mat na Ong
        // Dia (dung dieu kien nhu spec "phai deo mat na moi co the san boss"). Neu top dame
        // TRUNG voi nguoi ha guc thi ho nhan CA HAI luot quay rieng (dung "1 phan lasthit
        // va 1 phan top dame" cua spec, khong phai chi 1 phan chung).
        if (tenTopDame != null) {
            final Char topDame = CharManager.instance.getCharByCharName(tenTopDame);
            if (topDame != null && topDame.wModel != null && topDame.wModel.isMatNaOngDia()) {
                phatQuaTrungThu(topDame);
            }
        }
        return super.onDropItem(m, p);
    }

    /**
     * Qua rieng cho nguoi HA GUC boss (dang deo mat na Ong Dia).
     *
     * Sua 19/09/2026 (yeu cau nguoi dung, doi chieu spec day du hon): bang cu 8 muc dung
     * SAI loai (Manh kim co/kim cang thay vi Manh Giay/Ao/Quan) va thieu Luong khoa/Ve them
     * luot van tieu/The bai ho ve than thu. Manh Sa Tang/Ngo Khong BO HAN (nguoi dung xac
     * nhan hien chua co cac manh do). "Top dame" va "bat than thu ve lam ho ve" (nhac trong
     * chu thich dau file) VAN CHUA lam - can mot lan sua rieng do can bookkeeping sat thuong
     * rieng cho boss nay, ngoai pham vi sua nhanh nay.
     */
    private void phatQuaTrungThu(final Char p) {
        final int[] per      = {500, 500, 500, 700, 400, 500, 700, 700, 500, 500, 400, 300};
        final int[] idgift   = {852, 848, 849, 76,  GemTemplate.LKD_30, -2, 93, 95, 82,  -1,  -3,  10080};
        final int[] typegift = {3,   3,   3,   4,   6,                  9,  4,  4,  4,   8,   11,  12};
        final int index = Char.getIndexRandom(per);
        String info = "Boss Trung Thu: bạn nhận được ";
        switch (typegift[index]) {
            case 3: {
                // Ca 3 id o day (852, 848, 849) nam trong dai Item.isManhSuuTap() (848-859)
                // - DA SUA (23/09/2026): truoc tao thang bang "new Item(...)" nen moi lan ha
                // guc la THEM 1 item RIENG (khong cong don), lam hanh trang day rac item
                // "So luong: 0" vo dung (otherAtt[65] khong bao gio duoc gan). Doi sang
                // Char.createItemSoLuong nhu moi noi khac tao manh cai trang - tu cong don
                // vao 1 item duy nhat qua otherAtt[65] neu nguoi choi da co san.
                // createItemSoLuong tu gui createCharInventoryMessage + 1 thong bao rieng
                // cua no ("Chuc mung. Ban da nhan duoc...") - nguoi choi se thay 2 thong
                // bao (them thong bao "Boss Trung Thu: ..." o cuoi ham nay), chap nhan
                // duoc de doi lay dung logic cong don co san, khong tu viet lai.
                final Item newItem = Char.createItemSoLuong(idgift[index], 1, p);
                info = String.valueOf(info) + newItem.getName();
                break;
            }
            case 4: {
                p.potions[idgift[index]]++;
                p.sendMessage(MessageCreator.createCharInventoryMessage(p, 0));
                info = String.valueOf(info) + "1 " + LoginHandler.PORTION_NAME[idgift[index]];
                break;
            }
            case 6: {
                p.doAddGemItem(idgift[index], 1, false);
                p.sendMessage(MessageCreator.createCharGemItem(p));
                info = String.valueOf(info) + Map.gemTemplate[idgift[index]].name;
                break;
            }
            case 8: {
                final int xu = 500000 + Map.r.nextInt(1500000);
                p.addXu(xu, "boss trung thu");
                info = String.valueOf(info) + xu + " xu";
                break;
            }
            case 9: {
                final int idx = Map.r.nextInt(GemTemplate.ID_MATERIAL_LOW[5].length);
                final boolean khoa = Map.r.nextInt(100) < 50;
                p.doAddGemItem(GemTemplate.ID_MATERIAL_LOW[5][idx], 1, khoa);
                p.sendMessage(MessageCreator.createCharGemItem(p));
                info = String.valueOf(info) + Map.gemTemplate[GemTemplate.ID_MATERIAL_LOW[5][idx]].name + (khoa ? " khóa" : " không khóa");
                break;
            }
            case 11: {
                // Luong khoa (moi 19/09/2026).
                final int luong = 30 + Map.r.nextInt(70);
                p.addLuongLock(luong);
                info = String.valueOf(info) + luong + " lượng khóa";
                break;
            }
            case 12: {
                // The bai ho ve than thu (moi 19/09/2026) - tai su dung co che "ho ve lenh"
                // co san (Map.doCreateHoVeLenhByLevel), chua co template rieng cho "ho ve
                // than thu boss Trung Thu".
                final Item hvl = Map.doCreateHoVeLenhByLevel(p, idgift[index]);
                info = String.valueOf(info) + (hvl != null ? hvl.getName() : "hộ vệ lệnh");
                break;
            }
            default:
                return;
        }
        p.sendMessage(MessageCreator.createServerAlertMessage(info, ""));
        Database.instance.saveOrtherLog("", p.charname, info, "bosstrungthu");
    }
}
