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

import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.ElementScanner6;

/**
 * This annotation processor emits diagnostics when it sees a @Complaint or @Complaints annotation. It is used to test VerifyingProcessor by allowing test
 * classes to generate processor diagnostics on demand.
 */
@SupportedAnnotationTypes({ "uk.co.mutability.test.processors.Complaint", "uk.co.mutability.test.processors.Complaints" })
@SupportedSourceVersion(SourceVersion.RELEASE_6)
public class ComplainingProcessor extends AbstractProcessor {
    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        ElementScanner6<Void,Void> scanner = new ElementScanner6<Void,Void>() {
            @Override
            public Void scan(Element annotatedElement, Void p) {
                Complaint singleComplaint = annotatedElement.getAnnotation(Complaint.class);
                if (singleComplaint != null) {
                    complain(singleComplaint, annotatedElement);
                }

                Complaints complaints = annotatedElement.getAnnotation(Complaints.class);
                if (complaints != null) {
                    for (Complaint complaint : complaints.value()) {
                        complain(complaint, annotatedElement);
                    }
                }

                return super.scan(annotatedElement, p);
            }

            private void complain(Complaint complaint, Element annotatedElement) {
                if (complaint.omitElement())
                    processingEnv.getMessager().printMessage(complaint.kind(), complaint.value());
                else
                    processingEnv.getMessager().printMessage(complaint.kind(), complaint.value(), annotatedElement);
            }
        };

        scanner.scan(roundEnv.getRootElements(), null);
        return true;
    }
}
