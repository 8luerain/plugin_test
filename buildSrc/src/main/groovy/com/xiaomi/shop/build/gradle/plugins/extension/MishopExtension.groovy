package com.xiaomi.shop.build.gradle.plugins.extension

class MishopExtension {
    /**
     * 固定资源ID的文件路径
     */
    private String stableIdPath

    String getStableIdPath() {
        return stableIdPath
    }

    void setStableIdPath(String stableIdPath) {
        this.stableIdPath = stableIdPath
    }
}