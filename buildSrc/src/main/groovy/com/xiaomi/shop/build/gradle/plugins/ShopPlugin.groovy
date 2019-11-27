package com.xiaomi.shop.build.gradle.plugins


import com.android.build.gradle.LibraryExtension
import com.android.build.gradle.internal.api.ApplicationVariantImpl
import com.android.build.gradle.internal.api.LibraryVariantImpl
import com.android.build.gradle.internal.ide.dependencies.ArtifactDependencyGraph
import com.android.build.gradle.internal.ide.dependencies.BuildMappingUtils
import com.android.builder.model.Dependencies
import com.android.builder.model.SyncIssue
import com.google.common.collect.ImmutableMap
import com.xiaomi.shop.build.gradle.plugins.hooker.GenerateLibraryRFileHooker
import com.xiaomi.shop.build.gradle.plugins.hooker.ProcessResourcesHooker
import com.xiaomi.shop.build.gradle.plugins.hooker.TaskHookerManager
import com.xiaomi.shop.build.gradle.plugins.utils.Log
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.internal.reflect.Instantiator
import org.gradle.tooling.provider.model.ToolingModelBuilderRegistry

import javax.inject.Inject
import java.util.function.Consumer

class ShopPlugin implements Plugin<Project> {
    Instantiator mInstantiator

    @Inject
    public ShopPlugin(Instantiator instantiator, ToolingModelBuilderRegistry registry) {
        this.mInstantiator = instantiator
    }

    @Override
    void apply(Project project) {
        initialConfig(project)
    }


    void initialConfig(Project project) {
        injectBaseExtension(project)
        checkPathForErrors()
        project.afterEvaluate {
            VATaskHookerManager manager = new VATaskHookerManager(project, mInstantiator)
            manager.registerTaskHookers()
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

        VATaskHookerManager(Project project, Instantiator instantiator) {
            super(project, instantiator)
        }

        @Override
        void registerTaskHookers() {
            android.applicationVariants.all { ApplicationVariantImpl appVariant ->
                if (!appVariant.buildType.name.equalsIgnoreCase("release")) {
                    return
                }
                println("susscee registerTaskHookers")
                registerTaskHooker(mInstantiator.newInstance(ProcessResourcesHooker, project, appVariant))
                registerTaskHooker(mInstantiator.newInstance(GenerateLibraryRFileHooker, project, appVariant))
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