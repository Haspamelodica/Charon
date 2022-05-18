package net.haspamelodica.charon.serialization;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public class StringSerDes implements SerDes<String>
{
	@Override
	public Class<String> getHandledClass()
	{
		return String.class;
	}
	@Override
	public void serialize(DataOutput out, String obj) throws IOException
	{
		out.writeUTF(obj);
	}
	@Override
	public String deserialize(DataInput in) throws IOException
	{
		return in.readUTF();
	}
}
