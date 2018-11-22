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
package com.github.cafdataprocessing.workflow.transform;

public final class WorkflowRepresentation
{
    private String workflowJavascript;
    private String workflowSettings;

    /**
     * @return the workflowJavascript
     */
    public String getWorkflowJavascript()
    {
        return workflowJavascript;
    }

    /**
     * @param workflowJavascript the workflowJavascript to set
     */
    public void setWorkflowJavascript(String workflowJavascript)
    {
        this.workflowJavascript = workflowJavascript;
    }

    /**
     * @return the workflowSettings
     */
    public String getWorkflowSettings()
    {
        return workflowSettings;
    }

    /**
     * @param workflowSettings the workflowSettings to set
     */
    public void setWorkflowSettings(String workflowSettings)
    {
        this.workflowSettings = workflowSettings;
    }

}
