package net.haspamelodica.charon.utils;

import java.io.IOException;

@FunctionalInterface
public interface IORunnable
{
	public void run() throws IOException;
}
