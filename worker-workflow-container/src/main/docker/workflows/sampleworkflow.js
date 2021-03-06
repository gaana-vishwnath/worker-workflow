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

// Workflow ID: 1
// Workflow Name: sampleworkflow

/* global Java, java, thisScript */

var System = Java.type("java.lang.System");
var URL = Java.type("java.net.URL");
var ByteArray = Java.type("byte[]");

// Constants for return values
var ACTION_TO_EXECUTE = 'actionToExecute';
var CONDITIONS_NOT_MET = 'conditionsNotMet';
var ALREADY_EXECUTED = 'alreadyExecuted';

function onProcessTask() {
    thisScript.install();
}

function onAfterProcessTask(eventObj) {
    processDocument(eventObj.rootDocument);
}

function processDocument(document) {
    var cafWorkflowSettingsJson = document.getCustomData("CAF_WORKFLOW_SETTINGS") 
                                ? document.getCustomData("CAF_WORKFLOW_SETTINGS") 
                                : document.getField("CAF_WORKFLOW_SETTINGS").getStringValues().stream().findFirst().get();
    if (cafWorkflowSettingsJson === undefined){
        throw new java.lang.UnsupportedOperationException("Document must contain field CAF_WORKFLOW_SETTINGS.");
    }
    var cafWorkflowSettings = JSON.parse(cafWorkflowSettingsJson);
    updateActionStatus(document);

    // Rule Name: Document Enrichment
    var ruleResult = rule_1(document, cafWorkflowSettings);
    if (ruleResult == ACTION_TO_EXECUTE)
        return;

    // Rule Name: Output
    var ruleResult = rule_2(document, cafWorkflowSettings);
    if (ruleResult == ACTION_TO_EXECUTE)
        return;

}


// Rule Name: Document Enrichment
// Return ACTION_TO_EXECUTE if an action in the rule should be executed
function rule_1(document, settings) {
    if (isRuleCompleted(document, '1')) {
        return ALREADY_EXECUTED;
    }


    // Action Name: Family Hashing
    var actionResult = action_1(document, settings);
    if (actionResult == ACTION_TO_EXECUTE)
        return ACTION_TO_EXECUTE;


    // Action Name: LangDetect
    var actionResult = action_2(document, settings);
    if (actionResult == ACTION_TO_EXECUTE)
        return ACTION_TO_EXECUTE;


    recordRuleCompleted(document, '1');
}

// Action Name: Family Hashing
// Return CONDITIONS_NOT_MET if the document did not match the action conditions
function action_1(document, settings) {
    if (isActionCompleted(document, '1')) {
        return ALREADY_EXECUTED;
    }

    var actionDetails = {
        internal_name: 'ChainedActionType',
        queueName: 'dataprocessing-family-hashing-in',
        workerName: 'familyhashingworker',
        
        scripts: [
            {
                name: 'recordProcessingTimes.js', script: 'function onProcessTask(e) {\n  var startTime = new Date();\n  e.rootDocument.getField(\"LONG_FAMILY_HASHING_START_TIME\").set(startTime.getTime());\n  e.rootDocument.getField(\"ENRICHMENT_TIME\").set(Math.round(startTime.getTime() \/ 1000));\n}\nfunction onAfterProcessTask(e) {\n  var endTime = new Date();\n  e.rootDocument.getField(\"LONG_FAMILY_HASHING_END_TIME\").set(endTime.getTime());\n}\nfunction onError(e) {\n  var failedTime = new Date();\n  e.rootDocument.getField(\"LONG_FAMILY_HASHING_FAILED_TIME\").set(failedTime.getTime());\n}\n'
            }
        ]
    };

    recordActionToExecute(document, '1', actionDetails);
    return evaluateActionDetails(document, actionDetails);
}

// Action Name: LangDetect
// Return CONDITIONS_NOT_MET if the document did not match the action conditions
function action_2(document, settings) {
    if (isActionCompleted(document, '2')) {
        return ALREADY_EXECUTED;
    }
if (!(existsCondition_ (document, 'CONTENT_PRIMARY',''))) return CONDITIONS_NOT_MET;
    var actionDetails = {
        internal_name: 'ChainedActionType',
        queueName: 'dataprocessing-langdetect-in',
        workerName: 'langdetectworker',
        customData: {'fieldSpecs': 'CONTENT_PRIMARY'},
        scripts: [
            {
                name: 'recordProcessingTimes.js', script: 'function onProcessTask(e) {\n  var startTime = new Date();\n  e.rootDocument.getField(\"LONG_LANGUAGE_DETECTION_START_TIME\").set(startTime.getTime());\n}\nfunction onAfterProcessTask(e) {\n  var endTime = new Date();\n  e.rootDocument.getField(\"LONG_LANGUAGE_DETECTION_END_TIME\").set(endTime.getTime());\n}\nfunction onError(e) {\n  var failedTime = new Date();\n  e.rootDocument.getField(\"LONG_LANGUAGE_DETECTION_FAILED_TIME\").set(failedTime.getTime());\n}\n'
            }
        ]
    };

    recordActionToExecute(document, '2', actionDetails);
    return evaluateActionDetails(document, actionDetails);
}

// Rule Name: Output
// Return ACTION_TO_EXECUTE if an action in the rule should be executed
function rule_2(document, settings) {
    if (isRuleCompleted(document, '2')) {
        return ALREADY_EXECUTED;
    }


    // Action Name: Send to Output Queue
    var actionResult = action_5(document, settings);
    if (actionResult == ACTION_TO_EXECUTE)
        return ACTION_TO_EXECUTE;


    recordRuleCompleted(document, '2');
}

// Action Name: Send to Output Queue
// Return CONDITIONS_NOT_MET if the document did not match the action conditions
function action_5(document, settings) {
    if (isActionCompleted(document, '5')) {
        return ALREADY_EXECUTED;
    }

    var actionDetails = {
        internal_name: 'ChainedActionType',
        queueName: 'bulk-index',
        workerName: '',
        
        scripts: [
            
        ]
    };

    recordActionToExecute(document, '5', actionDetails);
    return evaluateActionDetails(document, actionDetails);
}

// Common logic to evaluate each field value on a document against a provided criteria function.
// If the a field with the passed name has not values then false is returned.
function evaluateValuesAgainstCondition(document, fieldName, expectedValue, evaluateFunction) {
    if (!document.getField(fieldName).hasValues()) {
        return false;
    }
    var fieldValues = document.getField(fieldName).getValues();
    for each(var fieldValue in fieldValues) {
        var valueToEvaluate = getDocumentFieldValueAsString(fieldValue);
        if (evaluateFunction(expectedValue, valueToEvaluate) === true) {
            return true;
        }
    }
    return false;
}

function existsCondition_(document, fieldName) {
    return document.getField(fieldName).hasValues();
}

function stringCondition_is(document, fieldName, value) {
    return evaluateValuesAgainstCondition(document, fieldName, value, function (expectedValue, actualValue) {
        if (actualValue.equalsIgnoreCase(expectedValue)) {
            return true;
        }
    });
}

function stringCondition_contains(document, fieldName, value) {
    return evaluateValuesAgainstCondition(document, fieldName, value, function (expectedValue, actualValue) {
        if (actualValue.toUpperCase(java.util.Locale.getDefault())
                .contains(expectedValue.toUpperCase(java.util.Locale.getDefault()))) {
            return true;
        }
    });
}

function stringCondition_starts_with(document, fieldName, value) {
    return evaluateValuesAgainstCondition(document, fieldName, value, function (expectedValue, actualValue) {
        if (actualValue.toUpperCase(java.util.Locale.getDefault())
                .startsWith(expectedValue.toUpperCase(java.util.Locale.getDefault()))) {
            return true;
        }
    });
}

function stringCondition_ends_with(document, fieldName, value) {
    return evaluateValuesAgainstCondition(document, fieldName, value, function (expectedValue, actualValue) {
        if (actualValue.toUpperCase(java.util.Locale.getDefault())
                .endsWith(expectedValue.toUpperCase(java.util.Locale.getDefault()))) {
            return true;
        }
    });
}

function regexCondition_(document, fieldName, value) {
    throw new java.lang.UnsupportedOperationException("Regex is not supported");
}

function dateCondition_before(document, fieldName, value) {
    throw new java.lang.UnsupportedOperationException("Date before is not supported");
}

function dateCondition_after(document, fieldName, value) {
    throw new java.lang.UnsupportedOperationException("Date after is not supported");
}

function dateCondition_on(document, fieldName, value) {
    throw new java.lang.UnsupportedOperationException("Date on is not supported");
}

function numberCondition_gt(document, fieldName, value) {
    throw new java.lang.UnsupportedOperationException("Number greater than is not supported");
}

function numberCondition_lt(document, fieldName, value) {
    throw new java.lang.UnsupportedOperationException("Number less than is not supported");
}

function numberCondition_eq(document, fieldName, value) {
    throw new java.lang.UnsupportedOperationException("Number equal to is not supported");
}

function notCondition(aBoolean) {
    return !aBoolean;
}

function isRuleCompleted(document, ruleId) {
    return document.getField('CAF_WORKFLOW_RULES_COMPLETED').getStringValues().contains(ruleId);
}

function isActionCompleted(document, actionId) {
    return document.getField('CAF_WORKFLOW_ACTIONS_COMPLETED').getStringValues().contains(actionId);
}

function recordRuleCompleted(document, ruleId) {
    document.getField('CAF_WORKFLOW_RULES_COMPLETED').add(ruleId);
}

function recordActionCompleted(document, actionId) {
    document.getField('CAF_WORKFLOW_ACTIONS_COMPLETED').add(actionId);
}

function recordActionToExecute(document, actionId) {
    document.getField('CAF_WORKFLOW_ACTION').add(actionId);
}

function updateActionStatus(document) {
    // This may be the first time the document has been presented to the workflow
    if (!document.getField('CAF_WORKFLOW_ACTION').hasValues()) {
        return;
    }
    recordActionCompleted(document, document.getField('CAF_WORKFLOW_ACTION').getStringValues().get(0));
    document.getField('CAF_WORKFLOW_ACTION').clear();
}


function resolveValue(value, defaultValue){
    var v = value ? value : defaultValue;
    if(v === undefined) {
         throw new java.lang.RuntimeException("Unable to determine which value to assign to custom data as both possiblities are undefined");    
    }
    return v;
}
        
// Evaluate the determined details of an action, either executing the action against document or preparing the
// document to execute the action
function evaluateActionDetails(document, actionDetails) {
    if (typeof actionDetails === 'function') {
        actionDetails();
        updateActionStatus(document);
        return ALREADY_EXECUTED;
    }

    // Propagate the custom data if it exists
    var responseCustomData = actionDetails.customData ? actionDetails.customData : {};

    // Update document destination queue to that specified by action and pass appropriate settings and customData
    var queueToSet = !isEmpty(actionDetails.queueName) ? actionDetails.queueName : actionDetails.workerName + "Input";
    var response = document.getTask().getResponse();
    response.successQueue.set(queueToSet);
    response.failureQueue.set(queueToSet);
    response.customData.putAll(responseCustomData);

    // Add any scripts specified on the action
    if (actionDetails.scripts.length != 0) {
        for each(var scriptToAdd in actionDetails.scripts) {
            var scriptObjectAdded = document.getTask().getScripts().add();
            scriptObjectAdded.setName(scriptToAdd.name);

            if (scriptToAdd.script !== undefined) {
                scriptObjectAdded.setScriptInline(scriptToAdd.script);
            } else if (scriptToAdd.storageRef !== undefined) {
                scriptObjectAdded.setScriptByReference(scriptToAdd.storageRef);
            } else if (scriptToAdd.url !== undefined) {
                scriptObjectAdded.setScriptByUrl(new URL(scriptToAdd.url));
            } else {
                throw new java.lang.RuntimeException("Invalid script definition on action. No valid script value source.");
            }

            scriptObjectAdded.install();
        }
    }

    return ACTION_TO_EXECUTE;
}

// Executes the field mapping action on the document
function executeFieldMapping(document, mappings) {
    // Get the field values to map (from the document)
    var documentFieldsValuesMap = {};
    for (var mappingKey in mappings) {
        var documentFieldToMapFrom = document.getField(mappingKey);
        documentFieldsValuesMap[mappingKey] = documentFieldToMapFrom.getValues();
        documentFieldToMapFrom.clear();
    }

    // For each mapping add the original field value with the new key
    for (var mappingKey in mappings) {
        var mappingDestination = mappings[mappingKey];
        var documentFieldToMapTo = document.getField(mappingDestination);
        for each(var fieldValue in documentFieldsValuesMap[mappingKey]) {
            if (fieldValue.isReference()) {
                documentFieldToMapTo.addReference(fieldValue.getReference());
            } else {
                documentFieldToMapTo.add(fieldValue.getValue());
            }
        }
    }
}

// Returns string representing value of a Document Worker FieldValue
function getDocumentFieldValueAsString(fieldValue) {
    if (!fieldValue.isReference()) {
        return fieldValue.getStringValue();
    }
    var reference = fieldValue.getReference();
    var valueByteArrayStream = new java.io.ByteArrayOutputStream();
    var valueToReturn;
    var valueDataStoreStream;
    try {
        valueDataStoreStream = fieldValue.openInputStream();
        var valueBuffer = new ByteArray(1024);
        var valuePortionLength;
        while ((valuePortionLength = valueDataStoreStream.read(valueBuffer)) != -1) {
            valueByteArrayStream.write(valueBuffer, 0, valuePortionLength);
        }
        valueToReturn = valueByteArrayStream.toString("UTF-8");
    } catch (e) {
        throw new java.lang.RuntimeException("Failed to retrieve document field value using reference: " + reference, e);
    } finally {
        valueByteArrayStream.close();
        if (valueDataStoreStream !== undefined) {
            valueDataStoreStream.close();
        }
    }
    return valueToReturn;
}

// Returns true if a string value is null, undefined or empty
function isEmpty(stringToCheck) {
    return (!stringToCheck || 0 === stringToCheck.length);
}

function onError(errorEventObj) {
    // We will not mark the error as handled here. This will allow the document-worker framework to add the failure
    // itself rather than us duplicating the format of the failure value it constructs for non-script failure responses

    // Even though the action failed it still completed in terms of the document being sent for processing against the
    // action, so the action should be marked as completed
    processDocument(errorEventObj.rootDocument);
}
