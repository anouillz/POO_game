package ch.hevs.gdx2d.hello

import box2dLight.{ConeLight, RayHandler}
import com.badlogic.gdx.{Gdx, Input}
import com.badlogic.gdx.math.{Interpolation, Vector2}
import ch.hevs.gdx2d.components.bitmaps.BitmapImage
import ch.hevs.gdx2d.components.physics.utils.PhysicsConstants
import ch.hevs.gdx2d.lib.GdxGraphics
import ch.hevs.gdx2d.desktop.PortableApplication
import ch.hevs.gdx2d.lib.physics.PhysicsWorld
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.maps.tiled.TiledMapTile

import java.util
import java.util.{Map, TreeMap}


/**
 * Hello World demo in Scala
 *
 * @author Pierre-André Mudry (mui)
 * @version 1.0
 */
object HelloWorldScala {


  def main(args: Array[String]): Unit = {
    new HelloWorldScala(1920, 1080)
  }
}

class HelloWorldScala(var width: Int, var height: Int) extends PortableApplication(width, height) {
  private val keyStatus: util.Map[Integer, Boolean] = new util.TreeMap[Integer, Boolean]

  var hero: Hero = null

  override def onInit(): Unit = {
    hero = new Hero(20, 20)

    keyStatus.put(Input.Keys.UP, false)
    keyStatus.put(Input.Keys.DOWN, false)
    keyStatus.put(Input.Keys.LEFT, false)
    keyStatus.put(Input.Keys.RIGHT, false)
  }

  /**
   * Some animation related variables
   */

  /**
   * This method is called periodically by the engine
   *
   * @param g
   */
  override def onGraphicRender(g: GdxGraphics): Unit = {
    g.clear()

    manageHero()

    //hero.animate((Gdx.graphics.getDeltaTime))
    hero.draw(g)
  }

  private def manageHero(): Unit = {
    // Do nothing if hero is already moving
    // Compute direction and next cell
    var nextCell: TiledMapTile = null
    var goalDirection = Hero.Direction.NULL
    if (keyStatus.get(Input.Keys.RIGHT) && keyStatus.get(Input.Keys.UP)) {
      goalDirection = Hero.Direction.UP_RIGHT
    }
    else if (keyStatus.get(Input.Keys.LEFT) && keyStatus.get(Input.Keys.UP)) {
      goalDirection = Hero.Direction.UP_LEFT
    }
    else if (keyStatus.get(Input.Keys.RIGHT) && keyStatus.get(Input.Keys.DOWN)) {
      goalDirection = Hero.Direction.DOWN_RIGHT
    }
    else if (keyStatus.get(Input.Keys.LEFT) && keyStatus.get(Input.Keys.DOWN)) {
      goalDirection = Hero.Direction.DOWN_LEFT
    }
    else if (keyStatus.get(Input.Keys.RIGHT)) {
      goalDirection = Hero.Direction.RIGHT
    }
    else if (keyStatus.get(Input.Keys.RIGHT)) {
      goalDirection = Hero.Direction.RIGHT
    }
    else if (keyStatus.get(Input.Keys.RIGHT)) {
      goalDirection = Hero.Direction.RIGHT
    }
    else if (keyStatus.get(Input.Keys.RIGHT)) {
      goalDirection = Hero.Direction.RIGHT
    }
    else if (keyStatus.get(Input.Keys.LEFT)) {
      goalDirection = Hero.Direction.LEFT
    }
    else if (keyStatus.get(Input.Keys.UP)) {
      goalDirection = Hero.Direction.UP
    }
    else if (keyStatus.get(Input.Keys.DOWN)) {
      goalDirection = Hero.Direction.DOWN
    }
    hero.go(goalDirection)

  }

  override def onKeyUp(keycode: Int): Unit = {
    super.onKeyUp(keycode)
    keyStatus.put(keycode, false)
  }

  override def onKeyDown(keycode: Int): Unit = {
    super.onKeyDown(keycode)
    keyStatus.put(keycode, true)
  }


  /**
   * Compute time percentage for making a looping animation
   *
   * @return the current normalized time
   */

}
