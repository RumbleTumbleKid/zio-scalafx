package example

import zio.*
import scalafx.application.*
import scalafx.scene.Scene
import zio.stream.ZStream
import scalafx.scene.control.TextField
import scalafx.scene.layout.VBox
import scalafx.application.Platform

object Main extends ZIOAppDefault:

  class FxApp extends JFXApp3:
    lazy val inputTextField  = new TextField

    lazy val resultTextField = new TextField:
      editable = false

    override def start(): Unit =
      stage = new JFXApp3.PrimaryStage:
        title = "ZIO ScalaFX App"
        scene = new Scene:
          content = new VBox(inputTextField, resultTextField)

  val zioApp = (fxApp: FxApp) =>
    for
      _ <- ZStream
             .async: cb =>
               fxApp.inputTextField.text.onChange: (_, _, newValue) =>
                 cb(ZIO.succeed(Chunk(newValue)))
             .tap: s =>
               ZIO.attemptBlocking {
                 Platform.runLater(fxApp.resultTextField.setText(s.toString().toUpperCase()))
               }.fork
             .runCollect
    yield ()

  def run =
    for
      fx <- ZIO.succeed(FxApp())
      _  <- ZIO.attemptBlocking(fx.main(null)).fork
      _  <- zioApp(fx)
    yield ()
