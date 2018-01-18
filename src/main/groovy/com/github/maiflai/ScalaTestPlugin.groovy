package com.github.maiflai

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.plugins.scala.ScalaPlugin
import org.gradle.api.tasks.testing.Test
import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent
import org.gradle.api.tasks.util.PatternSet

/**
 * Applies the Java & Scala Plugins
 * Replaces Java Test actions with a <code>ScalaTestAction</code>
 */
class ScalaTestPlugin implements Plugin<Project> {

    static String MODE = 'com.github.maiflai.gradle-scalatest.mode'
    static enum Mode {
        replaceAll, replaceOne, append, prepend
    }

    @Override
    void apply(Project t) {
        if (!t.plugins.hasPlugin(ScalaTestPlugin)) {
            t.plugins.add(this)
            t.plugins.apply(ScalaPlugin)
            switch (getMode(t)) {
                case Mode.replaceAll:
                    t.tasks.withType(Test) { configure(it) }
                    break
                case Mode.replaceOne:
                    t.tasks.withType(Test) {
                        if (it.name == JavaPlugin.TEST_TASK_NAME) {
                            configure(it)
                        }
                    }
                    break
                case Mode.append:
                    configure(createScalatestTask(t))
                    break
                case Mode.prepend:
                    def scalatest = createScalatestTask(t)
                    configure(scalatest)
                    t.tasks.getByName(JavaPlugin.TEST_TASK_NAME).dependsOn(scalatest)
            }
        }
    }

    private static Test createScalatestTask(Project t) {
        return t.tasks.create(
                            name: 'scalatest', type: Test, group: 'verification',
                            description: 'Run scalatest unit tests',
                            dependsOn: t.tasks.testClasses) as Test
    }

    private static Mode getMode(Project t) {
        if (!t.hasProperty(MODE)) {
            return Mode.prepend
        } else {
            return Mode.valueOf(t.properties[MODE].toString())
        }
    }

    static void configure(Test test) {
        test.maxParallelForks = Runtime.runtime.availableProcessors()
        //noinspection GroovyAssignabilityCheck
        test.actions = [
                new JacocoTestAction(),
                new ScalaTestAction()
        ]
        test.testLogging.exceptionFormat = TestExceptionFormat.SHORT
        test.extensions.add(ScalaTestAction.TAGS, new PatternSet())
        List<String> suites = []
        test.extensions.add(ScalaTestAction.SUITES, suites)
        test.extensions.add("suite", { String name -> suites.add(name) })
        test.extensions.add("suites", { String... name -> suites.addAll(name) })
        Map<String, ?> config = [:]
        test.extensions.add(ScalaTestAction.CONFIG, config)
        test.extensions.add("config", { String name, value -> config.put(name, value) })
        test.extensions.add("configMap", { Map<String, ?> c -> config.putAll(c) })
        List<String> argLines = []
        test.extensions.add(ScalaTestAction.ARGLINES, argLines)
        test.extensions.add("argLine", { String name -> argLines.add(name) })
        List<String> suffixes = []
        test.extensions.add(ScalaTestAction.SUFFIXES, suffixes)
        test.extensions.add("suffix", { String name -> suffixes.add(name) })
        test.extensions.add("suffixes", { String... name -> suffixes.addAll(name) })
        test.testLogging.events = TestLogEvent.values() as Set
    }
}
