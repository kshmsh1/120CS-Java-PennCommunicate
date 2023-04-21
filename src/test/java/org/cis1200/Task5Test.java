package org.cis1200;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Set;
import java.util.TreeSet;

/**
 * These tests should be written for testing the server's handling of invite-only
 * channels and kicking users.
 */
public class Task5Test {
    private ServerModel model;

    @BeforeEach
    public void setUp() {
        model = new ServerModel();
        model.registerUser(0); // add user with id = 0
        model.registerUser(1); // add user with id = 1

        Command create = new CreateCommand(0, "User0", "java", true);

        create.updateServerModel(model);
    }

    @Test
    public void testInviteByOwner() {
        Command invite = new InviteCommand(0, "User0", "java", "User1");
        Set<String> recipients = new TreeSet<>();
        recipients.add("User1");
        recipients.add("User0");
        Broadcast expected = Broadcast.names(invite, recipients, "User0");
        assertEquals(expected, invite.updateServerModel(model), "broadcast");

        assertEquals(2, model.getUsersInChannel("java").size(), "num. users in channel");
        assertTrue(model.getUsersInChannel("java").contains("User0"), "User0 in channel");
        assertTrue(model.getUsersInChannel("java").contains("User1"), "User1 in channel");
    }

    @Test
    public void testInviteByNonOwner() {
        model.registerUser(2);
        Command inviteValid = new InviteCommand(0, "User0", "java", "User1");
        inviteValid.updateServerModel(model);

        Command inviteInvalid = new InviteCommand(1, "User1", "java", "User2");
        Broadcast expected = Broadcast.error(inviteInvalid, ServerResponse.USER_NOT_OWNER);
        assertEquals(expected, inviteInvalid.updateServerModel(model), "error");

        assertEquals(2, model.getUsersInChannel("java").size(), "num. users in channel");
        assertTrue(model.getUsersInChannel("java").contains("User0"), "User0 in channel");
        assertTrue(model.getUsersInChannel("java").contains("User1"), "User1 in channel");
        assertFalse(model.getUsersInChannel("java").contains("User2"), "User2 not in channel");
    }

    @Test
    public void testKickOneChannel() {
        Command invite = new InviteCommand(0, "User0", "java", "User1");
        invite.updateServerModel(model);

        Command kick = new KickCommand(0, "User0", "java", "User1");
        Set<String> recipients = new TreeSet<>();
        recipients.add("User1");
        recipients.add("User0");
        Broadcast expected = Broadcast.okay(kick, recipients);
        assertEquals(expected, kick.updateServerModel(model));

        assertEquals(1, model.getUsersInChannel("java").size(), "num. users in channel");
        assertTrue(model.getUsersInChannel("java").contains("User0"), "User0 still in channel");
        assertFalse(model.getUsersInChannel("java").contains("User1"), "User1 still in channel");
    }
}