package ch.hevs.gdx2d.game.screens

import ch.hevs.gdx2d.components.audio.MusicPlayer
import ch.hevs.gdx2d.components.screen_management.RenderingScreen
import ch.hevs.gdx2d.game.rooms.{Room, GenerateRooms}
import ch.hevs.gdx2d.game.Main
import ch.hevs.gdx2d.game.entity.{Enemy, Hero}
import ch.hevs.gdx2d.lib.{GdxGraphics, ScreenManager}
import com.badlogic.gdx.files.FileHandle
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator.FreeTypeFontParameter
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer.Cell
import com.badlogic.gdx.{Gdx, Input, InputMultiplexer}
import com.badlogic.gdx.maps.tiled._
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.utils.Timer
import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer
import scala.util.Random



class GameScreen extends RenderingScreen{

  // key management
  val keyStatus: mutable.Map[Int, Boolean] = mutable.TreeMap[Int, Boolean]()

  // characters
  var hero: Hero = _

  //manage end of game
  private var lostGame: Boolean = false
  private var wonGame: Boolean = false
  private var remainingTime: Int = 60
  private var portalEntryTime: Long = 0

  //manage fonts
  private var  font80: BitmapFont = _
  private var font20: BitmapFont = _

  //screen management:
  private val stage = new Stage()
  private val multiplexer: InputMultiplexer = new InputMultiplexer()
  multiplexer.addProcessor(stage)
  multiplexer.addProcessor(Gdx.input.getInputProcessor)
  Gdx.input.setInputProcessor(multiplexer)

  //Grid that will represent the map
  private val gridPerso: GenerateRooms = new GenerateRooms
  private val gridMap = gridPerso.grid
  gridPerso.generateRooms(gridMap)
  var rooms: ArrayBuffer[Room] = gridPerso.rooms
  gridPerso.placeWalls(gridMap)

  //Portal Tiles:
  private val portalID: Int = 562
  private val portalCoordinates: ArrayBuffer[(Int, Int)] = ArrayBuffer.empty

  //manage sounds
  private var footsteps: MusicPlayer = _
  private var winSound: MusicPlayer = _
  private var lostSound: MusicPlayer = _

 // tiles management
  private var tiledMap: TiledMap = _
  var tiledMapRenderer: TiledMapRenderer = _
  // we have two layers
  private var tiledLayer1: TiledMapTileLayer = _
  private var tiledLayer2: TiledMapTileLayer = _
  var zoom: Float = _


  override def onInit(): Unit = {

    //fonts
    val optimusF: FileHandle = Gdx.files.internal("data/font/OptimusPrinceps.ttf")

    val generator: FreeTypeFontGenerator = new FreeTypeFontGenerator(optimusF)
    val parameter: FreeTypeFontParameter = new FreeTypeFontParameter()

    parameter.size = 80
    font80 = generator.generateFont(parameter)
    font80.setColor(Color.WHITE)

    //font for remaining time
    parameter.size = 20
    font20 = generator.generateFont(parameter)
    font20.setColor(Color.WHITE)

    generator.dispose()

    //sound source
    footsteps = new MusicPlayer("data/sound/footsteps-boots-short.mp3")
    winSound = new MusicPlayer("data/sound/yay.mp3")
    lostSound = new MusicPlayer("data/sound/boo.mp3")

    //create hero in room number 1
    hero = new Hero(2,2)

    //initial zoom, has to be small so the game isnt too easy, ideal: 0.3f
    zoom = 0.3f

    //init keys status
    keyStatus.put(Input.Keys.UP, false)
    keyStatus.put(Input.Keys.DOWN, false)
    keyStatus.put(Input.Keys.LEFT, false)
    keyStatus.put(Input.Keys.RIGHT, false)

    //create map
    try {
      //original map with different tiles used to make our custom map
      val exampleMap: TiledMap = new TmxMapLoader().load("data/maps/map.tmx")

      //custom map -> random each time
      tiledMap = createCustomMap(exampleMap)

      tiledMapRenderer = new OrthogonalTiledMapRenderer(tiledMap)

      val layers = tiledMap.getLayers
      tiledLayer1 = layers.get(0).asInstanceOf[TiledMapTileLayer]
      tiledLayer2 = layers.get(1).asInstanceOf[TiledMapTileLayer]

    } catch {
      case e: Exception => e.printStackTrace()
    }

    //create a timer that goes from remainingTime to 0
    Timer.schedule(new Timer.Task(){
      override def run(): Unit = {
        //decrement each second
        remainingTime -= 1
        if (remainingTime <= 0) {
          lostGame = true
        }
      }
    }, 1, 1) //parameters: task, delaySeconds, intervalSeconds
  }

  override def onGraphicRender(g: GdxGraphics): Unit = {
    g.clear()

    // Hero activity
    manageHero()
    manageEnemy()

    //camera that follows the hero
    g.zoom(zoom)
    g.moveCamera(hero.getPosition.x, hero.getPosition.y, tiledLayer1.getWidth * tiledLayer1.getTileWidth, tiledLayer1.getHeight * tiledLayer1.getTileHeight)

    // Render the tilemap
    tiledMapRenderer.setView(g.getCamera)
    tiledMapRenderer.render()

    //draw hero
    hero.animate(Gdx.graphics.getDeltaTime)
    hero.draw(g)

    //draw each enemy
    for(e <- Enemy.enemyArray){
      e.animate(Gdx.graphics.getDeltaTime)
      e.draw(g)
    }


    // to adapt to lostScreen and wonScreen
    g.zoom(1f)
    //display remaining time
    g.drawString(g.getCamera.position.x - 150, g.getCamera.position.y - 70, s"Time left: $remainingTime s", font20)
    //move camera to adapt to next screens (lostScreen and wonScreen)
    g.moveCamera(20,5)


    //if hero gets caught -> looses the game
    if (enemySeeHero(hero.getPosition, Enemy.enemyArray)) {
      lostGame = true
    }

    //checks if player won or not
    gameWon()


    if (wonGame){
      Enemy.enemyArray.clear()
      winSound.play()
      Main.instance.s.transitionTo(3, ScreenManager.TransactionType.SLICE)
    }

    if (lostGame && !wonGame) {
      Enemy.enemyArray.clear()
      lostSound.play()
      Main.instance.s.transitionTo(2, ScreenManager.TransactionType.SLICE)
    }

    //Optional
    g.drawFPS()
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
   * @param map1: TiledMap
   * @param layer: TiledMapLayer
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

    // depends on the layer -> portal tiles are on second layer
    if(newID < 270){
      tileSet = map1.getTileSets.getTileSet(0)
    } else {
      tileSet = map1.getTileSets.getTileSet(1)

    }
    newTile = tileSet.getTile(newID)

    if (newTile != null) {
      cell.setTile(newTile)
    }
  }

  /**
   * Create a custom map that is random each time based on the orinal map created on Tiled
   * @param originalMap: TiledMap
   * @return new map with groun, walls, objects and enemys placed
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

    //basic tiles ID
    val noneID = 0
    val groundID = 65
    val wallID = 23

    //breate a new TiledMap
    val newMap = new TiledMap()

    //get the tilesets from the original map and add it to the new map
    val originalTileSet1 = originalMap.getTileSets.getTileSet(0)
    val originalTileSet2 = originalMap.getTileSets.getTileSet(1)

    newMap.getTileSets.addTileSet(originalTileSet1)
    newMap.getTileSets.addTileSet(originalTileSet2)

    //create a new layer with the same dimensions and tile size as the original
    val originalLayer1 = originalMap.getLayers.get(0).asInstanceOf[TiledMapTileLayer]
    val originalLayer2 = originalMap.getLayers.get(1).asInstanceOf[TiledMapTileLayer]

    val tileWidth = originalLayer1.getTileWidth
    val tileHeight = originalLayer1.getTileHeight

    val newLayer1 = new TiledMapTileLayer(originalLayer1.getWidth, originalLayer1.getHeight, tileWidth.toInt, tileHeight.toInt)
    val newLayer2 = new TiledMapTileLayer(originalLayer2.getWidth, originalLayer2.getHeight, tileWidth.toInt, tileHeight.toInt)

    newMap.getLayers.add(newLayer1)
    newMap.getLayers.add(newLayer2)


    //place the tiles depending on our grid
    for(i <- gridMap.indices){
      for(j <- gridMap(0).indices){
        if(gridMap(i)(j) != 99 && gridMap(i)(j) != 0) {
          changeTile(newMap, newLayer1, i, j, groundID)
        }
      }
    }

    //place objects
    for (i <- rooms.indices){
      if (rooms(i).nb != 1) {
        if (rooms(i).nb % 2 == 0){
          placeRandomObjects(newMap, newLayer1, objectID("mirror"), rooms(i))
          placeRandomObjects(newMap, newLayer1, objectID("chest"), rooms(i))
        } else if (rooms(i).nb % 3 == 0) {
          placeRandomEnemy(rooms(i))
          placeRandomObjects(newMap, newLayer1, objectID("jar"), rooms(i))
          placeRandomObjects(newMap, newLayer1, objectID("table"), rooms(i))
        } else if (rooms(i).nb % 5 == 0){
          placeRandomObjects(newMap, newLayer1, objectID("chair"), rooms(i))
          placeRandomObjects(newMap, newLayer1, objectID("cauldron"), rooms(i))
        }
      }
    }


    //place walls and emptiness
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
    placePortal(newMap)

    newMap
  }

  private def placeRandomObjects(map1: TiledMap, layer: TiledMapTileLayer, objectID: Int, room: Room): Unit = {
    val rand = new Random()

    val roomWidth = room.roomGrid.length
    val roomHeight = room.roomGrid(0).length

    //find room's coordinate in main grid
    var roomX : Int = 0
    var roomY : Int = 0
    var leaveLoop: Boolean = false

    var i: Int = 0
    var j: Int = 0

    //we loop until we find the first cell that belongs to the room
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

    //random coordinates to place object
    val xRand = roomX + rand.nextInt(roomWidth)
    val yRand = roomY + rand.nextInt(roomHeight)

    changeTile(map1, layer, xRand, yRand, objectID)

  }

  private def placePortal(map1: TiledMap): Unit = {

    // we need both layers in case we have to put the portal on top of an object
    val layer1 = map1.getLayers.get(0).asInstanceOf[TiledMapTileLayer]
    val layer2 = map1.getLayers.get(1).asInstanceOf[TiledMapTileLayer]

    var leaveLoop: Boolean = false

    //coordinates
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

    //add these coordinates to an array to test the winning condition
    portalCoordinates.addOne((x+2, y+2))
    portalCoordinates.addOne((x+1, y+2))
    portalCoordinates.addOne((x+2, y+1))
    portalCoordinates.addOne((x+1, y+1))

  }

  private def placeRandomEnemy(room: Room) : Unit = {
    require(room.nb != 1)

    val rand = new Random

    val roomWidth: Int = room.roomGrid.length
    val roomHeight: Int = room.roomGrid(0).length

    var roomX: Int = 0
    var roomY: Int = 0
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

    val xRand = roomX + rand.nextInt(roomWidth - 1)
    val yRand = roomY + rand.nextInt(roomHeight - 1)

    Enemy.genEnemy(xRand,yRand)
  }

  /**
   * Method that checks if the player won the game or not
   */
  private def gameWon(): Unit = {
    val heroX = (hero.position.x.toInt / tiledLayer2.getTileWidth).toInt
    val heroY = (hero.position.y.toInt / tiledLayer2.getTileWidth).toInt

    //we check if the hero is on top of the portal and if 2 seconds later, he is still there
    if(portalCoordinates.contains((heroX, heroY))){
      if(portalEntryTime == 0){
        portalEntryTime = System.currentTimeMillis()
      } else if (System.currentTimeMillis() - portalEntryTime >= 2000)
        wonGame = true
    } else {
      //if the hero left the portal -> we put portalEntryTime back to 0
      portalEntryTime = 0
    }
  }

  /**
   * Get the "isWalkable" property of the given tile.
   *
   * @param tile The tile to know the property
   * @return true if the property is set to "true", false otherwise
   */
  private def isWalkable(tile: TiledMapTile): Boolean = {
    if (tile == null) return false

    val walkable = tile.getProperties.get("isWalkable")

    if(walkable == true){
      return true
    }
    false
  }

  private def isOccupiedEnemy(vector: Vector2) : Boolean = {
    for (enemy: Enemy <- Enemy.enemyArray){
      if (vector.x == enemy.position.x && vector.y == enemy.position.y) return false
    }
    true
  }

  private def isOccupiedHero(vector : Vector2) : Boolean = {
    if (vector.x == hero.position.x && vector.y == hero.position.y) return false
    true
  }

  /**
   * Enemy sees hero if hero is in a cell surrounding the enemy
   *
   * @param position - position of hero
   * @param listEnemy - list of all current enemies on the map
   * @return
   */
  private def enemySeeHero(position : Vector2, listEnemy : ArrayBuffer[Enemy]) : Boolean = {
    for (i <- listEnemy){
      if (math.abs(position.x-i.getPosition.x)<2*32 && math.abs(position.y-i.getPosition.y)<2*32){
        return true
      }
    }
    false
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
    //do nothing if hero is already moving
    if (!hero.isMoving) {
      //dompute direction and next cell
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

      val nextPositionVector: Vector2 = new Vector2(hero.getPosition.x+vectorOffset.x, hero.getPosition.y+vectorOffset.y)
      //is move valid ?
      if (isWalkable(nextCell) && isOccupiedEnemy(nextPositionVector)) {
        //go
        hero.setSpeed(getSpeed(nextCell))
        footsteps.play()
        hero.go(goalDirection)
      } else {
        // Face the wall
        hero.turn(goalDirection)
      }
    }
  }

  /**
   * Similar to manageHero but we can't control the enemies
   */
  private def manageEnemy(): Unit = {
    for (i <- Enemy.enemyArray){
      var nextCell: TiledMapTile = null
      var goalDirection: Enemy.Direction.Value = Enemy.Direction.NULL
      var vectorOffset: Vector2 = new Vector2(0, 0)
      if (math.random()>0.99) {
        if (math.random()<0.25){
          goalDirection = Enemy.Direction.RIGHT
          nextCell = getTile(i.getPosition, 1, 0)
          vectorOffset = new Vector2(32,0)
        } else if (math.random()>=0.25 && math.random()<0.50) {
          goalDirection = Enemy.Direction.LEFT
          nextCell = getTile(i.getPosition, -1, 0)
          vectorOffset = new Vector2(32,0)
        } else if (math.random() >= 0.50 && math.random() < 0.75) {
          goalDirection = Enemy.Direction.UP
          nextCell = getTile(i.getPosition, 0, 1)
          vectorOffset = new Vector2(32,0)
        } else if (math.random() >= 0.75 && math.random() < 1) {
          goalDirection = Enemy.Direction.DOWN
          nextCell = getTile(i.getPosition, 0, -1)
          vectorOffset = new Vector2(32,0)
        }
        val nextPositionVector: Vector2 = new Vector2(i.getPosition.x+vectorOffset.x, i.getPosition.y+vectorOffset.y)
        if (isWalkable(nextCell) && isOccupiedHero(nextPositionVector) && isOccupiedEnemy(nextPositionVector)) {
          // Go
          i.setSpeed(getSpeed(nextCell))
          i.go(goalDirection)
        } else {
          // Face the wall
          i.turn(goalDirection)
        }
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
    keyStatus.put(keycode, true)
  }

  override def dispose(): Unit = {
    super.dispose()
  }
}

