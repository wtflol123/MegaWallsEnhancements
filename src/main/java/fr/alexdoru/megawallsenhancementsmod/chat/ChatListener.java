package fr.alexdoru.megawallsenhancementsmod.chat;

import fr.alexdoru.megawallsenhancementsmod.api.apikey.HypixelApiKeyUtil;
import fr.alexdoru.megawallsenhancementsmod.asm.hooks.NetHandlerPlayClientHook;
import fr.alexdoru.megawallsenhancementsmod.commands.CommandScanGame;
import fr.alexdoru.megawallsenhancementsmod.config.ConfigHandler;
import fr.alexdoru.megawallsenhancementsmod.data.WDR;
import fr.alexdoru.megawallsenhancementsmod.data.WdrData;
import fr.alexdoru.megawallsenhancementsmod.events.MegaWallsGameEvent;
import fr.alexdoru.megawallsenhancementsmod.features.MegaWallsEndGameStats;
import fr.alexdoru.megawallsenhancementsmod.features.PartyDetection;
import fr.alexdoru.megawallsenhancementsmod.features.SquadHandler;
import fr.alexdoru.megawallsenhancementsmod.fkcounter.FKCounterMod;
import fr.alexdoru.megawallsenhancementsmod.fkcounter.KillCounter;
import fr.alexdoru.megawallsenhancementsmod.gui.huds.ArrowHitHUD;
import fr.alexdoru.megawallsenhancementsmod.gui.huds.CreeperPrimedTNTHUD;
import fr.alexdoru.megawallsenhancementsmod.gui.huds.HunterStrengthHUD;
import fr.alexdoru.megawallsenhancementsmod.gui.huds.KillCooldownHUD;
import fr.alexdoru.megawallsenhancementsmod.nocheaters.GameInfoGrabber;
import fr.alexdoru.megawallsenhancementsmod.utils.DelayedTask;
import fr.alexdoru.megawallsenhancementsmod.utils.SoundUtil;
import fr.alexdoru.megawallsenhancementsmod.utils.StringUtil;
import fr.alexdoru.megawallsenhancementsmod.utils.TimerUtil;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.HashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ChatListener {

    private static final String BAN_MESSAGE = "A player has been removed from your game.";
    private static final String HUNTER_STRENGTH_MESSAGE = "Your Force of Nature gave you a 5 second Strength I buff.";
    private static final String GENERAL_START_MESSAGE = "The game starts in 1 second!";
    private static final String OWN_WITHER_DEATH_MESSAGE = "Your wither has died. You can no longer respawn!";
    private static final String PREP_PHASE = "Prepare your defenses!";
    private static final Pattern API_KEY_PATTERN = Pattern.compile("^Your new API key is ([a-zA-Z0-9-]+)");
    private static final Pattern BLOCKED_MESSAGE = Pattern.compile("^We blocked your comment \"(.+)\" as it is breaking our rules because[a-zA-Z\\s]+\\. https:\\/\\/www.hypixel.net\\/rules\\/.*");
    private static final Pattern COINS_PATTERN = Pattern.compile("^\\+\\d+ coins!( \\((?:Triple Coins \\+ EXP, |)(?:Active Booster, |)\\w+'s Network Booster\\)).*");
    private static final Pattern DREADLORD_STRENGTH_PATTERN = Pattern.compile("\u00a74\u00a7lSOUL SIPHON \u00a7c\u00a7l85% ([0-9])s");
    private static final Pattern HEROBRINE_STRENGTH_PATTERN = Pattern.compile("\u00a7e\u00a7lPOWER \u00a7c\u00a7l85% ([0-9])s");
    private static final Pattern HUNTER_PRE_STRENGTH_PATTERN = Pattern.compile("\u00a7a\u00a7lF\\.O\\.N\\. \u00a77\\(\u00a7l\u00a7c\u00a7lStrength\u00a77\\) \u00a7e\u00a7l([0-9]+)");
    private static final Pattern CREEPER_FISSION_HEART_PATTERN = Pattern.compile("^\u00a7a\u00a7lFISSION HEART \u00a7c\u00a7l([0-9])s");
    private static final Pattern LOCRAW_PATTERN = Pattern.compile("^\\{\"server\":\"(\\w+)\",\"gametype\":\"\\w+\"(?:|,\"lobbyname\":\"\\w+\")(?:|,\"mode\":\"\\w+\")(?:|,\"map\":\"[a-zA-Z0-9_ ]+\")\\}$");
    private static final Pattern PLAYER_JOIN_PATTERN = Pattern.compile("^(\\w{1,16}) has joined \\([0-9]{1,3}/[0-9]{1,3}\\)!");
    private static final Pattern ZOMBIE_STRENGTH_PATTERN = Pattern.compile("\u00a72\u00a7lBERSERK \u00a7c\u00a7l75% ([0-9])s");
    private static final Pattern MESSAGE_PATTERN = Pattern.compile("^(?:|\\[SHOUT\\] |\\[SPECTATOR\\] )(?:|\\[[A-Z]{3,6}\\] )(?:|\\[((?:MV|VI)P\\+?\\+?)\\] )(\\w{2,16}):.*");
    private static final HashSet<String> MW_REPETITVE_MSG = new HashSet<>();
    private static final TimerUtil timerStrength = new TimerUtil(11000L);
    private static boolean interceptLocraw;
    private static long timeInterceptLocraw;

    static {
        MW_REPETITVE_MSG.add("You broke your protected chest");
        MW_REPETITVE_MSG.add("You broke your protected trapped chest");
        MW_REPETITVE_MSG.add("Get to the middle to stop the hunger!");
        MW_REPETITVE_MSG.add("Your Salvaging skill returned your arrow to you!");
        MW_REPETITVE_MSG.add("Your Efficiency skill got you an extra drop!");
        MW_REPETITVE_MSG.add("Your Soothing Moo Skill is ready!");
        MW_REPETITVE_MSG.add("Click your sword or bow to activate your skill!");
        MW_REPETITVE_MSG.add("Your Eagles Eye Skill is ready!");
        MW_REPETITVE_MSG.add("Left Click with any bow to activate your skill!");
        MW_REPETITVE_MSG.add("Your From the Depths Skill is ready!");
    }

    @SubscribeEvent
    public void onChatMessage(ClientChatReceivedEvent event) {

        /*normal chat messages*/
        if (event.type == 0) {

            final String msg = EnumChatFormatting.getTextWithoutFormattingCodes(event.message.getUnformattedText());
            final String fmsg = event.message.getFormattedText();

            if (FKCounterMod.isInMwGame && ConfigHandler.hideRepetitiveMWChatMsg && MW_REPETITVE_MSG.contains(msg)) {
                event.setCanceled(true);
                return;
            }

            if (msg.equals(GENERAL_START_MESSAGE)) {
                GameInfoGrabber.saveGameInfoOnGameStart();
                SquadHandler.formSquad();
                return;
            }

            if (msg.equals(OWN_WITHER_DEATH_MESSAGE)) {
                KillCooldownHUD.instance.hideHUD();
                return;
            }

            /*
             * shortens the coins messages removing the booster info
             */
            if (ConfigHandler.shortCoinMessage) {
                final Matcher matchercoins = COINS_PATTERN.matcher(msg);
                if (matchercoins.matches()) {
                    event.message = new ChatComponentText(fmsg.replace(matchercoins.group(1), ""));
                    return;
                }
            }

            if (msg.equals(PREP_PHASE)) {
                MinecraftForge.EVENT_BUS.post(new MegaWallsGameEvent(MegaWallsGameEvent.EventType.GAME_START));
                return;
            }

            if (KillCounter.processMessage(fmsg, msg)) {
                event.setCanceled(true);
                return;
            }

            if (ArrowHitHUD.instance.processMessage(msg, fmsg)) {
                event.setCanceled(true);
                return;
            }

            final Matcher matcher = MESSAGE_PATTERN.matcher(msg);
            String senderRank = null;
            String messageSender = null;
            String squadname = null;
            if (matcher.matches()) {
                senderRank = matcher.group(1);
                messageSender = matcher.group(2);
                squadname = SquadHandler.getSquad().get(messageSender);
            }

            final Matcher matcherBlockedMessage = BLOCKED_MESSAGE.matcher(msg);
            if (matcherBlockedMessage.matches()) {
                final String blockedMessage = matcherBlockedMessage.group(1);
                ReportSuggestionHandler.processBlockedMessage(blockedMessage);
                return;
            }

            if (ReportSuggestionHandler.parseReportMessage(senderRank, messageSender, squadname, msg, fmsg)) {
                event.setCanceled(true);
                return;
            }

            // Chat Censoring
            if (ConfigHandler.censorCheaterChatMsg && messageSender != null) {
                final NetworkPlayerInfo networkPlayerInfo = NetHandlerPlayClientHook.playerInfoMap.get(messageSender);
                if (networkPlayerInfo != null) {
                    final String uuid = networkPlayerInfo.getGameProfile().getId().toString().replace("-", "");
                    final WDR wdr = WdrData.getWdr(uuid, messageSender);
                    if (wdr != null && wdr.hasValidCheats()) {
                        if (!ConfigHandler.deleteCheaterChatMsg) {
                            ChatUtil.addChatMessage(StringUtil.censorChatMessage(fmsg, messageSender));
                        }
                        event.setCanceled(true);
                        return;
                    }
                }
            }

            if (squadname != null && messageSender != null) {
                event.message = new ChatComponentText(fmsg.replaceFirst(messageSender, squadname));
            }

            if (MegaWallsEndGameStats.processMessage(msg)) {
                return;
            }

            if (ConfigHandler.showStrengthHUD && msg.equals(HUNTER_STRENGTH_MESSAGE)) {
                HunterStrengthHUD.instance.setStrengthRenderStart(5000L);
                return;
            }

            if (parseAPIKey(msg)) {
                return;
            }

            if (FKCounterMod.preGameLobby) {
                final Matcher playerJoinMatcher = PLAYER_JOIN_PATTERN.matcher(msg);
                if (playerJoinMatcher.matches()) {
                    PartyDetection.onPlayerJoin(playerJoinMatcher.group(1), System.currentTimeMillis());
                    return;
                }
            }

            if (msg.equals(BAN_MESSAGE)) {
                new DelayedTask(NetHandlerPlayClientHook::printDisconnectedPlayers, 10);
                return;
            }

            if (interceptLocraw) {
                if (System.currentTimeMillis() - timeInterceptLocraw < 1001L) {
                    final Matcher locrawMatcher = LOCRAW_PATTERN.matcher(msg);
                    if (locrawMatcher.matches()) {
                        final String gameId = locrawMatcher.group(1).replace("mega", "M");
                        CommandScanGame.handleScangameCommand(gameId);
                        interceptLocraw = false;
                        event.setCanceled(true);
                    }
                } else {
                    interceptLocraw = false;
                }
            }

            /*Status messages*/
        } else if (event.type == 2) {

            final String fmsg = event.message.getFormattedText();

            if (ConfigHandler.showStrengthHUD) {
                final Matcher dreadStrenghtMatcher = DREADLORD_STRENGTH_PATTERN.matcher(fmsg);
                if (dreadStrenghtMatcher.find()) {
                    HunterStrengthHUD.instance.setStrengthRenderStart(Long.parseLong(dreadStrenghtMatcher.group(1)) * 1000L);
                    return;
                }
                final Matcher preStrengthMatcher = HUNTER_PRE_STRENGTH_PATTERN.matcher(fmsg);
                if (preStrengthMatcher.find()) {
                    if (timerStrength.update()) {
                        SoundUtil.playStrengthSound();
                    }
                    final String preStrengthTimer = preStrengthMatcher.group(1);
                    HunterStrengthHUD.instance.setPreStrengthTime(preStrengthTimer);
                    return;
                }
                final Matcher herobrineStrenghtMatcher = HEROBRINE_STRENGTH_PATTERN.matcher(fmsg);
                if (herobrineStrenghtMatcher.find()) {
                    HunterStrengthHUD.instance.setStrengthRenderStart(Long.parseLong(herobrineStrenghtMatcher.group(1)) * 1000L);
                    return;
                }
                final Matcher zombieStrenghtMatcher = ZOMBIE_STRENGTH_PATTERN.matcher(fmsg);
                if (zombieStrenghtMatcher.find()) {
                    HunterStrengthHUD.instance.setStrengthRenderStart(Long.parseLong(zombieStrenghtMatcher.group(1)) * 1000L);
                }
            }

            if (ConfigHandler.showPrimedTNTHUD) {
                final Matcher creeperMatcher = CREEPER_FISSION_HEART_PATTERN.matcher(fmsg);
                if (creeperMatcher.find()) {
                    CreeperPrimedTNTHUD.instance.setCooldownRenderStart(creeperMatcher.group(1));
                }
            }

        }

    }

    /*
     * automatically sets up the api key on hypixel when you type /api new
     */
    private boolean parseAPIKey(String msg) {
        final Matcher matcherapikey = API_KEY_PATTERN.matcher(msg);
        if (matcherapikey.matches()) {
            HypixelApiKeyUtil.setApiKey(matcherapikey.group(1));
            return true;
        }
        return false;
    }

    public static void interceptLocrawAndRunScangame() {
        interceptLocraw = true;
        timeInterceptLocraw = System.currentTimeMillis();
    }

}
