package com.njdaeger.greenfieldcore;

import com.njdaeger.greenfieldcore.codes.CodesModule;
import com.njdaeger.greenfieldcore.commandstore.CommandStoreModule;
import com.njdaeger.greenfieldcore.hotspots.HotspotModule;
import com.njdaeger.greenfieldcore.openserver.OpenServerModule;
import com.njdaeger.greenfieldcore.paintingswitch.PaintingSwitchModule;
import com.njdaeger.greenfieldcore.testresult.TestResultModule;
import com.njdaeger.greenfieldcore.utilities.UtilitiesModule;
import org.bukkit.plugin.java.JavaPlugin;

public final class GreenfieldCore extends JavaPlugin {
    
    private final OpenServerModule openServerModule = new OpenServerModule(this);
    private final CodesModule codesModule = new CodesModule(this);
    private final TestResultModule testResultModule = new TestResultModule(this);
    private final PaintingSwitchModule paintingSwitchModule = new PaintingSwitchModule(this);
    private final UtilitiesModule utilitiesModule = new UtilitiesModule(this);
    private final MCLinkIntegration mcLinkModule = new MCLinkIntegration(this);
    private final CommandStoreModule storeModule = new CommandStoreModule(this);
    private final HotspotModule hotspotModule = new HotspotModule(this);

    @Override
    public void onEnable() {
        openServerModule.onEnable();
        //signEditorModule.onEnable();
        codesModule.onEnable();
        //treePlanterModule.onEnable();
        testResultModule.onEnable();
        paintingSwitchModule.onEnable();
        utilitiesModule.onEnable();
        mcLinkModule.onEnable();
        storeModule.onEnable();
        hotspotModule.onEnable();
    }

    @Override
    public void onDisable() {
        testResultModule.onDisable();
        openServerModule.onDisable();
        codesModule.onDisable();
        storeModule.onDisable();
        hotspotModule.onDisable();
    }
}
