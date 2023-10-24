package rationals;

import static net.haspamelodica.charon.junitextension.CharonJUnitUtils.assertStudentThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import net.haspamelodica.charon.StudentSide;
import net.haspamelodica.charon.junitextension.CharonExtension;

@ExtendWith(CharonExtension.class)
public record TestRationalNumber(RationalNumber.Prototype RationalNumber)
{
	@Test
	public void testBasic()
	{
		RationalNumber zero = RationalNumber.ZERO();
		assertEquals(zero.approximateAsDouble(), 0);

		RationalNumber rational42 = RationalNumber.new_(42);
		assertEquals(rational42.num(), 42);
		assertEquals(rational42.den(), 1);
		assertEquals(rational42.approximateAsDouble(), 42);

		RationalNumber rational50 = rational42.add(RationalNumber.new_(8));
		assertEquals(rational50.num(), 50);
		assertEquals(rational50.den(), 1);
		assertEquals(rational50.approximateAsDouble(), 50);

		RationalNumber rational25_9 = rational50.div(RationalNumber.new_(18));
		assertEquals(rational25_9.num(), 25);
		assertEquals(rational25_9.den(), 9);
		assertEquals(rational25_9.approximateAsDouble(), 25 / 9d);

		RationalNumber rational125_6 = rational25_9.mul(RationalNumber.new_(15, 2));
		assertEquals(rational125_6.num(), 125);
		assertEquals(rational125_6.den(), 6);
		assertEquals(rational125_6.approximateAsDouble(), 125 / 6d);

		RationalNumber rational0 = rational125_6.mul(zero);
		assertEquals(rational0.approximateAsDouble(), 0);
	}

	@Test
	public void testDiv0(StudentSide studentSide)
	{
		assertStudentThrows(studentSide, DivisionByZeroException.Prototype.class,
				() -> RationalNumber.new_(42).div(RationalNumber.ZERO()));
	}
}
