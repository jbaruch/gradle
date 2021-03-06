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

import org.gradle.api.plugins.ExtensionAware
import org.gradle.language.HeaderExportingSourceSet
import org.gradle.nativebinaries.NativeBinary
import org.gradle.nativebinaries.internal.ArchitectureInternal
import org.gradle.nativebinaries.language.PreprocessingTool
import org.gradle.nativebinaries.toolchain.internal.MacroArgsConverter

abstract class VisualStudioProjectConfiguration {
    private final NativeBinary binary

    VisualStudioProjectConfiguration(NativeBinary binary) {
        this.binary = binary
    }

    String getName() {
        return "${configurationName}|${platformName}"
    }

    String getConfigurationName() {
        def configName = binary.buildType.name
        if (binary.component.flavors.size() > 1) {
            configName = "${binary.flavor.name}_${configName}"
        }
        return configName
    }

    String getPlatformName() {
        ArchitectureInternal arch = binary.targetPlatform.architecture as ArchitectureInternal
        return arch.ia64 ? "Itanium" : arch.amd64 ? "x64" : "Win32"
    }

    abstract String getType()

    String getBuildTask() {
        return binary.name
    }

    String getCleanTask() {
        return "clean" + binary.name.capitalize()
    }

    File getOutputFile() {
        return binary.outputFile
    }

    boolean isDebug() {
        return binary.buildType.name != 'release'
    }

    List<String> getDefines() {
        PreprocessingTool compilerTool = findCompiler()
        return compilerTool == null ? [] : new MacroArgsConverter().transform(compilerTool.macros)
    }

    private PreprocessingTool findCompiler() {
        ExtensionAware extendedBinary = binary as ExtensionAware;
        return extendedBinary.extensions.findByName('cppCompiler') ?: extendedBinary.extensions.findByName('cCompiler') as PreprocessingTool
    }

    List<File> getIncludePaths() {
        List<File> includes = []
        binary.source.withType(HeaderExportingSourceSet).each { HeaderExportingSourceSet sourceSet ->
            includes.addAll sourceSet.exportedHeaders.srcDirs
        }
        binary.libs*.includeRoots.each {
            includes.addAll it.files
        }
        return includes
    }
}
