using System;

public class TruTranThanh_HAIRMOD_PROTECTED : Monster_HAIRMOD_PROTECTED
{
	private mVector_HAIRMOD_PROTECTED exp = new mVector_HAIRMOD_PROTECTED();

	private sbyte[] ATT_FRAME = new sbyte[8] { 0, 0, 1, 1, 1, 1, 1, 1 };

	private sbyte[] STAND_FRAME = new sbyte[6] { 0, 0, 0, 1, 1, 1 };

	private int typeID;

	public static bool isChopSang;

	private int dem;

	private int lap;

	private sbyte totalExp;

	private mVector_HAIRMOD_PROTECTED listTarget = new mVector_HAIRMOD_PROTECTED();

	public TruTranThanh_HAIRMOD_PROTECTED(int TYPE)
	{
		typeID = TYPE;
	}

	public override void paint(MyGraphics_HAIRMOD_PROTECTED g)
	{
		if (!isFreeze)
		{
			if (Res_HAIRMOD_PROTECTED.monsterTemplates[monster_type] != null && Res_HAIRMOD_PROTECTED.monsterTemplates[monster_type].image != null)
			{
				if (dynamicEffBottom != null)
				{
					for (int i = 0; i < dynamicEffBottom.size(); i++)
					{
						((DynamicEffect_HAIRMOD_PROTECTED)dynamicEffBottom.elementAt(i)).paint(g, x, y);
					}
				}
				g.drawRegion(Res_HAIRMOD_PROTECTED.monsterTemplates[monster_type].image, 0, frame * Res_HAIRMOD_PROTECTED.monsterTemplates[monster_type].h, Res_HAIRMOD_PROTECTED.monsterTemplates[monster_type].w, Res_HAIRMOD_PROTECTED.monsterTemplates[monster_type].h, 0, x - Res_HAIRMOD_PROTECTED.monsterTemplates[monster_type].w / 2, y - Res_HAIRMOD_PROTECTED.monsterTemplates[monster_type].h, 0);
				if (dynamicEffTop != null)
				{
					for (int j = 0; j < dynamicEffTop.size(); j++)
					{
						((DynamicEffect_HAIRMOD_PROTECTED)dynamicEffTop.elementAt(j)).paint(g, x, y);
					}
				}
			}
			paintExp(g);
		}
		paintEffSkill(g);
	}

	public override void paintCorner(MyGraphics_HAIRMOD_PROTECTED g, int xCorner, int yCorner)
	{
		try
		{
			if (Res_HAIRMOD_PROTECTED.monsterTemplates[monster_type] != null && Res_HAIRMOD_PROTECTED.monsterTemplates[monster_type].image != null)
			{
				Image_HAIRMOD_PROTECTED image = Res_HAIRMOD_PROTECTED.monsterTemplates[monster_type].image;
				_ = Res_HAIRMOD_PROTECTED.monsterTemplates[monster_type].h;
				g.drawRegion(image, 0, 0, Res_HAIRMOD_PROTECTED.monsterTemplates[monster_type].w, 40, 0, xCorner - Res_HAIRMOD_PROTECTED.monsterTemplates[monster_type].xCenter, yCorner - 20, 0);
				g.drawRegion(Res_HAIRMOD_PROTECTED.imgNguHanh, nh[he] * 16, 0, 16, 16, 0, xCorner - 15, yCorner + 3, MyGraphics_HAIRMOD_PROTECTED.TOP | MyGraphics_HAIRMOD_PROTECTED.LEFT);
			}
		}
		catch (Exception)
		{
			Out_HAIRMOD_PROTECTED.println(" loi tai paintCorner monter");
		}
	}

	public override void update()
	{
		updateDynamicBuff();
		updateExp();
		switch (state)
		{
		case 4:
			frame = 0;
			break;
		case 0:
			if (typeID == 0)
			{
				frame = 0;
				listTarget.removeAllElements();
			}
			else if (typeID == 1)
			{
				if (isChopSang)
				{
					lap++;
					if (lap > STAND_FRAME.Length - 1)
					{
						lap = 0;
					}
					frame = STAND_FRAME[lap];
				}
				else
				{
					frame = 0;
				}
			}
			else
			{
				frame = 0;
			}
			break;
		case 2:
			frame = 0;
			break;
		case 3:
			if (typeID == 0)
			{
				p1++;
				if (p1 == ATT_FRAME.Length - 5)
				{
					EffectManager_HAIRMOD_PROTECTED.addHiEffect(x - 3, y - 30, 24);
					for (int i = 0; i < 10; i++)
					{
						Skill_TruTranThanh_HAIRMOD_PROTECTED o = new Skill_TruTranThanh_HAIRMOD_PROTECTED(x - 5, y - 70, this, listTarget);
						EffectManager_HAIRMOD_PROTECTED.hiEffects.addElement(o);
					}
				}
				if (p1 >= ATT_FRAME.Length - 3 && p1 <= ATT_FRAME.Length - 1)
				{
					EffectManager_HAIRMOD_PROTECTED.addHiEffect(x - 5, y - 65, 3);
				}
				if (p1 == ATT_FRAME.Length - 1 && listTarget.size() > 0)
				{
					for (int j = 0; j < listTarget.size(); j++)
					{
						LiveActor_HAIRMOD_PROTECTED liveActor_HAIRMOD_PROTECTED = (LiveActor_HAIRMOD_PROTECTED)listTarget.elementAt(j);
						Canvas_HAIRMOD_PROTECTED.gameScr.startWeaponsLazer(this, liveActor_HAIRMOD_PROTECTED, 0, -65, 0, liveActor_HAIRMOD_PROTECTED.damAttack, 2);
					}
				}
				if (p1 > ATT_FRAME.Length - 1)
				{
					p1 = 0;
					state = 0;
				}
				frame = ATT_FRAME[p1];
			}
			else
			{
				frame = 0;
			}
			break;
		case 5:
			frame = 0;
			createExp();
			break;
		}
		updateEffSkill();
	}

	public override void jump()
	{
	}

	public override void jumpVang(Actor_HAIRMOD_PROTECTED causeBy)
	{
		base.jumpVang(causeBy);
	}

	private void paintExp(MyGraphics_HAIRMOD_PROTECTED g)
	{
		int num = exp.size();
		for (int i = 0; i < num; i++)
		{
			Explode_HAIRMOD_PROTECTED explode_HAIRMOD_PROTECTED = (Explode_HAIRMOD_PROTECTED)exp.elementAt(i);
			explode_HAIRMOD_PROTECTED.paint(g);
		}
	}

	private void updateExp()
	{
		for (int i = 0; i < exp.size(); i++)
		{
			Explode_HAIRMOD_PROTECTED explode_HAIRMOD_PROTECTED = (Explode_HAIRMOD_PROTECTED)exp.elementAt(i);
			explode_HAIRMOD_PROTECTED.update();
			if (explode_HAIRMOD_PROTECTED.destroy)
			{
				exp.removeElement(explode_HAIRMOD_PROTECTED);
				if (exp.size() == 0)
				{
					wantDestroy = true;
				}
			}
		}
	}

	public override void startDeadFly(int dx, int dy)
	{
		state = 5;
	}

	private void createExp()
	{
		if (totalExp < 15)
		{
			totalExp++;
			MyRandom_HAIRMOD_PROTECTED myRandom_HAIRMOD_PROTECTED = new MyRandom_HAIRMOD_PROTECTED();
			exp.addElement(new Explode_HAIRMOD_PROTECTED(x - 12 + GameScr_HAIRMOD_PROTECTED.abs(myRandom_HAIRMOD_PROTECTED.nextInt() % 25), y - 30 + GameScr_HAIRMOD_PROTECTED.abs(myRandom_HAIRMOD_PROTECTED.nextInt() % 35), 0));
		}
	}

	public override void startAttack(mVector_HAIRMOD_PROTECTED list, sbyte skillID)
	{
		state = 3;
		listTarget = list;
		p1 = (p2 = (p3 = 0));
	}

	public override void setInfo(MonsterInfo_HAIRMOD_PROTECTED monsterInfo)
	{
		base.setInfo(monsterInfo);
	}

	public override bool allwayPaint()
	{
		return true;
	}
}
