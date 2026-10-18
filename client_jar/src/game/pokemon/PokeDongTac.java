package game.pokemon;

/** Mot dong tac trong PokeDuLieu (ban J2ME cua lop PokeDongTac trong PokeDuLieu.cs). */
public class PokeDongTac {
    public int rongKhung;
    public int caoKhung;
    public int soKhung;
    public int[] thoiLuong;
    public int tongThoiLuong;
    public int yAtlas;
    /** Cot bat dau cua dong tac trong atlas (phien ban 1 luon 0). */
    public int xAtlas;
    public int[] dxChan = new int[4];
    public int[] dyChan = new int[4];

    /** Khung can ve sau t (don vi 1/60 s) ke tu luc bat dau dong tac. */
    public int khungTai(int t, boolean lap) {
        if (t < 0) {
            t = 0;
        }
        if (t >= tongThoiLuong) {
            if (!lap) {
                return soKhung - 1;
            }
            t %= tongThoiLuong;
        }
        for (int i = 0; i < soKhung; i++) {
            if (t < thoiLuong[i]) {
                return i;
            }
            t -= thoiLuong[i];
        }
        return soKhung - 1;
    }

    /** Toa do y cua hang huong dir trong atlas (dir theo client: DOWN0 UP1 LEFT2 RIGHT3). */
    public int yHang(int dir) {
        if (dir < 0 || dir > 3) {
            dir = 0;
        }
        return yAtlas + dir * caoKhung;
    }
}
