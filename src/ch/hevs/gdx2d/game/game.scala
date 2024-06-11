package ch.hevs.gdx2d.game

import ch.hevs.gdx2d.desktop.PortableApplication
import ch.hevs.gdx2d.lib.utils.Logger
import ch.hevs.gdx2d.lib.{GdxGraphics, ScreenManager}

class game(var width: Int, var height: Int) extends PortableApplication(width, height) {

  var s: ScreenManager = new ScreenManager


  override def onInit(): Unit = {
    setTitle("Chicago - What's mine is yours")
    Logger.log("Press enter/space to show the next screen, 1/2/3 to transition to them")
    s.registerScreen(classOf[GameScreen])
    s.registerScreen(classOf[RulesScreen])

  }

  override def onGraphicRender(gdxGraphics: GdxGraphics): Unit = {
    s.render(gdxGraphics)
  }
}
