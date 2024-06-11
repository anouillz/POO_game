package ch.hevs.gdx2d.game

import ch.hevs.gdx2d.components.screen_management.RenderingScreen
import ch.hevs.gdx2d.desktop.PortableApplication
import ch.hevs.gdx2d.game.screens.{EndingScreen, MenuScreen}
import ch.hevs.gdx2d.lib.{GdxGraphics, ScreenManager}
import ch.hevs.gdx2d.lib.utils.Logger
import com.badlogic.gdx.{Gdx, Input}
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.scenes.scene2d.{InputEvent, Stage}
import com.badlogic.gdx.scenes.scene2d.ui.{Skin, TextButton}
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener

import scala.collection.mutable

class Main extends PortableApplication(700,700){

  // key management
  private val keyStatus: mutable.Map[Int, Boolean] = mutable.TreeMap[Int, Boolean]()

  // init keys status
  keyStatus.put(Input.Keys.UP, false)
  keyStatus.put(Input.Keys.DOWN, false)
  keyStatus.put(Input.Keys.LEFT, false)
  keyStatus.put(Input.Keys.RIGHT, false)


  var s: ScreenManager = new ScreenManager

  override def onInit(): Unit = {

    setTitle("Chicago - what's yours is mine")
    s.registerScreen(classOf[MenuScreen])
    s.registerScreen(classOf[GameScreen])
    s.registerScreen(classOf[EndingScreen])


  }

  override def onGraphicRender(g: GdxGraphics): Unit = {
    s.render(g)

  }

  override def onClick(x: Int, y: Int, button: Int): Unit = {
    // Delegate the click to the child class
    s.getActiveScreen.onClick(x, y, button)
  }


  // Manage keyboard events
  override def onKeyUp(keycode: Int): Unit = {
    //delegate key management to current screen
    val activeScreen: RenderingScreen = s.getActiveScreen

    if(activeScreen != null){
      s.getActiveScreen.onKeyUp(keycode)
      keyStatus.put(keycode, false)
    }

    super.onKeyUp(keycode)
    keyStatus.put(keycode, false)
  }

  override def onKeyDown(keycode: Int): Unit = {
    //delegate key management to current screen
    val activeScreen: RenderingScreen = s.getActiveScreen

    if(activeScreen != null){
      s.getActiveScreen.onKeyDown(keycode)
      keyStatus.put(keycode, true)
    }

    if (keycode == Input.Keys.ENTER) {
      keyStatus.put(keycode, true)
      s.transitionTo(1, ScreenManager.TransactionType.SMOOTH)
    }

  }

}

object Main {
  def main(args: Array[String]): Unit = {
    new Main
  }
}
