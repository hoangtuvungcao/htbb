using System;

public class Boss_Snake_HAIRMOD_PROTECTED : Monster_HAIRMOD_PROTECTED
{
	private mVector_HAIRMOD_PROTECTED exp = new mVector_HAIRMOD_PROTECTED();

	private mVector_HAIRMOD_PROTECTED listAttack = new mVector_HAIRMOD_PROTECTED();

	private sbyte[][] MOVE_FRAME = new sbyte[4][]
	{
		new sbyte[8] { 1, 1, 1, 1, 2, 2, 2, 2 },
		new sbyte[8] { 5, 5, 5, 5, 6, 6, 6, 6 },
		new sbyte[8] { 1, 1, 1, 1, 2, 2, 2, 2 },
		new sbyte[8] { 1, 1, 1, 1, 2, 2, 2, 2 }
	};

	private sbyte[][] ATT_FRAME = new sbyte[4][]
	{
		new sbyte[6] { 3, 3, 3, 3, 3, 3 },
		new sbyte[6] { 7, 7, 7, 7, 7, 7 },
		new sbyte[6] { 3, 3, 3, 3, 3, 3 },
		new sbyte[6] { 3, 3, 3, 3, 3, 3 }
	};

	private sbyte[][] STAND_FRAME = new sbyte[5][]
	{
		new sbyte[10] { 0, 0, 0, 0, 0, 1, 1, 1, 1, 1 },
		new sbyte[10] { 5, 5, 5, 5, 5, 6, 6, 6, 6, 6 },
		new sbyte[10] { 0, 0, 0, 0, 0, 1, 1, 1, 1, 1 },
		new sbyte[10] { 0, 0, 0, 0, 0, 1, 1, 1, 1, 1 },
		new sbyte[10] { 0, 0, 0, 0, 0, 1, 1, 1, 1, 1 }
	};

	private sbyte totalExp;

	private sbyte skillID;

	public override void paint(MyGraphics_HAIRMOD_PROTECTED g)
	{
		if (!isFreeze)
		{
			if (Res_HAIRMOD_PROTECTED.monsterTemplates[monster_type] != null && Res_HAIRMOD_PROTECTED.monsterTemplates[monster_type].image != null)
			{
				g.drawImage(Res_HAIRMOD_PROTECTED.imgShadow, x, y - 5, 3);
				if (dynamicEffBottom != null)
				{
					for (int i = 0; i < dynamicEffBottom.size(); i++)
					{
						((DynamicEffect_HAIRMOD_PROTECTED)dynamicEffBottom.elementAt(i)).paint(g, x, y);
					}
				}
				g.drawRegion(Res_HAIRMOD_PROTECTED.monsterTemplates[monster_type].image, 0, frame * Res_HAIRMOD_PROTECTED.monsterTemplates[monster_type].h, Res_HAIRMOD_PROTECTED.monsterTemplates[monster_type].w, Res_HAIRMOD_PROTECTED.monsterTemplates[monster_type].h, (dir == 3) ? 2 : 0, x, y, 33);
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
				g.drawRegion(Res_HAIRMOD_PROTECTED.monsterTemplates[monster_type].image, 0, 0, Res_HAIRMOD_PROTECTED.monsterTemplates[monster_type].w, Res_HAIRMOD_PROTECTED.monsterTemplates[monster_type].h / 2, 0, xCorner - Res_HAIRMOD_PROTECTED.monsterTemplates[monster_type].xCenter, yCorner - 20, 0);
				g.drawRegion(Res_HAIRMOD_PROTECTED.imgNguHanh, nh[he] * 16, 0, 16, 16, 0, xCorner - 15, yCorner + 3, MyGraphics_HAIRMOD_PROTECTED.TOP | MyGraphics_HAIRMOD_PROTECTED.LEFT);
			}
		}
		catch (Exception)
		{
		}
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
			((Explode_HAIRMOD_PROTECTED)exp.elementAt(i))?.paint(g);
		}
	}

	private void updateExp()
	{
		for (int i = 0; i < exp.size(); i++)
		{
			Explode_HAIRMOD_PROTECTED explode_HAIRMOD_PROTECTED = (Explode_HAIRMOD_PROTECTED)exp.elementAt(i);
			if (explode_HAIRMOD_PROTECTED == null)
			{
				continue;
			}
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

	public override void update()
	{
		updateExp();
		updateDynamicBuff();
		switch (state)
		{
		case 4:
			if (dir != 1)
			{
				frame = 3;
			}
			else
			{
				frame = 7;
			}
			break;
		case 0:
			p1++;
			if (p1 > STAND_FRAME[dir].Length - 1)
			{
				p1 = 0;
			}
			frame = STAND_FRAME[dir][p1];
			break;
		case 3:
			p1++;
			if (p1 % 3 == 0)
			{
				EffectManager_HAIRMOD_PROTECTED.addLowEffect(x - 20, y - 14, 30);
				EffectManager_HAIRMOD_PROTECTED.addLowEffect(x + 20, y - 14, 30);
				EffectManager_HAIRMOD_PROTECTED.addLowEffect(x - 20, y - 4, 30);
				EffectManager_HAIRMOD_PROTECTED.addLowEffect(x + 20, y - 4, 30);
				EffectManager_HAIRMOD_PROTECTED.addLowEffect(x, y + 4, 30);
			}
			if (p1 > ATT_FRAME[dir].Length - 1)
			{
				p1 = 0;
				if (skillID == 0)
				{
					for (int i = 0; i < listAttack.size(); i++)
					{
						LiveActor_HAIRMOD_PROTECTED acTo = (LiveActor_HAIRMOD_PROTECTED)listAttack.elementAt(i);
						Canvas_HAIRMOD_PROTECTED.gameScr.SkillofBoss_Snake(this, acTo, 4, 52);
					}
				}
				else if (skillID != 1)
				{
					for (int j = 0; j < listAttack.size(); j++)
					{
						LiveActor_HAIRMOD_PROTECTED liveActor_HAIRMOD_PROTECTED = (LiveActor_HAIRMOD_PROTECTED)listAttack.elementAt(j);
						if (liveActor_HAIRMOD_PROTECTED != null)
						{
							int num = liveActor_HAIRMOD_PROTECTED.damAttack;
							liveActor_HAIRMOD_PROTECTED.doublejump();
							Canvas_HAIRMOD_PROTECTED.gameScr.startFlyText("-" + num, 0, liveActor_HAIRMOD_PROTECTED.x, liveActor_HAIRMOD_PROTECTED.y + liveActor_HAIRMOD_PROTECTED.dy - 15, 1, -2);
							for (int k = 0; k < 15; k++)
							{
								EffectManager_HAIRMOD_PROTECTED.addHiEffect(liveActor_HAIRMOD_PROTECTED.x - 12 + GameScr_HAIRMOD_PROTECTED.abs(LiveActor_HAIRMOD_PROTECTED.r.nextInt() % 25), liveActor_HAIRMOD_PROTECTED.y - 30 + GameScr_HAIRMOD_PROTECTED.abs(LiveActor_HAIRMOD_PROTECTED.r.nextInt() % 35), 33);
							}
						}
					}
				}
				state = 2;
			}
			frame = ATT_FRAME[dir][p1];
			break;
		case 5:
			frame = 3;
			createExp();
			break;
		case 2:
			p1++;
			if (p1 > MOVE_FRAME[dir].Length - 1)
			{
				p1 = 0;
			}
			frame = MOVE_FRAME[dir][p1];
			if (!isFreeze)
			{
				move();
			}
			break;
		}
		updateEffSkill();
	}

	public override void startAttack(mVector_HAIRMOD_PROTECTED list, sbyte skillID)
	{
		listAttack = list;
		state = 3;
		p1 = (p2 = (p3 = 0));
		this.skillID = skillID;
	}

	public override void setInfo(MonsterInfo_HAIRMOD_PROTECTED monsterInfo)
	{
		base.setInfo(monsterInfo);
	}
}
