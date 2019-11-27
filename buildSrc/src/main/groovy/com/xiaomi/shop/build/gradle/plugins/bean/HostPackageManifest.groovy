package com.xiaomi.shop.build.gradle.plugins.bean

class HostPackageManifest {
    File dependenciesFile;
    File originalResourceFile;

    private Map hostDependenciesMap

    public Map getHostDependenciesMap() {
        if (hostDependenciesMap == null) {
            hostDependenciesMap = [] as LinkedHashMap
            dependenciesFile.splitEachLine('\\s+', { columns ->
                String id = columns[0]
                int index1 = id.indexOf(':')
                int index2 = id.lastIndexOf(':')
                def module = [group: 'unspecified', name: 'unspecified', version: 'unspecified']

                if (index1 < 0 || index2 < 0 || index1 == index2) {
                    Log.e('Dependencies', "Parsed error: [${id}] -> ${module}")
                    return
                }

                if (index1 > 0) {
                    module.group = id.substring(0, index1)
                }
                if (index2 - index1 > 0) {
                    module.name = id.substring(index1 + 1, index2)
                }
                if (id.length() - index2 > 1) {
                    module.version = id.substring(index2 + 1)
                }

                hostDependenciesMap.put("${module.group}:${module.name}", module)
            })
        }
        hostDependenciesMap.keySet().each {
            println("each key --- [${it}]")
        }
        return hostDependenciesMap
    }


}
