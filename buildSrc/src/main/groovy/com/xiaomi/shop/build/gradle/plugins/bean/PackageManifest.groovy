package com.xiaomi.shop.build.gradle.plugins.bean

import com.google.common.collect.ArrayListMultimap
import com.google.common.collect.ListMultimap
import com.google.common.collect.Lists
import com.xiaomi.shop.build.gradle.plugins.bean.res.ResourceEntry
import com.xiaomi.shop.build.gradle.plugins.bean.res.StyleableEntry
import org.gradle.api.Project

class PackageManifest {

    Project mProject
    //处理后的依赖文件，记录依赖库及版本等
    File dependenciesFile
    //依赖库内存化
    Map hostDependenciesMap

    //processResource后， 原始的R文件 ,../build/intermediates/symbols/release/R.txt
    File originalResourceTxtFile
    //处理R文件后生成的资源map
    private ListMultimap<String, ResourceEntry> mResourcesMap
    //处理R文件后生成的styleMap
    private List<StyleableEntry> mStyleablesList = Lists.newArrayList()

    //processResource后中间文件
    File resourceOutputFileDir
    File sourceOutputFileDir

    PackageManifest(Project mProject) {
        this.mProject = mProject
    }

    Map getHostDependenciesMap() {
        if (hostDependenciesMap == null) {
            generateDependenciesMap()
        }
        return hostDependenciesMap
    }

    ListMultimap<String, ResourceEntry> getResourcesMap() {
        if (this.mResourcesMap == null) {
            parseRFile()
        }
        return mResourcesMap
    }

    List<StyleableEntry> getStyleablesList() {
        if (mStyleablesList == null) {
            parseRFile()
        }
        return mStyleablesList
    }

    def generateDependenciesMap() {
        if (hostDependenciesMap == null) {
            hostDependenciesMap = [] as LinkedHashMap
        }
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

    private void parseRFile() {
        if (!originalResourceTxtFile.exists()) {
            return
        }
        if (mResourcesMap == null) {
            mResourcesMap = ArrayListMultimap.create()
        }
        if (mStyleablesList == null) {
            mStyleablesList = new ArrayList<>()
        }
        originalResourceTxtFile.eachLine { line ->
            if (!line.empty) {
                def tokenizer = new StringTokenizer(line)
                def valueType = tokenizer.nextToken()
                def resType = tokenizer.nextToken()
                def resName = tokenizer.nextToken()
                def resId = tokenizer.nextToken('\r\n').trim()

                if (resType == 'styleable') {
                    mStyleablesList.add(new StyleableEntry(resName, resId, valueType))
                } else {
                    mResourcesMap.put(resType, new ResourceEntry(resType, resName, Integer.decode(resId)))
                }
            }
        }
    }


    def getResourcesMapForAapt() {
        return convertResourcesForAsrsEditor(getResourcesMap())
    }

    def getStyleablesListForAapt() {
        return convertStyleablesForArscEditor(getStyleablesList())
    }

    def convertResourcesForAsrsEditor(ListMultimap<String, ResourceEntry> pluginResources) {
        def retainedTypes = []

        pluginResources.keySet().each { resType ->
            def firstEntry = pluginResources.get(resType).get(0)
            def typeEntry = [type   : "int", name: resType,
                             id     : parseTypeIdFromResId(firstEntry.resourceId),
                             _id    : parseTypeIdFromResId(firstEntry.newResourceId),
                             entries: []]

            pluginResources.get(resType).each { resEntry ->
                typeEntry.entries.add([
                        name: resEntry.resourceName,
                        id  : parseEntryIdFromResId(resEntry.resourceId),
                        _id : parseEntryIdFromResId(resEntry.newResourceId),
                        v   : resEntry.resourceId, _v: resEntry.newResourceId,
                        vs  : resEntry.hexResourceId, _vs: resEntry.hexNewResourceId])
            }

            retainedTypes.add(typeEntry)
        }

        retainedTypes.sort { t1, t2 ->
            t1._id - t2._id
        }

        return retainedTypes
    }


    def convertStyleablesForArscEditor(List<StyleableEntry> pluginStyleables) {
        def retainedStyleables = []
        pluginStyleables.each { styleableEntry ->
            retainedStyleables.add([vtype: styleableEntry.valueType,
                                    type : 'styleable',
                                    key  : styleableEntry.name,
                                    idStr: styleableEntry.value])
        }
        return retainedStyleables
    }


    def parseTypeIdFromResId(int resourceId) {
        resourceId >> 16 & 0xFF
    }

    def parseEntryIdFromResId(int resourceId) {
        resourceId & 0xFFFF
    }
}
