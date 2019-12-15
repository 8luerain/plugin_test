package com.xiaomi.shop.build.gradle.plugins.hooker.manager

import com.android.build.gradle.internal.api.ApplicationVariantImpl
import com.xiaomi.shop.build.gradle.plugins.hooker.LinkApplicationAndroidResourcesTaskHooker
import org.gradle.api.Project

class PluginHookerManager extends TaskHookerManager {

    PluginHookerManager(Project project) {
        super(project)
    }

    @Override
    void registerTaskHookers() {
        android.applicationVariants.all { ApplicationVariantImpl appVariant ->
            if (!appVariant.buildType.name.equalsIgnoreCase("release")) {
                return
            }
            registerTaskHooker(new LinkApplicationAndroidResourcesTaskHooker(mProject, appVariant))
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
