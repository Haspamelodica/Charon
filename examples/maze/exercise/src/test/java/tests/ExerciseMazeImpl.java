package tests;

import maze.Maze;
import net.haspamelodica.charon.annotations.SafeForCallByStudent;
import net.haspamelodica.charon.annotations.UseSerDes;
import net.haspamelodica.charon.marshaling.StringSerDes;

public class ExerciseMazeImpl implements Maze
{
	private final boolean[][]	maze;
	private final int			targetX, targetY;

	private int x, y;

	public ExerciseMazeImpl(boolean[][] maze, int targetX, int targetY, int startX, int startY)
	{
		this.maze = maze;
		this.targetX = targetX;
		this.targetY = targetY;
		this.x = startX;
		this.y = startY;
	}

	@Override
	@SafeForCallByStudent
	public void move(int dx, int dy)
	{
		if(!canMove(dx, dy))
			throw new IllegalArgumentException("Can't move by " + dx + ", " + dy);
	
		x += dx;
		y += dy;
	}

	@Override
	@SafeForCallByStudent
	public boolean isSolved()
	{
		return distanceToTargetX() == 0 && distanceToTargetY() == 0;
	}

	@Override
	@SafeForCallByStudent
	public int distanceToTargetX()
	{
		return targetX - x;
	}

	@Override
	@SafeForCallByStudent
	public int distanceToTargetY()
	{
		return targetY - y;
	}

	@Override
	@SafeForCallByStudent
	public boolean canMove(int dx, int dy)
	{
		if(Math.abs(dx) + Math.abs(dy) != 1)
			return false;

		return maze[x + dx][y + dy];
	}

	// If we don't mark toString as callable by student,
	// debugging the student submission becomes hard because
	// many debuggers automatically call toString on relevant objects.
	@Override
	@SafeForCallByStudent
	@UseSerDes(StringSerDes.class)
	public String toString()
	{
		return "MazeSolution";
	}
}
