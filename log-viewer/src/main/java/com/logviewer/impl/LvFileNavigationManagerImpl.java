package com.logviewer.impl;

import com.logviewer.api.LvFileAccessManager;
import com.logviewer.api.LvFileNavigationManager;
import com.logviewer.data2.*;
import com.logviewer.web.session.SessionAdapter;
import com.logviewer.web.session.tasks.SearchPattern;
import com.logviewer.web.session.tasks.SearchTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.util.StringUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.nio.ByteBuffer;
import java.nio.channels.Channel;
import java.nio.channels.Channels;
import java.nio.channels.SeekableByteChannel;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.AccessDeniedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class LvFileNavigationManagerImpl implements LvFileNavigationManager {

    private static final Logger LOG = LoggerFactory.getLogger(LvFileNavigationManagerImpl.class);

    private final LogService logService;
    private final LvFileAccessManager fileAccessManager;

    @Value("${log-viewer.default-directory:}")
    private String defaultDirectory;

    public LvFileNavigationManagerImpl(@NonNull LvFileAccessManager fileAccessManager, @NonNull LogService logService) {
        this.fileAccessManager = fileAccessManager;
        this.logService = logService;
    }

    @Nullable
    @Override
    public Path getDefaultDirectory() {
        if (!defaultDirectory.isEmpty())
            return Paths.get(defaultDirectory);

        List<Path> roots = fileAccessManager.getRoots();

        if (roots.stream().allMatch(r -> r.getParent() == null)) {
            Path userHomePath = Paths.get(System.getProperty("user.home"));

            if (fileAccessManager.isDirectoryVisible(userHomePath))
                return userHomePath;
        }

        if (roots.size() == 1)
            return roots.get(0);

        return null; // null means list of roots (c:\ , d:\ , f:\)
    }

    @NonNull
    @Override
    public List<LvFsItem> getChildren(@Nullable Path path, @Nullable String filter)
            throws SecurityException, IOException {

        if (path != null && !path.isAbsolute())
            throw new SecurityException("path must be absolute");

        if (path != null && !fileAccessManager.isDirectoryVisible(path))
            throw new SecurityException(fileAccessManager.errorMessage(path));

        Stream<Path> paths;

        try {
            if (path == null) {
                paths = fileAccessManager.getRoots().stream();
            } else {
                paths = Files.list(path);
            }

            var fsItems = paths
                    .filter(f -> Files.isDirectory(f) ?
                            fileAccessManager.isDirectoryVisible(f) : fileAccessManager.isFileVisible(f))
                    .sorted((p1, p2) -> Long.compare(p2.toFile().lastModified(), p1.toFile().lastModified()))
                    .map(it -> (LvFsItem) LvFsItemImpl.create(it))
                    .filter(Objects::nonNull);

            if (StringUtils.hasText(filter)) {
                fsItems = fsItems.parallel().filter(it -> containsText(it, filter));
            }

            return fsItems.toList();

        } catch (AccessDeniedException e) {
            throw new SecurityException("Not enough permissions to access file or directory");
        }
    }

    private boolean containsText(LvFsItem fsItem, String filter) {
        if (fsItem.isDirectory()) {
            LOG.debug("FsItem is directory{}", fsItem);
            return false;
        }

        Log log = logService.openLog(fsItem.getPath().toString());
        try (Log.LogSnapshot snapshot = (Log.LogSnapshot) log.createSnapshot()) {
            BufferedReader br =
                    new BufferedReader(Channels.newReader(snapshot.getChannel(), StandardCharsets.UTF_8));
            return br.lines().parallel().anyMatch(it -> it.contains(filter));
        } catch (IOException e) {
            LOG.error(e.getMessage());
            return false;
        }
    }

    // For tests only.
    public LvFileNavigationManagerImpl setDefaultDirectory(@NonNull String defaultDirectory) {
        this.defaultDirectory = defaultDirectory;
        return this;
    }
}
