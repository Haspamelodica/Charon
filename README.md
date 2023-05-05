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
or extracting and then selling details about the test cases
(in settings where these are not known to students).

Charon solves this problem by executing student code
in a different, sandboxed JVM than exercise code,
and providing an easy-to-use interface
to invoke methods or read fields from classes submitted by the student
across the boundary between the two JVMs,
which has almost the same look and feel as directly calling student-side code.
Security is achieved by never serializing or deserializing any objects.

### What is Charon not for?

Charon doesn't assist in creating test code or receiving student submissions.

Also, the code in this repository assumes both JVMs are invoked externally,
with certain requirements about their environment (classpath, isolation, command-line arguments, environment variables).
In a production environment, it's usually best
to use [Charon-CI](https://github.com/Haspamelodica/Charon-CI) to invoke a test which uses Charon.

## Usage

Charon provides two "frontends", "SSI" and "Mockclasses".
The Mockclasses frontend is deprecated due to serious drawbacks
in usability, transparency, and security, and is currently undocumented.

The following chapter describes how to use Charon with the SSI frontend.

### Defining expected structure

The first thing the exercise creator should do is
to specify the expected structure of student code.

Each expected student-side class is represented by two exercise-side interfaces,
which should be defined by the exercise creator.
One of these interfaces is called the "SSI" (student-side instance) of the student-side class
and represents all non-static operations on that student-side class,
for example calling an instance method or reading an instance field.
The other is called the "prototype" and represents all static operations,
for example calling a constructor or writing a static field.

The SSI has to extend
[`StudentSideInstance`](exercise/frontend/ssi/src/main/java/net/haspamelodica/charon/StudentSideInstance.java),
and the prototype has to extend
[`StudentSidePrototype`](exercise/frontend/ssi/src/main/java/net/haspamelodica/charon/StudentSidePrototype.java).
By convention, the prototype interfaces an inner interface of the SSI called `Prototype`, but this is not required by Charon.

Each method of these interfaces represents one student-side member (a method, a field, a constructor, ...).
Which kind of member each of these methods represents is declared by annotating it with 
[`StudentSideInstanceMethodKind`](exercise/frontend/ssi/src/main/java/net/haspamelodica/charon/annotations/StudentSideInstanceMethodKind.java)
or
[`StudentSidePrototypeMethodKind`](exercise/frontend/ssi/src/main/java/net/haspamelodica/charon/annotations/StudentSidePrototypeMethodKind.java),
respectively.
<details>
<summary>Example</summary>

(In all examples, imports to Charon classes will be omitted.)
```java
@StudentSideInstanceKind(INTERFACE)
public interface CumulativeCalculator extends StudentSideInstance
{
    @StudentSideInstanceMethodKind(INSTANCE_FIELD_GETTER)
    public int intermediateResult();

    @StudentSideInstanceMethodKind(INSTANCE_METHOD)
    public int add(int i);

    @StudentSideInstanceMethodKind(INSTANCE_METHOD)
    public int sub(int i);

    @StudentSideInstanceMethodKind(INSTANCE_METHOD)
    public int mul(int i);

    @StudentSideInstanceMethodKind(INSTANCE_METHOD)
    public int div(int i);

    public static interface Prototype extends StudentSidePrototype<CumulativeCalculator>
    {
        @StudentSidePrototypeMethodKind(CONSTRUCTOR)
        public CumulativeCalculator new_(int initialIntermediateResult);
    }
}
```

</details>

### Obtaining an instance of [`StudentSide`](exercise/frontend/ssi/src/main/java/net/haspamelodica/charon/StudentSide.java)

To obtain instances of SSI and prototype interfaces,
exercise code has to obtain an instance of
[`StudentSide`](exercise/junitextension/src/main/java/net/haspamelodica/charon/junitextension/CharonExtension.java).

This interface represents the entires student JVM to exercise code.
If the test code is written using JUnit5, this instance can be obtained
by using [CharonExtension](exercise/junitextension/src/main/java/net/haspamelodica/charon/junitextension/CharonExtension.java).
This will work out-of-the-box with Charon-CI.

<details>
<summary>Example</summary>

```java
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(CharonExtension.class)
public class TestCumulativeCalculator
{
    @Test
    public void testBasic(StudentSide studentSide)
    {
        // test code, with StudentSide instance available
    }
}
```

</details>

### Obtaining instances of prototypes

Once a `StudentSide` is obtained,
it can be used to obtain useable instances of prototype interfaces.

Specifically, test code should call the method `createPrototype`,
which create an instance of a prototype interface from the class of the prototype interface.
These instances are then called the "prototypes" of the respective student-side class.
(Internally, Java
[Proxy classes](https://docs.oracle.com/en/java/javase/19/docs/api/java.base/java/lang/reflect/Proxy.html)
are used to make this possible.)

<details>
<summary>Example</summary>

```java
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(CharonExtension.class)
public class TestCumulativeCalculator
{
    @Test
    public void testBasic(StudentSide studentSide)
    {
        CumulativeCalculator.Prototype CumulativeCalculatorP = studentSide.createPrototype(CumulativeCalculator.Prototype.class);
        // test code, with the prototype of CumulativeCalculator available
    }
}
```

</details>

### Interacting with student code

The prototypes can be used to cause Charon to interact with student code.
If student code returns an object instead of a primitive value,
this object is represented towards exercise code as an SSIs.

If exercise code calls a method on a student-side prototype,
Charon commands the student-side JVM to
perform the action this method represents,
which could be, for example, calling a constructor
or reading a student-side static field.

If one these student-side actions results in a primitive value,
Charon transmits this value as-is back from the student-side JVM to the exercise JVM
and uses it as the return value of the prototype method.

If the result is an object, Charon does not transmit the object.
Instead, Charon creates an SSI instance in the exercise side,
which from then on represents that student-side object toward exercise code.
(Internally, each student-side object is assigned a unique ID,
which is stored in each SSI.)

<details>
<summary>Example</summary>

```java
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(CharonExtension.class)
public class TestCumulativeCalculator
{
    @Test
    public void testBasic(StudentSide studentSide)
    {
        CumulativeCalculator.Prototype CumulativeCalculatorP = studentSide.createPrototype(CumulativeCalculator.Prototype.class);

        CumulativeCalculator calculator = CumulativeCalculatorP.new_(42);
        assertEquals(42, calculator.intermediateResult());

        calculator.add(100);
        assertEquals(142, calculator.intermediateResult());

        calculator.mul(2);
        assertEquals(284, calculator.intermediateResult());
    }
}
```

The call to `add` and `mul` will result in a primitive value,
directly transmitted back to the exercise JVM from the student JVM.

The call to `new_`, which represents the constructor
of the student-side class `CumulativeCalculator`,
will result in Charon creating a new SSI
for the new student-side `CumulativeCalculator` instance.

Note that the type `CumulativeCalculator` of the variable `calculator`
does not refer to the student-side class of that name,
but to the SSI interface defined by the exercise creator in an earlier example.

</details>

## Building from source

### Docker

See [Charon-CI-Images](https://github.com/Haspamelodica/Charon-CI-Images) for Dockerfiles.

### Without Docker

Charon consists of multiple Maven plugins. Charon depends on [streammultiplexer](https://github.com/Haspamelodica/net.haspamelodica.streammultiplexer), which unfortunately is not available from the Maven Central Hub, so you will have to build that project from source as well to build Charon. 

## Name

Charon is named after the ferryman over the river Acheron in Greek mythology.
This name was chosen because it is his duty to only allow souls to cross Acheron from the world of the living into the world of the dead, but not back,
which is similar to what this framework does: it only allows commands from the exercise side to the student side, but not back.

## TODO document:

- Serialization
- CharonJUnitUtils
- Mockclasses frontend?
- Setting up Eclipse for debugging
