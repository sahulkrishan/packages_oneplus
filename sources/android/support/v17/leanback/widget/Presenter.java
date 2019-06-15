package android.support.v17.leanback.widget;

import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class Presenter implements FacetProvider {
    private Map<Class, Object> mFacets;

    public static abstract class ViewHolderTask {
        public void run(ViewHolder holder) {
        }
    }

    public static class ViewHolder implements FacetProvider {
        private Map<Class, Object> mFacets;
        public final View view;

        public ViewHolder(View view) {
            this.view = view;
        }

        public final Object getFacet(Class<?> facetClass) {
            if (this.mFacets == null) {
                return null;
            }
            return this.mFacets.get(facetClass);
        }

        public final void setFacet(Class<?> facetClass, Object facetImpl) {
            if (this.mFacets == null) {
                this.mFacets = new HashMap();
            }
            this.mFacets.put(facetClass, facetImpl);
        }
    }

    public abstract void onBindViewHolder(ViewHolder viewHolder, Object obj);

    public abstract ViewHolder onCreateViewHolder(ViewGroup viewGroup);

    public abstract void onUnbindViewHolder(ViewHolder viewHolder);

    public void onBindViewHolder(ViewHolder viewHolder, Object item, List<Object> list) {
        onBindViewHolder(viewHolder, item);
    }

    public void onViewAttachedToWindow(ViewHolder holder) {
    }

    public void onViewDetachedFromWindow(ViewHolder holder) {
        cancelAnimationsRecursive(holder.view);
    }

    protected static void cancelAnimationsRecursive(View view) {
        if (view != null && view.hasTransientState()) {
            view.animate().cancel();
            if (view instanceof ViewGroup) {
                int count = ((ViewGroup) view).getChildCount();
                int i = 0;
                while (view.hasTransientState() && i < count) {
                    cancelAnimationsRecursive(((ViewGroup) view).getChildAt(i));
                    i++;
                }
            }
        }
    }

    public void setOnClickListener(ViewHolder holder, OnClickListener listener) {
        holder.view.setOnClickListener(listener);
    }

    public final Object getFacet(Class<?> facetClass) {
        if (this.mFacets == null) {
            return null;
        }
        return this.mFacets.get(facetClass);
    }

    public final void setFacet(Class<?> facetClass, Object facetImpl) {
        if (this.mFacets == null) {
            this.mFacets = new HashMap();
        }
        this.mFacets.put(facetClass, facetImpl);
    }
}
