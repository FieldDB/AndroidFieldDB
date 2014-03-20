package com.github.opensourcefieldlinguistics.fielddb.content;

import java.util.ArrayList;

public class Datum {
	protected String id;
	protected String rev;
	protected DatumField utterance;
	protected DatumField morphmemes;
	protected DatumField gloss;
	protected DatumField translation;
	protected DatumField orthography;
	protected ArrayList<AudioVideo> imageFiles;
	protected ArrayList<AudioVideo> audioFiles;
	protected ArrayList<AudioVideo> videoFiles;
	protected ArrayList<String> locations;
	protected ArrayList<String> similar;
	protected ArrayList<String> reminders;
	protected ArrayList<String> tags;
	protected ArrayList<String> coments;
	protected String actualJSON;

	public Datum(String id, String rev, DatumField utterance,
			DatumField morphmemes, DatumField gloss, DatumField translation,
			DatumField orthography, ArrayList<AudioVideo> imageFiles,
			ArrayList<AudioVideo> audioFiles, ArrayList<AudioVideo> videoFiles,
			ArrayList<String> locations, ArrayList<String> similar,
			ArrayList<String> reminders, ArrayList<String> tags,
			ArrayList<String> coments, String actualJSON) {
		super();
		this.id = id;
		this.rev = rev;
		this.utterance = utterance;
		this.morphmemes = morphmemes;
		this.gloss = gloss;
		this.translation = translation;
		this.orthography = orthography;
		this.imageFiles = imageFiles;
		this.audioFiles = audioFiles;
		this.videoFiles = videoFiles;
		this.locations = locations;
		this.similar = similar;
		this.reminders = reminders;
		this.tags = tags;
		this.coments = coments;
		this.actualJSON = actualJSON;
	}

	public Datum() {
		super();
		this.id = System.currentTimeMillis() + "";
		this.utterance = new DatumField("utterance", "");
		this.morphmemes = new DatumField("morphmemes", "");
		this.gloss = new DatumField("gloss", "");
		this.translation = new DatumField("translation", "");
		this.orthography = new DatumField("orthography", "");
		this.imageFiles = new ArrayList<Datum.AudioVideo>();
		this.audioFiles = new ArrayList<Datum.AudioVideo>();
		this.videoFiles = new ArrayList<Datum.AudioVideo>();
		this.locations = new ArrayList<String>();
		this.similar = new ArrayList<String>();
		this.reminders = new ArrayList<String>();
		this.tags = new ArrayList<String>();
		this.coments = new ArrayList<String>();
		this.actualJSON = "";
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getRev() {
		return rev;
	}

	public void setRev(String rev) {
		this.rev = rev;
	}

	public String getUtterance() {
		return utterance.getValue();
	}

	public void setUtterance(String utterance) {
		this.utterance.setValue(utterance);
	}

	public String getMorphmemes() {
		return morphmemes.getValue();
	}

	public void setMorphmemes(String morphmemes) {
		this.morphmemes.setValue(morphmemes);
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

	public ArrayList<AudioVideo> getImageFiles() {
		return imageFiles;
	}

	public void setImageFiles(ArrayList<AudioVideo> imageFiles) {
		this.imageFiles = imageFiles;
	}

	public ArrayList<AudioVideo> getAudioFiles() {
		return audioFiles;
	}

	public void setAudioFiles(ArrayList<AudioVideo> audioFiles) {
		this.audioFiles = audioFiles;
	}

	public ArrayList<AudioVideo> getVideoFiles() {
		return videoFiles;
	}

	public void setVideoFiles(ArrayList<AudioVideo> videoFiles) {
		this.videoFiles = videoFiles;
	}

	public ArrayList<String> getLocations() {
		return locations;
	}

	public void setLocations(ArrayList<String> locations) {
		this.locations = locations;
	}

	public ArrayList<String> getSimilar() {
		return similar;
	}

	public void setSimilar(ArrayList<String> similar) {
		this.similar = similar;
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

	public void setTags(ArrayList<String> tags) {
		this.tags = tags;
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

		public AudioVideo(String filenameL) {
			super();
			this.filename = filename;
			this.description = "";
			URL = filenameL;
		}

		public String getFilename() {
			return filename;
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
}
