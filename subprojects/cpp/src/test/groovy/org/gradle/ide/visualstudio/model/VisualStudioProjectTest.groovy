/*
 * Copyright 2013 the original author or authors.
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

package org.gradle.ide.visualstudio.model

import org.gradle.api.file.SourceDirectorySet
import org.gradle.api.internal.DefaultDomainObjectSet
import org.gradle.language.DependentSourceSet
import org.gradle.language.HeaderExportingSourceSet
import org.gradle.language.base.LanguageSourceSet
import org.gradle.language.cpp.CppSourceSet
import org.gradle.nativebinaries.Library
import org.gradle.nativebinaries.LibraryBinary
import org.gradle.nativebinaries.NativeComponent
import spock.lang.Specification

class VisualStudioProjectTest extends Specification {
    def component = Mock(NativeComponent)
    def vsProject = new TestVisualStudioProject(component, "Suffix")

    def "names"() {
        when:
        component.name >> "componentName"

        then:
        vsProject.name == "ComponentNameSuffix"
        vsProject.projectFile == "ComponentNameSuffix.vcxproj"
        vsProject.filtersFile == "ComponentNameSuffix.vcxproj.filters"
    }

    def "includes source files from all source sets"() {
        when:
        def file1 = Mock(File)
        def file2 = Mock(File)
        def file3 = Mock(File)
        def sourceSet1 = sourceSet(file1, file2)
        def sourceSet2 = sourceSet(file3)
        component.source >> new DefaultDomainObjectSet<LanguageSourceSet>(CppSourceSet, [sourceSet1, sourceSet2])

        then:
        vsProject.sourceFiles == [file1, file2, file3]
    }

    def "includes header files from all source sets"() {
        when:
        def file1 = Mock(File)
        def file2 = Mock(File)
        def file3 = Mock(File)
        def sourceSet1 = headerSourceSet(file1, file2)
        def sourceSet2 = headerSourceSet(file3)
        component.source >> new DefaultDomainObjectSet<LanguageSourceSet>(CppSourceSet, [sourceSet1, sourceSet2])

        then:
        vsProject.headerFiles == [file1, file2, file3]
    }

    def "includes library dependencies from all source sets"() {
        when:
        def lib1 = Mock(Library)
        def lib2 = Mock(Library)
        def binary2 = Mock(LibraryBinary)
        binary2.component >> lib2
        def lib3 = Mock(Library)

        def sourceSet1 = dependentSourceSet(lib1, binary2)
        def sourceSet2 = dependentSourceSet(lib3)

        component.source >> new DefaultDomainObjectSet<LanguageSourceSet>(CppSourceSet, [sourceSet1, sourceSet2])

        then:
        vsProject.libraryDependencies == [lib1, lib2, lib3]
    }

    private LanguageSourceSet sourceSet(File... files) {
        def allFiles = files as Set
        def sourceSet = Mock(LanguageSourceSet)
        def sourceDirs = Mock(SourceDirectorySet)
        1 * sourceSet.source >> sourceDirs
        1 * sourceDirs.files >> allFiles
        return sourceSet
    }

    private HeaderExportingSourceSet headerSourceSet(File... files) {
        def allFiles = files as Set
        def sourceSet = Mock(HeaderExportingSourceSet)
        def sourceDirs = Mock(SourceDirectorySet)
        1 * sourceSet.exportedHeaders >> sourceDirs
        1 * sourceDirs.files >> allFiles
        return sourceSet
    }

    private DependentSourceSet dependentSourceSet(Object... libs) {
        def sourceSet = Mock(DependentSourceSet)
        1 * sourceSet.libs >> libs
        return sourceSet
    }

    private static class TestVisualStudioProject extends VisualStudioProject {
        TestVisualStudioProject(NativeComponent component, String nameSuffix) {
            super(component, nameSuffix)
        }

        @Override
        List<? extends VisualStudioProjectConfiguration> getConfigurations() {
            return null
        }
    }
}
