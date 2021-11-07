package fr.alexdoru.megawallsenhancementsmod.gui;

import fr.alexdoru.megawallsenhancementsmod.config.MWEnConfigHandler;
import fr.alexdoru.nocheatersmod.NoCheatersMod;
import fr.alexdoru.nocheatersmod.events.NoCheatersEvents;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;

import java.io.IOException;

public class NoCheatersConfigGuiScreen extends MyGuiScreen {

    private final int ButtonsHeight = 20;
    private final GuiScreen parent;

    public NoCheatersConfigGuiScreen() {
        this.parent = null;
    }

    public NoCheatersConfigGuiScreen(GuiScreen parent) {
        this.parent = parent;
    }

    @Override
    public void initGui() {
        /*
         * Defines the button list
         */
        int buttonsWidth = 200;
        this.buttonList.add(new GuiButton(1, getxCenter() - buttonsWidth / 2, getyCenter() - ButtonsHeight / 2, buttonsWidth, ButtonsHeight, getButtonDisplayString(1)));
        this.buttonList.add(new GuiButton(2, getxCenter() - buttonsWidth / 2, getyCenter() - ButtonsHeight / 2 + (ButtonsHeight + 4), buttonsWidth, ButtonsHeight, getButtonDisplayString(2)));
        this.buttonList.add(new GuiButton(3, getxCenter() - 150 / 2, getyCenter() - ButtonsHeight / 2 + (ButtonsHeight + 4) * 4, 150, ButtonsHeight, getButtonDisplayString(3)));
        super.initGui();
    }

    private String getButtonDisplayString(int id) {
        switch (id) {
            case 1:
                return "Warning messages in chat : " + getSuffix(MWEnConfigHandler.togglewarnings);
            case 2:
                return "Autoreport cheaters : " + getSuffix(MWEnConfigHandler.toggleautoreport);
            case 3:
                return parent == null ? "Close" : "Done";
            default:
                return "invalid button id";
        }
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        switch (button.id) {
            case 1:
                MWEnConfigHandler.togglewarnings = !MWEnConfigHandler.togglewarnings;
                if (MWEnConfigHandler.togglewarnings) {
                    NoCheatersEvents.scanCurrentWorld();
                }
                break;
            case 2:
                MWEnConfigHandler.toggleautoreport = !MWEnConfigHandler.toggleautoreport;
                break;
            case 3:
                mc.displayGuiScreen(parent);
                break;
            default:
                break;
        }
        button.displayString = getButtonDisplayString(button.id);
        super.actionPerformed(button);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawCenteredTitle("NoCheaters v" + NoCheatersMod.version, 2, (width / 2.0f), getyCenter() - (ButtonsHeight + 4) * 6);
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    public void onGuiClosed() {
        MWEnConfigHandler.saveConfig();
        super.onGuiClosed();
    }

}
