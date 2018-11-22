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

import java.util.Locale;
import java.util.Objects;

import org.apache.commons.lang3.StringEscapeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Methods that are available to be called during Workflow XSLT transformation. These are intended to provide some useful operations at
 * transformation time.
 */
public class TransformerFunctions
{
    public static final Logger LOG = LoggerFactory.getLogger(TransformerFunctions.class);

    /**
     * Escape characters in the passed value that need to be escaped before being written to JavaScript.
     *
     * @param valueToEscape value that should have characters escaped
     * @return escaped value
     * @throws NullPointerException if {@code valueToEscape} is null
     */
    public static String escapeForJavaScript(final String valueToEscape)
    {
        Objects.requireNonNull(valueToEscape);
        return StringEscapeUtils.escapeEcmaScript(valueToEscape);
    }

    /**
     * Checks system environment and system properties for the specified property name returning the value if it is found or null if it is
     * not. If both value is set for both environment and system property then the system property will be returned.
     *
     * @param name Name of property to retrieve value for.
     * @return Value matching the specified property name or null if no matching name is found.
     */
    public static String getEnvironmentValue(final String name)
    {
        return System.getProperty(name, System.getenv(name) != null ? System.getenv(name) : "");
    }

    /**
     * Returns value for worker queue set in the environment based on provided worker name. Returns null if no match found in environment.
     *
     * @param workerName Identifies the worker queue is for. System environment and property values will be checked using this value.
     * @return Queue value associated with specified worker name or null if no match found.
     */
    public static String getWorkerQueueFromEnvironment(final String workerName)
    {
        if (workerName == null || workerName.isEmpty()) {
            return null;
        }
        return escapeForJavaScript(getEnvironmentValue(workerName.toLowerCase(Locale.ENGLISH) + ".taskqueue"));
    }
}
