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

    private static final ForgeConfigSpec.BooleanValue VUSERVERENABLED = BUILDER
            .comment("Enable VU Dials")
            .define("vuServerEnabled", true);
    private static final ForgeConfigSpec.ConfigValue<String> VUSERVERHOSTNAME = BUILDER
            .comment("Hostname of the VU Server instance (Default: localhost)")
            .define("vuServerHostname", "localhost");
    private static final ForgeConfigSpec.IntValue VUSERVERPORT = BUILDER
            .comment("Port of the VU Server instance (Default: 5340)")
            .defineInRange("vuServerPort", 5340, 0, 65535);
    private static final ForgeConfigSpec.ConfigValue<String> VUSERVERAPIKEY = BUILDER
            .comment("API key for the VU Server instance")
            .define("vuServerApiKey", "");

    static final ForgeConfigSpec SPEC = BUILDER.build();

    /** True if this mod is enabled */
    public static boolean vuServerEnabled;

    /** Hostname of the VU Server (default: localhost) */
    public static String vuServerHostname;

    /** Port of the VU Server (0-65535, defaul: 5340) */
    public static int vuServerPort;

    /** API Key for API access */
    public static String vuServerApiKey;

    /** API base URL that gets assembled by onLoad() */
    public static String vuServerApiBaseUrl;

    /**
     * This event method triggers on mod loading.
     * During this event, the mod will read the configuration file and saves it's contents to variables
     * 
     * @param   event   The PlayerTickEvent that is needed so this method triggers
     */
    @SubscribeEvent
    static void onLoad(final ModConfigEvent event) {
        vuServerEnabled = VUSERVERENABLED.get();
        vuServerHostname = VUSERVERHOSTNAME.get();
        vuServerPort = VUSERVERPORT.get();
        vuServerApiKey = VUSERVERAPIKEY.get();

        if (!vuServerHostname.isEmpty() && vuServerPort > 0) {
            vuServerApiBaseUrl = "http://" + vuServerHostname + ":" + vuServerPort;
        }
    }
}
