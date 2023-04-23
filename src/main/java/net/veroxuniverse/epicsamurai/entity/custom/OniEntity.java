package net.veroxuniverse.epicsamurai.entity.custom;

import net.minecraft.core.BlockPos;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.animal.Cat;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import software.bernie.geckolib3.core.AnimationState;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.PlayState;
import software.bernie.geckolib3.core.builder.AnimationBuilder;
import software.bernie.geckolib3.core.builder.ILoopType;
import software.bernie.geckolib3.core.controller.AnimationController;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;
import software.bernie.geckolib3.core.manager.AnimationData;
import software.bernie.geckolib3.core.manager.AnimationFactory;
import software.bernie.geckolib3.util.GeckoLibUtil;

import javax.annotation.Nullable;

public class OniEntity extends Monster implements IAnimatable {

    private static final EntityDataAccessor<Boolean> STOMPING = SynchedEntityData.defineId(OniEntity.class, EntityDataSerializers.BOOLEAN);

    private final AnimationFactory FACTORY = GeckoLibUtil.createFactory(this);

    public OniEntity(EntityType<? extends Monster> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }

    public static AttributeSupplier setAttributes() {
        return Monster.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 100.0D)
                .add(Attributes.ATTACK_DAMAGE, 7.0f)
                .add(Attributes.ATTACK_SPEED, 1.6f)
                .add(Attributes.FOLLOW_RANGE, 16.0D)
                .add(Attributes.ATTACK_KNOCKBACK, 1D)
                .add(Attributes.MOVEMENT_SPEED, 0.20f).build();
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new FloatGoal(this));
        this.goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.3D, false));
        this.goalSelector.addGoal(4, new WaterAvoidingRandomStrollGoal(this, 1.0D));
        this.goalSelector.addGoal(5, new LookAtPlayerGoal(this, Player.class, 6.0F));
        this.goalSelector.addGoal(6, new RandomLookAroundGoal(this));

        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, AbstractVillager.class, false));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, IronGolem.class, true));
        this.targetSelector.addGoal(4, new NearestAttackableTargetGoal<>(this, Cat.class, true));
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(STOMPING, false);
    }

    public boolean isStomping() {
        return this.entityData.get(STOMPING);
    }

    @Override
    public void setTarget(@Nullable LivingEntity target) {
        super.setTarget(target);
        entityData.set(STOMPING, target != null);
    }

    public void setStomping(boolean stomping) {
        this.entityData.set(STOMPING, stomping);
    }

    private <E extends IAnimatable> PlayState predicate(AnimationEvent<E> event) {
        var builder = new AnimationBuilder();
        if (event.isMoving() && !this.isStomping()){
            event.getController().setAnimation((new AnimationBuilder().addAnimation("animation.oni.walk", ILoopType.EDefaultLoopTypes.LOOP)));
            return PlayState.CONTINUE;
        } else if (!event.isMoving() && !this.isStomping()) {
            event.getController().setAnimation((new AnimationBuilder().addAnimation("animation.oni.idle", ILoopType.EDefaultLoopTypes.LOOP)));
            return PlayState.CONTINUE;
        }
        if (builder.getRawAnimationList().isEmpty()) {
            event.getController().markNeedsReload();
            return PlayState.STOP;
        }
        event.getController().setAnimation(builder);
        return PlayState.CONTINUE;
    }

    private <eAttack extends IAnimatable> PlayState attackPredicate(AnimationEvent<eAttack> event) {
        var builder = new AnimationBuilder();
        if (this.isStomping()) {
            event.getController().setAnimationSpeed(1.0D);
            event.getController().setAnimation((new AnimationBuilder().addAnimation("animation.oni.attack", ILoopType.EDefaultLoopTypes.LOOP)));
            return PlayState.CONTINUE;
        }
        if (builder.getRawAnimationList().isEmpty()) {
            event.getController().markNeedsReload();
            return PlayState.STOP;
        }
            event.getController().setAnimation(builder);
            return PlayState.CONTINUE;
    }



    @Override
    public void registerControllers(AnimationData data) {
        data.addAnimationController(new AnimationController(this, "controller",
                25, this::predicate));
        data.addAnimationController(new AnimationController(this, "attackController",
                5, this::attackPredicate));
    }



    @Override
    public AnimationFactory getFactory() {
        return FACTORY;
    }

    protected void playStepSound(BlockPos pos, BlockState blockIn) {
        this.playSound(SoundEvents.RAVAGER_STEP, 0.15F, 1.0F);
    }

    protected SoundEvent getAmbientSound() {
        return SoundEvents.RAVAGER_AMBIENT;
    }

    protected SoundEvent getHurtSound(DamageSource damageSourceIn) {
        return SoundEvents.RAVAGER_HURT;
    }

    protected SoundEvent getDeathSound() {
        return SoundEvents.RAVAGER_DEATH;
    }

    protected float getSoundVolume() {
        return 0.2F;
    }

}