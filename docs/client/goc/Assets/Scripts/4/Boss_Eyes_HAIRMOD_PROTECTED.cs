using System;

public class Boss_Eyes_HAIRMOD_PROTECTED : Monster_HAIRMOD_PROTECTED
{
	private mVector_HAIRMOD_PROTECTED exp = new mVector_HAIRMOD_PROTECTED();

	private mVector_HAIRMOD_PROTECTED listAttack = new mVector_HAIRMOD_PROTECTED();

	private sbyte[][] MOVE_FRAME = new sbyte[4][]
	{
		new sbyte[6] { 0, 0, 0, 1, 1, 1 },
		new sbyte[6] { 4, 4, 4, 5, 5, 5 },
		new sbyte[6] { 0, 0, 0, 1, 1, 1 },
		new sbyte[6] { 0, 0, 0, 1, 1, 1 }
	};

	private sbyte[][] ATT_FRAME = new sbyte[4][]
	{
		new sbyte[6] { 2, 2, 2, 2, 2, 2 },
		new sbyte[6] { 6, 6, 6, 6, 6, 6 },
		new sbyte[6] { 2, 2, 2, 2, 2, 2 },
		new sbyte[6] { 2, 2, 2, 2, 2, 2 }
	};

	private sbyte totalExp;

	private sbyte skillID;

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
				g.drawRegion(Res_HAIRMOD_PROTECTED.monsterTemplates[monster_type].image, 0, frame * Res_HAIRMOD_PROTECTED.monsterTemplates[monster_type].h, Res_HAIRMOD_PROTECTED.monsterTemplates[monster_type].w, Res_HAIRMOD_PROTECTED.monsterTemplates[monster_type].h, 0, xCorner - Res_HAIRMOD_PROTECTED.monsterTemplates[monster_type].xCenter, yCorner - 20, 0);
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
			if (dir != 1)
			{
				frame = 0;
			}
			else
			{
				frame = 4;
			}
			break;
		case 3:
			p1++;
			if (p1 % 3 == 0)
			{
				EffectManager_HAIRMOD_PROTECTED.addLowEffect(x - 20, y - 14, 4);
				EffectManager_HAIRMOD_PROTECTED.addLowEffect(x + 20, y - 14, 4);
				EffectManager_HAIRMOD_PROTECTED.addLowEffect(x - 20, y - 4, 4);
				EffectManager_HAIRMOD_PROTECTED.addLowEffect(x + 20, y - 4, 4);
				EffectManager_HAIRMOD_PROTECTED.addLowEffect(x, y + 4, 4);
			}
			if (skillID == 0 && p1 == 1)
			{
				int num = 0;
				int num2 = 0;
				if (dir == 3)
				{
					num2 = -22;
					num = 10;
				}
				else
				{
					num2 = -22;
					num = -10;
				}
				if (dir != 1)
				{
					EffectManager_HAIRMOD_PROTECTED.addHiEffect(x + num, y + num2, 37);
				}
			}
			if (p1 > ATT_FRAME[dir].Length - 1)
			{
				if (skillID == 0)
				{
					for (int i = 0; i < listAttack.size(); i++)
					{
						LiveActor_HAIRMOD_PROTECTED liveActor_HAIRMOD_PROTECTED = (LiveActor_HAIRMOD_PROTECTED)listAttack.elementAt(i);
						int num3 = 0;
						int num4 = 0;
						if (dir == 3)
						{
							num4 = -22;
							num3 = 10;
						}
						else
						{
							num4 = -22;
							num3 = -10;
						}
						EffectManager_HAIRMOD_PROTECTED.addHiEffect(x + num3, y + num4, 11);
						Canvas_HAIRMOD_PROTECTED.gameScr.startWeaponsLazer(this, liveActor_HAIRMOD_PROTECTED, num3, num4, dir, liveActor_HAIRMOD_PROTECTED.damAttack, 0);
					}
				}
				else if (skillID == 1)
				{
					for (int j = 0; j < listAttack.size(); j++)
					{
						LiveActor_HAIRMOD_PROTECTED acTo = (LiveActor_HAIRMOD_PROTECTED)listAttack.elementAt(j);
						Canvas_HAIRMOD_PROTECTED.gameScr.startWeapsMango(this, acTo, 30);
					}
				}
				else
				{
					for (int k = 0; k < listAttack.size(); k++)
					{
						LiveActor_HAIRMOD_PROTECTED liveActor_HAIRMOD_PROTECTED2 = (LiveActor_HAIRMOD_PROTECTED)listAttack.elementAt(k);
						if (liveActor_HAIRMOD_PROTECTED2 != null)
						{
							int num5 = liveActor_HAIRMOD_PROTECTED2.damAttack;
							liveActor_HAIRMOD_PROTECTED2.doublejump();
							Canvas_HAIRMOD_PROTECTED.gameScr.startFlyText("-" + num5, 0, liveActor_HAIRMOD_PROTECTED2.x, liveActor_HAIRMOD_PROTECTED2.y + liveActor_HAIRMOD_PROTECTED2.dy - 15, 1, -2);
							for (int l = 0; l < 10; l++)
							{
								EffectManager_HAIRMOD_PROTECTED.addHiEffect(liveActor_HAIRMOD_PROTECTED2.x - 12 + GameScr_HAIRMOD_PROTECTED.abs(LiveActor_HAIRMOD_PROTECTED.r.nextInt() % 25), liveActor_HAIRMOD_PROTECTED2.y - 30 + GameScr_HAIRMOD_PROTECTED.abs(LiveActor_HAIRMOD_PROTECTED.r.nextInt() % 35), 28);
							}
						}
					}
				}
				p1 = 0;
				state = 2;
			}
			frame = ATT_FRAME[dir][p1];
			break;
		case 5:
			frame = 0;
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
