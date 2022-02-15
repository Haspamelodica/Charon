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

For each class expected to be written by the staudent, the exercise creator needs to create an interface defining its expected structure: [`Sorter.java`](examples/sorter/exercise/src/sorter/Sorter.java), [`StringArrayList.java`](examples/sorter/exercise/src/sorter/StringArrayList.java).
Based on them, one or more classes containing test code can be written: [`SorterExercise.java`](examples/sorter/exercise/src/sorter/SorterExercise.java).

Students only need to create the classes required by the exercise: [`Sorter.java`](examples/sorter/student/src/sorter/Sorter.java), [`StringArrayList.java`](examples/sorter/student/src/sorter/StringArrayList.java).
