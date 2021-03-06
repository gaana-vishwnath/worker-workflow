/*
 * Copyright 2015-2018 Micro Focus or one of its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.cafdataprocessing.workflow.model;

/**
 * Constant values relating to the Workflow Worker
 */
public final class WorkflowWorkerConstants
{
    private WorkflowWorkerConstants()
    {
    }

    /**
     * Key values used for custom data on worker tasks.
     */
    public static class CustomData
    {
        /**
         * Key for custom data property identifying the data store partial reference to use when storing transformed workflow.
         */
        public static final String OUTPUT_PARTIAL_REFERENCE = "outputPartialReference";
        /**
         * Key for custom data property identifying the projectId that workflow and its components were created under.
         */
        public static final String PROJECT_ID = "projectId";
        /**
         * Key for custom data property identifying the tenantId that workflow and its components were created under.
         */
        public static final String TENANT_ID = "tenantId";
        /**
         * Key for custom data property identifying the name of the workflow to execute against a document.
         */
        public static final String WORKFLOW_NAME = "workflowName";
    }

    /**
     * Error codes describing reason for a failure to process documents.
     */
    public static class ErrorCodes
    {
        public static final String INVALID_CUSTOM_DATA = "WORKFLOW-InvalidCustomData";
        public static final String STORE_WORKFLOW_FAILED = "WORKFLOW-StoreWorkflowFailed";
        public static final String ADD_WORKFLOW_SCRIPTS_FAILED = "WORKFLOW-FailedToAddScriptsToTask";
    }
}
