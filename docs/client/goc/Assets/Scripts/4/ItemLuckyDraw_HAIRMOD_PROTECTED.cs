using System;

public class ItemLuckyDraw_HAIRMOD_PROTECTED
{
	public sbyte cat;

	public sbyte isSelect = 1;

	public int id;

	public string name;

	public string[] decript;

	public void paint(MyGraphics_HAIRMOD_PROTECTED g, int x, int y)
	{
		try
		{
			switch (cat)
			{
			case 3:
			{
				ItemTemplate_HAIRMOD_PROTECTED item = Res_HAIRMOD_PROTECTED.getItem(0, id);
				GameData_HAIRMOD_PROTECTED.paintIcon(g, item.idIcon, x, y);
				break;
			}
			case 12:
				GameData_HAIRMOD_PROTECTED.paintIcon(g, (short)(id + 7500), x, y);
				break;
			case 4:
				Res_HAIRMOD_PROTECTED.paintPotion(g, MainChar_HAIRMOD_PROTECTED.listPotion[id].index, x, y, 3);
				break;
			case 6:
				Res_HAIRMOD_PROTECTED.paintGemKhoa(g, Res_HAIRMOD_PROTECTED.getGemByID((short)id).idImage, x, y);
				break;
			}
		}
		catch (Exception)
		{
		}
	}

	public string getName()
	{
		return name;
	}

	public string[] getDecript()
	{
		return (decript != null) ? decript : new string[1] { name };
	}
}
