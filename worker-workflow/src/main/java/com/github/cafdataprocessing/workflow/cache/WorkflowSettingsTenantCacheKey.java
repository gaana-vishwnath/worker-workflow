/*
 * Copyright 2018 EntIT Software LLC, a Micro Focus company.
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

public final class WorkflowSettingsTenantCacheKey implements WorkflowSettingsCacheKey
{
    private final String tenantId;
    private final String settingKey;

    public WorkflowSettingsTenantCacheKey(final String tenantId, final String settingKey)
    {
        this.tenantId = tenantId;
        this.settingKey = settingKey;
    }

    /**
     * @return the tenantId
     */
    public String getTenantId()
    {
        return tenantId;
    }

    /**
     * @return the settingKey
     */
    public String getSettingKey()
    {
        return settingKey;
    }

    @Override
    public boolean equals(final Object o)
    {
        if (this == o) {
            return true;
        }

        if (!(o instanceof WorkflowSettingsTenantCacheKey)) {
            return false;
        }

        final WorkflowSettingsTenantCacheKey cacheKeyToCheck = (WorkflowSettingsTenantCacheKey) o;
        return this.tenantId.equals(cacheKeyToCheck.getTenantId())
            && this.settingKey.equals(cacheKeyToCheck.getSettingKey());
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(this.tenantId, this.settingKey);
    }

}
