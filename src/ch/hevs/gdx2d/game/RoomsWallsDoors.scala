package ch.hevs.gdx2d.game

import scala.collection.mutable.ArrayBuffer
import scala.util.Random

class RoomsWallsDoors {

  val width = 50
  val height = 30
  val maxRooms = 35
  val minRoomSize = 3
  val maxRoomSize = 9

  var grid = Array.fill(width, height)(0)

  //  generateRooms(grid)
  //  placeWalls(grid)
  //
  //  printGrid(grid)


  def generateRooms(grid: Array[Array[Int]]): Unit = {
    val rand = new Random()
    var currentRoom = 1

    val initialX = 2
    val initialY = 2
    val initialRoomWidth = rand.nextInt(maxRoomSize - minRoomSize + 1) + minRoomSize
    val initialRoomHeight = rand.nextInt(maxRoomSize - minRoomSize + 1) + minRoomSize
    fillRegion(grid, initialX, initialY, initialX + initialRoomWidth, initialY + initialRoomHeight, currentRoom)
    currentRoom += 1

    val rooms = ArrayBuffer((initialX, initialY, initialRoomWidth, initialRoomHeight))

    val coverageGoal = (grid.length * grid(0).length) * 0.8
    var attempts = 0

    while (currentRoom <= maxRooms && getCoverage(grid) < coverageGoal && attempts < 1000) {
      val roomWidth = rand.nextInt(maxRoomSize - minRoomSize + 1) + minRoomSize
      val roomHeight = rand.nextInt(maxRoomSize - minRoomSize + 1) + minRoomSize
      val (startX, startY) = findAdjacentPosition(grid, roomWidth, roomHeight, rooms)

      if (startX != -1 && startY != -1) {
        fillRegion(grid, startX, startY, startX + roomWidth, startY + roomHeight, currentRoom)
        rooms.append((startX, startY, roomWidth, roomHeight))
        currentRoom += 1
        attempts = 0
      } else {
        attempts += 1
      }
    }

    for (i <- 0 until rooms.length) {
      for (j <- i + 1 until rooms.length) {
        val (startX1, startY1, width1, height1) = rooms(i)
        val (startX2, startY2, width2, height2) = rooms(j)

        // Check if rooms are adjacent and place a door between them
        if (roomsAreAdjacent(startX1, startY1, width1, height1, startX2, startY2, width2, height2)) {
          placeDoorBetweenRooms(grid, startX1, startY1, width1, height1, startX2, startY2, width2, height2)
        }
      }
    }
  }

  def findAdjacentPosition(grid: Array[Array[Int]], roomWidth: Int, roomHeight: Int, rooms: ArrayBuffer[(Int, Int, Int, Int)]): (Int, Int) = {
    val rand = new Random()

    for ((startX, startY, width, height) <- rooms) {
      val directions = List((1, 0), (-1, 0), (0, 1), (0, -1))
      for ((dx, dy) <- directions) {
        val newStartX = startX + dx * (width + 1)
        val newStartY = startY + dy * (height + 1)
        if (canPlaceRoom(grid, newStartX, newStartY, roomWidth, roomHeight)) {
          return (newStartX, newStartY)
        }
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
      if (x < 0 || x >= grid(0).length || y < 0 || y >= grid.length || (grid(y)(x) != 0 && grid(y)(x) != 99)) return false
    }
    true
  }

  def fillRegion(grid: Array[Array[Int]], startX: Int, startY: Int, endX: Int, endY: Int, roomNumber: Int): Unit = {
    for (x <- (startX - 1) to (endX + 1); y <- (startY - 1) to (endY + 1)) {
      if (x >= 0 && x < grid(0).length && y >= 0 && y < grid.length) {
        if (x >= startX && x < endX && y >= startY && y < endY) {
          grid(y)(x) = roomNumber // Room
        } else {
          grid(y)(x) = 99 // Wall
        }
      }
    }
  }

  def placeDoorBetweenRooms(grid: Array[Array[Int]], startX1: Int, startY1: Int, width1: Int, height1: Int, startX2: Int, startY2: Int, width2: Int, height2: Int): Unit = {
    if (startX1 + width1 == startX2) {
      // Rooms are adjacent horizontally, place door on the left side of the second room
      placeDoor(grid, startX2, startY2 + height2 / 2, 1, 1)
    } else if (startX2 + width2 == startX1) {
      // Rooms are adjacent horizontally, place door on the right side of the first room
      placeDoor(grid, startX1, startY1 + height1 / 2, 1, 1)
    } else if (startY1 + height1 == startY2) {
      // Rooms are adjacent vertically, place door on the top side of the second room
      placeDoor(grid, startX2 + width2 / 2, startY2, 1, 1)
    } else if (startY2 + height2 == startY1) {
      // Rooms are adjacent vertically, place door on the bottom side of the first room
      placeDoor(grid, startX1 + width1 / 2, startY1, 1, 1)
    }
  }


  def placeDoor(grid: Array[Array[Int]], startX: Int, startY: Int, roomWidth: Int, roomHeight: Int): Unit = {
    val rand = new Random()
    val doorSide = rand.nextInt(4)
    doorSide match {
      case 0 =>
        if (isAdjacentToRoom(grid, startX + rand.nextInt(roomWidth), startY - 1, 1, 1)) {
          println(s"Door placed at position (${startX + rand.nextInt(roomWidth)}, ${startY - 1})")
          grid(startY - 1)(startX + rand.nextInt(roomWidth)) = 98
        }
      case 1 =>
        if (isAdjacentToRoom(grid, startX + roomWidth, startY + rand.nextInt(roomHeight), 1, 1)) {
          println(s"Door placed at position (${startX + roomWidth}, ${startY + rand.nextInt(roomHeight)})")
          grid(startY + rand.nextInt(roomHeight))(startX + roomWidth) = 98
        }
      case 2 =>
        if (isAdjacentToRoom(grid, startX + rand.nextInt(roomWidth), startY + roomHeight, 1, 1)) {
          println(s"Door placed at position (${startX + rand.nextInt(roomWidth)}, ${startY + roomHeight})")
          grid(startY + roomHeight)(startX + rand.nextInt(roomWidth)) = 98
        }
      case 3 =>
        if (isAdjacentToRoom(grid, startX - 1, startY + rand.nextInt(roomHeight), 1, 1)) {
          println(s"Door placed at position (${startX - 1}, ${startY + rand.nextInt(roomHeight)})")
          grid(startY + rand.nextInt(roomHeight))(startX - 1) = 98
        }
    }
  }

  def placeWalls(grid: Array[Array[Int]]): Unit = {
    for (y <- grid.indices; x <- grid(y).indices) {
      if (grid(y)(x) == 0 && hasAdjacentRoom(grid, x, y)) {
        grid(y)(x) = 99
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

  def roomsAreAdjacent(startX1: Int, startY1: Int, width1: Int, height1: Int, startX2: Int, startY2: Int, width2: Int, height2: Int): Boolean = {
    val endX1 = startX1 + width1
    val endY1 = startY1 + height1
    val endX2 = startX2 + width2
    val endY2 = startY2 + height2

    (startX1 <= endX2 && endX1 >= startX2 && startY1 <= endY2 && endY1 >= startY2)
  }

  def getCoverage(grid: Array[Array[Int]]): Int = {
    grid.flatten.count(_ != 0)
  }

  def printGrid(grid: Array[Array[Int]]): Unit = {
    for (row <- grid) {
      println(row.map {
        case 99 => "##"
        case 0  => "  "
        case n  => f"$n%2d"
      }.mkString(" "))
    }
  }
}