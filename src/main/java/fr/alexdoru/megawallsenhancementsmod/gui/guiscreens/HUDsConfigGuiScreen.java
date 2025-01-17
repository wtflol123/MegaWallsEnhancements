package fr.alexdoru.megawallsenhancementsmod.gui.guiscreens;

import fr.alexdoru.megawallsenhancementsmod.config.ConfigHandler;
import fr.alexdoru.megawallsenhancementsmod.gui.guiapi.PositionEditGuiScreen;
import fr.alexdoru.megawallsenhancementsmod.gui.huds.*;
import fr.alexdoru.megawallsenhancementsmod.utils.SoundUtil;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.EnumChatFormatting;
import net.minecraftforge.fml.client.config.GuiSlider;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class HUDsConfigGuiScreen extends MyGuiScreen implements GuiSlider.ISlider {

    public HUDsConfigGuiScreen(GuiScreen parent) {
        this.parent = parent;
    }

    @Override
    public void initGui() {
        final int buttonsWidth = 200;
        final int sideButtonsWidth = 90;
        this.maxWidth = sideButtonsWidth * 2 + buttonsWidth + 4 * 2;
        this.maxHeight = (buttonsHeight + 4) * 11 + buttonsHeight;
        super.initGui();
        final int XposCenterButton = getxCenter() - buttonsWidth / 2;
        final int XposCenterLeftButton = getxCenter() - buttonsWidth / 2 - 4 - sideButtonsWidth;
        final int XposCenterRightButton = getxCenter() + buttonsWidth / 2 + 4;
        /* HUD Buttons */
        buttonList.add(new GuiButton(3, XposCenterButton, getButtonYPos(1), buttonsWidth, buttonsHeight, getButtonDisplayString(3)));
        buttonList.add(new GuiButton(2, XposCenterButton, getButtonYPos(2), buttonsWidth, buttonsHeight, getButtonDisplayString(2)));
        buttonList.add(new GuiButton(22, XposCenterButton, getButtonYPos(3), buttonsWidth, buttonsHeight, getButtonDisplayString(22)));
        buttonList.add(new GuiButton(9, XposCenterButton, getButtonYPos(4), buttonsWidth, buttonsHeight, getButtonDisplayString(9)));
        buttonList.add(new GuiButton(11, XposCenterButton, getButtonYPos(5), buttonsWidth, buttonsHeight, getButtonDisplayString(11)));
        buttonList.add(new GuiButton(15, XposCenterButton, getButtonYPos(6), buttonsWidth, buttonsHeight, getButtonDisplayString(15)));
        buttonList.add(new GuiButton(18, XposCenterButton, getButtonYPos(7), buttonsWidth, buttonsHeight, getButtonDisplayString(18)));
        buttonList.add(new GuiSlider(21, XposCenterButton, getButtonYPos(8), buttonsWidth, buttonsHeight, "Energy threshold : ", "", 1d, 160d, ConfigHandler.aquaEnergyDisplayThreshold, false, true, this));

        /* Buttons : Reset HUD position */
        buttonList.add(new GuiButton(8, XposCenterLeftButton, getButtonYPos(1), sideButtonsWidth, buttonsHeight, getButtonDisplayString(8)));
        buttonList.add(new GuiButton(7, XposCenterLeftButton, getButtonYPos(2), sideButtonsWidth, buttonsHeight, getButtonDisplayString(7)));
        buttonList.add(new GuiButton(23, XposCenterLeftButton, getButtonYPos(3), sideButtonsWidth, buttonsHeight, getButtonDisplayString(23)));
        buttonList.add(new GuiButton(14, XposCenterLeftButton, getButtonYPos(4), sideButtonsWidth, buttonsHeight, getButtonDisplayString(14)));
        buttonList.add(new GuiButton(12, XposCenterLeftButton, getButtonYPos(5), sideButtonsWidth, buttonsHeight, getButtonDisplayString(12)));
        buttonList.add(new GuiButton(19, XposCenterLeftButton, getButtonYPos(6), sideButtonsWidth, buttonsHeight, getButtonDisplayString(19)));
        buttonList.add(new GuiButton(16, XposCenterLeftButton, getButtonYPos(7), sideButtonsWidth, buttonsHeight, getButtonDisplayString(16)));
        /* Buttons : Move HUD */
        buttonList.add(new GuiButton(6, XposCenterRightButton, getButtonYPos(1), sideButtonsWidth, buttonsHeight, getButtonDisplayString(6)));
        buttonList.add(new GuiButton(5, XposCenterRightButton, getButtonYPos(2), sideButtonsWidth, buttonsHeight, getButtonDisplayString(5)));
        buttonList.add(new GuiButton(24, XposCenterRightButton, getButtonYPos(3), sideButtonsWidth, buttonsHeight, getButtonDisplayString(24)));
        buttonList.add(new GuiButton(13, XposCenterRightButton, getButtonYPos(4), sideButtonsWidth, buttonsHeight, getButtonDisplayString(13)));
        buttonList.add(new GuiButton(10, XposCenterRightButton, getButtonYPos(5), sideButtonsWidth, buttonsHeight, getButtonDisplayString(10)));
        buttonList.add(new GuiButton(17, XposCenterRightButton, getButtonYPos(6), sideButtonsWidth, buttonsHeight, getButtonDisplayString(17)));
        buttonList.add(new GuiButton(20, XposCenterRightButton, getButtonYPos(7), sideButtonsWidth, buttonsHeight, getButtonDisplayString(20)));

        /* Exit button */
        buttonList.add(new GuiButton(4, getxCenter() - 150 / 2, getButtonYPos(10), 150, buttonsHeight, getButtonDisplayString(4)));
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawCenteredTitle(EnumChatFormatting.DARK_PURPLE + "HUDs", 2, getxCenter(), getButtonYPos(-1));
        super.drawScreen(mouseX, mouseY, partialTicks);
        drawTooltips(mouseX, mouseY);
    }

    private String getButtonDisplayString(int id) {
        switch (id) {
            case 3:
                return "Arrow Hit HUD : " + getSuffix(ConfigHandler.showArrowHitHUD);
            case 2:
                return "/kill cooldown HUD : " + getSuffix(ConfigHandler.showKillCooldownHUD);
            case 22:
                return "Squad HUD : " + getSuffix(ConfigHandler.showSquadHUD);
            case 9:
                return "Strength HUD : " + getSuffix(ConfigHandler.showStrengthHUD);
            case 11:
                return "Wither death time HUD : " + (ConfigHandler.witherHUDinSidebar ? EnumChatFormatting.YELLOW + "in Sidebar" : getSuffix(ConfigHandler.showLastWitherHUD));
            case 15:
                return "Creeper primed TNT HUD : " + getSuffix(ConfigHandler.showPrimedTNTHUD);
            case 18:
                return "Energy display HUD : " + getSuffix(ConfigHandler.showEnergyDisplayHUD);
            case 4:
                return "Done";
            case 5:
            case 6:
            case 10:
            case 13:
            case 24:
            case 17:
            case 20:
                return "Move HUD";
            case 7:
            case 8:
            case 12:
            case 14:
            case 23:
            case 16:
            case 19:
                return "Reset position";
            default:
                return "no display text for this button id";
        }
    }

    @Override
    protected List<String> getTooltipText(int id) {
        final List<String> textLines = new ArrayList<>();
        switch (id) {
            case 2:
                textLines.add(EnumChatFormatting.GREEN + "Displays a HUD with the cooldown of the /kill command in Mega Walls");
                break;
            case 3:
                textLines.add(EnumChatFormatting.GREEN + "Displays a HUD with the health of your opponent on arrow hits");
                break;
            case 9:
                textLines.add(EnumChatFormatting.GREEN + "Displays a HUD when you get strenght with Dreadlord, Herobrine, Hunter and Zombie");
                break;
            case 11:
                textLines.add(EnumChatFormatting.GREEN + "Displays a HUD with the time it takes for the last wither to die");
                textLines.add(EnumChatFormatting.GREEN + "The HUD can be configured to appear in the Sidebar");
                break;
            case 22:
                textLines.add(EnumChatFormatting.GREEN + "Displays a mini tablist with just your squadmates");
                break;
            case 15:
                textLines.add(EnumChatFormatting.GREEN + "Displays a HUD with the cooldown of the primed TNT when playing Creeper");
                break;
            case 18:
                textLines.add(EnumChatFormatting.GREEN + "Displays a HUD with the amount of energy you have");
                textLines.add(EnumChatFormatting.GREEN + "Turns " + EnumChatFormatting.AQUA + EnumChatFormatting.BOLD + "aqua" + EnumChatFormatting.GREEN + " when your energy level exceeds the amount set below");
                break;
        }
        return textLines;
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        switch (button.id) {
            case 9:
                ConfigHandler.showStrengthHUD = !ConfigHandler.showStrengthHUD;
                if (ConfigHandler.showStrengthHUD) {
                    SoundUtil.playStrengthSound();
                }
                break;
            case 2:
                ConfigHandler.showKillCooldownHUD = !ConfigHandler.showKillCooldownHUD;
                break;
            case 3:
                ConfigHandler.showArrowHitHUD = !ConfigHandler.showArrowHitHUD;
                break;
            case 22:
                ConfigHandler.showSquadHUD = !ConfigHandler.showSquadHUD;
                break;
            case 11:
                if (ConfigHandler.showLastWitherHUD && !ConfigHandler.witherHUDinSidebar) {
                    ConfigHandler.witherHUDinSidebar = true;
                    break;
                }
                if (!ConfigHandler.showLastWitherHUD && !ConfigHandler.witherHUDinSidebar) {
                    ConfigHandler.showLastWitherHUD = true;
                    break;
                }
                ConfigHandler.witherHUDinSidebar = false;
                ConfigHandler.showLastWitherHUD = false;
                break;
            case 15:
                ConfigHandler.showPrimedTNTHUD = !ConfigHandler.showPrimedTNTHUD;
                break;
            case 18:
                ConfigHandler.showEnergyDisplayHUD = !ConfigHandler.showEnergyDisplayHUD;
                break;
            case 4:
                mc.displayGuiScreen(parent);
                break;
            case 5:
                mc.displayGuiScreen(new PositionEditGuiScreen(KillCooldownHUD.instance, this));
                break;
            case 6:
                mc.displayGuiScreen(new PositionEditGuiScreen(ArrowHitHUD.instance, this));
                break;
            case 10:
                mc.displayGuiScreen(new PositionEditGuiScreen(LastWitherHPHUD.instance, this));
                break;
            case 13:
                mc.displayGuiScreen(new PositionEditGuiScreen(HunterStrengthHUD.instance, this));
                break;
            case 24:
                mc.displayGuiScreen(new PositionEditGuiScreen(SquadHealthHUD.instance, this));
                break;
            case 17:
                mc.displayGuiScreen(new PositionEditGuiScreen(CreeperPrimedTNTHUD.instance, this));
                break;
            case 20:
                mc.displayGuiScreen(new PositionEditGuiScreen(EnergyDisplayHUD.instance, this));
                break;
            case 7:
                KillCooldownHUD.instance.guiPosition.setRelative(0d, 0d);
                break;
            case 8:
                ArrowHitHUD.instance.guiPosition.setRelative(0.5d, 9d / 20d);
                break;
            case 12:
                LastWitherHPHUD.instance.guiPosition.setRelative(0.75d, 0.05d);
                break;
            case 14:
                HunterStrengthHUD.instance.guiPosition.setRelative(0.5d, 8d / 20d);
                break;
            case 23:
                SquadHealthHUD.instance.guiPosition.setRelative(0.25d, 0.0d);
                break;
            case 16:
                CreeperPrimedTNTHUD.instance.guiPosition.setRelative(0.5d, 8d / 20d);
                break;
            case 19:
                EnergyDisplayHUD.instance.guiPosition.setRelative(0.5d, 10.5d / 20d);
                break;
            default:
                break;
        }
        button.displayString = getButtonDisplayString(button.id);
        super.actionPerformed(button);
    }

    @Override
    public void onChangeSliderValue(GuiSlider slider) {
        if (slider.id == 21) {
            ConfigHandler.aquaEnergyDisplayThreshold = (int) slider.getValue();
        }
    }

}
