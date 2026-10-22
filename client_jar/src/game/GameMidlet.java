package game;

import game.model.IAction;
import game.model.Tilemap;
import game.networklogic.GameService;
import game.networklogic.MessageHandler;
import game.render.Canvas;
import game.render.Res;
import game.render.screen.RMS;
import game.render.screen.ServerListScr;
import java.io.IOException;
import java.io.InputStream;
import javax.microedition.io.Connector;
import javax.microedition.io.HttpConnection;
import javax.microedition.lcdui.Display;
import javax.microedition.midlet.MIDlet;
import javax.microedition.midlet.MIDletStateChangeException;
import javax.wireless.messaging.MessageConnection;
import javax.wireless.messaging.TextMessage;
import network.Session_ME;

public class GameMidlet extends MIDlet {
    public static Canvas gameCanvas;
    public static GameMidlet instance;
    public static String IP = "210.245.115.227";
    public static int PORT = 19129;
    public static String version = "2.7.6";
    public static String client_Provider = "0";
    public static String provider = "-1";
    public static String agent = "-1";
    public static String url = "";
    public static boolean isLoadAgentPro = false;
    public static int coutVerSion = 72;

    public GameMidlet() {
        instance = this;
        try {
            start();
        } catch (Throwable e) {
            game.pokemon.KhoiDong.loi("start", e);   // 0.9.7.2
        }
        Res.load.isInitGame = true;
    }

    public void start() {
        gameCanvas = new Canvas();
        Display.getDisplay(this).setCurrent(gameCanvas);
        gameCanvas.start();
    }

    public void load() {
        // 0.9.7.2: mỗi bước nạp trong try riêng + tên bước cho chẩn đoán (game.pokemon.KhoiDong); bước hỏng thì đi
        // tiếp - màn tải khởi động không bao giờ kẹt vì một ảnh / dữ liệu lỗi
        String[] ten = {"init", "sizeChanged", "loadTreeImage", "loadMonster", "loadPart2", "mang", "resetImgMonsTemp", "loadMap0", "ServerList"};
        for (int i = 0; i < ten.length; i++) {
            game.pokemon.KhoiDong.buoc(ten[i]);
            try {
                switch (i) {
                    case 0:
                        gameCanvas.init();
                        break;
                    case 1:
                        gameCanvas.sizeChanged(0, 0);
                        break;
                    case 2:
                        Res.loadTreeImage();
                        break;
                    case 3:
                        Res.loadMonster();
                        break;
                    case 4:
                        Res.loadPart2();
                        break;
                    case 5:
                        Session_ME.gI().setHandler(MessageHandler.gI());
                        GameService.gI().setSession(Session_ME.gI());
                        break;
                    case 6:
                        Res.resetImgMonsTemp();
                        break;
                    case 7:
                        Tilemap.loadMap(0, null);
                        break;
                    default:
                        ServerListScr.gI().switchToMe();
                        break;
                }
            } catch (Throwable e) {
                game.pokemon.KhoiDong.loi(ten[i], e);
                if (e instanceof OutOfMemoryError) {
                    game.pokemon.BoNho.giai("khoi dong " + ten[i]);
                }
            }
        }
        Canvas.isLoad = false;
        game.pokemon.KhoiDong.buoc("xong");
        game.pokemon.KhoiDong.xong = true;
        try {
            napAgent();
        } catch (Throwable e) {
            game.pokemon.KhoiDong.loi("agent", e);
        }
    }

    private void napAgent() {
        if (isLoadAgentPro) {
            agent = RMS.loadRMSString("agent");
            if (agent == null) {
                agent = "-1";
            }
            provider = RMS.loadRMSString("provider");
            if (provider == null) {
                provider = "-1";
            }
        } else {
            InputStream is1 = getClass().getResourceAsStream("/agent.txt");
            if (is1 != null) {
                StringBuffer sb1 = new StringBuffer();
                while (true) {
                    try {
                        int chars = is1.read();
                        if (chars == -1) {
                            break;
                        } else {
                            sb1.append((char) chars);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                String str = sb1.toString();
                agent = str.trim();
            }
            InputStream is10 = getClass().getResourceAsStream("/provider.txt");
            if (is10 != null) {
                StringBuffer sb10 = new StringBuffer();
                while (true) {
                    try {
                        int chars2 = is10.read();
                        if (chars2 == -1) {
                            break;
                        } else {
                            sb10.append((char) chars2);
                        }
                    } catch (Exception e2) {
                        e2.printStackTrace();
                    }
                }
                String str2 = sb10.toString();
                provider = str2.trim();
            }
        }
        System.out.println(new StringBuffer("agent: ================").append(agent).toString());
        System.out.println(new StringBuffer("provider: =============").append(provider).toString());
        System.out.println(new StringBuffer("client pro : =======").append(client_Provider).toString());
        if (!isLoadAgentPro) {
            System.out.println(new StringBuffer("ban thuong ").append(version).toString());
        } else {
            System.out.println(new StringBuffer("ban trang ").append(version).toString());
        }
    }

    public static String connectHTTP(String link) {
        try {
            HttpConnection connection = (HttpConnection) Connector.open(link);
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Content-Type", "//text plain");
            connection.setRequestProperty("Connection", "close");
            if (connection.getResponseCode() == 200) {
                String str = "";
                InputStream inputstream = connection.openInputStream();
                int length = (int) connection.getLength();
                if (length != -1) {
                    byte[] incomingData = new byte[length];
                    inputstream.read(incomingData);
                    str = new String(incomingData, "UTF-8");
                }
                return str;
            }
            return null;
        } catch (IOException e) {
            return null;
        }
    }

    protected void destroyApp(boolean arg0) throws MIDletStateChangeException {
    }

    protected void pauseApp() {
    }

    protected void startApp() throws MIDletStateChangeException {
    }

    public static void requestLink(String link) {
        try {
            instance.platformRequest(link);
            destroy();
        } catch (Exception e) {
        }
    }

    public static void destroy() {
        instance.notifyDestroyed();
    }

    public static void sendSMS(final String data, final String to, final IAction successAction, final IAction failAction) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    MessageConnection smsconn = (MessageConnection) Connector.open(to);
                    TextMessage txtmessage = (TextMessage) smsconn.newMessage("text");
                    txtmessage.setAddress(to);
                    txtmessage.setPayloadText(data);
                    smsconn.send(txtmessage);
                    successAction.perform();
                } catch (Exception e) {
                    failAction.perform();
                }
            }
        }).start();
    }
}
