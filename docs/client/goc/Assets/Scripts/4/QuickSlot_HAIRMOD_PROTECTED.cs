using System;

public class QuickSlot_HAIRMOD_PROTECTED
{
	public int quickslotType = -1;

	public int potionType;

	private sbyte skillType;

	private sbyte indexPotion;

	public static readonly sbyte TYPE_SKILL = 1;

	public static readonly sbyte TYPE_POTION = 2;

	public static readonly sbyte[] WEAPON_OF_CLAZZ = new sbyte[10] { 0, 1, 2, 3, 4, 0, 1, 2, 3, 4 };

	public static Image_HAIRMOD_PROTECTED img = null;

	public bool isBuff;

	public static void getImg()
	{
		img = FilePack_HAIRMOD_PROTECTED.getImg("cooldown");
	}

	public void setIsSkill(int skillType, int clazz, bool isBuff)
	{
		quickslotType = TYPE_SKILL;
		this.skillType = (sbyte)skillType;
		this.isBuff = isBuff;
	}

	public void paint(MyGraphics_HAIRMOD_PROTECTED g, int x, int y)
	{
		if (!Main_HAIRMOD_PROTECTED.isPC)
		{
			if (img == null)
			{
				return;
			}
			try
			{
				if (quickslotType == -1)
				{
					return;
				}
				if (quickslotType == TYPE_SKILL)
				{
					if (GameData_HAIRMOD_PROTECTED.imgSkillIcon != null)
					{
						switch (Canvas_HAIRMOD_PROTECTED.gameScr.mainChar.clazz)
						{
						case 0:
							g.drawRegion(Res_HAIRMOD_PROTECTED.imgIconSkillKiem, 0, skillType * 20, 20, 20, 0, x, y, 3);
							break;
						case 1:
							g.drawRegion(Res_HAIRMOD_PROTECTED.imgIconSkillDao, 0, skillType * 20, 20, 20, 0, x, y, 3);
							break;
						case 2:
							g.drawRegion(Res_HAIRMOD_PROTECTED.imgIconSkillPs, 0, skillType * 20, 20, 20, 0, x, y, 3);
							break;
						case 3:
							g.drawRegion(Res_HAIRMOD_PROTECTED.imgIconSkillBua, 0, skillType * 20, 20, 20, 0, x, y, 3);
							break;
						case 4:
							g.drawRegion(Res_HAIRMOD_PROTECTED.imgIconSkillCung, 0, skillType * 20, 20, 20, 0, x, y, 3);
							break;
						}
					}
					long num = mSystem_HAIRMOD_PROTECTED.getCurrentTimeMillis() - Canvas_HAIRMOD_PROTECTED.gameScr.mainChar.timeLastUseSkills[skillType];
					if (num < Canvas_HAIRMOD_PROTECTED.gameScr.mainChar.coolDownSkill[skillType])
					{
						int num2 = (int)(num * 26 / Canvas_HAIRMOD_PROTECTED.gameScr.mainChar.coolDownSkill[skillType]);
						g.drawRegion(img, 0, num2, 39, 39 - num2, 0, x, y + 1 + num2 / 2, 3);
					}
				}
				else
				{
					Res_HAIRMOD_PROTECTED.paintPotion(g, MainChar_HAIRMOD_PROTECTED.listPotion[potionType].index, x, y, 3);
					MFont_HAIRMOD_PROTECTED.smallFont.drawString(g, Canvas_HAIRMOD_PROTECTED.gameScr.mainChar.potions[potionType] + string.Empty, x + 3, y + 3, MFont_HAIRMOD_PROTECTED.CENTER);
					long num3 = mSystem_HAIRMOD_PROTECTED.getCurrentTimeMillis() - Canvas_HAIRMOD_PROTECTED.gameScr.mainChar.potionLastTimeUse[potionType];
					if (num3 < MainChar_HAIRMOD_PROTECTED.listPotion[potionType].delay)
					{
						int num4 = (int)(num3 * 26 / MainChar_HAIRMOD_PROTECTED.listPotion[potionType].delay);
						g.drawRegion(img, 0, num4, 39, 39 - num4, 0, x, y + 1 + num4 / 2, 3);
					}
				}
				return;
			}
			catch (Exception)
			{
				Out_HAIRMOD_PROTECTED.println("ERROR QUICKSLOT");
				return;
			}
		}
		try
		{
			if (quickslotType == -1)
			{
				return;
			}
			if (quickslotType == TYPE_SKILL)
			{
				if (GameData_HAIRMOD_PROTECTED.imgSkillIcon != null)
				{
					GameData_HAIRMOD_PROTECTED.imgSkillIcon.drawFrame(skillType, x - 2, y - 1, 0, 0, g);
				}
				long num5 = mSystem_HAIRMOD_PROTECTED.getCurrentTimeMillis() - Canvas_HAIRMOD_PROTECTED.gameScr.mainChar.timeLastUseSkills[skillType];
				if (num5 < Canvas_HAIRMOD_PROTECTED.gameScr.mainChar.coolDownSkill[skillType])
				{
					int num6 = (int)(num5 * 20 / Canvas_HAIRMOD_PROTECTED.gameScr.mainChar.coolDownSkill[skillType]);
					g.setColor(0, 0.5f);
					g.fillRect(x - 2, y - 2 + num6, 20, 20);
				}
			}
			else
			{
				Res_HAIRMOD_PROTECTED.paintPotion(g, MainChar_HAIRMOD_PROTECTED.listPotion[potionType].index, x, y, 0);
				MFont_HAIRMOD_PROTECTED.smallFontColor[0].drawString(g, Canvas_HAIRMOD_PROTECTED.gameScr.mainChar.potions[potionType] + string.Empty, x + 16, y + 12, 1);
				long num7 = mSystem_HAIRMOD_PROTECTED.getCurrentTimeMillis() - Canvas_HAIRMOD_PROTECTED.gameScr.mainChar.potionLastTimeUse[potionType];
				if (num7 < MainChar_HAIRMOD_PROTECTED.listPotion[potionType].delay)
				{
					int num8 = (int)(num7 * 20 / MainChar_HAIRMOD_PROTECTED.listPotion[potionType].delay);
					g.setColor(0, 0.5f);
					g.setColor(0, 0.5f);
					g.fillRect(x - 2, y - 2 + num8, 20, 20);
				}
			}
		}
		catch (Exception)
		{
			Out_HAIRMOD_PROTECTED.println("ERROR QUICKSLOT");
		}
	}

	public sbyte getSkillType()
	{
		return skillType;
	}

	public int getPotionType()
	{
		return potionType;
	}

	public void setIsPotion(int potionType)
	{
		quickslotType = TYPE_POTION;
		this.potionType = potionType;
	}

	public void setIsNothing()
	{
		quickslotType = -1;
	}
}
