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

package io.github.ssoloff.jedit.plugins.git_dirty_gutter.internal.ui;

import io.github.ssoloff.jedit.plugins.git_dirty_gutter.internal.model.DirtyMarkType;
import io.github.ssoloff.jedit.plugins.git_dirty_gutter.internal.util.Properties;
import java.awt.Color;
import java.awt.Graphics2D;
import lcm.painters.ColoredRectWithStripsPainter;
import lcm.painters.DirtyMarkPainter;
import org.gjt.sp.jedit.Buffer;
import org.gjt.sp.jedit.textarea.Gutter;

/**
 * A factory for creating dirty mark painters for various types of dirty marks.
 */
public final class DirtyMarkPainterFactory {
    private static final DirtyMarkPainter NULL_DIRTY_MARK_PAINTER = new DirtyMarkPainter() {
        @Override
        public void paint(final Graphics2D g, final Gutter gutter, final int y, final int height, final Buffer buffer,
                final int lineIndex) {
            // do nothing
        }
    };

    private DirtyMarkPainterFactory() {
    }

    /**
     * Creates a new dirty mark painter for the specified type of dirty mark.
     *
     * @param dirtyMarkType
     *        The type of dirty mark to be painted.
     *
     * @return A new dirty mark painter.
     */
    public static DirtyMarkPainter createDirtyMarkPainter(final DirtyMarkType dirtyMarkType) {
        if (dirtyMarkType == DirtyMarkType.UNCHANGED) {
            return NULL_DIRTY_MARK_PAINTER;
        }

        final ColoredRectWithStripsPainter dirtyMarkPainter = new ColoredRectWithStripsPainter();

        final boolean isTopStripPainted = isTopStripPainted(dirtyMarkType);
        final boolean isBodyPainted = isBodyPainted(dirtyMarkType);
        final boolean isBottomStripPainted = isBottomStripPainted(dirtyMarkType);
        dirtyMarkPainter.setParts(isTopStripPainted, isBodyPainted, isBottomStripPainted);

        final Color color = getColor(dirtyMarkType);
        if (isBodyPainted) {
            dirtyMarkPainter.setColor(color);
        }
        if (isTopStripPainted || isBottomStripPainted) {
            dirtyMarkPainter.setStripColor(color);
        }

        return dirtyMarkPainter;
    }

    private static Color getColor(final DirtyMarkType dirtyMarkType) {
        switch (dirtyMarkType) {
            case ADDED:
                return Properties.getAddedDirtyMarkColor();

            case CHANGED:
                return Properties.getChangedDirtyMarkColor();

            case REMOVED_ABOVE:
            case REMOVED_ABOVE_AND_BELOW:
            case REMOVED_BELOW:
                return Properties.getRemovedDirtyMarkColor();

            default:
                throw new AssertionError("unsupported dirty mark type"); //$NON-NLS-1$
        }
    }

    private static boolean isBodyPainted(final DirtyMarkType dirtyMarkType) {
        return (dirtyMarkType == DirtyMarkType.ADDED) || (dirtyMarkType == DirtyMarkType.CHANGED);
    }

    private static boolean isBottomStripPainted(final DirtyMarkType dirtyMarkType) {
        return (dirtyMarkType == DirtyMarkType.REMOVED_BELOW)
                || (dirtyMarkType == DirtyMarkType.REMOVED_ABOVE_AND_BELOW);
    }

    private static boolean isTopStripPainted(final DirtyMarkType dirtyMarkType) {
        return (dirtyMarkType == DirtyMarkType.REMOVED_ABOVE)
                || (dirtyMarkType == DirtyMarkType.REMOVED_ABOVE_AND_BELOW);
    }
}