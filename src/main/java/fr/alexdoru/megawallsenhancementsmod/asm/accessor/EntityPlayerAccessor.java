package fr.alexdoru.megawallsenhancementsmod.asm.accessor;

import fr.alexdoru.megawallsenhancementsmod.hackerdetector.data.PlayerDataSamples;

public interface EntityPlayerAccessor {
    String getPrestige4Tag();
    void setPrestige4Tag(String prestige4tag);
    String getPrestige5Tag();
    void setPrestige5Tag(String prestige5tag);
    PlayerDataSamples getPlayerDataSamples();
}
