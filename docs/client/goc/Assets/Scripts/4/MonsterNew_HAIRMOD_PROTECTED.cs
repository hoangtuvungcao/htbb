public class MonsterNew_HAIRMOD_PROTECTED : Monster_HAIRMOD_PROTECTED
{
	public const sbyte SKILL_BOSS_NOEL = 3;

	public const sbyte SKILL_LOC_XOAY = 4;

	public const sbyte SKILL_BLOOD_DOW = 5;

	public const sbyte SKILL_BAT = 6;

	public const sbyte SKILL_BABURAN = 7;

	public const sbyte SKILL_BI_NGO = 8;

	public const sbyte SKILL_THIEN_THACH = 9;

	public const sbyte SKILL_LUOI_KIEM = 10;

	public const sbyte SKILL_WEAPON_MAN_GO = 11;

	private Image_HAIRMOD_PROTECTED[] img = new Image_HAIRMOD_PROTECTED[2];

	private sbyte way;

	public mVector_HAIRMOD_PROTECTED data = new mVector_HAIRMOD_PROTECTED();

	private mVector_HAIRMOD_PROTECTED listAttack = new mVector_HAIRMOD_PROTECTED();

	private int IdvanTieu;

	private sbyte huongY;

	private sbyte flip;

	private sbyte[] splashDuaX = new sbyte[4] { -2, 2, -8, 8 };

	private sbyte[] splashDuaY = new sbyte[4] { -10, -30, -10, -10 };

	private sbyte[] x2 = new sbyte[9] { 0, 20, 20, 0, 0, 20, 20, 20, 20 };

	private sbyte[] y2 = new sbyte[9] { 0, 0, 0, 20, 20, 20, 20, 20, 20 };

	private sbyte skillID;

	private short idSkill = -1;

	private sbyte bymap;

	public bool isNewMonster;

	public MonsterNew_HAIRMOD_PROTECTED(int idTemplate)
	{
		isDied = false;
		dir = (sbyte)LiveActor_HAIRMOD_PROTECTED.r.nextInt(4);
		timeLimit = Res_HAIRMOD_PROTECTED.random(10, 20);
		state = 0;
		timeWait = 0;
		p1 = (p2 = (p3 = 0));
		IdvanTieu = (short)idTemplate;
		if (Res_HAIRMOD_PROTECTED.monsterTemplates[idTemplate] != null)
		{
			data = Res_HAIRMOD_PROTECTED.monsterTemplates[idTemplate].getDataMonster();
		}
	}

	public override void paint(MyGraphics_HAIRMOD_PROTECTED g)
	{
		if (data.size() == 0)
		{
			return;
		}
		DataEffect_HAIRMOD_PROTECTED dataEffect_HAIRMOD_PROTECTED = (DataEffect_HAIRMOD_PROTECTED)data.elementAt(huongY);
		if (dataEffect_HAIRMOD_PROTECTED == null)
		{
			return;
		}
		if (dynamicEffBottom != null)
		{
			for (int i = 0; i < dynamicEffBottom.size(); i++)
			{
				if ((DynamicEffect_HAIRMOD_PROTECTED)dynamicEffBottom.elementAt(i) != null)
				{
					((DynamicEffect_HAIRMOD_PROTECTED)dynamicEffBottom.elementAt(i)).paint(g, x, y);
				}
			}
		}
		dataEffect_HAIRMOD_PROTECTED.paint(g, dataEffect_HAIRMOD_PROTECTED.getFrame(frame, state), x, y, flip, Res_HAIRMOD_PROTECTED.monsterTemplates[monster_type].getImage(huongY));
		if (dynamicEffTop == null)
		{
			return;
		}
		for (int j = 0; j < dynamicEffTop.size(); j++)
		{
			if ((DynamicEffect_HAIRMOD_PROTECTED)dynamicEffTop.elementAt(j) != null)
			{
				((DynamicEffect_HAIRMOD_PROTECTED)dynamicEffTop.elementAt(j)).paint(g, x, y);
			}
		}
	}

	public override void paintCorner(MyGraphics_HAIRMOD_PROTECTED g, int xCorner, int yCorner)
	{
	}

	public bool isMonsterVantieu()
	{
		return IdvanTieu >= 95 && IdvanTieu <= 112;
	}

	public override void paintName(MyGraphics_HAIRMOD_PROTECTED g)
	{
		base.paintName(g);
	}

	public void set_Target(LiveActor_HAIRMOD_PROTECTED acto)
	{
		if (Res_HAIRMOD_PROTECTED.monsterTemplates[monster_type] != null && Res_HAIRMOD_PROTECTED.monsterTemplates[monster_type].moveType != 4)
		{
			huongY = (sbyte)((y > acto.y) ? 1 : 0);
			flip = (sbyte)((x - acto.x <= 0) ? 2 : 0);
			dir = Util_HAIRMOD_PROTECTED.findDirection(this, acto);
			setDir();
		}
	}

	public override void update()
	{
		updateDynamicBuff();
		DataEffect_HAIRMOD_PROTECTED dataEffect_HAIRMOD_PROTECTED = null;
		if (data.size() > 0)
		{
			dataEffect_HAIRMOD_PROTECTED = (DataEffect_HAIRMOD_PROTECTED)data.elementAt(0);
			frame = (sbyte)((frame + 1) % dataEffect_HAIRMOD_PROTECTED.getAnim(state).frame.Length);
			if ((wantDestroy || hp <= 0) && timeLive != -1 && state != 8)
			{
				state = 8;
			}
		}
		else
		{
			setInfoTemplate();
		}
		if (!isSetInfo && mSystem_HAIRMOD_PROTECTED.currentTimeMillis() - timeGetInfo > 15000)
		{
			timeGetInfo = mSystem_HAIRMOD_PROTECTED.currentTimeMillis();
			Canvas_HAIRMOD_PROTECTED.gameScr.gameService.requestMonsterInfo(ID);
		}
		if (Res_HAIRMOD_PROTECTED.monsterTemplates != null)
		{
			if (Res_HAIRMOD_PROTECTED.monsterTemplates[monster_type] == null)
			{
				if (!getMonsterTemplate && mSystem_HAIRMOD_PROTECTED.currentTimeMillis() > timeCreate)
				{
					getMonsterTemplate = false;
					GameService_HAIRMOD_PROTECTED.gI().sendGetMonsterTEmplate(0, monster_type);
					timeCreate = mSystem_HAIRMOD_PROTECTED.currentTimeMillis() + 10000;
				}
			}
			else if (!getMonsterTemplate)
			{
				setType(monster_type);
				getMonsterTemplate = true;
				speed = Res_HAIRMOD_PROTECTED.monsterTemplates[monster_type].speed;
				height = Res_HAIRMOD_PROTECTED.monsterTemplates[monster_type].height;
				if (Res_HAIRMOD_PROTECTED.monsterTemplates[monster_type].image == null)
				{
					Res_HAIRMOD_PROTECTED.monsterTemplates[monster_type].loadImage();
				}
			}
		}
		if (beStune && mSystem_HAIRMOD_PROTECTED.getCurrentTimeMillis() > timeBeStune)
		{
			beStune = false;
		}
		updateBuff();
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
		isPoinson();
		switch (state)
		{
		case 4:
			if (dataEffect_HAIRMOD_PROTECTED != null && frame == dataEffect_HAIRMOD_PROTECTED.getAnim(state).frame.Length - 1)
			{
				state = 0;
			}
			break;
		case 0:
		{
			timeWait++;
			if (timeWait <= timeLimit || idChar != -1)
			{
				break;
			}
			short num = (short)(xFirst - range);
			short num2 = (short)(xFirst + range);
			short num3 = (short)(yFirst - range);
			short num4 = (short)(yFirst + range);
			sbyte b = (sbyte)LiveActor_HAIRMOD_PROTECTED.r.nextInt(4);
			if (lastDir != -100 && lastDir == b)
			{
				if (b == Char_HAIRMOD_PROTECTED.LEFT)
				{
					b = Char_HAIRMOD_PROTECTED.RIGHT;
				}
				else if (b == Char_HAIRMOD_PROTECTED.RIGHT)
				{
					b = Char_HAIRMOD_PROTECTED.LEFT;
				}
				else if (b == Char_HAIRMOD_PROTECTED.UP)
				{
					b = Char_HAIRMOD_PROTECTED.DOWN;
				}
				else if (b == Char_HAIRMOD_PROTECTED.DOWN)
				{
					b = Char_HAIRMOD_PROTECTED.UP;
				}
			}
			if (b == Char_HAIRMOD_PROTECTED.LEFT)
			{
				vx = (sbyte)(-speed);
				vy = 0;
				if (Math_HAIRMOD_PROTECTED.abs(x - num) < 32)
				{
					b = Char_HAIRMOD_PROTECTED.RIGHT;
					vx = (sbyte)speed;
				}
			}
			else if (b == Char_HAIRMOD_PROTECTED.RIGHT)
			{
				vx = (sbyte)speed;
				vy = 0;
				if (Math_HAIRMOD_PROTECTED.abs(x - num2) < 32)
				{
					b = Char_HAIRMOD_PROTECTED.LEFT;
					vx = (sbyte)(-speed);
				}
			}
			else if (b == Char_HAIRMOD_PROTECTED.UP)
			{
				vx = 0;
				vy = (sbyte)(-speed);
				if (Math_HAIRMOD_PROTECTED.abs(y - num3) < 32)
				{
					b = Char_HAIRMOD_PROTECTED.DOWN;
					vy = (sbyte)speed;
				}
			}
			else if (b == Char_HAIRMOD_PROTECTED.DOWN)
			{
				vx = 0;
				vy = (sbyte)speed;
				if (Math_HAIRMOD_PROTECTED.abs(y - num4) < 32)
				{
					b = Char_HAIRMOD_PROTECTED.UP;
					vy = (sbyte)(-speed);
				}
			}
			dir = b;
			state = 2;
			timeWait = 0;
			break;
		}
		case 3:
			if (listAttack.size() == 0)
			{
				state = 0;
				return;
			}
			if (dataEffect_HAIRMOD_PROTECTED == null)
			{
				break;
			}
			if (!isNewMonster)
			{
				if (monster_type == 117)
				{
					if (listAttack.size() > 0)
					{
						LiveActor_HAIRMOD_PROTECTED liveActor_HAIRMOD_PROTECTED = (LiveActor_HAIRMOD_PROTECTED)listAttack.elementAt(0);
						set_Target(liveActor_HAIRMOD_PROTECTED);
						if (frame == dataEffect_HAIRMOD_PROTECTED.getAnim(state).frame.Length - 7)
						{
							if (skillID == 1)
							{
								Canvas_HAIRMOD_PROTECTED.gameScr.startNewArrow(7, this, liveActor_HAIRMOD_PROTECTED, x, y, liveActor_HAIRMOD_PROTECTED.damAttack, 0, 18);
							}
							else
							{
								Canvas_HAIRMOD_PROTECTED.gameScr.startSkill_BossHalloween(x, y, 0, listAttack);
							}
							listAttack.removeElementAt(0);
						}
					}
					if (listAttack.size() == 0 && frame == dataEffect_HAIRMOD_PROTECTED.getAnim(state).frame.Length - 1)
					{
						state = 0;
					}
				}
				else if (monster_type == 116)
				{
					if (listAttack.size() > 0)
					{
						LiveActor_HAIRMOD_PROTECTED liveActor_HAIRMOD_PROTECTED2 = (LiveActor_HAIRMOD_PROTECTED)listAttack.elementAt(0);
						set_Target(liveActor_HAIRMOD_PROTECTED2);
						if (frame == dataEffect_HAIRMOD_PROTECTED.getAnim(state).frame.Length - 7)
						{
							liveActor_HAIRMOD_PROTECTED2.jump();
							if (skillID == 1)
							{
								Canvas_HAIRMOD_PROTECTED.gameScr.startNewMagicBeam(9, this, liveActor_HAIRMOD_PROTECTED2, x, y, liveActor_HAIRMOD_PROTECTED2.damAttack, 0);
							}
							else if (skillID == 0)
							{
								for (int i = 0; i < 8; i++)
								{
									Canvas_HAIRMOD_PROTECTED.gameScr.startNewMagicBeam(10, this, liveActor_HAIRMOD_PROTECTED2, x + splashDuaX[dir] + x2[i], y + splashDuaY[dir] + y2[i], liveActor_HAIRMOD_PROTECTED2.damAttack, 0, Skill_Dao_Type3_HAIRMOD_PROTECTED.goc[dir][i]);
								}
							}
							else
							{
								Skill_blood_down_HAIRMOD_PROTECTED o = new Skill_blood_down_HAIRMOD_PROTECTED(liveActor_HAIRMOD_PROTECTED2.x, liveActor_HAIRMOD_PROTECTED2.y, liveActor_HAIRMOD_PROTECTED2.damAttack);
								EffectManager_HAIRMOD_PROTECTED.hiEffects.addElement(o);
							}
							listAttack.removeElementAt(0);
						}
					}
					if (listAttack.size() == 0 && frame == dataEffect_HAIRMOD_PROTECTED.getAnim(state).frame.Length - 1)
					{
						state = 0;
					}
				}
				else if (!isMonsterVantieu() && (skillID == 0 || skillID == 2))
				{
					LiveActor_HAIRMOD_PROTECTED liveActor_HAIRMOD_PROTECTED3 = (LiveActor_HAIRMOD_PROTECTED)listAttack.elementAt(0);
					set_Target(liveActor_HAIRMOD_PROTECTED3);
					if (frame == dataEffect_HAIRMOD_PROTECTED.getAnim(state).frame.Length - 7 && dir != Char_HAIRMOD_PROTECTED.LEFT)
					{
						if (dir == Char_HAIRMOD_PROTECTED.RIGHT)
						{
							Canvas_HAIRMOD_PROTECTED.gameScr.startSkill_Noel(x + 50, y, GameScr_HAIRMOD_PROTECTED.cmy, listAttack);
						}
						else if (dir == Char_HAIRMOD_PROTECTED.UP)
						{
							Canvas_HAIRMOD_PROTECTED.gameScr.startSkill_Noel(x, y - 50, GameScr_HAIRMOD_PROTECTED.cmy, listAttack);
						}
						else if (dir == Char_HAIRMOD_PROTECTED.DOWN)
						{
							Canvas_HAIRMOD_PROTECTED.gameScr.startSkill_Noel(x, y + 50, GameScr_HAIRMOD_PROTECTED.cmy, listAttack);
						}
					}
					if (frame == dataEffect_HAIRMOD_PROTECTED.getAnim(state).frame.Length - 1)
					{
						state = 0;
					}
				}
				else if (skillID == 1)
				{
					if (listAttack.size() > 0)
					{
						LiveActor_HAIRMOD_PROTECTED liveActor_HAIRMOD_PROTECTED4 = (LiveActor_HAIRMOD_PROTECTED)listAttack.elementAt(0);
						set_Target(liveActor_HAIRMOD_PROTECTED4);
						if (frame == dataEffect_HAIRMOD_PROTECTED.getAnim(state).frame.Length - 7)
						{
							liveActor_HAIRMOD_PROTECTED4.jump();
							if (monster_type == 115)
							{
								Canvas_HAIRMOD_PROTECTED.gameScr.startNewArrow(14, this, liveActor_HAIRMOD_PROTECTED4, x, y - 15, liveActor_HAIRMOD_PROTECTED4.damAttack, 0, 14);
							}
							else
							{
								Canvas_HAIRMOD_PROTECTED.gameScr.startNewArrow(13, this, liveActor_HAIRMOD_PROTECTED4, x, y - 15, liveActor_HAIRMOD_PROTECTED4.damAttack, 0, 13);
							}
						}
					}
					if (listAttack.size() == 0 && frame == dataEffect_HAIRMOD_PROTECTED.getAnim(state).frame.Length - 1)
					{
						state = 0;
					}
				}
				startEffectSkill(skillID, dataEffect_HAIRMOD_PROTECTED);
			}
			else
			{
				if (listAttack.size() <= 0)
				{
					break;
				}
				if (frame == dataEffect_HAIRMOD_PROTECTED.getAnim(state).frame.Length - 7)
				{
					for (int j = 0; j < listAttack.size(); j++)
					{
						LiveActor_HAIRMOD_PROTECTED liveActor_HAIRMOD_PROTECTED5 = (LiveActor_HAIRMOD_PROTECTED)listAttack.elementAt(j);
						GameScr_HAIRMOD_PROTECTED.startSkillEffect(liveActor_HAIRMOD_PROTECTED5, idSkill, 5000L, 1, liveActor_HAIRMOD_PROTECTED5.damAttack, bymap);
					}
					listAttack.removeAllElements();
				}
				if (listAttack.size() == 0 && frame == dataEffect_HAIRMOD_PROTECTED.getAnim(state).frame.Length - 1)
				{
					state = 0;
				}
			}
			break;
		case 5:
			wantDestroy = true;
			frame = 0;
			Canvas_HAIRMOD_PROTECTED.gameScr.actors.removeElement(this);
			break;
		case 8:
			if (Canvas_HAIRMOD_PROTECTED.gameScr.focusedActor != null && Canvas_HAIRMOD_PROTECTED.gameScr.focusedActor == this && Canvas_HAIRMOD_PROTECTED.gameScr.mainChar.state == 0)
			{
				Canvas_HAIRMOD_PROTECTED.gameScr.focusedActor = null;
			}
			if (timeRevive - mSystem_HAIRMOD_PROTECTED.getCurrentTimeMillis() < 0)
			{
				wantDestroy = false;
				isDied = false;
				dir = (sbyte)LiveActor_HAIRMOD_PROTECTED.r.nextInt(4);
				timeLimit = Res_HAIRMOD_PROTECTED.random(10, 20);
				state = 0;
				timeWait = 0;
				p1 = (p2 = (p3 = 0));
				x = (xTo = xFirst);
				y = (yTo = yFirst);
				hp = maxhp;
				Canvas_HAIRMOD_PROTECTED.gameScr.startExplosionAt(x, y);
				timeRevive = mSystem_HAIRMOD_PROTECTED.getCurrentTimeMillis() + timeLive;
			}
			break;
		case 2:
		case 6:
		case 7:
			if (!isFreeze)
			{
				if (idChar > -1)
				{
					move();
				}
				else if (state == 7)
				{
					setMove(xFirst, yFirst);
				}
				else if (state == 2)
				{
					move();
				}
				else if (target == null)
				{
					p1 = (p2 = (p3 = 0));
					state = 0;
					timeLimit = Res_HAIRMOD_PROTECTED.random(10, 20);
					vx = (vy = 0);
					lastDir = dir;
				}
				else if (target != null && Math_HAIRMOD_PROTECTED.abs(x - target.x) <= rangestop && Math_HAIRMOD_PROTECTED.abs(y - target.y) <= rangestop)
				{
					vx = (vy = 0);
				}
				else
				{
					setMove(target.x + xadd, target.y + yadd);
				}
			}
			break;
		}
		if (isDied && timeBeginDie - mSystem_HAIRMOD_PROTECTED.getCurrentTimeMillis() / 1000 <= 0 && !wantDestroy)
		{
			Canvas_HAIRMOD_PROTECTED.gameScr.startExplosionAt(x, y);
			wantDestroy = true;
			isDie = false;
		}
		if (hp <= 0 && Canvas_HAIRMOD_PROTECTED.gameScr.focusedActor != null && Canvas_HAIRMOD_PROTECTED.gameScr.focusedActor == this)
		{
			Canvas_HAIRMOD_PROTECTED.gameScr.focusedActor = null;
		}
	}

	private void setInfoTemplate()
	{
		if (Res_HAIRMOD_PROTECTED.monsterTemplates[monster_type] != null)
		{
			data = Res_HAIRMOD_PROTECTED.monsterTemplates[monster_type].getDataMonster();
		}
	}

	public override void startAttack(mVector_HAIRMOD_PROTECTED list, sbyte skillID)
	{
		if (skillID == 2)
		{
			skillID = ((monster_type == 113) ? ((sbyte)1) : ((sbyte)0));
		}
		listAttack = list;
		state = 3;
		p1 = (p2 = (p3 = 0));
		frame = 0;
		this.skillID = skillID;
	}

	public override void setPosTo(short xTo, short yTo)
	{
		if (idChar <= -1)
		{
			return;
		}
		if (state != 3 && x == xTo && y == yTo)
		{
			state = 0;
			return;
		}
		base.xTo = xTo;
		if (Res_HAIRMOD_PROTECTED.monsterTemplates[monster_type].moveType == 4)
		{
			base.yTo = yTo;
		}
		else
		{
			base.yTo = yTo;
			huongY = (sbyte)((y > yTo) ? 1 : 0);
			flip = (sbyte)((x - xTo <= 0) ? 2 : 0);
			setDir();
		}
		if (state != 3)
		{
			state = 2;
		}
		if (state != 3 && x == base.xTo && y == base.yTo)
		{
			state = 0;
		}
	}

	private void startEffectSkill(int id, DataEffect_HAIRMOD_PROTECTED deff)
	{
		if (listAttack.size() <= 0 || id <= 2)
		{
			return;
		}
		LiveActor_HAIRMOD_PROTECTED liveActor_HAIRMOD_PROTECTED = (LiveActor_HAIRMOD_PROTECTED)listAttack.elementAt(0);
		set_Target(liveActor_HAIRMOD_PROTECTED);
		switch (id)
		{
		case 3:
			if (frame == deff.getAnim(state).frame.Length - 7 && dir != Char_HAIRMOD_PROTECTED.LEFT)
			{
				if (dir == Char_HAIRMOD_PROTECTED.RIGHT)
				{
					Canvas_HAIRMOD_PROTECTED.gameScr.startSkill_Noel(x + 50, y, GameScr_HAIRMOD_PROTECTED.cmy, listAttack);
				}
				else if (dir == Char_HAIRMOD_PROTECTED.UP)
				{
					Canvas_HAIRMOD_PROTECTED.gameScr.startSkill_Noel(x, y - 50, GameScr_HAIRMOD_PROTECTED.cmy, listAttack);
				}
				else if (dir == Char_HAIRMOD_PROTECTED.DOWN)
				{
					Canvas_HAIRMOD_PROTECTED.gameScr.startSkill_Noel(x, y + 50, GameScr_HAIRMOD_PROTECTED.cmy, listAttack);
				}
			}
			if (frame == deff.getAnim(state).frame.Length - 1)
			{
				state = 0;
			}
			break;
		case 4:
			Canvas_HAIRMOD_PROTECTED.gameScr.startNewMagicBeam(9, this, liveActor_HAIRMOD_PROTECTED, x, y, liveActor_HAIRMOD_PROTECTED.damAttack, 0);
			break;
		case 5:
		{
			Skill_blood_down_HAIRMOD_PROTECTED o = new Skill_blood_down_HAIRMOD_PROTECTED(liveActor_HAIRMOD_PROTECTED.x, liveActor_HAIRMOD_PROTECTED.y, liveActor_HAIRMOD_PROTECTED.damAttack);
			EffectManager_HAIRMOD_PROTECTED.hiEffects.addElement(o);
			break;
		}
		case 7:
			Canvas_HAIRMOD_PROTECTED.gameScr.startNewArrow(7, this, liveActor_HAIRMOD_PROTECTED, x, y, liveActor_HAIRMOD_PROTECTED.damAttack, 0, 18);
			break;
		case 6:
		{
			for (int k = 0; k < 8; k++)
			{
				Canvas_HAIRMOD_PROTECTED.gameScr.startNewMagicBeam(10, this, liveActor_HAIRMOD_PROTECTED, x + splashDuaX[dir] + x2[k], y + splashDuaY[dir] + y2[k], liveActor_HAIRMOD_PROTECTED.damAttack, 0, Skill_Dao_Type3_HAIRMOD_PROTECTED.goc[dir][k]);
			}
			break;
		}
		case 8:
			Canvas_HAIRMOD_PROTECTED.gameScr.startSkill_BossHalloween(x, y, 0, listAttack);
			break;
		case 9:
		{
			for (int j = 0; j < listAttack.size(); j++)
			{
				LiveActor_HAIRMOD_PROTECTED liveActor_HAIRMOD_PROTECTED3 = (LiveActor_HAIRMOD_PROTECTED)listAttack.elementAt(j);
				ThienThach_Skill_HAIRMOD_PROTECTED o2 = new ThienThach_Skill_HAIRMOD_PROTECTED(liveActor_HAIRMOD_PROTECTED3.x, liveActor_HAIRMOD_PROTECTED3.y, liveActor_HAIRMOD_PROTECTED3.damAttack);
				EffectManager_HAIRMOD_PROTECTED.hiEffects.addElement(o2);
			}
			break;
		}
		case 10:
		{
			for (int i = 0; i < listAttack.size(); i++)
			{
				LiveActor_HAIRMOD_PROTECTED liveActor_HAIRMOD_PROTECTED2 = (LiveActor_HAIRMOD_PROTECTED)listAttack.elementAt(i);
				Canvas_HAIRMOD_PROTECTED.gameScr.startNewArrow(7, this, liveActor_HAIRMOD_PROTECTED2, x - 20, y - 50, liveActor_HAIRMOD_PROTECTED2.damAttack, 2, 8);
				Canvas_HAIRMOD_PROTECTED.gameScr.startNewArrow(7, this, liveActor_HAIRMOD_PROTECTED2, x, y - 30, liveActor_HAIRMOD_PROTECTED2.damAttack, 2, 8);
				Canvas_HAIRMOD_PROTECTED.gameScr.startNewArrow(7, this, liveActor_HAIRMOD_PROTECTED2, x - 20, y - 10, liveActor_HAIRMOD_PROTECTED2.damAttack, 2, 8);
				liveActor_HAIRMOD_PROTECTED.doublejump();
			}
			break;
		}
		case 11:
		{
			for (int l = 0; l < listAttack.size(); l++)
			{
				LiveActor_HAIRMOD_PROTECTED acTo = (LiveActor_HAIRMOD_PROTECTED)listAttack.elementAt(l);
				Canvas_HAIRMOD_PROTECTED.gameScr.startWeapsMango(this, acTo, 30);
			}
			break;
		}
		default:
			if (frame == deff.getAnim(state).frame.Length - 7 && dir != Char_HAIRMOD_PROTECTED.LEFT)
			{
				if (dir == Char_HAIRMOD_PROTECTED.RIGHT)
				{
					Canvas_HAIRMOD_PROTECTED.gameScr.startSkill_Noel(x + 50, y, GameScr_HAIRMOD_PROTECTED.cmy, listAttack);
				}
				else if (dir == Char_HAIRMOD_PROTECTED.UP)
				{
					Canvas_HAIRMOD_PROTECTED.gameScr.startSkill_Noel(x, y - 50, GameScr_HAIRMOD_PROTECTED.cmy, listAttack);
				}
				else if (dir == Char_HAIRMOD_PROTECTED.DOWN)
				{
					Canvas_HAIRMOD_PROTECTED.gameScr.startSkill_Noel(x, y + 50, GameScr_HAIRMOD_PROTECTED.cmy, listAttack);
				}
			}
			if (frame == deff.getAnim(state).frame.Length - 1)
			{
				state = 0;
			}
			break;
		}
		if (listAttack.size() == 0 && frame == deff.getAnim(state).frame.Length - 1)
		{
			state = 0;
		}
	}

	public void setDir()
	{
		if (data.size() > 2)
		{
			flip = 0;
			if (dir == Char_HAIRMOD_PROTECTED.RIGHT)
			{
				flip = 2;
				huongY = 0;
			}
			else if (dir == Char_HAIRMOD_PROTECTED.LEFT)
			{
				huongY = 0;
			}
			else if (dir == Char_HAIRMOD_PROTECTED.DOWN)
			{
				huongY = 1;
			}
			else if (dir == Char_HAIRMOD_PROTECTED.UP)
			{
				huongY = 2;
			}
		}
	}

	public override void setIdSkill(int idSkill)
	{
		this.idSkill = (short)idSkill;
	}

	public override void setByMap(int type)
	{
		bymap = (sbyte)type;
	}

	public override void setNewMonster(int isNewMonster)
	{
		this.isNewMonster = isNewMonster == 1;
	}
}
