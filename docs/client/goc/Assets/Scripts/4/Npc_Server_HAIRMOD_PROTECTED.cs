public class Npc_Server_HAIRMOD_PROTECTED : NPC_HAIRMOD_PROTECTED
{
	public short idImg;

	public sbyte nFrame;

	public sbyte index;

	public Npc_Server_HAIRMOD_PROTECTED()
	{
		npcType = 1;
		catagory = 2;
	}

	public Npc_Server_HAIRMOD_PROTECTED(int tx, int ty, int type, FilePack_HAIRMOD_PROTECTED file)
		: base(tx, ty, type, file)
	{
	}

	public override void paint(MyGraphics_HAIRMOD_PROTECTED g)
	{
		ImageIcon_HAIRMOD_PROTECTED imgIcon = GameData_HAIRMOD_PROTECTED.getImgIcon((short)(idImg + 4200));
		if (imgIcon != null && !imgIcon.isLoad)
		{
			g.drawRegion(GameData_HAIRMOD_PROTECTED.getImgIcon((short)(idImg + 4200)).img, 0, height * index, width, height, 0, x, y, MyGraphics_HAIRMOD_PROTECTED.BOTTOM | MyGraphics_HAIRMOD_PROTECTED.HCENTER);
		}
		Image_HAIRMOD_PROTECTED imgQuest = GameScr_HAIRMOD_PROTECTED.getImgQuest(type);
		if (!imgQuest.Equals(Res_HAIRMOD_PROTECTED.imgSelect))
		{
			g.drawImage(imgQuest, x, y - height - 5, MyGraphics_HAIRMOD_PROTECTED.HCENTER | MyGraphics_HAIRMOD_PROTECTED.BOTTOM);
		}
	}

	public override void paintCorner(MyGraphics_HAIRMOD_PROTECTED g, int xCorner, int yCorner)
	{
		g.setClip(xCorner - 10, yCorner + 12 - height, 20, 22);
		ImageIcon_HAIRMOD_PROTECTED imgIcon = GameData_HAIRMOD_PROTECTED.getImgIcon((short)(idImg + 4200));
		if (imgIcon != null && !imgIcon.isLoad)
		{
			g.drawRegion(GameData_HAIRMOD_PROTECTED.getImgIcon((short)(idImg + 4200)).img, 0, 0, width, height, 0, xCorner, yCorner + 12 - height, MyGraphics_HAIRMOD_PROTECTED.TOP | MyGraphics_HAIRMOD_PROTECTED.HCENTER);
		}
	}

	public override void update()
	{
		if (Canvas_HAIRMOD_PROTECTED.gameTick % 4 == 0)
		{
			index++;
		}
		if (index >= nFrame)
		{
			index = 0;
		}
	}

	public override void setPosTo(short x, short y)
	{
	}

	public override int getLimxL()
	{
		return x - width / 2 + 10;
	}

	public override int getLimxR()
	{
		return x + width / 2 - 10;
	}

	public override int getLimyD()
	{
		return y - 40;
	}

	public override int getLimyU()
	{
		return y - height * 3 / 4 - 30;
	}

	public override bool isNPC()
	{
		return true;
	}

	public override int getNpcType()
	{
		return npcType;
	}
}
