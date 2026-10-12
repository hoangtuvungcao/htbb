using System;

public class ItemInInventory_HAIRMOD_PROTECTED
{
	private static string[][] groupKick = new string[13][]
	{
		new string[2] { "Quần", "Nhẫn dưới" },
		new string[2] { "Ngọc", "Găng" },
		new string[2] { "áo", "Dây chuyền" },
		new string[2] { "áo", "Dây chuyền" },
		new string[2] { "áo", "Dây chuyền" },
		new string[2] { "áo", "Dây chuyền" },
		new string[2] { "áo", "Dây chuyền" },
		new string[2] { "áo", "Dây chuyền" },
		new string[4] { "Nón", "Vũ khí", "Ngọc bội", "Găng" },
		new string[2] { "Nhẫn dưới", "Quần" },
		new string[2] { "Nón", "Vũ khí" },
		new string[2] { "Giày", "Nhẫn trên" },
		new string[2] { "Giày", "Nhẫn trên" }
	};

	private static string[][] kickHe = new string[5][]
	{
		new string[1] { "Kim" },
		new string[1] { "Thủy" },
		new string[1] { "Mộc" },
		new string[1] { "Hỏa" },
		new string[1] { "Thổ" }
	};

	private static string[][] groupKickAnimal = new string[5][]
	{
		new string[1] { "Nón" },
		new string[1] { "Bàn đạp" },
		new string[1] { "Hộ uyển" },
		new string[1] { "Yên cương" },
		new string[1] { "Giáp" }
	};

	public sbyte[] newAtb = new sbyte[10];

	public sbyte[] addMoreLevelSkill = new sbyte[15];

	public sbyte[] lockAtb = new sbyte[3];

	public string nameCharSeal = string.Empty;

	public string nameCharSell;

	public string nameCharBet;

	public sbyte catagory = 3;

	public sbyte isItemDrop;

	public short itemID;

	public short number;

	public short itemOldID;

	public sbyte potionType;

	public sbyte charClazz;

	public sbyte heItem = -1;

	public sbyte beKicked;

	public sbyte hangItem = -1;

	public sbyte magic_physic = 2;

	public short idTemplate;

	public short plusTemplate;

	public short[] itemAttribute = new short[10];

	public short durable;

	public short mDurable;

	public int dayUse;

	public long lastTime;

	public short level = -1;

	public bool isSelling;

	public bool isShopItem;

	public int prize;

	public int islock;

	public sbyte clazz = -1;

	public sbyte maxNgoc;

	public static readonly string[] attrName = new string[10] { "Tấn công", "Thủ vật", "Né tránh", "Chính xác", "Chí mạng", "Sức khoẻ", "Thủ ma", "Chỉ số 7", "Chỉ số 8", "Chỉ số 9" };

	public short[] attCreate;

	public bool isEnoughData;

	public bool isTrade;

	public short[] property;

	public mVector_HAIRMOD_PROTECTED allAttribute = new mVector_HAIRMOD_PROTECTED();

	public sbyte numKham;

	public sbyte totalKham;

	public sbyte colorName;

	public sbyte viTriVe;

	public static bool[][] attribShowedForType = new bool[20][]
	{
		new bool[10] { false, true, true, true, false, false, true, false, false, false },
		new bool[10] { false, true, true, true, false, false, true, false, false, false },
		new bool[10] { false, true, true, true, false, false, true, false, false, false },
		new bool[10] { true, false, false, true, true, false, false, false, false, false },
		new bool[10] { true, false, false, true, true, false, false, false, false, false },
		new bool[10] { true, false, false, true, true, false, false, false, false, false },
		new bool[10] { true, false, false, true, true, false, false, false, false, false },
		new bool[10] { true, false, false, true, true, false, false, false, false, false },
		new bool[10] { true, false, false, true, false, false, false, false, false, false },
		new bool[10] { true, false, false, false, true, false, false, false, false, false },
		new bool[10] { false, true, true, false, false, false, true, false, false, false },
		new bool[10] { false, true, false, true, true, false, true, false, false, false },
		new bool[10] { false, false, false, true, true, true, false, false, false, false },
		new bool[10] { true, false, false, false, false, false, false, false, false, false },
		new bool[10] { false, true, false, false, false, false, true, false, false, false },
		new bool[10] { true, false, false, true, false, false, false, false, false, false },
		new bool[10] { false, true, false, false, false, false, true, false, false, false },
		new bool[10] { true, false, false, false, true, false, false, false, false, false },
		new bool[10] { false, true, false, false, false, false, true, false, false, false },
		new bool[10] { false, true, false, false, false, false, true, false, false, false }
	};

	public short[] otherAtt;

	private static string[] na = new string[6]
	{
		"Giáp nhẹ",
		"Giáp nặng",
		"Giáp vải",
		"Giáp nặng",
		"Giáp nhẹ",
		string.Empty
	};

	private static string[] nameNguHanh = new string[7] { "Giảm st vật: ", "Giảm st ma: ", "Tăng tc: ", "Bỏ qua pt: ", "Phản st: ", "Bỏ qua tc ma", "Bỏ qua tc vật" };

	private static string[] nameHangItem = new string[6]
	{
		string.Empty,
		"nhất phẩm",
		"nhị phẩm",
		"tam phẩm",
		"tứ phẩm",
		"ngũ phẩm"
	};

	private static string[] he = new string[5] { "hệ thuỷ", "hệ mộc", "hệ hoả", "hệ thổ", "hệ kim" };

	private static string[] magic = new string[3]
	{
		"ma pháp",
		"vật lý",
		string.Empty
	};

	public static sbyte typeItemBuy = 2;

	private int count = Res_HAIRMOD_PROTECTED.rnd(68);

	public int prizeFix;

	public int prizeBet;

	public long timeEnd;

	public string daySell = string.Empty;

	private static int[] color = new int[3] { 7667443, 3334656, 16705024 };

	public sbyte kindItemInventory;

	public short[] getPoint()
	{
		return null;
	}

	public static bool isItemChar(int type)
	{
		return type < 13;
	}

	public static bool isModelClothe(int id)
	{
		return id == 1;
	}

	public static bool isArmor(int type)
	{
		return type < 3;
	}

	public bool isMyItem()
	{
		return nameCharSell.Equals(Canvas_HAIRMOD_PROTECTED.gameScr.mainChar.name);
	}

	public ItemTemplate_HAIRMOD_PROTECTED getTemplate()
	{
		return Res_HAIRMOD_PROTECTED.getItem(charClazz, idTemplate);
	}

	public string[] getName()
	{
		string[] array = new string[3]
		{
			string.Empty,
			string.Empty,
			string.Empty
		};
		if (catagory == 3)
		{
			ItemTemplate_HAIRMOD_PROTECTED item = Res_HAIRMOD_PROTECTED.getItem(charClazz, idTemplate);
			string text = string.Empty;
			if (plusTemplate > 0)
			{
				text = text + " +" + plusTemplate;
			}
			text = text + " " + magic[magic_physic];
			array[0] = colorName + item.name + ((plusTemplate <= 0) ? string.Empty : (" +" + plusTemplate));
			array[1] = "Giá bán: " + prize + " xu";
			array[2] = "Giá bid: " + prizeBet + " xu";
		}
		return array;
	}

	public static mVector_HAIRMOD_PROTECTED getAllItemCharDrop()
	{
		mVector_HAIRMOD_PROTECTED mVector_HAIRMOD_PROTECTED2 = new mVector_HAIRMOD_PROTECTED();
		for (int i = 0; i < Char_HAIRMOD_PROTECTED.inventory.size(); i++)
		{
			ItemInInventory_HAIRMOD_PROTECTED itemInInventory_HAIRMOD_PROTECTED = (ItemInInventory_HAIRMOD_PROTECTED)Char_HAIRMOD_PROTECTED.inventory.elementAt(i);
			ItemTemplate_HAIRMOD_PROTECTED item = Res_HAIRMOD_PROTECTED.getItem(itemInInventory_HAIRMOD_PROTECTED.charClazz, itemInInventory_HAIRMOD_PROTECTED.idTemplate);
			if (isItemChar(item.type) && itemInInventory_HAIRMOD_PROTECTED.dayUse == 0 && (itemInInventory_HAIRMOD_PROTECTED.isItemDrop == 1 || itemInInventory_HAIRMOD_PROTECTED.colorName != 0))
			{
				mVector_HAIRMOD_PROTECTED2.addElement(itemInInventory_HAIRMOD_PROTECTED);
			}
		}
		return mVector_HAIRMOD_PROTECTED2;
	}

	public string compareItem(ItemInInventory_HAIRMOD_PROTECTED it)
	{
		string text = string.Empty;
		if (catagory == 3)
		{
			string text2 = string.Empty;
			ItemTemplate_HAIRMOD_PROTECTED item = Res_HAIRMOD_PROTECTED.getItem(charClazz, idTemplate);
			if (!isModelClothe(item.attb[9]))
			{
				if (plusTemplate > 0)
				{
					text2 = text2 + " +" + plusTemplate;
				}
				text2 = text2 + " " + magic[magic_physic];
				if (hangItem > -1)
				{
					text2 = text2 + " " + nameHangItem[hangItem];
				}
				if (heItem > -1)
				{
					text2 = text2 + " " + he[heItem];
				}
				text += setTemp(item.name + text2, string.Empty + colorName);
				if (!nameCharSeal.Equals(string.Empty))
				{
					text = text + "\n0" + nameCharSeal.ToUpper() + " tạo";
				}
				if (islock == 1)
				{
					text += "\nĐã khoá";
				}
				if (isArmor(item.type) && item.type != 13 && !isAnimalArmor(item.type))
				{
					text = text + "\n" + na[clazz];
				}
				if (clazz != -1 && item.type != 13 && item.type != 19 && !isAnimalArmor(item.type))
				{
					text = text + "\n" + Res_HAIRMOD_PROTECTED.nameClazz[clazz];
				}
				if (item.level != -1)
				{
					text = text + "\nYêu cầu lv: " + level;
				}
				text = text + "\nĐộ bền: " + durable;
				for (byte b = 0; b < allAttribute.size(); b++)
				{
					InfoAttributeItem_HAIRMOD_PROTECTED infoAttributeItem_HAIRMOD_PROTECTED = (InfoAttributeItem_HAIRMOD_PROTECTED)allAttribute.elementAt(b);
					string temp = infoAttributeItem_HAIRMOD_PROTECTED.getName(clazz) + infoAttributeItem_HAIRMOD_PROTECTED.getValue() + ((!infoAttributeItem_HAIRMOD_PROTECTED.isPercent()) ? string.Empty : "%");
					text += setTemp(temp, infoAttributeItem_HAIRMOD_PROTECTED.getColorPaint(beKicked == 1) + string.Empty);
					for (int i = 0; i < it.allAttribute.size(); i++)
					{
						InfoAttributeItem_HAIRMOD_PROTECTED infoAttributeItem_HAIRMOD_PROTECTED2 = (InfoAttributeItem_HAIRMOD_PROTECTED)allAttribute.elementAt(b);
						if (infoAttributeItem_HAIRMOD_PROTECTED2.getID() == infoAttributeItem_HAIRMOD_PROTECTED.getID())
						{
							if (infoAttributeItem_HAIRMOD_PROTECTED2.getValueAtt() > infoAttributeItem_HAIRMOD_PROTECTED.getValueAtt())
							{
								text = text + "~1" + (infoAttributeItem_HAIRMOD_PROTECTED2.getValueAtt() - infoAttributeItem_HAIRMOD_PROTECTED.getValueAtt());
							}
							else if (infoAttributeItem_HAIRMOD_PROTECTED2.getValueAtt() > infoAttributeItem_HAIRMOD_PROTECTED.getValueAtt())
							{
								text = text + "~0" + (infoAttributeItem_HAIRMOD_PROTECTED.getValueAtt() - infoAttributeItem_HAIRMOD_PROTECTED2.getValueAtt());
							}
						}
					}
				}
				try
				{
					if (hangItem > 0 && hangItem < 5 && item.type != 13 && item.type < 13)
					{
						text = ((item.type != 8) ? (text + setTemp("cần " + groupKick[item.type][0] + " hoặc " + groupKick[item.type][1] + " hệ " + kickHe[heItem][0] + " để kích ngũ hành", "0")) : ((viTriVe != 1) ? (text + setTemp("Nhẫn dưới cần " + groupKick[item.type][2] + " hoặc " + groupKick[item.type][3] + " hệ " + kickHe[heItem][0] + " để kích ngũ hành", "0")) : (text + setTemp("Nhẫn trên cần " + groupKick[item.type][0] + " hoặc " + groupKick[item.type][1] + " hệ " + kickHe[heItem][0] + " để kích ngũ hành", "0"))));
					}
					if (hangItem > 0 && hangItem < 5 && isAnimalArmor(item.type))
					{
						text += setTemp("cần " + groupKickAnimal[item.type - 14][0] + " hệ " + kickHe[heItem][0] + " để kích ngũ hành", "0");
					}
				}
				catch (Exception)
				{
				}
			}
			else
			{
				text += item.name;
				string text3 = text;
				text = text3 + "\nTấn công tăng: " + item.attb[0] + "%";
				text3 = text;
				text = text3 + "\nThủ tăng: " + item.attb[1] + "%";
				text = text + "\nHP tăng: " + item.attb[2];
				text = text + "\nMP tăng: " + item.attb[3];
			}
			if (dayUse != 0)
			{
				long num = dayUse - (mSystem_HAIRMOD_PROTECTED.currentTimeMillis() - lastTime) / 60000;
				if (num < 0)
				{
					num = 1L;
				}
				text += setTemp("\nThời gian còn lại: " + ((num / 60 <= 0) ? string.Empty : (num / 60 + "h ")) + ((num % 60 <= 0) ? string.Empty : (num % 60 + "p")), 0 + string.Empty);
			}
			if (timeEnd > 0)
			{
				text += setTemp(nameCharSell, "0");
				text += setTemp(daySell, "0");
				text += setTemp("Bid: " + prizeBet + " xu", "0");
				text += setTemp((!nameCharBet.Equals(string.Empty)) ? (nameCharBet + " bid") : "----", "0");
				text += setTemp(getTimeSell(), 0 + string.Empty);
			}
		}
		return text;
	}

	public string getDescription(bool showResellPrice)
	{
		string text = string.Empty;
		if (catagory == 3)
		{
			string text2 = string.Empty;
			ItemTemplate_HAIRMOD_PROTECTED item = Res_HAIRMOD_PROTECTED.getItem(charClazz, idTemplate);
			if (!isModelClothe(item.attb[9]))
			{
				if (plusTemplate > 0)
				{
					text2 = text2 + " +" + plusTemplate;
				}
				text2 = text2 + " " + magic[magic_physic];
				if (hangItem > -1)
				{
					text2 = text2 + " " + nameHangItem[hangItem];
				}
				if (heItem > -1)
				{
					text2 = text2 + " " + he[heItem];
				}
				text += setTemp(item.name + text2, string.Empty + colorName);
				if (!nameCharSeal.Equals(string.Empty))
				{
					text = text + "\n0" + nameCharSeal.ToLower() + ((nameCharSeal.IndexOf("-") <= -1) ? " tạo" : string.Empty);
				}
				if (islock == 1)
				{
					text += "\nĐã khoá";
				}
				if (isArmor(item.type) && item.type != 13 && !isAnimalArmor(item.type))
				{
					text = text + "\n" + na[clazz];
				}
				if (clazz != -1 && item.type != 13 && item.type != 19 && !isAnimalArmor(item.type))
				{
					text = text + "\n" + Res_HAIRMOD_PROTECTED.nameClazz[clazz];
				}
				if (isSelling)
				{
					text = text + "\nGiá bán: " + Canvas_HAIRMOD_PROTECTED.getMoneys(prize) + "~";
				}
				else if (showResellPrice)
				{
					text = text + "\nGiá bán lại: " + Canvas_HAIRMOD_PROTECTED.getMoneys(item.price / 5 + plusTemplate * 1000) + "~";
				}
				if (item.level != -1)
				{
					text = text + "\nYêu cầu lv: " + level;
				}
				text = text + "\nĐộ bền: " + durable;
				for (sbyte b = 0; b < allAttribute.size(); b++)
				{
					InfoAttributeItem_HAIRMOD_PROTECTED infoAttributeItem_HAIRMOD_PROTECTED = (InfoAttributeItem_HAIRMOD_PROTECTED)allAttribute.elementAt(b);
					string temp = infoAttributeItem_HAIRMOD_PROTECTED.getName(clazz) + infoAttributeItem_HAIRMOD_PROTECTED.getValue() + ((!infoAttributeItem_HAIRMOD_PROTECTED.isPercent()) ? string.Empty : "%");
					text += setTemp(temp, infoAttributeItem_HAIRMOD_PROTECTED.getColorPaint(beKicked == 1) + string.Empty);
				}
				try
				{
					if (hangItem > 0 && hangItem < 5 && item.type != 13 && item.type < 13)
					{
						text = ((item.type != 8) ? (text + setTemp("cần " + groupKick[item.type][0] + " hoặc " + groupKick[item.type][1] + " hệ " + kickHe[heItem][0] + " để kích ngũ hành", "0")) : ((viTriVe != 1) ? (text + setTemp("Nhẫn dưới cần " + groupKick[item.type][2] + " hoặc " + groupKick[item.type][3] + " hệ " + kickHe[heItem][0] + " để kích ngũ hành", "0")) : (text + setTemp("Nhẫn trên cần " + groupKick[item.type][0] + " hoặc " + groupKick[item.type][1] + " hệ " + kickHe[heItem][0] + " để kích ngũ hành", "0"))));
					}
					if (hangItem > 0 && hangItem < 5 && isAnimalArmor(item.type))
					{
						text += setTemp("cần " + groupKickAnimal[item.type - 14][0] + " hệ " + kickHe[heItem][0] + " để kích ngũ hành", "0");
					}
				}
				catch (Exception)
				{
				}
			}
			else
			{
				text += item.name;
				if (!nameCharSeal.Equals(string.Empty))
				{
					text = text + "\n0" + nameCharSeal.ToLower() + ((nameCharSeal.IndexOf("-") <= -1) ? " tạo" : string.Empty);
				}
				for (byte b2 = 0; b2 < allAttribute.size(); b2++)
				{
					InfoAttributeItem_HAIRMOD_PROTECTED infoAttributeItem_HAIRMOD_PROTECTED2 = (InfoAttributeItem_HAIRMOD_PROTECTED)allAttribute.elementAt(b2);
					string temp2 = infoAttributeItem_HAIRMOD_PROTECTED2.getName(clazz) + infoAttributeItem_HAIRMOD_PROTECTED2.getValue() + ((!infoAttributeItem_HAIRMOD_PROTECTED2.isPercent()) ? string.Empty : "%");
					text += setTemp(temp2, infoAttributeItem_HAIRMOD_PROTECTED2.getColorPaint(beKicked == 1) + string.Empty);
				}
			}
			if (dayUse != 0)
			{
				long num = dayUse - (mSystem_HAIRMOD_PROTECTED.currentTimeMillis() - lastTime) / 60000;
				if (num < 0)
				{
					num = 1L;
				}
				text += setTemp("\nThời gian còn lại: " + ((num / 60 <= 0) ? string.Empty : (num / 60 + "h ")) + ((num % 60 <= 0) ? string.Empty : (num % 60 + "p")), 0 + string.Empty);
			}
			if (timeEnd > 0)
			{
				text += setTemp(nameCharSell, "0");
				text += setTemp(daySell, "0");
				text += setTemp("Bid: " + prizeBet + " xu", "0");
				text += setTemp((!nameCharBet.Equals(string.Empty)) ? (nameCharBet + " bid") : "----", "0");
				text += setTemp(getTimeSell(), 0 + string.Empty);
			}
		}
		return text;
	}

	public static string setTemp(string temp, string typeFont)
	{
		string text = string.Empty;
		string[] array = MFont_HAIRMOD_PROTECTED.borderFont.splitFontBStrInLine(temp, 95);
		for (int i = 0; i < array.Length; i++)
		{
			text = text + "\n" + typeFont + array[i];
		}
		return text;
	}

	public static bool isAnimalArmor(int type)
	{
		return type >= 14 && type <= 18;
	}

	public string getTemplateDescription()
	{
		string text = string.Empty;
		try
		{
			string[] array = new string[5] { "Giáp nhẹ", "Giáp nặng", "Giáp vải", "Giáp nặng", "Giáp nhẹ" };
			if (catagory == 3)
			{
				string text2 = string.Empty;
				ItemTemplate_HAIRMOD_PROTECTED item = Res_HAIRMOD_PROTECTED.getItem(charClazz, idTemplate);
				if (idTemplate < 264 || idTemplate > 267)
				{
					if (plusTemplate > 0)
					{
						text2 = text2 + "+" + plusTemplate;
					}
					text = text + "\n" + item.name;
					text = text + " " + text2;
					if ((item.id < 79 || item.id > 137) && item.type != 13 && isAnimalArmor(item.type))
					{
						text = text + "\n" + array[clazz];
					}
					if (item.id > 113 && item.id <= 137 && item.type != 13)
					{
						text = text + "\n" + Res_HAIRMOD_PROTECTED.nameClazz[clazz];
					}
					text = ((item.ndayLoan != 0) ? (text + "\nGiá: " + Canvas_HAIRMOD_PROTECTED.getMoneys(item.price + plusTemplate) + " Lượng") : (text + "\nGiá: " + Canvas_HAIRMOD_PROTECTED.getMoneys(item.price + plusTemplate * 1000) + " xu"));
					if (level != -1)
					{
						text = text + "\nYêu cầu lv: " + level;
					}
					text = text + "\nĐộ bền: " + durable;
					if (item.clazz != -1 && item.type != 13)
					{
						text = text + "\nDành cho: " + CreateCharScr_HAIRMOD_PROTECTED.clazz[clazz][CreateCharScr_HAIRMOD_PROTECTED.selectGender].name;
					}
					int[] attributeItem = Res_HAIRMOD_PROTECTED.getAttributeItem(charClazz, idTemplate, item.type);
					if (item.type < 2 || item.type == 10 || item.type == 11 || isAnimalArmor(item.type))
					{
						if (typeItemBuy == 0)
						{
							attributeItem[6] = attributeItem[1];
							attributeItem[1] /= 10;
						}
						else
						{
							attributeItem[6] = attributeItem[1] / 10;
						}
					}
					for (int i = 0; i < 10; i++)
					{
						if (attribShowedForType[item.type][i])
						{
							string text3 = text;
							text = text3 + "\n" + attrName[i] + ": " + attributeItem[i];
						}
					}
				}
				else
				{
					text += item.name;
					string text4 = text;
					text = text4 + "\nTấn công tăng: " + item.attb[0] + "%";
					text4 = text;
					text = text4 + "\nThủ tăng: " + item.attb[1] + "%";
					text = text + "\nHP tăng: " + item.attb[2];
					text = text + "\nMP tăng: " + item.attb[3];
				}
				if (item.ndayLoan > 0)
				{
					text += setTemp("\nThời hạn: " + item.ndayLoan + " Phút.", "0");
				}
			}
			if (catagory == 4 && isShopItem)
			{
				text = string.Empty + MainChar_HAIRMOD_PROTECTED.listPotion[potionType].name;
				text = text + "\nGiá: " + Canvas_HAIRMOD_PROTECTED.getMoneys(Res_HAIRMOD_PROTECTED.potionTemplates[potionType].price) + "$";
				if (Res_HAIRMOD_PROTECTED.potionTemplates[potionType].recovered >= 80)
				{
					string text5 = text;
					text = text5 + "\nTăng " + Res_HAIRMOD_PROTECTED.potionTemplates[potionType].recovered + " " + MainChar_HAIRMOD_PROTECTED.listPotion[potionType].name2;
				}
				else if (Res_HAIRMOD_PROTECTED.potionTemplates[potionType].recovered == 0)
				{
					text += "\nKhăn đeo dùng khi đánh nhau.";
				}
				else if (Res_HAIRMOD_PROTECTED.potionTemplates[potionType].recovered == -1)
				{
					text += "\nDịch chuyển tức thời về làng.";
				}
				int num = Canvas_HAIRMOD_PROTECTED.gameScr.mainChar.potions[potionType];
				int num2 = 0;
				for (int j = 0; j < WindowInfoScr_HAIRMOD_PROTECTED.gI().readyBuy.size(); j++)
				{
					ItemInInventory_HAIRMOD_PROTECTED itemInInventory_HAIRMOD_PROTECTED = (ItemInInventory_HAIRMOD_PROTECTED)WindowInfoScr_HAIRMOD_PROTECTED.gI().readyBuy.elementAt(j);
					if (itemInInventory_HAIRMOD_PROTECTED.potionType == potionType)
					{
						num2 = itemInInventory_HAIRMOD_PROTECTED.number;
					}
				}
				if (num2 == 0)
				{
					string text6 = text;
					text = text6 + "\nĐang có: " + num + " cái.";
				}
				else
				{
					string text7 = text;
					text = text7 + "\nĐang có: " + num + "+" + num2 + " cái.";
				}
				if (num + num2 >= WindowInfoScr_HAIRMOD_PROTECTED.MAX_POTION)
				{
					text += "\n(tối đa)";
				}
			}
		}
		catch (Exception)
		{
		}
		return text;
	}

	public string getInfoItemUserSell()
	{
		string empty = string.Empty;
		string text = string.Empty;
		ItemTemplate_HAIRMOD_PROTECTED item = Res_HAIRMOD_PROTECTED.getItem(charClazz, idTemplate);
		if (plusTemplate > 0)
		{
			text = text + " +" + plusTemplate;
		}
		text = text + " " + magic[magic_physic];
		if (hangItem > -1)
		{
			text = text + " " + nameHangItem[hangItem];
		}
		if (heItem > -1)
		{
			text = text + " " + he[heItem];
		}
		empty += setTemp(item.name + text, string.Empty + colorName);
		if (!nameCharSeal.Equals(string.Empty))
		{
			empty = empty + "\n0" + nameCharSeal.ToUpper() + " tạo";
		}
		if (level != -1)
		{
			empty = empty + "\nYêu cầu lv: " + level;
		}
		empty = empty + "\nĐộ bền: " + durable;
		if (clazz != -1)
		{
			empty = empty + "\nDòng: " + CreateCharScr_HAIRMOD_PROTECTED.clazz[clazz][CreateCharScr_HAIRMOD_PROTECTED.selectGender].name;
		}
		for (sbyte b = 0; b < allAttribute.size(); b++)
		{
			InfoAttributeItem_HAIRMOD_PROTECTED infoAttributeItem_HAIRMOD_PROTECTED = (InfoAttributeItem_HAIRMOD_PROTECTED)allAttribute.elementAt(b);
			string temp = infoAttributeItem_HAIRMOD_PROTECTED.getName(clazz) + infoAttributeItem_HAIRMOD_PROTECTED.getValue() + ((!infoAttributeItem_HAIRMOD_PROTECTED.isPercent()) ? string.Empty : "%");
			empty += setTemp(temp, infoAttributeItem_HAIRMOD_PROTECTED.getColorPaint(false) + string.Empty);
		}
		if (prize > 0)
		{
			empty = empty + "\nGiá bán:\n0" + Canvas_HAIRMOD_PROTECTED.getMoneys(prize);
			Out_HAIRMOD_PROTECTED.println(prize + " GIA BAN " + Canvas_HAIRMOD_PROTECTED.getMoneys(prize));
		}
		return empty;
	}

	public static mVector_HAIRMOD_PROTECTED getAllItemChar()
	{
		mVector_HAIRMOD_PROTECTED mVector_HAIRMOD_PROTECTED2 = new mVector_HAIRMOD_PROTECTED();
		for (int i = 0; i < Char_HAIRMOD_PROTECTED.inventory.size(); i++)
		{
			ItemInInventory_HAIRMOD_PROTECTED itemInInventory_HAIRMOD_PROTECTED = (ItemInInventory_HAIRMOD_PROTECTED)Char_HAIRMOD_PROTECTED.inventory.elementAt(i);
			ItemTemplate_HAIRMOD_PROTECTED item = Res_HAIRMOD_PROTECTED.getItem(itemInInventory_HAIRMOD_PROTECTED.charClazz, itemInInventory_HAIRMOD_PROTECTED.idTemplate);
			if (isItemChar(item.type) && itemInInventory_HAIRMOD_PROTECTED.dayUse == 0)
			{
				mVector_HAIRMOD_PROTECTED2.addElement(itemInInventory_HAIRMOD_PROTECTED);
			}
		}
		return mVector_HAIRMOD_PROTECTED2;
	}

	public static mVector_HAIRMOD_PROTECTED getAllItem(int colorItem)
	{
		mVector_HAIRMOD_PROTECTED mVector_HAIRMOD_PROTECTED2 = new mVector_HAIRMOD_PROTECTED();
		for (int i = 0; i < Char_HAIRMOD_PROTECTED.inventory.size(); i++)
		{
			ItemInInventory_HAIRMOD_PROTECTED itemInInventory_HAIRMOD_PROTECTED = (ItemInInventory_HAIRMOD_PROTECTED)Char_HAIRMOD_PROTECTED.inventory.elementAt(i);
			if (itemInInventory_HAIRMOD_PROTECTED.colorName == colorItem)
			{
				mVector_HAIRMOD_PROTECTED2.addElement(itemInInventory_HAIRMOD_PROTECTED);
			}
		}
		return mVector_HAIRMOD_PROTECTED2;
	}

	public static mVector_HAIRMOD_PROTECTED getAllItemCharExcepColor(int colorItem)
	{
		mVector_HAIRMOD_PROTECTED mVector_HAIRMOD_PROTECTED2 = new mVector_HAIRMOD_PROTECTED();
		for (int i = 0; i < Char_HAIRMOD_PROTECTED.inventory.size(); i++)
		{
			ItemInInventory_HAIRMOD_PROTECTED itemInInventory_HAIRMOD_PROTECTED = (ItemInInventory_HAIRMOD_PROTECTED)Char_HAIRMOD_PROTECTED.inventory.elementAt(i);
			if (itemInInventory_HAIRMOD_PROTECTED.colorName != colorItem && isItemChar(itemInInventory_HAIRMOD_PROTECTED.getTemplate().type))
			{
				mVector_HAIRMOD_PROTECTED2.addElement(itemInInventory_HAIRMOD_PROTECTED);
			}
		}
		return mVector_HAIRMOD_PROTECTED2;
	}

	public static mVector_HAIRMOD_PROTECTED getAllItemAnimal(int colorItem, int hang)
	{
		mVector_HAIRMOD_PROTECTED mVector_HAIRMOD_PROTECTED2 = new mVector_HAIRMOD_PROTECTED();
		for (int i = 0; i < Char_HAIRMOD_PROTECTED.inventory.size(); i++)
		{
			ItemInInventory_HAIRMOD_PROTECTED itemInInventory_HAIRMOD_PROTECTED = (ItemInInventory_HAIRMOD_PROTECTED)Char_HAIRMOD_PROTECTED.inventory.elementAt(i);
			if (itemInInventory_HAIRMOD_PROTECTED.colorName == colorItem && itemInInventory_HAIRMOD_PROTECTED.hangItem == hang && isAnimalArmor(itemInInventory_HAIRMOD_PROTECTED.getTemplate().type))
			{
				mVector_HAIRMOD_PROTECTED2.addElement(itemInInventory_HAIRMOD_PROTECTED);
			}
		}
		return mVector_HAIRMOD_PROTECTED2;
	}

	public static mVector_HAIRMOD_PROTECTED getAllItemCanSell()
	{
		mVector_HAIRMOD_PROTECTED mVector_HAIRMOD_PROTECTED2 = new mVector_HAIRMOD_PROTECTED();
		for (int i = 0; i < Char_HAIRMOD_PROTECTED.inventory.size(); i++)
		{
			ItemInInventory_HAIRMOD_PROTECTED itemInInventory_HAIRMOD_PROTECTED = (ItemInInventory_HAIRMOD_PROTECTED)Char_HAIRMOD_PROTECTED.inventory.elementAt(i);
			ItemTemplate_HAIRMOD_PROTECTED item = Res_HAIRMOD_PROTECTED.getItem(itemInInventory_HAIRMOD_PROTECTED.charClazz, itemInInventory_HAIRMOD_PROTECTED.idTemplate);
			if (!isModelClothe(item.attb[9]) && itemInInventory_HAIRMOD_PROTECTED.islock == 0 && itemInInventory_HAIRMOD_PROTECTED.dayUse == 0)
			{
				mVector_HAIRMOD_PROTECTED2.addElement(itemInInventory_HAIRMOD_PROTECTED);
			}
		}
		return mVector_HAIRMOD_PROTECTED2;
	}

	public static mVector_HAIRMOD_PROTECTED getAllItem()
	{
		mVector_HAIRMOD_PROTECTED mVector_HAIRMOD_PROTECTED2 = new mVector_HAIRMOD_PROTECTED();
		for (int i = 0; i < Char_HAIRMOD_PROTECTED.inventory.size(); i++)
		{
			ItemInInventory_HAIRMOD_PROTECTED itemInInventory_HAIRMOD_PROTECTED = (ItemInInventory_HAIRMOD_PROTECTED)Char_HAIRMOD_PROTECTED.inventory.elementAt(i);
			ItemTemplate_HAIRMOD_PROTECTED item = Res_HAIRMOD_PROTECTED.getItem(itemInInventory_HAIRMOD_PROTECTED.charClazz, itemInInventory_HAIRMOD_PROTECTED.idTemplate);
			if (!isModelClothe(item.attb[9]))
			{
				mVector_HAIRMOD_PROTECTED2.addElement(itemInInventory_HAIRMOD_PROTECTED);
			}
		}
		return mVector_HAIRMOD_PROTECTED2;
	}

	public void paint(MyGraphics_HAIRMOD_PROTECTED g, int x, int y)
	{
		ItemTemplate_HAIRMOD_PROTECTED item = Res_HAIRMOD_PROTECTED.getItem(charClazz, idTemplate);
		GameData_HAIRMOD_PROTECTED.paintIcon(g, item.idIcon, x, y);
		if (plusTemplate > 12)
		{
			Res_HAIRMOD_PROTECTED.paintRectColor(g, x - 9, y - 9, 17, 17, count, color[plusTemplate - 13], 16516369);
			count += 3;
			if (count > 68)
			{
				count = 0;
			}
		}
	}

	public static bool isThanThu(int type)
	{
		return type == 25;
	}

	public string getTimeSell()
	{
		long num = timeEnd - mSystem_HAIRMOD_PROTECTED.currentTimeMillis();
		if (num <= 0)
		{
			return "Hết giờ";
		}
		if (num > 0 && num / 60000 == 0)
		{
			return (num <= 0) ? string.Empty : ("Còn lại: " + num / 1000 + " giây");
		}
		return (num <= 0) ? string.Empty : ("Còn lại: " + num / 60000 + " phút");
	}
}
