package net.haspamelodica.studentcodeseparator.serialization;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class StringSerializer implements Serializer<String>
{
	@Override
	public Class<String> getHandledClass()
	{
		return String.class;
	}
	@Override
	public void serialize(DataOutputStream out, String obj) throws IOException
	{
		out.writeUTF(obj);
	}
	@Override
	public String deserialize(DataInputStream in) throws IOException
	{
		return in.readUTF();
	}
}