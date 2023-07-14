package maze;

import static net.haspamelodica.charon.annotations.StudentSideInstanceKind.Kind.CLASS;
import static net.haspamelodica.charon.annotations.StudentSidePrototypeMethodKind.Kind.STATIC_METHOD;

import net.haspamelodica.charon.StudentSideInstance;
import net.haspamelodica.charon.StudentSidePrototype;
import net.haspamelodica.charon.annotations.PrototypeClass;
import net.haspamelodica.charon.annotations.StudentSideInstanceKind;
import net.haspamelodica.charon.annotations.StudentSidePrototypeMethodKind;

@StudentSideInstanceKind(CLASS)
@PrototypeClass(MazeSolver.Prototype.class)
public interface MazeSolver extends StudentSideInstance
{
	public static interface Prototype extends StudentSidePrototype<MazeSolver>
	{
		@StudentSidePrototypeMethodKind(STATIC_METHOD)
		public void solveMaze(Maze maze);
	}
}
