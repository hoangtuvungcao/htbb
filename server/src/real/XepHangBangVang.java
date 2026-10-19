package real;

/**
 * 0.9.6.1 "loi tai" Bang vang: man Xep hang (client game.pokemon.ManXepHang, tu ban 903) cham tab "Bang vang" gui top
 * kieu 7 (lenh -19) va CHO mot danh sach -19 kieu 7. Server cu tra menu -30 (-504, 10 muc top theo vung) - man Xep
 * hang khong ve menu do nen treo "Dang tai bang xep hang..." mai mai. Client &gt;= 903: tra thang danh sach top cong
 * trang vung cua minh (Map.doSendTopNation muc 3..5, dung cot "cong trang" man do hien); client cu / Unity (-1): giu
 * menu nhu truoc. Goi tu MapXuLyLenh.doSendTop; test/KiemTaiDuLieu096.
 */
public final class XepHangBangVang {
    /** Ban client dau tien co man Xep hang (0.9.3 = BAN 903). */
    public static final int BAN_MAN_XEP_HANG = 903;

    private XepHangBangVang() {
    }

    /** @return muc Map.doSendTopNation can gui ngay, -1 = gui menu -504 nhu cu */
    public static int muc(final int banClient, final int quocGia) {
        if (banClient < BAN_MAN_XEP_HANG) {
            return -1;
        }
        return 3 + (quocGia >= 0 && quocGia < 3 ? quocGia : 0);
    }
}
