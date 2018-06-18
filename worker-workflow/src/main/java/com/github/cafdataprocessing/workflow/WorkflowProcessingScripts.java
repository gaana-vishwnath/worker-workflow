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

import com.hpe.caf.worker.document.model.Document;
import com.hpe.caf.worker.document.model.Script;
import com.hpe.caf.worker.document.model.Scripts;
import javax.script.ScriptException;

/**
 * Adds a JavaScript workflow to a document's task
 */
final class WorkflowProcessingScripts
{
    private WorkflowProcessingScripts()
    {
    }

    /**
     * Sets the workflow script used in evaluating a document on the task, also sets a temporary script used for evaluating which worker
     * to send the document to next. The temporary script is used to remove the need for datastore retrieval of the script before
     * processing.
     *
     * @param document The document used to provide access to the task.
     * @param workflowAsJavaScript JavaScript representation of a workflow.
     * @param workflowStorageRef storage reference for the workflow passed.
     * @throws ScriptException if there is a failure in workflow script loading.
     */
    public static void setScripts(final Document document, final String workflowAsJavaScript, final String workflowStorageRef)
        throws ScriptException
    {
        final Scripts scripts = document.getTask().getScripts();

        // Add temporary script to the task using the setScriptInline setter.
        final Script tempWorkflowScript = scripts.add();
        tempWorkflowScript.setName("temp-workflow.js");
        tempWorkflowScript.setScriptInline(workflowAsJavaScript);
        tempWorkflowScript.load();

        // Add persistant script to the task using the setScriptByReference setter.
        final Script workflowScript = scripts.add();
        workflowScript.setName("workflow.js");
        workflowScript.setScriptByReference(workflowStorageRef);
        workflowScript.install();
    }
}
