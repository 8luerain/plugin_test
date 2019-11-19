package com.xiaomi.shop.build.gradle.plugins


import com.android.build.gradle.LibraryExtension
import com.android.build.gradle.internal.api.ApplicationVariantImpl
import com.android.build.gradle.internal.api.LibraryVariantImpl
import com.android.build.gradle.internal.ide.dependencies.ArtifactDependencyGraph
import com.android.build.gradle.internal.ide.dependencies.BuildMappingUtils
import com.android.builder.model.Dependencies
import com.android.builder.model.SyncIssue
import com.google.common.collect.ImmutableMap
import com.xiaomi.shop.build.gradle.plugins.hooker.ProcessResourcesHooker
import com.xiaomi.shop.build.gradle.plugins.hooker.TaskHookerManager
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.execution.TaskExecutionListener
import org.gradle.api.tasks.TaskState
import org.gradle.internal.reflect.Instantiator
import org.gradle.tooling.provider.model.ToolingModelBuilderRegistry

import javax.inject.Inject
import java.util.function.Consumer

class ShopPlugin implements Plugin<Project> {
    Instantiator instantiator

    @Inject
    public ShopPlugin(Instantiator instantiator, ToolingModelBuilderRegistry registry) {
        this.instantiator = instantiator
    }

    @Override
    void apply(Project project) {
//        def android = project.extensions.findByType(AppExtension.class)
//        android.applicationVariants.all {
//            ApplicationVariantImpl applicationVariant ->
//                if (applicationVariant.name == "release") {
//                    println("applicationVariantName[" + applicationVariant.name + "]")
//                    println("applicationVariantData[" + applicationVariant.variantData + "]")
//                    println("applicationVariantScope[" + applicationVariant.variantData.scope + "]")
//                    def mergeResourcesTask = applicationVariant.mergeResources
//                    mergeResourcesTask.doFirst {
//                        mergeResourcesTask.inputs.files.each {
//                            println("input file [${it.absolutePath}]")
//                        }
//                    }
//                }
//        }

//        project.rootProject.allprojects {
//            if (it.name == "plugina") {
//                Project pluginaProject = it
//                def android = pluginaProject.getExtensions().findByType(AppExtension)
//                android.applicationVariants.all {
//                    ApplicationVariantImpl applicationVariant ->
//                        def mergeResourcesTask = applicationVariant.variantData.scope.taskContainer.processAndroidResTask.get()
//                        mergeResourcesTask.doFirst {
//                            mergeResourcesTask.inputs.files.each {
//                                println("input file [${it.absolutePath}]")
//                            }
//                        }
//                        mergeResourcesTask.doLast {
//                            applicationVariant.variantData.outputScope.
//                                    getOutputs(TaskOutputHolder.TaskOutputType.PROCESSED_RES).each {
//                                println("out file [${it.outputFile}]")
//                            }
////                            mergeResourcesTask.outputs..each {
////                                println("out file [${it.absolutePath}]")
////                            }
//                        }
//                }
////                        project.tasks.each {
////                            it.doFirst {
////                                it.inputs.files.each {
////                                    println("input file [${it.absolutePath}]")
////                                }
////                            }
////                            it.doLast {
////                                it.outputs.files.each {
////                                    println("out file [${it.absolutePath}]")
////                                }
////                            }
////                        }
////                }
//            }
//            if (it.name == "baselib") {
//                Project baselibProject = it
//                def android = baselibProject.getExtensions().findByType(LibraryExtension.class)
//                android.libraryVariants.all {
//                    LibraryVariantImpl applicationVariant ->
//                        if (applicationVariant.name == "release") {
//                            println("applicationVariantName[" + applicationVariant.name + "]")
//                            println("applicationVariantData[" + applicationVariant.variantData + "]")
//                            println("applicationVariantScope[" + applicationVariant.variantData.scope + "]")
//
//                            def prebuildTask = applicationVariant.preBuild
//                            prebuildTask.doFirst {
//                                println("taskname[prebuild]")
//                                Consumer consumer = new Consumer<SyncIssue>() {
//                                    @Override
//                                    void accept(SyncIssue syncIssue) {
//                                        Log.i 'ShopPlugin collectLibraryDependencies', "Error: ${syncIssue}"
//                                    }
//                                }
//
//                                ImmutableMap<String, String> buildMapping =
//                                        BuildMappingUtils.computeBuildMapping(project.getGradle())
//
//                                Dependencies dependencies = new ArtifactDependencyGraph()
//                                        .createDependencies(
//                                                applicationVariant.variantData.scope,
//                                                false,
//                                                buildMapping,
//                                                consumer)
//
//
//                                dependencies.projects.each {
//                                    Log.i("ShopPlugin dependence project ", it)
//                                }
//
//                                dependencies.libraries.each {
//                                    Log.i("ShopPlugin dependence libraries ", it.name)
//                                }
//
//                                dependencies.javaLibraries.each {
//                                    Log.i("ShopPlugin dependence javaLibraries ", it.name)
//                                }
//                            }
//                        }
//                }
//            }
//        }
//        testMethod(project)
        Task my_plugin_task = project.task("my_plugin_test").doFirst {
//            performMyTask(project)
        }
        my_plugin_task.setGroup("hello gradle")
        VATaskHookerManager manager = new VATaskHookerManager(project, instantiator)
        manager.registerTaskHookers()
//        project.afterEvaluate {
//           AppExtension extension =  project.getExtensions().getByName(AppExtension)
//            extension.applicationVariants.all {
//                ApplicationVariantImpl variant ->
//                    println("variant name[" + variant.name + "]")
//            }
//            project.getTasks().all {
//                Task task ->
//                    if (project.getName() == 'plugina') {
//                        println("task name[" + task.name + "] ---- task imp [${task.getClass().toString()}]")
//                    }
//            }
    }

//        project.allprojects {
//            Project per ->
//                if (per.name == "plugina") {
//                    AppExtension extension = project.getExtensions().findByType(AppExtension.class)
//                    extension.applicationVariants.all {
//                        ApplicationVariantImpl variant ->
//                            println("variant data ${variant.name}")
//                            variant.variantData.taskContainer.getMergeResourcesTask().{
//                                    println("mergeResourcesTask---name-- ${task.name}")
//                            }
//                            variant.variantData.getTaskContainer().processAndroidResTask{
//                                Task task ->
//                                    println("task---name-- ${task.name}")
//                            }
//                    }
//                }
//        }
//    }

    private void testMethod(Project project) {
        project.gradle.addListener(new TaskTestListener())
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


    static class TaskTestListener implements TaskExecutionListener {
        @Override
        void beforeExecute(Task task) {
            println("beforeExecute[" + task.name + "]")
        }

        @Override
        void afterExecute(Task task, TaskState taskState) {
            println("afterExecute[" + task.name + "]")
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
                registerTaskHooker(instantiator.newInstance(ProcessResourcesHooker, project, appVariant))
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