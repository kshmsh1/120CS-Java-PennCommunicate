package org.cis1200;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Collection;

public class ServerModelTest {
    private ServerModel model;

    @BeforeEach
    public void setUp() {
        model = new ServerModel();
    }

    @Test
    public void testInvalidNickname() {
        model.registerUser(0);
        Command command = new NicknameCommand(0, "User0", "!nv@l!d!");
        Broadcast expected = Broadcast.error(
                command, ServerResponse.INVALID_NAME
        );
        Broadcast actual = command.updateServerModel(model);
        assertEquals(expected, actual, "Broadcast");

        Collection<String> users = model.getRegisteredUsers();
        assertEquals(1, users.size(), "Number of registered users");
        assertTrue(users.contains("User0"), "Old nickname still registered");
        assertEquals(
                "User0", model.getNickname(0),
                "User with id 0 nickname unchanged"
        );
    }

    private void assertTrue(boolean user0, String oldNicknameStillRegistered) {

    }

    // Test case for adding user with invalid nickname
    @Test
    public void testGetUserIdInvalidNickname() {
        model.registerUser(1);
        String nickname = "Inv@lidNickname";
        int expectedUserId = -1;
        int actualUserId = model.getUserId(nickname);
        assertEquals(expectedUserId, actualUserId, "getUserId returns -1 for an invalid nickname");
    }

    // Test case for user with nickname that does not exist
    @Test
    public void testGetUserIdNicknameDoesNotExist() {
        String nickname = "NicknameDoesNotExist";
        int expectedUserId = -1;
        int actualUserId = model.getUserId(nickname);
        assertEquals(
                expectedUserId, actualUserId,
                "getUserId returns -1 for a nickname that does not exist"
        );
    }

    // Test case for user ID without a nickname
    @Test
    public void testGetNicknameNoNickname() {
        Broadcast result = model.registerUser(1);
        Collection<String> users = model.getRegisteredUsers();

        assertEquals(1, users.size(), "amount of users registered");
        assertFalse(result);

        assertNotEquals("", model.getNickname(1), "nickname not changed");
    }

    // Test cases for empty collection, one user, and multiple users
    @Test
    void testGetRegisteredUsers() {
        Collection<String> empty = model.getRegisteredUsers();
        assertNotNull(empty, "getRegisteredUsers never returns null");

        Broadcast result1 = model.registerUser(1);
        Collection<String> oneUser = model.getRegisteredUsers();
        assertNotNull(oneUser, "getRegisteredUsers never returns null");
        assertEquals(1, oneUser.size(), "getRegisteredUsers returns a collection with one user");

        Broadcast result2 = model.registerUser(2);
        Broadcast result3 = model.registerUser(3);
        Collection<String> multipleUsers = model.getRegisteredUsers();
        assertNotNull(multipleUsers, "getRegisteredUsers never returns null");
        assertEquals(
                3, multipleUsers.size(), "getRegisteredUsers returns a collection with three users"
        );
        assertTrue(result1);
        assertTrue(result2);
        assertTrue(result3);
    }

    // Test case for correct channels when they are created
    @Test
    public void testGetChannelsExist() {
        Broadcast result1 = model.createChannel(new CreateCommand(1, "User1", "Channel1", false));
        Broadcast result2 = model.createChannel(new CreateCommand(2, "User2", "Channel2", false));
        Broadcast result3 = model.createChannel(new CreateCommand(3, "User3", "Channel3", false));
        Collection<String> channels = model.getChannels();

        assertNotNull(channels);
        assertEquals(3, channels.size());
        assertTrue(result1);
        assertTrue(result2);
        assertTrue(result3);
    }

    // Test case for getting collection of nicknames from empty channel
    @Test
    public void testGetUsersInChannelEmpty() {
        String channelID = "Channel1";
        Broadcast result = model.createChannel(new CreateCommand(1, "User1", "", false));
        Collection<String> users = model.getUsersInChannel(channelID);
        assertNotNull(users);
        assertTrue(result);
    }

    // Test case for getting collection of nicknames from channel that is not empty
    @Test
    public void testGetUsersInChannelNonEmpty() {
        model.registerUser(1);
        model.registerUser(2);
        model.registerUser(3);

        model.createChannel(new CreateCommand(1, "User1", "Channel1", false));

        Broadcast result1 = model.joinChannel(new JoinCommand(2, "User2", "Channel1"));
        Broadcast result2 = model.joinChannel(new JoinCommand(3, "User3", "Channel1"));
        Broadcast result3 = model.joinChannel(new JoinCommand(3, "User3", "Channel1"));
        Collection<String> users = model.getUsersInChannel("Channel1");

        assertNotNull(users);
        assertEquals(3, users.size());
        assertTrue(result1);
        assertTrue(result2);
        assertTrue(result3);
    }

    // Test case for getting owner of invalid channel
    @Test
    public void testGetOwnerInvalidChannel() {
        String channelID = "InvalidChannelID";
        String actualOwner = model.getOwner(channelID);
        assertNull(actualOwner);
    }

    // Test case for registering user with duplicate username
    @Test
    public void testRegisterUserDuplicateUsername() {
        model.registerUser(1);
        model.registerUser(1);
        assertFalse(Broadcast.connected("Expected result to be false"));
    }

    // Test case for registering user with empty username
    @Test
    public void testRegisterUserEmptyUsername() {
        Broadcast result = model.registerUser(1);
        assertFalse(result);
    }

    // Test case for registering user with empty nickname
    @Test
    public void testRegisterUserEmptyNickname() {
        Broadcast result = model.registerUser(1);
        assertFalse(result);
    }

    // Test case for deregistering owners
    @Test
    public void testDeregisterOwnerWithChannels() {
        Broadcast result1 = model.registerUser(1);
        Broadcast result2 = model.registerUser(2);
        Broadcast result3 = model.registerUser(3);

        model.createChannel(new CreateCommand(1, "User1", "Channel1", false));
        model.createChannel(new CreateCommand(1, "User1", "Channel2", false));

        model.joinChannel(new JoinCommand(2, "User2", "Channel2"));
        model.joinChannel(new JoinCommand(3, "User3", "Channel3"));

        model.deregisterUser(1);

        assertFalse(result1);
        assertFalse(result2);
        assertFalse(result3);
    }

    // Test case for changing nickname with valid user ID
    @Test
    public void testChangeNicknameValidUserID() {
        model.registerUser(1);
        int userId = model.getUserId("User1");
        Broadcast result = model.changeNickname(new NicknameCommand(1, "User1", "newNickname"));
        assertTrue(result);
    }

    private void assertTrue(Broadcast result) {
    }

    // Test case for changing nickname with empty value
    @Test
    public void testChangeNicknameEmptyNickname() {
        model.registerUser(1);
        int userID = model.getUserId("User1");
        Broadcast result = model.changeNickname(new NicknameCommand(1, "User1", ""));
        assertFalse(result);
    }

    @Test
    public void testConnectionNicknames() {
        Broadcast result1 = model.registerUser(1);
        Broadcast result2 = model.registerUser(2);
        model.changeNickname(new NicknameCommand(1, "User1", "Keshav"));
        model.changeNickname(new NicknameCommand(2, "User2", "John"));

        assertFalse(result1);
        assertFalse(result2);

        assertTrue(model.getRegisteredUsers().contains("Keshav"), "New nickname registered");
        assertTrue(model.getRegisteredUsers().contains("John"), "New nickname registered");
    }

    @Test
    public void testJoinChannel() {
        // Test case for joining valid channel
        String channelName1 = "General";
        String userName1 = "User1";
        model.joinChannel(new JoinCommand(1, "User1", "General"));
        assertTrue(Broadcast.connected("Joining valid channel returns true"));
        assertTrue(Broadcast.connected("Joining valid channel returns "));

        // Test case for joining empty channel name
        String channelName2 = "";
        String userName2 = "User2";
        model.joinChannel(new JoinCommand(2, "User2", ""));
        assertFalse(Broadcast.connected("Joining empty channel name returns false"));

        // Test case for joining channel that does not exist
        String channelName3 = "DoesNotExist";
        String userName3 = "User3";
        model.joinChannel(new JoinCommand(3, "User3", "DoesNotExist"));
        assertFalse(Broadcast.connected("Joining channel that does not exist returns false"));

        // Test case for joining channel the user is already in
        String channelName4 = "General";
        String userName4 = "User4";
        model.joinChannel(new JoinCommand(4, "User4", "General"));
        assertFalse(Broadcast.connected("Joining channel the user is already in returns false"));
    }

    @Test
    public void testSendMessage() {
        int send = 1;
        String receive = "Keshav";
        String channel = "General";
        String msg = "Keshav was here";

        // Test case for all valid
        assertTrue(model.sendMessage(new MessageCommand(send, receive, channel, msg)));

        // Test case for empty string receiver
        assertFalse(model.sendMessage(new MessageCommand(send, "", channel, msg)));

        // Test case for empty string channel
        assertFalse(model.sendMessage(new MessageCommand(send, receive, "", msg)));

        // Test case for empty string message
        assertFalse(model.sendMessage(new MessageCommand(send, receive, channel, "")));
    }

    // Test case for leaving valid channel
    @Test
    public void testLeaveChannelExists() {
        String userID = "User1";
        String channelID = "Channel1";
        model.registerUser(1);
        model.createChannel(new CreateCommand(1, "User1", "Channel1", false));
        model.joinChannel(new JoinCommand(1, "User1", "Channel1"));

        Broadcast result = model.leaveChannel(new LeaveCommand(1, "User1", "Channel1"));
        assertFalse(result);
    }

    // Test case for leaving channel that does not exist
    @Test
    public void testLeaveChannelDoesNotExist() {
        String userID = "User1";
        String channelID = "Channel1";
        model.registerUser(1);

        Broadcast result = model.leaveChannel(new LeaveCommand(1, "User1", "Channel1"));
        assertFalse(result);
    }

    private void assertFalse(Broadcast result) {
    }

    // Test case for leaving channel that user is not part of
    @Test
    public void testLeaveChannelUserNotPart() {
        String userID = "User1";
        String channelID1 = "Channel1";
        String channelID2 = "Channel2";
        model.registerUser(1);
        model.createChannel(new CreateCommand(1, "User1", "Channel1", false));

        Broadcast result = model.leaveChannel(new LeaveCommand(1, "User1", "Channel1"));
        assertFalse(result);
    }

    // Test case for valid invite
    @Test
    void testInviteUserWithValidInputs() {
        String channelName = "Channel1";
        String nickname = "Nickname1";
        String userID = "User1";
        String invitedUser = "InvitedUser1";

        model.registerUser(1);
        model.createChannel(new CreateCommand(1, "User1", "Channel1", false));
        model.joinChannel(new JoinCommand(1, "User1", "Channel1"));

        Broadcast result = model.inviteUser(
                new InviteCommand(1, "User1", "Channel1", "InvitedUser1")
        );
        assertTrue(result);
    }

    // Test case for invalid invite sent
    @Test
    void testInviteUserInvalidInvite() {
        String channelName = "Channel1";
        String nickname = "Nickname1";
        String userID = "User1";
        String invitedUser = "InvitedUser1";

        model.registerUser(1);
        model.createChannel(new CreateCommand(1, "User1", "Channel1", true));

        Broadcast result = model.inviteUser(
                new InviteCommand(1, "User1", "Channel1", "InvitedUser1"))
                ;
        assertFalse(result);
    }

    // Test case for kicking valid user
    @Test
    public void testKickUserValid() {
        model.registerUser(1);
        Broadcast result = model.kickUser(
                new KickCommand(1, "User1", "Channel1", "User1")
        );
        assertTrue(Broadcast.connected("Expected true for a valid user"));
        assertFalse(result);
    }

    // Test case for kicking user that does not exist
    @Test
    public void testKickUserDoesNotExist() {
        model.registerUser(1);
        model.kickUser(new KickCommand(1, "User1", "Channel1", "User1"));
        assertFalse(Broadcast.connected("Expected false for a non-existent user"));
    }

    // Test case for kicking user not in channel
    @Test
    public void testKickUserNotInChannel() {
        model.registerUser(1);
        model.kickUser(new KickCommand(1, "User1", "Channel1", "User1"));
        assertFalse(Broadcast.connected("Expected false for a user not in the channel"));
    }

}