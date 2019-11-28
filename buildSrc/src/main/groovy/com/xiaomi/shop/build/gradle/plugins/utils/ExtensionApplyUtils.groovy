package com.xiaomi.shop.build.gradle.plugins.utils

import com.xiaomi.shop.build.gradle.plugins.extension.PluginConfigExtension
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.DependencyResolveDetails
import org.gradle.api.artifacts.ResolutionStrategy

class ExtensionApplyUtils {

    static void applyUseHostResourceConfig(Project project) {
        HashSet<String> replacedSet = [] as HashSet
        project.rootProject.subprojects { Project p ->
            if (p.name == project.name) {
                p.configurations.all { Configuration configuration ->
                    configuration.resolutionStrategy { ResolutionStrategy resolutionStrategy ->
                        resolutionStrategy.eachDependency { DependencyResolveDetails details ->
                            PluginConfigExtension pluginConfigExtension = ProjectDataCenter.getInstance(project).pluginConfigExtension
                            def hostDependency = ProjectDataCenter.getInstance(project).hostPackageManifest
                                    .hostDependenciesMap.get("${details.requested.group}:${details.requested.name}")
                            if (hostDependency != null) {
                                if ("${details.requested.version}" != "${hostDependency['version']}") {
                                    String key = "${p.name}:${details.requested}"
                                    if (!replacedSet.contains(key)) {
                                        replacedSet.add(key)
                                        if (pluginConfigExtension.useHostDependencies) {
                                            Log.i 'Dependencies', "ATTENTION: Replaced module [${details.requested}] in project(:${p.name})'s configuration to host version: [${hostDependency['version']}]!"
                                        } else {
//                                        virtualApk.setFlag('tip.forceUseHostDependences', true)
                                            Log.i 'Dependencies', "WARNING: [${details.requested}] in project(:${p.name})'s configuration will be occupied by Host App! Please change it to host version: [${hostDependency['group']}:${hostDependency['name']}:${hostDependency['version']}]."
                                        }
                                    }

                                    if (pluginConfigExtension.useHostDependencies) {
                                        details.useVersion(hostDependency['version'])
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

    }
}
