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

import javax.tools.Diagnostic;

import uk.co.mutability.test.processors.ExpectDiagnostic;
import uk.co.mutability.test.processors.VerifyDiagnostics;

/**
 * A test for VerifyingProcessor for diagnostics that are not attached to elements.
 */
@VerifyDiagnostics(value=uk.co.mutability.test.processors.ComplainingProcessor.class, generalDiagnostics = {
		@ExpectDiagnostic("This is an unattached error"),
		@ExpectDiagnostic(".*regex substring.*"),
		@ExpectDiagnostic(value="This is an unattached warning", kind=Diagnostic.Kind.WARNING)
})
@Complaint(value="This is an unattached warning", kind=Diagnostic.Kind.WARNING, omitElement=true)
public class GenerateUnattachedDiagnosticsTest extends AbstractDiagnosticsTest {
	@Complaint(value="This is an unattached error", omitElement=true)
	public void dummy1() {}

	@Complaint(value="This should match the regex substring case", omitElement=true)
	public void dummy2() {}

	@Complaint(value="This should also match the regex substring case", omitElement=true)
	public void dummy3() {}
}
