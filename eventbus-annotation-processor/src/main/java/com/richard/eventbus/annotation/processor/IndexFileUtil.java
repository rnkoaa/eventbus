package com.richard.eventbus.annotation.processor;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.processing.Filer;
import javax.tools.FileObject;
import javax.tools.StandardLocation;

public class IndexFileUtil {

    public static final String ANNOTATED_INDEX_PREFIX = "META-INF/events/";

    public static void writeSimpleNameIndexFile(Filer filer, Set<String> elementList, String resourceName)
            throws IOException {

        FileObject file = readOldIndexFile(filer, elementList, resourceName);
        if (file != null) {
//             * Ugly hack for Eclipse JDT incremental compilation.
//             * Eclipse JDT can't createResource() after successful getResource().
//             * But we can file.openWriter().
            try {
                writeIndexFile(filer, elementList, resourceName, file);
                return;
            } catch (IllegalStateException e) {
                // Thrown by HotSpot Java Compiler
            }
        }
        writeIndexFile(filer, elementList, resourceName, null);
    }

    protected static void writeIndexFile(Filer filer, Set<String> entries, String resourceName, FileObject overrideFile)
            throws IOException {
        FileObject file = overrideFile;
        if (file == null) {
            file = filer.createResource(StandardLocation.CLASS_OUTPUT, "", resourceName);
        }
        try (Writer writer = file.openWriter()) {
            for (String entry : entries) {
                writer.write(entry);
                writer.write("\n");
            }
        }
    }

    protected static void readOldIndexFile(Set<String> entries, Reader reader) throws IOException {
        try (BufferedReader bufferedReader = new BufferedReader(reader)) {
            String line = bufferedReader.readLine();
            while (line != null) {
                entries.add(line);
                line = bufferedReader.readLine();
            }
        }
    }

    protected static FileObject readOldIndexFile(Filer filer, Set<String> entries, String resourceName)
            throws IOException {
        Reader reader = null;
        try {
            final FileObject resource = filer.getResource(StandardLocation.CLASS_OUTPUT, "", resourceName);
            reader = resource.openReader(true);
            readOldIndexFile(entries, reader);
            return resource;
        } catch (FileNotFoundException e) {
            // Ugly hack for Intellij IDEA incremental compilation.
            // The problem is that it throws FileNotFoundException on the files, if they were not created during the
            // current session of compilation.
            final String realPath = e.getMessage();
            if (new File(realPath).exists()) {
                try (Reader fileReader = new FileReader(realPath)) {
                    readOldIndexFile(entries, fileReader);
                }
            }
        } catch (IOException e) {
            // Thrown by Eclipse JDT when not found
        } catch (UnsupportedOperationException e) {
            // Java6 does not support reading old index files
        } finally {
            if (reader != null) {
                reader.close();
            }
        }
        return null;
    }

}
