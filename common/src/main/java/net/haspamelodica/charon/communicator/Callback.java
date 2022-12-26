package net.haspamelodica.charon.communicator;

import java.util.List;

import net.haspamelodica.charon.refs.Ref;

public interface Callback
{
	public Ref callInstanceMethod(String cn, String name, String returnClassname, List<String> params, Ref receiverRef, List<Ref> argRefs);
}
