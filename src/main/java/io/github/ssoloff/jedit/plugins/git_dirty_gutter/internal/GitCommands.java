/*
 * Copyright (C) 2015 Steven Soloff
 *
 * This program is free software; you can redistribute it and/or modify
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
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */

package io.github.ssoloff.jedit.plugins.git_dirty_gutter.internal;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import org.eclipse.jdt.annotation.Nullable;

/**
 * A facade for running various custom Git commands required by the plugin.
 */
final class GitCommands {
    private final IGitRunner gitRunner;

    /**
     * Initializes a new instance of the {@code GitCommands} class.
     *
     * @param gitRunner
     *        The Git process runner.
     */
    GitCommands(final IGitRunner gitRunner) {
        this.gitRunner = gitRunner;
    }

    /**
     * Calculates the changes between two files and sends the difference in
     * unified format to the specified writer.
     *
     * @param originalFilePath
     *        The path to the file containing the original content.
     * @param newFilePath
     *        The path to the file containing the new content.
     * @param writer
     *        The writer that will receive the difference in unified format.
     *
     * @return {@code true} if the files are different; {@code false} if the
     *         files are the same.
     *
     * @throws GitException
     *         If the Git process exits with an error.
     * @throws IOException
     *         If an error occurs while processing the Git process output.
     * @throws InterruptedException
     *         If interrupted while waiting for the Git process to exit.
     */
    boolean diffFiles(final Path originalFilePath, final Path newFilePath, final Writer writer)
            throws GitException, IOException, InterruptedException {
        final String[] args = new String[] {
            "diff", //$NON-NLS-1$
            "--no-index", //$NON-NLS-1$
            originalFilePath.toString(), //
            newFilePath.toString() //
        };
        final int exitCode = gitRunner.run(writer, args);
        if (exitCode == 0) {
            return false;
        } else if (exitCode == 1) {
            return true;
        } else {
            throw new RuntimeException(String.format("unexpected Git exit code: %d", Integer.valueOf(exitCode))); //$NON-NLS-1$
        }
    }

    /**
     * Gets the repository-relative path of the specified file at the HEAD
     * revision.
     *
     * @param filePath
     *        The path to the file whose repository-relative path is desired.
     *
     * @return The repository-relative path of the specified file at the HEAD
     *         revision or {@code null} if the file does not exist in the
     *         repository at the HEAD revision.
     *
     * @throws GitException
     *         If the Git process exits with an error.
     * @throws IOException
     *         If an error occurs while processing the Git process output.
     * @throws InterruptedException
     *         If interrupted while waiting for the Git process to exit.
     */
    @Nullable
    Path getRepoRelativeFilePathAtHeadRevision(final Path filePath)
            throws GitException, IOException, InterruptedException {
        final StringWriter outWriter = new StringWriter();
        final String[] args = new String[] {
            "ls-tree", //$NON-NLS-1$
            "--full-name", //$NON-NLS-1$
            "--name-only", //$NON-NLS-1$
            "HEAD", //$NON-NLS-1$
            filePath.toString() //
        };
        gitRunner.run(outWriter, args);
        final List<String> lines = readAllLines(outWriter);
        return (lines.size() == 1) ? Paths.get(lines.get(0)) : null;
    }

    private static List<String> readAllLines(final StringWriter writer) throws IOException {
        final List<String> lines = new ArrayList<>();
        try (final BufferedReader reader = new BufferedReader(new StringReader(writer.toString()))) {
            String line = null;
            while ((line = reader.readLine()) != null) {
                lines.add(line);
            }
        }
        return lines;
    }

    /**
     * Reads the content of the specified file at the HEAD revision and sends it
     * to the specified writer.
     *
     * @param repoRelativeFilePath
     *        The repository-relative path of the file whose content is to be
     *        read.
     * @param writer
     *        The writer that will receive the file content.
     *
     * @throws GitException
     *         If the Git process exits with an error.
     * @throws IOException
     *         If an error occurs while processing the Git process output.
     * @throws InterruptedException
     *         If interrupted while waiting for the Git process to exit.
     */
    void readFileContentAtHeadRevision(final Path repoRelativeFilePath, final Writer writer)
            throws GitException, IOException, InterruptedException {
        final String[] args = new String[] {
            "show", //$NON-NLS-1$
            String.format("HEAD:%s", repoRelativeFilePath) //$NON-NLS-1$
        };
        gitRunner.run(writer, args);
    }
}
