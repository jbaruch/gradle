/*
 * Copyright 2017 the original author or authors.
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

package org.gradle.language.cpp.internal;

import org.gradle.api.Action;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.type.ArtifactTypeDefinition;
import org.gradle.api.attributes.AttributeContainer;
import org.gradle.api.attributes.Usage;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.file.FileCollection;
import org.gradle.api.file.FileTree;
import org.gradle.api.internal.artifacts.configurations.RoleBasedConfigurationContainerInternal;
import org.gradle.api.internal.attributes.AttributesFactory;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.util.PatternSet;
import org.gradle.internal.Describables;
import org.gradle.internal.DisplayName;
import org.gradle.language.LibraryDependencies;
import org.gradle.language.cpp.CppBinary;
import org.gradle.language.cpp.CppLibrary;
import org.gradle.language.cpp.CppPlatform;
import org.gradle.language.internal.DefaultLibraryDependencies;
import org.gradle.language.nativeplatform.internal.PublicationAwareComponent;
import org.gradle.nativeplatform.Linkage;
import org.gradle.nativeplatform.toolchain.internal.NativeToolChainInternal;
import org.gradle.nativeplatform.toolchain.internal.PlatformToolProvider;

import javax.inject.Inject;
import java.util.Collections;

public abstract class DefaultCppLibrary extends DefaultCppComponent implements CppLibrary, PublicationAwareComponent {
    private final FileCollection publicHeadersWithConvention;
    private final Configuration apiElements;
    private final MainLibraryVariant mainVariant;
    private final DefaultLibraryDependencies dependencies;

    @Inject
    public DefaultCppLibrary(String name, RoleBasedConfigurationContainerInternal configurations, AttributesFactory attributesFactory) {
        super(name);
        publicHeadersWithConvention = createDirView(getPublicHeaders(), "src/" + name + "/public");

        getLinkage().convention(Collections.singleton(Linkage.SHARED));

        dependencies = getObjectFactory().newInstance(DefaultLibraryDependencies.class, getNames().withSuffix("implementation"), getNames().withSuffix("api"));

        Usage apiUsage = getObjectFactory().named(Usage.class, Usage.C_PLUS_PLUS_API);

        this.apiElements = configurations.consumableLocked(getNames().withSuffix("cppApiElements"), conf -> {
            conf.extendsFrom(dependencies.getApiDependencies());
            conf.getAttributes().attribute(Usage.USAGE_ATTRIBUTE, apiUsage);
            conf.getAttributes().attribute(ArtifactTypeDefinition.ARTIFACT_TYPE_ATTRIBUTE, ArtifactTypeDefinition.DIRECTORY_TYPE);
        });

        AttributeContainer publicationAttributes = attributesFactory.mutable();
        publicationAttributes.attribute(Usage.USAGE_ATTRIBUTE, apiUsage);
        publicationAttributes.attribute(ArtifactTypeDefinition.ARTIFACT_TYPE_ATTRIBUTE, ArtifactTypeDefinition.ZIP_TYPE);
        mainVariant = new MainLibraryVariant("api", apiElements, publicationAttributes, getObjectFactory());
    }

    public DefaultCppSharedLibrary addSharedLibrary(NativeVariantIdentity identity, CppPlatform targetPlatform, NativeToolChainInternal toolChain, PlatformToolProvider platformToolProvider) {
        DefaultCppSharedLibrary result = getObjectFactory().newInstance(DefaultCppSharedLibrary.class, getNames().append(identity.getName()), getBaseName(), getCppSource(), getAllHeaderDirs(), getImplementationDependencies(), targetPlatform, toolChain, platformToolProvider, identity);
        getBinaries().add(result);
        return result;
    }

    public DefaultCppStaticLibrary addStaticLibrary(NativeVariantIdentity identity, CppPlatform targetPlatform, NativeToolChainInternal toolChain, PlatformToolProvider platformToolProvider) {
        DefaultCppStaticLibrary result = getObjectFactory().newInstance(DefaultCppStaticLibrary.class, getNames().append(identity.getName()), getBaseName(), getCppSource(), getAllHeaderDirs(), getImplementationDependencies(), targetPlatform, toolChain, platformToolProvider, identity);
        getBinaries().add(result);
        return result;
    }

    @Override
    public DisplayName getDisplayName() {
        return Describables.withTypeAndName("C++ library", getName());
    }

    @Override
    public Configuration getImplementationDependencies() {
        return dependencies.getImplementationDependencies();
    }

    @Override
    public Configuration getApiDependencies() {
        return dependencies.getApiDependencies();
    }

    @Override
    public LibraryDependencies getDependencies() {
        return dependencies;
    }

    public void dependencies(Action<? super LibraryDependencies> action) {
        action.execute(dependencies);
    }

    public Configuration getApiElements() {
        return apiElements;
    }

    @Override
    public MainLibraryVariant getMainPublication() {
        return mainVariant;
    }

    @Override
    public void publicHeaders(Action<? super ConfigurableFileCollection> action) {
        action.execute(getPublicHeaders());
    }

    @Override
    public FileCollection getPublicHeaderDirs() {
        return publicHeadersWithConvention;
    }

    @Override
    public FileTree getPublicHeaderFiles() {
        PatternSet patterns = new PatternSet();
        // if you would like to add more endings to this pattern, make sure to also edit DefaultCppComponent.java and default.vcxproj.filters
        patterns.include("**/*.h");
        patterns.include("**/*.hpp");
        patterns.include("**/*.h++");
        patterns.include("**/*.hxx");
        patterns.include("**/*.hm");
        patterns.include("**/*.inl");
        patterns.include("**/*.inc");
        patterns.include("**/*.xsd");
        return publicHeadersWithConvention.getAsFileTree().matching(patterns);
    }

    @Override
    public FileCollection getAllHeaderDirs() {
        return publicHeadersWithConvention.plus(super.getAllHeaderDirs());
    }

    @Override
    public abstract Property<CppBinary> getDevelopmentBinary();
}
