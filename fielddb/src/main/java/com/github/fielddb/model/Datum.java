package com.github.fielddb.model;

import java.util.ArrayList;
import java.util.Arrays;

import com.github.fielddb.BuildConfig;
import com.github.fielddb.Config;
import com.github.fielddb.database.DatumContentProvider.DatumTable;

import android.database.Cursor;
import android.util.Log;

public class Datum {
  protected String _id;
  protected String _rev;
  protected DatumField utterance;
  protected DatumField morphemes;
  protected DatumField gloss;
  protected DatumField translation;
  protected DatumField orthography;
  protected DatumField context;
  protected ArrayList<FieldDBFile> imageFiles;
  protected ArrayList<FieldDBFile> audioVideoFiles;
  protected int currentAudioVideoIndex = 0;
  protected int currentImageIndex = 0;
  protected ArrayList<String> locations;
  protected ArrayList<String> related;
  protected ArrayList<String> reminders;
  protected ArrayList<String> tags;
  protected ArrayList<String> validationStati;
  protected ArrayList<String> coments;
  protected String actualJSON;

  public Datum(String id, String rev, DatumField utterance, DatumField morphemes, DatumField gloss,
      DatumField translation, DatumField orthography, DatumField context, ArrayList<FieldDBFile> imageFiles,
      ArrayList<FieldDBFile> audioVideoFiles, ArrayList<FieldDBFile> videoFiles, ArrayList<String> locations,
      ArrayList<String> related, ArrayList<String> reminders, ArrayList<String> tags,
      ArrayList<String> validationStati, ArrayList<String> coments, String actualJSON) {
    super();
    this._id = id;
    this._rev = rev;
    this.utterance = utterance;
    this.morphemes = morphemes;
    this.gloss = gloss;
    this.translation = translation;
    this.orthography = orthography;
    this.context = context;
    this.imageFiles = imageFiles;
    this.audioVideoFiles = audioVideoFiles;
    this.locations = locations;
    this.related = related;
    this.reminders = reminders;
    this.tags = tags;
    this.validationStati = validationStati;
    this.coments = coments;
    this.actualJSON = actualJSON;

    if (this.tags == null) {
      this.tags = new ArrayList<String>();
    }
  }

  public Datum(String orthography) {
    super();
    this._id = System.currentTimeMillis() + "";
    this.utterance = new DatumField("utterance", orthography);
    this.morphemes = new DatumField("morphemes", "");
    this.gloss = new DatumField("gloss", "");
    this.translation = new DatumField("translation", "");
    this.orthography = new DatumField("orthography", orthography);
    this.context = new DatumField("context", " ");
    this.imageFiles = new ArrayList<FieldDBFile>();
    this.audioVideoFiles = new ArrayList<FieldDBFile>();
    this.locations = new ArrayList<String>();
    this.related = new ArrayList<String>();
    this.reminders = new ArrayList<String>();
    this.tags = new ArrayList<String>();
    this.validationStati = new ArrayList<String>();
    this.coments = new ArrayList<String>();
    this.actualJSON = "";
  }

  public Datum(String orthography, String morphemes, String gloss, String translation) {
    super();
    this._id = System.currentTimeMillis() + "";
    this.utterance = new DatumField("utterance", orthography);
    this.morphemes = new DatumField("morphemes", morphemes);
    this.gloss = new DatumField("gloss", gloss);
    this.translation = new DatumField("translation", translation);
    this.orthography = new DatumField("orthography", orthography);
    this.context = new DatumField("context", " ");
    this.imageFiles = new ArrayList<FieldDBFile>();
    this.audioVideoFiles = new ArrayList<FieldDBFile>();
    this.locations = new ArrayList<String>();
    this.related = new ArrayList<String>();
    this.reminders = new ArrayList<String>();
    this.tags = new ArrayList<String>();
    this.validationStati = new ArrayList<String>();
    this.coments = new ArrayList<String>();
    this.actualJSON = "";
  }

  public Datum(String orthography, String morphemes, String gloss, String translation, String context) {
    super();
    this._id = System.currentTimeMillis() + "";
    this.utterance = new DatumField("utterance", orthography);
    this.morphemes = new DatumField("morphemes", morphemes);
    this.gloss = new DatumField("gloss", gloss);
    this.translation = new DatumField("translation", translation);
    this.orthography = new DatumField("orthography", orthography);
    this.context = new DatumField("context", context);
    this.imageFiles = new ArrayList<FieldDBFile>();
    this.audioVideoFiles = new ArrayList<FieldDBFile>();
    this.locations = new ArrayList<String>();
    this.related = new ArrayList<String>();
    this.reminders = new ArrayList<String>();
    this.tags = new ArrayList<String>();
    this.validationStati = new ArrayList<String>();
    this.coments = new ArrayList<String>();
    this.actualJSON = "";
  }

  public Datum() {
    super();
    this._id = System.currentTimeMillis() + "";
    this.utterance = new DatumField("utterance", "");
    this.morphemes = new DatumField("morphemes", "");
    this.gloss = new DatumField("gloss", "");
    this.translation = new DatumField("translation", "");
    this.orthography = new DatumField("orthography", "");
    this.context = new DatumField("context", " ");
    this.imageFiles = new ArrayList<FieldDBFile>();
    this.audioVideoFiles = new ArrayList<FieldDBFile>();
    this.locations = new ArrayList<String>();
    this.related = new ArrayList<String>();
    this.reminders = new ArrayList<String>();
    this.tags = new ArrayList<String>();
    this.validationStati = new ArrayList<String>();
    this.coments = new ArrayList<String>();
    this.actualJSON = "";
  }

  public Datum(Cursor cursor) {
    super();

    int currentColumnIndex = cursor.getColumnIndex(DatumTable.COLUMN_ID);
    if (currentColumnIndex > -1) {
      this._id = cursor.getString(currentColumnIndex);
    }

    currentColumnIndex = cursor.getColumnIndex(DatumTable.COLUMN_UTTERANCE);
    if (currentColumnIndex > -1) {
      this.utterance = new DatumField("utterance", cursor.getString(currentColumnIndex));
    }

    currentColumnIndex = cursor.getColumnIndex(DatumTable.COLUMN_MORPHEMES);
    if (currentColumnIndex > -1) {
      this.morphemes = new DatumField("morphemes", cursor.getString(currentColumnIndex));
    }

    currentColumnIndex = cursor.getColumnIndex(DatumTable.COLUMN_GLOSS);
    if (currentColumnIndex > -1) {
      this.gloss = new DatumField("gloss", cursor.getString(currentColumnIndex));
    }

    currentColumnIndex = cursor.getColumnIndex(DatumTable.COLUMN_TRANSLATION);
    if (currentColumnIndex > -1) {
      this.translation = new DatumField("translation", cursor.getString(currentColumnIndex));
    }

    currentColumnIndex = cursor.getColumnIndex(DatumTable.COLUMN_ORTHOGRAPHY);
    if (currentColumnIndex > -1) {
      this.orthography = new DatumField("orthography", cursor.getString(currentColumnIndex));
    }

    currentColumnIndex = cursor.getColumnIndex(DatumTable.COLUMN_CONTEXT);
    if (currentColumnIndex > -1) {
      this.context = new DatumField("context", cursor.getString(currentColumnIndex));
    }

    currentColumnIndex = cursor.getColumnIndex(DatumTable.COLUMN_CONTEXT);
    if (currentColumnIndex > -1) {
      this.context = new DatumField("context", cursor.getString(currentColumnIndex));
    }

    currentColumnIndex = cursor.getColumnIndex(DatumTable.COLUMN_IMAGE_FILES);
    if (currentColumnIndex > -1) {
      this.setImageFiles(cursor.getString(currentColumnIndex));
    }

    currentColumnIndex = cursor.getColumnIndex(DatumTable.COLUMN_AUDIO_VIDEO_FILES);
    if (currentColumnIndex > -1) {
      this.setAudioVideoFiles(cursor.getString(currentColumnIndex));
    }

    currentColumnIndex = cursor.getColumnIndex(DatumTable.COLUMN_VALIDATION_STATUS);
    if (currentColumnIndex > -1) {
      this.setValidationStatiFromSting(cursor.getString(currentColumnIndex));
    } else {
      this.validationStati = new ArrayList<String>();
    }

    currentColumnIndex = cursor.getColumnIndex(DatumTable.COLUMN_TAGS);
    if (currentColumnIndex > -1) {
      this.setTagsFromSting(cursor.getString(currentColumnIndex));
    } else {
      this.tags = new ArrayList<String>();
    }

    this.locations = new ArrayList<String>();
    this.related = new ArrayList<String>();
    this.reminders = new ArrayList<String>();
    this.coments = new ArrayList<String>();
    this.actualJSON = "";
  }

  public String getId() {
    return _id;
  }

  public void setId(String id) {
    this._id = id;
  }

  public String getRev() {
    return _rev;
  }

  public void setRev(String rev) {
    this._rev = rev;
  }

  public String getUtterance() {
    return utterance.getValue();
  }

  public void setUtterance(String utterance) {
    this.utterance.setValue(utterance);
  }

  public String getMorphemes() {
    return morphemes.getValue();
  }

  public void setMorphemes(String morphemes) {
    this.morphemes.setValue(morphemes);
  }

  public String getGloss() {
    return gloss.getValue();
  }

  public void setGloss(String gloss) {
    this.gloss.setValue(gloss);
  }

  public String getTranslation() {
    return translation.getValue();
  }

  public void setTranslation(String translation) {
    this.translation.setValue(translation);
  }

  public String getOrthography() {
    return orthography.getValue();
  }

  public void setOrthography(String orthography) {
    this.orthography.setValue(orthography);
  }

  public String getContext() {
    return this.context.getValue();
  }

  public void setContext(String context) {
    this.context.setValue(context);
  }

  public ArrayList<FieldDBFile> getImageFiles() {
    return imageFiles;
  }

  public void setImageFiles(String imageFilesString) {
    this.imageFiles = new ArrayList<FieldDBFile>();
    if (imageFilesString == null) {
      return;
    }

    String[] filenames = imageFilesString.split(",");
    for (int i = 0; i < filenames.length; i++) {
      this.addImageFile(filenames[i]);
    }
  }

  public void setImageFiles(ArrayList<FieldDBFile> imageFiles) {
    this.imageFiles = imageFiles;
  }

  public ArrayList<FieldDBFile> getAudioVideoFiles() {
    return audioVideoFiles;
  }

  public void setAudioVideoFiles(String filesString) {
    this.audioVideoFiles = new ArrayList<FieldDBFile>();
    if (filesString == null) {
      return;
    }

    String[] filenames = filesString.split(",");
    for (int i = 0; i < filenames.length; i++) {
      this.addAudioFile(filenames[i]);
    }
  }

  public void setAudioVideoFiles(ArrayList<FieldDBFile> audioVideoFiles) {
    this.audioVideoFiles = audioVideoFiles;
  }

  public ArrayList<FieldDBFile> getVideoFiles() {
    ArrayList<FieldDBFile> videoFiles = new ArrayList<FieldDBFile>();
    for (FieldDBFile audioVideo : this.audioVideoFiles) {
      if (audioVideo.getFilename().endsWith(Config.DEFAULT_VIDEO_EXTENSION)) {
        videoFiles.add(audioVideo);
      }
    }
    return videoFiles;
  }

  public void setVideoFiles(ArrayList<FieldDBFile> videoFiles) throws Exception {
    throw new Exception("Use setAudioVideoFiles instead");
  }

  public ArrayList<String> getLocations() {
    return locations;
  }

  public void setLocations(ArrayList<String> locations) {
    this.locations = locations;
  }

  public ArrayList<String> getRelated() {
    return related;
  }

  public void setRelated(ArrayList<String> Related) {
    this.related = Related;
  }

  public ArrayList<String> getReminders() {
    return reminders;
  }

  public void setReminders(ArrayList<String> reminders) {
    this.reminders = reminders;
  }

  public ArrayList<String> getTags() {
    return tags;
  }

  public String getTagsString() {
    String result = "";
    if (this.tags == null) {
      Log.d(Config.TAG, "The tags were null in getTagsString, this is not normal");
      return result;
    }
    for (String tag : this.tags) {
      if (!"".equals(result)) {
        result += ",";
      }
      result += tag;
    }
    return result;
  }

  public void setTagsFromSting(String tags) {
    if (tags != null && !"".equals(tags)) {
      this.tags = new ArrayList<String>(Arrays.asList(tags.split(",")));
    } else {
      this.tags = new ArrayList<String>();
    }
  }

  public void setTags(ArrayList<String> tags) {
    this.tags = tags;
  }

  public ArrayList<String> getValidationStati() {
    return validationStati;
  }

  public String getValidationStatiString() {
    String result = "";
    for (String validationStatus : this.validationStati) {
      if (!"".equals(result)) {
        result += ",";
      }
      result += validationStatus;
    }
    return result;
  }

  public void setValidationStatiFromSting(String validationStati) {
    if (validationStati != null && !"".equals(validationStati)) {
      this.validationStati = new ArrayList<String>(Arrays.asList(validationStati.split(",")));
    }
  }

  public void setValidationStati(ArrayList<String> validationStati) {
    this.validationStati = validationStati;
  }

  public ArrayList<String> getComents() {
    return coments;
  }

  public void setComents(ArrayList<String> coments) {
    this.coments = coments;
  }

  public String getActualJSON() {
    return actualJSON;
  }

  public void setActualJSON(String actualJSON) {
    this.actualJSON = actualJSON;
  }

  public void addImageFile(String filename) {
    this.imageFiles.add(new FieldDBFile(filename));
  }

  public String getMainImageFile() {
    if (this.imageFiles == null || this.imageFiles.size() == 0) {
      return null;
    }
    return this.imageFiles.get(this.imageFiles.size() - 1).getFilename();
  }

  public void addAudioFile(String audioFileName) {
    if (!this.audioVideoFiles.contains(audioFileName)) {
      this.audioVideoFiles.add(new FieldDBFile(audioFileName));
    }
  }

  public String getMainAudioVideoFile() {
    if (this.audioVideoFiles == null || this.audioVideoFiles.size() == 0) {
      return null;
    }
    return this.audioVideoFiles.get(this.audioVideoFiles.size() - 1).getFilename();
  }

  public void addVideoFile(String videoFileName) {
    this.audioVideoFiles.add(new FieldDBFile(videoFileName));
  }

  public String getMainVideoFile() {
    return this.getMainAudioVideoFile();
  }

  public String getPrevNextMediaFile(String type, ArrayList<FieldDBFile> mediaFiles, String prevNext) {
    if (mediaFiles == null || mediaFiles.size() == 0) {
      return null;
    }
    int index = 0;
    if ("audio".equals(type)) {
      index = this.currentAudioVideoIndex;
    }
    if ("image".equals(type)) {
      index = this.currentImageIndex;
    }
    if ("video".equals(type)) {
      index = this.currentAudioVideoIndex;
    }

    /* make it circular */
    if ("prev".equals(prevNext)) {
      if (index - 1 > 0) {
        index--;
      } else {
        index = mediaFiles.size() - 1;
      }
    }
    if ("next".equals(prevNext)) {
      if (mediaFiles.size() < index + 1) {
        index++;
      } else {
        index = 0;
      }
    }

    if ("audio".equals(type)) {
      this.currentAudioVideoIndex = index;
    }
    if ("image".equals(type)) {
      currentImageIndex = index;
    }
    if ("video".equals(type)) {
      this.currentAudioVideoIndex = index;
    }
    String fileWillBe = mediaFiles.get(index).getFilename();
    if (BuildConfig.DEBUG) {
      Log.d(Config.TAG, fileWillBe);
    }
    return fileWillBe;
  }

  public String getBaseFilename() {
    String filenameBasedOnMorphemesOrWhateverIsAvailable = "";
    filenameBasedOnMorphemesOrWhateverIsAvailable = this.getMorphemes();
    if (filenameBasedOnMorphemesOrWhateverIsAvailable == null
        || "".equals(filenameBasedOnMorphemesOrWhateverIsAvailable)) {
      filenameBasedOnMorphemesOrWhateverIsAvailable = this.getUtterance();
    }
    if (filenameBasedOnMorphemesOrWhateverIsAvailable == null
        || "".equals(filenameBasedOnMorphemesOrWhateverIsAvailable)) {
      filenameBasedOnMorphemesOrWhateverIsAvailable = this.getOrthography();
    }
    if (filenameBasedOnMorphemesOrWhateverIsAvailable == null
        || "".equals(filenameBasedOnMorphemesOrWhateverIsAvailable)) {
      filenameBasedOnMorphemesOrWhateverIsAvailable = this.getTranslation();
    }
    if (filenameBasedOnMorphemesOrWhateverIsAvailable == null
        || "".equals(filenameBasedOnMorphemesOrWhateverIsAvailable)) {
      filenameBasedOnMorphemesOrWhateverIsAvailable = "audio";
    }
    filenameBasedOnMorphemesOrWhateverIsAvailable = Config.getSafeUri(filenameBasedOnMorphemesOrWhateverIsAvailable);
    /* If the string will be longer than 50 char, truncate it with ellipsis */
    if (filenameBasedOnMorphemesOrWhateverIsAvailable.length() >= 50) {
      filenameBasedOnMorphemesOrWhateverIsAvailable = filenameBasedOnMorphemesOrWhateverIsAvailable.substring(0, 49)
          + "___";
    }
    /* Add a unique human readable date, and a timestamp */
    String dateString = (String) android.text.format.DateFormat.format("yyyy-MM-dd_kk.mm", new java.util.Date());
    dateString = dateString.replaceAll("/", "-");
    filenameBasedOnMorphemesOrWhateverIsAvailable = filenameBasedOnMorphemesOrWhateverIsAvailable + "_" + dateString
        + "_" + System.currentTimeMillis();
    return filenameBasedOnMorphemesOrWhateverIsAvailable;
  }

  public void addMediaFiles(String mediaFiles) {
    if (mediaFiles == null || "".equals(mediaFiles)) {
      return;
    }
    String[] files = mediaFiles.trim().split(",");
    for (String filename : files) {
      if (filename.trim().length() > 4) {
        filename = filename.trim();
        if (filename.endsWith("jpg")) {
          this.addImageFile(filename);
        } else if (filename.endsWith("png")) {
          this.addImageFile(filename);
        } else if (filename.endsWith("gif")) {
          this.addImageFile(filename);
        } else if (filename.endsWith("png")) {
          this.addAudioFile(filename);
        } else if (filename.endsWith("wav")) {
          this.addAudioFile(filename);
        } else if (filename.endsWith("ogg")) {
          this.addAudioFile(filename);
        } else if (filename.endsWith("amr")) {
          this.addAudioFile(filename);
        } else if (filename.endsWith("mp3")) {
          this.addAudioFile(filename);
        } else if (filename.endsWith("mtk")) {
          this.addAudioFile(filename);
        } else if (filename.endsWith("mov")) {
          this.addAudioFile(filename);
        } else if (filename.endsWith("avi")) {
          this.addAudioFile(filename);
        } else if (filename.endsWith("mp4")) {
          this.addVideoFile(filename);
        }
      }
    }
  }

  public String getMediaFilesAsCSV(ArrayList<FieldDBFile> mediaFiles) {
    String asString = "";
    boolean isFirst = true;
    for (FieldDBFile file : mediaFiles) {
      if (!isFirst) {
        asString = asString + ",";
      }
      isFirst = false;
      asString = asString + file.getFilename();
    }
    return asString;
  }

}
