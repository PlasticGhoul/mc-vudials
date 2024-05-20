package de.plasticghoul.vudials;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.config.ModConfigEvent;

/**
 * Main class that handles the configuration
 */
@Mod.EventBusSubscriber(modid = MCVUDials.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class MCVUDialsConfig {
    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();

    private static final ForgeConfigSpec.BooleanValue CONFIG_VUSERVERENABLED = BUILDER
            .comment("Enable VU Dials")
            .define("vuServerEnabled", true);
    private static final ForgeConfigSpec.ConfigValue<String> CONFIG_VUSERVERHOSTNAME = BUILDER
            .comment("Hostname of the VU Server instance (Default: localhost)")
            .define("vuServerHostname", "localhost");
    private static final ForgeConfigSpec.IntValue CONFIG_VUSERVERPORT = BUILDER
            .comment("Port of the VU Server instance (Default: 5340)")
            .defineInRange("vuServerPort", 5340, 0, 65535);
    private static final ForgeConfigSpec.ConfigValue<String> CONFIG_VUSERVERAPIKEY = BUILDER
            .comment("API key for the VU Server instance")
            .define("vuServerApiKey", "");

    static final ForgeConfigSpec SPEC = BUILDER.build();

    /** True if this mod is enabled */
    private static boolean vuServerEnabled;

    /** Hostname of the VU Server (default: localhost) */
    private static String vuServerHostname;

    /** Port of the VU Server (0-65535, defaul: 5340) */
    private static int vuServerPort;

    /** API Key for API access */
    private static String vuServerApiKey;

    /** API base URL that gets assembled by onLoad() */
    private static String vuServerApiBaseUrl;

    private MCVUDialsConfig() {

    }

    /**
     * This event method triggers on mod loading.
     * During this event, the mod will read the configuration file and saves it's contents to variables
     * 
     * @param   event   The PlayerTickEvent that is needed so this method triggers
     */
    @SubscribeEvent
    static void onLoad(final ModConfigEvent event) {
        vuServerEnabled = CONFIG_VUSERVERENABLED.get();
        vuServerHostname = CONFIG_VUSERVERHOSTNAME.get();
        vuServerPort = CONFIG_VUSERVERPORT.get();
        vuServerApiKey = CONFIG_VUSERVERAPIKEY.get();

        if (!vuServerHostname.isEmpty() && vuServerPort > 0) {
            vuServerApiBaseUrl = "http://" + vuServerHostname + ":" + vuServerPort;
        }
    }

    public static boolean isVuServerEnabled() {
        return vuServerEnabled;
    }

    public static String getVuServerHostname() {
        return vuServerHostname;
    }

    public static int getVuServerPort() {
        return vuServerPort;
    }

    public static String getVuServerApiKey() {
        return vuServerApiKey;
    }

    public static String getVuServerApiBaseUrl() {
        return vuServerApiBaseUrl;
    }
}
