package com.example.gabm.fancyplaces;

import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;
import android.view.View;

/**
 * Created by gabm on 24.12.14.
 */
public class SEPState implements Parcelable {

    public static final Parcelable.Creator<SEPState> CREATOR
            = new Parcelable.Creator<SEPState>() {
        public SEPState createFromParcel(Parcel in) {
            return new SEPState(in);
        }

        public SEPState[] newArray(int size) {
            return new SEPState[size];
        }
    };
    public int mode;
    public int result_code = 0;
    public FancyPlace data = null;
    public Bitmap image = null;
    // visibilities
    public ViewElementVisibility viewElementVisibility = new ViewElementVisibility();

    public SEPState() {
    }

    public SEPState(Parcel in) {
        mode = in.readInt();
        result_code = in.readInt();

        data = in.readParcelable(FancyPlace.class.getClassLoader());
        image = in.readParcelable(Bitmap.class.getClassLoader());

        viewElementVisibility = in.readParcelable(ViewElementVisibility.class.getClassLoader());
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(mode);
        dest.writeInt(result_code);

        dest.writeParcelable(data, flags);
        dest.writeParcelable(image, flags);

        dest.writeParcelable(viewElementVisibility, flags);
    }

    public class ViewElementVisibility implements Parcelable {

        // title
        public int titleCardVisibility = View.GONE;
        public int titleEditTextVisibility = View.GONE;
        // map
        public int mapCardVisibility = View.GONE;
        public int mapVisibility = View.GONE;
        public int mapUpdateButtonVisibility = View.GONE;
        // note
        public int notesCardVisibility = View.GONE;
        public int notesTextViewVisibility = View.GONE;
        public int notesEditTextVisibility = View.GONE;
        // image
        public int imageCardVisibility = View.GONE;
        public int imageViewVisibility = View.GONE;
        public int imageTakePhotoButtonVisibility = View.GONE;
        //buttons
        public int buttonVisibility = View.GONE;

        public ViewElementVisibility() {
        }

        public ViewElementVisibility(Parcel in) {
            titleCardVisibility = in.readInt();
            titleEditTextVisibility = in.readInt();

            mapCardVisibility = in.readInt();
            mapUpdateButtonVisibility = in.readInt();
            mapVisibility = in.readInt();

            notesCardVisibility = in.readInt();
            notesEditTextVisibility = in.readInt();
            notesTextViewVisibility = in.readInt();

            imageCardVisibility = in.readInt();
            imageTakePhotoButtonVisibility = in.readInt();
            imageViewVisibility = in.readInt();

            buttonVisibility = in.readInt();
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeInt(titleCardVisibility);
            dest.writeInt(titleEditTextVisibility);

            dest.writeInt(mapCardVisibility);
            dest.writeInt(mapUpdateButtonVisibility);
            dest.writeInt(mapVisibility);

            dest.writeInt(notesCardVisibility);
            dest.writeInt(notesEditTextVisibility);
            dest.writeInt(notesTextViewVisibility);

            dest.writeInt(imageCardVisibility);
            dest.writeInt(imageTakePhotoButtonVisibility);
            dest.writeInt(imageViewVisibility);

            dest.writeInt(buttonVisibility);

        }
    }

}