using System;

public class MyRandom_HAIRMOD_PROTECTED
{
	public Random r;

	public MyRandom_HAIRMOD_PROTECTED()
	{
		r = new Random();
	}

	public int nextInt()
	{
		return r.Next();
	}

	public int nextInt(int a)
	{
		return r.Next(a);
	}

	public int nextInt(int a, int b)
	{
		return r.Next(a, b);
	}
}
