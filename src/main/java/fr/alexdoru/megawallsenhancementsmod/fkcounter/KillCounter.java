package fr.alexdoru.megawallsenhancementsmod.fkcounter;

import fr.alexdoru.megawallsenhancementsmod.asm.accessor.NetworkPlayerInfoAccessor;
import fr.alexdoru.megawallsenhancementsmod.asm.hooks.NetHandlerPlayClientHook;
import fr.alexdoru.megawallsenhancementsmod.asm.hooks.RenderPlayerHook;
import fr.alexdoru.megawallsenhancementsmod.chat.ChatUtil;
import fr.alexdoru.megawallsenhancementsmod.config.ConfigHandler;
import fr.alexdoru.megawallsenhancementsmod.enums.MWClass;
import fr.alexdoru.megawallsenhancementsmod.events.MegaWallsGameEvent;
import fr.alexdoru.megawallsenhancementsmod.features.SquadHandler;
import fr.alexdoru.megawallsenhancementsmod.gui.huds.FKCounterHUD;
import fr.alexdoru.megawallsenhancementsmod.scoreboard.ScoreboardTracker;
import fr.alexdoru.megawallsenhancementsmod.scoreboard.ScoreboardUtils;
import fr.alexdoru.megawallsenhancementsmod.utils.DelayedTask;
import fr.alexdoru.megawallsenhancementsmod.utils.StringUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.EnumParticleTypes;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class KillCounter {

    public static final int TEAMS = 4;
    public static final int RED_TEAM = 0;
    public static final int GREEN_TEAM = 1;
    public static final int YELLOW_TEAM = 2;
    public static final int BLUE_TEAM = 3;
    private static final String[] KILL_MESSAGES = {
            /*Banana messages, put those messages at the top to not conflict with the other pattern (\w{1,16}) was killed by (\w{1,16})*/
            "(\\w{1,16}) got banana pistol'd by (\\w{1,16}).*",
            "(\\w{1,16}) was peeled by (\\w{1,16}).*",
            "(\\w{1,16}) was mushed by (\\w{1,16}).*",
            "(\\w{1,16}) was hit by a banana split from (\\w{1,16}).*",
            "(\\w{1,16}) was killed by an explosive banana from (\\w{1,16}).*",
            "(\\w{1,16}) was killed by a magic banana from (\\w{1,16}).*",
            "(\\w{1,16}) was turned into mush by (\\w{1,16}).*",
            /*Default messages*/
            "(\\w{1,16}) was shot and killed by (\\w{1,16}).*",
            "(\\w{1,16}) was snowballed to death by (\\w{1,16}).*",
            "(\\w{1,16}) was killed by (\\w{1,16}).*",
            "(\\w{1,16}) was killed with a potion by (\\w{1,16}).*",
            "(\\w{1,16}) was killed with an explosion by (\\w{1,16}).*",
            "(\\w{1,16}) was killed with magic by (\\w{1,16}).*",
            /*Western messages*/
            "(\\w{1,16}) was filled full of lead by (\\w{1,16}).*",
            "(\\w{1,16}) was iced by (\\w{1,16}).*",
            "(\\w{1,16}) met their end by (\\w{1,16}).*",
            "(\\w{1,16}) lost a drinking contest with (\\w{1,16}).*",
            "(\\w{1,16}) was killed with dynamite by (\\w{1,16}).*",
            "(\\w{1,16}) lost the draw to (\\w{1,16}).*",
            /*Fire messages*/
            "(\\w{1,16}) was struck down by (\\w{1,16}).*",
            "(\\w{1,16}) was turned to dust by (\\w{1,16}).*",
            "(\\w{1,16}) was turned to ash by (\\w{1,16}).*",
            "(\\w{1,16}) was melted by (\\w{1,16}).*",
            "(\\w{1,16}) was incinerated by (\\w{1,16}).*",
            "(\\w{1,16}) was vaporized by (\\w{1,16}).*",
            /*Love messages*/
            "(\\w{1,16}) was struck with Cupid's arrow by (\\w{1,16}).*",
            "(\\w{1,16}) was given the cold shoulder by (\\w{1,16}).*",
            "(\\w{1,16}) was hugged too hard by (\\w{1,16}).*",
            "(\\w{1,16}) drank a love potion from (\\w{1,16}).*",
            "(\\w{1,16}) was hit by a love bomb from (\\w{1,16}).*",
            "(\\w{1,16}) was no match for (\\w{1,16}).*",
            /*Paladin messages*/
            "(\\w{1,16}) was smote from afar by (\\w{1,16}).*",
            "(\\w{1,16}) was justly ended by (\\w{1,16}).*",
            "(\\w{1,16}) was purified by (\\w{1,16}).*",
            "(\\w{1,16}) was killed with holy water by (\\w{1,16}).*",
            "(\\w{1,16}) was dealt vengeful justice by (\\w{1,16}).*",
            "(\\w{1,16}) was returned to dust by (\\w{1,16}).*",
            /*Pirate messages*/
            "(\\w{1,16}) be shot and killed by (\\w{1,16}).*",
            "(\\w{1,16}) be snowballed to death by (\\w{1,16}).*",
            "(\\w{1,16}) be sent to Davy Jones' locker by (\\w{1,16}).*",
            "(\\w{1,16}) be killed with rum by (\\w{1,16}).*",
            "(\\w{1,16}) be shot with cannon by (\\w{1,16}).*",
            "(\\w{1,16}) be killed with magic by (\\w{1,16}).*",
            /*BBQ messages*/
            "(\\w{1,16}) was glazed in BBQ sauce by (\\w{1,16}).*",
            "(\\w{1,16}) was sprinkled with chilli powder by (\\w{1,16}).*",
            "(\\w{1,16}) was sliced up by (\\w{1,16}).*",
            "(\\w{1,16}) was overcooked by (\\w{1,16}).*",
            "(\\w{1,16}) was deep fried by (\\w{1,16}).*",
            "(\\w{1,16}) was boiled by (\\w{1,16}).*",
            /*Squeak messages*/
            "(\\w{1,16}) was squeaked from a distance by (\\w{1,16}).*",
            "(\\w{1,16}) was hit by frozen cheese from (\\w{1,16}).*",
            "(\\w{1,16}) was chewed up by (\\w{1,16}).*",
            "(\\w{1,16}) was chemically cheesed by (\\w{1,16}).*",
            "(\\w{1,16}) was turned into cheese whiz by (\\w{1,16}).*",
            "(\\w{1,16}) was magically squeaked by (\\w{1,16}).*",
            /*Natural deaths messages*/
            "(\\w{1,16}) starved to death\\.",
            "(\\w{1,16}) hit the ground too hard\\.",
            "(\\w{1,16}) blew up\\.",
            "(\\w{1,16}) exploded\\.",
            "(\\w{1,16}) tried to swim in lava\\.",
            "(\\w{1,16}) went up in flames\\.",
            "(\\w{1,16}) burned to death\\.",
            "(\\w{1,16}) suffocated in a wall\\.",
            "(\\w{1,16}) suffocated\\.",
            "(\\w{1,16}) fell out of the world\\.",
            "(\\w{1,16}) had a block fall on them\\.",
            "(\\w{1,16}) drowned\\.",
            "(\\w{1,16}) died from a cactus\\."
    };
    private static final String[] SCOREBOARD_PREFIXES = {"[R]", "[G]", "[Y]", "[B]"};
    private static final String[] DEFAULT_PREFIXES = {"c", "a", "e", "9"}; // RED GREEN YELLOW BLUE
    private static final HashMap<String, Integer> allPlayerKills = new HashMap<>();
    private static final ArrayList<String> deadPlayers = new ArrayList<>();
    /**
     * Used to check if a player is in the game for the reporting suggestions
     */
    private static final Set<String> playersPresentInGame = new HashSet<>();
    private static Pattern[] KILL_PATTERNS;
    private static String gameId;
    private static String[] prefixes; // color codes prefix that you are using in your hypixel mega walls settings
    private static HashMap<String, Integer>[] teamKillsArray;
    private static Random rand;

    public KillCounter() {
        rand = new Random();
        KILL_PATTERNS = new Pattern[KILL_MESSAGES.length];
        for (int i = 0; i < KILL_MESSAGES.length; i++) {
            KILL_PATTERNS[i] = Pattern.compile(KILL_MESSAGES[i]);
        }
        FKCounterHUD.instance.updateDisplayText();
    }

    /**
     * Resets the Killcounter and assigns it to a new game ID
     */
    @SuppressWarnings("unchecked")
    public static void ResetKillCounterTo(String gameIdIn) {
        playersPresentInGame.clear();
        gameId = gameIdIn;
        prefixes = new String[TEAMS];
        teamKillsArray = new HashMap[TEAMS];
        allPlayerKills.clear();
        deadPlayers.clear();
        for (int i = 0; i < TEAMS; i++) {
            prefixes[i] = DEFAULT_PREFIXES[i];
            teamKillsArray[i] = new HashMap<>();
        }
        Minecraft.getMinecraft().getNetHandler().getPlayerInfoMap().forEach(p -> {
            if (p != null) {
                ((NetworkPlayerInfoAccessor) p).setPlayerFinalkills(0);
            }
        });
        FKCounterHUD.instance.updateDisplayText();
    }

    public static boolean processMessage(String formattedText, String unformattedText) {

        if (!FKCounterMod.isInMwGame) {
            return false;
        }

        for (final Pattern pattern : KILL_PATTERNS) {

            final Matcher matcher = pattern.matcher(unformattedText);

            if (matcher.matches()) {

                if (matcher.groupCount() == 2) {
                    final String killedPlayer = matcher.group(1);
                    final String killer = matcher.group(2);
                    RenderPlayerHook.removeArrowsFrom(killedPlayer, -1);
                    final String killedTeamColor = StringUtil.getLastColorCodeBefore(formattedText, killedPlayer);
                    final String killerTeamColor = StringUtil.getLastColorCodeBefore(formattedText.replaceFirst(killedPlayer, ""), killer);
                    if (!killedTeamColor.equals("") && !killerTeamColor.equals("")) {
                        if (removeKilledPlayer(killedPlayer, killedTeamColor)) {
                            addKill(killer, killerTeamColor);
                            playersPresentInGame.add(killedPlayer);
                            playersPresentInGame.add(killer);
                        }
                        FKCounterHUD.instance.updateDisplayText();
                    }
                    if (ConfigHandler.strengthParticules) {
                        spawnParticles(killer);
                    }
                    ChatUtil.addChatMessage(formattedText.replace(killer, SquadHandler.getSquadname(killer)).replace(killedPlayer, SquadHandler.getSquadname(killedPlayer)));
                    return true;
                }

                if (matcher.groupCount() == 1) {
                    final String killedPlayer = matcher.group(1);
                    RenderPlayerHook.removeArrowsFrom(killedPlayer, -1);
                    final String killedTeamColor = StringUtil.getLastColorCodeBefore(formattedText, killedPlayer);
                    if (!killedTeamColor.equals("")) {
                        if (removeKilledPlayer(killedPlayer, killedTeamColor)) {
                            playersPresentInGame.add(killedPlayer);
                        }
                        FKCounterHUD.instance.updateDisplayText();
                    }
                    ChatUtil.addChatMessage(formattedText.replace(killedPlayer, SquadHandler.getSquadname(killedPlayer)));
                    return true;
                }

            }

        }

        return false;

    }

    public static String getGameId() {
        return gameId;
    }

    /**
     * Can return null if the fkcounter isn't initialized yet
     */
    public static HashMap<String, Integer>[] getTeamKillsArray() {
        return teamKillsArray;
    }

    public static int getKills(int team) {
        if (isNotValidTeam(team)) {
            return 0;
        }
        int kills = 0;
        for (final int k : teamKillsArray[team].values()) {
            kills += k;
        }
        return kills;
    }

    /**
     * Returns a sorted hashmap where the Keys are the Team interger, and the values are the amounts of finals for that team
     */
    public static HashMap<Integer, Integer> getSortedTeamKillsMap() {
        final HashMap<Integer, Integer> hashmap = new HashMap<>();
        hashmap.put(RED_TEAM, getKills(RED_TEAM));
        hashmap.put(GREEN_TEAM, getKills(GREEN_TEAM));
        hashmap.put(YELLOW_TEAM, getKills(YELLOW_TEAM));
        hashmap.put(BLUE_TEAM, getKills(BLUE_TEAM));
        return sortByDecreasingValue2(hashmap);
    }

    public static HashMap<String, Integer> getPlayers(int team) {
        if (isNotValidTeam(team)) {
            return new HashMap<>();
        }
        return teamKillsArray[team];
    }

    private static boolean isWitherDead(String color) {
        return !ScoreboardTracker.getMwScoreboardParser().isWitherAlive(color);
    }

    private static boolean areAllWithersAlive() {
        return ScoreboardTracker.getMwScoreboardParser().areAllWithersAlive();
    }

    /**
     * Detects the color codes you are using in your mega walls settings by looking at the scoreboard/sidebartext
     */
    private static void setTeamPrefixes() {
        for (final String line : ScoreboardUtils.getFormattedSidebarText()) {
            for (int team = 0; team < TEAMS; team++) {
                if (line.contains(SCOREBOARD_PREFIXES[team])) {
                    prefixes[team] = StringUtil.getLastColorCodeBefore(line, SCOREBOARD_PREFIXES[team]);
                }
            }
        }
        FKCounterHUD.instance.updateDisplayText();
    }

    public static void removeKilledPlayer(String player, int team) {
        removeKilledPlayer(player, getColorPrefixFromTeam(team).replace("\u00a7", ""));
    }

    /**
     * Removes a player from the fkcounter and returns true when successfull
     * i.e. if the players was in final kill and if the player wasn't already dead,
     * it sometime happens when a player gets double tapped
     */
    private static boolean removeKilledPlayer(String player, String color) {
        final int team = getTeamFromColor(color);
        if (isNotValidTeam(team)) {
            return false;
        }
        if (isWitherDead(color)) {
            teamKillsArray[team].remove(player);
            allPlayerKills.remove(player);
            deadPlayers.add(player);
            updateNetworkPlayerinfo(player, 0);
            return true;
        }
        return false;
    }

    private static void addKill(String playerGettingTheKill, String color) {
        final int team = getTeamFromColor(color);
        if (isNotValidTeam(team)) {
            return;
        }
        if (deadPlayers.contains(playerGettingTheKill)) {
            return;
        }
        final Integer finals = teamKillsArray[team].merge(playerGettingTheKill, 1, Integer::sum);
        allPlayerKills.merge(playerGettingTheKill, 1, Integer::sum);
        updateNetworkPlayerinfo(playerGettingTheKill, finals);
    }

    public static int getPlayersFinals(String playername) {
        final Integer finals = allPlayerKills.get(playername);
        return finals == null ? 0 : finals;
    }

    private static void updateNetworkPlayerinfo(String playername, int finals) {
        final NetworkPlayerInfo networkPlayerInfo = NetHandlerPlayClientHook.playerInfoMap.get(playername);
        if (networkPlayerInfo instanceof NetworkPlayerInfoAccessor) {
            ((NetworkPlayerInfoAccessor) networkPlayerInfo).setPlayerFinalkills(finals);
        }
    }

    private static int getTeamFromColor(String color) {
        for (int team = 0; team < TEAMS; team++) {
            if (prefixes[team].equalsIgnoreCase(color)) {
                return team;
            }
        }
        return -1;
    }

    public static String getColorPrefixFromTeam(int team) {
        return "\u00a7" + prefixes[team];
    }

    public static String getTeamNameFromTeam(int team) {
        switch (team) {
            case 0:
                return "Red";
            case 1:
                return "Green";
            case 2:
                return "Yellow";
            case 3:
                return "Blue";
            default:
                return "?";
        }
    }

    /**
     * Used by the report suggestion system
     */
    public static boolean wasPlayerInThisGame(String playername) {
        for (final String name : playersPresentInGame) {
            if (name.equalsIgnoreCase(playername))
                return true;
        }
        return false;
    }

    public static List<String> getPlayersInThisGame() {
        return new ArrayList<>(playersPresentInGame);
    }

    private static boolean isNotValidTeam(int team) {
        return (team < 0 || team >= TEAMS);
    }

    public static HashMap<String, Integer> sortByDecreasingValue1(HashMap<String, Integer> hashmapIn) {
        final List<Map.Entry<String, Integer>> list = new LinkedList<>(hashmapIn.entrySet());
        list.sort((o1, o2) -> (o2.getValue()).compareTo(o1.getValue()));
        final HashMap<String, Integer> temp = new LinkedHashMap<>();
        for (final Map.Entry<String, Integer> aa : list) {
            temp.put(aa.getKey(), aa.getValue());
        }
        return temp;
    }

    private static HashMap<Integer, Integer> sortByDecreasingValue2(HashMap<Integer, Integer> hashmapIn) {
        final List<Map.Entry<Integer, Integer>> list = new LinkedList<>(hashmapIn.entrySet());
        list.sort((o1, o2) -> (o2.getValue()).compareTo(o1.getValue()));
        final HashMap<Integer, Integer> temp = new LinkedHashMap<>();
        for (final Map.Entry<Integer, Integer> aa : list) {
            temp.put(aa.getKey(), aa.getValue());
        }
        return temp;
    }

    private static void spawnParticles(String killer) {
        final WorldClient world = Minecraft.getMinecraft().theWorld;
        if (world == null) {
            return;
        }
        final ScorePlayerTeam team = world.getScoreboard().getPlayersTeam(killer); // O(1)
        if (team == null) {
            return;
        }
        final String classTag = EnumChatFormatting.getTextWithoutFormattingCodes(team.getColorSuffix().replace("[", "").replace("]", "").replace(" ", ""));
        final MWClass mwClass = MWClass.fromTag(classTag);
        if (mwClass == null) {
            return;
        }

        final int duration;
        if (mwClass == MWClass.DREADLORD) {
            duration = 5 * 20;
        } else if (mwClass == MWClass.HEROBRINE) {
            duration = 6 * 20;
        } else {
            return;
        }

        final EntityPlayer player = world.getPlayerEntityByName(killer); // O(N)
        if (player == null) {
            return;
        }

        for (int i = 0; i < duration / 10; i++) {

            new DelayedTask(() -> {
                for (int j = 0; j < 5; ++j) {
                    final double d0 = rand.nextGaussian() * 0.02D;
                    final double d1 = rand.nextGaussian() * 0.02D;
                    final double d2 = rand.nextGaussian() * 0.02D;
                    world.spawnParticle(
                            EnumParticleTypes.VILLAGER_ANGRY,
                            player.posX + (double) (rand.nextFloat() * player.width * 2.0F) - (double) player.width,
                            player.posY + 1.0D + (double) (rand.nextFloat() * player.height),
                            player.posZ + (double) (rand.nextFloat() * player.width * 2.0F) - (double) player.width,
                            d0,
                            d1,
                            d2
                    );
                }
            }, i * 10);

        }

    }

    @SubscribeEvent
    public void onMwGame(MegaWallsGameEvent event) {

        /*
         * to fix the bug where the FKCounter doesn't work properly if you play two games in a row on a server with the same serverID
         */
        if (event.getType() == MegaWallsGameEvent.EventType.GAME_START) {
            final String currentGameId = ScoreboardTracker.getMwScoreboardParser().getGameId();
            if (currentGameId != null) {
                ResetKillCounterTo(currentGameId);
            }
            setTeamPrefixes();
            return;
        }

        if (event.getType() == MegaWallsGameEvent.EventType.CONNECT) {
            final String currentGameId = ScoreboardTracker.getMwScoreboardParser().getGameId(); // this is not null due to how the event is defined/posted
            if (gameId == null || !gameId.equals(currentGameId)) {
                ResetKillCounterTo(currentGameId);
            }
            /*
             * this is here to fix the bug where the killcounter doesn't work if you re-start your minecraft during a game of MW
             * or if you changed your colors for the teams in your MW settings and rejoined the game
             */
            setTeamPrefixes();
        }

    }

}
