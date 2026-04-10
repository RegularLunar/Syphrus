package com.syphrus;

import com.syphrus.modules.TPAHelper;
import meteordevelopment.meteorclient.addons.MeteorAddon;
import meteordevelopment.meteorclient.systems.modules.Category;
import meteordevelopment.meteorclient.systems.modules.Modules;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Addon extends MeteorAddon {
    public static final Logger LOG = LoggerFactory.getLogger(Addon.class);
    public static final Category CATEGORY = new Category("Syphrus");

    @Override
    public void onInitialize() {
        LOG.info("Initializing Syphrus...");

        Modules.get().add(new TPAHelper());
    }

    @Override
    public void onRegisterCategories() {
        Modules.registerCategory(CATEGORY);
    }

    @Override
    public String getPackage() {
        return "com.syphrus";
    }
}
