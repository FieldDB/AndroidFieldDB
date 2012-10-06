package ca.ilanguage.aphasia.anomia.android.content;

import android.content.Context;
import ca.ilanguage.oprime.activity.HTML5GameActivity;
import ca.ilanguage.oprime.content.JavaScriptInterface;

public class LanguageLessonsJavaScriptInterface extends JavaScriptInterface {

  private static final long serialVersionUID = -8186639717009199855L;

  public LanguageLessonsJavaScriptInterface(boolean d, String tag,
      String outputDir, Context context, HTML5GameActivity UIParent,
      String assetsPrefix) {
    super(d, tag, outputDir, context, UIParent, assetsPrefix);
  }

  public LanguageLessonsJavaScriptInterface(Context context) {
    super(context);
  }

}
