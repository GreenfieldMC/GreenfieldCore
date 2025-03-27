package com.njdaeger.greenfieldcore.testresult;

import com.njdaeger.greenfieldcore.GreenfieldCore;
import com.njdaeger.greenfieldcore.Module;
import com.njdaeger.greenfieldcore.ModuleConfig;
import com.njdaeger.greenfieldcore.shared.services.IVaultService;
import com.njdaeger.greenfieldcore.shared.services.VaultServiceImpl;
import com.njdaeger.greenfieldcore.testresult.services.ITestResultService;
import com.njdaeger.greenfieldcore.testresult.services.ITestResultStorageService;
import com.njdaeger.greenfieldcore.testresult.services.TestResultCommandService;
import com.njdaeger.greenfieldcore.testresult.services.TestResultServiceImpl;
import com.njdaeger.greenfieldcore.testresult.services.TestResultStorageServiceImpl;

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
