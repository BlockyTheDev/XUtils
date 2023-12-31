package me.cubecrafter.xutils.menu;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import me.cubecrafter.xutils.Events;
import me.cubecrafter.xutils.Tasks;
import me.cubecrafter.xutils.TextUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.scheduler.BukkitTask;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
@RequiredArgsConstructor
public abstract class Menu implements InventoryHolder {

    static {
        Events.subscribe(InventoryClickEvent.class, event -> {
            if (event.getInventory().getHolder() instanceof Menu) {
                Menu menu = (Menu) event.getInventory().getHolder();
                MenuItem item = menu.getItem(event.getSlot());
                if (item != null) {
                    item.onClick(event);
                    menu.updateInventory();
                } else {
                    event.setCancelled(true);
                }
            }
        });
        Events.subscribe(InventoryCloseEvent.class, event -> {
            if (event.getInventory().getHolder() instanceof Menu) {
                Menu menu = (Menu) event.getInventory().getHolder();
                BukkitTask task = menu.getUpdateTask();
                if (task != null) {
                    task.cancel();
                }
            }
        });
    }

    private final Player player;
    private Inventory inventory;
    private BukkitTask updateTask;

    private final Map<Integer, MenuItem> items = new HashMap<>();

    @Setter private boolean autoUpdate = true;
    @Setter private int updateInterval = 20;

    public MenuItem getItem(int slot) {
        return items.get(slot);
    }

    public void setItem(MenuItem item, List<Integer> slots) {
        for (int slot : slots) {
            items.put(slot, item);
        }
    }

    public void setItem(MenuItem item, Integer... slots) {
        setItem(item, Arrays.asList(slots));
    }

    public void fillBorders(MenuItem item) {
        for (int i = 0; i < getRows() * 9; i++) {
            if (i < 9 || i % 9 == 0 || i % 9 == 8 || i >= getRows() * 9 - 9) {
                items.put(i, item);
            }
        }
    }

    public void open() {
        Tasks.sync(() -> {
            if (inventory == null) {
                inventory = Bukkit.createInventory(this, getRows() * 9, TextUtil.color(TextUtil.parsePlaceholders(player, getTitle())));
            }
            if (autoUpdate) {
                updateTask = Tasks.repeat(this::updateInventory, 0L, updateInterval);
            } else {
                updateInventory();
            }
            player.openInventory(inventory);
        });
    }

    public void close() {
        player.closeInventory();
    }

    public abstract void update();
    public abstract int getRows();
    public abstract String getTitle();

    private void updateInventory() {
        items.clear();
        inventory.clear();
        update();
        items.forEach((slot, item) -> inventory.setItem(slot, TextUtil.parsePlaceholders(player, item.getItem())));
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }

}
