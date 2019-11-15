package com.xiaomi.shop.build.gradle.plugins

import org.gradle.api.Plugin
import org.gradle.api.Project

class ShopHostPlugin implements Plugin<Project> {
    @Override
    void apply(Project project) {
        project.task("my_plugin_test").doFirst {
            println("print form plugin test")
        }
        project.afterEvaluate {
            project.getTasks().forEach {
                println("task name[" + it.name + "]")
            }
        }
    }
}