package net.azisaba.interchatmod.common.util.function;

import java.io.IOException;

@FunctionalInterface
public interface IOSupplier<T> {
    T get() throws IOException;
}
