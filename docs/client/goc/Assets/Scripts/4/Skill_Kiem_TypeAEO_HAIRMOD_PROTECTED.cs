using System;

public class Skill_Kiem_TypeAEO_HAIRMOD_PROTECTED : SkillAnimate_HAIRMOD_PROTECTED
{
	private mVector_HAIRMOD_PROTECTED mst = new mVector_HAIRMOD_PROTECTED();

	private int xPos;

	private int yPos;

	private int type;

	private Lightning_HAIRMOD_PROTECTED light;

	private bool isMonster;

	public Skill_Kiem_TypeAEO_HAIRMOD_PROTECTED(int type)
		: base(-1)
	{
		this.type = type;
	}

	public override void setMonster(mVector_HAIRMOD_PROTECTED mst)
	{
		add(mst);
	}

	private void addEff()
	{
		if (light == null)
		{
			light = new Lightning_HAIRMOD_PROTECTED();
		}
		light.reset();
		for (int i = 0; i < mst.size(); i++)
		{
			Actor_HAIRMOD_PROTECTED actor_HAIRMOD_PROTECTED = (Actor_HAIRMOD_PROTECTED)mst.elementAt(i);
			light.listPos.addElement(new Position_HAIRMOD_PROTECTED(actor_HAIRMOD_PROTECTED.x, actor_HAIRMOD_PROTECTED.y));
		}
		if (light.listPos.size() > 0)
		{
			light.setInfo(light.listPos, new Position_HAIRMOD_PROTECTED(xPos, yPos - 40), type == 4);
			EffectManager_HAIRMOD_PROTECTED.hiEffects.addElement(light);
		}
	}

	public override void setActorter(mVector_HAIRMOD_PROTECTED mst)
	{
		add(mst);
		isMonster = false;
	}

	private void add(mVector_HAIRMOD_PROTECTED mst)
	{
		this.mst.removeAllElements();
		for (int i = 0; i < mst.size(); i++)
		{
			LiveActor_HAIRMOD_PROTECTED liveActor_HAIRMOD_PROTECTED = (LiveActor_HAIRMOD_PROTECTED)mst.elementAt(i);
			Actor_HAIRMOD_PROTECTED actor_HAIRMOD_PROTECTED = Canvas_HAIRMOD_PROTECTED.gameScr.findActorByID(liveActor_HAIRMOD_PROTECTED.ID);
			if (actor_HAIRMOD_PROTECTED != null)
			{
				this.mst.addElement(actor_HAIRMOD_PROTECTED);
			}
		}
	}

	public override void updateSkill(Char_HAIRMOD_PROTECTED c)
	{
		if (c == null)
		{
			return;
		}
		c.frame = 4;
		if (c.p1 == 16)
		{
			c.state = 0;
			c.p1 = (c.p2 = (c.p3 = 0));
			c.weapon_frame = 0;
		}
		else if (c.p1 >= 14 && c.p1 < 16)
		{
			c.frame = 5;
			c.weapon_frame = 7;
		}
		else if (c.p1 == 13 || c.p1 == 12)
		{
			c.frame = 5;
			c.weapon_frame = 6;
		}
		else if (c.p1 == 11 || c.p1 == 10)
		{
			c.frame = 4;
			c.weapon_frame = 5;
		}
		else
		{
			if (c.p1 % 2 == 0)
			{
				xPos = c.x;
				yPos = c.y;
				EffectManager_HAIRMOD_PROTECTED.addHiEffect(c.x, c.y - 40, 3);
			}
			c.frame = 4;
			c.weapon_frame = 4;
		}
		if (c.p1 == 9)
		{
			addEff();
			for (int i = 0; i < mst.size(); i++)
			{
				try
				{
					if (!((Actor_HAIRMOD_PROTECTED)mst.elementAt(i)).isNPC())
					{
						LiveActor_HAIRMOD_PROTECTED liveActor_HAIRMOD_PROTECTED = (LiveActor_HAIRMOD_PROTECTED)mst.elementAt(i);
						if (c.attkPower != 0 && c.attkPower != 2000000)
						{
							Canvas_HAIRMOD_PROTECTED.gameScr.startFlyText("-" + c.attkPower, 0, liveActor_HAIRMOD_PROTECTED.x, liveActor_HAIRMOD_PROTECTED.y - 15, 1, -2);
						}
						if (c.attkEffect != 0 && c.attkEffect < AttackResult_HAIRMOD_PROTECTED.EFF_NAME.Length)
						{
							Canvas_HAIRMOD_PROTECTED.gameScr.startFlyText(AttackResult_HAIRMOD_PROTECTED.EFF_NAME[c.attkEffect], 0, liveActor_HAIRMOD_PROTECTED.x, liveActor_HAIRMOD_PROTECTED.y - 15, 2, -2);
						}
						liveActor_HAIRMOD_PROTECTED.jump();
					}
				}
				catch (Exception)
				{
				}
			}
			isMonster = false;
		}
		c.p1++;
	}

	public override void updateSkill(Monster_HAIRMOD_PROTECTED c)
	{
	}
}
