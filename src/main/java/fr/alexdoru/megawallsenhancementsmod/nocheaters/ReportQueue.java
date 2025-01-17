package fr.alexdoru.megawallsenhancementsmod.nocheaters;

import fr.alexdoru.megawallsenhancementsmod.chat.ChatHandler;
import fr.alexdoru.megawallsenhancementsmod.chat.ChatUtil;
import fr.alexdoru.megawallsenhancementsmod.data.StringLong;
import fr.alexdoru.megawallsenhancementsmod.data.WDR;
import fr.alexdoru.megawallsenhancementsmod.events.MegaWallsGameEvent;
import fr.alexdoru.megawallsenhancementsmod.fkcounter.FKCounterMod;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.util.EnumChatFormatting;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

public class ReportQueue {

    public static final ReportQueue INSTANCE = new ReportQueue();
    private static final Minecraft mc = Minecraft.getMinecraft();
    private static final FontRenderer frObj = mc.fontRendererObj;
    private static final int TIME_BETWEEN_REPORTS_MAX = 5 * 60 * 20;
    private static final int TIME_BETWEEN_REPORTS_MIN = 3 * 60 * 20;
    private static final int AUTOREPORT_PER_GAME = 2;

    public boolean isDebugMode = false;
    private int counter;
    private int standingStillCounter;
    private int standingStillLimit = 18;
    private int movingCounter;
    private int autoReportSent;
    private final List<ReportInQueue> queueList = new ArrayList<>();
    private final List<Long> timestampsLastReports = new ArrayList<>();
    private final List<StringLong> playersReportedThisGame = new ArrayList<>();
    private final Random random = new Random();

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event) {

        if (event.phase != TickEvent.Phase.START) {
            return;
        }

        if (counter <= 0 && !queueList.isEmpty() && mc.thePlayer != null) {
            if (isPlayerStandingStill(mc.thePlayer)) {
                standingStillCounter++;
                if (standingStillCounter >= standingStillLimit) {
                    movingCounter = 0;
                    final int index = getIndexOfFirstReportSuggestion();
                    final ReportInQueue reportInQueue = queueList.remove(index == -1 ? 0 : index);
                    final String playername = reportInQueue.reportedPlayer;
                    if (reportInQueue.isReportSuggestion || FKCounterMod.isInMwGame) {
                        mc.thePlayer.sendChatMessage("/wdr " + playername);
                        addReportTimestamp(false);
                        if (isDebugMode) ChatUtil.debug("sent report for " + playername);
                    }
                    counter = getNextCounterDelay();
                    standingStillLimit = 12 + random.nextInt(20);
                    ChatHandler.deleteStopMovingInstruction();
                } else {
                    incrementMovingCounter();
                }
            } else {
                standingStillCounter = 0;
                incrementMovingCounter();
            }
        }

        if (isReportQueueInactive()) {
            MinecraftForge.EVENT_BUS.unregister(this);
        }

        counter--;

    }

    private void incrementMovingCounter() {
        movingCounter++;
        if (movingCounter % 5 == 0) {
            ChatHandler.printStopMovingInstruction();
        }
    }

    private int getNextCounterDelay() {
        if (doesQueueHaveReportSuggestion()) {
            return getTickDelay() + 20 * 20;
        } else {
            final int i = TIME_BETWEEN_REPORTS_MAX - 12 * 20 * (queueList.isEmpty() ? 0 : queueList.size() - 1);
            return (int) ((10d * random.nextGaussian() / 6d) + Math.max(i, TIME_BETWEEN_REPORTS_MIN));
        }
    }

    private int getIndexOfFirstReportSuggestion() {
        for (int i = 0; i < queueList.size(); i++) {
            if (queueList.get(i).isReportSuggestion) {
                return i;
            }
        }
        return -1;
    }

    private boolean doesQueueHaveReportSuggestion() {
        for (final ReportInQueue reportInQueue : queueList) {
            if (reportInQueue.isReportSuggestion) {
                return true;
            }
        }
        return false;
    }

    @SubscribeEvent
    public void onRender(RenderGameOverlayEvent.Post event) {
        if (isDebugMode && event.type == RenderGameOverlayEvent.ElementType.TEXT) {
            final int x = 0;
            int y = frObj.FONT_HEIGHT * 4;
            frObj.drawString(EnumChatFormatting.DARK_GREEN + "REPORT QUEUE", x, y, 0, true);
            y += frObj.FONT_HEIGHT;
            boolean first = true;
            for (final ReportInQueue reportInQueue : queueList) {
                frObj.drawString((reportInQueue.isReportSuggestion ? EnumChatFormatting.RED : EnumChatFormatting.GREEN) + reportInQueue.reportedPlayer, x, y, 0, true);
                if (first) {
                    frObj.drawString(EnumChatFormatting.GOLD + " " + counter / 20 + "s", x + frObj.getStringWidth(reportInQueue.reportedPlayer), y, 0, true);
                }
                y += frObj.FONT_HEIGHT;
                first = false;
            }
        }
    }

    @SubscribeEvent
    public void onGameEnd(MegaWallsGameEvent event) {
        if (event.getType() == MegaWallsGameEvent.EventType.GAME_END) {
            queueList.removeIf(reportInQueue -> !reportInQueue.isReportSuggestion);
        }
    }

    public void addPlayerToQueue(String playername, boolean printDelayMsg) {
        if (canReportPlayerThisGame(playername)) {
            if (isReportQueueInactive()) {
                MinecraftForge.EVENT_BUS.register(this);
                counter = random.nextInt(TIME_BETWEEN_REPORTS_MAX);
            } else if (printDelayMsg) {
                ChatUtil.addChatMessage(EnumChatFormatting.GRAY + "Sending report in a moment...");
            }
            queueList.add(new ReportInQueue(null, playername, false));
        }
    }

    private void addPlayerToQueue(String suggestionSender, String playername, int tickDelay) {
        if (isReportQueueInactive()) {
            MinecraftForge.EVENT_BUS.register(this);
        }
        counter = tickDelay;
        queueList.add(new ReportInQueue(suggestionSender, playername, true));
    }

    /**
     * Called from the auto-report suggestions
     * if an auto-report for that player is already in the queue, it prioritizes it
     * Returns true if it adds the players to the report queue
     */
    public boolean addPlayerToQueueRandom(String suggestionSender, String reportedPlayer) {
        queueList.removeIf(reportInQueue -> (reportInQueue.reportedPlayer.equalsIgnoreCase(reportedPlayer)));
        addPlayerToQueue(suggestionSender, reportedPlayer, getTickDelay());
        return true;
    }

    /**
     * Gaussian
     * Min value : 5 seconds
     * Avg value : 17 seconds
     */
    private int getTickDelay() {
        return (int) (100d + Math.abs(100d * random.nextGaussian() + 240d));
    }

    /**
     * Handles the auto report feature
     *
     * @return true if it sends a report
     */
    public boolean addAutoReportToQueue(long datenow, String playerName, WDR wdr) {
        if (autoReportSent < AUTOREPORT_PER_GAME && wdr.canBeAutoreported(datenow) && wdr.hasValidCheats()) {
            wdr.timestamp = datenow;
            addPlayerToQueue(playerName, false);
            autoReportSent++;
            return true;
        }
        return false;
    }

    private boolean isReportQueueInactive() {
        return counter <= 0 && queueList.isEmpty();
    }

    public void clearReportsSentBy(String playername) {
        final StringBuilder stringBuilder = new StringBuilder();
        final Iterator<ReportInQueue> iterator = queueList.iterator();
        while (iterator.hasNext()) {
            final ReportInQueue reportInQueue = iterator.next();
            if (reportInQueue.messageSender != null && reportInQueue.messageSender.equals(playername)) {
                stringBuilder.append(" ").append(reportInQueue.reportedPlayer);
                iterator.remove();
            }
        }
        final String msg = stringBuilder.toString();
        if (!msg.equals("")) {
            ChatUtil.addChatMessage(ChatUtil.getTagNoCheaters() + EnumChatFormatting.GREEN + "Removed reports targeting :" + EnumChatFormatting.GOLD + msg);
        }
    }

    public void clearSuggestionsInReportQueue() {
        final StringBuilder stringBuilder = new StringBuilder();
        final Iterator<ReportInQueue> iterator = queueList.iterator();
        while (iterator.hasNext()) {
            final ReportInQueue reportInQueue = iterator.next();
            if (reportInQueue.messageSender != null) {
                stringBuilder.append(" ").append(reportInQueue.reportedPlayer);
                iterator.remove();
            }
        }
        final String msg = stringBuilder.toString();
        if (!msg.equals("")) {
            ChatUtil.addChatMessage(ChatUtil.getTagNoCheaters() + EnumChatFormatting.GREEN + "Removed reports targeting :" + EnumChatFormatting.GOLD + msg);
        }
    }

    public void clearReportsFor(String reportedPlayer) {
        final StringBuilder stringBuilder = new StringBuilder();
        final Iterator<ReportInQueue> iterator = queueList.iterator();
        while (iterator.hasNext()) {
            final ReportInQueue reportInQueue = iterator.next();
            if (reportInQueue.reportedPlayer != null && reportInQueue.reportedPlayer.equals(reportedPlayer)) {
                stringBuilder.append(" ").append(reportInQueue.reportedPlayer);
                iterator.remove();
            }
        }
        final String msg = stringBuilder.toString();
        if (!msg.equals("")) {
            ChatUtil.addChatMessage(ChatUtil.getTagNoCheaters() + EnumChatFormatting.GREEN + "Removed all reports targeting :" + EnumChatFormatting.GOLD + msg);
        }
    }

    /**
     * Called everytime a report is sent to check that the player doesn't send too many reports in a short time
     */
    public void addReportTimestamp(boolean manualReport) {
        final long l = System.currentTimeMillis();
        timestampsLastReports.removeIf(o -> (o + 2 * 60 * 1000L < l));
        if (manualReport && timestampsLastReports.size() > 4) {
            ChatUtil.addChatMessage(ChatUtil.getTagNoCheaters() + EnumChatFormatting.RED + "Don't report too many players at once or Hypixel will ignore your reports thinking you are a bot trying to flood their system");
        }
        timestampsLastReports.add(l);
    }

    public void clearPlayersReportedThisGame() {
        playersReportedThisGame.clear();
        autoReportSent = 0;
    }

    /**
     * This is only a check for reports via the auto reports
     * the check for report suggestions is in the ReportSuggestionHandler
     */
    private boolean canReportPlayerThisGame(String playername) {
        final long timestamp = System.currentTimeMillis();
        playersReportedThisGame.removeIf(o -> (o.timestamp + 40L * 60L * 1000L < timestamp));
        for (final StringLong stringLong : playersReportedThisGame) {
            if (stringLong.message != null && stringLong.message.equalsIgnoreCase(playername)) {
                return false;
            }
        }
        playersReportedThisGame.add(new StringLong(timestamp, playername));
        return true;
    }

    private boolean isPlayerStandingStill(EntityPlayerSP thePlayer) {
        return (mc.inGameHasFocus || mc.ingameGUI.getChatGUI().getChatOpen())
                && thePlayer.movementInput.moveForward == 0.0F
                && thePlayer.movementInput.moveStrafe == 0.0F
                && !thePlayer.movementInput.jump
                && !thePlayer.movementInput.sneak
                && !thePlayer.isUsingItem()
                && !thePlayer.isSwingInProgress;
    }

    public List<StringLong> getPlayersReportedThisGame() {
        return playersReportedThisGame;
    }

}

class ReportInQueue {

    public final String messageSender;
    public final String reportedPlayer;
    public final boolean isReportSuggestion;

    public ReportInQueue(String messageSender, String reportedPlayer, boolean isReportSuggestion) {
        this.messageSender = messageSender;
        this.reportedPlayer = reportedPlayer;
        this.isReportSuggestion = isReportSuggestion;
    }

}
