using System;

public class Boss_TuongQuan_HAIRMOD_PROTECTED : Monster_HAIRMOD_PROTECTED
{
	private mVector_HAIRMOD_PROTECTED exp = new mVector_HAIRMOD_PROTECTED();

	private mVector_HAIRMOD_PROTECTED listAttack = new mVector_HAIRMOD_PROTECTED();

	private sbyte[][] MOVE_FRAME = new sbyte[4][]
	{
		new sbyte[8] { 11, 11, 11, 11, 12, 12, 12, 12 },
		new sbyte[8] { 19, 19, 19, 19, 20, 20, 20, 20 },
		new sbyte[8] { 3, 3, 3, 3, 4, 4, 4, 4 },
		new sbyte[8] { 3, 3, 3, 3, 4, 4, 4, 4 }
	};

	private sbyte[][][] ATT_FRAME = new sbyte[2][][]
	{
		new sbyte[4][]
		{
			new sbyte[16]
			{
				13, 13, 13, 13, 13, 13, 13, 13, 14, 14,
				15, 15, 15, 15, 15, 15
			},
			new sbyte[16]
			{
				21, 21, 21, 21, 21, 21, 21, 21, 22, 22,
				23, 23, 23, 23, 23, 23
			},
			new sbyte[16]
			{
				5, 5, 5, 5, 5, 5, 5, 5, 6, 6,
				7, 7, 7, 7, 7, 7
			},
			new sbyte[16]
			{
				5, 5, 5, 5, 5, 5, 5, 5, 6, 6,
				7, 7, 7, 7, 7, 7
			}
		},
		new sbyte[4][]
		{
			new sbyte[16]
			{
				13, 13, 13, 13, 13, 13, 13, 13, 14, 14,
				15, 15, 15, 15, 15, 15
			},
			new sbyte[16]
			{
				21, 21, 21, 21, 21, 21, 21, 21, 22, 22,
				23, 23, 23, 23, 23, 23
			},
			new sbyte[16]
			{
				5, 5, 5, 5, 5, 5, 5, 5, 6, 6,
				7, 7, 7, 7, 7, 7
			},
			new sbyte[16]
			{
				5, 5, 5, 5, 5, 5, 5, 5, 6, 6,
				7, 7, 7, 7, 7, 7
			}
		}
	};

	private sbyte[][] INJURE_FRAME = new sbyte[4][]
	{
		new sbyte[3] { 10, 10, 10 },
		new sbyte[3] { 18, 18, 18 },
		new sbyte[3] { 2, 2, 2 },
		new sbyte[3] { 2, 2, 2 }
	};

	private sbyte[][] STAND_FRAME = new sbyte[4][]
	{
		new sbyte[10] { 8, 8, 8, 8, 8, 9, 9, 9, 9, 9 },
		new sbyte[10] { 16, 16, 16, 16, 16, 17, 17, 17, 17, 17 },
		new sbyte[10] { 0, 0, 0, 0, 0, 1, 1, 1, 1, 1 },
		new sbyte[10] { 0, 0, 0, 0, 0, 1, 1, 1, 1, 1 }
	};

	private sbyte totalExp;

	private sbyte skillID;

	public override void paint(MyGraphics_HAIRMOD_PROTECTED g)
	{
		if (!isFreeze)
		{
			if (Res_HAIRMOD_PROTECTED.monsterTemplates[monster_type] != null && Res_HAIRMOD_PROTECTED.monsterTemplates[monster_type].Arrimage != null && Res_HAIRMOD_PROTECTED.monsterTemplates[monster_type].isMaxHight)
			{
				if (dynamicEffBottom != null)
				{
					for (int i = 0; i < dynamicEffBottom.size(); i++)
					{
						((DynamicEffect_HAIRMOD_PROTECTED)dynamicEffBottom.elementAt(i)).paint(g, x, y);
					}
				}
				g.drawRegion(Res_HAIRMOD_PROTECTED.monsterTemplates[monster_type].Arrimage[frame], 0, 0, Res_HAIRMOD_PROTECTED.monsterTemplates[monster_type].w, Res_HAIRMOD_PROTECTED.monsterTemplates[monster_type].h, (dir == 3) ? 2 : 0, x, y - 20, 3);
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
			if (Res_HAIRMOD_PROTECTED.monsterTemplates[monster_type] != null && Res_HAIRMOD_PROTECTED.monsterTemplates[monster_type].Arrimage != null && Res_HAIRMOD_PROTECTED.monsterTemplates[monster_type].isMaxHight)
			{
				g.drawRegion(Res_HAIRMOD_PROTECTED.monsterTemplates[monster_type].Arrimage[0], 0, 0, Res_HAIRMOD_PROTECTED.monsterTemplates[monster_type].w, 40, 0, xCorner - Res_HAIRMOD_PROTECTED.monsterTemplates[monster_type].xCenter, yCorner - 30, 0);
				g.drawRegion(Res_HAIRMOD_PROTECTED.imgNguHanh, nh[he] * 16, 0, 16, 16, 0, xCorner - 15, yCorner + 3, MyGraphics_HAIRMOD_PROTECTED.TOP | MyGraphics_HAIRMOD_PROTECTED.LEFT);
			}
		}
		catch (Exception)
		{
			Out_HAIRMOD_PROTECTED.println(" loi tai paaintCorner monter");
		}
	}

	public override void update()
	{
		updateExp();
		updateDynamicBuff();
		switch (state)
		{
		case 4:
			p1++;
			if (p1 > INJURE_FRAME[dir].Length - 1)
			{
				p1 = 0;
			}
			frame = INJURE_FRAME[dir][p1];
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
			if (p1 == ATT_FRAME[(skillID != 2) ? skillID : 0][dir].Length - 7)
			{
				if (skillID == 0)
				{
					for (int i = 0; i < listAttack.size(); i++)
					{
						LiveActor_HAIRMOD_PROTECTED liveActor_HAIRMOD_PROTECTED = (LiveActor_HAIRMOD_PROTECTED)listAttack.elementAt(i);
						Canvas_HAIRMOD_PROTECTED.gameScr.startNewArrow(7, this, liveActor_HAIRMOD_PROTECTED, x - 20, y - 50, attackDam(liveActor_HAIRMOD_PROTECTED), 2, 8);
						Canvas_HAIRMOD_PROTECTED.gameScr.startNewArrow(7, this, liveActor_HAIRMOD_PROTECTED, x, y - 30, attackDam(liveActor_HAIRMOD_PROTECTED), 2, 8);
						Canvas_HAIRMOD_PROTECTED.gameScr.startNewArrow(7, this, liveActor_HAIRMOD_PROTECTED, x - 20, y - 10, attackDam(liveActor_HAIRMOD_PROTECTED), 2, 8);
					}
				}
				else if (skillID == 1)
				{
					Canvas_HAIRMOD_PROTECTED.gameScr.timerung = 20;
					EffectManager_HAIRMOD_PROTECTED.lowEffects.addElement(new Skill_Bua_4_HAIRMOD_PROTECTED(attackTarget, x, y, true));
					if (attackDam(attackTarget) != 0 && attackDam(attackTarget) != 2000000)
					{
						Canvas_HAIRMOD_PROTECTED.gameScr.startFlyText("-" + attackDam(attackTarget), 0, attackTarget.x, attackTarget.y - 15, -1, -2);
					}
				}
			}
			if (p1 > ATT_FRAME[(skillID != 2) ? skillID : 0][dir].Length - 1)
			{
				p1 = 0;
				state = 2;
			}
			else
			{
				frame = ATT_FRAME[(skillID != 2) ? skillID : 0][dir][p1];
			}
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

	public override void startAttack(mVector_HAIRMOD_PROTECTED list, sbyte skillID)
	{
		listAttack = list;
		state = 3;
		p1 = (p2 = (p3 = 0));
		this.skillID = skillID;
		if (skillID == 1)
		{
			attackTarget = (LiveActor_HAIRMOD_PROTECTED)list.elementAt(0);
		}
	}

	public override void setInfo(MonsterInfo_HAIRMOD_PROTECTED monsterInfo)
	{
		base.setInfo(monsterInfo);
	}
}
