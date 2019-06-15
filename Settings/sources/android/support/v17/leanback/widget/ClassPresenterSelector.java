package android.support.v17.leanback.widget;

import java.util.ArrayList;
import java.util.HashMap;

public final class ClassPresenterSelector extends PresenterSelector {
    private final HashMap<Class<?>, Object> mClassMap = new HashMap();
    private final ArrayList<Presenter> mPresenters = new ArrayList();

    public ClassPresenterSelector addClassPresenter(Class<?> cls, Presenter presenter) {
        this.mClassMap.put(cls, presenter);
        if (!this.mPresenters.contains(presenter)) {
            this.mPresenters.add(presenter);
        }
        return this;
    }

    public ClassPresenterSelector addClassPresenterSelector(Class<?> cls, PresenterSelector presenterSelector) {
        this.mClassMap.put(cls, presenterSelector);
        Presenter[] innerPresenters = presenterSelector.getPresenters();
        for (int i = 0; i < innerPresenters.length; i++) {
            if (!this.mPresenters.contains(innerPresenters[i])) {
                this.mPresenters.add(innerPresenters[i]);
            }
        }
        return this;
    }

    public Presenter getPresenter(Object item) {
        Object presenter;
        Class<?> cls = item.getClass();
        do {
            presenter = this.mClassMap.get(cls);
            if (presenter instanceof PresenterSelector) {
                Presenter innerPresenter = ((PresenterSelector) presenter).getPresenter(item);
                if (innerPresenter != null) {
                    return innerPresenter;
                }
            }
            cls = cls.getSuperclass();
            if (presenter != null) {
                break;
            }
        } while (cls != null);
        return (Presenter) presenter;
    }

    public Presenter[] getPresenters() {
        return (Presenter[]) this.mPresenters.toArray(new Presenter[this.mPresenters.size()]);
    }
}
