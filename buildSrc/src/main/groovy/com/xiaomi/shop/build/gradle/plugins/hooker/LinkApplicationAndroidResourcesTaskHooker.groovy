package com.xiaomi.shop.build.gradle.plugins.hooker

import com.android.build.gradle.AndroidConfig
import com.android.build.gradle.AppExtension
import com.android.build.gradle.api.ApkVariant
import com.android.build.gradle.internal.res.LinkApplicationAndroidResourcesTask
import com.android.sdklib.BuildToolInfo
import com.google.common.io.Files
import com.xiaomi.shop.build.gradle.plugins.bean.MergedPackageManifest
import com.xiaomi.shop.build.gradle.plugins.bean.PackageManifest
import com.xiaomi.shop.build.gradle.plugins.utils.ProjectDataCenter
import com.xiaomi.shop.build.gradle.plugins.utils.ZipFileUtil
import com.xiaomi.shop.build.gradle.plugins.utils.aaptedit.AXmlEditor
import com.xiaomi.shop.build.gradle.plugins.utils.aaptedit.ArscEditor
import groovy.io.FileType
import org.gradle.api.Project

//android gradle plugin 更新后，使用新的LinkApplicationAndroidResourcesTask

class LinkApplicationAndroidResourcesTaskHooker extends GradleTaskHooker<LinkApplicationAndroidResourcesTask> {

    AndroidConfig androidConfig
    PackageManifest mPluginManifest


    LinkApplicationAndroidResourcesTaskHooker(Project project, ApkVariant apkVariant) {
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
        aaptTask.outputs.getFiles().each {

        }
    }

    @Override
    void afterTaskExecute(LinkApplicationAndroidResourcesTask task) {
        initPluginManifest(task)
        handleResource(task)
    }

    private void initPluginManifest(LinkApplicationAndroidResourcesTask task) {
        mPluginManifest.originalResourceTxtFile = task.textSymbolOutputFile
        mPluginManifest.resourceOutputFileDir = task.resPackageOutputFolder
        mPluginManifest.sourceOutputFileDir = task.sourceOutputDir
    }

    void handleResource(LinkApplicationAndroidResourcesTask task) {
        File apFile = new File([task.resPackageOutputFolder, "resources-${scope.fullVariantName}.ap_"].join(File.separator))
        File aaptResourceDir = project.ext.aaptResourceDir
        File aaptSourceDir = project.ext.aaptSourceDir
        if (aaptResourceDir.exists()) {
            println("aaptResourceDir.deleteDir() [${aaptResourceDir.deleteDir()}]")
//            aaptResourceDir.deleteDir()
        }
        if (aaptSourceDir.exists()) {
            println("aaptSourceDir.deleteDir() [${aaptSourceDir.deleteDir()}]")
//            aaptResourceDir.deleteDir()
        }

        def removedFileList = [] as HashSet<String> //记录需要删除的文件
        def modifyFileList = [] as HashSet<String> //记录修改过的文件，用于更换原始ap-file中的文件
        //1:解压ap文件，拷贝目录，准备修改
        unzip(apFile, aaptResourceDir)
        //2：删除res资源文件
        removeSameResourceFile(aaptResourceDir, removedFileList)

        reGenerateRText()

        //3：处理resource.arsc中value资源，并且删除已经过滤的资源对应条目
        modifyItemOfArscFile(aaptResourceDir, task ,modifyFileList)

        //4:处理xml文件，对资源文件的引用
        modifyItemOfXmlFile(aaptResourceDir, modifyFileList)

        //5：处理src文件中中间生产的R文件, 保证后面compileJava时的正确性
        reGenerateRJava()

        //6：更新zip文件
        reProcessResource(apFile, removedFileList, modifyFileList, aaptResourceDir, task)
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

    private void removeSameResourceFile(File aaptResourceDir, Set<String> modifyFileList) {
        def typeList = ProjectDataCenter.getInstance(project).mergedPluginPackageManifest.resourcesMap.keySet()
        def resDir = new File(aaptResourceDir, 'res')
        resDir.listFiles().each { typeDir ->
            def type = typeList.find { typeDir.name == it || typeDir.name.startsWith("${it}-") }
            if (type == null) {
                typeDir.listFiles().each {
                    modifyFileList.add("res/$typeDir.name/$it.name")
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
                    modifyFileList.add("res/$typeDir.name/$entryFile.name")
                    entryFile.delete()
                    retainedEntryCount--
                }
            }

            if (retainedEntryCount == 0) {
                typeDir.deleteDir()
            }
        }
    }

    private void modifyItemOfArscFile(File aaptResourceDir, LinkApplicationAndroidResourcesTask task , Set<String> modifyFileList) {
        MergedPackageManifest mergeManifest = ProjectDataCenter.getInstance(project).mergedPluginPackageManifest
        def libRefTable = ["${mergeManifest.packageId}": task.applicationId]
        println("libRefTable [${libRefTable}]")
        final File arscFile = new File(aaptResourceDir, 'resources.arsc')
        modifyFileList.add("resources.arsc")
        final def arscEditor = new ArscEditor(arscFile, androidConfig.buildToolsRevision)
        arscEditor.slice(mergeManifest.packageId, mergeManifest.resIdMapForArsc, libRefTable,
                mergeManifest.resourcesMapForAapt)
    }



    private void modifyItemOfXmlFile(File aaptResourceDir, Set<String> modifyFileList) {
        final String unixFileFileSeparator = "/"
        MergedPackageManifest manifest = ProjectDataCenter.getInstance(project).mergedPluginPackageManifest
        int len = aaptResourceDir.canonicalPath.length() + 1
        def isWindows = (File.separator != unixFileFileSeparator) //unix 文件路径分割符为'/'  window路径分隔符为'\'

        aaptResourceDir.eachFileRecurse(FileType.FILES) { file ->
            if ('xml'.equalsIgnoreCase(Files.getFileExtension(file.name))) {
                new AXmlEditor(file).setPackageId(manifest.packageId, manifest.resIdMapForArsc)

                if (modifyFileList != null) {
                    def path = file.canonicalPath.substring(len)
                    if (isWindows) {
                        path = path.replaceAll('\\\\', unixFileFileSeparator)
                    }
                    modifyFileList.add(path)
                }
            }
        }
    }


    private void reGenerateRText() {
        MergedPackageManifest mergeManifest = ProjectDataCenter.getInstance(project).mergedPluginPackageManifest
        File rFile = mPluginManifest.originalResourceTxtFile
        rFile.write('')
        rFile.withPrintWriter { pw ->
            mergeManifest.resourcesMapForAapt.each { t ->
                t.entries.each { e ->
                    pw.println("${t.type} ${t.name} ${e.name} ${e._vs}")
                }
            }
            mergeManifest.styleablesListForAapt.each {
                pw.println("${it.vtype} ${it.type} ${it.key} ${it.idStr}")
            }
        }
    }

    private void reGenerateRJava() {
        File sourceDir = project.ext.aaptSourceDir;
        if (sourceDir.exists()) {
            sourceDir.deleteDir()
        }
        ProjectDataCenter.getInstance(project).pluginPackageManifest.getRJavaFile()
        ProjectDataCenter.getInstance(project).mergedPluginPackageManifest.generateAarLibRJava2Dir()
        project.copy {
            from project.ext.aaptSourceDir
            into  mPluginManifest.sourceOutputFileDir
        }
    }

    private void reProcessResource(File ap_org, Set<String> removedFileList, Set<String> modifyFileList
                                   , File aaptResourceDir, LinkApplicationAndroidResourcesTask task) {
        //-------------方法1 ，需要原包，实现更新apFile-------------

        //删除原来zip包下变更过的文件
        ZipFileUtil.deleteAll(ap_org, removedFileList + modifyFileList)
        //将更新过的文件插入到zip包里
        project.exec {
            executable task.buildTools.getPath(BuildToolInfo.PathId.AAPT)
            workingDir aaptResourceDir
            args 'add', ap_org.path
            args modifyFileList
            standardOutput = System.out
            errorOutput = System.err
        }

        //-------------方法2 ，使用aapt2 ，生成新的apFile-------------
    }

}