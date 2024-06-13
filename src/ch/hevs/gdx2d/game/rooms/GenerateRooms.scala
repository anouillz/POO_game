package ch.hevs.gdx2d.game.rooms

import scala.collection.mutable.ArrayBuffer
import scala.util.Random

class GenerateRooms {

  //width and height of our grid
  val width: Int = 50
  val height: Int = 30

  //grid characterisitcs
  val maxRooms: Int = 43
  val minRoomSize: Int = 3
  val maxRoomSize: Int = 6

  var grid: Array[Array[Int]] = Array.fill(width, height)(0)

  //list that will contain all of our rooms
  var rooms: ArrayBuffer[Room] = ArrayBuffer.empty

  /**
   * Main method to generate the grid
   * Place first room with start coordinates (2,2)
   * continue placing rooms until we can't no more OR until we've covered enough of the map
   * @param grid
   */
  def generateRooms(grid: Array[Array[Int]]): Unit = {
    val rand = new Random()
    var currentRoom = 1

    //place the first room at the fixed position (2,2)
    val initialX = 2
    val initialY = 2
    //generate width and height randomly
    val initialRoomWidth = rand.nextInt(maxRoomSize - minRoomSize + 1) + minRoomSize
    val initialRoomHeight = rand.nextInt(maxRoomSize - minRoomSize + 1) + minRoomSize

    //fill the grid with the number of the room and add the room to the room array
    fillRegion(grid, initialX, initialY, initialX + initialRoomWidth, initialY + initialRoomHeight, currentRoom)
    val roomGrid: Array[Array[Int]] = Array.fill(initialRoomWidth, initialRoomHeight)(currentRoom)
    rooms.addOne(new Room(roomGrid, currentRoom))
    currentRoom += 1

    val coverageGoal = (grid.length * grid(0).length) * 0.8 // Target 80% coverage
    var attempts = 0

    //try to place rooms until maxRooms or coverage goal is reached
    while (currentRoom <= maxRooms && getCoverage(grid) < coverageGoal && attempts < 1000) {
      val roomWidth = rand.nextInt(maxRoomSize - minRoomSize + 1) + minRoomSize
      val roomHeight = rand.nextInt(maxRoomSize - minRoomSize + 1) + minRoomSize
      //find a position adjacent to an already existing room
      val (startX, startY) = findAdjacentPosition(grid, roomWidth, roomHeight)

      //if startX and startY = -1 it means no adjacentPosition was found
      if (startX != -1 && startY != -1 && canPlaceRoom(grid, startX, startY, roomWidth, roomHeight)) {
        //fill room with room's number and add it to the rooms array
        fillRegion(grid, startX, startY, startX + roomWidth, startY + roomHeight, currentRoom)
        val roomGrid = Array.fill(initialRoomWidth, initialRoomHeight)(currentRoom)
        rooms.addOne(new Room(roomGrid, currentRoom))

        //if we were able to place this room, we can try the next one
        currentRoom += 1
        attempts = 0 // reset attempts if successful
      } else {
        //increase attempts to try with another random width and height
        attempts += 1
      }

      //if we arrive at the last room
      if (currentRoom == maxRooms){
        val lastRoomWidth: Int = 5
        val lastRoomHeight: Int = 3
        val(lastRoomStartX, lastRoomStartY) = findAdjacentPosition(grid, lastRoomWidth, lastRoomHeight)

        if(lastRoomStartX != -1 && lastRoomStartY != -1 && canPlaceRoom(grid, lastRoomStartX, lastRoomStartY, lastRoomWidth, lastRoomHeight)) {
          fillRegion(grid, lastRoomStartX, lastRoomStartY, lastRoomStartX + lastRoomWidth, lastRoomStartY + lastRoomHeight, currentRoom)
          val roomGrid = Array.fill(lastRoomWidth, lastRoomHeight)(currentRoom)
          rooms.addOne(new Room(roomGrid, currentRoom))
          currentRoom += 1
          attempts = 0
        } else {
          attempts += 1
        }
      }
    }

    println(s"Placed $currentRoom rooms.")
  }

  /**
   * Find a position that is adjacent to an already existing room
   * First coordinate of the new room
   * @param grid
   * @param roomWidth
   * @param roomHeight
   * @return
   */
  def findAdjacentPosition(grid: Array[Array[Int]], roomWidth: Int, roomHeight: Int): (Int, Int) = {
    val rand = new Random()
    val width = grid(0).length
    val height = grid.length

    //try up to 100 times to find a valid position
    for (_: Int <- 0 until 100) {
      //make sure there is space to put walls
      val startX = rand.nextInt(width - roomWidth - 2) + 1
      val startY = rand.nextInt(height - roomHeight - 2) + 1
      if (isAdjacentToRoom(grid, startX, startY, roomWidth, roomHeight) && canPlaceRoom(grid, startX, startY, roomWidth, roomHeight)) {
        return (startX, startY)
      }
    }
    //if no valid position is found
    (-1, -1)
  }

  /**
   * Verifies if position given is adjacent to an existing room
   * @param grid
   * @param startX
   * @param startY
   * @param width - of room to be placed
   * @param height - of room to be placed
   * @return
   */
  def isAdjacentToRoom(grid: Array[Array[Int]], startX: Int, startY: Int, width: Int, height: Int): Boolean = {
    //possible directions
    val directions: List[(Int, Int)] = List((1, 0), (-1, 0), (0, 1), (0, -1))

    //checks in all directions if one cell is occupied
    for (x: Int <- startX until startX + width) {
      for (y: Int <- startY until startY + height) {
        for ((dx, dy) <- directions) {
          val nx: Int = x + dx
          val ny: Int = y + dy
          //if one of the adjacent cells is < 99, thus is a placed room, return true
          if (nx >= 0 && nx < grid(0).length && ny >= 0 && ny < grid.length && grid(ny)(nx) > 0 && grid(ny)(nx) < 99) {
            return true
          }
        }
      }
    }
    false
  }

  def canPlaceRoom(grid: Array[Array[Int]], startX: Int, startY: Int, width: Int, height: Int): Boolean = {
    //if coordinates out of border, cant place a room
    for (x: Int <- startX until (startX + width)) {
      for(y: Int <- startY until (startY + height)){
        if (x < 0 || x >= grid(0).length || y < 0 || y >= grid.length || grid(y)(x) != 0){
          return false
        }
      }
    }
    true
  }

  def fillRegion(grid: Array[Array[Int]], startX: Int, startY: Int, endX: Int, endY: Int, roomNumber: Int): Unit = {
    for (x <- startX until endX) {
      for (y <- startY until endY) {
        grid(y)(x) = roomNumber
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
      val nx: Int = x + dx
      val ny: Int = y + dy
      nx >= 0 && nx < grid(0).length && ny >= 0 && ny < grid.length && grid(ny)(nx) > 0 && grid(ny)(nx) < 99
    }
  }

  def getCoverage(grid: Array[Array[Int]]): Int = {
    //creates a 1D array
    val flattenedGrid = grid.flatten
    //get number of 0 in the current grid
    val nonZeroCellsCount = flattenedGrid.count(_ != 0)

    nonZeroCellsCount

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
