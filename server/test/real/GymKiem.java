package real;

import com.htbb.pokemon.HoSoLauDai;
import com.htbb.pokemon.NhiemVuMau;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/** Cua so test vao Gym / menu Quầy đổi thưởng / So tay (test/KiemDuongMenu, nhanh sua-duong-gym). */
public final class GymKiem {

    private GymKiem() {
    }

    public static List<Object[]> gym() {
        return GymLauDai.GYM;
    }

    public static void datGym(final List<Object[]> ds) {
        GymLauDai.GYM = ds;
    }

    public static int[] thuTu(final List<Object[]> ds, final int dau) {
        return GymLauDai.thuTu(ds, dau);
    }

    public static String thachDauStt(final Char p, final int stt) {
        return GymLauDai.thachDauStt(p, stt);
    }

    public static String chonSoTay(final Char p, final int i) {
        return GymLauDai.chon(p, i);
    }

    public static void moMenuGym(final Char p) {
        GymLauDai.moMenu(p);
    }

    public static int menuGym() {
        return SoTayLauDai.MENU_GYM;
    }

    public static int soMucGoc() {
        return SoTayLauDai.SO_MUC_GOC;
    }

    public static int[] nhomGoc() {
        return NhiemVuEngine.NHOM_GOC.clone();
    }

    public static void chonDangLam(final Char p, final String ma) {
        NhiemVuEngine.chonDangLam(p, ma);
    }

    public static void doMenuNhiemVu(final Char p, final int idMenu, final int op) {
        NhiemVuEngine.doMenu(p, idMenu, op);
    }

    public static String moTaCuaAi(final Char p) {
        final MucTieuNhiemVuGame.DongCuaAi d = MucTieuNhiemVuGame.cuaAiDangLam(p);
        return d == null ? null : d.moTa;
    }

    @SuppressWarnings("unchecked")
    public static LinkedHashMap<String, NhiemVuMau> datMau(final LinkedHashMap<String, NhiemVuMau> m) {
        final LinkedHashMap<String, NhiemVuMau> cu = NhiemVuEngine.MAU;
        NhiemVuEngine.MAU = m;
        return cu;
    }

    public static ConcurrentHashMap<String, long[]> tienDo(final Char p) {
        return NhiemVuEngine.td(p);
    }

    @SuppressWarnings("unchecked")
    public static void datHoSo(final Char p, final HoSoLauDai h) throws Exception {
        final java.lang.reflect.Field f = LauDaiNhanVat.class.getDeclaredField("CACHE");
        f.setAccessible(true);
        final ConcurrentHashMap<Integer, HoSoLauDai> c = (ConcurrentHashMap<Integer, HoSoLauDai>) f.get(null);
        if (h == null) {
            c.remove(Integer.valueOf(p.charDBID));
        } else {
            c.put(Integer.valueOf(p.charDBID), h);
        }
    }

    @SuppressWarnings("unchecked")
    public static void datTienDo(final Char p, final ConcurrentHashMap<String, long[]> t) throws Exception {
        final java.lang.reflect.Field f = NhiemVuEngine.class.getDeclaredField("TD");
        f.setAccessible(true);
        final ConcurrentHashMap<Integer, ConcurrentHashMap<String, long[]>> td = (ConcurrentHashMap<Integer, ConcurrentHashMap<String, long[]>>) f.get(null);
        if (t == null) {
            td.remove(Integer.valueOf(p.charDBID));
        } else {
            td.put(Integer.valueOf(p.charDBID), t);
        }
    }
}
