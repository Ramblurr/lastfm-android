package com.htc.music;

interface IMediaPlaybackService
{
    String getSongInfo(int id);
    int playAlbum(int id);

    void openfile(String path);
    void openfileAsync(String path);
    void open(in int [] list, int position);
    int getQueuePosition();
    boolean isPlaying();
    void stop();
    void pause();
    void play();
    void prev();
    void next();
    long duration();
    long position();
    long seek(long pos);
    String getTrackName();
    String getAlbumName();
    int getAlbumId();
    String getArtistName();
    int getArtistId();
    void enqueue(in int [] list, int action);
    int [] getQueue();
    void moveQueueItem(int from, int to);
    void setQueuePosition(int index);
    String getPath();
    int getAudioId();
    void setShuffleMode(int shufflemode);
    int getShuffleMode();
    int removeTracks(int first, int last);
    int removeTrack(int id);
    void setRepeatMode(int repeatmode);
    int getRepeatMode();
    int getMediaMountedCount();

    void startAnimation();
    void endAnimation();
    void setAlbumQueue(in int [] list);
    int [] getAlbumQueue();
    int getAlbumQueuePosition();
    void activityGoSleep();
    void activityWakeup();
    int getQueueSize();
    int getAlbumQueueSize();
    void prevAlbum();
    void nextAlbum();
    boolean isSystemReady();
    void reloadQueue();
    void setMediaMode(boolean mode);
    void syncNowPlayingQueue(in int [] list);
}