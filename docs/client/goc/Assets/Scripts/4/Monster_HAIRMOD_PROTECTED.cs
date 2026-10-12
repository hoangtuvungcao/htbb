using System;

public class Monster_HAIRMOD_PROTECTED : LiveActor_HAIRMOD_PROTECTED
{
	public const sbyte STAND = 0;

	public const sbyte DEAD = 1;

	public const sbyte WALK = 2;

	public const sbyte ATTACK = 3;

	public const sbyte INJURE = 4;

	public const sbyte DEADFLY = 5;

	public const sbyte MOVE_TO_TARGET = 6;

	public const sbyte GO_TO_XYFIRST = 7;

	public const sbyte WAIT = 8;

	public const sbyte TYPE4 = 0;

	public const sbyte TYPE2 = 1;

	public const sbyte TYPE1 = 2;

	public const sbyte TYPE3 = 3;

	public const sbyte TYPE0 = 4;

	public bool isMineralC;

	public LiveActor_HAIRMOD_PROTECTED target;

	public static sbyte[][][] REAL_FRAME = new sbyte[5][][]
	{
		new sbyte[4][]
		{
			new sbyte[4] { 0, 1, 2, 3 },
			new sbyte[4] { 4, 5, 6, 7 },
			new sbyte[4] { 8, 9, 10, 11 },
			new sbyte[4] { 12, 13, 14, 15 }
		},
		new sbyte[4][]
		{
			new sbyte[4] { 0, 1, 2, 3 },
			new sbyte[4] { 4, 5, 6, 7 },
			new sbyte[4] { 0, 1, 2, 3 },
			new sbyte[4] { 4, 5, 6, 7 }
		},
		new sbyte[4][]
		{
			new sbyte[4] { 0, 1, 2, 3 },
			new sbyte[4] { 0, 1, 2, 3 },
			new sbyte[4] { 0, 1, 2, 3 },
			new sbyte[4] { 0, 1, 2, 3 }
		},
		new sbyte[4][]
		{
			new sbyte[4] { 0, 1, 2, 3 },
			new sbyte[4] { 4, 5, 6, 7 },
			new sbyte[4] { 0, 1, 2, 3 },
			new sbyte[4] { 4, 5, 6, 7 }
		},
		new sbyte[4][]
		{
			new sbyte[4],
			new sbyte[4],
			new sbyte[4],
			new sbyte[4]
		}
	};

	public SkillAnimate_HAIRMOD_PROTECTED currentSkillAnimate;

	public LiveActor_HAIRMOD_PROTECTED attackTarget;

	public int idChar = -1;

	public short xTo;

	public short yTo;

	public short p1;

	public short p2;

	public short p3;

	public short monster_type;

	public long timeCreate;

	public bool isTarget;

	public bool isDied;

	public int xadd;

	public int yadd;

	public sbyte[] nh = new sbyte[5] { 1, 4, 0, 2, 3 };

	public bool getMonsterTemplate;

	public long timeBeginDie;

	public long timeRevive;

	public int timeWait;

	public int timeLimit;

	public short lastDir = -100;

	public int lastTimeStop;

	public sbyte vx;

	public sbyte vy;

	private int a;

	private int b;

	private bool isTargetPlayer = true;

	public short range = 60;

	public bool isSetInfo;

	public long timeGetInfo = mSystem_HAIRMOD_PROTECTED.getCurrentTimeMillis();

	public int rangestop = 12;

	public int targethplost;

	public int eff;

	public Monster_HAIRMOD_PROTECTED()
	{
		isDied = false;
		dir = (byte)LiveActor_HAIRMOD_PROTECTED.r.nextInt(4);
		timeLimit = Res_HAIRMOD_PROTECTED.random(10, 20);
		state = 0;
		timeWait = 0;
		p1 = (p2 = (p3 = 0));
	}

	public override string getDisplayName()
	{
		if (Res_HAIRMOD_PROTECTED.monsterTemplates == null)
		{
			return string.Empty;
		}
		return (Res_HAIRMOD_PROTECTED.monsterTemplates[monster_type] != null) ? Res_HAIRMOD_PROTECTED.monsterTemplates[monster_type].name : string.Empty;
	}

	public override void setType(short type)
	{
		monster_type = type;
		if (Res_HAIRMOD_PROTECTED.monsterTemplates != null && Res_HAIRMOD_PROTECTED.monsterTemplates[type] == null)
		{
			Res_HAIRMOD_PROTECTED.monsterTemplates[type] = new MonsterTemplate_HAIRMOD_PROTECTED();
			GameService_HAIRMOD_PROTECTED.gI().sendGetMonsterTEmplate(0, monster_type);
		}
		timeCreate = mSystem_HAIRMOD_PROTECTED.currentTimeMillis() + 10000;
	}

	public void setRemoveTarget()
	{
		target = null;
		if (state != 5)
		{
			state = 7;
		}
	}

	public void setTarget(LiveActor_HAIRMOD_PROTECTED tg)
	{
		target = tg;
		state = 6;
	}

	public override void dead()
	{
	}

	public override bool isPaint()
	{
		if (x < GameScr_HAIRMOD_PROTECTED.cmx)
		{
			return false;
		}
		if (x > GameScr_HAIRMOD_PROTECTED.cmx + Canvas_HAIRMOD_PROTECTED.w)
		{
			return false;
		}
		if (y < GameScr_HAIRMOD_PROTECTED.cmy)
		{
			return false;
		}
		if (y > GameScr_HAIRMOD_PROTECTED.cmy + Canvas_HAIRMOD_PROTECTED.h + 30)
		{
			return false;
		}
		return true;
	}

	public override void paint(MyGraphics_HAIRMOD_PROTECTED g)
	{
		if (!isPaint() || state == 8 || hp <= 0)
		{
			return;
		}
		paintDataEff_Top(g, x, y, false);
		if (!isFreeze)
		{
			if (z != 0)
			{
				frame = 3;
			}
			if (Res_HAIRMOD_PROTECTED.monsterTemplates[monster_type] != null)
			{
				int f = REAL_FRAME[Res_HAIRMOD_PROTECTED.monsterTemplates[monster_type].moveType][dir][frame];
				int num = 0;
				int num2 = 0;
				if (Res_HAIRMOD_PROTECTED.monsterTemplates[monster_type].image != null)
				{
					num = Res_HAIRMOD_PROTECTED.monsterTemplates[monster_type].xadd;
					num2 = Res_HAIRMOD_PROTECTED.monsterTemplates[monster_type].yadd;
				}
				if (dynamicEffBottom != null)
				{
					for (int i = 0; i < dynamicEffBottom.size(); i++)
					{
						((DynamicEffect_HAIRMOD_PROTECTED)dynamicEffBottom.elementAt(i)).paint(g, x, y);
					}
				}
				g.drawImage(Res_HAIRMOD_PROTECTED.imgShadow, x + num, y + num2, 3);
				Res_HAIRMOD_PROTECTED.monsterTemplates[monster_type].paint(g, x + vangx, y + vangy + dy + z, 0, 0, f);
			}
			for (int j = 0; j < buff.size(); j++)
			{
				((CharBuff_HAIRMOD_PROTECTED)buff.elementAt(j)).paint(g);
			}
			if (dynamicEffTop != null)
			{
				for (int k = 0; k < dynamicEffTop.size(); k++)
				{
					((DynamicEffect_HAIRMOD_PROTECTED)dynamicEffTop.elementAt(k)).paint(g, x, y);
				}
			}
		}
		paintDataEff_Bot(g, x, y, false);
		base.paint(g);
	}

	public override void paintActive(MyGraphics_HAIRMOD_PROTECTED g, int x, int y)
	{
	}

	public override void paintCorner(MyGraphics_HAIRMOD_PROTECTED g, int xCorner, int yCorner)
	{
		if (z != 0)
		{
			frame = 3;
		}
		try
		{
			int num = REAL_FRAME[Res_HAIRMOD_PROTECTED.monsterTemplates[monster_type].moveType][0][frame];
			g.drawRegion(Res_HAIRMOD_PROTECTED.monsterTemplates[monster_type].image, 0, num * Res_HAIRMOD_PROTECTED.monsterTemplates[monster_type].h, Res_HAIRMOD_PROTECTED.monsterTemplates[monster_type].w, Res_HAIRMOD_PROTECTED.monsterTemplates[monster_type].h, 0, xCorner - Res_HAIRMOD_PROTECTED.monsterTemplates[monster_type].xCenter, yCorner - 20, 0);
			g.drawRegion(Res_HAIRMOD_PROTECTED.imgNguHanh, nh[he] * 16, 0, 16, 16, 0, xCorner - 15, yCorner + 3, MyGraphics_HAIRMOD_PROTECTED.TOP | MyGraphics_HAIRMOD_PROTECTED.LEFT);
		}
		catch (Exception)
		{
		}
	}

	public override void createBuff(int typeBuffEff, int timeLive)
	{
		buff.addElement(new CharBuff_HAIRMOD_PROTECTED(x, y, typeBuffEff));
	}

	public override void updateBuff()
	{
		for (int i = 0; i < buff.size(); i++)
		{
			CharBuff_HAIRMOD_PROTECTED charBuff_HAIRMOD_PROTECTED = (CharBuff_HAIRMOD_PROTECTED)buff.elementAt(i);
			charBuff_HAIRMOD_PROTECTED.update();
			if (charBuff_HAIRMOD_PROTECTED.wantDetroy)
			{
				buff.removeElementAt(i);
			}
			else
			{
				charBuff_HAIRMOD_PROTECTED.setXY(x, (!charBuff_HAIRMOD_PROTECTED.isCenter()) ? y : (y - 12));
			}
		}
	}

	public override void startDeadFly(int dx, int dy)
	{
		state = 5;
		p1 = 0;
		p2 = (short)dx;
		p3 = (short)dy;
		if (isDied)
		{
			Canvas_HAIRMOD_PROTECTED.gameScr.startExplosionAt(x, y);
			wantDestroy = true;
		}
	}

	public override void update()
	{
		if (Res_HAIRMOD_PROTECTED.monsterTemplates[monster_type].moveType == 4)
		{
			p1 = (p2 = (p3 = 0));
			state = 0;
			return;
		}
		updateDataEffect();
		speed = Res_HAIRMOD_PROTECTED.monsterTemplates[monster_type].speed;
		if ((wantDestroy || hp <= 0) && timeLive != -1 && state != 8)
		{
			state = 8;
		}
		if (!isSetInfo && mSystem_HAIRMOD_PROTECTED.getCurrentTimeMillis() - timeGetInfo > 15000)
		{
			timeGetInfo = mSystem_HAIRMOD_PROTECTED.getCurrentTimeMillis();
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
		updateDynamicBuff();
		updateBuff();
		base.update();
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
			frame = 3;
			p1++;
			if (p1 == 5)
			{
				Canvas_HAIRMOD_PROTECTED.gameScr.startExplosionAt(x, y);
			}
			if (p1 > 7)
			{
				p1 = 0;
				state = 0;
			}
			break;
		case 0:
		{
			frame = 0;
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
			frame = 2;
			p1++;
			if (p1 > 6)
			{
				p1 = 0;
				state = 2;
			}
			else if (currentSkillAnimate != null)
			{
				currentSkillAnimate.updateSkill(this);
			}
			break;
		case 5:
			frame = 3;
			p1++;
			if (!isMineralC)
			{
				x += p2;
				y += p3;
			}
			p2 >>= 1;
			p3 >>= 1;
			if (p1 == 5)
			{
				Canvas_HAIRMOD_PROTECTED.gameScr.startExplosionAt(x, y);
				timeRevive = mSystem_HAIRMOD_PROTECTED.getCurrentTimeMillis() + timeLive;
			}
			isDied = true;
			timeBeginDie = mSystem_HAIRMOD_PROTECTED.getCurrentTimeMillis() / 1000 + 3;
			if (p1 > 7)
			{
				isDied = false;
				wantDestroy = true;
			}
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
				dir = (byte)LiveActor_HAIRMOD_PROTECTED.r.nextInt(4);
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
			if (FuntioncanMove())
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

	public void setMove(int i, int j)
	{
		if (Res_HAIRMOD_PROTECTED.monsterTemplates[monster_type].moveType != 4)
		{
			bool flag = false;
			bool flag2 = false;
			int num = Math_HAIRMOD_PROTECTED.abs(x - i);
			int num2 = Math_HAIRMOD_PROTECTED.abs(y - j);
			if (num <= speed)
			{
				x = (short)i;
				flag = true;
			}
			if (num2 < speed)
			{
				y = (short)j;
				flag2 = true;
			}
			if (flag && flag2)
			{
				p1 = (p2 = (p3 = 0));
				state = 0;
				timeLimit = Res_HAIRMOD_PROTECTED.random(10, 20);
				vx = (vy = 0);
				lastDir = dir;
			}
			else if (x < i)
			{
				x += speed;
				dir = Char_HAIRMOD_PROTECTED.RIGHT;
			}
			else if (x > i)
			{
				x -= speed;
				dir = Char_HAIRMOD_PROTECTED.LEFT;
			}
			else if (y > j)
			{
				y -= speed;
				dir = Char_HAIRMOD_PROTECTED.UP;
			}
			else if (y < j)
			{
				dir = Char_HAIRMOD_PROTECTED.DOWN;
				y += speed;
			}
		}
	}

	public virtual void move()
	{
		if (Res_HAIRMOD_PROTECTED.monsterTemplates[monster_type].moveType == 4)
		{
			p1 = (p2 = (p3 = 0));
			state = 0;
			return;
		}
		if (idChar == -1)
		{
			short num = (short)(xFirst - range + speed);
			short num2 = (short)(xFirst + range - speed);
			short num3 = (short)(yFirst - range + speed);
			short num4 = (short)(yFirst + range - speed);
			int px = x - speed;
			int px2 = x + speed;
			int py = y - speed;
			int py2 = y + speed;
			int num5 = LiveActor_HAIRMOD_PROTECTED.r.nextInt(50);
			if ((x >= num2 && dir == Char_HAIRMOD_PROTECTED.RIGHT) || (x <= num && dir == Char_HAIRMOD_PROTECTED.LEFT) || (y <= num3 && dir == Char_HAIRMOD_PROTECTED.UP) || (y >= num4 && dir == Char_HAIRMOD_PROTECTED.DOWN) || (Tilemap_HAIRMOD_PROTECTED.tileTypeAtPixel(px, y, 2) && dir == Char_HAIRMOD_PROTECTED.LEFT) || (Tilemap_HAIRMOD_PROTECTED.tileTypeAtPixel(px2, y, 2) && dir == Char_HAIRMOD_PROTECTED.RIGHT) || (Tilemap_HAIRMOD_PROTECTED.tileTypeAtPixel(x, py, 2) && dir == Char_HAIRMOD_PROTECTED.UP) || (Tilemap_HAIRMOD_PROTECTED.tileTypeAtPixel(x, py2, 2) && dir == Char_HAIRMOD_PROTECTED.DOWN) || num5 == 5)
			{
				p1 = (p2 = (p3 = 0));
				state = 0;
				timeLimit = Res_HAIRMOD_PROTECTED.random(10, 20);
				vx = (vy = 0);
				lastDir = dir;
			}
			x += vx;
			y += vy;
			return;
		}
		bool flag = false;
		bool flag2 = false;
		int num6 = Math_HAIRMOD_PROTECTED.abs(x - xTo);
		int num7 = Math_HAIRMOD_PROTECTED.abs(y - yTo);
		if (num6 <= speed)
		{
			x = xTo;
			flag = true;
		}
		if (num7 < speed)
		{
			y = yTo;
			flag2 = true;
		}
		if (flag && flag2)
		{
			p1 = (p2 = (p3 = 0));
			state = 0;
		}
		else if (x < xTo)
		{
			x += speed;
			dir = Char_HAIRMOD_PROTECTED.RIGHT;
		}
		else if (x > xTo)
		{
			x -= speed;
			dir = Char_HAIRMOD_PROTECTED.LEFT;
		}
		else if (y > yTo)
		{
			y -= speed;
			dir = Char_HAIRMOD_PROTECTED.UP;
		}
		else if (y < yTo)
		{
			dir = Char_HAIRMOD_PROTECTED.DOWN;
			y += speed;
		}
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
		this.xTo = xTo;
		if (Res_HAIRMOD_PROTECTED.monsterTemplates[monster_type].moveType == 4)
		{
			this.yTo = yTo;
		}
		else
		{
			this.yTo = (short)(yTo - 4 + Canvas_HAIRMOD_PROTECTED.r.nextInt() % 8);
		}
		if (state != 3)
		{
			state = 2;
		}
		if (state != 3 && x == this.xTo && y == this.yTo)
		{
			state = 0;
		}
	}

	public virtual void setInfo(MonsterInfo_HAIRMOD_PROTECTED monsterInfo)
	{
		timeLive = monsterInfo.timeLive;
		int[] array = new int[6] { 16, 32, 48, -16, -32, -48 };
		xadd = array[LiveActor_HAIRMOD_PROTECTED.r.nextInt(array.Length - 1)];
		yadd = array[LiveActor_HAIRMOD_PROTECTED.r.nextInt(array.Length - 1)];
		if (isMineralC)
		{
			x = monsterInfo.x;
			y = monsterInfo.y;
		}
		else
		{
			xTo = monsterInfo.x;
			yTo = monsterInfo.y;
			a = monsterInfo.x;
			b = monsterInfo.y;
		}
		rangestop = LiveActor_HAIRMOD_PROTECTED.r.nextInt(10) + 6;
		realHP = (hp = monsterInfo.hp);
		he = monsterInfo.he;
		state = 0;
		p1 = 0;
		p2 = 0;
		p3 = 0;
		monster_type = monsterInfo.monster_type;
		if (Res_HAIRMOD_PROTECTED.monsterTemplates != null && Res_HAIRMOD_PROTECTED.monsterTemplates[monster_type] != null)
		{
			speed = Res_HAIRMOD_PROTECTED.monsterTemplates[monster_type].speed;
			height = Res_HAIRMOD_PROTECTED.monsterTemplates[monster_type].height;
			if (Res_HAIRMOD_PROTECTED.monsterTemplates[monster_type].image == null)
			{
				Res_HAIRMOD_PROTECTED.monsterTemplates[monster_type].loadImage();
			}
		}
		level = (sbyte)monsterInfo.lv;
		maxhp = monsterInfo.maxhp;
		isSetInfo = true;
	}

	public virtual void startAttack()
	{
		state = 3;
		p1 = (p2 = (p3 = 0));
	}

	public virtual void startAttack(LiveActor_HAIRMOD_PROTECTED target, int hpLost, sbyte skillID, sbyte clazz)
	{
		state = 3;
		p1 = (p2 = (p3 = 0));
		targethplost = hpLost;
		attackTarget = target;
		currentSkillAnimate = SkillManager_HAIRMOD_PROTECTED.getSkillAnimate(skillID, clazz);
	}

	public virtual void startAttack(mVector_HAIRMOD_PROTECTED list, sbyte skillID)
	{
		state = 3;
		p1 = (p2 = (p3 = 0));
		if (monster_type == 90)
		{
			currentSkillAnimate = SkillManager_HAIRMOD_PROTECTED.setSkillBoss_Xmas();
			currentSkillAnimate.setMonster(list);
		}
	}

	public override void jump()
	{
		if (!isMineralC)
		{
			z = -3;
			dz = -5;
		}
	}

	public virtual void setHp(int dam)
	{
		hp -= dam;
	}

	public override void setRealHP(int realHP)
	{
		base.realHP = realHP;
		hp = base.realHP;
		realHPSyncTime = 20;
	}

	public override bool isMonster()
	{
		return true;
	}

	public override bool isMineral()
	{
		return isMineralC;
	}
}
