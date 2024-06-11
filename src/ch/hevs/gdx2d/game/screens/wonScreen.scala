package ch.hevs.gdx2d.game.screens

import ch.hevs.gdx2d.components.screen_management.RenderingScreen
import ch.hevs.gdx2d.lib.GdxGraphics
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.files.FileHandle
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator.FreeTypeFontParameter

class wonScreen extends RenderingScreen{
  var font40: BitmapFont = _

  override def onInit(): Unit = {

    val optimusF: FileHandle = Gdx.files.internal("data/font/OptimusPrinceps.ttf")

    val generator: FreeTypeFontGenerator = new FreeTypeFontGenerator(optimusF)
    val parameter: FreeTypeFontParameter = new FreeTypeFontParameter()

    parameter.size = 40
    font40 = generator.generateFont(parameter)
    font40.setColor(Color.WHITE)


    generator.dispose()

  }

  override def onGraphicRender(g: GdxGraphics): Unit = {

    g.clear(Color.BLACK)

    g.drawStringCentered(g.getScreenHeight/2, "You Won !", font40)
    g.drawStringCentered(g.getScreenHeight/2 - 50, "Press '1' to go back to menu", font40 )

    g.drawFPS()
  }

  override def dispose(): Unit = {
    super.dispose()

  }

}
