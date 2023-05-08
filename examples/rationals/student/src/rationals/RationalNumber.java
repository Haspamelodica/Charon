package rationals;

public class RationalNumber
{
	public static final RationalNumber ZERO = new RationalNumber(0);

	public final int num, den;

	public RationalNumber(int value)
	{
		this.num = value;
		this.den = 1;
	}
	public RationalNumber(int num, int den)
	{
		if(den == 0)
			throw new DivisionByZeroException("Trying to create a rational number with a denominator of 0 (numerator: " + num + ")");
		int gcd = gcd(num, den);
		this.num = num / gcd;
		this.den = den / gcd;
	}

	public double approximateAsDouble()
	{
		return num / (double) den;
	}

	public RationalNumber add(RationalNumber other)
	{
		return new RationalNumber(
				this.num * other.den + other.num * this.den,
				this.den * other.den);
	}

	public RationalNumber sub(RationalNumber other)
	{
		return new RationalNumber(
				this.num * other.den - other.num * this.den,
				this.den * other.den);
	}

	public RationalNumber mul(RationalNumber other)
	{
		return new RationalNumber(
				this.num * other.num,
				this.den * other.den);
	}

	public RationalNumber div(RationalNumber other)
	{
		return new RationalNumber(
				this.num * other.den,
				this.den * other.num);
	}

	private static int gcd(int a, int b)
	{
		return a <= 0 ? b : gcd(b % a, a);
	}
}
