package jigg.util

/*
 Copyright 2013-2015 Hiroshi Noji

 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

     http://www.apache.org/licencses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitation under the License.
*/

import ucar.nc2.{Attribute, Group, NetcdfFile}

class HDF5Object(rootGroup: Group) {

  def checkAndGetAttribute(name: String): Attribute = Option(rootGroup.findAttribute(name)) match {
    case Some(x) => x
    case None => throw new IllegalArgumentException("cannot get " + name + " attribute from input model file")
  }

  def checkAndGetGroup(name: String): Group = Option(rootGroup.findGroup(name)) match {
    case Some(x) => x
    case None => throw new IllegalArgumentException("cannot get " + name + " group from input model file")
  }

}

object HDF5Object {

  // Load from a path on the file system
  def fromFile(path: String): HDF5Object =  {
    val file = NetcdfFile.open(path, null)
    mkObj(file)
  }

  // Load from class loader
  def fromResource(path: String): HDF5Object = {
    val file =
      NetcdfFile.openInMemory(IOUtil.findResource(path).toURI)
    mkObj(file)
  }

  private def mkObj(file: NetcdfFile) = {
    val group = file.getRootGroup
    new HDF5Object(group)
  }
}
