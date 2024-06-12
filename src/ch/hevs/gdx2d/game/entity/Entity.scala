package ch.hevs.gdx2d.game.entity

import ch.hevs.gdx2d.components.bitmaps._
import com.badlogic.gdx.math.Vector2

trait Entity {

  var name: String
  var spriteFile: String
  var ss: Spritesheet
  var speed: Float
  var textureX: Int
  var textureY: Int
  var position: Vector2
  var lastPosition: Vector2
  var newPosition: Vector2
  var dt: Float
  var currentFrame: Int
  var nFrames: Int

  val SPRITEWIDTH: Int = 32
  val SPRITEHEIGHT: Int = 32
  val FRAMETIME: Float = 0.1f


  def animate(elsapsedTime: Double): Unit

  def isMoving: Boolean



}

