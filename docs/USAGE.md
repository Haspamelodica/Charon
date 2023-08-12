Charon provides two "frontends": "SSI" and "Mockclasses".
The Mockclasses frontend is deprecated due to serious drawbacks
in usability, transparency, and security, and is currently undocumented.
The following will describe the SSI frontend only.

# Basic concepts

To use the SSI frontend, the exercise creator - as a first step -
defines which classes, methods and fields students are expected to implement.
This is done by declaring interfaces representing student-side structure.

Once the expected student-side code structure is defined,
test code has to obtain an instance of
[`StudentSide`](../exercise/frontend/ssi/src/main/java/net/haspamelodica/charon/StudentSide.java),
which can then be used as an entry point to interacting with student code.

The following subsections describe these steps in detail,
using an example exercise where it's the students' job to implement a class representing rational numbers
and some basic operations on them.
The full example, along with an example student submission, can be found in this repository under
[examples/rationals](../examples/rationals).

## Defining expected structure

The first thing the exercise creator should do is
to specify the expected structure of student code.

Each expected student-side class is represented by two exercise-side interfaces,
which should be defined by the exercise creator:
 - One of these interfaces is called the "SSI" (student-side instance) of the student-side class.
   It represents all non-static operations on that student-side class,
   for example calling an instance method or reading an instance field.

   The SSI has to extend
   [`StudentSideInstance`](../exercise/frontend/ssi/src/main/java/net/haspamelodica/charon/StudentSideInstance.java).
 - The other is called the "prototype" and represents all static operations,
   for example calling a constructor or writing a static field.

   The prototype has to extend
   [`StudentSidePrototype`](../exercise/frontend/ssi/src/main/java/net/haspamelodica/charon/StudentSidePrototype.java).
   By convention, the prototype interface is an inner interface of the SSI called `Prototype`, but this is not required by Charon.

Each method of these interfaces represents one student-side member (a method, a field, a constructor, ...).
Annotating it with 
[`StudentSideInstanceMethodKind`](../exercise/frontend/ssi/src/main/java/net/haspamelodica/charon/annotations/StudentSideInstanceMethodKind.java)
or
[`StudentSidePrototypeMethodKind`](../exercise/frontend/ssi/src/main/java/net/haspamelodica/charon/annotations/StudentSidePrototypeMethodKind.java),
respectively, specifies which kind of member it represents.

All parameter types and the return type of each of these methods have to be either primitive or an SSI.
(An exception to this is serialization, which is described later).
Field getters have to have no parameters and the represented field's type as their return type;
field setters have to have return type `void` and exactly one parameter with the field's type.

<details>
<summary>Example</summary>

(In all examples, imports to Charon classes will be omitted.)
```java
@StudentSideInstanceKind(CLASS)
public interface RationalNumber extends StudentSideInstance
{
    @StudentSideInstanceMethodKind(INSTANCE_FIELD_GETTER)
    public int num();

    @StudentSideInstanceMethodKind(INSTANCE_FIELD_GETTER)
    public int den();

    @StudentSideInstanceMethodKind(INSTANCE_METHOD)
    public double approximateAsDouble();

    @StudentSideInstanceMethodKind(INSTANCE_METHOD)
    public RationalNumber add(RationalNumber other);

    @StudentSideInstanceMethodKind(INSTANCE_METHOD)
    public RationalNumber sub(RationalNumber other);

    @StudentSideInstanceMethodKind(INSTANCE_METHOD)
    public RationalNumber mul(RationalNumber other);

    @StudentSideInstanceMethodKind(INSTANCE_METHOD)
    public RationalNumber div(RationalNumber other);

    public static interface Prototype extends StudentSidePrototype<RationalNumber>
    {
        @StudentSidePrototypeMethodKind(STATIC_FIELD_GETTER)
        public RationalNumber ZERO();

        @StudentSidePrototypeMethodKind(CONSTRUCTOR)
        public RationalNumber new_(int value);

        @StudentSidePrototypeMethodKind(CONSTRUCTOR)
        public RationalNumber new_(int num, int den);
    }
}
```

</details>

## Obtaining an instance of [`StudentSide`](exercise/frontend/ssi/src/main/java/net/haspamelodica/charon/StudentSide.java)

To obtain instances of SSI and prototype interfaces,
exercise code has to obtain an instance of
[`StudentSide`](../exercise/frontend/ssi/src/main/java/net/haspamelodica/charon/StudentSide.java).

This interface represents the entire student JVM to the exercise code.
If the test code is written using JUnit5, this instance can be obtained by using
[CharonExtension](../exercise/junitextension/src/main/java/net/haspamelodica/charon/junitextension/CharonExtension.java).
This will work out-of-the-box with Charon-CI.

<details>
<summary>Example</summary>

```java
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(CharonExtension.class)
public class TestRationalNumber
{
    @Test
    public void testBasic(StudentSide studentSide)
    {
        // test code, with StudentSide instance available
    }
}
```

</details>

## Obtaining instances of prototypes

Once a `StudentSide` is obtained,
it can be used to obtain useable instances of prototype interfaces.

Specifically, test code should call the method `createPrototype`,
which creates an instance of a prototype interface from the `class` of the prototype interface.
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
public class TestRationalNumber
{
    @Test
    public void testBasic(StudentSide studentSide)
    {
        RationalNumber.Prototype RationalNumberP = studentSide.createPrototype(RationalNumber.Prototype.class);
        // test code, with the prototype of RationalNumber available
    }
}
```

</details>

## Interacting with student code

The prototypes can be used to (make Charon to) interact with student code.
If student code returns an object instead of a primitive value,
this object is represented to the exercise code as an SSI.

If exercise code calls a method on a student-side prototype,
Charon commands the student-side JVM to
perform the action this method represents.
This could be, for example, calling a constructor
or reading a student-side static field.

If one these student-side actions results in a primitive value,
Charon transmits this value as-is back from the student-side JVM to the exercise JVM
and uses it as the return value of the prototype method.

If the result is an object, Charon does not transmit the object.
Instead, Charon creates an SSI instance on the exercise side,
which from then on represents that student-side object to the exercise code.
(Internally, each student-side object is assigned a unique ID,
which is stored in each SSI.)

Calling methods on SSIs works analogously to prototypes.

<details>
<summary>Example</summary>

```java
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(CharonExtension.class)
public class TestRationalNumber
{
    @Test
    public void testBasic(StudentSide studentSide)
    {
        RationalNumber.Prototype RationalNumberP = studentSide.createPrototype(RationalNumber.Prototype.class);

        RationalNumber zero = RationalNumberP.ZERO();
        assertEquals(zero.approximateAsDouble(), 0);

        RationalNumber rational42 = RationalNumberP.new_(42);
        assertEquals(rational42.num(), 42);
        assertEquals(rational42.den(), 1);
        assertEquals(rational42.approximateAsDouble(), 42);

        RationalNumber rational50 = rational42.add(RationalNumberP.new_(8));
        assertEquals(rational50.num(), 50);
        assertEquals(rational50.den(), 1);
        assertEquals(rational50.approximateAsDouble(), 50);

        RationalNumber rational25_9 = rational50.div(RationalNumberP.new_(18));
        assertEquals(rational25_9.num(), 25);
        assertEquals(rational25_9.den(), 9);
        assertEquals(rational25_9.approximateAsDouble(), 25 / 9d);

        RationalNumber rational125_6 = rational25_9.mul(RationalNumberP.new_(15, 2));
        assertEquals(rational125_6.num(), 125);
        assertEquals(rational125_6.den(), 6);
        assertEquals(rational125_6.approximateAsDouble(), 125 / 6d);

        RationalNumber rational0 = rational125_6.mul(zero);
        assertEquals(rational0.approximateAsDouble(), 0);
    }
}
```

The calls to `num`, `den`, and `approximateAsDouble` will result in a primitive value,
directly transmitted back to the exercise JVM from the student JVM.

The calls to `add`, `div`, `mul`, and `new_` (which represents the constructor
of the student-side class `RationalNumber`)
will make Charon create a new SSI
for the new student-side `RationalNumber` instance.

Note that the type `RationalNumber`
does not refer to the student-side class of that name,
but to the SSI interface defined earlier by the exercise creator.

</details>

# TODO document:

- Setting up projects
- Overriding student-side names
- Arrays
- Serialization
- Callbacks
- Student-side exceptions
- Hint: Create prototypes once in a BeforeAll method or constructor
- CharonJUnitUtils
- Mockclasses frontend?
