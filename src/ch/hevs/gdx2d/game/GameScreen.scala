package ch.hevs.gdx2d.game

import ch.hevs.gdx2d.desktop.PortableApplication
import ch.hevs.gdx2d.lib.GdxGraphics
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer.Cell
import com.badlogic.gdx.maps.tiled._
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer
import com.badlogic.gdx.maps.tiled.tiles.StaticTiledMapTile
import com.badlogic.gdx.math.Vector2

import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer
import scala.util.Random
import scala.util.control.Breaks.break



class GameScreen(var width: Int, var height: Int) extends PortableApplication(width, height) {

  // key management
  private val keyStatus: mutable.Map[Int, Boolean] = mutable.TreeMap[Int, Boolean]()

  // characters
  private var hero: Hero = _
  private var enemy1: Enemy = _
  private var enemy2: Enemy = _

  var gridPerso: generateRooms = new generateRooms

  //Create the grid and rooms necessary to create the map
  var gridMap = gridPerso.grid
  gridPerso.generateRooms(gridMap)
  var rooms: ArrayBuffer[Room] = gridPerso.rooms
  gridPerso.placeWalls(gridMap)
  gridPerso.printGrid(gridMap)

  //Portal Tiles:
  var portalCoordinates: ArrayBuffer[(Int, Int)] = ArrayBuffer.empty


  // tiles management
  private var tiledMap: TiledMap = _
  private var tiledMapRenderer: TiledMapRenderer = _
  private var tiledLayer1: TiledMapTileLayer = _
  private var tiledLayer2: TiledMapTileLayer = _
  private var zoom: Float = _


  override def onInit(): Unit = {

    // Create hero
    hero = new Hero(2, 2)
    enemy1 = new Enemy (3,3)
    enemy2 = new Enemy (10,11)
    // Set initial zoom
    zoom = 2f

    // init keys status
    keyStatus.put(Input.Keys.UP, false)
    keyStatus.put(Input.Keys.DOWN, false)
    keyStatus.put(Input.Keys.LEFT, false)
    keyStatus.put(Input.Keys.RIGHT, false)

    // create map
    try {
      //Original map with different tiles used to make our custom map
      val exampleMap: TiledMap = new TmxMapLoader().load("data/maps/mapTest1.tmx")

      //Custom map -> random each time
      tiledMap = createCustomMap(exampleMap)

      tiledMapRenderer = new OrthogonalTiledMapRenderer(tiledMap)

      val layers = tiledMap.getLayers
      tiledLayer1 = layers.get(0).asInstanceOf[TiledMapTileLayer]
      tiledLayer2 = layers.get(1).asInstanceOf[TiledMapTileLayer]

    } catch {
      case e: Exception => e.printStackTrace()
    }
  }

  override def onGraphicRender(g: GdxGraphics): Unit = {
    g.clear()

    // Hero activity
    manageHero()

    // End of game
    isEnd()

    // Camera follows the hero
    g.zoom(zoom)
    g.moveCamera(hero.getPosition.x, hero.getPosition.y, tiledLayer1.getWidth * tiledLayer1.getTileWidth, tiledLayer1.getHeight * tiledLayer1.getTileHeight)

    // Render the tilemap
    tiledMapRenderer.setView(g.getCamera)
    tiledMapRenderer.render()

    // Draw the hero
    hero.animate(Gdx.graphics.getDeltaTime)
    hero.draw(g)
    enemy1.animate(Gdx.graphics.getDeltaTime)
    enemy1.draw(g)
    enemy2.animate(Gdx.graphics.getDeltaTime)
    enemy2.draw(g)

    //Optional
    g.drawFPS()
    g.drawSchoolLogo()
  }

  /**
   * exemple : getTile(myPosition,0,1) get the tile over myPosition
   *
   * @param position
   *            The position on map (not on screen)
   * @param offsetX
   *            The number of cells at right of the given position.
   * @param offsetY
   *            The number of cells over the given position.
   * @return The tile around the given position | null
   */
  def getTile(position: Vector2, offsetX: Int, offsetY: Int): TiledMapTile = {
    try {
      val x = (position.x / tiledLayer1.getTileWidth).toInt + offsetX
      val y = (position.y / tiledLayer1.getTileHeight).toInt + offsetY

      tiledLayer1.getCell(x, y).getTile
    } catch {
      case _: Exception =>
        null
    }
  }

  /**
   * Changes tile at position x,y on map to new tile
   * @param map1
   * @param layer
   *
   * @param x - position
   * @param y - position
   * @param newID - ID of new Tile
   */
  def changeTile(map1: TiledMap, layer: TiledMapTileLayer, x: Int, y: Int, newID: Int): Unit = {
    require(layer != null)

    var tileSet = map1.getTileSets.getTileSet(0)
    var newTile = tileSet.getTile(newID)

    val cell = new Cell()
    layer.setCell(x,y,cell)

    if(newID < 270){
      tileSet = map1.getTileSets.getTileSet(0)
    } else {
      tileSet = map1.getTileSets.getTileSet(1)

    }
    newTile = tileSet.getTile(newID)


    if (newTile != null) {
      cell.setTile(newTile) // Réutiliser l'ancienne tuile au lieu de créer une nouvelle pour garder les propriétés
    }
  }

  /**
   * Create a custom map that is random each time based on the orinal map created on Tiled
   * @param originalMap
   * @return new map
   */
  private def createCustomMap(originalMap: TiledMap): TiledMap = {

    //Object tiles ID
    val objectID = mutable.HashMap[String, Int](
      "mirror" -> 202,
      "coins" -> 64,
      "chest" -> 103,
      "jar" -> 242,
      "cauldron" -> 204,
      "chair" -> 243,
      "table" -> 244
    )

    //Basic tiles ID
    val noneID = 0
    val groundID = 65
    val wallID = 23

    //Create a new TiledMap
    var newMap = new TiledMap()

    //Get the tilesets from the original map and add it to the new map
    val originalTileSet1 = originalMap.getTileSets.getTileSet(0)
    val originalTileSet2 = originalMap.getTileSets.getTileSet(1)

    newMap.getTileSets.addTileSet(originalTileSet1)
    newMap.getTileSets.addTileSet(originalTileSet2)

    //Create a new layer with the same dimensions and tile size as the original
    val originalLayer1 = originalMap.getLayers.get(0).asInstanceOf[TiledMapTileLayer]
    val originalLayer2 = originalMap.getLayers.get(1).asInstanceOf[TiledMapTileLayer]

    val tileWidth = originalLayer1.getTileWidth
    val tileHeight = originalLayer1.getTileHeight

    val newLayer1 = new TiledMapTileLayer(originalLayer1.getWidth, originalLayer1.getHeight, tileWidth.toInt, tileHeight.toInt)
    val newLayer2 = new TiledMapTileLayer(originalLayer2.getWidth, originalLayer2.getHeight, tileWidth.toInt, tileHeight.toInt)

    newMap.getLayers.add(newLayer1)
    newMap.getLayers.add(newLayer2)

    //place the tiles depending on the given grid
    for(i <- gridMap.indices){
      for(j <- gridMap(0).indices){
        if(gridMap(i)(j) != 99 && gridMap(i)(j) != 0) {
          changeTile(newMap, newLayer1, i, j, groundID)
        }
      }
    }

    //place objects -
    // rooms even: mirror, chest, cauldron
    // rooms uneven: jar, table, chair
    var randObj: Int = 0

    for (i <- rooms.indices){
      if (rooms(i).nb != 1){
        if (rooms(i).nb % 2 == 0){
          placeRandomObjects(newMap, newLayer1, objectID("mirror"), rooms(i))
          placeRandomObjects(newMap, newLayer1, objectID("chest"), rooms(i))
        } else if (rooms(i).nb % 3 == 0) {
          placeRandomObjects(newMap, newLayer1, objectID("jar"), rooms(i))
          placeRandomObjects(newMap, newLayer1, objectID("table"), rooms(i))
        } else if (rooms(i).nb % 5 == 0){
          placeRandomObjects(newMap, newLayer1, objectID("chair"), rooms(i))
          placeRandomObjects(newMap, newLayer1, objectID("cauldron"), rooms(i))
        }
      }
    }

    for(i <- gridMap.indices){
      for(j <- gridMap(0).indices){
        if(gridMap(i)(j) == 99){
          changeTile(newMap, newLayer1, i, j, wallID)
        } else if(gridMap(i)(j) == 0){
          changeTile(newMap, newLayer1, i, j, noneID)
        }
      }
    }

    //place portal in far room
    //portal tile id : 562
    placePortal(newMap, 562)

    return newMap
  }

  private def placeRandomObjects(map1: TiledMap, layer: TiledMapTileLayer, objectID: Int, room: Room): Unit = {
    val rand = new Random()

    // dimensions of room
    val roomWidth = room.roomGrid.length
    val roomHeight = room.roomGrid(0).length

    // find room's coordinate in main grid
    var roomX : Int = 0
    var roomY : Int = 0
    var leaveLoop: Boolean = false

    var i: Int = 0
    var j: Int = 0

    while(!leaveLoop && i < gridMap.length){
      while(!leaveLoop && j < gridMap(0).length){
        if (gridMap(i)(j) == room.nb){
          roomX = i
          roomY = j
          leaveLoop = true
        }
        j += 1
      }
      i += 1
      j = 0
    }

    // random coordinates to place object
    val xRand = roomX + 1 + rand.nextInt(roomWidth)
    val yRand = roomY + 1 + rand.nextInt(roomHeight)

    changeTile(map1, layer, xRand, yRand, objectID)

  }

  private def placePortal(map1: TiledMap, portalID: Int): Unit = {

    val layer1 = map1.getLayers.get(0).asInstanceOf[TiledMapTileLayer]
    val layer2 = map1.getLayers.get(1).asInstanceOf[TiledMapTileLayer]

    var leaveLoop: Boolean = false
    var x: Int = 0
    var y: Int = 0


    var i: Int = 0
    var j: Int = 0

    while(!leaveLoop && i < gridMap.length){
      while(!leaveLoop && j < gridMap(0).length){
        if(gridMap(i)(j) == 37){
          x = i
          y = j
          leaveLoop = true
        }
        j += 1
      }
      i += 1
      j = 0
    }

    //place portal
    //put tile as a ground tile in case an object was occupying it
    changeTile(map1, layer2, x+2, y+2, portalID)
    changeTile(map1, layer1, x+2, y+2, 65)

    changeTile(map1, layer2, x+1, y+2, portalID)
    changeTile(map1, layer1, x+1, y+2, 65)

    changeTile(map1, layer2, x+2, y+1, portalID)
    changeTile(map1, layer1, x+2, y+1, 65)

    changeTile(map1, layer2, x+1, y+1, portalID)
    changeTile(map1, layer1, x+1, y+1, 65)

    portalCoordinates.addOne((x+2, y+2))
    portalCoordinates.addOne((x+1, y+2))
    portalCoordinates.addOne((x+2, y+1))
    portalCoordinates.addOne((x+1, y+1))


  }

  def isEnd(): Boolean = {
    var heroPosition = hero.position

    val heroX = (heroPosition.x.toInt / tiledLayer2.getTileWidth).toInt
    val heroY = (heroPosition.y.toInt / tiledLayer2.getTileWidth).toInt

    if(portalCoordinates.contains((heroX, heroY))){
      Thread.sleep(1000)
      return true
    }
    return false
  }

  /**
   * Get the "isWalkable" property of the given tile.
   *
   * @param tile
   *            The tile to know the property
   * @return true if the property is set to "true", false otherwise
   */
  def isWalkable(tile: TiledMapTile): Boolean = {
    if (tile == null) return false

    val walkable = tile.getProperties.get("isWalkable")

    if(walkable == true){
      return true
    }
    return false
  }

  def isOccupied(vector: Vector2) : Boolean = {
    for (enemy: Enemy <- Enemy.enemyArray){
      if (vector.x == enemy.position.x && vector.y == enemy.position.y) return false
    }
    return true
  }

  /**
   * Get the "speed" property of the given tile.
   *
   * @param tile
   *            The tile to know the property
   * @return The value of the property
   */
  private def getSpeed(tile: TiledMapTile): Float = {
    val test = tile.getProperties.get("speed")
    java.lang.Float.parseFloat(test.toString)
  }

  /**
   * Manage the movements of the hero using the keyboard.
   */
  def manageHero(): Unit = {
    // Do nothing if hero is already moving
    if (!hero.isMoving) {

      // Compute direction and next cell
      var nextCell: TiledMapTile = null
      var goalDirection: Hero.Direction.Value = Hero.Direction.NULL
      var vectorOffset : Vector2 = new Vector2(0,0)
      if (keyStatus(Input.Keys.RIGHT)) {
        goalDirection = Hero.Direction.RIGHT
        nextCell = getTile(hero.getPosition, 1, 0)
        vectorOffset = new Vector2(32,0)
      } else if (keyStatus(Input.Keys.LEFT)) {
        goalDirection = Hero.Direction.LEFT
        nextCell = getTile(hero.getPosition, -1, 0)
        vectorOffset = new Vector2(-32,0)
      } else if (keyStatus(Input.Keys.UP)) {
        goalDirection = Hero.Direction.UP
        nextCell = getTile(hero.getPosition, 0, 1)
        vectorOffset = new Vector2(0,32)
      } else if (keyStatus(Input.Keys.DOWN)) {
        goalDirection = Hero.Direction.DOWN
        nextCell = getTile(hero.getPosition, 0, -1)
        vectorOffset = new Vector2(0,-32)
      }
//      println(s"Hero position ${hero.position}")
//      println(s"Enemy position ${enemy1.position}")
//      println(s"Offset position ${vectorOffset}")
     //println(isWalkable(nextCell))
      val nextPositionVector: Vector2 = new Vector2(hero.getPosition.x+vectorOffset.x, hero.getPosition.y+vectorOffset.y)
      // Is the move valid ?
      if (isWalkable(nextCell) && isOccupied(nextPositionVector)) {
        // Go
        hero.setSpeed(getSpeed(nextCell))
        hero.go(goalDirection)
      } else {
        // Face the wall
        hero.turn(goalDirection)
      }
    }
  }

  // Manage keyboard events
  override def onKeyUp(keycode: Int): Unit = {
    super.onKeyUp(keycode)
    keyStatus.put(keycode, false)
  }

  override def onKeyDown(keycode: Int): Unit = {
    super.onKeyDown(keycode)

    keycode match {
      case Input.Keys.Z =>
        zoom match {
          case 1.0f => zoom = 0.5f
          case 0.5f => zoom = 2.0f
          case _ => zoom = 1.0f
        }
      case _ =>
    }
    keyStatus.put(keycode, true)
  }
}

object GameScreen {
  def main(args: Array[String]): Unit = {
    new GameScreen(700,700)
  }
}
