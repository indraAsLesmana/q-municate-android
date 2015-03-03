package com.quickblox.q_municate_db.helpers;

import android.util.Log;

import com.quickblox.q_municate_db.managers.DatabaseManager;
import com.quickblox.q_municate_db.models.AttachmentType;
import com.quickblox.q_municate_db.models.DialogType;
import com.quickblox.q_municate_db.models.Notification;
import com.quickblox.q_municate_db.models.Role;
import com.quickblox.q_municate_db.models.SocialType;
import com.quickblox.q_municate_db.models.State;
import com.quickblox.q_municate_db.models.Status;

public class TablesInitHelper {

    public static void init() {
        Log.d("InitTablesHelper", "--------------------- init DB ---------------------");
        saveRoles();
        saveSocialTypes();
        saveStatus();
        saveDialogTypes();
        saveNotifications();
        saveAttachmentTypes();
        saveStates();
    }

    private static void saveRoles() {
        Role ownerRole = new Role(Role.Type.OWNER);
        Role simpleRole = new Role(Role.Type.SIMPLE_ROLE);
        DatabaseManager.getInstance().getRoleManager().createOrUpdate(ownerRole);
        DatabaseManager.getInstance().getRoleManager().createOrUpdate(simpleRole);
    }

    private static void saveSocialTypes() {
        SocialType facebookSocialType = new SocialType(SocialType.Type.FACEBOOK);
        SocialType twitterSocialType = new SocialType(SocialType.Type.TWITTER);
        DatabaseManager.getInstance().getSocialTypeManager().createOrUpdate(facebookSocialType);
        DatabaseManager.getInstance().getSocialTypeManager().createOrUpdate(twitterSocialType);
    }

    private static void saveStatus() {
        Status incomingStatus = new Status(Status.Type.INCOMING);
        Status outgoingReject = new Status(Status.Type.OUTGOING);
        DatabaseManager.getInstance().getStatusManager().createOrUpdate(incomingStatus);
        DatabaseManager.getInstance().getStatusManager().createOrUpdate(outgoingReject);
    }

    private static void saveDialogTypes() {
        DialogType privateDialogType = new DialogType(DialogType.Type.PRIVATE);
        DialogType groupDialogType = new DialogType(DialogType.Type.GROUP);
        DatabaseManager.getInstance().getDialogTypeManager().createOrUpdate(privateDialogType);
        DatabaseManager.getInstance().getDialogTypeManager().createOrUpdate(groupDialogType);
    }

    private static void saveNotifications() {
        Notification createPrivateChatNotification = new Notification(Notification.Type.CREATE_PRIVATE_CHAT);
        Notification createGroupChatNotification = new Notification(Notification.Type.CREATE_GROUP_CHAT);
        Notification changeNameNotification = new Notification(Notification.Type.CHANGE_NAME_GROUP_CHAT);
        Notification changeOccupantsNotification = new Notification(
                Notification.Type.CHANGE_OCCUPANTS_GROUP_CHAT);
        Notification changePhotoNotification = new Notification(Notification.Type.CHANGE_PHOTO_GROUP_CHAT);
        DatabaseManager.getInstance().getNotificationManager().createOrUpdate(createPrivateChatNotification);
        DatabaseManager.getInstance().getNotificationManager().createOrUpdate(createGroupChatNotification);
        DatabaseManager.getInstance().getNotificationManager().createOrUpdate(changeNameNotification);
        DatabaseManager.getInstance().getNotificationManager().createOrUpdate(changeOccupantsNotification);
        DatabaseManager.getInstance().getNotificationManager().createOrUpdate(changePhotoNotification);
    }

    private static void saveAttachmentTypes() {
        AttachmentType audioAttachmentType = new AttachmentType(AttachmentType.Type.AUDIO);
        AttachmentType videoAttachmentType = new AttachmentType(AttachmentType.Type.VIDEO);
        AttachmentType pictureAttachmentType = new AttachmentType(AttachmentType.Type.PICTURE);
        AttachmentType docAttachmentType = new AttachmentType(AttachmentType.Type.DOC);
        AttachmentType otherAttachmentType = new AttachmentType(AttachmentType.Type.OTHER);
        DatabaseManager.getInstance().getAttachmentTypeManager().createOrUpdate(audioAttachmentType);
        DatabaseManager.getInstance().getAttachmentTypeManager().createOrUpdate(videoAttachmentType);
        DatabaseManager.getInstance().getAttachmentTypeManager().createOrUpdate(pictureAttachmentType);
        DatabaseManager.getInstance().getAttachmentTypeManager().createOrUpdate(docAttachmentType);
        DatabaseManager.getInstance().getAttachmentTypeManager().createOrUpdate(otherAttachmentType);
    }

    private static void saveStates() {
        State deliveredState = new State(State.Type.DELIVERED);
        State readState = new State(State.Type.READ);
        State syncState = new State(State.Type.SYNC);
        DatabaseManager.getInstance().getStateManager().createOrUpdate(deliveredState);
        DatabaseManager.getInstance().getStateManager().createOrUpdate(readState);
        DatabaseManager.getInstance().getStateManager().createOrUpdate(syncState);
    }
}