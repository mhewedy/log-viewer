package com.logviewer.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.nio.CharBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// source: https://docs.oracle.com/en/java/javase/17/core/grep-nio-example.html
public class Grep {

    private static final Logger LOG = LoggerFactory.getLogger(Grep.class);

    private static final Charset charset = StandardCharsets.UTF_8;
    private static final CharsetDecoder decoder = charset.newDecoder();

    // Pattern used to parse lines
    private static final Pattern linePattern = Pattern.compile(".*\r?\n");

    // Use the linePattern to break the given CharBuffer into lines, applying
    // the input pattern to each line to see if we have a match
    private static boolean grep(CharBuffer cb, String filter) {
        Matcher lm = linePattern.matcher(cb); // Line matcher

        while (lm.find()) {
            String cs = lm.group(); // The current line
            if (cs.contains(filter))
                return true;
            if (lm.end() == cb.limit())
                break;
        }
        return false;
    }

    // Search for occurrences of the input pattern in the given file
    public static boolean grep(File f, String filter) {
        // Open the file and then get a channel from the stream
        try (FileInputStream fis = new FileInputStream(f);
             FileChannel fc = fis.getChannel()) {

            // Get the file's size and then map it into memory
            int sz = (int) fc.size();
            MappedByteBuffer bb = fc.map(FileChannel.MapMode.READ_ONLY, 0, sz);

            // Decode the file into a char buffer
            CharBuffer cb = decoder.decode(bb);

            // Perform the search
            return grep(cb, filter);
        } catch (Exception ex) {
            LOG.error(ex.getMessage());
            return false;
        }
    }
}
