# StudentCodeSeparator

Work-in-progress.

A library meant for making secure, automatic tests of Java code in a didactic context.

The basic idea is that students submit code solving some exercise ("student code"),
which is then automatically tested by code written by the exercise creator ("exercise code").
To make this secure, student code needs to be executed in a different, sandboxed JVM than exercise code.
Running both in the same JVM would allow malicious students to
read private test cases or even fake results.

## Interface

This library provides an easy-to-use interface for securely executing methods and reading fields
of student code from exercise code, across the boundary between the two JVMs.

### Defining expected structure

The exercise creator specifies the expected structure of student code
by creating an interface for each expected student-side class
and annotating its methods with special annotations defined by this library.
Each method represents a student-side member (methods, fields, constructors).

### Interacting with student code

Once structure-defining interfaces are defined,
the library can be used to obtain useable instances of those interfaces.
(Internally, Proxy classes are used for this.)
Those instances can then be used very similarily to directly accessing student code.
Method calls on them are currently executed in the same JVM,
but will transparently be translated to method calls in the student JVM in the future.

### Example

An exercise creator only needs to provide two classes to use this library:
An interface defining the structure of a class expected to be written by the student ([`MyClass.java`](exampleExercise/net/haspamelodica/studentcodeseparator/MyClass.java)),
and a class containing test code ([`ExampleExercise.java`](exampleExercise/net/haspamelodica/studentcodeseparator/ExampleExercise.java)).

Students only need to provide the class required by the exercise: [`MyClassImpl.java`](exampleStudent/net/haspamelodica/studentcodeseparator/MyClassImpl.java).
