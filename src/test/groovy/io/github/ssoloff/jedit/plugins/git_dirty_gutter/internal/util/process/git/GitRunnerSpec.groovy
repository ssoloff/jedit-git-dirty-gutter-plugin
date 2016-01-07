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
package io.github.ssoloff.jedit.plugins.git_dirty_gutter.internal.util.process.git

import io.github.ssoloff.jedit.plugins.git_dirty_gutter.internal.util.process.IProcessRunner
import java.nio.file.Path
import java.nio.file.Paths
import spock.lang.Specification

class GitRunnerSpec extends Specification {
    private static final PROGRAM_PATH = Paths.get('git')
    private static final WORKING_DIR_PATH = Paths.get('workingDir')

    private newGitRunner(processRunner) {
        new GitRunner(processRunner, WORKING_DIR_PATH, PROGRAM_PATH)
    }

    def 'it should pass configured working directory to process runner'() {
        given:
        def processRunner = Mock(IProcessRunner)
        def gitRunner = newGitRunner(processRunner)

        when:
        gitRunner.run(new StringWriter())

        then:
        1 * processRunner.run(_, _, WORKING_DIR_PATH, _)
    }

    def 'it should pass configured git command line to process runner'() {
        given:
        def processRunner = Mock(IProcessRunner)
        def gitRunner = newGitRunner(processRunner)

        when:
        gitRunner.run(new StringWriter(), 'arg1', 'arg2')

        then:
        1 * processRunner.run(_, _, _, [PROGRAM_PATH.toString(), 'arg1', 'arg2'])
    }

    def 'when the process exits without error and when the exit code is zero it should capture stdout'() {
        given:
        def processRunner = Stub(IProcessRunner)
        processRunner.run(_, _, _, _) >> { Writer outWriter, Writer errWriter, Path workingDirPath, String[] command ->
            outWriter.write('stdout-line-1\n')
            outWriter.write('stdout-line-2\n')
            0
        }
        def gitRunner = newGitRunner(processRunner)
        def outWriter = new StringWriter()

        when:
        def result = gitRunner.run(outWriter)

        then:
        with(result) {
            command == [PROGRAM_PATH.toString()]
            exitCode == 0
            workingDirPath == WORKING_DIR_PATH
        }
        outWriter.toString() == 'stdout-line-1\nstdout-line-2\n'
    }

    def 'when the process exits without error and when the exit code is nonzero it should capture stdout'() {
        given:
        def processRunner = Stub(IProcessRunner)
        processRunner.run(_, _, _, _) >> { Writer outWriter, Writer errWriter, Path workingDirPath, String[] command ->
            outWriter.write('stdout-line-1\n')
            outWriter.write('stdout-line-2\n')
            1
        }
        def gitRunner = newGitRunner(processRunner)
        def outWriter = new StringWriter()

        when:
        def result = gitRunner.run(outWriter)

        then:
        with(result) {
            command == [PROGRAM_PATH.toString()]
            exitCode == 1
            workingDirPath == WORKING_DIR_PATH
        }
        outWriter.toString() == 'stdout-line-1\nstdout-line-2\n'
    }

    def 'when the process exits with error it should throw an exception'() {
        given:
        def processRunner = Stub(IProcessRunner)
        processRunner.run(_, _, _, _) >> { Writer outWriter, Writer errWriter, Path workingDirPath, String[] command ->
            errWriter.write('stderr-line-1\n')
            errWriter.write('stderr-line-2\n')
            1
        }
        def gitRunner = newGitRunner(processRunner)

        when:
        gitRunner.run(new StringWriter(), 'arg1', 'arg2')

        then:
        def e = thrown(GitException)
        with(e) {
            command == [PROGRAM_PATH.toString(), 'arg1', 'arg2']
            error == 'stderr-line-1\nstderr-line-2\n'
            exitCode == 1
            workingDirPath == WORKING_DIR_PATH
        }
    }
}
