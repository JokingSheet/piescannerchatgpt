package dev.pie.scanner.scanner;

import java.util.List;
import java.util.Set;

/** Eine ueberwachte Kategorie. Zugeordnet wird ueber den Knotennamen im Pie-Chart (z.B. "minecraft:chest"). */
public record Category(String key, String label, Set<String> exactNames, List<String> suffixes) {

    public static final List<Category> ALL = List.of(
        new Category("chest", "Chests", Set.of("chest"), List.of()),
        new Category("trapped_chest", "Trapped Chests", Set.of("trapped_chest"), List.of()),
        new Category("shulker_box", "Shulker Boxes", Set.of("shulker_box"), List.of("_shulker_box")),
        new Category("ender_chest", "Ender Chests", Set.of("ender_chest"), List.of()),
        new Category("piston", "Pistons", Set.of("piston", "moving_piston"), List.of()),
        new Category("sticky_piston", "Sticky Pistons", Set.of("sticky_piston"), List.of()),
        new Category("hopper", "Hoppers", Set.of("hopper", "hopper_minecart"), List.of()),
        new Category("dropper", "Droppers", Set.of("dropper"), List.of()),
        new Category("dispenser", "Dispensers", Set.of("dispenser"), List.of()),
        new Category("observer", "Observers", Set.of("observer"), List.of()),
        new Category("redstone", "Redstone", Set.of("comparator", "daylight_detector", "repeater",
            "redstone_wire", "redstone_torch", "redstone_wall_torch", "redstone_lamp", "lever", "target"), List.of()),
        new Category("furnace", "Furnaces", Set.of("furnace", "blast_furnace", "smoker"), List.of()),
        new Category("barrel", "Barrels", Set.of("barrel"), List.of()),
        new Category("item_frame", "Item Frames", Set.of("item_frame", "glow_item_frame"), List.of()),
        new Category("sign", "Signs", Set.of("sign", "hanging_sign"), List.of("_sign", "_hanging_sign"))
    );

    public boolean matches(String nodeName) {
        String n = nodeName.toLowerCase(java.util.Locale.ROOT);
        int c = n.indexOf(':');
        if (c >= 0) n = n.substring(c + 1);
        if (exactNames.contains(n)) return true;
        for (String s : suffixes) if (n.endsWith(s)) return true;
        return false;
    }
}
