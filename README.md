ap-test-utils
=============

This module provides helper classes for testing Java annotation processors.

In particular, it provides:

 * A "wrapping" annotation processor that verifies that your annotation
   processor, when given test input, generates a particular set of diagnostic
   messages;

 * A JavaFileManager that stores output on the heap only - useful when the
   act of running a processor is what you want to test, and you don't care
   about the generated output;

Feedback to: Oliver Jowett <oliver@mutability.co.uk>


Installation
------------

This module is a Maven 2 module.
Thirty second guide to building via Maven if you are unfamiliar with it:

 * `apt-get install maven2` or equivalent
 * `cd ap-test-utils; mvn package`
 * Look in `ap-test-utils/target` for output

Annotating test input
---------------------

Write a class that will be used as test input for your processor.

On each element of the class (field, method, etc) that is expected
to produce a diagnostic message when processed, add a
@ExpectDiagnostic or @ExpectDiagnostics annotation. These annotations need
to be on the same (javax.lang.model.element.) Element that your processor
includes when generating the diagnostic.

Annotate the class itself with @VerifyDiagnostics. This annotation requires:

 * The class of the processor to invoke on the test class (this is your
   processor class);
 * Optionally, one or more diagnostics to expect that are not associated with
   any particular Element. This is generally rare (and the error messages
   you get in this case lack source location, so are not very useful anyway).

Invoking the wrapper processor
------------------------------

There are two possible ways to invoke the wrapper processor.

 * Via Verifier.checkProcessorDiagnostics(). This accepts a test class and
   returns a list of unexpected/unmatched diagnostics. You can invoke this from
   e.g. a JUnit test:

      Verifier.assertNoDiagnostics(Verifier.checkProcessorDiagnostics(MyTestInput.class));

 * Via normal annotation processing. In this case, you should explicitly tell
   the compiler to invoke uk.co.mutability.test.annotations.VerifyingProcessor,
   and pass an annotation option "mutability.test.delegateClassName" that is
   the name of your processor. For example, with javac, this looks something
   like:

      javac -processor uk.co.mutability.test.annotations.VerifyingProcessor \
            -processorpath path/to/ap-test-utils:path/to/my-processor \
            -Amutability.test.delegateClassName=my.processor.ProcessorImpl \
            src/TestInput.java

License
-------

This module is licensed under the GPL, version 2 or later. See
<gpl-2.0.txt>. Note that merely using the module to feed test
input through a processor as part of testing does *not*
require any of the test input or processor code to be GPL-compatibile.

