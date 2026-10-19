import game.model.Char;
import game.pokemon.MauPP;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Kiểm "HP cũ sau khi dùng bình" (0.9.3): gói HP / PP của server (lệnh 22 bình, lệnh 60 HMP) đọc + áp qua MỘT nguồn
 * (game.pokemon.MauPP) -> Char.hp / mp (HUD mọi bản đọc thẳng các trường đó) bằng giá trị mới NGAY sau khi xử lý gói,
 * không đợi gói định kỳ; đồng bộ realHP trễ của cú đánh trước không đè lại; PP không ghi vào HP; MessageHandler
 * lệnh 22 không còn "break" nuốt lệnh khi có byte cờ.
 *
 *   sh client_pc/build.sh    # lớp game + MIDP cho Java SE (client_pc/out/build/cls)
 *   O=$(mktemp -d); C=client_pc/out/build/cls
 *   javac -cp $C -d $O client_jar/src/game/pokemon/MauPP.java client_jar/tools/KiemMauPP.java
 *   java -Djava.awt.headless=true -cp $O:$C:client_pc/out/build/res KiemMauPP client_jar/src   # thoát 1 = hỏng
 */
public final class KiemMauPP {
    static int dat, hong;

    static void bang(String mo, Object mong, Object that) {
        if (mong.equals(that)) {
            dat++;
        } else {
            hong++;
            System.out.println("HONG " + mo + ": mong " + mong + " thuc te " + that);
        }
    }

    static void dung(String mo, boolean v) {
        bang(mo, Boolean.TRUE, Boolean.valueOf(v));
    }

    /** Lệnh 22 đúng như server ghi (Map.doUseHP / createMsgUseHpMP); coCo = có byte cờ isHP ở cuối. */
    static DataInputStream g22(int id, int loai, int them, int moi, boolean coCo, int isHp) throws Exception {
        ByteArrayOutputStream bo = new ByteArrayOutputStream();
        DataOutputStream o = new DataOutputStream(bo);
        o.writeShort(id);
        o.writeByte(loai);
        o.writeShort(them);
        o.writeInt(moi);
        if (coCo) o.writeByte(isHp);
        return new DataInputStream(new ByteArrayInputStream(bo.toByteArray()));
    }

    /** Lệnh 60 (createNew_HMP_Message); mp < 0 = server cũ (không có PP ở đuôi). */
    static DataInputStream g60(int id, int maxhp, int maxmp, int def, int hp, int cong, int mp) throws Exception {
        ByteArrayOutputStream bo = new ByteArrayOutputStream();
        DataOutputStream o = new DataOutputStream(bo);
        o.writeShort(id);
        o.writeInt(maxhp);
        o.writeInt(maxmp);
        o.writeShort(def);
        o.writeInt(hp);
        o.writeShort(cong);
        if (mp >= 0) o.writeInt(mp);
        return new DataInputStream(new ByteArrayInputStream(bo.toByteArray()));
    }

    static Char nv() {
        Char c = new Char();
        c.ID = 7;
        c.maxhp = 1000;
        c.hp = 300;
        c.realHP = 300;
        c.maxmp = 500;
        c.mp = 100;
        return c;
    }

    static int[] ap22(Char c, DataInputStream r) throws Exception {
        int[] v = MauPP.doc22(r);
        MauPP.apDung22(c, v[1], v[3], v[4]);
        return v;
    }

    public static void main(String[] a) throws Exception {
        // bình HP: server gửi CÓ byte cờ (bản 0.9.2 bị break nuốt cả lệnh) -> HP mới ngay
        Char c = nv();
        ap22(c, g22(7, 1, 200, 500, true, 1));
        bang("binh HP (co co): hp ngay sau goi", Integer.valueOf(500), Integer.valueOf(c.hp));
        bang("binh HP: realHP", Integer.valueOf(500), Integer.valueOf(c.realHP));
        // cú đánh vừa trừ máu còn chờ đồng bộ (setRealHP 20 nhịp): bình đến sau -> không bị giá trị cũ đè
        c = nv();
        c.setRealHP(250);
        ap22(c, g22(7, 2, 300, 550, true, 1));
        bang("binh HP sau cu danh cho dong bo: hp", Integer.valueOf(550), Integer.valueOf(c.hp));
        bang("binh HP sau cu danh: huy dong bo tre", Integer.valueOf(0), Integer.valueOf(c.realHPSyncTime));
        // bình HP 93 / 94 (trước: default -> bỏ qua)
        c = nv();
        ap22(c, g22(7, 93, 400, 700, true, 1));
        bang("binh HP 93: hp", Integer.valueOf(700), Integer.valueOf(c.hp));
        // giá trị tuyệt đối: server nói HP thấp hơn (trước: chỉ tăng) -> theo server
        c = nv();
        ap22(c, g22(7, 1, 0, 280, true, 1));
        bang("gia tri tuyet doi cua server", Integer.valueOf(280), Integer.valueOf(c.hp));
        // PP: server 0.9.2 gửi cờ isHP = 1 cả cho PP (loại 4) -> không được ghi vào HP
        c = nv();
        ap22(c, g22(7, 4, 50, 150, true, 1));
        bang("binh PP co co 1: mp", Integer.valueOf(150), Integer.valueOf(c.mp));
        bang("binh PP co co 1: hp khong doi", Integer.valueOf(300), Integer.valueOf(c.hp));
        // PP về 0 không phải gục
        c = nv();
        ap22(c, g22(7, 4, -100, 0, true, 0));
        bang("PP = 0: mp", Integer.valueOf(0), Integer.valueOf(c.mp));
        dung("PP = 0: khong guc", c.state != 3);
        // HP về 0 = gục
        c = nv();
        ap22(c, g22(7, 1, -300, 0, true, 1));
        dung("HP = 0: guc", c.hp == 0 && c.state == 3);
        // server cũ không có byte cờ
        c = nv();
        ap22(c, g22(7, 5, 80, 180, false, 0));
        bang("binh PP khong co co: mp", Integer.valueOf(180), Integer.valueOf(c.mp));
        // mã lạ dùng cờ isHP (82: hồi máu qua sự kiện)
        c = nv();
        ap22(c, g22(7, 82, 100, 400, true, 1));
        bang("ma 82 co isHP: hp", Integer.valueOf(400), Integer.valueOf(c.hp));
        // lệnh 60 mới: HP + max + PP
        c = nv();
        MauPP.apDung60(c, MauPP.doc60(g60(7, 1200, 600, 30, 900, 600, 420)));
        dung("lenh 60 moi: hp / maxhp / mp", c.hp == 900 && c.realHP == 900 && c.maxhp == 1200 && c.maxmp == 600 && c.mp == 420);
        // lệnh 60 cũ (không PP): PP giữ nguyên
        c = nv();
        MauPP.apDung60(c, MauPP.doc60(g60(7, 1000, 500, 30, 650, 350, -1)));
        dung("lenh 60 cu: hp moi, mp giu", c.hp == 650 && c.mp == 100);
        // nguồn: MessageHandler lệnh 22 / 3 qua MauPP, không còn "break" trong try trước onUsePotion
        if (a.length > 0) {
            String s = new String(Files.readAllBytes(Paths.get(a[0], "game/networklogic/MessageHandler.java")), StandardCharsets.UTF_8);
            int i = s.indexOf("case 22: {");
            dung("MessageHandler lenh 22 doc qua MauPP.doc22", i > 0 && s.indexOf("MauPP.doc22(msg.reader())", i) > i
                    && s.indexOf("MauPP.doc22(msg.reader())", i) < s.indexOf("case 23:", i));
            dung("MessageHandler lenh 22 khong con 'ishp = ... break'", s.indexOf("ishp = msg.reader().readByte();\n                        break;") < 0);
            dung("MessageHandler lenh 3 dat HP qua MauPP", s.indexOf("MauPP.datHp(m, i37)") > 0);
            String gs = new String(Files.readAllBytes(Paths.get(a[0], "game/render/screen/GameScr.java")), StandardCharsets.UTF_8);
            dung("GameScr.onUsePotion qua MauPP.apDung22", gs.indexOf("MauPP.apDung22(c, potionType, valueNew, isHP)") > 0);
            dung("GameScr.onNewHPMP qua MauPP.doc60", gs.indexOf("MauPP.doc60(msg.reader())") > 0);
        }
        System.out.println("KiemMauPP: DAT " + dat + ", HONG " + hong);
        System.exit(hong == 0 ? 0 : 1);
    }
}
