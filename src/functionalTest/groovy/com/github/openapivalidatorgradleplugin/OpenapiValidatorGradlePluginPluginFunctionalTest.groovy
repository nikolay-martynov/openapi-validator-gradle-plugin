/*
 * This Groovy source file was generated by the Gradle 'init' task.
 */
package com.github.openapivalidatorgradleplugin

import spock.lang.Specification
import org.gradle.testkit.runner.GradleRunner

/**
 * A simple functional test for the 'com.github.openapivalidatorgradleplugin.greeting' plugin.
 */
public class OpenapiValidatorGradlePluginPluginFunctionalTest extends Specification {
    def "can run task"() {
        given:
        def projectDir = new File("build/functionalTest")
        projectDir.mkdirs()
        new File(projectDir, "settings.gradle") << ""
        new File(projectDir, "build.gradle") << """
            plugins {
                id('com.github.openapivalidatorgradleplugin.greeting')
            }
        """

        when:
        def runner = GradleRunner.create()
        runner.forwardOutput()
        runner.withPluginClasspath()
        runner.withArguments("greeting")
        runner.withProjectDir(projectDir)
        def result = runner.build()

        then:
        result.output.contains("Hello from plugin 'com.github.openapivalidatorgradleplugin.greeting'")
    }
}
