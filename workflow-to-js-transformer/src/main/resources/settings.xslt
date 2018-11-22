<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:sxl="http://www.w3.org/1999/XSL/Transform" xmlns:xslt="http://www.w3.org/1999/XSL/Transform"
                xmlns:workflow_transform="com.github.cafdataprocessing.workflow.transform"
                xmlns:xls="http://www.w3.org/1999/XSL/Transform">
    <xsl:output method="text" omit-xml-declaration="yes" indent="no"/>
    <xsl:template match="/">
        {
        <xls:call-template name="requiredTaskSettings"></xls:call-template>
        <xls:call-template name="requiredRepositorySettings"></xls:call-template>
        }                
    </xsl:template>
    <xsl:template name="requiredTaskSettings">"taskSettings": [<xsl:for-each select="//taskSettings">"<xsl:value-of select="key"/>"<xsl:if test="position() != last()">, </xsl:if></xsl:for-each>],
        </xsl:template>
    <xsl:template name="requiredRepositorySettings">"repositorySettings": [<xsl:for-each select="//repositorySettings">{"<xsl:value-of select="key"/>": {"source": "<xsl:value-of select="repositoryId/source"/>", "key": "<xsl:value-of select="repositoryId/key"/>"}<xsl:if test="position() != last()">, </xsl:if>}</xsl:for-each>]
    </xsl:template>
    <xsl:template name="requiredTenantSettings">"tenantSettings": [<xsl:for-each select="//tenantSettings">"<xsl:value-of select="key"/>"<xsl:if test="position() != last()">, </xsl:if></xsl:for-each>]</xsl:template>
</xsl:stylesheet>
