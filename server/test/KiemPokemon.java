import com.htbb.pokemon.HePokemon;
import com.htbb.pokemon.LoaiPokemon;

/**
 * Kiem thu nen tang chuyen doi Pokemon (com.htbb.pokemon).
 * Bang khac he duoc doi chieu bang SO LUONG o tung muc (51 x2, 61 x0.5, 8 x0
 * theo chuan Gen 6+) va mot so cap mau - go sai mot o la so luong lech ngay.
 */
public final class KiemPokemon {

    public static void chay() {
        Kiem.nhom("com.htbb.pokemon.HePokemon");

        int x2 = 0;
        int nua = 0;
        int khong = 0;
        for (int a = 0; a < HePokemon.SO_HE; a++) {
            for (int d = 0; d < HePokemon.SO_HE; d++) {
                final int h = HePokemon.heSoDon(a, d);
                if (h == 200) {
                    x2++;
                } else if (h == 50) {
                    nua++;
                } else if (h == 0) {
                    khong++;
                }
            }
        }
        Kiem.bang("so o x2", Integer.valueOf(51), Integer.valueOf(x2));
        Kiem.bang("so o x0.5", Integer.valueOf(61), Integer.valueOf(nua));
        Kiem.bang("so o x0", Integer.valueOf(8), Integer.valueOf(khong));
        Kiem.bang("du 18 ten", Integer.valueOf(HePokemon.SO_HE), Integer.valueOf(HePokemon.TEN.length));
        Kiem.bang("du 18 ten EN", Integer.valueOf(HePokemon.SO_HE), Integer.valueOf(HePokemon.TEN_EN.length));

        Kiem.bang("Nuoc -> Lua", Integer.valueOf(200), Integer.valueOf(HePokemon.heSoDon(HePokemon.NUOC, HePokemon.LUA)));
        Kiem.bang("Dien -> Dat", Integer.valueOf(0), Integer.valueOf(HePokemon.heSoDon(HePokemon.DIEN, HePokemon.DAT)));
        Kiem.bang("Rong -> Tien", Integer.valueOf(0), Integer.valueOf(HePokemon.heSoDon(HePokemon.RONG, HePokemon.TIEN)));
        Kiem.bang("Tien -> Rong", Integer.valueOf(200), Integer.valueOf(HePokemon.heSoDon(HePokemon.TIEN, HePokemon.RONG)));
        Kiem.bang("Thuong -> Thuong", Integer.valueOf(100), Integer.valueOf(HePokemon.heSoDon(HePokemon.THUONG, HePokemon.THUONG)));
        Kiem.bang("he loi -> 100", Integer.valueOf(100), Integer.valueOf(HePokemon.heSoDon(99, HePokemon.LUA)));

        // Hai he
        Kiem.bang("Bang -> Charizard? (Lua/Bay) = 100", Integer.valueOf(100),
                Integer.valueOf(HePokemon.heSoChinhXac(HePokemon.BANG, HePokemon.LUA, HePokemon.BAY)));
        Kiem.bang("Da -> Lua/Bay = 400", Integer.valueOf(400),
                Integer.valueOf(HePokemon.heSoChinhXac(HePokemon.DA, HePokemon.LUA, HePokemon.BAY)));
        Kiem.bang("Co -> Lua/Bay = 25", Integer.valueOf(25),
                Integer.valueOf(HePokemon.heSoChinhXac(HePokemon.CO, HePokemon.LUA, HePokemon.BAY)));
        Kiem.bang("Dat -> Lua/Bay = 0", Integer.valueOf(0),
                Integer.valueOf(HePokemon.heSoChinhXac(HePokemon.DAT, HePokemon.LUA, HePokemon.BAY)));
        Kiem.bang("he2 trung he1 khong nhan doi", Integer.valueOf(200),
                Integer.valueOf(HePokemon.heSoChinhXac(HePokemon.NUOC, HePokemon.LUA, HePokemon.LUA)));

        // Ban lam diu cho MMO
        Kiem.bang("trong game x4 -> 200", Integer.valueOf(200),
                Integer.valueOf(HePokemon.heSoTrongGame(HePokemon.DA, HePokemon.LUA, HePokemon.BAY)));
        Kiem.bang("trong game x2 -> 150", Integer.valueOf(150),
                Integer.valueOf(HePokemon.heSoTrongGame(HePokemon.NUOC, HePokemon.LUA, HePokemon.KHONG_CO)));
        Kiem.bang("trong game x0 -> 30", Integer.valueOf(30),
                Integer.valueOf(HePokemon.heSoTrongGame(HePokemon.DIEN, HePokemon.DAT, HePokemon.KHONG_CO)));
        Kiem.bang("trong game x0.25 -> 50", Integer.valueOf(50),
                Integer.valueOf(HePokemon.heSoTrongGame(HePokemon.CO, HePokemon.LUA, HePokemon.BAY)));

        Kiem.nhom("com.htbb.pokemon.LoaiPokemon");

        Kiem.bang("moi loai deu co ten", Integer.valueOf(LoaiPokemon.soLoai()), Integer.valueOf(LoaiPokemon.soTen()));
        final int[] dex = LoaiPokemon.tatCaDex();
        for (int i = 0; i < dex.length; i++) {
            Kiem.dung("co ten: " + dex[i], LoaiPokemon.ten(dex[i]).length() > 0);
            Kiem.dung("he1 hop le: " + dex[i], HePokemon.hopLe(LoaiPokemon.he1(dex[i])));
            for (int j = i + 1; j < dex.length; j++) {
                Kiem.dung("khong trung dex " + dex[i], dex[i] != dex[j]);
            }
            final int sau = LoaiPokemon.tienHoaKeTiep(dex[i], 100);
            if (sau != LoaiPokemon.KHONG) {
                Kiem.dung("dang sau cua " + dex[i] + " co trong bang", LoaiPokemon.coLoai(sau));
            }
        }

        Kiem.bang("Kiem khach -> Charmander", Integer.valueOf(4), Integer.valueOf(LoaiPokemon.khoiDau(0)));
        Kiem.bang("Phap su -> Eevee", Integer.valueOf(133), Integer.valueOf(LoaiPokemon.khoiDau(2)));
        Kiem.bang("Cung thu -> Pichu", Integer.valueOf(172), Integer.valueOf(LoaiPokemon.khoiDau(4)));
        Kiem.bang("class 5 -> KHONG", Integer.valueOf(LoaiPokemon.KHONG), Integer.valueOf(LoaiPokemon.khoiDau(5)));
        for (int c = 0; c < 5; c++) {
            Kiem.dung("khoi dau class " + c + " co trong bang", LoaiPokemon.coLoai(LoaiPokemon.khoiDau(c)));
        }

        Kiem.bang("Charmander cap 15 chua tien hoa", Integer.valueOf(LoaiPokemon.KHONG),
                Integer.valueOf(LoaiPokemon.tienHoaKeTiep(4, 15)));
        Kiem.bang("Charmander cap 16 -> Charmeleon", Integer.valueOf(5), Integer.valueOf(LoaiPokemon.tienHoaKeTiep(4, 16)));
        Kiem.bang("dangTheoCap(Charmander, 40) = Charizard", Integer.valueOf(6), Integer.valueOf(LoaiPokemon.dangTheoCap(4, 40)));
        Kiem.bang("dangTheoCap(Pichu, 1) = Pichu", Integer.valueOf(172), Integer.valueOf(LoaiPokemon.dangTheoCap(172, 1)));
        Kiem.bang("dangTheoCap(Pichu, 30) = Raichu", Integer.valueOf(26), Integer.valueOf(LoaiPokemon.dangTheoCap(172, 30)));
        Kiem.bang("Eevee khong tu tien hoa", Integer.valueOf(LoaiPokemon.KHONG), Integer.valueOf(LoaiPokemon.tienHoaKeTiep(133, 90)));
        Kiem.dung("Sylveon la nhanh Eevee", LoaiPokemon.laNhanhEevee(700));
        Kiem.sai("Pikachu khong phai nhanh Eevee", LoaiPokemon.laNhanhEevee(25));
        for (final int n : LoaiPokemon.NHANH_EEVEE) {
            Kiem.dung("nhanh Eevee " + n + " co trong bang", LoaiPokemon.coLoai(n));
        }
    }
}
