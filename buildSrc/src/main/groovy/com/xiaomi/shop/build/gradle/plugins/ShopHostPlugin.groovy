package com.xiaomi.shop.build.gradle.plugins

import com.android.build.gradle.internal.api.ApplicationVariantImpl
import com.xiaomi.shop.build.gradle.plugins.base.ShopBasePlugin
import com.xiaomi.shop.build.gradle.plugins.hooker.StableHostResourceHooker
import com.xiaomi.shop.build.gradle.plugins.hooker.manager.TaskHookerManager
import org.gradle.api.Project
import org.gradle.internal.reflect.Instantiator
import org.gradle.tooling.provider.model.ToolingModelBuilderRegistry

import javax.inject.Inject

class ShopHostPlugin extends ShopBasePlugin {

    TaskHookerManager mTaskHookerManager

    @Inject
    ShopHostPlugin(Instantiator instantiator, ToolingModelBuilderRegistry registry) {
        super(instantiator, registry)
    }

    @Override
    void apply(Project project) {
        super.apply(project)
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
