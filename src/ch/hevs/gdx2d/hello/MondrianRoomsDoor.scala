package ch.hevs.gdx2d.hello

import scala.util.Random

object MondrianRoomsDoor {

  def main(args: Array[String]): Unit = {
    val width = 50
    val height = 30
    val maxRooms = 20
    val minRoomSize = 3
    val maxRoomSize = 6
    val grid = Array.fill(height, width)(0)
    generateRooms(grid, maxRooms, minRoomSize, maxRoomSize)
    placeWallsAndDoors(grid)
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

    // Store the positions of rooms to ensure connectivity
    val rooms = scala.collection.mutable.ArrayBuffer((initialX, initialY, initialRoomWidth, initialRoomHeight))

    // Try to place rooms until maxRooms or coverage goal is reached
    val coverageGoal = (grid.length * grid(0).length) * 0.8 // Target 80% coverage
    var attempts = 0

    while (currentRoom <= maxRooms && getCoverage(grid) < coverageGoal && attempts < 1000) {
      val roomWidth = rand.nextInt(maxRoomSize - minRoomSize + 1) + minRoomSize
      val roomHeight = rand.nextInt(maxRoomSize - minRoomSize + 1) + minRoomSize
      val (startX, startY) = findAdjacentPosition(grid, roomWidth, roomHeight)

      if (startX != -1 && startY != -1 && canPlaceRoom(grid, startX, startY, roomWidth, roomHeight)) {
        fillRegion(grid, startX, startY, startX + roomWidth, startY + roomHeight, currentRoom)
        rooms.append((startX, startY, roomWidth, roomHeight))
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
      val startX = rand.nextInt(width - roomWidth - 2) + 1 // Ensure there is space for walls
      val startY = rand.nextInt(height - roomHeight - 2) + 1 // Ensure there is space for walls
      if (isAdjacentToRoom(grid, startX, startY, roomWidth, roomHeight) && canPlaceRoom(grid, startX, startY, roomWidth, roomHeight)) {
        return (startX, startY)
      }
    }
    (-1, -1) // If no valid position is found
  }

  def isAdjacentToRoom(grid: Array[Array[Int]], startX: Int, startY: Int, width: Int, height: Int): Boolean = {
    val directions = List((1, 0), (-1, 0), (0, 1), (0, -1))
    for (x <- startX until startX + width; y <- startY until startY + height) {
      for ((dx, dy) <- directions) {
        val nx = x + dx
        val ny = y + dy
        if (nx >= 0 && nx < grid(0).length && ny >= 0 && ny < grid.length && grid(ny)(nx) > 0 && grid(ny)(nx) < 99) {
          return true
        }
      }
    }
    false
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

  def placeWallsAndDoors(grid: Array[Array[Int]]): Unit = {
    placeWalls(grid)
    placeDoors(grid)
  }

  def placeWalls(grid: Array[Array[Int]]): Unit = {
    for (y <- grid.indices; x <- grid(y).indices) {
      if (grid(y)(x) == 0 && hasAdjacentRoom(grid, x, y)) {
        grid(y)(x) = 99
      }
    }
  }

  def placeDoors(grid: Array[Array[Int]]): Unit = {
    val doorPlaced = scala.collection.mutable.Set[(Int, Int)]()
    for (y <- grid.indices; x <- grid(y).indices) {
      if (grid(y)(x) == 99 && hasTwoAdjacentRooms(grid, x, y) && !doorPlaced.contains((x, y))) {
        grid(y)(x) = 80 // Use 80 to represent a door 'P' for easier distinction in the code
        doorPlaced.add((x, y))
      }
    }
  }

  def hasAdjacentRoom(grid: Array[Array[Int]], x: Int, y: Int): Boolean = {
    val directions = List((1, 0), (-1, 0), (0, 1), (0, -1))
    directions.exists { case (dx, dy) =>
      val nx = x + dx
      val ny = y + dy
      nx >= 0 && nx < grid(0).length && ny >= 0 && ny < grid.length && grid(ny)(nx) > 0 && grid(ny)(nx) < 99
    }
  }

  def hasTwoAdjacentRooms(grid: Array[Array[Int]], x: Int, y: Int): Boolean = {
    val directions = List((1, 0), (-1, 0), (0, 1), (0, -1))
    val adjacentRooms = directions.collect {
      case (dx, dy) if isRoom(grid, x + dx, y + dy) => (x + dx, y + dy)
    }

    adjacentRooms.size == 2 && adjacentRooms.map { case (x, y) => grid(y)(x) }.toSet.size == 2
  }

  def isRoom(grid: Array[Array[Int]], x: Int, y: Int): Boolean = {
    x >= 0 && x < grid(0).length && y >= 0 && y < grid.length && grid(y)(x) > 0 && grid(y)(x) < 99
  }

  def getCoverage(grid: Array[Array[Int]]): Int = {
    grid.flatten.count(_ != 0)
  }

  def printGrid(grid: Array[Array[Int]]): Unit = {
    for (row <- grid) {
      println(row.map {
        case 99 => "##"
        case 80 => " P"
        case 0  => "  "
        case n  => f"$n%2d"
      }.mkString(" "))
    }
  }
}
