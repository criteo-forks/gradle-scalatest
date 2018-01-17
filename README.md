gradle-scalatest
================
A plugin to enable the use of scalatest in a gradle Scala project. [![Build Status](https://travis-ci.org/maiflai/gradle-scalatest.svg?branch=master)](https://travis-ci.org/maiflai/gradle-scalatest)

Getting started
---------------
http://plugins.gradle.org/plugin/com.github.maiflai.scalatest

It prepends the test task provided by the scalatest plugin to the existing test task (see [Other Frameworks](#other-frameworks) below).

In addition to your `testCompile` dependency on scalatest, you also require a `testRuntime` dependency on pegdown in
order to create the HTML report.

```groovy
dependencies {
  testCompile 'org.scalatest:scalatest_2.11:3.0.1'
  testRuntime 'org.pegdown:pegdown:1.4.2'
}
```
---

Compatibility
-------------
This plugin aims to be compatible with the current version of Gradle. 
The table below indicates the minimum required version.

|Gradle|gradle-scalatest|scalatest|
|------|----------------|---------|
|4.0   |0.16            |2.0      |
|3.0   |0.14            |2.0      |
|2.14.1|0.13            |2.0      |

Parallel Testing
----------------
The default behaviour is to use as many parallel threads as you have available processors.

`Test` tasks are modified at the time that you apply the plugin (as otherwise they would default to single-threaded).

To disable this, you should configure your test tasks accordingly.

```groovy
test {
    maxParallelForks = 1
}
```

Tags
----
Scalatest provides support for filtering tests by tagging. We cannot use the `PatternSet` provided by the `Test`
task because it applies this filter to test files internally.

We therefore provide an extension named `tags` to `Test` tasks.

```groovy
test {
    tags {
        exclude 'org.scalatest.tags.Slow'
    }
}

task slowTest(type: Test) {
    tags {
        include 'org.scalatest.tags.Slow'
    }
}
```

Suites
------
Suites are supported with another extension to the `Test` task.
```groovy
task userStories(type: Test) {
    suite 'com.example.UserStories'
    // suites 'a.Spec', 'b.Spec', 'etc'
}
```

Filtering
---------
Scalatest provides a simplified wildcard syntax for selecting tests. 
We directly map [Gradle test filters](https://docs.gradle.org/current/javadoc/org/gradle/api/tasks/testing/TestFilter.html) to this form.

```groovy
test {
    filter {
        includeTestsMatching 'MyTest'
    }
}
```

This can also be supplied on the command line:

```
./gradlew test --tests MyTest
```

ConfigMap
---------
Additional configuration can be passed to Scalatest using the [config map](http://www.scalatest.org/user_guide/using_the_runner#configMapSection)

```groovy 
test {
    config 'db.name', 'testdb'
}
```

```groovy 
test {
    configMap([
        'db.name': 'testdb'
        'server': '192.168.1.188'
        ])
}
```

ArgLine
-------
Alternatively, you can append additional configuration to Scalatest with the `argLine` option. The whole lines will be appended at the end of the `JavaExec` task.

```groovy 
test {
    argLine '-Ddb.name=testdb -X'
    argLine '''\
        -Dserver=192.168.1.188
        -d64
        -classpath $PATH
    '''
}
```

Other Frameworks
----------------
The default behaviour is to inject the task "scalatest" just before the `Test` task named "test".

The `com.github.maiflai.gradle-scalatest.mode` property may be configured to support the following behaviour:

|Value        |Behaviour                                              |
|-------------|-------------------------------------------------------|
|replaceAll   |replace all instances of the `Test` task               |
|replaceOne   |replace only the `Test` task named "test"              |
|append       |create a new scalatest `Test` task named "scalatest"   |
|prepend      |create a new scalatest `Test` task named "scalatest" automatically called before task named "test" |

It's probably easiest to set this in a gradle.properties file at the root of your project.

```
com.github.maiflai.gradle-scalatest.mode = append
```