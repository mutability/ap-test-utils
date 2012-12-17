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

import java.util.Locale;

import javax.tools.Diagnostic;

/**
 * A simple data-holding implementation of Diagnostic.
 * @param <S> the type of source used in the Diagnostic.
 */
public class SimpleDiagnostic<S> implements Diagnostic<S> {
    private final S source;
    private final long lineNumber;
    private final long columnNumber;
    private final long startPosition;
    private final long endPosition;
    private final long position;
    private final String code;
    private final String message;
    private final Diagnostic.Kind kind;

    /**
     * Construct a new diagnostic from fully-specified values.
     * The constraints on position values documented in Diagnostic must be followed.
     *
     * @throws NullPointerException if {@code kind} or {@code message} are {@code null}  
     * @throws IllegalArgumentException if the provided position values would violate the constraints of Diagnostic.  
     */
    public SimpleDiagnostic(Diagnostic.Kind kind, String message, String code, S source, long position, long lineNumber, long columnNumber,
                            long startPosition, long endPosition) {
        // Enforce the constraints that Diagnostic mandates
        
        if (kind == null)
            throw new NullPointerException("kind");

        if (message== null)
            throw new NullPointerException("message");
        
        if (source == null && position != NOPOS)
            throw new IllegalArgumentException("source == null and position != NOPOS");
        
        if (position == Diagnostic.NOPOS) {
            if (startPosition != NOPOS)
                throw new IllegalArgumentException("position == NOPOS and startPosition != NOPOS");
            if (endPosition != NOPOS)
                throw new IllegalArgumentException("position == NOPOS and endPosition != NOPOS");
            if (lineNumber != NOPOS)
                throw new IllegalArgumentException("position == NOPOS and lineNumber != NOPOS");
            if (columnNumber != NOPOS)
                throw new IllegalArgumentException("position == NOPOS and columnNumber != NOPOS");
        } else {
            if (startPosition == NOPOS)
                throw new IllegalArgumentException("position != NOPOS and startPosition == NOPOS");
            if (endPosition == NOPOS)
                throw new IllegalArgumentException("position != NOPOS and startPosition == NOPOS");
            if (lineNumber == NOPOS)
                throw new IllegalArgumentException("position != NOPOS and lineNumber == NOPOS");
            if (columnNumber == NOPOS)
                throw new IllegalArgumentException("position != NOPOS and columnNumber == NOPOS");
            if (position < 0) 
                throw new IllegalArgumentException("position != NOPOS and position < 0");
            if (startPosition < 0)
                throw new IllegalArgumentException("position != NOPOS and startPosition < 0");
            if (endPosition < 0)
                throw new IllegalArgumentException("position != NOPOS and endPosition < 0");
            if (position < startPosition)
                throw new IllegalArgumentException("position < startPosition");
            if (position > endPosition)
                throw new IllegalArgumentException("position > endPosition");
            if (lineNumber < 1)
                throw new IllegalArgumentException("lineNumber != NOPOS and lineNumber < 1");
            if (columnNumber < 1)
                throw new IllegalArgumentException("columnNumber != NOPOS and columnNumber < 1");
        }
                
        this.kind = kind;
        this.message = message;
        this.code = code;
        this.source = source;
        this.position = position;
        this.lineNumber = lineNumber;
        this.columnNumber = columnNumber;
        this.startPosition = startPosition;
        this.endPosition = endPosition;
    }

    /**
     * Construct a new diagnostic with no source or position information.
     * @param kind the kind of diagnostic
     * @param message the localized message
     * @throws NullPointerException if {@code kind} or {@code message} are {@code null}  
     */
    public SimpleDiagnostic(Diagnostic.Kind kind, String message) {
        this(kind, message, null, null, NOPOS, NOPOS, NOPOS, NOPOS, NOPOS);
    }
    
    @Override
    public S getSource() {
        return source;
    }

    @Override
    public long getLineNumber() {
        return lineNumber;
    }

    @Override
    public long getColumnNumber() {
        return columnNumber;
    }

    @Override
    public long getStartPosition() {
        return startPosition;
    }

    @Override
    public long getEndPosition() {
        return endPosition;
    }

    @Override
    public long getPosition() {
        return position;
    }

    @Override
    public String getCode() {
        return code;
    }

    @Override
    public String getMessage(Locale locale) {
        return message;
    }

    @Override
    public Diagnostic.Kind getKind() {
        return kind;
    }

}
