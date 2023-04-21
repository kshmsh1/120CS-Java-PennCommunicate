package org.cis1200;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;

/**
 * These tests should be written for testing client connection and disconnection,
 * and nickname changes.
 */
public class Task3Test {

    private ServerModel model;

    @BeforeEach
    public void setUp() {
        model = new ServerModel();
    }

    @Test
    public void testEmptyOnInit() {
        assertTrue(model.getRegisteredUsers().isEmpty(), "No registered users");
    }

    @Test
    public void testRegisterSingleUser() {
        Broadcast expected = Broadcast.connected("User0");
        assertEquals(expected, model.registerUser(0), "Broadcast");

        Collection<String> registeredUsers = model.getRegisteredUsers();
        assertEquals(1, registeredUsers.size(), "Num. registered users");
        assertTrue(registeredUsers.contains("User0"), "User0 is registered");
    }

    @Test
    public void testRegisterMultipleUsers() {
        Broadcast expected0 = Broadcast.connected("User0");
        assertEquals(expected0, model.registerUser(0), "Broadcast for User0");
        Broadcast expected1 = Broadcast.connected("User1");
        assertEquals(expected1, model.registerUser(1), "Broadcast for User1");
        Broadcast expected2 = Broadcast.connected("User2");
        assertEquals(expected2, model.registerUser(2), "Broadcast for User2");

        Collection<String> registeredUsers = model.getRegisteredUsers();
        assertEquals(3, registeredUsers.size(), "Num. registered users");
        assertTrue(registeredUsers.contains("User0"), "User0 is registered");
        assertTrue(registeredUsers.contains("User1"), "User1 is registered");
        assertTrue(registeredUsers.contains("User2"), "User2 is registered");
    }

    @Test
    public void testDeregisterSingleUser() {
        model.registerUser(0);
        model.deregisterUser(0);
        assertTrue(model.getRegisteredUsers().isEmpty(), "No registered users");
    }

    @Test
    public void testDeregisterOneOfManyUsers() {
        model.registerUser(0);
        model.registerUser(1);
        model.deregisterUser(0);
        assertFalse(model.getRegisteredUsers().isEmpty(), "Registered users still exist");
        assertFalse(model.getRegisteredUsers().contains("User0"), "User0 does not exist");
        assertTrue(model.getRegisteredUsers().contains("User1"), "User1 still exists");
    }

    @Test
    public void testNickNotInChannels() {
        model.registerUser(0);
        Command command = new NicknameCommand(0, "User0", "cis120");
        Set<String> recipients = Collections.singleton("cis120");
        Broadcast expected = Broadcast.okay(command, recipients);
        assertEquals(expected, command.updateServerModel(model), "Broadcast");
        Collection<String> users = model.getRegisteredUsers();
        assertFalse(users.contains("User0"), "Old nick not registered");
        assertTrue(users.contains("cis120"), "New nick registered");
    }

    @Test
    public void testNickCollision() {
        model.registerUser(0);
        model.registerUser(1);
        Command command = new NicknameCommand(0, "User0", "User1");
        Broadcast expected = Broadcast.error(command, ServerResponse.NAME_ALREADY_IN_USE);
        assertEquals(expected, command.updateServerModel(model), "Broadcast");
        Collection<String> users = model.getRegisteredUsers();
        assertTrue(users.contains("User0"), "Old nick still registered");
        assertTrue(users.contains("User1"), "Other user still registered");
    }

    @Test
    public void testNickCollisionOnConnect() {
        model.registerUser(0);
        Command command = new NicknameCommand(0, "User0", "User1");
        command.updateServerModel(model);
        Broadcast expected = Broadcast.connected("User0");
        assertEquals(expected, model.registerUser(1), "Broadcast");
        Collection<String> users = model.getRegisteredUsers();
        assertEquals(2, users.size(), "Num. registered users");
        assertTrue(users.contains("User0"), "User0 registered");
        assertTrue(users.contains("User1"), "User1 registered");
        assertEquals(0, model.getUserId("User1"), "User1 has ID 0");
        assertEquals(1, model.getUserId("User0"), "User0 has ID 1");
    }

    @Test
    public void testEncapsulationGetRegisteredUsers() {
        model.registerUser(0);
        model.registerUser(1);
        model.registerUser(2);
        Collection<String> usersBefore = model.getRegisteredUsers();
        try {
            usersBefore.remove("User0");
            usersBefore.remove("User1");
        } catch (UnsupportedOperationException uox) {
            // Ok to return a Collections.unmodifiableSet
        }
        Collection<String> usersAfter = model.getRegisteredUsers();
        assertTrue(usersAfter.contains("User0"), "User0 not removed");
        assertTrue(usersAfter.contains("User1"), "User1 not removed");
    }

}