package dev.meowmayo.mmcore.utils;

import dev.meowmayo.mmcore.events.MMChatEvent;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PartyUtils {
    private static boolean kuudraChange = true;
    private static boolean dungeonChange = true;

    private static boolean downtime = false;

    private static boolean inParty = false;
    private static String leader = "";
    private static final Set<String> party = new HashSet<>();

    private static boolean leaveFlag = false;

    private static final String playerName = Minecraft.getInstance().getUser().getName();
    
    // getter
    public static boolean getKuudraFlag() { return kuudraChange; }
    public static boolean getDungeonFlag() { return dungeonChange; }

    public static void useKuudraFlag() { kuudraChange = false; }
    public static void useDungeonFlag() { dungeonChange = false; }

    public static void requestDowntime() { downtime = true; }
    public static void useDowntimeFlag() { downtime = false; }
    public static boolean getDowntimeFlag() { return downtime; }

    public static boolean isLeader() { return leader.equals(playerName); }
    public static boolean isInParty() { return inParty; }
    public static Set<String> getParty() { return party; }

    public static boolean isPlayerInParty(String playerName) {
        if (playerName == null) return false;
        return party.contains(playerName);
    }

    private static final Pattern disbandPattern = Pattern.compile("^(.+) has disbanded the party!$");
    private static final Pattern removedPattern = Pattern.compile("^(.+) has been removed from the party\\.$");
    private static final Pattern memberLeftPattern = Pattern.compile("^(.+) has left the party\\.$");
    private static final Pattern transferLeavePattern = Pattern.compile("^The party was transferred to (.+) because (.+) left$");
    private static final Pattern transferByPattern = Pattern.compile("^The party was transferred to (.+) by (.+)$");
    private static final Pattern joinedPattern = Pattern.compile("^(.+) joined the party\\.$");
    private static final Pattern pfinderDungeonPattern = Pattern.compile("^Party Finder > (.+) joined the dungeon group! \\(.+\\)$");
    private static final Pattern pfinderGroupPattern = Pattern.compile("^Party Finder > (.+) joined the group! \\(.+\\)$");
    private static final Pattern youJoinedPattern = Pattern.compile("^You have joined (.+)'s party!$");
    private static final Pattern plistPattern = Pattern.compile("^Party Members \\((.+)\\)$");
    private static final Pattern leaderPattern = Pattern.compile("^Party Leader: (.+) ●$");
    private static final Pattern notInPartyPattern = Pattern.compile("^You are not currently in a party\\.$");
    private static final Pattern membersPattern = Pattern.compile("^Party Members:(.+)● $");
    private static final Pattern moderatorsPattern = Pattern.compile("^Party Moderators:(.+)● $");

    private static boolean expectingConnection = false;

    public static void init() {
        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
            expectingConnection = true;
        });

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (!expectingConnection) return;
            if (client.level == null || client.player == null) return;

            getPartyMembers();
            expectingConnection = false;
        });

        MMChatEvent.SYSTEM.register(event -> {
//            System.out.println(ChatFormatting.stripFormatting(event.content().getString()));
            onChat(ChatFormatting.stripFormatting(event.content().getString()));
        });
    }

    public static void onChat(String msg) {
        // disband
        if (disbandPattern.matcher(msg).matches() || msg.equals("The party was disbanded because all invites expired and the party was empty.")) {
            if(!ChatUtils.isSystemMessage(msg)) return;
            resetParty();
            return;
        }

        // left party
        if (msg.equals("You left the party.")) {
            leaveFlag = true;
            resetParty();
            DelayUtils.scheduleTask(() -> {leaveFlag = false;}, 1000);
            return;
        }

        // left member
        Matcher removed = removedPattern.matcher(msg);
        Matcher left = memberLeftPattern.matcher(msg);
        if (removed.matches() || left.matches()) {
            String player = removed.matches() ? removed.group(1) : left.group(1);
            party.remove(ChatUtils.stripRank(player));
            inParty = true;
            kuudraChange = dungeonChange = downtime = true;
            return;
        }

        // transfer because left
        Matcher tLeave = transferLeavePattern.matcher(msg);
        if (tLeave.matches()) {
            if (!leaveFlag) {
                leader = ChatUtils.stripRank(tLeave.group(1));
                party.remove(ChatUtils.stripRank(tLeave.group(2)));
                inParty = true;
            }
            return;
        }

        // transfer by
        Matcher tBy = transferByPattern.matcher(msg);
        if (tBy.matches()) {
            leader = ChatUtils.stripRank(tBy.group(1));
            inParty = true;
            return;
        }

        // joined party
        Matcher joined = joinedPattern.matcher(msg);
        if (joined.matches()) {
            String user = ChatUtils.stripRank(joined.group(1));
            party.add(user);
            if (party.size() == 1 && !inParty) { // this should make you leader if someone joins your party and the party doesnt exist
                party.add(playerName);
                leader = playerName;
            }
            inParty = true;
            kuudraChange = dungeonChange = downtime = true;
            return;
        }

        // Party Finder (dungeon group)
        Matcher pfDungeon = pfinderDungeonPattern.matcher(msg);
        Matcher pfGroup = pfinderGroupPattern.matcher(msg);
        if (pfDungeon.matches() || pfGroup.matches()) {
            String user = ChatUtils.stripRank(pfDungeon.matches() ? pfDungeon.group(1) : pfGroup.group(1));
            if (user.equals(playerName)) {
                getPartyMembers();
                return;
            }
            party.add(user);
            inParty = true;
            kuudraChange = dungeonChange = downtime = true;
            return;
        }

        // joined someone elses party
        Matcher youJoined = youJoinedPattern.matcher(msg);
        if (youJoined.matches()) {
            DelayUtils.scheduleTask(PartyUtils::getPartyMembers, 500);
            return;
        }

        // party list output
        Matcher plist = plistPattern.matcher(msg);
        if (plist.matches()) {
            party.clear();
            inParty = true;
            kuudraChange = dungeonChange = downtime = true;
            return;
        }

        Matcher lead = leaderPattern.matcher(msg);
        if (lead.matches()) {
            leader = ChatUtils.stripRank(lead.group(1));
            party.add(leader);
            inParty = true;
            return;
        }

        if (notInPartyPattern.matcher(msg).matches()) {
            resetParty();
            return;
        }

        Matcher members = membersPattern.matcher(msg);
        Matcher moderators = moderatorsPattern.matcher(msg);
        if (members.matches() || moderators.matches()) {
            String list = (members.matches() ? members.group(1) : moderators.group(1));
            String[] arr = list.split(" ● ");
            for (String m : arr) {
                String clean = ChatUtils.stripRank(m.trim());
                party.add(clean);
            }
        }
    }

    private static void resetParty() {
        inParty = false;
        leader = "";
        party.clear();
        kuudraChange = dungeonChange = downtime = true;
    }

    private static void getPartyMembers() {
        party.clear();
        ChatUtils.command("p list");
    }
}