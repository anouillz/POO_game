package ch.hevs.gdx2d.game

import ch.hevs.gdx2d.components.bitmaps.Spritesheet
import ch.hevs.gdx2d.lib._
import ch.hevs.gdx2d.lib.interfaces.DrawableObject
import com.badlogic.gdx.math.{Interpolation, Vector2}

class Hero extends Entity with DrawableObject {

  override var name: String = "Hero"
  override var spriteFile: String = "data/images/lumberjack_sheet32.png"
  override var spriteSheet: Spritesheet = new Spritesheet(spriteFile, SPRITEWIDTH, SPRITEHEIGHT)

  override var speed: Float = 1
  override var textureX: Int = 0
  override var textureY: Int = 1
  override var dt: Float = 0
  override var currentFrame: Int = 0
  override var nFrames: Int = 4

  override var position: Vector2 = new Vector2(0,0)
  override var lastPosition: Vector2 = new Vector2(0,0)
  override var newPosition: Vector2 = new Vector2(0,0)

  //false by default
  var move: Boolean = false


  /**
   * Puts the hero at this given tile
   * @param x Column
   * @param y Row
   */
  def this(x: Int, y: Int) = {
    this()
    this.position = new Vector2(SPRITEWIDTH*x, SPRITEHEIGHT*y)
  }

  /**
   * Create Hero at inital position
   * @param initialPosition Start position on the map
   */
  def this(initialPosition: Vector2) = {
    this()
    this.lastPosition = new Vector2(initialPosition.x, initialPosition.y)
    this.newPosition = new Vector2(initialPosition.x, initialPosition.y)
    this.position = new Vector2(initialPosition.x, initialPosition.y)

  }

  /**
   * @return the current position on the map
   */
  def getPosition(): Vector2 ={
    return this.position
  }

  override def animate(elapsedTime: Double): Unit = {
    var frameTime: Float = FRAMETIME / speed
    position = new Vector2(lastPosition)

    if(isMoving()) {
      dt += elapsedTime
      //varie entre 0 et 1, 0: hero est à la position actuelle et 1: Hero est à la nouvelle position
      val alpha = (dt + frameTime * currentFrame) / (frameTime * nFrames)
      // interpolation.linear crée un mouvement lineaire uniforme entre les deux position
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

  override def isMoving(): Boolean = return move


  override def draw(gdxGraphics: GdxGraphics): Unit = ???
}