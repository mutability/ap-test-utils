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

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import javax.tools.Diagnostic;

/**
 * Marks the annotated element as being expected to generate a diagnostic during annotation processing. If more than one diagnostic from the same element is
 * expected, {@link ExpectDiagnostics} can be used to annotate the element with multiple @ExpectDiagnostic values.
 * 
 **/
@Retention(RetentionPolicy.RUNTIME)
public @interface ExpectDiagnostic {
    /** A regular expression to match against the expected diagnostic message */
    String value();

    /** The diagnostic kind to match. Defaults to ERROR */
    Diagnostic.Kind kind() default Diagnostic.Kind.ERROR;
}
