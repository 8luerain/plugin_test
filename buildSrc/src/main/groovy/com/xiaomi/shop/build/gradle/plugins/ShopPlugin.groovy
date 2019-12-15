package com.xiaomi.shop.build.gradle.plugins

import com.android.build.gradle.AppExtension
import com.android.build.gradle.AppPlugin
import com.android.build.gradle.internal.api.ApplicationVariantImpl
import com.android.build.gradle.internal.variant.VariantFactory
import com.xiaomi.shop.build.gradle.plugins.base.ShopBasePlugin
import com.xiaomi.shop.build.gradle.plugins.extension.PluginConfigExtension
import com.xiaomi.shop.build.gradle.plugins.hooker.manager.PluginHookerManager
import com.xiaomi.shop.build.gradle.plugins.utils.ExtensionApplyUtils
import com.xiaomi.shop.build.gradle.plugins.utils.ProjectDataCenter
import com.xiaomi.shop.build.gradle.plugins.utils.Reflect
import org.gradle.api.Project

import java.lang.reflect.InvocationHandler
import java.lang.reflect.Method
import java.lang.reflect.Proxy

class ShopPlugin extends ShopBasePlugin {

    AppPlugin mAppPlugin
    AppExtension mAndroidExtension

    @Override
    void apply(Project project) {
        super.apply(project)
        println("ShopPlugin apply()")
        mAppPlugin = project.plugins.findPlugin(AppPlugin)
        mAndroidExtension = project.getExtensions().findByType(AppExtension)
        injectBaseExtension(project)
        createAaptWorkspace()
        ProjectDataCenter.init(project).needRefresh = true
        modifyAndroidExtension()
        initialConfig(project)
    }

    def injectBaseExtension(Project project) {
        project.getExtensions().add("pluginconfig", PluginConfigExtension)
    }

    //生命周期「解析variant之前」,可以修改一些build.gradle原定的配置
    def modifyAndroidExtension() {
        Reflect reflect = Reflect.on(mAppPlugin.variantManager)
        VariantFactory variantFactory = Proxy.newProxyInstance(this.class.classLoader, [VariantFactory.class] as Class[],
                new InvocationHandler() {
                    Object delegate = reflect.get('variantFactory')

                    @Override
                    Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                        if ('preVariantWork' == method.name) {
                            project.gradle.startParameter.taskNames.each {
                                println("project.gradle.startParameter.taskNames.each projectname[${project.name}] [${it}]")
                            }
                            ExtensionApplyUtils.applyUseHostResourceConfig(mProject)
                        }
                        return method.invoke(delegate, args)
                    }
                })
        reflect.set('variantFactory', variantFactory)
    }

    @Override
    protected onBeforePreBuildTask() {
    }

    void initialConfig(Project project) {
        project.afterEvaluate {
            PluginConfigExtension extension = project.pluginconfig
            ProjectDataCenter projectDataCenter = ProjectDataCenter.getInstance(project)
            mProject.android.applicationVariants.each { ApplicationVariantImpl variant ->
                if (variant.name == "release") {
                    projectDataCenter.pluginPackageManifest.packageName = variant.applicationId
                    projectDataCenter.pluginPackageManifest.packagePath = variant.applicationId.replace('.'.charAt(0), File.separatorChar)
                    println("pluginPackageManifest.packageName[${projectDataCenter.pluginPackageManifest.packageName}]")
                    println("pluginPackageManifest.packagePath[${projectDataCenter.pluginPackageManifest.packagePath}]")
                    generateDependencies(variant, projectDataCenter.pluginPackageManifest)
                }
            }
//            projectDataCenter.pluginPackageManifest.aarDependenciesLibs.each {
//                println(" plugin --- packageManifest.aarDependenciesLibs.each [${it.compareKey}]")
//            }
            //host
            Project hostProject = project.getRootProject().getAllprojects().find {
                it != project.getRootProject() && extension.hostPath.contains(it.name)
            }
            hostProject.android.applicationVariants.each { ApplicationVariantImpl variant ->
                if (variant.name == "release") {
                    projectDataCenter.hostPackageManifest.packageName = variant.applicationId
                    projectDataCenter.hostPackageManifest.packagePath = variant.applicationId.replace('.'.charAt(0), File.separatorChar)
                    generateDependencies(variant, projectDataCenter.hostPackageManifest)

                }
            }
//            projectDataCenter.hostPackageManifest.aarDependenciesLibs.each {
//                println(" host --- packageManifest.aarDependenciesLibs.each [${it.compareKey}]")
//            }

            PluginHookerManager manager = new PluginHookerManager(project)
            manager.registerTaskHookers()
        }
    }
}