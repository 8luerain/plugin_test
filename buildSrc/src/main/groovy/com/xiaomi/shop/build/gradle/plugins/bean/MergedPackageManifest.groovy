package com.xiaomi.shop.build.gradle.plugins.bean

import com.google.common.collect.ArrayListMultimap
import com.google.common.collect.ListMultimap
import com.google.common.collect.Lists
import com.xiaomi.shop.build.gradle.plugins.bean.res.ResourceEntry
import com.xiaomi.shop.build.gradle.plugins.bean.res.StyleableEntry
import com.xiaomi.shop.build.gradle.plugins.extension.PluginConfigExtension
import org.gradle.api.Project

class MergedPackageManifest extends PackageManifest {
    PackageManifest mHostManifest
    PackageManifest mPluginManifest
    //处理R文件后生成的资源map
    private ListMultimap<String, ResourceEntry> mMergedResourcesMap
    //处理R文件后生成的styleMap
    private List<StyleableEntry> mMergedStyleablesList = Lists.newArrayList()
    //构建R.txt文件格式的list

    int packageId = 0x7f //指定新的packageID,默认7f

    MergedPackageManifest(PackageManifest input, PackageManifest source, Project project) {
        super(project)
        mHostManifest = input
        mPluginManifest = source
        packageId = mProject.getExtensions().findByType(PluginConfigExtension).packageId
    }


    @Override
    ListMultimap<String, ResourceEntry> getResourcesMap() {
        def plugin = mPluginManifest.resourcesMap
        def host = mHostManifest.resourcesMap
        if (mMergedResourcesMap == null) {
            mMergedResourcesMap = ArrayListMultimap.create()
            plugin.keySet().each { key ->
                int newResIndex = 0
                plugin.get(key).each {
                    def index = host.get(it.resourceType).indexOf(it)
                    if (index >= 0) {//相同的
                        //更新成host的id，保证使用hostAssetManger可以正常找到资源
                        it.newResourceId = host.get(it.resourceType).get(index).resourceId
                        //保证后续生成映射map时，能使用带更新过的resID
                        host.get(it.resourceType).set(index, it)
                    } else {
                        //重新排序
                        it.setNewResourceId(packageId, parseTypeIdFromResId(it.resourceId), newResIndex++)
                        mMergedResourcesMap.put(it.resourceType, it)
                    }
                }
            }

        }

        return mMergedResourcesMap
    }

    @Override
    List<StyleableEntry> getStyleablesList() {
        def plugin = mPluginManifest.styleablesList
        def host = mHostManifest.styleablesList
        if (mMergedStyleablesList == null) {
            mMergedStyleablesList = new ArrayList<>()
            plugin.each {
                def index = host.indexOf(it)
                if (index >= 0) {
                    it.value = host.get(index).value
                    host.set(index, it)
                } else {
                    mMergedStyleablesList.add(it)
                }
            }
        }
        return mMergedStyleablesList
    }


    def getResIdMap() { //映射新的resourceID
        def idMap = [:] as Map<Integer, Integer>
        getResourcesMap()
        mPluginManifest.resourcesMap.values().each { resEntry ->
            idMap.put(resEntry.resourceId, resEntry.newResourceId)
        }
        idMap.each {
            println("idmap [${it}]")
        }
        return idMap
    }
}
