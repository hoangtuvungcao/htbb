import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import javax.imageio.ImageIO;
import javax.microedition.lcdui.NenPc;
import javax.microedition.midlet.MIDlet;

/**
 * KIỂM GIAO DIỆN 0.9.8 HỘP PC / TRAO ĐỔI POKÉMON / POKÉDEX LOÀI (headless, không máy chủ, đường vẽ bản PC) ở 11 cỡ màn
 * của ChupTrongGame.CO. Dữ liệu đi qua MessageHandler thật bằng gói -121 giả, ĐÚNG thứ tự máy chủ ghi
 * (real.BatPokemonGame.guiHop / guiLoai, real.TraoDoiPokemonGame.guiPhien / xong):
 * <ul>
 * <li>Hộp PC lưới (thẻ hình PNG, sao, sắc màu, khoá), chi tiết (6 chỉ số cá thể, tính cách, Kẹo, 5 nút), chế độ chọn
 *     cho trao đổi;</li>
 * <li>cửa sổ trao đổi (hai cột, phí, Huỷ / Đổi Pokémon / Trao đổi), màn đã xong, hộp lời mời;</li>
 * <li>thẻ Pokédex có thông tin loài (độ hiếm, tỉ lệ, tiến hoá, nơi sống, chỉ số gốc).</li>
 * </ul>
 * Mỗi màn: tấm canh giữa trong vùng an toàn (ChupTrongGame.kiemTam), vùng chạm đủ cỡ / không chồng (kiemCham), đủ nút,
 * chữ, icon PNG đã vẽ (IconBat.demVe tăng, không vẽ icon bằng lệnh).
 *   APP_IMAGE=0 sh client_pc/build.sh && javac -cp client_pc/out/build/cls -d $O client_pc/test/ChupTrongGame.java \
 *       client_pc/test/KiemTraoDoiPokemon.java server/src/com/htbb/pokemon/IconPokemon.java server/src/com/htbb/pokemon/IconPokemonBang.java
 *   java -Djava.awt.headless=true -cp client_pc/out/build/cls:client_pc/out/build/res:$O KiemTraoDoiPokemon [thư mục ảnh]
 * Mã thoát 1 = có lỗi.
 */
public class KiemTraoDoiPokemon {
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

    // ------------------------------------------------------------------ gói giả (đúng thứ tự máy chủ)

    interface Ghi {
        void ghi(DataOutputStream o) throws Exception;
    }

    static byte[] goi(int kieu, Ghi g) throws Exception {
        ByteArrayOutputStream b = new ByteArrayOutputStream();
        DataOutputStream o = new DataOutputStream(b);
        o.writeByte(kieu);
        g.ghi(o);
        o.flush();
        return b.toByteArray();
    }

    /** {id, dex, cap, shiny, sao, ivPt, co} */
    static final int[][] HOP = {
        {1, 255, 18, 0, 1, 58, 1}, {2, 255, 12, 0, 0, 31, 1}, {3, 256, 36, 1, 3, 90, 16}, {4, 371, 30, 0, 2, 75, 6},
        {5, 93, 30, 0, 1, 55, 8}, {6, 19, 9, 0, 0, 22, 0}, {7, 43, 21, 0, 2, 70, 0},
    };
    static final String[] TEN = {"Torchic", "Torchic", "Combusken", "Bagon", "Haunter", "Rattata", "Oddish"};

    static byte[] goiHop() throws Exception {
        return goi(4, new Ghi() {
            public void ghi(DataOutputStream o) throws Exception {
                o.writeShort(HOP.length);
                o.writeShort(30);
                o.writeInt(3);
                o.writeShort(2);
                o.writeShort(HOP.length);
                for (int i = 0; i < HOP.length; i++) {
                    int[] c = HOP[i];
                    o.writeInt(c[0]);
                    o.writeShort(c[1]);
                    o.writeByte(c[2]);
                    o.writeByte(c[3]);
                    o.writeByte(c[4]);
                    o.writeByte(c[5]);
                    o.writeShort(40 + c[2] * 6);
                    o.writeByte(i * 3 % 25);
                    o.writeByte(c[1] == 371 ? 3 : 0);
                    o.writeShort(12 + i);
                    o.writeShort(1 + c[2] / 10);
                    o.writeShort(c[1] == 255 ? 256 : c[1] == 256 ? 257 : c[1] == 93 ? 94 : 0);
                    o.writeByte(c[1] == 255 ? 16 : c[1] == 256 ? 36 : 38);
                    o.writeShort(c[1] == 255 ? 25 : 50);
                    o.writeByte(c[6]);
                    for (int k = 0; k < 6; k++) {
                        o.writeByte((c[5] * 31 / 100 + k * 3) % 32);
                    }
                    o.writeUTF(TEN[i]);
                    o.writeUTF(i == 2 ? "Kiên quyết (+Tấn công, -Tấn công đặc biệt)" : "Cứng cỏi (trung tính)");
                    o.writeUTF(c[1] == 371 ? "Bagon" : c[1] == 93 ? "Gastly" : c[1] == 256 || c[1] == 255 ? "Torchic" : TEN[i]);
                    o.writeUTF("+2 Kẹo, +36 xu");
                }
            }
        });
    }

    static void ghiDs(DataOutputStream o, int[] chiSo) throws Exception {
        o.writeByte(chiSo.length);
        for (int j = 0; j < chiSo.length; j++) {
            int[] c = HOP[chiSo[j]];
            o.writeInt(c[0]);
            o.writeShort(c[1]);
            o.writeByte(c[2]);
            o.writeByte(c[3]);
            o.writeByte(c[4]);
            o.writeByte(c[5]);
            o.writeShort(40 + c[2] * 6);
            o.writeByte(c[1] == 371 ? 3 : 0);
            o.writeByte(c[1] == 93 ? 1 : 0);
            o.writeUTF(TEN[chiSo[j]]);
        }
    }

    static byte[] goiPhien(final int buocToi, final int buocDoi, final String ghiChu) throws Exception {
        return goi(6, new Ghi() {
            public void ghi(DataOutputStream o) throws Exception {
                o.writeByte(2);
                o.writeUTF("Mây Xanh");
                o.writeByte(buocToi);
                o.writeByte(buocDoi);
                ghiDs(o, new int[]{0, 4});
                ghiDs(o, new int[]{3});
                o.writeInt(7184);
                o.writeUTF(ghiChu);
            }
        });
    }

    static void nap(final byte[] b) throws Exception {
        ChupTrongGame.chay(new Runnable() {
            public void run() {
                ChupTrongGame.goi(-121, b);
            }
        });
    }

    // ------------------------------------------------------------------ mở màn

    static void mo(final String ten) throws Exception {
        ChupTrongGame.chay(new Runnable() {
            public void run() {
                try {
                    game.render.Canvas.endDlg();
                    game.pokemon.DungDo.xoaHet();
                    htbb.pc.ChuHD.ghi.clear();
                    game.render.Canvas.gameScr.switchToMe();
                    game.pokemon.BatPokemon.coTrangThai = true;
                    if (ten.startsWith("hop_pc")) {
                        ChupTrongGame.goi(-121, goiHop());
                        game.pokemon.ManHopPc m = game.pokemon.ManHopPc.gI();
                        if (ten.equals("hop_pc_chon_doi")) {
                            game.pokemon.BatPokemon.tdToi = new java.util.Vector();
                            game.pokemon.ManHopPc.mo(true);
                            ChupTrongGame.dat(m, "daChon", vec(new int[]{1, 5}));
                        } else {
                            ChupTrongGame.dat(m, "chon", Integer.valueOf(ten.equals("hop_pc_chi_tiet") ? 3 : -1));
                        }
                    } else if (ten.equals("trao_doi")) {
                        ChupTrongGame.goi(-121, goiHop());
                        ChupTrongGame.goi(-121, goiPhien(1, 1, ""));
                    } else if (ten.equals("trao_doi_chon")) {
                        ChupTrongGame.goi(-121, goiPhien(0, 1, "Pokémon này đang đi theo bạn: chọn Thôi đi theo trước khi trao đổi."));
                    } else if (ten.equals("trao_doi_xong")) {
                        ChupTrongGame.goi(-121, goi(6, new Ghi() {
                            public void ghi(DataOutputStream o) throws Exception {
                                o.writeByte(3);
                                o.writeUTF("Trao đổi xong! Nhận Bagon. Haunter đã tiến hoá thành Gengar!");
                                ghiDs(o, new int[]{3});
                            }
                        }));
                    } else if (ten.equals("pokedex_loai")) {
                        game.pokemon.BatPokemon.DEX.put(new Integer(dexDau()), new Integer(7));
                        game.pokemon.BatPokemon.coDex = true;
                        game.pokemon.ManPokedex.mo();
                        game.pokemon.ManPokedex p = game.pokemon.ManPokedex.gI();
                        ChupTrongGame.dat(p, "loc", Integer.valueOf(0));
                        ChupTrongGame.dat(p, "chon", Integer.valueOf(0));
                        ChupTrongGame.dat(p, "daXin", Integer.valueOf(dexDau()));
                        final int dex = dexDau();
                        ChupTrongGame.goi(-121, goi(5, new Ghi() {
                            public void ghi(DataOutputStream o) throws Exception {
                                o.writeShort(dex);
                                o.writeUTF("Thử");
                                o.writeByte(1);
                                o.writeByte(1);
                                o.writeUTF("Ít gặp");
                                o.writeShort(250);
                                o.writeShort(0);
                                o.writeShort(256);
                                o.writeByte(16);
                                o.writeByte(0);
                                o.writeUTF("");
                                o.writeUTF("Combusken");
                                o.writeShort(3);
                                o.writeShort(41);
                                o.writeByte(2);
                                o.writeUTF("Rừng Viridian");
                                o.writeUTF("Đường số 2");
                                o.writeByte(6);
                                int[] g = {45, 60, 40, 70, 50, 45};
                                for (int i = 0; i < 6; i++) {
                                    o.writeByte(g[i]);
                                }
                            }
                        }));
                    }
                } catch (Throwable e) {
                    e.printStackTrace();
                    kiem(false, ten + " mo man: " + e);
                }
            }
        });
    }

    static java.util.Vector vec(int[] a) {
        java.util.Vector v = new java.util.Vector();
        for (int i = 0; i < a.length; i++) {
            v.addElement(new Integer(a[i]));
        }
        return v;
    }

    static int dexDau() {
        try {
            java.lang.reflect.Method m = game.pokemon.ManPokedex.class.getDeclaredMethod("dsLoai");
            m.setAccessible(true);
            java.util.Vector v = (java.util.Vector) m.invoke(null);
            return v.size() > 0 ? ((int[]) v.elementAt(0))[0] : 19;
        } catch (Throwable e) {
            return 19;
        }
    }

    public static void main(String[] a) throws Exception {
        System.setProperty("java.awt.headless", "true");
        ra = new File(a.length > 0 ? a[0] : "anh_trao_doi_pokemon");
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

        kiem(game.pokemon.IconBat.coAnh(), "co anh PNG /pk/bat_16.png + bat_32.png");

        String[] man = {"hop_pc", "hop_pc_chi_tiet", "hop_pc_chon_doi", "trao_doi", "trao_doi_chon", "trao_doi_xong", "pokedex_loai"};
        for (int c = 0; c < ChupTrongGame.CO.length; c++) {
            final int W = ChupTrongGame.CO[c][0], H = ChupTrongGame.CO[c][1], dpi = ChupTrongGame.CO[c][2];
            String tenCo = ChupTrongGame.TEN_CO[c];
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
                String t = man[m] + " " + tenCo;
                mo(man[m]);
                Thread.sleep(150);
                ChupTrongGame.ve(W, H);
                int dem0 = game.pokemon.Bang.demTam;
                int ic0 = game.pokemon.IconBat.demVe;
                BufferedImage anh = ChupTrongGame.veKiem(W, H, "KiemTraoDoiPokemon:330");
                ImageIO.write(anh, "png", new File(ra, man[m] + "_" + tenCo + ".png"));
                ChupTrongGame.kiemTam(t, dem0 - 1);
                ChupTrongGame.kiemCham(t, min, cach);
                kiem(game.pokemon.IconBat.demVe > ic0, t + " ve icon PNG (IconBat)");
                if (man[m].equals("hop_pc")) {
                    kiem(game.render.Canvas.currentScreen == game.pokemon.ManHopPc.me, t + " goi Hop PC mo man Hop PC");
                    kiem(daVe("Hộp PC 7/30"), t + " tieu de so con / suc chua");
                    kiem(coVung("con-0") && coVung("con-6") || coVung("con-0") && coVung("con-3"), t + " the Pokemon cham duoc");
                    kiem(daVe("Torchic") && daVe("cấp 18"), t + " ten + cap tren the");
                } else if (man[m].equals("hop_pc_chi_tiet")) {
                    kiem(coVung("hd-0") && coVung("hd-1") && coVung("hd-2"), t + " nut Di theo / Luyen / Tien hoa");
                    kiem(!coVung("hd-3") && !coVung("hd-4") && daVe("Chuyển đổi") && daVe("Thả"), t + " con dang theo: Chuyen doi / Tha bi tat (khong cham)");
                    kiem(daVe("Combusken") && daVe("Sức mạnh"), t + " ten + Suc manh");
                    kiem(daVe("Kiên quyết") && daVe("HP") && daVe("Kẹo Torchic"), t + " tinh cach + chi so ca the + keo");
                    kiem(daVe("Thôi theo"), t + " con dang theo: nut Thoi theo");
                } else if (man[m].equals("hop_pc_chon_doi")) {
                    kiem(coVung("dua-trao-doi"), t + " nut Dua vao trao doi");
                    kiem(daVe("Chọn Pokémon trao đổi (2/3)"), t + " tieu de dem chon 2/3");
                } else if (man[m].equals("trao_doi")) {
                    kiem(game.render.Canvas.currentScreen == game.pokemon.ManTraoDoiPokemon.me, t + " goi phien mo cua so trao doi");
                    kiem(coVung("td-huy") && coVung("td-chon") && coVung("td-xac-nhan"), t + " nut Huy / Doi / Trao doi");
                    kiem(daVe("Bạn đưa") && daVe("Mây Xanh đưa"), t + " hai cot");
                    kiem(daVe("7.184") && daVe("Phí trao đổi"), t + " phi xu hien truoc");
                    kiem(daVe("Đã khoá"), t + " trang thai khoa");
                } else if (man[m].equals("trao_doi_chon")) {
                    kiem(coVung("td-khoa") && coVung("td-chon"), t + " chua khoa: nut Khoa + Chon");
                    kiem(daVe("Thôi đi theo"), t + " ghi chu loi tu may chu");
                } else if (man[m].equals("trao_doi_xong")) {
                    kiem(coVung("td-dong") && coVung("td-hop"), t + " xong: Dong / Mo Hop PC");
                    kiem(daVe("tiến hoá thành Gengar"), t + " bao tien hoa khi trao doi");
                } else if (man[m].equals("pokedex_loai")) {
                    kiem(daVe("Độ hiếm: Ít gặp") && daVe("tỉ lệ gốc 25%"), t + " do hiem + ti le goc");
                    kiem(daVe("Rừng Viridian"), t + " noi song");
                    kiem(daVe("Tiến hoá thành Combusken"), t + " dong tien hoa that");
                    kiem(daVe("Đã bắt"), t + " trang thai da bat");
                }
            }
        }
        // lời mời trao đổi: hộp xác nhận
        NenPc.tinhKichThuoc(1280, 720);
        nap(goi(6, new Ghi() {
            public void ghi(DataOutputStream o) throws Exception {
                o.writeByte(1);
                o.writeShort(42);
                o.writeUTF("Mây Xanh");
                o.writeByte(30);
            }
        }));
        Thread.sleep(150);
        ImageIO.write(ChupTrongGame.veKiem(1280, 720, "KiemTraoDoiPokemon:380"), "png", new File(ra, "loi_moi.png"));
        kiem(game.pokemon.HopXacNhan.dangMo(), "loi moi trao doi: hien hop xac nhan");
        // đóng phiên từ máy chủ: về màn chơi
        nap(goiPhien(1, 1, ""));
        Thread.sleep(100);
        nap(goi(6, new Ghi() {
            public void ghi(DataOutputStream o) throws Exception {
                o.writeByte(0);
                o.writeUTF("Mây Xanh đã rời game, trao đổi bị huỷ.");
            }
        }));
        Thread.sleep(100);
        kiem(game.render.Canvas.currentScreen != game.pokemon.ManTraoDoiPokemon.me && game.pokemon.BatPokemon.tdTrangThai == 0,
                "may chu dong phien: roi cua so trao doi");
        kiem(ChupTrongGame.hong == 0, "chu trong khung + icon PNG (ChupTrongGame.veKiem): " + ChupTrongGame.hong + " hong");
        System.out.println("KiemTraoDoiPokemon: " + dat + " dat, " + hong + " hong -> " + ra.getAbsolutePath());
        System.exit(hong == 0 ? 0 : 1);
    }
}
