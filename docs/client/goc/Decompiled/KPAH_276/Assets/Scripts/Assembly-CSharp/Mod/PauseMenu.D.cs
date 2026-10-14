
// Port 1:1 tu Jar276/src/game/render/screen/PauseMenu.java: autoTick, autoCheDoTick, PauseMenu()
public partial class PauseMenu
{
	private void autoTick()
	{
		if (!this.autoRunning)
		{
			return;
		}
		if (this.autoAnyKeyPressed())
		{
			this.autoStop("Đã dừng auto chế đồ.");
			return;
		}
		if (this.autoJob == 0 && this.autoSlot >= AUTO_SLOT_NAME.Length)
		{
			this.autoStop("Auto chế đồ đã hoàn tất.");
			return;
		}
		if (Canvas.gameScr == null || Canvas.gameScr.mainChar == null)
		{
			return;
		}
		if (this.autoJob == 14)
		{
			this.nghienTick();
		}
		if (this.autoWaitTicks > 0)
		{
			--this.autoWaitTicks;
			return;
		}
		switch (this.autoPhase)
		{
			case 0:
			{
				this.autoLevel = this.autoJob == 6 ? (this.autoTbLevel == 60 ? 61 : this.autoTbLevel - 9) : (this.autoSlot == 0 ? (this.autoMode == 0 ? 21 : 36) : (this.autoMode == 0 ? 20 : 35));
				this.autoBuildCraftQueue();
				this.autoPhase = 1;
				break;
			}
			case 1:
			{
				while (this.autoQueuePos < this.autoQueueMenu.Length)
				{
					GameService.gI().doMenuOption(this.autoQueueNpc, (sbyte)this.autoQueueMenu[this.autoQueuePos], this.autoQueueOption[this.autoQueuePos]);
					++this.autoQueuePos;
				}
				this.autoPhase = 2;
				this.autoWaitCount = 0;
				break;
			}
			case 2:
			{
				if (WindowInfoScr.gI().listCheDo != null)
				{
					this.autoPhase = 3;
					break;
				}
				if (++this.autoWaitCount > 200)
				{
					this.autoStop("Auto chế đồ: không nhận được danh sách nguyên liệu, đã dừng.");
				}
				break;
			}
			case 3:
			{
				this.autoFill = this.autoJob == 11 || this.autoJob == 14 ? this.petFillCap() : this.autoFillMaterial();
				Canvas.menu.showMenu = false;
				if (this.autoFill == null && this.autoJob == 14)
				{
					// món này hết nguyên liệu đã chọn -> thử món kế; hết hẳn thì nghiền nốt rồi dừng
					if (Canvas.currentScreen == WindowInfoScr.gI())
					{
						WindowInfoScr.gI().right.action();
					}
					if (++this.autoSlot >= AUTO_SLOT_NAME.Length)
					{
						this.autoNbHet = true;
						this.autoPhase = 194;
						break;
					}
					this.autoPhase = 0;
					this.autoWaitTicks = 1;
					break;
				}
				if (this.autoFill == null && this.autoJob == 11)
				{
					// món này thiếu nguyên liệu cấp đã chọn -> thử món kế tiếp
					if (Canvas.currentScreen == WindowInfoScr.gI())
					{
						WindowInfoScr.gI().right.action();
					}
					if (++this.autoSlot >= AUTO_SLOT_NAME.Length)
					{
						this.autoStop("Auto nuôi pet: không có món nào chế được bằng " + (this.autoPetLoai < 0 ? "nguyên liệu" : PET_LOAI_TEN[this.autoPetLoai]) + " (sơ cấp cấp " + (this.autoPetCapSo + 1) + ", cao cấp cấp " + (this.autoPetCapNL + 1) + ") để chế đồ, đã dừng.");
						return;
					}
					this.autoPhase = 0;
					this.autoWaitTicks = 1;
					break;
				}
				if (this.autoFill == null)
				{
					if (Canvas.currentScreen == WindowInfoScr.gI())
					{
						WindowInfoScr.gI().right.action();
					}
					this.autoStop("Auto chế đồ: không đủ nguyên liệu tổng hợp (thường + khóa), đã dừng.");
					return;
				}
				if (this.autoJob == 6 && (this.autoFill.Length < 2 || this.autoFill[0][5] < ((Mineral)WindowInfoScr.gI().listCheDo.elementAt(0)).number || this.autoFill[1][5] < ((Mineral)WindowInfoScr.gI().listCheDo.elementAt(1)).number))
				{
					if (Canvas.currentScreen == WindowInfoScr.gI())
					{
						WindowInfoScr.gI().right.action();
					}
					this.autoStop("Auto thần binh: cần đủ nguyên liệu cấp 6 cho cả 2 loại (mới chắc ra vũ khí Hoàn mỹ Nhất phẩm), đã dừng.");
					return;
				}
				if (this.autoJob == 2 && !this.autoFillTopGrade())
				{
					if (Canvas.currentScreen == WindowInfoScr.gI())
					{
						WindowInfoScr.gI().right.action();
					}
					this.autoStop("Auto khóa: không đủ nguyên liệu cấp 6 (phải chiếm nhiều nhất mới ra được 20-21%), đã dừng.");
					return;
				}
				WindowInfoScr.gI().name = this.autoJob == 11 || this.autoJob == 14 ? "[Auto]" : (this.autoJob == 0 || this.autoJob == 6 ? "[Auto] " + AUTO_SLOT_NAME[this.autoSlot] + " lv" + this.autoLevel + " - lần " + (this.autoTries + 1) : "[Auto] " + AUTO_JOB_NAME[this.autoJob] + " - món " + (this.autoTargetPos + 1) + "/" + this.autoTargets.Length);
				this.autoStageIdx = 0;
				while (this.autoStageNext())
				{
				}
				this.autoPhase = 7;
				this.autoWaitTicks = this.autoJob == 11 ? 0 : 1;
				break;
			}
			case 7:
			{
				int n = WindowInfoScr.gI().listCheDo.size();
				if (Canvas.currentScreen == WindowInfoScr.gI())
				{
					WindowInfoScr.gI().right.action();
				}
				this.autoSnapshot = this.autoInventoryIds();
				this.autoOkSeen = false;
				this.autoRefObj = (this.autoJob == 2 || this.autoJob == 3) && this.autoTargets != null ? this.autoFindItem(this.autoTargets[this.autoTargetPos]) : null;
				GameService.gI().doCheDo((short)n, WindowInfoScr.idTemp, this.autoFill);
				if (this.autoJob == 11)
				{
					// ghi "công thức" để các lần sau chế cả lô không cần chờ server mở cửa sổ
					this.autoPetQNpc = this.autoQueueNpc;
					this.autoPetQMenu = this.autoQueueMenu;
					this.autoPetQOpt = this.autoQueueOption;
					this.autoPetIdTemp = WindowInfoScr.idTemp;
					this.autoPetFillMau = new sbyte[this.autoFill.Length][];
					this.autoPetNLId = new int[this.autoFill.Length];
					for (int i = 0; i < this.autoFill.Length; ++i)
					{
						this.autoPetFillMau[i] = new sbyte[this.autoFill[i].Length];
						System.Array.Copy(this.autoFill[i], 0, this.autoPetFillMau[i], 0, this.autoFill[i].Length);
						this.autoPetNLId[i] = ((Mineral)WindowInfoScr.gI().listCheDo.elementAt(i)).idTemplate;
					}
					this.autoPetCoCongThuc = true;
					this.autoPetLoCho = 1;
					this.batDauDs();
					// server xử lý tuần tự: xin luôn danh sách "Cho pet ăn" - đã có món vừa chế
					this.autoPetDsAn = null;
					this.autoPetDay = false;
					this.autoPetCanNang = false;
					this.xinDsAn();
				}
				this.autoPhase = this.autoJob == 0 ? 4 : (this.autoJob == 6 ? 70 : (this.autoJob == 11 ? 110 : (this.autoJob == 14 ? 190 : 11)));
				this.autoWaitCount = 0;
				break;
			}
			case 10:
			{
				if (this.autoTargetPos >= this.autoTargets.Length)
				{
					this.autoStop("Auto " + AUTO_JOB_NAME[this.autoJob] + " đã hoàn tất.");
					return;
				}
				ItemInInventory itemInInventory = this.autoFindItem(this.autoTargets[this.autoTargetPos]);
				if (itemInInventory == null || this.autoJobDone(itemInInventory))
				{
					if (itemInInventory != null)
					{
						++this.autoFound;
					}
					++this.autoTargetPos;
					break;
				}
				GameService.gI().doRequestActionCheDo(this.autoJob == 1 ? 11 : (this.autoJob == 2 ? 12 : 13), itemInInventory.itemID);
				this.autoQueueNpc = -502;
				this.autoQueueMenu = new int[] { 0 };
				this.autoQueueOption = new int[] { 2 };
				this.autoQueuePos = 0;
				WindowInfoScr.gI().listCheDo = null;
				this.autoPhase = 1;
				break;
			}
			case 11:
			{
				if (this.autoJob == 1)
				{
					ItemInInventory itemInInventory = this.autoNewItem();
					if (itemInInventory != null)
					{
						++this.autoTries;
						this.autoTargets[this.autoTargetPos] = itemInInventory.itemID;
						this.autoPhase = 10;
						break;
					}
				}
				else if (this.autoOkSeen)
				{
					ItemInInventory itemInInventory = this.autoFindItem(this.autoTargets[this.autoTargetPos]);
					if (itemInInventory != null && itemInInventory == this.autoRefObj && this.autoWaitCount < 60)
					{
						++this.autoWaitCount;
						break;
					}
					++this.autoTries;
					if (this.autoJob == 3 && this.autoRepAny())
					{
						++this.autoFound;
						++this.autoTargetPos;
					}
					this.autoPhase = 10;
					break;
				}
				if (++this.autoWaitCount <= 250) break;
				this.autoStop("Auto " + AUTO_JOB_NAME[this.autoJob] + ": không nhận được kết quả từ server, đã dừng.");
				return;
			}
			case 4:
			{
				bool bl;
				ItemInInventory itemInInventory = this.autoNewItem();
				if (itemInInventory == null)
				{
					if (++this.autoWaitCount <= 250) break;
					this.autoStop("Auto chế đồ: không nhận được đồ, đã dừng (có thể không đủ nguyên liệu hoặc không ở trong Thành/Làng).");
					return;
				}
				this.autoCraftedItemID = itemInInventory.itemID;
				++this.autoTries;
				bl = itemInInventory.colorName != 0 && itemInInventory.colorName != 4;
				if (!this.dongAnAnyChosen() || this.autoItemMatches(itemInInventory))
				{
					if (this.dongAnAnyChosen())
					{
						++this.autoFound;
					}
					++this.autoSlot;
					this.autoPhase = 0;
					break;
				}
				if (this.autoMode == 1 && bl && this.autoLevel < (this.autoSlot == 0 ? 51 : 50))
				{
					this.autoLevel += 5;
					this.autoPhase = 5;
					break;
				}
				if (Canvas.gameScr.isOffLineMap(Canvas.gameScr.map))
				{
					this.autoSellQuiet(this.autoCraftedItemID);
					++this.autoSold;
				}
				this.autoPhase = 0;
				this.autoWaitTicks = 2;
				break;
			}
			case 109:
			{
				// vòng mới: kiểm tra pet trước khi chế
				// NPC thợ săn chỉ nâng cấp khi cấp < 40. Từ cấp 40 pet (đã lên cấp 5) vẫn luyện tiếp
				// tới 49.99% - lúc đó server không cho ăn nữa, phải tiến hoá bằng Huyết linh thảo
				if (PauseMenu.petCanTienHoa() || PauseMenu.petCap() >= 40 && (this.autoPetBaoNang || this.autoPetCanNang))
				{
					this.autoStop("Auto nuôi pet: pet đã đạt " + PauseMenu.petCap() + "+99.9%, cần tiến hoá.");
					return;
				}
				// cần nâng cấp: đọc từ thông tin pet, từ câu báo "đã có thể nâng cấp", hoặc từ việc
				// server từ chối cho ăn "Không thể cho ăn khi chưa nâng cấp" (lỡ hai cách kia trượt)
				if (PauseMenu.petCanNang() || this.autoPetBaoNang || this.autoPetCanNang)
				{
					this.autoPhase = 130;
					break;
				}
				// món chế từ trước chưa ăn (sức ăn đầy / cần nâng cấp giữa lô) -> ăn trước
				if (this.petConLai() > 0)
				{
					this.autoPhase = 125;
					break;
				}
				int n2 = PauseMenu.petSucAn();
				// sau khi đăng nhập lại sức ăn hiển thị vẫn là số cũ - server chỉ trừ ở lần ăn đầu
				if (!this.autoPetSauRelog && n2 >= 0 && n2 + this.petAnTon() > 300)
				{
					this.autoPhase = 150;
					break;
				}
				if (PauseMenu.autoBagFull())
				{
					this.autoStop("Auto nuôi pet: hành trang đầy, đã dừng.");
					return;
				}
				if (this.autoPetTuHon)
				{
					this.autoPhase = 160;
					break;
				}
				if (this.autoPetCoCongThuc)
				{
					this.autoPhase = 115;
					break;
				}
				this.autoSlot = this.autoSlot >= AUTO_SLOT_NAME.Length ? 0 : this.autoSlot;
				this.autoPhase = 0;
				break;
			}
			case 110:
			{
				// món đầu tiên (chế thường) đã về chưa - snapshot ở phase 7
				if (this.petMonMoi() == 0)
				{
					if (++this.autoWaitCount <= 250) break;
					this.autoStop("Auto nuôi pet: chế đồ không ra món nào (thiếu xu? cần 5000 xu/món), đã dừng.");
					return;
				}
				++this.autoTries;
				// danh sách đồ ăn đã xin ngay sau lệnh chế (phase 7) -> cho ăn luôn
				this.autoPhase = 126;
				this.autoWaitCount = 0;
				break;
			}
			case 115:
			{
				// chế theo công thức (không chờ server mở cửa sổ), xin luôn danh sách đồ ăn
				int n = this.petCheDuoc(PET_LO_TOI_DA);
				if (n < 1)
				{
					// không đủ cho công thức này -> quay lại chế thường (tự thử món khác / báo lỗi)
					this.autoPetCoCongThuc = false;
					this.autoPhase = 109;
					break;
				}
				this.petGuiChe(n);
				this.autoPetLoCho = n;
				this.batDauDs();
				this.autoPetDay = false;
				this.autoPetCanNang = false;
				this.xinDsAn();
				this.autoPhase = 116;
				this.autoWaitCount = 0;
				break;
			}
			case 116:
			{
				// chờ cả lô về (hoặc hết giờ mà đã có ít nhất 1 món)
				int n = this.petMonMoi();
				if (n < this.autoPetLoCho && ++this.autoWaitCount <= 300) break;
				if (n == 0)
				{
					this.autoPetCoCongThuc = false;
					this.autoPhase = 109;
					break;
				}
				this.autoTries += n;
				this.autoPhase = 126;
				this.autoWaitCount = 0;
				break;
			}
			case 125:
			{
				this.batDauDs();
				this.autoPetDsAn = null;
				this.autoPetDay = false;
				this.autoPetCanNang = false;
				this.xinDsAn();
				this.autoPhase = 126;
				this.autoWaitCount = 0;
				break;
			}
			case 126:
			{
				if (!this.dsSan())
				{
					if (++this.autoWaitCount <= 400) break;
					if (!this.autoPetXinLai)
					{
						// server bận (chế đồ ghi log lâu) - xin lại danh sách 1 lần rồi mới bỏ cuộc
						this.autoPetXinLai = true;
						this.autoPhase = 125;
						break;
					}
					this.autoStop("Auto nuôi pet: NPC thợ săn không trả lời (phải đứng gần NPC thợ săn trong Thành), đã dừng.");
					return;
				}
				this.autoPetXinLai = false;
				if (this.petConLai() == 0)
				{
					this.autoPhase = 109;
					break;
				}
				int n = this.petTinhLo();
				if (n < 0)
				{
					this.autoStop("Auto nuôi pet: trong hành trang có đồ trùng tên món chế nhưng cấp cao hơn 21 (hoặc danh sách đồ ăn lệch), đã dừng để tránh pet ăn nhầm đồ.");
					return;
				}
				// chụp id lô SAU petTinhLo (có thể vừa gộp thêm món cùng tên cấp 20/21)
				this.autoPetLoTruocIds = new short[this.autoPetLo.size()];
				for (int i = 0; i < this.autoPetLoTruocIds.Length; ++i)
				{
					this.autoPetLoTruocIds[i] = (short)(int)this.autoPetLo.elementAt(i);
				}
				this.autoPetLoCho = n % 256;
				// lượt cho ăn mới: chỉ chờ các danh sách xin từ đây
				this.autoPetDsCho = this.autoPetDsNhan;
				// vừa ăn vừa chế. THỨ TỰ QUAN TRỌNG: CHẾ món sau TRƯỚC rồi mới CHO ĂN món này.
				// Server tái dùng id món đồ: ăn xong id X được trả lại, lệnh chế đi SAU sẽ lấy
				// đúng X -> client tưởng món chưa bị ăn và món mới chưa về. Chế trước thì lúc đó
				// X còn đang dùng nên món mới mang id khác. Vị trí cho ăn vẫn đúng: danh sách đồ ăn
				// trên server tạo từ lần xin trước, món mới chỉ thêm vào cuối hành trang.
				this.autoPetCheTheo = 0;
				if (this.autoPetCoCongThuc && !this.autoPetDay && !this.autoPetCanNang && !this.autoPetBaoNang && !PauseMenu.petCanNang())
				{
					int n2 = this.petCheDuoc(PET_LO_TOI_DA);
					if (n2 > 0)
					{
						this.petGuiChe(n2);
						this.autoPetCheTheo = n2;
					}
				}
				this.petGuiAn(n);
				if (this.autoPetCheTheo > 0)
				{
					// danh sách mới: đã bỏ món vừa ăn, đã có món vừa chế
					this.xinDsAn();
				}
				this.autoPhase = 127;
				this.autoWaitCount = 0;
				break;
			}
			case 127:
			{
				// chờ pet ăn xong nhóm vừa gửi và món chế nối đuôi về; đầy / cần nâng cấp thì server từ chối phần còn lại
				int n = 0;
				for (int i = 0; i < this.autoPetLoTruocIds.Length; ++i)
				{
					if (this.autoFindItem(this.autoPetLoTruocIds[i]) != null) continue;
					++n;
				}
				int n2 = this.autoPetCheTheo > 0 ? this.petMonMoi() : 0;
				bool bl = this.autoPetDay || this.autoPetCanNang || this.autoPetBaoNang;
				bool bl2 = n < this.autoPetLoCho || this.autoPetCheTheo > 0 && n2 < this.autoPetCheTheo;
				if (bl2 && ++this.autoWaitCount <= (bl ? 40 : 400)) break;
				this.petConLai();
				this.autoPetDaAn += n;
				this.autoTries += n2;
				if (n > 0)
				{
					this.autoPetSauRelog = false;
				}
				if (this.autoPetCheTheo > 0 && n2 == 0)
				{
					// món nối đuôi không ra (hết xu/nguyên liệu...) -> 109 chế thường để báo đúng lỗi
					this.autoPetCoCongThuc = false;
				}
				if (this.autoPetDay && n < this.autoPetLoCho)
				{
					if (this.autoPetSauRelog)
					{
						this.autoStop("Auto nuôi pet: vừa đăng nhập lại mà sức ăn vẫn đầy (" + PauseMenu.petSucAn() + "/300), đã dừng.");
						return;
					}
					this.autoPhase = 150;
					break;
				}
				if (n == 0 && !bl)
				{
					this.autoStop("Auto nuôi pet: cho ăn không có phản hồi, đã dừng.");
					return;
				}
				if (bl || this.autoPetLo.size() == 0)
				{
					// cần nâng cấp / hết món: 109 lo nâng cấp rồi cho ăn nốt món còn lại
					this.autoPhase = 109;
					break;
				}
				// còn món (món nối đuôi vừa về): danh sách mới đã xin sau lệnh chế -> cho ăn luôn
				this.autoPhase = this.autoPetCheTheo > 0 ? 126 : 125;
				this.autoWaitCount = 0;
				break;
			}
			case 190:
			{
				// món vừa chế đã về chưa (snapshot ở phase 7)
				ItemInInventory itemInInventory = this.autoNewItem();
				if (itemInInventory == null)
				{
					if (++this.autoWaitCount <= 250) break;
					this.autoStop("Auto nghiền bột: chế đồ không ra món nào (cần 5000 xu/món chế + 2000 xu/lần nghiền), đã dừng.");
					return;
				}
				++this.autoNbChe;
				if (itemInInventory.colorName == 0)
				{
					// đồ trắng không nghiền được -> bán
					this.autoSellQuiet(itemInInventory.itemID);
					++this.autoNbBan;
				}
				this.autoPhase = 193;
				this.autoWaitCount = 0;
				break;
			}
			case 193:
			{
				// hàng đợi đủ thì chờ nghiền bớt rồi mới chế tiếp (khỏi đầy hành trang)
				int n0 = PauseMenu.nbDemNghien();
				if (n0 >= NB_HANG_DOI_TOI_DA) break;
				if (PauseMenu.autoBagFull())
				{
					// đầy: chờ nghiền bớt; không còn gì để nghiền mà vẫn đầy thì dừng
					if (n0 > 0 || this.autoNbCo) break;
					int n = 0;
					mVector vector = Char.inventory;
					for (int i = 0; vector != null && i < vector.size(); ++i)
					{
						if (!PauseMenu.nbNghienDuoc((ItemInInventory)vector.elementAt(i))) continue;
						++n;
					}
					this.autoStop("Auto nghiền bột: hành trang đầy, nghiền được " + this.autoFound + " món (trong túi " + (vector == null ? 0 : vector.size()) + " món, " + n + " món cấp 20/21 có màu; map " + Canvas.gameScr.map + ", gửi " + this.autoNbGui + ").");
					return;
				}
				this.autoPhase = 0;
				break;
			}
			case 194:
			{
				// hết nguyên liệu: nghiền nốt đồ còn trong túi rồi dừng
				if (PauseMenu.nbDemNghien() > 0 || this.autoNbCo) break;
				this.autoStop("Auto nghiền bột: " + (PauseMenu.autoBagFull() ? "hành trang đầy" : "đã hết nguyên liệu") + ", nghiền được " + this.autoFound + " món.");
				return;
			}
			case 180:
			{
				ItemInInventory itemInInventory = PauseMenu.hklMon();
				if (itemInInventory == null)
				{
					// vừa lên cấp: món cũ mất, món mới đang về
					if (++this.autoWaitCount <= 100) break;
					this.autoStop("Auto nâng cấp Hỏa kỳ lân: không thấy Hỏa kỳ lân trong hành trang, đã dừng.");
					return;
				}
				if (itemInInventory.idTemplate >= HKL_CAP_TOI_DA)
				{
					this.autoStop("Auto nâng cấp Hỏa kỳ lân: đã nâng cấp tối đa (cấp 3).");
					return;
				}
				int n = PauseMenu.hklDa(HOA_THACH_C2);
				int n2 = 1;
				if (n < 1)
				{
					n = PauseMenu.hklDa(HOA_THACH_C1);
					n2 = 0;
				}
				if (n < 1)
				{
					if (!this.autoHklMua)
					{
						this.autoStop("Auto nâng cấp Hỏa kỳ lân: hết Hỏa thạch, đã dừng.");
						return;
					}
					// Hỏa thạch chỉ mua được trong nhà
					if (!Canvas.gameScr.isOffLineMap(Canvas.gameScr.map))
					{
						this.autoAfterHouse = 183;
						this.autoPhase = 20;
						this.autoWaitCount = 0;
						break;
					}
					this.autoPhase = 183;
					break;
				}
				// gửi liền tối đa HKL_LO lần nâng; lên cấp giữa chừng thì món cũ biến mất nên các
				// lần sau server tự bỏ qua (Char.doNangCapHoaKyLan kiểm món còn trong túi), không tốn đá
				int n3 = Math.min(n, HKL_LO);
				this.autoHklKqTruoc = this.autoHklOk + this.autoHklHong;
				this.autoHklGui = n3;
				for (int i = 0; i < n3; ++i)
				{
					GameService.gI().useItem(itemInInventory.itemID);
					GameService.gI().doMenuOption(-513, (sbyte)0, 1);
					GameService.gI().doMenuOption(-513, (sbyte)1, n2);
				}
				this.autoPhase = 181;
				this.autoWaitCount = 0;
				break;
			}
			case 181:
			{
				int n = this.autoHklOk + this.autoHklHong - this.autoHklKqTruoc;
				bool bl = this.autoHklOk > 0 && this.autoTries < this.autoHklOk;
				if (n < this.autoHklGui && !bl && ++this.autoWaitCount <= 300) break;
				if (n == 0 && !bl)
				{
					this.autoStop("Auto nâng cấp Hỏa kỳ lân: không có phản hồi (Hỏa kỳ lân có thời hạn không nâng được), đã dừng.");
					return;
				}
				this.autoTries = this.autoHklOk;
				this.autoFound += n;
				this.autoPhase = 180;
				this.autoWaitCount = 0;
				this.autoWaitTicks = bl ? 8 : 1;
				break;
			}
			case 183:
			{
				// mua Hỏa thạch cấp 1 (đang ở trong nhà), tối đa 300 viên/gói
				if (!Canvas.gameScr.isOffLineMap(Canvas.gameScr.map))
				{
					this.autoAfterHouse = 183;
					this.autoPhase = 20;
					break;
				}
				this.autoHklMuaTruoc = PauseMenu.hklDa(HOA_THACH_C1);
				mVector vector = new mVector();
				GemTemp gemTemp = new GemTemp();
				gemTemp.id = (short)HOA_THACH_C1;
				gemTemp.number = (short)HKL_MUA_LO;
				vector.addElement(gemTemp);
				GameService.gI().buyGemItem(vector);
				this.autoPhase = 184;
				this.autoWaitCount = 0;
				break;
			}
			case 184:
			{
				int n = PauseMenu.hklDa(HOA_THACH_C1) - this.autoHklMuaTruoc;
				if (n <= 0)
				{
					if (++this.autoWaitCount <= 200) break;
					this.autoStop("Auto nâng cấp Hỏa kỳ lân: không mua được Hỏa thạch cấp 1 (cần 10 lượng/viên), đã dừng.");
					return;
				}
				this.autoHklDaMua += n;
				this.autoPhase = 180;
				this.autoWaitCount = 0;
				break;
			}
			case 170:
			{
				// học skill pet: quyết định học tiếp hay xoá skill (Huyết Bồ Đề)
				mVector vector = PauseMenu.skDangCo();
				int n4 = this.skDanhGia(vector);
				if (n4 == 1)
				{
					this.autoStop("Auto học skill pet: đã học được " + this.skMucTieu());
					return;
				}
				int n = PauseMenu.skPhamChat();
				int n2 = Math.max(0, PauseMenu.petDocSo(PauseMenu.petInfo(), "Kỹ năng có thể học:"));
				int n3 = vector.size();
				// học tiếp khi: chưa có skill sai, phẩm chất >= mức chọn, còn slot trống; slot 1 cần
				// phẩm chất >= 1, slot 2 cần >= 2 (tỉ lệ 0% ở dưới). Học slot 2 mà pet chỉ có 1 slot
				// thì cũng phải xoá (random lại phẩm chất để có 2 slot).
				bool bl = n4 == 0 && n >= this.autoSkPhamChat && n3 < n2 && (n3 == 0 ? n >= 1 : n >= 2) && (!this.autoSkSlot2 || n2 >= 2 && n >= 2);
				if (bl)
				{
					if (PauseMenu.skSach() <= 0)
					{
						this.autoStop("Auto học skill pet: hết Sách kỹ năng pet (đã dùng " + this.autoSkSach + ").");
						return;
					}
					if (mSystem.currentTimeMillis() < this.autoSkLuc) break;
					this.autoSkKq = 0;
					this.autoSkLuc = mSystem.currentTimeMillis() + 300L;
					GameService.gI().usePotion(POTION_SACH_SKILL);
					++this.autoSkSach;
					this.autoPhase = 171;
					this.autoWaitCount = 0;
					break;
				}
				ItemInInventory itemInInventory = PauseMenu.skHbd();
				if (itemInInventory == null && this.autoSkMua)
				{
					// hết Huyết Bồ Đề: mua 1 viên ở cửa hàng đặc biệt rồi dùng luôn
					this.autoSkMuaTruoc = PauseMenu.skHbdCo();
					GameService.gI().buyItemShopSpecial(SHOP_HUYET_BO_DE);
					this.autoPhase = 173;
					this.autoWaitCount = 0;
					break;
				}
				if (itemInInventory == null)
				{
					this.autoStop("Auto học skill pet: hết Huyết Bồ Đề để xoá skill / đổi phẩm chất (đã dùng " + this.autoSkHbd + ").");
					return;
				}
				this.autoSkReset = false;
				GameService.gI().useItem(itemInInventory.itemID);
				++this.autoSkHbd;
				this.autoPhase = 172;
				this.autoWaitCount = 0;
				break;
			}
			case 171:
			{
				if (this.autoSkKq == 0)
				{
					if (++this.autoWaitCount <= 200) break;
					this.autoStop("Auto học skill pet: dùng sách không có phản hồi, đã dừng.");
					return;
				}
				++this.autoTries;
				// chờ thông tin pet mới rồi mới quyết định tiếp
				this.autoPhase = 170;
				this.autoWaitTicks = 4;
				break;
			}
			case 173:
			{
				// chờ Huyết Bồ Đề vừa mua vào hành trang (không mua được thì server báo lý do và auto dừng)
				if (PauseMenu.skHbdCo() <= this.autoSkMuaTruoc)
				{
					if (++this.autoWaitCount <= 200) break;
					this.autoStop("Auto học skill pet: không mua được Huyết Bồ Đề (cần 10 lượng/viên và hành trang còn chỗ), đã dừng.");
					return;
				}
				++this.autoSkDaMua;
				this.autoPhase = 170;
				break;
			}
			case 172:
			{
				if (!this.autoSkReset)
				{
					if (++this.autoWaitCount <= 200) break;
					this.autoStop("Auto học skill pet: dùng Huyết Bồ Đề không có phản hồi (pet phải ở trạng thái 2 trở lên), đã dừng.");
					return;
				}
				this.autoPhase = 170;
				this.autoWaitTicks = 4;
				break;
			}
			case 160:
			{
				// ăn bằng Tụ hồn đan: xin danh sách, Tụ hồn đan luôn là dòng cuối
				int n = PauseMenu.tuHonDanCo();
				if (n <= 0)
				{
					this.autoStop("Auto nuôi pet: đã hết Tụ hồn đan (đã ăn " + this.autoPetDaAn + ").");
					return;
				}
				int n2 = PauseMenu.petSucAn();
				int n3 = Math.min(Math.min(n, PET_LO_TU_HON), this.autoPetSauRelog ? 300 : 300 - Math.max(0, n2));
				if (n3 < 1)
				{
					this.autoPhase = 150;
					break;
				}
				this.autoPetLoCho = n3;
				this.autoPetTuTruoc = n;
				this.batDauDs();
				this.autoPetDay = false;
				this.autoPetCanNang = false;
				this.xinDsAn();
				this.autoPhase = 161;
				this.autoWaitCount = 0;
				break;
			}
			case 161:
			{
				if (!this.dsSan())
				{
					if (++this.autoWaitCount <= 400) break;
					this.autoStop("Auto nuôi pet: NPC thợ săn không trả lời (phải đứng gần NPC thợ săn trong Thành), đã dừng.");
					return;
				}
				int n = this.autoPetDsAn.Length - 1;
				// dòng cuối phải đúng là Tụ hồn đan; số lệnh <= số viên đang có nên dòng cuối
				// không đổi suốt lượt (không chế đồ, hành trang không thay đổi)
				if (n < 0 || n > 127 || !this.autoPetDsAn[n].StartsWith("Tụ hồn đan", System.StringComparison.Ordinal))
				{
					this.autoStop("Auto nuôi pet: không thấy Tụ hồn đan trong danh sách cho ăn, đã dừng.");
					return;
				}
				this.petGuiAn(n * 256 + this.autoPetLoCho);
				this.autoPhase = 162;
				this.autoWaitCount = 0;
				break;
			}
			case 162:
			{
				int n = this.autoPetTuTruoc - PauseMenu.tuHonDanCo();
				bool bl = this.autoPetDay || this.autoPetCanNang || this.autoPetBaoNang;
				if (n < this.autoPetLoCho && ++this.autoWaitCount <= (bl ? 40 : 400)) break;
				this.autoPetDaAn += Math.max(0, n);
				if (n > 0)
				{
					this.autoPetSauRelog = false;
				}
				if (this.autoPetDay && n < this.autoPetLoCho)
				{
					if (this.autoPetSauRelog)
					{
						this.autoStop("Auto nuôi pet: vừa đăng nhập lại mà sức ăn vẫn đầy (" + PauseMenu.petSucAn() + "/300), đã dừng.");
						return;
					}
					this.autoPhase = 150;
					break;
				}
				if (n <= 0 && !bl)
				{
					this.autoStop("Auto nuôi pet: cho ăn Tụ hồn đan không có phản hồi, đã dừng.");
					return;
				}
				this.autoPhase = 109;
				break;
			}
			case 130:
			{
				// nâng cấp: ưu tiên LKD thấp nhất đang có, hết LKD thì nâng không dùng LKD
				int n = 2;
				for (int i = 0; i < PET_LKD_ID.Length; ++i)
				{
					if (PauseMenu.autoGemCountLock(PET_LKD_ID[i], false) + PauseMenu.autoGemCountLock(PET_LKD_ID[i], true) < 1) continue;
					n = PET_LKD_MUC[i];
					break;
				}
				this.autoPetUpKq = 0;
				GameService.gI().doMenuOption(-16, (sbyte)0, n);
				this.autoPhase = 131;
				this.autoWaitCount = 0;
				break;
			}
			case 131:
			{
				if (this.autoPetUpKq == 0)
				{
					if (++this.autoWaitCount <= 200) break;
					this.autoStop("Auto nuôi pet: nâng cấp pet không có phản hồi (phải đứng gần NPC thợ săn trong Thành), đã dừng.");
					return;
				}
				if (this.autoPetUpKq == 2)
				{
					this.autoPhase = 130;
					this.autoWaitTicks = 2;
					break;
				}
				if (this.autoPetUpKq == 1)
				{
					this.autoPetBaoNang = false;
					this.autoPetCanNang = false;
					++this.autoPetDaNang;
					this.autoPetUpKq = 3;
					this.autoWaitCount = 0;
				}
				// chờ thông tin pet mới (đã qua 99.9%) rồi mới chế/cho ăn tiếp
				if (PauseMenu.petCanNang() && ++this.autoWaitCount <= 260) break;
				this.autoPhase = 109;
				this.autoWaitTicks = 2;
				break;
			}
			case 140:
			{
				// điều phối đường lên Thành
				if (!PauseMenu.oTrongGame())
				{
					break;
				}
				if (PauseMenu.oMap(MAP_THANH))
				{
					// NPC thợ săn ở ô (25,10), server nhận trong bán kính 5 ô
					this.datDich(22, 7, 28, 13);
					this.autoPhase = 145;
					break;
				}
				if (PauseMenu.oMap(MAP_CONG_THANH))
				{
					// đích = ô trống sát cổng lên Thành, lấy từ dữ liệu bản đồ của client
					bool[] blArray = PauseMenu.dichSatCong(MAP_THANH);
					if (blArray == null)
					{
						this.autoStop("Auto nuôi pet: map này không thấy cổng lên Thành, đã dừng.");
						return;
					}
					this.datDich(blArray);
					this.autoPhase = 143;
					break;
				}
				XaphuTemplate xaphuTemplate = Res.getXaphu();
				int n = -1;
				int n2 = -1;
				for (int i = 0; xaphuTemplate != null && i < XE_NGUA_DICH.Length && n < 0; ++i)
				{
					for (int j = 0; j < xaphuTemplate.mapID.Length; ++j)
					{
						if (xaphuTemplate.mapID[j] != XE_NGUA_DICH[i]) continue;
						n = j;
						n2 = XE_NGUA_DICH[i];
						break;
					}
				}
				if (n < 0)
				{
					this.autoStop("Auto nuôi pet: map này không có xe ngựa đi lên Thành (hãy về Làng), đã dừng.");
					return;
				}
				if ((long)xaphuTemplate.price[n] > Canvas.gameScr.mainChar.charXu)
				{
					this.autoStop("Auto nuôi pet: không đủ xu đi xe ngựa, đã dừng.");
					return;
				}
				short s = Canvas.gameScr.getIndexArrMap(n2);
				if (s < 0 || Res.localXaphu == null || s >= Res.localXaphu.Length || Res.localXaphu[s] == null)
				{
					this.autoStop("Auto nuôi pet: không đọc được điểm đến của xe ngựa, đã dừng.");
					return;
				}
				this.autoLenMap = Canvas.gameScr.map;
				GameService.gI().doMove2Map((sbyte)n, Res.localXaphu[s][0]);
				ChangScr.gI().switchToMe();
				ChangScr.gI().setMap(Canvas.gameScr.map, 0, 0);
				Res.removeMonsterTemplet();
				this.autoPhase = 141;
				this.autoWaitCount = 0;
				break;
			}
			case 141:
			{
				if (PauseMenu.oTrongGame() && Canvas.gameScr.map != this.autoLenMap)
				{
					this.autoPhase = 140;
					this.autoWaitTicks = 15;
					break;
				}
				if (++this.autoWaitCount <= 400) break;
				this.autoStop("Auto nuôi pet: đi xe ngựa không được, đã dừng.");
				return;
			}
			case 143:
			{
				int n = this.diBo();
				if (n < 0)
				{
					this.autoStop("Auto nuôi pet: không tìm được đường tới cổng lên Thành (đang ở ô " + Canvas.gameScr.mainChar.x / 16 + "," + Canvas.gameScr.mainChar.y / 16 + " map " + Canvas.gameScr.map + "), đã dừng.");
					return;
				}
				if (n == 0) break;
				CMLocation cMLocation = PauseMenu.congToi(MAP_THANH);
				if (cMLocation == null)
				{
					this.autoStop("Auto nuôi pet: map này không thấy cổng lên Thành, đã dừng.");
					return;
				}
				this.autoLenMap = Canvas.gameScr.map;
				PauseMenu.quaCong(cMLocation, MAP_THANH);
				this.autoPhase = 144;
				this.autoWaitCount = 0;
				break;
			}
			case 144:
			{
				if (PauseMenu.oTrongGame() && PauseMenu.oMap(MAP_THANH))
				{
					this.autoPhase = 140;
					this.autoWaitTicks = 15;
					break;
				}
				if (++this.autoWaitCount <= 400) break;
				this.autoStop("Auto nuôi pet: không vào được Thành (đang công thành?), đã dừng.");
				return;
			}
			case 145:
			{
				int n = this.diBo();
				if (n < 0)
				{
					this.autoStop("Auto nuôi pet: không tìm được đường tới NPC thợ săn (đang ở ô " + Canvas.gameScr.mainChar.x / 16 + "," + Canvas.gameScr.mainChar.y / 16 + "), đã dừng.");
					return;
				}
				if (n == 0) break;
				this.autoPhase = 109;
				this.autoWaitTicks = 4;
				break;
			}
			case 150:
			{
				this.batDauRelog();
				break;
			}
			case 151:
			{
				// chạy lại khi đã vào game (luồng relog xong); lỗi thì dừng
				if (this.autoRelogLoi != null)
				{
					this.autoStop("Auto nuôi pet: " + this.autoRelogLoi + ", đã dừng.");
					return;
				}
				if (!PauseMenu.oTrongGame()) break;
				this.autoPetSauRelog = true;
				this.autoPhase = 140;
				this.autoWaitTicks = 30;
				break;
			}
			case 97:
			{
				ItemInInventory itemInInventory = this.autoFindItem(this.autoKtItem);
				if (itemInInventory == null || this.ktDat(itemInInventory))
				{
					if (this.autoKtFull)
					{
						if (this.autoKtNext()) break;
						this.autoStop(this.ktTongKet());
						return;
					}
					if (itemInInventory == null)
					{
						this.autoStop("Auto khóa đồ thú: không thấy món đồ trong hành trang, đã dừng.");
					}
					else
					{
						this.autoStop("Auto khóa đồ thú: đã khóa được " + PauseMenu.ktGiaTri(itemInInventory) + "%");
					}
					return;
				}
				// hết loại xương đang dùng (thường/khóa) thì chuyển sang loại còn lại cùng id
				if (PauseMenu.autoGemCountLock(this.autoKtDa, this.autoKtLock) < 1)
				{
					this.autoKtLock = !this.autoKtLock;
					if (PauseMenu.autoGemCountLock(this.autoKtDa, this.autoKtLock) < 1)
					{
						this.autoStop(this.autoKtFull ? this.ktTongKet() + " - hết xương cấp 6" : "Auto khóa đồ thú: hết xương cấp 6 sau " + this.autoTries + " lần, đã dừng.");
						return;
					}
				}
				this.autoOkSeen = false;
				this.autoCttBoQua = false;
				this.autoKtDaTruoc = PauseMenu.autoGemCountLock(this.autoKtDa, this.autoKtLock);
				GameService.gI().doRequestTachNguyenLIeu(this.autoKtItem, 2, this.autoKtDa, this.autoKtLock ? 1 : 0);
				this.autoPhase = 98;
				this.autoWaitCount = 0;
				break;
			}
			case 98:
			{
				if (this.autoCttBoQua)
				{
					if (this.autoKtNext()) break;
					this.autoStop(this.ktTongKet());
					return;
				}
				if (!this.autoOkSeen)
				{
					if (++this.autoWaitCount <= 200) break;
					this.autoStop("Auto khóa đồ thú: server không phản hồi (đồ thú phải Hoàn mỹ Nhất phẩm và đứng trong nhà Hắc Ngưu), đã dừng.");
					return;
				}
				// "Đã khóa thành công" tới TRƯỚC chỉ số mới. Server gửi tiếp chỉ số món đồ rồi mới
				// gửi lại túi nguyên liệu, nên thấy xương giảm là chỉ số đã về (client sửa chỉ số
				// ngay trên object cũ nên không dùng so sánh object được - chờ hết 60 tick mỗi lần)
				if (PauseMenu.autoGemCountLock(this.autoKtDa, this.autoKtLock) >= this.autoKtDaTruoc && ++this.autoWaitCount < 60) break;
				ItemInInventory itemInInventory = this.autoFindItem(this.autoKtItem);
				++this.autoTries;
				if (this.ktDat(itemInInventory))
				{
					++this.autoFound;
					if (this.autoKtFull)
					{
						if (this.autoKtNext())
						{
							this.autoWaitTicks = 1;
							break;
						}
						this.autoStop(this.ktTongKet());
						return;
					}
					this.autoStop("Auto khóa đồ thú: đã khóa được " + PauseMenu.ktGiaTri(itemInInventory) + "%");
					return;
				}
				this.autoPhase = 97;
				this.autoWaitTicks = 1;
				break;
			}
			case 95:
			{
				ItemInInventory itemInInventory = this.autoFindItem(this.autoCttItem);
				if (itemInInventory == null || this.autoCttFull && this.cttDatSan(itemInInventory))
				{
					if (this.autoCttFull)
					{
						if (this.autoCttNext()) break;
						this.autoStop(this.cttTongKet() + ". Đã hết đồ.");
						return;
					}
					this.autoStop("Auto cộng thuộc tính: không thấy món đồ trong hành trang, đã dừng.");
					return;
				}
				// hết loại đá đang dùng (thường/khóa) thì chuyển sang loại còn lại cùng id
				if (PauseMenu.autoGemCountLock(this.autoCttDa, this.autoCttLock) < 1)
				{
					this.autoCttLock = !this.autoCttLock;
					if (PauseMenu.autoGemCountLock(this.autoCttDa, this.autoCttLock) < 1)
					{
						this.autoStop(this.autoCttFull ? this.cttTongKet() + ". Hết đá thuộc tính, đã dừng." : "Auto cộng thuộc tính: hết đá thuộc tính sau " + this.autoTries + " lần" + (this.autoCttVuaRa == null ? "" : " (lần cuối ra " + this.autoCttVuaRa + ")") + ", đã dừng.");
						return;
					}
				}
				this.autoCttOk = false;
				this.autoCttBoQua = false;
				this.autoCttVuaRa = null;
				GameService.gI().doRequestTachNguyenLIeu(this.autoCttItem, 1, this.autoCttDa, this.autoCttLock ? 1 : 0);
				this.autoPhase = 96;
				this.autoWaitCount = 0;
				break;
			}
			case 96:
			{
				if (this.autoCttBoQua)
				{
					// server không cho cộng món này (đồ thời trang...) -> bỏ qua, sang món khác
					if (this.autoCttNext()) break;
					this.autoStop(this.cttTongKet() + ". Đã hết đồ.");
					return;
				}
				if (!this.autoCttOk)
				{
					if (++this.autoWaitCount <= 200) break;
					this.autoStop("Auto cộng thuộc tính: server không phản hồi (đồ phải màu xanh lá và đứng trong nhà Hắc Ngưu), đã dừng.");
					return;
				}
				++this.autoTries;
				if (this.cttDat(this.autoCttVuaRa))
				{
					++this.autoFound;
					if (this.autoCttFull)
					{
						if (this.autoCttNext())
						{
							this.autoWaitTicks = 1;
							break;
						}
						this.autoStop(this.cttTongKet() + ". Tất cả đồ đã đạt!");
						return;
					}
					this.autoStop("Auto cộng thuộc tính: đã ra " + this.autoCttVuaRa);
					return;
				}
				this.autoPhase = 95;
				this.autoWaitTicks = 1;
				break;
			}
			case 90:
			{
				ItemInInventory itemInInventory = this.autoFindItem(this.autoThemItem);
				if (itemInInventory == null)
				{
					if (this.autoThemFull && this.autoThemNext())
					{
						break;
					}
					this.autoStop(this.autoThemFull ? "Auto thêm dòng full: đã xong tất cả đồ." : "Auto thêm dòng: không thấy món đồ trong hành trang, đã dừng.");
					return;
				}
				this.autoThemBotLock = this.autoThemStock(249, true) >= 1 ? 1 : 0;
				if (this.autoThemStock(249, this.autoThemBotLock == 1) < 1)
				{
					this.autoStop("Auto thêm dòng: hết bột xanh lá (bột không bán ở shop), đã dừng.");
					return;
				}
				this.autoThemDaLock = this.autoThemStock(250, true) >= 1 ? 1 : 0;
				if (this.autoThemStock(250, this.autoThemDaLock == 1) < 1)
				{
					if (this.autoThemBuy && Canvas.gameScr.isOffLineMap(Canvas.gameScr.map))
					{
						mVector vector = new mVector();
						GemTemp gemTemp = new GemTemp();
						gemTemp.id = (short)250;
						gemTemp.number = (short)20;
						vector.addElement(gemTemp);
						GameService.gI().buyGemItem(vector);
						this.autoPhase = 92;
						this.autoWaitCount = 0;
						break;
					}
					this.autoStop("Auto thêm dòng: hết đá tiến giai, đã dừng.");
					return;
				}
				this.autoThemOk = false;
				GameService.gI().doRequestThemThuocTinh(this.autoThemItem, 5, 249, this.autoThemBotLock, 250, this.autoThemDaLock);
				this.autoPhase = 91;
				this.autoWaitCount = 0;
				break;
			}
			case 91:
			{
				if (!this.autoThemOk)
				{
					if (++this.autoWaitCount <= 200) break;
					this.autoStop("Auto thêm dòng: server không phản hồi (đồ phải Hoàn mỹ Nhất phẩm, đã khóa, lv50 trở lên, và đứng trong nhà Hắc Ngưu), đã dừng.");
					return;
				}
				++this.autoTries;
				ItemInInventory itemInInventory = this.autoFindItem(this.autoThemItem);
				if (itemInInventory != null)
				{
					bool[] blArray = new bool[2];
					int n = this.autoThemLines(itemInInventory, blArray);
					if (blArray[0])
					{
						++this.autoFound;
						if (this.autoThemFull)
						{
							if (this.autoThemNext())
							{
								this.autoWaitTicks = 1;
								break;
							}
							this.autoStop("Auto thêm dòng full: tất cả đồ đã ra dòng như ý!");
							return;
						}
						this.autoStop("Auto thêm dòng: đã ra dòng như ý sau " + this.autoTries + " lần!");
						return;
					}
				}
				this.autoPhase = 90;
				this.autoWaitTicks = 1;
				break;
			}
			case 92:
			{
				int n = this.autoThemStock(250, false) + this.autoThemStock(250, true);
				if (n < 1 && ++this.autoWaitCount <= 100) break;
				if (n < 1)
				{
					this.autoStop("Auto thêm dòng: không mua được đá tiến giai (10 lượng/viên, có thể hết lượng), đã dừng.");
					return;
				}
				this.autoPhase = 90;
				break;
			}
			case 80:
			{
				if (this.autoTargetPos >= this.autoTargets.Length)
				{
					this.autoStop("Auto luyện đồ: đã đập xong tất cả đồ.");
					return;
				}
				ItemInInventory itemInInventory = this.autoFindItem(this.autoTargets[this.autoTargetPos]);
				if (itemInInventory == null || itemInInventory.plusTemplate >= this.autoLuyenTarget)
				{
					if (itemInInventory != null)
					{
						++this.autoFound;
					}
					++this.autoTargetPos;
					break;
				}
				if (Canvas.gameScr.mainChar.charXu < 5000L)
				{
					this.autoStop("Auto luyện đồ: hết xu (cần 5000 xu mỗi lần đập), đã dừng.");
					return;
				}
				if (this.autoLuyenBuy && this.autoLuyenBuyCd <= 0 && Canvas.gameScr.isOffLineMap(Canvas.gameScr.map) && this.autoLuyenLow() && !this.autoLuyenShort())
				{
					this.autoLuyenBuyNow();
					this.autoLuyenBuyCd = 75;
				}
				if (this.autoLuyenShort())
				{
					if (this.autoLuyenBuy && Canvas.gameScr.isOffLineMap(Canvas.gameScr.map))
					{
						this.autoPhase = 82;
						break;
					}
					if (PauseMenu.autoGemCountLock(this.autoLkd, this.autoLkdLock) < 1)
					{
						this.autoStop(this.autoLuyenBuy ? "Auto luyện đồ: hết luyện kim dược (tự mua chỉ làm được trong nhà Hắc Ngưu), đã dừng." : "Auto luyện đồ: hết luyện kim dược, đã dừng.");
						return;
					}
				}
				mVector vector = new mVector();
				mVector vector2 = new mVector();
				vector.addElement(this.autoLkd.ToString());
				vector2.addElement(this.autoLkdLock ? "1" : "0");
				vector.addElement(this.autoBh.ToString());
				vector2.addElement(this.autoBh >= 0 && this.autoBhLock ? "1" : "0");
				vector.addElement(this.autoMm.ToString());
				vector2.addElement(this.autoMm >= 0 && this.autoMmLock ? "1" : "0");
				ScreenDapDo.itemSelect = itemInInventory;
				ScreenDapDo.itemID = itemInInventory.itemID;
				ScreenDapDo.isFinishImbue = false;
				this.autoLuyenIdle = 0;
				this.autoLuyenLastPlus = itemInInventory.plusTemplate;
				GameService.gI().doAutoImbue(itemInInventory.itemID, vector, this.autoLuyenTarget, vector2, -1);
				++this.autoTries;
				this.autoPhase = 81;
				break;
			}
			case 81:
			{
				if (!ScreenDapDo.isFinishImbue && this.autoLuyenBuy && --this.autoLuyenBuyCd <= 0 && Canvas.gameScr.isOffLineMap(Canvas.gameScr.map) && this.autoLuyenLow())
				{
					this.autoLuyenBuyNow();
					this.autoLuyenBuyCd = 75;
				}
				if (!ScreenDapDo.isFinishImbue)
				{
					ItemInInventory itemInInventory = this.autoFindItem(this.autoTargets[this.autoTargetPos]);
					int n = itemInInventory == null ? -1 : itemInInventory.plusTemplate;
					int n2 = PauseMenu.autoGemCountLock(this.autoLkd, false) + PauseMenu.autoGemCountLock(this.autoLkd, true);
					if (n != this.autoLuyenLastPlus || n2 != this.autoLuyenLastLkd)
					{
						this.autoLuyenLastPlus = n;
						this.autoLuyenLastLkd = n2;
						this.autoLuyenIdle = 0;
					}
					if (++this.autoLuyenIdle <= 3000) break;
					this.autoStop("Auto luyện đồ: server không phản hồi" + (this.autoLastAlert == null ? "" : " (" + this.autoLastAlert + ")") + ", đã dừng.");
					return;
				}
				this.autoPhase = 83;
				this.autoWaitTicks = 5;
				break;
			}
			case 83:
			{
				ItemInInventory itemInInventory = this.autoFindItem(this.autoTargets[this.autoTargetPos]);
				string @string = ScreenDapDo.gI().infoPoup;
				if (itemInInventory != null && itemInInventory.plusTemplate >= this.autoLuyenTarget)
				{
					++this.autoFound;
					++this.autoTargetPos;
					this.autoPhase = 80;
					break;
				}
				if (@string != null && @string.IndexOf("hết nguyên liệu", System.StringComparison.Ordinal) >= 0)
				{
					if (Canvas.gameScr.mainChar.charXu < 5000L)
					{
						this.autoStop("Auto luyện đồ: hết xu (cần 5000 xu mỗi lần đập), đã dừng.");
						return;
					}
					if (this.autoLuyenBuy && Canvas.gameScr.isOffLineMap(Canvas.gameScr.map))
					{
						this.autoPhase = 82;
						break;
					}
					this.autoStop("Auto luyện đồ: hết nguyên liệu, đã dừng.");
					return;
				}
				++this.autoTargetPos;
				this.autoPhase = 80;
				break;
			}
			case 82:
			{
				bool bl5 = this.autoLuyenBuyNow();
				if (!bl5 && PauseMenu.autoGemCountLock(this.autoLkd, false) < 1 && PauseMenu.autoGemCountLock(this.autoLkd, true) < 1)
				{
					this.autoStop("Auto luyện đồ: loại luyện kim dược đang dùng không bán ở shop, đã dừng.");
					return;
				}
				this.autoPhase = 84;
				this.autoWaitCount = 0;
				break;
			}
			case 84:
			{
				bool bl6 = PauseMenu.autoGemCountLock(this.autoLkd, false) >= 1;
				if (!bl6 && ++this.autoWaitCount <= 100) break;
				if (!bl6 && PauseMenu.autoGemCountLock(this.autoLkd, true) < 1)
				{
					if (this.autoLkd > 10)
					{
						this.autoStop("Auto luyện đồ: loại luyện kim dược đang dùng không bán ở shop và đã hết, đã dừng.");
					}
					else if (PauseMenu.autoBagFull())
					{
						this.autoStop("Auto luyện đồ: hành trang đầy nên không mua lại được luyện kim dược (cần 1 ô trống), đã dừng.");
					}
					else if (Canvas.gameScr.mainChar.charXu < 100000L)
					{
						this.autoStop("Auto luyện đồ: không đủ xu để mua luyện kim dược, đã dừng.");
					}
					else
					{
						this.autoStop("Auto luyện đồ: không mua được luyện kim dược" + (this.autoLastAlert == null ? "" : " (" + this.autoLastAlert + ")") + ", đã dừng.");
					}
					return;
				}
				if (this.autoLkdLock && PauseMenu.autoGemCountLock(this.autoLkd, true) < 1)
				{
					this.autoLkdLock = false;
				}
				if (this.autoBh >= 0 && this.autoBhLock && PauseMenu.autoGemCountLock(this.autoBh, true) < 1)
				{
					this.autoBhLock = false;
				}
				if (this.autoMm >= 0 && this.autoMmLock && PauseMenu.autoGemCountLock(this.autoMm, true) < (this.autoBh >= 0 ? 6 : 7))
				{
					this.autoMmLock = false;
				}
				this.autoPhase = 80;
				this.autoWaitTicks = 2;
				break;
			}
			case 70:
			{
				ItemInInventory itemInInventory = this.autoNewItem();
				if (itemInInventory == null)
				{
					if (++this.autoWaitCount <= 250) break;
					this.autoStop("Auto thần binh: không nhận được vũ khí vừa chế, đã dừng.");
					return;
				}
				if (itemInInventory.colorName != 1 || itemInInventory.hangItem < 1 || itemInInventory.hangItem > 3)
				{
					this.autoStop("Auto thần binh: vũ khí chế ra không phải Hoàn mỹ Nhất-Tam phẩm, đã dừng.");
					return;
				}
				this.autoTbItem = itemInInventory.itemID;
				this.autoShopRef = ShopNew.gI().ItemShopNew;
				GameService.gI().dobuyitemShopnew((sbyte)0, (short)7);
				this.autoPhase = 71;
				this.autoWaitCount = 0;
				break;
			}
			case 71:
			{
				mVector vector = ShopNew.gI().ItemShopNew;
				if (vector == null || vector == this.autoShopRef || ShopNew.gI().typeshop != 6)
				{
					if (++this.autoWaitCount <= 200) break;
					this.autoStop("Auto thần binh: không mở được danh sách nguyên liệu tinh luyện, đã dừng.");
					return;
				}
				int n = -1;
				for (int i = 0; i < vector.size(); ++i)
				{
					if (((ItemInInventory)vector.elementAt(i)).itemID != this.autoTbItem) continue;
					n = i;
					break;
				}
				if (n < 0)
				{
					this.autoStop("Auto thần binh: vũ khí vừa chế không có trong danh sách nguyên liệu (thần binh có thể đã 99%, hoặc chọn sai cấp thần binh), đã dừng.");
					return;
				}
				this.autoTbResult = 0;
				GameService.gI().doCustomPopup(n, (sbyte)124, "", 1);
				this.autoPhase = 72;
				this.autoWaitCount = 0;
				break;
			}
			case 72:
			{
				if (this.autoTbResult == 0)
				{
					if (++this.autoWaitCount <= 250) break;
					this.autoStop("Auto thần binh: không nhận được kết quả tinh luyện, đã dừng.");
					return;
				}
				if (Canvas.currentScreen == ShopNew.gI() && ShopNew.gI().right != null)
				{
					ShopNew.gI().right.action();
				}
				++this.autoTries;
				if (this.autoTbResult == 1)
				{
					++this.autoFound;
					if (this.autoTbPct >= 99)
					{
						this.autoStop("Auto thần binh: đã tinh luyện lên 99%, dừng lại (bước cuối lên 100% hãy tự làm bằng vũ khí cùng cấp).");
						return;
					}
				}
				this.autoPhase = 0;
				this.autoWaitTicks = 1;
				break;
			}
			case 60:
			{
				if (this.autoFound >= this.autoTkQty)
				{
					this.autoStop("Auto chế thần khí đã hoàn tất.");
					return;
				}
				if (this.autoTkGrade == 5 && (this.autoTkStones <= 0 || this.autoTkNeedStone))
				{
					this.autoAfterHouse = 62;
					this.autoPhase = 20;
					break;
				}
				this.autoSnapshot = this.autoInventoryIds();
				this.autoTkNoMat = false;
				this.autoTkNeedStone = false;
				GameService.gI().doMenuOption(-517, (sbyte)0, 0);
				GameService.gI().doMenuOption(-517, (sbyte)50, this.autoTkType);
				GameService.gI().doMenuOption(-517, (sbyte)55, 2);
				GameService.gI().doMenuOption(-517, (sbyte)51, this.autoTkGrade);
				GameService.gI().doCustomPopup(Canvas.gameScr.mainChar.ID, (sbyte)121, "", 1);
				this.autoPhase = 61;
				this.autoWaitCount = 0;
				break;
			}
			case 62:
			{
				int n = Math.max(1, Math.min(10, (this.autoTkQty - this.autoFound) * (this.autoTkFilter == 0 ? 5 : 1)));
				mVector vector = new mVector();
				GemTemp gemTemp = new GemTemp();
				gemTemp.id = (short)154;
				gemTemp.number = (short)n;
				vector.addElement(gemTemp);
				this.autoTkNeedStone = false;
				this.autoTkStones = 0;
				mVector[] vectorArray = new mVector[] { MainChar.gemItem, MainChar.gemItemKhoa };
				for (int i = 0; i < 2; ++i)
				{
					for (int j = 0; vectorArray[i] != null && j < vectorArray[i].size(); ++j)
					{
						GemTemplate gemTemplate = (GemTemplate)vectorArray[i].elementAt(j);
						if (gemTemplate.id != 154) continue;
						gemTemplate.number = 0;
					}
				}
				GameService.gI().buyGemItem(vector);
				this.autoPhase = 63;
				this.autoWaitCount = 0;
				break;
			}
			case 63:
			{
				int n = PauseMenu.autoGemCount(154);
				if (n <= 0 && ++this.autoWaitCount <= 100) break;
				if (n <= 0)
				{
					this.autoStop("Auto chế thần khí: không mua được Đá ngũ hợp tinh khiết cấp 6 (có thể hết lượng hoặc hành trang đầy), đã dừng.");
					return;
				}
				this.autoTkStones = n;
				this.autoAfterHouse = 60;
				this.autoPhase = 60;
				break;
			}
			case 61:
			{
				if (this.autoTkNeedStone)
				{
					Canvas.endDlg();
					this.autoTkStones = 0;
					this.autoPhase = 60;
					this.autoWaitTicks = 1;
					break;
				}
				if (this.autoTkNoMat)
				{
					Canvas.endDlg();
					if (this.autoTkGrade > (this.autoTkFilter == 0 ? 4 : 0))
					{
						--this.autoTkGrade;
						this.autoPhase = 60;
						this.autoWaitTicks = 2;
						break;
					}
					this.autoStop(this.autoTkFilter == 0 ? "Auto chế thần khí: hết nguyên liệu cấp 5-6 (cấp thấp hơn không ra được 9 dòng), đã dừng." : "Auto chế thần khí: hết nguyên liệu, đã dừng.");
					return;
				}
				ItemInInventory itemInInventory = this.autoNewItem();
				if (itemInInventory != null && itemInInventory.idTemplate >= 890 && itemInInventory.idTemplate <= 892)
				{
					Canvas.endDlg();
					++this.autoTries;
					if (this.autoTkGrade == 5)
					{
						--this.autoTkStones;
					}
					if (this.autoTkFilter != 0 || PauseMenu.autoAttVal(itemInInventory, 168) >= 9)
					{
						++this.autoFound;
					}
					else if (Canvas.gameScr.isOffLineMap(Canvas.gameScr.map))
					{
						this.autoSellQuiet(itemInInventory.itemID);
					}
					this.autoPhase = 60;
					this.autoWaitTicks = 1;
					break;
				}
				if (++this.autoWaitCount <= 250) break;
				Canvas.endDlg();
				this.autoStop("Auto chế thần khí: không nhận được kết quả từ server, đã dừng.");
				return;
			}
			case 30:
			{
				if (this.autoPetCurSlot >= this.autoPetEndSlot)
				{
					this.autoStop("Auto đồ thú đã hoàn tất.");
					return;
				}
				mVector vector = this.autoPetByColor(1);
				for (int i = 0; i < vector.size(); ++i)
				{
					ItemInInventory itemInInventory = (ItemInInventory)vector.elementAt(i);
					this.autoPetOwned.removeElement((int)itemInInventory.itemID);
					for (int j = this.autoPetOwned.size() - 1; j >= 0; --j)
					{
						if ((int)this.autoPetOwned.elementAt(j) != itemInInventory.itemID) continue;
						this.autoPetOwned.removeElementAt(j);
					}
					if (this.autoPetOk(itemInInventory) && this.autoPetCenterOk(itemInInventory, 2))
					{
						++this.autoFound;
						if (this.autoPetMode == 2 && this.autoPetBaseHe < 0 && itemInInventory.heItem >= 0)
						{
							this.autoPetBaseHe = (itemInInventory.heItem - this.autoPetCurSlot + 5) % 5;
						}
						++this.autoPetCurSlot;
						continue;
					}
					if (this.autoPetOk(itemInInventory)) continue;
					this.autoPetSell.addElement((int)itemInInventory.itemID);
				}
				if (this.autoPetCurSlot >= this.autoPetEndSlot)
				{
					break;
				}
				int n5 = this.autoPetByColor(2).size();
				bool bl4 = this.autoPetHas(2, 2);
				int n = n5 >= 5 && bl4 ? 2 : (this.autoPetByColor(3).size() >= 5 && (this.autoPetHas(3, 1) || bl4 || n5 < 4) ? 1 : (this.autoPetByColor(0).size() >= 5 ? 0 : -1));
				if (n < 0 || PauseMenu.autoGemCount(PET_STONE[n]) < this.autoPetN() || this.autoPetSell.size() > 0)
				{
					this.autoPhase = Canvas.gameScr.isOffLineMap(Canvas.gameScr.map) ? 41 : 20;
					break;
				}
				if (Canvas.gameScr.isOffLineMap(Canvas.gameScr.map))
				{
					this.autoPetExitHouse();
					return;
				}
				this.autoPetStep = n;
				int n6 = n == 2 ? 1 : (n == 1 ? Math.max(1, 5 - n5) : 8);
				n6 = Math.min(Math.min(n6, 8), PauseMenu.autoGemCount(PET_STONE[n]) / this.autoPetN());
				mVector vector2 = this.autoPetBuildGroups(n, n6);
				if (vector2.size() == 0)
				{
					this.autoPhase = 20;
					break;
				}
				sbyte[] byArray = new sbyte[6];
				byArray[n == 2 ? 5 : 0] = (sbyte)this.autoPetN();
				for (int i = 0; i < vector2.size(); ++i)
				{
					GameService.gI().doMenuOption(-8, (sbyte)0, 2);
					GameService.gI().doMenuOption(-8, (sbyte)7, (this.autoPetLevel - 30) / 5);
					GameService.gI().doMenuOption(-8, (sbyte)8, this.heMaPhap ? 0 : 1);
					GameService.gI().doMenuOption(-8, (sbyte)9, n);
					GameService.gI().doMenuOption(-8, (sbyte)10, 2);
					GameService.gI().epDothu((mVector)vector2.elementAt(i), byArray);
				}
				this.autoPetGroups = vector2.size();
				this.autoTries += this.autoPetGroups;
				this.autoOkCount = 0;
				this.autoOkSeen = false;
				this.autoPhase = 34;
				this.autoWaitCount = 0;
				break;
			}
			case 32:
			{
				if (WindowInfoScr.gI().name != null)
				{
					WindowInfoScr.gI().name = "[Auto] Hợp đồ thú " + new string[] { "xanh", "đỏ", "hoàn mỹ" }[this.autoPetStep] + " lv" + this.autoPetLevel;
					this.autoPhase = 33;
					this.autoWaitTicks = 1;
					break;
				}
				if (++this.autoWaitCount <= 200) break;
				this.autoStop("Auto đồ thú: không mở được hợp đồ thú (phải đứng ở làng và hành trang còn chỗ trống), đã dừng.");
				return;
			}
			case 33:
			{
				if (Canvas.currentScreen == WindowInfoScr.gI())
				{
					WindowInfoScr.gI().right.action();
				}
				this.autoPetEp = this.autoPetPickEp(this.autoPetStep);
				if (this.autoPetEp == null)
				{
					this.autoPhase = 30;
					break;
				}
				sbyte[] byArray = new sbyte[6];
				byArray[this.autoPetStep == 2 ? 5 : 0] = (sbyte)this.autoPetN();
				this.autoRefObj = (ItemInInventory)this.autoPetEp.elementAt(0);
				this.autoOkSeen = false;
				GameService.gI().epDothu(this.autoPetEp, byArray);
				++this.autoTries;
				this.autoPhase = 34;
				this.autoWaitCount = 0;
				break;
			}
			case 34:
			{
				if (this.autoOkCount < this.autoPetGroups && ++this.autoWaitCount <= 250) break;
				if (Canvas.currentScreen == WindowInfoScr.gI())
				{
					WindowInfoScr.gI().right.action();
				}
				if (this.autoOkCount == 0)
				{
					this.autoStop("Auto đồ thú: không hợp được đồ (phải đứng ở làng và hành trang còn chỗ trống), đã dừng.");
					return;
				}
				this.autoPhase = 30;
				this.autoWaitTicks = 1;
				break;
			}
			case 41:
			{
				for (int i = 0; i < this.autoPetSell.size(); ++i)
				{
					short s = (short)(int)this.autoPetSell.elementAt(i);
					if (this.autoFindItem(s) == null) continue;
					this.autoSellQuiet(s);
				}
				this.autoPetSell.removeAllElements();
				int n = this.autoPetN();
				int[] nArray2 = this.autoPetNeeds();
				int n2 = nArray2[0];
				int n3 = nArray2[1];
				int n4 = nArray2[2];
				int[] nArray = new int[] { n * n3, n * n2, n };
				mVector vector = new mVector();
				for (int i = 0; i < 3; ++i)
				{
					int n5 = nArray[i] - PauseMenu.autoGemCount(PET_STONE[i]);
					if (n5 <= 0) continue;
					GemTemp gemTemp = new GemTemp();
					gemTemp.id = (short)PET_STONE[i];
					gemTemp.number = (short)Math.min(n5, 300);
					vector.addElement(gemTemp);
				}
				if (vector.size() > 0)
				{
					GameService.gI().buyGemItem(vector);
				}
				int n7 = 0;
				for (int i = 0; i < vector.size(); ++i)
				{
					if (PauseMenu.autoHasGemType(((GemTemp)vector.elementAt(i)).id)) continue;
					++n7;
				}
				int n6 = 42 * Canvas.gameScr.mainChar.limItemBag - Canvas.gameScr.mainChar.FullInventory() - (MainChar.gemItemKhoa == null ? 0 : MainChar.gemItemKhoa.size()) - n7 - 1;
				this.autoPetBuyW = Math.min(n4, Math.min(n6, 120));
				if (n4 > 0 && this.autoPetBuyW <= 0)
				{
					this.autoStop("Auto đồ thú: hành trang đầy, không mua thêm được đồ thú, đã dừng.");
					return;
				}
				this.autoSnapshot = this.autoInventoryIds();
				if (this.autoPetBuyW > 0)
				{
					GameService.gI().doMenuOption(2, (sbyte)0, 1);
					GameService.gI().doMenuOption(2, (sbyte)1, this.heMaPhap ? 0 : 1);
				}
				this.autoPhase = 42;
				break;
			}
			case 42:
			{
				if (this.autoPetBuyW > 0)
				{
					mVector vector = new mVector();
					for (int i = 0; i < this.autoPetBuyW; ++i)
					{
						ItemInInventory itemInInventory = new ItemInInventory();
						itemInInventory.catagory = (sbyte)3;
						itemInInventory.charClazz = Canvas.gameScr.mainChar.clazz;
						itemInInventory.idTemplate = (short)this.autoPetTemplate();
						vector.addElement(itemInInventory);
					}
					this.autoInvRef = Char.inventory;
					GameService.gI().buyItem(vector);
				}
				this.autoPhase = 43;
				this.autoWaitCount = 0;
				break;
			}
			case 43:
			{
				int n = 0;
				mVector vector = Char.inventory;
				for (int i = 0; vector != null && i < vector.size(); ++i)
				{
					ItemInInventory itemInInventory = (ItemInInventory)vector.elementAt(i);
					bool bl2 = false;
					for (int j = 0; j < this.autoSnapshot.Length; ++j)
					{
						if (this.autoSnapshot[j] != itemInInventory.itemID) continue;
						bl2 = true;
						break;
					}
					if (bl2 || itemInInventory.idTemplate != this.autoPetTemplate()) continue;
					++n;
				}
				if (n < this.autoPetBuyW && Char.inventory == this.autoInvRef && ++this.autoWaitCount <= 150) break;
				for (int i = 0; vector != null && i < vector.size(); ++i)
				{
					ItemInInventory itemInInventory = (ItemInInventory)vector.elementAt(i);
					bool bl3 = false;
					for (int j = 0; j < this.autoSnapshot.Length; ++j)
					{
						if (this.autoSnapshot[j] != itemInInventory.itemID) continue;
						bl3 = true;
						break;
					}
					if (bl3 || itemInInventory.idTemplate != this.autoPetTemplate() || this.autoPetIsOwned(itemInInventory.itemID)) continue;
					this.autoPetOwned.addElement((int)itemInInventory.itemID);
				}
				this.autoSoldIds.removeAllElements();
				if (Canvas.currentScreen == WindowInfoScr.gI())
				{
					WindowInfoScr.gI().right.action();
				}
				if (this.autoPetBuyW > 0 && n == 0)
				{
					this.autoStop("Auto đồ thú: không mua được đồ thú (có thể hết xu), đã dừng.");
					return;
				}
				int n2 = this.autoPetN();
				int[] nArray3 = this.autoPetNeeds();
				int n3 = nArray3[0];
				int n4 = nArray3[1];
				if (PauseMenu.autoGemCount(PET_STONE[0]) < Math.min(n2 * n4, n2) || PauseMenu.autoGemCount(PET_STONE[1]) < Math.min(n2 * n3, n2) || PauseMenu.autoGemCount(PET_STONE[2]) < n2)
				{
					this.autoStop("Auto đồ thú: không mua đủ đá ngũ hợp (thiếu xu hoặc lượng), đã dừng.");
					return;
				}
				this.autoPetExitHouse();
				return;
			}
			case 51:
			{
				if (!Canvas.gameScr.isOffLineMap(Canvas.gameScr.map))
				{
					this.autoPhase = 30;
					this.autoWaitTicks = 8;
					break;
				}
				if (++this.autoWaitCount <= 300) break;
				this.autoStop("Auto đồ thú: không ra được khỏi nhà, đã dừng.");
				return;
			}
			case 20:
			{
				if (Canvas.gameScr.isOffLineMap(Canvas.gameScr.map))
				{
					this.autoPhase = this.autoAfterHouse;
					break;
				}
				CMLocation cMLocation = PauseMenu.autoFindHouseDoor();
				if (cMLocation != null)
				{
					GameScr.oldMap = Canvas.gameScr.map;
					GameService.gI().changeMap(cMLocation.toM, cMLocation.toX, cMLocation.toY);
					ChangScr.gI().switchToMe();
					Res.removeMonsterTemplet();
					this.autoPhase = 21;
					this.autoWaitCount = 0;
					break;
				}
				short s = this.autoFindCard();
				if (s < 0)
				{
					this.autoStop("Auto chế đồ: không có Thẻ mua bán trong hành trang. Hãy tự vào nhà Hắc Ngưu rồi bật lại auto.");
					return;
				}
				for (int i = 0; i < CARD_BLOCKED_MAP.Length; ++i)
				{
					if (CARD_BLOCKED_MAP[i] != Canvas.gameScr.map) continue;
					this.autoStop("Auto chế đồ: ở map này không có cửa nhà Hắc Ngưu và cũng không dùng được Thẻ mua bán. Hãy về làng rồi bật lại auto.");
					return;
				}
				GameService.gI().useItem(s);
				this.autoPhase = 21;
				this.autoWaitCount = 0;
				break;
			}
			case 21:
			{
				if (Canvas.gameScr.isOffLineMap(Canvas.gameScr.map))
				{
					this.autoPhase = this.autoAfterHouse;
					this.autoWaitTicks = 8;
					break;
				}
				if (++this.autoWaitCount <= 300) break;
				this.autoStop("Auto chế đồ: không vào được nhà Hắc Ngưu, đã dừng.");
				return;
			}
			case 5:
			{
				GameService.gI().doRequestActionCheDo(10, this.autoCraftedItemID);
				this.autoQueueNpc = -502;
				this.autoQueueMenu = new int[] { 0 };
				this.autoQueueOption = new int[] { 2 };
				this.autoQueuePos = 0;
				WindowInfoScr.gI().listCheDo = null;
				this.autoPhase = 1;
				this.autoWaitTicks = 1;
				break;
			}
		}
	}

	/**
	 * Gọi ở ĐẦU GameScr.update(): lỗi ném ra ở đây làm bỏ qua cả phần update phía sau (nhân
	 * vật không đi, màn hình như đơ) - nên bắt hết, dừng auto và báo lỗi ở bước nào.
	 */
	public static void autoCheDoTick()
	{
		try
		{
			AutoUp.tick();
			AutoVanTieu.tick();
			AutoVanMay.tick();
			LenThanh.tick();
			TienIch.batLuong();
		}
		catch (System.Exception)
		{
			// auto up lỗi không được làm hỏng khung hình của game
		}
		// Bản Java: if (KiemServer.canNgat()) { KiemServer.daQua = true; Canvas.cmdNgatKetNoi.action.perform(); return; }
		// -> không port: bản C# kiểm tra server ở KiemServer.cs (xem QUY-UOC.md)
		if (me != null)
		{
			try
			{
				me.autoTick();
			}
			catch (System.Exception throwable)
			{
				int n = me.autoPhase;
				try
				{
					// Java throwable.toString() = "tên lớp: thông báo"
					me.autoStop("Auto " + (me.autoJob >= 0 && me.autoJob < AUTO_JOB_NAME.Length ? AUTO_JOB_NAME[me.autoJob] : "") + " lỗi ở bước " + n + ": " + throwable.GetType().FullName + ": " + throwable.Message);
				}
				catch (System.Exception)
				{
					me.autoRunning = false;
					AutoGuard.on = false;
				}
			}
		}
	}

	public PauseMenu()
	{
		this.init();
		// Bản Java còn dựng this.name[9] (menu "Dòng ẩn": "( ) " + dongAnLabel(i)) và 2 lệnh center ("Chọn" -> doSelect)
		// / right ("Đóng" -> điều hướng index/selected/Limit/cmy của màn PauseMenu J2ME).
		// -> bỏ: bản Unity không hiện màn này (QUY-UOC.md). Trạng thái dòng ẩn (dongAnChon...) đã khởi tạo
		// ở field initializer bên PauseMenu.A.cs nên không cần làm gì thêm ở đây.
	}
}
