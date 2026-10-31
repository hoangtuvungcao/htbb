   // 
// Decompiled by Procyon v0.6.0
// 
package real.cmd;

import com.htbb.event.TichLuyOnline;
import java.io.IOException;
import real.Item;
import real.EffectBuff;
import real.RealController;
import real.Map;
import real.Wedding;
import real.MessageCreator;
import data.Database;
import java.util.Date;
import java.util.Vector;
import real.Char;
import real.CharManager;
import server.TeamServer;
import server.InfoClientConnect;
import io.Message;
import io.Session;
import io.SessionManager;
import java.io.DataInputStream;
import java.io.FileInputStream;
import static real.cmd.LoginHandler.createMsgLogin;

public class SelectCharHandler implements ICommandHandler {

    static byte[][] imgSkill;

    public static void loadImgSkill() {
        if (SelectCharHandler.imgSkill == null) {
            SelectCharHandler.imgSkill = new byte[5][];
            for (int i = 0; i < 5; ++i) {
                try {
                    final FileInputStream fis = new FileInputStream("map/sk" + i + ".png");
                    final DataInputStream dis = new DataInputStream(fis);
                    SelectCharHandler.imgSkill[i] = new byte[dis.available()];
                    int byteRead = 0;
                    while (byteRead != -1) {
                        if (byteRead >= SelectCharHandler.imgSkill[i].length) {
                            break;
                        }
                        final int len = dis.read(SelectCharHandler.imgSkill[i], byteRead, SelectCharHandler.imgSkill[i].length - byteRead);
                        if (len > 0) {
                            byteRead += len;
                        }
                        if (len == -1) {
                            break;
                        }
                    }
                    try {
                        fis.close();
                    } catch (final Exception ex) {
                    }
                    try {
                        dis.close();
                    } catch (final Exception ex2) {
                    }
                } catch (final Exception ex3) {
                }
            }
        }
    }

    /**
     * 31/10/2026 (dang nhap o thiet bi khac): phien cu vua nhan ly do va se bi da sau ~0,4 s (Session.dongVoiLyDo) - cho
     * nhan vat cu roi CharManager toi da cho ms roi moi chon tiep cho phien moi. Truoc day phien MOI bi ngat ngay (11/12):
     * client moi dang o man "Dang tai Pokemon" khong tu noi lai -> ket 60 s. true = nhan vat cu da roi.
     */
    static boolean choRoi(final Char cu, final long cho) {
        final long het = System.currentTimeMillis() + cho;
        while (System.currentTimeMillis() < het) {
            if (CharManager.instance.getByUserID(cu.userID) != cu && CharManager.instance.getCharByCharName(cu.charname) != cu) {
                return true;
            }
            try {
                Thread.sleep(100L);
            } catch (final InterruptedException e) {
                return false;
            }
        }
        return CharManager.instance.getByUserID(cu.userID) != cu && CharManager.instance.getCharByCharName(cu.charname) != cu;
    }

    @Override
    public void process(final Session session, final Message message) throws IOException {
        try {
            Char p = null;
            final DataInputStream dis = message.dis;
            final int type = dis.readByte();
            final int charDBID = dis.readInt();
            if (session.userID == -1) {
                session.disconnect(20);
                return;
            }
            // Cho 2s neu cung IP chon nhan vat 2 lan trong 5s - gioi han THEO IP, bo qua proxy
            // tin cay (sv.proxyTinCay: sau WAF ca server chung IP).
            final InfoClientConnect infoSelect = com.htbb.mang.GioiHanIp.apDung(session.ip)
                    ? TeamServer.ALL_IPCONNECT.get(session.ip) : null;
            if (infoSelect != null && !infoSelect.canSelectChar()) {
                try {
                    Thread.sleep(2000L);
                } catch (final Exception ex) {
                }
            }
            Char p2 = CharManager.instance.getByUserID(session.userID);
            if (p2 != null) {
                if (p2.getSession() != null) {
                    p2.getSession().dongVoiLyDo(LoginHandler.DANG_NHAP_KHAC, 10);
                } else {
                    CharManager.instance.kickPlayer(p2, "selectcharhandler 1");
                }
                if (!choRoi(p2, 2500L)) {
                    session.disconnect(11);
                    return;
                }
            }
            p = new Char(session);
            Vector<Item> charItem = null;
            final Vector<Date> lastLog = new Vector<Date>();
            charItem = Database.instance.getChar(p, charDBID, lastLog);
            if (p.userID != session.userID) {
                Database.instance.saveOrtherLog("", session.username, "hack dang nhap char " + p.charname + " > " + session.userID + " > " + p.userID, "hackchonchar");
                data.NghiVanGianLan.ghiChung(session, session.username, "hackchonchar",
                        "chon nhan vat '" + p.charname + "' (thuoc tai khoan " + p.userID
                      + ") trong khi dang dang nhap bang tai khoan " + session.userID);
                session.disconnect(0);
                return;
            }
//            if(type > 0 && type < 4)
//            {
//                session.sendMessage(MessageCreator.createServerAlertMessage("Bảo trì chức năng xoá nhân vật!", ""));
//                return;
//            }

            if (type == 1 && p.using == 0) {
                // 23/10/2026: truoc day bao "Chức năng đang bảo trì" -> nguoi choi tuong may chu hong.
                // Pokemon da xoa (del=0) van khoi phuc duoc: nut "Xóa" o man chon doi thanh "Khôi phục".
                session.sendMessage(MessageCreator.createServerAlertMessage("Không vào được: Pokémon «" + p.charname
                        + "» đã bị xoá. Bấm «Khôi phục» (phím Xóa ở màn chọn Pokémon) để chơi tiếp.", ""));
                return;
            }
            if (type == 2) {
                if (p.using == 1) {
//                    session.sendMessage(MessageCreator.createServerAlertMessage("Chức năng đang bảo trì", ""));
//                    return;
                    Database.instance.updateDelChar(charDBID, 0);
                    session.sendMessage(MessageCreator.createServerAlertMessage("Pokémon đã xoá. Có thể khôi phục trong 7 ngày.", ""));
//                    Database.instance.banChar(p, "xoa nhan vat");
                    session.sendMessage(MessageCreator.createCharListMessage(session.userID, p.getSession()));

                }
                return;
            }
            if (type == 3) {
                if (p.using == 0) {
//                    session.sendMessage(MessageCreator.createServerAlertMessage("Không thể khôi phục nhân vật", ""));
//                    return;

                    session.sendMessage(MessageCreator.createServerAlertMessage("Pokémon đã được khôi phục.", ""));
                    Database.instance.updateDelChar(charDBID, 1);
                    session.sendMessage(MessageCreator.createCharListMessage(session.userID, p.getSession()));

                }
                return;
            }
            p.userID = session.userID;
            p.initInfoFromDB(charItem, lastLog);
            p.timeXP = (short) session.timeXp;


                // Luc thoat game, CharManager dat timeOutGame = now + 600000.
                // Chan vao lai trong RELOGIN_WAIT_MS dau tien. sv.reloginWaitSec=0
                // (mac dinh) -> dieu kien luon sai -> vao lai duoc ngay.
                final long moc = 600000L - TeamServer.RELOGIN_WAIT_MS;
                if (TeamServer.RELOGIN_WAIT_MS > 0L && p.timeOutGame - System.currentTimeMillis() > moc
                        && !p.isAdmin && TeamServer.server != 100 && !TeamServer.isServerLocal()) {
                    long time = (p.timeOutGame - System.currentTimeMillis() - moc) / 1000L;
                    if (time <= 0L) {
                        time = 1L;
                    }
                    final String[] a = {"Bạn chỉ có thể vào lại game sau " + time + " giây nữa", "You only join to game after " + time + " seconds"};
                    if (TeamServer.isServerIndo()) {
                        a[0] = "Anda hanya bergabung ke permainan setelah 5 detik";
                    }
                    session.sendMessage(MessageCreator.createServerAlertMessage(a[0], ""));
                    session.userID = -1;
                    return;
                }
           

            if (com.htbb.pokemon.ThanhVien.chanDangNhap(p.userID) && !Database.check_active(p.userID)) { // kich hoat: mac dinh KHONG chan dang nhap
                session.sendMessage(MessageCreator.createServerAlertMessage(LoginHandler.KichHoatTaiKhoan, ""));
                session.userID = -1;
                return;
            }
            p2 = CharManager.instance.getCharByCharName(p.charname);
            if (p2 != null) {
                if (p2.getSession() != null) {
                    p2.getSession().dongVoiLyDo(LoginHandler.DANG_NHAP_KHAC, 9);
                } else {
                    CharManager.instance.kickPlayer(p2, "selectcharhandler 2");
                }
                if (!choRoi(p2, 2500L)) {
                    session.disconnect(12);
                    return;
                }
            }
            if (p.idWedding > -1) {
                final Wedding wd = Map.allWedding.get(p.idWedding);
                if (wd != null) {
                    wd.listMember.put(p.charDBID, p.userID);
                    if (wd.action == 1) {
                        com.htbb.thongbao.ThongBao.rieng(p, "Lễ kết đôi bạn được mời đang diễn ra. Hãy nhanh chân lên Cao nguyên Indigo để dự lễ.");
                    }
                } else {
                    p.idWedding = -1;
                }
            }
            if (p.getSession().bigProvider.equals("1") && p.getSession().privateProvider.equals("1") && !TeamServer.isServerIndo()) {
                TeamServer.addQueue(String.valueOf(p.getSession().username) + "/login/login/" + p.charname);
            }
            try {
              
                Database.instance.saveLogActiveUser(String.valueOf(p.charname) + "_login", String.valueOf(p.lvDetail.lv) + " " + "_" + p.getxu() + "_" + p.getLuong() + "_" + p.getLuongLock() + "_" + p.luongxainhanRuong);
            } catch (final Exception e) {
                e.printStackTrace();
            }
            p.setTimeOutBuff();
            p.id = RealController.intance.idGen.getID(0, "get char from db");
            // Dat lai chi so goc khi du lieu hong (strength/agitity = 0): moi nhan vat.
            if (p.strength == 0 || p.agitity == 0) {
                final int allBasicPoint = p.lvDetail.getBasicPoint(p.lastLV) + (p.lastLV - 1) * 5;
                p.basepoint = (short) (allBasicPoint - (p.lastLV - 1) * 5);
                p.skillpoint = (short) (p.lastLV - 1);
                p.hp = 100;
                p.mp = 100;
                p.strength = (short) (Database.basicPoint[p.charClass][0] + (p.lastLV - 1));
                p.agitity = (short) (Database.basicPoint[p.charClass][1] + (p.lastLV - 1));
                p.spirit = (short) (Database.basicPoint[p.charClass][2] + (p.lastLV - 1));
                p.health = (short) (Database.basicPoint[p.charClass][3] + (p.lastLV - 1));
                p.luck = (short) (Database.basicPoint[p.charClass][4] + (p.lastLV - 1));
                final int firstSkill = p.skill[0];
                for (int i = 0; i < p.skill.length; ++i) {
                    if (p.skill[i] >= 0) {
                        p.skill[i] = 0;
                    }
                }
                if (firstSkill > 0 || p.lvDetail.lv >= 3) {
                    p.skill[0] = 1;
                }
            }
            if (p.skillpoint < 0) {
                p.skillpoint = 0;
            }
            if (p.basepoint < 0) {
                p.basepoint = 0;
            }
            p.calculateAttrib();
            if (!CharManager.instance.put(p)) {
                final Char pp = CharManager.instance.getByUserID(p.userID);
                if (pp != null) {
                    if (pp.getSession() != null) {
                        pp.getSession().dongVoiLyDo(LoginHandler.DANG_NHAP_KHAC, 13);
                    } else {
                        CharManager.instance.kickPlayer(pp, "selectcharhandler 3");
                    }
                }
                CharManager.instance.remove(p);
                session.disconnect(14);
                return;
            }
            if (RealController.maquee2 != null && RealController.maquee2.length() > 0 && RealController.maquee2 != null && RealController.maquee2.length() > 0) {
                for (int j = 0; j < RealController.dayAlertMarquest2.length; ++j) {
                    if (RealController.dayAlertMarquest2[j].indexOf(Char.getDayOpen(0L)) > -1) {
                        session.sendMessage(MessageCreator.createServerInfoMessage1());
                        break;
                    }
                }
            }
            session.charname = p.charname;
            p.sendMessage(MessageCreator.createSkillInfoMessage(p));
            if (Map.getTown[0] || Map.getTown[1] || Map.getTown[2]) {
                if (p.myCountry > -1) {
                    p.sendMessage(MessageCreator.createMsgStartGetTown(p.myCountry));
                }
                String info = "";
                for (int k = 0; k < 3; ++k) {
                    info = String.valueOf(info) + (Map.getTown[k] ? (String.valueOf(Map.nameCountry[k]) + ",") : "");
                }
                p.sendMessage(MessageCreator.createServerAlertAutoOffMessage("đang diễn ra sự kiện chiếm Gym"));
            }
            p.typeFirmware = session.firmWare;
            p.setSizeScreen(session.w, session.h);
            p.calculatorHPMP();
            final Map toMap = RealController.mapList.get(p.mapID);
            if (toMap != null) {
                toMap.playerJoin(p);
            } else {
                final Map offlineMap = RealController.mapList.get(-1);
                offlineMap.playerJoin(p);
            }
            Message m = MessageCreator.createMapMessage(p);
            session.sendMessage(m);
            real.KhoaBanDoGame.guiDanhSach(p); // goi -108: ban do khoa / khong cho tu dong
            p.map.doSendDynamicObj(p);
            // Skin linh thu duoc suy ra tu vat pham dang mac o bang Trang bi thu (da nap
            // vao p.awItems truoc do), khong co cot DB rieng. Phai tinh TRUOC goi thong
            // tin nhan vat dau tien, khong thi dang nhap vao thay hinh thu that roi phai
            // doi den luc mac lai do moi hien skin.
            real.SkinThuCuoi.dongBoTheoTrangBi(p);
            // Tinh lai chi so SAU khi biet skin, TRUOC goi thong tin nhan vat dau tien.
            // Skin cong chi so cho chu nhan (HP/MP, thu, tiem nang...) ma attack/maxhp/
            // defend deu la truong duoc luu san — lan calculateAttrib truoc do trong qua
            // trinh dang nhap chay khi idSkinLinhThu con la -1, nen khong co phan skin.
            p.calculateAttrib();
            m = MessageCreator.createMainCharInfoMessage(p);
            try {
                session.sendMessage(m);
                m.cleanup();
            } catch (final Exception e2) {
                try {
                    m = MessageCreator.createMainCharInfoMessage(p);
                    session.sendMessage(m);
                    m.cleanup();
                } catch (final Exception ex2) {
                }
            }
            // hoan xu / hop thoai chuyen doi mot lan + goi -122 (client >= 908)
            real.HocChieuGame.sauVaoGame(p);
            try {
                m = MessageCreator.createCharWearingMessage(p, p);
                session.sendMessage(m);
                m.cleanup();
                m = MessageCreator.sendWpaLogin(p);
                if (m != null) {
                    session.sendMessage(m);
                    m.cleanup();
                }
                p.sendMessage(MessageCreator.createAnimalWearingMessage(p, p));
            } catch (final Exception e2) {
                e2.printStackTrace();
                try {
                    m = MessageCreator.createCharWearingMessage(p, p);
                    session.sendMessage(m);
                    m.cleanup();
                } catch (final Exception ex3) {
                }
            }
            p.map.sendInfoNpc(p);
            p.map.sendDynamicEff(p);
            try {
                p.sendMessage(MessageCreator.createCharGemItem(p));
            } catch (final Exception e2) {
                e2.printStackTrace();
            }
            try {
                p.sendMessage(MessageCreator.createCharSpecialItem(p));
            } catch (final Exception e2) {
                e2.printStackTrace();
            }
            Label_1964:
            {
                if (p.x2 == 0) {
                    if (p.myCountry <= -1 || Map.minuteX2Country[p.myCountry] <= 0L) {
                        break Label_1964;
                    }
                }
                try {
                    m = new Message(86);
                    m.dos.writeByte(2);
                    m.dos.writeByte(p.x2);
                    m.dos.writeInt(p.getGoldTime());
                    m.dos.writeUTF(p.getInfoX2());
                    p.sendMessage(m);
                    m.cleanup();
                } catch (final Exception e2) {
                    e2.printStackTrace();
                }
            }
//            Database.instance.countChar(p, session.userID);
            Database.instance.getBoardNapTien(p);
            com.htbb.pokemon.ThanhVien.baoKhiVaoGame(p); // kich hoat: nhac tinh nang bi khoa khi chua kich hoat
            Database.instance.getBoardCreated(p);
            Database.instance.sendNotiLogin(p, m);
            p.doAddGiftEventOffline();
//            p.doAddQuaTichLuy(1);
            p.doAddQuaTichLuy(2);
            p.doAddQuaTichLuy(3);
            //p.setMocNap();
            // MA CHET TRUOC DAY: getLuong()/getLuongLock() chan gia tri ve 0 truoc khi tra
            // ve, nen "< 0" khong bao gio dung — nguoi choi am tien van vao game binh
            // thuong. Gio hai ham do tu bao va ban dung mot lan (xem Char.java), o day chi
            // can hoi lai co bat duoc gi khong roi ngat ket noi.
            p.getLuong();
            p.getLuongLock();
            p.getxu();
            if (p.daPhatHienAmTien()) {
                session.disconnect(11);
            }
            // nhan vat da co huy hieu Gym ma chua nhan qua huy hieu dau tien (1 Ve dich chuyen): trao bu, truoc goi tui
            real.VeLangMienPhiGame.khiVaoGame(p);
            p.sendMessage(MessageCreator.createCharInventoryMessage(p, 0));
            p.sendMessage(MessageCreator.createCharInventoryMessage(p, 1));
            p.sendMessage(MessageCreator.createCharInventoryMessage(p, 2));
            p.sendMessage(MessageCreator.createCharInventoryMessage(p, 3));
            // Client >= 903: trang thai -105 ngay sau khi nap nhan vat (suc chua hanh trang, ve mo rong, gia xu...).
            real.MoRongTuiGame.guiTrangThai(p);
            p.sendInfoQuest();
            try {
                p.sendMessage(MessageCreator.createMsgFriendList(p));
            } catch (final Exception e2) {
                System.out.println("LOI GUI DANH SACH BAN");
            }
            p.checkCreateCharHire();
            if (RealController.maquee.length() > 0) {
                m = MessageCreator.createServerInfoMessage();
                session.sendMessage(m);
            }
            final Message msg = new Message(-8);
            msg.dos.writeByte(1);
            msg.dos.writeByte(0);
            msg.dos.writeShort(SelectCharHandler.imgSkill[p.charClass].length);
            msg.dos.write(SelectCharHandler.imgSkill[p.charClass]);
            p.sendMessage(msg);
            if (session.bigProvider.equals("1")) {
                if (session.privateProvider.equals("1")) {
                    // SECURITY: disabled external Net.getAgenID call to third-party server.
                    if (p.agenID == -1) {
                        p.agenID = 3;
                    }
                    session.sendMessage(MessageCreator.createSMS(p, "1", new StringBuilder(String.valueOf(p.agenID)).toString(), true));
                } else {
                    session.privateProvider.equals("0");
                }
            } else if (session.bigProvider.equals("0")) {
                String[] provider_agent = Database.instance.getProviderFromTeamUser(session.userID, session.clientProvider, session.agentProvider);
                session.clientProvider = provider_agent[0];
                if (provider_agent[1].equals("-1")) {
                    Database.instance.updateAgentTeamUser(session.userID, session.agentProvider);
                } else {
                    session.agentProvider = provider_agent[1];
                }
                if (session.idServer == 1) {
                    provider_agent = Database.instance.getProviderFromTeamUser1(session.userID, session.clientProvider, session.agentProvider);
                    session.clientProvider = provider_agent[0];
                    if (provider_agent[1].equals("-1")) {
                        Database.instance.updateAgentTeamUser1(session.userID, session.agentProvider);
                    } else {
                        session.agentProvider = provider_agent[1];
                    }
                }
                session.sendMessage(MessageCreator.createSMS(p, session.clientProvider, session.agentProvider, false));
            }
            try {
                // p.sendMessage(MessageCreator.createServerAlertAutoOffMessage("Chào mừng " + p.charname + " đã đến với OpenBeta của Ngũ Đế Online Private, chúc bạn một ngày chơi game vui vẻ nhé. Khi train quái, bạn sẽ nhận được thêm lượng khoá khi còn dưới 500 lượng khoá."));
                if (p.isCharNu() && Char.getDayOpen(0L).equals("2017-03-08")) {
                    com.htbb.thongbao.ThongBao.rieng(p, "Chúc " + p.charname + " 8/3 thật vui vẻ và nhiều niềm vui");
                }
                session.sendMessage(MessageCreator.createConfig(p));
                session.sendMessage(MessageCreator.createInboxMessage(p));
                if (Map.weather != null) {
                    session.sendMessage(MessageCreator.createMsgWeather(Map.weather.id, Map.weather.n, Map.weather.time));
                }
                if (Map.goldTime) {
                    session.sendMessage(MessageCreator.createServerAlertAutoOffMessage("Giờ vàng đã bắt đầu."));
                }
                if (p.myCountry > -1 && Map.eventKillMonster[p.myCountry]) {
                    session.sendMessage(MessageCreator.createServerAlertAutoOffMessage("Đang diễn ra sự kiện săn Mãnh thú."));
                }
                if (p.myCountry > -1 && Map.nationBeAttack[p.myCountry] > -1) {
                    session.sendMessage(MessageCreator.createServerAlertAutoOffMessage(String.valueOf(Map.nameCountry[Map.nationBeAttack[p.myCountry]]) + " sẽ tấn công vùng của bạn vào lúc 20h ngày " + Map.nationWar.get(Map.nationBeAttack[p.myCountry]).dayAttack));
                }
                final String infoBoss = Map.getNameBossApear(p.inCountry);
                if (!infoBoss.equals("")) {
                    session.sendMessage(MessageCreator.createServerAlertAutoOffMessage("Boss đang xuất hiện: " + infoBoss + "."));
                }
                p.sendMessage(MessageCreator.createSkillClan(p));
                p.sendMessage(MessageCreator.createCharInventoryMessage(p, 0));
                p.sendMessage(MessageCreator.createCharInventoryMessage(p, 1));
                p.sendMessage(MessageCreator.createCharInventoryMessage(p, 2));
                p.sendMessage(MessageCreator.createCharInventoryMessage(p, 3));
                if ((Map.checkRcvXP || (Char.isAcountTest(p) && Char.onOffTime180 == 1)) && Map.openLog && p.timeXP >= 180) {
                    p.sendMessage(MessageCreator.createServerAlertMessage("Bạn đã chơi được " + p.timeXP + " phút. Bạn không thể chơi tiếp trong ngày hôm nay.", ""));
                    Thread.sleep(5000L);
                    session.disconnect(1001);
                }
                if (p.petUsing != null && !p.petUsing.isPetTool() && p.petUsing.getIdEffPet() > -1 && p.addEffBuff(p.petUsing.getIdEffPet(), System.currentTimeMillis() + 320000000L, EffectBuff.BY_ACTOR, 0) != null) {
                    p.sendEffToChar(p);
                }
                message.cleanup();
                // TODO fixx hảo kì luân
                // Gui lai goi 1 sau khi chon nhan vat, nhung KHONG kem hai anh tile: client cache rong
                // (vsTile=-1) da nhan va luu tile o goi 1 luc dang nhap (LoginHandler). Truoc day gui lai
                // ~24 KB tile lan nua ngay tren duong vao ban do cua dung nguoi dang co mang cham nhat.
                session.sendMessage(createMsgLogin(0));
            } catch (final Exception ex4) {
            }

            TichLuyOnline.Instance.onlineGame(p);

            // Vao game trong 1 gio sau khi bang chiem thanh -> nhan thuong chiem thanh
            try {
                real.ThuongChiemGym.kiemTraKhiVaoGame(p);
                real.ChiemGymLauDai.kiemTraKhiVaoGame(p); // GD2 (tat co: khong lam gi)
            } catch (final Exception exThuong) {
                exThuong.printStackTrace();
            }
        } catch (final Exception e3) {
            e3.printStackTrace();
        }
    }
}
