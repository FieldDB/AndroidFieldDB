package ca.ilanguage.oprime.database;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ca.ilanguage.oprime.R;

/**
 * Helper class for providing sample content for user interfaces created by
 * Android template wizards.
 */
public class DataCollectionContent {

  /**
   * A dummy item representing a piece of content.
   */
  public static class DataCollectionItem {
    public String content;
    public String id;
    public int    layout_resource;

    public DataCollectionItem(String id, String content, int layout_resource) {
      this.id = id;
      this.content = content;
      this.layout_resource = layout_resource;
    }

    @Override
    public String toString() {
      return this.content;
    }
  }

  /**
   * A map of sample (dummy) items, by ID.
   */
  public static Map<String, DataCollectionItem> ITEM_MAP = new HashMap<String, DataCollectionItem>();

  /**
   * An array of sample (dummy) items.
   */
  public static List<DataCollectionItem>        ITEMS    = new ArrayList<DataCollectionItem>();

  static {
    // Add 3 sample items.
    addItem(new DataCollectionItem("Video", "Video Data", R.layout.fragment_video_recorder));
    addItem(new DataCollectionItem("Photo", "Photo Data", R.layout.fragment_take_picture));
    addItem(new DataCollectionItem("Audio", "Audio Data", R.layout.fragment_experiment_detail));
    addItem(new DataCollectionItem("Touch", "Touch Data", R.layout.fragment_experiment_detail));

    addItem(new DataCollectionItem("Storybook", "Storybook Experiment", R.layout.fragment_page_curl));
    addItem(new DataCollectionItem("HTML5", "HML5 Experiment", R.layout.fragment_html5webview));
    addItem(new DataCollectionItem("Gamified", "Gamified Experiment", R.layout.fragment_experiment_detail));
    addItem(new DataCollectionItem("Gravity", "Gravity Game", R.layout.fragment_experiment_detail));
    addItem(new DataCollectionItem("Sorting", "Sorting Experiment", R.layout.fragment_experiment_detail));
    addItem(new DataCollectionItem("Turns", "Turn taking Experiment", R.layout.fragment_experiment_detail));
    addItem(new DataCollectionItem("OneImage", "One image Experiment", R.layout.fragment_one_image));
    addItem(new DataCollectionItem("TwoImage", "Two image Experiment", R.layout.fragment_two_images));
    addItem(new DataCollectionItem("VideoEx", "Video Experiment", R.layout.fragment_video_and_image));
    addItem(new DataCollectionItem("Production", "Production Experiment", R.layout.fragment_experiment_detail));
    addItem(new DataCollectionItem("Drawing", "Drawing Experiment", R.layout.fragment_experiment_detail));
    addItem(new DataCollectionItem("StopWatch", "StopWatch Experiment", R.layout.fragment_stop_watch));
    addItem(new DataCollectionItem("Rating", "Rating Experiment", R.layout.fragment_experiment_detail));
  }

  private static void addItem(DataCollectionItem item) {
    ITEMS.add(item);
    ITEM_MAP.put(item.id, item);
  }
}
