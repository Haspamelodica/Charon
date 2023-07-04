package net.haspamelodica.charon.utils;

import java.io.IOException;

public interface IOConsumer<A>
{
	public void accept(A a) throws IOException;
}
