package fr.alexdoru.megawallsenhancementsmod.hackerdetector.utils;

import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;

public class Vector3D extends Vec3 {

    public Vector3D(double x, double y, double z) {
        super(x, y, z);
    }

    /**
     * Creates a Vector3D from pitch and yaw
     */
    public static Vector3D getVectorFromRotation(float pitch, float yaw) {
        final float f = MathHelper.cos(-yaw * 0.017453292F - (float) Math.PI);
        final float f1 = MathHelper.sin(-yaw * 0.017453292F - (float) Math.PI);
        final float f2 = -MathHelper.cos(-pitch * 0.017453292F);
        final float f3 = MathHelper.sin(-pitch * 0.017453292F);
        return new Vector3D(f1 * f2, f3, f * f2);
    }

    /**
     * Returns the absolute angle in between
     * the 2Dvector resulting of the projection of this onto the XZ plane and
     * the 2Dvector resulting of the projection of otherVector onto the XZ plane
     * Result is in degrees
     */
    public double getXZAngleDiffWithVector(Vector3D otherVector) {
        final double den = Math.sqrt((this.xCoord * this.xCoord + this.zCoord * this.zCoord) * (otherVector.xCoord * otherVector.xCoord + otherVector.zCoord * otherVector.zCoord));
        if (den < 1.0000000116860974E-7D) {
            return 0;
        }
        final double cos = (this.xCoord * otherVector.xCoord + this.zCoord * otherVector.zCoord) / den;
        if (cos > 1 || cos < -1) {
            return 0;
        }
        return Math.toDegrees(Math.acos(cos));
    }

    /**
     * Returns the length of the 2D vector resulting of the projection of this onto the XZ plane
     */
    public double lengthVector2DXZ() {
        return MathHelper.sqrt_double(this.xCoord * this.xCoord + this.zCoord * this.zCoord);
    }

}
