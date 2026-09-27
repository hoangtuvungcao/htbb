import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.regex.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import data.ModelWearing;
import real.Item;
import real.ItemTemplates;

public final class KiemTrungThu2026 {
    public static void main(String[] args) { real.Map.gemTemplate = new real.GemTemplate[400]; for (int i=0; i<400; i++) real.Map.gemTemplate[i] = new real.GemTemplate(); chay(); System.exit(Kiem.tongKet()); }
    private static final class TestItem extends Item {
        private final ItemTemplates template;
        TestItem(ItemTemplates t) { template = t; tempateID = t.id; }
        @Override public ItemTemplates getTemplate() { return template; }
    }
    public static void chay() {
        Kiem.nhom("Trung Thu 2026: chi so Hac Lang/Than Nu va tai nguyen");
        try {
            String sql = new String(Files.readAllBytes(Paths.get("sql/gamedb7_clean.sql")), StandardCharsets.UTF_8);
            for (int sex = 0; sex < 2; sex++) {
                ItemTemplates old = template(sql, 723 + sex), fresh = template(sql, 912 + sex);
                ModelWearing before = wearing(old), after = wearing(fresh);
                Kiem.bang("HP " + fresh.id, before.getHp(), after.getHp());
                Kiem.bang("MP " + fresh.id, before.getMp(), after.getMp());
                Kiem.bang("Tan cong " + fresh.id, before.getAttack(), after.getAttack());
                Kiem.bang("Phong thu " + fresh.id, before.getDefend(), after.getDefend());
                Kiem.bang("Bang set doc lua lan " + fresh.id, before.getPCXbangSetDocLuaLan(), after.getPCXbangSetDocLuaLan());
                Kiem.bang("Khong them giam sat thuong " + fresh.id, 0, after.getPcGiamSatThuong());
                Kiem.bang("Hieu ung " + fresh.id, old.idEff, fresh.idEff);
                Kiem.bang("Gioi tinh " + fresh.id, old.gender, fresh.gender);
                Kiem.dung("Nhan dien bo moi " + fresh.id, after.isTrungThu2026());
                Kiem.dung("An non thuong " + fresh.id, after.doNotPaintHat());
                BufferedImage icon = ImageIO.read(new File("map/icon/" + (868 + sex) + ".png"));
                Kiem.dung("Icon 18x18", icon.getWidth() == 18 && icon.getHeight() == 18);
                byte[] png = Files.readAllBytes(Paths.get("map/icon/" + (866 + sex) + ".png"));
                checkPart("head", 84 + sex, 1, png);
                checkPart("body", 81 + sex, 5, png);
                checkPart("leg", 81 + sex, 5, png);
                int[] avt = new real.Char(null).getIdBigAoThoiTrang();
                Kiem.bang("Avatar mac dinh", -1, avt[0]);
                real.Char c = new real.Char(null);
                c.wModel = after;
                avt = c.getIdBigAoThoiTrang();
                Kiem.bang("Avatar moi", 866 + sex, avt[0]);
                Kiem.bang("So khung avatar", 2, avt[2]);
            }
        } catch (Exception e) { Kiem.dung("Trung Thu: " + e, false); }
    }
    private static ModelWearing wearing(ItemTemplates t) {
        ModelWearing m = new ModelWearing();
        m.idModel = 1; m.itemModel = new TestItem(t); return m;
    }
    private static ItemTemplates template(String sql, int id) throws IOException {
        Matcher m = Pattern.compile("(?m)^\\(" + id + ",'[^']*',(.*)\\)[,;]$").matcher(sql);
        if (!m.find()) throw new IOException("Thieu item " + id);
        String[] f = m.group(1).split(",");
        ItemTemplates t = new ItemTemplates();
        t.id = id; t.type = Byte.parseByte(f[0]); t.gender = Byte.parseByte(f[3]);
        t.atb = new short[10];
        for (int i = 0; i < 10; i++) t.atb[i] = Short.parseShort(f[6+i]);
        t.idEff = Integer.parseInt(f[24]); return t;
    }
    private static void checkPart(String type, int id, int count, byte[] expected) throws Exception {
        DataInputStream in = new DataInputStream(new FileInputStream("map/c/" + type + "/" + id));
        byte[] png = new byte[in.readUnsignedShort()]; in.readFully(png);
        Kiem.dung("PNG goc " + type + "/" + id, Arrays.equals(expected, png));
        BufferedImage image = ImageIO.read(new ByteArrayInputStream(png));
        byte[] metadata = new byte[in.readUnsignedShort()]; in.readFully(metadata);
        Kiem.bang("Khong byte thua", -1, in.read()); in.close();
        DataInputStream data = new DataInputStream(new ByteArrayInputStream(metadata));
        data.skipBytes(6);
        for (int dir = 0; dir < 4; dir++) {
            for (int frame = 0; frame < count; frame++) {
                int x=data.readUnsignedByte(), y=data.readUnsignedByte();
                int w=data.readUnsignedByte(), h=data.readUnsignedByte();
                Kiem.dung("Khung trong anh " + type + "/" + id,
                    w > 0 && h > 0 && x+w <= image.getWidth() && y+h <= image.getHeight());
            }
            data.skipBytes(12);
        }
        Kiem.bang("Dung dinh dang client", -1, data.read());
    }
}
