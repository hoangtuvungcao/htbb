package game.pokemon;

import game.model.Char;
import java.io.DataInputStream;

/**
 * MỘT nguồn sự thật cho HP / PP hiện tại của nhân vật (mình + người khác trên map, gồm đồng đội): mọi gói tin đổi
 * HP / PP đi qua đây và ghi thẳng vào Char.hp / realHP / mp / maxhp / maxmp - các widget (thẻ trạng thái GameScr /
 * HudRong / Hud APK / TheTrangThai PC, thanh máu trên đầu, khung mục tiêu) đều đọc các trường đó mỗi khung vẽ, nên
 * giá trị mới hiện NGAY khung vẽ kế tiếp sau khi gói tin được xử lý.
 *
 * Lỗi 0.9.2 "HP cũ sau khi dùng bình": MessageHandler lệnh 22 (bản dịch ngược) có
 * {@code try { ishp = readByte(); break; } catch ...} -> server gửi kèm byte cờ HP (bình HP luôn gửi) thì
 * {@code break} thoát switch TRƯỚC khi gọi onUsePotion: HP chỉ cập nhật ở gói định kỳ sau đó. Thêm nữa onUsePotion
 * bỏ qua bình HP 93 / 94, chỉ tăng chứ không đặt giá trị tuyệt đối, coi PP = 0 là gục, và tin cờ isHP mà server đặt 1
 * cả cho PP (createMsgUseHpMP loại 4).
 */
public final class MauPP {
    public static final int KHONG = 0, HP = 1, PP = 2;

    /** Bình / nguồn hồi HP và PP theo mã potionType của lệnh 22 (khớp server DungVatPham.THUOC_HP / THUOC_MP). */
    static final int[] LOAI_HP = {1, 2, 3, 21, 22, 93, 94};
    static final int[] LOAI_PP = {4, 5, 6, 23, 24, 95, 96, 127};

    private MauPP() {
    }

    static boolean co(int[] a, int v) {
        for (int i = 0; i < a.length; i++) {
            if (a[i] == v) {
                return true;
            }
        }
        return false;
    }

    /** Lệnh 22 nói về HP hay PP: theo mã bình trước, cờ isHP chỉ dùng cho mã lạ (34, 82, 85, 126...). */
    public static int loai(int potionType, int isHP) {
        if (co(LOAI_HP, potionType)) {
            return HP;
        }
        if (co(LOAI_PP, potionType)) {
            return PP;
        }
        if (potionType == 34 || potionType == 82 || potionType == 85 || potionType == 126) {
            return isHP == 1 ? HP : PP;
        }
        return KHONG;
    }

    /** Đọc lệnh 22: {userId, potionType, valueAdd, valueNew, isHP} - byte isHP cuối tuỳ chọn. */
    public static int[] doc22(DataInputStream r) throws java.io.IOException {
        int id = r.readShort();
        int loai = r.readByte();
        int them = r.readShort();
        int moi = r.readInt();
        int isHP = 0;
        try {
            isHP = r.readByte();
        } catch (Exception e) {
            // server cũ không gửi cờ
        }
        return new int[]{id, loai, them, moi, isHP};
    }

    /** Đặt HP tuyệt đối theo server (hủy đồng bộ realHP trễ đang chờ - không để giá trị cũ đè lại). */
    public static void datHp(Char c, int hp) {
        if (hp < 0) {
            hp = 0;
        }
        c.hp = hp;
        c.realHP = hp;
        c.realHPSyncTime = 0;
    }

    public static void datPp(Char c, int mp) {
        c.mp = mp < 0 ? 0 : mp;
    }

    /**
     * Áp lệnh 22 lên nhân vật c. Trả HP / PP / KHONG (để nơi gọi bay chữ "hp+" / "mp+"). valueNew là giá trị
     * TUYỆT ĐỐI của server sau khi hồi / trừ.
     */
    public static int apDung22(Char c, int potionType, int valueNew, int isHP) {
        int k = loai(potionType, isHP);
        if (k == HP) {
            datHp(c, valueNew);
            if (valueNew <= 0) {
                c.state = (byte) 3;
            }
        } else if (k == PP) {
            datPp(c, valueNew);
        }
        if (k != KHONG) {
            TrangThaiNguoiChoi.doi(TrangThaiNguoiChoi.HP);
        }
        return k;
    }

    /**
     * Lệnh 60 (createNew_HMP_Message): id, maxhp, maxmp, def, hp [, hpCong, mp]. Đọc phần đuôi tuỳ chọn; trả
     * {id, maxhp, maxmp, def, hp, mp hoặc -1 khi server cũ không gửi}.
     */
    public static int[] doc60(DataInputStream r) throws java.io.IOException {
        int id = r.readShort();
        int maxhp = r.readInt();
        int maxmp = r.readInt();
        int def = r.readShort();
        int hp = r.readInt();
        int mp = -1;
        try {
            r.readShort(); // hpCong
            mp = r.readInt();
        } catch (Exception e) {
            // server cũ: không có PP
        }
        return new int[]{id, maxhp, maxmp, def, hp, mp};
    }

    /** Áp lệnh 60 lên c (HP / max / PP nếu có). */
    public static void apDung60(Char c, int[] v) {
        c.maxhp = v[1];
        c.maxmp = v[2];
        c.defend = (short) v[3];   // như onNewHPMP cũ (đọc short)
        datHp(c, v[4]);
        if (v[5] >= 0) {
            datPp(c, v[5]);
        }
        TrangThaiNguoiChoi.doi(TrangThaiNguoiChoi.HP);
    }
}
