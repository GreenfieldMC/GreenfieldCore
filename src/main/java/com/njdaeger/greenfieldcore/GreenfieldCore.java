package com.njdaeger.greenfieldcore;

import com.njdaeger.greenfieldcore.codes.CodesModule;
import com.njdaeger.greenfieldcore.openserver.OpenServerModule;
import com.njdaeger.greenfieldcore.paintingswitch.PaintingSwitchModule;
import com.njdaeger.greenfieldcore.testresult.TestResultModule;
import com.njdaeger.greenfieldcore.utilities.UtilitiesModule;
import org.bukkit.plugin.java.JavaPlugin;

public final class GreenfieldCore extends JavaPlugin {
    
    private final OpenServerModule openServerModule = new OpenServerModule(this);
    //private final SignEditorModule signEditorModule = new SignEditorModule(this);
    private final CodesModule codesModule = new CodesModule(this);
    //private final TreePlanterModule treePlanterModule = new TreePlanterModule(this);
    private final TestResultModule testResultModule = new TestResultModule(this);
    private final PaintingSwitchModule paintingSwitchModule = new PaintingSwitchModule(this);
    private final UtilitiesModule utilitiesModule = new UtilitiesModule(this);
    private final MCLinkIntegration mcLinkModule = new MCLinkIntegration(this);
    
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
    }

    @Override
    public void onDisable() {
        openServerModule.onDisable();
        //signEditorModule.onDisable();
        codesModule.onDisable();
    }
}
