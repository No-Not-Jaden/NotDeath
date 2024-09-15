package me.jadenp.notdeath;

import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.entity.PlayerDeathEvent;

public class ExperienceCalculator {
    private final String action;
    private final String lostExp;
    public ExperienceCalculator(NotDeath plugin) {
        ConfigurationSection configuration = plugin.getConfig().getConfigurationSection("experience");
        if (configuration != null) {
            action = configuration.getString("action");
            lostExp = configuration.getString("lost-exp");
        } else {
            Bukkit.getLogger().warning("[NotDeath] No configuration section for experience!");
            action = "DROP";
            lostExp = "DEFAULT";
        }
    }


    public void handleDeath(PlayerDeathEvent event) {
        if (action.equalsIgnoreCase("REMOVE")) {
            // all exp is removed, so none of the other options matter
            event.setKeepLevel(false);
            event.setDroppedExp(0);
        } else if (lostExp.equalsIgnoreCase("DEFAULT")) {
            if (action.equalsIgnoreCase("DROP")) {
                // drop the default minecraft exp
                event.setKeepLevel(false);
                event.setDroppedExp(Math.min(event.getEntity().getLevel() * 7, 100));
            } else if (action.equalsIgnoreCase("KEEP")) {
                // all exp is kept
                event.setKeepLevel(true);
            } else {
                // split the total exp to be kept and dropped
                splitDropKeepExp(event, event.getEntity().getTotalExperience());
            }
        } else {
            // parse the lost exp ratio
            double lostExpRatio;
            try {
                lostExpRatio = Double.parseDouble(lostExp) / 100;
            } catch (NumberFormatException e) {
                Bukkit.getLogger().warning("[NotDeath] Unknown experience lost-exp configured: " + lostExp);
                lostExpRatio = 0.5;
            }
            // convert to the new total experience that should be dropped
            int totalExp = (int) (Experience.getExp(event.getEntity()) * (1.0-lostExpRatio));
            if (action.equalsIgnoreCase("DROP")) {
                // set the dropped exp
                event.setKeepLevel(false);
                event.setDroppedExp(totalExp);
            } else if (action.equalsIgnoreCase("KEEP")) {
                // set the new exp on respawn to be the new total
                event.setKeepLevel(false);
                event.setNewExp(totalExp);
                event.setDroppedExp(0);
            } else {
                // split the new total to be dropped and kept
                splitDropKeepExp(event, totalExp);
            }
        }
    }

    /**
     * Splits totalExp with the percent value stored in action. One part will be dropped, and the other will be kept when the player respawns.
     * @param event Event that caused a player to die.
     * @param totalExp The total exp that should be handed out.
     */
    private void splitDropKeepExp(PlayerDeathEvent event, int totalExp) {
        double keptExpRatio;
        try {
            keptExpRatio = Double.parseDouble(action) / 100;
        } catch (NumberFormatException e) {
            Bukkit.getLogger().warning("[NotDeath] Unknown experience action configured: " + action);
            keptExpRatio = 1;
        }
        // split the exp
        int keptExp = (int) (totalExp * keptExpRatio);
        int droppedExp = totalExp - keptExp;

        event.setKeepLevel(false);
        event.setDroppedExp(droppedExp);
        event.setNewExp(keptExp);
    }

}
