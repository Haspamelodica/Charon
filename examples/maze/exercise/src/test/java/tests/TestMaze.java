package tests;

import static net.haspamelodica.charon.junitextension.CharonJUnitUtils.assertStudentThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import maze.Maze;
import maze.MazeSolver;
import net.haspamelodica.charon.StudentSide;
import net.haspamelodica.charon.junitextension.CharonExtension;

@ExtendWith(CharonExtension.class)
public class TestMaze
{
	private final Maze.Prototype		Maze;
	private final MazeSolver.Prototype	MazeSolver;

	public TestMaze(Maze.Prototype Maze, MazeSolver.Prototype MazeSolver)
	{
		this.Maze = Maze;
		this.MazeSolver = MazeSolver;
	}

	@Test
	public void testBasicMazeSolverAgainstExerciseMazeImpl()
	{
		ExerciseMazeImpl maze = new ExerciseMazeImplBuilder(3, 1).setStart(0, 0).setTarget(2, 0).build();
		MazeSolver.solveMaze(maze);
		assertEquals(true, maze.isSolved());
	}

	@Test
	public void testBasicMazeSolverAgainstStudentMaze()
	{
		Maze maze = Maze.builder(3, 1).setStart(0, 0).setTarget(2, 0).build();
		MazeSolver.solveMaze(maze);
		assertEquals(true, maze.isSolved());
	}

	@Test
	public void testBasicMaze()
	{
		Maze maze = Maze.builder(3, 1).setStart(0, 0).setTarget(2, 0).build();

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

	@Test
	public void testSolveNPE(StudentSide studentSide)
	{
		assertStudentThrows(studentSide, NullPointerExceptionSSI.Prototype.class, () -> MazeSolver.solveMaze(null));
	}
}
