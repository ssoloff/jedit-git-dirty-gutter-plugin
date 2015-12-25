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

import java.io.Writer;

/**
 * A Git process runner.
 */
interface IGitRunner {
    /**
     * Runs a new Git process.
     *
     * @param outWriter
     *        The writer that will receive the standard output of the process.
     * @param args
     *        The Git command arguments.
     * 
     * @throws GitRunnerException
     *         If the process exits with an error.
     * @throws InterruptedException
     *         If interrupted while waiting for the process to exit.
     */
    void run(Writer outWriter, String... args) throws GitRunnerException, InterruptedException;
}
