package ch.hevs.gdx2d.hello
import scala.util.Random

object MondrianRooms {

  def main(args: Array[String]): Unit = {
    val width = 50
    val height = 30
    val maxRooms = 20
    val minRoomSize = 3
    val maxRoomSize = 6
    val grid = Array.fill(height, width)(0)
    generateRooms(grid, maxRooms, minRoomSize, maxRoomSize)
    printGrid(grid)
  }

  def generateRooms(grid: Array[Array[Int]], maxRooms: Int, minRoomSize: Int, maxRoomSize: Int): Unit = {
    val rand = new Random()
    var currentRoom = 1

    // Place the first room at the fixed position (2,2)
    val initialX = 2
    val initialY = 2
    val initialRoomWidth = rand.nextInt(maxRoomSize - minRoomSize + 1) + minRoomSize
    val initialRoomHeight = rand.nextInt(maxRoomSize - minRoomSize + 1) + minRoomSize
    fillRegion(grid, initialX, initialY, initialX + initialRoomWidth, initialY + initialRoomHeight, currentRoom)
    currentRoom += 1

    // Try to place rooms until maxRooms or coverage goal is reached
    val coverageGoal = (grid.length * grid(0).length) * 0.8 // Target 80% coverage
    var attempts = 0

    while (currentRoom <= maxRooms && getCoverage(grid) < coverageGoal && attempts < 1000) {
      val roomWidth = rand.nextInt(maxRoomSize - minRoomSize + 1) + minRoomSize
      val roomHeight = rand.nextInt(maxRoomSize - minRoomSize + 1) + minRoomSize
      val (startX, startY) = findAdjacentPosition(grid, roomWidth, roomHeight)

      if (startX != -1 && startY != -1 && canPlaceRoom(grid, startX, startY, roomWidth, roomHeight)) {
        fillRegion(grid, startX, startY, startX + roomWidth, startY + roomHeight, currentRoom)
        currentRoom += 1
        attempts = 0 // Reset attempts if successful
      } else {
        attempts += 1
      }
    }

    println(s"Placed $currentRoom rooms.")
  }

  def findAdjacentPosition(grid: Array[Array[Int]], roomWidth: Int, roomHeight: Int): (Int, Int) = {
    val rand = new Random()
    val width = grid(0).length
    val height = grid.length

    for (_ <- 0 until 100) { // Try up to 100 times to find a valid position
      val x = rand.nextInt(width)
      val y = rand.nextInt(height)
      if (grid(y)(x) != 0) {
        val directions = List((1, 0), (-1, 0), (0, 1), (0, -1))
        val shuffledDirections = rand.shuffle(directions)
        for ((dx, dy) <- shuffledDirections) {
          val startX = x + dx
          val startY = y + dy
          if (startX >= 0 && startX + roomWidth < width && startY >= 0 && startY + roomHeight < height &&
            canPlaceRoom(grid, startX, startY, roomWidth, roomHeight)) {
            return (startX, startY)
          }
        }
      }
    }
    (-1, -1) // If no valid position is found
  }

  def canPlaceRoom(grid: Array[Array[Int]], startX: Int, startY: Int, width: Int, height: Int): Boolean = {
    for (x <- startX until (startX + width); y <- startY until (startY + height)) {
      if (x < 0 || x >= grid(0).length || y < 0 || y >= grid.length || grid(y)(x) != 0) return false
    }
    true
  }

  def fillRegion(grid: Array[Array[Int]], startX: Int, startY: Int, endX: Int, endY: Int, roomNumber: Int): Unit = {
    for (x <- startX until endX; y <- startY until endY) {
      grid(y)(x) = roomNumber
    }
  }

  def getCoverage(grid: Array[Array[Int]]): Int = {
    grid.flatten.count(_ != 0)
  }

  def printGrid(grid: Array[Array[Int]]): Unit = {
    for (row <- grid) {
      println(row.map(cell => f"$cell%2d").mkString(" "))
    }
  }
}
