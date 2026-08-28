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
