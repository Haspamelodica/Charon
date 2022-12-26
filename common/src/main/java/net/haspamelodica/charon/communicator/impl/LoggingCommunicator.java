package net.haspamelodica.charon.communicator.impl;

import java.util.List;
import java.util.stream.Collectors;

import net.haspamelodica.charon.communicator.Callback;
import net.haspamelodica.charon.communicator.StudentSideCommunicator;
import net.haspamelodica.charon.refs.Ref;

public class LoggingCommunicator<COMM extends StudentSideCommunicator> implements StudentSideCommunicator
{
	protected final COMM communicator;

	private final String prefix;

	public LoggingCommunicator(COMM communicator)
	{
		this(communicator, "");
	}
	public LoggingCommunicator(COMM communicator, String prefix)
	{
		this.communicator = communicator;
		this.prefix = prefix;
	}

	public static <COMM extends StudentSideCommunicator> StudentSideCommunicator maybeWrapLogging(COMM communicator, String prefix, boolean logging)
	{
		if(logging)
			return new LoggingCommunicator<>(communicator, prefix);
		return communicator;
	}
	public static <COMM extends StudentSideCommunicator> StudentSideCommunicator maybeWrapLogging(COMM communicator, boolean logging)
	{
		if(logging)
			return new LoggingCommunicator<>(communicator);
		return communicator;
	}

	@Override
	public String getStudentSideClassname(Ref ref)
	{
		log("classname " + ref);
		return communicator.getStudentSideClassname(ref);
	}

	@Override
	public Ref callConstructor(String cn, List<String> params, List<Ref> argRefs)
	{
		log("new " + cn + params.stream().collect(Collectors.joining(", ", "(", ")")) + ": " + argRefs.toString());
		return communicator.callConstructor(cn, params, argRefs);
	}

	@Override
	public Ref callStaticMethod(String cn, String name, String returnClassname, List<String> params, List<Ref> argRefs)
	{
		log(returnClassname + " " + cn + "." + name + params.stream().collect(Collectors.joining(", ", "(", ")")) + ": " + argRefs.toString());
		return communicator.callStaticMethod(cn, name, returnClassname, params, argRefs);
	}
	@Override
	public Ref getStaticField(String cn, String name, String fieldClassname)
	{
		log(fieldClassname + " " + cn + "." + name);
		return communicator.getStaticField(cn, name, fieldClassname);
	}
	@Override
	public void setStaticField(String cn, String name, String fieldClassname, Ref valueRef)
	{
		log(fieldClassname + " " + cn + "." + name + " = " + valueRef);
		communicator.setStaticField(cn, name, fieldClassname, valueRef);
	}

	@Override
	public Ref callInstanceMethod(String cn, String name, String returnClassname, List<String> params, Ref receiverRef, List<Ref> argRefs)
	{
		log(returnClassname + " " + cn + "." + name + params.stream().collect(Collectors.joining(", ", "(", ")")) + ": " + receiverRef + ", " + argRefs.toString());
		return communicator.callInstanceMethod(cn, name, returnClassname, params, receiverRef, argRefs);
	}
	@Override
	public Ref getInstanceField(String cn, String name, String fieldClassname, Ref receiverRef)
	{
		log(fieldClassname + " " + cn + "." + name + ": " + receiverRef);
		return communicator.getInstanceField(cn, name, fieldClassname, receiverRef);
	}
	@Override
	public void setInstanceField(String cn, String name, String fieldClassname, Ref receiverRef, Ref valueRef)
	{
		log(fieldClassname + " " + cn + "." + name + ": " + receiverRef + " = " + valueRef);
		communicator.setInstanceField(cn, name, fieldClassname, receiverRef, valueRef);
	}

	@Override
	public Ref createCallbackInstance(String interfaceName, Callback callback)
	{
		log("new callback " + interfaceName);
		return communicator.createCallbackInstance(interfaceName, callback);
	}

	protected void log(String message)
	{
		System.err.println(prefix + message);
	}
}
