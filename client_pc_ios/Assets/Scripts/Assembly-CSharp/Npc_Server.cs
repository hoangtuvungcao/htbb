public class Npc_Server : NPC
{
	public short idImg;

	public sbyte nFrame;

	public sbyte index;

	public Npc_Server()
	{
		npcType = 1;
		catagory = 2;
	}

	public Npc_Server(int tx, int ty, int type, FilePack file)
		: base(tx, ty, type, file)
	{
	}

	/// <summary>
	/// NPC su kien (lenh -50) theo idImg (anh icon idImg + 4200) -&gt; {idImg, dex} ve bang
	/// PokeBody (viec G7.B2). type cua Npc_Server la ID server, KHONG phai type NPC tinh nen
	/// khong dung POKE_DEX_THEO_TYPE. idImg la do vat (cong, bang khu, noi) thi ve do vat PMD
	/// qua VatThePokemon.NPC. Bang day du: du_lieu/bang_npc_su_kien.csv (sua 2 noi cung
	/// luc - test/KiemHinhPokemon so khop).
	/// </summary>
	public static readonly short[][] POKE_DEX_THEO_IDIMG = new short[][]
	{
		new short[] { 14, 52 },   // Than tai / Chuc mung nam moi / Nha Vua -> Meowth
		new short[] { 20, 282 },  // Phu thuy de thuong / Me hien -> Gardevoir
		new short[] { 21, 667 },  // Long den Lan Su Tu -> Litleo
		new short[] { 23, 460 },  // Cay thong Noel -> Abomasnow
		new short[] { 24, 225 },  // ong Gia Noel -> Delibird
		new short[] { 27, 448 },  // Ta/Huu thong linh -> Lucario
		new short[] { 28, 214 },  // Tran thong linh -> Heracross
		new short[] { 29, 185 },  // Cay li xi -> Sudowoodo
		new short[] { 30, 185 }   // Cay li xi -> Sudowoodo
	};

	public static int dexTheoIdImg(int idImg)
	{
		for (int i = 0; i < POKE_DEX_THEO_IDIMG.Length; i++)
		{
			if (POKE_DEX_THEO_IDIMG[i][0] == idImg)
			{
				return POKE_DEX_THEO_IDIMG[i][1];
			}
		}
		return 0;
	}

	public override int getPokeDex()
	{
		return dexTheoIdImg(idImg);
	}

	public override void paint(MyGraphics g)
	{
		int caoIcon = height;
		if (paintPokemon(g))
		{
			caoIcon = PokeBody.caoTrenChan(getPokeDex(), false, 100) + 4;
		}
		else if (VatThePokemon.veNpc(g, idImg, x, y))
		{
			caoIcon = VatThePokemon.caoNpc(idImg);
		}
		else
		{
			ImageIcon imgIcon = GameData.getImgIcon((short)(idImg + 4200));
			if (imgIcon != null && !imgIcon.isLoad)
			{
				g.drawRegion(GameData.getImgIcon((short)(idImg + 4200)).img, 0, height * index, width, height, 0, x, y, MyGraphics.BOTTOM | MyGraphics.HCENTER);
			}
		}
		Image imgQuest = GameScr.getImgQuest(type);
		if (!imgQuest.Equals(Res.imgSelect))
		{
			g.drawImage(imgQuest, x, y - caoIcon - 5, MyGraphics.HCENTER | MyGraphics.BOTTOM);
		}
	}

	public override void paintCorner(MyGraphics g, int xCorner, int yCorner)
	{
		int dex = getPokeDex();
		if (dex > 0 && ChanDungPoke.paintHuyHieu(g, dex, false, xCorner, yCorner - 8, 26))
		{
			return;
		}
		if (VatThePokemon.veGocNpc(g, idImg, xCorner, yCorner - 8, 24))
		{
			return;
		}
		g.setClip(xCorner - 10, yCorner + 12 - height, 20, 22);
		ImageIcon imgIcon = GameData.getImgIcon((short)(idImg + 4200));
		if (imgIcon != null && !imgIcon.isLoad)
		{
			g.drawRegion(GameData.getImgIcon((short)(idImg + 4200)).img, 0, 0, width, height, 0, xCorner, yCorner + 12 - height, MyGraphics.TOP | MyGraphics.HCENTER);
		}
	}

	public override void update()
	{
		if (Canvas.gameTick % 4 == 0)
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
