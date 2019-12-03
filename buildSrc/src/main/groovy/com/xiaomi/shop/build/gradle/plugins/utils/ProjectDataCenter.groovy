package com.xiaomi.shop.build.gradle.plugins.utils


import com.xiaomi.shop.build.gradle.plugins.bean.MergedPackageManifest
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

    private PackageManifest hostPackageManifest
    private PackageManifest pluginPackageManifest
    private MergedPackageManifest mergedPluginPackageManifest


    private boolean hasParse


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
    }

    PackageManifest getHostPackageManifest() {
        if (null == pluginPackageManifest) {
            hostPackageManifest = new PackageManifest(mProject)
            initHostManifest()
        }
        return hostPackageManifest
    }

    PackageManifest getPluginPackageManifest() {
        if (null == pluginPackageManifest) {
            pluginPackageManifest = new PackageManifest(mProject)
        }
        return pluginPackageManifest
    }

    MergedPackageManifest getMergedPluginPackageManifest() {
        if (!PackageManifestUtils.hasAaptProcessed(hostPackageManifest)) {
            throw new IllegalArgumentException("宿主资源初始化失败")
        }
        if (!PackageManifestUtils.hasAaptProcessed(pluginPackageManifest)) {
            throw new IllegalArgumentException("插件资源初始化失败")
        }
        if (null == mergedPluginPackageManifest) {
            mergedPluginPackageManifest = new MergedPackageManifest(hostPackageManifest, pluginPackageManifest , mProject)
        }
        return mergedPluginPackageManifest
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
        hostPackageManifest.dependenciesFile = hostVersions
        hostPackageManifest.originalResourceFile = hostR
    }


}
