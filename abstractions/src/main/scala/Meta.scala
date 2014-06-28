package meta
//just for fun and curiosity, not serious use

import scala.util.{Try, Success, Failure}

trait FieldMeta {
  def name: String
  def tpe: String
}

trait ClassMeta {
  def name: String
  def fieldsMeta : List[FieldMeta]
  def sqlCreate : String = {
    val fields = fieldsMeta.map{f => f.name+" "+f.tpe }.mkString(",")
    s"create table $name(id: Int, $fields );"
  }
}

trait InstanceMeta {
  def fieldValues : List[String]
  def cMeta : ClassMeta
}

trait RecordOps extends InstanceMeta {
  def id: Int
  //insertedAt, updatedBy etc
  //all fields quoted, does not matter for fun and macro sample purpose
  def sqlInsert : String = s"insert into ${cMeta.name} (id, ${cMeta.fieldsMeta.map(_.name).mkString(",")}) " +
                           s"values(${(id :: fieldValues).map(f => "'" + f + "'").mkString(",")});"
}
