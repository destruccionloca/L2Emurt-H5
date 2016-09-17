package l2p.commons.dao;

import java.io.Serializable;

public interface JdbcEntity extends Serializable {

    void setJdbcState(JdbcEntityState state);

    JdbcEntityState getJdbcState();

    void save();

    void update();

    void delete();
}
