import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import javax.imageio.ImageIO;
import javax.microedition.lcdui.NenPc;
import javax.microedition.midlet.MIDlet;

/**
 * KIỂM LUỒNG NGƯỜI CHƠI MỚI (báo lỗi chủ dự án 0.9.21: "Android tạo tài khoản mới, chọn nhân vật có vẻ lỗi"), headless,
 * không máy chủ (phiên giả ghi gói gửi đi), đường vẽ bản PC:
 *  1) màn Đăng ký / Đăng nhập / Tạo Pokémon / Chọn Pokémon ở mọi cỡ APK thật (ChupTrongGame.CO_APK: KhungCoDinh + 44 dp),
 *     cỡ PC và (-Dchup.j2me) cỡ J2ME: chụp ảnh, mọi vùng chạm lớp trên đủ cỡ / cách nhau / trong màn / bấm tâm trúng
 *     (ChupTrongGame.kiemCham), chữ không thiếu nét; màn Tạo: đủ 5 Pokémon khởi đầu chạm được, ô Tên + nút Tạo trong màn,
 *     ô Tên không bị vùng chạm khác che;
 *  2) chạm Pokémon khởi đầu i -> chọn đúng i (vòng chọn + tên nổi), chạm Vùng đổi vùng;
 *  3) tên từ bàn phím Android (TextBox: gõ được mọi ký tự): dấu cách thừa đầu / cuối bị bỏ, chữ có dấu / dấu cách giữa / quá
 *     dài -> báo NGAY ở máy (không gửi gói 14), nhãn ô Tên nói đúng giới hạn máy chủ (4-10 ký tự);
 *  4) chạm Tạo hai lần liền (chạm đúp / máy lag) -> đúng MỘT gói 14 (lỗi: gói thứ hai làm máy chủ trả "Xin chọn tên khác"
 *     dù Pokémon đã được tạo); máy chủ trả lỗi tên (hộp báo) -> chạm Tạo lại gửi được;
 *  5) máy chủ trả danh sách (gói tạo xong) -> vào game luôn bằng Pokémon vừa tạo (31/10/2026), hộp "Xin chờ" đóng.
 *
 *   javac -cp client_pc/out/build/cls -d $O client_pc/test/ChupTrongGame.java client_pc/test/KiemTaoNhanVat.java \
 *       server/src/com/htbb/pokemon/IconPokemon.java server/src/com/htbb/pokemon/IconPokemonBang.java
 *   java -Djava.awt.headless=true -cp client_pc/out/build/cls:client_pc/out/build/res:$O KiemTaoNhanVat <thư mục ảnh>
 * -Dchup.j2me=1: thêm cỡ J2ME (vẽ như máy J2ME, phím mềm). -Dchup.co=apk720x1600d2,... chạy một phần. Mã thoát 1 = có lỗi.
 */
public class KiemTaoNhanVat {
    static int dat, hong;
    static final List<Integer> LENH = new ArrayList<Integer>();
    static final List<byte[]> GOI = new ArrayList<byte[]>();

    static void kiem(boolean ok, String ten) {
        if (ok) dat++;
        else {
            hong++;
            System.out.println("HONG: " + ten);
        }
    }

    static void chay(Runnable r) {
        try {
            ChupTrongGame.chay(r);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    static void ngu(int ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
        }
    }

    static int demGoi(int lenh) {
        int n = 0;
        synchronized (LENH) {
            for (int i = 0; i < LENH.size(); i++) if (LENH.get(i) == lenh) n++;
        }
        return n;
    }

    static String tenGoiCuoi14() {
        synchronized (LENH) {
            for (int i = LENH.size() - 1; i >= 0; i--) {
                if (LENH.get(i) == 14) {
                    try {
                        return new java.io.DataInputStream(new java.io.ByteArrayInputStream(GOI.get(i))).readUTF();
                    } catch (Exception e) {
                        return null;
                    }
                }
            }
        }
        return null;
    }

    public static void main(String[] a) throws Exception {
        System.setProperty("java.awt.headless", "true");
        game.pokemon.Bang.dungHieuUng = true;
        File ra = new File(a.length > 0 ? a[0] : "anh_tao_nhan_vat");
        ra.mkdirs();
        ChupTrongGame.RA = ra;
        System.setProperty("htbb.duLieu", new File(ra, "du_lieu").getAbsolutePath());
        System.setProperty("microedition.platform", "HTBB-PC/kiem");
        System.setProperty("microedition.locale", "vi-VN");
        htbb.pc.CaiDat.thuMucDuLieu();
        htbb.pc.CaiDat.nap();
        htbb.pc.ChuHD.bat = true;
        htbb.pc.GiaoDienHD.bat = true;
        htbb.pc.CaiDat.tatTieng = true;
        MIDlet.GHI_DE.put("HTBB-May-Chu", "127.0.0.1:1");
        MIDlet.GHI_DE.put("HTBB-Ban-Thu", "0");
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
        for (int i = 0; i < 100 && (game.render.Canvas.instance == null || game.render.Canvas.currentScreen == null); i++) ngu(100);
        ngu(2500);
        game.networklogic.GameService.gI().setSession(new network.ISession() {
            public boolean isConnected() {
                return true;
            }

            public void setHandler(network.IMessageHandler h) {
            }

            public void connect(String s, String s2) {
            }

            public void sendMessage(network.Message m) {
                synchronized (LENH) {
                    LENH.add((int) m.command);
                    GOI.add(m.getData());
                }
            }

            public void close() {
            }
        });
        boolean j2 = System.getProperty("chup.j2me") != null;
        if (j2) {
            try {
                // 0.9.22: máy J2ME thật ném lỗi khi gọi sai đặc tả MIDP (bản cũ không có lớp này)
                Class.forName("javax.microedition.lcdui.NghiemMidp").getField("bat").setBoolean(null, true);
            } catch (Throwable e) {
            }
            htbb.pc.GiaoDienHD.bat = false;
            htbb.pc.ChuHD.bat = false;
            game.pokemon.ChuUI.bo = null;
        }
        String locCo = System.getProperty("chup.co");
        List<int[]> cos = new ArrayList<int[]>();
        List<String> tens = new ArrayList<String>();
        if (!j2) {
            for (int i = 0; i < ChupTrongGame.CO_APK.length; i++) {
                cos.add(ChupTrongGame.CO_APK[i]);
                tens.add(ChupTrongGame.TEN_CO_APK[i]);
            }
            int[][] pc = {{1280, 720, 0}, {1920, 1080, 0}, {1024, 768, 0}};
            String[] tpc = {"pc720", "pc1080", "pc4x3"};
            for (int i = 0; i < pc.length; i++) {
                cos.add(pc[i]);
                tens.add(tpc[i]);
            }
        } else {
            for (int i = 0; i < ChupTrongGame.CO_J2ME.length; i++) {
                cos.add(ChupTrongGame.CO_J2ME[i]);
                tens.add(ChupTrongGame.TEN_CO_J2ME[i]);
            }
        }
        String[] man = {"dang_ky", "dang_nhap", "tao_nhan_vat", "chon_nhan_vat"};
        for (int c = 0; c < cos.size(); c++) {
            final String tenCo = tens.get(c);
            if (locCo != null && !("," + locCo + ",").contains("," + tenCo + ",")) continue;
            final int[] co = cos.get(c);
            final int W = co[0], H = co[1], dpi = co[2];
            int min, cach;
            game.pokemon.Bang.anPhai = 0;
            if (dpi < 0) {
                ChupTrongGame.datCoJ2me(W, H);
                min = 0;
                cach = 0;
                game.pokemon.Bang.anTrai = 0;
            } else {
                NenPc.tinhKichThuoc(W, H);
            }
            chay(new Runnable() {
                public void run() {
                    game.render.Canvas.instance.sizeChanged(NenPc.rong, NenPc.cao);
                }
            });
            if (co.length > 3) {
                ChupTrongGame.datCoApk(W, H, dpi, co[3]);
                chay(new Runnable() {
                    public void run() {
                        game.render.Canvas.instance.sizeChanged(NenPc.rong, NenPc.cao);
                    }
                });
                min = game.pokemon.VungCham.MIN;
                cach = game.pokemon.VungCham.CACH;
            } else if (dpi == 0) {
                float s = NenPc.hienW / (float) NenPc.rong;
                min = (int) Math.ceil(32 / s);
                cach = (int) Math.ceil(8 / s);
                game.pokemon.Bang.anTrai = 0;
            } else {
                min = 0;
                cach = 0;
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
                ngu(150);
                ChupTrongGame.ve(W, H);
                ngu(100);
                game.pokemon.VeTay.xoa();
                game.pokemon.GhiChu.xoa();
                game.pokemon.GhiChu.bat = true;
                game.render.screen.Font.thieuNet = 0;
                BufferedImage anh = ChupTrongGame.ve(W, H);
                game.pokemon.GhiChu.bat = false;
                String nhan = ten + " " + tenCo;
                ImageIO.write(anh, "png", new File(ra, ten + "_" + tenCo + ".png"));
                kiem(game.render.Canvas.currentDialog == null, nhan + ": hop thoai de len man (" + game.render.Canvas.currentDialog + ")");
                if (dpi >= 0) {
                    kiemCham(nhan, min, cach);
                    ChupTrongGame.kiemChu(nhan);
                }
                if ("tao_nhan_vat".equals(ten)) kiemManTao(nhan, dpi < 0, anh);
            }
            if (dpi >= 0) {
                kiemLuongTao(tenCo, W, H);
            } else {
                kiemLuongTaoPhim(tenCo, W, H);
            }
        }
        dat += ChupTrongGame.dat;
        hong += ChupTrongGame.hong;   // kiemChu ghi vào bộ đếm của ChupTrongGame
        System.out.println("KiemTaoNhanVat: " + dat + " dat, " + hong + " hong -> " + ra.getAbsolutePath());
        System.exit(hong == 0 ? 0 : 1);
    }

    /**
     * Vùng chạm lớp trên (như ChupTrongGame.kiemCham) nhưng cỡ tính theo vùng chạm THẬT (VungCham.vung: đã nới tới MIN - ô
     * nhập / chữ "Quên mật khẩu" vẽ thấp hơn 44 dp vẫn chạm đủ cỡ): >= min, trong màn, bấm tâm trúng, cách nhau >= cach.
     */
    static void kiemCham(String nhan, int min, int cach) {
        int n = game.pokemon.VungCham.dem(), top = game.pokemon.VungCham.lopCaoNhat(), so = 0;
        for (int i = 0; i < n; i++) {
            if (game.pokemon.VungCham.lopVung(i) != top) continue;
            String ten = game.pokemon.VungCham.ten(i);
            if (ten != null && ten.startsWith("nen")) continue;
            so++;
            int[] v = game.pokemon.VungCham.vung(i);
            String t = nhan + " [" + ten + "]";
            kiem(v[2] >= min && v[3] >= min, t + " co cham " + v[2] + "x" + v[3] + " >= " + min);
            kiem(v[0] >= 0 && v[1] >= 0 && v[0] + v[2] <= game.render.Canvas.w + 1 && v[1] + v[3] <= game.render.Canvas.h + 1,
                    t + " trong man (" + v[0] + "," + v[1] + " " + v[2] + "x" + v[3] + ", man " + game.render.Canvas.w + "x" + game.render.Canvas.h + ")");
            int j = game.pokemon.VungCham.tim(v[0] + v[2] / 2, v[1] + v[3] / 2);
            kiem(j == i, t + " bam tam trung (" + (j < 0 ? "-" : game.pokemon.VungCham.ten(j)) + ")");
            for (int q = i + 1; q < n; q++) {
                if (game.pokemon.VungCham.lopVung(q) != top) continue;
                String tq = game.pokemon.VungCham.ten(q);
                if (tq != null && tq.startsWith("nen")) continue;
                // khoảng cách theo hình VẼ (như ChupTrongGame.kiemCham): vùng nới tới MIN được phép lấn mép nhau
                v = game.pokemon.VungCham.vungVe(i);
                int[] w = game.pokemon.VungCham.vungVe(q);
                boolean trong = w[0] >= v[0] && w[1] >= v[1] && w[0] + w[2] <= v[0] + v[2] && w[1] + w[3] <= v[1] + v[3]
                        || v[0] >= w[0] && v[1] >= w[1] && v[0] + v[2] <= w[0] + w[2] && v[1] + v[3] <= w[1] + w[3];
                if (trong) continue;
                int ho = Math.max(Math.max(w[0] - (v[0] + v[2]), v[0] - (w[0] + w[2])), Math.max(w[1] - (v[1] + v[3]), v[1] - (w[1] + w[3])));
                kiem(ho >= cach, t + " x [" + tq + "] cach " + ho + " >= " + cach);
            }
        }
        kiem(so > 0, nhan + " co vung cham");
    }

    static game.render.screen.CreateCharScr tao() {
        return game.render.screen.CreateCharScr.gI();
    }

    static int lopDangChon() {
        try {
            java.lang.reflect.Field f = game.render.screen.CreateCharScr.class.getDeclaredField("currentClazz");
            f.setAccessible(true);
            return f.getInt(tao());
        } catch (Exception e) {
            return -1;
        }
    }

    /** Màn Tạo vừa vẽ: 5 khởi đầu chạm được, ô Tên + nút Tạo trong màn, ô Tên không bị vùng khác chồng. */
    static void kiemManTao(String nhan, boolean j2me, BufferedImage anh) {
        game.render.screen.TField tf = tao().tfChat;
        int w = game.render.Canvas.w, h = game.render.Canvas.h;
        kiem(tf.x >= 0 && tf.y >= 0 && tf.x + tf.width <= w && tf.y + tf.height <= h - (j2me ? game.render.screen.Screen.deltaY : 0),
                nhan + ": o Ten trong man (" + tf.x + "," + tf.y + " " + tf.width + "x" + tf.height + ", man " + w + "x" + h + ")");
        kiem(tf.width >= 60, nhan + ": o Ten du rong (" + tf.width + ")");
        for (int i = 0; i < 5; i++) kiem(ChupTrongGame.coVungDung("khoi-dau-" + i), nhan + ": thieu vung cham Pokemon khoi dau " + i);
        if (!j2me) {
            kiem(ChupTrongGame.coVungDung("tao-1") && ChupTrongGame.coVungDung("tao-0"), nhan + ": thieu nut Tao / Quay lai");
            kiem(ChupTrongGame.coVungDung("vung"), nhan + ": thieu hang Vung");
            // ô Tên không bị vùng chạm khác (khởi đầu, vùng, nút) đè lên
            int top = game.pokemon.VungCham.lopCaoNhat();
            for (int i = 0; i < game.pokemon.VungCham.dem(); i++) {
                if (game.pokemon.VungCham.lopVung(i) != top) continue;
                String t = game.pokemon.VungCham.ten(i);
                if (t == null || t.startsWith("nen") || t.equals("o-nhap")) continue;   // o-nhap = chính ô Tên
                int[] v = game.pokemon.VungCham.vung(i);
                boolean chong = v[0] < tf.x + tf.width && tf.x < v[0] + v[2] && v[1] < tf.y + tf.height && tf.y < v[1] + v[3];
                kiem(!chong, nhan + ": vung cham [" + t + "] chong o Ten");
            }
            // nhãn ô Tên nói đúng giới hạn máy chủ
            kiem(!ChupTrongGame.coChu("4-15"), nhan + ": nhan o Ten ghi 4-15 ky tu (may chu chi nhan 4-10)");
        }
    }

    static void ve(int W, int H) {
        try {
            ChupTrongGame.ve(W, H);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    static void cham(final String vung, int W, int H) {
        chay(new Runnable() {
            public void run() {
                ChupTrongGame.chamVung(vung);
            }
        });
        ve(W, H);
    }

    /** Gõ tên như hộp nhập Android trả về (TextBox -> TField.setText). */
    static void datTen(final String t) {
        chay(new Runnable() {
            public void run() {
                tao().tfChat.setText(t);
            }
        });
    }

    static void xoaGoi() {
        synchronized (LENH) {
            LENH.clear();
            GOI.clear();
        }
    }

    static void moTao(int W, int H) {
        chay(new Runnable() {
            public void run() {
                game.render.screen.CreateCharScr.me = null;
                moMan("tao_nhan_vat");
            }
        });
        ve(W, H);
        ve(W, H);
    }

    static void kiemLuongTao(String co, int W, int H) {
        String nhan = "luong " + co;
        moTao(W, H);
        // chọn Pokémon khởi đầu bằng chạm
        for (int i = 4; i >= 0; i--) {
            cham("khoi-dau-" + i, W, H);
            kiem(lopDangChon() == i, nhan + ": cham khoi dau " + i + " -> chon " + lopDangChon());
        }
        cham("khoi-dau-3", W, H);
        int vung0 = game.render.screen.CreateCharScr.selectNation;
        cham("vung", W, H);
        kiem(game.render.screen.CreateCharScr.selectNation != vung0, nhan + ": cham Vung khong doi vung");
        // tên có dấu cách cuối (bàn phím Android gợi ý từ thêm dấu cách) -> bỏ dấu cách, gửi được
        xoaGoi();
        datTen("Pikachu ");
        cham("tao-1", W, H);
        kiem(demGoi(14) == 1 && "Pikachu".equals(tenGoiCuoi14()), nhan + ": ten 'Pikachu ' (dau cach cuoi) -> gui 'Pikachu' (" + demGoi(14)
                + " goi, ten '" + tenGoiCuoi14() + "')");
        // chạm Tạo lần nữa khi đang chờ -> không gửi thêm
        cham("tao-1", W, H);
        chay(new Runnable() {
            public void run() {
                // chạm đúp: cú chạm thứ hai trước khi hộp chờ kịp vẽ
                ChupTrongGame.chamVung("tao-1");
            }
        });
        ve(W, H);
        kiem(demGoi(14) == 1, nhan + ": cham Tao 2-3 lan -> " + demGoi(14) + " goi tao (phai 1)");
        // máy chủ báo lỗi tên (gói 14) -> hộp báo; đóng rồi Tạo lại gửi được
        chay(new Runnable() {
            public void run() {
                game.render.Canvas.endDlg();
                game.render.Canvas.startOKDlg("Xin chọn tên Pokémon khác");
                game.render.Canvas.endDlg();
            }
        });
        ve(W, H);
        datTen("Pikachu2");
        cham("tao-1", W, H);
        kiem(demGoi(14) == 2, nhan + ": sau loi may chu, Tao lai khong gui (" + demGoi(14) + " goi)");
        // chữ có dấu / dấu cách giữa / quá dài: báo ở máy, không gửi
        String[] sai = {"Pikachú", "Pika chu", "Pikachu12345", "Pik"};
        for (int i = 0; i < sai.length; i++) {
            chay(new Runnable() {
                public void run() {
                    game.render.Canvas.endDlg();
                }
            });
            ve(W, H);
            xoaGoi();
            datTen(sai[i]);
            cham("tao-1", W, H);
            kiem(demGoi(14) == 0, nhan + ": ten sai '" + sai[i] + "' van gui may chu");
            kiem(game.render.Canvas.currentDialog != null, nhan + ": ten sai '" + sai[i] + "' khong bao loi");
        }
        chay(new Runnable() {
            public void run() {
                game.render.Canvas.endDlg();
            }
        });
        kiemNhanDanhSach(nhan, W, H);
    }

    /** J2ME: phím lên / xuống / trái / phải + phím giữa (center). */
    static void kiemLuongTaoPhim(String co, int W, int H) {
        String nhan = "luong " + co;
        moTao(W, H);
        phim(-2);   // xuống: dòng Pokémon
        phim(-4);
        phim(-4);
        kiem(lopDangChon() == 3, nhan + ": phim phai x2 tu dong Pokemon -> " + lopDangChon());
        phim(-1);   // lên: ô Tên
        xoaGoi();
        datTen("Pikachu ");
        chay(new Runnable() {
            public void run() {
                game.render.screen.Screen s = game.render.Canvas.currentScreen;
                s.center.action.perform();
                s.center.action.perform();
            }
        });
        ve(W, H);
        kiem(demGoi(14) == 1 && "Pikachu".equals(tenGoiCuoi14()), nhan + ": phim giua 2 lan, ten 'Pikachu ' -> " + demGoi(14) + " goi '"
                + tenGoiCuoi14() + "'");
        chay(new Runnable() {
            public void run() {
                game.render.Canvas.endDlg();
            }
        });
        kiemNhanDanhSach(nhan, W, H);
    }

    static void phim(final int k) {
        chay(new Runnable() {
            public void run() {
                game.render.Canvas.instance.keyPressed(k);
                game.render.Canvas.instance.keyReleased(k);
            }
        });
        ngu(30);
        chay(new Runnable() {
            public void run() {
                game.render.Canvas.currentScreen.update();
            }
        });
    }

    /** Máy chủ trả danh sách Pokémon (tạo xong) -> màn Chọn có Pokémon mới, hộp chờ đóng. */
    static void kiemNhanDanhSach(String nhan, int W, int H) {
        final int chon0 = demGoi(13);
        final String vua = game.render.screen.CreateCharScr.tenVuaTao;
        chay(new Runnable() {
            public void run() {
                network.Session_ME.gI().connected = true;   // CharSelectScr.setCharList bỏ gói khi mất kết nối
                game.render.Canvas.startWaitDlg("Xin chờ", true);
                try {
                    java.io.ByteArrayOutputStream bo = new java.io.ByteArrayOutputStream();
                    java.io.DataOutputStream d = new java.io.DataOutputStream(bo);
                    viDanhSach(d);
                    java.lang.reflect.Constructor<network.Message> k = network.Message.class.getDeclaredConstructor(byte.class, byte[].class);
                    k.setAccessible(true);
                    game.networklogic.MessageHandler.gI().onMessage(k.newInstance((byte) 13, bo.toByteArray()));
                    network.Session_ME.gI().connected = false;
                } catch (Throwable e) {
                    e.printStackTrace();
                    kiem(false, "goi danh sach: " + e);
                }
            }
        });
        ve(W, H);
        ve(W, H);
        // 31/10/2026 (KiemHanhTrinhUI): danh sách có đúng Pokémon VỪA gửi tạo -> vào game luôn (gói chọn 13), không bắt chạm
        // "Vào game"; tên khác (lần tạo cuối là tên khác) -> màn Chọn như cũ
        if ("Pikachu".equalsIgnoreCase(vua)) {
            kiem(game.render.Canvas.currentScreen instanceof game.render.screen.ChangScr && demGoi(13) > chon0, nhan
                    + ": sau tao xong khong vao game ngay bang Pokemon vua tao (" + game.render.Canvas.currentScreen + ", goi 13: "
                    + (demGoi(13) - chon0) + ")");
        } else {
            kiem(game.render.Canvas.currentScreen instanceof game.render.screen.CharSelectScr && demGoi(13) == chon0, nhan
                    + ": danh sach khong co Pokemon vua tao (" + vua + ") ma khong ve man Chon (" + game.render.Canvas.currentScreen + ")");
        }
        kiem(game.render.Canvas.currentDialog == null, nhan + ": hop cho van mo sau tao xong (" + game.render.Canvas.currentDialog + ")");
    }

    /** Gói danh sách Pokémon (lệnh 13 máy chủ -> client) một Pokémon - dựng theo MessageHandler. */
    static void viDanhSach(java.io.DataOutputStream d) throws java.io.IOException {
        d.writeByte(1);
        d.writeInt(1234);
        d.writeUTF("Pikachu");
        d.writeByte(0);
        d.writeByte(0);      // không món đồ
        d.writeShort(1);     // cấp
        d.writeByte(0);      // using
        d.writeByte(0);      // vùng
        d.writeShort(0);     // ngày
        d.writeByte(-1);     // không ảnh màu
    }

    static void moMan(String ten) {
        game.render.Canvas.endDlg();
        game.render.Canvas.isLoad = false;
        if ("dang_nhap".equals(ten)) game.render.Canvas.loginScr.switchToMe();
        else if ("dang_ky".equals(ten)) game.render.screen.DangKyScr.mo();
        else if ("tao_nhan_vat".equals(ten)) game.render.screen.CreateCharScr.gI().switchToMe();
        else if ("chon_nhan_vat".equals(ten)) {
            try {
                game.render.screen.CharSelectScr s = game.render.screen.CharSelectScr.gI();
                game.model.MainChar[] ds = new game.model.MainChar[1];
                ds[0] = new game.model.MainChar();
                ds[0].name = "Pikachu99";
                ds[0].lastLv = 12;
                ds[0].weaponType = 7;
                ds[0].using = 1;
                ds[0].nationID = -1;
                java.lang.reflect.Field f1 = s.getClass().getDeclaredField("chars");
                f1.setAccessible(true);
                f1.set(s, ds);
                s.charInfos = ds;
                s.cmdSelect = new game.model.Command("Chọn", null);
                s.cmdNew = new game.model.Command("Tạo mới", null);
                s.cmdDel = new game.model.Command("Xóa", null);
                s.center = s.cmdSelect;
                s.switchToMe();
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }
    }
}
