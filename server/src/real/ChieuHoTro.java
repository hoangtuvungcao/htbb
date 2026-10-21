package real;

import com.htbb.pokemon.LauDai;

/**
 * Chieu ho tro / phong thu (lenh 51: giap, khien, phan don, khien PP, tang HP, hoi sinh) - MOT cho duy nhat
 * quyet thoi luong, hoi chieu, gia PP, luat dung lai va het han.
 *
 * Loi "giap vo han" (chu game 0.9.6.1): ban cu (CharCapNhat.setBuff2Char) chia doi hoi chieu moi chieu ho tro
 * tru hoi sinh ({@code * 100 / div}, div = 2), lay thoi luong tu bang kiem hiep TIME_LIFE_BUFF_SKILL (50..210
 * giay, sai hang theo o chieu) trong khi hoi chieu sau khi chia chi 15..30 giay -> hieu ung con ma chieu da hoi
 * xong, dung lai lam moi -> giap 100% thoi gian. Them nua tru PP theo o {@code buff} (o 0 = danh thuong, 0 PP)
 * thay vi o that {@code buff + 4} -> chieu ho tro dau tien cua moi dong mien phi; tru PP truoc khi kiem hoi
 * chieu -> bi tu choi van mat PP.
 *
 * Luat moi:
 * <ul>
 * <li>thoi luong = bang thiet ke {@link #THOI_LUONG_THIET_KE} (khop data_noi_dung_chieu.vai_tro) khi bat
 * sv.lau_dai, khong thi bang cu theo cap cua CHINH o chieu;</li>
 * <li>hoi chieu hieu qua = max(bang, thoi luong + bien), bien = max(5 giay, 25% thoi luong) - du lieu sai
 * (hoi chieu &lt; thoi luong) cung khong tao lai giap vo han;</li>
 * <li>muc tieu dang co dung hieu ung do -> tu choi (khong cong don, khong lam moi);</li>
 * <li>het han: go chi so, bao client xoa bieu tuong (lenh 51 kieu 0).</li>
 * </ul>
 */
public final class ChieuHoTro {

    private ChieuHoTro() {
    }

    /** Vi tri hieu ung trong arrayBuff / timeOut / countDown (giong client Char.BUFFTYPE). */
    public static final byte[] VI_TRI = {19, 20, 22, 23, 24, 25, 27};

    /** Hieu ung hoi sinh: thoi luong la hinh, khong tinh luat uptime. */
    public static final int HOI_SINH = 27;

    /** Bien toi thieu giua het hieu ung va het hoi chieu (mili giay). */
    public static final long BIEN_TOI_THIEU_MS = 5000L;

    /**
     * Thoi luong thiet ke (giay) theo [dong][o chieu]; 0 = khong co, dung bang cu. Nguon: data_noi_dung_chieu
     * (vai_tro "... N giây (Tên)"), thiet_ke_can_bang. KiemGiapVoHan doi chieu bang nay voi SQL.
     */
    public static final int[][] THOI_LUONG_THIET_KE = {
        {0, 0, 0, 0, 10, 30, 0, 0, 0, 0, 0}, // Charmander: Màn khói, Vũ điệu kiếm 30 s
        {0, 0, 0, 0, 8, 30, 0, 0, 0, 0, 0},  // Bulbasaur: Tự vệ 8 s (giáp), Sinh trưởng 30 s
        {0, 0, 0, 0, 10, 2, 3, 10, 0, 0, 0}, // Eevee: Mong ước, Nghỉ ngơi, hồi sinh (hình), Chia sẻ nỗi đau (khiên PP)
        {0, 0, 0, 0, 20, 10, 0, 0, 0, 0, 0}, // Squirtle: Phá mai 20 s, Rút vào mai 10 s
        {0, 0, 0, 0, 10, 10, 0, 0, 0, 0, 0}, // Pichu: Nhanh nhẹn 10 s, Nạp điện
    };

    /** Ket qua {@link #dung}. */
    public static final int OK = 0;
    public static final int KHONG_CO = 1;
    public static final int CHUA_HOC = 2;
    public static final int DANG_HOI = 3;
    public static final int THIEU_PP = 4;
    public static final int DANG_HIEU_LUC = 5;

    /** Vi tri hieu ung trong mang buff cua nhan vat, -1 neu khong co. */
    public static int viTri(final int buffEff) {
        for (int j = 0; j < VI_TRI.length; j++) {
            if (VI_TRI[j] == buffEff) {
                return j;
            }
        }
        return -1;
    }

    /** O chieu (4..) cua hieu ung trong dong, -1 neu dong khong co. */
    public static int oChieu(final int clazz, final int buffEff) {
        try {
            final int b = CharManager.getSkillBuffByID(clazz, buffEff);
            return b < 0 ? -1 : b + 4;
        } catch (final Exception e) {
            return -1;
        }
    }

    public static int capChieu(final Char p, final int o) {
        try {
            final int add = p.addMoreLevelSkill != null && o < p.addMoreLevelSkill.length ? p.addMoreLevelSkill[o] : 0;
            return p.skill[o] + add;
        } catch (final Exception e) {
            return 0;
        }
    }

    private static int kep(final int lv, final int dai) {
        return Math.max(0, Math.min(dai - 1, lv));
    }

    /** Thoi luong hieu ung (giay) cua o chieu {@code o} cap {@code lv}. */
    public static int thoiLuongGiay(final int clazz, final int o, final int lv, final int buffEff) {
        try {
            if (LauDai.bat() && clazz >= 0 && clazz < THOI_LUONG_THIET_KE.length && o >= 0
                    && o < THOI_LUONG_THIET_KE[clazz].length && THOI_LUONG_THIET_KE[clazz][o] > 0) {
                return THOI_LUONG_THIET_KE[clazz][o];
            }
            final int j = viTri(buffEff);
            if (j < 0 || j >= CharManager.TIME_LIFE_BUFF_SKILL.length) {
                return 0;
            }
            final short[] h = CharManager.TIME_LIFE_BUFF_SKILL[j];
            return Math.max(0, h[kep(lv, h.length)]);
        } catch (final Exception e) {
            return 0;
        }
    }

    /** Hoi chieu theo bang (mili giay), khong chia. */
    public static long hoiChieuBangMs(final int clazz, final int o, final int lv) {
        try {
            final short[] cd = CharManager.SKILL_COOLDOWN[clazz][o];
            return Math.max(0, cd[kep(lv, cd.length)]) * 100L;
        } catch (final Exception e) {
            return 0L;
        }
    }

    /** Hoi chieu hieu qua (mili giay): khong bao gio ngan hon thoi luong + bien. */
    public static long hoiChieuMs(final int clazz, final int o, final int lv, final int thoiLuongGiay) {
        final long bang = hoiChieuBangMs(clazz, o, lv);
        final long tl = Math.max(0, thoiLuongGiay) * 1000L;
        if (tl <= 0) {
            return bang;
        }
        return Math.max(bang, tl + Math.max(BIEN_TOI_THIEU_MS, tl / 4));
    }

    /** Hoi chieu hieu qua cua o chieu ho tro {@code o} (cho goi tin 35); -1 neu khong phai o ho tro. */
    public static long hoiChieuOMs(final int clazz, final int o, final int lv) {
        try {
            final int b = o - 4;
            if (b < 0 || b >= CharManager.EFF_BUFF_SKILL[clazz].length) {
                return -1L;
            }
            final int eff = CharManager.EFF_BUFF_SKILL[clazz][b];
            return hoiChieuMs(clazz, o, lv, eff == HOI_SINH ? 0 : thoiLuongGiay(clazz, o, lv, eff));
        } catch (final Exception e) {
            return -1L;
        }
    }

    /** Gia PP cua o chieu ho tro (o that, khong phai o danh thuong). */
    public static int giaPP(final int clazz, final int o, final int lv) {
        try {
            final short[] mp = CharManager.SKILL_MP[clazz][o];
            return Math.max(0, mp[kep(lv, mp.length)]);
        } catch (final Exception e) {
            return 0;
        }
    }

    /** Hieu ung {@code buffEff} dang con tren {@code c}. */
    public static boolean dangCo(final Char c, final int buffEff) {
        final int j = viTri(buffEff);
        return j >= 0 && c.arrayBuff != null && j < c.arrayBuff.length && c.arrayBuff[j] == (byte) buffEff;
    }

    /**
     * Kiem luat (khong doi trang thai): OK / ly do tu choi. {@code c} la muc tieu (co the la chinh {@code self}).
     */
    public static int kiem(final Char self, final Char c, final int buffEff, final long now) {
        final int o = oChieu(self.charClass, buffEff);
        final int j = viTri(buffEff);
        if (o < 0 || j < 0 || j >= self.arrayBuff.length) {
            return KHONG_CO;
        }
        final int lv = capChieu(self, o);
        if (lv <= 0) {
            return CHUA_HOC;
        }
        final int tl = buffEff == HOI_SINH ? 0 : thoiLuongGiay(self.charClass, o, lv, buffEff);
        if (now - self.timeLastUseSkills[o] < hoiChieuMs(self.charClass, o, lv, tl)) {
            return DANG_HOI;
        }
        if (self.mp + self.percentBuff[1] < giaPP(self.charClass, o, lv)) {
            return THIEU_PP;
        }
        if (buffEff != HOI_SINH && dangCo(c, buffEff)) {
            return DANG_HIEU_LUC;
        }
        return OK;
    }

    /** Con bao lau het hoi chieu (giay, lam tron len), 0 neu san sang. */
    public static long conHoiGiay(final Char self, final int buffEff, final long now) {
        final int o = oChieu(self.charClass, buffEff);
        if (o < 0) {
            return 0L;
        }
        final int lv = capChieu(self, o);
        final int tl = buffEff == HOI_SINH ? 0 : thoiLuongGiay(self.charClass, o, lv, buffEff);
        final long con = hoiChieuMs(self.charClass, o, lv, tl) - (now - self.timeLastUseSkills[o]);
        return con <= 0 ? 0L : (con + 999L) / 1000L;
    }

    /**
     * Ghi hieu ung len {@code c} sau khi {@link #kiem} = OK: tru PP, dat moc hoi chieu, thoi luong. Tra thoi luong
     * (giay) da dat. Ap chi so (upPercentAtribute) va gui goi tin la viec cua nguoi goi.
     */
    public static int ghi(final Char self, final Char c, final int buffEff, final long now) {
        final int o = oChieu(self.charClass, buffEff);
        final int j = viTri(buffEff);
        final int lv = capChieu(self, o);
        final int tl = thoiLuongGiay(self.charClass, o, lv, buffEff);
        self.mp -= giaPP(self.charClass, o, lv);
        if (self.mp < 0) {
            self.mp = 0;
        }
        self.timeLastUseSkills[o] = now;
        self.timeOut[j] = (short) Math.min(Short.MAX_VALUE, tl);
        c.timeUseBuff[j] = now;
        c.countDown[j] = 0;
        c.lvBuff[j] = (byte) lv;
        c.timeOut[j] = (short) Math.min(Short.MAX_VALUE, tl);
        c.arrayBuff[j] = (byte) buffEff;
        return tl;
    }

    /** Goi tin 51 kieu 0: client xoa bieu tuong / hinh hieu ung (co short cuoi - client doc timeOut truoc). */
    public static io.Message goiXoa(final int charID, final int buffEff) {
        final io.Message m = new io.Message(51);
        try {
            m.dos.writeByte(0);
            m.dos.writeShort(charID);
            m.dos.writeByte(buffEff);
            m.dos.writeShort(0);
        } catch (final Exception e) {
        }
        return m;
    }

    /** Go chi so cua hieu ung (khi het han). */
    static void goChiSo(final Char self, final int buffEff) {
        if (buffEff == 25) {
            self.percentBuff[1] = 0;
            self.percentBuff[2] = 0;
            self.calculatorHPMP();
        } else if (buffEff == 20) {
            self.percentBuff[0] = 0;
            if (self.percentBuffPlayer != null) {
                self.percentBuffPlayer[0] = 0;
            }
        }
    }

    /**
     * Het han hieu ung theo dong ho (goi moi nhip CharVongLap). Het han = go chi so + bao client (chinh minh va
     * nguoi gan) xoa bieu tuong. Tra so hieu ung vua het.
     */
    public static int capNhatHetHan(final Char self, final long now) {
        if (self.arrayBuff == null) {
            return 0;
        }
        int het = 0;
        for (int j = 0; j < self.arrayBuff.length; ++j) {
            final int eff = self.arrayBuff[j];
            if (eff == -1) {
                continue;
            }
            final long troi = now - self.timeUseBuff[j];
            final long han = Math.max(0, self.timeOut[j]) * 1000L;
            if (troi < han) {
                self.countDown[j] = (short) Math.max(0L, Math.min(self.timeOut[j], troi / 1000L));
                continue;
            }
            het++;
            self.arrayBuff[j] = -1;
            self.countDown[j] = 0;
            try {
                goChiSo(self, eff);
                if (eff == 25 || eff == 20) {
                    self.sendMessage(MessageCreator.createMainCharInfoMessage(self));
                }
            } catch (final Exception e) {
            }
            try {
                final io.Message m = goiXoa(self.id, eff);
                self.sendMessage(m);
                self.sendToNearPlayer(m);
            } catch (final Exception e) {
            }
        }
        return het;
    }
}
