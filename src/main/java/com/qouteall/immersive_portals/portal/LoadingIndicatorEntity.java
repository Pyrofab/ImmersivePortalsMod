package com.qouteall.immersive_portals.portal;

import com.qouteall.immersive_portals.McHelper;
import com.qouteall.immersive_portals.MyNetwork;
import net.fabricmc.fabric.api.entity.FabricEntityTypeBuilder;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityCategory;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Packet;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;

public class LoadingIndicatorEntity extends Entity {
    public static EntityType<LoadingIndicatorEntity> entityType;
    
    private static final TrackedData<String> text = DataTracker.registerData(
        LoadingIndicatorEntity.class, TrackedDataHandlerRegistry.STRING
    );
    
    public boolean isAlive = false;
    
    public static void init() {
        entityType = Registry.register(
            Registry.ENTITY_TYPE,
            new Identifier("immersive_portals", "loading_indicator"),
            FabricEntityTypeBuilder.create(
                EntityCategory.MISC,
                (EntityType.EntityFactory<LoadingIndicatorEntity>) LoadingIndicatorEntity::new
            ).size(
                new EntityDimensions(1, 1, true)
            ).build()
        );
    }
    
    public LoadingIndicatorEntity(World world) {
        this(entityType, world);
    }
    
    public LoadingIndicatorEntity(EntityType type, World world) {
        super(type, world);
    }
    
    @Override
    public Iterable<ItemStack> getArmorItems() {
        return null;
    }
    
    @Override
    public void tick() {
        super.tick();
    
        if (McHelper.getEntitiesNearby(this, Portal.class, 3).findAny().isPresent()) {
            this.remove();
        }
    }
    
    @Override
    protected void initDataTracker() {
        getDataTracker().startTracking(text, "Loading...");
    }
    
    @Override
    protected void readCustomDataFromTag(CompoundTag var1) {
    
    }
    
    @Override
    protected void writeCustomDataToTag(CompoundTag var1) {
    
    }
    
    @Override
    public Packet<?> createSpawnPacket() {
        return MyNetwork.createStcSpawnEntity(this);
    }
    
    public void setText(String str) {
        getDataTracker().set(text, str);
    }
    
    public String getText() {
        return getDataTracker().get(text);
    }
}
