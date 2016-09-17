package l2p.commons.dao;

public interface JdbcEntityStats {

    long getLoadCount();

    long getInsertCount();

    long getUpdateCount();

    long getDeleteCount();
}
