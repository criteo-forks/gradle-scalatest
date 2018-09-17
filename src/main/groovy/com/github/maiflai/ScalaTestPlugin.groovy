package com.github.maiflai

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.plugins.scala.ScalaPlugin
import org.gradle.api.tasks.testing.Test

/**
 * Applies the Java & Scala Plugins
 * Replaces Java Test actions with a <code>ScalaTestAction</code>
 */
class ScalaTestPlugin implements Plugin<Project> {

    static String MODE = 'com.github.maiflai.gradle-scalatest.mode'
    static enum Mode {
        replaceAll, replaceOne, append, prepend
    }
    BackwardsCompatibleJavaExecActionFactory factory

    @Override
    void apply(Project t) {
        if (!t.plugins.hasPlugin(ScalaTestPlugin)) {
            factory = new BackwardsCompatibleJavaExecActionFactory(t.gradle.gradleVersion)
            t.plugins.apply(ScalaPlugin)
            switch (getMode(t)) {
                case Mode.replaceAll:
                    t.tasks.withType(Test) { Scalatest.configure(it, factory) }
                    break
                case Mode.replaceOne:
                    t.tasks.withType(Test) {
                        if (it.name == JavaPlugin.TEST_TASK_NAME) {
                            Scalatest.configure(it, factory)
                        }
                    }
                    break
                case Mode.append:
                    createScalatestTask(t)
                    break
                case Mode.prepend:
                    def scalatest = createScalatestTask(t)
                    t.tasks.getByName(JavaPlugin.TEST_TASK_NAME).dependsOn(scalatest)
            }
        }
    }

    private static Test createScalatestTask(Project t) {
        return t.tasks.create(name: 'scalatest', type: Scalatest,
                              dependsOn: t.tasks.testClasses) as Test
    }

    private static Mode getMode(Project t) {
        if (!t.hasProperty(MODE)) {
            return Mode.prepend
        } else {
            return Mode.valueOf(t.properties[MODE].toString())
        }
    }
}
