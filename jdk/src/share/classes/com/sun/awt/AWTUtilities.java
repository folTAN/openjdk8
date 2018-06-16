/*
 *版权所有（c）1995,2013，Oracle和/或其附属公司。版权所有。
 *请勿更改或删除版权声明或本文件头。
 *
 *此代码是免费软件;你可以重新分配和/或修改它
 *仅限于GNU通用公共许可证版本2的条款，如
 *由自由软件基金会发布。 Oracle指定了这一点
 *特定文件受限于所提供的“Classpath”异常
 *由Oracle在伴随此代码的LICENSE文件中提供。
 *
 *这个代码是分发的，希望它会有用，但没有
 *任何担保;甚至没有对适销性或适销性的暗示保证
 *针对特定用途的适用性。请参阅GNU通用公共许可证
 *版本2了解更多详情（一份副本包含在LICENSE文件中
 *附有此代码）。
 *
 *您应该收到GNU通用公共许可证版本的副本
 * 2与这项工作一起;如果没有，请写信给自由软件基金会，
 * Inc.，51 Franklin St，Fifth Floor，Boston，MA 02110-1301 USA。
 *
 *请联系Oracle，500 Oracle Parkway，Redwood Shores，CA 94065 USA
 *或访问www.oracle.com如果你需要更多的信息或有任何
 *问题。
 */

package com.sun.awt;

import java.awt.*;

import sun.awt.AWTAccessor;
import sun.awt.SunToolkit;

/**
 * AWT的实用方法集合。
 *
 * 由类的静态方法提供的功能包括：
 * <ul>
 * <li> 在top-level window（顶层窗口）上设置形状
 * <li> 为top-level window（顶层窗口）的每个像素设置一个常量alpha值 
 * <li> 让一个窗口不透明，之后它在屏幕上绘制明确的绘制像素，每个像素使用任意的alpha值。
 * <li>  为组件设置“混合切口”形状。 
 * </ul>
 * <p>
 * “顶层窗口” 是一个{@code Windows}类（或者其后代，例如{@code JFrame}）的一个实例，
 * <p>
 * 本地平台(the native platform)可能不支持某些提到的功能(mentioned features)。
 * 为了确定是否支持一个特定的特性，用户必须使用该类的{@code isTranslucencySupported()}方法，
 * 传递一个期望的半透明类型（{@code Translucency} 枚举(enum)的成员）作为参数。
 * <p>
 * 每像素alpha功能还要求用户使用具有半透明功能的图形配置创建她/他的（图形）窗户。
 * 必须使用{@code isTranslucencyCapable()}方法来验证
 * 给定的GraphicsConfiguration是否支持trasnlcency效果。
 * <p>
 * <b>警告</b>: 该类是实现细节，仅用于核心平台之外的有限使用。
 * 此API可能会在更新版本之间发生急剧变化，甚至可能会被删除或移入其他某个软件包/类中。
 */
public final class AWTUtilities {

    /**
     * AWTUtilities类不应该被实例化(instantiated)
     */
    private AWTUtilities() {
    }

    /**
     * 底层系统支持的半透明性。(Kinds of translucency supported by the underlying system.)
     *  @see #isTranslucencySupported
     */
    public static enum Translucency {
        /**
         * 表示在Windows底层系统(underlying system for windows)的支持上，
         * 每个像素(pixel)都保证是完全的不透明的，则alpha值为1.0；
         * 或者完全透明，其alpha值为0.0
         */
        PERPIXEL_TRANSPARENT,

        /**
         * 表示(Represents)Windows底层系统的支持，其所有像素具有相同的alpha值（包括0.0和1.0）。
         */
        TRANSLUCENT,

        /**
         * 表示基础系统支持针对包含或可能包含与之间并且包括0.0和1.0的任意alpha值的像素窗口。
         */
        PERPIXEL_TRANSLUCENT;
    }


    /**
     * 返回底层系统是否支持给定的半透明级别(level of translucency)。
     *
     * 请注意，此方法有时可能会返回指示支持特定级别的值，
     * 但本地窗口系统可能仍不支持给定的半透明级别（由于窗口系统中的错误）。
     *
     * @param translucencyKind 一种受系统支持的半透明类型（PERPIXEL_TRANSPARENT，
     *              TRANSLUCENT或PERPIXEL_TRANSLUCENT）
     * @return 是否支持给定的半透明类型
     */
    public static boolean isTranslucencySupported(Translucency translucencyKind) {
        switch (translucencyKind) {
            case PERPIXEL_TRANSPARENT:
                return isWindowShapingSupported();
            case TRANSLUCENT:
                return isWindowOpacitySupported();
            case PERPIXEL_TRANSLUCENT:
                return isWindowTranslucencySupported();
        }
        return false;
    }


    /**
     * Returns whether the windowing system supports changing the opacity
     * value of top-level windows.
     * Note that this method may sometimes return true, but the native
     * windowing system may still not support the concept of
     * translucency (due to the bugs in the windowing system).
     */
    private static boolean isWindowOpacitySupported() {
        Toolkit curToolkit = Toolkit.getDefaultToolkit();
        if (!(curToolkit instanceof SunToolkit)) {
            return false;
        }
        return ((SunToolkit)curToolkit).isWindowOpacitySupported();
    }

    /**
     * 设置窗口的不透明度(opacity)。 不透明度在[0..1]的范围(range)内。
     * 请注意，设置不透明度级别为0
     * 可能会或可能不会禁用此窗口上的鼠标事件处理(mouse event handling on this window)。 
     * 这是一个依赖于平台的行为(platform-dependent behavior)。
     *
     * 为了使此方法启用半透明效果(translucency effect)，
     * isTranslucencySupported() 方法应指示支持半透明的TRANSLUCENT级别。
     *
     * <p>
     * 还要注意，当设置不透明度值<1时，窗口不能处于全屏模式。 
     * 否则，抛出IllegalArgumentException。
     *
     * @param window 设置不透明度级别的窗口
     * @param opacity 设置窗口的不透明度级别
     * @throws NullPointerException 如果窗口参数为空(null)
     * @throws IllegalArgumentException 如果不透明度超出范围[0..1]
     * @throws IllegalArgumentException 如果窗口处于全屏模式，并且不透明度小于1.0(float)
     * @throws UnsupportedOperationException 如果不支持TRANSLUCENT半透明类型
     */
    public static void setWindowOpacity(Window window, float opacity) {
        if (window == null) {
            throw new NullPointerException(
                    "The window argument should not be null.");
        }

        AWTAccessor.getWindowAccessor().setOpacity(window, opacity);
    }

    /**
     * 获取窗口的不透明度。如果不透明度尚未设置，则该方法返回1.0。
     *
     * @param window 获取不透明度级别的窗口
     * @throws NullPointerException 如果窗口参数为空
     */
    public static float getWindowOpacity(Window window) {
        if (window == null) {
            throw new NullPointerException(
                    "The window argument should not be null.");
        }

        return AWTAccessor.getWindowAccessor().getOpacity(window);
    }

    /**
     * 返回窗口系统是否支持改变顶层窗口的形状(shape)。
     * 
     * 请注意，该方法有时可能返回true，
     * 但是本机窗口系统可能仍然不支持整形的概念(concept of shaping)（由于窗口系统中的错误）。
     */
    public static boolean isWindowShapingSupported() {
        Toolkit curToolkit = Toolkit.getDefaultToolkit();
        if (!(curToolkit instanceof SunToolkit)) {
            return false;
        }
        return ((SunToolkit)curToolkit).isWindowShapingSupported();
    }

    /**
     * 返回实现Shape接口的对象，并表示之前(previously)通过调用 setWindowShape() 方法设置的形状。 
     * 如果尚未设置形状，或者形状已重置为空(null)，则此方法返回null。
     *
     * @param window 从中获取形状的窗口
     * @return 窗口的当前形状
     * @throws NullPointerException 如果窗口参数为空(null)
     */
    public static Shape getWindowShape(Window window) {
        if (window == null) {
            throw new NullPointerException(
                    "The window argument should not be null.");
        }
        return AWTAccessor.getWindowAccessor().getShape(window);
    }

    /**
     * Sets a shape for the given window.
     * If the shape argument is null, this methods restores
     * the default shape making the window rectangular.
     * <p>
     * Note that in order to set a shape, the window must be undecorated.
     * If the window is decorated, this method ignores the {@code shape}
     * argument and resets the shape to null.
     * <p>Also note that the window must not be in the full-screen mode
     * when setting a non-null shape. Otherwise the IllegalArgumentException
     * is thrown.
     * <p>Depending on the platform, the method may return without
     * effecting the shape of the window if the window has a non-null warning
     * string ({@link Window#getWarningString()}). In this case the passed
     * shape object is ignored.
     *
     * @param window the window to set the shape to
     * @param shape the shape to set to the window
     * @throws NullPointerException if the window argument is null
     * @throws IllegalArgumentException if the window is in full screen mode,
     *                                  and the shape is not null
     * @throws UnsupportedOperationException if the PERPIXEL_TRANSPARENT
     *                                       translucency kind is not supported
     */
    public static void setWindowShape(Window window, Shape shape) {
        if (window == null) {
            throw new NullPointerException(
                    "The window argument should not be null.");
        }
        AWTAccessor.getWindowAccessor().setShape(window, shape);
    }

    private static boolean isWindowTranslucencySupported() {
        /*
         * Per-pixel alpha is supported if all the conditions are TRUE:
         *    1. The toolkit is a sort of SunToolkit
         *    2. The toolkit supports translucency in general
         *        (isWindowTranslucencySupported())
         *    3. There's at least one translucency-capable
         *        GraphicsConfiguration
         */

        Toolkit curToolkit = Toolkit.getDefaultToolkit();
        if (!(curToolkit instanceof SunToolkit)) {
            return false;
        }

        if (!((SunToolkit)curToolkit).isWindowTranslucencySupported()) {
            return false;
        }

        GraphicsEnvironment env =
            GraphicsEnvironment.getLocalGraphicsEnvironment();

        // If the default GC supports translucency return true.
        // It is important to optimize the verification this way,
        // see CR 6661196 for more details.
        if (isTranslucencyCapable(env.getDefaultScreenDevice()
                    .getDefaultConfiguration()))
        {
            return true;
        }

        // ... otherwise iterate through all the GCs.
        GraphicsDevice[] devices = env.getScreenDevices();

        for (int i = 0; i < devices.length; i++) {
            GraphicsConfiguration[] configs = devices[i].getConfigurations();
            for (int j = 0; j < configs.length; j++) {
                if (isTranslucencyCapable(configs[j])) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Enables the per-pixel alpha support for the given window.
     * Once the window becomes non-opaque (the isOpaque is set to false),
     * the drawing sub-system is starting to respect the alpha value of each
     * separate pixel. If a pixel gets painted with alpha color component
     * equal to zero, it becomes visually transparent, if the alpha of the
     * pixel is equal to 255, the pixel is fully opaque. Interim values
     * of the alpha color component make the pixel semi-transparent (i.e.
     * translucent).
     * <p>Note that in order for the window to support the per-pixel alpha
     * mode, the window must be created using the GraphicsConfiguration
     * for which the {@link #isTranslucencyCapable}
     * method returns true.
     * <p>Also note that some native systems enable the per-pixel translucency
     * mode for any window created using the translucency-compatible
     * graphics configuration. However, it is highly recommended to always
     * invoke the setWindowOpaque() method for these windows, at least for
     * the sake of cross-platform compatibility reasons.
     * <p>Also note that the window must not be in the full-screen mode
     * when making it non-opaque. Otherwise the IllegalArgumentException
     * is thrown.
     * <p>If the window is a {@code Frame} or a {@code Dialog}, the window must
     * be undecorated prior to enabling the per-pixel translucency effect (see
     * {@link Frame#setUndecorated()} and/or {@link Dialog#setUndecorated()}).
     * If the window becomes decorated through a subsequent call to the
     * corresponding {@code setUndecorated()} method, the per-pixel
     * translucency effect will be disabled and the opaque property reset to
     * {@code true}.
     * <p>Depending on the platform, the method may return without
     * effecting the opaque property of the window if the window has a non-null
     * warning string ({@link Window#getWarningString()}). In this case
     * the passed 'isOpaque' value is ignored.
     *
     * @param window the window to set the shape to
     * @param isOpaque whether the window must be opaque (true),
     *                 or translucent (false)
     * @throws NullPointerException if the window argument is null
     * @throws IllegalArgumentException if the window uses
     *                                  a GraphicsConfiguration for which the
     *                                  {@code isTranslucencyCapable()}
     *                                  method returns false
     * @throws IllegalArgumentException if the window is in full screen mode,
     *                                  and the isOpaque is false
     * @throws IllegalArgumentException if the window is decorated and the
     * isOpaque argument is {@code false}.
     * @throws UnsupportedOperationException if the PERPIXEL_TRANSLUCENT
     *                                       translucency kind is not supported
     */
    public static void setWindowOpaque(Window window, boolean isOpaque) {
        if (window == null) {
            throw new NullPointerException(
                    "The window argument should not be null.");
        }
        if (!isOpaque && !isTranslucencySupported(Translucency.PERPIXEL_TRANSLUCENT)) {
            throw new UnsupportedOperationException(
                    "The PERPIXEL_TRANSLUCENT translucency kind is not supported");
        }
        AWTAccessor.getWindowAccessor().setOpaque(window, isOpaque);
    }

    /**
     * Returns whether the window is opaque or translucent.
     *
     * @param window the window to set the shape to
     * @return whether the window is currently opaque (true)
     *         or translucent (false)
     * @throws NullPointerException if the window argument is null
     */
    public static boolean isWindowOpaque(Window window) {
        if (window == null) {
            throw new NullPointerException(
                    "The window argument should not be null.");
        }

        return window.isOpaque();
    }

    /**
     * Verifies whether a given GraphicsConfiguration supports
     * the PERPIXEL_TRANSLUCENT kind of translucency.
     * All windows that are intended to be used with the {@link #setWindowOpaque}
     * method must be created using a GraphicsConfiguration for which this method
     * returns true.
     * <p>Note that some native systems enable the per-pixel translucency
     * mode for any window created using a translucency-capable
     * graphics configuration. However, it is highly recommended to always
     * invoke the setWindowOpaque() method for these windows, at least
     * for the sake of cross-platform compatibility reasons.
     *
     * @param gc GraphicsConfiguration
     * @throws NullPointerException if the gc argument is null
     * @return whether the given GraphicsConfiguration supports
     *         the translucency effects.
     */
    public static boolean isTranslucencyCapable(GraphicsConfiguration gc) {
        if (gc == null) {
            throw new NullPointerException("The gc argument should not be null");
        }
        /*
        return gc.isTranslucencyCapable();
        */
        Toolkit curToolkit = Toolkit.getDefaultToolkit();
        if (!(curToolkit instanceof SunToolkit)) {
            return false;
        }
        return ((SunToolkit)curToolkit).isTranslucencyCapable(gc);
    }

    /**
     * Sets a 'mixing-cutout' shape for the given component.
     *
     * By default a lightweight component is treated as an opaque rectangle for
     * the purposes of the Heavyweight/Lightweight Components Mixing feature.
     * This method enables developers to set an arbitrary shape to be cut out
     * from heavyweight components positioned underneath the lightweight
     * component in the z-order.
     * <p>
     * The {@code shape} argument may have the following values:
     * <ul>
     * <li>{@code null} - reverts the default cutout shape (the rectangle equal
     * to the component's {@code getBounds()})
     * <li><i>empty-shape</i> - does not cut out anything from heavyweight
     * components. This makes the given lightweight component effectively
     * transparent. Note that descendants of the lightweight component still
     * affect the shapes of heavyweight components.  An example of an
     * <i>empty-shape</i> is {@code new Rectangle()}.
     * <li><i>non-empty-shape</i> - the given shape will be cut out from
     * heavyweight components.
     * </ul>
     * <p>
     * The most common example when the 'mixing-cutout' shape is needed is a
     * glass pane component. The {@link JRootPane#setGlassPane()} method
     * automatically sets the <i>empty-shape</i> as the 'mixing-cutout' shape
     * for the given glass pane component.  If a developer needs some other
     * 'mixing-cutout' shape for the glass pane (which is rare), this must be
     * changed manually after installing the glass pane to the root pane.
     * <p>
     * Note that the 'mixing-cutout' shape neither affects painting, nor the
     * mouse events handling for the given component. It is used exclusively
     * for the purposes of the Heavyweight/Lightweight Components Mixing
     * feature.
     *
     * @param component the component that needs non-default
     * 'mixing-cutout' shape
     * @param shape the new 'mixing-cutout' shape
     * @throws NullPointerException if the component argument is {@code null}
     */
    public static void setComponentMixingCutoutShape(Component component,
            Shape shape)
    {
        if (component == null) {
            throw new NullPointerException(
                    "The component argument should not be null.");
        }

        AWTAccessor.getComponentAccessor().setMixingCutoutShape(component,
                shape);
    }
}

