public class Skill_blood_down_HAIRMOD_PROTECTED : Effect_HAIRMOD_PROTECTED
{
	private int count = 120;

	private sbyte frame;

	private sbyte g = 5;

	private sbyte delay = 10;

	private sbyte countPop;

	private new sbyte f;

	private sbyte[] arrframe = new sbyte[4] { 0, 1, 2, 3 };

	private int dame;

	public Skill_blood_down_HAIRMOD_PROTECTED(int x, int y, int dame)
	{
		base.x = x;
		base.y = y;
		delay = (sbyte)Canvas_HAIRMOD_PROTECTED.random(5, 20);
		EffectManager_HAIRMOD_PROTECTED.addLowEffect(new Effect_HAIRMOD_PROTECTED(x, y, 34)
		{
			loop = 3
		});
		this.dame = dame;
	}

	public override void update()
	{
		f++;
		if (f > arrframe.Length - 1)
		{
			f = 0;
		}
		frame = arrframe[f];
		if (delay > 0)
		{
			delay--;
		}
		if (delay <= 0 && count > 0)
		{
			count -= g;
			g += 8;
			if (count < 0)
			{
				count = 0;
				EffectManager_HAIRMOD_PROTECTED.hiEffects.removeElement(this);
				EffectManager_HAIRMOD_PROTECTED.lowEffects.addElement(this);
			}
		}
		if (count == 0 && Canvas_HAIRMOD_PROTECTED.gameTick % 2 == 1)
		{
			EffectManager_HAIRMOD_PROTECTED.addLowEffect(x, y, 62);
			EffectManager_HAIRMOD_PROTECTED.lowEffects.removeElement(this);
			Canvas_HAIRMOD_PROTECTED.gameScr.startFlyText("- " + dame, 0, x, y - 25, 1, -2);
		}
	}

	public override void paint(MyGraphics_HAIRMOD_PROTECTED g)
	{
		Image_HAIRMOD_PROTECTED imgeffect = Res_HAIRMOD_PROTECTED.getImgeffect(61);
		if (imgeffect != null)
		{
			g.drawRegion(imgeffect, 0, frame * Effect_HAIRMOD_PROTECTED.HEIGHT[61], Effect_HAIRMOD_PROTECTED.WIDTH[61], Effect_HAIRMOD_PROTECTED.HEIGHT[61], 0, x, y - count, MyGraphics_HAIRMOD_PROTECTED.VCENTER | MyGraphics_HAIRMOD_PROTECTED.HCENTER);
		}
	}
}
