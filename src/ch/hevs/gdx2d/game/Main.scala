package ch.hevs.gdx2d.game

import ch.hevs.gdx2d.components.screen_management.RenderingScreen
import ch.hevs.gdx2d.desktop.PortableApplication
import ch.hevs.gdx2d.game.screens.{MenuScreen, RulesScreen, lostScreen, wonScreen}
import ch.hevs.gdx2d.lib.{GdxGraphics, ScreenManager}
import com.badlogic.gdx.{Gdx, Input}

import scala.collection.mutable

class Main extends PortableApplication(1920,1080){

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
    s.registerScreen(classOf[lostScreen])
    s.registerScreen(classOf[wonScreen])
    s.registerScreen(classOf[RulesScreen])

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

    if (keycode == Input.Keys.NUM_1) {
      keyStatus.put(keycode, true)
      s.transitionTo(0, ScreenManager.TransactionType.SLIDE)
    }

    if (keycode == Input.Keys.R) {
      keyStatus.put(keycode, true)
      s.transitionTo(4, ScreenManager.TransactionType.SMOOTH)
    }

  }

}

object Main {
  // instance to be able to access main from GameScreen
  var instance: Main = _
  def main(args: Array[String]): Unit = {
    instance = new Main
  }
}
