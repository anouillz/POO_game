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



class MapManager(var width: Int, var height: Int) extends PortableApplication(width, height) {

  // key management
  private val keyStatus: mutable.Map[Int, Boolean] = mutable.TreeMap[Int, Boolean]()

  // character
  private var hero: Hero = _

  var gridPerso: MondrianRoomsWalls = new MondrianRoomsWalls
  var gridMap = gridPerso.grid
  gridPerso.generateRooms(gridMap)
  gridPerso.placeWalls(gridMap)
  gridPerso.printGrid(gridMap)


  // tiles management
  private var tiledMap: TiledMap = _
  private var tiledMapRenderer: TiledMapRenderer = _
  private var tiledLayer: TiledMapTileLayer = _
  private var zoom: Float = _



  override def onInit(): Unit = {

    // Create hero
    hero = new Hero(5, 5)

    // Set initial zoom
    zoom = 0.7f

    // init keys status
    keyStatus.put(Input.Keys.UP, false)
    keyStatus.put(Input.Keys.DOWN, false)
    keyStatus.put(Input.Keys.LEFT, false)
    keyStatus.put(Input.Keys.RIGHT, false)

    // create map
    try {
      val exampleMap: TiledMap = new TmxMapLoader().load("data/maps/mapTest1.tmx")

      //Custom map -> random each time
      tiledMap = createCustomMap(exampleMap)

      tiledMapRenderer = new OrthogonalTiledMapRenderer(tiledMap)
      tiledLayer = tiledMap.getLayers.get(0).asInstanceOf[TiledMapTileLayer]

    } catch {
      case e: Exception => e.printStackTrace()
    }
  }

  override def onGraphicRender(g: GdxGraphics): Unit = {
    g.clear()

    // Hero activity
    manageHero()

    // Camera follows the hero
    g.zoom(zoom)
    g.moveCamera(hero.getPosition.x, hero.getPosition.y, tiledLayer.getWidth * tiledLayer.getTileWidth, tiledLayer.getHeight * tiledLayer.getTileHeight)

    // Render the tilemap
    tiledMapRenderer.setView(g.getCamera)
    tiledMapRenderer.render()

    // Draw the hero
    hero.animate(Gdx.graphics.getDeltaTime)
    hero.draw(g)

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
  private def getTile(position: Vector2, offsetX: Int, offsetY: Int): TiledMapTile = {
    try {
      val x = (position.x / tiledLayer.getTileWidth).toInt + offsetX
      val y = (position.y / tiledLayer.getTileHeight).toInt + offsetY

      tiledLayer.getCell(x, y).getTile
    } catch {
      case _: Exception =>
        null
    }
  }

  def changeTile(map1: TiledMap, layer: TiledMapTileLayer, x: Int, y: Int, newID: Int): Unit = {
    require(layer != null)

    var cell = layer.getCell(x, y)
    if (cell == null) {
      cell = new Cell()
      layer.setCell(x, y, cell)
    }

    val tileSet = map1.getTileSets.getTileSet(0)
    val newTile = tileSet.getTile(newID)

    if (newTile != null) {
      cell.setTile(newTile) // Réutiliser l'ancienne tuile au lieu de créer une nouvelle pour garder les propriétés
    }
  }

  private def createCustomMap(originalMap: TiledMap): TiledMap = {
    // Create a new TiledMap
    val newMap = new TiledMap()

    // Get the tileset from the original map and add it to the new map
    val originalTileSet = originalMap.getTileSets.getTileSet(0)
    newMap.getTileSets.addTileSet(originalTileSet)

    // Create a new layer with the same dimensions and tile size as the original
    val originalLayer = originalMap.getLayers.get(0).asInstanceOf[TiledMapTileLayer]
    val tileWidth = originalLayer.getTileWidth
    val tileHeight = originalLayer.getTileHeight
    val newLayer = new TiledMapTileLayer(originalLayer.getWidth, originalLayer.getHeight, tileWidth.toInt, tileHeight.toInt)

    newMap.getLayers.add(newLayer)

    var groundID: Int = 65
    var wallID: Int = 23
    var noneID: Int = 0

    for(i <- gridMap.indices){
      for(j <- gridMap(0).indices){
        if(gridMap(i)(j) != 99 && gridMap(i)(j) != 0) {
          changeTile(newMap, newLayer, i, j, groundID)
        } else if(gridMap(i)(j) == 99){
          changeTile(newMap, newLayer, i, j, wallID)
        } else if(gridMap(i)(j) == 0){
          changeTile(newMap, newLayer, i, j, noneID)
        }
      }
    }

    return newMap
  }

  /**
   * Get the "walkable" property of the given tile.
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

      if (keyStatus(Input.Keys.RIGHT)) {
        goalDirection = Hero.Direction.RIGHT
        nextCell = getTile(hero.getPosition, 1, 0)
      } else if (keyStatus(Input.Keys.LEFT)) {
        goalDirection = Hero.Direction.LEFT
        nextCell = getTile(hero.getPosition, -1, 0)
      } else if (keyStatus(Input.Keys.UP)) {
        goalDirection = Hero.Direction.UP
        nextCell = getTile(hero.getPosition, 0, 1)
      } else if (keyStatus(Input.Keys.DOWN)) {
        goalDirection = Hero.Direction.DOWN
        nextCell = getTile(hero.getPosition, 0, -1)
      }

      // Is the move valid ?
      if (isWalkable(nextCell)) {
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

object MapManager {
  def main(args: Array[String]): Unit = {
    new MapManager(700,700)
  }
}
