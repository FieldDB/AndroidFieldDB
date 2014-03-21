package com.github.opensourcefieldlinguistics.fielddb.content;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Helper class for providing sample content for user interfaces created by
 * Android template wizards.
 * <p>
 * TODO: Replace all uses of this class before publishing your app.
 */
public class PlaceholderContent {

	/**
	 * An array of sample (dummy) items.
	 */
	public static List<Datum> ITEMS = new ArrayList<Datum>();

	/**
	 * A map of sample (dummy) items, by ID.
	 */
	public static Map<String, Datum> ITEM_MAP = new HashMap<String, Datum>();

	static {
		// Add 3 sample items.
		// addItem(new Datum(
		// "Lorem ipsum wie oienoiwaenoa oaiwneoiawneoiajeow  ipsum wie oienoiwaenoa oaiwneoiawneoiajeow  ipsum wie oienoiwaenoa oaiwneoiawneoiajeow  ipsum wie oienoiwaenoa oaiwneoiawneoiajeow  ipsum wie oienoiwaenoa oaiwneoiawneoiajeow  ipsum wie oienoiwaenoa oaiwneoiawneoiajeow  ipsum wie oienoiwaenoa oaiwneoiawneoiajeow"));
		Datum hello = new Datum("გამარჯობა", "gamardʒoba", "hello", "Hello",
				"(Standard greeting)");
		hello.addImage("gamardZoba.jpg");
		addItem(hello);

		addItem(new Datum("არა მადლობა", "ara madloba", "no.thanks",
				"No thanks", "(most common form)"));
		addItem(new Datum("მადლობა", "madloba", "thanks", "Thanks",
				"(most common form)"));

		Datum howMuch = new Datum("რა ღირს?", "ra ɣirs es?", "what worth is",
				"How much is this?");
		howMuch.addImage("ra_ghirs_es.jpg");
		addItem(howMuch);

		Datum repeat = new Datum("გთხოვთ გაიმეორეთ სამჯერ",
				"ktχoft gaimeorɛt samdʒɹ", "please repeat 3.times",
				"Please repeat 3 times?");
		repeat.addImage("ktxoft_gaimeorets_samdZr.jpg");
		addItem(repeat);

		addItem(new Datum("გაიმეორეთ გთხოვთ", "gaimeorɛt ktχoft",
				"repeat please", "Repeat please?"));

		Datum dontHave = new Datum("არ მაქვს", "ar markʰfs", "no have",
				"I don't have any", "(to begging children).");
		dontHave.addImage("ar_makfs.jpg");
		addItem(dontHave);
	}

	private static void addItem(Datum item) {
		ITEMS.add(item);
		ITEM_MAP.put(item.id, item);
	}

}
