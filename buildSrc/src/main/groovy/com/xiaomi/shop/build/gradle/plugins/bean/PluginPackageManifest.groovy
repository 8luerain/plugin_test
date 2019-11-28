package com.xiaomi.shop.build.gradle.plugins.bean

import com.google.common.collect.ArrayListMultimap
import com.google.common.collect.ListMultimap
import com.google.common.collect.Lists
import com.xiaomi.shop.build.gradle.plugins.bean.res.ResourceEntry
import com.xiaomi.shop.build.gradle.plugins.bean.res.StyleableEntry

class PluginPackageManifest {
    File dependenciesFile
    File originalResourceFile
    ListMultimap<String, ResourceEntry> resourcesMap
    List<StyleableEntry> styleablesList = Lists.newArrayList()
    //process资源后，文件输出文件夹
    File resourceOutputFileDir
    //process资源后，source输出文件夹
    File sourceOutputFileDir

    private Map pluginDependenciesMap

    Map getHostDependenciesMap() {
        if (pluginDependenciesMap == null) {
            pluginDependenciesMap = [] as LinkedHashMap
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
        return pluginDependenciesMap
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

    File getResourceOutputFileDir() {
        return resourceOutputFileDir
    }

    void setResourceOutputFileDir(File resourceOutputFileDir) {
        this.resourceOutputFileDir = resourceOutputFileDir
    }
}
