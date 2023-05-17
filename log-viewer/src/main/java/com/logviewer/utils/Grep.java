package com.logviewer.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

public class Grep {

    private static final Logger LOG = LoggerFactory.getLogger(Grep.class);

    public static boolean grep(File f, String filter) {
        try {
            return Runtime.getRuntime().exec(
                    new String[]{"sh", "-c", "grep -l %s %s".formatted(filter, f.getAbsolutePath())}
            ).waitFor() == 0;
        } catch (Exception e) {
            LOG.error(e.getMessage());
            return false;
        }
    }
}
