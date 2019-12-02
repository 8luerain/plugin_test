package com.xiaomi.shop.build.gradle.plugins.hooker

import com.android.build.gradle.AndroidConfig
import com.android.build.gradle.AppExtension
import com.android.build.gradle.api.ApkVariant
import com.android.build.gradle.internal.res.LinkApplicationAndroidResourcesTask
import com.xiaomi.shop.build.gradle.plugins.ShopPlugin
import com.xiaomi.shop.build.gradle.plugins.bean.PackageManifest
import com.xiaomi.shop.build.gradle.plugins.utils.ProjectDataCenter
import com.xiaomi.shop.build.gradle.plugins.utils.aaptedit.ArscEditor
import org.gradle.api.Project

class ProcessResourcesHooker extends GradleTaskHooker<LinkApplicationAndroidResourcesTask> {
    public static final String RESOURCES_ARSC = 'resources.arsc'

    AndroidConfig androidConfig
    File stable_id_lib_file
    PackageManifest mPluginManifest


    ProcessResourcesHooker(Project project, ApkVariant apkVariant) {
        super(project, apkVariant)
        androidConfig = project.extensions.findByType(AppExtension)
        mPluginManifest = ProjectDataCenter.getInstance(project).pluginPackageManifest

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
        mPluginManifest.originalResourceFile = task.textSymbolOutputFile
        mPluginManifest.resourceOutputFileDir = task.resPackageOutputFolder
        mPluginManifest.sourceOutputFileDir = task.sourceOutputDir

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
        File apFile = new File([task.resPackageOutputFolder, "resources-${scope.fullVariantName}.ap_"].join(File.separator))
        def aaptResourceDir = project.plugins.findPlugin(ShopPlugin).aaptResourceDir
        //1:解压ap文件，拷贝目录，准备修改
        unzip(apFile, aaptResourceDir)
        //2：删除res资源文件
        removeSameResourceFile(aaptResourceDir)

        //3：处理resource.arsc中value资源，并且删除已经过滤的资源对应条目
        removeItemFromArscFile(aaptResourceDir)
        //4:处理xml文件，对资源文件的引用

        //5：处理src文件中中间生产的R文件, 保证后面compileJava时的正确性
    }

    private void unzip(File ap_fle, File aaptResourceDir) {
        if (ap_fle.exists()) {
            project.copy {
                from project.zipTree(ap_fle)
                into aaptResourceDir

                include 'AndroidManifest.xml'
                include 'resources.arsc'
                include 'res/**/*'
            }
        }
    }

    void removeSameResourceFile(File aaptResourceDir) {
        def typeList = ProjectDataCenter.getInstance(project).mergedPluginPackageManifest.resourcesMap.keySet()
        typeList.each {
            println("typelist type[${it}]")
        }
        ArrayList<String> recordFilteredResPath = [] as ArrayList
        def resDir = new File(aaptResourceDir, 'res')
        resDir.listFiles().each { typeDir ->
            def type = typeList.find { typeDir.name == it || typeDir.name.startsWith("${it}-") }
            if (type == null) {
                typeDir.listFiles().each {
                    recordFilteredResPath.add("res/$typeDir.name/$it.name")
                }
                typeDir.deleteDir()
                return
            }
            def entryFiles = typeDir.listFiles()
            def retainedEntryCount = entryFiles.size()
            def resListOfType = ProjectDataCenter.getInstance(project).mergedPluginPackageManifest.resourcesMap.get(type)
            entryFiles.each { entryFile ->
                def entry = resListOfType.find { entryFile.name.startsWith("${it.resourceName}.") }
                if (entry == null) {
                    recordFilteredResPath.add("res/$typeDir.name/$entryFile.name")
                    entryFile.delete()
                    retainedEntryCount--
                }
            }

            if (retainedEntryCount == 0) {
                typeDir.deleteDir()
            }
        }
        recordFilteredResPath.each {
            println("recordFilteredResPath -- ${it}")
        }
    }

    def removeItemFromArscFile(File aaptResourceDir) {
        def libRefTable = ["${virtualApk.packageId}": par.applicationId]
        final File arscFile = new File(this.assetDir, RESOURCES_ARSC)
        final def arscEditor = new ArscEditor(arscFile, androidConfig.buildToolsVersion)
        arscEditor.slice(pp, idMaps, libRefTable, retainedTypes)
    }
}