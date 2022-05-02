package net.haspamelodica.studentcodeseparator.utils.communication;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public interface Communication extends AutoCloseable
{
	public boolean getLogging();
	public InputStream getIn();
	public OutputStream getOut();

	@Override
	public void close() throws IOException;

	public static Communication open(CommunicationParams params) throws IOException, InterruptedException
	{
		return new CommunicationImpl(params);
	}
}
