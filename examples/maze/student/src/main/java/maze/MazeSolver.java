package maze;

import java.util.HashSet;
import java.util.Set;

public class MazeSolver
{
	public static void solveMaze(Maze maze)
	{
		solveMazeDFS(maze, 0, 0, new HashSet<>());
	}

	private static boolean solveMazeDFS(Maze maze, int x, int y, Set<Position> visitedPositions)
	{
		if(!visitedPositions.add(new Position(x, y)))
			return false;

		if(maze.isSolved())
			return true;

		return false
				|| trySolve(maze, x, y, visitedPositions, 0, 1)
				|| trySolve(maze, x, y, visitedPositions, 0, -1)
				|| trySolve(maze, x, y, visitedPositions, 1, 0)
				|| trySolve(maze, x, y, visitedPositions, -1, 0);
	}

	private static boolean trySolve(Maze maze, int x, int y, Set<Position> visitedPositions, int dx, int dy)
	{
		if(!maze.canMove(dx, dy))
			return false;

		maze.move(dx, dy);

		if(solveMazeDFS(maze, x + dx, y + dy, visitedPositions))
			return true;

		maze.move(-dx, -dy);

		return false;
	}

	private static record Position(int x, int y)
	{}
}
