package com.xiaomi.shop.build.gradle.plugins

import com.android.build.gradle.internal.api.ApplicationVariantImpl
import com.xiaomi.shop.build.gradle.plugins.base.ShopBasePlugin
import com.xiaomi.shop.build.gradle.plugins.hooker.StableHostResourceHooker
import com.xiaomi.shop.build.gradle.plugins.hooker.manager.TaskHookerManager
import org.gradle.api.Project

class ShopHostPlugin extends ShopBasePlugin {

    HostTaskHookerManager mTaskHookerManager

    @Override
    void apply(Project project) {
        super.apply(project)
        mTaskHookerManager = new HostTaskHookerManager(project)
        project.afterEvaluate {
            mTaskHookerManager.registerTaskHookers()
        }
    }

    static class HostTaskHookerManager extends TaskHookerManager {

        HostTaskHookerManager(Project mProject) {
            super(mProject)
        }

        @Override
        void registerTaskHookers() {
            android.applicationVariants.all { ApplicationVariantImpl appVariant ->
                if (!appVariant.buildType.name.equalsIgnoreCase("release")) {
                    return
                }
                registerTaskHooker(new StableHostResourceHooker(mProject, appVariant))
            }
        }
    }
}
