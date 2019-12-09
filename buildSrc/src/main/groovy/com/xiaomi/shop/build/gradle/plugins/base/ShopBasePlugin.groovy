package com.xiaomi.shop.build.gradle.plugins.base

import com.android.build.gradle.internal.api.ApplicationVariantImpl
import com.android.build.gradle.internal.ide.dependencies.BuildMappingUtils
import com.android.build.gradle.tasks.ProcessAndroidResources
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
import org.gradle.internal.reflect.Instantiator
import org.gradle.tooling.provider.model.ToolingModelBuilderRegistry

import javax.inject.Inject
import java.util.function.Consumer

class ShopBasePlugin implements Plugin<Project> {

    Project mProject
    public static File mHookerDir
    public static File aaptResourceDir
    public static File aaptSourceDir
    Instantiator mInstantiator

    @Inject
    ShopBasePlugin(Instantiator instantiator, ToolingModelBuilderRegistry registry) {
        mInstantiator = instantiator
    }

    @Override
    void apply(Project project) {
        if (!project.plugins.hasPlugin('com.android.application')) {
            throw new Exception("需要在application中使用")
        }
        mProject = project
        //创建中间缓存文件
        mHookerDir = new File(project.getProjectDir(), "hooker")
        if (!mHookerDir.exists()) {
            mHookerDir.mkdir()
        }
        //存放资源
        aaptResourceDir = new File([mHookerDir, "intermediates", "resource"].join(File.separator))
        if (aaptResourceDir.exists()) {
            aaptResourceDir.delete()
        }
        aaptResourceDir.mkdir()
        //存放源码
        aaptSourceDir = new File([mHookerDir, "intermediates", "source", "r"].join(File.separator))
        if (aaptSourceDir.exists()) {
            aaptSourceDir.delete()
        }
        aaptSourceDir.mkdir()
    }

    def generateDependencies(ApplicationVariantImpl applicationVariant, PackageManifest packageManifest) {
        if (!applicationVariant.buildType.name.equalsIgnoreCase("release")) {
            return
        }
        FileUtil.saveFile(mHookerDir, "dependencies", {
            List<String> deps = new ArrayList<String>()
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
                deps.add(androidLibrary.name)
                packageManifest.aarDependenciesLibs.add(
                        new AarDependenceInfo(
                                androidCoordinates.groupId,
                                androidCoordinates.artifactId,
                                androidCoordinates.version,
                                androidLibrary))
            }
            dependencies.getJavaLibraries().each { JavaLibrary library ->
//                println(" dependencies.getLibraries()[${library.name}]")
                deps.add(library.name)
                def jarCoordinates = library.resolvedCoordinates
                packageManifest.jarDependenciesLibs.add(
                        new JarDependenceInfo(
                                jarCoordinates.groupId,
                                jarCoordinates.artifactId,
                                jarCoordinates.version,
                                library))
            }
            dependencies.getProjects().each { String path ->
//                println(" dependencies.getProjects[${path}]")
                deps.add(path)

            }
            Collections.sort(deps)
            return deps
        })

        ProcessAndroidResources aaptTask = mProject.tasks["process${applicationVariant.name.capitalize()}Resources"]
        aaptTask.doLast {
            mProject.copy {
                from aaptTask.textSymbolOutputFile
                into mHookerDir
                rename { "original_resource_file.txt" }
            }
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

    Instantiator getInstantiator() {
        return mInstantiator
    }
}
