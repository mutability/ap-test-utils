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
import uk.co.mutability.test.processors.ExpectDiagnostics;
import uk.co.mutability.test.processors.VerifyDiagnostics;

/**
 * A test for VerifyingProcessor that tests diagnostics attached to elements. 
 */
@VerifyDiagnostics(uk.co.mutability.test.processors.ComplainingProcessor.class)
@Complaint(value="This is a warning on the class", kind=Diagnostic.Kind.WARNING)
@ExpectDiagnostic(value="This is a warning on the class", kind=Diagnostic.Kind.WARNING)
public class GenerateAttachedDiagnosticsTest extends AbstractDiagnosticsTest {
	@Complaint("This is a single error on a constructor")
	@ExpectDiagnostic("This is a single error on a constructor")
	public GenerateAttachedDiagnosticsTest() {}

	@Complaint("This is a single error on a method")
	@ExpectDiagnostic("This is a single error on a method")
	public void dummy1() {}

	@Complaints({
		@Complaint("This is the first error on a method"),
		@Complaint("This is the second error on a method"),		
	})
	@ExpectDiagnostics({
		@ExpectDiagnostic("This is the first error on a method"),
		@ExpectDiagnostic("This is the second error on a method"),		
	})
	public void dummy2() {}

	@Complaint("This is a single error on a field")
	@ExpectDiagnostic("This is a single error on a field")
	public final int dummy3 = 0;
	
	@Complaint("This is a single error on a nested class")
	@ExpectDiagnostic("This is a single error on a nested class")
	public static final class nested {
		@Complaint("This is a single error on a field of a nested class")
		@ExpectDiagnostic("This is a single error on a field of a nested class")
		public final int dummy4 = 0;
	}	
}
