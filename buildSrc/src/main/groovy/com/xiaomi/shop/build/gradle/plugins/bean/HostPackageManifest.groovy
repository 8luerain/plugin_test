package com.xiaomi.shop.build.gradle.plugins.bean

import com.google.common.collect.ArrayListMultimap
import com.google.common.collect.ListMultimap
import com.google.common.collect.Lists
import com.xiaomi.shop.build.gradle.plugins.bean.res.ResourceEntry
import com.xiaomi.shop.build.gradle.plugins.bean.res.StyleableEntry

class HostPackageManifest {
    File dependenciesFile
    File originalResourceFile
    ListMultimap<String, ResourceEntry> resourcesMap
    List<StyleableEntry> styleablesList = Lists.newArrayList()

    private Map hostDependenciesMap

    Map getHostDependenciesMap() {
        if (hostDependenciesMap == null) {
            hostDependenciesMap = [] as LinkedHashMap
            dependenciesFile.splitEachLine('\\s+', { columns ->
                String id = columns[0]
                def module = [group: 'unspecified', name: 'unspecified', version: 'unspecified']
                def findResult = id =~ /[^@:]+/
                int matchIndex = 0
                findResult.each {
                    if (matchIndex == 0) {
                        module.group = it
                    }
                    if (matchIndex == 1) {
                        module.name = it
                    }
                    if (matchIndex == 2) {
                        module.version = it
                    }
                    matchIndex++
                }
                hostDependenciesMap.put("${module.group}:${module.name}", module)
            })
        }
        return hostDependenciesMap
    }

    ListMultimap<String, ResourceEntry> getResourcesMap() {
        if (resourcesMap == null) {
            resourcesMap = ArrayListMultimap.create()
        }
        return resourcesMap
    }

    List<StyleableEntry> getStyleablesList() {
        return styleablesList
    }
}
