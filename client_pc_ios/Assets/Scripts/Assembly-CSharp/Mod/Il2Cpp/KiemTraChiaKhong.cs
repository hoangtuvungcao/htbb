using Unity.IL2CPP.CompilerServices;

// IL2CPP mặc định KHÔNG chèn kiểm tra chia số nguyên cho 0: phép chia cho 0 làm CPU báo lỗi -> game sập ngay
// (bản IL2CPP crash khi vào map: GameScr.paintMainCharInfo chia cho maxhp khi server chưa gửi chỉ số = 0).
// Mono thì ném DivideByZeroException và game chạy tiếp. Bật lại kiểm tra cho cả assembly để IL2CPP chạy giống
// Mono với code game dịch ngược (còn nhiều phép chia không chặn 0). Il2CppSetOptionAttribute.cs chép từ
// Editor/Data/il2cpp của Unity (IL2CPP nhận theo tên đầy đủ của attribute).
[assembly: Il2CppSetOption(Option.DivideByZeroChecks, true)]
