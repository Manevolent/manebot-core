package io.manebot.user;

import io.manebot.chat.ChatMessage;
import io.manebot.chat.ChatMessageReceiver;
import io.manebot.chat.ChatSender;
import io.manebot.command.CommandSender;
import io.manebot.command.DefaultCommandSender;
import io.manebot.conversation.Conversation;
import io.manebot.entity.EntityType;
import io.manebot.platform.Platform;
import io.manebot.platform.PlatformUser;

import java.util.Collection;
import java.util.Date;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Users define individual manebot users.
 */
public interface User extends EntityType {

    /**
     * Gets this user's username on the system.
     *
     * @return Username.
     */
    String getUsername();

    /**
     * Gets this user's username on the system.  Equivalent to <b>getUsername()</b>.
     *
     * @return Username.
     */
    default String getName() {
        return getUsername();
    }

    /**
     * Gets this user's desired display name.
     *
     * @return Display name.
     */
    String getDisplayName();

    /**
     * Sets this user's desired display name.
     *
     * @param displayName Display name.
     */
    void setDisplayName(String displayName);

    /**
     * Gets the date this user was registered.
     *
     * @return Register date.
     */
    Date getRegisteredDate();

    /**
     * Gets the date this user was last seen.
     *
     * @return Last seen date.
     */
    Date getLastSeenDate();

    /**
     * Sets the last date this user was seen.
     *
     * @param date last seen date.
     */
    void setLastSeenDate(Date date);

    /**
     * Adds this user to a specific user group.
     * @param group UserGroup instance to add this user to.
     */
    default void addGroup(UserGroup group) {
        group.addUser(this);
    }

    /**
     * Removes this user from a specific user group.
     * @param group UserGroup instance to remove this user from.
     */
    default void removeGroup(UserGroup group) {
        group.removeUser(this);
    }

    /**
     * Finds the groups this user is a member of.
     * @return group collection.
     */
    default Collection<UserGroup> getGroups() {
        return getMembership().stream().map(UserGroupMembership::getGroup).collect(Collectors.toList());
    }

    /**
     * Gets a collection of individual memberships, which is more definite than getUsers(), providing more specific
     * information about membership.
     *
     * @return immutable collection of user group membership records.
     */
    Collection<UserGroupMembership> getMembership();

    /**
     * Get a list of connections that associate this user with platforms.
     * @return user associations.
     */
    Collection<UserAssociation> getAssociations();

    /**
     * Gets a list of all previous bans on record for this user.
     * @return immutable collection of user ban history.
     */
    Collection<UserBan> getBans();

    /**
     * Gets a list of all previous bans issued by this user.
     * @return immutable collection of user ban issuance history.
     */
    Collection<UserBan> getIssuedBans();

    /**
     * Gets the current ban on record for the user.
     * @return ban instance.
     */
    UserBan getBan();

    /**
     * Bans the user until the specified date
     * @param reason ban reason, may be null.
     * @param end date the ban should end.
     * @return UserBan instance.
     * @throws SecurityException if there was a security violation creating the ban.
     */
    UserBan ban(String reason, Date end) throws SecurityException;

    /**
     * Bans the user until the specified date
     * @param reason ban reason, may be null.
     * @return UserBan instance.
     * @throws SecurityException if there was a security violation creating the ban.
     */
    UserBan ban(String reason) throws SecurityException;

    /**
     * Gets a user association by platform and ID.
     * @param platform Platform to search for
     * @param id ID to search for.
     * @return UserAssociation instance if found, null otherwise.
     */
    default UserAssociation getUserAssociation(Platform platform, String id) {
        return getAssociations().stream()
                .filter(association -> association.getPlatform().equals(platform) && association.getPlatformId().equals(id))
                .findFirst()
                .orElse(null);
    }

    /**
     * Gets the list of connected IDs for this user.
     * @param platform Platform association to search on.
     * @return IDs associated with the given platform for this user.
     */
    default Collection<UserAssociation> getAssociations(Platform platform) {
        return getAssociations().stream()
                .filter(association -> association.getPlatform().equals(platform))
                .collect(Collectors.toList());
    }

    /**
     * Creates a user connection for this user.
     * @param platform Platform to create the association on.
     * @param id Platform-specific ID to associate the connection to.
     * @return UserAssociation instance.
     */
    UserAssociation createAssociation(Platform platform, String id);

    /**
     * Removes a user association from this user.
     * @param association Association to remove.
     * @return true if the association was removed, false otherwise.
     */
    default boolean removeAssociation(UserAssociation association) {
        if (association.getUser() != this) throw new IllegalArgumentException("user mismatch");
        return removeAssociation(association.getPlatform(), association.getPlatformId());
    }

    /**
     * Removes a user association from this user.
     * @param platform Platform to remove an association for.
     * @param id Platform-specific id to remove an association for.
     * @return true if the association was removed, false otherwise.
     */
    boolean removeAssociation(Platform platform, String id);

    /**
     * Creates a command sender for the specified user.
     *
     * @param conversation Conversation to create a command sender for.
     * @param platformUser platform specific user instance to associate the new command sender with.
     * @return CommandSender instance.
     * @throws SecurityException
     */
    default CommandSender createSender(Conversation conversation, PlatformUser platformUser)
            throws SecurityException {
        return new DefaultCommandSender(conversation, platformUser, this);
    }

    /**
     * Gets the user's system type.
     * @return UserType instance.
     */
    UserType getType();

    /**
     * Changes the user's system type.
     * @param type UserType instance to change to.
     * @return true if the type was changed.
     */
    boolean setType(UserType type);

    /**
     * Broadcasts the specified message receiver to the user.
     * @param sender ChatSender to broadcast, constructing messages for each broadcast endpoint for the user.
     * @return collection of all broadcast messages.
     */
    Collection<ChatMessage> broadcastMessage(Function<ChatSender, Collection<ChatMessage>> sender);

    /**
     * Gets the current user prompt.
     * @return user prompt instance if one exists, null otherwise.
     */
    UserPrompt getPrompt();

    /**
     * Constructs and assigns a user prompt for this user. UserPrompts are completed on the thread of the user after
     * they have been manually confirmed by the target user.
     *
     * @param consumer Consumer of a UserPrompt.Builder instance.
     * @return constructed UserPrompt instance.
     * @throws IllegalStateException if there is a state issue constructing the prompt.
     */
    UserPrompt prompt(Consumer<UserPrompt.Builder> consumer) throws IllegalStateException;

}
