package com.squareup.workflow1.ui

/**
 * Adds optional back button handling to a [wrapped] rendering, possibly overriding that
 * the wrapped rendering's own back button handler.
 *
 * @param shadow If `true`, [onBackPressed] is set as the
 * [backPressedHandler][android.view.View.backPressedHandler] after
 * the [wrapped] rendering's view is built / updated, effectively overriding it.
 * If false (the default), [onBackPressed] is set afterward, to allow the wrapped rendering to
 * take precedence if it sets a `backPressedHandler` of its own -- the handler provided
 * here serves as a default.
 *
 * @param onBackPressed The function to fire when the device back button
 * is pressed, or null to set no handler -- or clear a handler that was set previously.
 * Defaults to `null`.
 */
@Suppress("DEPRECATION")
@WorkflowUiExperimentalApi
@Deprecated(
  "Use com.squareup.workflow1.ui.navigation.BackButtonScreen",
  ReplaceWith("BackButtonScreen", "com.squareup.workflow1.ui.navigation.BackButtonScreen")
)
public class BackButtonScreen<W : Any>(
  public val wrapped: W,
  public val shadow: Boolean = false,
  public val onBackPressed: (() -> Unit)? = null
) : AndroidViewRendering<BackButtonScreen<*>> {
  override val viewFactory: ViewFactory<BackButtonScreen<*>> = DecorativeViewFactory(
    type = BackButtonScreen::class,
    map = { outer -> outer.wrapped },
    doShowRendering = { view, innerShowRendering, outerRendering, viewEnvironment ->
      if (!outerRendering.shadow) {
        // Place our handler before invoking innerShowRendering, so that
        // its later calls to view.backPressedHandler will take precedence
        // over ours.
        view.backPressedHandler = outerRendering.onBackPressed
      }

      innerShowRendering.invoke(outerRendering.wrapped, viewEnvironment)

      if (outerRendering.shadow) {
        // Place our handler after invoking innerShowRendering, so that ours wins.
        view.backPressedHandler = outerRendering.onBackPressed
      }
    }
  )
}
