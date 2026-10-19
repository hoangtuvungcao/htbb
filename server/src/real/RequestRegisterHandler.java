/*
 * PHUC HOI TU LOI CU (P3): lop nay duoc dich nguoc tu lib/htbb-core.jar - ma loi da bien dich cua
 * game goc - bang Vineflower 1.11.1 roi sua tay toi thieu cho bien dich duoc (--release 8). Giu
 * nguyen ten lop, package va hanh vi; doi ten / don dep de o giai doan sau. Nguon goc: docs/giay-phep.md.
 */
package real;

import data.Database;
import data.Text;
import io.Message;
import io.Session;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.Random;
import real.cmd.ICommandHandler;
import real.cmd.LoginHandler;
import server.TeamServer;

public class RequestRegisterHandler implements ICommandHandler {
    public static boolean checkInfoLogin(String userName) {
        String nick = userName.trim().toLowerCase();
        if (nick.equals("")) {
            return false;
        } else {
            for (int i = 0; i < nick.length(); i++) {
                char ch = nick.charAt(i);
                if ((ch < '0' || ch > '9') && (ch < 'a' || ch > 'z') && ch != '@' && ch != '_' && ch != '.' && (ch < 'A' || ch > 'Z')) {
                    return false;
                }
            }

            return true;
        }
    }

    @Override
    public void process(Session session, Message message) throws IOException {
        if (!Map.onSMS) {
            session.sendMessage(MessageCreator.createServerAlertMessage("Chức năng đang bảo trì. Xin các bạn quay lại sau. Xin cảm ơn", ""));
        } else {
            DataInputStream dis = message.dis;
            String uname = dis.readUTF();
            String uPass = dis.readUTF();
            if (uname.length() < 5 || uname.length() > 20 || !checkInfoLogin(uname)) {
                Message m = MessageCreator.createServerAlertMessage(Text.REGIST_ERR_ACOUNT, "");
                session.sendMessage(m);
            } else if (uPass.length() < 4) {
                Message m = MessageCreator.createServerAlertMessage(Text.REGIST_ERR_PASS, "");
                session.sendMessage(m);
            } else {
                String bigProvider = "0";
                String provider = "0";
                String agent = "0";
                String IMEI = "";
                int versionCode = 1;

                try {
                    bigProvider = dis.readUTF();
                    provider = dis.readUTF();
                    agent = dis.readUTF();
                    IMEI = dis.readUTF();
                    versionCode = dis.readInt();
                    session.IMEI = IMEI;
                    session.versionCode = versionCode;
                    session.firmWare = dis.readByte();
                } catch (Exception var17) {
                }

                session.usernameReg = uname;
                boolean available = false;

                try {
                    if (TeamServer.isServerIndo()) {
                        available = Database.instance.addNewAcountAuto(uname, uPass) != -1;
                        if (!available) {
                            session.sendMessage(MessageCreator.createServerAlertMessage(Text.REGIST_ERR_PHONE_NUMBER, ""));
                            return;
                        }

                        session.sendMessage(
                            MessageCreator.createServerAlertMessage(
                                Text.REGIST_CHUC_MUNG + " " + uname + " " + Text.REGIST_VOIMATKHAU + " " + uPass + ". " + Text.REGIST_BAO_MAT, ""
                            )
                        );
                        return;
                    }

                    if (bigProvider.equals("0")) {
                        if (agent.trim().length() > 4) {
                            agent = "0";
                            provider = "0";
                        }

                        available = Database.instance.checkUsername(uname);
                        if (session.firmWare == 2 && session.versionCode > LoginHandler.versionCode) {
                            String sResult = "2";
                            Random r = new Random(System.currentTimeMillis());
                            String IMEIRandom = "AppStore3" + String.valueOf(r.nextInt(10000));

                            try {
                                String link = "http://my.teamobi.com/register/kpah/?username=" + uname + "&email=&pass=" + uPass + "&pro=" + provider;
                                session.sendMessage(MessageCreator.createServerAlertMessage("Bạn muốn đăng ký tài khoản " + uname + "?", link));
                                return;
                            } catch (Exception var19) {
                                if (sResult.equals("0")) {
                                    session.sendMessage(MessageCreator.createServerAlertMessage("Chúc mừng bạn đã đăng ký thành công", ""));
                                    return;
                                }

                                if (sResult.equals("1")) {
                                    session.sendMessage(MessageCreator.createServerAlertMessage("Có lỗi xảy ra. Mã lỗi 456", ""));
                                    return;
                                }

                                session.sendMessage(MessageCreator.createServerAlertMessage("1 điện thoại chỉ có thể đăng ký được 1 tài khoản.", ""));
                                return;
                            }
                        }

                        if (session.firmWare == 2) {
                            String sResult = "2";

                            try {
                                String link = "http://my.teamobi.com/register/kpah/?username=" + uname + "&email=&pass=" + uPass + "&pro=" + provider;
                                session.sendMessage(MessageCreator.createServerAlertMessage("Bạn muốn đăng ký tài khoản " + uname + "?", link));
                                return;
                            } catch (Exception var20) {
                                if (sResult.equals("0")) {
                                    session.sendMessage(MessageCreator.createServerAlertMessage("Chúc mừng bạn đã đăng ký thành công", ""));
                                    return;
                                }
                            }

                            if (sResult.equals("1")) {
                                session.sendMessage(MessageCreator.createServerAlertMessage("Có lỗi xảy ra. Mã lỗi 456", ""));
                                return;
                            }
                        }
                    } else {
                        available = Database.instance.checkUsernameVTC(uname);
                    }
                } catch (Exception var21) {
                    var21.printStackTrace();
                }

                if (uname.toLowerCase().equals("lamkckc")) {
                    available = false;
                }

                Message m = new Message(39);
                m.dos.writeUTF(uname.trim() + " " + uPass.trim());
                m.dos.writeBoolean(available);
                if (!available) {
                    session.sendMessage(MessageCreator.createServerAlertMessage("Xin chọn tên tài khoản khác", ""));
                } else {
                    if (bigProvider.equals("0")) {
                        session.unameReg = uname.trim() + " " + uPass.trim();
                        session.available = available;
                        session.username = uname.trim().toLowerCase();

                        for (int i = 0; i < Database.smsNapMe.decript.size(); i++) {
                            String dec = Database.smsNapMe.decript.get(i);
                            if (dec.toLowerCase().equals("reg" + provider)) {
                                String syntax = Database.smsNapMe.syntax.get(i);
                                if (syntax.indexOf("REF") > -1) {
                                    String data = syntax.substring(0, syntax.indexOf("REF") + 3);
                                    syntax = syntax.replace(data, data + agent);
                                } else if (!agent.equals("0")) {
                                    String data = syntax.substring(0, syntax.indexOf(" "));
                                    syntax = syntax.replaceAll(data, data + agent);
                                }

                                m.dos.writeUTF(syntax);
                                m.dos.writeUTF(Database.smsNapMe.center.get(i));
                                session.syntax = syntax;
                                session.centerReg = Database.smsNapMe.center.get(i);
                                if (session.firmWare != 0 && Char.onOffThamdinh != 0) {
                                    session.sendMessage(MessageCreator.createMsgInputText(0, 0, "Họ và tên", 0));
                                } else {
                                    session.sendMessage(
                                        MessageCreator.createMsgPopUp(
                                            0, 0, "Để tạo tài khoản. 1 tin nhắn sẽ được gửi đến đầu số " + Database.smsNapMe.center.get(i) + " trị giá 500vnđ"
                                        )
                                    );
                                }

                                return;
                            }
                        }
                    } else if (bigProvider.equals("1")) {
                        try {
                            String stSMS = "";
                            String port = "6036";
                            if (Database.agent.contains(agent)) {
                                for (int ix = 0; ix < Database.smsNapVTC.decript.size(); ix++) {
                                    if (Database.smsNapVTC.agent.get(ix).equals(agent) && Database.smsNapVTC.decript.get(ix).toLowerCase().equals("reg")) {
                                        stSMS = Database.smsNapVTC.syntax.get(ix);
                                        port = Database.smsNapVTC.center.get(ix);
                                        break;
                                    }
                                }
                            } else {
                                for (int ixx = 0; ixx < Database.smsNapVTC.decript.size(); ixx++) {
                                    if (Database.smsNapVTC.agent.get(ixx).equals("3") && Database.smsNapVTC.decript.get(ixx).toLowerCase().equals("reg")) {
                                        stSMS = Database.smsNapVTC.syntax.get(ixx);
                                        port = Database.smsNapVTC.center.get(ixx);
                                        break;
                                    }
                                }
                            }

                            if (stSMS.equals("")) {
                                stSMS = "NQ TDK";
                            }

                            session.unameReg = uname.trim() + " " + uPass.trim();
                            session.available = available;
                            m.dos.writeUTF(stSMS + agent + " ");
                            m.dos.writeUTF(port);
                            session.syntax = stSMS + agent + " ";
                            session.centerReg = port;
                            session.sendMessage(MessageCreator.createMsgPopUp(0, 0, "Để tạo tài khoản. 1 tin nhắn sẽ được gửi đến đầu số " + port + " trị giá 500vnđ"));
                            return;
                        } catch (Exception var18) {
                            var18.printStackTrace();
                        }
                    }

                    session.sendMessage(m);
                    m.cleanup();
                }
            }
        }
    }

    public static void doProcessInput(Session s, Message msg) {
        try {
            int idActor = msg.dis.readShort();
            int idMenu = msg.dis.readByte();
            String text = msg.dis.readUTF().trim().toLowerCase();
            if (idActor != 0 && idActor != 1 && idActor != 2 && idActor != 3 && idActor != 4 && idActor == 5) {
                s.sendMessage(MessageCreator.createMsgPopUp(0, 0, "Để tạo tài khoản. 1 tin nhắn sẽ được gửi đến đầu số " + s.centerReg + " trị giá 5000vnđ"));
            }
        } catch (Exception var5) {
        }
    }
}
