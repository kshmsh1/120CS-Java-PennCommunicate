package org.cis1200;

import java.util.*;

/**
 * The {@code ServerModel} is the class responsible for tracking the
 * state of the server, including its current users and the channels
 * they are in.
 * This class is used by subclasses of {@link Command} to:
 * 1. handle commands from clients, and
 * 2. handle commands from {@link ServerBackend} to coordinate
 * client connection/disconnection.
 */
public final class ServerModel {
    private TreeMap<Integer, String> currentUsers;
    private TreeMap<String, Channel> currentChannels;

    /**
     * Constructs a {@code ServerModel}. Initializes any collections
     * used to model the server state.
     */
    public ServerModel() {
        currentUsers = new TreeMap<>();
        currentChannels = new TreeMap<>();
    }

    /**
     * Gets the user ID currently associated with the given
     * nickname. The returned ID is -1 if the nickname is not
     * currently in use.
     *
     * @param nickname The nickname for which to get the associated user ID
     * @return The user ID of the user with the argued nickname if
     *         such a user exists, otherwise -1
     */
    public int getUserId(String nickname) {
        for (Map.Entry<Integer, String> entry : currentUsers.entrySet()) {
            if (entry.getValue().equals(nickname)) {
                return entry.getKey();
            }
        }
        return -1;
    }

    /**
     * Gets the nickname currently associated with the given user
     * ID. The returned nickname is null if the user ID is not
     * currently in use.
     *
     * @param userId The user ID for which to get the associated
     *               nickname
     * @return The nickname of the user with the argued user ID if
     *         such a user exists, otherwise null
     */
    public String getNickname(int userId) {
        String nickname = currentUsers.get(userId);
        if (nickname == null) {
            return null;
        }
        return nickname;
    }

    /**
     * Gets a collection of the nicknames of all users who are
     * registered with the server. Changes to the returned collection
     * do not affect the server state.
     *
     * @return The collection of registered user nicknames
     */
    public Collection<String> getRegisteredUsers() {
        TreeSet<String> regUsersCollection = new TreeSet<>();
        for (Map.Entry<Integer, String> entry : currentUsers.entrySet()) {
            String name = "" + entry.getValue();
            regUsersCollection.add(name);
        }
        return regUsersCollection;
    }

    /**
     * Gets a collection of the names of all the channels that are
     * present on the server. Changes to the returned collection
     * do not affect the server state.
     *
     * @return The collection of channel names
     */
    public Collection<String> getChannels() {
        TreeSet<String> channelNames = new TreeSet<>();
        for (Map.Entry<String, Channel> entry : currentChannels.entrySet()) {
            String name = "" + entry.getValue().getName();
            channelNames.add(name);
        }
        return channelNames;
    }

    /**
     * Gets a collection of the nicknames of all the users in a given
     * channel. The collection is empty if no channel with the given
     * name exists. Modifications to the returned collection should
     * not affect the server state.
     *
     * @param channelName The channel for which to get member nicknames
     * @return A collection of all user nicknames in the channel
     */
    public Collection<String> getUsersInChannel(String channelName) {
        TreeSet<String> nicknamesCollection = new TreeSet<>();
        for (Map.Entry<String, Channel> entry1 : currentChannels.entrySet()) {
            if (channelName.equals(entry1.getValue().getName())) {
                for (Map.Entry<Integer, String> entry2 : entry1.getValue().getUsers().entrySet()) {
                    String name = "" + entry2.getValue();
                    nicknamesCollection.add(name);
                }
            }
        }
        return nicknamesCollection;
    }

    /**
     * Gets the nickname of the owner of the given channel. The result
     * is {@code null} if no channel with the given name exists.
     *
     * @param channelName The channel for which to get the owner nickname
     * @return The nickname of the channel owner if such a channel
     *         exists; otherwise, return null
     */
    public String getOwner(String channelName) {
        String name = null;
        for (Map.Entry<String, Channel> entry : currentChannels.entrySet()) {
            if (entry.getValue().getName().equals(channelName)) {
                name = entry.getValue().getOwner();
            }
        }
        return name;
    }

    /**
     * This method is automatically called by the backend when a new client
     * connects to the server. It generates a default nickname with
     * {@link #generateUniqueNickname()}, stores the new user's ID and username
     * in your data structures for {@link ServerModel} state, and constructs
     * and returns a {@link Broadcast} object using
     * {@link Broadcast#connected(String)}}.
     *
     * @param userId The new user's unique ID (automatically created by the
     *               backend)
     * @return The {@link Broadcast} object generated by calling
     *         {@link Broadcast#connected(String)} with the proper parameter
     */
    public Broadcast registerUser(int userId) {
        String nickname = generateUniqueNickname();
        currentUsers.put(userId, nickname);
        return Broadcast.connected(nickname);
    }

    /**
     * Helper for {@link #registerUser(int)}.
     * Generates a unique nickname of the form "UserX", where X is the
     * smallest non-negative integer that yields a unique nickname for a user.
     * 
     * @return The generated nickname
     */
    private String generateUniqueNickname() {
        int suffix = 0;
        String nickname;
        Collection<String> existingUsers = getRegisteredUsers();
        do {
            nickname = "User" + suffix++;
        } while (existingUsers.contains(nickname));
        return nickname;
    }

    /**
     * This method is automatically called by the backend when a client
     * disconnects from the server.
     * (1) All users who shared a channel with the disconnected user should be
     * notified that they left
     * (2) All channels owned by the disconnected user should be deleted
     * (3) The disconnected user's information should be removed from
     * {@link ServerModel}'s internal state
     * (4) Construct and return a {@link Broadcast} object using
     * {@link Broadcast#disconnected(String, Collection)}.
     *
     * @param userId The unique ID of the user to deregister
     * @return The {@link Broadcast} object generated by calling
     *         {@link Broadcast#disconnected(String, Collection)} with the proper
     *         parameters
     */
    public Broadcast deregisterUser(int userId) {
        String removeInfo = currentUsers.get(userId);

        TreeSet<Channel> removeUser = new TreeSet<>();
        TreeSet<String> notifyUser = new TreeSet<>();

        for (Map.Entry<String, Channel> entry1 : currentChannels.entrySet()) {
            if (entry1.getValue().getUsers().containsKey(userId)) {
                if (getNickname(userId).equals(entry1.getValue().getOwner())) {
                    removeUser.add(entry1.getValue());
                }
                entry1.getValue().deleteUser(userId);
                for (Map.Entry<Integer, String> entry2 : entry1.getValue().getUsers().entrySet()) {
                    notifyUser.add(entry2.getValue());
                }
            }
        }
        for (Channel c2 : removeUser) {
            currentChannels.remove(c2);
        }
        currentUsers.remove(userId);
        return Broadcast.disconnected(removeInfo, notifyUser);
    }

    /**
     * This method is called when a user wants to change their nickname.
     * 
     * @param nickCommand The {@link NicknameCommand} object containing
     *                    all information needed to attempt a nickname change
     * @return The {@link Broadcast} object generated by
     *         {@link Broadcast#okay(Command, Collection)} if the nickname
     *         change is successful. The command should be the original nickCommand
     *         and the collection of recipients should be any clients who
     *         share at least one channel with the sender, including the sender.
     *
     *         If an error occurs, use
     *         {@link Broadcast#error(Command, ServerResponse)} with either:
     *         (1) {@link ServerResponse#INVALID_NAME} if the proposed nickname
     *         is not valid according to
     *         {@link ServerModel#isValidName(String)}
     *         (2) {@link ServerResponse#NAME_ALREADY_IN_USE} if there is
     *         already a user with the proposed nickname
     */
    public Broadcast changeNickname(NicknameCommand nickCommand) {
        String oldNickname = nickCommand.getSender();
        String newNickname = nickCommand.getNewNickname();
        int iD = nickCommand.getSenderId();
        TreeSet<String> notifyUser = new TreeSet<>();

        if (!isValidName(newNickname)) {
            return Broadcast.error(nickCommand, ServerResponse.INVALID_NAME);
        }

        for (Map.Entry<Integer, String> entry1 : currentUsers.entrySet()) {
            if (entry1.getValue().equals(newNickname)) {
                return Broadcast.error(nickCommand, ServerResponse.NAME_ALREADY_IN_USE);
            }
        }

        currentUsers.remove(iD);
        currentUsers.put(iD, newNickname);

        for (Map.Entry<String, Channel> entry2 : currentChannels.entrySet()) {
            if (entry2.getValue().getUsers().containsKey(iD)) {
                if (entry2.getValue().getOwner().equals(oldNickname)) {
                    entry2.getValue().setOwner(newNickname);
                }
                entry2.getValue().deleteUser(iD);
                entry2.getValue().addUser(iD, newNickname);
                for (Map.Entry<Integer, String> entry : entry2.getValue().getUsers().entrySet()) {
                    notifyUser.add(entry.getValue());
                }
            }
        }
        return Broadcast.okay(nickCommand, notifyUser);
    }

    /**
     * Determines if a given nickname is valid or invalid (contains at least
     * one alphanumeric character, and no non-alphanumeric characters).
     * 
     * @param name The channel or nickname string to validate
     * @return true if the string is a valid name
     */
    public static boolean isValidName(String name) {
        if (name == null || name.isEmpty()) {
            return false;
        }
        for (char c : name.toCharArray()) {
            if (!Character.isLetterOrDigit(c)) {
                return false;
            }
        }
        return true;
    }

    /**
     * This method is called when a user wants to create a channel.
     * 
     * @param createCommand The {@link CreateCommand} object containing all
     *                      information needed to attempt channel creation
     * @return The {@link Broadcast} object generated by
     *         {@link Broadcast#okay(Command, Collection)} if the channel
     *         creation is successful. The only recipient should be the new
     *         channel's owner.
     *
     *         If an error occurs, use
     *         {@link Broadcast#error(Command, ServerResponse)} with either:
     *         (1) {@link ServerResponse#INVALID_NAME} if the proposed
     *         channel name is not valid according to
     *         {@link ServerModel#isValidName(String)}
     *         (2) {@link ServerResponse#CHANNEL_ALREADY_EXISTS} if there is
     *         already a channel with the proposed name
     */
    public Broadcast createChannel(CreateCommand createCommand) {
        String channelName = createCommand.getChannel();
        TreeSet<String> notifyUser = new TreeSet<>();

        if (!isValidName(channelName)) {
            return Broadcast.error(createCommand, ServerResponse.INVALID_NAME);
        }

        for (Map.Entry<String, Channel> entry : currentChannels.entrySet()) {
            if (channelName.equals(entry.getValue().getName())) {
                return Broadcast.error(createCommand, ServerResponse.CHANNEL_ALREADY_EXISTS);
            }
        }

        Channel c2 = new Channel(
                createCommand.getChannel(), createCommand.getSender(), createCommand.getSenderId(),
                createCommand.isInviteOnly()
        );
        currentChannels.put(channelName, c2);
        notifyUser.add(createCommand.getSender());
        return Broadcast.okay(createCommand, notifyUser);
    }

    /**
     * This method is called when a user wants to join a channel.
     * 
     * @param joinCommand The {@link JoinCommand} object containing all
     *                    information needed for the user's join attempt
     * @return The {@link Broadcast} object generated by
     *         {@link Broadcast#names(Command, Collection, String)} if the user
     *         joins the channel successfully. The recipients should be all
     *         people in the joined channel (including the sender).
     *
     *         If an error occurs, use
     *         {@link Broadcast#error(Command, ServerResponse)} with either:
     *         (1) {@link ServerResponse#NO_SUCH_CHANNEL} if there is no
     *         channel with the specified name
     *         (2) (after Task 5) {@link ServerResponse#JOIN_PRIVATE_CHANNEL} if
     *         the sender is attempting to join a private channel
     */
    public Broadcast joinChannel(JoinCommand joinCommand) {
        String channelName = joinCommand.getChannel();
        Channel x = null;
        TreeSet<String> notifyUser = new TreeSet<>();

        for (Map.Entry<String, Channel> entry : currentChannels.entrySet()) {
            if (entry.getValue().getName().equals(channelName)) {
                x = entry.getValue();
            }
        }

        if (x == null) {
            return Broadcast.error(joinCommand, ServerResponse.NO_SUCH_CHANNEL);
        }
        if (x.isPrivate()) {
            return Broadcast.error(joinCommand, ServerResponse.JOIN_PRIVATE_CHANNEL);
        }

        x.addUser(joinCommand.getSenderId(), joinCommand.getSender());

        for (Map.Entry<Integer, String> entry : x.getUsers().entrySet()) {
            notifyUser.add(entry.getValue());
        }
        return Broadcast.names(joinCommand, notifyUser, x.getOwner());
    }

    /**
     * This method is called when a user wants to send a message to a channel.
     *
     * @param messageCommand The {@link MessageCommand} object containing all
     *                       information needed for the messaging attempt
     * @return The {@link Broadcast} object generated by
     * {@link Broadcast#okay(Command, Collection)} if the message
     * attempt is successful. The recipients should be all clients
     * in the channel.
     * <p>
     * If an error occurs, use
     * {@link Broadcast#error(Command, ServerResponse)} with either:
     * (1) {@link ServerResponse#NO_SUCH_CHANNEL} if there is no
     * channel with the specified name
     * (2) {@link ServerResponse#USER_NOT_IN_CHANNEL} if the sender is
     * not in the channel they are trying to send the message to
     */
    public Broadcast sendMessage(MessageCommand messageCommand) {
        String certainChannel = messageCommand.getChannel();
        Channel x = null;
        TreeSet<String> notifyUser = new TreeSet<>();

        for (Map.Entry<String, Channel> entry : currentChannels.entrySet()) {
            if (certainChannel.equals(entry.getValue().getName())) {
                x = entry.getValue();
            }
        }

        if (x == null) {
            return Broadcast.error(messageCommand, ServerResponse.NO_SUCH_CHANNEL);
        }
        if (!x.getUsers().containsKey(messageCommand.getSenderId())) {
            return Broadcast.error(messageCommand, ServerResponse.USER_NOT_IN_CHANNEL);
        }

        for (Map.Entry<Integer, String> entry : x.getUsers().entrySet()) {
            notifyUser.add(entry.getValue());
        }
        return Broadcast.okay(messageCommand, notifyUser);
    }

    /**
     * This method is called when a user wants to leave a channel.
     * 
     * @param leaveCommand The {@link LeaveCommand} object containing all
     *                     information about the user's leave attempt
     * @return The {@link Broadcast} object generated by
     *         {@link Broadcast#okay(Command, Collection)} if the user leaves
     *         the channel successfully. The recipients should be all clients
     *         who were in the channel, including the user who left.
     * 
     *         If an error occurs, use
     *         {@link Broadcast#error(Command, ServerResponse)} with either:
     *         (1) {@link ServerResponse#NO_SUCH_CHANNEL} if there is no
     *         channel with the specified name
     *         (2) {@link ServerResponse#USER_NOT_IN_CHANNEL} if the sender is
     *         not in the channel they are trying to leave
     */
    public Broadcast leaveChannel(LeaveCommand leaveCommand) {
        String certainChannel = leaveCommand.getChannel();
        Channel x = null;
        TreeSet<String> notifyUser = new TreeSet<>();

        for (Map.Entry<String, Channel> entry : currentChannels.entrySet()) {
            if (certainChannel.equals(entry.getValue().getName())) {
                x = entry.getValue();
            }
        }

        if (x == null) {
            return Broadcast.error(leaveCommand, ServerResponse.NO_SUCH_CHANNEL);
        }
        if (!x.getUsers().containsKey(leaveCommand.getSenderId())) {
            return Broadcast.error(leaveCommand, ServerResponse.USER_NOT_IN_CHANNEL);
        }

        for (Map.Entry<Integer, String> entry : x.getUsers().entrySet()) {
            notifyUser.add(entry.getValue());
        }

        if (x.getOwner().equals(leaveCommand.getSender())) {
            currentChannels.remove(certainChannel);
        } else {
            x.deleteUser(leaveCommand.getSenderId());
        }
        return Broadcast.okay(leaveCommand, notifyUser);
    }

    /**
     * This method is called when a channel's owner adds a user to that channel.
     * 
     * @param inviteCommand The {@link InviteCommand} object containing all
     *                      information needed for the invite attempt
     * @return The {@link Broadcast} object generated by
     *         {@link Broadcast#names(Command, Collection, String)} if the user
     *         joins the channel successfully as a result of the invite.
     *         The recipients should be all people in the joined channel
     *         (including the new user).
     *
     *         If an error occurs, use
     *         {@link Broadcast#error(Command, ServerResponse)} with either:
     *         (1) {@link ServerResponse#NO_SUCH_USER} if the invited user
     *         does not exist
     *         (2) {@link ServerResponse#NO_SUCH_CHANNEL} if there is no channel
     *         with the specified name
     *         (3) {@link ServerResponse#INVITE_TO_PUBLIC_CHANNEL} if the
     *         invite refers to a public channel
     *         (4) {@link ServerResponse#USER_NOT_OWNER} if the sender is not
     *         the owner of the channel
     */
    public Broadcast inviteUser(InviteCommand inviteCommand) {
        String invite = inviteCommand.getUserToInvite();
        Channel certainChannel = null;
        TreeSet<String> notifyUser = new TreeSet<>();

        if (!currentUsers.containsValue(invite)) {
            return Broadcast.error(inviteCommand, ServerResponse.NO_SUCH_USER);
        }

        for (Map.Entry<String, Channel> entry : currentChannels.entrySet()) {
            if (entry.getValue().getName().equals(inviteCommand.getChannel())) {
                certainChannel = entry.getValue();
            }
        }
        System.out.println(certainChannel.isPrivate());
        if (certainChannel == null) {
            return Broadcast.error(inviteCommand, ServerResponse.NO_SUCH_CHANNEL);
        }
        if (!certainChannel.isPrivate()) {
            System.out.println("public");
            return Broadcast.error(inviteCommand, ServerResponse.INVITE_TO_PUBLIC_CHANNEL);
        }
        if (!certainChannel.getOwner().equals(inviteCommand.getSender())) {
            return Broadcast.error(inviteCommand, ServerResponse.USER_NOT_OWNER);
        }

        certainChannel.addUser(getUserId(invite), invite);

        for (Map.Entry<Integer, String> entry : certainChannel.getUsers().entrySet()) {
            notifyUser.add(entry.getValue());
        }
        return Broadcast.names(inviteCommand, notifyUser, inviteCommand.getSender());
    }

    /**
     * This method is called when a channel's owner removes a user from
     * that channel.
     * 
     * @param kickCommand The {@link KickCommand} object containing all
     *                    information needed for the kick attempt
     * @return The {@link Broadcast} object generated by
     *         {@link Broadcast#okay(Command, Collection)} if the user is
     *         successfully kicked from the channel. The recipients should be
     *         all clients who were in the channel, including the user
     *         who was kicked.
     *
     *         If an error occurs, use
     *         {@link Broadcast#error(Command, ServerResponse)} with either:
     *         (1) {@link ServerResponse#NO_SUCH_USER} if the user being kicked
     *         does not exist
     *         (2) {@link ServerResponse#NO_SUCH_CHANNEL} if there is no channel
     *         with the specified name
     *         (3) {@link ServerResponse#USER_NOT_IN_CHANNEL} if the
     *         user being kicked is not a member of the channel
     *         (4) {@link ServerResponse#USER_NOT_OWNER} if the sender is not
     *         the owner of the channel
     */
    public Broadcast kickUser(KickCommand kickCommand) {
        String kick = kickCommand.getUserToKick();
        Channel certainChannel = null;
        TreeSet<String> notifyUser = new TreeSet<>();

        if (!currentUsers.containsValue(kick)) {
            return Broadcast.error(kickCommand, ServerResponse.NO_SUCH_USER);
        }

        for (Map.Entry<String, Channel> entry : currentChannels.entrySet()) {
            if (entry.getValue().getName().equals(kickCommand.getChannel())) {
                certainChannel = entry.getValue();
                System.out.println(certainChannel.getName());
            }
        }

        if (certainChannel == null) {
            System.out.println("null");
            return Broadcast.error(kickCommand, ServerResponse.NO_SUCH_CHANNEL);
        }
        if (!certainChannel.getUsers().containsValue(kick)) {
            System.out.println("no user");
            return Broadcast.error(kickCommand, ServerResponse.USER_NOT_IN_CHANNEL);
        }
        if (!certainChannel.getOwner().equals(kickCommand.getSender())) {
            System.out.println("not owner");
            return Broadcast.error(kickCommand, ServerResponse.USER_NOT_OWNER);
        }

        System.out.println("will kick");

        for (Map.Entry<Integer, String> entry : certainChannel.getUsers().entrySet()) {
            notifyUser.add(entry.getValue());
        }

        if (kick.equals(certainChannel.getOwner())) {
            currentChannels.remove(certainChannel);
        } else {
            certainChannel.deleteUser(getUserId(kick));
        }
        return Broadcast.okay(kickCommand, notifyUser);
    }

}