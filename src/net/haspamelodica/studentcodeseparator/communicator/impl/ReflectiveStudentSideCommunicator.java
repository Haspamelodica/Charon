package net.haspamelodica.studentcodeseparator.communicator.impl;

import net.haspamelodica.studentcodeseparator.communicator.StudentSideCommunicator;
import net.haspamelodica.studentcodeseparator.communicator.impl.ReflectiveStudentSideCommunicator.Ref;

public class ReflectiveStudentSideCommunicator implements StudentSideCommunicator<Ref>
{
	@Override
	public Ref callConstructor(Class<?>[] paramTypes, Object... args)
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <R> R callStaticMethod(String name, Class<R> returnType, Class<?>[] paramTypes, Object... args)
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <F> F getStaticField(String name, Class<F> fieldType)
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <F> void setStaticField(String name, Class<F> fieldType, F value)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public <R> R callInstanceMethod(String name, Class<R> returnType, Class<?>[] paramTypes, Ref ref, Object... args)
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <F> F getField(String name, Class<F> fieldType, Ref ref)
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <F> void setField(String name, Class<F> fieldType, Ref ref, F value)
	{
		// TODO Auto-generated method stub

	}

	static class Ref
	{

	}
}
