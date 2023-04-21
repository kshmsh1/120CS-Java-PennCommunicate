package org.cis1200;

import java.util.*;

public final class Channel {
    private String name;
    private String owner;
    private int ownerId;
    private boolean inviteOnly;
    private TreeMap<Integer, String> users;
    private boolean isPrivate;

    public Channel(String name, String owner, int ownerId, boolean inviteOnly) {
        this.name = name;
        this.owner = owner;
        this.ownerId = ownerId;
        this.inviteOnly = inviteOnly;
        users = new TreeMap<>();
        users.put(ownerId, owner);
    }

    public String getName() {
        return name;
    }

    public String getOwner() {
        return owner;
    }

    public int getOwnerId() {
        return ownerId;
    }

    public boolean isInviteOnly() {
        return inviteOnly;
    }

    public TreeMap<Integer, String> getUsers() {
        return users;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public void addUser(int userId, String nickname) {
        users.put(userId, nickname);
    }

    public void deleteUser(int userId) {
        users.remove(userId);
    }

    public boolean isPrivate() {
        return this.inviteOnly;
    }

}