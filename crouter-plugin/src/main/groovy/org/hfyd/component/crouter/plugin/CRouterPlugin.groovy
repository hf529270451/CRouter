package org.hfyd.component.crouter.plugin

import com.android.build.gradle.AppExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.hfyd.component.crouter.plugin.extensions.CRouterExtension
import com.android.build.gradle.AppPlugin


class CRouterPlugin implements Plugin<Project> {

    @Override
    void apply(Project project) {
        def isApp = project.plugins.hasPlugin(AppPlugin)
        if (isApp) {
            AppExtension appExtension = project.extensions.findByType(AppExtension.class)
            appExtension.registerTransform(new CRouterTransform())
        }

        CRouterExtension cRouterExtension = project.extensions.create("crouter", CRouterExtension.class)
        if (cRouterExtension != null) {
            project.plugins.apply('maven-publish')
            ApiRegister apiRegister = new ApiRegister(project, cRouterExtension)
            project.afterEvaluate {
                apiRegister.init()
            }
        }
    }
}