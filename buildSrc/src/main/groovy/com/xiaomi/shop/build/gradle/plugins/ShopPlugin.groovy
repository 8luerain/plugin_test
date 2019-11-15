package com.xiaomi.shop.build.gradle.plugins

import com.android.build.gradle.AppExtension
import com.android.build.gradle.internal.api.ApplicationVariantImpl
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.execution.TaskExecutionListener
import org.gradle.api.tasks.TaskState

class ShopPlugin implements Plugin<Project> {
    @Override
    void apply(Project project) {
        def android = project.extensions.findByType(AppExtension.class)
        android.applicationVariants.all {
            ApplicationVariantImpl applicationVariant ->
                println("applicationVariantName["+applicationVariant.name+"]")
                println("applicationVariantData["+applicationVariant.variantData+"]")
                println("applicationVariantScope["+applicationVariant.variantData.scope+"]")
                applicationVariant.variantData.scope.getTaskContainer()

        }
//        testMethod(project)
        project.task("my_plugin_test").doFirst {
//            performMyTask(project)
        }
//        project.afterEvaluate {
//            project.getTasks().forEach {
//                println("task name[" + it.name + "]")
//            }
//        }

    }

    private void testMethod(Project project) {
        project.gradle.addListener(new TaskTestListener())

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
            println("beforeExecute["+task.name+"]")
        }

        @Override
        void afterExecute(Task task, TaskState taskState) {
            println("afterExecute["+task.name+"]")
        }
    }
}