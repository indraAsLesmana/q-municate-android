package com.quickblox.q_municate.ui.adapters.chats;


import android.content.Context;
import android.graphics.Bitmap;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.SimpleImageLoadingListener;
import com.quickblox.chat.model.QBAttachment;
import com.quickblox.chat.model.QBChatMessage;
import com.quickblox.content.model.QBFile;
import com.quickblox.q_municate.R;
import com.quickblox.q_municate.ui.adapters.base.BaseViewHolder;
import com.quickblox.q_municate.ui.views.maskedimageview.MaskedImageView;
import com.quickblox.q_municate.ui.views.roundedimageview.RoundedImageView;
import com.quickblox.q_municate.utils.DateUtils;
import com.quickblox.q_municate.utils.image.ImageLoaderUtils;
import com.quickblox.q_municate_core.models.AppSession;
import com.quickblox.q_municate_core.models.CombinationMessage;
import com.quickblox.q_municate_db.managers.DataManager;
import com.quickblox.q_municate_db.models.DialogNotification;
import com.quickblox.q_municate_db.models.State;
import com.quickblox.ui.kit.chatmessage.adapter.QBMessagesAdapter;
import com.quickblox.users.model.QBUser;
import com.timehop.stickyheadersrecyclerview.StickyRecyclerHeadersAdapter;

import java.util.Collection;
import java.util.List;

import butterknife.Bind;

public class PrivateChatMessageAdapter extends QBMessagesAdapter implements StickyRecyclerHeadersAdapter<RecyclerView.ViewHolder> {
    private static final String TAG = PrivateChatMessageAdapter.class.getSimpleName();
    protected static final int TYPE_REQUEST_MESSAGE = 5;
    private static int EMPTY_POSITION = -1;
    private int lastRequestPosition = EMPTY_POSITION;
    private int lastInfoRequestPosition = EMPTY_POSITION;

    private List<CombinationMessage> combinationMessagesList;
    protected QBUser currentUser;

    protected DataManager dataManager;

    public PrivateChatMessageAdapter(Context context, List<QBChatMessage> chatMessages, List<CombinationMessage> combinationMessagesList) {
        super(context, chatMessages);
        dataManager = DataManager.getInstance();
        currentUser = AppSession.getSession().getUser();
        this.combinationMessagesList = combinationMessagesList;
    }

    private CombinationMessage getCombinationMessage(int position) {
        return combinationMessagesList.get(position);
    }

    @Override
    protected void onBindViewCustomHolder(QBMessageViewHolder holder, QBChatMessage chatMessage, int position) {
        Log.d(TAG, "onBindViewCustomHolderr");

        CombinationMessage combinationMessage = getCombinationMessage(position);
        boolean friendsRequestMessage = DialogNotification.Type.FRIENDS_REQUEST.equals(
                combinationMessage.getNotificationType());
        boolean friendsInfoRequestMessage = combinationMessage
                .getNotificationType() != null && !friendsRequestMessage;

        if (friendsRequestMessage) {
            TextView textView = (TextView) holder.itemView.findViewById(R.id.message_textview);
            TextView timeTextMessageTextView = (TextView) holder.itemView.findViewById(R.id.time_text_message_textview);
            textView.setText(combinationMessage.getBody());
            timeTextMessageTextView.setText(DateUtils.formatDateSimpleTime(combinationMessage.getCreatedDate()));

        }
    }

    @Override
    protected void onBindViewMsgRightHolder(TextMessageHolder holder, QBChatMessage chatMessage, int position) {
        boolean ownMessage;
        ownMessage = !isIncoming(chatMessage);
        ImageView view = (ImageView) holder.itemView.findViewById(R.id.custom_text_view);

        CombinationMessage message = getCombinationMessage(position);

        if (ownMessage && message != null && message.getState() != null) {
            setMessageStatus(view, State.DELIVERED.equals(
                    message.getState()), State.READ.equals(message.getState()));
        } else if (ownMessage && message != null && message.getState() == null) {
            view.setImageResource(android.R.color.transparent);
        }

        super.onBindViewMsgRightHolder(holder, chatMessage, position);
    }

    @Override
    public long getHeaderId(int position) {
        QBChatMessage chatMessages = getItem(position);
        return DateUtils.toShortDateLong(chatMessages.getDateSent());
    }

    @Override
    public RecyclerView.ViewHolder onCreateHeaderViewHolder(ViewGroup parent) {
        View view = inflater.inflate(R.layout.item_chat_sticky_header_date, parent, false);
        return new RecyclerView.ViewHolder(view) {
        };
    }

    @Override
    public void onBindHeaderViewHolder(RecyclerView.ViewHolder holder, int position) {
        View view = holder.itemView;

        TextView headerTextView = (TextView) view.findViewById(R.id.header_date_textview);
        QBChatMessage chatMessages = getItem(position);
        headerTextView.setText(DateUtils.toTodayYesterdayFullMonthDate(chatMessages.getDateSent()));
    }

    @Override
    public void displayAttachment(QBMessageViewHolder holder, int position) {
        QBChatMessage chatMessage = getItem(position);
        Collection<QBAttachment> attachments = chatMessage.getAttachments();
        QBAttachment attachment = attachments.iterator().next();
        String privateUrl = QBFile.getPrivateUrlForUID(attachment.getId());

        ImageLoader.getInstance().displayImage(privateUrl, ((ImageAttachHolder) holder).attachImageView,
                ImageLoaderUtils.UIL_DEFAULT_DISPLAY_OPTIONS, new ImageListener((ImageAttachHolder) holder),
                null);
    }

    @Override
    protected String getDate(long milliseconds) {
        return DateUtils.formatDateSimpleTime(milliseconds / 1000);
    }

    @Override
    public int getItemViewType(int position) {
        CombinationMessage combinationMessage = getCombinationMessage(position);
        if (combinationMessage.getNotificationType() != null) {
            Log.d(TAG, "combinationMessage.getNotificationType()" + combinationMessage.getNotificationType());
            return TYPE_REQUEST_MESSAGE;
        }
        return super.getItemViewType(position);
    }

    @Override
    protected QBMessageViewHolder onCreateCustomViewHolder(ViewGroup parent, int viewType) {
        Log.d(TAG, "onCreateCustomViewHolder viewType= " + viewType);
        return viewType == TYPE_REQUEST_MESSAGE ? new FriendsViewHolder(inflater.inflate(R.layout.item_friends_notification_message, parent, false)) : null;
    }

    public void updateList(List<QBChatMessage> newData) {
        chatMessages = newData;
        notifyDataSetChanged();
    }

    public void findLastFriendsRequestMessagesPosition() {
        new FindLastFriendsRequestThread().run();
    }

    private void findLastFriendsRequest() {
        for (int i = 0; i < combinationMessagesList.size(); i++) {
            findLastFriendsRequest(i, combinationMessagesList.get(i));
        }
    }

    private void findLastFriendsRequest(int position, CombinationMessage combinationMessage) {
        boolean ownMessage;
        boolean friendsRequestMessage;
        boolean isFriend;

        if (combinationMessage.getNotificationType() != null) {
            ownMessage = !combinationMessage.isIncoming(currentUser.getId());
            friendsRequestMessage = DialogNotification.Type.FRIENDS_REQUEST.equals(
                    combinationMessage.getNotificationType());

            if (friendsRequestMessage && !ownMessage) {
                isFriend = dataManager.getFriendDataManager().
                        getByUserId(combinationMessage.getDialogOccupant().getUser().getUserId()) != null;
                if (!isFriend) {
                    lastRequestPosition = position;
                }
            }
        }
    }

    public void setList(List<CombinationMessage> combinationMessagesList) {
        this.combinationMessagesList = combinationMessagesList;
    }

    private class FindLastFriendsRequestThread extends Thread {

        @Override
        public void run() {
            findLastFriendsRequest();
        }
    }

    private class ImageListener extends SimpleImageLoadingListener {
        private ImageAttachHolder holder;

        public ImageListener(ImageAttachHolder holder) {
            this.holder = holder;
        }

        @Override
        public void onLoadingComplete(String imageUri, View view, final Bitmap loadedBitmap) {
            holder.attachImageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            holder.attachmentProgressBar.setVisibility(View.GONE);
        }
    }

    protected void setMessageStatus(ImageView imageView, boolean messageDelivered, boolean messageRead) {
        imageView.setImageResource(getMessageStatusIconId(messageDelivered, messageRead));
    }

    protected int getMessageStatusIconId(boolean isDelivered, boolean isRead) {
        int iconResourceId = 0;

        if (isRead) {
            iconResourceId = R.drawable.ic_status_mes_sent_received;
        } else if (isDelivered) {
            iconResourceId = R.drawable.ic_status_mes_sent;
        }

        return iconResourceId;
    }

    protected static class FriendsViewHolder extends QBMessageViewHolder {
        @Nullable
        @Bind(R.id.message_textview)
        TextView messageTextView;

        @Nullable
        @Bind(R.id.time_text_message_textview)
        TextView timeTextMessageTextView;

        @Nullable
        @Bind(R.id.time_attach_message_textview)
        TextView timeAttachMessageTextView;

        @Nullable
        @Bind(R.id.accept_friend_imagebutton)
        ImageView acceptFriendImageView;

        @Nullable
        @Bind(R.id.divider_view)
        View dividerView;

        @Nullable
        @Bind(R.id.reject_friend_imagebutton)
        ImageView rejectFriendImageView;


        public FriendsViewHolder(View view) {
            super(view);
        }
    }
}