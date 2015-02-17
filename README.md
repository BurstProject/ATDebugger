# ATDebugger

Requirement:
Java 7+ JVM with javafx support. Some builds of openjdk7 do not have javafx support.

Compiling:
A recent version of sbt(simple build tool) is needed. Edit the path to your jdk into the build.sbt file. Run sbt then type packageJavafx to compile.

Usage:
Run the jar, and the block control window will show up. Click 'Add AT' to open AT windows. You can drag a text file containing AT assembly into the code table and it will load it.

ATs can be given balance either by setting the balance at the top of their windows, or by clicking 'Send tx' and making a transaction to them. All balances are represented as they are processed internally in burst (100000000 (10^8) = 1 coin). Step fees are 10^7(10000000) for normal opcodes, and 10^8(100000000) for api calls.

Breakpoints can be set by clicking in the bp column next to code lines.

Clicking 'Advance Block' in the block control window will cause ATs which are eligable to run to run.

While an AT is running, run will cause an AT to run until it hits it's next breakpoint. Step into will run exactly one opcode. Step over will run until it gets to a line which is shown in the code. Step into and over will usually do the same thing, however the code window shows your source, which may not perfectly match the assembled code, so if any line was expanded to multiple operations, step into will run them one at a time, but step over will execute them together.

When the AT is in progress, you can double click in the instruction pointer column to set the next statement. Variables can also be edited on the right side while the AT is in progress. The instuction pointer and variables cannot be edited in between blocks.

If you have multiple AT windows, they share they same pool of transactions, so they can send to each other.

Each AT window keeps a complete history of it's state each block, so you can use 'Undo Block' all the way back.

This uses completly different assembling code from the available ATAssembler and may assemble to different code. There are 2 known cases where it will assemble differently:
1. Branch statements can jump a limited distance. ATAssembler issues a warning if you try to branch too far, but ATDebugger automatically fixes it by inverting the branch and adding a jmp.
2. ATAssembler wastes a variable when setting error handlers. ATDebugger does not.

A new assembler that shares the assembling code with ATDebugger will be released to make it consistent.

