package com.xiaomi.shop.build.gradle.plugins.hooker

import com.android.build.gradle.AppExtension
import com.android.build.gradle.api.ApkVariant
import com.android.build.gradle.internal.res.LinkApplicationAndroidResourcesTask
import com.xiaomi.shop.build.gradle.plugins.utils.ResourceFormatUtils
import org.gradle.api.Project

class StableHostResourceHooker extends GradleTaskHooker<LinkApplicationAndroidResourcesTask> {
    final static String sFileInputName = 'stable_id_input.txt'
    final static String sFileOutputName = 'stable_id_output.txt'
    File mStableOutputFile
    File mStableInputFile
    AppExtension mAndroidExtension
    File mHookerDir

    @Override
    String getTaskName() {
        return scope.getTaskName('process', 'Resources')
    }

    StableHostResourceHooker(Project project, ApkVariant apkVariant) {
        super(project, apkVariant)
        apkVariant.applicationId
        mHookerDir = project.file('hooker')
        if (!mHookerDir.exists()) {
            mHookerDir.mkdir()
        }
        mStableOutputFile = new File([mHookerDir, sFileOutputName].join(File.separator))
        mStableInputFile = new File([mHookerDir, sFileInputName].join(File.separator))
        mAndroidExtension = project.getExtensions().findByType(AppExtension.class)
//        configStableParam()
    }

    void configStableParam() {
//        if (mStableOutputFile.exists()) {
//            Log.i "ProcessResourcesHooker", "${mStableOutputFile} not exist , generate it."
//            mStableOutputFile.delete()
//        }
//        mStableOutputFile.createNewFile()
        if (mStableInputFile.exists()) {
            mAndroidExtension.aaptOptions.additionalParameters("--stable-ids", "${mStableInputFile}")
        }
//        mAndroidExtension.aaptOptions.additionalParameters("--emit-ids", "${mStableOutputFile}")
    }

    void configStableParamFromTask(LinkApplicationAndroidResourcesTask task) {
//        if (mStableOutputFile.exists()) {
//            Log.i "ProcessResourcesHooker", "${mStableOutputFile} not exist , generate it."
//            mStableOutputFile.delete()
//        }
//        mStableOutputFile.createNewFile()
        if (mStableInputFile.exists()) {
            task.aaptOptions.additionalParameters("--stable-ids", "${mStableInputFile}")
        }
//        task.aaptOptions.additionalParameters("--emit-ids", "${mStableOutputFile}")
    }


    @Override
    void beforeTaskExecute(LinkApplicationAndroidResourcesTask linkAndroidResForBundleTask) {
        println("projectname[$project.name] , taskname[${linkAndroidResForBundleTask.name}], " +
                "taskclass[${linkAndroidResForBundleTask.class.name}]")
        configStableParamFromTask(linkAndroidResForBundleTask)
    }

    @Override
    void afterTaskExecute(LinkApplicationAndroidResourcesTask linkAndroidResForBundleTask) {
        if (mStableOutputFile.exists()) {
            mStableOutputFile.delete()
        }
        if (mStableInputFile.exists()) {
            mStableInputFile.delete()
        }
        ResourceFormatUtils.convertR2Stable(apkVariant.applicationId,linkAndroidResForBundleTask.textSymbolOutputFile, mStableOutputFile)
        project.copy {
            from mStableOutputFile
            into mStableOutputFile.getParentFile()
            rename { sFileInputName }
        }
        mStableOutputFile.delete()
    }
}
