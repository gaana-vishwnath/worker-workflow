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

var System = Java.type("java.lang.System");
var URL = Java.type("java.net.URL");
var ByteArray = Java.type("byte[]");

function onAfterProcessTask(eventObj) {
    processDocument(eventObj.rootDocument, eventObj.task);
}

function processDocument(document, task) {
    var scripts = task.getScripts();
    var scriptSettingsFunction = scripts.stream().filter(function (script) {
        script.getName().equals("temp-workflow.js")
    }).findFirst().get().getObject();
    var requiredSettings = scriptSettingsFunction();
    var addWorkflowConfig = new com.github.cafdataprocessing.workflow.AddWorkflowConfigs();
    var customWorkflowSettings = addWorkflowConfig.addCustomWorkflowConfig(json.parse(requiredSettings), document.customdata.tenantId, document);
    document.getTask().getResponse().customData.put("CAF_WORKFLOW_SETTINGS", customWorkflowSettings);
    document.getField("CAF_WORKFLOW_SETTINGS").add(customWorkflowSettings);
}
