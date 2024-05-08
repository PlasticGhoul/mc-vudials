package de.plasticghoul.vudials;

import com.mojang.logging.LogUtils;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraft.client.Minecraft;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.event.entity.living.LivingHealEvent;

import net.minecraftforge.event.entity.player.PlayerEvent.PlayerLoggedInEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.PlayerLoggedOutEvent;
import org.slf4j.Logger;

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
    
        if(MCVUDialsConfig.vuServerApiKey.isEmpty()) {
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
                    float playerMaxHealth = event.getEntity().getMaxHealth();
                    float playerCurrentHealth = event.getEntity().getHealth();
                    float playerCurrentHealthPercent = (playerCurrentHealth * 100) / playerMaxHealth;

                    MCVUDialsControl.setDialValue(MCVUDialsHelper.dialUids[0],
                            String.valueOf(playerCurrentHealthPercent));
                    if (playerCurrentHealthPercent > 50) {
                        LOGGER.debug("Setting health dial color to green");
                        MCVUDialsControl.setDialColor(MCVUDialsHelper.dialUids[0], 0, 100, 0);
                    } else if (playerCurrentHealthPercent > 25) {
                        LOGGER.debug("Setting health dial color to yellow");
                        MCVUDialsControl.setDialColor(MCVUDialsHelper.dialUids[0], 100, 100, 0);
                    } else if (playerCurrentHealthPercent <= 25) {
                        LOGGER.debug("Setting health dial color to red");
                        MCVUDialsControl.setDialColor(MCVUDialsHelper.dialUids[0], 100, 0, 0);
                    }
                    MCVUDialsControl.setDialImage(MCVUDialsHelper.dialUids[0], "health.png");
                }

                // Food
                if (MCVUDialsHelper.dialUids.length >= 2) {
                    int foodLevel = event.getEntity().getFoodData().getFoodLevel();
                    int maxFoodLevel = 20;
                    int foodLevelPercent = (foodLevel * 100) / maxFoodLevel;

                    MCVUDialsControl.setDialValue(MCVUDialsHelper.dialUids[1],
                            String.valueOf(foodLevelPercent));
                    if (foodLevelPercent > 50) {
                        LOGGER.debug("Setting food dial color to green");
                        MCVUDialsControl.setDialColor(MCVUDialsHelper.dialUids[1], 0, 100, 0);
                    } else if (foodLevelPercent > 25) {
                        LOGGER.debug("Setting food dial color to yellow");
                        MCVUDialsControl.setDialColor(MCVUDialsHelper.dialUids[1], 100, 100, 0);
                    } else if (foodLevelPercent <= 25) {
                        LOGGER.debug("Setting food dial color to red");
                        MCVUDialsControl.setDialColor(MCVUDialsHelper.dialUids[1], 100, 0, 0);
                    }
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
                MCVUDialsControl.setDialValue(MCVUDialsHelper.dialUids[0], "0");
                MCVUDialsControl.setDialColor(MCVUDialsHelper.dialUids[0], 0, 0, 0);
                MCVUDialsControl.setDialImage(MCVUDialsHelper.dialUids[0], "blank.png");
            }

            if (MCVUDialsHelper.dialUids.length >= 2) {
                MCVUDialsControl.setDialValue(MCVUDialsHelper.dialUids[1], "0");
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
                float entityCurrentHealth = event.getEntity().self().getHealth();
                float entityDamageAmount = event.getAmount();
                int entityCurrentHealthPercent = Math
                        .round(((entityCurrentHealth - entityDamageAmount) * 100) / entityMaxHealth);

                LOGGER.debug("Setting health dial to " + entityCurrentHealthPercent);
                MCVUDialsControl.setDialValue(MCVUDialsHelper.dialUids[0],
                        String.valueOf(entityCurrentHealthPercent));

                if (entityCurrentHealthPercent > 50) {
                    LOGGER.debug("Setting health dial color to green");
                    MCVUDialsControl.setDialColor(MCVUDialsHelper.dialUids[0], 0, 100, 0);
                } else if (entityCurrentHealthPercent > 25) {
                    LOGGER.debug("Setting health dial color to yellow");
                    MCVUDialsControl.setDialColor(MCVUDialsHelper.dialUids[0], 100, 100, 0);
                } else if (entityCurrentHealthPercent <= 25) {
                    LOGGER.debug("Setting health dial color to red");
                    MCVUDialsControl.setDialColor(MCVUDialsHelper.dialUids[0], 100, 0, 0);
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
                float entityCurrentHealth = event.getEntity().self().getHealth();
                float entityHealAmount = event.getAmount();
                int entityCurrentHealthPercent = Math
                        .round(((entityCurrentHealth + entityHealAmount) * 100) / entityMaxHealth);

                LOGGER.debug("Setting health dial to " + entityCurrentHealthPercent);
                MCVUDialsControl.setDialValue(MCVUDialsHelper.dialUids[0],
                        String.valueOf(entityCurrentHealthPercent));

                if (entityCurrentHealthPercent > 50) {
                    LOGGER.debug("Setting health dial color to green");
                    MCVUDialsControl.setDialColor(MCVUDialsHelper.dialUids[0], 0, 100, 0);
                } else if (entityCurrentHealthPercent > 25) {
                    LOGGER.debug("Setting health dial color to yellow");
                    MCVUDialsControl.setDialColor(MCVUDialsHelper.dialUids[0], 100, 100, 0);
                } else if (entityCurrentHealthPercent <= 25) {
                    LOGGER.debug("Setting health dial color to red");
                    MCVUDialsControl.setDialColor(MCVUDialsHelper.dialUids[0], 100, 0, 0);
                }

            }
        } catch (NullPointerException exception) {
            LOGGER.error("Error getting player entity!");
            exception.printStackTrace(printwriter);
            LOGGER.error(buffer.toString());
        }
    }
}
