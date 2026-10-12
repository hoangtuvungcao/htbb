public class MainChar_HAIRMOD_PROTECTED : Char_HAIRMOD_PROTECTED
{
	public static QuickSlot_HAIRMOD_PROTECTED[][] quickSlot;

	public bool isFirstTimeGetItem;

	public bool isFirstTimeGetMoney;

	public bool isFirstTimeGetPotion;

	public bool autoFight;

	public bool check;

	public bool canMove;

	public sbyte idSkill;

	public sbyte limItemBag;

	public int idMonster;

	public static mVector_HAIRMOD_PROTECTED gemitemImbue = new mVector_HAIRMOD_PROTECTED();

	public static mVector_HAIRMOD_PROTECTED gemItem = new mVector_HAIRMOD_PROTECTED();

	public static mVector_HAIRMOD_PROTECTED gemItemKhoa = new mVector_HAIRMOD_PROTECTED();

	public static mVector_HAIRMOD_PROTECTED shopItem = new mVector_HAIRMOD_PROTECTED();

	public mVector_HAIRMOD_PROTECTED tItems = new mVector_HAIRMOD_PROTECTED();

	public mVector_HAIRMOD_PROTECTED rItems = new mVector_HAIRMOD_PROTECTED();

	public static Potion_HAIRMOD_PROTECTED[] listPotion;

	public static short[] itemQuest;

	public bool first;

	public short lastLv;

	public short xBegin;

	public short yBegin;

	public int countRoad;

	public short[] posTransRoad;

	public short day;

	public int dedicationPoint;

	public Quest_HAIRMOD_PROTECTED mainQuest;

	public sbyte typeClass;

	public int pArena;

	public string nameHusband_wife = string.Empty;

	public bool stop;

	public static void remove()
	{
		gemitemImbue.removeAllElements();
		gemItem.removeAllElements();
		gemItemKhoa.removeAllElements();
		shopItem.removeAllElements();
		quickSlot = null;
		listPotion = null;
	}

	public override bool FuntioncanMove()
	{
		return base.FuntioncanMove();
	}

	public void loadQuickSlot()
	{
		if (quickSlot == null)
		{
			quickSlot = new QuickSlot_HAIRMOD_PROTECTED[2][];
			for (int i = 0; i < quickSlot.Length; i++)
			{
				quickSlot[i] = new QuickSlot_HAIRMOD_PROTECTED[5];
			}
			for (int j = 0; j < 5; j++)
			{
				quickSlot[0][j] = new QuickSlot_HAIRMOD_PROTECTED();
				quickSlot[1][j] = new QuickSlot_HAIRMOD_PROTECTED();
			}
			if (Char_HAIRMOD_PROTECTED.skillLevelLearnt[1] > 0)
			{
				quickSlot[0][0].setIsSkill(1, clazz, false);
			}
			if (Char_HAIRMOD_PROTECTED.skillLevelLearnt[2] > 0)
			{
				quickSlot[0][1].setIsSkill(2, clazz, false);
			}
			quickSlot[0][2].setIsSkill(0, clazz, false);
			quickSlot[0][3].setIsPotion(1);
			quickSlot[0][4].setIsPotion(4);
			quickSlot[1][3].setIsPotion(1);
			quickSlot[1][4].setIsPotion(4);
			RMS_HAIRMOD_PROTECTED.loadQuickSlot();
		}
	}

	public void resetAuto()
	{
		autoFight = false;
		idSkill = 0;
	}

	public string getPercent()
	{
		return lvpercent / 10 + "." + lvpercent % 10 + "%";
	}

	public void add2Party(PartyInfo_HAIRMOD_PROTECTED info)
	{
		for (int i = 0; i < Char_HAIRMOD_PROTECTED.party.size(); i++)
		{
			if (((PartyInfo_HAIRMOD_PROTECTED)Char_HAIRMOD_PROTECTED.party.elementAt(i)).name.Equals(info.name))
			{
				return;
			}
		}
		Char_HAIRMOD_PROTECTED.party.addElement(info);
	}

	public void add2Party(Char_HAIRMOD_PROTECTED info)
	{
		for (int i = 0; i < Char_HAIRMOD_PROTECTED.party.size(); i++)
		{
			Char_HAIRMOD_PROTECTED char_HAIRMOD_PROTECTED = (Char_HAIRMOD_PROTECTED)Char_HAIRMOD_PROTECTED.party.elementAt(i);
			if (char_HAIRMOD_PROTECTED == info)
			{
				return;
			}
		}
		Char_HAIRMOD_PROTECTED.party.addElement(info);
	}

	public string removeUserParty(short iduser)
	{
		string empty = string.Empty;
		for (int i = 0; i < Char_HAIRMOD_PROTECTED.party.size(); i++)
		{
			if (((PartyInfo_HAIRMOD_PROTECTED)Char_HAIRMOD_PROTECTED.party.elementAt(i)).id == iduser)
			{
				empty = ((PartyInfo_HAIRMOD_PROTECTED)Char_HAIRMOD_PROTECTED.party.elementAt(i)).name;
				Char_HAIRMOD_PROTECTED.party.removeElementAt(i);
				return empty;
			}
		}
		return string.Empty;
	}

	public string removeCharParty(short iduser)
	{
		string empty = string.Empty;
		for (int i = 0; i < Char_HAIRMOD_PROTECTED.party.size(); i++)
		{
			if (((Char_HAIRMOD_PROTECTED)Char_HAIRMOD_PROTECTED.party.elementAt(i)).ID == iduser)
			{
				empty = ((Char_HAIRMOD_PROTECTED)Char_HAIRMOD_PROTECTED.party.elementAt(i)).name;
				Char_HAIRMOD_PROTECTED.party.removeElementAt(i);
				return empty;
			}
		}
		return string.Empty;
	}

	public override void update()
	{
		base.update();
		if (!isFreeze && posTransRoad != null)
		{
			updateWayPointer();
			check = false;
		}
		if (!stop && mSystem_HAIRMOD_PROTECTED.currentTimeMillis() - GameScr_HAIRMOD_PROTECTED.timeMove >= 0)
		{
			GameScr_HAIRMOD_PROTECTED.timeMove = mSystem_HAIRMOD_PROTECTED.currentTimeMillis() + GameScr_HAIRMOD_PROTECTED.dtmove;
			stop = true;
			GameService_HAIRMOD_PROTECTED.gI().moveChar(x, y);
		}
		checkChangemapTouch();
	}

	private void checkChangemapTouch()
	{
		if (GameScr_HAIRMOD_PROTECTED.iTaskAuto == 2)
		{
			return;
		}
		if (canMove && !check && state == 1)
		{
			int num = 0;
			int num2 = 0;
			if (dir == Char_HAIRMOD_PROTECTED.LEFT)
			{
				num = -32;
			}
			else if (dir == Char_HAIRMOD_PROTECTED.RIGHT)
			{
				num = 32;
			}
			if (dir == Char_HAIRMOD_PROTECTED.DOWN)
			{
				num2 = 32;
			}
			else if (dir == Char_HAIRMOD_PROTECTED.UP)
			{
				num2 = -32;
			}
			if ((Math_HAIRMOD_PROTECTED.abs(x - xBegin) >= 16 || Math_HAIRMOD_PROTECTED.abs(y - yBegin) >= 16) && Canvas_HAIRMOD_PROTECTED.gameScr.checkCanChangeMap(x + num, y + num2, dir))
			{
				posTransRoad = null;
				check = true;
				return;
			}
		}
		if (state == 1)
		{
			canMove = true;
		}
		else
		{
			canMove = false;
		}
	}

	private void updateWayPointer()
	{
		if (Util_HAIRMOD_PROTECTED.abs(x - xTo) > 3 || Util_HAIRMOD_PROTECTED.abs(y - yTo) > 3)
		{
			return;
		}
		for (int num = posTransRoad.Length - 1 - countRoad; num >= 0; num--)
		{
			if (posTransRoad[num] > 0)
			{
				sbyte b = (sbyte)(GameScr_HAIRMOD_PROTECTED.xStart + (posTransRoad[num] >> 8));
				sbyte b2 = (sbyte)(GameScr_HAIRMOD_PROTECTED.yStart + (posTransRoad[num] & 0xFF));
				if (GameScr_HAIRMOD_PROTECTED.iTaskAuto != 2 && (dir == 1 || Tilemap_HAIRMOD_PROTECTED.isOfflineMap) && Canvas_HAIRMOD_PROTECTED.gameScr.checkCanChangeMap(b * 16 + speed, b2 * 16 + speed, dir))
				{
					posTransRoad = null;
					countRoad = 0;
					break;
				}
				if (Canvas_HAIRMOD_PROTECTED.gameScr.checkMoveLimit(b * 16 + speed, b2 * 16 + speed))
				{
					posTransRoad = null;
					break;
				}
				moveTo((short)(b * 16 + speed), (short)(b2 * 16 + speed));
				Canvas_HAIRMOD_PROTECTED.gameScr.sendActorMove((short)(b * 16 + speed), (short)(b2 * 16 + speed));
				posTransRoad[num] = -1;
				countRoad++;
				break;
			}
			if (num == 0)
			{
				posTransRoad = null;
				countRoad = 0;
				break;
			}
		}
	}

	public string[] getInfoSkill(int idSkill)
	{
		string[] result = null;
		if (Char_HAIRMOD_PROTECTED.skillLevelLearnt[idSkill] == -1)
		{
			result = new string[2]
			{
				"Chưa học kỹ năng này",
				"Lv yêu cầu: " + SkillManager_HAIRMOD_PROTECTED.LEVEL_ADD_SKILL[idSkill][1]
			};
		}
		else if (Char_HAIRMOD_PROTECTED.skillLevelLearnt[idSkill] == 0)
		{
			result = new string[2]
			{
				"Chưa học kỹ năng này",
				getLvRequest(idSkill)
			};
		}
		else if (!MenuWindows_HAIRMOD_PROTECTED.gI().isBuffSkill())
		{
			result = new string[5]
			{
				"Thời gian đánh: " + SkillManager_HAIRMOD_PROTECTED.getSkillCooldown((sbyte)idSkill, clazz, Char_HAIRMOD_PROTECTED.skillLevelLearnt[idSkill]) + " ms",
				SkillManager_HAIRMOD_PROTECTED.infoString[clazz][idSkill] + SkillManager_HAIRMOD_PROTECTED.getSkillDamPercent(idSkill, Char_HAIRMOD_PROTECTED.skillLevelLearnt[idSkill], clazz) + "%",
				"Phạm vi: " + SkillManager_HAIRMOD_PROTECTED.getSkillRange((sbyte)idSkill, clazz),
				"MP mất: " + SkillManager_HAIRMOD_PROTECTED.getSkillMP(idSkill, Char_HAIRMOD_PROTECTED.skillLevelLearnt[idSkill], clazz),
				(Char_HAIRMOD_PROTECTED.skillLevelLearnt[idSkill] >= 9) ? string.Empty : ("Lv yêu cầu: " + SkillManager_HAIRMOD_PROTECTED.LEVEL_ADD_SKILL[idSkill][Char_HAIRMOD_PROTECTED.skillLevelLearnt[idSkill]])
			};
		}
		else
		{
			sbyte[][] array = new sbyte[5][]
			{
				new sbyte[2] { 1, 5 },
				new sbyte[2] { 1, 2 },
				new sbyte[4] { 3, 4, 5, 6 },
				new sbyte[2] { 0, 1 },
				new sbyte[2] { 0, 3 }
			};
			string text = "MP mất: " + SkillManager_HAIRMOD_PROTECTED.getSkillMP(idSkill, Char_HAIRMOD_PROTECTED.skillLevelLearnt[idSkill], clazz);
			switch (clazz)
			{
			case 0:
				switch (idSkill)
				{
				case 4:
					result = new string[4]
					{
						"Đánh xuyên giáp",
						"Tỷ lệ thành công: " + SkillManager_HAIRMOD_PROTECTED.SKILL_DAM_PERCENT[clazz][idSkill][Char_HAIRMOD_PROTECTED.skillLevelLearnt[idSkill]] + "%",
						text,
						(Char_HAIRMOD_PROTECTED.skillLevelLearnt[idSkill] >= 9) ? string.Empty : ("Lv yêu cầu: " + SkillManager_HAIRMOD_PROTECTED.LEVEL_ADD_SKILL[idSkill][Char_HAIRMOD_PROTECTED.skillLevelLearnt[idSkill]])
					};
					break;
				case 5:
					result = new string[6]
					{
						"Gây tác hại lại 1 phần",
						"sức công cho đối thủ",
						"Thời gian: " + SkillManager_HAIRMOD_PROTECTED.TIME_LIFE_BUFF_SKILL[array[clazz][idSkill - 4]][Char_HAIRMOD_PROTECTED.skillLevelLearnt[idSkill]] + "s",
						"Tỷ lệ: " + SkillManager_HAIRMOD_PROTECTED.SKILL_DAM_PERCENT[clazz][idSkill][Char_HAIRMOD_PROTECTED.skillLevelLearnt[idSkill]] + "%",
						text,
						(Char_HAIRMOD_PROTECTED.skillLevelLearnt[idSkill] >= 9) ? string.Empty : ("Lv yêu cầu: " + SkillManager_HAIRMOD_PROTECTED.LEVEL_ADD_SKILL[idSkill][Char_HAIRMOD_PROTECTED.skillLevelLearnt[idSkill]])
					};
					break;
				}
				break;
			case 1:
				switch (idSkill)
				{
				case 5:
					result = new string[4]
					{
						"Tăng sức tấn công",
						"của bản thân",
						"Tỷ lệ tăng: " + SkillManager_HAIRMOD_PROTECTED.getSkillDamPercent(idSkill, Char_HAIRMOD_PROTECTED.skillLevelLearnt[idSkill], clazz) + "%",
						(Char_HAIRMOD_PROTECTED.skillLevelLearnt[idSkill] >= 9) ? string.Empty : ("Lv yêu cầu: " + SkillManager_HAIRMOD_PROTECTED.LEVEL_ADD_SKILL[idSkill][Char_HAIRMOD_PROTECTED.skillLevelLearnt[idSkill]])
					};
					break;
				case 4:
					result = new string[4]
					{
						"Phòng thủ tăng: " + SkillManager_HAIRMOD_PROTECTED.getSkillDamPercent(idSkill, Char_HAIRMOD_PROTECTED.skillLevelLearnt[idSkill], clazz) + "%",
						"Thời gian: " + SkillManager_HAIRMOD_PROTECTED.TIME_LIFE_BUFF_SKILL[array[clazz][idSkill - 4]][Char_HAIRMOD_PROTECTED.skillLevelLearnt[idSkill]] + "s",
						text,
						(Char_HAIRMOD_PROTECTED.skillLevelLearnt[idSkill] >= 9) ? string.Empty : ("Lv yêu cầu: " + SkillManager_HAIRMOD_PROTECTED.LEVEL_ADD_SKILL[idSkill][Char_HAIRMOD_PROTECTED.skillLevelLearnt[idSkill]])
					};
					break;
				}
				break;
			case 2:
				switch (idSkill)
				{
				case 5:
					result = new string[4]
					{
						"Tăng tinh thần",
						"của bản thân",
						"Tỷ lệ tăng: " + SkillManager_HAIRMOD_PROTECTED.getSkillDamPercent(idSkill, Char_HAIRMOD_PROTECTED.skillLevelLearnt[idSkill], clazz) + "%",
						(Char_HAIRMOD_PROTECTED.skillLevelLearnt[idSkill] >= 9) ? string.Empty : ("Lv yêu cầu: " + SkillManager_HAIRMOD_PROTECTED.LEVEL_ADD_SKILL[idSkill][Char_HAIRMOD_PROTECTED.skillLevelLearnt[idSkill]])
					};
					break;
				case 4:
					result = new string[4]
					{
						"MP + HP tăng: " + SkillManager_HAIRMOD_PROTECTED.getSkillDamPercent(idSkill, Char_HAIRMOD_PROTECTED.skillLevelLearnt[idSkill], clazz) + " %",
						"Thời gian: " + SkillManager_HAIRMOD_PROTECTED.TIME_LIFE_BUFF_SKILL[array[clazz][idSkill - 4]][Char_HAIRMOD_PROTECTED.skillLevelLearnt[idSkill]] + "s",
						text,
						(Char_HAIRMOD_PROTECTED.skillLevelLearnt[idSkill] >= 9) ? string.Empty : ("Lv yêu cầu: " + SkillManager_HAIRMOD_PROTECTED.LEVEL_ADD_SKILL[idSkill][Char_HAIRMOD_PROTECTED.skillLevelLearnt[idSkill]])
					};
					break;
				case 6:
					result = new string[4]
					{
						"Hồi sinh",
						"HP hồi phục: " + SkillManager_HAIRMOD_PROTECTED.getSkillDamPercent(idSkill, Char_HAIRMOD_PROTECTED.skillLevelLearnt[idSkill], clazz) + "%",
						text,
						(Char_HAIRMOD_PROTECTED.skillLevelLearnt[idSkill] >= 9) ? string.Empty : ("Lv yêu cầu: " + SkillManager_HAIRMOD_PROTECTED.LEVEL_ADD_SKILL[idSkill][Char_HAIRMOD_PROTECTED.skillLevelLearnt[idSkill]])
					};
					break;
				case 7:
					result = new string[7]
					{
						"Khiên MP",
						"Chuyển 1 lượng hao tổn",
						"HP sang MP",
						"Tỷ lệ chuyển: " + SkillManager_HAIRMOD_PROTECTED.getSkillDamPercent(idSkill, Char_HAIRMOD_PROTECTED.skillLevelLearnt[idSkill], clazz) + "%",
						"Thời gian: " + SkillManager_HAIRMOD_PROTECTED.TIME_LIFE_BUFF_SKILL[array[clazz][idSkill - 4]][Char_HAIRMOD_PROTECTED.skillLevelLearnt[idSkill]] + "s",
						text,
						(Char_HAIRMOD_PROTECTED.skillLevelLearnt[idSkill] >= 9) ? string.Empty : ("Lv yêu cầu: " + SkillManager_HAIRMOD_PROTECTED.LEVEL_ADD_SKILL[idSkill][Char_HAIRMOD_PROTECTED.skillLevelLearnt[idSkill]])
					};
					break;
				}
				break;
			case 3:
				switch (idSkill)
				{
				case 5:
					result = new string[4]
					{
						"Tăng sức phòng thủ",
						"của bản thân",
						"Tỷ lệ tăng: " + SkillManager_HAIRMOD_PROTECTED.getSkillDamPercent(idSkill, Char_HAIRMOD_PROTECTED.skillLevelLearnt[idSkill], clazz) + "%",
						getLvRequest(idSkill)
					};
					break;
				case 4:
					result = new string[6]
					{
						"Gây choáng cho",
						"đối phương",
						"Thời gian: " + 5 + "s",
						"Tỷ lệ gây choáng: " + SkillManager_HAIRMOD_PROTECTED.getSkillDamPercent(idSkill, Char_HAIRMOD_PROTECTED.skillLevelLearnt[idSkill], clazz) + "%",
						text,
						getLvRequest(idSkill)
					};
					break;
				}
				break;
			case 4:
				switch (idSkill)
				{
				case 5:
					result = new string[3]
					{
						"Tăng độc tính sử dụng",
						"Tỷ lệ tăng: " + SkillManager_HAIRMOD_PROTECTED.getSkillDamPercent(idSkill, Char_HAIRMOD_PROTECTED.skillLevelLearnt[idSkill], clazz) + "%",
						getLvRequest(idSkill)
					};
					break;
				case 4:
					result = new string[5]
					{
						"Tẩm độc vào tên: ",
						"Độc tính: " + SkillManager_HAIRMOD_PROTECTED.getSkillDamPercent(idSkill, Char_HAIRMOD_PROTECTED.skillLevelLearnt[idSkill], clazz),
						"Thời gian: " + SkillManager_HAIRMOD_PROTECTED.getSkillDamPercent(idSkill, Char_HAIRMOD_PROTECTED.skillLevelLearnt[idSkill], clazz) + "s",
						text,
						getLvRequest(idSkill)
					};
					break;
				}
				break;
			}
		}
		return result;
	}

	public string getLvRequest(int idSkill)
	{
		return "Lv yêu cầu: " + SkillManager_HAIRMOD_PROTECTED.LEVEL_ADD_SKILL[idSkill][Char_HAIRMOD_PROTECTED.skillLevelLearnt[idSkill]];
	}

	public bool setWay(int vX, int vY)
	{
		short num = x;
		if (vX != 0)
		{
			if (!Tilemap_HAIRMOD_PROTECTED.tileTypeAtPixel(x + vX, y - 16, 2) && !Tilemap_HAIRMOD_PROTECTED.tileTypeAtPixel(x, y - 16, 2))
			{
				moveTo(num, (short)(y - 16));
				return true;
			}
			if (!Tilemap_HAIRMOD_PROTECTED.tileTypeAtPixel(x + vX, y + 16, 2) && !Tilemap_HAIRMOD_PROTECTED.tileTypeAtPixel(x, y + 16, 2))
			{
				moveTo(num, (short)(y + 16));
				return true;
			}
		}
		else if (vY != 0)
		{
			if (!Tilemap_HAIRMOD_PROTECTED.tileTypeAtPixel(x - 16, y + vY, 2) && !Tilemap_HAIRMOD_PROTECTED.tileTypeAtPixel(x - 16, y, 2))
			{
				moveTo((short)(x - 16), y);
				return true;
			}
			if (!Tilemap_HAIRMOD_PROTECTED.tileTypeAtPixel(x + 16, y + vY, 2) && !Tilemap_HAIRMOD_PROTECTED.tileTypeAtPixel(x + 16, y, 2))
			{
				moveTo((short)(x + 16), y);
				return true;
			}
		}
		return false;
	}
}
