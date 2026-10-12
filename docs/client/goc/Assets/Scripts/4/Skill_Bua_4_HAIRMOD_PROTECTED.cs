public class Skill_Bua_4_HAIRMOD_PROTECTED : Effect_HAIRMOD_PROTECTED
{
	private LiveActor_HAIRMOD_PROTECTED live;

	private bool isExpl;

	private bool isSkillBoss;

	private int count;

	public Skill_Bua_4_HAIRMOD_PROTECTED(LiveActor_HAIRMOD_PROTECTED live, int x, int y, bool isBoss)
	{
		base.x = x;
		base.y = y;
		this.live = live;
		isSkillBoss = isBoss;
	}

	public override void update()
	{
		if (!isExpl)
		{
			int a = Util_HAIRMOD_PROTECTED.angle(live.x - x, -(live.y - y));
			int num = 10 * Util_HAIRMOD_PROTECTED.Cos(a) >> 10;
			int num2 = -(10 * Util_HAIRMOD_PROTECTED.Sin(a)) >> 10;
			x += num;
			y += num2;
			EffectManager_HAIRMOD_PROTECTED.addLowEffect(x, y, (!isSkillBoss) ? 4 : 35);
			if (Util_HAIRMOD_PROTECTED.distance(x, y, live.x, live.y) <= 10)
			{
				isExpl = true;
			}
		}
		else if (Canvas_HAIRMOD_PROTECTED.gameTick % 2 == 0)
		{
			for (int i = 0; i < 5; i++)
			{
				int num3 = (sbyte)(20 - Res_HAIRMOD_PROTECTED.rnd(40));
				int num4 = (sbyte)(10 - Res_HAIRMOD_PROTECTED.rnd(20));
				EffectManager_HAIRMOD_PROTECTED.addLowEffect(live.x + num3, live.y + num4, 35);
			}
			count++;
			if (count > 3)
			{
				EffectManager_HAIRMOD_PROTECTED.lowEffects.removeElement(this);
			}
		}
	}

	public override void paint(MyGraphics_HAIRMOD_PROTECTED g)
	{
	}
}
