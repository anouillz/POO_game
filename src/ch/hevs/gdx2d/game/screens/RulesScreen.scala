package ch.hevs.gdx2d.game.screens

import ch.hevs.gdx2d.components.bitmaps.BitmapImage
import ch.hevs.gdx2d.components.screen_management.RenderingScreen
import ch.hevs.gdx2d.lib.GdxGraphics
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.files.FileHandle
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator.FreeTypeFontParameter

class RulesScreen extends RenderingScreen{

  var imgPortal: BitmapImage = _
  var imgEnemy: BitmapImage = _

  var font40: BitmapFont = _

  var goal: String = "1/ Your goal is to find the red portal to escape"
  var beware: String = "2/ Beware of the guards, they might kill you"
  var time: String = "3/ You have 60 seconds to escape"


  override def onInit(): Unit = {
    imgPortal = new BitmapImage("data/images/portal.png")
    imgEnemy = new BitmapImage("data/images/enemyExample.png")

    val optimusF: FileHandle = Gdx.files.internal("data/font/OptimusPrinceps.ttf")

    val generator: FreeTypeFontGenerator = new FreeTypeFontGenerator(optimusF)
    val parameter: FreeTypeFontParameter = new FreeTypeFontParameter()

    parameter.size = 60
    font40 = generator.generateFont(parameter)
    font40.setColor(Color.WHITE)

    generator.dispose()


  }

  override def onGraphicRender(g: GdxGraphics): Unit = {
    g.clear(Color.DARK_GRAY)

    g.drawStringCentered(g.getScreenHeight/2 + 400, goal, font40)
    g.drawPicture(g.getScreenWidth / 2, g.getScreenHeight / 2 + 250, imgPortal)
    g.drawStringCentered(g.getScreenHeight/2 + 100, beware, font40)
    g.drawPicture(g.getScreenWidth / 2, g.getScreenHeight / 2, imgEnemy)
    g.drawStringCentered(g.getScreenHeight/2 - 100, time, font40)
    g.drawStringCentered(g.getScreenHeight/2 - 200, "Press '1' to go back to menu", font40)

    g.drawFPS()
  }
}
