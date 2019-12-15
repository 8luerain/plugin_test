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
import com.xiaomi.shop.build.gradle.plugins.utils.CommonFactory
import com.xiaomi.shop.build.gradle.plugins.utils.FileUtil
import org.gradle.api.Plugin
import org.gradle.api.Project

import java.util.function.Consumer

class ShopBasePlugin implements Plugin<Project> {

    Project mProject
    public File mHookerDir
    public File aaptResourceDir
    public File aaptSourceDir

    @Override
    void apply(Project project) {
        if (!project.plugins.hasPlugin('com.android.application')) {
            throw new Exception("需要在application中使用")
        }
        mProject = project
        mHookerDir = new File(project.getProjectDir(), "hooker")
        if (!mHookerDir.exists()) {
            mHookerDir.mkdir()
        }
        project.ext.hookerDir = mHookerDir
        project.afterEvaluate {
            AppExtension androidExt = project.extensions.findByType(AppExtension)
            androidExt.applicationVariants.each { variant ->
                if (variant.buildType.name != "release") {
                    return
                }
                //清除hooker中间文件
                project.tasks.findByName("clean").doLast {
                    if (mHookerDir.exists()) {
                        mHookerDir.deleteDir()
                    }
                }
                //生成dependencies文件
                project.tasks.findByName("pre${variant.name.capitalize()}Build").doLast {
                    generateDependencies(variant, null)
                    onBeforePreBuildTask()
                }
                //生成resource文件
                project.tasks["process${variant.name.capitalize()}Resources"].doLast { task ->
                    mProject.copy {
                        from task.textSymbolOutputFile
                        into mHookerDir
                        rename { "original_resource_file.txt" }
                    }
                }
            }
        }
    }

    protected onBeforePreBuildTask() {

    }

    def createAaptWorkspace() {
        println("method createWorkDir")
        //创建中间缓存文件
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
        mProject.ext.aaptResourceDir = aaptResourceDir
        mProject.ext.aaptSourceDir = aaptSourceDir
    }

    def generateDependencies(ApplicationVariant applicationVariant, PackageManifest packageManifest) {

        if (!applicationVariant.buildType.name.equalsIgnoreCase("release")) {
            return
        }
        FileUtil.saveFile(mHookerDir, "dependencies", {
            List<String> dependenciesList = new ArrayList<String>()
            Consumer consumer = new Consumer<SyncIssue>() {
                @Override
                void accept(SyncIssue syncIssue) {
                }
            }
            ImmutableMap<String, String> buildMapping =
                    BuildMappingUtils.computeBuildMapping(mProject.getGradle())

            Dependencies dependencies = CommonFactory.getInstance()
                    .getArtifactDependencyGraph(applicationVariant.variantData.scope, false,
                            buildMapping, consumer)


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
        })


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
