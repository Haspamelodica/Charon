package maze;

import java.util.Arrays;

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
		private final boolean[][] maze;

		private int	startX, startY;
		private int	targetX, targetY;

		public MazeBuilder(int width, int height)
		{
			this.maze = new boolean[width + 2][height + 2];
			for(int i = 1; i < width + 1; i ++)
				Arrays.fill(maze[i], 1, height + 1, true);
		}

		/** You may assume this method is only called once */
		public MazeBuilder setStart(int x, int y)
		{
			startX = x + 1;
			startY = y + 1;
			return this;
		}

		/** You may assume this method is only called once */
		public MazeBuilder setTarget(int x, int y)
		{
			targetX = x + 1;
			targetY = y + 1;
			return this;
		}

		public MazeBuilder setWall(int x, int y)
		{
			maze[x + 1][y + 1] = false;
			return this;
		}

		public Maze build()
		{
			return new MazeImpl(maze, targetX, targetY, startX, startY);
		}
	}
}
