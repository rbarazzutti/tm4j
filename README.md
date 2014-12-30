Transactional Memory support in Java and Scala
==============================================

## Focus
- Intel TSX (Transactional Synchronization Extensions) using RTM (Restricted Transactional Memory)
- OpenJDK with Hotspot Java Virtual Machine
- Java and Scala programming language

## API design

A naive implementation is to provide 2 jni calls that map to xbegin and xend instructions.
However, this approach is not working in the current Hotspot implementation if you have an AVX processor (which is the case for Haswell) because in the JNI wrapper at the return of the native call an instruction vzeroupper is executed.
This instruction is forbidden inside a TSX transaction and make it abort.

So we focus our implementation on using a callback function.
By calling the Java method from the JNI context, it avoids the vzeroupper instruction.
One benefit of this design is to reduce the number of JNI wrapper to 1 instead of 2 and thusly reducing the overhead that comes with it.

This allows a java method to be executed inside an hardware transaction.
In the case the method to execute is not compiled yet, the transaction is likely to hit capacity issue.
Indeed, the interpreter adds a lot of memory accesses and is not as efficient as compiled code.
Anyway, transactions are meant to be executed in a hot spot that will be most probably compiled by the JIT compiler.
In order to check this assomption, you can use the paramter `-Xcomp` to force every methods to be compiled (Warning, this option is quite brutal and will slow down the startup).

