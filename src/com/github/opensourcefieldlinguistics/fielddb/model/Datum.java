package com.github.opensourcefieldlinguistics.fielddb.model;

import java.util.ArrayList;
import java.util.Arrays;

import android.util.Log;

import com.github.opensourcefieldlinguistics.fielddb.lessons.Config;

public class Datum {
	protected String _id;
	protected String _rev;
	protected DatumField utterance;
	protected DatumField morphemes;
	protected DatumField gloss;
	protected DatumField translation;
	protected DatumField orthography;
	protected DatumField context;
	protected ArrayList<AudioVideo> imageFiles;
	protected ArrayList<AudioVideo> audioVideoFiles;
	protected int currentAudioVideoIndex = 0;
	protected int currentImageIndex = 0;
	protected ArrayList<String> locations;
	protected ArrayList<String> related;
	protected ArrayList<String> reminders;
	protected ArrayList<String> tags;
	protected ArrayList<String> validationStati;
	protected ArrayList<String> coments;
	protected String actualJSON;

	public Datum(String id, String rev, DatumField utterance,
			DatumField morphemes, DatumField gloss, DatumField translation,
			DatumField orthography, DatumField context,
			ArrayList<AudioVideo> imageFiles,
			ArrayList<AudioVideo> audioVideoFiles,
			ArrayList<AudioVideo> videoFiles, ArrayList<String> locations,
			ArrayList<String> related, ArrayList<String> reminders,
			ArrayList<String> tags, ArrayList<String> validationStati,
			ArrayList<String> coments, String actualJSON) {
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
		this.imageFiles = new ArrayList<Datum.AudioVideo>();
		this.audioVideoFiles = new ArrayList<Datum.AudioVideo>();
		this.locations = new ArrayList<String>();
		this.related = new ArrayList<String>();
		this.reminders = new ArrayList<String>();
		this.tags = new ArrayList<String>();
		this.validationStati = new ArrayList<String>();
		this.coments = new ArrayList<String>();
		this.actualJSON = "";
	}

	public Datum(String orthography, String morphemes, String gloss,
			String translation) {
		super();
		this._id = System.currentTimeMillis() + "";
		this.utterance = new DatumField("utterance", orthography);
		this.morphemes = new DatumField("morphemes", morphemes);
		this.gloss = new DatumField("gloss", gloss);
		this.translation = new DatumField("translation", translation);
		this.orthography = new DatumField("orthography", orthography);
		this.context = new DatumField("context", " ");
		this.imageFiles = new ArrayList<Datum.AudioVideo>();
		this.audioVideoFiles = new ArrayList<Datum.AudioVideo>();
		this.locations = new ArrayList<String>();
		this.related = new ArrayList<String>();
		this.reminders = new ArrayList<String>();
		this.tags = new ArrayList<String>();
		this.validationStati = new ArrayList<String>();
		this.coments = new ArrayList<String>();
		this.actualJSON = "";
	}

	public Datum(String orthography, String morphemes, String gloss,
			String translation, String context) {
		super();
		this._id = System.currentTimeMillis() + "";
		this.utterance = new DatumField("utterance", orthography);
		this.morphemes = new DatumField("morphemes", morphemes);
		this.gloss = new DatumField("gloss", gloss);
		this.translation = new DatumField("translation", translation);
		this.orthography = new DatumField("orthography", orthography);
		this.context = new DatumField("context", context);
		this.imageFiles = new ArrayList<Datum.AudioVideo>();
		this.audioVideoFiles = new ArrayList<Datum.AudioVideo>();
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
		this.imageFiles = new ArrayList<Datum.AudioVideo>();
		this.audioVideoFiles = new ArrayList<Datum.AudioVideo>();
		this.locations = new ArrayList<String>();
		this.related = new ArrayList<String>();
		this.reminders = new ArrayList<String>();
		this.tags = new ArrayList<String>();
		this.validationStati = new ArrayList<String>();
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

	public ArrayList<AudioVideo> getImageFiles() {
		return imageFiles;
	}

	public void setImageFiles(ArrayList<AudioVideo> imageFiles) {
		this.imageFiles = imageFiles;
	}

	public ArrayList<AudioVideo> getAudioVideoFiles() {
		return audioVideoFiles;
	}

	public void setAudioVideoFiles(ArrayList<AudioVideo> audioVideoFiles) {
		this.audioVideoFiles = audioVideoFiles;
	}

	public ArrayList<AudioVideo> getVideoFiles() {
		ArrayList<AudioVideo> videoFiles = new ArrayList<AudioVideo>();
		for (AudioVideo audioVideo : this.audioVideoFiles) {
			if (audioVideo.getFilename().endsWith(
					Config.DEFAULT_VIDEO_EXTENSION)) {
				videoFiles.add(audioVideo);
			}
		}
		return videoFiles;
	}

	public void setVideoFiles(ArrayList<AudioVideo> videoFiles)
			throws Exception {
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
			this.validationStati = new ArrayList<String>(
					Arrays.asList(validationStati.split(",")));
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
		this.imageFiles.add(new AudioVideo(filename));
	}

	public String getMainImageFile() {
		if (this.imageFiles == null || this.imageFiles.size() == 0) {
			return null;
		}
		return this.imageFiles.get(this.imageFiles.size() - 1).getFilename();
	}

	public void addAudioFile(String audioFileName) {
		this.audioVideoFiles.add(new AudioVideo(audioFileName));
	}

	public String getMainAudioVideoFile() {
		if (this.audioVideoFiles == null || this.audioVideoFiles.size() == 0) {
			return null;
		}
		return this.audioVideoFiles.get(this.audioVideoFiles.size() - 1)
				.getFilename();
	}

	public void addVideoFile(String videoFileName) {
		this.audioVideoFiles.add(new AudioVideo(videoFileName));
	}

	public String getMainVideoFile() {
		return this.getMainAudioVideoFile();
	}

	public String getPrevNextMediaFile(String type,
			ArrayList<AudioVideo> mediaFiles, String prevNext) {
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
		if (Config.D) {
			Log.d(Config.TAG, fileWillBe);
		}
		return fileWillBe;
	}

	public class AudioVideo {
		protected String filename;
		protected String description;
		protected String URL;

		public AudioVideo(String filename, String description, String uRL) {
			super();
			this.filename = filename;
			this.description = description;
			URL = uRL;
		}

		public AudioVideo(String filename) {
			super();
			this.filename = filename;
			this.description = "";
			this.URL = filename;
		}

		public String getFilename() {
			return this.filename;
		}

		public void setFilename(String filename) {
			this.filename = filename;
		}

		public String getDescription() {
			return description;
		}

		public void setDescription(String description) {
			this.description = description;
		}

		public String getURL() {
			return URL;
		}

		public void setURL(String uRL) {
			URL = uRL;
		}

	}

	public class DatumField {
		protected String label;
		protected String value;
		protected String mask;
		protected String encrypted;
		protected String shouldBeEncrypted;
		protected String help;
		protected String size;
		protected String showToUserTypes;
		protected String userchooseable;

		public DatumField(String label, String value, String mask,
				String encrypted, String shouldBeEncrypted, String help,
				String size, String showToUserTypes, String userchooseable) {
			super();
			this.label = label;
			this.value = value;
			this.mask = mask;
			this.encrypted = encrypted;
			this.shouldBeEncrypted = shouldBeEncrypted;
			this.help = help;
			this.size = size;
			this.showToUserTypes = showToUserTypes;
			this.userchooseable = userchooseable;
		}

		public DatumField(String label, String value) {
			super();
			this.label = label;
			this.value = value;
			this.mask = value;
			this.encrypted = "false";
			this.shouldBeEncrypted = "true";
			this.help = "Field created on an Android App";
			this.showToUserTypes = "all";
			this.userchooseable = "false";
		}

		public String getLabel() {
			return label;
		}

		public void setLabel(String label) {
			this.label = label;
		}

		public String getValue() {
			return value;
		}

		public void setValue(String value) {
			this.value = value;
		}

		public String getMask() {
			return mask;
		}

		public void setMask(String mask) {
			this.mask = mask;
		}

		public String getEncrypted() {
			return encrypted;
		}

		public void setEncrypted(String encrypted) {
			this.encrypted = encrypted;
		}

		public String getShouldBeEncrypted() {
			return shouldBeEncrypted;
		}

		public void setShouldBeEncrypted(String shouldBeEncrypted) {
			this.shouldBeEncrypted = shouldBeEncrypted;
		}

		public String getHelp() {
			return help;
		}

		public void setHelp(String help) {
			this.help = help;
		}

		public String getSize() {
			return size;
		}

		public void setSize(String size) {
			this.size = size;
		}

		public String getShowToUserTypes() {
			return showToUserTypes;
		}

		public void setShowToUserTypes(String showToUserTypes) {
			this.showToUserTypes = showToUserTypes;
		}

		public String getUserchooseable() {
			return userchooseable;
		}

		public void setUserchooseable(String userchooseable) {
			this.userchooseable = userchooseable;
		}

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
			filenameBasedOnMorphemesOrWhateverIsAvailable = this
					.getOrthography();
		}
		if (filenameBasedOnMorphemesOrWhateverIsAvailable == null
				|| "".equals(filenameBasedOnMorphemesOrWhateverIsAvailable)) {
			filenameBasedOnMorphemesOrWhateverIsAvailable = this
					.getTranslation();
		}
		if (filenameBasedOnMorphemesOrWhateverIsAvailable == null
				|| "".equals(filenameBasedOnMorphemesOrWhateverIsAvailable)) {
			filenameBasedOnMorphemesOrWhateverIsAvailable = "unknown";
		}
		filenameBasedOnMorphemesOrWhateverIsAvailable = Config
				.getSafeUri(filenameBasedOnMorphemesOrWhateverIsAvailable);
		/* If the string will be longer than 50 char, truncate it with ellipsis */
		if (filenameBasedOnMorphemesOrWhateverIsAvailable.length() >= 50) {
			filenameBasedOnMorphemesOrWhateverIsAvailable = filenameBasedOnMorphemesOrWhateverIsAvailable
					.substring(0, 49) + "___";
		}
		/* Add a unique human readable date, and a timestamp */
		String dateString = (String) android.text.format.DateFormat.format(
				"yyyy-MM-dd_kk.mm", new java.util.Date());
		dateString = dateString.replaceAll("/", "-");
		filenameBasedOnMorphemesOrWhateverIsAvailable = filenameBasedOnMorphemesOrWhateverIsAvailable
				+ "_" + dateString + "_" + System.currentTimeMillis();
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

	public String getMediaFilesAsCSV(ArrayList<AudioVideo> mediaFiles) {
		String asString = "";
		boolean isFirst = true;
		for (AudioVideo file : mediaFiles) {
			if (!isFirst) {
				asString = asString + ",";
			}
			isFirst = false;
			asString = asString + file.getFilename();
		}
		return asString;
	}

}
