package com.xiaomi.shop.build.gradle.plugins.base

import com.android.build.gradle.AppExtension
import com.android.build.gradle.api.ApplicationVariant
import com.android.build.gradle.internal.ide.dependencies.BuildMappingUtils
import com.android.builder.model.AndroidLibrary
import com.android.builder.model.Dependencies
import com.android.builder.model.JavaLibrary
import com.android.builder.model.SyncIssue
import com.google.common.collect.ImmutableMap
import com.xiaomi.shop.build.gradle.plugins.bean.PackageManifest
import com.xiaomi.shop.build.gradle.plugins.bean.dependence.AarDependenceInfo
import com.xiaomi.shop.build.gradle.plugins.bean.dependence.JarDependenceInfo
import com.xiaomi.shop.build.gradle.plugins.utils.FileUtil
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task

import java.util.function.Consumer

class BaseGradlePlugin implements Plugin<Project> {

    Project mProject
    AppExtension mAndroidExtension
    ApplicationVariant mAppReleaseVariant

    public File mHookerDir
    public File aaptResourceDir
    public File aaptSourceDir
    public File outputDir
    public File mDependenciesFile

    @Override
    void apply(Project project) {
        if (!project.plugins.hasPlugin('com.android.application')) {
            throw new Exception("需要在application中使用")
        }
        mProject = project
        mHookerDir = new File(project.getProjectDir(), "hooker")
        mDependenciesFile = new File(mHookerDir, "dependencies.txt")
        project.afterEvaluate {
            mAndroidExtension = project.extensions.findByType(AppExtension)
            mAppReleaseVariant = mAndroidExtension.applicationVariants.find {
                it.name == "release"// 暂时不支持flavor
            }
            //清除hooker中间文件
            project.tasks.findByName("clean").doLast {
                if (mHookerDir.exists()) {
                    mHookerDir.deleteDir()
                }
            }
            //生成dependencies文件
            Task preBuild = project.tasks.findByName("pre${mAppReleaseVariant.name.capitalize()}Build")
            preBuild.outputs.upToDateWhen {
                false
            }
            preBuild.doFirst {
                createHookerDir()
                onBeforePreBuildTask()
            }
        }
    }

    protected onBeforePreBuildTask() {

    }


    /**
     * 备份R.txt文件
     * @param variant
     * @return
     */
    def backupOriginalRFile() {
        //生成resource文件
        project.tasks["process${mAppReleaseVariant.name.capitalize()}Resources"].doLast { task ->
            mProject.copy {
                from task.textSymbolOutputFile
                into mHookerDir
                rename { "original_resource_file.txt" }
            }
        }
    }

    def createHookerDir() {
        if (!mHookerDir.exists()) {
            mHookerDir.mkdir()
        }
        project.ext.hookerDir = mHookerDir
    }

    def createAaptWorkspace() {
        println("method createWorkDir")
        //创建中间缓存文件
        createHookerDir()
        //存放资源
        aaptResourceDir = new File([mHookerDir, "intermediates", "resource"].join(File.separator))
        if (!aaptResourceDir.parentFile.exists()) {
            aaptResourceDir.parentFile.mkdirs()
        }
        if (!aaptResourceDir.exists()) {
            aaptResourceDir.mkdir()
        }
        //存放源码
        aaptSourceDir = new File([mHookerDir, "intermediates", "source", "r"].join(File.separator))
        if (!aaptSourceDir.parentFile.exists()) {
            aaptSourceDir.parentFile.mkdirs()
        }
        if (!aaptSourceDir.exists()) {
            aaptSourceDir.mkdir()
        }
        outputDir = new File([mHookerDir, "outputs"].join(File.separator))
        if (!outputDir.parentFile.exists()) {
            outputDir.parentFile.mkdirs()
        }
        if (!outputDir.exists()) {
            outputDir.mkdir()
        }
        mProject.ext.aaptResourceDir = aaptResourceDir
        mProject.ext.aaptSourceDir = aaptSourceDir
        mProject.ext.outputDir = outputDir
    }

    def loadDependencies(PackageManifest packageManifest) {
        def collectAction = {
            List<String> dependenciesList = new ArrayList<String>()
            Consumer consumer = new Consumer<SyncIssue>() {
                @Override
                void accept(SyncIssue syncIssue) {
                }
            }
            ImmutableMap<String, String> buildMapping =
                    BuildMappingUtils.computeBuildMapping(mProject.getGradle())

            Dependencies dependencies = ApiVersionFactory.getInstance()
                    .getArtifactDependencyGraph(mAppReleaseVariant.variantData.scope,
                            false, buildMapping, consumer)


            dependencies.getLibraries().each { AndroidLibrary androidLibrary ->
                def androidCoordinates = androidLibrary.resolvedCoordinates
                dependenciesList.add(androidLibrary.name)
                if (null != packageManifest) {
                    packageManifest.aarDependenciesLibs.add(
                            new AarDependenceInfo(
                                    androidCoordinates.groupId,
                                    androidCoordinates.artifactId,
                                    androidCoordinates.version,
                                    androidLibrary))
                }

            }
            dependencies.getJavaLibraries().each { JavaLibrary library ->
//                println(" dependencies.getLibraries()[${library.name}]")
                dependenciesList.add(library.name)
                def jarCoordinates = library.resolvedCoordinates
                if (null != packageManifest) {
                    packageManifest.jarDependenciesLibs.add(
                            new JarDependenceInfo(
                                    jarCoordinates.groupId,
                                    jarCoordinates.artifactId,
                                    jarCoordinates.version,
                                    library))
                }

            }
            dependencies.getProjects().each { String path ->
//                println(" dependencies.getProjects[${path}]")
                dependenciesList.add(path)

            }
            Collections.sort(dependenciesList)

            return dependenciesList
        }
        if (mDependenciesFile.exists()) {
            collectAction()
        } else {
            FileUtil.saveFile(mHookerDir, "dependencies", collectAction)
        }
    }

    Project getProject() {
        return mProject
    }

    File getHookerDir() {
        return mHookerDir
    }

    File getAaptResourceDir() {
        return aaptResourceDir
    }

}
