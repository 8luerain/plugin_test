package com.xiaomi.shop.build.gradle.plugins.utils


import com.xiaomi.shop.build.gradle.plugins.bean.HostPackageManifest
import com.xiaomi.shop.build.gradle.plugins.extension.PluginConfigExtension
import org.gradle.api.InvalidUserDataException
import org.gradle.api.Project

/**
 * 汇总处理host和工程中各个plugin和lib的数据中心，通信中心
 *
 * */
class ProjectDataBridge {
    boolean hasParse
    static sInstance;
    HostPackageManifest hostPackageManifest
    private Project mProject
    private PluginConfigExtension mPluginConfigExtension

    static ProjectDataBridge getInstance(Project project) {
        if (null == sInstance) {
            synchronized (ProjectDataBridge.class) {
                if (null == sInstance) {
                    sInstance = new ProjectDataBridge(project)
                }
            }
        }
        return sInstance;
    }

    HostPackageManifest getHostPackageManifest() {
        return hostPackageManifest
    }

    ProjectDataBridge(Project project) {
        mProject = project
        mPluginConfigExtension = mProject.pluginconfig;
        initHostManifest()
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
        hostPackageManifest = new HostPackageManifest()
        hostPackageManifest.dependenciesFile = hostVersions
        hostPackageManifest.originalResourceFile = hostR

    }
}
