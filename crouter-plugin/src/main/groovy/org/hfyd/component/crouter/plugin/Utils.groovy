package org.hfyd.component.crouter.plugin

import com.android.build.gradle.AppExtension
import com.android.build.gradle.BaseExtension
import com.android.build.gradle.LibraryExtension
import org.gradle.api.Project

class Utils {

    static Project project

    static addApiSourceSets(Project project) {
        BaseExtension baseExtension = project.extensions.getByName('android')
        //将sourceSets.main 多一个mis的源代码路径
        addApiSourceSets(baseExtension, 'main')
        if(baseExtension instanceof AppExtension) {
            AppExtension appExtension = (AppExtension) baseExtension
            appExtension.getApplicationVariants().each {
                addApiSourceSets(baseExtension, it.buildType.name)
                it.productFlavors.each {
                    addApiSourceSets(baseExtension, it.name)
                }
                if(it.productFlavors.size() >= 1) {
                    if(it.productFlavors.size() > 1) {
                        addApiSourceSets(baseExtension, it.flavorName)
                    }
                    addApiSourceSets(baseExtension, it.name)
                }
            }
        } else if(baseExtension instanceof LibraryExtension) { //如果是个lib 走这里
            LibraryExtension libraryExtension = (LibraryExtension) baseExtension
            //Variants 变体 例如 debug release 每个打包成的apk版本
            libraryExtension.getLibraryVariants().each {
                //it.buildType.name 为渠道号下的debug 或者release
                addApiSourceSets(baseExtension, it.buildType.name)
                it.productFlavors.each {
                    addApiSourceSets(baseExtension, it.name)
                }
                if(it.productFlavors.size() >= 1) {
                    if(it.productFlavors.size() > 1) {
                        addApiSourceSets(baseExtension, it.flavorName)
                    }
                    addApiSourceSets(baseExtension, it.name)
                }
            }
        }

    }

    /**
     *  将sourceSets.main 多一个api的源代码路径
     * @param baseExtension
     * @param name
     * @return
     */
    static addApiSourceSets(BaseExtension baseExtension, String name) {
        //获取sorceSets.main
        def obj = baseExtension.sourceSets.getByName(name)
        def srcDirs = []
        obj.java.srcDirs.each {
            srcDirs.add(it)
        }
        srcDirs.add(project.file('src/main/api'))
        obj.java.srcDirs = srcDirs
    }
}