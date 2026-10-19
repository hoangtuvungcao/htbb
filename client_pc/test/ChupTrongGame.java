import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.NenPc;
import javax.microedition.midlet.MIDlet;

/**
 * Chụp màn TRONG GAME không cần cửa sổ / máy chủ (headless, đường vẽ bản PC) + KIỂM CỠ CHẠM giao diện mới:
 * nhân vật giả trên bản đồ offline, mở lần lượt màn chơi (HUD), Menu lưới (ManMenu), màn Nhân vật (5 tab), ô trang
 * bị -> hành trang lọc (có đồ / trống), thẻ chi tiết so sánh. Mỗi màn ở cỡ điện thoại 16:9, 19.5:9, 20:9, 21:9, 16:10, 4:3
 * (giả lập cỡ chạm Android 44 dp) và cỡ PC (16:9, 21:9, 16:10, 4:3; cỡ chạm 32 px). Với MỌI vùng chạm đăng ký (VungCham) ở lớp
 * trên cùng: cỡ >= tối thiểu, hai vùng không chồng + cách nhau >= 8 dp (PC 8 px), nằm trong màn, bấm giả tâm trúng đúng.
 *
 *   APP_IMAGE=0 sh client_pc/build.sh && javac -cp client_pc/out/build/cls -d $O client_pc/test/ChupTrongGame.java \
 *     && java -Djava.awt.headless=true -cp client_pc/out/build/cls:client_pc/out/build/res:$O ChupTrongGame <thư mục ảnh>
 * Mã thoát 1 = có lỗi.
 */
public class ChupTrongGame {
    static int dat, hong;

    static void kiem(boolean ok, String ten) {
        if (ok) dat++;
        else {
            hong++;
            System.out.println("HONG: " + ten);
        }
    }

    /** {rộng, cao, dpi giả lập (0 = PC)} */
    static final int[][] CO = {{1920, 1080, 420}, {2340, 1080, 420}, {2400, 1080, 420}, {2520, 1080, 420},
        {2560, 1600, 320}, {2048, 1536, 320}, {1280, 720, 0}, {1920, 1080, 0}, {2560, 1080, 0}, {1920, 1200, 0}, {1024, 768, 0}};
    static final String[] TEN_CO = {"dt16x9", "dt19.5x9", "dt20x9", "dt21x9", "tablet16x10", "tablet4x3", "pc720", "pc1080", "pc21x9", "pc16x10", "pc4x3"};

    static void chay(final Runnable r) throws Exception {
        final Object k = new Object();
        final boolean[] xong = {false};
        NenPc.luong().them(new Runnable() {
            public void run() {
                try {
                    r.run();
                } catch (Throwable e) {
                    e.printStackTrace();
                } finally {
                    synchronized (k) {
                        xong[0] = true;
                        k.notifyAll();
                    }
                }
            }
        });
        synchronized (k) {
            long het = System.currentTimeMillis() + 5000;
            while (!xong[0] && System.currentTimeMillis() < het) k.wait(100);
        }
    }

    static BufferedImage ve(int W, int H) throws Exception {
        final BufferedImage dem = new BufferedImage(NenPc.thatW, NenPc.thatH, BufferedImage.TYPE_INT_RGB);
        chay(new Runnable() {
            public void run() {
                Graphics2D g2 = dem.createGraphics();
                g2.setColor(java.awt.Color.MAGENTA);
                g2.fillRect(0, 0, dem.getWidth(), dem.getHeight());
                Graphics2D gg = (Graphics2D) g2.create();
                int k = NenPc.heSo;
                gg.scale(k, k);
                gg.clipRect(0, 0, NenPc.rong, NenPc.cao);
                Graphics g = new Graphics(gg, NenPc.rong, NenPc.cao, k);
                try {
                    java.lang.reflect.Method m = javax.microedition.lcdui.Canvas.class.getDeclaredMethod("paint", Graphics.class);
                    m.setAccessible(true);
                    m.invoke(game.render.Canvas.instance, g);
                } catch (Throwable e) {
                    e.printStackTrace();
                }
                gg.dispose();
                try {
                    htbb.pc.LopPhu.ve(g2, dem.getWidth(), dem.getHeight());
                } catch (Throwable e) {
                    // bỏ
                }
                g2.dispose();
            }
        });
        BufferedImage ra = new BufferedImage(W, H, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = ra.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.drawImage(dem, NenPc.hienX, NenPc.hienY, NenPc.hienW, NenPc.hienH, null);
        g.dispose();
        return ra;
    }

    /** Kiểm mọi vùng chạm lớp trên cùng: cỡ, khoảng cách, trong màn, bấm tâm trúng. */
    static void kiemCham(String nhan, int min, int cach) {
        int n = game.pokemon.VungCham.dem();
        int top = game.pokemon.VungCham.lopCaoNhat();
        int so = 0;
        for (int i = 0; i < n; i++) {
            if (game.pokemon.VungCham.lopVung(i) != top) continue;
            String ten = game.pokemon.VungCham.ten(i);
            if (ten != null && ten.startsWith("nen")) continue;   // vùng nền (chạm ra ngoài = đóng)
            so++;
            int[] v = game.pokemon.VungCham.vungVe(i);
            String t = nhan + " [" + ten + "]";
            kiem(v[2] >= min && v[3] >= min, t + " co " + v[2] + "x" + v[3] + " >= " + min + " @" + v[0] + "," + v[1] + " (MIN luc ve " + game.pokemon.VungCham.MIN + ", logic " + NenPc.rong + "x" + NenPc.cao + ")");
            kiem(v[0] >= 0 && v[1] >= 0 && v[0] + v[2] <= NenPc.rong + 1 && v[1] + v[3] <= NenPc.cao + 1, t + " trong man");
            int cx = v[0] + v[2] / 2, cy = v[1] + v[3] / 2;
            int j = game.pokemon.VungCham.tim(cx, cy);
            kiem(j == i, t + " bam tam trung (" + (j < 0 ? "-" : game.pokemon.VungCham.ten(j)) + ")");
            for (int q = i + 1; q < n; q++) {
                if (game.pokemon.VungCham.lopVung(q) != top) continue;
                String tq = game.pokemon.VungCham.ten(q);
                if (tq != null && tq.startsWith("nen")) continue;
                int[] w = game.pokemon.VungCham.vungVe(q);
                // vùng lồng nhau (nút trong hàng) bỏ qua; còn lại: khoảng hở >= cach
                boolean trong = w[0] >= v[0] && w[1] >= v[1] && w[0] + w[2] <= v[0] + v[2] && w[1] + w[3] <= v[1] + v[3]
                        || v[0] >= w[0] && v[1] >= w[1] && v[0] + v[2] <= w[0] + w[2] && v[1] + v[3] <= w[1] + w[3];
                if (trong) continue;
                int hx = Math.max(w[0] - (v[0] + v[2]), v[0] - (w[0] + w[2]));
                int hy = Math.max(w[1] - (v[1] + v[3]), v[1] - (w[1] + w[3]));
                int ho = Math.max(hx, hy);
                if (tron(ten) && tron(tq)) {
                    // hai nút tròn (HUD): khoảng hở thật giữa hai đường tròn nội tiếp
                    double dx = (v[0] + v[2] / 2.0) - (w[0] + w[2] / 2.0), dy = (v[1] + v[3] / 2.0) - (w[1] + w[3] / 2.0);
                    ho = (int) Math.floor(Math.sqrt(dx * dx + dy * dy) - v[2] / 2.0 - w[2] / 2.0);
                }
                kiem(ho >= cach, t + " x [" + tq + "] cach " + ho + " >= " + cach);
            }
        }
        kiem(so > 0, nhan + " co vung cham");
    }

    static boolean tron(String t) {
        return t != null && t.startsWith("hud-") && !t.equals("hud-the") && !t.equals("hud-tu-dong");
    }

    public static void main(String[] a) throws Exception {
        System.setProperty("java.awt.headless", "true");
        File ra = new File(a.length > 0 ? a[0] : "anh_trong_game");
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
        chay(new Runnable() {
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
        for (int i = 0; i < 100 && (game.render.Canvas.instance == null || game.render.Canvas.currentScreen == null); i++) Thread.sleep(100);
        Thread.sleep(2500);
        chay(new Runnable() {
            public void run() {
                giaLap();
            }
        });
        Thread.sleep(800);
        String[] man = {"the_gioi", "menu", "nv_trang_bi", "nv_o_co_do", "nv_o_trong", "nv_chi_tiet", "nv_hanh_trang", "nv_chi_so",
            "nv_ky_nang", "nv_thu", "nv_ngoai_hinh", "so_luong",
            "menu_ngu_canh", "tui_loc_thuoc", "tui_loc_nguyen_lieu", "xac_nhan", "cua_hang_thuoc", "cua_hang_thuoc_chi_tiet",
            "cua_hang_do", "cua_hang_do_chi_tiet", "cua_hang_ban", "nhiem_vu", "nhiem_vu_chi_tiet", "giao_dich", "pokedex",
            "pokedex_chi_tiet", "xep_hang", "doi", "ky_nang_hoc", "ky_nang_chi_tiet"};
        for (int c = 0; c < CO.length; c++) {
            final int W = CO[c][0], H = CO[c][1], dpi = CO[c][2];
            NenPc.tinhKichThuoc(W, H);
            chay(new Runnable() {
                public void run() {
                    game.render.Canvas.instance.sizeChanged(NenPc.rong, NenPc.cao);
                }
            });
            float s = NenPc.hienW / (float) NenPc.rong;
            int min, cach;
            if (dpi > 0) {
                // giả lập Android: 44 dp / 8 dp theo px logic của khung phủ kín
                float dpL = (dpi / 160f) / s;
                min = (int) Math.ceil(44 * dpL);
                cach = (int) Math.floor(8 * dpL);
                game.pokemon.Bang.anTrai = (int) Math.ceil(100 / s);   // tai thỏ 100 px
            } else {
                min = (int) Math.ceil(32 / s);
                cach = (int) Math.ceil(8 / s);
                game.pokemon.Bang.anTrai = 0;
            }
            game.pokemon.VungCham.MIN = min;
            game.pokemon.VungCham.CACH = cach;
            for (int m = 0; m < man.length; m++) {
                final String ten = man[m];
                chay(new Runnable() {
                    public void run() {
                        moMan(ten);
                    }
                });
                Thread.sleep(250);
                chay(new Runnable() {
                    public void run() {
                        // hộp thoại mạng (không có máy chủ khi kiểm) đè lên màn đang kiểm: bỏ
                        Object d = game.render.Canvas.currentDialog;
                        if (d instanceof game.render.screen.MsgDlg) {
                            game.render.Canvas.endDlg();
                        }
                    }
                });
                ve(W, H);
                Thread.sleep(150);
                BufferedImage anh = ve(W, H);
                ImageIO.write(anh, "png", new File(ra, ten + "_" + TEN_CO[c] + ".png"));
                // màn chơi: HUD PC (HudRong) chỉ ở cỡ PC; HUD APK (Hud.java) kiểm riêng bằng client_jar/tools/KiemCoCham
                if (!"the_gioi".equals(ten) || dpi == 0) {
                    kiemCham(ten + " " + TEN_CO[c], min, cach);
                }
            }
        }
        System.out.println("ChupTrongGame: " + dat + " dat, " + hong + " hong -> " + ra.getAbsolutePath());
        System.exit(hong == 0 ? 0 : 1);
    }

    /** Nhân vật + hành trang + đồ đang mặc giả (không máy chủ). */
    static void giaLap() {
        game.render.Canvas.isLoad = false;
        game.render.Canvas.endDlg();
        try {
            game.model.Tilemap.loadMap(0, null);
        } catch (Throwable e) {
            e.printStackTrace();
        }
        game.render.screen.GameScr gs = game.render.Canvas.gameScr;
        game.model.MainChar mc = gs.mainChar;
        mc.name = "Charmander99";
        mc.level = 24;
        mc.clazz = 0;
        mc.pokeDex = 5;
        mc.hp = 820;
        mc.maxhp = 1200;
        mc.mp = 240;
        mc.maxmp = 400;
        mc.lvpercent = 455;
        mc.defend = 310;
        mc.defend_magic = 180;
        mc.accurate = 92;
        mc.dodge = 41;
        mc.critical = 15;
        mc.baokich = 75;
        mc.potions[1] = 12;
        mc.potions[4] = 5;
        mc.x = 200;
        mc.y = 200;
        game.render.screen.WindowInfoScr.infoMainCharServer = new String[]{"Tốc độ: 118", "Kháng Lửa: 12%", "Hút máu: 3%"};
        // mẫu đồ
        String[] tenDo = {"Than lửa hồng", "Khăn quàng đỏ", "Băng đô gió", "Đai sức mạnh", "Vòng cổ ngọc", "Huy hiệu Đá",
            "Găng đấm thép", "Lông vũ tốc độ", "Phiến đá Lửa", "Ruy băng vàng", "Khăn quàng xanh", "Đai bạc", "Hạt giống thần"};
        int[] loai = {3, 0, 2, 1, 9, 8, 11, 10, 12, 19, 0, 1, 4};
        game.model.ItemTemplate[] mau = new game.model.ItemTemplate[tenDo.length];
        for (int i = 0; i < mau.length; i++) {
            game.model.ItemTemplate t = new game.model.ItemTemplate();
            t.id = (short) i;
            t.name = tenDo[i];
            t.type = (short) loai[i];
            t.level = (short) (i == 11 ? 40 : 5 + i);
            t.idIcon = (short) (100 + i);
            mau[i] = t;
        }
        game.render.Res.itemTemplates.removeAllElements();
        game.render.Res.itemTemplates.addElement(mau);
        String[] tenCs = {"Tấn công", "Phòng thủ", "Né tránh", "Chính xác", "Chí mạng", "HP"};
        for (int i = 0; i < tenCs.length; i++) {
            game.model.ItemTemplate.ALL_NAME_ATTRIBUTE_ITEM.put(String.valueOf(i), new game.model.NameAttributeItem((short) i, tenCs[i], (byte) 0, (byte) 0));
        }
        mc.wearingItems = new java.util.Vector();
        game.model.Char.inventory.removeAllElements();
        int[] mac = {0, 1, 4, 5};
        for (int i = 0; i < mau.length; i++) {
            game.model.ItemInInventory it = new game.model.ItemInInventory();
            it.itemID = (short) (500 + i);
            it.idTemplate = (short) i;
            it.charClazz = 0;
            it.level = mau[i].level;
            it.colorName = (byte) (i % 4);
            it.isEnoughData = true;
            it.allAttribute.addElement(new game.model.InfoAttributeItem((short) 0, (short) (20 + i * 3)));
            it.allAttribute.addElement(new game.model.InfoAttributeItem((short) 1, (short) (12 + (i % 5) * 4)));
            if (i % 2 == 0) it.allAttribute.addElement(new game.model.InfoAttributeItem((short) 5, (short) (40 + i)));
            boolean dangMac = false;
            for (int k = 0; k < mac.length; k++) if (mac[k] == i) dangMac = true;
            if (dangMac) {
                it.viTriVe = 1;
                mc.wearingItems.addElement(it);
            } else {
                game.model.Char.inventory.addElement(it);
            }
        }
        // nguyên liệu, vật phẩm nhiệm vụ
        game.model.GemTemp gt = new game.model.GemTemp();
        gt.id = 3;
        gt.name = "Đá tiến hoá Lửa";
        gt.decript = "Nguyên liệu tiến hoá dòng Lửa.";
        gt.type = 1;
        gt.price = 1200;
        gt.isSell = true;
        gt.typeMoney = 0;
        game.render.Res.gemTemplate.removeAllElements();
        game.render.Res.gemTemplate.addElement(gt);
        game.render.Res.shopTemplate.removeAllElements();
        for (int i = 0; i < 6; i++) {
            game.model.GemTemp sp = new game.model.GemTemp();
            sp.id = (short) (10 + i);
            sp.name = "Rương quà " + (i + 1);
            sp.decript = "Mở ra nhận vật phẩm ngẫu nhiên.";
            sp.price = 50 * (i + 1);
            sp.typeMoney = (byte) (i % 2);
            sp.isSell = true;
            game.render.Res.shopTemplate.addElement(sp);
        }
        game.model.GemTemplate gm = new game.model.GemTemplate(3);
        gm.number = 7;
        game.model.MainChar.gemItem.removeAllElements();
        game.model.MainChar.gemItem.addElement(gm);
        game.model.ItemQuest.NAME_ITEM = new String[]{"Lông Pidgey", "Vỏ Caterpie"};
        game.model.ItemQuest.ICON_IMG = new byte[]{1, 2};
        game.model.MainChar.itemQuest = new short[]{4, 9};
        mc.charXu = 1250000;
        mc.luong = 320;
        mc.skillPointLeft = 2;
        // giá thuốc
        try {
            for (int i = 0; i < game.render.Res.potionTemplates.length; i++) {
                if (game.render.Res.potionTemplates[i] == null) {
                    game.render.Res.potionTemplates[i] = new game.model.PotionTemplate((short) (20 + i * 15), (short) 100);
                }
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
        // nhiệm vụ
        game.model.Quest q = new game.model.Quest(1);
        q.name = "Dẹp loạn Rattata";
        q.decript = "Rattata phá vườn của bà Hoa. Hạ 10 con rồi về báo lại.";
        q.infoGift = "500 xu, 120 điểm kinh nghiệm";
        q.stateQuest = 2;
        q.type = 0;
        q.idMonsKill = new short[]{0};
        q.allMonsKilled = new short[]{6};
        q.totalMonskill = new short[]{10};
        game.render.screen.GameScr.mainQuest = q;
        game.model.Quest q2 = new game.model.Quest(2);
        q2.name = "Thư gửi giáo sư";
        q2.decript = "Mang thư tới phòng thí nghiệm.";
        q2.stateQuest = 0;
        game.render.screen.GameScr.allQuestCanReceive.removeAllElements();
        game.render.screen.GameScr.allQuestCanReceive.addElement(q2);
        // mẫu quái (Pokédex)
        String[] tq = {"Rattata", "Pidgey", "Caterpie", "Oddish", "Pikachu", "Geodude", "Zubat", "Magikarp"};
        int[] dq = {19, 16, 10, 43, 25, 74, 41, 129};
        game.render.Res.monsterTemplates = new game.model.MonsterTemplate[tq.length];
        for (int i = 0; i < tq.length; i++) {
            game.model.MonsterTemplate m = new game.model.MonsterTemplate();
            m.name = tq[i];
            m.maxhp = 50 + i * 20;
            game.render.Res.monsterTemplates[i] = m;
            game.model.MonsterTemplate.pokeDex[i] = (short) dq[i];
        }
        // cửa hàng thuốc
        gs.shop.removeAllElements();
        // giao dịch
        game.model.ItemInInventory ra = (game.model.ItemInInventory) game.model.Char.inventory.elementAt(0);
        mc.tItems.removeAllElements();
        mc.tItems.addElement(ra);
        mc.rItems.removeAllElements();
        mc.rItems.addElement(game.model.Char.inventory.elementAt(1));
        mc.nameUserTrade = "Misty";
        // đội / xếp hạng / bạn
        game.model.Char.party.removeAllElements();
        game.model.Char.party.addElement(new game.model.PartyInfo(mc.ID, mc.name, 24, 0));
        game.model.Char.party.addElement(new game.model.PartyInfo(7, "Misty", 22, 3));
        mc.IDMasterParty = mc.ID;
        java.util.Vector top = new java.util.Vector();
        String[] tt = {"Red", "Blue", "Charmander99", "Gold", "Silver", "Crystal", "Brock"};
        for (int i = 0; i < tt.length; i++) {
            game.model.Char c = new game.model.Char();
            c.name = tt[i];
            c.level = (byte) (60 - i * 4);
            c.clazz = (byte) (i % 5);
            c.charXu = 90000000L / (i + 1);
            top.addElement(c);
        }
        game.render.screen.ListScr.gI().freindList = top;
        java.util.Vector hoc = new java.util.Vector();
        for (int i = 0; i < 3; i++) {
            game.model.skill.InfoSkillLearn sk = new game.model.skill.InfoSkillLearn();
            sk.idSkill = (byte) (6 + i);
            sk.name = game.pokemon.PokeChieu.ten(0, 6 + i);
            sk.decript = game.pokemon.PokeChieu.moTa(0, 6 + i);
            sk.price = 15000 * (i + 1);
            hoc.addElement(sk);
        }
        game.render.screen.GameScr.infoNewSkill.removeAllElements();
        game.render.screen.GameScr.infoNewSkill.addElement(hoc);
        TOP = top;
        gs.switchToMe();
    }

    static java.util.Vector TOP;

    static void shopThuoc() {
        game.render.screen.GameScr gs = game.render.Canvas.gameScr;
        gs.shop.removeAllElements();
        byte[] loai = {1, 2, 3, 4, 5, 6};
        for (int i = 0; i < loai.length; i++) {
            game.model.ItemInInventory it = new game.model.ItemInInventory();
            it.catagory = 4;
            it.potionType = loai[i];
            it.isEnoughData = true;
            gs.shop.addElement(it);
        }
    }

    static void shopDo() {
        game.render.screen.GameScr gs = game.render.Canvas.gameScr;
        gs.shop.removeAllElements();
        for (int i = 0; i < 13; i++) {
            game.model.ItemInInventory it = new game.model.ItemInInventory();
            it.idTemplate = (short) i;
            it.charClazz = 0;
            it.level = (short) (5 + i);
            it.isEnoughData = true;
            it.allAttribute.addElement(new game.model.InfoAttributeItem((short) 0, (short) (30 + i * 2)));
            it.allAttribute.addElement(new game.model.InfoAttributeItem((short) 1, (short) (8 + i)));
            gs.shop.addElement(it);
        }
    }

    static void moMan(String ten) {
        game.render.Canvas.endDlg();
        game.render.Canvas.menu.showMenu = false;
        game.pokemon.ManNhanVat nv = game.pokemon.ManNhanVat.gI();
        if ("the_gioi".equals(ten)) {
            game.render.Canvas.gameScr.switchToMe();
        } else if ("menu".equals(ten)) {
            game.pokemon.ManMenu.mo();
        } else if ("nv_trang_bi".equals(ten)) {
            game.pokemon.ManNhanVat.mo(0);
        } else if ("nv_o_co_do".equals(ten)) {
            game.pokemon.ManNhanVat.mo(0);
            dat(nv, "o", 1);   // khăn quàng (đang mặc + 1 trong túi)
        } else if ("nv_o_trong".equals(ten)) {
            game.pokemon.ManNhanVat.mo(0);
            dat(nv, "o", 7);   // găng: không... (có 1) -> dùng Ruy băng? chọn ô Huy hiệu 2 (trống)
            dat(nv, "o", 6);
        } else if ("nv_chi_tiet".equals(ten)) {
            game.pokemon.ManNhanVat.mo(0);
            dat(nv, "o", 1);
            Object mon = game.model.Char.inventory.elementAt(6);   // Khăn quàng xanh (idTemplate 10)
            for (int i = 0; i < game.model.Char.inventory.size(); i++) {
                game.model.ItemInInventory it = (game.model.ItemInInventory) game.model.Char.inventory.elementAt(i);
                if (it.idTemplate == 10) mon = it;
            }
            dat(nv, "xem", mon);
        } else if ("nv_hanh_trang".equals(ten)) {
            game.pokemon.ManNhanVat.moHanhTrang();
        } else if ("nv_chi_so".equals(ten)) {
            game.pokemon.ManNhanVat.mo(1);
        } else if ("nv_ky_nang".equals(ten)) {
            game.pokemon.ManNhanVat.mo(2);
        } else if ("nv_thu".equals(ten)) {
            game.pokemon.ManNhanVat.mo(3);
        } else if ("nv_ngoai_hinh".equals(ten)) {
            game.pokemon.ManNhanVat.mo(4);
        } else if ("menu_ngu_canh".equals(ten)) {
            game.render.Canvas.gameScr.switchToMe();
            java.util.Vector v = new java.util.Vector();
            String[] c = {"Nói chuyện", "Mua bán", "Nhiệm vụ", "Giao dịch", "Mời vào đội", "Kết bạn", "Thông tin"};
            for (int i = 0; i < c.length; i++) {
                v.addElement(new game.model.Command(c[i], new game.model.IAction() {
                    public void perform() {
                    }
                }));
            }
            game.pokemon.MenuNguCanh.mo(v, false);
        } else if ("tui_loc_thuoc".equals(ten)) {
            game.pokemon.ManNhanVat.moHanhTrang();
            dat(nv, "loc", Integer.valueOf(1));
        } else if ("tui_loc_nguyen_lieu".equals(ten)) {
            game.pokemon.ManNhanVat.moHanhTrang();
            dat(nv, "loc", Integer.valueOf(3));
        } else if ("xac_nhan".equals(ten)) {
            game.pokemon.ManNhanVat.moHanhTrang();
            game.pokemon.HopXacNhan.hoiGia("Mua thuốc", "Mua 10 Thuốc hồi HP?", "Mua", game.pokemon.TienTe.XU, 250, null);
        } else if ("cua_hang_thuoc".equals(ten) || "cua_hang_thuoc_chi_tiet".equals(ten)) {
            shopThuoc();
            game.pokemon.ManCuaHang.mo(game.pokemon.ManCuaHang.THUOC, new byte[]{0, 19});
            if (ten.endsWith("chi_tiet")) {
                dat(game.pokemon.ManCuaHang.gI(), "chon", game.render.Canvas.gameScr.shop.elementAt(0));
                dat(game.pokemon.ManCuaHang.gI(), "soLuong", Integer.valueOf(12));
            }
        } else if ("cua_hang_do".equals(ten) || "cua_hang_do_chi_tiet".equals(ten)) {
            shopDo();
            game.pokemon.ManCuaHang.mo(game.pokemon.ManCuaHang.DO, new byte[]{0, 9});
            if (ten.endsWith("chi_tiet")) {
                dat(game.pokemon.ManCuaHang.gI(), "chon", game.render.Canvas.gameScr.shop.elementAt(1));
            }
        } else if ("cua_hang_ban".equals(ten)) {
            shopDo();
            game.pokemon.ManCuaHang.mo(game.pokemon.ManCuaHang.DO, new byte[]{0, 9});
            dat(game.pokemon.ManCuaHang.gI(), "tab", Integer.valueOf(1));
            dat(game.pokemon.ManCuaHang.gI(), "chon", game.model.Char.inventory.elementAt(2));
        } else if ("nhiem_vu".equals(ten)) {
            game.pokemon.ManNhiemVu.mo();
        } else if ("nhiem_vu_chi_tiet".equals(ten)) {
            game.pokemon.ManNhiemVu.mo();
            dat(game.pokemon.ManNhiemVu.gI(), "tab", Integer.valueOf(0));
            dat(game.pokemon.ManNhiemVu.gI(), "chon", game.render.screen.GameScr.mainQuest);
        } else if ("giao_dich".equals(ten)) {
            game.pokemon.ManGiaoDich.mo();
        } else if ("pokedex".equals(ten)) {
            game.pokemon.ManPokedex.mo();
        } else if ("pokedex_chi_tiet".equals(ten)) {
            game.pokemon.ManPokedex.mo();
            dat(game.pokemon.ManPokedex.gI(), "chon", Integer.valueOf(4));
        } else if ("xep_hang".equals(ten)) {
            game.pokemon.ManXepHang.mo();
            game.pokemon.ManXepHang.nhan(4, TOP);
        } else if ("doi".equals(ten)) {
            game.pokemon.ManDoi.mo(0);
        } else if ("ky_nang_hoc".equals(ten)) {
            game.pokemon.ManNhanVat.mo(2);
            dat(nv, "hocMoi", Boolean.TRUE);
        } else if ("ky_nang_chi_tiet".equals(ten)) {
            game.pokemon.ManNhanVat.mo(2);
            dat(nv, "chieu", Integer.valueOf(1));
        } else if ("so_luong".equals(ten)) {
            game.render.Canvas.gameScr.switchToMe();
            game.render.Canvas.inputDlg.setInfo("Số lượng mua: Thuốc hồi HP", new game.model.IAction() {
                public void perform() {
                    game.render.Canvas.endDlg();
                }
            }, game.render.screen.TField.INPUT_TYPE_NUMERIC, 3, true);
            game.render.Canvas.inputDlg.show();
        }
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
}
