package ch.hevs.gdx2d.game.screens

import ch.hevs.gdx2d.components.screen_management.RenderingScreen
import ch.hevs.gdx2d.lib.GdxGraphics
import ch.hevs.gdx2d.lib.utils.Logger
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.files.FileHandle
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator.FreeTypeFontParameter
import com.badlogic.gdx.scenes.scene2d.{InputEvent, Stage}
import com.badlogic.gdx.scenes.scene2d.ui.{Skin, TextButton, TextField}
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener

class MenuScreen extends RenderingScreen{


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

    g.drawStringCentered(300, "Press 'Enter' to continue", font40)
    g.drawStringCentered(500, "Chicago - What's yours is mine", font40 )

    g.drawSchoolLogo()
    g.drawFPS()
  }


  def onDispose(): Unit = {
    font40.dispose()
  }
}
