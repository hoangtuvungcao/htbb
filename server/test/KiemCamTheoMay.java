import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.Reader;

/**
 * Kiem thu viec cam dang nhap theo MA MAY (data/CamTheoMay.java).
 *
 * PHAN QUAN TRONG NHAT la lamSach(): ma may la chuoi do CLIENT gui len, tuc du lieu cua
 * ke tan cong neu ho muon. No di thang vao database va vao bo nho dem, nen phai chan
 * ngay tai cua: dai vo han, dau nhay, ky tu dieu khien.
 *
 * Phan thu hai la doi chieu voi LoginHandler: bo loc chay dung ma khong duoc goi, hoac
 * goi sai cho, thi coi nhu khong co. Cu the:
 *   - doc ma may PHAI nam trong khoi try san co va la truong CUOI CUNG, neu khong
 *     client cu (khong gui truong nay) se hong dang nhap.
 *   - biCam() PHAI duoc kiem TRUOC khi gan session.userID.
 */
public final class KiemCamTheoMay {

    private static final String F_LOGIN = "src/real/cmd/LoginHandler.java";
    private static final String F_CAM = "src/data/CamTheoMay.java";
    private static final String F_CLIENT = "../client_pc_ios/ClientUnity/Assets/Scripts/Game/GameService.cs";

    public static void chay() {
        kiemLamSach();
        kiemKhongCanDatabase();
        kiemNoiVaoLoginHandler();
        kiemHaiDauKhopNhau();
        kiemKhongGhepChuoiVaoSql();
    }

    // -----------------------------------------------------------------------
    //  lamSach: du lieu do client gui len
    // -----------------------------------------------------------------------
    private static void kiemLamSach() {
        Kiem.nhom("Cam theo may - lam sach ma may client gui len");

        Kiem.bang("null -> rong", "", data.CamTheoMay.lamSach(null));
        Kiem.bang("chuoi rong -> rong", "", data.CamTheoMay.lamSach(""));
        Kiem.bang("chi khoang trang -> rong", "", data.CamTheoMay.lamSach("   "));

        // Dang that ma Unity sinh ra (SystemInfo.deviceUniqueIdentifier).
        final String that = "8f14e45fceea167a5a36dedd4bea2543";
        Kiem.bang("ma that giu nguyen", that, data.CamTheoMay.lamSach(that));
        Kiem.bang("cat khoang trang hai dau", that, data.CamTheoMay.lamSach("  " + that + "  "));
        Kiem.bang("cho phep gach ngang va gach duoi",
                "a-b_c", data.CamTheoMay.lamSach("a-b_c"));

        // Dai qua muc: khong duoc de chuoi vo han vao database / bo nho dem.
        final StringBuilder dai = new StringBuilder();
        for (int i = 0; i < 65; i++) {
            dai.append('a');
        }
        Kiem.bang("65 ky tu -> tu choi", "", data.CamTheoMay.lamSach(dai.toString()));
        Kiem.bang("64 ky tu -> van nhan", 64, data.CamTheoMay.lamSach(dai.substring(0, 64)).length());

        // Ky tu la: tung cai deu phai bi tu choi.
        final String[] xau = {
            "abc'def",              // dau nhay don - kinh dien cua SQL injection
            "abc\"def",             // dau nhay kep
            "abc def",              // khoang trang giua
            "abc;DROP TABLE x",     // dau cham phay
            "abc\ndef",             // xuong dong
            "abc\tdef",             // tab
            "abc\\def",             // gach cheo nguoc
            "abc%def",              // ky tu dai dien cua LIKE
            "../../etc/passwd",     // dau cham va gach cheo
            "abc<def>",             // dau ngoac nhon
        };
        for (final String s : xau) {
            Kiem.bang("tu choi ma la: " + s.replace("\n", "\\n").replace("\t", "\\t"),
                    "", data.CamTheoMay.lamSach(s));
        }
    }

    // -----------------------------------------------------------------------
    //  Duong khong cham database: phai chay duoc ca khi khong co database
    // -----------------------------------------------------------------------
    private static void kiemKhongCanDatabase() {
        Kiem.nhom("Cam theo may - client cu khong gui ma may");

        // Day la truong hop THUONG GAP NHAT sau khi doi server ma chua doi client.
        // Phai tra ve false MA KHONG cham vao database (neu cham, mot tran do mat khau
        // se thanh mot tran doi database).
        Kiem.sai("ma rong -> khong bi cam", data.CamTheoMay.biCam(""));
        Kiem.sai("null -> khong bi cam", data.CamTheoMay.biCam(null));
        Kiem.sai("ma sai dinh dang -> khong bi cam", data.CamTheoMay.biCam("co' dau nhay"));

        Kiem.bang("ten bang cam", "tob_cam_may", data.CamTheoMay.BANG_CAM);
        Kiem.bang("ten bang lich su", "tob_may_dang_nhap", data.CamTheoMay.BANG_LICH_SU);
    }

    // -----------------------------------------------------------------------
    //  Noi dung vao LoginHandler
    // -----------------------------------------------------------------------
    private static void kiemNoiVaoLoginHandler() {
        Kiem.nhom("Cam theo may - noi vao duong dang nhap");

        final String ma = boChuThich(doc(F_LOGIN));
        Kiem.dung("doc duoc " + F_LOGIN, ma.length() > 0);

        Kiem.dung("co doc ma may tu goi tin",
                ma.contains("session.IMEI = data.CamTheoMay.lamSach(dis.readUTF())"));
        Kiem.dung("co kiem may bi cam", ma.contains("data.CamTheoMay.biCam(session.IMEI)"));
        Kiem.dung("co ghi nhan may dang nhap",
                ma.contains("data.CamTheoMay.ghiNhanDangNhap(session.IMEI"));

        // Doc ma may phai la truong CUOI CUNG cua goi dang nhap. Neu co readUTF() nao
        // dat SAU no thi client cu (khong gui ma may) se lech het cac truong phia sau.
        final int viTriDocMa = ma.indexOf("session.IMEI = data.CamTheoMay.lamSach(dis.readUTF())");
        // Lay lenh doc CUOI CUNG trong file roi xem no co phai lenh doc ma may khong.
        // (KHONG so viTriDocMa == lastIndexOf("dis.readUTF()"): chuoi "dis.readUTF()" nam
        //  BEN TRONG chinh cau lenh do nen hai vi tri khong bao gio bang nhau.)
        final int viTriDocCuoi = ma.lastIndexOf("dis.readUTF()");
        final String truocDo = (viTriDocCuoi < 0) ? ""
                : ma.substring(Math.max(0, viTriDocCuoi - 60), viTriDocCuoi);
        Kiem.dung("doc ma may la lenh doc CUOI CUNG cua goi",
                viTriDocMa >= 0 && truocDo.contains("session.IMEI"));

        // Va phai nam trong mot khoi try (client cu nem EOF phai bi nuot).
        final int viTriTry = ma.lastIndexOf("try {", viTriDocMa);
        final int viTriCatch = ma.indexOf("catch", viTriDocMa);
        Kiem.dung("nam trong khoi try/catch (client cu nem EOF phai bi nuot)",
                viTriTry >= 0 && viTriCatch > viTriDocMa);

        // Kiem cam PHAI dung truoc khi coi la dang nhap thanh cong.
        final int viTriCam = ma.indexOf("data.CamTheoMay.biCam(session.IMEI)");
        final int viTriGanUserId = ma.indexOf("session.userID = userID;");
        Kiem.dung("kiem cam TRUOC khi gan session.userID",
                viTriCam >= 0 && viTriGanUserId >= 0 && viTriCam < viTriGanUserId);

        // Ghi nhan thi phai SAU (chi ghi khi that su vao duoc).
        final int viTriGhi = ma.indexOf("data.CamTheoMay.ghiNhanDangNhap(session.IMEI");
        Kiem.dung("ghi nhan SAU khi gan session.userID",
                viTriGhi > viTriGanUserId);
    }

    // -----------------------------------------------------------------------
    //  Hai dau phai khop nhau: client ghi gi thi server doc dung thu tu do
    // -----------------------------------------------------------------------
    //
    // Goi dang nhap la mot day nhi phan KHONG CO TEN TRUONG. Client ghi theo thu tu,
    // server doc theo thu tu. Them mot truong o giua ben nao la lech het phia sau, va
    // KHONG co loi nao bao ca — chi la moi gia tri deu sai. Nen phai canh o day.
    private static void kiemHaiDauKhopNhau() {
        Kiem.nhom("Cam theo may - client va server khop thu tu truong");

        final String client = doc(F_CLIENT);
        if (client.isEmpty()) {
            Kiem.dung("doc duoc " + F_CLIENT, false);
            return;
        }

        // Chi lay than ham login(), khong lay ca tep.
        final int mo = client.indexOf("public void login(string username, string pass, string version)");
        final int dong = (mo < 0) ? -1 : client.indexOf("session.sendMessage(message);", mo);
        Kiem.dung("tim thay ham login() ben client", mo >= 0 && dong > mo);
        if (mo < 0 || dong <= mo) {
            return;
        }
        final String than = client.substring(mo, dong);

        Kiem.dung("client CO gui ma may trong goi dang nhap",
                than.contains("writeUTF(Canvas.IMEI"));
        Kiem.dung("co chan null (writeUTF(null) se lam hong ca goi dang nhap)",
                than.contains("Canvas.IMEI ?? \"\""));

        // Ma may phai la lenh ghi CUOI CUNG.
        // Lay lenh ghi cuoi cung roi xem no ghi gi — KHONG so vi tri cua
        // "writeUTF(Canvas.IMEI" voi vi tri cua "writer().write", vi chuoi thu hai nam
        // NGAY TRUOC chuoi thu nhat tren cung mot dong nen hai vi tri khong the bang nhau.
        final int viTriGhiCuoi = than.lastIndexOf("writer().write");
        final String ghiCuoi = (viTriGhiCuoi < 0) ? ""
                : than.substring(viTriGhiCuoi, Math.min(than.length(), viTriGhiCuoi + 60));
        Kiem.dung("ma may la lenh ghi CUOI CUNG cua goi", ghiCuoi.contains("Canvas.IMEI"));

        // Dem so lenh ghi ben client va so lenh doc ben server — phai bang nhau.
        final String server = boChuThich(doc(F_LOGIN));
        final int moS = server.indexOf("String uname = dis.readUTF();");
        final int dongS = (moS < 0) ? -1 : server.indexOf("session.IMEI = data.CamTheoMay.lamSach(dis.readUTF())", moS);
        if (moS >= 0 && dongS > moS) {
            final String thanS = server.substring(moS, dongS);
            final int ghi = dem(than, "writer().write");
            final int doc = dem(thanS, "dis.read") + 1;   // +1: chinh lenh doc ma may
            Kiem.bang("so truong client ghi = so truong server doc", ghi, doc);
        } else {
            Kiem.dung("tim thay khoi doc goi dang nhap ben server", false);
        }
    }

    private static int dem(final String trong, final String tim) {
        int n = 0;
        int i = trong.indexOf(tim);
        while (i >= 0) {
            n++;
            i = trong.indexOf(tim, i + tim.length());
        }
        return n;
    }

    // -----------------------------------------------------------------------
    //  Khong duoc ghep chuoi cua client vao cau SQL
    // -----------------------------------------------------------------------
    private static void kiemKhongGhepChuoiVaoSql() {
        Kiem.nhom("Cam theo may - khong ghep du lieu client vao cau SQL");

        final String ma = boChuThich(doc(F_CAM));
        Kiem.dung("doc duoc " + F_CAM, ma.length() > 0);

        Kiem.dung("dung PreparedStatement", ma.contains("PreparedStatement"));
        Kiem.dung("truyen ma may bang tham so ?", ma.contains("ps.setString(1, ma)"));

        // Phan lon ma nguon du an nay ghep thang chuoi vao SQL. O day KHONG duoc phep,
        // vi ma may den tu client. Soi tung dong co chu SQL xem co dau + kem bien khong.
        final String[] dong = ma.split("\n");
        final java.util.List<String> pham = new java.util.ArrayList<String>();
        for (final String d : dong) {
            final String t = d.trim();
            final boolean laSql = t.contains("SELECT ") || t.contains("INSERT ")
                    || t.contains("UPDATE ") || t.contains("DELETE ");
            if (!laSql) {
                continue;
            }
            // Cho phep ghep HANG SO ten bang (BANG_CAM / BANG_LICH_SU), cam moi thu khac.
            final String conLai = t.replace("BANG_CAM", "").replace("BANG_LICH_SU", "");
            if (conLai.contains("+ ma") || conLai.contains("+ username")
                    || conLai.contains("+ ip") || conLai.contains("+ maMay")) {
                pham.add(t);
            }
        }
        Kiem.bang("khong dong SQL nao ghep bien cua client", 0, pham.size());

        // Hong thi phai CHO QUA, khong duoc chan dang nhap.
        Kiem.dung("co ghi chu ro rang la fail-open",
                doc(F_CAM).contains("fail-open"));
    }

    // -----------------------------------------------------------------------
    private static String boChuThich(final String ma) {
        final StringBuilder sb = new StringBuilder(ma.length());
        boolean trongKhoi = false;
        for (final String dong : ma.split("\n", -1)) {
            String t = dong;
            if (trongKhoi) {
                final int het = t.indexOf("*/");
                if (het < 0) {
                    continue;
                }
                t = t.substring(het + 2);
                trongKhoi = false;
            }
            final int mo = t.indexOf("/*");
            if (mo >= 0) {
                final int het = t.indexOf("*/", mo + 2);
                if (het >= 0) {
                    t = t.substring(0, mo) + t.substring(het + 2);
                } else {
                    t = t.substring(0, mo);
                    trongKhoi = true;
                }
            }
            final int hai = t.indexOf("//");
            if (hai >= 0) {
                t = t.substring(0, hai);
            }
            sb.append(t).append('\n');
        }
        return sb.toString();
    }

    private static String doc(final String duongDan) {
        final File f = new File(duongDan);
        if (!f.isFile()) {
            return "";
        }
        final StringBuilder sb = new StringBuilder((int) f.length() + 16);
        Reader r = null;
        try {
            r = new InputStreamReader(new FileInputStream(f), "UTF-8");
            final char[] dem = new char[8192];
            int n;
            while ((n = r.read(dem)) > 0) {
                sb.append(dem, 0, n);
            }
        } catch (final Exception ex) {
            return "";
        } finally {
            try {
                if (r != null) {
                    r.close();
                }
            } catch (final Exception ignored) {
            }
        }
        return sb.toString();
    }

    private KiemCamTheoMay() {
    }
}
