package net.haspamelodica.charon.serialization;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.List;

public sealed abstract class PrimitiveSerDes<T> implements SerDes<T>
{
	public final static List<Class<? extends SerDes<?>>> PRIMITIVE_SERDESES = List.of(BooleanSerDes.class,
			CharSerDes.class, ByteSerDes.class, ShortSerDes.class, IntSerDes.class, LongSerDes.class,
			FloatSerDes.class, DoubleSerDes.class, VoidSerDes.class);

	public final static class BooleanSerDes extends PrimitiveSerDes<Boolean>
	{
		@Override
		public Class<Boolean> getHandledClass()
		{
			return boolean.class;
		}
		@Override
		public void serialize(DataOutput out, Boolean obj) throws IOException
		{
			out.writeBoolean(obj);
		}
		@Override
		public Boolean deserialize(DataInput in) throws IOException
		{
			return in.readBoolean();
		}
	}
	public final static class CharSerDes extends PrimitiveSerDes<Character>
	{
		@Override
		public Class<Character> getHandledClass()
		{
			return char.class;
		}
		@Override
		public void serialize(DataOutput out, Character obj) throws IOException
		{
			out.writeChar(obj);
		}
		@Override
		public Character deserialize(DataInput in) throws IOException
		{
			return in.readChar();
		}
	}
	public final static class ByteSerDes extends PrimitiveSerDes<Byte>
	{
		@Override
		public Class<Byte> getHandledClass()
		{
			return byte.class;
		}
		@Override
		public void serialize(DataOutput out, Byte obj) throws IOException
		{
			out.writeByte(obj);
		}
		@Override
		public Byte deserialize(DataInput in) throws IOException
		{
			return in.readByte();
		}
	}
	public final static class ShortSerDes extends PrimitiveSerDes<Short>
	{
		@Override
		public Class<Short> getHandledClass()
		{
			return short.class;
		}
		@Override
		public void serialize(DataOutput out, Short obj) throws IOException
		{
			out.writeShort(obj);
		}
		@Override
		public Short deserialize(DataInput in) throws IOException
		{
			return in.readShort();
		}
	}
	public final static class IntSerDes extends PrimitiveSerDes<Integer>
	{
		@Override
		public Class<Integer> getHandledClass()
		{
			return int.class;
		}
		@Override
		public void serialize(DataOutput out, Integer obj) throws IOException
		{
			out.writeInt(obj);
		}
		@Override
		public Integer deserialize(DataInput in) throws IOException
		{
			return in.readInt();
		}
	}
	public final static class LongSerDes extends PrimitiveSerDes<Long>
	{
		@Override
		public Class<Long> getHandledClass()
		{
			return long.class;
		}
		@Override
		public void serialize(DataOutput out, Long obj) throws IOException
		{
			out.writeLong(obj);
		}
		@Override
		public Long deserialize(DataInput in) throws IOException
		{
			return in.readLong();
		}
	}
	public final static class FloatSerDes extends PrimitiveSerDes<Float>
	{
		@Override
		public Class<Float> getHandledClass()
		{
			return float.class;
		}
		@Override
		public void serialize(DataOutput out, Float obj) throws IOException
		{
			out.writeFloat(obj);
		}
		@Override
		public Float deserialize(DataInput in) throws IOException
		{
			return in.readFloat();
		}
	}
	public final static class DoubleSerDes extends PrimitiveSerDes<Double>
	{
		@Override
		public Class<Double> getHandledClass()
		{
			return double.class;
		}
		@Override
		public void serialize(DataOutput out, Double obj) throws IOException
		{
			out.writeDouble(obj);
		}
		@Override
		public Double deserialize(DataInput in) throws IOException
		{
			return in.readDouble();
		}
	}
	public final static class VoidSerDes extends PrimitiveSerDes<Void>
	{
		@Override
		public Class<Void> getHandledClass()
		{
			return void.class;
		}
		@Override
		public void serialize(DataOutput out, Void obj) throws IOException
		{
			// do nothing: obj can only be null
			if(obj != null)
				//TODO better exception type
				throw new RuntimeException("Got an instance of Void");
		}
		@Override
		public Void deserialize(DataInput in) throws IOException
		{
			// null is the only possible void value
			return null;
		}
	}
}
