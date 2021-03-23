package ichttt.mods.firstaid.common.util;

import com.google.common.base.Stopwatch;
import ichttt.mods.firstaid.common.AABBAlignedBoundingBox;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class PlayerSizeHelper {
    private static final Map<EntityEquipmentSlot, AABBAlignedBoundingBox> NORMAL_BOXES;
    private static final Map<EntityEquipmentSlot, AABBAlignedBoundingBox> SNEAKING_BOXES;

    static {
        Map<EntityEquipmentSlot, AABBAlignedBoundingBox> builder = new LinkedHashMap<>();
        builder.put(EntityEquipmentSlot.FEET, new AABBAlignedBoundingBox(0D, 0D, 0D, 1D, 0.15D, 1D));
        builder.put(EntityEquipmentSlot.LEGS, new AABBAlignedBoundingBox(0D, 0.15D, 0D, 1D, 0.45D, 1D));
        builder.put(EntityEquipmentSlot.CHEST, new AABBAlignedBoundingBox(0D, 0.45D, 0D, 1D, 0.8D, 1D));
        builder.put(EntityEquipmentSlot.HEAD, new AABBAlignedBoundingBox(0D, 0.8D, 0D, 1D, 1D, 1D));
        NORMAL_BOXES = Collections.unmodifiableMap(builder);

        builder = new LinkedHashMap<>();
        builder.put(EntityEquipmentSlot.FEET,  new AABBAlignedBoundingBox(0D, 0D, 0D, 1D, 0.15D, 1D));
        builder.put(EntityEquipmentSlot.LEGS, new AABBAlignedBoundingBox(0D, 0.15D, 0D, 1D, 0.4D, 1D));
        builder.put(EntityEquipmentSlot.CHEST, new AABBAlignedBoundingBox(0D, 0.4D, 0D, 1D, 0.75D, 1D));
        builder.put(EntityEquipmentSlot.HEAD,  new AABBAlignedBoundingBox(0D, 0.75D, 0D, 1D, 1D, 1D));
        SNEAKING_BOXES = Collections.unmodifiableMap(builder);
    }

    @Nonnull
    public static Map<EntityEquipmentSlot, AABBAlignedBoundingBox> getBoxes(Entity entity) {
        return entity.isSneaking() ? SNEAKING_BOXES : NORMAL_BOXES;
    }

    public static EntityEquipmentSlot getSlotTypeForProjectileHit(Entity hittingObject, EntityPlayer toTest) {
        Stopwatch stopwatch = Stopwatch.createStarted();
        Map<EntityEquipmentSlot, AABBAlignedBoundingBox> toUse = getBoxes(toTest);
        Vec3d oldPosition = new Vec3d(hittingObject.posX, hittingObject.posY, hittingObject.posZ);
        Vec3d newPosition = new Vec3d(hittingObject.posX + hittingObject.motionX, hittingObject.posY + hittingObject.motionY, hittingObject.posZ + hittingObject.motionZ);

        // See ProjectileHelper.getEntityHitResult
        float[] inflationSteps = new float[] {0.01F, 0.1F, 0.2F, 0.3F};
        for (float inflation : inflationSteps) {
            EntityEquipmentSlot bestSlot = null;
            double bestValue = Double.MAX_VALUE;
            for (Map.Entry<EntityEquipmentSlot, AABBAlignedBoundingBox> entry : toUse.entrySet()) {
                AxisAlignedBB axisalignedbb = entry.getValue().createAABB(toTest.getEntityBoundingBox()).grow(inflation);
                RayTraceResult rtr = axisalignedbb.calculateIntercept(oldPosition, newPosition);
                if (rtr != null) {
                    double d1 = oldPosition.squareDistanceTo(rtr.hitVec);
                    double d2 = 0D;//newPosition.distanceToSqr(optional.get());
                    if ((d1 + d2) < bestValue) {
                        bestSlot = entry.getKey();
                        bestValue = d1 + d2;
                    }
                }
            }
            if (bestSlot != null) {
                stopwatch.stop();
                System.out.println("Inflation: " + inflation + " best slot: " + bestSlot);
                System.out.println("Took " + stopwatch.elapsed(TimeUnit.MICROSECONDS) + " us");
                return bestSlot;
            }
        }
        stopwatch.stop();
        System.out.println("Not found!");
        System.out.println("Took " + stopwatch.elapsed(TimeUnit.MICROSECONDS) + " us");
        return null;
    }

}
