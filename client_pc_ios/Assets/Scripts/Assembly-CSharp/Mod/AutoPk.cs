

/**
 * Chức năng PK theo bản "KPAH2-PKVIP" (pksvip, đọc từ bản đã giải mã):
 *  - Đập người (rlRW): Đồ sát / Đeo khăn / Bất chấp / Quái - tự chọn người gần nhất trong tầm nhìn
 *    rồi tung lần lượt các ô skill; Quái = đánh lan quái quanh mình.
 *  - Mã PK (MkmOY, bật bằng phím PK khi Auto pk khác Tắt): Ngầm = đánh mục tiêu đang chọn theo hệ;
 *    Auto click = giả lập bấm phím 1, 3, 5, 7, 9.
 *  - Cài đặt & Auto (KAUz): bơm HP/MP 15k + tự buff theo hệ, item Nộ, tự hồi sinh, tự tẩy đồ sát,
 *    lưu toạ độ, KS trụ rồng, phím tắt (PK, nháy map, bật đồ sát, dừng).
 *  - Menu PK (YPyz.ve): Đồ sát, Đổi pet, Đổi thú, Giao thẻ - buff, Cài skill pk, Chat, Tẩy + dame,
 *    Tẩy + hp, Thay đồ; phím gọi thú/pet.
 * Khác bản gốc: đánh qua GameScr.doUseSkillToFocusActor (game tự kiểm MP, tầm, thời gian hồi skill)
 * thay vì gửi thẳng lệnh 6/106 liên tục - tránh vượt chống flood của server (300 gói / 5s). Bỏ mọi
 * phần gửi dữ liệu ra ngoài / tải danh sách từ máy chủ tác giả mod (rkbom.site), kiểm tra bản quyền.
 */
public sealed class AutoPk
{
	private static readonly string[] DAP = new string[]{"Đang tắt", "Đồ sát", "Đeo khăn", "Bất chấp", "Quái"};
	private static readonly string[] AUTO_PK = new string[]{"Tắt", "Ngầm", "Auto click"};
	private static readonly string[] NO = new string[]{"Tắt", "Nộ 84", "Nộ 123"};
	private static readonly int[] ID_NO = new int[]{84, 123};
	private const string RMS_TEN = "kpahpk";

	// ---- cài đặt (lưu RMS)
	public static int dap;
	public static int autoPk;
	public static bool maPk;
	public static int tamNhin = 1000;
	public static int phanTramBom = 80;
	public static int tocDoBom = 8;
	public static int hpTru = 10;
	public static bool bom;
	public static int no;
	public static bool hoiSinh;
	public static bool tayDs;
	public static bool nhayMap;
	public static bool ltd;
	public static bool ks;
	public static int keyPk = 49;
	public static int keyNhay = 42;
	public static int keyBatDs = 46;
	public static int keySao = 35;
	/** Bản cảm ứng (PC / Android): 4 phím trên thành nút MÃ PK / NHÁY / ĐS / DỪNG cạnh nút UP. */
	public static bool hienNut = true;

	// ---- trạng thái
	private static bool daNap;
	private static long lucDap;
	private static int oDap;
	private static long lucBom;
	private static long lucNo;
	private static long lucHoiSinh;
	private static long lucTay;
	private static long lucLtd;
	private static int ltdX;
	private static int ltdY;
	private static int ltdBuoc = 5;
	private static long ltdLucDung;
	private static int ltdCuX = -1;
	private static int ltdCuY = -1;
	private static int tayDiem = -1;
	private static long lucTayDiem;

	private AutoPk()
	{
	}

	/** Chế độ Đập người lần gần nhất (nút PK bật lại đúng chế độ này). */
	private static int dapCu = 1;

	/** Nút PK nổi trên màn game (AutoGuard): đang đánh (Đập người / Mã pk) thì tắt, chưa thì bật Đập người. */
	public static void batTat()
	{
		AutoPk.nap();
		if (dap > 0 || maPk)
		{
			if (dap > 0)
			{
				dapCu = dap;
			}
			dap = 0;
			maPk = false;
		}
		else
		{
			dap = dapCu;
		}
	}

	/** Đang đánh - nút PK hiện BẬT. */
	public static bool danh()
	{
		return dap > 0 || maPk;
	}

	public static bool dang()
	{
		return dap > 0 || maPk || bom || no > 0 || hoiSinh || tayDs || ltd || ks || tayDiem >= 0;
	}

	// ------------------------------------------------------------------ lưu / nạp

	public static void nap()
	{
		if (daNap)
		{
			return;
		}
		daNap = true;
		try
		{
			string @string = RMS.loadRMSString(RMS_TEN);
			if (@string == null || @string.Length == 0)
			{
				return;
			}
			int[] nArray = new int[17];
			nArray[15] = 1;
			int n = 0;
			int n2 = 0;
			bool bl = false;
			for (int i = 0; i <= @string.Length && n < nArray.Length; ++i)
			{
				char c = i < @string.Length ? @string[i] : ',';
				if (c == ',')
				{
					nArray[n++] = bl ? -n2 : n2;
					n2 = 0;
					bl = false;
				}
				else if (c == '-')
				{
					bl = true;
				}
				else
				{
					n2 = n2 * 10 + (c - '0');
				}
			}
			tamNhin = nArray[0];
			phanTramBom = nArray[1];
			tocDoBom = nArray[2];
			hpTru = nArray[3];
			bom = nArray[4] == 1;
			no = nArray[5];
			hoiSinh = nArray[6] == 1;
			tayDs = nArray[7] == 1;
			nhayMap = nArray[8] == 1;
			ks = nArray[9] == 1;
			keyPk = nArray[10];
			keyNhay = nArray[11];
			keyBatDs = nArray[12];
			keySao = nArray[13];
			autoPk = nArray[14];
			hienNut = nArray[15] == 1;
		}
		catch (System.Exception)
		{
			// cài đặt hỏng: dùng mặc định
		}
	}

	public static void luu()
	{
		try
		{
			string @string = new System.Text.StringBuilder().Append(tamNhin).Append(",").Append(phanTramBom).Append(",").Append(tocDoBom).Append(",").Append(hpTru)
				.Append(",").Append(bom ? 1 : 0).Append(",").Append(no).Append(",").Append(hoiSinh ? 1 : 0).Append(",").Append(tayDs ? 1 : 0)
				.Append(",").Append(nhayMap ? 1 : 0).Append(",").Append(ks ? 1 : 0).Append(",").Append(keyPk).Append(",").Append(keyNhay)
				.Append(",").Append(keyBatDs).Append(",").Append(keySao).Append(",").Append(autoPk).Append(",").Append(hienNut ? 1 : 0).ToString();
			RMS.saveRMSString(RMS_TEN, @string);
		}
		catch (System.Exception)
		{
			// empty catch block
		}
	}

	// ------------------------------------------------------------------ menu

	public static void moMenu(int chon)
	{
		AutoPk.nap();
		mVector vector = new mVector();
		vector.addElement(new Command(new System.Text.StringBuilder().Append("Đập người: ").Append(DAP[dap]).ToString()) { action = () =>
		{
				dap = dap >= DAP.Length - 1 ? 0 : dap + 1;
				if (dap > 0)
				{
					dapCu = dap;
				}
				AutoPk.moMenu(0);
			} });
		vector.addElement(new Command(new System.Text.StringBuilder().Append("Auto pk: ").Append(AUTO_PK[autoPk]).ToString()) { action = () =>
		{
				autoPk = autoPk >= AUTO_PK.Length - 1 ? 0 : autoPk + 1;
				if (autoPk == 0)
				{
					maPk = false;
				}
				AutoPk.luu();
				AutoPk.moMenu(1);
			} });
		vector.addElement(new Command(maPk ? "Mã pk: ON" : "Mã pk: OFF") { action = () =>
		{
				maPk = !maPk;
				if (maPk && tayDs)
				{
					// bản gốc: bật Mã pk mà đang Tự tẩy ĐS thì bật đồ sát trước
					GameService.gI().requestKiller();
				}
				AutoPk.moMenu(2);
			} });
		vector.addElement(new Command("Cài Đặt & Auto") { action = () =>
		{
				AutoPk.moCaiDat(0);
			} });
		if (Tilemap.lv != 201)
		{
			vector.addElement(new Command("Đồ sát") { action = () =>
			{
					Canvas.gameScr.switchToMe();
					GameService.gI().requestKiller();
				} });
		}
		vector.addElement(new Command("Đổi pet") { action = () =>
		{
				AutoPk.moDoiThu(1);
			} });
		vector.addElement(new Command("Đổi thú") { action = () =>
		{
				AutoPk.moDoiThu(0);
			} });
		if (Tilemap.lv == 201)
		{
			vector.addElement(new Command("Giao thẻ - buff") { action = () =>
			{
					AutoPk.giaoThe();
				} });
			vector.addElement(new Command("Cài skill pk") { action = () =>
			{
					AutoPk.caiSkill(false);
				} });
		}
		vector.addElement(new Command("Chat") { action = () =>
		{
				Canvas.gameScr.switchToMe();
				Canvas.inputDlg.setInfo("Nội dung Chat", () => { Canvas.gameScr.doChatFromMe(Canvas.inputDlg.tfInput.getText()); Canvas.inputDlg.tfInput.setText(string.Empty); Canvas.endDlg(); }, TField.INPUT_TYPE_ANY, 100, false);
				Canvas.inputDlg.show();
				Canvas.menu.showMenu = false;
			} });
		vector.addElement(new Command("Tẩy + dame") { action = () =>
		{
				AutoPk.batTayDiem(true);
			} });
		vector.addElement(new Command("Tẩy + hp") { action = () =>
		{
				AutoPk.batTayDiem(false);
			} });
		vector.addElement(new Command("Thay đồ") { action = () =>
		{
				GameService.gI().setConfig(6);
				Canvas.menu.showMenu = false;
			} });
		vector.addElement(new Command("Phím gọi thú/pet") { action = () =>
		{
				AutoPk.moPhimThu();
			} });
		vector.addElement(new Command("Tắt toàn bộ PK") { action = () =>
		{
				AutoPk.tat();
			} });
		PauseMenu.moMenuGiuChon(vector, 2, chon);
	}

	public static void tat()
	{
		dap = 0;
		maPk = false;
		bom = false;
		no = 0;
		hoiSinh = false;
		tayDs = false;
		ltd = false;
		ks = false;
		tayDiem = -1;
		AutoPk.luu();
		Canvas.menu.showMenu = false;
	}

	private static void moCaiDat(int chon)
	{
		mVector vector = new mVector();
		vector.addElement(new Command(bom ? "Bơm HP/MP 15k: Bật" : "Bơm HP/MP 15k: Tắt") { action = () =>
		{
				bom = !bom;
				AutoPk.luu();
				AutoPk.moCaiDat(0);
			} });
		vector.addElement(new Command(new System.Text.StringBuilder().Append("Auto Item Nộ: ").Append(NO[no]).ToString()) { action = () =>
		{
				no = no >= NO.Length - 1 ? 0 : no + 1;
				AutoPk.luu();
				AutoPk.moCaiDat(1);
			} });
		vector.addElement(new Command(hoiSinh ? "Tự động Hồi Sinh: Bật" : "Tự động Hồi Sinh: Tắt") { action = () =>
		{
				hoiSinh = !hoiSinh;
				AutoPk.luu();
				AutoPk.moCaiDat(2);
			} });
		vector.addElement(new Command(tayDs ? "Tự động Tẩy ĐS: Bật" : "Tự động Tẩy ĐS: Tắt") { action = () =>
		{
				tayDs = !tayDs;
				AutoPk.luu();
				AutoPk.moCaiDat(3);
			} });
		vector.addElement(new Command(nhayMap ? "Nháy map (phím): Bật" : "Nháy map (phím): Tắt") { action = () =>
		{
				nhayMap = !nhayMap;
				AutoPk.luu();
				AutoPk.moCaiDat(4);
			} });
		vector.addElement(new Command(ltd ? new System.Text.StringBuilder().Append("Lưu tđ: ").Append(ltdX).Append(",").Append(ltdY).Append(" ON").ToString() : "Lưu tđ: OFF") { action = () =>
		{
				ltd = !ltd;
				if (ltd)
				{
					// bản gốc: bật là lưu chỗ đang đứng
					ltdX = Canvas.gameScr.mainChar.x / 16;
					ltdY = Canvas.gameScr.mainChar.y / 16;
					ltdBuoc = 5;
					ltdLucDung = mSystem.currentTimeMillis();
					ltdCuX = -1;
					ltdCuY = -1;
				}
				AutoPk.moCaiDat(5);
			} });
		vector.addElement(new Command(ks ? "KS trụ rồng: Bật" : "KS trụ rồng: Tắt") { action = () =>
		{
				ks = !ks;
				AutoPk.luu();
				AutoPk.moCaiDat(6);
			} });
		AutoPk.themSo(vector, "% HP bơm", 7, 1, 100);
		AutoPk.themSo(vector, "Tốc độ bơm", 8, 1, 10);
		AutoPk.themSo(vector, "% HP trụ (KS)", 9, 1, 100);
		AutoPk.themSo(vector, "Tầm nhìn", 10, 0, 10000);
		if (true)
		{
			// bản cảm ứng: phím tắt thành nút trên màn game
			vector.addElement(new Command(hienNut ? "Nút phím PK: Hiện" : "Nút phím PK: Ẩn") { action = () =>
			{
					hienNut = !hienNut;
					AutoPk.luu();
					AutoPk.moCaiDat(11);
				} });
		}
		else
		{
			AutoPk.themSo(vector, "Phím PK", 11, 0, 255);
			AutoPk.themSo(vector, "Phím nháy", 12, 0, 255);
			AutoPk.themSo(vector, "Phím bật ĐS", 13, 0, 255);
			AutoPk.themSo(vector, "Phím dừng", 14, 0, 255);
		}
		PauseMenu.moMenuGiuChon(vector, 2, chon);
	}

	private static int laySo(int n)
	{
		switch (n)
		{
			case 7: return phanTramBom;
			case 8: return tocDoBom;
			case 9: return hpTru;
			case 10: return tamNhin;
			case 11: return keyPk;
			case 12: return keyNhay;
			case 13: return keyBatDs;
		}
		return keySao;
	}

	private static void datSo(int n, int n2)
	{
		switch (n)
		{
			case 7: phanTramBom = n2; break;
			case 8: tocDoBom = n2; break;
			case 9: hpTru = n2; break;
			case 10: tamNhin = n2; break;
			case 11: keyPk = n2; break;
			case 12: keyNhay = n2; break;
			case 13: keyBatDs = n2; break;
			default: keySao = n2; break;
		}
	}

	/** Một dòng số trong Cài đặt: bấm thì nhập số mới (phím = mã phím, vd 49 = phím 1, 42 = *, 35 = #). */
	private static void themSo(mVector vector, string @string, int n, int n2, int n3)
	{
		vector.addElement(new Command(new System.Text.StringBuilder().Append(@string).Append(": ").Append(AutoPk.laySo(n)).ToString()) { action = () =>
		{
				Canvas.inputDlg.setInfo(new System.Text.StringBuilder().Append(@string).Append(" (").Append(n2).Append("-").Append(n3).Append(")").ToString(), () =>
				{
						try
						{
							int n4 = int.Parse(Canvas.inputDlg.tfInput.getText().Trim());
							if (n4 < n2 || n4 > n3)
							{
								Canvas.startOKDlg("Chỉ số không hợp lệ!");
								return;
							}
							AutoPk.datSo(n, n4);
							AutoPk.luu();
						}
						catch (System.Exception)
						{
							Canvas.startOKDlg("Vui lòng chỉ nhập số hợp lệ!");
							return;
						}
						Canvas.endDlg();
						AutoPk.moCaiDat(n);
					}, 1, 5, true);
				Canvas.inputDlg.show();
			} });
	}

	// ------------------------------------------------------------------ đổi pet / thú

	/** n = 1 pet, 0 thú cưỡi (AnimalInfo.typeAnimal). */
	private static void moDoiThu(int n)
	{
		mVector vector = new mVector();
		for (int i = 0; i < Char.animal.size(); ++i)
		{
			AnimalInfo animalInfo = (AnimalInfo)Char.animal.elementAt(i);
			if (animalInfo == null || animalInfo.typeAnimal != n || AutoPk.an(animalInfo.id)) continue;
			vector.addElement(new Command(animalInfo.name) { action = () =>
			{
					AutoPk.goiThu(animalInfo);
					Canvas.menu.showMenu = false;
				} });
		}
		if (vector.size() == 0)
		{
			Canvas.startOKDlg(n == 1 ? "Không có pet trong Hành Trang!" : "Không có thú cưỡi trong Hành Trang!");
			return;
		}
		vector.addElement(new Command("[Cấu hình ẩn/hiện]") { action = () =>
		{
				AutoPk.moAnHien();
			} });
		PauseMenu.moMenuGiuChon(vector, 2, 0);
	}

	private static void goiThu(AnimalInfo animalInfo)
	{
		if (animalInfo.typeAnimal == 0)
		{
			GameService.gI().unRideHorse((sbyte)0);
		}
		GameService.gI().doRideAnimal(animalInfo.id, animalInfo.typeAnimal);
	}

	private static bool an(short s)
	{
		try
		{
			return "1".Equals(RMS.loadRMSString(new System.Text.StringBuilder().Append("ph_").Append(s).ToString()));
		}
		catch (System.Exception)
		{
			return false;
		}
	}

	private static void moAnHien()
	{
		mVector vector = new mVector();
		for (int i = 0; i < Char.animal.size(); ++i)
		{
			AnimalInfo animalInfo = (AnimalInfo)Char.animal.elementAt(i);
			if (animalInfo == null || animalInfo.typeAnimal != 0 && animalInfo.typeAnimal != 1) continue;
			string @string = new System.Text.StringBuilder().Append(AutoPk.an(animalInfo.id) ? "[ẩn] " : "[hiện] ").Append(animalInfo.typeAnimal == 0 ? "[Thú] " : "[PET] ").Append(animalInfo.name).ToString();
			vector.addElement(new Command(@string) { action = () =>
			{
					RMS.saveRMSString(new System.Text.StringBuilder().Append("ph_").Append(animalInfo.id).ToString(), AutoPk.an(animalInfo.id) ? "0" : "1");
					AutoPk.moAnHien();
				} });
		}
		if (vector.size() == 0)
		{
			Canvas.startOKDlg("Không có pet, thú trong hành trang!");
			return;
		}
		PauseMenu.moMenuGiuChon(vector, 2, 0);
	}

	/** Cài phím gọi từng thú/pet (bản gốc: màn "Cài Phím Thú/Pet", lưu petkey_<id>). */
	private static void moPhimThu()
	{
		mVector vector = new mVector();
		for (int i = 0; i < Char.animal.size(); ++i)
		{
			AnimalInfo animalInfo = (AnimalInfo)Char.animal.elementAt(i);
			if (animalInfo == null || animalInfo.typeAnimal != 0 && animalInfo.typeAnimal != 1 || AutoPk.an(animalInfo.id)) continue;
			string @string = RMS.loadRMSString(new System.Text.StringBuilder().Append("petkey_").Append(animalInfo.id).ToString());
			string string2 = new System.Text.StringBuilder().Append(animalInfo.typeAnimal == 0 ? "Thú " : "Pet ").Append(animalInfo.name).Append(": ").Append(@string == null || @string.Length == 0 ? "-" : @string).ToString();
			vector.addElement(new Command(string2) { action = () =>
			{
					Canvas.inputDlg.setInfo("Mã phím (vd 49 = phím 1, 0 = bỏ)", () =>
					{
							try
							{
								int n = int.Parse(Canvas.inputDlg.tfInput.getText().Trim());
								RMS.saveRMSString(new System.Text.StringBuilder().Append("petkey_").Append(animalInfo.id).ToString(), n == 0 ? "" : (n).ToString());
							}
							catch (System.Exception)
							{
								Canvas.startOKDlg("Vui lòng chỉ nhập số hợp lệ!");
								return;
							}
							Canvas.endDlg();
							AutoPk.moPhimThu();
						}, 1, 3, true);
					Canvas.inputDlg.show();
				} });
		}
		if (vector.size() == 0)
		{
			Canvas.startOKDlg("Không có pet, thú trong hành trang!");
			return;
		}
		PauseMenu.moMenuGiuChon(vector, 2, 0);
	}

	// ------------------------------------------------------------------ giao thẻ / cài skill / tẩy điểm

	private static void giaoThe()
	{
		GameService.gI().doMenuOption(-29, (sbyte)0, 0);
		GameService.gI().doMenuOption(-4, (sbyte)0, 0);
		GameService.gI().doMenuOption(-5, (sbyte)0, 0);
		AutoPk.caiSkill(true);
		Canvas.menu.showMenu = false;
	}

	private static void oSkill(int n, int n2, int n3, bool bl)
	{
		MainChar.quickSlot[n][n2].setIsSkill(n3, Canvas.gameScr.mainChar.clazz, bl);
	}

	private static void oThuoc(int n, int n2, int n3)
	{
		MainChar.quickSlot[n][n2].setIsPotion(n3);
	}

	/** Xếp lại 2 trang ô nhanh theo hệ (bản gốc AnhmKz.sk / bf). bl = sau giao thẻ (ô nộ 123). */
	private static void caiSkill(bool bl)
	{
		try
		{
			int n = Canvas.gameScr.mainChar.clazz;
			int n2 = bl || no == 2 ? 123 : 84;
			if (bl && n == 2)
			{
				AutoPk.oThuoc(0, 0, 123);
				AutoPk.oThuoc(0, 1, 94);
				AutoPk.oThuoc(0, 2, 96);
				AutoPk.oThuoc(0, 3, 93);
				AutoPk.oThuoc(0, 4, 94);
				AutoPk.oSkill(1, 0, 4, true);
				AutoPk.oSkill(1, 1, 7, true);
				AutoPk.oThuoc(1, 2, 93);
				AutoPk.oThuoc(1, 3, 95);
				AutoPk.oThuoc(1, 4, 94);
			}
			else if (n == 2)
			{
				AutoPk.oSkill(0, 0, 8, false);
				AutoPk.oSkill(0, 1, 9, false);
				AutoPk.oSkill(0, 2, 10, false);
				AutoPk.oSkill(0, 3, 0, false);
				AutoPk.oThuoc(0, 4, n2);
				AutoPk.oSkill(1, 0, 4, true);
				AutoPk.oSkill(1, 1, 7, true);
				AutoPk.oSkill(1, 2, 3, false);
				AutoPk.oSkill(1, 3, 1, false);
				AutoPk.oSkill(1, 4, 2, false);
			}
			else
			{
				AutoPk.oSkill(0, 0, 6, false);
				AutoPk.oSkill(0, 1, 7, false);
				AutoPk.oSkill(0, 2, 8, false);
				AutoPk.oThuoc(0, 3, 94);
				AutoPk.oThuoc(0, 4, n2);
				AutoPk.oSkill(1, 4, 0, false);
				AutoPk.oSkill(1, 3, 1, false);
				AutoPk.oSkill(1, 1, 2, false);
				AutoPk.oSkill(1, 2, 3, false);
				AutoPk.oSkill(1, 0, n == 0 ? 5 : 4, true);
			}
			RMS.saveQuickSlot();
		}
		catch (System.Exception)
		{
			// empty catch block
		}
		Canvas.menu.showMenu = false;
	}

	/** Tẩy + dame / Tẩy + hp: dùng thuốc khôi phục tiềm năng (25, hết thì mua) rồi cộng hết điểm. */
	private static void batTayDiem(bool bl)
	{
		tayDiem = bl ? 1 : 0;
		lucTayDiem = 0L;
		Canvas.menu.showMenu = false;
	}

	private static void tickTayDiem(MainChar mainChar, long l)
	{
		if (tayDiem < 0 || l - lucTayDiem < 300L)
		{
			return;
		}
		lucTayDiem = l;
		if (mainChar.potions[25] < 1)
		{
			GameService.gI().buyItemShopSpecial(6);
			return;
		}
		if (mainChar.basePointLeft < 1)
		{
			Canvas.gameScr.doUsePotion(25);
			return;
		}
		int n = 3;
		if (tayDiem == 1)
		{
			// dame theo hệ: hệ 0/1/3 cộng sức mạnh, hệ 4 cộng thân pháp, hệ 2 cộng tinh thần (thứ tự gốc)
			n = mainChar.clazz == 4 ? 1 : (mainChar.clazz == 2 ? 2 : 0);
		}
		GameService.gI().addBasePoint(n, mainChar.basePointLeft);
		tayDiem = -1;
	}

	// ------------------------------------------------------------------ phím tắt

	/** Gọi ở đầu xử lý phím của Canvas (qua AutoGuard.phimPk): true = đã dùng phím này. */
	public static bool phim(int n)
	{
		try
		{
			GameScr gameScr = Canvas.gameScr;
			if (gameScr == null || Canvas.currentScreen != gameScr || Canvas.currentDialog != null || Canvas.menu.showMenu || gameScr.chatMode)
			{
				return false;
			}
			AutoPk.nap();
			AutoPk.goiThuTheoPhim(n);
			if (n == keyPk && autoPk != 0)
			{
				maPk = !maPk;
				if (maPk && tayDs)
				{
					GameService.gI().requestKiller();
				}
				return true;
			}
			if (n == keyNhay && nhayMap)
			{
				GameService.gI().changeMap(24, 3, 3);
				return true;
			}
			if (n == keyBatDs)
			{
				GameService.gI().requestKiller();
				return true;
			}
			if (n == keySao)
			{
				AutoPk.tat();
				AutoVanTieu.tat();
				return false;
			}
		}
		catch (System.Exception)
		{
			// không bao giờ cản phím của game
		}
		return false;
	}

	// ---- 4 nút (bản cảm ứng) thay cho 4 phím
	public static void nutMaPk()
	{
		AutoPk.nap();
		if (autoPk == 0)
		{
			autoPk = 1;
			AutoPk.luu();
		}
		maPk = !maPk;
		if (maPk && tayDs)
		{
			GameService.gI().requestKiller();
		}
	}

	public static void nutNhay()
	{
		GameService.gI().changeMap(24, 3, 3);
	}

	public static void nutDs()
	{
		GameService.gI().requestKiller();
	}

	private static int petKe;

	/** Nút ĐỔI PET: gọi con pet kế tiếp trong hành trang (vòng, bỏ con đã ẩn), báo tên lên khung chat. */
	public static void nutDoiPet()
	{
		mVector vector = new mVector();
		for (int i = 0; i < Char.animal.size(); ++i)
		{
			AnimalInfo pet = (AnimalInfo)Char.animal.elementAt(i);
			if (pet == null || pet.typeAnimal != 1 || AutoPk.an(pet.id)) continue;
			vector.addElement(pet);
		}
		if (vector.size() == 0)
		{
			Canvas.startOKDlg("Không có pet trong Hành Trang!");
			return;
		}
		if (petKe >= vector.size())
		{
			petKe = 0;
		}
		AnimalInfo animalInfo = (AnimalInfo)vector.elementAt(petKe);
		petKe = (petKe + 1) % vector.size();
		AutoPk.goiThu(animalInfo);
		Canvas.gameScr.addChat(new ChatInfo("", new System.Text.StringBuilder().Append("Đổi pet: ").Append(animalInfo.name).ToString(), 0));
	}

	public static void nutDung()
	{
		AutoPk.tat();
		AutoVanTieu.tat();
	}

	private static void goiThuTheoPhim(int n)
	{
		if (n == 0)
		{
			return;
		}
		string @string = (n).ToString();
		for (int i = 0; i < Char.animal.size(); ++i)
		{
			AnimalInfo animalInfo = (AnimalInfo)Char.animal.elementAt(i);
			if (animalInfo == null || animalInfo.typeAnimal != 0 && animalInfo.typeAnimal != 1) continue;
			if (!@string.Equals(RMS.loadRMSString(new System.Text.StringBuilder().Append("petkey_").Append(animalInfo.id).ToString()))) continue;
			AutoPk.goiThu(animalInfo);
			return;
		}
	}

	// ------------------------------------------------------------------ mỗi khung hình

	public static void tick()
	{
		if (!daNap)
		{
			AutoPk.nap();
		}
		GameScr gameScr = Canvas.gameScr;
		if (gameScr == null || gameScr.mainChar == null || Canvas.currentScreen != gameScr || !AutoPk.dang())
		{
			return;
		}
		MainChar mainChar = gameScr.mainChar;
		long l = mSystem.currentTimeMillis();
		try
		{
			AutoPk.tickHoiSinh(mainChar, l);
			if (mainChar.hp <= 0)
			{
				return;
			}
			AutoPk.tickTayDiem(mainChar, l);
			AutoPk.tickBom(gameScr, mainChar, l);
			AutoPk.tickNo(gameScr, mainChar, l);
			AutoPk.tickTayDs(gameScr, mainChar, l);
			AutoPk.tickLtd(mainChar, l);
			if (Canvas.currentDialog != null || Canvas.menu.showMenu || gameScr.chatMode)
			{
				return;
			}
			if (ks && AutoPk.tickKs(gameScr, mainChar, l))
			{
				return;
			}
			if (maPk)
			{
				AutoPk.tickMaPk(gameScr, mainChar, l);
			}
			else if (dap > 0)
			{
				AutoPk.tickDap(gameScr, mainChar, l);
			}
		}
		catch (System.Exception)
		{
			// lỗi auto không được làm hỏng khung hình của game
		}
	}

	private static void tickHoiSinh(MainChar mainChar, long l)
	{
		if (!hoiSinh || mainChar.hp > 0 || l - lucHoiSinh < 1000L)
		{
			return;
		}
		lucHoiSinh = l;
		GameService.gI().doCustomPopup(mainChar.ID, (sbyte)1, "", 1);
	}

	/** ba()/be(): bơm theo % HP/MP (thuốc 15k trước, rồi 7k, rồi thường) + tự buff theo hệ. */
	private static void tickBom(GameScr gameScr, MainChar mainChar, long l)
	{
		if (!bom || l - lucBom < (long)(150 - tocDoBom * 10))
		{
			return;
		}
		lucBom = l;
		if (mainChar.hp >= 1 && mainChar.hp <= mainChar.maxhp * phanTramBom / 100)
		{
			AutoPk.dungThuoc(gameScr, mainChar, new int[]{94, 93, 22, 21, 3});
		}
		if (mainChar.mp <= mainChar.maxmp * phanTramBom / 100 && mainChar.hp >= 10)
		{
			AutoPk.dungThuoc(gameScr, mainChar, new int[]{96, 95, 24, 23, 6});
		}
		if (gameScr.indexBuff == -1)
		{
			int n = mainChar.clazz;
			if (n == 1 || n == 3 || n == 4)
			{
				AutoPk.buff(mainChar, 4);
			}
			else if (n == 0)
			{
				AutoPk.buff(mainChar, 5);
			}
			else if (n == 2)
			{
				AutoPk.buff(mainChar, 4);
				AutoPk.buff(mainChar, 7);
			}
		}
	}

	private static void dungThuoc(GameScr gameScr, MainChar mainChar, int[] nArray)
	{
		for (int i = 0; i < nArray.Length; ++i)
		{
			if (nArray[i] >= mainChar.potions.Length || mainChar.potions[nArray[i]] <= 0) continue;
			gameScr.doUsePotion(nArray[i]);
			return;
		}
	}

	/** Tự buff skill ô n lên mình - như game khi bấm ô buff (GameScr.doFire). */
	private static void buff(MainChar mainChar, int n)
	{
		if (n >= Char.skillLevelLearnt.Length || Char.skillLevelLearnt[n] <= 0)
		{
			return;
		}
		long l = mSystem.currentTimeMillis();
		if (l - mainChar.timeLastUseSkills[n] <= mainChar.coolDownSkill[n])
		{
			return;
		}
		int n2 = SkillManager.getSkillMP(n, Char.skillLevelLearnt[n - 4], mainChar.clazz);
		if (n2 > mainChar.mp)
		{
			return;
		}
		sbyte by = SkillManager.EFF_BUFF_SKILL[mainChar.clazz][n - 4];
		mainChar.mp -= n2;
		GameService.gI().useBuff(mainChar.ID, (sbyte)0, by, (short)0);
		mainChar.coolDownSkill[n] = SkillManager.getSkillCooldown((sbyte)n, mainChar.clazz, Char.skillLevelLearnt[n]);
		mainChar.timeLastUseSkills[n] = l;
	}

	private static void tickNo(GameScr gameScr, MainChar mainChar, long l)
	{
		if (no == 0 || mainChar.isNo || l - lucNo < 1000L)
		{
			return;
		}
		int n = ID_NO[no - 1];
		if (n < mainChar.potions.Length && mainChar.potions[n] > 0)
		{
			lucNo = l;
			gameScr.doUsePotion(n);
		}
	}

	/** Tự tẩy đồ sát: còn điểm PK thì dùng Phiếu công ích (29), hết thì mua (shop 10). */
	private static void tickTayDs(GameScr gameScr, MainChar mainChar, long l)
	{
		if (!tayDs || mainChar.killer <= 1 || l - lucTay < 500L)
		{
			return;
		}
		lucTay = l;
		if (mainChar.potions[29] < 1)
		{
			GameService.gI().buyItemShopSpecial(10);
		}
		else
		{
			gameScr.doUsePotion(29);
		}
	}

	/** Lưu toạ độ: bị đẩy khỏi chỗ đã lưu thì mỗi 3s đi về một đoạn (5 ô, đứng yên lâu thì nới tới 20). */
	private static void tickLtd(MainChar mainChar, long l)
	{
		if (!ltd || l - lucLtd < 3000L)
		{
			return;
		}
		int n = mainChar.x / 16;
		int n2 = mainChar.y / 16;
		if (Math.abs(ltdX - n) < 1 && Math.abs(ltdY - n2) < 1)
		{
			return;
		}
		lucLtd = l;
		if (n == ltdCuX && n2 == ltdCuY)
		{
			if (l - ltdLucDung >= 10000L)
			{
				ltdBuoc = Math.min(20, ltdBuoc + 3);
				ltdLucDung = l;
			}
		}
		else
		{
			ltdLucDung = l;
		}
		ltdCuX = n;
		ltdCuY = n2;
		int n3 = Math.max(-ltdBuoc, Math.min(ltdBuoc, ltdX - n));
		int n4 = Math.max(-ltdBuoc, Math.min(ltdBuoc, ltdY - n2));
		mainChar.moveTo((short)((n + n3) * 16 + 8), (short)((n2 + n4) * 16 + 8));
	}

	// ------------------------------------------------------------------ đánh

	private static bool tenNguoi(Actor actor)
	{
		if (!(actor is Char))
		{
			return false;
		}
		string @string = ((Char)actor).name;
		return @string == null || @string.IndexOf(' ') == -1 && @string.IndexOf('@') == -1;
	}

	/** isValidPKTarget: người chơi khác mình, còn sống, đúng chế độ Đồ sát / Đeo khăn. */
	private static bool hopLe(GameScr gameScr, Actor actor)
	{
		MainChar mainChar = gameScr.mainChar;
		if (actor == null || actor.catagory != 0 || actor.ID == mainChar.ID || actor == mainChar)
		{
			return false;
		}
		if (actor.state == 3 || actor.state == 4 || actor.state == 7 || !AutoPk.tenNguoi(actor))
		{
			return false;
		}
		Char ch = (Char)actor;
		if (ch.name != null && ch.name.Equals(mainChar.name, System.StringComparison.OrdinalIgnoreCase) || ch.hp <= 0)
		{
			return false;
		}
		if (dap == 1 && ch.killer <= 0 && !ch.iskiller)
		{
			return false;
		}
		if (dap == 2 && (mainChar.pk <= 0 || ch.pk <= 0 || mainChar.pk == ch.pk))
		{
			return false;
		}
		return true;
	}

	private static bool xa(MainChar mainChar, Actor actor)
	{
		int n = Math.abs(mainChar.x - actor.x);
		int n2 = Math.abs(mainChar.y - actor.y);
		return n * n + n2 * n2 > tamNhin * tamNhin;
	}

	private static Actor timNguoi(GameScr gameScr)
	{
		MainChar mainChar = gameScr.mainChar;
		Actor actor = null;
		long l = (long)tamNhin * (long)tamNhin;
		mVector vector = gameScr.actors;
		for (int i = 0; i < vector.size(); ++i)
		{
			Actor actor2 = (Actor)vector.elementAt(i);
			if (!AutoPk.hopLe(gameScr, actor2)) continue;
			long l2 = (long)(actor2.x - mainChar.x) * (long)(actor2.x - mainChar.x) + (long)(actor2.y - mainChar.y) * (long)(actor2.y - mainChar.y);
			if (l2 > l) continue;
			l = l2;
			actor = actor2;
		}
		return actor;
	}

	private static void chon(GameScr gameScr, Actor actor)
	{
		if (gameScr.focusedActor != actor)
		{
			gameScr.focusedActor = actor;
			GameService.gI().requestCharInfo(actor.ID);
		}
	}

	/** Tung lần lượt các ô skill trong danh sách lên mục tiêu đang chọn, mỗi ô cách 50ms (autokill). */
	private static void tung(GameScr gameScr, int[] nArray, long l)
	{
		if (l - lucDap < 50L)
		{
			return;
		}
		lucDap = l;
		for (int i = 0; i < nArray.Length; ++i)
		{
			int n = nArray[(oDap + i) % nArray.Length];
			if (n >= Char.skillLevelLearnt.Length || Char.skillLevelLearnt[n] <= 0) continue;
			if (l - gameScr.mainChar.timeLastUseSkills[n] <= gameScr.mainChar.coolDownSkill[n]) continue;
			oDap = (oDap + i + 1) % nArray.Length;
			gameScr.doUseSkillToFocusActor((sbyte)n);
			return;
		}
	}

	private static readonly int[] SKILL_DAP = new int[]{0, 1, 2, 3, 8, 9};

	private static void tickDap(GameScr gameScr, MainChar mainChar, long l)
	{
		if (dap == 4)
		{
			AutoPk.danhQuai(gameScr, mainChar, l);
			return;
		}
		Actor actor = gameScr.focusedActor;
		if (dap == 3)
		{
			if (actor != null && actor.catagory == 0 && actor != mainChar)
			{
				AutoPk.tung(gameScr, SKILL_DAP, l);
			}
			return;
		}
		if (actor == null || !AutoPk.hopLe(gameScr, actor) || AutoPk.xa(mainChar, actor))
		{
			actor = AutoPk.timNguoi(gameScr);
			if (actor == null)
			{
				return;
			}
			AutoPk.chon(gameScr, actor);
		}
		AutoPk.tung(gameScr, SKILL_DAP, l);
	}

	/** Ô skill đánh theo hệ (MkmOY.autokill / killquai): hệ 2 dùng ô 0-3, 8-10; hệ khác 0-3, 6-8. */
	private static int[] skillTheoHe(MainChar mainChar)
	{
		return mainChar.clazz == 2 ? new int[]{10, 9, 8, 3, 2, 1, 0} : new int[]{8, 7, 6, 3, 2, 1, 0};
	}

	private static void danhQuai(GameScr gameScr, MainChar mainChar, long l)
	{
		Actor actor = gameScr.focusedActor;
		if (actor == null || actor.catagory != 1 || ((LiveActor)actor).hp <= 0)
		{
			Actor actor2 = null;
			int n = 30000;
			mVector vector = gameScr.actors;
			for (int i = 0; i < vector.size(); ++i)
			{
				Actor actor3 = (Actor)vector.elementAt(i);
				if (actor3 == null || actor3.catagory != 1 || !(actor3 is Monster) || ((LiveActor)actor3).hp <= 0) continue;
				if (((Monster)actor3).idChar >= 0) continue;
				int n2 = (actor3.x - mainChar.x) * (actor3.x - mainChar.x) + (actor3.y - mainChar.y) * (actor3.y - mainChar.y);
				if (n2 > n) continue;
				n = n2;
				actor2 = actor3;
			}
			if (actor2 == null)
			{
				return;
			}
			gameScr.focusedActor = actor2;
		}
		AutoPk.tung(gameScr, AutoPk.skillTheoHe(mainChar), l);
	}

	private static void tickMaPk(GameScr gameScr, MainChar mainChar, long l)
	{
		if (autoPk == 2)
		{
			// Auto click: giả lập bấm 1, 3, 5, 7, 9 (game tự tung ô skill / thuốc đã xếp)
			if (l - lucDap < 120L)
			{
				return;
			}
			lucDap = l;
			int[] nArray = new int[]{1, 3, 5, 7, 9};
			Canvas.keyPressed[nArray[oDap % nArray.Length]] = true;
			oDap = (oDap + 1) % nArray.Length;
			return;
		}
		Actor actor = gameScr.focusedActor;
		if (actor == null)
		{
			return;
		}
		if (actor.catagory == 0 && mainChar.hp > 10 && actor.state != 3 && actor.state != 4 && actor.state != 7)
		{
			AutoPk.tung(gameScr, AutoPk.skillTheoHe(mainChar), l);
		}
		else if (actor.catagory == 1)
		{
			AutoPk.tung(gameScr, AutoPk.skillTheoHe(mainChar), l);
		}
	}

	/** KS: mục tiêu là "Trụ rồng" và máu <= HP trụ % thì tung toàn bộ skill. */
	private static bool tickKs(GameScr gameScr, MainChar mainChar, long l)
	{
		Actor actor = gameScr.focusedActor;
		if (!(actor is LiveActor) || actor.state == 2 || actor.ID == 32001)
		{
			return false;
		}
		string @string = actor is Char ? ((Char)actor).name : null;
		if (@string == null && actor is Monster)
		{
			Monster monster = (Monster)actor;
			@string = Res.monsterTemplates != null && monster.monster_type < Res.monsterTemplates.Length && Res.monsterTemplates[monster.monster_type] != null ? Res.monsterTemplates[monster.monster_type].name : null;
		}
		if (!"Trụ rồng".Equals(@string))
		{
			return false;
		}
		LiveActor liveActor = (LiveActor)actor;
		if (liveActor.maxhp <= 0 || liveActor.hp <= 1 || (long)liveActor.hp * 100L / (long)liveActor.maxhp > (long)hpTru)
		{
			return false;
		}
		AutoPk.tung(gameScr, AutoPk.skillTheoHe(mainChar), l);
		return true;
	}
}
