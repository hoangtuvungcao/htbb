package real;

import data.Database;
import java.util.Calendar;
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
 * Rieng "top dame" (nguoi gay nhieu sat thuong nhat) CHUA lam trong ban nay: da khao sat ky,
 * khong co san bat ky bookkeeping sat thuong theo tung nguoi choi nao trong code hien tai
 * (moi boss khac cung chi thuong nguoi ha guc). Them "top dame" can mot ledger dam rieng cho
 * tung boss (Hashtable<String,Long>) cap nhat tai diem ap dung sat thuong that su - de ngo
 * cho mot lan sua rieng, KHONG lam voi trong ban nay de tranh dung vao code combat dung chung.
 */
public class BossTrungThu extends Boss {

    private static final int[] GIO_XUAT_HIEN = {9, 12, 23};

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
        if (this.isDead) {
            this.hp = this.maxhp;
            if (dangTrongGioXuatHien) {
                Map.createLocationGate(0);
                this.isDead = false;
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
        if (p.wModel == null || !p.wModel.isMatNaOngDia()) {
            p.sendMessage(MessageCreator.createServerAlertMessage(
                    "Bạn cần đeo Mặt nạ Ông Địa để nhận thưởng từ Boss Trung Thu.", ""));
            return super.onDropItem(m, p);
        }
        phatQuaTrungThu(p);
        return super.onDropItem(m, p);
    }

    /** Qua rieng cho nguoi HA GUC boss (dang deo mat na Ong Dia) - 1 trong 8 loai, ngau nhien. */
    private void phatQuaTrungThu(final Char p) {
        final int[] per      = {600, 600, 500, 700, 400, 500, 700, 700};
        final int[] idgift   = {851, 848, 76,  -1,  GemTemplate.LKD_30, -2, 93, 95};
        final int[] typegift = {3,   3,   4,   8,   6,                  9,  4,  4};
        final int index = Char.getIndexRandom(per);
        String info = "Boss Trung Thu: bạn nhận được ";
        switch (typegift[index]) {
            case 3: {
                final ItemTemplates template = (ItemTemplates) Map.itemTemplates.get(5).get(idgift[index]);
                final Item newItem = new Item(template, false, 0, 0, template.id);
                newItem.id = p.getIDItem();
                newItem.owner = p.charDBID;
                newItem.level = newItem.getTemplate().level;
                p.iItems.add(newItem);
                newItem.identify = p.charDBID;
                p.sendMessage(MessageCreator.createCharInventoryMessage(p, 1));
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
            default:
                return;
        }
        p.sendMessage(MessageCreator.createServerAlertMessage(info, ""));
        Database.instance.saveOrtherLog("", p.charname, info, "bosstrungthu");
    }
}
