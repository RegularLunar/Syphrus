package com.syphrus.modules;

import com.syphrus.addon;
import meteordevelopment.meteorclient.events.game.ReceiveMessageEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.friends.Friends;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import meteordevelopment.orbit.EventHandler;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class tpahelper extends Module {
    public enum Mode { Accept, Deny }

    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup sgCustomization = settings.createGroup("Customization");

    private final Setting<Boolean> whitelistOnly = sgGeneral.add(new BoolSetting.Builder()
        .name("whitelist-only")
        .description("Only accepts players on the whitelist. Ignores everyone else (unless blacklisted).")
        .defaultValue(false)
        .build());

    private final Setting<Mode> mode = sgGeneral.add(new EnumSetting.Builder<Mode>()
        .name("mode")
        .defaultValue(Mode.Accept)
        .visible(() -> !whitelistOnly.get())
        .build());

    private final Setting<Boolean> friendsOnly = sgGeneral.add(new BoolSetting.Builder()
        .name("friends-only")
        .defaultValue(true)
        .visible(() -> !whitelistOnly.get())
        .build());

    private final Setting<List<String>> whitelist = sgGeneral.add(new StringListSetting.Builder()
        .name("whitelist")
        .defaultValue(List.of())
        .build());

    private final Setting<List<String>> blacklist = sgGeneral.add(new StringListSetting.Builder()
        .name("blacklist")
        .description("Players in this list will always be denied.")
        .defaultValue(List.of())
        .build());

    private final Setting<Integer> delay = sgCustomization.add(new IntSetting.Builder()
        .name("delay-ticks")
        .description("Ticks to wait before responding (20 ticks = 1 sec).")
        .defaultValue(0)
        .min(0)
        .sliderMax(200)
        .build());

    private final Setting<Boolean> ignoreTpaHere = sgCustomization.add(new BoolSetting.Builder()
        .name("ignore-tp-here")
        .defaultValue(false)
        .build());

    private final Setting<String> acceptCmd = sgCustomization.add(new StringSetting.Builder()
        .name("accept-command")
        .defaultValue("/tpaccept")
        .build());

    private final Setting<String> denyCmd = sgCustomization.add(new StringSetting.Builder()
        .name("deny-command")
        .defaultValue("/tpdeny")
        .build());

    private final Pattern tpaPattern = Pattern.compile("([A-Za-z0-9_]{3,16})\\s+(has requested|wants|requested)");
    private final List<PendingRequest> queue = new ArrayList<>();

    public tpahelper() {
        super(addon.CATEGORY, "tpa-helper", "The ultimate TPA management tool.");
    }

    @EventHandler
    private void onReceiveMessage(ReceiveMessageEvent event) {
        String msg = event.getMessage().getString().replaceAll("§.", "");
        if (msg == null || !msg.toLowerCase().contains("teleport")) return;

        if (ignoreTpaHere.get() && (msg.contains("to them") || msg.contains("to their"))) return;

        Matcher matcher = tpaPattern.matcher(msg);
        if (matcher.find()) {
            String sender = matcher.group(1);

            if (isPlayerInList(sender, blacklist.get())) {
                queueRequest(sender, false, "Blacklisted");
                return;
            }

            if (isPlayerInList(sender, whitelist.get())) {
                queueRequest(sender, true, "Whitelisted");
                return;
            }

            if (whitelistOnly.get()) {
                return;
            }

            boolean isFriend = Friends.get().get(sender) != null;
            if (friendsOnly.get() && !isFriend) return;

            queueRequest(sender, mode.get() == Mode.Accept, isFriend ? "Friend" : "Global Mode");
        }
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        if (queue.isEmpty()) return;

        queue.removeIf(req -> {
            req.timer--;
            if (req.timer <= 0) {
                ChatUtils.sendPlayerMsg(req.accept ? acceptCmd.get() : denyCmd.get());
                if (req.accept) info("Accepted TPA from (highlight)%s (default)[%s]", req.name, req.reason);
                else warning("Denied TPA from (highlight)%s (default)[%s]", req.name, req.reason);
                return true;
            }
            return false;
        });
    }

    private void queueRequest(String name, boolean accept, String reason) {
        queue.add(new PendingRequest(name, accept, reason, delay.get()));
    }

    private boolean isPlayerInList(String name, List<String> list) {
        return list.stream().anyMatch(n -> n.equalsIgnoreCase(name));
    }

    private static class PendingRequest {
        String name, reason;
        boolean accept;
        int timer;

        PendingRequest(String name, boolean accept, String reason, int timer) {
            this.name = name;
            this.accept = accept;
            this.reason = reason;
            this.timer = timer;
        }
    }
}
