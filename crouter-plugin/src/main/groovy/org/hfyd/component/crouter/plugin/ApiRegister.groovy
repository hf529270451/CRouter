package org.hfyd.component.crouter.plugin

import com.android.build.gradle.BaseExtension
import com.android.builder.model.Dependencies
import org.gradle.api.Project
import org.gradle.api.artifacts.dsl.DependencyHandler
import org.gradle.api.tasks.bundling.Jar
import org.hfyd.component.crouter.plugin.extensions.CRouterExtension

class ApiRegister {

    private Project project
    private CRouterExtension cRouterExtension
    private BaseExtension android
    private DependencyHandler dependencies

    private String apiPackage

    ApiRegister(Project project, CRouterExtension cRouterExtension) {
        this.project = project
        this.cRouterExtension = cRouterExtension
        this.android = project.extensions.getByName('android')
        this.dependencies = project.dependencies
        Utils.project = project

        dependencies.metaClass.compileApi { value ->
            project.dependencies {
                compileOnly value
            }
        }

        Dependencies.metaClass.compileApi { value ->
            project.dependencies {
                compileOnly value
            }
        }
    }

    void init() {
        this.apiPackage = this.android.defaultConfig.javaCompileOptions.annotationProcessorOptions.arguments['CROUTER_API_PACKAGE']

        Utils.addApiSourceSets(project)
        if (!project.file('src/main/api').exists()) {
            project.file('src/main/api').mkdir()
        }

        def publishConfig = this.cRouterExtension.publishConfig
        if (publishConfig == null || apiPackage == null) {
            return
        }

        def maven = publishConfig.publications.getByName('maven')
        if (maven != null) {
            maven.pom.packaging 'jar'
            maven.artifact project.tasks.create("makeJar", Jar.class) {
                def apiPackagePath = apiPackage.replace('.', '/')
                from("build/intermediates/javac/release/classes/$apiPackagePath/")
                into(apiPackagePath)
            }
        }

        def buildTask = project.tasks.getByName('build')
        def publishTask = project.tasks.getByName('publish')
        publishTask.dependsOn(buildTask)

        project.tasks.create('publishApiToMaven') {
            dependsOn publishTask
            group 'crouter'
        }

    }
}