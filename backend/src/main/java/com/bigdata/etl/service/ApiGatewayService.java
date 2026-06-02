package com.bigdata.etl.service;

import com.bigdata.etl.model.entity.*;
import java.util.List;

public interface ApiGatewayService {
    // API Management
    List<ApiDefinition> listApis(String status);
    ApiDefinition getApi(Long id);
    ApiDefinition createApi(ApiDefinition api);
    void updateApi(ApiDefinition api);
    void deleteApi(Long id);
    void publishApi(Long id);
    void deprecateApi(Long id);

    // API Key Management
    List<ApiKey> listKeys();
    ApiKey createKey(ApiKey key);
    void revokeKey(Long id);
    boolean validateKey(String keyValue);

    // API Call Logging
    List<ApiCallLog> listCallLogs(Long apiId, int limit);
    void logCall(ApiCallLog log);
}
