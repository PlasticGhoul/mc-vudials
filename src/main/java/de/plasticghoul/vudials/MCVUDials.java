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

@Mod(MCVUDials.MODID)
public class MCVUDials {
    public static final String MODID = "mcvudials";
    private static final Logger LOGGER = LogUtils.getLogger();
    private static Writer buffer = new StringWriter();
    private static PrintWriter printwriter = new PrintWriter(buffer);

    public MCVUDials() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        modEventBus.addListener(this::commonSetup);

        MinecraftForge.EVENT_BUS.register(this);

        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, MCVUDialsConfig.SPEC);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        LOGGER.info("Common Setup started...");

        if (MCVUDialsConfig.vuServerApiKey.isEmpty()) {
            LOGGER.warn("The config parameter 'vuServerApiKey' is empty!");
            event.setCanceled(true);
        }

        LOGGER.debug("The following values are configured:");
        LOGGER.debug("VUSERVERENABLED = " + MCVUDialsConfig.vuServerEnabled);
        LOGGER.debug("VUSERVERHOSTNAME = " + MCVUDialsConfig.vuServerHostname);
        LOGGER.debug("VUSERVERPORT = " + MCVUDialsConfig.vuServerPort);

        LOGGER.info("Common Setup finished...");
    }

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {

        if (MCVUDialsConfig.vuServerEnabled && MCVUDialsHelper.isVUServerAvailable()) {
            MCVUDialsHelper.getDialUids();
            LOGGER.info("Mod initialized.");
        } else {
            LOGGER.info("Mod not enabled in configuration!");
        }

    }

    @SubscribeEvent
    public void onLoggedIn(PlayerLoggedInEvent event) {
        if (MCVUDialsConfig.vuServerEnabled && MCVUDialsHelper.isVUServerAvailable()) {
            if (MCVUDialsConfig.vuServerEnabled && MCVUDialsHelper.serverAvailable) {
                LOGGER.info("Setting initial dial values...");

                // Health
                if (MCVUDialsHelper.dialUids.length >= 1) {
                    MCVUDialsHelper.setCurrentHealthValuePercent(event.getEntity().getHealth(), event.getEntity().getMaxHealth());
                    MCVUDialsHelper.setCurrentHealthColors();

                    MCVUDialsControl.setDialValue(MCVUDialsHelper.dialUids[0], MCVUDialsHelper.getCurrentHealthValuePercent());
                    MCVUDialsControl.setDialColor(MCVUDialsHelper.dialUids[0], MCVUDialsHelper.getCurrentHealthColors().get("red"), MCVUDialsHelper.getCurrentHealthColors().get("green"), MCVUDialsHelper.getCurrentHealthColors().get("blue"));
                    MCVUDialsControl.setDialImage(MCVUDialsHelper.dialUids[0], "health.png");
                }

                // Food
                if (MCVUDialsHelper.dialUids.length >= 2) {
                    MCVUDialsHelper.setCurrentFoodLevelValuePercent(event.getEntity().getFoodData().getFoodLevel(), 20);
                    MCVUDialsHelper.setCurrentFoodLevelColors();
                    
                    MCVUDialsControl.setDialValue(MCVUDialsHelper.dialUids[1], MCVUDialsHelper.getCurrentFoodLevelValuePercent());
                    MCVUDialsControl.setDialColor(MCVUDialsHelper.dialUids[1], MCVUDialsHelper.getCurrentFoodLevelColors().get("red"), MCVUDialsHelper.getCurrentFoodLevelColors().get("green"), MCVUDialsHelper.getCurrentFoodLevelColors().get("blue"));
                    MCVUDialsControl.setDialImage(MCVUDialsHelper.dialUids[1], "food.png");
                }
            }
        }
    }

    @SubscribeEvent
    public void onLoggedOut(PlayerLoggedOutEvent event) {
        if (MCVUDialsConfig.vuServerEnabled && MCVUDialsHelper.serverAvailable) {
            LOGGER.info("Resetting dial values...");

            if (MCVUDialsHelper.dialUids.length >= 1) {
                MCVUDialsControl.setDialValue(MCVUDialsHelper.dialUids[0], 0);
                MCVUDialsControl.setDialColor(MCVUDialsHelper.dialUids[0], 0, 0, 0);
                MCVUDialsControl.setDialImage(MCVUDialsHelper.dialUids[0], "blank.png");
            }

            if (MCVUDialsHelper.dialUids.length >= 2) {
                MCVUDialsControl.setDialValue(MCVUDialsHelper.dialUids[1], 0);
                MCVUDialsControl.setDialColor(MCVUDialsHelper.dialUids[1], 0, 0, 0);
                MCVUDialsControl.setDialImage(MCVUDialsHelper.dialUids[1], "blank.png");
            }
        }
    }

    @SuppressWarnings("null")
    @SubscribeEvent
    public void onEntityHurt(LivingHurtEvent event) {
        try {
            if (MCVUDialsConfig.vuServerEnabled && MCVUDialsHelper.serverAvailable
                    && event.getEntity().getName().equals(Minecraft.getInstance().player.getName())
                    && MCVUDialsHelper.dialUids.length >= 1) {
                float entityMaxHealth = event.getEntity().self().getMaxHealth();
                float entityCurrentHealth = event.getEntity().self().getHealth() - event.getAmount();
                
                if (MCVUDialsHelper.getCurrentHealthValuePercent() != Math.round((entityCurrentHealth * 100)/entityMaxHealth)) {
                    MCVUDialsHelper.setCurrentHealthValuePercent(entityCurrentHealth, entityMaxHealth);
                    LOGGER.debug("Setting health dial to " + MCVUDialsHelper.getCurrentHealthValuePercent());
                    MCVUDialsControl.setDialValue(MCVUDialsHelper.dialUids[0], MCVUDialsHelper.getCurrentHealthValuePercent());

                    if (! MCVUDialsHelper.getCurrentHealthColors().equals(MCVUDialsHelper.getNewHealthColors(MCVUDialsHelper.getCurrentHealthValuePercent()))) {
                        MCVUDialsHelper.setCurrentHealthColors();
                        MCVUDialsControl.setDialColor(MCVUDialsHelper.dialUids[0], MCVUDialsHelper.getCurrentHealthColors().get("red"), MCVUDialsHelper.getCurrentHealthColors().get("green"), MCVUDialsHelper.getCurrentHealthColors().get("blue"));
                    }
                }
            }
        } catch (NullPointerException exception) {
            LOGGER.error("Error getting player entity!");
            exception.printStackTrace(printwriter);
            LOGGER.error(buffer.toString());
        }
    }

    @SuppressWarnings("null")
    @SubscribeEvent
    public void onEntityHeal(LivingHealEvent event) {
        try {
            if (MCVUDialsConfig.vuServerEnabled && MCVUDialsHelper.serverAvailable
                    && event.getEntity().getName().equals(Minecraft.getInstance().player.getName())
                    && MCVUDialsHelper.dialUids.length >= 1) {
                float entityMaxHealth = event.getEntity().self().getMaxHealth();
                float entityCurrentHealth = event.getEntity().self().getHealth() + event.getAmount();
                
                if (MCVUDialsHelper.getCurrentHealthValuePercent() != Math.round((entityCurrentHealth * 100)/entityMaxHealth)) {
                    MCVUDialsHelper.setCurrentHealthValuePercent(entityCurrentHealth, entityMaxHealth);
                    LOGGER.debug("Setting health dial to " + MCVUDialsHelper.getCurrentHealthValuePercent());
                    MCVUDialsControl.setDialValue(MCVUDialsHelper.dialUids[0], MCVUDialsHelper.getCurrentHealthValuePercent());

                    if (! MCVUDialsHelper.getCurrentHealthColors().equals(MCVUDialsHelper.getNewHealthColors(MCVUDialsHelper.getCurrentHealthValuePercent()))) {
                        MCVUDialsHelper.setCurrentHealthColors();
                        MCVUDialsControl.setDialColor(MCVUDialsHelper.dialUids[0], MCVUDialsHelper.getCurrentHealthColors().get("red"), MCVUDialsHelper.getCurrentHealthColors().get("green"), MCVUDialsHelper.getCurrentHealthColors().get("blue"));
                    }
                }
            }
        } catch (NullPointerException exception) {
            LOGGER.error("Error getting player entity!");
            exception.printStackTrace(printwriter);
            LOGGER.error(buffer.toString());
        }
    }

    @SubscribeEvent
    public void onServerTick(PlayerTickEvent event) {
        if (MCVUDialsHelper.getCurrentFoodLevelValuePercent() != Math.round((event.player.getFoodData().getFoodLevel()*100)/20)) {
            MCVUDialsHelper.setCurrentFoodLevelValuePercent(event.player.getFoodData().getFoodLevel(), 20);
            LOGGER.debug("Setting food dial to " + MCVUDialsHelper.getCurrentFoodLevelValuePercent());
            MCVUDialsControl.setDialValue(MCVUDialsHelper.dialUids[1], MCVUDialsHelper.getCurrentFoodLevelValuePercent());
            
            if (! MCVUDialsHelper.getCurrentFoodLevelColors().equals(MCVUDialsHelper.getNewFoodLevelColors(MCVUDialsHelper.getCurrentFoodLevelValuePercent()))) {
                MCVUDialsHelper.setCurrentFoodLevelColors();
                MCVUDialsControl.setDialColor(MCVUDialsHelper.dialUids[1], MCVUDialsHelper.getCurrentFoodLevelColors().get("red"), MCVUDialsHelper.getCurrentFoodLevelColors().get("green"), MCVUDialsHelper.getCurrentFoodLevelColors().get("blue"));
            }
        }
    }
}
