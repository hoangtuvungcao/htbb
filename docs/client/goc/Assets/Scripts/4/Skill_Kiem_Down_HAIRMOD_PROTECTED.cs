using System.IO;
using UnityEngine;

public class Skill_Kiem_Down_HAIRMOD_PROTECTED : Effect_HAIRMOD_PROTECTED
{
	private static FrameImage_HAIRMOD_PROTECTED imgSword;

	private static Image_HAIRMOD_PROTECTED imgCut;

	private int count = 120;

	private sbyte frame;

	private sbyte g = 5;

	private sbyte delay = 10;

	private sbyte countPop;

	public Skill_Kiem_Down_HAIRMOD_PROTECTED(int x, int y)
	{
		base.x = x;
		base.y = y;
		Effect_HAIRMOD_PROTECTED o = new Effect_HAIRMOD_PROTECTED(x, y, 34)
		{
			loop = 3
		};
		EffectManager_HAIRMOD_PROTECTED.lowEffects.addElement(o);
	}

	public static void load()
	{
		try
		{
			imgSword = new FrameImage_HAIRMOD_PROTECTED(Image_HAIRMOD_PROTECTED.createImage(Main_HAIRMOD_PROTECTED.res + "/sword skill/kiem01"), 12, 39);
			imgCut = Image_HAIRMOD_PROTECTED.createImage(Main_HAIRMOD_PROTECTED.res + "/sword skill/all1");
		}
		catch (IOException)
		{
			Debug.Log(" loi tai ham load skill kiem down");
		}
	}

	public override void update()
	{
		if (delay > 0)
		{
			delay--;
		}
		if (delay <= 0 && count > 0)
		{
			count -= g;
			g += 5;
			if (count < 0)
			{
				count = 0;
				EffectManager_HAIRMOD_PROTECTED.hiEffects.removeElement(this);
				EffectManager_HAIRMOD_PROTECTED.lowEffects.addElement(this);
			}
		}
		if (count == 0 && Canvas_HAIRMOD_PROTECTED.gameTick % 2 == 1)
		{
			for (int i = 0; i < 10; i++)
			{
				int num = countPop * Util_HAIRMOD_PROTECTED.Cos(i * 36) >> 10;
				int num2 = -(countPop * Util_HAIRMOD_PROTECTED.Sin(i * 36)) >> 10;
				num2 += -(num2 / 3) * 2;
				EffectManager_HAIRMOD_PROTECTED.addLowEffect(x + num, y + num2 - 10, 15);
			}
			countPop += 10;
			if (countPop == 30)
			{
				EffectManager_HAIRMOD_PROTECTED.lowEffects.removeElement(this);
			}
		}
	}

	public override void paint(MyGraphics_HAIRMOD_PROTECTED g)
	{
		if (count > 0)
		{
			imgSword.drawFrame(frame / 3, x, y - count, 0, MyGraphics_HAIRMOD_PROTECTED.BOTTOM | MyGraphics_HAIRMOD_PROTECTED.HCENTER, g);
		}
		else
		{
			g.drawImage(imgCut, x, y, MyGraphics_HAIRMOD_PROTECTED.BOTTOM | MyGraphics_HAIRMOD_PROTECTED.HCENTER);
		}
		frame++;
		if (frame >= 6)
		{
			frame = 0;
		}
	}
}
