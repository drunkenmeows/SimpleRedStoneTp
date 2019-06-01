package me.drunkenmeows.simpleredstonetp;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.plugin.java.JavaPlugin;


public class SimpleRedstoneTP extends JavaPlugin {
	
	public final Logger logger = Logger.getLogger("Minecraft");
	public Metrics metrics;
	
	public List<String> srtptoggle = new ArrayList<String>();
	
	@Override
	public void onEnable()
	{
		getServer().getPluginManager().registerEvents(new srtpListener(this), this);		
		this.saveDefaultConfig();
		
		try {
		    this.metrics = new Metrics(this);
		    metrics.start();
		} catch (IOException e) {
		    // Failed to submit the stats :-(
		}
	}
	
	@Override
	public void onDisable()
	{
		HandlerList.unregisterAll(this);
	}
	
	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args)	{	
		if(cmd.getLabel().equals("srtptoggle"))
		{
			String prefix = this.getConfig().getString("Message.Prefix", "&f[&csrTP&f] ");
			if(sender instanceof Player)
			{
				
				Player p = (Player)sender;
				String name = p.getName();
				
				if(srtptoggle.contains(name))
				{
					srtptoggle.remove(name);
					p.sendMessage(ChatColor.translateAlternateColorCodes('&', prefix + this.getConfig().getString("Message.ToggleOn", "&2Redstone teleport &f[&cenabled&f]!")));
				} else {
					srtptoggle.add(name);
					p.sendMessage(ChatColor.translateAlternateColorCodes('&', prefix + this.getConfig().getString("Message.ToggleOff", "&2Redstone teleport &f[&cdisabled&f]!")));
				}
			}
		}
		return true;
	}
	
	public void fireworkEffect(Player player, Color color, FireworkEffect.Type FET, int power, boolean trail)
	{
		Location loc = player.getLocation();
	    loc.setY(loc.getY() - 2.0D);
	    
	    Firework firework = (Firework)player.getWorld().spawnEntity(loc, EntityType.FIREWORK);
	    FireworkMeta fireworkMeta = firework.getFireworkMeta();
	    FireworkEffect effect = FireworkEffect.builder().flicker(false).withColor(color).with(FET).trail(trail).build();
	    fireworkMeta.addEffect(effect);
	    fireworkMeta.setPower(power);
		firework.setFireworkMeta(fireworkMeta);
		
	}
}