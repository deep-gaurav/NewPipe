package org.schabi.newpipe.util;

import android.content.Context;
import android.content.Intent;
import android.preference.PreferenceManager;

import org.schabi.newpipe.MainActivity;
import org.schabi.newpipe.R;
import org.schabi.newpipe.extractor.NewPipe;
import org.schabi.newpipe.extractor.StreamingService;
import org.schabi.newpipe.extractor.stream_info.StreamInfo;
import org.schabi.newpipe.fragments.OnItemSelectedListener;
import org.schabi.newpipe.fragments.detail.VideoDetailFragment;
import org.schabi.newpipe.player.AbstractPlayer;

@SuppressWarnings({"unused", "WeakerAccess"})
public class NavigationHelper {

    public static Intent getOpenPlayerIntent(Context context, Class targetClazz, StreamInfo info, int selectedStreamIndex) {
        return new Intent(context, targetClazz)
                .putExtra(AbstractPlayer.VIDEO_TITLE, info.title)
                .putExtra(AbstractPlayer.VIDEO_URL, info.webpage_url)
                .putExtra(AbstractPlayer.CHANNEL_NAME, info.uploader)
                .putExtra(AbstractPlayer.INDEX_SEL_VIDEO_STREAM, selectedStreamIndex)
                .putExtra(AbstractPlayer.VIDEO_STREAMS_LIST, Utils.getSortedStreamVideosList(context, info.video_streams, info.video_only_streams, false))
                .putExtra(AbstractPlayer.VIDEO_ONLY_AUDIO_STREAM, Utils.getHighestQualityAudio(info.audio_streams));
    }

    public static Intent getOpenPlayerIntent(Context context, Class targetClazz, AbstractPlayer instance) {
        return new Intent(context, targetClazz)
                .putExtra(AbstractPlayer.VIDEO_TITLE, instance.getVideoTitle())
                .putExtra(AbstractPlayer.VIDEO_URL, instance.getVideoUrl())
                .putExtra(AbstractPlayer.CHANNEL_NAME, instance.getChannelName())
                .putExtra(AbstractPlayer.INDEX_SEL_VIDEO_STREAM, instance.getSelectedStreamIndex())
                .putExtra(AbstractPlayer.VIDEO_STREAMS_LIST, instance.getVideoStreamsList())
                .putExtra(AbstractPlayer.VIDEO_ONLY_AUDIO_STREAM, instance.getAudioStream())
                .putExtra(AbstractPlayer.START_POSITION, ((int) instance.getPlayer().getCurrentPosition()));
    }


    /*//////////////////////////////////////////////////////////////////////////
    // Through Interface (faster)
    //////////////////////////////////////////////////////////////////////////*/

    public static void openChannel(OnItemSelectedListener listener, int serviceId, String url) {
        openChannel(listener, serviceId, url, null);
    }

    public static void openChannel(OnItemSelectedListener listener, int serviceId, String url, String name) {
        listener.onItemSelected(StreamingService.LinkType.CHANNEL, serviceId, url, name);
    }

    public static void openVideoDetail(OnItemSelectedListener listener, int serviceId, String url) {
        openVideoDetail(listener, serviceId, url, null);
    }

    public static void openVideoDetail(OnItemSelectedListener listener, int serviceId, String url, String title) {
        listener.onItemSelected(StreamingService.LinkType.STREAM, serviceId, url, title);
    }

    /*//////////////////////////////////////////////////////////////////////////
    // Through Intents
    //////////////////////////////////////////////////////////////////////////*/

    public static void openByLink(Context context, String url) throws Exception {
        context.startActivity(getIntentByLink(context, url));
    }

    public static void openChannel(Context context, int serviceId, String url) {
        openChannel(context, serviceId, url, null);
    }

    public static void openChannel(Context context, int serviceId, String url, String name) {
        Intent openIntent = getOpenIntent(context, url, serviceId, StreamingService.LinkType.CHANNEL);
        if (name != null && !name.isEmpty()) openIntent.putExtra(Constants.KEY_TITLE, name);
        context.startActivity(openIntent);
    }

    public static void openVideoDetail(Context context, int serviceId, String url) {
        openVideoDetail(context, serviceId, url, null);
    }

    public static void openVideoDetail(Context context, int serviceId, String url, String title) {
        Intent openIntent = getOpenIntent(context, url, serviceId, StreamingService.LinkType.STREAM);
        if (title != null && !title.isEmpty()) openIntent.putExtra(Constants.KEY_TITLE, title);
        context.startActivity(openIntent);
    }

    public static void openMainActivity(Context context) {
        Intent mIntent = new Intent(context, MainActivity.class);
        context.startActivity(mIntent);
    }

    private static Intent getOpenIntent(Context context, String url, int serviceId, StreamingService.LinkType type) {
        Intent mIntent = new Intent(context, MainActivity.class);
        mIntent.putExtra(Constants.KEY_SERVICE_ID, serviceId);
        mIntent.putExtra(Constants.KEY_URL, url);
        mIntent.putExtra(Constants.KEY_LINK_TYPE, type);
        return mIntent;
    }

    private static Intent getIntentByLink(Context context, String url) throws Exception {
        StreamingService service = NewPipe.getServiceByUrl(url);
        if (service == null) throw new Exception("NewPipe.getServiceByUrl returned null for url > \"" + url + "\"");
        int serviceId = service.getServiceId();
        switch (service.getLinkTypeByUrl(url)) {
            case STREAM:
                Intent sIntent = getOpenIntent(context, url, serviceId, StreamingService.LinkType.STREAM);
                sIntent.putExtra(VideoDetailFragment.AUTO_PLAY, PreferenceManager.getDefaultSharedPreferences(context)
                        .getBoolean(context.getString(R.string.autoplay_through_intent_key), false));
                return sIntent;
            case CHANNEL:
                return getOpenIntent(context, url, serviceId, StreamingService.LinkType.CHANNEL);
            case NONE:
                throw new Exception("Url not known to service. service="
                        + Integer.toString(serviceId) + " url=" + url);
        }
        return null;
    }
}
