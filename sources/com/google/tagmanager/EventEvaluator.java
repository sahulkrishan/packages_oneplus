package com.google.tagmanager;

import com.google.tagmanager.ResourceUtil.ExpandedResource;

class EventEvaluator {
    private final ExpandedResource mResource;
    private final Runtime mRuntime;

    public EventEvaluator(Runtime runtime, ExpandedResource resource) {
        if (runtime != null) {
            this.mRuntime = runtime;
            if (resource == runtime.getResource()) {
                this.mResource = runtime.getResource();
                return;
            }
            throw new IllegalArgumentException("resource must be the same as the resource in runtime");
        }
        throw new NullPointerException("runtime cannot be null");
    }

    /* Access modifiers changed, original: 0000 */
    public void evaluateEvent(String eventName) {
        throw new UnsupportedOperationException("this code not yet written");
    }
}
