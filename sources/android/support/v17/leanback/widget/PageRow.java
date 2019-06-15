package android.support.v17.leanback.widget;

public class PageRow extends Row {
    public PageRow(HeaderItem headerItem) {
        super(headerItem);
    }

    public final boolean isRenderedAsRowView() {
        return false;
    }
}
