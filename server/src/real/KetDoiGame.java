package real;

import com.htbb.pokemon.KetDoi;
import java.util.ArrayList;
import java.util.List;

/**
 * Noi com.htbb.pokemon.KetDoi (dieu kien Le ket doi, nhom trung) voi Char / Party cua game.
 * Thay Party.checkConditionMarry (htbb-core.jar, chi tra true/false) bang ly do ro rang cho nguoi choi.
 * Chi goi o NPC Le ket doi (MapDoMenu case -27): KHONG xet lai cap da ket doi (tien hoa, dang nhap...).
 */
public final class KetDoiGame {

    private KetDoiGame() {
    }

    /** Loai hien tai cua Pokemon nguoi choi (pokeDex; nhan vat cu chua co thi dang khoi dau theo cap). */
    static int loaiHienTai(final Char p) {
        if (p.pokeDex > 0) {
            return p.pokeDex;
        }
        final int dau = com.htbb.pokemon.LoaiPokemon.khoiDau(p.charClass);
        return dau <= 0 ? 0 : com.htbb.pokemon.LoaiPokemon.dangTheoCap(dau, p.lvDetail != null ? p.lvDetail.lv : 1);
    }

    static KetDoi.Ben ben(final Char p) {
        return new KetDoi.Ben(p.charname, loaiHienTai(p), p.gender, p.lvDetail != null ? p.lvDetail.lv : 0,
                p.married != 0);
    }

    /** Nap bang nhom trung neu chua nap (khoi dong / lan dau dung). */
    static void damBaoNap() {
        if (!KetDoi.daNap()) {
            KetDoi.nap();
        }
    }

    /** null = du dieu kien (tru xu / luong); nguoc lai la ly do tu choi. */
    static String lyDoTuChoi(final Char nguoiDangKy) {
        damBaoNap();
        List<KetDoi.Ben> nhom = null;
        if (nguoiDangKy.party != null && nguoiDangKy.party.userParty != null) {
            nhom = new ArrayList<KetDoi.Ben>();
            for (final Char c : nguoiDangKy.party.userParty) {
                if (c != null) {
                    nhom.add(ben(c));
                }
            }
        }
        return KetDoi.lyDoTuChoi(ben(nguoiDangKy), nhom);
    }

    /** Ban cung nhom (nhom 2 nguoi) cua nguoi dang ky, null neu khong co. */
    static Char banCungNhom(final Char p) {
        if (p.party == null || p.party.userParty == null || p.party.userParty.size() != 2) {
            return null;
        }
        final Char a = p.party.userParty.get(0);
        return a == p ? p.party.userParty.get(1) : a;
    }

    /** Cau nhom trung cua hai ben (vd "... cùng nhóm Quái vật - có thể kết đôi."). */
    static String moTaNhomTrung(final Char a, final Char b) {
        damBaoNap();
        return KetDoi.xet(loaiHienTai(a), loaiHienTai(b)).loiNhan;
    }
}
