package helloworld;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import net.haspamelodica.charon.StudentSide;
import net.haspamelodica.charon.junitextension.CharonExtension;

@ExtendWith(CharonExtension.class)
public class HelloWorldTest
{
	private final HelloWorld.Prototype HelloWorld;

	public HelloWorldTest(StudentSide studentSide)
	{
		this.HelloWorld = studentSide.createPrototype(HelloWorld.Prototype.class);
	}

	@Test
	public void testHelloWorld()
	{
		assertEquals("Hello, World!", HelloWorld.helloWorld());
	}
}
