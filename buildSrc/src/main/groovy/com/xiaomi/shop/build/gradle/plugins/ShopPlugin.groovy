package com.xiaomi.shop.build.gradle.plugins

import com.android.build.gradle.AppExtension
import com.android.build.gradle.AppPlugin
import com.android.build.gradle.internal.variant.VariantFactory
import com.xiaomi.shop.build.gradle.plugins.base.ShopBasePlugin
import com.xiaomi.shop.build.gradle.plugins.extension.PluginConfigExtension
import com.xiaomi.shop.build.gradle.plugins.hooker.manager.PluginHookerManager
import com.xiaomi.shop.build.gradle.plugins.utils.ExtensionApplyUtils
import com.xiaomi.shop.build.gradle.plugins.utils.Log
import com.xiaomi.shop.build.gradle.plugins.utils.ProjectDataCenter
import com.xiaomi.shop.build.gradle.plugins.utils.Reflect
import org.gradle.api.Project
import org.gradle.internal.reflect.Instantiator
import org.gradle.tooling.provider.model.ToolingModelBuilderRegistry

import javax.inject.Inject
import java.lang.reflect.InvocationHandler
import java.lang.reflect.Method
import java.lang.reflect.Proxy

class ShopPlugin extends ShopBasePlugin {
    Project mProject
    AppPlugin mAppPlugin
    AppExtension mAndroidExtension
    Instantiator mInstantiator

    @Inject
    ShopPlugin(Instantiator instantiator, ToolingModelBuilderRegistry registry) {
        super(instantiator, registry)
        mInstantiator = instantiator
    }

    @Override
    void apply(Project project) {
        mProject = project
        mAppPlugin = project.plugins.findPlugin(AppPlugin)
        mAndroidExtension = project.getExtensions().findByType(AppExtension)
        modifyAndroidExtension()
        initialConfig(project)
    }

    //生命周期「解析variant之前」,可以修改一些build.gradle原定的配置
    def modifyAndroidExtension() {
        Reflect reflect = Reflect.on(mAppPlugin.variantManager)
        VariantFactory variantFactory = Proxy.newProxyInstance(this.class.classLoader, [VariantFactory.class] as Class[],
                new InvocationHandler() {
                    Object delegate = reflect.get('variantFactory')

                    @Override
                    Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                        if ('preVariantWork' == method.name) {
                            onPreVariantWork()
                        }
                        return method.invoke(delegate, args)
                    }
                })
        reflect.set('variantFactory', variantFactory)

    }

    def onPreVariantWork() {
        Log.i 'mishop', "onPreVariantWork"
        def hostDependency = ProjectDataCenter.getInstance(mProject).hostPackageManifest
                .hostDependenciesMap.get("aa")
        ExtensionApplyUtils.applyUseHostResourceConfig(mProject)
    }

    void initialConfig(Project project) {
        injectBaseExtension(project)

        project.afterEvaluate {
            PluginHookerManager manager = new PluginHookerManager(project, mInstantiator)
            manager.registerTaskHookers()
        }
    }

    def injectBaseExtension(Project project) {
        project.getExtensions().add("pluginconfig", PluginConfigExtension)
    }

}