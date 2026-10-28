package net.greenfieldmc.core.testresult;

import com.njdaeger.pdk.serviceprovider.IServiceProvider;
import com.njdaeger.pdk.serviceprovider.ServiceProviderBuilder;
import net.greenfieldmc.core.IGreenfieldCorePlugin;
import net.greenfieldmc.core.Module;
import net.greenfieldmc.core.ModuleConfig;
import net.greenfieldmc.core.testresult.services.ITestResultService;
import net.greenfieldmc.core.testresult.services.ITestResultStorageService;
import net.greenfieldmc.core.testresult.services.TestResultCommandService;
import net.greenfieldmc.core.testresult.services.TestResultServiceImpl;
import net.greenfieldmc.core.testresult.services.TestResultStorageServiceImpl;

public final class TestResultModule extends Module {

    public TestResultModule(IGreenfieldCorePlugin plugin) {
        super(plugin, ModuleConfig::isTestResultsEnabled);
    }

    @Override
    public IServiceProvider buildModuleServices(ServiceProviderBuilder serviceProviderBuilder) {
        return serviceProviderBuilder
                .addSingleton(ITestResultStorageService.class, TestResultStorageServiceImpl.class)
                .addSingleton(ITestResultService.class, TestResultServiceImpl.class)
                .build(plugin);
    }

    @Override
    protected void tryEnable() throws Exception {
        moduleServiceProvider.initialize(TestResultCommandService.class);
    }

    @Override
    public void tryDisable() {
    }
}
