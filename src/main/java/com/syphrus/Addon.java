package com.syphrus;

import com.syphrus.modules.tpahelper;
import meteordevelopment.meteorclient.addons.MeteorAddon;
import meteordevelopment.meteorclient.systems.modules.Category;
import meteordevelopment.meteorclient.systems.modules.Modules;

public class addon extends MeteorAddon {
    public static final Category CATEGORY = new Category("Syphrus");

    @Override
    public void onInitialize() {

        Modules.get().add(new tpahelper());
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
