package me.drunkenmeows.simpleredstonetp;

import java.util.HashMap;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;

import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;

public class srtpListener implements Listener {
	private final SimpleRedstoneTP plugin;
	private int cost = 0;
	private int dist = 0;
	private int dist_cost = 0;
	private long combat_timeout = 0;
	private long combat_difference = 0;
	private String prefix;
	private float distance = 0;
	
	private HashMap<String, Long> players = new HashMap<String, Long>(); 
	
	//constructor
	public srtpListener(SimpleRedstoneTP plugin)	{
		this.plugin = plugin;
		this.prefix = plugin.getConfig().getString("Messages.Prefix","&f[&csrTP&f]");
	}
	
	public String parse(String str)
	{
		str = str.replaceAll("%t", ""+(int) ((combat_timeout-combat_difference)/20));
		str = str.replaceAll("%c", ""+dist_cost);
		str = str.replaceAll("%d", ""+distance);
		return str;
	}
	
	@EventHandler
	public void onPlayerDamage(EntityDamageByEntityEvent e)
	{
		if(e.getDamager() instanceof Player && e.getEntity() instanceof Player)
		{
			Player p = (Player)e.getDamager();
			Player v = (Player)e.getEntity();
			World w = p.getWorld();
			
			players.put(v.getName(), w.getFullTime());					
		}
	}
	
	@EventHandler
	public void onPlayerLeftClick(PlayerInteractEvent e)
	{
		Action eAction = e.getAction();
		String itemtype = e.getPlayer().getItemInHand().getType().name();
		Player p = e.getPlayer();
		World w = p.getWorld();
		long current_ticks = w.getFullTime();
		long attacked_ticks = 0;
		
		if(players.get(p.getName()) != null)
			attacked_ticks = players.get(p.getName()); 
		
		combat_difference = current_ticks - attacked_ticks;
		
		//if players doesn't have the permission, return.
		if(!(p.hasPermission("srtp.player")))
				return;
		
		cost = plugin.getConfig().getInt("cost");
		boolean disabled = plugin.srtptoggle.contains(p.getName());
		dist = plugin.getConfig().getInt("distance");
		combat_timeout = plugin.getConfig().getInt("combattimeout") * 20;
				
		if(itemtype.equals("REDSTONE") && !disabled )
		{			
			if (eAction == Action.LEFT_CLICK_AIR)
			{
				if(combat_difference > combat_timeout) {
					List<Block> blocklist = p.getLineOfSight(null, dist);				
					int size = blocklist.size()-1;
					Block target = blocklist.get(size);
					
					Block above = target.getRelative(BlockFace.UP);
					
					if(target.getType() != Material.AIR && target.getType() != Material.BEDROCK && target.getType() != Material.STATIONARY_WATER && target.getType() != Material.LAVA)
					{
						float divisor = (float)dist / (float)cost;
						
						Location cur = p.getLocation();
						Location loc = target.getLocation().add(0.5,1.0,0.5);
						loc.setPitch(p.getLocation().getPitch());
						loc.setYaw(p.getLocation().getYaw());
						
						distance = (int)cur.distance(loc);
						
						dist_cost = Math.round(distance / divisor);		
						
						//always have a cost
						if(dist_cost == 0)
							dist_cost = 1;
											
						if(p.getItemInHand().getAmount() >= dist_cost) {
							//updste player inventory
							if(above.getType() == Material.AIR ) 
							{
								if((p.getItemInHand().getAmount()-dist_cost) > 0)
									p.getItemInHand().setAmount(p.getItemInHand().getAmount()-dist_cost);
								else
									p.getInventory().clear(p.getInventory().getHeldItemSlot());

								//at current location set off a firework
								w.spawnEntity(cur, EntityType.FIREWORK);
								//move player to new location
								p.teleport(loc);						
								//at new location show flame effects
								plugin.fireworkEffect(p, Color.RED, FireworkEffect.Type.BURST, 0, false);
								//p.playEffect(loc,Effect.MOBSPAWNER_FLAMES,0);
								
							} else {
								p.sendMessage(ChatColor.translateAlternateColorCodes('&',prefix+parse(plugin.getConfig().getString("Messages.NoSpace","&2Sorry, Not enough space to teleport there!"))));
							}
						} else {
							p.sendMessage(ChatColor.translateAlternateColorCodes('&',prefix+parse(plugin.getConfig().getString("Messages.NoEnoughRedstone","&2Sorry, You need &f[&c%c&f]&2 Red Stone to Teleport."))));
						}
						
					} else {
						p.sendMessage(ChatColor.translateAlternateColorCodes('&',prefix+parse(plugin.getConfig().getString("Messages.NotThere","&2Sorry, you can''t teleport there.!"))));
					}
				} else {
					p.sendMessage(ChatColor.translateAlternateColorCodes('&',prefix+parse(plugin.getConfig().getString("Messages.CombatTimeout","&2In combat! You can''t teleport for &f[&c%t&f]&2seconds"))));
				}
			}
		}
	}
}
