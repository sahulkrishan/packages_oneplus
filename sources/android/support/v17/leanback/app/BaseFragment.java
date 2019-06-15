package android.support.v17.leanback.app;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v17.leanback.transition.TransitionHelper;
import android.support.v17.leanback.transition.TransitionListener;
import android.support.v17.leanback.util.StateMachine;
import android.support.v17.leanback.util.StateMachine.Condition;
import android.support.v17.leanback.util.StateMachine.Event;
import android.support.v17.leanback.util.StateMachine.State;
import android.view.View;
import android.view.ViewTreeObserver.OnPreDrawListener;

@Deprecated
public class BaseFragment extends BrandedFragment {
    final Condition COND_TRANSITION_NOT_SUPPORTED = new Condition("EntranceTransitionNotSupport") {
        public boolean canProceed() {
            return TransitionHelper.systemSupportsEntranceTransitions() ^ 1;
        }
    };
    final Event EVT_ENTRANCE_END = new Event("onEntranceTransitionEnd");
    final Event EVT_ON_CREATE = new Event("onCreate");
    final Event EVT_ON_CREATEVIEW = new Event("onCreateView");
    final Event EVT_PREPARE_ENTRANCE = new Event("prepareEntranceTransition");
    final Event EVT_START_ENTRANCE = new Event("startEntranceTransition");
    final State STATE_ENTRANCE_COMPLETE = new State("ENTRANCE_COMPLETE", true, false);
    final State STATE_ENTRANCE_INIT = new State("ENTRANCE_INIT");
    final State STATE_ENTRANCE_ON_ENDED = new State("ENTRANCE_ON_ENDED") {
        public void run() {
            BaseFragment.this.onEntranceTransitionEnd();
        }
    };
    final State STATE_ENTRANCE_ON_PREPARED = new State("ENTRANCE_ON_PREPARED", true, false) {
        public void run() {
            BaseFragment.this.mProgressBarManager.show();
        }
    };
    final State STATE_ENTRANCE_ON_PREPARED_ON_CREATEVIEW = new State("ENTRANCE_ON_PREPARED_ON_CREATEVIEW") {
        public void run() {
            BaseFragment.this.onEntranceTransitionPrepare();
        }
    };
    final State STATE_ENTRANCE_PERFORM = new State("STATE_ENTRANCE_PERFORM") {
        public void run() {
            BaseFragment.this.mProgressBarManager.hide();
            BaseFragment.this.onExecuteEntranceTransition();
        }
    };
    final State STATE_START = new State("START", true, false);
    Object mEntranceTransition;
    final ProgressBarManager mProgressBarManager = new ProgressBarManager();
    final StateMachine mStateMachine = new StateMachine();

    @SuppressLint({"ValidFragment"})
    BaseFragment() {
    }

    public void onCreate(Bundle savedInstanceState) {
        createStateMachineStates();
        createStateMachineTransitions();
        this.mStateMachine.start();
        super.onCreate(savedInstanceState);
        this.mStateMachine.fireEvent(this.EVT_ON_CREATE);
    }

    /* Access modifiers changed, original: 0000 */
    public void createStateMachineStates() {
        this.mStateMachine.addState(this.STATE_START);
        this.mStateMachine.addState(this.STATE_ENTRANCE_INIT);
        this.mStateMachine.addState(this.STATE_ENTRANCE_ON_PREPARED);
        this.mStateMachine.addState(this.STATE_ENTRANCE_ON_PREPARED_ON_CREATEVIEW);
        this.mStateMachine.addState(this.STATE_ENTRANCE_PERFORM);
        this.mStateMachine.addState(this.STATE_ENTRANCE_ON_ENDED);
        this.mStateMachine.addState(this.STATE_ENTRANCE_COMPLETE);
    }

    /* Access modifiers changed, original: 0000 */
    public void createStateMachineTransitions() {
        this.mStateMachine.addTransition(this.STATE_START, this.STATE_ENTRANCE_INIT, this.EVT_ON_CREATE);
        this.mStateMachine.addTransition(this.STATE_ENTRANCE_INIT, this.STATE_ENTRANCE_COMPLETE, this.COND_TRANSITION_NOT_SUPPORTED);
        this.mStateMachine.addTransition(this.STATE_ENTRANCE_INIT, this.STATE_ENTRANCE_COMPLETE, this.EVT_ON_CREATEVIEW);
        this.mStateMachine.addTransition(this.STATE_ENTRANCE_INIT, this.STATE_ENTRANCE_ON_PREPARED, this.EVT_PREPARE_ENTRANCE);
        this.mStateMachine.addTransition(this.STATE_ENTRANCE_ON_PREPARED, this.STATE_ENTRANCE_ON_PREPARED_ON_CREATEVIEW, this.EVT_ON_CREATEVIEW);
        this.mStateMachine.addTransition(this.STATE_ENTRANCE_ON_PREPARED, this.STATE_ENTRANCE_PERFORM, this.EVT_START_ENTRANCE);
        this.mStateMachine.addTransition(this.STATE_ENTRANCE_ON_PREPARED_ON_CREATEVIEW, this.STATE_ENTRANCE_PERFORM);
        this.mStateMachine.addTransition(this.STATE_ENTRANCE_PERFORM, this.STATE_ENTRANCE_ON_ENDED, this.EVT_ENTRANCE_END);
        this.mStateMachine.addTransition(this.STATE_ENTRANCE_ON_ENDED, this.STATE_ENTRANCE_COMPLETE);
    }

    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        this.mStateMachine.fireEvent(this.EVT_ON_CREATEVIEW);
    }

    public void prepareEntranceTransition() {
        this.mStateMachine.fireEvent(this.EVT_PREPARE_ENTRANCE);
    }

    /* Access modifiers changed, original: protected */
    public Object createEntranceTransition() {
        return null;
    }

    /* Access modifiers changed, original: protected */
    public void runEntranceTransition(Object entranceTransition) {
    }

    /* Access modifiers changed, original: protected */
    public void onEntranceTransitionPrepare() {
    }

    /* Access modifiers changed, original: protected */
    public void onEntranceTransitionStart() {
    }

    /* Access modifiers changed, original: protected */
    public void onEntranceTransitionEnd() {
    }

    public void startEntranceTransition() {
        this.mStateMachine.fireEvent(this.EVT_START_ENTRANCE);
    }

    /* Access modifiers changed, original: 0000 */
    public void onExecuteEntranceTransition() {
        final View view = getView();
        if (view != null) {
            view.getViewTreeObserver().addOnPreDrawListener(new OnPreDrawListener() {
                public boolean onPreDraw() {
                    view.getViewTreeObserver().removeOnPreDrawListener(this);
                    if (FragmentUtil.getContext(BaseFragment.this) == null || BaseFragment.this.getView() == null) {
                        return true;
                    }
                    BaseFragment.this.internalCreateEntranceTransition();
                    BaseFragment.this.onEntranceTransitionStart();
                    if (BaseFragment.this.mEntranceTransition != null) {
                        BaseFragment.this.runEntranceTransition(BaseFragment.this.mEntranceTransition);
                    } else {
                        BaseFragment.this.mStateMachine.fireEvent(BaseFragment.this.EVT_ENTRANCE_END);
                    }
                    return false;
                }
            });
            view.invalidate();
        }
    }

    /* Access modifiers changed, original: 0000 */
    public void internalCreateEntranceTransition() {
        this.mEntranceTransition = createEntranceTransition();
        if (this.mEntranceTransition != null) {
            TransitionHelper.addTransitionListener(this.mEntranceTransition, new TransitionListener() {
                public void onTransitionEnd(Object transition) {
                    BaseFragment.this.mEntranceTransition = null;
                    BaseFragment.this.mStateMachine.fireEvent(BaseFragment.this.EVT_ENTRANCE_END);
                }
            });
        }
    }

    public final ProgressBarManager getProgressBarManager() {
        return this.mProgressBarManager;
    }
}
