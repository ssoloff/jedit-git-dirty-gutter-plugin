/*
 * Copyright (C) 2015-2016 Steven Soloff
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
package io.github.ssoloff.jedit.plugins.git_dirty_gutter.internal.ui

import io.github.ssoloff.jedit.plugins.git_dirty_gutter.internal.model.ILog
import io.github.ssoloff.jedit.plugins.git_dirty_gutter.internal.test.GitIntegrationSpecification
import io.github.ssoloff.jedit.plugins.git_dirty_gutter.internal.util.AutoResetEvent
import io.github.ssoloff.jedit.plugins.git_dirty_gutter.internal.util.ISupplier
import java.awt.Color
import java.nio.file.Path
import java.util.concurrent.TimeUnit
import javax.swing.SwingUtilities
import spock.lang.Subject
import spock.lang.Title

@Subject(GitBufferHandler)
@Title('Integration tests for GitBufferHandler')
class GitBufferHandlerIntegrationSpec extends GitIntegrationSpecification {
    private static final ADDED_DIRTY_MARK_COLOR = Color.GREEN
    private static final CHANGED_DIRTY_MARK_COLOR = Color.ORANGE
    private static final REMOVED_DIRTY_MARK_COLOR = Color.RED

    private bufferHandler
    private final bufferHandlerListenerEvent = new AutoResetEvent()
    private final bufferHandlerListener = { bufferHandlerListenerEvent.signal() }

    private getDirtyMarkPainterSpecificationForLine(lineIndex) {
        def dirtyMarkPainterSpecification = null
        SwingUtilities.invokeAndWait {
            dirtyMarkPainterSpecification = bufferHandler.getDirtyMarkPainterSpecificationForLine(lineIndex)
        }
        dirtyMarkPainterSpecification
    }

    private void matchesChangedDirtyMarkPainterSpecification(dirtyMarkPainterSpecification) {
        assert dirtyMarkPainterSpecification.color == CHANGED_DIRTY_MARK_COLOR
    }

    private void matchesUnchangedDirtyMarkPainterSpecification(dirtyMarkPainterSpecification) {
        assert dirtyMarkPainterSpecification == DirtyMarkPainterSpecification.NULL
    }

    @SuppressWarnings('UnnecessaryGetter')
    private newBufferHandlerForFile(filePath) {
        def buffer = newBufferForFile(filePath)
        def dirtyMarkPainterSpecificationFactoryContext = Stub(IDirtyMarkPainterSpecificationFactoryContext) {
            getAddedDirtyMarkColor() >> ADDED_DIRTY_MARK_COLOR
            getChangedDirtyMarkColor() >> CHANGED_DIRTY_MARK_COLOR
            getRemovedDirtyMarkColor() >> REMOVED_DIRTY_MARK_COLOR
        }
        def log = Stub(ILog)
        def context = Stub(IGitBufferHandlerContext) {
            getBuffer() >> buffer
            getDirtyMarkPainterSpecificationFactoryContext() >> dirtyMarkPainterSpecificationFactoryContext
            getGitProgramPathSupplier() >> ({ gitProgramPath } as ISupplier<Path>)
            getLog() >> log
            getRepositoryPollTimeInMilliseconds() >> 500
        }
        new GitBufferHandler(context)
    }

    private requestPatchUpdate() {
        SwingUtilities.invokeAndWait {
            bufferHandler.updatePatch()
        }
    }

    private startBufferHandler(filePath) {
        SwingUtilities.invokeAndWait {
            bufferHandler = newBufferHandlerForFile(filePath)
            bufferHandler.addListener(bufferHandlerListener)
            bufferHandler.start()
        }
    }

    private stopBufferHandler() {
        SwingUtilities.invokeAndWait {
            if (bufferHandler) {
                bufferHandler.stop()
                bufferHandler.removeListener(bufferHandlerListener)
            }
        }
    }

    private waitForPatchUpdateNotification() {
        bufferHandlerListenerEvent.await(30, TimeUnit.SECONDS)
    }

    def cleanup() {
        stopBufferHandler()
    }

    def 'when buffer does not differ from HEAD revision at start it should not report dirty lines'() {
        given: 'a file with one line committed on HEAD'
        def filePath = repoPath.resolve('file')
        touchFile(filePath, 'line 1\n')
        addAndCommitFile(filePath)

        when: 'starting the buffer handler for the file'
        startBufferHandler(filePath)

        and: 'waiting for the initial patch update notification'
        waitForPatchUpdateNotification()

        and: 'getting the dirty mark painter specification for line 0'
        def dirtyMarkPainterSpecification = getDirtyMarkPainterSpecificationForLine(0)

        then: 'it should be an UNCHANGED dirty mark painter specification'
        matchesUnchangedDirtyMarkPainterSpecification(dirtyMarkPainterSpecification)
    }

    def 'when buffer differs from HEAD revision at start it should report dirty lines'() {
        given: 'a file with one line committed on HEAD'
        def filePath = repoPath.resolve('file')
        touchFile(filePath, 'line 1\n')
        addAndCommitFile(filePath)

        and: 'modifying the first line of the file'
        touchFile(filePath, 'new line 1\n')

        when: 'starting the buffer handler for the file'
        startBufferHandler(filePath)

        and: 'waiting for the initial patch update notification'
        waitForPatchUpdateNotification()

        and: 'getting the dirty mark painter specification for line 0'
        def dirtyMarkPainterSpecification = getDirtyMarkPainterSpecificationForLine(0)

        then: 'it should be a CHANGED dirty mark painter specification'
        matchesChangedDirtyMarkPainterSpecification(dirtyMarkPainterSpecification)
    }

    def 'when buffer differs from HEAD revision after explicit update request it should report dirty lines'() {
        given: 'a file with one line committed on HEAD'
        def filePath = repoPath.resolve('file')
        touchFile(filePath, 'line 1\n')
        addAndCommitFile(filePath)

        when: 'starting the buffer handler for the file'
        startBufferHandler(filePath)

        and: 'waiting for the initial patch update notification'
        waitForPatchUpdateNotification()

        and: 'modifying the first line of the file'
        touchFile(filePath, 'new line 1\n')

        and: 'requesting a patch update'
        requestPatchUpdate()

        and: 'waiting for the subsequent patch update notification'
        waitForPatchUpdateNotification()

        and: 'getting the dirty mark painter specification for line 0'
        def dirtyMarkPainterSpecification = getDirtyMarkPainterSpecificationForLine(0)

        then: 'it should be a CHANGED dirty mark painter specification'
        matchesChangedDirtyMarkPainterSpecification(dirtyMarkPainterSpecification)
    }

    def 'when buffer does not differ from HEAD revision after commit it should not report dirty lines'() {
        given: 'a file with one line committed on HEAD'
        def filePath = repoPath.resolve('file')
        touchFile(filePath, 'line 1\n')
        addAndCommitFile(filePath)

        when: 'modifying the first line of the file'
        touchFile(filePath, 'new line 1\n')

        and: 'starting the buffer handler for the file'
        startBufferHandler(filePath)

        and: 'waiting for the initial patch update notification'
        waitForPatchUpdateNotification()

        and: 'committing the changes made to the file'
        addAndCommitFile(filePath)

        and: 'waiting for the subsequent patch update notification'
        waitForPatchUpdateNotification()

        and: 'getting the dirty mark painter specification for line 0'
        def dirtyMarkPainterSpecification = getDirtyMarkPainterSpecificationForLine(0)

        then: 'it should be an UNCHANGED dirty mark painter specification'
        matchesUnchangedDirtyMarkPainterSpecification(dirtyMarkPainterSpecification)
    }
}
