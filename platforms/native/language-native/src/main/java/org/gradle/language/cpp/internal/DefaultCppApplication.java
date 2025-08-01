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
import org.gradle.api.provider.Property;
import org.gradle.internal.Describables;
import org.gradle.internal.DisplayName;
import org.gradle.language.ComponentDependencies;
import org.gradle.language.cpp.CppApplication;
import org.gradle.language.cpp.CppExecutable;
import org.gradle.language.cpp.CppPlatform;
import org.gradle.language.internal.DefaultComponentDependencies;
import org.gradle.language.nativeplatform.internal.PublicationAwareComponent;
import org.gradle.nativeplatform.toolchain.internal.NativeToolChainInternal;
import org.gradle.nativeplatform.toolchain.internal.PlatformToolProvider;

import javax.inject.Inject;

public abstract class DefaultCppApplication extends DefaultCppComponent implements CppApplication, PublicationAwareComponent {
    private final MainExecutableVariant mainVariant;
    private final DefaultComponentDependencies dependencies;

    @Inject
    public DefaultCppApplication(String name) {
        super(name);
        this.dependencies = getObjectFactory().newInstance(DefaultComponentDependencies.class, getNames().withSuffix("implementation"));
        this.mainVariant = new MainExecutableVariant(getObjectFactory());
    }

    public DefaultCppExecutable addExecutable(NativeVariantIdentity identity, CppPlatform targetPlatform, NativeToolChainInternal toolChain, PlatformToolProvider platformToolProvider) {
        DefaultCppExecutable result = getObjectFactory().newInstance(DefaultCppExecutable.class, getNames().append(identity.getName()), getBaseName(), getCppSource(), getPrivateHeaderDirs(), getImplementationDependencies(), targetPlatform, toolChain, platformToolProvider, identity);
        getBinaries().add(result);
        return result;
    }

    @Override
    public DisplayName getDisplayName() {
        return Describables.withTypeAndName("C++ application", getName());
    }

    @Override
    public Configuration getImplementationDependencies() {
        return dependencies.getImplementationDependencies();
    }

    @Override
    public ComponentDependencies getDependencies() {
        return dependencies;
    }

    public void dependencies(Action<? super ComponentDependencies> action) {
        action.execute(dependencies);
    }

    @Override
    public MainExecutableVariant getMainPublication() {
        return mainVariant;
    }

    @Override
    public abstract Property<CppExecutable> getDevelopmentBinary();
}
