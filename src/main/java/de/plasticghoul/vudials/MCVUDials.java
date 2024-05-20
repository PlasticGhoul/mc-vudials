package de.plasticghoul.vudials;

import com.mojang.logging.LogUtils;
import org.slf4j.Logger;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;

import net.minecraft.client.Minecraft;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.event.entity.living.LivingHealEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.PlayerLoggedInEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.PlayerLoggedOutEvent;
import net.minecraftforge.event.TickEvent.PlayerTickEvent;

/**
 * Main class that triggers all the events
 */
@Mod(MCVUDials.MODID)
public class MCVUDials {
    /** Mod ID for reference by forge */
    public static final String MODID = "mcvudials";
    private static final Logger LOGGER = LogUtils.getLogger();
    private static Writer buffer = new StringWriter();
    private static PrintWriter printwriter = new PrintWriter(buffer);

    // Color constants
    private static final String COLORRED = "red";
    private static final String COLORGREEN = "green";
    private static final String COLORBLUE = "blue";

    // Image constants
    private static final String IMAGEBLANK = "blank.png";
    private static final String IMAGEHEALTH = "health.png";
    private static final String IMAGEFOOD = "food.png";
    private static final String IMAGEARMOR = "armor.png";
    private static final String IMAGEAIR = "air.png";

    /**
     * Class constructor.
     */
    public MCVUDials() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        modEventBus.addListener(this::commonSetup);

        MinecraftForge.EVENT_BUS.register(this);

        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, MCVUDialsConfig.SPEC);
    }

    /**
     * This event method triggers when the mod loader starts and loads this mod
     * If the API key is set, this event will be cancelled so the mod will not load
     * 
     * @param event The FMLCommonSetupEvent that is needed so this method triggers
     */
    private void commonSetup(final FMLCommonSetupEvent event) {
        LOGGER.info("Common Setup started...");

        if (MCVUDialsConfig.getVuServerApiKey().isEmpty()) {
            LOGGER.warn("The config parameter 'vuServerApiKey' is empty!");
            event.setCanceled(true);
        }

        LOGGER.debug("The following values are configured:");
        LOGGER.debug("VUSERVERENABLED = {}", MCVUDialsConfig.isVuServerEnabled());
        LOGGER.debug("VUSERVERHOSTNAME = {}", MCVUDialsConfig.getVuServerHostname());
        LOGGER.debug("VUSERVERPORT = {}", MCVUDialsConfig.getVuServerPort());

        LOGGER.info("Common Setup finished...");
    }

    /**
     * This event method triggers when the minecraft server starts (the world is
     * loaded)
     * During this event, the mod tries to get all the dial UIDs
     * 
     * @param event The ServerStartingEvent that is needed so this method triggers
     */
    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {

        MCVUDialsHelper.setIsVUserverAvailable();

        if (MCVUDialsConfig.isVuServerEnabled() && MCVUDialsHelper.isVUserverAvailable()) {
            MCVUDialsHelper.setDialUids();
            LOGGER.info("Mod initialized.");
        } else {
            LOGGER.info("Mod not enabled in configuration!");
        }

    }

    /**
     * This event method triggers when the player joins a world
     * During this event, all initial dial values, colors and images will be set
     * 
     * @param event The PlayerLoggedInEvent that is needed so this method triggers
     */
    @SubscribeEvent
    public void onLoggedIn(PlayerLoggedInEvent event) {
        if (MCVUDialsConfig.isVuServerEnabled() && MCVUDialsHelper.isVUserverAvailable()) {
            LOGGER.info("Setting initial dial values...");

            // Health
            if (MCVUDialsHelper.getDialUids().length >= 1) {
                MCVUDialsHelper.setCurrentHealthValuePercent(event.getEntity().getHealth(),
                        event.getEntity().getMaxHealth());
                MCVUDialsHelper.setCurrentHealthColors();

                MCVUDialsControl.setDialValue(MCVUDialsHelper.getDialUids()[0],
                        MCVUDialsHelper.getCurrentHealthValuePercent());
                MCVUDialsControl.setDialColor(MCVUDialsHelper.getDialUids()[0],
                        MCVUDialsHelper.getCurrentHealthColors().get(COLORRED),
                        MCVUDialsHelper.getCurrentHealthColors().get(COLORGREEN),
                        MCVUDialsHelper.getCurrentHealthColors().get(COLORBLUE));
                MCVUDialsControl.setDialImage(MCVUDialsHelper.getDialUids()[0], IMAGEHEALTH);
            }

            // Food
            if (MCVUDialsHelper.getDialUids().length >= 2) {
                MCVUDialsHelper.setCurrentFoodLevelValuePercent(event.getEntity().getFoodData().getFoodLevel(), 20);
                MCVUDialsHelper.setCurrentFoodLevelColors();

                MCVUDialsControl.setDialValue(MCVUDialsHelper.getDialUids()[1],
                        MCVUDialsHelper.getCurrentFoodLevelValuePercent());
                MCVUDialsControl.setDialColor(MCVUDialsHelper.getDialUids()[1],
                        MCVUDialsHelper.getCurrentFoodLevelColors().get(COLORRED),
                        MCVUDialsHelper.getCurrentFoodLevelColors().get(COLORGREEN),
                        MCVUDialsHelper.getCurrentFoodLevelColors().get(COLORBLUE));
                MCVUDialsControl.setDialImage(MCVUDialsHelper.getDialUids()[1], IMAGEFOOD);
            }

            // Armor
            if (MCVUDialsHelper.getDialUids().length >= 3) {
                MCVUDialsHelper.setCurrentArmorValuePercent(event.getEntity().getArmorValue(), 20);
                MCVUDialsHelper.setCurrentArmorColors();

                MCVUDialsControl.setDialValue(MCVUDialsHelper.getDialUids()[2],
                        MCVUDialsHelper.getCurrentArmorValuePercent());
                MCVUDialsControl.setDialColor(MCVUDialsHelper.getDialUids()[2],
                        MCVUDialsHelper.getCurrentArmorColors().get(COLORRED),
                        MCVUDialsHelper.getCurrentArmorColors().get(COLORGREEN),
                        MCVUDialsHelper.getCurrentArmorColors().get(COLORBLUE));
                MCVUDialsControl.setDialImage(MCVUDialsHelper.getDialUids()[2], IMAGEARMOR);
            }

            // Air
            if (MCVUDialsHelper.getDialUids().length >= 4) {
                MCVUDialsHelper.setCurrentAirValuePercent(event.getEntity().getAirSupply(),
                        event.getEntity().getMaxAirSupply());
                MCVUDialsHelper.setCurrentAirColors();

                MCVUDialsControl.setDialValue(MCVUDialsHelper.getDialUids()[3],
                        MCVUDialsHelper.getCurrentAirValuePercent());
                MCVUDialsControl.setDialColor(MCVUDialsHelper.getDialUids()[3],
                        MCVUDialsHelper.getCurrentAirColors().get(COLORRED),
                        MCVUDialsHelper.getCurrentAirColors().get(COLORGREEN),
                        MCVUDialsHelper.getCurrentAirColors().get(COLORBLUE));
                MCVUDialsControl.setDialImage(MCVUDialsHelper.getDialUids()[3], IMAGEAIR);
            }
        }
    }

    /**
     * This event method triggers when the player leaves a world
     * During this event, all dial values, colors and images will be reset
     * 
     * @param event The PlayerLoggedOutEvent that is needed so this method triggers
     */
    @SubscribeEvent
    public void onLoggedOut(PlayerLoggedOutEvent event) {
        if (MCVUDialsConfig.isVuServerEnabled() && MCVUDialsHelper.isVUserverAvailable()) {
            LOGGER.info("Resetting dial values...");

            if (MCVUDialsHelper.getDialUids().length >= 1) {
                MCVUDialsControl.setDialValue(MCVUDialsHelper.getDialUids()[0], 0);
                MCVUDialsControl.setDialColor(MCVUDialsHelper.getDialUids()[0], 0, 0, 0);
                MCVUDialsControl.setDialImage(MCVUDialsHelper.getDialUids()[0], IMAGEBLANK);
            }

            if (MCVUDialsHelper.getDialUids().length >= 2) {
                MCVUDialsControl.setDialValue(MCVUDialsHelper.getDialUids()[1], 0);
                MCVUDialsControl.setDialColor(MCVUDialsHelper.getDialUids()[1], 0, 0, 0);
                MCVUDialsControl.setDialImage(MCVUDialsHelper.getDialUids()[1], IMAGEBLANK);
            }

            if (MCVUDialsHelper.getDialUids().length >= 3) {
                MCVUDialsControl.setDialValue(MCVUDialsHelper.getDialUids()[2], 0);
                MCVUDialsControl.setDialColor(MCVUDialsHelper.getDialUids()[2], 0, 0, 0);
                MCVUDialsControl.setDialImage(MCVUDialsHelper.getDialUids()[2], IMAGEBLANK);
            }

            if (MCVUDialsHelper.getDialUids().length >= 4) {
                MCVUDialsControl.setDialValue(MCVUDialsHelper.getDialUids()[3], 0);
                MCVUDialsControl.setDialColor(MCVUDialsHelper.getDialUids()[3], 0, 0, 0);
                MCVUDialsControl.setDialImage(MCVUDialsHelper.getDialUids()[3], IMAGEBLANK);
            }
        }
    }

    /**
     * This event method triggers when the player is hurt
     * During this event, the health value and color can be updated
     * 
     * @param event The LivingHurtEvent that is needed so this method triggers
     */
    @SuppressWarnings("null")
    @SubscribeEvent
    public void onEntityHurt(LivingHurtEvent event) {
        try {
            if (MCVUDialsConfig.isVuServerEnabled() && MCVUDialsHelper.isVUserverAvailable()
                    && event.getEntity().getName().equals(Minecraft.getInstance().player.getName())
                    && MCVUDialsHelper.getDialUids().length >= 1) {
                float entityMaxHealth = event.getEntity().self().getMaxHealth();
                float entityCurrentHealth = event.getEntity().self().getHealth() - event.getAmount();

                if (MCVUDialsHelper.getCurrentHealthValuePercent() != Math
                        .round((entityCurrentHealth * 100) / entityMaxHealth)) {
                    MCVUDialsHelper.setCurrentHealthValuePercent(entityCurrentHealth, entityMaxHealth);
                    LOGGER.debug("Setting health dial to {}", MCVUDialsHelper.getCurrentHealthValuePercent());
                    MCVUDialsControl.setDialValue(MCVUDialsHelper.getDialUids()[0],
                            MCVUDialsHelper.getCurrentHealthValuePercent());

                    if (!MCVUDialsHelper.getCurrentHealthColors().equals(
                            MCVUDialsHelper.getNewHealthColors(MCVUDialsHelper.getCurrentHealthValuePercent()))) {
                        MCVUDialsHelper.setCurrentHealthColors();
                        MCVUDialsControl.setDialColor(MCVUDialsHelper.getDialUids()[0],
                                MCVUDialsHelper.getCurrentHealthColors().get(COLORRED),
                                MCVUDialsHelper.getCurrentHealthColors().get(COLORGREEN),
                                MCVUDialsHelper.getCurrentHealthColors().get(COLORBLUE));
                    }
                }
            }
        } catch (NullPointerException exception) {
            LOGGER.error("Error getting player entity!");
            exception.printStackTrace(printwriter);
            LOGGER.error(buffer.toString());
        }
    }

    /**
     * This event method triggers when the player is healing
     * During this event, the health value and color can be updated
     * 
     * @param event The LivingHealEvent that is needed so this method triggers
     */
    @SuppressWarnings("null")
    @SubscribeEvent
    public void onEntityHeal(LivingHealEvent event) {
        try {
            if (MCVUDialsConfig.isVuServerEnabled() && MCVUDialsHelper.isVUserverAvailable()
                    && event.getEntity().getName().equals(Minecraft.getInstance().player.getName())
                    && MCVUDialsHelper.getDialUids().length >= 1) {
                float entityMaxHealth = event.getEntity().self().getMaxHealth();
                float entityCurrentHealth = event.getEntity().self().getHealth() + event.getAmount();

                if (MCVUDialsHelper.getCurrentHealthValuePercent() != Math
                        .round((entityCurrentHealth * 100) / entityMaxHealth)) {
                    MCVUDialsHelper.setCurrentHealthValuePercent(entityCurrentHealth, entityMaxHealth);
                    LOGGER.debug("Setting health dial to {}", MCVUDialsHelper.getCurrentHealthValuePercent());
                    MCVUDialsControl.setDialValue(MCVUDialsHelper.getDialUids()[0],
                            MCVUDialsHelper.getCurrentHealthValuePercent());

                    if (!MCVUDialsHelper.getCurrentHealthColors().equals(
                            MCVUDialsHelper.getNewHealthColors(MCVUDialsHelper.getCurrentHealthValuePercent()))) {
                        MCVUDialsHelper.setCurrentHealthColors();
                        MCVUDialsControl.setDialColor(MCVUDialsHelper.getDialUids()[0],
                                MCVUDialsHelper.getCurrentHealthColors().get(COLORRED),
                                MCVUDialsHelper.getCurrentHealthColors().get(COLORGREEN),
                                MCVUDialsHelper.getCurrentHealthColors().get(COLORBLUE));
                    }
                }
            }
        } catch (NullPointerException exception) {
            LOGGER.error("Error getting player entity!");
            exception.printStackTrace(printwriter);
            LOGGER.error(buffer.toString());
        }
    }

    /**
     * This event method triggers on each player tick.
     * Since there are no events for air, armor or food level changes, it has to be
     * on each player tick.
     * During this event, the armor, air or food level value and color can be
     * updated
     * 
     * @param event The PlayerTickEvent that is needed so this method triggers
     */
    @SubscribeEvent
    public void onPlayerTick(PlayerTickEvent event) {
        // New food level value
        final float newFoodLevel = ((float)event.player.getFoodData().getFoodLevel() * 100) / 20;
        final int newFoodLevelPercent = Math.round(newFoodLevel);

        // New armor value
        final float newArmor = ((float)event.player.getArmorValue() * 100) / 20;
        final int newArmorPercent = Math.round(newArmor);

        // New air value
        final float newAir = ((float)event.player.getAirSupply() * 100) / event.player.getMaxAirSupply();
        final int newAirPercent = Math.round(newAir);
        final int newMaxAir = event.player.getMaxAirSupply();

        // Food
        if (MCVUDialsHelper.getCurrentFoodLevelValuePercent() != newFoodLevelPercent) {
            MCVUDialsHelper.setCurrentFoodLevelValuePercent(event.player.getFoodData().getFoodLevel(), 20);
            LOGGER.debug("Setting food dial to {}", MCVUDialsHelper.getCurrentFoodLevelValuePercent());
            MCVUDialsControl.setDialValue(MCVUDialsHelper.getDialUids()[1],
                    MCVUDialsHelper.getCurrentFoodLevelValuePercent());

            if (!MCVUDialsHelper.getCurrentFoodLevelColors()
                    .equals(MCVUDialsHelper.getNewFoodLevelColors(MCVUDialsHelper.getCurrentFoodLevelValuePercent()))) {
                MCVUDialsHelper.setCurrentFoodLevelColors();
                MCVUDialsControl.setDialColor(MCVUDialsHelper.getDialUids()[1],
                        MCVUDialsHelper.getCurrentFoodLevelColors().get(COLORRED),
                        MCVUDialsHelper.getCurrentFoodLevelColors().get(COLORGREEN),
                        MCVUDialsHelper.getCurrentFoodLevelColors().get(COLORBLUE));
            }
        }

        // Armor
        if (MCVUDialsHelper.getCurrentArmorValuePercent() != newArmorPercent) {
            MCVUDialsHelper.setCurrentArmorValuePercent(event.player.getArmorValue(), 20);
            LOGGER.debug("Setting armor dial to {}", MCVUDialsHelper.getCurrentArmorValuePercent());
            MCVUDialsControl.setDialValue(MCVUDialsHelper.getDialUids()[2], MCVUDialsHelper.getCurrentArmorValuePercent());

            if (!MCVUDialsHelper.getCurrentArmorColors()
                    .equals(MCVUDialsHelper.getNewArmorColors(MCVUDialsHelper.getCurrentArmorValuePercent()))) {
                MCVUDialsHelper.setCurrentArmorColors();
                MCVUDialsControl.setDialColor(MCVUDialsHelper.getDialUids()[2],
                        MCVUDialsHelper.getCurrentArmorColors().get(COLORRED),
                        MCVUDialsHelper.getCurrentArmorColors().get(COLORGREEN),
                        MCVUDialsHelper.getCurrentArmorColors().get(COLORBLUE));
            }
        }

        // Air
        if (MCVUDialsHelper.getCurrentAirValuePercent() != newAirPercent && newAirPercent % 5 == 0) {
            MCVUDialsHelper.setCurrentAirValuePercent(event.player.getAirSupply(), newMaxAir);
            LOGGER.debug("Setting air dial to {}", MCVUDialsHelper.getCurrentAirValuePercent());
            MCVUDialsControl.setDialValue(MCVUDialsHelper.getDialUids()[3], MCVUDialsHelper.getCurrentAirValuePercent());

            if (!MCVUDialsHelper.getCurrentAirColors()
                    .equals(MCVUDialsHelper.getNewAirColors(MCVUDialsHelper.getCurrentAirValuePercent()))) {
                MCVUDialsHelper.setCurrentAirColors();
                MCVUDialsControl.setDialColor(MCVUDialsHelper.getDialUids()[3],
                        MCVUDialsHelper.getCurrentAirColors().get(COLORRED),
                        MCVUDialsHelper.getCurrentAirColors().get(COLORGREEN),
                        MCVUDialsHelper.getCurrentAirColors().get(COLORBLUE));
            }
        }
    }
}
