package com.lumoo.Model;

public class LiveStream {
    public String streamId;
    public String owner;
    public String title;
    public boolean isLive;
    public int viewerCount;

    public LiveStream() {
        // Firebase için boş constructor
    }

    public LiveStream(String streamId, String owner, String title, boolean isLive, int viewerCount) {
        this.streamId = streamId;
        this.owner = owner;
        this.title = title;
        this.isLive = isLive;
        this.viewerCount = viewerCount;
    }
}

