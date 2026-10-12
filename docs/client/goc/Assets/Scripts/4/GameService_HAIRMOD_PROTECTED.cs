using System;
using System.IO;
using UnityEngine;

public class GameService_HAIRMOD_PROTECTED : Cmd_message_HAIRMOD_PROTECTED
{
	public ISession_HAIRMOD_PROTECTED session;

	protected static GameService_HAIRMOD_PROTECTED instance;

	public static GameService_HAIRMOD_PROTECTED gI()
	{
		if (instance == null)
		{
			instance = new GameService_HAIRMOD_PROTECTED();
		}
		return instance;
	}

	private Message_HAIRMOD_PROTECTED init(sbyte cmd)
	{
		return new Message_HAIRMOD_PROTECTED(cmd);
	}

	public void giveCard2NPC(int x, int y)
	{
		Message_HAIRMOD_PROTECTED message_HAIRMOD_PROTECTED = init(-25);
		try
		{
			message_HAIRMOD_PROTECTED.writer().writeByte((sbyte)x);
			message_HAIRMOD_PROTECTED.writer().writeByte((sbyte)y);
			session.sendMessage(message_HAIRMOD_PROTECTED);
		}
		catch (Exception)
		{
		}
	}

	public void dosendKhacMenu()
	{
		Message_HAIRMOD_PROTECTED message_HAIRMOD_PROTECTED = init(-76);
		try
		{
			message_HAIRMOD_PROTECTED.writer().writeShort(-10000);
			session.sendMessage(message_HAIRMOD_PROTECTED);
			message_HAIRMOD_PROTECTED.cleanup();
		}
		catch (Exception)
		{
		}
	}

	public void playMiniGame(sbyte type, sbyte id)
	{
		Message_HAIRMOD_PROTECTED message_HAIRMOD_PROTECTED = init(-62);
		try
		{
			message_HAIRMOD_PROTECTED.writer().writeByte(type);
			if (id != 100)
			{
				message_HAIRMOD_PROTECTED.writer().writeByte(id);
			}
			session.sendMessage(message_HAIRMOD_PROTECTED);
		}
		catch (Exception)
		{
		}
	}

	public void learnSkill(int cClass, int idSelect)
	{
		Message_HAIRMOD_PROTECTED message_HAIRMOD_PROTECTED = init(109);
		try
		{
			message_HAIRMOD_PROTECTED.writer().writeByte((sbyte)cClass);
			message_HAIRMOD_PROTECTED.writer().writeByte((sbyte)idSelect);
			session.sendMessage(message_HAIRMOD_PROTECTED);
		}
		catch (Exception)
		{
		}
	}

	public void unRideHorse(sbyte type)
	{
		Message_HAIRMOD_PROTECTED message_HAIRMOD_PROTECTED = init(108);
		try
		{
			message_HAIRMOD_PROTECTED.writer().writeByte(type);
			session.sendMessage(message_HAIRMOD_PROTECTED);
		}
		catch (Exception)
		{
		}
	}

	public void setConfig(int type)
	{
		Message_HAIRMOD_PROTECTED message_HAIRMOD_PROTECTED = init(104);
		try
		{
			message_HAIRMOD_PROTECTED.writer().writeByte((sbyte)type);
			session.sendMessage(message_HAIRMOD_PROTECTED);
		}
		catch (Exception)
		{
		}
	}

	public void onSendNapSMS(int type, string cardType, string cardID, string cardCode)
	{
		Message_HAIRMOD_PROTECTED message_HAIRMOD_PROTECTED = init(107);
		try
		{
			message_HAIRMOD_PROTECTED.writer().writeByte((sbyte)type);
			message_HAIRMOD_PROTECTED.writer().writeUTF(cardType);
			message_HAIRMOD_PROTECTED.writer().writeUTF(cardID);
			message_HAIRMOD_PROTECTED.writer().writeUTF(cardCode);
			session.sendMessage(message_HAIRMOD_PROTECTED);
		}
		catch (Exception)
		{
		}
	}

	public void requestGetListUser(int npcType, int shopID)
	{
		Message_HAIRMOD_PROTECTED message_HAIRMOD_PROTECTED = init(94);
		try
		{
			message_HAIRMOD_PROTECTED.writer().writeByte(0);
			message_HAIRMOD_PROTECTED.writer().writeByte((sbyte)npcType);
			message_HAIRMOD_PROTECTED.writer().writeByte((sbyte)shopID);
			session.sendMessage(message_HAIRMOD_PROTECTED);
			message_HAIRMOD_PROTECTED.cleanup();
		}
		catch (Exception)
		{
		}
	}

	public void requestGetListItem(int npcType, int shopID, int charID)
	{
		Message_HAIRMOD_PROTECTED message_HAIRMOD_PROTECTED = init(94);
		try
		{
			message_HAIRMOD_PROTECTED.writer().writeByte(1);
			message_HAIRMOD_PROTECTED.writer().writeByte((sbyte)npcType);
			message_HAIRMOD_PROTECTED.writer().writeByte((sbyte)shopID);
			message_HAIRMOD_PROTECTED.writer().writeShort((short)charID);
			session.sendMessage(message_HAIRMOD_PROTECTED);
			message_HAIRMOD_PROTECTED.cleanup();
		}
		catch (Exception)
		{
		}
	}

	public void requestGetListItemMain(int npcType, int shopID, int charID)
	{
		Message_HAIRMOD_PROTECTED message_HAIRMOD_PROTECTED = init(94);
		try
		{
			message_HAIRMOD_PROTECTED.writer().writeByte(2);
			message_HAIRMOD_PROTECTED.writer().writeByte((sbyte)npcType);
			message_HAIRMOD_PROTECTED.writer().writeByte((sbyte)shopID);
			message_HAIRMOD_PROTECTED.writer().writeShort((short)charID);
			session.sendMessage(message_HAIRMOD_PROTECTED);
			message_HAIRMOD_PROTECTED.cleanup();
		}
		catch (Exception)
		{
		}
	}

	public void requestSellItem(int typeNpc, int shopID, ItemInInventory_HAIRMOD_PROTECTED itemSell, short real, int prize, bool isSelling)
	{
		Message_HAIRMOD_PROTECTED message_HAIRMOD_PROTECTED = init(92);
		try
		{
			message_HAIRMOD_PROTECTED.writer().writeBoolean(isSelling);
			message_HAIRMOD_PROTECTED.writer().writeByte((sbyte)typeNpc);
			message_HAIRMOD_PROTECTED.writer().writeByte((sbyte)shopID);
			if (itemSell != null)
			{
				message_HAIRMOD_PROTECTED.writer().writeShort(itemSell.itemID);
			}
			else
			{
				message_HAIRMOD_PROTECTED.writer().writeShort(real);
			}
			message_HAIRMOD_PROTECTED.writer().writeInt(prize);
			if (itemSell != null)
			{
				message_HAIRMOD_PROTECTED.writer().writeByte(0);
			}
			else
			{
				message_HAIRMOD_PROTECTED.writer().writeByte(1);
			}
			session.sendMessage(message_HAIRMOD_PROTECTED);
			message_HAIRMOD_PROTECTED.cleanup();
		}
		catch (Exception)
		{
		}
	}

	public void requestBuyItemUser(int charID, int npcID, int shopID, int itemID, int type)
	{
		Message_HAIRMOD_PROTECTED message_HAIRMOD_PROTECTED = init(95);
		try
		{
			message_HAIRMOD_PROTECTED.writer().writeByte((sbyte)npcID);
			message_HAIRMOD_PROTECTED.writer().writeByte((sbyte)shopID);
			message_HAIRMOD_PROTECTED.writer().writeShort((short)charID);
			message_HAIRMOD_PROTECTED.writer().writeShort((short)itemID);
			message_HAIRMOD_PROTECTED.writer().writeByte((sbyte)type);
			session.sendMessage(message_HAIRMOD_PROTECTED);
			message_HAIRMOD_PROTECTED.cleanup();
		}
		catch (Exception)
		{
		}
	}

	public void sendGetMonsterTEmplate(int idTemplate)
	{
		Message_HAIRMOD_PROTECTED message_HAIRMOD_PROTECTED = init(100);
		try
		{
			message_HAIRMOD_PROTECTED.writer().writeShort((short)idTemplate);
			session.sendMessage(message_HAIRMOD_PROTECTED);
			message_HAIRMOD_PROTECTED.cleanup();
		}
		catch (Exception)
		{
		}
	}

	public void sendGetMonsterTEmplate(int type, int idTemplate)
	{
		Message_HAIRMOD_PROTECTED message_HAIRMOD_PROTECTED = init(100);
		try
		{
			message_HAIRMOD_PROTECTED.writer().writeByte((sbyte)type);
			message_HAIRMOD_PROTECTED.writer().writeShort((short)idTemplate);
			session.sendMessage(message_HAIRMOD_PROTECTED);
			message_HAIRMOD_PROTECTED.cleanup();
		}
		catch (Exception)
		{
		}
	}

	public void doUseSpecialItem(int realID)
	{
		Message_HAIRMOD_PROTECTED message_HAIRMOD_PROTECTED = init(86);
		try
		{
			message_HAIRMOD_PROTECTED.writer().writeShort((short)realID);
			session.sendMessage(message_HAIRMOD_PROTECTED);
			message_HAIRMOD_PROTECTED.cleanup();
		}
		catch (Exception)
		{
		}
	}

	public void buyItemShopSpecial(int id)
	{
		Message_HAIRMOD_PROTECTED message_HAIRMOD_PROTECTED = init(85);
		try
		{
			message_HAIRMOD_PROTECTED.writer().writeByte((sbyte)id);
			session.sendMessage(message_HAIRMOD_PROTECTED);
			message_HAIRMOD_PROTECTED.cleanup();
		}
		catch (Exception)
		{
		}
	}

	public void moveUpBoard()
	{
		Message_HAIRMOD_PROTECTED message = init(82);
		try
		{
			session.sendMessage(message);
		}
		catch (Exception)
		{
		}
	}

	public void doMove2Map(sbyte index, int mapid)
	{
		Message_HAIRMOD_PROTECTED message_HAIRMOD_PROTECTED = init(79);
		try
		{
			message_HAIRMOD_PROTECTED.writer().writeByte(index);
			message_HAIRMOD_PROTECTED.writer().writeShort((short)mapid);
			session.sendMessage(message_HAIRMOD_PROTECTED);
		}
		catch (Exception)
		{
		}
	}

	public void addItemImbue(short itemID)
	{
		Message_HAIRMOD_PROTECTED message_HAIRMOD_PROTECTED = init(75);
		try
		{
			message_HAIRMOD_PROTECTED.writer().writeShort(itemID);
			session.sendMessage(message_HAIRMOD_PROTECTED);
		}
		catch (Exception)
		{
		}
	}

	public void addGemItemImbue(short gemId, sbyte boVaoLayRa)
	{
		Message_HAIRMOD_PROTECTED message_HAIRMOD_PROTECTED = init(76);
		try
		{
			message_HAIRMOD_PROTECTED.writer().writeShort(gemId);
			message_HAIRMOD_PROTECTED.writer().writeByte(boVaoLayRa);
			session.sendMessage(message_HAIRMOD_PROTECTED);
		}
		catch (Exception)
		{
		}
	}

	public void repairItem(int id)
	{
		Message_HAIRMOD_PROTECTED message_HAIRMOD_PROTECTED = init(72);
		try
		{
			message_HAIRMOD_PROTECTED.writer().writeByte((sbyte)id);
			session.sendMessage(message_HAIRMOD_PROTECTED);
		}
		catch (Exception)
		{
		}
	}

	public void requestImbue(int type)
	{
		Message_HAIRMOD_PROTECTED message_HAIRMOD_PROTECTED = init(71);
		try
		{
			message_HAIRMOD_PROTECTED.writer().writeByte((sbyte)type);
			session.sendMessage(message_HAIRMOD_PROTECTED);
			message_HAIRMOD_PROTECTED.cleanup();
		}
		catch (Exception)
		{
			Debug.Log("loi tai requestImbue game service");
		}
	}

	public void doImbueItem(int ok)
	{
		Message_HAIRMOD_PROTECTED message_HAIRMOD_PROTECTED = init(77);
		try
		{
			message_HAIRMOD_PROTECTED.writer().writeByte((sbyte)ok);
			session.sendMessage(message_HAIRMOD_PROTECTED);
			message_HAIRMOD_PROTECTED.cleanup();
		}
		catch (Exception)
		{
		}
	}

	public void login(string username, string pass, string version)
	{
		Message_HAIRMOD_PROTECTED message_HAIRMOD_PROTECTED = init(1);
		try
		{
			message_HAIRMOD_PROTECTED.writer().writeUTF(username);
			message_HAIRMOD_PROTECTED.writer().writeUTF(pass);
			message_HAIRMOD_PROTECTED.writer().writeUTF(version);
			message_HAIRMOD_PROTECTED.writer().writeUTF("nokia/1/1");
			message_HAIRMOD_PROTECTED.writer().writeUTF(GameMidlet_HAIRMOD_PROTECTED.client_Provider);
			message_HAIRMOD_PROTECTED.writer().writeUTF(GameMidlet_HAIRMOD_PROTECTED.provider);
			message_HAIRMOD_PROTECTED.writer().writeUTF(GameMidlet_HAIRMOD_PROTECTED.agent);
			message_HAIRMOD_PROTECTED.writer().writeByte((sbyte)((!Main_HAIRMOD_PROTECTED.isPC) ? 2 : 4));
			message_HAIRMOD_PROTECTED.writer().writeShort((short)Canvas_HAIRMOD_PROTECTED.w);
			message_HAIRMOD_PROTECTED.writer().writeByte((sbyte)(Canvas_HAIRMOD_PROTECTED.isLoadTileServer ? (-1) : 0));
			message_HAIRMOD_PROTECTED.writer().writeByte(getInDexServer(ServerListScr_HAIRMOD_PROTECTED.index));
			session.sendMessage(message_HAIRMOD_PROTECTED);
			message_HAIRMOD_PROTECTED.cleanup();
			Out_HAIRMOD_PROTECTED.println((short)Canvas_HAIRMOD_PROTECTED.w + ".w><><h ." + (short)Canvas_HAIRMOD_PROTECTED.h);
		}
		catch (Exception)
		{
		}
	}

	public sbyte getInDexServer(int index)
	{
		if (index > ServerListScr_HAIRMOD_PROTECTED.index_server.Length - 1)
		{
			return 0;
		}
		return ServerListScr_HAIRMOD_PROTECTED.index_server[index];
	}

	public void requestKiller()
	{
		Message_HAIRMOD_PROTECTED message_HAIRMOD_PROTECTED = init(67);
		try
		{
			message_HAIRMOD_PROTECTED.writer().writeByte(1);
			session.sendMessage(message_HAIRMOD_PROTECTED);
			message_HAIRMOD_PROTECTED.cleanup();
		}
		catch (Exception)
		{
		}
	}

	public void requestCreateParty(int charID)
	{
		Message_HAIRMOD_PROTECTED message_HAIRMOD_PROTECTED = init(48);
		try
		{
			message_HAIRMOD_PROTECTED.writer().writeInt(charID);
			session.sendMessage(message_HAIRMOD_PROTECTED);
			message_HAIRMOD_PROTECTED.cleanup();
		}
		catch (Exception)
		{
		}
	}

	public void invite2Party(int idUser)
	{
		Message_HAIRMOD_PROTECTED message_HAIRMOD_PROTECTED = init(49);
		try
		{
			message_HAIRMOD_PROTECTED.writer().writeByte(0);
			message_HAIRMOD_PROTECTED.writer().writeShort((short)idUser);
			session.sendMessage(message_HAIRMOD_PROTECTED);
			message_HAIRMOD_PROTECTED.cleanup();
		}
		catch (Exception)
		{
		}
	}

	public void trade(int idUser)
	{
		Message_HAIRMOD_PROTECTED message_HAIRMOD_PROTECTED = init(66);
		try
		{
			message_HAIRMOD_PROTECTED.writer().writeByte(0);
			message_HAIRMOD_PROTECTED.writer().writeShort((short)idUser);
			session.sendMessage(message_HAIRMOD_PROTECTED);
			message_HAIRMOD_PROTECTED.cleanup();
		}
		catch (Exception)
		{
		}
	}

	public void sendInfoAddFriend(int charID, int ok)
	{
		Message_HAIRMOD_PROTECTED message_HAIRMOD_PROTECTED = init(101);
		try
		{
			message_HAIRMOD_PROTECTED.writer().writeShort((short)charID);
			message_HAIRMOD_PROTECTED.writer().writeByte((sbyte)ok);
			session.sendMessage(message_HAIRMOD_PROTECTED);
		}
		catch (Exception)
		{
		}
	}

	public void getFriendList()
	{
		Message_HAIRMOD_PROTECTED message_HAIRMOD_PROTECTED = init(102);
		try
		{
			message_HAIRMOD_PROTECTED.writer().writeByte(0);
			session.sendMessage(message_HAIRMOD_PROTECTED);
		}
		catch (Exception)
		{
		}
	}

	public void getDetailFriend(int charID)
	{
		Message_HAIRMOD_PROTECTED message_HAIRMOD_PROTECTED = init(102);
		try
		{
			message_HAIRMOD_PROTECTED.writer().writeByte(1);
			message_HAIRMOD_PROTECTED.writer().writeInt(charID);
			session.sendMessage(message_HAIRMOD_PROTECTED);
		}
		catch (Exception)
		{
		}
	}

	public void sendOk_notOkInviteTrafe(int userid, int ok)
	{
		Message_HAIRMOD_PROTECTED message_HAIRMOD_PROTECTED = init(66);
		try
		{
			message_HAIRMOD_PROTECTED.writer().writeByte((sbyte)ok);
			message_HAIRMOD_PROTECTED.writer().writeShort((short)userid);
			session.sendMessage(message_HAIRMOD_PROTECTED);
			message_HAIRMOD_PROTECTED.cleanup();
		}
		catch (Exception)
		{
		}
	}

	public void sendOkTrade(int userid)
	{
		Message_HAIRMOD_PROTECTED message_HAIRMOD_PROTECTED = init(66);
		try
		{
			message_HAIRMOD_PROTECTED.writer().writeByte(4);
			message_HAIRMOD_PROTECTED.writer().writeShort((short)userid);
			session.sendMessage(message_HAIRMOD_PROTECTED);
			message_HAIRMOD_PROTECTED.cleanup();
		}
		catch (Exception)
		{
		}
	}

	public void sendCancelTrade(int userid)
	{
		Message_HAIRMOD_PROTECTED message_HAIRMOD_PROTECTED = init(66);
		try
		{
			message_HAIRMOD_PROTECTED.writer().writeByte(3);
			message_HAIRMOD_PROTECTED.writer().writeShort((short)userid);
			session.sendMessage(message_HAIRMOD_PROTECTED);
			message_HAIRMOD_PROTECTED.cleanup();
		}
		catch (Exception)
		{
		}
	}

	public void sendAddItemTrade(short itemID, int type, int number)
	{
		Message_HAIRMOD_PROTECTED message_HAIRMOD_PROTECTED = init(66);
		try
		{
			message_HAIRMOD_PROTECTED.writer().writeByte(2);
			message_HAIRMOD_PROTECTED.writer().writeByte((sbyte)type);
			switch (type)
			{
			case 0:
				message_HAIRMOD_PROTECTED.writer().writeShort(itemID);
				break;
			case 1:
				message_HAIRMOD_PROTECTED.writer().writeByte((sbyte)itemID);
				message_HAIRMOD_PROTECTED.writer().writeShort((short)number);
				break;
			case 2:
				message_HAIRMOD_PROTECTED.writer().writeByte((sbyte)itemID);
				break;
			}
			session.sendMessage(message_HAIRMOD_PROTECTED);
			message_HAIRMOD_PROTECTED.cleanup();
		}
		catch (Exception)
		{
		}
	}

	public void kickOutParty(int idUser, sbyte kickOrDel)
	{
		Message_HAIRMOD_PROTECTED message_HAIRMOD_PROTECTED = init(50);
		try
		{
			message_HAIRMOD_PROTECTED.writer().writeByte(kickOrDel);
			message_HAIRMOD_PROTECTED.writer().writeShort((short)idUser);
			session.sendMessage(message_HAIRMOD_PROTECTED);
			message_HAIRMOD_PROTECTED.cleanup();
		}
		catch (Exception)
		{
		}
	}

	public void joinParty(short idParty)
	{
		Message_HAIRMOD_PROTECTED message_HAIRMOD_PROTECTED = init(49);
		try
		{
			message_HAIRMOD_PROTECTED.writer().writeShort(idParty);
			session.sendMessage(message_HAIRMOD_PROTECTED);
			message_HAIRMOD_PROTECTED.cleanup();
		}
		catch (Exception)
		{
		}
	}

	public void outParty(int isParty, int isUser)
	{
	}

	public void moveChar(int x, int y)
	{
		if (!Tilemap_HAIRMOD_PROTECTED.isOfflineMap && !Tilemap_HAIRMOD_PROTECTED.tileTypeAtPixel(x, y, 2) && y / 16 * Tilemap_HAIRMOD_PROTECTED.w + x / 16 < Tilemap_HAIRMOD_PROTECTED.type.Length)
		{
			Message_HAIRMOD_PROTECTED message_HAIRMOD_PROTECTED = init(4);
			try
			{
				message_HAIRMOD_PROTECTED.writer().writeShort((short)x);
				message_HAIRMOD_PROTECTED.writer().writeShort((short)y);
			}
			catch (Exception)
			{
			}
			session.sendMessage(message_HAIRMOD_PROTECTED);
			message_HAIRMOD_PROTECTED.cleanup();
		}
	}

	public void requestCharInfo(short actorID)
	{
		Message_HAIRMOD_PROTECTED message_HAIRMOD_PROTECTED = init(5);
		try
		{
			message_HAIRMOD_PROTECTED.writer().writeShort(actorID);
		}
		catch (Exception)
		{
		}
		session.sendMessage(message_HAIRMOD_PROTECTED);
		message_HAIRMOD_PROTECTED.cleanup();
	}

	public void attackMonster(short id, sbyte skillType, sbyte effect, int dam, int crit, int haveBuffEff)
	{
		Message_HAIRMOD_PROTECTED message_HAIRMOD_PROTECTED = init(9);
		try
		{
			message_HAIRMOD_PROTECTED.writer().writeShort(id);
			message_HAIRMOD_PROTECTED.writer().writeByte(skillType);
		}
		catch (Exception)
		{
		}
		session.sendMessage(message_HAIRMOD_PROTECTED);
		message_HAIRMOD_PROTECTED.cleanup();
	}

	public void attackMultiMonster(short id, sbyte skillType, sbyte effect, int dam, int crit, int haveBuffEff, mVector_HAIRMOD_PROTECTED mst)
	{
		Message_HAIRMOD_PROTECTED message_HAIRMOD_PROTECTED = init(106);
		try
		{
			message_HAIRMOD_PROTECTED.writer().writeByte(skillType);
			message_HAIRMOD_PROTECTED.writer().writeByte((sbyte)mst.size());
			for (int i = 0; i < mst.size(); i++)
			{
				message_HAIRMOD_PROTECTED.writer().writeShort(((Actor_HAIRMOD_PROTECTED)mst.elementAt(i)).ID);
			}
		}
		catch (Exception)
		{
		}
		session.sendMessage(message_HAIRMOD_PROTECTED);
		message_HAIRMOD_PROTECTED.cleanup();
	}

	public void attackOtherPlayer(short id, sbyte skillType, sbyte effect, int dam, int crit, int haveBuffEff)
	{
		Message_HAIRMOD_PROTECTED message_HAIRMOD_PROTECTED = init(6);
		try
		{
			message_HAIRMOD_PROTECTED.writer().writeShort(id);
			message_HAIRMOD_PROTECTED.writer().writeByte(skillType);
		}
		catch (Exception)
		{
		}
		session.sendMessage(message_HAIRMOD_PROTECTED);
		message_HAIRMOD_PROTECTED.cleanup();
	}

	public void useBuff(short id, sbyte skillType, sbyte eff, short dam)
	{
		Message_HAIRMOD_PROTECTED message_HAIRMOD_PROTECTED = init(51);
		try
		{
			message_HAIRMOD_PROTECTED.writer().writeByte(1);
			message_HAIRMOD_PROTECTED.writer().writeShort(id);
			message_HAIRMOD_PROTECTED.writer().writeByte(skillType);
			message_HAIRMOD_PROTECTED.writer().writeByte(eff);
			message_HAIRMOD_PROTECTED.writer().writeShort(dam);
		}
		catch (Exception)
		{
		}
		session.sendMessage(message_HAIRMOD_PROTECTED);
		message_HAIRMOD_PROTECTED.cleanup();
	}

	public void endBuff(short idchar, sbyte buffType)
	{
		Message_HAIRMOD_PROTECTED message_HAIRMOD_PROTECTED = init(51);
		try
		{
			message_HAIRMOD_PROTECTED.writer().writeByte(0);
			message_HAIRMOD_PROTECTED.writer().writeShort(idchar);
			message_HAIRMOD_PROTECTED.writer().writeByte(buffType);
		}
		catch (Exception)
		{
		}
		session.sendMessage(message_HAIRMOD_PROTECTED);
		message_HAIRMOD_PROTECTED.cleanup();
	}

	public void requestMonsterInfo(short actorID)
	{
		Message_HAIRMOD_PROTECTED message_HAIRMOD_PROTECTED = init(7);
		try
		{
			message_HAIRMOD_PROTECTED.writer().writeShort(actorID);
		}
		catch (Exception)
		{
		}
		session.sendMessage(message_HAIRMOD_PROTECTED);
		message_HAIRMOD_PROTECTED.cleanup();
	}

	public void ping()
	{
		Message_HAIRMOD_PROTECTED message_HAIRMOD_PROTECTED = init(11);
		message_HAIRMOD_PROTECTED.cleanup();
	}

	public void setSession(ISession_HAIRMOD_PROTECTED gi)
	{
		session = gi;
	}

	public void selectChar(int id, int type)
	{
		try
		{
			Message_HAIRMOD_PROTECTED message_HAIRMOD_PROTECTED = init(13);
			message_HAIRMOD_PROTECTED.writer().writeByte((sbyte)type);
			message_HAIRMOD_PROTECTED.writer().writeInt(id);
			session.sendMessage(message_HAIRMOD_PROTECTED);
			message_HAIRMOD_PROTECTED.cleanup();
		}
		catch (Exception)
		{
		}
	}

	public void createChar(string charName, int clazz, int headStyle, int gender, int idnation)
	{
		Message_HAIRMOD_PROTECTED message_HAIRMOD_PROTECTED = init(14);
		try
		{
			message_HAIRMOD_PROTECTED.writer().writeUTF(charName);
			message_HAIRMOD_PROTECTED.writer().writeByte((sbyte)clazz);
			message_HAIRMOD_PROTECTED.writer().writeByte((sbyte)headStyle);
			message_HAIRMOD_PROTECTED.writer().writeByte((sbyte)gender);
			message_HAIRMOD_PROTECTED.writer().writeByte((sbyte)idnation);
			message_HAIRMOD_PROTECTED.writer().writeByte(ServerListScr_HAIRMOD_PROTECTED.index_server[ServerListScr_HAIRMOD_PROTECTED.index]);
			session.sendMessage(message_HAIRMOD_PROTECTED);
			message_HAIRMOD_PROTECTED.cleanup();
		}
		catch (Exception)
		{
		}
	}

	public void setClientType()
	{
		Message_HAIRMOD_PROTECTED message_HAIRMOD_PROTECTED = init(-1);
		try
		{
			message_HAIRMOD_PROTECTED.writer().writeByte(3);
		}
		catch (Exception)
		{
		}
		session.sendMessage(message_HAIRMOD_PROTECTED);
		message_HAIRMOD_PROTECTED.cleanup();
	}

	public void getDropableFromGround(sbyte catagory, short iD)
	{
		try
		{
			switch (catagory)
			{
			case 3:
			{
				Message_HAIRMOD_PROTECTED message_HAIRMOD_PROTECTED4 = init(18);
				message_HAIRMOD_PROTECTED4.writer().writeShort(iD);
				session.sendMessage(message_HAIRMOD_PROTECTED4);
				message_HAIRMOD_PROTECTED4.cleanup();
				break;
			}
			case 4:
			{
				Message_HAIRMOD_PROTECTED message_HAIRMOD_PROTECTED3 = init(19);
				message_HAIRMOD_PROTECTED3.writer().writeShort(iD);
				session.sendMessage(message_HAIRMOD_PROTECTED3);
				message_HAIRMOD_PROTECTED3.cleanup();
				break;
			}
			default:
				if (catagory != 6)
				{
					if (catagory == 14)
					{
						Message_HAIRMOD_PROTECTED message_HAIRMOD_PROTECTED = init(-65);
						message_HAIRMOD_PROTECTED.writer().writeShort(iD);
						session.sendMessage(message_HAIRMOD_PROTECTED);
					}
					break;
				}
				goto case 7;
			case 7:
			{
				Message_HAIRMOD_PROTECTED message_HAIRMOD_PROTECTED2 = init(-41);
				message_HAIRMOD_PROTECTED2.writer().writeByte(catagory);
				message_HAIRMOD_PROTECTED2.writer().writeShort(iD);
				session.sendMessage(message_HAIRMOD_PROTECTED2);
				message_HAIRMOD_PROTECTED2.cleanup();
				break;
			}
			}
		}
		catch (IOException)
		{
		}
	}

	public void requestMainCharInfo()
	{
		Message_HAIRMOD_PROTECTED message_HAIRMOD_PROTECTED = init(88);
		try
		{
			session.sendMessage(message_HAIRMOD_PROTECTED);
		}
		catch (Exception)
		{
		}
		message_HAIRMOD_PROTECTED.cleanup();
	}

	public void sendRequestMainInfo(int me, int id)
	{
		Message_HAIRMOD_PROTECTED message_HAIRMOD_PROTECTED = init(-2);
		try
		{
			message_HAIRMOD_PROTECTED.writer().writeByte((sbyte)me);
			message_HAIRMOD_PROTECTED.writer().writeShort((short)id);
			session.sendMessage(message_HAIRMOD_PROTECTED);
		}
		catch (Exception)
		{
		}
		message_HAIRMOD_PROTECTED.cleanup();
	}

	public void requestWearingInfo(int me, int id)
	{
		Message_HAIRMOD_PROTECTED message_HAIRMOD_PROTECTED = init(-3);
		try
		{
			message_HAIRMOD_PROTECTED.writer().writeByte((sbyte)me);
			message_HAIRMOD_PROTECTED.writer().writeShort((short)id);
			session.sendMessage(message_HAIRMOD_PROTECTED);
		}
		catch (Exception)
		{
		}
		message_HAIRMOD_PROTECTED.cleanup();
	}

	public void buyTicket()
	{
		Message_HAIRMOD_PROTECTED message_HAIRMOD_PROTECTED = init(81);
		try
		{
			session.sendMessage(message_HAIRMOD_PROTECTED);
			message_HAIRMOD_PROTECTED.cleanup();
		}
		catch (Exception)
		{
		}
	}

	public void requestItemInfo(short itemID, short id)
	{
		Message_HAIRMOD_PROTECTED message_HAIRMOD_PROTECTED = init(21);
		try
		{
			message_HAIRMOD_PROTECTED.writer().writeShort(itemID);
			message_HAIRMOD_PROTECTED.writer().writeShort(id);
			session.sendMessage(message_HAIRMOD_PROTECTED);
			message_HAIRMOD_PROTECTED.cleanup();
		}
		catch (IOException)
		{
		}
	}

	public void dobuyitemShopnew(sbyte id, short typeshop)
	{
		Message_HAIRMOD_PROTECTED message_HAIRMOD_PROTECTED = init(-76);
		try
		{
			message_HAIRMOD_PROTECTED.writer().writeShort(typeshop);
			message_HAIRMOD_PROTECTED.writer().writeByte(id);
			session.sendMessage(message_HAIRMOD_PROTECTED);
			message_HAIRMOD_PROTECTED.cleanup();
		}
		catch (IOException)
		{
		}
	}

	public void usePotion(int potionType)
	{
		Message_HAIRMOD_PROTECTED message_HAIRMOD_PROTECTED = init(22);
		try
		{
			message_HAIRMOD_PROTECTED.writer().writeByte((sbyte)potionType);
			session.sendMessage(message_HAIRMOD_PROTECTED);
			message_HAIRMOD_PROTECTED.cleanup();
		}
		catch (IOException)
		{
		}
	}

	public void changeMap(int toM, int toX, int toY)
	{
		ChangScr_HAIRMOD_PROTECTED.gI().setMap(toM, toX, toY);
		Canvas_HAIRMOD_PROTECTED.gameScr.posBoss = null;
		Message_HAIRMOD_PROTECTED message_HAIRMOD_PROTECTED = init(12);
		try
		{
			message_HAIRMOD_PROTECTED.writer().writeShort((short)toM);
			message_HAIRMOD_PROTECTED.writer().writeShort((short)toX);
			message_HAIRMOD_PROTECTED.writer().writeShort((short)toY);
			session.sendMessage(message_HAIRMOD_PROTECTED);
			message_HAIRMOD_PROTECTED.cleanup();
		}
		catch (IOException)
		{
		}
	}

	public void changeMapOK(int toM, int toX, int toY)
	{
		Message_HAIRMOD_PROTECTED message_HAIRMOD_PROTECTED = init(12);
		try
		{
			message_HAIRMOD_PROTECTED.writer().writeShort((short)toM);
			message_HAIRMOD_PROTECTED.writer().writeShort((short)toX);
			message_HAIRMOD_PROTECTED.writer().writeShort((short)toY);
			session.sendMessage(message_HAIRMOD_PROTECTED);
			message_HAIRMOD_PROTECTED.cleanup();
		}
		catch (IOException)
		{
		}
	}

	public void requestNPCInfo(int type)
	{
		Message_HAIRMOD_PROTECTED message_HAIRMOD_PROTECTED = init(23);
		try
		{
			message_HAIRMOD_PROTECTED.writer().writeByte((sbyte)type);
			session.sendMessage(message_HAIRMOD_PROTECTED);
			message_HAIRMOD_PROTECTED.cleanup();
		}
		catch (IOException)
		{
		}
	}

	public void requestNPCInfo(int type, int idType)
	{
		Message_HAIRMOD_PROTECTED message_HAIRMOD_PROTECTED = init(23);
		try
		{
			message_HAIRMOD_PROTECTED.writer().writeByte((sbyte)type);
			message_HAIRMOD_PROTECTED.writer().writeByte((sbyte)idType);
			session.sendMessage(message_HAIRMOD_PROTECTED);
			message_HAIRMOD_PROTECTED.cleanup();
		}
		catch (IOException)
		{
		}
	}

	public void buyGemItem(mVector_HAIRMOD_PROTECTED item)
	{
		Message_HAIRMOD_PROTECTED message_HAIRMOD_PROTECTED = init(74);
		try
		{
			int num = 0;
			for (int i = 0; i < item.size(); i++)
			{
				GemTemp_HAIRMOD_PROTECTED gemTemp_HAIRMOD_PROTECTED = (GemTemp_HAIRMOD_PROTECTED)item.elementAt(i);
				num += gemTemp_HAIRMOD_PROTECTED.number;
			}
			message_HAIRMOD_PROTECTED.writer().writeShort((short)num);
			for (int j = 0; j < item.size(); j++)
			{
				GemTemp_HAIRMOD_PROTECTED gemTemp_HAIRMOD_PROTECTED2 = (GemTemp_HAIRMOD_PROTECTED)item.elementAt(j);
				for (int k = 0; k < gemTemp_HAIRMOD_PROTECTED2.number; k++)
				{
					message_HAIRMOD_PROTECTED.writer().writeShort(gemTemp_HAIRMOD_PROTECTED2.id);
				}
			}
			session.sendMessage(message_HAIRMOD_PROTECTED);
			message_HAIRMOD_PROTECTED.cleanup();
		}
		catch (Exception)
		{
		}
	}

	public void buyItem(mVector_HAIRMOD_PROTECTED items)
	{
		Message_HAIRMOD_PROTECTED message_HAIRMOD_PROTECTED = init(24);
		try
		{
			message_HAIRMOD_PROTECTED.writer().writeByte((sbyte)items.size());
			for (int i = 0; i < items.size(); i++)
			{
				ItemInInventory_HAIRMOD_PROTECTED itemInInventory_HAIRMOD_PROTECTED = (ItemInInventory_HAIRMOD_PROTECTED)items.elementAt(i);
				ItemTemplate_HAIRMOD_PROTECTED item = Res_HAIRMOD_PROTECTED.getItem(itemInInventory_HAIRMOD_PROTECTED.charClazz, itemInInventory_HAIRMOD_PROTECTED.idTemplate);
				message_HAIRMOD_PROTECTED.writer().writeByte(itemInInventory_HAIRMOD_PROTECTED.catagory);
				message_HAIRMOD_PROTECTED.writer().writeShort(item.id);
				message_HAIRMOD_PROTECTED.writer().writeShort(1);
				if (itemInInventory_HAIRMOD_PROTECTED.catagory == 3)
				{
					if (WindowInfoScr_HAIRMOD_PROTECTED.gI().idClass >= 0)
					{
						message_HAIRMOD_PROTECTED.writer().writeByte((sbyte)WindowInfoScr_HAIRMOD_PROTECTED.gI().idClass);
					}
					else
					{
						message_HAIRMOD_PROTECTED.writer().writeByte(item.clazz);
					}
				}
			}
			session.sendMessage(message_HAIRMOD_PROTECTED);
			message_HAIRMOD_PROTECTED.cleanup();
		}
		catch (IOException)
		{
		}
	}

	public void buyPotion(mVector_HAIRMOD_PROTECTED items)
	{
		Message_HAIRMOD_PROTECTED message_HAIRMOD_PROTECTED = init(24);
		try
		{
			message_HAIRMOD_PROTECTED.writer().writeByte((sbyte)items.size());
			for (int i = 0; i < items.size(); i++)
			{
				ItemInInventory_HAIRMOD_PROTECTED itemInInventory_HAIRMOD_PROTECTED = (ItemInInventory_HAIRMOD_PROTECTED)items.elementAt(i);
				message_HAIRMOD_PROTECTED.writer().writeByte(4);
				message_HAIRMOD_PROTECTED.writer().writeShort(itemInInventory_HAIRMOD_PROTECTED.potionType);
				message_HAIRMOD_PROTECTED.writer().writeShort(itemInInventory_HAIRMOD_PROTECTED.number);
			}
			session.sendMessage(message_HAIRMOD_PROTECTED);
			message_HAIRMOD_PROTECTED.cleanup();
		}
		catch (IOException)
		{
		}
	}

	public void chat(string text)
	{
		Message_HAIRMOD_PROTECTED message_HAIRMOD_PROTECTED = init(27);
		try
		{
			message_HAIRMOD_PROTECTED.writer().writeUTF(text);
			session.sendMessage(message_HAIRMOD_PROTECTED);
			message_HAIRMOD_PROTECTED.cleanup();
		}
		catch (IOException)
		{
		}
	}

	public void finishPutItem2Bag()
	{
		Message_HAIRMOD_PROTECTED message_HAIRMOD_PROTECTED = init(70);
		try
		{
			session.sendMessage(message_HAIRMOD_PROTECTED);
			message_HAIRMOD_PROTECTED.cleanup();
		}
		catch (Exception)
		{
		}
	}

	public void putItem2Bag(short itemID)
	{
		Message_HAIRMOD_PROTECTED message_HAIRMOD_PROTECTED = init(68);
		try
		{
			message_HAIRMOD_PROTECTED.writer().writeShort(itemID);
			session.sendMessage(message_HAIRMOD_PROTECTED);
			message_HAIRMOD_PROTECTED.cleanup();
		}
		catch (IOException)
		{
		}
	}

	public void getItemFromBag(short itemID)
	{
		Message_HAIRMOD_PROTECTED message_HAIRMOD_PROTECTED = init(69);
		try
		{
			message_HAIRMOD_PROTECTED.writer().writeShort(itemID);
			session.sendMessage(message_HAIRMOD_PROTECTED);
			message_HAIRMOD_PROTECTED.cleanup();
		}
		catch (IOException)
		{
		}
	}

	public void sellItem(short itemID)
	{
		Message_HAIRMOD_PROTECTED message_HAIRMOD_PROTECTED = init(28);
		try
		{
			message_HAIRMOD_PROTECTED.writer().writeShort(itemID);
			session.sendMessage(message_HAIRMOD_PROTECTED);
			message_HAIRMOD_PROTECTED.cleanup();
		}
		catch (IOException)
		{
		}
	}

	public void sellGemItem(short realID, sbyte khoa)
	{
		Message_HAIRMOD_PROTECTED message_HAIRMOD_PROTECTED = init(78);
		try
		{
			message_HAIRMOD_PROTECTED.writer().writeShort(realID);
			message_HAIRMOD_PROTECTED.writer().writeByte(khoa);
			session.sendMessage(message_HAIRMOD_PROTECTED);
			message_HAIRMOD_PROTECTED.cleanup();
		}
		catch (IOException)
		{
		}
	}

	public void useItem(short itemID)
	{
		Message_HAIRMOD_PROTECTED message_HAIRMOD_PROTECTED = init(29);
		try
		{
			message_HAIRMOD_PROTECTED.writer().writeShort(itemID);
			session.sendMessage(message_HAIRMOD_PROTECTED);
			message_HAIRMOD_PROTECTED.cleanup();
		}
		catch (IOException)
		{
		}
	}

	public void giveItemToGround(short itemID)
	{
		Message_HAIRMOD_PROTECTED message_HAIRMOD_PROTECTED = init(61);
		try
		{
			message_HAIRMOD_PROTECTED.writer().writeShort(itemID);
			session.sendMessage(message_HAIRMOD_PROTECTED);
			message_HAIRMOD_PROTECTED.cleanup();
		}
		catch (IOException)
		{
		}
	}

	public void comeHomeWhenDie()
	{
		Message_HAIRMOD_PROTECTED message_HAIRMOD_PROTECTED = init(31);
		session.sendMessage(message_HAIRMOD_PROTECTED);
		message_HAIRMOD_PROTECTED.cleanup();
	}

	public void addBasePoint(int toWhatPoint, int point)
	{
		Message_HAIRMOD_PROTECTED message_HAIRMOD_PROTECTED = init(34);
		try
		{
			message_HAIRMOD_PROTECTED.writer().writeByte((sbyte)toWhatPoint);
			message_HAIRMOD_PROTECTED.writer().writeShort((short)point);
			session.sendMessage(message_HAIRMOD_PROTECTED);
			message_HAIRMOD_PROTECTED.cleanup();
		}
		catch (IOException)
		{
		}
	}

	public void addSkillPoint(int skillType)
	{
		Message_HAIRMOD_PROTECTED message_HAIRMOD_PROTECTED = init(36);
		try
		{
			message_HAIRMOD_PROTECTED.writer().writeByte((sbyte)skillType);
			session.sendMessage(message_HAIRMOD_PROTECTED);
			message_HAIRMOD_PROTECTED.cleanup();
		}
		catch (IOException)
		{
		}
	}

	public void requestRegister(string username, string pass)
	{
		Message_HAIRMOD_PROTECTED message_HAIRMOD_PROTECTED = init(39);
		try
		{
			message_HAIRMOD_PROTECTED.writer().writeUTF(username);
			message_HAIRMOD_PROTECTED.writer().writeUTF(pass);
			GameMidlet_HAIRMOD_PROTECTED.client_Provider.Trim();
			message_HAIRMOD_PROTECTED.writer().writeUTF(GameMidlet_HAIRMOD_PROTECTED.client_Provider);
			message_HAIRMOD_PROTECTED.writer().writeUTF(GameMidlet_HAIRMOD_PROTECTED.provider);
			message_HAIRMOD_PROTECTED.writer().writeUTF(GameMidlet_HAIRMOD_PROTECTED.agent);
			message_HAIRMOD_PROTECTED.writer().writeUTF(Canvas_HAIRMOD_PROTECTED.IMEI);
			message_HAIRMOD_PROTECTED.writer().writeInt(GameMidlet_HAIRMOD_PROTECTED.verSionCode);
		}
		catch (IOException)
		{
		}
		session.sendMessage(message_HAIRMOD_PROTECTED);
		message_HAIRMOD_PROTECTED.cleanup();
	}

	public void receiveQuest(int idNPC, int idQuest, int type)
	{
		Message_HAIRMOD_PROTECTED message_HAIRMOD_PROTECTED = init(52);
		try
		{
			message_HAIRMOD_PROTECTED.writer().writeByte((sbyte)type);
			message_HAIRMOD_PROTECTED.writer().writeByte((sbyte)idNPC);
			message_HAIRMOD_PROTECTED.writer().writeShort((short)idQuest);
		}
		catch (Exception)
		{
		}
		session.sendMessage(message_HAIRMOD_PROTECTED);
		message_HAIRMOD_PROTECTED.cleanup();
	}

	public void talk_npc_response_quest(int idNpc, int type, int idQuest)
	{
		Message_HAIRMOD_PROTECTED message_HAIRMOD_PROTECTED = init(55);
		try
		{
			message_HAIRMOD_PROTECTED.writer().writeByte((sbyte)idNpc);
			message_HAIRMOD_PROTECTED.writer().writeByte((sbyte)type);
			message_HAIRMOD_PROTECTED.writer().writeShort((short)idQuest);
			session.sendMessage(message_HAIRMOD_PROTECTED);
		}
		catch (Exception)
		{
		}
		finally
		{
			try
			{
				message_HAIRMOD_PROTECTED.cleanup();
			}
			catch (Exception)
			{
			}
		}
	}

	public void talk_npc(int idNpc, int type, int idQuest)
	{
		Message_HAIRMOD_PROTECTED message_HAIRMOD_PROTECTED = init(53);
		try
		{
			message_HAIRMOD_PROTECTED.writer().writeByte((sbyte)idNpc);
			message_HAIRMOD_PROTECTED.writer().writeByte((sbyte)type);
			message_HAIRMOD_PROTECTED.writer().writeShort((short)idQuest);
			session.sendMessage(message_HAIRMOD_PROTECTED);
		}
		catch (Exception)
		{
		}
		finally
		{
			try
			{
				message_HAIRMOD_PROTECTED.cleanup();
			}
			catch (Exception)
			{
			}
		}
	}

	public void sendRequestAddClan(short id, sbyte type, bool iss)
	{
		Message_HAIRMOD_PROTECTED message_HAIRMOD_PROTECTED = init(-11);
		try
		{
			message_HAIRMOD_PROTECTED.writer().writeByte(type);
			message_HAIRMOD_PROTECTED.writer().writeShort(id);
			if (type == 1)
			{
				message_HAIRMOD_PROTECTED.writer().writeBoolean(iss);
			}
		}
		catch (IOException)
		{
		}
		session.sendMessage(message_HAIRMOD_PROTECTED);
	}

	public void sendMsgPrivate(string name, string text)
	{
		Message_HAIRMOD_PROTECTED message_HAIRMOD_PROTECTED = init(-5);
		try
		{
			message_HAIRMOD_PROTECTED.writer().writeUTF(name);
			message_HAIRMOD_PROTECTED.writer().writeUTF(text);
		}
		catch (IOException)
		{
		}
		session.sendMessage(message_HAIRMOD_PROTECTED);
	}

	public void removeFriend(string name)
	{
		Message_HAIRMOD_PROTECTED message_HAIRMOD_PROTECTED = init(-6);
		try
		{
			message_HAIRMOD_PROTECTED.writer().writeUTF(name);
		}
		catch (IOException)
		{
		}
		session.sendMessage(message_HAIRMOD_PROTECTED);
	}

	public void requestClanList(int idClan, sbyte page)
	{
		Message_HAIRMOD_PROTECTED message_HAIRMOD_PROTECTED = init(-7);
		try
		{
			message_HAIRMOD_PROTECTED.writer().writeByte(page);
			message_HAIRMOD_PROTECTED.writer().writeShort((short)idClan);
		}
		catch (IOException)
		{
		}
		session.sendMessage(message_HAIRMOD_PROTECTED);
	}

	public void doGetImage(sbyte type, int ver)
	{
		Message_HAIRMOD_PROTECTED message_HAIRMOD_PROTECTED = init(-8);
		try
		{
			message_HAIRMOD_PROTECTED.writer().writeByte(type);
			message_HAIRMOD_PROTECTED.writer().writeByte((sbyte)ver);
		}
		catch (IOException)
		{
		}
		session.sendMessage(message_HAIRMOD_PROTECTED);
	}

	public void registerClan()
	{
		Message_HAIRMOD_PROTECTED message = init(-9);
		session.sendMessage(message);
	}

	public void chooseIconClan(short index, string name)
	{
		Message_HAIRMOD_PROTECTED message_HAIRMOD_PROTECTED = init(-10);
		try
		{
			message_HAIRMOD_PROTECTED.writer().writeShort(index);
			message_HAIRMOD_PROTECTED.writer().writeUTF(name);
		}
		catch (IOException)
		{
		}
		session.sendMessage(message_HAIRMOD_PROTECTED);
	}

	public void requestClanInfo(int idClan)
	{
		Canvas_HAIRMOD_PROTECTED.startWaitDlg();
		Message_HAIRMOD_PROTECTED message_HAIRMOD_PROTECTED = init(-12);
		try
		{
			message_HAIRMOD_PROTECTED.writer().writeShort((short)idClan);
		}
		catch (IOException)
		{
		}
		session.sendMessage(message_HAIRMOD_PROTECTED);
	}

	public void requestEvictionClan(string name)
	{
		Message_HAIRMOD_PROTECTED message_HAIRMOD_PROTECTED = init(-13);
		try
		{
			message_HAIRMOD_PROTECTED.writer().writeUTF(name);
		}
		catch (IOException)
		{
		}
		session.sendMessage(message_HAIRMOD_PROTECTED);
	}

	public void dissolveCLan()
	{
		Message_HAIRMOD_PROTECTED message = init(-14);
		session.sendMessage(message);
	}

	public void outClan()
	{
		Message_HAIRMOD_PROTECTED message = init(-15);
		session.sendMessage(message);
	}

	public void setSologan(string str)
	{
		Message_HAIRMOD_PROTECTED message_HAIRMOD_PROTECTED = init(-16);
		try
		{
			message_HAIRMOD_PROTECTED.writer().writeUTF(str);
		}
		catch (IOException)
		{
		}
		session.sendMessage(message_HAIRMOD_PROTECTED);
	}

	public void msgClanAll(string str, sbyte type, int idUser)
	{
		Message_HAIRMOD_PROTECTED message_HAIRMOD_PROTECTED = init(-17);
		try
		{
			message_HAIRMOD_PROTECTED.writer().writeByte(type);
			switch (type)
			{
			case 0:
				message_HAIRMOD_PROTECTED.writer().writeUTF(str);
				break;
			case 2:
				message_HAIRMOD_PROTECTED.writer().writeInt(idUser);
				break;
			}
		}
		catch (IOException)
		{
		}
		session.sendMessage(message_HAIRMOD_PROTECTED);
	}

	public void chatToClan(string str)
	{
		Message_HAIRMOD_PROTECTED message_HAIRMOD_PROTECTED = init(-18);
		try
		{
			message_HAIRMOD_PROTECTED.writer().writeUTF(str);
		}
		catch (IOException)
		{
		}
		session.sendMessage(message_HAIRMOD_PROTECTED);
	}

	public void requestTopStronger_Righer(int type, int page)
	{
		Canvas_HAIRMOD_PROTECTED.startWaitDlg();
		Message_HAIRMOD_PROTECTED message_HAIRMOD_PROTECTED = init(-19);
		try
		{
			message_HAIRMOD_PROTECTED.writer().writeByte((sbyte)type);
			message_HAIRMOD_PROTECTED.writer().writeByte((sbyte)page);
		}
		catch (IOException)
		{
		}
		session.sendMessage(message_HAIRMOD_PROTECTED);
	}

	public void transMoney(int parseInt)
	{
		Message_HAIRMOD_PROTECTED message_HAIRMOD_PROTECTED = init(-20);
		try
		{
			message_HAIRMOD_PROTECTED.writer().writeInt(parseInt);
		}
		catch (IOException)
		{
		}
		session.sendMessage(message_HAIRMOD_PROTECTED);
	}

	public void dellPotion(int potionType)
	{
		Message_HAIRMOD_PROTECTED message_HAIRMOD_PROTECTED = init(-21);
		try
		{
			message_HAIRMOD_PROTECTED.writer().writeShort((short)potionType);
		}
		catch (IOException)
		{
		}
		session.sendMessage(message_HAIRMOD_PROTECTED);
	}

	public void requestSomeOneInfo(short id, sbyte type)
	{
		Message_HAIRMOD_PROTECTED message_HAIRMOD_PROTECTED = init(-22);
		try
		{
			message_HAIRMOD_PROTECTED.writer().writeShort(id);
			message_HAIRMOD_PROTECTED.writer().writeByte(type);
		}
		catch (IOException)
		{
		}
		session.sendMessage(message_HAIRMOD_PROTECTED);
	}

	public void dellGemItem(short id, int type, sbyte khoa)
	{
		Message_HAIRMOD_PROTECTED message_HAIRMOD_PROTECTED = init(-23);
		try
		{
			message_HAIRMOD_PROTECTED.writer().writeByte((sbyte)type);
			message_HAIRMOD_PROTECTED.writer().writeShort(id);
			if (type == 0)
			{
				message_HAIRMOD_PROTECTED.writer().writeByte(khoa);
			}
		}
		catch (IOException)
		{
		}
		session.sendMessage(message_HAIRMOD_PROTECTED);
	}

	public void requestString(int type)
	{
		Message_HAIRMOD_PROTECTED message_HAIRMOD_PROTECTED = init(-24);
		try
		{
			message_HAIRMOD_PROTECTED.writer().writeByte((sbyte)type);
		}
		catch (Exception)
		{
		}
		session.sendMessage(message_HAIRMOD_PROTECTED);
	}

	public void doRequestWeapone(int type, int weaponType, int weaponStyle, sbyte weaponIndex)
	{
		Message_HAIRMOD_PROTECTED message_HAIRMOD_PROTECTED = init(-27);
		try
		{
			message_HAIRMOD_PROTECTED.writer().writeByte((sbyte)type);
			message_HAIRMOD_PROTECTED.writer().writeByte((sbyte)weaponType);
			message_HAIRMOD_PROTECTED.writer().writeByte((sbyte)weaponStyle);
			message_HAIRMOD_PROTECTED.writer().writeByte(weaponIndex);
		}
		catch (Exception)
		{
		}
		session.sendMessage(message_HAIRMOD_PROTECTED);
	}

	public void doCapCha(string text)
	{
		Message_HAIRMOD_PROTECTED message_HAIRMOD_PROTECTED = init(-28);
		try
		{
			message_HAIRMOD_PROTECTED.writer().writeUTF(text);
		}
		catch (IOException)
		{
			Debug.Log("cap cha");
		}
		session.sendMessage(message_HAIRMOD_PROTECTED);
	}

	public void doMenuOption(int userID, sbyte idMenu, int i)
	{
		Message_HAIRMOD_PROTECTED message_HAIRMOD_PROTECTED = init(-30);
		try
		{
			message_HAIRMOD_PROTECTED.writer().writeShort((short)userID);
			message_HAIRMOD_PROTECTED.writer().writeByte(idMenu);
			message_HAIRMOD_PROTECTED.writer().writeByte((sbyte)i);
		}
		catch (IOException)
		{
		}
		session.sendMessage(message_HAIRMOD_PROTECTED);
	}

	public void doTextBox(int userID, sbyte idMenu, string text)
	{
		Message_HAIRMOD_PROTECTED message_HAIRMOD_PROTECTED = init(-31);
		try
		{
			message_HAIRMOD_PROTECTED.writer().writeShort((short)userID);
			message_HAIRMOD_PROTECTED.writer().writeByte(idMenu);
			message_HAIRMOD_PROTECTED.writer().writeUTF(text);
		}
		catch (IOException)
		{
		}
		session.sendMessage(message_HAIRMOD_PROTECTED);
	}

	public void doCustomPopup(int idB, sbyte idPopup, string text5, int ok)
	{
		Message_HAIRMOD_PROTECTED message_HAIRMOD_PROTECTED = init(-32);
		try
		{
			message_HAIRMOD_PROTECTED.writer().writeShort((short)idB);
			message_HAIRMOD_PROTECTED.writer().writeByte(idPopup);
			message_HAIRMOD_PROTECTED.writer().writeUTF(text5);
			message_HAIRMOD_PROTECTED.writer().writeByte((sbyte)ok);
		}
		catch (Exception)
		{
		}
		session.sendMessage(message_HAIRMOD_PROTECTED);
	}

	public void doWayPoint(int x, int y, short type)
	{
		Message_HAIRMOD_PROTECTED message_HAIRMOD_PROTECTED = init(-33);
		try
		{
			message_HAIRMOD_PROTECTED.writer().writeByte((sbyte)(x / 16));
			message_HAIRMOD_PROTECTED.writer().writeByte((sbyte)(y / 16));
			message_HAIRMOD_PROTECTED.writer().writeShort(type);
		}
		catch (Exception)
		{
		}
		session.sendMessage(message_HAIRMOD_PROTECTED);
	}

	public void requestKham(int type)
	{
		Message_HAIRMOD_PROTECTED message_HAIRMOD_PROTECTED = init(-34);
		try
		{
			message_HAIRMOD_PROTECTED.writer().writeByte((sbyte)type);
		}
		catch (Exception)
		{
		}
		session.sendMessage(message_HAIRMOD_PROTECTED);
	}

	public void doKhamItem(int i)
	{
		Message_HAIRMOD_PROTECTED message_HAIRMOD_PROTECTED = init(-35);
		try
		{
			message_HAIRMOD_PROTECTED.writer().writeByte((sbyte)i);
		}
		catch (Exception)
		{
		}
		session.sendMessage(message_HAIRMOD_PROTECTED);
	}

	public void epNgoc(int type, mVector_HAIRMOD_PROTECTED listTemp)
	{
		Message_HAIRMOD_PROTECTED message_HAIRMOD_PROTECTED = init(-36);
		try
		{
			message_HAIRMOD_PROTECTED.writer().writeByte((sbyte)type);
			if (type == 1)
			{
				message_HAIRMOD_PROTECTED.writer().writeByte((sbyte)listTemp.size());
				for (int i = 0; i < listTemp.size(); i++)
				{
					RealID_HAIRMOD_PROTECTED realID_HAIRMOD_PROTECTED = (RealID_HAIRMOD_PROTECTED)listTemp.elementAt(i);
					message_HAIRMOD_PROTECTED.writer().writeShort(realID_HAIRMOD_PROTECTED.realID);
				}
			}
		}
		catch (IOException)
		{
		}
		session.sendMessage(message_HAIRMOD_PROTECTED);
	}

	public void epDothu(mVector_HAIRMOD_PROTECTED listTemp, sbyte[] listNL)
	{
		Message_HAIRMOD_PROTECTED message_HAIRMOD_PROTECTED = init(-61);
		try
		{
			message_HAIRMOD_PROTECTED.writer().writeByte((sbyte)listTemp.size());
			for (int i = 0; i < listTemp.size(); i++)
			{
				ItemInInventory_HAIRMOD_PROTECTED itemInInventory_HAIRMOD_PROTECTED = (ItemInInventory_HAIRMOD_PROTECTED)listTemp.elementAt(i);
				message_HAIRMOD_PROTECTED.writer().writeShort(itemInInventory_HAIRMOD_PROTECTED.itemID);
			}
			for (int j = 0; j < listNL.Length; j++)
			{
				message_HAIRMOD_PROTECTED.writer().writeByte(listNL[j]);
			}
		}
		catch (IOException)
		{
		}
		session.sendMessage(message_HAIRMOD_PROTECTED);
	}

	public void questClan(int type)
	{
		Message_HAIRMOD_PROTECTED message_HAIRMOD_PROTECTED = init(-37);
		try
		{
			message_HAIRMOD_PROTECTED.writer().writeByte((sbyte)type);
		}
		catch (Exception)
		{
		}
		session.sendMessage(message_HAIRMOD_PROTECTED);
	}

	public void doRideAnimal(short id, sbyte typeAnimal)
	{
		Message_HAIRMOD_PROTECTED message_HAIRMOD_PROTECTED = init(-45);
		try
		{
			message_HAIRMOD_PROTECTED.writer().writeByte(typeAnimal);
			message_HAIRMOD_PROTECTED.writer().writeShort(id);
		}
		catch (Exception)
		{
		}
		session.sendMessage(message_HAIRMOD_PROTECTED);
	}

	public void doRequestSoundData(byte id)
	{
		Message_HAIRMOD_PROTECTED message_HAIRMOD_PROTECTED = new Message_HAIRMOD_PROTECTED((sbyte)(-46));
		try
		{
			message_HAIRMOD_PROTECTED.writer().writeByte((sbyte)id);
		}
		catch (Exception)
		{
		}
		session.sendMessage(message_HAIRMOD_PROTECTED);
	}

	public void doRequestImageMonster(int idTemplate, sbyte palate, sbyte sPalate)
	{
		Message_HAIRMOD_PROTECTED message_HAIRMOD_PROTECTED = new Message_HAIRMOD_PROTECTED((sbyte)(-47));
		try
		{
			message_HAIRMOD_PROTECTED.writer().writeByte(palate);
			message_HAIRMOD_PROTECTED.writer().writeByte(sPalate);
			message_HAIRMOD_PROTECTED.writer().writeShort((short)idTemplate);
		}
		catch (Exception)
		{
		}
		session.sendMessage(message_HAIRMOD_PROTECTED);
	}

	public void getTreeImage(int i, int typeClothes, int id)
	{
		Message_HAIRMOD_PROTECTED message_HAIRMOD_PROTECTED = new Message_HAIRMOD_PROTECTED((sbyte)(-48));
		try
		{
			if (i == -1)
			{
				message_HAIRMOD_PROTECTED.writer().writeByte((sbyte)i);
				message_HAIRMOD_PROTECTED.writer().writeByte((sbyte)typeClothes);
				message_HAIRMOD_PROTECTED.writer().writeByte((sbyte)id);
			}
			else
			{
				message_HAIRMOD_PROTECTED.writer().writeByte((sbyte)i);
			}
		}
		catch (Exception)
		{
		}
		session.sendMessage(message_HAIRMOD_PROTECTED);
	}

	public void doRequestEffectData(short id3)
	{
		Message_HAIRMOD_PROTECTED message_HAIRMOD_PROTECTED = new Message_HAIRMOD_PROTECTED((sbyte)(-49));
		try
		{
			message_HAIRMOD_PROTECTED.writer().writeByte((sbyte)id3);
		}
		catch (Exception)
		{
		}
		session.sendMessage(message_HAIRMOD_PROTECTED);
	}

	public void doCheDo(short size, short idItem, sbyte[][] arrNumber)
	{
		Message_HAIRMOD_PROTECTED message_HAIRMOD_PROTECTED = new Message_HAIRMOD_PROTECTED((sbyte)(-52));
		try
		{
			message_HAIRMOD_PROTECTED.writer().writeByte(WindowInfoScr_HAIRMOD_PROTECTED.optionCreateItem);
			message_HAIRMOD_PROTECTED.writer().writeShort(idItem);
			for (int i = 0; i < size; i++)
			{
				for (int j = 0; j < arrNumber[i].Length; j++)
				{
					message_HAIRMOD_PROTECTED.writer().writeByte(arrNumber[i][j]);
				}
			}
		}
		catch (Exception)
		{
		}
		session.sendMessage(message_HAIRMOD_PROTECTED);
	}

	public void doRequestTachNguyenLIeu(short idItem, int type, int idMaterial, int ilock)
	{
		Message_HAIRMOD_PROTECTED message_HAIRMOD_PROTECTED = new Message_HAIRMOD_PROTECTED((sbyte)(-68));
		try
		{
			message_HAIRMOD_PROTECTED.writer().writeByte((sbyte)type);
			message_HAIRMOD_PROTECTED.writer().writeShort(idItem);
			message_HAIRMOD_PROTECTED.writer().writeShort((short)idMaterial);
			message_HAIRMOD_PROTECTED.writer().writeByte((sbyte)ilock);
		}
		catch (Exception)
		{
		}
		session.sendMessage(message_HAIRMOD_PROTECTED);
	}

	public void doRequestThemThuocTinh(short idItem, int type, int idBot, int lockbot, int idDatiengiai, int lockda)
	{
		Message_HAIRMOD_PROTECTED message_HAIRMOD_PROTECTED = new Message_HAIRMOD_PROTECTED((sbyte)(-68));
		try
		{
			message_HAIRMOD_PROTECTED.writer().writeByte((sbyte)type);
			message_HAIRMOD_PROTECTED.writer().writeShort(idItem);
			message_HAIRMOD_PROTECTED.writer().writeShort((short)idBot);
			message_HAIRMOD_PROTECTED.writer().writeByte((sbyte)lockbot);
			message_HAIRMOD_PROTECTED.writer().writeShort((short)idDatiengiai);
			message_HAIRMOD_PROTECTED.writer().writeByte((sbyte)lockda);
		}
		catch (Exception)
		{
		}
		session.sendMessage(message_HAIRMOD_PROTECTED);
	}

	public void doRequestActionCheDo(int type, short idItem)
	{
		Message_HAIRMOD_PROTECTED message_HAIRMOD_PROTECTED = new Message_HAIRMOD_PROTECTED((sbyte)(-52));
		try
		{
			message_HAIRMOD_PROTECTED.writer().writeByte((sbyte)type);
			message_HAIRMOD_PROTECTED.writer().writeShort(idItem);
		}
		catch (Exception ex)
		{
			Debug.Log(ex.Message);
		}
		session.sendMessage(message_HAIRMOD_PROTECTED);
	}

	public void requestQuanLyBang(short idClan)
	{
		Message_HAIRMOD_PROTECTED message_HAIRMOD_PROTECTED = new Message_HAIRMOD_PROTECTED((sbyte)(-53));
		try
		{
			message_HAIRMOD_PROTECTED.writer().writeShort(idClan);
		}
		catch (Exception ex)
		{
			Debug.Log(ex.Message);
		}
		session.sendMessage(message_HAIRMOD_PROTECTED);
	}

	public void doGetImgIcon(short id)
	{
		Message_HAIRMOD_PROTECTED message_HAIRMOD_PROTECTED = new Message_HAIRMOD_PROTECTED((sbyte)(-51));
		try
		{
			message_HAIRMOD_PROTECTED.writer().writeShort(id);
		}
		catch (Exception ex)
		{
			Debug.Log(ex.Message);
		}
		session.sendMessage(message_HAIRMOD_PROTECTED);
	}

	public void doRequestChangTypeItem(short itemID, int i)
	{
		Message_HAIRMOD_PROTECTED message_HAIRMOD_PROTECTED = new Message_HAIRMOD_PROTECTED((sbyte)(-54));
		try
		{
			message_HAIRMOD_PROTECTED.writer().writeByte((sbyte)i);
			message_HAIRMOD_PROTECTED.writer().writeShort(itemID);
		}
		catch (Exception ex)
		{
			Debug.Log(ex.Message);
		}
		session.sendMessage(message_HAIRMOD_PROTECTED);
	}

	public void sendGetImgIsanimal(int typeMonter, int idChar)
	{
		Message_HAIRMOD_PROTECTED message_HAIRMOD_PROTECTED = new Message_HAIRMOD_PROTECTED((sbyte)(-57));
		try
		{
			message_HAIRMOD_PROTECTED.writer().writeByte((sbyte)typeMonter);
			message_HAIRMOD_PROTECTED.writer().writeShort((short)idChar);
		}
		catch (Exception ex)
		{
			Debug.Log(ex.Message);
		}
		session.sendMessage(message_HAIRMOD_PROTECTED);
	}

	public void requestCharMonter(int idChar)
	{
		Message_HAIRMOD_PROTECTED message_HAIRMOD_PROTECTED = new Message_HAIRMOD_PROTECTED((sbyte)(-57));
		try
		{
			message_HAIRMOD_PROTECTED.writer().writeByte(-1);
			message_HAIRMOD_PROTECTED.writer().writeShort((short)idChar);
		}
		catch (Exception ex)
		{
			Debug.Log(ex.Message);
		}
		session.sendMessage(message_HAIRMOD_PROTECTED);
	}

	public void doPlant(sbyte type, sbyte idFarm)
	{
		Message_HAIRMOD_PROTECTED message_HAIRMOD_PROTECTED = new Message_HAIRMOD_PROTECTED((sbyte)(-59));
		try
		{
			message_HAIRMOD_PROTECTED.writer().writeByte(type);
			message_HAIRMOD_PROTECTED.writer().writeByte(idFarm);
		}
		catch (Exception ex)
		{
			Debug.Log(ex.Message);
		}
		session.sendMessage(message_HAIRMOD_PROTECTED);
	}

	public void sendTickets(sbyte num)
	{
		Message_HAIRMOD_PROTECTED message_HAIRMOD_PROTECTED = new Message_HAIRMOD_PROTECTED((sbyte)(-63));
		try
		{
			message_HAIRMOD_PROTECTED.writer().writeByte(num);
		}
		catch (Exception)
		{
		}
		session.sendMessage(message_HAIRMOD_PROTECTED);
	}

	public void invite2Party(int idUser, int type)
	{
		Message_HAIRMOD_PROTECTED message_HAIRMOD_PROTECTED = init(49);
		try
		{
			message_HAIRMOD_PROTECTED.writer().writeByte((sbyte)type);
			message_HAIRMOD_PROTECTED.writer().writeShort((short)idUser);
		}
		catch (Exception ex)
		{
			Debug.Log(ex.Message);
		}
		session.sendMessage(message_HAIRMOD_PROTECTED);
	}

	public void onStartLuckyDraw(int index)
	{
		Message_HAIRMOD_PROTECTED message_HAIRMOD_PROTECTED = new Message_HAIRMOD_PROTECTED((sbyte)87);
		try
		{
			message_HAIRMOD_PROTECTED.writer().writeByte((sbyte)index);
		}
		catch (Exception ex)
		{
			Debug.Log(ex.Message);
		}
		session.sendMessage(message_HAIRMOD_PROTECTED);
	}

	public void onSendInfoQuest(int type, int idQuest, int mainsub)
	{
		Message_HAIRMOD_PROTECTED message_HAIRMOD_PROTECTED = new Message_HAIRMOD_PROTECTED((sbyte)(-64));
		try
		{
			message_HAIRMOD_PROTECTED.writer().writeByte((sbyte)type);
			message_HAIRMOD_PROTECTED.writer().writeShort((short)idQuest);
			message_HAIRMOD_PROTECTED.writer().writeByte((sbyte)mainsub);
		}
		catch (Exception ex)
		{
			Debug.Log(ex.Message);
		}
		session.sendMessage(message_HAIRMOD_PROTECTED);
	}

	public void sendConfirmTrade()
	{
		Message_HAIRMOD_PROTECTED message_HAIRMOD_PROTECTED = init(66);
		try
		{
			message_HAIRMOD_PROTECTED.writer().writeByte(5);
			session.sendMessage(message_HAIRMOD_PROTECTED);
			message_HAIRMOD_PROTECTED.cleanup();
		}
		catch (Exception)
		{
		}
	}

	public void sendInfoFruit(int index, int type)
	{
		Message_HAIRMOD_PROTECTED message_HAIRMOD_PROTECTED = init(-66);
		try
		{
			message_HAIRMOD_PROTECTED.writer().writeByte((sbyte)type);
			message_HAIRMOD_PROTECTED.writer().writeByte((sbyte)index);
			session.sendMessage(message_HAIRMOD_PROTECTED);
		}
		catch (Exception)
		{
		}
	}

	public void douseGemItem(int idGemTemplate, sbyte locks)
	{
		Message_HAIRMOD_PROTECTED message_HAIRMOD_PROTECTED = init(-67);
		try
		{
			message_HAIRMOD_PROTECTED.writer().writeShort((short)idGemTemplate);
			message_HAIRMOD_PROTECTED.writer().writeByte(locks);
			session.sendMessage(message_HAIRMOD_PROTECTED);
		}
		catch (Exception)
		{
		}
	}

	public void doAutoImbue(int idItem, mVector_HAIRMOD_PROTECTED idGem, int plus, mVector_HAIRMOD_PROTECTED mlock, int isauto)
	{
		Message_HAIRMOD_PROTECTED message_HAIRMOD_PROTECTED = init(-69);
		try
		{
			message_HAIRMOD_PROTECTED.writer().writeShort((short)idItem);
			message_HAIRMOD_PROTECTED.writer().writeByte((sbyte)plus);
			message_HAIRMOD_PROTECTED.writer().writeByte((sbyte)idGem.size());
			for (int i = 0; i < idGem.size(); i++)
			{
				short value = short.Parse((string)idGem.elementAt(i));
				message_HAIRMOD_PROTECTED.writer().writeShort(value);
				string s = (string)mlock.elementAt(i);
				message_HAIRMOD_PROTECTED.writer().writeByte(sbyte.Parse(s));
			}
			message_HAIRMOD_PROTECTED.writer().writeByte((sbyte)isauto);
			session.sendMessage(message_HAIRMOD_PROTECTED);
		}
		catch (Exception)
		{
		}
	}

	public void doRequestSelItem(int type, int idItem, int price, int priceBid)
	{
		Message_HAIRMOD_PROTECTED message_HAIRMOD_PROTECTED = init(62);
		try
		{
			message_HAIRMOD_PROTECTED.writer().writeByte((sbyte)type);
			message_HAIRMOD_PROTECTED.writer().writeShort((short)idItem);
			message_HAIRMOD_PROTECTED.writer().writeInt(price);
			message_HAIRMOD_PROTECTED.writer().writeInt(priceBid);
			session.sendMessage(message_HAIRMOD_PROTECTED);
		}
		catch (Exception)
		{
		}
	}

	public void doBuyItemShopOnline(int id, int newItem_searchItem)
	{
		Message_HAIRMOD_PROTECTED message_HAIRMOD_PROTECTED = init(62);
		try
		{
			message_HAIRMOD_PROTECTED.writer().writeByte(3);
			message_HAIRMOD_PROTECTED.writer().writeByte((sbyte)newItem_searchItem);
			message_HAIRMOD_PROTECTED.writer().writeShort((short)id);
			session.sendMessage(message_HAIRMOD_PROTECTED);
		}
		catch (Exception)
		{
		}
	}

	public void doBid(int index, int newItem_searchItem, int price)
	{
		Message_HAIRMOD_PROTECTED message_HAIRMOD_PROTECTED = init(62);
		try
		{
			message_HAIRMOD_PROTECTED.writer().writeByte(6);
			message_HAIRMOD_PROTECTED.writer().writeByte((sbyte)newItem_searchItem);
			message_HAIRMOD_PROTECTED.writer().writeShort((short)index);
			message_HAIRMOD_PROTECTED.writer().writeInt(price);
			session.sendMessage(message_HAIRMOD_PROTECTED);
		}
		catch (Exception)
		{
		}
	}

	public void doGetItemBackInventory(int index)
	{
		Message_HAIRMOD_PROTECTED message_HAIRMOD_PROTECTED = init(62);
		try
		{
			message_HAIRMOD_PROTECTED.writer().writeByte(4);
			message_HAIRMOD_PROTECTED.writer().writeByte((sbyte)index);
			session.sendMessage(message_HAIRMOD_PROTECTED);
		}
		catch (Exception)
		{
		}
	}

	public void doGetMoneySellInventory()
	{
		Message_HAIRMOD_PROTECTED message_HAIRMOD_PROTECTED = init(62);
		try
		{
			message_HAIRMOD_PROTECTED.writer().writeByte(5);
			session.sendMessage(message_HAIRMOD_PROTECTED);
		}
		catch (Exception)
		{
		}
	}

	public void doSearchItemSell(int level, int typeItem, int colorItem, int pham, int cong)
	{
		Message_HAIRMOD_PROTECTED message_HAIRMOD_PROTECTED = init(62);
		try
		{
			message_HAIRMOD_PROTECTED.writer().writeByte(2);
			message_HAIRMOD_PROTECTED.writer().writeByte((sbyte)level);
			message_HAIRMOD_PROTECTED.writer().writeByte((sbyte)typeItem);
			message_HAIRMOD_PROTECTED.writer().writeByte((sbyte)colorItem);
			message_HAIRMOD_PROTECTED.writer().writeByte((sbyte)pham);
			message_HAIRMOD_PROTECTED.writer().writeByte((sbyte)cong);
			session.sendMessage(message_HAIRMOD_PROTECTED);
		}
		catch (Exception)
		{
		}
	}

	public void doSearchByName(string name)
	{
		Message_HAIRMOD_PROTECTED message_HAIRMOD_PROTECTED = init(62);
		try
		{
			message_HAIRMOD_PROTECTED.writer().writeByte(7);
			message_HAIRMOD_PROTECTED.writer().writeUTF(name);
			session.sendMessage(message_HAIRMOD_PROTECTED);
		}
		catch (Exception)
		{
		}
	}

	public void oninfoBydialog(string[] text)
	{
		Message_HAIRMOD_PROTECTED message_HAIRMOD_PROTECTED = init(-72);
		try
		{
			sbyte b = (sbyte)text.Length;
			message_HAIRMOD_PROTECTED.writer().writeByte(b);
			for (int i = 0; i < b; i++)
			{
				message_HAIRMOD_PROTECTED.writer().writeUTF(text[i]);
			}
			session.sendMessage(message_HAIRMOD_PROTECTED);
		}
		catch (Exception)
		{
		}
	}
}
