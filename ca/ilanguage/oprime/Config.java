package ca.ilanguage.oprime;

import java.util.regex.Pattern;

public class Config {

	/*
	 * Control Flow constants
	 */
	public static final int CODE_AUTO_ADVANCE_NEXT_SUB_EXPERIMENT = 59;
	public static final int CODE_EXPERIMENT_COMPLETED = 55;
	public static final int CODE_NOTSPECIFIED = 0;
	public static final int CODE_PICTURE_TAKEN = 60;
	public static final int CODE_PREPARE_TRIAL = 56;
	public static final int CODE_REPLAY_RESULTS = 58;
	public static final int CODE_SWITCH_LANGUAGE = 57;

	public static boolean D = true;
	
	public static String DEFAULT_AUDIO_EXTENSION = ".mp3";
	public static String DEFAULT_IMAGE_EXTENSION = ".png";
	public static final String DEFAULT_LANGUAGE = "en";
	public static final String DEFAULT_OUTPUT_DIRECTORY = "/sdcard/OPrime";
	public static final String DEFAULT_PARTICIPANT_ID = "0000";
	public static String DEFAULT_VIDEO_EXTENSION = ".mp4";

	public static final String EMPTYSTRING = "";
	public static final String ENGLISH = "en";

	public static final String EXTRA_DEBUG_MODE = "debug_mode";
	public static final String EXTRA_EXPERIMENT_TRIAL_INFORMATION = "experimenttrialinfo";
	public static final String EXTRA_HTML5_JAVASCRIPT_INTERFACE = "javascriptinterface";
	public static final String EXTRA_HTML5_SUB_EXPERIMENT_INITIAL_URL = "subexperimenturl";
	public static final String EXTRA_LANGUAGE = "language";
	public static final String EXTRA_MAX_PICTURE_SIZE = "maxpicturesize";
	public static final String EXTRA_OUTPUT_DIR = "outputdir";
	public static final String EXTRA_PARTICIPANT_ID = "participant";
	public static final String EXTRA_PLEASE_PREPARE_EXPERIMENT = "pleaseprepareexperiment";
	public static final String EXTRA_RECOGNITION_COMPLETED = "recognitioncompleted";
	public static final String EXTRA_RESULT_FILENAME = "resultfilename";
	public static final String EXTRA_STIMULI = "stimuli";
	public static final String EXTRA_STIMULI_IMAGE_ID = "stimuliimageid";
	public static final String EXTRA_SUB_EXPERIMENT = "subexperiment";
	public static final String EXTRA_SUB_EXPERIMENT_TITLE = "subexperimenttitle";
	public static final String EXTRA_TAG = "tag";
	public static final String EXTRA_TAKE_PICTURE_AT_END = "takepictureatend";
	public static final String EXTRA_TWO_PAGE_STORYBOOK = "twopagebook";
    public static final String EXTRA_USE_FRONT_FACING_CAMERA = "usefrontfacingcamera";

    public static final String FRENCH = "fr";

	/*
	 * Intents and Extras to call activities and services
	 */
	public static final String INTENT_FINISHED_SUB_EXPERIMENT = "ca.ilanguage.oprime.intent.action.FINISHED_SUB_EXPERIMENT";
	public static final String INTENT_SAVE_SUB_EXPERIMENT_JSON = "ca.ilanguage.oprime.intent.action.SAVE_SUB_EXPERIMENT_JSON";
	public static final String INTENT_START_AUDIO_RECORDING = "ca.ilanguage.oprime.intent.action.START_AUDIO_RECORDING_SERVICE";
	public static final String INTENT_START_HTML5_SUB_EXPERIMENT = "ca.ilanguage.oprime.intent.action.START_HTML5_SUB_EXPERIMENT";
	public static final String INTENT_START_STOP_WATCH_SUB_EXPERIMENT = "ca.ilanguage.oprime.intent.action.START_STOP_WATCH_SUB_EXPERIMENT";
	public static final String INTENT_START_STORY_BOOK_SUB_EXPERIMENT = "ca.ilanguage.oprime.intent.action.START_STORY_BOOK_SUB_EXPERIMENT";
	public static final String INTENT_START_SUB_EXPERIMENT = "ca.ilanguage.oprime.intent.action.START_SUB_EXPERIMENT";
	public static final String INTENT_START_TWO_IMAGE_SUB_EXPERIMENT = "ca.ilanguage.oprime.intent.action.START_TWO_IMAGE_SUB_EXPERIMENT";
	public static final String INTENT_START_VIDEO_RECORDING = "ca.ilanguage.oprime.intent.action.START_VIDEO_RECORDING_SERVICE";
	public static final String INTENT_STOP_AUDIO_RECORDING = "ca.ilanguage.oprime.intent.action.BROADCAST_STOP_AUDIO_RECORDING_SERVICE";
    public static final String INTENT_STOP_VIDEO_RECORDING = "ca.ilanguage.oprime.intent.action.BROADCAST_STOP_VIDEO_RECORDING_SERVICE";
    public static final String INTENT_PARTIAL_SPEECH_RECOGNITION_RESULT = "ca.ilanguage.oprime.intent.action.BROADCAST_PARTIAL_SPEECH_RECOGNITION_RESULT";
	public static final String INTENT_TAKE_PICTURE = "ca.ilanguage.oprime.intent.action.TAKE_PICTURE";

	
	public static String KEYSTORE_PASS = "notarealpassword";

	/*
	 * Preferences for persisting values
	 */
	public static final String PREFERENCE_EXPERIEMENTER_CODE = "experimenterCode";
	public static final String PREFERENCE_EXPERIMENT_AUTO_ADVANCE_ON_TOUCH = "autoAdvanceStimuliOnTouch";
	public static final String PREFERENCE_EXPERIMENT_LANGUAGE = "experimentlanguage";
	public static final String PREFERENCE_LAST_PICTURE_TAKEN = "lastpicturetaken";
	public static final String PREFERENCE_NAME = "OPrimePrefs";
	public static final String PREFERENCE_PARTICIPANT_BIRTHDATE = "participantbirthdate";
	public static final String PREFERENCE_PARTICIPANT_DETAILS = "participantdetails";
	public static final String PREFERENCE_PARTICIPANT_ENDTIME = "participantendtime";
	public static final String PREFERENCE_PARTICIPANT_FIRSTNAME = "participantfirstname";
	public static final String PREFERENCE_PARTICIPANT_GENDER = "participantgender";
	public static final String PREFERENCE_PARTICIPANT_ID = "participantId";
	public static final String PREFERENCE_PARTICIPANT_LANGUAGES = "participantlangs";
	public static final String PREFERENCE_PARTICIPANT_LASTNAME = "participantlastname";
	public static final String PREFERENCE_PARTICIPANT_NUMBER_IN_DAY = "participantnumberinday";
	public static final String PREFERENCE_PARTICIPANT_STARTTIME = "participantstarttime";
	public static final String PREFERENCE_REPLAY_PARTICIPANT_CODE = "replayparticipantcode";
	public static final String PREFERENCE_REPLAY_RESULTS_MODE = "replayresults";
	public static final String PREFERENCE_TESTING_DAY_NUMBER = "testingdaynumber";
	
	public static final String SHARED_OUTPUT_DIR = DEFAULT_OUTPUT_DIRECTORY;
	public static final String TAG = "OPrime";
	public static final String USER_AGENT_STRING = "OfflineAndroidApp";
	public static final String WEB_APP_BASE_DIR = "www";

	public static String getSafeUri(String input) {
		/** Used to sanitize a string to be {@link Uri} safe. */
		Pattern sSanitizePattern = Pattern.compile("[^a-z0-9-_]");
		if (input == null) {
			return null;
		}
		return sSanitizePattern
				.matcher(input.toLowerCase().replaceAll("'", "")).replaceAll(
						"_");
	}

	public static String getStartUrl() {
		return "file:///android_asset/www/index.html";
	}
}
