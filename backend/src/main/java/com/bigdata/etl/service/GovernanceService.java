package com.bigdata.etl.service;

import com.bigdata.etl.model.entity.*;
import java.util.List;

public interface GovernanceService {
    // Metadata
    List<GovMetadataTable> listTables(Long datasourceId);
    GovMetadataTable getTable(Long id);
    void syncMetadata(Long datasourceId);
    List<GovMetadataColumn> listColumns(Long tableId);

    // Lineage
    List<GovLineage> getUpstreamLineage(Long tableId);
    List<GovLineage> getDownstreamLineage(Long tableId);
    GovLineage addLineage(GovLineage lineage);

    // Quality
    List<GovQualityRule> listQualityRules(Long tableId);
    GovQualityRule createQualityRule(GovQualityRule rule);
    void updateQualityRule(GovQualityRule rule);
    void deleteQualityRule(Long id);
    GovQualityCheck runQualityCheck(Long ruleId);
    List<GovQualityCheck> listQualityChecks(Long ruleId);

    // Masking
    List<GovMaskingRule> listMaskingRules(Long tableId);
    GovMaskingRule createMaskingRule(GovMaskingRule rule);
    void updateMaskingRule(GovMaskingRule rule);
    void deleteMaskingRule(Long id);
    String applyMasking(Long maskingRuleId, String value);

    // Permission
    List<GovPermission> listPermissions(String roleName, Long tableId);
    GovPermission grantPermission(GovPermission permission);
    void revokePermission(Long id);
    boolean checkPermission(String roleName, Long tableId, String operation);

    // Audit
    List<GovAuditLog> listAuditLogs(String operator, String operation, int limit);
    void recordAudit(String operation, String targetType, Long targetId, String operator, String detail);
}
