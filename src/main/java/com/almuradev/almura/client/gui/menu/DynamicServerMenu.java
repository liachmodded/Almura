/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) 2014 - 2017 AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.client.gui.menu;

import com.almuradev.almura.client.gui.SimpleGui;
import com.almuradev.almura.client.gui.components.UIAnimatedBackground;
import com.almuradev.almura.client.gui.components.UIForm;
import com.almuradev.almura.util.Colors;
import com.almuradev.almura.util.Query;
import com.google.common.eventbus.Subscribe;
import cpw.mods.fml.client.FMLClientHandler;
import net.malisis.core.client.gui.Anchor;
import net.malisis.core.client.gui.GuiTexture;
import net.malisis.core.client.gui.component.decoration.UIImage;
import net.malisis.core.client.gui.component.decoration.UILabel;
import net.malisis.core.client.gui.component.interaction.UIButton;
import net.minecraft.client.gui.GuiMultiplayer;
import net.minecraft.client.gui.GuiYesNoCallback;
import net.minecraft.client.multiplayer.ServerData;

import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Timer;
import java.util.TimerTask;

public class DynamicServerMenu extends SimpleGui implements GuiYesNoCallback {

    private static final ServerData DATA_LIVE_SERVER = new ServerData("Almura", "srv1.almuramc.com");
    private static final ServerData DATA_DEV_SERVER = new ServerData("Almura (Dev)", "dev.almuramc.com");
    private static final Query QUERY_LIVE_SERVER = new Query(DATA_LIVE_SERVER, 25565), QUERY_DEV_SERVER = new Query(DATA_DEV_SERVER, 25565);
    private UIButton almuraLiveButton;
    private UIButton almuraDevButton;
    private UILabel liveServerOnline;
    private UILabel devServerOnline;
    Timer timer = null;
    boolean isRunning = false;

    public DynamicServerMenu(SimpleGui parent) {
        super(parent);
    }

    @Override
    public void construct() {

        // Create the form
        final UIForm form = new UIForm(this, 200, 225, "Multiplayer");
        form.setAnchor(Anchor.CENTER | Anchor.MIDDLE);

        // Create the logo
        final UIImage logoImage = new UIImage(this, new GuiTexture(DynamicMainMenu.ALMURA_LOGO_LOCATION), null);
        logoImage.setAnchor(Anchor.CENTER | Anchor.TOP);
        logoImage.setSize(55, 85);

        final int padding = 4;

        // Create the live Almura button
        almuraLiveButton = new UIButton(this, "Join ");
        almuraLiveButton.setPosition(-15, getPaddedY(logoImage, padding) + 3, Anchor.RIGHT | Anchor.TOP);
        almuraLiveButton.setSize(40, 16);
        almuraLiveButton.setName("button.server.almura.live");
        almuraLiveButton.setDisabled(true);
        almuraLiveButton.register(this);

        UILabel liveServerTitle = new UILabel(this, Colors.WHITE + "Public Server : ");
        liveServerTitle.setPosition(20, getPaddedY(logoImage, padding) + 7, Anchor.LEFT | Anchor.TOP);

        liveServerOnline = new UILabel(this, Colors.YELLOW + "Updating...");
        liveServerOnline.setPosition(85, liveServerTitle.getY(), Anchor.LEFT | Anchor.TOP);

        UILabel devServerTitle = new UILabel(this, Colors.WHITE + "Dev Server : ");
        devServerTitle.setPosition(26, getPaddedY(almuraLiveButton, padding) + 4, Anchor.LEFT | Anchor.TOP);

        devServerOnline = new UILabel(this, Colors.YELLOW + "Updating...");
        devServerOnline.setPosition(85, devServerTitle.getY(), Anchor.LEFT | Anchor.TOP);

        // Create the beta Almura button
        almuraDevButton = new UIButton(this, "Join ");
        almuraDevButton.setPosition(-15, getPaddedY(almuraLiveButton, padding) + 2, Anchor.RIGHT | Anchor.TOP);
        almuraDevButton.setSize(40, 16);
        almuraDevButton.setName("button.server.almura.dev");
        almuraDevButton.setDisabled(true);
        almuraDevButton.register(this);

        // Create the join another server button
        UIButton anotherButton = new UIButton(this, "Other Server");
        anotherButton.setPosition(-30, getPaddedY(almuraDevButton, padding) + 3, Anchor.CENTER | Anchor.TOP);
        anotherButton.setSize(60, 16);
        anotherButton.setName("button.server.another");
        anotherButton.register(this);

        // Create the map button
        UIButton mapButton = new UIButton(this, "Live Map");
        mapButton.setPosition(30, getPaddedY(almuraDevButton, padding) + 3, Anchor.CENTER | Anchor.TOP);
        mapButton.setSize(50, 16);
        mapButton.setName("button.server.map");
        mapButton.register(this);

        // Create the website button
        UIButton webButton = new UIButton(this, "Visit our Website for Updates");
        webButton.setPosition(0, getPaddedY(anotherButton, padding) + 4, Anchor.CENTER | Anchor.TOP);
        webButton.setSize(150, 16);
        webButton.setName("button.server.web");
        webButton.register(this);

        // Create the back button
        UIButton backButton = new UIButton(this, "Back");
        backButton.setPosition(0, -10, Anchor.CENTER | Anchor.BOTTOM);
        backButton.setSize(50, 16);
        backButton.setName("button.back");
        backButton.register(this);

        form.getContentContainer().add(logoImage, liveServerTitle, liveServerOnline, almuraLiveButton, devServerTitle,
                devServerOnline, almuraDevButton, anotherButton, mapButton, webButton, backButton);

        addToScreen(new UIAnimatedBackground(this));
        addToScreen(form);
    }

    @Override
    public void onClose() {
        // Future note, this window isn't technically closed when you try and connect to a server.  This is a chicken and egg issue.  Hence the need for the below update method to restart timer.
        super.onClose();
        isRunning = false;
        if (timer != null) {
            timer.cancel();
        }
    }

    @Subscribe
    public void onButtonClick(UIButton.ClickEvent event) throws IOException, URISyntaxException {
        if (!event.getComponent().getName().toLowerCase().equals("button.server.web")
                || (!event.getComponent().getName().toLowerCase().equals("button.server.map"))) {
            close();  //This doesn't close this screen when connecting to a server.
        }
        switch (event.getComponent().getName().toLowerCase()) {
            case "button.server.almura.live":
                FMLClientHandler.instance().setupServerList();
                FMLClientHandler.instance().connectToServer(this, DATA_LIVE_SERVER);
                break;
            case "button.server.almura.dev":
                FMLClientHandler.instance().setupServerList();
                FMLClientHandler.instance().connectToServer(this, DATA_DEV_SERVER);
                break;
            case "button.server.another":
                mc.displayGuiScreen(new GuiMultiplayer(this));
                break;
            case "button.server.map":
                Desktop.getDesktop().browse(new URI("http://srv1.almuramc.com:8123"));
                break;
            case "button.server.web":
                Desktop.getDesktop().browse(new URI("http://www.almuramc.com"));
                break;
        }
    }
    
    @Override
    public void update(int mouseX, int mouseY, float partialTick) {
        startTimer();
    }

    public void queryServers() {
        try {
            // Live Server
            if (QUERY_LIVE_SERVER.pingServer()) {
                QUERY_LIVE_SERVER.sendQuery();
                if (QUERY_LIVE_SERVER.getPlayers() == null || QUERY_LIVE_SERVER.getMaxPlayers() == null) {
                    liveServerOnline.setText(Colors.YELLOW + "Restarting...");
                } else {
                    liveServerOnline
                            .setText(Colors.GREEN + "Online " + Colors.BLUE + "(" + QUERY_LIVE_SERVER.getPlayers() + "/" + QUERY_LIVE_SERVER
                                    .getMaxPlayers() + ")");
                }
                almuraLiveButton.setDisabled(false);
            } else {
                liveServerOnline.setText(Colors.RED + "Offline");
                almuraLiveButton.setDisabled(true);
            }

            // Dev Server
            if (QUERY_DEV_SERVER.pingServer()) {
                QUERY_DEV_SERVER.sendQuery();
                if (QUERY_DEV_SERVER.getPlayers() == null || QUERY_DEV_SERVER.getMaxPlayers() == null) {
                    devServerOnline.setText(Colors.YELLOW + "Restarting...");
                } else {
                    devServerOnline
                            .setText(Colors.GREEN + "Online " + Colors.BLUE + "(" + QUERY_DEV_SERVER.getPlayers() + "/" + QUERY_DEV_SERVER
                                    .getMaxPlayers() + ")");
                }
                almuraDevButton.setDisabled(false);
            } else {
                devServerOnline.setText(Colors.RED + "Offline");
                almuraDevButton.setDisabled(true);
            }

        } catch (Exception ignored) {
        }
    }
    
    public void startTimer() {
        if (!isRunning) {
            timer = new Timer ();
            TimerTask updateListTask = new TimerTask () {
                @Override
                public void run () {
                    isRunning = true;
                    queryServers();
                }
            };
            timer.schedule(updateListTask, 0L, 3000L);
        }
    }
}
