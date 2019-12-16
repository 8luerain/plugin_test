package com.xiaomi.shop.build.gradle.plugins.hooker

import com.android.build.gradle.api.ApkVariant
import com.xiaomi.shop.build.gradle.plugins.HostGradlePlugin
import org.gradle.api.DefaultTask
import org.gradle.api.Project

/**
 * 负责收集project所有的依赖,主要包括三种 1:aar类型 2:jar类型 3:project类型
 * preBuildTask为占位的锚点
 */
class HostPreBuildTaskHooker extends GradleTaskHooker<DefaultTask> {
    HostGradlePlugin plugin


    HostPreBuildTaskHooker(Project project, ApkVariant apkVariant) {
        super(project, apkVariant)
        plugin = project.plugins.findPlugin(HostGradlePlugin.class)
    }

    @Override
    String getTaskName() {
        return scope.getTaskName('pre', 'Build')
    }


    @Override
    void beforeTaskExecute(DefaultTask task) {
        println("HostPreBuildTaskHooker beforeTaskExecute")

        if (plugin.mDependenciesFile.exists()) {
            plugin.mDependenciesFile.delete()
        }
        plugin.loadDependencies(null)
        plugin.backupOriginalRFile()

    }

    @Override
    void afterTaskExecute(DefaultTask task) {
        println("HostPreBuildTaskHooker afterTaskExecute")
    }

}