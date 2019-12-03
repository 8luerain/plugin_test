package com.xiaomi.shop.build.gradle.plugins.bean

import com.google.common.collect.ArrayListMultimap
import com.google.common.collect.ListMultimap
import com.google.common.collect.Lists
import com.xiaomi.shop.build.gradle.plugins.bean.res.ResourceEntry
import com.xiaomi.shop.build.gradle.plugins.bean.res.StyleableEntry
import com.xiaomi.shop.build.gradle.plugins.extension.PluginConfigExtension
import org.gradle.api.Project

class MergedPackageManifest extends PackageManifest {
    PackageManifest mInput
    PackageManifest mSource
    //处理R文件后生成的资源map
    private ListMultimap<String, ResourceEntry> mMergedResourcesMap
    //处理R文件后生成的styleMap
    private List<StyleableEntry> mMergedStyleablesList = Lists.newArrayList()
    //构建R.txt文件格式的list

    int packageId = 0x7f //指定新的packageID,默认7f

    MergedPackageManifest(PackageManifest input, PackageManifest source , Project project) {
        super(project)
        mInput = input
        mSource = source
        packageId = mProject.getExtensions().findByType(PluginConfigExtension).packageId
    }


    @Override
    ListMultimap<String, ResourceEntry> getResourcesMap() {
        def sourceResources = mSource.resourcesMap
        def inputResources = mInput.resourcesMap
        if (mMergedResourcesMap == null) {
            mMergedResourcesMap = ArrayListMultimap.create()
            sourceResources.values().each {
                def index = inputResources.get(it.resourceType).indexOf(it)
                if (index >= 0) {
                    it.newResourceId = inputResources.get(it.resourceType).get(index).resourceId
                    inputResources.get(it.resourceType).set(index, it)
                } else {
                    mMergedResourcesMap.put(it.resourceType, it)
                }
            }
        }
        return mMergedResourcesMap
    }

    @Override
    List<StyleableEntry> getStyleablesList() {
        def sourceStyle = mSource.styleablesList
        def inputStyle = mInput.styleablesList
        if (mMergedStyleablesList == null) {
            mMergedStyleablesList = new ArrayList<>()
            sourceStyle.each {
                def index = inputStyle.indexOf(it)
                if (index >= 0) {
//                    it.value = inputStyle.get(index).value
//                    inputStyle.set(index, it)
                } else {
                    mMergedStyleablesList.add(it)
                }
            }
        }
        return mMergedStyleablesList
    }
}
