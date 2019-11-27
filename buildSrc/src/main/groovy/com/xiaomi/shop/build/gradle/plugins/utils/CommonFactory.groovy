package com.xiaomi.shop.build.gradle.plugins.utils

import com.android.build.gradle.AppPlugin
import com.android.build.gradle.internal.TaskManager
import com.android.build.gradle.internal.tasks.factory.TaskFactory
import com.android.builder.model.Dependencies
import org.gradle.api.Project

/**
 * 根据api版本构造对应对象，应对不同版本差异
 *
 * */
class CommonFactory {
    public static final String GRADLE_VERSION_310 = "V_3.1.0"
    public static final String GRADLE_VERSION_332 = "V_3.3.2"


    String mGradleVersionMinVersion = "1.0.0";
    static CommonFactory mInstance

    CommonFactory() {
        try {
            Class.forName('com.android.builder.core.VariantConfiguration')
        } catch (Throwable e) {
            mGradleVersionMinVersion = GRADLE_VERSION_310;
        }
    }

    static CommonFactory getInstance() {
        if (null == mInstance) {
            synchronized (CommonFactory.class) {
                if (null == mInstance) {
                    mInstance = new CommonFactory()
                }
            }
        }
        return mInstance
    }

    TaskFactory getTaskManager(Project project) {
        TaskFactory factory
        AppPlugin appPlugin = project.plugins.findPlugin(AppPlugin)
        if (mGradleVersionMinVersion == GRADLE_VERSION_310) {
            TaskManager taskManager = Reflect.on(appPlugin).field('taskManager').get()
            factory = taskManager.getTaskFactory()
        } else {
            factory = Reflect.on('com.android.build.gradle.internal.TaskContainerAdaptor')
                    .create(project.tasks)
                    .get()
        }
        return factory
    }

    String getVariantName(def key, def buildType) {
        String variantName
//        if (mGradleVersionMinVersion == GRADLE_VERSION_310) {
//            variantName = Reflect.on('com.android.build.gradle.internal.core.VariantConfiguration')
//                    .call('computeFullName', key, buildType, VariantType., null)
//                    .get()
//        } else {
//            variantName = Reflect.on('com.android.builder.core.VariantConfiguration')
//                    .call('computeFullName', key, buildType, VariantType.DEFAULT, null)
//                    .get()
//        }
        return variantName
    }

    Dependencies getArtifactDependencyGraph(def scope, def downloadSource, def buildMapping, def consumer) {
        return Reflect.on("com.android.build.gradle.internal.ide.dependencies.ArtifactDependencyGraph")
                .create()
                .call("createDependencies", scope, downloadSource, buildMapping, consumer)
                .get()
    }

}
