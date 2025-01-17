/*
 * Copyright 2023 Thoughtworks, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.thoughtworks.go.util;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;

import java.io.*;
import java.net.URI;
import java.util.Base64;
import java.util.UUID;

public class FileUtil {
    public static final String TMP_PARENT_DIR = "data";
    private static final String CRUISE_TMP_FOLDER = "cruise" + "-" + UUID.randomUUID();
    private static final Logger LOGGER = org.slf4j.LoggerFactory.getLogger(FileUtil.class);

    private FileUtil() {}

    public static boolean isFolderEmpty(File folder) {
        if (folder == null) {
            return true;
        }
        File[] files = folder.listFiles();
        return files == null || files.length == 0;
    }

    public static String applyBaseDirIfRelativeAndNormalize(File baseDir, File actualFileToUse) {
        return FilenameUtils.separatorsToUnix(applyBaseDirIfRelative(baseDir, actualFileToUse).getPath());
    }

    public static File applyBaseDirIfRelative(File baseDir, File actualFileToUse) {
        if (actualFileToUse == null) {
            return baseDir;
        }
        if (actualFileToUse.isAbsolute()) {
            return actualFileToUse;
        }

        if (StringUtils.isBlank(baseDir.getPath())) {
            return actualFileToUse;
        }

        return new File(baseDir, actualFileToUse.getPath());

    }

    public static void validateAndCreateDirectory(File directory) {
        if (directory.exists()) {
            return;
        }
        try {
            FileUtils.forceMkdir(directory);
        } catch (IOException e) {
            throw new RuntimeException("Failed to create folder: " + directory.getAbsolutePath());
        }
    }

    public static void createParentFolderIfNotExist(File file) {
        File parentFile = file.getParentFile();
        if (parentFile != null && !parentFile.exists()) {
            parentFile.mkdirs();
        }
    }

    public static String toFileURI(File file) {
        URI uri = file.toURI();
        String uriString = uri.toASCIIString();
        return uriString.replaceAll("^file:/", "file:///");
    }

    public static String toFileURI(String path) {
        return toFileURI(new File(path));
    }

    public static String filesystemSafeFileHash(File folder) {
        String hash = Base64.getEncoder().encodeToString(DigestUtils.sha1(folder.getAbsolutePath().getBytes()));
        hash = hash.replaceAll("[^0-9a-zA-Z\\.\\-]", "");
        return hash;
    }

    public static boolean isSubdirectoryOf(File parent, File subdirectory) throws IOException {
        File parentFile = parent.getCanonicalFile();
        File current = subdirectory.getCanonicalFile();
        while (current != null) {
            if (current.equals(parentFile)) {
                return true;
            }
            current = current.getParentFile();
        }
        return false;
    }

    public static void createFilesByPath(File baseDir, String... files) throws IOException {
        for (String file : files) {
            if (file.endsWith("/")) {
                File file1 = new File(baseDir, file);
                file1.mkdirs();
            } else {
                File file1 = new File(baseDir, file);
                file1.getParentFile().mkdirs();
                file1.createNewFile();
            }
        }
    }

    public static String subtractPath(File rootPath, File file) {
        String fullPath = FilenameUtils.separatorsToUnix(file.getParentFile().getPath());
        String basePath = FilenameUtils.separatorsToUnix(rootPath.getPath());
        return StringUtils.removeStart(StringUtils.removeStart(fullPath, basePath), "/");
    }

    public static File createTempFolder() {
        File tempDir = new File(TMP_PARENT_DIR, CRUISE_TMP_FOLDER);
        File dir = new File(tempDir, UUID.randomUUID().toString());
        boolean ret = dir.mkdirs();
        if (!ret) {
            throw new RuntimeException("FileUtil#createTempFolder - Could not create temp folder");
        }
        return dir;
    }

    public static String getCanonicalPath(File workDir) {
        try {
            return workDir.getCanonicalPath();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void deleteDirectoryNoisily(File defaultDirectory) {
        if (!defaultDirectory.exists()) {
            return;
        }

        try {
            FileUtils.deleteDirectory(defaultDirectory);
        } catch (IOException e) {
            throw new RuntimeException("Failed to delete directory: " + defaultDirectory.getAbsolutePath(), e);
        }
    }

    public static String join(File defaultWorkingDir, String actualFileToUse) {
        if (actualFileToUse == null) {
            LOGGER.trace("Using the default Directory->{}", defaultWorkingDir);
            return FilenameUtils.separatorsToUnix(defaultWorkingDir.getPath());
        }
        return applyBaseDirIfRelativeAndNormalize(defaultWorkingDir, new File(actualFileToUse));
    }

    public static String sha1Digest(File file) {
        try (InputStream is = new BufferedInputStream(new FileInputStream(file))) {
            byte[] hash = DigestUtils.sha1(is);
            return Base64.getEncoder().encodeToString(hash);
        } catch (IOException e) {
            throw ExceptionUtils.bomb(e);
        }
    }
}


