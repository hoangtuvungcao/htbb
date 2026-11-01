import java.io.DataInputStream;
import java.io.DataOutputStream;

import javax.microedition.io.Connector;
import javax.microedition.io.SocketConnection;
import javax.microedition.midlet.MIDlet;

/**
 * Lop MIDP cua client PC / Android (javax.microedition.io.Connector) di qua Peerova khi ban build co ticket:
 * dung jar HTBB-PC.jar THAT (thu vien native nap tu /peerova-native trong jar), mo "socket://<host gia>:<cong game>"
 * nhu Session_ME, gui goi -40 (xin khoa) nhu client that va doc tra loi cua may chu game. Them: ticket hong ->
 * IOException + Session_ME.lyDoLoiKetNoi co cau bao ro rang (khong lui ve TCP thang).
 *
 *   java -cp HTBB-PC.jar:<lop> KiemConnectorP2P <ticket|@tep> <cong game>
 */
public final class KiemConnectorP2P {
    public static void main(String[] a) throws Exception {
        String ticket = a[0].startsWith("@") ? new String(java.nio.file.Files.readAllBytes(java.nio.file.Paths.get(a[0].substring(1))), "UTF-8").trim() : a[0];
        int cong = Integer.parseInt(a[1]);
        int loi = 0;
        MIDlet.GHI_DE.put("HTBB-P2P-Ticket", ticket);
        System.out.println("bat P2P: " + htbb.p2p.KetNoiP2P.bat());
        for (int lan = 1; lan <= 2; lan++) {
            long t0 = System.currentTimeMillis();
            // 192.0.2.1 (TEST-NET, khong toi duoc): P2P bo qua host, ticket xac dinh may chu
            SocketConnection sc = (SocketConnection) Connector.open("socket://192.0.2.1:" + cong);
            DataOutputStream o = sc.openDataOutputStream();
            DataInputStream in = sc.openDataInputStream();
            o.write(new byte[] {(byte) -40, 0, 0});
            o.flush();
            int cmd = in.readByte();
            int n = ((in.readByte() & 255) << 8) | (in.readByte() & 255);
            byte[] d = new byte[n];
            in.readFully(d);
            boolean ok = cmd == -40 && n > 0;
            System.out.println((ok ? "  OK   " : "  HONG ") + "lan " + lan + ": Connector qua P2P (" + sc.getAddress() + ":" + sc.getPort()
                    + ") -> may chu tra goi " + cmd + " (" + n + " byte khoa) sau " + (System.currentTimeMillis() - t0) + " ms");
            if (!ok) loi++;
            sc.close();
        }
        System.out.println("  thu vien native: " + System.getProperty("peerova.jni.path", "tu jar (/peerova-native)"));
        htbb.p2p.KetNoiP2P.dong();
        // ticket cua may chu khong ton tai -> loi ro rang
        MIDlet.GHI_DE.put("HTBB-P2P-Ticket", "pvt_khong_hop_le");
        try {
            Connector.open("socket://192.0.2.1:" + cong);
            System.out.println("  HONG ticket hong ma van mo duoc");
            loi++;
        } catch (java.io.IOException e) {
            String ly = network.Session_ME.lyDoLoiKetNoi;
            boolean ok = ly != null && ly.contains("P2P");
            System.out.println((ok ? "  OK   " : "  HONG ") + "ticket hong -> IOException, hop thoai se hien: \"" + ly + "\"");
            if (!ok) loi++;
        }
        System.out.println(loi == 0 ? "DAT KiemConnectorP2P" : "HONG KiemConnectorP2P");
        System.exit(loi == 0 ? 0 : 1);
    }
}
