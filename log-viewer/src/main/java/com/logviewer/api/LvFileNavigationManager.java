package com.logviewer.api;

import com.logviewer.files.FileType;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;

public interface LvFileNavigationManager {

    /**
     * @param path      Directory to list or {@code null}.
     * @param filter
     */
    @NonNull
    List<LvFsItem> getChildren(@Nullable Path path, Filter filter) throws SecurityException, IOException;

    @Nullable
    Path getDefaultDirectory();

    interface LvFsItem {

        Path getPath();

        boolean isDirectory();

        FileType getType();

        long getSize();

        @Nullable
        Long getModificationTime();
    }

    record Filter(String text, LocalDate startDate, LocalDate endDate) {
    }
}
