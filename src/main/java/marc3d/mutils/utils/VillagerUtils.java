package marc3d.mutils.utils;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.village.TradeOffer;
import net.minecraft.village.TradeOfferList;
import net.minecraft.village.TradedItem;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class VillagerUtils {
    private static final MinecraftClient mc = MinecraftClient.getInstance();

    /**
     * Finds all villagers within a specified radius
     */
    public static List<VillagerEntity> findNearbyVillagers(double radius) {
        if (mc.world == null || mc.player == null) return new ArrayList<>();

        Vec3d playerPos = mc.player.getPos();
        Box searchBox = new Box(playerPos.subtract(radius, radius, radius),
            playerPos.add(radius, radius, radius));

        return mc.world.getEntitiesByClass(VillagerEntity.class, searchBox,
                v -> v.isAlive() && !v.isBaby())
            .stream()
            .collect(Collectors.toList());
    }

    /**
     * Gets the closest villager to the player
     */
    public static Optional<VillagerEntity> getClosestVillager(double maxRadius) {
        if (mc.player == null) return Optional.empty();

        return findNearbyVillagers(maxRadius).stream()
            .min(Comparator.comparingDouble(v -> mc.player.squaredDistanceTo(v)));
    }

    /**
     * Interacts with a villager to open trading screen
     */
    public static boolean interactWithVillager(VillagerEntity villager) {
        if (mc.player == null || mc.interactionManager == null) return false;

        // Look at the villager
        Vec3d villagerPos = villager.getPos().add(0, villager.getEyeHeight(villager.getPose()), 0);
        lookAt(villagerPos);

        // Right-click the villager
        mc.interactionManager.interactEntity(mc.player, villager, Hand.MAIN_HAND);

        return true;
    }

    /**
     * Gets all available trades from a villager
     */
    public static TradeOfferList getVillagerTrades(VillagerEntity villager) {
        return villager.getOffers();
    }

    /**
     * Finds a specific trade by desired output item
     */
    public static Optional<TradeOffer> findTradeByOutput(VillagerEntity villager, ItemStack desiredOutput) {
        TradeOfferList offers = villager.getOffers();

        return offers.stream()
            .filter(offer -> ItemStack.areItemsEqual(offer.getSellItem(), desiredOutput) && !offer.isDisabled())
            .findFirst();
    }

    /**
     * Finds all trades that match a desired output item
     */
    public static List<TradeOffer> findAllTradesByOutput(VillagerEntity villager, ItemStack desiredOutput) {
        TradeOfferList offers = villager.getOffers();

        return offers.stream()
            .filter(offer -> ItemStack.areItemsEqual(offer.getSellItem(), desiredOutput) && !offer.isDisabled())
            .collect(Collectors.toList());
    }

    /**
     * Gets the index of a specific trade offer
     */
    public static int getTradeIndex(VillagerEntity villager, TradeOffer targetOffer) {
        TradeOfferList offers = villager.getOffers();
        for (int i = 0; i < offers.size(); i++) {
            if (offers.get(i) == targetOffer) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Checks if player has the required items for a trade
     */
    public static boolean hasRequiredItems(TradeOffer trade) {
        if (mc.player == null) return false;

        TradedItem firstBuy = trade.getFirstBuyItem();
        Optional<TradedItem> secondBuyOpt = trade.getSecondBuyItem();

        boolean hasFirst = hasRequiredTradedItem(firstBuy);
        boolean hasSecond = secondBuyOpt.isEmpty() || hasRequiredTradedItem(secondBuyOpt.get());

        return hasFirst && hasSecond;
    }

    /**
     * Checks if player has the required TradedItem in inventory
     */
    private static boolean hasRequiredTradedItem(TradedItem tradedItem) {
        if (mc.player == null) return false;

        ItemStack required = tradedItem.item().value().getDefaultStack();
        int requiredCount = tradedItem.count();
        int foundCount = countItemInInventory(required);

        return foundCount >= requiredCount;
    }

    /**
     * Checks if player has a specific item in inventory
     */
    public static boolean hasItemInInventory(ItemStack required) {
        if (mc.player == null || required.isEmpty()) return false;
        return countItemInInventory(required) >= required.getCount();
    }

    /**
     * Counts total amount of an item in inventory
     */
    private static int countItemInInventory(ItemStack item) {
        if (mc.player == null || item.isEmpty()) return 0;

        int count = 0;
        for (int i = 0; i < mc.player.getInventory().size(); i++) {
            ItemStack stack = mc.player.getInventory().getStack(i);
            if (ItemStack.areItemsEqual(stack, item)) {
                count += stack.getCount();
            }
        }
        return count;
    }

    /**
     * Performs a trade at the specified slot index
     */
    public static boolean executeTrade(int tradeIndex) {
        if (mc.player == null || mc.interactionManager == null) return false;
        if (mc.player.currentScreenHandler == null) return false;

        // Select the trade
        mc.interactionManager.clickButton(mc.player.currentScreenHandler.syncId, tradeIndex);

        // Wait a tick for the trade to register
        try {
            Thread.sleep(50);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        }

        // Click the output slot to complete trade
        int outputSlot = 2; // Merchant output slot
        mc.interactionManager.clickSlot(
            mc.player.currentScreenHandler.syncId,
            outputSlot,
            0,
            SlotActionType.QUICK_MOVE,
            mc.player
        );

        return true;
    }

    /**
     * Executes multiple trades with the same villager
     */
    public static int executeTrades(int tradeIndex, int times) {
        int successCount = 0;

        for (int i = 0; i < times; i++) {
            if (executeTrade(tradeIndex)) {
                successCount++;
                try {
                    Thread.sleep(100); // Small delay between trades
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            } else {
                break;
            }
        }

        return successCount;
    }

    /**
     * Executes all possible trades for a specific offer
     */
    public static int executeAllPossibleTrades(VillagerEntity villager, TradeOffer offer) {
        int tradeIndex = getTradeIndex(villager, offer);
        if (tradeIndex < 0) return 0;

        int possibleTrades = calculatePossibleTrades(offer);
        return executeTrades(tradeIndex, possibleTrades);
    }

    /**
     * Closes the current trading screen
     */
    public static void closeTradeScreen() {
        if (mc.player != null && mc.player.currentScreenHandler != null) {
            mc.player.closeHandledScreen();
        }
    }

    /**
     * Gets the profession of a villager as a string
     */
    public static String getVillagerProfession(VillagerEntity villager) {
        return villager.getVillagerData().profession().toString();
    }

    /**
     * Gets the level of a villager (1-5)
     */
    public static int getVillagerLevel(VillagerEntity villager) {
        return villager.getVillagerData().level();
    }

    /**
     * Checks if a villager has any unlocked trades
     */
    public static boolean hasAvailableTrades(VillagerEntity villager) {
        TradeOfferList offers = villager.getOffers();
        return offers != null && !offers.isEmpty() &&
            offers.stream().anyMatch(offer -> !offer.isDisabled());
    }

    /**
     * Gets all available (not disabled) trades from a villager
     */
    public static List<TradeOffer> getAvailableTrades(VillagerEntity villager) {
        TradeOfferList offers = villager.getOffers();
        if (offers == null || offers.isEmpty()) return new ArrayList<>();

        return offers.stream()
            .filter(offer -> !offer.isDisabled())
            .collect(Collectors.toList());
    }

    /**
     * Counts how many times a trade can be executed based on inventory
     */
    public static int calculatePossibleTrades(TradeOffer trade) {
        if (mc.player == null) return 0;

        TradedItem firstBuy = trade.getFirstBuyItem();
        Optional<TradedItem> secondBuyOpt = trade.getSecondBuyItem();

        int maxFromFirst = countTradedItemInInventory(firstBuy);
        int maxFromSecond = secondBuyOpt.isEmpty() ? Integer.MAX_VALUE :
            countTradedItemInInventory(secondBuyOpt.get());

        int maxFromUses = trade.getMaxUses() - trade.getUses();

        return Math.min(Math.min(maxFromFirst, maxFromSecond), maxFromUses);
    }

    /**
     * Counts how many times we can trade with a TradedItem
     */
    private static int countTradedItemInInventory(TradedItem tradedItem) {
        if (mc.player == null) return 0;

        ItemStack required = tradedItem.item().value().getDefaultStack();
        int requiredCount = tradedItem.count();
        int totalCount = countItemInInventory(required);

        return totalCount / requiredCount;
    }

    /**
     * Makes the player look at a specific position
     */
    private static void lookAt(Vec3d pos) {
        if (mc.player == null) return;

        Vec3d eyePos = mc.player.getEyePos();
        Vec3d direction = pos.subtract(eyePos).normalize();

        double yaw = Math.toDegrees(Math.atan2(direction.z, direction.x)) - 90.0;
        double pitch = -Math.toDegrees(Math.asin(direction.y));

        mc.player.setYaw((float) yaw);
        mc.player.setPitch((float) pitch);
    }

    /**
     * Checks if a villager is currently tradeable (not sleeping, not in bed)
     */
    public static boolean isVillagerTradeable(VillagerEntity villager) {
        return !villager.isSleeping() && villager.isAlive();
    }

    /**
     * Gets distance from player to villager
     */
    public static double getDistanceToVillager(VillagerEntity villager) {
        if (mc.player == null) return Double.MAX_VALUE;
        return Math.sqrt(mc.player.squaredDistanceTo(villager));
    }

    /**
     * Checks if the trading screen is currently open
     */
    public static boolean isTradingScreenOpen() {
        return mc.player != null &&
            mc.player.currentScreenHandler != null &&
            mc.currentScreen != null;
    }

    /**
     * Waits until the trading screen is opened or timeout occurs
     */
    public static boolean waitForTradingScreen(int timeoutMs) {
        long startTime = System.currentTimeMillis();
        while (!isTradingScreenOpen()) {
            if (System.currentTimeMillis() - startTime > timeoutMs) {
                return false;
            }
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return false;
            }
        }
        return true;
    }
}
