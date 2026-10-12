using System;

public class MessageHandler_HAIRMOD_PROTECTED : Cmd_message_HAIRMOD_PROTECTED, IMessageHandler_HAIRMOD_PROTECTED
{
	protected static MessageHandler_HAIRMOD_PROTECTED instance;

	public static MessageHandler_HAIRMOD_PROTECTED gI()
	{
		if (instance == null)
		{
			instance = new MessageHandler_HAIRMOD_PROTECTED();
		}
		return instance;
	}

	public void onConnectOK()
	{
		Out_HAIRMOD_PROTECTED.println("Conect Ok .");
		Canvas_HAIRMOD_PROTECTED.gameScr.onConnectOK();
	}

	public void onConnectionFail()
	{
		Out_HAIRMOD_PROTECTED.println("Conect fail");
		Canvas_HAIRMOD_PROTECTED.gameScr.onConnectFail();
	}

	public void onDisconnected()
	{
		Out_HAIRMOD_PROTECTED.println("Conect stop");
		Canvas_HAIRMOD_PROTECTED.gameScr.onDisconnect();
	}

	public void onMessage(Message_HAIRMOD_PROTECTED msg)
	{
		try
		{
			ItemDropInfo_HAIRMOD_PROTECTED[] array = null;
			switch (msg.command)
			{
			case -76:
				Canvas_HAIRMOD_PROTECTED.gameScr.onShopNew(msg);
				break;
			case -75:
				Canvas_HAIRMOD_PROTECTED.gameScr.onCountDown(msg);
				break;
			case -74:
				Canvas_HAIRMOD_PROTECTED.gameScr.onEffData(msg);
				break;
			case -73:
				Canvas_HAIRMOD_PROTECTED.gameScr.onPetAttack(msg);
				break;
			case 62:
				Canvas_HAIRMOD_PROTECTED.gameScr.onItemShopOnline(msg);
				break;
			case -68:
				Canvas_HAIRMOD_PROTECTED.gameScr.onTachNguyenLieu(msg);
				break;
			case -69:
				Canvas_HAIRMOD_PROTECTED.gameScr.onAutoImbue(msg);
				break;
			case -70:
				Canvas_HAIRMOD_PROTECTED.gameScr.onNewEffect(msg);
				break;
			case -71:
				Canvas_HAIRMOD_PROTECTED.gameScr.onDynamicEffect(msg);
				break;
			case -66:
				Canvas_HAIRMOD_PROTECTED.gameScr.onFruit(msg);
				break;
			case -67:
				Canvas_HAIRMOD_PROTECTED.gameScr.onTuBinh(msg);
				break;
			case -64:
				Canvas_HAIRMOD_PROTECTED.gameScr.onQuest(msg);
				break;
			case 104:
				try
				{
					GameScr_HAIRMOD_PROTECTED.loadConfig(msg.reader().readByte());
					int num135 = msg.reader().readByte();
					break;
				}
				catch (Exception ex26)
				{
					Out_HAIRMOD_PROTECTED.println("LOI CHO NAY NE " + ex26.StackTrace);
					break;
				}
			case 103:
				Canvas_HAIRMOD_PROTECTED.gameScr.onSMSStruct(msg);
				break;
			case 102:
				Canvas_HAIRMOD_PROTECTED.gameScr.onFriendList(msg, 0, "BẠN BÈ");
				break;
			case 101:
				Canvas_HAIRMOD_PROTECTED.gameScr.onInfoFriend(msg);
				break;
			case 95:
			{
				short itemID = msg.reader().readShort();
				short charID2 = msg.reader().readShort();
				sbyte type3 = msg.reader().readByte();
				Canvas_HAIRMOD_PROTECTED.gameScr.onBuyItemOfUser(itemID, charID2, type3);
				break;
			}
			case 94:
				Canvas_HAIRMOD_PROTECTED.gameScr.onListInfoDepositeItem(msg);
				break;
			case 92:
				Canvas_HAIRMOD_PROTECTED.gameScr.onUserSellItem(msg);
				break;
			case 91:
				break;
			case 72:
				Canvas_HAIRMOD_PROTECTED.startOKDlg("Đã sửa xong.");
				break;
			case 89:
				Canvas_HAIRMOD_PROTECTED.gameScr.onBuffAttack(msg);
				break;
			case 90:
				Canvas_HAIRMOD_PROTECTED.gameScr.onActorDie(msg);
				break;
			case 82:
				Canvas_HAIRMOD_PROTECTED.currentDialog = null;
				if (Canvas_HAIRMOD_PROTECTED.gameScr.mainChar.delay <= 0)
				{
					Canvas_HAIRMOD_PROTECTED.gameScr.mainChar.delay = msg.reader().readByte();
					Canvas_HAIRMOD_PROTECTED.gameScr.mainChar.realTime = Canvas_HAIRMOD_PROTECTED.gameScr.mainChar.delay;
					Canvas_HAIRMOD_PROTECTED.gameScr.mainChar.timeWait2Board = mSystem_HAIRMOD_PROTECTED.getCurrentTimeMillis();
				}
				break;
			case 81:
				Canvas_HAIRMOD_PROTECTED.startOKDlg("Đã mua được vé");
				break;
			case 80:
			{
				Res_HAIRMOD_PROTECTED.XAPHU = new XaphuTemplate_HAIRMOD_PROTECTED[msg.reader().readByte()];
				for (int num8 = 0; num8 < Res_HAIRMOD_PROTECTED.XAPHU.Length; num8++)
				{
					Res_HAIRMOD_PROTECTED.XAPHU[num8] = new XaphuTemplate_HAIRMOD_PROTECTED();
					int num9 = msg.reader().readByte();
					Res_HAIRMOD_PROTECTED.XAPHU[num8].mapID = new int[num9];
					for (int num10 = 0; num10 < Res_HAIRMOD_PROTECTED.XAPHU[num8].mapID.Length; num10++)
					{
						Res_HAIRMOD_PROTECTED.XAPHU[num8].mapID[num10] = msg.reader().readByte();
					}
					Res_HAIRMOD_PROTECTED.XAPHU[num8].x = new int[num9];
					for (int num11 = 0; num11 < Res_HAIRMOD_PROTECTED.XAPHU[num8].x.Length; num11++)
					{
						Res_HAIRMOD_PROTECTED.XAPHU[num8].x[num11] = msg.reader().readShort();
					}
					Res_HAIRMOD_PROTECTED.XAPHU[num8].y = new int[num9];
					for (int num12 = 0; num12 < Res_HAIRMOD_PROTECTED.XAPHU[num8].y.Length; num12++)
					{
						Res_HAIRMOD_PROTECTED.XAPHU[num8].y[num12] = msg.reader().readShort();
					}
					Res_HAIRMOD_PROTECTED.XAPHU[num8].price = new int[num9];
					for (int num13 = 0; num13 < Res_HAIRMOD_PROTECTED.XAPHU[num8].price.Length; num13++)
					{
						Res_HAIRMOD_PROTECTED.XAPHU[num8].price[num13] = msg.reader().readShort();
					}
				}
				break;
			}
			case 77:
				Canvas_HAIRMOD_PROTECTED.gameScr.onImbueOK(msg.reader().readUTF());
				break;
			case -35:
				Canvas_HAIRMOD_PROTECTED.gameScr.onImbueOK(msg.reader().readUTF());
				break;
			case -36:
				if (msg.reader().readByte() == 0)
				{
					Canvas_HAIRMOD_PROTECTED.gameScr.onEpNgoc(msg.reader().readShort());
				}
				else
				{
					Canvas_HAIRMOD_PROTECTED.gameScr.onImbueOK(msg.reader().readUTF());
				}
				break;
			case 85:
				Canvas_HAIRMOD_PROTECTED.startOKDlg("Đã mua, món đồ đang ở trong hành trang.");
				break;
			case 86:
				switch (msg.reader().readByte())
				{
				case 1:
					Canvas_HAIRMOD_PROTECTED.gameScr.mainChar.timeUseGolgTicket = mSystem_HAIRMOD_PROTECTED.getCurrentTimeMillis();
					Char_HAIRMOD_PROTECTED.idGoldTicket = msg.reader().readByte() - 1;
					Char_HAIRMOD_PROTECTED.goldTimeMinute = 1440;
					Char_HAIRMOD_PROTECTED.goldTime = msg.reader().readUTF();
					break;
				case 0:
					Canvas_HAIRMOD_PROTECTED.gameScr.mainChar.timeUseGolgTicket = 0L;
					Char_HAIRMOD_PROTECTED.goldTimeMinute = 0;
					Char_HAIRMOD_PROTECTED.goldTime = string.Empty;
					break;
				case 2:
					Char_HAIRMOD_PROTECTED.idGoldTicket = msg.reader().readByte() - 1;
					Canvas_HAIRMOD_PROTECTED.gameScr.mainChar.timeUseGolgTicket = mSystem_HAIRMOD_PROTECTED.getCurrentTimeMillis();
					Char_HAIRMOD_PROTECTED.goldTimeMinute = msg.reader().readInt();
					Char_HAIRMOD_PROTECTED.goldTime = msg.reader().readUTF();
					break;
				}
				break;
			case 73:
				Canvas_HAIRMOD_PROTECTED.gameScr.onGemItem(msg);
				break;
			case 76:
				Canvas_HAIRMOD_PROTECTED.gameScr.onSpecialItem(msg);
				break;
			case 71:
			{
				msg.reader().readByte();
				short priceImbue = msg.reader().readShort();
				sbyte khoa3 = 0;
				try
				{
					khoa3 = msg.reader().readByte();
				}
				catch (Exception)
				{
				}
				Canvas_HAIRMOD_PROTECTED.gameScr.onImbue(priceImbue, khoa3);
				break;
			}
			case -34:
			{
				short price = msg.reader().readShort();
				sbyte khoa4 = 0;
				try
				{
					khoa4 = msg.reader().readByte();
				}
				catch (Exception)
				{
				}
				Canvas_HAIRMOD_PROTECTED.gameScr.onKham(price, khoa4);
				break;
			}
			case 68:
				Canvas_HAIRMOD_PROTECTED.gameScr.onPutItem2Bag(msg.reader().readShort());
				Canvas_HAIRMOD_PROTECTED.endDlg();
				break;
			case 69:
				Canvas_HAIRMOD_PROTECTED.gameScr.onGetItemFromBag(msg.reader().readShort());
				Canvas_HAIRMOD_PROTECTED.endDlg();
				break;
			case 66:
				switch (msg.reader().readByte())
				{
				case 0:
					Canvas_HAIRMOD_PROTECTED.gameScr.onRequestTrade(msg.reader().readShort());
					break;
				case -2:
					Canvas_HAIRMOD_PROTECTED.startOKDlg("Người bạn mời đang trao đổi với người khác");
					break;
				case 1:
					Canvas_HAIRMOD_PROTECTED.gameScr.onOkTrade(msg.reader().readShort());
					break;
				case -1:
					Canvas_HAIRMOD_PROTECTED.gameScr.onDeniedTrade(msg.reader().readShort());
					break;
				case 2:
				{
					sbyte type5 = msg.reader().readByte();
					Canvas_HAIRMOD_PROTECTED.gameScr.onItemTrade(msg.reader().readShort(), msg, type5);
					break;
				}
				case 3:
					Canvas_HAIRMOD_PROTECTED.gameScr.oncancelTrade();
					break;
				case 4:
					Canvas_HAIRMOD_PROTECTED.gameScr.onFinishTrade();
					break;
				case 5:
					Canvas_HAIRMOD_PROTECTED.gameScr.onConfirmTrade();
					break;
				}
				break;
			case 67:
				Canvas_HAIRMOD_PROTECTED.gameScr.onKiller(msg.reader().readShort(), msg.reader().readByte(), msg.reader().readShort());
				break;
			case 60:
				Canvas_HAIRMOD_PROTECTED.gameScr.onNewHPMP(msg);
				break;
			case 65:
				Canvas_HAIRMOD_PROTECTED.gameScr.onUseItemPK(msg.reader().readShort(), msg.reader().readByte(), msg.reader().readByte());
				break;
			case 2:
			{
				string text13 = msg.reader().readUTF();
				if (text13.StartsWith("2"))
				{
					if (!Main_HAIRMOD_PROTECTED.isDisConnect)
					{
						string[] array16 = new string[msg.reader().readByte()];
						short[] array17 = new short[array16.Length];
						string[] array18 = new string[array16.Length];
						for (int num71 = 0; num71 < array16.Length; num71++)
						{
							array16[num71] = msg.reader().readUTF();
							array17[num71] = msg.reader().readShort();
							array18[num71] = msg.reader().readUTF();
						}
						Canvas_HAIRMOD_PROTECTED.gameScr.listServer(text13.Substring(1), array16, array17, array18);
						break;
					}
					Command_HAIRMOD_PROTECTED command_HAIRMOD_PROTECTED4 = new Command_HAIRMOD_PROTECTED("OK");
					ActionPerform_HAIRMOD_PROTECTED action2 = delegate
					{
						Canvas_HAIRMOD_PROTECTED.endDlg();
						Canvas_HAIRMOD_PROTECTED.instance.init();
						Session_ME_HAIRMOD_PROTECTED.gI().close();
						Tilemap_HAIRMOD_PROTECTED.loadMap(0, null);
						ServerListScr_HAIRMOD_PROTECTED.gI().switchToMe();
					};
					command_HAIRMOD_PROTECTED4.action = action2;
					Canvas_HAIRMOD_PROTECTED.msgdlg.setInfo(text13.Substring(1), null, command_HAIRMOD_PROTECTED4, null);
					Canvas_HAIRMOD_PROTECTED.currentDialog = Canvas_HAIRMOD_PROTECTED.msgdlg;
				}
				else if (text13.StartsWith("3"))
				{
					string text14 = msg.reader().readUTF();
					string text15 = msg.reader().readUTF();
					GameMidlet_HAIRMOD_PROTECTED.provider = text14;
					GameMidlet_HAIRMOD_PROTECTED.agent = text15;
					RMS_HAIRMOD_PROTECTED.saveRMSString("provider", text14);
					RMS_HAIRMOD_PROTECTED.saveRMSString("agent", text15);
				}
				else
				{
					Canvas_HAIRMOD_PROTECTED.gameScr.onLoginFail(text13);
				}
				break;
			}
			case 1:
			{
				Char_HAIRMOD_PROTECTED.char_speed = msg.reader().readByte();
				if (Char_HAIRMOD_PROTECTED.char_speed < 3)
				{
					Char_HAIRMOD_PROTECTED.char_speed = 3;
				}
				GameScr_HAIRMOD_PROTECTED.pTicket = msg.reader().readShort();
				Char_HAIRMOD_PROTECTED.CHAR_POTION = msg.reader().readUnsignedByte();
				MainChar_HAIRMOD_PROTECTED.listPotion = new Potion_HAIRMOD_PROTECTED[Char_HAIRMOD_PROTECTED.CHAR_POTION];
				Canvas_HAIRMOD_PROTECTED.gameScr.mainChar.potionLastTimeUse = new long[Char_HAIRMOD_PROTECTED.CHAR_POTION];
				for (int num175 = 0; num175 < Char_HAIRMOD_PROTECTED.CHAR_POTION; num175++)
				{
					MainChar_HAIRMOD_PROTECTED.listPotion[num175] = new Potion_HAIRMOD_PROTECTED();
					MainChar_HAIRMOD_PROTECTED.listPotion[num175].index = msg.reader().readUnsignedByte();
					MainChar_HAIRMOD_PROTECTED.listPotion[num175].name = msg.reader().readUTF();
					MainChar_HAIRMOD_PROTECTED.listPotion[num175].name2 = msg.reader().readUTF();
					MainChar_HAIRMOD_PROTECTED.listPotion[num175].delay = msg.reader().readShort();
					MainChar_HAIRMOD_PROTECTED.listPotion[num175].id = (short)num175;
					MainChar_HAIRMOD_PROTECTED.listPotion[num175].isTrade = msg.reader().readBoolean();
				}
				for (int num176 = 0; num176 < Char_HAIRMOD_PROTECTED.nSkill.Length; num176++)
				{
					Char_HAIRMOD_PROTECTED.nSkill[num176] = msg.reader().readByte();
				}
				GameScr_HAIRMOD_PROTECTED.infoNewSkill.removeAllElements();
				for (int num177 = 0; num177 < 5; num177++)
				{
					int num178 = msg.reader().readByte();
					mVector_HAIRMOD_PROTECTED mVector_HAIRMOD_PROTECTED12 = new mVector_HAIRMOD_PROTECTED();
					for (int num179 = 0; num179 < num178; num179++)
					{
						InfoSkillLearn_HAIRMOD_PROTECTED infoSkillLearn_HAIRMOD_PROTECTED = new InfoSkillLearn_HAIRMOD_PROTECTED();
						infoSkillLearn_HAIRMOD_PROTECTED.idSkill = msg.reader().readByte();
						infoSkillLearn_HAIRMOD_PROTECTED.name = msg.reader().readUTF();
						infoSkillLearn_HAIRMOD_PROTECTED.decript = msg.reader().readUTF();
						infoSkillLearn_HAIRMOD_PROTECTED.price = msg.reader().readInt();
						infoSkillLearn_HAIRMOD_PROTECTED.charClass = (sbyte)num177;
						mVector_HAIRMOD_PROTECTED12.addElement(infoSkillLearn_HAIRMOD_PROTECTED);
					}
					GameScr_HAIRMOD_PROTECTED.infoNewSkill.addElement(mVector_HAIRMOD_PROTECTED12);
				}
				if (msg.reader().available() > 0)
				{
					GameScr_HAIRMOD_PROTECTED.idMapPK = msg.reader().readShort();
					int num180 = msg.reader().readUnsignedByte();
					for (int num181 = 0; num181 < num180; num181++)
					{
						string o6 = msg.reader().readUnsignedByte() + string.Empty;
						WindowInfoScr_HAIRMOD_PROTECTED.gI().potionShop.addElement(o6);
					}
					for (int num182 = 0; num182 < 5; num182++)
					{
						for (int num183 = 0; num183 < 5; num183++)
						{
							Char_HAIRMOD_PROTECTED.mainDef[num182][num183] = msg.reader().readByte();
							Char_HAIRMOD_PROTECTED.mainAt[num182][num183] = msg.reader().readByte();
							Char_HAIRMOD_PROTECTED.pcAttack[num182][num183] = msg.reader().readByte();
							Char_HAIRMOD_PROTECTED.pcDef[num182][num183] = msg.reader().readByte();
						}
					}
				}
				GameScr_HAIRMOD_PROTECTED.xArena = msg.reader().readByte();
				GameScr_HAIRMOD_PROTECTED.yArena = msg.reader().readByte();
				GameScr_HAIRMOD_PROTECTED.wArena = msg.reader().readByte();
				GameScr_HAIRMOD_PROTECTED.hArena = msg.reader().readByte();
				sbyte b59 = msg.reader().readByte();
				Res_HAIRMOD_PROTECTED.khamProperty = new string[b59];
				Res_HAIRMOD_PROTECTED.idNgocKham = new sbyte[b59];
				for (int num184 = 0; num184 < b59; num184++)
				{
					Res_HAIRMOD_PROTECTED.khamProperty[num184] = msg.reader().readUTF();
					Res_HAIRMOD_PROTECTED.idNgocKham[num184] = msg.reader().readByte();
				}
				string text23 = msg.reader().readUTF();
				if (!GameMidlet_HAIRMOD_PROTECTED.numberSupport.Equals(text23))
				{
					GameMidlet_HAIRMOD_PROTECTED.numberSupport = text23;
					RMS_HAIRMOD_PROTECTED.saveRMSString("numbersupport", text23);
				}
				sbyte b60 = msg.reader().readByte();
				GameScr_HAIRMOD_PROTECTED.TYPE_MP_HP[0] = new sbyte[b60];
				GameScr_HAIRMOD_PROTECTED.TYPE_MP_HP[1] = new sbyte[b60];
				GameScr_HAIRMOD_PROTECTED.VALUE_MP_HP[1] = new int[b60];
				GameScr_HAIRMOD_PROTECTED.VALUE_MP_HP[0] = new int[b60];
				for (int num185 = 0; num185 < b60; num185++)
				{
					GameScr_HAIRMOD_PROTECTED.TYPE_MP_HP[0][num185] = msg.reader().readByte();
					GameScr_HAIRMOD_PROTECTED.VALUE_MP_HP[0][num185] = msg.reader().readInt();
					GameScr_HAIRMOD_PROTECTED.TYPE_MP_HP[1][num185] = msg.reader().readByte();
					GameScr_HAIRMOD_PROTECTED.VALUE_MP_HP[1][num185] = msg.reader().readInt();
				}
				int num186 = msg.reader().readByte();
				ItemQuest_HAIRMOD_PROTECTED.NAME_ITEM = new string[num186];
				ItemQuest_HAIRMOD_PROTECTED.ICON_IMG = new sbyte[num186];
				for (int num187 = 0; num187 < num186; num187++)
				{
					ItemQuest_HAIRMOD_PROTECTED.ICON_IMG[num187] = msg.reader().readByte();
					ItemQuest_HAIRMOD_PROTECTED.NAME_ITEM[num187] = msg.reader().readUTF();
				}
				GameMidlet_HAIRMOD_PROTECTED.url = msg.reader().readUTF();
				sbyte b61 = msg.reader().readByte();
				for (int num188 = 0; num188 < b61; num188++)
				{
					short num189 = msg.reader().readShort();
					sbyte[] array33 = new sbyte[num189];
					for (int num190 = 0; num190 < num189; num190++)
					{
						array33[num190] = msg.reader().readByte();
					}
					RMS_HAIRMOD_PROTECTED.saveRMS(Canvas_HAIRMOD_PROTECTED.nameTile[num188], array33);
				}
				sbyte b62 = msg.reader().readByte();
				try
				{
					ThanThu_HAIRMOD_PROTECTED.ALL_DATA_THAN_THU.clear();
					sbyte b63 = msg.reader().readByte();
					for (int num191 = 0; num191 < b63; num191++)
					{
						sbyte[] array34 = new sbyte[msg.reader().readShort()];
						for (int num192 = 0; num192 < array34.Length; num192++)
						{
							array34[num192] = msg.reader().readByte();
						}
						ThanThu_HAIRMOD_PROTECTED.setDataThanThu(num191, array34);
					}
				}
				catch (Exception)
				{
				}
				Canvas_HAIRMOD_PROTECTED.gameScr.onLoginSuccess();
				break;
			}
			case 3:
			{
				MainChar_HAIRMOD_PROTECTED mainChar = Canvas_HAIRMOD_PROTECTED.gameScr.mainChar;
				try
				{
					mainChar.ID = msg.reader().readShort();
					mainChar.name = msg.reader().readUTF();
					mainChar.wName = (short)MFont_HAIRMOD_PROTECTED.smallFont.getWidth(mainChar.name);
					mainChar.hp = (mainChar.realHP = msg.reader().readInt());
					mainChar.maxhp = msg.reader().readInt();
					mainChar.mp = msg.reader().readInt();
					mainChar.maxmp = msg.reader().readInt();
					mainChar.currentHead = msg.reader().readByte();
					mainChar.clazz = msg.reader().readByte();
					mainChar.attack = msg.reader().readInt();
					mainChar.defend = msg.reader().readInt();
					mainChar.defend_magic = msg.reader().readInt();
					mainChar.accurate = msg.reader().readShort();
					mainChar.dodge = msg.reader().readShort();
					mainChar.critical = msg.reader().readShort();
					mainChar.he = msg.reader().readByte();
					mainChar.level = msg.reader().readByte();
					mainChar.lvpercent = msg.reader().readShort();
					mainChar.strength = msg.reader().readShort();
					mainChar.agility = msg.reader().readShort();
					mainChar.spirit = msg.reader().readShort();
					mainChar.health = msg.reader().readShort();
					mainChar.luck = msg.reader().readShort();
					mainChar.basePointLeft = msg.reader().readShort();
					mainChar.skillPointLeft = msg.reader().readShort();
					mainChar.dedicationPoint = msg.reader().readInt();
					mainChar.baokich = msg.reader().readShort();
					Char_HAIRMOD_PROTECTED.skillLevelLearnt = new sbyte[msg.reader().readByte()];
					for (int num162 = 0; num162 < Char_HAIRMOD_PROTECTED.skillLevelLearnt.Length; num162++)
					{
						Char_HAIRMOD_PROTECTED.skillLevelLearnt[num162] = msg.reader().readByte();
					}
					mainChar.killer = msg.reader().readShort();
					if (mainChar.killer > 0)
					{
						mainChar.iskiller = true;
					}
					mainChar.gender = msg.reader().readByte();
					sbyte b52 = msg.reader().readByte();
					if (b52 >= 0)
					{
						mainChar.useHorse = (sbyte)(b52 - 1);
					}
					mainChar.anmFly = msg.reader().readByte();
					mainChar.idImgHorse = msg.reader().readByte();
					mainChar.speed = msg.reader().readByte();
					mainChar.idClan = msg.reader().readShort();
					if (mainChar.idClan != -1)
					{
						Canvas_HAIRMOD_PROTECTED.gameScr.mainChar.isMaster = msg.reader().readByte();
					}
					if (Canvas_HAIRMOD_PROTECTED.gameScr.mainChar.isMaster == 0)
					{
						Char_HAIRMOD_PROTECTED.dissolved = msg.reader().readBoolean();
					}
					mainChar.isNo = msg.reader().readBoolean();
					mainChar.limItemBag = msg.reader().readByte();
					mainChar.nationID = msg.reader().readByte();
					mainChar.inCountry = msg.reader().readByte();
					mainChar.lienTram = msg.reader().readShort();
					mainChar.congTrang = msg.reader().readInt();
					mainChar.hoatdong = msg.reader().readShort();
					mainChar.pArena = msg.reader().readInt();
					mainChar.nameHusband_wife = msg.reader().readUTF();
					mainChar.paintHat = msg.reader().readBoolean();
					short num163 = -1;
					short num164 = -1;
					try
					{
						num163 = msg.reader().readShort();
						num164 = msg.reader().readByte();
					}
					catch (Exception)
					{
						num163 = -1;
						num164 = -1;
					}
					Canvas_HAIRMOD_PROTECTED.gameScr.mainChar.setHorse(num163, num164);
					short num165 = -1;
					short num166 = -1;
					short num167 = -1;
					try
					{
						num165 = msg.reader().readShort();
						num166 = msg.reader().readByte();
						num167 = msg.reader().readShort();
					}
					catch (Exception)
					{
						num165 = -1;
						num166 = -1;
						num167 = -1;
					}
					Canvas_HAIRMOD_PROTECTED.gameScr.mainChar.setWeapon(num165, num166, num167);
					short num168 = -1;
					short num169 = -1;
					short num170 = -1;
					try
					{
						num168 = msg.reader().readShort();
						num169 = msg.reader().readShort();
						num170 = msg.reader().readShort();
					}
					catch (Exception)
					{
						num168 = -1;
						num169 = -1;
						num170 = -1;
					}
					Canvas_HAIRMOD_PROTECTED.gameScr.mainChar.setPartWearing(num168, num169, num170);
					sbyte b53 = -1;
					try
					{
						b53 = msg.reader().readByte();
						short[] array31 = new short[b53];
						sbyte[] array32 = new sbyte[b53];
						for (int num171 = 0; num171 < b53; num171++)
						{
							array31[num171] = msg.reader().readShort();
							array32[num171] = msg.reader().readByte();
						}
						Canvas_HAIRMOD_PROTECTED.gameScr.mainChar.setEff(array31, array32);
						string text22 = msg.reader().readUTF();
						if (!text22.Equals(string.Empty))
						{
							WindowInfoScr_HAIRMOD_PROTECTED.infoMainCharServer = Util_HAIRMOD_PROTECTED.split(text22, "@");
						}
						else
						{
							WindowInfoScr_HAIRMOD_PROTECTED.infoMainCharServer = null;
						}
					}
					catch (Exception)
					{
					}
					short num172 = -1;
					try
					{
						num172 = msg.reader().readShort();
					}
					catch (Exception)
					{
						num172 = -1;
					}
					sbyte b54 = -1;
					short num173 = -1;
					sbyte b55 = 0;
					sbyte b56 = 0;
					try
					{
						b54 = msg.reader().readByte();
						num173 = msg.reader().readShort();
						b55 = msg.reader().readByte();
						b56 = msg.reader().readByte();
					}
					catch (Exception)
					{
						b54 = -1;
						num173 = -1;
						b55 = 0;
						b56 = 0;
					}
					Canvas_HAIRMOD_PROTECTED.gameScr.mainChar.indexPP = b54;
					Canvas_HAIRMOD_PROTECTED.gameScr.mainChar.idImgPP = num173;
					Canvas_HAIRMOD_PROTECTED.gameScr.mainChar.nFrameWP = b55;
					Canvas_HAIRMOD_PROTECTED.gameScr.mainChar.nFramePP = b56;
					sbyte b57 = -1;
					short num174 = -1;
					sbyte b58 = 0;
					try
					{
						b57 = msg.reader().readByte();
						num174 = msg.reader().readShort();
						b58 = msg.reader().readByte();
					}
					catch (Exception)
					{
						b57 = -1;
						num174 = -1;
						b58 = 0;
					}
					Canvas_HAIRMOD_PROTECTED.gameScr.mainChar.indexAVT = b57;
					Canvas_HAIRMOD_PROTECTED.gameScr.mainChar.idImgAVT = num174;
					Canvas_HAIRMOD_PROTECTED.gameScr.mainChar.nFrameAVT = b58;
					Canvas_HAIRMOD_PROTECTED.gameScr.mainChar.idPhiPhong = num172;
				}
				catch (Exception)
				{
				}
				Canvas_HAIRMOD_PROTECTED.gameScr.onMainCharInfo();
				break;
			}
			case 5:
			{
				if (!Canvas_HAIRMOD_PROTECTED.gameScr.readyGetGameLogic)
				{
					break;
				}
				short num51 = msg.reader().readShort();
				Char_HAIRMOD_PROTECTED char_HAIRMOD_PROTECTED3 = (Char_HAIRMOD_PROTECTED)Canvas_HAIRMOD_PROTECTED.gameScr.findCharByID(num51);
				if (char_HAIRMOD_PROTECTED3 == null)
				{
					break;
				}
				char_HAIRMOD_PROTECTED3.ID = num51;
				if (char_HAIRMOD_PROTECTED3.idBoss != -1)
				{
					char_HAIRMOD_PROTECTED3.dir = 0;
				}
				char_HAIRMOD_PROTECTED3.name = msg.reader().readUTF();
				char_HAIRMOD_PROTECTED3.x = msg.reader().readShort();
				char_HAIRMOD_PROTECTED3.y = msg.reader().readShort();
				char_HAIRMOD_PROTECTED3.hp = msg.reader().readInt();
				char_HAIRMOD_PROTECTED3.maxhp = msg.reader().readInt();
				char_HAIRMOD_PROTECTED3.mp = msg.reader().readInt();
				char_HAIRMOD_PROTECTED3.maxmp = msg.reader().readInt();
				char_HAIRMOD_PROTECTED3.currentHead = msg.reader().readByte();
				char_HAIRMOD_PROTECTED3.clazz = msg.reader().readByte();
				for (int num52 = 0; num52 < char_HAIRMOD_PROTECTED3.buffType.Length; num52++)
				{
					char_HAIRMOD_PROTECTED3.buffType[num52] = msg.reader().readByte();
				}
				for (int num53 = 0; num53 < char_HAIRMOD_PROTECTED3.buffType.Length; num53++)
				{
					char_HAIRMOD_PROTECTED3.countDown[num53] = msg.reader().readShort();
				}
				char_HAIRMOD_PROTECTED3.killer = msg.reader().readShort();
				char_HAIRMOD_PROTECTED3.pk = msg.reader().readByte();
				char_HAIRMOD_PROTECTED3.defend = msg.reader().readShort();
				char_HAIRMOD_PROTECTED3.defend_magic = msg.reader().readShort();
				char_HAIRMOD_PROTECTED3.level = msg.reader().readByte();
				char_HAIRMOD_PROTECTED3.he = msg.reader().readByte();
				sbyte b24 = msg.reader().readByte();
				if (b24 >= 0)
				{
					char_HAIRMOD_PROTECTED3.useHorse = (sbyte)(b24 - 1);
				}
				char_HAIRMOD_PROTECTED3.anmFly = msg.reader().readByte();
				char_HAIRMOD_PROTECTED3.idImgHorse = msg.reader().readByte();
				char_HAIRMOD_PROTECTED3.speed = msg.reader().readByte();
				char_HAIRMOD_PROTECTED3.idClan = msg.reader().readShort();
				char_HAIRMOD_PROTECTED3.idBoss = msg.reader().readByte();
				if (char_HAIRMOD_PROTECTED3.idClan != -1)
				{
					char_HAIRMOD_PROTECTED3.isMaster = msg.reader().readByte();
				}
				char_HAIRMOD_PROTECTED3.idFashion = msg.reader().readByte();
				for (int num54 = 0; num54 < char_HAIRMOD_PROTECTED3.idModel.Length; num54++)
				{
					char_HAIRMOD_PROTECTED3.idModel[num54] = msg.reader().readShort();
				}
				char_HAIRMOD_PROTECTED3.isNo = msg.reader().readBoolean();
				char_HAIRMOD_PROTECTED3.setCharInfo();
				Canvas_HAIRMOD_PROTECTED.gameScr.onCharInfo(char_HAIRMOD_PROTECTED3);
				if (msg.reader().readBoolean())
				{
					Canvas_HAIRMOD_PROTECTED.gameScr.gameService.requestCharMonter(char_HAIRMOD_PROTECTED3.ID);
				}
				char_HAIRMOD_PROTECTED3.nationID = msg.reader().readByte();
				char_HAIRMOD_PROTECTED3.inCountry = msg.reader().readByte();
				char_HAIRMOD_PROTECTED3.paintHat = msg.reader().readBoolean();
				short num55 = -1;
				short num56 = -1;
				try
				{
					num55 = msg.reader().readShort();
					num56 = msg.reader().readByte();
				}
				catch (Exception)
				{
					num55 = -1;
					num56 = -1;
				}
				char_HAIRMOD_PROTECTED3.setHorse(num55, num56);
				short num57 = -1;
				short num58 = -1;
				short num59 = -1;
				try
				{
					num57 = msg.reader().readShort();
					num58 = msg.reader().readByte();
					num59 = msg.reader().readShort();
				}
				catch (Exception)
				{
					num57 = -1;
					num58 = -1;
					num59 = -1;
				}
				char_HAIRMOD_PROTECTED3.setWeapon(num57, num58, num59);
				short num60 = -1;
				short num61 = -1;
				short num62 = -1;
				try
				{
					num60 = msg.reader().readShort();
					num61 = msg.reader().readShort();
					num62 = msg.reader().readShort();
				}
				catch (Exception)
				{
					num60 = -1;
					num61 = -1;
					num62 = -1;
				}
				char_HAIRMOD_PROTECTED3.setPartWearing(num60, num61, num62);
				sbyte b25 = -1;
				try
				{
					b25 = msg.reader().readByte();
					short[] array14 = new short[b25];
					sbyte[] array15 = new sbyte[b25];
					for (int num63 = 0; num63 < b25; num63++)
					{
						array14[num63] = msg.reader().readShort();
						array15[num63] = msg.reader().readByte();
					}
					char_HAIRMOD_PROTECTED3.setEff(array14, array15);
				}
				catch (Exception)
				{
				}
				short num64 = -1;
				try
				{
					num64 = msg.reader().readShort();
				}
				catch (Exception)
				{
					num64 = -1;
				}
				char_HAIRMOD_PROTECTED3.idPhiPhong = num64;
				sbyte b26 = -1;
				short num65 = -1;
				sbyte b27 = 0;
				sbyte b28 = 0;
				try
				{
					b26 = msg.reader().readByte();
					num65 = msg.reader().readShort();
					b27 = msg.reader().readByte();
					b28 = msg.reader().readByte();
				}
				catch (Exception)
				{
					b26 = -1;
					num65 = -1;
					b27 = 0;
					b28 = 0;
				}
				char_HAIRMOD_PROTECTED3.indexPP = b26;
				char_HAIRMOD_PROTECTED3.idImgPP = num65;
				char_HAIRMOD_PROTECTED3.nFrameWP = b27;
				char_HAIRMOD_PROTECTED3.nFramePP = b28;
				sbyte b29 = -1;
				short num66 = -1;
				sbyte b30 = 0;
				try
				{
					b29 = msg.reader().readByte();
					num66 = msg.reader().readShort();
					b30 = msg.reader().readByte();
				}
				catch (Exception)
				{
					b29 = -1;
					num66 = -1;
					b30 = 0;
				}
				char_HAIRMOD_PROTECTED3.indexAVT = b29;
				char_HAIRMOD_PROTECTED3.idImgAVT = num66;
				char_HAIRMOD_PROTECTED3.nFrameAVT = b30;
				break;
			}
			case 12:
			{
				short map = msg.reader().readShort();
				short toX = msg.reader().readShort();
				short toY = msg.reader().readShort();
				sbyte idXaphu = -1;
				try
				{
					idXaphu = msg.reader().readByte();
				}
				catch (Exception ex11)
				{
					Out_HAIRMOD_PROTECTED.LogError(ex11.StackTrace + " loi change map");
				}
				short mapLoad = msg.reader().readShort();
				string mapName = msg.reader().readUTF();
				sbyte[] array8 = null;
				try
				{
					if (msg.reader().readBoolean())
					{
						int num38 = 0;
						int num39 = 0;
						array8 = new sbyte[msg.reader().available()];
						for (int num40 = 0; num40 < array8.Length - num39; num40++)
						{
							array8[num40] = msg.reader().readByte();
						}
						num38 = array8.Length;
						num39 += num38;
					}
				}
				catch (Exception)
				{
				}
				Canvas_HAIRMOD_PROTECTED.gameScr.onMap(map, toX, toY, mapLoad, mapName, array8);
				Tilemap_HAIRMOD_PROTECTED.idXaphu = idXaphu;
				break;
			}
			case 4:
				while (msg.reader().available() > 0)
				{
					sbyte b18 = msg.reader().readByte();
					short type = msg.reader().readUnsignedByte();
					short actorID = msg.reader().readShort();
					short x3 = msg.reader().readShort();
					short y = msg.reader().readShort();
					sbyte pk = msg.reader().readByte();
					int charid = -1;
					sbyte isnewmonster = 0;
					try
					{
						charid = msg.reader().readInt();
					}
					catch (Exception)
					{
					}
					try
					{
						if (b18 == Actor_HAIRMOD_PROTECTED.CAT_MONSTER)
						{
							isnewmonster = msg.reader().readByte();
						}
					}
					catch (Exception)
					{
					}
					sbyte isThanthu = -1;
					try
					{
						isThanthu = msg.reader().readByte();
					}
					catch (Exception)
					{
					}
					bool canfocus = true;
					try
					{
						canfocus = msg.reader().readBoolean();
					}
					catch (Exception)
					{
					}
					Canvas_HAIRMOD_PROTECTED.gameScr.onActorMove(b18, type, actorID, x3, y, pk, charid, isnewmonster, isThanthu, canfocus);
				}
				break;
			case 8:
				while (msg.reader().available() > 0)
				{
					short charID = msg.reader().readShort();
					Canvas_HAIRMOD_PROTECTED.gameScr.charOutGame(charID);
				}
				break;
			case 7:
			{
				MonsterInfo_HAIRMOD_PROTECTED monsterInfo_HAIRMOD_PROTECTED = new MonsterInfo_HAIRMOD_PROTECTED();
				monsterInfo_HAIRMOD_PROTECTED.id = msg.reader().readShort();
				monsterInfo_HAIRMOD_PROTECTED.monster_type = msg.reader().readUnsignedByte();
				monsterInfo_HAIRMOD_PROTECTED.x = msg.reader().readShort();
				monsterInfo_HAIRMOD_PROTECTED.y = msg.reader().readShort();
				monsterInfo_HAIRMOD_PROTECTED.hp = msg.reader().readInt();
				monsterInfo_HAIRMOD_PROTECTED.lv = msg.reader().readByte();
				monsterInfo_HAIRMOD_PROTECTED.he = msg.reader().readByte();
				monsterInfo_HAIRMOD_PROTECTED.maxhp = msg.reader().readInt();
				monsterInfo_HAIRMOD_PROTECTED.timeLive = msg.reader().readInt();
				Canvas_HAIRMOD_PROTECTED.gameScr.onMonsterInfo(monsterInfo_HAIRMOD_PROTECTED);
				break;
			}
			case 6:
			{
				short attacker2 = msg.reader().readShort();
				short attacked2 = msg.reader().readShort();
				sbyte skillID = msg.reader().readByte();
				int hpLost2 = msg.reader().readInt();
				int hpLeft2 = msg.reader().readInt();
				sbyte effect = msg.reader().readByte();
				sbyte x = msg.reader().readByte();
				sbyte buffAttack = msg.reader().readByte();
				sbyte level = msg.reader().readByte();
				Canvas_HAIRMOD_PROTECTED.gameScr.onPlayerAttackPlayer(attacker2, attacked2, skillID, hpLost2, hpLeft2, effect, x, buffAttack, level);
				break;
			}
			case 9:
			{
				short attacker3 = msg.reader().readShort();
				short attacked3 = msg.reader().readShort();
				sbyte skillID2 = msg.reader().readByte();
				int hpLost3 = msg.reader().readInt();
				int hpLeft3 = msg.reader().readInt();
				sbyte effect2 = msg.reader().readByte();
				sbyte x2 = msg.reader().readByte();
				sbyte buffAttack2 = msg.reader().readByte();
				sbyte level2 = msg.reader().readByte();
				Canvas_HAIRMOD_PROTECTED.gameScr.onPlayerAttackMonster(attacker3, attacked3, skillID2, hpLost3, hpLeft3, effect2, x2, buffAttack2, level2);
				break;
			}
			case 106:
				Canvas_HAIRMOD_PROTECTED.gameScr.onAttackMultiTarget(msg);
				break;
			case 10:
			{
				short attacker = msg.reader().readShort();
				short attacked = msg.reader().readShort();
				int hpLost = msg.reader().readInt();
				int hpLeft = msg.reader().readInt();
				Canvas_HAIRMOD_PROTECTED.gameScr.onMonsterAttackPlayer(attacker, attacked, hpLost, hpLeft);
				break;
			}
			case 83:
				Canvas_HAIRMOD_PROTECTED.gameScr.onBossAttackPlayer(msg);
				break;
			case 17:
				try
				{
					MonsterDieInfo_HAIRMOD_PROTECTED monsterDieInfo_HAIRMOD_PROTECTED = new MonsterDieInfo_HAIRMOD_PROTECTED();
					monsterDieInfo_HAIRMOD_PROTECTED.attacker = msg.reader().readShort();
					monsterDieInfo_HAIRMOD_PROTECTED.attacked = msg.reader().readShort();
					monsterDieInfo_HAIRMOD_PROTECTED.skillID = msg.reader().readByte();
					monsterDieInfo_HAIRMOD_PROTECTED.hpLost = msg.reader().readInt();
					monsterDieInfo_HAIRMOD_PROTECTED.effect = msg.reader().readByte();
					monsterDieInfo_HAIRMOD_PROTECTED.itemDrop = new ItemDropInfo_HAIRMOD_PROTECTED[msg.reader().readByte()];
					Out_HAIRMOD_PROTECTED.println("SO LUONG ITEMDROP " + monsterDieInfo_HAIRMOD_PROTECTED.itemDrop.Length);
					for (int num26 = 0; num26 < monsterDieInfo_HAIRMOD_PROTECTED.itemDrop.Length; num26++)
					{
						monsterDieInfo_HAIRMOD_PROTECTED.itemDrop[num26] = new ItemDropInfo_HAIRMOD_PROTECTED();
						monsterDieInfo_HAIRMOD_PROTECTED.itemDrop[num26].itemCatagory = msg.reader().readByte();
						string text7 = monsterDieInfo_HAIRMOD_PROTECTED.itemDrop[num26].itemCatagory + "_";
						monsterDieInfo_HAIRMOD_PROTECTED.itemDrop[num26].itemTemplateID = msg.reader().readShort();
						text7 = text7 + monsterDieInfo_HAIRMOD_PROTECTED.itemDrop[num26].itemTemplateID + "_";
						monsterDieInfo_HAIRMOD_PROTECTED.itemDrop[num26].itemID = msg.reader().readShort();
						text7 = text7 + monsterDieInfo_HAIRMOD_PROTECTED.itemDrop[num26].itemID + "_";
						monsterDieInfo_HAIRMOD_PROTECTED.itemDrop[num26].x = msg.reader().readShort();
						text7 = text7 + monsterDieInfo_HAIRMOD_PROTECTED.itemDrop[num26].x + "_";
						monsterDieInfo_HAIRMOD_PROTECTED.itemDrop[num26].y = msg.reader().readShort();
						text7 = text7 + monsterDieInfo_HAIRMOD_PROTECTED.itemDrop[num26].y + "_";
						Out_HAIRMOD_PROTECTED.println(text7);
					}
					Out_HAIRMOD_PROTECTED.println("DOC XONG DATA ITEMDROP");
					if (msg.reader().available() > 0)
					{
						monsterDieInfo_HAIRMOD_PROTECTED.x2 = msg.reader().readByte();
					}
					if (msg.reader().available() > 0)
					{
						monsterDieInfo_HAIRMOD_PROTECTED.buffAttack = msg.reader().readByte();
					}
					if (msg.reader().available() > 0)
					{
						monsterDieInfo_HAIRMOD_PROTECTED.level = msg.reader().readByte();
					}
					Canvas_HAIRMOD_PROTECTED.gameScr.onMonsterDie(monsterDieInfo_HAIRMOD_PROTECTED);
					break;
				}
				catch (Exception)
				{
					Out_HAIRMOD_PROTECTED.println("LOI TRONG HAM NHAN DATA ");
					break;
				}
			case 64:
			{
				array = new ItemDropInfo_HAIRMOD_PROTECTED[msg.reader().readByte()];
				for (int num23 = 0; num23 < array.Length; num23++)
				{
					array[num23] = new ItemDropInfo_HAIRMOD_PROTECTED();
					array[num23].itemCatagory = msg.reader().readByte();
					array[num23].itemTemplateID = msg.reader().readByte();
					array[num23].itemID = msg.reader().readShort();
					array[num23].x = msg.reader().readShort();
					array[num23].y = msg.reader().readShort();
				}
				break;
			}
			case 11:
				Canvas_HAIRMOD_PROTECTED.gameScr.onPingBack();
				break;
			case 13:
			{
				sbyte b69 = msg.reader().readByte();
				MainChar_HAIRMOD_PROTECTED[] array38 = null;
				if (b69 == -1)
				{
					Canvas_HAIRMOD_PROTECTED.startOKDlg(msg.reader().readUTF());
					break;
				}
				array38 = new MainChar_HAIRMOD_PROTECTED[b69];
				for (int num213 = 0; num213 < b69; num213++)
				{
					array38[num213] = new MainChar_HAIRMOD_PROTECTED();
					array38[num213].IDDB = msg.reader().readInt();
					array38[num213].name = msg.reader().readUTF();
					array38[num213].currentHead = msg.reader().readByte();
					sbyte b70 = msg.reader().readByte();
					for (int num214 = 0; num214 < b70; num214++)
					{
						sbyte b71 = msg.reader().readByte();
						sbyte b72 = msg.reader().readByte();
						switch (b71)
						{
						case 0:
							array38[num213].bodyStyle = b72;
							break;
						case 1:
							array38[num213].legStyle = b72;
							break;
						case 2:
							array38[num213].hatStyle = b72;
							break;
						default:
							if (b71 != 7)
							{
								if (b71 == 19)
								{
									array38[num213].coatStyle = b72;
								}
								break;
							}
							goto case 3;
						case 3:
						case 4:
						case 5:
						case 6:
							array38[num213].weaponType = b71;
							array38[num213].weaponStyle = b72;
							break;
						}
					}
					array38[num213].lastLv = msg.reader().readShort();
					array38[num213].isusing = msg.reader().readByte();
					array38[num213].nationID = msg.reader().readByte();
					array38[num213].day = msg.reader().readShort();
					sbyte b73 = msg.reader().readByte();
					if (b73 != -1)
					{
						int num215 = msg.reader().readShort();
						sbyte[] array39 = new sbyte[num215];
						for (int num216 = 0; num216 < num215; num216++)
						{
							array39[num216] = msg.reader().readByte();
						}
						int num217 = msg.reader().readShort();
						sbyte[] array40 = new sbyte[num217];
						for (int num218 = 0; num218 < num217; num218++)
						{
							array40[num218] = msg.reader().readByte();
						}
						array38[num213].imgWpa = Res_HAIRMOD_PROTECTED.createImgByHeader(array39, array40);
						array38[num213].dxWear = msg.reader().readByte();
						array38[num213].dyWear = msg.reader().readByte();
					}
				}
				if (array38 != null)
				{
					for (int num219 = 0; num219 < b69; num219++)
					{
						int num220 = -1;
						try
						{
							num220 = msg.reader().readInt();
						}
						catch (Exception)
						{
							num220 = -1;
						}
						if (num220 != -1 && array38[num219].IDDB == num220)
						{
							short num221 = -1;
							sbyte b74 = -1;
							sbyte b75 = -1;
							short num222 = -1;
							sbyte b76 = -1;
							sbyte b77 = -1;
							short num223 = -1;
							sbyte b78 = -1;
							sbyte b79 = -1;
							try
							{
								num221 = msg.reader().readShort();
								b74 = msg.reader().readByte();
								b75 = msg.reader().readByte();
								num222 = msg.reader().readShort();
								b76 = msg.reader().readByte();
								b77 = msg.reader().readByte();
								num223 = msg.reader().readShort();
								b78 = msg.reader().readByte();
								b79 = msg.reader().readByte();
							}
							catch (Exception)
							{
								num221 = -1;
								b74 = -1;
								b75 = -1;
								num222 = -1;
								b76 = -1;
								b77 = -1;
								num223 = -1;
								b78 = -1;
								b79 = -1;
							}
							array38[num219].idImgAVT = num221;
							array38[num219].indexAVT = b74;
							array38[num219].nFrameAVT = b75;
							array38[num219].idImgPP = num222;
							array38[num219].indexPP = b76;
							array38[num219].nFramePP = b77;
							array38[num219].idImageWeapon = num223;
							array38[num219].typeWeapon = b78;
							array38[num219].nFrameWP = b79;
						}
					}
				}
				CharSelectScr_HAIRMOD_PROTECTED.gI().setCharList(array38);
				CharSelectScr_HAIRMOD_PROTECTED.gI().switchToMe();
				break;
			}
			case 15:
			{
				if (msg.reader().readByte() == 0)
				{
					mVector_HAIRMOD_PROTECTED mVector_HAIRMOD_PROTECTED8 = new mVector_HAIRMOD_PROTECTED();
					short charID3 = msg.reader().readShort();
					int num129 = msg.reader().readByte();
					for (int num130 = 0; num130 < num129; num130++)
					{
						ItemInInventory_HAIRMOD_PROTECTED itemInInventory_HAIRMOD_PROTECTED6 = new ItemInInventory_HAIRMOD_PROTECTED();
						itemInInventory_HAIRMOD_PROTECTED6.charClazz = (itemInInventory_HAIRMOD_PROTECTED6.clazz = msg.reader().readByte());
						itemInInventory_HAIRMOD_PROTECTED6.itemID = msg.reader().readShort();
						itemInInventory_HAIRMOD_PROTECTED6.idTemplate = msg.reader().readShort();
						itemInInventory_HAIRMOD_PROTECTED6.plusTemplate = msg.reader().readByte();
						itemInInventory_HAIRMOD_PROTECTED6.level = msg.reader().readByte();
						itemInInventory_HAIRMOD_PROTECTED6.mDurable = msg.reader().readShort();
						itemInInventory_HAIRMOD_PROTECTED6.durable = msg.reader().readShort();
						itemInInventory_HAIRMOD_PROTECTED6.colorName = msg.reader().readByte();
						itemInInventory_HAIRMOD_PROTECTED6.viTriVe = msg.reader().readByte();
						itemInInventory_HAIRMOD_PROTECTED6.heItem = msg.reader().readByte();
						itemInInventory_HAIRMOD_PROTECTED6.beKicked = msg.reader().readByte();
						itemInInventory_HAIRMOD_PROTECTED6.hangItem = msg.reader().readByte();
						itemInInventory_HAIRMOD_PROTECTED6.magic_physic = msg.reader().readByte();
						itemInInventory_HAIRMOD_PROTECTED6.islock = msg.reader().readByte();
						itemInInventory_HAIRMOD_PROTECTED6.nameCharSeal = msg.reader().readUTF();
						itemInInventory_HAIRMOD_PROTECTED6.allAttribute.removeAllElements();
						itemInInventory_HAIRMOD_PROTECTED6.lastTime = mSystem_HAIRMOD_PROTECTED.getCurrentTimeMillis();
						itemInInventory_HAIRMOD_PROTECTED6.dayUse = msg.reader().readUnsignedShort();
						sbyte b42 = msg.reader().readByte();
						for (sbyte b43 = 0; b43 < b42; b43++)
						{
							InfoAttributeItem_HAIRMOD_PROTECTED o4 = new InfoAttributeItem_HAIRMOD_PROTECTED(msg.reader().readUnsignedByte(), msg.reader().readShort());
							itemInInventory_HAIRMOD_PROTECTED6.allAttribute.addElement(o4);
						}
						itemInInventory_HAIRMOD_PROTECTED6.isEnoughData = true;
						mVector_HAIRMOD_PROTECTED8.addElement(itemInInventory_HAIRMOD_PROTECTED6);
					}
					sbyte idFashion = msg.reader().readByte();
					short[] array28 = new short[5] { -1, -1, -1, -1, -1 };
					for (int num131 = 0; num131 < array28.Length; num131++)
					{
						array28[num131] = msg.reader().readShort();
					}
					sbyte b44 = msg.reader().readByte();
					sbyte idIconAnimal = 0;
					string petInfo = string.Empty;
					if (b44 != -1)
					{
						petInfo = msg.reader().readUTF();
						idIconAnimal = msg.reader().readByte();
					}
					sbyte b45 = msg.reader().readByte();
					AnimalMove_HAIRMOD_PROTECTED animalMove_HAIRMOD_PROTECTED = null;
					if (b45 != -1)
					{
						animalMove_HAIRMOD_PROTECTED = new AnimalMove_HAIRMOD_PROTECTED();
						animalMove_HAIRMOD_PROTECTED.type = msg.reader().readByte();
						animalMove_HAIRMOD_PROTECTED.imgID = msg.reader().readByte();
						animalMove_HAIRMOD_PROTECTED.sumFrame = msg.reader().readByte();
						animalMove_HAIRMOD_PROTECTED.infoPet = msg.reader().readUTF();
						animalMove_HAIRMOD_PROTECTED.totalTime = msg.reader().readInt();
						animalMove_HAIRMOD_PROTECTED.timeStart = mSystem_HAIRMOD_PROTECTED.getCurrentTimeMillis();
						animalMove_HAIRMOD_PROTECTED.count = (animalMove_HAIRMOD_PROTECTED.idFrame = 0);
						animalMove_HAIRMOD_PROTECTED.catagory = Actor_HAIRMOD_PROTECTED.CAT_MY_PET;
					}
					Canvas_HAIRMOD_PROTECTED.gameScr.onCharWearingInfo(charID3, mVector_HAIRMOD_PROTECTED8, idFashion, b44, petInfo, animalMove_HAIRMOD_PROTECTED, array28, idIconAnimal);
					break;
				}
				mVector_HAIRMOD_PROTECTED mVector_HAIRMOD_PROTECTED9 = new mVector_HAIRMOD_PROTECTED();
				short charID4 = msg.reader().readShort();
				int num132 = msg.reader().readByte();
				if (num132 <= -1)
				{
					break;
				}
				for (int num133 = 0; num133 < num132; num133++)
				{
					ItemInInventory_HAIRMOD_PROTECTED itemInInventory_HAIRMOD_PROTECTED7 = new ItemInInventory_HAIRMOD_PROTECTED();
					itemInInventory_HAIRMOD_PROTECTED7.charClazz = (itemInInventory_HAIRMOD_PROTECTED7.clazz = msg.reader().readByte());
					itemInInventory_HAIRMOD_PROTECTED7.itemID = msg.reader().readShort();
					itemInInventory_HAIRMOD_PROTECTED7.idTemplate = msg.reader().readShort();
					itemInInventory_HAIRMOD_PROTECTED7.plusTemplate = msg.reader().readByte();
					itemInInventory_HAIRMOD_PROTECTED7.level = msg.reader().readByte();
					itemInInventory_HAIRMOD_PROTECTED7.mDurable = msg.reader().readShort();
					itemInInventory_HAIRMOD_PROTECTED7.durable = msg.reader().readShort();
					itemInInventory_HAIRMOD_PROTECTED7.colorName = msg.reader().readByte();
					itemInInventory_HAIRMOD_PROTECTED7.viTriVe = msg.reader().readByte();
					itemInInventory_HAIRMOD_PROTECTED7.heItem = msg.reader().readByte();
					itemInInventory_HAIRMOD_PROTECTED7.beKicked = msg.reader().readByte();
					itemInInventory_HAIRMOD_PROTECTED7.hangItem = msg.reader().readByte();
					itemInInventory_HAIRMOD_PROTECTED7.magic_physic = msg.reader().readByte();
					itemInInventory_HAIRMOD_PROTECTED7.islock = msg.reader().readByte();
					itemInInventory_HAIRMOD_PROTECTED7.nameCharSeal = msg.reader().readUTF();
					itemInInventory_HAIRMOD_PROTECTED7.allAttribute.removeAllElements();
					sbyte b46 = msg.reader().readByte();
					for (byte b47 = 0; b47 < b46; b47++)
					{
						InfoAttributeItem_HAIRMOD_PROTECTED o5 = new InfoAttributeItem_HAIRMOD_PROTECTED(msg.reader().readUnsignedByte(), msg.reader().readShort());
						itemInInventory_HAIRMOD_PROTECTED7.allAttribute.addElement(o5);
					}
					itemInInventory_HAIRMOD_PROTECTED7.isEnoughData = true;
					mVector_HAIRMOD_PROTECTED9.addElement(itemInInventory_HAIRMOD_PROTECTED7);
				}
				sbyte b48 = msg.reader().readByte();
				Image_HAIRMOD_PROTECTED image_HAIRMOD_PROTECTED2 = null;
				int wimg = 0;
				int himg = 0;
				sbyte timeChangeFrame = 1;
				sbyte[] array29 = new sbyte[msg.reader().available()];
				while (msg.reader().available() > 0)
				{
					for (int num134 = 0; num134 < array29.Length; num134++)
					{
						array29[num134] = msg.reader().readByte();
					}
				}
				if (array29.Length != 0)
				{
					image_HAIRMOD_PROTECTED2 = Res_HAIRMOD_PROTECTED.createImgByByteArray(array29);
					timeChangeFrame = (sbyte)((b48 != 3) ? 6 : 3);
					if (image_HAIRMOD_PROTECTED2 != null)
					{
						wimg = image_HAIRMOD_PROTECTED2.getWidth();
						himg = image_HAIRMOD_PROTECTED2.getHeight() / b48;
					}
				}
				WindowInfoScr_HAIRMOD_PROTECTED.gI().countFrame = 0;
				Canvas_HAIRMOD_PROTECTED.gameScr.onAnimalWearingInfo(charID4, mVector_HAIRMOD_PROTECTED9, image_HAIRMOD_PROTECTED2, b48, wimg, himg, timeChangeFrame);
				break;
			}
			case 16:
			{
				int num118 = msg.reader().readByte();
				int[] array27 = new int[Char_HAIRMOD_PROTECTED.CHAR_POTION];
				long charXu = 0L;
				try
				{
					if (num118 == 0)
					{
						charXu = msg.reader().readLong();
						array27[0] = 0;
						int num119 = msg.reader().readByte();
						for (int num120 = 0; num120 < num119; num120++)
						{
							int num121 = msg.reader().readUnsignedByte();
							int num122 = msg.reader().readInt();
							MainChar_HAIRMOD_PROTECTED.listPotion[num121].number = num122;
							array27[num121] = num122;
							Out_HAIRMOD_PROTECTED.println(MainChar_HAIRMOD_PROTECTED.listPotion[num121].name);
						}
					}
					mVector_HAIRMOD_PROTECTED mVector_HAIRMOD_PROTECTED6 = new mVector_HAIRMOD_PROTECTED();
					if (num118 == 1)
					{
						int num123 = msg.reader().readShort();
						for (int num124 = 0; num124 < num123; num124++)
						{
							ItemInInventory_HAIRMOD_PROTECTED itemInInventory_HAIRMOD_PROTECTED5 = new ItemInInventory_HAIRMOD_PROTECTED();
							itemInInventory_HAIRMOD_PROTECTED5.charClazz = msg.reader().readByte();
							itemInInventory_HAIRMOD_PROTECTED5.itemID = msg.reader().readShort();
							itemInInventory_HAIRMOD_PROTECTED5.idTemplate = msg.reader().readShort();
							itemInInventory_HAIRMOD_PROTECTED5.plusTemplate = msg.reader().readByte();
							itemInInventory_HAIRMOD_PROTECTED5.level = msg.reader().readByte();
							itemInInventory_HAIRMOD_PROTECTED5.mDurable = msg.reader().readShort();
							itemInInventory_HAIRMOD_PROTECTED5.durable = msg.reader().readShort();
							itemInInventory_HAIRMOD_PROTECTED5.clazz = msg.reader().readByte();
							itemInInventory_HAIRMOD_PROTECTED5.numKham = msg.reader().readByte();
							itemInInventory_HAIRMOD_PROTECTED5.totalKham = msg.reader().readByte();
							itemInInventory_HAIRMOD_PROTECTED5.colorName = msg.reader().readByte();
							itemInInventory_HAIRMOD_PROTECTED5.heItem = msg.reader().readByte();
							itemInInventory_HAIRMOD_PROTECTED5.beKicked = msg.reader().readByte();
							itemInInventory_HAIRMOD_PROTECTED5.hangItem = msg.reader().readByte();
							itemInInventory_HAIRMOD_PROTECTED5.magic_physic = msg.reader().readByte();
							itemInInventory_HAIRMOD_PROTECTED5.islock = msg.reader().readByte();
							itemInInventory_HAIRMOD_PROTECTED5.nameCharSeal = msg.reader().readUTF();
							itemInInventory_HAIRMOD_PROTECTED5.allAttribute.removeAllElements();
							itemInInventory_HAIRMOD_PROTECTED5.lastTime = mSystem_HAIRMOD_PROTECTED.getCurrentTimeMillis();
							itemInInventory_HAIRMOD_PROTECTED5.dayUse = msg.reader().readUnsignedShort();
							sbyte b40 = msg.reader().readByte();
							for (sbyte b41 = 0; b41 < b40; b41++)
							{
								InfoAttributeItem_HAIRMOD_PROTECTED o3 = new InfoAttributeItem_HAIRMOD_PROTECTED(msg.reader().readUnsignedByte(), msg.reader().readShort());
								itemInInventory_HAIRMOD_PROTECTED5.allAttribute.addElement(o3);
							}
							itemInInventory_HAIRMOD_PROTECTED5.isItemDrop = msg.reader().readByte();
							itemInInventory_HAIRMOD_PROTECTED5.isEnoughData = true;
							mVector_HAIRMOD_PROTECTED6.addElement(itemInInventory_HAIRMOD_PROTECTED5);
						}
					}
					Canvas_HAIRMOD_PROTECTED.gameScr.mainChar.luong = msg.reader().readInt();
					mVector_HAIRMOD_PROTECTED mVector_HAIRMOD_PROTECTED7 = new mVector_HAIRMOD_PROTECTED();
					if (num118 == 2)
					{
						int num125 = msg.reader().readByte();
						for (int num126 = 0; num126 < num125; num126++)
						{
							AnimalInfo_HAIRMOD_PROTECTED animalInfo_HAIRMOD_PROTECTED = new AnimalInfo_HAIRMOD_PROTECTED();
							animalInfo_HAIRMOD_PROTECTED.id = msg.reader().readShort();
							animalInfo_HAIRMOD_PROTECTED.idImg = msg.reader().readByte();
							animalInfo_HAIRMOD_PROTECTED.idPaint = msg.reader().readByte();
							animalInfo_HAIRMOD_PROTECTED.level = msg.reader().readByte();
							animalInfo_HAIRMOD_PROTECTED.name = msg.reader().readUTF();
							animalInfo_HAIRMOD_PROTECTED.info = msg.reader().readUTF();
							animalInfo_HAIRMOD_PROTECTED.typeAnimal = msg.reader().readByte();
							mVector_HAIRMOD_PROTECTED7.addElement(animalInfo_HAIRMOD_PROTECTED);
						}
						num125 = msg.reader().readByte();
						for (int num127 = 0; num127 < num125; num127++)
						{
							AnimalInfo_HAIRMOD_PROTECTED animalInfo_HAIRMOD_PROTECTED2 = new AnimalInfo_HAIRMOD_PROTECTED();
							animalInfo_HAIRMOD_PROTECTED2.id = msg.reader().readShort();
							animalInfo_HAIRMOD_PROTECTED2.idImg = msg.reader().readByte();
							animalInfo_HAIRMOD_PROTECTED2.idPaint = msg.reader().readByte();
							animalInfo_HAIRMOD_PROTECTED2.name = msg.reader().readUTF();
							animalInfo_HAIRMOD_PROTECTED2.typePet = msg.reader().readByte();
							animalInfo_HAIRMOD_PROTECTED2.info = msg.reader().readUTF();
							animalInfo_HAIRMOD_PROTECTED2.totalTimeCon = msg.reader().readInt();
							animalInfo_HAIRMOD_PROTECTED2.timeStart = mSystem_HAIRMOD_PROTECTED.getCurrentTimeMillis();
							animalInfo_HAIRMOD_PROTECTED2.typeAnimal = msg.reader().readByte();
							mVector_HAIRMOD_PROTECTED7.addElement(animalInfo_HAIRMOD_PROTECTED2);
						}
					}
					if (num118 == 3)
					{
						MainChar_HAIRMOD_PROTECTED.itemQuest = new short[msg.reader().readByte()];
						for (int num128 = 0; num128 < MainChar_HAIRMOD_PROTECTED.itemQuest.Length; num128++)
						{
							MainChar_HAIRMOD_PROTECTED.itemQuest[num128] = msg.reader().readShort();
						}
					}
					Out_HAIRMOD_PROTECTED.println("DONG XONG DATA ");
					Canvas_HAIRMOD_PROTECTED.gameScr.mainChar.luongKhoa = msg.reader().readInt();
					Canvas_HAIRMOD_PROTECTED.gameScr.onCharInventory(charXu, array27, mVector_HAIRMOD_PROTECTED6, mVector_HAIRMOD_PROTECTED7, num118);
					break;
				}
				catch (Exception)
				{
					Out_HAIRMOD_PROTECTED.println("LOI CHARINVENTORY " + num118);
					break;
				}
			}
			case 18:
			{
				short whoGet3 = msg.reader().readShort();
				ItemInInventory_HAIRMOD_PROTECTED itemInInventory_HAIRMOD_PROTECTED4 = new ItemInInventory_HAIRMOD_PROTECTED();
				itemInInventory_HAIRMOD_PROTECTED4.clazz = (itemInInventory_HAIRMOD_PROTECTED4.charClazz = msg.reader().readByte());
				itemInInventory_HAIRMOD_PROTECTED4.itemOldID = msg.reader().readShort();
				itemInInventory_HAIRMOD_PROTECTED4.itemID = msg.reader().readShort();
				itemInInventory_HAIRMOD_PROTECTED4.idTemplate = msg.reader().readShort();
				itemInInventory_HAIRMOD_PROTECTED4.plusTemplate = msg.reader().readByte();
				itemInInventory_HAIRMOD_PROTECTED4.level = msg.reader().readByte();
				itemInInventory_HAIRMOD_PROTECTED4.durable = msg.reader().readShort();
				itemInInventory_HAIRMOD_PROTECTED4.mDurable = msg.reader().readShort();
				Canvas_HAIRMOD_PROTECTED.gameScr.onGetItemFromGround(whoGet3, itemInInventory_HAIRMOD_PROTECTED4);
				break;
			}
			case 19:
			{
				short whoGet4 = msg.reader().readShort();
				short potionID = msg.reader().readShort();
				short potionType2 = msg.reader().readUnsignedByte();
				short potionQuantity = msg.reader().readShort();
				Canvas_HAIRMOD_PROTECTED.gameScr.onGetPotionFromGround(whoGet4, potionID, potionType2, potionQuantity);
				break;
			}
			case -65:
				try
				{
					short whoGet2 = msg.reader().readShort();
					short gemID2 = msg.reader().readShort();
					sbyte itemQuestType = msg.reader().readByte();
					Canvas_HAIRMOD_PROTECTED.gameScr.onGetItemQuestFromGround(whoGet2, gemID2, itemQuestType);
					break;
				}
				catch (Exception)
				{
					break;
				}
			case -41:
			{
				short whoGet = msg.reader().readShort();
				sbyte catagory = msg.reader().readByte();
				short gemID = msg.reader().readShort();
				Canvas_HAIRMOD_PROTECTED.gameScr.onGetGemFromGround(whoGet, gemID, catagory);
				break;
			}
			case 21:
			{
				ItemInInventory_HAIRMOD_PROTECTED itemInInventory_HAIRMOD_PROTECTED3 = new ItemInInventory_HAIRMOD_PROTECTED();
				itemInInventory_HAIRMOD_PROTECTED3.itemID = msg.reader().readShort();
				itemInInventory_HAIRMOD_PROTECTED3.durable = msg.reader().readShort();
				itemInInventory_HAIRMOD_PROTECTED3.clazz = msg.reader().readByte();
				itemInInventory_HAIRMOD_PROTECTED3.dayUse = msg.reader().readUnsignedShort();
				itemInInventory_HAIRMOD_PROTECTED3.allAttribute.removeAllElements();
				sbyte b35 = msg.reader().readByte();
				for (sbyte b36 = 0; b36 < b35; b36++)
				{
					InfoAttributeItem_HAIRMOD_PROTECTED o = new InfoAttributeItem_HAIRMOD_PROTECTED(msg.reader().readUnsignedByte(), msg.reader().readShort());
					itemInInventory_HAIRMOD_PROTECTED3.allAttribute.addElement(o);
				}
				itemInInventory_HAIRMOD_PROTECTED3.plusTemplate = msg.reader().readByte();
				try
				{
					itemInInventory_HAIRMOD_PROTECTED3.islock = msg.reader().readByte();
				}
				catch (Exception)
				{
				}
				Canvas_HAIRMOD_PROTECTED.gameScr.onItemInfo(itemInInventory_HAIRMOD_PROTECTED3);
				break;
			}
			case 22:
			{
				short userId = msg.reader().readShort();
				sbyte potionType = msg.reader().readByte();
				short valueAdd = msg.reader().readShort();
				int valueNew = msg.reader().readInt();
				int isHP = 0;
				try
				{
					isHP = msg.reader().readByte();
				}
				catch (Exception)
				{
				}
				Canvas_HAIRMOD_PROTECTED.gameScr.onUsePotion(userId, potionType, valueAdd, valueNew, isHP);
				break;
			}
			case 23:
			{
				sbyte b13 = msg.reader().readByte();
				if (b13 == 0)
				{
					sbyte b14 = msg.reader().readByte();
					sbyte[] array5 = new sbyte[b14];
					for (int num28 = 0; num28 < b14; num28++)
					{
						array5[num28] = msg.reader().readByte();
					}
					Canvas_HAIRMOD_PROTECTED.gameScr.onNPCSellPotion(array5);
				}
				if (b13 == 1)
				{
					int num29 = msg.reader().readByte();
					WindowInfoScr_HAIRMOD_PROTECTED.gI().idClass = num29;
					mVector_HAIRMOD_PROTECTED mVector_HAIRMOD_PROTECTED3 = new mVector_HAIRMOD_PROTECTED();
					int num30 = msg.reader().readShort();
					for (int num31 = 0; num31 < num30; num31++)
					{
						ItemInInventory_HAIRMOD_PROTECTED itemInInventory_HAIRMOD_PROTECTED = new ItemInInventory_HAIRMOD_PROTECTED();
						itemInInventory_HAIRMOD_PROTECTED.isEnoughData = true;
						itemInInventory_HAIRMOD_PROTECTED.isShopItem = true;
						int num32 = msg.reader().readShort();
						if (num29 == -1)
						{
							itemInInventory_HAIRMOD_PROTECTED.charClazz = (sbyte)getIndexClassFromItemID(num32);
						}
						else
						{
							itemInInventory_HAIRMOD_PROTECTED.charClazz = (sbyte)num29;
						}
						itemInInventory_HAIRMOD_PROTECTED.idTemplate = (short)num32;
						itemInInventory_HAIRMOD_PROTECTED.clazz = (sbyte)((num29 != -1) ? num29 : getIndexClassFromItemID(num32));
						ItemTemplate_HAIRMOD_PROTECTED item = Res_HAIRMOD_PROTECTED.getItem(itemInInventory_HAIRMOD_PROTECTED.charClazz, num32);
						itemInInventory_HAIRMOD_PROTECTED.level = item.level;
						itemInInventory_HAIRMOD_PROTECTED.durable = item.durable;
						mVector_HAIRMOD_PROTECTED3.addElement(itemInInventory_HAIRMOD_PROTECTED);
					}
					try
					{
						ItemInInventory_HAIRMOD_PROTECTED.typeItemBuy = 2;
						ItemInInventory_HAIRMOD_PROTECTED.typeItemBuy = msg.reader().readByte();
					}
					catch (Exception)
					{
					}
					Canvas_HAIRMOD_PROTECTED.gameScr.onNPCSellItem(mVector_HAIRMOD_PROTECTED3);
				}
				try
				{
					if (b13 == 2)
					{
						mVector_HAIRMOD_PROTECTED mVector_HAIRMOD_PROTECTED4 = new mVector_HAIRMOD_PROTECTED();
						sbyte b15 = msg.reader().readByte();
						for (int num33 = 0; num33 < b15; num33++)
						{
							ItemInInventory_HAIRMOD_PROTECTED itemInInventory_HAIRMOD_PROTECTED2 = new ItemInInventory_HAIRMOD_PROTECTED();
							sbyte b16 = msg.reader().readByte();
							itemInInventory_HAIRMOD_PROTECTED2.itemID = msg.reader().readShort();
							short num34 = msg.reader().readShort();
							if (num34 < 0)
							{
								num34 += 256;
							}
							itemInInventory_HAIRMOD_PROTECTED2.charClazz = b16;
							itemInInventory_HAIRMOD_PROTECTED2.idTemplate = num34;
							itemInInventory_HAIRMOD_PROTECTED2.plusTemplate = msg.reader().readByte();
							itemInInventory_HAIRMOD_PROTECTED2.level = msg.reader().readByte();
							itemInInventory_HAIRMOD_PROTECTED2.mDurable = msg.reader().readShort();
							itemInInventory_HAIRMOD_PROTECTED2.durable = msg.reader().readShort();
							itemInInventory_HAIRMOD_PROTECTED2.clazz = b16;
							mVector_HAIRMOD_PROTECTED4.addElement(itemInInventory_HAIRMOD_PROTECTED2);
						}
						Canvas_HAIRMOD_PROTECTED.gameScr.onNPCKeepItem(mVector_HAIRMOD_PROTECTED4);
					}
				}
				catch (Exception)
				{
					Out_HAIRMOD_PROTECTED.LogError("LOI NHAN ITEM TRONG KHO DO");
				}
				if (b13 == 3)
				{
					Canvas_HAIRMOD_PROTECTED.gameScr.onNPCSellGemItem(null);
				}
				if (b13 == 4)
				{
					sbyte b17 = msg.reader().readByte();
					sbyte[] array6 = new sbyte[b17 + 2];
					for (int num35 = 0; num35 < b17; num35++)
					{
						array6[num35] = msg.reader().readByte();
					}
					array6[b17] = msg.reader().readByte();
					array6[b17 + 1] = msg.reader().readByte();
					Canvas_HAIRMOD_PROTECTED.gameScr.onListShopOfNPC(array6);
				}
				break;
			}
			case 25:
			{
				int num = msg.reader().readUnsignedByte();
				ItemTemplate_HAIRMOD_PROTECTED.ALL_NAME_ATTRIBUTE_ITEM.clear();
				for (byte b7 = 0; b7 < num; b7++)
				{
					ItemTemplate_HAIRMOD_PROTECTED.ALL_NAME_ATTRIBUTE_ITEM.put(b7 + string.Empty, new NameAttributeItem_HAIRMOD_PROTECTED(msg.reader().readUnsignedByte(), msg.reader().readUTF(), msg.reader().readByte(), msg.reader().readByte()));
				}
				int num2 = msg.reader().readByte();
				for (int i = 0; i < num2; i++)
				{
					Res_HAIRMOD_PROTECTED.potionTemplates[i] = new PotionTemplate_HAIRMOD_PROTECTED(msg.reader().readShort(), msg.reader().readShort());
				}
				for (int j = 0; j < 5; j++)
				{
					Res_HAIRMOD_PROTECTED.percent[j] = msg.reader().readByte();
				}
				int num3 = msg.reader().readShort();
				ItemTemplate_HAIRMOD_PROTECTED[] array3 = new ItemTemplate_HAIRMOD_PROTECTED[num3 + 1];
				for (int k = 0; k < num3; k++)
				{
					int num4 = msg.reader().readShort();
					try
					{
						array3[num4] = new ItemTemplate_HAIRMOD_PROTECTED();
						array3[num4].id = (short)num4;
						array3[num4].name = msg.reader().readUTF();
						array3[num4].type = msg.reader().readByte();
						array3[num4].style = msg.reader().readByte();
						array3[num4].he = msg.reader().readByte();
						array3[num4].gender = msg.reader().readByte();
						array3[num4].level = msg.reader().readByte();
						array3[num4].durable = msg.reader().readShort();
						for (int l = 0; l < 10; l++)
						{
							array3[num4].attb[l] = msg.reader().readShort();
						}
						array3[num4].price = msg.reader().readInt();
						array3[num4].clazz = msg.reader().readByte();
						array3[num4].colorItem = msg.reader().readByte();
						array3[num4].idIcon = msg.reader().readShort();
						array3[num4].ndayLoan = msg.reader().readShort();
					}
					catch (Exception ex2)
					{
						Out_HAIRMOD_PROTECTED.println(ex2.StackTrace + " loi tai ITEM_TEMPLATE");
					}
				}
				Res_HAIRMOD_PROTECTED.itemTemplates.addElement(array3);
				short num5 = msg.reader().readShort();
				Res_HAIRMOD_PROTECTED.gemTemplate.removeAllElements();
				for (int m = 0; m < num5; m++)
				{
					GemTemp_HAIRMOD_PROTECTED gemTemp_HAIRMOD_PROTECTED = new GemTemp_HAIRMOD_PROTECTED();
					gemTemp_HAIRMOD_PROTECTED.id = msg.reader().readShort();
					gemTemp_HAIRMOD_PROTECTED.idImage = msg.reader().readByte();
					gemTemp_HAIRMOD_PROTECTED.price = msg.reader().readInt();
					gemTemp_HAIRMOD_PROTECTED.name = msg.reader().readUTF();
					string s = gemTemp_HAIRMOD_PROTECTED.name.Substring(gemTemp_HAIRMOD_PROTECTED.name.Length - 1);
					gemTemp_HAIRMOD_PROTECTED.decript = msg.reader().readUTF();
					gemTemp_HAIRMOD_PROTECTED.type = msg.reader().readByte();
					try
					{
						int num6 = int.Parse(s);
						if (num6 >= 4 && gemTemp_HAIRMOD_PROTECTED.type != 4)
						{
							gemTemp_HAIRMOD_PROTECTED.isEff = (sbyte)(num6 - 4);
						}
					}
					catch (Exception)
					{
					}
					gemTemp_HAIRMOD_PROTECTED.isSell = msg.reader().readBoolean();
					gemTemp_HAIRMOD_PROTECTED.typeEp = msg.reader().readByte();
					gemTemp_HAIRMOD_PROTECTED.typeMoney = msg.reader().readByte();
					Res_HAIRMOD_PROTECTED.gemTemplate.addElement(gemTemp_HAIRMOD_PROTECTED);
				}
				int num7 = msg.reader().readUnsignedByte();
				Res_HAIRMOD_PROTECTED.shopTemplate.removeAllElements();
				for (int n = 0; n < num7; n++)
				{
					GemTemp_HAIRMOD_PROTECTED gemTemp_HAIRMOD_PROTECTED2 = new GemTemp_HAIRMOD_PROTECTED();
					gemTemp_HAIRMOD_PROTECTED2.id = msg.reader().readUnsignedByte();
					gemTemp_HAIRMOD_PROTECTED2.idImage = msg.reader().readUnsignedByte();
					gemTemp_HAIRMOD_PROTECTED2.price = msg.reader().readInt();
					gemTemp_HAIRMOD_PROTECTED2.name = msg.reader().readUTF();
					gemTemp_HAIRMOD_PROTECTED2.decript = msg.reader().readUTF();
					gemTemp_HAIRMOD_PROTECTED2.typeMoney = msg.reader().readByte();
					gemTemp_HAIRMOD_PROTECTED2.shopType = msg.reader().readByte();
					gemTemp_HAIRMOD_PROTECTED2.isSell = msg.reader().readBoolean();
					gemTemp_HAIRMOD_PROTECTED2.value = msg.reader().readShort();
					Res_HAIRMOD_PROTECTED.shopTemplate.addElement(gemTemp_HAIRMOD_PROTECTED2);
				}
				break;
			}
			case 26:
			{
				int num197 = msg.reader().readUnsignedByte();
				Res_HAIRMOD_PROTECTED.monsterTemplates = new MonsterTemplate_HAIRMOD_PROTECTED[num197];
				short num198 = msg.reader().readShort();
				sbyte[] data5 = new sbyte[num198];
				msg.reader().read(ref data5);
				Res_HAIRMOD_PROTECTED.setEffSh(data5);
				try
				{
					num198 = msg.reader().readUnsignedByte();
					sbyte[][] array37 = new sbyte[num198][];
					for (int num199 = 0; num199 < num198; num199++)
					{
						array37[num199] = new sbyte[msg.reader().readShort()];
						msg.reader().read(ref array37[num199]);
					}
					DynamicEffect_HAIRMOD_PROTECTED.setDataEffect(array37);
				}
				catch (Exception)
				{
				}
				try
				{
					num198 = msg.reader().readUnsignedByte();
					for (int num200 = 0; num200 < num198; num200++)
					{
						int idimg = msg.reader().readShort();
						short num201 = msg.reader().readShort();
						sbyte[] data6 = new sbyte[num201];
						msg.reader().read(ref data6);
						mDataEffect_HAIRMOD_PROTECTED v = new mDataEffect_HAIRMOD_PROTECTED(data6, idimg, true);
						Pet_HAIRMOD_PROTECTED.ALL_PET_DATA.put(string.Empty + idimg, v);
					}
				}
				catch (Exception)
				{
				}
				try
				{
					int num202 = msg.reader().readUnsignedByte();
					for (int num203 = 0; num203 < num202; num203++)
					{
						int id4 = msg.reader().readShort();
						sbyte[] data7 = new sbyte[msg.reader().readShort()];
						msg.reader().read(ref data7);
						PartFrame_HAIRMOD_PROTECTED v2 = new PartFrame_HAIRMOD_PROTECTED(id4, data7);
						DataSkillEff_HAIRMOD_PROTECTED.ALL_DATA_EFF.put(id4 + string.Empty, v2);
					}
				}
				catch (Exception)
				{
				}
				try
				{
					sbyte b65 = msg.reader().readByte();
					Char_HAIRMOD_PROTECTED.dx_horse = new sbyte[b65][];
					Char_HAIRMOD_PROTECTED.dy_horse = new sbyte[b65][];
					for (int num204 = 0; num204 < b65; num204++)
					{
						Char_HAIRMOD_PROTECTED.dx_horse[num204] = new sbyte[4];
						Char_HAIRMOD_PROTECTED.dy_horse[num204] = new sbyte[4];
						for (int num205 = 0; num205 < 4; num205++)
						{
							Char_HAIRMOD_PROTECTED.dx_horse[num204][num205] = msg.reader().readByte();
						}
						for (int num206 = 0; num206 < 4; num206++)
						{
							Char_HAIRMOD_PROTECTED.dy_horse[num204][num206] = msg.reader().readByte();
						}
					}
				}
				catch (Exception)
				{
					Char_HAIRMOD_PROTECTED.dx_horse = new sbyte[1][] { new sbyte[4] { 0, 0, 7, -7 } };
					Char_HAIRMOD_PROTECTED.dy_horse = new sbyte[1][] { new sbyte[4] { -20, -15, -15, -15 } };
				}
				try
				{
					for (int num207 = 0; num207 < 2; num207++)
					{
						sbyte b66 = msg.reader().readByte();
						Char_HAIRMOD_PROTECTED.Dx_Dy_WP[num207] = new sbyte[b66];
						for (int num208 = 0; num208 < b66; num208++)
						{
							Char_HAIRMOD_PROTECTED.Dx_Dy_WP[num207][num208] = msg.reader().readByte();
						}
					}
				}
				catch (Exception)
				{
					Char_HAIRMOD_PROTECTED.Dx_Dy_WP = new sbyte[2][]
					{
						new sbyte[1],
						new sbyte[1]
					};
				}
				try
				{
					for (int num209 = 0; num209 < 2; num209++)
					{
						sbyte b67 = msg.reader().readByte();
						Char_HAIRMOD_PROTECTED.Dx_Dy_PP[num209] = new sbyte[b67];
						for (int num210 = 0; num210 < b67; num210++)
						{
							Char_HAIRMOD_PROTECTED.Dx_Dy_PP[num209][num210] = msg.reader().readByte();
						}
					}
				}
				catch (Exception)
				{
					Char_HAIRMOD_PROTECTED.Dx_Dy_PP = new sbyte[2][]
					{
						new sbyte[1],
						new sbyte[1]
					};
				}
				try
				{
					for (int num211 = 0; num211 < 2; num211++)
					{
						sbyte b68 = msg.reader().readByte();
						Char_HAIRMOD_PROTECTED.Dx_Dy_AVT[num211] = new sbyte[b68];
						for (int num212 = 0; num212 < b68; num212++)
						{
							Char_HAIRMOD_PROTECTED.Dx_Dy_AVT[num211][num212] = msg.reader().readByte();
						}
					}
					break;
				}
				catch (Exception)
				{
					Char_HAIRMOD_PROTECTED.Dx_Dy_AVT = new sbyte[2][]
					{
						new sbyte[1],
						new sbyte[1]
					};
					break;
				}
			}
			case 100:
				Canvas_HAIRMOD_PROTECTED.gameScr.onTemplateInfo(msg);
				break;
			case 58:
			{
				int num195 = msg.reader().readByte();
				int[] array36 = new int[num195];
				for (int num196 = 0; num196 < num195; num196++)
				{
					array36[num196] = msg.reader().readShort();
				}
				break;
			}
			case 24:
				Canvas_HAIRMOD_PROTECTED.endDlg();
				break;
			case 27:
			{
				short chatFromID = msg.reader().readShort();
				string text24 = msg.reader().readUTF();
				Canvas_HAIRMOD_PROTECTED.gameScr.onChat(chatFromID, text24);
				break;
			}
			case 28:
			{
				short itemID4 = msg.reader().readShort();
				Canvas_HAIRMOD_PROTECTED.gameScr.onSellItem(itemID4);
				break;
			}
			case 78:
			{
				short itemID3 = msg.reader().readShort();
				Canvas_HAIRMOD_PROTECTED.gameScr.onSellGemItem(itemID3);
				break;
			}
			case 61:
			{
				short charID5 = msg.reader().readShort();
				short itemID2 = msg.reader().readShort();
				ItemDropInfo_HAIRMOD_PROTECTED itemDropInfo_HAIRMOD_PROTECTED = new ItemDropInfo_HAIRMOD_PROTECTED();
				itemDropInfo_HAIRMOD_PROTECTED.itemCatagory = msg.reader().readByte();
				itemDropInfo_HAIRMOD_PROTECTED.itemTemplateID = msg.reader().readByte();
				itemDropInfo_HAIRMOD_PROTECTED.itemID = msg.reader().readShort();
				itemDropInfo_HAIRMOD_PROTECTED.x = msg.reader().readShort();
				itemDropInfo_HAIRMOD_PROTECTED.y = msg.reader().readShort();
				Canvas_HAIRMOD_PROTECTED.gameScr.onGiveItemToGround(charID5, itemID2, itemDropInfo_HAIRMOD_PROTECTED);
				break;
			}
			case 30:
			{
				short whoSetXP = msg.reader().readShort();
				short newpercent = msg.reader().readShort();
				int dxp = msg.reader().readInt();
				Canvas_HAIRMOD_PROTECTED.gameScr.onSetXP(whoSetXP, newpercent, dxp);
				break;
			}
			case 32:
			{
				short num193 = msg.reader().readShort();
				sbyte b64 = msg.reader().readByte();
				Property_HAIRMOD_PROTECTED[] array35 = new Property_HAIRMOD_PROTECTED[b64];
				for (int num194 = 0; num194 < b64; num194++)
				{
					array35[num194] = new Property_HAIRMOD_PROTECTED();
					array35[num194].key = msg.reader().readUTF();
					array35[num194].value = msg.reader().readInt();
				}
				if (num193 == Canvas_HAIRMOD_PROTECTED.gameScr.mainChar.ID)
				{
					Canvas_HAIRMOD_PROTECTED.gameScr.mainChar.level = msg.reader().readByte();
					Canvas_HAIRMOD_PROTECTED.gameScr.mainChar.lvpercent = msg.reader().readShort();
				}
				Canvas_HAIRMOD_PROTECTED.gameScr.onCharProperties(num193, array35);
				break;
			}
			case 33:
			{
				short idc = msg.reader().readShort();
				sbyte newlevel = msg.reader().readByte();
				Canvas_HAIRMOD_PROTECTED.gameScr.onLevelUp(idc, newlevel, msg.reader().readInt(), msg.reader().readInt());
				break;
			}
			case 35:
			{
				SkillManager_HAIRMOD_PROTECTED.SKILL_DAM_PERCENT = new short[1][][];
				for (int num84 = 0; num84 < 1; num84++)
				{
					SkillManager_HAIRMOD_PROTECTED.SKILL_DAM_PERCENT[num84] = new short[15][];
					for (int num85 = 0; num85 < 15; num85++)
					{
						SkillManager_HAIRMOD_PROTECTED.SKILL_DAM_PERCENT[num84][num85] = new short[11];
					}
				}
				for (int num86 = 0; num86 < 15; num86++)
				{
					for (int num87 = 0; num87 < 11; num87++)
					{
						SkillManager_HAIRMOD_PROTECTED.SKILL_DAM_PERCENT[0][num86][num87] = msg.reader().readShort();
					}
				}
				SkillManager_HAIRMOD_PROTECTED.SKILL_COOLDOWN = new int[1][][];
				for (int num88 = 0; num88 < 1; num88++)
				{
					SkillManager_HAIRMOD_PROTECTED.SKILL_COOLDOWN[num88] = new int[15][];
					for (int num89 = 0; num89 < 15; num89++)
					{
						SkillManager_HAIRMOD_PROTECTED.SKILL_COOLDOWN[num88][num89] = new int[11];
					}
				}
				for (int num90 = 0; num90 < 15; num90++)
				{
					for (int num91 = 0; num91 < 11; num91++)
					{
						SkillManager_HAIRMOD_PROTECTED.SKILL_COOLDOWN[0][num90][num91] = msg.reader().readShort() * 100;
					}
				}
				SkillManager_HAIRMOD_PROTECTED.SKILL_RANGE = new short[1][];
				for (int num92 = 0; num92 < 1; num92++)
				{
					SkillManager_HAIRMOD_PROTECTED.SKILL_RANGE[num92] = new short[15];
				}
				for (int num93 = 0; num93 < 15; num93++)
				{
					SkillManager_HAIRMOD_PROTECTED.SKILL_RANGE[0][num93] = msg.reader().readShort();
				}
				SkillManager_HAIRMOD_PROTECTED.SKILL_MP = new short[1][][];
				for (int num94 = 0; num94 < 1; num94++)
				{
					SkillManager_HAIRMOD_PROTECTED.SKILL_MP[num94] = new short[15][];
					for (int num95 = 0; num95 < 15; num95++)
					{
						SkillManager_HAIRMOD_PROTECTED.SKILL_MP[num94][num95] = new short[11];
					}
				}
				for (int num96 = 0; num96 < 15; num96++)
				{
					for (int num97 = 0; num97 < 11; num97++)
					{
						SkillManager_HAIRMOD_PROTECTED.SKILL_MP[0][num96][num97] = msg.reader().readUnsignedByte();
					}
				}
				SkillManager_HAIRMOD_PROTECTED.TIME_LIFE_BUFF_SKILL = new short[15][];
				for (int num98 = 0; num98 < 15; num98++)
				{
					SkillManager_HAIRMOD_PROTECTED.TIME_LIFE_BUFF_SKILL[num98] = new short[11];
				}
				for (int num99 = 0; num99 < 15; num99++)
				{
					for (int num100 = 0; num100 < 11; num100++)
					{
						SkillManager_HAIRMOD_PROTECTED.TIME_LIFE_BUFF_SKILL[num99][num100] = msg.reader().readShort();
					}
				}
				SkillManager_HAIRMOD_PROTECTED.LEVEL_ADD_SKILL = new sbyte[15][];
				for (int num101 = 0; num101 < 15; num101++)
				{
					SkillManager_HAIRMOD_PROTECTED.LEVEL_ADD_SKILL[num101] = new sbyte[11];
				}
				for (int num102 = 0; num102 < 15; num102++)
				{
					for (int num103 = 0; num103 < 11; num103++)
					{
						SkillManager_HAIRMOD_PROTECTED.LEVEL_ADD_SKILL[num102][num103] = msg.reader().readByte();
					}
				}
				SkillManager_HAIRMOD_PROTECTED.SKILL_AEO = new sbyte[5][];
				for (int num104 = 0; num104 < 5; num104++)
				{
					SkillManager_HAIRMOD_PROTECTED.SKILL_AEO[num104] = new sbyte[msg.reader().readByte()];
					for (int num105 = 0; num105 < SkillManager_HAIRMOD_PROTECTED.SKILL_AEO[num104].Length; num105++)
					{
						SkillManager_HAIRMOD_PROTECTED.SKILL_AEO[num104][num105] = msg.reader().readByte();
					}
				}
				break;
			}
			case 14:
				Canvas_HAIRMOD_PROTECTED.startOKDlg("Xin chọn tên nhân vật khác");
				break;
			case 34:
				Canvas_HAIRMOD_PROTECTED.endDlg();
				break;
			case 36:
				Canvas_HAIRMOD_PROTECTED.endDlg();
				break;
			case 48:
				Canvas_HAIRMOD_PROTECTED.gameScr.onCreadtePTOK(msg.reader().readShort());
				break;
			case 49:
				Canvas_HAIRMOD_PROTECTED.gameScr.onAdd2Party(msg);
				break;
			case 50:
				Canvas_HAIRMOD_PROTECTED.gameScr.onKickOutParty(msg);
				break;
			case 38:
			{
				string o2 = msg.reader().readUTF();
				GameScr_HAIRMOD_PROTECTED.serverInfo.addElement(o2);
				break;
			}
			case 37:
			{
				string info4 = msg.reader().readUTF();
				string url = msg.reader().readUTF();
				if (url.Equals(string.Empty))
				{
					Canvas_HAIRMOD_PROTECTED.startOKDlg(info4);
					break;
				}
				Canvas_HAIRMOD_PROTECTED.gameScr.isNewVersionAvailable = true;
				Command_HAIRMOD_PROTECTED command_HAIRMOD_PROTECTED = new Command_HAIRMOD_PROTECTED("OK");
				Command_HAIRMOD_PROTECTED command_HAIRMOD_PROTECTED2 = new Command_HAIRMOD_PROTECTED();
				command_HAIRMOD_PROTECTED2.action = (command_HAIRMOD_PROTECTED.action = delegate
				{
					Main_HAIRMOD_PROTECTED.main.platformRequest(url);
					Main_HAIRMOD_PROTECTED.main.OnApplicationQuit();
				});
				Command_HAIRMOD_PROTECTED command_HAIRMOD_PROTECTED3 = new Command_HAIRMOD_PROTECTED("Đóng");
				ActionPerform_HAIRMOD_PROTECTED action = delegate
				{
					Canvas_HAIRMOD_PROTECTED.endDlg();
				};
				command_HAIRMOD_PROTECTED3.action = action;
				Canvas_HAIRMOD_PROTECTED.msgdlg.isIcon = false;
				Canvas_HAIRMOD_PROTECTED.msgdlg.setInfo(info4, command_HAIRMOD_PROTECTED, command_HAIRMOD_PROTECTED2, command_HAIRMOD_PROTECTED3);
				Canvas_HAIRMOD_PROTECTED.currentDialog = Canvas_HAIRMOD_PROTECTED.msgdlg;
				break;
			}
			case 39:
			{
				string text16 = msg.reader().readUTF();
				bool flag2 = msg.reader().readBoolean();
				string text17 = msg.reader().readUTF();
				string text18 = msg.reader().readUTF();
				if (!flag2)
				{
					Canvas_HAIRMOD_PROTECTED.startOKDlg("Xin chọn nick khác");
					break;
				}
				ActionPerform_HAIRMOD_PROTECTED successAction = delegate
				{
					Canvas_HAIRMOD_PROTECTED.startOKDlg("Đã gửi thông tin. Vui lòng thoát game và chờ trong ít phút");
				};
				ActionPerform_HAIRMOD_PROTECTED failAction = delegate
				{
					Canvas_HAIRMOD_PROTECTED.startOKDlg("Có lỗi xảy ra. Xin hãy thử lại");
				};
				if (Main_HAIRMOD_PROTECTED.isPC)
				{
					Canvas_HAIRMOD_PROTECTED.startOKDlg("Để đăng ký tài khoản vui lòng soạn\n" + text17 + text16 + " \ngửi tới " + text18);
				}
				else
				{
					GameMidlet_HAIRMOD_PROTECTED.sendSMS(text17 + text16, "sms://" + text18, successAction, failAction);
				}
				break;
			}
			case 20:
			{
				sbyte cat = msg.reader().readByte();
				short actorID2 = msg.reader().readShort();
				Canvas_HAIRMOD_PROTECTED.gameScr.onRemoveActor(actorID2, cat);
				break;
			}
			case 51:
				Canvas_HAIRMOD_PROTECTED.gameScr.onCharUseBuff(msg);
				break;
			case 52:
				Canvas_HAIRMOD_PROTECTED.gameScr.isreceiveQuest(msg);
				break;
			case 54:
				Canvas_HAIRMOD_PROTECTED.gameScr.onFinishQuest(msg);
				break;
			case 56:
				Canvas_HAIRMOD_PROTECTED.gameScr.onInfoQuestLogin(msg);
				break;
			case 59:
				Canvas_HAIRMOD_PROTECTED.gameScr.onInfoGiftQuest(msg.reader().readUTF());
				break;
			case 57:
				break;
			case -5:
			{
				sbyte b23 = msg.reader().readByte();
				for (int num50 = 0; num50 < b23; num50++)
				{
					string text11 = msg.reader().readUTF();
					string text12 = msg.reader().readUTF();
					MessageScr_HAIRMOD_PROTECTED.gI().addTab(text11 + ": " + text12, text11, 0);
				}
				break;
			}
			case -53:
			{
				string text10 = msg.reader().readUTF();
				Canvas_HAIRMOD_PROTECTED.gameScr.onFriendList(msg, 6, text10.ToUpper());
				break;
			}
			case -7:
			{
				string text9 = msg.reader().readUTF();
				Canvas_HAIRMOD_PROTECTED.gameScr.onFriendList(msg, 1, text9.ToUpper());
				break;
			}
			case -8:
			{
				sbyte b19 = msg.reader().readByte();
				sbyte ver = msg.reader().readByte();
				sbyte[] array9 = null;
				short num41 = msg.reader().readShort();
				array9 = new sbyte[num41];
				msg.reader().read(ref array9);
				switch (b19)
				{
				case 1:
					GameData_HAIRMOD_PROTECTED.saveImgSkill(ver, array9);
					break;
				case 2:
				{
					short num42 = msg.reader().readShort();
					sbyte[] data = new sbyte[num42];
					msg.reader().read(ref data);
					sbyte b20 = msg.reader().readByte();
					sbyte[][] array10 = new sbyte[b20][];
					sbyte[][][] array11 = new sbyte[b20][][];
					for (int num43 = 0; num43 < b20; num43++)
					{
						short num44 = msg.reader().readShort();
						array10[num43] = new sbyte[num44];
						msg.reader().read(ref array10[num43]);
						short num45 = msg.reader().readByte();
						array11[num43] = new sbyte[num45][];
						for (int num46 = 0; num46 < num45; num46++)
						{
							short num47 = msg.reader().readShort();
							array11[num43][num46] = new sbyte[num47];
							msg.reader().read(ref array11[num43][num46]);
						}
					}
					GameData_HAIRMOD_PROTECTED.saveImgPotion(ver, array9, null, array10, array11);
					break;
				}
				case 3:
					GameData_HAIRMOD_PROTECTED.saveImgGem(ver, array9);
					break;
				case 4:
					AnimalInfo_HAIRMOD_PROTECTED.imgAnimal = Image_HAIRMOD_PROTECTED.createImage(array9, 0, array9.Length);
					break;
				}
				break;
			}
			case -9:
			{
				short num36 = msg.reader().readShort();
				short[] array7 = new short[num36];
				for (int num37 = 0; num37 < num36; num37++)
				{
					array7[num37] = msg.reader().readShort();
				}
				string text8 = msg.reader().readUTF();
				if (text8.Equals(string.Empty))
				{
					Canvas_HAIRMOD_PROTECTED.gameScr.onRegClan(array7);
					break;
				}
				short[] arr = array7;
				ActionPerform_HAIRMOD_PROTECTED yesAction2 = delegate
				{
					Canvas_HAIRMOD_PROTECTED.gameScr.onRegClan(arr);
					Canvas_HAIRMOD_PROTECTED.endDlg();
				};
				Canvas_HAIRMOD_PROTECTED.startYesNoDlg(text8, yesAction2);
				break;
			}
			case -10:
			{
				short idClan2 = msg.reader().readShort();
				string info3 = msg.reader().readUTF();
				Canvas_HAIRMOD_PROTECTED.gameScr.mainChar.idClan = idClan2;
				Canvas_HAIRMOD_PROTECTED.gameScr.mainChar.isMaster = 0;
				PauseMenu_HAIRMOD_PROTECTED.gI().initName();
				Canvas_HAIRMOD_PROTECTED.startOKDlg(info3);
				break;
			}
			case -11:
			{
				if (msg.reader().readByte() == 0)
				{
					short idUser1 = msg.reader().readShort();
					string info = msg.reader().readUTF();
					ActionPerform_HAIRMOD_PROTECTED yesAction = delegate
					{
						GameService_HAIRMOD_PROTECTED.gI().sendRequestAddClan(idUser1, 1, true);
						Canvas_HAIRMOD_PROTECTED.endDlg();
					};
					ActionPerform_HAIRMOD_PROTECTED noAction = delegate
					{
						GameService_HAIRMOD_PROTECTED.gI().sendRequestAddClan(idUser1, 1, false);
						Canvas_HAIRMOD_PROTECTED.endDlg();
					};
					Canvas_HAIRMOD_PROTECTED.startYesNoDlg(info, yesAction, noAction);
					break;
				}
				string info2 = msg.reader().readUTF();
				if (msg.reader().readBoolean())
				{
					short num27 = msg.reader().readShort();
					short idClan = msg.reader().readShort();
					if (num27 == Canvas_HAIRMOD_PROTECTED.gameScr.mainChar.ID)
					{
						Canvas_HAIRMOD_PROTECTED.gameScr.mainChar.idClan = num27;
						PauseMenu_HAIRMOD_PROTECTED.gI().initName();
					}
					else
					{
						Char_HAIRMOD_PROTECTED char_HAIRMOD_PROTECTED2 = (Char_HAIRMOD_PROTECTED)Canvas_HAIRMOD_PROTECTED.gameScr.findCharByID(num27);
						char_HAIRMOD_PROTECTED2.idClan = idClan;
					}
				}
				Canvas_HAIRMOD_PROTECTED.startOKDlg(info2);
				break;
			}
			case -12:
			{
				ClanInfo_HAIRMOD_PROTECTED clanInfo_HAIRMOD_PROTECTED = new ClanInfo_HAIRMOD_PROTECTED();
				clanInfo_HAIRMOD_PROTECTED.id = msg.reader().readShort();
				clanInfo_HAIRMOD_PROTECTED.name = msg.reader().readUTF();
				clanInfo_HAIRMOD_PROTECTED.master = msg.reader().readUTF();
				clanInfo_HAIRMOD_PROTECTED.level = msg.reader().readByte();
				clanInfo_HAIRMOD_PROTECTED.members = msg.reader().readShort();
				clanInfo_HAIRMOD_PROTECTED.money = msg.reader().readLong();
				clanInfo_HAIRMOD_PROTECTED.dedicationPoint = msg.reader().readLong();
				clanInfo_HAIRMOD_PROTECTED.xp = msg.reader().readLong();
				clanInfo_HAIRMOD_PROTECTED.date = msg.reader().readUTF();
				clanInfo_HAIRMOD_PROTECTED.status = msg.reader().readUTF();
				clanInfo_HAIRMOD_PROTECTED.dissolved = msg.reader().readBoolean();
				clanInfo_HAIRMOD_PROTECTED.nationID = msg.reader().readByte();
				if (clanInfo_HAIRMOD_PROTECTED.dissolved)
				{
					clanInfo_HAIRMOD_PROTECTED.strDissolved = msg.reader().readUTF();
				}
				Canvas_HAIRMOD_PROTECTED.gameScr.showClanInfo(clanInfo_HAIRMOD_PROTECTED);
				break;
			}
			case -13:
				Canvas_HAIRMOD_PROTECTED.gameScr.mainChar.idClan = -1;
				Canvas_HAIRMOD_PROTECTED.startOKDlg("Bạn bị mời khỏi bang hội.");
				break;
			case -17:
			{
				mVector_HAIRMOD_PROTECTED mVector_HAIRMOD_PROTECTED2 = new mVector_HAIRMOD_PROTECTED();
				short num24 = msg.reader().readShort();
				for (int num25 = 0; num25 < num24; num25++)
				{
					ChatInfo_HAIRMOD_PROTECTED chatInfo_HAIRMOD_PROTECTED = new ChatInfo_HAIRMOD_PROTECTED();
					chatInfo_HAIRMOD_PROTECTED.ID = msg.reader().readInt();
					chatInfo_HAIRMOD_PROTECTED.name = msg.reader().readUTF();
					chatInfo_HAIRMOD_PROTECTED.content = msg.reader().readUTF();
					mVector_HAIRMOD_PROTECTED2.addElement(chatInfo_HAIRMOD_PROTECTED);
				}
				ListScr_HAIRMOD_PROTECTED.gI().setInfo(mVector_HAIRMOD_PROTECTED2, 2, "THÔNG BÁO");
				ListScr_HAIRMOD_PROTECTED.gI().switchToMe();
				Canvas_HAIRMOD_PROTECTED.endDlg();
				break;
			}
			case -18:
			{
				string te = msg.reader().readUTF();
				MessageScr_HAIRMOD_PROTECTED.gI().addTab(te, "Bang hội", 1);
				break;
			}
			case -19:
			{
				sbyte b80 = msg.reader().readByte();
				string title = msg.reader().readUTF();
				mVector_HAIRMOD_PROTECTED mVector_HAIRMOD_PROTECTED13 = new mVector_HAIRMOD_PROTECTED();
				if (b80 == ListScr_HAIRMOD_PROTECTED.TOP_CLAN)
				{
					short num224 = msg.reader().readShort();
					for (int num225 = 0; num225 < num224; num225++)
					{
						ClanInfo_HAIRMOD_PROTECTED clanInfo_HAIRMOD_PROTECTED2 = new ClanInfo_HAIRMOD_PROTECTED();
						clanInfo_HAIRMOD_PROTECTED2.id = msg.reader().readShort();
						clanInfo_HAIRMOD_PROTECTED2.name = msg.reader().readUTF();
						clanInfo_HAIRMOD_PROTECTED2.master = msg.reader().readUTF();
						clanInfo_HAIRMOD_PROTECTED2.level = msg.reader().readByte();
						clanInfo_HAIRMOD_PROTECTED2.members = msg.reader().readShort();
						clanInfo_HAIRMOD_PROTECTED2.money = msg.reader().readLong();
						clanInfo_HAIRMOD_PROTECTED2.nationID = msg.reader().readByte();
						mVector_HAIRMOD_PROTECTED13.addElement(clanInfo_HAIRMOD_PROTECTED2);
					}
					ListScr_HAIRMOD_PROTECTED.gI().setInfo(mVector_HAIRMOD_PROTECTED13, b80, "TOP BANG HỘI");
					ListScr_HAIRMOD_PROTECTED.gI().switchToMe();
					Canvas_HAIRMOD_PROTECTED.endDlg();
				}
				else if (b80 == ListScr_HAIRMOD_PROTECTED.TOP || b80 == ListScr_HAIRMOD_PROTECTED.TOP_CUC_BO)
				{
					Canvas_HAIRMOD_PROTECTED.gameScr.onFriendList(msg, b80, title);
				}
				else if (b80 == ListScr_HAIRMOD_PROTECTED.STRONGER)
				{
					Canvas_HAIRMOD_PROTECTED.gameScr.onFriendList(msg, b80, "TOP CAO THỦ");
				}
				else
				{
					Canvas_HAIRMOD_PROTECTED.gameScr.onFriendList(msg, b80, "TOP ĐẠI GIA");
				}
				break;
			}
			case -20:
			{
				int moneyClan1 = msg.reader().readInt();
				ActionPerform_HAIRMOD_PROTECTED action3 = delegate
				{
					Canvas_HAIRMOD_PROTECTED.gameScr.mainChar.charXu += moneyClan1;
				};
				Canvas_HAIRMOD_PROTECTED.startOKDlg("Bạn nhận được " + moneyClan1 + "xu từ bang hội.", action3);
				break;
			}
			case -22:
			{
				Char_HAIRMOD_PROTECTED char_HAIRMOD_PROTECTED5 = new Char_HAIRMOD_PROTECTED();
				if (msg.reader().readByte() == 0)
				{
					char_HAIRMOD_PROTECTED5.name = msg.reader().readUTF();
					char_HAIRMOD_PROTECTED5.currentHead = msg.reader().readByte();
					char_HAIRMOD_PROTECTED5.dir = 0;
					char_HAIRMOD_PROTECTED5.level = msg.reader().readByte();
					char_HAIRMOD_PROTECTED5.weapon_frame = 0;
					sbyte b50 = msg.reader().readByte();
					mVector_HAIRMOD_PROTECTED mVector_HAIRMOD_PROTECTED10 = new mVector_HAIRMOD_PROTECTED();
					for (int num149 = 0; num149 < b50; num149++)
					{
						ItemInInventory_HAIRMOD_PROTECTED itemInInventory_HAIRMOD_PROTECTED8 = new ItemInInventory_HAIRMOD_PROTECTED();
						char_HAIRMOD_PROTECTED5.clazz = (itemInInventory_HAIRMOD_PROTECTED8.clazz = (itemInInventory_HAIRMOD_PROTECTED8.charClazz = msg.reader().readByte()));
						itemInInventory_HAIRMOD_PROTECTED8.idTemplate = msg.reader().readShort();
						itemInInventory_HAIRMOD_PROTECTED8.level = msg.reader().readByte();
						itemInInventory_HAIRMOD_PROTECTED8.plusTemplate = msg.reader().readByte();
						itemInInventory_HAIRMOD_PROTECTED8.itemID = msg.reader().readShort();
						itemInInventory_HAIRMOD_PROTECTED8.colorName = msg.reader().readByte();
						itemInInventory_HAIRMOD_PROTECTED8.attCreate = new short[5];
						for (int num150 = 0; num150 < 5; num150++)
						{
							itemInInventory_HAIRMOD_PROTECTED8.attCreate[num150] = msg.reader().readShort();
						}
						itemInInventory_HAIRMOD_PROTECTED8.heItem = msg.reader().readByte();
						itemInInventory_HAIRMOD_PROTECTED8.beKicked = msg.reader().readByte();
						itemInInventory_HAIRMOD_PROTECTED8.hangItem = msg.reader().readByte();
						itemInInventory_HAIRMOD_PROTECTED8.magic_physic = msg.reader().readByte();
						itemInInventory_HAIRMOD_PROTECTED8.islock = msg.reader().readByte();
						itemInInventory_HAIRMOD_PROTECTED8.nameCharSeal = msg.reader().readUTF();
						for (int num151 = 0; num151 < itemInInventory_HAIRMOD_PROTECTED8.newAtb.Length; num151++)
						{
							itemInInventory_HAIRMOD_PROTECTED8.newAtb[num151] = msg.reader().readByte();
						}
						for (int num152 = 0; num152 < itemInInventory_HAIRMOD_PROTECTED8.lockAtb.Length; num152++)
						{
							itemInInventory_HAIRMOD_PROTECTED8.lockAtb[num152] = msg.reader().readByte();
						}
						for (int num153 = 0; num153 < itemInInventory_HAIRMOD_PROTECTED8.addMoreLevelSkill.Length; num153++)
						{
							itemInInventory_HAIRMOD_PROTECTED8.addMoreLevelSkill[num153] = msg.reader().readByte();
						}
						mVector_HAIRMOD_PROTECTED10.addElement(itemInInventory_HAIRMOD_PROTECTED8);
						ItemTemplate_HAIRMOD_PROTECTED item2 = Res_HAIRMOD_PROTECTED.getItem(itemInInventory_HAIRMOD_PROTECTED8.charClazz, itemInInventory_HAIRMOD_PROTECTED8.idTemplate);
						if (item2.type >= 3 && item2.type < 8)
						{
							ItemTemplate_HAIRMOD_PROTECTED item3 = Res_HAIRMOD_PROTECTED.getItem(char_HAIRMOD_PROTECTED5.clazz, itemInInventory_HAIRMOD_PROTECTED8.idTemplate);
							GameService_HAIRMOD_PROTECTED.gI().doRequestWeapone(2, item2.type, item2.style, item3.colorItem);
							Canvas_HAIRMOD_PROTECTED.startWaitDlg();
						}
					}
					char_HAIRMOD_PROTECTED5.idClan = msg.reader().readShort();
					char_HAIRMOD_PROTECTED5.idFashion = msg.reader().readByte();
					for (int num154 = 0; num154 < char_HAIRMOD_PROTECTED5.idModel.Length; num154++)
					{
						char_HAIRMOD_PROTECTED5.idModel[num154] = msg.reader().readShort();
					}
					char_HAIRMOD_PROTECTED5.idAnimal = msg.reader().readByte();
					if (char_HAIRMOD_PROTECTED5.idAnimal != -1)
					{
						char_HAIRMOD_PROTECTED5.infoAnimal = msg.reader().readUTF();
					}
					char_HAIRMOD_PROTECTED5.setWearingInfo(mVector_HAIRMOD_PROTECTED10);
					if (Canvas_HAIRMOD_PROTECTED.gameScr.focusedActor != null)
					{
						char_HAIRMOD_PROTECTED5.ID = Canvas_HAIRMOD_PROTECTED.gameScr.focusedActor.ID;
					}
					Canvas_HAIRMOD_PROTECTED.gameScr.onViewCharInfo(char_HAIRMOD_PROTECTED5);
					break;
				}
				mVector_HAIRMOD_PROTECTED mVector_HAIRMOD_PROTECTED11 = new mVector_HAIRMOD_PROTECTED();
				int num155 = msg.reader().readByte();
				if (num155 > -1)
				{
					for (int num156 = 0; num156 < num155; num156++)
					{
						ItemInInventory_HAIRMOD_PROTECTED itemInInventory_HAIRMOD_PROTECTED9 = new ItemInInventory_HAIRMOD_PROTECTED();
						itemInInventory_HAIRMOD_PROTECTED9.charClazz = (itemInInventory_HAIRMOD_PROTECTED9.clazz = msg.reader().readByte());
						itemInInventory_HAIRMOD_PROTECTED9.itemID = msg.reader().readShort();
						itemInInventory_HAIRMOD_PROTECTED9.idTemplate = msg.reader().readShort();
						itemInInventory_HAIRMOD_PROTECTED9.plusTemplate = msg.reader().readByte();
						itemInInventory_HAIRMOD_PROTECTED9.level = msg.reader().readByte();
						itemInInventory_HAIRMOD_PROTECTED9.mDurable = msg.reader().readShort();
						itemInInventory_HAIRMOD_PROTECTED9.durable = msg.reader().readShort();
						itemInInventory_HAIRMOD_PROTECTED9.colorName = msg.reader().readByte();
						itemInInventory_HAIRMOD_PROTECTED9.attCreate = new short[5];
						for (int num157 = 0; num157 < 5; num157++)
						{
							itemInInventory_HAIRMOD_PROTECTED9.attCreate[num157] = msg.reader().readShort();
						}
						itemInInventory_HAIRMOD_PROTECTED9.heItem = msg.reader().readByte();
						itemInInventory_HAIRMOD_PROTECTED9.beKicked = msg.reader().readByte();
						itemInInventory_HAIRMOD_PROTECTED9.hangItem = msg.reader().readByte();
						itemInInventory_HAIRMOD_PROTECTED9.magic_physic = msg.reader().readByte();
						itemInInventory_HAIRMOD_PROTECTED9.islock = msg.reader().readByte();
						itemInInventory_HAIRMOD_PROTECTED9.nameCharSeal = msg.reader().readUTF();
						for (int num158 = 0; num158 < itemInInventory_HAIRMOD_PROTECTED9.newAtb.Length; num158++)
						{
							itemInInventory_HAIRMOD_PROTECTED9.newAtb[num158] = msg.reader().readByte();
						}
						for (int num159 = 0; num159 < itemInInventory_HAIRMOD_PROTECTED9.lockAtb.Length; num159++)
						{
							itemInInventory_HAIRMOD_PROTECTED9.lockAtb[num159] = msg.reader().readByte();
						}
						for (int num160 = 0; num160 < itemInInventory_HAIRMOD_PROTECTED9.addMoreLevelSkill.Length; num160++)
						{
							itemInInventory_HAIRMOD_PROTECTED9.addMoreLevelSkill[num160] = msg.reader().readByte();
						}
						mVector_HAIRMOD_PROTECTED11.addElement(itemInInventory_HAIRMOD_PROTECTED9);
					}
					Image_HAIRMOD_PROTECTED image_HAIRMOD_PROTECTED3 = null;
					sbyte b51 = msg.reader().readByte();
					int wimg2 = 0;
					int himg2 = 0;
					sbyte timeChangeFrame2 = 0;
					sbyte[] array30 = new sbyte[msg.reader().available()];
					while (msg.reader().available() > 0)
					{
						for (int num161 = 0; num161 < array30.Length; num161++)
						{
							array30[num161] = msg.reader().readByte();
						}
					}
					if (array30.Length != 0)
					{
						image_HAIRMOD_PROTECTED3 = Res_HAIRMOD_PROTECTED.createImgByByteArray(array30);
						timeChangeFrame2 = (sbyte)((b51 != 3) ? 6 : 3);
						if (image_HAIRMOD_PROTECTED3 != null)
						{
							wimg2 = image_HAIRMOD_PROTECTED3.getWidth();
							himg2 = image_HAIRMOD_PROTECTED3.getHeight() / b51;
						}
					}
					Canvas_HAIRMOD_PROTECTED.gameScr.onAnimalWearingInfo(Canvas_HAIRMOD_PROTECTED.gameScr.saveIDViewInfoAnimal, mVector_HAIRMOD_PROTECTED11, image_HAIRMOD_PROTECTED3, b51, wimg2, himg2, timeChangeFrame2);
				}
				else
				{
					Canvas_HAIRMOD_PROTECTED.startOKDlg("Chưa có thông tin linh thú");
				}
				break;
			}
			case 105:
			{
				int num136 = msg.reader().readByte();
				int num137 = msg.reader().readByte();
				Res_HAIRMOD_PROTECTED.locationMap = new short[num137][][];
				Res_HAIRMOD_PROTECTED.nameMap = new string[num137][][];
				for (int num138 = 0; num138 < num137; num138++)
				{
					int num139 = msg.reader().readByte();
					Res_HAIRMOD_PROTECTED.locationMap[num138] = new short[num139][];
					Res_HAIRMOD_PROTECTED.nameMap[num138] = new string[num139][];
					for (int num140 = 0; num140 < num139; num140++)
					{
						Res_HAIRMOD_PROTECTED.locationMap[num138][num140] = new short[num136 * 3];
						Res_HAIRMOD_PROTECTED.nameMap[num138][num140] = new string[num136];
						for (int num141 = 0; num141 < num136 * 3; num141++)
						{
							Res_HAIRMOD_PROTECTED.locationMap[num138][num140][num141] = msg.reader().readShort();
							if (num141 % 3 == 0)
							{
								Res_HAIRMOD_PROTECTED.nameMap[num138][num140][num141 / 3] = msg.reader().readUTF();
							}
						}
					}
				}
				num137 = msg.reader().readByte();
				GameScr_HAIRMOD_PROTECTED.mapID = new short[num137][];
				GameScr_HAIRMOD_PROTECTED.idArrMap = new short[num137];
				for (int num142 = 0; num142 < num137; num142++)
				{
					GameScr_HAIRMOD_PROTECTED.mapID[num142] = new short[num136];
					for (int num143 = 0; num143 < num136; num143++)
					{
						GameScr_HAIRMOD_PROTECTED.mapID[num142][num143] = msg.reader().readShort();
					}
					GameScr_HAIRMOD_PROTECTED.idArrMap[num142] = msg.reader().readShort();
				}
				int num144 = msg.reader().readByte();
				Res_HAIRMOD_PROTECTED.localXaphu = new short[num144][];
				for (int num145 = 0; num145 < Res_HAIRMOD_PROTECTED.localXaphu.Length; num145++)
				{
					Res_HAIRMOD_PROTECTED.localXaphu[num145] = new short[num136 * 3];
				}
				Res_HAIRMOD_PROTECTED.namMap = new string[num144][];
				for (int num146 = 0; num146 < Res_HAIRMOD_PROTECTED.namMap.Length; num146++)
				{
					Res_HAIRMOD_PROTECTED.namMap[num146] = new string[num136];
				}
				for (int num147 = 0; num147 < num144; num147++)
				{
					for (int num148 = 0; num148 < num136 * 3; num148++)
					{
						Res_HAIRMOD_PROTECTED.localXaphu[num147][num148] = msg.reader().readShort();
						if (num148 % 3 == 0)
						{
							Res_HAIRMOD_PROTECTED.namMap[num147][num148 / 3] = msg.reader().readUTF();
						}
					}
				}
				break;
			}
			case -24:
			{
				sbyte b49 = msg.reader().readByte();
				string info5 = msg.reader().readUTF();
				switch (b49)
				{
				case 0:
					HelpScr_HAIRMOD_PROTECTED.gI().setInfo(info5);
					break;
				}
				break;
			}
			case -26:
				Canvas_HAIRMOD_PROTECTED.gameScr.onMSGServer(msg.reader().readUTF());
				break;
			case -27:
			{
				int num113 = msg.reader().readByte();
				sbyte b39 = msg.reader().readByte();
				if (b39 != -1)
				{
					int num114 = msg.reader().readShort();
					sbyte[] array25 = new sbyte[num114];
					for (int num115 = 0; num115 < num114; num115++)
					{
						array25[num115] = msg.reader().readByte();
					}
					int num116 = msg.reader().readShort();
					sbyte[] array26 = new sbyte[num116];
					for (int num117 = 0; num117 < num116; num117++)
					{
						array26[num117] = msg.reader().readByte();
					}
					Image_HAIRMOD_PROTECTED image_HAIRMOD_PROTECTED = Res_HAIRMOD_PROTECTED.createImgByHeader(array25, array26);
					short dxWear = msg.reader().readByte();
					short dyWear = msg.reader().readByte();
					switch (num113)
					{
					case 0:
						CreateCharScr_HAIRMOD_PROTECTED.gI().ch.imgWpa = image_HAIRMOD_PROTECTED;
						CreateCharScr_HAIRMOD_PROTECTED.gI().ch.dxWear = dxWear;
						CreateCharScr_HAIRMOD_PROTECTED.gI().ch.dyWear = dyWear;
						break;
					case 1:
						Canvas_HAIRMOD_PROTECTED.gameScr.mainChar.weaponIndex = b39;
						Canvas_HAIRMOD_PROTECTED.gameScr.mainChar.imgWpa = image_HAIRMOD_PROTECTED;
						Canvas_HAIRMOD_PROTECTED.gameScr.mainChar.dxWear = dxWear;
						Canvas_HAIRMOD_PROTECTED.gameScr.mainChar.dyWear = dyWear;
						break;
					case 2:
						if (WindowInfoScr_HAIRMOD_PROTECTED.getIndex() == 1)
						{
							WindowInfoScr_HAIRMOD_PROTECTED.charWearing.imgWpa = image_HAIRMOD_PROTECTED;
							WindowInfoScr_HAIRMOD_PROTECTED.charWearing.dxWear = dxWear;
							WindowInfoScr_HAIRMOD_PROTECTED.charWearing.dyWear = dyWear;
						}
						else
						{
							WindowInfoScr_HAIRMOD_PROTECTED.gI().imgWeaponAvatar = image_HAIRMOD_PROTECTED;
						}
						break;
					}
				}
				Canvas_HAIRMOD_PROTECTED.endDlg();
				break;
			}
			case -30:
			{
				int userId2 = msg.reader().readShort();
				sbyte idMenu2 = msg.reader().readByte();
				int num111 = msg.reader().readByte();
				string[] array24 = new string[num111];
				for (int num112 = 0; num112 < num111; num112++)
				{
					array24[num112] = msg.reader().readUTF();
				}
				Canvas_HAIRMOD_PROTECTED.gameScr.onMenuOption(userId2, idMenu2, array24);
				break;
			}
			case -31:
			{
				int userID = msg.reader().readShort();
				sbyte idMenu = msg.reader().readByte();
				string nameText = msg.reader().readUTF();
				int typeInput = msg.reader().readByte();
				Canvas_HAIRMOD_PROTECTED.gameScr.onTextBox(userID, idMenu, nameText, typeInput);
				break;
			}
			case -32:
			{
				int idB = msg.reader().readShort();
				sbyte idPopup = msg.reader().readByte();
				string text5 = msg.reader().readUTF();
				ActionPerform_HAIRMOD_PROTECTED yesAction3 = delegate
				{
					Canvas_HAIRMOD_PROTECTED.endDlg();
					GameService_HAIRMOD_PROTECTED.gI().doCustomPopup(idB, idPopup, text5, 1);
				};
				ActionPerform_HAIRMOD_PROTECTED noAction2 = delegate
				{
					Canvas_HAIRMOD_PROTECTED.endDlg();
					GameService_HAIRMOD_PROTECTED.gI().doCustomPopup(idB, idPopup, text5, 0);
				};
				Canvas_HAIRMOD_PROTECTED.startYesNoDlg(text5, yesAction3, noAction2);
				break;
			}
			case -33:
			{
				int x4 = msg.reader().readByte() * 16;
				int y2 = msg.reader().readByte() * 16;
				short num110 = msg.reader().readShort();
				Canvas_HAIRMOD_PROTECTED.gameScr.posBoss = new Position_HAIRMOD_PROTECTED(x4, y2);
				Canvas_HAIRMOD_PROTECTED.gameScr.posBoss.index = num110;
				if (Canvas_HAIRMOD_PROTECTED.gameScr.posBoss.index == Canvas_HAIRMOD_PROTECTED.gameScr.map)
				{
					EffectManager_HAIRMOD_PROTECTED.lowEffects.addElement(new WayPoint_HAIRMOD_PROTECTED(x4, y2, num110, 0, true));
					EffectManager_HAIRMOD_PROTECTED.hiEffects.addElement(new WayPoint_HAIRMOD_PROTECTED(x4, y2, num110, 1, true));
				}
				break;
			}
			case -37:
				switch (msg.reader().readByte())
				{
				case 0:
				{
					string text19 = "`" + msg.reader().readUTF();
					switch (msg.reader().readByte())
					{
					case 0:
					{
						WindowInfoScr_HAIRMOD_PROTECTED.idMonsterClanQuest = msg.reader().readByte();
						WindowInfoScr_HAIRMOD_PROTECTED.numberQuestAll = msg.reader().readShort();
						WindowInfoScr_HAIRMOD_PROTECTED.numberQuestGet = msg.reader().readShort();
						string text20 = text19;
						text19 = text20 + "\nHoàn thành: " + WindowInfoScr_HAIRMOD_PROTECTED.numberQuestGet + "/" + WindowInfoScr_HAIRMOD_PROTECTED.numberQuestAll + "Con";
						break;
					}
					case 1:
					{
						GameScr_HAIRMOD_PROTECTED.idItemClanquest = msg.reader().readByte();
						WindowInfoScr_HAIRMOD_PROTECTED.numberQuestAll = msg.reader().readShort();
						WindowInfoScr_HAIRMOD_PROTECTED.numberQuestGet = msg.reader().readShort();
						string text21 = text19;
						text19 = text21 + "\nĐã lấy: " + WindowInfoScr_HAIRMOD_PROTECTED.numberQuestGet + "/" + WindowInfoScr_HAIRMOD_PROTECTED.numberQuestAll + text19 + text19;
						break;
					}
					case 2:
					{
						GameScr_HAIRMOD_PROTECTED.posNpcReceiveClan = new Position_HAIRMOD_PROTECTED();
						GameScr_HAIRMOD_PROTECTED.posNpcReceiveClan.index = msg.reader().readByte();
						for (int num109 = 0; num109 < Canvas_HAIRMOD_PROTECTED.gameScr.actors.size(); num109++)
						{
							Actor_HAIRMOD_PROTECTED actor_HAIRMOD_PROTECTED = (Actor_HAIRMOD_PROTECTED)Canvas_HAIRMOD_PROTECTED.gameScr.actors.elementAt(num109);
							if (actor_HAIRMOD_PROTECTED is NPC_HAIRMOD_PROTECTED && ((NPC_HAIRMOD_PROTECTED)actor_HAIRMOD_PROTECTED).type == GameScr_HAIRMOD_PROTECTED.posNpcReceiveClan.index)
							{
								GameScr_HAIRMOD_PROTECTED.posNpcReceiveClan.x = actor_HAIRMOD_PROTECTED.x;
								GameScr_HAIRMOD_PROTECTED.posNpcReceiveClan.y = actor_HAIRMOD_PROTECTED.y;
								break;
							}
						}
						break;
					}
					}
					Res_HAIRMOD_PROTECTED.PORTION_ITEM_NAME_CLAN = msg.reader().readUTF();
					WindowInfoScr_HAIRMOD_PROTECTED.gI().questClan(text19, 0);
					break;
				}
				case 1:
				{
					string content = msg.reader().readUTF();
					Canvas_HAIRMOD_PROTECTED.gameScr.onContentClanQuest(content);
					break;
				}
				case 2:
				{
					string str = msg.reader().readUTF();
					Res_HAIRMOD_PROTECTED.PORTION_ITEM_NAME_CLAN = msg.reader().readUTF();
					ViewInfoScr_HAIRMOD_PROTECTED.gI().setInfo("NHIỆM VỤ", str);
					ViewInfoScr_HAIRMOD_PROTECTED.gI().switchToMe();
					Canvas_HAIRMOD_PROTECTED.endDlg();
					break;
				}
				}
				break;
			case -38:
			{
				GameScr_HAIRMOD_PROTECTED.isFight = msg.reader().readBoolean();
				int num106 = msg.reader().readByte();
				GameScr_HAIRMOD_PROTECTED.timeChiemThanh = new long[num106];
				GameScr_HAIRMOD_PROTECTED.nameChiemThanh = new string[num106];
				GameScr_HAIRMOD_PROTECTED.curTimeCT = new long[num106];
				GameScr_HAIRMOD_PROTECTED.idChiemThanh = new short[num106];
				GameScr_HAIRMOD_PROTECTED.nameClan = new string[num106];
				for (int num107 = 0; num107 < num106; num107++)
				{
					GameScr_HAIRMOD_PROTECTED.timeChiemThanh[num107] = msg.reader().readShort();
					short num108 = msg.reader().readShort();
					GameScr_HAIRMOD_PROTECTED.nameChiemThanh[num107] = msg.reader().readUTF();
					GameScr_HAIRMOD_PROTECTED.curTimeCT[num107] = mSystem_HAIRMOD_PROTECTED.getCurrentTimeMillis() / 1000;
					Char_HAIRMOD_PROTECTED char_HAIRMOD_PROTECTED4 = (Char_HAIRMOD_PROTECTED)Canvas_HAIRMOD_PROTECTED.gameScr.findCharByID(num108);
					GameScr_HAIRMOD_PROTECTED.idChiemThanh[num107] = num108;
					if (char_HAIRMOD_PROTECTED4 != null)
					{
						char_HAIRMOD_PROTECTED4.EffFace = null;
					}
					if (char_HAIRMOD_PROTECTED4 != null && GameScr_HAIRMOD_PROTECTED.timeChiemThanh[num107] > 0)
					{
						char_HAIRMOD_PROTECTED4.EffFace = new WayPoint_HAIRMOD_PROTECTED(char_HAIRMOD_PROTECTED4.x, char_HAIRMOD_PROTECTED4.y - 5, 1, 1, false);
					}
					GameScr_HAIRMOD_PROTECTED.nameClan[num107] = msg.reader().readUTF().ToLower();
				}
				break;
			}
			case -39:
			{
				sbyte weather = msg.reader().readByte();
				int number = msg.reader().readShort();
				int timeLimit = msg.reader().readInt();
				GameScr_HAIRMOD_PROTECTED.onWeather(weather, number, timeLimit);
				break;
			}
			case -42:
			{
				short idChar = msg.reader().readShort();
				sbyte b38 = (sbyte)(msg.reader().available() / 2);
				sbyte[] array22 = new sbyte[b38];
				sbyte[] array23 = new sbyte[b38];
				int num83 = 0;
				while (msg.reader().available() > 0)
				{
					array22[num83] = msg.reader().readByte();
					array23[num83] = msg.reader().readByte();
					num83++;
				}
				Canvas_HAIRMOD_PROTECTED.gameScr.onSetCharKham(idChar, array22, array23);
				break;
			}
			case -43:
			{
				int num76 = msg.reader().readByte();
				if (num76 <= 0)
				{
					break;
				}
				Image_HAIRMOD_PROTECTED[] array20 = new Image_HAIRMOD_PROTECTED[num76];
				for (int num77 = 0; num77 < num76; num77++)
				{
					try
					{
						sbyte[] array21 = new sbyte[msg.reader().readInt()];
						for (int num78 = 0; num78 < array21.Length; num78++)
						{
							array21[num78] = msg.reader().readByte();
						}
						array20[num77] = Image_HAIRMOD_PROTECTED.createImage(array21, 0, array21.Length);
					}
					catch (Exception)
					{
					}
				}
				sbyte b37 = msg.reader().readByte();
				for (int num79 = 0; num79 < b37; num79++)
				{
					DynamicObj_HAIRMOD_PROTECTED dynamicObj_HAIRMOD_PROTECTED = new DynamicObj_HAIRMOD_PROTECTED();
					int num80 = msg.reader().readByte();
					short num81 = msg.reader().readByte();
					short num82 = msg.reader().readByte();
					dynamicObj_HAIRMOD_PROTECTED.w = msg.reader().readByte();
					dynamicObj_HAIRMOD_PROTECTED.h = msg.reader().readByte();
					dynamicObj_HAIRMOD_PROTECTED.nFrame = msg.reader().readByte();
					num81 *= 16;
					num82 *= 16;
					dynamicObj_HAIRMOD_PROTECTED.setPosTo(num81, num82);
					dynamicObj_HAIRMOD_PROTECTED.img = array20[num80];
					dynamicObj_HAIRMOD_PROTECTED.height = (short)(dynamicObj_HAIRMOD_PROTECTED.img.getHeight() / dynamicObj_HAIRMOD_PROTECTED.nFrame);
					Canvas_HAIRMOD_PROTECTED.gameScr.onDynamicObj(dynamicObj_HAIRMOD_PROTECTED);
				}
				break;
			}
			case -47:
			{
				short num74 = msg.reader().readShort();
				bool isSave = msg.reader().readBoolean();
				short num75 = msg.reader().readShort();
				sbyte[] data4 = new sbyte[num75];
				msg.reader().read(ref data4);
				if (Res_HAIRMOD_PROTECTED.monsterTemplates[num74] != null)
				{
					Res_HAIRMOD_PROTECTED.monsterTemplates[num74].onImage(isSave, data4);
				}
				break;
			}
			case -48:
			{
				sbyte b34 = msg.reader().readByte();
				if (b34 != -1)
				{
					short num72 = msg.reader().readShort();
					sbyte[] data3 = new sbyte[num72];
					msg.reader().read(ref data3);
					Res_HAIRMOD_PROTECTED.getTree(b34)?.onImage(data3, b34);
					break;
				}
				sbyte type4 = msg.reader().readByte();
				sbyte id3 = msg.reader().readByte();
				sbyte[] array19 = new sbyte[msg.reader().available()];
				for (int num73 = 0; num73 < array19.Length; num73++)
				{
					array19[num73] = msg.reader().readByte();
				}
				Canvas_HAIRMOD_PROTECTED.gameScr.onLoadCloth(array19, type4, id3);
				break;
			}
			case -49:
			{
				sbyte b32 = msg.reader().readByte();
				short num69 = msg.reader().readByte();
				if (b32 == 0)
				{
					EffectData_HAIRMOD_PROTECTED effect3 = EffectServerManager_HAIRMOD_PROTECTED.getEffect(num69);
					if (effect3 == null)
					{
						GameService_HAIRMOD_PROTECTED.gI().doRequestEffectData(num69);
					}
					EffectServerManager_HAIRMOD_PROTECTED effectServerManager_HAIRMOD_PROTECTED = new EffectServerManager_HAIRMOD_PROTECTED();
					effectServerManager_HAIRMOD_PROTECTED.ID = num69;
					effectServerManager_HAIRMOD_PROTECTED.style = msg.reader().readByte();
					effectServerManager_HAIRMOD_PROTECTED.loopLimit = (effectServerManager_HAIRMOD_PROTECTED.count = msg.reader().readByte());
					effectServerManager_HAIRMOD_PROTECTED.loop = msg.reader().readShort();
					effectServerManager_HAIRMOD_PROTECTED.loopType = msg.reader().readByte();
					if (effectServerManager_HAIRMOD_PROTECTED.loopType == 1)
					{
						effectServerManager_HAIRMOD_PROTECTED.radius = msg.reader().readShort();
					}
					else if (effectServerManager_HAIRMOD_PROTECTED.loopType == 2)
					{
						sbyte b33 = msg.reader().readByte();
						effectServerManager_HAIRMOD_PROTECTED.xLoop = new short[b33];
						effectServerManager_HAIRMOD_PROTECTED.yLoop = new short[b33];
						for (int num70 = 0; num70 < b33; num70++)
						{
							effectServerManager_HAIRMOD_PROTECTED.xLoop[num70] = msg.reader().readShort();
							effectServerManager_HAIRMOD_PROTECTED.yLoop[num70] = msg.reader().readShort();
						}
					}
					if (effectServerManager_HAIRMOD_PROTECTED.style == 0)
					{
						effectServerManager_HAIRMOD_PROTECTED.idPlayer = msg.reader().readShort();
					}
					else
					{
						effectServerManager_HAIRMOD_PROTECTED.x = msg.reader().readShort();
						effectServerManager_HAIRMOD_PROTECTED.y = msg.reader().readShort();
					}
					Canvas_HAIRMOD_PROTECTED.gameScr.onEffect(effectServerManager_HAIRMOD_PROTECTED);
				}
				else
				{
					EffectData_HAIRMOD_PROTECTED effectData_HAIRMOD_PROTECTED = new EffectData_HAIRMOD_PROTECTED();
					effectData_HAIRMOD_PROTECTED.ID = num69;
					effectData_HAIRMOD_PROTECTED.setInfo(msg);
					EffectServerManager_HAIRMOD_PROTECTED.effectList.addElement(effectData_HAIRMOD_PROTECTED);
				}
				break;
			}
			case -50:
				Canvas_HAIRMOD_PROTECTED.gameScr.setNPC_server(msg.reader().readUTF(), msg.reader().readShort(), msg.reader().readShort(), msg.reader().readShort(), msg.reader().readShort(), msg.reader().readShort(), msg.reader().readShort(), msg.reader().readByte(), msg.reader().readByte());
				break;
			case -51:
			{
				short num68 = msg.reader().readShort();
				sbyte[] data2 = new sbyte[msg.reader().available()];
				msg.reader().read(ref data2);
				ImageIcon_HAIRMOD_PROTECTED imageIcon_HAIRMOD_PROTECTED = (ImageIcon_HAIRMOD_PROTECTED)GameData_HAIRMOD_PROTECTED.listImgIcon.get(string.Empty + num68);
				imageIcon_HAIRMOD_PROTECTED.isLoad = false;
				imageIcon_HAIRMOD_PROTECTED.img = Image_HAIRMOD_PROTECTED.createImage(data2, 0, data2.Length);
				break;
			}
			case -52:
			{
				string nameItem = msg.reader().readUTF();
				short idItem = msg.reader().readShort();
				sbyte b31 = msg.reader().readByte();
				mVector_HAIRMOD_PROTECTED mVector_HAIRMOD_PROTECTED5 = new mVector_HAIRMOD_PROTECTED();
				for (int num67 = 0; num67 < b31; num67++)
				{
					Mineral_HAIRMOD_PROTECTED mineral_HAIRMOD_PROTECTED = new Mineral_HAIRMOD_PROTECTED();
					mineral_HAIRMOD_PROTECTED.name = msg.reader().readUTF();
					mineral_HAIRMOD_PROTECTED.idTemplate = msg.reader().readShort();
					mineral_HAIRMOD_PROTECTED.number = msg.reader().readByte();
					mVector_HAIRMOD_PROTECTED5.addElement(mineral_HAIRMOD_PROTECTED);
				}
				sbyte type2 = msg.reader().readByte();
				sbyte khoa2 = 0;
				try
				{
					khoa2 = msg.reader().readByte();
				}
				catch (Exception)
				{
				}
				Canvas_HAIRMOD_PROTECTED.gameScr.onCheDo(nameItem, idItem, mVector_HAIRMOD_PROTECTED5, type2, khoa2);
				break;
			}
			case -55:
				Canvas_HAIRMOD_PROTECTED.gameScr.onDropItem2Ground(msg);
				break;
			case -56:
			{
				sbyte b21 = msg.reader().readByte();
				SkillClan_HAIRMOD_PROTECTED[] array12 = null;
				array12 = new SkillClan_HAIRMOD_PROTECTED[b21];
				for (int num48 = 0; num48 < b21; num48++)
				{
					array12[num48] = new SkillClan_HAIRMOD_PROTECTED();
					array12[num48].time = msg.reader().readShort();
					array12[num48].idIcon = msg.reader().readByte();
					array12[num48].info = msg.reader().readUTF();
					array12[num48].lv = msg.reader().readByte();
				}
				sbyte b22 = msg.reader().readByte();
				SkillClan_HAIRMOD_PROTECTED[] array13 = null;
				array13 = new SkillClan_HAIRMOD_PROTECTED[b22];
				for (int num49 = 0; num49 < b22; num49++)
				{
					array13[num49] = new SkillClan_HAIRMOD_PROTECTED();
					array13[num49].time = msg.reader().readShort();
					array13[num49].idIcon = msg.reader().readByte();
					array13[num49].info = msg.reader().readUTF();
					array13[num49].lv = msg.reader().readByte();
				}
				Canvas_HAIRMOD_PROTECTED.gameScr.onSkillClan(array12, array13);
				break;
			}
			case -57:
			{
				if (msg.reader().readByte() == 0)
				{
					bool flag = msg.reader().readBoolean();
					short num14 = msg.reader().readShort();
					sbyte b8 = msg.reader().readByte();
					if (Canvas_HAIRMOD_PROTECTED.gameScr.mainChar.ID == num14)
					{
						Canvas_HAIRMOD_PROTECTED.gameScr.mainChar.isAnimal = flag;
						if (flag)
						{
							Canvas_HAIRMOD_PROTECTED.gameScr.mainChar.myAnimal = new IsAnimal_HAIRMOD_PROTECTED();
							Canvas_HAIRMOD_PROTECTED.gameScr.mainChar.myAnimal.idMonster = b8;
							Canvas_HAIRMOD_PROTECTED.gameScr.mainChar.myAnimal.type = b8;
							Canvas_HAIRMOD_PROTECTED.gameScr.mainChar.effAnimal = new WayPoint_HAIRMOD_PROTECTED(Canvas_HAIRMOD_PROTECTED.gameScr.mainChar.x, Canvas_HAIRMOD_PROTECTED.gameScr.mainChar.y - 5, 1, 1, false);
							Canvas_HAIRMOD_PROTECTED.gameScr.mainChar.timeRemoEffanimal = mSystem_HAIRMOD_PROTECTED.getCurrentTimeMillis() / 1000 + 5;
						}
						else if (Canvas_HAIRMOD_PROTECTED.gameScr.mainChar.myAnimal != null)
						{
							Canvas_HAIRMOD_PROTECTED.gameScr.mainChar.myAnimal = null;
						}
					}
					else
					{
						Char_HAIRMOD_PROTECTED char_HAIRMOD_PROTECTED = (Char_HAIRMOD_PROTECTED)Canvas_HAIRMOD_PROTECTED.gameScr.findCharByID(num14);
						if (char_HAIRMOD_PROTECTED != null)
						{
							char_HAIRMOD_PROTECTED.isAnimal = flag;
							if (flag)
							{
								char_HAIRMOD_PROTECTED.myAnimal = new IsAnimal_HAIRMOD_PROTECTED();
								char_HAIRMOD_PROTECTED.myAnimal.idMonster = b8;
								char_HAIRMOD_PROTECTED.myAnimal.type = b8;
								char_HAIRMOD_PROTECTED.effAnimal = new WayPoint_HAIRMOD_PROTECTED(char_HAIRMOD_PROTECTED.x, char_HAIRMOD_PROTECTED.y - 5, 1, 1, false);
								char_HAIRMOD_PROTECTED.timeRemoEffanimal = mSystem_HAIRMOD_PROTECTED.getCurrentTimeMillis() / 1000 + 5;
							}
							else if (char_HAIRMOD_PROTECTED.myAnimal != null)
							{
								char_HAIRMOD_PROTECTED.myAnimal = null;
							}
						}
					}
					if (IsAnimal_HAIRMOD_PROTECTED.img[b8] == null)
					{
						Canvas_HAIRMOD_PROTECTED.gameScr.gameService.sendGetImgIsanimal(b8, num14);
					}
					break;
				}
				sbyte b9 = msg.reader().readByte();
				int num15 = msg.reader().readShort();
				sbyte[] array4 = new sbyte[num15];
				for (int num16 = 0; num16 < num15; num16++)
				{
					array4[num16] = msg.reader().readByte();
				}
				IsAnimal_HAIRMOD_PROTECTED.hImg[b9] = msg.reader().readByte();
				IsAnimal_HAIRMOD_PROTECTED.img[b9] = Res_HAIRMOD_PROTECTED.createImgByByteArray(array4);
				IsAnimal_HAIRMOD_PROTECTED.wimg[b9] = IsAnimal_HAIRMOD_PROTECTED.img[b9].getWidth();
				sbyte b10 = msg.reader().readByte();
				IsAnimal_HAIRMOD_PROTECTED.frameRun[b9] = new sbyte[4][];
				for (int num17 = 0; num17 < 4; num17++)
				{
					IsAnimal_HAIRMOD_PROTECTED.frameRun[b9][num17] = new sbyte[b10];
				}
				for (int num18 = 0; num18 < IsAnimal_HAIRMOD_PROTECTED.frameRun[b9][0].Length; num18++)
				{
					IsAnimal_HAIRMOD_PROTECTED.frameRun[b9][0][num18] = msg.reader().readByte();
					IsAnimal_HAIRMOD_PROTECTED.frameRun[b9][1][num18] = msg.reader().readByte();
					IsAnimal_HAIRMOD_PROTECTED.frameRun[b9][2][num18] = msg.reader().readByte();
					IsAnimal_HAIRMOD_PROTECTED.frameRun[b9][3][num18] = msg.reader().readByte();
				}
				sbyte b11 = msg.reader().readByte();
				IsAnimal_HAIRMOD_PROTECTED.frameStand[b9] = new sbyte[4][];
				for (int num19 = 0; num19 < 4; num19++)
				{
					IsAnimal_HAIRMOD_PROTECTED.frameStand[b9][num19] = new sbyte[b11];
				}
				for (int num20 = 0; num20 < IsAnimal_HAIRMOD_PROTECTED.frameStand[b9][0].Length; num20++)
				{
					IsAnimal_HAIRMOD_PROTECTED.frameStand[b9][0][num20] = msg.reader().readByte();
					IsAnimal_HAIRMOD_PROTECTED.frameStand[b9][1][num20] = msg.reader().readByte();
					IsAnimal_HAIRMOD_PROTECTED.frameStand[b9][2][num20] = msg.reader().readByte();
					IsAnimal_HAIRMOD_PROTECTED.frameStand[b9][3][num20] = msg.reader().readByte();
				}
				sbyte b12 = msg.reader().readByte();
				IsAnimal_HAIRMOD_PROTECTED.frameAt[b9] = new sbyte[4][];
				for (int num21 = 0; num21 < 4; num21++)
				{
					IsAnimal_HAIRMOD_PROTECTED.frameAt[b9][num21] = new sbyte[b12];
				}
				for (int num22 = 0; num22 < IsAnimal_HAIRMOD_PROTECTED.frameAt[b9][0].Length; num22++)
				{
					IsAnimal_HAIRMOD_PROTECTED.frameAt[b9][0][num22] = msg.reader().readByte();
					IsAnimal_HAIRMOD_PROTECTED.frameAt[b9][1][num22] = msg.reader().readByte();
					IsAnimal_HAIRMOD_PROTECTED.frameAt[b9][2][num22] = msg.reader().readByte();
					IsAnimal_HAIRMOD_PROTECTED.frameAt[b9][3][num22] = msg.reader().readByte();
				}
				IsAnimal_HAIRMOD_PROTECTED.addx[b9] = msg.reader().readByte();
				IsAnimal_HAIRMOD_PROTECTED.addy[b9] = msg.reader().readByte();
				break;
			}
			case -59:
			{
				sbyte b3 = msg.reader().readByte();
				for (sbyte b4 = 0; b4 < b3; b4++)
				{
					sbyte idFarm = msg.reader().readByte();
					sbyte id2 = msg.reader().readByte();
					string text6 = msg.reader().readUTF();
					sbyte idImgTree = msg.reader().readByte();
					int timeLive = msg.reader().readInt();
					sbyte b5 = msg.reader().readByte();
					sbyte b6 = msg.reader().readByte();
					bool isBuyOk = msg.reader().readBoolean();
					sbyte addy = msg.reader().readByte();
					sbyte colorString = msg.reader().readByte();
					Canvas_HAIRMOD_PROTECTED.gameScr.setMyFarm(idFarm, id2, b5 * 16, b6 * 16, text6, text6, idImgTree, timeLive, isBuyOk, addy, colorString);
				}
				Canvas_HAIRMOD_PROTECTED.gameScr.xBeginFrame = (short)getMinMax(Canvas_HAIRMOD_PROTECTED.gameScr.actors, 0, 5000);
				Canvas_HAIRMOD_PROTECTED.gameScr.yBeginFrame = (short)getMinMax(Canvas_HAIRMOD_PROTECTED.gameScr.actors, 1, 5000);
				Canvas_HAIRMOD_PROTECTED.gameScr.xTheendFrame = (short)getMinMax(Canvas_HAIRMOD_PROTECTED.gameScr.actors, 2, -1);
				Canvas_HAIRMOD_PROTECTED.gameScr.yTheendFrame = (short)getMinMax(Canvas_HAIRMOD_PROTECTED.gameScr.actors, 3, -1);
				break;
			}
			case -60:
				Canvas_HAIRMOD_PROTECTED.gameScr.addMsgWorld(msg.reader().readUTF());
				break;
			case -62:
				switch (msg.reader().readByte())
				{
				case 0:
					MiniGame_HAIRMOD_PROTECTED.gI().switchToMe();
					MiniGame_HAIRMOD_PROTECTED.gI().onMiniGame(msg);
					break;
				case 1:
				{
					sbyte id = msg.reader().readByte();
					string infoBox = msg.reader().readUTF();
					sbyte b = msg.reader().readByte();
					string[] array2 = new string[b];
					for (sbyte b2 = 0; b2 < b; b2++)
					{
						array2[b2] = msg.reader().readUTF();
					}
					MiniGame_HAIRMOD_PROTECTED.gI().getInfoBox(id, infoBox, array2);
					break;
				}
				case 2:
					MiniGame_HAIRMOD_PROTECTED.gI().reSetGame();
					Canvas_HAIRMOD_PROTECTED.gameScr.switchToMe();
					break;
				}
				break;
			case -63:
				MTickets_HAIRMOD_PROTECTED.gi().onHappy(msg);
				MTickets_HAIRMOD_PROTECTED.gi().switchToMe();
				break;
			case -61:
			{
				sbyte idColor = msg.reader().readByte();
				sbyte lv = msg.reader().readByte();
				sbyte slNguyenLieu = msg.reader().readByte();
				short startIDNL = msg.reader().readShort();
				sbyte magic = msg.reader().readByte();
				sbyte khoa = 0;
				try
				{
					khoa = msg.reader().readByte();
				}
				catch (Exception)
				{
				}
				Canvas_HAIRMOD_PROTECTED.gameScr.onEpDothu(1000, idColor, lv, startIDNL, slNguyenLieu, magic, khoa);
				break;
			}
			case 87:
				Canvas_HAIRMOD_PROTECTED.gameScr.onLuckyDraw(msg);
				break;
			case -72:
			case -58:
			case -54:
			case -46:
			case -45:
			case -44:
			case -40:
			case -29:
			case -28:
			case -25:
			case -23:
			case -21:
			case -16:
			case -15:
			case -14:
			case -6:
			case -4:
			case -3:
			case -2:
			case -1:
			case 0:
			case 29:
			case 31:
			case 40:
			case 41:
			case 42:
			case 43:
			case 44:
			case 45:
			case 46:
			case 47:
			case 53:
			case 55:
			case 63:
			case 70:
			case 74:
			case 75:
			case 79:
			case 84:
			case 88:
			case 93:
			case 96:
			case 97:
			case 98:
			case 99:
				break;
			}
		}
		catch (Exception ex47)
		{
			Out_HAIRMOD_PROTECTED.println(" Loi tai >>>>>>>>>>>> cmd thu >>  " + msg.command + " ly do" + ex47.StackTrace);
		}
	}

	public int getMinMax(mVector_HAIRMOD_PROTECTED data, sbyte typeXY, int aa)
	{
		for (int i = 0; i < data.size(); i++)
		{
			Actor_HAIRMOD_PROTECTED actor_HAIRMOD_PROTECTED = (Actor_HAIRMOD_PROTECTED)data.elementAt(i);
			if (actor_HAIRMOD_PROTECTED.catagory != Actor_HAIRMOD_PROTECTED.CAT_MY_GROUND)
			{
				continue;
			}
			Ground_HAIRMOD_PROTECTED ground_HAIRMOD_PROTECTED = (Ground_HAIRMOD_PROTECTED)actor_HAIRMOD_PROTECTED;
			switch (typeXY)
			{
			case 0:
				if (aa > ground_HAIRMOD_PROTECTED.x)
				{
					aa = ground_HAIRMOD_PROTECTED.x;
				}
				break;
			case 1:
				if (aa > ground_HAIRMOD_PROTECTED.y)
				{
					aa = ground_HAIRMOD_PROTECTED.y;
				}
				break;
			case 2:
				if (aa < ground_HAIRMOD_PROTECTED.x)
				{
					aa = ground_HAIRMOD_PROTECTED.x;
				}
				break;
			case 3:
				if (aa < ground_HAIRMOD_PROTECTED.y)
				{
					aa = ground_HAIRMOD_PROTECTED.y;
				}
				break;
			}
		}
		return aa;
	}

	public int getIndexClassFromItemID(int itemID)
	{
		if (itemID >= 79 && itemID <= 113)
		{
			return (itemID - 79) / 7;
		}
		if (itemID >= 174 && itemID <= 213)
		{
			return (itemID - 174) / 8;
		}
		if (itemID >= 214 && itemID <= 263)
		{
			return (itemID - 214) / 10;
		}
		return 0;
	}
}
