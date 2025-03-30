package net.greenfieldmc.core.testresult;

import net.greenfieldmc.core.GreenfieldCore;
import net.greenfieldmc.core.Module;
import net.greenfieldmc.core.ModuleConfig;
import net.greenfieldmc.core.shared.services.IVaultService;
import net.greenfieldmc.core.shared.services.VaultServiceImpl;
import net.greenfieldmc.core.testresult.services.ITestResultService;
import net.greenfieldmc.core.testresult.services.ITestResultStorageService;
import net.greenfieldmc.core.testresult.services.TestResultCommandService;
import net.greenfieldmc.core.testresult.services.TestResultServiceImpl;
import net.greenfieldmc.core.testresult.services.TestResultStorageServiceImpl;

import java.util.function.Predicate;

public final class TestResultModule extends Module {

    private ITestResultStorageService storageService;
    private ITestResultService testResultService;
    private IVaultService permissionService;

    public TestResultModule(GreenfieldCore plugin, Predicate<ModuleConfig> canEnable) {
        super(plugin, canEnable);
    }

    @Override
    public void tryEnable() {
        this.storageService = enableIntegration(new TestResultStorageServiceImpl(plugin, this), true);
        this.permissionService = enableIntegration(new VaultServiceImpl(plugin, this), true);
        this.testResultService = enableIntegration(new TestResultServiceImpl(plugin, this, storageService, permissionService), true);
        enableIntegration(new TestResultCommandService(plugin, this, testResultService), true);
    }

    @Override
    public void tryDisable() {
        disableIntegration(testResultService);
        disableIntegration(permissionService);
        disableIntegration(storageService);
    }
}
