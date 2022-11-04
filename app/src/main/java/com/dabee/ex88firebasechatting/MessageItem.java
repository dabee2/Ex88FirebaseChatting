package com.dabee.ex88firebasechatting;

public class MessageItem {
    public String nickName;
    public String message;
    public String profileUrl;
    public String time;

    public MessageItem() {
    }

    public MessageItem(String nickName, String message, String profileUrl, String time) {
        this.nickName = nickName;
        this.message = message;
        this.profileUrl = profileUrl;
        this.time = time;
    }
}
