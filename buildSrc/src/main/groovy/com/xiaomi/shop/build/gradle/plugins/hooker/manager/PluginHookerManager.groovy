package com.xiaomi.shop.build.gradle.plugins.hooker.manager

import com.android.build.gradle.api.ApplicationVariant
import com.xiaomi.shop.build.gradle.plugins.base.BaseGradlePlugin
import com.xiaomi.shop.build.gradle.plugins.hooker.PluginPreBuildTaskHooker
import com.xiaomi.shop.build.gradle.plugins.hooker.LinkApplicationAndroidResourcesTaskHooker
import org.gradle.api.Plugin
import org.gradle.api.Project

class PluginHookerManager extends TaskHookerManager {

    PluginHookerManager(Project project) {
        super(project)
    }

    @Override
    void registerTaskHookers(Plugin plugin) {
        ApplicationVariant variant = ((BaseGradlePlugin) plugin).mAppReleaseVariant
        registerTaskHooker(new LinkApplicationAndroidResourcesTaskHooker(mProject, variant))
        registerTaskHooker(new PluginPreBuildTaskHooker(mProject, variant))
    }
}
