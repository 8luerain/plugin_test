package com.xiaomi.shop.build.gradle.plugins

class PluginConfigExtension {
    public String libraryName

    String getLibraryName() {
        return libraryName
    }

    void setLibraryName(String libraryName) {
        this.libraryName = libraryName
    }
}