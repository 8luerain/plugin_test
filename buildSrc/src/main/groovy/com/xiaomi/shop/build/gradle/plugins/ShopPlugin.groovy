package com.xiaomi.shop.build.gradle.plugins

import com.android.build.gradle.AppExtension
import com.android.build.gradle.AppPlugin
import com.android.build.gradle.LibraryExtension
import com.android.build.gradle.internal.api.ApplicationVariantImpl
import com.android.build.gradle.internal.api.LibraryVariantImpl
import com.android.build.gradle.internal.ide.dependencies.ArtifactDependencyGraph
import com.android.build.gradle.internal.ide.dependencies.BuildMappingUtils
import com.android.build.gradle.internal.variant.VariantFactory
import com.android.builder.model.Dependencies
import com.android.builder.model.SyncIssue
import com.google.common.collect.ImmutableMap
import com.xiaomi.shop.build.gradle.plugins.base.ShopBasePlugin
import com.xiaomi.shop.build.gradle.plugins.extension.PluginConfigExtension
import com.xiaomi.shop.build.gradle.plugins.hooker.GenerateLibraryRFileHooker
import com.xiaomi.shop.build.gradle.plugins.hooker.ProcessResourcesHooker
import com.xiaomi.shop.build.gradle.plugins.hooker.TaskHookerManager
import com.xiaomi.shop.build.gradle.plugins.utils.ExtensionApplyUtils
import com.xiaomi.shop.build.gradle.plugins.utils.Log
import com.xiaomi.shop.build.gradle.plugins.utils.Reflect
import org.gradle.api.Project
import org.gradle.internal.reflect.Instantiator
import org.gradle.tooling.provider.model.ToolingModelBuilderRegistry

import javax.inject.Inject
import java.lang.reflect.InvocationHandler
import java.lang.reflect.Method
import java.lang.reflect.Proxy
import java.util.function.Consumer

class ShopPlugin extends ShopBasePlugin {
    Project mProject
    AppPlugin mAppPlugin
    AppExtension mAndroidExtension
    Instantiator mInstantiator

    @Inject
    ShopPlugin(Instantiator instantiator, ToolingModelBuilderRegistry registry) {
        super(instantiator, registry)
        mInstantiator = instantiator
    }

    @Override
    void apply(Project project) {
        mProject = project
        mAppPlugin = project.plugins.findPlugin(AppPlugin)
        mAndroidExtension = project.getExtensions().findByType(AppExtension)
        modifyAndroidExtension()
        initialConfig(project)
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
                            onPreVariantWork()
                        }
                        return method.invoke(delegate, args)
                    }
                })
        reflect.set('variantFactory', variantFactory)

    }

    def onPreVariantWork() {
        Log.i 'mishop', "onPreVariantWork"
        ExtensionApplyUtils.applyUseHostResourceConfig(mProject)
    }

    void initialConfig(Project project) {
        injectBaseExtension(project)

        project.afterEvaluate {
//            VATaskHookerManager manager = new VATaskHookerManager(project, mInstantiator)
//            manager.registerTaskHookers()
        }
    }

    def checkPathForErrors() {

    }

    def injectBaseExtension(Project project) {
        project.getExtensions().add("pluginconfig", PluginConfigExtension)
    }

    private void collectLibraryDependencies(Project libProject) {
        libProject.afterEvaluate {
            def libraryExtension = libProject.extensions.findByType(LibraryExtension.class)
            libraryExtension.libraryVariants.all { LibraryVariantImpl libraryVariant ->
                println("libraryVariants[${libraryVariant}]")
                libraryVariant.preBuild.doFirst {
                    Consumer consumer = new Consumer<SyncIssue>() {
                        @Override
                        void accept(SyncIssue syncIssue) {
                            Log.i 'ShopPlugin collectLibraryDependencies', "Error: ${syncIssue}"
                        }
                    }

                    ImmutableMap<String, String> buildMapping =
                            BuildMappingUtils.computeBuildMapping(project.getGradle())

                    Dependencies dependencies = new ArtifactDependencyGraph()
                            .createDependencies(
                                    libraryVariant.variantData.scope,
                                    false,
                                    buildMapping,
                                    consumer)


                    dependencies.projects.each {
                        Log.i("ShopPlugin dependence project ", it)
                    }

                    dependencies.libraries.each {
                        Log.i("ShopPlugin dependence libraries ", it.name)
                    }

                    dependencies.javaLibraries.each {
                        Log.i("ShopPlugin dependence javaLibraries ", it.name)
                    }

                    // add mishop2lib manually
                    pluginDependencyManager.addAndroidLibraryDependence("artifacts::mishop2lib:unspecified@jar")
                }
            }
        }
    }

    private void performMyTask(Project project) {
        println("print form plugin test")
        project.rootProject.allprojects {
            Project subProject ->
                println("subproject name -->" + subProject.name)
        }
    }


    static class VATaskHookerManager extends TaskHookerManager {
        Instantiator instantiator

        VATaskHookerManager(Project project, Instantiator instantiator) {
            super(project, instantiator)
            this.instantiator = instantiator
        }

        @Override
        void registerTaskHookers() {
            android.applicationVariants.all { ApplicationVariantImpl appVariant ->
                if (!appVariant.buildType.name.equalsIgnoreCase("release")) {
                    return
                }
                println("susscee registerTaskHookers")
                registerTaskHooker(instantiator.newInstance(ProcessResourcesHooker, project, appVariant))
                registerTaskHooker(instantiator.newInstance(GenerateLibraryRFileHooker, project, appVariant))
//                registerTaskHooker(instantiator.newInstance(PrepareDependenciesHooker, project, appVariant))
//                registerTaskHooker(instantiator.newInstance(MergeAssetsHooker, project, appVariant))
//                registerTaskHooker(instantiator.newInstance(MergeManifestsHooker, project, appVariant))
//                registerTaskHooker(instantiator.newInstance(MergeJniLibsHooker, project, appVariant))
//                registerTaskHooker(instantiator.newInstance(ShrinkResourcesHooker, project, appVariant))
//                registerTaskHooker(instantiator.newInstance(ProguardHooker, project, appVariant))
//                registerTaskHooker(instantiator.newInstance(DxTaskHooker, project, appVariant))
            }
        }
    }

}