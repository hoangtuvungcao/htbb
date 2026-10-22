package real;

import com.htbb.pokemon.ChuThuongNhiemVu;
import data.InfoQuestTemplate;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Chu phan thuong nhiem vu -64 LUC GUI (nhanh sua-tuyen): doan sau "Phần thưởng:|" cua infogift sinh lai tu chinh dong
 * thuong cua InfoQuestTemplate (exp / xu / luong / luong khoa / vat pham cua DUNG lop nhan vat / thuoc) + thuong_khac chuoi
 * chinh / phu (NhiemVuEngine.thuongKhacCu) - chu nguoi choi thay luon khop thu duoc phat, ke ca khi CSDL bi sua tay. Loi bat ky:
 * gui nguyen chu CSDL. Luat: com.htbb.pokemon.ChuThuongNhiemVu.
 */
public final class ChuThuongNhiemVuGame {

    /** (mau, lop) -> infogift da thay (mau = identityHashCode: nap lai mau thi tu het han). */
    private static final ConcurrentHashMap<String, String> DEM = new ConcurrentHashMap<String, String>();

    private ChuThuongNhiemVuGame() {
    }

    /** infogift gui cho nhan vat {@code p}. */
    public static String infoGift(final InfoQuestTemplate info, final Char p) {
        if (info == null) {
            return "";
        }
        final String goc = info.infoGift == null ? "" : info.infoGift;
        if (goc.indexOf(ChuThuongNhiemVu.NHAN) < 0) {
            return goc;
        }
        final int lop = p == null ? -1 : p.charClass;
        final String khoa = System.identityHashCode(info) + ":" + lop + ":" + System.identityHashCode(NhiemVuEngine.THUONG_CU) + ":" + System.identityHashCode(NhiemVuEngine.MAU);
        final String co = DEM.get(khoa);
        if (co != null) {
            return co;
        }
        String kq = goc;
        try {
            final String tk = NhiemVuEngine.thuongKhacCu(info.main_sub, info.ma);
            final String dong = ChuThuongNhiemVu.dong(info.exp, info.gold, info.luong, info.luongLock, chuoi(info.idItemgift), lop,
                    chuoi(info.potiongift), chuoi(info.npotiongift), tk, new ChuThuongNhiemVu.Ten() {
                        @Override
                        public String ten(final int id) {
                            return tenVatPham(lop, id);
                        }
                    }, new ChuThuongNhiemVu.Ten() {
                        @Override
                        public String ten(final int id) {
                            return tenThuoc(id);
                        }
                    });
            kq = ChuThuongNhiemVu.thayPhanThuong(goc, dong);
        } catch (final Exception e) {
            kq = goc;
        }
        if (DEM.size() > 20000) {
            DEM.clear();
        }
        DEM.put(khoa, kq);
        return kq;
    }

    static String chuoi(final short[] a) {
        if (a == null || a.length == 0) {
            return "32000";
        }
        final StringBuilder b = new StringBuilder();
        for (int i = 0; i < a.length; i++) {
            if (i > 0) {
                b.append(',');
            }
            b.append(a[i]);
        }
        return b.toString();
    }

    static String tenVatPham(final int lop, final int id) {
        try {
            for (final int l : new int[]{lop, 5}) {
                if (l >= 0 && l < Map.itemTemplates.size()) {
                    final ItemTemplates t = Map.itemTemplates.get(l).get(Integer.valueOf(id));
                    if (t != null && t.name != null) {
                        return t.name;
                    }
                }
            }
        } catch (final Exception ignored) {
        }
        return null;
    }

    static String tenThuoc(final int id) {
        try {
            for (final PotionTemplate2 t : Map.potionTemplates2) {
                if (t != null && t.id == id) {
                    return t.name;
                }
            }
        } catch (final Exception ignored) {
        }
        return null;
    }
}
