/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) 2014 AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.client.gui.ingame;

import com.almuradev.almura.client.ChatColor;
import com.almuradev.almura.client.gui.AlmuraGui;
import com.almuradev.almura.pack.IModelContainer;
import com.almuradev.almura.pack.IPackObject;
import com.almuradev.almura.pack.block.PackBlock;
import com.almuradev.almura.pack.crop.PackCrops;
import com.almuradev.almura.pack.crop.Stage;
import com.google.common.eventbus.Subscribe;

import net.malisis.core.client.gui.Anchor;
import net.malisis.core.client.gui.GuiTexture;
import net.malisis.core.client.gui.component.container.UIBackgroundContainer;
import net.malisis.core.client.gui.component.control.UIMoveHandle;
import net.malisis.core.client.gui.component.decoration.UIImage;
import net.malisis.core.client.gui.component.decoration.UILabel;
import net.malisis.core.client.gui.component.interaction.UIButton;
import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;

import java.text.DecimalFormat;

public class IngameBlockInformation extends AlmuraGui {

    private final Block block;
    private final int metadata;
    private final float hardness;
    private UILabel localizedNameLabel, unlocalizedNameLabel, soundLabel, metadataLabel, lightOpacityLabel, lightValueLabel, hardnessLabel, blockBoundsLabel, textureNameLabel, modelNameLabel, harvestToolLabel;
    private IModelContainer customBlock;
    

    /**
     * Creates an gui with a parent screen and calls {@link AlmuraGui#setup}, if the parent is null then no background will be added

     * @param parent the {@link AlmuraGui} that we came from
     */
    public IngameBlockInformation(AlmuraGui parent, Block block, int metadata, float hardness) {
        super(parent);
        this.block = block;
        this.metadata = metadata;
        this.hardness = hardness;
        setup();
    }

    @Override
    protected void setup() {
        guiscreenBackground = false;
        
        final int xPadding = 10;
        final int yPadding = 1;
        int windowHeight = 125;
        int windowWidth = 150;
        
        final UIBackgroundContainer window = new UIBackgroundContainer(this, windowWidth, windowHeight);
        window.setAnchor(Anchor.CENTER | Anchor.MIDDLE);
        window.setColor(Integer.MIN_VALUE);
        window.setBackgroundAlpha(200);
        
        final DecimalFormat decimal = new DecimalFormat("#.##");

        final UIImage blockImage = new UIImage(this, new GuiTexture(UIImage.BLOCKS_TEXTURE), block.getIcon(0, metadata));
        blockImage.setPosition(xPadding, yPadding, Anchor.LEFT | Anchor.TOP);

        final String localized = block.getLocalizedName();
        localizedNameLabel = new UILabel(this, ChatColor.WHITE + getFormattedString(localized, 22, "..."));
        localizedNameLabel.setPosition(getPaddedX(blockImage, xPadding), yPadding + 3, Anchor.LEFT | Anchor.TOP);

        unlocalizedNameLabel = new UILabel(this, ChatColor.GRAY + getFormattedString(block.getUnlocalizedName(), 60, "..."));
        unlocalizedNameLabel.setPosition(getPaddedX(blockImage, xPadding), getPaddedY(localizedNameLabel, 0), Anchor.LEFT | Anchor.TOP);
        unlocalizedNameLabel.setFontScale(0.5f);

        soundLabel = new UILabel(this, ChatColor.GRAY + "Step sound: " + ChatColor.BLUE + block.stepSound.soundName);
        soundLabel.setPosition(xPadding, getPaddedY(unlocalizedNameLabel, yPadding), Anchor.LEFT | Anchor.TOP);

        metadataLabel = new UILabel(this, ChatColor.GRAY + "Metadata: " + ChatColor.BLUE + metadata);
        metadataLabel.setPosition(xPadding, getPaddedY(soundLabel, yPadding), Anchor.LEFT | Anchor.TOP);

        lightOpacityLabel = new UILabel(this, ChatColor.GRAY + "Light opacity: " + ChatColor.BLUE + block.getLightOpacity());
        lightOpacityLabel.setPosition(xPadding, getPaddedY(metadataLabel, yPadding), Anchor.LEFT | Anchor.TOP);

        lightValueLabel = new UILabel(this, ChatColor.GRAY + "Light value: " + ChatColor.BLUE + block.getLightValue());
        lightValueLabel.setPosition(xPadding, getPaddedY(lightOpacityLabel, yPadding), Anchor.LEFT | Anchor.TOP);

        hardnessLabel = new UILabel(this, ChatColor.GRAY + "Hardness: " + ChatColor.BLUE + decimal.format(hardness));
        hardnessLabel.setPosition(xPadding, getPaddedY(lightValueLabel, yPadding), Anchor.LEFT | Anchor.TOP);        

        blockBoundsLabel = new UILabel(this, ChatColor.GRAY + String.format("Block bounds: %1$sx%2$sx%3$s", ChatColor.BLUE + decimal.format(block.getBlockBoundsMaxX()), decimal.format(block.getBlockBoundsMaxY()), decimal.format(block.getBlockBoundsMaxZ())));
        blockBoundsLabel.setPosition(xPadding, getPaddedY(hardnessLabel, yPadding), Anchor.LEFT | Anchor.TOP);      
                       
        if (block instanceof IModelContainer) {

            IModelContainer customBlock = (IModelContainer) block;

            if (customBlock != null) {
                textureNameLabel = new UILabel(this, ChatColor.GRAY + "Texture Name: " + ChatColor.BLUE + customBlock.getTextureName());
                textureNameLabel.setPosition(xPadding, getPaddedY(blockBoundsLabel, yPadding), Anchor.LEFT | Anchor.TOP);
                window.add(textureNameLabel);

                if (block instanceof PackCrops) {
                    final PackCrops crop = (PackCrops) block;
                    final Stage stage = crop.getStages().get(metadata);
                    modelNameLabel = new UILabel(this, ChatColor.GRAY + "Model Name: " + ChatColor.BLUE + stage.getModelName());
                } else {
                    modelNameLabel = new UILabel(this, ChatColor.GRAY + "Model Name: " + ChatColor.BLUE + customBlock.getModelName());
                }
                modelNameLabel.setPosition(xPadding, getPaddedY(textureNameLabel, yPadding), Anchor.LEFT | Anchor.TOP);
                window.add(modelNameLabel);

                if (modelNameLabel.getWidth() >= windowWidth) {
                    windowWidth = modelNameLabel.getWidth() + 20;
                } else if (textureNameLabel.getWidth() >= windowWidth) {
                    windowWidth = textureNameLabel.getWidth() + 20;
                }

                windowHeight = windowHeight+25;
            }
        }        
        
        final String harvestTool = block.getHarvestTool(metadata);
        if (harvestTool != null && !harvestTool.isEmpty()) {
            harvestToolLabel = new UILabel(this, ChatColor.GRAY + "Harvest tool: " + harvestTool);
            if (block instanceof PackBlock) {                
                harvestToolLabel.setPosition(xPadding, getPaddedY(modelNameLabel, yPadding), Anchor.LEFT | Anchor.TOP);                
            } else {                
                harvestToolLabel.setPosition(xPadding, getPaddedY(blockBoundsLabel, yPadding), Anchor.LEFT | Anchor.TOP);
            }
            window.add(harvestToolLabel);
        }

        final UIButton closeButton = new UIButton(this, "Close");
        closeButton.setSize(50, 14);
        closeButton.setPosition(-4, -4, Anchor.RIGHT | Anchor.BOTTOM);
        closeButton.setName("button.close");
        closeButton.register(this);

        window.setSize(windowWidth, windowHeight);
        
        window.add(blockImage, localizedNameLabel, unlocalizedNameLabel, soundLabel, metadataLabel, lightOpacityLabel, lightValueLabel, hardnessLabel,
                   blockBoundsLabel, closeButton);

        new UIMoveHandle(this, window);

        addToScreen(window);
    }

    @Subscribe
    public void onButtonClick(UIButton.ClickEvent event) {
        switch (event.getComponent().getName().toLowerCase()) {
            case "button.close":
                close();
                break;
        }
    }

    private static String getFormattedString(String raw, int length, String suffix) {
        if (raw.trim().length() <= length) {
            return raw;
        } else {
            return raw.substring(0, Math.min(raw.length(), length)).trim() + suffix;
        }
    }
}
