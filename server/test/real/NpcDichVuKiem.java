package real;

/** Cua so test vao NpcDichVu (Arcanine gac cong) - test/KiemDiaDiemNpc. */
public final class NpcDichVuKiem {

    private NpcDichVuKiem() {
    }

    public static int[][] loiDi() {
        return NpcDichVu.LOI_DI;
    }

    public static int soLoi(final int tuLoad) {
        return NpcDichVu.loiDi(tuLoad).size();
    }

    public static boolean laKhuXa(final int load) {
        return NpcDichVu.laKhuXa(load);
    }
}
