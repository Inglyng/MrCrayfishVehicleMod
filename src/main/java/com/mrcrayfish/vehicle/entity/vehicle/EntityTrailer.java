package com.mrcrayfish.vehicle.entity.vehicle;

import com.mrcrayfish.vehicle.entity.EntityLandVehicle;
import com.mrcrayfish.vehicle.entity.EntityVehicle;
import com.mrcrayfish.vehicle.init.ModItems;
import net.minecraft.entity.Entity;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;

/**
 * Author: MrCrayfish
 */
public abstract class EntityTrailer extends EntityVehicle
{
    private static final DataParameter<Integer> PULLING_ENTITY = EntityDataManager.createKey(EntityTrailer.class, DataSerializers.VARINT);

    private Entity pullingEntity;

    public float wheelRotation;
    public float prevWheelRotation;

    public EntityTrailer(World worldIn)
    {
        super(worldIn);
    }

    @Override
    protected void entityInit()
    {
        super.entityInit();
        this.dataManager.register(PULLING_ENTITY, -1);
    }

    @Override
    public void onClientInit()
    {
        body = new ItemStack(ModItems.TRAILER_BODY);
        wheel = new ItemStack(ModItems.WHEEL);
    }

    @Override
    public void onUpdateVehicle()
    {
        prevWheelRotation = wheelRotation;

        if(this.pullingEntity != null)
        {
            if(this.pullingEntity.isDead || (this.pullingEntity instanceof EntityLandVehicle && ((EntityLandVehicle) this.pullingEntity).getTrailer() != this))
            {
                this.pullingEntity = null;
                return;
            }

            this.updatePullingMotion();
        }
        else if(!world.isRemote)
        {
            this.motionX *= 0.75;
            this.motionY -= 0.08;
            this.motionZ *= 0.75;
            this.move(MoverType.SELF, this.motionX, this.motionY, this.motionZ);
        }

        float speed = (float) (Math.sqrt(Math.pow(this.posX - this.prevPosX, 2) + Math.pow(this.posY - this.prevPosY, 2) + Math.pow(this.posZ - this.prevPosZ, 2)) * 20);
        wheelRotation -= 90F * (speed / 10F);
    }

    public void updatePullingMotion()
    {
        Vec3d towBar = pullingEntity.getPositionVector();
        if(pullingEntity instanceof EntityLandVehicle)
        {
            EntityLandVehicle landVehicle = (EntityLandVehicle) pullingEntity;
            Vec3d towBarVec = landVehicle.getTowBarVec();
            towBarVec = new Vec3d(towBarVec.x, towBarVec.y, towBarVec.z + landVehicle.getBodyPosition().getZ() - 0.025);
            towBar = towBar.add(towBarVec.rotateYaw((float) Math.toRadians(-landVehicle.rotationYaw + landVehicle.additionalYaw)));
        }

        this.rotationYaw = (float) Math.toDegrees(Math.atan2(towBar.z - this.posZ, towBar.x - this.posX) - Math.toRadians(90F));
        double deltaRot = (double) (this.prevRotationYaw - this.rotationYaw);
        if (deltaRot < -180.0D)
        {
            this.prevRotationYaw += 360.0F;
        }
        else if (deltaRot >= 180.0D)
        {
            this.prevRotationYaw -= 360.0F;
        }

        Vec3d vec = new Vec3d(0, 0, -25 * 0.0625).rotateYaw((float) Math.toRadians(-this.rotationYaw)).add(towBar); //TOWING POS
        this.setPosition(vec.x, vec.y, vec.z);
        this.motionX = vec.x - this.posX;
        this.motionY = towBar.y - this.posY;
        this.motionZ = vec.z - this.posZ;
        this.move(MoverType.SELF, this.motionX, this.motionY, this.motionZ);
    }

    @Override
    public double getMountedYOffset()
    {
        return 0.0;
    }

    public boolean setPullingEntity(Entity pullingEntity)
    {
        if(pullingEntity instanceof EntityPlayer || (pullingEntity instanceof EntityLandVehicle && pullingEntity.getRidingEntity() == null && ((EntityLandVehicle) pullingEntity).canTowTrailer()))
        {
            this.pullingEntity = pullingEntity;
            this.dataManager.set(PULLING_ENTITY, pullingEntity.getEntityId());
            return true;
        }
        else
        {
            this.pullingEntity = null;
            this.dataManager.set(PULLING_ENTITY, -1);
            return false;
        }
    }

    @Nullable
    public Entity getPullingEntity()
    {
        return pullingEntity;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void setPositionAndRotationDirect(double x, double y, double z, float yaw, float pitch, int posRotationIncrements, boolean teleport)
    {
        this.lerpX = x;
        this.lerpY = y;
        this.lerpZ = z;
        this.lerpYaw = (double) yaw;
        this.lerpPitch = (double) pitch;
        this.lerpSteps = 1;
    }

    @Override
    public boolean canMountTrailer()
    {
        return false;
    }

    @Override
    public void notifyDataManagerChange(DataParameter<?> key)
    {
        super.notifyDataManagerChange(key);
        if(world.isRemote)
        {
            if(PULLING_ENTITY.equals(key))
            {
                int entityId = this.dataManager.get(PULLING_ENTITY);
                if(entityId != -1)
                {
                    Entity entity = world.getEntityByID(this.dataManager.get(PULLING_ENTITY));
                    if(entity instanceof EntityPlayer || (entity instanceof EntityLandVehicle && ((EntityLandVehicle) entity).canTowTrailer()))
                    {
                        pullingEntity = entity;
                    }
                    else
                    {
                        pullingEntity = null;
                    }
                }
                else
                {
                    pullingEntity = null;
                }
            }
        }
    }
}
