package de.plasticghoul.vudials;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.config.ModConfigEvent;


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
    
    public static boolean vuServerEnabled;
    public static String vuServerHostname;
    public static int vuServerPort;
    public static String vuServerApiKey;
    public static String vuServerApiBaseUrl;

    @SubscribeEvent
    static void onLoad(final ModConfigEvent event)
    {
        vuServerEnabled = VUSERVERENABLED.get();
        vuServerHostname = VUSERVERHOSTNAME.get();
        vuServerPort = VUSERVERPORT.get();
        vuServerApiKey = VUSERVERAPIKEY.get();

        if (!vuServerHostname.isEmpty() && vuServerPort > 0) {
            vuServerApiBaseUrl = "http://" + vuServerHostname + ":" + vuServerPort;
        }
    }
}
