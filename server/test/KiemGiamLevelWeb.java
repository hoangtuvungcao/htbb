import data.LenhWeb;
import real.LevelDetail;
public class KiemGiamLevelWeb {
    public static void main(String[] args) {
        for (int old = 2; old <= LevelDetail.expMain.length; old++) {
            for (int amount = 1; amount < old; amount++) {
                int target = LenhWeb.capSauKhiGiam(old, amount);
                LevelDetail level = new LevelDetail();
                level.setExpNew(LevelDetail.expMain[target - 1]);
                if (level.lv != old - amount || level.percent != 0) throw new AssertionError("Wrong level/EXP");
            }
        }
        int[][] bad = {{1,1}, {50,50}, {50,51}, {50,0}, {50,-1}, {50,Integer.MAX_VALUE}};
        for (int[] input : bad) {
            try {
                LenhWeb.capSauKhiGiam(input[0], input[1]);
                throw new AssertionError("Accepted invalid reduction");
            } catch (IllegalArgumentException expected) { }
        }
        System.out.println("PASS: every valid level reduction and invalid/overflow boundaries");
    }
}