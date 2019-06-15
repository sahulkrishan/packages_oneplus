package com.google.analytics.tracking.android;

class Hit {
    private final long mHitId;
    private String mHitString;
    private final long mHitTime;
    private String mHitUrlScheme = "https:";

    /* Access modifiers changed, original: 0000 */
    public String getHitParams() {
        return this.mHitString;
    }

    /* Access modifiers changed, original: 0000 */
    public void setHitString(String hitString) {
        this.mHitString = hitString;
    }

    /* Access modifiers changed, original: 0000 */
    public long getHitId() {
        return this.mHitId;
    }

    /* Access modifiers changed, original: 0000 */
    public long getHitTime() {
        return this.mHitTime;
    }

    Hit(String hitString, long hitId, long hitTime) {
        this.mHitString = hitString;
        this.mHitId = hitId;
        this.mHitTime = hitTime;
    }

    /* Access modifiers changed, original: 0000 */
    public String getHitUrlScheme() {
        return this.mHitUrlScheme;
    }

    /* Access modifiers changed, original: 0000 */
    /* JADX WARNING: Missing block: B:7:0x001e, code skipped:
            return;
     */
    public void setHitUrl(java.lang.String r3) {
        /*
        r2 = this;
        if (r3 == 0) goto L_0x001e;
    L_0x0002:
        r0 = r3.trim();
        r0 = android.text.TextUtils.isEmpty(r0);
        if (r0 == 0) goto L_0x000d;
    L_0x000c:
        goto L_0x001e;
    L_0x000d:
        r0 = r3.toLowerCase();
        r1 = "http:";
        r0 = r0.startsWith(r1);
        if (r0 == 0) goto L_0x001d;
    L_0x0019:
        r0 = "http:";
        r2.mHitUrlScheme = r0;
    L_0x001d:
        return;
    L_0x001e:
        return;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.google.analytics.tracking.android.Hit.setHitUrl(java.lang.String):void");
    }
}
