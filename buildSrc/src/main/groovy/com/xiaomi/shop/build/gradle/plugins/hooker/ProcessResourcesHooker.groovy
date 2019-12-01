package com.xiaomi.shop.build.gradle.plugins.hooker

import com.android.build.gradle.AndroidConfig
import com.android.build.gradle.AppExtension
import com.android.build.gradle.api.ApkVariant
import com.android.build.gradle.internal.res.LinkApplicationAndroidResourcesTask
import com.xiaomi.shop.build.gradle.plugins.ShopPlugin
import com.xiaomi.shop.build.gradle.plugins.utils.ProjectDataCenter
import org.gradle.api.Project

class ProcessResourcesHooker extends GradleTaskHooker<LinkApplicationAndroidResourcesTask> {

    AndroidConfig androidConfig
    File stable_id_lib_file
    ProjectDataCenter mProjectDataCenter

    ProcessResourcesHooker(Project project, ApkVariant apkVariant) {
        super(project, apkVariant)
        androidConfig = project.extensions.findByType(AppExtension)
        mProjectDataCenter = ProjectDataCenter.getInstance(project)

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


    @Override
    void afterTaskExecute(LinkApplicationAndroidResourcesTask task) {
//        ProjectDataCenter.getInstance(project).pluginPackageManifest.resourceOutputFileDir = task
//        variantData.outputScope.getOutputs(TaskOutputHolder.TaskOutputType.PROCESSED_RES).each {
//            repackage(par, it.outputFile)
//        }

        mProjectDataCenter.pluginPackageManifest.originalResourceFile = task.textSymbolOutputFile
        mProjectDataCenter.pluginPackageManifest.resourceOutputFileDir = task.resPackageOutputFolder
        mProjectDataCenter.pluginPackageManifest.sourceOutputFileDir = task.sourceOutputDir

//        variantData.getOutputScope().getApkDatas().each { ApkData data ->
//            data.outputs.each { OutputFile outputFile ->
//                println("output file name[${outputFile.getOutputFile().getName()}]")
//            }
//        }
//        task.outputs.getPreviousOutputFiles().each {
//            println("getPreviousOutputFiles[${it.name}]")
//        }
//        ExistingBuildElements
        println("scope dirname [${scope.fullVariantName}]")
        println("res dir [${task.sourceOutputDir}]  source output dir[${task.resPackageOutputFolder}]")
        handleResource(task)
    }

    void handleResource(LinkApplicationAndroidResourcesTask task) {
        //1:解压ap文件，拷贝目录，准备修改
        File AP_File = new File([task.resPackageOutputFolder , "resources-${scope.fullVariantName}.ap_"].join(File.separator))
        def basePlugin = project.plugins.findPlugin(ShopPlugin)
        if (AP_File.exists()) {
            project.copy {
                from project.zipTree(AP_File)
                into basePlugin.aaptResourceDir

                include 'AndroidManifest.xml'
                include 'resources.arsc'
                include 'res/**/*'
            }
        }
        //2：删除res资源文件

        //3：处理resource.arsc中value资源，并且删除已经过滤的资源对应条目

        //4:处理xml文件，对资源文件的引用

        //5：处理src文件中中间生产的R文件, 保证后面compileJava时的正确性
    }
}