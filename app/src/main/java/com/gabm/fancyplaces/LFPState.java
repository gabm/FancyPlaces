package com.gabm.fancyplaces;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by gabm on 29.12.14.
 */
public class LFPState implements Parcelable {
    public int mode = 1;
    public ImageFile OriginalImageFile = null;

    LFPState() {
    }

    LFPState(Parcel in) {
        mode = in.readInt();
        OriginalImageFile = in.readParcelable(ImageFile.class.getClassLoader());
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(mode);
        dest.writeParcelable(OriginalImageFile, flags);

    }
}
