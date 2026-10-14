// Port 1:1 từ Jar276/src/game/render/screen/PauseMenu.java (showSuaMenu .. autoItemMatches).
// Field/hằng số khai báo trong PauseMenu.A.cs.

public partial class PauseMenu
{
	private void showSuaMenu()
	{
		mVector vector = new mVector();
		vector.addElement(new Command("Thuộc tính: " + REP_STAT_NAME[this.autoRepStat])
		{
			action = () =>
			{
				this.autoRepStat = (this.autoRepStat + 1) % REP_STAT_NAME.Length;
				this.showSuaMenu();
			}
		});
		vector.addElement(new Command("HP/MP: " + REP_HP_NAME[this.autoRepHp])
		{
			action = () =>
			{
				this.autoRepHp = (this.autoRepHp + 1) % REP_HP_NAME.Length;
				this.showSuaMenu();
			}
		});
		vector.addElement(new Command("Bắt đầu sửa thuộc tính")
		{
			action = () =>
			{
				this.autoStartJob(3);
			}
		});
		Canvas.menu.startAt(vector, 3);
	}

	private static int autoAttVal(ItemInInventory itemInInventory, int n)
	{
		if (itemInInventory.allAttribute == null)
		{
			return 0;
		}
		for (int i = 0; i < itemInInventory.allAttribute.size(); ++i)
		{
			InfoAttributeItem infoAttributeItem = (InfoAttributeItem)itemInInventory.allAttribute.elementAt(i);
			if (infoAttributeItem.getID() != n) continue;
			return infoAttributeItem.getValueAtt();
		}
		return 0;
	}

	private bool autoLockOk(ItemInInventory itemInInventory)
	{
		int n = Math.max(PauseMenu.autoAttVal(itemInInventory, 58), Math.max(PauseMenu.autoAttVal(itemInInventory, 59), PauseMenu.autoAttVal(itemInInventory, 60)));
		return n >= this.autoLockGoal;
	}

	private bool autoRepAny()
	{
		return this.autoRepStat == 0 && this.autoRepHp == 0;
	}

	private bool autoRepOk(ItemInInventory itemInInventory)
	{
		return (this.autoRepStat == 0 || PauseMenu.autoAttVal(itemInInventory, 34 + this.autoRepStat) > 0) && (this.autoRepHp == 0 || PauseMenu.autoAttVal(itemInInventory, 32 + this.autoRepHp) > 0);
	}

	private bool autoJobDone(ItemInInventory itemInInventory)
	{
		if (this.autoJob == 1)
		{
			return itemInInventory.level >= this.autoGoalFor(itemInInventory);
		}
		if (this.autoJob == 2)
		{
			return this.autoLockOk(itemInInventory);
		}
		return !this.autoRepAny() && this.autoRepOk(itemInInventory);
	}

	private string dongAnLabel(int n)
	{
		int n2 = n / 2;
		int n3 = n % 2;
		return n3 == 0 ? DONG_AN_NAME[n2] : DONG_AN_NAME[n2] + " " + PauseMenu.dongAnMax(n2) + "%";
	}

	private static int dongAnMax(int n)
	{
		return n == 3 ? 5 : 9;
	}

	private bool dongAnAnyChosen()
	{
		for (int i = 0; i < this.dongAnChon.Length; ++i)
		{
			if (!this.dongAnChon[i]) continue;
			return true;
		}
		return false;
	}

	private void autoStop(string str)
	{
		PauseMenu.nhaPhim();
		this.autoRunning = false;
		AutoGuard.on = false;
		Canvas.menu.showMenu = false;
		Canvas.startOKDlg(str);
	}

	public static bool autoHideUi()
	{
		return me != null && me.autoRunning;
	}

	public static bool autoEatDlg(string str)
	{
		if (me == null || !me.autoRunning)
		{
			return false;
		}
		if (str != null && me.autoJob == 14)
		{
			if (str.StartsWith("Thao tác quá nhanh", System.StringComparison.Ordinal) || str.StartsWith("Chỉ có thể nghiền", System.StringComparison.Ordinal))
			{
				// nghienTick tự gửi lại ở lượt 5s sau; 3 lần không được thì dừng và báo câu này
				me.autoNbLoi = str;
				return true;
			}
		}
		if (str != null && me.autoJob == 13)
		{
			if (str.StartsWith("Nâng cấp thành công", System.StringComparison.Ordinal))
			{
				++me.autoHklOk;
				return true;
			}
			if (str.StartsWith("Nâng cấp thất bại", System.StringComparison.Ordinal))
			{
				++me.autoHklHong;
				return true;
			}
			if (str.StartsWith("Không đủ hoả thạch", System.StringComparison.Ordinal))
			{
				// phase 180 thấy hết đá sẽ tự mua; đếm như 1 lần không nâng
				++me.autoHklHong;
				return true;
			}
		}
		if (str != null && me.autoJob == 12)
		{
			if (str.IndexOf("học được kỹ năng", System.StringComparison.Ordinal) >= 0)
			{
				me.autoSkKq = 1;
				return true;
			}
			if (str.IndexOf("học kỹ năng thất bại", System.StringComparison.Ordinal) >= 0)
			{
				me.autoSkKq = 2;
				return true;
			}
			if (str.IndexOf("không thể học thêm kỹ năng", System.StringComparison.Ordinal) >= 0)
			{
				me.autoSkKq = 3;
				return true;
			}
			if (str.StartsWith("Thay đổi phẩm chất", System.StringComparison.Ordinal))
			{
				me.autoSkReset = true;
				return true;
			}
			if (str.StartsWith("Đã mua", System.StringComparison.Ordinal))
			{
				// mua Huyết Bồ Đề thành công ("Đã mua, món đồ đang ở trong hành trang.") - phase 173 tự thấy món mới
				return true;
			}
		}
		if (str != null && (str.StartsWith("Chúc mừng", System.StringComparison.Ordinal) || str.StartsWith("Đã bán", System.StringComparison.Ordinal)))
		{
			if (me.autoJob == 8 && str.StartsWith("Chúc mừng bạn đã tạo được thuộc tính", System.StringComparison.Ordinal))
			{
				me.autoThemOk = true;
			}
			if (me.autoJob == 9 && str.StartsWith("Chúc mừng bạn đã tạo được thuộc tính", System.StringComparison.Ordinal))
			{
				me.autoCttOk = true;
				me.autoCttVuaRa = PauseMenu.cttCatKetQua(str);
			}
			return true;
		}
		if (str != null && me.autoJob == 11)
		{
			if (str.StartsWith("Nâng cấp thành công", System.StringComparison.Ordinal))
			{
				me.autoPetUpKq = 1;
				return true;
			}
			if (str.StartsWith("Nâng cấp thất bại", System.StringComparison.Ordinal))
			{
				me.autoPetUpKq = 2;
				return true;
			}
			if (str.StartsWith("Vật phẩm cho ăn vượt quá sức ăn", System.StringComparison.Ordinal))
			{
				me.autoPetDay = true;
				return true;
			}
			if (str.IndexOf("đã có thể nâng cấp", System.StringComparison.Ordinal) >= 0 || str.IndexOf("đã có thể tiến hoá", System.StringComparison.Ordinal) >= 0)
			{
				me.autoPetBaoNang = true;
				return true;
			}
			if (str.StartsWith("Ngươi cần có", System.StringComparison.Ordinal) || str.StartsWith("Không đủ Luyện kim dược", System.StringComparison.Ordinal))
			{
				me.autoStop("Auto nuôi pet: không đủ nguyên liệu để nâng cấp pet (" + str + " - nguyên liệu cấp 6 của cả 10 loại), đã dừng.");
				return true;
			}
			if (str.StartsWith("Không thể cho ăn khi chưa nâng cấp", System.StringComparison.Ordinal))
			{
				me.autoPetCanNang = true;
				return true;
			}
		}
		if (str != null && me.autoJob == 10 && me.autoKtFull && (str.StartsWith("Vật phẩm không hợp lệ", System.StringComparison.Ordinal) || str.StartsWith("Chỉ có vật phẩm nhất phẩm", System.StringComparison.Ordinal)))
		{
			me.autoCttBoQua = true;
			return true;
		}
		if (str != null && me.autoJob == 9 && me.autoCttFull && str.StartsWith("Không thể cộng thuộc tính", System.StringComparison.Ordinal))
		{
			me.autoCttBoQua = true;
			return true;
		}
		if (me.autoJob == 7)
		{
			me.autoLastAlert = str;
			return true;
		}
		if (str != null && me.autoJob == 6 && str.StartsWith("Tinh luyện thành công", System.StringComparison.Ordinal))
		{
			try
			{
				me.autoTbPct = int.Parse(str.Substring(str.LastIndexOf(':') + 1).Trim());
			}
			catch (System.Exception)
			{
				// keep the previous value
			}
			me.autoTbResult = 1;
			return true;
		}
		if (str != null && me.autoJob == 6 && str.StartsWith("Tinh luyện thất bại", System.StringComparison.Ordinal))
		{
			me.autoTbResult = 2;
			return true;
		}
		if (str != null && me.autoJob == 5 && str.StartsWith("Ngươi không đủ nguyên liệu", System.StringComparison.Ordinal))
		{
			if (me.autoTkGrade == 5 && str.IndexOf("ngũ hợp", System.StringComparison.Ordinal) >= 0)
			{
				me.autoTkNeedStone = true;
			}
			else
			{
				me.autoTkNoMat = true;
			}
			return true;
		}
		if (str != null && me.autoJob != 0 && str.IndexOf("thành công", System.StringComparison.Ordinal) >= 0)
		{
			me.autoOkSeen = true;
			++me.autoOkCount;
			return true;
		}
		me.autoStop("Auto " + AUTO_JOB_NAME[me.autoJob] + " đã dừng: " + (str == null ? "null" : str));
		return true;
	}


	/** "Chúc mừng bạn đã tạo được thuộc tính Sét lan 0.5% cho vật phẩm ..." -> "Sét lan 0.5%". */
	private static string cttCatKetQua(string str)
	{
		int n = "Chúc mừng bạn đã tạo được thuộc tính ".Length;
		int n2 = str.IndexOf(" cho vật phẩm", System.StringComparison.Ordinal);
		if (n2 <= n)
		{
			return null;
		}
		return str.Substring(n, n2 - n).Trim();
	}

	/** "Sét lan 0.5%" -> 5 (phần mười %), -1 nếu không đọc được. */
	private static int cttDocGiaTri(string str)
	{
		int n = str.LastIndexOf(' ');
		int n2 = str.IndexOf('.', n + 1);
		int n3 = str.IndexOf('%', n + 1);
		if (n < 0 || n2 < 0 || n3 < n2)
		{
			return -1;
		}
		try
		{
			return int.Parse(str.Substring(n + 1, n2 - (n + 1))) * 10 + int.Parse(str.Substring(n2 + 1, n3 - (n2 + 1)));
		}
		catch (System.Exception)
		{
			return -1;
		}
	}

	private bool cttDat(string str)
	{
		if (str == null)
		{
			return false;
		}
		int n = str.LastIndexOf(' ');
		if (n < 0)
		{
			return false;
		}
		if (this.autoCttLoai >= 0 && !(str.Substring(0, n).Trim() == CTT_NAME[this.autoCttLoai]))
		{
			return false;
		}
		return PauseMenu.cttDocGiaTri(str) >= this.autoCttMin;
	}

	/** Full: sang món kế tiếp chưa đạt; false nếu đã hết đồ. */
	private bool autoCttNext()
	{
		while (this.autoTargets != null && ++this.autoTargetPos < this.autoTargets.Length)
		{
			ItemInInventory itemInInventory = this.autoFindItem(this.autoTargets[this.autoTargetPos]);
			if (itemInInventory == null || this.cttDatSan(itemInInventory)) continue;
			this.autoCttItem = this.autoTargets[this.autoTargetPos];
			this.autoCttVuaRa = null;
			this.autoPhase = 95;
			return true;
		}
		return false;
	}

	private string cttTongKet()
	{
		return "Auto cộng thuộc tính tất cả đồ: xong " + this.autoFound + "/" + (this.autoTargets == null ? 0 : this.autoTargets.Length) + " món, " + this.autoTries + " lần đập";
	}

	private void autoStart(int n)
	{
		this.autoRunning = true;
		AutoGuard.on = true;
		this.autoJob = 0;
		this.autoTries = 0;
		this.autoFound = 0;
		this.autoMode = n;
		this.autoSlot = 0;
		this.autoPhase = 20;
		this.autoWaitTicks = 0;
		this.autoSold = 0;
		this.autoAfterHouse = 0;
		AutoGuard.pendingSell = 0;
		this.autoCraftedItemID = (short)-1;
	}

	public static CMLocation autoFindHouseDoor()
	{
		mVector vector = Tilemap.mapchangeLocations;
		if (vector == null)
		{
			return null;
		}
		for (int i = 0; i < vector.size(); ++i)
		{
			CMLocation cMLocation = (CMLocation)vector.elementAt(i);
			if (cMLocation.toM != 103 && cMLocation.toM != 73 && cMLocation.toM != 83) continue;
			return cMLocation;
		}
		return null;
	}

	private short autoFindCard()
	{
		mVector vector = Char.inventory;
		if (vector == null)
		{
			return -1;
		}
		for (int i = 0; i < vector.size(); ++i)
		{
			ItemInInventory itemInInventory = (ItemInInventory)vector.elementAt(i);
			if (itemInInventory.idTemplate != 675) continue;
			return itemInInventory.itemID;
		}
		return -1;
	}

	private void autoStartJob(int n)
	{
		if (!this.dongAnAnyChosen())
		{
			Canvas.startOKDlg("Chưa tick dòng ẩn nào trong mục Dòng ẩn.");
			return;
		}
		mVector vector = Char.inventory;
		int n2 = 0;
		short[] sArray = new short[vector == null ? 0 : vector.size()];
		for (int i = 0; i < sArray.Length; ++i)
		{
			ItemInInventory itemInInventory = (ItemInInventory)vector.elementAt(i);
			if (itemInInventory.colorName == 0 || itemInInventory.colorName == 4 || itemInInventory.isSelling || itemInInventory.allAttribute == null || !this.autoItemMatches(itemInInventory)) continue;
			if (n == 1 && itemInInventory.level >= this.autoGoalFor(itemInInventory)) continue;
			if (n == 2 && this.autoLockOk(itemInInventory)) continue;
			if (n == 3 && !this.autoRepAny() && this.autoRepOk(itemInInventory)) continue;
			if ((n == 1 || n == 3) && itemInInventory.islock == 0) continue;
			if (n == 2 && this.autoLockTtc && !PauseMenu.autoIsTtc(itemInInventory)) continue;
			if (n >= 2 && (itemInInventory.nameCharSeal == null || !(itemInInventory.nameCharSeal.ToLower() == Canvas.gameScr.mainChar.name.ToLower()))) continue;
			sArray[n2++] = itemInInventory.itemID;
		}
		if (n2 == 0)
		{
			Canvas.startOKDlg(n != 1 ? "Không có món nào có dòng ẩn đã tick cần làm (hoặc tất cả đã đạt yêu cầu)." : n == 1 ? "Không có món nào có dòng ẩn đã tick cần nâng tới lv" + this.autoGoalLevel + "." : "Không có món nào có dòng ẩn đã tick trong hành trang.");
			return;
		}
		this.autoTargets = new short[n2];
		System.Array.Copy(sArray, 0, this.autoTargets, 0, n2);
		this.autoTargetPos = 0;
		this.autoRunning = true;
		AutoGuard.on = true;
		this.autoJob = n;
		this.autoTries = 0;
		this.autoFound = 0;
		this.autoPhase = 10;
		this.autoWaitTicks = 0;
	}

	private static bool autoIsTtc(ItemInInventory itemInInventory)
	{
		ItemTemplate itemTemplate = Res.getItem(itemInInventory.charClazz, itemInInventory.idTemplate);
		return itemTemplate != null && itemTemplate.type >= 3 && itemTemplate.type <= 9;
	}

	private int autoGoalFor(ItemInInventory itemInInventory)
	{
		return itemInInventory.level % 5 == 1 ? this.autoGoalLevel + 1 : this.autoGoalLevel;
	}

	private ItemInInventory autoFindItem(short s)
	{
		mVector vector = Char.inventory;
		if (vector == null)
		{
			return null;
		}
		for (int i = 0; i < vector.size(); ++i)
		{
			ItemInInventory itemInInventory = (ItemInInventory)vector.elementAt(i);
			if (itemInInventory.itemID != s) continue;
			return itemInInventory;
		}
		return null;
	}

	private bool autoAnyKeyPressed()
	{
		for (int i = 0; i < Canvas.keyPressed.Length; ++i)
		{
			if (!Canvas.keyPressed[i]) continue;
			return true;
		}
		return Canvas.isPointerDown || Canvas.isPointerClick;
	}

	private void autoBuildCraftQueue()
	{
		this.autoQueueNpc = -8;
		if (this.autoSlot == 0)
		{
			this.autoQueueMenu = new int[] { 0, 1, 2, 11 };
			this.autoQueueOption = new int[] { 0, Canvas.gameScr.mainChar.clazz, (this.autoLevel - 21) / 5, 2 };
		}
		else
		{
			int n2 = AUTO_SLOT_OPT[this.autoSlot];
			int n = n2 <= 2 ? 2 * ((this.autoLevel - 20) / 5) + (Canvas.gameScr.mainChar.gender == 1 ? 1 : 0) : (this.autoLevel - 20) / 5;
			this.autoQueueMenu = new int[] { 0, 3, 4, 5, 6, 11 };
			this.autoQueueOption = new int[] { 1, this.heMaPhap ? 0 : 1, Canvas.gameScr.mainChar.clazz, n2, n, 2 };
		}
		this.autoQueuePos = 0;
		WindowInfoScr.gI().listCheDo = null;
	}

	private sbyte[][] autoFillMaterial()
	{
		WindowInfoScr windowInfoScr = WindowInfoScr.gI();
		mVector vector = windowInfoScr.listCheDo;
		mVector vector2 = windowInfoScr.listTemp;
		if (vector == null || vector.size() == 0 || vector2 == null)
		{
			return null;
		}
		int n = WindowInfoScr.optionCreateItem;
		int[] nArray = new int[vector2.size()];
		// Java: new byte[vector.size()][6]
		sbyte[][] byArray = new sbyte[vector.size()][];
		for (int i = 0; i < byArray.Length; ++i)
		{
			byArray[i] = new sbyte[6];
		}
		for (int i = 0; i < vector.size(); ++i)
		{
			Mineral mineral = (Mineral)vector.elementAt(i);
			int n2 = mineral.number;
			int n3 = n <= 2 && (n != 2 || i == 0) || n > 2 ? (n == 0 ? 0 : 3) : 5;
			for (int j = 5; j >= n3 && n2 > 0; --j)
			{
				for (int k = 0; k < vector2.size() && n2 > 0; ++k)
				{
					GemTemplate gemTemplate = (GemTemplate)vector2.elementAt(k);
					if (gemTemplate.id != mineral.idTemplate + j) continue;
					int n4 = gemTemplate.number - nArray[k];
					if (n4 <= 0) continue;
					if (n4 > n2)
					{
						n4 = n2;
					}
					nArray[k] = nArray[k] + n4;
					byArray[i][j] = (sbyte)(byArray[i][j] + n4);
					n2 -= n4;
				}
			}
			if (n2 <= 0) continue;
			return null;
		}
		return byArray;
	}

	private bool autoFillTopGrade()
	{
		sbyte[] byArray = this.autoFill[this.autoFill.Length - 1];
		for (int i = 0; i < 5; ++i)
		{
			if (byArray[i] < byArray[5]) continue;
			return false;
		}
		return true;
	}

	private bool autoStageNext()
	{
		WindowInfoScr windowInfoScr = WindowInfoScr.gI();
		mVector vector = windowInfoScr.listCheDo;
		if (vector == null || windowInfoScr.listEp == null || this.autoFill == null)
		{
			return false;
		}
		while (this.autoStageIdx < this.autoFill.Length * 6)
		{
			int n = this.autoStageIdx / 6;
			int n2 = 5 - this.autoStageIdx % 6;
			++this.autoStageIdx;
			int n3 = this.autoFill[n][n2];
			if (n3 <= 0) continue;
			Mineral mineral = (Mineral)vector.elementAt(n);
			int n4 = mineral.idTemplate + n2;
			GemTemplate gemTemplate = null;
			for (int i = 0; i < windowInfoScr.listTemp.size(); ++i)
			{
				GemTemplate gemTemplate2 = (GemTemplate)windowInfoScr.listTemp.elementAt(i);
				if (gemTemplate2.id != n4) continue;
				gemTemplate = gemTemplate2;
				break;
			}
			GemTemplate gemTemplate3 = new GemTemplate(n4);
			if (gemTemplate != null)
			{
				GemTemplate.copyData(gemTemplate, gemTemplate3);
				if (WindowInfoScr.khoaCheDo == 2)
				{
					gemTemplate.number -= n3;
					if (gemTemplate.number <= 0)
					{
						windowInfoScr.listTemp.removeElement(gemTemplate);
					}
				}
			}
			gemTemplate3.number = n3;
			windowInfoScr.listEp.addElement(gemTemplate3);
			return true;
		}
		return false;
	}

	private void autoSellQuiet(short s)
	{
		++AutoGuard.pendingSell;
		// Java: new Integer(s) -> box int
		this.autoSoldIds.addElement((int)s);
		GameService.gI().sellItem(s);
	}

	private short[] autoInventoryIds()
	{
		mVector vector = Char.inventory;
		int n = vector == null ? 0 : vector.size();
		short[] sArray = new short[n];
		int n2 = 0;
		for (int i = 0; i < n; ++i)
		{
			short s = ((ItemInInventory)vector.elementAt(i)).itemID;
			if (this.autoSoldIds.conTains((int)s)) continue;
			sArray[n2++] = s;
		}
		short[] sArray2 = new short[n2];
		System.Array.Copy(sArray, 0, sArray2, 0, n2);
		return sArray2;
	}

	public static void autoPaintStatus(MyGraphics graphics)
	{
		if (me == null || !me.autoRunning || MFont.normalFont == null || MFont.normalFont[0] == null)
		{
			return;
		}
		string str = me.autoStatusText();
		MFont font = MFont.normalFont[0];
		if (font.getWidth(str) > Canvas.w - 12 && str.StartsWith("Auto ", System.StringComparison.Ordinal))
		{
			str = str.Substring(5);
		}
		while (str.Length > 6 && font.getWidth(str) > Canvas.w - 12)
		{
			str = str.Substring(0, str.Length - 1);
		}
		int n = Math.min(Canvas.w, font.getWidth(str) + 12);
		int n2 = font.getHeight() + 6;
		int n3 = (Canvas.w - n) / 2;
		graphics.translate(-graphics.getTranslateX(), -graphics.getTranslateY());
		graphics.setClip(0, 0, Canvas.w, Canvas.h);
		graphics.setColor(0x0B2E2E);
		graphics.fillRect(n3, 2, n, n2);
		graphics.setColor(0xE0B000);
		graphics.drawRect(n3, 2, n - 1, n2 - 1);
		font.drawString(graphics, str, Canvas.w / 2, 5, 2);
	}

	private string autoStatusText()
	{
		System.Text.StringBuilder stringBuilder = new System.Text.StringBuilder();
		if (this.autoJob == 0)
		{
			stringBuilder.Append("Auto chế đồ: ").Append(AUTO_SLOT_NAME[Math.min(this.autoSlot, AUTO_SLOT_NAME.Length - 1)]).Append(" lv").Append(this.autoLevel).Append(" - lần ").Append(this.autoTries + 1).Append(" - đạt ").Append(this.autoFound);
		}
		else if (this.autoJob == 4)
		{
			stringBuilder.Append("Auto đồ thú: ").Append(PET_SLOT_NAME[Math.min(this.autoPetCurSlot, 4)]).Append(" lv").Append(this.autoPetLevel).Append(" - hợp ").Append(this.autoTries).Append(" - đạt ").Append(this.autoFound);
		}
		else if (this.autoJob == 8)
		{
			ItemInInventory itemInInventory = this.autoFindItem(this.autoThemItem);
			int n2 = itemInInventory == null ? 0 : this.autoThemLines(itemInInventory, new bool[2]);
			if (this.autoThemFull)
			{
				stringBuilder.Append("Auto thêm dòng full: món ").Append(Math.min(this.autoTargetPos + 1, this.autoTargets == null ? 0 : this.autoTargets.Length)).Append("/").Append(this.autoTargets == null ? 0 : this.autoTargets.Length).Append(" - lần ").Append(this.autoTries).Append(" - xong ").Append(this.autoFound);
			}
			else
			{
				stringBuilder.Append("Auto thêm dòng: lần ").Append(this.autoTries).Append(" - đang có ").Append(n2).Append(" dòng");
			}
		}
		else if (this.autoJob == 14 && Canvas.currentScreen != Canvas.gameScr)
		{
			stringBuilder.Append("Nghiền bột: tạm dừng - đang mở màn khác, hãy đóng lại");
		}
		else if (this.autoJob == 14)
		{
			stringBuilder.Append("Nghiền bột: bột ").Append(PauseMenu.hklDa(NB_BOT)).Append(" | nghiền ").Append(this.autoFound).Append(" | chờ ").Append(PauseMenu.nbDemNghien()).Append(this.autoNbChe > 0 ? " | chế " + this.autoNbChe : "").Append(this.autoNbBan > 0 ? " | bán trắng " + this.autoNbBan : "").Append(this.autoNbHet ? " | nghiền nốt" : "");
		}
		else if (this.autoJob == 13)
		{
			ItemInInventory itemInInventory = PauseMenu.hklMon();
			stringBuilder.Append("Nâng cấp Hỏa kỳ lân: cấp ").Append(itemInInventory == null ? 0 : itemInInventory.idTemplate - 680).Append(" - đã đập ").Append(this.autoFound).Append(" - Hỏa thạch ").Append(PauseMenu.hklDa(HOA_THACH_C1)).Append("/").Append(PauseMenu.hklDa(HOA_THACH_C2)).Append(this.autoHklDaMua > 0 ? " - mua " + this.autoHklDaMua : "");
		}
		else if (this.autoJob == 12)
		{
			int n3 = PauseMenu.skPhamChat();
			stringBuilder.Append("Học skill pet: ").Append(this.skMucTieu()).Append(" - phẩm chất ").Append(n3 < 0 ? "chưa có" : SK_PHAM_CHAT[n3]).Append(" - sách ").Append(this.autoSkSach).Append(" - Huyết Bồ Đề ").Append(this.autoSkHbd).Append(this.autoSkDaMua > 0 ? " (mua " + this.autoSkDaMua + ")" : "");
		}
		else if (this.autoJob == 11)
		{
			stringBuilder.Append("Nuôi pet: cấp ").Append(PauseMenu.petCap()).Append("+").Append(PauseMenu.petPhanTram() / 10).Append(".").Append(Math.max(0, PauseMenu.petPhanTram()) % 10).Append("% - sức ăn ").Append(PauseMenu.petSucAn()).Append("/300 - đã ăn ").Append(this.autoPetDaAn).Append(" - nâng cấp ").Append(this.autoPetDaNang).Append(" - relog ").Append(this.autoPetRelog).Append(this.autoPhase == 130 || this.autoPhase == 131 ? " - đang nâng cấp" : "").Append(this.autoPhase >= 140 && this.autoPhase <= 145 ? " - đang lên Thành" : (this.autoPhase >= 150 ? " - đăng nhập lại" : (this.autoPhase == 116 || this.autoPhase == 127 || this.autoPhase == 162 ? " - lô " + this.autoPetLoCho + " món" : "")));
		}
		else if (this.autoJob == 10)
		{
			ItemInInventory itemInInventory = this.autoFindItem(this.autoKtItem);
			if (this.autoKtFull)
			{
				stringBuilder.Append("Auto khóa đồ thú: món ").Append(Math.min(this.autoTargetPos + 1, this.autoTargets == null ? 0 : this.autoTargets.Length)).Append("/").Append(this.autoTargets == null ? 0 : this.autoTargets.Length).Append(" - xong ").Append(this.autoFound).Append(" - ");
			}
			else
			{
				stringBuilder.Append("Auto khóa đồ thú: ");
			}
			stringBuilder.Append(this.autoKtMuc >= 21 ? "21%" : "20-21%").Append(" - lần ").Append(this.autoTries).Append(" - đang ").Append(itemInInventory == null ? 0 : PauseMenu.ktGiaTri(itemInInventory)).Append("%");
		}
		else if (this.autoJob == 9)
		{
			if (this.autoCttFull)
			{
				stringBuilder.Append("Auto cộng thuộc tính: món ").Append(Math.min(this.autoTargetPos + 1, this.autoTargets == null ? 0 : this.autoTargets.Length)).Append("/").Append(this.autoTargets == null ? 0 : this.autoTargets.Length).Append(" - xong ").Append(this.autoFound).Append(" - ");
			}
			else
			{
				stringBuilder.Append("Auto cộng thuộc tính: ");
			}
			stringBuilder.Append(this.autoCttLoai < 0 ? "Bất kỳ" : CTT_NAME[this.autoCttLoai]).Append(" ").Append(PauseMenu.cttMucText(this.autoCttMin)).Append(" - lần ").Append(this.autoTries).Append(this.autoCttVuaRa == null ? "" : " - vừa ra " + this.autoCttVuaRa);
		}
		else if (this.autoJob == 7)
		{
			ItemInInventory itemInInventory = this.autoTargets == null || this.autoTargetPos >= this.autoTargets.Length ? null : this.autoFindItem(this.autoTargets[this.autoTargetPos]);
			stringBuilder.Append("Auto luyện đồ: món ").Append(Math.min(this.autoTargetPos + 1, this.autoTargets == null ? 0 : this.autoTargets.Length)).Append("/").Append(this.autoTargets == null ? 0 : this.autoTargets.Length).Append(" - Cộng ").Append(itemInInventory == null ? 0 : itemInInventory.plusTemplate).Append("/").Append(this.autoLuyenTarget).Append(" - xong ").Append(this.autoFound);
		}
		else if (this.autoJob == 6)
		{
			stringBuilder.Append("Auto thần binh: tinh luyện ").Append(this.autoTbPct < 0 ? "--" : this.autoTbPct.ToString()).Append("% - lần ").Append(this.autoTries).Append(" - thành công ").Append(this.autoFound);
		}
		else if (this.autoJob == 5)
		{
			stringBuilder.Append("Auto thần khí: ").Append(this.autoFound).Append("/").Append(this.autoTkQty).Append(this.autoTkFilter == 0 ? " món 9 dòng" : " món").Append(" - đã chế ").Append(this.autoTries);
		}
		else
		{
			stringBuilder.Append("Auto ").Append(AUTO_JOB_NAME[this.autoJob]).Append(": món ").Append(Math.min(this.autoTargetPos + 1, this.autoTargets == null ? 0 : this.autoTargets.Length)).Append("/").Append(this.autoTargets == null ? 0 : this.autoTargets.Length).Append(" - ").Append(this.autoTries).Append(" lần");
		}
		int n = this.autoPhase;
		if (this.autoJob == 14)
		{
			// nghiền bột: dòng gọn, chỉ báo thêm khi đang vào nhà
			return n == 20 || n == 21 ? stringBuilder.ToString() + " | vào nhà" : stringBuilder.ToString();
		}
		stringBuilder.Append(n == 20 || n == 21 ? " - vào nhà" : (n >= 41 && n <= 43 || n == 62 || n == 63 ? " - mua" : (n == 50 || n == 51 ? " - ra làng" : " - đang chế")));
		long l = mSystem.currentTimeMillis() / 400L % 4L;
		for (int i = 0; (long)i < l; ++i)
		{
			stringBuilder.Append('.');
		}
		return stringBuilder.ToString();
	}

	private ItemInInventory autoNewItem()
	{
		ItemInInventory itemInInventory = this.autoNewItem0();
		if (itemInInventory != null)
		{
			this.autoSoldIds.removeAllElements();
		}
		return itemInInventory;
	}

	private ItemInInventory autoNewItem0()
	{
		mVector vector = Char.inventory;
		if (vector == null || this.autoSnapshot == null)
		{
			return null;
		}
		ItemInInventory itemInInventory = null;
		for (int i = 0; i < vector.size(); ++i)
		{
			ItemInInventory itemInInventory2 = (ItemInInventory)vector.elementAt(i);
			// Java: continue block0 (nhãn vòng ngoài) -> cờ coTrongSnapshot
			bool coTrongSnapshot = false;
			for (int j = 0; j < this.autoSnapshot.Length; ++j)
			{
				if (this.autoSnapshot[j] == itemInInventory2.itemID)
				{
					coTrongSnapshot = true;
					break;
				}
			}
			if (coTrongSnapshot) continue;
			if (itemInInventory != null && itemInInventory2.itemID <= itemInInventory.itemID) continue;
			itemInInventory = itemInInventory2;
		}
		return itemInInventory;
	}

	private ItemInInventory autoLatestItem()
	{
		mVector vector = Char.inventory;
		if (vector == null || vector.size() == 0)
		{
			return null;
		}
		ItemInInventory itemInInventory = null;
		for (int i = 0; i < vector.size(); ++i)
		{
			ItemInInventory itemInInventory2 = (ItemInInventory)vector.elementAt(i);
			if (itemInInventory != null && itemInInventory2.itemID <= itemInInventory.itemID) continue;
			itemInInventory = itemInInventory2;
		}
		return itemInInventory;
	}

	private bool autoItemMatches(ItemInInventory itemInInventory)
	{
		for (int i = 0; i < itemInInventory.allAttribute.size(); ++i)
		{
			InfoAttributeItem infoAttributeItem = (InfoAttributeItem)itemInInventory.allAttribute.elementAt(i);
			short s = infoAttributeItem.getID();
			if (s < 28 || s > 32) continue;
			int n = s - 28;
			int n2 = infoAttributeItem.getValueAtt();
			if (!this.dongAnChon[n * 2] && (n2 < PauseMenu.dongAnMax(n) || !this.dongAnChon[n * 2 + 1])) continue;
			return true;
		}
		return false;
	}
}
