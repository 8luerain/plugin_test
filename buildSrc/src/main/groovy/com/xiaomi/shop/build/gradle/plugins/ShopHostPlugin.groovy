package com.xiaomi.shop.build.gradle.plugins

import com.android.build.gradle.internal.api.ApplicationVariantImpl
import com.xiaomi.shop.build.gradle.plugins.hooker.GenerateLibraryRFileHooker
import com.xiaomi.shop.build.gradle.plugins.hooker.ProcessResourcesHooker
import com.xiaomi.shop.build.gradle.plugins.hooker.StableHostResourceHooker
import com.xiaomi.shop.build.gradle.plugins.hooker.TaskHookerManager
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.internal.reflect.Instantiator
import org.gradle.tooling.provider.model.ToolingModelBuilderRegistry

import javax.inject.Inject

class ShopHostPlugin implements Plugin<Project> {

    TaskHookerManager mTaskHookerManager
    Instantiator mInstantiator

    @Inject
    public ShopHostPlugin(Instantiator instantiator, ToolingModelBuilderRegistry registry) {
        mInstantiator = instantiator
    }

    @Override
    void apply(Project project) {
        mTaskHookerManager = new TaskHookerManager(project, mInstantiator) {
            @Override
            void registerTaskHookers() {
                android.applicationVariants.all { ApplicationVariantImpl appVariant ->
                    if (!appVariant.buildType.name.equalsIgnoreCase("release")) {
                        return
                    }
                    registerTaskHooker(mInstantiator.newInstance(StableHostResourceHooker, project, appVariant))
                }
            }
        }
        project.afterEvaluate {
            mTaskHookerManager.registerTaskHookers();
        }
    }
}