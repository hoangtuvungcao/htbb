public class NPC_HAIRMOD_PROTECTED : Actor_HAIRMOD_PROTECTED
{
	public int type;

	public int idLinhGac;

	private Image_HAIRMOD_PROTECTED[] img;

	public string name;

	public sbyte npcType;

	public new sbyte typeLimit;

	public static readonly string[] NPCNAME = new string[32]
	{
		"Dì út HP", "Bà tám tạp hóa", "Hắc ngưu", "Thiết bì", "Lính gác", "Trưởng làng", "Phú ông", "Xa phu", "ông nội", "Anh bảy",
		"Hoa tiêu", "Nhất giáp", "Nhị giáp", "Tam giáp", "Tứ giáp", "Ngũ giáp", "Nhất ngưu", "Nhị ngưu", "Tam ngưu", "Tứ ngưu",
		"Ngũ ngưu", "Lâm tướng quân", "Nhật thương nhân", "Hỏa xích", "Bảo ngọc", "Trần thống lĩnh", "Kim hoa", "Giáp Sư", "Kiếm Sư", "Bội Châu",
		"An Tâm", "Lộc Phát"
	};

	private static sbyte[] idImg = new sbyte[32]
	{
		0, 1, 2, 3, 4, 5, 6, 7, 8, 9,
		10, 11, 11, 11, 11, 11, 12, 12, 12, 12,
		12, 14, 13, 15, 16, 17, 1, 3, 2, 6,
		9, 13
	};

	private int dy;

	private int fra;

	public static sbyte[] NPCX = new sbyte[18]
	{
		0, 0, 2, 0, 1, 0, 0, 2, 0, 0,
		0, 0, 0, 0, 0, 0, 0, 0
	};

	public static sbyte[] NPCY = new sbyte[18]
	{
		-7, -4, -5, -5, -5, -9, -9, -10, -7, 0,
		-11, -10, -9, -10, 0, -5, -9, -5
	};

	public NPC_HAIRMOD_PROTECTED()
	{
	}

	public NPC_HAIRMOD_PROTECTED(int tx, int ty, int type, FilePack_HAIRMOD_PROTECTED file)
	{
		catagory = 2;
		x = (short)(tx * 16 + 8);
		y = (short)(ty * 16 + 8);
		height = 30;
		width = 40;
		this.type = type;
		img = new Image_HAIRMOD_PROTECTED[2];
		img[0] = file.loadImage("npc" + idImg[type] + "0.png");
		img[1] = file.loadImage("npc" + idImg[type] + "1.png");
		if (type == 4)
		{
			Tilemap_HAIRMOD_PROTECTED.idLinhGac++;
			idLinhGac = Tilemap_HAIRMOD_PROTECTED.idLinhGac;
		}
	}

	public override string getDisplayName()
	{
		if (name != null)
		{
			return name;
		}
		return NPCNAME[type];
	}

	public override void paint(MyGraphics_HAIRMOD_PROTECTED g)
	{
		g.drawImage(img[1], x, y, MyGraphics_HAIRMOD_PROTECTED.BOTTOM | MyGraphics_HAIRMOD_PROTECTED.HCENTER);
		if (img[0] != null)
		{
			g.drawImage(img[0], x + NPCX[idImg[type]], y + NPCY[idImg[type]] + fra / 5, MyGraphics_HAIRMOD_PROTECTED.BOTTOM | MyGraphics_HAIRMOD_PROTECTED.HCENTER);
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
		if (img[0] != null)
		{
			g.drawImage(img[0], xCorner, yCorner + 12 - height, MyGraphics_HAIRMOD_PROTECTED.TOP | MyGraphics_HAIRMOD_PROTECTED.HCENTER);
		}
		else if (img[1] != null)
		{
			g.drawImage(img[1], xCorner, yCorner + 12 - height, MyGraphics_HAIRMOD_PROTECTED.TOP | MyGraphics_HAIRMOD_PROTECTED.HCENTER);
		}
	}

	public override void setPosTo(short x, short y)
	{
		base.x = x;
		base.y = y;
	}

	public override void update()
	{
		base.update();
		if (chat != null && this != Canvas_HAIRMOD_PROTECTED.gameScr.focusedActor)
		{
			chat = null;
		}
		if (img[0] != null)
		{
			fra++;
			if (fra > 9)
			{
				fra = 0;
			}
		}
		if (Canvas_HAIRMOD_PROTECTED.gameScr.posNpcRequest != null && Util_HAIRMOD_PROTECTED.abs(x / 16 - Canvas_HAIRMOD_PROTECTED.gameScr.posNpcRequest.x / 16) <= 1 && Util_HAIRMOD_PROTECTED.abs(y / 16 - Canvas_HAIRMOD_PROTECTED.gameScr.posNpcRequest.y / 16) <= 1)
		{
			if (dy > -2)
			{
				dy--;
			}
			else
			{
				dy = 0;
			}
		}
	}

	public virtual int getLimxL()
	{
		return 0;
	}

	public virtual int getLimxR()
	{
		return 0;
	}

	public virtual int getLimyU()
	{
		return 0;
	}

	public virtual int getLimyD()
	{
		return 0;
	}

	public override bool isNPC()
	{
		return true;
	}

	public override int isNpcQuest()
	{
		return GameScr_HAIRMOD_PROTECTED.isNpcQuest(type);
	}

	public override int getNpcType()
	{
		return npcType;
	}

	public override int getTypeNpc()
	{
		return type;
	}
}
