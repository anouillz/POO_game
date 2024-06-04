package ch.hevs.gdx2d.game

import ch.hevs.gdx2d.components.bitmaps.{BitmapImage, Spritesheet}
import ch.hevs.gdx2d.lib.GdxGraphics
import ch.hevs.gdx2d.lib.interfaces.DrawableObject
import com.badlogic.gdx.math.{Interpolation, Vector2}

class Hero extends Entity with DrawableObject {

  println("Hero created")

  var name: String = "Hero"
  var spriteFile: String = "data/images/lumberjack_sheet32.png"

  var textureX = 0
  var textureY = 1
  var speed = 1f
  var dt = 0f
  var currentFrame = 0
  var nFrames = 4

  private val SPRITE_WIDTH = 32
  private val SPRITE_HEIGHT = 32
  final val FRAME_TIME = 0.1f // Duration of each frame

  var ss: Spritesheet = _

  var lastPosition: Vector2 = _
  var newPosition: Vector2 = _
  var position: Vector2 = _


  private var move = false


  /**
   * Create the hero at the given start tile.
   * @param x Column
   * @param y Line
   */
  def this(x: Int, y: Int) = {
    this()
    this.position = new Vector2(SPRITE_WIDTH * x, SPRITE_HEIGHT * y)
    init()
  }

  /**
   * Create the hero at the start position
   * @param initialPosition Start position [px] on the map.
   */
  def this(initialPosition: Vector2) = {
    this()
    this.position = new Vector2(initialPosition)
    init()
  }

  private def init(): Unit = {
    lastPosition = new Vector2(position)
    newPosition = new Vector2(position)
    ss = new Spritesheet(spriteFile, SPRITE_WIDTH, SPRITE_HEIGHT)
  }

  /**
   * @return the current position of the hero on the map.
   */
  def getPosition: Vector2 = this.position

  /**
   * Update the position and the texture of the hero.
   * @param elapsedTime The time [s] elapsed since the last time which this method was called.
   */
  def animate(elapsedTime: Double): Unit = {
    val frameTime = FRAME_TIME / speed

    position = new Vector2(lastPosition)
    if (isMoving) {
      dt += elapsedTime.toFloat
      val alpha = (dt + frameTime * currentFrame) / (frameTime * nFrames)
      position.interpolate(newPosition, alpha, Interpolation.linear)
    } else {
      dt = 0
    }

    if (dt > frameTime) {
      dt -= frameTime
      currentFrame = (currentFrame + 1) % nFrames

      if (currentFrame == 0) {
        move = false
        lastPosition = new Vector2(newPosition)
        position = new Vector2(newPosition)
      }
    }
  }

  /**
   * @return True if the hero is actually doing a step.
   */
  def isMoving: Boolean = move

  /**
   * @param speed The new speed of the hero.
   */
  def setSpeed(speed: Float): Unit = {
    this.speed = speed
  }

  /**
   * Do a step on the given direction
   * @param direction The direction to go.
   */
  def go(direction: Hero.Direction.Value): Unit = {
    move = true
    direction match {
      case Hero.Direction.RIGHT => newPosition.add(SPRITE_WIDTH, 0)
      case Hero.Direction.LEFT => newPosition.add(-SPRITE_WIDTH, 0)
      case Hero.Direction.UP => newPosition.add(0, SPRITE_HEIGHT)
      case Hero.Direction.DOWN => newPosition.add(0, -SPRITE_HEIGHT)
      case _ =>
    }

    turn(direction)
  }

  /**
   * Turn the hero on the given direction without doing any step.
   * @param direction The direction to turn.
   */
  def turn(direction: Hero.Direction.Value): Unit = {
    direction match {
      case Hero.Direction.RIGHT => textureY = 2
      case Hero.Direction.LEFT => textureY = 1
      case Hero.Direction.UP => textureY = 3
      case Hero.Direction.DOWN => textureY = 0
      case _ =>
    }
  }

  /**
   * Draw the character on the graphic object.
   * @param g Graphic object.
   */
  override def draw(g: GdxGraphics): Unit = {
    g.draw(ss.sprites(textureY)(currentFrame), position.x, position.y)
  }


}

object Hero {

  object Direction extends Enumeration {
    type Direction = Value
    val UP, DOWN, RIGHT, LEFT, NULL = Value
  }

}
