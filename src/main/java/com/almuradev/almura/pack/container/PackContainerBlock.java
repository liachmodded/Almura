/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) 2014 - 2017 AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.pack.container;

import com.almuradev.almura.Almura;
import com.almuradev.almura.Configuration;
import com.almuradev.almura.pack.IBlockTextureContainer;
import com.almuradev.almura.pack.IBlockModelContainer;
import com.almuradev.almura.pack.IItemBlockInformation;
import com.almuradev.almura.pack.INodeContainer;
import com.almuradev.almura.pack.IPackObject;
import com.almuradev.almura.pack.Pack;
import com.almuradev.almura.pack.PackUtil;
import com.almuradev.almura.pack.RotationMeta;
import com.almuradev.almura.pack.mapper.GameObject;
import com.almuradev.almura.pack.model.PackModelContainer;
import com.almuradev.almura.pack.node.BreakNode;
import com.almuradev.almura.pack.node.CollisionNode;
import com.almuradev.almura.pack.node.ContainerNode;
import com.almuradev.almura.pack.node.INode;
import com.almuradev.almura.pack.node.LightNode;
import com.almuradev.almura.pack.node.RenderNode;
import com.almuradev.almura.pack.node.RotationNode;
import com.almuradev.almura.pack.node.ToolsNode;
import com.almuradev.almura.pack.node.container.StateProperty;
import com.almuradev.almura.pack.node.event.AddNodeEvent;
import com.almuradev.almura.pack.node.property.DropProperty;
import com.almuradev.almura.pack.node.property.RangeProperty;
import com.almuradev.almura.pack.renderer.PackIcon;
import com.almuradev.almura.tabs.Tabs;
import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.malisis.core.renderer.icon.ClippedIcon;
import net.malisis.core.util.EntityUtils;
import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.stats.StatList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.IIcon;
import net.minecraft.util.MathHelper;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.event.ForgeEventFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;

public class PackContainerBlock extends BlockContainer implements IPackObject, IBlockTextureContainer, IBlockModelContainer, INodeContainer,
        IItemBlockInformation {

    public static int renderId;
    private final Pack pack;
    private final String identifier;
    private Map<Integer, List<Integer>> textureCoordinates;
    private final String modelName;
    private final ConcurrentMap<Class<? extends INode<?>>, INode<?>> nodes = Maps.newConcurrentMap();
    private final String textureName;
    private final List<String> tooltip;
    private final RenderNode renderNode;
    private final RotationNode rotationNode;
    private final ContainerNode containerNode;
    private ClippedIcon[] clippedIcons;
    private Optional<PackModelContainer> modelContainer;
    private BreakNode breakNode;
    @SuppressWarnings("unused")
	private CollisionNode collisionNode;

    public PackContainerBlock(Pack pack, String identifier, List<String> tooltip, String textureName, Map<Integer, List<Integer>> textureCoordinates,
            String modelName,
            PackModelContainer modelContainer, float hardness, float resistance, boolean showInCreativeTab, String creativeTabName,
            RotationNode rotationNode, LightNode lightNode, RenderNode renderNode, ContainerNode containerNode) {
        super(Material.ground);
        this.pack = pack;
        this.identifier = identifier;
        this.textureCoordinates = textureCoordinates;
        this.modelName = modelName;
        this.textureName = textureName;
        this.renderNode = renderNode;
        this.rotationNode = rotationNode;
        this.containerNode = containerNode;
        this.tooltip = tooltip;
        setModelContainer(modelContainer);
        addNode(rotationNode);
        addNode(lightNode);
        addNode(renderNode);
        addNode(containerNode);
        setUnlocalizedName(pack.getName() + "\\" + identifier);
        setTextureName(Almura.MOD_ID + ":images/" + textureName);
        setHardness(hardness);
        setResistance(resistance);
        setLightLevel(lightNode.getEmission());
        setLightOpacity(lightNode.getOpacity());
        if (showInCreativeTab) {
            setCreativeTab(Tabs.getTabByName(creativeTabName));
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public int getRenderType() {
        return renderId;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerIcons(IIconRegister register) {
        blockIcon = new PackIcon(this, textureName).register((TextureMap) register);
        clippedIcons = PackUtil.generateClippedIconsFromCoordinates(blockIcon, textureName, textureCoordinates);
        for (StateProperty prop : containerNode.getValue()) {
            prop.registerIcons(register);
        }
    }

    @Override
    public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int p_149727_6_, float p_149727_7_, float p_149727_8_,
            float p_149727_9_) {
        if (!world.isRemote) {
            final TileEntity te = world.getTileEntity(x, y, z);
            if (te != null && te instanceof PackContainerTileEntity) {
                player.displayGUIChest((PackContainerTileEntity) te);
            }
        }
        return true;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public IIcon getIcon(int side, int type) {
        if (PackUtil.isEmptyClip(clippedIcons)) {
            return super.getIcon(side, type);
        }
        ClippedIcon sideIcon;
        if (side >= clippedIcons.length) {
            sideIcon = clippedIcons[0];
        } else {
            sideIcon = clippedIcons[side];
            if (sideIcon == null) {
                sideIcon = clippedIcons[0];
            }
        }
        return sideIcon;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public boolean renderAsNormalBlock() {
        return modelContainer == null && renderNode.getValue();
    }

    @Override
    public boolean isOpaqueCube() {
        return fullBlock;
    }

    //TODO Check this come 1.8
    @Override
    public void breakBlock(World world, int x, int y, int z, Block block, int metadata) {
        final TileEntity te = world.getTileEntity(x, y, z);
        if (te != null && te instanceof PackContainerTileEntity) {
            final PackContainerTileEntity pte = (PackContainerTileEntity) te;
            for (int i1 = 0; i1 < pte.getSizeInventory(); ++i1) {
                ItemStack itemstack = pte.getStackInSlot(i1);

                if (itemstack != null) {
                    float f = RangeProperty.RANDOM.nextFloat() * 0.8F + 0.1F;
                    float f1 = RangeProperty.RANDOM.nextFloat() * 0.8F + 0.1F;
                    EntityItem item;

                    for (float f2 = RangeProperty.RANDOM.nextFloat() * 0.8F + 0.1F; itemstack.stackSize > 0; world.spawnEntityInWorld(item)) {
                        int j1 = RangeProperty.RANDOM.nextInt(21) + 10;

                        if (j1 > itemstack.stackSize) {
                            j1 = itemstack.stackSize;
                        }

                        itemstack.stackSize -= j1;
                        item =
                                new EntityItem(world, (double) ((float) x + f), (double) ((float) y + f1), (double) ((float) z + f2),
                                        new ItemStack(itemstack.getItem(), j1, itemstack.getMetadata()));
                        float f3 = 0.05F;
                        item.motionX = (double) ((float) RangeProperty.RANDOM.nextGaussian() * f3);
                        item.motionY = (double) ((float) RangeProperty.RANDOM.nextGaussian() * f3 + 0.2F);
                        item.motionZ = (double) ((float) RangeProperty.RANDOM.nextGaussian() * f3);

                        if (itemstack.hasTagCompound()) {
                            item.getEntityItem().setTagCompound((NBTTagCompound) itemstack.getTagCompound().copy());
                        }
                    }
                }
            }
        }

        super.breakBlock(world, x, y, z, block, metadata);
    }

    //TODO Check this come 1.8
    @Override
    public void harvestBlock(World world, EntityPlayer player, int x, int y, int z, int metadata) {
        player.addStat(StatList.mineBlockStatArray[getIdFromBlock(this)], 1);
        final ItemStack held = player.getHeldItem();
        ToolsNode found = null;

        if (breakNode.isEnabled()) {
            for (ToolsNode toolsNode : breakNode.getValue()) {
                if (toolsNode instanceof ToolsNode.OffHand) {
                    if (held == null) {
                        found = toolsNode;
                        break;
                    }
                    continue;
                }
                if (held != null && toolsNode.getTool().minecraftObject == held.getItem()) {
                    found = toolsNode;
                    break;
                }
            }

            if (found == null) {
                found = breakNode.getToolByIdentifier("", "none");
                if (found == null) {
                    return;
                }
            }
        } else {
            return;
        }

        player.addExhaustion(found.getExhaustionRange().getValueWithinRange());
        final ArrayList<ItemStack> drops = Lists.newArrayList();
        for (DropProperty src : found.getValue().getValue()) {
            final GameObject source = src.getSource();
            final ItemStack toDrop;
            if (source.isBlock()) {
                toDrop = new ItemStack((Block) source.minecraftObject, src.getAmountProperty().getValueWithinRange(), src.getData());
            } else {
                toDrop = new ItemStack((Item) source.minecraftObject, src.getAmountProperty().getValueWithinRange(), src.getData());
            }
            if (src.getBonusProperty().getSource()) {
                final double chance = src.getBonusProperty().getValueWithinRange();
                if (RangeProperty.RANDOM.nextDouble() <= (chance / 100)) {
                    toDrop.stackSize += src.getBonusProperty().getValueWithinRange();
                }
            }
            drops.add(toDrop);
        }
        harvesters.set(player);
        if (!world.isRemote && !world.restoringBlockSnapshots) {
            final int fortune = EnchantmentHelper.getFortuneModifier(player);
            final float
                    modchance =
                    ForgeEventFactory.fireBlockHarvesting(drops, world, this, x, y, z, metadata, fortune, 1.0f, false, harvesters.get());
            for (ItemStack is : drops) {
                if (RangeProperty.RANDOM.nextFloat() <= modchance && world.getGameRules().getGameRuleBooleanValue("doTileDrops")) {
                    if (captureDrops.get()) {
                        capturedDrops.get().add(is);
                        return;
                    }
                    final float f = 0.7F;
                    final double d0 = (double) (world.rand.nextFloat() * f) + (double) (1.0F - f) * 0.5D;
                    final double d1 = (double) (world.rand.nextFloat() * f) + (double) (1.0F - f) * 0.5D;
                    final double d2 = (double) (world.rand.nextFloat() * f) + (double) (1.0F - f) * 0.5D;
                    final EntityItem item = new EntityItem(world, (double) x + d0, (double) y + d1, (double) z + d2, is);
                    item.delayBeforeCanPickup = 10;
                    world.spawnEntityInWorld(item);
                }
            }
        }
        harvesters.set(null);
    }

    @Override
    public void onBlockPlacedBy(World world, int x, int y, int z, EntityLivingBase entity, ItemStack item) {
        if (rotationNode.isEnabled() && (rotationNode.isDefaultRotate() || rotationNode.isDefaultMirrorRotate())) {
            final ForgeDirection playerDir = EntityUtils.getEntityFacing(entity, false);

            if (rotationNode.isDefaultMirrorRotate()) {
                final ForgeDirection cameraDir = EntityUtils.getEntityFacing(entity, true);
                world.setBlockMetadataWithNotify(x, y, z, RotationMeta.Rotation.getState(cameraDir, playerDir).getId(), 3);
            } else {
                world.setBlockMetadataWithNotify(x, y, z, RotationMeta.Rotation.getState(ForgeDirection.NORTH, playerDir).getId(), 3);
            }
        }
    }

    @Override
    public AxisAlignedBB getCollisionBoundingBoxFromPool(World world, int x, int y, int z) {
        AxisAlignedBB collisionBoundingBox = super.getCollisionBoundingBoxFromPool(world, x, y, z);

        if (modelContainer.isPresent()) {
            collisionBoundingBox = modelContainer.get().getPhysics().getCollision(collisionBoundingBox, world, x, y, z);
            final TileEntity te = world.getTileEntity(x, y, z);
            if (te != null && te instanceof PackContainerTileEntity) {
                Optional<StateProperty> property = null;

                if (((PackContainerTileEntity) te).isFull()) {
                    property = containerNode.getByIdentifier("full");
                } else if (!((PackContainerTileEntity) te).isEmpty()) {
                    property = containerNode.getByIdentifier("has-contents");
                }

                if (property != null && property.isPresent()) {
                    final Optional<PackModelContainer> propModelContainer = property.get().getModelContainer();
                    if (propModelContainer.isPresent()) {
                        collisionBoundingBox =
                                propModelContainer.get().getPhysics().getCollision(collisionBoundingBox, world, x,
                                        y, z);
                    }
                }
            }
        }

        return collisionBoundingBox;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public AxisAlignedBB getSelectedBoundingBoxFromPool(World world, int x, int y, int z) {
        AxisAlignedBB wireframeBoundingBox = super.getSelectedBoundingBoxFromPool(world, x, y, z);

        if (modelContainer.isPresent()) {
            wireframeBoundingBox = modelContainer.get().getPhysics().getWireframe(wireframeBoundingBox, world, x, y, z);
            final TileEntity te = world.getTileEntity(x, y, z);
            if (te != null && te instanceof PackContainerTileEntity) {
                Optional<StateProperty> property = null;

                if (((PackContainerTileEntity) te).isFull()) {
                    property = containerNode.getByIdentifier("full");
                } else if (!((PackContainerTileEntity) te).isEmpty()) {
                    property = containerNode.getByIdentifier("has-contents");
                }

                if (property != null && property.isPresent()) {
                    final Optional<PackModelContainer> propModelContainer = property.get().getModelContainer();
                    if (propModelContainer.isPresent()) {
                        wireframeBoundingBox =
                                propModelContainer.get().getPhysics().getWireframe(wireframeBoundingBox, world, x,
                                        y, z);
                    }
                }
            }
        }

        return wireframeBoundingBox;
    }

    @Override
    public boolean canProvidePower() {
        return false;
    }

    @Override
    public int isProvidingWeakPower(IBlockAccess access, int x, int y, int z, int metadata) {
        if (!this.canProvidePower()) {
            return 0;
        } else {
            return MathHelper.clamp_int(0, 0, 15);
        }
    }

    @Override
    public int isProvidingStrongPower(IBlockAccess access, int x, int y, int z, int metadata) {
        return isProvidingWeakPower(access, x, y, z, metadata);
    }

    @Override
    public boolean hasComparatorInputOverride() {
        return true;
    }

    @Override
    public int getComparatorInputOverride(World world, int x, int y, int z, int metadata) {
        final TileEntity te = world.getTileEntity(x, y, z);
        if (te != null && te instanceof PackContainerTileEntity) {
            return Container.calcRedstoneFromInventory((PackContainerTileEntity) te);
        }
        return super.getComparatorInputOverride(world, x, y, z, metadata);
    }

    @Override
    public ClippedIcon[] getClipIcons(IBlockAccess access, int x, int y, int z, int metadata) {
        if (access != null) {
            if (containerNode != null) {
                final TileEntity te = access.getTileEntity(x, y, z);
                if (te != null && te instanceof PackContainerTileEntity) {
                    Optional<StateProperty> property = null;

                    if (((PackContainerTileEntity) te).isFull()) {
                        property = containerNode.getByIdentifier("full");
                    } else if (!((PackContainerTileEntity) te).isEmpty()) {
                        property = containerNode.getByIdentifier("has-contents");
                    }

                    if (property != null && property.isPresent()) {
                        return property.get().getClipIcons(access, x, y, z, metadata);
                    }
                }
            }
        }
        return clippedIcons;
    }

    @Override
    public Optional<PackModelContainer> getModelContainer(IBlockAccess access, int x, int y, int z, int metadata) {
        if (access != null) {
            if (containerNode != null) {
                final TileEntity te = access.getTileEntity(x, y, z);
                if (te != null && te instanceof PackContainerTileEntity) {
                    Optional<StateProperty> property = null;

                    if (((PackContainerTileEntity) te).isFull()) {
                        property = containerNode.getByIdentifier("full");
                    } else if (!((PackContainerTileEntity) te).isEmpty()) {
                        property = containerNode.getByIdentifier("has-contents");
                    }

                    if (property != null && property.isPresent()) {
                        return property.get().getModelContainer(access, x, y, z, metadata);
                    }
                }
            }
        }
        return modelContainer;
    }

    @Override
    public Map<Integer, List<Integer>> getTextureCoordinates() {
        return textureCoordinates;
    }

    @Override
    public void setTextureCoordinates(Map<Integer, List<Integer>> coordinates) {
        this.textureCoordinates = coordinates;
    }

    @Override
    public ClippedIcon[] getClipIcons() {
        return clippedIcons;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends INode<?>> T addNode(T node) {
        nodes.put((Class<? extends INode<?>>) node.getClass(), node);
        if (node.getClass() == BreakNode.class) {
            breakNode = (BreakNode) node;
        } else if (node.getClass() == CollisionNode.class) {
            collisionNode = (CollisionNode) node;
        }
        MinecraftForge.EVENT_BUS.post(new AddNodeEvent(this, node));
        return node;
    }

    @Override
    public void addNodes(INode<?>... nodes) {
        for (INode<?> node : nodes) {
            addNode(node);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends INode<?>> T getNode(Class<T> clazz) {
        return (T) nodes.get(clazz);
    }

    @Override
    public <T extends INode<?>> boolean hasNode(Class<T> clazz) {
        return getNode(clazz) != null;
    }

    @Override
    public Pack getPack() {
        return pack;
    }

    @Override
    public String getIdentifier() {
        return identifier;
    }

    @Override
    public Optional<PackModelContainer> getModelContainer() {
        return modelContainer;
    }

    @Override
    public void setModelContainer(PackModelContainer modelContainer) {
        this.modelContainer = Optional.fromNullable(modelContainer);

        if (Configuration.IS_CLIENT && this.modelContainer.isPresent()) {
            if (this.modelContainer.get().getModel().isPresent()) {
                fullBlock = false;
            } else {
                fullBlock = renderNode.isOpaque();
            }
        }
    }

    @Override
    public String getModelName() {
        return modelName;
    }

    @Override
    public String getTextureName() {
        return textureName;
    }

    @Override
    public TileEntity createNewTileEntity(World world, int metadata) {
        return new PackContainerTileEntity(containerNode);
    }

    @Override
    public String toString() {
        return "PackContainerBlock {pack= " + pack.getName() + ", registry_name= " + pack.getName() + "\\" + identifier + "}";
    }

    @Override
    public List<String> getTooltip() {
        return tooltip;
    }
}
