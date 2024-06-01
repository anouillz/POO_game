package ch.hevs.gdx2d.hello
import scala.util.Random

class MazeWithRooms(val width: Int, val height: Int, val roomCount: Int, val roomMinSize: Int, val roomMaxSize: Int) {
  require(width % 2 != 0 && height % 2 != 0, "Width and height must be odd numbers")

  private var rand = new Random()

  private val maze = Array.fill(width, height)(1) // 1 for walls, 0 for paths
  private val start = maze(rand.between(1, width - 1))(rand.between(1, height - 1))

  def generatePath(): Unit = {

  }

  def addRooms(): Unit = {

  }

  def getPoint(start: (Int, Int), d: Int): (Int, Int) = {
    ???
  }

}

object MazeWithRoomsTest extends App {
  val generator = new MazeWithRooms(21, 21, 5, 3, 5)

}
