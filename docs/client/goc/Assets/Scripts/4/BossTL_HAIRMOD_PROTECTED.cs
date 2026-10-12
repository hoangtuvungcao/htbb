public class BossTL_HAIRMOD_PROTECTED : Monster_HAIRMOD_PROTECTED
{
	private mVector_HAIRMOD_PROTECTED exp = new mVector_HAIRMOD_PROTECTED();

	private static int[] trans = new int[2] { 0, 2 };

	private byte totalExp;

	public override void paint(MyGraphics_HAIRMOD_PROTECTED g)
	{
		if (z != 0)
		{
			frame++;
			if (frame > 1)
			{
				frame = 0;
			}
		}
		if (Res_HAIRMOD_PROTECTED.monsterTemplates[monster_type] != null)
		{
			int num = Monster_HAIRMOD_PROTECTED.REAL_FRAME[Res_HAIRMOD_PROTECTED.monsterTemplates[monster_type].moveType][dir][frame];
			g.drawImage(Res_HAIRMOD_PROTECTED.imgShadow, x, y, 3);
			if (dynamicEffBottom != null)
			{
				for (int i = 0; i < dynamicEffBottom.size(); i++)
				{
					((DynamicEffect_HAIRMOD_PROTECTED)dynamicEffBottom.elementAt(i)).paint(g, x, y);
				}
			}
			g.drawRegion(Res_HAIRMOD_PROTECTED.monsterTemplates[monster_type].image, 0, num % 4 * Res_HAIRMOD_PROTECTED.monsterTemplates[monster_type].h, Res_HAIRMOD_PROTECTED.monsterTemplates[monster_type].w, Res_HAIRMOD_PROTECTED.monsterTemplates[monster_type].h, trans[num / 4], x - Res_HAIRMOD_PROTECTED.monsterTemplates[monster_type].xCenter + vangx, y - Res_HAIRMOD_PROTECTED.monsterTemplates[monster_type].yCenter + z + vangy, 0);
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
		if (z < 0)
		{
			z += dz;
			dz++;
			if (z > 0)
			{
				z = 0;
			}
		}
		vangx += dvangx;
		vangy += dvangy;
		if (dvangx > 0)
		{
			dvangx--;
		}
		if (dvangx < 0)
		{
			dvangx++;
		}
		if (dvangy > 0)
		{
			dvangy--;
		}
		if (dvangy < 0)
		{
			dvangy++;
		}
		if (dvangx == 0 && dvangy == 0)
		{
			if (vangx > 1)
			{
				vangx >>= 1;
			}
			else
			{
				vangx = 0;
			}
			if (vangy > 1)
			{
				vangy >>= 1;
			}
			else
			{
				vangy = 0;
			}
		}
		if (realHPSyncTime > 0)
		{
			realHPSyncTime--;
			if (realHPSyncTime == 0)
			{
				if (realHP < 0)
				{
					realHP = 0;
				}
				if (hp > realHP || realHP == 0)
				{
					hp = realHP;
				}
				if (hp == 0)
				{
					state = 4;
				}
			}
		}
		switch (state)
		{
		case 4:
			break;
		case 0:
			frame = 0;
			break;
		case 3:
			frame = 2;
			p1++;
			if (p1 % 6 == 3)
			{
				Skill_Tia_Thuy_HAIRMOD_PROTECTED skill_Tia_Thuy_HAIRMOD_PROTECTED = null;
				if (targethplost != 0)
				{
					Canvas_HAIRMOD_PROTECTED.gameScr.startFlyText("-" + targethplost, 0, attackTarget.x, attackTarget.y + attackTarget.dy - 15, 1, -2);
				}
				else
				{
					Canvas_HAIRMOD_PROTECTED.gameScr.startFlyText("MISS", 0, attackTarget.x, attackTarget.y + attackTarget.dy - 15, 1, -2);
				}
				skill_Tia_Thuy_HAIRMOD_PROTECTED = ((Monster_HAIRMOD_PROTECTED.REAL_FRAME[Res_HAIRMOD_PROTECTED.monsterTemplates[monster_type].moveType][dir][frame] >= 4) ? new Skill_Tia_Thuy_HAIRMOD_PROTECTED(attackTarget, x + 20, y - 30) : new Skill_Tia_Thuy_HAIRMOD_PROTECTED(attackTarget, x - 20, y - 30));
				EffectManager_HAIRMOD_PROTECTED.hiEffects.addElement(skill_Tia_Thuy_HAIRMOD_PROTECTED);
			}
			if (p1 > 10)
			{
				p1 = 0;
				state = 2;
			}
			break;
		case 5:
			frame = 3;
			p1++;
			x += p2;
			y += p3;
			p2 >>= 1;
			p3 >>= 1;
			createExp();
			break;
		case 2:
			p1++;
			if (p1 > 6)
			{
				p1 = 0;
			}
			if (p1 > 2)
			{
				frame = 1;
			}
			else
			{
				frame = 0;
			}
			move();
			break;
		case 1:
			break;
		}
	}

	public void startAttack(LiveActor_HAIRMOD_PROTECTED live, int dam, byte skillID)
	{
		state = 3;
		p1 = (p2 = (p3 = 0));
		attackTarget = live;
		targethplost = dam;
	}
}
