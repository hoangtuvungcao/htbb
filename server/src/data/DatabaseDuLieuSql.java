package data;

import com.htbb.cauhinh.CauHinhTrungTam;
import com.htbb.dulieu.DuLieuSql;
import com.htbb.dulieu.NguonJdbc;
import java.sql.Connection;

/**
 * Nap DU LIEU THIET KE tu CSDL luc khoi dong (com.htbb.dulieu.DuLieuSql, docs/kien-truc.md muc 4.4) qua pool chinh
 * {@link Database#connPool}. Goi MOT lan trong TeamServer NGAY SAU kiemTraCSDL va TRUOC SuKien.nap /
 * ChuyenDoiLauDai / Database.startSaveChar: cac buoc do dung bang EXP, lich su kien, so kinh te da nap.
 *
 * Bang quan trong (data_cap_do_lau_dai, data_nang_cap_lau_dai, data_kinh_te_lau_dai, data_su_kien_nam,
 * cau_hinh_danh_muc + cau_hinh_nhom) thieu / rong / sai hinh dang -> in ro bang nao, ly do, cach sua roi DUNG server
 * (ma thoat 1, systemd bao failed). Chay tiep voi so 0 se doi EXP nhan vat sai (ChuyenDoiLauDai) - te hon khong chay.
 * Bang phu (data_hst_ho_cau, data_noi_dung_trang_bi) thieu chi canh bao: goi y diem cau / trang bi giftcode tat.
 */
public final class DatabaseDuLieuSql {

    private DatabaseDuLieuSql() {
    }

    /** Nguon doc qua pool chinh (moi bang mot ket noi, tra lai pool ngay). */
    public static NguonJdbc nguon() {
        return new NguonJdbc(new NguonJdbc.KetNoi() {
            @Override
            public Connection lay() throws Exception {
                return Database.connPool.getConFromPool();
            }

            @Override
            public void tra(final Connection c) {
                if (c != null) {
                    try {
                        Database.connPool.free(c);
                    } catch (final Exception ignored) {
                    }
                }
            }
        }, "CSDL game");
    }

    /** Nap + kiem; hong bang quan trong thi dung server. */
    public static void napLucKhoiDong() {
        final DuLieuSql.KetQua kq = DuLieuSql.napSoLieu(nguon());
        final String loiDm = CauHinhTrungTam.loiDanhMuc();
        if (loiDm != null) {
            kq.loi.add(0, loiDm);
        }
        for (final String s : kq.ok) {
            System.out.println("[DU LIEU SQL] ok " + s);
        }
        for (final String s : kq.canhBao) {
            System.out.println("[DU LIEU SQL] CANH BAO " + s);
        }
        if (kq.dat()) {
            System.out.println("[DU LIEU SQL] da nap " + kq.ok.size() + " bang so lieu tu CSDL"
                    + (kq.canhBao.isEmpty() ? "" : ", " + kq.canhBao.size() + " canh bao"));
            return;
        }
        System.out.println();
        System.out.println("!!!!!!!!!! THIEU DU LIEU SQL QUAN TRONG - DUNG SERVER !!!!!!!!!!");
        for (final String s : kq.loi) {
            System.out.println("  LOI      : " + s);
        }
        System.out.println("  cach sua : nap tep migration P1 vao CSDL game roi khoi dong lai:");
        System.out.println("               mysql htbb_db < sql/them_p1_du_lieu_sql.sql");
        System.out.println("             (CSDL moi: mysql < sql/htbb_day_du.sql da gom san). Xem sql/README.md.");
        System.out.println("  (khong chay tiep voi bang rong: bang EXP / lich su kien / danh muc cau hinh = 0 se lam");
        System.out.println("   sai du lieu nhan vat; systemd se bao 'failed' dung su that)");
        System.out.println();
        System.out.flush();
        System.exit(1);
    }
}
