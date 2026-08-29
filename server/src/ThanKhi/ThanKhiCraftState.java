/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package ThanKhi;

/**
 *
 * @author ADMIN
 */
public class ThanKhiCraftState 
{
    // 0: Quạt ba tiêu | 1: Gậy như ý | 2: Lưỡng nhận đao
    private byte itemType = -1;

    // 0: Nguyên liệu không khóa | 1: Nguyên liệu khóa | 2: Nguyên liệu hỗn hợp
    private byte materialMode = -1;

    // 1..6 (menu truyền vào i=0..5 -> Level = i+1)
    private byte materialLevel = 0;

    // 5 nguyên liệu đã random (đã cộng offset theo cấp)
    private short[] randomMaterials = new short[0];

    // Mảng 2 chiều 3x5: mỗi hàng ứng với 1 ItemType (0..2), mỗi cột là 1 chỉ số nguyên liệu cơ sở (0..10)
    // Tương đương C#: public int[,] BaseIdxByItemType { get; } = new int[3, 5];
    private final int[][] baseIdxByItemType = new int[3][5];

    // Lưu trạng thái chọn nâng cấp ở menu case 61, Giá trị: 0..8 ứng với 9 lựa chọn trong menu nâng cấp
    private byte upgradeSelection = -1;
    
    private short[] upgradeItemIds = new short[0]; // Danh sách id item dùng cho menu nâng cấp (case 61)

    public ThanKhiCraftState() 
    {
        // Khởi tạo sẵn 5 nguyên liệu cơ sở theo quy tắc 2-2-1 cho từng ItemType
        for (byte type = 0; type < 3; type++) 
        {
            int[] arr = ThoRenThanBi.RandomBaseMaterialIndices();
            for (int k = 0; k < 5; k++) 
            {
                baseIdxByItemType[type][k] = arr[k];
            }
        }
    }

    /**
     * Xoá lựa chọn đã chọn ở menu, buộc phải đi lại menu Thợ Rèn cho lần tạo sau.
     *
     * BẮT BUỘC gọi sau mỗi lần tạo Thần Khí thành công. Trước đây không xoá, mà
     * lệnh tạo là một gói popup riêng (idpopup = 121, Map.java:10207) — client
     * chỉ cần đi menu ĐÚNG MỘT LẦN rồi gửi lại gói popup đó bao nhiêu lần tuỳ ý,
     * mỗi lần ra một món Thần Khí mới. CreateThanKhi có kiểm trạng thái ở đầu
     * hàm nhưng trạng thái đó không bao giờ hết hạn nên phép kiểm luôn qua.
     */
    public void datLai()
    {
        this.itemType = -1;
        this.materialMode = -1;
        this.materialLevel = 0;
        this.randomMaterials = new short[0];
    }

    public byte getItemType() {
        return itemType;
    }

    public void setItemType(byte itemType) {
        this.itemType = itemType;
    }

    public byte getMaterialMode() {
        return materialMode;
    }

    public void setMaterialMode(byte materialMode) {
        this.materialMode = materialMode;
    }

    public byte getMaterialLevel() {
        return materialLevel;
    }

    public void setMaterialLevel(byte materialLevel) {
        this.materialLevel = materialLevel;
    }

    public short[] getRandomMaterials() {
        return randomMaterials;
    }

    public void setRandomMaterials(short[] randomMaterials) {
        if (randomMaterials == null) {
            this.randomMaterials = new short[0];
        } else {
            this.randomMaterials = randomMaterials;
        }
    }

    public int[][] getBaseIdxByItemType() {
        return baseIdxByItemType;
    }

    public byte getUpgradeSelection() {
        return upgradeSelection;
    }

    public void setUpgradeSelection(byte upgradeSelection) {
        this.upgradeSelection = upgradeSelection;
    }
    
    public short[] getUpgradeItemIds() 
    {
        return upgradeItemIds;
    }

    public void setUpgradeItemIds(short[] upgradeItemIds) 
    {
        this.upgradeItemIds = (upgradeItemIds != null) ? upgradeItemIds : new short[0];
    }
}

