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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.tools.FileObject;
import javax.tools.ForwardingJavaFileManager;
import javax.tools.JavaFileManager;
import javax.tools.JavaFileObject;
import javax.tools.JavaFileObject.Kind;
import javax.tools.SimpleJavaFileObject;
import javax.tools.StandardLocation;

/**
 * A JavaFileManager that implements on-heap storage for CLASS_OUTPUT and SOURCE_OUTPUT locations,
 * and delegates other access to another file manager.
 * <p>
 * This is useful for tests where the output of annotation processing is only interesting for the duration of the test.
 */
public class MemoryFileManager extends ForwardingJavaFileManager<JavaFileManager> {
    /**
     * Build a URI for an internally-stored file from a canonical path.
     * 
     * @param canonicalPath the canonical path
     * @return a memfile: URI
     * @throws IllegalArgumentException if the canonical path is invalid
     */
    private static URI makeURI(String canonicalPath) {
        try {
            return new URI("memfile", canonicalPath, "");
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("Invalid canonical path: URI construction failed", e);
        }
    }

    /**
     * Internal implementation and storage of a single FileObject or JavaFileObject.
     */
    private class MemoryJavaFileObject extends SimpleJavaFileObject {
        /** The canonical path of this file; used by {@link #delete}. */
        private final String canonicalPath;

        /** The original type name, if this file is a JavaFileObject, or "" otherwise */
        private final String typeName;

        /** A stream containing the current content of the file. */
        private ByteArrayOutputStream content = new ByteArrayOutputStream();

        /** Whether this file has been deleted. */
        private boolean deleted;

        /**
         * @param canonicalPath the canonical path this file will be stored as
         * @param typeName the typename of the file, if it represents a class or source file; otherwise "".
         * @param kind the kind of file (source, class, or OTHER for resources)
         */
        public MemoryJavaFileObject(String canonicalPath, String typeName, JavaFileObject.Kind kind) {
            super(makeURI(canonicalPath), kind);
            this.canonicalPath = canonicalPath;
            this.typeName = typeName;
        }

        /**
         * Removes the file from the internal map, and sets the deleted flag.
         * 
         * @return true if the file was deleted, false if it was already deleted
         */
        @Override
        public boolean delete() {
            if (deleted)
                return false;

            deleted = true;
            files.remove(canonicalPath);
            content.reset();
            return true;
        }

        /**
         * Decodes a CharSequence from the current file content using the JVM's default encoding.
         * 
         * @throws IOException if this file has been deleted.
         */
        @Override
        public CharSequence getCharContent(boolean ignoreEncodingErrors) throws IOException {
            if (deleted)
                throw new IOException("This file is deleted");
            return new String(content.toByteArray());
        }

        /**
         * Gets an input stream for the current content of this file. The input stream refers to a copy of the content at the point of the call.
         * 
         * @return an input stream
         * @throws IOException if this file has been deleted
         */
        @Override
        public InputStream openInputStream() throws IOException {
            if (deleted)
                throw new IOException("This file is deleted");
            return new ByteArrayInputStream(content.toByteArray());
        }

        /**
         * Gets an output stream that modifies the content of the file. Any existing content is discarded.
         * 
         * @return an output stream
         * @throws IOException if this file has been deleted
         */
        @Override
        public OutputStream openOutputStream() throws IOException {
            if (deleted)
                throw new IOException("This file is deleted");
            content.reset();
            content = new ByteArrayOutputStream();
            return content;
        }
    }

    /**
     * On-heap storage of all files in CLASS_OUTPUT or SOURCE_OUTPUT locations. Each file is keyed by its canonical path (see {@link #canonicalise} and
     * {@link canonicalizePath}).
     */
    private final Map<String,MemoryJavaFileObject> files = Collections.synchronizedMap(new HashMap<String,MemoryJavaFileObject>());

    /**
     * Construct a new file manager that stores CLASS_OUTPUT and SOURCE_OUTPUT on-heap, and delegates all other non-output locations to another file manager.
     * 
     * @param delegate the file manager to delegate input locations to
     */
    public MemoryFileManager(JavaFileManager delegate) {
        super(delegate);
    }

    /** @return true if we should handle this location ourselves */
    private boolean handles(Location location) {
        return (location == StandardLocation.CLASS_OUTPUT || location == StandardLocation.SOURCE_OUTPUT);
    }

    /** @return the path prefix to use for a given location */
    private String basePath(Location location) {
        if (location == StandardLocation.CLASS_OUTPUT)
            return "classes/";
        else if (location == StandardLocation.SOURCE_OUTPUT)
            return "src/";
        else
            throw new IllegalArgumentException("Location not handled here: " + location.getName());
    }

    /**
     * Compute the canonical path for a resource. The canonical path for a resource is:
     * <ul>
     * <li>"classes/" or "src/" depending on its location; plus
     * <li>the package of the resource, with '/' as a separator for subpackages; plus
     * <li>the path of the resource relative to its package.
     * </ul>
     * 
     * @param location the location of the resource; must be CLASS_OUTPUT or SOURCE_OUTPUT
     * @param packageName the package name for the resource, or "" for the default package
     * @param relativeName the resource name, relative to the package
     * @return the canonical path for the resource
     */
    private String canonicalize(Location location, String packageName, String relativeName) {
        if (packageName.length() == 0)
            return basePath(location) + relativeName;

        return basePath(location) + packageName.replace('.', '/') + "/" + relativeName;
    }

    /**
     * Compute the canonical path for a source or class file representing a particular type. The canonical path for a type-related file is:
     * <ul>
     * <li>"classes/" or "src/" depending on its location; plus
     * <li>the fully qualified class name of the type, with '.' replaced with '/'; plus
     * <li>".class" or ".java" depending on whether it is a class or source file.
     * </ul>
     * 
     * @param location the location of the type; must be CLASS_OUTPUT or SOURCE_OUTPUT
     * @param className the fully qualified class name of the type
     * @param kind the kind of file that is being canonicalized
     * @return the canonical path for the file
     */
    private String canonicalizeClass(Location location, String className, JavaFileObject.Kind kind) {
        return basePath(location) + className.replace('.', '/') + kind.extension;
    }

    @Override
    public ClassLoader getClassLoader(Location location) {
        if (!handles(location))
            return super.getClassLoader(location);

        return null;
    }

    @Override
    public FileObject getFileForInput(Location location, String packageName, String relativeName) throws IOException {
        if (!handles(location))
            return super.getFileForInput(location, packageName, relativeName);

        return files.get(canonicalize(location, packageName, relativeName));
    }

    @Override
    public FileObject getFileForOutput(Location location, String packageName, String relativeName, FileObject sibling) throws IOException {
        if (!handles(location))
            throw new IOException("Location not supported for output: " + location.getName());

        String canonical = canonicalize(location, packageName, relativeName);
        MemoryJavaFileObject newFile = new MemoryJavaFileObject(canonical, "", JavaFileObject.Kind.OTHER);
        files.put(canonical, newFile);
        return newFile;
    }

    @Override
    public JavaFileObject getJavaFileForInput(Location location, String className, Kind kind) throws IOException {
        if (!handles(location))
            return super.getJavaFileForInput(location, className, kind);

        return files.get(canonicalizeClass(location, className, kind));
    }

    @Override
    public JavaFileObject getJavaFileForOutput(Location location, String className, Kind kind, FileObject sibling) throws IOException {
        if (!handles(location))
            throw new IOException("Location not supported for output: " + location.getName());

        String canonical = canonicalizeClass(location, className, kind);
        MemoryJavaFileObject newFile = new MemoryJavaFileObject(canonical, className, kind);
        files.put(canonical, newFile);
        return newFile;
    }

    @Override
    public boolean hasLocation(Location location) {
        return handles(location) || super.hasLocation(location);
    }

    @Override
    public String inferBinaryName(Location location, JavaFileObject file) {
        if (!handles(location))
            return super.inferBinaryName(location, file);

        return ((MemoryJavaFileObject) file).typeName;
    }

    @Override
    public boolean isSameFile(FileObject a, FileObject b) {
        if (a instanceof MemoryJavaFileObject && b instanceof MemoryJavaFileObject)
            return (a == b);

        return super.isSameFile(a, b);
    }

    @Override
    public Iterable<JavaFileObject> list(Location location, String packageName, Set<Kind> kinds, boolean recurse) throws IOException {
        if (!handles(location))
            return super.list(location, packageName, kinds, recurse);

        // We scan for all files matching a prefix that matches the package name, including a trailing '/'
        String prefix;
        if (packageName.length() == 0)
            prefix = basePath(location) + packageName.replace('.', '/') + '/';
        else
            prefix = basePath(location);

        ArrayList<JavaFileObject> found = new ArrayList<JavaFileObject>();
        synchronized (files) { // Grab the lock while we scan to avoid concurrent access causing problems
            for (MemoryJavaFileObject candidate : files.values()) {
                if (!kinds.contains(candidate.getKind()))
                    continue;

                if (!candidate.canonicalPath.startsWith(prefix))
                    continue;

                // If we are recursing, then a prefix match is enough to know that we found something.
                // If we are not recursing, then we must check that this file has no further path
                // separators after the prefix.
                if (!recurse && (candidate.canonicalPath.substring(prefix.length()).indexOf('/') != -1))
                    continue;

                found.add(candidate);
            }
        }

        return found;
    }
}
