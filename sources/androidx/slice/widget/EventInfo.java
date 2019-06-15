package androidx.slice.widget;

import android.support.annotation.RestrictTo;
import android.support.annotation.RestrictTo.Scope;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public class EventInfo {
    public static final int ACTION_TYPE_BUTTON = 1;
    public static final int ACTION_TYPE_CONTENT = 3;
    public static final int ACTION_TYPE_SEE_MORE = 4;
    public static final int ACTION_TYPE_SLIDER = 2;
    public static final int ACTION_TYPE_TOGGLE = 0;
    public static final int POSITION_CELL = 2;
    public static final int POSITION_END = 1;
    public static final int POSITION_START = 0;
    public static final int ROW_TYPE_GRID = 1;
    public static final int ROW_TYPE_LIST = 0;
    public static final int ROW_TYPE_MESSAGING = 2;
    public static final int ROW_TYPE_PROGRESS = 5;
    public static final int ROW_TYPE_SHORTCUT = -1;
    public static final int ROW_TYPE_SLIDER = 4;
    public static final int ROW_TYPE_TOGGLE = 3;
    public static final int STATE_OFF = 0;
    public static final int STATE_ON = 1;
    public int actionCount = -1;
    public int actionIndex = -1;
    public int actionPosition = -1;
    public int actionType;
    public int rowIndex;
    public int rowTemplateType;
    public int sliceMode;
    public int state = -1;

    @RestrictTo({Scope.LIBRARY})
    @Retention(RetentionPolicy.SOURCE)
    public @interface SliceActionType {
    }

    @RestrictTo({Scope.LIBRARY})
    @Retention(RetentionPolicy.SOURCE)
    public @interface SliceButtonPosition {
    }

    @RestrictTo({Scope.LIBRARY})
    @Retention(RetentionPolicy.SOURCE)
    public @interface SliceRowType {
    }

    public EventInfo(int sliceMode, int actionType, int rowTemplateType, int rowIndex) {
        this.sliceMode = sliceMode;
        this.actionType = actionType;
        this.rowTemplateType = rowTemplateType;
        this.rowIndex = rowIndex;
    }

    public void setPosition(int actionPosition, int actionIndex, int actionCount) {
        this.actionPosition = actionPosition;
        this.actionIndex = actionIndex;
        this.actionCount = actionCount;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("mode=");
        sb.append(SliceView.modeToString(this.sliceMode));
        sb.append(", actionType=");
        sb.append(actionToString(this.actionType));
        sb.append(", rowTemplateType=");
        sb.append(rowTypeToString(this.rowTemplateType));
        sb.append(", rowIndex=");
        sb.append(this.rowIndex);
        sb.append(", actionPosition=");
        sb.append(positionToString(this.actionPosition));
        sb.append(", actionIndex=");
        sb.append(this.actionIndex);
        sb.append(", actionCount=");
        sb.append(this.actionCount);
        sb.append(", state=");
        sb.append(this.state);
        return sb.toString();
    }

    private static String positionToString(int position) {
        switch (position) {
            case 0:
                return "START";
            case 1:
                return "END";
            case 2:
                return "CELL";
            default:
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append("unknown position: ");
                stringBuilder.append(position);
                return stringBuilder.toString();
        }
    }

    private static String actionToString(int action) {
        switch (action) {
            case 0:
                return "TOGGLE";
            case 1:
                return "BUTTON";
            case 2:
                return "SLIDER";
            case 3:
                return "CONTENT";
            case 4:
                return "SEE MORE";
            default:
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append("unknown action: ");
                stringBuilder.append(action);
                return stringBuilder.toString();
        }
    }

    private static String rowTypeToString(int type) {
        switch (type) {
            case -1:
                return "SHORTCUT";
            case 0:
                return "LIST";
            case 1:
                return "GRID";
            case 2:
                return "MESSAGING";
            case 3:
                return "TOGGLE";
            case 4:
                return "SLIDER";
            case 5:
                return "PROGRESS";
            default:
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append("unknown row type: ");
                stringBuilder.append(type);
                return stringBuilder.toString();
        }
    }
}
