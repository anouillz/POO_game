import scala.collection.mutable

var objectID = mutable.HashMap[String, Int](
  "ground" -> 65,
  "wall" -> 23,
  "none" -> 0,
  "mirror" -> 202,
  "coins" -> 64,
  "chest" -> 103,
  "jar" -> 242,
  "cauldron" -> 204,
  "chair" -> 243,
  "table" -> 244
)

objectID("ground")
