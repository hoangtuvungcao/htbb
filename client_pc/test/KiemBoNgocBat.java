import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Vector;
import javax.imageio.ImageIO;
import javax.microedition.lcdui.NenPc;
import javax.microedition.midlet.MIDlet;

/**
 * KIỂM GIAO DIỆN 0.9.8 BỘ / NGỌC / CHẾ TẠO / THU PHỤC (headless, không máy chủ, đường vẽ bản PC):
 * <ul>
 * <li>ManCheTao (danh sách + tấm chọn hệ Phiến đá), ManKhamNgoc (danh sách, thẻ chi tiết lỗ, túi ngọc): tấm canh giữa,
 *     trong vùng an toàn, mọi vùng chạm đủ cỡ / không chồng (ChupTrongGame.kiemTam / kiemCham), đủ nút, chữ tỉ lệ + pity;</li>
 * <li>nút "Ném Poké Ball" dưới khung mục tiêu: chỉ hiện khi Pokémon hoang dã yếu (HP &lt;= ngưỡng), trong màn, không chồng
 *     vùng HUD; hiệu ứng ném (bay / trong bóng / bắt được / thoát) vẽ không lỗi, quái ẩn đúng lúc;</li>
 * <li>Pokédex chip "Đã bắt" + dấu bóng; thẻ chi tiết đồ (ManNhanVat) có dòng bộ / lỗ ngọc từ gói -120; chữ nút "Đập".</li>
 * </ul>
 *   APP_IMAGE=0 sh client_pc/build.sh && javac -cp client_pc/out/build/cls -d $O client_pc/test/ChupTrongGame.java \
 *       client_pc/test/KiemBoNgocBat.java server/src/com/htbb/pokemon/IconPokemon.java server/src/com/htbb/pokemon/IconPokemonBang.java
 *   java -Djava.awt.headless=true -cp client_pc/out/build/cls:client_pc/out/build/res:$O KiemBoNgocBat [thư mục ảnh]
 * Mã thoát 1 = có lỗi.
 */
public class KiemBoNgocBat {
    static int dat, hong;
    static File ra;

    static void kiem(boolean ok, String ten) {
        if (ok) {
            dat++;
        } else {
            hong++;
            System.out.println("HONG: " + ten);
        }
    }

    static boolean coVung(String ten) {
        int n = game.pokemon.VungCham.dem();
        for (int i = 0; i < n; i++) {
            if (ten.equals(game.pokemon.VungCham.ten(i))) {
                return true;
            }
        }
        return false;
    }

    static int[] vung(String ten) {
        int n = game.pokemon.VungCham.dem();
        for (int i = 0; i < n; i++) {
            if (ten.equals(game.pokemon.VungCham.ten(i))) {
                return game.pokemon.VungCham.vungVe(i);
            }
        }
        return null;
    }

    static boolean daVe(String doan) {
        java.util.Set s = htbb.pc.ChuHD.ghi;
        if (s == null) {
            return false;
        }
        synchronized (s) {
            for (java.util.Iterator it = s.iterator(); it.hasNext(); ) {
                if (((String) it.next()).indexOf(doan) >= 0) {
                    return true;
                }
            }
        }
        return false;
    }

    static Object goi(Class c, String ten, Class[] kieu, Object[] gt) {
        try {
            java.lang.reflect.Method m = c.getDeclaredMethod(ten, kieu);
            m.setAccessible(true);
            return m.invoke(null, gt);
        } catch (Throwable e) {
            e.printStackTrace();
            return null;
        }
    }

    // ------------------------------------------------------------------ dữ liệu giả (như gói -120 / -121 máy chủ)

    static Object congThuc(int id, String ten, int bac, boolean phienDa, String thieu) throws Exception {
        Class c = Class.forName("game.pokemon.ManCheTao$CongThuc");
        java.lang.reflect.Constructor k = c.getDeclaredConstructor();
        k.setAccessible(true);
        Object o = k.newInstance();
        dat(o, "idTemplate", Integer.valueOf(id));
        dat(o, "idIcon", Integer.valueOf(14000 + 40 + bac));
        dat(o, "ten", ten);
        dat(o, "bac", Integer.valueOf(bac));
        dat(o, "cap", Integer.valueOf(bac == 0 ? 1 : bac * 10));
        dat(o, "da", Integer.valueOf(2 + bac));
        dat(o, "nlPhu", bac <= 2 ? "Bụi sao" : "Mảnh sao");
        dat(o, "soNl", Integer.valueOf(1 + bac / 3));
        dat(o, "nlCo", Integer.valueOf(1));
        dat(o, "xu", Integer.valueOf(2692 * (bac + 1)));
        dat(o, "loi", Integer.valueOf(0));
        dat(o, "phienDa", Boolean.valueOf(phienDa));
        dat(o, "thieu", thieu);
        return o;
    }

    static void datCheTao() throws Exception {
        Vector v = new Vector();
        v.addElement(congThuc(126, "Vòng cổ sơ cấp", 0, false, ""));
        v.addElement(congThuc(163, "Phiến đá hệ sơ cấp", 0, true, ""));
        v.addElement(congThuc(132, "Vòng cổ vàng", 3, false, "Cần cấp 30."));
        v.addElement(congThuc(168, "Phiến đá hệ vàng", 3, true, "Cần 5 Đá nâng cấp (có 3)."));
        goi(Class.forName("game.pokemon.ManCheTao"), "datDuLieu", new Class[]{String[].class, int[].class, int[].class, int.class, int.class,
            int.class, Vector.class}, new Object[]{new String[]{"Hiếm", "Quý", "Sử thi", "Huyền thoại"}, new int[]{3, 2, 1, 1},
            new int[]{600, 300, 90, 10}, Integer.valueOf(7), Integer.valueOf(20), Integer.valueOf(3), v});
    }

    static void datKham() throws Exception {
        Class cm = Class.forName("game.pokemon.ManKhamNgoc$Mon");
        java.lang.reflect.Constructor k = cm.getDeclaredConstructor();
        k.setAccessible(true);
        Vector v = new Vector();
        String[] ten = {"Vòng cổ bạc", "Găng đấm vàng", "Khăn quàng đồng"};
        int[][] he = {{1, -1}, {14, 2}, {-1}};
        int[][] cap = {{3, 0}, {10, 6}, {0}};
        for (int i = 0; i < ten.length; i++) {
            Object m = k.newInstance();
            dat(m, "id", Integer.valueOf(700 + i));
            dat(m, "idTemplate", Integer.valueOf(130 + i));
            dat(m, "idIcon", Integer.valueOf(14000 + 44 + i));
            dat(m, "ten", ten[i]);
            dat(m, "mau", Integer.valueOf(i == 0 ? 2 : i == 1 ? 1 : 0));
            dat(m, "plus", Integer.valueOf(i * 4));
            dat(m, "dangMac", Boolean.valueOf(i == 1));
            dat(m, "he", he[i]);
            dat(m, "cap", cap[i]);
            dat(m, "mai", new boolean[he[i].length]);
            v.addElement(m);
        }
        Vector t = new Vector();
        t.addElement(new int[]{4, 2, 0, 1});
        t.addElement(new int[]{17, 5, 1, 2});
        int[] g = new int[11];
        for (int c = 1; c <= 10; c++) {
            g[c] = (int) Math.round(52734 * Math.pow(1.45, c - 1));
        }
        goi(Class.forName("game.pokemon.ManKhamNgoc"), "datDuLieu", new Class[]{Vector.class, Vector.class, int[].class, int.class, int.class,
            int[].class}, new Object[]{v, t, g, Integer.valueOf(26367), Integer.valueOf(15820),
            new int[]{6, 2, 3, 8, 1, 1, 10, 8, 2, 3, 15, 1, 2, 15, 5, 6, 1, 16}});
    }

    static void dat(Object o, String ten, Object gt) {
        try {
            java.lang.reflect.Field f = o.getClass().getDeclaredField(ten);
            f.setAccessible(true);
            f.set(o, gt);
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    static game.model.Monster quai;

    /** Một Pokémon hoang dã (Rattata) cạnh nhân vật, làm mục tiêu; hp % theo tham số. */
    static void datQuai(int phanTramHp) {
        game.render.screen.GameScr gs = game.render.Canvas.gameScr;
        game.model.MainChar mc = gs.mainChar;
        if (quai == null) {
            quai = new game.model.Monster();
            quai.ID = (short) 3100;
            quai.catagory = 1;
            quai.setType((short) 0);
            quai.level = (byte) 20;
            quai.maxhp = 400;
        }
        quai.hp = Math.max(1, 400 * phanTramHp / 100);
        quai.x = (short) (mc.x + 40);
        quai.y = (short) (mc.y + 4);
        quai.xFirst = quai.x;
        quai.yFirst = quai.y;
        if (!gs.actors.contains(quai)) {
            gs.actors.addElement(quai);
        }
        if (!gs.actors.contains(mc)) {
            gs.actors.addElement(mc);
        }
        gs.focusedActor = quai;
        game.pokemon.BatPokemon.coTrangThai = true;
        game.pokemon.BatPokemon.nguongHp = 25;
        game.pokemon.BatPokemon.bongThuTu = new int[]{1, 2, 3};
        game.pokemon.BatPokemon.bongTen = new String[]{"Poké Ball", "Great Ball", "Ultra Ball"};
        game.pokemon.BatPokemon.bongSo = new int[]{12, 3, 0};
        game.pokemon.BatPokemon.bongChon = 0;
    }

    static void mo(final String ten) throws Exception {
        ChupTrongGame.chay(new Runnable() {
            public void run() {
                try {
                    game.render.Canvas.endDlg();
                    game.pokemon.DungDo.xoaHet();
                    htbb.pc.ChuHD.ghi.clear();
                    if (ten.startsWith("che_tao")) {
                        datCheTao();
                        game.pokemon.ManCheTao m = game.pokemon.ManCheTao.gI();
                        ChupTrongGame.dat(m, "chon", Integer.valueOf(ten.equals("che_tao_phien_da") ? 1 : -1));
                        moBang(m);
                    } else if (ten.startsWith("kham")) {
                        datKham();
                        game.pokemon.ManKhamNgoc m = game.pokemon.ManKhamNgoc.gI();
                        ChupTrongGame.dat(m, "chon", Integer.valueOf(ten.equals("kham_chi_tiet") ? 1 : ten.equals("kham_lo_trong") ? 0 : -1));
                        moBang(m);
                        datTab(m, ten.equals("kham_tui") ? 1 : 0);
                    } else if (ten.startsWith("nem")) {
                        game.render.Canvas.gameScr.switchToMe();
                        datQuai(ten.equals("nem_khoe") ? 100 : 20);
                    } else if (ten.equals("pokedex_da_bat")) {
                        game.pokemon.BatPokemon.DEX.clear();
                        game.pokemon.BatPokemon.DEX.put(new Integer(19), new Integer(3));
                        game.pokemon.BatPokemon.DEX.put(new Integer(43), new Integer(7));
                        game.pokemon.BatPokemon.soDaBat = 2;
                        game.pokemon.BatPokemon.coDex = true;
                        game.pokemon.ManPokedex.mo();
                        ChupTrongGame.dat(game.pokemon.ManPokedex.gI(), "loc", Integer.valueOf(6));
                    } else if (ten.equals("chi_tiet_do")) {
                        game.pokemon.ManNhanVat.mo(0);
                        game.pokemon.ManNhanVat nv = game.pokemon.ManNhanVat.gI();
                        ChupTrongGame.dat(nv, "o", Integer.valueOf(1));
                        Object mon = null;
                        for (int i = 0; i < game.model.Char.inventory.size(); i++) {
                            game.model.ItemInInventory it = (game.model.ItemInInventory) game.model.Char.inventory.elementAt(i);
                            if (it.idTemplate == 10) {
                                mon = it;
                            }
                        }
                        game.pokemon.TrangBiThem.datMoTa(((game.model.ItemInInventory) mon).itemID, new int[]{4, 1, 0, 0, 2, 2, 5},
                                new String[]{"Bộ Pallet (Kanto): đang mặc 4/11 món cùng bậc", "4 món: +5% HP (đang kích)", "· 7 món: +5% tấn công",
                                    "· 11 món: +3% chí mạng", "Lỗ ngọc 1/1: Ngọc Lửa cấp 3", "Ngọc: +3% chỉ số món · +4,5‰ sát thương hệ Lửa",
                                    "Đổi lại: −3‰ kháng hệ Nước"});
                        ChupTrongGame.dat(nv, "xem", mon);
                    }
                } catch (Throwable e) {
                    e.printStackTrace();
                    kiem(false, ten + " mo man: " + e);
                }
            }
        });
    }

    static void moBang(game.pokemon.ManBang m) throws Exception {
        java.lang.reflect.Method mb = game.pokemon.ManBang.class.getDeclaredMethod("moBang");
        mb.setAccessible(true);
        mb.invoke(m);
    }

    static void datTab(game.pokemon.ManBang m, int t) throws Exception {
        java.lang.reflect.Field f = game.pokemon.ManBang.class.getDeclaredField("tab");
        f.setAccessible(true);
        f.setInt(m, t);
    }

    public static void main(String[] a) throws Exception {
        System.setProperty("java.awt.headless", "true");
        ra = new File(a.length > 0 ? a[0] : "anh_bo_ngoc_bat");
        ra.mkdirs();
        System.setProperty("htbb.duLieu", new File(ra, "du_lieu").getAbsolutePath());
        System.setProperty("microedition.platform", "HTBB-PC/kiem");
        htbb.pc.CaiDat.thuMucDuLieu();
        htbb.pc.CaiDat.nap();
        htbb.pc.ChuHD.bat = true;
        htbb.pc.GiaoDienHD.bat = true;
        htbb.pc.CaiDat.tatTieng = true;
        htbb.pc.LopPhu.nap();
        MIDlet.GHI_DE.put("HTBB-May-Chu", "127.0.0.1:1");
        NenPc.tinhKichThuoc(1280, 720);
        ChupTrongGame.chay(new Runnable() {
            public void run() {
                try {
                    game.render.screen.TField.isQwerty = true;
                    MIDlet m = (MIDlet) Class.forName("game.GameMidlet").newInstance();
                    MIDlet.batDau(m);
                } catch (Throwable e) {
                    e.printStackTrace();
                }
            }
        });
        for (int i = 0; i < 100 && (game.render.Canvas.instance == null || game.render.Canvas.currentScreen == null); i++) {
            Thread.sleep(100);
        }
        Thread.sleep(2500);
        ChupTrongGame.chay(new Runnable() {
            public void run() {
                ChupTrongGame.giaLap();
            }
        });
        Thread.sleep(800);
        htbb.pc.ChuHD.ghi = java.util.Collections.synchronizedSet(new java.util.HashSet<String>());

        // ---- đơn vị: chữ nút "Đập" + dòng phụ (ScreenDapDo) ----
        game.pokemon.TrangBiThem.ncItem = 512;
        game.pokemon.TrangBiThem.ncK = 8;
        game.pokemon.TrangBiThem.ncTiLe = 74;
        game.pokemon.TrangBiThem.ncPity = 19;
        game.pokemon.TrangBiThem.ncMoc = 7;
        game.pokemon.TrangBiThem.ncXu = 12773;
        game.pokemon.TrangBiThem.ncDa = 2;
        kiem("Đập 74%".equals(game.pokemon.TrangBiThem.chuNut(512)), "nut Dap = ti le that: " + game.pokemon.TrangBiThem.chuNut(512));
        kiem(game.pokemon.TrangBiThem.chuNut(513) == null, "mon khac: giu chu cu");
        String phu = game.pokemon.TrangBiThem.chuPhu(512);
        kiem(phu != null && phu.indexOf("+19% pity") >= 0 && phu.indexOf("giữ mốc +7") >= 0 && phu.indexOf("12.773") >= 0,
                "dong phu: pity + moc neo + xu: " + phu);
        game.pokemon.TrangBiThem.datMoTa(9999, new int[]{4, 1}, new String[]{"Bộ Pallet (Kanto): đang mặc 4/11 món", "4 món: +5% HP (đang kích)"});
        String mt = game.pokemon.TrangBiThem.moTa(9999);
        kiem(mt.indexOf("\n4Bộ Pallet") >= 0 && mt.indexOf("\n14 món") >= 0, "mo ta the chi tiet dinh dang setTemp: " + mt.replace('\n', '|'));

        int[][] co = {{2400, 1080, 420}, {1280, 720, 0}, {1920, 1080, 0}, {1024, 768, 0}};
        String[] tenCo = {"dt20x9", "pc720", "pc1080", "pc4x3"};
        String[] man = {"che_tao", "che_tao_phien_da", "kham", "kham_chi_tiet", "kham_lo_trong", "kham_tui", "nem_yeu", "nem_khoe", "pokedex_da_bat", "chi_tiet_do"};
        for (int c = 0; c < co.length; c++) {
            final int W = co[c][0], H = co[c][1], dpi = co[c][2];
            NenPc.tinhKichThuoc(W, H);
            ChupTrongGame.chay(new Runnable() {
                public void run() {
                    game.render.Canvas.instance.sizeChanged(NenPc.rong, NenPc.cao);
                }
            });
            float s = NenPc.hienW / (float) NenPc.rong;
            int min, cach;
            if (dpi > 0) {
                float dpL = (dpi / 160f) / s;
                min = (int) Math.ceil(44 * dpL);
                cach = (int) Math.floor(8 * dpL);
                game.pokemon.Bang.anTrai = (int) Math.ceil(100 / s);
            } else {
                min = (int) Math.ceil(32 / s);
                cach = (int) Math.ceil(8 / s);
                game.pokemon.Bang.anTrai = 0;
            }
            game.pokemon.VungCham.MIN = min;
            game.pokemon.VungCham.CACH = cach;
            for (int m = 0; m < man.length; m++) {
                String t = man[m] + " " + tenCo[c];
                mo(man[m]);
                Thread.sleep(150);
                ChupTrongGame.ve(W, H);
                int dem0 = game.pokemon.Bang.demTam;
                int nut0 = game.pokemon.BatPokemon.demVeNut;
                BufferedImage anh = ChupTrongGame.ve(W, H);
                ImageIO.write(anh, "png", new File(ra, man[m] + "_" + tenCo[c] + ".png"));
                if (man[m].startsWith("nem")) {
                    kiemNem(man[m], t, nut0, min, cach, W, H);
                    continue;
                }
                ChupTrongGame.kiemTam(t, dem0 - 1);
                ChupTrongGame.kiemCham(t, min, cach);
                if (man[m].equals("che_tao")) {
                    kiem(coVung("che-0") && coVung("ct-0"), t + " nut Che tao + hang");
                    kiem(daVe("Sử thi 9%") && daVe("Huyền thoại 1%"), t + " hien ti le bac (60/30/9/1)");
                    kiem(daVe("Chưa ra Sử thi: 7/20"), t + " hien pity 7/20");
                    kiem(daVe("Cần cấp 30."), t + " hien ly do thieu");
                } else if (man[m].equals("che_tao_phien_da")) {
                    kiem(coVung("che-phien-da") && coVung("he-0") && coVung("he-17"), t + " tam chon 18 he + nut che");
                } else if (man[m].equals("kham")) {
                    kiem(coVung("mon-0") && coVung("mon-2"), t + " danh sach mon co lo");
                } else if (man[m].equals("kham_chi_tiet")) {
                    kiem(coVung("nang-1") && coVung("thao-0") && coVung("mai-0") && coVung("thao-1"), t + " lo co ngoc: Nang / Thao / Mai");
                    kiem(daVe("Cấp tối đa"), t + " ngoc cap 10: nut Nang tat");
                    kiem(daVe("Mọi lỗ đã có ngọc"), t + " moi lo day: goi y thao de thay he");
                } else if (man[m].equals("kham_lo_trong")) {
                    kiem(coVung("kham-1") && coVung("nang-0") && (coVung("he-0") && coVung("he-17") || coVung("he-truoc") && coVung("he-sau")), t + " lo trong: nut Kham + chon he");
                } else if (man[m].equals("kham_tui")) {
                    kiem(coVung("phan-ra-0") && coVung("phan-ra-1"), t + " tui ngoc: Phan ra");
                } else if (man[m].equals("pokedex_da_bat")) {
                    kiem(daVe("Đã bắt: 2"), t + " so da bat");
                } else if (man[m].equals("chi_tiet_do")) {
                    kiem(daVe("Bộ Pallet") && (dpi > 0 || daVe("+5% HP") && daVe("Ngọc Lửa cấp 3")), t + " the chi tiet co dong bo + lo ngoc");
                }
            }
        }
        // ---- hiệu ứng ném: bay -> trong bóng (quái ẩn) -> bắt được ----
        NenPc.tinhKichThuoc(1280, 720);
        ChupTrongGame.chay(new Runnable() {
            public void run() {
                game.render.Canvas.instance.sizeChanged(NenPc.rong, NenPc.cao);
                game.render.Canvas.gameScr.switchToMe();
                datQuai(10);
                game.pokemon.BatPokemon.nem(quai);
            }
        });
        Thread.sleep(150);
        BufferedImage bay = ChupTrongGame.ve(1280, 720);
        ImageIO.write(bay, "png", new File(ra, "nem_bay.png"));
        kiem(!game.pokemon.BatPokemon.anQuai(quai.ID), "dang bay: quai chua an");
        Thread.sleep(500);
        ChupTrongGame.ve(1280, 720);
        kiem(game.pokemon.BatPokemon.anQuai(quai.ID), "cho may chu: quai o trong bong (an)");
        goi(game.pokemon.BatPokemon.class, "ketQua", new Class[]{short.class, short.class, int.class, int.class, int.class, int.class, boolean.class},
                new Object[]{Short.valueOf(game.render.Canvas.gameScr.mainChar.ID), Short.valueOf(quai.ID), Integer.valueOf(1), Integer.valueOf(0),
                    Integer.valueOf(2), Integer.valueOf(19), Boolean.FALSE});
        Thread.sleep(400);
        ImageIO.write(ChupTrongGame.ve(1280, 720), "png", new File(ra, "nem_lac.png"));
        kiem(game.pokemon.BatPokemon.anQuai(quai.ID), "truot: dang lac van an quai");
        Thread.sleep(700);
        ImageIO.write(ChupTrongGame.ve(1280, 720), "png", new File(ra, "nem_thoat.png"));
        kiem(!game.pokemon.BatPokemon.anQuai(quai.ID), "truot: het lac -> quai hien lai");
        ChupTrongGame.chay(new Runnable() {
            public void run() {
                game.pokemon.BatPokemon.nem(quai);
            }
        });
        Thread.sleep(600);
        goi(game.pokemon.BatPokemon.class, "ketQua", new Class[]{short.class, short.class, int.class, int.class, int.class, int.class, boolean.class},
                new Object[]{Short.valueOf(game.render.Canvas.gameScr.mainChar.ID), Short.valueOf(quai.ID), Integer.valueOf(1), Integer.valueOf(1),
                    Integer.valueOf(3), Integer.valueOf(19), Boolean.FALSE});
        Thread.sleep(1300);
        ImageIO.write(ChupTrongGame.ve(1280, 720), "png", new File(ra, "nem_bat_duoc.png"));
        kiem(game.pokemon.BatPokemon.daBat(19), "bat duoc: Pokedex danh dau Da bat");
        kiem(game.pokemon.BatPokemon.anQuai(quai.ID), "bat duoc: quai van trong bong (may chu xoa bang goi 90)");
        System.out.println("KiemBoNgocBat: " + dat + " dat, " + hong + " hong -> " + ra.getAbsolutePath());
        System.exit(hong == 0 ? 0 : 1);
    }

    static void kiemNem(String man, String t, int nut0, int min, int cach, int W, int H) {
        boolean ve = game.pokemon.BatPokemon.demVeNut > nut0;
        if (man.equals("nem_khoe")) {
            kiem(!ve && !coVung("nem-bong"), t + " Pokemon con khoe: KHONG co nut nem");
            return;
        }
        kiem(ve && coVung("nem-bong"), t + " Pokemon yeu: co nut Nem bong");
        kiem(coVung("doi-bong"), t + " co nut doi loai bong");
        int[] v = vung("nem-bong");
        if (v == null) {
            return;
        }
        kiem(v[0] >= 0 && v[1] >= 0 && v[0] + v[2] <= NenPc.rong + 1 && v[1] + v[3] <= NenPc.cao + 1, t + " nut trong man");
        kiem(v[3] >= Math.min(min, 40) - 1, t + " nut cao " + v[3] + " >= " + Math.min(min, 40));
        kiem(daVe("Ném Poké Ball ×12"), t + " chu nut co ten bong + so con");
        if (game.render.screen.HudRong.bat()) {
            Object[][] hud = game.render.screen.HudRong.cacVung(game.render.Canvas.gameScr);
            for (int i = 0; i < hud.length; i++) {
                String ten = (String) hud[i][0];
                if ("muc_tieu".equals(ten)) {
                    continue;
                }
                int[] r = (int[]) hud[i][1];
                boolean giao = v[0] < r[0] + r[2] && r[0] < v[0] + v[2] && v[1] < r[1] + r[3] && r[1] < v[1] + v[3];
                kiem(!giao, t + " nut nem khong chong HUD " + ten);
            }
        }
    }
}
