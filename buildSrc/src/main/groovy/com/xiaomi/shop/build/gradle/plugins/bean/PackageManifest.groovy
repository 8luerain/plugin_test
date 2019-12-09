package com.xiaomi.shop.build.gradle.plugins.bean

import com.google.common.collect.ArrayListMultimap
import com.google.common.collect.ListMultimap
import com.google.common.collect.Lists
import com.xiaomi.shop.build.gradle.plugins.ShopPlugin
import com.xiaomi.shop.build.gradle.plugins.bean.dependence.AarDependenceInfo
import com.xiaomi.shop.build.gradle.plugins.bean.dependence.JarDependenceInfo
import com.xiaomi.shop.build.gradle.plugins.bean.res.ResourceEntry
import com.xiaomi.shop.build.gradle.plugins.bean.res.StyleableEntry
import org.gradle.api.Project

class PackageManifest {
    //包名
    String packageName
    //路径
    String packagePath
    Project mProject
    //处理后的依赖文件，记录依赖库及版本等
    private File mDependenciesFile
    //依赖库内存化
    private Map mDependenciesMap
    //aar库依赖
    private aarDependenciesLibs = [] as Set<AarDependenceInfo>
    //jar库依赖
    private jarDependenciesLibs = [] as Set<JarDependenceInfo>
    //processResource后， 原始的R文件 ,../build/intermediates/symbols/release/R.txt
    private File mOriginalResourceTxtFile
    //更新新的PackageId后，重新生成R.java
    private File updatedRJava
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

    Map getDependenciesMap() {
        if (mDependenciesMap == null) {
            generateDependenciesMap()
        }
        return mDependenciesMap
    }

    def getAarDependenciesLibs() {
        return aarDependenciesLibs
    }

    void setAarDependenciesLibs(aarDependenciesLibs) {
        this.aarDependenciesLibs = aarDependenciesLibs
    }

    def getJarDependenciesLibs() {
        return jarDependenciesLibs
    }

    void setJarDependenciesLibs(jarDependenciesLibs) {
        this.jarDependenciesLibs = jarDependenciesLibs
    }

    public getOriginalResourceTxtFile() {
        return mOriginalResourceTxtFile
    }

    void setOriginalResourceTxtFile(File originalResourceTxtFile) {
        this.mOriginalResourceTxtFile = originalResourceTxtFile
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

    File getRJavaFile() {
        File updatedRJava = new File([ShopPlugin.aaptSourceDir, packagePath, "R.java"].join(File.separator))
        return generateRJavaInner(updatedRJava, packageName, getResourcesMap(), getStyleablesList())
    }

    protected void generateRJavaInner(File dest, String pkg, ListMultimap<String, ResourceEntry> resources, List<StyleableEntry> styleables) {
        if (!dest.parentFile.exists()) {
            dest.parentFile.mkdirs()
        }

        if (!dest.exists()) {
            dest.createNewFile()
        }

        dest.withPrintWriter { pw ->
            pw.println "/* AUTO-GENERATED FILE.  DO NOT MODIFY."
            pw.println " * "
            pw.println " * This class was automatically generated by the"
            pw.println " * aapt tool from the resource data it found.  It"
            pw.println " * should not be modified by hand."
            pw.println " */"
            pw.println "package ${pkg};"
            pw.println "public final class R {"

            resources.keySet().each { type ->
                pw.println "    public static final class ${type} {"
                resources.get(type).each { entry ->
                    pw.println "        public static final int ${entry.resourceName} = ${entry.hexNewResourceId};"
                }
                pw.println "    }"
            }

            pw.println "    public static final class styleable {"
            styleables.each { styleable ->
                pw.println "        public static final ${styleable.valueType} ${styleable.name} = ${styleable.value};"
            }
            pw.println "    }"
            pw.println "}"
        }
    }


    def getDependenciesFile() {
        return mDependenciesFile
    }

    void setDependenciesFile(File dependenciesFile) {
        this.mDependenciesFile = dependenciesFile
    }

    def generateDependenciesMap() {
        if (mDependenciesMap == null) {
            mDependenciesMap = [] as LinkedHashMap
        }
        mDependenciesFile.splitEachLine('\\s+', { columns ->
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
            mDependenciesMap.put("${module.group}:${module.name}", module)
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
