package net.haspamelodica.studentcodeseparator;

import java.lang.instrument.Instrumentation;

public class TestAgent
{
	public static void agentmain(String agentArgs, Instrumentation inst)
	{
		System.out.println("agentmain started!");
	}
}
