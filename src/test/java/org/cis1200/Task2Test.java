package org.cis1200;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

public class Task2Test {

    private ServerModel model;

    @BeforeEach
    public void setUp() {
        model = new ServerModel();
    }

    @Test
    public void testGetUserIdNonexistentUser() {
        assertEquals(-1, model.getUserId("Nick"));
    }

    @Test
    public void testGetNicknameNonexistentUser() {
        assertNull(model.getNickname(0));
    }

    @Test
    public void testGetRegisteredUsersEmpty() {
        assertTrue(model.getRegisteredUsers().isEmpty(), "No registered users");
    }

    @Test
    public void testGetChannelsEmpty() {
        assertTrue(model.getChannels().isEmpty(), "No channels");
    }

    @Test
    public void testGetUsersInChannelEmpty() {
        assertTrue(model.getUsersInChannel("java").isEmpty(), "No channels");
    }

    @Test
    public void testGetOwnerNonexistentChannel() {
        assertNull(model.getOwner("java"));
    }
}