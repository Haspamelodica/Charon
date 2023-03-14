package tests;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import maze.Maze;
import maze.Maze.MazeBuilder;
import maze.MazeSolver;
import net.haspamelodica.charon.StudentSide;
import net.haspamelodica.charon.junitextension.CharonExtension;

@ExtendWith(CharonExtension.class)
public class TestMaze
{
	private static Maze.Prototype		MazeP;
	private static MazeSolver.Prototype	MazeSolverP;

	@BeforeAll
	public static void setupPrototypes(StudentSide studentSide)
	{
		MazeP = studentSide.createPrototype(Maze.Prototype.class);
		studentSide.createPrototype(MazeBuilder.Prototype.class);
		MazeSolverP = studentSide.createPrototype(MazeSolver.Prototype.class);
	}

	@Test
	public void testBasicMazeSolverAgainstStudentMaze()
	{
		Maze maze = MazeP.builder(3, 1).setStart(0, 0).setTarget(2, 0).build();
		MazeSolverP.solveMaze(maze);
		assertEquals(true, maze.isSolved());
	}

	@Test
	public void testBasicMazeSolverAgainstSolution()
	{
		Maze maze = new MazeSolutionBuilder(3, 1).setStart(0, 0).setTarget(2, 0).build();
		MazeSolverP.solveMaze(maze);
		assertEquals(true, maze.isSolved());
	}

	@Test
	public void testBasicMaze()
	{
		Maze maze = MazeP.builder(3, 1).setStart(0, 0).setTarget(2, 0).build();

		assertEquals(false, maze.isSolved());
		assertEquals(false, maze.canMove(0, 1));
		assertEquals(false, maze.canMove(0, -1));
		assertEquals(true, maze.canMove(1, 0));
		assertEquals(false, maze.canMove(-1, 0));

		maze.move(1, 0);

		assertEquals(false, maze.isSolved());
		assertEquals(false, maze.canMove(0, 1));
		assertEquals(false, maze.canMove(0, -1));
		assertEquals(true, maze.canMove(1, 0));
		assertEquals(true, maze.canMove(-1, 0));

		maze.move(1, 0);

		assertEquals(true, maze.isSolved());
		assertEquals(false, maze.canMove(0, 1));
		assertEquals(false, maze.canMove(0, -1));
		assertEquals(false, maze.canMove(1, 0));
		assertEquals(true, maze.canMove(-1, 0));
	}
}
