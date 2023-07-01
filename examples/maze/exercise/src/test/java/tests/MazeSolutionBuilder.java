package tests;

import java.util.Arrays;

import maze.Maze;

public class MazeSolutionBuilder
{
	private final boolean[][] maze;

	private int	startX, startY;
	private int	targetX, targetY;

	public MazeSolutionBuilder(int width, int height)
	{
		this.maze = new boolean[width + 2][height + 2];
		for(int i = 1; i < width + 1; i ++)
			Arrays.fill(maze[i], 1, height + 1, true);
	}

	/** You may assume this method is only called once */
	public MazeSolutionBuilder setStart(int x, int y)
	{
		startX = x + 1;
		startY = y + 1;
		return this;
	}

	/** You may assume this method is only called once */
	public MazeSolutionBuilder setTarget(int x, int y)
	{
		targetX = x + 1;
		targetY = y + 1;
		return this;
	}

	public MazeSolutionBuilder setWall(int x, int y)
	{
		maze[x + 1][y + 1] = false;
		return this;
	}

	public Maze build()
	{
		return new MazeSolution(maze, targetX, targetY, startX, startY);
	}
}
