package com.android.settingslib.drawer;

import android.content.ComponentName;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.text.TextUtils;
import android.util.Log;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class DashboardCategory implements Parcelable {
    public static final Creator<DashboardCategory> CREATOR = new Creator<DashboardCategory>() {
        public DashboardCategory createFromParcel(Parcel source) {
            return new DashboardCategory(source);
        }

        public DashboardCategory[] newArray(int size) {
            return new DashboardCategory[size];
        }
    };
    private static final boolean DEBUG = Log.isLoggable(TAG, 3);
    private static final String TAG = "DashboardCategory";
    public static final Comparator<Tile> TILE_COMPARATOR = new Comparator<Tile>() {
        public int compare(Tile lhs, Tile rhs) {
            return rhs.priority - lhs.priority;
        }
    };
    public String key;
    private List<Tile> mTiles = new ArrayList();
    public int priority;
    public CharSequence title;

    DashboardCategory(DashboardCategory in) {
        if (in != null) {
            this.title = in.title;
            this.key = in.key;
            this.priority = in.priority;
            for (Tile tile : in.mTiles) {
                this.mTiles.add(tile);
            }
        }
    }

    public synchronized List<Tile> getTiles() {
        List<Tile> result;
        result = new ArrayList(this.mTiles.size());
        for (Tile tile : this.mTiles) {
            result.add(tile);
        }
        return result;
    }

    public synchronized void addTile(Tile tile) {
        this.mTiles.add(tile);
    }

    public synchronized void addTile(int n, Tile tile) {
        this.mTiles.add(n, tile);
    }

    public synchronized void removeTile(Tile tile) {
        this.mTiles.remove(tile);
    }

    public synchronized void removeTile(int n) {
        this.mTiles.remove(n);
    }

    public int getTilesCount() {
        return this.mTiles.size();
    }

    public Tile getTile(int n) {
        return (Tile) this.mTiles.get(n);
    }

    public synchronized boolean containsComponent(ComponentName component) {
        String str;
        for (Tile tile : this.mTiles) {
            if (TextUtils.equals(tile.intent.getComponent().getClassName(), component.getClassName())) {
                if (DEBUG) {
                    str = TAG;
                    StringBuilder stringBuilder = new StringBuilder();
                    stringBuilder.append("category ");
                    stringBuilder.append(this.key);
                    stringBuilder.append("contains component");
                    stringBuilder.append(component);
                    Log.d(str, stringBuilder.toString());
                }
                return true;
            }
        }
        if (DEBUG) {
            str = TAG;
            StringBuilder stringBuilder2 = new StringBuilder();
            stringBuilder2.append("category ");
            stringBuilder2.append(this.key);
            stringBuilder2.append(" does not contain component");
            stringBuilder2.append(component);
            Log.d(str, stringBuilder2.toString());
        }
        return false;
    }

    public void sortTiles() {
        Collections.sort(this.mTiles, TILE_COMPARATOR);
    }

    public synchronized void sortTiles(String skipPackageName) {
        Collections.sort(this.mTiles, new -$$Lambda$DashboardCategory$hMIMtvkEGTs2t-7RyY7SqwVmOgI(skipPackageName));
    }

    static /* synthetic */ int lambda$sortTiles$0(String skipPackageName, Tile tile1, Tile tile2) {
        String package1 = tile1.intent.getComponent().getPackageName();
        String package2 = tile2.intent.getComponent().getPackageName();
        int packageCompare = String.CASE_INSENSITIVE_ORDER.compare(package1, package2);
        int priorityCompare = tile2.priority - tile1.priority;
        if (priorityCompare != 0) {
            return priorityCompare;
        }
        if (packageCompare != 0) {
            if (TextUtils.equals(package1, skipPackageName)) {
                return -1;
            }
            if (TextUtils.equals(package2, skipPackageName)) {
                return 1;
            }
        }
        return packageCompare;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        TextUtils.writeToParcel(this.title, dest, flags);
        dest.writeString(this.key);
        dest.writeInt(this.priority);
        int count = this.mTiles.size();
        dest.writeInt(count);
        for (int n = 0; n < count; n++) {
            ((Tile) this.mTiles.get(n)).writeToParcel(dest, flags);
        }
    }

    public void readFromParcel(Parcel in) {
        this.title = (CharSequence) TextUtils.CHAR_SEQUENCE_CREATOR.createFromParcel(in);
        this.key = in.readString();
        this.priority = in.readInt();
        int count = in.readInt();
        for (int n = 0; n < count; n++) {
            this.mTiles.add((Tile) Tile.CREATOR.createFromParcel(in));
        }
    }

    DashboardCategory(Parcel in) {
        readFromParcel(in);
    }
}
