package com.github.openapivalidatorgradleplugin

import com.atlassian.oai.validator.OpenApiInteractionValidator
import com.atlassian.oai.validator.model.Request
import com.atlassian.oai.validator.model.SimpleRequest
import com.atlassian.oai.validator.model.SimpleResponse
import com.atlassian.oai.validator.report.LevelResolver
import com.atlassian.oai.validator.report.LevelResolverFactory
import com.atlassian.oai.validator.report.ValidationReport
import groovy.transform.CompileDynamic
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.file.FileCollection
import org.gradle.api.logging.LogLevel
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction

/**
 * Validates specified files as request or response bodies for specified call in OpenAPI specification.
 */
@CompileDynamic
class ValidateRequestResponseTask extends DefaultTask {

    /**
     * OpenAPI specification file.
     */
    @InputFile
    File specificationFile

    /**
     * Location of request files to validate.
     */
    @InputFiles
    FileCollection requestFiles = project.files()

    /**
     * Location of response files to validate.
     */
    @InputFiles
    FileCollection responseFiles = project.files()

    /**
     * Request path.
     */
    @Input
    String path

    /**
     * Request method.
     */
    @Input
    String method

    /**
     * Response status code.
     */
    @Input
    int status = 200

    /**
     * Request content type.
     */
    @Input
    String requestContentType = 'application/merge-patch+json'

    /**
     * Response content type.
     */
    @Input
    String responseContentType = 'application/json'

    /**
     * Indicates if validation of additional properties should be the same as
     * specified by the schema (true) or additional properties
     * should not be allowed (false).
     */
    @Input
    boolean additionalPropertiesAsInSchema = true

    /**
     * Validation message level to gradle logger level mapping.
     */
    // Let's have a nicely formatted map.
    @SuppressWarnings('SpaceAroundMapEntryColon')
    @Internal
    Map<ValidationReport.Level, LogLevel> levels = [
            (ValidationReport.Level.ERROR) : LogLevel.ERROR,
            (ValidationReport.Level.WARN)  : LogLevel.WARN,
            (ValidationReport.Level.INFO)  : LogLevel.INFO,
            (ValidationReport.Level.IGNORE): LogLevel.DEBUG,
    ]

    /**
     * Creates an instance.
     */
    ValidateRequestResponseTask() {
        group = 'verification'
        description = 'Validates files as responses bodies against OpenAPI specification'
    }

    /**
     * Validates all specified files.
     */
    @TaskAction
    void validate() {
        OpenApiInteractionValidator validator =
                OpenApiInteractionValidator.createFor('file:' + specificationFile.absolutePath).
                        withLevelResolver(additionalPropertiesAsInSchema ?
                                LevelResolverFactory.withAdditionalPropertiesIgnored() :
                                LevelResolver.create().withDefaultLevel(ValidationReport.Level.ERROR).build()).
                        build()
        requestFiles?.each { requestFile ->
            validator.validateRequest(
                    (SimpleRequest.Builder."${method.toLowerCase()}"(path) as SimpleRequest.Builder).
                            withAccept(responseContentType).
                            withContentType(requestContentType).
                            withBody(requestFile.text).
                            build()).with { result ->
                result.messages?.each { message ->
                    logger.log(levels[message.level], "[${message.key}] ${message.message}")
                }
                if (result.hasErrors()) {
                    throw new GradleException(
                            "$requestFile does not match request ${method.toUpperCase()} $path from $specificationFile")
                }
            }
        }
        responseFiles?.each { responseFile ->
            validator.validateResponse(
                    path,
                    Request.Method.valueOf(method.toUpperCase()),
                    SimpleResponse.Builder.status(status).
                            withContentType(responseContentType).
                            withBody(responseFile.text).
                            build()).with { result ->
                result.messages?.each { message ->
                    logger.log(levels[message.level], "[${message.key}] ${message.message}")
                }
                if (result.hasErrors()) {
                    throw new GradleException(
                            "$responseFile does not match response ${method.toUpperCase()} $path $status" +
                                    " from $specificationFile")
                }
            }
        }
    }
}
