package l2p.gameserver.utils.simpleAcp;

import l2p.commons.dbutils.DbUtils;
import l2p.gameserver.database.DatabaseFactory;
import l2p.gameserver.model.Player;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class AcpDAO {
    private static final Logger _log = LoggerFactory.getLogger(AcpDAO.class);

    public static void saveAcpData(Player player, PotionModel potion)
    {
        if(player == null || potion == null)
            return;

        Connection con = null;
        PreparedStatement statement = null;
        try
        {
            con = DatabaseFactory.getInstance().getConnection();
            statement = con.prepareStatement("REPLACE INTO character_acp_info (obj_id, potion, min, max, status) VALUES (?,?,?,?,?)");
            statement.setInt(1, player.getObjectId());
            statement.setInt(2, potion.getItemId());
            statement.setInt(3, potion.getMinPercent());
            statement.setInt(4, potion.getMaxPercent());
            statement.setString(5, potion.isAllow() ? "on" : "off");
            statement.execute();
        }
        catch(final Exception e)
        {
            _log.warn("AcpDAO Error! saveAcpData: name (" + potion.getItemId() + "), value (" + potion.getMinPercent() + " "
                    + potion.getMaxPercent() + " " + potion.isAllow() + ")", e);
        }
        finally
        {
            DbUtils.closeQuietly(con, statement);
        }
    }

    public static void loadAcpData(Player player)
    {
        Connection con = null;
        PreparedStatement offline = null;
        ResultSet rs = null;
        try
        {
            con = DatabaseFactory.getInstance().getConnection();
            offline = con.prepareStatement("SELECT * FROM character_acp_info WHERE obj_id = ?");
            offline.setInt(1, player.getObjectId());
            rs = offline.executeQuery();
            while (rs.next())
            {
                PotionModel potion = player.getAcp().getPotionByItemId(rs.getInt("potion"));
                if(potion != null) {
                    potion.setMinPercent(rs.getInt("min"));
                    potion.setMaxPercent(rs.getInt("max"));
                    potion.setAllow(rs.getString("status").equals("on"));
                }
                else {
                    //TODO: delete record
                }
            }
        }
        catch(final Exception e)
        {
            _log.warn("AcpDAO.loadAcpData(): error couldn't load vars:", e);
        }
        finally
        {
            DbUtils.closeQuietly(con, offline, rs);
        }
    }

}
