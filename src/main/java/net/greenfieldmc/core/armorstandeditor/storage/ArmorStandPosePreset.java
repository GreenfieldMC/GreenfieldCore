package net.greenfieldmc.core.armorstandeditor.storage;

import org.bukkit.Material;
import org.bukkit.entity.ArmorStand;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.EulerAngle;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Preset armor-stand poses and associated item metadata.
 *
 * Each enum value is a preset. Fill in the EulerAngles and attributes manually.
 * The body-part pose fields now use nullable per-axis values: if an axis is null
 * it will be left unchanged when applying the preset to an existing armor stand.
 */
public enum ArmorStandPosePreset {
    DEFAULT(
            "default",
            new NullableEuler(Math.toRadians(0), Math.toRadians(0), Math.toRadians(0)), // head
            new NullableEuler(Math.toRadians(0), Math.toRadians(0), Math.toRadians(0)), // body
            new NullableEuler(Math.toRadians(0), Math.toRadians(0), Math.toRadians(0)), // left arm
            new NullableEuler(Math.toRadians(0), Math.toRadians(0), Math.toRadians(0)), // right arm
            new NullableEuler(Math.toRadians(0), Math.toRadians(0), Math.toRadians(0)), // left leg
            new NullableEuler(Math.toRadians(0), Math.toRadians(0), Math.toRadians(0)), // right leg
            null,
            false,
            null,
            true,
            null,
            null,
            List.of("pose_default"), // data component string
            "Default",// display name
            List.of("Resets all attributes.") // lore strings
    ),

    HERO(
            "hero",
            new NullableEuler(Math.toRadians(0), Math.toRadians(0), Math.toRadians(0)), // head
            new NullableEuler(Math.toRadians(0), Math.toRadians(0), Math.toRadians(0)), // body
            new NullableEuler(Math.toRadians(268), Math.toRadians(37), Math.toRadians(337)), // left arm
            new NullableEuler(Math.toRadians(76), Math.toRadians(152), Math.toRadians(352)), // right arm
            new NullableEuler(Math.toRadians(0), Math.toRadians(0), Math.toRadians(0)), // left leg
            new NullableEuler(Math.toRadians(0), Math.toRadians(0), Math.toRadians(0)), // right leg
            null,
            true,
            null,
            true,
            null,
            null,
            List.of("pose_hero"), // data component string
            "Hero",// display name
            List.of("Statue like.") // lore strings
    ),

    HOLD(
            "hold",
            new NullableEuler(Math.toRadians(-10), Math.toRadians(0), Math.toRadians(0)), // head: slightly down
            new NullableEuler(Math.toRadians(0), Math.toRadians(0), Math.toRadians(0)), // body
            new NullableEuler(Math.toRadians(340), Math.toRadians(20), Math.toRadians(350)), // left arm (relaxed)
            new NullableEuler(Math.toRadians(300), Math.toRadians(10), Math.toRadians(350)), // right arm (holding forward)
            new NullableEuler(Math.toRadians(0), Math.toRadians(0), Math.toRadians(0)), // left leg
            new NullableEuler(Math.toRadians(0), Math.toRadians(0), Math.toRadians(0)), // right leg
            null,
            true,
            null,
            true,
            null,
            null,
            List.of("pose_hold"),
            "Hold",
            List.of("Holding an item in front.")
    ),

    LEGLESS(
            "legless",
            new NullableEuler(Math.toRadians(0), Math.toRadians(0), Math.toRadians(0)), // head
            new NullableEuler(Math.toRadians(0), Math.toRadians(0), Math.toRadians(0)), // body
            new NullableEuler(Math.toRadians(330), Math.toRadians(0), Math.toRadians(0)), // left arm raised slightly
            new NullableEuler(Math.toRadians(30), Math.toRadians(0), Math.toRadians(0)), // right arm raised slightly
            new NullableEuler(Math.toRadians(85), Math.toRadians(0), Math.toRadians(0)), // left leg folded (appears legless)
            new NullableEuler(Math.toRadians(85), Math.toRadians(0), Math.toRadians(0)), // right leg folded
            null,
            true,
            null,
            true,
            null,
            null,
            List.of("pose_legless"),
            "Legless",
            List.of("Seated / folded legs look.")
    ),

    POINT_LEFT(
            "point_left",
            new NullableEuler(Math.toRadians(0), Math.toRadians(0), Math.toRadians(0)),
            new NullableEuler(Math.toRadians(0), Math.toRadians(0), Math.toRadians(0)),
            new NullableEuler(Math.toRadians(350), Math.toRadians(0), Math.toRadians(0)), // left arm slightly back
            new NullableEuler(Math.toRadians(270), Math.toRadians(0), Math.toRadians(0)), // right arm pointing left (rotated)
            new NullableEuler(Math.toRadians(0), Math.toRadians(0), Math.toRadians(0)),
            new NullableEuler(Math.toRadians(0), Math.toRadians(0), Math.toRadians(0)),
            null,
            true,
            null,
            true,
            null,
            null,
            List.of("pose_point_left"),
            "Point Left",
            List.of("Points to the left.")
    ),

    POINT_RIGHT(
            "point_right",
            new NullableEuler(Math.toRadians(0), Math.toRadians(0), Math.toRadians(0)),
            new NullableEuler(Math.toRadians(0), Math.toRadians(0), Math.toRadians(0)),
            new NullableEuler(Math.toRadians(270), Math.toRadians(0), Math.toRadians(0)), // left arm pointing right
            new NullableEuler(Math.toRadians(350), Math.toRadians(0), Math.toRadians(0)), // right arm slightly back
            new NullableEuler(Math.toRadians(0), Math.toRadians(0), Math.toRadians(0)),
            new NullableEuler(Math.toRadians(0), Math.toRadians(0), Math.toRadians(0)),
            null,
            true,
            null,
            true,
            null,
            null,
            List.of("pose_point_right"),
            "Point Right",
            List.of("Points to the right.")
    ),

    SIT(
            "sit",
            new NullableEuler(Math.toRadians(10), Math.toRadians(0), Math.toRadians(0)), // head slightly forward
            new NullableEuler(Math.toRadians(0), Math.toRadians(0), Math.toRadians(0)),
            new NullableEuler(Math.toRadians(330), Math.toRadians(0), Math.toRadians(0)),
            new NullableEuler(Math.toRadians(30), Math.toRadians(0), Math.toRadians(0)),
            new NullableEuler(Math.toRadians(85), Math.toRadians(0), Math.toRadians(0)), // legs bent for sitting
            new NullableEuler(Math.toRadians(85), Math.toRadians(0), Math.toRadians(0)),
            null,
            true,
            null,
            true,
            null,
            null,
            List.of("pose_sit"),
            "Sit",
            List.of("Seated pose.")
    ),

    T_POSE(
            "t_pose",
            new NullableEuler(Math.toRadians(0), Math.toRadians(0), Math.toRadians(0)),
            new NullableEuler(Math.toRadians(0), Math.toRadians(0), Math.toRadians(0)),
            new NullableEuler(Math.toRadians(0), Math.toRadians(0), Math.toRadians(0)), // left arm straight out
            new NullableEuler(Math.toRadians(0), Math.toRadians(0), Math.toRadians(0)), // right arm straight out
            new NullableEuler(Math.toRadians(0), Math.toRadians(0), Math.toRadians(0)),
            new NullableEuler(Math.toRadians(0), Math.toRadians(0), Math.toRadians(0)),
            null,
            true,
            null,
            true,
            null,
            null,
            List.of("pose_tpose"),
            "T-Pose",
            List.of("Arms outstretched.")
    ),

    WALK(
            "walk",
            new NullableEuler(Math.toRadians(0), Math.toRadians(-45), Math.toRadians(0)), // head
            new NullableEuler(Math.toRadians(0), Math.toRadians(0), Math.toRadians(0)), // body
            new NullableEuler(Math.toRadians(24), Math.toRadians(0), Math.toRadians(0)), // left arm
            new NullableEuler(Math.toRadians(325), Math.toRadians(0), Math.toRadians(0)), // right arm
            new NullableEuler(Math.toRadians(351), Math.toRadians(0), Math.toRadians(0)), // left leg
            new NullableEuler(Math.toRadians(24), Math.toRadians(0), Math.toRadians(0)), // right leg
            null,
            true,
            null,
            true,
            null,
            null,
            List.of("pose_walk"), // data component string (adjusted key)
            "Walk",
            List.of("Walking pose") // lore strings
    );

    private final String key;
    private final NullableEuler headPose; // per-axis nullable
    private final NullableEuler bodyPose; // per-axis nullable
    private final NullableEuler leftArmPose; // per-axis nullable
    private final NullableEuler rightArmPose; // per-axis nullable
    private final NullableEuler leftLegPose; // per-axis nullable
    private final NullableEuler rightLegPose; // per-axis nullable

    private final Boolean basePlate; // nullable
    private final Boolean arms; // nullable
    private final Boolean small; // nullable
    private final Boolean visible; // nullable
    private final Boolean gravity; // nullable
    private final Boolean invulnerable; // nullable

    private final List<String> dataComponents; // simple string tags to be translated to components elsewhere
    private final String displayName;
    private final List<String> lore;

    ArmorStandPosePreset(String key,
                         NullableEuler headPose,
                         NullableEuler bodyPose,
                         NullableEuler leftArmPose,
                         NullableEuler rightArmPose,
                         NullableEuler leftLegPose,
                         NullableEuler rightLegPose,
                         Boolean basePlate,
                         Boolean arms,
                         Boolean small,
                         Boolean visible,
                         Boolean gravity,
                         Boolean invulnerable,
                         List<String> dataComponents,
                         String displayName,
                         List<String> lore) {
         this.key = key;
         this.headPose = headPose;
         this.bodyPose = bodyPose;
         this.leftArmPose = leftArmPose;
         this.rightArmPose = rightArmPose;
         this.leftLegPose = leftLegPose;
         this.rightLegPose = rightLegPose;
         this.basePlate = basePlate;
         this.arms = arms;
         this.small = small;
         this.visible = visible;
         this.gravity = gravity;
         this.invulnerable = invulnerable;
         this.dataComponents = dataComponents == null ? Collections.emptyList() : new ArrayList<>(dataComponents);
         this.displayName = displayName;
         this.lore = lore == null ? Collections.emptyList() : new ArrayList<>(lore);
     }

    public String getKey() { return key; }
    public NullableEuler getHeadPose() { return headPose; }
    public NullableEuler getBodyPose() { return bodyPose; }
    public NullableEuler getLeftArmPose() { return leftArmPose; }
    public NullableEuler getRightArmPose() { return rightArmPose; }
    public NullableEuler getLeftLegPose() { return leftLegPose; }
    public NullableEuler getRightLegPose() { return rightLegPose; }

    public Boolean hasBasePlate() { return basePlate; }
    public Boolean hasArms() { return arms; }
    public Boolean isSmall() { return small; }
    public Boolean isVisible() { return visible; }
    public Boolean hasGravity() { return gravity; }
    public Boolean isInvulnerable() { return invulnerable; }

    public List<String> getDataComponents() { return Collections.unmodifiableList(dataComponents); }
    public String getDisplayName() { return displayName; }
    public List<String> getLore() { return Collections.unmodifiableList(lore); }

    /**
     * Apply preset attributes to an ArmorStand instance.
     * Null boolean fields are skipped so they do not overwrite existing values.
     * For poses, any null axis value will keep the existing axis value on the armor stand.
     */
    public void applyTo(ArmorStand stand) {
        if (stand == null) return;

        // Head
        EulerAngle currentHead = stand.getHeadPose();
        EulerAngle newHead = mergeNullableEuler(currentHead, headPose);
        stand.setHeadPose(newHead);

        EulerAngle currentBody = stand.getBodyPose();
        EulerAngle newBody = mergeNullableEuler(currentBody, bodyPose);
        stand.setBodyPose(newBody);

        EulerAngle currentLeftArm = stand.getLeftArmPose();
        EulerAngle newLeftArm = mergeNullableEuler(currentLeftArm, leftArmPose);
        stand.setLeftArmPose(newLeftArm);

        EulerAngle currentRightArm = stand.getRightArmPose();
        EulerAngle newRightArm = mergeNullableEuler(currentRightArm, rightArmPose);
        stand.setRightArmPose(newRightArm);

        EulerAngle currentLeftLeg = stand.getLeftLegPose();
        EulerAngle newLeftLeg = mergeNullableEuler(currentLeftLeg, leftLegPose);
        stand.setLeftLegPose(newLeftLeg);

        EulerAngle currentRightLeg = stand.getRightLegPose();
        EulerAngle newRightLeg = mergeNullableEuler(currentRightLeg, rightLegPose);
        stand.setRightLegPose(newRightLeg);

        if (basePlate != null) stand.setBasePlate(basePlate);
        if (arms != null) stand.setArms(arms);
        if (small != null) stand.setSmall(small);
        if (visible != null) stand.setVisible(visible);
        if (gravity != null) stand.setGravity(gravity);
        if (invulnerable != null) stand.setInvulnerable(invulnerable);
    }

    private static EulerAngle mergeNullableEuler(EulerAngle current, NullableEuler preset) {
        if (current == null) current = new EulerAngle(0,0,0);
        if (preset == null) return current;
        double x = preset.x != null ? preset.x : current.getX();
        double y = preset.y != null ? preset.y : current.getY();
        double z = preset.z != null ? preset.z : current.getZ();
        return new EulerAngle(x, y, z);
    }

    /**
     * Small holder for per-axis nullable Euler components (radians). If any axis is null
     * that axis will not be changed on the target armor stand.
     */
    public static class NullableEuler {
        public final Double x;
        public final Double y;
        public final Double z;

        public NullableEuler(Double x, Double y, Double z) {
            this.x = x;
            this.y = y;
            this.z = z;
        }
    }

     /**
      * Create a simple ItemStack representing this preset (display name + lore).
      * Material can be supplied; default is ARMOR_STAND.
      */
     public ItemStack toItemStack(Material material) {
         ItemStack item = new ItemStack(material == null ? Material.ARMOR_STAND : material);
         ItemMeta meta = item.getItemMeta();
         if (meta != null) {
             if (displayName != null) meta.setDisplayName(displayName);
             if (!lore.isEmpty()) meta.setLore(new ArrayList<>(lore));
             item.setItemMeta(meta);
         }
         return item;
     }

     public ItemStack toItemStack() {
         return toItemStack(Material.ARMOR_STAND);
     }

     public static Optional<ArmorStandPosePreset> fromKey(String key) {
         if (key == null) return Optional.empty();
         for (ArmorStandPosePreset p : values()) {
             if (p.key.equalsIgnoreCase(key)) return Optional.of(p);
             // also check dataComponents list
             for (String comp : p.getDataComponents()) {
                 if (comp != null && comp.equalsIgnoreCase(key)) return Optional.of(p);
             }
         }
         return Optional.empty();
     }
 }
