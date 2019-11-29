package com.xiaomi.shop.build.gradle.plugins.bean

import com.google.common.collect.ArrayListMultimap
import com.google.common.collect.ListMultimap
import com.google.common.collect.Lists
import com.xiaomi.shop.build.gradle.plugins.bean.res.ResourceEntry
import com.xiaomi.shop.build.gradle.plugins.bean.res.StyleableEntry

class PackageManifest {

    //处理后的依赖文件，记录依赖库及版本等
    File dependenciesFile
    //依赖库内存化
    private Map hostDependenciesMap

    //processResource后， 原始的R文件
    File originalResourceFile
    //处理R文件后生成的资源map
    ListMultimap<String, ResourceEntry> resourcesMap
    //处理R文件后生成的styleMap
    List<StyleableEntry> styleablesList = Lists.newArrayList()

    //processResource后中间文件
    File resourceOutputFileDir
    File sourceOutputFileDir


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
            parseRFile()
        }
        return resourcesMap
    }

    List<StyleableEntry> getStyleablesList() {
        if (styleablesList == null) {
            styleablesList = new ArrayList<>()
            parseRFile()
        }
        return styleablesList
    }

    private void parseRFile() {
        if (!originalResourceFile.exists()) {
            return
        }
        originalResourceFile.eachLine { line ->
            if (!line.empty) {
                def tokenizer = new StringTokenizer(line)
                def valueType = tokenizer.nextToken()
                def resType = tokenizer.nextToken()
                def resName = tokenizer.nextToken()
                def resId = tokenizer.nextToken('\r\n').trim()

                if (resType == 'styleable') {
                    styleablesList.add(new StyleableEntry(resName, resId, valueType))
                } else {
                    resourcesMap.put(resType, new ResourceEntry(resType, resName, Integer.decode(resId)))
                }
            }
        }
    }

    def parseTypeIdFromResId(int resourceId) {
        resourceId >> 16 & 0xFF
    }

    def parseEntryIdFromResId(int resourceId) {
        resourceId & 0xFFFF
    }
}
