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

    // Bao truoc bao tri cho nguoi dang online. Chi thong bao, KHONG tat server - viec tat
    // de cho systemctl stop (SIGTERM -> shutdown hook luu du lieu), xem vps/deploy-auto.sh.
    //
    // DUNG HOP THOAI (tin 37) chu TUYET DOI KHONG dung marquee (tin 38).
    // Marquee bi client nhet vao hang doi TINH GameScr.serverInfo, va paintServerInfo chi
    // chay khi nguoi choi dang o man hinh game: ai dang mo menu / o shop / dang loading thi
    // ticker khong troi, tin nam im trong hang doi. Hang doi lai khong bi xoa khi mat ket
    // noi, nen sau khi bao tri xong ho dang nhap lai, vua vao GameScr la hang doi moi bat
    // dau chay -> "thong bao bao tri" hien ra dung luc bao tri DA XONG, gay hieu lam nang.
    // Tin 37 thi client goi Canvas.startOKDlg ngay lap tuc (MessageHandler.cs case 37),
    // khong qua hang doi nao, khong the ton lai sau khi dang nhap lai.
    //
    // Cung khong dat RealController.maquee: LoginHandler/SelectCharHandler gui lai maquee
    // cho nguoi moi vao neu no khac rong, lai thanh mot duong ton thong bao nua.
    public static void baoTruocBaoTri(final int giay) {
        try {
            final String khiNao = (giay >= 60 && giay % 60 == 0) ? (giay / 60) + " phút" : giay + " giây";
            final String noiDung = "Hệ thống bảo trì sau " + khiNao
                    + ", vui lòng thoát game để tránh mất mát dữ liệu.";
            final Message m = MessageCreator.createServerAlertMessage(noiDung, "");
            for (int i = 0; i < CharManager.instance.vChars.size(); ++i) {
                CharManager.instance.vChars.elementAt(i).sendMessage(m);
            }
            m.cleanup();
            System.out.println("BAO TRI SAU " + giay + "s (" + CharManager.instance.vChars.size() + " nguoi online)");
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
