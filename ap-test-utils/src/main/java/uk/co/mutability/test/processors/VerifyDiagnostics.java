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

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.annotation.processing.Processor;

/**
 * Marks a test class as being expected to generate diagnostic messages when processed by an annotation processor. The expected messages are provided by
 * {@link ExpectDiagnostic} or {@link ExpectDiagnostics} annotations on the elements that should generate diagnostics.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface VerifyDiagnostics {
    /**
     * The annotation processor class that will generate the diagnostics. This is used by the test harness to work out which
     * processor to build when given a class to verify.
     */
    public Class<? extends Processor> value();

    /**
     * Marks the annotated class as being expected to generate one or more diagnostics from annotation processing that are not associated with any particular
     * element. If not specified, only annotations indicated via {@link ExpectDiagnostic} / {@link ExpectDiagnostics} are expected.
     */
    public ExpectDiagnostic[] generalDiagnostics() default {};
}
