/*
 * This file is part of the ap-test-utils package.
 * 
 * Copyright (C) 2012 Oliver Jowett <oliver@mutability.co.uk>
 *
 * ap-test-utils is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *  
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with ap-test-utils. If not, see <http://www.gnu.org/licenses/>.
 *
 */

package uk.co.mutability.test.processors;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.annotation.processing.Processor;
import javax.tools.*;

/**
 * A test helper class for testing annotation processors. Designed for use with "import static".
 */
public final class Verifier {
    private Verifier() {
        /* Prevent construction */
    }

    private static JavaCompiler COMPILER = ToolProvider.getSystemJavaCompiler();

    /**
     * Use the system java compiler to process annotations in a given class, and check that the correct set of diagnostics were generated.
     * <p>
     * The annotation processor to use is determined by the {@link VerifyDiagnostics} annotation on the class. The expected diagnostics are determined from
     * {@link VerifyDiagnostics}, {@link ExpectDiagnostic}, and {@link ExpectDiagnostics} annotations.
     * <p>
     * If there are any unexpected diagnostics, or if there are missing expected diagnostics, then a non-empty list of diagnostics describing the problems is
     * returned.
     * <p>
     * The output of annotation processing is stored on heap (only) for the duration of processing, then discarded.
     * 
     * @param classToProcess the class to perform annotation processing on
     * @param compilerArgs any additional compiler args to pass
     * @return a list of diagnostics if there were problems; an empty list if everything was OK
     * @throws Exception if something went wrong during execution
     */
    public static List<Diagnostic<? extends JavaFileObject>> checkProcessorDiagnostics(Class<?> classToProcess, String... compilerArgs) {
        /* Identify the processor class */
        VerifyDiagnostics diags = classToProcess.getAnnotation(VerifyDiagnostics.class);
        if (diags == null)
            throw new IllegalArgumentException("No @VerifyDiagnostics annotation found on " + classToProcess);

        Processor processorInstance;
        try {
            processorInstance = diags.value().getConstructor().newInstance();
        } catch (IllegalAccessException e) {
            throw new IllegalAccessError(e.getMessage());
        } catch (NoSuchMethodException e) {
            throw new NoSuchMethodError(e.getMessage());
        } catch (InvocationTargetException e) {
            if (e.getCause() instanceof Error)
                throw (Error) e.getCause();
            else if (e.getCause() instanceof RuntimeException)
                throw (RuntimeException) e.getCause();
            else
                throw new UndeclaredThrowableException(e.getCause());
        } catch (InstantiationException e) {
            throw new InstantiationError("Processor class is abstract");
        }

        if (COMPILER == null)
            throw new UnsupportedOperationException("No system compiler available via the tool interface");

        DiagnosticCollector<JavaFileObject> diagnosticListener = new DiagnosticCollector<JavaFileObject>();
        VerifyingProcessor wrappedProcessor = new VerifyingProcessor(processorInstance);

        MemoryFileManager memFileManager = new MemoryFileManager(COMPILER.getStandardFileManager(diagnosticListener, null, null));
        JavaCompiler.CompilationTask task = COMPILER.getTask(null, memFileManager, diagnosticListener, Arrays.asList(compilerArgs),
                                                             Arrays.asList(classToProcess.getName()), null);
        task.setProcessors(Collections.singleton(wrappedProcessor));

        if (!task.call()) {
            if (diagnosticListener.getDiagnostics().isEmpty()) {
                diagnosticListener.report(new SimpleDiagnostic<JavaFileObject>(Diagnostic.Kind.ERROR,
                                                                               "error: compilation failed, but no diagnostics were generated"));
            }
        }

        return diagnosticListener.getDiagnostics();
    }

    /**
     * Throw AssertionError if any diagnostics are present in the given diagnostics list.
     * 
     * @param diags a list of diagnostics to check.
     */
    public static void assertNoDiagnostics(List<Diagnostic<? extends JavaFileObject>> diags) {
        if (diags.isEmpty())
            return;

        StringBuilder sb = new StringBuilder("One or more diagnostics reported by the verifying processor; these usually indicate test failures: ");
        for (Diagnostic<? extends JavaFileObject> diagnostic : diags) {
            sb.append("\n ");
            sb.append(diagnostic.getMessage(null));
        }

        throw new AssertionError(sb.toString());
    }
}
