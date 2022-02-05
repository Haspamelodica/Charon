package net.haspamelodica.studentcodeseparator.communicator.impl;

import java.util.List;
import java.util.stream.Collectors;

import net.haspamelodica.studentcodeseparator.communicator.StudentSideCommunicator;
import net.haspamelodica.studentcodeseparator.serialization.Serializer;

public class LoggingCommunicator<REF> implements StudentSideCommunicator<REF>
{
	private final StudentSideCommunicator<REF> communicator;

	public LoggingCommunicator(StudentSideCommunicator<REF> communicator)
	{
		this.communicator = communicator;
	}

	@Override
	public String getStudentSideClassname(REF ref)
	{
		System.err.println("classname " + ref);
		return communicator.getStudentSideClassname(ref);
	}

	@Override
	public <T> REF send(Serializer<T> serializer, REF serializerRef, T obj)
	{
		System.err.println("send " + serializer.getHandledClass() + ": " + serializer + ", " + serializerRef + ", " + obj);
		return communicator.send(serializer, serializerRef, obj);
	}
	@Override
	public <T> T receive(Serializer<T> serializer, REF serializerRef, REF objRef)
	{
		System.err.println("recv " + serializer.getHandledClass() + ": " + serializer + ", " + serializerRef + ", " + objRef);
		return communicator.receive(serializer, serializerRef, objRef);
	}

	@Override
	public REF callConstructor(String cn, List<String> params, List<REF> argRefs)
	{
		System.err.println("new " + cn + params.stream().collect(Collectors.joining(", ", "(", ")")) + ": " + argRefs.toString());
		return communicator.callConstructor(cn, params, argRefs);
	}

	@Override
	public REF callStaticMethod(String cn, String name, String returnClassname, List<String> params, List<REF> argRefs)
	{
		System.err.println(returnClassname + " " + cn + "." + name + params.stream().collect(Collectors.joining(", ", "(", ")")) + ": " + argRefs.toString());
		return communicator.callStaticMethod(cn, name, returnClassname, params, argRefs);
	}

	@Override
	public REF getStaticField(String cn, String name, String fieldClassname)
	{
		System.err.println(fieldClassname + " " + cn + "." + name);
		return communicator.getStaticField(cn, name, fieldClassname);
	}

	@Override
	public void setStaticField(String cn, String name, String fieldClassname, REF valueRef)
	{
		System.err.println(fieldClassname + " " + cn + "." + name + " = " + valueRef);
		communicator.setStaticField(cn, name, fieldClassname, valueRef);
	}

	@Override
	public REF callInstanceMethod(String cn, String name, String returnClassname, List<String> params, REF receiverRef, List<REF> argRefs)
	{
		System.err.println(returnClassname + " " + cn + "." + name + params.stream().collect(Collectors.joining(", ", "(", ")")) + ": " + receiverRef + ", " + argRefs.toString());
		return communicator.callInstanceMethod(cn, name, returnClassname, params, receiverRef, argRefs);
	}

	@Override
	public REF getField(String cn, String name, String fieldClassname, REF receiverRef)
	{
		System.err.println(fieldClassname + " " + cn + "." + name + ": " + receiverRef);
		return communicator.getField(cn, name, fieldClassname, receiverRef);
	}

	@Override
	public void setField(String cn, String name, String fieldClassname, REF receiverRef, REF valueRef)
	{
		System.err.println(fieldClassname + " " + cn + "." + name + ": " + receiverRef + " = " + valueRef);
		communicator.setField(cn, name, fieldClassname, receiverRef, valueRef);
	}
}
