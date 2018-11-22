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

import com.github.cafdataprocessing.processing.service.client.ApiClient;
import com.github.cafdataprocessing.processing.service.client.ApiException;
import com.github.cafdataprocessing.processing.service.client.api.RepositoryConfigurationApi;
import com.github.cafdataprocessing.processing.service.client.api.TenantConfigurationApi;
import com.github.cafdataprocessing.processing.service.client.model.EffectiveRepositoryConfigValue;
import com.github.cafdataprocessing.processing.service.client.model.EffectiveTenantConfigValue;
import com.github.cafdataprocessing.workflow.cache.WorkflowSettingsCacheKey;
import com.github.cafdataprocessing.workflow.cache.WorkflowSettingsRepositoryCacheKey;
import com.github.cafdataprocessing.workflow.cache.WorkflowSettingsTenantCacheKey;
import static com.github.cafdataprocessing.workflow.transform.TransformerFunctions.LOG;
import static com.github.cafdataprocessing.workflow.transform.TransformerFunctions.escapeForJavaScript;
import com.google.common.base.Strings;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.gson.Gson;
import java.lang.reflect.Type;
import com.google.gson.reflect.TypeToken;
import com.hpe.caf.worker.document.exceptions.DocumentWorkerTransientException;
import com.hpe.caf.worker.document.model.Document;
import com.sun.jersey.api.client.ClientHandlerException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class AddWorkflowConfigs
{
    private final static Logger LOGGER;
    private final static ApiClient API_CLIENT;
    private final static LoadingCache<WorkflowSettingsCacheKey, String> SETTINGS_CACHE;
    private final static Gson GSON;
    private final static RepositoryConfigurationApi REPOSITORY_CONFIG_API;
    private final static TenantConfigurationApi TENANT_CONFIG_API;

    static {
        LOGGER = LoggerFactory.getLogger(AddWorkflowConfigs.class);
        API_CLIENT = new ApiClient();
        SETTINGS_CACHE = CacheBuilder.newBuilder()
            .maximumSize(100)
            .expireAfterAccess(5, TimeUnit.MINUTES)
            .recordStats()
            .build(
                new CacheLoader<WorkflowSettingsCacheKey, String>()
            {
                @Override
                public String load(final WorkflowSettingsCacheKey key)
                    throws ApiException, DocumentWorkerTransientException
                {
                    LOGGER.debug("Key not found in cache: " + key);
                    return getSettingsFromServer(key);
                }
            });
        GSON = new Gson();
        final String processingSericeBaseUrl = System.getenv("CAF_WORKFLOW_WORKER_PROCESSING_API_URL");
        Objects.requireNonNull(processingSericeBaseUrl);
        API_CLIENT.setBasePath(processingSericeBaseUrl);
        TENANT_CONFIG_API = new TenantConfigurationApi(API_CLIENT);
        REPOSITORY_CONFIG_API = new RepositoryConfigurationApi(API_CLIENT);
    }

    public static void addCustomWorkflowConfig(final String requiredConfig, final Document document)
        throws ApiException, DocumentWorkerTransientException
    {
        if (Strings.isNullOrEmpty(requiredConfig)) {
            return;
        }
        final String tenantId = document.getCustomData("tenantId");
        Objects.requireNonNull(tenantId);
        final Type type = new TypeToken<Map<String, List<Object>>>(){}.getType();
        final Map<String, List<Object>> requiredConfigMap = GSON.fromJson(requiredConfig, type);
        final List<Object> taskConfigs = requiredConfigMap.get("taskSettings");
        final List<Object> repositoryConfigs = requiredConfigMap.get("repositorySettings");
        final List<Object> tenantConfigs = requiredConfigMap.get("tenantSettings");
        final Map<String, Map<String, String>> settings = new HashMap<>();
        settings.put("task", processTaskConfigs(document, taskConfigs));
        settings.put("repository", processRepositoryConfigs(document, tenantId, repositoryConfigs));
        settings.put("tenant", processTenantConfigs(tenantId, tenantConfigs));
        final Gson gson = new Gson();
        document.getField("CAF_WORKFLOW_SETTINGS").set(gson.toJson(settings));
        document.getTask().getResponse().getCustomData().put("CAF_WORKFLOW_SETTINGS", gson.toJson(settings));
    }

    private static Map<String, String> processTenantConfigs(final String tenantId, final List<Object> configs)
        throws ApiException, DocumentWorkerTransientException
    {
        if (configs == null) {
            return null;
        }
        final Map<String, String> customConfigs = new HashMap<>();
        for (final Object config : configs) {
            final String configKey = (String) config;
            try {
                customConfigs.put(configKey,
                                  SETTINGS_CACHE.get(
                                      new WorkflowSettingsTenantCacheKey(tenantId, configKey)));
            } catch (final ExecutionException ex) {
                if (ex.getCause() instanceof ApiException) {
                    throw (ApiException) ex.getCause();
                } else if (ex.getCause() instanceof DocumentWorkerTransientException) {
                    throw (DocumentWorkerTransientException) ex.getCause();
                } else {
                    throw new RuntimeException(ex);
                }
            }
        }
        return customConfigs;
    }

    private static Map<String, String> processRepositoryConfigs(final Document document, final String tenantId,
                                                                final List<Object> configs) throws ApiException,
                                                                                                   DocumentWorkerTransientException
    {
        final Map<String, String> customConfigs = new HashMap<>();
        String repositoryKey = null;
        for (final Object config : configs) {
            final String configJson = GSON.toJson(config);
            final Type type = new TypeToken<Map<String, RepoConfigSource>>(){}.getType();
            final Map<String, RepoConfigSource> configEntry = GSON.fromJson(configJson, type);
            for (final Map.Entry<String, RepoConfigSource> entry : configEntry.entrySet()) {
                final RepoConfigSource repoConfig = entry.getValue();
                switch (repoConfig.source.toUpperCase(Locale.US)) {
                    case "FIELD":
                        repositoryKey = document.getField(
                            repoConfig.key).getValues().stream().findFirst().get().getStringValue();
                        break;
                    case "CUSTOMDATA":
                        repositoryKey = document.getCustomData(repoConfig.key);
                        if (repositoryKey == null) {
                            throw new RuntimeException("Unable to obtain repository id for config " + repoConfig.key);
                        }
                        break;
                    default:
                        throw new RuntimeException("Invalid source for repository id. Source of "
                            + repoConfig.key + " is not recognised as a valid source.");
                }
                try {
                    customConfigs.put(entry.getKey(),
                                      SETTINGS_CACHE.get(
                                          new WorkflowSettingsRepositoryCacheKey(tenantId, entry.getKey(), repositoryKey)));
                } catch (final ExecutionException ex) {
                    if (ex.getCause() instanceof ApiException) {
                        throw (ApiException) ex.getCause();
                    } else if (ex.getCause() instanceof DocumentWorkerTransientException) {
                        throw (DocumentWorkerTransientException) ex.getCause();
                    } else {
                        throw new RuntimeException(ex);
                    }
                }
            }
        }
        return customConfigs;
    }

    private static Map<String, String> processTaskConfigs(final Document document, final List<Object> configs)
        throws ApiException, DocumentWorkerTransientException
    {
        final Map<String, String> customConfigs = new HashMap<>();
        for (final Object config : configs) {
            final String configKey = (String) config;
            final String configValue = document.getCustomData("TASK_SETTING_" + configKey.toUpperCase(Locale.US));
            customConfigs.put(configKey, configValue);
        }
        return customConfigs;
    }

    /**
     * Returns the value of the tenant's configuration that was requested.
     *
     * @param tenantId A unique string that identifies the tenant.
     * @param key The unique string used to identify a specific configuration.
     * @return The string representation of the value of the configuration requested.
     * @throws ApiException When an error occurs while trying to retrieve a config's value from the processing service.
     * @throws NullPointerException When the tenantId or key passed to the method is null.
     */
    private static String getTenantRepositorySpecificConfigValue(final String tenantId, final String key,
                                                                 final String repositoryId) throws ApiException,
                                                                                                   DocumentWorkerTransientException
    {
        Objects.requireNonNull(tenantId);
        Objects.requireNonNull(key);
        Objects.requireNonNull(repositoryId);
        try {
            final EffectiveRepositoryConfigValue effectiveRepoConfigValue
                = REPOSITORY_CONFIG_API.getEffectiveRepositoryConfig(tenantId, repositoryId, key);
            LOG.debug("Retrieved value for repository configuration using key: {}", key);
            LOG.debug("Retrieved value for repository configuration is of type: {}", effectiveRepoConfigValue.getValueType());
            // escape the config value in case it has characters that would cause issues in JavaScript
            return escapeForJavaScript(effectiveRepoConfigValue.getValue());
        } catch (final ApiException ex) {
            if (ex.getCode() == 404) {
                LOG.error("Unable to obtain repository configuration from processing service for tenant: {} using key: {} as no "
                    + "configuration could be found match the provided key", tenantId, key);
                throw ex;
            }
            LOG.error("Unable to obtain repository configuration from processing service for tenant: {} using key: {}", tenantId, key);
            throw ex;
        } catch (final ClientHandlerException ex) {
            LOG.error("Unable to obtain repository configuration from processing service for tenant: {} using key: {}", tenantId, key);
            throw new DocumentWorkerTransientException("Unable to contact processing service to retrieve repository configs.");
        }
    }

    /**
     * Returns the value of the tenant's configuration that was requested.
     *
     * @param tenantId A unique string that identifies the tenant.
     * @param key The unique string used to identify a specific configuration.
     * @return The string representation of the value of the configuration requested.
     * @throws ApiException When an error occurs while trying to retrieve a config's value from the processing service.
     * @throws NullPointerException When the tenantId or key passed to the method is null.
     */
    public static String getTenantSpecificConfigValue(final String tenantId, final String key)
        throws ApiException, DocumentWorkerTransientException
    {
        Objects.requireNonNull(tenantId);
        Objects.requireNonNull(key);
        try {
            final EffectiveTenantConfigValue effectiveRepoConfigValue
                = TENANT_CONFIG_API.getEffectiveTenantConfig(tenantId, key);
            LOG.debug("Retrieved value for tenant configuration using key: {}", key);
            LOG.debug("Retrieved value for tenant configuration is of type: {}", effectiveRepoConfigValue.getValueType());
            // escape the config value in case it has characters that would cause issues in JavaScript
            return escapeForJavaScript(effectiveRepoConfigValue.getValue());
        } catch (final ApiException ex) {
            if (ex.getCode() == 404) {
                LOG.error("Unable to obtain tenant configuration from processing service for tenant: {} using key: {} as no "
                    + "configuration could be found match the provided key", tenantId, key);
                throw ex;
            }
            LOG.error("Unable to obtain tenant configuration from processing service for tenant: {} using key: {}", tenantId, key);
            throw ex;
        } catch (final ClientHandlerException ex) {
            LOG.error("Unable to obtain tenant configuration from processing service for tenant: {} using key: {}", tenantId, key);
            throw new DocumentWorkerTransientException("Unable to contact processing service to retrieve tenant configs.");
        }
    }

    private static String getSettingsFromServer(final WorkflowSettingsCacheKey key) throws ApiException, DocumentWorkerTransientException
    {
        if (key instanceof WorkflowSettingsRepositoryCacheKey) {
            final WorkflowSettingsRepositoryCacheKey cacheKey = (WorkflowSettingsRepositoryCacheKey) key;
            if (cacheKey.getSettingKey() == null) {
                throw new RuntimeException("Can't look up value for null setting key.");
            }
            return getTenantRepositorySpecificConfigValue(cacheKey.getTenantId(),
                                                          cacheKey.getSettingKey(),
                                                          cacheKey.getRepositoryId());
        } else if (key instanceof WorkflowSettingsTenantCacheKey) {
            final WorkflowSettingsTenantCacheKey cacheKey = (WorkflowSettingsTenantCacheKey) key;
            if (cacheKey.getSettingKey() == null) {
                throw new RuntimeException("Can't look up value for null setting key.");
            }
            return getTenantSpecificConfigValue(cacheKey.getTenantId(), cacheKey.getSettingKey());
        } else {
            throw new RuntimeException("Workflow settings cache key type not recognised."
                + " Only supposed cache keys are of types WorkflowSettingsRepositoryCacheKey and WorkflowSettingsTenantCacheKey.");
        }
    }

    private static final class RepoConfigSource
    {
        public String source;
        public String key;
    }

}
