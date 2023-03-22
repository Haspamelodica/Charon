package maze;

public interface Maze
{
	public int distanceToTargetX();
	public int distanceToTargetY();
	public default boolean isSolved()
	{
		return distanceToTargetX() == 0 && distanceToTargetY() == 0;
	}

	public boolean canMove(int dx, int dy);
	public void move(int dx, int dy);

	public static MazeBuilder builder(int width, int height)
	{
		return new MazeBuilder(width, height);
	}

	public static class MazeBuilder
	{
		private MazeBuilder(int width, int height)
		{
			// implement your solution here
		}

		/** You may assume this method is only called once */
		public MazeBuilder setStart(int x, int y)
		{
			// implement your solution here
			return null;
		}

		/** You may assume this method is only called once */
		public MazeBuilder setTarget(int x, int y)
		{
			// implement your solution here
			return null;
		}

		public MazeBuilder setWall(int x, int y)
		{
			// implement your solution here
			return null;
		}

		public Maze build()
		{
			// implement your solution here
			return null;
		}
	}
}
