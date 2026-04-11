package net.azisaba.interchatmod.common.util;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class Constants {
    public static final Set<String> FORMAT_VARIABLES = Collections.unmodifiableSet(new HashSet<>(Arrays.asList(
            "%gname", "%server", "%playername", "%username", "%username-n", "%msg", "%prereplace-b", "%prereplace",
            "%prefix", "%{prefix:server}", "%{prefix:server:default}", "%suffix", "%{suffix:server}", "%{suffix:server:default}"
    )));
    public static final String DEFAULT_API_HOST = "api-ktor.azisaba.net";
}
