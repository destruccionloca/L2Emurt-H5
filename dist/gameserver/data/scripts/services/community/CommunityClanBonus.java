package services.community;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import l2p.commons.dbutils.DbUtils;
import l2p.gameserver.Config;
import l2p.gameserver.database.DatabaseFactory;
import l2p.gameserver.handler.bbs.CommunityBoardManager;
import l2p.gameserver.handler.bbs.ICommunityBoardHandler;
import l2p.gameserver.model.Player;
import l2p.gameserver.model.pledge.Clan;
import l2p.gameserver.scripts.Functions;
import l2p.gameserver.scripts.ScriptFile;

/**
 *
 * @author dislike
 * @editor JunkyFunky
 */
public class CommunityClanBonus extends Functions implements ScriptFile, ICommunityBoardHandler
{
    
    @Override
    public void onLoad() {
            CommunityBoardManager.getInstance().registerHandler(this);
    }

    @Override
    public void onReload() {
            CommunityBoardManager.getInstance().removeHandler(this);
    }

    @Override
    public void onShutdown() {

    }

    @Override
    public String[] getBypassCommands() {
        return new String[] {"_bbsclanbonus"};
    }

    @Override
    public void onBypassCommand(Player player, String bypass) {
        if(bypass.startsWith("_bbsclanbonus"))
        {
            if(checkCondition(player))
                if(addReward(player.getClan()))
                {
                    deletRecord(player.getClan());
                    player.sendMessage(player.isLangRus() ? "Вас нет в списках  или вы уже получили бонус" : "You are not in the bonus list");
                }
        }
    }
    
    private boolean checkCondition(Player player)
    {
        if(player == null)
            return false;
        
        Clan clan  = player.getClan();
        
        if(clan == null)
        {
            player.sendMessage(player.isLangRus() ? "Вы не состоите в клане." : "You are not in a clan.");
            return false;
        }
        
        if(!player.isClanLeader())
        {
            player.sendMessage(player.isLangRus() ? "Вы не являетесь главой клана." : "You are not the head of the clan.");
            return false;
        }
        
        Connection con = null;
        PreparedStatement statement = null;
        ResultSet rset = null;
        List<String> names = new ArrayList<String>();
        try
		{
            con = DatabaseFactory.getInstance().getConnection();
            statement = con.prepareStatement("SELECT name FROM bbs_clanbonus");
            rset = statement.executeQuery();
            while(rset.next())
				names.add(rset.getString("name"));
		}
		catch (SQLException e)
		{
            e.printStackTrace();
            return false;
		}
		finally
		{
            DbUtils.closeQuietly(con, statement, rset);
		}
        
        if(!names.contains(clan.getName()))
        {
            player.sendMessage(player.isLangRus() ? "Вашего клана нет в списке." : "Your clan is not listed.");
            sendMessage("Вашего клана нет в списке.",player);
            return false;
        }
        
        if(!checkPlayersCount(clan))
        {
            player.sendMessage(player.isLangRus() ? "Условия не соблюдены." : "The conditions are not met.");
            return false;
        }
        
        return true;
        
    }
    
    private boolean checkPlayersCount(Clan clan)
    {
        Map<String, String> ips = new HashMap<String, String>();
        //Map<HardwareID, HardwareID> hwids = new HashMap<HardwareID, HardwareID>();
        int realOnline = clan.getOnlineMembers(0).size();
        int need = 0;
        //проверка на твинков
        for(Player p : clan.getOnlineMembers(0)){
            ips.put(p.getIP(), p.getIP());
            //hwids.put(p.getHWID(), p.getHWID());
		}
        if(realOnline > ips.size())// || realOnline > hwids.size())
            return false;
        
        Connection con = null;
		PreparedStatement statement = null;
		ResultSet rset = null;
		try
		{
            con = DatabaseFactory.getInstance().getConnection();
            statement = con.prepareStatement("SELECT players_count FROM bbs_clanbonus WHERE name=? LIMIT 1");
            statement.setString(1, clan.getName());
            rset = statement.executeQuery();
            if(rset.next())
            {
                need = rset.getInt("players_count");

            }
		}
		catch(SQLException e)
		{
            e.printStackTrace();
            return false;
		}
		finally
		{
            DbUtils.closeQuietly(con, statement, rset);
		}
        
        if(need > realOnline)
            return false;
        
        return true;
        
    }
    
    private boolean addReward(Clan clan)
    {     
        Connection con = null;
		PreparedStatement statement = null;
		ResultSet rset = null;
        
        int lvl = 0,rep = 0;
		int itemId = 0,count = 0;
		
        //Clan clan = player.getClan();
    
		try
		{
            con = DatabaseFactory.getInstance().getConnection();
            statement = con.prepareStatement("SELECT reward_lvl, reward_rep, reward_item, reward_count FROM bbs_clanbonus WHERE name=? LIMIT 1");
            statement.setString(1, clan.getName());
            rset = statement.executeQuery();
            if(rset.next())
            {
                lvl = rset.getInt("reward_lvl");
                rep = rset.getInt("reward_rep");
				itemId = rset.getInt("reward_item");
				count = rset.getInt("reward_count");
            }
		}
		catch(SQLException e)
		{
            e.printStackTrace();
            return false;
		}
		finally
		{
            DbUtils.closeQuietly(con, statement, rset);
		}
        
        if(lvl > clan.getLevel())
            clan.setLevel(lvl);
        clan.incReputation(rep);
        clan.broadcastClanStatus(true, true, false);
        for (Player clanMember : clan.getOnlineMembers(0))
        {
            if (clanMember.isClanLeader() && itemId > 0)
            {
                clanMember.getInventory().addItem(itemId, count);
            }
			
        }
        return true;
	}
    
    private void deletRecord(Clan clan)
    {
        Connection con = null;
		PreparedStatement statement = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("DELETE FROM bbs_clanbonus WHERE name = ?");
			statement.setString(1, clan.getName());
			statement.execute();
		}
		catch(SQLException e)
		{
			e.printStackTrace();
		}
		finally
		{
			DbUtils.closeQuietly(con, statement);
		}
    }
    
    @Override
    public void onWriteCommand(Player player, String bypass, String arg1, String arg2, String arg3, String arg4, String arg5) {
        
    }
    
}
