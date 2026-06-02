package com.bigdata.etl.engine;

import lombok.Data;
import java.util.List;
import java.util.Map;

@Data
public class QueryResult {
    private List<String> columns;
    private List<Map<String, Object>> rows;
    private long rowCount;
    private long elapsedMs;
    private String error;

    public static QueryResult success(List<String> columns, List<Map<String, Object>> rows, long elapsedMs) {
        QueryResult r = new QueryResult();
        r.columns = columns;
        r.rows = rows;
        r.rowCount = rows.size();
        r.elapsedMs = elapsedMs;
        return r;
    }

    public static QueryResult error(String error) {
        QueryResult r = new QueryResult();
        r.error = error;
        return r;
    }
}
