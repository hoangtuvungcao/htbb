package real;

/**
 * Chieu BI DONG (noi tai) cua 5 dong nguoi choi - MOT cho noi "chieu nay la gi" cho server.
 *
 * Bao loi nguoi choi 05/11/2026 (dong Squirtle, Blastoise cap 64): "skill con rua chu dong hay bi dong ma dung khong
 * duoc, tang diem lau roi ma khong cai vao auto duoc". Chieu do la Rut vao mai (o 5): CharManager.SKILL_CAN_BUFF_TO_USER
 * = -1 -&gt; CharManager.isBuffActive = false, tuc chieu BI DONG - da hoc la luon co hieu luc (CharChiSo: +X% phong thu
 * theo cap chieu), khong bam duoc, khong gan o nhanh / Tu dong danh duoc (client OChieuNhanh.ganDuoc, BangAuto,
 * TuBuff.KHONG_DUNG_DUOC). Hieu ung van chay dung; loi la KHONG CHO NAO noi cho nguoi choi biet: bang ky nang khong ghi
 * "bi dong", bang Auto cu bam vao khong bao gi (im lang), server tra "Không thể sử dụng" chung chung.
 *
 * Moi dong co dung mot chieu bi dong (o {@link #O}); hieu ung lay tu cho game that su ap:
 * <ul>
 * <li>Charmander Man khoi (o 4): Char.getBuffEffAttack - X% co hoi danh xuyen giap;</li>
 * <li>Bulbasaur Sinh truong (o 5): CharChiSo tan cong +X%;</li>
 * <li>Eevee Nghi ngoi (o 5): CharChiSo PP toi da +X%;</li>
 * <li>Squirtle Rut vao mai (o 5): CharChiSo phong thu +X%;</li>
 * <li>Pichu Nap dien (o 5): Char.buffAttackSkill doc cua Nhanh nhen +X%.</li>
 * </ul>
 * X = CharManager.SKILL_DAM_PERCENT[dong][o][cap chieu]. KiemChieuBiDong doi chieu voi bang CharManager.
 */
public final class ChieuBiDong {

    private ChieuBiDong() {
    }

    /** O chieu bi dong theo dong (index = charClass). */
    public static final int[] O = {4, 5, 5, 5, 5};

    /** Ten chieu bi dong (khop MessageCreator.SKILL_NAME / client PokeChieu.TEN). */
    public static final String[] TEN = {"Màn khói", "Sinh trưởng", "Nghỉ ngơi", "Rút vào mai", "Nạp điện"};

    /** Hieu ung theo dong, "{X}" = phan tram theo cap chieu. */
    static final String[] HIEU_UNG = {
        "{X}% cơ hội mỗi đòn đánh xuyên giáp",
        "+{X}% tấn công",
        "+{X}% PP tối đa",
        "+{X}% phòng thủ",
        "độc của Nhanh nhẹn mạnh thêm {X}%",
    };

    /** Chieu {@code o} cua dong {@code clazz} la bi dong (bo tro khong dung duoc - SKILL_CAN_BUFF_TO_USER == -1). */
    public static boolean la(final int clazz, final int o) {
        try {
            final int b = o - 4;
            if (clazz < 0 || clazz >= CharManager.SKILL_CAN_BUFF_TO_USER.length || b < 0
                    || b >= CharManager.SKILL_CAN_BUFF_TO_USER[clazz].length) {
                return false;
            }
            return CharManager.SKILL_CAN_BUFF_TO_USER[clazz][b] == -1;
        } catch (final Exception e) {
            return false;
        }
    }

    /** Hieu ung bo tro {@code buffEff} cua dong la hieu ung cua chieu bi dong. */
    public static boolean laHieuUng(final int clazz, final int buffEff) {
        try {
            final int b = CharManager.getSkillBuffByID(clazz, buffEff);
            return b >= 0 && la(clazz, b + 4);
        } catch (final Exception e) {
            return false;
        }
    }

    /** Phan tram hieu ung o cap chieu {@code lv} (0 neu chua hoc). */
    public static int phanTram(final int clazz, final int lv) {
        try {
            if (lv <= 0) {
                return 0;
            }
            final short[] t = CharManager.SKILL_DAM_PERCENT[clazz][O[clazz]];
            return t[Math.max(0, Math.min(t.length - 1, lv))];
        } catch (final Exception e) {
            return 0;
        }
    }

    /** "+65% phòng thủ" (cap chieu {@code lv}). */
    public static String hieuUng(final int clazz, final int lv) {
        if (clazz < 0 || clazz >= HIEU_UNG.length) {
            return "";
        }
        return HIEU_UNG[clazz].replace("{X}", String.valueOf(phanTram(clazz, lv)));
    }

    /** Cap chieu bi dong hien tai cua nhan vat (ca cap cong them). */
    public static int cap(final Char p) {
        return ChieuHoTro.capChieu(p, O[p.charClass]);
    }

    /** Cau chinh: "Rút vào mai là chiêu bị động". */
    public static String cauChinh(final int clazz) {
        return (clazz >= 0 && clazz < TEN.length ? TEN[clazz] : "Chiêu này") + " là chiêu bị động";
    }

    /** Cau phu: "Đã học là luôn có hiệu lực (+65% phòng thủ), không cần bấm, không cài vào Tự động đánh". */
    public static String cauPhu(final int clazz, final int lv) {
        final String hu = lv > 0 ? "Đã học là luôn có hiệu lực (" + hieuUng(clazz, lv) + ")" : "Học xong là luôn có hiệu lực";
        return hu + ", không cần bấm, không cài vào Tự động đánh";
    }
}
