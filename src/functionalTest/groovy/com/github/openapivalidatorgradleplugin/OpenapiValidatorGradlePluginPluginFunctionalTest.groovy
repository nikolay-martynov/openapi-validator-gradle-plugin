package com.github.openapivalidatorgradleplugin

import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification

class OpenapiValidatorGradlePluginPluginFunctionalTest extends Specification {

    @Rule
    TemporaryFolder testProjectDir = new TemporaryFolder()

    File buildFile

    def setup() {
        buildFile = testProjectDir.newFile('build.gradle')
        buildFile << """
plugins {
    id 'com.github.openapi-validator-gradle-plugin'
}
"""
    }

    def "multiple valid response files result in success"() {
        given:
        buildFile << """
task validate(type:com.github.openapivalidatorgradleplugin.ValidateRequestResponseTask) {
    specificationFile = file("${new File("src/functionalTest/resources/specification.json").absolutePath}")
    responseFiles = fileTree("${new File("src/functionalTest/resources/").absolutePath}").tap {
        it.include "alarms-response-valid-*.json"
    }
    path = "/alarms"
    method = "get"
}
        """
        when:
        BuildResult result = GradleRunner.create()
                .withProjectDir(testProjectDir.root)
                .withPluginClasspath()
                .withArguments("-s", "validate")
                .build()

        then:
        result.task(":validate").outcome == TaskOutcome.SUCCESS
    }

    def "invalid response file results in error"() {
        given:
        buildFile << """
task validate(type:com.github.openapivalidatorgradleplugin.ValidateRequestResponseTask) {
    specificationFile = file("${new File("src/functionalTest/resources/specification.json").absolutePath}")
    responseFiles = fileTree("${new File("src/functionalTest/resources/").absolutePath}").tap {
        it.include "alarms-response-valid-1.json"
        it.include "alarms-response-invalid-2.json"
    }
    path = "/alarms"
    method = "get"
}
        """
        when:
        BuildResult result = GradleRunner.create()
                .withProjectDir(testProjectDir.root)
                .withPluginClasspath()
                .withArguments("-s", "validate")
                .buildAndFail()

        then:
        result.task(":validate").outcome == TaskOutcome.FAILED
        result.output.contains("alarms-response-invalid-2.json")
    }

    def "unexpected request body results in error"() {
        given:
        buildFile << """
task validate(type:com.github.openapivalidatorgradleplugin.ValidateRequestResponseTask) {
    specificationFile = file("${new File("src/functionalTest/resources/specification.json").absolutePath}")
    requestFiles = files("${new File("src/functionalTest/resources/alarms-request.json").absolutePath}")
    path = "/alarms"
    method = "get"
}
        """
        when:
        BuildResult result = GradleRunner.create()
                .withProjectDir(testProjectDir.root)
                .withPluginClasspath()
                .withArguments("-s", "validate")
                .buildAndFail()

        then:
        result.task(":validate").outcome == TaskOutcome.FAILED
        result.output.contains("alarms-request.json")
    }

    def "invalid request body results in error"() {
        given:
        buildFile << """
task validate(type:com.github.openapivalidatorgradleplugin.ValidateRequestResponseTask) {
    specificationFile = file("${new File("src/functionalTest/resources/specification.json").absolutePath}")
    requestFiles = files("${new File("src/functionalTest/resources/comment-request-invalid.json").absolutePath}")
    path = "/alarms/123/comments"
    requestContentType = "application/json"
    method = "post"
}
        """
        when:
        BuildResult result = GradleRunner.create()
                .withProjectDir(testProjectDir.root)
                .withPluginClasspath()
                .withArguments("-s", "validate")
                .buildAndFail()

        then:
        result.task(":validate").outcome == TaskOutcome.FAILED
        result.output.contains("comment-request-invalid.json")
    }

    def "valid request body results in success"() {
        given:
        buildFile << """
task validate(type:com.github.openapivalidatorgradleplugin.ValidateRequestResponseTask) {
    specificationFile = file("${new File("src/functionalTest/resources/specification.json").absolutePath}")
    requestFiles = files("${new File("src/functionalTest/resources/comment-request-valid.json").absolutePath}")
    path = "/alarms/123/comments"
    requestContentType = "application/json"
    method = "post"
}
        """
        when:
        BuildResult result = GradleRunner.create()
                .withProjectDir(testProjectDir.root)
                .withPluginClasspath()
                .withArguments("-s", "validate")
                .build()

        then:
        result.task(":validate").outcome == TaskOutcome.SUCCESS
    }

    def "oneOf in schema can be used without false positives"() {
        given:
        buildFile << """
task validate(type:com.github.openapivalidatorgradleplugin.ValidateRequestResponseTask) {
    specificationFile = file("${new File("src/functionalTest/resources/specification.json").absolutePath}")
    requestFiles = files("${new File("src/functionalTest/resources/acknowledge-request.json").absolutePath}")
    path = "/alarms/123"
    method = "patch"
}
        """
        when:
        BuildResult result = GradleRunner.create()
                .withProjectDir(testProjectDir.root)
                .withPluginClasspath()
                .withArguments("-s", "validate")
                .build()

        then:
        result.task(":validate").outcome == TaskOutcome.SUCCESS
    }
}
