using System;

public class Pet_HAIRMOD_PROTECTED : AnimalMove_HAIRMOD_PROTECTED
{
	public const sbyte ROAM = 0;

	public const sbyte FOLLOW = 1;

	public const sbyte ATTACK = 2;

	public const sbyte RETURN = 3;

	public const sbyte WAIT = 4;

	public const sbyte DEATH = 5;

	public const sbyte ATTRACTION = 6;

	protected const sbyte DIS_TO_ATTRACT = 80;

	protected const sbyte DIS_TO_FOLLOW = 40;

	public const sbyte M_STAND = 0;

	public const sbyte M_DEAD = 1;

	public const sbyte M_WALK = 2;

	public const sbyte M_ATTACK = 3;

	public const sbyte AC_STAND = 0;

	public const sbyte AC_MOVE = 1;

	public const sbyte AC_FIRE = 2;

	public const sbyte AC_HIT = 3;

	public const sbyte AC_DIE = 4;

	public const sbyte DIR_UP = 1;

	public const sbyte DIR_DOWN = 0;

	public const sbyte DIR_LEFT = 2;

	public const sbyte DIR_RIGHT = 3;

	protected bool isSequenceAttack;

	protected bool isDoneAttack;

	protected sbyte imageId;

	protected sbyte Action;

	protected sbyte preState;

	protected sbyte attackType;

	protected int attackCount;

	protected int oldPosX;

	protected int oldPosY;

	protected int ownerX;

	protected new int p1;

	protected int ownerY;

	protected int time;

	protected int timeAutoAction;

	protected int vx;

	protected int vy;

	protected int xAnchor;

	protected int yAnchor;

	protected int limitMove;

	public int xtam;

	public int vatam = 6;

	public new int vMax;

	public int idTemplate;

	public short yfly;

	public short range;

	public short dyattack;

	public short idSkill;

	public sbyte typemove;

	public sbyte f;

	public sbyte mAction;

	protected mVector_HAIRMOD_PROTECTED wayPoint = new mVector_HAIRMOD_PROTECTED();

	private mVector_HAIRMOD_PROTECTED ntarget = new mVector_HAIRMOD_PROTECTED();

	protected Actor_HAIRMOD_PROTECTED owner;

	public static sbyte[] mTypemove = new sbyte[6] { 2, 1, 0, 2, 0, 1 };

	private bool isBeginAttack;

	private sbyte huongY;

	private sbyte flip;

	private new sbyte frame;

	public static MyHashTable_HAIRMOD_PROTECTED ALL_PET_DATA = new MyHashTable_HAIRMOD_PROTECTED();

	private int toX;

	private int toY;

	private int cmove;

	private bool isRunAttack;

	public long timeBeginMoveAttack;

	public int[] arrdame;

	public int[] arrhpcon;

	private sbyte typeAttack;

	public int maxAttack;

	public Pet_HAIRMOD_PROTECTED(Char_HAIRMOD_PROTECTED mowner, int idtemplate)
	{
		idTemplate = idtemplate;
		owner = mowner;
		xAnchor = owner.x;
		yAnchor = owner.y;
		x = owner.x;
		y = owner.y;
		oldPosX = owner.x;
		oldPosY = owner.y;
		limitMove = 48;
		dir = 0;
		vMax = 4;
		speed = (short)(owner.getSpeed() - 1);
		state = 0;
		Action = 0;
		mAction = 0;
		range = 30;
		timeAutoAction = Res_HAIRMOD_PROTECTED.random(200, 250);
		catagory = Actor_HAIRMOD_PROTECTED.CAT_MY_PET;
		ID = owner.ID;
	}

	public Pet_HAIRMOD_PROTECTED(int idtemplate)
	{
		idTemplate = idtemplate;
		dir = 0;
		state = 0;
		Action = 0;
		mAction = 0;
		range = 30;
		timeAutoAction = Res_HAIRMOD_PROTECTED.random(200, 250);
		catagory = Actor_HAIRMOD_PROTECTED.CAT_MY_PET;
	}

	public Pet_HAIRMOD_PROTECTED(short typePet, byte nFrame, byte imageId, byte typemove)
	{
	}

	public void loadpetData()
	{
	}

	public void setAnim()
	{
	}

	public void clearWayPoints()
	{
		wayPoint.removeAllElements();
	}

	public void setState(sbyte state)
	{
		base.state = state;
	}

	public override void paint(MyGraphics_HAIRMOD_PROTECTED g)
	{
		try
		{
			mDataEffect_HAIRMOD_PROTECTED mDataEffect_HAIRMOD_PROTECTED2 = (mDataEffect_HAIRMOD_PROTECTED)ALL_PET_DATA.get(string.Empty + idTemplate);
			if (mDataEffect_HAIRMOD_PROTECTED2 != null && mDataEffect_HAIRMOD_PROTECTED2 != null)
			{
				int action = mAction;
				if (state == 2 && isBeginAttack)
				{
					action = 3;
				}
				sbyte[] dxDyShadow = mDataEffect_HAIRMOD_PROTECTED2.getDxDyShadow(mDataEffect_HAIRMOD_PROTECTED2.getFrame(frame, action, huongY));
				if (isFly())
				{
					int num = x + dxDyShadow[0];
					int num2 = y + dxDyShadow[1];
					g.drawImage(GameScr_HAIRMOD_PROTECTED.petShadow, num, num2, 0);
				}
				else
				{
					int num3 = x + dxDyShadow[0];
					int num4 = y + dxDyShadow[1];
					g.drawImage(GameScr_HAIRMOD_PROTECTED.petShadow, num3, num4, 0);
				}
				if (width == 0 || height == 0)
				{
					width = mDataEffect_HAIRMOD_PROTECTED2.getWith();
					height = mDataEffect_HAIRMOD_PROTECTED2.getHeight();
				}
				ImageIcon_HAIRMOD_PROTECTED imgIcon = GameData_HAIRMOD_PROTECTED.getImgIcon((short)(idTemplate + 8700));
				if (imgIcon != null && imgIcon.img != null)
				{
					mDataEffect_HAIRMOD_PROTECTED2.paintPet(g, mDataEffect_HAIRMOD_PROTECTED2.getFrame(frame, action, huongY), x, y, flip, imgIcon.img, dyattack);
				}
			}
		}
		catch (Exception)
		{
		}
	}

	public bool isFly()
	{
		mDataEffect_HAIRMOD_PROTECTED mDataEffect_HAIRMOD_PROTECTED2 = (mDataEffect_HAIRMOD_PROTECTED)ALL_PET_DATA.get(string.Empty + idTemplate);
		if (mDataEffect_HAIRMOD_PROTECTED2 != null && mDataEffect_HAIRMOD_PROTECTED2.isFly <= -5)
		{
			return true;
		}
		return false;
	}

	protected void roam()
	{
		vMax = 1;
		if (Action == 1)
		{
			if (time > timeAutoAction || Res_HAIRMOD_PROTECTED.random(16) == 0 || Util_HAIRMOD_PROTECTED.getDistance(x + vx, y + vy, xAnchor, yAnchor) >= limitMove)
			{
				time = 0;
				Action = 0;
				mAction = 0;
				vx = 0;
				vy = 0;
			}
		}
		else if (Action == 0)
		{
			vx = 0;
			vy = 0;
			if (time > timeAutoAction / 2 || Res_HAIRMOD_PROTECTED.random(12) == 0)
			{
				time = 0;
				Action = 1;
				mAction = 2;
				dir = (short)Res_HAIRMOD_PROTECTED.random(4);
				setSpeedInDirection(vMax);
			}
		}
		if (owner != null)
		{
			if (owner.getState() == 1 && Util_HAIRMOD_PROTECTED.getDistance(x, y, ownerX, ownerY) > 40)
			{
				setState(1);
			}
			if (owner.getState() == 0 && Util_HAIRMOD_PROTECTED.getDistance(x, y, ownerX, ownerY) > limitMove * 2)
			{
				setState(3);
			}
		}
		else
		{
			int distance = Util_HAIRMOD_PROTECTED.getDistance(x, y, Canvas_HAIRMOD_PROTECTED.gameScr.mainChar.x, Canvas_HAIRMOD_PROTECTED.gameScr.mainChar.y);
			if (distance < 80 && distance > 40 && Res_HAIRMOD_PROTECTED.random(6) == 0)
			{
				setState(6);
			}
		}
	}

	public void setSpeedInDirection(int max)
	{
		int num = Res_HAIRMOD_PROTECTED.random_Am_0(3);
		if (Util_HAIRMOD_PROTECTED.abs(num) > 1)
		{
			max--;
		}
		switch (dir)
		{
		case 1:
			vy = -max;
			vx = num;
			break;
		case 0:
			vy = max;
			vx = num;
			break;
		case 2:
			vy = num;
			vx = -max;
			break;
		case 3:
			vy = num;
			vx = max;
			break;
		}
		if (vx == 0 && Res_HAIRMOD_PROTECTED.random(3) == 0)
		{
			time = 0;
			state = 0;
			vx = 0;
			vy = 0;
			mAction = 0;
		}
		if (vx > 0)
		{
			dir = 3;
		}
		else
		{
			dir = 2;
		}
		huongY = (sbyte)((vy > 0) ? 1 : 0);
		flip = (sbyte)((vx <= 0) ? 2 : 0);
		flip = 0;
		if (dir == Char_HAIRMOD_PROTECTED.RIGHT)
		{
			flip = 2;
		}
		else if (dir != Char_HAIRMOD_PROTECTED.LEFT)
		{
			if (dir == Char_HAIRMOD_PROTECTED.DOWN)
			{
				huongY = 0;
			}
			else if (dir == Char_HAIRMOD_PROTECTED.UP)
			{
				huongY = 1;
			}
		}
		mAction = 2;
	}

	public void setHuong(int xto, int yto)
	{
		huongY = (sbyte)((y > yto) ? 1 : 0);
		flip = (sbyte)((x - xto <= 0) ? 2 : 0);
		flip = 0;
		if (dir == Char_HAIRMOD_PROTECTED.RIGHT)
		{
			flip = 2;
		}
		else if (dir != Char_HAIRMOD_PROTECTED.LEFT)
		{
			if (dir == Char_HAIRMOD_PROTECTED.DOWN)
			{
				huongY = 0;
			}
			else if (dir == Char_HAIRMOD_PROTECTED.UP)
			{
				huongY = 1;
			}
		}
	}

	protected void standStill()
	{
		vx = 0;
		vy = 0;
		state = 4;
		if (owner != null && owner.getState() == 0)
		{
			setState(0);
		}
	}

	protected void follow()
	{
		vMax = owner.getSpeed();
		Action = 1;
		if (wayPoint.size() <= 0)
		{
			cmove++;
			if (cmove > 20)
			{
				setState(0);
			}
		}
		if (Util_HAIRMOD_PROTECTED.getDistance(oldPosX, oldPosY, ownerX, ownerY) > 40)
		{
			Point_HAIRMOD_PROTECTED o = new Point_HAIRMOD_PROTECTED(ownerX, ownerY);
			oldPosX = ownerX;
			oldPosY = ownerY;
			wayPoint.addElement(o);
		}
		else if (Util_HAIRMOD_PROTECTED.getDistance(x, y, xAnchor, yAnchor) < 40)
		{
			wayPoint.removeAllElements();
			setState(4);
		}
		if (wayPoint.size() > 0 && wayPoint.elementAt(0) != null)
		{
			toX = ((Point_HAIRMOD_PROTECTED)wayPoint.elementAt(0)).x;
			toY = ((Point_HAIRMOD_PROTECTED)wayPoint.elementAt(0)).y;
			setHuong(toX, toY);
			move_to_XY();
		}
	}

	public void move_to_XY()
	{
		int num = Math_HAIRMOD_PROTECTED.abs(x - toX);
		int num2 = Math_HAIRMOD_PROTECTED.abs(y - toY);
		if (num <= speed)
		{
			x = (short)toX;
		}
		if (num2 < speed)
		{
			y = (short)toY;
		}
		if (x < toX)
		{
			x += speed;
			dir = Char_HAIRMOD_PROTECTED.RIGHT;
		}
		else if (x > toX)
		{
			x -= speed;
			dir = Char_HAIRMOD_PROTECTED.LEFT;
		}
		else if (y > toY)
		{
			y -= speed;
			dir = Char_HAIRMOD_PROTECTED.UP;
		}
		else if (y < toY)
		{
			dir = Char_HAIRMOD_PROTECTED.DOWN;
			y += speed;
		}
		huongY = (sbyte)((vy > 0) ? 1 : 0);
		flip = (sbyte)((vx <= 0) ? 2 : 0);
		flip = 0;
		if (dir == Char_HAIRMOD_PROTECTED.RIGHT)
		{
			flip = 2;
		}
		else if (dir != Char_HAIRMOD_PROTECTED.LEFT)
		{
			if (dir == Char_HAIRMOD_PROTECTED.DOWN)
			{
				huongY = 0;
			}
			else if (dir == Char_HAIRMOD_PROTECTED.UP)
			{
				huongY = 1;
			}
		}
		if (state != 3 && (owner.getDir() == Char_HAIRMOD_PROTECTED.UP || owner.getDir() == Char_HAIRMOD_PROTECTED.DOWN))
		{
			xtam += vatam;
			if (vatam > 0)
			{
				dir = 3;
			}
			if (vatam < 0)
			{
				dir = 2;
			}
			if ((xtam + vatam >= 48 && vatam > 0) || (xtam + vatam < -48 && vatam < 0))
			{
				vatam *= -1;
			}
		}
	}

	public void initAttackState(sbyte skillId)
	{
		try
		{
			attackType = skillId;
			isRunAttack = true;
			timeBeginMoveAttack = mSystem_HAIRMOD_PROTECTED.currentTimeMillis();
			attackCount = 0;
			isSequenceAttack = false;
		}
		catch (Exception)
		{
		}
	}

	public override void petAttack(mVector_HAIRMOD_PROTECTED target, sbyte type, short idSkill, int[] dame, int[] hpcon)
	{
		state = 2;
		ntarget = target;
		typeAttack = type;
		this.idSkill = idSkill;
		arrdame = dame;
		arrhpcon = hpcon;
	}

	protected new void attack()
	{
		if (ntarget != null && ntarget.size() > 0)
		{
			Actor_HAIRMOD_PROTECTED actor_HAIRMOD_PROTECTED = (Actor_HAIRMOD_PROTECTED)ntarget.elementAt(0);
			if (actor_HAIRMOD_PROTECTED == null)
			{
				setState(1);
				return;
			}
			if (Util_HAIRMOD_PROTECTED.getDistance(x + speed, y + speed, actor_HAIRMOD_PROTECTED.x, actor_HAIRMOD_PROTECTED.y) > range && !isBeginAttack && actor_HAIRMOD_PROTECTED.getHp() > 0)
			{
				toX = actor_HAIRMOD_PROTECTED.x;
				toY = actor_HAIRMOD_PROTECTED.y;
				setHuong(toX, toY);
				move_to_XY();
				mAction = 2;
				if (dyattack < 40 && isFly())
				{
					dyattack++;
				}
			}
			else
			{
				isBeginAttack = true;
			}
			if (!isBeginAttack)
			{
				return;
			}
			if (dyattack < 50 && isFly())
			{
				dyattack += 10;
			}
			p1++;
			if (p1 <= 6)
			{
				return;
			}
			p1 = 0;
			isBeginAttack = false;
			setState(4);
			for (int i = 0; i < ntarget.size(); i++)
			{
				Actor_HAIRMOD_PROTECTED actor_HAIRMOD_PROTECTED2 = (Actor_HAIRMOD_PROTECTED)ntarget.elementAt(i);
				if (actor_HAIRMOD_PROTECTED2 != null)
				{
					Canvas_HAIRMOD_PROTECTED.gameScr.startFlyText(arrdame[i] + string.Empty, 0, actor_HAIRMOD_PROTECTED2.x, actor_HAIRMOD_PROTECTED2.y - 15, 1, -2);
					actor_HAIRMOD_PROTECTED2.setHP(arrhpcon[i]);
					actor_HAIRMOD_PROTECTED2.addEffectSkillTime(idSkill, actor_HAIRMOD_PROTECTED2.x, actor_HAIRMOD_PROTECTED2.y, 0L, false, false, false, 0, 0, 0);
				}
			}
		}
		else
		{
			p1++;
			isBeginAttack = true;
			if (p1 > 6)
			{
				p1 = 0;
				isBeginAttack = false;
				setState(4);
			}
		}
	}

	protected void returnToPlayer()
	{
		vMax = owner.getSpeed();
		toX = owner.x;
		toY = owner.y;
		setHuong(toX, toY);
		move_to_XY();
		if (Util_HAIRMOD_PROTECTED.getDistance(x, y, xAnchor, yAnchor) < 40 && owner.getState() != 2)
		{
			setState(0);
		}
	}

	protected void waitForCommand()
	{
		vx = 0;
		vy = 0;
		Action = 1;
		if (owner != null)
		{
			if (owner.getState() == 0)
			{
				wayPoint.removeAllElements();
				setState(0);
			}
			else if (owner.getState() == 1 && Util_HAIRMOD_PROTECTED.getDistance(x, y, xAnchor, yAnchor) > 40)
			{
				wayPoint.removeAllElements();
				setState(1);
			}
		}
	}

	public void addAttackEffect()
	{
	}

	private void attract()
	{
		vMax = 3;
		state = 1;
		toX = Canvas_HAIRMOD_PROTECTED.gameScr.mainChar.x;
		toY = Canvas_HAIRMOD_PROTECTED.gameScr.mainChar.y;
		move_to_XY();
		if (Util_HAIRMOD_PROTECTED.getDistance(x, y, toX, toY) < 40)
		{
			setState(0);
		}
	}

	public void doChangeFrmaeBoss()
	{
		mDataEffect_HAIRMOD_PROTECTED mDataEffect_HAIRMOD_PROTECTED2 = (mDataEffect_HAIRMOD_PROTECTED)ALL_PET_DATA.get(string.Empty + idTemplate);
		if (mDataEffect_HAIRMOD_PROTECTED2 != null)
		{
			int action = mAction;
			if (state == 2 && isBeginAttack)
			{
				action = 3;
			}
			frame = (sbyte)((frame + 1) % mDataEffect_HAIRMOD_PROTECTED2.getAnim(action, huongY).frame.Length);
			if (maxAttack == 0)
			{
				maxAttack = mDataEffect_HAIRMOD_PROTECTED2.getAnim(3, huongY).frame.Length;
			}
		}
	}

	public void updateMenu()
	{
		if (Canvas_HAIRMOD_PROTECTED.gameTick % 2 == 0)
		{
			f = (sbyte)((f + 1) % 3);
		}
		if (state != 2)
		{
			x += (short)vx;
			y += (short)vy;
		}
		doChangeFrmaeBoss();
		if (Canvas_HAIRMOD_PROTECTED.gameTick % 200 == 0)
		{
			state = 2;
		}
		if (state == 2)
		{
			attack();
		}
	}

	public override void update()
	{
		try
		{
			if (!isBeginAttack && dyattack > 0)
			{
				dyattack -= 3;
				if (dyattack < 0)
				{
					dyattack = 0;
				}
			}
			if (Canvas_HAIRMOD_PROTECTED.gameTick % 2 == 0)
			{
				f = (sbyte)((f + 1) % 3);
			}
			if (state != 2)
			{
				x += (short)vx;
				y += (short)vy;
			}
			doChangeFrmaeBoss();
			if (owner != null)
			{
				if (Util_HAIRMOD_PROTECTED.getDistance(x, y, toX, toY) <= 10 && wayPoint.size() > 0)
				{
					wayPoint.removeElementAt(0);
				}
				xAnchor = owner.x;
				yAnchor = owner.y;
				ownerX = owner.x;
				ownerY = owner.y;
			}
			time++;
			if (state != preState)
			{
				preState = state;
			}
			switch (state)
			{
			case 0:
				roam();
				break;
			case 1:
				mAction = 2;
				follow();
				break;
			case 2:
				attack();
				break;
			case 3:
				returnToPlayer();
				mAction = 2;
				break;
			case 4:
				mAction = 0;
				waitForCommand();
				break;
			case 6:
				attract();
				break;
			case 5:
				break;
			}
		}
		catch (Exception ex)
		{
			ex.ToString();
		}
	}

	public bool canFocus()
	{
		return false;
	}

	public void startAttack(mVector_HAIRMOD_PROTECTED target, int idskill)
	{
		state = 2;
	}

	public void setidTemplatePet(short id)
	{
		idTemplate = id;
	}

	public void petAttack(sbyte idSkill, mVector_HAIRMOD_PROTECTED ntarget, int[] dame, short range)
	{
		this.range = range;
		this.idSkill = idSkill;
		this.ntarget = ntarget;
		state = 2;
		dyattack = 0;
	}

	public bool isMypet()
	{
		return owner.ID == Canvas_HAIRMOD_PROTECTED.gameScr.mainChar.ID;
	}

	public int getIDTem()
	{
		return idTemplate;
	}

	public new void setPosTo(short x, short y)
	{
	}

	public short getOwnerID()
	{
		return ID;
	}

	public override bool ispetTool()
	{
		return true;
	}
}
