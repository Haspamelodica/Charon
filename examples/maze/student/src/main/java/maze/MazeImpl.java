package maze;

public class MazeImpl implements Maze
{
	private final boolean[][]	maze;
	private final int			targetX, targetY;

	private int x, y;

	public MazeImpl(boolean[][] maze, int targetX, int targetY, int startX, int startY)
	{
		this.maze = maze;
		this.targetX = targetX;
		this.targetY = targetY;
		this.x = startX;
		this.y = startY;
	}

	@Override
	public int distanceToTargetX()
	{
		return targetX - x;
	}

	@Override
	public int distanceToTargetY()
	{
		return targetY - y;
	}

	@Override
	public boolean canMove(int dx, int dy)
	{
		if(Math.abs(dx) + Math.abs(dy) != 1)
			return false;

		return maze[x + dx][y + dy];
	}

	@Override
	public void move(int dx, int dy)
	{
		if(!canMove(dx, dy))
			throw new IllegalArgumentException("Can't move by " + dx + ", " + dy);

		x += dx;
		y += dy;
	}
}
