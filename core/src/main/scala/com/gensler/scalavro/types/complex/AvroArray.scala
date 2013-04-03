package com.gensler.scalavro.types.complex

import com.gensler.scalavro.types.{AvroType, AvroComplexType}
import com.gensler.scalavro.types.primitive.AvroNull
import com.gensler.scalavro.JsonSchemaProtocol._

import spray.json._

import scala.reflect.runtime.universe._
import scala.collection.immutable.ListMap
import scala.util.Success

class AvroArray[T: TypeTag] extends AvroComplexType[Seq[T]] {

  type ItemType = T

  val typeName = "array"

  // name, type, fields, symbols, items, values, size
  override def schema() = new JsObject(ListMap(
    "type"  -> typeName.toJson,
    "items" -> typeSchemaOrNull[T]
  ))

  override def parsingCanonicalForm(): JsValue = new JsObject(ListMap(
    "type"  -> typeName.toJson,
    "items" -> {
      AvroType.fromType[ItemType].map { _.canonicalFormOrFullyQualifiedName } getOrElse AvroNull.schema
    }
  ))

  def dependsOn(thatType: AvroType[_]) = AvroType.fromType[ItemType] match {
    case Success(avroTypeOfItems) => {
      avroTypeOfItems == thatType || (avroTypeOfItems dependsOn thatType)
    }
    case _ => false
  }

}