package com.moreoutlines.util;

/**
 * Thread-local holder for the outline color that should be applied to render
 * commands submitted during the current block entity render.
 *
 * <p>Block entity renderers submit their geometry to the render command queue
 * with a hard-coded outline color of {@code 0} (no outline). To give a tracked
 * block entity an outline without re-rendering it (which would double-draw and
 * deviate from the live pose), we set the desired color here for the duration of
 * its render and have a mixin on the command queue substitute it into the
 * submitted commands.
 */
public final class OutlineColorContext {
    private static final ThreadLocal<Integer> COLOR = new ThreadLocal<>();

    private OutlineColorContext() {}

    public static void set(int argb) {
        COLOR.set(argb);
    }

    public static void clear() {
        COLOR.remove();
    }

    /** @return the active outline color, or 0 if none is set. */
    public static int get() {
        Integer value = COLOR.get();
        return value == null ? 0 : value;
    }
}
