public class CreateCharScr : MyScreen
{
	public static CreateCharScr me;

	public static CharClassInfo[][] clazz = new CharClassInfo[5][]
	{
		new CharClassInfo[2]
		{
			new CharClassInfo(0, "Dòng Charmander", "Nam", "Than lửa", "Mien Bac", "Lửa", 3, 0),
			new CharClassInfo(0, "Dòng Charmander", "Nữ", "Than lửa", "Mien Bac", "Lửa", 3, 0)
		},
		new CharClassInfo[2]
		{
			new CharClassInfo(1, "Dòng Bulbasaur", "Nam", "Hạt giống thần", "Mien Bac", "Cỏ", 4, 0),
			new CharClassInfo(1, "Dòng Bulbasaur", "Nữ", "Hạt giống thần", "Mien Bac", "Cỏ", 4, 0)
		},
		new CharClassInfo[2]
		{
			new CharClassInfo(2, "Dòng Eevee", "Nam", "Khăn lụa", "Mien Nam", "Thường", 5, 0),
			new CharClassInfo(2, "Dòng Eevee", "Nữ", "Khăn lụa", "Mien Nam", "Thường", 5, 0)
		},
		new CharClassInfo[2]
		{
			new CharClassInfo(3, "Dòng Squirtle", "Nam", "Nước thần", "Mien Nam", "Nước", 6, 0),
			new CharClassInfo(3, "Dòng Squirtle", "Nữ", "Nước thần", "Mien Nam", "Nước", 6, 0)
		},
		new CharClassInfo[2]
		{
			new CharClassInfo(4, "Dòng Pichu", "Nam", "Nam châm", "Mien Bac", "Điện", 7, 0),
			new CharClassInfo(4, "Dòng Pichu", "Nữ", "Nam châm", "Mien Bac", "Điện", 7, 0)
		}
	};

	public static string[] hairName = new string[6] { "Đầu đinh", "Lông búi", "Lông cột cao", "Lông chéo", "Lông xù", "Lông ngang vai" };

	private static string[] gender = new string[2] { "Nam", "Nữ" };

	public static int selectGender = 0;

	public static int selectNation = 0;

	public TField tfChat;

	private Command cmdClear;

	public MainChar ch = new MainChar();

	private int currentClazz;

	private int selected;

	private int xL;

	private int xR;

	public CreateCharScr()
	{
		tfChat = new TField(this);
		tfChat.width = 125;
		tfChat.height = 21;
		tfChat.isFocus = true;
		tfChat.setIputType(TField.INPUT_ALPHA_NUMBER_ONLY);
		// Pokemon khong co gioi tinh/kieu toc: luon gui gender = 0, headStyle = 0 (muc 4.2).
		selectGender = 0;
		ch.state = 0;
		currentClazz = 0;
		ch.hatStyle = -1;
		ch.currentHead = clazz[currentClazz][selectGender].headID;
		ch.bodyStyle = clazz[currentClazz][selectGender].bodyID;
		ch.legStyle = clazz[currentClazz][selectGender].legID;
		ch.weaponType = clazz[currentClazz][selectGender].weaponType;
		ch.weaponStyle = clazz[currentClazz][selectGender].weaponImageID;
		left = new Command("Đóng");
		ActionPerform action = () =>
		{
			CharSelectScr.gI().switchToMe();
			me = null;
		};
		left.action = action;
		cmdClear = new Command("Xóa");
		ActionPerform action2 = () =>
		{
			tfChat.clear();
		};
		cmdClear.action = action2;
		center = new Command("Tạo");
		ActionPerform action3 = () =>
		{
			if (tfChat.getText().Length < 4 || tfChat.getText().Length > 15)
			{
				Canvas.startOKDlg("Tên Pokémon phải từ 4 đến 15 ký tự");
			}
			else
			{
				GameService.gI().createChar(tfChat.getText(), currentClazz, 0, 0, selectNation);
				Canvas.startWaitDlg("Xin chờ", true);
			}
		};
		center.action = action3;
		init();
	}

	public static CreateCharScr gI()
	{
		return (me != null) ? me : (me = new CreateCharScr());
	}

	public override void switchToMe()
	{
		loadImg();
		base.switchToMe();
		selectNation = 0;
	}

	public override void init()
	{
		tfChat.x = Canvas.hw - 155;
		tfChat.y = Canvas.hh + 25;
	}

	private void changChar()
	{
		ch.currentHead = clazz[currentClazz][selectGender].headID;
		ch.bodyStyle = clazz[currentClazz][selectGender].bodyID;
		ch.legStyle = clazz[currentClazz][selectGender].legID;
		ch.weaponType = clazz[currentClazz][selectGender].weaponType;
		ch.weaponStyle = clazz[currentClazz][selectGender].weaponImageID;
	}

	private void loadImg()
	{
		GameService.gI().doRequestWeapone(0, ch.weaponType, ch.weaponStyle, ch.weaponIndex);
	}

	public override bool keyPress(int keyCode)
	{
		bool flag = false;
		tfChat.keyPressed(keyCode);
		return base.keyPress(keyCode);
	}

	public void updateKeyPT()
	{
		bool flag = false;
		if (Canvas.isPointer(Canvas.hw, Canvas.hh - 90, 180, 170) && Canvas.isPointerClick)
		{
			Canvas.isPointerClick = false;
			int num = (Canvas.py - (Canvas.hh - 90)) / 30;
			// Che do Pokemon chi con 2 dong: 0 = Pokemon khoi dau (class), 1 = lanh tho.
			int soDongCuoi = 1;
			if (num > soDongCuoi)
			{
				num = soDongCuoi;
			}
			if (selected != num)
			{
				selected = num;
			}
			// Doi dong hien thi sang dong goc (1 -> 3 = lanh tho) de giu nguyen cac nhanh cu.
			int dongGoc = ((selected == 1) ? 3 : selected);
			if (Canvas.px >= Canvas.hw && Canvas.px <= Canvas.hw + 80)
			{
				xL = 5;
				if (dongGoc == 0)
				{
					currentClazz++;
					if (currentClazz >= 5)
					{
						currentClazz = 0;
					}
					flag = true;
				}
				else if (dongGoc == 1)
				{
					selectGender = ((selectGender != 1) ? 1 : 0);
					changChar();
				}
				else if (dongGoc == 2)
				{
					clazz[currentClazz][selectGender].headID -= 2;
					if (clazz[currentClazz][selectGender].headID < 0)
					{
						clazz[currentClazz][selectGender].headID = (byte)(4 + clazz[currentClazz][selectGender].headID);
					}
					flag = true;
				}
				else if (dongGoc == 3)
				{
					selectNation--;
					if (selectNation < 0)
					{
						selectNation = 1;
					}
				}
			}
			else if (Canvas.px <= Canvas.hw + 180 && Canvas.px >= Canvas.hw + 90)
			{
				xR = 5;
				if (dongGoc == 0)
				{
					currentClazz--;
					if (currentClazz < 0)
					{
						currentClazz = 4;
					}
					flag = true;
				}
				else if (dongGoc == 1)
				{
					selectGender = ((selectGender == 0) ? 1 : 0);
					changChar();
				}
				else if (dongGoc == 2)
				{
					clazz[currentClazz][selectGender].headID += 2;
					if (clazz[currentClazz][selectGender].headID > 5)
					{
						clazz[currentClazz][selectGender].headID = (sbyte)(clazz[currentClazz][selectGender].headID % 2);
					}
					flag = true;
				}
				else if (dongGoc == 3)
				{
					selectNation++;
					if (selectNation > 1)
					{
						selectNation = 0;
					}
				}
			}
		}
		if (flag)
		{
			ch.bodyStyle = clazz[currentClazz][selectGender].bodyID;
			ch.legStyle = clazz[currentClazz][selectGender].legID;
			ch.currentHead = clazz[currentClazz][selectGender].headID;
			ch.weaponType = clazz[currentClazz][selectGender].weaponType;
			ch.weaponStyle = clazz[currentClazz][selectGender].weaponImageID;
			loadImg();
		}
	}

	public override void pointerPress(int dx, int dy)
	{
		base.pointerPress(dx, dy);
	}

	public override void paint(MyGraphics g)
	{
		BannerParallax.chon = currentClazz; // pokemon: Pokémon đang chọn nhún nhảy trên nền key art
		ServerListScr.gI().paintBg(g);
		BannerParallax.chon = -1;
		Canvas.resetTrans(g);
		int num = Canvas.hw + 35;
		int num2 = Canvas.hh - 70;
		Res.paintDlgDragonFull(g, Canvas.hw - 190, Canvas.hh - 90, 380, 170);
		int num3 = Canvas.hw - 1;
		int num4 = Canvas.hh - 89;
		for (int i = 0; i < 3; i++)
		{
			g.setColor(Res.color[i]);
			g.fillRect(num3 + i, num4 + i, 1, 168);
		}
		g.drawImage(Res.imgBar, num - 25 - xL, num2 + selected * 30 + 7, 3);
		g.drawRegion(Res.imgBar, 0, 0, 11, 7, 2, Canvas.hw + 175 + xR, num2 + selected * 30 + 7, 3);
		MFont.normalFont[0].drawString(g, "Tên:", tfChat.x, Canvas.hh + 10, 0);
		paintPokemon(g, num, num2);
		tfChat.paint(g);
		g.setClip(0, 0, Canvas.w, Canvas.h);
		base.paint(g);
	}

	/// <summary>Man tao nhan vat che do Pokemon: 5 Pokemon khoi dau thay 5 class (bang 4.1).</summary>
	private void paintPokemon(MyGraphics g, int num, int num2)
	{
		MFont.normalFont[0].drawString(g, "Pokémon: " + LoaiPokemonClient.TEN_KHOI_DAU[currentClazz], num - 5, num2, 0);
		num2 += 30;
		MFont.normalFont[0].drawString(g, "Vùng: " + GameScr.nameCountry[selectNation], num - 5, num2, 0);
		num2 += 30;
		MFont.normalFont[0].drawString(g, LoaiPokemonClient.HE_KHOI_DAU[currentClazz], num - 5, num2, 0);
		num2 += 30;
		MFont.normalFont[0].drawString(g, LoaiPokemonClient.VAI_TRO[currentClazz], num - 5, num2, 0);
		// Chan dung + Pokemon dang dung (Idle). Thieu sprite thi ve paper doll cu.
		int dexKhoiDau = LoaiPokemonClient.khoiDau(currentClazz);
		ChanDungPoke.paintHuyHieu(g, dexKhoiDau, false, Canvas.hw - 140, Canvas.hh - 45, 56);
		if (!PokeBody.paint(g, dexKhoiDau, false, Canvas.hw - 60, Canvas.hh + 2, Char.DOWN, PokeDuLieu.DUNG, Canvas.gameTick, 100))
		{
			ch.paintAvatar(g, (short)(Canvas.hw - 85), (short)(Canvas.hh + 10));
			if (ch.weaponType != -1 && ch.weaponStyle != -1 && ch.imgWpa != null)
			{
				int num5 = ((ch.frame == 1) ? 1 : 0);
				g.drawImage(ch.imgWpa, Canvas.hw - 85 + ch.dxWear, Canvas.hh + 10 + ch.dyWear + num5, 0);
			}
		}
	}

	public override void update()
	{
		tfChat.update();
		ch.update();
		base.update();
		updateKeyPT();
		if (Canvas.gameTick % 4 == 0)
		{
			if (xL > 0)
			{
				xL--;
			}
			else
			{
				xL = 0;
			}
			if (xR > 0)
			{
				xR--;
			}
			else
			{
				xR = 0;
			}
		}
	}
}
