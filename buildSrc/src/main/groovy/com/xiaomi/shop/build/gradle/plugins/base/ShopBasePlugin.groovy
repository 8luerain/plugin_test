package com.xiaomi.shop.build.gradle.plugins.base

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.internal.reflect.Instantiator
import org.gradle.tooling.provider.model.ToolingModelBuilderRegistry

import javax.inject.Inject

class ShopBasePlugin implements Plugin<Project> {

    @Inject
    public ShopBasePlugin(Instantiator instantiator, ToolingModelBuilderRegistry registry) {
    }
    @Override
    void apply(Project project) {

    }
}
