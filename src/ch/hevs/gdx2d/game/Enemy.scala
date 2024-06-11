package ch.hevs.gdx2d.game

import ch.hevs.gdx2d.components.bitmaps.{BitmapImage, Spritesheet}
import ch.hevs.gdx2d.game.Enemy.enemyArray
import ch.hevs.gdx2d.lib.GdxGraphics
import ch.hevs.gdx2d.lib.interfaces.DrawableObject
import com.badlogic.gdx.math.{Interpolation, Vector2}

import scala.collection.mutable.ArrayBuffer

class Enemy private extends Entity with DrawableObject {
  var name: String = "Enemy"
  var spriteFile: String = "data/images/lumberjack_sheet32.png"

  var textureX = 0
  var textureY = 1
  var speed = 1f
  var dt = 0f
  var currentFrame = 0
  var nFrames = 4

  var ss: Spritesheet = _

  var lastPosition: Vector2 = _
  var newPosition: Vector2 = _
  var position: Vector2 = _

  private var move = false

  /**
   * Create the enemy at the given start tile.
   * @param x Column
   * @param y Line
   */
  def this(x: Int, y: Int) = {
    this()
    this.position = new Vector2(SPRITEWIDTH * x, SPRITEHEIGHT * y)
    init()
  }

  /**
   * Create the enemy at the start position
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
    ss = new Spritesheet(spriteFile, SPRITEWIDTH, SPRITEHEIGHT)
  }

  /**
   * @return the current position of the enemy on the map.
   */
  def getPosition: Vector2 = this.position


  /**
   * Update the position and the texture of the enemy.
   * @param elapsedTime The time [s] elapsed since the last time which this method was called.
   */
  def animate(elapsedTime: Double): Unit = {
    val frameTime = FRAMETIME / speed

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
   * @return True if the enemy is actually doing a step.
   */
  def isMoving: Boolean = move

  /**
   * @param speed The new speed of the enemy.
   */
  def setSpeed(speed: Float): Unit = {
    this.speed = speed
  }

  /**
   * Do a step on the given direction
   * @param direction The direction to go.
   */
  def go(direction: Enemy.Direction.Value): Unit = {
    move = true
    direction match {
      case Enemy.Direction.RIGHT => newPosition.add(SPRITEWIDTH, 0)
      case Enemy.Direction.LEFT => newPosition.add(-SPRITEWIDTH, 0)
      case Enemy.Direction.UP => newPosition.add(0, SPRITEHEIGHT)
      case Enemy.Direction.DOWN => newPosition.add(0, -SPRITEHEIGHT)
      case _ =>
    }

    turn(direction)
  }

  /**
   * Turn the enemy on the given direction without doing any step.
   * @param direction The direction to turn.
   */
  def turn(direction: Enemy.Direction.Value): Unit = {
    direction match {
      case Enemy.Direction.RIGHT => textureY = 2
      case Enemy.Direction.LEFT => textureY = 1
      case Enemy.Direction.UP => textureY = 3
      case Enemy.Direction.DOWN => textureY = 0
      case _ =>
    }
  }

  def areadetection(direction : Enemy.Direction.Value): Vector2= {
    var offset : Vector2 = new Vector2(0,0)
    direction match {
      case Enemy.Direction.RIGHT => offset.set(1,0)
      case Enemy.Direction.LEFT => offset.set(-1,0)
      case Enemy.Direction.UP => offset.set(0,1)
      case Enemy.Direction.DOWN => offset.set(0,-1)
      case _ =>
    }
    return offset
  }

  /**
   * Draw the character on the graphic object.
   * @param g Graphic object.
   */
  override def draw(g: GdxGraphics): Unit = {
    g.draw(ss.sprites(textureY)(currentFrame), position.x, position.y)
  }
}

object Enemy {

  /**
   * Factory method for an enemy, which is automatically added to the enemies list enemyArray
   * @param x
   * @param y
   * @return the enemy created
   */
  def genEnemy(x: Int, y : Int) : Enemy = {
    val e = new Enemy(x, y)
    enemyArray += e
    return e
  }

  val enemyArray: ArrayBuffer[Enemy] = new ArrayBuffer()

  object Direction extends Enumeration {
    type Direction = Value
    val UP, DOWN, RIGHT, LEFT, NULL = Value
  }

}
