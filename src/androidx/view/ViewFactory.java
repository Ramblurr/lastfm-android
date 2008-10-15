package androidx.view;

import android.content.Context;
import android.view.View;

public interface ViewFactory {
  View createView(Context context);
}
