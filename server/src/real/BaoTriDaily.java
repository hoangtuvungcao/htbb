/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package real;

import data.Database;
import data.Logdata;
import data.NewClan;
import io.Message;
import java.awt.Desktop;
import java.io.File;
import java.util.Date;
import java.util.Random;
import server.TeamServer;
import util.Logger;

/**
 *
 *
 */
public class BaoTriDaily implements Runnable {

    public static boolean Running = false;
    public static boolean BaoTriTay = false;
    public static String domain = "";
    public static int Server = 1;
    
    // Thêm biến để lưu giờ và phút bảo trì
    private static int maintenanceHour;
    private static int maintenanceMinute;
    // Gio:phut canh bao, dung 1 phut truoc gio bao tri (tinh theo tong so phut trong
    // ngay de khoi le khi maintenanceMinute = 0).
    private static int warnHour;
    private static int warnMinute;
    // Chi gui canh bao dung MOT lan - JVM nay chi trai qua dung mot lan bao tri ngau
    // nhien roi thoat (xem cuoi run()), nen khong can dat lai ve false.
    private static boolean daBaoTruoc = false;

    // Khởi tạo thời gian bảo trì random khi class được load
    static {
        Random r = new Random();
        // Random giờ từ 4-5
        maintenanceHour = 4 + r.nextInt(2);
        // Random phút từ 30-59 nếu là 4h, hoặc 0-30 nếu là 5h
        if (maintenanceHour == 4) {
            maintenanceMinute = 30 + r.nextInt(30);
        } else {
            maintenanceMinute = r.nextInt(31);
        }
        final int tongPhutBaoTri = maintenanceHour * 60 + maintenanceMinute;
        final int tongPhutCanhBao = (tongPhutBaoTri - 1 + 24 * 60) % (24 * 60);
        warnHour = tongPhutCanhBao / 60;
        warnMinute = tongPhutCanhBao % 60;
        System.out.println("Thời gian bảo trì được set: " + maintenanceHour + ":" + maintenanceMinute);
    }

    /** Tat han bao tri tu dong hang ngay (sv.gioBaoTri=tat). */
    private static volatile boolean tatBaoTri = false;

    /**
     * Dat gio bao tri tu server.ini (sv.gioBaoTri): "HH:MM" = co dinh, "tat" = khong tu tat may hang
     * ngay, trong/sai = ngau nhien 4:30-5:30 nhu cu. Goi tu TeamServer luc doc cau hinh. Unit systemd
     * (vps/htbb-game.service, Restart=always) tu bat lai sau khi bao tri.
     */
    public static void datGio(final String s) {
        if (s == null || s.trim().isEmpty()) {
            // Goi lai luc dang chay (CauHinhTrungTam, sv.gioBaoTri doi thanh trong): bo "tat", giu gio dang co.
            tatBaoTri = false;
            return;
        }
        final String v = s.trim();
        if (v.equalsIgnoreCase("tat") || v.equalsIgnoreCase("off")) {
            tatBaoTri = true;
            System.out.println("BAO TRI HANG NGAY -> TAT (sv.gioBaoTri=" + v + ")");
            return;
        }
        try {
            final String[] hm = v.split(":");
            final int h = Integer.parseInt(hm[0].trim());
            final int mi = Integer.parseInt(hm[1].trim());
            if (h < 0 || h > 23 || mi < 0 || mi > 59) {
                throw new IllegalArgumentException();
            }
            maintenanceHour = h;
            maintenanceMinute = mi;
            tatBaoTri = false;
            daBaoTruoc = false;
            final int tongPhutCanhBao = (h * 60 + mi - 1 + 24 * 60) % (24 * 60);
            warnHour = tongPhutCanhBao / 60;
            warnMinute = tongPhutCanhBao % 60;
            System.out.println("Thời gian bảo trì (sv.gioBaoTri): " + h + ":" + mi);
        } catch (final Exception e) {
            System.out.println("sv.gioBaoTri sai dinh dang \"" + v + "\" (can HH:MM hoac tat) - giu gio ngau nhien");
        }
    }

    // Bao truoc bao tri cho nguoi dang online. Chi thong bao, KHONG tat server - viec tat
    // de cho systemctl stop (SIGTERM -> shutdown hook luu du lieu), xem vps/deploy-auto.sh.
    //
    // Dong chay tren dinh man hinh, giong nut "Bao Tri" cua AdminPanel - cung dung
    // MessageCreator.createthongbao (tin 38). KHONG dung createServerInfoMessage vi ham do
    // doc RealController.maquee, ma maquee khac rong thi LoginHandler/SelectCharHandler gui
    // lai cho MOI nguoi moi dang nhap -> thanh mot duong ton thong bao thu hai.
    //
    // CHI GUI DUNG MOT TIN, cang som cang tot (60 giay truoc khi tat).
    // Ly do: client nhet tin 38 vao hang doi TINH GameScr.serverInfo va paintServerInfo chi
    // chay khi nguoi choi dang o man hinh game, moi tin mat 15-20 giay moi troi het. Hang
    // doi khong bi xoa khi mat ket noi, nen tin nao chua troi kip luc tat server se chay
    // tiep NGAY SAU KHI ho dang nhap lai - hien "bao tri sau 1 phut" luc bao tri da xong.
    // Ban dau gui 4 moc 60/30/20/10 nen moc 10s gan nhu chac chan con dong lai; mot tin gui
    // truoc 60 giay thi troi xong tu luc con 40 giay.
    public static void baoTruocBaoTri(final int giay) {
        try {
            final String khiNao = (giay >= 60 && giay % 60 == 0) ? (giay / 60) + " phút" : giay + " giây";
            final String noiDung = "Hệ thống sẽ bảo trì sau " + khiNao
                    + ". Vui lòng thoát game để tránh mất dữ liệu!";
            final Message m = MessageCreator.createthongbao(noiDung);
            int soNguoiChoiThat = 0;
            for (int i = 0; i < CharManager.instance.vChars.size(); ++i) {
                final Char c = CharManager.instance.vChars.elementAt(i);
                c.sendMessage(m);
                // vChars con chua ca NPC/bot/thu cuoi thue (isBot != -1), phai loc rieng
                // nguoi choi that (isBot == -1) thi so moi dung, xem RealController.saveAllChar.
                if (c.isBot == -1) {
                    soNguoiChoiThat++;
                }
            }
            m.cleanup();
            System.out.println("BAO TRI SAU " + giay + "s (" + soNguoiChoiThat + " nguoi online)");
        } catch (final Exception ex) {
        }
    }

    @Override
    public synchronized void run() {
        try {
            if (Running) {
                return;
            }
            Running = true;
            System.out.println("Thời gian bảo trì được set: " + maintenanceHour + ":" + maintenanceMinute);
            Database.instance.loadTangExp();
            Database.instance.loadTangNap();
            Database.instance.loadPercentDrop();
            Database.instance.loadTileDrop();
            while (Running) {
                //Map.openvantieu = false;
                Date d = new Date();
                if (d.getSeconds() % 5 == 0) {
                    BaoTriTay = Database.instance.baotriCMD();
                    // Deploy tu dong bat co nay de bao truoc cho nguoi dang online. No CHI bao,
                    // khong tu tat: vps/deploy-auto.sh goi systemctl stop sau khi het dem nguoc.
                    if (Database.instance.baoTriThongBaoCMD()) {
                        baoTruocBaoTri(60);
                    }
                }
                // Bao truoc 60s cho DUNG gio bao tri ngau nhien hang ngay - nhanh nay truoc
                // day khong bao gi ca (chi deploy moi tu bat co baotri_thongbao o tren), nen
                // deploy-auto.sh "di nho" gio bao tri nay se lam mat luon canh bao nguoi choi
                // dang duoc huong moi lan deploy. daBaoTruoc chan gui lap trong 5s dung.
                if (!tatBaoTri && !daBaoTruoc && d.getHours() == warnHour && d.getMinutes() == warnMinute && d.getSeconds() <= 5) {
                    baoTruocBaoTri(60);
                    daBaoTruoc = true;
                }
                if (d.getSeconds() == 0) {
                    // SECURITY: disabled external heartbeat call that sent online count to a remote server (api.url).
                }
                // GD2: job dem cua thiet ke dai han (tat co: khong lam gi). Xem LauDaiJob.
                LauDaiJob.nhip(com.htbb.pokemon.DongHo.ngay()); // dong ho ao tren may thu (DongHo), gio that tren may that
                if (d.getMinutes() == 0 && d.getSeconds() == 0) {
                    Database.instance.loadTangExp();
                    Database.instance.loadTangNap();
                    Database.instance.loadPercentDrop();
                    Database.instance.loadTileDrop();
                }
                if ((!tatBaoTri && d.getHours() == maintenanceHour && d.getMinutes() == maintenanceMinute && d.getSeconds() <= 5) || BaoTriTay) {
                    try {
                        this.notifyAll();
                    } catch (final Exception ex) {
                    }
                    BaoTriTay = false;
                    System.out.println("CHUAN BI THOAT");
                    AdminHandler.isStopServer = true;
                    System.out.println("update clandata");
                    NewClan.updateAllClan();
                    System.out.println("save char, event, log");
                    RealController.intance.saveAllChar(true);
                    System.out.println("SAVE CHARSELL");
                    ItemMarket.saveAllCharSell();
                    MonsterVantieu.saveAllMonster();
                    Database.instance.saveCooking(Char.nauBanhChung, 1);
                    Database.instance.saveCooking(Char.nauBanhTet, 2);
                    Database.instance.saveEvent(Map.event.getInfo());
                    Database.instance.xaTatCaLogMuaShop();
                    Database.instance.saveAllLog(Logdata.query);
                    System.out.println("save topLienTram");
                    for (int i = 0; i < 3; ++i) {
                        Database.instance.saveTopienTram(Map.topPK.get(i));
                    }
                    TeamServer.running = false;
                    System.out.println("CHUAN BI THOAT4");
                    System.out.println("THOAT");
                    Logger.logStringWithDate(String.valueOf(MapTown.count_all_item_sell_per_day) + "_" + ItemSell.ID_ITEM_SELL_AUTO_INCREMENT, "matketinfo.txt");
                    try {
                        Desktop.getDesktop().open(new File("run.bat"));
                    } catch (Exception e) {
                    }
                    System.exit(0);
                }
                Thread.sleep(1000);
            }
        } catch (InterruptedException e) {
        }
    }
}
