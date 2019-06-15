package com.android.settings.datetime.timezone.model;

import java.util.function.Function;
import libcore.util.CountryTimeZones.TimeZoneMapping;

/* compiled from: lambda */
public final /* synthetic */ class -$$Lambda$FilteredCountryTimeZones$ISUVeCzEqV6U2C82Sgby5UdDf3Y implements Function {
    public static final /* synthetic */ -$$Lambda$FilteredCountryTimeZones$ISUVeCzEqV6U2C82Sgby5UdDf3Y INSTANCE = new -$$Lambda$FilteredCountryTimeZones$ISUVeCzEqV6U2C82Sgby5UdDf3Y();

    private /* synthetic */ -$$Lambda$FilteredCountryTimeZones$ISUVeCzEqV6U2C82Sgby5UdDf3Y() {
    }

    public final Object apply(Object obj) {
        return ((TimeZoneMapping) obj).timeZoneId;
    }
}
