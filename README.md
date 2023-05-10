# Charon

A library for securely executing automated tests of Java code in a didactic context.

Work-in-progress.

## What is Charon for?

Suppose some programming exercise where students are to submit Java code,
which then should be tested automatically by some test code
written by the exercise creator.

However, student code is not trustworthy in general.
There are many motivations for students to submit malicious code,
for example to fake test results, to read other student's submissions,
or to extract and then sell details about the test cases
(in settings where these are not known to students).

Charon solves this problem by executing student code
in a different, sandboxed JVM than exercise code.
It provides an easy-to-use interface
to invoke methods or read fields from classes submitted by the student
across the boundary between the two JVMs.
This interface has almost the same look and feel as directly calling student code.
Security is achieved by never serializing or deserializing any objects.

### What is Charon not for?

Charon doesn't assist in creating test code or receiving student submissions.

Also, the code in this repository assumes both JVMs are invoked externally,
with certain requirements about their environment (classpath, isolation, command-line arguments, environment variables).
In a production environment, it's usually best
to use [Charon-CI](https://github.com/Haspamelodica/Charon-CI) to invoke a test which uses Charon.

## Usage

See [docs/USAGE.md](docs/USAGE.md).

## Building from source

### Docker

See [Charon-CI-Images](https://github.com/Haspamelodica/Charon-CI-Images) for Dockerfiles.

### Maven

Charon consists of multiple Maven plugins, so Maven can be used to compile Charon.
Charon also depends on
[streammultiplexer](https://github.com/Haspamelodica/net.haspamelodica.streammultiplexer),
which unfortunately is not available from the Maven Central Hub,
so you will have to build that project from source as well to build Charon.

## Compiling, running, and debugging in Eclipse

### Import and set up launch configurations

For the Rationals example, follow these steps.
The Maze and Sorter examples work analogously.

1. Clone this repository and
   [streammultiplexer](https://github.com/Haspamelodica/net.haspamelodica.streammultiplexer).

2. In Eclipse, import all pom.xml files from both repositories as "Existing Maven projects":

   Go to "File" -> "Import...", select "Existing Maven projects" from the group "Maven".
   Then click "Browse..." and select the folder you cloned the repositories in.
   Then select all pom.xml files and click "Finish".

3. Create a launch configuration for the student side:

   Go to "Run" -> "Run Configurations...".
   Double-click on "Java Application" to create a new Java Application launch configuration.
   Assign the launch configuration a sensible name, for example "Rationals Student side only".

   Set the launch configuration's project to `examples.rationals.studentrunner`,
   and set the main class to `net.haspamelodica.charon.StudentSideRunner`.
   (This class can't be found in the "Search..." dialog
   because it isn't declared in the launch configuration's main project;
   its name has to be entered by hand.)
   
   Switch to the "Arguments" tab and set the "Program arguments" to `listen 1337`.
   This means the student side will open a server socket on port 1337 upon launch,
   waiting for a connection from the exercise side.

   When starting this launch configuration once,
   it should run indefinitely without giving any output.
   Starting it a second time while another instance is still running
   should immediately crash with the error
   `java.net.BindException: Address already in use: bind` or similar.

4. Create a launch configuration for the exercise side:

   Create a new "JUnit" launch configuration
   and assign it a sensible name like "Rationals Exercise side only".

   Set its mode to "Run all tests in the selected project"
   and select the project `examples.rationals.studentrunner`.
   Set the test runner to JUnit 5.

   Switch to the "Arguments" tab and add a line in the "VM arguments" text box
   saying `-Dnet.haspamelodica.charon.communicationargs="socket localhost 1337"`.
   (There should already be a line saying `-ea`.)
   This means that `CharonExtension` will try to connect with the student side on port 1337
   when creating the `StudentSide` instance.

   Starting this launch configuration while the student side is running
   should result in both launches terminating without output,
   and the JUnit view of Eclipse should show all tests being successful.
   Starting it without the student side running should result in an error similar to
   `Failed to resolve parameter: [...] java.net.ConnectException: Connection refused: connect`.

5. Create a launch group starting both sides for convenience:

   Create a new "Launch Group" launch configuration
   and give it a name like "Rationals".

   Add both the student-side and exercise-side launch configurations to the list,
   with the student side coming first.
   For both, the launch mode should be "Inherit" and the post launch action "None".

### Running

Running the launch group should execute both sides.
The JUnit tab should report the exercise-side test results.
Keep in mind that only one instance of these tests can run at a time.

### Debugging

Debugging the exercise or student side works as usual:
just set some breakpoints
and launch the launch group from the Debug button instead of the Run button.

Unfortunately, "Step Into" and "Step Return" don't work
across the boundary between student and exercise code.
(If you try to Step Into or Step Return across the boundary,
you'll end up in code dynamically generated by Charon.)
To work around this,
set a breakpoint where the Step Into or Step Return would end up,
then let the paused JVM resume,
which should cause the other JVM to suspend at the newly created breakpoint.

Also, the stack visualization in the Debugger view of Eclipse for one of the JVMs
won't show the stack frames of the other JVM.

### Troubleshooting

#### Projects fail to build due to `release 19 is not found in the system`.

Install a JDK for Java 19, and set it as the default JRE in Eclipse:
Go to "Window" -> "Preferences" -> "Java" -> "Installed JREs",
add the JDK19, and select its checkbox to make it the default JRE.

#### On Linux, tests are extremely slow.

Consider switching from sockets to named pipes:
Create two named pipes (FIFOs) and adjust the communication arguments
in both the student-side and exercise-side launch configurations accordingly; for this, see
[CommunicationArgsParser](https://github.com/Haspamelodica/Charon/blob/master/common/src/main/java/net/haspamelodica/charon/utils/communication/CommunicationArgsParser.java#L119-L122),
and take inspiration from how Charon-CI invokes tests:
[exercise side](https://github.com/Haspamelodica/Charon-CI/blob/main/exercise/run_in_docker.sh#L3),
[student side](https://github.com/Haspamelodica/Charon-CI/blob/main/student/pom.xml#L41).

If both the student side and exercise freeze without doing anything,
make sure the pipes are specified in the same order for both the student and exercise side, like in Charon-CI,
and make sure no other instances using these pipes are running.

#### The exercise side sometimes crashes with `Connection refused: connect` when invoking the tests via the launch group.

This happens if the exercise side tries to connect to the student-side socket
before the student side has finished opening it.

On Linux, switching from sockets to named pipes (see troubleshooting step above) resolves this issue
because for pipes it doesn't matter whether the reading or writing end opens a pipe first.

Alternatively, in the launch group, set the post launch action of the student side
to "Delay" with some empirically determined number of seconds.

## Name

Charon is named after the ferryman over the river Acheron in Greek mythology.
This name was chosen because it is his duty to only allow souls to cross Acheron
from the world of the living into the world of the dead, but not back,
which is similar to what this framework does:
it only allows commands from the exercise side to the student side, but not back.
