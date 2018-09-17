package com.github.maiflai

import org.gradle.api.tasks.testing.Test
import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent
import org.gradle.api.tasks.util.PatternSet

class Scalatest extends Test {
    protected Scalatest(BackwardsCompatibleJavaExecActionFactory factory) {
        this.group = 'verification'
        this.description = 'Run scalatest tests'
        this.configure(this, factory ?: new BackwardsCompatibleJavaExecActionFactory(this.project.gradle.gradleVersion))
    }

    public Scalatest() {
        this(null)
    }

    protected static void configure(Test test, BackwardsCompatibleJavaExecActionFactory factory) {
        test.maxParallelForks = Runtime.runtime.availableProcessors()
        //noinspection GroovyAssignabilityCheck
        test.actions = [
                new ScalaTestAction(factory)
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
        test.reports.html.enabled = false
    }
}
