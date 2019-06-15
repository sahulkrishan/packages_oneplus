package androidx.slice;

import android.support.annotation.NonNull;
import android.support.annotation.RestrictTo;
import android.support.annotation.RestrictTo.Scope;
import androidx.versionedparcelable.VersionedParcelable;

@RestrictTo({Scope.LIBRARY_GROUP})
public final class SliceSpec implements VersionedParcelable {
    int mRevision;
    String mType;

    public SliceSpec(@NonNull String type, int revision) {
        this.mType = type;
        this.mRevision = revision;
    }

    public String getType() {
        return this.mType;
    }

    public int getRevision() {
        return this.mRevision;
    }

    public boolean canRender(@NonNull SliceSpec candidate) {
        boolean z = false;
        if (!this.mType.equals(candidate.mType)) {
            return false;
        }
        if (this.mRevision >= candidate.mRevision) {
            z = true;
        }
        return z;
    }

    public boolean equals(Object obj) {
        boolean z = false;
        if (!(obj instanceof SliceSpec)) {
            return false;
        }
        SliceSpec other = (SliceSpec) obj;
        if (this.mType.equals(other.mType) && this.mRevision == other.mRevision) {
            z = true;
        }
        return z;
    }

    public int hashCode() {
        return this.mType.hashCode() + this.mRevision;
    }

    public String toString() {
        return String.format("SliceSpec{%s,%d}", new Object[]{this.mType, Integer.valueOf(this.mRevision)});
    }
}
