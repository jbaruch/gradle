/*
 * Copyright 2014 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.gradle.internal.nativeplatform.filesystem

import spock.lang.Specification

class GenericFileSystemTest extends Specification {
    def fileModeMutator = Stub(FileModeMutator)
    def fileSystem = new GenericFileSystem(fileModeMutator, Stub(Stat), Stub(Symlink))

    def "wraps failure to set file mode"() {
        def failure = new RuntimeException()
        def file = new File("does-not-exist")

        given:
        fileModeMutator.chmod(_, _) >> { throw failure }

        when:
        fileSystem.chmod(file, 0640)

        then:
        FileException e = thrown()
        e.message == "Could not set file mode 640 on '$file'."
    }
}
