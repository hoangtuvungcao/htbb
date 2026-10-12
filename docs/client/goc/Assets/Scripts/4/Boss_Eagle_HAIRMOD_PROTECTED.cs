public class Boss_Eagle_HAIRMOD_PROTECTED : Monster_HAIRMOD_PROTECTED
{
	private mVector_HAIRMOD_PROTECTED exp = new mVector_HAIRMOD_PROTECTED();

	private mVector_HAIRMOD_PROTECTED listAttack = new mVector_HAIRMOD_PROTECTED();

	private sbyte[] FRAME = new sbyte[3] { 4, 6, 5 };

	private new sbyte frame;

	private sbyte transChan;

	private sbyte f;

	private sbyte maxCount = 10;

	private sbyte totalExp;

	private sbyte skillID;

	private long timeGetImage = mSystem_HAIRMOD_PROTECTED.getCurrentTimeMillis();

	public override void paint(MyGraphics_HAIRMOD_PROTECTED g)
	{
		if (!isFreeze)
		{
			if (Res_HAIRMOD_PROTECTED.monsterTemplates[monster_type].imageObj != null)
			{
				g.drawImage(Res_HAIRMOD_PROTECTED.imgShadow, x, y + 15, 3);
				if (dynamicEffBottom != null)
				{
					for (int i = 0; i < dynamicEffBottom.size(); i++)
					{
						((DynamicEffect_HAIRMOD_PROTECTED)dynamicEffBottom.elementAt(i)).paint(g, x, y);
					}
				}
				int num = frame / 5;
				int num2 = y - num * 3;
				if (num > 2)
				{
					num = 2;
				}
				Res_HAIRMOD_PROTECTED.monsterTemplates[monster_type].imageObj.paint(g, x, num2, 0, 0, 1, 1);
				Res_HAIRMOD_PROTECTED.monsterTemplates[monster_type].imageObj.paint(g, x, num2, f + 1, transChan, 1, 1);
				Res_HAIRMOD_PROTECTED.monsterTemplates[monster_type].imageObj.paint(g, x, num2, FRAME[num], 0, 1, 1);
				Res_HAIRMOD_PROTECTED.monsterTemplates[monster_type].imageObj.paint(g, x, num2, FRAME[num], 2, -1, 1);
				if (state == 3)
				{
					Res_HAIRMOD_PROTECTED.monsterTemplates[monster_type].imageObj.paint(g, x, num2, 3, 0, 1, 1);
				}
				if (dynamicEffTop != null)
				{
					for (int j = 0; j < dynamicEffTop.size(); j++)
					{
						((DynamicEffect_HAIRMOD_PROTECTED)dynamicEffTop.elementAt(j)).paint(g, x, y);
					}
				}
			}
			else if (mSystem_HAIRMOD_PROTECTED.getCurrentTimeMillis() > timeGetImage)
			{
				timeGetImage = mSystem_HAIRMOD_PROTECTED.getCurrentTimeMillis() + 10000;
				Res_HAIRMOD_PROTECTED.monsterTemplates[monster_type].isLoad = false;
				Res_HAIRMOD_PROTECTED.monsterTemplates[monster_type].loadImage();
			}
			paintExp(g);
		}
		paintEffSkill(g);
	}

	public override void jump()
	{
	}

	private void changeChan(int index)
	{
		if (z > 20)
		{
			f = (sbyte)index;
			z = 0;
		}
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
		updateDynamicBuff();
		z++;
		frame++;
		if (frame >= maxCount)
		{
			frame = 0;
		}
		transChan = 0;
		updateExp();
		switch (state)
		{
		case 0:
			maxCount = 10;
			changeChan(1);
			break;
		case 3:
			maxCount = 20;
			changeChan(0);
			p1++;
			if (frame / 5 <= 2 || p1 <= 20)
			{
				break;
			}
			p1 = 0;
			state = 2;
			if (skillID == 1)
			{
				sbyte[] array = new sbyte[2] { -1, 1 };
				for (int i = 0; i < 30; i++)
				{
					ArrowTarget_HAIRMOD_PROTECTED arrowTarget_HAIRMOD_PROTECTED = new ArrowTarget_HAIRMOD_PROTECTED(6);
					int num = Res_HAIRMOD_PROTECTED.rnd(2);
					arrowTarget_HAIRMOD_PROTECTED.set(0, x + array[num] * 25, y - frame / 5 * 3, 0, 0, x + Res_HAIRMOD_PROTECTED.rnd(200) * array[num] - 10 * array[num], y + 20 + Res_HAIRMOD_PROTECTED.rnd(150), (sbyte)(Res_HAIRMOD_PROTECTED.rnd(10) + 15), false);
					GameScr_HAIRMOD_PROTECTED.arrowsUp.addElement(arrowTarget_HAIRMOD_PROTECTED);
				}
				for (int j = 0; j < listAttack.size(); j++)
				{
					LiveActor_HAIRMOD_PROTECTED liveActor_HAIRMOD_PROTECTED = (LiveActor_HAIRMOD_PROTECTED)listAttack.elementAt(j);
					ArrowTarget_HAIRMOD_PROTECTED arrowTarget_HAIRMOD_PROTECTED2 = new ArrowTarget_HAIRMOD_PROTECTED(6);
					int num2 = Res_HAIRMOD_PROTECTED.rnd(2);
					arrowTarget_HAIRMOD_PROTECTED2.set(0, x + array[num2] * 25, y - frame / 5 * 3, liveActor_HAIRMOD_PROTECTED.damAttack, 0, liveActor_HAIRMOD_PROTECTED.x, liveActor_HAIRMOD_PROTECTED.y, 20, true);
					GameScr_HAIRMOD_PROTECTED.arrowsUp.addElement(arrowTarget_HAIRMOD_PROTECTED2);
				}
			}
			else
			{
				for (int k = 0; k < listAttack.size(); k++)
				{
					LiveActor_HAIRMOD_PROTECTED liveActor_HAIRMOD_PROTECTED2 = (LiveActor_HAIRMOD_PROTECTED)listAttack.elementAt(k);
					ThienThach_Skill_HAIRMOD_PROTECTED o = new ThienThach_Skill_HAIRMOD_PROTECTED(liveActor_HAIRMOD_PROTECTED2.x, liveActor_HAIRMOD_PROTECTED2.y, liveActor_HAIRMOD_PROTECTED2.damAttack);
					EffectManager_HAIRMOD_PROTECTED.hiEffects.addElement(o);
				}
			}
			break;
		case 5:
			changeChan(1);
			createExp();
			break;
		case 2:
			maxCount = 10;
			changeChan(0);
			if (frame / 5 == 1 && f == 0)
			{
				transChan = 2;
			}
			move();
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
		Res_HAIRMOD_PROTECTED.monsterTemplates[monster_type].w = 50;
		timeGetImage = mSystem_HAIRMOD_PROTECTED.getCurrentTimeMillis() + 10000;
		height = 100;
	}
}
