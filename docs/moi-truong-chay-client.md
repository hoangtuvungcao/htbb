# Linh thu khong hien: loi o MOI TRUONG CHAY client, khong phai server

## Ket luan

Cung mot file `KPAH_276_TEST_v3.jar`, cung mot may chu:

| Chay bang | Linh thu |
|---|---|
| MicroEmulator (`microemulator.jar`) | **hien dung** |
| Moi truong nguoi dung van dung | khong hien |

Nen phia server khong phai sua gi. Toan bo phep do deu da xac nhan dieu do
tu truoc (xem phan duoi).

## Da xac minh nhung gi

Tat ca deu do bang cach chay MA CUA CHINH CLIENT tren BYTE CUA CHINH SERVER,
khong phai doc code roi suy dien:

- **Goi 26**: `eff=254 thucuoi=du`, khop het khong thua byte. Nap vao
  `effect.DataSkillEff.ALL_DATA_EFF` that thi id 8764 co **21 khung**, trong
  khi `getFrameHorse()` toi da tra 18 -> du de ve.
- **Goi 3**: `idhorse=8764 typehorse=2`, khop het khong thua byte, khong co goi
  nao mang `-1` (da bo dieu kien chan va doi khoa chong trung sang theo gia tri).
- **Goi 5**: khong duoc gui cho chinh nhan vat (`nearChars` loai self).
- **Anh atlas**: 8764 can `138x192`, `map/d_effect/64.png` dung `138x192`,
  khong o sprite nao tran ra ngoai.
- **EFF_PACK**: 38 file, co `explosion.png` -> `Res.setEffSh` khong nem, nen
  khong lam mat ca goi 26.
- **Bang thu tu ve**: ca 8 o `ORDER_PAINT` deu chua so 6 (o thu cuoi).

## Cach dung lai moi truong chay duoc

    java -jar microemulator.jar --enhanceCatchBlock --rms memory \
         --resizableDevice 800 600 KPAH_276_TEST_v3.jar

`--enhanceCatchBlock` la phan quan trong nhat khi go loi: client nay co rat
nhieu khoi `catch { }` RONG, nen loi bi nuot het. Co nay in chung ra.

## Diem can nho ve client nay

Ngay trong phien chay DUNG, `MessageHandler.onMessage` van nem `EOFException`
**109 lan** - client doc qua cuoi goi roi ca ham xu ly thoat giua chung. No
khong can con thu o day, nhung nghia la client dua vao EOF lam diem dung, nen
doi moi truong chay la hanh vi lech han.

Danh sach day du cac ngoai le bi nuot trong phien chay dung: xem
`nhat-ky-client-chay-duoc.txt` - dung lam moc de so voi moi truong hong.
