package net.roarsoftware.lastfm;

import java.util.Collection;
import java.util.LinkedHashMap;

/**
 * Provides nothing more than a namespace for the API methods starting with group.
 *
 * @author Janni Kovacs
 */
public class Group {

	private Group() {
	}

	public static Chart<Album> getWeeklyAlbumChart(String group, String apiKey) {
		return getWeeklyAlbumChart(group, null, null, -1, apiKey);
	}

	public static Chart<Album> getWeeklyAlbumChart(String group, int limit, String apiKey) {
		return getWeeklyAlbumChart(group, null, null, limit, apiKey);
	}

	public static Chart<Album> getWeeklyAlbumChart(String group, String from, String to, int limit, String apiKey) {
		return Chart.getChart("group.getWeeklyAlbumChart", "group", group, "album", from, to, limit, apiKey);
	}

	public static Chart<Artist> getWeeklyArtistChart(String group, String apiKey) {
		return getWeeklyArtistChart(group, null, null, -1, apiKey);
	}

	public static Chart<Artist> getWeeklyArtistChart(String group, int limit, String apiKey) {
		return getWeeklyArtistChart(group, null, null, limit, apiKey);
	}

	public static Chart<Artist> getWeeklyArtistChart(String group, String from, String to, int limit, String apiKey) {
		return Chart.getChart("group.getWeeklyArtistChart", "group", group, "artist", from, to, limit, apiKey);
	}

	public static Chart<Track> getWeeklyTrackChart(String group, String apiKey) {
		return getWeeklyTrackChart(group, null, null, -1, apiKey);
	}

	public static Chart<Track> getWeeklyTrackChart(String group, int limit, String apiKey) {
		return getWeeklyTrackChart(group, null, null, limit, apiKey);
	}

	public static Chart<Track> getWeeklyTrackChart(String group, String from, String to, int limit, String apiKey) {
		return Chart.getChart("group.getWeeklyTrackChart", "group", group, "track", from, to, limit, apiKey);
	}

	public static LinkedHashMap<String, String> getWeeklyChartList(String group, String apiKey) {
		return Chart.getWeeklyChartList("group", group, apiKey);
	}

	public static Collection<Chart> getWeeklyChartListAsCharts(String group, String apiKey) {
		return Chart.getWeeklyChartListAsCharts("group", group, apiKey);
	}
}
