package com.xiaomi.shop.build.gradle.plugins.bean

import com.google.common.collect.ArrayListMultimap
import com.google.common.collect.ListMultimap
import com.xiaomi.shop.build.gradle.plugins.bean.PackageManifest
import com.xiaomi.shop.build.gradle.plugins.bean.res.ResourceEntry
import com.xiaomi.shop.build.gradle.plugins.bean.res.StyleableEntry

class MergedPackageManifest extends PackageManifest {
    PackageManifest mInput
    PackageManifest mSource


    MergedPackageManifest(PackageManifest input, PackageManifest source) {
        mInput = input
        mSource = source
    }

    @Override
    ListMultimap<String, ResourceEntry> getResourcesMap() {
        def sourceResources = mSource.resourcesMap
        def inputResources = mInput.resourcesMap
        if (resourcesMap == null) {
            resourcesMap = ArrayListMultimap.create()
            sourceResources.values().each {
                def index = inputResources.get(it.resourceType).indexOf(it)
                if (index >= 0) {
//                it.newResourceId = inputResources.get(it.resourceType).get(index).resourceId
//                inputResources.get(it.resourceType).set(index, it)
                } else {
                    resourcesMap.put(it.resourceType, it)
                }
            }
        }
        return resourcesMap
    }

    @Override
    List<StyleableEntry> getStyleablesList() {
        def sourceStyle = mSource.styleablesList
        def inputStyle = mInput.styleablesList
        if (styleablesList == null) {
            styleablesList = new ArrayList<>()
            sourceStyle.each {
                def index = inputStyle.indexOf(it)
                if (index >= 0) {
//                    it.value = inputStyle.get(index).value
//                    inputStyle.set(index, it)
                } else {
                    styleablesList.add(it)
                }
            }
        }
        return styleablesList
    }
}
