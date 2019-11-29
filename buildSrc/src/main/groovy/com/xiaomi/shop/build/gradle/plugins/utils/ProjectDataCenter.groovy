package com.xiaomi.shop.build.gradle.plugins.utils

import com.xiaomi.shop.build.gradle.plugins.bean.PackageManifest
import com.xiaomi.shop.build.gradle.plugins.extension.PluginConfigExtension
import org.gradle.api.InvalidUserDataException
import org.gradle.api.Project

/**
 * 汇总处理host和工程中各个plugin和lib的数据中心，通信中心
 *
 * */
class ProjectDataCenter {
    static sInstance

    private Project mProject
    private PluginConfigExtension mPluginConfigExtension

    PackageManifest hostPackageManifest
    PackageManifest pluginPackageManifest

    //过滤资源后，需要重新打包的资源清单
    PackageManifest rePackageManifest

    boolean hasParse


    static ProjectDataCenter getInstance(Project project) {
        if (null == sInstance) {
            synchronized (ProjectDataCenter.class) {
                if (null == sInstance) {
                    sInstance = new ProjectDataCenter(project)
                }
            }
        }
        return sInstance;
    }

    ProjectDataCenter(Project project) {
        mProject = project
        mPluginConfigExtension = mProject.pluginconfig;
        initHostManifest()
    }

    PackageManifest getHostPackageManifest() {
        return hostPackageManifest
    }

    PackageManifest getPluginPackageManifest() {
        return pluginPackageManifest
    }


    PluginConfigExtension getPluginConfigExtension() {
        return mPluginConfigExtension
    }

    def initHostManifest() {
        String targetHost = mPluginConfigExtension.hostPath
        if (!targetHost) {
            def err = new StringBuilder('\nyou should specify the targetHost in build.gradle, e.g.: \n')
            err.append('    virtualApk {\n')
            err.append('        //when target Host in local machine, value is host application directory\n')
            err.append('        targetHost = ../xxxProject/app \n')
            err.append('    }\n')
            throw new InvalidUserDataException(err.toString())
        }
        File hostLocalDir = new File(targetHost)
        if (!hostLocalDir.exists()) {
            def err = "The directory of host application doesn't exist! Dir: ${hostLocalDir.canonicalPath}"
            throw new InvalidUserDataException(err)
        }

        File hostR = new File(hostLocalDir, "hooker/original_resource_file.txt")
        if (!hostR.exists()) {
            def err = new StringBuilder("Can't find ${hostR.canonicalPath}, please check up your host application\n")
            throw new InvalidUserDataException(err.toString())
        }
        File hostVersions = new File(hostLocalDir, "hooker/dependencies.txt")
        if (!hostVersions.exists()) {
            def err = new StringBuilder("Can't find ${hostVersions.canonicalPath}, please check up your host application\n")
            throw new InvalidUserDataException(err.toString())
        }
        hostPackageManifest = new PackageManifest()
        hostPackageManifest.dependenciesFile = hostVersions
        hostPackageManifest.originalResourceFile = hostR

    }

    def createRePackageManifest() {
        if (null == rePackageManifest) {
            rePackageManifest = new PackageManifest()
        }
        def pluginResource = pluginPackageManifest.resourcesMap
        def hostResources = hostPackageManifest.resourcesMap
        pluginResource.values().each {
            def index = hostResources.get(it.resourceType).indexOf(it)
            if (index >= 0) {
//                it.newResourceId = hostResources.get(it.resourceType).get(index).resourceId
//                hostResources.get(it.resourceType).set(index, it)
            } else {
                pluginResource.put(it.resourceType, it)
            }
        }

        allStyleables.each {
            def index = hostStyleables.indexOf(it)
            if (index >= 0) {
                /**
                 * Do not support the same name but different content styleable entry
                 */
                it.value = hostStyleables.get(index).value
                hostStyleables.set(index, it)
            } else {
                pluginStyleables.add(it)
            }
        }
    }
}
