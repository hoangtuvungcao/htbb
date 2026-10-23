package real;

import com.htbb.pokemon.ThuongLenCap;
import data.Database;
import data.DatabaseHocChieu;

/**
 * BU DIEM LEN CAP LUC NAP NHAN VAT (sua bao loi 23/10/2026). Goi tu CharNapDuLieu.initInfoFromDB sau khi doc chi so /
 * chieu, TRUOC chot chong sua chi so va HocChieuGame.khiNap:
 * <ol>
 *   <li>cap da len ma chua duoc thuong (Char.capChuaThuongKhiNap - truoc day initPinfo nang lastLV im lang): cong dung
 *       luat cu cho tung cap (+5 tiem nang, +1 moi chi so, +1 ky nang);</li>
 *   <li>bu phan con thieu theo cong thuc {@link ThuongLenCap} (nhan vat da mat diem truoc ban sua): chi cong, khong tru;
 *       bu xong thi thieu = 0 nen nap lai khong cong lan hai (khong can co rieng).</li>
 * </ol>
 * Moi lan bu: ghi tob_other_log 'budiemlencap' + mot dong tob_chuyen_doi_can_bang (bao=...) de HocChieuGame.sauVaoGame
 * bao cho nguoi choi va luu ngay. Luat moi (Diem huan luyen / hoc chieu theo loai) tinh lai tu lastLV ngay sau
 * (HocChieuGame.khiNap) nen buoc nay khong doi gi o do.
 */
public final class ThuongLenCapNap {

    private ThuongLenCapNap() {
    }

    /** @return {tiem nang da cong, ky nang da cong} */
    public static int[] khiNap(final Char self) {
        final int[] kq = new int[2];
        if (self == null || self.dbBasic == null || self.isBot != -1 || self.charDBID <= 0) {
            return kq;
        }
        try {
            final int d = self.capChuaThuongKhiNap;
            self.capChuaThuongKhiNap = 0;
            if (d > 0) {
                self.basepoint = (short) Math.min(Short.MAX_VALUE, self.basepoint + ThuongLenCap.TIEM_NANG_MOI_CAP * d);
                self.strength = (short) Math.min(Short.MAX_VALUE, self.strength + d);
                self.agitity = (short) Math.min(Short.MAX_VALUE, self.agitity + d);
                self.spirit = (short) Math.min(Short.MAX_VALUE, self.spirit + d);
                self.health = (short) Math.min(Short.MAX_VALUE, self.health + d);
                self.luck = (short) Math.min(Short.MAX_VALUE, self.luck + d);
                self.skillpoint = (short) Math.min(Short.MAX_VALUE, self.skillpoint + d);
                kq[0] += (ThuongLenCap.TIEM_NANG_MOI_CAP + ThuongLenCap.SO_CHI_SO) * d;
                kq[1] += d;
            }
            final byte[] g = Database.basicPoint[self.charClass];
            final int thieuTn = ThuongLenCap.thieuTiemNang(self.lastLV,
                    new int[]{self.strength, self.agitity, self.spirit, self.health, self.luck},
                    new int[]{g[0], g[1], g[2], g[3], g[4]}, self.basepoint);
            if (thieuTn > 0) {
                self.basepoint = (short) Math.min(Short.MAX_VALUE, self.basepoint + thieuTn);
                kq[0] += thieuTn;
            }
            // Hoc chieu theo loai: diem ky nang tinh lai tu lastLV trong khiNap - khong bu kieu cu.
            if (!HocChieuGame.batChieu()) {
                final int thieuKn = ThuongLenCap.thieuKyNang(self.lastLV, self.skill, self.skillpoint);
                if (thieuKn > 0) {
                    self.skillpoint = (short) Math.min(Short.MAX_VALUE, self.skillpoint + thieuKn);
                    kq[1] += thieuKn;
                }
            }
            if (kq[0] > 0 || kq[1] > 0) {
                ghi(self, d, kq);
            }
        } catch (final Exception e) {
            System.out.println("[LEN CAP] LOI bu diem khi nap " + self.charname + ": " + e);
        }
        return kq;
    }

    static void ghi(final Char self, final int capChuaThuong, final int[] kq) {
        final String kt = "cap " + self.lvDetail.lv + " lastLV " + self.lastLV + " class " + self.charClass + " chua thuong "
                + capChuaThuong + " cap, +" + kq[0] + " tiem nang, +" + kq[1] + " ky nang -> bp " + self.basepoint + " sp "
                + self.skillpoint + " SM/KL/TT/SK/MM " + self.strength + "/" + self.agitity + "/" + self.spirit + "/"
                + self.health + "/" + self.luck;
        System.out.println("[LEN CAP] bu diem " + self.charname + ": " + kt);
        try {
            Database.instance.saveOrtherLog("", self.charname, "bu diem len cap: " + kt, "budiemlencap");
        } catch (final Exception ignored) {
        }
        final StringBuilder bao = new StringBuilder();
        if (HocChieuGame.batHuanLuyen()) {
            // Diem huan luyen / diem ky nang tinh lai tu lastLV ngay sau (khiNap): chi bao khi that su co cap chua thuong.
            if (capChuaThuong <= 0) {
                return;
            }
            bao.append("Đã cộng điểm cho ").append(capChuaThuong).append(" cấp chưa được thưởng (Pokémon → Chỉ số → Huấn luyện).");
        } else {
            bao.append("Đã bù điểm lên cấp còn thiếu:");
            if (kq[0] > 0) {
                bao.append(" +").append(kq[0]).append(" điểm tiềm năng");
            }
            if (kq[1] > 0) {
                bao.append(kq[0] > 0 ? "," : "").append(" +").append(kq[1]).append(" điểm kỹ năng");
            }
            bao.append(" (Pokémon → Chỉ số).");
        }
        // buoc VARCHAR(24): "bu_diem_" + yyyyMMddHHmmss = 22 ky tu. Chi de bao + luu ngay (sauVaoGame); viec "mot lan"
        // do cong thuc dam bao (bu xong thieu = 0), khong phu thuoc dong nay.
        final String buoc = "bu_diem_" + new java.text.SimpleDateFormat("yyyyMMddHHmmss").format(new java.util.Date());
        try {
            DatabaseHocChieu.themBuoc(self.charDBID, buoc, "bao=" + bao + "|kt=" + kt, 0L);
        } catch (final Exception ignored) {
        }
    }
}
