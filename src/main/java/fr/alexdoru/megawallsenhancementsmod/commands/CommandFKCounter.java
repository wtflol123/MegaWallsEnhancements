package fr.alexdoru.megawallsenhancementsmod.commands;

import fr.alexdoru.megawallsenhancementsmod.chat.ChatUtil;
import fr.alexdoru.megawallsenhancementsmod.features.SquadHandler;
import fr.alexdoru.megawallsenhancementsmod.fkcounter.KillCounter;
import fr.alexdoru.megawallsenhancementsmod.gui.guiscreens.FKConfigGuiScreen;
import fr.alexdoru.megawallsenhancementsmod.gui.huds.FKCounterHUD;
import fr.alexdoru.megawallsenhancementsmod.utils.DelayedTask;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumChatFormatting;

import java.util.*;
import java.util.stream.Collectors;

public class CommandFKCounter extends MyAbstractCommand {

    @Override
    public String getCommandName() {
        return "fks";
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return "/fks <help|p|players|say|settings>";
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) {

        if (args.length > 0 && args[0].equalsIgnoreCase("settings")) {

            new DelayedTask(() -> mc.displayGuiScreen(new FKConfigGuiScreen(null)), 1);

        } else if (args.length > 0 && (args[0].equalsIgnoreCase("players") || args[0].equalsIgnoreCase("player") || args[0].equalsIgnoreCase("p"))) {

            if (KillCounter.getGameId() == null) {
                ChatUtil.addChatMessage(EnumChatFormatting.RED + "This is not available right now");
                return;
            }

            final StringBuilder strBuilder = new StringBuilder();
            for (int TEAM = KillCounter.RED_TEAM; TEAM <= KillCounter.BLUE_TEAM; TEAM++) {
                strBuilder.append(KillCounter.getColorPrefixFromTeam(TEAM))
                        .append(KillCounter.getTeamNameFromTeam(TEAM))
                        .append(EnumChatFormatting.WHITE)
                        .append(": ");
                for (final Iterator<Map.Entry<String, Integer>> iterator = KillCounter.sortByDecreasingValue1(KillCounter.getPlayers(TEAM)).entrySet().iterator(); iterator.hasNext(); ) {
                    final Map.Entry<String, Integer> entry = iterator.next();
                    strBuilder.append(SquadHandler.getSquadname(entry.getKey())).append(" (").append(entry.getValue()).append(")");
                    if (iterator.hasNext()) {
                        strBuilder.append(", ");
                    }
                }
                strBuilder.append("\n");
            }

            ChatUtil.addChatMessage(strBuilder.toString());

        } else if (args.length == 2 && args[0].equalsIgnoreCase("remove")) {
            removePlayer(args[1]);
        } else if (args.length > 0 && args[0].equalsIgnoreCase("say")) {

            if (KillCounter.getGameId() == null) {
                ChatUtil.addChatMessage(EnumChatFormatting.RED + "This is not available right now");
                return;
            }

            final StringBuilder strBuilder = new StringBuilder();

            if (args.length == 1) {

                final HashMap<Integer, Integer> sortedmap = KillCounter.getSortedTeamKillsMap();
                int i = 0;
                for (final Map.Entry<Integer, Integer> entry : sortedmap.entrySet()) {
                    if (i != 0) {
                        strBuilder.append(", ");
                    }
                    strBuilder.append(KillCounter.getTeamNameFromTeam(entry.getKey())).append(": ").append(entry.getValue());
                    i++;
                }

            } else if (args.length == 2 && args[1].equalsIgnoreCase("red")) {
                strBuilder.append(KillCounter.getTeamNameFromTeam(KillCounter.RED_TEAM)).append(": ").append(KillCounter.sortByDecreasingValue1(KillCounter.getPlayers(KillCounter.RED_TEAM)).entrySet().stream().map(entry -> entry.getKey() + " (" + entry.getValue() + ")").collect(Collectors.joining(", ")));
            } else if (args.length == 2 && args[1].equalsIgnoreCase("green")) {
                strBuilder.append(KillCounter.getTeamNameFromTeam(KillCounter.GREEN_TEAM)).append(": ").append(KillCounter.sortByDecreasingValue1(KillCounter.getPlayers(KillCounter.GREEN_TEAM)).entrySet().stream().map(entry -> entry.getKey() + " (" + entry.getValue() + ")").collect(Collectors.joining(", ")));
            } else if (args.length == 2 && args[1].equalsIgnoreCase("yellow")) {
                strBuilder.append(KillCounter.getTeamNameFromTeam(KillCounter.YELLOW_TEAM)).append(": ").append(KillCounter.sortByDecreasingValue1(KillCounter.getPlayers(KillCounter.YELLOW_TEAM)).entrySet().stream().map(entry -> entry.getKey() + " (" + entry.getValue() + ")").collect(Collectors.joining(", ")));
            } else if (args.length == 2 && args[1].equalsIgnoreCase("blue")) {
                strBuilder.append(KillCounter.getTeamNameFromTeam(KillCounter.BLUE_TEAM)).append(": ").append(KillCounter.sortByDecreasingValue1(KillCounter.getPlayers(KillCounter.BLUE_TEAM)).entrySet().stream().map(entry -> entry.getKey() + " (" + entry.getValue() + ")").collect(Collectors.joining(", ")));
            }

            sendChatMessage(strBuilder.toString());

        } else if (args.length > 0 && args[0].equalsIgnoreCase("help")) {

            ChatUtil.addChatMessage(EnumChatFormatting.RED + "Usage : " + getCommandUsage(sender) + "\n"
                    + EnumChatFormatting.RED + "/fks : prints the amount of finals per team in the chat \n"
                    + EnumChatFormatting.RED + "/fks p or players : prints the amount of finals per player in the chat \n "
                    + EnumChatFormatting.RED + "/fks say : makes you send a message in the chat with the amount of finals per team \n"
                    + EnumChatFormatting.RED + "/fks settings : opens the settings GUI");

        } else {
            if (KillCounter.getGameId() == null) {
                ChatUtil.addChatMessage(EnumChatFormatting.RED + "This is not available right now");
                return;
            }
            final String strBuilder = KillCounter.getColorPrefixFromTeam(KillCounter.RED_TEAM) + KillCounter.getTeamNameFromTeam(KillCounter.RED_TEAM) + EnumChatFormatting.WHITE + ": " + KillCounter.getKills(KillCounter.RED_TEAM) + "\n" +
                    KillCounter.getColorPrefixFromTeam(KillCounter.GREEN_TEAM) + KillCounter.getTeamNameFromTeam(KillCounter.GREEN_TEAM) + EnumChatFormatting.WHITE + ": " + KillCounter.getKills(KillCounter.GREEN_TEAM) + "\n" +
                    KillCounter.getColorPrefixFromTeam(KillCounter.YELLOW_TEAM) + KillCounter.getTeamNameFromTeam(KillCounter.YELLOW_TEAM) + EnumChatFormatting.WHITE + ": " + KillCounter.getKills(KillCounter.YELLOW_TEAM) + "\n" +
                    KillCounter.getColorPrefixFromTeam(KillCounter.BLUE_TEAM) + KillCounter.getTeamNameFromTeam(KillCounter.BLUE_TEAM) + EnumChatFormatting.WHITE + ": " + KillCounter.getKills(KillCounter.BLUE_TEAM);
            ChatUtil.addChatMessage(strBuilder);

        }

    }

    @Override
    public List<String> getCommandAliases() {
        return Collections.singletonList("finalkillcounter");
    }

    @Override
    public List<String> addTabCompletionOptions(ICommandSender sender, String[] args, BlockPos pos) {
        final String[] fksarguments = {"players", "remove", "say", "settings", "help"};
        final String[] colors = {"red", "green", "yellow", "blue"};

        if (args.length == 1) {
            return getListOfStringsMatchingLastWord(args, fksarguments);
        }
        if (args.length == 2) {
            if (args[0].equals("remove")) {
                return getListOfStringsMatchingLastWord(args, getPlayerListinKillCounter());
            } else {
                return getListOfStringsMatchingLastWord(args, colors);
            }
        }
        return null;
    }

    private void removePlayer(String playerName) {
        final HashMap<String, Integer>[] teamKillsArray = KillCounter.getTeamKillsArray();
        if (teamKillsArray != null) {
            for (int team = 0; team < KillCounter.TEAMS; team++) {
                final Integer kills = teamKillsArray[team].get(playerName);
                if (kills != null) {
                    KillCounter.removeKilledPlayer(playerName, team);
                    FKCounterHUD.instance.updateDisplayText();
                    ChatUtil.addChatMessage(EnumChatFormatting.GREEN + "Removed " + KillCounter.getColorPrefixFromTeam(team) + playerName
                            + EnumChatFormatting.GREEN + " with " + EnumChatFormatting.GOLD + kills + EnumChatFormatting.GREEN + " final" + (kills > 1 ? "s" : "") + " from the " + KillCounter.getColorPrefixFromTeam(team) + KillCounter.getTeamNameFromTeam(team) + EnumChatFormatting.GREEN + " team.");
                    return;
                }
            }
        }
        ChatUtil.addChatMessage(EnumChatFormatting.RED + "Cannot find " + playerName + " in the FKCounter.");
    }

    private ArrayList<String> getPlayerListinKillCounter() {
        final ArrayList<String> playerList = new ArrayList<>();
        final HashMap<String, Integer>[] teamKillsArray = KillCounter.getTeamKillsArray();
        if (teamKillsArray == null) {
            return playerList;
        }
        for (final HashMap<String, Integer> teamMap : teamKillsArray) {
            for (final Map.Entry<String, Integer> entry : teamMap.entrySet()) {
                playerList.add(entry.getKey());
            }
        }
        return playerList;
    }

}
