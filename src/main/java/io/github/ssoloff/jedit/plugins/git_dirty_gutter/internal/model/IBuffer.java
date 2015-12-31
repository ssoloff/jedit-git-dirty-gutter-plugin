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

package io.github.ssoloff.jedit.plugins.git_dirty_gutter.internal.model;

import java.nio.file.Path;
import java.util.List;

/**
 * A jEdit buffer.
 */
public interface IBuffer {
    /**
     * Gets the path to the file associated with the buffer.
     *
     * @return The path to the file associated with the buffer.
     */
    public Path getFilePath();

    /**
     * Gets the lines of text in the buffer.
     *
     * @return The lines of text in the buffer.
     */
    public List<String> getLines();
}