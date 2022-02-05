package net.haspamelodica.studentcodeseparator.serialization;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.List;

public sealed abstract class PrimitiveSerializer<T> implements Serializer<T>
{
	public final static List<Class<? extends Serializer<?>>> PRIMITIVE_SERIALIZERS = List.of(BooleanSerializer.class,
			CharacterSerializer.class, ByteSerializer.class, ShortSerializer.class, IntegerSerializer.class, LongSerializer.class,
			FloatSerializer.class, DoubleSerializer.class, VoidSerializer.class);

	public final static class BooleanSerializer extends PrimitiveSerializer<Boolean>
	{
		@Override
		public Class<Boolean> getHandledClass()
		{
			return boolean.class;
		}
		@Override
		public void serialize(DataOutputStream out, Boolean obj) throws IOException
		{
			out.writeBoolean(obj);
		}
		@Override
		public Boolean deserialize(DataInputStream in) throws IOException
		{
			return in.readBoolean();
		}
	}
	public final static class CharacterSerializer extends PrimitiveSerializer<Character>
	{
		@Override
		public Class<Character> getHandledClass()
		{
			return char.class;
		}
		@Override
		public void serialize(DataOutputStream out, Character obj) throws IOException
		{
			out.writeChar(obj);
		}
		@Override
		public Character deserialize(DataInputStream in) throws IOException
		{
			return in.readChar();
		}
	}
	public final static class ByteSerializer extends PrimitiveSerializer<Byte>
	{
		@Override
		public Class<Byte> getHandledClass()
		{
			return byte.class;
		}
		@Override
		public void serialize(DataOutputStream out, Byte obj) throws IOException
		{
			out.writeByte(obj);
		}
		@Override
		public Byte deserialize(DataInputStream in) throws IOException
		{
			return in.readByte();
		}
	}
	public final static class ShortSerializer extends PrimitiveSerializer<Short>
	{
		@Override
		public Class<Short> getHandledClass()
		{
			return short.class;
		}
		@Override
		public void serialize(DataOutputStream out, Short obj) throws IOException
		{
			out.writeShort(obj);
		}
		@Override
		public Short deserialize(DataInputStream in) throws IOException
		{
			return in.readShort();
		}
	}
	public final static class IntegerSerializer extends PrimitiveSerializer<Integer>
	{
		@Override
		public Class<Integer> getHandledClass()
		{
			return int.class;
		}
		@Override
		public void serialize(DataOutputStream out, Integer obj) throws IOException
		{
			out.writeInt(obj);
		}
		@Override
		public Integer deserialize(DataInputStream in) throws IOException
		{
			return in.readInt();
		}
	}
	public final static class LongSerializer extends PrimitiveSerializer<Long>
	{
		@Override
		public Class<Long> getHandledClass()
		{
			return long.class;
		}
		@Override
		public void serialize(DataOutputStream out, Long obj) throws IOException
		{
			out.writeLong(obj);
		}
		@Override
		public Long deserialize(DataInputStream in) throws IOException
		{
			return in.readLong();
		}
	}
	public final static class FloatSerializer extends PrimitiveSerializer<Float>
	{
		@Override
		public Class<Float> getHandledClass()
		{
			return float.class;
		}
		@Override
		public void serialize(DataOutputStream out, Float obj) throws IOException
		{
			out.writeFloat(obj);
		}
		@Override
		public Float deserialize(DataInputStream in) throws IOException
		{
			return in.readFloat();
		}
	}
	public final static class DoubleSerializer extends PrimitiveSerializer<Double>
	{
		@Override
		public Class<Double> getHandledClass()
		{
			return double.class;
		}
		@Override
		public void serialize(DataOutputStream out, Double obj) throws IOException
		{
			out.writeDouble(obj);
		}
		@Override
		public Double deserialize(DataInputStream in) throws IOException
		{
			return in.readDouble();
		}
	}
	public final static class VoidSerializer extends PrimitiveSerializer<Void>
	{
		@Override
		public Class<Void> getHandledClass()
		{
			return void.class;
		}
		@Override
		public void serialize(DataOutputStream out, Void obj) throws IOException
		{
			//do nothing: obj can only be null
			if(obj != null)
				throw new IllegalStateException("Got an instance of Void");
		}
		@Override
		public Void deserialize(DataInputStream in) throws IOException
		{
			//null is the only possible void value
			return null;
		}
	}
}