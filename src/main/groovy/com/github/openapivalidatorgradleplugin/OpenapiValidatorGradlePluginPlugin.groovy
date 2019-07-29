package com.github.openapivalidatorgradleplugin

import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * A plugin that allows request/response validation.
 *
 * No default task is added. You need to explicitly specify parameters and register the task.
 *
 * See {@link ValidateRequestResponseTask}.
 */
class OpenapiValidatorGradlePluginPlugin implements Plugin<Project> {
    @Override
    void apply(Project project) {
    }
}
