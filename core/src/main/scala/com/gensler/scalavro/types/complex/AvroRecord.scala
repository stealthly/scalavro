package com.gensler.scalavro.types.complex

import com.gensler.scalavro.types.{ AvroType, AvroNamedType, SelfDescribingSchemaHelpers }
import com.gensler.scalavro.{ JsonSchemifiable, CanonicalForm }
import com.gensler.scalavro.JsonSchemaProtocol._

import spray.json._

import scala.collection.immutable.ListMap
import scala.reflect.runtime.universe.TypeTag

class AvroRecord[T: TypeTag](
    val name: String,
    val fields: Seq[AvroRecord.Field[_]],
    val aliases: Seq[String] = Seq(),
    val namespace: Option[String] = None,
    val doc: Option[String] = None) extends AvroNamedType[T] {

  import AvroRecord._

  val typeName = "record"

  def schema() = {
    val requiredParams = ListMap(
      "name" -> name.toJson,
      "type" -> typeName.toJson,
      "fields" -> fields.toJson,
      "namespace" -> namespace.toJson
    )

    val aliasesParam = ListMap("aliases" -> aliases).collect {
      case (k, s) if s.nonEmpty => (k, s.toJson)
    }

    val docParam = ListMap("doc" -> doc).collect {
      case (k, Some(v)) => (k, v.toJson)
    }

    new JsObject(requiredParams ++ aliasesParam ++ docParam)
  }

  def selfContainedSchema(
    resolvedSymbols: scala.collection.mutable.Set[String] = scala.collection.mutable.Set[String]()) = {
    val requiredParams = ListMap(
      "name" -> fullyQualifiedName.toJson,
      "type" -> typeName.toJson,
      "fields" -> fields.map { _.selfContainedSchema(resolvedSymbols) }.toJson
    )

    val aliasesParam = ListMap("aliases" -> aliases).collect {
      case (k, s) if s.nonEmpty => (k, s.toJson)
    }

    val docParam = ListMap("doc" -> doc).collect {
      case (k, Some(v)) => (k, v.toJson)
    }

    resolvedSymbols += this.fullyQualifiedName

    new JsObject(requiredParams ++ aliasesParam ++ docParam)
  }

  override def parsingCanonicalForm(): JsValue = new JsObject(ListMap(
    "name" -> fullyQualifiedName.toJson,
    "type" -> typeName.toJson,
    "fields" -> fields.asInstanceOf[Seq[CanonicalForm]].toJson
  ))

  override def toString(): String = {
    "%s[%s]".format(getClass.getSimpleName, name)
  }

}

object AvroRecord {

  /**
    * Records fields have:
    *
    * name: a JSON string providing the name of the field (required)
    *
    * doc: a JSON string describing this field for users (optional).
    *
    * type: A JSON object defining a schema, or a JSON string naming a record
    * definition (required).
    *
    * default: A default value for this field, used when reading instances that
    * lack this field (optional). Permitted values depend on the field's schema
    * type. Default values for union fields correspond to the first schema in
    * the union. Default values for bytes and fixed fields are JSON strings,
    * where Unicode code points 0-255 are mapped to unsigned 8-bit byte values
    * 0-255.
    *
    * order
    *
    * aliases
    */
  case class Field[U](
    name: String,
    fieldType: AvroType[U],
    default: Option[U] = None,
    order: Option[Order] = None,
    aliases: Seq[String] = Seq(),
    doc: Option[String] = None) extends JsonSchemifiable
      with CanonicalForm
      with SelfDescribingSchemaHelpers {

    def optionalParams = {
      //    val defaultParam = ListMap("default" -> default).collect {
      //      case (k, Some(u)) => (k, fieldType writeAsJson u)
      //    }
      val orderParam = ListMap("order" -> order).collect {
        case (k, Some(o)) => (k, o.schema)
      }

      val aliasesParam = ListMap("aliases" -> aliases).collect {
        case (k, s) if s.nonEmpty => (k, s.toJson)
      }

      val docParam = ListMap("doc" -> doc).collect {
        case (k, Some(v)) => (k, v.toJson)
      }

      /* defaultParam ++ */ orderParam ++ aliasesParam ++ docParam
    }

    def schema(): spray.json.JsValue = {
      val requiredParams = ListMap(
        "name" -> name.toJson,
        "type" -> fieldType.schemaOrName
      )
      new JsObject(requiredParams ++ optionalParams)
    }

    def selfContainedSchema(
      resolvedSymbols: scala.collection.mutable.Set[String] = scala.collection.mutable.Set[String]()): JsValue = {
      val requiredParams = ListMap(
        "name" -> name.toJson,
        "type" -> selfContainedSchemaOrFullyQualifiedName(fieldType, resolvedSymbols)
      )
      new JsObject(requiredParams ++ optionalParams)
    }

    def parsingCanonicalForm(): JsValue = {
      val requiredParams = ListMap(
        "name" -> name.toJson,
        "type" -> fieldType.canonicalFormOrFullyQualifiedName
      )
      //      val defaultParam = ListMap("default" -> default).collect {
      //        case (k, Some(u)) => (k, fieldType writeAsJson u) }

      val orderParam = ListMap("order" -> order).collect {
        case (k, Some(o)) => (k, o.schema)
      }

      new JsObject(requiredParams ++ /* defaultParam ++ */ orderParam)
    }

  }

  trait Order {
    def value(): String
    final def schema(): JsValue = value.toJson
  }

  object Order {
    case object Ascending extends Order { val value = "ascending" }
    case object Descending extends Order { val value = "descending" }
    case object Ignore extends Order { val value = "ignore" }
  }

}