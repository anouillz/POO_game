package ch.hevs.gdx2d.game

import scala.util.Random

class Room(var roomGrid: Array[Array[Int]], var nb: Int) {

  //if Sprite in room -> room "lights" on
  var isActive: Boolean = false

}
