package android.support.v17.leanback.widget;

import android.support.v17.leanback.widget.ItemBridgeAdapter.Wrapper;
import android.view.View;

public class ItemBridgeAdapterShadowOverlayWrapper extends Wrapper {
    private final ShadowOverlayHelper mHelper;

    public ItemBridgeAdapterShadowOverlayWrapper(ShadowOverlayHelper helper) {
        this.mHelper = helper;
    }

    public View createWrapper(View root) {
        return this.mHelper.createShadowOverlayContainer(root.getContext());
    }

    public void wrap(View wrapper, View wrapped) {
        ((ShadowOverlayContainer) wrapper).wrap(wrapped);
    }
}
