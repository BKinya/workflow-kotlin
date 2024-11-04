package workflow.tutorial

import com.squareup.workflow1.Snapshot
import com.squareup.workflow1.StatefulWorkflow
import com.squareup.workflow1.action
import workflow.tutorial.WelcomeWorkflow.LoggedIn
import workflow.tutorial.WelcomeWorkflow.State

object WelcomeWorkflow : StatefulWorkflow<Unit, State, LoggedIn, WelcomeScreen>() {

  data class State(
    val username: String
  )

  data class LoggedIn(val username: String)

  override fun initialState(
    props: Unit,
    snapshot: Snapshot?
  ): State = State(username = "")

  override fun render(
    renderProps: Unit,
    renderState: State,
    context: RenderContext
  ): WelcomeScreen = WelcomeScreen(
      username = renderState.username,
      onUsernameChanged = { context.actionSink.send(onNameChanged(it)) },
      onLoginTapped = {
        // Whenever the login button is tapped, emit the onLogin action.
        context.actionSink.send(onLogin())
      }
  )

  private fun onNameChanged(name: String) = action("onNameChanged") {
    state = state.copy(username = name)
  }

  private fun onLogin() = action("onLogin") {
    setOutput(LoggedIn(state.username))
  }

  override fun snapshotState(state: State): Snapshot? = null
}
