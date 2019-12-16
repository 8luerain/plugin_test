package com.xiaomi.shop.build.gradle.plugins

import com.xiaomi.shop.build.gradle.plugins.base.BaseGradlePlugin
import com.xiaomi.shop.build.gradle.plugins.hooker.StableHostResourceHooker
import com.xiaomi.shop.build.gradle.plugins.hooker.manager.TaskHookerManager
import org.gradle.api.Plugin
import org.gradle.api.Project

class HostGradlePlugin extends BaseGradlePlugin {

    HostTaskHookerManager mTaskHookerManager

    @Override
    void apply(Project project) {
        super.apply(project)
    }

    @Override
    protected onBeforePreBuildTask() {
        mTaskHookerManager = new HostTaskHookerManager(project)
        mTaskHookerManager.registerTaskHookers(this)
        loadDependencies(null)
        backupOriginalRFile()
    }

    static class HostTaskHookerManager extends TaskHookerManager {

        HostTaskHookerManager(Project mProject) {
            super(mProject)
        }

        @Override
        void registerTaskHookers(Plugin plugin) {
            registerTaskHooker(new StableHostResourceHooker(mProject, ((HostGradlePlugin) plugin).mAppReleaseVariant))
        }
    }
}
