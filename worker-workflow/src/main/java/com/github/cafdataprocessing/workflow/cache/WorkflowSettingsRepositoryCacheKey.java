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
package com.github.cafdataprocessing.workflow.cache;

import java.util.Objects;

public final class WorkflowSettingsRepositoryCacheKey implements WorkflowSettingsCacheKey
{
    private String tenantId;
    private String settingKey;
    private final String repositoryId;

    public WorkflowSettingsRepositoryCacheKey(final String tenantId, final String settingKey, final String repositoryId)
    {
        this.tenantId = tenantId;
        this.settingKey = settingKey;
        this.repositoryId = repositoryId;
    }

    /**
     * @return the repositoryId
     */
    public String getRepositoryId()
    {
        return repositoryId;
    }

    /**
     * @return the tenantId
     */
    public String getTenantId()
    {
        return tenantId;
    }

    /**
     * @param tenantId the tenantId to set
     */
    public void setTenantId(String tenantId)
    {
        this.tenantId = tenantId;
    }

    /**
     * @return the settingKey
     */
    public String getSettingKey()
    {
        return settingKey;
    }

    /**
     * @param settingKey the settingKey to set
     */
    public void setSettingKey(String settingKey)
    {
        this.settingKey = settingKey;
    }

    @Override
    public boolean equals(final Object o)
    {
        if (this == o) {
            return true;
        }

        if (!(o instanceof WorkflowSettingsRepositoryCacheKey)) {
            return false;
        }

        final WorkflowSettingsRepositoryCacheKey cacheKeyToCheck = (WorkflowSettingsRepositoryCacheKey) o;
        return this.getTenantId().equals(cacheKeyToCheck.getTenantId())
            && this.getSettingKey().equals(cacheKeyToCheck.getSettingKey())
            && this.repositoryId.equals(cacheKeyToCheck.repositoryId);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(this.getTenantId(), this.getSettingKey(), this.repositoryId);
    }

}
