package game.pokemon;

import game.model.Actor;
import game.model.AnimalMove;
import game.model.Char;
import game.model.monster.Pet;
import java.util.Vector;

/**
 * MỘT pet đang đi theo cho MỖI người chơi (mình lẫn người khác) - chỗ duy nhất thêm / thay / bỏ pet trong danh sách
 * actor của màn chơi.
 *
 * Lỗi cũ "hai pet đi theo dù chỉ có một": pet kiểu 5 (Pet, tool) được thêm vào actor nhưng Char.myPet lại giữ MẪU
 * AnimalMove từ gói 37 -> đổi map, Tilemap.reset thêm lại MẪU (vẽ bằng PokeBody) rồi máy chủ gửi lại trang bị ->
 * thêm Pet mới = 2 pet; đổi pet kiểu cũ -> kiểu 5 (hoặc ngược lại) không gỡ pet loại kia; pet đã cất (mờ dần) vẫn được
 * Tilemap.reset thêm lại; pet AnimalMove của người chơi khác không bao giờ bị gỡ khi họ ra khỏi tầm nhìn; đổi
 * nhân vật / đăng nhập lại giữ pet của nhân vật cũ.
 *
 * Quy tắc: Char.petCanh = actor pet ĐANG ở trong cảnh (Pet hoặc AnimalMove), Char.myPet = thông tin pet (mô tả, thời
 * hạn) như cũ. Mọi đường (gói trang bị, cất pet, đổi map, đăng nhập lại, người khác rời tầm nhìn) đi qua đây.
 */
public final class PetDuyNhat {
    private PetDuyNhat() {
    }

    /** Actor a có phải pet (đi theo) của người chơi idChu không. */
    public static boolean laPetCua(Actor a, int idChu) {
        if (a == null || a.catagory != 12) {
            return false;
        }
        if (a instanceof Pet) {
            return a.ID == idChu;
        }
        if (a instanceof AnimalMove) {
            AnimalMove m = (AnimalMove) a;
            return m.idSameMyOwnerID == idChu || m.myOwner != null && m.myOwner.ID == idChu;
        }
        return false;
    }

    /** Số pet (còn hiện, không tính pet đang mờ dần biến mất) của idChu trong ds. */
    public static int dem(Vector ds, int idChu) {
        int n = 0;
        for (int i = 0; i < ds.size(); i++) {
            Actor a = (Actor) ds.elementAt(i);
            if (laPetCua(a, idChu) && !dangBienMat(a)) {
                n++;
            }
        }
        return n;
    }

    static boolean dangBienMat(Actor a) {
        return !(a instanceof Pet) && a instanceof AnimalMove && ((AnimalMove) a).status == 3 || a.wantDestroy;
    }

    /** Gỡ mọi pet của idChu khỏi ds trừ giu (có thể null). Trả số đã gỡ. */
    public static int bo(Vector ds, int idChu, Actor giu) {
        int n = 0;
        for (int i = ds.size() - 1; i >= 0; i--) {
            Actor a = (Actor) ds.elementAt(i);
            if (a != giu && laPetCua(a, idChu)) {
                ds.removeElementAt(i);
                n++;
            }
        }
        return n;
    }

    /** Pet AnimalMove (không phải tool) hiện có của idChu để dùng lại, null = không có. */
    public static AnimalMove timAnimal(Vector ds, int idChu) {
        for (int i = 0; i < ds.size(); i++) {
            Actor a = (Actor) ds.elementAt(i);
            if (a instanceof AnimalMove && !(a instanceof Pet) && laPetCua(a, idChu)) {
                return (AnimalMove) a;
            }
        }
        return null;
    }

    /** Đặt pet trong cảnh của chủ = p (đã nằm trong ds hoặc sẽ thêm): gỡ hết pet khác của chủ, thêm p nếu thiếu. */
    public static void dat(Vector ds, Char chu, AnimalMove p) {
        bo(ds, chu.ID, p);
        if (p != null && !ds.contains(p)) {
            ds.addElement(p);
        }
        chu.petCanh = p;
    }

    /** Cất pet (gói trang bị không còn pet): pet trong cảnh mờ dần biến mất, các bản thừa gỡ ngay. */
    public static void cat(Vector ds, Char chu) {
        AnimalMove p = chu.petCanh;
        if (p == null || !ds.contains(p)) {
            p = timAnimal(ds, chu.ID);
        }
        bo(ds, chu.ID, p);
        if (p instanceof Pet) {
            ds.removeElement(p);
        } else if (p != null) {
            p.status = (short) 3;
            p.timeMagic = 20;
        }
        chu.petCanh = null;
        chu.myPet = null;
    }

    /**
     * Đổi map (Tilemap.reset, ds vừa xoá chỉ còn nhân vật chính): thêm lại đúng MỘT pet của nhân vật chính - chỉ
     * khi pet còn hạn / chưa cất và thuộc đúng nhân vật này (đổi nhân vật / đăng nhập lại: bỏ pet cũ).
     */
    public static void sauDoiMap(Vector ds, Char chu) {
        AnimalMove p = chu.petCanh;
        if (p == null || dangBienMat(p) || !laPetCua(p, chu.ID)) {
            chu.petCanh = null;
            if (chu.myPet != null && chu.myPet.idSameMyOwnerID != chu.ID) {
                chu.myPet = null;   // pet của nhân vật cũ (đổi nhân vật / đăng nhập lại)
            }
            return;
        }
        bo(ds, chu.ID, null);
        ds.addElement(p);
    }

    /** Dọn định kỳ: pet mà chủ không còn trong cảnh, và pet thừa (giữ pet trong cảnh của chủ / bản đầu tiên). */
    public static int donDep(Vector ds) {
        int n = 0;
        for (int i = ds.size() - 1; i >= 0; i--) {
            Actor a = (Actor) ds.elementAt(i);
            if (a == null || a.catagory != 12 || !(a instanceof AnimalMove)) {
                continue;
            }
            Char chu = chuCua(ds, a);
            if (chu == null) {
                // chủ đã rời cảnh (người chơi khác ra khỏi tầm nhìn / thoát)
                ds.removeElementAt(i);
                n++;
                continue;
            }
            Actor giu = chu.petCanh != null && ds.contains(chu.petCanh) ? (Actor) chu.petCanh : null;
            if (giu != null && a != giu && !dangBienMat(a)) {
                ds.removeElementAt(i);
                n++;
            }
        }
        return n;
    }

    /** Char chủ của pet a đang ở trong ds (theo ID), null = không có. */
    static Char chuCua(Vector ds, Actor a) {
        int id = a instanceof Pet ? a.ID : ((AnimalMove) a).idSameMyOwnerID;
        if (!(a instanceof Pet) && ((AnimalMove) a).myOwner != null) {
            id = ((AnimalMove) a).myOwner.ID;
        }
        for (int i = 0; i < ds.size(); i++) {
            Object o = ds.elementAt(i);
            if (o instanceof Char && ((Char) o).catagory == 0 && ((Char) o).ID == id) {
                return (Char) o;
            }
        }
        return null;
    }
}
