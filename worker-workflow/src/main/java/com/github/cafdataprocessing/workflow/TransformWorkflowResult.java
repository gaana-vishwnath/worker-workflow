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
package com.github.cafdataprocessing.workflow;

import com.github.cafdataprocessing.workflow.transform.WorkflowRepresentation;

/**
 * Holds information about a transformed workflow
 */
final class TransformWorkflowResult
{
    private final WorkflowRepresentation workflowRepresentation;
    private final String workflowStorageRef;

    /**
     * Instantiate a TransformWorkflowResult recording the provided transformed workflow as a string and its storage reference.
     *
     * @param transformedWorkflow the transformed workflow as a string.
     * @param workflowStorageRef storage reference to the transformed workflow in the data store.
     */
    public TransformWorkflowResult(final WorkflowRepresentation transformedWorkflow, final String workflowStorageRef)
    {
        this.workflowRepresentation = transformedWorkflow;
        this.workflowStorageRef = workflowStorageRef;
    }

    public WorkflowRepresentation getWorkflowRepresentation()
    {
        return workflowRepresentation;
    }

    public String getWorkflowStorageRef()
    {
        return workflowStorageRef;
    }
}
