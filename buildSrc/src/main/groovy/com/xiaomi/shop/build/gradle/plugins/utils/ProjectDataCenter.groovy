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
    static ProjectDataCenter sInstance

    private Project mProject
    private PluginConfigExtension mPluginConfigExtension

    private PackageManifest hostPackageManifest
    private PackageManifest pluginPackageManifest
    private MergedPackageManifest mergedPluginPackageManifest

    //refresh 是否刷新上次运行数据， 在重新运行plugin时，设为true
    private boolean hostNeedRefresh
    private boolean pluginNeedRefresh
    private boolean mergedPluginNeedRefresh

    static ProjectDataCenter init(Project project) {
        return getInstance(project)
    }

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

    Project getProject() {
        return mProject
    }

    void setProject(Project project) {
        this.mProject = mProject
    }

    boolean getNeedRefresh() {
        return needRefresh
    }

    void setNeedRefresh(boolean needRefresh) {
        println("need data center refresh !!")
        this.hostNeedRefresh = needRefresh
        this.pluginNeedRefresh = needRefresh
        this.mergedPluginNeedRefresh = needRefresh
    }

    PackageManifest getHostPackageManifest() {
        if (null == hostPackageManifest || hostNeedRefresh) {
            println("create new hostPackageManifest ")
            hostNeedRefresh = false
            hostPackageManifest = new PackageManifest(mProject)
            initHostManifest()
        }
        return hostPackageManifest
    }

    def initHostManifest() {
        String targetHost = mPluginConfigExtension.hostPath
        if (!targetHost) {
            def err = new StringBuilder("\n必须需要指定host路径 targetHost = ../xxxProject/app \n")
            throw new InvalidUserDataException(err.toString())
        }
        File hostLocalDir = new File(targetHost)
        if (!hostLocalDir.exists()) {
            def err = "此路径不存在: ${hostLocalDir.canonicalPath}"
            throw new InvalidUserDataException(err)
        }
        File hostR = new File(hostLocalDir, "hooker/original_resource_file.txt")
        File hostDependencies = new File(hostLocalDir, "hooker/dependencies.txt")
        if (!hostR.exists() || !hostDependencies.exists()) {
            def err = new StringBuilder("没有找到 \n" +
                    "[${hostR.canonicalPath}] \n" +
                    "${hostDependencies.canonicalPath}\n," +
                    " 需要先buildHost\n")
            throw new InvalidUserDataException(err.toString())
        }
        hostPackageManifest.dependenciesFile = hostDependencies
        hostPackageManifest.originalResourceTxtFile = hostR
    }


    PackageManifest getPluginPackageManifest() {
        if (null == pluginPackageManifest || pluginNeedRefresh) {
            println("recreate getPluginPackageManifest null ?[${null == pluginPackageManifest}] isNeedFresh[${pluginNeedRefresh}]")
            pluginNeedRefresh = false
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
        if (null == mergedPluginPackageManifest || mergedPluginNeedRefresh) {
            println("create new mergedPluginPackageManifest ")
            mergedPluginNeedRefresh = false
            mergedPluginPackageManifest = new MergedPackageManifest(hostPackageManifest, pluginPackageManifest, mProject)
        }
        return mergedPluginPackageManifest
    }


    PluginConfigExtension getPluginConfigExtension() {
        return mPluginConfigExtension
    }

}
