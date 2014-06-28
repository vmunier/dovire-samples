package meta

import scala.annotation.StaticAnnotation
import scala.language.experimental.macros
import scala.reflect.macros.whitebox.Context

class Record extends StaticAnnotation {
  def macroTransform(annottees: Any*) = macro RecordImpl.impl
}

object RecordImpl {
  def impl(c: Context)(annottees: c.Expr[Any]*): c.Expr[Any] = {
    import c.universe._

    def stop(message: String) = c.abort(c.enclosingPosition, message)

    val (inCaseClass, inCompanionObject) = annottees.map(_.tree).toList match {
      case caseClass :: companionObject :: Nil => (caseClass, companionObject)
      case _ => stop("No case class and its companion object given")
    }

    //is order of params preserved in $params ?
    val (outCaseClass, params) = {
      val q"case class $name(..$params) extends ..$bases { ..$body }" = inCaseClass
      val fieldValues = params.collect{case q"$_ val $fname: $_ = $_" => q"$fname.toString" }
      val tree = q"""
        case class $name(..$params) extends ..$bases {
          lazy val instanceMeta = new InstanceMeta {
            def id = recordId
            val cMeta = classMeta
            val fieldValues = $fieldValues
          }
          def classMeta = ${name.toTermName}.classMeta
          def recordId: Int = -1
          ..$body
        }
      """
      (tree, params)
    }
    //println(showCode(outCaseClass))

    val fieldsMeta = params.collect {
      case q"$_ val $name: $tpt = $_" => q"""
        new FieldMeta {
          val name = ${name.toTermName.toString}
          val tpe = ${tpt.toString}
        }
      """
    }
    //println(xx)

    val outCompanionObject = {
      val q"object $name extends ..$bases { ..$body }" = inCompanionObject
      val pNames = params.collect{case q"$_ val $pname: $_ = $_" => pname }
      q"""
        object $name extends ..$bases {
          def apply(_id: Int, ..$params): ${TypeName(name.decoded)} with RecordOps =
            new ${TypeName(name.decoded)}(..$pNames) with RecordOps {
             val id=_id
             override val fieldValues = this.instanceMeta.fieldValues
             override val cMeta = classMeta
          }
          lazy val classMeta = new ClassMeta {
            val name = ${name.toTermName.toString}
            val fieldsMeta = $fieldsMeta
          }
          ..$body
        }
      """
    }
    //println(showCode(outCompanionObject))

    c.Expr[Any](Block(List(outCaseClass, outCompanionObject), Literal(Constant(()))))
  }
}


