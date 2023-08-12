package maze;

import static net.haspamelodica.charon.annotations.StudentSideInstanceKind.Kind.CLASS;
import static net.haspamelodica.charon.annotations.StudentSideInstanceKind.Kind.INTERFACE;
import static net.haspamelodica.charon.annotations.StudentSideInstanceMethodKind.Kind.INSTANCE_METHOD;
import static net.haspamelodica.charon.annotations.StudentSidePrototypeMethodKind.Kind.STATIC_METHOD;

import net.haspamelodica.charon.StudentSideInstance;
import net.haspamelodica.charon.StudentSidePrototype;
import net.haspamelodica.charon.annotations.PrototypeClass;
import net.haspamelodica.charon.annotations.StudentSideInstanceKind;
import net.haspamelodica.charon.annotations.StudentSideInstanceMethodKind;
import net.haspamelodica.charon.annotations.StudentSidePrototypeMethodKind;
import net.haspamelodica.charon.annotations.UseSerDes;
import net.haspamelodica.charon.marshaling.StringSerDes;

@StudentSideInstanceKind(INTERFACE)
@PrototypeClass(Maze.Prototype.class)
public interface Maze extends StudentSideInstance
{
	@StudentSideInstanceMethodKind(INSTANCE_METHOD)
	public boolean canMove(int dx, int dy);
	@StudentSideInstanceMethodKind(INSTANCE_METHOD)
	public void move(int dx, int dy);

	@StudentSideInstanceMethodKind(INSTANCE_METHOD)
	public int distanceToTargetX();
	@StudentSideInstanceMethodKind(INSTANCE_METHOD)
	public int distanceToTargetY();
	@StudentSideInstanceMethodKind(INSTANCE_METHOD)
	public boolean isSolved();

	public static interface Prototype extends StudentSidePrototype<Maze>
	{
		@StudentSidePrototypeMethodKind(STATIC_METHOD)
		@UseSerDes(StringSerDes.class)
		public MazeBuilder builder(int width, int height);
	}

	@StudentSideInstanceKind(CLASS)
	@PrototypeClass(MazeBuilder.Prototype.class)
	public static interface MazeBuilder extends StudentSideInstance
	{
		/** You may assume this method is only called once */
		@StudentSideInstanceMethodKind(INSTANCE_METHOD)
		public MazeBuilder setStart(int x, int y);

		/** You may assume this method is only called once */
		@StudentSideInstanceMethodKind(INSTANCE_METHOD)
		public MazeBuilder setTarget(int x, int y);

		@StudentSideInstanceMethodKind(INSTANCE_METHOD)
		public MazeBuilder setWall(int x, int y);

		@StudentSideInstanceMethodKind(INSTANCE_METHOD)
		public Maze build();

		public static interface Prototype extends StudentSidePrototype<MazeBuilder>
		{}
	}
}
