public class Skill_Eff_end_HAIRMOD_PROTECTED : Effect_HAIRMOD_PROTECTED
{
	private new int x;

	private new int y;

	private int angle;

	private int delay;

	private int yTo;

	private int vy;

	public Skill_Eff_end_HAIRMOD_PROTECTED(int x, int y, int transForm)
	{
		this.x = Canvas_HAIRMOD_PROTECTED.random(x - 10, x + 10);
		this.y = Canvas_HAIRMOD_PROTECTED.random(y - 10, y + 10);
		angle = transForm;
		type = 0;
	}

	public Skill_Eff_end_HAIRMOD_PROTECTED(int xF, int yF, LiveActor_HAIRMOD_PROTECTED to)
	{
		x = xF;
		y = yF;
		yTo = to.y;
		type = 1;
	}

	public override void update()
	{
		if (type == 0)
		{
			delay++;
			if (delay > 50)
			{
				EffectManager_HAIRMOD_PROTECTED.hiEffects.removeElement(this);
				EffectManager_HAIRMOD_PROTECTED.lowEffects.removeElement(this);
				delay = 0;
			}
		}
		else
		{
			y += vy;
			vy += 2;
			if (y > yTo)
			{
				vy = 0;
				EffectManager_HAIRMOD_PROTECTED.hiEffects.removeElement(this);
			}
		}
	}

	public override void paint(MyGraphics_HAIRMOD_PROTECTED g)
	{
		if (type == 0)
		{
			if (delay > 0 && Res_HAIRMOD_PROTECTED.getImgArrow(7) != null)
			{
				g.drawRegion(Res_HAIRMOD_PROTECTED.imgArrow[7], 0, 38, 20, 18, angle, x, y, 3);
			}
			return;
		}
		Image_HAIRMOD_PROTECTED image_HAIRMOD_PROTECTED = Res_HAIRMOD_PROTECTED.loadImgEff(5);
		if (image_HAIRMOD_PROTECTED != null)
		{
			g.drawRegion(image_HAIRMOD_PROTECTED, 0, 0, 32, 32, 0, x, y, 3);
			g.drawRegion(image_HAIRMOD_PROTECTED, 0, 32, 32, 32, 0, x, y, 3);
			g.drawRegion(image_HAIRMOD_PROTECTED, 0, 0, 32, 32, 0, x + 2, y + 2, 3);
			g.drawRegion(image_HAIRMOD_PROTECTED, 0, 32, 32, 32, 0, x + 2, y + 2, 3);
		}
	}
}
