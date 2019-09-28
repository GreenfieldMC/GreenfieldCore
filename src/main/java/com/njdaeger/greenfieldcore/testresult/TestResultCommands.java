package com.njdaeger.greenfieldcore.testresult;

import com.njdaeger.bci.base.BCIException;
import com.njdaeger.bci.defaults.CommandContext;
import com.njdaeger.greenfieldcore.GreenfieldCore;

public class TestResultCommands {



    public TestResultCommands(GreenfieldCore plugin) {

    }


    private void pass(CommandContext context) {

    }

    private void fail(CommandContext context) {
    }

    private void test(CommandContext context) throws BCIException {
        context.subCommandAt(0, "pass", true, this::pass);
        context.subCommandAt(0, "fail", true, this::fail);
    }



}
