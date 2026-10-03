package com.medclinic.security;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

import java.sql.Timestamp;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

@Component
@ConditionalOnProperty(name = "app.security.rate-limit.store", havingValue = "jdbc")
public class JdbcSqlServerRateLimitStore implements RateLimitStore {
    private final JdbcTemplate jdbc;
    private final TransactionTemplate transactions;
    private final AtomicLong operations = new AtomicLong();

    public JdbcSqlServerRateLimitStore(JdbcTemplate jdbc, PlatformTransactionManager transactionManager) {
        this.jdbc = jdbc;
        this.transactions = new TransactionTemplate(transactionManager);
        this.transactions.setIsolationLevel(TransactionDefinition.ISOLATION_SERIALIZABLE);
    }

    @Override
    public Decision increment(String bucketKey, long nowMillis, long windowMillis, int limit) {
        return transactions.execute(status -> incrementLocked(bucketKey, nowMillis, windowMillis, limit));
    }

    private Decision incrementLocked(String bucketKey, long nowMillis, long windowMillis, int limit) {
        Timestamp now = new Timestamp(nowMillis);
        List<WindowCounter> rows = jdbc.query("""
                        select window_start, request_count
                        from security_rate_limit_buckets with (updlock, holdlock)
                        where bucket_key = ?
                        """, (rs, rowNum) -> new WindowCounter(
                        rs.getTimestamp("window_start").getTime(),
                        rs.getInt("request_count")), bucketKey);

        long windowStart;
        int count;
        if (rows.isEmpty()) {
            jdbc.update("insert into security_rate_limit_buckets(bucket_key, window_start, request_count) values (?, ?, 1)",
                    bucketKey, now);
            windowStart = nowMillis;
            count = 1;
        } else {
            WindowCounter current = rows.getFirst();
            if (nowMillis - current.windowStartMillis() >= windowMillis) {
                jdbc.update("update security_rate_limit_buckets set window_start = ?, request_count = 1 where bucket_key = ?",
                        now, bucketKey);
                windowStart = nowMillis;
                count = 1;
            } else {
                count = current.count() + 1;
                jdbc.update("update security_rate_limit_buckets set request_count = ? where bucket_key = ?",
                        count, bucketKey);
                windowStart = current.windowStartMillis();
            }
        }

        if (operations.incrementAndGet() % 1000 == 0) {
            jdbc.update("delete from security_rate_limit_buckets where window_start < ?",
                    new Timestamp(nowMillis - windowMillis * 2));
        }
        long retry = Math.max(1L, (windowStart + windowMillis - nowMillis + 999L) / 1000L);
        return new Decision(count <= limit, retry);
    }

    private record WindowCounter(long windowStartMillis, int count) {
    }
}
