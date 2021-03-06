package com.qouteall.immersive_portals.portal.nether_portal;

import com.qouteall.immersive_portals.Helper;
import com.qouteall.immersive_portals.McHelper;
import com.qouteall.immersive_portals.portal.Portal;
import com.qouteall.immersive_portals.portal.PortalPlaceholderBlock;
import net.fabricmc.fabric.api.entity.FabricEntityTypeBuilder;
import net.minecraft.block.Blocks;
import net.minecraft.entity.EntityCategory;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;

import java.util.UUID;

public class NewNetherPortalEntity extends Portal {
    public static EntityType<NewNetherPortalEntity> entityType;
    
    public NetherPortalShape netherPortalShape;
    public UUID reversePortalId;
    public boolean unbreakable = false;
    
    private boolean isNotified = true;
    private boolean shouldBreakNetherPortal = false;
    
    public NewNetherPortalEntity(
        EntityType<?> entityType_1,
        World world_1
    ) {
        super(entityType_1, world_1);
    }
    
    public static void init() {
        entityType = Registry.register(
            Registry.ENTITY_TYPE,
            new Identifier("immersive_portals", "nether_portal_new"),
            FabricEntityTypeBuilder.create(
                EntityCategory.MISC,
                (EntityType<NewNetherPortalEntity> type, World world1) ->
                    new NewNetherPortalEntity(type, world1)
            ).size(
                new EntityDimensions(1, 1, true)
            ).setImmuneToFire().build()
        );
        
        PortalPlaceholderBlock.portalBlockUpdateSignal.connect((world, pos) -> {
            McHelper.getEntitiesNearby(
                world,
                new Vec3d(pos),
                NewNetherPortalEntity.class,
                20
            ).forEach(
                NewNetherPortalEntity::notifyToCheckIntegrity
            );
        });
    }
    
    @Override
    public boolean isPortalValid() {
        if (world.isClient) {
            return super.isPortalValid();
        }
        return super.isPortalValid() && netherPortalShape != null && reversePortalId != null;
    }
    
    @Override
    protected void readCustomDataFromTag(CompoundTag compoundTag) {
        super.readCustomDataFromTag(compoundTag);
        if (compoundTag.containsKey("netherPortalShape")) {
            netherPortalShape = new NetherPortalShape(compoundTag.getCompound("netherPortalShape"));
        }
        reversePortalId = compoundTag.getUuid("reversePortalId");
        unbreakable = compoundTag.getBoolean("unbreakable");
    }
    
    @Override
    protected void writeCustomDataToTag(CompoundTag compoundTag) {
        super.writeCustomDataToTag(compoundTag);
        if (netherPortalShape != null) {
            compoundTag.put("netherPortalShape", netherPortalShape.toTag());
        }
        compoundTag.putUuid("reversePortalId", reversePortalId);
        compoundTag.putBoolean("unbreakable", unbreakable);
    }
    
    
    private void breakPortalOnThisSide() {
        assert shouldBreakNetherPortal;
        assert !removed;
        
        netherPortalShape.area.forEach(
            blockPos -> world.setBlockState(
                blockPos, Blocks.AIR.getDefaultState()
            )
        );
        this.remove();
        
        Helper.log("Broke " + this);
    }
    
    private void notifyToCheckIntegrity() {
        isNotified = true;
    }
    
    private NewNetherPortalEntity getReversePortal() {
        assert !world.isClient;
        
        ServerWorld world = getServer().getWorld(dimensionTo);
        return (NewNetherPortalEntity) world.getEntity(reversePortalId);
    }
    
    @Override
    public void tick() {
        super.tick();
        
        if (world.isClient) {
            return;
        }
        if (unbreakable) {
            return;
        }
        
        if (isNotified) {
            isNotified = false;
            checkPortalIntegrity();
        }
        if (shouldBreakNetherPortal) {
            breakPortalOnThisSide();
        }
    }
    
    private void checkPortalIntegrity() {
        assert !world.isClient;
        
        if (!isPortalValid()) {
            remove();
            return;
        }
        
        if (!isPortalIntactOnThisSide()) {
            shouldBreakNetherPortal = true;
            NewNetherPortalEntity reversePortal = getReversePortal();
            if (reversePortal != null) {
                reversePortal.shouldBreakNetherPortal = true;
            }
        }
    }
    
    private boolean isPortalIntactOnThisSide() {
        assert McHelper.getServer() != null;
        
        return netherPortalShape.area.stream()
            .allMatch(blockPos ->
                world.getBlockState(blockPos).getBlock() == PortalPlaceholderBlock.instance
            ) &&
            netherPortalShape.frameAreaWithoutCorner.stream()
                .allMatch(blockPos ->
                    world.getBlockState(blockPos).getBlock() == Blocks.OBSIDIAN
                );
    }
}
