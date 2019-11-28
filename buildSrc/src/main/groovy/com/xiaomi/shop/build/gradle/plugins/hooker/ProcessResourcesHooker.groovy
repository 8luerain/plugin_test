package com.xiaomi.shop.build.gradle.plugins.hooker

import com.android.build.gradle.AndroidConfig
import com.android.build.gradle.AppExtension
import com.android.build.gradle.api.ApkVariant
import com.android.build.gradle.internal.res.LinkApplicationAndroidResourcesTask
import com.xiaomi.shop.build.gradle.plugins.utils.ProjectDataCenter
import org.gradle.api.Project

class ProcessResourcesHooker extends GradleTaskHooker<LinkApplicationAndroidResourcesTask> {

    /**
     * Collector to gather the sources and styleables
     */
    /**
     * Android config information specified in build.gradle
     */
    AndroidConfig androidConfig
    File stable_id_lib_file

    ProcessResourcesHooker(Project project, ApkVariant apkVariant) {
        super(project, apkVariant)
        androidConfig = project.extensions.findByType(AppExtension)


    }

    @Override
    String getTaskName() {
        return scope.getTaskName('process', 'Resources')
    }

    @Override
    void beforeTaskExecute(LinkApplicationAndroidResourcesTask aaptTask) {
        println("hahaha projectname[$project.name] , taskname[${aaptTask.name}], taskclass[${aaptTask.class.name}]")
//        Project libProject = project.rootProject.findProject("baselib")
//        if (libProject) {
//            stable_id_lib_file = libProject.file("stable_id_file.txt")
//            if (!stable_id_lib_file.exists()) {
//                Log.i "ProcessResourcesHooker", "${stable_id_lib_file} not exist , generate it."
//                stable_id_lib_file.createNewFile()
//            } else {
//
//            }
//        }
//        AppExtension extension = project.getExtensions().findByType(AppExtension.class)
//        extension.aaptOptions.additionalParameters("--emit-ids", "${stable_id_lib_file.absolutePath}")
//        println("is aatp2 enable[${aaptTask.aaptOptions}]")
//        aaptTask.getAaptOptionsInput().additionalParameters("--emit-ids", "${stable_id_lib_file.absolutePath}")
    }
    /**
     * Since we need to remove the host resources and modify the resource ID,
     * we will reedit the AP_ file and repackage it after the task execute
     *
     * @param par Gradle task of process android resources
     */
    @Override
    void afterTaskExecute(LinkApplicationAndroidResourcesTask task) {
//        ProjectDataCenter.getInstance(project).pluginPackageManifest.resourceOutputFileDir = task
//        variantData.outputScope.getOutputs(TaskOutputHolder.TaskOutputType.PROCESSED_RES).each {
//            repackage(par, it.outputFile)
//        }
        ProjectDataCenter.getInstance(project).pluginPackageManifest.resourceOutputFileDir = task.resPackageOutputFolder;
        ProjectDataCenter.getInstance(project).pluginPackageManifest.originalResourceFile = task.textSymbolOutputFile;


        println("res dir [${task.sourceOutputDir}]  source output dir[${task.resPackageOutputFolder}]")
    }


    /**
     * Parse the type part of a android resource id
     */
    def parseTypeIdFromResId(int resourceId) {
        resourceId >> 16 & 0xFF
    }

    /**
     * Parse the entry part of a android resource id
     */
    def parseEntryIdFromResId(int resourceId) {
        resourceId & 0xFFFF
    }

}