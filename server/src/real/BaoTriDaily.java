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
import io.SessionManager;
import java.awt.Desktop;
import java.io.File;
import java.util.Date;
import java.util.Random;
import server.TeamServer;
import util.Logger;
import util.Net;

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
        System.out.println("Thời gian bảo trì được set: " + maintenanceHour + ":" + maintenanceMinute);
    }

    // Dem nguoc bao tri cho nguoi dang online: chay marquee o moc 60s, 30s, 10s roi thoi.
    // Chi gui thong bao, KHONG tat server - viec tat de cho systemctl stop (SIGTERM ->
    // shutdown hook luu du lieu). Chay trong thread cua BaoTriDaily nen ngu o day khong
    // anh huong gi ngoai viec hoan vong kiem tra bao tri lai vai chuc giay.
    public static void demNguocBaoTri(final int giay) {
        int conLai = giay;
        try {
            while (conLai > 0) {
                RealController.maquee = "Hệ thống bảo trì sau " + conLai
                        + " giây. Vui lòng thoát game để tránh mất mát dữ liệu.";
                final Message m = MessageCreator.createServerInfoMessage();
                for (int i = 0; i < CharManager.instance.vChars.size(); ++i) {
                    CharManager.instance.vChars.elementAt(i).sendMessage(m);
                }
                m.cleanup();
                System.out.println("BAO TRI SAU " + conLai + "s (" + CharManager.instance.vChars.size() + " nguoi online)");
                final int buoc = (conLai > 30) ? 30 : ((conLai > 10) ? 10 : conLai);
                Thread.sleep(buoc * 1000L);
                conLai -= buoc;
            }
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
                        demNguocBaoTri(60);
                    }
                }
                if (d.getSeconds() == 0) {
                    // SECURITY: disabled external heartbeat call that sent online count to a remote server (api.url).
                }
                if (d.getMinutes() == 0 && d.getSeconds() == 0) {
                    Database.instance.loadTangExp();
                    Database.instance.loadTangNap();
                    Database.instance.loadPercentDrop();
                    Database.instance.loadTileDrop();
                }
                if ((d.getHours() == maintenanceHour && d.getMinutes() == maintenanceMinute && d.getSeconds() <= 5) || BaoTriTay) {
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
