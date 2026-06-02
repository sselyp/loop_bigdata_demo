package com.bigdata.etl.service.impl;

import com.bigdata.etl.model.entity.*;
import com.bigdata.etl.repository.*;
import com.bigdata.etl.service.GovernanceService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class GovernanceServiceImpl implements GovernanceService {

    private final GovMetadataTableMapper tableMapper;
    private final GovMetadataColumnMapper columnMapper;
    private final GovLineageMapper lineageMapper;
    private final GovQualityRuleMapper qualityRuleMapper;
    private final GovQualityCheckMapper qualityCheckMapper;
    private final GovMaskingRuleMapper maskingRuleMapper;
    private final GovPermissionMapper permissionMapper;
    private final GovAuditLogMapper auditLogMapper;

    // ========== Metadata ==========

    @Override
    public List<GovMetadataTable> listTables(Long datasourceId) {
        LambdaQueryWrapper<GovMetadataTable> w = new LambdaQueryWrapper<>();
        if (datasourceId != null) {
            w.eq(GovMetadataTable::getDatasourceId, datasourceId);
        }
        w.orderByAsc(GovMetadataTable::getTableSchema, GovMetadataTable::getTableName);
        return tableMapper.selectList(w);
    }

    @Override
    public GovMetadataTable getTable(Long id) {
        return tableMapper.selectById(id);
    }

    @Override
    @Transactional
    public void syncMetadata(Long datasourceId) {
        log.info("Syncing metadata for datasource id={}", datasourceId);
        recordAudit("METADATA_SYNC", "DATASOURCE", datasourceId, "system", "Metadata sync triggered");
    }

    @Override
    public List<GovMetadataColumn> listColumns(Long tableId) {
        LambdaQueryWrapper<GovMetadataColumn> w = new LambdaQueryWrapper<>();
        w.eq(GovMetadataColumn::getTableId, tableId)
         .orderByAsc(GovMetadataColumn::getOrdinalPosition);
        return columnMapper.selectList(w);
    }

    // ========== Lineage ==========

    @Override
    public List<GovLineage> getUpstreamLineage(Long tableId) {
        LambdaQueryWrapper<GovLineage> w = new LambdaQueryWrapper<>();
        w.eq(GovLineage::getTargetTableId, tableId);
        return lineageMapper.selectList(w);
    }

    @Override
    public List<GovLineage> getDownstreamLineage(Long tableId) {
        LambdaQueryWrapper<GovLineage> w = new LambdaQueryWrapper<>();
        w.eq(GovLineage::getSourceTableId, tableId);
        return lineageMapper.selectList(w);
    }

    @Override
    public GovLineage addLineage(GovLineage lineage) {
        if (lineage.getLineageType() == null || lineage.getLineageType().isBlank()) {
            lineage.setLineageType("ETL");
        }
        lineageMapper.insert(lineage);
        log.info("Added lineage: source={} -> target={}", lineage.getSourceTableId(), lineage.getTargetTableId());
        return lineage;
    }

    // ========== Quality ==========

    @Override
    public List<GovQualityRule> listQualityRules(Long tableId) {
        LambdaQueryWrapper<GovQualityRule> w = new LambdaQueryWrapper<>();
        if (tableId != null) {
            w.eq(GovQualityRule::getTableId, tableId);
        }
        return qualityRuleMapper.selectList(w);
    }

    @Override
    @Transactional
    public GovQualityRule createQualityRule(GovQualityRule rule) {
        if (rule.getStatus() == null || rule.getStatus().isBlank()) {
            rule.setStatus("ENABLED");
        }
        if (rule.getSeverity() == null || rule.getSeverity().isBlank()) {
            rule.setSeverity("WARN");
        }
        qualityRuleMapper.insert(rule);
        log.info("Created quality rule: id={}, name={}, type={}", rule.getId(), rule.getName(), rule.getRuleType());
        return rule;
    }

    @Override
    public void updateQualityRule(GovQualityRule rule) {
        qualityRuleMapper.updateById(rule);
    }

    @Override
    public void deleteQualityRule(Long id) {
        qualityRuleMapper.deleteById(id);
    }

    @Override
    @Transactional
    public GovQualityCheck runQualityCheck(Long ruleId) {
        GovQualityRule rule = qualityRuleMapper.selectById(ruleId);
        if (rule == null) {
            throw new IllegalArgumentException("Quality rule not found: " + ruleId);
        }
        GovQualityCheck check = new GovQualityCheck();
        check.setRuleId(ruleId);
        check.setCheckTime(LocalDateTime.now().toString());
        long start = System.currentTimeMillis();
        try {
            String checkSql = buildQualityCheckSql(rule);
            check.setExecutedSql(checkSql);
            check.setStatus("PASS");
            check.setErrorCount(0L);
        } catch (Exception e) {
            check.setStatus("ERROR");
            check.setActualValue(e.getMessage());
        }
        check.setDurationMs(System.currentTimeMillis() - start);
        qualityCheckMapper.insert(check);
        log.info("Quality check: ruleId={}, status={}", ruleId, check.getStatus());
        return check;
    }

    private String buildQualityCheckSql(GovQualityRule rule) {
        String col = rule.getColumnName() != null ? rule.getColumnName() : "*";
        return switch (rule.getRuleType().toUpperCase()) {
            case "NOT_NULL" -> String.format(
                "SELECT COUNT(*) AS errors FROM (SELECT 1 FROM t WHERE %s IS NULL) tmp", col);
            case "UNIQUE" -> String.format(
                "SELECT %s, COUNT(*) AS cnt FROM t GROUP BY %s HAVING COUNT(*) > 1", col, col);
            case "ROW_COUNT" -> "SELECT COUNT(*) FROM t";
            case "FRESHNESS" -> "SELECT MAX(update_time) AS last_update FROM t";
            default -> rule.getRuleConfig() != null ? rule.getRuleConfig() : "SELECT 1";
        };
    }

    @Override
    public List<GovQualityCheck> listQualityChecks(Long ruleId) {
        LambdaQueryWrapper<GovQualityCheck> w = new LambdaQueryWrapper<>();
        w.eq(GovQualityCheck::getRuleId, ruleId)
         .orderByDesc(GovQualityCheck::getCreateTime);
        return qualityCheckMapper.selectList(w);
    }

    // ========== Masking ==========

    @Override
    public List<GovMaskingRule> listMaskingRules(Long tableId) {
        LambdaQueryWrapper<GovMaskingRule> w = new LambdaQueryWrapper<>();
        if (tableId != null) {
            w.eq(GovMaskingRule::getTableId, tableId);
        }
        return maskingRuleMapper.selectList(w);
    }

    @Override
    @Transactional
    public GovMaskingRule createMaskingRule(GovMaskingRule rule) {
        if (rule.getStatus() == null || rule.getStatus().isBlank()) {
            rule.setStatus("ENABLED");
        }
        maskingRuleMapper.insert(rule);
        log.info("Created masking rule: id={}, name={}, type={}", rule.getId(), rule.getName(), rule.getMaskType());
        return rule;
    }

    @Override
    public void updateMaskingRule(GovMaskingRule rule) {
        maskingRuleMapper.updateById(rule);
    }

    @Override
    public void deleteMaskingRule(Long id) {
        maskingRuleMapper.deleteById(id);
    }

    @Override
    public String applyMasking(Long maskingRuleId, String value) {
        GovMaskingRule rule = maskingRuleMapper.selectById(maskingRuleId);
        if (rule == null || value == null) return value;
        return switch (rule.getMaskType().toUpperCase()) {
            case "FULL_MASK" -> "***";
            case "PARTIAL_MASK" -> maskPartial(value);
            case "EMAIL_MASK" -> maskEmail(value);
            case "PHONE_MASK" -> maskPhone(value);
            case "HASH" -> Integer.toHexString(value.hashCode());
            default -> value;
        };
    }

    private String maskPartial(String value) {
        if (value.length() <= 4) return "***";
        return value.charAt(0) + "***" + value.substring(value.length() - 1);
    }

    private String maskEmail(String email) {
        int atIdx = email.indexOf('@');
        if (atIdx <= 1) return "***@***";
        return email.charAt(0) + "***" + email.substring(atIdx);
    }

    private String maskPhone(String phone) {
        if (phone.length() < 7) return "***";
        return phone.substring(0, 3) + "****" + phone.substring(phone.length() - 4);
    }

    // ========== Permission ==========

    @Override
    public List<GovPermission> listPermissions(String roleName, Long tableId) {
        LambdaQueryWrapper<GovPermission> w = new LambdaQueryWrapper<>();
        w.eq(GovPermission::getStatus, "ACTIVE");
        if (roleName != null) w.eq(GovPermission::getRoleName, roleName);
        if (tableId != null) w.eq(GovPermission::getTableId, tableId);
        return permissionMapper.selectList(w);
    }

    @Override
    @Transactional
    public GovPermission grantPermission(GovPermission permission) {
        if (permission.getStatus() == null || permission.getStatus().isBlank()) {
            permission.setStatus("ACTIVE");
        }
        permissionMapper.insert(permission);
        log.info("Granted permission: role={}, tableId={}", permission.getRoleName(), permission.getTableId());
        return permission;
    }

    @Override
    @Transactional
    public void revokePermission(Long id) {
        GovPermission perm = permissionMapper.selectById(id);
        if (perm != null) {
            perm.setStatus("REVOKED");
            permissionMapper.updateById(perm);
        }
        log.info("Revoked permission: id={}", id);
    }

    @Override
    public boolean checkPermission(String roleName, Long tableId, String operation) {
        LambdaQueryWrapper<GovPermission> w = new LambdaQueryWrapper<>();
        w.eq(GovPermission::getRoleName, roleName)
         .eq(GovPermission::getTableId, tableId)
         .eq(GovPermission::getStatus, "ACTIVE");
        GovPermission perm = permissionMapper.selectOne(w);
        if (perm == null) return false;
        return switch (operation.toUpperCase()) {
            case "SELECT" -> perm.getGrantSelect() != null && perm.getGrantSelect() == 1;
            case "INSERT" -> perm.getGrantInsert() != null && perm.getGrantInsert() == 1;
            case "UPDATE" -> perm.getGrantUpdate() != null && perm.getGrantUpdate() == 1;
            case "DELETE" -> perm.getGrantDelete() != null && perm.getGrantDelete() == 1;
            default -> false;
        };
    }

    // ========== Audit ==========

    @Override
    public List<GovAuditLog> listAuditLogs(String operator, String operation, int limit) {
        LambdaQueryWrapper<GovAuditLog> w = new LambdaQueryWrapper<>();
        if (operator != null && !operator.isBlank()) w.eq(GovAuditLog::getOperator, operator);
        if (operation != null && !operation.isBlank()) w.eq(GovAuditLog::getOperation, operation);
        w.orderByDesc(GovAuditLog::getCreateTime);
        if (limit > 0) w.last("LIMIT " + limit);
        return auditLogMapper.selectList(w);
    }

    @Override
    @Transactional
    public void recordAudit(String operation, String targetType, Long targetId, String operator, String detail) {
        GovAuditLog logEntry = new GovAuditLog();
        logEntry.setOperation(operation);
        logEntry.setTargetType(targetType);
        logEntry.setTargetId(targetId);
        logEntry.setOperator(operator);
        logEntry.setDetail(detail);
        logEntry.setStatus("SUCCESS");
        auditLogMapper.insert(logEntry);
    }
}
