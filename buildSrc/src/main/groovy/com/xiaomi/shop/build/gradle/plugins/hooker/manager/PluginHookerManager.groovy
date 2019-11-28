package com.xiaomi.shop.build.gradle.plugins.hooker.manager

import com.android.build.gradle.internal.api.ApplicationVariantImpl
import com.xiaomi.shop.build.gradle.plugins.hooker.ProcessResourcesHooker
import org.gradle.api.Project
import org.gradle.internal.reflect.Instantiator

class PluginHookerManager extends TaskHookerManager {
    Instantiator instantiator

    PluginHookerManager(Project project, Instantiator instantiator) {
        super(project, instantiator)
        this.instantiator = instantiator
    }

    @Override
    void registerTaskHookers() {
        android.applicationVariants.all { ApplicationVariantImpl appVariant ->
            if (!appVariant.buildType.name.equalsIgnoreCase("release")) {
                return
            }
            registerTaskHooker(instantiator.newInstance(ProcessResourcesHooker, project, appVariant))
//            registerTaskHooker(instantiator.newInstance(GenerateLibraryRFileHooker, project, appVariant))
//                registerTaskHooker(instantiator.newInstance(PrepareDependenciesHooker, project, appVariant))
//                registerTaskHooker(instantiator.newInstance(MergeAssetsHooker, project, appVariant))
//                registerTaskHooker(instantiator.newInstance(MergeManifestsHooker, project, appVariant))
//                registerTaskHooker(instantiator.newInstance(MergeJniLibsHooker, project, appVariant))
//                registerTaskHooker(instantiator.newInstance(ShrinkResourcesHooker, project, appVariant))
//                registerTaskHooker(instantiator.newInstance(ProguardHooker, project, appVariant))
//                registerTaskHooker(instantiator.newInstance(DxTaskHooker, project, appVariant))
        }
    }
}
