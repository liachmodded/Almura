/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) 2014 AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.pack;

import com.almuradev.almura.Almura;
import com.almuradev.almura.Configuration;
import com.almuradev.almura.Filesystem;
import com.flowpowered.cerealization.config.yaml.YamlConfiguration;
import net.malisis.core.renderer.icon.ClippedIcon;
import net.malisis.core.renderer.icon.MalisisIcon;
import net.minecraft.client.Minecraft;
import net.minecraft.util.IIcon;
import net.minecraft.util.ResourceLocation;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class PackUtil {

    public static Map<Integer, List<Integer>> extractCoordsFrom(YamlConfiguration reader) {
        final List<String> textureCoordinatesList = reader.getChild("Coords").getStringList();

        final Map<Integer, List<Integer>> textureCoordinatesByFace = new HashMap<>();

        for (int i = 0; i < textureCoordinatesList.size(); i++) {
            final String[] coordSplit = textureCoordinatesList.get(i).split(" ");

            final List<Integer> coords = new LinkedList<>();
            for (String coord : coordSplit) {
                coords.add(Integer.parseInt(coord));
            }

            textureCoordinatesByFace.put(i, coords);
        }

        return textureCoordinatesByFace;
    }

    public static ClippedIcon[] generateClippedIconsFromCoords(ContentPack pack, IIcon source, String textureName, Map<Integer, List<Integer>> texCoords) {
        final ClippedIcon[] clippedIcons = new ClippedIcon[texCoords.size()];
        Dimension dimension = null;

        try {
            dimension =
                    Filesystem.getImageDimension(Files.newInputStream(
                            Paths.get(Filesystem.CONFIG_PATH.toString(), "images" + File.separator + textureName + ".png")));
        } catch (IOException e) {
            if (Configuration.DEBUG_MODE || Configuration.DEBUG_PACKS_MODE) {
                Almura.LOGGER.error("Failed to load texture [" + textureName + "] for dimensions", e);
            }
        }

        if (dimension == null) {
            if (Configuration.DEBUG_MODE || Configuration.DEBUG_PACKS_MODE) {
                Almura.LOGGER.error("Failed to calculate the dimensions for texture [" + textureName + "]");
            }
        }

        if (dimension != null) {
            for (int i = 0; i < texCoords.size(); i++) {
                final List<Integer> coordList = texCoords.get(i);

                clippedIcons[i] =
                        new ClippedIcon((MalisisIcon) source, (float) (coordList.get(0) / dimension.getWidth()),
                                (float) (coordList.get(1) / dimension.getHeight()), (float) (coordList.get(2) / dimension.getWidth()),
                                (float) (coordList.get(3) / dimension.getHeight()));
            }
        }
        return clippedIcons;
    }

    public static boolean isEmpty(ClippedIcon[] value) {
        boolean isEmpty = true;

        if (value != null) {
            for (ClippedIcon icon : value) {
                if (icon != null) {
                    isEmpty = false;
                    break;
                }
            }
        }
        return isEmpty;
    }

    public static boolean isEmpty(IClipContainer container) {
        return isEmpty(container.getClipIcons());
    }
}
