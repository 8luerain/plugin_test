package com.xiaomi.shop.build.gradle.plugins.extension

class PluginConfigExtension {
    public String libraryName
    public boolean useHostDependencies
    public String hostPath


    String getLibraryName() {
        return libraryName
    }

    String getHostPath() {
        return hostPath
    }

    void setHostPath(String hostPath) {
        this.hostPath = hostPath
    }

    void setLibraryName(String libraryName) {
        this.libraryName = libraryName
    }

    boolean getUseHostDependencies() {
        return useHostDependencies
    }

    void setUseHostDependencies(boolean useHostDependencies) {
        this.useHostDependencies = useHostDependencies
    }
}