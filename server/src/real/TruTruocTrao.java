package real;

import java.util.concurrent.Callable;

/**
 * TRU TRUOC, TRAO SAU (29/10/2026, docs/kiem-thu/ma-tran-bot.md "Lo hong uu tien" #2/#3/#5).
 *
 * Mau chung cho moi duong "tra xu lay ket qua": kiem-va-tru xu trong MOT buoc duoi Char.KHOA_TIEN
 * ({@link Char#truXuNeuDu}) TRUOC khi trao bat cu thu gi; buoc trao that bai (tra false) hoac nem ngoai le thi HOAN
 * dung so vua tru. Cac handler cua cung mot nhan vat chay tren HAI luong (luong phien cho lenh trong
 * RealController.privateCmd, luong ban do cho lenh con lai), nen kieu cu "getxu() &lt; gia ... trao ... subXu(gia)" de
 * mot lenh tieu xu khac xen vao giua: subXu chan ve 0, ket qua da trao = mien phi (va voi cho, nguoi ban da duoc cong =
 * xu sinh ra).
 */
public final class TruTruocTrao {

    private TruTruocTrao() {
    }

    /**
     * Tru {@code phi} (XU_DEM, dem top) roi chay {@code trao}. {@code trao} tra false hoac nem ngoai le -&gt; hoan phi
     * (XU_HOAN: tru lai xu_ra da dem). Thieu xu -&gt; {@code trao} KHONG chay.
     *
     * @return {@link KetQua#THIEU_XU}, {@link KetQua#HONG} (da hoan) hoac {@link KetQua#XONG}
     */
    public static KetQua voiPhi(final Char p, final long phi, final String viTri, final Callable<Boolean> trao) throws Exception {
        if (phi < 0L) {
            return KetQua.THIEU_XU;
        }
        if (!p.truXuNeuDu(phi, true, viTri)) {
            return KetQua.THIEU_XU;
        }
        boolean xong = false;
        try {
            final Boolean r = trao.call();
            xong = r != null && r.booleanValue();
        } finally {
            if (!xong && phi > 0L) {
                p.addXu(phi, "hoan " + viTri, Char.XU_HOAN);
            }
        }
        return xong ? KetQua.XONG : KetQua.HONG;
    }

    public enum KetQua {
        THIEU_XU, HONG, XONG
    }

    // ------------------------------------------------------------------ cho (95 / -32)

    /** Cho: tru xu nguoi mua (chuyen cho nguoi ban - XU_CHUYEN, khong dem kinh te) TRUOC khi rut mon / cong nguoi ban. */
    public static boolean truNguoiMuaCho(final Char nguoiMua, final long gia) {
        return gia > 0L && nguoiMua.truXuNeuDu(gia, false, "offline map 2", Char.XU_CHUYEN);
    }

    /** Hoan cho nguoi mua khi mon khong con (da tru bang {@link #truNguoiMuaCho}). */
    public static void hoanNguoiMuaCho(final Char nguoiMua, final long gia) {
        if (gia > 0L) {
            nguoiMua.addXu(gia, "hoan mua cho", Char.XU_CHUYEN);
        }
    }

    // ------------------------------------------------------------------ gop quy doi (-20 / popup 114)

    /**
     * Gop quy doi: tru dung {@code xu} cua nguoi gop TRUOC (kiem-va-tru mot buoc); chi khi tru duoc moi duoc cong dung
     * so do vao quy. Ban cu kiem getxu() &lt; money roi subXu (chan ve 0) roi cong money vao quy: tieu xu o luong khac
     * giua hai buoc -&gt; quy duoc cong nhieu hon so that bi tru.
     */
    public static boolean truGopQuy(final Char p, final long xu) {
        return xu > 0L && p.truXuNeuDu(xu, false, "map 777");
    }
}
