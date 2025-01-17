package fr.alexdoru.megawallsenhancementsmod.hackerdetector;

import fr.alexdoru.megawallsenhancementsmod.asm.accessor.EntityPlayerAccessor;
import fr.alexdoru.megawallsenhancementsmod.chat.ChatUtil;
import fr.alexdoru.megawallsenhancementsmod.hackerdetector.checks.*;
import fr.alexdoru.megawallsenhancementsmod.hackerdetector.data.PlayerDataSamples;
import fr.alexdoru.megawallsenhancementsmod.hackerdetector.utils.Vector3D;
import fr.alexdoru.megawallsenhancementsmod.utils.NameUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class HackerDetector {

    public static final HackerDetector INSTANCE = new HackerDetector();
    /** Field stolen from EntityLivingBase */
    private static final UUID sprintingUUID = UUID.fromString("662A6B8D-DA3E-4C1C-8813-96EA6097278D");
    private final Minecraft mc = Minecraft.getMinecraft();
    private final List<ICheck> checkList = new ArrayList<>();
    private long timeElapsedTemp = 0L;
    private long timeElapsed = 0L;
    private int playersChecked = 0;
    private int playersCheckedTemp = 0;

    static {
        MinecraftForge.EVENT_BUS.register(INSTANCE);
    }

    public HackerDetector() {
        checkList.add(new AutoblockCheck());
        checkList.add(new KillAuraHeadsnapCheck());
        checkList.add(new NoSlowdownCheck());
        checkList.add(new OmniSprintCheck());
    }

    @SubscribeEvent
    public void onDrawDebugText(RenderGameOverlayEvent.Text event) {
        if (mc.gameSettings.showDebugInfo) {
            event.left.add("");
            event.left.add("Hacker Detector:");
            event.left.add("Player" + (playersChecked > 1 ? "s" : "") + " checked: " + playersChecked);
            event.left.add("Time elapsed (ns/s): " + ChatUtil.formatLong(timeElapsed));
            final double fpsLost = (timeElapsed / (10e9d - timeElapsed)) * Minecraft.getDebugFPS();
            event.left.add("Impact on performance : -" + String.format("%.2f", fpsLost) + "fps");
        }
    }

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.START) {
            playersCheckedTemp = 0;
        } else if (event.phase == TickEvent.Phase.END) {
            if (mc.thePlayer != null && mc.thePlayer.ticksExisted % 20 == 0) {
                timeElapsed = timeElapsedTemp;
                timeElapsedTemp = 0L;
            }
            playersChecked = playersCheckedTemp;
        }
    }

    /**
     * This gets called once per entity per tick
     * only gets called when the client plays on a server
     * Hook is injected at end of {@link net.minecraft.world.World#updateEntityWithOptionalForce}
     */
    public void performChecksOnPlayer(EntityPlayer player) {
        if (mc.thePlayer == null ||
                player == mc.thePlayer ||
                //!player.getName().equals("") || // TODO removedebug
                player.ticksExisted < 20 ||
                player.isDead ||
                player.capabilities.isFlying ||
                player.capabilities.isCreativeMode ||
                NameUtil.filterNPC(player.getUniqueID())) {
            return;
        }
        final long timeStart = System.nanoTime();
        playersCheckedTemp++;
        final PlayerDataSamples data = ((EntityPlayerAccessor) player).getPlayerDataSamples();
        updateEntityFields(player, data);
        checkList.forEach(check -> check.performCheck(player, data));
        timeElapsedTemp += System.nanoTime() - timeStart;
    }

    private void updateEntityFields(EntityPlayer player, PlayerDataSamples data) {
        final IAttributeInstance attribute = player.getEntityAttribute(SharedMonsterAttributes.movementSpeed);
        data.sprintTime = attribute.getModifier(sprintingUUID) == null ? 0 : data.sprintTime + 1;
        data.useItemTime = player.isUsingItem() ? data.useItemTime + 1 : 0;
        data.lastHurtTime = player.hurtTime == 9 ? 0 : data.lastHurtTime + 1;
        final Vector3D dXdYdZ = new Vector3D(
                player.posX - player.lastTickPosX,
                player.posY - player.lastTickPosY,
                player.posZ - player.lastTickPosZ
        );
        //data.prevPositionDiffXZ = data.positionDiffXZ;
        //data.positionDiffXZ = dXdYdZ.lengthVector2DXZ();
        if (!data.dXdYdZSampleList.isEmpty()) {
            final Vector3D lastdXdYdZ = data.dXdYdZSampleList.getFirst();
            data.directionDeltaXZList.add(dXdYdZ.getXZAngleDiffWithVector(lastdXdYdZ));
        }
        data.dXdYdZSampleList.add(dXdYdZ);
    }

    /**
     * Used for debuging and testing
     */
    @SuppressWarnings("unused")
    private EntityPlayer getClosestPlayer() {
        EntityPlayer closestPlayer = null;
        double distance = 1000D;
        for (final EntityPlayer player : mc.theWorld.playerEntities) {
            if (player instanceof EntityPlayerSP || player.ticksExisted < 60 || player.capabilities.isFlying || player.capabilities.isCreativeMode || NameUtil.filterNPC(player.getUniqueID())) {
                continue;
            }
            final float distanceToEntity = mc.thePlayer.getDistanceToEntity(player);
            if (distanceToEntity < distance) {
                closestPlayer = player;
                distance = distanceToEntity;
            }
        }
        return closestPlayer;
    }

}
