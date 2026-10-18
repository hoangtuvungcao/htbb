/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package real;

import data.Animal;
import data.Database;
import data.Pet;
import io.Message;

/**
 *
 *
 */
public class doCuoiThu {

    public static void doCuoiThu(Char player, Message message) {
        try {
            byte type = 0;

            try {
                type = message.dis.readByte();
            } catch (Exception var8) {
                var8.printStackTrace();
            }

            short id = message.dis.readShort();

//            System.out.println("BẮT ĐẦU CƯỠI NGỰAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAa: type: " + type + " id: " + id);

            short idPet;
            if (type == 0) {
                idPet = id;

                for (int i = 0; i < player.animal.size(); ++i) {
                    Animal animal = (Animal) player.animal.get(i);
                    if (animal.id != idPet) {
                        animal.place = 0;
                    } else {
                        if (player.map.isMapOffline) {
                            if ((animal.isPoro() || animal.isLan()) && animal.place == 0) {
                                player.sendMessage(MessageCreator.createMsgMenu(new String[]{"Sử dụng", "Bán"}, -800 - type, i));
                                return;
                            }
                        } else if (animal.isAnimalCanTang() && animal.place == 0) {
                            player.sendMessage(MessageCreator.createMsgMenu(new String[]{"Sử dụng", "Tặng"}, -800 - type, i));
                            return;
                        }

                        if (player.rideHorse > 0) {
                            player.sendMessage(MessageCreator.createServerAlertMessage("Không thể dùng khi đang cưỡi thú khác.", ""));
                            return;
                        }

//                        System.out.println("CƯỠI CON LỒN GÌ ĐÂY ????? : " + animal.name);

                        player.rideHorse = 0;
                        player.animalRide = null;
                        animal.place = 1;
                        player.animalRide = animal;
                        player.rideHorse = Animal.rideHorse[animal.idImg];
                        player.idImgHorse = Animal.rideHorse[animal.idImg];
                    }
                }

                // Skin gan theo NHAN VAT nen doi con thu van giu skin cu. Van phai goi
                // dong bo o day: luc chua cuoi thu thi getAnimalWearing khong chay nen
                // vat pham skin chua duoc doc, va SkinThuCuoi.dangMang doi phai co
                // animalRide. Goi truoc khi dung cac goi tin ben duoi vi ca ba goi deu
                // mang hinh con thu.
                SkinThuCuoi.dongBoTheoTrangBi(player);
                // calculateAttrib() PHAI chay SAU dong tren, khong duoc truoc.
                //
                // SkinThuCuoi.dangMang doi phai co animalRide, ma animalRide vua moi duoc
                // gan ngay ben tren — tuc chi so skin chuyen tu TAT sang BAT dung tai day.
                // Tinh truoc thi tinh voi trang thai "chua cuoi", nguoi choi len thu xong
                // thay tiem nang tang ma tan cong dung im cho den lan mac do ke tiep.
                //
                // Cung KHONG duoc gan vao dieu kien "dongBoTheoTrangBi tra true": ham do
                // chi tra true khi so hieu skin DOI, ma o day so hieu thuong khong doi
                // (da co san tu luc dang nhap) — cai doi la animalRide.
                player.calculateAttrib();
                player.sendMessage(MessageCreator.createCharInventoryMessage(player, 2));
                player.sendMessage(MessageCreator.createMainCharInfoMessage(player));
                player.sendToNearPlayer(MessageCreator.createCharInfo(player));
                player.sendMessage(MessageCreator.createCharWearingMessage(player, player));
                player.sendMessage(MessageCreator.createAnimalWearingMessage(player, player));
                // KHONG gui createAnimalWearingMessage cho nguoi xung quanh - client hieu
                // goi nay la "mo cua so Trang bi thu" (GameScr.onAnimalWearingInfo), charID
                // khac nguoi nhan se hat man hinh ho sang xem trang bi thu cua nguoi vua len
                // thu, cat ngang viec ho dang lam (xem docs/thiet-ke-game.md). Ho cung
                // khong can goi do: du lieu duy nhat no mang la imgAnimal, chi dung ve trong
                // cua so thong tin, khong dung ve con thu tren ban do (hinh tren ban do di
                // bang createCharInfo, da gui vo dieu kien o dong tren).
                MessageCreator.createMsgCharMonster(player, player);
            } else if (type == 1) {
                idPet = id;
                Pet pet = null;

                for (int i = 0; i < player.pet.size(); ++i) {
                    pet = (Pet) player.pet.get(i);
                    if (pet.id == idPet) {
                        if (player.map.isMapOffline && pet.isPetBachThu() && pet.place == 0) {
                            player.sendMessage(MessageCreator.createMsgMenu(new String[]{"Sử dụng", "Bán"}, -800 - type, i));
                            return;
                        }

                        pet.place = 1;
                        break;
                    }
                }

                if (pet != null && pet.canTrade()) {
                    player.idPetTang = idPet;
                    player.sendMessage(MessageCreator.createMsgMenu((String[]) (new String[]{"Sử dụng", "Giao dịch"}), -514, 0));
                    pet.place = 0;
                    return;
                }

                if (pet != null) {
                    if (player.petUsing != null) {
                        player.petUsing.place = 0;
                        if (!player.petUsing.isPetTool() && player.petUsing.getIdEffPet() > -1 && player.petUsing.getIdEffPet() != pet.getIdEffPet()) {
                            player.huyEff(player, player.petUsing.getIdEffPet());
                        }
                    }

                    player.petUsing = pet;
                    player.calculateAttrib();
                    player.sendMessage(MessageCreator.createMainCharInfoMessage(player));
                    player.sendMessage(MessageCreator.createCharInventoryMessage(player, 2));
                    player.sendMessage(MessageCreator.createCharWearingMessage(player, player));
                    player.sendToNearPlayer(MessageCreator.createCharWearingMessage(player, player));
                    if (!player.petUsing.isPetTool() && pet.getIdEffPet() > -1 && player.addEffBuff(pet.getIdEffPet(), System.currentTimeMillis() + 320000000L, EffectBuff.BY_ACTOR, 0) != null) {
                        player.sendEffToChar(player);
                    }

                    Database.instance.addInfoWebChar(player);
                } else {
                    player.sendMessage(MessageCreator.createServerAlertMessage("Không thể sử dụng", ""));
                }
            }
        } catch (Exception var9) {
            var9.printStackTrace();
        }

    }
}
